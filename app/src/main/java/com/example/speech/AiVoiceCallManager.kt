package com.example.speech

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.example.ai.GeminiTutorService
import com.example.ai.VoiceCallAiResponse
import com.example.data.model.CallLearningReport
import com.example.data.model.CallMode
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.TutorVoice
import com.example.data.model.VoiceCallStatus
import com.example.data.model.VoiceCallTurn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class AiVoiceCallManager(
  private val context: Context,
  private val speechManager: SpeechManager,
  private val coroutineScope: CoroutineScope,
  private val targetLanguage: Language,
  private val nativeLanguage: Language,
  private val proficiencyLevel: String,
  initialMode: CallMode = CallMode.FREE_TALK,
  initialVoice: TutorVoice = TutorVoice.AOEDE,
  val isPremium: Boolean = false,
  private val onCallReportGenerated: (CallLearningReport) -> Unit
) {
  private val tag = "AiVoiceCallManager"
  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

  private val _callStatus = MutableStateFlow(VoiceCallStatus.CONNECTING)
  val callStatus = _callStatus.asStateFlow()

  private val _callSeconds = MutableStateFlow(0)
  val callSeconds = _callSeconds.asStateFlow()

  val callLimitSeconds = if (isPremium) 3000 else 300 // 50 mins vs 5 mins

  private val _isQuotaWarning = MutableStateFlow(false)
  val isQuotaWarning = _isQuotaWarning.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  val isMuted = _isMuted.asStateFlow()

  private val _isSpeakerOn = MutableStateFlow(true)
  val isSpeakerOn = _isSpeakerOn.asStateFlow()

  private val _currentMode = MutableStateFlow(initialMode)
  val currentMode = _currentMode.asStateFlow()

  private val _currentVoice = MutableStateFlow(initialVoice)
  val currentVoice = _currentVoice.asStateFlow()

  private val _liveTranscript = MutableStateFlow<List<VoiceCallTurn>>(emptyList())
  val liveTranscript = _liveTranscript.asStateFlow()

  private val _currentPartialSpeech = MutableStateFlow("")
  val currentPartialSpeech = _currentPartialSpeech.asStateFlow()

  private val _latestTutorResponse = MutableStateFlow<VoiceCallAiResponse?>(null)
  val latestTutorResponse = _latestTutorResponse.asStateFlow()

  private val _learningReport = MutableStateFlow<CallLearningReport?>(null)
  val learningReport = _learningReport.asStateFlow()

  private var timerJob: Job? = null
  private var silenceWatcherJob: Job? = null
  private var isCallActive = false

  init {
    try {
      audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
      audioManager?.isSpeakerphoneOn = true
    } catch (e: Exception) {
      Log.w(tag, "Audio routing adjustment issue", e)
    }
  }

  fun startCall() {
    isCallActive = true
    _callStatus.value = VoiceCallStatus.CONNECTING

    // Setup SpeechManager callbacks
    speechManager.setCallSpeechCallbacks(
      onPartial = { partialText ->
        if (!isCallActive || _isMuted.value) return@setCallSpeechCallbacks
        // If tutor was speaking and user begins speaking -> INTERRUPT TUTOR
        if (_callStatus.value == VoiceCallStatus.TUTOR_SPEAKING) {
          interruptTutor()
        }
        _callStatus.value = VoiceCallStatus.USER_SPEAKING
        _currentPartialSpeech.value = partialText
        resetSilenceTimer()
      },
      onResult = { finalSpeech ->
        if (!isCallActive || _isMuted.value) return@setCallSpeechCallbacks
        if (finalSpeech.isNotBlank()) {
          onUserFinishedUtterance(finalSpeech)
        } else {
          resumeListening()
        }
      },
      onSpeechStarted = {
        if (!isCallActive || _isMuted.value) return@setCallSpeechCallbacks
        if (_callStatus.value == VoiceCallStatus.TUTOR_SPEAKING) {
          interruptTutor()
        }
        _callStatus.value = VoiceCallStatus.USER_SPEAKING
        resetSilenceTimer()
      }
    )

    startTimer()

    // Generate Tutor Opening Greeting
    coroutineScope.launch {
      delay(800) // Realistic connection latency
      val openingGreeting = getOpeningGreeting(targetLanguage, _currentMode.value)
      deliverTutorResponse(
        VoiceCallAiResponse(
          replyInTargetLanguage = openingGreeting.first,
          phoneticGuide = openingGreeting.second,
          translationInNativeLanguage = openingGreeting.third,
          gentleCorrection = null
        )
      )
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = coroutineScope.launch {
      while (isActive && isCallActive) {
        delay(1000)
        _callSeconds.value += 1
        val elapsed = _callSeconds.value

        // Quota check
        if (elapsed >= (callLimitSeconds - 60) && !_isQuotaWarning.value) {
          _isQuotaWarning.value = true
        }

        if (elapsed >= callLimitSeconds) {
          // Reached daily time limit!
          endCall(limitReached = true)
          break
        }
      }
    }
  }

  private fun resetSilenceTimer() {
    silenceWatcherJob?.cancel()
    silenceWatcherJob = coroutineScope.launch {
      // Natural silence detection: If user pauses for 1.6s during speech, assume turn completion
      delay(1600)
      if (_callStatus.value == VoiceCallStatus.USER_SPEAKING && _currentPartialSpeech.value.isNotBlank()) {
        val speech = _currentPartialSpeech.value
        _currentPartialSpeech.value = ""
        onUserFinishedUtterance(speech)
      }
    }
  }

  private fun onUserFinishedUtterance(userText: String) {
    if (!isCallActive) return
    silenceWatcherJob?.cancel()
    speechManager.stopListening()

    val turn = VoiceCallTurn(
      id = UUID.randomUUID().toString(),
      speaker = MessageSender.USER,
      text = userText
    )
    _liveTranscript.value = _liveTranscript.value + turn
    _currentPartialSpeech.value = ""
    _callStatus.value = VoiceCallStatus.THINKING

    coroutineScope.launch {
      try {
        val aiResponse = GeminiTutorService.chatInVoiceCall(
          userSpeechInput = userText,
          targetLanguage = targetLanguage,
          nativeLanguage = nativeLanguage,
          proficiencyLevel = proficiencyLevel,
          mode = _currentMode.value,
          tutorVoice = _currentVoice.value,
          history = _liveTranscript.value
        )
        if (isCallActive) {
          deliverTutorResponse(aiResponse)
        }
      } catch (e: Exception) {
        Log.e(tag, "Failed to get AI call response", e)
        if (isCallActive) {
          resumeListening()
        }
      }
    }
  }

  private fun deliverTutorResponse(response: VoiceCallAiResponse) {
    if (!isCallActive) return
    _latestTutorResponse.value = response

    val tutorTurn = VoiceCallTurn(
      id = UUID.randomUUID().toString(),
      speaker = MessageSender.AI_TUTOR,
      text = response.replyInTargetLanguage,
      phoneticGuide = response.phoneticGuide,
      translation = response.translationInNativeLanguage
    )
    _liveTranscript.value = _liveTranscript.value + tutorTurn
    _callStatus.value = VoiceCallStatus.TUTOR_SPEAKING

    speechManager.speakWithVoice(
      text = response.replyInTargetLanguage,
      languageCode = targetLanguage.code,
      pitch = _currentVoice.value.pitch,
      speed = _currentVoice.value.speed,
      onDone = {
        coroutineScope.launch(Dispatchers.Main) {
          if (isCallActive && !_isMuted.value) {
            resumeListening()
          }
        }
      }
    )
  }

  fun interruptTutor() {
    Log.d(tag, "User interrupted tutor speaking")
    speechManager.stopSpeaking()
    if (isCallActive && !_isMuted.value) {
      resumeListening()
    }
  }

  fun toggleMute() {
    val newMute = !_isMuted.value
    _isMuted.value = newMute
    if (newMute) {
      speechManager.stopListening()
      _callStatus.value = VoiceCallStatus.MUTED
    } else {
      resumeListening()
    }
  }

  fun toggleSpeaker() {
    val newSpeaker = !_isSpeakerOn.value
    _isSpeakerOn.value = newSpeaker
    try {
      audioManager?.isSpeakerphoneOn = newSpeaker
    } catch (e: Exception) {
      Log.w(tag, "Failed to toggle speakerphone", e)
    }
  }

  fun switchMode(mode: CallMode) {
    _currentMode.value = mode
  }

  fun switchVoice(voice: TutorVoice) {
    _currentVoice.value = voice
  }

  private fun resumeListening() {
    if (!isCallActive || _isMuted.value) return
    coroutineScope.launch(Dispatchers.Main) {
      if (!isCallActive || _isMuted.value) return@launch
      _callStatus.value = VoiceCallStatus.LISTENING
      _currentPartialSpeech.value = ""
      speechManager.startListening(targetLanguage.code)
    }
  }

  fun endCall(limitReached: Boolean = false) {
    if (!isCallActive) return
    isCallActive = false
    timerJob?.cancel()
    silenceWatcherJob?.cancel()
    speechManager.stopSpeaking()
    speechManager.stopListening()
    _callStatus.value = VoiceCallStatus.ENDED

    // Reset audio routing
    try {
      audioManager?.mode = AudioManager.MODE_NORMAL
    } catch (ignored: Exception) {}

    // Generate comprehensive learning report
    coroutineScope.launch {
      val report = GeminiTutorService.generatePostCallReport(
        language = targetLanguage,
        nativeLanguage = nativeLanguage,
        mode = _currentMode.value,
        durationSeconds = _callSeconds.value,
        turns = _liveTranscript.value
      )
      _learningReport.value = report
      onCallReportGenerated(report)
    }
  }

  fun release() {
    isCallActive = false
    timerJob?.cancel()
    silenceWatcherJob?.cancel()
    speechManager.stopSpeaking()
    speechManager.stopListening()
    speechManager.setCallSpeechCallbacks(null, null, null)
    try {
      audioManager?.mode = AudioManager.MODE_NORMAL
    } catch (ignored: Exception) {}
  }

  private fun getOpeningGreeting(language: Language, mode: CallMode): Triple<String, String, String> {
    return when (language.code.lowercase()) {
      "es" -> when (mode) {
        CallMode.JOB_INTERVIEW -> Triple(
          "¡Hola! Bienvenido a la entrevista de hoy. Por favor, háblame un poco de ti.",
          "oh-lah! byen-beh-nee-doh ah lah en-treh-bees-tah. pohr fah-bohr, ah-blah-meh oon poh-koh deh tee.",
          "Hello! Welcome to today's interview. Please tell me a little about yourself."
        )
        CallMode.TRAVEL -> Triple(
          "¡Buenos días! Soy el encargado del aeropuerto. ¿En qué puedo asistirte con tu viaje?",
          "bweh-nohs dee-ahs! soy el en-kahr-gah-doh del ah-eh-roh-pwehr-toh. ehn keh pweh-doh ah-sees-teer-teh?",
          "Good day! I am the airport agent. How may I assist you with your travel?"
        )
        CallMode.RESTAURANT -> Triple(
          "¡Buenas tardes! Bienvenido a nuestro restaurante. ¿Le gustaría empezar con una bebida?",
          "bweh-nahs tahr-dehs! byen-beh-nee-doh ah nwehs-troh res-tow-rahn-teh. leh goos-tah-ree-ah?",
          "Good afternoon! Welcome to our restaurant. Would you like to start with a drink?"
        )
        else -> Triple(
          "¡Hola! Qué gusto hablar contigo hoy. ¿De qué te gustaría charlar?",
          "oh-lah! keh goos-toh ah-blahr kohn-tee-goh oy. deh keh teh goos-tah-ree-ah chahr-lahr?",
          "Hello! Such a pleasure to talk with you today. What would you like to chat about?"
        )
      }
      "fr" -> when (mode) {
        CallMode.JOB_INTERVIEW -> Triple(
          "Bonjour ! Bienvenue à notre entretien. Parlez-moi un peu de votre parcours.",
          "bohn-zhoor! byen-veh-noo ah noh-truh ahn-truh-tyen. par-lay mwah uhn puh deh voht-ruh par-koor.",
          "Hello! Welcome to our interview. Tell me a little about your background."
        )
        else -> Triple(
          "Bonjour ! C'est un réel plaisir de vous appeler. Comment allez-vous aujourd'hui ?",
          "bohn-zhoor! sayt uhn ray-el pleh-zeer duh vooz ah-play. koh-mahn tah-lay vooz oh-zhoor-dwee?",
          "Hello! It is a real pleasure to call you. How are you doing today?"
        )
      }
      "de" -> Triple(
        "Hallo! Schön, mit dir zu sprechen. Wie läuft dein Tag bis jetzt?",
        "hah-loh! shern, mit deer tsoo shprekh-en. vee loyft dayn tahk bis yetst?",
        "Hello! Great to speak with you. How is your day going so far?"
      )
      "ja" -> Triple(
        "こんにちは！お電話ありがとうございます。今日はどんなお話をしましょうか？",
        "kohn-nee-chee-wah! oh-den-wah ah-ree-gah-toh goh-zah-ee-mahs. kyoh wah dohn-nah oh-hah-nah-shee?",
        "Hello! Thank you for the call. What would you like to talk about today?"
      )
      else -> Triple(
        "Hello there! It is wonderful to connect with you. What would you like to practice today?",
        "he-loh dhayr! it iz wun-der-ful too kuh-nekt with yoo. wut wood yoo like too prak-tis?",
        "Hello there! It is wonderful to connect with you. What would you like to practice today?"
      )
    }
  }
}

package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

  private val mainHandler = Handler(Looper.getMainLooper())
  private var tts: TextToSpeech? = null
  private var isTtsReady = false

  private var speechRecognizer: SpeechRecognizer? = null
  private var isRecognizerListening = false

  private val _isListening = MutableStateFlow(false)
  val isListening = _isListening.asStateFlow()

  private val _rmsLevel = MutableStateFlow(0f)
  val rmsLevel = _rmsLevel.asStateFlow()

  private val _spokenText = MutableStateFlow("")
  val spokenText = _spokenText.asStateFlow()

  private val _speechError = MutableStateFlow<String?>(null)
  val speechError = _speechError.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking = _isSpeaking.asStateFlow()

  private var onSpeechDoneCallback: (() -> Unit)? = null
  private var onCallSpeechResult: ((String) -> Unit)? = null
  private var onCallSpeechPartial: ((String) -> Unit)? = null
  private var onSpeechBeginDetected: (() -> Unit)? = null

  init {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e("SpeechManager", "Failed to initialize TTS", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsReady = true
      tts?.setSpeechRate(0.95f)
      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          mainHandler.post {
            _isSpeaking.value = true
          }
        }

        override fun onDone(utteranceId: String?) {
          mainHandler.post {
            _isSpeaking.value = false
            onSpeechDoneCallback?.invoke()
          }
        }

        override fun onError(utteranceId: String?) {
          mainHandler.post {
            _isSpeaking.value = false
            onSpeechDoneCallback?.invoke()
          }
        }
      })
    } else {
      isTtsReady = false
      Log.e("SpeechManager", "TTS initialization returned failure: $status")
    }
  }

  fun speak(text: String, languageCode: String) {
    speakWithVoice(text, languageCode, pitch = 1.0f, speed = 0.95f, onDone = null)
  }

  fun speakWithVoice(
    text: String,
    languageCode: String,
    pitch: Float = 1.0f,
    speed: Float = 0.95f,
    onDone: (() -> Unit)? = null
  ) {
    if (!isTtsReady || text.isBlank()) {
      onDone?.invoke()
      return
    }

    onSpeechDoneCallback = onDone
    val targetLocale = resolveLocale(languageCode)
    val result = tts?.setLanguage(targetLocale)
    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
      tts?.setLanguage(Locale.US)
    }

    tts?.setPitch(pitch)
    tts?.setSpeechRate(speed)
    _isSpeaking.value = true

    val utteranceId = "lingo_utt_${System.currentTimeMillis()}"
    val params = Bundle().apply {
      putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
    }
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
  }

  fun stopSpeaking() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      mainHandler.post { stopSpeaking() }
      return
    }
    tts?.stop()
    _isSpeaking.value = false
    onSpeechDoneCallback = null
  }

  fun setCallSpeechCallbacks(
    onPartial: ((String) -> Unit)?,
    onResult: ((String) -> Unit)?,
    onSpeechStarted: (() -> Unit)?
  ) {
    onCallSpeechPartial = onPartial
    onCallSpeechResult = onResult
    onSpeechBeginDetected = onSpeechStarted
  }

  fun startListening(languageCode: String) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      mainHandler.post { startListening(languageCode) }
      return
    }

    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      _speechError.value = "Speech recognition is not available on this device"
      return
    }

    stopSpeaking()
    stopListening()

    try {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _speechError.value = null
          }

          override fun onBeginningOfSpeech() {
            _isListening.value = true
            onSpeechBeginDetected?.invoke()
          }

          override fun onRmsChanged(rmsdB: Float) {
            _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _isListening.value = false
          }

          override fun onError(error: Int) {
            _isListening.value = false
            _rmsLevel.value = 0f
            val message = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection error for speech"
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer to the mic."
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please retry."
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
              else -> "Speech recognition issue ($error)"
            }
            // For continuous calls, timeouts are normal during pauses between thoughts
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
              _speechError.value = message
            }
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            _rmsLevel.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestMatch = matches?.firstOrNull() ?: ""
            if (bestMatch.isNotBlank()) {
              _spokenText.value = bestMatch
              onCallSpeechResult?.invoke(bestMatch)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull() ?: ""
            if (partial.isNotBlank()) {
              _spokenText.value = partial
              onCallSpeechPartial?.invoke(partial)
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }

      val targetLocale = resolveLocale(languageCode)
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLocale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, targetLocale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
      }

      speechRecognizer?.startListening(intent)
      isRecognizerListening = true
      _isListening.value = true
    } catch (e: Exception) {
      Log.e("SpeechManager", "Error starting speech recognition", e)
      _speechError.value = e.localizedMessage ?: "Failed to start microphone"
      _isListening.value = false
    }
  }

  fun stopListening() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      mainHandler.post { stopListening() }
      return
    }

    if (isRecognizerListening) {
      try {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
      } catch (ignored: Exception) {}
      speechRecognizer = null
      isRecognizerListening = false
      _isListening.value = false
      _rmsLevel.value = 0f
    }
  }

  fun clearSpokenText() {
    _spokenText.value = ""
  }

  fun clearError() {
    _speechError.value = null
  }

  fun release() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      mainHandler.post { release() }
      return
    }
    stopListening()
    tts?.stop()
    tts?.shutdown()
    tts = null
    onCallSpeechResult = null
    onCallSpeechPartial = null
    onSpeechBeginDetected = null
  }

  private fun resolveLocale(code: String): Locale {
    return when (code.lowercase()) {
      "es" -> Locale.Builder().setLanguage("es").setRegion("ES").build()
      "fr" -> Locale.FRANCE
      "de" -> Locale.GERMANY
      "ar" -> Locale.Builder().setLanguage("ar").setRegion("SA").build()
      "zh" -> Locale.SIMPLIFIED_CHINESE
      "ja" -> Locale.JAPAN
      "ko" -> Locale.KOREA
      "hi" -> Locale.Builder().setLanguage("hi").setRegion("IN").build()
      "ur" -> Locale.Builder().setLanguage("ur").setRegion("PK").build()
      "pt" -> Locale.Builder().setLanguage("pt").setRegion("BR").build()
      "it" -> Locale.ITALIAN
      "tr" -> Locale.Builder().setLanguage("tr").setRegion("TR").build()
      "ru" -> Locale.Builder().setLanguage("ru").setRegion("RU").build()
      else -> Locale.US
    }
  }
}

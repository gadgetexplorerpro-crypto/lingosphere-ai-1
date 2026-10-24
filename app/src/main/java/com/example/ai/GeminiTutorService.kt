package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CallLearningReport
import com.example.data.model.CallMode
import com.example.data.model.GrammarCorrection
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.TutorVoice
import com.example.data.model.VocabItem
import com.example.data.model.VoiceCallTurn
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
  val contents: List<GeminiContent>,
  val systemInstruction: GeminiContent? = null,
  val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val role: String? = null,
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  val temperature: Float? = 0.7f,
  val topP: Float? = 0.95f,
  val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
  val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  val content: GeminiContent? = null
)

interface GeminiApi {
  @POST("v1beta/models/gemini-3.5-flash:generateContent")
  suspend fun generateContent(
    @Query("key") apiKey: String,
    @Body request: GeminiRequest
  ): GeminiResponse
}

data class TutorAiResponse(
  val replyInTargetLanguage: String,
  val phoneticGuide: String,
  val translationInNativeLanguage: String,
  val grammarFeedback: String? = null
)

data class VoiceCallAiResponse(
  val replyInTargetLanguage: String,
  val phoneticGuide: String,
  val translationInNativeLanguage: String,
  val gentleCorrection: String? = null
)

object GeminiTutorService {
  private const val TAG = "GeminiTutor"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/"

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val api: GeminiApi = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
    .create(GeminiApi::class.java)

  /**
   * Real-time voice call conversational turn generation.
   * Tailored for low-latency, natural turn-taking: short conversational spoken sentences.
   */
  suspend fun chatInVoiceCall(
    userSpeechInput: String,
    targetLanguage: Language,
    nativeLanguage: Language,
    proficiencyLevel: String,
    mode: CallMode,
    tutorVoice: TutorVoice,
    history: List<VoiceCallTurn>
  ): VoiceCallAiResponse = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    val systemPrompt = buildString {
      append("You are ${tutorVoice.displayName}, a warm, highly engaging personal native language tutor conducting a LIVE REAL-TIME VOICE CALL with a learner. ")
      append("Target Language: ${targetLanguage.name} (${targetLanguage.code}). ")
      append("Learner Proficiency Level: $proficiencyLevel. ")
      append("Learner Native Language: ${nativeLanguage.name} (${nativeLanguage.code}). ")
      append("CALL MODE: ${mode.title} - ${mode.description}\n")
      append("MODE INSTRUCTION: ${mode.systemInstruction}\n\n")
      append("CRITICAL REAL-TIME VOICE CALL GUIDELINES:")
      append("\n1. Speak naturally as in a phone call. Keep replies concise (1 to 2 spoken sentences maximum) so conversation flows back and forth.")
      append("\n2. Always end your turn with an engaging, friendly question to encourage the student to speak next.")
      append("\n3. If the student makes an obvious grammar error, naturally weave the correct phrasing into your response, or gently provide the correction in the CORRECTION field.")
      append("\n4. STRICT RESPONSE FORMAT (4 labeled lines only, no markdown asterisks):\n")
      append("REPLY: <Concise response in ${targetLanguage.name} to be spoken aloud by TTS>\n")
      append("PHONETICS: <Phonetic transcription or pronunciation guide for the reply>\n")
      append("TRANSLATION: <Natural translation of the reply in ${nativeLanguage.name}>\n")
      append("CORRECTION: <Brief correction or encouraging tip in ${nativeLanguage.name}, or NONE>\n")
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext generateOfflineCallResponse(userSpeechInput, targetLanguage, nativeLanguage, mode)
    }

    try {
      val contentList = mutableListOf<GeminiContent>()

      // Add recent call context
      history.takeLast(8).forEach { turn ->
        val role = if (turn.speaker == MessageSender.USER) "user" else "model"
        contentList.add(
          GeminiContent(
            role = role,
            parts = listOf(GeminiPart(text = turn.text))
          )
        )
      }

      contentList.add(
        GeminiContent(
          role = "user",
          parts = listOf(GeminiPart(text = userSpeechInput))
        )
      )

      val request = GeminiRequest(
        contents = contentList,
        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
        generationConfig = GeminiGenerationConfig(temperature = 0.65f, topK = 30)
      )

      val response = api.generateContent(apiKey, request)
      val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
      parseVoiceCallResponse(rawText, userSpeechInput, targetLanguage, nativeLanguage)
    } catch (e: Exception) {
      Log.e(TAG, "Voice Call Gemini API error: ${e.message}", e)
      generateOfflineCallResponse(userSpeechInput, targetLanguage, nativeLanguage, mode)
    }
  }

  private fun parseVoiceCallResponse(
    rawText: String,
    userSpeechInput: String,
    targetLanguage: Language,
    nativeLanguage: Language
  ): VoiceCallAiResponse {
    var reply = ""
    var phonetics = ""
    var translation = ""
    var correction: String? = null

    rawText.lines().forEach { line ->
      val trimmed = line.trim()
      when {
        trimmed.startsWith("REPLY:", ignoreCase = true) -> {
          reply = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("PHONETICS:", ignoreCase = true) -> {
          phonetics = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("TRANSLATION:", ignoreCase = true) -> {
          translation = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("CORRECTION:", ignoreCase = true) -> {
          val corr = trimmed.substringAfter(":").trim()
          if (!corr.equals("NONE", ignoreCase = true) && corr.isNotBlank()) {
            correction = corr
          }
        }
      }
    }

    if (reply.isBlank()) {
      reply = rawText.trim()
    }
    if (phonetics.isBlank()) {
      phonetics = "(Native pronunciation)"
    }
    if (translation.isBlank()) {
      translation = "Translation ready."
    }

    return VoiceCallAiResponse(
      replyInTargetLanguage = reply,
      phoneticGuide = phonetics,
      translationInNativeLanguage = translation,
      gentleCorrection = correction
    )
  }

  /**
   * Generates a comprehensive learning report when the call concludes.
   */
  suspend fun generatePostCallReport(
    language: Language,
    nativeLanguage: Language,
    mode: CallMode,
    durationSeconds: Int,
    turns: List<VoiceCallTurn>
  ): CallLearningReport = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val userTurns = turns.filter { it.speaker == MessageSender.USER }
    val totalTurns = turns.size
    val xpEarned = maxOf(30, (durationSeconds / 5) + (userTurns.size * 10))

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || userTurns.isEmpty()) {
      return@withContext generateOfflineCallReport(language, nativeLanguage, mode, durationSeconds, turns, xpEarned)
    }

    val transcript = buildString {
      turns.forEach { turn ->
        val who = if (turn.speaker == MessageSender.USER) "Learner" else "Tutor"
        append("$who: ${turn.text}\n")
      }
    }

    val reportPrompt = """
      You are an expert language teacher reviewing a recorded voice call with a student.
      Target Language: ${language.name} (${language.code})
      Native Language: ${nativeLanguage.name} (${nativeLanguage.code})
      Mode: ${mode.title}
      Call Duration: $durationSeconds seconds.
      
      Call Transcript:
      $transcript
      
      Analyze the student's speaking performance and output in EXACTLY this structure:
      SCORE: <Integer from 60 to 98 based on student's turns>
      FEEDBACK: <2 sentences of encouraging, actionable teacher feedback in ${nativeLanguage.name}>
      VOCAB: <Word1 in ${language.name}> | <Phonetic> | <Translation in ${nativeLanguage.name}> | <Example sentence in ${language.name}>
      VOCAB: <Word2 in ${language.name}> | <Phonetic> | <Translation in ${nativeLanguage.name}> | <Example sentence in ${language.name}>
      VOCAB: <Word3 in ${language.name}> | <Phonetic> | <Translation in ${nativeLanguage.name}> | <Example sentence in ${language.name}>
      CORRECTION: <Student's phrase> | <Better native phrasing in ${language.name}> | <Quick explanation in ${nativeLanguage.name}>
      CORRECTION: <Student's phrase> | <Better native phrasing in ${language.name}> | <Quick explanation in ${nativeLanguage.name}>
    """.trimIndent()

    try {
      val request = GeminiRequest(
        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = reportPrompt)))),
        generationConfig = GeminiGenerationConfig(temperature = 0.5f)
      )
      val response = api.generateContent(apiKey, request)
      val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

      var score = 85
      var feedback = "Great speaking practice! Your conversational confidence is developing nicely."
      val vocabList = mutableListOf<VocabItem>()
      val correctionList = mutableListOf<GrammarCorrection>()

      rawText.lines().forEach { line ->
        val trimmed = line.trim()
        when {
          trimmed.startsWith("SCORE:", ignoreCase = true) -> {
            val num = trimmed.substringAfter(":").trim().toIntOrNull()
            if (num != null) score = num.coerceIn(50, 100)
          }
          trimmed.startsWith("FEEDBACK:", ignoreCase = true) -> {
            feedback = trimmed.substringAfter(":").trim()
          }
          trimmed.startsWith("VOCAB:", ignoreCase = true) -> {
            val parts = trimmed.substringAfter(":").split("|").map { it.trim() }
            if (parts.size >= 4) {
              vocabList.add(
                VocabItem(
                  term = parts[0],
                  phonetic = parts[1],
                  translation = parts[2],
                  exampleSentence = parts[3]
                )
              )
            }
          }
          trimmed.startsWith("CORRECTION:", ignoreCase = true) -> {
            val parts = trimmed.substringAfter(":").split("|").map { it.trim() }
            if (parts.size >= 3) {
              correctionList.add(
                GrammarCorrection(
                  originalPhrase = parts[0],
                  correctedPhrase = parts[1],
                  explanation = parts[2]
                )
              )
            }
          }
        }
      }

      if (vocabList.isEmpty()) {
        vocabList.addAll(generateFallbackVocab(language))
      }

      CallLearningReport(
        sessionId = "call_${UUID.randomUUID()}",
        languageCode = language.code,
        mode = mode,
        durationSeconds = durationSeconds,
        turnsCount = totalTurns,
        fluencyScore = score,
        vocabularyLearned = vocabList,
        grammarCorrections = correctionList,
        overallFeedback = feedback,
        xpEarned = xpEarned
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed to generate report via Gemini, using fallback report", e)
      generateOfflineCallReport(language, nativeLanguage, mode, durationSeconds, turns, xpEarned)
    }
  }

  private fun generateOfflineCallResponse(
    userInput: String,
    targetLanguage: Language,
    nativeLanguage: Language,
    mode: CallMode
  ): VoiceCallAiResponse {
    return when (targetLanguage.code.lowercase()) {
      "es" -> VoiceCallAiResponse(
        replyInTargetLanguage = "¡Hola! Te escucho perfectamente. ¿Cómo ha estado tu día hasta ahora?",
        phoneticGuide = "oh-lah! teh es-koo-choh per-fek-tah-men-teh. koh-moh ah es-tah-doh too dee-ah?",
        translationInNativeLanguage = "Hello! I hear you clearly. How has your day been so far?",
        gentleCorrection = null
      )
      "fr" -> VoiceCallAiResponse(
        replyInTargetLanguage = "Bonjour ! Je vous entends très bien. De quoi aimeriez-vous parler aujourd'hui ?",
        phoneticGuide = "bohn-zhoor! zhuh vooz ahn-tahn tray byen. duh kwah eh-muh-ree-ay voo par-lay?",
        translationInNativeLanguage = "Hello! I hear you clearly. What would you like to speak about today?",
        gentleCorrection = null
      )
      "de" -> VoiceCallAiResponse(
        replyInTargetLanguage = "Hallo! Ich verstehe dich super. Erzähl mir, was hast du heute Schönes gemacht?",
        phoneticGuide = "hah-loh! ikh fer-shtay-eh dikh zoo-per. er-tsayl meer, vahs hahst doo hoy-tuh?",
        translationInNativeLanguage = "Hello! I understand you great. Tell me, what nice things did you do today?",
        gentleCorrection = null
      )
      "ja" -> VoiceCallAiResponse(
        replyInTargetLanguage = "こんにちは！声がよく聞こえますよ。今日はどんな一日でしたか？",
        phoneticGuide = "kohn-nee-chee-wah! koh-eh gah yoh-koo kee-koh-eh-mahs yoh. kyoh wah dohn-nah?",
        translationInNativeLanguage = "Hello! I can hear your voice clearly. What kind of day did you have today?",
        gentleCorrection = null
      )
      "zh" -> VoiceCallAiResponse(
        replyInTargetLanguage = "你好！我听得很清楚。今天过得怎么样？",
        phoneticGuide = "Nǐ hǎo! Wǒ tīng de hěn qīngchǔ. Jīntiān guò de zěnmeyàng?",
        translationInNativeLanguage = "Hello! I hear you very clearly. How has your day been?",
        gentleCorrection = null
      )
      else -> VoiceCallAiResponse(
        replyInTargetLanguage = "Hello there! I hear you loud and clear. What would you like to chat about today?",
        phoneticGuide = "he-loh dhayr! eye heer yoo lowd and kleer.",
        translationInNativeLanguage = "Hello! I hear you clearly. What would you like to talk about today?",
        gentleCorrection = null
      )
    }
  }

  private fun generateOfflineCallReport(
    language: Language,
    nativeLanguage: Language,
    mode: CallMode,
    durationSeconds: Int,
    turns: List<VoiceCallTurn>,
    xpEarned: Int
  ): CallLearningReport {
    val fluencyScore = if (durationSeconds > 60) 88 else 82
    val vocab = generateFallbackVocab(language)
    val corrections = listOf(
      GrammarCorrection(
        originalPhrase = "I go to store yesterday",
        correctedPhrase = "I went to the store yesterday",
        explanation = "Use past tense for actions completed in the past."
      )
    )

    return CallLearningReport(
      sessionId = "call_${UUID.randomUUID()}",
      languageCode = language.code,
      mode = mode,
      durationSeconds = durationSeconds,
      turnsCount = turns.size,
      fluencyScore = fluencyScore,
      vocabularyLearned = vocab,
      grammarCorrections = corrections,
      overallFeedback = "Great two-way conversation session! You communicated naturally and maintained the conversational tempo.",
      xpEarned = xpEarned
    )
  }

  private fun generateFallbackVocab(language: Language): List<VocabItem> {
    return when (language.code.lowercase()) {
      "es" -> listOf(
        VocabItem("conversación", "kohn-ber-sah-SYOHN", "conversation", "Tuvimos una excelente conversación."),
        VocabItem("fluidez", "floo-ee-DETH", "fluency", "Tu fluidez mejora cada día."),
        VocabItem("desarrollar", "deh-sahr-roh-YAHR", "to develop", "Practicar ayuda a desarrollar confianza.")
      )
      "fr" -> listOf(
        VocabItem("conversation", "kohn-vair-sah-SYOHN", "conversation", "Nous avons eu une belle conversation."),
        VocabItem("fluidité", "floo-ee-dee-TAY", "fluency", "Votre fluidité s'améliore constamment."),
        VocabItem("développer", "day-vlop-PAY", "to develop", "Pratiquer développe votre assurance.")
      )
      "de" -> listOf(
        VocabItem("Gespräch", "guh-SHPRAYKH", "conversation", "Wir hatten ein wunderbares Gespräch."),
        VocabItem("Flüssigkeit", "FLYOO-sig-kayt", "fluency", "Deine Sprachflüssigkeit nimmt zu."),
        VocabItem("entwickeln", "ent-VIK-eln", "to develop", "Übung hilft Fähigkeiten zu entwickeln.")
      )
      "ja" -> listOf(
        VocabItem("会話 (kaiwa)", "kah-ee-wah", "conversation", "楽しい会話ができました。"),
        VocabItem("流暢 (ryuuchou)", "ryoo-choh", "fluency", "流暢さが向上しています。"),
        VocabItem("練習 (renshuu)", "ren-shoo", "practice", "毎日の練習が大切です。")
      )
      else -> listOf(
        VocabItem("articulate", "ar-TIK-yoo-lut", "express clearly", "You were able to articulate your thoughts clearly."),
        VocabItem("tempo", "TEM-poh", "pace / rhythm", "Maintaining a steady conversational tempo builds confidence."),
        VocabItem("vocabulary", "voh-KAB-yuh-lair-ee", "words", "You utilized great vocabulary during our call.")
      )
    }
  }

  // --- Existing Speaking Chat Method preserved ---
  suspend fun chatWithTutor(
    userSpeechInput: String,
    targetLanguage: Language,
    nativeLanguage: Language,
    proficiencyLevel: String,
    scenarioTitle: String? = null,
    scenarioInstruction: String? = null,
    conversationHistory: List<Pair<String, String>> = emptyList()
  ): TutorAiResponse = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    val systemPrompt = buildString {
      append("You are LingoSphere, an expert, warm, and highly engaging AI language tutor. ")
      append("The learner is practicing ${targetLanguage.name} (${targetLanguage.code}) at the '$proficiencyLevel' level. ")
      append("Their native language is ${nativeLanguage.name} (${nativeLanguage.code}). ")
      if (!scenarioTitle.isNullOrBlank()) {
        append("CURRENT ROLEPLAY SCENARIO: '$scenarioTitle'. ")
        append("Role instruction: ${scenarioInstruction ?: ""}. Stay in character, keep the conversation natural and authentic. ")
      } else {
        append("GENERAL TUTOR MODE: Engage in friendly, natural conversation. Keep sentences appropriate for $proficiencyLevel level. ")
      }
      append("\nSTRICT RESPONSE FORMAT REQUIREMENTS:")
      append("\nYou MUST return your answer in EXACTLY four labeled sections separated by newlines:\n")
      append("REPLY: <Your conversational reply written ONLY in ${targetLanguage.name}>\n")
      append("PHONETICS: <A clear phonetic or transliterated reading guide for your reply>\n")
      append("TRANSLATION: <A natural, accurate translation of your reply into ${nativeLanguage.name}>\n")
      append("CORRECTION: <If the learner made any grammar, pronunciation, or word choice mistakes, gently explain it localized in ${nativeLanguage.name} with the correct phrasing. If their input was great, give an encouraging compliment in ${nativeLanguage.name}>\n")
      append("Do NOT include markdown asterisks, headers, or any other format.")
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      Log.w(TAG, "GEMINI_API_KEY not set or placeholder. Returning offline coaching fallback.")
      return@withContext generateOfflineFallback(userSpeechInput, targetLanguage, nativeLanguage, scenarioTitle)
    }

    try {
      val contentList = mutableListOf<GeminiContent>()

      conversationHistory.takeLast(6).forEach { (speaker, text) ->
        val role = if (speaker == "USER") "user" else "model"
        contentList.add(
          GeminiContent(
            role = role,
            parts = listOf(GeminiPart(text = text))
          )
        )
      }

      contentList.add(
        GeminiContent(
          role = "user",
          parts = listOf(GeminiPart(text = userSpeechInput))
        )
      )

      val request = GeminiRequest(
        contents = contentList,
        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
        generationConfig = GeminiGenerationConfig(temperature = 0.7f)
      )

      val response = api.generateContent(apiKey, request)
      val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
      parseTutorResponse(rawText, userSpeechInput, targetLanguage, nativeLanguage)
    } catch (e: Exception) {
      Log.e(TAG, "Gemini API error: ${e.message}", e)
      generateOfflineFallback(userSpeechInput, targetLanguage, nativeLanguage, scenarioTitle)
    }
  }

  private fun parseTutorResponse(
    rawText: String,
    userSpeechInput: String,
    targetLanguage: Language,
    nativeLanguage: Language
  ): TutorAiResponse {
    var reply = ""
    var phonetics = ""
    var translation = ""
    var correction = ""

    rawText.lines().forEach { line ->
      val trimmed = line.trim()
      when {
        trimmed.startsWith("REPLY:", ignoreCase = true) -> {
          reply = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("PHONETICS:", ignoreCase = true) -> {
          phonetics = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("TRANSLATION:", ignoreCase = true) -> {
          translation = trimmed.substringAfter(":").trim()
        }
        trimmed.startsWith("CORRECTION:", ignoreCase = true) -> {
          correction = trimmed.substringAfter(":").trim()
        }
      }
    }

    if (reply.isBlank()) {
      reply = rawText.trim()
    }
    if (phonetics.isBlank()) {
      phonetics = "(Listen to native pronunciation)"
    }
    if (translation.isBlank()) {
      translation = "Translation available upon tap."
    }

    return TutorAiResponse(
      replyInTargetLanguage = reply,
      phoneticGuide = phonetics,
      translationInNativeLanguage = translation,
      grammarFeedback = correction.ifBlank { null }
    )
  }

  private fun generateOfflineFallback(
    userSpeechInput: String,
    targetLanguage: Language,
    nativeLanguage: Language,
    scenarioTitle: String?
  ): TutorAiResponse {
    return when (targetLanguage.code.lowercase()) {
      "es" -> TutorAiResponse(
        replyInTargetLanguage = "¡Muy bien! Te escucho perfectamente. ¿Qué planes tienes para el resto del día?",
        phoneticGuide = "mwee byen! teh es-koo-choh per-fek-tah-men-teh",
        translationInNativeLanguage = "Very good! I hear you clearly. What plans do you have for the rest of the day?",
        grammarFeedback = "¡Excelente pronunciación! Tu estructura gramatical es natural."
      )
      "fr" -> TutorAiResponse(
        replyInTargetLanguage = "Très bien ! Je vous entends très bien. Quel temps fait-il chez vous aujourd'hui ?",
        phoneticGuide = "tray byen! zhuh voo zahn-tahn tray byen",
        translationInNativeLanguage = "Very good! I hear you clearly. How is the weather where you are today?",
        grammarFeedback = "Bravo pour votre accent et votre fluidité !"
      )
      "ar" -> TutorAiResponse(
        replyInTargetLanguage = "ممتاز جداً! أنا أسمعك بوضوح. ما هو موضوعك المفضل للحديث عنه اليوم؟",
        phoneticGuide = "mum-taz jid-dan! ana as-ma-uk bi-wuduh.",
        translationInNativeLanguage = "Excellent! I hear you clearly. What is your favorite topic to discuss today?",
        grammarFeedback = "أحسنت! مخارج الحروف ممتازة وواضحة."
      )
      "de" -> TutorAiResponse(
        replyInTargetLanguage = "Sehr gut! Ich verstehe dich deutlich. Was möchtest du heute noch üben?",
        phoneticGuide = "zayr goot! ikh fer-shtay-eh dikh doyt-likh",
        translationInNativeLanguage = "Very good! I understand you clearly. What else would you like to practice today?",
        grammarFeedback = "Ausgezeichnet! Deine Aussprache ist sehr klar."
      )
      "ja" -> TutorAiResponse(
        replyInTargetLanguage = "素晴らしいですね！よく聞こえます。今日の調子はいかがですか？",
        phoneticGuide = "soo-bah-rah-shee des neh! yoh-koo kee-koh-eh-mahs",
        translationInNativeLanguage = "Wonderful! I can hear you clearly. How are you feeling today?",
        grammarFeedback = "発音がとても自然です。その調子で続けましょう！"
      )
      else -> TutorAiResponse(
        replyInTargetLanguage = "Wonderful speaking practice! I understood your response. Tell me more about your thoughts on this.",
        phoneticGuide = "wun-der-ful spee-king prak-tis!",
        translationInNativeLanguage = "Great effort! Continuous speaking will build your conversational confidence.",
        grammarFeedback = "Good phrasing and natural tempo."
      )
    }
  }
}

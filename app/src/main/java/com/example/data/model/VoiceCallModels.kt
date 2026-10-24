package com.example.data.model

enum class CallMode(
  val id: String,
  val title: String,
  val description: String,
  val emoji: String,
  val systemInstruction: String
) {
  FREE_TALK(
    id = "free_talk",
    title = "Free Talk",
    description = "Casual open-ended conversation about anything you like.",
    emoji = "💬",
    systemInstruction = "Engage in an open, warm, natural chat. Ask thoughtful follow-up questions about the learner's day, ideas, and experiences. Speak like a close friend."
  ),
  JOB_INTERVIEW(
    id = "job_interview",
    title = "Job Interview",
    description = "Practice professional interviews, elevator pitches, and behavioral questions.",
    emoji = "💼",
    systemInstruction = "You are an encouraging hiring manager. Ask professional behavioral and situational questions, test problem solving, and give feedback on tone and confidence."
  ),
  TRAVEL(
    id = "travel",
    title = "Travel & Transit",
    description = "Navigate airports, book hotels, order taxis, and ask directions.",
    emoji = "✈️",
    systemInstruction = "You are a local transit agent or concierge. Help the traveler navigate travel scenarios, purchase tickets, check into rooms, and ask for local tips."
  ),
  RESTAURANT(
    id = "restaurant",
    title = "Restaurant & Dining",
    description = "Order delicious food, ask about specials, and handle dietary restrictions.",
    emoji = "🍽️",
    systemInstruction = "You are an attentive restaurant server. Present daily specials, take orders, answer food questions, and bring the check."
  ),
  BUSINESS(
    id = "business",
    title = "Business & Meetings",
    description = "Practice negotiations, project updates, and business discussions.",
    emoji = "📈",
    systemInstruction = "You are a corporate colleague or business partner. Discuss quarterly goals, meeting agendas, project timelines, and negotiate professionally."
  ),
  DAILY(
    id = "daily",
    title = "Daily Conversation",
    description = "Talk about hobbies, weekend plans, weather, and routines.",
    emoji = "☕",
    systemInstruction = "Talk casually about everyday life, food, movies, weekend plans, weather, and current routines. Keep it lively and relatable."
  ),
  BEGINNER(
    id = "beginner",
    title = "Beginner Practice",
    description = "Simple vocabulary, slow pacing, and immediate bilingual assistance.",
    emoji = "🌱",
    systemInstruction = "You are an exceptionally gentle language coach. Speak in short, simple sentences with clear pronunciation. If the user hesitates, suggest words and praise their effort."
  );

  companion object {
    fun findById(id: String): CallMode {
      return entries.firstOrNull { it.id == id } ?: FREE_TALK
    }
  }
}

enum class TutorVoice(
  val id: String,
  val displayName: String,
  val gender: String,
  val description: String,
  val pitch: Float = 1.0f,
  val speed: Float = 0.95f
) {
  AOEDE("Aoede", "Elena (Aoede)", "Female", "Warm, encouraging & melodic", 1.05f, 0.95f),
  PUCK("Puck", "Marco (Puck)", "Male", "Energetic, friendly & lively", 1.0f, 1.0f),
  CHARON("Charon", "Julian (Charon)", "Male", "Calm, thoughtful & resonant", 0.9f, 0.9f),
  FENRIR("Fenrir", "David (Fenrir)", "Male", "Authoritative, clear & professional", 0.95f, 0.95f),
  KORE("Kore", "Sofia (Kore)", "Female", "Bright, articulate & enthusiastic", 1.1f, 0.95f);

  companion object {
    fun findById(id: String): TutorVoice {
      return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AOEDE
    }
  }
}

enum class VoiceCallStatus {
  IDLE,
  CONNECTING,
  LISTENING,
  USER_SPEAKING,
  THINKING,
  TUTOR_SPEAKING,
  MUTED,
  INTERRUPTED,
  ENDED
}

data class VoiceCallTurn(
  val id: String,
  val speaker: MessageSender,
  val text: String,
  val phoneticGuide: String? = null,
  val translation: String? = null,
  val timestampEpoch: Long = System.currentTimeMillis()
)

data class CallLearningReport(
  val sessionId: String,
  val languageCode: String,
  val mode: CallMode,
  val durationSeconds: Int,
  val turnsCount: Int,
  val fluencyScore: Int,
  val vocabularyLearned: List<VocabItem> = emptyList(),
  val grammarCorrections: List<GrammarCorrection> = emptyList(),
  val overallFeedback: String = "",
  val xpEarned: Int = 50
)

data class VocabItem(
  val term: String,
  val phonetic: String,
  val translation: String,
  val exampleSentence: String
)

data class GrammarCorrection(
  val originalPhrase: String,
  val correctedPhrase: String,
  val explanation: String
)

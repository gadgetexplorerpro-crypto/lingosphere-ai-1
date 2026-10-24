package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class ChatMessage(
  val id: String,
  val sender: MessageSender,
  val text: String,
  val translation: String? = null,
  val phoneticGuide: String? = null,
  val grammarFeedback: String? = null,
  val timestampEpoch: Long = System.currentTimeMillis()
)

enum class MessageSender {
  USER,
  AI_TUTOR
}

data class RoleplayScenario(
  val id: String,
  val title: String,
  val roleName: String,
  val description: String,
  val iconEmoji: String,
  val systemPromptInstruction: String,
  val openingGreetingTemplate: String
) {
  companion object {
    val SCENARIOS = listOf(
      RoleplayScenario(
        id = "hotel_receptionist",
        title = "Hotel Check-in",
        roleName = "Hotel Receptionist",
        description = "Check into your boutique room, request extra amenities, and ask about local attractions.",
        iconEmoji = "🏨",
        systemPromptInstruction = "You are a warm, courteous hotel receptionist welcoming a guest. Help them check in, ask for their reservation details, and answer questions about breakfast hours and WiFi.",
        openingGreetingTemplate = "Welcome! Good day. How may I assist you with your reservation today?"
      ),
      RoleplayScenario(
        id = "job_interviewer",
        title = "Job Interview",
        roleName = "Hiring Manager",
        description = "Practice introducing your professional background, strengths, and solving workplace questions.",
        iconEmoji = "💼",
        systemPromptInstruction = "You are an encouraging and professional hiring manager conducting a casual first interview. Ask the learner about their experience, problem solving, and career goals.",
        openingGreetingTemplate = "Welcome to our interview. Please tell me a little about yourself and what interests you about this role."
      ),
      RoleplayScenario(
        id = "doctor_consultation",
        title = "Doctor's Office",
        roleName = "Doctor",
        description = "Explain common health symptoms, ask about medication, and understand medical advice.",
        iconEmoji = "🩺",
        systemPromptInstruction = "You are a gentle, attentive doctor listening to a patient's symptoms. Ask when the symptoms started and advise on care and rest.",
        openingGreetingTemplate = "Hello, please have a seat. What brings you to the clinic today?"
      ),
      RoleplayScenario(
        id = "restaurant_waiter",
        title = "Restaurant Dining",
        roleName = "Waiter",
        description = "Order delicious regional dishes, ask about specials, and request the check.",
        iconEmoji = "🍽️",
        systemPromptInstruction = "You are an enthusiastic restaurant server. Recommend the chef's special, take their food and drink orders, and inquire about any dietary restrictions.",
        openingGreetingTemplate = "Good evening! Here are your menus. Would you like to start with some sparkling water or an appetizer?"
      ),
      RoleplayScenario(
        id = "airport_employee",
        title = "Airport & Customs",
        roleName = "Airport Staff",
        description = "Navigate gate departures, baggage drop-off, flight connections, and passport control.",
        iconEmoji = "✈️",
        systemPromptInstruction = "You are an airport gate and check-in agent. Check their boarding pass, ask for passport identification, and direct them to their departure gate.",
        openingGreetingTemplate = "Hello! May I please see your passport and flight booking confirmation?"
      ),
      RoleplayScenario(
        id = "teacher_mentor",
        title = "Language Classroom",
        roleName = "Language Teacher",
        description = "Discuss grammar nuances, clarify homework doubts, and practice cultural idioms.",
        iconEmoji = "📚",
        systemPromptInstruction = "You are an inspiring, friendly language teacher. Gently correct pronunciation and encourage the student to practice speaking in full sentences.",
        openingGreetingTemplate = "Hello! Today we are practicing daily conversations. What is your favorite hobby to do on weekends?"
      ),
      RoleplayScenario(
        id = "customer_service",
        title = "Shopping & Returns",
        roleName = "Store Customer Service",
        description = "Ask for sizes, colors, return policies, or help finding items in a market.",
        iconEmoji = "🛍️",
        systemPromptInstruction = "You are an attentive store associate assisting a customer looking for clothing or gifts. Offer sizes and colors.",
        openingGreetingTemplate = "Hello! Are you looking for anything in particular today? I would be glad to help you find your size."
      ),
      RoleplayScenario(
        id = "friendly_coffee",
        title = "Coffee Chat with a Friend",
        roleName = "Local Friend",
        description = "Catch up on weekend plans, favorite movies, food, travel dreams, and daily life.",
        iconEmoji = "☕",
        systemPromptInstruction = "You are a fun, friendly local peer meeting for a cup of coffee. Talk casually, use natural everyday expressions, and ask about their day.",
        openingGreetingTemplate = "Hey there! It is so good to see you. How has your week been going?"
      )
    )

    fun findById(id: String): RoleplayScenario {
      return SCENARIOS.firstOrNull { it.id == id } ?: SCENARIOS.first()
    }
  }
}

@IgnoreExtraProperties
data class SpeakingSession(
  @DocumentId val id: String = "",
  val userId: String = "",
  val languageCode: String = "",
  val mode: String = "Tutor", // Tutor, Roleplay
  val scenarioTitle: String = "",
  val proficiencyLevel: String = "Beginner",
  val turnsCount: Long = 1L,
  val fluencyScore: Long = 85L,
  val grammarFeedbackSummary: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  val userId: String = "",
  val displayName: String = "",
  val nativeLanguageCode: String = "en",
  val targetLanguageCode: String = "es",
  val activeLanguages: List<String> = listOf("es"),
  val proficiencyLevel: String = "Beginner", // Beginner, Intermediate, Advanced
  val xpPoints: Long = 0L,
  val level: Long = 1L,
  val currentStreak: Long = 1L,
  val longestStreak: Long = 1L,
  val lastActiveDate: String = "",
  val dailyGoalMinutes: Long = 15L,
  val dailyAiSpeakingCount: Long = 0L,
  val dailyAiResetDate: String = "",
  val subscriptionTier: String = "free", // free, premium_monthly, premium_yearly
  val purchaseToken: String? = null,
  val completedLessonIds: List<String> = emptyList(),
  val achievements: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val isPremium: Boolean
    get() = subscriptionTier == "premium_monthly" || subscriptionTier == "premium_yearly"

  val dailyAiLimit: Long
    get() = if (isPremium) 50L else 5L
}

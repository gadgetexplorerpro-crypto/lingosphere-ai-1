package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FlashcardProgress
import com.example.data.model.LessonProgress
import com.example.data.model.SpeakingSession
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LingoRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth
) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // --- User Profile ---

  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val path = "users/$userId"
    val docRef = db.collection("users").document(userId)
    docRef.snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          try {
            val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            profile?.let {
              if (it.userId.isNotBlank()) it else it.copy(userId = snapshot.id)
            }
          } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            UserProfile(
              userId = snapshot.getString("userId") ?: snapshot.id,
              displayName = snapshot.getString("displayName") ?: "Lingo Learner",
              nativeLanguageCode = snapshot.getString("nativeLanguageCode") ?: "en",
              targetLanguageCode = snapshot.getString("targetLanguageCode") ?: "es",
              proficiencyLevel = snapshot.getString("proficiencyLevel") ?: "Beginner",
              xpPoints = snapshot.getLong("xpPoints") ?: 0L,
              level = snapshot.getLong("level") ?: 1L,
              currentStreak = snapshot.getLong("currentStreak") ?: 1L,
              longestStreak = snapshot.getLong("longestStreak") ?: 1L,
              lastActiveDate = snapshot.getString("lastActiveDate") ?: "",
              dailyGoalMinutes = snapshot.getLong("dailyGoalMinutes") ?: 15L,
              subscriptionTier = snapshot.getString("subscriptionTier") ?: "free"
            )
          }
        } else {
          null
        }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
        emit(UserProfile(userId = userId))
      }
      .collect { emit(it) }
  }

  suspend fun getUserProfile(userId: String): UserProfile? {
    val path = "users/$userId"
    return try {
      val snapshot = db.collection("users").document(userId).get().await()
      if (snapshot.exists()) {
        try {
          val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          profile?.let {
            if (it.userId.isNotBlank()) it else it.copy(userId = snapshot.id)
          }
        } catch (e: Exception) {
          handleFirestoreError(e, OperationType.GET, path)
          UserProfile(
            userId = snapshot.getString("userId") ?: snapshot.id,
            displayName = snapshot.getString("displayName") ?: "Lingo Learner",
            nativeLanguageCode = snapshot.getString("nativeLanguageCode") ?: "en",
            targetLanguageCode = snapshot.getString("targetLanguageCode") ?: "es",
            proficiencyLevel = snapshot.getString("proficiencyLevel") ?: "Beginner",
            xpPoints = snapshot.getLong("xpPoints") ?: 0L,
            level = snapshot.getLong("level") ?: 1L,
            currentStreak = snapshot.getLong("currentStreak") ?: 1L,
            longestStreak = snapshot.getLong("longestStreak") ?: 1L,
            lastActiveDate = snapshot.getString("lastActiveDate") ?: "",
            dailyGoalMinutes = snapshot.getLong("dailyGoalMinutes") ?: 15L,
            subscriptionTier = snapshot.getString("subscriptionTier") ?: "free"
          )
        }
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      null
    }
  }

  suspend fun createUserProfile(profile: UserProfile) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
      val payload = hashMapOf<String, Any>(
        "userId" to uid,
        "displayName" to profile.displayName.ifBlank { "Lingo Learner" },
        "nativeLanguageCode" to profile.nativeLanguageCode,
        "targetLanguageCode" to profile.targetLanguageCode,
        "activeLanguages" to profile.activeLanguages,
        "proficiencyLevel" to profile.proficiencyLevel,
        "xpPoints" to profile.xpPoints,
        "level" to profile.level,
        "currentStreak" to profile.currentStreak,
        "longestStreak" to profile.longestStreak,
        "lastActiveDate" to today,
        "dailyGoalMinutes" to profile.dailyGoalMinutes,
        "dailyAiSpeakingCount" to profile.dailyAiSpeakingCount,
        "dailyAiResetDate" to today,
        "subscriptionTier" to profile.subscriptionTier,
        "completedLessonIds" to profile.completedLessonIds,
        "achievements" to profile.achievements,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (profile.purchaseToken != null) {
        payload["purchaseToken"] = profile.purchaseToken
      }
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateUserProfile(profile: UserProfile) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      val payload = hashMapOf<String, Any>(
        "displayName" to profile.displayName,
        "nativeLanguageCode" to profile.nativeLanguageCode,
        "targetLanguageCode" to profile.targetLanguageCode,
        "activeLanguages" to profile.activeLanguages,
        "proficiencyLevel" to profile.proficiencyLevel,
        "xpPoints" to profile.xpPoints,
        "level" to profile.level,
        "currentStreak" to profile.currentStreak,
        "longestStreak" to profile.longestStreak,
        "lastActiveDate" to profile.lastActiveDate,
        "dailyGoalMinutes" to profile.dailyGoalMinutes,
        "dailyAiSpeakingCount" to profile.dailyAiSpeakingCount,
        "dailyAiResetDate" to profile.dailyAiResetDate,
        "subscriptionTier" to profile.subscriptionTier,
        "completedLessonIds" to profile.completedLessonIds,
        "achievements" to profile.achievements,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (profile.purchaseToken != null) {
        payload["purchaseToken"] = profile.purchaseToken
      }
      docRef.update(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun saveUserPrivateEmail(email: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("private").document("info")
    try {
      docRef.set(
        mapOf(
          "userId" to uid,
          "email" to email,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
    }
  }

  suspend fun deleteUserAccountAndData(userId: String) {
    val uid = requireUserId()
    if (uid != userId) throw SecurityException("Cannot delete account of another user")
    try {
      // 1. Delete lesson progress
      val progressSnapshots = db.collection("lesson_progress").whereEqualTo("userId", uid).get().await()
      for (doc in progressSnapshots.documents) {
        doc.reference.delete().await()
      }
      // 2. Delete flashcards
      val flashcardSnapshots = db.collection("flashcards").whereEqualTo("userId", uid).get().await()
      for (doc in flashcardSnapshots.documents) {
        doc.reference.delete().await()
      }
      // 3. Delete speaking sessions
      val sessionSnapshots = db.collection("speaking_sessions").whereEqualTo("userId", uid).get().await()
      for (doc in sessionSnapshots.documents) {
        doc.reference.delete().await()
      }
      // 4. Delete private info
      try {
        db.collection("users").document(uid).collection("private").document("info").delete().await()
      } catch (ignored: Exception) {}
      // 5. Delete user profile
      db.collection("users").document(uid).delete().await()
      // 6. Delete Firebase Auth user
      auth.currentUser?.delete()?.await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "users/$uid")
      throw e
    }
  }

  suspend fun recordLessonCompletion(
    lessonId: String,
    languageCode: String,
    category: String,
    scorePercent: Int,
    xpGained: Int
  ) {
    val uid = requireUserId()
    val progressId = "${uid}_${lessonId}"
    val docRef = db.collection("lesson_progress").document(progressId)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    try {
      docRef.set(
        mapOf(
          "userId" to uid,
          "lessonId" to lessonId,
          "languageCode" to languageCode,
          "category" to category,
          "scorePercent" to scorePercent.toLong(),
          "xpEarned" to xpGained.toLong(),
          "completed" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()

      // Update User XP & Streaks
      val user = getUserProfile(uid)
      if (user != null) {
        val newXp = user.xpPoints + xpGained
        val newLevel = (newXp / 100) + 1
        val completedList = (user.completedLessonIds + lessonId).distinct()
        val isNewDay = user.lastActiveDate != today
        val newStreak = if (isNewDay) user.currentStreak + 1 else user.currentStreak
        val longestStreak = maxOf(user.longestStreak, newStreak)

        val achievements = user.achievements.toMutableList()
        if (completedList.size >= 1 && !achievements.contains("first_lesson")) achievements.add("first_lesson")
        if (completedList.size >= 5 && !achievements.contains("scholar_5")) achievements.add("scholar_5")
        if (newStreak >= 3 && !achievements.contains("streak_3")) achievements.add("streak_3")

        updateUserProfile(
          user.copy(
            xpPoints = newXp,
            level = newLevel,
            currentStreak = newStreak,
            longestStreak = longestStreak,
            lastActiveDate = today,
            completedLessonIds = completedList,
            achievements = achievements
          )
        )
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun incrementDailyAiSpeaking(userId: String) {
    val user = getUserProfile(userId) ?: return
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val isReset = user.dailyAiResetDate != today
    val newCount = if (isReset) 1L else user.dailyAiSpeakingCount + 1L

    val achievements = user.achievements.toMutableList()
    if (!achievements.contains("first_speak")) achievements.add("first_speak")

    updateUserProfile(
      user.copy(
        dailyAiSpeakingCount = newCount,
        dailyAiResetDate = today,
        achievements = achievements
      )
    )
  }

  // --- Lesson Progress Observation ---

  fun observeLessonProgress(userId: String): Flow<List<LessonProgress>> = flow {
    val path = "lesson_progress"
    db.collection(path)
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(LessonProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        throw error
      }
      .collect { emit(it) }
  }

  // --- Spaced Repetition Flashcards ---

  fun observeFlashcards(userId: String): Flow<List<FlashcardProgress>> = flow {
    val path = "flashcards"
    db.collection(path)
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(FlashcardProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        throw error
      }
      .collect { emit(it) }
  }

  suspend fun saveFlashcard(card: FlashcardProgress) {
    val uid = requireUserId()
    val cardId = if (card.id.isNotBlank()) card.id else "card_${UUID.randomUUID()}"
    val docRef = db.collection("flashcards").document(cardId)
    try {
      docRef.set(
        mapOf(
          "userId" to uid,
          "wordId" to card.wordId,
          "languageCode" to card.languageCode,
          "frontText" to card.frontText,
          "backText" to card.backText,
          "pronunciation" to card.pronunciation,
          "boxLevel" to card.boxLevel,
          "nextReviewEpochDay" to card.nextReviewEpochDay,
          "mastered" to card.mastered,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun updateFlashcardBox(card: FlashcardProgress, isCorrect: Boolean) {
    val uid = requireUserId()
    val docRef = db.collection("flashcards").document(card.id)
    val currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    val newBox = if (isCorrect) minOf(5L, card.boxLevel + 1) else maxOf(1L, card.boxLevel - 1)
    val intervalDays = when (newBox) {
      1L -> 1L
      2L -> 3L
      3L -> 7L
      4L -> 14L
      else -> 30L
    }
    val nextReview = currentDay + intervalDays
    val mastered = newBox >= 5L

    try {
      docRef.update(
        mapOf(
          "boxLevel" to newBox,
          "nextReviewEpochDay" to nextReview,
          "mastered" to mastered,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  // --- Speaking Sessions ---

  fun observeSpeakingSessions(userId: String): Flow<List<SpeakingSession>> = flow {
    val path = "speaking_sessions"
    db.collection(path)
      .whereEqualTo("userId", userId)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(SpeakingSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        throw error
      }
      .collect { emit(it) }
  }

  suspend fun saveSpeakingSession(session: SpeakingSession) {
    val uid = requireUserId()
    val sessionId = if (session.id.isNotBlank()) session.id else "sess_${UUID.randomUUID()}"
    val docRef = db.collection("speaking_sessions").document(sessionId)
    try {
      docRef.set(
        mapOf(
          "userId" to uid,
          "languageCode" to session.languageCode,
          "mode" to session.mode,
          "scenarioTitle" to session.scenarioTitle,
          "proficiencyLevel" to session.proficiencyLevel,
          "turnsCount" to session.turnsCount,
          "fluencyScore" to session.fluencyScore,
          "grammarFeedbackSummary" to session.grammarFeedbackSummary,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      incrementDailyAiSpeaking(uid)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }
}

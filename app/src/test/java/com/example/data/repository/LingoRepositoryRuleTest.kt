package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FlashcardProgress
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class LingoRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testCreateAndGetProfile_authenticated_succeeds() = runBlocking {
    val uid = signInTestUser("user_profile_${UUID.randomUUID()}@test.com")
    val repo = LingoRepository(firestore, auth)

    val profile = UserProfile(
      userId = uid,
      displayName = "Alice Test",
      nativeLanguageCode = "en",
      targetLanguageCode = "es",
      activeLanguages = listOf("es")
    )

    withTimeout(TIMEOUT_MS) {
      repo.createUserProfile(profile)
      val retrieved = repo.getUserProfile(uid)
      assertNotNull(retrieved)
      assertEquals("Alice Test", retrieved?.displayName)
      assertEquals("es", retrieved?.targetLanguageCode)
    }
  }

  @Test
  fun testRecordLessonCompletion_authenticated_emitsProgress() = runBlocking {
    val uid = signInTestUser("user_lesson_${UUID.randomUUID()}@test.com")
    val repo = LingoRepository(firestore, auth)

    val profile = UserProfile(userId = uid, displayName = "Alice Test")
    repo.createUserProfile(profile)

    val lessonId = "test_lesson_${UUID.randomUUID()}"
    withTimeout(TIMEOUT_MS) {
      repo.recordLessonCompletion(lessonId, "es", "Vocabulary", 100, 30)

      val progressList = repo.observeLessonProgress(uid).first { list ->
        list.any { it.lessonId == lessonId }
      }
      assertTrue(progressList.any { it.lessonId == lessonId && it.completed })
    }
  }

  @Test
  fun testSaveAndObserveFlashcards_authenticated_succeeds() = runBlocking {
    val uid = signInTestUser("user_cards_${UUID.randomUUID()}@test.com")
    val repo = LingoRepository(firestore, auth)

    val wordId = "word_${UUID.randomUUID()}"
    val card = FlashcardProgress(
      id = "card_${UUID.randomUUID()}",
      userId = uid,
      wordId = wordId,
      languageCode = "es",
      frontText = "Hola",
      backText = "Hello",
      pronunciation = "oh-lah",
      boxLevel = 1L
    )

    withTimeout(TIMEOUT_MS) {
      repo.saveFlashcard(card)

      val cards = repo.observeFlashcards(uid).first { list ->
        list.any { it.wordId == wordId }
      }
      assertTrue(cards.any { it.wordId == wordId && it.frontText == "Hola" })
    }
  }

  @Test
  fun testUnauthenticated_observeProgress_rejected() = runBlocking {
    auth.signOut()
    val repo = LingoRepository(firestore)

    try {
      withTimeout(TIMEOUT_MS) {
        repo.observeLessonProgress("some_unauth_user").first()
      }
      fail("Expected exception for unauthenticated query")
    } catch (e: Exception) {
      val fsError = e.unwrapFirestoreException()
      assertTrue(
        "Expected permission denied or cancelled stream, got $e",
        fsError?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED || e.message?.contains("PERMISSION_DENIED") == true || e is kotlinx.coroutines.CancellationException
      )
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_learner@test.com"
    const val TIMEOUT_MS = 5000L
  }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CurriculumLesson
import com.example.data.model.FlashcardItem
import com.example.data.model.FlashcardProgress
import com.example.data.model.Language
import com.example.data.model.LessonProgress
import com.example.data.model.SpeakingSession
import com.example.data.model.UserProfile
import com.example.data.repository.CurriculumData
import com.example.data.repository.LingoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LingoViewModel(
  private val repository: LingoRepository,
  private val userId: String
) : ViewModel() {

  val userProfileState: StateFlow<UserProfile> = repository.observeUserProfile(userId)
    .catch { emit(UserProfile(userId = userId)) }
    .map { it ?: UserProfile(userId = userId) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = UserProfile(userId = userId)
    )

  val lessonProgressState: StateFlow<List<LessonProgress>> = repository.observeLessonProgress(userId)
    .catch { emit(emptyList()) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList()
    )

  val flashcardProgressState: StateFlow<List<FlashcardProgress>> = repository.observeFlashcards(userId)
    .catch { emit(emptyList()) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000L),
      initialValue = emptyList()
    )

  private val _activeLesson = MutableStateFlow<CurriculumLesson?>(null)
  val activeLesson: StateFlow<CurriculumLesson?> = _activeLesson.asStateFlow()

  init {
    viewModelScope.launch {
      // Seed default flashcards if none exist
      val existing = repository.observeFlashcards(userId)
      // Check profile once
      val profile = repository.getUserProfile(userId)
      if (profile == null) {
        val initialProfile = UserProfile(
          userId = userId,
          displayName = "Language Learner",
          nativeLanguageCode = "en",
          targetLanguageCode = "es",
          activeLanguages = listOf("es")
        )
        repository.createUserProfile(initialProfile)
        seedStarterFlashcards("es")
      }
    }
  }

  fun seedStarterFlashcards(targetLangCode: String) {
    viewModelScope.launch {
      val starterList = CurriculumData.getStarterFlashcards(targetLangCode)
      for (item in starterList) {
        repository.saveFlashcard(
          FlashcardProgress(
            userId = userId,
            wordId = item.wordId,
            languageCode = targetLangCode,
            frontText = item.frontText,
            backText = item.backText,
            pronunciation = item.pronunciation,
            boxLevel = item.boxLevel.toLong()
          )
        )
      }
    }
  }

  fun startLesson(lesson: CurriculumLesson) {
    _activeLesson.value = lesson
  }

  fun dismissLesson() {
    _activeLesson.value = null
  }

  fun finishActiveLesson(scorePercent: Int, xpEarned: Int) {
    val lesson = _activeLesson.value ?: return
    viewModelScope.launch {
      repository.recordLessonCompletion(
        lessonId = lesson.id,
        languageCode = lesson.targetLanguageCode,
        category = lesson.category,
        scorePercent = scorePercent,
        xpGained = xpEarned
      )
      _activeLesson.value = null
    }
  }

  fun changeTargetLanguage(code: String) {
    viewModelScope.launch {
      val profile = userProfileState.value
      val newActive = (profile.activeLanguages + code).distinct()
      repository.updateUserProfile(
        profile.copy(
          targetLanguageCode = code,
          activeLanguages = newActive
        )
      )
      seedStarterFlashcards(code)
    }
  }

  fun changeNativeLanguage(code: String) {
    viewModelScope.launch {
      val profile = userProfileState.value
      repository.updateUserProfile(
        profile.copy(nativeLanguageCode = code)
      )
    }
  }

  fun reviewFlashcard(card: FlashcardProgress, isCorrect: Boolean) {
    viewModelScope.launch {
      repository.updateFlashcardBox(card, isCorrect)
    }
  }

  fun saveSpeakingSession(session: SpeakingSession) {
    viewModelScope.launch {
      repository.saveSpeakingSession(session)
    }
  }

  fun updateSubscription(tier: String, token: String) {
    viewModelScope.launch {
      val profile = userProfileState.value
      repository.updateUserProfile(
        profile.copy(
          subscriptionTier = tier,
          purchaseToken = token
        )
      )
    }
  }

  fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      try {
        repository.deleteUserAccountAndData(userId)
        onSuccess()
      } catch (e: Exception) {
        onError(e.localizedMessage ?: "Failed to delete account data")
      }
    }
  }
}

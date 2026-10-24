package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ExerciseType {
  VOCABULARY,
  GRAMMAR,
  LISTENING,
  READING,
  WRITING,
  QUIZ
}

data class Exercise(
  val id: String,
  val type: ExerciseType,
  val prompt: String,
  val explanationNative: String,
  val targetAudioText: String = "",
  val phoneticGuide: String = "",
  val options: List<String> = emptyList(),
  val correctOptionIndex: Int = 0,
  val acceptableAnswers: List<String> = emptyList()
)

data class CurriculumLesson(
  val id: String,
  val title: String,
  val description: String,
  val category: String, // Vocabulary, Grammar, Listening, Reading, Writing, Quiz
  val proficiencyLevel: String, // Beginner, Intermediate, Advanced
  val targetLanguageCode: String,
  val estimatedMinutes: Int = 5,
  val xpReward: Int = 30,
  val exercises: List<Exercise> = emptyList()
)

@IgnoreExtraProperties
data class LessonProgress(
  @DocumentId val id: String = "",
  val userId: String = "",
  val lessonId: String = "",
  val languageCode: String = "",
  val category: String = "Vocabulary",
  val scorePercent: Long = 0L,
  val xpEarned: Long = 0L,
  val completed: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

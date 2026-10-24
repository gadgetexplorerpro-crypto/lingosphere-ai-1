package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class FlashcardItem(
  val wordId: String,
  val languageCode: String,
  val frontText: String,
  val backText: String,
  val pronunciation: String = "",
  val exampleSentence: String = "",
  val boxLevel: Int = 1
)

@IgnoreExtraProperties
data class FlashcardProgress(
  @DocumentId val id: String = "",
  val userId: String = "",
  val wordId: String = "",
  val languageCode: String = "",
  val frontText: String = "",
  val backText: String = "",
  val pronunciation: String = "",
  val boxLevel: Long = 1L, // 1 to 5
  val nextReviewEpochDay: Long = 0L,
  val mastered: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

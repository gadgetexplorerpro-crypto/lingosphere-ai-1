package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurriculumLesson
import com.example.data.model.ExerciseType
import com.example.speech.SpeechManager
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoGold

@Composable
fun LessonExerciseScreen(
  lesson: CurriculumLesson,
  speechManager: SpeechManager,
  onFinishLesson: (scorePercent: Int, xpEarned: Int) -> Unit,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var currentExerciseIndex by remember { mutableIntStateOf(0) }
  var correctCount by remember { mutableIntStateOf(0) }
  var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
  var writtenAnswer by remember { mutableStateOf("") }
  var isAnswerSubmitted by remember { mutableStateOf(false) }
  var isCorrectAnswer by remember { mutableStateOf(false) }
  var isLessonFinished by remember { mutableStateOf(false) }

  val totalExercises = lesson.exercises.size
  val currentExercise = lesson.exercises.getOrNull(currentExerciseIndex)

  // Auto-play speech for listening exercises
  LaunchedEffect(currentExerciseIndex) {
    val ex = lesson.exercises.getOrNull(currentExerciseIndex)
    if (ex != null && ex.type == ExerciseType.LISTENING && ex.targetAudioText.isNotBlank()) {
      speechManager.speak(ex.targetAudioText, lesson.targetLanguageCode)
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      // Header Bar
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
          progress = {
            if (totalExercises > 0) (currentExerciseIndex + 1).toFloat() / totalExercises else 0f
          },
          modifier = Modifier
            .weight(1f)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = LingoElectricBlue
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "${currentExerciseIndex + 1}/$totalExercises",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (isLessonFinished || currentExercise == null) {
        // Completion Card
        val finalScore = if (totalExercises > 0) (correctCount * 100) / totalExercises else 100
        val earnedXp = if (finalScore >= 60) lesson.xpReward else (lesson.xpReward / 2)

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(110.dp)
              .clip(CircleShape)
              .background(LingoGold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.EmojiEvents,
              contentDescription = "Celebration",
              tint = LingoGold,
              modifier = Modifier.size(64.dp)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = "Lesson Complete!",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          Text(
            text = "You scored $finalScore% on ${lesson.title}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(0.8f)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(text = "⭐ +$earnedXp XP Earned!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LingoElectricBlue)
            }
          }

          Spacer(modifier = Modifier.height(32.dp))

          Button(
            onClick = {
              onFinishLesson(finalScore, earnedXp)
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LingoElectricBlue),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
          ) {
            Text("Claim XP & Return to Home", fontWeight = FontWeight.Bold)
          }
        }
      } else {
        // Exercise Active Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
        ) {
          Text(
            text = currentExercise.type.name.replace("_", " "),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LingoElectricBlue
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = currentExercise.prompt,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          // Audio pronounce button if target text available
          if (currentExercise.targetAudioText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(LingoElectricBlue.copy(alpha = 0.15f))
                .clickable {
                  speechManager.speak(currentExercise.targetAudioText, lesson.targetLanguageCode)
                }
                .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
              Icon(Icons.Default.VolumeUp, contentDescription = "Play Pronunciation", tint = LingoElectricBlue)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = currentExercise.targetAudioText,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = LingoElectricBlue
                )
                if (currentExercise.phoneticGuide.isNotBlank()) {
                  Text(
                    text = currentExercise.phoneticGuide,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Exercise Type Handling
          when (currentExercise.type) {
            ExerciseType.VOCABULARY, ExerciseType.GRAMMAR, ExerciseType.LISTENING, ExerciseType.READING, ExerciseType.QUIZ -> {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentExercise.options.forEachIndexed { index, option ->
                  val isSelected = selectedOptionIndex == index
                  val isCorrect = index == currentExercise.correctOptionIndex

                  val cardBg = when {
                    isAnswerSubmitted && isCorrect -> LingoEmerald.copy(alpha = 0.2f)
                    isAnswerSubmitted && isSelected && !isCorrect -> LingoCoral.copy(alpha = 0.2f)
                    isSelected -> LingoElectricBlue.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }

                  val borderColor = when {
                    isAnswerSubmitted && isCorrect -> LingoEmerald
                    isAnswerSubmitted && isSelected && !isCorrect -> LingoCoral
                    isSelected -> LingoElectricBlue
                    else -> Color.Transparent
                  }

                  Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable(enabled = !isAnswerSubmitted) {
                        selectedOptionIndex = index
                      }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(16.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .background(
                            if (isSelected) LingoElectricBlue else MaterialTheme.colorScheme.surface
                          ),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = ('A' + index).toString(),
                          fontWeight = FontWeight.Bold,
                          color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                      }
                      Spacer(modifier = Modifier.width(14.dp))
                      Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                      )
                      if (isAnswerSubmitted) {
                        if (isCorrect) {
                          Icon(Icons.Default.Check, contentDescription = "Correct", tint = LingoEmerald)
                        } else if (isSelected) {
                          Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = LingoCoral)
                        }
                      }
                    }
                  }
                }
              }
            }
            ExerciseType.WRITING -> {
              Column {
                OutlinedTextField(
                  value = writtenAnswer,
                  onValueChange = { writtenAnswer = it },
                  placeholder = { Text("Type your answer in ${lesson.targetLanguageCode.uppercase()}...") },
                  shape = RoundedCornerShape(14.dp),
                  enabled = !isAnswerSubmitted,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Answer feedback card
          AnimatedVisibility(visible = isAnswerSubmitted) {
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isCorrectAnswer) LingoEmerald.copy(alpha = 0.15f) else LingoCoral.copy(alpha = 0.15f)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = if (isCorrectAnswer) "🎉 Excellent! That is correct." else "💡 Not quite.",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isCorrectAnswer) LingoEmerald else LingoCoral
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = currentExercise.explanationNative,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        // Action Button: Check Answer or Next Exercise
        if (!isAnswerSubmitted) {
          val canSubmit = when (currentExercise.type) {
            ExerciseType.WRITING -> writtenAnswer.isNotBlank()
            else -> selectedOptionIndex != null
          }

          Button(
            onClick = {
              isAnswerSubmitted = true
              when (currentExercise.type) {
                ExerciseType.WRITING -> {
                  val trimmed = writtenAnswer.trim().lowercase()
                  isCorrectAnswer = currentExercise.acceptableAnswers.any { it.lowercase() == trimmed }
                }
                else -> {
                  isCorrectAnswer = selectedOptionIndex == currentExercise.correctOptionIndex
                }
              }
              if (isCorrectAnswer) correctCount++
            },
            enabled = canSubmit,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LingoElectricBlue),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
          ) {
            Text("Check Answer", fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = {
              if (currentExerciseIndex + 1 < totalExercises) {
                currentExerciseIndex++
                selectedOptionIndex = null
                writtenAnswer = ""
                isAnswerSubmitted = false
                isCorrectAnswer = false
              } else {
                isLessonFinished = true
              }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCorrectAnswer) LingoEmerald else LingoElectricBlue
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
          ) {
            Text(
              if (currentExerciseIndex + 1 < totalExercises) "Continue" else "Finish Lesson",
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurriculumLesson
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald

@Composable
fun LearnScreen(
  userProfile: UserProfile,
  lessons: List<CurriculumLesson>,
  onStartLesson: (CurriculumLesson) -> Unit
) {
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)
  val categories = listOf("All", "Vocabulary", "Grammar", "Listening", "Reading", "Writing", "Quiz")
  var selectedCategory by remember { mutableStateOf("All") }

  val filteredLessons = remember(selectedCategory, lessons) {
    if (selectedCategory == "All") lessons
    else lessons.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = targetLang.flagEmoji,
          fontSize = 32.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "${targetLang.name} Curriculum",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Personalized learning path • ${userProfile.completedLessonIds.size} completed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { category ->
          val isSelected = selectedCategory == category
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = category },
            label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = LingoElectricBlue,
              selectedLabelColor = Color.White
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredLessons) { lesson ->
          val isCompleted = userProfile.completedLessonIds.contains(lesson.id)
          Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onStartLesson(lesson) }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(18.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(
                    if (isCompleted) LingoEmerald.copy(alpha = 0.2f) else LingoElectricBlue.copy(alpha = 0.15f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isCompleted) {
                  Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = LingoEmerald,
                    modifier = Modifier.size(28.dp)
                  )
                } else {
                  Text(
                    text = when (lesson.category) {
                      "Vocabulary" -> "📖"
                      "Grammar" -> "📐"
                      "Listening" -> "🎧"
                      "Reading" -> "📰"
                      "Writing" -> "✍️"
                      else -> "🎯"
                    },
                    fontSize = 24.sp
                  )
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = lesson.category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LingoElectricBlue
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "• ${lesson.proficiencyLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = lesson.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                  text = lesson.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Star,
                    contentDescription = "XP",
                    tint = LingoElectricBlue,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "+${lesson.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LingoElectricBlue
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "${lesson.estimatedMinutes} mins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(if (isCompleted) LingoEmerald else LingoElectricBlue),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                  contentDescription = "Start",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(30.dp))
        }
      }
    }
  }
}

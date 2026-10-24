package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurriculumLesson
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoNavyCard
import com.example.ui.theme.LingoNavyDeep
import com.example.ui.theme.LingoViolet
import com.example.ui.theme.LingoVioletLight

@Composable
fun HomeScreen(
  userProfile: UserProfile,
  nextLesson: CurriculumLesson?,
  onNavigateToLearn: () -> Unit,
  onNavigateToSpeak: () -> Unit,
  onNavigateToPractice: () -> Unit,
  onStartLesson: (CurriculumLesson) -> Unit,
  onTargetLanguageChanged: (String) -> Unit,
  onStartVoiceCall: () -> Unit = onNavigateToSpeak
) {
  var showLanguagePicker by remember { mutableStateOf(false) }
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      // Top Navigation / Header Bar
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Language Switcher Pill
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showLanguagePicker = true }
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Text(text = targetLang.flagEmoji, fontSize = 20.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = targetLang.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Switch Language")
        }

        // Gamification Counters (Streaks, XP)
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Streak Counter
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(LingoAmber.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              Icons.Default.LocalFireDepartment,
              contentDescription = "Streak",
              tint = LingoAmber,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${userProfile.currentStreak}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = LingoAmber
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          // XP Points
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(LingoCyan.copy(alpha = 0.15f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              Icons.Default.Star,
              contentDescription = "XP Points",
              tint = LingoCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${userProfile.xpPoints} XP",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = LingoElectricBlue
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Greeting & Level Banner with Logo
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Hello, ${userProfile.displayName.ifBlank { "Learner" }}!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Level ${userProfile.level} ${userProfile.proficiencyLevel} • Ready to speak ${targetLang.name}?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Image(
          painter = painterResource(id = R.drawable.lingosphere_logo),
          contentDescription = "LingoSphere Logo",
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Hero Card: Real-Time AI Voice Call
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF31104B))
              )
            )
            .border(1.dp, LingoViolet.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(22.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LingoEmerald.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.Call,
                    contentDescription = "Call AI Tutor",
                    tint = LingoEmerald,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Real-Time AI Voice Call",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "5 min daily free allowance • 2-way live audio",
                    style = MaterialTheme.typography.bodySmall,
                    color = LingoCyan
                  )
                }
              }

              // Glowing live badge
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(LingoEmerald.copy(alpha = 0.2f))
                  .border(1.dp, LingoEmerald, CircleShape)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "● LIVE",
                  color = LingoEmerald,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Have continuous, natural voice conversations with your personal native language tutor in ${targetLang.name}. Hands-free speech, natural interruptions, and instant feedback.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.9f),
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Prominent "Call AI Tutor" Action Button
            Button(
              onClick = onStartVoiceCall,
              colors = ButtonDefaults.buttonColors(
                containerColor = LingoEmerald,
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Call AI Tutor Now", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary option: Open Chat Mode
            Button(
              onClick = onNavigateToSpeak,
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Or Practice with Text & Voice Chat", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Recommended Lesson Card
      Text(
        text = "Next Up in Learning Path",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(10.dp))

      if (nextLesson != null) {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartLesson(nextLesson) }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
          ) {
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LingoCyan.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = when (nextLesson.category) {
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

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = nextLesson.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${nextLesson.estimatedMinutes} mins • +${nextLesson.xpReward} XP",
                style = MaterialTheme.typography.bodySmall,
                color = LingoElectricBlue
              )
            }

            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LingoElectricBlue),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Start Lesson", tint = Color.White)
            }
          }
        }
      } else {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Explore ${targetLang.name} Curriculum",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Browse vocabulary, listening, reading, writing, and quizzes.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onNavigateToLearn,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Go to Curriculum")
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Quick Practice Grid
      Text(
        text = "Quick Practice",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        QuickActionCard(
          icon = "🎴",
          title = "Flashcards",
          subtitle = "Spaced Repetition",
          onClick = onNavigateToPractice,
          modifier = Modifier.weight(1f)
        )
        QuickActionCard(
          icon = "🎭",
          title = "Roleplay",
          subtitle = "8 Real Scenarios",
          onClick = onNavigateToSpeak,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }

  if (showLanguagePicker) {
    LanguagePickerDialog(
      title = "Change Active Language",
      currentLanguageCode = userProfile.targetLanguageCode,
      onLanguageSelected = { selected ->
        onTargetLanguageChanged(selected.code)
      },
      onDismiss = { showLanguagePicker = false }
    )
  }
}

@Composable
private fun QuickActionCard(
  icon: String,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(text = icon, fontSize = 28.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlashcardProgress
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.speech.SpeechManager
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCoral
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoViolet

@Composable
fun PracticeScreen(
  userProfile: UserProfile,
  flashcards: List<FlashcardProgress>,
  speechManager: SpeechManager,
  onReviewCard: (card: FlashcardProgress, correct: Boolean) -> Unit
) {
  val targetLang = Language.findByCode(userProfile.targetLanguageCode)
  var currentCardIndex by remember { mutableIntStateOf(0) }
  var isFlipped by remember { mutableStateOf(false) }

  val activeCard = flashcards.getOrNull(currentCardIndex)

  val rotation by animateFloatAsState(
    targetValue = if (isFlipped) 180f else 0f,
    animationSpec = tween(durationMillis = 400),
    label = "flip"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      Text(
        text = "Spaced Repetition Mastery",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Leitner 5-box system • ${flashcards.size} active vocabulary cards",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Leitner Box Status Indicator
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Mastery Distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            for (box in 1..5) {
              val count = flashcards.count { it.boxLevel == box.toLong() }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                      if (box == 5) LingoEmerald else LingoElectricBlue.copy(alpha = 0.2f * box)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$count",
                    fontWeight = FontWeight.Bold,
                    color = if (box >= 4) Color.White else MaterialTheme.colorScheme.onSurface
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Box $box", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (activeCard != null) {
        // Active Flip Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
              rotationY = rotation
              cameraDistance = 12 * density
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable { isFlipped = !isFlipped }
            .background(
              if (rotation <= 90f) {
                Brush.linearGradient(listOf(LingoElectricBlue, LingoViolet))
              } else {
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface))
              }
            )
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          if (rotation <= 90f) {
            // Front Side (Target Language)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Box ${activeCard.boxLevel} • Tap to flip",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
              )
              Spacer(modifier = Modifier.height(20.dp))
              Text(
                text = activeCard.frontText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
              )
              if (activeCard.pronunciation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "🔊 ${activeCard.pronunciation}",
                  style = MaterialTheme.typography.titleMedium,
                  color = LingoCyan
                )
              }
              Spacer(modifier = Modifier.height(16.dp))
              IconButton(
                onClick = { speechManager.speak(activeCard.frontText, targetLang.code) },
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.25f))
              ) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Play Pronunciation", tint = Color.White)
              }
            }
          } else {
            // Back Side (Native Translation)
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
              Text(
                text = "Meaning in ${Language.findByCode(userProfile.nativeLanguageCode).name}",
                style = MaterialTheme.typography.labelSmall,
                color = LingoElectricBlue
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = activeCard.backText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Tap card to flip back",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Evaluation Action Buttons
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Button(
            onClick = {
              onReviewCard(activeCard, false)
              isFlipped = false
              if (currentCardIndex + 1 < flashcards.size) currentCardIndex++ else currentCardIndex = 0
            },
            colors = ButtonDefaults.buttonColors(containerColor = LingoCoral.copy(alpha = 0.15f), contentColor = LingoCoral),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Review Again", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              onReviewCard(activeCard, true)
              isFlipped = false
              if (currentCardIndex + 1 < flashcards.size) currentCardIndex++ else currentCardIndex = 0
            },
            colors = ButtonDefaults.buttonColors(containerColor = LingoEmerald, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("I Know This", fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
          ) {
            Text(text = "🎉", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "All Cards Reviewed Today!",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Keep completing new lessons to expand your vocabulary deck with more terms.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

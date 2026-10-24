package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoiceCallStatus
import com.example.ui.theme.LingoAmber
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoEmerald
import com.example.ui.theme.LingoViolet
import com.example.ui.theme.LingoVioletLight
import kotlin.math.sin

@Composable
fun AiCallAvatar(
  status: VoiceCallStatus,
  rmsLevel: Float,
  emoji: String,
  modifier: Modifier = Modifier,
  size: Dp = 150.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
  
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  val ringColor = when (status) {
    VoiceCallStatus.TUTOR_SPEAKING -> LingoViolet
    VoiceCallStatus.USER_SPEAKING -> LingoEmerald
    VoiceCallStatus.LISTENING -> LingoCyan
    VoiceCallStatus.THINKING -> LingoElectricBlue
    VoiceCallStatus.MUTED -> LingoAmber
    else -> Color(0xFF64748B)
  }

  Box(
    modifier = modifier.size(size * 1.5f),
    contentAlignment = Alignment.Center
  ) {
    // Dynamic multi-layer glowing ripple rings
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.toPx() * 0.75f, size.toPx() * 0.75f)
      val baseRadius = (size.toPx() / 2f)
      val dynamicMultiplier = if (status == VoiceCallStatus.TUTOR_SPEAKING || status == VoiceCallStatus.USER_SPEAKING) {
        1f + (rmsLevel * 0.4f) + (pulseScale - 1f) * 0.5f
      } else {
        1f + (pulseScale - 1f) * 0.2f
      }

      // Outer ripple ring
      drawCircle(
        color = ringColor.copy(alpha = 0.15f),
        radius = baseRadius * dynamicMultiplier * 1.35f,
        center = center
      )

      // Middle ripple ring
      drawCircle(
        color = ringColor.copy(alpha = 0.35f),
        radius = baseRadius * dynamicMultiplier * 1.18f,
        center = center,
        style = Stroke(width = 3.dp.toPx())
      )

      // Inner glowing border
      drawCircle(
        color = ringColor.copy(alpha = 0.7f),
        radius = baseRadius * 1.05f,
        center = center,
        style = Stroke(width = 4.dp.toPx())
      )
    }

    // Core avatar circle
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color(0xFF1E1B4B),
              Color(0xFF0F172A),
              Color(0xFF31104B)
            )
          )
        )
        .border(
          width = 3.dp,
          brush = Brush.sweepGradient(
            listOf(
              ringColor,
              ringColor.copy(alpha = 0.3f),
              ringColor
            )
          ),
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = emoji,
        fontSize = 54.sp
      )
    }
  }
}

@Composable
fun LiveAudioWaveformBar(
  status: VoiceCallStatus,
  rmsLevel: Float,
  modifier: Modifier = Modifier,
  barCount: Int = 16,
  maxHeight: Dp = 48.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (Math.PI * 2).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  val barColor = when (status) {
    VoiceCallStatus.TUTOR_SPEAKING -> LingoVioletLight
    VoiceCallStatus.USER_SPEAKING -> LingoEmerald
    VoiceCallStatus.LISTENING -> LingoCyan
    VoiceCallStatus.THINKING -> LingoElectricBlue
    VoiceCallStatus.MUTED -> LingoAmber
    else -> Color.Gray
  }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val normalizedIndex = i.toFloat() / barCount.toFloat()
      val sineComponent = ((sin(phase + normalizedIndex * 4f) + 1f) / 2f)

      val heightRatio = when (status) {
        VoiceCallStatus.TUTOR_SPEAKING -> {
          (0.25f + sineComponent * 0.75f).coerceIn(0.15f, 1f)
        }
        VoiceCallStatus.USER_SPEAKING -> {
          val audioIntensity = (rmsLevel * 1.5f).coerceIn(0.2f, 1f)
          (0.2f + sineComponent * audioIntensity).coerceIn(0.15f, 1f)
        }
        VoiceCallStatus.LISTENING -> {
          (0.15f + sineComponent * 0.25f)
        }
        VoiceCallStatus.THINKING -> {
          (0.2f + (sin(phase * 2f + i) + 1f) * 0.2f)
        }
        else -> 0.1f
      }

      Box(
        modifier = Modifier
          .width(4.dp)
          .height(maxHeight * heightRatio)
          .clip(CircleShape)
          .background(barColor)
      )
    }
  }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LingoCyan
import com.example.ui.theme.LingoElectricBlue
import com.example.ui.theme.LingoVioletLight

@Composable
fun AnimatedVoiceWaveform(
  isListening: Boolean,
  rmsLevel: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
  val animPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "phase"
  )

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
  ) {
    val barCount = 21
    val spacing = size.width / (barCount * 2)
    val barWidth = spacing * 0.9f
    val centerY = size.height / 2f

    val gradient = Brush.linearGradient(
      colors = listOf(LingoCyan, LingoElectricBlue, LingoVioletLight),
      start = Offset(0f, 0f),
      end = Offset(size.width, size.height)
    )

    for (i in 0 until barCount) {
      val x = spacing + i * (barWidth + spacing)
      val normalizedIndex = (i - barCount / 2f) / (barCount / 2f)
      val bellFactor = 1f - (normalizedIndex * normalizedIndex * 0.7f).coerceIn(0f, 1f)

      val dynamicHeight = if (isListening) {
        val waveMod = Math.sin((i * 0.5 + animPhase * 6.28)).toFloat() * 0.25f + 0.75f
        val boost = rmsLevel.coerceIn(0.1f, 1.0f) * 1.5f
        (size.height * 0.8f * bellFactor * waveMod * boost).coerceIn(8.dp.toPx(), size.height * 0.95f)
      } else {
        (12.dp.toPx() * bellFactor).coerceAtLeast(4.dp.toPx())
      }

      val top = centerY - dynamicHeight / 2f
      drawRoundRect(
        brush = gradient,
        topLeft = Offset(x, top),
        size = Size(barWidth, dynamicHeight),
        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
      )
    }
  }
}

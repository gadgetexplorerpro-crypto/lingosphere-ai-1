package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = LingoElectricBlueLight,
  onPrimary = Color.White,
  primaryContainer = LingoNavyCard,
  onPrimaryContainer = LingoCyan,
  secondary = LingoVioletLight,
  onSecondary = Color.White,
  secondaryContainer = LingoNavyStroke,
  onSecondaryContainer = LingoLilac,
  tertiary = LingoCyan,
  onTertiary = LingoNavyDeep,
  background = LingoNavyDeep,
  onBackground = LingoTextPrimaryLight,
  surface = LingoNavySurface,
  onSurface = LingoTextPrimaryLight,
  surfaceVariant = LingoNavyCard,
  onSurfaceVariant = LingoTextSecondaryLight,
  outline = LingoNavyStroke,
  error = LingoCoral,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = LingoElectricBlue,
  onPrimary = Color.White,
  primaryContainer = LingoCyanSoft,
  onPrimaryContainer = LingoElectricBlue,
  secondary = LingoViolet,
  onSecondary = Color.White,
  secondaryContainer = LingoVioletSoft,
  onSecondaryContainer = LingoViolet,
  tertiary = LingoEmerald,
  onTertiary = Color.White,
  background = LingoLightBg,
  onBackground = LingoTextPrimaryDark,
  surface = LingoLightSurface,
  onSurface = LingoTextPrimaryDark,
  surfaceVariant = LingoLightCard,
  onSurfaceVariant = LingoTextSecondaryDark,
  outline = LingoLightStroke,
  error = LingoCoral,
  onError = Color.White
)

@Composable
fun LingoSphereTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

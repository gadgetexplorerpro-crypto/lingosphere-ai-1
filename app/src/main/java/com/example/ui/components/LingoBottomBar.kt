package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.LingoElectricBlue

enum class LingoScreen(val label: String) {
  HOME("Home"),
  LEARN("Learn"),
  SPEAK("Speak"),
  PRACTICE("Practice"),
  PROFILE("Profile")
}

@Composable
fun LingoBottomBar(
  currentScreen: LingoScreen,
  onScreenSelected: (LingoScreen) -> Unit
) {
  NavigationBar(
    modifier = Modifier
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("bottom_nav_bar")
  ) {
    NavigationBarItem(
      selected = currentScreen == LingoScreen.HOME,
      onClick = { onScreenSelected(LingoScreen.HOME) },
      icon = {
        Icon(
          imageVector = if (currentScreen == LingoScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
          contentDescription = "Home"
        )
      },
      label = { Text("Home") },
      colors = NavigationBarItemDefaults.colors(indicatorColor = LingoElectricBlue.copy(alpha = 0.2f))
    )
    NavigationBarItem(
      selected = currentScreen == LingoScreen.LEARN,
      onClick = { onScreenSelected(LingoScreen.LEARN) },
      icon = {
        Icon(
          imageVector = if (currentScreen == LingoScreen.LEARN) Icons.Filled.School else Icons.Outlined.School,
          contentDescription = "Learn"
        )
      },
      label = { Text("Learn") },
      colors = NavigationBarItemDefaults.colors(indicatorColor = LingoElectricBlue.copy(alpha = 0.2f))
    )
    NavigationBarItem(
      selected = currentScreen == LingoScreen.SPEAK,
      onClick = { onScreenSelected(LingoScreen.SPEAK) },
      icon = {
        Icon(
          imageVector = if (currentScreen == LingoScreen.SPEAK) Icons.Filled.Mic else Icons.Outlined.Mic,
          contentDescription = "Speak"
        )
      },
      label = { Text("Speak") },
      colors = NavigationBarItemDefaults.colors(indicatorColor = LingoElectricBlue.copy(alpha = 0.2f))
    )
    NavigationBarItem(
      selected = currentScreen == LingoScreen.PRACTICE,
      onClick = { onScreenSelected(LingoScreen.PRACTICE) },
      icon = {
        Icon(
          imageVector = if (currentScreen == LingoScreen.PRACTICE) Icons.Filled.Style else Icons.Outlined.Style,
          contentDescription = "Practice"
        )
      },
      label = { Text("Practice") },
      colors = NavigationBarItemDefaults.colors(indicatorColor = LingoElectricBlue.copy(alpha = 0.2f))
    )
    NavigationBarItem(
      selected = currentScreen == LingoScreen.PROFILE,
      onClick = { onScreenSelected(LingoScreen.PROFILE) },
      icon = {
        Icon(
          imageVector = if (currentScreen == LingoScreen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
          contentDescription = "Profile"
        )
      },
      label = { Text("Profile") },
      colors = NavigationBarItemDefaults.colors(indicatorColor = LingoElectricBlue.copy(alpha = 0.2f))
    )
  }
}

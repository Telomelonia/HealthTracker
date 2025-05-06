package com.caresync.healthtracker

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF6200EE),
  onPrimary = Color.White,
  secondary = Color(0xFF03DAC6),
  onSecondary = Color.Black,
  background = Color(0xFFF5F5F5),
  surface = Color.White,
  onBackground = Color.Black,
  onSurface = Color.Black
)

@Composable
fun HealthTrackerTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    content = content
  )
}
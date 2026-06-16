package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val CelestialColorScheme =
  darkColorScheme(
    primary = Color(0xFFFFB300),       // Celestial gold
    secondary = Color(0xFF4DD0E1),     // Radiant teal
    tertiary = Color(0xFFBA68C8),      // Deep violet
    background = Color(0xFF070414),    // Space background
    surface = Color(0xFF110E1D),       // Glass container
    onPrimary = Color(0xFF0C091A),
    onSecondary = Color(0xFF0C091A),
    onBackground = Color.White,
    onSurface = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force gorgeous dark space theme
  dynamicColor: Boolean = false, // Disable system dynamic coloring to preserve cosmic color integrity
  content: @Composable () -> Unit,
) {
  val colorScheme = CelestialColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

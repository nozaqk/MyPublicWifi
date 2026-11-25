package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = BrandBlueLight,
  onPrimary = Color.White,
  primaryContainer = BrandSidebarDark,
  onPrimaryContainer = Color.White,
  secondary = BrandBlue,
  onSecondary = Color.White,
  background = BackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceDarkSecondary,
  onSurfaceVariant = TextSecondaryDark,
  outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = BrandBlue,
  onPrimary = Color.White,
  primaryContainer = SurfaceLightSecondary,
  onPrimaryContainer = BrandBlueDark,
  secondary = BrandBlueDark,
  onSecondary = Color.White,
  background = BackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceLightSecondary,
  onSurfaceVariant = TextSecondaryLight,
  outline = BorderLight
)

@Composable
fun MyPublicWiFiTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

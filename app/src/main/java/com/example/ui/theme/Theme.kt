package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CafeColorScheme = darkColorScheme(
  primary = CafeCyanPrimary,
  onPrimary = Color(0xFF032125),
  primaryContainer = CafeSurfaceVariant,
  onPrimaryContainer = CafeCyanLight,
  secondary = CafeGold,
  onSecondary = Color(0xFF261A00),
  secondaryContainer = Color(0xFF332505),
  onSecondaryContainer = CafeGoldLight,
  background = CafeDarkBg,
  onBackground = CafeTextPrimary,
  surface = CafeSurface,
  onSurface = CafeTextPrimary,
  surfaceVariant = CafeSurfaceVariant,
  onSurfaceVariant = CafeTextSecondary,
  outline = CafeCardBorder,
  outlineVariant = CafeInputBorder,
  error = OrderStatusCancelled,
  onError = Color.White
)

@Composable
fun CafeTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CafeColorScheme,
    typography = Typography,
    content = content
  )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CafeTheme(
  accentColor: Color = CafeCyanPrimary,
  content: @Composable () -> Unit
) {
  val dynamicScheme = darkColorScheme(
    primary = accentColor,
    onPrimary = Color(0xFF160E04),
    primaryContainer = CafeSurfaceVariant,
    onPrimaryContainer = accentColor,
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

  MaterialTheme(
    colorScheme = dynamicScheme,
    typography = Typography,
    content = content
  )
}

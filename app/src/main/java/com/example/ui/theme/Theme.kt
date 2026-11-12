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

private val DarkColorScheme =
  darkColorScheme(
    primary = PolishPrimaryDark,
    onPrimary = PolishOnPrimaryDark,
    primaryContainer = PolishPrimaryContainerDark,
    onPrimaryContainer = PolishOnPrimaryContainerDark,
    secondary = PolishSecondaryDark,
    onSecondary = PolishOnSecondaryDark,
    secondaryContainer = PolishSecondaryContainerDark,
    onSecondaryContainer = PolishOnSecondaryContainerDark,
    tertiary = PolishTertiaryDark,
    onTertiary = PolishOnTertiaryDark,
    tertiaryContainer = PolishTertiaryContainerDark,
    onTertiaryContainer = PolishOnTertiaryContainerDark,
    background = PolishBackgroundDark,
    surface = PolishSurfaceDark,
    surfaceVariant = PolishSurfaceVariantDark,
    outline = PolishOutlineDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PolishPrimaryLight,
    onPrimary = PolishOnPrimaryLight,
    primaryContainer = PolishPrimaryContainerLight,
    onPrimaryContainer = PolishOnPrimaryContainerLight,
    secondary = PolishSecondaryLight,
    onSecondary = PolishOnSecondaryLight,
    secondaryContainer = PolishSecondaryContainerLight,
    onSecondaryContainer = PolishOnSecondaryContainerLight,
    tertiary = PolishTertiaryLight,
    onTertiary = PolishOnTertiaryLight,
    tertiaryContainer = PolishTertiaryContainerLight,
    onTertiaryContainer = PolishOnTertiaryContainerLight,
    background = PolishBackgroundLight,
    surface = PolishSurfaceLight,
    surfaceVariant = PolishSurfaceVariantLight,
    outline = PolishOutlineLight,
    onBackground = PolishTextPrimaryLight,
    onSurface = PolishTextPrimaryLight,
    onSurfaceVariant = PolishTextSecondaryLight,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our specialized industrial SCADA palette for clear valve status visibility
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

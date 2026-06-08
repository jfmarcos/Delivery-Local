package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkNaturalGreen,
    secondary = SageHighlight,
    tertiary = DarkSageTextSecondary,
    background = DarkSageBg,
    surface = DarkSageCard,
    onPrimary = DeepMossText,
    onSecondary = DeepMossText,
    onBackground = DarkSageTextSecondary,
    onSurface = DarkSageTextSecondary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalGreenPrimary,
    secondary = SageContainer,
    tertiary = DarkSageText,
    background = NaturalBeigeBackground,
    surface = CardSoftBeige,
    onPrimary = Color.White,
    onSecondary = DeepMossText,
    onTertiary = Color.White,
    onBackground = DeepMossText,
    onSurface = DeepMossText,
    outline = OutlineOliveBorder,
    surfaceVariant = SoftOliveNavBg
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom theme looks pristine with specific handpicked colors, disabling dynamic keys
  dynamicColor: Boolean = false,
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

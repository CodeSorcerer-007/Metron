package com.metron.app.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AegeanDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = AegeanAzure,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceContainer,
    onSecondaryContainer = AegeanAzure,
    tertiary = LaurelGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = SpartanRose,
    onError = Color.White
)

private val AthenianLightColorScheme = lightColorScheme(
    primary = BronzeAccent,
    onPrimary = Color.White,
    primaryContainer = GoldLight,
    onPrimaryContainer = DarkBackground,
    secondary = AegeanAzure,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceContainer,
    onSecondaryContainer = DarkBackground,
    tertiary = LaurelGreen,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = SpartanRose,
    onError = Color.White
)

private val OledBlackColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = AegeanAzure,
    onSecondary = Color.Black,
    secondaryContainer = OledSurfaceVariant,
    onSecondaryContainer = AegeanAzure,
    tertiary = LaurelGreen,
    onTertiary = Color.Black,
    background = OledBackground,
    onBackground = DarkTextPrimary,
    surface = OledSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0x33D4AF37),
    error = SpartanRose,
    onError = Color.White
)

@Composable
fun MetronTheme(
    themeMode: String = "AEGEAN_DARK",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "ATHENIAN_LIGHT" -> AthenianLightColorScheme
        "OLED_BLACK" -> OledBlackColorScheme
        else -> AegeanDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val isLight = themeMode == "ATHENIAN_LIGHT"
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

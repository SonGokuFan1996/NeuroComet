package com.kyilmaz.neuronetworkingtitle.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Classic Material Design (M1) Color Palette ---
val M1PrimaryLight = Color(0xFF3F51B5) // Indigo 500
val M1PrimaryDark = Color(0xFF303F9F) // Indigo 700 (used for status bar in light theme)
val M1Accent = Color(0xFFFF4081)      // Pink A200

val M1PrimaryNight = Color(0xFF9FA8DA) // Indigo 200

private val Material1DarkColorScheme = darkColorScheme(
    primary = M1PrimaryNight,
    secondary = M1Accent,
    background = Color(0xFF121212),
    surface = Color(0xFF212121),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val Material1LightColorScheme = lightColorScheme(
    primary = M1PrimaryLight,
    secondary = M1Accent,
    background = Color(0xFFFAFAFA), // M1 uses a slightly off-white background
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun NeuroNetWorkingTitleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // neuroState, quietMode, and dynamicColor are ignored to enforce M1 style.
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        Material1DarkColorScheme
    } else {
        Material1LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to the darker primary variant for M1 style in light theme.
            window.statusBarColor = if (darkTheme) colorScheme.surface.toArgb() else M1PrimaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Keep existing typography for now
        content = content
    )
}
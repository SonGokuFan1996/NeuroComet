package com.kyilmaz.neuronetworkingtitle.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kyilmaz.neuronetworkingtitle.NeuroState

// Helper to generate a simple scheme from a seed color
fun generateLightScheme(seed: Color): ColorScheme {
    // If seed is very light, we need dark text on it. If dark, white text.
    val onPrimaryColor = if (seed.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
    
    return lightColorScheme(
        primary = seed,
        secondary = seed.copy(alpha = 0.7f),
        tertiary = Color(0xFFE91E63), // Universal Accent
        background = Color(0xFFFDFDFD),
        surface = Color(0xFFFFFFFF),
        onPrimary = onPrimaryColor,
        onSecondary = onPrimaryColor, 
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        primaryContainer = seed.copy(alpha = 0.15f), // Very subtle container
        onPrimaryContainer = if (seed.luminance() > 0.5f) Color(0xFF1C1B1F) else seed, // Dark text on light container
        secondaryContainer = seed.copy(alpha = 0.08f),
        onSecondaryContainer = Color(0xFF1D192B)
    )
}

fun generateDarkScheme(seed: Color): ColorScheme {
    // For dark mode, we lighten the seed to make it "glow" against dark backgrounds
    // or keep it saturated if it's already bright.
    val isDarkSeed = seed.luminance() < 0.3f
    val primaryColor = if (isDarkSeed) seed.lighten(1.5f) else seed
    
    return darkColorScheme(
        primary = primaryColor,
        secondary = primaryColor.copy(alpha = 0.7f),
        tertiary = Color(0xFFF48FB1),
        background = Color(0xFF121016),
        surface = Color(0xFF1E1C24),
        onPrimary = if (primaryColor.luminance() > 0.5f) Color(0xFF381E72) else Color.White,
        onBackground = Color(0xFFE6E1E5),
        onSurface = Color(0xFFE6E1E5),
        primaryContainer = primaryColor.copy(alpha = 0.2f),
        onPrimaryContainer = primaryColor.lighten(0.8f) // Ensure visibility
    )
}

// Simple color utils
fun Color.lighten(factor: Float): Color {
    val red = (this.red * factor).coerceAtMost(1f)
    val green = (this.green * factor).coerceAtMost(1f)
    val blue = (this.blue * factor).coerceAtMost(1f)
    return Color(red, green, blue, this.alpha)
}

fun Color.darken(factor: Float): Color {
    val red = (this.red * factor).coerceAtMost(1f)
    val green = (this.green * factor).coerceAtMost(1f)
    val blue = (this.blue * factor).coerceAtMost(1f)
    return Color(red, green, blue, this.alpha)
}

fun Color.desaturate(factor: Float = 0.2f): Color {
    // A simplified desaturation: blend with a gray of similar luminance
    val grayValue = this.luminance()
    val gray = Color(grayValue, grayValue, grayValue, this.alpha)
    
    // factor 0.0 = original, 1.0 = fully gray
    val r = this.red * (1 - factor) + gray.red * factor
    val g = this.green * (1 - factor) + gray.green * factor
    val b = this.blue * (1 - factor) + gray.blue * factor
    
    return Color(r, g, b, this.alpha)
}

@Composable
fun NeuroNetWorkingTitleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    neuroState: NeuroState = NeuroState.DEFAULT,
    quietMode: Boolean = false, // Added quietMode parameter
    content: @Composable () -> Unit
) {
    // In Quiet Mode, we desaturate the seed color significantly
    val effectiveSeed = if (quietMode) {
        neuroState.seedColor.desaturate(0.7f) // 70% desaturated
    } else {
        neuroState.seedColor
    }

    val colorScheme = if (darkTheme) {
        generateDarkScheme(effectiveSeed)
    } else {
        generateLightScheme(effectiveSeed)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

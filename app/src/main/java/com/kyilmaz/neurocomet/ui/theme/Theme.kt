package com.kyilmaz.neurocomet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// DYNAMIC COLOR THEME SETTINGS
// ============================================================================

/**
 * Enum representing the available theme modes for the app.
 */
enum class ThemeMode {
    /** Use system default (follows device light/dark setting) */
    SYSTEM,
    /** Force light theme */
    LIGHT,
    /** Force dark theme */
    DARK
}

/**
 * Enum representing the color scheme source.
 */
enum class ColorSchemeSource {
    /** Use Material 3 Dynamic Colors from device wallpaper (Android 12+) */
    DYNAMIC,
    /** Use the classic Material 1 inspired palette */
    CLASSIC,
    /** Use the neurodivergent-friendly default palette */
    NEURODIVERGENT
}

// ============================================================================
// CLASSIC MATERIAL DESIGN (M1) COLOR PALETTE
// ============================================================================

private val M1PrimaryLightColor = Color(0xFF3F51B5) // Indigo 500
private val M1AccentColor = Color(0xFFFF4081)      // Pink A200
private val M1PrimaryNightColor = Color(0xFF9FA8DA) // Indigo 200

private val Material1DarkColorScheme = darkColorScheme(
    primary = M1PrimaryNightColor,
    secondary = M1AccentColor,
    background = Color(0xFF121212),
    surface = Color(0xFF212121),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val Material1LightColorScheme = lightColorScheme(
    primary = M1PrimaryLightColor,
    secondary = M1AccentColor,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// ============================================================================
// NEURODIVERGENT-FRIENDLY COLOR PALETTE (M3 Inspired)
// Designed to be calming, accessible, and reduce sensory overwhelm
// ============================================================================

private val NeurodivergentLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),           // Calming purple
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),          // Warm accent
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFD0BCFF)
)

private val NeurodivergentDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),           // Soft purple for dark mode
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),          // Warm accent for dark
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF6750A4)
)

// Public accessors for backwards compatibility
val M1PrimaryLight = M1PrimaryLightColor
val M1Accent = M1AccentColor
val M1PrimaryNight = M1PrimaryNightColor

/**
 * Determines if dynamic colors are available on the current device.
 * Dynamic colors require Android 12 (API 31) or higher.
 */
fun isDynamicColorAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Main theme composable for the NeuroComet app.
 *
 * Supports:
 * - Material 3 Dynamic Colors (Android 12+) - pulls colors from device wallpaper
 * - Classic Material 1 inspired palette
 * - Neurodivergent-friendly custom palette
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic colors (requires Android 12+)
 * @param colorSchemeSource The source for the color scheme
 * @param content The composable content
 */
@Composable
fun NeuroCometWorkingTitleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    colorSchemeSource: ColorSchemeSource = ColorSchemeSource.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Select the appropriate color scheme based on settings
    val colorScheme: ColorScheme = when {
        // Dynamic colors - use system wallpaper colors (Android 12+)
        dynamicColor && colorSchemeSource == ColorSchemeSource.DYNAMIC && isDynamicColorAvailable() -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // Classic Material 1 palette
        colorSchemeSource == ColorSchemeSource.CLASSIC -> {
            if (darkTheme) Material1DarkColorScheme else Material1LightColorScheme
        }
        // Neurodivergent-friendly palette (default fallback)
        else -> {
            if (darkTheme) NeurodivergentDarkColorScheme else NeurodivergentLightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        val insetsController = remember(window, view) {
            WindowCompat.getInsetsController(window, view)
        }

        SideEffect {
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Simplified theme composable that auto-detects dynamic color support.
 * Uses dynamic colors if available, falls back to neurodivergent palette.
 */
@Composable
fun NeuroCometDynamicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    NeuroCometWorkingTitleTheme(
        darkTheme = darkTheme,
        dynamicColor = true,
        colorSchemeSource = ColorSchemeSource.DYNAMIC,
        content = content
    )
}

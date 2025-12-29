package com.kyilmaz.neuronetworkingtitle

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.kyilmaz.neuronetworkingtitle.NeuroState // Import local enum

// --- Theme Utility Functions ---

/**
 * Creates a new Typography object with all font sizes scaled by the given factor.
 */
fun scaledTypography(scale: Float): Typography {
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontSize = defaultTypography.displayLarge.fontSize * scale),
        displayMedium = defaultTypography.displayMedium.copy(fontSize = defaultTypography.displayMedium.fontSize * scale),
        displaySmall = defaultTypography.displaySmall.copy(fontSize = defaultTypography.displaySmall.fontSize * scale),
        headlineLarge = defaultTypography.headlineLarge.copy(fontSize = defaultTypography.headlineLarge.fontSize * scale),
        headlineMedium = defaultTypography.headlineMedium.copy(fontSize = defaultTypography.headlineMedium.fontSize * scale),
        headlineSmall = defaultTypography.headlineSmall.copy(fontSize = defaultTypography.headlineSmall.fontSize * scale),
        titleLarge = defaultTypography.titleLarge.copy(fontSize = defaultTypography.titleLarge.fontSize * scale),
        titleMedium = defaultTypography.titleMedium.copy(fontSize = defaultTypography.titleMedium.fontSize * scale),
        titleSmall = defaultTypography.titleSmall.copy(fontSize = defaultTypography.titleSmall.fontSize * scale),
        bodyLarge = defaultTypography.bodyLarge.copy(fontSize = defaultTypography.bodyLarge.fontSize * scale),
        bodyMedium = defaultTypography.bodyMedium.copy(fontSize = defaultTypography.bodyMedium.fontSize * scale),
        bodySmall = defaultTypography.bodySmall.copy(fontSize = defaultTypography.bodySmall.fontSize * scale),
        labelLarge = defaultTypography.labelLarge.copy(fontSize = defaultTypography.labelLarge.fontSize * scale),
        labelMedium = defaultTypography.labelMedium.copy(fontSize = defaultTypography.labelMedium.fontSize * scale),
        labelSmall = defaultTypography.labelSmall.copy(fontSize = defaultTypography.labelSmall.fontSize * scale)
    )
}

fun defaultTheme(darkTheme: Boolean) = if (darkTheme) darkColorScheme() else lightColorScheme()
fun highContrastTheme(darkTheme: Boolean) = if (darkTheme) DarkHyperfocusColorScheme else LightHyperfocusColorScheme
fun quietModeTheme(darkTheme: Boolean) = if (darkTheme) DarkOverloadColorScheme else LightOverloadColorScheme
fun calmTheme(darkTheme: Boolean) = if (darkTheme) DarkCalmColorScheme else LightCalmColorScheme

fun highContrastOverlay(colorScheme: ColorScheme, darkTheme: Boolean): ColorScheme {
    val pureWhite = Color(0xFFFFFFFF)
    val pureBlack = Color(0xFF000000)

    return if (darkTheme) {
        colorScheme.copy(
            background = pureBlack,
            surface = pureBlack,
            onBackground = pureWhite,
            onSurface = pureWhite,
            primary = pureWhite,
            onPrimary = pureBlack,
            secondary = Color(0xFFFFFF00),
            onSecondary = pureBlack
        )
    } else {
        colorScheme.copy(
            background = pureWhite,
            surface = pureWhite,
            onBackground = pureBlack,
            onSurface = pureBlack,
            primary = pureBlack,
            onPrimary = pureWhite,
            secondary = Color(0xFF0000FF),
            onSecondary = pureWhite
        )
    }
}

// Color Schemes (Simplified Mocked Values for illustration)
val LightHyperfocusColorScheme = lightColorScheme(
    primary = Color(0xFF1F2937), onPrimary = Color(0xFFFFFFFF), background = Color(0xFFF8FAFC), onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF0F172A), surfaceVariant = Color(0xFFE2E8F0), onSurfaceVariant = Color(0xFF334155), outline = Color(0xFF94A3B8)
)
val DarkHyperfocusColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA), onPrimary = Color(0xFF0B1220), background = Color(0xFF0B1220), onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827), onSurface = Color(0xFFE5E7EB), surfaceVariant = Color(0xFF1F2937), onSurfaceVariant = Color(0xFFCBD5E1), outline = Color(0xFF475569)
)
val LightOverloadColorScheme = lightColorScheme(
    primary = Color(0xFF64748B), onPrimary = Color(0xFFFFFFFF), background = Color(0xFFF1F5F9), onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF0F172A), surfaceVariant = Color(0xFFE2E8F0), onSurfaceVariant = Color(0xFF334155)
)
val DarkOverloadColorScheme = darkColorScheme(
    primary = Color(0xFF94A3B8), onPrimary = Color(0xFF0B1220), background = Color(0xFF0B1220), onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827), onSurface = Color(0xFFE2E8F0), surfaceVariant = Color(0xFF1F2937), onSurfaceVariant = Color(0xFFCBD5E1)
)
val LightCalmColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), onPrimary = Color(0xFFFFFFFF), background = Color(0xFFF1F8F4), onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF0F172A), surfaceVariant = Color(0xFFDDEFE3), onSurfaceVariant = Color(0xFF1F3A2B)
)
val DarkCalmColorScheme = darkColorScheme(
    primary = Color(0xFF81C784), onPrimary = Color(0xFF0B1220), background = Color(0xFF0F1B14), onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF13261C), onSurface = Color(0xFFE2E8F0), surfaceVariant = Color(0xFF1B3A2A), onSurfaceVariant = Color(0xFFCFE8D8)
)

// --- Composable Theme Wrapper ---
@Composable
fun NeuroThemeApplication(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()

    var colorScheme = when (themeState.selectedState) {
        NeuroState.HYPERFOCUS -> highContrastTheme(themeState.isDarkMode)
        NeuroState.OVERLOAD -> quietModeTheme(themeState.isDarkMode)
        NeuroState.CALM -> calmTheme(themeState.isDarkMode)
        NeuroState.DEFAULT -> defaultTheme(themeState.isDarkMode)
    }

    if (themeState.isHighContrast) {
        colorScheme = highContrastOverlay(colorScheme, themeState.isDarkMode)
    }

    // Custom Typography scaled by the user's accessibility setting
    val typography = scaledTypography(themeState.textScaleFactor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
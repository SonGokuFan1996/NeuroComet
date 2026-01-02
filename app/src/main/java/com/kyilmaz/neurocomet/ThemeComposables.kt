package com.kyilmaz.neurocomet

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * Neurodivergent-friendly theming engine for the NeuroComet application.
 *
 * This file contains:
 * - Color schemes designed for various neurodivergent needs (ADHD, Autism, Anxiety, etc.)
 * - Mood-based themes that adapt to how the user is feeling
 * - Accessibility-focused themes (Dyslexia-friendly, High Contrast)
 * - Typography scaling utilities
 *
 * Each color palette is carefully designed to:
 * - Reduce sensory overwhelm when needed
 * - Provide appropriate stimulation for focus
 * - Maintain WCAG accessibility standards
 * - Support both light and dark modes
 */

// ============================================================================
// TYPOGRAPHY UTILITIES
// ============================================================================

/**
 * Creates a new Typography object with all font sizes scaled by the given factor.
 * Useful for accessibility settings where users need larger text.
 *
 * @param scale The scaling factor (1.0 = normal, 1.5 = 50% larger, etc.)
 * @return Typography object with scaled font sizes
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

// ============================================================================
// NEURODIVERGENT-FRIENDLY COLOR PALETTES
// Each palette is designed with specific sensory and cognitive needs in mind
// ============================================================================

// --- BASIC THEMES ---

// Default Theme - Balanced, neutral colors
val LightDefaultColorScheme = lightColorScheme(
    primary = Color(0xFF5C6BC0), onPrimary = Color.White,
    secondary = Color(0xFF7E57C2), onSecondary = Color.White,
    tertiary = Color(0xFF26A69A), onTertiary = Color.White,
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF1C1B1F),
    surface = Color.White, onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC), onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E), error = Color(0xFFB3261E), onError = Color.White
)
val DarkDefaultColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA), onPrimary = Color(0xFF1A237E),
    secondary = Color(0xFFB39DDB), onSecondary = Color(0xFF311B92),
    tertiary = Color(0xFF80CBC4), onTertiary = Color(0xFF004D40),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF252429), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F), onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99), error = Color(0xFFF2B8B5), onError = Color(0xFF601410)
)

// Hyperfocus Theme - High contrast, minimal distractions, focus-enhancing blues
val LightHyperfocusColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0), onPrimary = Color.White,
    primaryContainer = Color(0xFF90CAF9), onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF0277BD), onSecondary = Color.White,
    secondaryContainer = Color(0xFF81D4FA), onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF00838F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFF80DEEA), onTertiaryContainer = Color(0xFF006064),
    background = Color(0xFFF5F9FC), onBackground = Color(0xFF0D1627),
    surface = Color.White, onSurface = Color(0xFF0D1627),
    surfaceVariant = Color(0xFFE3EDF5), onSurfaceVariant = Color(0xFF2A364A),
    surfaceContainerHigh = Color(0xFFBBDEFB), // More visible blue for received messages
    outline = Color(0xFF5C7A99), error = Color(0xFFC62828), onError = Color.White
)
val DarkHyperfocusColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6), onPrimary = Color(0xFF0A1929),
    primaryContainer = Color(0xFF1565C0), onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF4FC3F7), onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF0277BD), onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = Color(0xFF4DD0E1), onTertiary = Color(0xFF006064),
    tertiaryContainer = Color(0xFF00838F), onTertiaryContainer = Color(0xFFB2EBF2),
    background = Color(0xFF0A1929), onBackground = Color(0xFFE3F2FD),
    surface = Color(0xFF102A43), onSurface = Color(0xFFE3F2FD),
    surfaceVariant = Color(0xFF1B3A52), onSurfaceVariant = Color(0xFFB3D4F0),
    surfaceContainerHigh = Color(0xFF1E5080), // More visible blue for received messages
    outline = Color(0xFF5C8AB3), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// Overload/Quiet Theme - Muted, desaturated, gentle colors for sensory overwhelm
val LightOverloadColorScheme = lightColorScheme(
    primary = Color(0xFF607D8B), onPrimary = Color.White,
    secondary = Color(0xFF78909C), onSecondary = Color.White,
    tertiary = Color(0xFF90A4AE), onTertiary = Color(0xFF263238),
    background = Color(0xFFF5F5F5), onBackground = Color(0xFF37474F),
    surface = Color(0xFFFAFAFA), onSurface = Color(0xFF37474F),
    surfaceVariant = Color(0xFFECEFF1), onSurfaceVariant = Color(0xFF546E7A),
    outline = Color(0xFFB0BEC5), error = Color(0xFF8D6E63), onError = Color.White
)
val DarkOverloadColorScheme = darkColorScheme(
    primary = Color(0xFF90A4AE), onPrimary = Color(0xFF263238),
    secondary = Color(0xFFB0BEC5), onSecondary = Color(0xFF37474F),
    tertiary = Color(0xFFCFD8DC), onTertiary = Color(0xFF455A64),
    background = Color(0xFF1A1E20), onBackground = Color(0xFFCFD8DC),
    surface = Color(0xFF252B2E), onSurface = Color(0xFFCFD8DC),
    surfaceVariant = Color(0xFF37474F), onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF78909C), error = Color(0xFFBCAAA4), onError = Color(0xFF3E2723)
)

// Calm Theme - Soft greens and blues, nature-inspired for relaxation
val LightCalmColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7), onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF66BB6A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F6CA), onSecondaryContainer = Color(0xFF2E7D32),
    tertiary = Color(0xFF81C784), onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF81C784), onTertiaryContainer = Color(0xFF1B5E20),
    background = Color(0xFFF1F8E9), onBackground = Color(0xFF1B5E20),
    surface = Color(0xFFFFFDE7), onSurface = Color(0xFF33691E),
    surfaceVariant = Color(0xFFDCEDC8), onSurfaceVariant = Color(0xFF558B2F),
    surfaceContainerHigh = Color(0xFFC8E6C9), // More visible green for received messages
    outline = Color(0xFFA5D6A7), error = Color(0xFFE57373), onError = Color.White
)
val DarkCalmColorScheme = darkColorScheme(
    primary = Color(0xFF81C784), onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32), onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA5D6A7), onSecondary = Color(0xFF2E7D32),
    secondaryContainer = Color(0xFF388E3C), onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = Color(0xFFC8E6C9), onTertiary = Color(0xFF388E3C),
    tertiaryContainer = Color(0xFF43A047), onTertiaryContainer = Color(0xFFA5D6A7),
    background = Color(0xFF0D1A0D), onBackground = Color(0xFFC8E6C9),
    surface = Color(0xFF1A2E1A), onSurface = Color(0xFFC8E6C9),
    surfaceVariant = Color(0xFF2E4A2E), onSurfaceVariant = Color(0xFFA5D6A7),
    surfaceContainerHigh = Color(0xFF3D5C3D), // More visible green for received messages
    outline = Color(0xFF66BB6A), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// --- ADHD THEMES ---

// ADHD Energized - Bright, engaging colors for productive high-energy days
val LightADHDEnergizedColorScheme = lightColorScheme(
    primary = Color(0xFFFF6D00), onPrimary = Color.White,
    secondary = Color(0xFFFFAB00), onSecondary = Color(0xFF3E2723),
    tertiary = Color(0xFFFFD600), onTertiary = Color(0xFF3E2723),
    background = Color(0xFFFFF8E1), onBackground = Color(0xFF3E2723),
    surface = Color.White, onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFFFECB3), onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFFFCC80), error = Color(0xFFD84315), onError = Color.White
)
val DarkADHDEnergizedColorScheme = darkColorScheme(
    primary = Color(0xFFFFAB40), onPrimary = Color(0xFF3E2723),
    secondary = Color(0xFFFFD54F), onSecondary = Color(0xFF3E2723),
    tertiary = Color(0xFFFFE082), onTertiary = Color(0xFF5D4037),
    background = Color(0xFF1A1510), onBackground = Color(0xFFFFE0B2),
    surface = Color(0xFF2A2015), onSurface = Color(0xFFFFE0B2),
    surfaceVariant = Color(0xFF3E2F20), onSurfaceVariant = Color(0xFFFFCC80),
    outline = Color(0xFFFFB74D), error = Color(0xFFFF8A65), onError = Color(0xFF3E2723)
)

// ADHD Low Dopamine - Warm, stimulating colors to boost mood and motivation
val LightADHDLowDopamineColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63), onPrimary = Color.White,
    secondary = Color(0xFFF48FB1), onSecondary = Color(0xFF880E4F),
    tertiary = Color(0xFFCE93D8), onTertiary = Color(0xFF4A148C),
    background = Color(0xFFFCE4EC), onBackground = Color(0xFF4A0E24),
    surface = Color.White, onSurface = Color(0xFF4A0E24),
    surfaceVariant = Color(0xFFF8BBD9), onSurfaceVariant = Color(0xFF880E4F),
    outline = Color(0xFFF48FB1), error = Color(0xFFD32F2F), onError = Color.White
)
val DarkADHDLowDopamineColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1), onPrimary = Color(0xFF880E4F),
    secondary = Color(0xFFF8BBD9), onSecondary = Color(0xFFC2185B),
    tertiary = Color(0xFFE1BEE7), onTertiary = Color(0xFF7B1FA2),
    background = Color(0xFF1A0A10), onBackground = Color(0xFFF8BBD9),
    surface = Color(0xFF2A1520), onSurface = Color(0xFFF8BBD9),
    surfaceVariant = Color(0xFF4A2030), onSurfaceVariant = Color(0xFFF48FB1),
    outline = Color(0xFFEC407A), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// ADHD Task Mode - Minimal distractions, focus-enhancing neutral palette
val LightADHDTaskModeColorScheme = lightColorScheme(
    primary = Color(0xFF455A64), onPrimary = Color.White,
    secondary = Color(0xFF546E7A), onSecondary = Color.White,
    tertiary = Color(0xFF607D8B), onTertiary = Color.White,
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF263238),
    surface = Color.White, onSurface = Color(0xFF263238),
    surfaceVariant = Color(0xFFECEFF1), onSurfaceVariant = Color(0xFF455A64),
    outline = Color(0xFFB0BEC5), error = Color(0xFFB71C1C), onError = Color.White
)
val DarkADHDTaskModeColorScheme = darkColorScheme(
    primary = Color(0xFF90A4AE), onPrimary = Color(0xFF263238),
    secondary = Color(0xFFB0BEC5), onSecondary = Color(0xFF37474F),
    tertiary = Color(0xFFCFD8DC), onTertiary = Color(0xFF455A64),
    background = Color(0xFF121518), onBackground = Color(0xFFCFD8DC),
    surface = Color(0xFF1E2428), onSurface = Color(0xFFCFD8DC),
    surfaceVariant = Color(0xFF2E3840), onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF78909C), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// --- AUTISM THEMES ---

// Autism Routine - Predictable, consistent colors with clear visual hierarchy
val LightAutismRoutineColorScheme = lightColorScheme(
    primary = Color(0xFF3949AB), onPrimary = Color.White,
    secondary = Color(0xFF5C6BC0), onSecondary = Color.White,
    tertiary = Color(0xFF7986CB), onTertiary = Color.White,
    background = Color(0xFFE8EAF6), onBackground = Color(0xFF1A237E),
    surface = Color.White, onSurface = Color(0xFF1A237E),
    surfaceVariant = Color(0xFFC5CAE9), onSurfaceVariant = Color(0xFF303F9F),
    outline = Color(0xFF9FA8DA), error = Color(0xFFB71C1C), onError = Color.White
)
val DarkAutismRoutineColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA), onPrimary = Color(0xFF1A237E),
    secondary = Color(0xFFC5CAE9), onSecondary = Color(0xFF283593),
    tertiary = Color(0xFFE8EAF6), onTertiary = Color(0xFF3949AB),
    background = Color(0xFF0A0D1A), onBackground = Color(0xFFC5CAE9),
    surface = Color(0xFF151A2E), onSurface = Color(0xFFC5CAE9),
    surfaceVariant = Color(0xFF232B45), onSurfaceVariant = Color(0xFF9FA8DA),
    outline = Color(0xFF5C6BC0), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// Autism Sensory Seeking - Rich, satisfying contrasts and engaging colors
val LightAutismSensorySeekColorScheme = lightColorScheme(
    primary = Color(0xFF7B1FA2), onPrimary = Color.White,
    secondary = Color(0xFF9C27B0), onSecondary = Color.White,
    tertiary = Color(0xFF00BCD4), onTertiary = Color.White,
    background = Color(0xFFF3E5F5), onBackground = Color(0xFF311B92),
    surface = Color.White, onSurface = Color(0xFF311B92),
    surfaceVariant = Color(0xFFE1BEE7), onSurfaceVariant = Color(0xFF6A1B9A),
    outline = Color(0xFFCE93D8), error = Color(0xFFD32F2F), onError = Color.White
)
val DarkAutismSensorySeekColorScheme = darkColorScheme(
    primary = Color(0xFFCE93D8), onPrimary = Color(0xFF4A148C),
    secondary = Color(0xFFE1BEE7), onSecondary = Color(0xFF6A1B9A),
    tertiary = Color(0xFF80DEEA), onTertiary = Color(0xFF006064),
    background = Color(0xFF120A15), onBackground = Color(0xFFE1BEE7),
    surface = Color(0xFF1F152A), onSurface = Color(0xFFE1BEE7),
    surfaceVariant = Color(0xFF32204A), onSurfaceVariant = Color(0xFFCE93D8),
    outline = Color(0xFFAB47BC), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// Autism Low Stimulation - Very muted, gentle colors for sensory rest
val LightAutismLowStimColorScheme = lightColorScheme(
    primary = Color(0xFF8D9EAB), onPrimary = Color.White,
    primaryContainer = Color(0xFFCFD8DC), onPrimaryContainer = Color(0xFF37474F),
    secondary = Color(0xFFA0B0BD), onSecondary = Color(0xFF37474F),
    secondaryContainer = Color(0xFFB0BEC5), onSecondaryContainer = Color(0xFF455A64),
    tertiary = Color(0xFFB3C2CF), onTertiary = Color(0xFF455A64),
    tertiaryContainer = Color(0xFFCFD8DC), onTertiaryContainer = Color(0xFF546E7A),
    background = Color(0xFFF7F8F9), onBackground = Color(0xFF455A64),
    surface = Color(0xFFFCFCFC), onSurface = Color(0xFF455A64),
    surfaceVariant = Color(0xFFEFF1F3), onSurfaceVariant = Color(0xFF607D8B),
    surfaceContainerHigh = Color(0xFFCFD8DC), // More visible gray for received messages
    outline = Color(0xFFCFD8DC), error = Color(0xFFA1887F), onError = Color.White
)
val DarkAutismLowStimColorScheme = darkColorScheme(
    primary = Color(0xFFB0BEC5), onPrimary = Color(0xFF37474F),
    primaryContainer = Color(0xFF546E7A), onPrimaryContainer = Color(0xFFCFD8DC),
    secondary = Color(0xFFCFD8DC), onSecondary = Color(0xFF455A64),
    secondaryContainer = Color(0xFF607D8B), onSecondaryContainer = Color(0xFFECEFF1),
    tertiary = Color(0xFFECEFF1), onTertiary = Color(0xFF546E7A),
    tertiaryContainer = Color(0xFF78909C), onTertiaryContainer = Color(0xFFCFD8DC),
    background = Color(0xFF16191B), onBackground = Color(0xFFCFD8DC),
    surface = Color(0xFF1E2225), onSurface = Color(0xFFCFD8DC),
    surfaceVariant = Color(0xFF2C3135), onSurfaceVariant = Color(0xFFB0BEC5),
    surfaceContainerHigh = Color(0xFF455A64), // More visible gray for received messages
    outline = Color(0xFF78909C), error = Color(0xFFBCAAA4), onError = Color(0xFF3E2723)
)

// --- ANXIETY/OCD THEMES ---

// Anxiety Soothe - Cool, reassuring blues to ease worry
val LightAnxietySootheColorScheme = lightColorScheme(
    primary = Color(0xFF039BE5), onPrimary = Color.White,
    secondary = Color(0xFF4FC3F7), onSecondary = Color(0xFF01579B),
    tertiary = Color(0xFF81D4FA), onTertiary = Color(0xFF0277BD),
    background = Color(0xFFE1F5FE), onBackground = Color(0xFF01579B),
    surface = Color.White, onSurface = Color(0xFF01579B),
    surfaceVariant = Color(0xFFB3E5FC), onSurfaceVariant = Color(0xFF0288D1),
    outline = Color(0xFF4FC3F7), error = Color(0xFFE57373), onError = Color.White
)
val DarkAnxietySootheColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7), onPrimary = Color(0xFF01579B),
    secondary = Color(0xFF81D4FA), onSecondary = Color(0xFF0277BD),
    tertiary = Color(0xFFB3E5FC), onTertiary = Color(0xFF0288D1),
    background = Color(0xFF051520), onBackground = Color(0xFFB3E5FC),
    surface = Color(0xFF0A2030), onSurface = Color(0xFFB3E5FC),
    surfaceVariant = Color(0xFF0F2D45), onSurfaceVariant = Color(0xFF81D4FA),
    outline = Color(0xFF29B6F6), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// Anxiety Grounding - Earthy, stable colors for centering and grounding
val LightAnxietyGroundingColorScheme = lightColorScheme(
    primary = Color(0xFF6D4C41), onPrimary = Color.White,
    secondary = Color(0xFF8D6E63), onSecondary = Color.White,
    tertiary = Color(0xFF795548), onTertiary = Color.White,
    background = Color(0xFFEFEBE9), onBackground = Color(0xFF3E2723),
    surface = Color.White, onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFD7CCC8), onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFBCAAA4), error = Color(0xFFB71C1C), onError = Color.White
)
val DarkAnxietyGroundingColorScheme = darkColorScheme(
    primary = Color(0xFFBCAAA4), onPrimary = Color(0xFF3E2723),
    secondary = Color(0xFFD7CCC8), onSecondary = Color(0xFF4E342E),
    tertiary = Color(0xFFEFEBE9), onTertiary = Color(0xFF5D4037),
    background = Color(0xFF1A1412), onBackground = Color(0xFFD7CCC8),
    surface = Color(0xFF2A2220), onSurface = Color(0xFFD7CCC8),
    surfaceVariant = Color(0xFF3E3230), onSurfaceVariant = Color(0xFFBCAAA4),
    outline = Color(0xFF8D6E63), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// --- DYSLEXIA FRIENDLY THEME ---

// Dyslexia Friendly - High readability with optimal contrast, warm background
val LightDyslexiaFriendlyColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0), onPrimary = Color.White,
    secondary = Color(0xFF0D47A1), onSecondary = Color.White,
    tertiary = Color(0xFF283593), onTertiary = Color.White,
    background = Color(0xFFFFFBF0), onBackground = Color(0xFF212121), // Cream background reduces visual stress
    surface = Color(0xFFFFF8E7), onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F0E0), onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF757575), error = Color(0xFFB71C1C), onError = Color.White
)
val DarkDyslexiaFriendlyColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9), onPrimary = Color(0xFF0D47A1),
    secondary = Color(0xFF64B5F6), onSecondary = Color(0xFF1565C0),
    tertiary = Color(0xFF42A5F5), onTertiary = Color(0xFF1976D2),
    background = Color(0xFF1A1814), onBackground = Color(0xFFF5F0E0), // Warm dark background
    surface = Color(0xFF252218), onSurface = Color(0xFFF5F0E0),
    surfaceVariant = Color(0xFF3A3528), onSurfaceVariant = Color(0xFFE0D8C8),
    outline = Color(0xFF9E9E9E), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// --- MOOD-BASED THEMES ---

// Mood Tired - Gentle colors that don't strain the eyes
val LightMoodTiredColorScheme = lightColorScheme(
    primary = Color(0xFF7986CB), onPrimary = Color.White,
    secondary = Color(0xFF9FA8DA), onSecondary = Color(0xFF303F9F),
    tertiary = Color(0xFFC5CAE9), onTertiary = Color(0xFF3949AB),
    background = Color(0xFFF5F6FA), onBackground = Color(0xFF3C4858),
    surface = Color(0xFFFAFBFC), onSurface = Color(0xFF3C4858),
    surfaceVariant = Color(0xFFE8EAF6), onSurfaceVariant = Color(0xFF5C6BC0),
    outline = Color(0xFFC5CAE9), error = Color(0xFFE57373), onError = Color.White
)
val DarkMoodTiredColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA), onPrimary = Color(0xFF283593),
    secondary = Color(0xFFC5CAE9), onSecondary = Color(0xFF3949AB),
    tertiary = Color(0xFFE8EAF6), onTertiary = Color(0xFF5C6BC0),
    background = Color(0xFF12141A), onBackground = Color(0xFFE8EAF6),
    surface = Color(0xFF1A1D25), onSurface = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF252A35), onSurfaceVariant = Color(0xFFC5CAE9),
    outline = Color(0xFF7986CB), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// Mood Anxious - Calming palette to reduce stress
val LightMoodAnxiousColorScheme = LightAnxietySootheColorScheme
val DarkMoodAnxiousColorScheme = DarkAnxietySootheColorScheme

// Mood Happy - Cheerful colors to match and amplify positive mood
val LightMoodHappyColorScheme = lightColorScheme(
    primary = Color(0xFFFFA000), onPrimary = Color.White,
    secondary = Color(0xFFFFCA28), onSecondary = Color(0xFF5D4037),
    tertiary = Color(0xFFFFF176), onTertiary = Color(0xFF5D4037),
    background = Color(0xFFFFFDE7), onBackground = Color(0xFF3E2723),
    surface = Color.White, onSurface = Color(0xFF3E2723),
    surfaceVariant = Color(0xFFFFF9C4), onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFFFE082), error = Color(0xFFD84315), onError = Color.White
)
val DarkMoodHappyColorScheme = darkColorScheme(
    primary = Color(0xFFFFCA28), onPrimary = Color(0xFF5D4037),
    secondary = Color(0xFFFFE082), onSecondary = Color(0xFF6D4C41),
    tertiary = Color(0xFFFFF59D), onTertiary = Color(0xFF795548),
    background = Color(0xFF1A1508), onBackground = Color(0xFFFFF9C4),
    surface = Color(0xFF2A2010), onSurface = Color(0xFFFFF9C4),
    surfaceVariant = Color(0xFF3A2D18), onSurfaceVariant = Color(0xFFFFE082),
    outline = Color(0xFFFFC107), error = Color(0xFFFF8A65), onError = Color(0xFF3E2723)
)

// Mood Overwhelmed - Simplified, quiet palette for overstimulation
val LightMoodOverwhelmedColorScheme = LightAutismLowStimColorScheme
val DarkMoodOverwhelmedColorScheme = DarkAutismLowStimColorScheme

// Mood Creative - Inspiring colors to fuel imagination
val LightMoodCreativeColorScheme = lightColorScheme(
    primary = Color(0xFF6A1B9A), onPrimary = Color.White,
    secondary = Color(0xFF00897B), onSecondary = Color.White,
    tertiary = Color(0xFFFF6F00), onTertiary = Color.White,
    background = Color(0xFFF3E5F5), onBackground = Color(0xFF311B92),
    surface = Color.White, onSurface = Color(0xFF311B92),
    surfaceVariant = Color(0xFFE1BEE7), onSurfaceVariant = Color(0xFF4A148C),
    outline = Color(0xFFCE93D8), error = Color(0xFFD32F2F), onError = Color.White
)
val DarkMoodCreativeColorScheme = darkColorScheme(
    primary = Color(0xFFCE93D8), onPrimary = Color(0xFF4A148C),
    secondary = Color(0xFF80CBC4), onSecondary = Color(0xFF004D40),
    tertiary = Color(0xFFFFAB40), onTertiary = Color(0xFF5D4037),
    background = Color(0xFF120A18), onBackground = Color(0xFFE1BEE7),
    surface = Color(0xFF1F1528), onSurface = Color(0xFFE1BEE7),
    surfaceVariant = Color(0xFF30203D), onSurfaceVariant = Color(0xFFCE93D8),
    outline = Color(0xFFAB47BC), error = Color(0xFFEF9A9A), onError = Color(0xFF7F0000)
)

// --- COLORBLIND-FRIENDLY THEMES ---
// These palettes use colors that are distinguishable for people with various types of color vision deficiency

// Deuteranopia (Green-weak) - Uses blue/orange contrast instead of red/green
val LightDeuteranopiaColorScheme = lightColorScheme(
    primary = Color(0xFF0077BB), onPrimary = Color.White, // Blue
    secondary = Color(0xFFEE7733), onSecondary = Color.White, // Orange
    tertiary = Color(0xFF009988), onTertiary = Color.White, // Teal
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF1A1A2E),
    surface = Color.White, onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8F4F8), onSurfaceVariant = Color(0xFF004466),
    outline = Color(0xFF88CCEE), error = Color(0xFFCC3311), onError = Color.White
)
val DarkDeuteranopiaColorScheme = darkColorScheme(
    primary = Color(0xFF88CCEE), onPrimary = Color(0xFF003355), // Light blue
    secondary = Color(0xFFEE9955), onSecondary = Color(0xFF663300), // Light orange
    tertiary = Color(0xFF44BB99), onTertiary = Color(0xFF004D40), // Light teal
    background = Color(0xFF0D1520), onBackground = Color(0xFFE8F4F8),
    surface = Color(0xFF152030), onSurface = Color(0xFFE8F4F8),
    surfaceVariant = Color(0xFF1F3040), onSurfaceVariant = Color(0xFF88CCEE),
    outline = Color(0xFF5599BB), error = Color(0xFFEE6655), onError = Color(0xFF550000)
)

// Protanopia (Red-weak) - Uses blue/yellow contrast, avoids red
val LightProtanopiaColorScheme = lightColorScheme(
    primary = Color(0xFF0077BB), onPrimary = Color.White, // Blue
    secondary = Color(0xFFDDBB00), onSecondary = Color(0xFF332200), // Yellow/Gold
    tertiary = Color(0xFF33BBEE), onTertiary = Color(0xFF003344), // Cyan
    background = Color(0xFFFFFDF5), onBackground = Color(0xFF1A1A2E),
    surface = Color.White, onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF5F0E0), onSurfaceVariant = Color(0xFF444400),
    outline = Color(0xFFBBAA33), error = Color(0xFF994400), onError = Color.White
)
val DarkProtanopiaColorScheme = darkColorScheme(
    primary = Color(0xFF77AADD), onPrimary = Color(0xFF002244), // Light blue
    secondary = Color(0xFFEEDD55), onSecondary = Color(0xFF443300), // Light yellow
    tertiary = Color(0xFF77DDEE), onTertiary = Color(0xFF004455), // Light cyan
    background = Color(0xFF12150D), onBackground = Color(0xFFF5F0E0),
    surface = Color(0xFF1A1F15), onSurface = Color(0xFFF5F0E0),
    surfaceVariant = Color(0xFF2A3020), onSurfaceVariant = Color(0xFFDDCC66),
    outline = Color(0xFF99AA44), error = Color(0xFFDD8833), onError = Color(0xFF331100)
)

// Tritanopia (Blue-yellow weakness) - Uses red/cyan contrast
val LightTritanopiaColorScheme = lightColorScheme(
    primary = Color(0xFFCC3366), onPrimary = Color.White, // Magenta/Pink
    secondary = Color(0xFF009999), onSecondary = Color.White, // Cyan/Teal
    tertiary = Color(0xFFEE6677), onTertiary = Color.White, // Coral
    background = Color(0xFFFFF5F5), onBackground = Color(0xFF2E1A1A),
    surface = Color.White, onSurface = Color(0xFF2E1A1A),
    surfaceVariant = Color(0xFFF8E8E8), onSurfaceVariant = Color(0xFF660033),
    outline = Color(0xFFDD88AA), error = Color(0xFF882255), onError = Color.White
)
val DarkTritanopiaColorScheme = darkColorScheme(
    primary = Color(0xFFEE99AA), onPrimary = Color(0xFF660033), // Light pink
    secondary = Color(0xFF66CCCC), onSecondary = Color(0xFF004444), // Light cyan
    tertiary = Color(0xFFFFAAAA), onTertiary = Color(0xFF662222), // Light coral
    background = Color(0xFF1A0D10), onBackground = Color(0xFFF8E8E8),
    surface = Color(0xFF251518), onSurface = Color(0xFFF8E8E8),
    surfaceVariant = Color(0xFF3A2028), onSurfaceVariant = Color(0xFFEE99AA),
    outline = Color(0xFFBB6688), error = Color(0xFFDD6699), onError = Color(0xFF330022)
)

// Monochromacy (Complete color blindness) - High contrast grayscale with texture differentiation
val LightMonochromacyColorScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A), onPrimary = Color.White, // Near black
    secondary = Color(0xFF666666), onSecondary = Color.White, // Medium gray
    tertiary = Color(0xFF333333), onTertiary = Color.White, // Dark gray
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF1A1A1A),
    surface = Color.White, onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE8E8E8), onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFFAAAAAA), error = Color(0xFF444444), onError = Color.White
)
val DarkMonochromacyColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0), onPrimary = Color(0xFF1A1A1A), // Near white
    secondary = Color(0xFFAAAAAA), onSecondary = Color(0xFF1A1A1A), // Light gray
    tertiary = Color(0xFFCCCCCC), onTertiary = Color(0xFF333333), // Lighter gray
    background = Color(0xFF121212), onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E), onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A), onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF666666), error = Color(0xFFBBBBBB), onError = Color(0xFF1A1A1A)
)

// --- BLIND / LOW VISION THEMES ---
// These themes are optimized for screen readers and users with very low or no vision

// Screen Reader Mode - Maximum contrast with clear semantic boundaries
// Uses pure black/white with bold accent colors for focus indicators
val LightScreenReaderColorScheme = lightColorScheme(
    primary = Color(0xFF0000FF), onPrimary = Color.White, // Pure blue for links/actions
    secondary = Color(0xFF006600), onSecondary = Color.White, // Green for success
    tertiary = Color(0xFF660066), onTertiary = Color.White, // Purple for emphasis
    background = Color.White, onBackground = Color.Black,
    surface = Color.White, onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0), onSurfaceVariant = Color.Black,
    outline = Color.Black, error = Color(0xFFCC0000), onError = Color.White
)
val DarkScreenReaderColorScheme = darkColorScheme(
    primary = Color(0xFF6699FF), onPrimary = Color.Black, // Bright blue for links/actions
    secondary = Color(0xFF66FF66), onSecondary = Color.Black, // Bright green for success
    tertiary = Color(0xFFFF66FF), onTertiary = Color.Black, // Bright purple for emphasis
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A), onSurfaceVariant = Color.White,
    outline = Color.White, error = Color(0xFFFF6666), onError = Color.Black
)

// Maximum Contrast - Pure black and white only, no grays
// For users with extremely low vision who need the highest possible contrast
val LightHighContrastBlindColorScheme = lightColorScheme(
    primary = Color.Black, onPrimary = Color.White,
    secondary = Color.Black, onSecondary = Color.White,
    tertiary = Color.Black, onTertiary = Color.White,
    background = Color.White, onBackground = Color.Black,
    surface = Color.White, onSurface = Color.Black,
    surfaceVariant = Color.White, onSurfaceVariant = Color.Black,
    outline = Color.Black, error = Color.Black, onError = Color.White
)
val DarkHighContrastBlindColorScheme = darkColorScheme(
    primary = Color.White, onPrimary = Color.Black,
    secondary = Color.White, onSecondary = Color.Black,
    tertiary = Color.White, onTertiary = Color.Black,
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White,
    surfaceVariant = Color.Black, onSurfaceVariant = Color.White,
    outline = Color.White, error = Color.White, onError = Color.Black
)

// Large Text Mode - Same as screen reader but with larger UI elements implied
// The actual text scaling is handled in the typography, colors here support readability
val LightLargeTextColorScheme = lightColorScheme(
    primary = Color(0xFF1A237E), onPrimary = Color.White, // Deep blue
    secondary = Color(0xFF2E7D32), onSecondary = Color.White, // Deep green
    tertiary = Color(0xFF6A1B9A), onTertiary = Color.White, // Deep purple
    background = Color(0xFFFFFDE7), onBackground = Color.Black, // Warm white, easier on eyes
    surface = Color(0xFFFFFDE7), onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5DC), onSurfaceVariant = Color(0xFF1A1A1A),
    outline = Color(0xFF333333), error = Color(0xFFB71C1C), onError = Color.White
)
val DarkLargeTextColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9), onPrimary = Color.Black, // Light blue
    secondary = Color(0xFFA5D6A7), onSecondary = Color.Black, // Light green
    tertiary = Color(0xFFCE93D8), onTertiary = Color.Black, // Light purple
    background = Color(0xFF1A1A0A), onBackground = Color(0xFFFFFDE7), // Dark warm
    surface = Color(0xFF252510), onSurface = Color(0xFFFFFDE7),
    surfaceVariant = Color(0xFF333320), onSurfaceVariant = Color(0xFFE0E0D0),
    outline = Color(0xFFCCCCBB), error = Color(0xFFEF9A9A), onError = Color.Black
)

// ============================================================================
// SECRET THEME: RAINBOW BRAIN 🦄
// A celebration of neurodivergent minds with vibrant, joyful colors
// ============================================================================

val LightRainbowBrainColorScheme = lightColorScheme(
    primary = Color(0xFF9C27B0), onPrimary = Color.White, // Vibrant purple (neurodiversity symbol)
    secondary = Color(0xFFFF6B9D), onSecondary = Color.White, // Playful pink
    tertiary = Color(0xFF00BCD4), onTertiary = Color.White, // Cyan accent
    background = Color(0xFFFFF8E1), onBackground = Color(0xFF37474F), // Warm cream
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF37474F),
    surfaceVariant = Color(0xFFE1BEE7), onSurfaceVariant = Color(0xFF4A148C), // Light purple variant
    primaryContainer = Color(0xFFE1BEE7), onPrimaryContainer = Color(0xFF4A148C),
    secondaryContainer = Color(0xFFF8BBD9), onSecondaryContainer = Color(0xFF880E4F),
    tertiaryContainer = Color(0xFFB2EBF2), onTertiaryContainer = Color(0xFF006064),
    outline = Color(0xFFBA68C8), error = Color(0xFFD32F2F), onError = Color.White
)

val DarkRainbowBrainColorScheme = darkColorScheme(
    primary = Color(0xFFCE93D8), onPrimary = Color.Black, // Soft purple
    secondary = Color(0xFFF48FB1), onSecondary = Color.Black, // Soft pink
    tertiary = Color(0xFF4DD0E1), onTertiary = Color.Black, // Cyan
    background = Color(0xFF1A1025), onBackground = Color(0xFFF3E5F5), // Deep purple-black
    surface = Color(0xFF2D1B3D), onSurface = Color(0xFFF3E5F5),
    surfaceVariant = Color(0xFF3D2952), onSurfaceVariant = Color(0xFFE1BEE7),
    primaryContainer = Color(0xFF4A148C), onPrimaryContainer = Color(0xFFE1BEE7),
    secondaryContainer = Color(0xFF880E4F), onSecondaryContainer = Color(0xFFF8BBD9),
    tertiaryContainer = Color(0xFF006064), onTertiaryContainer = Color(0xFFB2EBF2),
    outline = Color(0xFF9C27B0), error = Color(0xFFEF5350), onError = Color.Black
)

// ============================================================================
// THEME SELECTOR FUNCTIONS
// ============================================================================

fun getColorSchemeForState(state: NeuroState, isDarkMode: Boolean): ColorScheme {
    return when (state) {
        NeuroState.DEFAULT -> if (isDarkMode) DarkDefaultColorScheme else LightDefaultColorScheme
        NeuroState.HYPERFOCUS -> if (isDarkMode) DarkHyperfocusColorScheme else LightHyperfocusColorScheme
        NeuroState.OVERLOAD -> if (isDarkMode) DarkOverloadColorScheme else LightOverloadColorScheme
        NeuroState.CALM -> if (isDarkMode) DarkCalmColorScheme else LightCalmColorScheme

        NeuroState.ADHD_ENERGIZED -> if (isDarkMode) DarkADHDEnergizedColorScheme else LightADHDEnergizedColorScheme
        NeuroState.ADHD_LOW_DOPAMINE -> if (isDarkMode) DarkADHDLowDopamineColorScheme else LightADHDLowDopamineColorScheme
        NeuroState.ADHD_TASK_MODE -> if (isDarkMode) DarkADHDTaskModeColorScheme else LightADHDTaskModeColorScheme

        NeuroState.AUTISM_ROUTINE -> if (isDarkMode) DarkAutismRoutineColorScheme else LightAutismRoutineColorScheme
        NeuroState.AUTISM_SENSORY_SEEK -> if (isDarkMode) DarkAutismSensorySeekColorScheme else LightAutismSensorySeekColorScheme
        NeuroState.AUTISM_LOW_STIM -> if (isDarkMode) DarkAutismLowStimColorScheme else LightAutismLowStimColorScheme

        NeuroState.ANXIETY_SOOTHE -> if (isDarkMode) DarkAnxietySootheColorScheme else LightAnxietySootheColorScheme
        NeuroState.ANXIETY_GROUNDING -> if (isDarkMode) DarkAnxietyGroundingColorScheme else LightAnxietyGroundingColorScheme

        NeuroState.DYSLEXIA_FRIENDLY -> if (isDarkMode) DarkDyslexiaFriendlyColorScheme else LightDyslexiaFriendlyColorScheme

        NeuroState.COLORBLIND_DEUTERANOPIA -> if (isDarkMode) DarkDeuteranopiaColorScheme else LightDeuteranopiaColorScheme
        NeuroState.COLORBLIND_PROTANOPIA -> if (isDarkMode) DarkProtanopiaColorScheme else LightProtanopiaColorScheme
        NeuroState.COLORBLIND_TRITANOPIA -> if (isDarkMode) DarkTritanopiaColorScheme else LightTritanopiaColorScheme
        NeuroState.COLORBLIND_MONOCHROMACY -> if (isDarkMode) DarkMonochromacyColorScheme else LightMonochromacyColorScheme

        NeuroState.BLIND_SCREEN_READER -> if (isDarkMode) DarkScreenReaderColorScheme else LightScreenReaderColorScheme
        NeuroState.BLIND_HIGH_CONTRAST -> if (isDarkMode) DarkHighContrastBlindColorScheme else LightHighContrastBlindColorScheme
        NeuroState.BLIND_LARGE_TEXT -> if (isDarkMode) DarkLargeTextColorScheme else LightLargeTextColorScheme

        NeuroState.MOOD_TIRED -> if (isDarkMode) DarkMoodTiredColorScheme else LightMoodTiredColorScheme
        NeuroState.MOOD_ANXIOUS -> if (isDarkMode) DarkMoodAnxiousColorScheme else LightMoodAnxiousColorScheme
        NeuroState.MOOD_HAPPY -> if (isDarkMode) DarkMoodHappyColorScheme else LightMoodHappyColorScheme
        NeuroState.MOOD_OVERWHELMED -> if (isDarkMode) DarkMoodOverwhelmedColorScheme else LightMoodOverwhelmedColorScheme
        NeuroState.MOOD_CREATIVE -> if (isDarkMode) DarkMoodCreativeColorScheme else LightMoodCreativeColorScheme

        // Secret Theme
        NeuroState.RAINBOW_BRAIN -> if (isDarkMode) DarkRainbowBrainColorScheme else LightRainbowBrainColorScheme
    }
}

// Legacy functions for backward compatibility
@Deprecated("Use getColorSchemeForState(...) with NeuroThemeApplication")
fun defaultTheme(darkTheme: Boolean) = if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme

@Deprecated("Use getColorSchemeForState(...) with NeuroThemeApplication")
fun highContrastTheme(darkTheme: Boolean) = if (darkTheme) DarkHyperfocusColorScheme else LightHyperfocusColorScheme

@Deprecated("Use getColorSchemeForState(...) with NeuroThemeApplication")
fun quietModeTheme(darkTheme: Boolean) = if (darkTheme) DarkOverloadColorScheme else LightOverloadColorScheme

@Deprecated("Use getColorSchemeForState(...) with NeuroThemeApplication")
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

// --- Composable Theme Wrapper ---
@Composable
fun NeuroThemeApplication(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Determine the base color scheme
    var colorScheme: ColorScheme = when {
        // If dynamic colors are enabled and available, use system dynamic colors
        themeState.useDynamicColor &&
        themeState.colorSchemeSource == com.kyilmaz.neurocomet.ui.theme.ColorSchemeSource.DYNAMIC &&
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (themeState.isDarkMode) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        // Otherwise use the neurodivergent theme based on selected state
        else -> getColorSchemeForState(themeState.selectedState, themeState.isDarkMode)
    }

    if (themeState.isHighContrast) {
        colorScheme = highContrastOverlay(colorScheme, themeState.isDarkMode)
    }

    // Apply automatic text scaling for blind/accessibility themes
    val autoScaleFactor = when (themeState.selectedState) {
        NeuroState.BLIND_LARGE_TEXT -> 1.5f  // 50% larger text
        NeuroState.BLIND_SCREEN_READER -> 1.2f  // 20% larger for screen reader
        NeuroState.BLIND_HIGH_CONTRAST -> 1.3f  // 30% larger for high contrast
        NeuroState.DYSLEXIA_FRIENDLY -> 1.1f  // 10% larger for dyslexia
        else -> 1.0f
    }

    // Use the larger of user preference or auto-scale
    val effectiveScale = maxOf(themeState.textScaleFactor, autoScaleFactor)

    // Custom Typography scaled by the accessibility setting
    val typography = scaledTypography(effectiveScale)

    // Provide font settings via CompositionLocal
    androidx.compose.runtime.CompositionLocalProvider(
        LocalFontSettings provides themeState.fontSettings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}

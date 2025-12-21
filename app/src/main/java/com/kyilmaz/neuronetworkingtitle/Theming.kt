package com.kyilmaz.neuronetworkingtitle

import androidx.compose.ui.graphics.Color

// --- Theme Generation Data Structures ---

data class CustomPalette(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
)

enum class Mood {
    CALM, EXCITED, ANXIOUS, FOCUSED, TIRED
}

enum class Neurotype {
    GENERAL, AUTISM, ADHD
}

// --- Theme Generation Logic ---

/**
 * Generates a custom color palette optimized for neurodivergence and current mood.
 */
fun generateCustomPalette(mood: Mood, neurotype: Neurotype): CustomPalette {
    // Colors are chosen to be generally desaturated and low-contrast to prevent visual stress,
    // but with specific hues adjusted for mood and neurotype.

    val basePrimary = when (mood) {
        Mood.CALM -> Color(0xFF5A8D8F) // Desaturated Teal/Cyan
        Mood.EXCITED -> Color(0xFF9C775F) // Warm, grounding brown/orange
        Mood.ANXIOUS -> Color(0xFF6F797B) // Cool, neutral gray to reduce visual noise
        Mood.FOCUSED -> Color(0xFF5A6F9C) // Deep, steady blue
        Mood.TIRED -> Color(0xFF837F8F) // Muted lavender/gray
    }

    val primary = basePrimary
    val secondary = basePrimary.copy(alpha = 0.7f)
    val onSurface = Color(0xFF1F1F1F) // Dark text

    // Adjusting contrast based on neurotype for background layers
    val (bgColor, surfaceColor) = when (neurotype) {
        Neurotype.AUTISM -> {
            // Very low contrast, minimal visual separation
            Color(0xFFF7F7F7) to Color(0xFFFFFFFF)
        }
        Neurotype.ADHD -> {
            // Slight separation to aid focus on separate elements
            Color(0xFFF0F5FA) to Color(0xFFFFFBF0) // Subtle blue/cream contrast
        }
        Neurotype.GENERAL -> {
            // Slightly more visual interest
            Color(0xFFE8F0F3) to Color(0xFFF5FFFF)
        }
    }

    return CustomPalette(
        primary = primary,
        secondary = secondary,
        background = bgColor,
        surface = surfaceColor,
        onSurface = onSurface
    )
}

// --- Daily Log Data ---

data class DailyLog(
    val mood: Mood = Mood.CALM,
    val neurotype: Neurotype = Neurotype.GENERAL
)
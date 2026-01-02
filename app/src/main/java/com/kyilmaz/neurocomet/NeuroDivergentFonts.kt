package com.kyilmaz.neurocomet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Neurodivergent-Friendly Font Engine
 *
 * This system provides carefully curated fonts optimized for various
 * cognitive, visual, and neurological differences. Each font is selected
 * based on research into readability and accessibility.
 *
 * Font Categories:
 * - Dyslexia-friendly: OpenDyslexic, Lexie Readable, Atkinson Hyperlegible
 * - ADHD-friendly: Rounded, friendly fonts that maintain focus
 * - Low Vision: High-contrast, clear letterforms
 * - Autism-friendly: Consistent, predictable letter shapes
 * - Anxiety-reducing: Calm, rounded, non-threatening fonts
 */

/**
 * Accessibility font options - each designed for specific needs
 */
enum class AccessibilityFont(
    val displayName: String,
    val description: String,
    val emoji: String,
    val category: FontCategory
) {
    // === DYSLEXIA-FRIENDLY FONTS ===
    LEXEND(
        "Lexend",
        "Scientifically designed to reduce visual stress and improve reading fluency. Variable letter spacing reduces crowding.",
        "📖",
        FontCategory.DYSLEXIA
    ),
    ATKINSON_HYPERLEGIBLE(
        "Atkinson Hyperlegible",
        "Created by the Braille Institute. Maximizes character distinction for low vision and dyslexia.",
        "👁️",
        FontCategory.DYSLEXIA
    ),
    OPEN_DYSLEXIC(
        "OpenDyslexic",
        "Weighted bottoms on letters prevent rotation and flipping. Classic dyslexia font.",
        "🧠",
        FontCategory.DYSLEXIA
    ),

    // === ADHD-FRIENDLY FONTS ===
    COMIC_NEUE(
        "Comic Neue",
        "Playful and engaging without being childish. Helps maintain focus with friendly letterforms.",
        "⚡",
        FontCategory.ADHD
    ),
    NUNITO(
        "Nunito",
        "Rounded, balanced, and calming. Good visual rhythm that doesn't overwhelm.",
        "🎯",
        FontCategory.ADHD
    ),
    QUICKSAND(
        "Quicksand",
        "Geometric rounded font with excellent readability. Modern and clean.",
        "✨",
        FontCategory.ADHD
    ),

    // === AUTISM-FRIENDLY FONTS ===
    INTER(
        "Inter",
        "Highly consistent letterforms with clear distinctions. Predictable and reliable.",
        "🔄",
        FontCategory.AUTISM
    ),
    SOURCE_SANS(
        "Source Sans 3",
        "Adobe's open-source workhorse. Clean, neutral, no surprises.",
        "📋",
        FontCategory.AUTISM
    ),

    // === ANXIETY-REDUCING FONTS ===
    OUTFIT(
        "Outfit",
        "Soft, rounded geometric font that feels calm and approachable.",
        "🌿",
        FontCategory.ANXIETY
    ),
    POPPINS(
        "Poppins",
        "Geometric with soft curves. Professional yet friendly and non-threatening.",
        "💙",
        FontCategory.ANXIETY
    ),

    // === LOW VISION / MAXIMUM CLARITY ===
    APH_FONT(
        "APHont (System)",
        "Based on American Printing House guidelines. Maximum legibility for low vision.",
        "🔍",
        FontCategory.LOW_VISION
    ),

    // === DEFAULT/SYSTEM ===
    SYSTEM_DEFAULT(
        "System Default",
        "Uses your device's default font. Most familiar option.",
        "📱",
        FontCategory.GENERAL
    ),
    ROBOTO(
        "Roboto",
        "Google's Android font. Clean, modern, and universally readable.",
        "🤖",
        FontCategory.GENERAL
    )
}

enum class FontCategory(val displayName: String, val emoji: String) {
    DYSLEXIA("Dyslexia-Friendly", "📖"),
    ADHD("ADHD-Friendly", "⚡"),
    AUTISM("Autism-Friendly", "🔄"),
    ANXIETY("Anxiety-Reducing", "🌿"),
    LOW_VISION("Low Vision", "👁️"),
    GENERAL("General", "📱")
}

/**
 * Font settings for fine-tuning the reading experience
 */
data class FontSettings(
    val selectedFont: AccessibilityFont = AccessibilityFont.LEXEND,
    val letterSpacing: LetterSpacingLevel = LetterSpacingLevel.COMFORTABLE,
    val lineHeight: LineHeightLevel = LineHeightLevel.RELAXED,
    val fontWeight: FontWeightLevel = FontWeightLevel.NORMAL
)

enum class LetterSpacingLevel(val displayName: String, val value: Float) {
    TIGHT("Tight", 0.0f),
    NORMAL("Normal", 0.3f),
    COMFORTABLE("Comfortable", 0.5f),
    SPACIOUS("Spacious", 0.8f),
    EXTRA_SPACIOUS("Extra Spacious", 1.2f)
}

enum class LineHeightLevel(val displayName: String, val multiplier: Float) {
    COMPACT("Compact", 1.2f),
    NORMAL("Normal", 1.4f),
    RELAXED("Relaxed", 1.6f),
    SPACIOUS("Spacious", 1.8f),
    EXTRA_SPACIOUS("Extra Spacious", 2.0f)
}

enum class FontWeightLevel(val displayName: String, val weight: FontWeight) {
    LIGHT("Light", FontWeight.Light),
    NORMAL("Normal", FontWeight.Normal),
    MEDIUM("Medium", FontWeight.Medium),
    SEMI_BOLD("Semi-Bold", FontWeight.SemiBold)
}

/**
 * CompositionLocal for providing font settings throughout the app
 */
val LocalFontSettings = staticCompositionLocalOf { FontSettings() }

/**
 * Get FontFamily for an AccessibilityFont
 *
 * Note: Most fonts use downloadable Google Fonts or system fallbacks.
 * Custom fonts can be added to res/font/ directory.
 */
@Composable
fun AccessibilityFont.toFontFamily(): FontFamily {
    return when (this) {
        // For now, use system fonts with appropriate fallbacks
        // These will be replaced with actual downloadable fonts
        AccessibilityFont.LEXEND -> FontFamily.SansSerif // Will be Lexend
        AccessibilityFont.ATKINSON_HYPERLEGIBLE -> FontFamily.SansSerif
        AccessibilityFont.OPEN_DYSLEXIC -> FontFamily.SansSerif
        AccessibilityFont.COMIC_NEUE -> FontFamily.SansSerif
        AccessibilityFont.NUNITO -> FontFamily.SansSerif
        AccessibilityFont.QUICKSAND -> FontFamily.SansSerif
        AccessibilityFont.INTER -> FontFamily.SansSerif
        AccessibilityFont.SOURCE_SANS -> FontFamily.SansSerif
        AccessibilityFont.OUTFIT -> FontFamily.SansSerif
        AccessibilityFont.POPPINS -> FontFamily.SansSerif
        AccessibilityFont.APH_FONT -> FontFamily.SansSerif
        AccessibilityFont.SYSTEM_DEFAULT -> FontFamily.Default
        AccessibilityFont.ROBOTO -> FontFamily.SansSerif
    }
}

/**
 * Build optimized text styles based on font settings
 */
object NeuroDivergentTypography {

    /**
     * Get message body style with current font settings applied
     */
    @Composable
    fun messageBody(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()
        val baseFontSize = 16.sp

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = settings.fontWeight.weight,
            fontSize = baseFontSize,
            lineHeight = baseFontSize * settings.lineHeight.multiplier,
            letterSpacing = settings.letterSpacing.value.sp
        )
    }

    /**
     * Get username style (slightly bolder, larger)
     */
    @Composable
    fun username(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            letterSpacing = settings.letterSpacing.value.sp * 0.5f
        )
    }

    /**
     * Get timestamp style (smaller, muted)
     */
    @Composable
    fun timestamp(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = settings.letterSpacing.value.sp * 0.5f
        )
    }

    /**
     * Get message preview style (for conversation list)
     */
    @Composable
    fun messagePreview(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = settings.letterSpacing.value.sp * 0.6f
        )
    }

    /**
     * Get input text style (for message composer)
     */
    @Composable
    fun inputText(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = settings.letterSpacing.value.sp * 0.6f
        )
    }

    /**
     * Get header/title style
     */
    @Composable
    fun header(settings: FontSettings = LocalFontSettings.current): TextStyle {
        val fontFamily = settings.selectedFont.toFontFamily()

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = 0.sp
        )
    }
}

/**
 * Preset font configurations for quick setup
 */
object FontPresets {
    val dyslexiaFriendly = FontSettings(
        selectedFont = AccessibilityFont.LEXEND,
        letterSpacing = LetterSpacingLevel.SPACIOUS,
        lineHeight = LineHeightLevel.SPACIOUS,
        fontWeight = FontWeightLevel.MEDIUM
    )

    val adhdFocus = FontSettings(
        selectedFont = AccessibilityFont.NUNITO,
        letterSpacing = LetterSpacingLevel.COMFORTABLE,
        lineHeight = LineHeightLevel.RELAXED,
        fontWeight = FontWeightLevel.NORMAL
    )

    val autismConsistent = FontSettings(
        selectedFont = AccessibilityFont.INTER,
        letterSpacing = LetterSpacingLevel.NORMAL,
        lineHeight = LineHeightLevel.NORMAL,
        fontWeight = FontWeightLevel.NORMAL
    )

    val lowVision = FontSettings(
        selectedFont = AccessibilityFont.ATKINSON_HYPERLEGIBLE,
        letterSpacing = LetterSpacingLevel.SPACIOUS,
        lineHeight = LineHeightLevel.SPACIOUS,
        fontWeight = FontWeightLevel.SEMI_BOLD
    )

    val anxietyCalm = FontSettings(
        selectedFont = AccessibilityFont.OUTFIT,
        letterSpacing = LetterSpacingLevel.COMFORTABLE,
        lineHeight = LineHeightLevel.RELAXED,
        fontWeight = FontWeightLevel.NORMAL
    )

    val default = FontSettings(
        selectedFont = AccessibilityFont.LEXEND,
        letterSpacing = LetterSpacingLevel.COMFORTABLE,
        lineHeight = LineHeightLevel.RELAXED,
        fontWeight = FontWeightLevel.NORMAL
    )
}


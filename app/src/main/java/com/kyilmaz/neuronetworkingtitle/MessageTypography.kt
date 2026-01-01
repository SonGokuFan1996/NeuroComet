package com.kyilmaz.neuronetworkingtitle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Neurodivergent-friendly typography for Messages.
 *
 * @deprecated This file is deprecated. Use [NeuroDivergentFonts.kt] instead, which provides:
 * - [AccessibilityFont] enum with fonts for dyslexia, ADHD, autism, anxiety, low vision
 * - [FontSettings] data class for letter spacing, line height, font weight
 * - [NeuroDivergentTypography] object for getting styled TextStyles
 * - [FontPresets] object for quick preset configurations
 * - [LocalFontSettings] CompositionLocal for providing settings throughout the app
 *
 * The new system is integrated with ThemeViewModel and provides a full UI in FontSettingsScreen.
 *
 * Uses typography settings that are:
 * - Easier to read for people with dyslexia
 * - More visually interesting with better letter spacing
 * - Comfortable for extended reading
 * - Rounded, friendly appearance
 *
 * Key improvements over default:
 * - Larger base font size (16sp vs 14sp)
 * - Increased line height for better readability
 * - Slightly increased letter spacing for clarity
 * - Softer font weights
 */

/**
 * Message-specific text styles with neurodivergent-friendly settings.
 * Uses default system fonts but with optimized sizing and spacing.
 */
object MessageTextStyles {

    // Using SansSerif as it's cleaner and more readable
    // The key improvements are in sizing, spacing, and weight
    private val messageFontFamily = FontFamily.SansSerif

    // Message bubble text - slightly larger with excellent spacing
    val messageBody = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,  // 1.5x line height for better readability
        letterSpacing = 0.4.sp  // Slightly wider for clarity
    )

    // Sent message style
    val sentMessage = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp
    )

    // Received message style
    val receivedMessage = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp
    )

    // Timestamp style - smaller but still very readable
    val timestamp = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.3.sp
    )

    // Username in conversation list - prominent and clear
    val username = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.2.sp
    )

    // Preview text in conversation list
    val messagePreview = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    )

    // Input field text - comfortable for typing
    val inputText = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp
    )

    // Emoji reactions - slightly larger for visibility
    val emojiReaction = TextStyle(
        fontSize = 20.sp
    )

    // Header/title text - clear and friendly
    val headerTitle = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    )

    // Status text (typing, online, etc.)
    val statusText = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp
    )

    // System message style - for things like "Message blocked"
    val systemMessage = TextStyle(
        fontFamily = messageFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    )
}

/**
 * Composition local for message typography preference
 */
enum class MessageFontPreference {
    DEFAULT,     // System sans-serif with ND-friendly settings
    MONOSPACE,   // Monospace for tech-savvy users
    SERIF        // Serif for traditional readers
}

val LocalMessageFontPreference = staticCompositionLocalOf { MessageFontPreference.DEFAULT }

/**
 * Get the appropriate font family based on user preference
 */
@Composable
fun getMessageFontFamily(preference: MessageFontPreference = LocalMessageFontPreference.current): FontFamily {
    return when (preference) {
        MessageFontPreference.DEFAULT -> FontFamily.SansSerif
        MessageFontPreference.MONOSPACE -> FontFamily.Monospace
        MessageFontPreference.SERIF -> FontFamily.Serif
    }
}


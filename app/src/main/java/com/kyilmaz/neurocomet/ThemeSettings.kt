package com.kyilmaz.neurocomet

import android.content.Context
import androidx.core.content.edit
import com.kyilmaz.neurocomet.ui.theme.ColorSchemeSource

private const val PREFS = "theme_settings"
private const val KEY_DARK = "dark"
private const val KEY_HIGH_CONTRAST = "high_contrast"
private const val KEY_TEXT_SCALE = "text_scale"
private const val KEY_STATE = "neuro_state"
private const val KEY_LANGUAGE = "language_code"
private const val KEY_STORY_ANIMATION = "story_animation_enabled"
private const val KEY_RAINBOW_BRAIN_UNLOCKED = "rainbow_brain_unlocked"
private const val KEY_DYNAMIC_COLOR = "dynamic_color_enabled"
private const val KEY_COLOR_SCHEME_SOURCE = "color_scheme_source"

// Animation settings keys
private const val KEY_ANIM_DISABLE_ALL = "anim_disable_all"
private const val KEY_ANIM_DISABLE_LOGO = "anim_disable_logo"
private const val KEY_ANIM_DISABLE_STORY = "anim_disable_story"
private const val KEY_ANIM_DISABLE_FEED = "anim_disable_feed"
private const val KEY_ANIM_DISABLE_TRANSITION = "anim_disable_transition"
private const val KEY_ANIM_DISABLE_BUTTON = "anim_disable_button"
private const val KEY_ANIM_DISABLE_LOADING = "anim_disable_loading"

// Font settings keys
private const val KEY_FONT_SELECTED = "font_selected"
private const val KEY_FONT_LETTER_SPACING = "font_letter_spacing"
private const val KEY_FONT_LINE_HEIGHT = "font_line_height"
private const val KEY_FONT_WEIGHT = "font_weight"

object ThemeSettings {
    fun get(context: Context): ThemeState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedState = prefs.getString(KEY_STATE, NeuroState.DEFAULT.name)
        val selectedState = runCatching { NeuroState.valueOf(storedState ?: NeuroState.DEFAULT.name) }
            .getOrDefault(NeuroState.DEFAULT)

        val storedColorSource = prefs.getString(KEY_COLOR_SCHEME_SOURCE, ColorSchemeSource.DYNAMIC.name)
        val colorSchemeSource = runCatching { ColorSchemeSource.valueOf(storedColorSource ?: ColorSchemeSource.DYNAMIC.name) }
            .getOrDefault(ColorSchemeSource.DYNAMIC)

        val animationSettings = AnimationSettings(
            disableAllAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_ALL, false),
            disableLogoAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_LOGO, false),
            disableStoryAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_STORY, false),
            disableFeedAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_FEED, false),
            disableTransitionAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_TRANSITION, false),
            disableButtonAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_BUTTON, false),
            disableLoadingAnimations = prefs.getBoolean(KEY_ANIM_DISABLE_LOADING, false)
        )

        // Load font settings
        val storedFont = prefs.getString(KEY_FONT_SELECTED, AccessibilityFont.LEXEND.name)
        val selectedFont = runCatching { AccessibilityFont.valueOf(storedFont ?: AccessibilityFont.LEXEND.name) }
            .getOrDefault(AccessibilityFont.LEXEND)

        val storedLetterSpacing = prefs.getString(KEY_FONT_LETTER_SPACING, LetterSpacingLevel.COMFORTABLE.name)
        val letterSpacing = runCatching { LetterSpacingLevel.valueOf(storedLetterSpacing ?: LetterSpacingLevel.COMFORTABLE.name) }
            .getOrDefault(LetterSpacingLevel.COMFORTABLE)

        val storedLineHeight = prefs.getString(KEY_FONT_LINE_HEIGHT, LineHeightLevel.RELAXED.name)
        val lineHeight = runCatching { LineHeightLevel.valueOf(storedLineHeight ?: LineHeightLevel.RELAXED.name) }
            .getOrDefault(LineHeightLevel.RELAXED)

        val storedFontWeight = prefs.getString(KEY_FONT_WEIGHT, FontWeightLevel.NORMAL.name)
        val fontWeight = runCatching { FontWeightLevel.valueOf(storedFontWeight ?: FontWeightLevel.NORMAL.name) }
            .getOrDefault(FontWeightLevel.NORMAL)

        val fontSettings = FontSettings(
            selectedFont = selectedFont,
            letterSpacing = letterSpacing,
            lineHeight = lineHeight,
            fontWeight = fontWeight
        )

        return ThemeState(
            isDarkMode = prefs.getBoolean(KEY_DARK, false),
            isHighContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false),
            textScaleFactor = prefs.getFloat(KEY_TEXT_SCALE, 1.0f),
            selectedState = selectedState,
            languageCode = prefs.getString(KEY_LANGUAGE, "") ?: "",
            storyAnimationEnabled = prefs.getBoolean(KEY_STORY_ANIMATION, true),
            rainbowBrainUnlocked = prefs.getBoolean(KEY_RAINBOW_BRAIN_UNLOCKED, false),
            animationSettings = animationSettings,
            useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            colorSchemeSource = colorSchemeSource,
            fontSettings = fontSettings
        )
    }

    fun save(context: Context, state: ThemeState) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_DARK, state.isDarkMode)
            putBoolean(KEY_HIGH_CONTRAST, state.isHighContrast)
            putFloat(KEY_TEXT_SCALE, state.textScaleFactor)
            putString(KEY_STATE, state.selectedState.name)
            putString(KEY_LANGUAGE, state.languageCode)
            putBoolean(KEY_STORY_ANIMATION, state.storyAnimationEnabled)
            putBoolean(KEY_RAINBOW_BRAIN_UNLOCKED, state.rainbowBrainUnlocked)
            putBoolean(KEY_DYNAMIC_COLOR, state.useDynamicColor)
            putString(KEY_COLOR_SCHEME_SOURCE, state.colorSchemeSource.name)

            // Animation settings
            putBoolean(KEY_ANIM_DISABLE_ALL, state.animationSettings.disableAllAnimations)
            putBoolean(KEY_ANIM_DISABLE_LOGO, state.animationSettings.disableLogoAnimations)
            putBoolean(KEY_ANIM_DISABLE_STORY, state.animationSettings.disableStoryAnimations)
            putBoolean(KEY_ANIM_DISABLE_FEED, state.animationSettings.disableFeedAnimations)
            putBoolean(KEY_ANIM_DISABLE_TRANSITION, state.animationSettings.disableTransitionAnimations)
            putBoolean(KEY_ANIM_DISABLE_BUTTON, state.animationSettings.disableButtonAnimations)
            putBoolean(KEY_ANIM_DISABLE_LOADING, state.animationSettings.disableLoadingAnimations)

            // Font settings
            putString(KEY_FONT_SELECTED, state.fontSettings.selectedFont.name)
            putString(KEY_FONT_LETTER_SPACING, state.fontSettings.letterSpacing.name)
            putString(KEY_FONT_LINE_HEIGHT, state.fontSettings.lineHeight.name)
            putString(KEY_FONT_WEIGHT, state.fontSettings.fontWeight.name)
        }
    }
}

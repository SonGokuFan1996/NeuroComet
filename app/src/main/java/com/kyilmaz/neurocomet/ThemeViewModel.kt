package com.kyilmaz.neurocomet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Explicit imports for symbols needed from other files in the package
import com.kyilmaz.neurocomet.NeuroState

/**
 * Animation settings for neurodivergent-friendly experience.
 * Some users are sensitive to animations and prefer a calmer UI.
 */
data class AnimationSettings(
    val disableAllAnimations: Boolean = false, // Master toggle
    val disableLogoAnimations: Boolean = false, // Rainbow infinity, shimmer text
    val disableStoryAnimations: Boolean = false, // Story circle spinning
    val disableFeedAnimations: Boolean = false, // Post card animations, like hearts
    val disableTransitionAnimations: Boolean = false, // Screen transitions
    val disableButtonAnimations: Boolean = false, // Button press effects
    val disableLoadingAnimations: Boolean = false // Loading spinners, skeleton screens
) {
    /**
     * Check if a specific animation type should be shown.
     * Returns false if master toggle is on OR individual toggle is on.
     */
    fun shouldAnimate(type: AnimationType): Boolean {
        if (disableAllAnimations) return false
        return when (type) {
            AnimationType.LOGO -> !disableLogoAnimations
            AnimationType.STORY -> !disableStoryAnimations
            AnimationType.FEED -> !disableFeedAnimations
            AnimationType.TRANSITION -> !disableTransitionAnimations
            AnimationType.BUTTON -> !disableButtonAnimations
            AnimationType.LOADING -> !disableLoadingAnimations
        }
    }
}

enum class AnimationType {
    LOGO,       // Rainbow infinity, shimmer text
    STORY,      // Story circle spinning
    FEED,       // Post card animations
    TRANSITION, // Screen transitions
    BUTTON,     // Button press effects
    LOADING     // Loading spinners
}

data class ThemeState(
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false,
    val textScaleFactor: Float = 1.0f,
    val selectedState: NeuroState = NeuroState.DEFAULT,
    val languageCode: String = "", // ISO 639-1 code
    val storyAnimationEnabled: Boolean = true, // Legacy - use animationSettings instead
    val rainbowBrainUnlocked: Boolean = false, // Secret easter egg theme unlock status
    val animationSettings: AnimationSettings = AnimationSettings(),
    val useDynamicColor: Boolean = true, // Use Material 3 dynamic colors (Android 12+)
    val colorSchemeSource: com.kyilmaz.neurocomet.ui.theme.ColorSchemeSource =
        com.kyilmaz.neurocomet.ui.theme.ColorSchemeSource.DYNAMIC,
    val fontSettings: FontSettings = FontSettings() // Neurodivergent-friendly font settings
) {
    // Convenience method to check if animations should play
    fun shouldAnimate(type: AnimationType): Boolean = animationSettings.shouldAnimate(type)
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val _themeState = MutableStateFlow(ThemeSettings.get(appContext))
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    private fun persist(state: ThemeState) {
        ThemeSettings.save(appContext, state)
    }

    fun setDarkMode(isDarkMode: Boolean) {
        val newState = _themeState.value.copy(isDarkMode = isDarkMode)
        _themeState.value = newState
        persist(newState)
    }

    fun setIsHighContrast(isHighContrast: Boolean) {
        val newState = _themeState.value.copy(isHighContrast = isHighContrast)
        _themeState.value = newState
        persist(newState)
    }

    fun setTextScaleFactor(factor: Float) {
        val newState = _themeState.value.copy(textScaleFactor = factor)
        _themeState.value = newState
        persist(newState)
    }

    fun setSelectedState(state: NeuroState) {
        val newState = _themeState.value.copy(selectedState = state)
        _themeState.value = newState
        persist(newState)
    }

    fun setLanguageCode(code: String) {
        val newState = _themeState.value.copy(languageCode = code)
        _themeState.value = newState
        persist(newState)
    }

    fun setStoryAnimationEnabled(enabled: Boolean) {
        val newState = _themeState.value.copy(storyAnimationEnabled = enabled)
        _themeState.value = newState
        persist(newState)
    }

    fun unlockRainbowBrain() {
        val newState = _themeState.value.copy(rainbowBrainUnlocked = true)
        _themeState.value = newState
        persist(newState)
    }

    // === Dynamic Color Settings ===

    fun setUseDynamicColor(enabled: Boolean) {
        val newState = _themeState.value.copy(useDynamicColor = enabled)
        _themeState.value = newState
        persist(newState)
    }

    fun setColorSchemeSource(source: com.kyilmaz.neurocomet.ui.theme.ColorSchemeSource) {
        val newState = _themeState.value.copy(colorSchemeSource = source)
        _themeState.value = newState
        persist(newState)
    }

    // === Animation Settings ===

    fun setDisableAllAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableAllAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableLogoAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableLogoAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableStoryAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableStoryAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableFeedAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableFeedAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableTransitionAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableTransitionAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableButtonAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableButtonAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setDisableLoadingAnimations(disabled: Boolean) {
        val current = _themeState.value
        val newState = current.copy(
            animationSettings = current.animationSettings.copy(disableLoadingAnimations = disabled)
        )
        _themeState.value = newState
        persist(newState)
    }

    // === Font Settings ===

    fun setFontSettings(settings: FontSettings) {
        val newState = _themeState.value.copy(fontSettings = settings)
        _themeState.value = newState
        persist(newState)
    }

    fun setSelectedFont(font: AccessibilityFont) {
        val current = _themeState.value
        val newState = current.copy(
            fontSettings = current.fontSettings.copy(selectedFont = font)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setLetterSpacing(spacing: LetterSpacingLevel) {
        val current = _themeState.value
        val newState = current.copy(
            fontSettings = current.fontSettings.copy(letterSpacing = spacing)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setLineHeight(height: LineHeightLevel) {
        val current = _themeState.value
        val newState = current.copy(
            fontSettings = current.fontSettings.copy(lineHeight = height)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun setFontWeight(weight: FontWeightLevel) {
        val current = _themeState.value
        val newState = current.copy(
            fontSettings = current.fontSettings.copy(fontWeight = weight)
        )
        _themeState.value = newState
        persist(newState)
    }

    fun applyFontPreset(preset: FontSettings) {
        val newState = _themeState.value.copy(fontSettings = preset)
        _themeState.value = newState
        persist(newState)
    }
}

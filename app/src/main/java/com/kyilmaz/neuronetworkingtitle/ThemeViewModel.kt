package com.kyilmaz.neuronetworkingtitle

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NeuroState {
    DEFAULT,
    HYPERFOCUS, // High Contrast, Sharp Focus
    OVERLOAD,   // Low Contrast, Quiet Mode
    CALM        // Soft, Soothing Tones
}

// Map from a descriptive size to a scale factor
val TEXT_SCALE_FACTORS = mapOf(
    "Small" to 0.8f,
    "Medium" to 1.0f,
    "Large" to 1.2f,
    "X-Large" to 1.5f
)

// List of supported locales (code -> display name)
val SUPPORTED_LOCALES = mapOf(
    "" to "System Default",
    "en" to "English (US/Default)",
    "en-GB" to "English (British)",
    "en-CA" to "English (Canadian)",
    "en-AU" to "English (Aus/NZ)",
    "fr" to "Français",
    "es" to "Español",
    "sv" to "Svenska",
    "nl" to "Nederlands",
    "tr" to "Türkçe",
    "ar" to "العربية"
)

@Immutable
data class ThemeState(
    val selectedState: NeuroState = NeuroState.DEFAULT,
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false,
    val textScaleFactor: Float = 1.0f, // Added for accessibility
    val languageCode: String = "" // ISO 639-1 code + optional region, or "" for system default
)

class ThemeViewModel : ViewModel() {
    private val _themeState = MutableStateFlow(ThemeState())
    val themeState = _themeState.asStateFlow()

    fun setNeuroState(state: NeuroState) {
        _themeState.update { it.copy(selectedState = state) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _themeState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleHighContrast(enabled: Boolean) {
        _themeState.update { it.copy(isHighContrast = enabled) }
    }
    
    fun setTextScaleFactor(scale: Float) {
        _themeState.update { it.copy(textScaleFactor = scale) }
    }

    fun setLanguageCode(code: String) {
        _themeState.update { it.copy(languageCode = code) }
    }
}
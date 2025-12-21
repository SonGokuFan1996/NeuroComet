package com.kyilmaz.neuronetworkingtitle

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class NeuroState(val label: String, val seedColor: Color, val description: String) {
    DEFAULT("NeuroNet Standard", Color(0xFF6750A4), "The classic look."),
    OVERSTIMULATED("Sensory Soothe", Color(0xFF546E7A), "Deep, muted blue-greys to reduce visual noise."), // Blue Grey 600
    UNDERSTIMULATED("Dopamine Boost", Color(0xFFFF6D00), "High-energy orange to wake up the brain."), // Orange A700
    ANXIETY("Grounding", Color(0xFF2E7D32), "Natural forest tones to feel safe and stable."), // Green 800
    FOCUS("Hyperfocus", Color(0xFF283593), "Deep indigo for minimizing distraction."), // Indigo 800
    MELTDOWN("Safe Space", Color(0xFFAD1457), "Warm, comforting rose tones for recovery.") // Pink 800
}

@Immutable
data class ThemeState(
    val selectedState: NeuroState = NeuroState.DEFAULT,
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false
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
}

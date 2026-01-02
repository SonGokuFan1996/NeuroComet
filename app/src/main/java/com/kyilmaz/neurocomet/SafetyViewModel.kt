package com.kyilmaz.neurocomet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Explicit imports for symbols needed from other files in the package
import com.kyilmaz.neurocomet.DevOptionsSettings

// --- Data Models (Consolidated from what was likely in SafetyModels.kt) ---

/**
 * App-level safety audience.
 */
enum class Audience {
    UNDER_13,
    TEEN,
    ADULT
}

/**
 * Controls the strength of filtering in UNDER_13 mode.
 */
enum class KidsFilterLevel {
    STRICT, // hide/sanitize more aggressively
    MODERATE
}

data class SafetyState(
    val audience: Audience = Audience.ADULT,
    val kidsFilterLevel: KidsFilterLevel = KidsFilterLevel.STRICT,
    val isParentalPinSet: Boolean = false
) {
    val isKidsMode: Boolean get() = audience == Audience.UNDER_13
}
// --- ViewModel Implementation ---

class SafetyViewModel : ViewModel() {
    private val _state = MutableStateFlow(SafetyState())
    val state: StateFlow<SafetyState> = _state.asStateFlow()

    fun refresh(application: Application) {
        viewModelScope.launch {
            val devOptions = DevOptionsSettings.get(application)
            val parentalState = ParentalControlsSettings.getState(application)

            // Override audience/filter level if Dev Options are set
            _state.update { currentState ->
                currentState.copy(
                    audience = devOptions.forceAudience ?: currentState.audience,
                    kidsFilterLevel = devOptions.forceKidsFilterLevel ?: currentState.kidsFilterLevel,
                    isParentalPinSet = devOptions.forcePinSet || parentalState.isPinSet
                )
            }
        }
    }

    // Mutator function used by SettingsScreen
    fun setAudience(audience: Audience, application: Application) {
        // Only allow changes if not forced by dev options, or if setting back to ADULT
        _state.update { it.copy(
            audience = audience, 
            kidsFilterLevel = if (audience == Audience.UNDER_13) KidsFilterLevel.STRICT else KidsFilterLevel.MODERATE) 
        }
    }

    // Allow setting audience directly without Application for age verification flow.
    fun setAudienceDirect(audience: Audience) {
        _state.update { it.copy(audience = audience, kidsFilterLevel = if (audience == Audience.UNDER_13) KidsFilterLevel.STRICT else KidsFilterLevel.MODERATE) }
    }
}

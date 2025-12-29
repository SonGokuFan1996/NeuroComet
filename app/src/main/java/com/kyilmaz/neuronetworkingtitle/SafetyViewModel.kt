package com.kyilmaz.neuronetworkingtitle

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SafetyViewModel : ViewModel() {
    private val _state = MutableStateFlow(SafetyState())
    val state: StateFlow<SafetyState> = _state.asStateFlow()

    fun refresh(application: Application) {
        viewModelScope.launch {
            val devOptions = DevOptionsSettings.get(application)

            // Override audience/filter level if Dev Options are set
            _state.update { currentState ->
                currentState.copy(
                    audience = devOptions.forceAudience ?: currentState.audience,
                    kidsFilterLevel = devOptions.forceKidsFilterLevel ?: currentState.kidsFilterLevel,
                    isParentalPinSet = devOptions.forcePinSet || currentState.isParentalPinSet
                )
            }
        }
    }

    // Example of a mutator function used by SettingsScreen
    fun setAudience(audience: Audience, application: Application) {
        // This setter logic is simplified for demo purposes.
        // In a real app, changing the audience to UNDER_13 would involve PIN verification.
        _state.update { it.copy(audience = audience, kidsFilterLevel = if (audience == Audience.UNDER_13) KidsFilterLevel.STRICT else KidsFilterLevel.MODERATE) }
    }
}
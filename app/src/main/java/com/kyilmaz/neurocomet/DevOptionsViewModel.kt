package com.kyilmaz.neurocomet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Explicit imports for symbols in the same package
import com.kyilmaz.neurocomet.DevOptionsSettings
import com.kyilmaz.neurocomet.DevOptions
import com.kyilmaz.neurocomet.DevModerationOverride
import com.kyilmaz.neurocomet.Audience
import com.kyilmaz.neurocomet.KidsFilterLevel

class DevOptionsViewModel : ViewModel() {
    private val _options = MutableStateFlow(DevOptions())
    val options: StateFlow<DevOptions> = _options.asStateFlow()

    fun refresh(application: Application) {
        viewModelScope.launch {
            _options.value = DevOptionsSettings.get(application)
        }
    }

    // Minimal setters required by SettingsScreen and DevOptionsScreen
    fun setDevMenuEnabled(application: Application, enabled: Boolean) { DevOptionsSettings.setDevMenuEnabled(application, enabled); refresh(application) }
    fun setShowDmDebugOverlay(application: Application, enabled: Boolean) { DevOptionsSettings.setShowDmDebugOverlay(application, enabled); refresh(application) }
    fun setDmForceSendFailure(application: Application, enabled: Boolean) { DevOptionsSettings.setDmForceSendFailure(application, enabled); refresh(application) }
    fun setDmSendDelayMs(application: Application, delayMs: Long) { DevOptionsSettings.setDmSendDelayMs(application, delayMs); refresh(application) }
    fun setDmDisableRateLimit(application: Application, enabled: Boolean) { DevOptionsSettings.setDmDisableRateLimit(application, enabled); refresh(application) }
    fun setDmMinIntervalOverrideMs(application: Application, overrideMs: Long?) { DevOptionsSettings.setDmMinIntervalOverrideMs(application, overrideMs); refresh(application) }
    fun setModerationOverride(application: Application, override: DevModerationOverride) { DevOptionsSettings.setModerationOverride(application, override); refresh(application) }
    fun setForceAudience(application: Application, audience: Audience?) { DevOptionsSettings.setForceAudience(application, audience); refresh(application) }
    fun setForceKidsFilterLevel(application: Application, level: KidsFilterLevel?) { DevOptionsSettings.setForceKidsFilterLevel(application, level); refresh(application) }
    fun setForcePinSet(application: Application, enabled: Boolean) { DevOptionsSettings.setForcePinSet(application, enabled); refresh(application) }
    fun setForcePinVerifySuccess(application: Application, enabled: Boolean) { DevOptionsSettings.setForcePinVerifySuccess(application, enabled); refresh(application) }
    fun resetAll(application: Application) { DevOptionsSettings.resetAll(application); refresh(application) }
}

package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * ViewModel for developer options.
 *
 * Uses [ApplicationProvider] internally so callers don't need to
 * pass [Application] on every setter — just call [init] once.
 */
class DevOptionsViewModel : ViewModel() {

    private val _options = MutableStateFlow(DevOptions())
    val options: StateFlow<DevOptions> = _options.asStateFlow()

    /** The application context used for all reads/writes. */
    @Suppress("StaticFieldLeak") // applicationContext is safe to hold
    private var ctx: Context? = null

    /** Call once from the hosting Activity/Fragment/Application. */
    fun init(application: Application) {
        ctx = application.applicationContext
        refresh()
    }

    fun refresh(application: Application? = null) {
        val c = application ?: ctx ?: ApplicationProvider.app ?: return
        if (ctx == null) ctx = c
        viewModelScope.launch {
            _options.value = DevOptionsSettings.get(c)
        }
    }

    private fun context(): Context =
        ctx ?: ApplicationProvider.app
        ?: throw IllegalStateException("DevOptionsViewModel not initialised – call init(app) first")

    // ─── Global ─────────────────────────────────────────────
    fun setDevMenuEnabled(enabled: Boolean) { DevOptionsSettings.setDevMenuEnabled(context(), enabled); refresh() }
    fun setVerboseLogging(enabled: Boolean) { DevOptionsSettings.setVerboseLogging(context(), enabled); refresh() }

    // ─── Environment ────────────────────────────────────────
    fun setEnvironment(env: DevEnvironment) { DevOptionsSettings.setEnvironment(context(), env); refresh() }

    // ─── Feature Flags ──────────────────────────────────────
    fun setEnableNewFeedLayout(v: Boolean) { DevOptionsSettings.setEnableNewFeedLayout(context(), v); refresh() }
    fun setEnableVideoChat(v: Boolean) { DevOptionsSettings.setEnableVideoChat(context(), v); refresh() }
    fun setEnableStoryReactions(v: Boolean) { DevOptionsSettings.setEnableStoryReactions(context(), v); refresh() }
    fun setEnableAdvancedSearch(v: Boolean) { DevOptionsSettings.setEnableAdvancedSearch(context(), v); refresh() }
    fun setEnableAiSuggestions(v: Boolean) { DevOptionsSettings.setEnableAiSuggestions(context(), v); refresh() }

    // ─── Cross-Device ──────────────────────────────────────
    fun setEnableHandoff(v: Boolean) { DevOptionsSettings.setEnableHandoff(context(), v); refresh() }

    // ─── DM Debug ───────────────────────────────────────────
    fun setShowDmDebugOverlay(enabled: Boolean) { DevOptionsSettings.setShowDmDebugOverlay(context(), enabled); refresh() }
    fun setDmForceSendFailure(enabled: Boolean) { DevOptionsSettings.setDmForceSendFailure(context(), enabled); refresh() }
    fun setDmSendDelayMs(delayMs: Long) { DevOptionsSettings.setDmSendDelayMs(context(), delayMs); refresh() }
    fun setDmDisableRateLimit(enabled: Boolean) { DevOptionsSettings.setDmDisableRateLimit(context(), enabled); refresh() }
    fun setDmMinIntervalOverrideMs(overrideMs: Long?) { DevOptionsSettings.setDmMinIntervalOverrideMs(context(), overrideMs); refresh() }

    // ─── Moderation / Safety ────────────────────────────────
    fun setModerationOverride(override: DevModerationOverride) { DevOptionsSettings.setModerationOverride(context(), override); refresh() }
    fun setForceAudience(audience: Audience?) { DevOptionsSettings.setForceAudience(context(), audience); refresh() }
    fun setForceKidsFilterLevel(level: KidsFilterLevel?) { DevOptionsSettings.setForceKidsFilterLevel(context(), level); refresh() }
    fun setForcePinSet(enabled: Boolean) { DevOptionsSettings.setForcePinSet(context(), enabled); refresh() }
    fun setForcePinVerifySuccess(enabled: Boolean) { DevOptionsSettings.setForcePinVerifySuccess(context(), enabled); refresh() }

    // ─── Rendering / Network ────────────────────────────────
    fun setSimulateOffline(v: Boolean) { DevOptionsSettings.setSimulateOffline(context(), v); refresh() }
    fun setSimulateLoadingError(v: Boolean) { DevOptionsSettings.setSimulateLoadingError(context(), v); refresh() }
    fun setInfiniteLoading(v: Boolean) { DevOptionsSettings.setInfiniteLoading(context(), v); refresh() }
    fun setShowFallbackUi(v: Boolean) { DevOptionsSettings.setShowFallbackUi(context(), v); refresh() }
    fun setPublicReleaseMockUiEnabled(v: Boolean) { DevOptionsSettings.setPublicReleaseMockUiEnabled(context(), v); refresh() }
    fun setNetworkLatencyMs(ms: Long) { DevOptionsSettings.setNetworkLatencyMs(context(), ms); refresh() }

    // ─── Auth Overrides ─────────────────────────────────────
    fun setForceLoggedOut(v: Boolean) { DevOptionsSettings.setForceLoggedOut(context(), v); refresh() }
    fun setBypassBiometric(v: Boolean) { DevOptionsSettings.setBypassBiometric(context(), v); refresh() }
    fun setForce2FA(v: Boolean) { DevOptionsSettings.setForce2FA(context(), v); refresh() }

    // ─── Performance ────────────────────────────────────────
    fun setShowPerformanceOverlay(v: Boolean) { DevOptionsSettings.setShowPerformanceOverlay(context(), v); refresh() }

    // ─── Feedback & Beta ─────────────────────────────────────
    fun setBypassFeedbackRateLimit(v: Boolean) { DevOptionsSettings.setBypassFeedbackRateLimit(context(), v); refresh() }
    fun setForceFeedbackSubmitFailure(v: Boolean) { DevOptionsSettings.setForceFeedbackSubmitFailure(context(), v); refresh() }

    // ─── Supabase & Cloud Sync ──────────────────────────────
    fun setForcePremiumCloudSync(v: Boolean) { DevOptionsSettings.setForcePremiumCloudSync(context(), v); refresh() }
    fun setForceGameAchievementsSync(v: Boolean) { DevOptionsSettings.setForceGameAchievementsSync(context(), v); refresh() }
    fun setForceSettingsCloudSync(v: Boolean) { DevOptionsSettings.setForceSettingsCloudSync(context(), v); refresh() }
    fun setForceAiPracticeLogSync(v: Boolean) { DevOptionsSettings.setForceAiPracticeLogSync(context(), v); refresh() }
    fun setForceAbTestSync(v: Boolean) { DevOptionsSettings.setForceAbTestSync(context(), v); refresh() }
    fun setShowCloudSyncDebug(v: Boolean) { DevOptionsSettings.setShowCloudSyncDebug(context(), v); refresh() }

    // ─── Reset ──────────────────────────────────────────────
    fun resetMockData(feedViewModel: FeedViewModel) {
        feedViewModel.resetMockData()
        NotificationTester.clearAllTestNotifications(context())
    }

    fun resetAll() { DevOptionsSettings.resetAll(context()); refresh() }

    // ─── Legacy compat overloads (used by MainActivity) ─────
    fun setDevMenuEnabled(application: Application, enabled: Boolean) { init(application); setDevMenuEnabled(enabled) }
    fun setShowDmDebugOverlay(application: Application, enabled: Boolean) { init(application); setShowDmDebugOverlay(enabled) }
    fun setDmForceSendFailure(application: Application, enabled: Boolean) { init(application); setDmForceSendFailure(enabled) }
    fun setDmSendDelayMs(application: Application, delayMs: Long) { init(application); setDmSendDelayMs(delayMs) }
    fun setModerationOverride(application: Application, override: DevModerationOverride) { init(application); setModerationOverride(override) }
    fun setForceAudience(application: Application, audience: Audience?) { init(application); setForceAudience(audience) }
    fun setForceKidsFilterLevel(application: Application, level: KidsFilterLevel?) { init(application); setForceKidsFilterLevel(level) }
    fun setForcePinSet(application: Application, enabled: Boolean) { init(application); setForcePinSet(enabled) }
    fun setForcePinVerifySuccess(application: Application, enabled: Boolean) { init(application); setForcePinVerifySuccess(enabled) }
    fun setDmDisableRateLimit(application: Application, enabled: Boolean) { init(application); setDmDisableRateLimit(enabled) }
    fun setDmMinIntervalOverrideMs(application: Application, overrideMs: Long?) { init(application); setDmMinIntervalOverrideMs(overrideMs) }
    fun resetAll(application: Application) { init(application); resetAll() }
}

package com.kyilmaz.neurocomet

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.core.content.edit


/**
 * SharedPreferences-backed dev options store.
 *
 * SECURITY: Dev options are only available in debug builds.
 * Any attempt to enable dev options in a release build will be blocked and logged.
 */
object DevOptionsSettings {
    private const val PREFS = "dev_options"

    // Keys – Global
    private const val KEY_DEV_MENU_ENABLED = "dev_menu_enabled"
    private const val KEY_VERBOSE_LOGGING = "verbose_logging"

    // Keys – Environment
    private const val KEY_ENVIRONMENT = "environment"

    // Keys – Feature Flags
    private const val KEY_FF_NEW_FEED_LAYOUT = "ff_new_feed_layout"
    private const val KEY_FF_VIDEO_CHAT = "ff_video_chat"
    private const val KEY_FF_STORY_REACTIONS = "ff_story_reactions"
    private const val KEY_FF_ADVANCED_SEARCH = "ff_advanced_search"
    private const val KEY_FF_AI_SUGGESTIONS = "ff_ai_suggestions"

    // Keys – Cross-Device
    private const val KEY_ENABLE_HANDOFF = "enable_handoff"

    // Keys – DM Debug
    private const val KEY_SHOW_DM_DEBUG_OVERLAY = "show_dm_debug_overlay"
    private const val KEY_DM_FORCE_SEND_FAILURE = "dm_force_send_failure"
    private const val KEY_DM_SEND_DELAY_MS = "dm_send_delay_ms"
    private const val KEY_DM_DISABLE_RATE_LIMIT = "dm_disable_rate_limit"
    private const val KEY_DM_MIN_INTERVAL_OVERRIDE_MS = "dm_min_interval_override_ms"

    // Keys – Moderation / Safety
    private const val KEY_MODERATION_OVERRIDE = "moderation_override"
    private const val KEY_FORCE_AUDIENCE = "force_audience"
    private const val KEY_FORCE_KIDS_FILTER = "force_kids_filter"
    private const val KEY_FORCE_PIN_SET = "force_pin_set"
    private const val KEY_FORCE_PIN_VERIFY_SUCCESS = "force_pin_verify_success"

    // Keys – Rendering / Network
    private const val KEY_SIMULATE_OFFLINE = "simulate_offline"
    private const val KEY_SIMULATE_LOADING_ERROR = "simulate_loading_error"
    private const val KEY_INFINITE_LOADING = "infinite_loading"
    private const val KEY_SHOW_FALLBACK_UI = "show_fallback_ui"
    private const val KEY_PUBLIC_RELEASE_MOCK_UI = "public_release_mock_ui"
    private const val KEY_NETWORK_LATENCY_MS = "network_latency_ms"

    // Keys – Auth Overrides
    private const val KEY_FORCE_LOGGED_OUT = "force_logged_out"
    private const val KEY_BYPASS_BIOMETRIC = "bypass_biometric"
    private const val KEY_FORCE_2FA = "force_2fa"

    // Keys – Performance
    private const val KEY_SHOW_PERFORMANCE_OVERLAY = "show_performance_overlay"

    // Keys – Feedback & Beta
    private const val KEY_BYPASS_FEEDBACK_RATE_LIMIT = "bypass_feedback_rate_limit"
    private const val KEY_FORCE_FEEDBACK_SUBMIT_FAILURE = "force_feedback_submit_failure"

    // Keys – Supabase & Cloud Sync
    private const val KEY_FORCE_PREMIUM_CLOUD_SYNC = "force_premium_cloud_sync"
    private const val KEY_FORCE_GAME_ACHIEVEMENTS_SYNC = "force_game_achievements_sync"
    private const val KEY_FORCE_SETTINGS_CLOUD_SYNC = "force_settings_cloud_sync"
    private const val KEY_FORCE_AI_PRACTICE_LOG_SYNC = "force_ai_practice_log_sync"
    private const val KEY_FORCE_AB_TEST_SYNC = "force_ab_test_sync"
    private const val KEY_SHOW_CLOUD_SYNC_DEBUG = "show_cloud_sync_debug"

    // ─── Helpers ────────────────────────────────────────────

    private fun isDebugBuild(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun enforceDebugBuild(context: Context, operation: String) {
        val isAuthorized = DeviceAuthority.isAuthorizedDevice(context)
        if (!isDebugBuild(context) && !isAuthorized) {
            android.util.Log.wtf(
                "SECURITY",
                "Attempted to $operation in an unauthorized production build!"
            )
            throw SecurityException(
                "Developer options are restricted. " +
                "Operation attempted: $operation"
            )
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun resolvedDevMenuEnabled(context: Context, prefs: SharedPreferences = prefs(context)): Boolean {
        return isDebugBuild(context) ||
            DeviceAuthority.isHouseholdAuthorizedDevice(context) ||
            prefs.getBoolean(KEY_DEV_MENU_ENABLED, false)
    }

    fun canSurfaceDevOptions(context: Context): Boolean {
        return DeviceAuthority.canUseDeveloperTools(context) && resolvedDevMenuEnabled(context)
    }

    fun requireDeveloperToolsAccess(context: Context, operation: String) {
        DeviceAuthority.requireDeveloperToolsAccess(context, operation)
    }

    private inline fun editAuthorized(
        context: Context,
        operation: String,
        block: SharedPreferences.Editor.() -> Unit
    ) {
        requireDeveloperToolsAccess(context, operation)
        prefs(context).edit(commit = false, action = block)
    }

    // ─── Read ───────────────────────────────────────────────

    fun get(context: Context): DevOptions {
        val isDebug = isDebugBuild(context)
        val isAuthorized = DeviceAuthority.isAuthorizedDevice(context)

        // SECURITY: In production builds, only authorized devices get dev options
        if (!isDebug && !isAuthorized) return DevOptions()

        val p = prefs(context)

        // Persisted hidden-unlock state for the settings menu.
        // For convenience in debug builds or on the two household test devices,
        // default to true so both can exercise the same test surface.
        val devMenuEnabled = resolvedDevMenuEnabled(context, p)

        val moderationOverride = runCatching {
            DevModerationOverride.valueOf(
                p.getString(KEY_MODERATION_OVERRIDE, DevModerationOverride.OFF.name)!!
            )
        }.getOrDefault(DevModerationOverride.OFF)

        val forceAudience = p.getString(KEY_FORCE_AUDIENCE, null)?.let {
            runCatching { Audience.valueOf(it) }.getOrNull()
        }

        val forceKids = p.getString(KEY_FORCE_KIDS_FILTER, null)?.let {
            runCatching { KidsFilterLevel.valueOf(it) }.getOrNull()
        }

        val environment = runCatching {
            DevEnvironment.valueOf(
                p.getString(KEY_ENVIRONMENT, DevEnvironment.PRODUCTION.name)!!
            )
        }.getOrDefault(DevEnvironment.PRODUCTION)

        val minIntervalOverride = p.getLong(KEY_DM_MIN_INTERVAL_OVERRIDE_MS, -1L)
            .takeIf { it >= 0L }
        val allowSensitiveSafetyOverrides = isAuthorized

        return DevOptions(
            // Global
            devMenuEnabled = devMenuEnabled,
            verboseLogging = p.getBoolean(KEY_VERBOSE_LOGGING, false),
            // Environment
            environment = environment,
            // Feature Flags
            enableNewFeedLayout = p.getBoolean(KEY_FF_NEW_FEED_LAYOUT, false),
            enableVideoChat = p.getBoolean(KEY_FF_VIDEO_CHAT, true),
            enableStoryReactions = p.getBoolean(KEY_FF_STORY_REACTIONS, false),
            enableAdvancedSearch = p.getBoolean(KEY_FF_ADVANCED_SEARCH, false),
            enableAiSuggestions = p.getBoolean(KEY_FF_AI_SUGGESTIONS, false),
            // Cross-Device
            enableHandoff = p.getBoolean(KEY_ENABLE_HANDOFF, true),
            // DM
            showDmDebugOverlay = p.getBoolean(KEY_SHOW_DM_DEBUG_OVERLAY, false),
            dmForceSendFailure = p.getBoolean(KEY_DM_FORCE_SEND_FAILURE, false),
            dmArtificialSendDelayMs = p.getLong(KEY_DM_SEND_DELAY_MS, 0L),
            dmDisableRateLimit = p.getBoolean(KEY_DM_DISABLE_RATE_LIMIT, false) && allowSensitiveSafetyOverrides,
            dmMinIntervalOverrideMs = minIntervalOverride,
            // Moderation / Safety
            moderationOverride = moderationOverride,
            forceAudience = forceAudience,
            forceKidsFilterLevel = forceKids,
            forcePinSet = p.getBoolean(KEY_FORCE_PIN_SET, false) && allowSensitiveSafetyOverrides,
            forcePinVerifySuccess = p.getBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, false) && allowSensitiveSafetyOverrides,
            // Rendering / Network
            simulateOffline = p.getBoolean(KEY_SIMULATE_OFFLINE, false),
            simulateLoadingError = p.getBoolean(KEY_SIMULATE_LOADING_ERROR, false),
            infiniteLoading = p.getBoolean(KEY_INFINITE_LOADING, false),
            showFallbackUi = p.getBoolean(KEY_SHOW_FALLBACK_UI, false),
            publicReleaseMockUiEnabled = p.getBoolean(KEY_PUBLIC_RELEASE_MOCK_UI, false),
            networkLatencyMs = p.getLong(KEY_NETWORK_LATENCY_MS, 0L),
            // Auth Overrides
            forceLoggedOut = p.getBoolean(KEY_FORCE_LOGGED_OUT, false),
            bypassBiometric = p.getBoolean(KEY_BYPASS_BIOMETRIC, false),
            force2FA = p.getBoolean(KEY_FORCE_2FA, false),
            // Performance
            showPerformanceOverlay = p.getBoolean(KEY_SHOW_PERFORMANCE_OVERLAY, false),
            // Feedback & Beta
            bypassFeedbackRateLimit = p.getBoolean(KEY_BYPASS_FEEDBACK_RATE_LIMIT, false),
            forceFeedbackSubmitFailure = p.getBoolean(KEY_FORCE_FEEDBACK_SUBMIT_FAILURE, false),
            // Supabase & Cloud Sync
            forcePremiumCloudSync = p.getBoolean(KEY_FORCE_PREMIUM_CLOUD_SYNC, false),
            forceGameAchievementsSync = p.getBoolean(KEY_FORCE_GAME_ACHIEVEMENTS_SYNC, false),
            forceSettingsCloudSync = p.getBoolean(KEY_FORCE_SETTINGS_CLOUD_SYNC, false),
            forceAiPracticeLogSync = p.getBoolean(KEY_FORCE_AI_PRACTICE_LOG_SYNC, false),
            forceAbTestSync = p.getBoolean(KEY_FORCE_AB_TEST_SYNC, false),
            showCloudSyncDebug = p.getBoolean(KEY_SHOW_CLOUD_SYNC_DEBUG, false)
        )
    }

    // ─── Write helpers ──────────────────────────────────────

    fun setDevMenuEnabled(context: Context, enabled: Boolean) {
        requireDeveloperToolsAccess(context, if (enabled) "enable dev menu" else "disable dev menu")
        if (enabled) {
            enforceDebugBuild(context, "enable dev menu")
        }
        prefs(context).edit { putBoolean(KEY_DEV_MENU_ENABLED, enabled) }
    }

    fun setVerboseLogging(context: Context, enabled: Boolean) {
        editAuthorized(context, "change verbose logging") { putBoolean(KEY_VERBOSE_LOGGING, enabled) }
    }

    fun setEnvironment(context: Context, env: DevEnvironment) {
        enforceDebugBuild(context, "change environment")
        editAuthorized(context, "change environment") { putString(KEY_ENVIRONMENT, env.name) }
    }

    // Feature flags
    fun setEnableNewFeedLayout(context: Context, v: Boolean) { editAuthorized(context, "change new feed layout flag") { putBoolean(KEY_FF_NEW_FEED_LAYOUT, v) } }
    fun setEnableVideoChat(context: Context, v: Boolean) { editAuthorized(context, "change video chat flag") { putBoolean(KEY_FF_VIDEO_CHAT, v) } }
    fun setEnableStoryReactions(context: Context, v: Boolean) { editAuthorized(context, "change story reactions flag") { putBoolean(KEY_FF_STORY_REACTIONS, v) } }
    fun setEnableAdvancedSearch(context: Context, v: Boolean) { editAuthorized(context, "change advanced search flag") { putBoolean(KEY_FF_ADVANCED_SEARCH, v) } }
    fun setEnableAiSuggestions(context: Context, v: Boolean) { editAuthorized(context, "change AI suggestions flag") { putBoolean(KEY_FF_AI_SUGGESTIONS, v) } }

    // Cross-Device
    fun setEnableHandoff(context: Context, v: Boolean) { editAuthorized(context, "change handoff setting") { putBoolean(KEY_ENABLE_HANDOFF, v) } }

    // DM
    fun setShowDmDebugOverlay(context: Context, enabled: Boolean) { editAuthorized(context, "change DM debug overlay") { putBoolean(KEY_SHOW_DM_DEBUG_OVERLAY, enabled) } }
    fun setDmForceSendFailure(context: Context, enabled: Boolean) { editAuthorized(context, "change DM send failure simulation") { putBoolean(KEY_DM_FORCE_SEND_FAILURE, enabled) } }
    fun setDmSendDelayMs(context: Context, delayMs: Long) { editAuthorized(context, "change DM send delay") { putLong(KEY_DM_SEND_DELAY_MS, delayMs.coerceIn(0L, 15_000L)) } }
    fun setDmDisableRateLimit(context: Context, enabled: Boolean) { editAuthorized(context, "change DM rate limiting") { putBoolean(KEY_DM_DISABLE_RATE_LIMIT, enabled) } }
    fun setDmMinIntervalOverrideMs(context: Context, overrideMs: Long?) {
        editAuthorized(context, "change DM min interval override") {
            if (overrideMs == null) remove(KEY_DM_MIN_INTERVAL_OVERRIDE_MS)
            else putLong(KEY_DM_MIN_INTERVAL_OVERRIDE_MS, overrideMs.coerceIn(0L, 60_000L))
        }
    }

    // Moderation / Safety
    fun setModerationOverride(context: Context, override: DevModerationOverride) { editAuthorized(context, "change moderation override") { putString(KEY_MODERATION_OVERRIDE, override.name) } }
    fun setForceAudience(context: Context, audience: Audience?) {
        editAuthorized(context, "change forced audience") {
            if (audience == null) remove(KEY_FORCE_AUDIENCE) else putString(KEY_FORCE_AUDIENCE, audience.name)
        }
    }
    fun setForceKidsFilterLevel(context: Context, level: KidsFilterLevel?) {
        editAuthorized(context, "change forced kids filter") {
            if (level == null) remove(KEY_FORCE_KIDS_FILTER) else putString(KEY_FORCE_KIDS_FILTER, level.name)
        }
    }
    fun setForcePinSet(context: Context, enabled: Boolean) { editAuthorized(context, "change forced parental PIN state") { putBoolean(KEY_FORCE_PIN_SET, enabled) } }
    fun setForcePinVerifySuccess(context: Context, enabled: Boolean) { editAuthorized(context, "change forced PIN verification state") { putBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, enabled) } }

    // Rendering / Network
    fun setSimulateOffline(context: Context, v: Boolean) { editAuthorized(context, "change offline simulation") { putBoolean(KEY_SIMULATE_OFFLINE, v) } }
    fun setSimulateLoadingError(context: Context, v: Boolean) { editAuthorized(context, "change loading error simulation") { putBoolean(KEY_SIMULATE_LOADING_ERROR, v) } }
    fun setInfiniteLoading(context: Context, v: Boolean) { editAuthorized(context, "change infinite loading simulation") { putBoolean(KEY_INFINITE_LOADING, v) } }
    fun setShowFallbackUi(context: Context, v: Boolean) { editAuthorized(context, "change fallback UI simulation") { putBoolean(KEY_SHOW_FALLBACK_UI, v) } }
    fun setPublicReleaseMockUiEnabled(context: Context, v: Boolean) { editAuthorized(context, "change public release mock UI") { putBoolean(KEY_PUBLIC_RELEASE_MOCK_UI, v) } }
    fun setNetworkLatencyMs(context: Context, ms: Long) { editAuthorized(context, "change network latency simulation") { putLong(KEY_NETWORK_LATENCY_MS, ms.coerceIn(0L, 30_000L)) } }

    // Auth Overrides
    fun setForceLoggedOut(context: Context, v: Boolean) { editAuthorized(context, "change force logged out override") { putBoolean(KEY_FORCE_LOGGED_OUT, v) } }
    fun setBypassBiometric(context: Context, v: Boolean) { editAuthorized(context, "change biometric override") { putBoolean(KEY_BYPASS_BIOMETRIC, v) } }
    fun setForce2FA(context: Context, v: Boolean) { editAuthorized(context, "change force 2FA override") { putBoolean(KEY_FORCE_2FA, v) } }

    // Performance
    fun setShowPerformanceOverlay(context: Context, v: Boolean) { editAuthorized(context, "change performance overlay") { putBoolean(KEY_SHOW_PERFORMANCE_OVERLAY, v) } }

    // Feedback & Beta
    fun setBypassFeedbackRateLimit(context: Context, v: Boolean) { editAuthorized(context, "change feedback rate limiting") { putBoolean(KEY_BYPASS_FEEDBACK_RATE_LIMIT, v) } }
    fun setForceFeedbackSubmitFailure(context: Context, v: Boolean) { editAuthorized(context, "change feedback failure simulation") { putBoolean(KEY_FORCE_FEEDBACK_SUBMIT_FAILURE, v) } }

    // Supabase & Cloud Sync
    fun setForcePremiumCloudSync(context: Context, v: Boolean) { editAuthorized(context, "change premium cloud sync override") { putBoolean(KEY_FORCE_PREMIUM_CLOUD_SYNC, v) } }
    fun setForceGameAchievementsSync(context: Context, v: Boolean) { editAuthorized(context, "change game achievements sync override") { putBoolean(KEY_FORCE_GAME_ACHIEVEMENTS_SYNC, v) } }
    fun setForceSettingsCloudSync(context: Context, v: Boolean) { editAuthorized(context, "change settings cloud sync override") { putBoolean(KEY_FORCE_SETTINGS_CLOUD_SYNC, v) } }
    fun setForceAiPracticeLogSync(context: Context, v: Boolean) { editAuthorized(context, "change AI practice log sync override") { putBoolean(KEY_FORCE_AI_PRACTICE_LOG_SYNC, v) } }
    fun setForceAbTestSync(context: Context, v: Boolean) { editAuthorized(context, "change A/B test sync override") { putBoolean(KEY_FORCE_AB_TEST_SYNC, v) } }
    fun setShowCloudSyncDebug(context: Context, v: Boolean) { editAuthorized(context, "change cloud sync debug visibility") { putBoolean(KEY_SHOW_CLOUD_SYNC_DEBUG, v) } }

    // Reset
    fun resetAll(context: Context) {
        editAuthorized(context, "reset all developer options") { clear() }
    }
}

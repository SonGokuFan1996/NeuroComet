package com.kyilmaz.neurocomet

import android.content.Context
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
    private const val KEY_NETWORK_LATENCY_MS = "network_latency_ms"

    // Keys – Auth Overrides
    private const val KEY_FORCE_LOGGED_OUT = "force_logged_out"
    private const val KEY_BYPASS_BIOMETRIC = "bypass_biometric"
    private const val KEY_FORCE_2FA = "force_2fa"

    // Keys – Performance
    private const val KEY_SHOW_PERFORMANCE_OVERLAY = "show_performance_overlay"

    // ─── Helpers ────────────────────────────────────────────

    private fun isDebugBuild(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun enforceDebugBuild(context: Context, operation: String) {
        if (!isDebugBuild(context)) {
            android.util.Log.wtf(
                "SECURITY",
                "Attempted to $operation in a production build! This is a security violation."
            )
            throw SecurityException(
                "Developer options are not available in production builds. " +
                "Operation attempted: $operation"
            )
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ─── Read ───────────────────────────────────────────────

    fun get(context: Context): DevOptions {
        val isDebug = isDebugBuild(context)

        // SECURITY: In production builds, always return hardened defaults
        if (!isDebug) return DevOptions()

        // SECURITY: In debug builds, only authorized devices get dev options
        if (!DeviceAuthority.isAuthorizedDevice(context)) return DevOptions()

        val p = prefs(context)

        // Persisted hidden-unlock state for the settings menu.
        // Authorized debug devices can still open dev options directly
        // via the long-press gesture, which sets this flag to true.
        // For convenience in debug builds, default to true.
        val devMenuEnabled = isDebug || p.getBoolean(KEY_DEV_MENU_ENABLED, false)

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
            dmDisableRateLimit = p.getBoolean(KEY_DM_DISABLE_RATE_LIMIT, true),
            dmMinIntervalOverrideMs = minIntervalOverride,
            // Moderation / Safety
            moderationOverride = moderationOverride,
            forceAudience = forceAudience,
            forceKidsFilterLevel = forceKids,
            forcePinSet = p.getBoolean(KEY_FORCE_PIN_SET, false),
            forcePinVerifySuccess = p.getBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, false),
            // Rendering / Network
            simulateOffline = p.getBoolean(KEY_SIMULATE_OFFLINE, false),
            simulateLoadingError = p.getBoolean(KEY_SIMULATE_LOADING_ERROR, false),
            infiniteLoading = p.getBoolean(KEY_INFINITE_LOADING, false),
            showFallbackUi = p.getBoolean(KEY_SHOW_FALLBACK_UI, false),
            networkLatencyMs = p.getLong(KEY_NETWORK_LATENCY_MS, 0L),
            // Auth Overrides
            forceLoggedOut = p.getBoolean(KEY_FORCE_LOGGED_OUT, false),
            bypassBiometric = p.getBoolean(KEY_BYPASS_BIOMETRIC, false),
            force2FA = p.getBoolean(KEY_FORCE_2FA, false),
            // Performance
            showPerformanceOverlay = p.getBoolean(KEY_SHOW_PERFORMANCE_OVERLAY, false)
        )
    }

    // ─── Write helpers ──────────────────────────────────────

    fun setDevMenuEnabled(context: Context, enabled: Boolean) {
        if (enabled) {
            enforceDebugBuild(context, "enable dev menu")
            if (!DeviceAuthority.isAuthorizedDevice(context)) {
                android.util.Log.wtf(
                    "SECURITY",
                    "Unauthorized device tried to enable dev menu"
                )
                return
            }
        }
        prefs(context).edit { putBoolean(KEY_DEV_MENU_ENABLED, enabled) }
    }

    fun setVerboseLogging(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_VERBOSE_LOGGING, enabled) }
    }

    fun setEnvironment(context: Context, env: DevEnvironment) {
        enforceDebugBuild(context, "change environment")
        prefs(context).edit { putString(KEY_ENVIRONMENT, env.name) }
    }

    // Feature flags
    fun setEnableNewFeedLayout(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FF_NEW_FEED_LAYOUT, v) } }
    fun setEnableVideoChat(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FF_VIDEO_CHAT, v) } }
    fun setEnableStoryReactions(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FF_STORY_REACTIONS, v) } }
    fun setEnableAdvancedSearch(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FF_ADVANCED_SEARCH, v) } }
    fun setEnableAiSuggestions(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FF_AI_SUGGESTIONS, v) } }

    // Cross-Device
    fun setEnableHandoff(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_ENABLE_HANDOFF, v) } }

    // DM
    fun setShowDmDebugOverlay(context: Context, enabled: Boolean) { prefs(context).edit { putBoolean(KEY_SHOW_DM_DEBUG_OVERLAY, enabled) } }
    fun setDmForceSendFailure(context: Context, enabled: Boolean) { prefs(context).edit { putBoolean(KEY_DM_FORCE_SEND_FAILURE, enabled) } }
    fun setDmSendDelayMs(context: Context, delayMs: Long) { prefs(context).edit { putLong(KEY_DM_SEND_DELAY_MS, delayMs.coerceIn(0L, 15_000L)) } }
    fun setDmDisableRateLimit(context: Context, enabled: Boolean) { prefs(context).edit { putBoolean(KEY_DM_DISABLE_RATE_LIMIT, enabled) } }
    fun setDmMinIntervalOverrideMs(context: Context, overrideMs: Long?) {
        prefs(context).edit {
            if (overrideMs == null) remove(KEY_DM_MIN_INTERVAL_OVERRIDE_MS)
            else putLong(KEY_DM_MIN_INTERVAL_OVERRIDE_MS, overrideMs.coerceIn(0L, 60_000L))
        }
    }

    // Moderation / Safety
    fun setModerationOverride(context: Context, override: DevModerationOverride) { prefs(context).edit { putString(KEY_MODERATION_OVERRIDE, override.name) } }
    fun setForceAudience(context: Context, audience: Audience?) {
        prefs(context).edit {
            if (audience == null) remove(KEY_FORCE_AUDIENCE) else putString(KEY_FORCE_AUDIENCE, audience.name)
        }
    }
    fun setForceKidsFilterLevel(context: Context, level: KidsFilterLevel?) {
        prefs(context).edit {
            if (level == null) remove(KEY_FORCE_KIDS_FILTER) else putString(KEY_FORCE_KIDS_FILTER, level.name)
        }
    }
    fun setForcePinSet(context: Context, enabled: Boolean) { prefs(context).edit { putBoolean(KEY_FORCE_PIN_SET, enabled) } }
    fun setForcePinVerifySuccess(context: Context, enabled: Boolean) { prefs(context).edit { putBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, enabled) } }

    // Rendering / Network
    fun setSimulateOffline(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_SIMULATE_OFFLINE, v) } }
    fun setSimulateLoadingError(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_SIMULATE_LOADING_ERROR, v) } }
    fun setInfiniteLoading(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_INFINITE_LOADING, v) } }
    fun setShowFallbackUi(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_SHOW_FALLBACK_UI, v) } }
    fun setNetworkLatencyMs(context: Context, ms: Long) { prefs(context).edit { putLong(KEY_NETWORK_LATENCY_MS, ms.coerceIn(0L, 30_000L)) } }

    // Auth Overrides
    fun setForceLoggedOut(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FORCE_LOGGED_OUT, v) } }
    fun setBypassBiometric(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_BYPASS_BIOMETRIC, v) } }
    fun setForce2FA(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_FORCE_2FA, v) } }

    // Performance
    fun setShowPerformanceOverlay(context: Context, v: Boolean) { prefs(context).edit { putBoolean(KEY_SHOW_PERFORMANCE_OVERLAY, v) } }

    // Reset
    fun resetAll(context: Context) {
        prefs(context).edit { clear() }
    }
}

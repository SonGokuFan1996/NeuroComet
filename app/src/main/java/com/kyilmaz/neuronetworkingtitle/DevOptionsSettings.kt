package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import androidx.core.content.edit

// Explicit imports for models
import com.kyilmaz.neuronetworkingtitle.DevOptions
import com.kyilmaz.neuronetworkingtitle.DevModerationOverride
import com.kyilmaz.neuronetworkingtitle.Audience
import com.kyilmaz.neuronetworkingtitle.KidsFilterLevel

/**
 * Minimal SharedPreferences-backed dev options store.
 */
object DevOptionsSettings {
    private const val PREFS = "dev_options"

    private const val KEY_DEV_MENU_ENABLED = "dev_menu_enabled"
    private const val KEY_SHOW_DM_DEBUG_OVERLAY = "show_dm_debug_overlay"
    private const val KEY_DM_FORCE_SEND_FAILURE = "dm_force_send_failure"
    private const val KEY_DM_SEND_DELAY_MS = "dm_send_delay_ms"
    private const val KEY_DM_DISABLE_RATE_LIMIT = "dm_disable_rate_limit"
    private const val KEY_DM_MIN_INTERVAL_OVERRIDE_MS = "dm_min_interval_override_ms"
    private const val KEY_MODERATION_OVERRIDE = "moderation_override"
    private const val KEY_FORCE_AUDIENCE = "force_audience"
    private const val KEY_FORCE_KIDS_FILTER = "force_kids_filter"
    private const val KEY_FORCE_PIN_SET = "force_pin_set"
    private const val KEY_FORCE_PIN_VERIFY_SUCCESS = "force_pin_verify_success"


    fun get(context: Context): DevOptions {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val moderationOverride = runCatching {
            DevModerationOverride.valueOf(p.getString(KEY_MODERATION_OVERRIDE, DevModerationOverride.OFF.name)!!)
        }.getOrDefault(DevModerationOverride.OFF)

        val forceAudience = p.getString(KEY_FORCE_AUDIENCE, null)?.let {
            runCatching { Audience.valueOf(it) }.getOrNull()
        }

        val forceKids = p.getString(KEY_FORCE_KIDS_FILTER, null)?.let {
            runCatching { KidsFilterLevel.valueOf(it) }.getOrNull()
        }

        val minIntervalOverride = p.getLong(KEY_DM_MIN_INTERVAL_OVERRIDE_MS, -1L).takeIf { it >= 0L }

        return DevOptions(
            devMenuEnabled = p.getBoolean(KEY_DEV_MENU_ENABLED, false),
            showDmDebugOverlay = p.getBoolean(KEY_SHOW_DM_DEBUG_OVERLAY, false),

            dmForceSendFailure = p.getBoolean(KEY_DM_FORCE_SEND_FAILURE, false),
            dmArtificialSendDelayMs = p.getLong(KEY_DM_SEND_DELAY_MS, 450L),

            dmDisableRateLimit = p.getBoolean(KEY_DM_DISABLE_RATE_LIMIT, false),
            dmMinIntervalOverrideMs = minIntervalOverride,

            moderationOverride = moderationOverride,

            forceAudience = forceAudience,
            forceKidsFilterLevel = forceKids,
            forcePinSet = p.getBoolean(KEY_FORCE_PIN_SET, false),
            forcePinVerifySuccess = p.getBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, false)
        )
    }

    fun setDevMenuEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_DEV_MENU_ENABLED, enabled) }
    }

    fun setShowDmDebugOverlay(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_SHOW_DM_DEBUG_OVERLAY, enabled) }
    }

    fun setDmForceSendFailure(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_DM_FORCE_SEND_FAILURE, enabled) }
    }

    fun setDmSendDelayMs(context: Context, delayMs: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putLong(KEY_DM_SEND_DELAY_MS, delayMs.coerceIn(0L, 15_000L)) }
    }

    fun setDmDisableRateLimit(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_DM_DISABLE_RATE_LIMIT, enabled) }
    }

    fun setDmMinIntervalOverrideMs(context: Context, overrideMs: Long?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (overrideMs == null) remove(KEY_DM_MIN_INTERVAL_OVERRIDE_MS) else putLong(KEY_DM_MIN_INTERVAL_OVERRIDE_MS, overrideMs.coerceIn(0L, 60_000L))
        }
    }

    fun setModerationOverride(context: Context, override: DevModerationOverride) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_MODERATION_OVERRIDE, override.name) }
    }

    fun setForceAudience(context: Context, audience: Audience?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (audience == null) remove(KEY_FORCE_AUDIENCE) else putString(KEY_FORCE_AUDIENCE, audience.name)
        }
    }

    fun setForceKidsFilterLevel(context: Context, level: KidsFilterLevel?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
            if (level == null) remove(KEY_FORCE_KIDS_FILTER) else putString(KEY_FORCE_KIDS_FILTER, level.name)
        }
    }

    fun setForcePinSet(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_FORCE_PIN_SET, enabled) }
    }

    fun setForcePinVerifySuccess(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_FORCE_PIN_VERIFY_SUCCESS, enabled) }
    }

    fun resetAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { clear() }
    }
}
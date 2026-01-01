package com.kyilmaz.neuronetworkingtitle

import com.kyilmaz.neuronetworkingtitle.Audience
import com.kyilmaz.neuronetworkingtitle.KidsFilterLevel

/**
 * Developer options are intended for DEBUG/testing only.
 */
data class DevOptions(
    // Global
    val devMenuEnabled: Boolean = false,
    val showDmDebugOverlay: Boolean = false,

    // DM delivery simulation
    val dmForceSendFailure: Boolean = false,
    val dmArtificialSendDelayMs: Long = 0L, // No artificial delay by default

    // DM throttling - DISABLED by default (no rate limits)
    val dmDisableRateLimit: Boolean = true, // Rate limits disabled by default
    val dmMinIntervalOverrideMs: Long? = null,

    // Moderation override
    val moderationOverride: DevModerationOverride = DevModerationOverride.OFF,

    // Safety overrides (used by SafetyViewModel)
    val forceAudience: Audience? = null,
    val forceKidsFilterLevel: KidsFilterLevel? = null,
    val forcePinSet: Boolean = false,
    val forcePinVerifySuccess: Boolean = false
)

enum class DevModerationOverride {
    OFF,
    CLEAN,
    FLAGGED,
    BLOCKED
}
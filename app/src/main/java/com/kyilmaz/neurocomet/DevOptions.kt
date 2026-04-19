package com.kyilmaz.neurocomet


/**
 * Environment targets for backend configuration.
 */
enum class DevEnvironment {
    PRODUCTION,
    STAGING,
    LOCAL
}

/**
 * Content moderation override for testing.
 */
enum class DevModerationOverride {
    OFF,
    CLEAN,
    FLAGGED,
    BLOCKED
}

/**
 * Developer options state – intended for DEBUG/testing only.
 *
 * SECURITY: These options are only active in debuggable builds.
 * In production builds, [DevOptionsSettings.get] always returns the
 * hardened defaults (all overrides disabled).
 */
data class DevOptions(
    // ── Global ──────────────────────────────────────────────
    val devMenuEnabled: Boolean = false,
    val verboseLogging: Boolean = false,

    // ── Environment ─────────────────────────────────────────
    val environment: DevEnvironment = DevEnvironment.PRODUCTION,

    // ── Feature Flags ───────────────────────────────────────
    val enableNewFeedLayout: Boolean = false,
    val enableVideoChat: Boolean = true,
    val enableStoryReactions: Boolean = false,
    val enableAdvancedSearch: Boolean = false,
    val enableAiSuggestions: Boolean = false,

    // ── Cross-Device ──────────────────────────────────────────
    val enableHandoff: Boolean = true,

    // ── DM Debug ────────────────────────────────────────────
    val showDmDebugOverlay: Boolean = false,
    val dmForceSendFailure: Boolean = false,
    val dmArtificialSendDelayMs: Long = 0L,
    val dmDisableRateLimit: Boolean = true,
    val dmMinIntervalOverrideMs: Long? = null,

    // ── Content Moderation ──────────────────────────────────
    val moderationOverride: DevModerationOverride = DevModerationOverride.OFF,

    // ── Content Safety Overrides ────────────────────────────
    val forceAudience: Audience? = null,
    val forceKidsFilterLevel: KidsFilterLevel? = null,
    val forcePinSet: Boolean = false,
    val forcePinVerifySuccess: Boolean = false,

    // ── Rendering / Network Simulation ──────────────────────
    val simulateOffline: Boolean = false,
    val simulateLoadingError: Boolean = false,
    val infiniteLoading: Boolean = false,
    val showFallbackUi: Boolean = false,
    val publicReleaseMockUiEnabled: Boolean = false,
    val networkLatencyMs: Long = 0L,

    // ── Auth Overrides ──────────────────────────────────────
    val forceLoggedOut: Boolean = false,
    val bypassBiometric: Boolean = false,
    val force2FA: Boolean = false,

    // ── Performance ─────────────────────────────────────────
    val showPerformanceOverlay: Boolean = false,

    // ── Feedback & Beta Testing ────────────────────────────
    val bypassFeedbackRateLimit: Boolean = false,
    val forceFeedbackSubmitFailure: Boolean = false,

    // ── Supabase & Cloud Sync ──────────────────────────────
    val forcePremiumCloudSync: Boolean = false,
    val forceGameAchievementsSync: Boolean = false,
    val forceSettingsCloudSync: Boolean = false,
    val forceAiPracticeLogSync: Boolean = false,
    val forceAbTestSync: Boolean = false,
    val showCloudSyncDebug: Boolean = false,
)

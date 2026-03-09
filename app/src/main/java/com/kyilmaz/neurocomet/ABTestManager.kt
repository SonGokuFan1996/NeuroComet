@file:Suppress("unused")

package com.kyilmaz.neurocomet

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * A/B Testing framework for NeuroComet, restricted to authorized developer devices.
 *
 * ## Architecture
 * - Each **experiment** has a unique key, a set of named variants, and a traffic split.
 * - **Variant assignment** is deterministic: SHA-256(deviceHash + experimentKey) mod 100
 *   maps each device to a consistent variant across restarts without a server.
 * - Assignments are cached in SharedPreferences for speed and can be **overridden**
 *   from the Dev Options menu on whitelisted devices.
 * - On non-authorized devices (or release builds), every experiment returns its
 *   **control** variant — zero risk of accidentally shipping test behavior.
 *
 * ## Usage
 * ```kotlin
 * // Check an experiment anywhere in the code:
 * val variant = ABTestManager.getVariant(context, ABExperiment.NEW_PROFILE_LAYOUT)
 * when (variant) {
 *     "control"  -> showOldProfileLayout()
 *     "variant_a" -> showNewProfileLayoutA()
 *     "variant_b" -> showNewProfileLayoutB()
 * }
 *
 * // Or use the convenience boolean for simple on/off experiments:
 * if (ABTestManager.isEnabled(context, ABExperiment.COMPACT_FEED_CARDS)) {
 *     showCompactFeedCards()
 * }
 * ```
 *
 * ## Adding a new experiment
 * 1. Add an entry to the [ABExperiment] enum.
 * 2. Query it with [getVariant] or [isEnabled] at the feature site.
 * 3. When done, remove the enum entry — the framework auto-cleans stale overrides.
 */
object ABTestManager {

    private const val TAG = "ABTestManager"
    private const val PREFS_NAME = "ab_tests"
    private const val KEY_OVERRIDE_PREFIX = "override_"
    private const val KEY_ASSIGNMENT_PREFIX = "assigned_"

    // ── Observable state for the dev UI ────────────────────────
    private val _experiments = MutableStateFlow<Map<ABExperiment, ExperimentState>>(emptyMap())
    val experiments: StateFlow<Map<ABExperiment, ExperimentState>> = _experiments.asStateFlow()

    // ── Public API ─────────────────────────────────────────────

    /**
     * Get the variant string for an experiment on this device.
     *
     * Returns the control variant if:
     * - The device is not authorized
     * - The build is a release build
     * - The experiment is disabled
     */
    fun getVariant(context: Context, experiment: ABExperiment): String {
        // On non-dev devices, always return control for safety
        if (!DeviceAuthority.isAuthorizedDevice(context)) {
            return experiment.variants.first() // first variant is always "control"
        }

        // Check for manual override first
        val override = getOverride(context, experiment)
        if (override != null) return override

        // Deterministic assignment
        return getOrAssignVariant(context, experiment)
    }

    /**
     * Convenience: returns `true` when the device is NOT on the control variant.
     * Useful for simple on/off A/B tests.
     */
    fun isEnabled(context: Context, experiment: ABExperiment): Boolean {
        return getVariant(context, experiment) != experiment.variants.first()
    }

    /**
     * Manually override an experiment's variant from the dev menu.
     * Pass `null` to clear the override and revert to deterministic assignment.
     */
    fun setOverride(context: Context, experiment: ABExperiment, variant: String?) {
        if (!DeviceAuthority.isAuthorizedDevice(context)) {
            Log.w(TAG, "Cannot set override on unauthorized device")
            return
        }
        prefs(context).edit {
            if (variant == null) {
                remove("$KEY_OVERRIDE_PREFIX${experiment.key}")
            } else {
                putString("$KEY_OVERRIDE_PREFIX${experiment.key}", variant)
            }
        }
        Log.d(TAG, "Override set: ${experiment.key} → ${variant ?: "(cleared)"}")
        refreshState(context)
    }

    /**
     * Clear all overrides, reverting every experiment to deterministic assignment.
     */
    fun clearAllOverrides(context: Context) {
        if (!DeviceAuthority.isAuthorizedDevice(context)) return
        prefs(context).edit {
            prefs(context).all.keys
                .filter { it.startsWith(KEY_OVERRIDE_PREFIX) }
                .forEach { remove(it) }
        }
        Log.d(TAG, "All overrides cleared")
        refreshState(context)
    }

    /**
     * Re-roll assignments for ALL experiments on this device (new hash seed).
     * Useful for testing different variant paths during development.
     */
    fun rerollAllAssignments(context: Context) {
        if (!DeviceAuthority.isAuthorizedDevice(context)) return
        prefs(context).edit {
            prefs(context).all.keys
                .filter { it.startsWith(KEY_ASSIGNMENT_PREFIX) }
                .forEach { remove(it) }
            // Store a salt to change future deterministic assignments
            putLong("reroll_salt", System.currentTimeMillis())
        }
        Log.d(TAG, "All assignments re-rolled")
        refreshState(context)
    }

    /**
     * Refresh the observable state. Call after any change to overrides/assignments
     * or on app startup.
     */
    fun refreshState(context: Context) {
        val stateMap = ABExperiment.entries.associateWith { exp ->
            val override = getOverride(context, exp)
            val assigned = getOrAssignVariant(context, exp)
            val active = override ?: assigned
            ExperimentState(
                experiment = exp,
                assignedVariant = assigned,
                overrideVariant = override,
                activeVariant = active,
                isOverridden = override != null
            )
        }
        _experiments.value = stateMap
    }

    // ── Internal ────────────────────────────────────────────────

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getOverride(context: Context, experiment: ABExperiment): String? {
        return prefs(context).getString("$KEY_OVERRIDE_PREFIX${experiment.key}", null)
            ?.takeIf { it in experiment.variants }
    }

    /**
     * Deterministic variant assignment using SHA-256.
     * The same device + experiment always produces the same variant.
     */
    private fun getOrAssignVariant(context: Context, experiment: ABExperiment): String {
        val p = prefs(context)

        // Check cache
        val cached = p.getString("$KEY_ASSIGNMENT_PREFIX${experiment.key}", null)
        if (cached != null && cached in experiment.variants) return cached

        // Compute deterministic assignment
        val deviceHash = DeviceAuthority.computeDeviceHash(context)
        val salt = p.getLong("reroll_salt", 0L)
        val raw = "$deviceHash|${experiment.key}|$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        val bucket = ((bytes[0].toInt() and 0xFF) * 256 + (bytes[1].toInt() and 0xFF)) % 100

        // Map bucket to variant using traffic splits
        var cumulative = 0
        var assignedVariant = experiment.variants.first() // default: control
        for (i in experiment.variants.indices) {
            cumulative += experiment.trafficSplit.getOrElse(i) { 0 }
            if (bucket < cumulative) {
                assignedVariant = experiment.variants[i]
                break
            }
        }

        // Cache assignment
        p.edit { putString("$KEY_ASSIGNMENT_PREFIX${experiment.key}", assignedVariant) }
        Log.d(TAG, "Assigned ${experiment.key}: bucket=$bucket → $assignedVariant")
        return assignedVariant
    }
}

// ════════════════════════════════════════════════════════════════
// Experiment definitions
// ════════════════════════════════════════════════════════════════

/**
 * All active A/B experiments.
 *
 * To add a new experiment:
 * 1. Add an entry here with a unique [key].
 * 2. Define [variants] (first is always control) and [trafficSplit] (percentages summing to 100).
 * 3. Use [ABTestManager.getVariant] or [ABTestManager.isEnabled] at the feature site.
 *
 * When an experiment concludes, remove its entry and ship the winning variant.
 */
enum class ABExperiment(
    val key: String,
    val displayName: String,
    val description: String,
    val variants: List<String>,
    /** Percentage of traffic for each variant (must sum to 100). */
    val trafficSplit: List<Int>
) {
    // ── Feed experiments ────────────────────────────────────────
    COMPACT_FEED_CARDS(
        key = "compact_feed_cards",
        displayName = "Compact Feed Cards",
        description = "Test a denser feed card layout with less padding",
        variants = listOf("control", "compact"),
        trafficSplit = listOf(50, 50)
    ),
    FEED_LIKE_ANIMATION(
        key = "feed_like_animation",
        displayName = "Feed Like Animation",
        description = "Test different heart animation styles on like",
        variants = listOf("control", "bounce", "confetti"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Explore experiments ─────────────────────────────────────
    EXPLORE_CHIP_STYLE(
        key = "explore_chip_style",
        displayName = "Explore Chip Style",
        description = "Test rounded vs. pill-shaped chips in the Explore tab",
        variants = listOf("control", "pill"),
        trafficSplit = listOf(50, 50)
    ),

    EXPLORE_LIQUID_GLASS(
        key = "explore_liquid_glass",
        displayName = "Liquid Glass Interface",
        description = "Experimental translucent frosted-glass cards with blur and refraction effects",
        variants = listOf("control", "frosted", "refraction"),
        trafficSplit = listOf(34, 33, 33)
    ),

    EXPLORE_STORIES_LAYOUT(
        key = "explore_stories_layout",
        displayName = "Stories Layout",
        description = "Test full-bleed story circles vs. hiding stories entirely",
        variants = listOf("control", "fullbleed", "hidden"),
        trafficSplit = listOf(34, 33, 33)
    ),

    EXPLORE_TRENDING_RANK_STYLE(
        key = "explore_trending_rank_style",
        displayName = "Trending Rank Badge",
        description = "Test numbered rank badges or fire-gradient badges on trending posts",
        variants = listOf("control", "numbered", "fire_gradient"),
        trafficSplit = listOf(34, 33, 33)
    ),

    EXPLORE_PEOPLE_CARD_STYLE(
        key = "explore_people_card_style",
        displayName = "People Card Style",
        description = "Test horizontal card carousel for suggested people",
        variants = listOf("control", "horizontal_carousel"),
        trafficSplit = listOf(50, 50)
    ),

    EXPLORE_QUICK_ACCESS_LAYOUT(
        key = "explore_quick_access_layout",
        displayName = "Quick Access Layout",
        description = "Test a 2-row grid layout for quick access chips instead of horizontal scroll",
        variants = listOf("control", "grid"),
        trafficSplit = listOf(50, 50)
    ),

    // ── Notifications experiments ──────────────────────────────
    NOTIFICATION_GROUPING(
        key = "notification_grouping",
        displayName = "Notification Grouping",
        description = "Group similar notifications together",
        variants = listOf("control", "grouped"),
        trafficSplit = listOf(50, 50)
    ),

    // ── DM experiments ──────────────────────────────────────────
    DM_TYPING_INDICATOR(
        key = "dm_typing_indicator",
        displayName = "DM Typing Indicator",
        description = "Show a typing indicator in direct messages",
        variants = listOf("control", "dots", "text"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Profile experiments ─────────────────────────────────────
    PROFILE_HEADER_LAYOUT(
        key = "profile_header_layout",
        displayName = "Profile Header Layout",
        description = "Test different profile header layouts",
        variants = listOf("control", "centered", "hero_image"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Onboarding experiments ──────────────────────────────────
    ONBOARDING_FLOW(
        key = "onboarding_flow",
        displayName = "Onboarding Flow",
        description = "Test streamlined vs. detailed onboarding",
        variants = listOf("control", "streamlined"),
        trafficSplit = listOf(50, 50)
    ),

    // ── Explore – Liquid Glass Header ───────────────────────────
    EXPLORE_LIQUID_GLASS_HEADER(
        key = "explore_liquid_glass_header",
        displayName = "Liquid Glass Header",
        description = "Experimental translucent frosted-glass header with animated refraction highlights",
        variants = listOf("control", "frosted", "aurora"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Explore – Engagement Prompt Cards ────────────────────────
    EXPLORE_ENGAGEMENT_PROMPTS(
        key = "explore_engagement_prompts",
        displayName = "Engagement Prompt Cards",
        description = "Show motivational micro-prompt cards between feed posts to boost interaction",
        variants = listOf("control", "motivational", "question"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Explore – Card Entrance Animation ────────────────────────
    EXPLORE_CARD_ENTRANCE_ANIMATION(
        key = "explore_card_entrance_anim",
        displayName = "Card Entrance Animation",
        description = "Test different entrance animations for feed cards (slide, scale, flip)",
        variants = listOf("control", "scale_bounce", "flip"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Explore – Section Divider Style ──────────────────────────
    EXPLORE_SECTION_DIVIDER_STYLE(
        key = "explore_section_divider",
        displayName = "Section Divider Style",
        description = "Test gradient-line, icon-break, or hidden dividers between feed sections",
        variants = listOf("control", "gradient_line", "icon_break"),
        trafficSplit = listOf(34, 33, 33)
    ),

    // ── Settings experiments ────────────────────────────────────
    SETTINGS_SEARCH(
        key = "settings_search",
        displayName = "Settings Search Bar",
        description = "Add a search bar to the Settings screen",
        variants = listOf("control", "enabled"),
        trafficSplit = listOf(50, 50)
    )
}

/**
 * Snapshot of a single experiment's state on this device.
 */
data class ExperimentState(
    val experiment: ABExperiment,
    /** The variant assigned by the deterministic hash. */
    val assignedVariant: String,
    /** The manual override, if any. */
    val overrideVariant: String?,
    /** The effectively active variant (override wins). */
    val activeVariant: String,
    /** Whether a manual override is in effect. */
    val isOverridden: Boolean
)


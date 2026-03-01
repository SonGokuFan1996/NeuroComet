package com.kyilmaz.neurocomet

import android.app.Activity
import android.app.HandoffActivityParams
import android.os.Build
import android.util.Log

/**
 * Manages Android 17 (CinnamonBun / API 37+) Activity handoff support.
 *
 * `Activity.setHandoffEnabled(true, HandoffActivityParams)` allows the system
 * to offer the user the ability to seamlessly transfer/continue the current
 * activity on a nearby Android device (phone → tablet, phone → Chromebook, etc.).
 *
 * On pre-CinnamonBun API levels all calls are safe no-ops.
 *
 * @see <a href="https://developer.android.com/reference/kotlin/android/app/Activity#sethandoffenabled">Activity.setHandoffEnabled</a>
 */
object HandoffManager {

    private const val TAG = "HandoffManager"

    /**
     * Routes where handoff is appropriate (content-browsing / social).
     * The set uses the raw route patterns declared in [Screen].
     */
    private val HANDOFF_ENABLED_ROUTES = setOf(
        Screen.Feed.route,
        Screen.Explore.route,
        Screen.Messages.route,
        Screen.Notifications.route,
        Screen.Conversation.route,        // conversation/{conversationId}
        Screen.TopicDetail.route,          // topic/{topicId}
        Screen.Profile.route,             // profile/{userId}
        Screen.MyProfile.route,
        Screen.GamesHub.route,
        Screen.GamePlay.route,            // game/{gameId}
        Screen.CallHistory.route
    )

    /**
     * Routes where handoff should be disabled (sensitive / transient).
     * This is the fallback — anything not in [HANDOFF_ENABLED_ROUTES] is
     * disabled by default, but these are called out explicitly for clarity.
     */
    @Suppress("unused")
    private val HANDOFF_DISABLED_ROUTES = setOf(
        Screen.Settings.route,
        Screen.PrivacySettings.route,
        Screen.ParentalControls.route,
        Screen.Subscription.route,
        Screen.DevOptions.route,
        Screen.PracticeCall.route,
        Screen.PracticeCallSelection.route,
        Screen.FeedbackHub.route
    )

    /**
     * Enable or disable handoff for the given [activity].
     *
     * Safe to call on any API level — silently no-ops below CinnamonBun.
     */
    fun setHandoffEnabled(activity: Activity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            try {
                val params = HandoffActivityParams.Builder().build()
                activity.setHandoffEnabled(enabled, params)
                Log.d(TAG, "Handoff ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set handoff enabled=$enabled", e)
            }
        }
    }

    /**
     * Determine whether handoff should be enabled for the given navigation [route].
     *
     * @param route The current Compose NavHost destination route (may contain arguments).
     * @param userOptIn Whether the user has opted into handoff (from settings).
     *                  When `false`, handoff is always disabled regardless of route.
     * @param devOverride Optional developer override. `null` = follow normal logic,
     *                    `true`/`false` = force enable/disable.
     */
    fun shouldEnableHandoff(
        route: String?,
        userOptIn: Boolean = true,
        devOverride: Boolean? = null
    ): Boolean {
        // Developer override takes precedence
        devOverride?.let { return it }

        // User preference gate
        if (!userOptIn) return false

        // Route-based decision — strip arguments for pattern matching
        if (route == null) return false
        return HANDOFF_ENABLED_ROUTES.any { pattern ->
            route == pattern || route.startsWith(pattern.substringBefore("{"))
        }
    }
}


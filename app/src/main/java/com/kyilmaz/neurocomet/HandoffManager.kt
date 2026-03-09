package com.kyilmaz.neurocomet

import android.app.Activity
import android.os.Build
import android.util.Log

/**
 * Manages Android 17 (CinnamonBun / API 37+) Activity handoff support.
 *
 * On API 37+ `Activity.setHandoffEnabled(true, HandoffActivityParams)` allows
 * the system to seamlessly transfer the current activity to a nearby device.
 * On API 36 and below all calls are safe no-ops.
 */
object HandoffManager {

    private const val TAG = "HandoffManager"

    private val HANDOFF_ENABLED_ROUTES = setOf(
        Screen.Feed.route,
        Screen.Explore.route,
        Screen.Messages.route,
        Screen.Notifications.route,
        Screen.Conversation.route,
        Screen.TopicDetail.route,
        Screen.Profile.route,
        Screen.MyProfile.route,
        Screen.GamesHub.route,
        Screen.GamePlay.route,
        Screen.CallHistory.route
    )

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
     * Safe to call on any API level — silently no-ops below API 37.
     */
    @Suppress("NewApi")
    fun setHandoffEnabled(activity: Activity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= 37) {
            try {
                val params = android.app.HandoffActivityParams.Builder().build()
                activity.setHandoffEnabled(enabled, params)
                Log.d(TAG, "Handoff ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set handoff enabled=$enabled", e)
            }
        }
    }

    /**
     * Determine whether handoff should be enabled for the given navigation [route].
     */
    fun shouldEnableHandoff(
        route: String?,
        userOptIn: Boolean = true,
        devOverride: Boolean? = null
    ): Boolean {
        // Handoff only exists on API 37+
        if (Build.VERSION.SDK_INT < 37) return false

        devOverride?.let { return it }
        if (!userOptIn) return false
        if (route == null) return false
        return HANDOFF_ENABLED_ROUTES.any { pattern ->
            route == pattern || route.startsWith(pattern.substringBefore("{"))
        }
    }
}


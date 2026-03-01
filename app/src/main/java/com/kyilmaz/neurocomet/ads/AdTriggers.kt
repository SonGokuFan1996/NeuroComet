package com.kyilmaz.neurocomet.ads

import android.app.Activity
import android.util.Log

/**
 * Utility class for triggering ads at appropriate moments in the app
 *
 * Best practices for ad placement in a neurodivergent-friendly app:
 * - Never interrupt focus sessions or calming activities
 * - Only show interstitials at natural transition points
 * - Respect frequency caps strictly
 * - Never show ads during meltdown/overload modes
 */
object AdTriggers {

    private const val TAG = "AdTriggers"

    /**
     * Natural break points where interstitial ads are acceptable
     */
    enum class AdMoment {
        // Good moments for ads
        GAME_COMPLETE,           // After finishing a mini-game
        FEED_TAB_SWITCH,         // When switching tabs (but not constantly)
        SESSION_END,             // When the user is leaving the app
        LEVEL_UP,                // After earning a badge/achievement
        EXPLORE_CATEGORY_EXIT,   // When exiting an explore category

        // Should skip ads for these
        ENTERING_CALM_MODE,      // User needs calm, no ads
        DURING_FOCUS_SESSION,    // User is focusing, don't interrupt
        MELTDOWN_RECOVERY,       // User is recovering, be gentle
        FIRST_APP_LAUNCH,        // Give user time to explore first
    }

    /**
     * Try to show an interstitial ad at a natural moment
     * Returns true if ad was shown, false otherwise
     */
    fun tryShowInterstitialAt(activity: Activity, moment: AdMoment): Boolean {
        // Check if this is an appropriate moment
        if (!isAppropriateAdMoment(moment)) {
            Log.d(TAG, "Skipping ad at $moment - not appropriate")
            return false
        }

        // Try to show the interstitial
        val shown = GoogleAdsManager.showInterstitialAd(activity)

        if (shown) {
            Log.d(TAG, "Showed interstitial ad at $moment")
        } else {
            Log.d(TAG, "Could not show interstitial at $moment")
        }

        return shown
    }

    /**
     * Check if the current moment is appropriate for an ad
     */
    private fun isAppropriateAdMoment(moment: AdMoment): Boolean {
        return when (moment) {
            // These are good moments
            AdMoment.GAME_COMPLETE,
            AdMoment.SESSION_END,
            AdMoment.LEVEL_UP,
            AdMoment.EXPLORE_CATEGORY_EXIT -> true

            // Tab switch only occasionally
            AdMoment.FEED_TAB_SWITCH -> {
                // Only show on every 3rd tab switch
                tabSwitchCount++
                tabSwitchCount % 3 == 0
            }

            // These should never show ads
            AdMoment.ENTERING_CALM_MODE,
            AdMoment.DURING_FOCUS_SESSION,
            AdMoment.MELTDOWN_RECOVERY,
            AdMoment.FIRST_APP_LAUNCH -> false
        }
    }

    // Counter for tab switches
    private var tabSwitchCount = 0

    /**
     * Reset counters (call on new session)
     */
    fun resetCounters() {
        tabSwitchCount = 0
    }

    /**
     * Preload ads for expected moments
     * Call this when user enters certain areas of the app
     */
    fun preloadForUpcomingMoment(activity: Activity, expectedMoment: AdMoment) {
        when (expectedMoment) {
            AdMoment.GAME_COMPLETE -> {
                // User started a game, preload interstitial for when they finish
                GoogleAdsManager.loadInterstitialAd(activity)
            }
            AdMoment.SESSION_END -> {
                // Good to have interstitial ready for app backgrounding
                GoogleAdsManager.loadInterstitialAd(activity)
            }
            else -> {
                // Other moments don't need special preloading
            }
        }
    }
}


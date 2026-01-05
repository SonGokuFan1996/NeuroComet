package com.kyilmaz.neurocomet

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Ads Manager for NeuroComet
 *
 * Features:
 * - Automatically disables ads when user has premium subscription
 * - Supports banner, interstitial, and rewarded ads
 * - Neurodivergent-friendly: respects reduced motion and sensory preferences
 * - Implements frequency capping to avoid overwhelming users
 * - Dev testing support for simulating ad states
 *
 * IMPORTANT: Ads are completely removed for premium users (monthly or lifetime).
 * The isPremium check is done securely through SubscriptionManager verification.
 */
object GoogleAdsManager {

    private const val TAG = "GoogleAdsManager"

    // Ad Unit IDs - Replace with your actual AdMob IDs
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/XXXXXXXX"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/XXXXXXXX"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/XXXXXXXX"
    private const val NATIVE_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/XXXXXXXX"

    // Test Ad Unit IDs (for development)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // State
    private val _adsState = MutableStateFlow(AdsState())
    val adsState: StateFlow<AdsState> = _adsState.asStateFlow()

    // Frequency capping
    private var lastInterstitialTime: Long = 0L
    private var interstitialCount: Int = 0
    private const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L // 1 minute minimum between interstitials
    private const val MAX_INTERSTITIALS_PER_SESSION = 5

    // Neurodivergent-friendly settings
    private var reducedMotionEnabled: Boolean = false
    private var sensoryFriendlyMode: Boolean = false

    data class AdsState(
        val isInitialized: Boolean = false,
        val isLoading: Boolean = false,
        val isPremium: Boolean = false,
        val adsEnabled: Boolean = true,
        val bannerLoaded: Boolean = false,
        val interstitialLoaded: Boolean = false,
        val rewardedLoaded: Boolean = false,
        val nativeLoaded: Boolean = false,
        val error: String? = null,
        val lastAdShown: String? = null,
        val totalAdsShown: Int = 0,
        // Dev testing options
        val useTestAds: Boolean = true,
        val forceShowAds: Boolean = false,
        val simulateAdFailure: Boolean = false,
        val simulateSlowLoad: Boolean = false,
        val adLoadDelayMs: Long = 0L
    )

    /**
     * Initialize the Google Ads SDK
     * Called once at app startup
     */
    fun initialize(context: Context, useTestAds: Boolean = BuildConfig.DEBUG) {
        if (_adsState.value.isInitialized) {
            Log.d(TAG, "Ads already initialized")
            return
        }

        Log.d(TAG, "Initializing Google Ads SDK...")
        _adsState.value = _adsState.value.copy(
            isLoading = true,
            useTestAds = useTestAds
        )

        try {
            // In a real implementation, you would:
            // MobileAds.initialize(context) { initializationStatus ->
            //     Log.d(TAG, "Ads initialized: ${initializationStatus.adapterStatusMap}")
            //     _adsState.value = _adsState.value.copy(isInitialized = true, isLoading = false)
            // }

            // For now, simulate initialization
            _adsState.value = _adsState.value.copy(
                isInitialized = true,
                isLoading = false
            )

            // Check premium status and disable ads if premium
            checkPremiumStatusAndUpdateAds()

            Log.d(TAG, "Ads initialization complete")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ads", e)
            _adsState.value = _adsState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }

    /**
     * Check premium status and completely disable ads for premium users
     * This is called automatically on initialization and when subscription status changes
     */
    fun checkPremiumStatusAndUpdateAds() {
        SubscriptionManager.checkPremiumStatus { isPremium ->
            val verified = if (isPremium) SubscriptionManager.verifyPremiumStatus() else false

            _adsState.value = _adsState.value.copy(
                isPremium = isPremium && verified,
                adsEnabled = !(isPremium && verified)
            )

            if (isPremium && verified) {
                Log.d(TAG, "Premium user detected - ads completely disabled")
                // Clear any loaded ads
                clearAllAds()
            } else {
                Log.d(TAG, "Non-premium user - ads enabled")
                // Preload ads
                preloadAds()
            }
        }
    }

    /**
     * Check if ads should be shown
     * Returns false for premium users or if ads are disabled
     */
    fun shouldShowAds(): Boolean {
        val state = _adsState.value

        // Dev override
        if (state.forceShowAds) return true

        // Premium users never see ads
        if (state.isPremium) {
            // Double-check with secure verification
            val verified = SubscriptionManager.verifyPremiumStatus()
            if (verified) {
                return false
            }
        }

        return state.adsEnabled && state.isInitialized
    }

    /**
     * Set neurodivergent-friendly preferences
     */
    fun setNeurodivergentPreferences(reducedMotion: Boolean, sensoryFriendly: Boolean) {
        reducedMotionEnabled = reducedMotion
        sensoryFriendlyMode = sensoryFriendly

        Log.d(TAG, "Neurodivergent preferences updated: reducedMotion=$reducedMotion, sensoryFriendly=$sensoryFriendly")
    }

    /**
     * Load a banner ad
     */
    fun loadBannerAd(context: Context) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping banner ad load - ads disabled or premium")
            return
        }

        val state = _adsState.value
        if (state.simulateAdFailure) {
            _adsState.value = _adsState.value.copy(error = "Simulated banner ad failure")
            return
        }

        Log.d(TAG, "Loading banner ad...")
        _adsState.value = _adsState.value.copy(isLoading = true)

        // In real implementation:
        // val adRequest = AdRequest.Builder().build()
        // bannerAdView.loadAd(adRequest)

        // Simulate load
        _adsState.value = _adsState.value.copy(
            isLoading = false,
            bannerLoaded = true
        )
    }

    /**
     * Load an interstitial ad
     */
    fun loadInterstitialAd(context: Context) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping interstitial ad load - ads disabled or premium")
            return
        }

        val state = _adsState.value
        if (state.simulateAdFailure) {
            _adsState.value = _adsState.value.copy(error = "Simulated interstitial ad failure")
            return
        }

        Log.d(TAG, "Loading interstitial ad...")
        _adsState.value = _adsState.value.copy(isLoading = true)

        // In real implementation:
        // InterstitialAd.load(context, getInterstitialAdUnitId(), adRequest, callback)

        _adsState.value = _adsState.value.copy(
            isLoading = false,
            interstitialLoaded = true
        )
    }

    /**
     * Show interstitial ad with frequency capping
     * Returns true if ad was shown, false otherwise
     */
    fun showInterstitialAd(activity: Activity): Boolean {
        if (!shouldShowAds()) {
            Log.d(TAG, "Not showing interstitial - ads disabled or premium")
            return false
        }

        // Frequency capping
        val now = System.currentTimeMillis()
        if (now - lastInterstitialTime < INTERSTITIAL_MIN_INTERVAL_MS) {
            Log.d(TAG, "Skipping interstitial - too soon since last one")
            return false
        }

        if (interstitialCount >= MAX_INTERSTITIALS_PER_SESSION) {
            Log.d(TAG, "Skipping interstitial - max per session reached")
            return false
        }

        // Sensory-friendly mode: reduce interstitial frequency further
        if (sensoryFriendlyMode && interstitialCount >= 2) {
            Log.d(TAG, "Skipping interstitial - sensory-friendly mode limits reached")
            return false
        }

        if (!_adsState.value.interstitialLoaded) {
            Log.d(TAG, "Interstitial not loaded")
            return false
        }

        Log.d(TAG, "Showing interstitial ad...")

        // In real implementation:
        // interstitialAd?.show(activity)

        lastInterstitialTime = now
        interstitialCount++

        _adsState.value = _adsState.value.copy(
            interstitialLoaded = false,
            lastAdShown = "interstitial",
            totalAdsShown = _adsState.value.totalAdsShown + 1
        )

        // Preload next interstitial
        loadInterstitialAd(activity)

        return true
    }

    /**
     * Load rewarded ad
     */
    fun loadRewardedAd(context: Context) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping rewarded ad load - ads disabled or premium")
            return
        }

        Log.d(TAG, "Loading rewarded ad...")
        _adsState.value = _adsState.value.copy(isLoading = true)

        // In real implementation:
        // RewardedAd.load(context, getRewardedAdUnitId(), adRequest, callback)

        _adsState.value = _adsState.value.copy(
            isLoading = false,
            rewardedLoaded = true
        )
    }

    /**
     * Show rewarded ad
     */
    fun showRewardedAd(activity: Activity, onRewarded: (Int, String) -> Unit): Boolean {
        // Rewarded ads can be shown even for premium users if they choose
        // but premium users don't need rewards typically

        if (!_adsState.value.rewardedLoaded) {
            Log.d(TAG, "Rewarded ad not loaded")
            return false
        }

        Log.d(TAG, "Showing rewarded ad...")

        // In real implementation:
        // rewardedAd?.show(activity) { reward ->
        //     onRewarded(reward.amount, reward.type)
        // }

        // Simulate reward
        onRewarded(1, "coins")

        _adsState.value = _adsState.value.copy(
            rewardedLoaded = false,
            lastAdShown = "rewarded",
            totalAdsShown = _adsState.value.totalAdsShown + 1
        )

        return true
    }

    /**
     * Preload all ad types
     */
    private fun preloadAds() {
        // This would be called with actual context in real usage
        Log.d(TAG, "Preloading ads...")
    }

    /**
     * Clear all loaded ads (called when user becomes premium)
     */
    private fun clearAllAds() {
        _adsState.value = _adsState.value.copy(
            bannerLoaded = false,
            interstitialLoaded = false,
            rewardedLoaded = false,
            nativeLoaded = false
        )
        Log.d(TAG, "All ads cleared")
    }

    /**
     * Get the appropriate ad unit ID based on test mode
     */
    private fun getBannerAdUnitId(): String {
        return if (_adsState.value.useTestAds) TEST_BANNER_AD_UNIT_ID else BANNER_AD_UNIT_ID
    }

    private fun getInterstitialAdUnitId(): String {
        return if (_adsState.value.useTestAds) TEST_INTERSTITIAL_AD_UNIT_ID else INTERSTITIAL_AD_UNIT_ID
    }

    private fun getRewardedAdUnitId(): String {
        return if (_adsState.value.useTestAds) TEST_REWARDED_AD_UNIT_ID else REWARDED_AD_UNIT_ID
    }

    private fun getNativeAdUnitId(): String {
        return if (_adsState.value.useTestAds) TEST_NATIVE_AD_UNIT_ID else NATIVE_AD_UNIT_ID
    }

    // ═══════════════════════════════════════════════════════════════
    // DEV TESTING FUNCTIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Force enable/disable ads (dev testing only)
     */
    fun devSetAdsEnabled(enabled: Boolean) {
        _adsState.value = _adsState.value.copy(adsEnabled = enabled)
        Log.d(TAG, "[DEV] Ads enabled: $enabled")
    }

    /**
     * Force show ads even for premium users (dev testing only)
     */
    fun devSetForceShowAds(force: Boolean) {
        _adsState.value = _adsState.value.copy(forceShowAds = force)
        Log.d(TAG, "[DEV] Force show ads: $force")
    }

    /**
     * Simulate ad load failures (dev testing only)
     */
    fun devSetSimulateAdFailure(simulate: Boolean) {
        _adsState.value = _adsState.value.copy(simulateAdFailure = simulate)
        Log.d(TAG, "[DEV] Simulate ad failure: $simulate")
    }

    /**
     * Simulate slow ad loading (dev testing only)
     */
    fun devSetSimulateSlowLoad(simulate: Boolean, delayMs: Long = 3000L) {
        _adsState.value = _adsState.value.copy(
            simulateSlowLoad = simulate,
            adLoadDelayMs = delayMs
        )
        Log.d(TAG, "[DEV] Simulate slow load: $simulate (${delayMs}ms)")
    }

    /**
     * Toggle test ads mode (dev testing only)
     */
    fun devSetUseTestAds(useTest: Boolean) {
        _adsState.value = _adsState.value.copy(useTestAds = useTest)
        Log.d(TAG, "[DEV] Use test ads: $useTest")
    }

    /**
     * Simulate premium status for testing (dev testing only)
     */
    fun devSetSimulatePremium(premium: Boolean) {
        _adsState.value = _adsState.value.copy(
            isPremium = premium,
            adsEnabled = !premium
        )
        if (premium) {
            clearAllAds()
        }
        Log.d(TAG, "[DEV] Simulate premium: $premium")
    }

    /**
     * Reset session counters (dev testing only)
     */
    fun devResetSessionCounters() {
        lastInterstitialTime = 0L
        interstitialCount = 0
        _adsState.value = _adsState.value.copy(totalAdsShown = 0)
        Log.d(TAG, "[DEV] Session counters reset")
    }

    /**
     * Force load all ad types (dev testing only)
     */
    fun devForceLoadAllAds() {
        _adsState.value = _adsState.value.copy(
            bannerLoaded = true,
            interstitialLoaded = true,
            rewardedLoaded = true,
            nativeLoaded = true
        )
        Log.d(TAG, "[DEV] Force loaded all ads")
    }

    /**
     * Get debug info string (dev testing only)
     */
    fun devGetDebugInfo(): String {
        val state = _adsState.value
        return buildString {
            appendLine("=== Google Ads Debug Info ===")
            appendLine("Initialized: ${state.isInitialized}")
            appendLine("Premium: ${state.isPremium}")
            appendLine("Ads Enabled: ${state.adsEnabled}")
            appendLine("Using Test Ads: ${state.useTestAds}")
            appendLine("")
            appendLine("=== Ad Load Status ===")
            appendLine("Banner: ${state.bannerLoaded}")
            appendLine("Interstitial: ${state.interstitialLoaded}")
            appendLine("Rewarded: ${state.rewardedLoaded}")
            appendLine("Native: ${state.nativeLoaded}")
            appendLine("")
            appendLine("=== Session Stats ===")
            appendLine("Total Ads Shown: ${state.totalAdsShown}")
            appendLine("Last Ad: ${state.lastAdShown ?: "none"}")
            appendLine("Interstitials This Session: $interstitialCount")
            appendLine("")
            appendLine("=== Dev Overrides ===")
            appendLine("Force Show: ${state.forceShowAds}")
            appendLine("Simulate Failure: ${state.simulateAdFailure}")
            appendLine("Simulate Slow Load: ${state.simulateSlowLoad}")
            if (state.error != null) {
                appendLine("")
                appendLine("=== Error ===")
                appendLine(state.error)
            }
        }
    }
}


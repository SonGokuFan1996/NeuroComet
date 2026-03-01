package com.kyilmaz.neurocomet.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.kyilmaz.neurocomet.BuildConfig
import com.kyilmaz.neurocomet.SecurityUtils
import com.kyilmaz.neurocomet.SubscriptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Ads Manager for NeuroComet
 *
 * Features:
 * - Automatically disables ads when user has premium subscription
 * - Supports banner, interstitial, and rewarded ads
 * - Neurodivergent-friendly: respects reduced motion and sensory preferences
 * - Implements frequency capping to avoid overwhelming users
 * - Secure handling of Ad Unit IDs via obfuscation
 */
object GoogleAdsManager {

    private const val TAG = "GoogleAdsManager"

    // Coroutine scope for async operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // State
    private val _adsState = MutableStateFlow(AdsState())
    val adsState: StateFlow<AdsState> = _adsState.asStateFlow()

    // Ad instances
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // Banner ad views cache
    private val bannerAdViews = mutableMapOf<String, AdView>()

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
        val error: String? = null,
        val lastAdShown: String? = null,
        val totalAdsShown: Int = 0,
        // Dev testing options
        val useTestAds: Boolean = BuildConfig.DEBUG,
        val forceShowAds: Boolean = false,
        val simulateAdFailure: Boolean = false,
        val kidsMode: Boolean = false
    )

    /**
     * Initialize the Google Mobile Ads SDK
     * Called once at app startup
     */
    fun initialize(context: Context, useTestAds: Boolean = BuildConfig.DEBUG) {
        if (_adsState.value.isInitialized) {
            Log.d(TAG, "Ads already initialized")
            return
        }

        Log.d(TAG, "Initializing Google Mobile Ads SDK...")
        _adsState.value = _adsState.value.copy(
            isLoading = true,
            useTestAds = useTestAds
        )

        try {
            // Configure test devices if in debug mode
            if (useTestAds) {
                val testDeviceIds = listOf(
                    AdRequest.DEVICE_ID_EMULATOR,
                    // Add your test device IDs here
                )
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
            }

            // Initialize the Mobile Ads SDK
            MobileAds.initialize(context) { initializationStatus ->
                val statusMap = initializationStatus.adapterStatusMap
                for ((adapter, status) in statusMap) {
                    Log.d(TAG, "Adapter: $adapter, Status: ${status.initializationState}")
                }

                _adsState.value = _adsState.value.copy(
                    isInitialized = true,
                    isLoading = false
                )

                Log.d(TAG, "Google Mobile Ads SDK initialized successfully")

                // Check premium status and preload ads if not premium
                checkPremiumStatusAndUpdateAds(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Mobile Ads SDK", e)
            _adsState.value = _adsState.value.copy(
                isLoading = false,
                error = "Initialization failed: ${e.message}"
            )
        }
    }

    /**
     * Check premium status and completely disable ads for premium users
     */
    fun checkPremiumStatusAndUpdateAds(context: Context) {
        SubscriptionManager.checkPremiumStatus { isPremium ->
            val verified = if (isPremium) SubscriptionManager.verifyPremiumStatus() else false

            _adsState.value = _adsState.value.copy(
                isPremium = isPremium && verified,
                adsEnabled = !(isPremium && verified)
            )

            if (isPremium && verified) {
                Log.d(TAG, "Premium user detected - ads completely disabled")
                clearAllAds()
            } else {
                Log.d(TAG, "Non-premium user - preloading ads")
                preloadAds(context)
            }
        }
    }

    /**
     * Check if ads should be shown
     */
    fun shouldShowAds(): Boolean {
        val state = _adsState.value

        // Kids mode: no ads
        if (state.kidsMode) {
            Log.d(TAG, "shouldShowAds: false (kids mode)")
            return false
        }

        // Dev override
        if (state.forceShowAds) {
            Log.d(TAG, "shouldShowAds: true (force show)")
            return true
        }

        // Premium users never see ads
        if (state.isPremium) {
            val verified = SubscriptionManager.verifyPremiumStatus()
            if (verified) {
                Log.d(TAG, "shouldShowAds: false (premium user)")
                return false
            }
        }

        // If ads are explicitly disabled, don't show
        if (!state.adsEnabled) {
            Log.d(TAG, "shouldShowAds: false (ads disabled)")
            return false
        }

        Log.d(TAG, "shouldShowAds: true (initialized=${state.isInitialized})")
        return true
    }

    /**
     * Set kids mode (disables all ads)
     */
    fun setKidsMode(enabled: Boolean) {
        _adsState.value = _adsState.value.copy(kidsMode = enabled)
        if (enabled) {
            clearAllAds()
        }
        Log.d(TAG, "Kids mode: $enabled")
    }

    /**
     * Set neurodivergent-friendly preferences
     */
    fun setNeurodivergentPreferences(reducedMotion: Boolean, sensoryFriendly: Boolean) {
        reducedMotionEnabled = reducedMotion
        sensoryFriendlyMode = sensoryFriendly
        Log.d(TAG, "ND preferences: reducedMotion=$reducedMotion, sensoryFriendly=$sensoryFriendly")
    }

    // ═══════════════════════════════════════════════════════════════
    // BANNER ADS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create and load a banner ad view
     */
    fun createBannerAdView(
        context: Context,
        adSize: AdSize = AdSize.BANNER,
        adUnitKey: String = "default"
    ): AdView? {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping banner ad - ads disabled or premium")
            return null
        }

        if (_adsState.value.simulateAdFailure) {
            _adsState.value = _adsState.value.copy(error = "Simulated banner ad failure")
            return null
        }

        // Check if we already have this banner
        bannerAdViews[adUnitKey]?.let { existingView ->
            return existingView
        }

        Log.d(TAG, "Creating banner ad view: $adUnitKey")

        val adView = AdView(context).apply {
            setAdSize(adSize)
            adUnitId = SecurityUtils.decrypt(BuildConfig.ADMOB_BANNER_ID)

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded: $adUnitKey")
                    _adsState.value = _adsState.value.copy(bannerLoaded = true, error = null)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Banner ad failed to load: ${error.message}")
                    _adsState.value = _adsState.value.copy(
                        bannerLoaded = false,
                        error = "Banner: ${error.message}"
                    )
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Banner ad clicked")
                }

                override fun onAdImpression() {
                    _adsState.value = _adsState.value.copy(
                        totalAdsShown = _adsState.value.totalAdsShown + 1,
                        lastAdShown = "banner"
                    )
                }
            }
        }

        // Load the ad
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Cache the banner
        bannerAdViews[adUnitKey] = adView

        return adView
    }

    /**
     * Destroy a specific banner ad
     */
    fun destroyBannerAd(adUnitKey: String = "default") {
        bannerAdViews[adUnitKey]?.destroy()
        bannerAdViews.remove(adUnitKey)
        Log.d(TAG, "Banner ad destroyed: $adUnitKey")
    }

    // ═══════════════════════════════════════════════════════════════
    // INTERSTITIAL ADS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Load an interstitial ad
     */
    fun loadInterstitialAd(context: Context) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Skipping interstitial ad load - ads disabled or premium")
            return
        }

        if (_adsState.value.simulateAdFailure) {
            _adsState.value = _adsState.value.copy(error = "Simulated interstitial ad failure")
            return
        }

        if (interstitialAd != null) {
            Log.d(TAG, "Interstitial ad already loaded")
            return
        }

        Log.d(TAG, "Loading interstitial ad...")
        _adsState.value = _adsState.value.copy(isLoading = true)

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            SecurityUtils.decrypt(BuildConfig.ADMOB_INTERSTITIAL_ID),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    _adsState.value = _adsState.value.copy(
                        isLoading = false,
                        interstitialLoaded = true,
                        error = null
                    )

                    // Set up full screen content callback
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            _adsState.value = _adsState.value.copy(interstitialLoaded = false)
                            // Preload next interstitial
                            loadInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                            interstitialAd = null
                            _adsState.value = _adsState.value.copy(
                                interstitialLoaded = false,
                                error = "Interstitial show error: ${error.message}"
                            )
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad shown")
                            _adsState.value = _adsState.value.copy(
                                totalAdsShown = _adsState.value.totalAdsShown + 1,
                                lastAdShown = "interstitial"
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    _adsState.value = _adsState.value.copy(
                        isLoading = false,
                        interstitialLoaded = false,
                        error = "Interstitial: ${error.message}"
                    )
                }
            }
        )
    }

    /**
     * Show interstitial ad with frequency capping
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

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial not loaded")
            loadInterstitialAd(activity)
            return false
        }

        Log.d(TAG, "Showing interstitial ad...")
        ad.show(activity)

        lastInterstitialTime = now
        interstitialCount++

        return true
    }

    // ═══════════════════════════════════════════════════════════════
    // REWARDED ADS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Load a rewarded ad
     */
    fun loadRewardedAd(context: Context) {
        if (_adsState.value.simulateAdFailure) {
            _adsState.value = _adsState.value.copy(error = "Simulated rewarded ad failure")
            return
        }

        if (rewardedAd != null) {
            Log.d(TAG, "Rewarded ad already loaded")
            return
        }

        Log.d(TAG, "Loading rewarded ad...")
        _adsState.value = _adsState.value.copy(isLoading = true)

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            SecurityUtils.decrypt(BuildConfig.ADMOB_REWARDED_ID),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
                    rewardedAd = ad
                    _adsState.value = _adsState.value.copy(
                        isLoading = false,
                        rewardedLoaded = true,
                        error = null
                    )

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed")
                            rewardedAd = null
                            _adsState.value = _adsState.value.copy(rewardedLoaded = false)
                            // Preload next rewarded ad
                            loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                            rewardedAd = null
                            _adsState.value = _adsState.value.copy(
                                rewardedLoaded = false,
                                error = "Rewarded show error: ${error.message}"
                            )
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad shown")
                            _adsState.value = _adsState.value.copy(
                                totalAdsShown = _adsState.value.totalAdsShown + 1,
                                lastAdShown = "rewarded"
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    _adsState.value = _adsState.value.copy(
                        isLoading = false,
                        rewardedLoaded = false,
                        error = "Rewarded: ${error.message}"
                    )
                }
            }
        )
    }

    /**
     * Show rewarded ad and deliver reward
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (amount: Int, type: String) -> Unit
    ): Boolean {
        if (_adsState.value.kidsMode) {
            Log.d(TAG, "Not showing rewarded ad - kids mode enabled")
            return false
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.d(TAG, "Rewarded ad not loaded")
            loadRewardedAd(activity)
            return false
        }

        Log.d(TAG, "Showing rewarded ad...")
        ad.show(activity) { reward ->
            Log.d(TAG, "User earned reward: ${reward.amount} ${reward.type}")
            onRewarded(reward.amount, reward.type)
        }

        return true
    }

    /**
     * Check if rewarded ad is ready
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null

    // ═══════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Preload all ad types
     */
    private fun preloadAds(context: Context) {
        if (!shouldShowAds()) return

        scope.launch {
            loadInterstitialAd(context)
            loadRewardedAd(context)
        }
        Log.d(TAG, "Preloading ads...")
    }

    /**
     * Clear all loaded ads
     */
    private fun clearAllAds() {
        bannerAdViews.values.forEach { it.destroy() }
        bannerAdViews.clear()
        interstitialAd = null
        rewardedAd = null
        _adsState.value = _adsState.value.copy(
            bannerLoaded = false,
            interstitialLoaded = false,
            rewardedLoaded = false
        )
        Log.d(TAG, "All ads cleared")
    }

    // ═══════════════════════════════════════════════════════════════
    // DEV TESTING FUNCTIONS
    // ═══════════════════════════════════════════════════════════════

    fun devSetAdsEnabled(enabled: Boolean) {
        _adsState.value = _adsState.value.copy(adsEnabled = enabled)
    }

    fun devSetForceShowAds(force: Boolean) {
        _adsState.value = _adsState.value.copy(forceShowAds = force)
    }

    fun devSetSimulateAdFailure(simulate: Boolean) {
        _adsState.value = _adsState.value.copy(simulateAdFailure = simulate)
    }

    fun devSetUseTestAds(useTest: Boolean) {
        _adsState.value = _adsState.value.copy(useTestAds = useTest)
    }

    fun devSetSimulatePremium(premium: Boolean) {
        _adsState.value = _adsState.value.copy(
            isPremium = premium,
            adsEnabled = !premium
        )
        if (premium) clearAllAds()
    }

    fun devResetSessionCounters() {
        lastInterstitialTime = 0L
        interstitialCount = 0
        _adsState.value = _adsState.value.copy(totalAdsShown = 0)
    }

    fun devForceLoadAllAds() {
        _adsState.value = _adsState.value.copy(
            bannerLoaded = true,
            interstitialLoaded = true,
            rewardedLoaded = true
        )
    }

    fun devGetDebugInfo(): String {
        val state = _adsState.value
        return buildString {
            appendLine("=== Google Ads Debug Info ===")
            appendLine("Initialized: ${state.isInitialized}")
            appendLine("Premium: ${state.isPremium}")
            appendLine("Ads Enabled: ${state.adsEnabled}")
            appendLine("Kids Mode: ${state.kidsMode}")
            appendLine("Using Test Ads: ${state.useTestAds}")
            appendLine("")
            appendLine("=== Ad Load Status ===")
            appendLine("Banner: ${state.bannerLoaded}")
            appendLine("Interstitial: ${state.interstitialLoaded}")
            appendLine("Rewarded: ${state.rewardedLoaded}")
            appendLine("")
            appendLine("=== Session Stats ===")
            appendLine("Total Ads Shown: ${state.totalAdsShown}")
            appendLine("Last Ad: ${state.lastAdShown ?: "none"}")
            appendLine("")
            appendLine("=== Ad Unit IDs (Decrypted) ===")
            appendLine("Banner: ${SecurityUtils.decrypt(BuildConfig.ADMOB_BANNER_ID)}")
            appendLine("Interstitial: ${SecurityUtils.decrypt(BuildConfig.ADMOB_INTERSTITIAL_ID)}")
            appendLine("Rewarded: ${SecurityUtils.decrypt(BuildConfig.ADMOB_REWARDED_ID)}")
        }
    }
}

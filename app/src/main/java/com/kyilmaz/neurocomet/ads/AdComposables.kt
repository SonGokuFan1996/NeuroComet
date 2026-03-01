package com.kyilmaz.neurocomet.ads

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.kyilmaz.neurocomet.R

/**
 * Composable wrapper for Google AdMob Banner Ads
 *
 * Automatically handles:
 * - Premium user check (no ads shown)
 * - Kids mode check (no ads shown)
 * - Loading states
 * - Error handling
 * - Cleanup on disposal
 *
 * @param modifier Modifier for the banner container
 * @param adSize The ad size to use (default: BANNER)
 * @param adKey Unique key for this banner instance (for caching)
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    adKey: String = "default_banner"
) {
    val context = LocalContext.current
    val adsState by GoogleAdsManager.adsState.collectAsState()

    // Don't show anything if ads are disabled
    if (!GoogleAdsManager.shouldShowAds()) {
        return
    }

    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var adView by remember { mutableStateOf<com.google.android.gms.ads.AdView?>(null) }

    // Create and load ad on first composition
    LaunchedEffect(adKey) {
        isLoading = true
        hasError = false
    }

    DisposableEffect(adKey) {
        onDispose {
            adView?.destroy()
            adView = null
        }
    }

    // Don't render anything if there's an error
    if (hasError) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(getBannerHeight(adSize))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }

        // Actual ad view - create directly in AndroidView
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                com.google.android.gms.ads.AdView(ctx).apply {
                    setAdSize(adSize)
                    // Use test ad unit ID in debug, production in release
                    adUnitId = if (adsState.useTestAds) {
                        "ca-app-pub-3940256099942544/6300978111" // Google test banner
                    } else {
                        "ca-app-pub-XXXXXXXX/XXXXXXXX" // Replace with production ID
                    }

                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            isLoading = false
                            hasError = false
                        }

                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            isLoading = false
                            hasError = true
                        }

                        override fun onAdClicked() {
                            // Ad clicked
                        }

                        override fun onAdImpression() {
                            // Ad impression recorded
                        }
                    }

                    // Load the ad
                    loadAd(com.google.android.gms.ads.AdRequest.Builder().build())

                    adView = this
                }
            },
            update = { view ->
                // Update if needed
            }
        )
    }
}

/**
 * Adaptive banner that fills the width of its container
 */
@Composable
fun AdaptiveBannerAd(
    modifier: Modifier = Modifier,
    adKey: String = "adaptive_banner"
) {
    val context = LocalContext.current
    val adsState by GoogleAdsManager.adsState.collectAsState()

    if (!GoogleAdsManager.shouldShowAds()) {
        return
    }

    // Calculate adaptive banner size
    val displayMetrics = context.resources.displayMetrics
    val adWidthPixels = displayMetrics.widthPixels
    val adWidth = (adWidthPixels / displayMetrics.density).toInt()
    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

    BannerAd(
        modifier = modifier,
        adSize = adaptiveSize,
        adKey = adKey
    )
}

/**
 * Button to show a rewarded ad
 *
 * @param modifier Modifier for the button
 * @param text Button text
 * @param onRewarded Callback when user earns reward
 * @param onAdNotReady Callback when ad is not ready
 */
@Composable
fun RewardedAdButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.ads_watch_to_support),
    onRewarded: (amount: Int, type: String) -> Unit,
    onAdNotReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val adsState by GoogleAdsManager.adsState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    // Preload rewarded ad if not loaded
    LaunchedEffect(Unit) {
        if (!adsState.rewardedLoaded) {
            GoogleAdsManager.loadRewardedAd(context)
        }
    }

    // Don't show if in kids mode
    if (adsState.kidsMode) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ads_disabled_kids),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Premium users see a different message
    if (adsState.isPremium && !adsState.forceShowAds) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.ads_premium_no_ads),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Button(
        onClick = {
            if (activity != null && GoogleAdsManager.isRewardedAdReady()) {
                isLoading = true
                GoogleAdsManager.showRewardedAd(activity) { amount, type ->
                    isLoading = false
                    onRewarded(amount, type)
                }
            } else {
                onAdNotReady()
                // Try to load the ad
                GoogleAdsManager.loadRewardedAd(context)
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = text)
                if (!adsState.rewardedLoaded) {
                    Text(
                        text = stringResource(R.string.ads_loading),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Ad container that can show banner or be empty for premium users
 * Useful for feed items or list items
 */
@Composable
fun FeedAdSlot(
    modifier: Modifier = Modifier,
    adKey: String,
    showEveryNItems: Int = 5,
    currentIndex: Int
) {
    // Only show ad at specific intervals
    if (currentIndex > 0 && currentIndex % showEveryNItems == 0) {
        BannerAd(
            modifier = modifier.padding(vertical = 8.dp),
            adKey = "${adKey}_$currentIndex"
        )
    }
}

// Helper function to get banner height based on ad size
private fun getBannerHeight(adSize: AdSize): androidx.compose.ui.unit.Dp {
    return when (adSize) {
        AdSize.BANNER -> 50.dp
        AdSize.LARGE_BANNER -> 100.dp
        AdSize.MEDIUM_RECTANGLE -> 250.dp
        AdSize.FULL_BANNER -> 60.dp
        AdSize.LEADERBOARD -> 90.dp
        else -> 60.dp // Default/adaptive
    }
}


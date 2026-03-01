@file:Suppress("unused")

package com.kyilmaz.neurocomet

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Performance optimizations for reducing jank in NeuroComet.
 *
 * Key optimizations:
 * 1. Emulator detection for reduced animation complexity
 * 2. Smart recomposition throttling
 * 3. Memory-efficient state management
 * 4. Frame-rate aware animation timing
 * 5. Low-RAM device detection
 * 6. Prefetch distance tuning
 */
object PerformanceOptimizations {

    // Cached values to avoid repeated checks
    private var cachedIsEmulator: Boolean? = null
    private var cachedIsLowRamDevice: Boolean? = null

    /**
     * Check if running on emulator where GPU performance is limited.
     * Result is cached after first call.
     */
    @JvmStatic
    fun isEmulator(): Boolean {
        return cachedIsEmulator ?: run {
            val result = Build.FINGERPRINT.startsWith("generic") ||
                   Build.FINGERPRINT.startsWith("unknown") ||
                   Build.MODEL.contains("google_sdk") ||
                   Build.MODEL.contains("Emulator") ||
                   Build.MODEL.contains("Android SDK built for x86") ||
                   Build.MANUFACTURER.contains("Genymotion") ||
                   Build.HARDWARE.contains("goldfish") ||
                   Build.HARDWARE.contains("ranchu") ||
                   Build.PRODUCT.contains("sdk_google") ||
                   Build.PRODUCT.contains("google_sdk") ||
                   Build.PRODUCT.contains("sdk") ||
                   Build.PRODUCT.contains("sdk_x86") ||
                   Build.PRODUCT.contains("sdk_gphone64_arm64") ||
                   Build.PRODUCT.contains("vbox86p") ||
                   Build.PRODUCT.contains("emulator") ||
                   Build.PRODUCT.contains("simulator")
            cachedIsEmulator = result
            result
        }
    }

    /**
     * Check if the device is a low-RAM device.
     * These devices need reduced image quality and smaller caches.
     */
    @JvmStatic
    fun isLowRamDevice(context: Context): Boolean {
        return cachedIsLowRamDevice ?: run {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val result = activityManager?.isLowRamDevice ?: false
            cachedIsLowRamDevice = result
            result
        }
    }

    /**
     * Get recommended animation duration multiplier based on device capabilities.
     * Emulators and low-end devices get faster animations (shorter duration = less frames).
     */
    @JvmStatic
    fun getAnimationDurationMultiplier(): Float {
        return if (isEmulator()) 0.5f else 1.0f
    }

    /**
     * Get recommended frame delay for game loops.
     * Emulators need longer delays to prevent frame dropping.
     */
    @JvmStatic
    fun getGameLoopDelayMs(): Long {
        return if (isEmulator()) 32L else 16L // 30fps vs 60fps
    }

    /**
     * Get recommended prefetch distance for lazy lists based on device capability.
     * Higher-end devices can prefetch more items for smoother scrolling.
     */
    @JvmStatic
    fun getListPrefetchDistance(context: Context): Int {
        return when {
            isLowRamDevice(context) -> 2
            isEmulator() -> 3
            else -> 5
        }
    }

    /**
     * Get recommended image cache percentage based on device RAM.
     */
    @JvmStatic
    fun getImageCachePercentage(context: Context): Double {
        return when {
            isLowRamDevice(context) -> 0.15
            isEmulator() -> 0.20
            else -> 0.30
        }
    }

    /**
     * Check if complex animations should be reduced.
     */
    @JvmStatic
    fun shouldReduceAnimations(context: Context): Boolean {
        if (isEmulator()) return true

        // Check system animation scale
        val animationScale = try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        } catch (_: Exception) {
            1.0f
        }

        return animationScale < 1.0f
    }
}

/**
 * Composable that provides performance-aware settings.
 */
@Composable
fun rememberPerformanceSettings(): PerformanceSettings {
    val context = LocalContext.current

    return remember {
        PerformanceSettings(
            isEmulator = PerformanceOptimizations.isEmulator(),
            isLowRamDevice = PerformanceOptimizations.isLowRamDevice(context),
            animationMultiplier = PerformanceOptimizations.getAnimationDurationMultiplier(),
            gameLoopDelay = PerformanceOptimizations.getGameLoopDelayMs(),
            shouldReduceAnimations = PerformanceOptimizations.shouldReduceAnimations(context),
            listPrefetchDistance = PerformanceOptimizations.getListPrefetchDistance(context)
        )
    }
}

/**
 * Immutable performance settings data class.
 */
@Stable
data class PerformanceSettings(
    val isEmulator: Boolean,
    val isLowRamDevice: Boolean,
    val animationMultiplier: Float,
    val gameLoopDelay: Long,
    val shouldReduceAnimations: Boolean,
    val listPrefetchDistance: Int
)

/**
 * Smart scroll state observer that reduces recomposition during fast scrolling.
 * Uses derivedStateOf and throttling to minimize jank.
 */
@Composable
fun rememberThrottledScrollState(
    listState: LazyListState,
    throttleMs: Long = 100L
): ThrottledScrollState {
    val isScrolling by remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    val firstVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    // Throttled index updates to prevent excessive recomposition
    var throttledIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(firstVisibleIndex) {
        delay(throttleMs)
        if (isActive) {
            throttledIndex = firstVisibleIndex
        }
    }

    return remember(isScrolling, throttledIndex) {
        ThrottledScrollState(
            isScrolling = isScrolling,
            firstVisibleIndex = throttledIndex
        )
    }
}

@Stable
data class ThrottledScrollState(
    val isScrolling: Boolean,
    val firstVisibleIndex: Int
)


/**
 * Modifier for optimized list item rendering.
 * Adds performance hints for items in lazy lists.
 */
object ListItemOptimizations {

    /**
     * Get cache policy recommendations for list images.
     */
    fun getImageCachePolicy(isScrolling: Boolean): coil.request.CachePolicy {
        return if (isScrolling) {
            // During scroll, prefer memory cache to avoid decode overhead
            coil.request.CachePolicy.ENABLED
        } else {
            coil.request.CachePolicy.ENABLED
        }
    }

    /**
     * Check if animations should be paused for list items.
     */
    fun shouldPauseAnimations(isScrolling: Boolean, isEmulator: Boolean): Boolean {
        return isScrolling || isEmulator
    }
}

/**
 * Recomposition tracker for debugging performance issues.
 * Only active in debug builds.
 */
@Composable
fun RecompositionTracker(
    tag: String,
    enabled: Boolean = BuildConfig.DEBUG
) {
    if (!enabled) return

    val recomposeCount = remember { mutableIntStateOf(0) }
    SideEffect {
        recomposeCount.intValue++
        if (recomposeCount.intValue % 10 == 0) {
            android.util.Log.d("Recompose", "$tag: ${recomposeCount.intValue} recompositions")
        }
    }
}

/**
 * Extension function to create a stable key for list items.
 */
fun Any?.stableKey(): Any {
    return when (this) {
        null -> System.identityHashCode(this)
        is String -> this
        is Number -> this
        else -> this.hashCode()
    }
}


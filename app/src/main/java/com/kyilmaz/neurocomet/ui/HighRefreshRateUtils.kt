package com.kyilmaz.neurocomet.ui

import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * High Refresh Rate Utilities
 *
 * Optimizations for smooth scrolling on high refresh rate displays (90Hz, 120Hz, 144Hz, etc.)
 *
 * Key optimizations:
 * 1. Reduced frame interpolation for smoother perceived motion
 * 2. Adjusted fling decay for natural feel at higher refresh rates
 * 3. Lower stiffness springs for smoother animations
 */

/**
 * Display refresh rate categories
 */
enum class RefreshRateCategory {
    STANDARD,    // 60Hz
    HIGH,        // 90Hz
    VERY_HIGH,   // 120Hz+
}

/**
 * Get display refresh rate using modern API
 */
private fun getDisplayRefreshRate(context: android.content.Context): Float {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ - Use display from context
            val display = context.display ?: run {
                val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            display.refreshRate
        } else {
            // Older API - use deprecated method
            val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.refreshRate
        }
    } catch (e: Exception) {
        60f
    }
}

/**
 * Get the current display refresh rate category
 */
@Composable
fun rememberRefreshRateCategory(): RefreshRateCategory {
    val context = LocalContext.current
    return remember {
        val refreshRate = getDisplayRefreshRate(context)
        when {
            refreshRate >= 120f -> RefreshRateCategory.VERY_HIGH
            refreshRate >= 90f -> RefreshRateCategory.HIGH
            else -> RefreshRateCategory.STANDARD
        }
    }
}

/**
 * Get the actual display refresh rate in Hz
 */
@Composable
fun rememberDisplayRefreshRate(): Float {
    val context = LocalContext.current
    return remember {
        getDisplayRefreshRate(context)
    }
}

/**
 * Optimized spring animation spec for high refresh rate displays
 * Uses lower stiffness for smoother perceived motion at high refresh rates
 */
@Composable
fun <T> rememberOptimizedSpring(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMedium,
    visibilityThreshold: T? = null
): AnimationSpec<T> {
    val refreshRateCategory = rememberRefreshRateCategory()

    // Adjust stiffness based on refresh rate
    // Higher refresh rates benefit from lower stiffness for smoother motion
    val adjustedStiffness = when (refreshRateCategory) {
        RefreshRateCategory.VERY_HIGH -> stiffness * 0.7f  // 30% softer for 120Hz+
        RefreshRateCategory.HIGH -> stiffness * 0.85f      // 15% softer for 90Hz
        RefreshRateCategory.STANDARD -> stiffness
    }

    return spring(
        dampingRatio = dampingRatio,
        stiffness = adjustedStiffness,
        visibilityThreshold = visibilityThreshold
    )
}

/**
 * Get optimal scroll parameters for the current display
 */
data class OptimalScrollParameters(
    val velocityThreshold: Float,
    val decelerationRate: Float,
    val overscrollDistance: Float
)

@Composable
fun rememberOptimalScrollParameters(): OptimalScrollParameters {
    val refreshRateCategory = rememberRefreshRateCategory()
    val density = LocalDensity.current

    return remember(refreshRateCategory) {
        when (refreshRateCategory) {
            RefreshRateCategory.VERY_HIGH -> OptimalScrollParameters(
                velocityThreshold = 0.5f,      // More responsive to small movements
                decelerationRate = 0.992f,      // Slightly slower deceleration for smoothness
                overscrollDistance = with(density) { 48f }
            )
            RefreshRateCategory.HIGH -> OptimalScrollParameters(
                velocityThreshold = 0.75f,
                decelerationRate = 0.994f,
                overscrollDistance = with(density) { 40f }
            )
            RefreshRateCategory.STANDARD -> OptimalScrollParameters(
                velocityThreshold = 1f,
                decelerationRate = 0.998f,
                overscrollDistance = with(density) { 32f }
            )
        }
    }
}

/**
 * Composable that provides information about the current display for debugging
 */
@Composable
fun rememberDisplayInfo(): DisplayInfo {
    val context = LocalContext.current
    return remember {
        try {
            val windowManager = context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
            val refreshRate = getDisplayRefreshRate(context)

            val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                android.util.DisplayMetrics().apply {
                    widthPixels = bounds.width()
                    heightPixels = bounds.height()
                    densityDpi = context.resources.displayMetrics.densityDpi
                }
            } else {
                android.util.DisplayMetrics().also { metrics ->
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay.getRealMetrics(metrics)
                }
            }

            DisplayInfo(
                refreshRate = refreshRate,
                widthPixels = metrics.widthPixels,
                heightPixels = metrics.heightPixels,
                densityDpi = metrics.densityDpi,
                isHighRefreshRate = refreshRate > 60f
            )
        } catch (e: Exception) {
            DisplayInfo(
                refreshRate = 60f,
                widthPixels = 0,
                heightPixels = 0,
                densityDpi = 160,
                isHighRefreshRate = false
            )
        }
    }
}

data class DisplayInfo(
    val refreshRate: Float,
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val isHighRefreshRate: Boolean
) {
    val refreshRateFormatted: String
        get() = "${refreshRate.toInt()}Hz"

    val resolutionFormatted: String
        get() = "${widthPixels}x${heightPixels}"
}


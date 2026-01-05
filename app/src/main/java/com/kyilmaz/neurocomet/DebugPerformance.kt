package com.kyilmaz.neurocomet

import android.view.Choreographer
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Debug Performance Overlay for NeuroComet
 *
 * Features:
 * - Real-time FPS monitoring
 * - Frame drop detection
 * - Scroll jank detection
 * - Memory usage tracking
 * - Battery-efficient sampling
 */

// ═══════════════════════════════════════════════════════════════
// PERFORMANCE OVERLAY STATE (Global toggle with persistence)
// ═══════════════════════════════════════════════════════════════

/**
 * Global state for controlling the performance overlay.
 * Can be toggled from Developer Options.
 * Settings are persisted to SharedPreferences.
 */
object PerformanceOverlayState {
    private const val PREFS_NAME = "performance_overlay_prefs"
    private const val KEY_ENABLED = "overlay_enabled"
    private const val KEY_SCROLL_JANK = "scroll_jank_enabled"

    private var _isEnabled by mutableStateOf(false)
    private var _scrollJankDetectionEnabled by mutableStateOf(true)

    var isEnabled: Boolean
        get() = _isEnabled
        set(value) {
            _isEnabled = value
            saveToPrefs()
        }

    var scrollJankDetectionEnabled: Boolean
        get() = _scrollJankDetectionEnabled
        set(value) {
            _scrollJankDetectionEnabled = value
            saveToPrefs()
        }

    private var context: android.content.Context? = null

    /**
     * Initialize with context to load/save preferences.
     * Should be called from Application.onCreate or MainActivity.
     */
    fun init(ctx: android.content.Context) {
        context = ctx.applicationContext
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            _isEnabled = prefs.getBoolean(KEY_ENABLED, false)
            _scrollJankDetectionEnabled = prefs.getBoolean(KEY_SCROLL_JANK, true)
        }
    }

    private fun saveToPrefs() {
        context?.let { ctx ->
            ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, _isEnabled)
                .putBoolean(KEY_SCROLL_JANK, _scrollJankDetectionEnabled)
                .apply()
        }
    }

    /**
     * Reset all performance overlay settings to defaults.
     */
    fun resetToDefaults() {
        _isEnabled = false
        _scrollJankDetectionEnabled = true
        saveToPrefs()
    }
}

// ═══════════════════════════════════════════════════════════════
// PERFORMANCE MONITORING STATE
// ═══════════════════════════════════════════════════════════════

object PerformanceMonitor {
    // Frame timing
    private var lastFrameTimeNanos = 0L
    private var frameCount = 0
    private var totalFrameTimeNanos = 0L
    private var droppedFrames = 0

    // Target: 60 FPS = 16.67ms per frame
    private const val TARGET_FRAME_TIME_NANOS = 16_666_666L // 16.67ms
    private const val JANK_THRESHOLD_NANOS = 32_000_000L // 32ms = dropped frame

    // Public state
    private val _currentFps = mutableFloatStateOf(60f)
    val currentFps: State<Float> = _currentFps

    private val _droppedFrameCount = mutableIntStateOf(0)
    val droppedFrameCount: State<Int> = _droppedFrameCount

    private val _isJanking = mutableStateOf(false)
    val isJanking: State<Boolean> = _isJanking

    private val _memoryUsageMb = mutableFloatStateOf(0f)
    val memoryUsageMb: State<Float> = _memoryUsageMb

    private val _scrollVelocity = mutableFloatStateOf(0f)
    val scrollVelocity: State<Float> = _scrollVelocity

    private val _scrollJankDetected = mutableStateOf(false)
    val scrollJankDetected: State<Boolean> = _scrollJankDetected

    // Scroll tracking
    private var lastScrollOffset = 0
    private var lastScrollTime = 0L
    private var scrollSamples = mutableListOf<Float>()

    /**
     * Record a frame and update FPS calculations.
     * Uses Choreographer for accurate frame timing.
     */
    fun recordFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos > 0) {
            val frameDurationNanos = frameTimeNanos - lastFrameTimeNanos
            totalFrameTimeNanos += frameDurationNanos
            frameCount++

            // Detect dropped frames (jank)
            if (frameDurationNanos > JANK_THRESHOLD_NANOS) {
                droppedFrames++
                _droppedFrameCount.intValue = droppedFrames
                _isJanking.value = true
            } else {
                _isJanking.value = false
            }

            // Calculate FPS every 30 frames for smoother updates
            if (frameCount >= 30) {
                val avgFrameTimeNanos = totalFrameTimeNanos / frameCount
                _currentFps.floatValue = (1_000_000_000f / avgFrameTimeNanos).coerceIn(0f, 120f)
                frameCount = 0
                totalFrameTimeNanos = 0
            }
        }
        lastFrameTimeNanos = frameTimeNanos
    }

    /**
     * Update memory usage statistics.
     */
    fun updateMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        _memoryUsageMb.floatValue = usedMemory
    }

    /**
     * Track scroll performance.
     */
    fun trackScroll(currentOffset: Int) {
        val now = System.nanoTime()
        if (lastScrollTime > 0) {
            val timeDelta = (now - lastScrollTime) / 1_000_000f // ms
            val offsetDelta = kotlin.math.abs(currentOffset - lastScrollOffset)

            if (timeDelta > 0) {
                val velocity = offsetDelta / timeDelta * 1000f // px/s
                _scrollVelocity.floatValue = velocity

                // Track scroll samples for jank detection
                scrollSamples.add(timeDelta)
                if (scrollSamples.size > 10) {
                    scrollSamples.removeAt(0)
                }

                // Detect scroll jank (inconsistent frame timing)
                if (scrollSamples.size >= 5) {
                    val avg = scrollSamples.average()
                    val variance = scrollSamples.map { (it - avg) * (it - avg) }.average()
                    _scrollJankDetected.value = variance > 100 // High variance = jank
                }
            }
        }
        lastScrollOffset = currentOffset
        lastScrollTime = now
    }

    /**
     * Reset all counters.
     */
    fun reset() {
        lastFrameTimeNanos = 0L
        frameCount = 0
        totalFrameTimeNanos = 0
        droppedFrames = 0
        _currentFps.floatValue = 60f
        _droppedFrameCount.intValue = 0
        _isJanking.value = false
        scrollSamples.clear()
    }
}

// ═══════════════════════════════════════════════════════════════
// DEBUG OVERLAY COMPOSABLE
// ═══════════════════════════════════════════════════════════════

@Composable
fun DebugPerformanceOverlay(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    val fps by PerformanceMonitor.currentFps
    val droppedFrames by PerformanceMonitor.droppedFrameCount
    val isJanking by PerformanceMonitor.isJanking
    val memoryMb by PerformanceMonitor.memoryUsageMb
    val scrollVelocity by PerformanceMonitor.scrollVelocity
    val scrollJank by PerformanceMonitor.scrollJankDetected

    // Start frame callback
    DisposableEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        var isRunning = true

        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (isRunning) {
                    PerformanceMonitor.recordFrame(frameTimeNanos)
                    choreographer.postFrameCallback(this)
                }
            }
        }

        choreographer.postFrameCallback(frameCallback)

        onDispose {
            isRunning = false
            // Choreographer will stop calling our callback
        }
    }

    // Update memory usage periodically
    LaunchedEffect(Unit) {
        while (isActive) {
            PerformanceMonitor.updateMemoryUsage()
            delay(1000) // Every second
        }
    }

    // Overlay UI
    Box(
        modifier = modifier
            .padding(8.dp)
            .background(
                Color.Black.copy(alpha = 0.75f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // FPS indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val fpsColor = when {
                    fps >= 55 -> Color(0xFF4CAF50) // Green
                    fps >= 45 -> Color(0xFFFFEB3B) // Yellow
                    fps >= 30 -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(fpsColor, RoundedCornerShape(4.dp))
                )

                Text(
                    text = "FPS: ${fps.toInt()}",
                    color = fpsColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (isJanking) {
                    Text(
                        text = "⚠️ JANK",
                        color = Color(0xFFF44336),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dropped frames
            Text(
                text = "Drops: $droppedFrames",
                color = if (droppedFrames > 10) Color(0xFFFF9800) else Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Memory usage
            Text(
                text = "Mem: ${String.format("%.1f", memoryMb)} MB",
                color = if (memoryMb > 200) Color(0xFFFF9800) else Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Scroll info
            if (scrollVelocity > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Scroll: ${scrollVelocity.toInt()} px/s",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (scrollJank) {
                        Text(
                            text = "⚡",
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SCROLL PERFORMANCE TRACKER
// ═══════════════════════════════════════════════════════════════

/**
 * Modifier extension to track scroll performance.
 */
@Composable
fun Modifier.trackScrollPerformance(
    listState: LazyListState,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this

    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val scrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    LaunchedEffect(firstVisibleIndex, scrollOffset) {
        val totalOffset = firstVisibleIndex * 1000 + scrollOffset // Approximate
        PerformanceMonitor.trackScroll(totalOffset)
    }

    return this
}

// ═══════════════════════════════════════════════════════════════
// BATTERY OPTIMIZATION UTILITIES
// ═══════════════════════════════════════════════════════════════

/**
 * Battery optimization tips and utilities.
 *
 * Key optimizations already applied in NeuroComet:
 * 1. Lazy loading in lists (LazyColumn, LazyRow)
 * 2. remember {} for expensive computations
 * 3. derivedStateOf for computed values
 * 4. Reduced animation complexity option
 * 5. Dark theme support (saves battery on OLED)
 * 6. Coil image caching
 * 7. Debounced input handlers
 * 8. Background work using WorkManager
 */
object BatteryOptimizations {

    /**
     * Check if device is in low battery mode.
     */
    fun isLowBatteryMode(context: android.content.Context): Boolean {
        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
        return powerManager?.isPowerSaveMode == true
    }

    /**
     * Get recommended animation scale based on battery.
     */
    fun getRecommendedAnimationScale(context: android.content.Context): Float {
        return if (isLowBatteryMode(context)) 0.5f else 1.0f
    }

    /**
     * Get recommended refresh interval for periodic updates.
     */
    fun getRecommendedRefreshIntervalMs(context: android.content.Context): Long {
        return if (isLowBatteryMode(context)) 60_000L else 30_000L // 1 min vs 30 sec
    }
}

// ═══════════════════════════════════════════════════════════════
// DEBUG SETTINGS PANEL
// ═══════════════════════════════════════════════════════════════

@Composable
fun DebugSettingsPanel(
    showPerformanceOverlay: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🔧 Debug Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Performance Overlay",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Shows FPS, memory, and scroll stats",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showPerformanceOverlay,
                    onCheckedChange = onToggleOverlay
                )
            }

            HorizontalDivider()

            // Reset button
            OutlinedButton(
                onClick = { PerformanceMonitor.reset() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Reset Counters")
            }
        }
    }
}


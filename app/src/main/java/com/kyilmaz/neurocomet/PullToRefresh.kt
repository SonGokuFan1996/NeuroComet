package com.kyilmaz.neurocomet

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

/**
 * Pull-to-Refresh System for NeuroComet
 *
 * Neurodivergent-Friendly Design:
 * - Clear visual feedback
 * - Gentle haptic feedback at threshold
 * - Customizable trigger distance
 * - Emoji-based indicators (optional)
 * - Reduced motion support
 * - High contrast mode
 */

// ═══════════════════════════════════════════════════════════════
// PULL-TO-REFRESH STATE
// ═══════════════════════════════════════════════════════════════

enum class PullRefreshState {
    IDLE,           // Not pulling
    PULLING,        // User is pulling down
    THRESHOLD,      // Pull distance exceeds threshold, ready to refresh
    REFRESHING,     // Currently refreshing
    COMPLETING      // Refresh complete, animating back
}

class NeuroPullRefreshState {
    var state by mutableStateOf(PullRefreshState.IDLE)
        private set

    var pullDistance by mutableFloatStateOf(0f)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    fun updatePullDistance(delta: Float, maxPull: Float) {
        pullDistance = (pullDistance + delta).coerceIn(0f, maxPull)
        state = when {
            isRefreshing -> PullRefreshState.REFRESHING
            pullDistance > maxPull * 0.6f -> PullRefreshState.THRESHOLD
            pullDistance > 0f -> PullRefreshState.PULLING
            else -> PullRefreshState.IDLE
        }
    }

    fun startRefreshing() {
        isRefreshing = true
        state = PullRefreshState.REFRESHING
    }

    fun completeRefreshing() {
        isRefreshing = false
        state = PullRefreshState.COMPLETING
        pullDistance = 0f
        state = PullRefreshState.IDLE
    }

    fun cancelPull() {
        if (!isRefreshing) {
            pullDistance = 0f
            state = PullRefreshState.IDLE
        }
    }
}

@Composable
fun rememberNeuroPullRefreshState(): NeuroPullRefreshState {
    return remember { NeuroPullRefreshState() }
}

// ═══════════════════════════════════════════════════════════════
// PULL-TO-REFRESH CONTAINER
// ═══════════════════════════════════════════════════════════════

/**
 * A neurodivergent-friendly pull-to-refresh container
 *
 * @param isRefreshing Whether the content is currently refreshing
 * @param onRefresh Callback when refresh is triggered
 * @param modifier Modifier for the container
 * @param enabled Whether pull-to-refresh is enabled
 * @param pullThreshold Distance to pull before refresh triggers
 * @param maxPullDistance Maximum pull distance
 * @param showEmoji Show emoji indicator instead of spinner
 * @param reducedMotion Disable animations
 * @param highContrast Use high contrast colors
 * @param content The scrollable content
 */
@Composable
fun NeuroPullRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pullThreshold: Dp = 80.dp,
    maxPullDistance: Dp = 150.dp,
    showEmoji: Boolean = true,
    reducedMotion: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val refreshState = rememberNeuroPullRefreshState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val pullThresholdPx = with(density) { pullThreshold.toPx() }
    val maxPullPx = with(density) { maxPullDistance.toPx() }

    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Sync with external isRefreshing state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            refreshState.startRefreshing()
        } else {
            refreshState.completeRefreshing()
            hasTriggeredHaptic = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectVerticalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        if (refreshState.state == PullRefreshState.THRESHOLD && !refreshState.isRefreshing) {
                            refreshState.startRefreshing()
                            onRefresh()
                        } else {
                            refreshState.cancelPull()
                        }
                    },
                    onDragCancel = {
                        refreshState.cancelPull()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0 && !refreshState.isRefreshing) {
                            // User is pulling down
                            refreshState.updatePullDistance(dragAmount * 0.5f, maxPullPx)

                            // Haptic feedback at threshold
                            if (refreshState.pullDistance > pullThresholdPx && !hasTriggeredHaptic) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasTriggeredHaptic = true
                            } else if (refreshState.pullDistance < pullThresholdPx) {
                                hasTriggeredHaptic = false
                            }

                            change.consume()
                        }
                    }
                )
            }
    ) {
        // Main content with offset based on pull
        val contentOffset = if (refreshState.isRefreshing) {
            with(density) { 60.dp.toPx() }
        } else {
            refreshState.pullDistance
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = contentOffset * 0.3f
                }
        ) {
            content()
        }

        // Pull indicator
        PullRefreshIndicator(
            state = refreshState.state,
            pullDistance = refreshState.pullDistance,
            threshold = pullThresholdPx,
            showEmoji = showEmoji,
            reducedMotion = reducedMotion,
            highContrast = highContrast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun PullRefreshIndicator(
    state: PullRefreshState,
    pullDistance: Float,
    threshold: Float,
    showEmoji: Boolean,
    reducedMotion: Boolean,
    highContrast: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = min(pullDistance / threshold, 1f)
    val indicatorOffset = min(pullDistance, threshold) - 60f

    // Animation for spinner
    val rotation by if (!reducedMotion && state == PullRefreshState.REFRESHING) {
        val infiniteTransition = rememberInfiniteTransition(label = "refresh")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = when (state) {
            PullRefreshState.IDLE -> 0f
            PullRefreshState.PULLING -> 0.5f + (progress * 0.5f)
            PullRefreshState.THRESHOLD -> 1.1f
            PullRefreshState.REFRESHING -> 1f
            PullRefreshState.COMPLETING -> 0f
        },
        animationSpec = if (reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    if (state != PullRefreshState.IDLE || pullDistance > 0) {
        Box(
            modifier = modifier
                .offset(y = with(LocalDensity.current) { indicatorOffset.coerceAtLeast(0f).toDp() })
                .padding(top = 16.dp)
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = if (state == PullRefreshState.REFRESHING) rotation else progress * 180f
                }
                .clip(CircleShape)
                .background(
                    if (highContrast) Color.White
                    else if (state == PullRefreshState.THRESHOLD || state == PullRefreshState.REFRESHING)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                showEmoji -> {
                    val emoji = when (state) {
                        PullRefreshState.IDLE -> "⬇️"
                        PullRefreshState.PULLING -> "⬇️"
                        PullRefreshState.THRESHOLD -> "✨"
                        PullRefreshState.REFRESHING -> "🔄"
                        PullRefreshState.COMPLETING -> "✅"
                    }
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                state == PullRefreshState.REFRESHING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = if (highContrast) Color.Black else Color.White
                    )
                }
                else -> {
                    Icon(
                        if (state == PullRefreshState.THRESHOLD) Icons.Filled.Refresh
                        else Icons.Filled.ArrowDownward,
                        contentDescription = "Pull to refresh",
                        tint = if (highContrast) Color.Black
                               else if (state == PullRefreshState.THRESHOLD) Color.White
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(if (state == PullRefreshState.THRESHOLD) 0f else progress * 180f)
                    )
                }
            }
        }
    }

    // Status text
    if (state != PullRefreshState.IDLE) {
        Box(
            modifier = Modifier
                .offset(y = with(LocalDensity.current) { (indicatorOffset + 56f).coerceAtLeast(16f).toDp() })
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = when (state) {
                    PullRefreshState.IDLE -> ""
                    PullRefreshState.PULLING -> "Pull to refresh"
                    PullRefreshState.THRESHOLD -> "Release to refresh"
                    PullRefreshState.REFRESHING -> "Refreshing..."
                    PullRefreshState.COMPLETING -> "Done!"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (state == PullRefreshState.THRESHOLD) FontWeight.Bold else FontWeight.Normal,
                color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SIMPLE PULL-TO-REFRESH WRAPPER
// ═══════════════════════════════════════════════════════════════

/**
 * A simpler pull-to-refresh wrapper using the custom implementation
 */
@Composable
fun SimplePullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    NeuroPullRefreshContainer(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        enabled = enabled,
        showEmoji = false,
        content = content
    )
}


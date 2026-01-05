package com.kyilmaz.neurocomet

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Error Boundary and Fallback UI System for NeuroComet
 *
 * Neurodivergent-Friendly Error Handling:
 * - Clear, non-alarming error messages
 * - Calming visual design (no harsh red errors)
 * - Emoji-based communication
 * - Simple recovery options
 * - Detailed info available but hidden by default
 * - Graceful degradation
 */

private const val TAG = "ErrorBoundary"

// ═══════════════════════════════════════════════════════════════
// ERROR TYPES AND DATA
// ═══════════════════════════════════════════════════════════════

enum class ErrorSeverity {
    INFO,       // Minor issue, feature still works
    WARNING,    // Some functionality affected
    ERROR,      // Feature not working, fallback shown
    CRITICAL    // Major issue, needs app restart
}

data class ErrorInfo(
    val title: String,
    val message: String,
    val emoji: String = "😅",
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val exception: Throwable? = null,
    val recoveryAction: String? = "Try Again",
    val technicalDetails: String? = null
)

// ═══════════════════════════════════════════════════════════════
// ERROR BOUNDARY COMPOSABLE
// ═══════════════════════════════════════════════════════════════

/**
 * Wraps content and provides a way to show fallback UI on error
 * Note: Compose doesn't support try-catch around composable functions.
 * Use this with manual error state management.
 *
 * @param error The current error state (null if no error)
 * @param onClearError Callback to clear the error and retry
 * @param fallbackContent The UI to show when an error occurs
 * @param content The main content to render
 */
@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    error: Throwable? = null,
    onClearError: () -> Unit = {},
    fallbackContent: @Composable (ErrorInfo, () -> Unit) -> Unit = { errorInfo, retry ->
        DefaultFallbackUI(error = errorInfo, onRetry = retry)
    },
    content: @Composable () -> Unit
) {
    if (error != null) {
        val errorInfo = ErrorInfo(
            title = "Something went wrong",
            message = "We couldn't load this content. Don't worry, your data is safe!",
            emoji = "🔧",
            exception = error,
            technicalDetails = error.stackTraceToString().take(500)
        )

        fallbackContent(errorInfo) {
            onClearError()
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * State holder for error boundary
 */
class ErrorBoundaryState {
    var error by mutableStateOf<Throwable?>(null)
        private set

    fun captureError(e: Throwable) {
        error = e
        Log.e(TAG, "Error captured in ErrorBoundaryState", e)
    }

    fun clearError() {
        error = null
    }

    /**
     * Run a block safely, capturing any exception
     */
    inline fun <T> runCatching(fallback: T, block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            captureError(e)
            fallback
        }
    }
}

@Composable
fun rememberErrorBoundaryState(): ErrorBoundaryState {
    return remember { ErrorBoundaryState() }
}

/**
 * Simple error boundary using state
 */
@Composable
fun ErrorBoundaryWithState(
    modifier: Modifier = Modifier,
    state: ErrorBoundaryState = rememberErrorBoundaryState(),
    fallbackContent: @Composable (ErrorInfo, () -> Unit) -> Unit = { errorInfo, retry ->
        DefaultFallbackUI(error = errorInfo, onRetry = retry)
    },
    content: @Composable () -> Unit
) {
    ErrorBoundary(
        modifier = modifier,
        error = state.error,
        onClearError = { state.clearError() },
        fallbackContent = fallbackContent,
        content = content
    )
}

/**
 * Safe composable wrapper that catches errors and shows fallback
 * Uses ErrorBoundaryWithState for automatic error management
 */
@Composable
fun SafeContent(
    modifier: Modifier = Modifier,
    fallbackTitle: String = "Content unavailable",
    fallbackMessage: String = "We're having trouble loading this section",
    fallbackEmoji: String = "🔄",
    content: @Composable () -> Unit
) {
    val errorState = rememberErrorBoundaryState()

    ErrorBoundaryWithState(
        modifier = modifier,
        state = errorState,
        fallbackContent = { _, retry ->
            MinimalFallbackUI(
                title = fallbackTitle,
                message = fallbackMessage,
                emoji = fallbackEmoji,
                onRetry = retry
            )
        },
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════
// DEFAULT FALLBACK UI
// ═══════════════════════════════════════════════════════════════

/**
 * Default fallback UI shown when an error occurs
 * Neurodivergent-friendly design with calming colors
 */
@Composable
fun DefaultFallbackUI(
    error: ErrorInfo,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showTechnicalDetails: Boolean = false
) {
    var showDetails by remember { mutableStateOf(false) }

    val backgroundColor = when (error.severity) {
        ErrorSeverity.INFO -> MaterialTheme.colorScheme.primaryContainer
        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.surfaceVariant
        ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated emoji
            val infiniteTransition = rememberInfiniteTransition(label = "bounce")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "emojiScale"
            )

            Text(
                text = error.emoji,
                fontSize = 64.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = error.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            // Message
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Retry button
            if (error.recoveryAction != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(error.recoveryAction, fontWeight = FontWeight.SemiBold)
                }
            }

            // Technical details toggle (for developers/advanced users)
            if (showTechnicalDetails && error.technicalDetails != null) {
                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { showDetails = !showDetails }) {
                    Icon(
                        if (showDetails) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (showDetails) "Hide Details" else "Show Technical Details")
                }

                AnimatedVisibility(visible = showDetails) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = error.technicalDetails,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MINIMAL FALLBACK UI
// ═══════════════════════════════════════════════════════════════

/**
 * Compact fallback UI for inline errors
 */
@Composable
fun MinimalFallbackUI(
    title: String,
    message: String,
    emoji: String = "😅",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onRetry != null) {
                IconButton(onClick = onRetry) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Retry",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// FULL SCREEN FALLBACK
// ═══════════════════════════════════════════════════════════════

/**
 * Full screen fallback for major errors
 */
@Composable
fun FullScreenFallback(
    title: String = "Oops! Something unexpected happened",
    message: String = "Don't worry, your data is safe. We just need a moment to fix things.",
    emoji: String = "🛠️",
    onRetry: (() -> Unit)? = null,
    onGoHome: (() -> Unit)? = null,
    showSupportOption: Boolean = true
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            // Calming animation
            val infiniteTransition = rememberInfiniteTransition(label = "calm")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "gentleRotate"
            )

            Text(
                text = emoji,
                fontSize = 80.sp,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(32.dp))

            // Primary action
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Try Again", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
            }

            // Secondary action - go home
            if (onGoHome != null) {
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Go to Home", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(12.dp))
            }

            // Support option
            if (showSupportOption) {
                TextButton(
                    onClick = {
                        // Open support/feedback
                        android.widget.Toast.makeText(
                            context,
                            "Support coming soon!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Get Help")
                }
            }

            Spacer(Modifier.height(48.dp))

            // Reassuring message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "This is just a temporary hiccup. The app is working on fixing itself!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LOADING WITH FALLBACK
// ═══════════════════════════════════════════════════════════════

/**
 * Loading state with automatic fallback after timeout
 */
@Composable
fun LoadingWithFallback(
    isLoading: Boolean,
    loadingTimeoutMs: Long = 15000L,
    onTimeout: () -> Unit = {},
    loadingContent: @Composable () -> Unit = { DefaultLoadingContent() },
    timeoutContent: @Composable (onRetry: () -> Unit) -> Unit = { retry ->
        MinimalFallbackUI(
            title = "Taking longer than expected",
            message = "The content is still loading. You can wait or try again.",
            emoji = "⏳",
            onRetry = retry
        )
    },
    content: @Composable () -> Unit
) {
    var hasTimedOut by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isLoading, retryCount) {
        if (isLoading) {
            hasTimedOut = false
            delay(loadingTimeoutMs)
            if (isLoading) {
                hasTimedOut = true
                onTimeout()
            }
        }
    }

    when {
        !isLoading -> content()
        hasTimedOut -> timeoutContent {
            hasTimedOut = false
            retryCount++
        }
        else -> loadingContent()
    }
}

@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPTY STATE FALLBACK
// ═══════════════════════════════════════════════════════════════

/**
 * Empty state UI when there's no content
 */
@Composable
fun EmptyStateFallback(
    title: String = "Nothing here yet",
    message: String = "This space is waiting to be filled!",
    emoji: String = "📭",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 64.sp)

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// OFFLINE FALLBACK
// ═══════════════════════════════════════════════════════════════

/**
 * Offline state fallback
 */
@Composable
fun OfflineFallback(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📡", fontSize = 48.sp)

            Spacer(Modifier.height(12.dp))

            Text(
                "You're offline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Check your internet connection and try again",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// FEATURE UNAVAILABLE FALLBACK
// ═══════════════════════════════════════════════════════════════

/**
 * Feature not available fallback (e.g., permission denied, device not supported)
 */
@Composable
fun FeatureUnavailableFallback(
    featureName: String,
    reason: String = "This feature isn't available on your device",
    emoji: String = "🚫",
    alternativeAction: String? = null,
    onAlternative: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 40.sp)

            Spacer(Modifier.height(12.dp))

            Text(
                "$featureName Unavailable",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                reason,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (alternativeAction != null && onAlternative != null) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onAlternative) {
                    Text(alternativeAction)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════

/** Log tag for error boundary */
const val ERROR_BOUNDARY_TAG = "ErrorBoundary"

/**
 * Run a block safely, returning a fallback value on error
 */
inline fun <T> runSafely(
    fallback: T,
    logTag: String = ERROR_BOUNDARY_TAG,
    block: () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        Log.e(logTag, "Error in runSafely", e)
        fallback
    }
}

/**
 * Run a suspending block safely, returning a fallback value on error
 */
suspend inline fun <T> runSafelySuspend(
    fallback: T,
    logTag: String = ERROR_BOUNDARY_TAG,
    crossinline block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        Log.e(logTag, "Error in runSafelySuspend", e)
        fallback
    }
}

/**
 * Extension to run a composable calculation safely with fallback
 */
@Composable
fun <T> rememberSafe(
    fallback: T,
    calculation: () -> T
): T {
    return remember {
        try {
            calculation()
        } catch (e: Exception) {
            Log.e(ERROR_BOUNDARY_TAG, "Error in rememberSafe", e)
            fallback
        }
    }
}


package com.kyilmaz.neurocomet.utils

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Screen Timeout Manager for NeuroComet
 *
 * Allows the app to override system screen timeout settings to prevent
 * the screen from dimming or locking during certain activities.
 *
 * Neurodivergent-Friendly Rationale:
 * - Users with ADHD may lose focus when screen dims mid-task
 * - Users reading long-form content shouldn't be interrupted
 * - Meditation/breathing exercises need uninterrupted display
 * - Video/audio content viewing shouldn't trigger screen sleep
 * - Reduces anxiety from unexpected screen changes
 *
 * Usage:
 * ```
 * // In a Composable
 * KeepScreenOn() // Keeps screen on while composable is active
 *
 * // Or with condition
 * KeepScreenOn(enabled = isVideoPlaying)
 *
 * // In Activity
 * ScreenTimeoutManager.keepScreenOn(activity)
 * ScreenTimeoutManager.allowScreenTimeout(activity)
 * ```
 */
object ScreenTimeoutManager {

    /**
     * Keep the screen on indefinitely until explicitly allowed to timeout.
     * Call [allowScreenTimeout] to restore normal behavior.
     *
     * @param activity The activity whose window should stay on
     */
    fun keepScreenOn(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Allow the screen to timeout normally based on system settings.
     *
     * @param activity The activity whose window can now timeout
     */
    fun allowScreenTimeout(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Check if the screen is currently being kept on.
     *
     * @param activity The activity to check
     * @return true if screen is being kept on
     */
    fun isScreenKeptOn(activity: Activity): Boolean {
        return activity.window.attributes.flags and
               WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0
    }

    /**
     * Toggle the keep screen on state.
     *
     * @param activity The activity to toggle
     * @return The new state (true = kept on, false = normal timeout)
     */
    fun toggle(activity: Activity): Boolean {
        return if (isScreenKeptOn(activity)) {
            allowScreenTimeout(activity)
            false
        } else {
            keepScreenOn(activity)
            true
        }
    }
}

/**
 * Composable effect that keeps the screen on while the composable is in the composition.
 * When the composable leaves the composition, screen timeout is restored to normal.
 *
 * @param enabled Whether to keep the screen on. Defaults to true.
 *                Set to false to conditionally disable this behavior.
 *
 * Example usage:
 * ```
 * @Composable
 * fun VideoPlayerScreen() {
 *     KeepScreenOn() // Screen stays on while this screen is visible
 *
 *     // Your video player UI
 * }
 *
 * @Composable
 * fun MeditationScreen(isPlaying: Boolean) {
 *     KeepScreenOn(enabled = isPlaying) // Only keep on during meditation
 *
 *     // Your meditation UI
 * }
 * ```
 */
@Composable
fun KeepScreenOn(enabled: Boolean = true) {
    val context = LocalContext.current

    DisposableEffect(enabled) {
        val activity = context as? Activity

        if (enabled && activity != null) {
            ScreenTimeoutManager.keepScreenOn(activity)
        }

        onDispose {
            // Restore normal timeout when composable leaves composition
            activity?.let { ScreenTimeoutManager.allowScreenTimeout(it) }
        }
    }
}

/**
 * Composable that provides a toggle for screen timeout with state management.
 *
 * @param onStateChange Callback when the keep-on state changes
 * @return Current keep-on state
 */
@Composable
fun rememberScreenTimeoutState(
    initiallyKeptOn: Boolean = false,
    onStateChange: ((Boolean) -> Unit)? = null
): ScreenTimeoutState {
    val context = LocalContext.current
    val activity = context as? Activity

    var isKeptOn by remember { mutableStateOf(initiallyKeptOn) }

    DisposableEffect(isKeptOn) {
        if (activity != null) {
            if (isKeptOn) {
                ScreenTimeoutManager.keepScreenOn(activity)
            } else {
                ScreenTimeoutManager.allowScreenTimeout(activity)
            }
        }

        onDispose {
            activity?.let { ScreenTimeoutManager.allowScreenTimeout(it) }
        }
    }

    return remember(isKeptOn) {
        ScreenTimeoutState(
            isKeptOn = isKeptOn,
            toggle = {
                isKeptOn = !isKeptOn
                onStateChange?.invoke(isKeptOn)
            },
            setKeptOn = { value ->
                isKeptOn = value
                onStateChange?.invoke(value)
            }
        )
    }
}

/**
 * State holder for screen timeout management.
 */
data class ScreenTimeoutState(
    val isKeptOn: Boolean,
    val toggle: () -> Unit,
    val setKeptOn: (Boolean) -> Unit
)


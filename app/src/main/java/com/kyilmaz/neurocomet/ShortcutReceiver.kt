package com.kyilmaz.neurocomet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Broadcast receiver for shortcut creation callbacks.
 * This receiver is called when a shortcut is successfully pinned to the home screen.
 * Supports Pixel Launcher and other launchers that use the standard ShortcutManager API.
 *
 * Uses LauncherDetector for robust compatibility across all launchers.
 */
class ShortcutReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ShortcutReceiver"
        const val ACTION_SHORTCUT_ADDED = "com.kyilmaz.neurocomet.SHORTCUT_ADDED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Shortcut callback received: ${intent.action}")

        // Log launcher diagnostics for debugging
        try {
            val launcherInfo = LauncherDetector.detectLauncher(context)
            Log.d(TAG, "Current launcher: ${launcherInfo.launcherType.displayName} (${launcherInfo.packageName})")
            Log.d(TAG, "Launcher supports pinned shortcuts: ${launcherInfo.supportsPinnedShortcuts}")
            Log.d(TAG, "Launcher requires confirmation: ${launcherInfo.requiresConfirmation}")
        } catch (e: Exception) {
            Log.w(TAG, "Could not detect launcher info", e)
        }

        when (intent.action) {
            ACTION_SHORTCUT_ADDED -> {
                val iconStyle = intent.getStringExtra("icon_style") ?: "DEFAULT"
                Log.d(TAG, "Shortcut added successfully for style: $iconStyle")

                // Get launcher-specific message
                val message = try {
                    val launcherInfo = LauncherDetector.detectLauncher(context)
                    "NeuroComet shortcut added! ${launcherInfo.getShortcutTip()}"
                } catch (e: Exception) {
                    "NeuroComet shortcut added to home screen!"
                }

                // Show confirmation toast
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            // Handle other potential shortcut actions
            "com.android.launcher.action.INSTALL_SHORTCUT" -> {
                Log.d(TAG, "Legacy shortcut install broadcast received")
                handleLegacyShortcut(context, intent)
            }

            else -> {
                Log.d(TAG, "Unknown action received: ${intent.action}")
            }
        }
    }

    /**
     * Handle legacy shortcut install broadcasts.
     * Some older launchers still use this method.
     */
    private fun handleLegacyShortcut(context: Context, intent: Intent) {
        try {
            val shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            Log.d(TAG, "Legacy shortcut name: $shortcutName")

            // Log which launcher received the legacy broadcast
            val launcherInfo = LauncherDetector.detectLauncher(context)
            Log.d(TAG, "Legacy broadcast handled by: ${launcherInfo.launcherType.displayName}")

            if (launcherInfo.supportsLegacyBroadcast) {
                Log.d(TAG, "Launcher supports legacy broadcast - shortcut should be added")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling legacy shortcut", e)
        }
    }
}


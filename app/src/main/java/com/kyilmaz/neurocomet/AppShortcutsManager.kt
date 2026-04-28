package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.pm.ShortcutManagerCompat

/**
 * Central registry for the app's launcher shortcut intents.
 *
 * These actions are fired from two places:
 *   1. The static shortcuts declared in `res/xml/shortcuts.xml`
 *   2. Dynamic shortcuts pushed via [ShortcutManagerCompat.pushDynamicShortcut]
 *      (currently used by [IconCustomization] for pinned icon variants).
 *
 * [MainActivity.handleShortcutIntent] inspects the action and routes
 * to the appropriate in-app destination. Keeping the constants in one
 * place prevents typos drifting between XML and Kotlin.
 */
object AppShortcutsManager {

    /** Opens a composer for a new post. */
    const val ACTION_NEW_POST = "com.kyilmaz.neurocomet.action.NEW_POST"

    /** Opens the DM / messages list. */
    const val ACTION_OPEN_DMS = "com.kyilmaz.neurocomet.action.OPEN_DMS"

    /** Starts a regulation session. */
    const val ACTION_START_REGULATION = "com.kyilmaz.neurocomet.action.START_REGULATION"

    /** Set of all known shortcut actions — used by tests/dev tools. */
    val ALL_ACTIONS: Set<String> = setOf(
        ACTION_NEW_POST,
        ACTION_OPEN_DMS,
        ACTION_START_REGULATION,
    )

    /**
     * Decide which initial route [MainActivity] should navigate to based on an
     * inbound intent. Returns `null` when the intent isn't a shortcut we own,
     * leaving the default start destination (Feed / Auth) untouched.
     */
    fun routeForIntent(intent: Intent?): ShortcutDestination? {
        val action = intent?.action ?: return null
        return when (action) {
            ACTION_NEW_POST -> ShortcutDestination.NewPost
            ACTION_OPEN_DMS -> ShortcutDestination.Messages
            ACTION_START_REGULATION -> ShortcutDestination.Regulation
            else -> null
        }
    }

    /**
     * Reports shortcut usage back to the system so launchers can rank them
     * (e.g. Google Assistant's suggestions + the Pixel launcher's
     * recently-used shortcuts row). Safe to call from any thread.
     */
    fun reportShortcutUsed(context: Context, destination: ShortcutDestination) {
        val id = destination.shortcutId ?: return
        runCatching {
            ShortcutManagerCompat.reportShortcutUsed(context, id)
        }.onFailure {
            Log.w("AppShortcuts", "reportShortcutUsed failed", it)
        }
    }
}

/** Semantic destination the launcher wants us to open. */
enum class ShortcutDestination(val shortcutId: String?) {
    NewPost("new_post"),
    Messages("open_dms"),
    Regulation("regulation_session"),
}


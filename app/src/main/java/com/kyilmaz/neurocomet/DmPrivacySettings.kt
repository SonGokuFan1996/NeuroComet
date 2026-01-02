package com.kyilmaz.neurocomet

import android.content.Context

/**
 * In-memory DM privacy toggles (block/mute) for the demo.
 * This avoids bringing in storage dependencies while keeping FeedViewModel compiling.
 */
object DmPrivacySettings {
    private val blocked = mutableSetOf<String>()
    private val muted = mutableSetOf<String>()

    fun isBlocked(context: Context, userId: String): Boolean = blocked.contains(userId)
    fun isMuted(context: Context, userId: String): Boolean = muted.contains(userId)

    fun block(context: Context, userId: String) {
        blocked.add(userId)
    }

    fun unblock(context: Context, userId: String) {
        blocked.remove(userId)
    }

    fun mute(context: Context, userId: String) {
        muted.add(userId)
    }

    fun unmute(context: Context, userId: String) {
        muted.remove(userId)
    }
}


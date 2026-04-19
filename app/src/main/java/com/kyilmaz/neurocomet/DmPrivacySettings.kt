package com.kyilmaz.neurocomet

import android.content.Context

/**
 * In-memory DM privacy toggles (block/mute) for the demo.
 * This is the shared singleton source of truth — MessagesViewModel
 * reads from and writes to it so all components stay in sync.
 */
object DmPrivacySettings {
    private val blocked = mutableSetOf<String>()
    private val muted = mutableSetOf<String>()

    // --- Context-accepting API (kept for backward compat) ---

    fun isBlocked(context: Context, userId: String): Boolean = blocked.contains(userId)
    fun isMuted(context: Context, userId: String): Boolean = muted.contains(userId)

    fun block(context: Context, userId: String) { blocked.add(userId) }
    fun unblock(context: Context, userId: String) { blocked.remove(userId) }
    fun mute(context: Context, userId: String) { muted.add(userId) }
    fun unmute(context: Context, userId: String) { muted.remove(userId) }

    // --- Context-free API (for ViewModel sync) ---

    fun isBlockedNoContext(userId: String): Boolean = blocked.contains(userId)
    fun isMutedNoContext(userId: String): Boolean = muted.contains(userId)

    fun blockNoContext(userId: String) { blocked.add(userId) }
    fun unblockNoContext(userId: String) { blocked.remove(userId) }
    fun muteNoContext(userId: String) { muted.add(userId) }
    fun unmuteNoContext(userId: String) { muted.remove(userId) }

    /** Snapshot of all currently blocked user IDs. */
    fun getBlockedIds(): Set<String> = blocked.toSet()

    /** Snapshot of all currently muted user IDs. */
    fun getMutedIds(): Set<String> = muted.toSet()
}


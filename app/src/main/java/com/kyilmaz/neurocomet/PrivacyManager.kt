package com.kyilmaz.neurocomet

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Privacy Manager - Handles all privacy-related functionality across the app.
 * This includes account privacy, blocking, muting, and activity status.
 */
@Suppress("unused") // Many functions are designed for future use
object PrivacyManager {
    private const val PREFS_NAME = "NeuroComet_privacy"

    // Keys
    private const val KEY_BLOCKED_USERS = "blocked_users"
    private const val KEY_MUTED_USERS = "muted_users"
    private const val KEY_MUTED_WORDS = "muted_words"
    private const val KEY_HIDDEN_POSTS = "hidden_posts"
    private const val KEY_RESTRICTED_USERS = "restricted_users"

    private val _blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    private val _mutedUsers = MutableStateFlow<Set<String>>(emptySet())
    val mutedUsers: StateFlow<Set<String>> = _mutedUsers.asStateFlow()

    private val _mutedWords = MutableStateFlow<Set<String>>(emptySet())
    val mutedWords: StateFlow<Set<String>> = _mutedWords.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _blockedUsers.value = prefs.getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
        _mutedUsers.value = prefs.getStringSet(KEY_MUTED_USERS, emptySet()) ?: emptySet()
        _mutedWords.value = prefs.getStringSet(KEY_MUTED_WORDS, emptySet()) ?: emptySet()
    }

    // === Blocking ===

    fun blockUser(context: Context, userId: String) {
        val updated = _blockedUsers.value + userId
        _blockedUsers.value = updated
        saveBlockedUsers(context, updated)
    }

    fun unblockUser(context: Context, userId: String) {
        val updated = _blockedUsers.value - userId
        _blockedUsers.value = updated
        saveBlockedUsers(context, updated)
    }

    fun isUserBlocked(userId: String): Boolean = _blockedUsers.value.contains(userId)

    private fun saveBlockedUsers(context: Context, users: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_BLOCKED_USERS, users)
        }
    }

    fun getBlockedUsers(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_BLOCKED_USERS, emptySet()) ?: emptySet()
    }

    // === Muting Users ===

    fun muteUser(context: Context, userId: String) {
        val updated = _mutedUsers.value + userId
        _mutedUsers.value = updated
        saveMutedUsers(context, updated)
    }

    fun unmuteUser(context: Context, userId: String) {
        val updated = _mutedUsers.value - userId
        _mutedUsers.value = updated
        saveMutedUsers(context, updated)
    }

    fun isUserMuted(userId: String): Boolean = _mutedUsers.value.contains(userId)

    private fun saveMutedUsers(context: Context, users: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_MUTED_USERS, users)
        }
    }

    fun getMutedUsers(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_MUTED_USERS, emptySet()) ?: emptySet()
    }

    // === Muting Words ===

    fun addMutedWord(context: Context, word: String) {
        val normalizedWord = word.trim().lowercase()
        if (normalizedWord.isNotEmpty()) {
            val updated = _mutedWords.value + normalizedWord
            _mutedWords.value = updated
            saveMutedWords(context, updated)
        }
    }

    fun removeMutedWord(context: Context, word: String) {
        val normalizedWord = word.trim().lowercase()
        val updated = _mutedWords.value - normalizedWord
        _mutedWords.value = updated
        saveMutedWords(context, updated)
    }

    fun containsMutedWord(text: String): Boolean {
        val lowerText = text.lowercase()
        return _mutedWords.value.any { lowerText.contains(it) }
    }

    private fun saveMutedWords(context: Context, words: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_MUTED_WORDS, words)
        }
    }

    fun getMutedWords(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_MUTED_WORDS, emptySet()) ?: emptySet()
    }

    // === Hidden Posts ===

    fun hidePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hidden = prefs.getStringSet(KEY_HIDDEN_POSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        hidden.add(postId)
        prefs.edit { putStringSet(KEY_HIDDEN_POSTS, hidden) }
    }

    fun unhidePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hidden = prefs.getStringSet(KEY_HIDDEN_POSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        hidden.remove(postId)
        prefs.edit { putStringSet(KEY_HIDDEN_POSTS, hidden) }
    }

    fun isPostHidden(context: Context, postId: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIDDEN_POSTS, emptySet())?.contains(postId) ?: false
    }

    fun getHiddenPosts(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIDDEN_POSTS, emptySet()) ?: emptySet()
    }

    // === Restricted Users (can see posts but can't interact) ===

    fun restrictUser(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val restricted = prefs.getStringSet(KEY_RESTRICTED_USERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        restricted.add(userId)
        prefs.edit { putStringSet(KEY_RESTRICTED_USERS, restricted) }
    }

    fun unrestrictUser(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val restricted = prefs.getStringSet(KEY_RESTRICTED_USERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        restricted.remove(userId)
        prefs.edit { putStringSet(KEY_RESTRICTED_USERS, restricted) }
    }

    fun isUserRestricted(context: Context, userId: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_RESTRICTED_USERS, emptySet())?.contains(userId) ?: false
    }

    // === Content Filtering ===

    /**
     * Filters a list of posts based on privacy settings.
     * Removes posts from blocked users, muted users (optional), and hidden posts.
     */
    fun filterPosts(
        context: Context,
        posts: List<Post>,
        privacySettings: PrivacySettings,
        includeMuted: Boolean = false
    ): List<Post> {
        val blockedUsers = getBlockedUsers(context)
        val mutedUsers = if (!includeMuted) getMutedUsers(context) else emptySet()
        val hiddenPosts = getHiddenPosts(context)
        val mutedWords = getMutedWords(context)

        return posts.filter { post ->
            val authorId = post.userId ?: ""

            // Filter blocked users
            if (blockedUsers.contains(authorId)) return@filter false

            // Filter muted users (if not showing muted content)
            if (mutedUsers.contains(authorId)) return@filter false

            // Filter hidden posts
            if (hiddenPosts.contains(post.id?.toString() ?: "")) return@filter false

            // Filter muted words
            if (mutedWords.isNotEmpty()) {
                val content = post.content.lowercase()
                if (mutedWords.any { content.contains(it) }) return@filter false
            }

            true
        }
    }

    /**
     * Filters DM conversations based on privacy settings.
     */
    fun filterConversations(
        context: Context,
        conversations: List<Conversation>,
        privacySettings: PrivacySettings
    ): List<Conversation> {
        val blockedUsers = getBlockedUsers(context)

        // Filter based on DM permission - check all participants
        return conversations.filter { conv ->
            // Check if any participant (other than current user) is blocked
            conv.participants.none { participantId -> blockedUsers.contains(participantId) }
        }
    }

    /**
     * Check if a user can send DMs based on privacy settings.
     */
    fun canSendDM(
        context: Context,
        senderId: String,
        currentUserId: String,
        privacySettings: PrivacySettings,
        isFollower: Boolean,
        isFollowing: Boolean
    ): Boolean {
        // Check if blocked
        if (isUserBlocked(senderId)) return false

        // Check DM permission
        return when (privacySettings.allowDMsFrom) {
            DMPermission.EVERYONE -> true
            DMPermission.FOLLOWERS -> isFollower
            DMPermission.FOLLOWING -> isFollowing
            DMPermission.MUTUALS -> isFollower && isFollowing
            DMPermission.NOBODY -> false
        }
    }

    /**
     * Check if a user can tag the current user based on privacy settings.
     */
    fun canTag(
        senderId: String,
        privacySettings: PrivacySettings,
        isFollower: Boolean
    ): Boolean {
        if (isUserBlocked(senderId)) return false

        return when (privacySettings.allowTagging) {
            TagPermission.EVERYONE -> true
            TagPermission.FOLLOWERS -> isFollower
            TagPermission.NOBODY -> false
        }
    }

    // === Reset All Privacy Data ===

    fun resetAll(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
        _blockedUsers.value = emptySet()
        _mutedUsers.value = emptySet()
        _mutedWords.value = emptySet()
    }
}

/**
 * Content Manager - Handles content preferences and filtering.
 */
@Suppress("unused") // Functions designed for future use
object ContentManager {
    private const val PREFS_NAME = "NeuroComet_content"

    private const val KEY_LIKED_POSTS = "liked_posts"
    private const val KEY_SAVED_POSTS = "saved_posts"
    private const val KEY_WATCH_HISTORY = "watch_history"
    private const val KEY_SEARCH_HISTORY = "search_history"

    // === Saved/Bookmarked Posts ===

    fun savePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SAVED_POSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.add(postId)
        prefs.edit { putStringSet(KEY_SAVED_POSTS, saved) }
    }

    fun unsavePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SAVED_POSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.remove(postId)
        prefs.edit { putStringSet(KEY_SAVED_POSTS, saved) }
    }

    fun isPostSaved(context: Context, postId: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_SAVED_POSTS, emptySet())?.contains(postId) ?: false
    }

    fun getSavedPosts(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_SAVED_POSTS, emptySet()) ?: emptySet()
    }

    // === Search History ===

    fun addSearchQuery(context: Context, query: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = prefs.getString(KEY_SEARCH_HISTORY, "")?.split("|")?.toMutableList() ?: mutableListOf()

        // Remove duplicates and add to front
        history.remove(query)
        history.add(0, query)

        // Keep only last 20 searches
        val trimmed = history.take(20)

        prefs.edit { putString(KEY_SEARCH_HISTORY, trimmed.joinToString("|")) }
    }

    fun getSearchHistory(context: Context): List<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SEARCH_HISTORY, "")?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
    }

    fun clearSearchHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_SEARCH_HISTORY)
        }
    }

    // === Video Autoplay ===

    fun shouldAutoplayVideo(context: Context, contentPrefs: ContentPreferences): Boolean {
        return when (contentPrefs.autoplayVideos) {
            AutoplayOption.ALWAYS -> true
            AutoplayOption.WIFI_ONLY -> isOnWifi(context)
            AutoplayOption.NEVER -> false
        }
    }

    private fun isOnWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
    }

    // === Data Saver ===

    fun getImageQuality(contentPrefs: ContentPreferences): ImageQuality {
        return if (contentPrefs.dataSaverMode) ImageQuality.LOW else ImageQuality.HIGH
    }

    // === Clear All Content Data ===

    fun clearAllData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
    }
}

enum class ImageQuality {
    LOW, MEDIUM, HIGH
}

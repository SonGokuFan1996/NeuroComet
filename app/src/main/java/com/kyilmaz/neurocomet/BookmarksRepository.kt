package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/**
 * Repository for managing post bookmarks with Supabase persistence.
 *
 * DB table: `bookmarks`
 *   id UUID PK, user_id UUID, post_id BIGINT, created_at TIMESTAMPTZ
 *   UNIQUE(user_id, post_id)
 */
object BookmarksRepository {

    private const val TAG = "BookmarksRepository"
    private const val TABLE = "bookmarks"

    /**
     * Check if a post is bookmarked by the current user.
     */
    suspend fun isBookmarked(userId: String, postId: Long): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "id",
                filters = "user_id=eq.$userId&post_id=eq.$postId"
            )
            rows.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "isBookmarked check failed", e)
            false
        }
    }

    /**
     * Add a bookmark. Returns true on success.
     */
    suspend fun addBookmark(userId: String, postId: Long): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            val client = AppSupabaseClient.client ?: return@withContext false
            client.safeInsert(TABLE, buildJsonObject {
                put("user_id", userId)
                put("post_id", postId)
            })
            Log.d(TAG, "✅ Bookmarked post #$postId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "addBookmark failed", e)
            false
        }
    }

    /**
     * Remove a bookmark. Returns true on success.
     */
    suspend fun removeBookmark(userId: String, postId: Long): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            safeDelete(
                table = TABLE,
                filters = "user_id=eq.$userId&post_id=eq.$postId"
            )
            Log.d(TAG, "✅ Unbookmarked post #$postId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "removeBookmark failed", e)
            false
        }
    }

    /**
     * Toggle bookmark state. Returns new state (true = now bookmarked).
     */
    suspend fun toggleBookmark(userId: String, postId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentlyBookmarked = isBookmarked(userId, postId)
            if (currentlyBookmarked) {
                removeBookmark(userId, postId)
                Result.success(false)
            } else {
                addBookmark(userId, postId)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all bookmarked post IDs for a user.
     */
    suspend fun getBookmarkedPostIds(userId: String): Set<Long> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptySet()
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "post_id",
                filters = "user_id=eq.$userId&order=created_at.desc"
            )
            rows.mapNotNull { it.jsonObject["post_id"]?.jsonPrimitive?.long }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "getBookmarkedPostIds failed", e)
            emptySet()
        }
    }
}


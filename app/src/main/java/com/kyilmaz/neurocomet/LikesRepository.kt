package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/**
 * Repository for managing post likes with Supabase persistence.
 * Falls back to local-only mode if Supabase is not configured.
 *
 * All Supabase calls use the safe REST helpers from SupabaseInsertHelper.kt
 * to avoid the kotlin-reflect typeOf() crash on Android.
 */
object LikesRepository {

    private const val TAG = "LikesRepository"
    private const val TABLE_POST_LIKES = "post_likes"
    private const val TABLE_POSTS = "posts"

    /**
     * Get current timestamp in ISO 8601 format for Supabase
     */
    private fun nowTimestamp(): String {
        return java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    /**
     * Toggle like on a post - adds if not liked, removes if already liked.
     * Returns the new like state (true = liked, false = unliked)
     */
    suspend fun toggleLike(postId: Long, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            Log.d(TAG, "Supabase not available - like will only be stored locally")
            return@withContext Result.failure(Exception("Supabase not configured"))
        }

        try {
            // Check if already liked via safe REST call
            val existing = try {
                safeSelect(
                    table = TABLE_POST_LIKES,
                    columns = "post_id,user_id",
                    filters = "post_id=eq.$postId&user_id=eq.$userId"
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not check existing like status, assuming not liked", e)
                emptyList()
            }

            val isCurrentlyLiked = existing.isNotEmpty()

            if (isCurrentlyLiked) {
                // Remove like
                try {
                    safeDelete(
                        table = TABLE_POST_LIKES,
                        filters = "post_id=eq.$postId&user_id=eq.$userId"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete like record", e)
                    return@withContext Result.failure(e)
                }

                // Decrement like count on post (without is_liked_by_me — it's per-user)
                updatePostLikeCount(postId, increment = false)

                Log.d(TAG, "\uD83D\uDC4E Unliked post #$postId")
                Result.success(false)
            } else {
                // Add like
                try {
                    val client = AppSupabaseClient.client!!
                    client.safeInsert(TABLE_POST_LIKES, buildJsonObject {
                        put("post_id", postId)
                        put("user_id", userId)
                        put("created_at", nowTimestamp())
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to insert like record", e)
                    return@withContext Result.failure(e)
                }

                // Increment like count on post (without is_liked_by_me — it's per-user)
                updatePostLikeCount(postId, increment = true)

                Log.d(TAG, "\uD83D\uDC4D Liked post #$postId")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle like for post #$postId", e)
            Result.failure(e)
        }
    }

    /**
     * Update only the like count on a post.
     * NOTE: is_liked_by_me is a per-user concept and should NOT be stored
     * as a column on the global posts table. We only update the count.
     */
    private suspend fun updatePostLikeCount(postId: Long, increment: Boolean) {
        try {
            // Get current like count via safe REST call
            val rows = safeSelect(
                table = TABLE_POSTS,
                columns = "id,likes",
                filters = "id=eq.$postId"
            )

            // Safety: if the post doesn't exist in the database, bail out.
            // This prevents phantom updates when mock/local-only post IDs
            // coincidentally match the filter.
            if (rows.isEmpty()) {
                Log.d(TAG, "Post #$postId not found in DB — skipping like count update")
                return
            }

            val currentLikes = rows.firstOrNull()
                ?.jsonObject?.get("likes")?.jsonPrimitive?.int ?: 0
            val delta = if (increment) 1 else -1
            val newLikes = (currentLikes + delta).coerceAtLeast(0)

            // Update only the likes count — do NOT touch is_liked_by_me
            // (it may not even exist as a column, or may have NOT NULL without default)
            safeUpdate(
                table = TABLE_POSTS,
                body = buildJsonObject {
                    put("likes", newLikes)
                },
                filters = "id=eq.$postId"
            )

            Log.d(TAG, "Updated post #$postId: likes $currentLikes -> $newLikes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update like count for post #$postId (non-fatal)", e)
            // Non-fatal: the like was toggled successfully, just the count sync failed
        }
    }

    /**
     * Check if a user has liked a specific post
     */
    suspend fun isLiked(postId: Long, userId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false

        try {
            val rows = safeSelect(
                table = TABLE_POST_LIKES,
                columns = "post_id",
                filters = "post_id=eq.$postId&user_id=eq.$userId"
            )
            rows.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check like status for post #$postId", e)
            false
        }
    }

    /**
     * Get all liked post IDs for a user
     */
    suspend fun getUserLikedPosts(userId: String): List<Long> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()

        try {
            val rows = safeSelect(
                table = TABLE_POST_LIKES,
                columns = "post_id",
                filters = "user_id=eq.$userId"
            )
            rows.mapNotNull { it.jsonObject["post_id"]?.jsonPrimitive?.long }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get liked posts for user $userId", e)
            emptyList()
        }
    }
}

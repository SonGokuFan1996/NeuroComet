package com.kyilmaz.neurocomet

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Repository for managing post likes with Supabase persistence.
 * Falls back to local-only mode if Supabase is not configured.
 */
object LikesRepository {

    private const val TAG = "LikesRepository"
    private const val TABLE_POST_LIKES = "post_likes"
    private const val TABLE_POSTS = "posts"

    /**
     * Data class for the post_likes table
     */
    @Serializable
    data class PostLike(
        val post_id: Long,
        val user_id: String
    )

    /**
     * Toggle like on a post - adds if not liked, removes if already liked.
     * Returns the new like state (true = liked, false = unliked)
     */
    suspend fun toggleLike(postId: Long, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client

        if (client == null) {
            Log.d(TAG, "Supabase not available - like will only be stored locally")
            return@withContext Result.failure(Exception("Supabase not configured"))
        }

        try {
            // Check if already liked
            val existingLikes = client.postgrest[TABLE_POST_LIKES]
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<PostLike>()

            val isCurrentlyLiked = existingLikes.isNotEmpty()

            if (isCurrentlyLiked) {
                // Remove like
                client.postgrest[TABLE_POST_LIKES]
                    .delete {
                        filter {
                            eq("post_id", postId)
                            eq("user_id", userId)
                        }
                    }

                // Decrement like count on post
                updatePostLikeCount(postId, -1)

                Log.d(TAG, "👎 Unliked post #$postId")
                Result.success(false)
            } else {
                // Add like
                client.postgrest[TABLE_POST_LIKES]
                    .insert(PostLike(post_id = postId, user_id = userId))

                // Increment like count on post
                updatePostLikeCount(postId, 1)

                Log.d(TAG, "👍 Liked post #$postId")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle like for post #$postId", e)
            Result.failure(e)
        }
    }

    /**
     * Update the like count on a post
     */
    private suspend fun updatePostLikeCount(postId: Long, delta: Int) {
        val client = AppSupabaseClient.client ?: return

        try {
            // Get current like count
            val posts = client.postgrest[TABLE_POSTS]
                .select(columns = Columns.list("id", "likes")) {
                    filter {
                        eq("id", postId)
                    }
                }
                .decodeList<PostLikeCount>()

            val currentLikes = posts.firstOrNull()?.likes ?: 0
            val newLikes = (currentLikes + delta).coerceAtLeast(0)

            // Update the count
            client.postgrest[TABLE_POSTS]
                .update({ set("likes", newLikes) }) {
                    filter {
                        eq("id", postId)
                    }
                }

            Log.d(TAG, "Updated post #$postId like count: $currentLikes -> $newLikes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update like count for post #$postId", e)
        }
    }

    @Serializable
    private data class PostLikeCount(
        val id: Long,
        val likes: Int
    )

    /**
     * Check if a user has liked a specific post
     */
    suspend fun isLiked(postId: Long, userId: String): Boolean = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client ?: return@withContext false

        try {
            val likes = client.postgrest[TABLE_POST_LIKES]
                .select {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<PostLike>()

            likes.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check like status for post #$postId", e)
            false
        }
    }

    /**
     * Get all liked post IDs for a user
     */
    suspend fun getUserLikedPosts(userId: String): List<Long> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client ?: return@withContext emptyList()

        try {
            val likes = client.postgrest[TABLE_POST_LIKES]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<PostLike>()

            likes.map { it.post_id }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get liked posts for user $userId", e)
            emptyList()
        }
    }
}


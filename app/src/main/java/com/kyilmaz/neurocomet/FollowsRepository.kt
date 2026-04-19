package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Repository for managing follows with Supabase persistence.
 *
 * DB table: `follows`
 *   id UUID PK, follower_id UUID, following_id UUID, created_at TIMESTAMPTZ
 *   UNIQUE(follower_id, following_id)
 */
object FollowsRepository {

    private const val TAG = "FollowsRepository"
    private const val TABLE = "follows"

    /**
     * Check if [followerId] is following [followingId].
     */
    suspend fun isFollowing(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "id",
                filters = "follower_id=eq.$followerId&following_id=eq.$followingId"
            )
            rows.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "isFollowing check failed", e)
            false
        }
    }

    /**
     * Follow a user. Returns true on success.
     */
    suspend fun follow(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            val client = AppSupabaseClient.client ?: return@withContext false
            client.safeInsert(TABLE, buildJsonObject {
                put("follower_id", followerId)
                put("following_id", followingId)
            })
            Log.d(TAG, "✅ $followerId now follows $followingId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "follow failed", e)
            false
        }
    }

    /**
     * Unfollow a user. Returns true on success.
     */
    suspend fun unfollow(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            safeDelete(
                table = TABLE,
                filters = "follower_id=eq.$followerId&following_id=eq.$followingId"
            )
            Log.d(TAG, "✅ $followerId unfollowed $followingId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "unfollow failed", e)
            false
        }
    }

    /**
     * Toggle follow state. Returns the new state (true = now following).
     */
    suspend fun toggleFollow(followerId: String, followingId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentlyFollowing = isFollowing(followerId, followingId)
            if (currentlyFollowing) {
                unfollow(followerId, followingId)
                Result.success(false)
            } else {
                follow(followerId, followingId)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the follower count for a user.
     */
    suspend fun getFollowerCount(userId: String): Int = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext 0
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "id",
                filters = "following_id=eq.$userId"
            )
            rows.size
        } catch (e: Exception) {
            Log.e(TAG, "getFollowerCount failed", e)
            0
        }
    }

    /**
     * Get the following count for a user.
     */
    suspend fun getFollowingCount(userId: String): Int = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext 0
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "id",
                filters = "follower_id=eq.$userId"
            )
            rows.size
        } catch (e: Exception) {
            Log.e(TAG, "getFollowingCount failed", e)
            0
        }
    }

    /**
     * Get the list of user IDs that [userId] is following.
     */
    suspend fun getFollowingIds(userId: String): List<String> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()
        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "following_id",
                filters = "follower_id=eq.$userId"
            )
            rows.mapNotNull { it.jsonObject["following_id"]?.jsonPrimitive?.content }
        } catch (e: Exception) {
            Log.e(TAG, "getFollowingIds failed", e)
            emptyList()
        }
    }
}



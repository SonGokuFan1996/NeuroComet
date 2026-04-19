package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Repository for fetching user profiles from Supabase.
 *
 * Uses the `users` table (primary) + `profiles` table (display data).
 * Falls back to mock data when Supabase is not configured.
 *
 * DB tables:
 *   `users`    — id, email, username, display_name, avatar_url, banner_url, bio, created_at
 *   `profiles` — id, display_name, username, avatar_url, bio, is_verified, created_at
 *   `follows`  — for counts
 *   `posts`    — for post count
 */
object ProfileRepository {

    private const val TAG = "ProfileRepository"

    /**
     * Fetch a full user profile from Supabase.
     * Returns null if Supabase is unavailable or the user isn't found.
     */
    suspend fun fetchProfile(userId: String, currentUserId: String? = null): UserProfile? = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext null

        try {
            // Fetch from users table first (has most fields)
            val userRows = safeSelect(
                table = "users",
                columns = "id,email,username,display_name,avatar_url,banner_url,bio,created_at",
                filters = "id=eq.$userId"
            )

            if (userRows.isEmpty()) {
                Log.d(TAG, "User $userId not found in users table")
                return@withContext null
            }

            val userObj = userRows.first().jsonObject
            val username = userObj["username"]?.jsonPrimitive?.content ?: ""
            val displayName = userObj["display_name"]?.jsonPrimitive?.content ?: username
            val avatarUrlVal = userObj["avatar_url"]?.jsonPrimitive?.content
            val bio = userObj["bio"]?.jsonPrimitive?.content ?: ""
            val createdAt = userObj["created_at"]?.jsonPrimitive?.content ?: ""

            // Check profiles table for verification status
            var isVerified = false
            try {
                val profileRows = safeSelect(
                    table = "profiles",
                    columns = "is_verified",
                    filters = "id=eq.$userId"
                )
                if (profileRows.isNotEmpty()) {
                    isVerified = profileRows.first().jsonObject["is_verified"]
                        ?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check verification status", e)
            }

            // Get follow counts
            val followerCount = FollowsRepository.getFollowerCount(userId)
            val followingCount = FollowsRepository.getFollowingCount(userId)

            // Get post count
            val postCount = try {
                val posts = safeSelect(
                    table = "posts",
                    columns = "id",
                    filters = "user_id=eq.$userId"
                )
                posts.size
            } catch (e: Exception) { 0 }

            // Check follow relationship with current user
            val isFollowing = if (currentUserId != null && currentUserId != userId) {
                FollowsRepository.isFollowing(currentUserId, userId)
            } else false

            val isFollowedByMe = isFollowing // same thing from current user's perspective

            // Format join date
            val joinedDate = try {
                val instant = java.time.Instant.parse(createdAt)
                val zdt = instant.atZone(java.time.ZoneId.systemDefault())
                "${zdt.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${zdt.year}"
            } catch (_: Exception) { "" }

            val user = User(
                id = userId,
                name = displayName.ifBlank { username },
                avatarUrl = avatarUrlVal ?: avatarUrl(username),
                isVerified = isVerified,
                personality = bio
            )

            UserProfile(
                user = user,
                bio = bio,
                joinedDate = joinedDate,
                followerCount = followerCount,
                followingCount = followingCount,
                postCount = postCount,
                isFollowing = isFollowing,
                isFollowedByMe = isFollowedByMe,
                isMutual = false, // Would need reverse check
                badges = emptyList(),
                posts = emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "fetchProfile failed for $userId", e)
            null
        }
    }

    /**
     * Update the current user's profile fields.
     */
    suspend fun updateProfile(
        userId: String,
        displayName: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            val body = buildJsonObject {
                displayName?.let { put("display_name", it) }
                bio?.let { put("bio", it) }
                avatarUrl?.let { put("avatar_url", it) }
            }
            safeUpdate(table = "users", body = body, filters = "id=eq.$userId")
            // Also update profiles table to keep in sync
            safeUpdate(table = "profiles", body = body, filters = "id=eq.$userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile failed", e)
            false
        }
    }
}


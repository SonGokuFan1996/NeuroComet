package com.kyilmaz.neurocomet

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * Developer utility for sending test data to Supabase tables.
 * Only available in debug builds for testing database connectivity.
 */
@OptIn(ExperimentalSerializationApi::class)
object SupabaseTestData {

    private const val TAG = "SupabaseTestData"

    // =========================================================================
    // Test Data Models (must match Supabase table schemas)
    // Note: We always provide created_at to avoid NOT NULL constraint issues
    // Note: @EncodeDefault ensures all fields are serialized even with defaults
    // =========================================================================

    @Serializable
    data class TestPost(
        val user_id: String,
        val content: String,
        val image_url: String? = null,
        val video_url: String? = null,
        @EncodeDefault val likes: Int = 0,
        @EncodeDefault val comments: Int = 0,  // Required - NOT NULL in database
        @EncodeDefault val shares: Int = 0,    // Required - NOT NULL in database
        @EncodeDefault val is_liked_by_me: Boolean = false, // Required - NOT NULL in database
        val created_at: String  // Required - always provide timestamp
    )

    @Serializable
    data class TestUser(
        val email: String,
        val username: String,
        val display_name: String,
        val avatar_url: String? = null,
        val bio: String? = null,
        val created_at: String, // Required - always provide timestamp
        val updated_at: String  // Required - always provide timestamp
    )

    @Serializable
    data class TestProfile(
        val id: String,
        val display_name: String,
        val username: String,
        val avatar_url: String? = null,
        val bio: String? = null,
        val is_verified: Boolean = false,
        val created_at: String,
        val updated_at: String
    )

    @Serializable
    data class TestPostLike(
        val post_id: Long,
        val user_id: String,
        val created_at: String // Required - always provide timestamp
    )

    @Serializable
    data class TestStory(
        val user_id: String,
        val image_url: String,
        val duration: Int = 5000
    )

    /**
     * Get current timestamp in ISO 8601 format for Supabase
     */
    private fun nowTimestamp(): String {
        return java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    // =========================================================================
    // Send Test Data Functions
    // =========================================================================

    /**
     * Check if Supabase is available
     */
    fun isSupabaseAvailable(): Boolean {
        return AppSupabaseClient.isAvailable()
    }

    /**
     * Send a test post to the posts table
     */
    suspend fun sendTestPost(): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            Log.d(TAG, "Preparing test post...")

            // Use buildJsonObject to avoid kotlin-reflect typeOf() failures on Android.
            // The Supabase SDK's insert() is reified and calls typeOf<T>() internally,
            // which crashes with "Unresolved class: java.lang.String" for Map/data classes.
            // JsonObject is already @Serializable so it bypasses reflection entirely.
            val postData = buildJsonObject {
                put("user_id", "test_user_${System.currentTimeMillis()}")
                put("content", "\uD83E\uDDEA Test post from NeuroComet DevOptions!\n\nThis is a test to verify Supabase connectivity. \uD83D\uDE80")
                put("likes", (0..100).random())
                put("comments", (0..50).random())
                put("shares", (0..20).random())
                put("is_liked_by_me", false)
                put("created_at", nowTimestamp())
            }

            Log.d(TAG, "Inserting test post into 'posts' table...")
            client.safeInsert("posts", postData)

            Log.d(TAG, "\u2705 Test post sent successfully")
            Result.success("Test post sent successfully!")
        } catch (e: Throwable) {
            Log.e(TAG, "\u274C Failed to send test post: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(Exception("Send failed: ${e.message}", e))
        }
    }

    /**
     * Send a test user to the users table
     */
    suspend fun sendTestUser(): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val timestamp = System.currentTimeMillis()
            val now = nowTimestamp()
            val username = "testuser_$timestamp"
            val email = "$username@NeuroComet.dev"

            val testUserData = buildJsonObject {
                put("email", email)
                put("username", username)
                put("display_name", "Test User $timestamp")
                put("avatar_url", avatarUrl(username))
                put("bio", "Android test user")
                put("created_at", now)
                put("updated_at", now)
            }

            client.safeInsert("users", testUserData)

            // Keep profiles in sync when that table exists.
            try {
                val userRow = client.from("users")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id")) {
                        filter { eq("email", email) }
                        limit(1)
                    }
                    .decodeList<Map<String, String>>()
                    .firstOrNull()

                val userId = userRow?.get("id")
                if (userId != null) {
                    val profileData = buildJsonObject {
                        put("id", userId)
                        put("display_name", "Test User $timestamp")
                        put("username", username)
                        put("avatar_url", avatarUrl(username))
                        put("bio", "Android test user")
                        put("is_verified", false)
                        put("created_at", now)
                        put("updated_at", now)
                    }
                    client.from("profiles").upsert(profileData)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Profiles table not available while creating test user", e)
            }

            Result.success("Test user sent successfully!")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ Failed to send test user: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(Exception("Send failed: ${e.message}", e))
        }
    }

    /**
     * Send a test like to the post_likes table
     */
    suspend fun sendTestLike(postId: Long = 1): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val likeData = buildJsonObject {
                put("post_id", postId)
                put("user_id", "test_user_${System.currentTimeMillis()}")
                put("created_at", nowTimestamp())
            }

            client.safeInsert("post_likes", likeData)

            Log.d(TAG, "âœ… Test like sent for post #$postId")
            Result.success("Like added to post #$postId!")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to send test like", e)
            Result.failure(e)
        }
    }

    /**
     * Send multiple test posts
     */
    suspend fun sendBulkTestPosts(count: Int = 5): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val testPosts = (1..count).map { i ->
                buildJsonObject {
                    put("user_id", "bulk_test_user_$i")
                    put("content", "\uD83E\uDDEA Bulk test post #$i\n\n${getRandomNeuroContent()}")
                    put("likes", (0..500).random())
                    put("comments", (0..100).random())
                    put("shares", (0..50).random())
                    put("is_liked_by_me", false)
                    put("created_at", nowTimestamp())
                }
            }

            client.safeInsertList("posts", testPosts)

            Log.d(TAG, "âœ… $count test posts sent successfully")
            Result.success("$count test posts created!")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to send bulk test posts", e)
            Result.failure(e)
        }
    }

    /**
     * Query and count rows in a table
     */
    suspend fun getTableRowCount(tableName: String): Result<Int> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not configured"))
        }

        try {
            val result = safeSelect(table = tableName, columns = "id")
            val count = result.size
            Log.d(TAG, "\uD83D\uDCCA Table '$tableName' has $count rows")
            Result.success(count)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to count rows in '$tableName': ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(Exception("Count failed: ${e.message}", e))
        }
    }

    /**
     * Clear all test data from a table (DANGEROUS - use with caution!)
     */
    suspend fun clearTestData(tableName: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            Log.d(TAG, "\uD83D\uDDD1\uFE0F Attempting to clear test data from '$tableName'...")

            // Only delete rows with test identifiers
            when (tableName) {
                "posts" -> {
                    // Delete posts where user_id starts with "test_" or "bulk_test_"
                    client.from("posts")
                        .delete {
                            filter {
                                or {
                                    like("user_id", "test_%")
                                    like("user_id", "bulk_test_%")
                                }
                            }
                        }
                    Log.d(TAG, "âœ… Deleted test posts")
                }
                "users" -> {
                    // Delete users with test email domain
                    client.from("users")
                        .delete {
                            filter {
                                like("email", "%@NeuroComet.dev")
                            }
                        }
                    Log.d(TAG, "✅ Deleted test users")
                }
                "profiles" -> {
                    client.from("profiles")
                        .delete {
                            filter {
                                or {
                                    like("username", "testuser_%")
                                    like("username", "bulk_test_%")
                                }
                            }
                        }
                    Log.d(TAG, "✅ Deleted test profiles")
                }
                "dm_messages" -> {
                    client.from("dm_messages")
                        .delete {
                            filter {
                                or {
                                    like("sender_id", "test_%")
                                    like("content", "%NeuroComet DevOptions%")
                                }
                            }
                        }
                    Log.d(TAG, "✅ Deleted test direct messages")
                }
                "post_likes" -> {
                    // Delete likes from test users
                    client.from("post_likes")
                        .delete {
                            filter {
                                like("user_id", "test_%")
                            }
                        }
                    Log.d(TAG, "âœ… Deleted test likes")
                }
                else -> {
                    return@withContext Result.failure(Exception("Unknown table: $tableName"))
                }
            }

            Log.d(TAG, "\uD83D\uDDD1\uFE0F Test data cleared from '$tableName'")
            Result.success("Test data cleared from '$tableName'!")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to clear test data from '$tableName'", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // Helper Functions
    // =========================================================================

    /**
     * Minimal model for decoding rows with only an 'id' column.
     * Used by [getTableRowCount] to avoid reflection crashes on Map<String, Any?>.
     */
    @Serializable
    data class IdOnly(
        val id: Long = 0
    )

    /**
     * Generate a Gravatar-style avatar URL for test users.
     */
    private fun avatarUrl(username: String): String {
        return "https://api.dicebear.com/7.x/identicon/png?seed=$username"
    }

    private fun getRandomNeuroContent(): String {
        val contents = listOf(
            "Today's hyperfocus session was incredibly productive! \uD83C\uDFAF",
            "Finally found a stim toy that works perfectly for meetings \uD83C\uDF00",
            "Reminder: Your brain isn't broken, it's just different \uD83D\uDC9C",
            "Does anyone else feel like they have 100 tabs open in their brain? \uD83E\uDDE0",
            "Found a great sensory-friendly restaurant! Dim lights, quiet music \u2728",
            "Executive dysfunction is hitting hard today. Sending solidarity \uD83D\uDCAA",
            "Just discovered body doubling and it's a game changer!",
            "My special interest just connected to my work project somehow \uD83C\uDF89",
            "Celebrating small wins today: I remembered to eat lunch! \uD83C\uDF7D\uFE0F",
            "Noise-canceling headphones are a neurodivergent essential \uD83C\uDFA7"
        )
        return contents.random()
    }

    // =========================================================================
    // Account Management Functions (GDPR Compliance)
    // =========================================================================

    @Serializable
    data class AccountDeletionRequest(
        val user_id: String,
        val deletion_scheduled_at: String?,
        val is_active: Boolean
    )

    @Serializable
    data class ReportSubmission(
        val reporter_id: String,
        val content_type: String,
        val content_id: String,
        val reason: String,
        val additional_info: String?,
        val status: String = "pending",
        val created_at: String
    )

    @Serializable
    data class BlockedUserRecord(
        val blocker_id: String,
        val blocked_id: String,
        val created_at: String
    )

    @Serializable
    data class BookmarkRecord(
        val user_id: String,
        val post_id: String,
        val created_at: String
    )

    /**
     * Schedule account for deletion (14-day grace period for GDPR)
     */
    suspend fun scheduleAccountDeletion(userId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val deletionDate = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
                .plusDays(14)
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            client.from("users").update({
                set("deletion_scheduled_at", deletionDate)
                set("is_active", false)
            }) {
                filter { eq("id", userId) }
            }

            Log.d(TAG, "âœ… Account deletion scheduled for $userId")
            Result.success("Account scheduled for deletion on $deletionDate. Log in within 14 days to cancel.")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to schedule account deletion", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel scheduled account deletion
     */
    suspend fun cancelAccountDeletion(userId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            client.from("users").update({
                set("deletion_scheduled_at", null as String?)
                set("is_active", true)
            }) {
                filter { eq("id", userId) }
            }

            Log.d(TAG, "âœ… Account deletion cancelled for $userId")
            Result.success("Account deletion cancelled!")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to cancel account deletion", e)
            Result.failure(e)
        }
    }

    /**
     * Submit a content report
     */
    suspend fun submitReport(
        reporterId: String,
        contentType: String,
        contentId: String,
        reason: String,
        additionalInfo: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val reportData = buildJsonObject {
                put("reporter_id", reporterId)
                put("content_type", contentType)
                put("content_id", contentId)
                put("reason", reason)
                put("additional_info", additionalInfo)
                put("status", "pending")
                put("created_at", nowTimestamp())
            }

            client.safeInsert("reports", reportData)

            Log.d(TAG, "âœ… Report submitted for $contentType:$contentId")
            Result.success("Report submitted. We'll review it shortly.")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to submit report", e)
            Result.failure(e)
        }
    }

    /**
     * Block a user (Supabase)
     */
    suspend fun blockUserInDatabase(blockerId: String, blockedId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val blockData = buildJsonObject {
                put("blocker_id", blockerId)
                put("blocked_id", blockedId)
                put("created_at", nowTimestamp())
            }

            client.safeInsert("blocked_users", blockData)

            Log.d(TAG, "âœ… User $blockedId blocked by $blockerId")
            Result.success("User blocked")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to block user", e)
            Result.failure(e)
        }
    }

    /**
     * Unblock a user (Supabase)
     */
    suspend fun unblockUserInDatabase(blockerId: String, blockedId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            client.from("blocked_users").delete {
                filter {
                    eq("blocker_id", blockerId)
                    eq("blocked_id", blockedId)
                }
            }

            Log.d(TAG, "âœ… User $blockedId unblocked by $blockerId")
            Result.success("User unblocked")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to unblock user", e)
            Result.failure(e)
        }
    }

    /**
     * Bookmark a post
     */
    suspend fun bookmarkPost(userId: String, postId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val bookmarkData = buildJsonObject {
                put("user_id", userId)
                put("post_id", postId)
                put("created_at", nowTimestamp())
            }

            client.safeInsert("bookmarks", bookmarkData)

            Log.d(TAG, "âœ… Post $postId bookmarked by $userId")
            Result.success("Post bookmarked")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to bookmark post", e)
            Result.failure(e)
        }
    }

    /**
     * Remove bookmark
     */
    suspend fun removeBookmark(userId: String, postId: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            client.from("bookmarks").delete {
                filter {
                    eq("user_id", userId)
                    eq("post_id", postId)
                }
            }

            Log.d(TAG, "âœ… Bookmark removed for post $postId")
            Result.success("Bookmark removed")
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to remove bookmark", e)
            Result.failure(e)
        }
    }

    /**
     * Search posts
     */
    suspend fun searchPosts(query: String, limit: Int = 20): Result<List<JsonObject>> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not configured"))
        }

        try {
            val result = safeSelect(
                table = "posts",
                columns = "*",
                filters = "content=ilike.*$query*&limit=$limit"
            )
            val list = result.map { it.jsonObject }

            Log.d(TAG, "âœ… Found ${list.size} posts matching '$query'")
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to search posts", e)
            Result.failure(e)
        }
    }

    /**
     * Search users
     */
    suspend fun searchUsers(query: String, limit: Int = 20): Result<List<JsonObject>> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not configured"))
        }

        try {
            val result = safeSelect(
                table = "users",
                columns = "*",
                filters = "or=(username.ilike.*$query*,display_name.ilike.*$query*)&limit=$limit"
            )
            val list = result.map { it.jsonObject }

            Log.d(TAG, "âœ… Found ${list.size} users matching '$query'")
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Failed to search users", e)
            Result.failure(e)
        }
    }
}

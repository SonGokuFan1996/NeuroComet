package com.kyilmaz.neuronetworkingtitle

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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
    // =========================================================================

    @Serializable
    data class TestPost(
        val user_id: String,
        val content: String,
        val image_url: String? = null,
        val video_url: String? = null,
        val likes: Int = 0,
        val created_at: String // Required - always provide timestamp
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
            val testPost = TestPost(
                user_id = "test_user_${System.currentTimeMillis()}",
                content = "🧪 Test post from NeuroNet DevOptions!\n\nThis is a test to verify Supabase connectivity. 🚀",
                likes = (0..100).random(),
                created_at = nowTimestamp()
            )

            client.postgrest["posts"].insert(testPost)

            Log.d(TAG, "✅ Test post sent successfully")
            Result.success("Test post sent successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send test post", e)
            Result.failure(e)
        }
    }

    /**
     * Send a test user to the users table
     */
    suspend fun sendTestUser(): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val randomId = (1000..9999).random()
            val now = nowTimestamp()
            val testUser = TestUser(
                email = "testuser$randomId@neuronet.dev",
                username = "testuser$randomId",
                display_name = "Test User $randomId",
                avatar_url = avatarUrl("testuser$randomId"),
                bio = "🧪 Test user created from DevOptions",
                created_at = now,
                updated_at = now
            )

            client.postgrest["users"].insert(testUser)

            Log.d(TAG, "✅ Test user sent successfully: ${testUser.username}")
            Result.success("Test user '${testUser.username}' created!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send test user", e)
            Result.failure(e)
        }
    }

    /**
     * Send a test like to the post_likes table
     */
    suspend fun sendTestLike(postId: Long = 1): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val testLike = TestPostLike(
                post_id = postId,
                user_id = "test_user_${System.currentTimeMillis()}",
                created_at = nowTimestamp()
            )

            client.postgrest["post_likes"].insert(testLike)

            Log.d(TAG, "✅ Test like sent for post #$postId")
            Result.success("Like added to post #$postId!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send test like", e)
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
                TestPost(
                    user_id = "bulk_test_user_$i",
                    content = "🧪 Bulk test post #$i\n\n${getRandomNeuroContent()}",
                    likes = (0..500).random(),
                    created_at = nowTimestamp()
                )
            }

            client.postgrest["posts"].insert(testPosts)

            Log.d(TAG, "✅ $count test posts sent successfully")
            Result.success("$count test posts created!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send bulk test posts", e)
            Result.failure(e)
        }
    }

    /**
     * Query and count rows in a table
     */
    suspend fun getTableRowCount(tableName: String): Result<Int> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            val result = client.postgrest[tableName]
                .select()
                .decodeList<Map<String, Any?>>()

            Log.d(TAG, "📊 Table '$tableName' has ${result.size} rows")
            Result.success(result.size)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to count rows in '$tableName'", e)
            Result.failure(e)
        }
    }

    /**
     * Clear all test data from a table (DANGEROUS - use with caution!)
     */
    suspend fun clearTestData(tableName: String): Result<String> = withContext(Dispatchers.IO) {
        val client = AppSupabaseClient.client
            ?: return@withContext Result.failure(Exception("Supabase not configured"))

        try {
            Log.d(TAG, "🗑️ Attempting to clear test data from '$tableName'...")

            // Only delete rows with test identifiers
            when (tableName) {
                "posts" -> {
                    // Delete posts where user_id starts with "test_" or "bulk_test_"
                    client.postgrest["posts"]
                        .delete {
                            filter {
                                or {
                                    like("user_id", "test_%")
                                    like("user_id", "bulk_test_%")
                                }
                            }
                        }
                    Log.d(TAG, "✅ Deleted test posts")
                }
                "users" -> {
                    // Delete users with test email domain
                    client.postgrest["users"]
                        .delete {
                            filter {
                                like("email", "%@neuronet.dev")
                            }
                        }
                    Log.d(TAG, "✅ Deleted test users")
                }
                "post_likes" -> {
                    // Delete likes from test users
                    client.postgrest["post_likes"]
                        .delete {
                            filter {
                                like("user_id", "test_%")
                            }
                        }
                    Log.d(TAG, "✅ Deleted test likes")
                }
                else -> {
                    return@withContext Result.failure(Exception("Unknown table: $tableName"))
                }
            }

            Log.d(TAG, "🗑️ Test data cleared from '$tableName'")
            Result.success("Test data cleared from '$tableName'!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to clear test data from '$tableName'", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // Helper Functions
    // =========================================================================

    private fun getRandomNeuroContent(): String {
        val contents = listOf(
            "Today's hyperfocus session was incredibly productive! 🎯",
            "Finally found a stim toy that works perfectly for meetings 🌀",
            "Reminder: Your brain isn't broken, it's just different 💜",
            "Does anyone else feel like they have 100 tabs open in their brain? 🧠",
            "Found a great sensory-friendly restaurant! Dim lights, quiet music ✨",
            "Executive dysfunction is hitting hard today. Sending solidarity 💪",
            "Just discovered body doubling and it's a game changer!",
            "My special interest just connected to my work project somehow 🎉",
            "Celebrating small wins today: I remembered to eat lunch! 🍽️",
            "Noise-canceling headphones are a neurodivergent essential 🎧"
        )
        return contents.random()
    }
}
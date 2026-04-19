package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Repository for managing posts with Supabase persistence.
 * 
 * All Supabase calls use the safe REST helpers from SupabaseInsertHelper.kt
 * to avoid the kotlin-reflect typeOf() crash on Android.
 */
object PostsRepository {

    private const val TAG = "PostsRepository"
    private const val TABLE_POSTS = "posts"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch the latest posts from Supabase.
     */
    suspend fun fetchPosts(): List<Post> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()

        try {
            // Fetch posts ordered by created_at descending
            val rows = safeSelect(
                table = TABLE_POSTS,
                columns = "*",
                filters = "order=created_at.desc"
            )

            rows.mapNotNull { element ->
                try {
                    // Manual mapping because Post has complex fields and we want to avoid reflection crashes
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.long
                    val createdAt = obj["created_at"]?.jsonPrimitive?.content ?: Instant.now().toString()
                    val content = obj["content"]?.jsonPrimitive?.content ?: ""
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: "unknown"
                    val likes = obj["likes"]?.jsonPrimitive?.int ?: 0
                    val comments = obj["comments"]?.jsonPrimitive?.int ?: 0
                    val shares = obj["shares"]?.jsonPrimitive?.int ?: 0
                    
                    // Note: userAvatar is often in a profiles table join, 
                    // for now we'll use a seed-based generator if null
                    val avatar = obj["user_avatar"]?.jsonPrimitive?.content ?: avatarUrl(userId)
                    
                    val imageUrl = obj["image_url"]?.jsonPrimitive?.content
                    val videoUrl = obj["video_url"]?.jsonPrimitive?.content
                    
                    // Decode mediaItems if present
                    val mediaItems = obj["media_items"]?.let {
                        try {
                            json.decodeFromJsonElement<List<MediaItem>>(it)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } ?: emptyList()
                    
                    val minAudienceStr = obj["min_audience"]?.jsonPrimitive?.content ?: "UNDER_13"
                    val minAudience = try { Audience.valueOf(minAudienceStr) } catch(e: Exception) { Audience.UNDER_13 }
                    val backgroundColor = obj["background_color"]?.jsonPrimitive?.long

                    Post(
                        id = id,
                        createdAt = createdAt,
                        content = content,
                        userId = userId,
                        likes = likes,
                        comments = comments,
                        shares = shares,
                        isLikedByMe = false, // Will be updated by caller (per-user state)
                        userAvatar = avatar,
                        imageUrl = imageUrl,
                        videoUrl = videoUrl,
                        mediaItems = mediaItems,
                        minAudience = minAudience,
                        backgroundColor = backgroundColor
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse post: ${element}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchPosts failed", e)
            emptyList()
        }
    }

    /**
     * Create a new post in Supabase.
     */
    suspend fun createPost(
        userId: String,
        content: String,
        imageUrl: String? = null,
        videoUrl: String? = null,
        mediaItems: List<MediaItem> = emptyList(),
        minAudience: Audience = Audience.UNDER_13,
        backgroundColor: Long? = null,
        category: String? = null,
        locationTag: String? = null
    ): Result<Post> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not available"))
        }

        try {
            val now = Instant.now().toString()
            val payload = buildJsonObject {
                put("user_id", userId)
                put("content", content)
                put("created_at", now)
                imageUrl?.let { put("image_url", it) }
                videoUrl?.let { put("video_url", it) }
                if (mediaItems.isNotEmpty()) {
                    put("media_items", json.encodeToJsonElement(mediaItems))
                }
                put("min_audience", minAudience.name)
                backgroundColor?.let { put("background_color", it) }
                category?.let { put("category", it) }
                locationTag?.let { put("location_tag", it) }
                put("likes", 0)
                put("comments", 0)
                put("shares", 0)
            }

            val fallbackPayload = buildJsonObject {
                put("user_id", userId)
                put("content", content)
                put("created_at", now)
                imageUrl?.let { put("image_url", it) }
                videoUrl?.let { put("video_url", it) }
                if (mediaItems.isNotEmpty()) {
                    put("media_items", json.encodeToJsonElement(mediaItems))
                }
                put("min_audience", minAudience.name)
                backgroundColor?.let { put("background_color", it) }
                category?.let { put("category", it) }
                put("likes", 0)
                put("comments", 0)
                put("shares", 0)
            }

            runCatching {
                AppSupabaseClient.client!!.safeInsert(TABLE_POSTS, payload)
            }.getOrElse {
                AppSupabaseClient.client!!.safeInsert(TABLE_POSTS, fallbackPayload)
            }

            // Return the built post (optimistic)
            Result.success(Post(
                id = System.currentTimeMillis(), // Temporary local ID
                createdAt = now,
                content = content,
                userId = userId,
                likes = 0,
                comments = 0,
                shares = 0,
                isLikedByMe = false,
                userAvatar = avatarUrl(userId),
                imageUrl = imageUrl,
                videoUrl = videoUrl,
                mediaItems = mediaItems,
                minAudience = minAudience,
                backgroundColor = backgroundColor,
                locationTag = locationTag
            ))
        } catch (e: Exception) {
            Log.e(TAG, "createPost failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a post from Supabase.
     */
    suspend fun deletePost(postId: Long, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext Result.failure(Exception("Supabase not available"))

        try {
            safeDelete(
                table = TABLE_POSTS,
                filters = "id=eq.$postId&user_id=eq.$userId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deletePost failed", e)
            Result.failure(e)
        }
    }
}

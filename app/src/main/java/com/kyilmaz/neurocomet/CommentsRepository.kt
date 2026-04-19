package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Repository for managing post comments with Supabase persistence.
 */
object CommentsRepository {

    private const val TAG = "CommentsRepository"
    private const val TABLE_COMMENTS = "post_comments"
    private const val TABLE_POSTS = "posts"

    /**
     * Fetch comments for a specific post.
     */
    suspend fun fetchComments(postId: Long): List<Comment> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()

        try {
            val rows = safeSelect(
                table = TABLE_COMMENTS,
                columns = "*",
                filters = "post_id=eq.$postId&order=created_at.asc"
            )

            rows.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: ""
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: ""
                    val content = obj["content"]?.jsonPrimitive?.content ?: ""
                    val timestamp = obj["created_at"]?.jsonPrimitive?.content ?: ""

                    Comment(
                        id = id,
                        postId = postId,
                        userId = userId,
                        userAvatar = avatarUrl(userId),
                        content = content,
                        timestamp = timestamp
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchComments failed", e)
            emptyList()
        }
    }

    /**
     * Add a comment to a post in Supabase.
     */
    suspend fun addComment(
        postId: Long,
        userId: String,
        content: String
    ): Result<Comment> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not available"))
        }

        try {
            val now = Instant.now().toString()
            val payload = buildJsonObject {
                put("post_id", postId)
                put("user_id", userId)
                put("content", content)
                put("created_at", now)
            }

            AppSupabaseClient.client!!.safeInsert(TABLE_COMMENTS, payload)
            
            // Increment comment count on the post
            updatePostCommentCount(postId)

            Result.success(Comment(
                id = "c-${System.currentTimeMillis()}",
                postId = postId,
                userId = userId,
                userAvatar = avatarUrl(userId),
                content = content,
                timestamp = now
            ))
        } catch (e: Exception) {
            Log.e(TAG, "addComment failed", e)
            Result.failure(e)
        }
    }

    /**
     * Increment the comment count on a post.
     */
    private suspend fun updatePostCommentCount(postId: Long) {
        try {
            val rows = safeSelect(
                table = TABLE_POSTS,
                columns = "id,comments",
                filters = "id=eq.$postId"
            )

            if (rows.isNotEmpty()) {
                val currentCount = rows.first().jsonObject["comments"]?.jsonPrimitive?.int ?: 0
                safeUpdate(
                    table = TABLE_POSTS,
                    body = buildJsonObject {
                        put("comments", currentCount + 1)
                    },
                    filters = "id=eq.$postId"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update post comment count", e)
        }
    }
}

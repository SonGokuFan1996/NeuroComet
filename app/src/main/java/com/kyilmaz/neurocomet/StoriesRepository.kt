package com.kyilmaz.neurocomet

import android.util.Log
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Repository for managing stories/moments with Supabase persistence.
 */
object StoriesRepository {

    private const val TAG = "StoriesRepository"
    private const val TABLE_STORIES = "stories"
    private val legacyStoriesColumns = setOf(
        "content_type",
        "text_overlay",
        "background_color",
        "background_color_end",
        "link_preview"
    )

    private fun storyInsertFailureNeedsUserBackfill(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return (
            "violates foreign key constraint" in message ||
                "\"23503\"" in message
            ) && (
            "stories" in message ||
                "user_id" in message ||
                "public.users" in message
            )
    }

            private fun storyInsertFailureNeedsLegacySchemaFallback(error: Throwable): Boolean {
                val message = error.message.orEmpty().lowercase()
                return (
                    "schema cache" in message ||
                        "could not find the" in message ||
                        "column" in message
                    ) && legacyStoriesColumns.any { it in message }
            }

            private fun canFallbackToLegacyStorySchema(
                contentType: StoryContentType,
                textOverlay: String?,
                backgroundColorEnd: Long?,
                linkPreview: LinkPreviewData?
            ): Boolean {
                return contentType == StoryContentType.IMAGE &&
                    textOverlay == null &&
                    backgroundColorEnd == null &&
                    linkPreview == null
            }

            private fun buildStoryPayload(
                id: String,
                userId: String,
                imageUrl: String,
                duration: Long,
                contentType: StoryContentType,
                textOverlay: String?,
                backgroundColor: Long,
                backgroundColorEnd: Long?,
                linkPreview: LinkPreviewData?,
                legacySchema: Boolean = false
            ) = buildJsonObject {
                put("id", id)
                put("user_id", userId)
                put("image_url", imageUrl)
                put("duration", duration)
                if (!legacySchema) {
                    put("content_type", contentType.name)
                    textOverlay?.let { put("text_overlay", it) }
                    put("background_color", backgroundColor)
                    backgroundColorEnd?.let { put("background_color_end", it) }
                    linkPreview?.let {
                        put("link_preview", AppSupabaseClient.json.encodeToJsonElement(LinkPreviewData.serializer(), it))
                    }
                }
                put("created_at", Instant.now().toString())
            }

    private suspend fun ensurePublicUserExists(userId: String) {
        val client = AppSupabaseClient.client ?: return
        val currentUser = try {
            client.auth.currentUserOrNull()
        } catch (_: Exception) {
            null
        } ?: return

        if (currentUser.id != userId) return

        val meta = currentUser.userMetadata
        val email = currentUser.email.orEmpty()
        val displayName = meta?.get("display_name")?.jsonPrimitive?.contentOrNull
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: "NeuroComet user"
        val username = meta?.get("username")?.jsonPrimitive?.contentOrNull
            ?: "user_${userId.take(8)}"
        val now = Instant.now().toString()

        client.safeUpsert("users", buildJsonObject {
            put("id", userId)
            put("email", email)
            put("username", username)
            put("display_name", displayName)
            put("created_at", now)
            put("updated_at", now)
        })
    }

    /**
     * Fetch all active stories from Supabase and group them by user.
     * Stories older than 24 hours are filtered out by the database (ideally) 
     * or we filter them here.
     */
    suspend fun fetchStories(): List<Story> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()

        try {
            // Fetch recent stories
            val rows = safeSelect(
                table = TABLE_STORIES,
                columns = "*",
                filters = "order=created_at.desc"
            )

            // Group by userId
            val itemsByUser = rows.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: ""
                    val userId = obj["user_id"]?.jsonPrimitive?.content ?: ""
                    val imageUrl = obj["image_url"]?.jsonPrimitive?.content ?: ""
                    val duration = obj["duration"]?.jsonPrimitive?.long ?: 5000L
                    val createdAt = obj["created_at"]?.jsonPrimitive?.content ?: ""
                    
                    if (userId.isEmpty()) return@mapNotNull null
                    
                    // Filter locally for 24h safety
                    val createdInstant = Instant.parse(createdAt)
                    if (createdInstant.isBefore(Instant.now().minusSeconds(86400))) return@mapNotNull null

                    val contentTypeStr = obj["content_type"]?.jsonPrimitive?.content ?: "IMAGE"
                    val contentType = try { StoryContentType.valueOf(contentTypeStr) } catch (e: Exception) { StoryContentType.IMAGE }

                    // TEXT_ONLY stories store their text in image_url; other types need a real URL
                    if (contentType != StoryContentType.TEXT_ONLY && imageUrl.isEmpty()) return@mapNotNull null
                    val textOverlay = obj["text_overlay"]?.jsonPrimitive?.content
                    val backgroundColor = obj["background_color"]?.jsonPrimitive?.long ?: 0xFF1a1a2eL
                    val backgroundColorEnd = obj["background_color_end"]?.jsonPrimitive?.longOrNull
                    val linkPreviewJson = obj["link_preview"]?.jsonObject
                    val linkPreview = if (linkPreviewJson != null) {
                        try {
                            AppSupabaseClient.json.decodeFromJsonElement<LinkPreviewData>(linkPreviewJson)
                        } catch (e: Exception) { null }
                    } else null

                    userId to StoryItem(
                        id = id,
                        imageUrl = imageUrl,
                        duration = duration,
                        contentType = contentType,
                        textOverlay = textOverlay,
                        backgroundColor = backgroundColor,
                        backgroundColorEnd = backgroundColorEnd,
                        linkPreview = linkPreview
                    )
                } catch (e: Exception) {
                    null
                }
            }.groupBy({ it.first }, { it.second })

            // Build Story objects
            itemsByUser.map { (userId, items) ->
                Story(
                    id = "story_$userId",
                    userName = userId,
                    userAvatar = avatarUrl(userId),
                    items = items,
                    isViewed = false, // Default to false, local logic handles viewing
                    userId = userId
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchStories failed", e)
            emptyList()
        }
    }

    /**
     * Create a new story item in Supabase.
     */
    suspend fun createStory(
        userId: String,
        imageUrl: String,
        duration: Long,
        contentType: StoryContentType = StoryContentType.IMAGE,
        textOverlay: String? = null,
        backgroundColor: Long = 0xFF1a1a2eL,
        backgroundColorEnd: Long? = null,
        linkPreview: LinkPreviewData? = null
    ): Result<StoryItem> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) {
            return@withContext Result.failure(Exception("Supabase not available"))
        }

        try {
            val client = AppSupabaseClient.client!!
            val id = java.util.UUID.randomUUID().toString()
            val payload = buildStoryPayload(
                id = id,
                userId = userId,
                imageUrl = imageUrl,
                duration = duration,
                contentType = contentType,
                textOverlay = textOverlay,
                backgroundColor = backgroundColor,
                backgroundColorEnd = backgroundColorEnd,
                linkPreview = linkPreview
            )
            val legacyPayload = if (
                canFallbackToLegacyStorySchema(
                    contentType = contentType,
                    textOverlay = textOverlay,
                    backgroundColorEnd = backgroundColorEnd,
                    linkPreview = linkPreview
                )
            ) {
                buildStoryPayload(
                    id = id,
                    userId = userId,
                    imageUrl = imageUrl,
                    duration = duration,
                    contentType = contentType,
                    textOverlay = textOverlay,
                    backgroundColor = backgroundColor,
                    backgroundColorEnd = backgroundColorEnd,
                    linkPreview = linkPreview,
                    legacySchema = true
                )
            } else {
                null
            }

            try {
                ensurePublicUserExists(userId)
                client.safeInsert(TABLE_STORIES, payload)
            } catch (e: Exception) {
                when {
                    storyInsertFailureNeedsUserBackfill(e) -> {
                        Log.w(TAG, "Story insert failed because the public.users row is missing; backfilling and retrying", e)
                        ensurePublicUserExists(userId)
                        client.safeInsert(TABLE_STORIES, payload)
                    }
                    legacyPayload != null && storyInsertFailureNeedsLegacySchemaFallback(e) -> {
                        Log.w(TAG, "Story insert failed because the deployed stories table is missing newer columns; retrying with legacy payload", e)
                        client.safeInsert(TABLE_STORIES, legacyPayload)
                    }
                    else -> throw e
                }
            }

            Result.success(StoryItem(
                id = id,
                imageUrl = imageUrl,
                duration = duration,
                contentType = contentType,
                textOverlay = textOverlay,
                backgroundColor = backgroundColor,
                backgroundColorEnd = backgroundColorEnd,
                linkPreview = linkPreview
            ))
        } catch (e: Exception) {
            Log.e(TAG, "createStory failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a specific story item.
     */
    suspend fun deleteStory(storyId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext Result.failure(Exception("Supabase not available"))

        try {
            safeDelete(
                table = TABLE_STORIES,
                filters = "id=eq.$storyId&user_id=eq.$userId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteStory failed", e)
            Result.failure(e)
        }
    }
}

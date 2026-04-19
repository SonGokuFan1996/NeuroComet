package com.kyilmaz.neurocomet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Repository for managing notifications with Supabase persistence.
 * Falls back gracefully when Supabase is not configured.
 *
 * DB table: `notifications`
 *   id UUID PK, user_id UUID, actor_id UUID?, type TEXT,
 *   message TEXT, target_id TEXT, target_type TEXT,
 *   is_read BOOLEAN, created_at TIMESTAMPTZ
 */
object NotificationsRepository {

    private const val TAG = "NotificationsRepo"
    private const val TABLE = "notifications"

    /**
     * Fetch notifications for the authenticated user, newest first.
     */
    suspend fun fetchNotifications(userId: String): List<NotificationItem> = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext emptyList()

        try {
            val rows = safeSelect(
                table = TABLE,
                columns = "*",
                filters = "user_id=eq.$userId&order=created_at.desc&limit=50"
            )

            rows.mapNotNull { element ->
                try {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val type = obj["type"]?.jsonPrimitive?.content ?: "SYSTEM"
                    val message = obj["message"]?.jsonPrimitive?.content ?: ""
                    val targetId = obj["target_id"]?.jsonPrimitive?.content
                    val targetType = obj["target_type"]?.jsonPrimitive?.content
                    val isRead = obj["is_read"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                    val createdAt = obj["created_at"]?.jsonPrimitive?.content ?: ""
                    val actorId = obj["actor_id"]?.jsonPrimitive?.content

                    val notifType = try {
                        NotificationType.valueOf(type.uppercase())
                    } catch (_: Exception) {
                        NotificationType.SYSTEM
                    }

                    // Build a user-friendly title from the type
                    val title = when (notifType) {
                        NotificationType.LIKE -> "New Like"
                        NotificationType.COMMENT -> "New Comment"
                        NotificationType.FOLLOW -> "New Follower"
                        NotificationType.MENTION -> "You were mentioned"
                        NotificationType.REPOST -> "Repost"
                        NotificationType.BADGE -> "Badge Earned!"
                        NotificationType.SYSTEM -> "System"
                        NotificationType.WELCOME -> "Welcome!"
                        NotificationType.SAFETY_ALERT -> "Safety Alert"
                    }

                    NotificationItem(
                        id = id,
                        title = title,
                        message = message,
                        timestamp = createdAt,
                        type = notifType,
                        isRead = isRead,
                        avatarUrl = actorId?.let { avatarUrl(it) },
                        relatedUserId = actorId,
                        relatedPostId = if (targetType == "post") targetId?.toLongOrNull() else null,
                        actionUrl = targetId?.takeIf { it.startsWith("/") }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse notification row", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchNotifications failed", e)
            emptyList()
        }
    }

    /**
     * Mark a single notification as read.
     */
    suspend fun markAsRead(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            safeUpdate(
                table = TABLE,
                body = buildJsonObject { put("is_read", true) },
                filters = "id=eq.$notificationId"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead failed", e)
            false
        }
    }

    /**
     * Mark all notifications for a user as read.
     */
    suspend fun markAllAsRead(userId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            safeUpdate(
                table = TABLE,
                body = buildJsonObject { put("is_read", true) },
                filters = "user_id=eq.$userId&is_read=eq.false"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "markAllAsRead failed", e)
            false
        }
    }

    /**
     * Delete a notification.
     */
    suspend fun deleteNotification(notificationId: String): Boolean = withContext(Dispatchers.IO) {
        if (!AppSupabaseClient.isAvailable()) return@withContext false
        try {
            safeDelete(
                table = TABLE,
                filters = "id=eq.$notificationId"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteNotification failed", e)
            false
        }
    }
}


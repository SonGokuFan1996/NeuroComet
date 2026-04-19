package com.kyilmaz.neurocomet

import android.net.Uri
import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.time.Instant

/**
 * Repository for real-time messaging via Supabase.
 * Falls back gracefully when Supabase is not configured.
 */
object MessagesRepository {

    private const val TAG = "MessagesRepository"

    // ---- Supabase row DTOs ----

    @Serializable
    data class DbMessage(
        val id: String,
        val conversation_id: String,
        val sender_id: String,
        val content: String,
        val type: String = "text",
        val media_url: String? = null,
        val is_read: Boolean = false,
        val read_at: String? = null,
        val created_at: String
    )

    @Serializable
    data class DbConversation(
        val id: String,
        val is_group: Boolean = false,
        val group_name: String? = null,
        val created_at: String,
        val updated_at: String
    )

    @Serializable
    data class DbParticipant(
        val id: String,
        val conversation_id: String,
        val user_id: String,
        val joined_at: String
    )

    @Serializable
    data class DbProfile(
        val id: String,
        val display_name: String? = null,
        val username: String? = null,
        val avatar_url: String? = null,
        val bio: String? = null,
        val is_verified: Boolean = false
    )

    private fun parseProfile(obj: kotlinx.serialization.json.JsonObject): DbProfile? {
        val id = obj["id"]?.jsonPrimitive?.content ?: return null
        return DbProfile(
            id = id,
            display_name = obj["display_name"]?.jsonPrimitive?.contentOrNull,
            username = obj["username"]?.jsonPrimitive?.contentOrNull,
            avatar_url = obj["avatar_url"]?.jsonPrimitive?.contentOrNull,
            bio = obj["bio"]?.jsonPrimitive?.contentOrNull,
            is_verified = obj["is_verified"]?.jsonPrimitive?.boolean ?: false
        )
    }

    // ---- Public API ----

    /**
     * Fetch all conversations for the current user, with last message and participant profiles.
     */
    suspend fun fetchConversations(userId: String): List<Conversation> {
        return try {
            // 1. Get all conversation IDs this user participates in
            val participations = safeSelect(
                table = "conversation_participants",
                columns = "conversation_id",
                filters = "user_id=eq.$userId"
            )

            val convIds = participations.mapNotNull { it.jsonObject["conversation_id"]?.jsonPrimitive?.content }
            if (convIds.isEmpty()) return emptyList()

            // 2. Fetch conversations
            val filters = "id=in.(${convIds.joinToString(",")})"
            val conversationsArray = safeSelect(
                table = "conversations",
                columns = "*",
                filters = filters
            )
            val conversations = conversationsArray.map { 
                val obj = it.jsonObject
                DbConversation(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    is_group = obj["is_group"]?.jsonPrimitive?.boolean ?: false,
                    group_name = obj["group_name"]?.jsonPrimitive?.contentOrNull,
                    created_at = obj["created_at"]?.jsonPrimitive?.content ?: "",
                    updated_at = obj["updated_at"]?.jsonPrimitive?.content ?: ""
                )
            }

            // 3. Fetch all participants for these conversations
            val allParticipantsArray = safeSelect(
                table = "conversation_participants",
                columns = "*",
                filters = filters.replace("id=in", "conversation_id=in")
            )
            val allParticipants = allParticipantsArray.map {
                val obj = it.jsonObject
                DbParticipant(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    conversation_id = obj["conversation_id"]?.jsonPrimitive?.content ?: "",
                    user_id = obj["user_id"]?.jsonPrimitive?.content ?: "",
                    joined_at = obj["joined_at"]?.jsonPrimitive?.content ?: ""
                )
            }

            // 4. Fetch profiles for all participants
            val allUserIds = allParticipants.map { it.user_id }.distinct()
            val profiles = if (allUserIds.isNotEmpty()) {
                val userFilters = "id=in.(${allUserIds.joinToString(",")})"
                val profilesArray = safeSelect(
                    table = "profiles",
                    columns = "*",
                    filters = userFilters
                )
                profilesArray.map {
                    val obj = it.jsonObject
                    DbProfile(
                        id = obj["id"]?.jsonPrimitive?.content ?: "",
                        display_name = obj["display_name"]?.jsonPrimitive?.contentOrNull,
                        username = obj["username"]?.jsonPrimitive?.contentOrNull,
                        avatar_url = obj["avatar_url"]?.jsonPrimitive?.contentOrNull,
                        bio = obj["bio"]?.jsonPrimitive?.contentOrNull,
                        is_verified = obj["is_verified"]?.jsonPrimitive?.boolean ?: false
                    )
                }
            } else emptyList()

            // 5. Fetch last message for each conversation
            val result = conversations.map { conv ->
                val participants = allParticipants
                    .filter { it.conversation_id == conv.id }
                    .map { it.user_id }

                val lastMessagesArray = safeSelect(
                    table = "dm_messages",
                    columns = "*",
                    filters = "conversation_id=eq.${conv.id}&order=created_at.desc&limit=1"
                )
                val lastMessages = lastMessagesArray.map {
                    val obj = it.jsonObject
                    DbMessage(
                        id = obj["id"]?.jsonPrimitive?.content ?: "",
                        conversation_id = obj["conversation_id"]?.jsonPrimitive?.content ?: "",
                        sender_id = obj["sender_id"]?.jsonPrimitive?.content ?: "",
                        content = obj["content"]?.jsonPrimitive?.content ?: "",
                        type = obj["type"]?.jsonPrimitive?.content ?: "text",
                        media_url = obj["media_url"]?.jsonPrimitive?.contentOrNull,
                        is_read = obj["is_read"]?.jsonPrimitive?.boolean ?: false,
                        read_at = obj["read_at"]?.jsonPrimitive?.contentOrNull,
                        created_at = obj["created_at"]?.jsonPrimitive?.content ?: ""
                    )
                }

                val unreadCount = safeSelect(
                    table = "dm_messages",
                    columns = "id",
                    filters = "conversation_id=eq.${conv.id}&is_read=eq.false&sender_id=neq.$userId"
                ).size

                val messages = lastMessages.map { it.toDirectMessage(participants, userId) }

                Conversation(
                    id = conv.id,
                    participants = participants,
                    messages = messages,
                    lastMessageTimestamp = conv.updated_at,
                    unreadCount = unreadCount,
                    isGroup = conv.is_group,
                    groupName = conv.group_name
                )
            }.sortedByDescending { it.lastMessageTimestamp }

            result
        } catch (e: Exception) {
            Log.e(TAG, "fetchConversations failed", e)
            emptyList()
        }
    }

    /**
     * Fetch all messages for a conversation.
     */
    suspend fun fetchMessages(conversationId: String, userId: String): List<DirectMessage> {
        return try {
            // Get participants for recipientId mapping
            val participantsArray = safeSelect("conversation_participants", "user_id", "conversation_id=eq.$conversationId")
            val participants = participantsArray.mapNotNull { it.jsonObject["user_id"]?.jsonPrimitive?.content }

            val dbMessagesArray = safeSelect("dm_messages", "*", "conversation_id=eq.$conversationId&order=created_at.asc")
            val dbMessages = dbMessagesArray.map {
                val obj = it.jsonObject
                DbMessage(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    conversation_id = obj["conversation_id"]?.jsonPrimitive?.content ?: "",
                    sender_id = obj["sender_id"]?.jsonPrimitive?.content ?: "",
                    content = obj["content"]?.jsonPrimitive?.content ?: "",
                    type = obj["type"]?.jsonPrimitive?.content ?: "text",
                    media_url = obj["media_url"]?.jsonPrimitive?.contentOrNull,
                    is_read = obj["is_read"]?.jsonPrimitive?.boolean ?: false,
                    read_at = obj["read_at"]?.jsonPrimitive?.contentOrNull,
                    created_at = obj["created_at"]?.jsonPrimitive?.content ?: ""
                )
            }

            dbMessages.map { it.toDirectMessage(participants, userId) }
        } catch (e: Exception) {
            Log.e(TAG, "fetchMessages failed", e)
            emptyList()
        }
    }

    /**
     * Send a message to a conversation.
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String
    ): DirectMessage? {
        return try {
            val payload = kotlinx.serialization.json.buildJsonObject {
                put("conversation_id", conversationId)
                put("sender_id", senderId)
                put("content", content)
                put("type", "text")
            }
            val inserted = AppSupabaseClient.client!!.safeInsert("dm_messages", payload, true)
                ?: return null

            val dbMsg = DbMessage(
                id = inserted["id"]?.jsonPrimitive?.content ?: "",
                conversation_id = inserted["conversation_id"]?.jsonPrimitive?.content ?: "",
                sender_id = inserted["sender_id"]?.jsonPrimitive?.content ?: "",
                content = inserted["content"]?.jsonPrimitive?.content ?: "",
                type = inserted["type"]?.jsonPrimitive?.content ?: "text",
                media_url = inserted["media_url"]?.jsonPrimitive?.contentOrNull,
                is_read = inserted["is_read"]?.jsonPrimitive?.boolean ?: false,
                read_at = inserted["read_at"]?.jsonPrimitive?.contentOrNull,
                created_at = inserted["created_at"]?.jsonPrimitive?.content ?: ""
            )

            val participantsArray = safeSelect(
                table = "conversation_participants",
                columns = "user_id",
                filters = "conversation_id=eq.$conversationId"
            )
            val participants = participantsArray.mapNotNull { it.jsonObject["user_id"]?.jsonPrimitive?.content }

            dbMsg.toDirectMessage(participants, senderId)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage failed", e)
            null
        }
    }

    /**
     * Mark all messages in a conversation as read for the current user.
     */
    suspend fun markConversationRead(conversationId: String, userId: String) {
        try {
            safeUpdate(
                table = "dm_messages",
                body = kotlinx.serialization.json.buildJsonObject {
                    put("is_read", kotlinx.serialization.json.JsonPrimitive(true))
                },
                filters = "conversation_id=eq.$conversationId&sender_id=neq.$userId&is_read=eq.false"
            )
        } catch (e: Exception) {
            Log.e(TAG, "markConversationRead failed", e)
        }
    }

    /**
     * Create a new 1:1 conversation between two users. Returns the conversation ID.
     * If a conversation already exists between them, returns that one.
     */
    suspend fun getOrCreateConversation(userId: String, otherUserId: String): String? {
        return try {
            // Check if conversation already exists between these two users
            val myConvs = safeSelect("conversation_participants", "conversation_id", "user_id=eq.$userId")
            val myConvIds = myConvs.mapNotNull { it.jsonObject["conversation_id"]?.jsonPrimitive?.content }

            if (myConvIds.isNotEmpty()) {
                val otherConvs = safeSelect(
                    table = "conversation_participants",
                    columns = "conversation_id",
                    filters = "user_id=eq.$otherUserId&conversation_id=in.(${myConvIds.joinToString(",")})"
                )
                val sharedIds = otherConvs.mapNotNull { it.jsonObject["conversation_id"]?.jsonPrimitive?.content }

                if (sharedIds.isNotEmpty()) {
                    return sharedIds.first()
                }
            }

            // Create new conversation
            val convPayload = kotlinx.serialization.json.buildJsonObject {
                put("is_group", false)
            }
            val newConv = AppSupabaseClient.client!!.safeInsert("conversations", convPayload, true)
                ?: return null
            val newId = newConv["id"]?.jsonPrimitive?.content ?: return null

            // Add both participants
            val participants = listOf(
                kotlinx.serialization.json.buildJsonObject {
                    put("conversation_id", newId)
                    put("user_id", userId)
                },
                kotlinx.serialization.json.buildJsonObject {
                    put("conversation_id", newId)
                    put("user_id", otherUserId)
                }
            )
            AppSupabaseClient.client!!.safeInsertList("conversation_participants", participants)

            newId
        } catch (e: Exception) {
            Log.e(TAG, "getOrCreateConversation failed", e)
            null
        }
    }

    /**
     * Look up a user profile by username (for starting new chats).
     */
    suspend fun findUserByUsername(username: String): DbProfile? {
        return try {
            val profiles = safeSelect("profiles", "*", "username=eq.$username&limit=1")
            profiles.firstOrNull()?.jsonObject?.let(::parseProfile)
        } catch (e: Exception) {
            Log.e(TAG, "findUserByUsername failed", e)
            null
        }
    }

    /**
     * Fetch a profile by user ID.
     */
    suspend fun getProfile(userId: String): DbProfile? {
        return try {
            val profiles = safeSelect("profiles", "*", "id=eq.$userId&limit=1")
            profiles.firstOrNull()?.jsonObject?.let(::parseProfile)
        } catch (e: Exception) {
            Log.e(TAG, "getProfile failed", e)
            null
        }
    }

    /**
     * Fetch all profiles for a list of user IDs.
     */
    suspend fun getProfiles(userIds: List<String>): Map<String, DbProfile> {
        if (userIds.isEmpty()) return emptyMap()
        return try {
            val filters = "id=in.(${userIds.joinToString(",")})"
            val profilesArray = safeSelect("profiles", "*", filters)
            profilesArray.mapNotNull { row ->
                parseProfile(row.jsonObject)?.let { profile -> profile.id to profile }
            }.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "getProfiles failed", e)
            emptyMap()
        }
    }

    /**
     * Fetch a small discoverable profile list for real authenticated users.
     * This powers developer-to-developer chat/call testing without relying on mock IDs.
     */
    suspend fun fetchDiscoverableProfiles(excludeUserId: String, limit: Int = 50): List<DbProfile> {
        return try {
            val profilesArray = safeSelect(
                table = "profiles",
                columns = "*",
                filters = "id=neq.$excludeUserId&limit=$limit"
            )
            profilesArray.mapNotNull { row -> parseProfile(row.jsonObject) }.sortedWith(
                compareBy<DbProfile> {
                    it.display_name?.lowercase()?.ifBlank { null }
                        ?: it.username?.lowercase()?.ifBlank { null }
                        ?: it.id.lowercase()
                }.thenBy { it.id }
            )
        } catch (e: Exception) {
            Log.e(TAG, "fetchDiscoverableProfiles failed", e)
            emptyList()
        }
    }

    suspend fun searchProfiles(query: String, excludeUserId: String, limit: Int = 25): List<DbProfile> {
        val normalized = query.trim().removePrefix("@").replace("*", "")
        if (normalized.isBlank()) {
            return fetchDiscoverableProfiles(excludeUserId, limit = limit)
        }

        return try {
            val encodedQuery = Uri.encode(normalized)
            val profilesArray = safeSelect(
                table = "profiles",
                columns = "*",
                filters = "id=neq.$excludeUserId&or=(display_name.ilike.*$encodedQuery*,username.ilike.*$encodedQuery*)&limit=$limit"
            )
            profilesArray.mapNotNull { row -> parseProfile(row.jsonObject) }
                .distinctBy { it.id }
                .sortedWith(
                    compareByDescending<DbProfile> { it.username.equals(normalized, ignoreCase = true) }
                        .thenByDescending { it.display_name.equals(normalized, ignoreCase = true) }
                        .thenBy {
                            it.display_name?.lowercase()?.ifBlank { null }
                                ?: it.username?.lowercase()?.ifBlank { null }
                                ?: it.id.lowercase()
                        }
                )
        } catch (e: Exception) {
            Log.e(TAG, "searchProfiles failed", e)
            emptyList()
        }
    }

    /**
     * Subscribe to new messages in a conversation via Supabase Realtime.
     * Returns a Flow of new DirectMessages, or an empty flow if not configured.
     */
    fun subscribeToMessages(conversationId: String, userId: String): Pair<Flow<DirectMessage>, RealtimeChannel?> {
        val client = AppSupabaseClient.client ?: return Pair(emptyFlow(), null)
        return try {
            val channel = client.channel("messages:$conversationId")
            val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "dm_messages"
            }.mapNotNull { action ->
                try {
                    val record = action.record
                    val msgConvId = record["conversation_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    // Only process messages for this conversation
                    if (msgConvId != conversationId) return@mapNotNull null

                    val senderId = record["sender_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    // Don't emit our own messages (we already show them optimistically)
                    if (senderId == userId) return@mapNotNull null

                    DirectMessage(
                        id = record["id"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                        senderId = senderId,
                        recipientId = userId,
                        content = record["content"]?.jsonPrimitive?.content ?: "",
                        timestamp = record["created_at"]?.jsonPrimitive?.content ?: "",
                        isRead = false,
                        deliveryStatus = MessageDeliveryStatus.SENT
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing realtime message", e)
                    null
                }
            }
            Pair(flow, channel)
        } catch (e: Exception) {
            Log.e(TAG, "subscribeToMessages failed", e)
            Pair(emptyFlow(), null)
        }
    }

    // ---- Helpers ----

    private fun DbMessage.toDirectMessage(participants: List<String>, currentUserId: String): DirectMessage {
        val recipientId = participants.firstOrNull { it != sender_id } ?: currentUserId
        val read = is_read || !read_at.isNullOrBlank()
        return DirectMessage(
            id = id,
            senderId = sender_id,
            recipientId = recipientId,
            content = content,
            timestamp = created_at,
            isRead = read,
            deliveryStatus = MessageDeliveryStatus.SENT,
            moderationStatus = ModerationStatus.CLEAN
        )
    }
}


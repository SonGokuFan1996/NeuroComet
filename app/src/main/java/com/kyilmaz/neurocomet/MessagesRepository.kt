package com.kyilmaz.neurocomet

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
import kotlinx.serialization.json.jsonPrimitive

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

    // ---- Public API ----

    /**
     * Fetch all conversations for the current user, with last message and participant profiles.
     */
    suspend fun fetchConversations(userId: String): List<Conversation> {
        val client = AppSupabaseClient.client ?: return emptyList()
        return try {
            // 1. Get all conversation IDs this user participates in
            val participations = client.postgrest["conversation_participants"]
                .select(Columns.list("conversation_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()

            val convIds = participations.mapNotNull { it["conversation_id"] }
            if (convIds.isEmpty()) return emptyList()

            // 2. Fetch conversations
            val conversations = client.postgrest["conversations"]
                .select {
                    filter { isIn("id", convIds) }
                }
                .decodeList<DbConversation>()

            // 3. Fetch all participants for these conversations
            val allParticipants = client.postgrest["conversation_participants"]
                .select {
                    filter { isIn("conversation_id", convIds) }
                }
                .decodeList<DbParticipant>()

            // 4. Fetch profiles for all participants
            val allUserIds = allParticipants.map { it.user_id }.distinct()
            val profiles = if (allUserIds.isNotEmpty()) {
                client.postgrest["profiles"]
                    .select {
                        filter { isIn("id", allUserIds) }
                    }
                    .decodeList<DbProfile>()
            } else emptyList()

            // 5. Fetch last message for each conversation
            val result = conversations.map { conv ->
                val participants = allParticipants
                    .filter { it.conversation_id == conv.id }
                    .map { it.user_id }

                val lastMessages = client.postgrest["dm_messages"]
                    .select {
                        filter { eq("conversation_id", conv.id) }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<DbMessage>()

                val unread = client.postgrest["dm_messages"]
                    .select(Columns.list("id")) {
                        filter {
                            eq("conversation_id", conv.id)
                            eq("is_read", false)
                            neq("sender_id", userId)
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .size

                val messages = lastMessages.map { it.toDirectMessage(participants, userId) }

                Conversation(
                    id = conv.id,
                    participants = participants,
                    messages = messages,
                    lastMessageTimestamp = conv.updated_at,
                    unreadCount = unread,
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
        val client = AppSupabaseClient.client ?: return emptyList()
        return try {
            // Get participants for recipientId mapping
            val participants = client.postgrest["conversation_participants"]
                .select(Columns.list("user_id")) {
                    filter { eq("conversation_id", conversationId) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["user_id"] }

            val dbMessages = client.postgrest["dm_messages"]
                .select {
                    filter { eq("conversation_id", conversationId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<DbMessage>()

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
        val client = AppSupabaseClient.client ?: return null
        return try {
            val row = mapOf(
                "conversation_id" to conversationId,
                "sender_id" to senderId,
                "content" to content,
                "type" to "text"
            )
            val result = client.postgrest["dm_messages"]
                .insert(row) {
                    select()
                }
                .decodeSingle<DbMessage>()

            result.toDirectMessage(emptyList(), senderId)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage failed", e)
            null
        }
    }

    /**
     * Mark all messages in a conversation as read for the current user.
     */
    suspend fun markConversationRead(conversationId: String, userId: String) {
        val client = AppSupabaseClient.client ?: return
        try {
            client.postgrest["dm_messages"]
                .update(mapOf("is_read" to true)) {
                    filter {
                        eq("conversation_id", conversationId)
                        neq("sender_id", userId)
                        eq("is_read", false)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "markConversationRead failed", e)
        }
    }

    /**
     * Create a new 1:1 conversation between two users. Returns the conversation ID.
     * If a conversation already exists between them, returns that one.
     */
    suspend fun getOrCreateConversation(userId: String, otherUserId: String): String? {
        val client = AppSupabaseClient.client ?: return null
        return try {
            // Check if conversation already exists between these two users
            val myConvs = client.postgrest["conversation_participants"]
                .select(Columns.list("conversation_id")) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<Map<String, String>>()
                .mapNotNull { it["conversation_id"] }

            if (myConvs.isNotEmpty()) {
                val otherConvs = client.postgrest["conversation_participants"]
                    .select(Columns.list("conversation_id")) {
                        filter {
                            eq("user_id", otherUserId)
                            isIn("conversation_id", myConvs)
                        }
                    }
                    .decodeList<Map<String, String>>()
                    .mapNotNull { it["conversation_id"] }

                if (otherConvs.isNotEmpty()) {
                    return otherConvs.first()
                }
            }

            // Create new conversation
            val conv = client.postgrest["conversations"]
                .insert(mapOf("is_group" to false)) {
                    select()
                }
                .decodeSingle<DbConversation>()

            // Add both participants
            client.postgrest["conversation_participants"]
                .insert(listOf(
                    mapOf("conversation_id" to conv.id, "user_id" to userId),
                    mapOf("conversation_id" to conv.id, "user_id" to otherUserId)
                ))

            conv.id
        } catch (e: Exception) {
            Log.e(TAG, "getOrCreateConversation failed", e)
            null
        }
    }

    /**
     * Look up a user profile by username (for starting new chats).
     */
    suspend fun findUserByUsername(username: String): DbProfile? {
        val client = AppSupabaseClient.client ?: return null
        return try {
            client.postgrest["profiles"]
                .select {
                    filter { eq("username", username) }
                    limit(1)
                }
                .decodeList<DbProfile>()
                .firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "findUserByUsername failed", e)
            null
        }
    }

    /**
     * Fetch a profile by user ID.
     */
    suspend fun getProfile(userId: String): DbProfile? {
        val client = AppSupabaseClient.client ?: return null
        return try {
            client.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                    limit(1)
                }
                .decodeList<DbProfile>()
                .firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "getProfile failed", e)
            null
        }
    }

    /**
     * Fetch all profiles for a list of user IDs.
     */
    suspend fun getProfiles(userIds: List<String>): Map<String, DbProfile> {
        val client = AppSupabaseClient.client ?: return emptyMap()
        if (userIds.isEmpty()) return emptyMap()
        return try {
            client.postgrest["profiles"]
                .select {
                    filter { isIn("id", userIds) }
                }
                .decodeList<DbProfile>()
                .associateBy { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "getProfiles failed", e)
            emptyMap()
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


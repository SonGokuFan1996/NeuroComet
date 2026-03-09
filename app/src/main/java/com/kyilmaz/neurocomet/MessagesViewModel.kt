package com.kyilmaz.neurocomet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * MessagesViewModel — manages real-time messaging via Supabase.
 *
 * When Supabase is configured + user is authenticated:
 *   - Fetches real conversations/messages from DB
 *   - Sends messages via Postgrest
 *   - Subscribes to Realtime for incoming messages
 *
 * When Supabase is NOT configured:
 *   - Falls back to mock data so the UI still works for demos
 */
class MessagesViewModel : ViewModel() {

    private companion object {
        const val TAG = "MessagesViewModel"
    }

    data class MessagesState(
        val conversations: List<Conversation> = emptyList(),
        val activeConversation: Conversation? = null,
        val isLoading: Boolean = false,
        val currentUserId: String = "me",
        val profiles: Map<String, MessagesRepository.DbProfile> = emptyMap(),
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(MessagesState())
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeJob: Job? = null

    /**
     * Get the current authenticated user's ID from Supabase Auth,
     * or fall back to "me" for mock mode.
     */
    private fun getCurrentUserId(): String {
        return try {
            AppSupabaseClient.client?.auth?.currentSessionOrNull()?.user?.id ?: "me"
        } catch (e: Exception) {
            "me"
        }
    }

    /**
     * Initialize: detect user, load conversations.
     */
    fun initialize() {
        val userId = getCurrentUserId()
        _state.update { it.copy(currentUserId = userId) }

        if (AppSupabaseClient.isAvailable() && userId != "me") {
            fetchConversationsFromSupabase(userId)
        } else {
            // Mock mode — conversations come from FeedViewModel's mock data
            Log.d(TAG, "Using mock mode (Supabase not configured or user not authenticated)")
        }
    }

    /**
     * Fetch real conversations from Supabase.
     */
    private fun fetchConversationsFromSupabase(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val conversations = MessagesRepository.fetchConversations(userId)

                // Fetch profiles for all participants
                val allParticipantIds = conversations.flatMap { it.participants }.distinct()
                val profiles = MessagesRepository.getProfiles(allParticipantIds)

                _state.update {
                    it.copy(
                        conversations = conversations,
                        profiles = profiles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch conversations", e)
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to load messages") }
            }
        }
    }

    /**
     * Refresh conversations list.
     */
    fun refreshConversations() {
        val userId = _state.value.currentUserId
        if (userId != "me") {
            fetchConversationsFromSupabase(userId)
        }
    }

    /**
     * Open a conversation: fetch full messages + subscribe to Realtime.
     */
    fun openConversation(conversationId: String) {
        val userId = _state.value.currentUserId

        viewModelScope.launch {
            if (userId != "me") {
                // Real mode: fetch messages from DB
                val messages = MessagesRepository.fetchMessages(conversationId, userId)
                MessagesRepository.markConversationRead(conversationId, userId)

                val existingConv = _state.value.conversations.find { it.id == conversationId }
                val fullConv = existingConv?.copy(messages = messages, unreadCount = 0)
                    ?: Conversation(
                        id = conversationId,
                        participants = emptyList(),
                        messages = messages,
                        lastMessageTimestamp = Instant.now().toString(),
                        unreadCount = 0
                    )

                _state.update { it.copy(activeConversation = fullConv) }

                // Subscribe to Realtime
                subscribeToConversation(conversationId, userId)
            } else {
                // Mock mode: just set the active conversation from existing list
                val conv = _state.value.conversations.find { it.id == conversationId }
                if (conv != null) {
                    val updatedConv = conv.copy(
                        messages = conv.messages.map { it.copy(isRead = true) },
                        unreadCount = 0
                    )
                    _state.update {
                        it.copy(
                            activeConversation = updatedConv,
                            conversations = it.conversations.map {
                                c -> if (c.id == conversationId) updatedConv else c
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Close the active conversation and unsubscribe from Realtime.
     */
    fun dismissConversation() {
        realtimeJob?.cancel()
        realtimeJob = null

        viewModelScope.launch {
            try {
                realtimeChannel?.unsubscribe()
            } catch (e: Exception) {
                Log.d(TAG, "Error unsubscribing from realtime", e)
            }
            realtimeChannel = null
        }

        _state.update { it.copy(activeConversation = null) }
    }

    /**
     * Send a direct message.
     */
    fun sendDirectMessage(recipientId: String, content: String) {
        val userId = _state.value.currentUserId
        val activeConv = _state.value.activeConversation ?: return

        // Optimistic: show message immediately
        val optimisticMessage = DirectMessage(
            id = "local-${System.currentTimeMillis()}",
            senderId = userId,
            recipientId = recipientId,
            content = content,
            timestamp = Instant.now().toString(),
            deliveryStatus = MessageDeliveryStatus.SENDING,
            moderationStatus = ModerationStatus.CLEAN
        )

        _state.update { state ->
            val updatedConv = state.activeConversation?.copy(
                messages = state.activeConversation.messages + optimisticMessage,
                lastMessageTimestamp = optimisticMessage.timestamp
            )
            // Also update the conversations list so the UI sees the new message immediately
            val updatedConversations = state.conversations.map { conv ->
                if (conv.id == activeConv.id) {
                    conv.copy(
                        messages = conv.messages + optimisticMessage,
                        lastMessageTimestamp = optimisticMessage.timestamp
                    )
                } else conv
            }
            state.copy(activeConversation = updatedConv, conversations = updatedConversations)
        }

        viewModelScope.launch {
            if (userId != "me") {
                // Real mode: send via Supabase
                val sent = MessagesRepository.sendMessage(activeConv.id, userId, content)
                if (sent != null) {
                    updateMessageInState(activeConv.id, optimisticMessage.id) {
                        sent.copy(deliveryStatus = MessageDeliveryStatus.SENT)
                    }
                } else {
                    updateMessageInState(activeConv.id, optimisticMessage.id) {
                        it.copy(deliveryStatus = MessageDeliveryStatus.FAILED)
                    }
                }
            } else {
                // Mock mode: just mark as sent
                updateMessageInState(activeConv.id, optimisticMessage.id) {
                    it.copy(deliveryStatus = MessageDeliveryStatus.SENT)
                }
            }

            // Refresh conversation list
            refreshConversations()
        }
    }

    /**
     * Helper: update a specific message in both activeConversation and conversations list.
     */
    private fun updateMessageInState(convId: String, messageId: String, transform: (DirectMessage) -> DirectMessage) {
        _state.update { state ->
            val mapMessages = { messages: List<DirectMessage> ->
                messages.map { msg -> if (msg.id == messageId) transform(msg) else msg }
            }
            val updatedActive = state.activeConversation?.let { ac ->
                if (ac.id == convId) ac.copy(messages = mapMessages(ac.messages)) else ac
            }
            val updatedConversations = state.conversations.map { conv ->
                if (conv.id == convId) conv.copy(messages = mapMessages(conv.messages)) else conv
            }
            state.copy(activeConversation = updatedActive, conversations = updatedConversations)
        }
    }

    /**
     * Start or open a conversation with a user.
     */
    fun startOrOpenConversation(otherUserId: String): String {
        val userId = _state.value.currentUserId

        // Check existing conversations first
        val existing = _state.value.conversations.find { conv ->
            conv.participants.contains(otherUserId) && conv.participants.contains(userId)
        }
        if (existing != null) {
            openConversation(existing.id)
            return existing.id
        }

        if (userId != "me") {
            // Real mode: create via Supabase
            val placeholderId = "pending-${System.currentTimeMillis()}"
            viewModelScope.launch {
                val convId = MessagesRepository.getOrCreateConversation(userId, otherUserId)
                if (convId != null) {
                    refreshConversations()
                    openConversation(convId)
                }
            }
            return placeholderId
        } else {
            // Mock mode
            val newId = "conv-${System.currentTimeMillis()}"
            val newConv = Conversation(
                id = newId,
                participants = listOf(userId, otherUserId),
                messages = emptyList(),
                lastMessageTimestamp = Instant.now().toString(),
                unreadCount = 0
            )
            _state.update { it.copy(
                conversations = it.conversations + newConv,
                activeConversation = newConv
            )}
            return newId
        }
    }

    /**
     * React to a message (local-only for now, persisted reactions in future phase).
     */
    fun reactToMessage(conversationId: String, messageId: String, emoji: String) {
        val userId = _state.value.currentUserId
        _state.update { state ->
            val updatedMessages: (List<DirectMessage>) -> List<DirectMessage> = { messages ->
                messages.map { msg ->
                    if (msg.id == messageId) {
                        val currentReactions = msg.reactions.toMutableList()
                        val existing = currentReactions.find { it.userId == userId && it.emoji == emoji }
                        if (existing != null) {
                            currentReactions.remove(existing)
                        } else {
                            currentReactions.add(
                                MessageReaction(emoji = emoji, userId = userId, timestamp = Instant.now().toString())
                            )
                        }
                        msg.copy(reactions = currentReactions)
                    } else msg
                }
            }

            val updatedConv = state.activeConversation?.let {
                if (it.id == conversationId) it.copy(messages = updatedMessages(it.messages)) else it
            }

            val updatedConversations = state.conversations.map { conv ->
                if (conv.id == conversationId) conv.copy(messages = updatedMessages(conv.messages)) else conv
            }

            state.copy(
                activeConversation = updatedConv,
                conversations = updatedConversations
            )
        }
    }

    fun reportMessage(messageId: String) {
        _state.update { state ->
            val updatedConv = state.activeConversation?.copy(
                messages = state.activeConversation.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(moderationStatus = ModerationStatus.FLAGGED)
                    else msg
                }
            )
            state.copy(activeConversation = updatedConv)
        }
    }

    fun retryDirectMessage(convId: String, msgId: String) {
        val failedMsg = _state.value.activeConversation?.messages?.find { it.id == msgId } ?: return
        // Remove the failed message
        _state.update { state ->
            val updatedConv = state.activeConversation?.copy(
                messages = state.activeConversation.messages.filter { it.id != msgId }
            )
            state.copy(activeConversation = updatedConv)
        }
        // Resend
        sendDirectMessage(failedMsg.recipientId, failedMsg.content)
    }

    fun isUserBlocked(userId: String): Boolean = false
    fun isUserMuted(userId: String): Boolean = false

    // ---- Realtime subscription ----

    private fun subscribeToConversation(conversationId: String, userId: String) {
        // Cancel existing subscription
        realtimeJob?.cancel()

        val (flow, channel) = MessagesRepository.subscribeToMessages(conversationId, userId)
        realtimeChannel = channel

        if (channel != null) {
            realtimeJob = viewModelScope.launch {
                try {
                    channel.subscribe()
                    flow.collect { newMessage ->
                        _state.update { state ->
                            val updatedConv = state.activeConversation?.let { conv ->
                                // Avoid duplicates
                                if (conv.messages.any { it.id == newMessage.id }) conv
                                else conv.copy(messages = conv.messages + newMessage)
                            }
                            state.copy(activeConversation = updatedConv)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Realtime subscription error", e)
                }
            }
        }
    }

    /**
     * Set conversations from external source (for mock mode compatibility with FeedViewModel).
     */
    fun setMockConversations(conversations: List<Conversation>) {
        if (_state.value.currentUserId == "me") {
            _state.update { it.copy(conversations = conversations) }
        }
    }

    /**
     * Receive a mock reply (for simulated incoming messages in mock mode).
     */
    fun receiveMockReply(conversationId: String, senderId: String, content: String) {
        if (_state.value.currentUserId != "me") return

        val incomingMessage = DirectMessage(
            id = "mock-${System.currentTimeMillis()}",
            senderId = senderId,
            recipientId = _state.value.currentUserId,
            content = content,
            timestamp = Instant.now().toString(),
            deliveryStatus = MessageDeliveryStatus.SENT,
            moderationStatus = ModerationStatus.CLEAN
        )

        _state.update { state ->
            val isActiveConversation = state.activeConversation?.id == conversationId
            val updatedConversations = state.conversations
                .map { conv ->
                    if (conv.id == conversationId) {
                        conv.copy(
                            messages = conv.messages + incomingMessage,
                            lastMessageTimestamp = incomingMessage.timestamp,
                            unreadCount = if (isActiveConversation) 0 else conv.unreadCount + 1
                        )
                    } else conv
                }
                .sortedByDescending { Instant.parse(it.lastMessageTimestamp) }

            val updatedActiveConversation = state.activeConversation?.let { active ->
                if (active.id == conversationId) {
                    active.copy(
                        messages = active.messages + incomingMessage,
                        lastMessageTimestamp = incomingMessage.timestamp,
                        unreadCount = 0
                    )
                } else active
            }

            state.copy(
                conversations = updatedConversations,
                activeConversation = updatedActiveConversation
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        viewModelScope.launch {
            try {
                realtimeChannel?.unsubscribe()
            } catch (e: Exception) {
                Log.w(TAG, "Error unsubscribing realtime channel on clear", e)
            }
        }
    }
}

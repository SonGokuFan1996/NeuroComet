package com.kyilmaz.neurocomet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
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
        const val DM_MIN_SEND_INTERVAL_MS = 1_500L
        const val DM_MAX_MESSAGE_LENGTH = 1_000
    }

    data class MessagesState(
        val conversations: List<Conversation> = emptyList(),
        val activeConversation: Conversation? = null,
        val isLoading: Boolean = false,
        val isSearchingNewChatUsers: Boolean = false,
        val isStartingConversation: Boolean = false,
        val currentUserId: String = "me",
        val profiles: Map<String, MessagesRepository.DbProfile> = emptyMap(),
        val discoverableProfiles: List<MessagesRepository.DbProfile> = emptyList(),
        val newChatProfiles: List<MessagesRepository.DbProfile> = emptyList(),
        val blockedUserIds: Set<String> = emptySet(),
        val mutedUserIds: Set<String> = emptySet(),
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(MessagesState())
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeJob: Job? = null
    private var initializedUserId: String? = null
    private val lastDmSentAtMsByRecipient = mutableMapOf<String, Long>()


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
    fun initialize(force: Boolean = false) {
        val userId = getCurrentUserId()
        val userChanged = initializedUserId != null && initializedUserId != userId

        if (userChanged) {
            dismissConversation()
        }

        _state.update {
            it.copy(
                currentUserId = userId,
                // Sync initial block/mute state from DmPrivacySettings singleton
                blockedUserIds = DmPrivacySettings.getBlockedIds(),
                mutedUserIds = DmPrivacySettings.getMutedIds(),
                conversations = if (userChanged) emptyList() else it.conversations,
                profiles = if (userChanged) emptyMap() else it.profiles,
                discoverableProfiles = if (userChanged || userId == "me") emptyList() else it.discoverableProfiles,
                newChatProfiles = if (userChanged || userId == "me") emptyList() else it.newChatProfiles,
                isSearchingNewChatUsers = false,
                isStartingConversation = false
            )
        }

        if (!force && initializedUserId == userId) {
            return
        }

        initializedUserId = userId

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
                val discoverableProfiles = MessagesRepository.fetchDiscoverableProfiles(userId)
                val baselineConversations = _state.value.conversations
                val effectiveConversations = if (conversations.isEmpty() && baselineConversations.isNotEmpty()) {
                    baselineConversations
                } else {
                    conversations
                }

                // Fetch profiles for all participants
                val allParticipantIds = effectiveConversations.flatMap { it.participants }.distinct()
                val profiles = MessagesRepository.getProfiles(allParticipantIds)

                _state.update {
                    it.copy(
                        conversations = effectiveConversations,
                        profiles = profiles,
                        discoverableProfiles = discoverableProfiles,
                        newChatProfiles = discoverableProfiles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch conversations", e)
                if (initializedUserId == userId) {
                    initializedUserId = null
                }
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to load messages") }
            }
        }
    }

    /**
     * Refresh conversations list.
     */
    fun refreshConversations() {
        initialize()
        val userId = _state.value.currentUserId
        if (userId != "me") {
            fetchConversationsFromSupabase(userId)
        }
    }

    fun searchUsersForNewChat(query: String) {
        initialize()
        val userId = _state.value.currentUserId
        if (userId == "me") {
            return
        }

        val normalized = query.trim()
        if (normalized.isBlank()) {
            _state.update {
                it.copy(
                    newChatProfiles = it.discoverableProfiles,
                    isSearchingNewChatUsers = false
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSearchingNewChatUsers = true) }
            val results = MessagesRepository.searchProfiles(normalized, excludeUserId = userId)
            _state.update { state ->
                if (state.currentUserId != userId) {
                    state
                } else {
                    state.copy(
                        newChatProfiles = results,
                        isSearchingNewChatUsers = false
                    )
                }
            }
        }
    }

    fun resetNewChatSearch() {
        _state.update {
            it.copy(
                newChatProfiles = if (it.currentUserId == "me") emptyList() else it.discoverableProfiles,
                isSearchingNewChatUsers = false
            )
        }
    }

    /**
     * Open a conversation: fetch full messages + subscribe to Realtime.
     */
    fun openConversation(conversationId: String) {
        initialize()
        val userId = _state.value.currentUserId

        viewModelScope.launch {
            try {
                val existingConv = _state.value.conversations.find { it.id == conversationId }
                if (userId != "me") {
                    if (existingConv?.isLocalOnlyFor(userId) == true) {
                        realtimeJob?.cancel()
                        realtimeJob = null
                        try {
                            realtimeChannel?.unsubscribe()
                        } catch (e: Exception) {
                            Log.d(TAG, "Error unsubscribing from realtime", e)
                        }
                        realtimeChannel = null

                        val updatedConv = existingConv.copy(
                            messages = existingConv.messages.map { it.copy(isRead = true) },
                            unreadCount = 0,
                            lastMessageTimestamp = existingConv.messages.lastOrNull()?.timestamp
                                ?: existingConv.lastMessageTimestamp
                        )
                        _state.update { state ->
                            state.copy(
                                activeConversation = updatedConv,
                                conversations = state.conversations.map { conversation ->
                                    if (conversation.id == conversationId) updatedConv else conversation
                                }
                            )
                        }
                        return@launch
                    }

                    // Real mode: fetch messages from DB
                    val messages = MessagesRepository.fetchMessages(conversationId, userId)
                    MessagesRepository.markConversationRead(conversationId, userId)

                    val resolvedParticipants = existingConv?.participants
                        ?.filter { it.isNotBlank() }
                        ?.distinct()
                        ?.takeIf { it.isNotEmpty() }
                        ?: messages
                            .flatMap { message -> listOf(message.senderId, message.recipientId) }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .ifEmpty { listOf(userId) }
                    val fullConv = existingConv?.copy(
                        participants = resolvedParticipants,
                        messages = messages,
                        unreadCount = 0,
                        lastMessageTimestamp = messages.lastOrNull()?.timestamp ?: existingConv.lastMessageTimestamp
                    )
                        ?: Conversation(
                            id = conversationId,
                            participants = resolvedParticipants,
                            messages = messages,
                            lastMessageTimestamp = messages.lastOrNull()?.timestamp ?: Instant.now().toString(),
                            unreadCount = 0
                        )

                    _state.update { state ->
                        state.copy(
                            activeConversation = fullConv,
                            conversations = state.conversations.map { conversation ->
                                if (conversation.id == conversationId) fullConv else conversation
                            }
                        )
                    }

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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open conversation $conversationId", e)
                _state.update {
                    it.copy(errorMessage = "We couldn't open that conversation right now. Please try again.")
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
        val trimmedContent = content.trim()

        if (trimmedContent.isBlank()) {
            _state.update { it.copy(errorMessage = "Message cannot be empty") }
            return
        }
        if (trimmedContent.length > DM_MAX_MESSAGE_LENGTH) {
            _state.update { it.copy(errorMessage = "Message is too long") }
            return
        }
        if (_state.value.blockedUserIds.contains(recipientId)) {
            _state.update { it.copy(errorMessage = "You blocked this user. Unblock them before messaging.") }
            return
        }

        val now = System.currentTimeMillis()
        val previousSentAt = lastDmSentAtMsByRecipient[recipientId] ?: 0L
        if (now - previousSentAt < DM_MIN_SEND_INTERVAL_MS) {
            _state.update { it.copy(errorMessage = "You’re sending messages too quickly. Please slow down.") }
            return
        }

        viewModelScope.launch {
            val moderationResult = ModerationService.analyzeText(trimmedContent)
            if (moderationResult == ModerationResult.BLOCKED_ABUSE) {
                _state.update { it.copy(errorMessage = "Message blocked for safety. Please remove risky, coercive, scam, or abusive language.") }
                return@launch
            }

            val moderationStatus = if (moderationResult == ModerationResult.FLAGGED_PROFANITY) {
                ModerationStatus.FLAGGED
            } else {
                ModerationStatus.CLEAN
            }

            // Optimistic: show message immediately after local safety checks pass
            val optimisticMessage = DirectMessage(
                id = "local-${System.currentTimeMillis()}",
                senderId = userId,
                recipientId = recipientId,
                content = trimmedContent,
                timestamp = Instant.now().toString(),
                deliveryStatus = MessageDeliveryStatus.SENDING,
                moderationStatus = moderationStatus
            )

            _state.update { state ->
                val updatedConv = state.activeConversation?.copy(
                    messages = state.activeConversation.messages + optimisticMessage,
                    lastMessageTimestamp = optimisticMessage.timestamp,
                    unreadCount = state.activeConversation.unreadCount
                )
                val updatedConversations = state.conversations.map { conv ->
                    if (conv.id == activeConv.id) {
                        conv.copy(
                            messages = conv.messages + optimisticMessage,
                            lastMessageTimestamp = optimisticMessage.timestamp
                        )
                    } else conv
                }
                state.copy(
                    activeConversation = updatedConv,
                    conversations = updatedConversations,
                    errorMessage = if (moderationStatus == ModerationStatus.FLAGGED) {
                        "Message sent, but it was flagged for review-sensitive language."
                    } else null
                )
            }
            lastDmSentAtMsByRecipient[recipientId] = now

            if (userId != "me") {
                // Real mode: send via Supabase
                val sent = MessagesRepository.sendMessage(activeConv.id, userId, trimmedContent)
                if (sent != null) {
                    updateMessageInState(activeConv.id, optimisticMessage.id) {
                        sent.copy(
                            deliveryStatus = MessageDeliveryStatus.SENT,
                            moderationStatus = moderationStatus
                        )
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
    fun startOrOpenConversation(
        otherUserId: String,
        onConversationReady: (String) -> Unit = {}
    ): String {
        initialize()
        val userId = _state.value.currentUserId

        if (otherUserId.isBlank() || otherUserId == userId) {
            _state.update { it.copy(errorMessage = "Choose another user to start a conversation.") }
            return ""
        }

        // Check existing conversations first
        val existing = _state.value.conversations.find { conv ->
            conv.participants.contains(otherUserId) && conv.participants.contains(userId)
        }
        if (existing != null) {
            openConversation(existing.id)
            onConversationReady(existing.id)
            return existing.id
        }

        if (userId != "me") {
            // Real mode: create via Supabase
            val placeholderId = "pending-${System.currentTimeMillis()}"
            viewModelScope.launch {
                _state.update { it.copy(isStartingConversation = true, errorMessage = null) }
                val targetProfile = MessagesRepository.getProfile(otherUserId)
                if (targetProfile == null) {
                    _state.update {
                        it.copy(
                            isStartingConversation = false,
                            errorMessage = "We couldn't find that user. Ask them to finish sign-in and profile setup, then try again."
                        )
                    }
                    return@launch
                }

                val convId = MessagesRepository.getOrCreateConversation(userId, otherUserId)
                if (convId != null) {
                    _state.update { state ->
                        state.copy(
                            profiles = state.profiles + (targetProfile.id to targetProfile),
                            discoverableProfiles = (state.discoverableProfiles + targetProfile).distinctBy { it.id },
                            newChatProfiles = (state.newChatProfiles + targetProfile).distinctBy { it.id },
                            isStartingConversation = false
                        )
                    }
                    refreshConversations()
                    openConversation(convId)
                    onConversationReady(convId)
                } else {
                    _state.update {
                        it.copy(
                            isStartingConversation = false,
                            errorMessage = "Couldn't start that conversation right now. Please try again."
                        )
                    }
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
            onConversationReady(newId)
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
        val convId = _state.value.activeConversation?.id ?: return
        updateMessageInState(convId, messageId) {
            it.copy(moderationStatus = ModerationStatus.FLAGGED)
        }
    }

    fun retryDirectMessage(convId: String, msgId: String) {
        val failedMsg = _state.value.activeConversation?.messages?.find { it.id == msgId } ?: return
        // Remove the failed message from both activeConversation and conversations list
        _state.update { state ->
            val updatedConv = state.activeConversation?.copy(
                messages = state.activeConversation.messages.filter { it.id != msgId }
            )
            val updatedConversations = state.conversations.map { conv ->
                if (conv.id == convId) conv.copy(messages = conv.messages.filter { it.id != msgId })
                else conv
            }
            state.copy(activeConversation = updatedConv, conversations = updatedConversations)
        }
        // Resend
        sendDirectMessage(failedMsg.recipientId, failedMsg.content)
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Send a reply to a story. Opens or creates a conversation with the story author
     * and sends the reply prefixed with a story reference.
     * Returns the conversation ID so the caller can navigate to it.
     */
    fun sendStoryReply(storyAuthorId: String, storyAuthorName: String, replyText: String): String {
        val convId = startOrOpenConversation(storyAuthorId)
        // Give the conversation creation a moment to settle, then send the message
        viewModelScope.launch {
            // Small delay to let startOrOpenConversation set up activeConversation
            kotlinx.coroutines.delay(100)
            val messageContent = "Replied to ${storyAuthorName}'s story: $replyText"
            sendDirectMessage(storyAuthorId, messageContent)
        }
        return convId
    }

    fun isUserBlocked(userId: String): Boolean = _state.value.blockedUserIds.contains(userId)
    fun isUserMuted(userId: String): Boolean = _state.value.mutedUserIds.contains(userId)

    fun blockUser(userId: String) {
        _state.update { it.copy(blockedUserIds = it.blockedUserIds + userId) }
        // Sync to DmPrivacySettings singleton so other components see the same state
        DmPrivacySettings.blockNoContext(userId)
        // In real app, persist this to Supabase 'blocks' table
        viewModelScope.launch {
            try {
                val client = AppSupabaseClient.client
                val currentId = _state.value.currentUserId
                if (client != null && currentId != "me") {
                    client.from("blocked_users").insert(mapOf("blocker_id" to currentId, "blocked_id" to userId))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist block", e)
            }
        }
    }

    fun unblockUser(userId: String) {
        _state.update { it.copy(blockedUserIds = it.blockedUserIds - userId) }
        DmPrivacySettings.unblockNoContext(userId)
        viewModelScope.launch {
            try {
                val client = AppSupabaseClient.client
                val currentId = _state.value.currentUserId
                if (client != null && currentId != "me") {
                    client.from("blocked_users").delete {
                        filter {
                            eq("blocker_id", currentId)
                            eq("blocked_id", userId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist unblock", e)
            }
        }
    }

    fun muteUser(userId: String) {
        _state.update { it.copy(mutedUserIds = it.mutedUserIds + userId) }
        DmPrivacySettings.muteNoContext(userId)
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val client = AppSupabaseClient.client
                val currentId = _state.value.currentUserId
                if (client != null && currentId != "me") {
                    client.from("muted_users").insert(mapOf("muter_id" to currentId, "muted_id" to userId))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist mute", e)
            }
        }
    }

    fun unmuteUser(userId: String) {
        _state.update { it.copy(mutedUserIds = it.mutedUserIds - userId) }
        DmPrivacySettings.unmuteNoContext(userId)
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val client = AppSupabaseClient.client
                val currentId = _state.value.currentUserId
                if (client != null && currentId != "me") {
                    client.from("muted_users").delete {
                        filter {
                            eq("muter_id", currentId)
                            eq("muted_id", userId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist unmute", e)
            }
        }
    }

    fun reportUser(userId: String, reason: String = "Inappropriate behavior") {
        viewModelScope.launch {
            try {
                val client = AppSupabaseClient.client
                val reporterId = _state.value.currentUserId
                if (client != null && reporterId != "me") {
                    client.from("reports").insert(mapOf(
                        "reporter_id" to reporterId,
                        "content_type" to "user",
                        "content_id" to userId,
                        "reason" to reason
                    ))
                    _state.update { it.copy(errorMessage = "User reported. Thank you.") }
                } else {
                    // Mock mode report
                    _state.update { it.copy(errorMessage = "User reported (Mock).") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit user report", e)
                _state.update { it.copy(errorMessage = "Failed to submit report.") }
            }
        }
    }

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
                            val isDuplicate = state.activeConversation?.messages?.any { it.id == newMessage.id } == true

                            val updatedConv = if (!isDuplicate) {
                                state.activeConversation?.let { conv ->
                                    conv.copy(messages = conv.messages + newMessage)
                                }
                            } else {
                                state.activeConversation
                            }

                            val updatedConversations = if (!isDuplicate) {
                                state.conversations.map { conv ->
                                    if (conv.id == conversationId) {
                                        conv.copy(
                                            messages = conv.messages + newMessage,
                                            lastMessageTimestamp = newMessage.timestamp
                                        )
                                    } else conv
                                }
                            } else {
                                state.conversations
                            }

                            state.copy(activeConversation = updatedConv, conversations = updatedConversations)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Realtime subscription error", e)
                }
            }
        }
    }

    private fun Conversation.isLocalOnlyFor(currentUserId: String): Boolean {
        return messages.isNotEmpty() && currentUserId !in participants
    }

    /**
     * Set conversations from external source (for mock mode compatibility with FeedViewModel).
     * Only applies the external list as a baseline — locally modified conversations
     * (messages sent/retried/reported) are preserved via merge rather than overwrite.
     */
    fun setMockConversations(conversations: List<Conversation>) {
        _state.update { current ->
            val canApplyMockBaseline = current.currentUserId == "me" || current.conversations.isEmpty()
            if (!canApplyMockBaseline) {
                return@update current
            }

            val currentById = current.conversations.associateBy { it.id }
            val merged = conversations.map { incoming ->
                val local = currentById[incoming.id]
                if (local != null && local.messages.size >= incoming.messages.size) {
                    local
                } else {
                    incoming
                }
            }
            val incomingIds = conversations.map { it.id }.toSet()
            val localOnly = current.conversations.filter { it.id !in incomingIds }
            current.copy(conversations = (merged + localOnly)
                .sortedByDescending { runCatching { Instant.parse(it.lastMessageTimestamp) }.getOrNull() ?: Instant.EPOCH })
        }
    }

    fun injectDevConversation(conversation: Conversation, openAfterInject: Boolean = false) {
        val normalizedConversation = if (openAfterInject) {
            conversation.copy(
                unreadCount = 0,
                messages = conversation.messages.map { it.copy(isRead = true) }
            )
        } else {
            conversation
        }

        _state.update { current ->
            val merged = (listOf(normalizedConversation) + current.conversations.filter { it.id != normalizedConversation.id })
                .sortedByDescending { runCatching { Instant.parse(it.lastMessageTimestamp) }.getOrNull() ?: Instant.EPOCH }

            current.copy(
                conversations = merged,
                activeConversation = if (openAfterInject) normalizedConversation else current.activeConversation
            )
        }
    }

    fun clearDevInjectedConversations() {
        _state.update { current ->
            current.copy(
                conversations = current.conversations.filterNot { it.id.startsWith("dev-") },
                activeConversation = current.activeConversation?.takeUnless { it.id.startsWith("dev-") }
            )
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
        // Unsubscribe synchronously — viewModelScope is already cancelled at this point
        // so launching a coroutine on it would be a no-op.
        try {
            val channel = realtimeChannel
            realtimeChannel = null
            if (channel != null) {
                kotlinx.coroutines.runBlocking {
                    channel.unsubscribe()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unsubscribing realtime channel on clear", e)
        }
    }
}

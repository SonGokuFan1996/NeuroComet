@file:Suppress(
    "unused",
    "RedundantSuppression",
    "UNUSED_PARAMETER",
    "SimplifyBooleanWithConstants",
    "KotlinConstantConditions",
    "MemberVisibilityCanBePrivate"
)

package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Context
import android.content.Intent
import com.kyilmaz.neurocomet.ads.GoogleAdsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * ViewModel for managing the main feed, stories, conversations, and notifications.
 *
 * This ViewModel handles:
 * - Feed posts (CRUD operations, likes, comments)
 * - Stories (viewing, creating, deleting)
 * - Direct message conversations
 * - Notifications
 * - Content moderation
 * - Premium status
 *
 * Currently uses mock data for development and testing purposes.
 */

// User IDs for mock data
private const val CURRENT_USER_ID_MOCK = "me"
private const val DINO_USER_ID = "DinoLover99"
private const val THERAPY_USER_ID = "Therapy_Bot"

/**
 * All post IDs used by mock/localized data.
 * These MUST NOT be persisted to Supabase, because they collide with
 * auto-incremented IDs in the real `posts` table and cause data corruption.
 */
private val MOCK_POST_IDS: Set<Long> = setOf(
    1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L,
    101L, 102L, 103L, 104L, 105L, 106L
)

/** Returns true if [postId] belongs to mock/local-only data. */
private fun isMockPostId(postId: Long): Boolean = postId in MOCK_POST_IDS

// --- SCREENSHOT-WORTHY MOCK DATA ---
// These are additional posts shown alongside localized mock data
// IDs start at 100 to avoid conflicts with localized posts (IDs 1-17)
val MOCK_FEED_POSTS = listOf(
    Post(
        id = 101L,
        createdAt = Instant.now().toString(),
        content = "✨ Finally got my sensory-friendly workspace set up! Noise-canceling headphones, soft lighting, and my weighted lap pad. Game changer for focus! 🎧💡",
        userId = "FocusQueen",
        likes = 2847,
        comments = 156,
        shares = 89,
        isLikedByMe = true,
        userAvatar = "https://i.pravatar.cc/150?u=focusqueen_ws",
        imageUrl = "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800",
        locationTag = "Portland, OR"
    ),
    Post(
        id = 102L,
        createdAt = Instant.now().toString(),
        content = "Reminder: Your brain works differently, not wrong. 🧠💜 Embrace your unique way of processing the world!",
        userId = "NeuroDivergentPride",
        likes = 5621,
        comments = 342,
        shares = 1205,
        isLikedByMe = false,
        userAvatar = "https://i.pravatar.cc/150?u=ndpride_inspire",
        imageUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800",
        locationTag = "San Francisco, CA"
    ),
    Post(
        id = 103L,
        createdAt = Instant.now().toString(),
        content = "Just discovered body doubling on NeuroComet and WOW. Studied for 3 hours straight with my virtual study buddy! 📚🎉 Anyone else find this helpful?",
        userId = "ADHDStudent",
        likes = 1893,
        comments = 287,
        shares = 156,
        isLikedByMe = false,
        userAvatar = "https://i.pravatar.cc/150?u=adhdstudent_study",
        imageUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=800",
        locationTag = "Austin, TX"
    ),
    Post(
        id = 104L,
        createdAt = Instant.now().toString(),
        content = "My stim toy collection is growing! 🌈 These fidget tools have helped me so much in meetings. No shame in stimming! ✨",
        userId = "StimHappy",
        likes = 3412,
        comments = 198,
        shares = 445,
        isLikedByMe = true,
        userAvatar = "https://i.pravatar.cc/150?u=stimhappy_fidget",
        imageUrl = "https://images.unsplash.com/photo-1612538498456-e861df91d4d0?w=800",
        locationTag = "London, UK"
    ),
    Post(
        id = 105L,
        createdAt = Instant.now().toString(),
        content = "Pro tip: Create a 'launch pad' by your door with everything you need for the day. Keys, wallet, meds, snacks - all in one spot! 🚀 ADHD life hack #247",
        userId = "LifeHacker_ND",
        likes = 4156,
        comments = 523,
        shares = 892,
        isLikedByMe = false,
        userAvatar = "https://i.pravatar.cc/150?u=lifehacker_tips",
        imageUrl = "https://images.unsplash.com/photo-1497032628192-86f99bcd76bc?w=800",
        locationTag = "Berlin, DE"
    ),
    Post(
        id = 106L,
        createdAt = Instant.now().toString(),
        content = "Today's win: I remembered to eat lunch! 🎊 Setting phone alarms for basic needs is self-care. Don't let anyone tell you otherwise. 💪",
        userId = "SelfCareSunday",
        likes = 2234,
        comments = 178,
        shares = 234,
        isLikedByMe = true,
        userAvatar = "https://i.pravatar.cc/150?u=selfcare_sunday",
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800",
        locationTag = "Seattle, WA"
    )
)

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPremium: Boolean = false,
    val showStories: Boolean = true,
    val isVideoAutoplayEnabled: Boolean = false,
    val isMockInterfaceEnabled: Boolean = false,
    val isFallbackUiEnabled: Boolean = false,
    val isFakePremiumEnabled: Boolean = false,
    val activePostId: Long? = null,
    val activePostComments: List<Comment> = emptyList(),
    val commentDraftsByPostId: Map<Long, String> = emptyMap(),
    val commentsByPostId: Map<Long, List<Comment>> = emptyMap(),
    val isCommentSheetVisible: Boolean = false,
    val activeStory: Story? = null,
    val conversations: List<Conversation> = emptyList(),
    val activeConversation: Conversation? = null,
    val isDinoBanned: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val submittedReports: List<Map<String, String>> = emptyList(),
    // Content preferences (synced from Settings)
    val hideLikeCounts: Boolean = false,
    val hideViewCounts: Boolean = false,
    val dataSaverMode: Boolean = false,
    // Supabase-synced social state
    val bookmarkedPostIds: Set<Long> = emptySet(),
    val followingUserIds: Set<String> = emptySet(),
    val blockedUserIds: Set<String> = emptySet(),
    val mutedUserIds: Set<String> = emptySet()
)

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _localizedNotifications = MockDataProvider.getLocalizedNotifications(application)
    private val _localizedPosts = MockDataProvider.getLocalizedExplorePosts(application)
    private val _localizedFeedPosts = MockDataProvider.getLocalizedFeedPosts(application)
    private val _localizedConversations = MockDataProvider.getLocalizedConversations(application)

    private var realPremiumStatus = false

    // ── Feature flags (from DevOptions) ──────────────────────
    private val _enableNewFeedLayout = MutableStateFlow(false)
    val enableNewFeedLayout: StateFlow<Boolean> = _enableNewFeedLayout.asStateFlow()

    private val _enableAdvancedSearch = MutableStateFlow(false)
    val enableAdvancedSearch: StateFlow<Boolean> = _enableAdvancedSearch.asStateFlow()

    private val _enableAiSuggestions = MutableStateFlow(false)
    val enableAiSuggestions: StateFlow<Boolean> = _enableAiSuggestions.asStateFlow()

    /**
     * Update feature flags from DevOptions. Called by MainActivity whenever
     * the [DevOptions] change so that toggling flags in the dev menu
     * actually takes effect in the feed and other consuming screens.
     */
    fun setFeatureFlags(
        enableNewFeedLayout: Boolean = false,
        enableAdvancedSearch: Boolean = false,
        enableAiSuggestions: Boolean = false
    ) {
        _enableNewFeedLayout.value = enableNewFeedLayout
        _enableAdvancedSearch.value = enableAdvancedSearch
        _enableAiSuggestions.value = enableAiSuggestions
    }

    /**
     * Apply content preferences from Settings → Content Preferences screen.
     * Called by MainActivity when settings change.
     */
    fun applyContentPreferences(
        hideLikeCounts: Boolean = false,
        hideViewCounts: Boolean = false,
        dataSaverMode: Boolean = false
    ) {
        _uiState.update { it.copy(
            hideLikeCounts = hideLikeCounts,
            hideViewCounts = hideViewCounts,
            dataSaverMode = dataSaverMode
        ) }
    }


    private val _userStories = MutableStateFlow<List<Story>>(emptyList())
    private val _userConversations = MutableStateFlow<List<Conversation>>(_localizedConversations)
    private val _userCreatedPosts = MutableStateFlow<List<Post>>(emptyList())
    private val _deletedPostIds = MutableStateFlow<Set<Long>>(emptySet())

    /** Observable status from dev story operations (for StoriesTestingSection UI). */
    private val _devStoryStatus = MutableStateFlow<String?>(null)
    val devStoryStatus: StateFlow<String?> = _devStoryStatus

    /**
     * One-shot request to open the post composer, fired by launcher shortcuts
     * (AppShortcutsManager.ACTION_NEW_POST) and Assistant intents. FeedScreen
     * observes this and shows its composer, then calls [consumeOpenComposer].
     */
    private val _openComposerRequest = MutableStateFlow(false)
    val openComposerRequest: StateFlow<Boolean> = _openComposerRequest
    fun requestOpenComposer() { _openComposerRequest.value = true }
    fun consumeOpenComposer() { _openComposerRequest.value = false }

    // Mock strike counter
    private val _userStrikeCount = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Notifications - use localized data
    private val _notifications = MutableStateFlow<List<NotificationItem>>(_localizedNotifications)

    init {
        fetchPosts()
        fetchStories()
        fetchConversations()
        fetchNotifications()
        loadSocialState()
    }

    /**
     * Load bookmarks, follows, blocks, and mutes from Supabase on startup.
     * These are used by the UI to show correct initial states for bookmark icons,
     * follow badges, etc. Falls back gracefully when Supabase is not available.
     */
    private fun loadSocialState() {
        val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return
        if (userId == CURRENT_USER_ID_MOCK) return

        viewModelScope.launch {
            try {
                val bookmarks = BookmarksRepository.getBookmarkedPostIds(userId)
                val following = FollowsRepository.getFollowingIds(userId).toSet()
                val blocked = try {
                    safeSelect("blocked_users", "blocked_id", "blocker_id=eq.$userId")
                        .mapNotNull { it.jsonObject["blocked_id"]?.jsonPrimitive?.content }
                        .toSet()
                } catch (_: Exception) { emptySet() }
                val muted = try {
                    safeSelect("muted_users", "muted_id", "muter_id=eq.$userId")
                        .mapNotNull { it.jsonObject["muted_id"]?.jsonPrimitive?.content }
                        .toSet()
                } catch (_: Exception) { emptySet() }

                _uiState.update {
                    it.copy(
                        bookmarkedPostIds = bookmarks,
                        followingUserIds = following,
                        blockedUserIds = blocked,
                        mutedUserIds = muted
                    )
                }
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "Failed to load social state from Supabase", e)
            }
        }
    }

    private fun devOptions(): DevOptions {
        val app = ApplicationProvider.app ?: return DevOptions()
        return DevOptionsSettings.get(app)
    }

    // Mock content moderation
    private fun performContentModeration(content: String): ModerationStatus {
        return ModerationHeuristics.analyze(content).status
    }

    private fun effectiveModerationStatus(content: String): ModerationStatus {
        return when (devOptions().moderationOverride) {
            DevModerationOverride.OFF -> performContentModeration(content)
            DevModerationOverride.CLEAN -> ModerationStatus.CLEAN
            DevModerationOverride.FLAGGED -> ModerationStatus.FLAGGED
            DevModerationOverride.BLOCKED -> ModerationStatus.BLOCKED
        }
    }

    private fun handleAbusiveContent(senderId: String, status: ModerationStatus) {
        if (status == ModerationStatus.BLOCKED) {
            _userStrikeCount.update { currentCounts ->
                val strikes = (currentCounts[senderId] ?: 0) + 1
                val newCounts = currentCounts + (senderId to strikes)
                if (strikes >= 3) banUser(senderId)
                newCounts
            }
        }
    }

    fun banUser(userId: String) {
        if (userId == DINO_USER_ID) {
            _uiState.update { it.copy(isDinoBanned = true, errorMessage = "User DinoLover99 has been banned.") }
        } else if (userId == CURRENT_USER_ID_MOCK) {
            _uiState.update { it.copy(errorMessage = "You have been banned (Mock)! DM features disabled.") }
        }
    }

    // --- Conversations / DM list ---

    fun fetchConversations() {
        viewModelScope.launch {
            // No artificial delay

            val combinedConversations = _localizedConversations.map { staticConv ->
                _userConversations.value.find { it.id == staticConv.id } ?: staticConv
            }.plus(_userConversations.value.filter { conv -> _localizedConversations.none { it.id == conv.id } })

            _uiState.update {
                it.copy(conversations = combinedConversations.sortedByDescending { conv -> Instant.parse(conv.lastMessageTimestamp) })
            }
        }
    }

    fun openConversation(conversationId: String) {
        _uiState.update { state ->
            val conversation = state.conversations.find { it.id == conversationId } ?: return@update state
            val updatedMessages = conversation.messages.map { it.copy(isRead = true) }
            val updatedConversation = conversation.copy(messages = updatedMessages, unreadCount = 0)

            _userConversations.update { currentList ->
                currentList.map { if (it.id == conversationId) updatedConversation else it }
            }

            state.copy(
                activeConversation = updatedConversation,
                conversations = state.conversations.map { if (it.id == conversationId) updatedConversation else it }
            )
        }
    }

    fun dismissConversation() {
        _uiState.update { it.copy(activeConversation = null) }
    }

    /**
     * Start a new conversation with a user or open an existing one.
     * Returns the conversation ID.
     */
    fun startOrOpenConversation(userId: String): String {
        // Check if conversation already exists
        val existingConversation = _uiState.value.conversations.find { conv ->
            conv.participants.contains(userId) && conv.participants.contains(CURRENT_USER_ID_MOCK)
        }

        if (existingConversation != null) {
            openConversation(existingConversation.id)
            return existingConversation.id
        }

        // Create a new conversation
        val newConversationId = "conv-${System.currentTimeMillis()}"
        val newConversation = Conversation(
            id = newConversationId,
            participants = listOf(CURRENT_USER_ID_MOCK, userId),
            messages = emptyList(),
            lastMessageTimestamp = Instant.now().toString(),
            unreadCount = 0
        )

        _userConversations.update { it + newConversation }

        viewModelScope.launch {
            fetchConversations()
            openConversation(newConversationId)
        }

        return newConversationId
    }

    // --- Conversations / DM actions ---

    fun sendDirectMessage(recipientId: String, content: String) {
        viewModelScope.launch {
            val moderationStatus = effectiveModerationStatus(content)
            handleAbusiveContent(recipientId, moderationStatus)

            val newMessage = DirectMessage(
                id = "dm-${System.currentTimeMillis()}",
                senderId = CURRENT_USER_ID_MOCK,
                recipientId = recipientId,
                content = content,
                timestamp = Instant.now().toString(),
                moderationStatus = moderationStatus
            )

            _userConversations.update { list ->
                val updatedList = list.map { conv ->
                    if (conv.participants.contains(recipientId)) {
                        conv.copy(
                            messages = conv.messages + newMessage,
                            lastMessageTimestamp = newMessage.timestamp,
                            unreadCount = 0
                        )
                    } else conv
                }
                // If it's a new conversation, create it (mock logic only)
                if (updatedList.none { it.participants.contains(recipientId) }) {
                    val newConv = Conversation(
                        id = "conv-${System.currentTimeMillis()}",
                        participants = listOf(CURRENT_USER_ID_MOCK, recipientId),
                        messages = listOf(newMessage),
                        lastMessageTimestamp = newMessage.timestamp,
                        unreadCount = 0
                    )
                    updatedList + newConv
                } else updatedList
            }

            fetchConversations()
            // Keep active conversation updated
            _uiState.update { state ->
                state.copy(
                    activeConversation = state.activeConversation?.let {
                        if (it.participants.contains(recipientId)) it.copy(messages = it.messages + newMessage) else it
                    }
                )
            }
        }
    }

    fun retryDirectMessage(convId: String, msgId: String) {
        viewModelScope.launch {
            // Find the failed message in the conversation
            val conversation = _userConversations.value.find { it.id == convId }
            val failedMessage = conversation?.messages?.find { it.id == msgId }

            if (failedMessage == null) {
                _uiState.update { it.copy(errorMessage = "Message not found for retry.") }
                return@launch
            }

            // Remove the failed message
            _userConversations.update { list ->
                list.map { conv ->
                    if (conv.id == convId) {
                        conv.copy(messages = conv.messages.filter { it.id != msgId })
                    } else conv
                }
            }

            // Resend with a new ID and timestamp
            val recipientId = failedMessage.recipientId
            sendDirectMessage(recipientId, failedMessage.content)
        }
    }

    fun reportMessage(messageId: String) {
        viewModelScope.launch {
            // Mark the message as reported in all conversations
            _userConversations.update { list ->
                list.map { conv ->
                    conv.copy(
                        messages = conv.messages.map { msg ->
                            if (msg.id == messageId) {
                                msg.copy(moderationStatus = ModerationStatus.FLAGGED)
                            } else msg
                        }
                    )
                }
            }

            // Update active conversation if it contains the message
            _uiState.update { state ->
                state.copy(
                    activeConversation = state.activeConversation?.let { conv ->
                        conv.copy(
                            messages = conv.messages.map { msg ->
                                if (msg.id == messageId) {
                                    msg.copy(moderationStatus = ModerationStatus.FLAGGED)
                                } else msg
                            }
                        )
                    },
                    errorMessage = null
                )
            }

            fetchConversations()
        }
    }

    /**
     * Add or toggle a reaction on a message.
     * Works like WhatsApp/Telegram/iMessage - tapping the same reaction removes it.
     */
    fun reactToMessage(conversationId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            _userConversations.update { list ->
                list.map { conv ->
                    if (conv.id == conversationId) {
                        conv.copy(
                            messages = conv.messages.map { msg ->
                                if (msg.id == messageId) {
                                    val currentReactions = msg.reactions.toMutableList()
                                    val existingReaction = currentReactions.find {
                                        it.userId == CURRENT_USER_ID_MOCK && it.emoji == emoji
                                    }

                                    if (existingReaction != null) {
                                        // Remove reaction (toggle off)
                                        currentReactions.remove(existingReaction)
                                    } else {
                                        // Add new reaction
                                        currentReactions.add(
                                            MessageReaction(
                                                emoji = emoji,
                                                userId = CURRENT_USER_ID_MOCK,
                                                timestamp = Instant.now().toString()
                                            )
                                        )
                                    }
                                    msg.copy(reactions = currentReactions)
                                } else msg
                            }
                        )
                    } else conv
                }
            }

            // Update active conversation if this is the current one
            _uiState.update { state ->
                val updatedActiveConv = _userConversations.value.find { it.id == conversationId }
                state.copy(activeConversation = updatedActiveConv ?: state.activeConversation)
            }
        }
    }

    fun submitReport(contentType: String, contentId: String, reason: String) {
        viewModelScope.launch {
            // Log locally for testing as requested
            val report = mapOf(
                "timestamp" to Instant.now().toString(),
                "content_type" to contentType,
                "content_id" to contentId,
                "reason" to reason
            )
            _uiState.update { it.copy(submittedReports = it.submittedReports + report) }

            try {
                val client = AppSupabaseClient.client ?: run {
                    android.util.Log.d("FeedViewModel", "Supabase not available, report stored locally only")
                    _uiState.update { it.copy(errorMessage = "Report submitted (Local Test Mode)") }
                    return@launch
                }
                val userId = client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.update { it.copy(errorMessage = "Report submitted (Local Test Mode)") }
                    return@launch
                }
                val payload = kotlinx.serialization.json.buildJsonObject {
                    put("reporter_id", kotlinx.serialization.json.JsonPrimitive(userId))
                    put("content_type", kotlinx.serialization.json.JsonPrimitive(contentType))
                    put("content_id", kotlinx.serialization.json.JsonPrimitive(contentId))
                    put("reason", kotlinx.serialization.json.JsonPrimitive(reason))
                }
                client.safeInsert("reports", payload)
                android.util.Log.d("FeedViewModel", "Successfully submitted report for $contentType $contentId")
                _uiState.update { it.copy(errorMessage = "Report submitted successfully") }
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "Failed to submit report", e)
                _uiState.update { it.copy(errorMessage = "Failed to submit report. Stored locally.") }
            }
        }
    }

    fun isUserBlocked(userId: String): Boolean {
        return _uiState.value.blockedUserIds.contains(userId)
                || (userId == DINO_USER_ID && _uiState.value.isDinoBanned)
    }

    fun isUserMuted(userId: String): Boolean {
        return _uiState.value.mutedUserIds.contains(userId)
    }

    fun blockUser(userId: String) {
        _uiState.update { it.copy(
            blockedUserIds = it.blockedUserIds + userId,
            errorMessage = "$userId blocked."
        ) }
        banUser(userId)
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val currentId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
                val client = AppSupabaseClient.client ?: return@launch
                client.safeInsert("blocked_users", kotlinx.serialization.json.buildJsonObject {
                    put("blocker_id", kotlinx.serialization.json.JsonPrimitive(currentId))
                    put("blocked_id", kotlinx.serialization.json.JsonPrimitive(userId))
                })
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "Failed to persist block", e)
            }
        }
    }

    fun unblockUser(userId: String) {
        _uiState.update { it.copy(
            blockedUserIds = it.blockedUserIds - userId,
            errorMessage = "$userId unblocked."
        ) }
        if (userId == DINO_USER_ID) {
            _uiState.update { it.copy(isDinoBanned = false) }
        }
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val currentId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
                safeDelete("blocked_users", "blocker_id=eq.$currentId&blocked_id=eq.$userId")
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "Failed to persist unblock", e)
            }
        }
    }

    fun muteUser(userId: String) {
        _uiState.update { it.copy(
            mutedUserIds = it.mutedUserIds + userId,
            errorMessage = "$userId muted."
        ) }
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val currentId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
                val client = AppSupabaseClient.client ?: return@launch
                client.safeInsert("muted_users", kotlinx.serialization.json.buildJsonObject {
                    put("muter_id", kotlinx.serialization.json.JsonPrimitive(currentId))
                    put("muted_id", kotlinx.serialization.json.JsonPrimitive(userId))
                })
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "Failed to persist mute", e)
            }
        }
    }

    fun unmuteUser(userId: String) {
        _uiState.update { it.copy(
            mutedUserIds = it.mutedUserIds - userId,
            errorMessage = "$userId unmuted."
        ) }
        // Persist to Supabase
        viewModelScope.launch {
            try {
                val currentId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
                safeDelete("muted_users", "muter_id=eq.$currentId&muted_id=eq.$userId")
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "Failed to persist unmute", e)
            }
        }
    }

    /** Hide a post from the feed (client-side removal). */
    fun hidePost(postId: Long) {
        _uiState.update { state ->
            state.copy(posts = state.posts.filter { it.id != postId })
        }
    }


    // --- Bookmark toggling (Supabase-persisted) ---

    fun toggleBookmark(postId: Long) {
        val currentlyBookmarked = _uiState.value.bookmarkedPostIds.contains(postId)
        // Optimistic UI update
        _uiState.update {
            it.copy(
                bookmarkedPostIds = if (currentlyBookmarked) it.bookmarkedPostIds - postId
                                    else it.bookmarkedPostIds + postId
            )
        }
        // Persist to Supabase
        viewModelScope.launch {
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
            BookmarksRepository.toggleBookmark(userId, postId)
        }
    }

    fun isPostBookmarked(postId: Long): Boolean {
        return _uiState.value.bookmarkedPostIds.contains(postId)
    }

    // --- Follow toggling (Supabase-persisted) ---

    fun toggleFollow(targetUserId: String) {
        val parentalRestriction = shouldBlockFeature(
            parentalState = ParentalControlsSettings.getState(getApplication()),
            feature = BlockableFeature.FOLLOWS
        )
        if (parentalRestriction != null) {
            _uiState.update {
                it.copy(errorMessage = "Follow actions are restricted by parental controls right now.")
            }
            return
        }

        val currentlyFollowing = _uiState.value.followingUserIds.contains(targetUserId)
        // Optimistic UI update
        _uiState.update {
            it.copy(
                followingUserIds = if (currentlyFollowing) it.followingUserIds - targetUserId
                                   else it.followingUserIds + targetUserId
            )
        }
        // Persist to Supabase
        viewModelScope.launch {
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
            FollowsRepository.toggleFollow(userId, targetUserId)
        }
    }

    fun isFollowingUser(targetUserId: String): Boolean {
        return _uiState.value.followingUserIds.contains(targetUserId)
    }

    // --- Premium ---

    fun setPremiumStatus(isPremium: Boolean) {
        // SECURITY: Only allow setting premium if it comes from verified source
        if (isPremium && !BuildConfig.DEBUG) {
            // In release builds, verify premium status is legitimate
            SubscriptionManager.checkPremiumStatus { verifiedPremium ->
                if (verifiedPremium) {
                    realPremiumStatus = true
                    _uiState.update { it.copy(isPremium = if (it.isFakePremiumEnabled) true else true) }
                } else {
                    // Attempted to set premium without valid purchase
                    android.util.Log.w("FeedViewModel", "⚠️ Premium status verification failed")
                    realPremiumStatus = false
                    _uiState.update { it.copy(isPremium = it.isFakePremiumEnabled) }
                }
            }
        } else {
            // Debug builds or setting to false
            realPremiumStatus = isPremium
            _uiState.update { it.copy(isPremium = if (it.isFakePremiumEnabled) true else isPremium) }
        }
    }

    fun toggleFakePremium(enabled: Boolean) {
        // SECURITY: Only allow fake premium on debug builds or authorized dev devices
        val hasDeveloperAccess = BuildConfig.DEBUG || DeviceAuthority.isAuthorizedDevice(getApplication())
        if (!hasDeveloperAccess && enabled) {
            android.util.Log.e("FeedViewModel", "🚨 SECURITY: Fake premium blocked in release build")
            throw SecurityException("Debug features are not available in production builds.")
        }

        // Keep GoogleAdsManager in sync with the simulated status
        GoogleAdsManager.devSetSimulatePremium(enabled)

        // If enabled, also simulate a successful purchase in SubscriptionManager for full integration testing
        if (enabled) {
            SubscriptionManager.simulateTestSuccess()
        } else {
            SubscriptionManager.resetTestPurchase()
        }

        _uiState.update {
            it.copy(
                isFakePremiumEnabled = enabled,
                isPremium = if (enabled) true else realPremiumStatus
            )
        }
        // Force an update on GoogleAdsManager's state flow to push changes to UI listeners
        GoogleAdsManager.devUpdateAdsState()
    }

    // --- Feed toggles ---

    fun toggleStories(enabled: Boolean) {
        _uiState.update { it.copy(showStories = enabled) }
    }

    fun toggleVideoAutoplay(enabled: Boolean) {
        _uiState.update { it.copy(isVideoAutoplayEnabled = enabled) }
    }

    fun setDevUiModes(
        isMockInterfaceEnabled: Boolean = _uiState.value.isMockInterfaceEnabled,
        isFallbackUiEnabled: Boolean = _uiState.value.isFallbackUiEnabled
    ) {
        _uiState.update {
            it.copy(
                isMockInterfaceEnabled = isMockInterfaceEnabled,
                isFallbackUiEnabled = isFallbackUiEnabled
            )
        }
    }

    fun toggleMockInterface(enabled: Boolean) {
        setDevUiModes(isMockInterfaceEnabled = enabled)
        fetchPosts()
    }

    fun toggleFallbackUi(enabled: Boolean) {
        setDevUiModes(isFallbackUiEnabled = enabled)
    }

    // --- Data loading ---

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val opts = devOptions()

            if (opts.simulateOffline) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No network connection (simulated offline)") }
                return@launch
            }
            if (opts.simulateLoadingError) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Simulated server error (HTTP 500)") }
                return@launch
            }
            if (opts.infiniteLoading) {
                // keep loading forever
                return@launch
            }

            // --- PREMIUM BENEFIT: Faster Loading ---
            // If the user is premium, reduce the simulated network latency by 75%
            val effectiveLatency = if (_uiState.value.isPremium) {
                (opts.networkLatencyMs / 4).coerceAtLeast(0)
            } else {
                opts.networkLatencyMs
            }

            if (effectiveLatency > 0) {
                delay(effectiveLatency)
            }

            // Fetch from Supabase if available, otherwise use localized mock data
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            
            val remotePosts = if (AppSupabaseClient.isAvailable()) {
                PostsRepository.fetchPosts()
            } else {
                emptyList()
            }

            // Get liked posts for the current user
            val likedPostIds = if (AppSupabaseClient.isAvailable() && userId != CURRENT_USER_ID_MOCK) {
                LikesRepository.getUserLikedPosts(userId).toSet()
            } else {
                emptySet<Long>()
            }

            // Combine localized explore posts with localized feed posts and remote posts
            val deletedIds = _deletedPostIds.value
            val combinedPosts = (_userCreatedPosts.value + remotePosts + _localizedPosts + _localizedFeedPosts)
                .distinctBy { it.id }
                .filter { it.id !in deletedIds }
                .map { post ->
                    if (post.id in likedPostIds) {
                        post.copy(isLikedByMe = true)
                    } else post
                }

            _uiState.update { it.copy(posts = combinedPosts, isLoading = false) }
        }
    }

    /**
     * Resets all mock data to their initial localized states.
     * Use this during development/testing to restore original content.
     */
    fun resetMockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500) // Visual feedback for reset

            _userStories.value = emptyList()
            _userConversations.value = MockDataProvider.getLocalizedConversations(getApplication())
            _userCreatedPosts.value = emptyList()
            _deletedPostIds.value = emptySet()

            _uiState.update { state ->
                state.copy(
                    posts = MockDataProvider.getLocalizedFeedPosts(getApplication()) + MOCK_FEED_POSTS,
                    notifications = MockDataProvider.getLocalizedNotifications(getApplication()),
                    conversations = MockDataProvider.getLocalizedConversations(getApplication()),
                    stories = MOCK_STORIES,
                    activeStory = null,
                    activePostId = null,
                    activePostComments = emptyList(),
                    commentDraftsByPostId = emptyMap(),
                    commentsByPostId = emptyMap(),
                    isLoading = false,
                    isDinoBanned = false
                )
            }
        }
    }

    private fun fetchStories() {
        viewModelScope.launch {
            val remoteStories = if (AppSupabaseClient.isAvailable()) {
                StoriesRepository.fetchStories()
            } else {
                emptyList()
            }
            
            val allStories = _userStories.value + remoteStories + MOCK_STORIES
            
            _uiState.update { state ->
                state.copy(
                    stories = allStories.distinctBy { it.id },
                    activeStory = state.activeStory?.let { active ->
                        allStories.firstOrNull { it.id == active.id }
                    }
                )
            }
        }
    }

    // --- Post actions ---

    fun createPost(content: String, tone: String, imageUrl: String? = null, videoUrl: String? = null, backgroundColor: Long? = null, locationTag: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            
            if (AppSupabaseClient.isAvailable() && userId != CURRENT_USER_ID_MOCK) {
                val result = PostsRepository.createPost(
                    userId = userId,
                    content = content,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    backgroundColor = backgroundColor,
                    category = tone,
                    locationTag = locationTag
                )
                
                result.onSuccess { fetchPosts() }
                    .onFailure { error ->
                        _uiState.update { it.copy(errorMessage = "Failed to post to server: ${error.message}", isLoading = false) }
                    }
            } else {
                // Fallback to local-only
                val newPost = Post(
                    id = System.currentTimeMillis(),
                    createdAt = Instant.now().toString(),
                    content = content,
                    userId = CURRENT_USER_ID_MOCK,
                    likes = 0,
                    comments = 0,
                    shares = 0,
                    isLikedByMe = false,
                    imageUrl = imageUrl,
                    userAvatar = CURRENT_USER.avatarUrl,
                    videoUrl = videoUrl,
                    backgroundColor = backgroundColor,
                    locationTag = locationTag
                )
                _userCreatedPosts.update { listOf(newPost) + it }
                fetchPosts()
            }
        }
    }

    fun deletePost(postId: Long) {
        // Immediately remove from UI
        _uiState.update { state ->
            state.copy(posts = state.posts.filter { it.id != postId })
        }
        // Track deletion so the post doesn't reappear on refresh
        _deletedPostIds.update { it + postId }
        _userCreatedPosts.update { it.filter { p -> p.id != postId } }

        viewModelScope.launch {
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            if (AppSupabaseClient.isAvailable() && !isMockPostId(postId)) {
                PostsRepository.deletePost(postId, userId)
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            if (AppSupabaseClient.isAvailable() && storyId.startsWith("s-")) {
                StoriesRepository.deleteStory(storyId, userId)
            }
            
            // Remove the story from the main stories list
            _uiState.update { currentState ->
                currentState.copy(
                    stories = currentState.stories.filter { story -> story.id != storyId },
                    isLoading = false
                )
            }
            // Remove the story from the mutable user stories list
            _userStories.update { currentStories ->
                currentStories.filter { story -> story.id != storyId }
            }
        }
    }

    fun deleteStoryItem(storyId: String, itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            if (AppSupabaseClient.isAvailable() && !itemId.startsWith("si-")) {
                StoriesRepository.deleteStory(itemId, userId)
            }

            // Remove the item from the story in main stories list
            _uiState.update { currentState ->
                val updatedStories = currentState.stories.mapNotNull { story ->
                    if (story.id == storyId) {
                        val updatedItems = story.items.filter { it.id != itemId }
                        if (updatedItems.isEmpty()) null else story.copy(items = updatedItems)
                    } else {
                        story
                    }
                }
                
                // If the active story was deleted completely, dismiss it
                var newActiveStory = currentState.activeStory
                if (newActiveStory?.id == storyId) {
                    val activeAfterDelete = updatedStories.find { it.id == storyId }
                    if (activeAfterDelete == null) {
                        newActiveStory = null
                    } else {
                        newActiveStory = activeAfterDelete
                    }
                }

                currentState.copy(
                    stories = updatedStories,
                    activeStory = newActiveStory,
                    isLoading = false
                )
            }

            // Remove the item from mutable user stories list
            _userStories.update { currentStories ->
                currentStories.mapNotNull { story ->
                    if (story.id == storyId) {
                        val updatedItems = story.items.filter { it.id != itemId }
                        if (updatedItems.isEmpty()) null else story.copy(items = updatedItems)
                    } else {
                        story
                    }
                }
            }
        }
    }

    // --- Story actions ---

    fun viewStory(story: Story) {
        _uiState.update { it.copy(activeStory = story) }
    }

    fun dismissStory() {
        _uiState.update { it.copy(activeStory = null) }
    }

    private fun moveToAdjacentStory(direction: Int, markCurrentViewed: Boolean) {
        val current = _uiState.value.activeStory ?: run {
            dismissStory()
            return
        }
        if (markCurrentViewed) {
            markStoryAsViewed(current.id)
        }

        val stories = _uiState.value.stories
        val currentIndex = stories.indexOfFirst { it.id == current.id }
        if (currentIndex < 0) {
            dismissStory()
            return
        }

        val adjacentStory = when {
            direction > 0 -> stories.drop(currentIndex + 1).firstOrNull()
            direction < 0 -> stories.take(currentIndex).lastOrNull()
            else -> null
        }

        if (adjacentStory != null) {
            _uiState.update { it.copy(activeStory = adjacentStory) }
        } else {
            dismissStory()
        }
    }

    /** Advance to the next story user after the current user is finished. */
    fun advanceToNextStory() {
        moveToAdjacentStory(direction = 1, markCurrentViewed = true)
    }

    /** Skip to the next user's stories without forcing this user's remaining items to be completed. */
    fun skipToNextStoryUser() {
        moveToAdjacentStory(direction = 1, markCurrentViewed = false)
    }

    /** Return to the previous user's stories. */
    fun goToPreviousStoryUser() {
        moveToAdjacentStory(direction = -1, markCurrentViewed = false)
    }

    fun markStoryAsViewed(storyId: String) {
        _uiState.update { state ->
            val updatedStories = state.stories.map { story ->
                if (story.id == storyId) story.copy(isViewed = true) else story
            }
            state.copy(stories = updatedStories)
        }
    }

    fun createStory(
        imageUrl: String,
        duration: Long,
        contentType: StoryContentType = StoryContentType.IMAGE,
        textOverlay: String? = null,
        backgroundColor: Long = 0xFF1a1a2eL,
        backgroundColorEnd: Long? = null,
        linkPreview: LinkPreviewData? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
            
            if (AppSupabaseClient.isAvailable() && userId != CURRENT_USER_ID_MOCK) {
                val result = StoriesRepository.createStory(
                    userId = userId,
                    imageUrl = imageUrl,
                    duration = duration,
                    contentType = contentType,
                    textOverlay = textOverlay,
                    backgroundColor = backgroundColor,
                    backgroundColorEnd = backgroundColorEnd,
                    linkPreview = linkPreview
                )
                result.onSuccess { fetchStories() }
                    .onFailure { error ->
                        _uiState.update { it.copy(errorMessage = "Failed to post story: ${error.message}", isLoading = false) }
                    }
            } else {
                val newItem = StoryItem(
                    id = "si-${System.currentTimeMillis()}",
                    imageUrl = imageUrl,
                    duration = duration,
                    contentType = contentType,
                    textOverlay = textOverlay,
                    backgroundColor = backgroundColor,
                    backgroundColorEnd = backgroundColorEnd,
                    linkPreview = linkPreview
                )

                _userStories.update { currentStories ->
                    val myExistingStory = currentStories.find { it.userName == CURRENT_USER.name }
                    if (myExistingStory != null) {
                        currentStories.map { 
                            if (it.id == myExistingStory.id) {
                                it.copy(items = it.items + newItem, isViewed = false)
                            } else it
                        }
                    } else {
                        val newStory = Story(
                            id = "s-me",
                            userName = CURRENT_USER.name,
                            userAvatar = CURRENT_USER.avatarUrl,
                            items = listOf(newItem),
                            userId = CURRENT_USER_ID_MOCK
                        )
                        listOf(newStory) + currentStories
                    }
                }
                fetchStories()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun toggleLike(postId: Long) {
        val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK
        
        // Update UI immediately for responsive feel
        try {
            _uiState.update { currentState ->
                val updatedPosts = currentState.posts.map { post ->
                    if (post.id == postId) {
                        val nowLiked = !post.isLikedByMe
                        val newLikeCount = (post.likes + if (nowLiked) 1 else -1).coerceAtLeast(0)

                        // Log the like action for debugging
                        android.util.Log.d("NeuroComet", "👍 Like toggled: Post #$postId | Liked: $nowLiked | Count: $newLikeCount")

                        post.copy(
                            isLikedByMe = nowLiked,
                            likes = newLikeCount
                        )
                    } else post
                }
                currentState.copy(posts = updatedPosts)
            }
        } catch (e: Exception) {
            android.util.Log.e("NeuroComet", "❌ Failed to update like UI state", e)
            return // Don't attempt network call if UI update failed
        }

        // Skip Supabase persistence for mock/local-only posts.
        // Mock posts use small hard-coded IDs (1-17, 101-106) that collide with
        // real auto-incremented IDs in the database, causing data corruption.
        if (isMockPostId(postId)) {
            android.util.Log.d("NeuroComet", "⏭️ Skipping Supabase persist for mock post #$postId")
            return
        }

        // Persist to Supabase in background
        viewModelScope.launch {
            try {
                val result = LikesRepository.toggleLike(postId, userId)
                result.onSuccess { isNowLiked ->
                    android.util.Log.d("NeuroComet", "✅ Like persisted to Supabase: Post #$postId | Liked: $isNowLiked")
                }.onFailure { error ->
                    android.util.Log.w("NeuroComet", "⚠️ Like not persisted (local only): ${error.message}")
                    // Like still works locally, just not synced to server
                }
            } catch (e: Exception) {
                android.util.Log.w("NeuroComet", "⚠️ Like persist failed (local only): ${e.message}")
            }
        }
    }

    // --- Comments bottom sheet ---

    fun openCommentSheet(post: Post) {
        val postId = post.id ?: return
        
        _uiState.update { it.copy(activePostId = postId, isCommentSheetVisible = true, isLoading = true) }
        
        viewModelScope.launch {
            val remoteComments = if (AppSupabaseClient.isAvailable() && !isMockPostId(postId)) {
                CommentsRepository.fetchComments(postId)
            } else {
                emptyList()
            }
            
            _uiState.update { state ->
                val localComments = state.commentsByPostId[postId].orEmpty()
                state.copy(
                    activePostComments = (remoteComments + localComments).distinctBy { it.id },
                    isLoading = false
                )
            }
        }
    }

    fun dismissCommentSheet() {
        _uiState.update {
            it.copy(
                isCommentSheetVisible = false,
                activePostId = null,
                activePostComments = emptyList()
            )
        }
    }

    fun updateActiveCommentDraft(content: String) {
        val postId = _uiState.value.activePostId ?: return
        _uiState.update { state ->
            state.copy(commentDraftsByPostId = state.commentDraftsByPostId + (postId to content))
        }
    }

    fun addComment(content: String) {
        val postId = _uiState.value.activePostId ?: return
        val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: CURRENT_USER_ID_MOCK

        // Optimistic UI update
        val newComment = Comment(
            id = "c-${System.currentTimeMillis()}",
            postId = postId,
            userId = userId,
            userAvatar = CURRENT_USER.avatarUrl,
            content = content,
            timestamp = Instant.now().toString()
        )

        _uiState.update { state ->
            val existingComments = state.commentsByPostId[postId].orEmpty()
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + 1)
                } else post
            }
            state.copy(
                activePostComments = (existingComments + newComment).distinctBy { it.id },
                commentsByPostId = state.commentsByPostId + (postId to (existingComments + newComment)),
                commentDraftsByPostId = state.commentDraftsByPostId - postId,
                posts = updatedPosts
            )
        }

        // Persist to Supabase
        viewModelScope.launch {
            if (AppSupabaseClient.isAvailable() && !isMockPostId(postId) && userId != CURRENT_USER_ID_MOCK) {
                CommentsRepository.addComment(postId, userId, content)
            } else {
                // Keep the existing safeInsert logic for backward compatibility/simpler setups
                try {
                    val client = AppSupabaseClient.client ?: return@launch
                    client.safeInsert("post_comments", kotlinx.serialization.json.buildJsonObject {
                        put("post_id", kotlinx.serialization.json.JsonPrimitive(postId))
                        put("user_id", kotlinx.serialization.json.JsonPrimitive(userId))
                        put("content", kotlinx.serialization.json.JsonPrimitive(content))
                    })
                } catch (e: Exception) {
                    android.util.Log.w("FeedVM", "Failed to persist comment to Supabase", e)
                }
            }
        }
    }

    // --- Dev-only: Story testing ---

    /**
     * Helper: persist a list of StoryItems via optimistic local update + Supabase.
     *
     * 1. **Always** adds to local [_userStories] immediately (so the story is visible).
     * 2. If Supabase is available AND the user is authenticated, also inserts each item
     *    via [StoriesRepository.createStory] (uses the real `user_id` for FK/RLS).
     * 3. Reports outcome to [_devStoryStatus] so the dev UI shows what happened.
     */
    private fun devPersistStoryItems(
        items: List<StoryItem>,
        localFallbackLabel: String = "DevStory"
    ) {
        viewModelScope.launch {
            // ── Step 1: optimistic local insert ─────────────────────
            val ts = System.currentTimeMillis()
            val localStory = Story(
                id = "dev-$localFallbackLabel-$ts",
                userName = localFallbackLabel,
                userAvatar = avatarUrl(localFallbackLabel.lowercase()),
                userId = CURRENT_USER_ID_MOCK,
                items = items
            )
            _userStories.update { listOf(localStory) + it }
            fetchStories()

            // ── Step 2: attempt Supabase persist ────────────────────
            val client = AppSupabaseClient.client
            val userId = client?.auth?.currentUserOrNull()?.id
            val sessionToken = try {
                client?.auth?.currentSessionOrNull()?.accessToken
            } catch (_: Exception) { null }

            if (!AppSupabaseClient.isAvailable()) {
                _devStoryStatus.value = "⚠️ Supabase client not configured → local-only"
                return@launch
            }
            if (userId == null || userId == CURRENT_USER_ID_MOCK) {
                _devStoryStatus.value = "⚠️ Not signed in (userId=$userId) → local-only"
                return@launch
            }
            if (sessionToken.isNullOrBlank()) {
                _devStoryStatus.value = "⚠️ No active session / expired JWT → local-only (userId=$userId)"
                return@launch
            }

            // We have a client, userId, and session → try Supabase

            // Ensure the public.users row exists (FK target for stories.user_id).
            // The DB trigger should create it on sign-up, but it may be missing
            // if the SQL setup script wasn't applied or the trigger failed.
            try {
                val userEmail = client.auth.currentUserOrNull()?.email ?: ""
                val displayName = userEmail.substringBefore("@").ifEmpty { "DevTester" }
                client.safeUpsert("users", kotlinx.serialization.json.buildJsonObject {
                    put("id", userId)
                    put("email", userEmail)
                    put("username", "dev_${userId.take(8)}")
                    put("display_name", displayName)
                    put("created_at", java.time.Instant.now().toString())
                    put("updated_at", java.time.Instant.now().toString())
                })
            } catch (e: Exception) {
                android.util.Log.w("FeedVM", "ensurePublicUserExists: ${e.message}")
            }

            var succeeded = 0
            var failed = 0
            val errors = mutableListOf<String>()
            for (item in items) {
                val result = StoriesRepository.createStory(
                    userId = userId,
                    imageUrl = item.imageUrl,
                    duration = item.duration,
                    contentType = item.contentType,
                    textOverlay = item.textOverlay,
                    backgroundColor = item.backgroundColor,
                    backgroundColorEnd = item.backgroundColorEnd,
                    linkPreview = item.linkPreview
                )
                result.onSuccess { succeeded++ }
                result.onFailure { e ->
                    failed++
                    errors.add(e.message ?: "unknown")
                    android.util.Log.w("FeedVM", "devPersistStoryItems: insert failed", e)
                }
            }

            _devStoryStatus.value = if (failed == 0) {
                "✅ Inserted $succeeded item(s) into Supabase (userId=${userId.take(8)}…)"
            } else {
                "❌ $failed/${ succeeded + failed } inserts failed: ${errors.firstOrNull()}"
            }
            // Re-fetch to include the Supabase-persisted stories
            fetchStories()
        }
    }

    /**
     * Inject a single test story with one image item.
     * Good for verifying basic story display, progress bar, and auto-advance.
     */
    fun devAddSingleImageStory() {
        devPersistStoryItems(
            items = listOf(
                StoryItem(
                    id = "si-dev-${System.currentTimeMillis()}",
                    imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080",
                    duration = 5000L
                )
            ),
            localFallbackLabel = "DevTester"
        )
    }

    /**
     * Inject a multi-item story (3 pages) to test navigation between items,
     * progress bar segments, and auto-advance between pages.
     */
    fun devAddMultiItemStory() {
        val ts = System.currentTimeMillis()
        devPersistStoryItems(
            items = listOf(
                StoryItem(
                    id = "si-m1-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1080",
                    duration = 4000L
                ),
                StoryItem(
                    id = "si-m2-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1080",
                    duration = 3000L
                ),
                StoryItem(
                    id = "si-m3-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=1080",
                    duration = 5000L
                )
            ),
            localFallbackLabel = "MultiStory"
        )
    }

    /**
     * Inject a text-only story with gradient background.
     * Tests the TEXT_ONLY content type path and overlay rendering.
     */
    fun devAddTextOnlyStory() {
        devPersistStoryItems(
            items = listOf(
                StoryItem(
                    id = "si-txt-${System.currentTimeMillis()}",
                    imageUrl = "Today's vibe: hyperfocusing on accessibility improvements 🧠✨",
                    duration = 6000L,
                    contentType = StoryContentType.TEXT_ONLY,
                    backgroundColor = 0xFF6A1B9AL,
                    backgroundColorEnd = 0xFF0D47A1L
                )
            ),
            localFallbackLabel = "TextPoster"
        )
    }

    /**
     * Inject a story with a link preview to test the LINK content type.
     */
    fun devAddLinkPreviewStory() {
        devPersistStoryItems(
            items = listOf(
                StoryItem(
                    id = "si-link-${System.currentTimeMillis()}",
                    imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080",
                    duration = 8000L,
                    contentType = StoryContentType.LINK,
                    textOverlay = "Check out this awesome resource!",
                    linkPreview = LinkPreviewData(
                        url = "https://www.neurodivergent-resources.org",
                        title = "Neurodivergent Resources Hub",
                        description = "A curated collection of tools, tips, and communities for neurodivergent individuals.",
                        imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400",
                        siteName = "ND Resources"
                    )
                )
            ),
            localFallbackLabel = "LinkSharer"
        )
    }

    /**
     * Inject a story from the "current user" to test own-story features
     * (delete button, "Your Story" indicator, etc.).
     */
    fun devAddOwnStory() {
        // Own story always uses createStory() which already handles Supabase vs local
        createStory(
            imageUrl = "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=1080",
            duration = 5000L,
            textOverlay = "My dev test story! 🚀"
        )
    }

    /**
     * Bulk-add many stories to test scrolling the stories bar, performance,
     * and navigation through a long list.
     */
    fun devFloodStories(count: Int = 10) {
        val ts = System.currentTimeMillis()
        val items = (1..count).map { i ->
            StoryItem(
                id = "si-flood-$ts-$i",
                imageUrl = "https://picsum.photos/seed/nc$i/1080/1920",
                duration = 4000L
            )
        }
        devPersistStoryItems(items, localFallbackLabel = "FloodTest")
    }

    /**
     * Clear all user-created/dev stories, keeping only the built-in MOCK_STORIES.
     */
    fun devClearStories() {
        _userStories.value = emptyList()
        _uiState.update { it.copy(stories = MOCK_STORIES) }
    }

    /**
     * Add a story that mixes all supported content types in one multi-page story.
     * This is the comprehensive test that exercises every rendering path.
     */
    fun devAddKitchenSinkStory() {
        val ts = System.currentTimeMillis()
        devPersistStoryItems(
            items = listOf(
                StoryItem(
                    id = "si-ks1-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080",
                    duration = 4000L,
                    contentType = StoryContentType.IMAGE,
                    textOverlay = "Page 1: Image with overlay"
                ),
                StoryItem(
                    id = "si-ks2-$ts",
                    imageUrl = "Page 2: Text-only with gradient 🎨",
                    duration = 5000L,
                    contentType = StoryContentType.TEXT_ONLY,
                    backgroundColor = 0xFFE91E63L,
                    backgroundColorEnd = 0xFFFF9800L
                ),
                StoryItem(
                    id = "si-ks3-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080",
                    duration = 7000L,
                    contentType = StoryContentType.LINK,
                    textOverlay = "Page 3: Link preview",
                    linkPreview = LinkPreviewData(
                        url = "https://github.com/neurocomet",
                        title = "NeuroComet on GitHub",
                        description = "Open-source neurodivergent social platform",
                        siteName = "GitHub"
                    )
                ),
                StoryItem(
                    id = "si-ks4-$ts",
                    imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080",
                    duration = 3000L,
                    contentType = StoryContentType.IMAGE
                ),
                StoryItem(
                    id = "si-ks5-$ts",
                    imageUrl = "Page 5: Final page — does auto-dismiss work? ✅",
                    duration = 4000L,
                    contentType = StoryContentType.TEXT_ONLY,
                    backgroundColor = 0xFF1B5E20L,
                    backgroundColorEnd = 0xFF004D40L
                )
            ),
            localFallbackLabel = "KitchenSink"
        )
    }

    // --- Dev-only actions referenced in SettingsScreen ---

    fun stressTestDb() {
        viewModelScope.launch {
            repeat(50) { idx ->
                createPost("Stress post #$idx", tone = "/gen")
            }
        }
    }

    fun floodDb() {
        repeat(5) { idx ->
            createPost("Flood post #$idx", tone = "/gen")
        }
    }

    fun nukeDb() {
        _uiState.update { it.copy(posts = emptyList(), errorMessage = "Local feed cleared (mock).") }
    }

    fun sharePost(context: Context, post: Post) {
        val postUrl = AppLinks.postUrl(post.id)
        val shareTitle = post.content.ifBlank { "NeuroComet post" }.take(80)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, postUrl)
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            putExtra(Intent.EXTRA_TITLE, shareTitle)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.post_share_post_via))
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)

        // Update share count
        _uiState.update { state ->
            val updatedPosts = state.posts.map { p ->
                if (p.id == post.id) p.copy(shares = p.shares + 1) else p
            }
            state.copy(posts = updatedPosts)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Notifications ---

    fun fetchNotifications() {
        viewModelScope.launch {
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id

            if (userId != null && userId != CURRENT_USER_ID_MOCK && AppSupabaseClient.isAvailable()) {
                // Real mode: fetch from Supabase, merge with any locally-added notifications
                try {
                    val remote = NotificationsRepository.fetchNotifications(userId)
                    val localOnly = _notifications.value.filter { local ->
                        remote.none { it.id == local.id }
                    }
                    val merged = (localOnly + remote).sortedByDescending {
                        runCatching { Instant.parse(it.timestamp) }.getOrNull() ?: Instant.EPOCH
                    }
                    _notifications.value = merged
                } catch (e: Exception) {
                    android.util.Log.w("FeedVM", "Failed to fetch remote notifications", e)
                }
            }

            _uiState.update { it.copy(notifications = _notifications.value) }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
        }
        _uiState.update { it.copy(notifications = _notifications.value) }
        // Persist to Supabase
        viewModelScope.launch {
            NotificationsRepository.markAsRead(notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
        _uiState.update { it.copy(notifications = _notifications.value) }
        // Persist to Supabase
        viewModelScope.launch {
            val userId = AppSupabaseClient.client?.auth?.currentUserOrNull()?.id ?: return@launch
            NotificationsRepository.markAllAsRead(userId)
        }
    }

    fun dismissNotification(notificationId: String) {
        _notifications.update { list ->
            list.filter { it.id != notificationId }
        }
        _uiState.update { it.copy(notifications = _notifications.value) }
        // Persist to Supabase
        viewModelScope.launch {
            NotificationsRepository.deleteNotification(notificationId)
        }
    }

    // Legacy functions for backward compatibility
    fun markNotificationRead(notificationId: String) = markNotificationAsRead(notificationId)
    fun markAllNotificationsRead() = markAllNotificationsAsRead()
    fun deleteNotification(notificationId: String) = dismissNotification(notificationId)

    fun addNotification(notification: NotificationItem) {
        // Check notification settings — only add if the category is enabled
        val notifSettings = SocialSettingsManager.getNotificationSettings(getApplication())
        if (!notifSettings.pushEnabled) return
        val allowed = when (notification.type) {
            NotificationType.LIKE -> notifSettings.likesEnabled
            NotificationType.COMMENT -> notifSettings.commentsEnabled
            NotificationType.FOLLOW -> notifSettings.followsEnabled
            NotificationType.MENTION -> notifSettings.mentionsEnabled
            NotificationType.REPOST -> true
            NotificationType.SYSTEM -> true
            NotificationType.BADGE -> true
            NotificationType.WELCOME -> true
            NotificationType.SAFETY_ALERT -> true
        }
        if (!allowed) return

        _notifications.update { listOf(notification) + it }
        _uiState.update { it.copy(notifications = _notifications.value) }
    }
}

val MOCK_STORIES = listOf(
    Story(
        id = "1",
        userName = "NeuroTips",
        userAvatar = "https://i.pravatar.cc/150?u=neurotips",
        userId = "NeuroTips",
        items = listOf(
            StoryItem("s1i1", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=1080"),
            StoryItem("s1i2", "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1080"),
            StoryItem("s1i3", "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=1080")
        )
    ),
    Story(
        id = "2",
        userName = "StimSpace",
        userAvatar = "https://i.pravatar.cc/150?u=stimspace",
        userId = "StimSpace",
        items = listOf(
            StoryItem("s2i1", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080")
        )
    ),
    Story(
        id = "3",
        userName = "ADHDWins",
        userAvatar = "https://i.pravatar.cc/150?u=adhdwins",
        userId = "ADHDWins",
        items = listOf(
            StoryItem("s3i1", "https://images.unsplash.com/photo-1552581234-26160f608093?w=1080"),
            StoryItem("s3i2", "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1080")
        )
    ),
    Story(
        id = "4",
        userName = "CalmCorner",
        userAvatar = "https://i.pravatar.cc/150?u=calmcorner",
        userId = "CalmCorner",
        items = listOf(
            StoryItem("s4i1", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=1080")
        )
    ),
    Story(
        id = "5",
        userName = "FocusFlow",
        userAvatar = "https://i.pravatar.cc/150?u=focusflow",
        userId = "FocusFlow",
        items = listOf(
            StoryItem("s5i1", "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=1080"),
            StoryItem("s5i2", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=1080")
        )
    ),
    Story(
        id = "6",
        userName = "SensoryJoy",
        userAvatar = "https://i.pravatar.cc/150?u=sensoryjoy",
        userId = "SensoryJoy",
        items = listOf(
            StoryItem("s6i1", "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=1080")
        )
    )
)

val MOCK_CONVERSATIONS = listOf(
    Conversation(
        id = "conv1",
        participants = listOf(CURRENT_USER_ID_MOCK, DINO_USER_ID),
        messages = listOf(
            DirectMessage("msg1", DINO_USER_ID, CURRENT_USER_ID_MOCK, "Hey, how are you feeling today?", Instant.now().minusSeconds(3600).toString()),
            DirectMessage("msg2", CURRENT_USER_ID_MOCK, DINO_USER_ID, "I'm good, thanks! Just chilling.", Instant.now().minusSeconds(1800).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(1800).toString(),
        unreadCount = 0
    ),
    Conversation(
        id = "conv2",
        participants = listOf(CURRENT_USER_ID_MOCK, THERAPY_USER_ID),
        messages = listOf(
            DirectMessage("msg3", THERAPY_USER_ID, CURRENT_USER_ID_MOCK, "Did you see that new scam post? Don't fall for it!", Instant.now().minusSeconds(7200).toString(), moderationStatus = ModerationStatus.FLAGGED),
            DirectMessage("msg4", CURRENT_USER_ID_MOCK, THERAPY_USER_ID, "Thanks for the warning. I saw the phishing attempt.", Instant.now().minusSeconds(6000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(6000).toString(),
        unreadCount = 1
    ),
    Conversation(
        id = "conv3",
        participants = listOf(CURRENT_USER_ID_MOCK, "NeuroNaut"),
        messages = listOf(
            DirectMessage("msg5", "NeuroNaut", CURRENT_USER_ID_MOCK, "Hey! I saw your post about executive function struggles. I totally relate!", Instant.now().minusSeconds(86400).toString()),
            DirectMessage("msg6", CURRENT_USER_ID_MOCK, "NeuroNaut", "Right?! Some days are just harder than others.", Instant.now().minusSeconds(85000).toString()),
            DirectMessage("msg7", "NeuroNaut", CURRENT_USER_ID_MOCK, "Have you tried body doubling? It really helps me stay on task.", Instant.now().minusSeconds(84000).toString()),
            DirectMessage("msg8", CURRENT_USER_ID_MOCK, "NeuroNaut", "I've heard of it but never tried. How does it work for you?", Instant.now().minusSeconds(83000).toString()),
            DirectMessage("msg9", "NeuroNaut", CURRENT_USER_ID_MOCK, "I just video call a friend while we both work on our own stuff. The accountability helps!", Instant.now().minusSeconds(82000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(82000).toString(),
        unreadCount = 0
    ),
    Conversation(
        id = "conv4",
        participants = listOf(CURRENT_USER_ID_MOCK, "HyperFocusCode"),
        messages = listOf(
            DirectMessage("msg10", "HyperFocusCode", CURRENT_USER_ID_MOCK, "Yo! That productivity hack you shared was 🔥", Instant.now().minusSeconds(172800).toString()),
            DirectMessage("msg11", CURRENT_USER_ID_MOCK, "HyperFocusCode", "Glad it helped! The Pomodoro technique is a game changer.", Instant.now().minusSeconds(172000).toString()),
            DirectMessage("msg12", "HyperFocusCode", CURRENT_USER_ID_MOCK, "I modified it to 25 min work / 10 min stim break. Works way better for my ADHD brain.", Instant.now().minusSeconds(171000).toString()),
            DirectMessage("msg13", CURRENT_USER_ID_MOCK, "HyperFocusCode", "That's genius! Mind if I share that tip in my next post?", Instant.now().minusSeconds(170000).toString()),
            DirectMessage("msg14", "HyperFocusCode", CURRENT_USER_ID_MOCK, "Go for it! Tag me 😄", Instant.now().minusSeconds(169000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(169000).toString(),
        unreadCount = 2
    ),
    Conversation(
        id = "conv5",
        participants = listOf(CURRENT_USER_ID_MOCK, "SensorySeeker"),
        messages = listOf(
            DirectMessage("msg15", "SensorySeeker", CURRENT_USER_ID_MOCK, "🎨 Just finished a new painting during my hyperfocus session!", Instant.now().minusSeconds(259200).toString()),
            DirectMessage("msg16", CURRENT_USER_ID_MOCK, "SensorySeeker", "OMG I need to see it! You're so talented!", Instant.now().minusSeconds(258000).toString()),
            DirectMessage("msg17", "SensorySeeker", CURRENT_USER_ID_MOCK, "Thanks! It's all blues and purples - very calming colors. I'll post it later today.", Instant.now().minusSeconds(257000).toString()),
            DirectMessage("msg18", CURRENT_USER_ID_MOCK, "SensorySeeker", "Those are my favorite colors too! They really help when I'm overstimulated.", Instant.now().minusSeconds(256000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(256000).toString(),
        unreadCount = 0
    )
)



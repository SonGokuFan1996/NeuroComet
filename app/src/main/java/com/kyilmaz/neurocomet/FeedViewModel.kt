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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

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
        userAvatar = "https://i.pravatar.cc/150?u=focusqueen",
        imageUrl = "https://images.unsplash.com/photo-1593062096033-9a26b09da705?w=800"
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
        userAvatar = "https://i.pravatar.cc/150?u=ndpride"
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
        userAvatar = "https://i.pravatar.cc/150?u=adhdstudent"
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
        userAvatar = "https://i.pravatar.cc/150?u=stimhappy",
        imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800"
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
        userAvatar = "https://i.pravatar.cc/150?u=lifehacker"
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
        userAvatar = "https://i.pravatar.cc/150?u=selfcare"
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
    val isCommentSheetVisible: Boolean = false,
    val activeStory: Story? = null,
    val conversations: List<Conversation> = emptyList(),
    val activeConversation: Conversation? = null,
    val isDinoBanned: Boolean = false,
    val notifications: List<NotificationItem> = emptyList()
)

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _localizedNotifications = MockDataProvider.getLocalizedNotifications(application)
    private val _localizedPosts = MockDataProvider.getLocalizedExplorePosts(application)
    private val _localizedFeedPosts = MockDataProvider.getLocalizedFeedPosts(application)
    private val _localizedConversations = MockDataProvider.getLocalizedConversations(application)

    private var realPremiumStatus = false

    var simulateError = false
    var simulateInfiniteLoading = false

    private val _userStories = MutableStateFlow<List<Story>>(emptyList())
    private val _userConversations = MutableStateFlow<List<Conversation>>(_localizedConversations)

    // Mock strike counter
    private val _userStrikeCount = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Notifications - use localized data
    private val _notifications = MutableStateFlow<List<NotificationItem>>(_localizedNotifications)

    init {
        fetchPosts()
        fetchStories()
        fetchConversations()
        fetchNotifications()
    }

    private fun devOptions(): DevOptions {
        val app = ApplicationProvider.app ?: return DevOptions()
        return DevOptionsSettings.get(app)
    }

    // Mock content moderation
    private fun performContentModeration(content: String): ModerationStatus {
        val lowerCaseContent = content.lowercase()
        val flaggedKeywords = listOf("scam", "phishing", "hate", "link", "spam", "shit", "damn")
        val blockedKeywords = listOf("kill", "harm", "abuse", "underage", "threat", "illegal", "criminal")

        return when {
            blockedKeywords.any { lowerCaseContent.contains(it) } -> ModerationStatus.BLOCKED
            flaggedKeywords.any { lowerCaseContent.contains(it) } -> ModerationStatus.FLAGGED
            else -> ModerationStatus.CLEAN
        }
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
        _uiState.update { it.copy(errorMessage = "Message $msgId in conversation $convId retry logic not implemented (mock).") }
    }

    fun reportMessage(messageId: String) {
        _uiState.update { it.copy(errorMessage = "Message $messageId reported (mock).") }
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

    fun isUserBlocked(userId: String): Boolean {
        // Mock logic: assume user is not blocked unless they are Dino and we banned Dino in our mock state
        return userId == DINO_USER_ID && _uiState.value.isDinoBanned
    }

    fun isUserMuted(userId: String): Boolean {
        // Mock logic: no users are muted in this mock implementation
        return false
    }

    fun blockUser(userId: String) {
        // Mock logic
        _uiState.update { it.copy(errorMessage = "$userId blocked (mock).") }
        banUser(userId)
    }

    fun unblockUser(userId: String) {
        // Mock logic
        _uiState.update { it.copy(errorMessage = "$userId unblocked (mock).") }
        if (userId == DINO_USER_ID) {
            _uiState.update { it.copy(isDinoBanned = false) }
        }
    }

    fun muteUser(userId: String) {
        _uiState.update { it.copy(errorMessage = "$userId muted (mock).") }
    }

    fun unmuteUser(userId: String) {
        _uiState.update { it.copy(errorMessage = "$userId unmuted (mock).") }
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
                    _uiState.update { it.copy(isPremium = if (it.isFakePremiumEnabled) true else false) }
                }
            }
        } else {
            // Debug builds or setting to false
            realPremiumStatus = isPremium
            _uiState.update { it.copy(isPremium = if (it.isFakePremiumEnabled) true else isPremium) }
        }
    }

    fun toggleFakePremium(enabled: Boolean) {
        // SECURITY: Only allow fake premium in debug builds
        if (!BuildConfig.DEBUG && enabled) {
            android.util.Log.e("FeedViewModel", "🚨 SECURITY: Fake premium blocked in release build")
            throw SecurityException("Debug features are not available in production builds.")
        }

        _uiState.update {
            it.copy(
                isFakePremiumEnabled = enabled,
                isPremium = if (enabled) true else realPremiumStatus
            )
        }
    }

    // --- Feed toggles ---

    fun toggleStories(enabled: Boolean) {
        _uiState.update { it.copy(showStories = enabled) }
    }

    fun toggleVideoAutoplay(enabled: Boolean) {
        _uiState.update { it.copy(isVideoAutoplayEnabled = enabled) }
    }

    fun toggleMockInterface(enabled: Boolean) {
        _uiState.update { it.copy(isMockInterfaceEnabled = enabled) }
        fetchPosts()
    }

    fun toggleFallbackUi(enabled: Boolean) {
        _uiState.update { it.copy(isFallbackUiEnabled = enabled) }
    }

    // --- Data loading ---

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // No artificial delay - instant loading

            if (simulateError) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Simulated server error (HTTP 500)") }
                return@launch
            }
            if (simulateInfiniteLoading) {
                // keep loading forever
                return@launch
            }

            // Combine localized explore posts with localized feed posts
            // All posts are now localized for proper language support
            val combinedPosts = _localizedPosts + _localizedFeedPosts
            _uiState.update { it.copy(posts = combinedPosts, isLoading = false) }
        }
    }

    /**
     * Resets all mock data to initial state for testing purposes.
     * - Resets all post likes to their original values
     * - Resets DM reactions
     * - Clears user-created content
     * - Resets strike counts and bans
     */
    fun resetMockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // No artificial delay

            // Reset posts to original mock data (fresh copies with original like counts)
            // All posts are localized
            val freshPosts = (_localizedPosts + _localizedFeedPosts).map { it.copy() }

            // Reset conversations to original mock data (fresh copies with empty reactions)
            val freshConversations = _localizedConversations.map { conv ->
                conv.copy(
                    messages = conv.messages.map { msg ->
                        msg.copy(reactions = emptyList(), isRead = false)
                    },
                    unreadCount = conv.messages.count { it.senderId != "me" }
                )
            }
            _userConversations.value = freshConversations

            // Reset strike counts
            _userStrikeCount.value = emptyMap()

            // Reset user-created stories
            _userStories.value = emptyList()

            // Update UI state
            _uiState.update {
                it.copy(
                    posts = freshPosts,
                    conversations = freshConversations,
                    activeConversation = null,
                    isDinoBanned = false,
                    isLoading = false,
                    errorMessage = "Mock data has been reset!"
                )
            }

            // Refresh stories
            fetchStories()
        }
    }

    private fun fetchStories() {
        val allStories = MOCK_STORIES + _userStories.value
        _uiState.update { it.copy(stories = allStories) }
    }

    // --- Post actions ---

    fun createPost(content: String, tone: String, imageUrl: String? = null, videoUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // No artificial delay
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
                videoUrl = videoUrl
            )
            _uiState.update { it.copy(posts = listOf(newPost) + it.posts, isLoading = false) }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // No artificial delay
            _uiState.update { it.copy(posts = it.posts.filter { p -> p.id != postId }, isLoading = false) }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // No artificial delay
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

    // --- Story actions ---

    fun viewStory(story: Story) {
        _uiState.update { it.copy(activeStory = story) }
    }

    fun dismissStory() {
        _uiState.update { it.copy(activeStory = null) }
    }

    fun markStoryAsViewed(storyId: String) {
        _uiState.update { state ->
            val updatedStories = state.stories.map { story ->
                if (story.id == storyId) story.copy(isViewed = true) else story
            }
            state.copy(stories = updatedStories)
        }
    }

    fun createStory(imageUrl: String, duration: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // No artificial delay

            val newStory = Story(
                id = "s-${System.currentTimeMillis()}",
                userName = CURRENT_USER_ID_MOCK,
                userAvatar = CURRENT_USER.avatarUrl,
                items = listOf(StoryItem("si-${System.currentTimeMillis()}", imageUrl, duration))
            )
            _userStories.update { listOf(newStory) + it }
            fetchStories()
            _uiState.update { it.copy(isLoading = false) }
        }
    }


    fun toggleLike(postId: Long) {
        // Update UI immediately for responsive feel
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

        // Persist to Supabase in background
        viewModelScope.launch {
            val result = LikesRepository.toggleLike(postId, CURRENT_USER_ID_MOCK)
            result.onSuccess { isNowLiked ->
                android.util.Log.d("NeuroComet", "✅ Like persisted to Supabase: Post #$postId | Liked: $isNowLiked")
            }.onFailure { error ->
                android.util.Log.w("NeuroComet", "⚠️ Like not persisted (local only): ${error.message}")
                // Like still works locally, just not synced to server
            }
        }
    }

    // --- Comments bottom sheet ---

    fun openCommentSheet(post: Post) {
        _uiState.update {
            it.copy(
                activePostId = post.id,
                activePostComments = emptyList(),
                isCommentSheetVisible = true
            )
        }
    }

    fun dismissCommentSheet() {
        _uiState.update { it.copy(isCommentSheetVisible = false, activePostId = null) }
    }

    fun addComment(content: String) {
        val postId = _uiState.value.activePostId ?: return
        val newComment = Comment(
            id = "c-${System.currentTimeMillis()}",
            postId = postId,
            userId = CURRENT_USER_ID_MOCK,
            userAvatar = CURRENT_USER.avatarUrl,
            content = content,
            timestamp = Instant.now().toString()
        )

        // Add comment to the active post comments list
        _uiState.update { state ->
            // Also update the comment count on the post
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + 1)
                } else post
            }
            state.copy(
                activePostComments = state.activePostComments + newComment,
                posts = updatedPosts
            )
        }
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
        val shareText = buildString {
            append(post.content)
            if (!post.userId.isNullOrEmpty()) {
                append("\n\n— ${post.userId} on NeuroComet")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
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
            // No artificial delay
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
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
        _uiState.update { it.copy(notifications = _notifications.value) }
    }

    fun dismissNotification(notificationId: String) {
        _notifications.update { list ->
            list.filter { it.id != notificationId }
        }
        _uiState.update { it.copy(notifications = _notifications.value) }
    }

    // Legacy functions for backward compatibility
    fun markNotificationRead(notificationId: String) = markNotificationAsRead(notificationId)
    fun markAllNotificationsRead() = markAllNotificationsAsRead()
    fun deleteNotification(notificationId: String) = dismissNotification(notificationId)

    fun addNotification(notification: NotificationItem) {
        _notifications.update { listOf(notification) + it }
        _uiState.update { it.copy(notifications = _notifications.value) }
    }
}

val MOCK_STORIES = listOf(
    Story(
        id = "1",
        userName = "NeuroTips",
        userAvatar = "https://i.pravatar.cc/150?u=neurotips",
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
        items = listOf(
            StoryItem("s2i1", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080")
        )
    ),
    Story(
        id = "3",
        userName = "ADHDWins",
        userAvatar = "https://i.pravatar.cc/150?u=adhdwins",
        items = listOf(
            StoryItem("s3i1", "https://images.unsplash.com/photo-1552581234-26160f608093?w=1080"),
            StoryItem("s3i2", "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1080")
        )
    ),
    Story(
        id = "4",
        userName = "CalmCorner",
        userAvatar = "https://i.pravatar.cc/150?u=calmcorner",
        items = listOf(
            StoryItem("s4i1", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=1080")
        )
    ),
    Story(
        id = "5",
        userName = "FocusFlow",
        userAvatar = "https://i.pravatar.cc/150?u=focusflow",
        items = listOf(
            StoryItem("s5i1", "https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=1080"),
            StoryItem("s5i2", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=1080")
        )
    ),
    Story(
        id = "6",
        userName = "SensoryJoy",
        userAvatar = "https://i.pravatar.cc/150?u=sensoryjoy",
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
    ),
    Conversation(
        id = "conv6",
        participants = listOf(CURRENT_USER_ID_MOCK, "CalmObserver"),
        messages = listOf(
            DirectMessage("msg19", "CalmObserver", CURRENT_USER_ID_MOCK, "Hi there! Wanted to share a grounding technique that helped me today.", Instant.now().minusSeconds(345600).toString()),
            DirectMessage("msg20", CURRENT_USER_ID_MOCK, "CalmObserver", "I'm all ears! Been having a rough week with anxiety.", Instant.now().minusSeconds(344000).toString()),
            DirectMessage("msg21", "CalmObserver", CURRENT_USER_ID_MOCK, "Try the 5-4-3-2-1 method: 5 things you see, 4 you hear, 3 you touch, 2 you smell, 1 you taste.", Instant.now().minusSeconds(343000).toString()),
            DirectMessage("msg22", CURRENT_USER_ID_MOCK, "CalmObserver", "This is really helpful, thank you! 💙", Instant.now().minusSeconds(342000).toString()),
            DirectMessage("msg23", "CalmObserver", CURRENT_USER_ID_MOCK, "Anytime! We're all in this together. 🤗", Instant.now().minusSeconds(341000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(341000).toString(),
        unreadCount = 1
    ),
    Conversation(
        id = "conv7",
        participants = listOf(CURRENT_USER_ID_MOCK, "Alex_Stims"),
        messages = listOf(
            DirectMessage("msg24", "Alex_Stims", CURRENT_USER_ID_MOCK, "Just got the new mechanical keyboard! The clicks are SO satisfying 🎹", Instant.now().minusSeconds(432000).toString()),
            DirectMessage("msg25", CURRENT_USER_ID_MOCK, "Alex_Stims", "Ooooh which switches did you get?", Instant.now().minusSeconds(431000).toString()),
            DirectMessage("msg26", "Alex_Stims", CURRENT_USER_ID_MOCK, "Cherry MX Blues! The tactile feedback is *chef's kiss* for my stimming needs.", Instant.now().minusSeconds(430000).toString()),
            DirectMessage("msg27", CURRENT_USER_ID_MOCK, "Alex_Stims", "I've been eyeing those! Do they help you focus while coding?", Instant.now().minusSeconds(429000).toString()),
            DirectMessage("msg28", "Alex_Stims", CURRENT_USER_ID_MOCK, "Absolutely! The rhythmic clicking is like a built-in focus soundtrack. Highly recommend!", Instant.now().minusSeconds(428000).toString()),
            DirectMessage("msg29", CURRENT_USER_ID_MOCK, "Alex_Stims", "Adding to cart right now 😂", Instant.now().minusSeconds(427000).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(427000).toString(),
        unreadCount = 0
    ),
    Conversation(
        id = "conv8",
        participants = listOf(CURRENT_USER_ID_MOCK, "SpoonCounter"),
        messages = listOf(
            DirectMessage("msg30", "SpoonCounter", CURRENT_USER_ID_MOCK, "Low spoon day today 😴 How are you managing?", Instant.now().minusSeconds(14400).toString()),
            DirectMessage("msg31", CURRENT_USER_ID_MOCK, "SpoonCounter", "Same here. Decided to cancel all non-essential plans.", Instant.now().minusSeconds(13800).toString()),
            DirectMessage("msg32", "SpoonCounter", CURRENT_USER_ID_MOCK, "That's smart! Self-care isn't selfish. Rest up! 💜", Instant.now().minusSeconds(13200).toString())
        ),
        lastMessageTimestamp = Instant.now().minusSeconds(13200).toString(),
        unreadCount = 1
    )
)

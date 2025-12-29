@file:Suppress(
    "unused",
    "RedundantSuppression",
    "UNUSED_PARAMETER",
    "SimplifyBooleanWithConstants",
    "KotlinConstantConditions",
    "MemberVisibilityCanBePrivate"
)

package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

// Required for Mock Data
private const val CURRENT_USER_ID_MOCK = "me"
private const val DINO_USER_ID = "DinoLover99"
private const val THERAPY_USER_ID = "Therapy_Bot"

// --- HIGH QUALITY MOCK DATA ---
val MOCK_FEED_POSTS = listOf(
    Post(
        id = 1L,
        createdAt = Instant.now().toString(),
        content = "My new weighted blanket arrived. 10/10 would recommend for anxiety.",
        userId = DINO_USER_ID,
        likes = 124,
        comments = 12,
        shares = 5,
        isLikedByMe = false,
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=DinoLover99"
    ),
    Post(
        id = 2L,
        createdAt = Instant.now().toString(),
        content = "Hyperfocused on this new Android project. Reminder to hydrate!",
        userId = "CodeWitch",
        likes = 89,
        comments = 12,
        shares = 5,
        isLikedByMe = false,
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=CodeWitch"
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
    val isDinoBanned: Boolean = false
)

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var realPremiumStatus = false

    var simulateError = false
    var simulateInfiniteLoading = false

    private val _userStories = MutableStateFlow<List<Story>>(emptyList())
    private val _userConversations = MutableStateFlow<List<Conversation>>(MOCK_CONVERSATIONS)

    // Mock strike counter
    private val _userStrikeCount = MutableStateFlow<Map<String, Int>>(emptyMap())

    init {
        fetchPosts()
        fetchStories()
        fetchConversations()
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
            delay(300.milliseconds)

            val combinedConversations = MOCK_CONVERSATIONS.map { staticConv ->
                _userConversations.value.find { it.id == staticConv.id } ?: staticConv
            }.plus(_userConversations.value.filter { conv -> MOCK_CONVERSATIONS.none { it.id == conv.id } })

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

    // --- Premium ---

    fun setPremiumStatus(isPremium: Boolean) {
        realPremiumStatus = isPremium
        _uiState.update { it.copy(isPremium = if (it.isFakePremiumEnabled) true else isPremium) }
    }

    fun toggleFakePremium(enabled: Boolean) {
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
            delay(500.milliseconds)

            if (simulateError) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Simulated server error (HTTP 500)") }
                return@launch
            }
            if (simulateInfiniteLoading) {
                // keep loading forever
                return@launch
            }

            _uiState.update { it.copy(posts = MOCK_FEED_POSTS, isLoading = false) }
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
            delay(300.milliseconds)
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
            delay(200.milliseconds)
            _uiState.update { it.copy(posts = it.posts.filter { p -> p.id != postId }, isLoading = false) }
        }
    }

    fun toggleLike(postId: Long) {
        _uiState.update { currentState ->
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val nowLiked = !post.isLikedByMe
                    post.copy(
                        isLikedByMe = nowLiked,
                        likes = (post.likes + if (nowLiked) 1 else -1).coerceAtLeast(0)
                    )
                } else post
            }
            currentState.copy(posts = updatedPosts)
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
        _uiState.update { it.copy(activePostComments = it.activePostComments + newComment) }
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
        // Placeholder: real sharing would use ACTION_SEND intent.
        _uiState.update { it.copy(errorMessage = "Share is not wired up in this mock build.") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

val MOCK_STORIES = listOf(
    Story(
        id = "1",
        userName = "Therapy_Bot",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Therapy_Bot",
        items = listOf(
            StoryItem("s1i1", "https://picsum.photos/seed/s1i1/1080/1920"),
            StoryItem("s1i2", "https://picsum.photos/seed/s1i2/1080/1920"),
            StoryItem("s1i3", "https://picsum.photos/seed/s1i3/1080/1920")
        )
    ),
    Story(
        id = "2",
        userName = "Alex",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex",
        items = listOf(
            StoryItem("s2i1", "https://picsum.photos/seed/s2i1/1080/1920")
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
    )
)
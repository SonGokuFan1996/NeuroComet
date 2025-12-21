package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Placeholder user ID for posts created within the app, as full auth isn't integrated yet.
private const val CURRENT_USER_ID_MOCK = "bdd5700a-1157-4581-b547-063a563fc854"

// --- HIGH QUALITY MOCK DATA ---
val MOCK_FEED_POSTS = listOf(
    Post(
        id = 1,
        userId = "DinoLover99",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=DinoLover99",
        content = "My new weighted blanket arrived and I have ascended to a higher plane of existence. 10/10 would recommend for anxiety.",
        tone = "/srs",
        likes = 124,
        community = "r/SensoryTools",
        createdAt = "10m ago"
    ),
    Post(
        id = 2,
        userId = "CodeWitch",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=CodeWitch",
        content = "Hyperfocused on this new Android project for 6 hours straight. Forgot to drink water. Reminder to hydrate!",
        tone = "/lh",
        likes = 89,
        community = "r/ADHDProgrammers",
        createdAt = "1h ago"
    ),
    Post(
        id = 3,
        userId = "ArtisticSoul",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=ArtisticSoul",
        content = "Look at this texture study I did today! The colors are so soothing.",
        tone = "/gen",
        likes = 452,
        imageUrl = "https://picsum.photos/seed/artistic/500/400",
        community = "r/ArtTherapy",
        createdAt = "3h ago"
    ),
    Post(
        id = 4,
        userId = "ForestWalker",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=ForestWalker",
        content = "Found a really quiet spot in the park. No cars, just birds. Perfect for decompressing after a meltdown.",
        tone = "/pos",
        likes = 210,
        community = "r/SafePlaces",
        createdAt = "5h ago"
    ),
    Post(
        id = 5,
        userId = "RetroGamer",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=RetroGamer",
        content = "Anyone else use video game soundtracks for focus? Stardew Valley OST is saving my life right now.",
        tone = "/gen",
        likes = 333,
        community = "r/MusicForFocus",
        createdAt = "1d ago"
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
    val isFakePremiumEnabled: Boolean = false,
    val activePostId: Long? = null,
    val activePostComments: List<Comment> = emptyList(),
    val isCommentSheetVisible: Boolean = false
)

class FeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // --- Developer Mode Flags ---
    var simulateError = false
    var simulateInfiniteLoading = false

    init {
        fetchPosts()
        fetchStories()
    }

    // Call this when RevenueCat confirms a purchase or restores entitlements
    fun setPremiumStatus(isPremium: Boolean) {
        // Only set the real premium status if the fake toggle is OFF
        if (!_uiState.value.isFakePremiumEnabled) {
            _uiState.update { it.copy(isPremium = isPremium) }
        }
    }

    fun toggleFakePremium(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isFakePremiumEnabled = enabled,
                isPremium = enabled // Immediately apply the fake status
            ) 
        }
    }

    fun toggleStories(enabled: Boolean) {
        _uiState.update { it.copy(showStories = enabled) }
    }

    fun toggleVideoAutoplay(enabled: Boolean) {
        _uiState.update { it.copy(isVideoAutoplayEnabled = enabled) }
    }

    fun toggleMockInterface(enabled: Boolean) {
        _uiState.update { it.copy(isMockInterfaceEnabled = enabled) }
        // Refetch posts to apply the toggle immediately
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // If Mock Interface is enabled, return fake posts instantly
            if (_uiState.value.isMockInterfaceEnabled) {
                delay(500) // Small delay for realism
                _uiState.update {
                    it.copy(posts = MOCK_FEED_POSTS, isLoading = false)
                }
                return@launch
            }

            if (simulateInfiniteLoading) return@launch

            if (simulateError) {
                delay(1000)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Simulated Error: 500 Server Error") }
                return@launch
            }

            try {
                Log.d("NeuroNetDB", "Fetching posts...")
                val fetchedPosts = SupabaseClient.client
                    .from("posts")
                    .select {
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<Post>()

                _uiState.update {
                    it.copy(posts = fetchedPosts, isLoading = false)
                }
                Log.d("NeuroNetDB", "Success! Fetched ${fetchedPosts.size} posts.")

            } catch (e: Exception) {
                Log.e("NeuroNetDB", "Error fetching posts", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load feed: ${e.message}")
                }
            }
        }
    }

    private fun fetchStories() {
        // Mock stories for now
        val mockStories = listOf(
            Story("1", "Therapy_Bot", THERAPY_BOT_AVATAR, "https://picsum.photos/400/600"),
            Story("2", "Alex", "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex", "https://picsum.photos/401/600"),
            Story("3", "Sam", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sam", "https://picsum.photos/402/600")
        )
        _uiState.update { it.copy(stories = mockStories) }
    }

    fun createPost(content: String, tone: String, imageUrl: String? = null, videoUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Handle creation in Mock Mode
            if (_uiState.value.isMockInterfaceEnabled) {
                delay(500)
                val newPost = Post(
                    id = System.currentTimeMillis(),
                    userId = "Me",
                    userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Me",
                    content = content,
                    tone = tone,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    community = "r/MyProfile",
                    likes = 0,
                    createdAt = "Just now"
                )
                // Prepend new post to the local list
                _uiState.update { 
                    it.copy(posts = listOf(newPost) + it.posts, isLoading = false) 
                }
                return@launch
            }

            try {
                Log.d("NeuroNetDB", "Creating post...")
                val newPost = Post(
                    content = content,
                    community = "r/General",
                    tone = tone,
                    userId = CURRENT_USER_ID_MOCK, // Using constant
                    likes = 0,
                    imageUrl = if (imageUrl?.isNotBlank() == true) imageUrl else null,
                    videoUrl = if (videoUrl?.isNotBlank() == true) videoUrl else null
                )

                SupabaseClient.client.from("posts").insert(newPost)
                Log.d("NeuroNetDB", "Post created successfully!")

                fetchPosts()

            } catch (e: Exception) {
                Log.e("NeuroNetDB", "Error creating post", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to post: ${e.message}")
                }
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Handle deletion in Mock Mode
            if (_uiState.value.isMockInterfaceEnabled) {
                delay(300)
                _uiState.update { 
                    it.copy(posts = it.posts.filter { p -> p.id != postId }, isLoading = false) 
                }
                return@launch
            }

            try {
                Log.d("NeuroNetDB", "Deleting post $postId...")

                SupabaseClient.client
                    .from("posts")
                    .delete {
                        filter {
                            eq("id", postId)
                        }
                    }

                Log.d("NeuroNetDB", "Post deleted successfully!")
                fetchPosts()

            } catch (e: Exception) {
                Log.e("NeuroNetDB", "Error deleting post", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to delete: ${e.message}")
                }
            }
        }
    }

    // --- LIKE FUNCTIONALITY ---
    fun toggleLike(postId: Long) {
        // Optimistic UI update
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    val isLiked = !post.isLikedByMe
                    val newLikes = if (isLiked) post.likes + 1 else (post.likes - 1).coerceAtLeast(0)
                    post.copy(isLikedByMe = isLiked, likes = newLikes)
                } else {
                    post
                }
            }
            state.copy(posts = updatedPosts)
        }

        // Ideally, fire and forget network request here
        // if (!state.isMockInterfaceEnabled) { ... }
    }

    // --- COMMENT FUNCTIONALITY ---
    fun openCommentSheet(post: Post) {
        _uiState.update { 
            it.copy(
                isCommentSheetVisible = true, 
                activePostId = post.id,
                activePostComments = emptyList() // Reset while loading
            ) 
        }
        
        // Mock fetching comments
        viewModelScope.launch {
            delay(500)
            if (_uiState.value.activePostId == post.id) {
                val mockComments = listOf(
                    Comment(1, post.id ?: 0, "UserA", "Totally agree!", "10m ago", "https://api.dicebear.com/7.x/avataaars/svg?seed=UserA"),
                    Comment(2, post.id ?: 0, "UserB", "This helps so much.", "1h ago", "https://api.dicebear.com/7.x/avataaars/svg?seed=UserB")
                )
                _uiState.update { it.copy(activePostComments = mockComments) }
            }
        }
    }

    fun dismissCommentSheet() {
        _uiState.update { it.copy(isCommentSheetVisible = false, activePostId = null) }
    }

    fun addComment(content: String) {
        val postId = _uiState.value.activePostId ?: return
        val newComment = Comment(
            id = System.currentTimeMillis(),
            postId = postId,
            userId = "Me",
            content = content,
            createdAt = "Just now",
            userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Me"
        )
        
        _uiState.update { 
            it.copy(activePostComments = it.activePostComments + newComment) 
        }
        
        // In real app, send to backend here
    }

    // --- SHARE FUNCTIONALITY ---
    fun sharePost(context: Context, post: Post) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this post on NeuroNet:\n\n${post.content}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        if (context !is android.app.Activity) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun floodDb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dummyPosts = (1..5).map { i ->
                    Post(
                        content = "Flood Post #$i: Testing database list performance.",
                        community = "r/DevTest",
                        tone = "/test",
                        userId = CURRENT_USER_ID_MOCK, // Using constant
                        likes = (0..100).random()
                    )
                }
                SupabaseClient.client.from("posts").insert(dummyPosts)
                fetchPosts()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Flood failed: ${e.message}") }
            }
        }
    }

    fun stressTestDb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Batch insert 50 posts
                val dummyPosts = (1..50).map { i ->
                    Post(
                        content = "STRESS TEST POST #$i: Loading check...",
                        community = "r/StressTest",
                        tone = "/stress",
                        userId = CURRENT_USER_ID_MOCK,
                        likes = 0
                    )
                }
                SupabaseClient.client.from("posts").insert(dummyPosts)
                fetchPosts()
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Stress Test failed: ${e.message}") }
            }
        }
    }

    fun nukeDb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                SupabaseClient.client.from("posts").delete {
                    filter {
                        gt("id", 0)
                    }
                }
                fetchPosts()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Nuke failed: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

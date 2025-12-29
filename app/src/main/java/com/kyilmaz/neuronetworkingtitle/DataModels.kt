package com.kyilmaz.neuronetworkingtitle

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

@Serializable
data class Post(
    val id: Long?,
    val createdAt: String,
    val content: String,
    val userId: String?,
    var likes: Int,
    val comments: Int,
    val shares: Int,
    var isLikedByMe: Boolean,
    val userAvatar: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null // Added for full media support
) {
    val timeAgo: String
        get() {
            val now = Instant.now()
            val postTime = Instant.parse(createdAt)
            val diff = Duration.between(postTime, now)

            return when {
                diff.toDays() > 0 -> "${diff.toDays()}d ago"
                diff.toHours() > 0 -> "${diff.toHours()}h ago"
                diff.toMinutes() > 0 -> "${diff.toMinutes()}m ago"
                else -> "Just now"
            }
        }
}

@Serializable
data class Comment(
    val id: String,
    val postId: Long,
    val userId: String,
    val userAvatar: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isVerified: Boolean,
    val personality: String,
    val isBanned: Boolean = false
)

val CURRENT_USER = User(
    id = "me", // Matches CURRENT_USER_ID_MOCK in FeedViewModel
    name = "You",
    avatarUrl = "https://api.dicebear.com/7.x/adventurer/svg?seed=Me",
    isVerified = true,
    personality = "The current user."
)

val MOCK_USERS = listOf(
    CURRENT_USER, // Include the current user in the mock list
    User(
        id = "NeuroNaut",
        name = "NeuroNaut",
        avatarUrl = "https://api.dicebear.com/7.x/adventurer/svg?seed=NeuroNaut",
        isVerified = true,
        personality = "A seasoned explorer of the neurodivergent community, offering deep, empathetic insights."
    ),
    User(
        id = "HyperFocusCode",
        name = "H.F. Code",
        avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=Code",
        isVerified = true,
        personality = "A developer known for intense hyperfocus sessions and sharing productivity hacks."
    ),
    User(
        id = "SensorySeeker",
        name = "SensorySeeker",
        avatarUrl = "https://api.dicebear.com/7.x/openpeeps/svg?seed=Seeker",
        isVerified = false,
        personality = "A creative artist who experiences the world intensely and shares sensory-friendly tips."
    ),
    User(
        id = "CalmObserver",
        name = "CalmObserver",
        avatarUrl = "https://api.dicebear.com/7.x/miniavs/svg?seed=Calm",
        isVerified = false,
        personality = "Focuses on mindfulness and low-stimulation techniques to manage overwhelm and anxiety."
    ),
    User(
        id = "DinoLover99",
        name = "DinoLover99",
        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=DinoLover99",
        isVerified = true,
        personality = "Special interest in paleontology and a verified user who posts thoughtful, detailed content."
    ),
    User(
        id = "Alex_Stims",
        name = "Alex_Stims",
        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex_Stims",
        isVerified = false,
        personality = "Likes mechanical keyboards and shares their favorite stim toys and routines."
    )
)

// Represents one "page" or item within a user's story
@Serializable
data class StoryItem(
    val id: String,
    val imageUrl: String,
    val duration: Long = 5000L // Duration in milliseconds
)

// Represents the collection of stories for a single user
@Serializable
data class Story(
    val id: String, // Should be unique per user story collection
    val userAvatar: String,
    val userName: String,
    val items: List<StoryItem>,
    var isViewed: Boolean = false
)

@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val isEarned: Boolean
)

val MOCK_BADGES = listOf(
    Badge("1", "Verified Human", "Completed the humanity test.", "https://api.dicebear.com/7.x/notionists/svg?seed=Verified", true),
    Badge("2", "First Post", "Shared your first thought with the network.", "https://api.dicebear.com/7.x/notionists/svg?seed=Post", true),
    Badge("3", "HyperFocus Master", "Posted 10 times in HyperFocus state.", "https://api.dicebear.com/7.x/notionists/svg?seed=Focus", false),
    Badge("4", "Community Pillar", "Received 50 likes on one post.", "https://api.dicebear.com/7.x/notionists/svg?seed=Pillar", false),
    Badge("5", "Quiet Achiever", "Used Quiet Mode for 7 consecutive days.", "https://api.dicebear.com/7.x/notionists/svg?seed=Quiet", true)
)


@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType
)

@Serializable
enum class NotificationType {
    LIKE,
    COMMENT,
    SYSTEM
}

// --- DM FUNCTIONALITY --- 
@Serializable
enum class ModerationStatus {
    CLEAN, // No issues detected
    FLAGGED, // Contains potentially inappropriate content, review needed
    BLOCKED // Contains clearly inappropriate/criminal content, blocked from sending/viewing
}

@Serializable
enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    FAILED
}

@Serializable
data class DirectMessage(
    val id: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val timestamp: String,
    val moderationStatus: ModerationStatus = ModerationStatus.CLEAN,
    var isRead: Boolean = false,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.SENT
)

@Serializable
data class Conversation(
    val id: String,
    val participants: List<String>, // User IDs involved in the conversation
    val messages: List<DirectMessage>,
    val lastMessageTimestamp: String,
    var unreadCount: Int = 0
)

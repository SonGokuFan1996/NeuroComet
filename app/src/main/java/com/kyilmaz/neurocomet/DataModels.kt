package com.kyilmaz.neurocomet

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

// Prefer a stable avatar CDN over pravatar which intermittently fails
fun avatarUrl(seed: String): String = "https://api.dicebear.com/7.x/adventurer/png?seed=${seed}&size=256&radius=50&backgroundColor=dae5ff"

/**
 * Represents a single media item (image or video) in a post.
 * Supports up to 20 media items per post like Instagram.
 */
@Serializable
data class MediaItem(
    val url: String,
    val type: MediaType,
    val thumbnailUrl: String? = null, // For video thumbnails
    val aspectRatio: Float = 1f, // Width / Height ratio
    val altText: String? = null // Accessibility description
)

@Serializable
enum class MediaType {
    IMAGE,
    VIDEO
}

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
    val imageUrl: String? = null, // Legacy single image support
    val videoUrl: String? = null, // Legacy single video support
    val mediaItems: List<MediaItem> = emptyList(), // New multi-media support (max 20)
    val minAudience: Audience = Audience.UNDER_13
) {
    val timeAgo: String
        get() {
            val now = Instant.now()
            val postTime = Instant.parse(createdAt)
            val diff = Duration.between(postTime, now)

            return when {
                diff.toDays() >= 365 -> "${diff.toDays() / 365}y"
                diff.toDays() >= 30 -> "${diff.toDays() / 30}mo"
                diff.toDays() >= 7 -> "${diff.toDays() / 7}w"
                diff.toDays() > 0 -> "${diff.toDays()}d"
                diff.toHours() > 0 -> "${diff.toHours()}h"
                diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
                else -> "now"
            }
        }

    /**
     * Get all media items for this post, combining legacy single media with new multi-media.
     * Returns up to MAX_MEDIA_ITEMS (20) items.
     */
    fun getAllMedia(): List<MediaItem> {
        if (mediaItems.isNotEmpty()) return mediaItems.take(MAX_MEDIA_ITEMS)

        // Convert legacy single media to MediaItem list
        val legacyMedia = mutableListOf<MediaItem>()
        imageUrl?.let { legacyMedia.add(MediaItem(it, MediaType.IMAGE)) }
        videoUrl?.let { legacyMedia.add(MediaItem(it, MediaType.VIDEO)) }
        return legacyMedia
    }

    /**
     * Check if this post has any media
     */
    fun hasMedia(): Boolean = mediaItems.isNotEmpty() || imageUrl != null || videoUrl != null

    /**
     * Get the media count
     */
    fun mediaCount(): Int = getAllMedia().size

    companion object {
        const val MAX_MEDIA_ITEMS = 20
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
    avatarUrl = avatarUrl("me"),
    isVerified = true,
    personality = "The current user."
)

val MOCK_USERS = listOf(
    CURRENT_USER, // Include the current user in the mock list
    User(
        id = "NeuroNaut",
        name = "NeuroNaut",
        avatarUrl = avatarUrl("neuronaut"),
        isVerified = true,
        personality = "A seasoned explorer of the neurodivergent community, offering deep, empathetic insights."
    ),
    User(
        id = "HyperFocusCode",
        name = "H.F. Code",
        avatarUrl = avatarUrl("hyperfocuscode"),
        isVerified = true,
        personality = "A developer known for intense hyperfocus sessions and sharing productivity hacks."
    ),
    User(
        id = "SensorySeeker",
        name = "SensorySeeker",
        avatarUrl = avatarUrl("sensoryseeker"),
        isVerified = false,
        personality = "A creative artist who experiences the world intensely and shares sensory-friendly tips."
    ),
    User(
        id = "CalmObserver",
        name = "CalmObserver",
        avatarUrl = avatarUrl("calmobserver"),
        isVerified = false,
        personality = "Focuses on mindfulness and low-stimulation techniques to manage overwhelm and anxiety."
    ),
    User(
        id = "DinoLover99",
        name = "DinoLover99",
        avatarUrl = avatarUrl("dinolover99"),
        isVerified = true,
        personality = "Special interest in paleontology and a verified user who posts thoughtful, detailed content."
    ),
    User(
        id = "Alex_Stims",
        name = "Alex_Stims",
        avatarUrl = avatarUrl("alexstims"),
        isVerified = false,
        personality = "Likes mechanical keyboards and shares their favorite stim toys and routines."
    ),
    User(
        id = "SpoonCounter",
        name = "SpoonCounter",
        avatarUrl = avatarUrl("spooncounter"),
        isVerified = false,
        personality = "Advocates for chronic illness awareness and practices energy management using spoon theory."
    ),
    // LGBTQ+ Community Members
    User(
        id = "RainbowNerd",
        name = "RainbowNerd",
        avatarUrl = avatarUrl("rainbownerd"),
        isVerified = true,
        personality = "Proudly queer and autistic! Shares memes, resources, and celebrates the intersection of LGBTQ+ and neurodivergent identities. 🏳️‍🌈♾️"
    ),
    User(
        id = "TransTechie",
        name = "TransTechie",
        avatarUrl = avatarUrl("transtechie"),
        isVerified = true,
        personality = "Trans woman in tech. ADHD warrior. Loves coding, cats, and creating accessible software. She/Her 🏳️‍⚧️"
    ),
    User(
        id = "NonBinaryNinja",
        name = "NonBinaryNinja",
        avatarUrl = avatarUrl("nonbinaryninja"),
        isVerified = false,
        personality = "Enby with AuDHD. Special interest: martial arts history. Uses stim toys between kata practice. They/Them 💜"
    ),
    User(
        id = "BiBookworm",
        name = "BiBookworm",
        avatarUrl = avatarUrl("bibookworm"),
        isVerified = false,
        personality = "Bisexual booklover with dyslexia. Audiobook enthusiast. Reviews queer YA novels and shares reading accessibility tips. 💗💜💙"
    ),
    User(
        id = "AceExplorer",
        name = "AceExplorer",
        avatarUrl = avatarUrl("aceexplorer"),
        isVerified = true,
        personality = "Asexual & autistic adventurer! Posts about solo travel, sensory-friendly destinations, and ace pride. 🖤🤍💜"
    ),
    User(
        id = "PanPride_Sam",
        name = "Pan Pride Sam",
        avatarUrl = avatarUrl("panpridesam"),
        isVerified = false,
        personality = "Pansexual, ADHD, and proud! Artist who creates neurodivergent-affirming LGBTQ+ art. Commissions open! 💖💛💙"
    ),
    User(
        id = "QueerCoder",
        name = "QueerCoder",
        avatarUrl = avatarUrl("queercoder"),
        isVerified = true,
        personality = "Queer software engineer building inclusive apps. Autistic. Hyperfocuses on accessibility. Open source advocate. 🌈💻"
    ),
    User(
        id = "LesbianLuna",
        name = "LesbianLuna",
        avatarUrl = avatarUrl("lesbianluna"),
        isVerified = false,
        personality = "Lesbian artist with autism. Creates cozy, sensory-friendly digital art. Cat mom x3. She/They 🧡🤍💗"
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
    Badge("1", "Verified Human", "Completed the humanity test.", "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2705.png", true),
    Badge("2", "First Post", "Shared your first thought with the network.", "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f4dd.png", true),
    Badge("3", "HyperFocus Master", "Posted 10 times in HyperFocus state.", "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f3af.png", false),
    Badge("4", "Community Pillar", "Received 50 likes on one post.", "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f3c6.png", false),
    Badge("5", "Quiet Achiever", "Used Quiet Mode for 7 consecutive days.", "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f54a.png", true)
)

// Suppressed: Will be used in Badges screen implementation
@Suppress("unused")
val MOCK_BADGES_EXPORTED = MOCK_BADGES


@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val actionUrl: String? = null, // Deep link for click action
    val relatedUserId: String? = null,
    val relatedPostId: Long? = null
)

@Serializable
enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    MENTION,
    REPOST,
    BADGE,
    SYSTEM,
    WELCOME,
    SAFETY_ALERT
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

     /**
 * Message reaction from a specific user.
 * Like WhatsApp/Telegram/iMessage - shows who reacted with what emoji.
 */
@Serializable
data class MessageReaction(
    val emoji: String,
    val userId: String,
    val timestamp: String = ""
)

@Serializable
data class DirectMessage(
    val id: String,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val timestamp: String,
    val moderationStatus: ModerationStatus = ModerationStatus.CLEAN,
    var isRead: Boolean = false,
    val deliveryStatus: MessageDeliveryStatus = MessageDeliveryStatus.SENT,
    val reactions: List<MessageReaction> = emptyList(), // User-specific reactions like modern messaging apps
    @Deprecated("Use reactions list instead")
    val legacyReactions: Map<String, Int> = emptyMap() // Legacy: emoji to count
) {
    /**
     * Get grouped reactions for display (emoji -> list of user IDs)
     */
    fun getGroupedReactions(): Map<String, List<String>> {
        return reactions.groupBy { it.emoji }.mapValues { entry -> entry.value.map { it.userId } }
    }

    /**
     * Check if current user has reacted with a specific emoji
     */
    fun hasUserReacted(userId: String, emoji: String): Boolean {
        return reactions.any { it.userId == userId && it.emoji == emoji }
    }

    /**
     * Get all unique emojis used as reactions
     */
    fun getUniqueEmojis(): List<String> {
        return reactions.map { it.emoji }.distinct()
    }
}

@Serializable
data class Conversation(
    val id: String,
    val participants: List<String>, // User IDs involved in the conversation
    val messages: List<DirectMessage>,
    val lastMessageTimestamp: String,
    var unreadCount: Int = 0
)

package com.kyilmaz.neurocomet

import android.content.Context
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Provides localized mock data for the app.
 *
 * This class retrieves translated strings for mock notifications, posts,
 * and other demo content based on the current app locale.
 */
object MockDataProvider {

    /**
     * Get localized mock notifications
     */
    fun getLocalizedNotifications(context: Context): List<NotificationItem> {
        val res = context.resources
        return listOf(
            NotificationItem(
                id = "1",
                title = res.getString(R.string.mock_notif_welcome_title),
                message = res.getString(R.string.mock_notif_welcome_msg),
                timestamp = res.getString(R.string.post_time_just_now),
                type = NotificationType.WELCOME,
                isRead = false
            ),
            NotificationItem(
                id = "2",
                title = res.getString(R.string.mock_notif_badge_title),
                message = res.getString(R.string.mock_notif_badge_msg),
                timestamp = res.getString(R.string.post_time_minutes_ago, 10),
                type = NotificationType.BADGE,
                isRead = false
            ),
            NotificationItem(
                id = "3",
                title = res.getString(R.string.mock_notif_like_title, "Alex_Stims"),
                message = res.getString(R.string.mock_notif_like_msg),
                timestamp = res.getString(R.string.post_time_hours_ago, 1),
                type = NotificationType.LIKE,
                isRead = false,
                avatarUrl = avatarUrl("alex_stims"),
                relatedUserId = "Alex_Stims"
            ),
            NotificationItem(
                id = "4",
                title = res.getString(R.string.mock_notif_follow_title),
                message = res.getString(R.string.mock_notif_follow_msg, "NeuroDiverse_Dan"),
                timestamp = res.getString(R.string.post_time_hours_ago, 2),
                type = NotificationType.FOLLOW,
                isRead = false,
                avatarUrl = avatarUrl("dan"),
                relatedUserId = "NeuroDiverse_Dan"
            ),
            NotificationItem(
                id = "5",
                title = res.getString(R.string.mock_notif_reply_title, "DinoLover99"),
                message = res.getString(R.string.mock_notif_reply_msg),
                timestamp = res.getString(R.string.post_time_hours_ago, 3),
                type = NotificationType.COMMENT,
                isRead = true,
                avatarUrl = avatarUrl("dinolover99"),
                relatedUserId = "DinoLover99"
            ),
            NotificationItem(
                id = "6",
                title = res.getString(R.string.mock_notif_mention_title),
                message = res.getString(R.string.mock_notif_mention_msg, "CalmObserver"),
                timestamp = res.getString(R.string.post_time_hours_ago, 5),
                type = NotificationType.MENTION,
                isRead = true,
                avatarUrl = avatarUrl("calmobserver"),
                relatedUserId = "CalmObserver"
            ),
            NotificationItem(
                id = "7",
                title = res.getString(R.string.mock_notif_repost_title),
                message = res.getString(R.string.mock_notif_repost_msg, "FocusFriend", 142),
                timestamp = res.getString(R.string.post_time_days_ago, 1),
                type = NotificationType.REPOST,
                isRead = true,
                avatarUrl = avatarUrl("focusfriend"),
                relatedUserId = "FocusFriend"
            ),
            NotificationItem(
                id = "8",
                title = res.getString(R.string.mock_notif_safety_title),
                message = res.getString(R.string.mock_notif_safety_msg),
                timestamp = res.getString(R.string.post_time_days_ago, 1),
                type = NotificationType.SYSTEM,
                isRead = true
            ),
            NotificationItem(
                id = "9",
                title = res.getString(R.string.mock_notif_filtered_title),
                message = res.getString(R.string.mock_notif_filtered_msg),
                timestamp = res.getString(R.string.post_time_days_ago, 2),
                type = NotificationType.SAFETY_ALERT,
                isRead = true
            )
        )
    }

    /**
     * Get localized explore posts
     */
    fun getLocalizedExplorePosts(context: Context): List<Post> {
        val res = context.resources
        return listOf(
            // Kids posts
            Post(
                id = 1L,
                createdAt = Instant.now().minus(30, ChronoUnit.MINUTES).toString(),
                content = res.getString(R.string.mock_post_puzzle),
                userId = "PuzzleKid",
                likes = 89,
                comments = 12,
                shares = 3,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "CalmObserver" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 2L,
                createdAt = Instant.now().minus(1, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_dinosaur),
                userId = "DinoLover99",
                likes = 156,
                comments = 28,
                shares = 8,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "DinoLover99" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 3L,
                createdAt = Instant.now().minus(2, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_cat),
                userId = "CatLover",
                likes = 234,
                comments = 45,
                shares = 15,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "SensorySeeker" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 13L,
                createdAt = Instant.now().minus(45, ChronoUnit.MINUTES).toString(),
                content = res.getString(R.string.mock_post_game),
                userId = "GameKid",
                likes = 178,
                comments = 32,
                shares = 5,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "Alex_Stims" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 14L,
                createdAt = Instant.now().minus(3, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_bracelet),
                userId = "CraftyKid",
                likes = 245,
                comments = 38,
                shares = 12,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "SensorySeeker" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 15L,
                createdAt = Instant.now().minus(4, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_dog),
                userId = "DoggieFriend",
                likes = 312,
                comments = 45,
                shares = 18,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "CalmObserver" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 16L,
                createdAt = Instant.now().minus(5, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_reading),
                userId = "BookWorm",
                likes = 198,
                comments = 28,
                shares = 8,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "DinoLover99" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 17L,
                createdAt = Instant.now().minus(6, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_art),
                userId = "ArtistKid",
                likes = 267,
                comments = 41,
                shares = 15,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "SensorySeeker" }?.avatarUrl,
                minAudience = Audience.UNDER_13
            ),

            // Teen posts
            Post(
                id = 4L,
                createdAt = Instant.now().minus(1, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_hyperfocus),
                userId = "NeuroThinker",
                likes = 125,
                comments = 18,
                shares = 5,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "HyperFocusCode" }?.avatarUrl,
                imageUrl = "https://example.com/image1.jpg",
                minAudience = Audience.TEEN
            ),
            Post(
                id = 5L,
                createdAt = Instant.now().minus(3, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_weighted_vest),
                userId = "SensorySeeker",
                likes = 250,
                comments = 45,
                shares = 10,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "SensorySeeker" }?.avatarUrl,
                imageUrl = "https://picsum.photos/seed/SensorySeeker/400/300",
                minAudience = Audience.TEEN
            ),
            Post(
                id = 6L,
                createdAt = Instant.now().minus(5, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_dating_adhd),
                userId = "ADHDDater",
                likes = 892,
                comments = 156,
                shares = 45,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "NeuroNaut" }?.avatarUrl,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 7L,
                createdAt = Instant.now().minus(8, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_school_stress),
                userId = "QuietStudier",
                likes = 445,
                comments = 89,
                shares = 22,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "CalmObserver" }?.avatarUrl,
                minAudience = Audience.TEEN
            ),

            // Adult posts
            Post(
                id = 8L,
                createdAt = Instant.now().minus(10, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_quiet_space),
                userId = "CalmObserver",
                likes = 50,
                comments = 5,
                shares = 1,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "CalmObserver" }?.avatarUrl,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 9L,
                createdAt = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
                content = res.getString(R.string.mock_post_organizing),
                userId = "NeuroNaut",
                likes = 1200,
                comments = 250,
                shares = 80,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "NeuroNaut" }?.avatarUrl,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 10L,
                createdAt = Instant.now().minus(6, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_workplace),
                userId = "WorkAdvocate",
                likes = 567,
                comments = 78,
                shares = 34,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "HyperFocusCode" }?.avatarUrl,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 11L,
                createdAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(),
                content = res.getString(R.string.mock_post_adulting),
                userId = "ChaoticAdult",
                likes = 2340,
                comments = 445,
                shares = 156,
                isLikedByMe = true,
                userAvatar = MOCK_USERS.find { it.id == "Alex_Stims" }?.avatarUrl,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 12L,
                createdAt = Instant.now().minus(4, ChronoUnit.HOURS).toString(),
                content = res.getString(R.string.mock_post_late_diagnosis),
                userId = "LateDiscovery",
                likes = 3450,
                comments = 567,
                shares = 234,
                isLikedByMe = false,
                userAvatar = MOCK_USERS.find { it.id == "NeuroNaut" }?.avatarUrl,
                minAudience = Audience.ADULT
            )
        )
    }

    /**
     * Get localized feed posts (the main feed posts with IDs 101-106)
     */
    fun getLocalizedFeedPosts(context: Context): List<Post> {
        val res = context.resources
        return listOf(
            Post(
                id = 101L,
                createdAt = Instant.now().toString(),
                content = res.getString(R.string.mock_post_workspace),
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
                content = res.getString(R.string.mock_post_brain_different),
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
                content = res.getString(R.string.mock_post_body_doubling),
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
                content = res.getString(R.string.mock_post_stim_toys),
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
                content = res.getString(R.string.mock_post_launch_pad),
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
                content = res.getString(R.string.mock_post_lunch_win),
                userId = "SelfCareSunday",
                likes = 2234,
                comments = 178,
                shares = 234,
                isLikedByMe = true,
                userAvatar = "https://i.pravatar.cc/150?u=selfcare"
            )
        )
    }

    /**
     * Get localized DM messages for a conversation
     */
    fun getLocalizedDmMessages(context: Context): List<Pair<String, Boolean>> {
        val res = context.resources
        return listOf(
            res.getString(R.string.mock_dm_hey) to false,
            res.getString(R.string.mock_dm_thanks) to true,
            res.getString(R.string.mock_dm_question) to false,
            res.getString(R.string.mock_dm_sure) to true
        )
    }

    /**
     * Get fully localized mock conversations for DM screen
     */
    fun getLocalizedConversations(context: Context): List<Conversation> {
        val res = context.resources
        val currentUserId = "me"
        val dinoUserId = "DinoLover99"
        val therapyUserId = "Therapy_Bot"

        return listOf(
            Conversation(
                id = "conv1",
                participants = listOf(currentUserId, dinoUserId),
                messages = listOf(
                    DirectMessage("msg1", dinoUserId, currentUserId, res.getString(R.string.mock_dm_conv1_msg1), Instant.now().minusSeconds(3600).toString()),
                    DirectMessage("msg2", currentUserId, dinoUserId, res.getString(R.string.mock_dm_conv1_msg2), Instant.now().minusSeconds(1800).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(1800).toString(),
                unreadCount = 0
            ),
            Conversation(
                id = "conv2",
                participants = listOf(currentUserId, therapyUserId),
                messages = listOf(
                    DirectMessage("msg3", therapyUserId, currentUserId, res.getString(R.string.mock_dm_conv2_msg1), Instant.now().minusSeconds(7200).toString(), moderationStatus = ModerationStatus.FLAGGED),
                    DirectMessage("msg4", currentUserId, therapyUserId, res.getString(R.string.mock_dm_conv2_msg2), Instant.now().minusSeconds(6000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(6000).toString(),
                unreadCount = 1
            ),
            Conversation(
                id = "conv3",
                participants = listOf(currentUserId, "NeuroNaut"),
                messages = listOf(
                    DirectMessage("msg5", "NeuroNaut", currentUserId, res.getString(R.string.mock_dm_conv3_msg1), Instant.now().minusSeconds(86400).toString()),
                    DirectMessage("msg6", currentUserId, "NeuroNaut", res.getString(R.string.mock_dm_conv3_msg2), Instant.now().minusSeconds(85000).toString()),
                    DirectMessage("msg7", "NeuroNaut", currentUserId, res.getString(R.string.mock_dm_conv3_msg3), Instant.now().minusSeconds(84000).toString()),
                    DirectMessage("msg8", currentUserId, "NeuroNaut", res.getString(R.string.mock_dm_conv3_msg4), Instant.now().minusSeconds(83000).toString()),
                    DirectMessage("msg9", "NeuroNaut", currentUserId, res.getString(R.string.mock_dm_conv3_msg5), Instant.now().minusSeconds(82000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(82000).toString(),
                unreadCount = 0
            ),
            Conversation(
                id = "conv4",
                participants = listOf(currentUserId, "HyperFocusCode"),
                messages = listOf(
                    DirectMessage("msg10", "HyperFocusCode", currentUserId, res.getString(R.string.mock_dm_conv4_msg1), Instant.now().minusSeconds(172800).toString()),
                    DirectMessage("msg11", currentUserId, "HyperFocusCode", res.getString(R.string.mock_dm_conv4_msg2), Instant.now().minusSeconds(172000).toString()),
                    DirectMessage("msg12", "HyperFocusCode", currentUserId, res.getString(R.string.mock_dm_conv4_msg3), Instant.now().minusSeconds(171000).toString()),
                    DirectMessage("msg13", currentUserId, "HyperFocusCode", res.getString(R.string.mock_dm_conv4_msg4), Instant.now().minusSeconds(170000).toString()),
                    DirectMessage("msg14", "HyperFocusCode", currentUserId, res.getString(R.string.mock_dm_conv4_msg5), Instant.now().minusSeconds(169000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(169000).toString(),
                unreadCount = 2
            ),
            Conversation(
                id = "conv5",
                participants = listOf(currentUserId, "SensorySeeker"),
                messages = listOf(
                    DirectMessage("msg15", "SensorySeeker", currentUserId, res.getString(R.string.mock_dm_conv5_msg1), Instant.now().minusSeconds(259200).toString()),
                    DirectMessage("msg16", currentUserId, "SensorySeeker", res.getString(R.string.mock_dm_conv5_msg2), Instant.now().minusSeconds(258000).toString()),
                    DirectMessage("msg17", "SensorySeeker", currentUserId, res.getString(R.string.mock_dm_conv5_msg3), Instant.now().minusSeconds(257000).toString()),
                    DirectMessage("msg18", currentUserId, "SensorySeeker", res.getString(R.string.mock_dm_conv5_msg4), Instant.now().minusSeconds(256000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(256000).toString(),
                unreadCount = 0
            ),
            Conversation(
                id = "conv6",
                participants = listOf(currentUserId, "CalmObserver"),
                messages = listOf(
                    DirectMessage("msg19", "CalmObserver", currentUserId, res.getString(R.string.mock_dm_conv6_msg1), Instant.now().minusSeconds(345600).toString()),
                    DirectMessage("msg20", currentUserId, "CalmObserver", res.getString(R.string.mock_dm_conv6_msg2), Instant.now().minusSeconds(344000).toString()),
                    DirectMessage("msg21", "CalmObserver", currentUserId, res.getString(R.string.mock_dm_conv6_msg3), Instant.now().minusSeconds(343000).toString()),
                    DirectMessage("msg22", currentUserId, "CalmObserver", res.getString(R.string.mock_dm_conv6_msg4), Instant.now().minusSeconds(342000).toString()),
                    DirectMessage("msg23", "CalmObserver", currentUserId, res.getString(R.string.mock_dm_conv6_msg5), Instant.now().minusSeconds(341000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(341000).toString(),
                unreadCount = 1
            ),
            Conversation(
                id = "conv7",
                participants = listOf(currentUserId, "Alex_Stims"),
                messages = listOf(
                    DirectMessage("msg24", "Alex_Stims", currentUserId, res.getString(R.string.mock_dm_conv7_msg1), Instant.now().minusSeconds(432000).toString()),
                    DirectMessage("msg25", currentUserId, "Alex_Stims", res.getString(R.string.mock_dm_conv7_msg2), Instant.now().minusSeconds(431000).toString()),
                    DirectMessage("msg26", "Alex_Stims", currentUserId, res.getString(R.string.mock_dm_conv7_msg3), Instant.now().minusSeconds(430000).toString()),
                    DirectMessage("msg27", currentUserId, "Alex_Stims", res.getString(R.string.mock_dm_conv7_msg4), Instant.now().minusSeconds(429000).toString()),
                    DirectMessage("msg28", "Alex_Stims", currentUserId, res.getString(R.string.mock_dm_conv7_msg5), Instant.now().minusSeconds(428000).toString()),
                    DirectMessage("msg29", currentUserId, "Alex_Stims", res.getString(R.string.mock_dm_conv7_msg6), Instant.now().minusSeconds(427000).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(427000).toString(),
                unreadCount = 0
            ),
            Conversation(
                id = "conv8",
                participants = listOf(currentUserId, "SpoonCounter"),
                messages = listOf(
                    DirectMessage("msg30", "SpoonCounter", currentUserId, res.getString(R.string.mock_dm_conv8_msg1), Instant.now().minusSeconds(14400).toString()),
                    DirectMessage("msg31", currentUserId, "SpoonCounter", res.getString(R.string.mock_dm_conv8_msg2), Instant.now().minusSeconds(13800).toString()),
                    DirectMessage("msg32", "SpoonCounter", currentUserId, res.getString(R.string.mock_dm_conv8_msg3), Instant.now().minusSeconds(13200).toString())
                ),
                lastMessageTimestamp = Instant.now().minusSeconds(13200).toString(),
                unreadCount = 1
            )
        )
    }
}

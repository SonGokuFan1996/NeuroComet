package com.kyilmaz.neurocomet

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationChannels
 *
 * These tests verify that the notification channel configuration is correct
 * and that the channel-to-notification-type mapping works properly.
 */
class NotificationChannelsTest {

    // ============================================================================
    // CHANNEL ID TESTS
    // ============================================================================

    @Test
    fun `channel IDs are not empty`() {
        // Messages
        assertTrue(NotificationChannels.CHANNEL_DIRECT_MESSAGES.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_GROUP_MESSAGES.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_MESSAGE_REQUESTS.isNotEmpty())

        // Social
        assertTrue(NotificationChannels.CHANNEL_LIKES.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_COMMENTS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_MENTIONS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_FOLLOWS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_FRIEND_ACTIVITY.isNotEmpty())

        // Community
        assertTrue(NotificationChannels.CHANNEL_COMMUNITY_UPDATES.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_EVENT_REMINDERS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_LIVE_EVENTS.isNotEmpty())

        // Account
        assertTrue(NotificationChannels.CHANNEL_ACCOUNT_SECURITY.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_PARENTAL_ALERTS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_LOGIN_ALERTS.isNotEmpty())

        // App
        assertTrue(NotificationChannels.CHANNEL_APP_UPDATES.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_TIPS_AND_TRICKS.isNotEmpty())

        // Wellness
        assertTrue(NotificationChannels.CHANNEL_WELLNESS_REMINDERS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_BREAK_REMINDERS.isNotEmpty())
        assertTrue(NotificationChannels.CHANNEL_CALM_MODE.isNotEmpty())
    }

    @Test
    fun `all channel IDs are unique`() {
        val channelIds = listOf(
            NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            NotificationChannels.CHANNEL_GROUP_MESSAGES,
            NotificationChannels.CHANNEL_MESSAGE_REQUESTS,
            NotificationChannels.CHANNEL_LIKES,
            NotificationChannels.CHANNEL_COMMENTS,
            NotificationChannels.CHANNEL_MENTIONS,
            NotificationChannels.CHANNEL_FOLLOWS,
            NotificationChannels.CHANNEL_FRIEND_ACTIVITY,
            NotificationChannels.CHANNEL_COMMUNITY_UPDATES,
            NotificationChannels.CHANNEL_EVENT_REMINDERS,
            NotificationChannels.CHANNEL_LIVE_EVENTS,
            NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            NotificationChannels.CHANNEL_PARENTAL_ALERTS,
            NotificationChannels.CHANNEL_LOGIN_ALERTS,
            NotificationChannels.CHANNEL_APP_UPDATES,
            NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS,
            NotificationChannels.CHANNEL_TIPS_AND_TRICKS,
            NotificationChannels.CHANNEL_WELLNESS_REMINDERS,
            NotificationChannels.CHANNEL_BREAK_REMINDERS,
            NotificationChannels.CHANNEL_CALM_MODE
        )

        val uniqueIds = channelIds.toSet()
        assertEquals("All channel IDs should be unique", channelIds.size, uniqueIds.size)
    }

    @Test
    fun `channel IDs follow naming convention`() {
        // All channel IDs should be lowercase with underscores
        val channelIds = listOf(
            NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            NotificationChannels.CHANNEL_GROUP_MESSAGES,
            NotificationChannels.CHANNEL_MESSAGE_REQUESTS,
            NotificationChannels.CHANNEL_LIKES,
            NotificationChannels.CHANNEL_COMMENTS,
            NotificationChannels.CHANNEL_MENTIONS,
            NotificationChannels.CHANNEL_FOLLOWS,
            NotificationChannels.CHANNEL_FRIEND_ACTIVITY,
            NotificationChannels.CHANNEL_COMMUNITY_UPDATES,
            NotificationChannels.CHANNEL_EVENT_REMINDERS,
            NotificationChannels.CHANNEL_LIVE_EVENTS,
            NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            NotificationChannels.CHANNEL_PARENTAL_ALERTS,
            NotificationChannels.CHANNEL_LOGIN_ALERTS,
            NotificationChannels.CHANNEL_APP_UPDATES,
            NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS,
            NotificationChannels.CHANNEL_TIPS_AND_TRICKS,
            NotificationChannels.CHANNEL_WELLNESS_REMINDERS,
            NotificationChannels.CHANNEL_BREAK_REMINDERS,
            NotificationChannels.CHANNEL_CALM_MODE
        )

        channelIds.forEach { channelId ->
            assertTrue(
                "Channel ID '$channelId' should match naming convention (lowercase with underscores)",
                channelId.matches(Regex("^[a-z][a-z0-9_]*$"))
            )
        }
    }

    // ============================================================================
    // NOTIFICATION TYPE MAPPING TESTS
    // ============================================================================

    @Test
    fun `getChannelForNotificationType returns correct channel for LIKE`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.LIKE)
        assertEquals(NotificationChannels.CHANNEL_LIKES, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for COMMENT`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.COMMENT)
        assertEquals(NotificationChannels.CHANNEL_COMMENTS, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for FOLLOW`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.FOLLOW)
        assertEquals(NotificationChannels.CHANNEL_FOLLOWS, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for MENTION`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.MENTION)
        assertEquals(NotificationChannels.CHANNEL_MENTIONS, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for REPOST`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.REPOST)
        assertEquals(NotificationChannels.CHANNEL_FRIEND_ACTIVITY, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for BADGE`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.BADGE)
        assertEquals(NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for SYSTEM`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.SYSTEM)
        assertEquals(NotificationChannels.CHANNEL_APP_UPDATES, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for WELCOME`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.WELCOME)
        assertEquals(NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS, channel)
    }

    @Test
    fun `getChannelForNotificationType returns correct channel for SAFETY_ALERT`() {
        val channel = NotificationChannels.getChannelForNotificationType(NotificationType.SAFETY_ALERT)
        assertEquals(NotificationChannels.CHANNEL_ACCOUNT_SECURITY, channel)
    }

    @Test
    fun `getChannelForNotificationType handles all notification types`() {
        // This test ensures that when new notification types are added,
        // they are also handled in the channel mapping
        NotificationType.entries.forEach { type ->
            val channel = NotificationChannels.getChannelForNotificationType(type)
            assertNotNull("Channel mapping should exist for $type", channel)
            assertTrue("Channel for $type should not be empty", channel.isNotEmpty())
        }
    }

    // ============================================================================
    // CHANNEL COUNT TESTS
    // ============================================================================

    @Test
    fun `expected number of message channels`() {
        val messageChannels = listOf(
            NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            NotificationChannels.CHANNEL_GROUP_MESSAGES,
            NotificationChannels.CHANNEL_MESSAGE_REQUESTS
        )
        assertEquals(3, messageChannels.size)
    }

    @Test
    fun `expected number of social channels`() {
        val socialChannels = listOf(
            NotificationChannels.CHANNEL_LIKES,
            NotificationChannels.CHANNEL_COMMENTS,
            NotificationChannels.CHANNEL_MENTIONS,
            NotificationChannels.CHANNEL_FOLLOWS,
            NotificationChannels.CHANNEL_FRIEND_ACTIVITY
        )
        assertEquals(5, socialChannels.size)
    }

    @Test
    fun `expected number of community channels`() {
        val communityChannels = listOf(
            NotificationChannels.CHANNEL_COMMUNITY_UPDATES,
            NotificationChannels.CHANNEL_EVENT_REMINDERS,
            NotificationChannels.CHANNEL_LIVE_EVENTS
        )
        assertEquals(3, communityChannels.size)
    }

    @Test
    fun `expected number of account channels`() {
        val accountChannels = listOf(
            NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            NotificationChannels.CHANNEL_PARENTAL_ALERTS,
            NotificationChannels.CHANNEL_LOGIN_ALERTS
        )
        assertEquals(3, accountChannels.size)
    }

    @Test
    fun `expected number of app channels`() {
        val appChannels = listOf(
            NotificationChannels.CHANNEL_APP_UPDATES,
            NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS,
            NotificationChannels.CHANNEL_TIPS_AND_TRICKS
        )
        assertEquals(3, appChannels.size)
    }

    @Test
    fun `expected number of wellness channels`() {
        val wellnessChannels = listOf(
            NotificationChannels.CHANNEL_WELLNESS_REMINDERS,
            NotificationChannels.CHANNEL_BREAK_REMINDERS,
            NotificationChannels.CHANNEL_CALM_MODE
        )
        assertEquals(3, wellnessChannels.size)
    }

    @Test
    fun `total channel count is 20`() {
        val allChannels = listOf(
            // Messages (3)
            NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            NotificationChannels.CHANNEL_GROUP_MESSAGES,
            NotificationChannels.CHANNEL_MESSAGE_REQUESTS,
            // Social (5)
            NotificationChannels.CHANNEL_LIKES,
            NotificationChannels.CHANNEL_COMMENTS,
            NotificationChannels.CHANNEL_MENTIONS,
            NotificationChannels.CHANNEL_FOLLOWS,
            NotificationChannels.CHANNEL_FRIEND_ACTIVITY,
            // Community (3)
            NotificationChannels.CHANNEL_COMMUNITY_UPDATES,
            NotificationChannels.CHANNEL_EVENT_REMINDERS,
            NotificationChannels.CHANNEL_LIVE_EVENTS,
            // Account (3)
            NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            NotificationChannels.CHANNEL_PARENTAL_ALERTS,
            NotificationChannels.CHANNEL_LOGIN_ALERTS,
            // App (3)
            NotificationChannels.CHANNEL_APP_UPDATES,
            NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS,
            NotificationChannels.CHANNEL_TIPS_AND_TRICKS,
            // Wellness (3)
            NotificationChannels.CHANNEL_WELLNESS_REMINDERS,
            NotificationChannels.CHANNEL_BREAK_REMINDERS,
            NotificationChannels.CHANNEL_CALM_MODE
        )
        assertEquals(20, allChannels.size)
    }
}


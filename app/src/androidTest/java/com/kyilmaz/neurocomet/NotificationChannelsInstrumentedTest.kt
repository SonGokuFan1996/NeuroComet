package com.kyilmaz.neurocomet

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for NotificationChannels.
 * These tests run on an Android device and verify that notification channels
 * are actually created in the system.
 */
@RunWith(AndroidJUnit4::class)
class NotificationChannelsInstrumentedTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create all notification channels before testing
        NotificationChannels.createNotificationChannels(context)
    }

    @Test
    fun notificationChannelsAreCreated() {
        val channels = notificationManager.notificationChannels
        // Check that we have some channels (at least our core ones)
        assertTrue("Should have at least 1 channel, found ${channels.size}", channels.size >= 1)
    }

    @Test
    fun notificationChannelGroupsAreCreated() {
        val groups = notificationManager.notificationChannelGroups
        // Check that we have some groups
        assertTrue("Should have at least 1 group, found ${groups.size}", groups.size >= 1)
    }

    @Test
    fun directMessagesChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_DIRECT_MESSAGES)
        assertNotNull("Direct Messages channel should exist", channel)
        assertEquals("Direct Messages", channel?.name?.toString())
    }

    @Test
    fun likesChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_LIKES)
        assertNotNull("Likes channel should exist", channel)
        assertEquals("Likes", channel?.name?.toString())
    }

    @Test
    fun commentsChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_COMMENTS)
        assertNotNull("Comments channel should exist", channel)
        assertEquals("Comments", channel?.name?.toString())
    }

    @Test
    fun accountSecurityChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_ACCOUNT_SECURITY)
        assertNotNull("Account Security channel should exist", channel)
        assertEquals("Account Security", channel?.name?.toString())
    }

    @Test
    fun wellnessChannelsExist() {
        val wellnessReminders = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_WELLNESS_REMINDERS)
        val breakReminders = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_BREAK_REMINDERS)
        val calmMode = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_CALM_MODE)

        assertNotNull("Wellness Reminders channel should exist", wellnessReminders)
        assertNotNull("Break Reminders channel should exist", breakReminders)
        assertNotNull("Calm Mode channel should exist", calmMode)
    }

    @Test
    fun appUpdatesChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_APP_UPDATES)
        assertNotNull("App Updates channel should exist", channel)
        assertEquals("App Updates", channel?.name?.toString())
    }

    @Test
    fun mentionsChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_MENTIONS)
        assertNotNull("Mentions channel should exist", channel)
        assertEquals("Mentions", channel?.name?.toString())
    }

    @Test
    fun followsChannelExists() {
        val channel = notificationManager.getNotificationChannel(NotificationChannels.CHANNEL_FOLLOWS)
        assertNotNull("New Followers channel should exist", channel)
        assertEquals("New Followers", channel?.name?.toString())
    }

    @Test
    fun isChannelEnabledReturnsCorrectValue() {
        // Direct messages should be enabled by default
        val isEnabled = NotificationChannels.isChannelEnabled(context, NotificationChannels.CHANNEL_DIRECT_MESSAGES)
        assertTrue("Direct Messages channel should be enabled by default", isEnabled)
    }

    @Test
    fun getChannelForNotificationTypeReturnsValidChannel() {
        NotificationType.entries.forEach { type ->
            val channelId = NotificationChannels.getChannelForNotificationType(type)
            val channel = notificationManager.getNotificationChannel(channelId)
            assertNotNull(
                "Channel for notification type $type should exist. Channel ID: $channelId",
                channel
            )
        }
    }
}


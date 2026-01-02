package com.kyilmaz.neurocomet

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

private const val TAG = "NotificationChannels"

/**
 * Production-ready notification channels for NeuroComet.
 *
 * This class manages all notification channels required by the app.
 * Channels are organized by category for better user control.
 *
 * Features:
 * - Neurodivergent-friendly defaults (less intrusive)
 * - Grouped channels for organized settings
 * - Customizable importance levels
 * - Support for accessibility needs
 */
@Suppress("unused")
object NotificationChannels {

    // ============================================================================
    // CHANNEL IDs
    // ============================================================================

    // Messages
    const val CHANNEL_DIRECT_MESSAGES = "direct_messages"
    const val CHANNEL_GROUP_MESSAGES = "group_messages"
    const val CHANNEL_MESSAGE_REQUESTS = "message_requests"

    // Social
    const val CHANNEL_LIKES = "likes"
    const val CHANNEL_COMMENTS = "comments"
    const val CHANNEL_MENTIONS = "mentions"
    const val CHANNEL_FOLLOWS = "follows"
    const val CHANNEL_FRIEND_ACTIVITY = "friend_activity"

    // Community
    const val CHANNEL_COMMUNITY_UPDATES = "community_updates"
    const val CHANNEL_EVENT_REMINDERS = "event_reminders"
    const val CHANNEL_LIVE_EVENTS = "live_events"

    // Account & Security
    const val CHANNEL_ACCOUNT_SECURITY = "account_security"
    const val CHANNEL_PARENTAL_ALERTS = "parental_alerts"
    const val CHANNEL_LOGIN_ALERTS = "login_alerts"

    // App Updates
    const val CHANNEL_APP_UPDATES = "app_updates"
    const val CHANNEL_FEATURE_ANNOUNCEMENTS = "feature_announcements"
    const val CHANNEL_TIPS_AND_TRICKS = "tips_and_tricks"

    // Wellness
    const val CHANNEL_WELLNESS_REMINDERS = "wellness_reminders"
    const val CHANNEL_BREAK_REMINDERS = "break_reminders"
    const val CHANNEL_CALM_MODE = "calm_mode"

    // ============================================================================
    // CHANNEL GROUPS
    // ============================================================================

    private const val GROUP_MESSAGES = "group_messages_category"
    private const val GROUP_SOCIAL = "group_social"
    private const val GROUP_COMMUNITY = "group_community"
    private const val GROUP_ACCOUNT = "group_account"
    private const val GROUP_APP = "group_app"
    private const val GROUP_WELLNESS = "group_wellness"

    // ============================================================================
    // INITIALIZATION
    // ============================================================================

    /**
     * Initialize all notification channels.
     * Call this from Application.onCreate() or MainActivity.onCreate()
     */
    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d(TAG, "Creating notification channels...")

        // Create channel groups first
        createChannelGroups(notificationManager)

        // Create all channels
        createMessagesChannels(notificationManager)
        createSocialChannels(notificationManager)
        createCommunityChannels(notificationManager)
        createAccountChannels(notificationManager)
        createAppChannels(notificationManager)
        createWellnessChannels(notificationManager)

        Log.d(TAG, "Notification channels created successfully. Total channels: ${notificationManager.notificationChannels.size}")
    }

    private fun createChannelGroups(notificationManager: NotificationManager) {
        val groups = listOf(
            NotificationChannelGroup(GROUP_MESSAGES, "Messages"),
            NotificationChannelGroup(GROUP_SOCIAL, "Social"),
            NotificationChannelGroup(GROUP_COMMUNITY, "Community"),
            NotificationChannelGroup(GROUP_ACCOUNT, "Account & Security"),
            NotificationChannelGroup(GROUP_APP, "App Updates"),
            NotificationChannelGroup(GROUP_WELLNESS, "Wellness")
        )

        groups.forEach { notificationManager.createNotificationChannelGroup(it) }
        Log.d(TAG, "Created ${groups.size} channel groups")
    }

    // ============================================================================
    // MESSAGES CHANNELS
    // ============================================================================

    private fun createMessagesChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_DIRECT_MESSAGES,
                "Direct Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new direct messages"
                group = GROUP_MESSAGES
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100) // Gentle pattern
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_GROUP_MESSAGES,
                "Group Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for group chat messages"
                group = GROUP_MESSAGES
                enableVibration(true)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_MESSAGE_REQUESTS,
                "Message Requests",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for new message requests from people you don't follow"
                group = GROUP_MESSAGES
                setShowBadge(true)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // SOCIAL CHANNELS
    // ============================================================================

    private fun createSocialChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_LIKES,
                "Likes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications when someone likes your posts"
                group = GROUP_SOCIAL
                enableVibration(false) // Non-intrusive
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_COMMENTS,
                "Comments",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for comments on your posts"
                group = GROUP_SOCIAL
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_MENTIONS,
                "Mentions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when someone mentions you"
                group = GROUP_SOCIAL
                enableVibration(true)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_FOLLOWS,
                "New Followers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for new followers"
                group = GROUP_SOCIAL
                enableVibration(false)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_FRIEND_ACTIVITY,
                "Friend Activity",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Updates about what your friends are doing"
                group = GROUP_SOCIAL
                enableVibration(false)
                setShowBadge(false)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // COMMUNITY CHANNELS
    // ============================================================================

    private fun createCommunityChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_COMMUNITY_UPDATES,
                "Community Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates from communities you've joined"
                group = GROUP_COMMUNITY
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_EVENT_REMINDERS,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming events you've RSVP'd to"
                group = GROUP_COMMUNITY
                enableVibration(true)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_LIVE_EVENTS,
                "Live Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when live events start"
                group = GROUP_COMMUNITY
                enableVibration(true)
                setShowBadge(true)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // ACCOUNT & SECURITY CHANNELS
    // ============================================================================

    private fun createAccountChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_ACCOUNT_SECURITY,
                "Account Security",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important security alerts for your account"
                group = GROUP_ACCOUNT
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true) // Security alerts should bypass DND
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            },

            NotificationChannel(
                CHANNEL_PARENTAL_ALERTS,
                "Parental Control Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for parents monitoring child accounts"
                group = GROUP_ACCOUNT
                enableVibration(true)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_LOGIN_ALERTS,
                "Login Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about new logins to your account"
                group = GROUP_ACCOUNT
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // APP CHANNELS
    // ============================================================================

    private fun createAppChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_APP_UPDATES,
                "App Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications about app updates"
                group = GROUP_APP
                enableVibration(false)
                setShowBadge(false)
            },

            NotificationChannel(
                CHANNEL_FEATURE_ANNOUNCEMENTS,
                "Feature Announcements",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Announcements about new features"
                group = GROUP_APP
                enableVibration(false)
                setShowBadge(true)
            },

            NotificationChannel(
                CHANNEL_TIPS_AND_TRICKS,
                "Tips & Tricks",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Helpful tips for using NeuroComet"
                group = GROUP_APP
                enableVibration(false)
                setShowBadge(false)
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // WELLNESS CHANNELS (Neurodivergent-Friendly)
    // ============================================================================

    private fun createWellnessChannels(notificationManager: NotificationManager) {

        val channels = listOf(
            NotificationChannel(
                CHANNEL_WELLNESS_REMINDERS,
                "Wellness Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders for self-care and wellness"
                group = GROUP_WELLNESS
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 50) // Very gentle
                setShowBadge(false)
            },

            NotificationChannel(
                CHANNEL_BREAK_REMINDERS,
                "Break Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reminders to take breaks from the app"
                group = GROUP_WELLNESS
                enableVibration(false) // Non-intrusive
                setShowBadge(false)
            },

            NotificationChannel(
                CHANNEL_CALM_MODE,
                "Calm Mode",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Ultra-quiet notifications for calm mode"
                group = GROUP_WELLNESS
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
                setSound(null, null) // Silent
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    // ============================================================================
    // HELPER FUNCTIONS
    // ============================================================================

    /**
     * Get the appropriate channel ID for a notification type
     */
    fun getChannelForNotificationType(type: NotificationType): String {
        return when (type) {
            NotificationType.LIKE -> CHANNEL_LIKES
            NotificationType.COMMENT -> CHANNEL_COMMENTS
            NotificationType.FOLLOW -> CHANNEL_FOLLOWS
            NotificationType.MENTION -> CHANNEL_MENTIONS
            NotificationType.REPOST -> CHANNEL_FRIEND_ACTIVITY
            NotificationType.BADGE -> CHANNEL_FEATURE_ANNOUNCEMENTS
            NotificationType.SYSTEM -> CHANNEL_APP_UPDATES
            NotificationType.WELCOME -> CHANNEL_FEATURE_ANNOUNCEMENTS
            NotificationType.SAFETY_ALERT -> CHANNEL_ACCOUNT_SECURITY
        }
    }

    /**
     * Check if a specific channel is enabled
     */
    fun isChannelEnabled(context: Context, channelId: String): Boolean {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }

    /**
     * Open notification channel settings for a specific channel
     */
    fun openChannelSettings(context: Context, channelId: String) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, channelId)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Open app notification settings
     */
    fun openAppNotificationSettings(context: Context) {
        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ============================================================================
    // PERMISSION HANDLING (Android 13+)
    // ============================================================================

    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    /**
     * Check if the app has notification permission.
     * Returns true on Android 12 and below (permission not required).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on Android 12 and below
        }
    }

    /**
     * Request notification permission on Android 13+.
     * Call this from an Activity.
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Check if we should show rationale for notification permission
     */
    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }
}

/**
 * Helper class for building notifications with the correct channel
 */
object NotificationHelper {

    /**
     * Create a notification builder with the appropriate channel
     */
    fun createNotificationBuilder(
        context: Context,
        channelId: String,
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use app icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }

    /**
     * Create a DM notification
     */
    fun createDmNotification(
        context: Context,
        senderName: String,
        message: String,
        senderAvatarUrl: String? = null
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_DIRECT_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
    }

    /**
     * Create a social notification (like, comment, follow, mention)
     */
    fun createSocialNotification(
        context: Context,
        type: NotificationType,
        title: String,
        content: String
    ): NotificationCompat.Builder {
        val channelId = NotificationChannels.getChannelForNotificationType(type)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(
                when (type) {
                    NotificationType.MENTION -> NotificationCompat.PRIORITY_HIGH
                    NotificationType.COMMENT -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_LOW
                }
            )
            .setAutoCancel(true)
    }

    /**
     * Create a security alert notification
     */
    fun createSecurityNotification(
        context: Context,
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ACCOUNT_SECURITY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
    }

    /**
     * Create a notification with DecoratedCustomViewStyle for custom layouts.
     * This style allows custom content views while keeping the system-provided
     * decorations (small icon, timestamp, expand affordance).
     */
    fun createDecoratedCustomViewNotification(
        context: Context,
        channelId: String,
        title: String,
        content: String,
        customContentView: RemoteViews? = null,
        customBigContentView: RemoteViews? = null
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // Apply custom views if provided
        customContentView?.let { builder.setCustomContentView(it) }
        customBigContentView?.let { builder.setCustomBigContentView(it) }

        return builder
    }

    /**
     * Create a RemoteViews for a simple custom notification layout.
     * This creates a basic layout programmatically without requiring a separate XML file.
     */
    fun createSimpleRemoteViews(
        context: Context,
        title: String,
        content: String,
        showIcon: Boolean = true
    ): RemoteViews {
        // Use a simple system layout as base
        val remoteViews = RemoteViews(context.packageName, android.R.layout.simple_list_item_2)
        remoteViews.setTextViewText(android.R.id.text1, title)
        remoteViews.setTextViewText(android.R.id.text2, content)
        return remoteViews
    }

    /**
     * Create a DM notification with custom view style for better visual integration.
     */
    fun createCustomDmNotification(
        context: Context,
        senderName: String,
        message: String,
        senderAvatarUrl: String? = null
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_DIRECT_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
    }

    /**
     * Create a messaging-style notification (recommended for chat apps).
     * This provides the best integration with the system UI.
     */
    fun createMessagingStyleNotification(
        context: Context,
        senderName: String,
        message: String,
        conversationTitle: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): NotificationCompat.Builder {
        val person = androidx.core.app.Person.Builder()
            .setName(senderName)
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(message, timestamp, person)

        conversationTitle?.let { messagingStyle.setConversationTitle(it) }

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_DIRECT_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(messagingStyle)
            .setAutoCancel(true)
    }

    /**
     * Create a notification using the custom XML layout files.
     * Uses DecoratedCustomViewStyle for proper system integration.
     */
    fun createCustomLayoutNotification(
        context: Context,
        channelId: String,
        title: String,
        content: String,
        timestamp: String? = null,
        category: String? = null
    ): NotificationCompat.Builder {
        // Create collapsed view with neurodivergent-friendly design
        val collapsedView = RemoteViews(context.packageName, R.layout.notification_custom)
        collapsedView.setTextViewText(R.id.notification_title, title)
        collapsedView.setTextViewText(R.id.notification_content, content)
        timestamp?.let { collapsedView.setTextViewText(R.id.notification_time, it) }

        // Create expanded view with full neurodivergent-friendly design
        val expandedView = RemoteViews(context.packageName, R.layout.notification_custom_big)
        expandedView.setTextViewText(R.id.notification_title, title)
        expandedView.setTextViewText(R.id.notification_content, content)
        timestamp?.let { expandedView.setTextViewText(R.id.notification_timestamp, it) }
        category?.let { expandedView.setTextViewText(R.id.notification_category, it) }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setAutoCancel(true)
    }

    /**
     * Create a heads-up notification that appears as a banner.
     * Uses full-screen intent priority to ensure visibility.
     */
    fun createHeadsUpNotification(
        context: Context,
        channelId: String,
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            // Heads-up notifications require high importance channel + priority
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }
}

/**
 * Notification testing utility for development.
 * Provides methods to test all notification types with banner-style display.
 */
object NotificationTester {

    private const val TAG = "NotificationTester"

    /**
     * Data class representing a test notification configuration
     */
    data class TestNotificationConfig(
        val id: Int,
        val channelId: String,
        val title: String,
        val content: String,
        val priority: Int = NotificationCompat.PRIORITY_HIGH,
        val category: String? = null,
        val groupKey: String? = null
    )

    /**
     * All available test notification configurations
     */
    val allTestNotifications: List<TestNotificationConfig> = listOf(
        // Messages
        TestNotificationConfig(
            id = 1001,
            channelId = NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            title = "💬 DinoLover99 sent you a message",
            content = "Hey! Did you see the new sensory room tips?",
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_MESSAGE,
            groupKey = "messages"
        ),
        TestNotificationConfig(
            id = 1002,
            channelId = NotificationChannels.CHANNEL_GROUP_MESSAGES,
            title = "👥 ADHD Support Group",
            content = "FocusFriend: Anyone else struggle with time blindness?",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_MESSAGE,
            groupKey = "messages"
        ),
        TestNotificationConfig(
            id = 1003,
            channelId = NotificationChannels.CHANNEL_MESSAGE_REQUESTS,
            title = "📩 New message request",
            content = "QuietMind wants to send you a message",
            priority = NotificationCompat.PRIORITY_LOW,
            category = NotificationCompat.CATEGORY_MESSAGE,
            groupKey = "messages"
        ),

        // Social
        TestNotificationConfig(
            id = 2001,
            channelId = NotificationChannels.CHANNEL_LIKES,
            title = "❤️ CalmObserver liked your post",
            content = "\"My new weighted blanket arrived...\"",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "social"
        ),
        TestNotificationConfig(
            id = 2002,
            channelId = NotificationChannels.CHANNEL_COMMENTS,
            title = "💭 New comment on your post",
            content = "TherapyBot: Great advice! This really helps 💙",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "social"
        ),
        TestNotificationConfig(
            id = 2003,
            channelId = NotificationChannels.CHANNEL_MENTIONS,
            title = "📢 You were mentioned",
            content = "AutismAdvocate mentioned you in a post about sensory tips",
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "social"
        ),
        TestNotificationConfig(
            id = 2004,
            channelId = NotificationChannels.CHANNEL_FOLLOWS,
            title = "👋 New follower!",
            content = "MindfulMoments started following you",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "social"
        ),
        TestNotificationConfig(
            id = 2005,
            channelId = NotificationChannels.CHANNEL_FRIEND_ACTIVITY,
            title = "🔄 Your friend shared a post",
            content = "FocusFriend shared your ADHD tips with 142 followers",
            priority = NotificationCompat.PRIORITY_LOW,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "social"
        ),

        // Community
        TestNotificationConfig(
            id = 3001,
            channelId = NotificationChannels.CHANNEL_COMMUNITY_UPDATES,
            title = "🌟 Community Update",
            content = "New guidelines for the Autism Support community",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SOCIAL,
            groupKey = "community"
        ),
        TestNotificationConfig(
            id = 3002,
            channelId = NotificationChannels.CHANNEL_EVENT_REMINDERS,
            title = "📅 Event Reminder",
            content = "Virtual Meditation Session starts in 30 minutes",
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_EVENT,
            groupKey = "community"
        ),
        TestNotificationConfig(
            id = 3003,
            channelId = NotificationChannels.CHANNEL_LIVE_EVENTS,
            title = "🔴 Live now!",
            content = "Dr. Sarah is hosting a Q&A on managing anxiety",
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_EVENT,
            groupKey = "community"
        ),

        // Account & Security
        TestNotificationConfig(
            id = 4001,
            channelId = NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            title = "🔒 Security Alert",
            content = "New login detected from Pixel 10 Pro",
            priority = NotificationCompat.PRIORITY_MAX,
            category = NotificationCompat.CATEGORY_ALARM,
            groupKey = "security"
        ),
        TestNotificationConfig(
            id = 4002,
            channelId = NotificationChannels.CHANNEL_PARENTAL_ALERTS,
            title = "👨‍👩‍👧 Parental Alert",
            content = "Your child has requested to change their screen time settings",
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_SYSTEM,
            groupKey = "security"
        ),
        TestNotificationConfig(
            id = 4003,
            channelId = NotificationChannels.CHANNEL_LOGIN_ALERTS,
            title = "🔑 Login successful",
            content = "You signed in from a new device",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SYSTEM,
            groupKey = "security"
        ),

        // App Updates
        TestNotificationConfig(
            id = 5001,
            channelId = NotificationChannels.CHANNEL_APP_UPDATES,
            title = "🎉 App Update Available",
            content = "NeuroComet v1.1.0 is ready to install with new features!",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_SYSTEM,
            groupKey = "app"
        ),
        TestNotificationConfig(
            id = 5002,
            channelId = NotificationChannels.CHANNEL_FEATURE_ANNOUNCEMENTS,
            title = "✨ New Feature",
            content = "Try our new Calm Mode for reduced stimulation",
            priority = NotificationCompat.PRIORITY_LOW,
            category = NotificationCompat.CATEGORY_RECOMMENDATION,
            groupKey = "app"
        ),
        TestNotificationConfig(
            id = 5003,
            channelId = NotificationChannels.CHANNEL_TIPS_AND_TRICKS,
            title = "💡 Tip of the Day",
            content = "Did you know you can customize notification sounds per channel?",
            priority = NotificationCompat.PRIORITY_MIN,
            category = NotificationCompat.CATEGORY_RECOMMENDATION,
            groupKey = "app"
        ),

        // Wellness
        TestNotificationConfig(
            id = 6001,
            channelId = NotificationChannels.CHANNEL_WELLNESS_REMINDERS,
            title = "🧘 Wellness Check",
            content = "Take a moment to check in with yourself. How are you feeling?",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_REMINDER,
            groupKey = "wellness"
        ),
        TestNotificationConfig(
            id = 6002,
            channelId = NotificationChannels.CHANNEL_BREAK_REMINDERS,
            title = "☕ Time for a break!",
            content = "You've been active for a while. Consider stretching or hydrating.",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_REMINDER,
            groupKey = "wellness"
        ),
        TestNotificationConfig(
            id = 6003,
            channelId = NotificationChannels.CHANNEL_CALM_MODE,
            title = "🌙 Calm Mode Active",
            content = "Notifications are now quieter. Tap to adjust settings.",
            priority = NotificationCompat.PRIORITY_LOW,
            category = NotificationCompat.CATEGORY_STATUS,
            groupKey = "wellness"
        )
    )

    /**
     * Send a single test notification with banner display
     */
    fun sendTestNotification(
        context: Context,
        config: TestNotificationConfig
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if channel exists
        val channel = notificationManager.getNotificationChannel(config.channelId)
        if (channel == null) {
            Log.w(TAG, "Channel ${config.channelId} not found. Creating channels...")
            NotificationChannels.createNotificationChannels(context)
        }

        val notification = NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(config.title)
            .setContentText(config.content)
            .setPriority(config.priority)
            .setAutoCancel(true)
            // Force banner/heads-up display for HIGH priority
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .apply {
                config.category?.let { setCategory(it) }
                config.groupKey?.let { setGroup(it) }
            }
            .build()

        notificationManager.notify(config.id, notification)
        Log.d(TAG, "Sent test notification: ${config.title}")
    }

    /**
     * Send all test notifications with a delay between each
     */
    suspend fun sendAllTestNotifications(
        context: Context,
        delayMillis: Long = 1500L
    ) {
        allTestNotifications.forEach { config ->
            sendTestNotification(context, config)
            kotlinx.coroutines.delay(delayMillis)
        }
    }

    /**
     * Send test notifications by category
     */
    fun sendNotificationsByCategory(
        context: Context,
        category: NotificationTestCategory
    ) {
        val configs = when (category) {
            NotificationTestCategory.MESSAGES -> allTestNotifications.filter { it.groupKey == "messages" }
            NotificationTestCategory.SOCIAL -> allTestNotifications.filter { it.groupKey == "social" }
            NotificationTestCategory.COMMUNITY -> allTestNotifications.filter { it.groupKey == "community" }
            NotificationTestCategory.SECURITY -> allTestNotifications.filter { it.groupKey == "security" }
            NotificationTestCategory.APP -> allTestNotifications.filter { it.groupKey == "app" }
            NotificationTestCategory.WELLNESS -> allTestNotifications.filter { it.groupKey == "wellness" }
        }

        configs.forEach { config ->
            sendTestNotification(context, config)
        }
    }

    /**
     * Clear all test notifications
     */
    fun clearAllTestNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        allTestNotifications.forEach { config ->
            notificationManager.cancel(config.id)
        }
        Log.d(TAG, "Cleared all test notifications")
    }

    /**
     * Get notification count by category
     */
    fun getNotificationCountByCategory(): Map<NotificationTestCategory, Int> {
        return mapOf(
            NotificationTestCategory.MESSAGES to allTestNotifications.count { it.groupKey == "messages" },
            NotificationTestCategory.SOCIAL to allTestNotifications.count { it.groupKey == "social" },
            NotificationTestCategory.COMMUNITY to allTestNotifications.count { it.groupKey == "community" },
            NotificationTestCategory.SECURITY to allTestNotifications.count { it.groupKey == "security" },
            NotificationTestCategory.APP to allTestNotifications.count { it.groupKey == "app" },
            NotificationTestCategory.WELLNESS to allTestNotifications.count { it.groupKey == "wellness" }
        )
    }
}

/**
 * Categories for notification testing
 */
enum class NotificationTestCategory(val displayName: String, val emoji: String) {
    MESSAGES("Messages", "💬"),
    SOCIAL("Social", "❤️"),
    COMMUNITY("Community", "🌟"),
    SECURITY("Security", "🔒"),
    APP("App Updates", "🎉"),
    WELLNESS("Wellness", "🧘")
}


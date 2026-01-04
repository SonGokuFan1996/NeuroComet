package com.kyilmaz.neurocomet

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit

/**
 * Social media settings that fit the neurodivergent-friendly vibe of NeuroComet.
 * All settings are designed with sensory sensitivities and cognitive needs in mind.
 */

// ============================================================================
// SETTINGS DATA CLASSES
// ============================================================================

/**
 * Privacy settings state
 */
data class PrivacySettings(
    val isAccountPrivate: Boolean = false,
    val allowDMsFrom: DMPermission = DMPermission.EVERYONE,
    val showOnlineStatus: Boolean = true,
    val showReadReceipts: Boolean = true,
    val allowTagging: TagPermission = TagPermission.EVERYONE,
    val hideFromSearch: Boolean = false,
    val twoFactorEnabled: Boolean = false
)

enum class DMPermission(val displayName: String, val description: String) {
    EVERYONE("Everyone", "Anyone can message you"),
    FOLLOWERS("Followers Only", "Only people who follow you"),
    FOLLOWING("People You Follow", "Only people you follow"),
    MUTUALS("Mutual Followers", "Only mutual followers"),
    NOBODY("Nobody", "Disable DMs completely")
}

enum class TagPermission(val displayName: String) {
    EVERYONE("Everyone"),
    FOLLOWERS("Followers Only"),
    NOBODY("Nobody")
}

/**
 * Notification settings state
 */
data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val likesEnabled: Boolean = true,
    val commentsEnabled: Boolean = true,
    val followsEnabled: Boolean = true,
    val mentionsEnabled: Boolean = true,
    val dmEnabled: Boolean = true,
    val storyRepliesEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22, // 10 PM
    val quietHoursEnd: Int = 8,    // 8 AM
    val groupNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val previewsEnabled: Boolean = true
)

/**
 * Content preferences state
 */
data class ContentPreferences(
    val autoplayVideos: AutoplayOption = AutoplayOption.WIFI_ONLY,
    val dataSaverMode: Boolean = false,
    val showSensitiveContent: Boolean = false,
    val defaultPostVisibility: PostVisibility = PostVisibility.PUBLIC,
    val hideViewCounts: Boolean = false,
    val hideLikeCounts: Boolean = false,
    val muteWords: List<String> = emptyList(),
    val preferredLanguages: List<String> = listOf("en")
)

enum class AutoplayOption(val displayName: String) {
    ALWAYS("Always"),
    WIFI_ONLY("Wi-Fi Only"),
    NEVER("Never")
}

enum class PostVisibility(val displayName: String, val icon: ImageVector) {
    PUBLIC("Public", Icons.Default.Public),
    FOLLOWERS("Followers Only", Icons.Default.People),
    CLOSE_FRIENDS("Close Friends", Icons.Default.Favorite),
    PRIVATE("Only Me", Icons.Default.Lock)
}

/**
 * Accessibility settings (neurodivergent-focused)
 */
data class AccessibilitySettings(
    val reduceMotion: Boolean = false,
    val largerText: Boolean = false,
    val screenReaderOptimized: Boolean = false,
    val simplifiedUI: Boolean = false,
    val focusMode: Boolean = false,
    val extendedTimeouts: Boolean = true,
    val hapticFeedback: Boolean = true,
    val autoCapitalization: Boolean = true,
    val dyslexiaFont: Boolean = false,
    val highContrastIcons: Boolean = false
)

/**
 * Wellbeing settings (unique to NeuroComet)
 */
data class WellbeingSettings(
    val dailyReminderEnabled: Boolean = false,
    val dailyLimitMinutes: Int = 0, // 0 = no limit
    val breakRemindersEnabled: Boolean = false,
    val breakIntervalMinutes: Int = 30,
    val showUsageStats: Boolean = true,
    val calmModeAutoEnable: Boolean = false,
    val bedtimeModeEnabled: Boolean = false,
    val bedtimeStart: Int = 22, // 10 PM
    val bedtimeEnd: Int = 7,    // 7 AM
    val positivityBoostEnabled: Boolean = true // Show encouraging messages
)

// ============================================================================
// SETTINGS MANAGER
// ============================================================================

object SocialSettingsManager {
    private const val PREFS_NAME = "NeuroComet_social_settings"

    // Privacy keys
    private const val KEY_ACCOUNT_PRIVATE = "account_private"
    private const val KEY_DM_PERMISSION = "dm_permission"
    private const val KEY_SHOW_ONLINE = "show_online"
    private const val KEY_READ_RECEIPTS = "read_receipts"
    private const val KEY_TAG_PERMISSION = "tag_permission"
    private const val KEY_HIDE_SEARCH = "hide_search"
    private const val KEY_TWO_FACTOR = "two_factor"

    // Notification keys
    private const val KEY_PUSH_ENABLED = "push_enabled"
    private const val KEY_LIKES_NOTIF = "likes_notif"
    private const val KEY_COMMENTS_NOTIF = "comments_notif"
    private const val KEY_FOLLOWS_NOTIF = "follows_notif"
    private const val KEY_MENTIONS_NOTIF = "mentions_notif"
    private const val KEY_DM_NOTIF = "dm_notif"
    private const val KEY_STORY_REPLIES_NOTIF = "story_replies_notif"
    private const val KEY_QUIET_HOURS = "quiet_hours"
    private const val KEY_QUIET_START = "quiet_start"
    private const val KEY_QUIET_END = "quiet_end"
    private const val KEY_GROUP_NOTIFS = "group_notifs"
    private const val KEY_SOUND = "sound"
    private const val KEY_VIBRATION = "vibration"
    private const val KEY_PREVIEWS = "previews"

    // Content keys
    private const val KEY_AUTOPLAY = "autoplay"
    private const val KEY_DATA_SAVER = "data_saver"
    private const val KEY_SENSITIVE_CONTENT = "sensitive_content"
    private const val KEY_DEFAULT_VISIBILITY = "default_visibility"
    private const val KEY_HIDE_VIEWS = "hide_views"
    private const val KEY_HIDE_LIKES = "hide_likes"
    private const val KEY_MUTED_WORDS = "muted_words"

    // Accessibility keys
    private const val KEY_REDUCE_MOTION = "reduce_motion"
    private const val KEY_LARGER_TEXT = "larger_text"
    private const val KEY_SCREEN_READER = "screen_reader"
    private const val KEY_SIMPLIFIED_UI = "simplified_ui"
    private const val KEY_FOCUS_MODE = "focus_mode"
    private const val KEY_EXTENDED_TIMEOUTS = "extended_timeouts"
    private const val KEY_HAPTIC = "haptic"
    private const val KEY_DYSLEXIA_FONT = "dyslexia_font"
    private const val KEY_HIGH_CONTRAST_ICONS = "high_contrast_icons"

    // Wellbeing keys
    private const val KEY_DAILY_REMINDER = "daily_reminder"
    private const val KEY_DAILY_LIMIT = "daily_limit"
    private const val KEY_BREAK_REMINDERS = "break_reminders"
    private const val KEY_BREAK_INTERVAL = "break_interval"
    private const val KEY_USAGE_STATS = "usage_stats"
    private const val KEY_CALM_AUTO = "calm_auto"
    private const val KEY_BEDTIME_MODE = "bedtime_mode"
    private const val KEY_BEDTIME_START = "bedtime_start"
    private const val KEY_BEDTIME_END = "bedtime_end"
    private const val KEY_POSITIVITY_BOOST = "positivity_boost"

    fun getPrivacySettings(context: Context): PrivacySettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PrivacySettings(
            isAccountPrivate = prefs.getBoolean(KEY_ACCOUNT_PRIVATE, false),
            allowDMsFrom = DMPermission.entries.getOrElse(prefs.getInt(KEY_DM_PERMISSION, 0)) { DMPermission.EVERYONE },
            showOnlineStatus = prefs.getBoolean(KEY_SHOW_ONLINE, true),
            showReadReceipts = prefs.getBoolean(KEY_READ_RECEIPTS, true),
            allowTagging = TagPermission.entries.getOrElse(prefs.getInt(KEY_TAG_PERMISSION, 0)) { TagPermission.EVERYONE },
            hideFromSearch = prefs.getBoolean(KEY_HIDE_SEARCH, false),
            twoFactorEnabled = prefs.getBoolean(KEY_TWO_FACTOR, false)
        )
    }

    fun savePrivacySettings(context: Context, settings: PrivacySettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_ACCOUNT_PRIVATE, settings.isAccountPrivate)
            putInt(KEY_DM_PERMISSION, settings.allowDMsFrom.ordinal)
            putBoolean(KEY_SHOW_ONLINE, settings.showOnlineStatus)
            putBoolean(KEY_READ_RECEIPTS, settings.showReadReceipts)
            putInt(KEY_TAG_PERMISSION, settings.allowTagging.ordinal)
            putBoolean(KEY_HIDE_SEARCH, settings.hideFromSearch)
            putBoolean(KEY_TWO_FACTOR, settings.twoFactorEnabled)
        }
    }

    fun getNotificationSettings(context: Context): NotificationSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return NotificationSettings(
            pushEnabled = prefs.getBoolean(KEY_PUSH_ENABLED, true),
            likesEnabled = prefs.getBoolean(KEY_LIKES_NOTIF, true),
            commentsEnabled = prefs.getBoolean(KEY_COMMENTS_NOTIF, true),
            followsEnabled = prefs.getBoolean(KEY_FOLLOWS_NOTIF, true),
            mentionsEnabled = prefs.getBoolean(KEY_MENTIONS_NOTIF, true),
            dmEnabled = prefs.getBoolean(KEY_DM_NOTIF, true),
            storyRepliesEnabled = prefs.getBoolean(KEY_STORY_REPLIES_NOTIF, true),
            quietHoursEnabled = prefs.getBoolean(KEY_QUIET_HOURS, false),
            quietHoursStart = prefs.getInt(KEY_QUIET_START, 22),
            quietHoursEnd = prefs.getInt(KEY_QUIET_END, 8),
            groupNotifications = prefs.getBoolean(KEY_GROUP_NOTIFS, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            previewsEnabled = prefs.getBoolean(KEY_PREVIEWS, true)
        )
    }

    fun saveNotificationSettings(context: Context, settings: NotificationSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_PUSH_ENABLED, settings.pushEnabled)
            putBoolean(KEY_LIKES_NOTIF, settings.likesEnabled)
            putBoolean(KEY_COMMENTS_NOTIF, settings.commentsEnabled)
            putBoolean(KEY_FOLLOWS_NOTIF, settings.followsEnabled)
            putBoolean(KEY_MENTIONS_NOTIF, settings.mentionsEnabled)
            putBoolean(KEY_DM_NOTIF, settings.dmEnabled)
            putBoolean(KEY_STORY_REPLIES_NOTIF, settings.storyRepliesEnabled)
            putBoolean(KEY_QUIET_HOURS, settings.quietHoursEnabled)
            putInt(KEY_QUIET_START, settings.quietHoursStart)
            putInt(KEY_QUIET_END, settings.quietHoursEnd)
            putBoolean(KEY_GROUP_NOTIFS, settings.groupNotifications)
            putBoolean(KEY_SOUND, settings.soundEnabled)
            putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
            putBoolean(KEY_PREVIEWS, settings.previewsEnabled)
        }
    }

    fun getContentPreferences(context: Context): ContentPreferences {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return ContentPreferences(
            autoplayVideos = AutoplayOption.entries.getOrElse(prefs.getInt(KEY_AUTOPLAY, 1)) { AutoplayOption.WIFI_ONLY },
            dataSaverMode = prefs.getBoolean(KEY_DATA_SAVER, false),
            showSensitiveContent = prefs.getBoolean(KEY_SENSITIVE_CONTENT, false),
            defaultPostVisibility = PostVisibility.entries.getOrElse(prefs.getInt(KEY_DEFAULT_VISIBILITY, 0)) { PostVisibility.PUBLIC },
            hideViewCounts = prefs.getBoolean(KEY_HIDE_VIEWS, false),
            hideLikeCounts = prefs.getBoolean(KEY_HIDE_LIKES, false),
            muteWords = prefs.getString(KEY_MUTED_WORDS, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        )
    }

    fun saveContentPreferences(context: Context, prefs: ContentPreferences) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putInt(KEY_AUTOPLAY, prefs.autoplayVideos.ordinal)
            putBoolean(KEY_DATA_SAVER, prefs.dataSaverMode)
            putBoolean(KEY_SENSITIVE_CONTENT, prefs.showSensitiveContent)
            putInt(KEY_DEFAULT_VISIBILITY, prefs.defaultPostVisibility.ordinal)
            putBoolean(KEY_HIDE_VIEWS, prefs.hideViewCounts)
            putBoolean(KEY_HIDE_LIKES, prefs.hideLikeCounts)
            putString(KEY_MUTED_WORDS, prefs.muteWords.joinToString(","))
        }
    }

    fun getAccessibilitySettings(context: Context): AccessibilitySettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AccessibilitySettings(
            reduceMotion = prefs.getBoolean(KEY_REDUCE_MOTION, false),
            largerText = prefs.getBoolean(KEY_LARGER_TEXT, false),
            screenReaderOptimized = prefs.getBoolean(KEY_SCREEN_READER, false),
            simplifiedUI = prefs.getBoolean(KEY_SIMPLIFIED_UI, false),
            focusMode = prefs.getBoolean(KEY_FOCUS_MODE, false),
            extendedTimeouts = prefs.getBoolean(KEY_EXTENDED_TIMEOUTS, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            dyslexiaFont = prefs.getBoolean(KEY_DYSLEXIA_FONT, false),
            highContrastIcons = prefs.getBoolean(KEY_HIGH_CONTRAST_ICONS, false)
        )
    }

    fun saveAccessibilitySettings(context: Context, settings: AccessibilitySettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_REDUCE_MOTION, settings.reduceMotion)
            putBoolean(KEY_LARGER_TEXT, settings.largerText)
            putBoolean(KEY_SCREEN_READER, settings.screenReaderOptimized)
            putBoolean(KEY_SIMPLIFIED_UI, settings.simplifiedUI)
            putBoolean(KEY_FOCUS_MODE, settings.focusMode)
            putBoolean(KEY_EXTENDED_TIMEOUTS, settings.extendedTimeouts)
            putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            putBoolean(KEY_DYSLEXIA_FONT, settings.dyslexiaFont)
            putBoolean(KEY_HIGH_CONTRAST_ICONS, settings.highContrastIcons)
        }
    }

    fun getWellbeingSettings(context: Context): WellbeingSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return WellbeingSettings(
            dailyReminderEnabled = prefs.getBoolean(KEY_DAILY_REMINDER, false),
            dailyLimitMinutes = prefs.getInt(KEY_DAILY_LIMIT, 0),
            breakRemindersEnabled = prefs.getBoolean(KEY_BREAK_REMINDERS, false),
            breakIntervalMinutes = prefs.getInt(KEY_BREAK_INTERVAL, 30),
            showUsageStats = prefs.getBoolean(KEY_USAGE_STATS, true),
            calmModeAutoEnable = prefs.getBoolean(KEY_CALM_AUTO, false),
            bedtimeModeEnabled = prefs.getBoolean(KEY_BEDTIME_MODE, false),
            bedtimeStart = prefs.getInt(KEY_BEDTIME_START, 22),
            bedtimeEnd = prefs.getInt(KEY_BEDTIME_END, 7),
            positivityBoostEnabled = prefs.getBoolean(KEY_POSITIVITY_BOOST, true)
        )
    }

    fun saveWellbeingSettings(context: Context, settings: WellbeingSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_DAILY_REMINDER, settings.dailyReminderEnabled)
            putInt(KEY_DAILY_LIMIT, settings.dailyLimitMinutes)
            putBoolean(KEY_BREAK_REMINDERS, settings.breakRemindersEnabled)
            putInt(KEY_BREAK_INTERVAL, settings.breakIntervalMinutes)
            putBoolean(KEY_USAGE_STATS, settings.showUsageStats)
            putBoolean(KEY_CALM_AUTO, settings.calmModeAutoEnable)
            putBoolean(KEY_BEDTIME_MODE, settings.bedtimeModeEnabled)
            putInt(KEY_BEDTIME_START, settings.bedtimeStart)
            putInt(KEY_BEDTIME_END, settings.bedtimeEnd)
            putBoolean(KEY_POSITIVITY_BOOST, settings.positivityBoostEnabled)
        }
    }
}

// ============================================================================
// SETTINGS SCREENS
// ============================================================================

/**
 * Privacy & Security Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getPrivacySettings(context)) }
    var showDMDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Privacy Section
            item {
                SettingsSectionHeader(
                    title = "Account Privacy",
                    icon = Icons.Default.Lock
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Private Account",
                    description = "Only approved followers can see your posts",
                    icon = Icons.Default.LockPerson,
                    isChecked = settings.isAccountPrivate,
                    onCheckedChange = {
                        settings = settings.copy(isAccountPrivate = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Hide from Search",
                    description = "Don't show your profile in search results",
                    icon = Icons.Default.SearchOff,
                    isChecked = settings.hideFromSearch,
                    onCheckedChange = {
                        settings = settings.copy(hideFromSearch = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            // Interactions Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Interactions",
                    icon = Icons.Default.People
                )
            }

            item {
                SocialSettingsSelector(
                    title = "Who can message you",
                    currentValue = settings.allowDMsFrom.displayName,
                    description = settings.allowDMsFrom.description,
                    icon = Icons.Default.Mail,
                    onClick = { showDMDialog = true }
                )
            }

            item {
                SocialSettingsSelector(
                    title = "Who can tag you",
                    currentValue = settings.allowTagging.displayName,
                    icon = Icons.Default.AlternateEmail,
                    onClick = { showTagDialog = true }
                )
            }

            // Activity Status Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Activity Status",
                    icon = Icons.Default.Visibility
                )
            }

            item {
                SocialSettingsToggle(
                    title = stringResource(R.string.social_show_online_status),
                    description = stringResource(R.string.social_show_online_status_desc),
                    icon = Icons.Default.Circle,
                    isChecked = settings.showOnlineStatus,
                    onCheckedChange = {
                        settings = settings.copy(showOnlineStatus = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Read Receipts",
                    description = "Show when you've read messages",
                    icon = Icons.Default.DoneAll,
                    isChecked = settings.showReadReceipts,
                    onCheckedChange = {
                        settings = settings.copy(showReadReceipts = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            // Security Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Security",
                    icon = Icons.Default.Security
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Two-Factor Authentication",
                    description = "Add extra security to your account",
                    icon = Icons.Default.PhonelinkLock,
                    isChecked = settings.twoFactorEnabled,
                    onCheckedChange = {
                        settings = settings.copy(twoFactorEnabled = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            // Blocked & Muted Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Blocked & Muted",
                    icon = Icons.Default.Block,
                    subtitle = "Manage who you've blocked or muted"
                )
            }

            item {
                BlockedMutedSection(context = context)
            }

            // Muted Words Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Muted Words",
                    icon = Icons.Default.TextFields,
                    subtitle = "Hide posts containing specific words"
                )
            }

            item {
                MutedWordsSection(context = context)
            }

            // Data & History Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Data & History",
                    icon = Icons.Default.History,
                    subtitle = "Manage your data"
                )
            }

            item {
                DataManagementSection(context = context)
            }
        }
    }

    // DM Permission Dialog
    if (showDMDialog) {
        AlertDialog(
            onDismissRequest = { showDMDialog = false },
            title = { Text("Who can message you?") },
            text = {
                Column {
                    DMPermission.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings = settings.copy(allowDMsFrom = option)
                                    SocialSettingsManager.savePrivacySettings(context, settings)
                                    showDMDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.allowDMsFrom == option,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(option.displayName, fontWeight = FontWeight.Medium)
                                Text(
                                    option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDMDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Tag Permission Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Who can tag you?") },
            text = {
                Column {
                    TagPermission.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings = settings.copy(allowTagging = option)
                                    SocialSettingsManager.savePrivacySettings(context, settings)
                                    showTagDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.allowTagging == option,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(option.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Notification Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getNotificationSettings(context)) }
    var showQuietHoursSection by remember { mutableStateOf(settings.quietHoursEnabled) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Master Toggle
            item {
                SocialSettingsToggle(
                    title = "Push Notifications",
                    description = "Receive notifications on your device",
                    icon = Icons.Default.Notifications,
                    isChecked = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(pushEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            // Activity Notifications
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Activity",
                    icon = Icons.Default.Favorite
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Likes",
                    description = "When someone likes your post",
                    icon = Icons.Default.Favorite,
                    isChecked = settings.likesEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(likesEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Comments",
                    description = "When someone comments on your post",
                    icon = Icons.AutoMirrored.Filled.Comment,
                    isChecked = settings.commentsEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(commentsEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "New Followers",
                    description = "When someone follows you",
                    icon = Icons.Default.PersonAdd,
                    isChecked = settings.followsEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(followsEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Mentions",
                    description = "When someone mentions you",
                    icon = Icons.Default.AlternateEmail,
                    isChecked = settings.mentionsEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(mentionsEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Direct Messages",
                    description = "When you receive a new message",
                    icon = Icons.Default.Mail,
                    isChecked = settings.dmEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(dmEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Story Replies",
                    description = "When someone replies to your story",
                    icon = Icons.AutoMirrored.Filled.Reply,
                    isChecked = settings.storyRepliesEnabled && settings.pushEnabled,
                    enabled = settings.pushEnabled,
                    onCheckedChange = {
                        settings = settings.copy(storyRepliesEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            // Quiet Hours (Neurodivergent-friendly!)
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Quiet Hours 🌙",
                    icon = Icons.Default.Nightlight,
                    subtitle = "For when you need peace"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Enable Quiet Hours",
                    description = "Pause notifications during set times",
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    isChecked = settings.quietHoursEnabled,
                    onCheckedChange = {
                        settings = settings.copy(quietHoursEnabled = it)
                        showQuietHoursSection = it
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            // Delivery Settings
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Delivery",
                    icon = Icons.Default.SettingsApplications
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Group Notifications",
                    description = "Bundle similar notifications together",
                    icon = Icons.Default.Inbox,
                    isChecked = settings.groupNotifications,
                    onCheckedChange = {
                        settings = settings.copy(groupNotifications = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Sound",
                    description = "Play sound for notifications",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    isChecked = settings.soundEnabled,
                    onCheckedChange = {
                        settings = settings.copy(soundEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Vibration",
                    description = "Vibrate for notifications",
                    icon = Icons.Default.Vibration,
                    isChecked = settings.vibrationEnabled,
                    onCheckedChange = {
                        settings = settings.copy(vibrationEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Show Previews",
                    description = "Show message content in notifications",
                    icon = Icons.Default.Preview,
                    isChecked = settings.previewsEnabled,
                    onCheckedChange = {
                        settings = settings.copy(previewsEnabled = it)
                        SocialSettingsManager.saveNotificationSettings(context, settings)
                    }
                )
            }
        }
    }
}

/**
 * Wellbeing Settings Screen (Unique to NeuroComet!)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getWellbeingSettings(context)) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("NeuroBalance 🧠✨", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Introduction card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Your wellbeing matters 💜",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "These tools help you maintain a healthy relationship with social media. Take breaks, set boundaries, and remember: it's okay to step away.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Break Reminders
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Mindful Breaks",
                    icon = Icons.Default.Timer,
                    subtitle = "Gentle reminders to take a breather"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Break Reminders",
                    description = "Get gentle reminders to take breaks",
                    icon = Icons.Default.Pause,
                    isChecked = settings.breakRemindersEnabled,
                    onCheckedChange = {
                        settings = settings.copy(breakRemindersEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Daily Usage Reminder",
                    description = "See how long you've been on the app",
                    icon = Icons.Default.AccessTime,
                    isChecked = settings.dailyReminderEnabled,
                    onCheckedChange = {
                        settings = settings.copy(dailyReminderEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Show Usage Stats",
                    description = "Display time spent in the app",
                    icon = Icons.Default.BarChart,
                    isChecked = settings.showUsageStats,
                    onCheckedChange = {
                        settings = settings.copy(showUsageStats = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }

            // Calm Mode
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Calm Mode 🌊",
                    icon = Icons.Default.Spa,
                    subtitle = "For when you need extra peace"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Auto-Enable Calm Mode",
                    description = "Automatically reduce stimulation at night",
                    icon = Icons.Default.DarkMode,
                    isChecked = settings.calmModeAutoEnable,
                    onCheckedChange = {
                        settings = settings.copy(calmModeAutoEnable = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Bedtime Mode",
                    description = "Limit features and reduce brightness at bedtime",
                    icon = Icons.Default.Nightlight,
                    isChecked = settings.bedtimeModeEnabled,
                    onCheckedChange = {
                        settings = settings.copy(bedtimeModeEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }

            // Positivity
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Positivity",
                    icon = Icons.Default.Favorite,
                    subtitle = "Little boosts throughout the day"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Positivity Boost",
                    description = "Show encouraging messages and affirmations",
                    icon = Icons.Default.Favorite,
                    isChecked = settings.positivityBoostEnabled,
                    onCheckedChange = {
                        settings = settings.copy(positivityBoostEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }
        }
    }
}

/**
 * Content Preferences Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPreferencesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getContentPreferences(context)) }
    var showAutoplayDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Content & Media", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Media Settings
            item {
                SettingsSectionHeader(
                    title = "Media",
                    icon = Icons.Default.PlayCircle
                )
            }

            item {
                SocialSettingsSelector(
                    title = "Autoplay Videos",
                    currentValue = settings.autoplayVideos.displayName,
                    icon = Icons.Default.PlayArrow,
                    onClick = { showAutoplayDialog = true }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Data Saver Mode",
                    description = "Reduce image quality to save data",
                    icon = Icons.Default.SaveAlt,
                    isChecked = settings.dataSaverMode,
                    onCheckedChange = {
                        settings = settings.copy(dataSaverMode = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }

            // Posting Defaults
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Posting",
                    icon = Icons.Default.Create
                )
            }

            item {
                SocialSettingsSelector(
                    title = "Default Post Visibility",
                    currentValue = settings.defaultPostVisibility.displayName,
                    icon = settings.defaultPostVisibility.icon,
                    onClick = { showVisibilityDialog = true }
                )
            }

            // Feed Preferences
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Feed Preferences",
                    icon = Icons.Default.Tune,
                    subtitle = "Control what you see"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Hide View Counts",
                    description = "Don't show view counts on posts",
                    icon = Icons.Default.VisibilityOff,
                    isChecked = settings.hideViewCounts,
                    onCheckedChange = {
                        settings = settings.copy(hideViewCounts = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Hide Like Counts",
                    description = "Don't show like counts on posts",
                    icon = Icons.Default.FavoriteBorder,
                    isChecked = settings.hideLikeCounts,
                    onCheckedChange = {
                        settings = settings.copy(hideLikeCounts = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Show Sensitive Content",
                    description = "Display content that may be sensitive",
                    icon = Icons.Default.Warning,
                    isChecked = settings.showSensitiveContent,
                    onCheckedChange = {
                        settings = settings.copy(showSensitiveContent = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }
        }
    }

    // Autoplay Dialog
    if (showAutoplayDialog) {
        AlertDialog(
            onDismissRequest = { showAutoplayDialog = false },
            title = { Text("Autoplay Videos") },
            text = {
                Column {
                    AutoplayOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings = settings.copy(autoplayVideos = option)
                                    SocialSettingsManager.saveContentPreferences(context, settings)
                                    showAutoplayDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.autoplayVideos == option,
                                onClick = null
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(option.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoplayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Visibility Dialog
    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = { Text("Default Post Visibility") },
            text = {
                Column {
                    PostVisibility.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings = settings.copy(defaultPostVisibility = option)
                                    SocialSettingsManager.saveContentPreferences(context, settings)
                                    showVisibilityDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.defaultPostVisibility == option,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(option.icon, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(option.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVisibilityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Accessibility Settings Screen (Neurodivergent-focused!)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getAccessibilitySettings(context)) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Accessibility", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Motion & Animation
            item {
                SettingsSectionHeader(
                    title = "Motion & Animation",
                    icon = Icons.Default.Animation,
                    subtitle = "For sensory sensitivities"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Reduce Motion",
                    description = "Minimize animations and movement",
                    icon = Icons.Default.Speed,
                    isChecked = settings.reduceMotion,
                    onCheckedChange = {
                        settings = settings.copy(reduceMotion = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Haptic Feedback",
                    description = "Feel vibrations for interactions",
                    icon = Icons.Default.Vibration,
                    isChecked = settings.hapticFeedback,
                    onCheckedChange = {
                        settings = settings.copy(hapticFeedback = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            // Display
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Display",
                    icon = Icons.Default.Visibility,
                    subtitle = "Visual adjustments"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Larger Text",
                    description = "Increase text size throughout the app",
                    icon = Icons.Default.FormatSize,
                    isChecked = settings.largerText,
                    onCheckedChange = {
                        settings = settings.copy(largerText = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Dyslexia-Friendly Font",
                    description = "Use a font designed for easier reading",
                    icon = Icons.Default.TextFormat,
                    isChecked = settings.dyslexiaFont,
                    onCheckedChange = {
                        settings = settings.copy(dyslexiaFont = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "High Contrast Icons",
                    description = "Make icons more visible",
                    icon = Icons.Default.Contrast,
                    isChecked = settings.highContrastIcons,
                    onCheckedChange = {
                        settings = settings.copy(highContrastIcons = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            // Cognitive Support
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Cognitive Support",
                    icon = Icons.Default.Lightbulb,
                    subtitle = "Reduce cognitive load"
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Simplified UI",
                    description = "Hide non-essential elements",
                    icon = Icons.Default.Settings,
                    isChecked = settings.simplifiedUI,
                    onCheckedChange = {
                        settings = settings.copy(simplifiedUI = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Focus Mode",
                    description = "Reduce distractions in the feed",
                    icon = Icons.Default.FilterList,
                    isChecked = settings.focusMode,
                    onCheckedChange = {
                        settings = settings.copy(focusMode = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Extended Timeouts",
                    description = "More time to read and respond",
                    icon = Icons.Default.Timer,
                    isChecked = settings.extendedTimeouts,
                    onCheckedChange = {
                        settings = settings.copy(extendedTimeouts = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            // Screen Reader
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Screen Reader",
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                )
            }

            item {
                SocialSettingsToggle(
                    title = "Screen Reader Optimized",
                    description = "Optimize layout for screen readers",
                    icon = Icons.Default.Accessibility,
                    isChecked = settings.screenReaderOptimized,
                    onCheckedChange = {
                        settings = settings.copy(screenReaderOptimized = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }
        }
    }
}

// ============================================================================
// HELPER COMPONENTS
// ============================================================================

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SocialSettingsToggle(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                contentDescription = "$title, $description, ${if (isChecked) "enabled" else "disabled"}"
            }
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .alpha(alpha),
            tint = if (isChecked && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
fun SocialSettingsSelector(
    title: String,
    currentValue: String,
    icon: ImageVector,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = currentValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

private fun Modifier.alpha(alphaValue: Float): Modifier =
    this.graphicsLayer { alpha = alphaValue }

// ============================================================================
// ANIMATION SETTINGS SCREEN
// ============================================================================

/**
 * Animation Settings Screen
 * Allows users to disable animations for sensory sensitivities.
 * Includes a master toggle that grays out individual settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val themeState by themeViewModel.themeState.collectAsState()
    val animSettings = themeState.animationSettings
    val allDisabled = animSettings.disableAllAnimations

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Animation Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Introduction card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Animation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Motion & Animation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Some people are sensitive to motion and animations. " +
                            "Use these settings to reduce or disable animations throughout the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Master Toggle
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allDisabled)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { themeViewModel.setDisableAllAnimations(!allDisabled) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Disable All Animations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (allDisabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (allDisabled)
                                        "All animations are currently disabled"
                                    else
                                        "Turn off all motion and animations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = allDisabled,
                                onCheckedChange = { themeViewModel.setDisableAllAnimations(it) }
                            )
                        }

                        if (allDisabled) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "💡 Individual settings below are disabled when this is on",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Individual Animation Toggles
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Individual Controls",
                    icon = Icons.Default.Tune,
                    subtitle = if (allDisabled) "Disabled (master toggle is on)" else "Fine-tune specific animations"
                )
            }

            // Logo Animations
            item {
                AnimationToggleItem(
                    title = "Logo Animations",
                    description = "Rainbow infinity symbol, shimmer text effects",
                    icon = Icons.Default.AutoAwesome,
                    isDisabled = animSettings.disableLogoAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableLogoAnimations(it) }
                )
            }

            // Story Animations
            item {
                AnimationToggleItem(
                    title = "Story Circle Animations",
                    description = "Spinning rainbow borders on unviewed stories",
                    icon = Icons.Default.Circle,
                    isDisabled = animSettings.disableStoryAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableStoryAnimations(it) }
                )
            }

            // Feed Animations
            item {
                AnimationToggleItem(
                    title = "Feed Animations",
                    description = "Post card transitions, like heart effects",
                    icon = Icons.Default.ViewStream,
                    isDisabled = animSettings.disableFeedAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableFeedAnimations(it) }
                )
            }

            // Transition Animations
            item {
                AnimationToggleItem(
                    title = "Screen Transitions",
                    description = "Animations when navigating between screens",
                    icon = Icons.Default.SwapHoriz,
                    isDisabled = animSettings.disableTransitionAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableTransitionAnimations(it) }
                )
            }

            // Button Animations
            item {
                AnimationToggleItem(
                    title = "Button Animations",
                    description = "Press effects and ripples on buttons",
                    icon = Icons.Default.TouchApp,
                    isDisabled = animSettings.disableButtonAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableButtonAnimations(it) }
                )
            }

            // Loading Animations
            item {
                AnimationToggleItem(
                    title = "Loading Animations",
                    description = "Spinning loaders and skeleton screens",
                    icon = Icons.Default.Refresh,
                    isDisabled = animSettings.disableLoadingAnimations,
                    isGrayedOut = allDisabled,
                    onToggle = { themeViewModel.setDisableLoadingAnimations(it) }
                )
            }

            // Reset button
            item { Spacer(Modifier.height(16.dp)) }
            item {
                OutlinedButton(
                    onClick = {
                        themeViewModel.setDisableAllAnimations(false)
                        themeViewModel.setDisableLogoAnimations(false)
                        themeViewModel.setDisableStoryAnimations(false)
                        themeViewModel.setDisableFeedAnimations(false)
                        themeViewModel.setDisableTransitionAnimations(false)
                        themeViewModel.setDisableButtonAnimations(false)
                        themeViewModel.setDisableLoadingAnimations(false)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset to Defaults (All Animations On)")
                }
            }
        }
    }
}

@Composable
private fun AnimationToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    isDisabled: Boolean,
    isGrayedOut: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val alpha = if (isGrayedOut) 0.4f else 1f
    val effectiveDisabled = isGrayedOut || isDisabled

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable(enabled = !isGrayedOut) { onToggle(!isDisabled) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (effectiveDisabled)
                MaterialTheme.colorScheme.error.copy(alpha = alpha)
            else
                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        Switch(
            checked = isDisabled,
            onCheckedChange = { if (!isGrayedOut) onToggle(it) },
            enabled = !isGrayedOut
        )
    }
}

// ============================================================================
// PRIVACY MANAGEMENT COMPONENTS
// ============================================================================

/**
 * Section for managing blocked and muted users
 */
@Composable
private fun BlockedMutedSection(context: android.content.Context) {
    var blockedUsers by remember { mutableStateOf(PrivacyManager.getBlockedUsers(context)) }
    var mutedUsers by remember { mutableStateOf(PrivacyManager.getMutedUsers(context)) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showMutedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Blocked Users
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBlockedDialog = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Blocked Users",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${blockedUsers.size} users blocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        // Muted Users
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMutedDialog = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Muted Users",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${mutedUsers.size} users muted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    // Blocked Users Dialog
    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            title = { Text("Blocked Users") },
            text = {
                if (blockedUsers.isEmpty()) {
                    Text(
                        "You haven't blocked anyone yet.\n\nBlocked users can't see your posts or message you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(blockedUsers.toList()) { userId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(userId, style = MaterialTheme.typography.bodyMedium)
                                TextButton(
                                    onClick = {
                                        PrivacyManager.unblockUser(context, userId)
                                        blockedUsers = PrivacyManager.getBlockedUsers(context)
                                    }
                                ) {
                                    Text("Unblock", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Muted Users Dialog
    if (showMutedDialog) {
        AlertDialog(
            onDismissRequest = { showMutedDialog = false },
            title = { Text("Muted Users") },
            text = {
                if (mutedUsers.isEmpty()) {
                    Text(
                        "You haven't muted anyone yet.\n\nMuted users' posts won't appear in your feed, but they can still follow you.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mutedUsers.toList()) { userId ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(userId, style = MaterialTheme.typography.bodyMedium)
                                TextButton(
                                    onClick = {
                                        PrivacyManager.unmuteUser(context, userId)
                                        mutedUsers = PrivacyManager.getMutedUsers(context)
                                    }
                                ) {
                                    Text("Unmute", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMutedDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Section for managing muted words
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MutedWordsSection(context: android.content.Context) {
    var mutedWords by remember { mutableStateOf(PrivacyManager.getMutedWords(context)) }
    var newWord by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Current muted words count
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${mutedWords.size} words muted",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Button(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Word")
                    }
                }

                if (mutedWords.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Posts containing these words will be hidden:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    // Display muted words as chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mutedWords.forEach { word ->
                            InputChip(
                                selected = false,
                                onClick = {
                                    PrivacyManager.removeMutedWord(context, word)
                                    mutedWords = PrivacyManager.getMutedWords(context)
                                },
                                label = { Text(word) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Word Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newWord = ""
            },
            title = { Text("Add Muted Word") },
            text = {
                Column {
                    Text(
                        "Posts containing this word will be hidden from your feed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        label = { Text("Word or phrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newWord.isNotBlank()) {
                            PrivacyManager.addMutedWord(context, newWord)
                            mutedWords = PrivacyManager.getMutedWords(context)
                        }
                        showAddDialog = false
                        newWord = ""
                    },
                    enabled = newWord.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newWord = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Section for managing data and clearing history
 */
@Composable
private fun DataManagementSection(context: android.content.Context) {
    var showClearSearchDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clear Search History
        OutlinedButton(
            onClick = { showClearSearchDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear Search History")
        }

        // Download Data
        OutlinedButton(
            onClick = {
                android.widget.Toast.makeText(context, "Data download requested. You'll receive an email when ready.", android.widget.Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Download Your Data")
        }

        // Clear All Privacy Data (dangerous)
        OutlinedButton(
            onClick = { showClearAllDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reset All Privacy Settings")
        }
    }

    // Clear Search History Dialog
    if (showClearSearchDialog) {
        AlertDialog(
            onDismissRequest = { showClearSearchDialog = false },
            title = { Text("Clear Search History?") },
            text = {
                Text("This will delete all your search history. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ContentManager.clearSearchHistory(context)
                        showClearSearchDialog = false
                        android.widget.Toast.makeText(context, "Search history cleared", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSearchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Data Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Reset All Privacy Settings?") },
            text = {
                Text("This will:\n• Unblock all users\n• Unmute all users\n• Clear muted words\n• Clear hidden posts\n\nThis cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        PrivacyManager.resetAll(context)
                        showClearAllDialog = false
                        android.widget.Toast.makeText(context, "Privacy settings reset", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

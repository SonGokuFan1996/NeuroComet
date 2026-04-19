package com.kyilmaz.neurocomet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kyilmaz.neurocomet.ui.design.M3ETopAppBar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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
    val brailleDisplayOptimized: Boolean = false,
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
    private const val KEY_BRAILLE_DISPLAY = "braille_display"
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
    private const val KEY_DETOX_BACKUP_APPLIED = "detox_backup_applied"
    private const val KEY_DETOX_BACKUP_PUSH = "detox_backup_push"
    private const val KEY_DETOX_BACKUP_QUIET_HOURS = "detox_backup_quiet_hours"
    private const val KEY_DETOX_BACKUP_SOUND = "detox_backup_sound"
    private const val KEY_DETOX_BACKUP_VIBRATION = "detox_backup_vibration"
    private const val KEY_DETOX_BACKUP_BREAK_REMINDERS = "detox_backup_break_reminders"
    private const val KEY_DETOX_BACKUP_CALM_MODE = "detox_backup_calm_mode"

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
            brailleDisplayOptimized = prefs.getBoolean(KEY_BRAILLE_DISPLAY, false),
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
            putBoolean(KEY_BRAILLE_DISPLAY, settings.brailleDisplayOptimized)
            putBoolean(KEY_SIMPLIFIED_UI, settings.simplifiedUI)
            putBoolean(KEY_FOCUS_MODE, settings.focusMode)
            putBoolean(KEY_EXTENDED_TIMEOUTS, settings.extendedTimeouts)
            putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            putBoolean(KEY_DYSLEXIA_FONT, settings.dyslexiaFont)
            putBoolean(KEY_HIGH_CONTRAST_ICONS, settings.highContrastIcons)
        }
    }

    fun isBrailleOptimized(context: Context): Boolean {
        val settings = getAccessibilitySettings(context)
        return settings.screenReaderOptimized || settings.brailleDisplayOptimized
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

    fun applyDetoxDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notificationSettings = getNotificationSettings(context)
        val wellbeingSettings = getWellbeingSettings(context)

        prefs.edit {
            if (!prefs.getBoolean(KEY_DETOX_BACKUP_APPLIED, false)) {
                putBoolean(KEY_DETOX_BACKUP_PUSH, notificationSettings.pushEnabled)
                putBoolean(KEY_DETOX_BACKUP_QUIET_HOURS, notificationSettings.quietHoursEnabled)
                putBoolean(KEY_DETOX_BACKUP_SOUND, notificationSettings.soundEnabled)
                putBoolean(KEY_DETOX_BACKUP_VIBRATION, notificationSettings.vibrationEnabled)
                putBoolean(KEY_DETOX_BACKUP_BREAK_REMINDERS, wellbeingSettings.breakRemindersEnabled)
                putBoolean(KEY_DETOX_BACKUP_CALM_MODE, wellbeingSettings.calmModeAutoEnable)
                putBoolean(KEY_DETOX_BACKUP_APPLIED, true)
            }
        }

        saveNotificationSettings(
            context,
            notificationSettings.copy(
                pushEnabled = false,
                quietHoursEnabled = true,
                soundEnabled = false,
                vibrationEnabled = false
            )
        )
        saveWellbeingSettings(
            context,
            wellbeingSettings.copy(
                breakRemindersEnabled = true,
                calmModeAutoEnable = true
            )
        )
    }

    fun restoreDetoxDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_DETOX_BACKUP_APPLIED, false)) return

        val notificationSettings = getNotificationSettings(context)
        val wellbeingSettings = getWellbeingSettings(context)

        saveNotificationSettings(
            context,
            notificationSettings.copy(
                pushEnabled = prefs.getBoolean(KEY_DETOX_BACKUP_PUSH, true),
                quietHoursEnabled = prefs.getBoolean(KEY_DETOX_BACKUP_QUIET_HOURS, false),
                soundEnabled = prefs.getBoolean(KEY_DETOX_BACKUP_SOUND, true),
                vibrationEnabled = prefs.getBoolean(KEY_DETOX_BACKUP_VIBRATION, true)
            )
        )
        saveWellbeingSettings(
            context,
            wellbeingSettings.copy(
                breakRemindersEnabled = prefs.getBoolean(KEY_DETOX_BACKUP_BREAK_REMINDERS, false),
                calmModeAutoEnable = prefs.getBoolean(KEY_DETOX_BACKUP_CALM_MODE, false)
            )
        )

        prefs.edit {
            remove(KEY_DETOX_BACKUP_PUSH)
            remove(KEY_DETOX_BACKUP_QUIET_HOURS)
            remove(KEY_DETOX_BACKUP_SOUND)
            remove(KEY_DETOX_BACKUP_VIBRATION)
            remove(KEY_DETOX_BACKUP_BREAK_REMINDERS)
            remove(KEY_DETOX_BACKUP_CALM_MODE)
            remove(KEY_DETOX_BACKUP_APPLIED)
        }
    }
}

// ============================================================================
// SETTINGS SCREENS
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocialSettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val contentMaxWidth = settingsPaneContentMaxWidth()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            M3ETopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            content(
                Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(bottom = androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            )
        }
    }
}

/**
 * Privacy & Security Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getPrivacySettings(context)) }
    val accountStatus by authViewModel.accountStatus.collectAsState()
    var showDMDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDeletionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentAccountStatus()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.social_schedule_deletion_title)) },
            text = {
                Text(stringResource(R.string.social_schedule_deletion_desc))
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    authViewModel.scheduleAccountDeletion { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text(stringResource(R.string.social_schedule_deletion_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.social_keep_account))
                }
            }
        )
    }

    if (showCancelDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDeletionDialog = false },
            title = { Text(stringResource(R.string.social_cancel_deletion_title)) },
            text = {
                Text(stringResource(R.string.social_cancel_deletion_desc))
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDeletionDialog = false
                    authViewModel.cancelScheduledDeletion { _, message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text(stringResource(R.string.account_cancel_deletion))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDeletionDialog = false }) {
                    Text(stringResource(R.string.account_keep_scheduled))
                }
            }
        )
    }

    SocialSettingsScaffold(
        title = stringResource(R.string.social_privacy_security_title),
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Privacy Section
            item {
                SettingsSectionHeader(
                    title = stringResource(R.string.social_account_privacy),
                    icon = Icons.Default.Lock
                )
            }

            item {
                SocialSettingsToggle(
                    title = stringResource(R.string.social_private_account),
                    description = stringResource(R.string.social_private_account_desc),
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
                    title = stringResource(R.string.social_hide_search),
                    description = stringResource(R.string.social_hide_search_desc),
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
                    title = stringResource(R.string.social_interactions),
                    icon = Icons.Default.People
                )
            }

            item {
                SocialSettingsSelector(
                    title = stringResource(R.string.social_who_can_message),
                    currentValue = settings.allowDMsFrom.displayName,
                    description = settings.allowDMsFrom.description,
                    icon = Icons.Default.Mail,
                    onClick = { showDMDialog = true }
                )
            }

            item {
                SocialSettingsSelector(
                    title = stringResource(R.string.social_who_can_tag),
                    currentValue = settings.allowTagging.displayName,
                    icon = Icons.Default.AlternateEmail,
                    onClick = { showTagDialog = true }
                )
            }

            // Activity Status Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = stringResource(R.string.social_activity_status),
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
                    title = stringResource(R.string.social_read_receipts),
                    description = stringResource(R.string.social_read_receipts_desc),
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
                    title = stringResource(R.string.social_security),
                    icon = Icons.Default.Security
                )
            }

            item {
                SocialSettingsToggle(
                    title = stringResource(R.string.social_two_factor),
                    description = stringResource(R.string.social_two_factor_desc),
                    icon = Icons.Default.PhonelinkLock,
                    isChecked = settings.twoFactorEnabled,
                    onCheckedChange = {
                        settings = settings.copy(twoFactorEnabled = it)
                        SocialSettingsManager.savePrivacySettings(context, settings)
                    }
                )
            }

            // Contacts & Friends Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Contacts & Friends",
                    icon = Icons.Default.Contacts,
                    subtitle = "Find friends from your device contacts"
                )
            }

            item {
                ContactsAccessSettingsCard()
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Blocked and muted account management is available in the conversation and profile flows.", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "This screen is a summary surface for that data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                val contentPrefs = SocialSettingsManager.getContentPreferences(context)
                val mutedWordsSummary = contentPrefs.muteWords.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "No muted words yet."
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(mutedWordsSummary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Muted word editing will be expanded in a later pass.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Data & History Section
            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = stringResource(R.string.social_data_history),
                    icon = Icons.Default.History,
                    subtitle = stringResource(R.string.social_data_history_desc)
                )
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.social_data_local_hint), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Use Backup & Restore in Settings for export and recovery tools.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Account Care",
                    icon = Icons.Default.SelfImprovement,
                    subtitle = "Take a break or schedule deletion with a safety window"
                )
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (accountStatus?.hasDeletionScheduled == true)
                                "Deletion is scheduled. Sign in again within the grace period to cancel it."
                            else
                                "Need a real break? Try Detox Mode in Wellbeing before choosing account deletion.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                if (accountStatus?.hasDeletionScheduled == true) {
                                    showCancelDeletionDialog = true
                                } else {
                                    showDeleteDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (accountStatus?.hasDeletionScheduled == true)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(if (accountStatus?.hasDeletionScheduled == true) "Cancel scheduled deletion" else "Delete account")
                        }
                    }
                }
            }
        }
    }

    // DM Permission Dialog
    if (showDMDialog) {
        AlertDialog(
            onDismissRequest = { showDMDialog = false },
            title = { Text(stringResource(R.string.social_who_can_message_title)) },
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
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Tag Permission Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text(stringResource(R.string.social_who_can_tag_title)) },
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
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ContactsAccessSettingsCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasContactsPermission by remember { mutableStateOf(ContactsManager.hasContactsPermission(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var deviceContacts by remember { mutableStateOf<List<ContactsManager.DeviceContact>>(emptyList()) }
    var matchedContacts by remember { mutableStateOf<List<ContactsManager.MatchedContact>>(emptyList()) }
    var syncEnabled by remember { mutableStateOf(ContactsManager.isContactsSyncEnabled(context)) }
    var syncedCount by remember { mutableIntStateOf(ContactsManager.getSyncedContactCount(context)) }
    var lastSyncTimestamp by remember { mutableLongStateOf(ContactsManager.getLastSyncTimestamp(context)) }
    var showAllContacts by remember { mutableStateOf(false) }

    fun refreshSyncMetadata() {
        syncEnabled = ContactsManager.isContactsSyncEnabled(context)
        syncedCount = ContactsManager.getSyncedContactCount(context)
        lastSyncTimestamp = ContactsManager.getLastSyncTimestamp(context)
    }

    fun loadContacts(enableSync: Boolean = syncEnabled) {
        scope.launch {
            isLoading = true
            val snapshot = ContactsManager.loadContactsSnapshot(context)
            deviceContacts = snapshot.deviceContacts
            matchedContacts = snapshot.matchedContacts
            if (enableSync) {
                ContactsManager.setContactsSyncEnabled(context, true)
            }
            refreshSyncMetadata()
            isLoading = false
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactsPermission = granted
        if (granted) {
            loadContacts(enableSync = true)
        } else {
            deviceContacts = emptyList()
            matchedContacts = emptyList()
            Toast.makeText(context, "Contacts access was not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission && deviceContacts.isEmpty()) {
            loadContacts()
        } else if (!hasContactsPermission) {
            deviceContacts = emptyList()
            matchedContacts = emptyList()
            refreshSyncMetadata()
        }
    }

    DisposableEffect(lifecycleOwner, syncEnabled, deviceContacts) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContactsManager.hasContactsPermission(context)
                hasContactsPermission = granted
                if (granted) {
                    if (deviceContacts.isEmpty() || syncEnabled) {
                        loadContacts(enableSync = syncEnabled)
                    }
                } else {
                    deviceContacts = emptyList()
                    matchedContacts = emptyList()
                    refreshSyncMetadata()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isPermanentlyDenied = remember(hasContactsPermission) {
        isContactsPermissionPermanentlyDenied(context)
    }
    val visibleContacts = if (showAllContacts) deviceContacts else deviceContacts.take(8)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Let NeuroComet read the contacts stored on this device so it can show your contacts list and help find people you already know.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (hasContactsPermission) "Permission granted" else "Permission needed") }
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (syncEnabled) "Sync enabled" else "Sync off") }
                )
            }

            if (hasContactsPermission) {
                Text(
                    "${deviceContacts.size} contacts visible on this device • ${matchedContacts.size} possible NeuroComet matches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lastSyncTimestamp > 0L) {
                    Text(
                        "Last synced ${formatContactsSyncTimestamp(lastSyncTimestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (syncedCount > 0) {
                    Text(
                        "Last local sync included $syncedCount contacts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    if (isPermanentlyDenied) {
                        "Contacts permission was denied in Android. Open App Settings to enable it for this device."
                    } else {
                        "Grant contacts access to let the app see and list the contacts saved on this device."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        trackPermissionAsked(context, Manifest.permission.READ_CONTACTS)
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    enabled = !isLoading && !hasContactsPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (hasContactsPermission) "Allowed" else "Allow contacts")
                }

                OutlinedButton(
                    onClick = {
                        if (hasContactsPermission) {
                            loadContacts(enableSync = true)
                        } else {
                            openAppSettings(context)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (hasContactsPermission) "Refresh list" else "Open settings")
                }
            }

            if (hasContactsPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ContactsManager.setContactsSyncEnabled(context, true)
                            syncEnabled = true
                            loadContacts(enableSync = true)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Keep synced")
                    }
                    TextButton(
                        onClick = {
                            ContactsManager.clearSyncData(context)
                            syncEnabled = false
                            syncedCount = 0
                            lastSyncTimestamp = 0L
                            deviceContacts = emptyList()
                            matchedContacts = emptyList()
                            showAllContacts = false
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear local data")
                    }
                }
            }

            when {
                isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    }
                }

                hasContactsPermission && visibleContacts.isNotEmpty() -> {
                    HorizontalDivider()
                    Text(
                        if (!showAllContacts && deviceContacts.size > visibleContacts.size) {
                            "Contacts on this device • showing ${visibleContacts.size} of ${deviceContacts.size}"
                        } else {
                            "Contacts on this device"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        visibleContacts.forEach { contact ->
                            ContactPreviewRow(contact)
                        }
                    }
                    if (deviceContacts.size > 8) {
                        TextButton(onClick = { showAllContacts = !showAllContacts }) {
                            Text(if (showAllContacts) "Show fewer contacts" else "Show all ${deviceContacts.size} contacts")
                        }
                    }
                }

                hasContactsPermission -> {
                    Text(
                        "No contacts were found on this device yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactPreviewRow(contact: ContactsManager.DeviceContact) {
    val subtitle = when {
        contact.phoneNumbers.isNotEmpty() -> contact.phoneNumbers.first()
        contact.emails.isNotEmpty() -> contact.emails.first()
        else -> "No phone or email saved"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contact.displayName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.displayName, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatContactsSyncTimestamp(timestamp: Long): String {
    return try {
        DateTimeFormatter.ofPattern("MMM d, h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))
    } catch (_: Exception) {
        "recently"
    }
}

private fun isContactsPermissionPermanentlyDenied(context: Context): Boolean {
    val activity = context.findActivity() ?: return false
    return !ContactsManager.hasContactsPermission(context) &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS) &&
        context.getSharedPreferences("permission_tracking", Context.MODE_PRIVATE)
            .getBoolean("asked_${Manifest.permission.READ_CONTACTS}", false)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
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

    SocialSettingsScaffold(
        title = stringResource(R.string.social_notifications_title),
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
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
                    title = "Quiet Hours",
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

            if (showQuietHoursSection) {
                item {
                    // Start Time Picker
                    TimePickerSetting(
                        title = "Quiet Hours Start",
                        time = settings.quietHoursStart,
                        onTimeChange = { newTime: Int ->
                            settings = settings.copy(quietHoursStart = newTime)
                            SocialSettingsManager.saveNotificationSettings(context, settings)
                        },
                        icon = Icons.Default.Alarm
                    )
                }

                item {
                    // End Time Picker
                    TimePickerSetting(
                        title = "Quiet Hours End",
                        time = settings.quietHoursEnd,
                        onTimeChange = { newTime: Int ->
                            settings = settings.copy(quietHoursEnd = newTime)
                            SocialSettingsManager.saveNotificationSettings(context, settings)
                        },
                        icon = Icons.Default.AlarmOff
                    )
                }
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
 * Content Preferences Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPreferencesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getContentPreferences(context)) }

    SocialSettingsScaffold(
        title = stringResource(R.string.social_content_media_title),
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader(
                    title = "Media",
                    icon = Icons.Default.PlayCircle,
                    subtitle = "Tune playback and content intensity"
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Data Saver Mode",
                    description = "Reduce media quality and network usage",
                    icon = Icons.Default.SaveAlt,
                    isChecked = settings.dataSaverMode,
                    onCheckedChange = {
                        settings = settings.copy(dataSaverMode = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }
            item {
                SocialSettingsSelector(
                    title = "Autoplay Videos",
                    currentValue = settings.autoplayVideos.displayName,
                    icon = Icons.Default.PlayArrow,
                    description = "Tap to cycle autoplay preferences",
                    onClick = {
                        val next = when (settings.autoplayVideos) {
                            AutoplayOption.ALWAYS -> AutoplayOption.WIFI_ONLY
                            AutoplayOption.WIFI_ONLY -> AutoplayOption.NEVER
                            AutoplayOption.NEVER -> AutoplayOption.ALWAYS
                        }
                        settings = settings.copy(autoplayVideos = next)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Feed Preferences",
                    icon = Icons.Default.Tune,
                    subtitle = "Control counts and sensitive content"
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Hide View Counts",
                    description = "Remove view counts from posts",
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
                    description = "Remove like counts from posts",
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
                    description = "Allow content that may require extra care",
                    icon = Icons.Default.Warning,
                    isChecked = settings.showSensitiveContent,
                    onCheckedChange = {
                        settings = settings.copy(showSensitiveContent = it)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }
            item {
                SocialSettingsSelector(
                    title = "Default Post Visibility",
                    currentValue = settings.defaultPostVisibility.displayName,
                    icon = settings.defaultPostVisibility.icon,
                    description = "Tap to cycle post visibility presets",
                    onClick = {
                        val next = when (settings.defaultPostVisibility) {
                            PostVisibility.PUBLIC -> PostVisibility.FOLLOWERS
                            PostVisibility.FOLLOWERS -> PostVisibility.CLOSE_FRIENDS
                            PostVisibility.CLOSE_FRIENDS -> PostVisibility.PRIVATE
                            PostVisibility.PRIVATE -> PostVisibility.PUBLIC
                        }
                        settings = settings.copy(defaultPostVisibility = next)
                        SocialSettingsManager.saveContentPreferences(context, settings)
                    }
                )
            }
        }
    }
}

/**
 * Accessibility Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getAccessibilitySettings(context)) }

    SocialSettingsScaffold(
        title = stringResource(R.string.social_accessibility_title),
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader(
                    title = "Reading & Motion",
                    icon = Icons.Default.Accessibility,
                    subtitle = "Reduce sensory load and improve readability"
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Reduce Motion",
                    description = "Minimize animation and movement",
                    icon = Icons.Default.MotionPhotosOff,
                    isChecked = settings.reduceMotion,
                    onCheckedChange = {
                        settings = settings.copy(reduceMotion = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
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
                    description = "Use the accessibility reading font",
                    icon = Icons.Default.TextFields,
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
                    description = "Increase icon contrast and clarity",
                    icon = Icons.Default.Contrast,
                    isChecked = settings.highContrastIcons,
                    onCheckedChange = {
                        settings = settings.copy(highContrastIcons = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Assistive Tech",
                    icon = Icons.Default.RecordVoiceOver,
                    subtitle = "Support screen readers and braille displays"
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Screen Reader Optimized",
                    description = "Improve labels, grouping, and navigation cues",
                    icon = Icons.Default.RecordVoiceOver,
                    isChecked = settings.screenReaderOptimized,
                    onCheckedChange = {
                        settings = settings.copy(screenReaderOptimized = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Braille Display Optimized",
                    description = "Prefer text-first labels and explicit state summaries",
                    icon = Icons.Default.TouchApp,
                    isChecked = settings.brailleDisplayOptimized,
                    onCheckedChange = {
                        settings = settings.copy(brailleDisplayOptimized = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Simplified UI",
                    description = "Reduce non-essential controls and clutter",
                    icon = Icons.Default.FilterAlt,
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
                    description = "Reduce visual distractions in key flows",
                    icon = Icons.Default.CenterFocusStrong,
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
                    description = "Allow more time to read and respond",
                    icon = Icons.Default.Timer,
                    isChecked = settings.extendedTimeouts,
                    onCheckedChange = {
                        settings = settings.copy(extendedTimeouts = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Haptic Feedback",
                    description = "Keep tactile feedback for app interactions",
                    icon = Icons.Default.Vibration,
                    isChecked = settings.hapticFeedback,
                    onCheckedChange = {
                        settings = settings.copy(hapticFeedback = it)
                        SocialSettingsManager.saveAccessibilitySettings(context, settings)
                    }
                )
            }
        }
    }
}

/**
 * Wellbeing Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingSettingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(SocialSettingsManager.getWellbeingSettings(context)) }
    val accountStatus by authViewModel.accountStatus.collectAsState()
    var detoxDays by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentAccountStatus()
    }

    SocialSettingsScaffold(
        title = stringResource(R.string.social_neurobalance_title),
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader(
                    title = "Daily Reminder",
                    icon = Icons.Default.Alarm
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Enable Daily Reminder",
                    description = "Get reminded to check in with yourself",
                    icon = Icons.Default.Notifications,
                    isChecked = settings.dailyReminderEnabled,
                    onCheckedChange = {
                        settings = settings.copy(dailyReminderEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }
            if (settings.dailyReminderEnabled) {
                item {
                    TimePickerSetting(
                        title = "Reminder Time",
                        time = settings.dailyLimitMinutes,
                        onTimeChange = { newTime: Int ->
                            settings = settings.copy(dailyLimitMinutes = newTime)
                            SocialSettingsManager.saveWellbeingSettings(context, settings)
                        },
                        icon = Icons.Default.Alarm
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Usage Limit",
                    icon = Icons.Default.Timer
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Enable Usage Limit",
                    description = "Limit app usage to reduce burnout",
                    icon = Icons.Default.HourglassTop,
                    isChecked = settings.dailyLimitMinutes > 0,
                    onCheckedChange = { enabled ->
                        settings = settings.copy(dailyLimitMinutes = if (enabled) 60 else 0)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }
            if (settings.dailyLimitMinutes > 0) {
                item {
                    SliderSetting(
                        title = "Daily Limit (minutes)",
                        value = settings.dailyLimitMinutes.toFloat(),
                        onValueChange = { newValue: Float ->
                            settings = settings.copy(dailyLimitMinutes = newValue.toInt())
                            SocialSettingsManager.saveWellbeingSettings(context, settings)
                        },
                        valueRange = 0f..180f,
                        icon = Icons.Default.Timer
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Break Reminders",
                    icon = Icons.Default.BreakfastDining
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Enable Break Reminders",
                    description = "Get reminded to take breaks and stretch",
                    icon = Icons.Default.Notifications,
                    isChecked = settings.breakRemindersEnabled,
                    onCheckedChange = {
                        settings = settings.copy(breakRemindersEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }
            if (settings.breakRemindersEnabled) {
                item {
                    SliderSetting(
                        title = "Break Interval (minutes)",
                        value = settings.breakIntervalMinutes.toFloat(),
                        onValueChange = { newValue: Float ->
                            settings = settings.copy(breakIntervalMinutes = newValue.toInt())
                            SocialSettingsManager.saveWellbeingSettings(context, settings)
                        },
                        valueRange = 5f..60f,
                        icon = Icons.Default.BreakfastDining
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Detox Mode",
                    icon = Icons.Default.SelfImprovement,
                    subtitle = "Sign out and quiet the app for a while without deleting your account"
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (accountStatus?.isDetoxActive == true)
                                "Detox mode is active until ${accountStatus?.detox_until ?: "your scheduled return"}."
                            else
                                "Start a break that silences push notifications, enables quiet hours, and signs you out so the break actually sticks.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (accountStatus?.isDetoxActive == true) {
                            OutlinedButton(
                                onClick = {
                                    authViewModel.endDetoxMode { _, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            ) {
                                Text("End detox early")
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(listOf(1, 3), listOf(7, 14)).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        row.forEach { days ->
                                            FilterChip(
                                                selected = detoxDays == days,
                                                onClick = { detoxDays = days },
                                                label = { Text("$days day${if (days == 1) "" else "s"}") }
                                            )
                                        }
                                    }
                                }
                            }
                            Button(onClick = {
                                authViewModel.startDetoxMode(detoxDays) { _, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Text("Start detox")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Calm Mode",
                    icon = Icons.Default.Spa
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Auto-enable Calm Mode",
                    description = "Enable during quiet hours or based on usage",
                    icon = Icons.Default.Schedule,
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
                    description = "Reduce notifications and screen time at night",
                    icon = Icons.Default.Bedtime,
                    isChecked = settings.bedtimeModeEnabled,
                    onCheckedChange = {
                        settings = settings.copy(bedtimeModeEnabled = it)
                        SocialSettingsManager.saveWellbeingSettings(context, settings)
                    }
                )
            }
            if (settings.bedtimeModeEnabled) {
                item {
                    TimePickerSetting(
                        title = "Bedtime Start",
                        time = settings.bedtimeStart,
                        onTimeChange = { newTime: Int ->
                            settings = settings.copy(bedtimeStart = newTime)
                            SocialSettingsManager.saveWellbeingSettings(context, settings)
                        },
                        icon = Icons.Default.Bedtime
                    )
                }
                item {
                    TimePickerSetting(
                        title = "Bedtime End",
                        time = settings.bedtimeEnd,
                        onTimeChange = { newTime: Int ->
                            settings = settings.copy(bedtimeEnd = newTime)
                            SocialSettingsManager.saveWellbeingSettings(context, settings)
                        },
                        icon = Icons.Default.Bedtime
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SettingsSectionHeader(
                    title = "Positivity Boost",
                    icon = Icons.Default.EmojiEmotions
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Enable Positivity Boost",
                    description = "Receive encouraging messages and content",
                    icon = Icons.Default.Star,
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
 * Animation Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val themeState by themeViewModel.themeState.collectAsState()
    val animSettings = themeState.animationSettings

    SocialSettingsScaffold(
        title = "Animation Settings",
        onBack = onBack
    ) { contentModifier ->
        LazyColumn(
            modifier = contentModifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSectionHeader(
                    title = "Animation Controls",
                    icon = Icons.Default.Animation,
                    subtitle = "Dial motion down to match your sensory comfort"
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Disable All Animations",
                    description = "Turn off app motion wherever possible",
                    icon = Icons.Default.MotionPhotosOff,
                    isChecked = animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableAllAnimations(it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Logo Animations",
                    description = "Splash and decorative logo motion",
                    icon = Icons.Default.AutoAwesome,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableLogoAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableLogoAnimations(!it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Story Animations",
                    description = "Story progress and motion effects",
                    icon = Icons.Default.Timelapse,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableStoryAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableStoryAnimations(!it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Feed Animations",
                    description = "Feed entry, expand, and content transitions",
                    icon = Icons.Default.ViewStream,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableFeedAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableFeedAnimations(!it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Screen Transitions",
                    description = "Navigation and route transitions",
                    icon = Icons.Default.SwapHoriz,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableTransitionAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableTransitionAnimations(!it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Button Animations",
                    description = "Pressed, hover, and state-change feedback",
                    icon = Icons.Default.SmartButton,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableButtonAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableButtonAnimations(!it) }
                )
            }
            item {
                SocialSettingsToggle(
                    title = "Loading Animations",
                    description = "Progress indicators and shimmer effects",
                    icon = Icons.Default.HourglassTop,
                    isChecked = !animSettings.disableAllAnimations && !animSettings.disableLoadingAnimations,
                    enabled = !animSettings.disableAllAnimations,
                    onCheckedChange = { themeViewModel.setDisableLoadingAnimations(!it) }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 0.3.sp
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
private fun settingsPaneContentMaxWidth() =
    canonicalSettingsPaneMaxWidth()

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
            modifier = Modifier.alpha(alpha).size(24.dp),
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
        Switch(checked = isChecked, onCheckedChange = null, enabled = enabled)
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

@Composable
fun TimePickerSetting(
    title: String,
    time: Int,
    onTimeChange: (Int) -> Unit,
    icon: ImageVector
) {
    SocialSettingsSelector(
        title = title,
        currentValue = String.format(java.util.Locale.US, "%02d:00", time.coerceIn(0, 23)),
        icon = icon,
        description = "Tap to cycle through hours",
        onClick = { onTimeChange((time + 1) % 24) }
    )
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SocialSettingsSelector(
            title = title,
            currentValue = value.toInt().toString(),
            icon = icon,
            description = "Adjust with the slider below",
            onClick = {}
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

private fun Modifier.alpha(alphaValue: Float): Modifier =
    this.graphicsLayer { alpha = alphaValue }

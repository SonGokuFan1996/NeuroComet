package com.kyilmaz.neurocomet

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Central settings manager that persists all app settings across app updates.
 *
 * Uses SharedPreferences with a dedicated file that survives code updates.
 * All settings are automatically saved when changed and loaded on app startup.
 */
object SettingsManager {
    private const val PREFS_NAME = "neurocomet_persistent_settings"

    // ═══════════════════════════════════════════════════════════════
    // KEYS
    // ═══════════════════════════════════════════════════════════════

    // Theme & Appearance
    private const val KEY_THEME_MODE = "theme_mode" // light, dark, system
    private const val KEY_DYNAMIC_COLORS = "dynamic_colors"
    private const val KEY_SELECTED_THEME = "selected_theme"
    private const val KEY_AMOLED_BLACK = "amoled_black"
    private const val KEY_HIGH_CONTRAST = "high_contrast"

    // Accessibility
    private const val KEY_REDUCED_MOTION = "reduced_motion"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_SELECTED_FONT = "selected_font"
    private const val KEY_DYSLEXIA_FRIENDLY = "dyslexia_friendly"
    private const val KEY_SCREEN_READER_MODE = "screen_reader_mode"

    // Notifications
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_MESSAGE_NOTIFICATIONS = "message_notifications"
    private const val KEY_LIKE_NOTIFICATIONS = "like_notifications"
    private const val KEY_COMMENT_NOTIFICATIONS = "comment_notifications"
    private const val KEY_FOLLOW_NOTIFICATIONS = "follow_notifications"
    private const val KEY_NOTIFICATION_SOUND = "notification_sound"
    private const val KEY_NOTIFICATION_VIBRATION = "notification_vibration"

    // Privacy
    private const val KEY_PROFILE_VISIBILITY = "profile_visibility" // public, friends, private
    private const val KEY_ONLINE_STATUS_VISIBLE = "online_status_visible"
    private const val KEY_READ_RECEIPTS = "read_receipts"
    private const val KEY_TYPING_INDICATORS = "typing_indicators"
    private const val KEY_ACTIVITY_STATUS = "activity_status"

    // Content
    private const val KEY_AUTOPLAY_VIDEOS = "autoplay_videos"
    private const val KEY_DATA_SAVER = "data_saver"
    private const val KEY_HD_IMAGES = "hd_images"
    private const val KEY_NSFW_FILTER = "nsfw_filter"
    private const val KEY_CONTENT_FILTER_LEVEL = "content_filter_level"

    // Parental Controls
    private const val KEY_PARENTAL_PIN_HASH = "parental_pin_hash"
    private const val KEY_SCREEN_TIME_LIMIT = "screen_time_limit" // minutes, 0 = unlimited
    private const val KEY_BEDTIME_START = "bedtime_start" // HH:mm
    private const val KEY_BEDTIME_END = "bedtime_end" // HH:mm
    private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"

    // Language
    private const val KEY_SELECTED_LOCALE = "selected_locale"

    // Developer
    private const val KEY_DEVELOPER_MODE = "developer_mode"

    // ═══════════════════════════════════════════════════════════════
    // STATE VARIABLES (with defaults)
    // ═══════════════════════════════════════════════════════════════

    // Theme
    private var _themeMode by mutableStateOf("system")
    val themeMode: String get() = _themeMode

    private var _dynamicColorsEnabled by mutableStateOf(true)
    val dynamicColorsEnabled: Boolean get() = _dynamicColorsEnabled

    private var _selectedTheme by mutableStateOf("default")
    val selectedTheme: String get() = _selectedTheme

    private var _amoledBlack by mutableStateOf(false)
    val amoledBlack: Boolean get() = _amoledBlack

    private var _highContrast by mutableStateOf(false)
    val highContrast: Boolean get() = _highContrast

    // Accessibility
    private var _reducedMotion by mutableStateOf(false)
    val reducedMotion: Boolean get() = _reducedMotion

    private var _fontScale by mutableStateOf(1.0f)
    val fontScale: Float get() = _fontScale

    private var _selectedFont by mutableStateOf("default")
    val selectedFont: String get() = _selectedFont

    private var _dyslexiaFriendly by mutableStateOf(false)
    val dyslexiaFriendly: Boolean get() = _dyslexiaFriendly

    private var _screenReaderMode by mutableStateOf(false)
    val screenReaderMode: Boolean get() = _screenReaderMode

    // Notifications
    private var _notificationsEnabled by mutableStateOf(true)
    val notificationsEnabled: Boolean get() = _notificationsEnabled

    private var _messageNotifications by mutableStateOf(true)
    val messageNotifications: Boolean get() = _messageNotifications

    private var _likeNotifications by mutableStateOf(true)
    val likeNotifications: Boolean get() = _likeNotifications

    private var _commentNotifications by mutableStateOf(true)
    val commentNotifications: Boolean get() = _commentNotifications

    private var _followNotifications by mutableStateOf(true)
    val followNotifications: Boolean get() = _followNotifications

    private var _notificationSound by mutableStateOf(true)
    val notificationSound: Boolean get() = _notificationSound

    private var _notificationVibration by mutableStateOf(true)
    val notificationVibration: Boolean get() = _notificationVibration

    // Privacy
    private var _profileVisibility by mutableStateOf("public")
    val profileVisibility: String get() = _profileVisibility

    private var _onlineStatusVisible by mutableStateOf(true)
    val onlineStatusVisible: Boolean get() = _onlineStatusVisible

    private var _readReceipts by mutableStateOf(true)
    val readReceipts: Boolean get() = _readReceipts

    private var _typingIndicators by mutableStateOf(true)
    val typingIndicators: Boolean get() = _typingIndicators

    private var _activityStatus by mutableStateOf(true)
    val activityStatus: Boolean get() = _activityStatus

    // Content
    private var _autoplayVideos by mutableStateOf(true)
    val autoplayVideos: Boolean get() = _autoplayVideos

    private var _dataSaver by mutableStateOf(false)
    val dataSaver: Boolean get() = _dataSaver

    private var _hdImages by mutableStateOf(true)
    val hdImages: Boolean get() = _hdImages

    private var _nsfwFilter by mutableStateOf(true)
    val nsfwFilter: Boolean get() = _nsfwFilter

    private var _contentFilterLevel by mutableStateOf("moderate")
    val contentFilterLevel: String get() = _contentFilterLevel

    // Parental Controls
    private var _parentalPinHash by mutableStateOf<String?>(null)
    val parentalPinHash: String? get() = _parentalPinHash

    private var _screenTimeLimit by mutableStateOf(0) // 0 = unlimited
    val screenTimeLimit: Int get() = _screenTimeLimit

    private var _bedtimeStart by mutableStateOf("22:00")
    val bedtimeStart: String get() = _bedtimeStart

    private var _bedtimeEnd by mutableStateOf("07:00")
    val bedtimeEnd: String get() = _bedtimeEnd

    private var _bedtimeEnabled by mutableStateOf(false)
    val bedtimeEnabled: Boolean get() = _bedtimeEnabled

    // Language
    private var _selectedLocale by mutableStateOf<String?>(null)
    val selectedLocale: String? get() = _selectedLocale

    // Developer
    private var _developerMode by mutableStateOf(false)
    val developerMode: Boolean get() = _developerMode

    private var context: Context? = null
    private var isInitialized = false

    // ═══════════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Initialize settings manager. Call this from Application.onCreate or MainActivity.
     */
    fun init(ctx: Context) {
        if (isInitialized) return
        context = ctx.applicationContext
        loadAllSettings()
        isInitialized = true
    }

    private fun getPrefs(): SharedPreferences? {
        return context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun loadAllSettings() {
        val prefs = getPrefs() ?: return

        // Theme
        _themeMode = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        _dynamicColorsEnabled = prefs.getBoolean(KEY_DYNAMIC_COLORS, true)
        _selectedTheme = prefs.getString(KEY_SELECTED_THEME, "default") ?: "default"
        _amoledBlack = prefs.getBoolean(KEY_AMOLED_BLACK, false)
        _highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)

        // Accessibility
        _reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)
        _fontScale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        _selectedFont = prefs.getString(KEY_SELECTED_FONT, "default") ?: "default"
        _dyslexiaFriendly = prefs.getBoolean(KEY_DYSLEXIA_FRIENDLY, false)
        _screenReaderMode = prefs.getBoolean(KEY_SCREEN_READER_MODE, false)

        // Notifications
        _notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        _messageNotifications = prefs.getBoolean(KEY_MESSAGE_NOTIFICATIONS, true)
        _likeNotifications = prefs.getBoolean(KEY_LIKE_NOTIFICATIONS, true)
        _commentNotifications = prefs.getBoolean(KEY_COMMENT_NOTIFICATIONS, true)
        _followNotifications = prefs.getBoolean(KEY_FOLLOW_NOTIFICATIONS, true)
        _notificationSound = prefs.getBoolean(KEY_NOTIFICATION_SOUND, true)
        _notificationVibration = prefs.getBoolean(KEY_NOTIFICATION_VIBRATION, true)

        // Privacy
        _profileVisibility = prefs.getString(KEY_PROFILE_VISIBILITY, "public") ?: "public"
        _onlineStatusVisible = prefs.getBoolean(KEY_ONLINE_STATUS_VISIBLE, true)
        _readReceipts = prefs.getBoolean(KEY_READ_RECEIPTS, true)
        _typingIndicators = prefs.getBoolean(KEY_TYPING_INDICATORS, true)
        _activityStatus = prefs.getBoolean(KEY_ACTIVITY_STATUS, true)

        // Content
        _autoplayVideos = prefs.getBoolean(KEY_AUTOPLAY_VIDEOS, true)
        _dataSaver = prefs.getBoolean(KEY_DATA_SAVER, false)
        _hdImages = prefs.getBoolean(KEY_HD_IMAGES, true)
        _nsfwFilter = prefs.getBoolean(KEY_NSFW_FILTER, true)
        _contentFilterLevel = prefs.getString(KEY_CONTENT_FILTER_LEVEL, "moderate") ?: "moderate"

        // Parental Controls
        _parentalPinHash = prefs.getString(KEY_PARENTAL_PIN_HASH, null)
        _screenTimeLimit = prefs.getInt(KEY_SCREEN_TIME_LIMIT, 0)
        _bedtimeStart = prefs.getString(KEY_BEDTIME_START, "22:00") ?: "22:00"
        _bedtimeEnd = prefs.getString(KEY_BEDTIME_END, "07:00") ?: "07:00"
        _bedtimeEnabled = prefs.getBoolean(KEY_BEDTIME_ENABLED, false)

        // Language
        _selectedLocale = prefs.getString(KEY_SELECTED_LOCALE, null)

        // Developer
        _developerMode = prefs.getBoolean(KEY_DEVELOPER_MODE, false)
    }

    // ═══════════════════════════════════════════════════════════════
    // SETTERS (auto-save)
    // ═══════════════════════════════════════════════════════════════

    // Theme setters
    fun setThemeMode(value: String) {
        _themeMode = value
        getPrefs()?.edit()?.putString(KEY_THEME_MODE, value)?.apply()
    }

    fun setDynamicColors(value: Boolean) {
        _dynamicColorsEnabled = value
        getPrefs()?.edit()?.putBoolean(KEY_DYNAMIC_COLORS, value)?.apply()
    }

    fun setSelectedTheme(value: String) {
        _selectedTheme = value
        getPrefs()?.edit()?.putString(KEY_SELECTED_THEME, value)?.apply()
    }

    fun setAmoledBlack(value: Boolean) {
        _amoledBlack = value
        getPrefs()?.edit()?.putBoolean(KEY_AMOLED_BLACK, value)?.apply()
    }

    fun setHighContrast(value: Boolean) {
        _highContrast = value
        getPrefs()?.edit()?.putBoolean(KEY_HIGH_CONTRAST, value)?.apply()
    }

    // Accessibility setters
    fun setReducedMotion(value: Boolean) {
        _reducedMotion = value
        getPrefs()?.edit()?.putBoolean(KEY_REDUCED_MOTION, value)?.apply()
    }

    fun setFontScale(value: Float) {
        _fontScale = value
        getPrefs()?.edit()?.putFloat(KEY_FONT_SCALE, value)?.apply()
    }

    fun setSelectedFont(value: String) {
        _selectedFont = value
        getPrefs()?.edit()?.putString(KEY_SELECTED_FONT, value)?.apply()
    }

    fun setDyslexiaFriendly(value: Boolean) {
        _dyslexiaFriendly = value
        getPrefs()?.edit()?.putBoolean(KEY_DYSLEXIA_FRIENDLY, value)?.apply()
    }

    fun setScreenReaderMode(value: Boolean) {
        _screenReaderMode = value
        getPrefs()?.edit()?.putBoolean(KEY_SCREEN_READER_MODE, value)?.apply()
    }

    // Notification setters
    fun setNotificationsEnabled(value: Boolean) {
        _notificationsEnabled = value
        getPrefs()?.edit()?.putBoolean(KEY_NOTIFICATIONS_ENABLED, value)?.apply()
    }

    fun setMessageNotifications(value: Boolean) {
        _messageNotifications = value
        getPrefs()?.edit()?.putBoolean(KEY_MESSAGE_NOTIFICATIONS, value)?.apply()
    }

    fun setLikeNotifications(value: Boolean) {
        _likeNotifications = value
        getPrefs()?.edit()?.putBoolean(KEY_LIKE_NOTIFICATIONS, value)?.apply()
    }

    fun setCommentNotifications(value: Boolean) {
        _commentNotifications = value
        getPrefs()?.edit()?.putBoolean(KEY_COMMENT_NOTIFICATIONS, value)?.apply()
    }

    fun setFollowNotifications(value: Boolean) {
        _followNotifications = value
        getPrefs()?.edit()?.putBoolean(KEY_FOLLOW_NOTIFICATIONS, value)?.apply()
    }

    fun setNotificationSound(value: Boolean) {
        _notificationSound = value
        getPrefs()?.edit()?.putBoolean(KEY_NOTIFICATION_SOUND, value)?.apply()
    }

    fun setNotificationVibration(value: Boolean) {
        _notificationVibration = value
        getPrefs()?.edit()?.putBoolean(KEY_NOTIFICATION_VIBRATION, value)?.apply()
    }

    // Privacy setters
    fun setProfileVisibility(value: String) {
        _profileVisibility = value
        getPrefs()?.edit()?.putString(KEY_PROFILE_VISIBILITY, value)?.apply()
    }

    fun setOnlineStatusVisible(value: Boolean) {
        _onlineStatusVisible = value
        getPrefs()?.edit()?.putBoolean(KEY_ONLINE_STATUS_VISIBLE, value)?.apply()
    }

    fun setReadReceipts(value: Boolean) {
        _readReceipts = value
        getPrefs()?.edit()?.putBoolean(KEY_READ_RECEIPTS, value)?.apply()
    }

    fun setTypingIndicators(value: Boolean) {
        _typingIndicators = value
        getPrefs()?.edit()?.putBoolean(KEY_TYPING_INDICATORS, value)?.apply()
    }

    fun setActivityStatus(value: Boolean) {
        _activityStatus = value
        getPrefs()?.edit()?.putBoolean(KEY_ACTIVITY_STATUS, value)?.apply()
    }

    // Content setters
    fun setAutoplayVideos(value: Boolean) {
        _autoplayVideos = value
        getPrefs()?.edit()?.putBoolean(KEY_AUTOPLAY_VIDEOS, value)?.apply()
    }

    fun setDataSaver(value: Boolean) {
        _dataSaver = value
        getPrefs()?.edit()?.putBoolean(KEY_DATA_SAVER, value)?.apply()
    }

    fun setHdImages(value: Boolean) {
        _hdImages = value
        getPrefs()?.edit()?.putBoolean(KEY_HD_IMAGES, value)?.apply()
    }

    fun setNsfwFilter(value: Boolean) {
        _nsfwFilter = value
        getPrefs()?.edit()?.putBoolean(KEY_NSFW_FILTER, value)?.apply()
    }

    fun setContentFilterLevel(value: String) {
        _contentFilterLevel = value
        getPrefs()?.edit()?.putString(KEY_CONTENT_FILTER_LEVEL, value)?.apply()
    }

    // Parental Control setters
    fun setParentalPinHash(value: String?) {
        _parentalPinHash = value
        getPrefs()?.edit()?.apply {
            if (value == null) remove(KEY_PARENTAL_PIN_HASH) else putString(KEY_PARENTAL_PIN_HASH, value)
        }?.apply()
    }

    fun setScreenTimeLimit(value: Int) {
        _screenTimeLimit = value
        getPrefs()?.edit()?.putInt(KEY_SCREEN_TIME_LIMIT, value)?.apply()
    }

    fun setBedtimeStart(value: String) {
        _bedtimeStart = value
        getPrefs()?.edit()?.putString(KEY_BEDTIME_START, value)?.apply()
    }

    fun setBedtimeEnd(value: String) {
        _bedtimeEnd = value
        getPrefs()?.edit()?.putString(KEY_BEDTIME_END, value)?.apply()
    }

    fun setBedtimeEnabled(value: Boolean) {
        _bedtimeEnabled = value
        getPrefs()?.edit()?.putBoolean(KEY_BEDTIME_ENABLED, value)?.apply()
    }

    // Language setter
    fun setSelectedLocale(value: String?) {
        _selectedLocale = value
        getPrefs()?.edit()?.apply {
            if (value == null) remove(KEY_SELECTED_LOCALE) else putString(KEY_SELECTED_LOCALE, value)
        }?.apply()
    }

    // Developer setter
    fun setDeveloperMode(value: Boolean) {
        _developerMode = value
        getPrefs()?.edit()?.putBoolean(KEY_DEVELOPER_MODE, value)?.apply()
    }

    // ═══════════════════════════════════════════════════════════════
    // RESET
    // ═══════════════════════════════════════════════════════════════

    /**
     * Reset all settings to defaults (except parental controls which require PIN).
     */
    fun resetToDefaults(includeParentalControls: Boolean = false) {
        val prefs = getPrefs() ?: return

        prefs.edit().apply {
            // Theme
            remove(KEY_THEME_MODE)
            remove(KEY_DYNAMIC_COLORS)
            remove(KEY_SELECTED_THEME)
            remove(KEY_AMOLED_BLACK)
            remove(KEY_HIGH_CONTRAST)

            // Accessibility
            remove(KEY_REDUCED_MOTION)
            remove(KEY_FONT_SCALE)
            remove(KEY_SELECTED_FONT)
            remove(KEY_DYSLEXIA_FRIENDLY)
            remove(KEY_SCREEN_READER_MODE)

            // Notifications
            remove(KEY_NOTIFICATIONS_ENABLED)
            remove(KEY_MESSAGE_NOTIFICATIONS)
            remove(KEY_LIKE_NOTIFICATIONS)
            remove(KEY_COMMENT_NOTIFICATIONS)
            remove(KEY_FOLLOW_NOTIFICATIONS)
            remove(KEY_NOTIFICATION_SOUND)
            remove(KEY_NOTIFICATION_VIBRATION)

            // Privacy
            remove(KEY_PROFILE_VISIBILITY)
            remove(KEY_ONLINE_STATUS_VISIBLE)
            remove(KEY_READ_RECEIPTS)
            remove(KEY_TYPING_INDICATORS)
            remove(KEY_ACTIVITY_STATUS)

            // Content
            remove(KEY_AUTOPLAY_VIDEOS)
            remove(KEY_DATA_SAVER)
            remove(KEY_HD_IMAGES)
            remove(KEY_NSFW_FILTER)
            remove(KEY_CONTENT_FILTER_LEVEL)

            // Language
            remove(KEY_SELECTED_LOCALE)

            // Developer
            remove(KEY_DEVELOPER_MODE)

            if (includeParentalControls) {
                remove(KEY_PARENTAL_PIN_HASH)
                remove(KEY_SCREEN_TIME_LIMIT)
                remove(KEY_BEDTIME_START)
                remove(KEY_BEDTIME_END)
                remove(KEY_BEDTIME_ENABLED)
            }
        }.apply()

        // Reload defaults
        loadAllSettings()
    }

    /**
     * Export settings as a map (for backup/debugging).
     */
    fun exportSettings(): Map<String, Any?> {
        return mapOf(
            "themeMode" to themeMode,
            "dynamicColors" to dynamicColorsEnabled,
            "selectedTheme" to selectedTheme,
            "amoledBlack" to amoledBlack,
            "highContrast" to highContrast,
            "reducedMotion" to reducedMotion,
            "fontScale" to fontScale,
            "selectedFont" to selectedFont,
            "dyslexiaFriendly" to dyslexiaFriendly,
            "notificationsEnabled" to notificationsEnabled,
            "profileVisibility" to profileVisibility,
            "autoplayVideos" to autoplayVideos,
            "dataSaver" to dataSaver,
            "selectedLocale" to selectedLocale,
            "developerMode" to developerMode
        )
    }
}


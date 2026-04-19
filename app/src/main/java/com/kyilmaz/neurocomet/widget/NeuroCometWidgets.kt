package com.kyilmaz.neurocomet.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.kyilmaz.neurocomet.MainActivity
import com.kyilmaz.neurocomet.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * NeuroComet Home Screen Widgets
 *
 * Neurodivergent-Friendly Widget Design Philosophy:
 * - Clear, large text for easy reading
 * - High contrast options for visual sensitivity
 * - Simple, focused information to reduce cognitive load
 * - Quick actions for common self-regulation tasks
 * - Calming color schemes that don't overstimulate
 * - Spoon theory integration for energy management
 * - Stim-friendly reminders and sensory check-ins
 * - Affirmations that celebrate neurodivergent minds
 * - Body doubling and accountability features
 * - Transition warnings and time blindness helpers
 */

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT AFFIRMATIONS
// ═══════════════════════════════════════════════════════════════

private fun dailyAffirmations(context: Context): List<String> {
    val affirmations = context.resources
        .getStringArray(R.array.widget_daily_affirmations)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return affirmations.ifEmpty {
        listOf(context.getString(R.string.widget_affirmation_default_text))
    }
}

// Stim suggestions for different needs
val STIM_SUGGESTIONS = mapOf(
    "calming" to listOf("Deep breathing 🌬️", "Hand massage 🤲", "Weighted blanket 🛋️", "Slow rocking 🪑", "Soft humming 🎵"),
    "alerting" to listOf("Cold water on wrists 💧", "Crunchy snack 🥕", "Quick walk 🚶", "Bright light ☀️", "Upbeat music 🎶"),
    "focusing" to listOf("Fidget toy 🔮", "Gum chewing 🍬", "Background noise 📻", "Pressure vest 🦺", "Foot tapping 👟"),
    "grounding" to listOf("5-4-3-2-1 senses 👁️", "Cold object ❄️", "Strong scent 🌸", "Textured item 🧶", "Body scan 🧘")
)

// Extended mood options with neurodivergent-specific states
val EXTENDED_MOODS = listOf(
    "😊" to "Happy",
    "😌" to "Calm",
    "🤩" to "Hyperfocused",
    "😴" to "Low Energy",
    "😰" to "Anxious",
    "😤" to "Frustrated",
    "🥺" to "Overwhelmed",
    "🫠" to "Melting",
    "🧊" to "Shutdown",
    "🔋" to "Recharging",
    "💫" to "Stimmy",
    "🌈" to "Unmasked",
    "😶‍🌫️" to "Brain Fog",
    "🎭" to "Masking",
    "✨" to "Thriving"
)

// ═══════════════════════════════════════════════════════════════
// WIDGET PREFERENCES MANAGER
// ═══════════════════════════════════════════════════════════════

object WidgetPreferences {
    private const val PREFS_NAME = "neurocomet_widget_prefs"
    private const val KEY_ENERGY_LEVEL = "energy_level"
    private const val KEY_CURRENT_MOOD = "current_mood"
    private const val KEY_MOOD_LABEL = "mood_label"
    private const val KEY_FOCUS_MINUTES = "focus_minutes"
    private const val KEY_TIMER_RUNNING = "timer_running"
    private const val KEY_TIMER_END_TIME = "timer_end_time"
    private const val KEY_TASKS_COMPLETED = "tasks_completed"
    private const val KEY_TASKS_TOTAL = "tasks_total"
    private const val KEY_LAST_CHECK_IN = "last_check_in"
    private const val KEY_HIGH_CONTRAST = "high_contrast"

    // Neurodivergent-specific keys
    private const val KEY_SPOONS_REMAINING = "spoons_remaining"
    private const val KEY_SPOONS_TOTAL = "spoons_total"
    private const val KEY_LAST_STIM_BREAK = "last_stim_break"
    private const val KEY_STIM_BREAK_INTERVAL = "stim_break_interval"
    private const val KEY_DAILY_AFFIRMATION_INDEX = "daily_affirmation_index"
    private const val KEY_AFFIRMATION_DATE = "affirmation_date"
    private const val KEY_SENSORY_LEVEL = "sensory_level"
    private const val KEY_MASKING_LEVEL = "masking_level"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Energy level (0-100)
    fun setEnergyLevel(context: Context, level: Int) {
        getPrefs(context).edit().putInt(KEY_ENERGY_LEVEL, level.coerceIn(0, 100)).apply()
        updateAllWidgets(context)
    }

    fun getEnergyLevel(context: Context): Int {
        return getPrefs(context).getInt(KEY_ENERGY_LEVEL, 50)
    }

    // Current mood with label
    fun setCurrentMood(context: Context, mood: String, label: String = "") {
        getPrefs(context).edit()
            .putString(KEY_CURRENT_MOOD, mood)
            .putString(KEY_MOOD_LABEL, label)
            .apply()
        updateAllWidgets(context)
    }

    fun getCurrentMood(context: Context): String {
        return getPrefs(context).getString(KEY_CURRENT_MOOD, "😊") ?: "😊"
    }

    fun getCurrentMoodLabel(context: Context): String {
        return getPrefs(context).getString(KEY_MOOD_LABEL, "Happy") ?: "Happy"
    }

    // Focus timer
    fun setFocusTimer(context: Context, minutes: Int, isRunning: Boolean) {
        val endTime = if (isRunning) {
            System.currentTimeMillis() + (minutes * 60 * 1000)
        } else 0L

        getPrefs(context).edit()
            .putInt(KEY_FOCUS_MINUTES, minutes)
            .putBoolean(KEY_TIMER_RUNNING, isRunning)
            .putLong(KEY_TIMER_END_TIME, endTime)
            .apply()

        if (isRunning) {
            FocusTimerWidgetProvider.scheduleTimerCompletion(context, endTime)
        } else {
            FocusTimerWidgetProvider.cancelTimerCompletion(context)
        }

        updateAllWidgets(context)
    }

    fun completeFocusTimer(context: Context, resetMinutes: Int = 25) {
        getPrefs(context).edit()
            .putBoolean(KEY_TIMER_RUNNING, false)
            .putLong(KEY_TIMER_END_TIME, 0L)
            .putInt(KEY_FOCUS_MINUTES, resetMinutes)
            .apply()
        FocusTimerWidgetProvider.cancelTimerCompletion(context)
        updateAllWidgets(context)
    }

    fun getFocusMinutes(context: Context): Int {
        val prefs = getPrefs(context)
        val isRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false)
        if (isRunning) {
            val endTime = prefs.getLong(KEY_TIMER_END_TIME, 0)
            val remainingMs = endTime - System.currentTimeMillis()
            if (remainingMs <= 0L) {
                completeFocusTimer(context)
                return 25
            }
            return ((remainingMs + 59_999L) / 60_000L).toInt()
        }
        return prefs.getInt(KEY_FOCUS_MINUTES, 25)
    }

    fun isTimerRunning(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isRunning = prefs.getBoolean(KEY_TIMER_RUNNING, false)
        if (!isRunning) return false

        val endTime = prefs.getLong(KEY_TIMER_END_TIME, 0L)
        if (endTime <= System.currentTimeMillis()) {
            completeFocusTimer(context)
            return false
        }
        return true
    }

    fun getFocusTimerEndTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_TIMER_END_TIME, 0L)
    }

    // Tasks
    fun setTaskProgress(context: Context, completed: Int, total: Int) {
        getPrefs(context).edit()
            .putInt(KEY_TASKS_COMPLETED, completed)
            .putInt(KEY_TASKS_TOTAL, total)
            .apply()
        updateAllWidgets(context)
    }

    fun getTasksCompleted(context: Context): Int {
        return getPrefs(context).getInt(KEY_TASKS_COMPLETED, 0)
    }

    fun getTasksTotal(context: Context): Int {
        return getPrefs(context).getInt(KEY_TASKS_TOTAL, 5)
    }

    // Last check-in
    fun setLastCheckIn(context: Context) {
        getPrefs(context).edit()
            .putLong(KEY_LAST_CHECK_IN, System.currentTimeMillis())
            .apply()
        updateAllWidgets(context)
    }

    fun getLastCheckIn(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_CHECK_IN, 0)
    }

    // High contrast
    fun setHighContrast(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        updateAllWidgets(context)
    }

    fun isHighContrast(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HIGH_CONTRAST, false)
    }

    // ═══════════════════════════════════════════════════════════
    // SPOON THEORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    fun setSpoonsRemaining(context: Context, spoons: Int) {
        getPrefs(context).edit().putInt(KEY_SPOONS_REMAINING, spoons.coerceAtLeast(0)).apply()
        updateAllWidgets(context)
    }

    fun getSpoonsRemaining(context: Context): Int {
        return getPrefs(context).getInt(KEY_SPOONS_REMAINING, 12)
    }

    fun setSpoonsTotal(context: Context, total: Int) {
        getPrefs(context).edit().putInt(KEY_SPOONS_TOTAL, total.coerceIn(1, 24)).apply()
        updateAllWidgets(context)
    }

    fun getSpoonsTotal(context: Context): Int {
        return getPrefs(context).getInt(KEY_SPOONS_TOTAL, 12)
    }

    fun useSpoon(context: Context, count: Int = 1) {
        val current = getSpoonsRemaining(context)
        setSpoonsRemaining(context, current - count)
    }

    fun resetSpoons(context: Context) {
        val total = getSpoonsTotal(context)
        setSpoonsRemaining(context, total)
    }

    // ═══════════════════════════════════════════════════════════
    // STIM BREAK TRACKING
    // ═══════════════════════════════════════════════════════════

    fun setLastStimBreak(context: Context) {
        getPrefs(context).edit()
            .putLong(KEY_LAST_STIM_BREAK, System.currentTimeMillis())
            .apply()
        updateAllWidgets(context)
    }

    fun getLastStimBreak(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_STIM_BREAK, 0L)
    }

    fun getMinutesSinceStimBreak(context: Context): Int {
        val last = getLastStimBreak(context)
        if (last <= 0L) return 0
        return ((System.currentTimeMillis() - last) / 60000).toInt()
    }

    fun getStimBreakInterval(context: Context): Int {
        return getPrefs(context).getInt(KEY_STIM_BREAK_INTERVAL, 45)
    }

    fun needsStimBreak(context: Context): Boolean {
        return getMinutesSinceStimBreak(context) >= getStimBreakInterval(context)
    }

    // ═══════════════════════════════════════════════════════════
    // DAILY AFFIRMATION
    // ═══════════════════════════════════════════════════════════

    fun getDailyAffirmation(context: Context): String {
        val prefs = getPrefs(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val savedDate = prefs.getString(KEY_AFFIRMATION_DATE, "")
        val affirmations = dailyAffirmations(context)

        return if (savedDate == today) {
            val index = prefs.getInt(KEY_DAILY_AFFIRMATION_INDEX, 0)
            affirmations.getOrElse(index) { affirmations.first() }
        } else {
            val newIndex = (0 until affirmations.size).random()
            prefs.edit()
                .putString(KEY_AFFIRMATION_DATE, today)
                .putInt(KEY_DAILY_AFFIRMATION_INDEX, newIndex)
                .apply()
            affirmations[newIndex]
        }
    }

    fun getNextAffirmation(context: Context): String {
        val prefs = getPrefs(context)
        val affirmations = dailyAffirmations(context)
        val currentIndex = prefs.getInt(KEY_DAILY_AFFIRMATION_INDEX, 0)
        val newIndex = (currentIndex + 1) % affirmations.size
        prefs.edit().putInt(KEY_DAILY_AFFIRMATION_INDEX, newIndex).apply()
        updateAllWidgets(context)
        return affirmations[newIndex]
    }

    // ═══════════════════════════════════════════════════════════
    // SENSORY & MASKING LEVELS
    // ═══════════════════════════════════════════════════════════

    fun setSensoryLevel(context: Context, level: Int) {
        getPrefs(context).edit().putInt(KEY_SENSORY_LEVEL, level.coerceIn(0, 100)).apply()
        updateAllWidgets(context)
    }

    fun getSensoryLevel(context: Context): Int {
        return getPrefs(context).getInt(KEY_SENSORY_LEVEL, 50)
    }

    fun setMaskingLevel(context: Context, level: Int) {
        getPrefs(context).edit().putInt(KEY_MASKING_LEVEL, level.coerceIn(0, 100)).apply()
        updateAllWidgets(context)
    }

    fun getMaskingLevel(context: Context): Int {
        return getPrefs(context).getInt(KEY_MASKING_LEVEL, 0)
    }

    // Update all widgets
    fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)

        // Update all widget types
        listOf(
            EnergyWidgetProvider::class.java,
            FocusTimerWidgetProvider::class.java,
            MoodWidgetProvider::class.java,
            SpoonWidgetProvider::class.java,
            AffirmationWidgetProvider::class.java,
            StimBreakWidgetProvider::class.java
        ).forEach { providerClass ->
            try {
                val component = ComponentName(context, providerClass)
                val ids = manager.getAppWidgetIds(component)
                if (ids.isNotEmpty()) {
                    val intent = Intent(context, providerClass).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                // Widget not registered yet
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ENERGY LEVEL WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Widget showing current energy/social battery level
 * Quick tap to update energy level
 */
class EnergyWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ENERGY_UP = "com.kyilmaz.neurocomet.ENERGY_UP"
        const val ACTION_ENERGY_DOWN = "com.kyilmaz.neurocomet.ENERGY_DOWN"
        const val ACTION_OPEN_APP = "com.kyilmaz.neurocomet.OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ENERGY_UP -> {
                val current = WidgetPreferences.getEnergyLevel(context)
                WidgetPreferences.setEnergyLevel(context, (current + 10).coerceAtMost(100))
            }
            ACTION_ENERGY_DOWN -> {
                val current = WidgetPreferences.getEnergyLevel(context)
                WidgetPreferences.setEnergyLevel(context, (current - 10).coerceAtLeast(0))
            }
            ACTION_OPEN_APP -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val energyLevel = WidgetPreferences.getEnergyLevel(context)
        val isHighContrast = WidgetPreferences.isHighContrast(context)

        val views = RemoteViews(context.packageName, R.layout.widget_energy)

        // Set energy level text
        views.setTextViewText(R.id.widget_energy_level, context.getString(R.string.widget_energy_percent_format, energyLevel))
        views.setTextViewText(R.id.widget_energy_label, getEnergyLabel(context, energyLevel))

        // Set emoji based on level
        views.setTextViewText(R.id.widget_energy_emoji, getEnergyEmoji(energyLevel))

        // Set progress (if using ProgressBar)
        views.setProgressBar(R.id.widget_energy_progress, 100, energyLevel, false)

        // Set click actions
        views.setOnClickPendingIntent(
            R.id.widget_energy_up,
            createPendingIntent(context, ACTION_ENERGY_UP)
        )
        views.setOnClickPendingIntent(
            R.id.widget_energy_down,
            createPendingIntent(context, ACTION_ENERGY_DOWN)
        )
        views.setOnClickPendingIntent(
            R.id.widget_energy_container,
            createPendingIntent(context, ACTION_OPEN_APP)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getEnergyEmoji(level: Int): String {
        return when {
            level >= 80 -> "⚡"
            level >= 60 -> "🔋"
            level >= 40 -> "🪫"
            level >= 20 -> "😴"
            else -> "💤"
        }
    }

    private fun getEnergyLabel(context: Context, level: Int): String {
        return when {
            level >= 80 -> context.getString(R.string.widget_energy_state_high)
            level >= 60 -> context.getString(R.string.widget_energy_state_good)
            level >= 40 -> context.getString(R.string.widget_energy_state_moderate)
            level >= 20 -> context.getString(R.string.widget_energy_state_low_battery)
            else -> context.getString(R.string.widget_energy_state_recharging)
        }
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, EnergyWidgetProvider::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
    }
}

// ═══════════════════════════════════════════════════════════════
// FOCUS TIMER WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Widget for focus/pomodoro timer
 * Start/pause timer directly from home screen
 */
class FocusTimerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_START_TIMER = "com.kyilmaz.neurocomet.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.kyilmaz.neurocomet.PAUSE_TIMER"
        const val ACTION_RESET_TIMER = "com.kyilmaz.neurocomet.RESET_TIMER"
        const val ACTION_TIMER_FINISHED = "com.kyilmaz.neurocomet.TIMER_FINISHED"

        fun scheduleTimerCompletion(context: Context, endTimeMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerAtMillis = endTimeMillis.coerceAtLeast(System.currentTimeMillis())
            val pendingIntent = createBroadcastPendingIntent(context, ACTION_TIMER_FINISHED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        fun cancelTimerCompletion(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            alarmManager.cancel(createBroadcastPendingIntent(context, ACTION_TIMER_FINISHED))
        }

        private fun createBroadcastPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, FocusTimerWidgetProvider::class.java).apply {
                this.action = action
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_START_TIMER -> {
                val minutes = WidgetPreferences.getFocusMinutes(context)
                WidgetPreferences.setFocusTimer(context, minutes, true)
            }
            ACTION_PAUSE_TIMER -> {
                val minutes = WidgetPreferences.getFocusMinutes(context)
                WidgetPreferences.setFocusTimer(context, minutes, false)
            }
            ACTION_RESET_TIMER -> {
                WidgetPreferences.setFocusTimer(context, 25, false)
            }
            ACTION_TIMER_FINISHED -> {
                WidgetPreferences.completeFocusTimer(context)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val minutes = WidgetPreferences.getFocusMinutes(context)
        val isRunning = WidgetPreferences.isTimerRunning(context)
        val endTime = WidgetPreferences.getFocusTimerEndTime(context)
        val remainingMs = if (isRunning) {
            (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            minutes * 60_000L
        }

        val views = RemoteViews(context.packageName, R.layout.widget_focus_timer)

        // Set time display
        views.setChronometer(
            R.id.widget_timer_minutes,
            SystemClock.elapsedRealtime() + remainingMs,
            null,
            isRunning
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            views.setChronometerCountDown(R.id.widget_timer_minutes, true)
        }
        views.setTextViewText(R.id.widget_timer_label, context.getString(if (isRunning) R.string.widget_focus_active else R.string.widget_focus_ready))

        // Set status emoji
        views.setTextViewText(
            R.id.widget_timer_emoji,
            if (isRunning) "🎯" else "⏱️"
        )

        // Set button text/action
        val buttonAction = if (isRunning) ACTION_PAUSE_TIMER else ACTION_START_TIMER
        views.setTextViewText(
            R.id.widget_timer_button,
            context.getString(if (isRunning) R.string.widget_focus_pause else R.string.widget_focus_start)
        )
        views.setOnClickPendingIntent(
            R.id.widget_timer_button,
            createPendingIntent(context, buttonAction)
        )

        // Reset button
        views.setOnClickPendingIntent(
            R.id.widget_timer_reset,
            createPendingIntent(context, ACTION_RESET_TIMER)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        return createBroadcastPendingIntent(context, action)
    }
}

// ═══════════════════════════════════════════════════════════════
// MOOD WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Quick mood tracking widget
 * Tap to select current mood
 */
class MoodWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_MOOD_HAPPY = "com.kyilmaz.neurocomet.MOOD_HAPPY"
        const val ACTION_MOOD_CALM = "com.kyilmaz.neurocomet.MOOD_CALM"
        const val ACTION_MOOD_SAD = "com.kyilmaz.neurocomet.MOOD_SAD"
        const val ACTION_MOOD_ANXIOUS = "com.kyilmaz.neurocomet.MOOD_ANXIOUS"
        const val ACTION_MOOD_TIRED = "com.kyilmaz.neurocomet.MOOD_TIRED"

        private val moodActions = mapOf(
            ACTION_MOOD_HAPPY to "😊",
            ACTION_MOOD_CALM to "😌",
            ACTION_MOOD_SAD to "😢",
            ACTION_MOOD_ANXIOUS to "😰",
            ACTION_MOOD_TIRED to "😴"
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        moodActions[intent.action]?.let { mood ->
            WidgetPreferences.setCurrentMood(context, mood)
            WidgetPreferences.setLastCheckIn(context)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val currentMood = WidgetPreferences.getCurrentMood(context)
        val lastCheckIn = WidgetPreferences.getLastCheckIn(context)

        val views = RemoteViews(context.packageName, R.layout.widget_mood)

        // Set current mood
        views.setTextViewText(R.id.widget_mood_current, currentMood)

        // Set last check-in time
        val checkInText = if (lastCheckIn > 0) {
            val formattedTime = android.text.format.DateFormat.getTimeFormat(context).format(Date(lastCheckIn))
            context.getString(R.string.widget_mood_last_checkin, formattedTime)
        } else {
            context.getString(R.string.widget_daily_checkin)
        }
        views.setTextViewText(R.id.widget_mood_time, checkInText)

        // Set mood button click handlers
        views.setOnClickPendingIntent(
            R.id.widget_mood_happy,
            createPendingIntent(context, ACTION_MOOD_HAPPY)
        )
        views.setOnClickPendingIntent(
            R.id.widget_mood_calm,
            createPendingIntent(context, ACTION_MOOD_CALM)
        )
        views.setOnClickPendingIntent(
            R.id.widget_mood_sad,
            createPendingIntent(context, ACTION_MOOD_SAD)
        )
        views.setOnClickPendingIntent(
            R.id.widget_mood_anxious,
            createPendingIntent(context, ACTION_MOOD_ANXIOUS)
        )
        views.setOnClickPendingIntent(
            R.id.widget_mood_tired,
            createPendingIntent(context, ACTION_MOOD_TIRED)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, MoodWidgetProvider::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
    }
}

// ═══════════════════════════════════════════════════════════════
// SPOON WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Widget for tracking spoon theory energy levels
 * Visual representation of available energy (spoons)
 */
class SpoonWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_USE_SPOON = "com.kyilmaz.neurocomet.USE_SPOON"
        const val ACTION_RESET_SPOONS = "com.kyilmaz.neurocomet.RESET_SPOONS"
        const val ACTION_OPEN_APP = "com.kyilmaz.neurocomet.OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_USE_SPOON -> {
                WidgetPreferences.useSpoon(context)
            }
            ACTION_RESET_SPOONS -> {
                WidgetPreferences.resetSpoons(context)
            }
            ACTION_OPEN_APP -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val spoonsRemaining = WidgetPreferences.getSpoonsRemaining(context)
        val spoonsTotal = WidgetPreferences.getSpoonsTotal(context)
        val isHighContrast = WidgetPreferences.isHighContrast(context)

        val views = RemoteViews(context.packageName, R.layout.widget_spoon)

        // Set spoon count text
        views.setTextViewText(R.id.widget_spoon_count, "$spoonsRemaining / $spoonsTotal")

        // Set progress (if using ProgressBar)
        views.setProgressBar(R.id.widget_spoon_progress, spoonsTotal, spoonsRemaining, false)

        // Set click actions
        views.setOnClickPendingIntent(
            R.id.widget_spoon_use,
            createPendingIntent(context, ACTION_USE_SPOON)
        )
        views.setOnClickPendingIntent(
            R.id.widget_spoon_reset,
            createPendingIntent(context, ACTION_RESET_SPOONS)
        )
        views.setOnClickPendingIntent(
            R.id.widget_spoon_container,
            createPendingIntent(context, ACTION_OPEN_APP)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, SpoonWidgetProvider::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
    }
}

// ═══════════════════════════════════════════════════════════════
// AFFIRMATION WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Widget for displaying daily neurodivergent affirmation
 * Tap to cycle to next affirmation
 */
class AffirmationWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_NEXT_AFFIRMATION = "com.kyilmaz.neurocomet.NEXT_AFFIRMATION"
        const val ACTION_OPEN_APP = "com.kyilmaz.neurocomet.OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_NEXT_AFFIRMATION -> {
                WidgetPreferences.getNextAffirmation(context)
            }
            ACTION_OPEN_APP -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val affirmation = WidgetPreferences.getDailyAffirmation(context)

        val views = RemoteViews(context.packageName, R.layout.widget_affirmation)

        // Set affirmation text
        views.setTextViewText(R.id.widget_affirmation_text, affirmation)

        // Set click actions
        views.setOnClickPendingIntent(
            R.id.widget_affirmation_next,
            createPendingIntent(context, ACTION_NEXT_AFFIRMATION)
        )
        views.setOnClickPendingIntent(
            R.id.widget_affirmation_container,
            createPendingIntent(context, ACTION_OPEN_APP)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, AffirmationWidgetProvider::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
    }
}

// ═══════════════════════════════════════════════════════════════
// STIM BREAK WIDGET
// ═══════════════════════════════════════════════════════════════

/**
 * Widget for tracking and reminding stim breaks
 * Visual and/or haptic reminders to take breaks

 */
class StimBreakWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TAKE_BREAK = "com.kyilmaz.neurocomet.TAKE_BREAK"
        const val ACTION_RESET_BREAK_TIMER = "com.kyilmaz.neurocomet.RESET_BREAK_TIMER"
        const val ACTION_OPEN_APP = "com.kyilmaz.neurocomet.OPEN_APP"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TAKE_BREAK -> {
                // Log the stim break action
                Log.d("StimBreakWidget", "ACTION_TAKE_BREAK received")

                // Register the stim break
                WidgetPreferences.setLastStimBreak(context)

                // Optionally: Show a notification or feedback
                // NotificationHelper.showStimBreakNotification(context)
            }
            ACTION_RESET_BREAK_TIMER -> {
                // Log the reset action
                Log.d("StimBreakWidget", "ACTION_RESET_BREAK_TIMER received")

                // Reset the stim break timer
                WidgetPreferences.setLastStimBreak(context)
            }
            ACTION_OPEN_APP -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val lastBreak = WidgetPreferences.getLastStimBreak(context)
        val minutesSinceBreak = WidgetPreferences.getMinutesSinceStimBreak(context)
        val breakInterval = WidgetPreferences.getStimBreakInterval(context)

        val views = RemoteViews(context.packageName, R.layout.widget_stim_break)

        // Set last break time
        views.setTextViewText(
            R.id.widget_break_last,
            if (lastBreak > 0L) {
                context.getString(R.string.widget_mood_last_checkin, formatTime(context, lastBreak))
            } else {
                context.getString(R.string.widget_stim_break_last_placeholder)
            }
        )

        views.setTextViewText(
            R.id.widget_break_emoji,
            when {
                lastBreak <= 0L -> context.getString(R.string.widget_stim_break_default_emoji)
                minutesSinceBreak >= breakInterval -> "⏰"
                breakInterval - minutesSinceBreak <= 10 -> "🔔"
                else -> "🧘"
            }
        )

        // Set minutes since last break
        views.setTextViewText(
            R.id.widget_break_since,
            context.resources.getQuantityString(R.plurals.widget_stim_break_since_minutes, minutesSinceBreak, minutesSinceBreak)
        )

        // Set next break reminder
        val nextBreakIn = (breakInterval - minutesSinceBreak).coerceAtLeast(0)
        views.setTextViewText(
            R.id.widget_break_next,
            context.resources.getQuantityString(R.plurals.widget_stim_break_next_in_minutes, nextBreakIn, nextBreakIn)
        )

        // Set click actions
        views.setOnClickPendingIntent(
            R.id.widget_break_take,
            createPendingIntent(context, ACTION_TAKE_BREAK)
        )
        views.setOnClickPendingIntent(
            R.id.widget_break_reset,
            createPendingIntent(context, ACTION_RESET_BREAK_TIMER)
        )
        views.setOnClickPendingIntent(
            R.id.widget_break_container,
            createPendingIntent(context, ACTION_OPEN_APP)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun formatTime(context: Context, timestamp: Long): String {
        return android.text.format.DateFormat.getTimeFormat(context).format(Date(timestamp))
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, StimBreakWidgetProvider::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, flags)
    }
}

// ═══════════════════════════════════════════════════════════════
// WIDGET HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════

/**
 * Helper to update widget from Compose/App
 */
object WidgetHelper {

    fun updateEnergyLevel(context: Context, level: Int) {
        WidgetPreferences.setEnergyLevel(context, level)
    }

    fun updateMood(context: Context, mood: String) {
        WidgetPreferences.setCurrentMood(context, mood)
        WidgetPreferences.setLastCheckIn(context)
    }

    fun startFocusTimer(context: Context, minutes: Int = 25) {
        WidgetPreferences.setFocusTimer(context, minutes, true)
    }

    fun pauseFocusTimer(context: Context) {
        val minutes = WidgetPreferences.getFocusMinutes(context)
        WidgetPreferences.setFocusTimer(context, minutes, false)
    }

    fun resetFocusTimer(context: Context) {
        WidgetPreferences.setFocusTimer(context, 25, false)
    }

    fun updateTaskProgress(context: Context, completed: Int, total: Int) {
        WidgetPreferences.setTaskProgress(context, completed, total)
    }
}


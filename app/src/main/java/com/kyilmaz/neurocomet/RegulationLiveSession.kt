package com.kyilmaz.neurocomet

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kyilmaz.neurocomet.widget.WidgetPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class RegulationSessionPreset(
    val title: String,
    val subtitle: String,
    val defaultDurationMinutes: Int,
    val neuroState: NeuroState
) {
    RECHARGE_WINDOW(
        title = "Recharge Window",
        subtitle = "A gentle nervous-system reset with spoon-aware pacing",
        defaultDurationMinutes = 15,
        neuroState = NeuroState.ANXIETY_GROUNDING
    ),
    FOCUS_SPRINT(
        title = "Focus Sprint",
        subtitle = "One task, protected attention, minimal noise",
        defaultDurationMinutes = 25,
        neuroState = NeuroState.ADHD_TASK_MODE
    ),
    STIM_BREAK(
        title = "Stim Break",
        subtitle = "A short sensory regulation window before re-entry",
        defaultDurationMinutes = 5,
        neuroState = NeuroState.AUTISM_SENSORY_SEEK
    )
}

data class RegulationLiveSession(
    val preset: RegulationSessionPreset,
    val startedAtMillis: Long,
    val endTimeMillis: Long,
    val isPaused: Boolean,
    val pausedRemainingMillis: Long,
    val spoonsRemaining: Int,
    val spoonsTotal: Int,
    val brailleOptimized: Boolean
)

object RegulationLiveSessionManager {
    private const val PREFS_NAME = "regulation_live_session"
    private const val KEY_PRESET = "preset"
    private const val KEY_STARTED_AT = "started_at"
    private const val KEY_END_TIME = "end_time"
    private const val KEY_IS_PAUSED = "is_paused"
    private const val KEY_PAUSED_REMAINING = "paused_remaining"
    private const val KEY_SPOONS_REMAINING = "spoons_remaining"
    private const val KEY_SPOONS_TOTAL = "spoons_total"
    private const val KEY_BRAILLE_OPTIMIZED = "braille_optimized"
    private const val NOTIFICATION_ID = 62_501

    private val _session = MutableStateFlow<RegulationLiveSession?>(null)
    val session: StateFlow<RegulationLiveSession?> = _session.asStateFlow()

    fun initialize(context: Context) {
        if (_session.value == null) {
            _session.value = load(context)
        }
    }

    fun startPreset(
        context: Context,
        preset: RegulationSessionPreset,
        durationMinutes: Int = preset.defaultDurationMinutes
    ) {
        val now = System.currentTimeMillis()
        val durationMillis = durationMinutes.coerceIn(1, 180) * 60_000L
        val a11y = SocialSettingsManager.getAccessibilitySettings(context)
        val session = RegulationLiveSession(
            preset = preset,
            startedAtMillis = now,
            endTimeMillis = now + durationMillis,
            isPaused = false,
            pausedRemainingMillis = durationMillis,
            spoonsRemaining = WidgetPreferences.getSpoonsRemaining(context),
            spoonsTotal = WidgetPreferences.getSpoonsTotal(context),
            brailleOptimized = SocialSettingsManager.isBrailleOptimized(context)
        )
        setSession(context, session)
    }

    fun pause(context: Context) {
        val current = _session.value ?: return
        if (current.isPaused) return
        val remaining = remainingMillis(current)
        setSession(
            context,
            current.copy(
                isPaused = true,
                pausedRemainingMillis = remaining
            )
        )
    }

    fun resume(context: Context) {
        val current = _session.value ?: return
        if (!current.isPaused) return
        val now = System.currentTimeMillis()
        setSession(
            context,
            current.copy(
                isPaused = false,
                endTimeMillis = now + current.pausedRemainingMillis.coerceAtLeast(60_000L)
            )
        )
    }

    fun togglePause(context: Context) {
        val current = _session.value ?: return
        if (current.isPaused) resume(context) else pause(context)
    }

    fun adjustMinutes(context: Context, deltaMinutes: Int) {
        val current = _session.value ?: return
        val deltaMillis = deltaMinutes * 60_000L
        if (current.isPaused) {
            val updatedRemaining = (current.pausedRemainingMillis + deltaMillis).coerceIn(60_000L, 180L * 60_000L)
            setSession(context, current.copy(pausedRemainingMillis = updatedRemaining))
        } else {
            val remaining = remainingMillis(current)
            val updatedRemaining = (remaining + deltaMillis).coerceIn(60_000L, 180L * 60_000L)
            setSession(
                context,
                current.copy(endTimeMillis = System.currentTimeMillis() + updatedRemaining)
            )
        }
    }

    fun spendSpoon(context: Context, count: Int = 1) {
        val current = _session.value ?: return
        WidgetPreferences.useSpoon(context, count)
        syncSpoonsFromWidgets(context, current.brailleOptimized)
    }

    fun restoreSpoon(context: Context, count: Int = 1) {
        val total = WidgetPreferences.getSpoonsTotal(context)
        val currentRemaining = WidgetPreferences.getSpoonsRemaining(context)
        WidgetPreferences.setSpoonsRemaining(context, (currentRemaining + count).coerceAtMost(total))
        val session = _session.value ?: return
        syncSpoonsFromWidgets(context, session.brailleOptimized)
    }

    fun refreshAccessibilityMode(context: Context) {
        val current = _session.value ?: return
        val a11y = SocialSettingsManager.getAccessibilitySettings(context)
        setSession(
            context,
            current.copy(brailleOptimized = SocialSettingsManager.isBrailleOptimized(context))
        )
    }

    fun end(context: Context) {
        _session.value = null
        clear(context)
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun syncSpoonsFromWidgets(context: Context, brailleOptimized: Boolean) {
        val current = _session.value ?: return
        setSession(
            context,
            current.copy(
                spoonsRemaining = WidgetPreferences.getSpoonsRemaining(context),
                spoonsTotal = WidgetPreferences.getSpoonsTotal(context),
                brailleOptimized = brailleOptimized
            )
        )
    }

    private fun setSession(context: Context, session: RegulationLiveSession) {
        _session.value = session
        persist(context, session)
        postNotification(context, session)
    }

    fun remainingMillis(session: RegulationLiveSession, now: Long = System.currentTimeMillis()): Long {
        return if (session.isPaused) {
            session.pausedRemainingMillis.coerceAtLeast(0L)
        } else {
            (session.endTimeMillis - now).coerceAtLeast(0L)
        }
    }

    private fun postNotification(context: Context, session: RegulationLiveSession) {
        if (!NotificationChannels.hasNotificationPermission(context)) return

        val remaining = remainingMillis(session)
        val manager = NotificationManagerCompat.from(context)
        runCatching {
            manager.notify(NOTIFICATION_ID, buildNotification(context, session, remaining))
        }
    }

    private fun buildNotification(
        context: Context,
        session: RegulationLiveSession,
        remainingMillis: Long
    ) = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_LIVE_EVENTS)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(buildNotificationTitle(session, remainingMillis))
        .setContentText(buildNotificationText(session, remainingMillis))
        .setStyle(
            NotificationCompat.BigTextStyle().bigText(
                "${session.preset.subtitle}\n${session.preset.neuroState.getDisplayName(context)} • ${session.spoonsRemaining}/${session.spoonsTotal} spoons"
            )
        )
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContentIntent(createLaunchIntent(context))
        .apply {
            if (session.isPaused) {
                setSubText("Paused")
                setTimeoutAfter(0L)
            } else {
                setUsesChronometer(true)
                setChronometerCountDown(true)
                setWhen(System.currentTimeMillis() + remainingMillis)
                setShowWhen(true)
                setTimeoutAfter(remainingMillis.coerceAtLeast(1_000L))
            }
        }
        .build()

    private fun buildNotificationTitle(session: RegulationLiveSession, remainingMillis: Long): String {
        val timeLabel = formatDuration(remainingMillis)
        return if (session.isPaused) {
            "${session.preset.title} paused • $timeLabel left"
        } else {
            "${session.preset.title} • $timeLabel left"
        }
    }

    private fun buildNotificationText(session: RegulationLiveSession, remainingMillis: Long): String {
        val brailleSuffix = if (session.brailleOptimized) " • braille-optimized" else ""
        val timing = if (session.isPaused) "paused at ${formatDuration(remainingMillis)}" else session.preset.neuroState.name.lowercase()
            .replace('_', ' ')
        return "${session.preset.neuroState.emoji} ${session.preset.subtitle} • ${session.spoonsRemaining}/${session.spoonsTotal} spoons$brailleSuffix"
            .replace("${session.preset.neuroState.emoji} ", "")
            .let { if (session.brailleOptimized) it else "$timing • $it" }
    }

    private fun createLaunchIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("targetRoute", Screen.Settings.route)
        }
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun persist(context: Context, session: RegulationLiveSession) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRESET, session.preset.name)
            .putLong(KEY_STARTED_AT, session.startedAtMillis)
            .putLong(KEY_END_TIME, session.endTimeMillis)
            .putBoolean(KEY_IS_PAUSED, session.isPaused)
            .putLong(KEY_PAUSED_REMAINING, session.pausedRemainingMillis)
            .putInt(KEY_SPOONS_REMAINING, session.spoonsRemaining)
            .putInt(KEY_SPOONS_TOTAL, session.spoonsTotal)
            .putBoolean(KEY_BRAILLE_OPTIMIZED, session.brailleOptimized)
            .apply()
    }

    private fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun load(context: Context): RegulationLiveSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val presetName = prefs.getString(KEY_PRESET, null) ?: return null
        val preset = runCatching { RegulationSessionPreset.valueOf(presetName) }.getOrNull() ?: return null
        return RegulationLiveSession(
            preset = preset,
            startedAtMillis = prefs.getLong(KEY_STARTED_AT, System.currentTimeMillis()),
            endTimeMillis = prefs.getLong(KEY_END_TIME, System.currentTimeMillis()),
            isPaused = prefs.getBoolean(KEY_IS_PAUSED, false),
            pausedRemainingMillis = prefs.getLong(KEY_PAUSED_REMAINING, preset.defaultDurationMinutes * 60_000L),
            spoonsRemaining = prefs.getInt(KEY_SPOONS_REMAINING, WidgetPreferences.getSpoonsRemaining(context)),
            spoonsTotal = prefs.getInt(KEY_SPOONS_TOTAL, WidgetPreferences.getSpoonsTotal(context)),
            brailleOptimized = prefs.getBoolean(KEY_BRAILLE_OPTIMIZED, false)
        )
    }
}

@Composable
fun RegulationLiveSessionHost(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        RegulationLiveSessionManager.initialize(context)
        RegulationLiveSessionManager.refreshAccessibilityMode(context)
    }

    val session by RegulationLiveSessionManager.session.collectAsState()
    session ?: return
    val activeSession = session ?: return
    val remaining by rememberLiveSessionRemaining(activeSession)

    LaunchedEffect(activeSession, remaining) {
        if (!activeSession.isPaused && remaining <= 0L) {
            RegulationLiveSessionManager.end(context)
        }
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Polite
                role = Role.Button
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
     ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeSession.preset.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeSession.preset.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (activeSession.isPaused) "Paused • ${formatDuration(remaining)}" else formatDuration(remaining),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionMetaChip(label = activeSession.preset.neuroState.getDisplayName(context))
                SessionMetaChip(label = "${activeSession.spoonsRemaining}/${activeSession.spoonsTotal} spoons")
                if (activeSession.brailleOptimized) {
                    SessionMetaChip(label = "Braille mode")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { RegulationLiveSessionManager.togglePause(context) }) {
                    Icon(
                        imageVector = if (activeSession.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (activeSession.isPaused) "Resume" else "Pause")
                }
                OutlinedButton(onClick = { RegulationLiveSessionManager.adjustMinutes(context, 2) }) {
                    Text("+2 min")
                }
                OutlinedButton(onClick = { RegulationLiveSessionManager.end(context) }) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("End")
                }
            }
        }
    }
}

@Composable
private fun SessionMetaChip(label: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun rememberLiveSessionRemaining(session: RegulationLiveSession): State<Long> = produceState(
    initialValue = RegulationLiveSessionManager.remainingMillis(session),
    key1 = session
) {
    while (true) {
        value = RegulationLiveSessionManager.remainingMillis(session)
        if (session.isPaused || value <= 0L) break
        delay(1_000L)
    }
}

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis.coerceAtLeast(0L) / 1_000L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remMinutes = minutes % 60
        "${hours}h ${remMinutes}m"
    } else if (minutes > 0) {
        if (seconds == 0L) "${minutes}m" else "${minutes}m ${seconds}s"
    } else {
        "${seconds}s"
    }
}

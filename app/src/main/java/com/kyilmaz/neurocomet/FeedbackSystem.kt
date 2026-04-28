package com.kyilmaz.neurocomet

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.put
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Feedback & Feature Request System for NeuroComet
 *
 * Features:
 * - Bug Reporter with device info collection
 * - Feature Request submission with voting
 * - General Feedback form
 * - Neurodivergent-friendly UI with clear categories
 */

// ═══════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════

enum class FeedbackType {
    BUG_REPORT,
    FEATURE_REQUEST,
    GENERAL_FEEDBACK,
    SAFETY_REPORT,
    SUPPORT_TICKET
}

enum class BugSeverity {
    LOW,      // Minor annoyance
    MEDIUM,   // Impacts usability
    HIGH,     // Major feature broken
    CRITICAL  // App crashes/data loss
}

enum class FeatureCategory {
    ACCESSIBILITY,
    SOCIAL,
    SAFETY,
    CUSTOMIZATION,
    MESSAGING,
    CONTENT,
    PERFORMANCE,
    OTHER
}

data class FeedbackItem(
    val id: String = UUID.randomUUID().toString(),
    val type: FeedbackType,
    val title: String,
    val description: String,
    val category: FeatureCategory? = null,
    val severity: BugSeverity? = null,
    val deviceInfo: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: FeedbackStatus = FeedbackStatus.SUBMITTED,
    val votes: Int = 0,
    val userVoted: Boolean = false
)

enum class FeedbackStatus {
    SUBMITTED,
    UNDER_REVIEW,
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    DECLINED
}

// ═══════════════════════════════════════════════════════════════
// FEEDBACK MANAGER
// ═══════════════════════════════════════════════════════════════

object FeedbackManager {
    private const val PREFS_NAME = "neurocomet_feedback"
    private const val KEY_SUBMISSIONS_COUNT = "submissions_count"
    private const val KEY_LAST_SUBMISSION_TIME = "last_submission_time"

    // Rate limiting: max 5 submissions per day
    private const val MAX_DAILY_SUBMISSIONS = 5
    private const val DAY_MS = 24 * 60 * 60 * 1000L

    /**
     * Collect device information for bug reports
     */
    fun getDeviceInfo(context: Context): String {
        return buildString {
            appendLine("📱 Device Information:")
            appendLine("• Model: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("• Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("• App Version: ${getAppVersion(context)}")
            appendLine("• Screen: ${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}")
            appendLine("• Density: ${context.resources.displayMetrics.density}x")
            appendLine("• Language: ${Locale.getDefault().displayLanguage}")
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val buildCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toString()
            }
            "${pInfo.versionName} ($buildCode)"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Check if user can submit feedback (rate limiting).
     * Respects [DevOptions.bypassFeedbackRateLimit] in debug builds.
     */
    fun canSubmitFeedback(context: Context): Boolean {
        // Dev option bypass
        if (DevOptionsSettings.get(context).bypassFeedbackRateLimit) return true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSubmissionTime = prefs.getLong(KEY_LAST_SUBMISSION_TIME, 0)
        val submissionsCount = prefs.getInt(KEY_SUBMISSIONS_COUNT, 0)

        val now = System.currentTimeMillis()

        // Reset count if it's a new day
        if (now - lastSubmissionTime > DAY_MS) {
            prefs.edit {
                putInt(KEY_SUBMISSIONS_COUNT, 0)
                putLong(KEY_LAST_SUBMISSION_TIME, now)
            }
            return true
        }

        return submissionsCount < MAX_DAILY_SUBMISSIONS
    }

    /**
     * Record a feedback submission
     */
    fun recordSubmission(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSubmissionTime = prefs.getLong(KEY_LAST_SUBMISSION_TIME, 0)
        val currentCount = prefs.getInt(KEY_SUBMISSIONS_COUNT, 0)
        val now = System.currentTimeMillis()

        val newCount = if (now - lastSubmissionTime > DAY_MS) 1 else currentCount + 1

        prefs.edit {
            putInt(KEY_SUBMISSIONS_COUNT, newCount)
            putLong(KEY_LAST_SUBMISSION_TIME, now)
        }
    }

    /**
     * Get remaining submissions for today
     */
    fun getRemainingSubmissions(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSubmissionTime = prefs.getLong(KEY_LAST_SUBMISSION_TIME, 0)
        val submissionsCount = prefs.getInt(KEY_SUBMISSIONS_COUNT, 0)

        val now = System.currentTimeMillis()
        return if (now - lastSubmissionTime > DAY_MS) {
            MAX_DAILY_SUBMISSIONS
        } else {
            (MAX_DAILY_SUBMISSIONS - submissionsCount).coerceAtLeast(0)
        }
    }

    // ── Supabase persistence with offline queue ──

    private const val KEY_PENDING_FEEDBACK = "pending_feedback"
    private const val TAG = "FeedbackManager"

    /**
     * Submit feedback data to Supabase. Falls back to local queue on failure.
     * Respects [DevOptions.forceFeedbackSubmitFailure] in debug builds.
     */
    suspend fun submitToSupabase(context: Context, data: Map<String, Any?>) {
        withContext(Dispatchers.IO) {
            // Dev option: force all submissions to the offline queue
            if (DevOptionsSettings.get(context).forceFeedbackSubmitFailure) {
                Log.d(TAG, "Dev option: forcing feedback submit failure → queuing locally")
                queueLocally(context, data)
                return@withContext
            }
            try {
                val client = AppSupabaseClient.client
                if (client != null) {
                    val payload = kotlinx.serialization.json.buildJsonObject {
                        data.forEach { (key, value) ->
                            when (value) {
                                is String -> put(key, value)
                                is Number -> put(key, value)
                                is Boolean -> put(key, value)
                                null -> put(key, kotlinx.serialization.json.JsonNull)
                            }
                        }
                    }
                    client.safeInsert("feedback", payload)
                    Log.d(TAG, "Feedback submitted to Supabase")
                } else {
                    // No client — queue locally
                    queueLocally(context, data)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Supabase insert failed, queuing locally", e)
                queueLocally(context, data)
            }
        }
    }

    private fun queueLocally(context: Context, data: Map<String, Any?>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_PENDING_FEEDBACK, emptySet())?.toMutableSet() ?: mutableSetOf()
        val json = JSONObject(data.mapValues { it.value?.toString() }).toString()
        existing.add(json)
        prefs.edit { putStringSet(KEY_PENDING_FEEDBACK, existing) }
        Log.d(TAG, "Feedback queued locally (${existing.size} pending)")
    }

    /**
     * Flush any pending offline feedback to Supabase.
     * Call before each new submission to opportunistically sync.
     */
    suspend fun flushPendingFeedback(context: Context) {
        withContext(Dispatchers.IO) {
            val client = AppSupabaseClient.client ?: return@withContext
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pending = prefs.getStringSet(KEY_PENDING_FEEDBACK, emptySet())?.toMutableSet() ?: return@withContext
            if (pending.isEmpty()) return@withContext

            val remaining = mutableSetOf<String>()
            for (jsonStr in pending) {
                try {
                    val jsonObj = JSONObject(jsonStr)
                    val map = mutableMapOf<String, Any?>()
                    jsonObj.keys().forEach { key -> map[key] = jsonObj.opt(key) }
                    client.from("feedback").insert(map)
                    Log.d(TAG, "Flushed pending feedback item")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to flush pending item, keeping in queue", e)
                    remaining.add(jsonStr)
                }
            }
            prefs.edit { putStringSet(KEY_PENDING_FEEDBACK, remaining) }
        }
    }

    /**
     * Build a standard feedback payload map for Supabase.
     */
    fun buildPayload(
        context: Context,
        type: String,
        title: String? = null,
        description: String,
        severity: String? = null,
        category: String? = null,
        rating: Int? = null,
        includeDeviceInfo: Boolean = true
    ): Map<String, Any?> {
        val userId = try {
            AppSupabaseClient.client?.auth?.currentUserOrNull()?.id
        } catch (_: Exception) { null }

        return buildMap {
            put("type", type)
            if (title != null) put("title", title)
            put("description", description)
            if (severity != null) put("severity", severity)
            if (category != null) put("category", category)
            if (rating != null) put("rating", rating)
            if (includeDeviceInfo) put("device_info", getDeviceInfo(context))
            put("user_id", userId)
            put("app_version", BuildConfig.VERSION_NAME)
            put("submitted_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date()))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MOCK FEATURE REQUESTS (for display)
// ═══════════════════════════════════════════════════════════════

val MOCK_FEATURE_REQUESTS = listOf(
    FeedbackItem(
        id = "fr1",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Dark AMOLED Theme",
        description = "A pure black theme to save battery on OLED screens and reduce eye strain",
        category = FeatureCategory.ACCESSIBILITY,
        status = FeedbackStatus.IN_PROGRESS,
        votes = 847,
        userVoted = false
    ),
    FeedbackItem(
        id = "fr2",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Scheduled Quiet Hours",
        description = "Automatically mute notifications during specified times",
        category = FeatureCategory.SAFETY,
        status = FeedbackStatus.PLANNED,
        votes = 632,
        userVoted = true
    ),
    FeedbackItem(
        id = "fr3",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Custom Stim Timer Sounds",
        description = "Let users upload their own calming sounds for break reminders",
        category = FeatureCategory.CUSTOMIZATION,
        status = FeedbackStatus.UNDER_REVIEW,
        votes = 423,
        userVoted = false
    ),
    FeedbackItem(
        id = "fr4",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Group Video Chats",
        description = "Video chat rooms for community meetups with camera-optional mode",
        category = FeatureCategory.SOCIAL,
        status = FeedbackStatus.PLANNED,
        votes = 389,
        userVoted = false
    ),
    FeedbackItem(
        id = "fr5",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Dyslexia-Friendly Font",
        description = "Add OpenDyslexic or similar fonts as a accessibility option",
        category = FeatureCategory.ACCESSIBILITY,
        status = FeedbackStatus.COMPLETED,
        votes = 756,
        userVoted = true
    ),
    FeedbackItem(
        id = "fr6",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Offline Mode",
        description = "Cache posts and messages for reading without internet",
        category = FeatureCategory.PERFORMANCE,
        status = FeedbackStatus.UNDER_REVIEW,
        votes = 298,
        userVoted = false
    ),
    FeedbackItem(
        id = "fr7",
        type = FeedbackType.FEATURE_REQUEST,
        title = "AI-Powered Content Warnings",
        description = "Automatically detect and blur potentially triggering content",
        category = FeatureCategory.SAFETY,
        status = FeedbackStatus.IN_PROGRESS,
        votes = 512,
        userVoted = false
    ),
    FeedbackItem(
        id = "fr8",
        type = FeedbackType.FEATURE_REQUEST,
        title = "Interest-Based Matching",
        description = "Find others with similar special interests for connections",
        category = FeatureCategory.SOCIAL,
        status = FeedbackStatus.PLANNED,
        votes = 445,
        userVoted = true
    )
)

// ═══════════════════════════════════════════════════════════════
// MAIN FEEDBACK HUB SCREEN
// ═══════════════════════════════════════════════════════════════


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackHubScreen(
    onBack: () -> Unit,
    onOpenBugReport: () -> Unit = {},
    onOpenFeatureRequest: () -> Unit = {},
    onOpenGeneralFeedback: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Opportunistically flush any pending offline feedback
    LaunchedEffect(Unit) {
        scope.launch { FeedbackManager.flushPendingFeedback(context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feedback_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Closed Beta banner ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧪", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.beta_closed_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Your feedback shapes NeuroComet! All submissions are reviewed by the dev team.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // ── Navigation cards ──
            item {
                FeedbackOptionCard(
                    icon = Icons.Filled.BugReport,
                    title = "Report a Bug",
                    description = "Found something broken? Let us know so we can fix it.",
                    emoji = "🐛",
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = onOpenBugReport
                )
            }

            item {
                FeedbackOptionCard(
                    icon = Icons.Filled.Lightbulb,
                    title = "Request a Feature",
                    description = "Have an idea to make NeuroComet better? We'd love to hear it!",
                    emoji = "💡",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenFeatureRequest
                )
            }

            item {
                FeedbackOptionCard(
                    icon = Icons.Filled.Feedback,
                    title = "Send Feedback",
                    description = "Share your thoughts, suggestions, or just say hi!",
                    emoji = "💬",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenGeneralFeedback
                )
            }

            item {
                FeedbackOptionCard(
                    icon = Icons.Filled.Shield,
                    title = "Safety & Reporting",
                    description = "Report content or users that violate community guidelines.",
                    emoji = "🛡️",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { /* Could navigate to a specific safety screen */ onOpenBugReport() }
                )
            }

            item {
                FeedbackOptionCard(
                    icon = Icons.Filled.SupportAgent,
                    title = "Get Support",
                    description = "Need help with your account or a specific feature?",
                    emoji = "🤝",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenGeneralFeedback
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "All feedback is reviewed by our team. We prioritize features based on community votes and impact on neurodivergent users.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STANDALONE SCREENS — Bug Report, Feature Request, General Feedback
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(BugSeverity.MEDIUM) }
    var includeDeviceInfo by remember { mutableStateOf(true) }

    val deviceInfo = remember { FeedbackManager.getDeviceInfo(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report a Bug") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐛", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Found a bug? Let us know so we can fix it!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Bug Title") },
                placeholder = { Text(stringResource(R.string.feedback_bug_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Steps to Reproduce") },
                placeholder = { Text(stringResource(R.string.feedback_steps_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Severity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BugSeverity.entries.forEach { sev ->
                    FilterChip(
                        selected = severity == sev,
                        onClick = { severity = sev },
                        label = { Text(sev.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (sev) {
                                BugSeverity.LOW -> Color(0xFF4CAF50)
                                BugSeverity.MEDIUM -> Color(0xFFFF9800)
                                BugSeverity.HIGH -> Color(0xFFFF5722)
                                BugSeverity.CRITICAL -> Color(0xFFF44336)
                            }
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeDeviceInfo,
                    onCheckedChange = { includeDeviceInfo = it }
                )
                Spacer(Modifier.width(8.dp))
                Text("Include device information")
            }

            if (includeDeviceInfo) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = deviceInfo,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        FeedbackManager.flushPendingFeedback(context)
                        val payload = FeedbackManager.buildPayload(
                            context = context,
                            type = "bug_report",
                            title = title,
                            description = description,
                            severity = severity.name.lowercase()
                        )
                        FeedbackManager.submitToSupabase(context, payload)
                        FeedbackManager.recordSubmission(context)
                    }
                    onBack()
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null)
                Spacer(Modifier.width(8.dp))
                Text("Submit Bug Report")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureRequestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(FeatureCategory.OTHER) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request a Feature") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Have an idea? We'd love to hear it!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Feature Title") },
                placeholder = { Text(stringResource(R.string.feedback_feature_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text(stringResource(R.string.feedback_feature_desc_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = if (category == cat) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                        } else {
                            { Icon(getCategoryIcon(cat), null, Modifier.size(16.dp)) }
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        FeedbackManager.flushPendingFeedback(context)
                        val payload = FeedbackManager.buildPayload(
                            context = context,
                            type = "feature_request",
                            title = title,
                            description = description,
                            category = category.name.lowercase()
                        )
                        FeedbackManager.submitToSupabase(context, payload)
                        FeedbackManager.recordSubmission(context)
                    }
                    onBack()
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null)
                Spacer(Modifier.width(8.dp))
                Text("Submit Request")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralFeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Feedback") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💬", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Your feedback helps us make NeuroComet better for everyone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "How's NeuroComet?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // Star rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { index ->
                    IconButton(onClick = { rating = index + 1 }) {
                        Icon(
                            if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                            "Rate ${index + 1}",
                            tint = if (index < rating)
                                Color(0xFFFFD700)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Your thoughts (optional)") },
                placeholder = { Text(stringResource(R.string.feedback_general_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 6
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = {
                        scope.launch {
                            FeedbackManager.flushPendingFeedback(context)
                            val payload = FeedbackManager.buildPayload(
                                context = context,
                                type = "general_feedback",
                                description = feedback,
                                rating = rating
                            )
                            FeedbackManager.submitToSupabase(context, payload)
                            FeedbackManager.recordSubmission(context)
                        }
                        onBack()
                    },
                    enabled = rating > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_submit))
                }
            }
        }
    }
}

@Composable
private fun SubmitFeedbackTab(
    onBugReport: () -> Unit,
    onFeatureRequest: () -> Unit,
    onGeneralFeedback: () -> Unit
) {
    val context = LocalContext.current
    val remainingSubmissions = FeedbackManager.getRemainingSubmissions(context)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Rate limit info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "You have $remainingSubmissions submissions remaining today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        item {
            FeedbackOptionCard(
                icon = Icons.Filled.BugReport,
                title = "Report a Bug",
                description = "Found something broken? Let us know so we can fix it.",
                emoji = "🐛",
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onBugReport,
                enabled = remainingSubmissions > 0
            )
        }

        item {
            FeedbackOptionCard(
                icon = Icons.Filled.Lightbulb,
                title = "Request a Feature",
                description = "Have an idea to make NeuroComet better? We'd love to hear it!",
                emoji = "💡",
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onFeatureRequest,
                enabled = remainingSubmissions > 0
            )
        }

        item {
            FeedbackOptionCard(
                icon = Icons.Filled.Feedback,
                title = "Send Feedback",
                description = "Share your thoughts, suggestions, or just say hi!",
                emoji = "💬",
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onGeneralFeedback,
                enabled = remainingSubmissions > 0
            )
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "All feedback is reviewed by our team. We prioritize features based on community votes and impact on neurodivergent users.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FeedbackOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    emoji: String,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) color else color.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                null,
                tint = contentColor.copy(alpha = 0.5f),
                modifier = Modifier.rotate(180f)
            )
        }
    }
}

@Composable
private fun FeatureRequestsTab() {
    var featureRequests by remember { mutableStateOf(MOCK_FEATURE_REQUESTS) }
    var sortByVotes by remember { mutableStateOf(true) }

    val sortedRequests = if (sortByVotes) {
        featureRequests.sortedByDescending { it.votes }
    } else {
        featureRequests.sortedByDescending { it.timestamp }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sort options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = sortByVotes,
                onClick = { sortByVotes = true },
                label = { Text(stringResource(R.string.feedback_top_voted)) },
                leadingIcon = if (sortByVotes) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }
                } else null
            )
            FilterChip(
                selected = !sortByVotes,
                onClick = { sortByVotes = false },
                label = { Text(stringResource(R.string.feedback_newest)) },
                leadingIcon = if (!sortByVotes) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedRequests) { request ->
                FeatureRequestCard(
                    request = request,
                    onVote = { voted ->
                        featureRequests = featureRequests.map {
                            if (it.id == request.id) {
                                it.copy(
                                    votes = if (voted) it.votes + 1 else it.votes - 1,
                                    userVoted = voted
                                )
                            } else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FeatureRequestCard(
    request: FeedbackItem,
    onVote: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Vote button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                IconButton(
                    onClick = { onVote(!request.userVoted) }
                ) {
                    Icon(
                        if (request.userVoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(R.string.action_vote),
                        tint = if (request.userVoted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${request.votes}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (request.userVoted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Status badge
                StatusBadge(request.status)

                Spacer(Modifier.height(8.dp))

                Text(
                    text = request.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (request.category != null) {
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(request.category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            Icon(
                                getCategoryIcon(request.category),
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: FeedbackStatus) {
    val (color, text) = when (status) {
        FeedbackStatus.SUBMITTED -> MaterialTheme.colorScheme.outline to "Submitted"
        FeedbackStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.tertiary to "Under Review"
        FeedbackStatus.PLANNED -> MaterialTheme.colorScheme.primary to "Planned"
        FeedbackStatus.IN_PROGRESS -> Color(0xFF4CAF50) to "In Progress"
        FeedbackStatus.COMPLETED -> Color(0xFF2196F3) to "Completed"
        FeedbackStatus.DECLINED -> MaterialTheme.colorScheme.error to "Declined"
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Suppress("DEPRECATION")
private fun getCategoryIcon(category: FeatureCategory): ImageVector {
    return when (category) {
        FeatureCategory.ACCESSIBILITY -> Icons.Outlined.Accessibility
        FeatureCategory.SOCIAL -> Icons.Outlined.People
        FeatureCategory.SAFETY -> Icons.Outlined.Shield
        FeatureCategory.CUSTOMIZATION -> Icons.Outlined.Palette
        FeatureCategory.MESSAGING -> Icons.Outlined.Chat
        FeatureCategory.CONTENT -> Icons.Outlined.Article
        FeatureCategory.PERFORMANCE -> Icons.Outlined.Speed
        FeatureCategory.OTHER -> Icons.Outlined.MoreHoriz
    }
}

@Composable
private fun RoadmapTab() {
    val plannedItems = MOCK_FEATURE_REQUESTS.filter {
        it.status == FeedbackStatus.PLANNED || it.status == FeedbackStatus.IN_PROGRESS
    }.sortedByDescending { it.votes }

    val completedItems = MOCK_FEATURE_REQUESTS.filter {
        it.status == FeedbackStatus.COMPLETED
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🚀 Coming Soon",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(plannedItems) { item ->
            RoadmapCard(item)
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "✅ Recently Shipped",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(completedItems) { item ->
            RoadmapCard(item)
        }
    }
}

@Composable
private fun RoadmapCard(item: FeedbackItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.status == FeedbackStatus.COMPLETED)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (item.status) {
                    FeedbackStatus.COMPLETED -> Icons.Filled.CheckCircle
                    FeedbackStatus.IN_PROGRESS -> Icons.Filled.Build
                    else -> Icons.Outlined.Schedule
                },
                null,
                tint = when (item.status) {
                    FeedbackStatus.COMPLETED -> Color(0xFF4CAF50)
                    FeedbackStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${item.votes} votes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(item.status)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DIALOG FORMS
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReporterDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, severity: BugSeverity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(BugSeverity.MEDIUM) }
    var includeDeviceInfo by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val deviceInfo = remember { FeedbackManager.getDeviceInfo(context) }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50)) },
            title = { Text(stringResource(R.string.feedback_bug_submitted)) },
            text = { Text("Thank you for helping us improve NeuroComet! We'll investigate this issue.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐛 Report a Bug",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bug Title") },
                    placeholder = { Text(stringResource(R.string.feedback_bug_title_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Steps to Reproduce") },
                    placeholder = { Text(stringResource(R.string.feedback_steps_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 8
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Severity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BugSeverity.entries.forEach { sev ->
                        FilterChip(
                            selected = severity == sev,
                            onClick = { severity = sev },
                            label = { Text(sev.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (sev) {
                                    BugSeverity.LOW -> Color(0xFF4CAF50)
                                    BugSeverity.MEDIUM -> Color(0xFFFF9800)
                                    BugSeverity.HIGH -> Color(0xFFFF5722)
                                    BugSeverity.CRITICAL -> Color(0xFFF44336)
                                }
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeDeviceInfo,
                        onCheckedChange = { includeDeviceInfo = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Include device information")
                }

                if (includeDeviceInfo) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = deviceInfo,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSubmit(title, description, severity)
                        showSuccessMessage = true
                    },
                    enabled = title.isNotBlank() && description.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Bug Report")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: FeatureCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(FeatureCategory.OTHER) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFFFFD700)) },
            title = { Text(stringResource(R.string.feedback_feature_submitted)) },
            text = { Text("Your idea has been submitted! The community can now vote on it.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Feature Request",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                }

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Feature Title") },
                    placeholder = { Text(stringResource(R.string.feedback_feature_title_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text(stringResource(R.string.feedback_feature_desc_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 8
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                // Category selector with horizontal scroll to prevent overflow
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = if (category == cat) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                            } else {
                                { Icon(getCategoryIcon(cat), null, Modifier.size(16.dp)) }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSubmit(title, description, category)
                        showSuccessMessage = true
                    },
                    enabled = title.isNotBlank() && description.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Request")
                }
            }
        }
    }
}

@Composable
fun GeneralFeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (feedback: String, rating: Int) -> Unit
) {
    var feedback by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    if (showSuccessMessage) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Filled.Favorite, null, tint = Color(0xFFE91E63)) },
            title = { Text(stringResource(R.string.feedback_thank_you)) },
            text = { Text("Your feedback helps us make NeuroComet better for everyone.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💬 How's NeuroComet?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // Star rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                                "Rate ${index + 1}",
                                tint = if (index < rating)
                                    Color(0xFFFFD700)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Your thoughts (optional)") },
                    placeholder = { Text(stringResource(R.string.feedback_general_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = {
                            onSubmit(feedback, rating)
                            showSuccessMessage = true
                        },
                        enabled = rating > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.action_submit))
                    }
                }
            }
        }
    }
}


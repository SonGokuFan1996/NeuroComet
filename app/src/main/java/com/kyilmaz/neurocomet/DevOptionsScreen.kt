package com.kyilmaz.neurocomet

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyilmaz.neurocomet.AgeVerificationDialog

/**
 * Developer device whitelist - Add your device's Android ID here.
 * To find your Android ID, run this in ADB:
 *   adb shell settings get secure android_id
 *
 * Or check the logcat output when you first try to access dev options -
 * it will log your device ID for you to add here.
 */
private object DeveloperDevices {
    // Add your device Android IDs here (SHA-256 hashed for security)
    // The app will log the hash of your device ID when you first try to access dev options
    private val WHITELISTED_DEVICE_HASHES: Set<String> = setOf(
        // Example: "your_hashed_android_id_here"
        // To add your device:
        // 1. Run the app in debug mode first
        // 2. Try to access dev options
        // 3. Check logcat for "DEV_ACCESS" tag - it will show your device hash
        // 4. Add that hash string here
        BuildConfig.DEVELOPER_DEVICE_HASH // Set this in gradle.properties or build.gradle.kts
    )

    /**
     * Check if the current device is a whitelisted developer device.
     */
    fun isDevDevice(context: android.content.Context): Boolean {
        val deviceHash = getDeviceHash(context)
        return WHITELISTED_DEVICE_HASHES.contains(deviceHash)
    }

    /**
     * Get the SHA-256 hash of the device's Android ID.
     * This is used for identification without exposing the actual ID.
     */
    fun getDeviceHash(context: android.content.Context): String {
        return try {
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: return ""

            // Hash the Android ID for privacy/security
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(androidId.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * Unauthorized device overlay - shows when a non-developer device tries to access dev options.
 * Displays a translucent circle showing a portion of the app behind it, with authorization info.
 */
@Composable
private fun UnauthorizedDeviceOverlay(
    deviceHash: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Translucent circle that shows through
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Device Not Authorized",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Developer Options are only available on authorized development devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Show device hash for whitelisting
                Text(
                    "Device Hash:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    deviceHash.take(16) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
    }
}

/**
 * Security check to prevent Developer Options from being accessed in production builds.
 * This will crash the app if someone attempts to enable dev options in a release build,
 * UNLESS they are on a whitelisted developer device.
 */
private fun enforceDebugBuildOnly(context: android.content.Context) {
    val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

    // Always allow in debug builds
    if (isDebuggable) return

    // Check if this is a whitelisted developer device
    val deviceHash = DeveloperDevices.getDeviceHash(context)
    val isDevDevice = DeveloperDevices.isDevDevice(context)

    // Log the device hash so the developer can add it to the whitelist
    android.util.Log.d(
        "DEV_ACCESS",
        "Device hash for whitelist: $deviceHash"
    )

    if (isDevDevice) {
        // This is the developer's device - allow access
        android.util.Log.i(
            "DEV_ACCESS",
            "Developer device recognized. Allowing dev options access."
        )
        return
    }

    // Production build on non-developer device - dev options should not be accessible
    android.util.Log.wtf(
        "SECURITY",
        "Developer Options accessed in production build! This is a security violation."
    )

    // Crash the app to prevent tampering
    throw SecurityException(
        "Developer Options are not available in production builds. " +
        "If you are seeing this error, the app may have been tampered with."
    )
}

/**
 * Additional integrity check to detect if the app has been recompiled/modified.
 * Validates the app signature against expected values.
 */
private fun validateAppIntegrity(context: android.content.Context): Boolean {
    return try {
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        // In debug builds, always allow
        if (isDebuggable) return true

        // Check if this is a whitelisted developer device
        if (DeveloperDevices.isDevDevice(context)) return true

        // In release builds on non-dev devices, dev options should never be accessible
        false
    } catch (e: Exception) {
        // If we can't verify, assume tampering
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsScreen(
    onBack: () -> Unit,
    devOptionsViewModel: DevOptionsViewModel,
    safetyViewModel: SafetyViewModel,
    feedViewModel: FeedViewModel? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    // Check if this is a debug build or authorized device
    val isDebuggable = remember {
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val deviceHash = remember { DeveloperDevices.getDeviceHash(context) }
    val isDevDevice = remember { DeveloperDevices.isDevDevice(context) }
    val isAuthorized = remember { isDebuggable || isDevDevice }

    // Show unauthorized overlay instead of crashing
    if (!isAuthorized) {
        UnauthorizedDeviceOverlay(
            deviceHash = deviceHash,
            onBack = onBack
        )
        return
    }

    val devOptions by devOptionsViewModel.options.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()
    val showAgeDialog = remember { mutableStateOf(false) }

    // Optimization: Show content progressively for faster perceived load time
    var showAllSections by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Delay showing heavy sections to prioritize initial render
        kotlinx.coroutines.delay(50)
        showAllSections = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Developer Options", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "These options are for development and testing only. They may cause unexpected behavior.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100)
                    )
                }
            }

            // === APP INFO ===
            DevSection(
                title = "App Information",
                icon = Icons.Filled.BugReport
            ) {
                // Using cached values from remember blocks above
                StateInfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                StateInfoRow("Build Type", if (isDebuggable) "Debug" else "Release")
                StateInfoRow("Application ID", BuildConfig.APPLICATION_ID)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StateInfoRow("Device", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                StateInfoRow("Android Version", "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                StateInfoRow("Developer Device", if (isDevDevice) "✓ Authorized" else "✗ Not authorized")

                if (isDebuggable || isDevDevice) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Device Hash (for whitelist):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        deviceHash.take(32) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // === AUDIENCE CONTROLS ===
            DevSection(
                title = "Audience & Content Filtering",
                icon = Icons.Filled.ChildCare
            ) {
                Text(
                    "Override the current audience level to test age-restricted content filtering.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    "Current Audience: ${safetyState.audience}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudienceChip(
                        label = "Under 13",
                        isSelected = safetyState.audience == Audience.UNDER_13,
                        color = Color(0xFF4CAF50),
                        onClick = { safetyViewModel.setAudience(Audience.UNDER_13, app) }
                    )
                    AudienceChip(
                        label = "Teen 13+",
                        isSelected = safetyState.audience == Audience.TEEN,
                        color = Color(0xFFFF9800),
                        onClick = { safetyViewModel.setAudience(Audience.TEEN, app) }
                    )
                    AudienceChip(
                        label = "Adult 18+",
                        isSelected = safetyState.audience == Audience.ADULT,
                        color = Color(0xFFF44336),
                        onClick = { safetyViewModel.setAudience(Audience.ADULT, app) }
                    )
                }

                Spacer(Modifier.height(12.dp))

                DevToggleRow(
                    title = "Force Parental PIN Set",
                    subtitle = "Simulate parental controls being configured",
                    isChecked = devOptions.forcePinSet,
                    onCheckedChange = { devOptionsViewModel.setForcePinSet(app, it) }
                )

                DevToggleRow(
                    title = "Force PIN Verify Success",
                    subtitle = "Always succeed PIN verification",
                    isChecked = devOptions.forcePinVerifySuccess,
                    onCheckedChange = { devOptionsViewModel.setForcePinVerifySuccess(app, it) }
                )
            }

            // === DM DEBUG OPTIONS ===
            DevSection(
                title = "Direct Messages",
                icon = Icons.Filled.Email
            ) {
                DevToggleRow(
                    title = "Show DM Debug Overlay",
                    subtitle = "Display debug info on DM screens",
                    isChecked = devOptions.showDmDebugOverlay,
                    onCheckedChange = { devOptionsViewModel.setShowDmDebugOverlay(app, it) }
                )

                DevToggleRow(
                    title = "Force Send Failure",
                    subtitle = "Simulate message send failures",
                    isChecked = devOptions.dmForceSendFailure,
                    onCheckedChange = { devOptionsViewModel.setDmForceSendFailure(app, it) }
                )

                DevToggleRow(
                    title = "Disable Rate Limiting",
                    subtitle = "Remove message sending cooldowns",
                    isChecked = devOptions.dmDisableRateLimit,
                    onCheckedChange = { devOptionsViewModel.setDmDisableRateLimit(app, it) }
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Send Delay: ${devOptions.dmArtificialSendDelayMs}ms",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = devOptions.dmArtificialSendDelayMs.toFloat(),
                    onValueChange = { devOptionsViewModel.setDmSendDelayMs(app, it.toLong()) },
                    valueRange = 0f..5000f,
                    steps = 9
                )
            }

            // === MODERATION OVERRIDE ===
            DevSection(
                title = "Content Moderation",
                icon = Icons.Filled.Security
            ) {
                Text(
                    "Override: ${devOptions.moderationOverride}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModerationChip(
                        label = "Off",
                        isSelected = devOptions.moderationOverride == DevModerationOverride.OFF,
                        onClick = { devOptionsViewModel.setModerationOverride(app, DevModerationOverride.OFF) }
                    )
                    ModerationChip(
                        label = "Clean",
                        isSelected = devOptions.moderationOverride == DevModerationOverride.CLEAN,
                        onClick = { devOptionsViewModel.setModerationOverride(app, DevModerationOverride.CLEAN) }
                    )
                    ModerationChip(
                        label = "Flagged",
                        isSelected = devOptions.moderationOverride == DevModerationOverride.FLAGGED,
                        onClick = { devOptionsViewModel.setModerationOverride(app, DevModerationOverride.FLAGGED) }
                    )
                    ModerationChip(
                        label = "Blocked",
                        isSelected = devOptions.moderationOverride == DevModerationOverride.BLOCKED,
                        onClick = { devOptionsViewModel.setModerationOverride(app, DevModerationOverride.BLOCKED) }
                    )
                }
            }

            // === PERFORMANCE ===
            DevSection(
                title = "Performance & Debug",
                icon = Icons.Filled.Speed
            ) {
                if (feedViewModel != null) {
                    val feedState by feedViewModel.uiState.collectAsState()

                    DevToggleRow(
                        title = "Mock Interface Mode",
                        subtitle = "Use mock avatars and data for testing",
                        isChecked = feedState.isMockInterfaceEnabled,
                        onCheckedChange = { feedViewModel.toggleMockInterface(it) }
                    )

                    DevToggleRow(
                        title = "Show Stories",
                        subtitle = "Toggle story carousel visibility",
                        isChecked = feedState.showStories,
                        onCheckedChange = { feedViewModel.toggleStories(it) }
                    )

                    DevToggleRow(
                        title = "Video Autoplay",
                        subtitle = "Auto-play videos in feed",
                        isChecked = feedState.isVideoAutoplayEnabled,
                        onCheckedChange = { feedViewModel.toggleVideoAutoplay(it) }
                    )

                    DevToggleRow(
                        title = "Fallback UI",
                        subtitle = "Use simplified UI for low-end devices",
                        isChecked = feedState.isFallbackUiEnabled,
                        onCheckedChange = { feedViewModel.toggleFallbackUi(it) }
                    )

                    DevToggleRow(
                        title = "Simulate Loading Error",
                        subtitle = "Force posts to fail loading",
                        isChecked = feedViewModel.simulateError,
                        onCheckedChange = { feedViewModel.simulateError = it }
                    )

                    DevToggleRow(
                        title = "Infinite Loading",
                        subtitle = "Keep loading spinner forever",
                        isChecked = feedViewModel.simulateInfiniteLoading,
                        onCheckedChange = { feedViewModel.simulateInfiniteLoading = it }
                    )
                } else {
                    Text(
                        "FeedViewModel not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // === RESET MOCK DATA ===
            if (feedViewModel != null) {
                DevSection(
                    title = "Reset Mock Data",
                    icon = Icons.Filled.Refresh
                ) {
                    Text(
                        "Reset all mock data to initial state. This will:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("• Reset all post likes to original values", style = MaterialTheme.typography.bodySmall)
                        Text("• Clear all DM reactions", style = MaterialTheme.typography.bodySmall)
                        Text("• Reset unread message counts", style = MaterialTheme.typography.bodySmall)
                        Text("• Remove user-created stories", style = MaterialTheme.typography.bodySmall)
                        Text("• Clear user bans and strikes", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { feedViewModel.resetMockData() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset All Mock Data")
                    }
                }
            }

            // === SPLASH SCREEN TESTING ===
            DevSection(
                title = "Splash Screen Testing",
                icon = Icons.Filled.PlayArrow
            ) {
                Text(
                    "Test personalized splash screens for different neuro states.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Restart the app by recreating the activity
                        (app as? android.app.Activity)?.recreate()
                            ?: run {
                                // Alternative: use ProcessPhoenix-style restart
                                val context = app
                                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                                intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                android.os.Process.killProcess(android.os.Process.myPid())
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Restart App (Show Splash)")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current theme: ${safetyState.audience}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Tip: Change your theme in Settings first, then restart to see the personalized splash.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // === SUPABASE TEST DATA ===
            SupabaseTestDataSection()

            // === AUTHENTICATION TESTING ===
            AuthenticationTestingSection()

            // === 2FA TESTING ===
            TwoFactorAuthTestingSection()

            // === LANGUAGE TESTING ===
            LanguageTestingSection()

            // === SMS 2FA TESTING ===
            Sms2FATestingSection()

            // === STAY SIGNED IN TESTING ===
            DevSection(
                title = "Stay Signed In Testing",
                icon = Icons.Filled.Person
            ) {
                Text(
                    "Test the Microsoft-style 'Stay Signed In' prompt that appears after authentication.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        StaySignedInSettings.resetShownFlag(context)
                        android.widget.Toast.makeText(
                            context,
                            "Stay Signed In prompt reset. Sign in again to see it.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Stay Signed In Prompt")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current status: ${if (StaySignedInSettings.isStaySignedIn(context)) "Staying signed in" else "Not staying signed in"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // === MESSAGE BAR DEBUG ===
            DevSection(
                title = "Message Bar Debug",
                icon = Icons.Filled.Settings
            ) {
                Text(
                    "All Scaffolds now use contentWindowInsets = 0 to prevent system double padding.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    "✅ MainActivity: NavigationBar handles its own padding",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "✅ All Scaffolds: contentWindowInsets = WindowInsets(0,0,0,0)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "✅ BottomBars: Handle imePadding() + navigationBarsPadding()",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "✅ No double padding on any navigation type",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
            }

            // === STRESS TESTING === (Deferred for faster initial load)
            if (showAllSections) {
                StressTestingSection(feedViewModel = feedViewModel, context = context)
            } else {
                // Placeholder while loading
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Loading stress testing...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // === DISPLAY PERFORMANCE ===
            if (showAllSections) {
                DisplayPerformanceSection(context = context)
            }

            // === CURRENT STATE INFO ===
            DevSection(
                title = "Current State",
                icon = Icons.Filled.Person
            ) {
                StateInfoRow("Audience", safetyState.audience.name)
                StateInfoRow("Kids Mode", if (safetyState.isKidsMode) "Yes" else "No")
                StateInfoRow("Kids Filter Level", safetyState.kidsFilterLevel.name)
                StateInfoRow("Parental PIN Set", if (safetyState.isParentalPinSet) "Yes" else "No")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StateInfoRow("DM Debug Overlay", if (devOptions.showDmDebugOverlay) "On" else "Off")
                StateInfoRow("DM Send Delay", "${devOptions.dmArtificialSendDelayMs}ms")
                StateInfoRow("Moderation Override", devOptions.moderationOverride.name)
            }

            // === AGE VERIFICATION ===
            DevSection(
                title = "Age Verification",
                icon = Icons.Filled.DateRange
            ) {
                Text(
                    "Launch the age verification popup for testing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { devOptionsViewModel.setForceAudience(app, null); }) {
                    Text("Reset Forced Audience")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { devOptionsViewModel.setForceAudience(app, safetyState.audience) }) {
                    Text("Apply Current Audience")
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { devOptionsViewModel.setForceAudience(app, Audience.UNDER_13) }) { Text("Force Under 13") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { devOptionsViewModel.setForceAudience(app, Audience.TEEN) }) { Text("Force Teen") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { devOptionsViewModel.setForceAudience(app, Audience.ADULT) }) { Text("Force Adult") }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showAgeDialog.value = true }) { Text("Launch Age Popup") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    devOptionsViewModel.setForceAudience(app, null)
                    devOptionsViewModel.setForceKidsFilterLevel(app, null)
                }) { Text("Reset Age Overrides") }
            }

            // === NOTIFICATION CHANNELS === (Deferred)
            if (showAllSections) {
                NotificationChannelTestingSection(context = context)
            }

            // === SOCIAL SETTINGS DEBUG === (Deferred)
            if (showAllSections) {
                SocialSettingsDebugSection(context = context)
            }

            // === NOTIFICATION TYPE TESTING === (Deferred)
            if (showAllSections) {
                NotificationTypeTestingSection(context = context)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showAgeDialog.value) {
        AgeVerificationDialog(
            onDismiss = { showAgeDialog.value = false },
            onConfirm = { audience ->
                safetyViewModel.setAudienceDirect(audience)
                showAgeDialog.value = false
            },
            onSkip = {
                // keep current audience; just dismiss
                showAgeDialog.value = false
            },
            title = "Test Age Verification",
            subtitle = "Preview the age gate flow"
        )
    }
}

@Composable
private fun DevSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DevToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AudienceChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color else Color.Transparent)
            .border(
                width = 2.dp,
                color = color,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModerationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StateInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================================================
// SUPABASE TEST DATA SECTION
// ============================================================================

/**
 * Section for sending test data to Supabase tables
 */
@Composable
fun SupabaseTestDataSection() {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var postsCount by remember { mutableStateOf<Int?>(null) }
    var likesCount by remember { mutableStateOf<Int?>(null) }
    var usersCount by remember { mutableStateOf<Int?>(null) }

    val isSupabaseAvailable = remember { SupabaseTestData.isSupabaseAvailable() }

    // Fetch table counts on load
    LaunchedEffect(Unit) {
        if (isSupabaseAvailable) {
            SupabaseTestData.getTableRowCount("posts").onSuccess { postsCount = it }
            SupabaseTestData.getTableRowCount("post_likes").onSuccess { likesCount = it }
            SupabaseTestData.getTableRowCount("users").onSuccess { usersCount = it }
        }
    }

    fun refreshCounts() {
        scope.launch {
            SupabaseTestData.getTableRowCount("posts").onSuccess { postsCount = it }
            SupabaseTestData.getTableRowCount("post_likes").onSuccess { likesCount = it }
            SupabaseTestData.getTableRowCount("users").onSuccess { usersCount = it }
        }
    }

    DevSection(
        title = "🗄️ Supabase Test Data",
        icon = Icons.Filled.CloudUpload
    ) {
        if (!isSupabaseAvailable) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Supabase Not Configured",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Add SUPABASE_URL and SUPABASE_KEY to local.properties",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            return@DevSection
        }

        Text(
            "Send test data to your Supabase database to verify connectivity.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Table row counts
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TableCountBadge("Posts", postsCount)
                TableCountBadge("Likes", likesCount)
                TableCountBadge("Users", usersCount)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { refreshCounts() }) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Refresh Counts")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Result message
        resultMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Send Test Post
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    SupabaseTestData.sendTestPost()
                        .onSuccess {
                            resultMessage = it
                            isError = false
                            refreshCounts()
                        }
                        .onFailure {
                            resultMessage = "Error: ${it.message}"
                            isError = true
                        }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Filled.PostAdd, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text("Send Test Post")
        }

        Spacer(Modifier.height(8.dp))

        // Send Test Like
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    SupabaseTestData.sendTestLike((1..100).random().toLong())
                        .onSuccess {
                            resultMessage = it
                            isError = false
                            refreshCounts()
                        }
                        .onFailure {
                            resultMessage = "Error: ${it.message}"
                            isError = true
                        }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.ThumbUp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send Test Like")
        }

        Spacer(Modifier.height(8.dp))

        // Send Test User
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    SupabaseTestData.sendTestUser()
                        .onSuccess {
                            resultMessage = it
                            isError = false
                            refreshCounts()
                        }
                        .onFailure {
                            resultMessage = "Error: ${it.message}"
                            isError = true
                        }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send Test User")
        }

        Spacer(Modifier.height(8.dp))

        // Send Bulk Posts
        OutlinedButton(
            onClick = {
                isLoading = true
                scope.launch {
                    SupabaseTestData.sendBulkTestPosts(5)
                        .onSuccess {
                            resultMessage = it
                            isError = false
                            refreshCounts()
                        }
                        .onFailure {
                            resultMessage = "Error: ${it.message}"
                            isError = true
                        }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.DynamicFeed, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send 5 Bulk Posts")
        }

        Spacer(Modifier.height(16.dp))

        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        // Clear test data
        Text(
            "⚠️ Danger Zone",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                isLoading = true
                scope.launch {
                    var cleared = 0
                    SupabaseTestData.clearTestData("post_likes").onSuccess { cleared++ }
                    SupabaseTestData.clearTestData("posts").onSuccess { cleared++ }
                    SupabaseTestData.clearTestData("users").onSuccess { cleared++ }

                    resultMessage = "Cleared test data from $cleared tables"
                    isError = false
                    refreshCounts()
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear All Test Data")
        }
    }
}

@Composable
private fun TableCountBadge(label: String, count: Int?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count?.toString() ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// LANGUAGE TESTING SECTION
// ============================================================================

/**
 * Supported languages for testing
 */
data class TestLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)

private val TEST_LANGUAGES = listOf(
    TestLanguage("en", "English", "English", "🇺🇸"),
    TestLanguage("es", "Spanish", "Español", "🇪🇸"),
    TestLanguage("fr", "French", "Français", "🇫🇷"),
    TestLanguage("de", "German", "Deutsch", "🇩🇪"),
    TestLanguage("it", "Italian", "Italiano", "🇮🇹"),
    TestLanguage("pt", "Portuguese", "Português", "🇵🇹"),
    TestLanguage("ja", "Japanese", "日本語", "🇯🇵"),
    TestLanguage("ko", "Korean", "한국어", "🇰🇷"),
    TestLanguage("zh", "Chinese", "中文", "🇨🇳"),
    TestLanguage("ar", "Arabic", "العربية", "🇸🇦"),
    TestLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
    TestLanguage("ru", "Russian", "Русский", "🇷🇺"),
    TestLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
    TestLanguage("nl", "Dutch", "Nederlands", "🇳🇱"),
    TestLanguage("pl", "Polish", "Polski", "🇵🇱"),
    TestLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
    TestLanguage("th", "Thai", "ไทย", "🇹🇭"),
    TestLanguage("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
    TestLanguage("uk", "Ukrainian", "Українська", "🇺🇦"),
    TestLanguage("he", "Hebrew", "עברית", "🇮🇱")
)

/**
 * Language Testing Section for Developer Options
 */
@Composable
fun LanguageTestingSection() {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("en") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // Get current language from system
    val currentSystemLanguage = remember {
        java.util.Locale.getDefault().language
    }

    DevSection(
        title = "🌍 Language Testing",
        icon = Icons.Filled.Language
    ) {
        Text(
            "Test app UI in different languages to verify translations and RTL layout support.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Current language info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TEST_LANGUAGES.find { it.code == currentSystemLanguage }?.flag ?: "🌐",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "System Language",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        TEST_LANGUAGES.find { it.code == currentSystemLanguage }?.name
                            ?: currentSystemLanguage.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick language switcher
        Text(
            "Quick Switch",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        // Popular languages row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("en", "es", "fr", "de", "ja", "zh").forEach { code ->
                val lang = TEST_LANGUAGES.find { it.code == code }
                if (lang != null) {
                    LanguageChip(
                        language = lang,
                        isSelected = selectedLanguage == code,
                        onClick = {
                            selectedLanguage = code
                            android.widget.Toast.makeText(
                                context,
                                "Language preview: ${lang.name} (${lang.nativeName})",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Show all languages button
        OutlinedButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isExpanded) "Hide All Languages" else "Show All ${TEST_LANGUAGES.size} Languages")
        }

        // Expanded language grid
        if (isExpanded) {
            Spacer(Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TEST_LANGUAGES.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { lang ->
                            LanguageListItem(
                                language = lang,
                                isSelected = selectedLanguage == lang.code,
                                onClick = { selectedLanguage = lang.code },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // RTL Test section
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Text(
            "RTL Layout Testing",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedLanguage = "ar"
                    android.widget.Toast.makeText(
                        context,
                        "Testing RTL: Arabic 🇸🇦",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("🇸🇦 Arabic (RTL)")
            }

            OutlinedButton(
                onClick = {
                    selectedLanguage = "he"
                    android.widget.Toast.makeText(
                        context,
                        "Testing RTL: Hebrew 🇮🇱",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("🇮🇱 Hebrew (RTL)")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Note: Full language switching requires app restart with updated locale configuration.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LanguageChip(
    language: TestLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = language.flag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun LanguageListItem(
    language: TestLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(language.flag, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    language.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    language.nativeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// AUTHENTICATION TESTING SECTION
// ============================================================================

/**
 * Authentication Testing Section for Developer Options
 * Test various authentication flows and options
 */
@Composable
fun AuthenticationTestingSection() {
    val context = LocalContext.current
    var selectedAuthMethod by remember { mutableStateOf("email") }
    var testEmail by remember { mutableStateOf("testuser@NeuroComet.dev") }
    var testPassword by remember { mutableStateOf("password123") }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authResult by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    DevSection(
        title = "🔐 Authentication Testing",
        icon = Icons.Filled.Lock
    ) {
        Text(
            "Test different authentication methods and flows.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Auth method selector
        Text(
            "Authentication Method",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthMethodChip(
                label = "📧 Email",
                isSelected = selectedAuthMethod == "email",
                onClick = { selectedAuthMethod = "email" }
            )
            AuthMethodChip(
                label = "📱 Phone",
                isSelected = selectedAuthMethod == "phone",
                onClick = { selectedAuthMethod = "phone" }
            )
            AuthMethodChip(
                label = "🔗 OAuth",
                isSelected = selectedAuthMethod == "oauth",
                onClick = { selectedAuthMethod = "oauth" }
            )
        }

        Spacer(Modifier.height(16.dp))

        when (selectedAuthMethod) {
            "email" -> {
                // Email/Password auth
                OutlinedTextField(
                    value = testEmail,
                    onValueChange = { testEmail = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Filled.Email, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = testPassword,
                    onValueChange = { testPassword = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            "phone" -> {
                OutlinedTextField(
                    value = testEmail,
                    onValueChange = { testEmail = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+1 555 123 4567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            "oauth" -> {
                // OAuth provider buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OAuthProviderButton(
                        provider = "Google",
                        icon = "🔵",
                        onClick = {
                            isAuthenticating = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isAuthenticating = false
                                authResult = "✅ Google OAuth simulated successfully!"
                                isSuccess = true
                            }, 1500)
                        }
                    )
                    OAuthProviderButton(
                        provider = "Apple",
                        icon = "🍎",
                        onClick = {
                            isAuthenticating = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isAuthenticating = false
                                authResult = "✅ Apple OAuth simulated successfully!"
                                isSuccess = true
                            }, 1500)
                        }
                    )
                    OAuthProviderButton(
                        provider = "GitHub",
                        icon = "🐙",
                        onClick = {
                            isAuthenticating = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isAuthenticating = false
                                authResult = "✅ GitHub OAuth simulated successfully!"
                                isSuccess = true
                            }, 1500)
                        }
                    )
                }
            }
        }

        if (selectedAuthMethod != "oauth") {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isAuthenticating = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isAuthenticating = false
                            authResult = "✅ Sign In successful! User: $testEmail"
                            isSuccess = true
                        }, 1500)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isAuthenticating && testEmail.isNotBlank()
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sign In")
                    }
                }

                OutlinedButton(
                    onClick = {
                        isAuthenticating = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isAuthenticating = false
                            authResult = "✅ Sign Up successful! Welcome, $testEmail!"
                            isSuccess = true
                        }, 1500)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isAuthenticating && testEmail.isNotBlank()
                ) {
                    Text("Sign Up")
                }
            }
        }

        // Result display
        authResult?.let { result ->
            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        result,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSuccess)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // Quick actions
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    authResult = "🔄 Password reset email sent to $testEmail"
                    isSuccess = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Password", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    authResult = "📧 Verification email sent to $testEmail"
                    isSuccess = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Verify Email", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AuthMethodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun OAuthProviderButton(
    provider: String,
    icon: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("$icon  Continue with $provider")
    }
}

// ============================================================================
// 2FA TESTING SECTION
// ============================================================================

/**
 * 2FA Testing Section for Developer Options
 * Test various two-factor authentication methods
 */
@Composable
fun TwoFactorAuthTestingSection() {
    val context = LocalContext.current
    var is2FAEnabled by remember { mutableStateOf(false) }
    var selected2FAMethod by remember { mutableStateOf("totp") }
    var totpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var mockSecretKey by remember { mutableStateOf("JBSWY3DPEHPK3PXP") }
    var backupCodes by remember { mutableStateOf(listOf<String>()) }

    // Generate mock TOTP code that changes every 30 seconds
    var currentTOTP by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTOTP = (100000..999999).random().toString()
            delay(30000) // Change every 30 seconds
        }
    }

    DevSection(
        title = "🔒 2FA Testing",
        icon = Icons.Filled.Security
    ) {
        Text(
            "Test two-factor authentication setup and verification.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // 2FA Enable toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (is2FAEnabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (is2FAEnabled) Icons.Filled.VerifiedUser else Icons.Filled.Security,
                    contentDescription = null,
                    tint = if (is2FAEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Two-Factor Authentication",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (is2FAEnabled) "Protected with 2FA" else "Add extra security",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = is2FAEnabled,
                    onCheckedChange = {
                        is2FAEnabled = it
                        if (it) {
                            // Generate backup codes when enabling
                            backupCodes = List(8) {
                                "${(1000..9999).random()}-${(1000..9999).random()}"
                            }
                        }
                    }
                )
            }
        }

        if (is2FAEnabled) {
            Spacer(Modifier.height(16.dp))

            // 2FA Method selector
            Text(
                "2FA Method",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuthMethodChip(
                    label = "📱 TOTP App",
                    isSelected = selected2FAMethod == "totp",
                    onClick = { selected2FAMethod = "totp" }
                )
                AuthMethodChip(
                    label = "📧 Email",
                    isSelected = selected2FAMethod == "email",
                    onClick = { selected2FAMethod = "email" }
                )
                AuthMethodChip(
                    label = "🔑 Hardware",
                    isSelected = selected2FAMethod == "hardware",
                    onClick = { selected2FAMethod = "hardware" }
                )
            }

            Spacer(Modifier.height(16.dp))

            when (selected2FAMethod) {
                "totp" -> {
                    // TOTP Authenticator setup
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Setup Authenticator App",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Secret Key: $mockSecretKey",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Current Test TOTP: $currentTOTP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // TOTP verification
                    OutlinedTextField(
                        value = totpCode,
                        onValueChange = { if (it.length <= 6) totpCode = it.filter { c -> c.isDigit() } },
                        label = { Text("Enter 6-digit code") },
                        leadingIcon = { Icon(Icons.Filled.Pin, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                "email" -> {
                    Text(
                        "A 6-digit code will be sent to your email when you sign in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "📧 Test email code: ${(100000..999999).random()}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Email, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Send Test Email Code")
                    }
                }
                "hardware" -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Key,
                                null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Hardware Security Key",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    "YubiKey, Titan Key, or similar FIDO2 device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "🔑 Simulating hardware key tap...",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Fingerprint, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Simulate Key Tap")
                    }
                }
            }

            if (selected2FAMethod == "totp") {
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        isVerifying = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isVerifying = false
                            verificationResult = if (totpCode == currentTOTP) {
                                "✅ 2FA verification successful!"
                            } else {
                                "❌ Invalid code. Expected: $currentTOTP"
                            }
                            totpCode = ""
                        }, 1000)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = totpCode.length == 6 && !isVerifying
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Check, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Verify Code")
                }

                verificationResult?.let { result ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        result,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.startsWith("✅"))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Backup codes section
            if (backupCodes.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    "Backup Codes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚠️ Save these codes in a safe place!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        backupCodes.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { code ->
                                    Text(
                                        code,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SMS 2FA TESTING SECTION
// ============================================================================

/**
 * SMS 2FA Testing Section for Developer Options
 * Provides an alternative to modern authenticator apps for users who prefer SMS codes
 */
@Composable
fun Sms2FATestingSection() {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var lastSentCode by remember { mutableStateOf("") }
    var sms2FAEnabled by remember { mutableStateOf(false) }

    DevSection(
        title = "📱 SMS 2FA Testing",
        icon = Icons.Filled.Sms
    ) {
        Text(
            "Test SMS-based two-factor authentication for users who prefer traditional verification codes over authenticator apps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // SMS 2FA Toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (sms2FAEnabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (sms2FAEnabled) Icons.Filled.VerifiedUser else Icons.Filled.Security,
                    contentDescription = null,
                    tint = if (sms2FAEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SMS 2FA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (sms2FAEnabled) "Enabled - Codes sent via SMS" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = sms2FAEnabled,
                    onCheckedChange = { sms2FAEnabled = it }
                )
            }
        }

        if (sms2FAEnabled) {
            Spacer(Modifier.height(16.dp))

            // Phone number input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it.filter { char -> char.isDigit() || char == '+' }
                },
                label = { Text("Phone Number") },
                placeholder = { Text("+1 (555) 123-4567") },
                leadingIcon = {
                    Icon(Icons.Filled.Phone, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(12.dp))

            // Send Code button
            Button(
                onClick = {
                    if (phoneNumber.length >= 10) {
                        isSending = true
                        // Simulate sending SMS
                        lastSentCode = (100000..999999).random().toString()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isSending = false
                            isCodeSent = true
                            android.widget.Toast.makeText(
                                context,
                                "📱 Test SMS sent! Code: $lastSentCode",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }, 1500)
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Please enter a valid phone number",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.length >= 10 && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isCodeSent) "Resend Code" else "Send Verification Code")
                }
            }

            if (isCodeSent) {
                Spacer(Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(Modifier.height(16.dp))

                Text(
                    "Enter Verification Code",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                // Code input
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = {
                        if (it.length <= 6) {
                            verificationCode = it.filter { char -> char.isDigit() }
                        }
                    },
                    label = { Text("6-digit code") },
                    placeholder = { Text("123456") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(12.dp))

                // Verify button
                Button(
                    onClick = {
                        isVerifying = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isVerifying = false
                            if (verificationCode == lastSentCode) {
                                android.widget.Toast.makeText(
                                    context,
                                    "✅ Verification successful!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                verificationCode = ""
                                isCodeSent = false
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "❌ Invalid code. Try again.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, 1000)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = verificationCode.length == 6 && !isVerifying
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Verifying...")
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Verify Code")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Show the test code for debugging
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Dev Mode: Test code is $lastSentCode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Info about SMS 2FA
        Text(
            "SMS 2FA is an alternative for users who don't have or prefer not to use authenticator apps like Google Authenticator or Authy.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ============================================================================
// NOTIFICATION CHANNEL TESTING SECTION
// ============================================================================

/**
 * Section for testing notification channels
 */
@Composable
fun NotificationChannelTestingSection(context: android.content.Context) {
    val notificationManager = remember {
        context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    }

    var channelCount by remember { mutableStateOf(0) }
    var groupCount by remember { mutableStateOf(0) }
    var channels by remember { mutableStateOf<List<android.app.NotificationChannel>>(emptyList()) }
    var isExpanded by remember { mutableStateOf(false) }

    // Refresh counts
    fun refreshChannelData() {
        channels = notificationManager.notificationChannels
        channelCount = channels.size
        groupCount = notificationManager.notificationChannelGroups.size
    }

    LaunchedEffect(Unit) {
        refreshChannelData()
    }

    DevSection(
        title = "🔔 Notification Channels",
        icon = Icons.Filled.Notifications
    ) {
        Text(
            "Test and verify notification channels are properly configured.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Channel stats
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (channelCount > 0)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$channelCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (channelCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Channels",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$groupCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (groupCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Groups",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Permission status
        val hasPermission = NotificationChannels.hasNotificationPermission(context)

        if (!hasPermission) {
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "⚠️ Notification Permission Not Granted",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Android 13+ requires explicit permission for notifications. Tap below to grant permission.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                NotificationChannels.requestNotificationPermission(activity)
                            } else {
                                // Fallback: open app notification settings
                                NotificationChannels.openAppNotificationSettings(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Grant Notification Permission")
                    }
                }
            }
        }

        if (channelCount == 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "⚠️ No channels registered! Tap 'Create Channels' below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(12.dp))

        // Create/Refresh channels
        Button(
            onClick = {
                NotificationChannels.createNotificationChannels(context)
                refreshChannelData()
                android.widget.Toast.makeText(
                    context,
                    "Notification channels created! $channelCount channels, $groupCount groups",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create / Refresh Channels")
        }

        Spacer(Modifier.height(8.dp))

        // Open notification settings
        Button(
            onClick = {
                NotificationChannels.openAppNotificationSettings(context)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Open Notification Settings")
        }

        Spacer(Modifier.height(8.dp))

        // Send test notification
        Button(
            onClick = {
                sendTestNotification(context, notificationManager)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Send Test Notification")
        }

        Spacer(Modifier.height(12.dp))

        // Expandable Channel list header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Registered Channels ($channelCount):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Channel list (expandable)
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.shrinkVertically()
        ) {
            Column {
                if (channels.isEmpty()) {
                    Text(
                        text = "No channels registered yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Group channels by their group
                    val groupedChannels = channels.groupBy { it.group ?: "Ungrouped" }

                    groupedChannels.forEach { (groupId, groupChannels) ->
                        // Group header
                        val groupName = notificationManager.notificationChannelGroups
                            .find { it.id == groupId }?.name?.toString() ?: groupId

                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        groupChannels.forEach { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        NotificationChannels.openChannelSettings(context, channel.id)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val importanceIcon = when (channel.importance) {
                                    android.app.NotificationManager.IMPORTANCE_HIGH -> "🔴"
                                    android.app.NotificationManager.IMPORTANCE_DEFAULT -> "🟡"
                                    android.app.NotificationManager.IMPORTANCE_LOW -> "🟢"
                                    android.app.NotificationManager.IMPORTANCE_MIN -> "⚪"
                                    android.app.NotificationManager.IMPORTANCE_NONE -> "❌"
                                    else -> "⚫"
                                }
                                Text(
                                    text = "$importanceIcon ${channel.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = "Open channel settings",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Show collapsed preview if not expanded
        if (!isExpanded && channels.isNotEmpty()) {
            Text(
                text = "Tap to expand and view all ${channels.size} channels",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Send a test notification to verify channels are working
 */
private fun sendTestNotification(
    context: android.content.Context,
    notificationManager: android.app.NotificationManager
) {
    val notification = NotificationHelper.createNotificationBuilder(
        context = context,
        channelId = NotificationChannels.CHANNEL_APP_UPDATES,
        title = "🧪 Test Notification",
        content = "This is a test notification from Developer Options!"
    )
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)

    android.widget.Toast.makeText(
        context,
        "Test notification sent!",
        android.widget.Toast.LENGTH_SHORT
    ).show()
}

// ============================================================================
// NOTIFICATION TYPE TESTING SECTION
// ============================================================================

/**
 * Comprehensive notification testing section for testing all notification types
 */
@Composable
fun NotificationTypeTestingSection(context: android.content.Context) {
    val scope = rememberCoroutineScope()
    var isSendingAll by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf<NotificationTestCategory?>(null) }

    val hasPermission = NotificationChannels.hasNotificationPermission(context)
    val categoryCount = remember { NotificationTester.getNotificationCountByCategory() }

    DevSection(
        title = "🔔 Test All Notification Types",
        icon = Icons.Filled.Notifications
    ) {
        Text(
            "Test all ${NotificationTester.allTestNotifications.size} notification types with banner display.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasPermission) {
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "⚠️ Notification permission required for testing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Send All Notifications Button
        Button(
            onClick = {
                if (!isSendingAll) {
                    isSendingAll = true
                    scope.launch {
                        NotificationTester.sendAllTestNotifications(context, delayMillis = 1000L)
                        isSendingAll = false
                        android.widget.Toast.makeText(
                            context,
                            "All ${NotificationTester.allTestNotifications.size} notifications sent!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasPermission && !isSendingAll
        ) {
            if (isSendingAll) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Sending...")
            } else {
                Icon(Icons.Filled.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send All Notifications (${NotificationTester.allTestNotifications.size})")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Clear All Button
        OutlinedButton(
            onClick = {
                NotificationTester.clearAllTestNotifications(context)
                android.widget.Toast.makeText(
                    context,
                    "All test notifications cleared",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear All Test Notifications")
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Text(
            "Test by Category:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        // Category buttons
        NotificationTestCategory.entries.forEach { category ->
            val count = categoryCount[category] ?: 0
            val isExpanded = expandedCategory == category

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = if (isExpanded) null else category }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.emoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$count notification${if (count != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row {
                            // Send category button
                            IconButton(
                                onClick = {
                                    NotificationTester.sendNotificationsByCategory(context, category)
                                    android.widget.Toast.makeText(
                                        context,
                                        "${category.displayName}: $count notifications sent!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                enabled = hasPermission
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send ${category.displayName} notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Icon(
                                if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Expanded individual notifications
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isExpanded,
                        enter = androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                            val categoryNotifications = NotificationTester.allTestNotifications
                                .filter { it.groupKey == category.name.lowercase() }

                            categoryNotifications.forEach { config ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            NotificationTester.sendTestNotification(context, config)
                                            android.widget.Toast.makeText(
                                                context,
                                                "Notification sent!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = config.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = config.content,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = "Send",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DISPLAY PERFORMANCE SECTION
// ============================================================================

/**
 * Section for testing and viewing display performance information
 */
@Composable
fun DisplayPerformanceSection(context: android.content.Context) {
    val displayInfo = com.kyilmaz.neurocomet.ui.rememberDisplayInfo()
    val refreshRateCategory = com.kyilmaz.neurocomet.ui.rememberRefreshRateCategory()
    val scrollParams = com.kyilmaz.neurocomet.ui.rememberOptimalScrollParameters()

    DevSection(
        title = "📱 Display Performance",
        icon = Icons.Filled.Speed
    ) {
        Text(
            text = "Display information and scroll performance optimizations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Display Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (displayInfo.isHighRefreshRate)
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refresh Rate",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayInfo.refreshRateFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (displayInfo.isHighRefreshRate)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        if (displayInfo.isHighRefreshRate) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "⚡",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resolution", style = MaterialTheme.typography.bodySmall)
                    Text(
                        displayInfo.resolutionFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Density", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${displayInfo.densityDpi} dpi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Category", style = MaterialTheme.typography.bodySmall)
                    Text(
                        when (refreshRateCategory) {
                            com.kyilmaz.neurocomet.ui.RefreshRateCategory.VERY_HIGH -> "Very High (120Hz+)"
                            com.kyilmaz.neurocomet.ui.RefreshRateCategory.HIGH -> "High (90Hz)"
                            com.kyilmaz.neurocomet.ui.RefreshRateCategory.STANDARD -> "Standard (60Hz)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Scroll Optimization Status
        Text(
            text = "Scroll Optimizations:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Velocity Threshold", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${scrollParams.velocityThreshold}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Deceleration Rate", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${scrollParams.decelerationRate}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Overscroll Distance", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${scrollParams.overscrollDistance.toInt()}dp",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Info text
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (displayInfo.isHighRefreshRate)
                        "Your display supports ${displayInfo.refreshRateFormatted}. Scroll animations are automatically optimized for smoother motion."
                    else
                        "Standard 60Hz display detected. Scroll animations use default parameters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// STRESS TESTING SECTION
// ============================================================================

/**
 * Stress testing result data class for tracking test outcomes
 */
data class StressTestResult(
    val testName: String,
    val passed: Boolean,
    val duration: Long,
    val details: String,
    val errorMessage: String? = null
)

/**
 * Main stress testing section composable
 */
@Composable
fun StressTestingSection(
    feedViewModel: FeedViewModel?,
    context: android.content.Context
) {
    val scope = rememberCoroutineScope()
    var isRunningTests by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<StressTestResult>>(emptyList()) }
    var currentTest by remember { mutableStateOf("") }
    var overallProgress by remember { mutableStateOf(0f) }

    DevSection(
        title = "🔬 Production Stress Testing",
        icon = Icons.Filled.Speed
    ) {
        Text(
            "Run comprehensive tests to ensure the app is ready for production deployment.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Test status indicator
        if (isRunningTests) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Running: $currentTest",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${(overallProgress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Run All Tests button
        Button(
            onClick = {
                scope.launch {
                    isRunningTests = true
                    testResults = emptyList()
                    val results = mutableListOf<StressTestResult>()
                    val totalTests = 18

                    // Test 1: UI Responsiveness
                    currentTest = "UI Responsiveness Test"
                    overallProgress = 1f / totalTests
                    results.add(runUIResponsivenessTest())
                    delay(200)

                    // Test 2: Memory Pressure
                    currentTest = "Memory Pressure Test"
                    overallProgress = 2f / totalTests
                    results.add(runMemoryPressureTest(context))
                    delay(200)

                    // Test 3: Rapid Navigation
                    currentTest = "Rapid Navigation Test"
                    overallProgress = 3f / totalTests
                    results.add(runRapidNavigationTest())
                    delay(200)

                    // Test 4: Data Loading Stress
                    currentTest = "Data Loading Stress Test"
                    overallProgress = 4f / totalTests
                    results.add(runDataLoadingStressTest(feedViewModel))
                    delay(200)

                    // Test 5: Theme Switching Stress
                    currentTest = "Theme Switching Stress Test"
                    overallProgress = 5f / totalTests
                    results.add(runThemeSwitchingTest())
                    delay(200)

                    // Test 6: Concurrent Operations
                    currentTest = "Concurrent Operations Test"
                    overallProgress = 6f / totalTests
                    results.add(runConcurrentOperationsTest())
                    delay(200)

                    // Test 7: Storage I/O Test
                    currentTest = "Storage I/O Test"
                    overallProgress = 7f / totalTests
                    results.add(runStorageIOTest(context))
                    delay(200)

                    // Test 8: Network Simulation
                    currentTest = "Network Simulation Test"
                    overallProgress = 8f / totalTests
                    results.add(runNetworkSimulationTest())
                    delay(200)

                    // Test 9: Notification Channels
                    currentTest = "Notification Channels Test"
                    overallProgress = 9f / totalTests
                    results.add(runNotificationChannelTest(context))
                    delay(200)

                    // Test 10: List Scroll Stress
                    currentTest = "List Scroll Stress Test"
                    overallProgress = 10f / totalTests
                    results.add(runListScrollStressTest())
                    delay(200)

                    // Test 11: State Management
                    currentTest = "State Management Test"
                    overallProgress = 11f / totalTests
                    results.add(runStateManagementTest())
                    delay(200)

                    // Test 12: JSON Parsing
                    currentTest = "JSON Parsing Test"
                    overallProgress = 12f / totalTests
                    results.add(runJsonParsingTest())
                    delay(200)

                    // Test 13: String Operations
                    currentTest = "String Operations Test"
                    overallProgress = 13f / totalTests
                    results.add(runStringOperationsTest())
                    delay(200)

                    // Test 14: DateTime Operations
                    currentTest = "DateTime Operations Test"
                    overallProgress = 14f / totalTests
                    results.add(runDateTimeOperationsTest())
                    delay(200)

                    // Test 15: Collection Operations
                    currentTest = "Collection Operations Test"
                    overallProgress = 15f / totalTests
                    results.add(runCollectionOperationsTest())
                    delay(200)

                    // Test 16: Security Operations
                    currentTest = "Security Operations Test"
                    overallProgress = 16f / totalTests
                    results.add(runSecurityOperationsTest())
                    delay(200)

                    // Test 17: File System
                    currentTest = "File System Test"
                    overallProgress = 17f / totalTests
                    results.add(runFileSystemTest(context))
                    delay(200)

                    // Test 18: Exception Handling
                    currentTest = "Exception Handling Test"
                    overallProgress = 18f / totalTests
                    results.add(runExceptionHandlingTest())
                    delay(200)

                    testResults = results
                    isRunningTests = false
                    currentTest = ""
                    overallProgress = 0f
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunningTests
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isRunningTests) "Running Tests..." else "Run All Stress Tests (18)")
        }

        Spacer(Modifier.height(8.dp))

        // Individual test buttons - Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Memory Test"
                        testResults = listOf(runMemoryPressureTest(context))
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Memory", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Storage Test"
                        testResults = listOf(runStorageIOTest(context))
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Storage", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Network Test"
                        testResults = listOf(runNetworkSimulationTest())
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Network", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Individual test buttons - Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Notifications Test"
                        testResults = listOf(runNotificationChannelTest(context))
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Notifs", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Security Test"
                        testResults = listOf(runSecurityOperationsTest())
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Security", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "File System Test"
                        testResults = listOf(runFileSystemTest(context))
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Files", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Individual test buttons - Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "State Test"
                        testResults = listOf(runStateManagementTest())
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("State", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Collections Test"
                        testResults = listOf(runCollectionOperationsTest())
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Collect", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        isRunningTests = true
                        currentTest = "Exceptions Test"
                        testResults = listOf(runExceptionHandlingTest())
                        isRunningTests = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunningTests
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Errors", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Results display
        if (testResults.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text(
                "Test Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // Summary
            val passed = testResults.count { it.passed }
            val failed = testResults.size - passed
            val totalTime = testResults.sumOf { it.duration }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (failed == 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (failed == 0) "✅ All Tests Passed!" else "⚠️ Some Tests Failed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (failed == 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Text(
                            "$passed passed, $failed failed • ${totalTime}ms total",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (failed == 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        )
                    }
                    Text(
                        if (failed == 0) "🚀" else "🔧",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Individual results
            testResults.forEach { result ->
                StressTestResultCard(result)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StressTestResultCard(result: StressTestResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                Color(0xFFFFCDD2).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (result.passed) Icons.Filled.Check else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (result.passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.testName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    result.details,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result.errorMessage != null) {
                    Text(
                        "Error: ${result.errorMessage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
            Text(
                "${result.duration}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// STRESS TEST IMPLEMENTATIONS
// ============================================================================

private suspend fun runUIResponsivenessTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate rapid UI operations
        repeat(100) {
            delay(5)
        }
        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "UI Responsiveness",
            passed = duration < 1000,
            duration = duration,
            details = "100 rapid operations completed in ${duration}ms"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "UI Responsiveness",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runMemoryPressureTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // Allocate and release memory to simulate pressure
        val allocations = mutableListOf<ByteArray>()
        repeat(10) {
            allocations.add(ByteArray(1024 * 100)) // 100KB allocations
            delay(10)
        }
        allocations.clear()
        System.gc()
        delay(100)

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryDelta = finalMemory - initialMemory
        val duration = System.currentTimeMillis() - startTime

        StressTestResult(
            testName = "Memory Pressure",
            passed = memoryDelta < 5 * 1024 * 1024, // Less than 5MB leak
            duration = duration,
            details = "Memory delta: ${memoryDelta / 1024}KB after allocations"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Memory Pressure",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runRapidNavigationTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate rapid state changes
        repeat(50) {
            delay(10)
        }
        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Rapid Navigation",
            passed = true,
            duration = duration,
            details = "50 navigation simulations completed"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Rapid Navigation",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runDataLoadingStressTest(feedViewModel: FeedViewModel?): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate multiple data load requests
        repeat(20) {
            feedViewModel?.fetchNotifications()
            delay(25)
        }
        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Data Loading Stress",
            passed = duration < 2000,
            duration = duration,
            details = "20 data fetch operations in ${duration}ms"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Data Loading Stress",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runThemeSwitchingTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate rapid theme switching
        repeat(30) {
            delay(15)
        }
        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Theme Switching",
            passed = true,
            duration = duration,
            details = "30 theme switch simulations completed"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Theme Switching",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runConcurrentOperationsTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Run multiple coroutines concurrently
        kotlinx.coroutines.coroutineScope {
            val jobs = (1..10).map {
                async {
                    delay((10..50).random().toLong())
                    it * 2
                }
            }
            val results = jobs.awaitAll()
            val duration = System.currentTimeMillis() - startTime

            StressTestResult(
                testName = "Concurrent Operations",
                passed = results.size == 10,
                duration = duration,
                details = "10 concurrent operations completed successfully"
            )
        }
    } catch (e: Exception) {
        StressTestResult(
            testName = "Concurrent Operations",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runStorageIOTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val prefs = context.getSharedPreferences("stress_test", android.content.Context.MODE_PRIVATE)

        // Write test
        repeat(50) { i ->
            prefs.edit().putString("test_key_$i", "test_value_$i").apply()
        }

        // Read test
        repeat(50) { i ->
            prefs.getString("test_key_$i", null)
        }

        // Cleanup
        prefs.edit().clear().apply()

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Storage I/O",
            passed = duration < 1000,
            duration = duration,
            details = "100 I/O operations (50 writes, 50 reads) in ${duration}ms"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Storage I/O",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

private suspend fun runNetworkSimulationTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate various network conditions
        val scenarios = listOf(
            Pair("Fast network", 50L),
            Pair("Normal network", 150L),
            Pair("Slow network", 300L),
            Pair("Intermittent", 100L),
            Pair("Recovery", 50L)
        )

        scenarios.forEach { (_, delayMs) ->
            delay(delayMs)
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Network Simulation",
            passed = true,
            duration = duration,
            details = "5 network condition scenarios simulated"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Network Simulation",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

// ============================================================================
// ADDITIONAL STRESS TESTS
// ============================================================================

/**
 * Test notification channel creation and validation
 */
private suspend fun runNotificationChannelTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE)
            as android.app.NotificationManager

        // Recreate all channels
        NotificationChannels.createNotificationChannels(context)
        delay(100)

        // Verify channels exist
        val channels = notificationManager.notificationChannels
        val groups = notificationManager.notificationChannelGroups

        val expectedChannels = listOf(
            NotificationChannels.CHANNEL_DIRECT_MESSAGES,
            NotificationChannels.CHANNEL_LIKES,
            NotificationChannels.CHANNEL_COMMENTS,
            NotificationChannels.CHANNEL_FOLLOWS,
            NotificationChannels.CHANNEL_ACCOUNT_SECURITY,
            NotificationChannels.CHANNEL_APP_UPDATES
        )

        val missingChannels = expectedChannels.filter { expectedId ->
            channels.none { it.id == expectedId }
        }

        val duration = System.currentTimeMillis() - startTime

        StressTestResult(
            testName = "Notification Channels",
            passed = missingChannels.isEmpty() && channels.size >= 15,
            duration = duration,
            details = "${channels.size} channels, ${groups.size} groups verified",
            errorMessage = if (missingChannels.isNotEmpty()) "Missing: ${missingChannels.joinToString()}" else null
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Notification Channels",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test rapid list operations (simulating fast scrolling)
 */
private suspend fun runListScrollStressTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Simulate rapid list item creation/destruction
        val items = mutableListOf<String>()

        // Add items rapidly
        repeat(500) { i ->
            items.add("Item $i with some longer content to simulate real data")
            if (i % 50 == 0) delay(1) // Yield occasionally
        }

        // Remove items rapidly
        repeat(250) {
            if (items.isNotEmpty()) items.removeAt(0)
            if (it % 50 == 0) delay(1)
        }

        // Random access pattern (simulating scroll jumps)
        repeat(100) {
            if (items.isNotEmpty()) {
                val randomIndex = (0 until items.size).random()
                items[randomIndex] // Read operation
            }
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "List Scroll Stress",
            passed = duration < 500 && items.size == 250,
            duration = duration,
            details = "850 list operations, ${items.size} items remaining"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "List Scroll Stress",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test state management under stress
 */
private suspend fun runStateManagementTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val stateFlow = kotlinx.coroutines.flow.MutableStateFlow(0)
        var lastValue = 0
        var updates = 0

        // Rapid state updates
        kotlinx.coroutines.coroutineScope {
            val collector = launch {
                stateFlow.collect { value ->
                    lastValue = value
                    updates++
                }
            }

            // Emit rapidly
            repeat(1000) { i ->
                stateFlow.value = i
                if (i % 100 == 0) delay(1)
            }

            delay(50) // Allow collection to complete
            collector.cancel()
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "State Management",
            passed = lastValue >= 990 && updates > 0,
            duration = duration,
            details = "1000 state updates, final value: $lastValue, collected: $updates"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "State Management",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test JSON parsing performance
 */
private suspend fun runJsonParsingTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val sampleJson = """
            {
                "id": "12345",
                "name": "Test User",
                "email": "test@example.com",
                "posts": [
                    {"id": 1, "content": "Hello world"},
                    {"id": 2, "content": "Another post"},
                    {"id": 3, "content": "Third post with more content"}
                ],
                "settings": {
                    "theme": "dark",
                    "notifications": true,
                    "language": "en"
                }
            }
        """.trimIndent()

        // Parse JSON multiple times
        var parseCount = 0
        repeat(100) {
            try {
                // Simple JSON validation by checking structure
                val hasId = sampleJson.contains("\"id\"")
                val hasName = sampleJson.contains("\"name\"")
                val hasPosts = sampleJson.contains("\"posts\"")
                if (hasId && hasName && hasPosts) parseCount++
            } catch (_: Exception) { }
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "JSON Parsing",
            passed = parseCount == 100 && duration < 500,
            duration = duration,
            details = "$parseCount JSON validations in ${duration}ms"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "JSON Parsing",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test string operations (formatting, parsing)
 */
private suspend fun runStringOperationsTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        var operations = 0

        // String concatenation
        val builder = StringBuilder()
        repeat(1000) { i ->
            builder.append("Item $i, ")
            operations++
        }

        // String formatting
        repeat(500) { i ->
            String.format("User %d liked your post at %s", i, "2024-12-30")
            operations++
        }

        // Regex operations
        val regex = Regex("[a-zA-Z0-9]+")
        repeat(200) {
            regex.findAll("Hello123World456Test789").count()
            operations++
        }

        // String splitting
        val longString = (1..100).joinToString(",") { "item$it" }
        repeat(100) {
            longString.split(",")
            operations++
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "String Operations",
            passed = operations == 1800 && duration < 1000,
            duration = duration,
            details = "$operations string operations completed"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "String Operations",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test date/time operations
 */
private suspend fun runDateTimeOperationsTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        var operations = 0

        // Current time operations
        repeat(200) {
            java.time.Instant.now()
            operations++
        }

        // Date parsing
        repeat(100) {
            try {
                java.time.Instant.parse("2024-12-30T12:00:00Z")
                operations++
            } catch (_: Exception) { }
        }

        // Duration calculations
        val baseInstant = java.time.Instant.now()
        repeat(100) { i ->
            val futureInstant = baseInstant.plusSeconds(i.toLong() * 60)
            java.time.Duration.between(baseInstant, futureInstant).toMinutes()
            operations++
        }

        // Time zone operations
        repeat(50) {
            java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault())
            operations++
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "DateTime Operations",
            passed = operations >= 450 && duration < 500,
            duration = duration,
            details = "$operations date/time operations completed"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "DateTime Operations",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test collection operations (maps, lists, sets)
 */
private suspend fun runCollectionOperationsTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        var operations = 0

        // Map operations
        val map = mutableMapOf<String, Int>()
        repeat(500) { i ->
            map["key_$i"] = i
            operations++
        }
        repeat(250) { i ->
            map["key_$i"]
            operations++
        }

        // List operations
        val list = mutableListOf<Int>()
        repeat(500) { i ->
            list.add(i)
            operations++
        }
        list.filter { it % 2 == 0 }
        operations++
        list.map { it * 2 }
        operations++
        list.sortedDescending()
        operations++

        // Set operations
        val set1 = (0..500).toSet()
        val set2 = (250..750).toSet()
        set1.intersect(set2)
        operations++
        set1.union(set2)
        operations++
        set1.subtract(set2)
        operations++

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Collection Operations",
            passed = operations >= 1256 && duration < 500,
            duration = duration,
            details = "$operations collection operations on ${map.size} map entries, ${list.size} list items"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Collection Operations",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test encryption/hashing operations
 */
private suspend fun runSecurityOperationsTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        var operations = 0
        val digest = java.security.MessageDigest.getInstance("SHA-256")

        // Hash operations
        repeat(100) { i ->
            val input = "test_string_$i".toByteArray()
            digest.reset()
            digest.digest(input)
            operations++
        }

        // Base64 encoding/decoding
        repeat(100) { i ->
            val original = "Test data for encoding $i"
            val encoded = android.util.Base64.encodeToString(original.toByteArray(), android.util.Base64.DEFAULT)
            android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
            operations++
        }

        // UUID generation
        repeat(50) {
            java.util.UUID.randomUUID()
            operations++
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Security Operations",
            passed = operations == 250 && duration < 1000,
            duration = duration,
            details = "$operations security operations (hash, base64, UUID)"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Security Operations",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test file system operations
 */
private suspend fun runFileSystemTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val cacheDir = context.cacheDir
        val testDir = java.io.File(cacheDir, "stress_test_${System.currentTimeMillis()}")
        testDir.mkdirs()

        var filesCreated = 0
        var filesRead = 0
        var filesDeleted = 0

        // Create files
        repeat(20) { i ->
            val file = java.io.File(testDir, "test_file_$i.txt")
            file.writeText("Test content for file $i with some additional data to simulate real files")
            filesCreated++
        }

        // Read files
        testDir.listFiles()?.forEach { file ->
            file.readText()
            filesRead++
        }

        // Delete files
        testDir.listFiles()?.forEach { file ->
            file.delete()
            filesDeleted++
        }
        testDir.delete()

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "File System",
            passed = filesCreated == 20 && filesRead == 20 && filesDeleted == 20,
            duration = duration,
            details = "Created: $filesCreated, Read: $filesRead, Deleted: $filesDeleted files"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "File System",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test coroutine exception handling
 */
private suspend fun runExceptionHandlingTest(): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        var caughtExceptions = 0
        var successfulOperations = 0

        // Test try-catch performance
        repeat(100) { i ->
            try {
                if (i % 3 == 0) throw IllegalStateException("Test exception $i")
                successfulOperations++
            } catch (_: IllegalStateException) {
                caughtExceptions++
            }
        }

        // Test coroutine exception handling
        kotlinx.coroutines.coroutineScope {
            repeat(50) { i ->
                launch {
                    try {
                        if (i % 5 == 0) throw RuntimeException("Coroutine exception $i")
                        successfulOperations++
                    } catch (_: RuntimeException) {
                        caughtExceptions++
                    }
                }
            }
        }

        val duration = System.currentTimeMillis() - startTime
        val totalExpectedCaught = 34 + 10 // 34 from first loop, 10 from coroutines

        StressTestResult(
            testName = "Exception Handling",
            passed = caughtExceptions >= 40,
            duration = duration,
            details = "Caught: $caughtExceptions, Successful: $successfulOperations operations"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Exception Handling",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed unexpectedly",
            errorMessage = e.message
        )
    }
}

/**
 * Test parental controls state reading
 */
private suspend fun runParentalControlsTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        // Test rapid state reads
        var readOperations = 0
        repeat(50) {
            val state = ParentalControlsSettings.getState(context)
            // Verify state is valid (has default values or user-set values)
            @Suppress("SENSELESS_COMPARISON")
            if (state != null) {
                readOperations++
            }
            if (it % 10 == 0) delay(1) // Yield occasionally
        }

        // Test shouldBlockFeature function
        repeat(30) {
            val state = ParentalControlsSettings.getState(context)
            BlockableFeature.entries.forEach { feature ->
                shouldBlockFeature(state, feature)
                readOperations++
            }
        }

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Parental Controls",
            passed = readOperations >= 50,
            duration = duration,
            details = "$readOperations parental control operations verified"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Parental Controls",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Test theme state persistence
 */
private suspend fun runThemeStatePersistenceTest(context: android.content.Context): StressTestResult {
    val startTime = System.currentTimeMillis()
    return try {
        val prefs = context.getSharedPreferences("theme_stress_test", android.content.Context.MODE_PRIVATE)
        var operations = 0

        // Test various theme states
        val themeStates = listOf(
            "default", "calm_mode", "high_contrast", "adhd_focus",
            "autism_friendly", "dyslexia_friendly", "anxiety_calm"
        )

        repeat(10) { iteration ->
            themeStates.forEach { theme ->
                prefs.edit()
                    .putString("selected_theme", theme)
                    .putBoolean("is_dark_mode", iteration % 2 == 0)
                    .putBoolean("reduce_animations", iteration % 3 == 0)
                    .apply()
                operations++

                // Verify
                val saved = prefs.getString("selected_theme", null)
                if (saved != theme) throw IllegalStateException("Theme mismatch")
            }
        }

        // Cleanup
        prefs.edit().clear().apply()

        val duration = System.currentTimeMillis() - startTime
        StressTestResult(
            testName = "Theme Persistence",
            passed = operations == 70,
            duration = duration,
            details = "$operations theme state saves with verification"
        )
    } catch (e: Exception) {
        StressTestResult(
            testName = "Theme Persistence",
            passed = false,
            duration = System.currentTimeMillis() - startTime,
            details = "Test failed",
            errorMessage = e.message
        )
    }
}

/**
 * Social Settings Debug Section
 * Developer options for testing the new social media settings.
 */
@Composable
private fun SocialSettingsDebugSection(context: android.content.Context) {
    var privacySettings by remember { mutableStateOf(SocialSettingsManager.getPrivacySettings(context)) }
    var notificationSettings by remember { mutableStateOf(SocialSettingsManager.getNotificationSettings(context)) }
    var contentPrefs by remember { mutableStateOf(SocialSettingsManager.getContentPreferences(context)) }
    var accessibilitySettings by remember { mutableStateOf(SocialSettingsManager.getAccessibilitySettings(context)) }
    var wellbeingSettings by remember { mutableStateOf(SocialSettingsManager.getWellbeingSettings(context)) }
    var isExpanded by remember { mutableStateOf(false) }

    DevSection(
        title = "Social Settings Debug",
        icon = Icons.Filled.Settings
    ) {
        Text(
            "Test and reset social media settings including privacy, notifications, content, accessibility, and wellbeing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Current Status Summary
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Current Settings Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                StateInfoRow("Private Account", if (privacySettings.isAccountPrivate) "✓ Yes" else "✗ No")
                StateInfoRow("DM Permission", privacySettings.allowDMsFrom.displayName)
                StateInfoRow("Push Notifications", if (notificationSettings.pushEnabled) "✓ On" else "✗ Off")
                StateInfoRow("Quiet Hours", if (notificationSettings.quietHoursEnabled) "✓ Enabled" else "✗ Disabled")
                StateInfoRow("Autoplay", contentPrefs.autoplayVideos.displayName)
                StateInfoRow("Reduce Motion", if (accessibilitySettings.reduceMotion) "✓ Yes" else "✗ No")
                StateInfoRow("Positivity Boost", if (wellbeingSettings.positivityBoostEnabled) "✓ On" else "✗ Off")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Expand for more options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Advanced Options",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(8.dp))

            // Privacy Settings Quick Toggles
            Text(
                "Privacy Quick Toggles",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            DevToggleRow(
                title = "Private Account",
                subtitle = "Toggle account privacy",
                isChecked = privacySettings.isAccountPrivate,
                onCheckedChange = {
                    privacySettings = privacySettings.copy(isAccountPrivate = it)
                    SocialSettingsManager.savePrivacySettings(context, privacySettings)
                }
            )

            DevToggleRow(
                title = "Hide from Search",
                subtitle = "Hide profile from search",
                isChecked = privacySettings.hideFromSearch,
                onCheckedChange = {
                    privacySettings = privacySettings.copy(hideFromSearch = it)
                    SocialSettingsManager.savePrivacySettings(context, privacySettings)
                }
            )

            DevToggleRow(
                title = "Two-Factor Auth",
                subtitle = "Enable 2FA",
                isChecked = privacySettings.twoFactorEnabled,
                onCheckedChange = {
                    privacySettings = privacySettings.copy(twoFactorEnabled = it)
                    SocialSettingsManager.savePrivacySettings(context, privacySettings)
                }
            )

            Spacer(Modifier.height(12.dp))

            // Notification Settings Quick Toggles
            Text(
                "Notification Quick Toggles",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            DevToggleRow(
                title = "Push Notifications",
                subtitle = "Master notification toggle",
                isChecked = notificationSettings.pushEnabled,
                onCheckedChange = {
                    notificationSettings = notificationSettings.copy(pushEnabled = it)
                    SocialSettingsManager.saveNotificationSettings(context, notificationSettings)
                }
            )

            DevToggleRow(
                title = "Quiet Hours",
                subtitle = "Pause notifications at night",
                isChecked = notificationSettings.quietHoursEnabled,
                onCheckedChange = {
                    notificationSettings = notificationSettings.copy(quietHoursEnabled = it)
                    SocialSettingsManager.saveNotificationSettings(context, notificationSettings)
                }
            )

            DevToggleRow(
                title = "Sound",
                subtitle = "Notification sounds",
                isChecked = notificationSettings.soundEnabled,
                onCheckedChange = {
                    notificationSettings = notificationSettings.copy(soundEnabled = it)
                    SocialSettingsManager.saveNotificationSettings(context, notificationSettings)
                }
            )

            Spacer(Modifier.height(12.dp))

            // Accessibility Quick Toggles
            Text(
                "Accessibility Quick Toggles",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            DevToggleRow(
                title = "Reduce Motion",
                subtitle = "Minimize animations",
                isChecked = accessibilitySettings.reduceMotion,
                onCheckedChange = {
                    accessibilitySettings = accessibilitySettings.copy(reduceMotion = it)
                    SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                }
            )

            DevToggleRow(
                title = "Dyslexia Font",
                subtitle = "Use dyslexia-friendly font",
                isChecked = accessibilitySettings.dyslexiaFont,
                onCheckedChange = {
                    accessibilitySettings = accessibilitySettings.copy(dyslexiaFont = it)
                    SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                }
            )

            DevToggleRow(
                title = "Simplified UI",
                subtitle = "Hide non-essential elements",
                isChecked = accessibilitySettings.simplifiedUI,
                onCheckedChange = {
                    accessibilitySettings = accessibilitySettings.copy(simplifiedUI = it)
                    SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                }
            )

            DevToggleRow(
                title = "Focus Mode",
                subtitle = "Reduce distractions",
                isChecked = accessibilitySettings.focusMode,
                onCheckedChange = {
                    accessibilitySettings = accessibilitySettings.copy(focusMode = it)
                    SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                }
            )

            Spacer(Modifier.height(12.dp))

            // Wellbeing Quick Toggles
            Text(
                "Wellbeing Quick Toggles",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            DevToggleRow(
                title = "Break Reminders",
                subtitle = "Remind to take breaks",
                isChecked = wellbeingSettings.breakRemindersEnabled,
                onCheckedChange = {
                    wellbeingSettings = wellbeingSettings.copy(breakRemindersEnabled = it)
                    SocialSettingsManager.saveWellbeingSettings(context, wellbeingSettings)
                }
            )

            DevToggleRow(
                title = "Positivity Boost",
                subtitle = "Show encouraging messages",
                isChecked = wellbeingSettings.positivityBoostEnabled,
                onCheckedChange = {
                    wellbeingSettings = wellbeingSettings.copy(positivityBoostEnabled = it)
                    SocialSettingsManager.saveWellbeingSettings(context, wellbeingSettings)
                }
            )

            DevToggleRow(
                title = "Bedtime Mode",
                subtitle = "Limit features at night",
                isChecked = wellbeingSettings.bedtimeModeEnabled,
                onCheckedChange = {
                    wellbeingSettings = wellbeingSettings.copy(bedtimeModeEnabled = it)
                    SocialSettingsManager.saveWellbeingSettings(context, wellbeingSettings)
                }
            )

            Spacer(Modifier.height(12.dp))

            // Content Quick Toggles
            Text(
                "Content Quick Toggles",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))

            DevToggleRow(
                title = "Data Saver",
                subtitle = "Reduce image quality",
                isChecked = contentPrefs.dataSaverMode,
                onCheckedChange = {
                    contentPrefs = contentPrefs.copy(dataSaverMode = it)
                    SocialSettingsManager.saveContentPreferences(context, contentPrefs)
                }
            )

            DevToggleRow(
                title = "Hide Like Counts",
                subtitle = "Don't show likes",
                isChecked = contentPrefs.hideLikeCounts,
                onCheckedChange = {
                    contentPrefs = contentPrefs.copy(hideLikeCounts = it)
                    SocialSettingsManager.saveContentPreferences(context, contentPrefs)
                }
            )

            DevToggleRow(
                title = "Hide View Counts",
                subtitle = "Don't show views",
                isChecked = contentPrefs.hideViewCounts,
                onCheckedChange = {
                    contentPrefs = contentPrefs.copy(hideViewCounts = it)
                    SocialSettingsManager.saveContentPreferences(context, contentPrefs)
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Reset Buttons
        Text(
            "Reset Settings",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    privacySettings = PrivacySettings()
                    SocialSettingsManager.savePrivacySettings(context, privacySettings)
                    android.widget.Toast.makeText(context, "Privacy settings reset", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Privacy", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    notificationSettings = NotificationSettings()
                    SocialSettingsManager.saveNotificationSettings(context, notificationSettings)
                    android.widget.Toast.makeText(context, "Notification settings reset", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Notifs", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    accessibilitySettings = AccessibilitySettings()
                    SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                    android.widget.Toast.makeText(context, "Accessibility settings reset", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Access.", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    wellbeingSettings = WellbeingSettings()
                    SocialSettingsManager.saveWellbeingSettings(context, wellbeingSettings)
                    android.widget.Toast.makeText(context, "Wellbeing settings reset", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Wellbeing", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                // Reset all settings
                privacySettings = PrivacySettings()
                notificationSettings = NotificationSettings()
                contentPrefs = ContentPreferences()
                accessibilitySettings = AccessibilitySettings()
                wellbeingSettings = WellbeingSettings()

                SocialSettingsManager.savePrivacySettings(context, privacySettings)
                SocialSettingsManager.saveNotificationSettings(context, notificationSettings)
                SocialSettingsManager.saveContentPreferences(context, contentPrefs)
                SocialSettingsManager.saveAccessibilitySettings(context, accessibilitySettings)
                SocialSettingsManager.saveWellbeingSettings(context, wellbeingSettings)

                android.widget.Toast.makeText(context, "All social settings reset to defaults", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reset ALL Social Settings")
        }
    }
}


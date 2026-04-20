@file:Suppress(
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "AssignmentToStateVariable"
)

package com.kyilmaz.neurocomet

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.kyilmaz.neurocomet.BuildConfig
import com.revenuecat.purchases.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.kyilmaz.neurocomet.calling.NeurodivergentPersona
import com.kyilmaz.neurocomet.calling.PracticeCallScreen
import com.kyilmaz.neurocomet.calling.PracticeCallSelectionScreen
import com.kyilmaz.neurocomet.calling.WebRTCCallManager

// --- 1. NAVIGATION & ROUTES ---
sealed class Screen(val route: String, val labelId: Int, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    data object Feed : Screen("feed", R.string.nav_feed, Icons.Filled.Home, Icons.Outlined.Home)
    data object Explore : Screen("explore", R.string.nav_explore, Icons.Filled.Search, Icons.Outlined.Search)
    data object Messages : Screen("messages", R.string.nav_messages, Icons.Filled.Mail, Icons.Outlined.Mail)
    data object Notifications : Screen("notifications", R.string.nav_notifications, Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    data object ThemeSettings : Screen("theme_settings", R.string.settings_theme, Icons.Filled.Palette, Icons.Outlined.Palette)
    data object AnimationSettings : Screen("animation_settings", R.string.settings_animation, Icons.Filled.Animation, Icons.Outlined.Animation)
    data object IconCustomization : Screen("icon_customization", R.string.settings_app_icon, Icons.Filled.AppShortcut, Icons.Filled.AppShortcut)
    data object PrivacySettings : Screen("privacy_settings", R.string.settings_privacy, Icons.Filled.Lock, Icons.Outlined.Lock)
    data object NotificationSettings : Screen("notification_settings", R.string.settings_notif_prefs, Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object ContentSettings : Screen("content_settings", R.string.settings_content_filters, Icons.Filled.FilterList, Icons.Filled.FilterList)
    data object AccessibilitySettingsScreen : Screen("accessibility_settings", R.string.settings_reduce_motion, Icons.Filled.Accessibility, Icons.Outlined.Accessibility)
    data object WellbeingSettings : Screen("wellbeing_settings", R.string.settings_break_reminders, Icons.Filled.Bedtime, Icons.Filled.Bedtime)
    data object FontSettings : Screen("font_settings", R.string.settings_text_display, Icons.Filled.FormatSize, Icons.Filled.FormatSize)
    data object ParentalControls : Screen("parental_controls", R.string.settings_parental, Icons.Filled.Shield, Icons.Outlined.Shield)
    data object Conversation : Screen("conversation/{conversationId}", R.string.nav_messages, Icons.Filled.Mail, Icons.Outlined.Mail) {
        fun route(conversationId: String) = "conversation/$conversationId"
    }

    data object DevOptions : Screen("dev_options", R.string.settings_developer_options_group, Icons.Filled.Build, Icons.Outlined.Build)
    data object TopicDetail : Screen("topic/{topicId}", R.string.nav_explore, Icons.Filled.Search, Icons.Outlined.Search) {
        fun route(topicId: String) = "topic/$topicId"
    }
    data object Subscription : Screen("subscription", R.string.settings_go_premium, Icons.Filled.Star, Icons.Outlined.Star)
    data object CallHistory : Screen("call_history", R.string.nav_messages, Icons.Filled.Phone, Icons.Outlined.Phone)
    data object PracticeCallSelection : Screen("practice_call_selection", R.string.nav_messages, Icons.Filled.Headset, Icons.Outlined.Headset)
    data object PracticeCall : Screen("practice_call/{personaId}", R.string.nav_messages, Icons.Filled.Phone, Icons.Outlined.Phone) {
        fun route(personaId: String) = "practice_call/$personaId"
    }
    data object Profile : Screen("profile/{userId}", R.string.nav_settings, Icons.Filled.Person, Icons.Outlined.Person) {
        fun route(userId: String) = "profile/$userId"
    }
    data object PostDetail : Screen("post/{postId}", R.string.nav_feed, Icons.Filled.Home, Icons.Outlined.Home)
    data object MyProfile : Screen("my_profile", R.string.settings_my_profile, Icons.Filled.Person, Icons.Outlined.Person)
    data object GamesHub : Screen("games_hub", R.string.games_hub_title, Icons.Filled.Games, Icons.Outlined.Games)
    data object GamePlay : Screen("game/{gameId}", R.string.games_hub_title, Icons.Filled.Games, Icons.Outlined.Games) {
        fun route(gameId: String) = "game/$gameId"
    }
    data object FeedbackHub : Screen("feedback_hub/{action}", R.string.feedback_hub_title, Icons.Filled.Feedback, Icons.Outlined.Feedback) {
        fun route(action: String = "none") = "feedback_hub/$action"
    }
    data object BackupSettings : Screen("backup_settings", R.string.backup_title, Icons.Filled.CloudUpload, Icons.Filled.CloudUpload)
}

open class MainActivity : AppCompatActivity() {

    /**
     * Stream of inbound deep-link intents. Updated on launch and on each
     * `onNewIntent` so the Compose layer can forward them to NavController.
     * Cleared to `null` after consumption.
     */
    internal val deepLinkIntent: kotlinx.coroutines.flow.MutableStateFlow<android.content.Intent?> =
        kotlinx.coroutines.flow.MutableStateFlow(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkIntent.value = intent
    }

    private fun configureOrientationForDevice() {
        val displayMetrics = resources.displayMetrics
        val widthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val heightDp = (displayMetrics.heightPixels / displayMetrics.density).toInt()
        val canonicalLayout = canonicalLayoutForDp(widthDp = widthDp, heightDp = heightDp)

        requestedOrientation = if (canonicalLayout.deviceFamily == CanonicalDeviceFamily.PHONE) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Seed deep-link flow with the launch intent (if any).
        intent?.let { if (it.data != null) deepLinkIntent.value = it }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Android 17+: Enable cross-device handoff by default.
        // Per-route toggling is handled in NeuroCometApp via HandoffManager.
        HandoffManager.setHandoffEnabled(this, true)
        
        // Register Android 17 ProfilingManager triggers
        ProfilingManagerHelper.registerTracerTriggers(this)

        PerformanceOverlayState.init(this)
        SettingsManager.init(this)
        CredentialStorage.initialize(this)
        RegulationLiveSessionManager.initialize(this)

        try {
            SecurityManager.performSecurityCheck(this)
            if (!BuildConfig.DEBUG) {
                // RELAXED SECURITY FOR BETA TESTING:
                // We'll only enforce critical integrity and rooting checks.
                // We allow emulators and don't explicitly call enforceSecurity(allowEmulator=false)
                // which might be too aggressive for different beta tester devices.
                val result = SecurityManager.performSecurityCheck(this)
                if (result.isRooted || result.isAppTampered) {
                    throw SecurityException("Security check failed")
                }
            }
        } catch (_: SecurityException) {
            finish()
            return
        }

        configureOrientationForDevice()

        if (!NotificationChannels.hasNotificationPermission(this)) {
            NotificationChannels.requestNotificationPermission(this)
        }

        // RevenueCat: Automagically configure if a valid API key is present.
        val revenueCatKey = SecurityUtils.decrypt(BuildConfig.REVENUECAT_API_KEY)
        val billingConfigured = if (revenueCatKey.isNotBlank() && !revenueCatKey.startsWith("goog_your_")) {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(PurchasesConfiguration.Builder(this, revenueCatKey).build())
            
            // Initialize the manager
            SubscriptionManager.initialize(debug = true)
            Log.d("MainActivity", "💰 RevenueCat configured with real key. Real-time billing active.")
            true
        } else {
            // Initialize the manager
            SubscriptionManager.initialize(debug = false)
            if (!BuildConfig.DEBUG) {
                Log.e("MainActivity", "🚨 RevenueCat key missing in release build; billing disabled!")
            } else {
                Log.d("MainActivity", "🧪 TEST MODE: RevenueCat SDK skipped — purchases will be simulated.")
            }
            false
        }

        SubscriptionManager.setBillingConfigured(
            isConfigured = billingConfigured,
            errorMessage = if (!BuildConfig.DEBUG && !billingConfigured) {
                "Purchases are temporarily unavailable in this build."
            } else null
        )

        setContent {
            val feedViewModel: FeedViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val themeViewModel: ThemeViewModel = viewModel()
            val safetyViewModel: SafetyViewModel = viewModel()

            val context = LocalContext.current
            LaunchedEffect(Unit) {
                authViewModel.initialize(context)
            }

            val authState by authViewModel.user.collectAsState()
            val authError by authViewModel.error.collectAsState()
            val is2FARequired by authViewModel.is2FARequired.collectAsState()

            // Initialize WebRTC call manager for real voice/video calls
            val webRtcUserId = authState?.id
            LaunchedEffect(webRtcUserId) {
                if (webRtcUserId != null) {
                    WebRTCCallManager.getInstance().initialize(
                        context = context,
                        supabase = AppSupabaseClient.client,
                        userId = webRtcUserId
                    )
                }
            }

            val themeState by themeViewModel.themeState.collectAsState()
            
            // Determine effective dark theme mode
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeState.themeMode) {
                com.kyilmaz.neurocomet.ui.theme.ThemeMode.DARK -> true
                com.kyilmaz.neurocomet.ui.theme.ThemeMode.LIGHT -> false
                else -> isSystemDark
            }
            
            // Sync themeViewModel.setDarkMode if SYSTEM mode changes the actual dark/light status
            LaunchedEffect(isSystemDark, themeState.themeMode) {
                if (themeState.themeMode == com.kyilmaz.neurocomet.ui.theme.ThemeMode.SYSTEM) {
                    themeViewModel.setDarkMode(isSystemDark)
                }
            }
            
            SideEffect {
                val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
                // AppearanceLightStatusBars: true means dark text (light background)
                windowInsetsController.isAppearanceLightStatusBars = !isDark
                windowInsetsController.isAppearanceLightNavigationBars = !isDark
            }

            LaunchedEffect(Unit) {
                val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                val languageTag = if (!currentLocales.isEmpty) {
                    currentLocales.get(0)?.toLanguageTag() ?: ""
                } else {
                    ""
                }
                themeViewModel.setLanguageCode(languageTag)

                if (!BuildConfig.DEBUG && billingConfigured) {
                    Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            val isPremium = customerInfo.entitlements["premium"]?.isActive == true
                            feedViewModel.setPremiumStatus(isPremium)
                        }
                        override fun onError(error: PurchasesError) {
                            feedViewModel.setPremiumStatus(false)
                        }
                    })
                } else {
                    // In debug/test mode, or when billing is unavailable, use the safe fallback path.
                    SubscriptionManager.checkPremiumStatus { isPremium ->
                        feedViewModel.setPremiumStatus(isPremium)
                    }
                }
            }

            var showSplash by remember { mutableStateOf(true) }
            var showStaySignedIn by remember { mutableStateOf(false) }
            var staySignedInHandled by remember { mutableStateOf(false) }

            LaunchedEffect(authState, staySignedInHandled) {
                if (authState != null && !staySignedInHandled) {
                    if (StaySignedInSettings.shouldShowPrompt(this@MainActivity)) {
                        showStaySignedIn = true
                    } else {
                        staySignedInHandled = true
                    }
                }
            }

            NeuroThemeApplication(themeViewModel = themeViewModel) {
                ProvideCanonicalLayout {
                    if (showSplash) {
                        NeuroSplashScreen(
                            onFinished = { showSplash = false },
                            neuroState = themeState.selectedState,
                            animationSettings = themeState.animationSettings
                        )
                    } else if (showStaySignedIn && authState != null) {
                        StaySignedInScreen(
                            userEmail = authState?.id ?: "",
                            userDisplayName = authState?.name,
                            onYes = { dontShowAgain ->
                                StaySignedInSettings.savePreference(
                                    context = this@MainActivity,
                                    staySignedIn = true,
                                    dontShowAgain = dontShowAgain
                                )
                                showStaySignedIn = false
                                staySignedInHandled = true
                            },
                            onNo = { dontShowAgain ->
                                StaySignedInSettings.savePreference(
                                    context = this@MainActivity,
                                    staySignedIn = false,
                                    dontShowAgain = dontShowAgain
                                )
                                showStaySignedIn = false
                                staySignedInHandled = true
                            }
                        )
                    } else {
                        NeuroCometApp(
                            feedViewModel = feedViewModel,
                            authViewModel = authViewModel,
                            themeViewModel = themeViewModel,
                            safetyViewModel = safetyViewModel,
                            authError = authError,
                            is2FARequired = is2FARequired,
                            authState = authState
                        )
                    }
                }
            }
        }
    }

    /**
     * Android 17+ (API 37): Provide data for cross-device handoff.
     * Uses reflection to avoid compile-time dependency on preview SDK.
     */
    @Suppress("NewApi", "unused")
    fun onHandoffActivityRequested(): Any? {
        return if (Build.VERSION.SDK_INT >= 37) {
            try {
                val builderClass = Class.forName("android.app.HandoffActivityData\$Builder")
                val constructor = builderClass.getConstructor(ComponentName::class.java)
                val builder = constructor.newInstance(ComponentName(this, MainActivity::class.java))
                builderClass.getMethod("build").invoke(builder)
            } catch (e: Exception) {
                Log.w("MainActivity", "Failed to build HandoffActivityData", e)
                null
            }
        } else {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroCometApp(
    feedViewModel: FeedViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    safetyViewModel: SafetyViewModel,
    authError: String?,
    is2FARequired: Boolean,
    authState: User?
) {
    val navController = rememberNavController()
    val feedState by feedViewModel.uiState.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()
    val canonicalLayout = LocalCanonicalLayout.current

    // Route inbound Android App Link intents (https://getneurocomet.com/post/... or /u/...)
    // into the NavController so the correct destination is opened.
    val context = LocalContext.current
    val deepLinkActivity = remember(context) {
        var c = context
        while (c is android.content.ContextWrapper) {
            if (c is MainActivity) break
            c = c.baseContext
        }
        c as? MainActivity
    }
    val pendingDeepLink by (
        deepLinkActivity?.deepLinkIntent
            ?: kotlinx.coroutines.flow.MutableStateFlow<android.content.Intent?>(null)
    ).collectAsState()
    LaunchedEffect(pendingDeepLink) {
        val link = pendingDeepLink ?: return@LaunchedEffect
        val uri = link.data
        if (uri != null && uri.scheme == "https" &&
            (uri.host == "getneurocomet.com" || uri.host == "www.getneurocomet.com")
        ) {
            navController.handleDeepLink(link)
        }
        deepLinkActivity?.deepLinkIntent?.value = null
    }

    val isGuestUser = authState?.id == "guest_user"
    val authedUser = authState

    var showPremiumDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isPermanentDrawerCollapsed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(feedState.errorMessage) {
        val msg = feedState.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
            feedViewModel.clearError()
        }
    }

    val app = remember(context) { context.applicationContext as android.app.Application }
    val devOptionsViewModel: DevOptionsViewModel = viewModel()
    val devOptions by devOptionsViewModel.options.collectAsState()
    val messagesViewModel: MessagesViewModel = viewModel()
    val messagesState by messagesViewModel.state.collectAsState()
    val abExperiments by ABTestManager.experiments.collectAsState()
    val pendingAccountAction by authViewModel.pendingAccountAction.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        devOptionsViewModel.refresh(app)
        safetyViewModel.refresh(app)
        messagesViewModel.initialize()
    }

    // Keep mock conversations in sync when in mock mode
    LaunchedEffect(feedState.conversations) {
        messagesViewModel.setMockConversations(feedState.conversations)
    }

    // Sync social settings → consuming components on startup and when returning from settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            // Accessibility → ThemeViewModel
            val a11y = SocialSettingsManager.getAccessibilitySettings(context)
            themeViewModel.setDisableAllAnimations(a11y.reduceMotion)
            RegulationLiveSessionManager.refreshAccessibilityMode(context)
            if (a11y.dyslexiaFont) {
                themeViewModel.setSelectedFont(AccessibilityFont.OPEN_DYSLEXIC)
            }
            if (a11y.largerText) {
                themeViewModel.setTextScaleFactor(1.3f)
            }

            // Content preferences → FeedViewModel
            val content = SocialSettingsManager.getContentPreferences(context)
            feedViewModel.applyContentPreferences(
                hideLikeCounts = content.hideLikeCounts,
                hideViewCounts = content.hideViewCounts,
                dataSaverMode = content.dataSaverMode
            )
        }
    }

    // Sync dev option flags to their consuming components
    LaunchedEffect(devOptions) {
        // Performance overlay
        PerformanceOverlayState.isEnabled = devOptions.showPerformanceOverlay
        // Propagate feature flags to FeedViewModel
        feedViewModel.setFeatureFlags(
            enableNewFeedLayout = devOptions.enableNewFeedLayout,
            enableAdvancedSearch = devOptions.enableAdvancedSearch,
            enableAiSuggestions = devOptions.enableAiSuggestions
        )
        // Re-fetch posts when simulation flags change so they take effect immediately
        feedViewModel.fetchPosts()
    }

    val compactFeedExperimentEnabled =
        (abExperiments[ABExperiment.COMPACT_FEED_CARDS]?.activeVariant
            ?: ABTestManager.getVariant(app, ABExperiment.COMPACT_FEED_CARDS)) != "control"
    val settingsSearchEnabled =
        (abExperiments[ABExperiment.SETTINGS_SEARCH]?.activeVariant
            ?: ABTestManager.getVariant(app, ABExperiment.SETTINGS_SEARCH)) != "control"
    val notificationGroupingEnabled =
        (abExperiments[ABExperiment.NOTIFICATION_GROUPING]?.activeVariant
            ?: ABTestManager.getVariant(app, ABExperiment.NOTIFICATION_GROUPING)) != "control"
    val dmTypingIndicatorVariant =
        abExperiments[ABExperiment.DM_TYPING_INDICATOR]?.activeVariant
            ?: ABTestManager.getVariant(app, ABExperiment.DM_TYPING_INDICATOR)

    // Wellbeing: Break reminders via snackbar
    LaunchedEffect(Unit) {
        while (true) {
            val wellbeing = SocialSettingsManager.getWellbeingSettings(context)
            if (wellbeing.breakRemindersEnabled && wellbeing.breakIntervalMinutes > 0) {
                kotlinx.coroutines.delay(wellbeing.breakIntervalMinutes.minutes)
                val messages = listOf(
                    "🧘 Time for a mindful break! Stretch, breathe, or look away from the screen for a moment.",
                    "💙 Hey, you've been scrolling for a while. How about a quick break?",
                    "🌿 Gentle reminder: Take a breath. You deserve a moment of calm.",
                    "☕ Break time! Grab some water or a snack.",
                    "✨ Your brain deserves a rest. Step away for a few minutes!"
                )
                snackbarHostState.showSnackbar(
                    message = messages.random(),
                    duration = SnackbarDuration.Long
                )
            } else {
                kotlinx.coroutines.delay(60_000L) // Re-check every minute
            }
        }
    }

    // Age-gate: show DOB verification for returning users who never completed it
    var showSignInAgeGate by remember { mutableStateOf(false) }

    // Detect when a user signs in but has no persisted audience
    LaunchedEffect(authState) {
        if (authState != null && AudiencePrefs.needsVerification(context)) {
            showSignInAgeGate = true
        }
    }

    if (authState == null) {
        AuthScreen(
            onSignIn = { email, password -> authViewModel.signIn(email, password) },
            onSignUp = { email, password, audience ->
                authViewModel.signUp(email, password, audience)
                audience?.let { safetyViewModel.setAudienceDirect(it, context) }
            },
            onVerify2FA = { code -> authViewModel.verify2FA(code) },
            is2FARequired = is2FARequired,
            error = authError,
            animationSettings = themeState.animationSettings,
            // Only allow skipping auth in debug builds to prevent unauthorized access
            onSkip = if (BuildConfig.DEBUG) {{ authViewModel.skipAuth() }} else null
        )
    } else if (showSignInAgeGate) {
        // Mandatory age verification for returning users who lack a persisted audience
        AgeVerificationDialog(
            onDismiss = { /* non-dismissable — user must verify */ },
            onConfirm = { audience ->
                safetyViewModel.setAudienceDirect(audience, context)
                showSignInAgeGate = false
            },
            onSkip = null,          // no skip for returning users
            title = stringResource(R.string.age_verify_title_returning),
            subtitle = stringResource(R.string.age_verify_subtitle_returning)
        )
    } else {
        when (val action = pendingAccountAction) {
            is PendingAccountAction.ScheduledDeletion -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(R.string.account_deletion_scheduled_title)) },
                    text = {
                        Text(stringResource(R.string.account_deletion_scheduled_desc))
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            authViewModel.cancelScheduledDeletion { _, message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }) {
                            Text(stringResource(R.string.account_cancel_deletion))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { authViewModel.keepScheduledDeletion() }) {
                            Text(stringResource(R.string.account_keep_scheduled))
                        }
                    }
                )
            }
            is PendingAccountAction.DetoxActive -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(R.string.account_detox_active_title)) },
                    text = {
                        Text(
                            stringResource(R.string.account_detox_active_desc, action.until ?: stringResource(R.string.account_detox_active_desc_default))
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            authViewModel.endDetoxMode { _, message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }) {
                            Text(stringResource(R.string.account_end_detox))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { authViewModel.continueDetoxBreak() }) {
                            Text(stringResource(R.string.account_stay_on_break))
                        }
                    }
                )
            }
            null -> Unit
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val currentDestination = navBackStackEntry?.destination

        // Android 17+: Toggle handoff per-route based on content sensitivity.
        // Respects the dev option toggle (enableHandoff).
        val activity = context as? android.app.Activity
        LaunchedEffect(currentRoute, devOptions.enableHandoff) {
            if (activity != null) {
                val enabled = HandoffManager.shouldEnableHandoff(
                    route = currentRoute,
                    userOptIn = true,
                    devOverride = if (!devOptions.enableHandoff) false else null
                )
                HandoffManager.setHandoffEnabled(activity, enabled)
            }
        }

        val topLevelScreens = remember {
            listOf(Screen.Feed, Screen.Explore, Screen.Messages, Screen.Notifications, Screen.Settings)
        }
        val topLevelRoutes = remember(topLevelScreens) { topLevelScreens.map { it.route }.toSet() }
        val selectedTopLevelRoute = when {
            currentRoute == null -> null
            currentRoute == Screen.Conversation.route || currentRoute.startsWith("conversation/") ||
                currentRoute == Screen.CallHistory.route ||
                currentRoute == Screen.PracticeCallSelection.route ||
                currentRoute == Screen.PracticeCall.route ||
                currentRoute.startsWith("practice_call/") -> Screen.Messages.route

            currentRoute == Screen.TopicDetail.route || currentRoute.startsWith("topic/") -> Screen.Explore.route

            currentRoute == Screen.ThemeSettings.route ||
                currentRoute == Screen.AnimationSettings.route ||
                currentRoute == Screen.IconCustomization.route ||
                currentRoute == Screen.PrivacySettings.route ||
                currentRoute == Screen.NotificationSettings.route ||
                currentRoute == Screen.ContentSettings.route ||
                currentRoute == Screen.AccessibilitySettingsScreen.route ||
                currentRoute == Screen.WellbeingSettings.route ||
                currentRoute == Screen.FontSettings.route ||
                currentRoute == Screen.ParentalControls.route ||
                currentRoute == Screen.Subscription.route ||
                currentRoute == Screen.BackupSettings.route ||
                currentRoute == Screen.MyProfile.route ||
                currentRoute == Screen.GamesHub.route ||
                currentRoute == Screen.FeedbackHub.route ||
                currentRoute.startsWith("feedback_hub/") ||
                currentRoute == "feedback_bug_report" ||
                currentRoute == "feedback_feature_request" ||
                currentRoute == "feedback_general" ||
                currentRoute == Screen.GamePlay.route ||
                currentRoute.startsWith("game/") ||
                currentRoute == Screen.Profile.route ||
                currentRoute.startsWith("profile/") -> Screen.Settings.route

            else -> currentRoute
        }
        val showPrimaryNavigation = when (canonicalLayout.navigationChrome) {
            CanonicalNavigationChrome.BOTTOM_BAR -> currentRoute in topLevelRoutes
            CanonicalNavigationChrome.NAVIGATION_RAIL,
            CanonicalNavigationChrome.PERMANENT_DRAWER -> true
        }
        val adaptiveNavItems = remember(messagesState.conversations) {
            listOf(
                AdaptiveNavItem(Screen.Feed.route, Screen.Feed.labelId, Screen.Feed.iconFilled, Screen.Feed.iconOutlined),
                AdaptiveNavItem(Screen.Explore.route, Screen.Explore.labelId, Screen.Feed.iconFilled, Screen.Explore.iconOutlined),
                AdaptiveNavItem(
                    route = Screen.Messages.route,
                    labelRes = Screen.Messages.labelId,
                    iconFilled = Screen.Messages.iconFilled,
                    iconOutlined = Screen.Messages.iconOutlined,
                    badgeCount = messagesState.conversations.sumOf { it.unreadCount }
                ),
                AdaptiveNavItem(Screen.Notifications.route, Screen.Notifications.labelId, Screen.Notifications.iconFilled, Screen.Notifications.iconOutlined),
                AdaptiveNavItem(Screen.Settings.route, Screen.Settings.labelId, Screen.Settings.iconFilled, Screen.Settings.iconOutlined, section = NavigationSection.SETTINGS)
            )
        }
        val showMessagesSplitPane = canonicalLayout.supportsMultiPane &&
            (currentRoute == Screen.Messages.route || currentRoute == Screen.Conversation.route)
        val showExploreSplitPane = canonicalLayout.supportsMultiPane &&
            (currentRoute == Screen.Explore.route || currentRoute == Screen.TopicDetail.route)
        // Keep settings on the exact same navigation flow and row presentation as the
        // phone layout until a dedicated large-screen settings experience is designed.
        val showSettingsSplitPane = false
        val selectedConversationId = navBackStackEntry?.arguments?.getString("conversationId")
        val selectedTopicId = navBackStackEntry?.arguments?.getString("topicId")
        val selectedConversation = remember(
            messagesState.conversations,
            messagesState.activeConversation,
            selectedConversationId
        ) {
            selectedConversationId?.let { conversationId ->
                messagesState.conversations.find { it.id == conversationId }
                    ?: messagesState.activeConversation?.takeIf { it.id == conversationId }
            }
        }

        fun openConversationRoute(conversationId: String) {
            messagesViewModel.openConversation(conversationId)
            if (selectedConversationId != conversationId || currentRoute != Screen.Conversation.route) {
                navController.navigate(Screen.Conversation.route(conversationId))
            }
        }

        fun openTopicRoute(topicId: String) {
            if (selectedTopicId != topicId || currentRoute != Screen.TopicDetail.route) {
                navController.navigate(Screen.TopicDetail.route(topicId))
            }
        }

        fun launchAuthEntry() {
            authViewModel.clearError()
            authViewModel.signOut()
        }

        val settingsOverviewContent: @Composable () -> Unit = {
            val settingsContext = LocalContext.current
            SettingsScreen(
                authViewModel = authViewModel,
                onLogout = {
                    if (!StaySignedInSettings.isStaySignedIn(settingsContext)) {
                        StaySignedInSettings.clearAll(settingsContext)
                    }
                    authViewModel.signOut()
                    navController.popBackStack(Screen.Feed.route, true)
                },
                onRequireAuth = ::launchAuthEntry,
                safetyViewModel = safetyViewModel,
                devOptionsViewModel = devOptionsViewModel,
                canShowDevOptions = devOptions.devMenuEnabled,
                onOpenDevOptions = {
                    navController.navigate(Screen.DevOptions.route)
                },
                onOpenParentalControls = {
                    navController.navigate(Screen.ParentalControls.route)
                },
                onOpenThemeSettings = {
                    navController.navigate(Screen.ThemeSettings.route)
                },
                onOpenAnimationSettings = {
                    navController.navigate(Screen.AnimationSettings.route)
                },
                onOpenIconCustomization = {
                    navController.navigate(Screen.IconCustomization.route)
                },
                onOpenPrivacySettings = {
                    navController.navigate(Screen.PrivacySettings.route)
                },
                onOpenNotificationSettings = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onOpenContentSettings = {
                    navController.navigate(Screen.ContentSettings.route)
                },
                onOpenAccessibilitySettings = {
                    navController.navigate(Screen.AccessibilitySettingsScreen.route)
                },
                onOpenWellbeingSettings = {
                    navController.navigate(Screen.WellbeingSettings.route)
                },
                onOpenFontSettings = {
                    navController.navigate(Screen.FontSettings.route)
                },
                onOpenSubscription = {
                    navController.navigate(Screen.Subscription.route)
                },
                onOpenMyProfile = {
                    navController.navigate(Screen.MyProfile.route)
                },
                onOpenGames = {
                    navController.navigate(Screen.GamesHub.route)
                },
                onOpenBugReport = {
                    navController.navigate(Screen.FeedbackHub.route())
                },
                onOpenFeatureRequest = {
                    navController.navigate(Screen.FeedbackHub.route())
                },
                onOpenGeneralFeedback = {
                    navController.navigate(Screen.FeedbackHub.route())
                },
                onOpenBackupSettings = {
                    navController.navigate(Screen.BackupSettings.route)
                },
                isPremium = feedState.isPremium,
                isFakePremiumEnabled = feedState.isFakePremiumEnabled,
                onFakePremiumToggle = { enabled ->
                    feedViewModel.toggleFakePremium(enabled)
                },
                themeViewModel = themeViewModel,
                showSearchBar = settingsSearchEnabled
            )
        }

        val settingsDetailContent: @Composable (String) -> Unit = { detailRoute ->
            when (detailRoute) {
                Screen.Settings.route -> CanonicalEmptyDetailPane(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.nav_settings),
                    subtitle = "Choose a settings category to open its full controls here."
                )

                Screen.ThemeSettings.route -> ThemeSettingsScreen(
                    themeViewModel = themeViewModel,
                    isPremium = feedState.isPremium,
                    isFakePremiumEnabled = feedState.isFakePremiumEnabled,
                    onBack = { navController.popBackStack() }
                )

                Screen.AnimationSettings.route -> AnimationSettingsScreen(
                    onBack = { navController.popBackStack() },
                    themeViewModel = themeViewModel
                )

                Screen.IconCustomization.route -> IconCustomizationScreen(
                    onBack = { navController.popBackStack() }
                )

                Screen.PrivacySettings.route -> PrivacySettingsScreen(
                    onBack = { navController.popBackStack() },
                    authViewModel = authViewModel
                )

                Screen.NotificationSettings.route -> NotificationSettingsScreen(
                    onBack = { navController.popBackStack() }
                )

                Screen.ContentSettings.route -> ContentPreferencesScreen(
                    onBack = { navController.popBackStack() }
                )

                Screen.AccessibilitySettingsScreen.route -> AccessibilitySettingsScreen(
                    onBack = { navController.popBackStack() }
                )

                Screen.WellbeingSettings.route -> WellbeingSettingsScreen(
                    onBack = { navController.popBackStack() },
                    authViewModel = authViewModel
                )

                Screen.FontSettings.route -> FontSettingsScreen(
                    themeViewModel = themeViewModel,
                    onBack = { navController.popBackStack() }
                )

                Screen.ParentalControls.route -> ParentalControlsScreen(
                    onBack = { navController.popBackStack() }
                )

                Screen.BackupSettings.route -> BackupSettingsScreen(
                    onBack = { navController.popBackStack() }
                )

                else -> CanonicalEmptyDetailPane(
                    icon = Icons.Default.SettingsApplications,
                    title = stringResource(R.string.nav_settings),
                    subtitle = "This section stays full-screen on the current route."
                )
            }
        }

        val settingsSupportingContent: (@Composable () -> Unit)? = if (canonicalLayout.paneLayout == CanonicalPaneLayout.TRIPLE) {
            {
                SettingsSupportPane(
                    user = authedUser,
                    isPremium = feedState.isPremium,
                    themeState = themeState
                )
            }
        } else {
            null
        }

        val messagesSplitPaneContent: @Composable (Conversation?) -> Unit = { activeConversation ->
            CanonicalAdaptivePaneLayout(
                primary = {
                    NeuroInboxScreen(
                        conversations = messagesState.conversations,
                        safetyState = safetyState,
                        onOpenConversation = ::openConversationRoute,
                        onStartNewChat = { userId ->
                            val conversationId = messagesViewModel.startOrOpenConversation(userId)
                            openConversationRoute(conversationId)
                        },
                        onOpenCallHistory = {
                            navController.navigate(Screen.CallHistory.route)
                        },
                        onStartCall = { userId, displayName, avatarUrl, isVideo ->
                            if (feedState.isMockInterfaceEnabled) {
                                MockCallManager.startCall(
                                    recipientId = userId,
                                    recipientName = displayName,
                                    recipientAvatar = avatarUrl,
                                    callType = if (isVideo) CallType.VIDEO else CallType.VOICE
                                )
                            } else {
                                WebRTCCallManager.getInstance().startCall(
                                    recipientId = userId,
                                    recipientName = displayName,
                                    recipientAvatar = avatarUrl,
                                    callType = if (isVideo) com.kyilmaz.neurocomet.calling.CallType.VIDEO else com.kyilmaz.neurocomet.calling.CallType.VOICE
                                )
                            }
                        }
                    )
                },
                secondary = {
                    if (activeConversation == null) {
                        CanonicalEmptyDetailPane(
                            icon = Icons.Default.MarkunreadMailbox,
                            title = stringResource(R.string.nav_messages),
                            subtitle = "Select a conversation to keep your inbox list visible while you reply."
                        )
                    } else {
                        NeuroConversationScreen(
                            conversation = activeConversation,
                            onBack = {
                                navController.popBackStack()
                                messagesViewModel.dismissConversation()
                            },
                            onSend = { recipientId, content ->
                                messagesViewModel.sendDirectMessage(recipientId, content)
                            },
                            onReport = { messageId ->
                                messagesViewModel.reportMessage(messageId)
                            },
                            onRetryMessage = { convId, msgId ->
                                messagesViewModel.retryDirectMessage(convId, msgId)
                            },
                            onReactToMessage = { messageId, emoji ->
                                messagesViewModel.reactToMessage(activeConversation.id, messageId, emoji)
                            },
                            isUserBlocked = (activeConversation.participants.firstOrNull { it != (authState.id) }?.let { messagesViewModel.isUserBlocked(it) } ?: false),
                            isUserMuted = (activeConversation.participants.firstOrNull { it != (authState.id) }?.let { messagesViewModel.isUserMuted(it) } ?: false),
                            enableVideoChat = devOptions.enableVideoChat,
                            typingIndicatorVariant = dmTypingIndicatorVariant,
                            enableSimulatedReplies = BuildConfig.DEBUG && messagesState.currentUserId == "me",
                            onSimulatedReply = { conversationId, senderId, content ->
                                messagesViewModel.receiveMockReply(conversationId, senderId, content)
                            }
                        )
                    }
                },
                tertiary = if (canonicalLayout.paneLayout == CanonicalPaneLayout.TRIPLE) {
                    {
                        MessagesSupportPane(
                            conversations = messagesState.conversations,
                            activeConversation = activeConversation,
                            onOpenCallHistory = {
                                navController.navigate(Screen.CallHistory.route)
                            },
                            onOpenPracticeCall = {
                                navController.navigate(Screen.PracticeCallSelection.route)
                            }
                        )
                    }
                } else {
                    null
                }
            )
        }

        val exploreSplitPaneContent: @Composable (String?) -> Unit = { topicId ->
            CanonicalAdaptivePaneLayout(
                primary = {
                    ExploreScreen(
                        posts = feedState.posts,
                        safetyState = safetyState,
                        onLikePost = { postId -> feedViewModel.toggleLike(postId) },
                        onSharePost = { ctx, post -> feedViewModel.sharePost(ctx, post) },
                        onCommentPost = { post -> feedViewModel.openCommentSheet(post) },
                        onTopicClick = ::openTopicRoute,
                        onProfileClick = { userId ->
                            navController.navigate(Screen.Profile.route(userId))
                        }
                    )
                },
                secondary = {
                    if (topicId.isNullOrBlank()) {
                        CanonicalEmptyDetailPane(
                            icon = Icons.Default.TravelExplore,
                            title = stringResource(R.string.nav_explore),
                            subtitle = "Pick a topic to compare discovery results with its dedicated detail view side by side."
                        )
                    } else {
                        TopicDetailScreen(
                            topicName = topicId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            )
        }

        fun navigateToTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        fun unlockDeveloperMenuFromSettings() {
            if (DeviceAuthority.isAuthorizedDevice(app)) {
                val wasDevMenuEnabled = devOptions.devMenuEnabled
                DevOptionsSettings.setDevMenuEnabled(app, true)
                devOptionsViewModel.refresh(app)
                if (!wasDevMenuEnabled) {
                    android.widget.Toast.makeText(
                        app,
                        app.getString(R.string.easter_egg_secret_unlocked),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                navController.navigate(Screen.DevOptions.route)
            } else {
                android.widget.Toast.makeText(
                    app,
                    app.getString(
                        R.string.error_feature_unavailable,
                        app.getString(R.string.settings_developer_options_group)
                    ),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showPrimaryNavigation && canonicalLayout.navigationChrome == CanonicalNavigationChrome.BOTTOM_BAR) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = NavigationBarDefaults.containerColor,
                        tonalElevation = NavigationBarDefaults.Elevation
                    ) {
                        Column {
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth(),
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                topLevelScreens.forEach { screen ->
                                    val isSelected = selectedTopLevelRoute == screen.route || currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        modifier = if (screen == Screen.Settings) {
                                            Modifier.combinedClickable(
                                                onClick = { navigateToTopLevel(screen.route) },
                                                onLongClick = { unlockDeveloperMenuFromSettings() }
                                            )
                                        } else Modifier,
                                        icon = {
                                            Icon(
                                                if (isSelected) screen.iconFilled else screen.iconOutlined,
                                                stringResource(screen.labelId)
                                            )
                                        },
                                        label = { Text(stringResource(screen.labelId)) },
                                        selected = isSelected,
                                        onClick = {
                                            navigateToTopLevel(screen.route)
                                        }
                                    )
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val navHostPadding = if (showPrimaryNavigation && canonicalLayout.navigationChrome == CanonicalNavigationChrome.BOTTOM_BAR) {
                innerPadding
            } else {
                PaddingValues(top = innerPadding.calculateTopPadding())
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val navHostContent: @Composable (Modifier) -> Unit = { hostModifier ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Feed.route,
                        modifier = hostModifier
                            .padding(navHostPadding)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                composable(Screen.Feed.route) {
                    FeedScreen(
                        feedUiState = feedState,
                        onAddPost = { content, tone, imageUrl, videoUrl, _, _ ->
                            feedViewModel.createPost(content, tone, imageUrl, videoUrl)
                        },
                        onAddStory = { type, content, _, _, duration, _, linkData ->
                            feedViewModel.createStory(
                                imageUrl = if (type == StoryContentType.IMAGE || type == StoryContentType.VIDEO || type == StoryContentType.DOCUMENT || type == StoryContentType.AUDIO) content else "",
                                duration = duration,
                                contentType = type,
                                textOverlay = if (type == StoryContentType.TEXT_ONLY || type == StoryContentType.LINK) content else null,
                                linkPreview = linkData
                            )
                        },
                        onLikePost = { postId: Long -> feedViewModel.toggleLike(postId) },
                        onReplyPost = { post: Post -> feedViewModel.openCommentSheet(post) },
                        onSharePost = { ctx: Context, post: Post -> feedViewModel.sharePost(ctx, post) },
                        onDeletePost = { postId: Long -> feedViewModel.deletePost(postId) },
                        onProfileClick = { userId ->
                            navController.navigate(Screen.Profile.route(userId))
                        },
                        onViewStory = { story -> feedViewModel.viewStory(story) },
                        isPremium = feedState.isPremium,
                        onUpgradeClick = { showPremiumDialog = true },
                        isMockInterfaceEnabled = feedState.isMockInterfaceEnabled,
                        animationSettings = themeState.animationSettings,
                        safetyState = safetyState,
                        enableNewFeedLayout = devOptions.enableNewFeedLayout || compactFeedExperimentEnabled,
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onHashtagClick = { hashtag ->
                            navController.navigate(Screen.TopicDetail.route(hashtag))
                        }
                    )

                    feedState.activeStory?.let { story ->
                        key(story.id) {
                            StoryViewerDialog(
                                story = story,
                                onDismiss = { feedViewModel.dismissStory() },
                                onStoryViewed = { viewedStory ->
                                    feedViewModel.markStoryAsViewed(viewedStory.id)
                                },
                                onReply = { _, _ ->
                                    // TODO: wire to feedViewModel.sendStoryReply() when messaging backend is ready
                                    android.widget.Toast.makeText(
                                        navController.context,
                                        "Story replies coming soon!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                enableReactions = devOptions.enableStoryReactions,
                                onNextStory = { feedViewModel.advanceToNextStory() }
                            )
                        }
                    }
                }
                composable(Screen.Explore.route) {
                    if (showExploreSplitPane) {
                        exploreSplitPaneContent(null)
                    } else {
                        ExploreScreen(
                            posts = feedState.posts,
                            safetyState = safetyState,
                            onLikePost = { postId -> feedViewModel.toggleLike(postId) },
                            onSharePost = { ctx, post -> feedViewModel.sharePost(ctx, post) },
                            onCommentPost = { post -> feedViewModel.openCommentSheet(post) },
                            onTopicClick = ::openTopicRoute,
                            onProfileClick = { userId ->
                                navController.navigate(Screen.Profile.route(userId))
                            }
                        )
                    }
                }
                composable(Screen.Messages.route) {
                    if (showMessagesSplitPane) {
                        messagesSplitPaneContent(selectedConversation)
                    } else {
                        NeuroInboxScreen(
                            conversations = messagesState.conversations,
                            safetyState = safetyState,
                            onOpenConversation = ::openConversationRoute,
                            onStartNewChat = { userId ->
                                val conversationId = messagesViewModel.startOrOpenConversation(userId)
                                openConversationRoute(conversationId)
                            },
                            onOpenCallHistory = {
                                navController.navigate(Screen.CallHistory.route)
                            },
                            onStartCall = { userId, displayName, avatarUrl, isVideo ->
                                if (feedState.isMockInterfaceEnabled) {
                                    MockCallManager.startCall(
                                        recipientId = userId,
                                        recipientName = displayName,
                                        recipientAvatar = avatarUrl,
                                        callType = if (isVideo) CallType.VIDEO else CallType.VOICE
                                    )
                                } else {
                                    WebRTCCallManager.getInstance().startCall(
                                        recipientId = userId,
                                        recipientName = displayName,
                                        recipientAvatar = avatarUrl,
                                        callType = if (isVideo) com.kyilmaz.neurocomet.calling.CallType.VIDEO else com.kyilmaz.neurocomet.calling.CallType.VOICE
                                    )
                                }
                            }
                        )
                    }
                }
                composable(Screen.Conversation.route) { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId")
                    val conv = messagesState.conversations.find { it.id == conversationId }
                        ?: messagesState.activeConversation
                    if (showMessagesSplitPane) {
                        messagesSplitPaneContent(conv)
                    } else if (conv == null) {
                        NeuroInboxScreen(
                            conversations = messagesState.conversations,
                            safetyState = safetyState,
                            onOpenConversation = ::openConversationRoute,
                            onStartNewChat = { userId ->
                                val cId = messagesViewModel.startOrOpenConversation(userId)
                                openConversationRoute(cId)
                            },
                            onBack = { navController.popBackStack() },
                            onOpenCallHistory = {
                                navController.navigate(Screen.CallHistory.route)
                            },
                            onOpenPracticeCall = {
                                navController.navigate(Screen.PracticeCallSelection.route)
                            },
                            onStartCall = { userId, displayName, avatarUrl, isVideo ->
                                if (feedState.isMockInterfaceEnabled) {
                                    MockCallManager.startCall(
                                        recipientId = userId,
                                        recipientName = displayName,
                                        recipientAvatar = avatarUrl,
                                        callType = if (isVideo) CallType.VIDEO else CallType.VOICE
                                    )
                                } else {
                                    WebRTCCallManager.getInstance().startCall(
                                        recipientId = userId,
                                        recipientName = displayName,
                                        recipientAvatar = avatarUrl,
                                        callType = if (isVideo) com.kyilmaz.neurocomet.calling.CallType.VIDEO else com.kyilmaz.neurocomet.calling.CallType.VOICE
                                    )
                                }
                            }
                        )
                    } else {
                        NeuroConversationScreen(
                            conversation = conv,
                            onBack = {
                                navController.popBackStack()
                                messagesViewModel.dismissConversation()
                            },
                            onSend = { recipientId, content ->
                                messagesViewModel.sendDirectMessage(recipientId, content)
                            },
                            onReport = { messageId ->
                                messagesViewModel.reportMessage(messageId)
                            },
                            onRetryMessage = { convId, msgId ->
                                messagesViewModel.retryDirectMessage(convId, msgId)
                            },
                            onReactToMessage = { messageId, emoji ->
                                messagesViewModel.reactToMessage(conv.id, messageId, emoji)
                            },
                            isUserBlocked = (conv.participants.firstOrNull { it != authState.id }?.let { messagesViewModel.isUserBlocked(it) } ?: false),
                            isUserMuted = (conv.participants.firstOrNull { it != authState.id }?.let { messagesViewModel.isUserMuted(it) } ?: false),
                            enableVideoChat = devOptions.enableVideoChat,
                            typingIndicatorVariant = dmTypingIndicatorVariant,
                            enableSimulatedReplies = BuildConfig.DEBUG && messagesState.currentUserId == "me",
                            onSimulatedReply = { conversationId, senderId, content ->
                                messagesViewModel.receiveMockReply(conversationId, senderId, content)
                            }
                        )
                    }
                }
                composable(Screen.Notifications.route) {
                    val state by feedViewModel.uiState.collectAsState()
                    NotificationsScreen(
                        notifications = state.notifications,
                        modifier = Modifier.fillMaxSize(),
                        onRefresh = { feedViewModel.fetchNotifications() },
                        onNotificationClick = { notification ->
                            when (notification.type) {
                                NotificationType.LIKE, NotificationType.COMMENT, NotificationType.MENTION, NotificationType.REPOST -> {
                                    notification.relatedPostId?.let {
                                        navController.navigate(Screen.Feed.route) {
                                            popUpTo(Screen.Notifications.route) { inclusive = true }
                                        }
                                    }
                                }
                                NotificationType.FOLLOW -> {
                                    notification.relatedUserId?.let { userId ->
                                        navController.navigate(Screen.Profile.route(userId))
                                    }
                                }
                                else -> {}
                            }
                        },
                        onMarkAsRead = { notificationId ->
                            feedViewModel.markNotificationAsRead(notificationId)
                        },
                        onMarkAllAsRead = {
                            feedViewModel.markAllNotificationsAsRead()
                        },
                        onDismissNotification = { notificationId ->
                            feedViewModel.dismissNotification(notificationId)
                        },
                        enableGrouping = notificationGroupingEnabled
                    )
                }
                composable(Screen.Settings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.Settings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        settingsOverviewContent()
                    }
                }
                composable(Screen.BackupSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.BackupSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        BackupSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.ThemeSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.ThemeSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        ThemeSettingsScreen(
                            themeViewModel = themeViewModel,
                            isPremium = feedState.isPremium,
                            isFakePremiumEnabled = feedState.isFakePremiumEnabled,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.AnimationSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.AnimationSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        AnimationSettingsScreen(
                            onBack = { navController.popBackStack() },
                            themeViewModel = themeViewModel
                        )
                    }
                }
                composable(Screen.IconCustomization.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.IconCustomization.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        IconCustomizationScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.PrivacySettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.PrivacySettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        PrivacySettingsScreen(
                            onBack = { navController.popBackStack() },
                            authViewModel = authViewModel
                        )
                    }
                }
                composable(Screen.NotificationSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.NotificationSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        NotificationSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.ContentSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.ContentSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        ContentPreferencesScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.AccessibilitySettingsScreen.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.AccessibilitySettingsScreen.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        AccessibilitySettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.WellbeingSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.WellbeingSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        WellbeingSettingsScreen(
                            onBack = { navController.popBackStack() },
                            authViewModel = authViewModel
                        )
                    }
                }
                composable(Screen.FontSettings.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.FontSettings.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        FontSettingsScreen(
                            themeViewModel = themeViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.DevOptions.route) {
                    DevOptionsScreen(
                        onBack = { navController.popBackStack() },
                        devOptionsViewModel = devOptionsViewModel,
                        safetyViewModel = safetyViewModel,
                        feedViewModel = feedViewModel,
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel,
                        onNavigateToGame = { gameId ->
                            navController.navigate(Screen.GamePlay.route(gameId))
                        },
                        onNavigateToBackup = {
                            navController.navigate(Screen.BackupSettings.route)
                        }
                    )
                }
                composable(Screen.ParentalControls.route) {
                    if (showSettingsSplitPane) {
                        CanonicalAdaptivePaneLayout(
                            primary = settingsOverviewContent,
                            secondary = { settingsDetailContent(Screen.ParentalControls.route) },
                            tertiary = settingsSupportingContent
                        )
                    } else {
                        ParentalControlsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.TopicDetail.route) { backStackEntry ->
                    val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
                    if (showExploreSplitPane) {
                        exploreSplitPaneContent(topicId)
                    } else {
                        TopicDetailScreen(
                            topicName = topicId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.Subscription.route) {
                    SubscriptionScreen(
                        onBack = { navController.popBackStack() },
                        onPurchaseSuccess = {
                            feedViewModel.setPremiumStatus(true)
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.CallHistory.route) {
                    CallHistoryScreen(
                        onBack = { navController.popBackStack() },
                        onCallUser = { userId, userName, userAvatar, callType ->
                            WebRTCCallManager.getInstance().startCall(
                                recipientId = userId,
                                recipientName = userName,
                                recipientAvatar = userAvatar,
                                callType = when (callType) {
                                    CallType.VIDEO -> com.kyilmaz.neurocomet.calling.CallType.VIDEO
                                    else -> com.kyilmaz.neurocomet.calling.CallType.VOICE
                                }
                            )
                        },
                        onOpenPracticeCallSelection = {
                            navController.navigate(Screen.PracticeCallSelection.route)
                        }
                    )
                }
                composable(Screen.PracticeCallSelection.route) {
                    PracticeCallSelectionScreen(
                        onBack = { navController.popBackStack() },
                        onPersonaSelected = { persona ->
                            navController.navigate(Screen.PracticeCall.route(persona.name))
                        }
                    )
                }
                composable(Screen.PracticeCall.route) { backStackEntry ->
                    val personaId = backStackEntry.arguments?.getString("personaId") ?: ""
                    val persona = try {
                        NeurodivergentPersona.valueOf(personaId)
                    } catch (_: IllegalArgumentException) {
                        NeurodivergentPersona.ADHD_FRIEND
                    }
                    PracticeCallScreen(
                        persona = persona,
                        onEndCall = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.Profile.route,
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "https://getneurocomet.com/u/{userId}" },
                        navDeepLink { uriPattern = "https://www.getneurocomet.com/u/{userId}" },
                    )
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    ProfileScreen(
                        userId = userId,
                        onBack = { navController.popBackStack() },
                        onMessageUser = { uid ->
                            val existingConvo = feedState.conversations.find { conv ->
                                conv.participants.contains(uid)
                            }
                            if (existingConvo != null) {
                                feedViewModel.openConversation(existingConvo.id)
                                navController.navigate(Screen.Conversation.route(existingConvo.id))
                            } else {
                                val convId = feedViewModel.startOrOpenConversation(uid)
                                navController.navigate(Screen.Conversation.route(convId))
                            }
                        },
                        onFollowToggle = { /* Toggle handled internally by ProfileScreen state */ },
                        onPostClick = {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onEditProfile = {
                            navController.navigate(Screen.MyProfile.route)
                        }
                    )
                }
                composable(
                    route = Screen.PostDetail.route,
                    arguments = listOf(navArgument("postId") { type = NavType.LongType }),
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "https://getneurocomet.com/post/{postId}" },
                        navDeepLink { uriPattern = "https://www.getneurocomet.com/post/{postId}" },
                    )
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                    PostDetailScreen(
                        postId = postId,
                        feedUiState = feedState,
                        feedViewModel = feedViewModel,
                        safetyState = safetyState,
                        currentUserId = authState.id,
                        isMockInterfaceEnabled = feedState.isMockInterfaceEnabled,
                        onBack = {
                            if (!navController.popBackStack()) {
                                // Deep-link launch: no backstack, so go to Feed.
                                navController.navigate(Screen.Feed.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onProfileClick = { uid ->
                            navController.navigate(Screen.Profile.route(uid))
                        },
                        onReplyPost = { /* reply handled inside FeedScreen flow */ },
                        onSharePost = { ctx, post -> feedViewModel.sharePost(ctx, post) }
                    )
                }
                composable(Screen.MyProfile.route) {
                    ProfileScreen(
                        userId = "me",
                        onBack = { navController.popBackStack() },
                        onMessageUser = { },
                        onFollowToggle = { },
                        onPostClick = { },
                        onEditProfile = { }
                    )
                }
                composable(Screen.GamesHub.route) {
                    com.kyilmaz.neurocomet.games.GamesHubScreen(
                        onBack = { navController.popBackStack() },
                        onGameSelected = { game ->
                            navController.navigate(Screen.GamePlay.route(game.id))
                        }
                    )
                }
                composable(Screen.GamePlay.route) { backStackEntry ->
                    val gameId = backStackEntry.arguments?.getString("gameId") ?: "bubble_pop"
                    com.kyilmaz.neurocomet.games.GameScreen(
                        gameId = gameId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.FeedbackHub.route) {
                    FeedbackHubScreen(
                        onBack = { navController.popBackStack() },
                        onOpenBugReport = { navController.navigate("feedback_bug_report") },
                        onOpenFeatureRequest = { navController.navigate("feedback_feature_request") },
                        onOpenGeneralFeedback = { navController.navigate("feedback_general") }
                    )
                }
                composable("feedback_bug_report") {
                    BugReportScreen(onBack = { navController.popBackStack() })
                }
                composable("feedback_feature_request") {
                    FeatureRequestScreen(onBack = { navController.popBackStack() })
                }
                composable("feedback_general") {
                    GeneralFeedbackScreen(onBack = { navController.popBackStack() })
                }
            }
                }

                when {
                    showPrimaryNavigation && canonicalLayout.navigationChrome == CanonicalNavigationChrome.NAVIGATION_RAIL -> {
                        Row(modifier = Modifier.fillMaxSize()) {
                            NeurodivergentNavigationRail(
                                currentRoute = selectedTopLevelRoute.orEmpty(),
                                navItems = adaptiveNavItems.filter { it.section == NavigationSection.MAIN },
                                onNavigate = ::navigateToTopLevel,
                                userAvatar = authedUser.avatarUrl,
                                onProfileClick = {
                                    if (isGuestUser) launchAuthEntry() else navigateToTopLevel(Screen.MyProfile.route)
                                },
                                highContrast = themeState.isHighContrast
                            )
                            navHostContent(Modifier.weight(1f))
                        }
                    }

                    showPrimaryNavigation && canonicalLayout.navigationChrome == CanonicalNavigationChrome.PERMANENT_DRAWER -> {
                        PermanentNavigationDrawer(
                            drawerContent = {
                                NeurodivergentPermanentDrawerContent(
                                    currentRoute = selectedTopLevelRoute.orEmpty(),
                                    navItems = adaptiveNavItems,
                                    onNavigate = ::navigateToTopLevel,
                                    userAvatar = authedUser.avatarUrl,
                                    userName = authedUser.name,
                                    isPremium = feedState.isPremium,
                                    isGuestUser = isGuestUser,
                                    onProfileClick = {
                                        if (isGuestUser) launchAuthEntry() else navigateToTopLevel(Screen.MyProfile.route)
                                    },
                                    onSettingsClick = { navigateToTopLevel(Screen.Settings.route) },
                                    collapsed = isPermanentDrawerCollapsed,
                                    onToggleCollapsed = { isPermanentDrawerCollapsed = it },
                                    highContrast = themeState.isHighContrast
                                )
                            }
                        ) {
                            navHostContent(Modifier.fillMaxSize())
                        }
                    }

                    else -> navHostContent(Modifier.fillMaxSize())
                }

            DebugPerformanceOverlay(
                enabled = PerformanceOverlayState.isEnabled,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
            )

            // Global call overlay — shows from ANY screen when a call is active
            val globalCallManager = remember { WebRTCCallManager.getInstance() }
            val globalCallState = globalCallManager.callState
            var showGlobalCallDialog by remember { mutableStateOf(false) }

            // Auto-show dialog whenever a call is active (outgoing, incoming, connected)
            LaunchedEffect(globalCallState) {
                when (globalCallState) {
                    com.kyilmaz.neurocomet.calling.CallState.RINGING,
                    com.kyilmaz.neurocomet.calling.CallState.INCOMING,
                    com.kyilmaz.neurocomet.calling.CallState.CONNECTING,
                    com.kyilmaz.neurocomet.calling.CallState.CONNECTED,
                    com.kyilmaz.neurocomet.calling.CallState.RECONNECTING -> {
                        showGlobalCallDialog = true
                    }
                    com.kyilmaz.neurocomet.calling.CallState.ENDED -> {
                        // Keep showing briefly so user sees "Call Ended"
                        kotlinx.coroutines.delay(1500)
                        showGlobalCallDialog = false
                    }
                    com.kyilmaz.neurocomet.calling.CallState.IDLE -> {
                        showGlobalCallDialog = false
                    }
                }
            }

            if (showGlobalCallDialog && globalCallManager.currentCall != null) {
                com.kyilmaz.neurocomet.calling.ActiveCallDialog(
                    callManager = globalCallManager,
                    onDismiss = { showGlobalCallDialog = false }
                )
            }

            RegulationLiveSessionHost(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp
                    )
            )
        }
        }

        if (showPremiumDialog) {
            AlertDialog(
                onDismissRequest = { showPremiumDialog = false },
                title = { Text(stringResource(R.string.premium_dialog_title)) },
                text = { Text(stringResource(R.string.premium_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { showPremiumDialog = false }) { Text(stringResource(R.string.button_ok)) }
                }
            )
        }

        CommentBottomSheet(
            isVisible = feedState.isCommentSheetVisible,
            comments = feedState.activePostComments,
            onDismiss = { feedViewModel.dismissCommentSheet() },
            onAddComment = { content -> feedViewModel.addComment(content) },
            postAuthor = feedState.posts.find { it.id == feedState.activePostId }?.userId,
            draftText = feedState.activePostId?.let { feedState.commentDraftsByPostId[it] }.orEmpty(),
            onDraftChange = { draft -> feedViewModel.updateActiveCommentDraft(draft) }
        )

        TutorialTrigger()
        TutorialOverlay()
    }
}

@Composable
private fun CanonicalAdaptivePaneLayout(
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tertiary: (@Composable () -> Unit)? = null
) {
    val canonicalLayout = LocalCanonicalLayout.current
    val showTertiary = tertiary != null && canonicalLayout.paneLayout == CanonicalPaneLayout.TRIPLE
    val primaryWeight = if (showTertiary) 0.92f else 1f
    val secondaryWeight = if (showTertiary) 1.12f else 1.08f
    val tertiaryWeight = 0.74f

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .weight(primaryWeight)
                .fillMaxHeight()
        ) {
            primary()
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

        Box(
            modifier = Modifier
                .weight(secondaryWeight)
                .fillMaxHeight()
        ) {
            secondary()
        }

        if (showTertiary) {
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Box(
                modifier = Modifier
                    .weight(tertiaryWeight)
                    .fillMaxHeight()
            ) {
                tertiary()
            }
        }
    }
}

@Composable
private fun CanonicalEmptyDetailPane(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessagesSupportPane(
    conversations: List<Conversation>,
    activeConversation: Conversation?,
    onOpenCallHistory: () -> Unit,
    onOpenPracticeCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadCount = remember(conversations) { conversations.sumOf { it.unreadCount } }
    val activeParticipantCount = activeConversation?.participants?.size ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Inbox overview", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$unreadCount unread across ${conversations.size} conversations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (activeConversation != null) {
                    HorizontalDivider()
                    Text(
                        text = "Open thread",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${activeConversation.messages.size} messages · $activeParticipantCount participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ElevatedCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Quick actions", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = onOpenCallHistory, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call history")
                }
                OutlinedButton(onClick = onOpenPracticeCall, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Practice call")
                }
            }
        }
    }
}

@Composable
private fun SettingsSupportPane(
    user: User?,
    isPremium: Boolean,
    themeState: ThemeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Profile snapshot", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = user?.name ?: "Signed in",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = user?.id ?: "Local session",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(if (isPremium) "Premium active" else "Free plan") }
                )
            }
        }

        ElevatedCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Current theme", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = themeState.selectedState.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (themeState.animationSettings.disableAllAnimations) {
                        "Motion minimized"
                    } else {
                        "Motion enabled"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Build ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

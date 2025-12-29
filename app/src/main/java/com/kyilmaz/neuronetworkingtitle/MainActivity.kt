@file:Suppress(
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "AssignmentToStateVariable"
)

package com.kyilmaz.neuronetworkingtitle

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.random.Random

// Mock for Shared Preferences to persist locale across recreates (needed for on-the-fly switch)
private const val PREFS_NAME = "app_settings"
private const val KEY_LOCALE = "selected_locale"

private fun getLocaleCode(context: Context): String {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LOCALE, "") ?: ""
}

private fun setLocaleCode(context: Context, code: String) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { putString(KEY_LOCALE, code) }
}

// Helper function to apply the locale, must be called before super.onCreate in Activity
private fun Context.applyLocale(localeCode: String): Context {
    if (localeCode.isBlank()) return this

    val locale = if (localeCode.contains("-")) {
        val parts = localeCode.split("-")
        Locale.Builder()
            .setLanguage(parts[0])
            .setRegion(parts.getOrNull(1)?.removePrefix("r") ?: "")
            .build()
    } else {
        Locale.Builder().setLanguage(localeCode).build()
    }

    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    return createConfigurationContext(config)
}


// --- 1. NAVIGATION & ROUTES ---
sealed class Screen(val route: String, val labelId: Int, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    data object Feed : Screen("feed", R.string.nav_feed, Icons.Filled.Home, Icons.Outlined.Home)
    data object Explore : Screen("explore", R.string.nav_explore, Icons.Filled.Search, Icons.Outlined.Search)
    data object Messages : Screen("messages", R.string.nav_messages, Icons.Filled.Mail, Icons.Outlined.Mail)
    data object Notifications : Screen("notifications", R.string.nav_notifications, Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    data object Badges : Screen("badges", R.string.settings_badges_title, Icons.Filled.Star, Icons.Outlined.Star)
    data object Conversation : Screen("conversation/{conversationId}", R.string.nav_messages, Icons.Filled.Mail, Icons.Outlined.Mail) {
        fun route(conversationId: String) = "conversation/$conversationId"
    }

    data object DevOptions : Screen("dev_options", R.string.settings_developer_options_group, Icons.Filled.Build, Icons.Outlined.Build)
}

// --- 3. MOCK DATA & ASSETS (Relies on DataModels.kt) ---
val INITIAL_MOCK_NOTIFICATIONS = listOf(
    NotificationItem("1", "New Badge Earned", "You verified your humanity!", "10m ago", NotificationType.SYSTEM),
    NotificationItem("2", "Alex_Stims liked your post", "The one about mechanical keyboards.", "1h ago", NotificationType.LIKE),
    NotificationItem("3", "Reply from DinoLover99", "I totally agree with that!", "2h ago", NotificationType.COMMENT)
)

val MOCK_EXPLORE_POSTS = listOf(
    Post(
        id = 1L,
        createdAt = Instant.now().minus(1, ChronoUnit.HOURS).toString(),
        content = "Just had a breakthrough on my project! Hyperfocus is a superpower when you can direct it.",
        userId = "NeuroThinker",
        likes = 125,
        comments = 18,
        shares = 5,
        isLikedByMe = true,
        userAvatar = MOCK_USERS.find { it.id == "HyperFocusCode" }?.avatarUrl,
        imageUrl = "https://example.com/image1.jpg"
    ),
    Post(
        id = 2L,
        createdAt = Instant.now().minus(3, ChronoUnit.HOURS).toString(),
        content = "Trying out my new weighted vest today. Instant calm, highly recommend for sensory regulation!",
        userId = "SensorySeeker",
        likes = 250,
        comments = 45,
        shares = 10,
        isLikedByMe = false,
        userAvatar = MOCK_USERS.find { it.id == "SensorySeeker" }?.avatarUrl,
        imageUrl = "https://picsum.photos/seed/SensorySeeker/400/300"
    ),
    Post(
        id = 3L,
        createdAt = Instant.now().minus(10, ChronoUnit.HOURS).toString(),
        content = "Need a quiet space. Going into Overload Mode for the rest of the afternoon. See you all tomorrow. #QuietMode",
        userId = "CalmObserver",
        likes = 50,
        comments = 5,
        shares = 1,
        isLikedByMe = false,
        userAvatar = MOCK_USERS.find { it.id == "CalmObserver" }?.avatarUrl,
    ),
    Post(
        id = 4L,
        createdAt = Instant.now().minus(2, ChronoUnit.DAYS).toString(),
        content = "Just finished sorting all my project files into categorized folders. The visual order is immensely satisfying.",
        userId = "NeuroNaut",
        likes = 1200,
        comments = 250,
        shares = 80,
        isLikedByMe = true,
        userAvatar = MOCK_USERS.find { it.id == "NeuroNaut" }?.avatarUrl,
    )
)

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val localeCode = getLocaleCode(newBase)
        super.attachBaseContext(newBase.applyLocale(localeCode))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, "goog_your_revenuecat_api_key_here").build())
        
        setContent {
            val feedViewModel: FeedViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val themeViewModel: ThemeViewModel = viewModel()
            val safetyViewModel: SafetyViewModel = viewModel()

            val themeState by themeViewModel.themeState.collectAsState()
            val darkIcons = !themeState.isDarkMode
            SideEffect {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = darkIcons
                    isAppearanceLightNavigationBars = darkIcons
                }
            }

            LaunchedEffect(Unit) {
                themeViewModel.setLanguageCode(getLocaleCode(this@MainActivity))
                
                Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        val isPremium = customerInfo.entitlements["premium"]?.isActive == true
                        feedViewModel.setPremiumStatus(isPremium)
                    }
                    override fun onError(error: PurchasesError) { /* Log error */ }
                })
            }

            NeuroThemeApplication(themeViewModel = themeViewModel) {
                NeuroNetApp(
                    feedViewModel = feedViewModel,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    safetyViewModel = safetyViewModel
                )
            }
        }
    }
}

// All Composables and supporting functions are moved to ThemeComposables.kt
// and other UI files to resolve compilation conflicts.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroNetApp(feedViewModel: FeedViewModel, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel, safetyViewModel: SafetyViewModel) {
    val navController = rememberNavController()
    val feedState by feedViewModel.uiState.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()

    val authedUser by authViewModel.user.collectAsState()
    val isUserVerified = authedUser?.isVerified ?: CURRENT_USER.isVerified

    var showPremiumDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedState.errorMessage) {
        val msg = feedState.errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
            feedViewModel.clearError()
        }
    }

    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as android.app.Application }
    val devOptionsViewModel: DevOptionsViewModel = viewModel()
    val devOptions by devOptionsViewModel.options.collectAsState()

    LaunchedEffect(Unit) {
        devOptionsViewModel.refresh(app)
        safetyViewModel.refresh(app)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val screens = listOf(Screen.Feed, Screen.Explore, Screen.Messages, Screen.Notifications, Screen.Settings)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(if (isSelected) screen.iconFilled else screen.iconOutlined, stringResource(screen.labelId)) },
                        label = { Text(stringResource(screen.labelId)) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = if (screen == Screen.Settings) {
                            Modifier.combinedClickable(
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onLongClick = {
                                    DevOptionsSettings.setDevMenuEnabled(app, true)
                                    devOptionsViewModel.refresh(app)
                                    navController.navigate(Screen.DevOptions.route)
                                }
                            )
                        } else Modifier
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    posts = feedState.posts,
                    stories = feedState.stories,
                    currentUser = CURRENT_USER.copy(isVerified = isUserVerified),
                    onLikePost = { postId: Long -> feedViewModel.toggleLike(postId) },
                    onReplyPost = { post: Post -> feedViewModel.openCommentSheet(post) },
                    onSharePost = { ctx: Context, post: Post -> feedViewModel.sharePost(ctx, post) },
                    onAddPost = { content: String, tone: String, imageUrl: String?, videoUrl: String? ->
                        feedViewModel.createPost(content, tone, imageUrl, videoUrl)
                    },
                    onDeletePost = { postId: Long -> feedViewModel.deletePost(postId) },
                    onProfileClick = { },
                    onViewStory = { story -> feedViewModel.viewStory(story) },
                    onAddStory = { imageUrl, duration -> feedViewModel.createStory(imageUrl, duration) },
                    isPremium = feedState.isPremium,
                    onUpgradeClick = { showPremiumDialog = true },
                    isMockInterfaceEnabled = feedState.isMockInterfaceEnabled,
                    safetyState = safetyState
                )
            }
            composable(Screen.Explore.route) { ExploreScreen(posts = MOCK_EXPLORE_POSTS, safetyState = safetyState) }
            composable(Screen.Messages.route) {
                val state by feedViewModel.uiState.collectAsState()
                DmInboxScreen(
                    conversations = state.conversations,
                    safetyState = safetyState,
                    onOpenConversation = { conversationId ->
                        feedViewModel.openConversation(conversationId)
                        navController.navigate(Screen.Conversation.route(conversationId))
                    }
                )
            }
            composable(Screen.Conversation.route) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                val state by feedViewModel.uiState.collectAsState()
                val conv = state.conversations.find { it.id == conversationId } ?: state.activeConversation
                if (conv == null) {
                    DmInboxScreen(
                        conversations = state.conversations,
                        safetyState = safetyState,
                        onOpenConversation = { id ->
                            feedViewModel.openConversation(id)
                            navController.navigate(Screen.Conversation.route(id))
                        },
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    val recipientId = conv.participants.firstOrNull { it != "me" }.orEmpty()
                    DmConversationScreen(
                        conversation = conv,
                        safetyState = safetyState,
                        onBack = {
                            navController.popBackStack()
                            feedViewModel.dismissConversation()
                        },
                        onSend = { recipientId, content ->
                            feedViewModel.sendDirectMessage(recipientId, content)
                        },
                        onReport = { messageId ->
                            feedViewModel.reportMessage(messageId)
                        },
                        onRetryMessage = { convId, msgId ->
                            feedViewModel.retryDirectMessage(convId, msgId)
                        },
                         onBlockUser = { feedViewModel.blockUser(it) },
                         onUnblockUser = { feedViewModel.unblockUser(it) },
                         onMuteUser = { feedViewModel.muteUser(it) },
                         onUnmuteUser = { feedViewModel.unmuteUser(it) },
                         isBlocked = { feedViewModel.isUserBlocked(it) },
                         isMuted = { feedViewModel.isUserMuted(it) }
                     )
                 }
             }
            composable(Screen.Settings.route) {
                com.kyilmaz.neuronetworkingtitle.SettingsScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.signOut()
                        navController.popBackStack(Screen.Feed.route, true)
                    },
                    safetyViewModel = safetyViewModel,
                    devOptionsViewModel = devOptionsViewModel,
                    canShowDevOptions = devOptions.devMenuEnabled,
                    onOpenDevOptions = {
                        navController.navigate(Screen.DevOptions.route)
                    }
                )
            }
            composable(Screen.DevOptions.route) {
                DevOptionsScreen(
                    onBack = { navController.popBackStack() },
                    devOptionsViewModel = devOptionsViewModel,
                    safetyViewModel = safetyViewModel
                )
            }
        }
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text("Premium") },
            text = { Text("Premium upgrades are not wired in this demo build.") },
            confirmButton = {
                TextButton(onClick = { showPremiumDialog = false }) { Text("OK") }
            }
        )
    }
}

// --- DEV OPTIONS UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsScreen(
    onBack: () -> Unit,
    devOptionsViewModel: DevOptionsViewModel,
    safetyViewModel: SafetyViewModel
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as android.app.Application }
    val options by devOptionsViewModel.options.collectAsState()

    // Keep SafetyViewModel in sync when toggles change.
    LaunchedEffect(options.forceAudience, options.forceKidsFilterLevel, options.forcePinSet, options.forcePinVerifySuccess) {
        safetyViewModel.refresh(app)
    }

    var delayText by remember(options.dmArtificialSendDelayMs) { mutableStateOf(options.dmArtificialSendDelayMs.toString()) }
    var minIntervalText by remember(options.dmMinIntervalOverrideMs) { mutableStateOf(options.dmMinIntervalOverrideMs?.toString().orEmpty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Options") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.dm_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            devOptionsViewModel.resetAll(app)
                            safetyViewModel.refresh(app)
                        }
                    ) { Text("Reset") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "These settings are for testing DM + safety behavior. Keep them OFF in production builds.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item { HorizontalDivider() }

            // GLOBAL
            item {
                Text("Global", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Dev Menu")
                        Text("Shows Developer Options entry in Settings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = options.devMenuEnabled,
                        onCheckedChange = { devOptionsViewModel.setDevMenuEnabled(app, it) }
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show DM debug overlay")
                        Text("(Reserved) Add extra debug chips/labels in DM UI.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = options.showDmDebugOverlay,
                        onCheckedChange = { devOptionsViewModel.setShowDmDebugOverlay(app, it) }
                    )
                }
            }

            item { HorizontalDivider() }

            // DM DELIVERY
            item {
                Text("DM Delivery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Force send failure")
                        Text("All outgoing DMs will fail and become retryable.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = options.dmForceSendFailure,
                        onCheckedChange = { devOptionsViewModel.setDmForceSendFailure(app, it) }
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = delayText,
                    onValueChange = { newVal ->
                        delayText = newVal.filter { it.isDigit() }.take(5)
                    },
                    label = { Text("Artificial send delay (ms)") },
                    supportingText = { Text("Controls how long messages stay in SENDING after you press send.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            val ms = delayText.toLongOrNull() ?: 450L
                            devOptionsViewModel.setDmSendDelayMs(app, ms)
                        }
                    ) { Text("Apply") }
                }
            }

            item { HorizontalDivider() }

            // DM RATE LIMITING
            item {
                Text("DM Rate Limiting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Disable rate limit")
                        Text("Bypasses ViewModel throttle checks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = options.dmDisableRateLimit,
                        onCheckedChange = { devOptionsViewModel.setDmDisableRateLimit(app, it) }
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = minIntervalText,
                    onValueChange = { newVal ->
                        minIntervalText = newVal.filter { it.isDigit() }.take(5)
                    },
                    label = { Text("Min interval override (ms)") },
                    supportingText = { Text("Leave empty to use the default (1200ms).") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            val ms = minIntervalText.trim().takeIf { it.isNotBlank() }?.toLongOrNull()
                            devOptionsViewModel.setDmMinIntervalOverrideMs(app, ms)
                        }
                    ) { Text("Apply") }
                }
            }

            item { HorizontalDivider() }

            // MODERATION
            item {
                Text("Moderation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                val choices = listOf(
                    DevModerationOverride.OFF to "Normal",
                    DevModerationOverride.CLEAN to "Force CLEAN",
                    DevModerationOverride.FLAGGED to "Force FLAGGED",
                    DevModerationOverride.BLOCKED to "Force BLOCKED"
                )

                Column {
                    Text("Moderation override", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    choices.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { devOptionsViewModel.setModerationOverride(app, value) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = options.moderationOverride == value,
                                onClick = { devOptionsViewModel.setModerationOverride(app, value) }
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(label)
                        }
                    }
                }
            }

            item { HorizontalDivider() }

            // SAFETY
            item {
                Text("Safety Overrides", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                val choices = listOf(
                    null to "No override",
                    Audience.ADULT to "Force ADULT",
                    Audience.TEEN to "Force TEEN",
                    Audience.UNDER_13 to "Force UNDER 13"
                )

                Column {
                    Text("Force audience", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    choices.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { devOptionsViewModel.setForceAudience(app, value) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = options.forceAudience == value,
                                onClick = { devOptionsViewModel.setForceAudience(app, value) }
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(label)
                        }
                    }
                }
            }

            item {
                val choices = listOf(
                    null to "No override",
                    KidsFilterLevel.STRICT to "Force STRICT",
                    KidsFilterLevel.MODERATE to "Force MODERATE"
                )

                Column {
                    Text("Force kids filter level", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    choices.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { devOptionsViewModel.setForceKidsFilterLevel(app, value) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = options.forceKidsFilterLevel == value,
                                onClick = { devOptionsViewModel.setForceKidsFilterLevel(app, value) }
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(label)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Force PIN set")
                        Text("Makes SafetyViewModel think a parental PIN exists.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = options.forcePinSet, onCheckedChange = { devOptionsViewModel.setForcePinSet(app, it) })
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Force PIN verify success")
                        Text("Any PIN entry returns success.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = options.forcePinVerifySuccess, onCheckedChange = { devOptionsViewModel.setForcePinVerifySuccess(app, it) })
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

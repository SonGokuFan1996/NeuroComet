package com.kyilmaz.neuronetworkingtitle

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kyilmaz.neuronetworkingtitle.ui.theme.BubblyBlue
import com.kyilmaz.neuronetworkingtitle.ui.theme.BubblyOrange
import com.kyilmaz.neuronetworkingtitle.ui.theme.BubblyPink
import com.kyilmaz.neuronetworkingtitle.ui.theme.BubblyPurple
import com.kyilmaz.neuronetworkingtitle.ui.theme.BubblyTeal
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeonPink
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeonPurple
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeonTeal
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeuroNetWorkingTitleTheme

// --- REVENUECAT IMPORTS ---
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.LogLevel

// --- 1. NAVIGATION & ROUTES ---
sealed class Screen(val route: String, val label: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    data object Feed : Screen("feed", "Feed", Icons.Filled.Home, Icons.Outlined.Home)
    data object Explore : Screen("explore", "Explore", Icons.Filled.Search, Icons.Outlined.Search)
    data object Notifications : Screen("notifications", "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

// --- 2. DATA MODELS ---
data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isVerified: Boolean = false,
)

// --- 3. MOCK DATA & ASSETS ---
val CURRENT_USER = User("me", "MyProfile", "https://api.dicebear.com/7.x/avataaars/svg?seed=Me", true)

val SPECTRUM_GRADIENT = listOf(
    Color(0xFFFF6FB5),
    Color(0xFFFFB554),
    Color(0xFF5BE7C4),
    Color(0xFF6DB4FF),
    Color(0xFFD27BFF)
)

val SPECTRUM_GRADIENT_DARK = listOf(
    Color(0xFFFF7FD0),
    Color(0xFF7CFFD9),
    Color(0xFF8A7BFF)
)

val MOCK_NOTIFICATIONS = listOf(
    NotificationItem("1", "New Badge Earned", "You verified your humanity!", "10m ago", NotificationType.SYSTEM),
    NotificationItem("2", "Alex_Stims liked your post", "The one about mechanical keyboards.", "1h ago", NotificationType.LIKE),
    NotificationItem("3", "Reply from DinoLover99", "I totally agree with that!", "2h ago", NotificationType.COMMENT)
)

val EXPLORE_TOPICS = listOf(
    "ADHD Hacks" to Color(0xFFFFF3E0), "Safe Foods" to Color(0xFFE0F7FA),
    "Executive Dysfunction" to Color(0xFFF3E5F5), "Hyperfixations" to Color(0xFFFFF9C4),
    "Noise Cancelling" to Color(0xFFE8F5E9), "Stimming" to Color(0xFFE3F2FD)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize RevenueCat
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, "goog_your_revenuecat_api_key_here").build())

        setContent {
            var isQuietMode by rememberSaveable { mutableStateOf(false) }

            // ThemeViewModel to manage personalization
            val themeViewModel: ThemeViewModel = viewModel()
            val themeState by themeViewModel.themeState.collectAsState()
            
            val feedViewModel: FeedViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()

            // Apply the custom theme wrapper
            NeuroNetWorkingTitleTheme(
                darkTheme = themeState.isDarkMode, // Controlled by ViewModel now
                neuroState = themeState.selectedState,
                quietMode = isQuietMode
            ) {
                LaunchedEffect(Unit) {
                    Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            val isPremium = customerInfo.entitlements["premium"]?.isActive == true
                            feedViewModel.setPremiumStatus(isPremium)
                        }
                        override fun onError(error: PurchasesError) { /* Log error */ }
                    })
                }

                NeuroNetApp(
                    isDarkMode = themeState.isDarkMode,
                    onDarkToggle = { themeViewModel.toggleDarkMode(it) }, // Pass ViewModel function
                    isQuietMode = isQuietMode,
                    onQuietToggle = { isQuietMode = it },
                    feedViewModel = feedViewModel,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel // Pass down for Settings
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroNetApp(
    isDarkMode: Boolean,
    onDarkToggle: (Boolean) -> Unit,
    isQuietMode: Boolean,
    onQuietToggle: (Boolean) -> Unit,
    feedViewModel: FeedViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val feedState by feedViewModel.uiState.collectAsState()
    val notifications = remember { mutableStateListOf(*MOCK_NOTIFICATIONS.toTypedArray()) }
    var isUserVerified by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedState.errorMessage) {
        feedState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(message = error, withDismissAction = true)
            feedViewModel.clearError()
        }
    }

    if (feedState.isCommentSheetVisible) {
        ModalBottomSheet(onDismissRequest = { feedViewModel.dismissCommentSheet() }) {
            CommentSheetContent(
                comments = feedState.activePostComments,
                onAddComment = { feedViewModel.addComment(it) }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val screens = listOf(Screen.Feed, Screen.Explore, Screen.Notifications, Screen.Settings)

                screens.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val iconScale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f)

                    NavigationBarItem(
                        icon = { Icon(if (isSelected) screen.iconFilled else screen.iconOutlined, screen.label, Modifier.scale(iconScale)) },
                        label = { Text(screen.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        selected = isSelected,
                        onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer, selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, Screen.Feed.route, Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    posts = feedState.posts,
                    stories = feedState.stories,
                    isQuietMode = isQuietMode,
                    currentUser = CURRENT_USER.copy(isVerified = isUserVerified),
                    onQuietToggle = onQuietToggle,
                    onLikePost = { feedViewModel.toggleLike(it) },
                    onReplyPost = { feedViewModel.openCommentSheet(it) },
                    onSharePost = { feedViewModel.sharePost(context, it) },
                    onAddPost = { c, t, i, v -> feedViewModel.createPost(c, t, i, v) },
                    onDeletePost = { id -> feedViewModel.deletePost(id) },
                    onProfileClick = { },
                    isPremium = feedState.isPremium,
                    showStories = feedState.showStories,
                    isVideoAutoplayEnabled = feedState.isVideoAutoplayEnabled
                )
                if(feedState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        NeuroLoadingAnimation()
                    }
                }
            }
            composable(Screen.Explore.route) { 
                ExploreScreen(exploreTopics = EXPLORE_TOPICS) 
            }
            composable(Screen.Notifications.route) { 
                NotificationsScreen(notifications = notifications) 
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isDarkMode, onDarkToggle, isQuietMode, onQuietToggle, isUserVerified,
                    { isUserVerified = !isUserVerified },
                    feedViewModel = feedViewModel,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    isPremium = feedState.isPremium,
                    onPurchaseSuccess = { feedViewModel.setPremiumStatus(true) }
                )
            }
        }
    }
}

@Composable
fun CommentSheetContent(comments: List<Comment>, onAddComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Comments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (comments.isEmpty()) {
                item {
                    Text("No comments yet. Be the first!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))
                }
            }
            items(comments) { comment ->
                Row(verticalAlignment = Alignment.Top) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(comment.userAvatar).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(comment.userId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = CircleShape
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if (text.isNotBlank()) {
                        onAddComment(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(32.dp)) // Keyboard spacer
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    posts: List<Post>,
    stories: List<Story>,
    isQuietMode: Boolean,
    currentUser: User,
    onQuietToggle: (Boolean) -> Unit,
    onLikePost: (Long) -> Unit,
    onReplyPost: (Post) -> Unit, // Changed to accept Post object for context
    onSharePost: (Post) -> Unit, // ADDED
    onAddPost: (String, String, String?, String?) -> Unit,
    onDeletePost: (Long) -> Unit,
    onProfileClick: () -> Unit,
    isPremium: Boolean,
    showStories: Boolean,
    isVideoAutoplayEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val logoBrush = remember(isDarkTheme) {
        Brush.linearGradient(if (isDarkTheme) SPECTRUM_GRADIENT else SPECTRUM_GRADIENT_DARK)
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isQuietMode) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .graphicsLayer(alpha = 0.99f)
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        if (!isQuietMode) {
                                            drawRect(logoBrush, blendMode = BlendMode.SrcAtop)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.BubbleChart,
                                "Logo",
                                tint = if (isQuietMode) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color.White.copy(alpha = 0.95f)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("NeuroNet", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = if (isQuietMode) MaterialTheme.colorScheme.onSurface else Color.Unspecified), modifier = Modifier.graphicsLayer(alpha = 0.99f).drawWithCache { onDrawWithContent { drawContent(); if (!isQuietMode) drawRect(logoBrush, blendMode = BlendMode.SrcAtop) } })
                    }
                },
                actions = {
                    IconButton(onClick = { onQuietToggle(!isQuietMode) }) { Icon(if(isQuietMode) Icons.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp, "Quiet Mode", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = onProfileClick) { Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Text(currentUser.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, scrolledContainerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreatePostDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, elevation = FloatingActionButtonDefaults.elevation(4.dp), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Add, "Create Post"); Spacer(modifier = Modifier.width(8.dp)); Text("Post", fontWeight = FontWeight.SemiBold) }
            }
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 16.dp, bottom = innerPadding.calculateBottomPadding() + 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

            // Stories Row (Conditional)
            if (showStories) {
                item {
                    StoriesRow(stories)
                }
            }

            if (!isPremium) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ADVERTISEMENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Buy Premium to Remove Ads!", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            items(items = posts) { post ->
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }
                AnimatedVisibility(visible = isVisible, enter = fadeIn() + slideInVertically { 50 }) {
                    BubblyPostCard(
                        post = post,
                        isQuietMode = isQuietMode,
                        isAutoVideoPlayback = isVideoAutoplayEnabled, 
                        onLike = { onLikePost(post.id ?: 0L) },
                        onDelete = { onDeletePost(post.id ?: 0L) },
                        onReplyClick = { onReplyPost(post) }, // Pass post to open sheet
                        onShare = { onSharePost(post) }
                    )
                }
            }
        }
    }
    if (showCreatePostDialog) {
        CreatePostDialog(onDismiss = { showCreatePostDialog = false }, onPost = { c, t, i, v -> onAddPost(c, t, i, v); showCreatePostDialog = false })
    }
}

@Composable
fun StoriesRow(stories: List<Story>) {
    var selectedStory by remember { mutableStateOf<Story?>(null) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(stories) { story ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedStory = story }) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(2.dp, Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFFF9800))), CircleShape)
                        .padding(4.dp)
                ) {
                     AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(story.userAvatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(story.userId, style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    selectedStory?.let { story ->
        Dialog(onDismissRequest = { selectedStory = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(story.imageUrl).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(onClick = { selectedStory = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onPost: (String, String, String?, String?) -> Unit) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("/gen") }
    val tones = listOf("/gen", "/pos", "/srs", "/j", "/lh")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(32.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Create Post", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("What's on your mind?") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("Video URL (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tone Indicator", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    tones.forEach { tone -> FilterChip(selected = tone == selectedTone, onClick = { selectedTone = tone }, label = { Text(tone) }, modifier = Modifier.padding(end = 8.dp), shape = CircleShape) }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { if (text.isNotBlank()) onPost(text, selectedTone, imageUrl.ifBlank { null }, videoUrl.ifBlank { null }) }, enabled = text.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Post") }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean, onDarkToggle: (Boolean) -> Unit,
    isQuietMode: Boolean, onQuietToggle: (Boolean) -> Unit,
    isUserVerified: Boolean, onToggleVerification: () -> Unit,
    feedViewModel: FeedViewModel,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel, // Add ThemeViewModel
    isPremium: Boolean, onPurchaseSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current as Activity
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showNukeConfirmation by remember { mutableStateOf(false) }

    var simulateError by remember { mutableStateOf(feedViewModel.simulateError) }
    var simulateInfiniteLoading by remember { mutableStateOf(feedViewModel.simulateInfiniteLoading) }
    
    val is2FAEnabled by authViewModel.is2FAEnabled.collectAsState()
    val feedState by feedViewModel.uiState.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = if(isPremium) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clickable { if(!isPremium) showPremiumDialog = true }
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if(isPremium) Icons.Outlined.VerifiedUser else Icons.Outlined.Star, null, tint = if(isPremium) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(if(isPremium) "Premium Active" else "Go Premium", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if(isPremium) "Thank you for your support!" else "Remove ads & support devs", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // --- NEW: Personalized Theme Section ---
        SettingsGroup("Personalized Theme") {
            SettingsTile("Dark Mode", "Easier on the eyes.", Icons.Outlined.DarkMode, isDarkMode, onDarkToggle)
            
            Text("How are you feeling?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(com.kyilmaz.neuronetworkingtitle.NeuroState.values()) { state ->
                    FilterChip(
                        selected = themeState.selectedState == state,
                        onClick = { themeViewModel.setNeuroState(state) },
                        label = { Text(state.label) },
                        leadingIcon = { 
                            Box(modifier = Modifier.size(12.dp).background(state.seedColor, CircleShape)) 
                        }
                    )
                }
            }
            Text(
                text = themeState.selectedState.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 16.dp)
            )
        }

        SettingsGroup("Sensory & Interface") {
            SettingsTile("Quiet Mode", "Reduces saturation.", Icons.Outlined.VolumeOff, isQuietMode, onQuietToggle)
             SettingsTile("Show Stories", "Toggle story bar.", Icons.Outlined.AmpStories, feedState.showStories, { feedViewModel.toggleStories(it) })
             SettingsTile("Video Autoplay", "Play videos automatically.", Icons.Outlined.PlayCircle, feedState.isVideoAutoplayEnabled, { feedViewModel.toggleVideoAutoplay(it) })
        }
        SettingsGroup("Security") {
            SettingsTile("Verified Human", "Identity status.", Icons.Outlined.Shield, isUserVerified, {})
            SettingsTile("Two-Factor Auth", "Require code at login.", Icons.Outlined.Lock, is2FAEnabled, { authViewModel.toggle2FA(it) })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Developer Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                SettingsTile("Force Verify", "Toggle status.", Icons.Outlined.VerifiedUser, isUserVerified, { onToggleVerification() })

                // NEW: Fake Premium Toggle
                SettingsTile("Fake Premium", "Simulate paid status.", Icons.Outlined.MonetizationOn, feedState.isFakePremiumEnabled, { feedViewModel.toggleFakePremium(it) })

                // NEW: Mock Interface Toggle
                SettingsTile("Mock Interface", "Show fake data for demo.", Icons.Outlined.DesignServices, feedState.isMockInterfaceEnabled, { feedViewModel.toggleMockInterface(it) })

                Row(modifier = Modifier.fillMaxWidth().clickable { authViewModel.reset2FAState() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LockReset, null, tint = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.width(16.dp))
                    Column {
                         Text("Reset 2FA State", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                         Text("Clear verified status.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                SettingsTile("Simulate HTTP 500", "Force fetch failure.", Icons.Outlined.ErrorOutline, simulateError, {
                    simulateError = it
                    feedViewModel.simulateError = it
                })

                SettingsTile("Infinite Loading", "Stalls fetch request.", Icons.Outlined.HourglassEmpty, simulateInfiniteLoading, {
                    simulateInfiniteLoading = it
                    feedViewModel.simulateInfiniteLoading = it
                })

                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha=0.1f))
                
                Row(modifier = Modifier.fillMaxWidth().clickable { feedViewModel.stressTestDb() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Speed, null, tint = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Stress Test DB", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text("Insert 50 posts rapidly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha=0.1f))

                Row(modifier = Modifier.fillMaxWidth().clickable { feedViewModel.floodDb() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.WaterDrop, null, tint = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Flood Database", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text("Adds 5 dummy posts instantly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha=0.1f))

                Row(modifier = Modifier.fillMaxWidth().clickable { showNukeConfirmation = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Nuke Database", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text("Delete ALL posts. Irreversible.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    if(showPremiumDialog) {
        PremiumDialog(context, onDismiss = { showPremiumDialog = false }, onPurchaseSuccess = {
            onPurchaseSuccess()
            showPremiumDialog = false
        })
    }

    if(showNukeConfirmation) {
        AlertDialog(
            onDismissRequest = { showNukeConfirmation = false },
            title = { Text("Nuke Database?") },
            text = { Text("This will permanently delete all posts from the server. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        feedViewModel.nukeDb()
                        showNukeConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Yes, Nuke it") }
            },
            dismissButton = {
                TextButton(onClick = { showNukeConfirmation = false }) { Text("Cancel") }
            },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) }
        )
    }
}

@Composable
fun PremiumDialog(activity: Activity, onDismiss: () -> Unit, onPurchaseSuccess: () -> Unit) {
    fun purchaseProduct(productId: String) {
        Purchases.sharedInstance.getProducts(listOf(productId), object : GetStoreProductsCallback {
            override fun onReceived(storeProducts: List<StoreProduct>) {
                val product = storeProducts.firstOrNull { it.id == productId }
                if (product != null) {
                    val params = PurchaseParams.Builder(activity, product).build()
                    Purchases.sharedInstance.purchase(params, object : PurchaseCallback {
                        override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                            if (customerInfo.entitlements["premium"]?.isActive == true) {
                                onPurchaseSuccess()
                            }
                        }
                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                            if(!userCancelled) {
                                Toast.makeText(activity, "Purchase Error: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                } else {
                    Toast.makeText(activity, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onError(error: PurchasesError) {
                Toast.makeText(activity, "Error fetching products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remove Ads", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Choose a plan to support NeuroNet.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { purchaseProduct("neuro_monthly_no_ads") }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Monthly Subscription ($2.00)") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { purchaseProduct("neuro_lifetime_no_ads") }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("One-Time Purchase ($60.00)") }
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss) { Text("Maybe Later") }
            }
        }
    }
}

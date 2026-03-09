@file:Suppress("MissingPermission")

package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// Haptic Feedback Utility
// ============================================================================

@Suppress("DEPRECATION", "MissingPermission")
private fun hapticFeedback(context: Context, light: Boolean = false) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(
                    if (light) 10L else 25L,
                    if (light) VibrationEffect.EFFECT_TICK else VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    if (light) 10L else 25L,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    } catch (_: Exception) {
        // Ignore vibration errors
    }
}

// ============================================================================
// Color Palette - Fixed colors for backward compatibility
// Note: For dynamic theming, use MaterialTheme.colorScheme where possible
// ============================================================================

object ExploreColors {
    // Primary accent colors
    val primaryPurple = Color(0xFF7C4DFF)
    val secondaryTeal = Color(0xFF26A69A)

    // Utility colors
    val calmGreen = Color(0xFF66BB6A)
    val calmBlue = Color(0xFF42A5F5)

    // Category-specific colors (for branding consistency)
    val categoryADHD = Color(0xFFFFB74D)
    val categoryAutism = Color(0xFF7986CB)
    val categoryAnxiety = Color(0xFFBA68C8)
    val categoryDepression = Color(0xFF90A4AE)

    // Background colors
    val darkBackground = Color(0xFF0D0D12)
    val lightBackground = Color(0xFFF8F9FC)
}

// ============================================================================
// Data Classes
// ============================================================================

data class TopicData(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val postCount: String
)

data class TrendingItem(
    val hashtag: String,
    val count: String,
    val icon: ImageVector
)

data class PersonData(
    val name: String,
    val username: String,
    val bio: String,
    val avatarUrl: String,
    val isVerified: Boolean
)

data class CategoryData(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val topics: List<String>
)

data class ContentItemData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val rank: Int? = null
)

// ============================================================================
// Premium Explore Screen
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    posts: List<Post> = emptyList(),
    safetyState: SafetyState = SafetyState(),
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {},
    onPostClick: (Post) -> Unit = {},
    onLikePost: (Long) -> Unit = {},
    onSharePost: (Context, Post) -> Unit = { _, _ -> },
    onCommentPost: (Post) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Header animation
    val headerAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Premium Header
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600))
            ) {
                ExploreHeader(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onStartSearch = {
                        hapticFeedback(context)
                        isSearching = true
                    },
                    onCancelSearch = {
                        hapticFeedback(context)
                        isSearching = false
                        searchQuery = ""
                        focusManager.clearFocus()
                    },
                    onFilterClick = { showFilterSheet = true },
                    focusRequester = focusRequester,
                    modifier = Modifier.graphicsLayer { alpha = headerAlpha }
                )
            }

            // Filter Tabs (when not searching)
            AnimatedVisibility(
                visible = !isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExploreFilterTabs(
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        hapticFeedback(context, light = true)
                        selectedTabIndex = index
                    }
                )
            }

            // Content
            AnimatedContent(
                targetState = if (isSearching) -1 else selectedTabIndex,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "contentSwitch"
            ) { tabIndex ->
                when (tabIndex) {
                    -1 -> SearchView(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        focusRequester = focusRequester,
                        onProfileClick = onProfileClick,
                        onTopicClick = onTopicClick
                    )
                    0 -> ForYouTab(
                        onLikeClick = onLikePost,
                        onCommentClick = onCommentPost,
                        onShareClick = onSharePost,
                        onProfileClick = onProfileClick,
                        onTopicClick = onTopicClick
                    )
                    1 -> TrendingTab(
                        onLikeClick = onLikePost,
                        onCommentClick = onCommentPost,
                        onShareClick = onSharePost,
                        onProfileClick = onProfileClick,
                        onTopicClick = onTopicClick
                    )
                    2 -> PeopleTab(
                        onProfileClick = onProfileClick
                    )
                    3 -> TopicsTab(
                        onTopicClick = onTopicClick
                    )
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            ExploreFilterSheet(
                onCategorySelected = { category ->
                    showFilterSheet = false
                    onTopicClick(category)
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    // Request focus when search starts
    LaunchedEffect(isSearching) {
        if (isSearching) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}

// ============================================================================
// Filter Bottom Sheet
// ============================================================================

@Composable
private fun ExploreFilterSheet(
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Triple("🎯", "ADHD Hacks", "Tips & strategies"),
        Triple("🧩", "Autism Community", "Connection & support"),
        Triple("✨", "Sensory Tips", "Sensory-friendly living"),
        Triple("🧠", "Mental Health", "Wellness & recovery"),
        Triple("💪", "Executive Function", "Planning & focus"),
        Triple("🎨", "Creative Arts", "Art therapy & expression"),
        Triple("🎮", "Gaming", "ND-friendly gaming"),
        Triple("💼", "Career Paths", "Workplace tips"),
        Triple("🍽️", "Safe Foods", "ARFID & food support"),
        Triple("🤝", "Relationships", "Communication & bonds"),
        Triple("🎓", "College Transition", "Academic support"),
        Triple("🏠", "Independent Living", "Life skills")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Browse by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        categories.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (emoji, name, description) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCategorySelected(name) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                // Fill remaining space if odd number
                if (row.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ============================================================================
// Premium Explore Header
// ============================================================================

@Composable
private fun ExploreHeader(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStartSearch: () -> Unit,
    onCancelSearch: () -> Unit,
    onFilterClick: () -> Unit = {},
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        // Title Row (hidden when searching)
        AnimatedVisibility(
            visible = !isSearching,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.explore_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = if (isDark) Color.White else Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discover amazing content ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF666680)
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExploreHeaderIconButton(
                        icon = Icons.Default.Search,
                        onClick = onStartSearch,
                        contentDescription = "Search",
                        isDark = isDark
                    )
                    ExploreHeaderIconButton(
                        icon = Icons.Default.Tune,
                        onClick = onFilterClick,
                        contentDescription = "Filters",
                        isDark = isDark
                    )
                }
            }
        }

        // Search Bar (shown when searching)
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF666680)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDark) Color.White else Color(0xFF1A1A2E)
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search posts, people, topics...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF999999)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    IconButton(onClick = onCancelSearch) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel search",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF666680)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header icon button matching Notifications/Messages style with proper contrast
 */
@Composable
private fun ExploreHeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isDark: Boolean = false
) {
    val context = LocalContext.current

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                hapticFeedback(context, light = true)
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) {
            primaryColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (isPrimary) {
                    primaryColor
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ============================================================================
// Filter Tabs - Pill Style matching Notifications/Messages
// ============================================================================

@Composable
private fun ExploreFilterTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val tabs = listOf(
        Triple(Icons.Outlined.AutoAwesome, "For You", 0),
        Triple(Icons.AutoMirrored.Filled.TrendingUp, "Trending", 1),
        Triple(Icons.Outlined.People, "People", 2),
        Triple(Icons.Outlined.Tag, "Topics", 3)
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tabs.size) { index ->
            val (icon, label, tabIndex) = tabs[index]
            val isSelected = selectedIndex == tabIndex

            ExploreFilterPill(
                icon = icon,
                label = label,
                isSelected = isSelected,
                onClick = {
                    hapticFeedback(context, light = true)
                    onTabSelected(tabIndex)
                }
            )
        }
    }
}

/**
 * Individual filter pill matching Notifications/Messages style with explicit colors
 */
@Composable
private fun ExploreFilterPill(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            primaryColor
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (!isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

// ============================================================================
// Search View
// ============================================================================

@Composable
private fun SearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onProfileClick: (String) -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (searchQuery.isEmpty()) {
        RecentSearchesView(
            onSearchSelected = { query ->
                hapticFeedback(context, light = true)
                onSearchQueryChange(query)
            }
        )
    } else {
        SearchResultsView(
            query = searchQuery,
            onProfileClick = onProfileClick,
            onTopicClick = onTopicClick
        )
    }
}

@Composable
private fun RecentSearchesView(
    onSearchSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recentSearches by remember { mutableStateOf(listOf("ADHD tips", "sensory friendly", "autism community", "mental health")) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recent Searches
        if (recentSearches.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(
                        title = "Recent Searches",
                        icon = Icons.Default.History,
                        action = {
                            TextButton(onClick = {
                                hapticFeedback(context)
                                recentSearches = emptyList()
                            }) {
                                Text("Clear All")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { search ->
                            SearchChip(
                                label = search,
                                onClick = { onSearchSelected(search) }
                            )
                        }
                    }
                }
            }
        }

        // Suggested Topics
        item {
            Column {
                SectionHeader(
                    title = "Suggested Topics",
                    icon = Icons.Outlined.Lightbulb
                )

                Spacer(modifier = Modifier.height(12.dp))

                TopicsGrid(onTopicSelected = onSearchSelected)
            }
        }

        // Trending Now
        item {
            Column {
                SectionHeader(
                    title = "Trending Now",
                    icon = Icons.Default.LocalFireDepartment
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrendingList(onTrendingSelected = onSearchSelected)
            }
        }
    }
}

@Composable
private fun SearchResultsView(
    query: String,
    onProfileClick: (String) -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Results for \"$query\"",
                icon = Icons.Default.Search
            )
        }

        // Mock people results
        item {
            PersonResultCard(
                name = "ADHD Support Group",
                username = "adhd_support",
                bio = "Community for ADHD tips and support",
                avatarUrl = "https://i.pravatar.cc/150?u=adhd",
                onClick = { onProfileClick("adhd_support") }
            )
        }

        item {
            PersonResultCard(
                name = "Mindful Living",
                username = "mindful_life",
                bio = "Daily mindfulness and self-care tips",
                avatarUrl = "https://i.pravatar.cc/150?u=mindful",
                onClick = { onProfileClick("mindful_life") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Related Topics",
                icon = Icons.Outlined.Tag
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("#$query", "#${query}Community", "#${query}Tips", "#${query}Support").forEach { tag ->
                    TopicChip(
                        label = tag,
                        onClick = { onTopicClick(tag.removePrefix("#")) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Tab Content - Enhanced with Rich Mock Data
// ============================================================================

@Composable
private fun ForYouTab(
    modifier: Modifier = Modifier,
    onLikeClick: (Long) -> Unit = {},
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Context, Post) -> Unit = { _, _ -> },
    onProfileClick: (String) -> Unit = {},
    onTopicClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedChip by remember { mutableStateOf<String?>(null) }

    // Rich mock posts for "For You" feed
    val forYouPosts = remember {
        listOf(
            ExploreMockPost(
                id = 1,
                username = "HyperFocusCode",
                displayName = "Alex Chen",
                avatar = "https://i.pravatar.cc/150?u=hyperfocuscode",
                content = "Just discovered that setting 3 alarms for everything actually works! 🎯 First alarm: \"Hey, you should start thinking about this\". Second: \"Okay seriously, transition time\". Third: \"DO IT NOW\" 😅 #ADHDHacks #ExecutiveFunction",
                likes = 2847,
                comments = 342,
                shares = 156,
                timeAgo = "2h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("ADHDHacks", "ExecutiveFunction"),
                category = "ADHD Tips"
            ),
            ExploreMockPost(
                id = 2,
                username = "SensorySeeker",
                displayName = "Jordan Rivera",
                avatar = "https://i.pravatar.cc/150?u=sensoryseeker",
                content = "My new weighted blanket arrived! 15lbs of pure comfort 💙 Already feeling more grounded. For anyone curious - the pressure really does help with anxiety and sleep!",
                likes = 1923,
                comments = 187,
                shares = 89,
                timeAgo = "4h",
                isLiked = true,
                isVerified = false,
                imageUrl = "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800",
                tags = listOf("SensoryFriendly", "AnxietyRelief"),
                category = "Anxiety"
            ),
            ExploreMockPost(
                id = 3,
                username = "NeuroNurse",
                displayName = "Dr. Sam Kim",
                avatar = "https://i.pravatar.cc/150?u=neuronurse",
                content = "🧠 Quick reminder: Your brain isn't broken - it's just wired differently. What society calls \"deficits\" are often just different ways of processing the world. Embrace your unique neurology! 💜 #Neurodiversity #SelfAcceptance",
                likes = 5621,
                comments = 428,
                shares = 892,
                timeAgo = "6h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("Neurodiversity", "SelfAcceptance"),
                category = "Autism"
            ),
            ExploreMockPost(
                id = 4,
                username = "QuietQueen",
                displayName = "Maya Thompson",
                avatar = "https://i.pravatar.cc/150?u=quietqueen",
                content = "Created a \"sensory kit\" for my desk at work! Includes: fidget cube, noise-canceling earbuds, lavender roller, chewy snacks, and a small plant 🌱 Game changer for overstimulating days!",
                likes = 3156,
                comments = 276,
                shares = 234,
                timeAgo = "8h",
                isLiked = true,
                isVerified = false,
                imageUrl = "https://images.unsplash.com/photo-1416339442236-8ceb164046f8?w=800",
                tags = listOf("SensoryKit", "WorkplaceAccommodations"),
                category = "Anxiety"
            ),
            ExploreMockPost(
                id = 5,
                username = "FocusFounder",
                displayName = "Chris Lee",
                avatar = "https://i.pravatar.cc/150?u=focusfounder",
                content = "Body doubling session starting in 30 mins! Join me for 2 hours of focused work. No talking, just vibes and productivity. Link in bio! 💪✨ #BodyDoubling #ADHDCommunity",
                likes = 892,
                comments = 67,
                shares = 45,
                timeAgo = "10h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("BodyDoubling", "ADHDCommunity"),
                category = "ADHD Tips"
            ),
            ExploreMockPost(
                id = 6,
                username = "MindfulMeditator",
                displayName = "Priya Sharma",
                avatar = "https://i.pravatar.cc/150?u=mindfulmeditator",
                content = "5-minute grounding technique that actually works for racing thoughts: Name 5 things you see, 4 you hear, 3 you can touch, 2 you smell, 1 you taste. Saved me in a meeting today 🧘 #Mindfulness #GroundingTechnique",
                likes = 4312,
                comments = 389,
                shares = 678,
                timeAgo = "3h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("Mindfulness", "GroundingTechnique"),
                category = "Mindfulness"
            ),
            ExploreMockPost(
                id = 7,
                username = "SleepyNeuro",
                displayName = "Taylor Brooks",
                avatar = "https://i.pravatar.cc/150?u=sleepyneuro",
                content = "Finally found my perfect sleep stack: weighted blanket + brown noise + 68°F room + lavender pillow spray 😴 Went from 2hr to fall asleep → 15min. ADHD brains CAN sleep! #SleepTips #ADHD",
                likes = 3789,
                comments = 521,
                shares = 412,
                timeAgo = "5h",
                isLiked = true,
                isVerified = false,
                imageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=800",
                tags = listOf("SleepTips", "SleepHygiene"),
                category = "Sleep"
            ),
            ExploreMockPost(
                id = 8,
                username = "ZenCoder",
                displayName = "Lin Wei",
                avatar = "https://i.pravatar.cc/150?u=zencoder",
                content = "Started doing walking meditations during lunch and my afternoon focus is 10x better. Not sitting-still meditation — actual moving meditation. Perfect for restless brains! 🚶‍♂️✨ #Mindfulness #WalkingMeditation",
                likes = 2156,
                comments = 178,
                shares = 234,
                timeAgo = "7h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("Mindfulness", "WalkingMeditation"),
                category = "Mindfulness"
            ),
            ExploreMockPost(
                id = 9,
                username = "SpectrumStories",
                displayName = "Jamie Nguyen",
                avatar = "https://i.pravatar.cc/150?u=spectrumstories",
                content = "Reminder: masking is exhausting. If you need to unmask today, that's okay. Your comfort matters more than anyone else's convenience. 🌈💜 #Autism #Unmasking #AutismAcceptance",
                likes = 6234,
                comments = 567,
                shares = 1023,
                timeAgo = "1h",
                isLiked = true,
                isVerified = true,
                imageUrl = null,
                tags = listOf("Autism", "Unmasking", "AutismAcceptance"),
                category = "Autism"
            ),
            ExploreMockPost(
                id = 10,
                username = "NightOwlNeuro",
                displayName = "Casey Adams",
                avatar = "https://i.pravatar.cc/150?u=nightowlneuro",
                content = "Hot take: 'sleep hygiene' advice is often ableist. 'Just put your phone away' doesn't address why our brains won't shut off. What helped me: accepting my chronotype and adapting my schedule instead of fighting it 🌙 #Sleep #Chronotype",
                likes = 4567,
                comments = 623,
                shares = 567,
                timeAgo = "9h",
                isLiked = false,
                isVerified = false,
                imageUrl = null,
                tags = listOf("Sleep", "Chronotype"),
                category = "Sleep"
            ),
            ExploreMockPost(
                id = 11,
                username = "ADHDDadLife",
                displayName = "Marcus Johnson",
                avatar = "https://i.pravatar.cc/150?u=adhddadlife",
                content = "Pro tip from an ADHD dad: visual timers are a game changer for kids AND adults. My 7yo and I both use them now. He calls it 'the rainbow clock' 🌈⏰ #ADHDParenting #ADHDTips",
                likes = 3421,
                comments = 298,
                shares = 456,
                timeAgo = "11h",
                isLiked = false,
                isVerified = false,
                imageUrl = "https://images.unsplash.com/photo-1501139083538-0139583c060f?w=800",
                tags = listOf("ADHDParenting", "ADHDTips"),
                category = "ADHD Tips"
            )
        )
    }

    // Filter posts based on selected chip
    val filteredPosts = remember(selectedChip, forYouPosts) {
        if (selectedChip == null) forYouPosts
        else forYouPosts.filter { it.category == selectedChip }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Featured Stories Row
        item {
            Column {
                SectionHeader(
                    title = "Stories",
                    icon = Icons.Outlined.AutoAwesome
                )
                Spacer(modifier = Modifier.height(12.dp))
                StoriesRow()
            }
        }

        // Quick Categories
        item {
            Column {
                SectionHeader(
                    title = "Quick Access",
                    icon = Icons.Outlined.Bolt
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuickAccessChips(
                    selectedChip = selectedChip,
                    onChipSelected = { chipLabel ->
                        hapticFeedback(context, light = true)
                        selectedChip = if (selectedChip == chipLabel) null else chipLabel
                    }
                )
            }
        }

        // For You Posts
        item {
            SectionHeader(
                title = if (selectedChip != null) "$selectedChip Posts" else "Curated For You",
                icon = if (selectedChip != null) Icons.Outlined.FilterList else Icons.Outlined.Favorite
            )
        }

        if (filteredPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🔍",
                            fontSize = 48.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No posts found for \"$selectedChip\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Try a different category or check back later",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        itemsIndexed(filteredPosts) { index, post ->
            ExplorePostCard(
                post = post,
                animationDelay = index * 80,
                onLikeClick = {
                    hapticFeedback(context)
                    onLikeClick(post.id.toLong())
                },
                onCommentClick = {
                    hapticFeedback(context)
                    onCommentClick(post.toPost())
                },
                onShareClick = {
                    hapticFeedback(context)
                    onShareClick(context, post.toPost())
                },
                onProfileClick = {
                    hapticFeedback(context)
                    onProfileClick(post.username)
                },
                onTagClick = { tag -> onTopicClick(tag) }
            )
        }

        // Load more indicator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun TrendingTab(
    modifier: Modifier = Modifier,
    onLikeClick: (Long) -> Unit = {},
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Context, Post) -> Unit = { _, _ -> },
    onProfileClick: (String) -> Unit = {},
    onTopicClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val trendingHashtags = remember {
        listOf(
            TrendingHashtag("#ADHDAwareness", "12.4K", "posts today", true),
            TrendingHashtag("#NeurodiversityWeek", "8.7K", "posts today", true),
            TrendingHashtag("#SensoryFriendly", "5.2K", "posts today", false),
            TrendingHashtag("#MindfulMonday", "4.8K", "posts today", false),
            TrendingHashtag("#AutismAcceptance", "3.9K", "posts today", false),
            TrendingHashtag("#ExecutiveFunction", "2.1K", "posts today", false)
        )
    }

    val viralPosts = remember {
        listOf(
            ExploreMockPost(
                id = 101,
                username = "ADHDMemes",
                displayName = "ADHD Meme Central",
                avatar = "https://i.pravatar.cc/150?u=adhdmemes",
                content = "Me: I'll just check one thing real quick\n\n*3 hours later*\n\nMe: Wait what was I doing? 🤔😂\n\n#ADHD #Relatable",
                likes = 24521,
                comments = 1847,
                shares = 5621,
                timeAgo = "5h",
                isLiked = true,
                isVerified = true,
                imageUrl = "https://images.unsplash.com/photo-1543610892-0b1f7e6d8ac1?w=800",
                tags = listOf("ADHD", "Relatable")
            ),
            ExploreMockPost(
                id = 102,
                username = "AutismAdvocate",
                displayName = "Emma's Autism Journey",
                avatar = "https://i.pravatar.cc/150?u=autismadvocate",
                content = "🧵 Thread: Things I wish people understood about autism masking:\n\n1. It's exhausting\n2. It's often unconscious\n3. \"You don't look autistic\" isn't a compliment\n4. We do it to survive, not to deceive\n\nPlease RT to spread awareness 💙",
                likes = 18934,
                comments = 2156,
                shares = 8742,
                timeAgo = "8h",
                isLiked = false,
                isVerified = true,
                imageUrl = null,
                tags = listOf("Autism", "Masking", "Awareness")
            ),
            ExploreMockPost(
                id = 103,
                username = "TherapyTips",
                displayName = "Dr. Mental Health",
                avatar = "https://i.pravatar.cc/150?u=therapytips",
                content = "📊 Study just released: Neurodivergent individuals who found supportive communities reported 67% improvement in mental health outcomes.\n\nCommunity matters. You matter. 💜",
                likes = 15678,
                comments = 892,
                shares = 4521,
                timeAgo = "12h",
                isLiked = true,
                isVerified = true,
                imageUrl = null,
                tags = listOf("MentalHealth", "Research", "Community")
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trending Now Header with fire icon
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trending Now",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Trending Hashtags
        item {
            TrendingHashtagsSection(
                hashtags = trendingHashtags,
                onTopicClick = onTopicClick
            )
        }

        // Viral Posts Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Going Viral",
                icon = Icons.Outlined.Whatshot
            )
        }

        itemsIndexed(viralPosts) { index, post ->
            ExplorePostCard(
                post = post,
                animationDelay = index * 80,
                onLikeClick = {
                    hapticFeedback(context)
                    onLikeClick(post.id.toLong())
                },
                onCommentClick = {
                    hapticFeedback(context)
                    onCommentClick(post.toPost())
                },
                onShareClick = {
                    hapticFeedback(context)
                    onShareClick(context, post.toPost())
                },
                onProfileClick = {
                    hapticFeedback(context)
                    onProfileClick(post.username)
                },
                onTagClick = { tag -> onTopicClick(tag) },
                showTrendingBadge = true
            )
        }
    }
}

@Composable
private fun PeopleTab(
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val featuredCreators = remember {
        listOf(
            EnhancedPersonData(
                name = "Dr. Sarah Chen",
                username = "neuropsychologist",
                bio = "Clinical psychologist specializing in ADHD & autism. Author of 'The Neurodivergent Mind'. Here to spread knowledge & hope 🧠💜",
                avatar = "https://i.pravatar.cc/150?u=sarahchen",
                isVerified = true,
                followers = "124K",
                mutualFollowers = 12,
                category = "Professional"
            ),
            EnhancedPersonData(
                name = "Alex Rivera",
                username = "adhd_alex",
                bio = "ADHD advocate & content creator 🎯 Late-diagnosed at 28. Sharing my journey & tips that actually work!",
                avatar = "https://i.pravatar.cc/150?u=alexrivera",
                isVerified = true,
                followers = "89.2K",
                mutualFollowers = 8,
                category = "Creator"
            ),
            EnhancedPersonData(
                name = "Jordan Taylor",
                username = "mindful_jordan",
                bio = "Mindfulness coach for neurodivergent adults 🧘 Making meditation accessible for busy brains",
                avatar = "https://i.pravatar.cc/150?u=jordantaylor",
                isVerified = false,
                followers = "45.6K",
                mutualFollowers = 5,
                category = "Coach"
            )
        )
    }

    val suggestedPeople = remember {
        listOf(
            EnhancedPersonData(
                name = "Sam Kim",
                username = "sensory_sam",
                bio = "Sensory processing tips & product reviews ✨ OT student & SPD advocate",
                avatar = "https://i.pravatar.cc/150?u=samkim",
                isVerified = false,
                followers = "23.1K",
                mutualFollowers = 3,
                category = "Advocate"
            ),
            EnhancedPersonData(
                name = "Chris Morgan",
                username = "autism_chris",
                bio = "Autism self-advocate & speaker 🌈 AAC user. Late-diagnosed. Proudly autistic!",
                avatar = "https://i.pravatar.cc/150?u=chrismorgan",
                isVerified = true,
                followers = "67.8K",
                mutualFollowers = 15,
                category = "Speaker"
            ),
            EnhancedPersonData(
                name = "Taylor Swift-Mind",
                username = "adhd_swiftie",
                bio = "Combining special interests: Taylor Swift + ADHD content 🎵 Making executive dysfunction eras fun!",
                avatar = "https://i.pravatar.cc/150?u=adhdswiftie",
                isVerified = false,
                followers = "31.4K",
                mutualFollowers = 7,
                category = "Entertainment"
            ),
            EnhancedPersonData(
                name = "Dr. Mike Therapy",
                username = "mike_therapy",
                bio = "Licensed therapist specializing in anxiety & neurodivergence 🎓 Free resources in bio!",
                avatar = "https://i.pravatar.cc/150?u=miketherapy",
                isVerified = true,
                followers = "156K",
                mutualFollowers = 21,
                category = "Professional"
            ),
            EnhancedPersonData(
                name = "Stim Queen",
                username = "stim_queen",
                bio = "Stimming is self-care! 💫 Fidget reviews & stim toy recommendations",
                avatar = "https://i.pravatar.cc/150?u=stimqueen",
                isVerified = false,
                followers = "18.9K",
                mutualFollowers = 4,
                category = "Reviews"
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Featured Creators Section
        item {
            SectionHeader(
                title = "Featured Creators",
                icon = Icons.Filled.Star
            )
            Spacer(modifier = Modifier.height(12.dp))
            FeaturedCreatorsCarousel(creators = featuredCreators)
        }

        // Categories filter chips
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PeopleCategoryChips()
        }

        // Suggested People Header
        item {
            SectionHeader(
                title = "Suggested For You",
                icon = Icons.Outlined.PersonAdd
            )
        }

        itemsIndexed(suggestedPeople) { index, person ->
            EnhancedPersonCard(
                person = person,
                animationDelay = index * 60,
                onFollowClick = { hapticFeedback(context) },
                onProfileClick = {
                    hapticFeedback(context)
                    onProfileClick(person.username)
                }
            )
        }
    }
}

@Composable
private fun TopicsTab(
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val featuredTopics = remember {
        listOf(
            EnhancedTopicData(
                name = "ADHD Life",
                emoji = "🎯",
                description = "Tips, memes, and support for living with ADHD",
                memberCount = "45.2K",
                postsToday = 892,
                gradient = listOf(Color(0xFF7C4DFF), Color(0xFF536DFE)),
                isJoined = true,
                isHot = true
            ),
            EnhancedTopicData(
                name = "Autism Community",
                emoji = "🧩",
                description = "A safe space for autistic individuals and allies",
                memberCount = "38.7K",
                postsToday = 654,
                gradient = listOf(Color(0xFF00BFA5), Color(0xFF1DE9B6)),
                isJoined = false,
                isHot = true
            ),
            EnhancedTopicData(
                name = "Sensory World",
                emoji = "✨",
                description = "Sensory processing, stims, and comfort tips",
                memberCount = "28.1K",
                postsToday = 423,
                gradient = listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D)),
                isJoined = true,
                isHot = false
            )
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val inversePrimaryColor = MaterialTheme.colorScheme.inversePrimary

    val allTopics = remember(primaryColor, secondaryColor, tertiaryColor, inversePrimaryColor) {
        listOf(
            TopicCategory(
                name = "Mental Health",
                icon = Icons.Outlined.Psychology,
                color = tertiaryColor,
                topics = listOf(
                    SimpleTopicData("Anxiety Support", "12.3K members", "💙"),
                    SimpleTopicData("Depression Recovery", "9.8K members", "🌱"),
                    SimpleTopicData("Therapy Talk", "15.6K members", "🗣️"),
                    SimpleTopicData("Mindfulness", "21.2K members", "🧘")
                )
            ),
            TopicCategory(
                name = "Neurodivergence",
                icon = Icons.Outlined.Hub,
                color = primaryColor,
                topics = listOf(
                    SimpleTopicData("ADHD Tips", "34.5K members", "⚡"),
                    SimpleTopicData("Autism Life", "28.9K members", "🌈"),
                    SimpleTopicData("Dyslexia Support", "8.2K members", "📚"),
                    SimpleTopicData("Executive Function", "11.4K members", "🧠")
                )
            ),
            TopicCategory(
                name = "Self Care",
                icon = Icons.Outlined.Spa,
                color = secondaryColor,
                topics = listOf(
                    SimpleTopicData("Sleep Routines", "18.7K members", "😴"),
                    SimpleTopicData("Movement & Exercise", "14.2K members", "🏃"),
                    SimpleTopicData("Nutrition Tips", "9.6K members", "🥗"),
                    SimpleTopicData("Hobby Corner", "22.1K members", "🎨")
                )
            ),
            TopicCategory(
                name = "Productivity",
                icon = Icons.Outlined.Bolt,
                color = inversePrimaryColor,
                topics = listOf(
                    SimpleTopicData("Focus Hacks", "25.3K members", "🎯"),
                    SimpleTopicData("Body Doubling", "16.8K members", "👥"),
                    SimpleTopicData("Time Management", "19.4K members", "⏰"),
                    SimpleTopicData("Organization", "12.7K members", "📋")
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Featured Topics Header
        item {
            SectionHeader(
                title = "Featured Topics",
                icon = Icons.Filled.LocalFireDepartment
            )
        }

        // Featured Topic Cards (horizontal scroll)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(featuredTopics.size) { index ->
                    FeaturedTopicCard(
                        topic = featuredTopics[index],
                        onClick = {
                            hapticFeedback(context)
                            onTopicClick(featuredTopics[index].name)
                        }
                    )
                }
            }
        }

        // Browse By Category
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Browse by Category",
                icon = Icons.Outlined.Category
            )
        }

        // Category sections
        items(allTopics.size) { index ->
            TopicCategorySection(
                category = allTopics[index],
                onTopicClick = { topicName ->
                    hapticFeedback(context)
                    onTopicClick(topicName)
                }
            )
        }
    }
}

@Composable
private fun ContentTab(
    icon: ImageVector,
    title: String,
    subtitle: String,
    items: List<ContentItemData>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                SectionHeader(title = title, icon = icon)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(items) { index, item ->
            AnimatedContentItem(
                item = item,
                animationDelay = index * 50
            )
        }
    }
}

// ============================================================================
// Component Widgets
// ============================================================================

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gradient accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryColor, tertiaryColor)
                    )
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = primaryColor
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        action?.invoke()
    }
}

@Composable
private fun SearchChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TopicChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.clickable {
            hapticFeedback(context, light = true)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = primaryColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = primaryColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun TopicsGrid(
    onTopicSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val topics = listOf(
        TopicData("ADHD", Icons.Outlined.Bolt, MaterialTheme.colorScheme.inversePrimary, "12.5K"),
        TopicData("Autism", Icons.Outlined.Hub, primaryColor, "9.8K"),
        TopicData("Anxiety", Icons.Outlined.Psychology, tertiaryColor, "15.2K"),
        TopicData("Depression", Icons.Outlined.Cloud, MaterialTheme.colorScheme.outline, "11.3K"),
        TopicData("Self Care", Icons.Outlined.Spa, secondaryColor, "8.7K"),
        TopicData("Sensory", Icons.Outlined.Hearing, MaterialTheme.colorScheme.primaryContainer, "6.4K")
    )

    val rows = topics.chunked(2)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEachIndexed { colIndex, topic ->
                    AnimatedTopicCard(
                        topic = topic,
                        onClick = { onTopicSelected(topic.name) },
                        animationDelay = (rowIndex * 2 + colIndex) * 50,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AnimatedTopicCard(
    topic: TopicData,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .height(90.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = topic.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, topic.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(topic.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = topic.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = topic.color
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = topic.color.copy(alpha = 0.5f)
                )
            }

            Column {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = topic.color
                )
                Text(
                    text = "${topic.postCount} posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrendingList(
    onTrendingSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val trending = listOf(
        TrendingItem("#NeuroPositivity", "2.4K today", Icons.Outlined.Favorite),
        TrendingItem("#SensoryFriendly", "1.8K today", Icons.Outlined.Hearing),
        TrendingItem("#ADHDHacks", "3.1K today", Icons.Outlined.TipsAndUpdates),
        TrendingItem("#MindfulMoments", "956 today", Icons.Outlined.SelfImprovement)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        trending.forEachIndexed { index, item ->
            AnimatedTrendingItem(
                item = item,
                rank = index + 1,
                onClick = { onTrendingSelected(item.hashtag) },
                animationDelay = index * 100
            )
        }
    }
}

@Composable
private fun AnimatedTrendingItem(
    item: TrendingItem,
    rank: Int,
    onClick: () -> Unit,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    val offsetX by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "offsetX"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = offsetX * 3
                this.alpha = alpha
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.hashtag,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.count,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PersonResultCard(
    name: String,
    username: String,
    bio: String,
    avatarUrl: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                hapticFeedback(context)
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = { hapticFeedback(context) },
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Text(stringResource(R.string.profile_follow))
            }
        }
    }
}

@Composable
private fun AnimatedPersonCard(
    person: PersonData,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "offsetY"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY * 2
                this.alpha = alpha
            }
            .clickable { hapticFeedback(context) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = person.avatarUrl,
                contentDescription = person.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (person.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "@${person.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = person.bio,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = { hapticFeedback(context) },
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Text(stringResource(R.string.profile_follow))
            }
        }
    }
}

@Composable
private fun ResourcesSection(topicName: String) {
    val resources = listOf(
        ResourceItem("📚", R.string.resource_getting_started, R.string.resource_getting_started_desc, R.string.resource_pinned),
        ResourceItem("🎨", R.string.resource_themes, R.string.resource_themes_desc, R.string.resource_new),
        ResourceItem("🔤", R.string.resource_fonts, R.string.resource_fonts_desc, R.string.resource_fonts_count),
        ResourceItem("🎬", R.string.resource_animation, R.string.resource_animation_desc, R.string.resource_animation_count),
        ResourceItem("🎄", R.string.resource_events, R.string.resource_events_desc, R.string.resource_events_count),
        ResourceItem("🛡️", R.string.resource_safety, R.string.resource_safety_desc, R.string.resource_essential),
        ResourceItem("💡", R.string.resource_tips, R.string.resource_tips_desc, R.string.resource_tips_count),
        ResourceItem("🔗", R.string.resource_links, R.string.resource_links_desc, R.string.resource_links_updated),
        ResourceItem("📝", R.string.resource_wiki, R.string.resource_wiki_desc, R.string.resource_wiki_count),
        ResourceItem("🎥", R.string.resource_videos, R.string.resource_videos_desc, R.string.resource_videos_count),
        ResourceItem("📖", R.string.resource_reading, R.string.resource_reading_desc, R.string.resource_reading_count)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.topic_resources_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        resources.forEach { resource ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open resource */ },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(resource.titleRes),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(resource.descriptionRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(resource.metaRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private data class ResourceItem(
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val metaRes: Int
)

@Composable
private fun EventsSection(topicName: String) {
    val events = listOf(
        EventItem("🎤", R.string.event_weekly_chat, R.string.event_weekly_chat_time, R.string.event_type_discussion, true),
        EventItem("📚", R.string.event_book_club, R.string.event_book_club_time, R.string.event_type_reading, false),
        EventItem("🧘", R.string.event_mindfulness, R.string.event_mindfulness_time, R.string.event_type_meditation, false),
        EventItem("🎮", R.string.event_game_night, R.string.event_game_night_time, R.string.event_type_social, false),
        EventItem("💬", R.string.event_qa, R.string.event_qa_time, R.string.event_type_ama, false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.topic_events_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        events.forEach { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open event */ },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (event.isLive)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(event.titleRes),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (event.isLive)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.isLive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = Color(0xFFEF4444)
                                ) {
                                    Text(
                                        text = stringResource(R.string.event_live),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(event.datetimeRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (event.isLive)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(event.typeRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (event.isLive)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!event.isLive) {
                        Button(
                            onClick = { /* RSVP */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(stringResource(R.string.event_rsvp), style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = { /* Join */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        ) {
                            Text(stringResource(R.string.event_join), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

private data class EventItem(
    val emoji: String,
    val titleRes: Int,
    val datetimeRes: Int,
    val typeRes: Int,
    val isLive: Boolean
)

@Composable
private fun MembersSection(topicName: String) {
    val context = LocalContext.current
    val members = listOf(
        MemberItem("CommunityMod", R.string.member_role_moderator, "https://i.pravatar.cc/150?u=mod1", true),
        MemberItem("HelpfulHelper", R.string.member_role_contributor, "https://i.pravatar.cc/150?u=helper", true),
        MemberItem("NewbieFriend", R.string.member_role_active, "https://i.pravatar.cc/150?u=newbie", false),
        MemberItem("WiseOwl", R.string.member_role_veteran, "https://i.pravatar.cc/150?u=owl", true),
        MemberItem("SupportiveSoul", R.string.member_role_member, "https://i.pravatar.cc/150?u=soul", false),
        MemberItem("CuriousCat", R.string.member_role_new, "https://i.pravatar.cc/150?u=cat", false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.topic_members_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                android.widget.Toast.makeText(context, "Showing all members of $topicName", android.widget.Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(R.string.explore_see_all))
            }
        }

        // Online indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.explore_online_now, members.count { it.isOnline }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open profile */ }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(
                        model = member.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    if (member.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.username,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(member.roleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = { /* Follow/Message */ },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(stringResource(R.string.member_follow), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private data class MemberItem(
    val username: String,
    val roleRes: Int,
    val avatarUrl: String,
    val isOnline: Boolean
)

// ============================================================================
// Animated Components
// ============================================================================

@Composable
private fun AnimatedCategoryCard(
    category: CategoryData,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { -it / 10 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.topics.take(4).forEach { topic ->
                        AssistChip(
                            onClick = { /* Navigate to topic */ },
                            label = {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    if (category.topics.size > 4) {
                        AssistChip(
                            onClick = { /* Show more */ },
                            label = {
                                Text(
                                    text = "+${category.topics.size - 4}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedContentItem(
    item: ContentItemData,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { -it / 10 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge (if applicable)
                if (item.rank != null) {
                    Text(
                        text = "#${item.rank}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = item.color,
                        modifier = Modifier.width(40.dp)
                    )
                }

                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Arrow
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ============================================================================
// Enhanced Mock Data Classes
// ============================================================================

private data class ExploreMockPost(
    val id: Int,
    val username: String,
    val displayName: String,
    val avatar: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val timeAgo: String,
    val isLiked: Boolean,
    val isVerified: Boolean,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val category: String = ""
) {
    /** Convert to a [Post] for sharing/commenting callbacks. */
    fun toPost(): Post = Post(
        id = id.toLong(),
        userId = username,
        userAvatar = avatar,
        content = content,
        likes = likes,
        comments = comments,
        shares = shares,
        imageUrl = imageUrl,
        createdAt = java.time.Instant.now().toString(),
        isLikedByMe = isLiked
    )
}

private data class TrendingHashtag(
    val tag: String,
    val count: String,
    val subtitle: String,
    val isHot: Boolean
)

private data class EnhancedPersonData(
    val name: String,
    val username: String,
    val bio: String,
    val avatar: String,
    val isVerified: Boolean,
    val followers: String,
    val mutualFollowers: Int,
    val category: String
)

private data class EnhancedTopicData(
    val name: String,
    val emoji: String,
    val description: String,
    val memberCount: String,
    val postsToday: Int,
    val gradient: List<Color>,
    val isJoined: Boolean,
    val isHot: Boolean
)

private data class SimpleTopicData(
    val name: String,
    val memberCount: String,
    val emoji: String
)

private data class TopicCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val topics: List<SimpleTopicData>
)

// ============================================================================
// Stories Row Component
// ============================================================================

@Composable
private fun StoriesRow() {
    val stories = listOf(
        StoryData("Your Story", "https://i.pravatar.cc/150?u=you", true, false),
        StoryData("ADHDCoach", "https://i.pravatar.cc/150?u=coach", false, true),
        StoryData("SensoryTips", "https://i.pravatar.cc/150?u=sensory", false, true),
        StoryData("MindfulMom", "https://i.pravatar.cc/150?u=mom", false, true),
        StoryData("NeuroDoc", "https://i.pravatar.cc/150?u=doc", false, false),
        StoryData("FocusHacks", "https://i.pravatar.cc/150?u=focus", false, true)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories.size) { index ->
            StoryItem(story = stories[index])
        }
    }
}

private data class StoryData(
    val username: String,
    val avatar: String,
    val isYourStory: Boolean,
    val hasNewStory: Boolean
)

@Composable
private fun StoryItem(story: StoryData) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Gradient ring for new stories
            if (story.hasNewStory && !story.isYourStory) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor, tertiaryColor)
                            )
                        )
                )
            } else if (!story.isYourStory) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = story.avatar,
                    contentDescription = "Story",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Add button for your story
            if (story.isYourStory) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add story",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (story.isYourStory) "Add Story" else story.username,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================================
// Quick Access Chips
// ============================================================================

@Composable
private fun QuickAccessChips(
    selectedChip: String? = null,
    onChipSelected: (String) -> Unit = {}
) {
    val chips = listOf(
        QuickChipData("ADHD Tips", "🎯", Color(0xFFFF7043)),
        QuickChipData("Mindfulness", "🧘", Color(0xFF66BB6A)),
        QuickChipData("Anxiety", "💙", Color(0xFF42A5F5)),
        QuickChipData("Autism", "🌈", Color(0xFF7C4DFF)),
        QuickChipData("Sleep", "😴", Color(0xFFAB47BC))
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips.size) { index ->
            val chip = chips[index]
            val isSelected = selectedChip == chip.label


            Surface(
                modifier = Modifier.clickable {
                    onChipSelected(chip.label)
                },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                    chip.color
                } else {
                    chip.color.copy(alpha = 0.15f)
                },
                border = if (!isSelected) {
                    BorderStroke(1.dp, chip.color.copy(alpha = 0.5f))
                } else null,
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = chip.emoji,
                        fontSize = 14.sp
                    )
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else chip.color
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear filter",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private data class QuickChipData(val label: String, val emoji: String, val color: Color)

// ============================================================================
// Explore Post Card
// ============================================================================

@Composable
private fun ExplorePostCard(
    post: ExploreMockPost,
    animationDelay: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    showTrendingBadge: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableIntStateOf(post.likes) }
    var isBookmarked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with gradient ring
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                )
                            )
                            .clickable(onClick = onProfileClick)
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = post.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (post.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "@${post.username} • ${post.timeAgo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showTrendingBadge) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Viral",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B35)
                                )
                            }
                        }
                    }

                    var showPostMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showPostMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showPostMenu,
                            onDismissRequest = { showPostMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save Post") },
                                onClick = {
                                    showPostMenu = false
                                    android.widget.Toast.makeText(context, "Post saved!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Outlined.BookmarkBorder, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    showPostMenu = false
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this post on NeuroComet: \"${post.content.take(100)}\"")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Post"))
                                },
                                leadingIcon = { Icon(Icons.Outlined.Share, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Report", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showPostMenu = false
                                    android.widget.Toast.makeText(context, "Report submitted. Thank you.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Outlined.Flag, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )

                // Tags
                if (post.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        post.tags.take(3).forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onTagClick(tag) }
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Image (if present)
                if (post.imageUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                isLiked = !isLiked
                                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                                onLikeClick()
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatCount(likeCount),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Comment button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onCommentClick)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatCount(post.comments),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onShareClick)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatCount(post.shares),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Bookmark button
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                isBookmarked = !isBookmarked
                                android.widget.Toast.makeText(
                                    context,
                                    if (isBookmarked) "Bookmarked" else "Removed bookmark",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                    )
                }
            }
        }
    }
}

// ============================================================================
// Trending Hashtags Section
// ============================================================================

@Composable
private fun TrendingHashtagsSection(
    hashtags: List<TrendingHashtag>,
    onTopicClick: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hashtags.forEachIndexed { index, hashtag ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTopicClick(hashtag.tag.removePrefix("#")) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (index < 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(28.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hashtag.tag,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (hashtag.isHot) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Hot",
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${hashtag.count} ${hashtag.subtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (index < hashtags.size - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ============================================================================
// Featured Creators Carousel
// ============================================================================

@Composable
private fun FeaturedCreatorsCarousel(creators: List<EnhancedPersonData>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(creators.size) { index ->
            FeaturedCreatorCard(creator = creators[index])
        }
    }
}

@Composable
private fun FeaturedCreatorCard(creator: EnhancedPersonData) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = creator.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = creator.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (creator.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "@${creator.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = creator.bio,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = creator.followers,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "followers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                var isFollowing by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        isFollowing = !isFollowing
                        android.widget.Toast.makeText(
                            context,
                            if (isFollowing) "Following ${creator.name}" else "Unfollowed ${creator.name}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    colors = if (isFollowing) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors()
                ) {
                    Text(if (isFollowing) "Following" else "Follow")
                }
            }
        }
    }
}

// ============================================================================
// People Category Chips
// ============================================================================

@Composable
private fun PeopleCategoryChips() {
    val categories = listOf("All", "Professionals", "Creators", "Coaches", "Advocates")
    var selectedIndex by remember { mutableIntStateOf(0) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val isSelected = selectedIndex == index
            Surface(
                modifier = Modifier.clickable { selectedIndex = index },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = categories[index],
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ============================================================================
// Enhanced Person Card
// ============================================================================

@Composable
private fun EnhancedPersonCard(
    person: EnhancedPersonData,
    animationDelay: Int,
    onFollowClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { it / 4 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                AsyncImage(
                    model = person.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (person.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "@${person.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = person.bio,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${person.followers} followers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (person.mutualFollowers > 0) {
                            Text(
                                text = " • ${person.mutualFollowers} mutual",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Follow button
                Button(
                    onClick = {
                        isFollowing = !isFollowing
                        onFollowClick()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = if (isFollowing) {
                        ButtonDefaults.outlinedButtonColors()
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    border = if (isFollowing) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else null,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// ============================================================================
// Featured Topic Card
// ============================================================================

@Composable
private fun FeaturedTopicCard(
    topic: EnhancedTopicData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = topic.gradient)
                )
        ) {
            // Large emoji decoration
            Text(
                text = topic.emoji,
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .offset(x = 10.dp, y = (-10).dp),
                color = Color.White.copy(alpha = 0.25f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (topic.isHot) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🔥 Hot",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (topic.isJoined) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "✓ Joined",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = topic.emoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = topic.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = topic.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${topic.memberCount} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${topic.postsToday} posts today",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Topic Category Section
// ============================================================================

@Composable
private fun TopicCategorySection(
    category: TopicCategory,
    onTopicClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = { onTopicClick(category.name) }) {
                    Text("See all")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topic chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                category.topics.forEach { topic ->
                    Surface(
                        modifier = Modifier.clickable(onClick = { onTopicClick(topic.name) }),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = topic.emoji,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = topic.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = topic.memberCount,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// Utility Functions
// ============================================================================

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}


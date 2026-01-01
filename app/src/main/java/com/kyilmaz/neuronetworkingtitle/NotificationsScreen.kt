package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Production-ready Notifications Screen
 *
 * Features:
 * - Pull-to-refresh
 * - Filter chips with horizontal scroll
 * - Time-based grouping (Today, Yesterday, This Week, Earlier)
 * - Read/unread visual states
 * - Smooth animations
 * - Accessibility support
 * - Empty states per filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>?,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    onNotificationClick: ((NotificationItem) -> Unit)? = null,
    onMarkAsRead: ((String) -> Unit)? = null,
    onMarkAllAsRead: (() -> Unit)? = null,
    onDismissNotification: ((String) -> Unit)? = null
) {
    val safeList = notifications ?: emptyList()
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    // Derived states for performance
    val unreadCount by remember(safeList) {
        derivedStateOf { safeList.count { !it.isRead } }
    }

    val filteredNotifications by remember(safeList, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                NotificationFilter.ALL -> safeList
                NotificationFilter.UNREAD -> safeList.filter { !it.isRead }
                NotificationFilter.MENTIONS -> safeList.filter { it.type == NotificationType.MENTION }
                NotificationFilter.LIKES -> safeList.filter { it.type == NotificationType.LIKE }
                NotificationFilter.FOLLOWS -> safeList.filter { it.type == NotificationType.FOLLOW }
            }
        }
    }

    val groupedNotifications by remember(filteredNotifications) {
        derivedStateOf { groupNotificationsByTime(filteredNotifications) }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            NotificationsTopBar(
                unreadCount = unreadCount,
                onMarkAllAsRead = if (unreadCount > 0) onMarkAllAsRead else null
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    onRefresh?.invoke()
                    delay(800)
                    isRefreshing = false
                }
            },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Filter chips
                item(key = "filters") {
                    NotificationFilterRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        unreadCount = unreadCount
                    )
                }

                // Content
                if (filteredNotifications.isEmpty()) {
                    item(key = "empty") {
                        NotificationsEmptyState(
                            filter = selectedFilter,
                            onRefresh = onRefresh
                        )
                    }
                } else {
                    groupedNotifications.forEach { (groupTitle, items) ->
                        // Section header
                        item(key = "header_$groupTitle") {
                            SectionHeader(title = groupTitle)
                        }

                        // Notification items
                        itemsIndexed(
                            items = items,
                            key = { _, item -> item.id }
                        ) { index, notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        onMarkAsRead?.invoke(notification.id)
                                    }
                                    onNotificationClick?.invoke(notification)
                                },
                                animationDelay = index * 50
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTopBar(
    unreadCount: Int,
    onMarkAllAsRead: (() -> Unit)?
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Column {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                AnimatedVisibility(visible = unreadCount > 0) {
                    Text(
                        text = "$unreadCount unread",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            if (onMarkAllAsRead != null) {
                IconButton(
                    onClick = onMarkAllAsRead,
                    modifier = Modifier.semantics {
                        contentDescription = "Mark all notifications as read"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ============================================================================
// FILTER CHIPS
// ============================================================================

enum class NotificationFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    MENTIONS("Mentions"),
    LIKES("Likes"),
    FOLLOWS("Follows")
}

@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    unreadCount: Int
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(NotificationFilter.entries) { filter ->
            val displayLabel = when {
                filter == NotificationFilter.UNREAD && unreadCount > 0 -> "${filter.label} ($unreadCount)"
                else -> filter.label
            }

            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.semantics {
                    role = Role.Tab
                    contentDescription = "Filter notifications by ${filter.label}"
                }
            )
        }
    }
}

// ============================================================================
// SECTION HEADER
// ============================================================================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// ============================================================================
// NOTIFICATION ROW - Android-native style (not iOS-like cards)
// ============================================================================

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "rowAlpha"
    )

    val (icon, iconColor) = getNotificationStyle(notification.type)

    val backgroundColor by animateColorAsState(
        targetValue = if (notification.isRead)
            Color.Transparent
        else
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        animationSpec = tween(200),
        label = "rowBg"
    )

    // Android-native row style - no cards, just rows with dividers
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.material3.ripple(),
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics {
                    contentDescription = buildString {
                        append(notification.title)
                        append(". ")
                        append(notification.message)
                        append(". ")
                        append(notification.timestamp)
                        if (!notification.isRead) append(". Unread")
                    }
                    role = Role.Button
                },
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar / Icon
            NotificationAvatar(
                avatarUrl = notification.avatarUrl,
                icon = icon,
                iconColor = iconColor,
                notificationType = notification.type
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Title row with timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Unread indicator next to timestamp (Android style)
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                // Message
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }

        // Subtle divider between items (Android pattern)
        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp), // Align with content after avatar
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun NotificationAvatar(
    avatarUrl: String?,
    icon: ImageVector,
    iconColor: Color,
    notificationType: NotificationType
) {
    // 48dp avatar with 12dp spacing = 60dp, plus 16dp padding = 76dp for divider alignment
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            // User avatar with type badge
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            // Type badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        } else {
            // Icon only (system notifications)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================

@Composable
private fun NotificationsEmptyState(
    filter: NotificationFilter,
    onRefresh: (() -> Unit)?
) {
    val (icon, title, message) = when (filter) {
        NotificationFilter.ALL -> Triple(
            Icons.Outlined.Notifications,
            "No notifications yet",
            "When someone interacts with you, you'll see it here."
        )
        NotificationFilter.UNREAD -> Triple(
            Icons.Default.CheckCircle,
            "All caught up! ✨",
            "You've read all your notifications. Nice work!"
        )
        NotificationFilter.MENTIONS -> Triple(
            Icons.Outlined.AlternateEmail,
            "No mentions yet",
            "When someone mentions you, it'll appear here."
        )
        NotificationFilter.LIKES -> Triple(
            Icons.Default.Favorite,
            "No likes yet",
            "When someone likes your content, you'll see it here."
        )
        NotificationFilter.FOLLOWS -> Triple(
            Icons.Default.PersonAdd,
            "No new followers",
            "When someone follows you, they'll appear here."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (onRefresh != null && filter == NotificationFilter.ALL) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }
    }
}

// ============================================================================
// UTILITIES
// ============================================================================

private fun getNotificationStyle(type: NotificationType): Pair<ImageVector, Color> {
    return when (type) {
        NotificationType.LIKE -> Icons.Default.Favorite to Color(0xFFE91E63)
        NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Chat to Color(0xFF5C6BC0)
        NotificationType.FOLLOW -> Icons.Default.PersonAdd to Color(0xFF0EA5E9)
        NotificationType.MENTION -> Icons.Outlined.AlternateEmail to Color(0xFF8B5CF6)
        NotificationType.REPOST -> Icons.Default.Repeat to Color(0xFF10B981)
        NotificationType.BADGE -> Icons.Default.Star to Color(0xFFF59E0B)
        NotificationType.SYSTEM -> Icons.Default.Shield to Color(0xFF0D9488)
        NotificationType.WELCOME -> Icons.Default.WavingHand to Color(0xFF6366F1)
        NotificationType.SAFETY_ALERT -> Icons.Default.Warning to Color(0xFFEF4444)
    }
}

private fun groupNotificationsByTime(notifications: List<NotificationItem>): LinkedHashMap<String, List<NotificationItem>> {
    val today = mutableListOf<NotificationItem>()
    val yesterday = mutableListOf<NotificationItem>()
    val thisWeek = mutableListOf<NotificationItem>()
    val earlier = mutableListOf<NotificationItem>()

    notifications.forEach { notification ->
        val ts = notification.timestamp.lowercase().trim()
        when {
            // Today patterns
            ts.contains("just now") ||
            ts.contains("now") ||
            ts.endsWith("m ago") ||
            ts.endsWith("h ago") ||
            ts.contains("min ago") ||
            ts.contains("hour ago") ||
            ts.contains("hours ago") -> today.add(notification)

            // Yesterday patterns
            ts.contains("yesterday") ||
            ts == "1d ago" ||
            ts == "1 day ago" -> yesterday.add(notification)

            // This week patterns (2-6 days)
            ts.matches(Regex("^[2-6]d ago$")) ||
            ts.matches(Regex("^[2-6] days? ago$")) -> thisWeek.add(notification)

            // Earlier (everything else)
            else -> earlier.add(notification)
        }
    }

    return linkedMapOf<String, List<NotificationItem>>().apply {
        if (today.isNotEmpty()) put("Today", today)
        if (yesterday.isNotEmpty()) put("Yesterday", yesterday)
        if (thisWeek.isNotEmpty()) put("This Week", thisWeek)
        if (earlier.isNotEmpty()) put("Earlier", earlier)
    }
}

// ============================================================================
// PREVIEW
// ============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationsScreenPreview() {
    NeuroThemeApplication(themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        NotificationsScreen(
            notifications = listOf(
                NotificationItem(
                    id = "1",
                    title = "Welcome to NeuroNet! 👋",
                    message = "We're so glad you're here. This is a safe space for neurodivergent minds.",
                    timestamp = "Just now",
                    type = NotificationType.WELCOME,
                    isRead = false
                ),
                NotificationItem(
                    id = "2",
                    title = "New Badge Earned",
                    message = "You verified your humanity! 🎉",
                    timestamp = "10m ago",
                    type = NotificationType.BADGE,
                    isRead = false
                ),
                NotificationItem(
                    id = "3",
                    title = "Alex_Stims liked your post",
                    message = "The one about mechanical keyboards.",
                    timestamp = "1h ago",
                    type = NotificationType.LIKE,
                    isRead = false,
                    avatarUrl = avatarUrl("alex"),
                    relatedUserId = "Alex_Stims"
                ),
                NotificationItem(
                    id = "4",
                    title = "New follower",
                    message = "NeuroDiverse_Dan started following you",
                    timestamp = "Yesterday",
                    type = NotificationType.FOLLOW,
                    isRead = true,
                    avatarUrl = avatarUrl("dan")
                ),
                NotificationItem(
                    id = "5",
                    title = "You were mentioned",
                    message = "@CalmObserver: Check out this accessibility tip!",
                    timestamp = "2d ago",
                    type = NotificationType.MENTION,
                    isRead = true,
                    avatarUrl = avatarUrl("calm")
                ),
                NotificationItem(
                    id = "6",
                    title = "Safety reminder",
                    message = "Remember to take breaks! You've been active for a while. 💙",
                    timestamp = "3d ago",
                    type = NotificationType.SYSTEM,
                    isRead = true
                )
            ),
            onRefresh = {},
            onNotificationClick = {},
            onMarkAsRead = {},
            onMarkAllAsRead = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsEmptyPreview() {
    NeuroThemeApplication(themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        NotificationsScreen(
            notifications = emptyList(),
            onRefresh = {}
        )
    }
}

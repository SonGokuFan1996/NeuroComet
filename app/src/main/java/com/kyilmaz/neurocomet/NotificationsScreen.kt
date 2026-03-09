package com.kyilmaz.neurocomet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Notifications Screen matching Flutter design
 * Uses Material You dynamic colors from wallpaper
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
    onDismissNotification: ((String) -> Unit)? = null,
    enableGrouping: Boolean = true
) {
    val safeList = notifications ?: emptyList()
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
        containerColor = MaterialTheme.colorScheme.background
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
                // Modern header matching Flutter
                item(key = "header") {
                    NotificationsHeader(
                        unreadCount = unreadCount,
                        onMarkAllAsRead = if (unreadCount > 0) onMarkAllAsRead else null,
                        isDark = isDark
                    )
                }

                // Filter pills
                item(key = "filters") {
                    NotificationFilterPills(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        unreadCount = unreadCount,
                        isDark = isDark
                    )
                }

                // Content
                if (filteredNotifications.isEmpty()) {
                    item(key = "empty") {
                        NotificationsEmptyState(
                            filter = selectedFilter,
                            onRefresh = onRefresh,
                            isDark = isDark
                        )
                    }
                } else if (enableGrouping) {
                    groupedNotifications.forEach { (groupKey, items) ->
                        // Section header
                        item(key = "header_${groupKey.name}") {
                            AnimatedSectionHeader(
                                title = stringResource(groupKey.labelRes),
                                count = items.size,
                                isDark = isDark
                            )
                        }

                        // Notification items
                        itemsIndexed(
                            items = items,
                            key = { _, item -> item.id }
                        ) { index, notification ->
                            EnhancedNotificationTile(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        onMarkAsRead?.invoke(notification.id)
                                    }
                                    onNotificationClick?.invoke(notification)
                                },
                                onDismiss = {
                                    onDismissNotification?.invoke(notification.id)
                                },
                                isDark = isDark,
                                animationDelay = index * 50
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = filteredNotifications,
                        key = { _, item -> item.id }
                    ) { index, notification ->
                        EnhancedNotificationTile(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    onMarkAsRead?.invoke(notification.id)
                                }
                                onNotificationClick?.invoke(notification)
                            },
                            onDismiss = {
                                onDismissNotification?.invoke(notification.id)
                            },
                            isDark = isDark,
                            animationDelay = index * 50
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MODERN HEADER - Matching Flutter design
// ============================================================================

@Composable
private fun NotificationsHeader(
    unreadCount: Int,
    onMarkAllAsRead: (() -> Unit)?,
    isDark: Boolean
) {
     Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.notifications_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(12.dp))
                        AnimatedUnreadBadge(count = unreadCount)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (unreadCount > 0) {
                        if (unreadCount == 1) stringResource(R.string.notifications_subtitle_new_one)
                        else stringResource(R.string.notifications_subtitle_new_many, unreadCount)
                    } else {
                        stringResource(R.string.notifications_subtitle_caught_up)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onMarkAllAsRead != null) {
                MarkAllReadButton(
                    onClick = onMarkAllAsRead,
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Animated unread badge with pulsing effect - uses dynamic colors
 */
@Composable
private fun AnimatedUnreadBadge(count: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, tertiaryColor)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/**
 * Mark all read button - uses dynamic colors
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun MarkAllReadButton(
    onClick: () -> Unit,
    isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = primaryColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = primaryColor
            )
            Text(
                text = stringResource(R.string.notifications_mark_all_read_short),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor
            )
        }
    }
}

// ============================================================================
// FILTER PILLS - Matching Flutter design
// ============================================================================

enum class NotificationFilter(val labelRes: Int, val icon: ImageVector) {
    ALL(R.string.filter_all, Icons.Rounded.Notifications),
    UNREAD(R.string.filter_unread, Icons.Rounded.MarkEmailUnread),
    MENTIONS(R.string.filter_mentions, Icons.Rounded.AlternateEmail),
    LIKES(R.string.filter_likes, Icons.Rounded.Favorite),
    FOLLOWS(R.string.filter_follows, Icons.Rounded.PersonAdd)
}

@Composable
private fun NotificationFilterPills(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    unreadCount: Int,
    isDark: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(NotificationFilter.entries) { filter ->
            val count = when (filter) {
                NotificationFilter.UNREAD -> if (unreadCount > 0) unreadCount else null
                else -> null
            }

            FilterPill(
                filter = filter,
                count = count,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                isDark = isDark
            )
        }
    }
}

@Composable
private fun FilterPill(
    filter: NotificationFilter,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val label = stringResource(filter.labelRes)
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
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
            modifier = Modifier.padding(
                horizontal = if (isSelected) 16.dp else 14.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            if (count != null) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else primaryColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION HEADER - Matching Flutter design with gradient accent
// ============================================================================

@Suppress("UNUSED_PARAMETER")
@Composable
private fun AnimatedSectionHeader(
    title: String,
    count: Int,
    isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "headerAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gradient accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, tertiaryColor)
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// ENHANCED NOTIFICATION TILE - Matching Flutter design
// ============================================================================

@Suppress("UNUSED_PARAMETER")
@Composable
private fun EnhancedNotificationTile(
    notification: NotificationItem,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    isDark: Boolean,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "tileAlpha"
    )

    val (icon, iconColor) = getNotificationStyle(notification.type)
    val hasUnread = !notification.isRead

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (hasUnread) {
            primaryColor.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                brush = Brush.linearGradient(colors = listOf(primaryColor, tertiaryColor)),
                                shape = CircleShape
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(if (hasUnread) 2.dp else 0.dp)
                        .size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (notification.avatarUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(notification.avatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
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
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = notification.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (hasUnread) primaryColor else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasUnread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(primaryColor, CircleShape)
                        )
                    }
                }
            }

            // Dismiss button
            if (onDismiss != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(bounded = true, radius = 16.dp),
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.notifications_remove),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// EMPTY STATE - Matching Flutter design
// ============================================================================

@Composable
private fun NotificationsEmptyState(
    filter: NotificationFilter,
    onRefresh: (() -> Unit)?,
    isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val successColor = Color(0xFF4CAF50)

    val (icon, emoji, title, message, color) = when (filter) {
        NotificationFilter.ALL -> listOf(
            Icons.Outlined.Notifications,
            "🌟",
            stringResource(R.string.notifications_empty_all_title),
            stringResource(R.string.notifications_empty_all_message),
            primaryColor
        )
        NotificationFilter.UNREAD -> listOf(
            Icons.Default.CheckCircle,
            "✨",
            stringResource(R.string.notifications_empty_unread_title),
            stringResource(R.string.notifications_empty_unread_message),
            successColor
        )
        NotificationFilter.MENTIONS -> listOf(
            Icons.Outlined.AlternateEmail,
            "💬",
            stringResource(R.string.notifications_empty_mentions_title),
            stringResource(R.string.notifications_empty_mentions_message),
            MaterialTheme.colorScheme.secondary
        )
        NotificationFilter.LIKES -> listOf(
            Icons.Default.Favorite,
            "💜",
            stringResource(R.string.notifications_empty_likes_title),
            stringResource(R.string.notifications_empty_likes_message),
            Color(0xFFE91E63)
        )
        NotificationFilter.FOLLOWS -> listOf(
            Icons.Default.PersonAdd,
            "🤝",
            stringResource(R.string.notifications_empty_follows_title),
            stringResource(R.string.notifications_empty_follows_message),
            MaterialTheme.colorScheme.tertiary
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji
            Text(
                text = emoji as String,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(16.dp))

            // Icon container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background((color as Color).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon as ImageVector,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = color
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = title as String,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message as String,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (onRefresh != null && filter == NotificationFilter.ALL) {
                Spacer(Modifier.height(24.dp))
                Button(onClick = onRefresh) {
                    Text(stringResource(R.string.notifications_refresh))
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

enum class NotificationTimeGroup(val labelRes: Int) {
    TODAY(R.string.notifications_group_today),
    YESTERDAY(R.string.notifications_group_yesterday),
    THIS_WEEK(R.string.notifications_group_this_week),
    EARLIER(R.string.notifications_group_earlier)
}

private fun groupNotificationsByTime(notifications: List<NotificationItem>): LinkedHashMap<NotificationTimeGroup, List<NotificationItem>> {
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

    return linkedMapOf<NotificationTimeGroup, List<NotificationItem>>().apply {
        if (today.isNotEmpty()) put(NotificationTimeGroup.TODAY, today)
        if (yesterday.isNotEmpty()) put(NotificationTimeGroup.YESTERDAY, yesterday)
        if (thisWeek.isNotEmpty()) put(NotificationTimeGroup.THIS_WEEK, thisWeek)
        if (earlier.isNotEmpty()) put(NotificationTimeGroup.EARLIER, earlier)
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
                    title = "Welcome to NeuroComet! 👋",
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


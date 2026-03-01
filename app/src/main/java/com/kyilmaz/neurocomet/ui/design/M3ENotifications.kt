package com.kyilmaz.neurocomet.ui.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kyilmaz.neurocomet.NotificationType

/**
 * M3E Notification Components for NeuroComet
 *
 * Notification tiles matching Flutter version with:
 * - Type badge overlay on avatar
 * - Unread indicator dot
 * - Time-based grouping headers
 * - Swipe-to-dismiss support
 * - Accessibility semantics
 */

/**
 * Notification data class for M3E components.
 */
data class M3ENotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val actionUrl: String? = null,
    val relatedPostId: String? = null
)

/**
 * M3E Notification Tile - Row-based design matching Android native style.
 */
@Composable
fun M3ENotificationTile(
    notification: M3ENotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationIndex: Int = 0,
    reduceMotion: Boolean = false
) {
    var visible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            kotlinx.coroutines.delay(M3EAnimations.staggeredDelay(animationIndex).toLong())
            visible = true
        }
    }

    val tileContent = @Composable {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(
                    if (notification.isRead) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    }
                )
                .padding(
                    horizontal = M3EDesignSystem.Spacing.md,
                    vertical = M3EDesignSystem.Spacing.sm
                )
                .semantics {
                    contentDescription = "${notification.title}: ${notification.message}"
                },
            verticalAlignment = Alignment.Top
        ) {
            // Avatar with type badge
            NotificationAvatar(
                avatarUrl = notification.avatarUrl,
                type = notification.type
            )

            Spacer(modifier = Modifier.width(M3EDesignSystem.Spacing.md))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Title row with timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = notification.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Unread indicator dot
                        if (!notification.isRead) {
                            M3EUnreadDot(size = 8.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Message
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (reduceMotion) {
        tileContent()
    } else {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(M3EDesignSystem.AnimationDuration.cardEntry)
            ) + slideInHorizontally(
                initialOffsetX = { -it / 10 },
                animationSpec = tween(M3EDesignSystem.AnimationDuration.cardEntry)
            )
        ) {
            tileContent()
        }
    }
}

/**
 * Notification avatar with type badge overlay.
 */
@Composable
private fun NotificationAvatar(
    avatarUrl: String?,
    type: NotificationType,
    size: Dp = M3EDesignSystem.AvatarSize.notification
) {
    Box(
        modifier = Modifier.size(size + 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Avatar
        M3EAvatar(
            imageUrl = avatarUrl,
            size = size,
            showGradientRing = false
        )

        // Type badge in bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .background(
                    color = getNotificationTypeColor(type),
                    shape = CircleShape
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getNotificationTypeIcon(type),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.White
            )
        }
    }
}

/**
 * Get icon for notification type.
 */
private fun getNotificationTypeIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT -> Icons.Default.ChatBubble
        NotificationType.FOLLOW -> Icons.Default.PersonAdd
        NotificationType.MENTION -> Icons.Default.AlternateEmail
        NotificationType.SYSTEM -> Icons.Default.Info
        NotificationType.REPOST -> Icons.Default.Repeat
        NotificationType.BADGE -> Icons.Default.WorkspacePremium
        NotificationType.WELCOME -> Icons.Default.Celebration
        NotificationType.SAFETY_ALERT -> Icons.Default.Shield
    }
}

/**
 * Get color for notification type.
 */
private fun getNotificationTypeColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.LIKE -> Color(0xFFE91E63) // Pink
        NotificationType.COMMENT -> Color(0xFF2196F3) // Blue
        NotificationType.FOLLOW -> Color(0xFF4CAF50) // Green
        NotificationType.MENTION -> Color(0xFF9C27B0) // Purple
        NotificationType.SYSTEM -> Color(0xFF607D8B) // Blue Grey
        NotificationType.REPOST -> Color(0xFF00E676) // Green accent
        NotificationType.BADGE -> Color(0xFFFF9800) // Orange
        NotificationType.WELCOME -> Color(0xFF7C4DFF) // Deep Purple
        NotificationType.SAFETY_ALERT -> Color(0xFFF44336) // Red
    }
}

/**
 * Group header for time-based notification grouping.
 */
@Composable
fun M3ENotificationGroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(
                horizontal = M3EDesignSystem.Spacing.md,
                vertical = M3EDesignSystem.Spacing.xs
            )
    )
}

/**
 * Empty state for notifications with filter-specific messages.
 */
@Composable
fun M3ENotificationEmptyState(
    filter: String,
    modifier: Modifier = Modifier
) {
    val (icon, title, message) = when (filter.lowercase()) {
        "unread" -> Triple(
            Icons.Default.DoneAll,
            "All caught up!",
            "You've read all your notifications"
        )
        "mentions" -> Triple(
            Icons.Default.AlternateEmail,
            "No mentions yet",
            "When someone mentions you, it'll show up here"
        )
        "likes" -> Triple(
            Icons.Default.FavoriteBorder,
            "No likes yet",
            "When someone likes your posts, it'll show up here"
        )
        "follows" -> Triple(
            Icons.Default.PersonAddDisabled,
            "No new followers",
            "When someone follows you, it'll show up here"
        )
        else -> Triple(
            Icons.Default.Notifications,
            "No notifications",
            "You don't have any notifications yet"
        )
    }

    M3EEmptyState(
        icon = icon,
        title = title,
        message = message,
        modifier = modifier
    )
}

/**
 * Filter chip row for notification filtering.
 */
@Composable
fun M3ENotificationFilters(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val filters = listOf("All", "Unread", "Mentions", "Likes", "Follows")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = M3EDesignSystem.Spacing.md)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(M3EDesignSystem.Spacing.xs)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter.equals(filter, ignoreCase = true)
            val showBadge = filter == "Unread" && unreadCount > 0

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (showBadge) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                shape = M3EDesignSystem.Shapes.Chip,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}



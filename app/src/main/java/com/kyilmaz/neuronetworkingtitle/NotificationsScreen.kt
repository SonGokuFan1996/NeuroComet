package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeuroNetWorkingTitleTheme

@Composable
fun NotificationsScreen(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)) {
                Text("Notifications", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text(
                    "Stay in sync with your circles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("You're all caught up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "We'll let you know when something new appears.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(notifications, key = { it.id }) { item ->
            val color = when (item.type) {
                NotificationType.LIKE -> Color(0xFFE91E63)
                NotificationType.COMMENT -> Color(0xFF4F46E5)
                NotificationType.SYSTEM -> Color(0xFF0D9488)
            }

            ListItem(
                headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        item.message,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (item.type) {
                                NotificationType.LIKE -> Icons.Filled.Favorite
                                NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Chat
                                NotificationType.SYSTEM -> Icons.Filled.Shield
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingContent = {
                    Text(
                        item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    NeuroNetWorkingTitleTheme {
        NotificationsScreen(
            notifications = listOf(
                NotificationItem(
                    id = "1",
                    title = "New Badge Earned",
                    message = "You verified your humanity!",
                    timestamp = "10m ago",
                    type = NotificationType.SYSTEM
                ),
                NotificationItem(
                    id = "2",
                    title = "Alex_Stims liked your post",
                    message = "The one about mechanical keyboards.",
                    timestamp = "1h ago",
                    type = NotificationType.LIKE
                )
            )
        )
    }
}

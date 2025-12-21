package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsScreen(notifications: List<NotificationItem>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Notifications", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)) }
        items(notifications) { item ->
            ListItem(
                headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(item.body, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                leadingContent = {
                    val color = when(item.type) { NotificationType.LIKE -> Color(0xFFE91E63); NotificationType.COMMENT -> Color(0xFF4F46E5); NotificationType.SYSTEM -> Color(0xFF0D9488) }
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(
                            when(item.type) {
                                NotificationType.LIKE -> Icons.Filled.Favorite
                                NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Chat
                                NotificationType.SYSTEM -> Icons.Filled.Shield
                            },
                            null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingContent = { Text(item.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            )
        }
    }
}
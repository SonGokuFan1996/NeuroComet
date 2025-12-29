package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
// MODIFIED: Replaced Icons.Filled.Info with Icons.Filled.WorkspacePremium for a 'Badge' look
import androidx.compose.material.icons.filled.WorkspacePremium 
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeuroNetWorkingTitleTheme

@Composable
fun NotificationCard(item: NotificationItem) {
    val icon: ImageVector = when (item.type) {
        NotificationType.LIKE -> Icons.Filled.Favorite
        NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Comment
        // MODIFIED: Use the new badge icon
        NotificationType.SYSTEM -> Icons.Filled.WorkspacePremium
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        // ADDED: subtle elevation for depth
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Notification Icon for ${item.type.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp).align(Alignment.Top)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = item.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun NotificationsScreen(notifications: List<NotificationItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No new notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(item = item)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    val mockNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "New Connection Request!",
            message = "Your profile caught the attention of Alex. Check them out!",
            timestamp = "5 min ago",
            type = NotificationType.SYSTEM
        ),
        NotificationItem(
            id = "2",
            title = "Post Liked",
            message = "Someone enjoyed your recent post about hyperfocus!",
            timestamp = "2 hours ago",
            type = NotificationType.LIKE
        ),
        NotificationItem(
            id = "3",
            title = "Quiet Mode Enabled",
            message = "Colors have been gently desaturated. Take a breath.",
            timestamp = "1 day ago",
            type = NotificationType.SYSTEM
        ),
        NotificationItem(
            id = "4",
            title = "Welcome Back!",
            message = "We missed you! See what's new in your network.",
            timestamp = "2 weeks ago",
            type = NotificationType.SYSTEM
        )
    )

    NeuroNetWorkingTitleTheme {
        NotificationsScreen(notifications = mockNotifications)
    }
}
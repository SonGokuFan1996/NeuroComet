package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private const val THERAPY_BOT_ID = "Therapy_Bot"
private const val THERAPY_BOT_AVATAR = "https://api.dicebear.com/7.x/bottts/svg?seed=TherapyBot&radius=50"

@Composable
fun BubblyPostCard(
    post: Post,
    isQuietMode: Boolean,
    isAutoVideoPlayback: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReplyClick: () -> Unit,
    onShare: () -> Unit
) {
    val likeIcon = if (post.isLikedByMe == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
    val likeColor = if (post.isLikedByMe == true) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant

    val cardShape = RoundedCornerShape(24.dp)

    // Vibrant Mode: subtle glass gradient
    val vibrantBackground = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    )

    val quietBackground = MaterialTheme.colorScheme.surface

    Card(
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isQuietMode) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = if (isQuietMode) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(if (isQuietMode) Brush.linearGradient(listOf(quietBackground, quietBackground)) else vibrantBackground)
                .padding(16.dp)
        ) {
            Column {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUrl = if (post.userId == THERAPY_BOT_ID) THERAPY_BOT_AVATAR
                    else post.userAvatar ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${post.userId ?: "User"}"

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.userId ?: "Anonymous",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.createdAt ?: "Just now",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Content
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Media (quiet mode hides heavy media)
                if (!isQuietMode && !post.imageUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(post.imageUrl).crossfade(true).build(),
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                // Footer actions
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ActionButton(likeIcon, post.likes.toString(), likeColor, onLike)
                        Spacer(Modifier.width(16.dp))
                        ActionButton(Icons.AutoMirrored.Filled.Chat, "Reply", MaterialTheme.colorScheme.onSurfaceVariant, onReplyClick)
                        Spacer(Modifier.width(16.dp))
                        ActionButton(Icons.Rounded.Share, null, MaterialTheme.colorScheme.onSurfaceVariant, onShare)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String?, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        if (label != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
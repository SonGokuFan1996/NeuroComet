package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

val THERAPY_BOT_AVATAR = "https://api.dicebear.com/7.x/bottts/svg?seed=TherapyBot&radius=50"

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
    val likeIcon = if (post.isLikedByMe) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
    val likeColor = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant

    // --- OVERHAUL: Glassmorphism vs High Contrast ---
    val cardShape = RoundedCornerShape(24.dp)

    // Vibrant Mode: Gradient Glass effect
    val vibrantBackground = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    )

    // Quiet Mode: Solid, flat color for zero visual noise
    val quietBackground = MaterialTheme.colorScheme.surface

    Card(
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isQuietMode) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), // We handle BG manually
        border = if (isQuietMode) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // Tighter padding
    ) {
        Box(
            modifier = Modifier
                .background(if (isQuietMode) Brush.linearGradient(listOf(quietBackground, quietBackground)) else vibrantBackground)
                .padding(16.dp)
        ) {
            Column {
                // --- Header ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUrl = if (post.userId == "Therapy_Bot") THERAPY_BOT_AVATAR else post.userAvatar
                        ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${post.userId}"

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.userId ?: "Anonymous",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (post.userId == "Therapy_Bot") {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Outlined.Verified, "Verified", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(
                            text = post.createdAt,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Context Menu
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.MoreHoriz, "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // --- Content ---
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15
                )

                // --- Media ---
                if (!isQuietMode) {
                    if (!post.imageUrl.isNullOrBlank()) {
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
                }

                // --- Footer (Tags & Actions) ---
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Action Buttons
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
fun ActionButton(icon: ImageVector, label: String?, tint: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        if (label != null) {
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}
package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kyilmaz.neuronetworkingtitle.ui.theme.QUIET_NEUTRAL_DARK_BG
import com.kyilmaz.neuronetworkingtitle.ui.theme.QUIET_NEUTRAL_LIGHT_BG
import com.kyilmaz.neuronetworkingtitle.ui.theme.BUBBLE_YELLOW
import kotlinx.coroutines.delay

val THERAPY_BOT_AVATAR = "https://api.dicebear.com/7.x/bottts/svg?seed=TherapyBot&radius=50"

@Composable
fun BubblyPostCard(
    post: Post,
    isQuietMode: Boolean,
    isAutoVideoPlayback: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReplyClick: () -> Unit,
    onShare: () -> Unit // ADDED onShare callback
) {
    val likeIcon = if (post.isLikedByMe) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder
    val likeTint = if (post.isLikedByMe) Color(0xFFE91E63) else LocalContentColor.current.copy(alpha = 0.8f)
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val quietNeutralBg = if (isDarkTheme) QUIET_NEUTRAL_DARK_BG else QUIET_NEUTRAL_LIGHT_BG
    val avatarPlaceholderColor = if (isQuietMode) quietNeutralBg else BUBBLE_YELLOW

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderPulseColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        targetValue = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "borderPulse"
    )

    // Bubbly Background Gradient for Light Mode
    val cardBackground = if (!isQuietMode && !isDarkTheme) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            )
        )
    } else {
        null // Use default container color
    }
    
    val containerColor = if(isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(32.dp), // Extra rounded corners for "bubbly" feel
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isQuietMode) 0.dp else 4.dp),
        border = if (!isQuietMode) BorderStroke(1.dp, borderPulseColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp) // Slight inset to emphasize floating/bubble look
            .clip(RoundedCornerShape(32.dp))
    ) {
        Column(
            Modifier
                .then(if (cardBackground != null) Modifier.background(cardBackground) else Modifier)
                .padding(20.dp)
        ) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                val avatarUrl = if (post.userId == "Therapy_Bot") THERAPY_BOT_AVATAR else post.userAvatar ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${post.userId}"
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(avatarPlaceholderColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            (if (post.userId != null && post.userId.length > 30) "User ${post.userId.take(4)}" else post.userId ?: "Anon"),
                            fontWeight = FontWeight.ExtraBold, // Bolder font
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        post.community ?: "General",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.MoreHoriz, "More", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            
            // --- CONTENT ---
            Spacer(Modifier.height(12.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)

            // --- MEDIA ---
            if (!isQuietMode) {
                if (post.videoUrl != null) {
                    Spacer(Modifier.height(16.dp))
                    VideoPlayer(videoUrl = post.videoUrl, shouldPlay = isAutoVideoPlayback)
                } else if (post.imageUrl != null) {
                    Spacer(Modifier.height(16.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(post.imageUrl).crossfade(true).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(24.dp))
                    )
                }
            }
            
            // --- TAGS & ACTIONS ---
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Text(post.tone ?: "/gen", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BouncyIcon(likeIcon, "Like", onLike, likeTint, isQuietMode, post.likes.toString())
                BouncyIcon(Icons.AutoMirrored.Filled.Chat, "Reply", onReplyClick, MaterialTheme.colorScheme.secondary, isQuietMode)
                BouncyIcon(Icons.Rounded.Share, "Share", onShare, MaterialTheme.colorScheme.tertiary, isQuietMode) // Wired up onShare
            }
        }
    }
}

@Composable
fun BouncyIcon(icon: ImageVector, description: String, onClick: () -> Unit, tint: Color, isQuietMode: Boolean, labelText: String? = null) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.7f else 1.0f, spring(dampingRatio = 0.4f, stiffness = 400f), label = "bounce")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { isPressed = true; onClick() }) {
        IconButton(onClick = { isPressed = true; onClick() }, modifier = Modifier.scale(scale)) {
            LaunchedEffect(isPressed) { if (isPressed) { delay(100); isPressed = false } }
            Icon(icon, description, tint = tint, modifier = Modifier.size(26.dp))
        }
        if (labelText != null) {
            Text(labelText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = tint)
        }
    }
}

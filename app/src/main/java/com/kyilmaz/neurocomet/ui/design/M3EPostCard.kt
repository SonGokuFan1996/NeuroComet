package com.kyilmaz.neurocomet.ui.design

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kyilmaz.neurocomet.Post
import com.kyilmaz.neurocomet.R
import com.kyilmaz.neurocomet.ui.components.NeuroLinkedText
import com.kyilmaz.neurocomet.ui.components.defaultNeuroLinkStyle

/**
 * M3E Post Card - Bubbly design matching Flutter version
 *
 * Features:
 * - 20dp rounded corners for bubbly feel
 * - Gradient avatar ring
 * - Following badge
 * - Emotional tone detection
 * - Animated like button
 * - Media carousel support
 * - @mention and #hashtag linking
 * - Staggered entry animation
 */

/**
 * Emotional tone detection for neurodivergent-friendly content awareness.
 */
enum class M3EEmotionalTone(
    val emoji: String,
    val labelResId: Int,
    val backgroundColor: Color,
    val textColor: Color,
    val showWarning: Boolean = false
) {
    NEUTRAL("💭", R.string.tone_neutral, Color(0xFF9E9E9E), Color(0xFF616161)),
    HAPPY("😊", R.string.tone_happy, Color(0xFF81C784), Color(0xFF2E7D32)),
    EXCITED("🎉", R.string.tone_excited, Color(0xFFFFD54F), Color(0xFFF57F17)),
    SAD("💙", R.string.tone_sad, Color(0xFF64B5F6), Color(0xFF1565C0), true),
    ANXIOUS("🫂", R.string.tone_anxious, Color(0xFFCE93D8), Color(0xFF7B1FA2), true),
    FRUSTRATED("😤", R.string.tone_frustrated, Color(0xFFFFAB91), Color(0xFFD84315), true),
    SUPPORTIVE("💜", R.string.tone_supportive, Color(0xFFB39DDB), Color(0xFF512DA8)),
    QUESTION("❓", R.string.tone_question, Color(0xFF80DEEA), Color(0xFF00838F)),
    CELEBRATION("✨", R.string.tone_celebration, Color(0xFFF48FB1), Color(0xFFC2185B)),
    INFORMATIVE("📚", R.string.tone_informative, Color(0xFF90CAF9), Color(0xFF1976D2))
}

/**
 * Detects the emotional tone of post content for neurodivergent users.
 */
fun detectM3EEmotionalTone(content: String): M3EEmotionalTone {
    val lowerContent = content.lowercase()

    return when {
        // Celebration patterns
        lowerContent.contains("congrat") ||
        lowerContent.contains("achieved") ||
        lowerContent.contains("finally did it") ||
        lowerContent.contains("so proud") ||
        content.contains("🎉") || content.contains("🎊") || content.contains("🥳") -> M3EEmotionalTone.CELEBRATION

        // Excited/happy patterns
        lowerContent.contains("so happy") ||
        lowerContent.contains("amazing") ||
        lowerContent.contains("love this") ||
        lowerContent.contains("best day") ||
        content.contains("😊") || content.contains("😄") || content.contains("❤️") -> M3EEmotionalTone.HAPPY

        // Excited patterns
        lowerContent.contains("can't wait") ||
        lowerContent.contains("so excited") ||
        lowerContent.contains("omg") ||
        content.contains("🔥") || content.contains("⚡") -> M3EEmotionalTone.EXCITED

        // Supportive patterns
        lowerContent.contains("you've got this") ||
        lowerContent.contains("proud of you") ||
        lowerContent.contains("here for you") ||
        lowerContent.contains("sending love") ||
        lowerContent.contains("you're not alone") -> M3EEmotionalTone.SUPPORTIVE

        // Question/help seeking patterns
        lowerContent.contains("does anyone") ||
        lowerContent.contains("how do i") ||
        lowerContent.contains("any tips") ||
        lowerContent.contains("help me") ||
        lowerContent.contains("advice") ||
        content.contains("?") && content.length < 200 -> M3EEmotionalTone.QUESTION

        // Informative patterns
        lowerContent.contains("did you know") ||
        lowerContent.contains("research shows") ||
        lowerContent.contains("fun fact") ||
        lowerContent.contains("psa") ||
        lowerContent.contains("reminder") -> M3EEmotionalTone.INFORMATIVE

        // Sad/emotional patterns (with warning)
        lowerContent.contains("struggling") ||
        lowerContent.contains("hard day") ||
        lowerContent.contains("feeling down") ||
        lowerContent.contains("crying") ||
        (lowerContent.contains("miss") && lowerContent.contains("so much")) ||
        content.contains("😢") || content.contains("😭") || content.contains("💔") -> M3EEmotionalTone.SAD

        // Anxious patterns (with warning)
        lowerContent.contains("anxious") ||
        lowerContent.contains("panic") ||
        lowerContent.contains("overwhelm") ||
        lowerContent.contains("can't cope") ||
        lowerContent.contains("sensory overload") -> M3EEmotionalTone.ANXIOUS

        // Frustrated/venting patterns (with warning)
        lowerContent.contains("rant") ||
        lowerContent.contains("so frustrated") ||
        lowerContent.contains("hate when") ||
        lowerContent.contains("ugh") ||
        lowerContent.contains("annoyed") ||
        content.contains("😤") || content.contains("🙄") -> M3EEmotionalTone.FRUSTRATED

        else -> M3EEmotionalTone.NEUTRAL
    }
}

/**
 * M3E Bubbly Post Card Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3EPostCard(
    post: Post,
    modifier: Modifier = Modifier,
    animationIndex: Int = 0,
    reduceMotion: Boolean = false,
    isFollowing: Boolean = false,
    isQuietMode: Boolean = false,
    onLike: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    onSave: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onReport: () -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    onHashtagClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showMenu by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(post.isLikedByMe ?: false) }
    var likeCount by remember { mutableIntStateOf(post.likes) }
    var isSaved by remember { mutableStateOf(false) }

    // Animation states
    var visible by remember { mutableStateOf(reduceMotion) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            kotlinx.coroutines.delay(M3EAnimations.staggeredDelay(animationIndex).toLong())
            visible = true
        }
    }

    // Detect emotional tone
    val emotionalTone = remember(post.content) { detectM3EEmotionalTone(post.content) }

    val cardContent = @Composable {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = M3EDesignSystem.Spacing.sm, vertical = M3EDesignSystem.Spacing.xs)
                .animateContentSize(),
            shape = M3EDesignSystem.Shapes.BubblyCard,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isQuietMode) 0.dp else M3EDesignSystem.Elevation.card
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = if (isQuietMode) {
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            } else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(M3EDesignSystem.Spacing.md)
            ) {
                // Header Row
                PostCardHeader(
                    post = post,
                    isFollowing = isFollowing,
                    showMenu = showMenu,
                    onMenuClick = { showMenu = true },
                    onMenuDismiss = { showMenu = false },
                    onProfileClick = onProfileClick,
                    onSave = {
                        isSaved = !isSaved
                        onSave()
                    },
                    onCopyLink = {
                        clipboardManager.setText(AnnotatedString("https://neurocomet.app/post/${post.id}"))
                        Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = onShare,
                    onDelete = onDelete,
                    onReport = onReport,
                    isSaved = isSaved
                )

                Spacer(modifier = Modifier.height(M3EDesignSystem.Spacing.sm))

                // Content
                NeuroLinkedText(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    linkStyle = defaultNeuroLinkStyle(),
                    onLinkClick = { link ->
                        when (link.type) {
                            com.kyilmaz.neurocomet.ui.components.LinkType.MENTION ->
                                onMentionClick(link.text.removePrefix("@"))
                            com.kyilmaz.neurocomet.ui.components.LinkType.HASHTAG ->
                                onHashtagClick(link.text.removePrefix("#"))
                            else -> { /* Default handling */ }
                        }
                    }
                )

                // Media Carousel
                if (!isQuietMode && !post.imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(M3EDesignSystem.Spacing.sm))
                    PostMediaCarousel(
                        mediaUrls = listOf(post.imageUrl!!)
                    )
                }

                Spacer(modifier = Modifier.height(M3EDesignSystem.Spacing.sm))

                // Action Row
                PostCardActions(
                    isLiked = isLiked,
                    likeCount = likeCount,
                    commentCount = post.comments,
                    onLike = {
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        onLike()
                    },
                    onComment = onComment,
                    onShare = onShare,
                    isSaved = isSaved,
                    onSave = {
                        isSaved = !isSaved
                        onSave()
                    }
                )

                // Emotional Tone Tag
                if (emotionalTone != M3EEmotionalTone.NEUTRAL) {
                    Spacer(modifier = Modifier.height(M3EDesignSystem.Spacing.xs))
                    M3EEmotionalToneTag(
                        emoji = emotionalTone.emoji,
                        label = stringResource(emotionalTone.labelResId),
                        backgroundColor = emotionalTone.backgroundColor,
                        textColor = emotionalTone.textColor,
                        showWarning = emotionalTone.showWarning
                    )
                }
            }
        }
    }

    // Apply animation wrapper
    if (reduceMotion) {
        cardContent()
    } else {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(M3EDesignSystem.AnimationDuration.cardEntry)
            ) + slideInVertically(
                initialOffsetY = { it / 20 },
                animationSpec = tween(M3EDesignSystem.AnimationDuration.cardEntry)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(M3EDesignSystem.AnimationDuration.cardEntry)
            )
        ) {
            cardContent()
        }
    }
}

/**
 * Post card header with avatar, username, and menu.
 */
@Composable
private fun PostCardHeader(
    post: Post,
    isFollowing: Boolean,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onProfileClick: () -> Unit,
    onSave: () -> Unit,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    isSaved: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with gradient ring
        M3EAvatar(
            imageUrl = post.userAvatar ?: "https://i.pravatar.cc/150?u=${post.userId}",
            size = M3EDesignSystem.AvatarSize.postCard,
            showGradientRing = true,
            ringWidth = 2.dp,
            onClick = onProfileClick
        )

        Spacer(modifier = Modifier.width(M3EDesignSystem.Spacing.sm))

        // User info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.userId ?: "Anonymous",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onProfileClick),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isFollowing) {
                    Spacer(modifier = Modifier.width(M3EDesignSystem.Spacing.xs))
                    M3EFollowingChip()
                }
            }

            Text(
                text = post.createdAt ?: "Just now",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // More menu
        Box {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PostDropdownMenu(
                expanded = showMenu,
                onDismiss = onMenuDismiss,
                isSaved = isSaved,
                onSave = {
                    onSave()
                    onMenuDismiss()
                },
                onCopyLink = {
                    onCopyLink()
                    onMenuDismiss()
                },
                onShare = {
                    onShare()
                    onMenuDismiss()
                },
                onDelete = {
                    onDelete()
                    onMenuDismiss()
                },
                onReport = {
                    onReport()
                    onMenuDismiss()
                }
            )
        }
    }
}

/**
 * Dropdown menu for post actions.
 */
@Composable
private fun PostDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit,
    onCopyLink: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(if (isSaved) "Unsave" else "Save") },
            onClick = onSave,
            leadingIcon = {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Copy link") },
            onClick = onCopyLink,
            leadingIcon = {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Share") },
            onClick = onShare,
            leadingIcon = {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Report") },
            onClick = onReport,
            leadingIcon = {
                Icon(Icons.Default.Flag, contentDescription = null)
            }
        )
    }
}

/**
 * Post action buttons row.
 */
@Composable
private fun PostCardActions(
    isLiked: Boolean,
    likeCount: Int,
    commentCount: Int,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(M3EDesignSystem.Spacing.md)
        ) {
            // Like button with animation
            M3EAnimatedLikeButton(
                isLiked = isLiked,
                likeCount = likeCount,
                onClick = onLike
            )

            // Comment button
            M3EIconTextButton(
                icon = Icons.AutoMirrored.Outlined.Comment,
                text = if (commentCount > 0) formatCount(commentCount) else null,
                onClick = onComment
            )

            // Share button
            M3EIconTextButton(
                icon = Icons.Outlined.Share,
                text = null,
                onClick = onShare
            )
        }

        // Bookmark button
        IconButton(onClick = onSave) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "Unsave" else "Save",
                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Media carousel for post images.
 */
@Composable
private fun PostMediaCarousel(
    mediaUrls: List<String>,
    modifier: Modifier = Modifier
) {
    if (mediaUrls.isEmpty()) return

    if (mediaUrls.size == 1) {
        // Single image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaUrls[0])
                .crossfade(true)
                .build(),
            contentDescription = "Post image",
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(M3EDesignSystem.Shapes.MediumShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
    } else {
        // Multiple images - carousel
        val pagerState = rememberPagerState { mediaUrls.size }

        Column(modifier = modifier) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mediaUrls[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post image ${page + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(M3EDesignSystem.Shapes.MediumShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }

            // Page indicators
            if (mediaUrls.size > 1) {
                Spacer(modifier = Modifier.height(M3EDesignSystem.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(mediaUrls.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format large numbers with K/M suffix.
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}


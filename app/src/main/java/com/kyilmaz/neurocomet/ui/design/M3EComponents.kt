package com.kyilmaz.neurocomet.ui.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * M3E Component Library for NeuroComet Android
 *
 * Reusable UI components implementing the M3E design language with:
 * - Bubbly, rounded aesthetics
 * - Gradient accents
 * - Smooth animations
 * - Neurodivergent-friendly design
 */

// ============================================================================
// AVATAR COMPONENTS
// ============================================================================

/**
 * M3E Avatar with optional gradient ring.
 *
 * @param imageUrl URL of the avatar image
 * @param size Size of the avatar
 * @param showGradientRing Whether to show a gradient ring around the avatar
 * @param ringWidth Width of the gradient ring
 * @param onClick Callback when avatar is clicked
 * @param contentDescription Accessibility description
 */
@Composable
fun M3EAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = M3EDesignSystem.AvatarSize.md,
    showGradientRing: Boolean = false,
    ringWidth: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val gradientBrush = if (showGradientRing) {
        M3EColors.avatarGradientBrush()
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    val ringSize = if (showGradientRing) ringWidth else 0.dp

    Box(
        modifier = modifier
            .size(size + ringSize * 2)
            .then(
                if (showGradientRing) {
                    Modifier.background(gradientBrush, CircleShape)
                } else {
                    Modifier
                }
            )
            .padding(ringSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Story avatar with animated ring for unseen stories.
 */
@Composable
fun M3EStoryAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = M3EDesignSystem.AvatarSize.story,
    hasUnseenStory: Boolean = true,
    isAddStory: Boolean = false,
    userName: String = "",
    onClick: (() -> Unit)? = null
) {
    val rotation by M3EAnimations.rainbowRotation()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size + 6.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Animated ring for unseen stories
            if (hasUnseenStory) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    rotate(rotation) {
                        drawCircle(
                            brush = Brush.sweepGradient(M3EColors.vibrantRainbowColors),
                            radius = size.toPx() / 2 + 3.dp.toPx(),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            } else {
                // Static ring for seen stories
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.3f),
                        radius = size.toPx() / 2 + 3.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Avatar image
            M3EAvatar(
                imageUrl = imageUrl,
                size = size,
                showGradientRing = false
            )

            // Add story badge
            if (isAddStory) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add story",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (userName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isAddStory) "Your story" else userName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(size)
            )
        }
    }
}

// ============================================================================
// CARD COMPONENTS
// ============================================================================

/**
 * M3E Bubbly Card with optional gradient background.
 * Used for post cards, content cards, etc.
 */
@Composable
fun M3EBubblyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = M3EDesignSystem.Elevation.card,
    useGradient: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = M3EDesignSystem.Shapes.BubblyCard
    val backgroundColor = if (useGradient) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        if (useGradient) {
            Column(
                modifier = Modifier
                    .background(M3EGradients.cardGradient())
                    .padding(M3EDesignSystem.Spacing.md),
                content = content
            )
        } else {
            Column(
                modifier = Modifier.padding(M3EDesignSystem.Spacing.md),
                content = content
            )
        }
    }
}

/**
 * M3E Surface Card for general content containers.
 */
@Composable
fun M3ESurfaceCard(
    modifier: Modifier = Modifier,
    shape: Shape = M3EDesignSystem.Shapes.MediumShape,
    elevation: Dp = M3EDesignSystem.Elevation.level1,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        shadowElevation = elevation,
        color = containerColor,
        content = { Box(content = content) }
    )
}

// ============================================================================
// CHIP COMPONENTS
// ============================================================================

/**
 * M3E Filter Chip with pill shape.
 */
@Composable
fun M3EFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        shape = M3EDesignSystem.Shapes.Chip,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * M3E Badge Chip for displaying small pieces of info.
 */
@Composable
fun M3EBadgeChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        modifier = modifier,
        shape = M3EDesignSystem.Shapes.ExtraSmallShape,
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = M3EDesignSystem.Spacing.xs,
                vertical = M3EDesignSystem.Spacing.xxs
            ),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * M3E Following chip displayed next to usernames.
 */
@Composable
fun M3EFollowingChip(
    modifier: Modifier = Modifier
) {
    M3EBadgeChip(
        text = "Following",
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

// ============================================================================
// BUTTON COMPONENTS
// ============================================================================

/**
 * M3E Animated Like Button with heart animation.
 */
@Composable
fun M3EAnimatedLikeButton(
    isLiked: Boolean,
    likeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (animating) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { animating = false },
        label = "likeScale"
    )

    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                animating = true
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isLiked) "Unlike" else "Like",
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
            tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (likeCount > 0) {
            Text(
                text = formatCount(likeCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * M3E Icon Text Button for post actions.
 */
@Composable
fun M3EIconTextButton(
    icon: ImageVector,
    text: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier
            .clip(M3EDesignSystem.Shapes.SmallShape)
            .clickable(onClick = onClick)
            .padding(M3EDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint
        )

        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = tint
            )
        }
    }
}

// ============================================================================
// LOADING COMPONENTS
// ============================================================================

/**
 * M3E Loading indicator with breathing animation.
 */
@Composable
fun M3ELoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp
) {
    val scale by M3EAnimations.breathingScale()

    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .scale(scale),
        strokeWidth = strokeWidth,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * M3E Shimmer loading effect.
 */
@Composable
fun M3EShimmer(
    modifier: Modifier = Modifier,
    shape: Shape = M3EDesignSystem.Shapes.SmallShape
) {
    val shimmerOffset by M3EAnimations.shimmerOffset()

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerOffset * 300f, 0f),
        end = Offset((shimmerOffset + 1) * 300f, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// ============================================================================
// EMPTY STATE COMPONENTS
// ============================================================================

/**
 * M3E Empty State display.
 */
@Composable
fun M3EEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(M3EDesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(M3EDesignSystem.Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        action?.invoke()
    }
}

// ============================================================================
// EMOTIONAL TONE TAG
// ============================================================================

/**
 * M3E Emotional Tone Tag for post content awareness.
 */
@Composable
fun M3EEmotionalToneTag(
    emoji: String,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    showWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(M3EDesignSystem.Shapes.ExtraSmallShape)
            .background(backgroundColor.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )

        if (showWarning) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Sensitive content",
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
        }
    }
}

// ============================================================================
// DIVIDER COMPONENTS
// ============================================================================

/**
 * M3E Divider with avatar alignment (for notification lists, etc.).
 */
@Composable
fun M3EAlignedDivider(
    modifier: Modifier = Modifier,
    startPadding: Dp = 76.dp // Aligned after 44dp avatar + 16dp padding + 16dp gap
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startPadding),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

// ============================================================================
// UNREAD INDICATOR
// ============================================================================

/**
 * M3E Unread indicator dot.
 */
@Composable
fun M3EUnreadDot(
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
    )
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

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

// ============================================================================
// ANIMATED ENTRY WRAPPER
// ============================================================================

/**
 * Wrapper for staggered list item animations.
 */
@Composable
fun M3EAnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            kotlinx.coroutines.delay(M3EAnimations.staggeredDelay(index).toLong())
        }
        visible = true
    }

    if (reduceMotion) {
        Box(modifier = modifier) {
            content()
        }
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
            ),
            modifier = modifier
        ) {
            content()
        }
    }
}

// ============================================================================
// RAINBOW INFINITY SYMBOL
// ============================================================================

/**
 * Animated rainbow infinity symbol - the NeuroComet brand mark.
 */
@Composable
fun M3ERainbowInfinitySymbol(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    animate: Boolean = true
) {
    val rotation by M3EAnimations.rainbowRotation()
    val breatheScale by M3EAnimations.breathingScale()

    val effectiveScale = if (animate) breatheScale else 1f
    val effectiveRotation = if (animate) rotation else 0f

    Box(
        modifier = modifier
            .size(size)
            .scale(effectiveScale),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = size.toPx() * 0.1f
            val infinityPath = Path().apply {
                // Left loop
                val cx1 = size.toPx() * 0.25f
                val cy = size.toPx() * 0.5f
                val r = size.toPx() * 0.2f

                // Right loop
                val cx2 = size.toPx() * 0.75f

                // Draw figure-8 path
                moveTo(size.toPx() * 0.5f, cy)
                cubicTo(
                    cx1 + r, cy - r * 1.5f,
                    cx1 - r, cy - r * 1.5f,
                    cx1, cy
                )
                cubicTo(
                    cx1 - r, cy + r * 1.5f,
                    cx1 + r, cy + r * 1.5f,
                    size.toPx() * 0.5f, cy
                )
                cubicTo(
                    cx2 - r, cy - r * 1.5f,
                    cx2 + r, cy - r * 1.5f,
                    cx2, cy
                )
                cubicTo(
                    cx2 + r, cy + r * 1.5f,
                    cx2 - r, cy + r * 1.5f,
                    size.toPx() * 0.5f, cy
                )
            }

            rotate(effectiveRotation) {
                drawPath(
                    path = infinityPath,
                    brush = Brush.sweepGradient(M3EColors.rainbowColors),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}


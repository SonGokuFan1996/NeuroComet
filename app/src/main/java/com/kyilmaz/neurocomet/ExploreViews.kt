package com.kyilmaz.neurocomet

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Explore Page View Types
 *
 * - GRID: Instagram-style grid layout
 * - COMPACT: Small cards, more content visible
 * - STANDARD: Default balanced view
 * - LARGE_CARDS: Featured large cards for accessibility
 */
enum class ExploreViewType(
    val icon: ImageVector,
    val label: String,
    val description: String
) {
    GRID(Icons.Filled.GridView, "Grid", "Compact grid layout"),
    COMPACT(Icons.AutoMirrored.Filled.ViewList, "Compact", "Small cards, more visible"),
    STANDARD(Icons.Filled.ViewAgenda, "Standard", "Balanced card layout"),
    LARGE_CARDS(Icons.Filled.ViewCarousel, "Large", "Big cards, accessibility friendly")
}

/**
 * Data class representing an explore topic for category views.
 */
data class ExploreTopic(
    val id: String,
    val nameRes: Int,
    val descriptionRes: Int,
    val emoji: String,
    val gradientColors: List<Color> = emptyList(),
    val memberCount: Int = 0,
    val postCount: Int = 0,
    val isPopular: Boolean = false,
    val isNew: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
// VIEW TYPE SELECTOR
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreViewTypeSelector(
    currentViewType: ExploreViewType,
    onViewTypeChange: (ExploreViewType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "View",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ExploreViewType.entries.forEach { viewType ->
                val isSelected = viewType == currentViewType

                IconButton(
                    onClick = { onViewTypeChange(viewType) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = viewType.icon,
                        contentDescription = viewType.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// GRID VIEW - Instagram-style compact grid
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreGridView(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(posts, key = { it.id ?: it.hashCode() }) { post ->
            GridPostItem(
                post = post,
                onClick = { onPostClick(post) }
            )
        }
    }
}

@Composable
private fun GridPostItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Image or placeholder
        val mediaItems = post.getAllMedia()
        if (mediaItems.isNotEmpty()) {
            AsyncImage(
                model = mediaItems.first().url,
                contentDescription = stringResource(R.string.cd_post_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Multi-media indicator
            if (mediaItems.size > 1) {
                Icon(
                    imageVector = Icons.Filled.Collections,
                    contentDescription = "${mediaItems.size} images",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp)
                )
            }

            // Video indicator
            if (mediaItems.any { it.type == MediaType.VIDEO }) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        } else {
            // Text-only post preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.content.take(50),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Overlay with stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = post.likes.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = post.comments.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STANDARD VIEW - Balanced card layout (default)
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreStandardView(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onLikePost: (Long) -> Unit,
    onSharePost: (Context, Post) -> Unit,
    onCommentPost: (Post) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts, key = { it.id ?: it.hashCode() }) { post ->
            StandardPostCard(
                post = post,
                onClick = { onPostClick(post) },
                onLike = { post.id?.let(onLikePost) },
                onShare = { onSharePost(context, post) },
                onComment = { onCommentPost(post) },
                onProfileClick = { post.userId?.let(onProfileClick) }
            )
        }
    }
}

@Composable
private fun StandardPostCard(
    post: Post,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onComment: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header - Medium sized
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medium avatar (44dp)
                AsyncImage(
                    model = post.userAvatar ?: avatarUrl(post.userId ?: "unknown"),
                    contentDescription = stringResource(R.string.cd_user_avatar),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userId ?: "Unknown User",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content - Show more lines than compact
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Media - Medium sized
            val mediaItems = post.getAllMedia()
            if (mediaItems.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                if (mediaItems.size == 1) {
                    AsyncImage(
                        model = mediaItems.first().url,
                        contentDescription = stringResource(R.string.cd_post_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    // Multiple images - horizontal scroll
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        items(mediaItems.take(4)) { media ->
                            AsyncImage(
                                model = media.url,
                                contentDescription = stringResource(R.string.cd_post_image),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Actions - Standard layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onLike)
                ) {
                    Icon(
                        if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.action_like),
                        tint = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.likes}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onComment)
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.action_comment),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onShare)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.cd_share),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.shares}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Bookmark
                Icon(
                    Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.action_bookmark),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPACT VIEW - Small cards, more content visible
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreCompactView(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onLikePost: (Long) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts, key = { it.id ?: it.hashCode() }) { post ->
            CompactPostCard(
                post = post,
                onClick = { onPostClick(post) },
                onLike = { post.id?.let(onLikePost) },
                onProfileClick = { post.userId?.let(onProfileClick) }
            )
        }
    }
}

@Composable
private fun CompactPostCard(
    post: Post,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            AsyncImage(
                model = post.userAvatar ?: avatarUrl(post.userId ?: "unknown"),
                contentDescription = stringResource(R.string.cd_user_avatar),
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.userId ?: "Unknown",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "• ${post.timeAgo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // Compact actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onLike)
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Filled.Favorite
                                         else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.action_like),
                            tint = if (post.isLikedByMe) Color.Red
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${post.likes}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.comments_title),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${post.comments}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Thumbnail if has media
            val mediaItems = post.getAllMedia()
            if (mediaItems.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = mediaItems.first().url,
                        contentDescription = "Post thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (mediaItems.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${mediaItems.size - 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LARGE CARD VIEW - Big cards for accessibility
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreLargeCardView(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onLikePost: (Long) -> Unit,
    onSharePost: (Context, Post) -> Unit,
    onCommentPost: (Post) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(posts, key = { it.id ?: it.hashCode() }) { post ->
            LargePostCard(
                post = post,
                onClick = { onPostClick(post) },
                onLike = { post.id?.let(onLikePost) },
                onShare = { onSharePost(context, post) },
                onComment = { onCommentPost(post) },
                onProfileClick = { post.userId?.let(onProfileClick) }
            )
        }
    }
}

@Composable
private fun LargePostCard(
    post: Post,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onComment: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header - Large and clear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large avatar
                AsyncImage(
                    model = post.userAvatar ?: avatarUrl(post.userId ?: "unknown"),
                    contentDescription = stringResource(R.string.cd_user_avatar),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onProfileClick),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.userId ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Media - Full width with carousel for multiple
            val mediaItems = post.getAllMedia()
            if (mediaItems.isNotEmpty()) {
                if (mediaItems.size == 1) {
                    // Single image - large display
                    AsyncImage(
                        model = mediaItems.first().url,
                        contentDescription = stringResource(R.string.cd_post_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                } else {
                    // Multiple images - horizontal carousel
                    Column {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(mediaItems.take(Post.MAX_MEDIA_ITEMS)) { media ->
                                AsyncImage(
                                    model = media.url,
                                    contentDescription = stringResource(R.string.cd_post_image),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(280.dp)
                                        .height(280.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        // Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${mediaItems.size} photos",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Content - Larger text
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                lineHeight = 24.sp
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Large action buttons - Accessibility friendly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LargeActionButton(
                    icon = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "${post.likes} Likes",
                    onClick = onLike,
                    tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )

                LargeActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "${post.comments} Comments",
                    onClick = onComment
                )

                LargeActionButton(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
private fun LargeActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// EXPLORE CATEGORY LARGE CARDS
// ═══════════════════════════════════════════════════════════════

@Composable
fun ExploreCategoryLargeCard(
    topic: ExploreTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = topic.gradientColors.ifEmpty {
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    )
                )
        ) {
            // Large decorative emoji
            Text(
                text = topic.emoji,
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .offset(x = 20.dp, y = (-20).dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (topic.isPopular) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🔥 Popular",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    if (topic.isNew) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "✨ New",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = topic.emoji,
                            fontSize = 32.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(topic.nameRes),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = stringResource(topic.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = formatCountCompact(topic.memberCount),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${formatCountCompact(topic.postCount)} posts",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════
// LOCAL HELPER FUNCTION
// ═══════════════════════════════════════════════════════════════

/**
 * Format large numbers compactly (e.g., 12.5k, 1.2M)
 * Local helper to avoid conflicts with other formatCount functions
 */
private fun formatCountCompact(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}

// ═══════════════════════════════════════════════════════════════
// ENHANCED MOCK DATA FOR EXPLORE
// ═══════════════════════════════════════════════════════════════

/**
 * Generate mock posts with multi-media support for testing
 */
fun generateMockExplorePostsWithMedia(): List<Post> {
    return listOf(
        // Post with multiple images (carousel)
        Post(
            id = 1001L,
            createdAt = "2026-01-03T10:00:00Z",
            content = "Just finished setting up my new sensory-friendly workspace! Swipe to see all the details 🎨✨ #ADHD #ProductivityHacks",
            userId = "HyperFocusCode",
            likes = 234,
            comments = 45,
            shares = 12,
            isLikedByMe = false,
            userAvatar = avatarUrl("hyperfocuscode"),
            mediaItems = listOf(
                MediaItem("https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800", MediaType.IMAGE),
                MediaItem("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800", MediaType.IMAGE),
                MediaItem("https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=800", MediaType.IMAGE),
                MediaItem("https://images.unsplash.com/photo-1593062096033-9a26b09da705?w=800", MediaType.IMAGE)
            )
        ),
        // Post with single image
        Post(
            id = 1002L,
            createdAt = "2026-01-03T09:30:00Z",
            content = "Today's stim toy haul! The texture on the blue one is *chef's kiss* 💙",
            userId = "SensorySeeker",
            likes = 189,
            comments = 32,
            shares = 8,
            isLikedByMe = true,
            userAvatar = avatarUrl("sensoryseeker"),
            mediaItems = listOf(
                MediaItem("https://images.unsplash.com/photo-1612538498456-e861df91d4d0?w=800", MediaType.IMAGE)
            )
        ),
        // Text-only post
        Post(
            id = 1003L,
            createdAt = "2026-01-03T08:45:00Z",
            content = "Reminder: Your brain isn't broken, it just works differently. And that's okay. 🧠💜 #NeurodiversityAcceptance",
            userId = "NeuroNaut",
            likes = 567,
            comments = 78,
            shares = 145,
            isLikedByMe = false,
            userAvatar = avatarUrl("neuronaut")
        ),
        // Post with many images (Instagram-style)
        Post(
            id = 1004L,
            createdAt = "2026-01-02T22:00:00Z",
            content = "My special interest journey through 2025! Every photo represents a hyperfocus phase 📸",
            userId = "DinoLover99",
            likes = 412,
            comments = 89,
            shares = 34,
            isLikedByMe = true,
            userAvatar = avatarUrl("dinolover99"),
            mediaItems = listOf(
                "photo-1519389950473-47ba0277781c",
                "photo-1514905552197-0610a4d8fd73",
                "photo-1605367705882-e1e4a6a72f2b",
                "photo-1563281746-48bb472c5e64",
                "photo-1544197150-b99a580bb7a8",
                "photo-1569091791842-7cfb64e04797",
                "photo-1535338454528-1b44e3b4cf42",
                "photo-1524492926121-16fc4b98bcf6",
                "photo-1580894742597-87bc8789db3d",
                "photo-1509281373149-e957c6296406"
            ).map { id ->
                MediaItem("https://images.unsplash.com/$id?w=600", MediaType.IMAGE)
            }
        ),
        // Post with video
        Post(
            id = 1005L,
            createdAt = "2026-01-02T18:30:00Z",
            content = "Made a quick video about my morning routine with ADHD. Hope it helps someone! 🌅",
            userId = "RainbowNerd",
            likes = 298,
            comments = 56,
            shares = 23,
            isLikedByMe = false,
            userAvatar = avatarUrl("rainbownerd"),
            mediaItems = listOf(
                MediaItem(
                    url = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
                    type = MediaType.VIDEO,
                    thumbnailUrl = "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=800"
                )
            )
        ),
        // Post with mixed media (images and video)
        Post(
            id = 1006L,
            createdAt = "2026-01-02T14:15:00Z",
            content = "Art therapy session today! Here's my process from sketch to final piece 🎨",
            userId = "LesbianLuna",
            likes = 345,
            comments = 67,
            shares = 28,
            isLikedByMe = false,
            userAvatar = avatarUrl("lesbianluna"),
            mediaItems = listOf(
                MediaItem("https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800", MediaType.IMAGE),
                MediaItem("https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=800", MediaType.IMAGE),
                MediaItem(
                    url = "https://example.com/timelapse.mp4",
                    type = MediaType.VIDEO,
                    thumbnailUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800"
                ),
                MediaItem("https://images.unsplash.com/photo-1547891654-e66ed7ebb968?w=800", MediaType.IMAGE)
            )
        ),
        // Post with maximum images (20)
        Post(
            id = 1007L,
            createdAt = "2026-01-01T20:00:00Z",
            content = "My entire sticker collection! Took forever to photograph them all 😅 #SpecialInterest #Collection",
            userId = "TransTechie",
            likes = 523,
            comments = 98,
            shares = 45,
            isLikedByMe = true,
            userAvatar = avatarUrl("transtechie"),
            mediaItems = listOf(
                "photo-1558618666-fcd25c85cd64",
                "photo-1513542789411-b6a5d4f31634",
                "photo-1534670007418-fbb7f6cf32c3",
                "photo-1504805572947-34fad45aed93",
                "photo-1531346878377-a5be20888e57",
                "photo-1509281373149-e957c6296406",
                "photo-1487611459768-bd414656ea10",
                "photo-1552862750-746b8f9e7240",
                "photo-1526925539354-3adb5b94e4ae",
                "photo-1498050108023-c5249f4df085",
                "photo-1542831371-29b0f74f9713",
                "photo-1515879218367-8fd44bd4ec0e",
                "photo-1461749280684-dccba630e2f6",
                "photo-1517694712202-14dd9538aa97",
                "photo-1523800503107-5bc3ba2a6f81",
                "photo-1519389950473-47ba0277781c",
                "photo-1550063873-ab792950096b",
                "photo-1531297484001-80022131f5a1",
                "photo-1544197150-b99a580bb7a8",
                "photo-1485988412941-77a35537dae4"
            ).map { id ->
                MediaItem("https://images.unsplash.com/$id?w=400", MediaType.IMAGE)
            }
        )
    )
}

// ═══════════════════════════════════════════════════════════════
// TOPIC DETAIL SCREEN
// ═══════════════════════════════════════════════════════════════

/**
 * Topic detail screen showing posts and info for a specific topic/category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = topicName.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.topic_detail_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.topic_detail_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Topic info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "#$topicName",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.topic_detail_description, topicName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.topic_detail_recent_posts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Empty state — real topic posts are backend-driven and not yet wired in here.
            // We deliberately avoid rendering English-only sample posts so translated
            // locales never see broken/untranslated content.
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.topic_detail_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.topic_detail_empty_subtitle, topicName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

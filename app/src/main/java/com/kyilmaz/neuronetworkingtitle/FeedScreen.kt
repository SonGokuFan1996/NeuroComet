@file:Suppress(
    "UnusedImport",
    "AssignmentToStateVariable",
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead"
)

package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage


@Composable
private fun NeuroNetLogo(modifier: Modifier = Modifier) {
    // Simple, dependency-free logo header.
    // If you later add a real brand asset, swap this composable to show it.
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun FeedScreen(
    posts: List<Post>,
    stories: List<Story>,
    currentUser: User,
    onLikePost: (Long) -> Unit,
    onReplyPost: (Post) -> Unit,
    onSharePost: (Context, Post) -> Unit,
    onAddPost: (String, String, String?, String?) -> Unit,
    onDeletePost: (Long) -> Unit,
    @Suppress("unused") onProfileClick: () -> Unit,
    onViewStory: (Story) -> Unit,
    onAddStory: (String, Long) -> Unit,
    @Suppress("unused") isPremium: Boolean,
    @Suppress("unused") onUpgradeClick: () -> Unit,
    isMockInterfaceEnabled: Boolean,
    modifier: Modifier = Modifier,
    safetyState: SafetyState = SafetyState()
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) } // New state for story dialog

    Column(modifier = modifier.fillMaxSize()) {
        // App Bar / Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { showCreatePostDialog = true }) {
                Icon(Icons.Default.Create, contentDescription = stringResource(R.string.create_post_title))
            }
        }

        // Horizontal divider
        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp), // Padding for the FAB and bottom bar
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stories Row at the top
            item { 
                StoriesRow(
                    stories = stories,
                    currentUser = currentUser,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    onViewStory = onViewStory,
                    onAddStoryClick = { showCreateStoryDialog = true } // Pass callback to open story dialog
                )
            }

            // Divider before feed starts
            item { HorizontalDivider() }

            // Feed Posts
            items(posts, key = { it.id ?: it.hashCode() }) { post ->
                BubblyPostCard(
                    post = post,
                    onLike = { post.id?.let(onLikePost) },
                    onDelete = { post.id?.let(onDeletePost) },
                    onReplyPost = { onReplyPost(post) },
                    onShare = onSharePost,
                    isMockInterfaceEnabled = isMockInterfaceEnabled, // Passed down
                    safetyState = safetyState
                )
            }
        }
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    run {
        // In kids mode, disable creating posts/stories unless app is in mock mode.
        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onPost = { content, tone, imageUrl, videoUrl ->
                    onAddPost(content, tone, imageUrl, videoUrl)
                    showCreatePostDialog = false
                },
                isPremium = isPremium,
                safetyState = safetyState
            )
        }

        if (showCreateStoryDialog) {
            CreateStoryDialog(
                onDismiss = { showCreateStoryDialog = false },
                onPost = { imageUrl, seconds ->
                    onAddStory(imageUrl, seconds)
                    showCreateStoryDialog = false
                },
                safetyState = safetyState
            )
        }
    }
}

@Composable
fun BubblyPostCard(
    post: Post,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReplyPost: () -> Unit,
    onShare: (Context, Post) -> Unit,
    isMockInterfaceEnabled: Boolean, // Added flag
    safetyState: SafetyState = SafetyState()
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val context = LocalContext.current
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                
                val avatarUrl = if (isMockInterfaceEnabled) {
                    post.userAvatar ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${post.userId}"
                } else {
                    // In real mode, use the provided avatar or treat null as a missing avatar (which we'll handle below)
                    post.userAvatar
                }

                if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                    // Show generic icon when mock is off and no real avatar is available
                    Icon(
                        Icons.Default.AccountCircle, 
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    // Use AsyncImage for all other cases (mock or real avatar available)
                    AsyncImage(
                        model = avatarUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${post.userId}", // Fallback to dicebear only if mock is on
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                }

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.userId ?: stringResource(R.string.unknown_user_id), fontWeight = FontWeight.Bold)
                    Text(post.timeAgo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_content_description))
                }
            }
            Spacer(Modifier.height(16.dp))

            val shouldHide = safetyState.isKidsMode && ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)
            if (shouldHide) {
                Text(
                    text = "Content hidden for kids mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val textToShow = if (safetyState.isKidsMode) {
                    ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel)
                } else post.content
                Text(textToShow)
            }

            // Media Display logic updated for video/image support
            // In UNDER_13 mode, hide media previews by default.
            val showMedia = !safetyState.isKidsMode

            if (showMedia && post.videoUrl != null) {
                Spacer(Modifier.height(16.dp))
                VideoPlayerView(
                    videoUrl = post.videoUrl,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardDefaults.shape)
                )
            } else if (showMedia && post.imageUrl != null) {
                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = stringResource(R.string.post_image_content_description),
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardDefaults.shape),
                    contentScale = ContentScale.Crop
                )
            }
            // End Media Display logic

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onShare(context, post) }) { Icon(Icons.Default.Share, stringResource(R.string.share_button_content_description)) }
                IconButton(onClick = onReplyPost) { Icon(Icons.AutoMirrored.Outlined.Comment, stringResource(R.string.comment_button_content_description)) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onLike) {
                    Icon(
                        if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        stringResource(R.string.like_button_content_description),
                        tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(post.likes.toString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun StoriesRow(stories: List<Story>, currentUser: User, isMockInterfaceEnabled: Boolean, onViewStory: (Story) -> Unit, onAddStoryClick: () -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        // "Add Story" item - Placeholder for current user to add story
        item { 
            StoryItem(
                userAvatar = currentUser.avatarUrl,
                username = stringResource(R.string.story_your_story_label),
                isAddButton = true,
                isViewed = false,
                isMockInterfaceEnabled = isMockInterfaceEnabled,
                onClick = onAddStoryClick // New: Use the callback to open CreateStoryDialog
            )
        }
        
        // Display actual stories
        items(stories, key = { it.id }) { story ->
            StoryItem(
                userAvatar = story.userAvatar,
                username = story.userName,
                isAddButton = false,
                isViewed = story.isViewed,
                isMockInterfaceEnabled = isMockInterfaceEnabled,
                onClick = { onViewStory(story) } // Updated to call the callback
            )
        }
    }
}

@Composable
fun StoryItem(
    userAvatar: String?,
    username: String,
    isAddButton: Boolean,
    isViewed: Boolean,
    isMockInterfaceEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isAddButton || !isViewed) 2.dp else 1.dp
    
    val rainbowColors = remember {
        listOf(
            Color(0xFFFF6B6B), Color(0xFFFFA500), Color(0xFFFFD700),
            Color(0xFF78C850), Color(0xFF6A5ACD), Color(0xFF487DE7),
            Color(0xFF9F70FD)
        )
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "story-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val borderBrush = when {
        isAddButton -> SolidColor(MaterialTheme.colorScheme.primary)
        !isViewed -> Brush.sweepGradient(rainbowColors)
        else -> SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageModifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .then(
                if (!isViewed && !isAddButton) {
                    Modifier.graphicsLayer { rotationZ = rotation }
                        .drawWithContent {
                            drawContent()
                            // This rotating border is drawn outside the clip
                        }
                        .border(borderWidth, borderBrush, CircleShape)
                } else {
                    Modifier.border(borderWidth, borderBrush, CircleShape)
                }
            )

        if (isAddButton) {
            Icon(
                Icons.Default.AddCircle, 
                contentDescription = stringResource(R.string.story_add_button_content_description), 
                modifier = imageModifier,
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
             val avatarUrl = if (isMockInterfaceEnabled) {
                userAvatar ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$username"
            } else {
                userAvatar
            }

            if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                 Icon(
                    Icons.Default.AccountCircle, 
                    contentDescription = stringResource(R.string.story_user_story_content_description, username), 
                    modifier = imageModifier,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.story_user_story_content_description, username), 
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(username, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String?, String?) -> Unit,
    @Suppress("unused") isPremium: Boolean,
    @Suppress("unused") safetyState: SafetyState
) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.create_post_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.create_post_hint)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image URL field
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.create_post_image_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Video URL field
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text(stringResource(R.string.create_post_video_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_post_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) onPost(
                                text,
                                "/gen",
                                imageUrl.ifBlank { null },
                                videoUrl.ifBlank { null }
                            )
                        },
                        // Enable if content is not blank
                        enabled = text.isNotBlank()
                    ) { Text(stringResource(R.string.create_post_button)) }
                }
            }
        }
    }
}

@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onPost: (String, Long) -> Unit,
    @Suppress("unused") safetyState: SafetyState
) {
    var imageUrl by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("5") } // Default to 5 seconds

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.create_story_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.create_story_image_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) { // Max 2 digits for seconds
                            durationText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.create_story_duration_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_story_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val duration = durationText.toLongOrNull() ?: 5 // Default to 5s if invalid
                            if (imageUrl.isNotBlank()) {
                                onPost(imageUrl, duration * 1000L) // Convert to milliseconds
                                onDismiss()
                            }
                        },
                        enabled = imageUrl.isNotBlank() && durationText.isNotBlank()
                    ) { Text(stringResource(R.string.create_story_add_button)) }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerView(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = false // Don't auto-play on load
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}
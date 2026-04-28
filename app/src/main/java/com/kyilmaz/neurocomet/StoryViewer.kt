package com.kyilmaz.neurocomet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Full-screen production-ready story viewer with:
 * - Progress bars for each story item
 * - Tap left/right to navigate
 * - Hold to pause
 * - Swipe down to close
 * - User info header
 * - Reply functionality
 */
@Composable
fun StoryViewerDialog(
    story: Story,
    onDismiss: () -> Unit,
    onStoryViewed: (Story) -> Unit,
    onReply: (Story, String) -> Unit = { _, _ -> },
    enableReactions: Boolean = true,
    onNextStory: (() -> Unit)? = null,
    onPreviousStory: (() -> Unit)? = null,
    onReaction: (Story, String) -> Unit = { _, _ -> },
    onDeleteStoryItem: ((Story, StoryItem) -> Unit)? = null
) {
    var currentItemIndex by remember(story.id) { mutableIntStateOf(0) }
    var isPaused by remember(story.id) { mutableStateOf(false) }
    var showReplyField by remember(story.id) { mutableStateOf(false) }
    var showReactionTray by remember(story.id) { mutableStateOf(false) }
    var showCustomReactionDialog by remember(story.id) { mutableStateOf(false) }
    var customReactionInput by remember(story.id) { mutableStateOf("") }
    var replyText by remember(story.id) { mutableStateOf("") }
    var isLiked by remember(story.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val appContext = remember { context.applicationContext }
    val brailleOptimized = SocialSettingsManager.isBrailleOptimized(context)
    val reactionPrefs = remember(context) {
        context.getSharedPreferences("story_reaction_prefs", android.content.Context.MODE_PRIVATE)
    }
    var customReactionEmojis by remember(story.id) {
        mutableStateOf(loadStoryReactionEmojis(reactionPrefs))
    }
    val availableReactionEmojis = remember(customReactionEmojis) {
        (DEFAULT_STORY_REACTIONS + customReactionEmojis).distinct().take(12)
    }

    // Swipe-down to dismiss state (neurodivergent-centric: smooth, predictable motion)
    val swipeOffsetY = remember(story.id) { Animatable(0f) }
    val dismissThreshold = 300f // Generous threshold for easier dismissal
    var isDragging by remember(story.id) { mutableStateOf(false) }
    var showDismissHint by remember(story.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Calculate visual feedback for swipe
    val swipeProgress = (swipeOffsetY.value / dismissThreshold).coerceIn(0f, 1f)
    val contentScale = 1f - (swipeProgress * 0.15f) // Subtle scale down (max 15%)
    val contentAlpha = 1f - (swipeProgress * 0.4f) // Fade out as swiping
    val cornerRadius = (swipeProgress * 32).dp // Rounded corners appear during swipe

    val currentItem = story.items.getOrNull(currentItemIndex)
    val activeItemKey = remember(story.id, currentItem?.id) { "${story.id}:${currentItem?.id ?: "none"}" }
    val progress = remember(activeItemKey) { Animatable(0f) }

    // Detect if current item is video
    val isVideo = remember(currentItem?.id, currentItem?.imageUrl) {
        currentItem?.imageUrl?.let { url ->
            url.endsWith(".mp4", ignoreCase = true) ||
            url.endsWith(".mov", ignoreCase = true) ||
            url.endsWith(".webm", ignoreCase = true) ||
            url.endsWith(".mkv", ignoreCase = true) ||
            url.endsWith(".avi", ignoreCase = true) ||
            url.contains("video", ignoreCase = true) ||
            (url.startsWith("content://") && url.contains("video"))
        } == true
    }

    // Mark story as viewed
    LaunchedEffect(story.id) {
        onStoryViewed(story)
    }

    // Reset progress when the active story item changes, including next-user transitions.
    LaunchedEffect(activeItemKey) {
        progress.snapTo(0f)
    }

    // Auto-advance timer (only for images)
    LaunchedEffect(activeItemKey, isPaused, isVideo) {
        if (!isPaused && currentItem != null && !isVideo) {
            val duration = currentItem.duration.toFloat()
            val currentProgress = progress.value
            val remainingTime = (duration * (1f - currentProgress)).toLong()

            if (remainingTime > 0) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = remainingTime.toInt(),
                        easing = LinearEasing
                    )
                )
                if (currentItemIndex < story.items.size - 1) {
                    currentItemIndex++
                } else {
                    if (onNextStory != null) onNextStory() else onDismiss()
                }
            } else if (progress.value >= 0.99f) {
                if (currentItemIndex < story.items.size - 1) {
                    currentItemIndex++
                } else {
                    if (onNextStory != null) onNextStory() else onDismiss()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // Outer container for swipe-down dismiss
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = contentAlpha))
        ) {
            // Swipe-down hint indicator (neurodivergent-friendly: clear visual cue)
            if (showDismissHint || swipeProgress > 0.1f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                        .alpha(swipeProgress.coerceIn(0.3f, 1f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.story_swipe_to_close),
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(0.8f + (swipeProgress * 0.2f))
                        )
                        if (swipeProgress > 0.5f) {
                            Text(
                                text = stringResource(R.string.story_release_to_close),
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Main content with swipe offset and visual transformations
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, swipeOffsetY.value.roundToInt()) }
                    .graphicsLayer {
                        scaleX = contentScale
                        scaleY = contentScale
                        alpha = contentAlpha
                    }
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(Color.Black)
                    // Swipe-down gesture detector
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                isDragging = true
                                isPaused = true // Pause story during swipe
                                showDismissHint = true
                            },
                            onDragEnd = {
                                isDragging = false
                                showDismissHint = false
                                scope.launch {
                                    if (swipeOffsetY.value > dismissThreshold) {
                                        // Dismiss with smooth animation
                                        swipeOffsetY.animateTo(
                                            targetValue = 1500f,
                                            animationSpec = tween(200)
                                        )
                                        onDismiss()
                                    } else {
                                        // Snap back with gentle spring (less jarring for sensory sensitivity)
                                        swipeOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                        isPaused = false
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                showDismissHint = false
                                scope.launch {
                                    swipeOffsetY.animateTo(0f)
                                    isPaused = false
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                // Only allow downward swipe (positive Y)
                                if (dragAmount > 0 || swipeOffsetY.value > 0) {
                                    scope.launch {
                                        val newOffset = (swipeOffsetY.value + dragAmount).coerceAtLeast(0f)
                                        swipeOffsetY.snapTo(newOffset)
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(story.id) {
                        var dragDistance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragDistance = 0f
                                isPaused = true
                            },
                            onHorizontalDrag = { _, amount ->
                                dragDistance += amount
                            },
                            onDragEnd = {
                                when {
                                    dragDistance <= -90f -> {
                                        if (onNextStory != null) onNextStory() else onDismiss()
                                    }
                                    dragDistance >= 90f -> {
                                        if (onPreviousStory != null) onPreviousStory() else Unit
                                    }
                                }
                                isPaused = false
                            }
                        )
                    }
                    // Tap gestures for navigation (separate from swipe)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val width = size.width
                                if (offset.x < width / 3) {
                                    // Tap left - go back
                                    if (currentItemIndex > 0) {
                                        currentItemIndex--
                                        scope.launch { progress.snapTo(0f) }
                                    }
                                    } else if (offset.x > width * 2 / 3) {
                                    // Tap right - go forward
                                    if (currentItemIndex < story.items.size - 1) {
                                        currentItemIndex++
                                        scope.launch { progress.snapTo(0f) }
                                    } else {
                                        if (onNextStory != null) onNextStory() else onDismiss()
                                    }
                                }
                            },
                            onLongPress = { isPaused = true },
                            onPress = {
                                tryAwaitRelease()
                                if (!isDragging) isPaused = false
                            }
                        )
                    }
            ) {
            // Story content - detect if video or image
            currentItem?.let { item ->

                when (item.contentType) {
                    StoryContentType.TEXT_ONLY -> {
                        val backgroundModifier = if (item.backgroundColorEnd != null) {
                            Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(item.backgroundColor.toInt()),
                                        Color(item.backgroundColorEnd.toInt())
                                    )
                                )
                            )
                        } else {
                            Modifier.background(Color(item.backgroundColor.toInt()))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(backgroundModifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.imageUrl,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                    StoryContentType.LINK -> {
                        // Display the link preview
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Tap to open link",
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                item.linkPreview?.let { preview ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                openSafeExternalUrl(
                                                    context = context,
                                                    url = preview.url
                                                )
                                            },
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column {
                                            if (preview.imageUrl != null) {
                                                AsyncImage(
                                                    model = preview.imageUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(150.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = preview.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (preview.description != null) {
                                                    Text(
                                                        text = preview.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 2,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                                Text(
                                                    text = preview.siteName ?: item.imageUrl,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(top = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Fallback logic for IMAGE and VIDEO
                        if (isVideo) {
                            // Video story
                            StoryVideoPlayer(
                                videoUri = item.imageUrl,
                                isPlaying = !isPaused,
                                isMuted = false,
                                onVideoEnded = {
                                    if (currentItemIndex < story.items.size - 1) {
                                        currentItemIndex++
                                    } else {
                                        if (onNextStory != null) onNextStory() else onDismiss()
                                    }
                                },
                                onProgressUpdate = { videoProgress ->
                                    scope.launch { progress.snapTo(videoProgress) }
                                },
                                modifier = Modifier.fillMaxSize(),
                                showControls = false
                            )
                        } else {
                            // Image story - use full quality loading
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.imageUrl)
                                        .crossfade(true)
                                        // No size constraints - load at full resolution
                                        .size(coil.size.Size.ORIGINAL)
                                        .build(),
                                    contentDescription = stringResource(R.string.story_image_content_description),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                // Optional text overlay for images
                                if (!item.textOverlay.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.textOverlay,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Progress bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    story.items.forEachIndexed { index, _ ->
                        StoryProgressBar(
                            progress = when {
                                index < currentItemIndex -> 1f
                                index == currentItemIndex -> progress.value
                                else -> 0f
                            },
                            itemIndex = index,
                            currentItemIndex = currentItemIndex,
                            totalItems = story.items.size,
                             modifier = Modifier.weight(1f)
                        )
                    }
                }

                // User info header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = story.userAvatar,
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.story_counter, currentItemIndex + 1, story.items.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Pause indicator
                    if (isPaused) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = stringResource(R.string.story_paused),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    if (onDeleteStoryItem != null && currentItem != null) {
                        IconButton(onClick = { onDeleteStoryItem(story, currentItem) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Story",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.story_close_button_content_description),
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Bottom reply section (gated by enableReactions flag)
                if (enableReactions) {
                if (showReactionTray && !showReplyField) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color.Black.copy(alpha = 0.42f),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableReactionEmojis.forEach { emoji ->
                                TextButton(
                                    onClick = {
                                        onReaction(story, emoji)
                                        showReactionTray = false
                                        isPaused = false
                                    }
                                ) {
                                    Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            StoryActionButton(
                                icon = Icons.Filled.Add,
                                label = "Add",
                                onClick = { showCustomReactionDialog = true }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (showReplyField) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text(stringResource(R.string.dm_reply_to_placeholder, story.userName)) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = if (brailleOptimized) "Story reply input" else "Reply to ${story.userName}"
                                    stateDescription = if (replyText.isBlank()) "Empty" else "${replyText.length} characters entered"
                                },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            modifier = Modifier.semantics {
                                contentDescription = if (replyText.isBlank()) "Send story reply disabled" else "Send story reply"
                            },
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReply(story, replyText)
                                    replyText = ""
                                    showReplyField = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.story_send_reply_content_description),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    // Pre-compute string resources for share functionality
                    val shareChooserLabel = stringResource(R.string.share_story_chooser)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StoryActionButton(
                            icon = Icons.Filled.EmojiEmotions,
                            label = if (showReactionTray) "Hide reactions" else "React",
                            onClick = {
                                val nextTrayState = !showReactionTray
                                showReactionTray = nextTrayState
                                isPaused = nextTrayState
                            }
                        )
                        StoryActionButton(
                            icon = Icons.Filled.ChatBubbleOutline,
                            label = stringResource(R.string.story_reply),
                            onClick = {
                                showReactionTray = false
                                showReplyField = true
                            }
                        )
                        StoryActionButton(
                            icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            label = if (isLiked) stringResource(R.string.story_liked) else stringResource(R.string.story_like),
                            onClick = {
                                isLiked = !isLiked
                                // Haptic feedback
                                @Suppress("DEPRECATION")
                                android.os.Build.VERSION.SDK_INT
                            },
                            tint = if (isLiked) Color(0xFFFF6B6B) else Color.White
                        )
                        StoryActionButton(
                            icon = Icons.Filled.Share,
                            label = stringResource(R.string.post_share),
                            onClick = {
                                // Share the current story item
                                currentItem?.let { item ->
                                    val shareText = appContext.getString(R.string.share_story_text, story.userName, item.imageUrl)
                                    val shareIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(
                                        android.content.Intent.createChooser(shareIntent, shareChooserLabel)
                                    )
                                }
                            }
                        )
                    }
                }
                } // end enableReactions gate

                if (showCustomReactionDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showCustomReactionDialog = false
                            customReactionInput = ""
                        },
                        title = { Text("Add custom reaction") },
                        text = {
                            OutlinedTextField(
                                value = customReactionInput,
                                onValueChange = { customReactionInput = it },
                                label = { Text("Emoji") },
                                singleLine = true,
                                placeholder = { Text("🫶") }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val emoji = customReactionInput.trim()
                                    if (emoji.isNotBlank()) {
                                        val updated = (customReactionEmojis + emoji).distinct().take(12)
                                        customReactionEmojis = updated
                                        saveStoryReactionEmojis(reactionPrefs, updated)
                                    }
                                    customReactionInput = ""
                                    showCustomReactionDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.action_save))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCustomReactionDialog = false
                                customReactionInput = ""
                            }) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    )
                }
            }
            } // End of swipe-down content Box
        } // End of outer container Box
    }
}

private val DEFAULT_STORY_REACTIONS = listOf("❤️", "😂", "😮", "😢", "🔥", "👏")

private fun loadStoryReactionEmojis(prefs: android.content.SharedPreferences): List<String> {
    return prefs.getString("custom_story_reactions", "")
        ?.split("|")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
}

private fun saveStoryReactionEmojis(
    prefs: android.content.SharedPreferences,
    emojis: List<String>
) {
    prefs.edit().putString("custom_story_reactions", emojis.joinToString("|")).apply()
}

@Composable
private fun StoryProgressBar(
    progress: Float,
    itemIndex: Int,
    currentItemIndex: Int,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp))
            .semantics {
                contentDescription = "Story progress"
                stateDescription = "Item ${currentItemIndex + 1} of $totalItems. Segment ${itemIndex + 1} is ${(progress * 100).toInt()} percent complete"
                progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
            }
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color.White)
        )
    }
}

@Composable
private fun StoryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = label
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

/**
 * Enhanced create story dialog with:
 * - Multiple story items support
 * - Preview functionality
 * - Text overlay option
 * - Background color selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCreateStoryDialog(
    onDismiss: () -> Unit,
    onPost: (String, Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") safetyState: SafetyState = SafetyState()
) {
    var currentImageUrl by remember { mutableStateOf("") }
    var currentDuration by remember { mutableStateOf("5") }
    var currentTextOverlay by remember { mutableStateOf("") }
    var selectedBackgroundColor by remember { mutableStateOf(0) }

    val backgroundColors = listOf(
        Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460),
        Color(0xFF4ECDC4), Color(0xFF6BCB77), Color(0xFFFFB347),
        Color(0xFFFF6B6B), Color(0xFF9B59B6)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.create_story_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.create_post_close))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Story type tabs
                Text(
                    text = stringResource(R.string.create_story_choose_type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Image URL input
                OutlinedTextField(
                    value = currentImageUrl,
                    onValueChange = { currentImageUrl = it },
                    label = { Text(stringResource(R.string.create_story_image_url_hint)) },
                    placeholder = { Text(stringResource(R.string.create_story_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Filled.Image, contentDescription = null)
                    },
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                // Duration selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.create_story_duration_hint), style = MaterialTheme.typography.bodyMedium)
                    listOf("3", "5", "7", "10").forEach { duration ->
                        FilterChip(
                            selected = currentDuration == duration,
                            onClick = { currentDuration = duration },
                            label = { Text(stringResource(R.string.time_seconds_short, duration.toInt())) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Text overlay input
                OutlinedTextField(
                    value = currentTextOverlay,
                    onValueChange = { currentTextOverlay = it },
                    label = { Text(stringResource(R.string.create_story_caption_label)) },
                    placeholder = { Text(stringResource(R.string.create_story_caption_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(Modifier.height(12.dp))

                // Background color picker (for text-only stories)
                Text(
                    text = stringResource(R.string.create_story_background_color),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    backgroundColors.forEachIndexed { index, color ->
                        val isSelected = selectedBackgroundColor == index
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedBackgroundColor = index }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Preview section
                if (currentImageUrl.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = currentImageUrl,
                                contentDescription = stringResource(R.string.create_story_preview_content_description),
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                            if (currentTextOverlay.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentTextOverlay,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.create_story_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val duration = currentDuration.toLongOrNull() ?: 5
                            if (currentImageUrl.isNotBlank()) {
                                onPost(currentImageUrl, duration * 1000L)
                            }
                        },
                        enabled = currentImageUrl.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.create_story_share_button))
                    }
                }
            }
        }
    }
}

/**
 * Draft story item for creation
 */
private data class StoryItemDraft(
    val imageUrl: String,
    val duration: Long,
    val textOverlay: String = "",
    val backgroundColor: Color = Color.Black
)


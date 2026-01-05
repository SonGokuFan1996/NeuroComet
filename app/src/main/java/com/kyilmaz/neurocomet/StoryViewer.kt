package com.kyilmaz.neurocomet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.abs
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
    onReply: (Story, String) -> Unit = { _, _ -> }
) {
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appContext = remember { context.applicationContext }

    // Swipe-down to dismiss state (neurodivergent-centric: smooth, predictable motion)
    val swipeOffsetY = remember { Animatable(0f) }
    val dismissThreshold = 300f // Generous threshold for easier dismissal
    var isDragging by remember { mutableStateOf(false) }
    var showDismissHint by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Calculate visual feedback for swipe
    val swipeProgress = (swipeOffsetY.value / dismissThreshold).coerceIn(0f, 1f)
    val contentScale = 1f - (swipeProgress * 0.15f) // Subtle scale down (max 15%)
    val contentAlpha = 1f - (swipeProgress * 0.4f) // Fade out as swiping
    val cornerRadius = (swipeProgress * 32).dp // Rounded corners appear during swipe

    val currentItem = story.items.getOrNull(currentItemIndex)
    val progress = remember { Animatable(0f) }

    // Mark story as viewed
    LaunchedEffect(story.id) {
        onStoryViewed(story)
    }

    // Auto-advance timer
    LaunchedEffect(currentItemIndex, isPaused) {
        if (!isPaused && currentItem != null) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = currentItem.duration.toInt(),
                    easing = LinearEasing
                )
            )
            // Move to next item or close
            if (currentItemIndex < story.items.size - 1) {
                currentItemIndex++
            } else {
                onDismiss()
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
                            contentDescription = "Swipe down to close",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(0.8f + (swipeProgress * 0.2f))
                        )
                        if (swipeProgress > 0.5f) {
                            Text(
                                text = "Release to close",
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
                                        onDismiss()
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
                val isVideo = item.imageUrl.let { url ->
                    url.endsWith(".mp4", ignoreCase = true) ||
                    url.endsWith(".mov", ignoreCase = true) ||
                    url.endsWith(".webm", ignoreCase = true) ||
                    url.endsWith(".mkv", ignoreCase = true) ||
                    url.endsWith(".avi", ignoreCase = true) ||
                    url.contains("video", ignoreCase = true) ||
                    url.startsWith("content://") && url.contains("video")
                }

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
                                onDismiss()
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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            // No size constraints - load at full resolution
                            .size(coil.size.Size.ORIGINAL)
                            .build(),
                        contentDescription = "Story image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
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
                        contentDescription = "User avatar",
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
                            text = "${currentItemIndex + 1} of ${story.items.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Pause indicator
                    if (isPaused) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Paused",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Bottom reply section
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
                            modifier = Modifier.weight(1f),
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
                                contentDescription = "Send reply",
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
                            icon = Icons.Filled.ChatBubbleOutline,
                            label = stringResource(R.string.story_reply),
                            onClick = { showReplyField = true }
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
            }
            } // End of swipe-down content Box
        } // End of outer container Box
    }
}

@Composable
private fun StoryProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp))
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
        modifier = Modifier.clickable(onClick = onClick)
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
    safetyState: SafetyState = SafetyState()
) {
    var storyItems by remember { mutableStateOf(listOf<StoryItemDraft>()) }
    var currentImageUrl by remember { mutableStateOf("") }
    var currentDuration by remember { mutableStateOf("5") }
    var currentTextOverlay by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }
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
                        text = "Create Story",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Story type tabs
                Text(
                    text = "Choose how to create your story:",
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
                            label = { Text("${duration}s") }
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
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = currentImageUrl,
                                contentDescription = "Story preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
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
                        Text("Cancel")
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
                        Text("Share Story")
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


package com.kyilmaz.neurocomet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Production-Ready Enhanced Story Creation Dialog
 *
 * Features:
 * - Smooth animations and transitions
 * - Input validation with helpful feedback
 * - Accessibility-friendly design
 * - Media upload with preview (images, videos, documents)
 * - Link sharing with auto-generated previews
 * - Text-only stories with beautiful gradient backgrounds
 * - Character limits and validation
 * - Loading states and error handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onPost: (StoryContentType, String, Long, String?, LinkPreviewData?) -> Unit,
    safetyState: SafetyState = SafetyState()
) {
    val context = LocalContext.current

    // State for story creation
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var linkUrl by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("5") }
    var textOverlay by remember { mutableStateOf("") }
    var selectedBackgroundIndex by remember { mutableIntStateOf(0) }
    var selectedGradientIndex by remember { mutableIntStateOf(-1) }
    var linkPreview by remember { mutableStateOf<LinkPreviewData?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }
    var contentType by remember { mutableStateOf(StoryContentType.IMAGE) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var fileSize by remember { mutableStateOf<Long?>(null) }
    var showBlockedWarning by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    val dialogScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val dialogAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "dialogAlpha"
    )

    // File picker launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            contentType = StoryFileUtils.getContentType(context, it)
            fileName = StoryFileUtils.getFileName(context, it)
            fileSize = StoryFileUtils.getFileSize(context, it)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            contentType = StoryContentType.VIDEO
            fileName = StoryFileUtils.getFileName(context, it)
            fileSize = StoryFileUtils.getFileSize(context, it)
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            contentType = StoryContentType.DOCUMENT
            fileName = StoryFileUtils.getFileName(context, it)
            fileSize = StoryFileUtils.getFileSize(context, it)
        }
    }

    // Generate link preview when URL changes
    LaunchedEffect(linkUrl) {
        if (linkUrl.isNotBlank() && (linkUrl.startsWith("http://") || linkUrl.startsWith("https://"))) {
            isLoadingPreview = true
            // Simulate network delay for real preview fetching
            delay(500)
            val preview = LinkPreviewGenerator.generatePreview(linkUrl)
            linkPreview = preview
            isLoadingPreview = false

            if (!preview.isSafe) {
                showBlockedWarning = true
            }
        } else {
            linkPreview = null
        }
    }

    // Validation helpers
    val isMediaValid = selectedTab == 0 && selectedUri != null
    val isLinkValid = selectedTab == 1 && linkPreview?.isSafe == true
    val isTextValid = selectedTab == 2 && textContent.isNotBlank() && textContent.length >= 3
    val canPost = (isMediaValid || isLinkValid || isTextValid) && !isPosting

    Dialog(
        onDismissRequest = { if (!isPosting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 680.dp)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.wrapContentHeight()) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    // Header with close button
                    StoryDialogHeader(
                        onDismiss = onDismiss,
                        isPosting = isPosting
                    )

                    Spacer(Modifier.height(12.dp))

                    // Content Type Tabs with animation
                    AnimatedContentTypeTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            selectedUri = null
                            linkUrl = ""
                            linkPreview = null
                        },
                        enabled = !isPosting
                    )

                    Spacer(Modifier.height(12.dp))

                    // Tab Content with crossfade animation
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        modifier = Modifier.wrapContentHeight(),
                        label = "tabContent"
                    ) { tab ->
                        Column(
                            modifier = Modifier.heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            when (tab) {
                                0 -> MediaTabContent(
                                    selectedUri = selectedUri,
                                    contentType = contentType,
                                    fileName = fileName,
                                    fileSize = fileSize,
                                    onPickImage = { imagePickerLauncher.launch("image/*") },
                                    onPickVideo = { videoPickerLauncher.launch("video/*") },
                                    onPickDocument = { documentPickerLauncher.launch("*/*") },
                                    onClear = {
                                        selectedUri = null
                                        fileName = null
                                        fileSize = null
                                    }
                                )

                                1 -> LinkTabContent(
                                    linkUrl = linkUrl,
                                    onLinkChange = { linkUrl = it },
                                    linkPreview = linkPreview,
                                    isLoading = isLoadingPreview
                                )

                                2 -> TextTabContent(
                                    textContent = textContent,
                                    onTextChange = { textContent = it },
                                    selectedBackgroundIndex = selectedBackgroundIndex,
                                    onBackgroundSelect = {
                                        selectedBackgroundIndex = it
                                        selectedGradientIndex = -1
                                    },
                                    selectedGradientIndex = selectedGradientIndex,
                                    onGradientSelect = {
                                        selectedGradientIndex = it
                                        selectedBackgroundIndex = -1
                                    }
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Duration selector (for all types)
                            DurationSelector(
                                selectedDuration = duration,
                                onDurationChange = { duration = it },
                                enabled = !isPosting
                            )

                            // Text overlay (for media types)
                            if (selectedTab == 0 && selectedUri != null) {
                                Column {
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = textOverlay,
                                        onValueChange = { if (it.length <= 100) textOverlay = it },
                                        label = { Text(stringResource(R.string.create_story_caption_label)) },
                                        placeholder = { Text(stringResource(R.string.create_story_caption_placeholder)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        maxLines = 2,
                                        supportingText = { Text("${textOverlay.length}/100") },
                                        enabled = !isPosting
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Action buttons with validation feedback
                    StoryActionButtons(
                        onDismiss = onDismiss,
                        onPost = {
                            isPosting = true
                            val durationMs = (duration.toLongOrNull() ?: 5) * 1000L
                            when (selectedTab) {
                                0 -> selectedUri?.let {
                                    onPost(contentType, it.toString(), durationMs, textOverlay.takeIf { overlay -> overlay.isNotBlank() }, null)
                                }
                                1 -> if (linkPreview?.isSafe == true) {
                                    onPost(StoryContentType.LINK, linkUrl, durationMs, null, linkPreview)
                                }
                                2 -> if (textContent.isNotBlank()) {
                                    onPost(StoryContentType.TEXT_ONLY, textContent, durationMs, null, null)
                                }
                            }
                        },
                        canPost = canPost,
                        isPosting = isPosting
                    )
                }

                // Success animation overlay
                if (showSuccessAnimation) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Story Posted!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Blocked content warning dialog
    if (showBlockedWarning) {
        AlertDialog(
            onDismissRequest = { showBlockedWarning = false },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Content Blocked",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    linkPreview?.safetyWarning
                        ?: "This link contains content that violates our community guidelines and cannot be shared.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockedWarning = false
                        linkUrl = ""
                        linkPreview = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.create_story_understood))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Story dialog header with title and close button
 */
@Composable
private fun StoryDialogHeader(
    onDismiss: () -> Unit,
    isPosting: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "✨",
                fontSize = 22.sp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Create Moment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(
            onClick = onDismiss,
            enabled = !isPosting,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Close",
                modifier = Modifier.size(22.dp),
                tint = if (isPosting)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Animated content type tabs
 */
@Composable
private fun AnimatedContentTypeTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    enabled: Boolean
) {
    val tabs = listOf(
        TabInfo("📷", "Media"),
        TabInfo("🔗", "Link"),
        TabInfo("✏️", "Text")
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "tabWeight"
                )

                Surface(
                    onClick = { if (enabled) onTabSelected(index) },
                    modifier = Modifier
                        .weight(animatedWeight)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent,
                    enabled = enabled
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tab.emoji,
                            fontSize = 16.sp
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TabInfo(val emoji: String, val label: String)

/**
 * Duration selector with visual feedback
 */
@Composable
private fun DurationSelector(
    selectedDuration: String,
    onDurationChange: (String) -> Unit,
    enabled: Boolean
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Display Duration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("3", "5", "7", "10", "15").forEach { dur ->
                val isSelected = selectedDuration == dur

                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "durationScale"
                )

                FilterChip(
                    selected = isSelected,
                    onClick = { if (enabled) onDurationChange(dur) },
                    label = {
                        Text(
                            "${dur}s",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.scale(animatedScale),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * Action buttons with post validation
 */
@Composable
private fun StoryActionButtons(
    onDismiss: () -> Unit,
    onPost: () -> Unit,
    canPost: Boolean,
    isPosting: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isPosting
        ) {
            Text(stringResource(R.string.create_story_cancel))
        }

        Button(
            onClick = onPost,
            enabled = canPost,
            modifier = Modifier
                .weight(1.5f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (canPost) 2.dp else 0.dp
            )
        ) {
            if (isPosting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.create_story_posting))
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.moments_your_story),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MediaTabContent(
    selectedUri: Uri?,
    contentType: StoryContentType,
    fileName: String?,
    fileSize: Long?,
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickDocument: () -> Unit,
    onClear: () -> Unit
) {
    var showImageEditor by remember { mutableStateOf(false) }
    var editState by remember { mutableStateOf<ImageEditState?>(null) }

    Column {
        Text(
            "Choose content from your device",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Media type buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MediaPickerButton(
                icon = Icons.Filled.Image,
                label = "Photo",
                onClick = onPickImage,
                modifier = Modifier.weight(1f)
            )
            MediaPickerButton(
                icon = Icons.Filled.Videocam,
                label = "Video",
                onClick = onPickVideo,
                modifier = Modifier.weight(1f)
            )
            MediaPickerButton(
                icon = Icons.Filled.Description,
                label = "File",
                onClick = onPickDocument,
                modifier = Modifier.weight(1f)
            )
        }

        // Selected file preview
        AnimatedVisibility(
            visible = selectedUri != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    when (contentType) {
                        StoryContentType.IMAGE -> {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(selectedUri)
                                        .crossfade(true)
                                        // Preserve original quality - no resizing
                                        .size(coil.size.Size.ORIGINAL)
                                        .build(),
                                    contentDescription = "Selected image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .then(
                                            if (editState?.filter?.colorMatrix != null) {
                                                Modifier.graphicsLayer {
                                                    // Apply filter visually
                                                }
                                            } else Modifier
                                        ),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = editState?.filter?.colorMatrix?.let {
                                        ColorFilter.colorMatrix(ColorMatrix(it))
                                    }
                                )

                                // Edit and Clear buttons
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Edit button
                                    IconButton(
                                        onClick = { showImageEditor = true },
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit image",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Clear button
                                    IconButton(
                                        onClick = {
                                            onClear()
                                            editState = null
                                        },
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Show edit badge if edited
                                if (editState != null && (editState!!.filter != StoryImageFilter.NONE ||
                                    editState!!.textOverlays.isNotEmpty() ||
                                    editState!!.drawingPaths.isNotEmpty() ||
                                    editState!!.stickers.isNotEmpty())) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Edited",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        StoryContentType.VIDEO -> {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Video preview with play button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black)
                                ) {
                                    // Use the video player for preview
                                    selectedUri?.let { uri ->
                                        FeedVideoPlayer(
                                            videoUri = uri.toString(),
                                            modifier = Modifier.fillMaxSize(),
                                            autoPlay = false,
                                            showControls = true
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onClear,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        StoryContentType.DOCUMENT, StoryContentType.AUDIO -> {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        StoryFileUtils.getContentTypeIcon(contentType),
                                        fontSize = 28.sp
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        fileName ?: "File",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    fileSize?.let {
                                        Text(
                                            StoryFileUtils.formatFileSize(it),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = onClear) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove")
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    // Image Editor Dialog
    if (showImageEditor && selectedUri != null) {
        ImageEditorDialog(
            imageUri = selectedUri,
            onDismiss = { showImageEditor = false },
            onSave = { state ->
                editState = state
                showImageEditor = false
            }
        )
    }
}

@Composable
private fun MediaPickerButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun LinkTabContent(
    linkUrl: String,
    onLinkChange: (String) -> Unit,
    linkPreview: LinkPreviewData?,
    isLoading: Boolean
) {
    Column {
        Text(
            stringResource(R.string.create_story_share_link),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = linkUrl,
            onValueChange = onLinkChange,
            label = { Text(stringResource(R.string.create_story_url_label)) },
            placeholder = { Text(stringResource(R.string.create_story_url_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // Link preview card
        AnimatedVisibility(visible = isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        AnimatedVisibility(visible = !isLoading && linkPreview != null) {
            linkPreview?.let { preview ->
                LinkPreviewCard(preview = preview)
            }
        }
    }
}

@Composable
private fun LinkPreviewCard(preview: LinkPreviewData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (preview.isSafe)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column {
            // Image preview if available
            preview.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Content type badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val typeIcon = when (preview.contentType) {
                        LinkContentType.VIDEO -> "🎬"
                        LinkContentType.MUSIC -> "🎵"
                        LinkContentType.REPOSITORY -> "💻"
                        LinkContentType.SOCIAL_POST -> "💬"
                        LinkContentType.PRODUCT -> "🛒"
                        LinkContentType.ARTICLE -> "📰"
                        LinkContentType.DOCUMENT -> "📄"
                        else -> "🔗"
                    }
                    Text(typeIcon, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        preview.siteName ?: "Link",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (preview.isSafe)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    preview.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (preview.isSafe)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )

                // Description
                preview.description?.let { desc ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (preview.isSafe)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Safety warning
                if (!preview.isSafe) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            preview.safetyWarning ?: "Content blocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextTabContent(
    textContent: String,
    onTextChange: (String) -> Unit,
    selectedBackgroundIndex: Int,
    onBackgroundSelect: (Int) -> Unit,
    selectedGradientIndex: Int,
    onGradientSelect: (Int) -> Unit
) {
    val selectedBackground = if (selectedBackgroundIndex >= 0 && selectedBackgroundIndex < StoryBackgroundColors.colors.size) {
        StoryBackgroundColors.colors[selectedBackgroundIndex].first
    } else {
        null
    }

    val selectedGradient = if (selectedGradientIndex >= 0 && selectedGradientIndex < StoryBackgroundColors.gradients.size) {
        StoryBackgroundColors.gradients[selectedGradientIndex].first
    } else {
        null
    }

    Column {
        Text(
            stringResource(R.string.create_story_create_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (selectedGradient != null) {
                            Modifier.background(
                                Brush.linearGradient(selectedGradient)
                            )
                        } else {
                            Modifier.background(selectedBackground ?: Color(0xFF1a1a2e))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val previewPlaceholder = stringResource(R.string.create_story_text_preview)
                Text(
                    text = textContent.ifBlank { previewPlaceholder },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Text input
        OutlinedTextField(
            value = textContent,
            onValueChange = { if (it.length <= 200) onTextChange(it) },
            label = { Text(stringResource(R.string.create_story_message_label)) },
            placeholder = { Text(stringResource(R.string.create_story_message_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            supportingText = { Text("${textContent.length}/200") }
        )

        Spacer(Modifier.height(16.dp))

        // Solid colors
        Text(
            stringResource(R.string.create_story_background_color),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StoryBackgroundColors.colors.size) { index ->
                val (color, name) = StoryBackgroundColors.colors[index]
                val isSelected = selectedBackgroundIndex == index && selectedGradientIndex == -1
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else Modifier
                        )
                        .clickable { onBackgroundSelect(index) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Gradient backgrounds
        Text(
            stringResource(R.string.create_story_gradient_backgrounds),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StoryBackgroundColors.gradients.size) { index ->
                val (gradientColors, name) = StoryBackgroundColors.gradients[index]
                val isSelected = selectedGradientIndex == index
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradientColors))
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else Modifier
                        )
                        .clickable { onGradientSelect(index) }
                )
            }
        }
    }
}


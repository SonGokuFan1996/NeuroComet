package com.kyilmaz.neuronetworkingtitle

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Enhanced Story Creation Dialog
 *
 * Allows users to create stories from:
 * - Local images/videos from gallery
 * - Document files (PDF, etc.)
 * - Links with auto-generated previews
 * - Text-only stories with beautiful backgrounds
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
            kotlinx.coroutines.delay(500)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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

                // Content Type Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; selectedUri = null },
                        text = { Text("📷 Media") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; selectedUri = null },
                        text = { Text("🔗 Link") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; selectedUri = null },
                        text = { Text("✏️ Text") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
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
                    Text(
                        "Display Duration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("3", "5", "7", "10", "15").forEach { dur ->
                            FilterChip(
                                selected = duration == dur,
                                onClick = { duration = dur },
                                label = { Text("${dur}s") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Text overlay (for media types)
                    if (selectedTab == 0 && selectedUri != null) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = textOverlay,
                            onValueChange = { textOverlay = it },
                            label = { Text("Text Overlay (optional)") },
                            placeholder = { Text("Add caption...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

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
                            val durationMs = (duration.toLongOrNull() ?: 5) * 1000L
                            when (selectedTab) {
                                0 -> {
                                    selectedUri?.let {
                                        onPost(contentType, it.toString(), durationMs, textOverlay.takeIf { it.isNotBlank() }, null)
                                    }
                                }
                                1 -> {
                                    if (linkPreview?.isSafe == true) {
                                        onPost(StoryContentType.LINK, linkUrl, durationMs, null, linkPreview)
                                    }
                                }
                                2 -> {
                                    if (textContent.isNotBlank()) {
                                        onPost(StoryContentType.TEXT_ONLY, textContent, durationMs, null, null)
                                    }
                                }
                            }
                        },
                        enabled = when (selectedTab) {
                            0 -> selectedUri != null
                            1 -> linkPreview?.isSafe == true
                            2 -> textContent.isNotBlank()
                            else -> false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Share Story")
                    }
                }
            }
        }
    }

    // Blocked content warning dialog
    if (showBlockedWarning) {
        AlertDialog(
            onDismissRequest = { showBlockedWarning = false },
            icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Content Blocked") },
            text = {
                Text(
                    linkPreview?.safetyWarning
                        ?: "This link contains content that violates our community guidelines and cannot be shared."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showBlockedWarning = false
                    linkUrl = ""
                    linkPreview = null
                }) {
                    Text("OK")
                }
            }
        )
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
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
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
            "Share a link with a preview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = linkUrl,
            onValueChange = onLinkChange,
            label = { Text("URL") },
            placeholder = { Text("https://...") },
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
            "Create a text story",
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
                Text(
                    text = textContent.ifBlank { "Your text here..." },
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
            label = { Text("Your message") },
            placeholder = { Text("What's on your mind?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            supportingText = { Text("${textContent.length}/200") }
        )

        Spacer(Modifier.height(16.dp))

        // Solid colors
        Text(
            "Background Color",
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
            "Gradient Backgrounds",
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


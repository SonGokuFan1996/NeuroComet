@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_PARAMETER")

package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Duration
import java.time.Instant
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import android.widget.Toast

// =============================================================================
// NEURO-FRIENDLY MESSAGES UI - REBUILT FROM GROUND UP
// =============================================================================

// Quick reaction emojis for messages (like WhatsApp/Telegram/iMessage)
private val QUICK_REACTIONS = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")

/**
 * Screen size categories for adaptive layouts
 */
private enum class ScreenSize {
    COMPACT,    // Phones in portrait (< 600dp)
    MEDIUM,     // Tablets in portrait, phones in landscape (600-840dp)
    EXPANDED    // Tablets in landscape (> 840dp)
}

@Composable
private fun rememberScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    return when {
        screenWidthDp < 600 -> ScreenSize.COMPACT
        screenWidthDp < 840 -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

/**
 * Design tokens for the messages UI - Material/Android style
 * Adaptive based on screen size
 */
@Composable
private fun rememberMessagesDesign(): MessagesDesignTokens {
    val screenSize = rememberScreenSize()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    return remember(screenSize, isLandscape) {
        MessagesDesignTokens(
            touchTarget = when (screenSize) {
                ScreenSize.COMPACT -> 48.dp
                ScreenSize.MEDIUM -> 52.dp
                ScreenSize.EXPANDED -> 56.dp
            },
            avatarSize = when (screenSize) {
                ScreenSize.COMPACT -> 36.dp
                ScreenSize.MEDIUM -> 40.dp
                ScreenSize.EXPANDED -> 44.dp
            },
            avatarSizeLarge = when (screenSize) {
                ScreenSize.COMPACT -> 48.dp
                ScreenSize.MEDIUM -> 52.dp
                ScreenSize.EXPANDED -> 56.dp
            },
            bubbleMaxWidth = when (screenSize) {
                ScreenSize.COMPACT -> 280.dp
                ScreenSize.MEDIUM -> 400.dp
                ScreenSize.EXPANDED -> 500.dp
            },
            bubbleCornerRadius = 12.dp,
            composerCornerRadius = 28.dp,
            bubblePadding = when (screenSize) {
                ScreenSize.COMPACT -> 12.dp
                ScreenSize.MEDIUM -> 14.dp
                ScreenSize.EXPANDED -> 16.dp
            },
            itemSpacing = 4.dp,
            horizontalPadding = when (screenSize) {
                ScreenSize.COMPACT -> 8.dp
                ScreenSize.MEDIUM -> 16.dp
                ScreenSize.EXPANDED -> 24.dp
            },
            contentMaxWidth = when (screenSize) {
                ScreenSize.COMPACT -> null // Full width
                ScreenSize.MEDIUM -> 600.dp
                ScreenSize.EXPANDED -> 800.dp
            },
            isLandscape = isLandscape
        )
    }
}

private data class MessagesDesignTokens(
    val touchTarget: androidx.compose.ui.unit.Dp,
    val avatarSize: androidx.compose.ui.unit.Dp,
    val avatarSizeLarge: androidx.compose.ui.unit.Dp,
    val bubbleMaxWidth: androidx.compose.ui.unit.Dp,
    val bubbleCornerRadius: androidx.compose.ui.unit.Dp,
    val composerCornerRadius: androidx.compose.ui.unit.Dp,
    val bubblePadding: androidx.compose.ui.unit.Dp,
    val itemSpacing: androidx.compose.ui.unit.Dp,
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val contentMaxWidth: androidx.compose.ui.unit.Dp?,
    val isLandscape: Boolean
)

// Static fallback for non-composable contexts
private object MessagesDesign {
    val touchTarget = 48.dp
    val avatarSize = 36.dp
    val avatarSizeLarge = 48.dp
    val bubbleMaxWidth = 320.dp
    val bubbleCornerRadius = 12.dp
    val composerCornerRadius = 28.dp
    val bubblePadding = 12.dp
    val itemSpacing = 4.dp
    val horizontalPadding = 8.dp
}

/**
 * Debug settings for message bar (simplified - navbar padding handled automatically)
 */
object MessageBarDebug {
    var enabled by mutableStateOf(false)
    var surfaceElevation by mutableStateOf(2f)
    var listBottomPadding by mutableStateOf(0f)
}

// Neurocentric sensory modes
private enum class SensoryMode { CALM, FOCUS, STIM }

// Theme-aware bubble colors - pass isDark to avoid @Composable requirement
private fun outgoingBubbleColor(mode: SensoryMode, energy: Float, isDark: Boolean): Color = when (mode) {
    SensoryMode.CALM -> if (isDark) Color(0xFF4A90D9) else Color(0xFF2962FF)  // Blue
    SensoryMode.FOCUS -> if (isDark) Color(0xFF5CB8A5) else Color(0xFF00897B) // Teal
    SensoryMode.STIM -> if (isDark) Color(0xFF9575CD) else Color(0xFF7B1FA2)  // Purple
}

private fun incomingBubbleColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF37474F) else Color(0xFFE0E0E0)  // Dark gray / Light gray

private fun bubbleTextColor(isFromMe: Boolean, isDark: Boolean): Color = when {
    isFromMe -> Color.White  // Outgoing always white (on colored background)
    isDark -> Color.White    // Incoming in dark mode
    else -> Color(0xFF212121) // Incoming in light mode
}

private fun laneAccent(mode: SensoryMode, isDark: Boolean): Color = when (mode) {
    SensoryMode.CALM -> if (isDark) Color(0xFFB3D4FF) else Color(0xFFE3F2FD)
    SensoryMode.FOCUS -> if (isDark) Color(0xFFC9F3E8) else Color(0xFFE0F2F1)
    SensoryMode.STIM -> if (isDark) Color(0xFFE4D1FF) else Color(0xFFF3E5F5)
}

private val sensoryModes = listOf(
    SensoryMode.CALM to "Calm",
    SensoryMode.FOCUS to "Focus",
    SensoryMode.STIM to "Stim"
)

// =============================================================================
// INBOX SCREEN
// =============================================================================

@Composable
fun NeuroInboxScreen(
    conversations: List<Conversation>,
    safetyState: SafetyState,
    onOpenConversation: (String) -> Unit,
    onNewMessage: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onOpenCallHistory: () -> Unit = {},
    onOpenPracticeCall: () -> Unit = {}
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }
    val restriction = shouldBlockFeature(parentalState, BlockableFeature.DMS)

    if (restriction != null) {
        ParentalBlockedScreen(
            restrictionType = restriction,
            featureName = "Direct Messages"
        )
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { conv ->
            val otherId = conv.participants.firstOrNull { it != "me" } ?: ""
            val user = MOCK_USERS.find { it.id == otherId }
            val text = "${user?.name ?: otherId} ${conv.messages.lastOrNull()?.content ?: ""}"
            text.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                if (isSearching) {
                    SearchTopBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClose = {
                            isSearching = false
                            searchQuery = ""
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                "Messages",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        navigationIcon = {
                            if (onBack != null) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = onOpenPracticeCall) {
                                Icon(Icons.Filled.Headset, "Practice Calls")
                            }
                            IconButton(onClick = onOpenCallHistory) {
                                Icon(Icons.Filled.Phone, "Call History")
                            }
                            IconButton(onClick = { isSearching = true }) {
                                Icon(Icons.Filled.Search, "Search")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewMessage,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Edit, "New message")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (filteredConversations.isEmpty()) {
            EmptyInboxState(
                isSearchResult = searchQuery.isNotBlank(),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredConversations, key = { it.id }) { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onClick = { onOpenConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search conversations") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
            }
        },
        colors = topAppBarColors
    )
}

@Composable
private fun EmptyInboxState(
    isSearchResult: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Forum,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = if (isSearchResult) "No results found" else "No conversations yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isSearchResult) {
                Text(
                    text = "Tap + to start a conversation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val design = rememberMessagesDesign()
    val otherId = conversation.participants.firstOrNull { it != "me" } ?: return
    val user = MOCK_USERS.find { it.id == otherId }
    val avatar = user?.avatarUrl ?: avatarUrl(otherId)
    val name = user?.name ?: otherId
    val lastMessage = conversation.messages.lastOrNull()
    val hasUnread = conversation.unreadCount > 0

    val timeAgo = remember(lastMessage?.timestamp) {
        formatTimeAgo(lastMessage?.timestamp)
    }

    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = design.horizontalPadding + 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(design.avatarSizeLarge)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                // Get font settings
                val fontSettings = LocalFontSettings.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = NeuroDivergentTypography.username(fontSettings),
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeAgo,
                        style = NeuroDivergentTypography.timestamp(fontSettings),
                        color = if (hasUnread)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lastMessage?.content ?: "No messages yet",
                    style = NeuroDivergentTypography.messagePreview(fontSettings),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =============================================================================
// CONVERSATION SCREEN - Google Messages + AOSP + Neurodivergent Design
// =============================================================================

/**
 * NeuroComet Messaging Screen
 *
 * A unique blend of:
 * - Google Messages: Material 3 design, smooth animations, modern feel
 * - AOSP Messages: Clean simplicity, efficient layout, no bloat
 * - Neurodivergent-friendly: Calm colors, clear hierarchy, reduced cognitive load
 * - Modern reactions: Long-press to react like WhatsApp/Telegram/iMessage
 */
@Composable
fun NeuroConversationScreen(
    conversation: Conversation,
    onBack: () -> Unit,
    onSend: (recipientId: String, content: String) -> Unit,
    onReport: (messageId: String) -> Unit,
    onRetryMessage: (convId: String, msgId: String) -> Unit,
    onReactToMessage: (messageId: String, emoji: String) -> Unit = { _, _ -> },
    isBlocked: (String) -> Boolean,
    isMuted: (String) -> Boolean,
    onBlockUser: (String) -> Unit = {},
    onUnblockUser: (String) -> Unit = {}
) {
    val recipientId = conversation.participants.firstOrNull { it != "me" } ?: return
    val user = MOCK_USERS.find { it.id == recipientId }
    val avatar = user?.avatarUrl ?: avatarUrl(recipientId)
    val displayName = user?.name ?: recipientId

    val isUserBlocked = isBlocked(recipientId)
    val isUserMuted = isMuted(recipientId)

    // Detect dark mode for theme-aware colors
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    var showEmojiPanel by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentPicker by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var pendingAttachment by remember { mutableStateOf<MessageAttachment?>(null) }
    val context = LocalContext.current

    // Call state
    var showCallDialog by remember { mutableStateOf(false) }
    val currentCall = MockCallManager.currentCall
    val callState = MockCallManager.callState
    val callDuration = MockCallManager.callDuration

    // Voice recorder
    val voiceRecorder = remember { VoiceRecorder(context) }

    // Attachment state with handlers
    val attachmentState = rememberAttachmentState { attachment ->
        pendingAttachment = attachment
        showAttachmentPicker = false
        Toast.makeText(
            context,
            "${attachment.type}: ${attachment.displayName}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Recording timer effect
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            while (isRecordingVoice) {
                recordingDuration = voiceRecorder.getCurrentDuration()
                kotlinx.coroutines.delay(100)
            }
        } else {
            recordingDuration = 0
        }
    }

    // Keyboard visibility detection
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardVisible = imeBottom > 0

    // Neurodivergent color palette - calming gradients
    val primaryGradient = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    // Auto-hide emoji when keyboard shows
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && showEmojiPanel) {
            showEmojiPanel = false
        }
    }

    // Scroll to bottom on new messages
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    // Show scroll-to-bottom button logic
    val showScrollButton by remember {
        derivedStateOf {
            val total = conversation.messages.size
            if (total <= 2) false
            else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible < total - 2
            }
        }
    }

    // Input bar color
    val inputBarColor = if (isUserBlocked)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // AOSP-inspired minimal header with Google Messages polish
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button - AOSP style
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Avatar with neurodivergent-friendly gradient ring
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(primaryGradient),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // User info - clean AOSP layout
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Status row with visual indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Status dot
                            val statusColor = when {
                                isUserBlocked -> MaterialTheme.colorScheme.error
                                isUserMuted -> MaterialTheme.colorScheme.outline
                                else -> Color(0xFF4CAF50) // Online green
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = when {
                                    isUserBlocked -> "Blocked"
                                    isUserMuted -> "Muted"
                                    else -> "Online"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action buttons - Google Messages style
                    IconButton(onClick = {
                        MockCallManager.startCall(
                            recipientId = recipientId,
                            recipientName = displayName,
                            recipientAvatar = avatar,
                            callType = CallType.VIDEO
                        )
                        showCallDialog = true
                    }) {
                        Icon(
                            Icons.Filled.Videocam,
                            contentDescription = "Video call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = {
                        MockCallManager.startCall(
                            recipientId = recipientId,
                            recipientName = displayName,
                            recipientAvatar = avatar,
                            callType = CallType.VOICE
                        )
                        showCallDialog = true
                    }) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Voice call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overflow menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View profile") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Filled.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Search") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Filled.Search, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (isUserBlocked) "Unblock" else "Block") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Filled.Block, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isUserMuted) "Unmute" else "Mute") },
                                onClick = { showMenu = false },
                                leadingIcon = {
                                    Icon(
                                        if (isUserMuted) Icons.AutoMirrored.Filled.VolumeUp
                                        else Icons.AutoMirrored.Filled.VolumeOff,
                                        null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Google Messages-style floating input with neurodivergent enhancements
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = inputBarColor,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    if (isUserBlocked) {
                        // Blocked state - calming design
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Messaging paused",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Unblock to resume conversation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                            FilledTonalButton(onClick = { onUnblockUser(recipientId) }) {
                                Text("Unblock")
                            }
                        }
                    } else {
                        // Pending attachment preview
                        pendingAttachment?.let { attachment ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        when (attachment.type) {
                                            AttachmentType.IMAGE -> Icons.Filled.Image
                                            AttachmentType.VIDEO -> Icons.Filled.Videocam
                                            AttachmentType.DOCUMENT -> Icons.Outlined.Description
                                            AttachmentType.AUDIO -> Icons.Filled.Headphones
                                            AttachmentType.LOCATION -> Icons.Filled.LocationOn
                                            AttachmentType.CONTACT -> Icons.Filled.Person
                                            AttachmentType.VOICE_MESSAGE -> Icons.Filled.Mic
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        attachment.displayName.ifEmpty { attachment.type.name },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { pendingAttachment = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove attachment",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Voice recording indicator
                        if (isRecordingVoice) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Pulsing recording indicator
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        "Recording...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        formatRecordingDuration(recordingDuration),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    IconButton(
                                        onClick = {
                                            voiceRecorder.cancelRecording()
                                            isRecordingVoice = false
                                            Toast.makeText(context, "Recording cancelled", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Cancel recording",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Attachment picker with smooth animation
                        AnimatedVisibility(
                            visible = showAttachmentPicker && !isRecordingVoice,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            NeuroAttachmentPicker(
                                attachmentState = attachmentState,
                                onDismiss = { showAttachmentPicker = false }
                            )
                        }

                        // Emoji picker with smooth animation
                        AnimatedVisibility(
                            visible = showEmojiPanel,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            NeuroEmojiPanel(
                                onEmojiSelected = { messageText += it },
                                onDismiss = { showEmojiPanel = false }
                            )
                        }

                        // Input row - blend of Google Messages & AOSP
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Attachment button - Google Messages style
                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { showAttachmentPicker = !showAttachmentPicker },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        if (showAttachmentPicker) Icons.Filled.Close else Icons.Filled.Add,
                                        contentDescription = if (showAttachmentPicker) "Close attachments" else "Add attachment",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Text field - AOSP minimal with Google polish
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp, max = 120.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Emoji toggle
                                    IconButton(
                                        onClick = {
                                            if (showEmojiPanel) {
                                                showEmojiPanel = false
                                                keyboardController?.show()
                                            } else {
                                                keyboardController?.hide()
                                                showEmojiPanel = true
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            if (showEmojiPanel) Icons.Filled.Keyboard else Icons.Filled.EmojiEmotions,
                                            contentDescription = if (showEmojiPanel) "Keyboard" else "Emoji",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Text input
                                    TextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Message") },
                                        colors = TextFieldDefaults.colors(
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent
                                        ),
                                        maxLines = 4,
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                val text = messageText.trim()
                                                if (text.isNotEmpty()) {
                                                    onSend(recipientId, text)
                                                    messageText = ""
                                                    showEmojiPanel = false
                                                }
                                            }
                                        )
                                    )

                                    // Camera button when empty and not recording
                                    if (messageText.isEmpty() && !isRecordingVoice) {
                                        IconButton(
                                            onClick = { attachmentState.onTakePhoto() },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.CameraAlt,
                                                contentDescription = "Take photo",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Send/Mic button with animation
                            val hasText = messageText.isNotBlank()
                            val hasAttachment = pendingAttachment != null

                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        when {
                                            hasText || hasAttachment -> {
                                                // Send message with optional attachment
                                                val text = messageText.trim()
                                                if (text.isNotEmpty() || hasAttachment) {
                                                    // Include attachment info in message if present
                                                    val finalMessage = if (hasAttachment && text.isEmpty()) {
                                                        "[${pendingAttachment?.type?.name}: ${pendingAttachment?.displayName}]"
                                                    } else if (hasAttachment) {
                                                        "$text\n[${pendingAttachment?.type?.name}: ${pendingAttachment?.displayName}]"
                                                    } else {
                                                        text
                                                    }
                                                    onSend(recipientId, finalMessage)
                                                    messageText = ""
                                                    pendingAttachment = null
                                                    showEmojiPanel = false
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                            isRecordingVoice -> {
                                                // Stop recording and send voice message
                                                val voiceMessage = voiceRecorder.stopRecording()
                                                isRecordingVoice = false
                                                if (voiceMessage != null) {
                                                    // Send voice message
                                                    onSend(recipientId, "[Voice message: ${voiceMessage.durationFormatted}]")
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    Toast.makeText(
                                                        context,
                                                        "Voice message sent (${voiceMessage.durationFormatted})",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Recording failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            else -> {
                                                // Start voice recording
                                                if (attachmentState.hasAudioPermission()) {
                                                    if (voiceRecorder.startRecording()) {
                                                        isRecordingVoice = true
                                                        showAttachmentPicker = false
                                                        showEmojiPanel = false
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to start recording",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else {
                                                    attachmentState.onRequestAudioPermission()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(44.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = when {
                                            hasText || hasAttachment -> MaterialTheme.colorScheme.primary
                                            isRecordingVoice -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        },
                                        contentColor = when {
                                            hasText || hasAttachment -> MaterialTheme.colorScheme.onPrimary
                                            isRecordingVoice -> MaterialTheme.colorScheme.onError
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                ) {
                                    Icon(
                                        when {
                                            hasText || hasAttachment -> Icons.AutoMirrored.Filled.Send
                                            isRecordingVoice -> Icons.Filled.Stop
                                            else -> Icons.Filled.Mic
                                        },
                                        contentDescription = when {
                                            hasText || hasAttachment -> "Send message"
                                            isRecordingVoice -> "Stop recording"
                                            else -> "Record voice message"
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (conversation.messages.isEmpty()) {
                // Empty state - neurodivergent calming design
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Infinity-inspired icon container
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Start chatting with $displayName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Messages are end-to-end encrypted. Say hi! 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    // Quick action suggestions - neurodivergent friendly
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("👋 Hi!", "✨ Hello!", "💜 Hey there!").forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    onSend(recipientId, suggestion.substringAfter(" "))
                                },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            } else {
                // Message list - AOSP efficiency with Google polish
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(conversation.messages, key = { it.id }) { message ->
                        val isFromMe = message.senderId == "me"

                        NeuroMessageItem(
                            message = message,
                            isFromMe = isFromMe,
                            isDark = isDark,
                            onReport = { onReport(message.id) },
                            onRetry = { onRetryMessage(conversation.id, message.id) },
                            onReact = { emoji -> onReactToMessage(message.id, emoji) }
                        )
                    }
                }

                // Scroll to bottom FAB - Google Messages style
                AnimatedVisibility(
                    visible = showScrollButton,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(conversation.messages.size - 1)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom"
                        )
                    }
                }
            }
        }
    }

    // Call Dialog
    if (showCallDialog && currentCall != null) {
        CallDialog(
            call = currentCall,
            callState = callState,
            callDuration = callDuration,
            onDismiss = { showCallDialog = false }
        )
    }
}

/**
 * Neurodivergent-friendly emoji panel.
 */
@Composable
private fun NeuroEmojiPanel(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val recentEmojis = listOf("👍", "❤️", "😊", "🙌", "😂", "🔥", "✨", "💜", "🎉", "👏")
    val smileyEmojis = listOf("😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "😊", "😇", "🥰", "😍", "🤩", "😘")
    val gestureEmojis = listOf("👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "👋", "🤚", "✋", "🖐️", "👏", "🙌", "🤝", "🙏")
    val heartEmojis = listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💕", "💞", "💓", "💗", "💖", "💝")
    val objectEmojis = listOf("🎉", "🎊", "🎁", "🎈", "🔥", "⭐", "🌟", "✨", "💫", "🌈", "☀️", "🌙", "💡", "🎵", "🎶")
    val animalEmojis = listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recent emojis section
            item {
                Text(
                    "Recent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = recentEmojis, onEmojiSelected = onEmojiSelected)
            }

            // Smileys section
            item {
                Text(
                    "Smileys",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = smileyEmojis, onEmojiSelected = onEmojiSelected)
            }

            // Gestures section
            item {
                Text(
                    "Gestures",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = gestureEmojis, onEmojiSelected = onEmojiSelected)
            }

            // Hearts section
            item {
                Text(
                    "Hearts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = heartEmojis, onEmojiSelected = onEmojiSelected)
            }

            // Objects section
            item {
                Text(
                    "Objects",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = objectEmojis, onEmojiSelected = onEmojiSelected)
            }

            // Animals section
            item {
                Text(
                    "Animals",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                EmojiRow(emojis = animalEmojis, onEmojiSelected = onEmojiSelected)
            }
        }
    }
}

/**
 * Horizontal scrolling row of emojis.
 */
@Composable
private fun EmojiRow(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(emojis) { emoji ->
            EmojiButton(emoji = emoji, onClick = { onEmojiSelected(emoji) })
        }
    }
}

/**
 * Individual emoji button with proper rendering.
 */
@Composable
private fun EmojiButton(
    emoji: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

/**
 * Neurodivergent-friendly attachment picker.
 * Shows options for photos, camera, files, location, etc.
 */
@Composable
private fun NeuroAttachmentPicker(
    attachmentState: AttachmentState,
    onDismiss: () -> Unit
) {
    data class AttachmentOption(
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val label: String,
        val color: Color,
        val onClick: () -> Unit
    )

    val options = listOf(
        AttachmentOption(Icons.Filled.Image, "Gallery", Color(0xFF4CAF50)) {
            attachmentState.onPickImage()
            onDismiss()
        },
        AttachmentOption(Icons.Filled.CameraAlt, "Camera", Color(0xFF2196F3)) {
            attachmentState.onTakePhoto()
            onDismiss()
        },
        AttachmentOption(Icons.Outlined.Description, "Document", Color(0xFFFF9800)) {
            attachmentState.onPickDocument()
            onDismiss()
        },
        AttachmentOption(Icons.Filled.LocationOn, "Location", Color(0xFFE91E63)) {
            attachmentState.onShareLocation()
            onDismiss()
        },
        AttachmentOption(Icons.Filled.Person, "Contact", Color(0xFF9C27B0)) {
            attachmentState.onPickContact()
            onDismiss()
        },
        AttachmentOption(Icons.Filled.Headphones, "Audio", Color(0xFF00BCD4)) {
            attachmentState.onPickAudio()
            onDismiss()
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Share",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(options.size) { index ->
                    val option = options[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { option.onClick() }
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = option.color.copy(alpha = 0.15f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    option.icon,
                                    contentDescription = option.label,
                                    tint = option.color,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format recording duration for display.
 */
private fun formatRecordingDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}

/**
 * Neurodivergent-friendly message bubble with reactions.
 * Long-press to add reactions like WhatsApp/Telegram/iMessage.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NeuroMessageItem(
    message: DirectMessage,
    isFromMe: Boolean,
    isDark: Boolean,
    onReport: () -> Unit,
    onRetry: () -> Unit,
    onReact: (emoji: String) -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showReactionPicker by remember { mutableStateOf(false) }

    val bubbleColor = if (isFromMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val textColor = if (isFromMe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    // Get font settings from theme
    val fontSettings = LocalFontSettings.current

    // Get grouped reactions for display
    val groupedReactions = remember(message.reactions) {
        message.getGroupedReactions()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            // Message bubble with long-press for reactions
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 16.dp
                ),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(
                        onClick = { /* Normal tap - could open message options */ },
                        onLongClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            showReactionPicker = true
                        }
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.content,
                        style = NeuroDivergentTypography.messageBody(fontSettings),
                        color = textColor
                    )

                    Spacer(Modifier.height(4.dp))

                    // Timestamp row with delivery status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatMessageTimeString(message.timestamp),
                            style = NeuroDivergentTypography.timestamp(fontSettings),
                            color = textColor.copy(alpha = 0.6f)
                        )
                        if (isFromMe) {
                            when (message.deliveryStatus) {
                                MessageDeliveryStatus.SENDING -> Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = "Sending",
                                    modifier = Modifier.size(12.dp),
                                    tint = textColor.copy(alpha = 0.6f)
                                )
                                MessageDeliveryStatus.SENT -> Icon(
                                    Icons.Filled.Done,
                                    contentDescription = "Sent",
                                    modifier = Modifier.size(12.dp),
                                    tint = textColor.copy(alpha = 0.6f)
                                )
                                MessageDeliveryStatus.FAILED -> Icon(
                                    Icons.Filled.ErrorOutline,
                                    contentDescription = "Failed",
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { onRetry() },
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Reactions display (like iMessage/WhatsApp - shown below the bubble)
            if (groupedReactions.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                MessageReactionsRow(
                    reactions = groupedReactions,
                    isFromMe = isFromMe,
                    onReactionClick = { emoji -> onReact(emoji) }
                )
            }
        }

        // Reaction picker popup (appears above the message like iMessage)
        if (showReactionPicker) {
            ReactionPickerPopup(
                isFromMe = isFromMe,
                onReactionSelected = { emoji ->
                    onReact(emoji)
                    showReactionPicker = false
                },
                onDismiss = { showReactionPicker = false }
            )
        }
    }
}

/**
 * Display reactions on a message bubble (like WhatsApp/iMessage style).
 */
@Composable
private fun MessageReactionsRow(
    reactions: Map<String, List<String>>,
    isFromMe: Boolean,
    onReactionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .offset(x = if (isFromMe) (-8).dp else 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.forEach { (emoji, users) ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onReactionClick(emoji) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 14.sp
                )
                if (users.size > 1) {
                    Text(
                        text = users.size.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Reaction picker popup that appears on long-press (like iMessage/WhatsApp/Telegram).
 */
@Composable
private fun ReactionPickerPopup(
    isFromMe: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "popup-scale"
    )

    Popup(
        alignment = if (isFromMe) Alignment.TopEnd else Alignment.TopStart,
        offset = IntOffset(0, -120),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier
                .scale(animatedScale)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QUICK_REACTIONS.forEach { emoji ->
                    ReactionButton(
                        emoji = emoji,
                        onClick = { onReactionSelected(emoji) }
                    )
                }
                // "More" button to show full emoji picker
                IconButton(
                    onClick = {
                        // For now, just dismiss - could open full emoji picker
                        onDismiss()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "More reactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual reaction button with scale animation on press.
 */
@Composable
private fun ReactionButton(
    emoji: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "reaction-scale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp
        )
    }

    // Reset pressed state after animation
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

/**
 * Format message timestamp for display.
 */
private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}

/**
 * Format message timestamp string for display.
 * Handles ISO format or falls back to showing the raw string.
 */
private fun formatMessageTimeString(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Instant.now()
        val diff = Duration.between(instant, now).toMillis()
        when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> "${diff / 86_400_000}d ago"
        }
    } catch (e: Exception) {
        // Fallback: return as-is or extract time portion
        timestamp.substringAfter("T").substringBefore("Z").take(5)
    }
}

// =============================================================================
// HELPER FUNCTIONS AND OLD COMPONENTS (kept for compatibility)
// =============================================================================

@Composable
private fun ConversationTopBar(
    displayName: String,
    avatar: String,
    isBlocked: Boolean,
    isMuted: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val design = rememberMessagesDesign()
    val topBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(design.avatarSize)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isBlocked || isMuted) {
                        Text(
                            text = if (isBlocked) "Blocked" else "Muted",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBlocked)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                Toast.makeText(context, "More options coming soon! ⚙️", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Outlined.MoreVert, "More options")
            }
        },
        colors = topBarColors
    )
}

@Composable
private fun ConversationBottomBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    showEmojiPanel: Boolean,
    onToggleEmojiPanel: () -> Unit,
    onSend: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    isBlocked: Boolean,
    sensoryMode: SensoryMode,
    energy: Float,
    onEnergyChange: (Float) -> Unit,
    onModeChange: (SensoryMode) -> Unit
) {
    val screenSize = rememberScreenSize()
    val design = rememberMessagesDesign()

    // Center content on larger screens
    val contentModifier = if (design.contentMaxWidth != null) {
        Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = design.contentMaxWidth)
    } else {
        Modifier.fillMaxWidth()
    }

    // Get the navigation bar height to extend the surface behind it
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val navBarHeight = navBarPadding.calculateBottomPadding()

    // Handle IME padding here
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        // Main content - Surface extends behind navbar for seamless blend
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = MessageBarDebug.surfaceElevation.coerceAtLeast(0f).dp
        ) {
            Column(modifier = contentModifier) {
                if (isBlocked) {
                    BlockedUserNotice()
                } else {
                    // Sensory controls - hide in landscape on compact screens
                    if (!(design.isLandscape && screenSize == ScreenSize.COMPACT)) {
                        SensoryModeSwitcher(selected = sensoryMode, onSelect = onModeChange)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calm", style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = energy,
                                onValueChange = onEnergyChange,
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                            )
                            Text("Stim", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Emoji panel
                    AnimatedVisibility(
                        visible = showEmojiPanel,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        QuickEmojiPanel(
                            onEmojiSelected = onEmojiSelected,
                            onDismiss = onToggleEmojiPanel
                        )
                    }

                    // Composer
                    MessageComposer(
                        text = messageText,
                        onTextChange = onMessageTextChange,
                        showEmojiPanel = showEmojiPanel,
                        onToggleEmoji = onToggleEmojiPanel,
                        onSend = onSend,
                        design = design
                    )

                    // Spacer that extends behind the navigation bar - same color as Surface
                    Spacer(modifier = Modifier.height(navBarHeight))
                }
            }
        }
    }
}


@Composable
private fun BlockedUserNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Block,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column {
                Text(
                    "User blocked",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Unblock to send messages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MessageComposer(
    text: String,
    onTextChange: (String) -> Unit,
    showEmojiPanel: Boolean,
    onToggleEmoji: () -> Unit,
    onSend: () -> Unit,
    design: MessagesDesignTokens = rememberMessagesDesign()
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = design.horizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emoji button
            IconButton(
                onClick = onToggleEmoji,
                modifier = Modifier.size(design.touchTarget)
            ) {
                Icon(
                    if (showEmojiPanel) Icons.Filled.Keyboard else Icons.Outlined.EmojiEmotions,
                    contentDescription = if (showEmojiPanel) "Show keyboard" else "Show emoji",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Text field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                maxLines = if (design.isLandscape) 2 else 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                shape = RoundedCornerShape(design.composerCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Send button
            AnimatedVisibility(
                visible = text.trim().isNotEmpty(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FilledIconButton(
                    onClick = onSend,
                    modifier = Modifier.size(design.touchTarget)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    }
}

@Composable
private fun QuickEmojiPanel(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf(
        listOf("😊", "🙂", "😌", "🤗", "💙", "💚", "🧡", "💜"),
        listOf("👍", "👋", "🙏", "✨", "🌟", "💪", "🤝", "👏"),
        listOf("✅", "❌", "❓", "💭", "💡", "🎯", "⏰", "❤️")
    )

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick emoji",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Close", Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            emojis.forEach { row ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(row) { emoji ->
                        Surface(
                            onClick = { onEmojiSelected(emoji) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun EmptyConversationState(
    displayName: String,
    avatar: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatar)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Start your conversation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessagesList(
    messages: List<DirectMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    conversationId: String,
    onReport: (String) -> Unit,
    onRetry: (String, String) -> Unit,
    bubbleColorProvider: (isFromMe: Boolean) -> Color,
    isDark: Boolean
) {
    val design = rememberMessagesDesign()

    // Center content on larger screens
    val contentModifier = if (design.contentMaxWidth != null) {
        Modifier
            .fillMaxSize()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = design.contentMaxWidth)
    } else {
        Modifier.fillMaxSize()
    }

    LazyColumn(
        state = listState,
        modifier = contentModifier,
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = MessageBarDebug.listBottomPadding.coerceAtLeast(0f).dp,
            start = design.horizontalPadding,
            end = design.horizontalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(design.itemSpacing)
    ) {
        items(messages.size, key = { messages[it].id }) { index ->
            val message = messages[index]
            val isFromMe = message.senderId == "me"
            val prev = messages.getOrNull(index - 1)
            val next = messages.getOrNull(index + 1)
            val isFirstInGroup = prev?.senderId != message.senderId
            val isLastInGroup = next?.senderId != message.senderId

            MessageBubble(
                message = message,
                isFromMe = isFromMe,
                isFirstInGroup = isFirstInGroup,
                isLastInGroup = isLastInGroup,
                onReport = { onReport(message.id) },
                onRetry = { onRetry(conversationId, message.id) },
                bubbleColor = bubbleColorProvider(isFromMe),
                design = design,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: DirectMessage,
    isFromMe: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onReport: () -> Unit,
    onRetry: () -> Unit,
    bubbleColor: Color,
    design: MessagesDesignTokens = rememberMessagesDesign(),
    isDark: Boolean = true
) {
    val timeAgo = remember(message.timestamp) {
        formatTimeAgo(message.timestamp)
    }

    // Get font settings from theme
    val fontSettings = LocalFontSettings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        // Android-style bubble with consistent corners
        val shape = RoundedCornerShape(design.bubbleCornerRadius)
        val textColor = bubbleTextColor(isFromMe, isDark)

        Surface(
            shape = shape,
            color = bubbleColor,
            modifier = Modifier.widthIn(max = design.bubbleMaxWidth)
        ) {
            Column(
                modifier = Modifier.padding(design.bubblePadding)
            ) {
                Text(
                    text = message.content,
                    style = NeuroDivergentTypography.messageBody(fontSettings),
                    color = textColor
                )

                // Timestamp inline at bottom right - Android style
                if (isLastInGroup) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            timeAgo,
                            style = NeuroDivergentTypography.timestamp(fontSettings),
                            color = textColor.copy(alpha = 0.7f)
                        )
                        if (isFromMe) {
                            DeliveryStatusIndicator(
                                status = message.deliveryStatus,
                                onRetry = onRetry,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusIndicator(
    status: MessageDeliveryStatus,
    onRetry: () -> Unit,
    textColor: Color = Color.White
) {
    when (status) {
        MessageDeliveryStatus.SENDING -> {
            Icon(
                Icons.Filled.Schedule,
                contentDescription = "Sending",
                modifier = Modifier.size(14.dp),
                tint = textColor.copy(alpha = 0.7f)
            )
        }
        MessageDeliveryStatus.SENT -> {
            Icon(
                Icons.Filled.Done,
                contentDescription = "Sent",
                modifier = Modifier.size(14.dp),
                tint = textColor.copy(alpha = 0.7f)
            )
        }
        MessageDeliveryStatus.FAILED -> {
            Row(
                modifier = Modifier.clickable { onRetry() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF6B6B)
                )
                Text(
                    "Retry",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
    }
}

// =============================================================================
// SENSORY LANE UI
// =============================================================================

@Composable
private fun SensoryLane(
    modifier: Modifier = Modifier,
    mode: SensoryMode,
    energy: Float,
    isDark: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing-animation")
    val breathingAlpha = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (4000 + 4000 * (1f - energy)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing-alpha"
    )

    val laneColor = laneAccent(mode, isDark)
    val gradient = Brush.verticalGradient(
        0f to laneColor.copy(alpha = 0.20f + 0.25f * energy * breathingAlpha.value),
        1f to laneColor.copy(alpha = 0.05f)
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        content = content
    )
}

@Composable
private fun SensoryModeSwitcher(selected: SensoryMode, onSelect: (SensoryMode) -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sensoryModes.forEach { (mode, label) ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                label = { Text(label) },
                leadingIcon = {
                    val dot = when (mode) {
                        SensoryMode.CALM -> "●"
                        SensoryMode.FOCUS -> "◆"
                        SensoryMode.STIM -> "✺"
                    }
                    Text(dot, fontSize = 14.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = laneAccent(mode, isDark).copy(alpha = 0.25f),
                    selectedContainerColor = laneAccent(mode, isDark).copy(alpha = 0.5f),
                    labelColor = if (isDark) Color.White else Color.Black,
                    selectedLabelColor = if (isDark) Color.White else Color.Black
                )
            )
        }
    }
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

private fun formatTimeAgo(timestamp: String?): String {
    if (timestamp == null) return ""
    return try {
        val time = Instant.parse(timestamp)
        val diff = Duration.between(time, Instant.now())
        when {
            diff.toDays() > 6 -> "${diff.toDays() / 7}w"
            diff.toDays() > 0 -> "${diff.toDays()}d"
            diff.toHours() > 0 -> "${diff.toHours()}h"
            diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
            else -> "now"
        }
    } catch (_: Exception) {
        ""
    }
}

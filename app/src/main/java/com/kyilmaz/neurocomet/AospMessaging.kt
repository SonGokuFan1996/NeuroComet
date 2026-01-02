@file:Suppress("UNUSED_PARAMETER")

package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Duration
import java.time.Instant

// =============================================================================
// AOSP-STYLE MESSAGING WITH NEURODIVERGENT ENHANCEMENTS
// =============================================================================

/**
 * Design principles for neurodivergent-friendly AOSP messaging:
 *
 * 1. REDUCED COGNITIVE LOAD
 *    - Clean, minimal interface with clear hierarchy
 *    - Consistent spacing and predictable layouts
 *    - "Calm Mode" colors that reduce visual stress
 *
 * 2. SENSORY CONSIDERATIONS
 *    - Muted, low-contrast backgrounds (not stark white)
 *    - Gentle animations (no jarring transitions)
 *    - Clear visual separation between elements
 *
 * 3. EXECUTIVE FUNCTION SUPPORT
 *    - Unread badges are prominent but not overwhelming
 *    - Quick actions visible without hunting
 *    - Typing indicators are subtle, not anxiety-inducing
 *
 * 4. MOTOR ACCESSIBILITY
 *    - Large touch targets (minimum 48dp)
 *    - Generous spacing between interactive elements
 *    - Long-press actions for detailed options
 */

// --- Color tokens for AOSP-style + neuro-friendly ---
// These are now composable functions to support light/dark mode properly
object AospNeuroTokens {
    // Spacing (static values)
    val touchTarget = 48.dp
    val composerCornerRadius = 24.dp
}

/**
 * Theme-aware colors for AOSP-style messaging
 */
@Composable
fun aospOutgoingBubbleColor(): Color = MaterialTheme.colorScheme.primary

@Composable
fun aospOutgoingBubbleTextColor(): Color = MaterialTheme.colorScheme.onPrimary

@Composable
fun aospIncomingBubbleColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh

@Composable
fun aospIncomingBubbleTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun aospUnreadBadgeColor(): Color = Color(0xFF34A853) // Calm green - consistent across themes

@Composable
fun aospTimestampTextColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun aospComposerBackgroundColor(): Color = MaterialTheme.colorScheme.surfaceContainer

@Composable
fun aospInputFieldBackgroundColor(): Color = MaterialTheme.colorScheme.surface

@Composable
fun aospCalmModeBackgroundColor(): Color = MaterialTheme.colorScheme.background

// =============================================================================
// INBOX SCREEN - AOSP STYLE
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AospInboxScreen(
    conversations: List<Conversation>,
    safetyState: SafetyState,
    onOpenConversation: (String) -> Unit,
    onNewMessage: () -> Unit = {},
    onBack: (() -> Unit)? = null
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
    var showSearch by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter { conv ->
                val otherId = conv.participants.firstOrNull { it != "me" } ?: ""
                val user = MOCK_USERS.find { it.id == otherId }
                val searchText = buildString {
                    append(user?.name ?: otherId)
                    append(" ")
                    append(conv.messages.lastOrNull()?.content ?: "")
                }.lowercase()
                searchText.contains(searchQuery.lowercase())
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (showSearch) {
                // Search mode app bar
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search conversations...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showSearch = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                // Normal app bar - AOSP style
                TopAppBar(
                    title = {
                        Text(
                            text = "Messages",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Normal // AOSP uses lighter weight
                        )
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { /* Settings */ }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        floatingActionButton = {
            // AOSP-style FAB for new message
            FloatingActionButton(
                onClick = onNewMessage,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New message")
            }
        },
        containerColor = aospCalmModeBackgroundColor()
    ) { padding ->
        if (filteredConversations.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) "No results found" else "No conversations yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isBlank()) {
                        Text(
                            text = "Start a conversation with someone!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = filteredConversations,
                    key = { it.id }
                ) { conversation ->
                    AospConversationItem(
                        conversation = conversation,
                        onClick = { onOpenConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AospConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val otherId = conversation.participants.firstOrNull { it != "me" } ?: return
    val user = MOCK_USERS.find { it.id == otherId }
    val avatar = user?.avatarUrl ?: avatarUrl(otherId)
    val displayName = user?.name ?: otherId
    val lastMessage = conversation.messages.lastOrNull()
    val hasUnread = conversation.unreadCount > 0

    val timeAgo = remember(lastMessage?.timestamp) {
        lastMessage?.timestamp?.let {
            try {
                val msgTime = Instant.parse(it)
                val diff = Duration.between(msgTime, Instant.now())
                when {
                    diff.toDays() > 6 -> "${diff.toDays() / 7}w"
                    diff.toDays() > 0 -> "${diff.toDays()}d"
                    diff.toHours() > 0 -> "${diff.toHours()}h"
                    diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
                    else -> "now"
                }
            } catch (_: Exception) { "" }
        } ?: ""
    }

    // AOSP-style list item with neuro-friendly spacing
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar with online indicator slot
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture of $displayName",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )

                // Unread indicator - calm green dot
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(aospUnreadBadgeColor())
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasUnread) aospUnreadBadgeColor() else aospTimestampTextColor()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lastMessage?.content ?: "No messages yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasUnread)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread count badge
                    if (hasUnread && conversation.unreadCount > 1) {
                        Surface(
                            shape = CircleShape,
                            color = aospUnreadBadgeColor(),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// CONVERSATION SCREEN - AOSP STYLE WITH NEURO ENHANCEMENTS (API 36 COMPLIANT)
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AospConversationScreen(
    conversation: Conversation,
    onBack: () -> Unit,
    onSend: (recipientId: String, content: String) -> Unit,
    onReport: (messageId: String) -> Unit,
    onRetryMessage: (convId: String, msgId: String) -> Unit,
    isBlocked: (String) -> Boolean,
    isMuted: (String) -> Boolean
) {
    val recipientId = conversation.participants.firstOrNull { it != "me" } ?: return
    val user = MOCK_USERS.find { it.id == recipientId }
    val avatar = user?.avatarUrl ?: avatarUrl(recipientId)
    val displayName = user?.name ?: recipientId

    val isUserBlocked = isBlocked(recipientId)
    val isUserMuted = isMuted(recipientId)

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Detect keyboard visibility for emoji picker auto-dismiss
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeVisible = remember(imeInsets) {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    LaunchedEffect(imeVisible.value) {
        if (imeVisible.value && showEmojiPicker) showEmojiPicker = false
    }

    // Scroll to bottom on new messages
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val total = conversation.messages.size
            if (total <= 1) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible < total - 2
        }
    }

    // Column layout: TopBar -> Messages (weight 1f) -> Composer (with imePadding)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(aospCalmModeBackgroundColor())
    ) {
        // Top App Bar - with status bar padding built in
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
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
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
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
                        val statusText = when {
                            isUserBlocked -> "Blocked"
                            isUserMuted -> "Muted"
                            else -> null
                        }
                        if (statusText != null) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUserBlocked)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Call */ }) {
                    Icon(Icons.Outlined.Phone, contentDescription = "Call")
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Message list - takes remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (conversation.messages.isEmpty()) {
                // Empty conversation state
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
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Start your conversation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = conversation.messages.size,
                        key = { idx -> conversation.messages[idx].id }
                    ) { idx ->
                        val msg = conversation.messages[idx]
                        val isFromMe = msg.senderId == "me"

                        val prevMsg = conversation.messages.getOrNull(idx - 1)
                        val nextMsg = conversation.messages.getOrNull(idx + 1)
                        val isFirstInGroup = prevMsg?.senderId != msg.senderId
                        val isLastInGroup = nextMsg?.senderId != msg.senderId

                        AospMessageBubble(
                            message = msg,
                            isFromMe = isFromMe,
                            isFirstInGroup = isFirstInGroup,
                            isLastInGroup = isLastInGroup,
                            onReport = { onReport(msg.id) },
                            onRetry = { onRetryMessage(conversation.id, msg.id) }
                        )
                    }
                }

                // Scroll to bottom FAB
                if (showScrollToBottom) {
                    SmallFloatingActionButton(
                        onClick = {
                            if (conversation.messages.isNotEmpty()) {
                                listState.requestScrollToItem(conversation.messages.size - 1)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Scroll to latest")
                    }
                }
            }
        }

        // Bottom bar - composer
        // Handle IME and nav bar insets - union ensures we get the max of both
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.ime.union(WindowInsets.navigationBars)
                )
        ) {
            if (isUserBlocked) {
                // Blocked user notice
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 2.dp
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "User blocked",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Unblock to send messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                // Emoji picker panel
                AnimatedVisibility(
                    visible = showEmojiPicker,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    AospEmojiPanel(
                        onDismiss = { showEmojiPicker = false },
                        onEmoji = { emoji -> messageText += emoji }
                    )
                }

                // Composer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = aospComposerBackgroundColor()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Emoji toggle
                        IconButton(
                            onClick = {
                                if (showEmojiPicker) {
                                    showEmojiPicker = false
                                    keyboardController?.show()
                                } else {
                                    keyboardController?.hide()
                                    showEmojiPicker = true
                                }
                            },
                            modifier = Modifier.size(AospNeuroTokens.touchTarget)
                        ) {
                            Icon(
                                imageVector = if (showEmojiPicker) Icons.Filled.Keyboard else Icons.Outlined.EmojiEmotions,
                                contentDescription = if (showEmojiPicker) "Show keyboard" else "Show emoji",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Text input
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Type a message" },
                            placeholder = {
                                Text(
                                    "Message",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    val trimmed = messageText.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onSend(recipientId, trimmed)
                                        messageText = ""
                                        showEmojiPicker = false
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(AospNeuroTokens.composerCornerRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = aospInputFieldBackgroundColor(),
                                unfocusedContainerColor = aospInputFieldBackgroundColor(),
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Send button
                        val canSend = messageText.trim().isNotEmpty()
                        AnimatedVisibility(
                            visible = canSend,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            FilledIconButton(
                                onClick = {
                                    val trimmed = messageText.trim()
                                    if (trimmed.isNotEmpty()) {
                                        onSend(recipientId, trimmed)
                                        messageText = ""
                                        showEmojiPicker = false
                                    }
                                },
                                modifier = Modifier.size(AospNeuroTokens.touchTarget),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// AOSP-STYLE MESSAGE BUBBLE WITH NEURO ENHANCEMENTS
// =============================================================================

@Composable
private fun AospMessageBubble(
    message: DirectMessage,
    isFromMe: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onReport: () -> Unit,
    onRetry: () -> Unit
) {
    val timeAgo = remember(message.timestamp) {
        try {
            val messageTime = Instant.parse(message.timestamp)
            val diff = Duration.between(messageTime, Instant.now())
            when {
                diff.toDays() > 0 -> "${diff.toDays()}d"
                diff.toHours() > 0 -> "${diff.toHours()}h"
                diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
                else -> "now"
            }
        } catch (_: Exception) { "" }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // AOSP-style bubble with adaptive corners based on grouping
        val bubbleColor = if (isFromMe) aospOutgoingBubbleColor() else aospIncomingBubbleColor()
        val textColor = if (isFromMe) aospOutgoingBubbleTextColor() else aospIncomingBubbleTextColor()

        // Adaptive corner radius for message grouping (smaller corners for mid-group messages)
        val topStart = if (isFromMe) 20.dp else if (isFirstInGroup) 20.dp else 6.dp
        val topEnd = if (isFromMe) if (isFirstInGroup) 20.dp else 6.dp else 20.dp
        val bottomStart = if (isFromMe) 20.dp else if (isLastInGroup) 20.dp else 6.dp
        val bottomEnd = if (isFromMe) if (isLastInGroup) 20.dp else 6.dp else 20.dp

        val shape = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart
        )

        Surface(
            shape = shape,
            color = bubbleColor,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = { /* no-op */ },
                    onLongClick = { if (!isFromMe) onReport() }
                )
                .semantics {
                    contentDescription = if (isFromMe) "Sent message" else "Received message"
                }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    lineHeight = 22.sp
                )
            }
        }

        // Timestamp and delivery status (only on last message of group)
        if (isLastInGroup) {
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = aospTimestampTextColor()
                )
                if (isFromMe) {
                    when (message.deliveryStatus) {
                        MessageDeliveryStatus.SENDING -> {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = "Sending",
                                modifier = Modifier.size(14.dp),
                                tint = aospTimestampTextColor()
                            )
                        }
                        MessageDeliveryStatus.FAILED -> {
                            Surface(
                                onClick = onRetry,
                                color = Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ErrorOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Tap to retry",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        MessageDeliveryStatus.SENT -> {
                            Icon(
                                Icons.Filled.Done,
                                contentDescription = "Sent",
                                modifier = Modifier.size(14.dp),
                                tint = aospTimestampTextColor()
                            )
                        }
                    }
                }
            }
        }
    }
}


// =============================================================================
// NEURO-FRIENDLY EMOJI PANEL
// =============================================================================

@Composable
private fun AospEmojiPanel(
    onDismiss: () -> Unit,
    onEmoji: (String) -> Unit
) {
    // Curated emoji set with neurodivergent-friendly options
    // Includes calming emojis, stim-related, and clear communication aids
    val emojiCategories = listOf(
        "😊" to listOf("😊", "🙂", "😌", "🤗", "💙", "💚", "🧡", "💜"),
        "👍" to listOf("👍", "👋", "🙏", "✨", "🌟", "💪", "🤝", "👏"),
        "🎯" to listOf("✅", "❌", "❓", "💭", "💡", "🎯", "⏰", "📍"),
        "🌿" to listOf("🌿", "🌸", "☀️", "🌙", "⭐", "🌈", "🔥", "❤️")
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick emoji",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Emoji rows organized by category
            emojiCategories.forEach { (_, emojis) ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { emoji ->
                        Surface(
                            onClick = { onEmoji(emoji) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}


package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Duration
import java.time.Instant

/**
 * DM Conversation UI (v2):
 * - Stable Scaffold layout
 * - Bottom composer always above IME
 * - Simple emoji panel (lightweight) that inserts into text
 * - Production-ish message list + bubble reuse (uses existing NeuroMessageBubble)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DmConversationScreenV2(
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

    val isUserBlocked = isBlocked(recipientId)
    val isUserMuted = isMuted(recipientId)

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Auto-hide emoji panel when the keyboard appears.
    // Keep this in sync with how the rest of the app detects IME visibility.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible) {
        if (imeVisible && showEmojiPicker) showEmojiPicker = false
    }

    // Scroll to bottom on new messages
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    val showJumpToLatest by remember {
        derivedStateOf {
            val total = conversation.messages.size
            if (total <= 1) return@derivedStateOf false
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible < total - 2
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(avatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text(
                                text = user?.name ?: recipientId,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val status = when {
                                isUserBlocked -> "Blocked"
                                isUserMuted -> "Muted"
                                else -> "Direct message"
                            }
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUserBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Conversation options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Profile") },
                                onClick = {
                                    showMenu = false
                                    android.widget.Toast.makeText(
                                        context,
                                        "Profile view coming soon! 👤",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = { Icon(Icons.Filled.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Search in Chat") },
                                onClick = {
                                    showMenu = false
                                    android.widget.Toast.makeText(
                                        context,
                                        "Chat search coming soon! 🔍",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = { Icon(Icons.Filled.Search, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Mute Notifications") },
                                onClick = {
                                    showMenu = false
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isUserMuted) "Notifications unmuted 🔔" else "Notifications muted 🔕",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isUserMuted) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                                        null
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (isUserBlocked) "Unblock User" else "Block User") },
                                onClick = {
                                    showMenu = false
                                    android.widget.Toast.makeText(
                                        context,
                                        if (isUserBlocked) "User unblocked ✓" else "User blocked ✗",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = { Icon(Icons.Filled.Block, null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // IME padding on the wrapper, navbar padding inside content for seamless blend
            val bottomBarModifier = Modifier
                .fillMaxWidth()
                .imePadding()

            if (isUserBlocked) {
                Surface(
                    modifier = bottomBarModifier,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Messaging is disabled",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "You blocked this user. Unblock to send messages.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                Surface(
                    modifier = bottomBarModifier,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        AnimatedVisibility(
                            visible = showEmojiPicker,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            SimpleEmojiPanel(
                                onDismiss = { showEmojiPicker = false },
                                onEmoji = { emoji ->
                                    messageText += emoji
                                }
                            )
                        }

                        DmComposerV2(
                            text = messageText,
                            onTextChange = { messageText = it },
                            showEmojiPicker = showEmojiPicker,
                            onToggleEmoji = {
                                if (showEmojiPicker) {
                                    showEmojiPicker = false
                                    keyboardController?.show()
                                } else {
                                    keyboardController?.hide()
                                    showEmojiPicker = true
                                }
                            },
                            onSend = {
                                val trimmed = messageText.trim()
                                if (trimmed.isNotEmpty()) {
                                    onSend(recipientId, trimmed)
                                    messageText = ""
                                    showEmojiPicker = false
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (conversation.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Say hi 👋",
                        style = MessageTextStyles.headerTitle
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 96.dp // keep list clear of composer/navigation
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = conversation.messages.size,
                        key = { idx -> conversation.messages[idx].id }
                    ) { idx ->
                        val msg = conversation.messages[idx]
                        MessageBubbleV2(
                            message = msg,
                            isFromMe = msg.senderId == "me",
                            onReport = { onReport(msg.id) },
                            onRetry = { onRetryMessage(conversation.id, msg.id) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showJumpToLatest,
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 88.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (conversation.messages.isNotEmpty()) {
                            listState.requestScrollToItem(conversation.messages.size - 1)
                        }
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Scroll to latest")
                }
            }
        }
    }
}

@Composable
private fun DmComposerV2(
    text: String,
    onTextChange: (String) -> Unit,
    showEmojiPicker: Boolean,
    onToggleEmoji: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onToggleEmoji,
                modifier = Modifier.size(MessagesTokens.touchTarget)
            ) {
                Icon(
                    imageVector = if (showEmojiPicker) Icons.Filled.Keyboard else Icons.Filled.EmojiEmotions,
                    contentDescription = if (showEmojiPicker) "Keyboard" else "Emoji"
                )
            }

            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Message input" },
                placeholder = {
                    Text(
                        "Message",
                        style = MessageTextStyles.inputText.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                },
                textStyle = MessageTextStyles.inputText,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            FilledIconButton(
                onClick = onSend,
                enabled = text.trim().isNotEmpty(),
                modifier = Modifier.size(MessagesTokens.touchTarget)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun SimpleEmojiPanel(
    onDismiss: () -> Unit,
    onEmoji: (String) -> Unit
) {
    val emojis = listOf("😀", "😁", "😂", "😊", "😍", "👍", "❤️", "🙏", "🔥", "🎉", "🤝", "💙")

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Emoji", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close emoji panel")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojis.forEach { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        onClick = { onEmoji(emoji) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MessageBubbleV2(
    message: DirectMessage,
     isFromMe: Boolean,
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
         } catch (_: Exception) {
             ""
         }
     }

     Column(
         modifier = Modifier.fillMaxWidth(),
         horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
     ) {
         val iMessageSent = Color(0xFF0B93F6)
         val iMessageReceived = Color(0xFFE5E5EA)
         val finalBubbleColor = if (isFromMe) iMessageSent else iMessageReceived
         val finalTextColor = if (isFromMe) Color.White else Color.Black
         val shape = if (isFromMe) {
             RoundedCornerShape(topStart = 18.dp, topEnd = 22.dp, bottomEnd = 4.dp, bottomStart = 18.dp)
         } else {
             RoundedCornerShape(topStart = 22.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
         }

         Surface(
             shape = shape,
             color = finalBubbleColor,
             modifier = Modifier
                .fillMaxWidth(0.78f)
                 .combinedClickable(
                     onClick = { /* no-op */ },
                     onLongClick = { if (!isFromMe) onReport() }
                 )
                 .semantics {
                     contentDescription = if (isFromMe) "Sent message" else "Received message"
                 }
         ) {
             Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                 Text(
                     text = message.content,
                     style = MaterialTheme.typography.bodyLarge,
                     color = finalTextColor
                 )
             }
         }

         Spacer(Modifier.height(4.dp))
         Row(
             verticalAlignment = Alignment.CenterVertically,
             horizontalArrangement = Arrangement.spacedBy(6.dp)
         ) {
             Text(
                 text = timeAgo,
                 style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
             )
             if (isFromMe) {
                 when (message.deliveryStatus) {
                     MessageDeliveryStatus.SENDING -> Text(
                         text = "Sending…",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                     )
                     MessageDeliveryStatus.FAILED -> Text(
                         text = "Retry",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.error,
                         fontWeight = FontWeight.SemiBold,
                         modifier = Modifier.clickable { onRetry() }
                     )
                     MessageDeliveryStatus.SENT -> Text(
                         text = "Sent",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                     )
                 }
             }
         }
     }
 }

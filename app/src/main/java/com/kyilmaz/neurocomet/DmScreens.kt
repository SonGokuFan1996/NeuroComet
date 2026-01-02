package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Duration
import java.time.Instant
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch

private val DM_REACTIONS = listOf("👍", "❤️", "😊", "🙌")

// ============================================================================
// EMOJI PICKER - Production-Ready COMPONENT
// ============================================================================

/**
 * Emoji categories for the picker
 */
private enum class EmojiCategory(val icon: String, val label: String) {
    RECENT("🕐", "Recent"),
    SMILEYS("😀", "Smileys"),
    PEOPLE("👋", "People"),
    NATURE("🌿", "Nature"),
    FOOD("🍕", "Food"),
    ACTIVITIES("⚽", "Activities"),
    TRAVEL("✈️", "Travel"),
    OBJECTS("💡", "Objects"),
    SYMBOLS("❤️", "Symbols")
}

/**
 * Emoji data organized by category
 */
private val EMOJI_DATA = mapOf(
    EmojiCategory.SMILEYS to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
        "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
        "🥲", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫",
        "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬",
        "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢",
        "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸",
        "😎", "🤓", "🧐", "😕", "😟", "🙁", "☹️", "😮", "😯", "😲",
        "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱",
        "😖", "😣", "😞", "😓", "😩", "😫", "🥱", "😤", "😡", "😠"
    ),
    EmojiCategory.PEOPLE to listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
        "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍",
        "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝",
        "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂",
        "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴", "👀", "👁️", "👅",
        "👄", "👶", "🧒", "👦", "👧", "🧑", "👱", "👨", "🧔", "👩"
    ),
    EmojiCategory.NATURE to listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨",
        "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤",
        "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱",
        "🐛", "🦋", "🐌", "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗",
        "🌸", "💮", "🏵️", "🌹", "🥀", "🌺", "🌻", "🌼", "🌷", "🌱",
        "🪴", "🌲", "🌳", "🌴", "🌵", "🌾", "🌿", "☘️", "🍀", "🍁"
    ),
    EmojiCategory.FOOD to listOf(
        "🍇", "🍈", "🍉", "🍊", "🍋", "🍌", "🍍", "🥭", "🍎", "🍏",
        "🍐", "🍑", "🍒", "🍓", "🫐", "🥝", "🍅", "🫒", "🥥", "🥑",
        "🍆", "🥔", "🥕", "🌽", "🌶️", "🫑", "🥒", "🥬", "🥦", "🧄",
        "🍞", "🥐", "🥖", "🫓", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖",
        "🍕", "🍔", "🍟", "🌭", "🥪", "🌮", "🌯", "🫔", "🥙", "🧆",
        "🍜", "🍝", "🍣", "🍱", "🍛", "🍚", "☕", "🍵", "🧃", "🥤"
    ),
    EmojiCategory.ACTIVITIES to listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
        "🪀", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🪃", "🥅", "⛳",
        "🎯", "🪁", "🎮", "🕹️", "🎲", "🧩", "♟️", "🎭", "🎨", "🎬",
        "🎤", "🎧", "🎼", "🎹", "🥁", "🪘", "🎷", "🎺", "🪗", "🎸",
        "🎻", "🎪", "🤹", "🏋️", "🤼", "🤸", "⛹️", "🤺", "🏇", "⛷️"
    ),
    EmojiCategory.TRAVEL to listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴", "🛹", "🛼",
        "✈️", "🛫", "🛬", "🛩️", "💺", "🚀", "🛸", "🚁", "🛶", "⛵",
        "🚤", "🛥️", "🛳️", "⛴️", "🚢", "🗼", "🗽", "🗿", "🏰", "🏯",
        "🏟️", "🎡", "🎢", "🎠", "⛲", "⛱️", "🏖️", "🏝️", "🏜️", "🌋"
    ),
    EmojiCategory.OBJECTS to listOf(
        "💡", "🔦", "🏮", "🪔", "📱", "💻", "🖥️", "🖨️", "⌨️", "🖱️",
        "💾", "💿", "📀", "📷", "📸", "📹", "🎥", "📽️", "📞", "☎️",
        "📺", "📻", "🎙️", "⏰", "⌚", "⏱️", "🔋", "🔌", "💎", "💰",
        "💳", "💵", "💴", "💶", "💷", "🧲", "🔧", "🪛", "🔩", "⚙️",
        "🧱", "🪵", "🔨", "⛏️", "🪓", "🔪", "🗡️", "⚔️", "🔫", "🛡️"
    ),
    EmojiCategory.SYMBOLS to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
        "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
        "⭐", "🌟", "💫", "✨", "⚡", "🔥", "💥", "☀️", "🌙", "⭕",
        "✅", "❌", "❓", "❗", "💯", "🔴", "🟠", "🟡", "🟢", "🔵"
    )
)

/**
 * Production-ready emoji picker composable
 */
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    recentEmojis: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EmojiCategory.SMILEYS) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emoji",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Category tabs
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Recent category (if has recent emojis)
                if (recentEmojis.isNotEmpty()) {
                    item {
                        EmojiCategoryTab(
                            category = EmojiCategory.RECENT,
                            isSelected = selectedCategory == EmojiCategory.RECENT,
                            onClick = { selectedCategory = EmojiCategory.RECENT }
                        )
                    }
                }

                items(EmojiCategory.entries.filter { it != EmojiCategory.RECENT }.size) { index ->
                    val category = EmojiCategory.entries.filter { it != EmojiCategory.RECENT }[index]
                    EmojiCategoryTab(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Emoji grid
            val emojis = if (selectedCategory == EmojiCategory.RECENT) {
                recentEmojis
            } else {
                EMOJI_DATA[selectedCategory] ?: emptyList()
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(emojis.size) { index ->
                    EmojiItem(
                        emoji = emojis[index],
                        onClick = { onEmojiSelected(emojis[index]) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiCategoryTab(
    category: EmojiCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EmojiItem(
    emoji: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UnusedParameter")
fun DmInboxScreen(
    conversations: List<Conversation>,
    safetyState: SafetyState,
    onOpenConversation: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }

    // Check if DMs are blocked by parental controls
    val restriction = shouldBlockFeature(parentalState, BlockableFeature.DMS)

    if (restriction != null) {
        ParentalBlockedScreen(
            restrictionType = restriction,
            featureName = "Direct Messages"
        )
        return
    }

    var query by remember { mutableStateOf("") }
    var showUnreadOnly by remember { mutableStateOf(false) }

    val filtered = remember(conversations, query, showUnreadOnly) {
        conversations
            .asSequence()
            .filter { if (showUnreadOnly) it.unreadCount > 0 else true }
            .filter { conv ->
                if (query.isBlank()) return@filter true
                val other = conv.participants.firstOrNull { it != "me" } ?: ""
                val user = MOCK_USERS.find { it.id == other }
                val haystack = buildString {
                    append(other)
                    append(' ')
                    append(user?.name ?: "")
                    append(' ')
                    append(conv.messages.lastOrNull()?.content ?: "")
                }
                haystack.contains(query, ignoreCase = true)
            }
            .toList()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.nav_messages),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                conversations.isEmpty() -> "No conversations"
                                filtered.size == conversations.size -> "${conversations.size} conversation${if (conversations.size != 1) "s" else ""}"
                                else -> "${filtered.size} of ${conversations.size}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Start a new conversation from someone's profile! 💬",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "New Message")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search + filters (low-cognitive-load controls)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MessagesTokens.pagePadding)
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Search conversations" },
                    shape = RoundedCornerShape(MessagesTokens.cornerLarge),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    placeholder = { Text("Search messages, names…") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* no-op */ })
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showUnreadOnly,
                        onClick = { showUnreadOnly = false },
                        label = { Text("All") },
                        leadingIcon = {
                            if (!showUnreadOnly) Icon(Icons.Filled.Done, contentDescription = null)
                        }
                    )
                    FilterChip(
                        selected = showUnreadOnly,
                        onClick = { showUnreadOnly = true },
                        label = { Text("Unread") },
                        leadingIcon = {
                            if (showUnreadOnly) Icon(Icons.Filled.Done, contentDescription = null)
                        }
                    )
                }
            }

            when {
                conversations.isEmpty() -> {
                    MessagesEmptyState(
                        title = "No conversations yet",
                        subtitle = "When you message someone, it will show up here.",
                        hint = "Tip: Try Explore → tap a profile → Message (mock)."
                    )
                }

                filtered.isEmpty() -> {
                    MessagesEmptyState(
                        title = "No results",
                        subtitle = "Try a different search or switch back to All.",
                        hint = if (showUnreadOnly) "You’re filtering to Unread." else null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MessagesTokens.pagePadding,
                            end = MessagesTokens.pagePadding,
                            bottom = 12.dp,
                            top = 4.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(MessagesTokens.itemSpacing)
                    ) {
                        items(items = filtered, key = { it.id }) { conversation ->
                            NeuroConversationListItem(
                                conversation = conversation,
                                onClick = { onOpenConversation(conversation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesEmptyState(
    title: String,
    subtitle: String,
    hint: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NeuroConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val otherParticipant = conversation.participants.firstOrNull { it != "me" } ?: "Unknown"
    val user = MOCK_USERS.find { it.id == otherParticipant }
    val avatar = user?.avatarUrl ?: avatarUrl(otherParticipant)

    val lastMessage = conversation.messages.lastOrNull()
    val hasUnread = conversation.unreadCount > 0

    val timeAgo = remember(conversation.lastMessageTimestamp) {
        try {
            val messageTime = Instant.parse(conversation.lastMessageTimestamp)
            val now = Instant.now()
            val diff = Duration.between(messageTime, now)
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

    val title = user?.name ?: otherParticipant
    val preview = when {
        lastMessage == null -> "No messages"
        lastMessage.moderationStatus == ModerationStatus.BLOCKED -> "Message blocked"
        lastMessage.moderationStatus == ModerationStatus.FLAGGED -> "Message flagged"
        lastMessage.senderId == "me" -> "You: ${lastMessage.content}"
        else -> lastMessage.content
    }

    val container = if (hasUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    else MaterialTheme.colorScheme.surfaceContainerHigh

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(MessagesTokens.cornerLarge),
        color = container,
        tonalElevation = if (hasUnread) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(MessagesTokens.avatarSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MessageTextStyles.username,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeAgo,
                        style = MessageTextStyles.timestamp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = preview,
                    style = MessageTextStyles.messagePreview,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (hasUnread) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Unread") },
                        leadingIcon = { Icon(Icons.Filled.MarkChatUnread, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// NOTE: legacy ConversationListItem left in file for compatibility with older call sites.
// It is no longer used by the redesigned inbox.
@Composable
private fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val otherParticipant = conversation.participants.firstOrNull { it != "me" } ?: "Unknown"
    val user = MOCK_USERS.find { it.id == otherParticipant }
    val avatar = user?.avatarUrl ?: avatarUrl(otherParticipant)
    val lastMessage = conversation.messages.lastOrNull()
    val hasUnread = conversation.unreadCount > 0

    val timeAgo = remember(conversation.lastMessageTimestamp) {
        try {
            val messageTime = Instant.parse(conversation.lastMessageTimestamp)
            val now = Instant.now()
            val diff = Duration.between(messageTime, now)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (hasUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user?.name ?: otherParticipant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            val messagePreview = when {
                lastMessage == null -> "No messages"
                lastMessage.moderationStatus == ModerationStatus.BLOCKED -> "[Message blocked]"
                lastMessage.moderationStatus == ModerationStatus.FLAGGED -> "[Message flagged for review]"
                lastMessage.senderId == "me" -> "You: ${lastMessage.content}"
                else -> lastMessage.content
            }

            Text(
                text = messagePreview,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    HorizontalDivider(modifier = Modifier.padding(start = 84.dp))
}

private fun dmDayLabel(isoInstant: String): String {
    return try {
        val messageTime = Instant.parse(isoInstant)
        val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
        val msgDate = java.time.LocalDateTime.ofInstant(messageTime, java.time.ZoneId.systemDefault()).toLocalDate()
        val days = java.time.temporal.ChronoUnit.DAYS.between(msgDate, today)
        when (days) {
            0L -> "Today"
            1L -> "Yesterday"
            else -> msgDate.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }
        }
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun DateSeparator(label: String) {
    if (label.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(999.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * NeuroComet Messaging Screen
 *
 * A unique blend of:
 * - Google Messages: Material 3 design, smooth animations, modern feel
 * - AOSP Messages: Clean simplicity, efficient layout, no bloat
 * - Neurodivergent-friendly: Calm colors, clear hierarchy, reduced cognitive load
 *
 * Key features:
 * - Gradient accent header inspired by infinity symbol colors
 * - AOSP-style minimal bubble design with soft edges
 * - Google Messages-style floating compose bar
 * - Sensory-friendly color palette with customizable intensity
 * - Clear timestamp grouping for time orientation
 * - Calm mode integration for overwhelm prevention
 */
@Composable
@Suppress("UnusedParameter")
fun DmConversationScreen(
    conversation: Conversation,
    safetyState: SafetyState,
    onBack: () -> Unit,
    onSend: (recipientId: String, content: String) -> Unit,
    onReport: (messageId: String) -> Unit,
    onRetryMessage: (convId: String, msgId: String) -> Unit,
    isBlocked: (String) -> Boolean,
    isMuted: (String) -> Boolean,
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onMuteUser: (String) -> Unit,
    onUnmuteUser: (String) -> Unit
) {
    val recipientId = conversation.participants.firstOrNull { it != "me" } ?: return
    val user = MOCK_USERS.find { it.id == recipientId }
    val avatar = user?.avatarUrl ?: avatarUrl(recipientId)
    val context = LocalContext.current

    val isUserBlocked = isBlocked(recipientId)
    val isUserMuted = isMuted(recipientId)

    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var recentEmojis by remember { mutableStateOf(listOf("👍", "❤️", "😊", "🙌", "😂", "🔥", "✨", "💜")) }

    // Call state
    var showCallDialog by remember { mutableStateOf(false) }
    val currentCall = MockCallManager.currentCall
    val callState = MockCallManager.callState
    val callDuration = MockCallManager.callDuration

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Neurodivergent color palette - calming gradients
    val primaryGradient = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    // Scroll to bottom on new messages
    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    // Close emoji picker when keyboard appears
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    LaunchedEffect(imeVisible) {
        if (imeVisible && showEmojiPicker) showEmojiPicker = false
    }

    // Jump to latest visibility
    val showJumpToLatest by remember {
        derivedStateOf {
            val total = conversation.messages.size
            if (total <= 2) false
            else (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) < total - 2
        }
    }

    // Selection mode for multi-select
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }

    // Theme-aware colors
    val backgroundColor = MaterialTheme.colorScheme.surface
    val sentBubbleColor = MaterialTheme.colorScheme.primaryContainer
    val receivedBubbleColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val inputBarColor = if (isUserBlocked)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                            text = user?.name ?: recipientId,
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
                            recipientName = user?.name ?: recipientId,
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
                            recipientName = user?.name ?: recipientId,
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
                                text = { Text(if (isUserBlocked) "Unblock" else "Block") },
                                onClick = {
                                    if (isUserBlocked) onUnblockUser(recipientId) else onBlockUser(recipientId)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Block, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isUserMuted) "Unmute" else "Mute") },
                                onClick = {
                                    if (isUserMuted) onUnmuteUser(recipientId) else onMuteUser(recipientId)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isUserMuted) Icons.AutoMirrored.Filled.VolumeUp
                                        else Icons.AutoMirrored.Filled.VolumeOff,
                                        null
                                    )
                                }
                            )
                            HorizontalDivider()
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
                        // Emoji picker with smooth animation
                        AnimatedVisibility(
                            visible = showEmojiPicker,
                            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                        ) {
                            EmojiPicker(
                                onEmojiSelected = { emoji ->
                                    messageText += emoji
                                    recentEmojis = (listOf(emoji) + recentEmojis.filter { it != emoji }).take(20)
                                },
                                onDismiss = { showEmojiPicker = false },
                                recentEmojis = recentEmojis
                            )
                        }

                        // Input row - blend of Google Messages & AOSP
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Attachment button - Google Messages style
                            IconButton(
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Media attachments coming soon! 📎",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Attach",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
                                            if (showEmojiPicker) {
                                                showEmojiPicker = false
                                                keyboardController?.show()
                                            } else {
                                                keyboardController?.hide()
                                                showEmojiPicker = true
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            if (showEmojiPicker) Icons.Filled.Keyboard else Icons.Filled.EmojiEmotions,
                                            contentDescription = if (showEmojiPicker) "Keyboard" else "Emoji",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Text input
                                    BasicTextField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 10.dp),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        maxLines = 4,
                                        decorationBox = { innerTextField ->
                                            Box {
                                                if (messageText.isEmpty()) {
                                                    Text(
                                                        "Message",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )

                                    // Camera button when empty
                                    if (messageText.isEmpty()) {
                                        IconButton(
                                            onClick = {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Camera integration coming soon! 📸",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.CameraAlt,
                                                contentDescription = "Camera",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Send/Mic button with animation
                            val hasText = messageText.isNotBlank()
                            val buttonColor by animateColorAsState(
                                if (hasText) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                label = "sendButtonColor"
                            )

                            FilledIconButton(
                                onClick = {
                                    if (hasText) {
                                        onSend(recipientId, messageText)
                                        messageText = ""
                                        showEmojiPicker = false
                                    }
                                },
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = buttonColor,
                                    contentColor = if (hasText) MaterialTheme.colorScheme.onPrimary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic,
                                    contentDescription = if (hasText) "Send" else "Voice message",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        "Start chatting with ${user?.name ?: "them"}",
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
                    var lastDay: String? = null
                    items(conversation.messages, key = { it.id }) { message ->
                        // Date separators
                        val day = dmDayLabel(message.timestamp)
                        if (day.isNotBlank() && day != lastDay) {
                            NeuroDateChip(day)
                            lastDay = day
                        }

                        val isSelected = message.id in selectedIds
                        val isFromMe = message.senderId == "me"

                        NeuroMessageBubble(
                            message = message,
                            isFromMe = isFromMe,
                            isSelected = isSelected,
                            selectionMode = selectionMode,
                            onToggleSelected = {
                                if (!selectionMode) selectionMode = true
                                if (isSelected) selectedIds.remove(message.id) else selectedIds.add(message.id)
                                if (selectedIds.isEmpty()) selectionMode = false
                            },
                            onEnterSelectionMode = {
                                selectionMode = true
                                if (!selectedIds.contains(message.id)) selectedIds.add(message.id)
                            },
                            onReport = { onReport(message.id) },
                            onRetry = { onRetryMessage(conversation.id, message.id) },
                            onCopy = { clipboard.setText(AnnotatedString(message.content)) },
                            onReact = { }
                        )
                    }
                }

                // Scroll to bottom FAB - Google Messages style
                AnimatedVisibility(
                    visible = showJumpToLatest,
                    enter = scaleIn(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                    exit = scaleOut(spring(stiffness = Spring.StiffnessMedium)) + fadeOut(),
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
 * Neurodivergent-friendly date chip with calming design.
 */
@Composable
private fun NeuroDateChip(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NeuroMessageBubble(
    message: DirectMessage,
    isFromMe: Boolean,
    isSelected: Boolean,
    selectionMode: Boolean,
    onToggleSelected: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onReport: () -> Unit,
    onRetry: () -> Unit,
    onCopy: () -> Unit,
    onReact: (String) -> Unit
) {
    // Convert new List<MessageReaction> to Map<String, Int> for display
    var reactionsMap by remember(message.id, message.reactions) {
        mutableStateOf(message.getGroupedReactions().mapValues { it.value.size })
    }
    var menuOpen by remember(message.id) { mutableStateOf(false) }
    var reactMenuOpen by remember(message.id) { mutableStateOf(false) }

    val bubbleColor = when {
        message.moderationStatus == ModerationStatus.BLOCKED -> MaterialTheme.colorScheme.errorContainer
        message.moderationStatus == ModerationStatus.FLAGGED -> MaterialTheme.colorScheme.tertiaryContainer
        isFromMe -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val onBubbleColor = when {
        message.moderationStatus == ModerationStatus.BLOCKED -> MaterialTheme.colorScheme.onErrorContainer
        message.moderationStatus == ModerationStatus.FLAGGED -> MaterialTheme.colorScheme.onTertiaryContainer
        isFromMe -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val selectionStroke = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(MessagesTokens.cornerLarge),
                color = bubbleColor,
                border = selectionStroke,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .combinedClickable(
                        onClick = {
                            if (selectionMode) onToggleSelected()
                        },
                        onLongClick = {
                            if (selectionMode) {
                                onToggleSelected()
                            } else {
                                // Long-press enters selection mode (Google Messages-like)
                                onEnterSelectionMode()
                            }
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (selectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelected() }
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        when (message.moderationStatus) {
                            ModerationStatus.BLOCKED -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Message blocked",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = onBubbleColor
                                    )
                                }
                                Text(
                                    text = "This message violated community guidelines.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onBubbleColor.copy(alpha = 0.9f)
                                )
                            }

                            ModerationStatus.FLAGGED -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = message.content,
                                        style = MessageTextStyles.messageBody,
                                        color = onBubbleColor
                                    )
                                }
                            }

                            ModerationStatus.CLEAN -> {
                                Text(
                                    text = message.content,
                                    style = MessageTextStyles.messageBody,
                                    color = onBubbleColor
                                )
                            }
                        }

                        if (reactionsMap.isNotEmpty()) {
                            ReactionChips(reactionsMap)
                        }

                        if (message.deliveryStatus == MessageDeliveryStatus.FAILED) {
                            AssistChip(
                                onClick = onRetry,
                                label = { Text("Tap to retry") },
                                leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            // Overflow menu (tap on a small icon area)
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier
                    .align(if (isFromMe) Alignment.TopStart else Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Message options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = {
                    menuOpen = false
                    reactMenuOpen = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = {
                        onCopy()
                        menuOpen = false
                    },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) }
                )

                DropdownMenuItem(
                    text = { Text("React…") },
                    onClick = {
                        reactMenuOpen = true
                    },
                    leadingIcon = { Icon(Icons.Filled.EmojiEmotions, contentDescription = null) }
                )

                if (!isFromMe && message.moderationStatus != ModerationStatus.BLOCKED) {
                    DropdownMenuItem(
                        text = { Text("Report") },
                        onClick = {
                            onReport()
                            menuOpen = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) }
                    )
                }

                if (isFromMe && message.deliveryStatus == MessageDeliveryStatus.FAILED) {
                    DropdownMenuItem(
                        text = { Text("Retry") },
                        onClick = {
                            onRetry()
                            menuOpen = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) }
                    )
                }
            }

            DropdownMenu(
                expanded = reactMenuOpen,
                onDismissRequest = {
                    reactMenuOpen = false
                    menuOpen = false
                }
            ) {
                DM_REACTIONS.forEach { emoji ->
                    DropdownMenuItem(
                        text = { Text(emoji) },
                        onClick = {
                            // Update local state for immediate feedback
                            val currentCount = reactionsMap[emoji] ?: 0
                            reactionsMap = reactionsMap.toMutableMap().apply {
                                this[emoji] = currentCount + 1
                            }
                            onReact(emoji)
                            reactMenuOpen = false
                            menuOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        val timeAgo = remember(message.timestamp) {
            try {
                val messageTime = Instant.parse(message.timestamp)
                val now = Instant.now()
                val diff = Duration.between(messageTime, now)
                when {
                    diff.toDays() > 0 -> "${diff.toDays()}d ago"
                    diff.toHours() > 0 -> "${diff.toHours()}h ago"
                    diff.toMinutes() > 0 -> "${diff.toMinutes()}m ago"
                    else -> "Just now"
                }
            } catch (_: Exception) {
                ""
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isFromMe) {
                Icon(
                    imageVector = when (message.deliveryStatus) {
                        MessageDeliveryStatus.SENDING -> Icons.Filled.Schedule
                        MessageDeliveryStatus.SENT -> Icons.Filled.Done
                        MessageDeliveryStatus.FAILED -> Icons.Filled.Error
                    },
                    contentDescription = message.deliveryStatus.name,
                    modifier = Modifier.size(14.dp),
                    tint = when (message.deliveryStatus) {
                        MessageDeliveryStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun ReactionChips(reactions: Map<String, Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.entries.filter { it.value > 0 }.forEach { (emoji, count) ->
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji)
                    Spacer(Modifier.width(4.dp))
                    Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Neurodivergent-friendly message input bar.
 * IME padding is applied by the caller.
 */
@Composable
private fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    showEmojiPicker: Boolean,
    onEmojiToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showNavBarSpacer: Boolean = true  // Whether to show the navbar spacer
) {
    val hasText = messageText.isNotBlank()

    // Just the content - no Surface wrapper here since parent handles it
    Column(modifier = modifier.fillMaxWidth()) {
        // Content row - compact padding for balanced dimensions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onEmojiToggle,
                modifier = Modifier.size(MessagesTokens.touchTarget)
            ) {
                Icon(
                    imageVector = if (showEmojiPicker) Icons.Filled.Keyboard else Icons.Filled.EmojiEmotions,
                    contentDescription = if (showEmojiPicker) "Keyboard" else "Emoji",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MessagesTokens.touchTarget, max = 160.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(MessagesTokens.cornerLarge)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    maxLines = 6,
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (messageText.isEmpty()) {
                                Text(
                                    text = "Message",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            inner()
                        }
                    }
                )
            }

            FilledIconButton(
                onClick = onSendClick,
                enabled = hasText,
                modifier = Modifier.size(MessagesTokens.touchTarget)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Spacer that extends behind the navigation bar - uses windowInsets for accurate height
        if (showNavBarSpacer) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
            )
        }
    }
}

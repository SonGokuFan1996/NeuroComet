@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("UNUSED_PARAMETER")

package com.kyilmaz.neurocomet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.res.stringResource
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
import com.kyilmaz.neurocomet.calling.WebRTCCallManager

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
    var surfaceElevation by mutableFloatStateOf(2f)
    var listBottomPadding by mutableFloatStateOf(0f)
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
// INBOX SCREEN - NeuroComet Unique Design
// =============================================================================

@Composable
fun NeuroInboxScreen(
    conversations: List<Conversation>,
    safetyState: SafetyState,
    onOpenConversation: (String) -> Unit,
    onStartNewChat: (userId: String) -> Unit = {},
    onBack: (() -> Unit)? = null,
    onOpenCallHistory: () -> Unit = {},
    onOpenPracticeCall: () -> Unit = {}
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }
    val restriction = shouldBlockFeature(parentalState, BlockableFeature.DMS)

    // State for new chat dialog
    var showNewChatDialog by remember { mutableStateOf(false) }

    if (restriction != null) {
        ParentalBlockedScreen(
            restrictionType = restriction,
            featureName = "Direct Messages"
        )
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Unread", "Calls", "Groups", "Archived")

    val filteredConversations = remember(conversations, searchQuery, selectedFilter) {
        var result = conversations

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            result = result.filter { conv ->
                val otherId = conv.participants.firstOrNull { it != "me" } ?: ""
                val user = MOCK_USERS.find { it.id == otherId }
                val text = "${user?.name ?: otherId} ${conv.messages.lastOrNull()?.content ?: ""}"
                text.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply category filter
        when (selectedFilter) {
            "All" -> result = result.filter { !it.isArchived }
            "Unread" -> result = result.filter { it.unreadCount > 0 && !it.isArchived }
            "Groups" -> result = result.filter { it.isGroup && !it.isArchived }
            "Archived" -> result = result.filter { it.isArchived }
        }

        result
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val unreadTotal = conversations.sumOf { it.unreadCount }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (isSearching) {
                // Search mode
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    NeuroSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClose = {
                            isSearching = false
                            searchQuery = ""
                        }
                    )
                }
            } else {
                // Modern header matching Flutter design exactly
                MessagesHeader(
                    unreadCount = unreadTotal,
                    onNewMessage = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showNewChatDialog = true
                    },
                    onSearch = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isSearching = true
                    },
                    onCallHistory = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenCallHistory()
                    },
                    isDark = isDark
                )
            }
        },
        floatingActionButton = {
            // Only show FAB when not using the header buttons
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips matching Flutter style
            if (!isSearching) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(filters.size) { index ->
                        val filter = filters[index]
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            "Unread" -> unreadTotal
                            else -> null
                        }

                        MessagesFilterPill(
                            label = filter,
                            count = count,
                            isSelected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedFilter = filter
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            if (selectedFilter == "Calls") {
                // Inline call history — like IG Calls tab
                InlineCallHistoryView(
                    onOpenCallHistory = onOpenCallHistory,
                    onOpenPracticeCall = onOpenPracticeCall,
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredConversations.isEmpty()) {
                NeuroEmptyInboxState(
                    isSearchResult = searchQuery.isNotBlank(),
                    selectedFilter = selectedFilter,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        ModernConversationListItem(
                            conversation = conversation,
                            onClick = { onOpenConversation(conversation.id) },
                            isDark = isDark,
                            onVideoCall = {
                                val otherId = conversation.participants.firstOrNull { it != "me" } ?: return@ModernConversationListItem
                                val user = MOCK_USERS.find { it.id == otherId }
                                WebRTCCallManager.getInstance().startCall(
                                    recipientId = otherId,
                                    recipientName = user?.name ?: otherId,
                                    recipientAvatar = user?.avatarUrl ?: avatarUrl(otherId),
                                    callType = com.kyilmaz.neurocomet.calling.CallType.VIDEO
                                )
                            },
                            onVoiceCall = {
                                val otherId = conversation.participants.firstOrNull { it != "me" } ?: return@ModernConversationListItem
                                val user = MOCK_USERS.find { it.id == otherId }
                                WebRTCCallManager.getInstance().startCall(
                                    recipientId = otherId,
                                    recipientName = user?.name ?: otherId,
                                    recipientAvatar = user?.avatarUrl ?: avatarUrl(otherId),
                                    callType = com.kyilmaz.neurocomet.calling.CallType.VOICE
                                )
                            }
                        )
                    }

                    // Bottom spacing for navigation bar
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        NewChatDialog(
            existingConversations = conversations,
            onDismiss = { showNewChatDialog = false },
            onSelectUser = { userId ->
                showNewChatDialog = false
                onStartNewChat(userId)
            }
        )
    }
}

/**
 * Inline call history view shown when the "Calls" filter pill is selected.
 * Mimics Instagram's Calls tab — uses real device contacts from ContactsManager.
 */
@Composable
private fun InlineCallHistoryView(
    onOpenCallHistory: () -> Unit,
    onOpenPracticeCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val callManager = remember { WebRTCCallManager.getInstance() }
    val callHistory = callManager.callHistory
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Device contacts state
    var hasContactsPermission by remember {
        mutableStateOf(ContactsManager.hasContactsPermission(context))
    }
    var deviceContacts by remember {
        mutableStateOf<List<ContactsManager.DeviceContact>>(emptyList())
    }
    var matchedContacts by remember {
        mutableStateOf<List<ContactsManager.MatchedContact>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(false) }

    // Permission launcher
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactsPermission = granted
        if (granted) {
            scope.launch {
                isLoading = true
                deviceContacts = ContactsManager.readDeviceContacts(context)
                matchedContacts = ContactsManager.findFriendsOnApp(context)
                ContactsManager.setContactsSyncEnabled(context, true)
                isLoading = false
            }
        }
    }

    // Auto-load contacts if permission already granted
    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission && deviceContacts.isEmpty()) {
            isLoading = true
            deviceContacts = ContactsManager.readDeviceContacts(context)
            matchedContacts = ContactsManager.findFriendsOnApp(context)
            isLoading = false
        }
    }

    // Build a unified contact list: matched app users first, then device contacts with phone numbers
    val callableContacts = remember(matchedContacts, deviceContacts) {
        val result = mutableListOf<CallableContact>()

        // 1. Matched contacts (device contact ↔ app user) — can do in-app calls
        for (match in matchedContacts) {
            result.add(
                CallableContact(
                    id = match.appUser.id,
                    name = match.appUser.name,
                    phoneNumber = match.deviceContact.phoneNumbers.firstOrNull(),
                    photoUri = match.deviceContact.photoUri ?: match.appUser.avatarUrl,
                    isAppUser = true
                )
            )
        }

        // 2. App users not yet matched via contacts
        val matchedIds = matchedContacts.map { it.appUser.id }.toSet()
        for (user in MOCK_USERS.filter { it.id != "me" && it.id !in matchedIds }) {
            result.add(
                CallableContact(
                    id = user.id,
                    name = user.name,
                    phoneNumber = null,
                    photoUri = user.avatarUrl,
                    isAppUser = true
                )
            )
        }

        // 3. Device contacts with phone numbers (not matched to app users)
        val matchedNames = matchedContacts.map { it.deviceContact.displayName.lowercase() }.toSet()
        for (contact in deviceContacts) {
            if (contact.displayName.lowercase() in matchedNames) continue
            if (contact.phoneNumbers.isEmpty()) continue
            result.add(
                CallableContact(
                    id = "device_${contact.displayName.hashCode()}",
                    name = contact.displayName,
                    phoneNumber = contact.phoneNumbers.first(),
                    photoUri = contact.photoUri,
                    isAppUser = false
                )
            )
        }

        result
    }

    if (callHistory.isEmpty()) {
        // Empty state — encourage first call
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No calls yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Call your contacts using voice or video.\nTap any contact below to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }

            // Contacts permission prompt
            if (!hasContactsPermission) {
                item {
                    Spacer(Modifier.height(20.dp))
                    ContactsPermissionCard(
                        onGrant = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenPracticeCall()
                        }
                    ) {
                        Icon(Icons.Filled.Headset, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Practice Call")
                    }
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenCallHistory()
                        }
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call History")
                    }
                }
            }

            // Quick Call contacts
            if (callableContacts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                items(callableContacts.take(20)) { contact ->
                    CallableContactRow(
                        contact = contact,
                        onVoiceCall = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            callManager.startCall(
                                recipientId = contact.id,
                                recipientName = contact.name,
                                recipientAvatar = contact.photoUri ?: "",
                                callType = com.kyilmaz.neurocomet.calling.CallType.VOICE
                            )
                        },
                        onVideoCall = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            callManager.startCall(
                                recipientId = contact.id,
                                recipientName = contact.name,
                                recipientAvatar = contact.photoUri ?: "",
                                callType = com.kyilmaz.neurocomet.calling.CallType.VIDEO
                            )
                        }
                    )
                }

                item { Spacer(Modifier.height(100.dp)) }
            } else if (isLoading) {
                item {
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    } else {
        // Show call history entries
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Quick call row at top — real contacts
            if (callableContacts.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(callableContacts.take(12)) { contact ->
                            QuickCallContact(
                                name = contact.name,
                                photoUri = contact.photoUri,
                                isAppUser = contact.isAppUser,
                                onVoiceCall = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    callManager.startCall(
                                        recipientId = contact.id,
                                        recipientName = contact.name,
                                        recipientAvatar = contact.photoUri ?: "",
                                        callType = com.kyilmaz.neurocomet.calling.CallType.VOICE
                                    )
                                },
                                onVideoCall = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    callManager.startCall(
                                        recipientId = contact.id,
                                        recipientName = contact.name,
                                        recipientAvatar = contact.photoUri ?: "",
                                        callType = com.kyilmaz.neurocomet.calling.CallType.VIDEO
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Contacts permission card in the list if not granted
            if (!hasContactsPermission) {
                item {
                    ContactsPermissionCard(
                        onGrant = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            items(callHistory, key = { it.id }) { entry ->
                InlineCallHistoryItem(
                    entry = entry,
                    onCallBack = {
                        callManager.startCall(
                            recipientId = entry.recipientId,
                            recipientName = entry.recipientName,
                            recipientAvatar = entry.recipientAvatar,
                            callType = entry.callTypeEnum
                        )
                    }
                )
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

/**
 * Data class representing a contact that can be called — unified from device contacts + app users.
 */
private data class CallableContact(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val photoUri: String?,
    val isAppUser: Boolean
)

@Composable
private fun QuickCallContact(
    name: String,
    photoUri: String? = null,
    isAppUser: Boolean = true,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onVoiceCall),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable(onClick = onVoiceCall),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            // In-app user badge
            if (isAppUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Mini call buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                Icons.Filled.Phone,
                contentDescription = "Voice call $name",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onVoiceCall),
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                Icons.Filled.Videocam,
                contentDescription = "Video call $name",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onVideoCall),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Full-width contact row for the Calls tab — shows name, phone number, photo, and call buttons.
 */
@Composable
private fun CallableContactRow(
    contact: CallableContact,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVoiceCall)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = contact.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            if (contact.isAppUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    contact.isAppUser && contact.phoneNumber != null -> "NeuroComet · ${contact.phoneNumber}"
                    contact.isAppUser -> "NeuroComet"
                    contact.phoneNumber != null -> contact.phoneNumber
                    else -> "Contact"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Voice call
        IconButton(onClick = onVoiceCall, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Outlined.Phone,
                contentDescription = "Voice call ${contact.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        // Video call
        IconButton(onClick = onVideoCall, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Outlined.Videocam,
                contentDescription = "Video call ${contact.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Card prompting the user to grant contacts permission for the Calls tab.
 */
@Composable
private fun ContactsPermissionCard(
    onGrant: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ContactPhone,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Access your contacts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Call people from your phone's contacts directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = onGrant) {
                Text("Allow")
            }
        }
    }
}

@Composable
private fun InlineCallHistoryItem(
    entry: com.kyilmaz.neurocomet.calling.CallHistoryEntry,
    onCallBack: () -> Unit
) {
    val callTypeEnum = entry.callTypeEnum
    val outcomeEnum = entry.outcomeEnum

    val callIcon = when (callTypeEnum) {
        com.kyilmaz.neurocomet.calling.CallType.VOICE -> Icons.Filled.Phone
        com.kyilmaz.neurocomet.calling.CallType.VIDEO -> Icons.Filled.Videocam
    }

    val outcomeColor = when (outcomeEnum) {
        com.kyilmaz.neurocomet.calling.CallOutcome.MISSED,
        com.kyilmaz.neurocomet.calling.CallOutcome.DECLINED -> MaterialTheme.colorScheme.error
        com.kyilmaz.neurocomet.calling.CallOutcome.COMPLETED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val outcomeText = when (outcomeEnum) {
        com.kyilmaz.neurocomet.calling.CallOutcome.COMPLETED -> if (entry.formattedDuration.isNotEmpty()) entry.formattedDuration else "Connected"
        com.kyilmaz.neurocomet.calling.CallOutcome.MISSED -> "Missed"
        com.kyilmaz.neurocomet.calling.CallOutcome.DECLINED -> "Declined"
        com.kyilmaz.neurocomet.calling.CallOutcome.NO_ANSWER -> "No answer"
        com.kyilmaz.neurocomet.calling.CallOutcome.CANCELLED -> "Cancelled"
        com.kyilmaz.neurocomet.calling.CallOutcome.FAILED -> "Failed"
    }

    val directionLabel = if (entry.isOutgoing) "Outgoing" else "Incoming"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallBack)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.recipientName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.recipientName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (outcomeEnum == com.kyilmaz.neurocomet.calling.CallOutcome.MISSED)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    callIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = outcomeColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$directionLabel · $outcomeText · ${entry.formattedTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = outcomeColor
                )
            }
        }

        // Call back button
        IconButton(onClick = onCallBack) {
            Icon(
                callIcon,
                contentDescription = "Call ${entry.recipientName}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Modern Messages header matching Flutter design - uses dynamic colors
 */
@Composable
private fun MessagesHeader(
    unreadCount: Int,
    onNewMessage: () -> Unit,
    onSearch: () -> Unit,
    onCallHistory: () -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title section
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.nav_messages),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(12.dp))
                        UnreadBadge(count = unreadCount)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (unreadCount > 0) {
                        "You have $unreadCount unread ${if (unreadCount == 1) "message" else "messages"}"
                    } else {
                        "Your conversations ✨"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MessagesHeaderIconButton(
                    icon = Icons.Outlined.Search,
                    onClick = onSearch,
                    contentDescription = stringResource(R.string.conversation_search),
                    isDark = isDark
                )
                MessagesHeaderIconButton(
                    icon = Icons.Outlined.Videocam,
                    onClick = onCallHistory,
                    contentDescription = "Video calls",
                    isDark = isDark
                )
                MessagesHeaderIconButton(
                    icon = Icons.Outlined.Phone,
                    onClick = onCallHistory,
                    contentDescription = "Call history",
                    isDark = isDark
                )
                MessagesHeaderIconButton(
                    icon = Icons.Outlined.Edit,
                    onClick = onNewMessage,
                    contentDescription = stringResource(R.string.conversation_new_message),
                    isPrimary = true,
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Animated unread badge - uses dynamic colors
 */
@Composable
private fun UnreadBadge(count: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, tertiaryColor)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/**
 * Header icon button - uses dynamic colors
 */
@Composable
private fun MessagesHeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    isDark: Boolean,
    isPrimary: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) {
            primaryColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = if (isPrimary) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Filter pill - uses dynamic colors
 */
@Composable
private fun MessagesFilterPill(
    label: String,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) {
            primaryColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = primaryColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            color = primaryColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

/**
 * Modern conversation list item - uses dynamic colors
 */
@Composable
private fun ModernConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit,
    isDark: Boolean,
    onVideoCall: () -> Unit = {},
    onVoiceCall: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val successColor = Color(0xFF4CAF50)

    val hasUnread = conversation.unreadCount > 0
    val otherUserId = conversation.participants.firstOrNull { it != "me" } ?: ""
    val otherUser = MOCK_USERS.find { it.id == otherUserId }
    val displayName = if (conversation.isGroup) {
        conversation.groupName ?: "Group Chat"
    } else {
        otherUser?.name ?: otherUserId
    }
    val lastMessage = conversation.messages.lastOrNull()


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (hasUnread) {
            primaryColor.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient ring for unread
            Box {
                if (hasUnread) {
                    // Gradient ring
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(primaryColor, tertiaryColor)
                                ),
                                shape = CircleShape
                            )
                    )
                }
                // Avatar
                Box(
                    modifier = Modifier
                        .padding(if (hasUnread) 2.dp else 0.dp)
                        .size(54.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Online indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = if (hasUnread) (-4).dp else (-2).dp, y = if (hasUnread) (-4).dp else (-2).dp)
                        .size(14.dp)
                        .background(successColor, CircleShape)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.background,
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Verified badge (if applicable)
                    if (otherUser?.name?.contains("Dr.") == true) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified",
                            modifier = Modifier.size(16.dp),
                            tint = primaryColor
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = lastMessage?.timestamp?.let { formatMessageTimeString(it) } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (hasUnread) primaryColor else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lastMessage?.content ?: "Start a conversation",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasUnread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(primaryColor, tertiaryColor)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Call shortcut icons — IG style
            if (!conversation.isGroup) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onVoiceCall,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = "Voice call",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onVideoCall,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Videocam,
                            contentDescription = "Video call",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun NeuroSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.dm_search_conversations_placeholder)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                }
            }
        }
    }
}
@Composable
private fun NeuroEmptyInboxState(
    isSearchResult: Boolean,
    selectedFilter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = when {
                    isSearchResult -> "Search"
                    selectedFilter == "Unread" -> "Unread"
                    selectedFilter == "Groups" -> "Groups"
                    selectedFilter == "Archived" -> "Archived"
                    else -> "Inbox"
                },
                fontSize = 64.sp
            )
            Text(
                text = when {
                    isSearchResult -> "No results found"
                    selectedFilter == "Unread" -> "All caught up!"
                    selectedFilter == "Groups" -> "No group chats yet"
                    selectedFilter == "Archived" -> "Nothing archived"
                    else -> "No conversations yet"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    isSearchResult -> "Try a different search term"
                    selectedFilter != "All" -> "Change filters to see more"
                    else -> "Start a conversation with someone who gets you."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatDialog(
    existingConversations: List<Conversation>,
    onDismiss: () -> Unit,
    onSelectUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val availableUsers = remember(searchQuery, existingConversations) {
        MOCK_USERS.filter { user ->
            user.id != "me" && (
                searchQuery.isBlank() ||
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.id.contains(searchQuery, ignoreCase = true)
            )
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.new_chat),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text(stringResource(R.string.search_users)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (availableUsers.isEmpty()) {
                    item {
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(availableUsers, key = { it.id }) { user ->
                        val hasExistingChat = existingConversations.any { conv -> conv.participants.contains(user.id) }
                        Surface(
                            onClick = { onSelectUser(user.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${user.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (hasExistingChat) {
                                    AssistChip(
                                        onClick = { onSelectUser(user.id) },
                                        label = { Text("Open") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


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
    onUnblockUser: (String) -> Unit = {},
    enableVideoChat: Boolean = true,
    typingIndicatorVariant: String = "control",
    enableSimulatedReplies: Boolean = false,
    onSimulatedReply: (conversationId: String, senderId: String, content: String) -> Unit = { _, _, _ -> }
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

    // Wallpaper state
    var showWallpaperPicker by remember { mutableStateOf(false) }
    var activeWallpaperKey by remember {
        mutableStateOf(
            SettingsManager.getConversationWallpaper(conversation.id)
                ?: SettingsManager.globalWallpaper
        )
    }
    val activeWallpaper = ConversationWallpaper.fromKey(activeWallpaperKey)
    val showsTypingStatusText = typingIndicatorVariant != "control"
    val showsTypingBubble = typingIndicatorVariant == "dots"

    // Typing indicator state for optional demo replies in debug/mock mode only.
    var isOtherTyping by remember(conversation.id) { mutableStateOf(false) }
    var lastSentMessageCount by remember(conversation.id) {
        mutableIntStateOf(conversation.messages.count { it.senderId == "me" })
    }

    LaunchedEffect(enableSimulatedReplies) {
        if (!enableSimulatedReplies) {
            isOtherTyping = false
        }
    }

    // Simulated auto-reply is intentionally disabled for production behavior.
    LaunchedEffect(conversation.messages.size, enableSimulatedReplies) {
        if (!enableSimulatedReplies) return@LaunchedEffect
        val currentSentCount = conversation.messages.count { it.senderId == "me" }
        if (currentSentCount < lastSentMessageCount) {
            lastSentMessageCount = currentSentCount
            return@LaunchedEffect
        }
        if (currentSentCount > lastSentMessageCount && currentSentCount > 0) {
            lastSentMessageCount = currentSentCount
            kotlinx.coroutines.delay(800L)
            isOtherTyping = true
            kotlinx.coroutines.delay((1500L..3000L).random())
            isOtherTyping = false
            val lastMsg = conversation.messages.lastOrNull { it.senderId == "me" }?.content ?: ""
            val reply = generateSimulatedReply(displayName, lastMsg)
            onSimulatedReply(conversation.id, recipientId, reply)
        }
    }

    // Call manager — uses real WebRTC call manager (dialog handled globally by MainActivity)
    val webRtcCallManager = remember { WebRTCCallManager.getInstance() }

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

    // Scroll to bottom on new messages or typing indicator
    LaunchedEffect(conversation.messages.size, isOtherTyping) {
        val itemCount = conversation.messages.size + (if (isOtherTyping && showsTypingBubble) 1 else 0)
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
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

    // Keep keyboard insets scoped to the composer so focusing the input
    // doesn't shift the entire conversation under the status bar.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ──
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
                            contentDescription = stringResource(R.string.conversation_back),
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
                                isOtherTyping && showsTypingStatusText -> MaterialTheme.colorScheme.primary
                                else -> Color(0xFF4CAF50) // Online green
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = when {
                                    isUserBlocked -> stringResource(R.string.status_blocked)
                                    isUserMuted -> stringResource(R.string.status_muted)
                                    isOtherTyping && showsTypingStatusText -> "typing..."
                                    else -> stringResource(R.string.status_online)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action buttons — voice & video calling (always visible)
                    IconButton(onClick = {
                        webRtcCallManager.startCall(
                            recipientId = recipientId,
                            recipientName = displayName,
                            recipientAvatar = avatar,
                            callType = com.kyilmaz.neurocomet.calling.CallType.VIDEO
                        )
                    }) {
                        Icon(
                        Icons.Filled.Videocam,
                        contentDescription = stringResource(R.string.conversation_video_call),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = {
                        webRtcCallManager.startCall(
                            recipientId = recipientId,
                            recipientName = displayName,
                            recipientAvatar = avatar,
                            callType = com.kyilmaz.neurocomet.calling.CallType.VOICE
                        )
                    }) {
                        Icon(
                        Icons.Filled.Call,
                        contentDescription = stringResource(R.string.conversation_voice_call),
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
                                leadingIcon = { Icon(Icons.Outlined.Person, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Search") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Outlined.Search, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Wallpaper") },
                                onClick = {
                                    showMenu = false
                                    showWallpaperPicker = true
                                },
                                leadingIcon = { Icon(Icons.Outlined.Wallpaper, null) }
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
                            DropdownMenuItem(
                                text = { Text("Report User", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Outlined.Flag, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
        }


        // ── Content Area (messages) ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .conversationWallpaper(
                    wallpaper = activeWallpaper,
                    isDark = isDark,
                    reducedMotion = SettingsManager.reducedMotion,
                    dataSaver = SettingsManager.dataSaver
                )
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
                        text = "Messages are end-to-end encrypted. Say hi! 👋",
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

                    // Typing indicator
                    if (isOtherTyping && showsTypingBubble) {
                        item(key = "typing_indicator") {
                            TypingIndicatorBubble(
                                displayName = displayName,
                                isDark = isDark
                            )
                        }
                    }
                }

                // Scroll to bottom FAB - Google Messages style
                androidx.compose.animation.AnimatedVisibility(
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

        // ── Bottom Bar (input) ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            color = if (isUserBlocked) inputBarColor else Color.Transparent,
        ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                        // Rebuilt Floating Message Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            // Pending attachment preview
                            pendingAttachment?.let { attachment ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 2.dp
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
                                        .padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 2.dp
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

                            // Attachment picker & Emoji panels
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showAttachmentPicker && !isRecordingVoice,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 4.dp
                                ) {
                                    NeuroAttachmentPicker(
                                        attachmentState = attachmentState,
                                        onDismiss = { showAttachmentPicker = false }
                                    )
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showEmojiPanel,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 4.dp
                                ) {
                                    NeuroEmojiPanel(
                                        onEmojiSelected = { messageText += it },
                                        onDismiss = { showEmojiPanel = false }
                                    )
                                }
                            }

                            // Main Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Attachment button
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            showAttachmentPicker = !showAttachmentPicker
                                            if (showAttachmentPicker) showEmojiPanel = false
                                        },
                                        modifier = Modifier.size(48.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = if (showAttachmentPicker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                    ) {
                                        Icon(
                                            if (showAttachmentPicker) Icons.Filled.Close else Icons.Filled.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                // Text Container
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isKeyboardVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
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
                                                    showAttachmentPicker = false
                                                }
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                if (showEmojiPanel) Icons.Filled.Keyboard else Icons.Filled.EmojiEmotions,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        // TextField
                                        BasicTextField(
                                            value = messageText,
                                            onValueChange = { messageText = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 12.dp),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                                            ),
                                            decorationBox = { innerTextField ->
                                                if (messageText.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.dm_message_placeholder),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )

                                        // Camera button when empty
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

                                // Send / Mic Button
                                val hasContent = messageText.isNotBlank() || pendingAttachment != null
                                FilledIconButton(
                                    onClick = {
                                        when {
                                            hasContent -> {
                                                val text = messageText.trim()
                                                val finalMessage = if (pendingAttachment != null && text.isEmpty()) {
                                                    "[${pendingAttachment?.type?.name}: ${pendingAttachment?.displayName}]"
                                                } else if (pendingAttachment != null) {
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
                                            isRecordingVoice -> {
                                                val voiceMessage = voiceRecorder.stopRecording()
                                                isRecordingVoice = false
                                                if (voiceMessage != null) {
                                                    onSend(recipientId, "[Voice message: ${voiceMessage.durationFormatted}]")
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                            else -> {
                                                if (attachmentState.hasAudioPermission()) {
                                                    if (voiceRecorder.startRecording()) {
                                                        isRecordingVoice = true
                                                        showAttachmentPicker = false
                                                        showEmojiPanel = false
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                } else {
                                                    attachmentState.onRequestAudioPermission()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = when {
                                            hasContent -> MaterialTheme.colorScheme.primary
                                            isRecordingVoice -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        }
                                    )
                                ) {
                                    Icon(
                                        imageVector = when {
                                            hasContent -> Icons.AutoMirrored.Filled.Send
                                            isRecordingVoice -> Icons.Filled.Stop
                                            else -> Icons.Filled.Mic
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
        }
    } // end outer Column

    // Call dialog handled globally by MainActivity's ActiveCallDialog overlay

    // Wallpaper picker bottom sheet
    if (showWallpaperPicker) {
        WallpaperPickerSheet(
            currentWallpaper = activeWallpaper,
            conversationId = conversation.id,
            onSelect = { wallpaper, perConversationOnly ->
                if (perConversationOnly) {
                    SettingsManager.setConversationWallpaper(conversation.id, wallpaper.name)
                } else {
                    SettingsManager.setGlobalWallpaper(wallpaper.name)
                    // Clear per-conversation override so global takes effect
                    SettingsManager.setConversationWallpaper(conversation.id, null)
                }
                activeWallpaperKey = wallpaper.name
                showWallpaperPicker = false
            },
            onDismiss = { showWallpaperPicker = false }
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
        val icon: ImageVector,
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
    return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
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
 *
 * Neurodivergent-friendly features:
 * - Gentle, predictable animations that don't cause sensory overload
 * - Staggered entrance for each reaction (visually satisfying)
 * - Clear haptic feedback on selection
 * - Optional full emoji picker via plus button
 */
@Composable
private fun ReactionPickerPopup(
    isFromMe: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showFullPicker by remember { mutableStateOf(false) }

    // Staggered animation for each reaction
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    // Main container animation - gentle scale + fade
    val containerScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "popup-scale"
    )

    val containerAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "popup-alpha"
    )

    Popup(
        alignment = if (isFromMe) Alignment.TopEnd else Alignment.TopStart,
        offset = IntOffset(0, -120),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column {
            // Main reaction bar
            Surface(
                modifier = Modifier
                    .scale(containerScale)
                    .graphicsLayer { alpha = containerAlpha }
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QUICK_REACTIONS.forEachIndexed { index, emoji ->
                        // Staggered animation for each reaction button
                        val delayMs = index * 40 // 40ms delay between each
                        var buttonVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(animationStarted) {
                            if (animationStarted) {
                                kotlinx.coroutines.delay(delayMs.toLong())
                                buttonVisible = true
                            }
                        }

                        val buttonScale by animateFloatAsState(
                            targetValue = if (buttonVisible) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "button-scale-$index"
                        )

                        ReactionButton(
                            emoji = emoji,
                            scale = buttonScale,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReactionSelected(emoji)
                            }
                        )
                    }

                    // "More" button to show full emoji picker
                    var moreButtonVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(animationStarted) {
                        if (animationStarted) {
                            kotlinx.coroutines.delay((QUICK_REACTIONS.size * 40 + 50).toLong())
                            moreButtonVisible = true
                        }
                    }

                    val moreScale by animateFloatAsState(
                        targetValue = if (moreButtonVisible) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "more-scale"
                    )

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFullPicker = !showFullPicker
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .scale(moreScale)
                    ) {
                        Icon(
                            if (showFullPicker) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (showFullPicker) "Close picker" else "More reactions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Full emoji picker (expands below when plus is tapped)
            AnimatedVisibility(
                visible = showFullPicker,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FullEmojiPicker(
                    onEmojiSelected = { emoji ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReactionSelected(emoji)
                    }
                )
            }
        }
    }
}

/**
 * Full emoji picker with categorized emojis for more reaction options.
 */
@Composable
private fun FullEmojiPicker(
    onEmojiSelected: (String) -> Unit
) {
    val emojiCategories = listOf(
        "Smileys" to listOf("😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊", "😇", "🙂", "😉", "😌", "😍", "🥰", "😘"),
        "Emotions" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "💔", "💕", "💖", "💗", "💝", "💞", "💟"),
        "Gestures" to listOf("👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✌️", "🤞", "🤟", "🤘", "👌", "🤌", "💪"),
        "Nature" to listOf("🌸", "🌺", "🌻", "🌷", "🌹", "🌼", "💐", "🌿", "🍀", "🌈", "⭐", "✨", "🌙", "☀️", "🔥"),
        "Neurodivergent" to listOf("♾️", "🧠", "💜", "🦋", "🌈", "🎨", "🎵", "📚", "🧩", "💡", "🌟", "🦄", "🐸", "🦦", "🐢")
    )

    var selectedCategory by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier
            .padding(top = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .padding(8.dp)
        ) {
            // Category tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                emojiCategories.forEachIndexed { index, (name, _) ->
                    FilterChip(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        label = {
                            Text(
                                name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Emoji grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(emojiCategories[selectedCategory].second) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 24.sp)
                    }
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
    scale: Float = 1f,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "reaction-press-scale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale * pressScale)
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

private fun generateSimulatedReply(displayName: String, lastMessage: String): String {
    val lower = lastMessage.lowercase()
    return when {
        lower.contains("hello") || lower.contains("hi") -> "Hey! It's good to hear from you."
        lower.contains("how are you") -> "I'm doing okay — thanks for checking in."
        lower.contains("thanks") -> "Of course. Happy to help."
        lower.contains("?") -> "I think so, but I'd want to hear a bit more from you first."
        lower.contains("story") -> "That makes sense. Tell me more when you want to."
        else -> listOf(
            "That makes sense.",
            "I hear you.",
            "Got it — I'm with you.",
            "Thanks for sharing that.",
            "I'm here and listening."
        ).random()
    }
}

@Composable
private fun TypingIndicatorBubble(
    displayName: String,
    isDark: Boolean
) {
    val bubbleColor = if (isDark) Color(0xFF2F3136) else Color(0xFFE9ECEF)
    val dotColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF5F6368)
    val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator")

    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.35f at 0
                1f at 180
                0.35f at 360
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "typing_dot_1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.35f at 120
                1f at 300
                0.35f at 480
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "typing_dot_2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.35f at 240
                1f at 420
                0.35f at 600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "typing_dot_3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = "$displayName is typing",
                    style = MaterialTheme.typography.labelSmall,
                    color = dotColor.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(dot1, dot2, dot3).forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .graphicsLayer {
                                    this.alpha = alpha
                                    scaleX = 0.85f + (alpha * 0.15f)
                                    scaleY = 0.85f + (alpha * 0.15f)
                                }
                                .background(dotColor, CircleShape)
                        )
                    }
                }
            }
        }
    }
}




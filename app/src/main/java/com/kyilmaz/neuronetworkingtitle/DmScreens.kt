package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DmInboxScreen(
    conversations: List<Conversation>,
    safetyState: SafetyState,
    onOpenConversation: (String) -> Unit,
    onBack: (() -> Unit)? = null // Optional back action for nested navigation
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_messages)) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (conversations.isEmpty()) {
                Text("No conversations yet.", modifier = Modifier.padding(16.dp))
            } else {
                // List messages here. For now, just a placeholder.
                Text("DM Inbox (Placeholder)", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
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
    Scaffold(
        topBar = { TopAppBar(title = { Text(conversation.participants.first { it != "me" }) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Conversation with ${conversation.participants.first { it != "me" }} (Placeholder)", modifier = Modifier.padding(16.dp))
            Spacer(Modifier.weight(1f))
            Text("Input area placeholder", modifier = Modifier.padding(16.dp))
        }
    }
}
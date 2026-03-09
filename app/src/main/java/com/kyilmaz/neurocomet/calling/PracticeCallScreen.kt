package com.kyilmaz.neurocomet.calling
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kyilmaz.neurocomet.avatarUrl
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeCallSelectionScreen(
    onBack: () -> Unit,
    onPersonaSelected: (NeurodivergentPersona) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice Calls") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    "Practice phone calls with AI personas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Choose a Practice Partner",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(NeurodivergentPersona.entries.toList()) { persona ->
                PersonaCard(persona = persona, onClick = { onPersonaSelected(persona) })
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "?? The AI will speak responses aloud. Type or use voice to respond!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Composable
private fun PersonaCard(persona: NeurodivergentPersona, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl(persona.avatarSeed),
                contentDescription = persona.displayName,
                modifier = Modifier.size(56.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = persona.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = persona.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Filled.Call, contentDescription = "Start call", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeCallScreen(persona: NeurodivergentPersona, onEndCall: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember { CallAudioManager(context) }
    val isSpeaking by audioManager.isSpeaking.collectAsState()
    val isTtsReady by audioManager.isReady.collectAsState()
    val state by GeminiCallSimulator.state.collectAsState()
    val currentResponse by GeminiCallSimulator.currentResponse.collectAsState()
    val messages by GeminiCallSimulator.messagesFlow.collectAsState()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    var isMuted by remember { mutableStateOf(false) }
    var audioEnabled by remember { mutableStateOf(true) }
    var lastSpokenMessageId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(persona) { GeminiCallSimulator.startCall(persona) }
    LaunchedEffect(isTtsReady, persona) { if (isTtsReady) audioManager.setVoiceForGender(persona.isMale) }
    LaunchedEffect(messages) {
        if (audioEnabled && isTtsReady) {
            val lastMessage = messages.lastOrNull()
            if (lastMessage != null && !lastMessage.isFromUser && lastMessage.id != lastSpokenMessageId) {
                audioManager.speak(lastMessage.content, lastMessage.id)
                lastSpokenMessageId = lastMessage.id
            }
        }
    }
    LaunchedEffect(messages.size, currentResponse) { if (messages.isNotEmpty()) try { listState.animateScrollToItem(messages.size) } catch (e: Exception) {} }
    DisposableEffect(Unit) { onDispose { audioManager.shutdown(); GeminiCallSimulator.endCall() } }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.1f, animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "pulseScale")
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = avatarUrl(persona.avatarSeed), contentDescription = persona.displayName, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = persona.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(text = when { isSpeaking -> "Speaking..."; state is SimulatorState.Generating -> "Thinking..."; state is SimulatorState.Connected -> "Connected"; else -> GeminiCallSimulator.formatDuration() }, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = { audioManager.stop(); GeminiCallSimulator.endCall(); onEndCall() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "End call", tint = Color.White) } },
                actions = { IconButton(onClick = { audioEnabled = !audioEnabled; if (!audioEnabled) audioManager.stop() }) { Icon(if (audioEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Mute audio", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            when (val currentState = state) {
                is SimulatorState.Idle, is SimulatorState.Connecting -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = avatarUrl(persona.avatarSeed), contentDescription = persona.displayName, modifier = Modifier.size(120.dp).scale(pulseScale).clip(CircleShape), contentScale = ContentScale.Crop)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = "Calling ${persona.displayName}...", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        }
                    }
                }
                is SimulatorState.Error -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFFFB74D))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = currentState.message, style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { GeminiCallSimulator.startCall(persona) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))) { Icon(Icons.Filled.Refresh, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Retry", color = Color.White) }
                        }
                    }
                }
                is SimulatorState.Connected, is SimulatorState.Generating, is SimulatorState.Ended -> {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(messages, key = { it.id }) { message -> MessageBubble(message = message, personaAvatar = avatarUrl(persona.avatarSeed)) }
                        if (currentResponse.isNotEmpty()) { item { StreamingBubble(text = currentResponse, personaAvatar = avatarUrl(persona.avatarSeed)) } }
                        if (state is SimulatorState.Generating && currentResponse.isEmpty()) { item { TypingIndicator(personaAvatar = avatarUrl(persona.avatarSeed)) } }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { isMuted = !isMuted }, modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isMuted) Color(0xFFE53935).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))) { Icon(if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic, contentDescription = "Mute", tint = if (isMuted) Color(0xFFE53935) else Color.White, modifier = Modifier.size(20.dp)) }
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 16.dp, vertical = 12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                cursorBrush = SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    if (messageText.isEmpty()) Text("Type what youd say...", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyLarge)
                                    innerTextField()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            AnimatedVisibility(visible = messageText.isNotBlank(), enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) { IconButton(onClick = { if (messageText.isNotBlank()) { audioManager.stop(); GeminiCallSimulator.sendMessage(messageText); messageText = "" } }, modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp)) } }
                            AnimatedVisibility(visible = messageText.isBlank(), enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) { IconButton(onClick = { audioManager.stop(); GeminiCallSimulator.endCall(); onEndCall() }, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE53935))) { Icon(Icons.Filled.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(20.dp)) } }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun MessageBubble(message: AIConversationMessage, personaAvatar: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start) {
        if (!message.isFromUser) { AsyncImage(model = personaAvatar, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(modifier = Modifier.width(8.dp)) }
        Surface(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (message.isFromUser) 16.dp else 4.dp, bottomEnd = if (message.isFromUser) 4.dp else 16.dp), color = if (message.isFromUser) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f), modifier = Modifier.widthIn(max = 280.dp)) { Text(text = message.content, modifier = Modifier.padding(12.dp), color = if (message.isFromUser) MaterialTheme.colorScheme.onPrimary else Color.White, style = MaterialTheme.typography.bodyMedium) }
    }
}
@Composable
private fun StreamingBubble(text: String, personaAvatar: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        AsyncImage(model = personaAvatar, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(modifier = Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp), color = Color.White.copy(alpha = 0.15f), modifier = Modifier.widthIn(max = 280.dp)) { Text(text = text, modifier = Modifier.padding(12.dp), color = Color.White, style = MaterialTheme.typography.bodyMedium) }
    }
}
@Composable
private fun TypingIndicator(personaAvatar: String) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = personaAvatar, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop); Spacer(modifier = Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.15f)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val alpha by transition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, delayMillis = index * 200), repeatMode = RepeatMode.Reverse), label = "dot$index")
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White.copy(alpha = alpha)))
                }
            }
        }
    }
}

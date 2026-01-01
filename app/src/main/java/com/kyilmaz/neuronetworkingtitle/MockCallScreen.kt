package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Mock Call Types
 */
enum class CallType {
    VOICE,
    VIDEO
}

/**
 * Call State
 */
enum class CallState {
    RINGING,      // Outgoing call ringing
    INCOMING,     // Incoming call
    CONNECTED,    // Call in progress
    ENDED         // Call ended
}

/**
 * Call outcome for history
 */
enum class CallOutcome {
    COMPLETED,    // Call was answered and ended normally
    MISSED,       // Incoming call was not answered
    DECLINED,     // Call was declined
    NO_ANSWER,    // Outgoing call was not answered
    CANCELLED     // Outgoing call was cancelled before answer
}

/**
 * Call History Entry
 */
data class CallHistoryEntry(
    val id: String = "call_${System.currentTimeMillis()}",
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val callType: CallType,
    val isOutgoing: Boolean,
    val outcome: CallOutcome,
    val timestamp: String = Instant.now().toString(),
    val durationSeconds: Long = 0
) {
    val formattedDuration: String
        get() {
            if (durationSeconds == 0L) return ""
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "${minutes}:${seconds.toString().padStart(2, '0')}"
        }

    val formattedTime: String
        get() {
            return try {
                val instant = Instant.parse(timestamp)
                val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            } catch (e: Exception) {
                timestamp
            }
        }
}

/**
 * Mock Call Data
 */
data class MockCall(
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val callType: CallType,
    val isOutgoing: Boolean = true
)

/**
 * Mock Call Manager - handles call state and history
 */
object MockCallManager {
    var currentCall by mutableStateOf<MockCall?>(null)
        private set

    var callState by mutableStateOf(CallState.ENDED)
        private set

    var callDuration by mutableLongStateOf(0L)
        private set

    // Call history - most recent first
    private val _callHistory = mutableStateListOf<CallHistoryEntry>()
    val callHistory: List<CallHistoryEntry> get() = _callHistory.toList()

    private var callStartTime: Long = 0L

    fun startCall(
        recipientId: String,
        recipientName: String,
        recipientAvatar: String,
        callType: CallType
    ) {
        currentCall = MockCall(
            recipientId = recipientId,
            recipientName = recipientName,
            recipientAvatar = recipientAvatar,
            callType = callType,
            isOutgoing = true
        )
        callState = CallState.RINGING
        callDuration = 0L
        callStartTime = System.currentTimeMillis()
    }

    fun simulateIncomingCall(
        callerId: String,
        callerName: String,
        callerAvatar: String,
        callType: CallType
    ) {
        currentCall = MockCall(
            recipientId = callerId,
            recipientName = callerName,
            recipientAvatar = callerAvatar,
            callType = callType,
            isOutgoing = false
        )
        callState = CallState.INCOMING
        callDuration = 0L
        callStartTime = System.currentTimeMillis()
    }

    fun answerCall() {
        callState = CallState.CONNECTED
        callStartTime = System.currentTimeMillis() // Reset for actual call duration
    }

    fun connectCall() {
        callState = CallState.CONNECTED
        callStartTime = System.currentTimeMillis() // Reset for actual call duration
    }

    fun endCall() {
        // Add to call history
        currentCall?.let { call ->
            val outcome = when {
                callState == CallState.CONNECTED -> CallOutcome.COMPLETED
                callState == CallState.RINGING && call.isOutgoing -> CallOutcome.CANCELLED
                callState == CallState.RINGING && !call.isOutgoing -> CallOutcome.MISSED
                callState == CallState.INCOMING -> CallOutcome.MISSED
                else -> CallOutcome.COMPLETED
            }

            val durationSecs = if (callState == CallState.CONNECTED) {
                callDuration / 1000
            } else 0L

            val historyEntry = CallHistoryEntry(
                recipientId = call.recipientId,
                recipientName = call.recipientName,
                recipientAvatar = call.recipientAvatar,
                callType = call.callType,
                isOutgoing = call.isOutgoing,
                outcome = outcome,
                durationSeconds = durationSecs
            )

            _callHistory.add(0, historyEntry) // Add to beginning (most recent first)

            // Keep only last 100 calls
            while (_callHistory.size > 100) {
                _callHistory.removeAt(_callHistory.lastIndex)
            }
        }

        callState = CallState.ENDED
        currentCall = null
        callDuration = 0L
    }

    fun updateDuration(duration: Long) {
        callDuration = duration
    }

    /**
     * Clear all call history
     */
    fun clearCallHistory() {
        _callHistory.clear()
    }

    /**
     * Add mock call history entries for testing
     */
    fun addMockCallHistory() {
        val mockEntries = listOf(
            CallHistoryEntry(
                recipientId = "luna",
                recipientName = "Luna",
                recipientAvatar = avatarUrl("luna"),
                callType = CallType.VIDEO,
                isOutgoing = false,
                outcome = CallOutcome.COMPLETED,
                timestamp = Instant.now().minusSeconds(3600).toString(),
                durationSeconds = 245
            ),
            CallHistoryEntry(
                recipientId = "alex",
                recipientName = "Alex",
                recipientAvatar = avatarUrl("alex"),
                callType = CallType.VOICE,
                isOutgoing = true,
                outcome = CallOutcome.COMPLETED,
                timestamp = Instant.now().minusSeconds(7200).toString(),
                durationSeconds = 180
            ),
            CallHistoryEntry(
                recipientId = "jamie",
                recipientName = "Jamie",
                recipientAvatar = avatarUrl("jamie"),
                callType = CallType.VOICE,
                isOutgoing = false,
                outcome = CallOutcome.MISSED,
                timestamp = Instant.now().minusSeconds(14400).toString(),
                durationSeconds = 0
            ),
            CallHistoryEntry(
                recipientId = "sam",
                recipientName = "Sam",
                recipientAvatar = avatarUrl("sam"),
                callType = CallType.VIDEO,
                isOutgoing = true,
                outcome = CallOutcome.NO_ANSWER,
                timestamp = Instant.now().minusSeconds(28800).toString(),
                durationSeconds = 0
            ),
            CallHistoryEntry(
                recipientId = "luna",
                recipientName = "Luna",
                recipientAvatar = avatarUrl("luna"),
                callType = CallType.VOICE,
                isOutgoing = true,
                outcome = CallOutcome.COMPLETED,
                timestamp = Instant.now().minusSeconds(86400).toString(),
                durationSeconds = 520
            )
        )
        _callHistory.addAll(0, mockEntries)
    }
}

/**
 * Voice Call Screen - Full screen voice call UI
 */
@Composable
fun VoiceCallScreen(
    call: MockCall,
    callState: CallState,
    callDuration: Long,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit = {},
    onMuteToggle: (Boolean) -> Unit = {},
    onSpeakerToggle: (Boolean) -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    // Simulate call connecting after ringing
    LaunchedEffect(callState) {
        if (callState == CallState.RINGING) {
            delay(3000) // Ring for 3 seconds
            MockCallManager.connectCall()
        }
    }

    // Call duration timer
    LaunchedEffect(callState) {
        if (callState == CallState.CONNECTED) {
            var duration = 0L
            while (true) {
                delay(1000)
                duration++
                MockCallManager.updateDuration(duration)
            }
        }
    }

    // Pulsing animation for ringing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = { /* Cannot dismiss during call */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a1a2e),
                            Color(0xFF16213e),
                            Color(0xFF0f3460)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                // Call status text
                Text(
                    text = when (callState) {
                        CallState.RINGING -> "Calling..."
                        CallState.INCOMING -> "Incoming Call"
                        CallState.CONNECTED -> formatCallDuration(callDuration)
                        CallState.ENDED -> "Call Ended"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(40.dp))

                // Avatar with pulse animation when ringing
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .then(
                            if (callState == CallState.RINGING || callState == CallState.INCOMING) {
                                Modifier.scale(pulseScale)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    // Avatar
                    AsyncImage(
                        model = call.recipientAvatar,
                        contentDescription = "Caller avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Caller name
                Text(
                    text = call.recipientName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Voice Call",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.weight(1f))

                // Call controls
                when (callState) {
                    CallState.INCOMING -> {
                        // Answer/Decline buttons for incoming call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Decline button
                            CallControlButton(
                                icon = Icons.Filled.CallEnd,
                                label = "Decline",
                                backgroundColor = Color(0xFFE53935),
                                onClick = onEndCall
                            )

                            // Answer button
                            CallControlButton(
                                icon = Icons.Filled.Call,
                                label = "Answer",
                                backgroundColor = Color(0xFF43A047),
                                onClick = onAnswerCall
                            )
                        }
                    }

                    CallState.RINGING, CallState.CONNECTED -> {
                        // In-call controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Mute button
                            CallControlButton(
                                icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                label = if (isMuted) "Unmute" else "Mute",
                                backgroundColor = if (isMuted) Color.White else Color.White.copy(alpha = 0.2f),
                                iconColor = if (isMuted) Color.Black else Color.White,
                                onClick = {
                                    isMuted = !isMuted
                                    onMuteToggle(isMuted)
                                }
                            )

                            // End call button
                            CallControlButton(
                                icon = Icons.Filled.CallEnd,
                                label = "End",
                                backgroundColor = Color(0xFFE53935),
                                size = 72.dp,
                                onClick = onEndCall
                            )

                            // Speaker button
                            CallControlButton(
                                icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                label = if (isSpeakerOn) "Speaker" else "Speaker",
                                backgroundColor = if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f),
                                iconColor = if (isSpeakerOn) Color.Black else Color.White,
                                onClick = {
                                    isSpeakerOn = !isSpeakerOn
                                    onSpeakerToggle(isSpeakerOn)
                                }
                            )
                        }
                    }

                    CallState.ENDED -> {
                        // Call ended - auto dismiss
                        LaunchedEffect(Unit) {
                            delay(1500)
                            onEndCall()
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

/**
 * Video Call Screen - Full screen video call UI
 */
@Composable
fun VideoCallScreen(
    call: MockCall,
    callState: CallState,
    callDuration: Long,
    onEndCall: () -> Unit,
    onAnswerCall: () -> Unit = {},
    onMuteToggle: (Boolean) -> Unit = {},
    onCameraToggle: (Boolean) -> Unit = {},
    onFlipCamera: () -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOn by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }

    // Simulate call connecting after ringing
    LaunchedEffect(callState) {
        if (callState == CallState.RINGING) {
            delay(3000) // Ring for 3 seconds
            MockCallManager.connectCall()
        }
    }

    // Call duration timer
    LaunchedEffect(callState) {
        if (callState == CallState.CONNECTED) {
            var duration = 0L
            while (true) {
                delay(1000)
                duration++
                MockCallManager.updateDuration(duration)
            }
        }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls && callState == CallState.CONNECTED) {
            delay(5000)
            showControls = false
        }
    }

    Dialog(
        onDismissRequest = { /* Cannot dismiss during call */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Remote video (simulated with gradient background)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2a2a4e),
                                Color(0xFF1a1a2e)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (callState == CallState.CONNECTED) {
                    // Show "remote" participant avatar as placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = call.recipientAvatar,
                            contentDescription = "Remote participant",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = call.recipientName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Camera is simulated for testing",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // Ringing/Incoming state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = call.recipientAvatar,
                            contentDescription = "Caller avatar",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = call.recipientName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = when (callState) {
                                CallState.RINGING -> "Calling..."
                                CallState.INCOMING -> "Incoming Video Call"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Local video preview (self view) - simulated
            if (callState == CallState.CONNECTED && isCameraOn) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(width = 100.dp, height = 140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3a3a5e))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Your camera",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "You",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Top bar with call info
            if (showControls || callState != CallState.CONNECTED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = call.recipientName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when (callState) {
                                CallState.RINGING -> "Calling..."
                                CallState.INCOMING -> "Incoming"
                                CallState.CONNECTED -> formatCallDuration(callDuration)
                                CallState.ENDED -> "Call Ended"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    if (callState == CallState.CONNECTED) {
                        // Flip camera button
                        IconButton(onClick = onFlipCamera) {
                            Icon(
                                Icons.Filled.FlipCameraAndroid,
                                contentDescription = "Flip camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (callState) {
                    CallState.INCOMING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallControlButton(
                                icon = Icons.Filled.CallEnd,
                                label = "Decline",
                                backgroundColor = Color(0xFFE53935),
                                onClick = onEndCall
                            )
                            CallControlButton(
                                icon = Icons.Filled.Videocam,
                                label = "Answer",
                                backgroundColor = Color(0xFF43A047),
                                onClick = onAnswerCall
                            )
                        }
                    }

                    CallState.RINGING, CallState.CONNECTED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Camera toggle
                            CallControlButton(
                                icon = if (isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                                label = if (isCameraOn) "Camera" else "Camera Off",
                                backgroundColor = if (isCameraOn) Color.White.copy(alpha = 0.2f) else Color.White,
                                iconColor = if (isCameraOn) Color.White else Color.Black,
                                onClick = {
                                    isCameraOn = !isCameraOn
                                    onCameraToggle(isCameraOn)
                                }
                            )

                            // Mute toggle
                            CallControlButton(
                                icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                                label = if (isMuted) "Unmute" else "Mute",
                                backgroundColor = if (isMuted) Color.White else Color.White.copy(alpha = 0.2f),
                                iconColor = if (isMuted) Color.Black else Color.White,
                                onClick = {
                                    isMuted = !isMuted
                                    onMuteToggle(isMuted)
                                }
                            )

                            // End call
                            CallControlButton(
                                icon = Icons.Filled.CallEnd,
                                label = "End",
                                backgroundColor = Color(0xFFE53935),
                                size = 64.dp,
                                onClick = onEndCall
                            )
                        }
                    }

                    CallState.ENDED -> {
                        LaunchedEffect(Unit) {
                            delay(1500)
                            onEndCall()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable call control button
 */
@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color = Color.White,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(size * 0.45f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Format call duration as MM:SS
 */
private fun formatCallDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

/**
 * Call Dialog - wrapper that shows the appropriate call screen
 */
@Composable
fun CallDialog(
    call: MockCall,
    callState: CallState,
    callDuration: Long,
    onDismiss: () -> Unit
) {
    when (call.callType) {
        CallType.VOICE -> {
            VoiceCallScreen(
                call = call,
                callState = callState,
                callDuration = callDuration,
                onEndCall = {
                    MockCallManager.endCall()
                    onDismiss()
                },
                onAnswerCall = {
                    MockCallManager.answerCall()
                }
            )
        }
        CallType.VIDEO -> {
            VideoCallScreen(
                call = call,
                callState = callState,
                callDuration = callDuration,
                onEndCall = {
                    MockCallManager.endCall()
                    onDismiss()
                },
                onAnswerCall = {
                    MockCallManager.answerCall()
                }
            )
        }
    }
}

// =============================================================================
// CALL HISTORY SCREEN
// =============================================================================

/**
 * Call History Screen - Shows all voice/video call history
 * Accessible from Messages without cluttering the main UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onBack: () -> Unit,
    onCallUser: (userId: String, userName: String, userAvatar: String, callType: CallType) -> Unit = { _, _, _, _ -> }
) {
    val callHistory = MockCallManager.callHistory

    // Add mock data if empty (for demo purposes)
    LaunchedEffect(Unit) {
        if (callHistory.isEmpty()) {
            MockCallManager.addMockCallHistory()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Call History",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (callHistory.isNotEmpty()) {
                        IconButton(onClick = { MockCallManager.clearCallHistory() }) {
                            Icon(Icons.Filled.DeleteSweep, "Clear History")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (callHistory.isEmpty()) {
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
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        "No call history yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Your voice and video calls will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(callHistory, key = { it.id }) { entry ->
                    CallHistoryItem(
                        entry = entry,
                        onCallBack = {
                            onCallUser(
                                entry.recipientId,
                                entry.recipientName,
                                entry.recipientAvatar,
                                entry.callType
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual call history item
 */
@Composable
private fun CallHistoryItem(
    entry: CallHistoryEntry,
    onCallBack: () -> Unit
) {
    val callIcon = when (entry.callType) {
        CallType.VOICE -> Icons.Filled.Phone
        CallType.VIDEO -> Icons.Filled.Videocam
    }

    val directionIcon = when {
        entry.outcome == CallOutcome.MISSED -> Icons.AutoMirrored.Filled.CallMissed
        entry.isOutgoing -> Icons.AutoMirrored.Filled.CallMade
        else -> Icons.AutoMirrored.Filled.CallReceived
    }

    val outcomeColor = when (entry.outcome) {
        CallOutcome.MISSED -> MaterialTheme.colorScheme.error
        CallOutcome.DECLINED -> MaterialTheme.colorScheme.error
        CallOutcome.NO_ANSWER -> MaterialTheme.colorScheme.onSurfaceVariant
        CallOutcome.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
        CallOutcome.COMPLETED -> MaterialTheme.colorScheme.primary
    }

    val outcomeText = when (entry.outcome) {
        CallOutcome.COMPLETED -> if (entry.formattedDuration.isNotEmpty()) entry.formattedDuration else "Connected"
        CallOutcome.MISSED -> "Missed"
        CallOutcome.DECLINED -> "Declined"
        CallOutcome.NO_ANSWER -> "No answer"
        CallOutcome.CANCELLED -> "Cancelled"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallBack() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = entry.recipientAvatar,
                contentDescription = entry.recipientName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(12.dp))

        // Call info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.recipientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (entry.outcome == CallOutcome.MISSED)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    callIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    directionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = outcomeColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    outcomeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = outcomeColor
                )
                Text(
                    " • ${entry.formattedTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Call back button
        IconButton(
            onClick = onCallBack
        ) {
            Icon(
                callIcon,
                contentDescription = "Call ${entry.recipientName}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

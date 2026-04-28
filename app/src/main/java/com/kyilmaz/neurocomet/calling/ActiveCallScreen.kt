package com.kyilmaz.neurocomet.calling

import android.Manifest
import androidx.compose.ui.res.stringResource
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import com.kyilmaz.neurocomet.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ACTIVE CALL SCREEN — Instagram-style full-screen voice/video calling UI
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Uses WebRTCCallManager for real peer-to-peer audio/video via WebRTC.
 * Shows:
 *   - Full-screen remote video (video calls) or gradient background (voice)
 *   - PiP local video preview (video calls)
 *   - Caller avatar + name + status
 *   - Call duration timer
 *   - Control bar: mute, speaker, camera toggle, switch camera, end call
 *   - Incoming call: accept / decline buttons
 */

// ═════════════════════════════════════════════════════════════════════════════
// ACTIVE CALL DIALOG — wraps the full-screen UI in a Dialog
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun ActiveCallDialog(
    callManager: WebRTCCallManager = WebRTCCallManager.getInstance(),
    onDismiss: () -> Unit
) {
    if (callManager.currentCall == null) return
    val state = callManager.callState

    // Auto-dismiss when call ends
    LaunchedEffect(state) {
        if (state == CallState.ENDED) {
            delay(1200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { /* cannot dismiss during call */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        ActiveCallContent(callManager = callManager, onDismiss = onDismiss)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// CORE CONTENT
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun ActiveCallContent(
    callManager: WebRTCCallManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val call = callManager.currentCall ?: return
    val state = callManager.callState
    val duration = callManager.callDuration
    val isVideo = call.callType == CallType.VIDEO

    // Permission launcher
    val permissions = remember {
        val list = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (isVideo) list.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 37) {
            list.add("android.permission.ACCESS_LOCAL_NETWORK")
        }
        list.toTypedArray()
    }
    var permissionsGranted by remember {
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }
    LaunchedEffect(Unit) {
        if (!permissionsGranted) permissionLauncher.launch(permissions)
    }

    // Pulsing animation for ringing/incoming
    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Controls visibility - auto-hide after 5s during video call
    var showControls by remember { mutableStateOf(true) }
    LaunchedEffect(showControls, state) {
        if (isVideo && state == CallState.CONNECTED && showControls) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isVideo) Modifier.clickable { showControls = !showControls }
                else Modifier
            )
    ) {
        // Background: remote video or gradient
        if (isVideo) {
            VideoBackground(
                remoteVideoTrack = callManager.remoteVideoTrack,
                eglContext = callManager.getEglBaseContext(),
                state = state
            )
        } else {
            VoiceBackground()
        }

        // Main content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Status text
            Text(
                text = when (state) {
                    CallState.RINGING -> "Ringing..."
                    CallState.INCOMING -> "Incoming ${if (isVideo) "Video" else "Voice"} Call"
                    CallState.CONNECTING -> "Connecting..."
                    CallState.CONNECTED -> formatCallDuration(duration)
                    CallState.RECONNECTING -> "Reconnecting..."
                    CallState.ENDED -> "Call Ended"
                    CallState.IDLE -> ""
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

            // Avatar (hide during connected video call unless controls visible)
            val showAvatar = !isVideo || state != CallState.CONNECTED || showControls
            AnimatedVisibility(
                visible = showAvatar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .then(
                                if (state == CallState.RINGING || state == CallState.INCOMING)
                                    Modifier.scale(pulseScale)
                                else Modifier
                            )
                    ) {
                        // Glow ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(call.recipientAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.cd_caller_avatar),
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = call.recipientName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (isVideo) {
                        Text(
                            text = "Video Call",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Local video PiP preview (video calls, connected)
            if (isVideo && state == CallState.CONNECTED) {
                LocalVideoPip(
                    localVideoTrack = callManager.localVideoTrack,
                    eglContext = callManager.getEglBaseContext(),
                    isCameraOn = callManager.isCameraOn,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(Modifier.height(16.dp))
            }

            // Incoming call buttons
            if (state == CallState.INCOMING) {
                IncomingCallButtons(
                    onAccept = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        callManager.answerCall()
                    },
                    onDecline = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        callManager.declineCall()
                        onDismiss()
                    }
                )
            }

            // Connected call controls
            AnimatedVisibility(
                visible = state == CallState.CONNECTED || state == CallState.RECONNECTING || state == CallState.RINGING,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                CallControlBar(
                    callManager = callManager,
                    isVideo = isVideo,
                    onEndCall = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        callManager.endCall()
                    },
                    onDismiss = onDismiss
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// VIDEO BACKGROUND — remote peer's video
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun VideoBackground(
    remoteVideoTrack: VideoTrack?,
    eglContext: EglBase.Context?,
    state: CallState
) {
    if (remoteVideoTrack != null && eglContext != null && state == CallState.CONNECTED) {
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    init(eglContext, null)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    setEnableHardwareScaler(true)
                    remoteVideoTrack.addSink(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { renderer ->
                remoteVideoTrack.removeSink(renderer)
                renderer.release()
            }
        )
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
        )
    } else {
        // Fallback gradient while connecting
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// VOICE BACKGROUND — calming gradient
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun VoiceBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// LOCAL VIDEO PIP — small preview of own camera
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun LocalVideoPip(
    localVideoTrack: VideoTrack?,
    eglContext: EglBase.Context?,
    isCameraOn: Boolean,
    modifier: Modifier = Modifier
) {
    if (localVideoTrack != null && eglContext != null && isCameraOn) {
        Surface(
            modifier = modifier
                .width(120.dp)
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        init(eglContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setMirror(true)
                        setEnableHardwareScaler(true)
                        localVideoTrack.addSink(this)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { renderer ->
                    localVideoTrack.removeSink(renderer)
                    renderer.release()
                }
            )
        }
    } else {
        // Camera off placeholder
        Surface(
            modifier = modifier
                .width(120.dp)
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2A2A3E)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.VideocamOff,
                    contentDescription = "Camera off",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// INCOMING CALL BUTTONS — Accept / Decline
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun IncomingCallButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Decline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDecline),
                shape = CircleShape,
                color = Color(0xFFFF3B30)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Decline", color = Color.White, fontSize = 14.sp)
        }

        // Accept
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAccept),
                shape = CircleShape,
                color = Color(0xFF34C759)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Accept", color = Color.White, fontSize = 14.sp)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// CALL CONTROL BAR — mute, speaker, camera, flip, end
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun CallControlBar(
    callManager: WebRTCCallManager,
    isVideo: Boolean,
    onEndCall: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Black.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute
            CallControlButton(
                icon = if (callManager.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                label = if (callManager.isMuted) "Unmute" else "Mute",
                isActive = callManager.isMuted,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    callManager.toggleMute()
                }
            )

            // Speaker
            CallControlButton(
                icon = if (callManager.isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp
                       else Icons.AutoMirrored.Filled.VolumeOff,
                label = if (callManager.isSpeakerOn) "Speaker" else "Earpiece",
                isActive = callManager.isSpeakerOn,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    callManager.toggleSpeaker(context)
                }
            )

            // Camera toggle (video calls only)
            if (isVideo) {
                CallControlButton(
                    icon = if (callManager.isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                    label = if (callManager.isCameraOn) "Cam On" else "Cam Off",
                    isActive = !callManager.isCameraOn,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        callManager.toggleCamera()
                    }
                )

                // Switch camera
                CallControlButton(
                    icon = Icons.Filled.FlipCameraAndroid,
                    label = "Flip",
                    isActive = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        callManager.switchCamera()
                    }
                )
            }

            // End call
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable {
                        onEndCall()
                        onDismiss()
                    },
                shape = CircleShape,
                color = Color(0xFFFF3B30)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isActive) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


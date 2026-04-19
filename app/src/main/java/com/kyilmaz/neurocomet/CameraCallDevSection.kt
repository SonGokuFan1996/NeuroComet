package com.kyilmaz.neurocomet

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═════════════════════════════════════════════════════════════════════════════
// CAMERA & CALL UI TESTING
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun CameraCallTestingSection() {
    val context = LocalContext.current

    // ── Permission state ────────────────────────────────────────────────
    var cameraGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var audioGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        cameraGranted = results[android.Manifest.permission.CAMERA] == true
        audioGranted = results[android.Manifest.permission.RECORD_AUDIO] == true
    }

    // ── Camera info ─────────────────────────────────────────────────────
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
    }
    val cameraIds = remember { try { cameraManager.cameraIdList.toList() } catch (_: Exception) { emptyList() } }
    val hasFrontCamera = remember {
        cameraIds.any {
            try {
                val chars = cameraManager.getCameraCharacteristics(it)
                chars[android.hardware.camera2.CameraCharacteristics.LENS_FACING] ==
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
            } catch (_: Exception) { false }
        }
    }
    val hasBackCamera = remember {
        cameraIds.any {
            try {
                val chars = cameraManager.getCameraCharacteristics(it)
                chars[android.hardware.camera2.CameraCharacteristics.LENS_FACING] ==
                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            } catch (_: Exception) { false }
        }
    }

    // ── Camera preview toggle ───────────────────────────────────────────
    var showCameraPreview by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(true) }

    // ── Log messages ────────────────────────────────────────────────────
    val logLines = remember { mutableStateListOf<String>() }
    fun log(msg: String) {
        logLines.add(0, "\u2022 $msg")
        if (logLines.size > 20) logLines.removeAt(logLines.lastIndex)
    }

    DevSectionCard(title = "Camera & Call UI", icon = Icons.Filled.Videocam) {

        // ── 1. Permission status ────────────────────────────────────────
        Text(
            "Permissions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CameraPermissionChip(label = "Camera", granted = cameraGranted)
            CameraPermissionChip(label = "Microphone", granted = audioGranted)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO
                    )
                )
                log("Requested camera + mic permissions")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Request Permissions")
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── 2. Camera info ──────────────────────────────────────────────
        Text(
            "Device Cameras",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CameraInfoChip("Front", hasFrontCamera)
            CameraInfoChip("Back", hasBackCamera)
            CameraInfoChip("IDs: ${cameraIds.size}", true)
        }

        Spacer(Modifier.height(12.dp))

        // ── 3. Live camera preview ──────────────────────────────────────
        Text(
            "Live Camera Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    if (!cameraGranted) {
                        log("\u26A0 Camera permission not granted")
                        Toast.makeText(context, "Grant camera permission first", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    showCameraPreview = !showCameraPreview
                    log(if (showCameraPreview) "Camera preview opened" else "Camera preview closed")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (showCameraPreview) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (showCameraPreview) "Stop Preview" else "Start Preview", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    useFrontCamera = !useFrontCamera
                    log("Switched to ${if (useFrontCamera) "front" else "back"} camera")
                },
                enabled = showCameraPreview,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (useFrontCamera) "\u2192 Back" else "\u2192 Front", fontSize = 12.sp)
            }
        }

        if (showCameraPreview && cameraGranted) {
            Spacer(Modifier.height(8.dp))
            CameraPreviewView(
                useFrontCamera = useFrontCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── 3b. Rotation testing ────────────────────────────────────────
        Text(
            "Rotation Testing",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Force the screen to a specific orientation to verify the camera preview adapts correctly. Tap 'Auto' to return to sensor-based rotation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // Current orientation info
        val activity = context as? android.app.Activity
        @Suppress("DEPRECATION")
        val currentRotation = (context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager)
            ?.defaultDisplay?.rotation ?: -1
        val currentOrientation = activity?.requestedOrientation
            ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        val rotationLabel = when (currentRotation) {
            android.view.Surface.ROTATION_0 -> "0\u00B0 (Portrait)"
            android.view.Surface.ROTATION_90 -> "90\u00B0 (Landscape L)"
            android.view.Surface.ROTATION_180 -> "180\u00B0 (Reverse Portrait)"
            android.view.Surface.ROTATION_270 -> "270\u00B0 (Landscape R)"
            else -> "Unknown"
        }
        val lockLabel = when (currentOrientation) {
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> "Portrait"
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> "Landscape"
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> "Reverse Portrait"
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> "Reverse Landscape"
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR,
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> "Auto (Sensor)"
            else -> "Other ($currentOrientation)"
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Display rotation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(rotationLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Orientation lock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(lockLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Orientation override buttons \u2014 row 1: Portrait / Landscape
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    log("Forced Portrait orientation")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.StayCurrentPortrait, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Portrait", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    log("Forced Landscape orientation")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.StayCurrentLandscape, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Landscape", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(6.dp))

        // Row 2: Reverse portrait / Reverse landscape
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    log("Forced Reverse Portrait orientation")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ScreenRotation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rev. Portrait", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    log("Forced Reverse Landscape orientation")
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ScreenRotation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Rev. Landscape", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(6.dp))

        // Row 3: Auto (sensor) reset
        Button(
            onClick = {
                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                log("Restored Auto (full sensor) orientation")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.ScreenRotationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Auto (Sensor)")
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── 4. Mock call launchers ──────────────────────────────────────
        Text(
            "Call UI Testing",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Launch mock voice / video calls to verify the call screen UI, controls, and camera integration.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // Voice call buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    MockCallManager.startCall(
                        recipientId = "DinoLover99",
                        recipientName = "DinoLover99",
                        recipientAvatar = "https://i.pravatar.cc/150?u=dinolover99",
                        callType = CallType.VOICE
                    )
                    log("Started outgoing VOICE call \u2192 DinoLover99")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
            ) {
                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Voice Call", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    MockCallManager.simulateIncomingCall(
                        callerId = "NeuroNaut",
                        callerName = "NeuroNaut",
                        callerAvatar = "https://i.pravatar.cc/150?u=neuronaut",
                        callType = CallType.VOICE
                    )
                    log("Simulated incoming VOICE call \u2190 NeuroNaut")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853))
            ) {
                Icon(Icons.AutoMirrored.Filled.PhoneCallback, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Incoming Voice", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Video call buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    MockCallManager.startCall(
                        recipientId = "SensorySeeker",
                        recipientName = "SensorySeeker",
                        recipientAvatar = "https://i.pravatar.cc/150?u=sensoryseeker",
                        callType = CallType.VIDEO
                    )
                    log("Started outgoing VIDEO call \u2192 SensorySeeker")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Video Call", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    MockCallManager.simulateIncomingCall(
                        callerId = "CalmObserver",
                        callerName = "CalmObserver",
                        callerAvatar = "https://i.pravatar.cc/150?u=calmobserver",
                        callType = CallType.VIDEO
                    )
                    log("Simulated incoming VIDEO call \u2190 CalmObserver")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Icon(Icons.Filled.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Incoming Video", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Quick end / reset
        OutlinedButton(
            onClick = {
                if (MockCallManager.currentCall != null) {
                    MockCallManager.endCall()
                    log("Ended current mock call")
                } else {
                    log("No active mock call")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.CallEnd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("End Current Mock Call")
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── 5. Call state info ──────────────────────────────────────────
        Text(
            "Mock Call State",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        val currentMockCall = MockCallManager.currentCall
        val currentMockState = MockCallManager.callState
        val currentMockDuration = MockCallManager.callDuration

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (currentMockCall != null)
                    Color(0xFF1B5E20).copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                if (currentMockCall != null) {
                    Text(
                        "Active: ${currentMockCall.callType} call with ${currentMockCall.recipientName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "State: $currentMockState  \u2022  Duration: ${currentMockDuration}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Direction: ${if (currentMockCall.isOutgoing) "Outgoing \u2197" else "Incoming \u2199"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "No active mock call",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── 6. Event log ────────────────────────────────────────────────
        if (logLines.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Event Log",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    logLines.take(10).forEach { line ->
                        Text(
                            line,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── Permission / camera info chips ──────────────────────────────────────────

@Composable
private fun CameraPermissionChip(label: String, granted: Boolean) {
    Surface(
        color = if (granted) Color(0xFF4CAF50).copy(alpha = 0.2f)
        else Color(0xFFFF5722).copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (granted) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (granted) Color(0xFF4CAF50) else Color(0xFFFF5722),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CameraInfoChip(label: String, available: Boolean) {
    Surface(
        color = if (available) Color(0xFF2196F3).copy(alpha = 0.15f)
        else Color(0xFF9E9E9E).copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (available) Icons.Filled.CameraAlt else Icons.Filled.NoPhotography,
                contentDescription = null,
                tint = if (available) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Live camera preview composable using Camera2 + TextureView ──────────────

@Composable
private fun CameraPreviewView(
    useFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = Color.Black) {
        Box(contentAlignment = Alignment.Center) {
            key(useFrontCamera) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.view.TextureView(ctx).also { textureView ->
                            textureView.surfaceTextureListener =
                                object : android.view.TextureView.SurfaceTextureListener {
                                    private var cameraDevice: android.hardware.camera2.CameraDevice? = null

                                    override fun onSurfaceTextureAvailable(
                                        surface: android.graphics.SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) {
                                        openCamera(ctx, surface, useFrontCamera, textureView) { cameraDevice = it }
                                    }

                                    override fun onSurfaceTextureSizeChanged(
                                        surface: android.graphics.SurfaceTexture,
                                        width: Int,
                                        height: Int
                                    ) {
                                        centerCropTransform(textureView)
                                    }

                                    override fun onSurfaceTextureDestroyed(
                                        surface: android.graphics.SurfaceTexture
                                    ): Boolean {
                                        cameraDevice?.close()
                                        cameraDevice = null
                                        return true
                                    }

                                    override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
                                }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = if (useFrontCamera) "Front Camera" else "Back Camera",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun centerCropTransform(textureView: android.view.TextureView) {
    val bufferSize = textureView.tag as? android.util.Size ?: return
    val viewWidth = textureView.width.toFloat()
    val viewHeight = textureView.height.toFloat()
    if (viewWidth == 0f || viewHeight == 0f) return

    val matrix = android.graphics.Matrix()
    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f

    val effectivePreviewW = bufferSize.height.toFloat()
    val effectivePreviewH = bufferSize.width.toFloat()

    val ratioView = viewWidth / viewHeight
    val ratioPreview = effectivePreviewW / effectivePreviewH

    val scaleX: Float
    val scaleY: Float
    if (ratioView > ratioPreview) {
        scaleX = 1f
        scaleY = ratioView / ratioPreview
    } else {
        scaleX = ratioPreview / ratioView
        scaleY = 1f
    }

    matrix.setScale(scaleX, scaleY, centerX, centerY)
    textureView.setTransform(matrix)
}

@Suppress("MissingPermission")
private fun openCamera(
    context: Context,
    surfaceTexture: android.graphics.SurfaceTexture,
    useFront: Boolean,
    textureView: android.view.TextureView,
    onOpened: (android.hardware.camera2.CameraDevice) -> Unit
) {
    try {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        val targetFacing = if (useFront)
            android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT
        else
            android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK

        val cameraId = manager.cameraIdList.firstOrNull { id ->
            try {
                manager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.LENS_FACING) == targetFacing
            } catch (_: Exception) { false }
        } ?: manager.cameraIdList.firstOrNull() ?: return

        val chars = manager.getCameraCharacteristics(cameraId)
        val map = chars.get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val previewSizes = map?.getOutputSizes(android.graphics.SurfaceTexture::class.java)
        val bestSize = previewSizes
            ?.sortedByDescending { it.width * it.height }
            ?.firstOrNull { it.width * it.height <= 1920 * 1080 }
            ?: previewSizes?.firstOrNull()
        if (bestSize != null) {
            surfaceTexture.setDefaultBufferSize(bestSize.width, bestSize.height)
            textureView.tag = bestSize
            textureView.post { centerCropTransform(textureView) }
        }

        val surface = android.view.Surface(surfaceTexture)

        manager.openCamera(
            cameraId,
            object : android.hardware.camera2.CameraDevice.StateCallback() {
                override fun onOpened(camera: android.hardware.camera2.CameraDevice) {
                    onOpened(camera)
                    try {
                        val builder = camera.createCaptureRequest(
                            android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
                        ).apply { addTarget(surface) }
                        @Suppress("DEPRECATION")
                        camera.createCaptureSession(
                            listOf(surface),
                            object : android.hardware.camera2.CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: android.hardware.camera2.CameraCaptureSession) {
                                    try {
                                        session.setRepeatingRequest(builder.build(), null, null)
                                    } catch (e: Exception) {
                                        Log.e("CameraCallDev", "Preview start failed: ${e.message}")
                                    }
                                }
                                override fun onConfigureFailed(session: android.hardware.camera2.CameraCaptureSession) {
                                    Log.e("CameraCallDev", "Session configure failed")
                                }
                            },
                            null
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCallDev", "Capture session creation failed: ${e.message}")
                    }
                }

                override fun onDisconnected(camera: android.hardware.camera2.CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: android.hardware.camera2.CameraDevice, error: Int) {
                    Log.e("CameraCallDev", "Camera error: $error")
                    camera.close()
                }
            },
            null
        )
    } catch (e: Exception) {
        Log.e("CameraCallDev", "openCamera failed: ${e.message}")
    }
}


package com.kyilmaz.neuronetworkingtitle

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MediaUtils"

/**
 * Voice message recorder with production-ready implementation.
 */
class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

    /**
     * Check if recording permission is granted.
     */
    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Start recording a voice message.
     * @return true if recording started successfully, false otherwise
     */
    fun startRecording(): Boolean {
        if (!hasRecordPermission()) {
            Log.w(TAG, "Recording permission not granted")
            return false
        }

        try {
            // Create output file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "voice_${timestamp}.m4a"
            outputFile = File(context.cacheDir, fileName)

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            Log.d(TAG, "Recording started: ${outputFile?.absolutePath}")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            cleanup()
            return false
        }
    }

    /**
     * Stop recording and return the recorded file.
     * @return The recorded audio file, or null if recording failed
     */
    fun stopRecording(): VoiceMessage? {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return null
        }

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val duration = System.currentTimeMillis() - startTime
            val file = outputFile

            if (file != null && file.exists() && file.length() > 0) {
                Log.d(TAG, "Recording stopped: ${file.absolutePath}, duration: ${duration}ms")
                VoiceMessage(
                    file = file,
                    durationMs = duration,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                Log.w(TAG, "Recording file is empty or missing")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            cleanup()
            null
        }
    }

    /**
     * Cancel the current recording without saving.
     */
    fun cancelRecording() {
        cleanup()
    }

    /**
     * Check if currently recording.
     */
    fun isCurrentlyRecording(): Boolean = isRecording

    /**
     * Get current recording duration in milliseconds.
     */
    fun getCurrentDuration(): Long {
        return if (isRecording) {
            System.currentTimeMillis() - startTime
        } else {
            0
        }
    }

    private fun cleanup() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaRecorder", e)
        }
        mediaRecorder = null
        isRecording = false
        outputFile?.delete()
        outputFile = null
    }
}

/**
 * Represents a recorded voice message.
 */
data class VoiceMessage(
    val file: File,
    val durationMs: Long,
    val timestamp: Long
) {
    val durationFormatted: String
        get() {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / 1000) / 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

/**
 * Voice message player.
 */
class VoicePlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null
    var isPlaying by mutableStateOf(false)
        private set
    var progress by mutableFloatStateOf(0f)
        private set

    fun play(file: File, onComplete: () -> Unit = {}) {
        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
            currentFile = file
            isPlaying = true

            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                progress = 0f
                onComplete()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio", e)
            Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        isPlaying = false
    }

    fun resume() {
        mediaPlayer?.start()
        isPlaying = true
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping player", e)
        }
        mediaPlayer = null
        currentFile = null
        isPlaying = false
        progress = 0f
    }
}

/**
 * Attachment types supported by the app.
 */
enum class AttachmentType {
    IMAGE,
    VIDEO,
    DOCUMENT,
    AUDIO,
    LOCATION,
    CONTACT,
    VOICE_MESSAGE
}

/**
 * Represents an attachment to be sent with a message.
 */
data class MessageAttachment(
    val type: AttachmentType,
    val uri: Uri? = null,
    val file: File? = null,
    val displayName: String = "",
    val mimeType: String = "",
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Helper object for handling attachments.
 */
object AttachmentHelper {

    /**
     * Check if camera permission is granted.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if contacts permission is granted.
     */
    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if media/storage permission is granted.
     */
    fun hasMediaPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Create a temporary file for camera capture.
     */
    fun createTempImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timestamp}.jpg"
        return File(context.cacheDir, fileName)
    }

    /**
     * Get URI for a file using FileProvider.
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Get file name from URI.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "Unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    /**
     * Get mime type from URI.
     */
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }
}

/**
 * Composable state holder for attachment handling.
 */
@Composable
fun rememberAttachmentState(
    onAttachmentReady: (MessageAttachment) -> Unit
): AttachmentState {
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onAttachmentReady(
                MessageAttachment(
                    type = AttachmentType.IMAGE,
                    uri = it,
                    displayName = AttachmentHelper.getFileName(context, it),
                    mimeType = AttachmentHelper.getMimeType(context, it)
                )
            )
        }
    }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onAttachmentReady(
                MessageAttachment(
                    type = AttachmentType.DOCUMENT,
                    uri = it,
                    displayName = AttachmentHelper.getFileName(context, it),
                    mimeType = AttachmentHelper.getMimeType(context, it)
                )
            )
        }
    }

    // Audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onAttachmentReady(
                MessageAttachment(
                    type = AttachmentType.AUDIO,
                    uri = it,
                    displayName = AttachmentHelper.getFileName(context, it),
                    mimeType = AttachmentHelper.getMimeType(context, it)
                )
            )
        }
    }

    // Contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            onAttachmentReady(
                MessageAttachment(
                    type = AttachmentType.CONTACT,
                    uri = it,
                    displayName = "Contact"
                )
            )
        }
    }

    // Camera launcher
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            onAttachmentReady(
                MessageAttachment(
                    type = AttachmentType.IMAGE,
                    file = tempCameraFile,
                    displayName = tempCameraFile?.name ?: "Photo",
                    mimeType = "image/jpeg"
                )
            )
        }
        tempCameraFile = null
    }

    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tempCameraFile = AttachmentHelper.createTempImageFile(context)
            tempCameraFile?.let { file ->
                val uri = AttachmentHelper.getUriForFile(context, file)
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // TODO: Get location and create attachment
            Toast.makeText(context, "Getting location...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contacts permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, state will handle this
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    return remember {
        AttachmentState(
            context = context,
            onPickImage = { imagePickerLauncher.launch("image/*") },
            onTakePhoto = {
                if (AttachmentHelper.hasCameraPermission(context)) {
                    tempCameraFile = AttachmentHelper.createTempImageFile(context)
                    tempCameraFile?.let { file ->
                        val uri = AttachmentHelper.getUriForFile(context, file)
                        cameraLauncher.launch(uri)
                    }
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onPickDocument = { documentPickerLauncher.launch(arrayOf("*/*")) },
            onShareLocation = {
                if (AttachmentHelper.hasLocationPermission(context)) {
                    // TODO: Get location
                    Toast.makeText(context, "Getting location...", Toast.LENGTH_SHORT).show()
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            onPickContact = {
                if (AttachmentHelper.hasContactsPermission(context)) {
                    contactPickerLauncher.launch(null)
                } else {
                    contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            onPickAudio = { audioPickerLauncher.launch("audio/*") },
            onRequestAudioPermission = {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
    }
}

/**
 * State holder for attachment operations.
 */
class AttachmentState(
    private val context: Context,
    val onPickImage: () -> Unit,
    val onTakePhoto: () -> Unit,
    val onPickDocument: () -> Unit,
    val onShareLocation: () -> Unit,
    val onPickContact: () -> Unit,
    val onPickAudio: () -> Unit,
    val onRequestAudioPermission: () -> Unit
) {
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}


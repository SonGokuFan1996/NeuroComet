package com.kyilmaz.neuronetworkingtitle.calling

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.webrtc.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "WebRTCCallManager"

/**
 * Call Types
 */
enum class CallType {
    VOICE,
    VIDEO
}

/**
 * Call State
 */
enum class CallState {
    IDLE,           // No active call
    RINGING,        // Outgoing call ringing
    INCOMING,       // Incoming call
    CONNECTING,     // Establishing connection
    CONNECTED,      // Call in progress
    RECONNECTING,   // Reconnecting after network issue
    ENDED           // Call ended
}

/**
 * Call outcome for history
 */
enum class CallOutcome {
    COMPLETED,    // Call was answered and ended normally
    MISSED,       // Incoming call was not answered
    DECLINED,     // Call was declined
    NO_ANSWER,    // Outgoing call was not answered
    CANCELLED,    // Outgoing call was cancelled before answer
    FAILED        // Call failed due to technical issues
}

/**
 * Call Quality Metrics
 */
data class CallQualityMetrics(
    val audioBitrate: Int = 0,
    val videoBitrate: Int = 0,
    val packetLoss: Float = 0f,
    val roundTripTime: Long = 0L,
    val isGoodQuality: Boolean = true
)

/**
 * Call History Entry
 */
@Serializable
data class CallHistoryEntry(
    val id: String = "call_${System.currentTimeMillis()}",
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val callType: String, // "VOICE" or "VIDEO"
    val isOutgoing: Boolean,
    val outcome: String,
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

    val callTypeEnum: CallType get() = CallType.valueOf(callType)
    val outcomeEnum: CallOutcome get() = CallOutcome.valueOf(outcome)
}

/**
 * Active Call Data
 */
data class ActiveCall(
    val callId: String,
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String,
    val callType: CallType,
    val isOutgoing: Boolean,
    val startTime: Long = System.currentTimeMillis()
)

/**
 * Signaling message types
 */
@Serializable
data class SignalingMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val callId: String,
    val fromUserId: String,
    val toUserId: String,
    val type: String, // "offer", "answer", "ice_candidate", "call_request", "call_accept", "call_decline", "call_end"
    val payload: String, // JSON encoded SDP or ICE candidate
    val callType: String = "VOICE",
    val timestamp: String = Instant.now().toString()
)

/**
 * WebRTC Call Manager - Handles real voice/video calls using WebRTC
 *
 * Features:
 * - Real-time audio/video calls using WebRTC
 * - Signaling via Supabase Realtime
 * - Call history persistence
 * - Quality monitoring
 * - Audio routing (speaker/earpiece)
 */
class WebRTCCallManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: WebRTCCallManager? = null

        fun getInstance(): WebRTCCallManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebRTCCallManager().also { INSTANCE = it }
            }
        }
    }

    // State
    var currentCall by mutableStateOf<ActiveCall?>(null)
        private set

    var callState by mutableStateOf(CallState.IDLE)
        private set

    var callDuration by mutableLongStateOf(0L)
        private set

    var isMuted by mutableStateOf(false)
        private set

    var isSpeakerOn by mutableStateOf(false)
        private set

    var isCameraOn by mutableStateOf(true)
        private set

    var isUsingFrontCamera by mutableStateOf(true)
        private set

    var callQuality by mutableStateOf(CallQualityMetrics())
        private set

    var localVideoTrack by mutableStateOf<VideoTrack?>(null)
        private set

    var remoteVideoTrack by mutableStateOf<VideoTrack?>(null)
        private set

    // Call history
    private val _callHistory = mutableStateListOf<CallHistoryEntry>()
    val callHistory: List<CallHistoryEntry> get() = _callHistory.toList()

    // WebRTC components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var eglBase: EglBase? = null

    // Context and dependencies
    private var appContext: Context? = null
    private var supabaseClient: SupabaseClient? = null
    private var currentUserId: String? = null

    // Coroutine scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var callStartTime: Long = 0L
    private var durationJob: kotlinx.coroutines.Job? = null

    // JSON parser
    private val json = Json { ignoreUnknownKeys = true }

    // ICE servers for STUN/TURN
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
    )

    /**
     * Initialize the call manager with context and Supabase client
     */
    fun initialize(context: Context, supabase: SupabaseClient?, userId: String?) {
        appContext = context.applicationContext
        supabaseClient = supabase
        currentUserId = userId

        initializeWebRTC(context)
        setupSignalingListener()
        loadCallHistory()
    }

    private fun initializeWebRTC(context: Context) {
        try {
            // Initialize EGL context for video rendering
            eglBase = EglBase.create()

            // Initialize PeerConnectionFactory
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            val encoderFactory = DefaultVideoEncoderFactory(
                eglBase?.eglBaseContext,
                true,
                true
            )
            val decoderFactory = DefaultVideoDecoderFactory(eglBase?.eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(PeerConnectionFactory.Options())
                .createPeerConnectionFactory()

            Log.d(TAG, "WebRTC initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WebRTC", e)
        }
    }

    private fun setupSignalingListener() {
        val client = supabaseClient ?: return
        val userId = currentUserId ?: return

        scope.launch {
            try {
                val channel = client.realtime.channel("calls:$userId")

                // Subscribe to call signals for this user
                // Note: Server-side filter removed - we'll filter client-side by to_user_id
                channel.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "call_signals"
                }.onEach { change ->
                    // Client-side filter: only handle signals for this user
                    val toUserId = change.record["to_user_id"] as? String
                    if (toUserId == userId) {
                        handleSignalingMessage(change.record)
                    }
                }.launchIn(scope)

                channel.subscribe()
                Log.d(TAG, "Signaling listener setup for user: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to setup signaling listener", e)
            }
        }
    }

    private suspend fun handleSignalingMessage(record: Map<String, Any?>) {
        try {
            val type = record["type"] as? String ?: return
            val callId = record["call_id"] as? String ?: return
            val fromUserId = record["from_user_id"] as? String ?: return
            val payload = record["payload"] as? String ?: ""
            val callTypeStr = record["call_type"] as? String ?: "VOICE"

            Log.d(TAG, "Received signaling message: type=$type, callId=$callId")

            when (type) {
                "call_request" -> {
                    // Incoming call
                    val callerName = record["caller_name"] as? String ?: "Unknown"
                    val callerAvatar = record["caller_avatar"] as? String ?: ""

                    handleIncomingCall(
                        callId = callId,
                        callerId = fromUserId,
                        callerName = callerName,
                        callerAvatar = callerAvatar,
                        callType = CallType.valueOf(callTypeStr)
                    )
                }
                "call_accept" -> {
                    // Call was accepted, create answer
                    if (currentCall?.callId == callId) {
                        callState = CallState.CONNECTING
                    }
                }
                "call_decline" -> {
                    // Call was declined
                    if (currentCall?.callId == callId) {
                        endCall(CallOutcome.DECLINED)
                    }
                }
                "call_end" -> {
                    // Remote party ended call
                    if (currentCall?.callId == callId) {
                        endCall(CallOutcome.COMPLETED)
                    }
                }
                "offer" -> {
                    handleOffer(callId, payload)
                }
                "answer" -> {
                    handleAnswer(payload)
                }
                "ice_candidate" -> {
                    handleIceCandidate(payload)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling signaling message", e)
        }
    }

    /**
     * Start an outgoing call
     */
    fun startCall(
        recipientId: String,
        recipientName: String,
        recipientAvatar: String,
        callType: CallType
    ) {
        if (callState != CallState.IDLE) {
            Log.w(TAG, "Cannot start call - already in call state: $callState")
            return
        }

        val callId = "call_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}"

        currentCall = ActiveCall(
            callId = callId,
            recipientId = recipientId,
            recipientName = recipientName,
            recipientAvatar = recipientAvatar,
            callType = callType,
            isOutgoing = true
        )

        callState = CallState.RINGING
        callDuration = 0L
        callStartTime = System.currentTimeMillis()

        // Setup WebRTC and send offer
        scope.launch {
            try {
                setupPeerConnection(callType)
                createAndSendOffer(recipientId, callId, callType)

                // Timeout after 60 seconds if no answer
                delay(60000)
                if (callState == CallState.RINGING) {
                    endCall(CallOutcome.NO_ANSWER)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start call", e)
                endCall(CallOutcome.FAILED)
            }
        }

        Log.d(TAG, "Starting ${callType.name} call to $recipientName (ID: $recipientId)")
    }

    private fun handleIncomingCall(
        callId: String,
        callerId: String,
        callerName: String,
        callerAvatar: String,
        callType: CallType
    ) {
        if (callState != CallState.IDLE) {
            // Already in a call, auto-decline
            scope.launch {
                sendSignalingMessage(
                    toUserId = callerId,
                    callId = callId,
                    type = "call_decline",
                    payload = ""
                )
            }
            return
        }

        currentCall = ActiveCall(
            callId = callId,
            recipientId = callerId,
            recipientName = callerName,
            recipientAvatar = callerAvatar,
            callType = callType,
            isOutgoing = false
        )

        callState = CallState.INCOMING
        callDuration = 0L

        // Start ringtone/vibration here if needed

        Log.d(TAG, "Incoming ${callType.name} call from $callerName")
    }

    /**
     * Answer an incoming call
     */
    fun answerCall() {
        val call = currentCall ?: return
        if (callState != CallState.INCOMING) return

        callState = CallState.CONNECTING
        callStartTime = System.currentTimeMillis()

        scope.launch {
            try {
                setupPeerConnection(call.callType)

                // Send accept signal
                sendSignalingMessage(
                    toUserId = call.recipientId,
                    callId = call.callId,
                    type = "call_accept",
                    payload = ""
                )

                Log.d(TAG, "Answered call from ${call.recipientName}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to answer call", e)
                endCall(CallOutcome.FAILED)
            }
        }
    }

    /**
     * Decline an incoming call
     */
    fun declineCall() {
        val call = currentCall ?: return

        scope.launch {
            sendSignalingMessage(
                toUserId = call.recipientId,
                callId = call.callId,
                type = "call_decline",
                payload = ""
            )
        }

        endCall(CallOutcome.DECLINED)
    }

    /**
     * End the current call
     */
    fun endCall(outcome: CallOutcome = CallOutcome.COMPLETED) {
        val call = currentCall ?: return

        // Calculate duration
        val durationSecs = if (callState == CallState.CONNECTED) {
            (System.currentTimeMillis() - callStartTime) / 1000
        } else 0L

        // Send end signal to remote
        scope.launch {
            try {
                sendSignalingMessage(
                    toUserId = call.recipientId,
                    callId = call.callId,
                    type = "call_end",
                    payload = ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send end signal", e)
            }
        }

        // Add to history
        val historyEntry = CallHistoryEntry(
            id = call.callId,
            recipientId = call.recipientId,
            recipientName = call.recipientName,
            recipientAvatar = call.recipientAvatar,
            callType = call.callType.name,
            isOutgoing = call.isOutgoing,
            outcome = outcome.name,
            durationSeconds = durationSecs
        )
        _callHistory.add(0, historyEntry)
        saveCallToHistory(historyEntry)

        // Cleanup WebRTC
        cleanupCall()

        // Reset state
        callState = CallState.ENDED
        durationJob?.cancel()

        // After a brief delay, reset to idle
        scope.launch {
            delay(1500)
            if (callState == CallState.ENDED) {
                callState = CallState.IDLE
                currentCall = null
            }
        }

        Log.d(TAG, "Call ended: $outcome, duration: ${durationSecs}s")
    }

    private fun setupPeerConnection(callType: CallType) {
        val factory = peerConnectionFactory ?: return
        val context = appContext ?: return

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "Signaling state: $state")
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE connection state: $state")
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        callState = CallState.CONNECTED
                        startDurationTimer()
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        callState = CallState.RECONNECTING
                    }
                    PeerConnection.IceConnectionState.FAILED -> {
                        endCall(CallOutcome.FAILED)
                    }
                    else -> {}
                }
            }

            override fun onIceConnectionReceivingChange(receiving: Boolean) {}

            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                Log.d(TAG, "ICE gathering state: $state")
            }

            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { sendIceCandidate(it) }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

            override fun onAddStream(stream: MediaStream?) {
                stream?.videoTracks?.firstOrNull()?.let { track ->
                    remoteVideoTrack = track
                }
            }

            override fun onRemoveStream(stream: MediaStream?) {
                remoteVideoTrack = null
            }

            override fun onDataChannel(channel: DataChannel?) {}

            override fun onRenegotiationNeeded() {
                Log.d(TAG, "Renegotiation needed")
            }

            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                receiver?.track()?.let { track ->
                    if (track is VideoTrack) {
                        remoteVideoTrack = track
                    }
                }
            }
        })

        // Add audio track
        addAudioTrack()

        // Add video track if video call
        if (callType == CallType.VIDEO) {
            addVideoTrack(context)
        }
    }

    private fun addAudioTrack() {
        val factory = peerConnectionFactory ?: return

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        val audioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("audio_track", audioSource)

        peerConnection?.addTrack(localAudioTrack, listOf("stream"))
    }

    private fun addVideoTrack(context: Context) {
        val factory = peerConnectionFactory ?: return

        // Create video capturer
        videoCapturer = createVideoCapturer(context)

        videoCapturer?.let { capturer ->
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase?.eglBaseContext)

            val videoSource = factory.createVideoSource(capturer.isScreencast)
            capturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            capturer.startCapture(1280, 720, 30)

            localVideoTrack = factory.createVideoTrack("video_track", videoSource)
            peerConnection?.addTrack(localVideoTrack, listOf("stream"))
        }
    }

    private fun createVideoCapturer(context: Context): VideoCapturer? {
        return try {
            val enumerator = Camera2Enumerator(context)
            val deviceNames = enumerator.deviceNames

            // Try front camera first
            for (deviceName in deviceNames) {
                if (enumerator.isFrontFacing(deviceName)) {
                    val capturer = enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        isUsingFrontCamera = true
                        return capturer
                    }
                }
            }

            // Fall back to back camera
            for (deviceName in deviceNames) {
                if (enumerator.isBackFacing(deviceName)) {
                    val capturer = enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        isUsingFrontCamera = false
                        return capturer
                    }
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create video capturer", e)
            null
        }
    }

    private suspend fun createAndSendOffer(recipientId: String, callId: String, callType: CallType) {
        val pc = peerConnection ?: return

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                if (callType == CallType.VIDEO) "true" else "false"))
        }

        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let { sessionDesc ->
                    pc.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            scope.launch {
                                sendSignalingMessage(
                                    toUserId = recipientId,
                                    callId = callId,
                                    type = "offer",
                                    payload = sessionDesc.description,
                                    callType = callType
                                )
                            }
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, sessionDesc)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun handleOffer(callId: String, sdpDescription: String) {
        val pc = peerConnection ?: return
        val call = currentCall ?: return

        val sdp = SessionDescription(SessionDescription.Type.OFFER, sdpDescription)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                createAndSendAnswer(call.recipientId, callId)
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description: $error")
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    private fun createAndSendAnswer(recipientId: String, callId: String) {
        val pc = peerConnection ?: return

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        pc.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let { sessionDesc ->
                    pc.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            scope.launch {
                                sendSignalingMessage(
                                    toUserId = recipientId,
                                    callId = callId,
                                    type = "answer",
                                    payload = sessionDesc.description
                                )
                            }
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Failed to set local description: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, sessionDesc)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun handleAnswer(sdpDescription: String) {
        val pc = peerConnection ?: return

        val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpDescription)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully")
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote description: $error")
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val call = currentCall ?: return

        val candidateJson = """
            {
                "sdpMid": "${candidate.sdpMid}",
                "sdpMLineIndex": ${candidate.sdpMLineIndex},
                "sdp": "${candidate.sdp}"
            }
        """.trimIndent()

        scope.launch {
            sendSignalingMessage(
                toUserId = call.recipientId,
                callId = call.callId,
                type = "ice_candidate",
                payload = candidateJson
            )
        }
    }

    private fun handleIceCandidate(candidateJson: String) {
        val pc = peerConnection ?: return

        try {
            // Parse the JSON manually for simplicity
            val sdpMid = Regex(""""sdpMid":\s*"([^"]+)"""").find(candidateJson)?.groupValues?.get(1)
            val sdpMLineIndex = Regex(""""sdpMLineIndex":\s*(\d+)""").find(candidateJson)?.groupValues?.get(1)?.toIntOrNull()
            val sdp = Regex(""""sdp":\s*"([^"]+)"""").find(candidateJson)?.groupValues?.get(1)

            if (sdpMid != null && sdpMLineIndex != null && sdp != null) {
                val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                pc.addIceCandidate(candidate)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ICE candidate", e)
        }
    }

    private suspend fun sendSignalingMessage(
        toUserId: String,
        callId: String,
        type: String,
        payload: String,
        callType: CallType = CallType.VOICE
    ) {
        val client = supabaseClient ?: run {
            Log.w(TAG, "Supabase client not available - using mock mode")
            // In mock mode, just log and simulate
            simulateMockResponse(type, callId)
            return
        }
        val userId = currentUserId ?: return
        val call = currentCall

        try {
            client.from("call_signals").insert(
                mapOf(
                    "call_id" to callId,
                    "from_user_id" to userId,
                    "to_user_id" to toUserId,
                    "type" to type,
                    "payload" to payload,
                    "call_type" to callType.name,
                    "caller_name" to (call?.recipientName ?: "Unknown"),
                    "caller_avatar" to (call?.recipientAvatar ?: "")
                )
            )
            Log.d(TAG, "Sent signaling message: type=$type, to=$toUserId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send signaling message", e)
        }
    }

    /**
     * Simulate mock responses when Supabase is not available
     */
    private fun simulateMockResponse(type: String, callId: String) {
        scope.launch {
            when (type) {
                "call_request", "offer" -> {
                    // Simulate call being answered after 3 seconds
                    delay(3000)
                    if (callState == CallState.RINGING) {
                        callState = CallState.CONNECTED
                        startDurationTimer()
                    }
                }
            }
        }
    }

    private fun startDurationTimer() {
        durationJob?.cancel()
        callStartTime = System.currentTimeMillis()

        durationJob = scope.launch {
            while (callState == CallState.CONNECTED) {
                delay(1000)
                callDuration = (System.currentTimeMillis() - callStartTime) / 1000
            }
        }
    }

    private fun cleanupCall() {
        // Stop video capture
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping video capture", e)
        }

        // Dispose tracks
        localAudioTrack?.dispose()
        localVideoTrack?.dispose()

        // Close peer connection
        peerConnection?.close()
        peerConnection = null

        // Reset state
        localAudioTrack = null
        localVideoTrack = null
        remoteVideoTrack = null
        videoCapturer = null

        isMuted = false
        isSpeakerOn = false
        isCameraOn = true
    }

    // ==================== CALL CONTROLS ====================

    /**
     * Toggle microphone mute
     */
    fun toggleMute(): Boolean {
        isMuted = !isMuted
        localAudioTrack?.setEnabled(!isMuted)
        Log.d(TAG, "Microphone ${if (isMuted) "muted" else "unmuted"}")
        return isMuted
    }

    /**
     * Toggle speaker
     */
    fun toggleSpeaker(context: Context): Boolean {
        isSpeakerOn = !isSpeakerOn

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = isSpeakerOn

        Log.d(TAG, "Speaker ${if (isSpeakerOn) "on" else "off"}")
        return isSpeakerOn
    }

    /**
     * Toggle camera on/off (video calls only)
     */
    fun toggleCamera(): Boolean {
        isCameraOn = !isCameraOn
        localVideoTrack?.setEnabled(isCameraOn)
        Log.d(TAG, "Camera ${if (isCameraOn) "on" else "off"}")
        return isCameraOn
    }

    /**
     * Switch between front and back camera (video calls only)
     */
    fun switchCamera() {
        val capturer = videoCapturer as? CameraVideoCapturer ?: return

        capturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                isUsingFrontCamera = isFrontCamera
                Log.d(TAG, "Switched to ${if (isFrontCamera) "front" else "back"} camera")
            }

            override fun onCameraSwitchError(error: String?) {
                Log.e(TAG, "Failed to switch camera: $error")
            }
        })
    }

    // ==================== CALL HISTORY ====================

    private fun loadCallHistory() {
        val client = supabaseClient ?: return
        val userId = currentUserId ?: return

        scope.launch {
            try {
                val history = client.from("call_history")
                    .select()
                    .decodeList<CallHistoryEntry>()

                _callHistory.clear()
                _callHistory.addAll(history.sortedByDescending { it.timestamp })
                Log.d(TAG, "Loaded ${history.size} call history entries")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load call history", e)
            }
        }
    }

    private fun saveCallToHistory(entry: CallHistoryEntry) {
        val client = supabaseClient ?: return

        scope.launch {
            try {
                client.from("call_history").insert(entry)
                Log.d(TAG, "Saved call to history: ${entry.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save call to history", e)
            }
        }
    }

    fun clearCallHistory() {
        _callHistory.clear()

        val client = supabaseClient ?: return
        val userId = currentUserId ?: return

        scope.launch {
            try {
                client.from("call_history")
                    .delete {
                        filter {
                            // Delete user's call history
                        }
                    }
                Log.d(TAG, "Cleared call history")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear call history", e)
            }
        }
    }

    /**
     * Add mock call history entries for testing
     */
    fun addMockCallHistory() {
        val mockEntries = listOf(
            CallHistoryEntry(
                id = "mock_1",
                recipientId = "luna",
                recipientName = "Luna",
                recipientAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=luna",
                callType = "VIDEO",
                isOutgoing = false,
                outcome = "COMPLETED",
                timestamp = Instant.now().minusSeconds(3600).toString(),
                durationSeconds = 245
            ),
            CallHistoryEntry(
                id = "mock_2",
                recipientId = "alex",
                recipientName = "Alex",
                recipientAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=alex",
                callType = "VOICE",
                isOutgoing = true,
                outcome = "COMPLETED",
                timestamp = Instant.now().minusSeconds(7200).toString(),
                durationSeconds = 180
            ),
            CallHistoryEntry(
                id = "mock_3",
                recipientId = "jamie",
                recipientName = "Jamie",
                recipientAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=jamie",
                callType = "VOICE",
                isOutgoing = false,
                outcome = "MISSED",
                timestamp = Instant.now().minusSeconds(14400).toString(),
                durationSeconds = 0
            ),
            CallHistoryEntry(
                id = "mock_4",
                recipientId = "sam",
                recipientName = "Sam",
                recipientAvatar = "https://api.dicebear.com/7.x/avataaars/png?seed=sam",
                callType = "VIDEO",
                isOutgoing = true,
                outcome = "NO_ANSWER",
                timestamp = Instant.now().minusSeconds(28800).toString(),
                durationSeconds = 0
            )
        )
        _callHistory.addAll(0, mockEntries)
    }

    /**
     * Get EGL base context for video rendering
     */
    fun getEglBaseContext() = eglBase?.eglBaseContext

    /**
     * Cleanup resources
     */
    fun dispose() {
        cleanupCall()

        peerConnectionFactory?.dispose()
        peerConnectionFactory = null

        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        eglBase?.release()
        eglBase = null
    }
}

/**
 * Format call duration as MM:SS or HH:MM:SS
 */
fun formatCallDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}


package com.kyilmaz.neurocomet.calling

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kyilmaz.neurocomet.BuildConfig
import com.kyilmaz.neurocomet.SecurityUtils
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import com.kyilmaz.neurocomet.safeInsert
import com.kyilmaz.neurocomet.AttachmentHelper
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
    @kotlinx.serialization.SerialName("caller_id")
    val userId: String, // The user this history belongs to
    @kotlinx.serialization.SerialName("recipient_id")
    val recipientId: String,
    @kotlinx.serialization.SerialName("recipient_name")
    val recipientName: String,
    @kotlinx.serialization.SerialName("recipient_avatar")
    val recipientAvatar: String,
    @kotlinx.serialization.SerialName("call_type")
    val callType: String, // "VOICE" or "VIDEO"
    @kotlinx.serialization.SerialName("is_outgoing")
    val isOutgoing: Boolean,
    val outcome: String,
    @kotlinx.serialization.SerialName("created_at")
    val timestamp: String = Instant.now().toString(),
    @kotlinx.serialization.SerialName("duration_seconds")
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
    private var signalingChannel: RealtimeChannel? = null
    private var signalingSetupJob: Job? = null
    private var signalingEventsJob: Job? = null
    private var signalingUserId: String? = null
    private var callHistoryLoadedForUserId: String? = null
    private var isWebRtcInitialized = false
    private var pendingRemoteOfferCallId: String? = null
    private var pendingRemoteOfferSdp: String? = null
    private val pendingIceCandidateSignals = mutableListOf<Pair<String, String>>()
    private var lastOutgoingCallAttemptAtMs: Long = 0L
    private val recentIncomingCallers = mutableMapOf<String, Long>()

    // Coroutine scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var callStartTime: Long = 0L
    private var durationJob: kotlinx.coroutines.Job? = null

    // JSON parser
    private val json = Json { ignoreUnknownKeys = true }
    private val minCallIntervalMs = 10_000L

    // ICE servers for STUN/TURN. TURN credentials can be supplied via
    // local.properties / secrets.properties using TURN_URL, TURN_USERNAME,
    // and TURN_PASSWORD for better connectivity across restrictive NATs.
    private val iceServers: List<PeerConnection.IceServer> by lazy {
        buildList {
            add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer())
            add(PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer())

            val turnUrl = SecurityUtils.decrypt(BuildConfig.TURN_URL)
            val turnUsername = SecurityUtils.decrypt(BuildConfig.TURN_USERNAME)
            val turnPassword = SecurityUtils.decrypt(BuildConfig.TURN_PASSWORD)
            if (turnUrl.isNotBlank()) {
                val builder = PeerConnection.IceServer.builder(turnUrl)
                if (turnUsername.isNotBlank()) {
                    builder.setUsername(turnUsername)
                }
                if (turnPassword.isNotBlank()) {
                    builder.setPassword(turnPassword)
                }
                add(builder.createIceServer())
                Log.d(TAG, "TURN server configured for WebRTC connectivity")
            } else {
                Log.w(TAG, "No TURN server configured; calls may fail on strict NAT/firewall networks")
            }
        }
    }

    /**
     * Whether the local network permission is missing on API 37+ (CinnamonBun).
     * When `true`, WebRTC ICE candidate gathering over LAN may fail.
     * Observe this from the calling UI to prompt the user.
     */
    var localNetworkPermissionNeeded by mutableStateOf(false)
        private set

    /**
     * Initialize the call manager with context and Supabase client
     */
    fun initialize(context: Context, supabase: SupabaseClient?, userId: String?) {
        val previousUserId = currentUserId
        appContext = context.applicationContext
        supabaseClient = supabase
        currentUserId = userId

        // API 37+ requires ACCESS_LOCAL_NETWORK for WebRTC local ICE candidates
        if (!AttachmentHelper.hasLocalNetworkPermission(context)) {
            Log.w(TAG, "ACCESS_LOCAL_NETWORK not granted — WebRTC local ICE candidates may be unavailable")
            localNetworkPermissionNeeded = true
        } else {
            localNetworkPermissionNeeded = false
        }

        if (previousUserId != null && previousUserId != userId) {
            clearSessionStateForUserChange()
        }

        setupSignalingListener()
    }

    private fun initializeWebRTC(context: Context) {
        if (isWebRtcInitialized && peerConnectionFactory != null && eglBase != null) {
            return
        }

        try {
            // Initialize EGL context for video rendering
            eglBase = EglBase.create()

            // Initialize PeerConnectionFactory
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(BuildConfig.DEBUG)
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
            isWebRtcInitialized = true

            Log.d(TAG, "WebRTC initialized successfully")
        } catch (e: Exception) {
            isWebRtcInitialized = false
            Log.e(TAG, "Failed to initialize WebRTC", e)
        }
    }

    private fun ensureWebRtcInitialized() {
        val context = appContext ?: return
        initializeWebRTC(context)
    }

    private fun setupSignalingListener() {
        val client = supabaseClient ?: return
        val userId = currentUserId ?: return

        if (signalingUserId == userId && signalingChannel != null) {
            return
        }

        stopSignalingListener()

        signalingSetupJob = scope.launch {
            try {
                val channel = client.realtime.channel("calls:$userId")
                signalingChannel = channel
                signalingUserId = userId

                // Subscribe to call signals for this user
                // Note: Server-side filter removed - we'll filter client-side by to_user_id
                signalingEventsJob = channel.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "call_signals"
                }.onEach { change ->
                    // Client-side filter: only handle signals for this user
                    val toUserId = change.record["to_user_id"]?.jsonPrimitive?.contentOrNull
                    if (toUserId == userId) {
                        handleSignalingMessage(change.record)
                    }
                }.launchIn(scope)

                channel.subscribe()
                Log.d(TAG, "Signaling listener setup for user: $userId")
            } catch (e: Exception) {
                signalingChannel = null
                signalingUserId = null
                Log.e(TAG, "Failed to setup signaling listener", e)
            }
        }
    }

    private fun stopSignalingListener() {
        signalingSetupJob?.cancel()
        signalingSetupJob = null
        signalingEventsJob?.cancel()
        signalingEventsJob = null

        val existingChannel = signalingChannel
        signalingChannel = null
        signalingUserId = null

        if (existingChannel != null) {
            scope.launch {
                try {
                    existingChannel.unsubscribe()
                } catch (e: Exception) {
                    Log.d(TAG, "Failed to unsubscribe signaling channel cleanly", e)
                }
            }
        }
    }

    private fun clearSessionStateForUserChange() {
        stopSignalingListener()
        callHistoryLoadedForUserId = null
        _callHistory.clear()
        clearPendingSignals()
        if (callState == CallState.IDLE) {
            cleanupCall()
        }
    }

    private fun clearPendingSignals() {
        pendingRemoteOfferCallId = null
        pendingRemoteOfferSdp = null
        pendingIceCandidateSignals.clear()
    }

    private suspend fun handleSignalingMessage(record: JsonObject) {
        try {
            val type = record["type"]?.jsonPrimitive?.contentOrNull ?: return
            val callId = record["call_id"]?.jsonPrimitive?.contentOrNull ?: return
            val fromUserId = record["from_user_id"]?.jsonPrimitive?.contentOrNull ?: return
            val payload = record["payload"]?.jsonPrimitive?.contentOrNull ?: ""
            val callTypeStr = record["call_type"]?.jsonPrimitive?.contentOrNull ?: "VOICE"

            Log.d(TAG, "Received signaling message: type=$type, callId=$callId")

            when (type) {
                "call_request" -> {
                    // Incoming call
                    val callerName = record["caller_name"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                    val callerAvatar = record["caller_avatar"]?.jsonPrimitive?.contentOrNull ?: ""

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
                    handleAnswer(callId, payload)
                }
                "ice_candidate" -> {
                    handleIceCandidate(callId, payload)
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
        val callerId = currentUserId
        if (callerId.isNullOrBlank() || callerId == "guest_user") {
            Log.w(TAG, "Cannot start call without a real authenticated caller")
            return
        }
        if (recipientId.isBlank() || recipientId == callerId) {
            Log.w(TAG, "Blocked invalid or self-call attempt: recipientId=$recipientId callerId=$callerId")
            return
        }
        if (callState != CallState.IDLE) {
            Log.w(TAG, "Cannot start call - already in call state: $callState")
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastOutgoingCallAttemptAtMs < minCallIntervalMs) {
            Log.w(TAG, "Outgoing call throttled to reduce spam/abuse")
            return
        }
        lastOutgoingCallAttemptAtMs = now

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

        // Start dial tone for the caller
        startDialTone()

        // Setup WebRTC and send offer
        scope.launch {
            try {
                ensureWebRtcInitialized()

                // Send call request first to trigger incoming call UI on receiver
                sendSignalingMessage(
                    toUserId = recipientId,
                    callId = callId,
                    type = "call_request",
                    payload = "",
                    callType = callType
                )

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
        val now = System.currentTimeMillis()
        val previousCallAt = recentIncomingCallers[callerId] ?: 0L
        if (now - previousCallAt < minCallIntervalMs) {
            scope.launch {
                sendSignalingMessage(
                    toUserId = callerId,
                    callId = callId,
                    type = "call_decline",
                    payload = ""
                )
            }
            Log.w(TAG, "Incoming call throttled for callerId=$callerId")
            return
        }
        recentIncomingCallers[callerId] = now

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

        // Start ringtone for incoming call
        startIncomingRingtone()

        Log.d(TAG, "Incoming ${callType.name} call from $callerName")
    }

    private var incomingRingtonePlayer: android.media.MediaPlayer? = null
    private var toneGenerator: android.media.ToneGenerator? = null

    private fun startIncomingRingtone() {
        try {
            val context = appContext ?: return
            val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
            incomingRingtonePlayer = android.media.MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play incoming ringtone", e)
        }
    }

    private fun startDialTone() {
        try {
            // Use ToneGenerator for a standard "ringing" sound for the caller
            if (toneGenerator == null) {
                toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_VOICE_CALL, 80)
            }
            toneGenerator?.startTone(android.media.ToneGenerator.TONE_SUP_RINGTONE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start dial tone", e)
        }
    }

    private fun stopRingtones() {
        try {
            incomingRingtonePlayer?.stop()
            incomingRingtonePlayer?.release()
            incomingRingtonePlayer = null
            
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ringtones", e)
        }
    }

    /**
     * Answer an incoming call
     */
    fun answerCall() {
        stopRingtones()
        val call = currentCall ?: return
        if (callState != CallState.INCOMING) return

        callState = CallState.CONNECTING
        callStartTime = System.currentTimeMillis()

        scope.launch {
            try {
                ensureWebRtcInitialized()
                setupPeerConnection(call.callType)

                pendingRemoteOfferSdp
                    ?.takeIf { pendingRemoteOfferCallId == call.callId }
                    ?.let { sdp ->
                        pendingRemoteOfferCallId = null
                        pendingRemoteOfferSdp = null
                        handleOffer(call.callId, sdp)
                    }

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
        stopRingtones()
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
        stopRingtones()
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
            userId = currentUserId ?: "unknown",
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
        clearPendingSignals()

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
        ensureWebRtcInitialized()
        val factory = peerConnectionFactory ?: return
        val context = appContext ?: return

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
        }

        peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "Signaling state: $state")
            }

            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE connection state: $state")
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        stopRingtones()
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
        val call = currentCall
        if (call != null && call.callId != callId) {
            Log.w(TAG, "Ignoring offer for stale callId=$callId (active=${call.callId})")
            return
        }

        val pc = peerConnection
        if (pc == null || call == null) {
            pendingRemoteOfferCallId = callId
            pendingRemoteOfferSdp = sdpDescription
            Log.d(TAG, "Buffered remote offer until peer connection is ready for callId=$callId")
            return
        }

        val sdp = SessionDescription(SessionDescription.Type.OFFER, sdpDescription)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                flushPendingIceCandidates(callId)
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

    private fun handleAnswer(callId: String, sdpDescription: String) {
        val activeCallId = currentCall?.callId
        if (activeCallId != null && activeCallId != callId) {
            Log.w(TAG, "Ignoring answer for stale callId=$callId (active=$activeCallId)")
            return
        }

        val pc = peerConnection ?: return

        val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpDescription)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set successfully")
                flushPendingIceCandidates(callId)
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

    private fun handleIceCandidate(callId: String, candidateJson: String) {
        val activeCallId = currentCall?.callId
        if (activeCallId != null && activeCallId != callId) {
            Log.w(TAG, "Ignoring ICE candidate for stale callId=$callId (active=$activeCallId)")
            return
        }

        val pc = peerConnection
        if (pc == null || pc.remoteDescription == null) {
            pendingIceCandidateSignals.add(callId to candidateJson)
            Log.d(TAG, "Buffered ICE candidate until remote description is ready for callId=$callId")
            return
        }

        try {
            @Serializable
            data class IceCandidateJson(val sdpMid: String, val sdpMLineIndex: Int, val sdp: String)
            val data = json.decodeFromString<IceCandidateJson>(candidateJson)
            val candidate = IceCandidate(data.sdpMid, data.sdpMLineIndex, data.sdp)
            pc.addIceCandidate(candidate)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ICE candidate: $candidateJson", e)
        }
    }

    private fun flushPendingIceCandidates(callId: String) {
        val pc = peerConnection ?: return
        if (pc.remoteDescription == null) return

        val iterator = pendingIceCandidateSignals.iterator()
        while (iterator.hasNext()) {
            val (queuedCallId, payload) = iterator.next()
            if (queuedCallId != callId) continue
            try {
                @Serializable
                data class IceCandidateJson(val sdpMid: String, val sdpMLineIndex: Int, val sdp: String)
                val data = json.decodeFromString<IceCandidateJson>(payload)
                val candidate = IceCandidate(data.sdpMid, data.sdpMLineIndex, data.sdp)
                pc.addIceCandidate(candidate)
                iterator.remove()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to flush queued ICE candidate", e)
                iterator.remove()
            }
        }
    }

    private fun resolveCallerName(): String {
        return try {
            val user = supabaseClient?.auth?.currentUserOrNull()
            user?.userMetadata?.get("display_name")?.jsonPrimitive?.contentOrNull
                ?: user?.userMetadata?.get("username")?.jsonPrimitive?.contentOrNull
                ?: currentUserId
                ?: "Unknown"
        } catch (_: Exception) {
            currentUserId ?: "Unknown"
        }
    }

    private fun resolveCallerAvatar(): String {
        return try {
            val user = supabaseClient?.auth?.currentUserOrNull()
            user?.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull ?: ""
        } catch (_: Exception) {
            ""
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
            val callerName = resolveCallerName()
            val callerAvatar = resolveCallerAvatar()
            client.safeInsert("call_signals", buildJsonObject {
                put("call_id", callId)
                put("from_user_id", userId)
                put("to_user_id", toUserId)
                put("type", type)
                put("payload", payload)
                put("call_type", callType.name)
                put("caller_name", callerName)
                put("caller_avatar", callerAvatar)
            })
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
        isSpeakerOn = setSpeakerphoneEnabled(audioManager, isSpeakerOn)

        Log.d(TAG, "Speaker ${if (isSpeakerOn) "on" else "off"}")
        return isSpeakerOn
    }

    private fun setSpeakerphoneEnabled(audioManager: AudioManager, enabled: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (enabled) {
                val speakerDevice = audioManager.availableCommunicationDevices
                    .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                speakerDevice?.let(audioManager::setCommunicationDevice) ?: false
            } else {
                audioManager.clearCommunicationDevice()
                false
            }
        } else {
            @Suppress("DEPRECATION")
            run {
                audioManager.isSpeakerphoneOn = enabled
            }
            enabled
        }
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

    fun loadCallHistoryIfNeeded(force: Boolean = false) {
        if (supabaseClient == null) return
        val userId = currentUserId ?: return

        if (!force && callHistoryLoadedForUserId == userId) {
            return
        }

        callHistoryLoadedForUserId = userId

        scope.launch {
            try {
                // Ensure we only load history for the current user
                val rows = com.kyilmaz.neurocomet.safeSelect(
                    table = "call_history",
                    columns = "*",
                    filters = "or=(caller_id.eq.$userId,recipient_id.eq.$userId)"
                )
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                val history = rows.map { json.decodeFromJsonElement(CallHistoryEntry.serializer(), it) }

                _callHistory.clear()
                _callHistory.addAll(history.sortedByDescending { it.timestamp })
                Log.d(TAG, "Loaded ${history.size} call history entries")
            } catch (e: Exception) {
                callHistoryLoadedForUserId = null
                Log.e(TAG, "Failed to load call history", e)
            }
        }
    }

    private fun saveCallToHistory(entry: CallHistoryEntry) {
        val client = supabaseClient ?: return

        scope.launch {
            try {
                val entryJson = kotlinx.serialization.json.Json.encodeToJsonElement(
                    CallHistoryEntry.serializer(), entry
                ) as kotlinx.serialization.json.JsonObject
                client.safeInsert("call_history", entryJson)
                Log.d(TAG, "Saved call to history: ${entry.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save call to history", e)
            }
        }
    }

    fun clearCallHistory() {
        _callHistory.clear()
        callHistoryLoadedForUserId = currentUserId

        val client = supabaseClient ?: return
        val userId = currentUserId ?: return

        scope.launch {
            try {
                client.from("call_history")
                    .delete {
                        filter {
                            eq("caller_id", userId)
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
                userId = "me",
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
                userId = "me",
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
                userId = "me",
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
                userId = "me",
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
        stopSignalingListener()
        cleanupCall()

        peerConnectionFactory?.dispose()
        peerConnectionFactory = null

        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        eglBase?.release()
        eglBase = null
        isWebRtcInitialized = false
        callHistoryLoadedForUserId = null
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

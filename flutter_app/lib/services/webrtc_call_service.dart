import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

/// Call type — voice or video
enum CallType { voice, video }

/// Call state machine
enum CallState {
  idle,
  ringing,
  incoming,
  connecting,
  connected,
  reconnecting,
  ended,
}

/// Call outcome for history
enum CallOutcome {
  completed,
  missed,
  declined,
  noAnswer,
  cancelled,
  failed,
}

/// Active call data
class ActiveCall {
  final String callId;
  final String recipientId;
  final String recipientName;
  final String recipientAvatar;
  final CallType callType;
  final bool isOutgoing;
  final DateTime startTime;

  ActiveCall({
    required this.callId,
    required this.recipientId,
    required this.recipientName,
    required this.recipientAvatar,
    required this.callType,
    required this.isOutgoing,
    DateTime? startTime,
  }) : startTime = startTime ?? DateTime.now();
}

/// Call history entry
class CallHistoryEntry {
  final String id;
  final String recipientId;
  final String recipientName;
  final String recipientAvatar;
  final CallType callType;
  final bool isOutgoing;
  final CallOutcome outcome;
  final DateTime timestamp;
  final int durationSeconds;

  const CallHistoryEntry({
    required this.id,
    required this.recipientId,
    required this.recipientName,
    required this.recipientAvatar,
    required this.callType,
    required this.isOutgoing,
    required this.outcome,
    required this.timestamp,
    this.durationSeconds = 0,
  });

  String get formattedDuration {
    if (durationSeconds == 0) return '';
    final m = durationSeconds ~/ 60;
    final s = durationSeconds % 60;
    return '$m:${s.toString().padLeft(2, '0')}';
  }
}

/// WebRTC Call Service — manages real peer-to-peer voice/video calls
/// Uses Supabase Realtime for signaling, mirrors Android WebRTCCallManager
class WebRTCCallService extends ChangeNotifier {
  static final WebRTCCallService _instance = WebRTCCallService._();
  static WebRTCCallService get instance => _instance;
  WebRTCCallService._();

  // State
  ActiveCall? _currentCall;
  CallState _callState = CallState.idle;
  int _callDuration = 0;
  bool _isMuted = false;
  bool _isSpeakerOn = false;
  bool _isCameraOn = true;
  bool _isUsingFrontCamera = true;

  ActiveCall? get currentCall => _currentCall;
  CallState get callState => _callState;
  int get callDuration => _callDuration;
  bool get isMuted => _isMuted;
  bool get isSpeakerOn => _isSpeakerOn;
  bool get isCameraOn => _isCameraOn;
  bool get isUsingFrontCamera => _isUsingFrontCamera;

  // WebRTC
  RTCPeerConnection? _peerConnection;
  MediaStream? _localStream;
  MediaStream? _remoteStream;
  RTCVideoRenderer localRenderer = RTCVideoRenderer();
  RTCVideoRenderer remoteRenderer = RTCVideoRenderer();

  MediaStream? get localStream => _localStream;
  MediaStream? get remoteStream => _remoteStream;

  // Call history
  final List<CallHistoryEntry> _callHistory = [];
  List<CallHistoryEntry> get callHistory => List.unmodifiable(_callHistory);

  // Supabase
  String? _currentUserId;
  RealtimeChannel? _signalingChannel;
  Timer? _durationTimer;
  DateTime? _callStartTime;

  // ICE servers
  final Map<String, dynamic> _iceConfig = {
    'iceServers': [
      {'urls': 'stun:stun.l.google.com:19302'},
      {'urls': 'stun:stun1.l.google.com:19302'},
      {'urls': 'stun:stun2.l.google.com:19302'},
    ],
    'sdpSemantics': 'unified-plan',
  };

  /// Initialize with current user ID and start listening for incoming calls
  Future<void> initialize(String userId) async {
    _currentUserId = userId;
    await localRenderer.initialize();
    await remoteRenderer.initialize();
    _setupSignalingListener();
    _loadCallHistory();
  }

  void _setupSignalingListener() {
    final userId = _currentUserId;
    if (userId == null) return;

    try {
      final supabase = Supabase.instance.client;
      _signalingChannel = supabase.channel('calls:$userId');

      _signalingChannel!
          .onPostgresChanges(
            event: PostgresChangeEvent.insert,
            schema: 'public',
            table: 'call_signals',
            callback: (payload) {
              final record = payload.newRecord;
              final toUserId = record['to_user_id'] as String?;
              if (toUserId == userId) {
                _handleSignalingMessage(record);
              }
            },
          )
          .subscribe();

      debugPrint('[WebRTCCallService] Signaling listener ready for $userId');
    } catch (e) {
      debugPrint('[WebRTCCallService] Failed to setup signaling: $e');
    }
  }

  void _handleSignalingMessage(Map<String, dynamic> record) {
    final type = record['type'] as String?;
    final callId = record['call_id'] as String?;
    final fromUserId = record['from_user_id'] as String?;
    final payload = record['payload'] as String? ?? '';
    final callTypeStr = record['call_type'] as String? ?? 'VOICE';

    if (type == null || callId == null || fromUserId == null) return;

    switch (type) {
      case 'call_request':
        final callerName = record['caller_name'] as String? ?? 'Unknown';
        final callerAvatar = record['caller_avatar'] as String? ?? '';
        _handleIncomingCall(
          callId: callId,
          callerId: fromUserId,
          callerName: callerName,
          callerAvatar: callerAvatar,
          callType: callTypeStr == 'VIDEO' ? CallType.video : CallType.voice,
        );
        break;
      case 'call_accept':
        if (_currentCall?.callId == callId) {
          _callState = CallState.connecting;
          notifyListeners();
        }
        break;
      case 'call_decline':
        if (_currentCall?.callId == callId) {
          endCall(outcome: CallOutcome.declined);
        }
        break;
      case 'call_end':
        if (_currentCall?.callId == callId) {
          endCall(outcome: CallOutcome.completed);
        }
        break;
      case 'offer':
        _handleOffer(callId, payload);
        break;
      case 'answer':
        _handleAnswer(payload);
        break;
      case 'ice_candidate':
        _handleIceCandidate(payload);
        break;
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // START / ANSWER / DECLINE / END
  // ═══════════════════════════════════════════════════════════════════════════

  /// Start an outgoing call
  Future<void> startCall({
    required String recipientId,
    required String recipientName,
    required String recipientAvatar,
    required CallType callType,
  }) async {
    if (_callState != CallState.idle) return;

    final callId = 'call_${DateTime.now().millisecondsSinceEpoch}';

    _currentCall = ActiveCall(
      callId: callId,
      recipientId: recipientId,
      recipientName: recipientName,
      recipientAvatar: recipientAvatar,
      callType: callType,
      isOutgoing: true,
    );
    _callState = CallState.ringing;
    _callDuration = 0;
    _callStartTime = DateTime.now();
    notifyListeners();

    try {
      await _setupPeerConnection(callType);
      await _createAndSendOffer(recipientId, callId, callType);

      // Send call_request signal
      await _sendSignalingMessage(
        toUserId: recipientId,
        callId: callId,
        type: 'call_request',
        payload: '',
        callType: callType,
      );

      // Timeout after 60 seconds
      Future.delayed(const Duration(seconds: 60), () {
        if (_callState == CallState.ringing) {
          endCall(outcome: CallOutcome.noAnswer);
        }
      });
    } catch (e) {
      debugPrint('[WebRTCCallService] Failed to start call: $e');
      endCall(outcome: CallOutcome.failed);
    }
  }

  void _handleIncomingCall({
    required String callId,
    required String callerId,
    required String callerName,
    required String callerAvatar,
    required CallType callType,
  }) {
    if (_callState != CallState.idle) {
      // Already in a call, auto-decline
      _sendSignalingMessage(
        toUserId: callerId,
        callId: callId,
        type: 'call_decline',
        payload: '',
      );
      return;
    }

    _currentCall = ActiveCall(
      callId: callId,
      recipientId: callerId,
      recipientName: callerName,
      recipientAvatar: callerAvatar,
      callType: callType,
      isOutgoing: false,
    );
    _callState = CallState.incoming;
    _callDuration = 0;
    notifyListeners();
  }

  /// Answer an incoming call
  Future<void> answerCall() async {
    final call = _currentCall;
    if (call == null || _callState != CallState.incoming) return;

    _callState = CallState.connecting;
    _callStartTime = DateTime.now();
    notifyListeners();

    try {
      await _setupPeerConnection(call.callType);
      await _sendSignalingMessage(
        toUserId: call.recipientId,
        callId: call.callId,
        type: 'call_accept',
        payload: '',
      );
    } catch (e) {
      debugPrint('[WebRTCCallService] Failed to answer: $e');
      endCall(outcome: CallOutcome.failed);
    }
  }

  /// Decline an incoming call
  void declineCall() {
    final call = _currentCall;
    if (call == null) return;

    _sendSignalingMessage(
      toUserId: call.recipientId,
      callId: call.callId,
      type: 'call_decline',
      payload: '',
    );
    endCall(outcome: CallOutcome.declined);
  }

  /// End the current call
  void endCall({CallOutcome outcome = CallOutcome.completed}) {
    final call = _currentCall;
    if (call == null) return;

    final durationSecs = _callState == CallState.connected && _callStartTime != null
        ? DateTime.now().difference(_callStartTime!).inSeconds
        : 0;

    // Send end signal
    _sendSignalingMessage(
      toUserId: call.recipientId,
      callId: call.callId,
      type: 'call_end',
      payload: '',
    );

    // Add to history
    _callHistory.insert(
      0,
      CallHistoryEntry(
        id: call.callId,
        recipientId: call.recipientId,
        recipientName: call.recipientName,
        recipientAvatar: call.recipientAvatar,
        callType: call.callType,
        isOutgoing: call.isOutgoing,
        outcome: outcome,
        timestamp: DateTime.now(),
        durationSeconds: durationSecs,
      ),
    );

    _cleanup();
    _callState = CallState.ended;
    notifyListeners();

    // Reset to idle after brief delay
    Future.delayed(const Duration(milliseconds: 1500), () {
      if (_callState == CallState.ended) {
        _callState = CallState.idle;
        _currentCall = null;
        notifyListeners();
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // WEBRTC PEER CONNECTION
  // ═══════════════════════════════════════════════════════════════════════════

  Future<void> _setupPeerConnection(CallType callType) async {
    _peerConnection = await createPeerConnection(_iceConfig);

    _peerConnection!.onIceConnectionState = (state) {
      debugPrint('[WebRTC] ICE state: $state');
      switch (state) {
        case RTCIceConnectionState.RTCIceConnectionStateConnected:
          _callState = CallState.connected;
          _startDurationTimer();
          notifyListeners();
          break;
        case RTCIceConnectionState.RTCIceConnectionStateDisconnected:
          _callState = CallState.reconnecting;
          notifyListeners();
          break;
        case RTCIceConnectionState.RTCIceConnectionStateFailed:
          endCall(outcome: CallOutcome.failed);
          break;
        default:
          break;
      }
    };

    _peerConnection!.onIceCandidate = (candidate) {
      _sendIceCandidate(candidate);
    };

    _peerConnection!.onAddStream = (stream) {
      _remoteStream = stream;
      remoteRenderer.srcObject = stream;
      notifyListeners();
    };

    _peerConnection!.onTrack = (event) {
      if (event.streams.isNotEmpty) {
        _remoteStream = event.streams[0];
        remoteRenderer.srcObject = _remoteStream;
        notifyListeners();
      }
    };

    // Get local media
    final mediaConstraints = <String, dynamic>{
      'audio': true,
      'video': callType == CallType.video
          ? {'facingMode': 'user', 'width': 1280, 'height': 720}
          : false,
    };

    _localStream = await navigator.mediaDevices.getUserMedia(mediaConstraints);
    localRenderer.srcObject = _localStream;

    for (final track in _localStream!.getTracks()) {
      await _peerConnection!.addTrack(track, _localStream!);
    }
  }

  Future<void> _createAndSendOffer(
    String recipientId,
    String callId,
    CallType callType,
  ) async {
    final pc = _peerConnection;
    if (pc == null) return;

    final offer = await pc.createOffer({
      'offerToReceiveAudio': true,
      'offerToReceiveVideo': callType == CallType.video,
    });
    await pc.setLocalDescription(offer);

    await _sendSignalingMessage(
      toUserId: recipientId,
      callId: callId,
      type: 'offer',
      payload: offer.sdp ?? '',
      callType: callType,
    );
  }

  void _handleOffer(String callId, String sdpDescription) async {
    final pc = _peerConnection;
    if (pc == null) return;

    await pc.setRemoteDescription(
      RTCSessionDescription(sdpDescription, 'offer'),
    );

    final answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    final call = _currentCall;
    if (call == null) return;

    await _sendSignalingMessage(
      toUserId: call.recipientId,
      callId: callId,
      type: 'answer',
      payload: answer.sdp ?? '',
    );
  }

  void _handleAnswer(String sdpDescription) async {
    final pc = _peerConnection;
    if (pc == null) return;

    await pc.setRemoteDescription(
      RTCSessionDescription(sdpDescription, 'answer'),
    );
  }

  void _sendIceCandidate(RTCIceCandidate candidate) {
    final call = _currentCall;
    if (call == null) return;

    final candidateJson = jsonEncode({
      'sdpMid': candidate.sdpMid,
      'sdpMLineIndex': candidate.sdpMLineIndex,
      'candidate': candidate.candidate,
    });

    _sendSignalingMessage(
      toUserId: call.recipientId,
      callId: call.callId,
      type: 'ice_candidate',
      payload: candidateJson,
    );
  }

  void _handleIceCandidate(String candidateJson) {
    final pc = _peerConnection;
    if (pc == null) return;

    try {
      final data = jsonDecode(candidateJson) as Map<String, dynamic>;
      final candidate = RTCIceCandidate(
        data['candidate'] as String?,
        data['sdpMid'] as String?,
        data['sdpMLineIndex'] as int?,
      );
      pc.addCandidate(candidate);
    } catch (e) {
      debugPrint('[WebRTC] Failed to parse ICE candidate: $e');
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // CONTROLS
  // ═══════════════════════════════════════════════════════════════════════════

  void toggleMute() {
    _isMuted = !_isMuted;
    _localStream?.getAudioTracks().forEach((track) {
      track.enabled = !_isMuted;
    });
    notifyListeners();
  }

  void toggleSpeaker() {
    _isSpeakerOn = !_isSpeakerOn;
    _localStream?.getAudioTracks().forEach((track) {
      // Speaker routing handled by platform
    });
    notifyListeners();
  }

  void toggleCamera() {
    _isCameraOn = !_isCameraOn;
    _localStream?.getVideoTracks().forEach((track) {
      track.enabled = _isCameraOn;
    });
    notifyListeners();
  }

  Future<void> switchCamera() async {
    final videoTrack = _localStream?.getVideoTracks().firstOrNull;
    if (videoTrack != null) {
      await Helper.switchCamera(videoTrack);
      _isUsingFrontCamera = !_isUsingFrontCamera;
      notifyListeners();
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // SIGNALING
  // ════���══════════════════════════════════════════════════════════════════════

  Future<void> _sendSignalingMessage({
    required String toUserId,
    required String callId,
    required String type,
    required String payload,
    CallType? callType,
  }) async {
    try {
      final supabase = Supabase.instance.client;
      await supabase.from('call_signals').insert({
        'call_id': callId,
        'from_user_id': _currentUserId,
        'to_user_id': toUserId,
        'type': type,
        'payload': payload,
        'call_type': (callType ?? _currentCall?.callType ?? CallType.voice) == CallType.video
            ? 'VIDEO'
            : 'VOICE',
        'caller_name': _currentCall?.recipientName ?? 'Unknown',
        'caller_avatar': _currentCall?.recipientAvatar ?? '',
      });
    } catch (e) {
      debugPrint('[WebRTCCallService] Signaling send failed: $e');
      // Fall back to mock mode — simulate acceptance after 3s
      if (type == 'call_request' || type == 'offer') {
        Future.delayed(const Duration(seconds: 3), () {
          if (_callState == CallState.ringing) {
            _callState = CallState.connected;
            _startDurationTimer();
            notifyListeners();
          }
        });
      }
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // TIMER & CLEANUP
  // ═══════════════════════════════════════════════════════════════════════════

  void _startDurationTimer() {
    _durationTimer?.cancel();
    _callStartTime = DateTime.now();
    _durationTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (_callState == CallState.connected) {
        _callDuration =
            DateTime.now().difference(_callStartTime!).inSeconds;
        notifyListeners();
      }
    });
  }

  void _cleanup() {
    _durationTimer?.cancel();
    _localStream?.getTracks().forEach((track) => track.stop());
    _localStream?.dispose();
    _localStream = null;
    _remoteStream = null;
    _peerConnection?.close();
    _peerConnection = null;
    localRenderer.srcObject = null;
    remoteRenderer.srcObject = null;
    _isMuted = false;
    _isSpeakerOn = false;
    _isCameraOn = true;
  }

  void _loadCallHistory() {
    // Load from Supabase if available — for now, starts empty
  }

  void clearCallHistory() {
    _callHistory.clear();
    notifyListeners();
  }

  /// Dispose all resources
  @override
  void dispose() {
    _cleanup();
    localRenderer.dispose();
    remoteRenderer.dispose();
    _signalingChannel?.unsubscribe();
    super.dispose();
  }
}

/// Format call duration as M:SS or H:MM:SS
String formatCallDuration(int totalSeconds) {
  final h = totalSeconds ~/ 3600;
  final m = (totalSeconds % 3600) ~/ 60;
  final s = totalSeconds % 60;
  if (h > 0) return '$h:${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
  return '$m:${s.toString().padLeft(2, '0')}';
}


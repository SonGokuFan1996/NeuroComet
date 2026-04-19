import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_webrtc/flutter_webrtc.dart';
import '../../services/webrtc_call_service.dart';
import '../../core/theme/app_colors.dart';

/// Full-screen active call screen — Instagram-style voice/video calling UI.
/// Mirrors the Android ActiveCallScreen.kt.
class ActiveCallScreen extends StatefulWidget {
  final bool isPracticeCall;
  const ActiveCallScreen({super.key, this.isPracticeCall = false});

  @override
  State<ActiveCallScreen> createState() => _ActiveCallScreenState();
}

class _ActiveCallScreenState extends State<ActiveCallScreen>
    with SingleTickerProviderStateMixin {
  final _callService = WebRTCCallService.instance;
  late AnimationController _pulseController;
  bool _showControls = true;
  Timer? _controlsTimer;

  @override
  void initState() {
    super.initState();
    _callService.addListener(_onCallStateChanged);
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1200),
      vsync: this,
    )..repeat(reverse: true);
    _resetControlsTimer();
  }

  @override
  void dispose() {
    _callService.removeListener(_onCallStateChanged);
    _pulseController.dispose();
    _controlsTimer?.cancel();
    super.dispose();
  }

  void _onCallStateChanged() {
    setState(() {});
    if (_callService.callState == CallState.ended) {
      Future.delayed(const Duration(milliseconds: 1200), () {
        if (mounted) Navigator.of(context).pop();
      });
    }
  }

  void _resetControlsTimer() {
    _controlsTimer?.cancel();
    final call = _callService.currentCall;
    if (call?.callType == CallType.video &&
        _callService.callState == CallState.connected) {
      _controlsTimer = Timer(const Duration(seconds: 5), () {
        if (mounted) setState(() => _showControls = false);
      });
    }
  }

  Future<void> _startPreviewCall(CallType callType) async {
    await _callService.prepareDebugPreviewCall(
      callType: callType,
      initialState: CallState.connected,
    );
    if (mounted) {
      setState(() {
        _showControls = true;
      });
      _resetControlsTimer();
    }
  }

  @override
  Widget build(BuildContext context) {
    final call = _callService.currentCall;
    if (call == null) {
      return Scaffold(
        backgroundColor: Colors.black,
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.call_outlined, color: Colors.white70, size: 72),
                  const SizedBox(height: 20),
                  const Text(
                    'No active call yet',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    'Launch a quick preview call for the developer lab, or start a real call from Messages.',
                    style: TextStyle(color: Colors.white70, fontSize: 15),
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 24),
                  Wrap(
                    alignment: WrapAlignment.center,
                    spacing: 12,
                    runSpacing: 12,
                    children: [
                      ElevatedButton.icon(
                        onPressed: () => _startPreviewCall(CallType.video),
                        icon: const Icon(Icons.videocam_outlined),
                        label: const Text('Preview Video Call'),
                      ),
                      OutlinedButton.icon(
                        onPressed: () => _startPreviewCall(CallType.voice),
                        icon: const Icon(Icons.call_outlined),
                        label: const Text('Preview Voice Call'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.white,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      );
    }

    final state = _callService.callState;
    final isVideo = call.callType == CallType.video;

    return Scaffold(
      backgroundColor: Colors.black,
      body: GestureDetector(
        onTap: isVideo
            ? () {
                setState(() => _showControls = !_showControls);
                if (_showControls) _resetControlsTimer();
              }
            : null,
        child: Stack(
          children: [
            // Background
            if (isVideo)
              _VideoBackground(
                remoteRenderer: _callService.remoteRenderer,
                state: state,
              )
            else
              _VoiceBackground(),

            // Content overlay
            SafeArea(
              child: Column(
                children: [
                  const SizedBox(height: 32),

                  // Status text
                  _StatusText(state: state, isVideo: isVideo, duration: _callService.callDuration),

                  const SizedBox(height: 24),

                  // Avatar & name (hide during connected video unless controls visible)
                  if (!isVideo || state != CallState.connected || _showControls)
                    _CallerInfo(
                      call: call,
                      state: state,
                      pulseController: _pulseController,
                    ),

                  const Spacer(),

                  // Local video PiP
                  if (isVideo && state == CallState.connected)
                    Align(
                      alignment: Alignment.centerRight,
                      child: Padding(
                        padding: const EdgeInsets.only(right: 16),
                        child: _LocalVideoPip(
                          localRenderer: _callService.localRenderer,
                          isCameraOn: _callService.isCameraOn,
                        ),
                      ),
                    ),

                  const SizedBox(height: 16),

                  // Incoming call buttons
                  if (state == CallState.incoming)
                    _IncomingCallButtons(
                      onAccept: () {
                        HapticFeedback.heavyImpact();
                        _callService.answerCall();
                      },
                      onDecline: () {
                        HapticFeedback.heavyImpact();
                        _callService.declineCall();
                      },
                    ),

                  // Connected controls
                  if (state == CallState.connected ||
                      state == CallState.reconnecting ||
                      state == CallState.ringing)
                    AnimatedOpacity(
                      opacity: _showControls ? 1.0 : 0.0,
                      duration: const Duration(milliseconds: 200),
                      child: _CallControlBar(
                        callService: _callService,
                        isVideo: isVideo,
                        onEndCall: () {
                          HapticFeedback.heavyImpact();
                          _callService.endCall();
                        },
                      ),
                    ),

                  const SizedBox(height: 32),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// STATUS TEXT
// ═══════════════════════════════════════════════════════════════════════════════

class _StatusText extends StatelessWidget {
  final CallState state;
  final bool isVideo;
  final int duration;

  const _StatusText({
    required this.state,
    required this.isVideo,
    required this.duration,
  });

  @override
  Widget build(BuildContext context) {
    final text = switch (state) {
      CallState.ringing => 'Ringing...',
      CallState.incoming => 'Incoming ${isVideo ? "Video" : "Voice"} Call',
      CallState.connecting => 'Connecting...',
      CallState.connected => formatCallDuration(duration),
      CallState.reconnecting => 'Reconnecting...',
      CallState.ended => 'Call Ended',
      CallState.idle => '',
    };

    return Text(
      text,
      style: const TextStyle(
        color: Colors.white70,
        fontSize: 16,
        fontWeight: FontWeight.w500,
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CALLER INFO — avatar + name
// ═══════════════════════════════════════════════════════════════════════════════

class _CallerInfo extends StatelessWidget {
  final ActiveCall call;
  final CallState state;
  final AnimationController pulseController;

  const _CallerInfo({
    required this.call,
    required this.state,
    required this.pulseController,
  });

  @override
  Widget build(BuildContext context) {
    final isPulsing = state == CallState.ringing || state == CallState.incoming;

    return Column(
      children: [
        AnimatedBuilder(
          animation: pulseController,
          builder: (context, child) {
            final scale = isPulsing ? 1.0 + (pulseController.value * 0.12) : 1.0;
            return Transform.scale(scale: scale, child: child);
          },
          child: Container(
            width: 120,
            height: 120,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  AppColors.primaryPurple.withValues(alpha: 0.3),
                  Colors.transparent,
                ],
              ),
            ),
            child: Center(
              child: CircleAvatar(
                radius: 50,
                backgroundImage: call.recipientAvatar.isNotEmpty
                    ? NetworkImage(call.recipientAvatar)
                    : null,
                child: call.recipientAvatar.isEmpty
                    ? const Icon(Icons.person, size: 40, color: Colors.white70)
                    : null,
              ),
            ),
          ),
        ),
        const SizedBox(height: 16),
        Text(
          call.recipientName,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 28,
            fontWeight: FontWeight.bold,
          ),
        ),
        if (call.callType == CallType.video)
          const Padding(
            padding: EdgeInsets.only(top: 4),
            child: Text(
              'Video Call',
              style: TextStyle(color: Colors.white54, fontSize: 14),
            ),
          ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIDEO / VOICE BACKGROUNDS
// ═══════════════════════════════════════════════════════════════════════════════

class _VideoBackground extends StatelessWidget {
  final RTCVideoRenderer remoteRenderer;
  final CallState state;

  const _VideoBackground({required this.remoteRenderer, required this.state});

  @override
  Widget build(BuildContext context) {
    if (state == CallState.connected && remoteRenderer.srcObject != null) {
      return SizedBox.expand(
        child: RTCVideoView(
          remoteRenderer,
          objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
        ),
      );
    }
    return _VoiceBackground();
  }
}

class _VoiceBackground extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)],
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LOCAL VIDEO PIP
// ═══════════════════════════════════════════════════════════════════════════════

class _LocalVideoPip extends StatelessWidget {
  final RTCVideoRenderer localRenderer;
  final bool isCameraOn;

  const _LocalVideoPip({required this.localRenderer, required this.isCameraOn});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 120,
      height: 160,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        color: const Color(0xFF2A2A3E),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.4),
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      clipBehavior: Clip.hardEdge,
      child: isCameraOn && localRenderer.srcObject != null
          ? RTCVideoView(
              localRenderer,
              mirror: true,
              objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
            )
          : const Center(
              child: Icon(Icons.videocam_off, color: Colors.white38, size: 32),
            ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// INCOMING CALL BUTTONS — Accept / Decline
// ═══════════════════════════════════════════════════════════════════════════════

class _IncomingCallButtons extends StatelessWidget {
  final VoidCallback onAccept;
  final VoidCallback onDecline;

  const _IncomingCallButtons({
    required this.onAccept,
    required this.onDecline,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 48),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Decline
          Column(
            children: [
              _CircleButton(
                color: const Color(0xFFFF3B30),
                icon: Icons.call_end,
                onTap: onDecline,
              ),
              const SizedBox(height: 8),
              const Text('Decline', style: TextStyle(color: Colors.white, fontSize: 14)),
            ],
          ),
          // Accept
          Column(
            children: [
              _CircleButton(
                color: const Color(0xFF34C759),
                icon: Icons.call,
                onTap: onAccept,
              ),
              const SizedBox(height: 8),
              const Text('Accept', style: TextStyle(color: Colors.white, fontSize: 14)),
            ],
          ),
        ],
      ),
    );
  }
}

class _CircleButton extends StatelessWidget {
  final Color color;
  final IconData icon;
  final VoidCallback onTap;

  const _CircleButton({
    required this.color,
    required this.icon,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 72,
        height: 72,
        decoration: BoxDecoration(shape: BoxShape.circle, color: color),
        child: Icon(icon, color: Colors.white, size: 32),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CALL CONTROL BAR — mute, speaker, camera, flip, end
// ═══════════════════════════════════════════════════════════════════════════════

class _CallControlBar extends StatelessWidget {
  final WebRTCCallService callService;
  final bool isVideo;
  final VoidCallback onEndCall;

  const _CallControlBar({
    required this.callService,
    required this.isVideo,
    required this.onEndCall,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.black45,
        borderRadius: BorderRadius.circular(28),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _ControlButton(
            icon: callService.isMuted ? Icons.mic_off : Icons.mic,
            label: callService.isMuted ? 'Unmute' : 'Mute',
            isActive: callService.isMuted,
            onTap: () {
              HapticFeedback.selectionClick();
              callService.toggleMute();
            },
          ),
          _ControlButton(
            icon: callService.isSpeakerOn ? Icons.volume_up : Icons.volume_off,
            label: callService.isSpeakerOn ? 'Speaker' : 'Earpiece',
            isActive: callService.isSpeakerOn,
            onTap: () {
              HapticFeedback.selectionClick();
              callService.toggleSpeaker();
            },
          ),
          if (isVideo) ...[
            _ControlButton(
              icon: callService.isCameraOn ? Icons.videocam : Icons.videocam_off,
              label: callService.isCameraOn ? 'Cam On' : 'Cam Off',
              isActive: !callService.isCameraOn,
              onTap: () {
                HapticFeedback.selectionClick();
                callService.toggleCamera();
              },
            ),
            _ControlButton(
              icon: Icons.flip_camera_android,
              label: 'Flip',
              isActive: false,
              onTap: () {
                HapticFeedback.selectionClick();
                callService.switchCamera();
              },
            ),
          ],
          // End call
          GestureDetector(
            onTap: onEndCall,
            child: Container(
              width: 56,
              height: 56,
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                color: Color(0xFFFF3B30),
              ),
              child: const Icon(Icons.call_end, color: Colors.white, size: 28),
            ),
          ),
        ],
      ),
    );
  }
}

class _ControlButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _ControlButton({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: isActive ? Colors.white24 : Colors.white10,
            ),
            child: Icon(
              icon,
              color: isActive ? Colors.white : Colors.white70,
              size: 24,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(
              color: Colors.white54,
              fontSize: 10,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}


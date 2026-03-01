import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';
import '../../core/theme/app_colors.dart';

/// Custom video player widget with neurodivergent-friendly controls
class NeuroVideoPlayer extends StatefulWidget {
  final String videoUrl;
  final bool autoPlay;
  final bool showControls;
  final bool looping;
  final double? aspectRatio;
  final Widget? placeholder;
  final VoidCallback? onVideoEnd;

  const NeuroVideoPlayer({
    super.key,
    required this.videoUrl,
    this.autoPlay = false,
    this.showControls = true,
    this.looping = false,
    this.aspectRatio,
    this.placeholder,
    this.onVideoEnd,
  });

  @override
  State<NeuroVideoPlayer> createState() => _NeuroVideoPlayerState();
}

class _NeuroVideoPlayerState extends State<NeuroVideoPlayer> {
  late VideoPlayerController _controller;
  bool _isInitialized = false;
  bool _showOverlay = true;
  bool _isFullscreen = false;

  @override
  void initState() {
    super.initState();
    _initializePlayer();
  }

  Future<void> _initializePlayer() async {
    _controller = VideoPlayerController.networkUrl(Uri.parse(widget.videoUrl))
      ..initialize().then((_) {
        setState(() => _isInitialized = true);
        if (widget.autoPlay) {
          _controller.play();
        }
      });

    _controller.setLooping(widget.looping);
    _controller.addListener(_videoListener);
  }

  void _videoListener() {
    if (!mounted) return;
    if (_controller.value.position >= _controller.value.duration &&
        !widget.looping) {
      widget.onVideoEnd?.call();
    }
    setState(() {});
  }

  @override
  void dispose() {
    _controller.removeListener(_videoListener);
    _controller.dispose();
    super.dispose();
  }

  void _togglePlayPause() {
    HapticFeedback.lightImpact();
    if (_controller.value.isPlaying) {
      _controller.pause();
    } else {
      _controller.play();
    }
    setState(() {});
  }

  void _seekTo(Duration position) {
    _controller.seekTo(position);
  }

  void _toggleFullscreen() {
    setState(() {
      _isFullscreen = !_isFullscreen;
    });
    if (_isFullscreen) {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersive);
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]);
    } else {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.portraitUp,
      ]);
    }
  }

  void _toggleMute() {
    HapticFeedback.lightImpact();
    _controller.setVolume(_controller.value.volume > 0 ? 0 : 1);
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    if (!_isInitialized) {
      return widget.placeholder ??
          AspectRatio(
            aspectRatio: widget.aspectRatio ?? 16 / 9,
            child: Container(
              color: Colors.black,
              child: const Center(
                child: CircularProgressIndicator(color: Colors.white),
              ),
            ),
          );
    }

    return GestureDetector(
      onTap: () {
        setState(() => _showOverlay = !_showOverlay);
      },
      child: AspectRatio(
        aspectRatio: widget.aspectRatio ?? _controller.value.aspectRatio,
        child: Stack(
          alignment: Alignment.center,
          children: [
            // Video
            VideoPlayer(_controller),

            // Overlay controls
            if (widget.showControls)
              AnimatedOpacity(
                duration: const Duration(milliseconds: 200),
                opacity: _showOverlay ? 1 : 0,
                child: _buildControls(),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildControls() {
    final position = _controller.value.position;
    final duration = _controller.value.duration;
    final isPlaying = _controller.value.isPlaying;
    final isMuted = _controller.value.volume == 0;

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Colors.black.withAlpha(100),
            Colors.transparent,
            Colors.transparent,
            Colors.black.withAlpha(100),
          ],
          stops: const [0, 0.3, 0.7, 1],
        ),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Top bar
          Padding(
            padding: const EdgeInsets.all(8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                IconButton(
                  icon: Icon(
                    isMuted ? Icons.volume_off : Icons.volume_up,
                    color: Colors.white,
                  ),
                  onPressed: _toggleMute,
                ),
              ],
            ),
          ),

          // Center play button
          IconButton(
            iconSize: 64,
            icon: Icon(
              isPlaying ? Icons.pause_circle : Icons.play_circle,
              color: Colors.white,
            ),
            onPressed: _togglePlayPause,
          ),

          // Bottom bar
          Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              children: [
                // Progress bar
                SliderTheme(
                  data: SliderThemeData(
                    thumbShape: const RoundSliderThumbShape(
                      enabledThumbRadius: 6,
                    ),
                    overlayShape: const RoundSliderOverlayShape(
                      overlayRadius: 12,
                    ),
                    trackHeight: 4,
                    activeTrackColor: AppColors.primaryPurple,
                    inactiveTrackColor: Colors.white.withAlpha(100),
                    thumbColor: Colors.white,
                  ),
                  child: Slider(
                    value: position.inMilliseconds.toDouble(),
                    max: duration.inMilliseconds.toDouble(),
                    onChanged: (value) {
                      _seekTo(Duration(milliseconds: value.toInt()));
                    },
                  ),
                ),

                // Time and fullscreen
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        '${_formatDuration(position)} / ${_formatDuration(duration)}',
                        style: const TextStyle(color: Colors.white, fontSize: 12),
                      ),
                      IconButton(
                        icon: Icon(
                          _isFullscreen
                              ? Icons.fullscreen_exit
                              : Icons.fullscreen,
                          color: Colors.white,
                        ),
                        onPressed: _toggleFullscreen,
                        padding: EdgeInsets.zero,
                        constraints: const BoxConstraints(),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    final minutes = duration.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = duration.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }
}

/// Simple video thumbnail widget
class VideoThumbnail extends StatelessWidget {
  final String videoUrl;
  final String? thumbnailUrl;
  final Duration? duration;
  final VoidCallback? onTap;

  const VideoThumbnail({
    super.key,
    required this.videoUrl,
    this.thumbnailUrl,
    this.duration,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Thumbnail image
          if (thumbnailUrl != null)
            Image.network(
              thumbnailUrl!,
              fit: BoxFit.cover,
              width: double.infinity,
              height: double.infinity,
              errorBuilder: (_, __, ___) => Container(
                color: Colors.black,
                child: const Icon(Icons.video_library, color: Colors.white54),
              ),
            )
          else
            Container(
              color: Colors.black,
              child: const Icon(Icons.video_library, color: Colors.white54, size: 48),
            ),

          // Play icon
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.black.withAlpha(150),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.play_arrow,
              color: Colors.white,
              size: 32,
            ),
          ),

          // Duration badge
          if (duration != null)
            Positioned(
              bottom: 8,
              right: 8,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.black.withAlpha(180),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  _formatDuration(duration!),
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  String _formatDuration(Duration duration) {
    final minutes = duration.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = duration.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }
}


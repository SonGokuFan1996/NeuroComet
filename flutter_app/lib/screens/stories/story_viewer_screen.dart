import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../models/story.dart';
import '../../providers/stories_provider.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../core/theme/app_colors.dart';

/// Premium Story Viewer Screen - Full-screen immersive story viewing experience
class StoryViewerScreen extends ConsumerStatefulWidget {
  final List<StoryGroup> storyGroups;
  final int initialGroupIndex;

  const StoryViewerScreen({
    super.key,
    required this.storyGroups,
    this.initialGroupIndex = 0,
  });

  @override
  ConsumerState<StoryViewerScreen> createState() => _StoryViewerScreenState();
}

class _StoryViewerScreenState extends ConsumerState<StoryViewerScreen>
    with TickerProviderStateMixin {
  late PageController _pageController;
  late AnimationController _progressController;

  int _currentGroupIndex = 0;
  int _currentStoryIndex = 0;
  bool _isPaused = false;
  bool _showReactions = false;
  double _dragOffset = 0;

  // Reaction emojis
  final List<String> _reactions = ['❤️', '😂', '😮', '😢', '🔥', '👏', '💜', '✨'];

  @override
  void initState() {
    super.initState();
    _currentGroupIndex = widget.initialGroupIndex;
    _pageController = PageController(initialPage: widget.initialGroupIndex);
    _progressController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 5),
    )..addStatusListener(_onProgressComplete);

    // Start progress after build
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _progressController.forward();
      _markCurrentStoryViewed();
    });
  }

  @override
  void dispose() {
    _pageController.dispose();
    _progressController.dispose();
    super.dispose();
  }

  StoryGroup get _currentGroup {
    if (widget.storyGroups.isEmpty) {
      return const StoryGroup(userId: '', userName: '', stories: [], hasUnseenStories: false);
    }
    final clampedIndex = _currentGroupIndex.clamp(0, widget.storyGroups.length - 1);
    return widget.storyGroups[clampedIndex];
  }

  Story? get _currentStory {
    if (_currentGroup.stories.isEmpty) return null;
    if (_currentStoryIndex >= _currentGroup.stories.length) return null;
    return _currentGroup.stories[_currentStoryIndex];
  }

  void _onProgressComplete(AnimationStatus status) {
    if (status == AnimationStatus.completed) {
      _goToNextStory();
    }
  }

  void _markCurrentStoryViewed() {
    final story = _currentStory;
    if (story != null && !story.isViewed) {
      ref.read(storiesProvider.notifier).markStoryViewed(story.id);
    }
  }

  void _goToNextStory() {
    if (_currentStoryIndex < _currentGroup.stories.length - 1) {
      setState(() {
        _currentStoryIndex++;
      });
      _progressController.forward(from: 0);
      _markCurrentStoryViewed();
    } else {
      _goToNextGroup();
    }
  }

  void _goToPreviousStory() {
    // If we're partway through, restart current story
    if (_progressController.value > 0.3) {
      _progressController.forward(from: 0);
      return;
    }

    if (_currentStoryIndex > 0) {
      setState(() {
        _currentStoryIndex--;
      });
      _progressController.forward(from: 0);
    } else {
      _goToPreviousGroup();
    }
  }

  void _goToNextGroup() {
    if (_currentGroupIndex < widget.storyGroups.length - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      Navigator.pop(context);
    }
  }

  void _goToPreviousGroup() {
    if (_currentGroupIndex > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  void _onPageChanged(int index) {
    setState(() {
      _currentGroupIndex = index;
      _currentStoryIndex = 0;
    });
    _progressController.forward(from: 0);
    _markCurrentStoryViewed();
  }

  void _onTapDown(TapDownDetails details, Size size) {
    setState(() => _isPaused = true);
    _progressController.stop();
  }

  void _onTapUp(TapUpDetails details, Size size) {
    setState(() => _isPaused = false);
    final x = details.localPosition.dx;
    if (x < size.width / 3) {
      HapticFeedback.lightImpact();
      _goToPreviousStory();
    } else if (x > size.width * 2 / 3) {
      HapticFeedback.lightImpact();
      _goToNextStory();
    } else {
      _progressController.forward();
    }
  }

  void _onLongPressStart(LongPressStartDetails details) {
    HapticFeedback.mediumImpact();
    setState(() => _isPaused = true);
    _progressController.stop();
  }

  void _onLongPressEnd(LongPressEndDetails details) {
    setState(() => _isPaused = false);
    _progressController.forward();
  }

  void _toggleReactions() {
    HapticFeedback.lightImpact();
    setState(() {
      _showReactions = !_showReactions;
      if (_showReactions) {
        _progressController.stop();
      } else {
        _progressController.forward();
      }
    });
  }

  void _sendReaction(String emoji) {
    HapticFeedback.mediumImpact();
    final story = _currentStory;
    if (story != null) {
      ref.read(storiesProvider.notifier).addReaction(story.id, emoji);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              Text(emoji, style: const TextStyle(fontSize: 24)),
              const SizedBox(width: 12),
              const Text('Reaction sent!'),
            ],
          ),
          behavior: SnackBarBehavior.floating,
          backgroundColor: AppColors.success,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          margin: const EdgeInsets.all(16),
          duration: const Duration(seconds: 1),
        ),
      );
    }
    setState(() => _showReactions = false);
    _progressController.forward();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: GestureDetector(
        onVerticalDragUpdate: (details) {
          if (details.delta.dy > 0) {
            setState(() => _dragOffset += details.delta.dy);
          }
        },
        onVerticalDragEnd: (details) {
          if (_dragOffset > 100) {
            Navigator.pop(context);
          } else {
            setState(() => _dragOffset = 0);
          }
        },
        child: Transform.translate(
          offset: Offset(0, _dragOffset.clamp(0, 200)),
          child: Opacity(
            opacity: (1 - (_dragOffset / 400)).clamp(0.3, 1.0),
            child: PageView.builder(
        controller: _pageController,
        onPageChanged: _onPageChanged,
        itemCount: widget.storyGroups.length,
        itemBuilder: (context, groupIndex) {
          final group = widget.storyGroups[groupIndex];
          final isCurrentGroup = groupIndex == _currentGroupIndex;
          final storyIndex = isCurrentGroup ? _currentStoryIndex : 0;

          if (group.stories.isEmpty) {
            return _buildEmptyStoryPage(group);
          }

          final story = group.stories[storyIndex.clamp(0, group.stories.length - 1)];

          return LayoutBuilder(
            builder: (context, constraints) {
              return GestureDetector(
                onTapDown: (details) => _onTapDown(details, constraints.biggest),
                onTapUp: (details) => _onTapUp(details, constraints.biggest),
                onLongPressStart: _onLongPressStart,
                onLongPressEnd: _onLongPressEnd,
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    // Story content
                    _buildStoryContent(story),

                    // Overlay gradient
                    _buildOverlayGradient(),

                    // Top bar with progress and user info
                    SafeArea(
                      child: Column(
                        children: [
                          // Progress indicators
                          _buildProgressIndicators(group, isCurrentGroup),
                          // User info
                          _buildUserInfo(group, story),
                        ],
                      ),
                    ),

                    // Caption at bottom
                    if (story.caption != null && story.caption!.isNotEmpty)
                      _buildCaption(story.caption!),

                    // Reply/reaction bar at bottom
                    _buildBottomBar(),

                    // Reactions overlay
                    if (_showReactions && ref.watch(devOptionsProvider).enableStoryReactions)
                      _buildReactionsOverlay(),

                    // Pause indicator
                    if (_isPaused)
                      Center(
                        child: Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: Colors.black.withValues(alpha: 0.5),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: const Icon(
                            Icons.pause,
                            color: Colors.white,
                            size: 32,
                          ),
                        ),
                      ),
                  ],
                ),
              );
            },
          );
        },
      ),
    ),
        ),
      ),
    );
  }

  Widget _buildEmptyStoryPage(StoryGroup group) {
    return Container(
      color: AppColors.primaryPurple.withValues(alpha: 0.3),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            NeuroAvatar(
              imageUrl: group.userAvatarUrl,
              name: group.userName,
              size: 80,
            ),
            const SizedBox(height: 16),
            Text(
              group.userName,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'No stories yet',
              style: TextStyle(color: Colors.white70),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStoryContent(Story story) {
    switch (story.contentType) {
      case StoryContentType.text:
        return _buildTextStory(story);
      case StoryContentType.video:
        return _buildVideoStory(story);
      case StoryContentType.document:
        return _buildDocumentStory(story);
      case StoryContentType.link:
        return _buildLinkStory(story);
      case StoryContentType.audio:
        return _buildAudioStory(story);
      case StoryContentType.photo:
        return _buildPhotoStory(story);
    }
  }

  Widget _buildTextStory(Story story) {
    List<Color> gradientColors = [AppColors.primaryPurple, AppColors.secondaryTeal];

    // Parse gradient colors if available
    if (story.backgroundGradient != null) {
      try {
        final colors = story.backgroundGradient!.split(',');
        if (colors.length >= 2) {
          gradientColors = colors.map((c) {
            final hex = c.trim().replaceAll('#', '');
            return Color(int.parse('FF$hex', radix: 16));
          }).toList();
        }
      } catch (_) {
        // Use defaults
      }
    } else if (story.backgroundColor != null) {
      try {
        final hex = story.backgroundColor!.replaceAll('#', '');
        final color = Color(int.parse('FF$hex', radix: 16));
        gradientColors = [color, color.withValues(alpha: 0.7)];
      } catch (_) {
        // Use defaults
      }
    }

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: gradientColors,
        ),
      ),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Text(
            story.caption ?? '',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 28,
              fontWeight: FontWeight.bold,
              height: 1.4,
            ),
            textAlign: TextAlign.center,
          ),
        ),
      ),
    );
  }

  Widget _buildPhotoStory(Story story) {
    if (story.mediaUrl == null) {
      return Container(
        color: Colors.black,
        child: const Center(
          child: Icon(Icons.image_not_supported, color: Colors.white54, size: 64),
        ),
      );
    }

    Widget imageWidget = CachedNetworkImage(
      imageUrl: story.mediaUrl!,
      fit: BoxFit.cover,
      placeholder: (_, __) => Container(
        color: Colors.black,
        child: const Center(child: CircularProgressIndicator(color: Colors.white)),
      ),
      errorWidget: (_, __, ___) => Container(
        color: AppColors.primaryPurple,
        child: const Center(
          child: Icon(Icons.image_not_supported, color: Colors.white54, size: 64),
        ),
      ),
    );

    // Apply filter if set
    if (story.filter != null && story.filter != 'none') {
      imageWidget = ColorFiltered(
        colorFilter: _getColorFilter(story.filter!),
        child: imageWidget,
      );
    }

    return imageWidget;
  }

  Widget _buildVideoStory(Story story) {
    return Container(
      color: Colors.black,
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.play_arrow_rounded,
                color: Colors.white,
                size: 64,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Video Story',
              style: TextStyle(
                color: Colors.white.withValues(alpha: 0.7),
                fontSize: 16,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDocumentStory(Story story) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.blueGrey.shade800, Colors.blueGrey.shade900],
        ),
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(28),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Icon(Icons.description_rounded, color: Colors.white, size: 64),
            ),
            const SizedBox(height: 20),
            Text(
              story.fileName ?? 'Shared Document',
              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            if (story.fileSize != null) ...[
              const SizedBox(height: 8),
              Text(
                '${(story.fileSize! / 1024).toStringAsFixed(1)} KB',
                style: TextStyle(color: Colors.white.withValues(alpha: 0.6), fontSize: 14),
              ),
            ],
            if (story.caption != null && story.caption!.isNotEmpty) ...[
              const SizedBox(height: 16),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 40),
                child: Text(story.caption!, style: const TextStyle(color: Colors.white70, fontSize: 16), textAlign: TextAlign.center),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildLinkStory(Story story) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Colors.indigo.shade800, Colors.indigo.shade900],
        ),
      ),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.link_rounded, color: Colors.white, size: 48),
              ),
              const SizedBox(height: 24),
              if (story.linkPreview != null) ...[
                if (story.linkPreview!.title != null)
                  Text(
                    story.linkPreview!.title!,
                    style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold),
                    textAlign: TextAlign.center,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                if (story.linkPreview!.description != null) ...[
                  const SizedBox(height: 12),
                  Text(
                    story.linkPreview!.description!,
                    style: TextStyle(color: Colors.white.withValues(alpha: 0.7), fontSize: 14),
                    textAlign: TextAlign.center,
                    maxLines: 4,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
                if (story.linkPreview!.siteName != null) ...[
                  const SizedBox(height: 12),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(story.linkPreview!.siteName!, style: const TextStyle(color: Colors.white70, fontSize: 12)),
                  ),
                ],
              ] else
                Text(
                  story.mediaUrl ?? 'Shared Link',
                  style: const TextStyle(color: Colors.white, fontSize: 16),
                  textAlign: TextAlign.center,
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAudioStory(Story story) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Colors.deepPurple.shade800, Colors.purple.shade900],
        ),
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.1),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.headphones_rounded, color: Colors.white, size: 56),
            ),
            const SizedBox(height: 20),
            Text(
              story.fileName ?? 'Audio Story',
              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            if (story.durationSeconds != null) ...[
              const SizedBox(height: 8),
              Text(
                '${story.durationSeconds! ~/ 60}:${(story.durationSeconds! % 60).toString().padLeft(2, '0')}',
                style: TextStyle(color: Colors.white.withValues(alpha: 0.6), fontSize: 14),
              ),
            ],
            const SizedBox(height: 24),
            // Simulated waveform
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(20, (i) {
                final height = 10.0 + (i * 7 % 30);
                return Container(
                  margin: const EdgeInsets.symmetric(horizontal: 2),
                  width: 4,
                  height: height,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.5),
                    borderRadius: BorderRadius.circular(2),
                  ),
                );
              }),
            ),
            if (story.caption != null && story.caption!.isNotEmpty) ...[
              const SizedBox(height: 20),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 40),
                child: Text(story.caption!, style: const TextStyle(color: Colors.white70, fontSize: 16), textAlign: TextAlign.center),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildOverlayGradient() {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Colors.black.withValues(alpha: 0.6),
            Colors.transparent,
            Colors.transparent,
            Colors.black.withValues(alpha: 0.6),
          ],
          stops: const [0, 0.2, 0.8, 1],
        ),
      ),
    );
  }

  Widget _buildProgressIndicators(StoryGroup group, bool isCurrentGroup) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      child: Row(
        children: List.generate(
          group.stories.length,
          (index) => Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 2),
              child: _buildProgressIndicator(index, isCurrentGroup),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildProgressIndicator(int index, bool isCurrentGroup) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(2),
      child: SizedBox(
        height: 3,
        child: AnimatedBuilder(
          animation: _progressController,
          builder: (context, child) {
            double progress;
            if (!isCurrentGroup) {
              progress = 0;
            } else if (index < _currentStoryIndex) {
              progress = 1;
            } else if (index > _currentStoryIndex) {
              progress = 0;
            } else {
              progress = _progressController.value;
            }

            return LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.white.withValues(alpha: 0.3),
              valueColor: const AlwaysStoppedAnimation(Colors.white),
            );
          },
        ),
      ),
    );
  }

  Widget _buildUserInfo(StoryGroup group, Story story) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          GestureDetector(
            onTap: () {
              // Navigate to profile
            },
            child: NeuroAvatar(
              imageUrl: group.userAvatarUrl,
              name: group.userName,
              size: 40,
              showBorder: true,
              borderColor: Colors.white,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      group.userName,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 15,
                      ),
                    ),
                    if (story.mood != null) ...[
                      const SizedBox(width: 8),
                      Text(story.mood!, style: const TextStyle(fontSize: 16)),
                    ],
                  ],
                ),
                Text(
                  story.timeAgo,
                  style: TextStyle(
                    color: Colors.white.withValues(alpha: 0.7),
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.more_horiz, color: Colors.white),
            onPressed: () => _showStoryOptions(story),
          ),
          IconButton(
            icon: const Icon(Icons.close, color: Colors.white),
            onPressed: () => Navigator.pop(context),
          ),
        ],
      ),
    );
  }

  Widget _buildCaption(String caption) {
    return Positioned(
      bottom: MediaQuery.of(context).padding.bottom + 80,
      left: 16,
      right: 16,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          color: Colors.black.withValues(alpha: 0.4),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(
          caption,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 16,
            height: 1.4,
          ),
          textAlign: TextAlign.center,
          maxLines: 3,
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }

  Widget _buildBottomBar() {
    return Positioned(
      bottom: MediaQuery.of(context).padding.bottom + 8,
      left: 16,
      right: 16,
      child: Row(
        children: [
          Expanded(
            child: Container(
              height: 48,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: Colors.white.withValues(alpha: 0.3)),
              ),
              child: TextField(
                style: const TextStyle(color: Colors.white),
                decoration: InputDecoration(
                  hintText: 'Send a message...',
                  hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.6)),
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                ),
                onTap: () {
                  _progressController.stop();
                },
                onSubmitted: (value) {
                  if (value.isNotEmpty) {
                    // Send message
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: const Text('Message sent!'),
                        behavior: SnackBarBehavior.floating,
                        backgroundColor: AppColors.success,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        margin: const EdgeInsets.all(16),
                      ),
                    );
                  }
                  _progressController.forward();
                },
              ),
            ),
          ),
          const SizedBox(width: 12),
          if (ref.watch(devOptionsProvider).enableStoryReactions)
          GestureDetector(
            onTap: _toggleReactions,
            child: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.15),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white.withValues(alpha: 0.3)),
              ),
              child: const Icon(
                Icons.favorite_border,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(width: 8),
          GestureDetector(
            onTap: () {
              HapticFeedback.lightImpact();
              // Share story
            },
            child: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.15),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white.withValues(alpha: 0.3)),
              ),
              child: const Icon(
                Icons.send_outlined,
                color: Colors.white,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildReactionsOverlay() {
    return Positioned.fill(
      child: GestureDetector(
        onTap: () {
          setState(() => _showReactions = false);
          _progressController.forward();
        },
        child: Container(
          color: Colors.black.withValues(alpha: 0.5),
          child: Center(
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 32),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(24),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text(
                    'React to this story',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Wrap(
                    spacing: 12,
                    runSpacing: 12,
                    alignment: WrapAlignment.center,
                    children: _reactions.map((emoji) {
                      return GestureDetector(
                        onTap: () => _sendReaction(emoji),
                        child: Container(
                          width: 56,
                          height: 56,
                          decoration: BoxDecoration(
                            color: Colors.grey.shade100,
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: Center(
                            child: Text(
                              emoji,
                              style: const TextStyle(fontSize: 28),
                            ),
                          ),
                        ),
                      );
                    }).toList(),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _showStoryOptions(Story story) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        padding: EdgeInsets.only(bottom: MediaQuery.of(context).padding.bottom),
        decoration: BoxDecoration(
          color: Theme.of(context).scaffoldBackgroundColor,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              margin: const EdgeInsets.only(top: 12),
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey.shade300,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.share_outlined),
              title: const Text('Share'),
              onTap: () {
                Navigator.pop(context);
                // Share functionality
              },
            ),
            ListTile(
              leading: const Icon(Icons.link),
              title: const Text('Copy Link'),
              onTap: () {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Link copied!')),
                );
              },
            ),
            if (story.authorId == 'current_user')
              ListTile(
                leading: const Icon(Icons.delete_outline, color: Colors.red),
                title: const Text('Delete Story', style: TextStyle(color: Colors.red)),
                onTap: () {
                  Navigator.pop(context);
                  _confirmDeleteStory(story);
                },
              ),
            ListTile(
              leading: const Icon(Icons.flag_outlined),
              title: const Text('Report'),
              onTap: () {
                Navigator.pop(context);
                // Report functionality
              },
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  void _confirmDeleteStory(Story story) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Story?'),
        content: const Text('This story will be permanently deleted.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              ref.read(storiesProvider.notifier).deleteStory(story.id);
              Navigator.pop(this.context);
              ScaffoldMessenger.of(this.context).showSnackBar(
                const SnackBar(content: Text('Story deleted')),
              );
            },
            child: const Text('Delete', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }

  ColorFilter _getColorFilter(String filter) {
    switch (filter) {
      case 'vivid':
        return const ColorFilter.matrix([
          1.3, 0, 0, 0, 0,
          0, 1.3, 0, 0, 0,
          0, 0, 1.3, 0, 0,
          0, 0, 0, 1, 0,
        ]);
      case 'warm':
        return const ColorFilter.matrix([
          1.2, 0.1, 0, 0, 10,
          0, 1.0, 0, 0, 0,
          0, 0, 0.8, 0, 0,
          0, 0, 0, 1, 0,
        ]);
      case 'cool':
        return const ColorFilter.matrix([
          0.9, 0, 0, 0, 0,
          0, 1.0, 0.1, 0, 0,
          0, 0, 1.3, 0, 10,
          0, 0, 0, 1, 0,
        ]);
      case 'vintage':
        return const ColorFilter.matrix([
          0.9, 0.15, 0.1, 0, 0,
          0.1, 0.85, 0.1, 0, 0,
          0.1, 0.1, 0.7, 0, 0,
          0, 0, 0, 1, 0,
        ]);
      case 'mono':
        return const ColorFilter.matrix([
          0.33, 0.33, 0.33, 0, 0,
          0.33, 0.33, 0.33, 0, 0,
          0.33, 0.33, 0.33, 0, 0,
          0, 0, 0, 1, 0,
        ]);
      default:
        return const ColorFilter.mode(Colors.transparent, BlendMode.multiply);
    }
  }
}


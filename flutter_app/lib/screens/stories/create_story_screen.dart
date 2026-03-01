import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../utils/cross_platform_image.dart';
import '../../core/theme/app_colors.dart';
import '../../models/story.dart';
import '../../providers/stories_provider.dart';


/// Premium Create Story Screen - A polished, neurodivergent-friendly story creation experience
class CreateStoryScreen extends ConsumerStatefulWidget {
  const CreateStoryScreen({super.key});

  @override
  ConsumerState<CreateStoryScreen> createState() => _CreateStoryScreenState();
}

class _CreateStoryScreenState extends ConsumerState<CreateStoryScreen>
    with TickerProviderStateMixin {
  XFile? _selectedMedia;
  bool _isVideo = false;
  final _captionController = TextEditingController();
  bool _isSubmitting = false;
  Color _backgroundColor = AppColors.primaryPurple;
  String _selectedFilter = 'none';
  StoryType _storyType = StoryType.text;

  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  // Beautiful gradient backgrounds for text stories
  final List<StoryBackground> _backgrounds = [
    StoryBackground(
      name: 'Purple Dream',
      colors: [const Color(0xFF7C4DFF), const Color(0xFF536DFE)],
      icon: '💜',
    ),
    StoryBackground(
      name: 'Ocean Calm',
      colors: [const Color(0xFF00BFA5), const Color(0xFF26C6DA)],
      icon: '🌊',
    ),
    StoryBackground(
      name: 'Sunset Glow',
      colors: [const Color(0xFFFF6E40), const Color(0xFFFFAB40)],
      icon: '🌅',
    ),
    StoryBackground(
      name: 'Forest Peace',
      colors: [const Color(0xFF66BB6A), const Color(0xFF26A69A)],
      icon: '🌲',
    ),
    StoryBackground(
      name: 'Night Sky',
      colors: [const Color(0xFF1A1A2E), const Color(0xFF16213E)],
      icon: '🌙',
    ),
    StoryBackground(
      name: 'Cotton Candy',
      colors: [const Color(0xFFF48FB1), const Color(0xFFCE93D8)],
      icon: '🍬',
    ),
    StoryBackground(
      name: 'Aurora',
      colors: [const Color(0xFF5C6BC0), const Color(0xFF00BCD4)],
      icon: '✨',
    ),
    StoryBackground(
      name: 'Warm Embrace',
      colors: [const Color(0xFFFF7043), const Color(0xFFFFCA28)],
      icon: '🤗',
    ),
  ];

  final List<StoryFilter> _filters = [
    StoryFilter(name: 'None', id: 'none', icon: '✨'),
    StoryFilter(name: 'Vivid', id: 'vivid', icon: '🎨'),
    StoryFilter(name: 'Warm', id: 'warm', icon: '☀️'),
    StoryFilter(name: 'Cool', id: 'cool', icon: '❄️'),
    StoryFilter(name: 'Vintage', id: 'vintage', icon: '📷'),
    StoryFilter(name: 'B&W', id: 'mono', icon: '🖤'),
  ];

  // Mood/emoji reactions for story
  final List<String> _moods = ['😊', '🎉', '💭', '💪', '🌈', '🧠', '💜', '✨'];

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _fadeAnimation = CurvedAnimation(
      parent: _fadeController,
      curve: Curves.easeOutCubic,
    );
    _fadeController.forward();

    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.05).animate(
      CurvedAnimation(parent: _pulseController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _captionController.dispose();
    _fadeController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  Future<void> _showMediaPicker() async {
    HapticFeedback.lightImpact();
    final picker = ImagePicker();

    await showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => _MediaPickerSheet(
        onGalleryTap: () async {
          Navigator.pop(context);
          final image = await picker.pickImage(source: ImageSource.gallery);
          if (image != null && mounted) {
            setState(() {
              _selectedMedia = image;
              _isVideo = false;
              _storyType = StoryType.photo;
            });
          }
        },
        onCameraTap: () async {
          Navigator.pop(context);
          final image = await picker.pickImage(source: ImageSource.camera);
          if (image != null && mounted) {
            setState(() {
              _selectedMedia = image;
              _isVideo = false;
              _storyType = StoryType.photo;
            });
          }
        },
        onVideoTap: () async {
          Navigator.pop(context);
          final video = await picker.pickVideo(
            source: ImageSource.camera,
            maxDuration: const Duration(seconds: 30),
          );
          if (video != null && mounted) {
            setState(() {
              _selectedMedia = video;
              _isVideo = true;
              _storyType = StoryType.video;
            });
          }
        },
        onTextTap: () {
          Navigator.pop(context);
          setState(() {
            _selectedMedia = null;
            _storyType = StoryType.text;
          });
        },
      ),
    );
  }

  Future<void> _publishStory() async {
    if (_storyType == StoryType.text && _captionController.text.trim().isEmpty) {
      _showErrorSnackBar('Please add some text to your story');
      return;
    }

    if ((_storyType == StoryType.photo || _storyType == StoryType.video) && _selectedMedia == null) {
      _showErrorSnackBar('Please select media for your story');
      return;
    }

    HapticFeedback.mediumImpact();
    setState(() => _isSubmitting = true);

    try {
      // Get the selected background for text stories
      final selectedBg = _backgrounds.firstWhere(
        (bg) => bg.colors.first == _backgroundColor,
        orElse: () => _backgrounds.first,
      );

      // Convert story type
      final contentType = switch (_storyType) {
        StoryType.text => StoryContentType.text,
        StoryType.photo => StoryContentType.photo,
        StoryType.video => StoryContentType.video,
      };

      // Create the story data
      final storyData = CreateStoryData(
        contentType: contentType,
        mediaPath: _selectedMedia?.path,
        caption: _captionController.text.trim().isNotEmpty
            ? _captionController.text.trim()
            : null,
        backgroundColor: '#${_backgroundColor.value.toRadixString(16).substring(2)}',
        backgroundGradient: _storyType == StoryType.text ? selectedBg.colors : null,
        filter: _selectedFilter != 'none' ? _selectedFilter : null,
        mood: _extractMood(_captionController.text),
      );

      // Create the story via provider
      final story = await ref.read(storyCreationProvider.notifier).createStory(storyData);

      if (mounted && story != null) {
        HapticFeedback.heavyImpact();
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Row(
              children: const [
                Icon(Icons.check_circle, color: Colors.white),
                SizedBox(width: 12),
                Text('Story shared! ✨'),
              ],
            ),
            behavior: SnackBarBehavior.floating,
            backgroundColor: AppColors.success,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            margin: const EdgeInsets.all(16),
          ),
        );
      } else if (mounted) {
        final error = ref.read(storyCreationProvider).error;
        _showErrorSnackBar(error ?? 'Failed to share story. Please try again.');
      }
    } catch (e) {
      _showErrorSnackBar('Failed to share story. Please try again.');
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  /// Extract mood emoji from caption text
  String? _extractMood(String text) {
    for (final mood in _moods) {
      if (text.contains(mood)) {
        return mood;
      }
    }
    return null;
  }

  void _showErrorSnackBar(String message) {
    if (!mounted) return;
    HapticFeedback.heavyImpact();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.error_outline, color: Colors.white),
            const SizedBox(width: 12),
            Expanded(child: Text(message)),
          ],
        ),
        behavior: SnackBarBehavior.floating,
        backgroundColor: Theme.of(context).colorScheme.error,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final bottomPadding = MediaQuery.of(context).padding.bottom;

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0D0D12) : const Color(0xFFF0F2F5),
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: Column(
          children: [
            // Custom App Bar
            _buildAppBar(context, theme),

            // Main Content
            Expanded(
              child: _storyType == StoryType.text
                  ? _buildTextStoryEditor(theme)
                  : _buildMediaStoryEditor(theme),
            ),

            // Bottom Controls
            _buildBottomControls(context, theme, bottomPadding),
          ],
        ),
      ),
    );
  }

  Widget _buildAppBar(BuildContext context, ThemeData theme) {
    return Container(
      padding: EdgeInsets.only(
        top: MediaQuery.of(context).padding.top + 8,
        left: 8,
        right: 16,
        bottom: 8,
      ),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        border: Border(
          bottom: BorderSide(
            color: theme.dividerColor.withOpacity(0.1),
          ),
        ),
      ),
      child: Row(
        children: [
          IconButton(
            icon: Icon(Icons.close, color: theme.colorScheme.onSurface),
            onPressed: () {
              HapticFeedback.lightImpact();
              Navigator.pop(context);
            },
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Create Story',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  _getStoryTypeLabel(),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.primary,
                  ),
                ),
              ],
            ),
          ),
          // Publish Button
          AnimatedBuilder(
            animation: _pulseAnimation,
            builder: (context, child) {
              return Transform.scale(
                scale: _isSubmitting ? 1.0 : _pulseAnimation.value,
                child: child,
              );
            },
            child: FilledButton.icon(
              onPressed: _isSubmitting ? null : _publishStory,
              icon: _isSubmitting
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation(Colors.white),
                      ),
                    )
                  : const Icon(Icons.send_rounded, size: 18),
              label: Text(_isSubmitting ? 'Sharing...' : 'Share'),
              style: FilledButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _getStoryTypeLabel() {
    switch (_storyType) {
      case StoryType.text:
        return 'Text Story';
      case StoryType.photo:
        return 'Photo Story';
      case StoryType.video:
        return 'Video Story';
    }
  }

  Widget _buildTextStoryEditor(ThemeData theme) {
    final selectedBg = _backgrounds.firstWhere(
      (bg) => bg.colors.first == _backgroundColor,
      orElse: () => _backgrounds.first,
    );

    return Column(
      children: [
        // Story Preview
        Expanded(
          child: Container(
            margin: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: selectedBg.colors,
              ),
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: selectedBg.colors.first.withOpacity(0.3),
                  blurRadius: 20,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: Stack(
              children: [
                // Background pattern (subtle)
                Positioned.fill(
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(24),
                    child: CustomPaint(
                      painter: _PatternPainter(
                        color: Colors.white.withOpacity(0.05),
                      ),
                    ),
                  ),
                ),
                // Text input
                Center(
                  child: Padding(
                    padding: const EdgeInsets.all(32),
                    child: TextField(
                      controller: _captionController,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                        height: 1.4,
                      ),
                      textAlign: TextAlign.center,
                      maxLines: 6,
                      maxLength: 200,
                      decoration: InputDecoration(
                        hintText: 'Share your thoughts...',
                        hintStyle: TextStyle(
                          color: Colors.white.withOpacity(0.5),
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                        ),
                        border: InputBorder.none,
                        counterStyle: TextStyle(
                          color: Colors.white.withOpacity(0.5),
                        ),
                      ),
                      onChanged: (_) => setState(() {}),
                    ),
                  ),
                ),
                // Story type indicator
                Positioned(
                  top: 16,
                  left: 16,
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: Colors.black.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(selectedBg.icon, style: const TextStyle(fontSize: 14)),
                        const SizedBox(width: 6),
                        Text(
                          selectedBg.name,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),

        // Background Selector
        _buildBackgroundSelector(theme),
      ],
    );
  }

  Widget _buildBackgroundSelector(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Text(
              'Choose Background',
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 80,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _backgrounds.length,
              itemBuilder: (context, index) {
                final bg = _backgrounds[index];
                final isSelected = bg.colors.first == _backgroundColor;

                return GestureDetector(
                  onTap: () {
                    HapticFeedback.selectionClick();
                    setState(() => _backgroundColor = bg.colors.first);
                  },
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    width: 64,
                    margin: const EdgeInsets.only(right: 12),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: bg.colors,
                      ),
                      borderRadius: BorderRadius.circular(16),
                      border: isSelected
                          ? Border.all(color: Colors.white, width: 3)
                          : null,
                      boxShadow: isSelected
                          ? [
                              BoxShadow(
                                color: bg.colors.first.withOpacity(0.5),
                                blurRadius: 12,
                                offset: const Offset(0, 4),
                              ),
                            ]
                          : null,
                    ),
                    child: Center(
                      child: Text(
                        bg.icon,
                        style: const TextStyle(fontSize: 24),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMediaStoryEditor(ThemeData theme) {
    return Column(
      children: [
        // Media Preview
        Expanded(
          child: Container(
            margin: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.black,
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.3),
                  blurRadius: 20,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(24),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  // Media content
                  if (_selectedMedia != null)
                    _isVideo
                        ? _buildVideoPreview()
                        : ColorFiltered(
                            colorFilter: _getColorFilter(),
                            child: buildXFileImage(
                              _selectedMedia!,
                              fit: BoxFit.cover,
                            ),
                          )
                  else
                    _buildMediaPlaceholder(theme),

                  // Caption overlay at bottom
                  if (_selectedMedia != null)
                    Positioned(
                      bottom: 0,
                      left: 0,
                      right: 0,
                      child: Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              Colors.transparent,
                              Colors.black.withOpacity(0.8),
                            ],
                          ),
                        ),
                        child: TextField(
                          controller: _captionController,
                          style: const TextStyle(color: Colors.white),
                          maxLines: 2,
                          maxLength: 150,
                          decoration: InputDecoration(
                            hintText: 'Add a caption...',
                            hintStyle: TextStyle(color: Colors.white.withOpacity(0.6)),
                            border: InputBorder.none,
                            counterStyle: TextStyle(color: Colors.white.withOpacity(0.5)),
                          ),
                        ),
                      ),
                    ),

                  // Filter indicator
                  if (_selectedMedia != null && _selectedFilter != 'none')
                    Positioned(
                      top: 16,
                      left: 16,
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.5),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              _filters.firstWhere((f) => f.id == _selectedFilter).icon,
                              style: const TextStyle(fontSize: 14),
                            ),
                            const SizedBox(width: 6),
                            Text(
                              _filters.firstWhere((f) => f.id == _selectedFilter).name,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),

        // Filter Selector (for photo stories)
        if (_storyType == StoryType.photo && _selectedMedia != null)
          _buildFilterSelector(theme),
      ],
    );
  }

  Widget _buildVideoPreview() {
    return Container(
      color: Colors.black,
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.1),
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
              'Video ready to share',
              style: TextStyle(
                color: Colors.white.withOpacity(0.7),
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Up to 30 seconds',
              style: TextStyle(
                color: Colors.white.withOpacity(0.5),
                fontSize: 14,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMediaPlaceholder(ThemeData theme) {
    return GestureDetector(
      onTap: _showMediaPicker,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              theme.colorScheme.surfaceContainerHighest,
              theme.colorScheme.surfaceContainerHighest.withOpacity(0.5),
            ],
          ),
        ),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primary.withOpacity(0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  Icons.add_photo_alternate_rounded,
                  size: 48,
                  color: theme.colorScheme.primary,
                ),
              ),
              const SizedBox(height: 16),
              Text(
                'Tap to add media',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Photo or Video',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFilterSelector(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Text(
              'Filters',
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 80,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _filters.length,
              itemBuilder: (context, index) {
                final filter = _filters[index];
                final isSelected = filter.id == _selectedFilter;

                return GestureDetector(
                  onTap: () {
                    HapticFeedback.selectionClick();
                    setState(() => _selectedFilter = filter.id);
                  },
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    width: 64,
                    margin: const EdgeInsets.only(right: 12),
                    decoration: BoxDecoration(
                      color: isSelected
                          ? theme.colorScheme.primaryContainer
                          : theme.colorScheme.surfaceContainerHighest,
                      borderRadius: BorderRadius.circular(16),
                      border: isSelected
                          ? Border.all(color: theme.colorScheme.primary, width: 2)
                          : null,
                    ),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          filter.icon,
                          style: const TextStyle(fontSize: 24),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          filter.name,
                          style: theme.textTheme.labelSmall?.copyWith(
                            fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                            color: isSelected
                                ? theme.colorScheme.primary
                                : theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomControls(BuildContext context, ThemeData theme, double bottomPadding) {
    return Container(
      padding: EdgeInsets.only(
        left: 16,
        right: 16,
        top: 12,
        bottom: bottomPadding + 16,
      ),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        border: Border(
          top: BorderSide(
            color: theme.dividerColor.withOpacity(0.1),
          ),
        ),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Story type selector
          _buildStoryTypeSelector(theme),
          const SizedBox(height: 16),

          // Quick mood selector
          _buildMoodSelector(theme),
          const SizedBox(height: 16),

          // Add media button (for text stories)
          if (_storyType == StoryType.text)
            _AddMediaButton(
              onTap: () {
                HapticFeedback.lightImpact();
                _showMediaPicker();
              },
            ),

          // Change media button (for media stories)
          if (_storyType != StoryType.text && _selectedMedia != null)
            OutlinedButton.icon(
              onPressed: _showMediaPicker,
              icon: const Icon(Icons.refresh_rounded),
              label: const Text('Change Media'),
              style: OutlinedButton.styleFrom(
                minimumSize: const Size(double.infinity, 48),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildStoryTypeSelector(ThemeData theme) {
    return Row(
      children: [
        _StoryTypeChip(
          icon: Icons.text_fields_rounded,
          label: 'Text',
          isSelected: _storyType == StoryType.text,
          onTap: () {
            HapticFeedback.selectionClick();
            setState(() {
              _storyType = StoryType.text;
              _selectedMedia = null;
            });
          },
        ),
        const SizedBox(width: 8),
        _StoryTypeChip(
          icon: Icons.photo_rounded,
          label: 'Photo',
          isSelected: _storyType == StoryType.photo,
          onTap: () {
            HapticFeedback.selectionClick();
            if (_storyType != StoryType.photo) {
              _showMediaPicker();
            }
          },
        ),
        const SizedBox(width: 8),
        _StoryTypeChip(
          icon: Icons.videocam_rounded,
          label: 'Video',
          isSelected: _storyType == StoryType.video,
          onTap: () async {
            HapticFeedback.selectionClick();
            final picker = ImagePicker();
            final video = await picker.pickVideo(
              source: ImageSource.camera,
              maxDuration: const Duration(seconds: 30),
            );
            if (video != null && mounted) {
              setState(() {
                _selectedMedia = video;
                _isVideo = true;
                _storyType = StoryType.video;
              });
            }
          },
        ),
      ],
    );
  }

  Widget _buildMoodSelector(ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Add a mood',
          style: theme.textTheme.labelMedium?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: _moods.map((mood) {
            return GestureDetector(
              onTap: () {
                HapticFeedback.selectionClick();
                final currentText = _captionController.text;
                if (!currentText.contains(mood)) {
                  _captionController.text = '$currentText $mood'.trim();
                  _captionController.selection = TextSelection.fromPosition(
                    TextPosition(offset: _captionController.text.length),
                  );
                }
              },
              child: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(mood, style: const TextStyle(fontSize: 20)),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  ColorFilter _getColorFilter() {
    switch (_selectedFilter) {
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

// ============================================================================
// Supporting Widgets
// ============================================================================

enum StoryType { text, photo, video }

class StoryBackground {
  final String name;
  final List<Color> colors;
  final String icon;

  const StoryBackground({
    required this.name,
    required this.colors,
    required this.icon,
  });
}

class StoryFilter {
  final String name;
  final String id;
  final String icon;

  const StoryFilter({
    required this.name,
    required this.id,
    required this.icon,
  });
}

/// Media picker bottom sheet
class _MediaPickerSheet extends StatelessWidget {
  final VoidCallback onGalleryTap;
  final VoidCallback onCameraTap;
  final VoidCallback onVideoTap;
  final VoidCallback onTextTap;

  const _MediaPickerSheet({
    required this.onGalleryTap,
    required this.onCameraTap,
    required this.onVideoTap,
    required this.onTextTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bottomPadding = MediaQuery.of(context).padding.bottom;

    return Container(
      padding: EdgeInsets.only(bottom: bottomPadding),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Handle
          Container(
            margin: const EdgeInsets.only(top: 12),
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: theme.colorScheme.outline.withOpacity(0.3),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                Text(
                  'Add to your story',
                  style: theme.textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _MediaOption(
                      icon: Icons.photo_library_rounded,
                      label: 'Gallery',
                      color: AppColors.calmBlue,
                      onTap: onGalleryTap,
                    ),
                    _MediaOption(
                      icon: Icons.camera_alt_rounded,
                      label: 'Camera',
                      color: AppColors.primaryPurple,
                      onTap: onCameraTap,
                    ),
                    _MediaOption(
                      icon: Icons.videocam_rounded,
                      label: 'Video',
                      color: AppColors.accentOrange,
                      onTap: onVideoTap,
                    ),
                    _MediaOption(
                      icon: Icons.text_fields_rounded,
                      label: 'Text',
                      color: AppColors.secondaryTeal,
                      onTap: onTextTap,
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _MediaOption extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;

  const _MediaOption({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: () {
        HapticFeedback.lightImpact();
        onTap();
      },
      child: Column(
        children: [
          Container(
            width: 64,
            height: 64,
            decoration: BoxDecoration(
              color: color.withOpacity(0.15),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Icon(icon, color: color, size: 28),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: theme.textTheme.labelMedium?.copyWith(
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
}

class _StoryTypeChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _StoryTypeChip({
    required this.icon,
    required this.label,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            color: isSelected
                ? theme.colorScheme.primaryContainer
                : theme.colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(12),
            border: isSelected
                ? Border.all(color: theme.colorScheme.primary, width: 2)
                : null,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: 18,
                color: isSelected
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurfaceVariant,
              ),
              const SizedBox(width: 6),
              Text(
                label,
                style: theme.textTheme.labelMedium?.copyWith(
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                  color: isSelected
                      ? theme.colorScheme.primary
                      : theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _AddMediaButton extends StatelessWidget {
  final VoidCallback onTap;

  const _AddMediaButton({required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return OutlinedButton.icon(
      onPressed: onTap,
      icon: const Icon(Icons.add_photo_alternate_rounded),
      label: const Text('Add Photo or Video'),
      style: OutlinedButton.styleFrom(
        minimumSize: const Size(double.infinity, 48),
        side: BorderSide(color: theme.colorScheme.outline.withOpacity(0.3)),
      ),
    );
  }
}

/// Pattern painter for text story backgrounds
class _PatternPainter extends CustomPainter {
  final Color color;

  _PatternPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 1
      ..style = PaintingStyle.stroke;

    const spacing = 30.0;

    // Draw diagonal lines
    for (double i = -size.height; i < size.width + size.height; i += spacing) {
      canvas.drawLine(
        Offset(i, 0),
        Offset(i + size.height, size.height),
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}


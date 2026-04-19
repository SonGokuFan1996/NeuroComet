import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../models/post.dart';
import '../../providers/feed_provider.dart';
import '../../services/supabase_service.dart';
import '../../services/app_services.dart';
import '../../l10n/app_localizations.dart';
import '../../utils/cross_platform_image.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Screen for creating a new post (Polished Bottom Sheet style, inside a Scaffold)
class CreatePostScreen extends ConsumerStatefulWidget {
  const CreatePostScreen({super.key});

  @override
  ConsumerState<CreatePostScreen> createState() => _CreatePostScreenState();
}

class _CreatePostScreenState extends ConsumerState<CreatePostScreen> {
  final _contentController = TextEditingController();
  XFile? _selectedMedia;
  bool _isVideo = false;
  String? _selectedMood;
  bool _hasContentWarning = false;
  Color? _selectedBackgroundColor;
  bool _isSubmitting = false;
  bool _isResolvingLocation = false;
  String? _locationTag;

  final int maxCharacters = 1000; // Premium users logic would alter this

  final List<Color> _backgroundColors = [
    const Color(0xFF1a1a2e), const Color(0xFF16213e), const Color(0xFF0f3460),
    const Color(0xFF4ECDC4), const Color(0xFF6BCB77), const Color(0xFFFFB347),
    const Color(0xFFFF6B6B), const Color(0xFF9B59B6), const Color(0xFFE91E63),
    const Color(0xFF3F51B5), const Color(0xFF009688), const Color(0xFF795548)
  ];

  late List<Map<String, String>> _moods;

  @override
  void initState() {
    super.initState();
    _contentController.addListener(_saveDraft);
    _loadDraft();
  }

  Future<void> _loadDraft() async {
    final prefs = await SharedPreferences.getInstance();
    if (!mounted) return;
    setState(() {
      _contentController.text = prefs.getString('draft_post_content') ?? '';
      _selectedMood = prefs.getString('draft_post_mood');
      _hasContentWarning = prefs.getBool('draft_post_cw') ?? false;
      _locationTag = prefs.getString('draft_post_location');
      final bg = prefs.getInt('draft_post_bg');
      if (bg != null) _selectedBackgroundColor = Color(bg);
    });
  }

  Future<void> _saveDraft() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('draft_post_content', _contentController.text);
    if (_selectedMood != null) {
      await prefs.setString('draft_post_mood', _selectedMood!);
    } else {
      await prefs.remove('draft_post_mood');
    }
    await prefs.setBool('draft_post_cw', _hasContentWarning);
    if (_locationTag != null) {
      await prefs.setString('draft_post_location', _locationTag!);
    } else {
      await prefs.remove('draft_post_location');
    }
    if (_selectedBackgroundColor != null) {
      await prefs.setInt('draft_post_bg', _selectedBackgroundColor!.value);
    } else {
      await prefs.remove('draft_post_bg');
    }
  }

  Future<void> _clearDraft() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('draft_post_content');
    await prefs.remove('draft_post_mood');
    await prefs.remove('draft_post_cw');
    await prefs.remove('draft_post_location');
    await prefs.remove('draft_post_bg');
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final l10n = AppLocalizations.of(context)!;
    _moods = [
      {'emoji': '😊', 'label': l10n.get('moodMessageGood')},
      {'emoji': '🤔', 'label': 'Thoughtful'},
      {'emoji': '😴', 'label': 'Tired'},
      {'emoji': '🎉', 'label': 'Celebrating'},
      {'emoji': '💪', 'label': 'Motivated'},
      {'emoji': '😌', 'label': 'Calm'},
      {'emoji': '🤯', 'label': 'Mind Blown'},
      {'emoji': '💡', 'label': 'Inspired'}
    ];
  }

  @override
  void dispose() {
    _contentController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    HapticFeedback.lightImpact();
    final picker = ImagePicker();
    final image = await picker.pickImage(source: ImageSource.gallery, maxWidth: 1920, maxHeight: 1920, imageQuality: 85);
    if (image != null && mounted) {
      setState(() {
        _selectedMedia = image;
        _isVideo = false;
      });
    }
  }

  Future<void> _takePhoto() async {
    HapticFeedback.lightImpact();
    final picker = ImagePicker();
    final photo = await picker.pickImage(source: ImageSource.camera, maxWidth: 1920, maxHeight: 1920, imageQuality: 85);
    if (photo != null && mounted) {
      setState(() {
        _selectedMedia = photo;
        _isVideo = false;
      });
    }
  }

  Future<void> _pickVideo() async {
    HapticFeedback.lightImpact();
    final picker = ImagePicker();
    final video = await picker.pickVideo(source: ImageSource.gallery, maxDuration: const Duration(seconds: 30));
    if (video != null && mounted) {
      setState(() {
        _selectedMedia = video;
        _isVideo = true;
      });
    }
  }

  Future<void> _submitPost() async {
    if (_contentController.text.trim().isEmpty && _selectedMedia == null) return;
    
    HapticFeedback.mediumImpact();
    setState(() => _isSubmitting = true);

    try {
      String? imageUrl;
      String? videoUrl;

      if (_selectedMedia != null) {
        final bytes = await _selectedMedia!.readAsBytes();
        final ext = _selectedMedia!.name.split('.').last;
        final path = 'posts/${DateTime.now().millisecondsSinceEpoch}.$ext';
        final url = await SupabaseService.uploadImage(
          bucket: 'post-media',
          path: path,
          bytes: bytes,
          contentType: _isVideo ? 'video/$ext' : 'image/$ext',
        );
        if (_isVideo) {
          videoUrl = url;
        } else {
          imageUrl = url;
        }
      }

      String content = _contentController.text.trim();
      if (_selectedMood != null) {
        content = "$_selectedMood $content";
      }
      if (_hasContentWarning) {
        content = "⚠️ Content Warning\n\n$content";
      }

      final mediaUrls = imageUrl != null
          ? [imageUrl]
          : (videoUrl != null ? [videoUrl] : null);
      final createdPost = SupabaseService.isInitialized &&
              SupabaseService.isAuthenticated &&
              SupabaseService.currentUser != null
          ? await SupabaseService.createPost(
              content: content,
              category: '/gen',
              mediaUrls: mediaUrls,
              locationTag: _locationTag,
            )
          : Post(
              id: 'local_${DateTime.now().millisecondsSinceEpoch}',
              authorId: 'local_user',
              authorName: 'You',
              content: content,
              mediaUrls: mediaUrls,
              category: '/gen',
              createdAt: DateTime.now(),
              backgroundColor: _selectedBackgroundColor?.value,
              locationTag: _locationTag,
            );

      ref.read(feedProvider.notifier).injectDevPost(
            createdPost.copyWith(
              backgroundColor: createdPost.backgroundColor ?? _selectedBackgroundColor?.value,
              locationTag: createdPost.locationTag ?? _locationTag,
            ),
          );

      if (mounted) {
        HapticFeedback.heavyImpact();
        await _clearDraft();
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Post shared successfully!'), behavior: SnackBarBehavior.floating),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to create post: $e'), behavior: SnackBarBehavior.floating, backgroundColor: Theme.of(context).colorScheme.error),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  Future<void> _toggleLocationTag() async {
    if (_isResolvingLocation) return;
    HapticFeedback.lightImpact();

    if (_locationTag != null) {
      setState(() { _locationTag = null; _saveDraft(); });
      return;
    }

    setState(() => _isResolvingLocation = true);
    final locationService = LocationService();

    try {
      final locationTag = await locationService.getCurrentLocationTag();
      if (!mounted) return;

      if (locationTag == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Unable to get your current location right now.'), behavior: SnackBarBehavior.floating),
        );
        return;
      }
      setState(() { _locationTag = locationTag; _saveDraft(); });
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to resolve your location: $e'), behavior: SnackBarBehavior.floating),
      );
    } finally {
      if (mounted) setState(() => _isResolvingLocation = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final remainingCharacters = maxCharacters - _contentController.text.length;
    final bottomPadding = MediaQuery.of(context).padding.bottom;

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        title: Row(
          children: [
            Icon(Icons.create_rounded, color: theme.colorScheme.primary),
            const SizedBox(width: 8),
            Text('Create Post', style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface)),
          ],
        ),
        leading: IconButton(
          icon: const Icon(Icons.close_rounded),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12, top: 8, bottom: 8),
            child: FilledButton.icon(
              onPressed: _isSubmitting || (_contentController.text.isEmpty && _selectedMedia == null) ? null : _submitPost,
              icon: _isSubmitting 
                ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, valueColor: AlwaysStoppedAnimation(Colors.white))) 
                : const Icon(Icons.send_rounded, size: 16),
              label: Text(_isSubmitting ? 'Posting...' : 'Post', style: const TextStyle(fontWeight: FontWeight.bold)),
            ),
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.only(bottom: bottomPadding + 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Text Input
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: TextFormField(
                controller: _contentController,
                maxLines: 8,
                minLines: 4,
                maxLength: maxCharacters,
                style: theme.textTheme.bodyLarge,
                decoration: InputDecoration(
                  hintText: _selectedMood != null ? "What's on your mind? $_selectedMood" : "What's on your mind? Share with your community...",
                  hintStyle: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.6)),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide.none,
                  ),
                  filled: true,
                  fillColor: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide(color: theme.colorScheme.primary.withValues(alpha: 0.5)),
                  ),
                ),
                onChanged: (_) => setState(() {}),
              ),
            ),

            // Media Preview
            if (_selectedMedia != null)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Stack(
                  children: [
                    Container(
                      width: double.infinity,
                      height: 200,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(16),
                        color: theme.colorScheme.surfaceContainerHighest,
                      ),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: _isVideo
                          ? const Center(child: Icon(Icons.videocam_rounded, size: 48, color: Colors.white54))
                          : CrossPlatformImage(xFile: _selectedMedia!, fit: BoxFit.cover),
                      ),
                    ),
                    Positioned(
                      top: 8,
                      right: 8,
                      child: GestureDetector(
                        onTap: () => setState(() => _selectedMedia = null),
                        child: Container(
                          padding: const EdgeInsets.all(6),
                          decoration: BoxDecoration(color: Colors.black.withValues(alpha: 0.6), shape: BoxShape.circle),
                          child: const Icon(Icons.close_rounded, size: 18, color: Colors.white),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

            // Location Tag
            if (_locationTag != null)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.secondaryContainer.withValues(alpha: 0.5),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.place_outlined, color: theme.colorScheme.onSecondaryContainer, size: 18),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          _locationTag!,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: theme.colorScheme.onSecondaryContainer,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),

            const SizedBox(height: 16),

            // Toolbar actions (Camera, Image, Location, CW)
            SizedBox(
              height: 60,
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: [
                  _ToolbarButton(icon: Icons.camera_alt_rounded, onTap: _takePhoto, tooltip: 'Camera'),
                  const SizedBox(width: 12),
                  _ToolbarButton(icon: Icons.image_rounded, onTap: _pickImage, tooltip: 'Gallery'),
                  const SizedBox(width: 12),
                  _ToolbarButton(icon: Icons.videocam_rounded, onTap: _pickVideo, tooltip: 'Video'),
                  const SizedBox(width: 12),
                  _ToolbarButton(
                    icon: _locationTag == null ? Icons.location_on_outlined : Icons.location_off_rounded, 
                    onTap: _toggleLocationTag, 
                    tooltip: 'Location',
                    isLoading: _isResolvingLocation,
                  ),
                  const SizedBox(width: 12),
                  GestureDetector(
                    onTap: () {
                      HapticFeedback.selectionClick();
                      setState(() { _hasContentWarning = !_hasContentWarning; _saveDraft(); });
                    },
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      decoration: BoxDecoration(
                        color: _hasContentWarning ? theme.colorScheme.errorContainer : theme.colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.warning_rounded, size: 20, color: _hasContentWarning ? theme.colorScheme.error : theme.colorScheme.onSurfaceVariant),
                          const SizedBox(width: 8),
                          Text('CW', style: TextStyle(fontWeight: FontWeight.w600, color: _hasContentWarning ? theme.colorScheme.onErrorContainer : theme.colorScheme.onSurfaceVariant)),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Text('How are you feeling?', style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            ),
            const SizedBox(height: 12),
            
            // Mood Selector
            SizedBox(
              height: 50,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _moods.length,
                itemBuilder: (context, index) {
                  final mood = _moods[index];
                  final isSelected = _selectedMood == mood['emoji'];
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: FilterChip(
                      selected: isSelected,
                      showCheckmark: false,
                      onSelected: (selected) {
                        HapticFeedback.selectionClick();
                        setState(() { _selectedMood = selected ? mood['emoji'] : null; _saveDraft(); });
                      },
                      label: Text(mood['emoji']!, style: const TextStyle(fontSize: 20)),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                      backgroundColor: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
                    ),
                  );
                },
              ),
            ),

            const SizedBox(height: 24),
            
            // Background Colors (Only shown if no media)
            if (_selectedMedia == null) ...[
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Text('Post Background', style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
              ),
              const SizedBox(height: 12),
              SizedBox(
                height: 48,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: _backgroundColors.length + 1,
                  itemBuilder: (context, index) {
                    if (index == 0) {
                      return GestureDetector(
                        onTap: () => setState(() { _selectedBackgroundColor = null; _saveDraft(); }),
                        child: Container(
                          width: 48,
                          height: 48,
                          margin: const EdgeInsets.only(right: 12),
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: theme.colorScheme.surfaceContainerHighest,
                            border: _selectedBackgroundColor == null ? Border.all(color: theme.colorScheme.primary, width: 2) : null,
                          ),
                          child: Icon(Icons.block_rounded, size: 20, color: theme.colorScheme.onSurfaceVariant),
                        ),
                      );
                    }
                    
                    final color = _backgroundColors[index - 1];
                    final isSelected = _selectedBackgroundColor == color;
                    return GestureDetector(
                      onTap: () {
                        HapticFeedback.selectionClick();
                        setState(() { _selectedBackgroundColor = color; _saveDraft(); });
                      },
                      child: Container(
                        width: 48,
                        height: 48,
                        margin: const EdgeInsets.only(right: 12),
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: color,
                          border: isSelected ? Border.all(color: theme.colorScheme.primary, width: 3) : null,
                          boxShadow: isSelected ? [BoxShadow(color: color.withValues(alpha: 0.4), blurRadius: 8, offset: const Offset(0, 2))] : null,
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _ToolbarButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  final String tooltip;
  final bool isLoading;

  const _ToolbarButton({
    required this.icon,
    required this.onTap,
    required this.tooltip,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: tooltip,
      child: InkWell(
        onTap: isLoading ? null : () {
          HapticFeedback.lightImpact();
          onTap();
        },
        borderRadius: BorderRadius.circular(14),
        child: Container(
          width: 60,
          height: 60,
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
            borderRadius: BorderRadius.circular(14),
          ),
          child: Center(
            child: isLoading 
              ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
              : Icon(icon, color: Theme.of(context).colorScheme.primary),
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../providers/feed_provider.dart';
import '../../services/supabase_service.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../utils/cross_platform_image.dart';

/// Screen for creating a new post
class CreatePostScreen extends ConsumerStatefulWidget {
  const CreatePostScreen({super.key});

  @override
  ConsumerState<CreatePostScreen> createState() => _CreatePostScreenState();
}

class _CreatePostScreenState extends ConsumerState<CreatePostScreen> {
  final _contentController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  final List<XFile> _selectedImages = [];
  String? _selectedCategory;
  final List<String> _selectedTags = [];
  bool _isSubmitting = false;

  // Categories for neurodivergent community
  final List<Map<String, dynamic>> _categories = [
    {'id': 'adhd', 'name': 'ADHD', 'icon': Icons.bolt, 'color': AppColors.categoryADHD},
    {'id': 'autism', 'name': 'Autism', 'icon': Icons.hub, 'color': AppColors.categoryAutism},
    {'id': 'dyslexia', 'name': 'Dyslexia', 'icon': Icons.menu_book, 'color': AppColors.categoryDyslexia},
    {'id': 'anxiety', 'name': 'Anxiety', 'icon': Icons.psychology, 'color': AppColors.categoryAnxiety},
    {'id': 'depression', 'name': 'Depression', 'icon': Icons.cloud, 'color': AppColors.categoryDepression},
    {'id': 'ocd', 'name': 'OCD', 'icon': Icons.repeat, 'color': AppColors.categoryOCD},
    {'id': 'bipolar', 'name': 'Bipolar', 'icon': Icons.swap_vert, 'color': AppColors.categoryBipolar},
    {'id': 'general', 'name': 'General', 'icon': Icons.people, 'color': AppColors.categoryGeneral},
  ];

  // Common tags for the community
  final List<String> _availableTags = [
    'Support',
    'Tips',
    'Question',
    'Win',
    'Struggle',
    'Meme',
    'Resource',
    'Story',
    'Art',
    'Music',
    'Sensory',
    'SelfCare',
    'Stimming',
    'Masking',
    'Burnout',
    'Celebration',
  ];

  @override
  void dispose() {
    _contentController.dispose();
    super.dispose();
  }

  /// Build image preview that works on both web and native platforms
  Widget _buildImagePreview(XFile image) {
    return CrossPlatformImage(
      xFile: image,
      width: 100,
      height: 100,
      fit: BoxFit.cover,
    );
  }

  Future<void> _pickImages() async {
    final picker = ImagePicker();
    final images = await picker.pickMultiImage(
      maxWidth: 1920,
      maxHeight: 1920,
      imageQuality: 85,
    );

    if (images.isNotEmpty) {
      setState(() {
        // Limit to 4 images
        final remaining = 4 - _selectedImages.length;
        _selectedImages.addAll(images.take(remaining));
      });
    }
  }

  Future<void> _takePhoto() async {
    final picker = ImagePicker();
    final photo = await picker.pickImage(
      source: ImageSource.camera,
      maxWidth: 1920,
      maxHeight: 1920,
      imageQuality: 85,
    );

    if (photo != null && _selectedImages.length < 4) {
      setState(() {
        _selectedImages.add(photo);
      });
    }
  }

  void _removeImage(int index) {
    setState(() {
      _selectedImages.removeAt(index);
    });
  }

  void _toggleTag(String tag) {
    setState(() {
      if (_selectedTags.contains(tag)) {
        _selectedTags.remove(tag);
      } else if (_selectedTags.length < 5) {
        _selectedTags.add(tag);
      }
    });
  }

  Future<void> _submitPost() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    try {
      // Upload images to storage and get URLs
      List<String>? mediaUrls;
      if (_selectedImages.isNotEmpty) {
        mediaUrls = [];
        for (int i = 0; i < _selectedImages.length; i++) {
          final image = _selectedImages[i];
          final bytes = await image.readAsBytes();
          final ext = image.name.split('.').last;
          final path = 'posts/${DateTime.now().millisecondsSinceEpoch}_$i.$ext';
          final url = await SupabaseService.uploadImage(
            bucket: 'post-media',
            path: path,
            bytes: bytes,
            contentType: 'image/$ext',
          );
          mediaUrls.add(url);
        }
      }

      await ref.read(createPostProvider.notifier).createPost(
        content: _contentController.text.trim(),
        category: _selectedCategory,
        tags: _selectedTags.isNotEmpty ? _selectedTags : null,
        mediaUrls: mediaUrls,
      );

      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Post created successfully!'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to create post: $e'),
            behavior: SnackBarBehavior.floating,
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.createPost),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: FilledButton(
              onPressed: _isSubmitting || _contentController.text.isEmpty
                  ? null
                  : _submitPost,
              child: _isSubmitting
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : Text(l10n.post),
            ),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Content input
              TextFormField(
                controller: _contentController,
                maxLines: 8,
                minLines: 4,
                maxLength: 2000,
                decoration: InputDecoration(
                  hintText: 'What\'s on your mind? Share with your community...',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none,
                  ),
                  filled: true,
                  fillColor: theme.colorScheme.surfaceContainerHighest.withOpacity(0.5),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Please enter some content';
                  }
                  return null;
                },
                onChanged: (_) => setState(() {}),
              ),

              const SizedBox(height: 20),

              // Media attachments
              if (_selectedImages.isNotEmpty) ...[
                Text(
                  'Attached Media',
                  style: theme.textTheme.titleSmall,
                ),
                const SizedBox(height: 8),
                SizedBox(
                  height: 100,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: _selectedImages.length,
                    itemBuilder: (context, index) {
                      return Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: Stack(
                          children: [
                            ClipRRect(
                              borderRadius: BorderRadius.circular(8),
                              child: _buildImagePreview(_selectedImages[index]),
                            ),
                            Positioned(
                              top: 4,
                              right: 4,
                              child: GestureDetector(
                                onTap: () => _removeImage(index),
                                child: Container(
                                  padding: const EdgeInsets.all(4),
                                  decoration: const BoxDecoration(
                                    color: Colors.black54,
                                    shape: BoxShape.circle,
                                  ),
                                  child: const Icon(
                                    Icons.close,
                                    size: 16,
                                    color: Colors.white,
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      );
                    },
                  ),
                ),
                const SizedBox(height: 20),
              ],

              // Media buttons
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _selectedImages.length >= 4 ? null : _pickImages,
                      icon: const Icon(Icons.photo_library),
                      label: const Text('Gallery'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _selectedImages.length >= 4 ? null : _takePhoto,
                      icon: const Icon(Icons.camera_alt),
                      label: const Text('Camera'),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 24),

              // Category selection
              Text(
                'Category (optional)',
                style: theme.textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _categories.map((category) {
                  final isSelected = _selectedCategory == category['id'];
                  return FilterChip(
                    selected: isSelected,
                    label: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          category['icon'] as IconData,
                          size: 16,
                          color: isSelected ? Colors.white : category['color'] as Color,
                        ),
                        const SizedBox(width: 4),
                        Text(category['name'] as String),
                      ],
                    ),
                    selectedColor: category['color'] as Color,
                    onSelected: (selected) {
                      setState(() {
                        _selectedCategory = selected ? category['id'] as String : null;
                      });
                    },
                  );
                }).toList(),
              ),

              const SizedBox(height: 24),

              // Tags selection
              Text(
                'Tags (optional, max 5)',
                style: theme.textTheme.titleSmall,
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _availableTags.map((tag) {
                  final isSelected = _selectedTags.contains(tag);
                  return FilterChip(
                    selected: isSelected,
                    label: Text('#$tag'),
                    onSelected: (selected) => _toggleTag(tag),
                  );
                }).toList(),
              ),

              const SizedBox(height: 32),

              // Community guidelines reminder
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primaryContainer.withOpacity(0.3),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    Icon(
                      Icons.favorite,
                      color: theme.colorScheme.primary,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Remember: This is a safe space. Be kind, supportive, and respectful of everyone\'s journey.',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurface,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Provider for creating a post
final createPostProvider = NotifierProvider<CreatePostNotifier, AsyncValue<void>>(
  CreatePostNotifier.new,
);

class CreatePostNotifier extends Notifier<AsyncValue<void>> {
  @override
  AsyncValue<void> build() => const AsyncValue.data(null);

  Future<void> createPost({
    required String content,
    String? category,
    List<String>? tags,
    List<String>? mediaUrls,
  }) async {
    state = const AsyncValue.loading();
    try {
      await SupabaseService.createPost(
        content: content,
        category: category,
        tags: tags,
        mediaUrls: mediaUrls,
      );

      // Refresh the feed after creating a post
      ref.read(feedProvider.notifier).refresh();

      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
      rethrow;
    }
  }
}



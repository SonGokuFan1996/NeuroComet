import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../utils/cross_platform_image.dart';
import '../../models/user.dart';
import '../../models/custom_avatar.dart';
import '../../providers/profile_provider.dart';
import '../../services/supabase_service.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../widgets/avatar/custom_avatar_widget.dart';
import '../../l10n/app_localizations.dart';
import 'profile_picture_picker.dart';


/// Screen for editing user profile
class EditProfileScreen extends ConsumerStatefulWidget {
  const EditProfileScreen({super.key});

  @override
  ConsumerState<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends ConsumerState<EditProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _displayNameController = TextEditingController();
  final _usernameController = TextEditingController();
  final _bioController = TextEditingController();

  XFile? _selectedAvatar;
  XFile? _selectedBanner;
  CustomAvatar? _customAvatar;
  bool _useCustomAvatar = false;
  bool _isSaving = false;
  bool _hasChanges = false;

  @override
  void initState() {
    super.initState();
    _loadCurrentProfile();
  }

  void _loadCurrentProfile() {
    final profileAsync = ref.read(currentUserProfileProvider);
    profileAsync.whenData((profile) {
      _displayNameController.text = profile.displayName;
      _usernameController.text = profile.username ?? '';
      _bioController.text = profile.bio ?? '';
    });
  }

  @override
  void dispose() {
    _displayNameController.dispose();
    _usernameController.dispose();
    _bioController.dispose();
    super.dispose();
  }

  void _markChanged() {
    if (!_hasChanges) {
      setState(() => _hasChanges = true);
    }
  }

  Future<void> _pickAvatar() async {
    final profileAsync = ref.read(currentUserProfileProvider);
    User? currentUser;
    profileAsync.whenData((user) => currentUser = user);

    await showProfilePicturePicker(
      context: context,
      currentImageUrl: currentUser?.avatarUrl,
      currentCustomAvatar: _customAvatar,
      onImageSelected: (image) {
        setState(() {
          _selectedAvatar = image;
          _customAvatar = null;
          _useCustomAvatar = false;
          _hasChanges = true;
        });
      },
      onCustomAvatarCreated: (avatar) {
        setState(() {
          _customAvatar = avatar;
          _selectedAvatar = null;
          _useCustomAvatar = true;
          _hasChanges = true;
        });
      },
      onRemove: () {
        setState(() {
          _selectedAvatar = null;
          _customAvatar = null;
          _useCustomAvatar = false;
          _hasChanges = true;
        });
      },
    );
  }

  Future<void> _pickBanner() async {
    final picker = ImagePicker();
    final image = await picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 1920,
      maxHeight: 1080,
      imageQuality: 85,
    );

    if (image != null) {
      setState(() {
        _selectedBanner = image;
        _hasChanges = true;
      });
    }
  }

  Future<void> _saveProfile() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSaving = true);

    try {
      // Upload images to storage if changed
      String? avatarUrl;
      String? bannerUrl;
      String? customAvatarData;

      if (_useCustomAvatar && _customAvatar != null) {
        // Store custom avatar as JSON data
        customAvatarData = _customAvatar!.id;
        avatarUrl = 'custom:${_customAvatar!.id}';
      } else if (_selectedAvatar != null) {
        final bytes = await _selectedAvatar!.readAsBytes();
        final ext = _selectedAvatar!.name.split('.').last;
        final path = 'avatars/${DateTime.now().millisecondsSinceEpoch}.$ext';
        avatarUrl = await SupabaseService.uploadImage(
          bucket: 'profile-media',
          path: path,
          bytes: bytes,
          contentType: 'image/$ext',
        );
      }

      if (_selectedBanner != null) {
        final bytes = await _selectedBanner!.readAsBytes();
        final ext = _selectedBanner!.name.split('.').last;
        final path = 'banners/${DateTime.now().millisecondsSinceEpoch}.$ext';
        bannerUrl = await SupabaseService.uploadImage(
          bucket: 'profile-media',
          path: path,
          bytes: bytes,
          contentType: 'image/$ext',
        );
      }

      await ref.read(editProfileProvider.notifier).updateProfile(
        displayName: _displayNameController.text.trim(),
        username: _usernameController.text.trim().isEmpty
            ? null
            : _usernameController.text.trim(),
        bio: _bioController.text.trim().isEmpty
            ? null
            : _bioController.text.trim(),
        avatarUrl: avatarUrl,
        bannerUrl: bannerUrl,
        customAvatarData: customAvatarData,
      );

      // Save custom avatar to provider for app-wide use
      if (_useCustomAvatar && _customAvatar != null) {
        ref.read(customAvatarProvider.notifier).setAvatar(_customAvatar!);
      } else {
        ref.read(customAvatarProvider.notifier).clearAvatar();
      }

      if (mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Profile updated successfully!'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to update profile: $e'),
            behavior: SnackBarBehavior.floating,
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSaving = false);
      }
    }
  }

  Future<bool> _onWillPop() async {
    if (!_hasChanges) return true;

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Discard changes?'),
        content: const Text('You have unsaved changes. Are you sure you want to leave?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Keep Editing'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Discard'),
          ),
        ],
      ),
    );

    return result ?? false;
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final profileState = ref.watch(currentUserProfileProvider);

    return PopScope(
      canPop: !_hasChanges,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        final shouldPop = await _onWillPop();
        if (shouldPop && context.mounted) {
          Navigator.pop(context);
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(l10n.edit),
          leading: IconButton(
            icon: const Icon(Icons.close),
            onPressed: () async {
              if (await _onWillPop()) {
                if (context.mounted) Navigator.pop(context);
              }
            },
          ),
          actions: [
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: FilledButton(
                onPressed: _isSaving || !_hasChanges ? null : _saveProfile,
                child: _isSaving
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Text(l10n.save),
              ),
            ),
          ],
        ),
        body: profileState.when(
          loading: () => const NeuroLoading(),
          error: (error, stack) => Center(child: Text('Error: $error')),
          data: (user) => _buildForm(user),
        ),
      ),
    );
  }

  Widget _buildForm(User? user) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Form(
      key: _formKey,
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Banner image
            GestureDetector(
              onTap: _pickBanner,
              child: Stack(
                children: [
                  Container(
                    height: 150,
                    width: double.infinity,
                    decoration: BoxDecoration(
                      color: theme.colorScheme.primaryContainer,
                    ),
                    child: _selectedBanner != null
                        ? buildXFileImage(
                            _selectedBanner!,
                            fit: BoxFit.cover,
                          )
                        : user?.bannerUrl != null
                            ? Image.network(
                                user!.bannerUrl!,
                                fit: BoxFit.cover,
                                errorBuilder: (_, __, ___) => _buildBannerPlaceholder(theme),
                              )
                            : _buildBannerPlaceholder(theme),
                  ),
                  Positioned(
                    bottom: 8,
                    right: 8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.black54,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: const Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.camera_alt, size: 16, color: Colors.white),
                          SizedBox(width: 4),
                          Text(
                            'Change Banner',
                            style: TextStyle(color: Colors.white, fontSize: 12),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),

            // Avatar
            Transform.translate(
              offset: const Offset(16, -40),
              child: GestureDetector(
                onTap: _pickAvatar,
                child: Stack(
                  children: [
                    Container(
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: theme.scaffoldBackgroundColor,
                          width: 4,
                        ),
                      ),
                      child: _buildAvatarPreview(user, theme),
                    ),
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: theme.colorScheme.primary,
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: theme.scaffoldBackgroundColor,
                            width: 2,
                          ),
                        ),
                        child: const Icon(
                          Icons.camera_alt,
                          size: 16,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // Form fields
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Display name
                  TextFormField(
                    controller: _displayNameController,
                    decoration: InputDecoration(
                      labelText: l10n.displayName,
                      hintText: 'Enter your display name',
                    ),
                    maxLength: 50,
                    validator: (value) {
                      if (value == null || value.trim().isEmpty) {
                        return 'Display name is required';
                      }
                      if (value.trim().length < 2) {
                        return 'Display name must be at least 2 characters';
                      }
                      return null;
                    },
                    onChanged: (_) => _markChanged(),
                  ),

                  const SizedBox(height: 16),

                  // Username
                  TextFormField(
                    controller: _usernameController,
                    decoration: InputDecoration(
                      labelText: l10n.username,
                      hintText: 'Choose a unique username',
                      prefixText: '@',
                    ),
                    maxLength: 30,
                    validator: (value) {
                      if (value != null && value.isNotEmpty) {
                        final regex = RegExp(r'^[a-zA-Z0-9_]+$');
                        if (!regex.hasMatch(value)) {
                          return 'Only letters, numbers, and underscores allowed';
                        }
                        if (value.length < 3) {
                          return 'Username must be at least 3 characters';
                        }
                      }
                      return null;
                    },
                    onChanged: (_) => _markChanged(),
                  ),

                  const SizedBox(height: 16),

                  // Bio
                  TextFormField(
                    controller: _bioController,
                    decoration: InputDecoration(
                      labelText: l10n.bio,
                      hintText: 'Tell us about yourself...',
                      alignLabelWithHint: true,
                    ),
                    maxLines: 4,
                    maxLength: 300,
                    onChanged: (_) => _markChanged(),
                  ),

                  const SizedBox(height: 24),

                  // Tips for profile
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.surfaceContainerHighest.withOpacity(0.5),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(
                              Icons.tips_and_updates,
                              color: theme.colorScheme.primary,
                              size: 20,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              'Profile Tips',
                              style: theme.textTheme.titleSmall,
                            ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        _buildTip('Use a friendly display name that you\'re comfortable with'),
                        _buildTip('Your bio is a great place to share your interests'),
                        _buildTip('You can mention your neurodivergent identity if you want'),
                        _buildTip('A profile picture helps others recognize you'),
                      ],
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

  Widget _buildAvatarPreview(User? user, ThemeData theme) {
    // Priority: 1. Custom avatar, 2. Selected image, 3. User's existing avatar
    if (_useCustomAvatar && _customAvatar != null) {
      return CustomAvatarWidget(
        avatar: _customAvatar!,
        size: 100,
      );
    }

    if (_selectedAvatar != null) {
      return ClipOval(
        child: buildXFileImage(
          _selectedAvatar!,
          width: 100,
          height: 100,
          fit: BoxFit.cover,
        ),
      );
    }

    return NeuroAvatar(
      imageUrl: user?.avatarUrl,
      name: user?.displayName ?? 'User',
      size: 100,
    );
  }

  Widget _buildBannerPlaceholder(ThemeData theme) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            theme.colorScheme.primary.withOpacity(0.5),
            theme.colorScheme.secondary.withOpacity(0.5),
          ],
        ),
      ),
      child: const Center(
        child: Icon(
          Icons.panorama,
          size: 40,
          color: Colors.white54,
        ),
      ),
    );
  }

  Widget _buildTip(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text('• ', style: TextStyle(fontSize: 16)),
          Expanded(
            child: Text(
              text,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ),
        ],
      ),
    );
  }
}

/// Provider for editing profile
final editProfileProvider = NotifierProvider<EditProfileNotifier, AsyncValue<void>>(
  EditProfileNotifier.new,
);

class EditProfileNotifier extends Notifier<AsyncValue<void>> {
  @override
  AsyncValue<void> build() => const AsyncValue.data(null);

  Future<void> updateProfile({
    required String displayName,
    String? username,
    String? bio,
    String? avatarUrl,
    String? bannerUrl,
    String? customAvatarData,
  }) async {
    state = const AsyncValue.loading();
    try {
      // In a real app, call the Supabase service to update profile
      // If customAvatarData is set, save it to user preferences or profile
      await Future.delayed(const Duration(milliseconds: 500));

      // Invalidate the current user profile to refetch
      ref.invalidate(currentUserProfileProvider);

      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
      rethrow;
    }
  }
}



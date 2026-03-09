import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../models/custom_avatar.dart';
import '../../widgets/avatar/custom_avatar_widget.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../core/theme/app_colors.dart';
import '../../l10n/app_localizations.dart';
import 'avatar_maker_screen.dart';

/// A bottom sheet that provides options for picking or creating a profile picture
class ProfilePicturePickerSheet extends ConsumerStatefulWidget {
  final String? currentImageUrl;
  final CustomAvatar? currentCustomAvatar;
  final Function(XFile image)? onImageSelected;
  final Function(CustomAvatar avatar)? onCustomAvatarCreated;
  final VoidCallback? onRemove;

  const ProfilePicturePickerSheet({
    super.key,
    this.currentImageUrl,
    this.currentCustomAvatar,
    this.onImageSelected,
    this.onCustomAvatarCreated,
    this.onRemove,
  });

  @override
  ConsumerState<ProfilePicturePickerSheet> createState() =>
      _ProfilePicturePickerSheetState();
}

class _ProfilePicturePickerSheetState
    extends ConsumerState<ProfilePicturePickerSheet> {
  final ImagePicker _picker = ImagePicker();

  Future<void> _pickFromGallery() async {
    final image = await _picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 512,
      maxHeight: 512,
      imageQuality: 90,
    );

    if (image != null && mounted) {
      widget.onImageSelected?.call(image);
      Navigator.pop(context);
    }
  }

  Future<void> _pickFromCamera() async {
    final image = await _picker.pickImage(
      source: ImageSource.camera,
      maxWidth: 512,
      maxHeight: 512,
      imageQuality: 90,
      preferredCameraDevice: CameraDevice.front,
    );

    if (image != null && mounted) {
      widget.onImageSelected?.call(image);
      Navigator.pop(context);
    }
  }

  void _openAvatarMaker() {
    Navigator.pop(context);
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => AvatarMakerScreen(
          initialAvatar: widget.currentCustomAvatar,
          onSave: (avatar) {
            widget.onCustomAvatarCreated?.call(avatar);
          },
        ),
      ),
    );
  }

  void _removeProfilePicture() {
    widget.onRemove?.call();
    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Handle bar
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.outline.withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Title
            Text(
              l10n.get('profilePictureTitle'),
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              l10n.get('profilePictureSubtitle'),
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurface.withValues(alpha: 0.7),
              ),
            ),
            const SizedBox(height: 24),

            // Current preview (if any)
            if (widget.currentImageUrl != null ||
                widget.currentCustomAvatar != null) ...[
              Center(
                child: widget.currentCustomAvatar != null
                    ? CustomAvatarWidget(
                        avatar: widget.currentCustomAvatar!,
                        size: 80,
                        showBorder: true,
                        borderColor: theme.colorScheme.outline,
                      )
                    : NeuroAvatar(
                        imageUrl: widget.currentImageUrl,
                        size: 80,
                        showBorder: true,
                        borderColor: theme.colorScheme.outline,
                      ),
              ),
              const SizedBox(height: 24),
            ],

            // Options
            _buildOption(
              icon: Icons.photo_library,
              iconColor: AppColors.primaryPurple,
              title: l10n.get('chooseFromGallery'),
              subtitle: l10n.get('pickExistingPhoto'),
              onTap: _pickFromGallery,
            ),
            const SizedBox(height: 12),

            _buildOption(
              icon: Icons.camera_alt,
              iconColor: AppColors.secondaryTeal,
              title: l10n.get('takePhoto'),
              subtitle: l10n.get('useYourCamera'),
              onTap: _pickFromCamera,
            ),
            const SizedBox(height: 12),

            _buildOption(
              icon: Icons.face,
              iconColor: AppColors.accentOrange,
              title: l10n.get('createAvatar'),
              subtitle: l10n.get('designCustomAvatar'),
              onTap: _openAvatarMaker,
              highlighted: true,
            ),

            if (widget.currentImageUrl != null ||
                widget.currentCustomAvatar != null) ...[
              const SizedBox(height: 12),
              _buildOption(
                icon: Icons.delete_outline,
                iconColor: theme.colorScheme.error,
                title: l10n.get('removePicture'),
                subtitle: l10n.get('useDefaultInitialsAvatar'),
                onTap: _removeProfilePicture,
              ),
            ],

            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Widget _buildOption({
    required IconData icon,
    required Color iconColor,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
    bool highlighted = false,
  }) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: highlighted
                ? iconColor.withValues(alpha: 0.5)
                : theme.colorScheme.outline.withValues(alpha: 0.2),
            width: highlighted ? 2 : 1,
          ),
          color: highlighted ? iconColor.withValues(alpha: 0.05) : null,
        ),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: iconColor.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                icon,
                color: iconColor,
                size: 24,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                    ),
                  ),
                ],
              ),
            ),
            Icon(
              Icons.chevron_right,
              color: theme.colorScheme.onSurface.withValues(alpha: 0.4),
            ),
          ],
        ),
      ),
    );
  }
}

/// Shows the profile picture picker bottom sheet
Future<void> showProfilePicturePicker({
  required BuildContext context,
  String? currentImageUrl,
  CustomAvatar? currentCustomAvatar,
  Function(XFile image)? onImageSelected,
  Function(CustomAvatar avatar)? onCustomAvatarCreated,
  VoidCallback? onRemove,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (context) => ProfilePicturePickerSheet(
      currentImageUrl: currentImageUrl,
      currentCustomAvatar: currentCustomAvatar,
      onImageSelected: onImageSelected,
      onCustomAvatarCreated: onCustomAvatarCreated,
      onRemove: onRemove,
    ),
  );
}

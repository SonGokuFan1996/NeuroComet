import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../core/theme/app_colors.dart';
import '../../models/custom_avatar.dart';
import '../avatar/custom_avatar_widget.dart';

/// Custom avatar widget with neurodivergent-friendly design
/// Shows initials when image is not available
/// Supports both image URLs and custom-designed avatars
class NeuroAvatar extends StatelessWidget {
  final String? imageUrl;
  final String? name;
  final double size;
  final Color? backgroundColor;
  final Color? foregroundColor;
  final bool showBorder;
  final Color? borderColor;
  final double borderWidth;
  final bool isOnline;
  final VoidCallback? onTap;
  final CustomAvatar? customAvatar;

  const NeuroAvatar({
    super.key,
    this.imageUrl,
    this.name,
    this.size = 40,
    this.backgroundColor,
    this.foregroundColor,
    this.showBorder = false,
    this.borderColor,
    this.borderWidth = 2,
    this.isOnline = false,
    this.onTap,
    this.customAvatar,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bgColor = backgroundColor ?? _generateColor(name ?? '');
    final fgColor = foregroundColor ?? Colors.white;

    Widget avatar;

    // Priority: 1. Custom avatar, 2. Image URL, 3. Initials placeholder
    if (customAvatar != null) {
      avatar = Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: showBorder
              ? Border.all(
                  color: borderColor ?? theme.colorScheme.primary,
                  width: borderWidth,
                )
              : null,
        ),
        child: CustomAvatarWidget(
          avatar: customAvatar!,
          size: size,
        ),
      );
    } else {
      avatar = Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: showBorder
              ? Border.all(
                  color: borderColor ?? theme.colorScheme.primary,
                  width: borderWidth,
                )
              : null,
        ),
        child: ClipOval(
          child: imageUrl != null && imageUrl!.isNotEmpty
              ? CachedNetworkImage(
                  imageUrl: imageUrl!,
                  width: size,
                  height: size,
                  fit: BoxFit.cover,
                  placeholder: (context, url) => _buildPlaceholder(bgColor, fgColor),
                  errorWidget: (context, url, error) => _buildPlaceholder(bgColor, fgColor),
                )
              : _buildPlaceholder(bgColor, fgColor),
        ),
      );
    }

    if (isOnline) {
      avatar = Stack(
        children: [
          avatar,
          Positioned(
            right: 0,
            bottom: 0,
            child: Container(
              width: size * 0.3,
              height: size * 0.3,
              decoration: BoxDecoration(
                color: AppColors.success,
                shape: BoxShape.circle,
                border: Border.all(
                  color: theme.scaffoldBackgroundColor,
                  width: 2,
                ),
              ),
            ),
          ),
        ],
      );
    }

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: avatar,
      );
    }

    return avatar;
  }

  Widget _buildPlaceholder(Color bgColor, Color fgColor) {
    return Builder(
      builder: (context) {
        final theme = Theme.of(context);
        // Use theme's primaryContainer and onPrimaryContainer for proper contrast
        final containerColor = backgroundColor ?? theme.colorScheme.primaryContainer;
        final textColor = foregroundColor ?? theme.colorScheme.onPrimaryContainer;

        return Container(
          width: size,
          height: size,
          color: containerColor,
          alignment: Alignment.center,
          child: Text(
            _getInitials(name ?? '?'),
            style: TextStyle(
              color: textColor,
              fontSize: size * 0.4,
              fontWeight: FontWeight.bold,
            ),
          ),
        );
      },
    );
  }

  String _getInitials(String name) {
    if (name.isEmpty) return '?';
    final parts = name.trim().split(' ');
    if (parts.length == 1) {
      return parts[0][0].toUpperCase();
    }
    return '${parts[0][0]}${parts[parts.length - 1][0]}'.toUpperCase();
  }

  Color _generateColor(String name) {
    if (name.isEmpty) return AppColors.primaryPurple;

    final colors = [
      AppColors.primaryPurple,
      AppColors.secondaryTeal,
      AppColors.accentOrange,
      AppColors.calmBlue,
      AppColors.calmGreen,
      AppColors.calmPink,
      AppColors.calmLavender,
      AppColors.categoryADHD,
      AppColors.categoryAutism,
      AppColors.categoryDyslexia,
    ];

    final hash = name.codeUnits.fold<int>(0, (prev, curr) => prev + curr);
    return colors[hash % colors.length];
  }
}

/// Group avatar for showing multiple users
class NeuroGroupAvatar extends StatelessWidget {
  final List<String?> imageUrls;
  final List<String?> names;
  final double size;

  const NeuroGroupAvatar({
    super.key,
    required this.imageUrls,
    required this.names,
    this.size = 48,
  });

  @override
  Widget build(BuildContext context) {
    final displayCount = imageUrls.length.clamp(0, 3);
    final avatarSize = size * 0.7;

    return SizedBox(
      width: size,
      height: size,
      child: Stack(
        children: List.generate(displayCount, (index) {
          final offset = index * (size * 0.2);
          return Positioned(
            left: offset,
            top: offset,
            child: NeuroAvatar(
              imageUrl: imageUrls.length > index ? imageUrls[index] : null,
              name: names.length > index ? names[index] : null,
              size: avatarSize,
              showBorder: true,
              borderColor: Theme.of(context).scaffoldBackgroundColor,
            ),
          );
        }),
      ),
    );
  }
}


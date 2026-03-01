import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

/// Cache for loaded image bytes to prevent re-loading
final Map<String, Uint8List> _imageCache = {};

/// A widget that displays an XFile image on any platform (web, mobile, desktop)
/// Uses bytes-based approach for cross-platform compatibility with caching
class CrossPlatformImage extends StatefulWidget {
  final XFile xFile;
  final double? width;
  final double? height;
  final BoxFit fit;
  final Widget Function(BuildContext, Object, StackTrace?)? errorBuilder;
  final Widget? placeholder;
  final bool enableCache;

  const CrossPlatformImage({
    super.key,
    required this.xFile,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.errorBuilder,
    this.placeholder,
    this.enableCache = true,
  });

  @override
  State<CrossPlatformImage> createState() => _CrossPlatformImageState();
}

class _CrossPlatformImageState extends State<CrossPlatformImage> {
  Uint8List? _imageBytes;
  bool _loading = true;
  Object? _error;

  @override
  void initState() {
    super.initState();
    _loadImage();
  }

  @override
  void didUpdateWidget(CrossPlatformImage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.xFile.path != widget.xFile.path) {
      _loadImage();
    }
  }

  Future<void> _loadImage() async {
    final cacheKey = widget.xFile.path;

    // Check cache first
    if (widget.enableCache && _imageCache.containsKey(cacheKey)) {
      if (mounted) {
        setState(() {
          _imageBytes = _imageCache[cacheKey];
          _loading = false;
        });
      }
      return;
    }

    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final bytes = await widget.xFile.readAsBytes();

      // Cache the bytes
      if (widget.enableCache) {
        _imageCache[cacheKey] = bytes;

        // Limit cache size to 50 images
        if (_imageCache.length > 50) {
          _imageCache.remove(_imageCache.keys.first);
        }
      }

      if (mounted) {
        setState(() {
          _imageBytes = bytes;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e;
          _loading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return widget.placeholder ??
          SizedBox(
            width: widget.width,
            height: widget.height,
            child: const Center(
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          );
    }

    if (_error != null || _imageBytes == null) {
      if (widget.errorBuilder != null) {
        return widget.errorBuilder!(context, _error ?? 'Unknown error', null);
      }
      return _buildErrorPlaceholder(context);
    }

    return ClipRRect(
      borderRadius: BorderRadius.circular(0),
      child: Image.memory(
        _imageBytes!,
        width: widget.width,
        height: widget.height,
        fit: widget.fit,
        errorBuilder: widget.errorBuilder ??
            (context, error, stackTrace) => _buildErrorPlaceholder(context),
        // Optimize for web
        cacheWidth: widget.width?.toInt(),
        cacheHeight: widget.height?.toInt(),
      ),
    );
  }

  Widget _buildErrorPlaceholder(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      width: widget.width,
      height: widget.height,
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Icon(
        Icons.image_not_supported_outlined,
        color: theme.colorScheme.outline,
        size: 24,
      ),
    );
  }
}

/// Helper function to build image from XFile that works on web and native
Widget buildXFileImage(
  XFile file, {
  BoxFit fit = BoxFit.cover,
  double? width,
  double? height,
  Widget? placeholder,
  bool enableCache = true,
}) {
  return CrossPlatformImage(
    xFile: file,
    fit: fit,
    width: width,
    height: height,
    placeholder: placeholder,
    enableCache: enableCache,
  );
}

/// Clear the image cache
void clearImageCache() {
  _imageCache.clear();
}

/// A network image widget optimized for web with proper loading states
class WebOptimizedImage extends StatelessWidget {
  final String imageUrl;
  final double? width;
  final double? height;
  final BoxFit fit;
  final BorderRadius? borderRadius;
  final Widget? placeholder;
  final Widget? errorWidget;

  const WebOptimizedImage({
    super.key,
    required this.imageUrl,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.borderRadius,
    this.placeholder,
    this.errorWidget,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    Widget image = Image.network(
      imageUrl,
      width: width,
      height: height,
      fit: fit,
      cacheWidth: width?.toInt(),
      cacheHeight: height?.toInt(),
      loadingBuilder: (context, child, loadingProgress) {
        if (loadingProgress == null) return child;
        return placeholder ??
            Container(
              width: width,
              height: height,
              color: theme.colorScheme.surfaceContainerHighest,
              child: Center(
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  value: loadingProgress.expectedTotalBytes != null
                      ? loadingProgress.cumulativeBytesLoaded /
                          loadingProgress.expectedTotalBytes!
                      : null,
                ),
              ),
            );
      },
      errorBuilder: (context, error, stackTrace) {
        return errorWidget ??
            Container(
              width: width,
              height: height,
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: borderRadius,
              ),
              child: Icon(
                Icons.image_not_supported_outlined,
                color: theme.colorScheme.outline,
              ),
            );
      },
    );

    if (borderRadius != null) {
      image = ClipRRect(
        borderRadius: borderRadius!,
        child: image,
      );
    }

    return image;
  }
}

/// Avatar widget that works on all platforms
class CrossPlatformAvatar extends StatelessWidget {
  final String? imageUrl;
  final XFile? imageFile;
  final String? fallbackText;
  final double size;
  final Color? backgroundColor;

  const CrossPlatformAvatar({
    super.key,
    this.imageUrl,
    this.imageFile,
    this.fallbackText,
    this.size = 40,
    this.backgroundColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bgColor = backgroundColor ?? theme.colorScheme.primaryContainer;

    if (imageFile != null) {
      return ClipOval(
        child: CrossPlatformImage(
          xFile: imageFile!,
          width: size,
          height: size,
          fit: BoxFit.cover,
        ),
      );
    }

    if (imageUrl != null && imageUrl!.isNotEmpty) {
      return ClipOval(
        child: WebOptimizedImage(
          imageUrl: imageUrl!,
          width: size,
          height: size,
          fit: BoxFit.cover,
          placeholder: _buildFallback(context, bgColor),
          errorWidget: _buildFallback(context, bgColor),
        ),
      );
    }

    return _buildFallback(context, bgColor);
  }

  Widget _buildFallback(BuildContext context, Color bgColor) {
    final theme = Theme.of(context);
    final initials = _getInitials(fallbackText ?? '?');

    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: bgColor,
        shape: BoxShape.circle,
      ),
      child: Center(
        child: Text(
          initials,
          style: theme.textTheme.titleMedium?.copyWith(
            color: theme.colorScheme.onPrimaryContainer,
            fontWeight: FontWeight.w600,
            fontSize: size * 0.4,
          ),
        ),
      ),
    );
  }

  String _getInitials(String name) {
    if (name.isEmpty) return '?';
    final parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return '${parts[0][0]}${parts[1][0]}'.toUpperCase();
    }
    return name[0].toUpperCase();
  }
}


import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// Error boundary widget that catches and displays errors gracefully
class WebErrorBoundary extends StatefulWidget {
  final Widget child;
  final Widget Function(Object error, StackTrace? stackTrace)? errorBuilder;
  final VoidCallback? onRetry;

  const WebErrorBoundary({
    super.key,
    required this.child,
    this.errorBuilder,
    this.onRetry,
  });

  @override
  State<WebErrorBoundary> createState() => _WebErrorBoundaryState();
}

class _WebErrorBoundaryState extends State<WebErrorBoundary> {
  Object? _error;
  StackTrace? _stackTrace;


  void _retry() {
    setState(() {
      _error = null;
      _stackTrace = null;
    });
    widget.onRetry?.call();
  }

  @override
  Widget build(BuildContext context) {
    if (_error != null) {
      if (widget.errorBuilder != null) {
        return widget.errorBuilder!(_error!, _stackTrace);
      }
      return _DefaultErrorWidget(
        error: _error!,
        onRetry: _retry,
      );
    }

    return widget.child;
  }
}

/// Default error display widget
class _DefaultErrorWidget extends StatelessWidget {
  final Object error;
  final VoidCallback? onRetry;

  const _DefaultErrorWidget({
    required this.error,
    this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline_rounded,
              size: 64,
              color: theme.colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              'Something went wrong',
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              kDebugMode ? error.toString() : 'Please try again later',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            if (onRetry != null) ...[
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: onRetry,
                icon: const Icon(Icons.refresh),
                label: const Text('Try Again'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

/// Enhanced loading widget with different styles
class WebLoading extends StatelessWidget {
  final LoadingStyle style;
  final String? message;
  final double size;
  final Color? color;

  const WebLoading({
    super.key,
    this.style = LoadingStyle.circular,
    this.message,
    this.size = 40,
    this.color,
  });

  /// Creates a fullscreen loading overlay
  factory WebLoading.fullscreen({String? message}) {
    return WebLoading(
      style: LoadingStyle.fullscreen,
      message: message,
    );
  }

  /// Creates a small inline loading indicator
  factory WebLoading.inline({double size = 20}) {
    return WebLoading(
      style: LoadingStyle.inline,
      size: size,
    );
  }

  /// Creates a loading indicator for cards/content areas
  factory WebLoading.card({String? message}) {
    return WebLoading(
      style: LoadingStyle.card,
      message: message,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final loadingColor = color ?? theme.colorScheme.primary;

    switch (style) {
      case LoadingStyle.circular:
        return _buildCircular(loadingColor);
      case LoadingStyle.inline:
        return _buildInline(loadingColor);
      case LoadingStyle.fullscreen:
        return _buildFullscreen(context, loadingColor);
      case LoadingStyle.card:
        return _buildCard(context, loadingColor);
      case LoadingStyle.skeleton:
        return _buildSkeleton(context);
    }
  }

  Widget _buildCircular(Color color) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: size,
            height: size,
            child: CircularProgressIndicator(
              strokeWidth: 3,
              valueColor: AlwaysStoppedAnimation(color),
            ),
          ),
          if (message != null) ...[
            const SizedBox(height: 16),
            Text(message!),
          ],
        ],
      ),
    );
  }

  Widget _buildInline(Color color) {
    return SizedBox(
      width: size,
      height: size,
      child: CircularProgressIndicator(
        strokeWidth: 2,
        valueColor: AlwaysStoppedAnimation(color),
      ),
    );
  }

  Widget _buildFullscreen(BuildContext context, Color color) {
    final theme = Theme.of(context);

    return Container(
      color: theme.colorScheme.surface.withOpacity(0.9),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              width: 60,
              height: 60,
              child: CircularProgressIndicator(
                strokeWidth: 4,
                valueColor: AlwaysStoppedAnimation(color),
              ),
            ),
            if (message != null) ...[
              const SizedBox(height: 24),
              Text(
                message!,
                style: theme.textTheme.bodyLarge,
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildCard(BuildContext context, Color color) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: 40,
            height: 40,
            child: CircularProgressIndicator(
              strokeWidth: 3,
              valueColor: AlwaysStoppedAnimation(color),
            ),
          ),
          if (message != null) ...[
            const SizedBox(height: 16),
            Text(
              message!,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSkeleton(BuildContext context) {
    return const SkeletonLoader();
  }
}

/// Loading style variants
enum LoadingStyle {
  circular,
  inline,
  fullscreen,
  card,
  skeleton,
}

/// Skeleton loading placeholder
class SkeletonLoader extends StatefulWidget {
  final double? width;
  final double? height;
  final BorderRadius? borderRadius;

  const SkeletonLoader({
    super.key,
    this.width,
    this.height = 20,
    this.borderRadius,
  });

  /// Creates a circular skeleton (for avatars)
  factory SkeletonLoader.circle({double size = 40}) {
    return SkeletonLoader(
      width: size,
      height: size,
      borderRadius: BorderRadius.circular(size / 2),
    );
  }

  /// Creates a text-like skeleton
  factory SkeletonLoader.text({double? width, double height = 16}) {
    return SkeletonLoader(
      width: width,
      height: height,
      borderRadius: BorderRadius.circular(4),
    );
  }

  /// Creates a card-like skeleton
  factory SkeletonLoader.card({double? height = 120}) {
    return SkeletonLoader(
      height: height,
      borderRadius: BorderRadius.circular(12),
    );
  }

  @override
  State<SkeletonLoader> createState() => _SkeletonLoaderState();
}

class _SkeletonLoaderState extends State<SkeletonLoader>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat();

    _animation = Tween<double>(begin: -1.0, end: 2.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOutSine),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final baseColor = theme.colorScheme.surfaceContainerHighest;
    final highlightColor = theme.colorScheme.surfaceContainerLow;

    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: widget.width,
          height: widget.height,
          decoration: BoxDecoration(
            borderRadius: widget.borderRadius ?? BorderRadius.circular(8),
            gradient: LinearGradient(
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
              colors: [baseColor, highlightColor, baseColor],
              stops: [
                (_animation.value - 0.3).clamp(0.0, 1.0),
                _animation.value.clamp(0.0, 1.0),
                (_animation.value + 0.3).clamp(0.0, 1.0),
              ],
            ),
          ),
        );
      },
    );
  }
}

/// Empty state widget for when there's no content
class WebEmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? action;

  const WebEmptyState({
    super.key,
    required this.icon,
    required this.title,
    this.subtitle,
    this.action,
  });

  /// Creates an empty state for a search with no results
  factory WebEmptyState.noResults({String? query}) {
    return WebEmptyState(
      icon: Icons.search_off,
      title: 'No results found',
      subtitle: query != null ? 'Try searching for something else' : null,
    );
  }

  /// Creates an empty state for an empty list
  factory WebEmptyState.emptyList({
    required String itemType,
    Widget? action,
  }) {
    return WebEmptyState(
      icon: Icons.inbox_outlined,
      title: 'No $itemType yet',
      subtitle: 'Check back later or create something new',
      action: action,
    );
  }

  /// Creates an empty state for offline mode
  factory WebEmptyState.offline({VoidCallback? onRetry}) {
    return WebEmptyState(
      icon: Icons.cloud_off,
      title: 'You\'re offline',
      subtitle: 'Connect to the internet to see content',
      action: onRetry != null
          ? FilledButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('Retry'),
            )
          : null,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 64,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 16),
            Text(
              title,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.center,
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (action != null) ...[
              const SizedBox(height: 24),
              action!,
            ],
          ],
        ),
      ),
    );
  }
}


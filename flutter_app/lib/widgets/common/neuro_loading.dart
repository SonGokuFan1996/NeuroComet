import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/theme_provider.dart';

class NeuroLoading extends ConsumerStatefulWidget {
  final String? message;
  final double size;

  const NeuroLoading({
    super.key,
    this.message,
    this.size = 48,
  });

  @override
  ConsumerState<NeuroLoading> createState() => _NeuroLoadingState();
}

class _NeuroLoadingState extends ConsumerState<NeuroLoading>
    with SingleTickerProviderStateMixin {
  AnimationController? _controller;
  Animation<double>? _scaleAnimation;
  Animation<double>? _opacityAnimation;

  @override
  void initState() {
    super.initState();
  }

  void _initAnimations() {
    if (_controller != null) return;

    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);

    _scaleAnimation = Tween<double>(begin: 0.8, end: 1.0).animate(
      CurvedAnimation(parent: _controller!, curve: Curves.easeInOut),
    );

    _opacityAnimation = Tween<double>(begin: 0.5, end: 1.0).animate(
      CurvedAnimation(parent: _controller!, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final reducedMotion = ref.watch(reducedMotionProvider);

    // Initialize animations only if needed
    if (!reducedMotion && _controller == null) {
      _initAnimations();
    }

    final loadingIcon = Container(
      width: widget.size,
      height: widget.size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: LinearGradient(
          colors: [
            theme.colorScheme.primary,
            theme.colorScheme.secondary,
          ],
        ),
      ),
      child: Icon(
        Icons.psychology,
        color: theme.colorScheme.onPrimary,
        size: widget.size * 0.5,
      ),
    );

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          reducedMotion || _controller == null
              ? loadingIcon
              : AnimatedBuilder(
                  animation: _controller!,
                  builder: (context, child) {
                    return Transform.scale(
                      scale: _scaleAnimation!.value,
                      child: Opacity(
                        opacity: _opacityAnimation!.value,
                        child: child,
                      ),
                    );
                  },
                  child: loadingIcon,
                ),
          if (widget.message != null) ...[
            const SizedBox(height: 16),
            Text(
              widget.message!,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class NeuroShimmer extends StatefulWidget {
  final double width;
  final double height;
  final double borderRadius;

  const NeuroShimmer({
    super.key,
    required this.width,
    required this.height,
    this.borderRadius = 8,
  });

  @override
  State<NeuroShimmer> createState() => _NeuroShimmerState();
}

class _NeuroShimmerState extends State<NeuroShimmer>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Container(
          width: widget.width,
          height: widget.height,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(widget.borderRadius),
            gradient: LinearGradient(
              begin: Alignment(-1.0 + 2 * _controller.value, 0),
              end: Alignment(-1.0 + 2 * _controller.value + 1, 0),
              colors: [
                theme.colorScheme.surfaceContainerHighest,
                theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
                theme.colorScheme.surfaceContainerHighest,
              ],
            ),
          ),
        );
      },
    );
  }
}

class PostCardSkeleton extends StatelessWidget {
  const PostCardSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const NeuroShimmer(width: 44, height: 44, borderRadius: 22),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: const [
                    NeuroShimmer(width: 120, height: 16),
                    SizedBox(height: 4),
                    NeuroShimmer(width: 80, height: 12),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 12),
            const NeuroShimmer(width: double.infinity, height: 14),
            const SizedBox(height: 8),
            const NeuroShimmer(width: 200, height: 14),
            const SizedBox(height: 12),
            Row(
              children: const [
                NeuroShimmer(width: 60, height: 24),
                SizedBox(width: 16),
                NeuroShimmer(width: 60, height: 24),
                SizedBox(width: 16),
                NeuroShimmer(width: 60, height: 24),
              ],
            ),
          ],
        ),
      ),
    );
  }
}


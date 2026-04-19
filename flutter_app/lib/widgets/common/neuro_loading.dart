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

  @override
  void initState() {
    super.initState();
  }

  void _initAnimations() {
    if (_controller != null) return;

    _controller = AnimationController(
      duration: const Duration(milliseconds: 1200),
      vsync: this,
    )..repeat();
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

    final dotSize = widget.size * 0.22;
    final dotSpacing = widget.size * 0.12;
    final brandColors = [
      theme.colorScheme.primary,
      theme.colorScheme.secondary,
      theme.colorScheme.tertiary,
    ];

    Widget loadingWidget;
    if (reducedMotion || _controller == null) {
      // Static fallback: three colored dots
      loadingWidget = Row(
        mainAxisSize: MainAxisSize.min,
        children: List.generate(3, (i) => Container(
          width: dotSize,
          height: dotSize,
          margin: EdgeInsets.symmetric(horizontal: dotSpacing / 2),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: brandColors[i % brandColors.length],
          ),
        )),
      );
    } else {
      // Animated pulsing dots with staggered phase
      loadingWidget = AnimatedBuilder(
        animation: _controller!,
        builder: (context, _) {
          return Row(
            mainAxisSize: MainAxisSize.min,
            children: List.generate(3, (i) {
              // Stagger each dot by 0.2 of the animation cycle
              final phase = (_controller!.value + i * 0.28) % 1.0;
              // Smooth sine-like pulse: scale 0.6→1.0 and opacity 0.4→1.0
              final t = (phase < 0.5) ? phase * 2 : 2 - phase * 2;
              final ease = Curves.easeInOut.transform(t);
              final scale = 0.6 + 0.4 * ease;
              final opacity = 0.4 + 0.6 * ease;

              return Container(
                margin: EdgeInsets.symmetric(horizontal: dotSpacing / 2),
                child: Transform.scale(
                  scale: scale,
                  child: Opacity(
                    opacity: opacity,
                    child: Container(
                      width: dotSize,
                      height: dotSize,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: brandColors[i % brandColors.length],
                        boxShadow: [
                          BoxShadow(
                            color: brandColors[i % brandColors.length]
                                .withValues(alpha: 0.3 * ease),
                            blurRadius: 6 * ease,
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
          );
        },
      );
    }

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SizedBox(
            height: widget.size,
            child: Center(child: loadingWidget),
          ),
          if (widget.message != null) ...[
            const SizedBox(height: 20),
            Text(
              widget.message!,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
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
      duration: const Duration(milliseconds: 1800),
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
    final base = theme.colorScheme.surfaceContainerHighest;
    final highlight = theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.35);

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        final v = _controller.value;
        return Container(
          width: widget.width,
          height: widget.height,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(widget.borderRadius),
            gradient: LinearGradient(
              begin: Alignment(-1.5 + 3 * v, 0),
              end: Alignment(-0.5 + 3 * v, 0),
              colors: [
                base,
                base.withValues(alpha: base.a * 0.8),
                highlight,
                base.withValues(alpha: base.a * 0.8),
                base,
              ],
              stops: const [0.0, 0.3, 0.5, 0.7, 1.0],
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
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: theme.colorScheme.outlineVariant.withValues(alpha: 0.18),
          width: 0.5,
        ),
        boxShadow: [
          BoxShadow(
            color: theme.colorScheme.shadow.withValues(alpha: 0.08),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
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
                  NeuroShimmer(width: 120, height: 14, borderRadius: 7),
                  SizedBox(height: 6),
                  NeuroShimmer(width: 80, height: 10, borderRadius: 5),
                ],
              ),
              const Spacer(),
              const NeuroShimmer(width: 24, height: 24, borderRadius: 12),
            ],
          ),
          const SizedBox(height: 14),
          const NeuroShimmer(width: double.infinity, height: 14, borderRadius: 7),
          const SizedBox(height: 8),
          const NeuroShimmer(width: 220, height: 14, borderRadius: 7),
          const SizedBox(height: 8),
          const NeuroShimmer(width: 160, height: 14, borderRadius: 7),
          const SizedBox(height: 16),
          Row(
            children: const [
              NeuroShimmer(width: 64, height: 28, borderRadius: 14),
              SizedBox(width: 12),
              NeuroShimmer(width: 64, height: 28, borderRadius: 14),
              SizedBox(width: 12),
              NeuroShimmer(width: 48, height: 28, borderRadius: 14),
              Spacer(),
              NeuroShimmer(width: 28, height: 28, borderRadius: 14),
            ],
          ),
        ],
      ),
    );
  }
}


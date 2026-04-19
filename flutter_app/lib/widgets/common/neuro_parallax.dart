import 'package:flutter/material.dart';

/// Neurodivergent-friendly parallax scroll effect.
/// Mirrors the Kotlin NeuroParallax.kt
///
/// Features:
/// - Subtle, non-distracting motion
/// - Predictable, smooth animations
/// - Optional reduced motion support
/// - Fade on scroll
/// - Scale on scroll
class ParallaxConfig {
  /// How much slower the background moves (0-1)
  final double parallaxRatio;

  /// Maximum parallax offset
  final double maxOffset;

  /// Fade content as it scrolls
  final bool fadeOnScroll;

  /// Subtle scale effect on scroll
  final bool scaleOnScroll;

  /// Respect reduced motion preferences
  final bool reducedMotion;

  const ParallaxConfig({
    this.parallaxRatio = 0.5,
    this.maxOffset = 100.0,
    this.fadeOnScroll = true,
    this.scaleOnScroll = false,
    this.reducedMotion = false,
  });
}

/// Parallax container for scrollable content
class NeuroParallaxContainer extends StatelessWidget {
  final ScrollController scrollController;
  final ParallaxConfig config;
  final Widget backgroundContent;
  final Widget foregroundContent;
  final double backgroundHeight;

  const NeuroParallaxContainer({
    super.key,
    required this.scrollController,
    this.config = const ParallaxConfig(),
    required this.backgroundContent,
    required this.foregroundContent,
    this.backgroundHeight = 300,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: scrollController,
      builder: (context, child) {
        double scrollProgress = 0;
        if (!config.reducedMotion && scrollController.hasClients) {
          scrollProgress =
              (scrollController.offset / 1000.0).clamp(0.0, 1.0);
        }

        final parallaxOffset =
            scrollProgress * config.maxOffset * config.parallaxRatio;
        final fadeAlpha =
            config.fadeOnScroll ? 1.0 - (scrollProgress * 0.3) : 1.0;
        final scale =
            config.scaleOnScroll ? 1.0 + (scrollProgress * 0.05) : 1.0;

        return Stack(
          children: [
            // Background with parallax
            Transform.translate(
              offset: Offset(0, -parallaxOffset),
              child: Transform.scale(
                scale: scale,
                child: Opacity(
                  opacity: fadeAlpha.clamp(0.0, 1.0),
                  child: SizedBox(
                    height: backgroundHeight + config.maxOffset,
                    width: double.infinity,
                    child: backgroundContent,
                  ),
                ),
              ),
            ),
            // Foreground content
            foregroundContent,
          ],
        );
      },
    );
  }
}

/// A simple parallax image header that collapses on scroll
class NeuroParallaxHeader extends StatelessWidget {
  final ScrollController scrollController;
  final String? imageUrl;
  final Widget? child;
  final double height;
  final ParallaxConfig config;
  final List<Color>? gradientOverlayColors;

  const NeuroParallaxHeader({
    super.key,
    required this.scrollController,
    this.imageUrl,
    this.child,
    this.height = 250,
    this.config = const ParallaxConfig(),
    this.gradientOverlayColors,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final overlayColors = gradientOverlayColors ??
        [
          Colors.transparent,
          theme.colorScheme.surface.withValues(alpha: 0.8),
          theme.colorScheme.surface,
        ];

    return AnimatedBuilder(
      animation: scrollController,
      builder: (context, _) {
        double scrollProgress = 0;
        if (!config.reducedMotion && scrollController.hasClients) {
          scrollProgress = (scrollController.offset / height).clamp(0.0, 1.0);
        }

        final parallaxOffset =
            scrollProgress * height * config.parallaxRatio;
        final fadeAlpha =
            config.fadeOnScroll ? 1.0 - (scrollProgress * 0.5) : 1.0;

        return SizedBox(
          height: height,
          child: Stack(
            fit: StackFit.expand,
            children: [
              // Parallax layer
              Transform.translate(
                offset: Offset(0, parallaxOffset * 0.5),
                child: Opacity(
                  opacity: fadeAlpha.clamp(0.0, 1.0),
                  child: child ??
                      (imageUrl != null
                          ? Image.network(
                              imageUrl!,
                              fit: BoxFit.cover,
                              errorBuilder: (_, __, ___) => Container(
                                color: theme.colorScheme.primaryContainer,
                              ),
                            )
                          : Container(
                              color: theme.colorScheme.primaryContainer,
                            )),
                ),
              ),
              // Gradient overlay
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                height: height * 0.6,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: overlayColors,
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// Parallax card that responds to scroll position for list items
class NeuroParallaxCard extends StatelessWidget {
  final Widget child;
  final double scrollOffset;
  final double itemExtent;
  final int index;
  final ParallaxConfig config;

  const NeuroParallaxCard({
    super.key,
    required this.child,
    required this.scrollOffset,
    required this.itemExtent,
    required this.index,
    this.config = const ParallaxConfig(),
  });

  @override
  Widget build(BuildContext context) {
    if (config.reducedMotion) return child;

    final itemOffset = (index * itemExtent) - scrollOffset;
    final viewportHeight = MediaQuery.of(context).size.height;
    final progress = (itemOffset / viewportHeight).clamp(-1.0, 1.0);

    final parallax = progress * config.maxOffset * config.parallaxRatio;

    return Transform.translate(
      offset: Offset(0, parallax * 0.2),
      child: child,
    );
  }
}


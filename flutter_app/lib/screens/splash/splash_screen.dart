import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/splash_configs.dart';
import '../../providers/theme_provider.dart';
import '../../widgets/brand/brand_mark.dart';
import '../../widgets/brand/brand_pill.dart';
import '../../widgets/common/neuro_loading.dart';

/// A calming, neurodivergent-friendly splash screen that adapts to the user's NeuroState.
/// Matches the Android version's design, dimensions, and behavior.
class SplashScreen extends ConsumerStatefulWidget {
  final VoidCallback onComplete;

  const SplashScreen({super.key, required this.onComplete});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with TickerProviderStateMixin {
  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;

  int _messageIndex = 0;
  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 520),
      vsync: this,
    );
    _fadeAnimation = CurvedAnimation(
      parent: _fadeController,
      curve: Curves.fastOutSlowIn,
    );
    _fadeController.forward();
    _startSplashSequence();
  }

  Future<void> _startSplashSequence() async {
    final neuroState = ref.read(neuroStateProvider);
    final config = getSplashConfigForState(neuroState);
    final effectiveDuration = config.durationMs.clamp(2000, 5000);
    final messages = config.messages;

    if (messages.length <= 1) {
      await Future.delayed(Duration(milliseconds: (effectiveDuration - 420).clamp(900, 5000)));
    } else {
      final messageDelay = ((effectiveDuration - 900) ~/ messages.length).clamp(650, 2000);
      for (int i = 0; i < messages.length; i++) {
        if (!mounted) return;
        setState(() => _messageIndex = i);
        await Future.delayed(Duration(milliseconds: messageDelay));
      }
    }

    if (!mounted) return;
    setState(() => _isVisible = false);
    await _fadeController.reverse();
    widget.onComplete();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final neuroState = ref.watch(neuroStateProvider);
    final animationSettings = ref.watch(animationSettingsProvider);
    final config = getSplashConfigForState(neuroState);
    final theme = Theme.of(context);

    final allowLogoMotion = !animationSettings.disableLogo && !animationSettings.disableAll;
    final allowLoadingMotion = !animationSettings.disableLoading && !animationSettings.disableAll;
    final allowTransitions = !animationSettings.disableTransitions && !animationSettings.disableAll;

    final accentColor = _getAccentColor(config.animationStyle, theme.colorScheme);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 560),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const BrandPill(text: 'Loading…'),
                    const SizedBox(height: 12),
                    NeuroCometBrandMark(
                      size: 100, // Exact Android dimension
                      haloColor: theme.colorScheme.primary,
                      accentColor: accentColor,
                      motionEnabled: allowLogoMotion,
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'NeuroComet',
                      style: theme.textTheme.headlineLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.onSurface,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 12),
                    Text(
                      config.tagline,
                      style: theme.textTheme.titleMedium?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 12),
                    SizedBox(
                      height: 48,
                      child: allowTransitions
                          ? AnimatedSwitcher(
                              duration: const Duration(milliseconds: 260),
                              child: Text(
                                config.messages[_messageIndex],
                                key: ValueKey(_messageIndex),
                                style: theme.textTheme.bodyLarge,
                                textAlign: TextAlign.center,
                              ),
                            )
                          : Text(
                              config.messages[_messageIndex],
                              style: theme.textTheme.bodyLarge,
                              textAlign: TextAlign.center,
                            ),
                    ),
                    const SizedBox(height: 12),
                    _SplashMessageIndicators(
                      count: config.messages.length,
                      selectedIndex: _messageIndex,
                      accentColor: accentColor,
                    ),
                    const SizedBox(height: 12),
                    if (allowLoadingMotion)
                      const NeuroLoading(size: 56)
                    else
                      _StaticLoadingIndicator(color: accentColor),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Color _getAccentColor(SplashAnimationStyle style, ColorScheme colors) {
    switch (style) {
      case SplashAnimationStyle.energyBurst:
      case SplashAnimationStyle.creativeSwirl:
      case SplashAnimationStyle.sensorySparkle:
      case SplashAnimationStyle.rainbowSparkle:
        return colors.tertiary;
      case SplashAnimationStyle.routineGrid:
      case SplashAnimationStyle.focusPulse:
      case SplashAnimationStyle.contrastRings:
      case SplashAnimationStyle.patternShapes:
        return colors.secondary;
      default:
        return colors.primary;
    }
  }
}

class _SplashMessageIndicators extends StatelessWidget {
  final int count;
  final int selectedIndex;
  final Color accentColor;

  const _SplashMessageIndicators({
    required this.count,
    required this.selectedIndex,
    required this.accentColor,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        count.clamp(1, 10),
        (index) {
          final selected = index == selectedIndex;
          return AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            margin: const EdgeInsets.symmetric(horizontal: 4),
            height: 6,
            width: selected ? 26 : 10,
            decoration: BoxDecoration(
              color: selected ? accentColor : accentColor.withValues(alpha: 0.22),
              borderRadius: BorderRadius.circular(999),
            ),
          );
        },
      ),
    );
  }
}

class _StaticLoadingIndicator extends StatelessWidget {
  final Color color;

  const _StaticLoadingIndicator({required this.color});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(3, (index) {
        final size = index == 1 ? 10.0 : 8.0;
        return Container(
          margin: const EdgeInsets.symmetric(horizontal: 4),
          width: size,
          height: size,
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.45 + (index * 0.12)),
            shape: BoxShape.circle,
          ),
        );
      }),
    );
  }
}

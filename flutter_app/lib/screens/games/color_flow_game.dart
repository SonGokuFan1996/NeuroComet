import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback that only triggers on supported platforms
void _safeHapticFeedback() {
  if (!kIsWeb) {
    HapticFeedback.lightImpact();
  }
}

/// Color Flow Game - Relaxing color gradients
class ColorFlowGame extends ConsumerStatefulWidget {
  const ColorFlowGame({super.key});

  @override
  ConsumerState<ColorFlowGame> createState() => _ColorFlowGameState();
}

class _ColorFlowGameState extends ConsumerState<ColorFlowGame>
    with TickerProviderStateMixin {
  late AnimationController _controller;
  late List<Color> _currentColors;
  late List<Color> _nextColors;
  final Random _random = Random();

  final List<List<Color>> _colorPalettes = [
    [AppColors.primaryPurple, AppColors.secondaryTeal, AppColors.calmBlue],
    [AppColors.calmPink, AppColors.calmLavender, AppColors.primaryPurple],
    [AppColors.calmGreen, AppColors.secondaryTeal, AppColors.calmBlue],
    [AppColors.accentOrange, AppColors.calmYellow, AppColors.calmPink],
    [AppColors.calmBlue, AppColors.calmLavender, AppColors.calmPink],
    [AppColors.categoryADHD, AppColors.categoryAutism, AppColors.categoryDyslexia],
    [AppColors.categoryAnxiety, AppColors.categoryDepression, AppColors.categoryOCD],
  ];

  @override
  void initState() {
    super.initState();
    _currentColors = _getRandomPalette();
    _nextColors = _getRandomPalette();

    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 8),
    )..addStatusListener((status) {
        if (status == AnimationStatus.completed) {
          setState(() {
            _currentColors = _nextColors;
            _nextColors = _getRandomPalette();
          });
          _controller.forward(from: 0);
        }
      });

    _controller.forward();
  }

  List<Color> _getRandomPalette() {
    return _colorPalettes[_random.nextInt(_colorPalettes.length)];
  }

  void _shuffleColors() {
    _safeHapticFeedback();
    setState(() {
      _currentColors = _nextColors;
      _nextColors = _getRandomPalette();
    });
    _controller.forward(from: 0);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(l10n.gameColorFlow),
        actions: [
          IconButton(
            icon: const Icon(Icons.shuffle),
            onPressed: _shuffleColors,
            tooltip: l10n.get('shuffleColors'),
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => _showTutorial(context),
          ),
        ],
      ),
      body: GestureDetector(
        onTap: _shuffleColors,
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, child) {
            return Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment(
                    cos(_controller.value * 2 * pi),
                    sin(_controller.value * 2 * pi),
                  ),
                  end: Alignment(
                    cos(_controller.value * 2 * pi + pi),
                    sin(_controller.value * 2 * pi + pi),
                  ),
                  colors: [
                    Color.lerp(
                      _currentColors[0],
                      _nextColors[0],
                      _controller.value,
                    )!,
                    Color.lerp(
                      _currentColors[1],
                      _nextColors[1],
                      _controller.value,
                    )!,
                    Color.lerp(
                      _currentColors[2],
                      _nextColors[2],
                      _controller.value,
                    )!,
                  ],
                ),
              ),
              child: Stack(
                children: [
                  // Floating orbs
                  ..._buildFloatingOrbs(),

                  // Center text
                  Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(
                          Icons.gradient,
                          size: 64,
                          color: Colors.white54,
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'Tap anywhere to shuffle',
                          style: TextStyle(
                            color: Colors.white.withAlpha(180),
                            fontSize: 16,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  List<Widget> _buildFloatingOrbs() {
    return List.generate(5, (index) {
      final size = 50.0 + _random.nextDouble() * 100;
      final left = _random.nextDouble() * 300;
      final top = _random.nextDouble() * 600;

      return Positioned(
        left: left,
        top: top,
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, child) {
            return Transform.translate(
              offset: Offset(
                sin(_controller.value * 2 * pi + index) * 20,
                cos(_controller.value * 2 * pi + index) * 20,
              ),
              child: Container(
                width: size,
                height: size,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white.withAlpha(30),
                ),
              ),
            );
          },
        ),
      );
    });
  }

  void _showTutorial(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Text('🎨 '),
            Text('Color Flow'),
          ],
        ),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('• Watch the colors flow and blend'),
            SizedBox(height: 8),
            Text('• Tap anywhere to shuffle to new colors'),
            SizedBox(height: 8),
            Text('• Just relax and enjoy the visual experience'),
            SizedBox(height: 8),
            Text('• No goals - pure relaxation 😌'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(AppLocalizations.of(context)!.get('gotIt')),
          ),
        ],
      ),
    );
  }
}


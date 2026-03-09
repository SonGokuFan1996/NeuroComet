import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback that only triggers on supported platforms
void _safeHapticFeedback() {
  if (!kIsWeb) {
    HapticFeedback.lightImpact();
  }
}

class BubblePopGame extends StatefulWidget {
  const BubblePopGame({super.key});

  @override
  State<BubblePopGame> createState() => _BubblePopGameState();
}

class _BubblePopGameState extends State<BubblePopGame>
    with TickerProviderStateMixin {
  final List<_Bubble> _bubbles = [];
  final Random _random = Random();
  int _score = 0;
  int _combo = 0;
  late AnimationController _spawnController;

  @override
  void initState() {
    super.initState();
    _spawnController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    )..addListener(_spawnBubble);
    _spawnController.repeat();
  }

  @override
  void dispose() {
    _spawnController.dispose();
    for (var bubble in _bubbles) {
      bubble.controller.dispose();
    }
    super.dispose();
  }

  void _spawnBubble() {
    if (_spawnController.value > 0.99) {
      _addBubble();
    }
  }

  void _addBubble() {
    if (!mounted) return;

    final size = MediaQuery.of(context).size;
    final bubbleSize = 40.0 + _random.nextDouble() * 40;

    final controller = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 3000 + _random.nextInt(2000)),
    );

    final bubble = _Bubble(
      id: DateTime.now().millisecondsSinceEpoch,
      x: _random.nextDouble() * (size.width - bubbleSize),
      startY: size.height,
      endY: -bubbleSize,
      size: bubbleSize,
      color: _getRandomColor(),
      controller: controller,
    );

    controller.addStatusListener((status) {
      if (status == AnimationStatus.completed) {
        _removeBubble(bubble.id);
        _combo = 0;
      }
    });

    setState(() {
      _bubbles.add(bubble);
    });

    controller.forward();
  }

  void _removeBubble(int id) {
    if (!mounted) return;
    setState(() {
      _bubbles.removeWhere((b) => b.id == id);
    });
  }

  void _popBubble(_Bubble bubble) {
    _safeHapticFeedback();

    setState(() {
      _combo++;
      _score += _combo;
      _bubbles.removeWhere((b) => b.id == bubble.id);
    });

    bubble.controller.dispose();
  }

  Color _getRandomColor() {
    final colors = [
      Colors.pink.shade300,
      Colors.purple.shade300,
      Colors.blue.shade300,
      Colors.cyan.shade300,
      Colors.teal.shade300,
      Colors.green.shade300,
      Colors.amber.shade300,
      Colors.orange.shade300,
    ];
    return colors[_random.nextInt(colors.length)];
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        title: Text(l10n.gameBubblePop),
        backgroundColor: Colors.transparent,
        elevation: 0,
        actions: [
          Center(
            child: Padding(
              padding: const EdgeInsets.only(right: 16),
              child: Row(
                children: [
                  if (_combo > 1)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 4,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.orange,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        '${_combo}x',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  const SizedBox(width: 8),
                  Text(
                    'Score: $_score',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
      body: Stack(
        children: [
          // Background gradient
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
                  theme.colorScheme.surface,
                ],
              ),
            ),
          ),
          // Bubbles
          ..._bubbles.map((bubble) => _BubbleWidget(
            bubble: bubble,
            onPop: () => _popBubble(bubble),
          )),
        ],
      ),
    );
  }
}

class _Bubble {
  final int id;
  final double x;
  final double startY;
  final double endY;
  final double size;
  final Color color;
  final AnimationController controller;

  _Bubble({
    required this.id,
    required this.x,
    required this.startY,
    required this.endY,
    required this.size,
    required this.color,
    required this.controller,
  });
}

class _BubbleWidget extends StatelessWidget {
  final _Bubble bubble;
  final VoidCallback onPop;

  const _BubbleWidget({
    required this.bubble,
    required this.onPop,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: bubble.controller,
      builder: (context, child) {
        final y = bubble.startY +
            (bubble.endY - bubble.startY) * bubble.controller.value;

        // Slight horizontal wobble
        final wobble = sin(bubble.controller.value * 6 * pi) * 5;

        return Positioned(
          left: bubble.x + wobble,
          top: y,
          child: child!,
        );
      },
      child: GestureDetector(
        onTapDown: (_) => onPop(),
        child: Container(
          width: bubble.size,
          height: bubble.size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              center: const Alignment(-0.3, -0.3),
              colors: [
                bubble.color.withValues(alpha: 0.8),
                bubble.color,
                bubble.color.withValues(alpha: 0.6),
              ],
              stops: const [0.0, 0.5, 1.0],
            ),
            boxShadow: [
              BoxShadow(
                color: bubble.color.withValues(alpha: 0.3),
                blurRadius: 8,
                spreadRadius: 2,
              ),
            ],
          ),
          child: Stack(
            children: [
              // Shine effect
              Positioned(
                left: bubble.size * 0.2,
                top: bubble.size * 0.15,
                child: Container(
                  width: bubble.size * 0.25,
                  height: bubble.size * 0.2,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.5),
                    borderRadius: BorderRadius.circular(bubble.size),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}


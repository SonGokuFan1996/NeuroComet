import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback
void _safeHaptic() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

/// Emotion Garden - Plant flowers representing your emotions
class EmotionGardenGame extends StatefulWidget {
  const EmotionGardenGame({super.key});

  @override
  State<EmotionGardenGame> createState() => _EmotionGardenGameState();
}

class _EmotionGardenGameState extends State<EmotionGardenGame>
    with TickerProviderStateMixin {
  final List<EmotionFlower> _flowers = [];
  Emotion _selectedEmotion = Emotion.happy;
  final Random _random = Random();
  AnimationController? _growthController;
  int _growingFlowerIndex = -1;

  @override
  void dispose() {
    _growthController?.dispose();
    super.dispose();
  }

  void _plantFlower(Offset position, Size size) {
    _safeHaptic();

    // Normalize position
    final normalizedX = position.dx / size.width;
    final normalizedY = position.dy / size.height;

    // Only plant in the garden area (bottom 70%)
    if (normalizedY < 0.3) return;

    final newFlowerIndex = _flowers.length;
    setState(() {
      _flowers.add(EmotionFlower(
        emotion: _selectedEmotion,
        x: normalizedX,
        y: normalizedY,
        size: 0.08 + _random.nextDouble() * 0.04,
        rotation: (_random.nextDouble() - 0.5) * 0.3,
        growthProgress: 0,
        swayPhase: _random.nextDouble() * 2 * pi,
      ));
    });

    // Animate growth using AnimationController for better performance
    _animateGrowth(newFlowerIndex);
  }

  void _animateGrowth(int index) {
    _growthController?.dispose();
    _growingFlowerIndex = index;

    _growthController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    )..addListener(() {
      if (_growingFlowerIndex >= 0 && _growingFlowerIndex < _flowers.length) {
        setState(() {
          _flowers[_growingFlowerIndex].growthProgress = _growthController!.value;
        });
      }
    });

    _growthController!.forward().then((_) {
      _growingFlowerIndex = -1;
    });
  }

  void _clearGarden() {
    _safeHaptic();
    setState(() => _flowers.clear());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(l10n.gameEmotionGarden),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _clearGarden,
            tooltip: l10n.emotionGardenClearGarden,
          ),
        ],
      ),
      body: Stack(
        children: [
          // Sky gradient background
          Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Color(0xFF87CEEB), // Sky blue
                  Color(0xFFB0E0E6), // Powder blue
                  Color(0xFF90EE90), // Light green (grass)
                ],
                stops: [0.0, 0.3, 0.35],
              ),
            ),
          ),

          // Garden (ground)
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            height: MediaQuery.of(context).size.height * 0.7,
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Color(0xFF90EE90),
                    Color(0xFF228B22),
                  ],
                ),
              ),
            ),
          ),

          // Flowers
          LayoutBuilder(
            builder: (context, constraints) {
              return GestureDetector(
                onTapDown: (details) => _plantFlower(
                  details.localPosition,
                  constraints.biggest,
                ),
                child: RepaintBoundary(
                  child: CustomPaint(
                    painter: GardenPainter(flowers: _flowers),
                    size: constraints.biggest,
                  ),
                ),
              );
            },
          ),

          // Emotion selector
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Colors.transparent,
                    Colors.black.withValues(alpha: 0.3),
                  ],
                ),
              ),
              child: SafeArea(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      l10n.emotionGardenHowFeeling,
                      style: theme.textTheme.titleMedium?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: Emotion.values.map((emotion) {
                          final isSelected = emotion == _selectedEmotion;
                          return Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 4),
                            child: GestureDetector(
                              onTap: () {
                                _safeHaptic();
                                setState(() => _selectedEmotion = emotion);
                              },
                              child: AnimatedContainer(
                                duration: const Duration(milliseconds: 150),
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 12,
                                  vertical: 8,
                                ),
                                decoration: BoxDecoration(
                                  color: isSelected
                                      ? emotion.color.withValues(alpha: 0.9)
                                      : Colors.white.withValues(alpha: 0.2),
                                  borderRadius: BorderRadius.circular(20),
                                  border: Border.all(
                                    color: isSelected
                                        ? Colors.white
                                        : Colors.transparent,
                                    width: 2,
                                  ),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Text(
                                      emotion.emoji,
                                      style: const TextStyle(fontSize: 20),
                                    ),
                                    if (isSelected) ...[
                                      const SizedBox(width: 6),
                                      Text(
                                        emotion.getLocalizedLabel(l10n),
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 12,
                                        ),
                                      ),
                                    ],
                                  ],
                                ),
                              ),
                            ),
                          );
                        }).toList(),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      '👆 ${l10n.emotionGardenTapToPlant(_selectedEmotion.getLocalizedLabel(l10n).toLowerCase())}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: Colors.white70,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Emotions with their flower representations
enum Emotion {
  happy('Happy', '😊', Color(0xFFFFD700), FlowerType.sunflower),
  calm('Calm', '😌', Color(0xFF87CEEB), FlowerType.bluebell),
  love('Love', '❤️', Color(0xFFFF69B4), FlowerType.rose),
  sad('Sad', '😢', Color(0xFF6495ED), FlowerType.violet),
  anxious('Anxious', '😰', Color(0xFF9370DB), FlowerType.lavender),
  excited('Excited', '🤩', Color(0xFFFF4500), FlowerType.tulip),
  grateful('Grateful', '🙏', Color(0xFFFFB6C1), FlowerType.cherry),
  hopeful('Hopeful', '🌟', Color(0xFF98FB98), FlowerType.daisy);

  final String label;
  final String emoji;
  final Color color;
  final FlowerType flowerType;

  const Emotion(this.label, this.emoji, this.color, this.flowerType);

  /// Get localized label for the emotion
  String getLocalizedLabel(AppLocalizations l10n) {
    switch (this) {
      case Emotion.happy:
        return l10n.emotionHappy;
      case Emotion.calm:
        return l10n.emotionCalm;
      case Emotion.love:
        return l10n.emotionLove;
      case Emotion.sad:
        return l10n.emotionSad;
      case Emotion.anxious:
        return l10n.emotionAnxious;
      case Emotion.excited:
        return l10n.emotionExcited;
      case Emotion.grateful:
        return l10n.emotionGrateful;
      case Emotion.hopeful:
        return l10n.emotionHopeful;
    }
  }
}

/// Flower types
enum FlowerType {
  sunflower,
  bluebell,
  rose,
  violet,
  lavender,
  tulip,
  cherry,
  daisy,
}

/// Represents a planted emotion flower
class EmotionFlower {
  final Emotion emotion;
  final double x;
  final double y;
  final double size;
  final double rotation;
  double growthProgress;
  final double swayPhase;

  EmotionFlower({
    required this.emotion,
    required this.x,
    required this.y,
    required this.size,
    required this.rotation,
    required this.growthProgress,
    required this.swayPhase,
  });
}

/// Custom painter for the garden
class GardenPainter extends CustomPainter {
  final List<EmotionFlower> flowers;

  // Cache reusable Paint objects for better performance
  static final Paint _stemPaint = Paint()
    ..color = const Color(0xFF228B22)
    ..strokeWidth = 3
    ..strokeCap = StrokeCap.round;

  static final Paint _leafPaint = Paint()..color = const Color(0xFF32CD32);

  GardenPainter({required this.flowers});

  @override
  void paint(Canvas canvas, Size size) {
    for (final flower in flowers) {
      _drawFlower(canvas, size, flower);
    }
  }

  void _drawFlower(Canvas canvas, Size size, EmotionFlower flower) {
    final centerX = flower.x * size.width;
    final centerY = flower.y * size.height;
    final flowerSize = flower.size * size.width * flower.growthProgress;

    if (flowerSize <= 0) return;

    canvas.save();
    canvas.translate(centerX, centerY);
    canvas.rotate(flower.rotation);

    // Draw stem
    canvas.drawLine(
      Offset.zero,
      Offset(0, flowerSize * 1.5),
      _stemPaint,
    );

    // Draw leaves
    canvas.drawOval(
      Rect.fromCenter(
        center: Offset(-flowerSize * 0.3, flowerSize * 0.8),
        width: flowerSize * 0.4,
        height: flowerSize * 0.2,
      ),
      _leafPaint,
    );
    canvas.drawOval(
      Rect.fromCenter(
        center: Offset(flowerSize * 0.3, flowerSize * 1.0),
        width: flowerSize * 0.4,
        height: flowerSize * 0.2,
      ),
      _leafPaint,
    );

    // Draw flower head
    _drawFlowerHead(canvas, flower, flowerSize);

    canvas.restore();
  }

  void _drawFlowerHead(Canvas canvas, EmotionFlower flower, double size) {
    final petalPaint = Paint()..color = flower.emotion.color;
    final centerPaint = Paint()..color = _darkenColor(flower.emotion.color);

    switch (flower.emotion.flowerType) {
      case FlowerType.sunflower:
        // Many petals around center
        for (int i = 0; i < 12; i++) {
          canvas.save();
          canvas.rotate(i * pi / 6);
          canvas.drawOval(
            Rect.fromCenter(
              center: Offset(0, -size * 0.5),
              width: size * 0.3,
              height: size * 0.6,
            ),
            petalPaint,
          );
          canvas.restore();
        }
        canvas.drawCircle(Offset.zero, size * 0.3, centerPaint);
        break;

      case FlowerType.rose:
        // Layered petals
        for (int layer = 0; layer < 3; layer++) {
          final layerSize = size * (1 - layer * 0.2);
          for (int i = 0; i < 5; i++) {
            canvas.save();
            canvas.rotate(i * pi * 2 / 5 + layer * 0.3);
            canvas.drawOval(
              Rect.fromCenter(
                center: Offset(0, -layerSize * 0.3),
                width: layerSize * 0.5,
                height: layerSize * 0.6,
              ),
              petalPaint..color = flower.emotion.color.withValues(alpha: 0.9 - layer * 0.2),
            );
            canvas.restore();
          }
        }
        break;

      case FlowerType.tulip:
        // Cup-shaped
        for (int i = 0; i < 5; i++) {
          canvas.save();
          canvas.rotate(i * pi * 2 / 5);
          canvas.drawOval(
            Rect.fromCenter(
              center: Offset(0, -size * 0.2),
              width: size * 0.35,
              height: size * 0.7,
            ),
            petalPaint,
          );
          canvas.restore();
        }
        break;

      case FlowerType.daisy:
      case FlowerType.bluebell:
      case FlowerType.violet:
      case FlowerType.lavender:
      case FlowerType.cherry:
        // Simple 5-petal flower
        for (int i = 0; i < 5; i++) {
          canvas.save();
          canvas.rotate(i * pi * 2 / 5);
          canvas.drawOval(
            Rect.fromCenter(
              center: Offset(0, -size * 0.4),
              width: size * 0.4,
              height: size * 0.5,
            ),
            petalPaint,
          );
          canvas.restore();
        }
        canvas.drawCircle(Offset.zero, size * 0.2, centerPaint);
        break;
    }
  }

  Color _darkenColor(Color color) {
    return Color.fromARGB(
      color.alpha,
      (color.red * 0.7).round(),
      (color.green * 0.7).round(),
      (color.blue * 0.7).round(),
    );
  }

  @override
  bool shouldRepaint(covariant GardenPainter oldDelegate) {
    return oldDelegate.flowers != flowers;
  }
}


import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback
void _safeHaptic() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

/// Zen Sand - Meditative sand garden game
class ZenSandGame extends StatefulWidget {
  const ZenSandGame({super.key});

  @override
  State<ZenSandGame> createState() => _ZenSandGameState();
}

class _ZenSandGameState extends State<ZenSandGame> {
  final List<SandStroke> _strokes = [];
  final List<Stone> _stones = [];
  SandStroke? _currentStroke;
  bool _isPlacingStone = false;
  StoneType _selectedStoneType = StoneType.small;
  final Random _random = Random();

  final Color _sandColor = const Color(0xFFE6D5AC);
  final Color _strokeColor = const Color(0xFFD4C499);

  void _onPanStart(DragStartDetails details) {
    if (_isPlacingStone) return;
    _safeHaptic();
    setState(() {
      _currentStroke = SandStroke(
        points: [details.localPosition],
        width: 12,
      );
    });
  }

  void _onPanUpdate(DragUpdateDetails details) {
    if (_currentStroke == null || _isPlacingStone) return;
    setState(() {
      _currentStroke!.points.add(details.localPosition);
    });
  }

  void _onPanEnd(DragEndDetails details) {
    if (_currentStroke == null || _isPlacingStone) return;
    setState(() {
      _strokes.add(_currentStroke!);
      _currentStroke = null;
    });
  }

  void _onTap(TapDownDetails details) {
    if (!_isPlacingStone) return;
    _safeHaptic();
    setState(() {
      _stones.add(Stone(
        position: details.localPosition,
        type: _selectedStoneType,
        rotation: _random.nextDouble() * 2 * pi,
      ));
      _isPlacingStone = false;
    });
  }

  void _clearCanvas() {
    _safeHaptic();
    setState(() {
      _strokes.clear();
    });
  }

  void _clearAll() {
    _safeHaptic();
    setState(() {
      _strokes.clear();
      _stones.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      backgroundColor: _sandColor,
      appBar: AppBar(
        backgroundColor: const Color(0xFF8B7355),
        foregroundColor: Colors.white,
        title: Text(l10n.gameZenSand),
        actions: [
          IconButton(
            icon: const Icon(Icons.auto_fix_high),
            onPressed: _clearCanvas,
            tooltip: l10n.get('clearStrokes'),
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _clearAll,
            tooltip: l10n.get('clearAll'),
          ),
        ],
      ),
      body: Column(
        children: [
          // Stone palette
          Container(
            height: 80,
            color: const Color(0xFF8B7355).withOpacity(0.9),
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _StoneButton(
                  stoneType: StoneType.small,
                  isSelected: _isPlacingStone && _selectedStoneType == StoneType.small,
                  onTap: () {
                    _safeHaptic();
                    setState(() {
                      _isPlacingStone = true;
                      _selectedStoneType = StoneType.small;
                    });
                  },
                ),
                _StoneButton(
                  stoneType: StoneType.medium,
                  isSelected: _isPlacingStone && _selectedStoneType == StoneType.medium,
                  onTap: () {
                    _safeHaptic();
                    setState(() {
                      _isPlacingStone = true;
                      _selectedStoneType = StoneType.medium;
                    });
                  },
                ),
                _StoneButton(
                  stoneType: StoneType.large,
                  isSelected: _isPlacingStone && _selectedStoneType == StoneType.large,
                  onTap: () {
                    _safeHaptic();
                    setState(() {
                      _isPlacingStone = true;
                      _selectedStoneType = StoneType.large;
                    });
                  },
                ),
                _StoneButton(
                  stoneType: StoneType.flat,
                  isSelected: _isPlacingStone && _selectedStoneType == StoneType.flat,
                  onTap: () {
                    _safeHaptic();
                    setState(() {
                      _isPlacingStone = true;
                      _selectedStoneType = StoneType.flat;
                    });
                  },
                ),
                // Cancel button
                if (_isPlacingStone)
                  IconButton(
                    icon: const Icon(Icons.close, color: Colors.white),
                    onPressed: () {
                      setState(() => _isPlacingStone = false);
                    },
                  ),
              ],
            ),
          ),

          // Sand canvas
          Expanded(
            child: GestureDetector(
              onPanStart: _onPanStart,
              onPanUpdate: _onPanUpdate,
              onPanEnd: _onPanEnd,
              onTapDown: _onTap,
              child: Container(
                color: _sandColor,
                child: CustomPaint(
                  painter: ZenSandPainter(
                    strokes: _strokes,
                    currentStroke: _currentStroke,
                    stones: _stones,
                    sandColor: _sandColor,
                    strokeColor: _strokeColor,
                  ),
                  size: Size.infinite,
                ),
              ),
            ),
          ),

          // Instructions
          Container(
            color: const Color(0xFF8B7355).withOpacity(0.9),
            padding: const EdgeInsets.all(12),
            child: SafeArea(
              top: false,
              child: Text(
                _isPlacingStone
                    ? '👆 ${l10n.get('tapToPlaceStone').replaceAll('{stone}', _selectedStoneType.name)}'
                    : '🖌️ ${l10n.get('zenSandInstructions')}',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: Colors.white70,
                ),
                textAlign: TextAlign.center,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Stone types
enum StoneType {
  small(20, '🪨'),
  medium(35, '🪨'),
  large(50, '🪨'),
  flat(40, '⬭');

  final double size;
  final String emoji;

  const StoneType(this.size, this.emoji);
}

/// Stone button widget
class _StoneButton extends StatelessWidget {
  final StoneType stoneType;
  final bool isSelected;
  final VoidCallback onTap;

  const _StoneButton({
    required this.stoneType,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        width: 56,
        height: 56,
        decoration: BoxDecoration(
          color: isSelected ? Colors.white24 : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: isSelected ? Colors.white : Colors.white24,
            width: 2,
          ),
        ),
        child: Center(
          child: Container(
            width: stoneType.size * 0.6,
            height: stoneType.size * 0.5,
            decoration: BoxDecoration(
              color: const Color(0xFF5C5C5C),
              borderRadius: BorderRadius.circular(stoneType == StoneType.flat ? 4 : 12),
              boxShadow: [
                BoxShadow(
                  color: Colors.black26,
                  blurRadius: 2,
                  offset: const Offset(1, 1),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Represents a sand rake stroke
class SandStroke {
  final List<Offset> points;
  final double width;

  SandStroke({
    required this.points,
    required this.width,
  });
}

/// Represents a placed stone
class Stone {
  final Offset position;
  final StoneType type;
  final double rotation;

  Stone({
    required this.position,
    required this.type,
    required this.rotation,
  });
}

/// Custom painter for the zen sand garden
class ZenSandPainter extends CustomPainter {
  final List<SandStroke> strokes;
  final SandStroke? currentStroke;
  final List<Stone> stones;
  final Color sandColor;
  final Color strokeColor;

  ZenSandPainter({
    required this.strokes,
    this.currentStroke,
    required this.stones,
    required this.sandColor,
    required this.strokeColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    // Draw rake strokes
    final strokePaint = Paint()
      ..color = strokeColor
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round
      ..style = PaintingStyle.stroke;

    for (final stroke in [...strokes, ?currentStroke]) {
      if (stroke.points.length < 2) continue;

      // Draw parallel lines for rake effect
      for (int offset = -2; offset <= 2; offset++) {
        final path = Path();
        bool first = true;

        for (int i = 0; i < stroke.points.length; i++) {
          final point = stroke.points[i];

          // Calculate perpendicular offset
          Offset perpOffset = Offset.zero;
          if (i < stroke.points.length - 1) {
            final next = stroke.points[i + 1];
            final dx = next.dx - point.dx;
            final dy = next.dy - point.dy;
            final len = sqrt(dx * dx + dy * dy);
            if (len > 0) {
              perpOffset = Offset(-dy / len * offset * 4, dx / len * offset * 4);
            }
          } else if (i > 0) {
            final prev = stroke.points[i - 1];
            final dx = point.dx - prev.dx;
            final dy = point.dy - prev.dy;
            final len = sqrt(dx * dx + dy * dy);
            if (len > 0) {
              perpOffset = Offset(-dy / len * offset * 4, dx / len * offset * 4);
            }
          }

          final adjustedPoint = point + perpOffset;

          if (first) {
            path.moveTo(adjustedPoint.dx, adjustedPoint.dy);
            first = false;
          } else {
            path.lineTo(adjustedPoint.dx, adjustedPoint.dy);
          }
        }

        canvas.drawPath(path, strokePaint);
      }
    }

    // Draw stones
    for (final stone in stones) {
      canvas.save();
      canvas.translate(stone.position.dx, stone.position.dy);
      canvas.rotate(stone.rotation);

      final paint = Paint()..color = const Color(0xFF4A4A4A);
      final shadowPaint = Paint()
        ..color = Colors.black26
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4);

      final size = stone.type.size;

      if (stone.type == StoneType.flat) {
        // Flat oval stone
        canvas.drawOval(
          Rect.fromCenter(center: const Offset(2, 2), width: size, height: size * 0.6),
          shadowPaint,
        );
        canvas.drawOval(
          Rect.fromCenter(center: Offset.zero, width: size, height: size * 0.6),
          paint,
        );
      } else {
        // Round stones
        canvas.drawCircle(const Offset(2, 2), size / 2, shadowPaint);
        canvas.drawCircle(Offset.zero, size / 2, paint);

        // Highlight
        final highlightPaint = Paint()
          ..color = Colors.white24
          ..style = PaintingStyle.fill;
        canvas.drawCircle(
          Offset(-size * 0.15, -size * 0.15),
          size * 0.2,
          highlightPaint,
        );
      }

      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(covariant ZenSandPainter oldDelegate) {
    return oldDelegate.strokes != strokes ||
        oldDelegate.currentStroke != currentStroke ||
        oldDelegate.stones != stones;
  }
}


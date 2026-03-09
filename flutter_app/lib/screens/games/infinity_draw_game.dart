import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback
void _safeHaptic() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

/// Infinity Draw - Endless creative drawing game
class InfinityDrawGame extends StatefulWidget {
  const InfinityDrawGame({super.key});

  @override
  State<InfinityDrawGame> createState() => _InfinityDrawGameState();
}

class _InfinityDrawGameState extends State<InfinityDrawGame> {
  final List<DrawingStroke> _strokes = [];
  DrawingStroke? _currentStroke;
  Color _selectedColor = Colors.purple;
  double _strokeWidth = 4.0;
  bool _isEraser = false;

  final List<Color> _colorPalette = [
    Colors.purple,
    Colors.pink,
    Colors.red,
    Colors.orange,
    Colors.yellow,
    Colors.green,
    Colors.teal,
    Colors.blue,
    Colors.indigo,
    Colors.white,
    Colors.grey,
    Colors.black,
  ];

  void _onPanStart(DragStartDetails details) {
    _safeHaptic();
    setState(() {
      _currentStroke = DrawingStroke(
        color: _isEraser ? Colors.transparent : _selectedColor,
        strokeWidth: _isEraser ? 20.0 : _strokeWidth,
        points: [details.localPosition],
        isEraser: _isEraser,
      );
    });
  }

  void _onPanUpdate(DragUpdateDetails details) {
    if (_currentStroke == null) return;
    setState(() {
      _currentStroke!.points.add(details.localPosition);
    });
  }

  void _onPanEnd(DragEndDetails details) {
    if (_currentStroke == null) return;
    setState(() {
      _strokes.add(_currentStroke!);
      _currentStroke = null;
    });
  }

  void _clearCanvas() {
    _safeHaptic();
    setState(() {
      _strokes.clear();
      _currentStroke = null;
    });
  }

  void _undo() {
    if (_strokes.isNotEmpty) {
      _safeHaptic();
      setState(() {
        _strokes.removeLast();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        title: Text(l10n.gameInfinityDraw),
        actions: [
          IconButton(
            icon: const Icon(Icons.undo),
            onPressed: _strokes.isNotEmpty ? _undo : null,
            tooltip: l10n.get('undo'),
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _clearCanvas,
            tooltip: l10n.get('clear'),
          ),
        ],
      ),
      body: Column(
        children: [
          // Color palette
          Container(
            height: 60,
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _colorPalette.length + 1, // +1 for eraser
              itemBuilder: (context, index) {
                if (index == _colorPalette.length) {
                  // Eraser
                  return Padding(
                    padding: const EdgeInsets.only(left: 8),
                    child: GestureDetector(
                      onTap: () {
                        _safeHaptic();
                        setState(() => _isEraser = !_isEraser);
                      },
                      child: Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: theme.colorScheme.surfaceContainerHighest,
                          border: Border.all(
                            color: _isEraser
                                ? theme.colorScheme.primary
                                : Colors.transparent,
                            width: 3,
                          ),
                        ),
                        child: Icon(
                          Icons.auto_fix_high,
                          color: _isEraser
                              ? theme.colorScheme.primary
                              : theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                  );
                }

                final color = _colorPalette[index];
                final isSelected = color == _selectedColor && !_isEraser;

                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: GestureDetector(
                    onTap: () {
                      _safeHaptic();
                      setState(() {
                        _selectedColor = color;
                        _isEraser = false;
                      });
                    },
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 150),
                      width: isSelected ? 44 : 40,
                      height: isSelected ? 44 : 40,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: color,
                        border: Border.all(
                          color: isSelected
                              ? theme.colorScheme.onSurface
                              : Colors.transparent,
                          width: 3,
                        ),
                        boxShadow: isSelected
                            ? [
                                BoxShadow(
                                  color: color.withValues(alpha: 0.5),
                                  blurRadius: 8,
                                  spreadRadius: 2,
                                ),
                              ]
                            : null,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),

          // Stroke width slider
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Icon(Icons.lens, size: 8, color: theme.colorScheme.outline),
                Expanded(
                  child: Slider(
                    value: _strokeWidth,
                    min: 2,
                    max: 20,
                    onChanged: (value) {
                      setState(() => _strokeWidth = value);
                    },
                  ),
                ),
                Icon(Icons.lens, size: 20, color: theme.colorScheme.outline),
              ],
            ),
          ),

          // Drawing canvas
          Expanded(
            child: Container(
              margin: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerLowest,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: theme.colorScheme.outline.withValues(alpha: 0.2),
                ),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(20),
                child: GestureDetector(
                  onPanStart: _onPanStart,
                  onPanUpdate: _onPanUpdate,
                  onPanEnd: _onPanEnd,
                  child: CustomPaint(
                    painter: DrawingPainter(
                      strokes: _strokes,
                      currentStroke: _currentStroke,
                      backgroundColor: theme.colorScheme.surfaceContainerLowest,
                    ),
                    size: Size.infinite,
                  ),
                ),
              ),
            ),
          ),

          // Tips
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '✨ Draw freely! No rules, no pressure.',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Represents a single drawing stroke
class DrawingStroke {
  final Color color;
  final double strokeWidth;
  final List<Offset> points;
  final bool isEraser;

  DrawingStroke({
    required this.color,
    required this.strokeWidth,
    required this.points,
    this.isEraser = false,
  });
}

/// Custom painter for the drawing canvas
class DrawingPainter extends CustomPainter {
  final List<DrawingStroke> strokes;
  final DrawingStroke? currentStroke;
  final Color backgroundColor;

  DrawingPainter({
    required this.strokes,
    this.currentStroke,
    required this.backgroundColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    // Draw all completed strokes
    for (final stroke in strokes) {
      _drawStroke(canvas, stroke);
    }

    // Draw current stroke
    if (currentStroke != null) {
      _drawStroke(canvas, currentStroke!);
    }
  }

  void _drawStroke(Canvas canvas, DrawingStroke stroke) {
    if (stroke.points.isEmpty) return;

    final paint = Paint()
      ..color = stroke.isEraser ? backgroundColor : stroke.color
      ..strokeWidth = stroke.strokeWidth
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..style = PaintingStyle.stroke;

    if (stroke.points.length == 1) {
      // Single point - draw a dot
      canvas.drawCircle(stroke.points.first, stroke.strokeWidth / 2, paint);
    } else {
      // Multiple points - draw a path
      final path = Path();
      path.moveTo(stroke.points.first.dx, stroke.points.first.dy);

      for (int i = 1; i < stroke.points.length; i++) {
        final p0 = stroke.points[i - 1];
        final p1 = stroke.points[i];

        // Use quadratic bezier for smoother lines
        final midPoint = Offset(
          (p0.dx + p1.dx) / 2,
          (p0.dy + p1.dy) / 2,
        );
        path.quadraticBezierTo(p0.dx, p0.dy, midPoint.dx, midPoint.dy);
      }

      // Draw to the last point
      if (stroke.points.length > 1) {
        path.lineTo(stroke.points.last.dx, stroke.points.last.dy);
      }

      canvas.drawPath(path, paint);
    }
  }

  @override
  bool shouldRepaint(covariant DrawingPainter oldDelegate) {
    return oldDelegate.strokes != strokes ||
        oldDelegate.currentStroke != currentStroke;
  }
}


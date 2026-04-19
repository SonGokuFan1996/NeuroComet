import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Constellation Connect Game - Connect stars to create constellations
class ConstellationConnectGame extends ConsumerStatefulWidget {
  const ConstellationConnectGame({super.key});

  @override
  ConsumerState<ConstellationConnectGame> createState() => _ConstellationConnectGameState();
}

class _ConstellationConnectGameState extends ConsumerState<ConstellationConnectGame>
    with SingleTickerProviderStateMixin {
  final List<_Star> _stars = [];
  final List<_Connection> _connections = [];
  _Star? _selectedStar;
  late AnimationController _twinkleController;
  final Random _random = Random();

  @override
  void initState() {
    super.initState();
    _twinkleController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);
    _generateStars();
  }

  @override
  void dispose() {
    _twinkleController.dispose();
    super.dispose();
  }

  void _generateStars() {
    _stars.clear();
    _connections.clear();
    _selectedStar = null;
    for (int i = 0; i < 15; i++) {
      _stars.add(_Star(
        id: i,
        position: Offset(
          0.1 + _random.nextDouble() * 0.8,
          0.1 + _random.nextDouble() * 0.8,
        ),
        brightness: 0.5 + _random.nextDouble() * 0.5,
        size: 4 + _random.nextDouble() * 6,
      ));
    }
  }

  void _onStarTap(_Star star) {
    if (!kIsWeb) HapticFeedback.lightImpact();
    setState(() {
      if (_selectedStar == null) {
        _selectedStar = star;
      } else if (_selectedStar!.id == star.id) {
        _selectedStar = null;
      } else {
        // Check if connection already exists
        final exists = _connections.any((c) =>
            (c.from == _selectedStar!.id && c.to == star.id) ||
            (c.from == star.id && c.to == _selectedStar!.id));
        if (!exists) {
          _connections.add(_Connection(from: _selectedStar!.id, to: star.id));
          if (!kIsWeb) HapticFeedback.selectionClick();
        } else {
          _connections.removeWhere((c) =>
              (c.from == _selectedStar!.id && c.to == star.id) ||
              (c.from == star.id && c.to == _selectedStar!.id));
        }
        _selectedStar = null;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0A0A1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        title: const Text('Constellation Connect', style: TextStyle(color: Colors.white)),
        iconTheme: const IconThemeData(color: Colors.white),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => setState(() => _generateStars()),
            tooltip: 'New stars',
          ),
          IconButton(
            icon: const Icon(Icons.undo),
            onPressed: _connections.isNotEmpty ? () => setState(() => _connections.removeLast()) : null,
            tooltip: 'Undo',
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: LayoutBuilder(
              builder: (context, constraints) {
                return AnimatedBuilder(
                  animation: _twinkleController,
                  builder: (context, _) => GestureDetector(
                    onTapDown: (details) {
                      // Add a new star where user taps
                      final pos = Offset(
                        details.localPosition.dx / constraints.maxWidth,
                        details.localPosition.dy / constraints.maxHeight,
                      );
                      // Check if tapped near an existing star
                      for (final star in _stars) {
                        final starPos = Offset(
                          star.position.dx * constraints.maxWidth,
                          star.position.dy * constraints.maxHeight,
                        );
                        if ((details.localPosition - starPos).distance < 30) {
                          _onStarTap(star);
                          return;
                        }
                      }
                      // Add new star
                      if (!kIsWeb) HapticFeedback.lightImpact();
                      setState(() {
                        _stars.add(_Star(
                          id: _stars.length,
                          position: pos,
                          brightness: 0.5 + _random.nextDouble() * 0.5,
                          size: 4 + _random.nextDouble() * 6,
                        ));
                      });
                    },
                    child: CustomPaint(
                      painter: _ConstellationPainter(
                        stars: _stars,
                        connections: _connections,
                        selectedStarId: _selectedStar?.id,
                        twinkleValue: _twinkleController.value,
                      ),
                      size: Size(constraints.maxWidth, constraints.maxHeight),
                    ),
                  ),
                );
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.touch_app, color: Colors.white.withAlpha(100), size: 16),
                const SizedBox(width: 8),
                Text(
                  '${_stars.length} stars • ${_connections.length} connections',
                  style: TextStyle(color: Colors.white.withAlpha(150), fontSize: 12),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Star {
  final int id;
  final Offset position;
  final double brightness;
  final double size;

  const _Star({required this.id, required this.position, required this.brightness, required this.size});
}

class _Connection {
  final int from;
  final int to;

  const _Connection({required this.from, required this.to});
}

class _ConstellationPainter extends CustomPainter {
  final List<_Star> stars;
  final List<_Connection> connections;
  final int? selectedStarId;
  final double twinkleValue;

  _ConstellationPainter({
    required this.stars,
    required this.connections,
    this.selectedStarId,
    required this.twinkleValue,
  });

  @override
  void paint(Canvas canvas, Size size) {
    // Draw connections
    final linePaint = Paint()
      ..color = Colors.white.withAlpha(80)
      ..strokeWidth = 1.5
      ..style = PaintingStyle.stroke;

    for (final connection in connections) {
      final from = stars.firstWhere((s) => s.id == connection.from);
      final to = stars.firstWhere((s) => s.id == connection.to);
      canvas.drawLine(
        Offset(from.position.dx * size.width, from.position.dy * size.height),
        Offset(to.position.dx * size.width, to.position.dy * size.height),
        linePaint,
      );
    }

    // Draw stars
    for (final star in stars) {
      final pos = Offset(star.position.dx * size.width, star.position.dy * size.height);
      final twinkle = (star.brightness + twinkleValue * 0.3).clamp(0.3, 1.0);
      final isSelected = star.id == selectedStarId;

      // Glow
      final glowPaint = Paint()
        ..color = (isSelected ? Colors.cyanAccent : Colors.white).withAlpha((twinkle * 40).round())
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);
      canvas.drawCircle(pos, star.size * 2, glowPaint);

      // Star body
      final starPaint = Paint()
        ..color = (isSelected ? Colors.cyanAccent : Colors.white).withAlpha((twinkle * 255).round());
      canvas.drawCircle(pos, isSelected ? star.size + 2 : star.size, starPaint);
    }
  }

  @override
  bool shouldRepaint(covariant _ConstellationPainter old) => true;
}


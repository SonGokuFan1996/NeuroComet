import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback
void _safeHaptic() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

/// Sensory Rain - Calming rain effect game
class SensoryRainGame extends StatefulWidget {
  const SensoryRainGame({super.key});

  @override
  State<SensoryRainGame> createState() => _SensoryRainGameState();
}

class _SensoryRainGameState extends State<SensoryRainGame>
    with TickerProviderStateMixin {
  late AnimationController _rainController;
  final List<RainDrop> _rainDrops = [];
  final List<Ripple> _ripples = [];
  final Random _random = Random();

  double _intensity = 0.5; // 0.0 to 1.0
  bool _isNightMode = true;

  @override
  void initState() {
    super.initState();
    _rainController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    )..addListener(_updateRain);
    _rainController.repeat();
  }

  @override
  void dispose() {
    _rainController.dispose();
    super.dispose();
  }

  void _updateRain() {
    setState(() {
      // Add new rain drops based on intensity
      final dropCount = (_intensity * 5).round();
      for (int i = 0; i < dropCount; i++) {
        if (_random.nextDouble() < 0.3) {
          _rainDrops.add(RainDrop(
            x: _random.nextDouble(),
            y: -0.1,
            speed: 0.01 + _random.nextDouble() * 0.02,
            length: 0.02 + _random.nextDouble() * 0.03,
            opacity: 0.3 + _random.nextDouble() * 0.7,
          ));
        }
      }

      // Update rain drops
      for (int i = _rainDrops.length - 1; i >= 0; i--) {
        _rainDrops[i].y += _rainDrops[i].speed;
        if (_rainDrops[i].y > 1.1) {
          // Create ripple at bottom
          if (_random.nextDouble() < 0.3) {
            _ripples.add(Ripple(
              x: _rainDrops[i].x,
              y: 0.95,
              radius: 0,
              maxRadius: 0.03 + _random.nextDouble() * 0.02,
              opacity: 0.6,
            ));
          }
          _rainDrops.removeAt(i);
        }
      }

      // Update ripples
      for (int i = _ripples.length - 1; i >= 0; i--) {
        _ripples[i].radius += 0.002;
        _ripples[i].opacity -= 0.02;
        if (_ripples[i].opacity <= 0 || _ripples[i].radius >= _ripples[i].maxRadius) {
          _ripples.removeAt(i);
        }
      }
    });
  }

  void _onTapDown(TapDownDetails details, Size size) {
    _safeHaptic();
    final x = details.localPosition.dx / size.width;
    final y = details.localPosition.dy / size.height;

    setState(() {
      // Create multiple ripples at tap location
      for (int i = 0; i < 3; i++) {
        _ripples.add(Ripple(
          x: x,
          y: y,
          radius: 0,
          maxRadius: 0.08 + i * 0.03,
          opacity: 0.8 - i * 0.2,
        ));
      }
    });
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
        title: Text(l10n.gameSensoryRain),
        actions: [
          // Night/Day mode toggle
          IconButton(
            icon: Icon(_isNightMode ? Icons.light_mode : Icons.dark_mode),
            onPressed: () {
              _safeHaptic();
              setState(() => _isNightMode = !_isNightMode);
            },
            tooltip: _isNightMode ? l10n.get('dayMode') : l10n.get('nightMode'),
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
                colors: _isNightMode
                    ? [
                        const Color(0xFF0D1B2A),
                        const Color(0xFF1B263B),
                        const Color(0xFF415A77),
                      ]
                    : [
                        const Color(0xFF6B8DD6),
                        const Color(0xFF8E9AAF),
                        const Color(0xFFBDC3C7),
                      ],
              ),
            ),
          ),

          // Rain and ripples
          LayoutBuilder(
            builder: (context, constraints) {
              return GestureDetector(
                onTapDown: (details) => _onTapDown(details, constraints.biggest),
                child: CustomPaint(
                  painter: RainPainter(
                    rainDrops: _rainDrops,
                    ripples: _ripples,
                    isNightMode: _isNightMode,
                  ),
                  size: constraints.biggest,
                ),
              );
            },
          ),

          // Controls overlay
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Colors.transparent,
                    Colors.black.withOpacity(0.5),
                  ],
                ),
              ),
              child: SafeArea(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      l10n.get('rainIntensity'),
                      style: theme.textTheme.labelMedium?.copyWith(
                        color: Colors.white70,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        const Icon(Icons.water_drop_outlined,
                            color: Colors.white54, size: 20),
                        Expanded(
                          child: Slider(
                            value: _intensity,
                            onChanged: (value) {
                              setState(() => _intensity = value);
                            },
                            activeColor: Colors.white,
                            inactiveColor: Colors.white24,
                          ),
                        ),
                        const Icon(Icons.thunderstorm,
                            color: Colors.white54, size: 20),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Text(
                      '💧 ${l10n.get('tapForRipples')}',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: Colors.white54,
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

/// Represents a single rain drop
class RainDrop {
  double x;
  double y;
  final double speed;
  final double length;
  final double opacity;

  RainDrop({
    required this.x,
    required this.y,
    required this.speed,
    required this.length,
    required this.opacity,
  });
}

/// Represents a ripple effect
class Ripple {
  final double x;
  final double y;
  double radius;
  final double maxRadius;
  double opacity;

  Ripple({
    required this.x,
    required this.y,
    required this.radius,
    required this.maxRadius,
    required this.opacity,
  });
}

/// Custom painter for rain and ripples
class RainPainter extends CustomPainter {
  final List<RainDrop> rainDrops;
  final List<Ripple> ripples;
  final bool isNightMode;

  RainPainter({
    required this.rainDrops,
    required this.ripples,
    required this.isNightMode,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final rainColor = isNightMode
        ? const Color(0xFF87CEEB)
        : const Color(0xFF4A6FA5);

    // Draw rain drops
    for (final drop in rainDrops) {
      final paint = Paint()
        ..color = rainColor.withOpacity(drop.opacity * 0.6)
        ..strokeWidth = 2
        ..strokeCap = StrokeCap.round;

      final startX = drop.x * size.width;
      final startY = drop.y * size.height;
      final endY = startY + drop.length * size.height;

      canvas.drawLine(
        Offset(startX, startY),
        Offset(startX, endY),
        paint,
      );
    }

    // Draw ripples
    for (final ripple in ripples) {
      final paint = Paint()
        ..color = rainColor.withOpacity(ripple.opacity * 0.5)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.5;

      final centerX = ripple.x * size.width;
      final centerY = ripple.y * size.height;
      final radius = ripple.radius * size.width;

      canvas.drawCircle(Offset(centerX, centerY), radius, paint);
    }
  }

  @override
  bool shouldRepaint(covariant RainPainter oldDelegate) {
    return true; // Always repaint for animation
  }
}


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
    HapticFeedback.selectionClick();
  }
}

void _safeHapticMedium() {
  if (!kIsWeb) {
    HapticFeedback.mediumImpact();
  }
}

/// Fidget Spinner Game - Spin to relax
class FidgetSpinnerGame extends ConsumerStatefulWidget {
  const FidgetSpinnerGame({super.key});

  @override
  ConsumerState<FidgetSpinnerGame> createState() => _FidgetSpinnerGameState();
}

class _FidgetSpinnerGameState extends ConsumerState<FidgetSpinnerGame>
    with SingleTickerProviderStateMixin {
  double _rotation = 0;
  double _velocity = 0;
  Offset? _lastPosition;
  DateTime? _lastTime;
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    )..addListener(_updateRotation);
  }

  void _updateRotation() {
    setState(() {
      _rotation += _velocity * 0.016; // ~60fps
      _velocity *= 0.995; // Friction

      if (_velocity.abs() < 0.1) {
        _velocity = 0;
        _controller.stop();
      } else {
        // Haptic feedback at certain intervals
        if ((_rotation * 10).floor() % 60 == 0) {
          _safeHapticFeedback();
        }
      }
    });
  }

  void _onPanStart(DragStartDetails details) {
    _controller.stop();
    _lastPosition = details.localPosition;
    _lastTime = DateTime.now();
  }

  void _onPanUpdate(DragUpdateDetails details) {
    if (_lastPosition == null || _lastTime == null) return;

    final center = Offset(
      MediaQuery.of(context).size.width / 2,
      MediaQuery.of(context).size.height / 2,
    );

    final previousAngle = atan2(
      _lastPosition!.dy - center.dy,
      _lastPosition!.dx - center.dx,
    );
    final currentAngle = atan2(
      details.localPosition.dy - center.dy,
      details.localPosition.dx - center.dx,
    );

    final angleDiff = currentAngle - previousAngle;
    setState(() {
      _rotation += angleDiff;
    });

    final now = DateTime.now();
    final timeDiff = now.difference(_lastTime!).inMilliseconds;
    if (timeDiff > 0) {
      _velocity = angleDiff / (timeDiff / 1000.0);
    }

    _lastPosition = details.localPosition;
    _lastTime = now;
  }

  void _onPanEnd(DragEndDetails details) {
    _controller.repeat();
    _safeHapticMedium();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.gameFidgetSpinner),
        actions: [
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => _showTutorial(context),
          ),
        ],
      ),
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              theme.colorScheme.surface,
              AppColors.calmBlue.withAlpha(30),
            ],
          ),
        ),
        child: GestureDetector(
          onPanStart: _onPanStart,
          onPanUpdate: _onPanUpdate,
          onPanEnd: _onPanEnd,
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Speed indicator
                Text(
                  'Speed: ${(_velocity.abs() * 10).toStringAsFixed(0)} RPM',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(height: 40),

                // Spinner
                Transform.rotate(
                  angle: _rotation,
                  child: SizedBox(
                    width: 250,
                    height: 250,
                    child: CustomPaint(
                      painter: _SpinnerPainter(
                        color: AppColors.primaryPurple,
                        secondaryColor: AppColors.secondaryTeal,
                      ),
                    ),
                  ),
                ),

                const SizedBox(height: 40),

                // Instructions
                Text(
                  'Swipe to spin!',
                  style: theme.textTheme.bodyLarge?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showTutorial(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Text('🌀 '),
            Text('Fidget Spinner'),
          ],
        ),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('• Swipe in a circular motion to spin'),
            SizedBox(height: 8),
            Text('• The faster you swipe, the faster it spins'),
            SizedBox(height: 8),
            Text('• Feel the haptic feedback as it spins'),
            SizedBox(height: 8),
            Text('• No goals - just relax and spin! '),
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

class _SpinnerPainter extends CustomPainter {
  final Color color;
  final Color secondaryColor;

  _SpinnerPainter({required this.color, required this.secondaryColor});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;

    // Draw 3 arms
    for (int i = 0; i < 3; i++) {
      final angle = i * 2 * pi / 3;
      final armEnd = Offset(
        center.dx + cos(angle) * (radius - 20),
        center.dy + sin(angle) * (radius - 20),
      );

      // Arm
      final armPaint = Paint()
        ..color = i == 0 ? color : (i == 1 ? secondaryColor : AppColors.accentOrange)
        ..style = PaintingStyle.fill;

      final armPath = Path();
      armPath.addOval(Rect.fromCenter(center: armEnd, width: 60, height: 60));
      canvas.drawPath(armPath, armPaint);

      // Connection to center
      final connectionPaint = Paint()
        ..color = color.withAlpha(150)
        ..strokeWidth = 20
        ..style = PaintingStyle.stroke
        ..strokeCap = StrokeCap.round;

      canvas.drawLine(center, armEnd, connectionPaint);
    }

    // Center circle
    final centerPaint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, 30, centerPaint);

    // Inner circle
    final innerPaint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, 15, innerPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}


import 'dart:math' as math;
import 'package:flutter/material.dart';

enum ConversationWallpaper {
  none,
  calmOcean,
  auroraBorealis,
  breathingBubbles,
  softRain,
  deepFocus,
  warmSunset,
  starfield,
}

class WallpaperPainter extends CustomPainter {
  final ConversationWallpaper wallpaper;
  final double phase;
  final bool isDark;

  WallpaperPainter({
    required this.wallpaper,
    required this.phase,
    required this.isDark,
  });

  @override
  void paint(Canvas canvas, Size size) {
    switch (wallpaper) {
      case ConversationWallpaper.none:
        break;
      case ConversationWallpaper.calmOcean:
        _drawCalmOcean(canvas, size);
        break;
      case ConversationWallpaper.auroraBorealis:
        _drawAuroraBorealis(canvas, size);
        break;
      case ConversationWallpaper.breathingBubbles:
        _drawBreathingBubbles(canvas, size);
        break;
      case ConversationWallpaper.softRain:
        _drawSoftRain(canvas, size);
        break;
      case ConversationWallpaper.deepFocus:
        _drawDeepFocus(canvas, size);
        break;
      case ConversationWallpaper.warmSunset:
        _drawWarmSunset(canvas, size);
        break;
      case ConversationWallpaper.starfield:
        _drawStarfield(canvas, size);
        break;
    }
  }

  void _drawCalmOcean(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;

    final baseColors = isDark
        ? [const Color(0xFF0D1B2A), const Color(0xFF1B2838), const Color(0xFF0D253A)]
        : [const Color(0xFFE3F2FD), const Color(0xFFBBDEFB), const Color(0xFFE1F5FE)];

    final paint = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: baseColors,
      ).createShader(Rect.fromLTWH(0, 0, w, h));
    canvas.drawRect(Rect.fromLTWH(0, 0, w, h), paint);

    final wave1Alpha = isDark ? 0.15 : 0.12;
    final wave1Color = isDark ? const Color(0xFF42A5F5) : const Color(0xFF1976D2);
    final phaseOffset = phase * 2 * math.pi;

    final path1 = Path()..moveTo(0, h * 0.65);
    for (double x = 0; x <= w; x += 4) {
      final y = h * 0.65 + math.sin(x / w * 2 * math.pi + phaseOffset) * h * 0.04;
      path1.lineTo(x, y);
    }
    path1.lineTo(w, h);
    path1.lineTo(0, h);
    path1.close();
    canvas.drawPath(path1, Paint()..color = wave1Color.withValues(alpha: wave1Alpha));

    final wave2Color = isDark ? const Color(0xFF64B5F6) : const Color(0xFF42A5F5);
    final phaseOffset2 = phase * 2 * math.pi + math.pi * 0.7;
    final path2 = Path()..moveTo(0, h * 0.72);
    for (double x = 0; x <= w; x += 4) {
      final y = h * 0.72 + math.sin(x / w * 3 * math.pi + phaseOffset2) * h * 0.03;
      path2.lineTo(x, y);
    }
    path2.lineTo(w, h);
    path2.lineTo(0, h);
    path2.close();
    canvas.drawPath(path2, Paint()..color = wave2Color.withValues(alpha: wave1Alpha * 0.7));
  }

  void _drawAuroraBorealis(Canvas canvas, Size size) {
    final phaseRad = phase * 2 * math.pi;
    canvas.drawRect(Rect.fromLTWH(0, 0, size.width, size.height), Paint()..color = isDark ? const Color(0xFF0A0E1A) : const Color(0xFFF5F5F5));

    final band1Y = size.height * (0.15 + math.sin(phaseRad) * 0.08);
    final band1H = size.height * 0.25;
    final color1 = isDark ? const Color(0xFF00E676) : const Color(0xFF81C784);
    canvas.drawRect(
      Rect.fromLTWH(0, band1Y, size.width, band1H),
      Paint()..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [Colors.transparent, color1.withValues(alpha: 0.12), Colors.transparent],
      ).createShader(Rect.fromLTWH(0, band1Y, size.width, band1H)),
    );

    final band2Y = size.height * (0.30 + math.cos(phaseRad * 0.8) * 0.06);
    final band2H = size.height * 0.20;
    final color2 = isDark ? const Color(0xFF7C4DFF) : const Color(0xFFBA68C8);
    canvas.drawRect(
      Rect.fromLTWH(0, band2Y, size.width, band2H),
      Paint()..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [Colors.transparent, color2.withValues(alpha: 0.10), Colors.transparent],
      ).createShader(Rect.fromLTWH(0, band2Y, size.width, band2H)),
    );

    final band3Y = size.height * (0.50 + math.sin(phaseRad * 1.3) * 0.05);
    final band3H = size.height * 0.18;
    final color3 = isDark ? const Color(0xFF00BCD4) : const Color(0xFF80CBC4);
    canvas.drawRect(
      Rect.fromLTWH(0, band3Y, size.width, band3H),
      Paint()..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [Colors.transparent, color3.withValues(alpha: 0.08), Colors.transparent],
      ).createShader(Rect.fromLTWH(0, band3Y, size.width, band3H)),
    );
  }

  void _drawBreathingBubbles(Canvas canvas, Size size) {
    canvas.drawRect(Rect.fromLTWH(0, 0, size.width, size.height), Paint()..color = isDark ? const Color(0xFF1A1A2E) : const Color(0xFFF8F0FF));

    final scale = 0.7 + phase * 0.3;
    final alpha = 0.06 + phase * 0.08;

    final bubbles = [
      (0.25, 0.30, 0.12, isDark ? const Color(0xFF7C4DFF) : const Color(0xFFCE93D8)),
      (0.70, 0.20, 0.09, isDark ? const Color(0xFF536DFE) : const Color(0xFF90CAF9)),
      (0.50, 0.55, 0.14, isDark ? const Color(0xFF00BFA5) : const Color(0xFFA5D6A7)),
      (0.15, 0.75, 0.08, isDark ? const Color(0xFFFF80AB) : const Color(0xFFF8BBD0)),
      (0.80, 0.65, 0.10, isDark ? const Color(0xFF64B5F6) : const Color(0xFFBBDEFB)),
    ];

    for (final b in bubbles) {
      final r = b.$3 * size.width * scale;
      final center = Offset(b.$1 * size.width, b.$2 * size.height);
      canvas.drawCircle(center, r, Paint()..color = b.$4.withValues(alpha: alpha));
      canvas.drawCircle(center, r * 0.6, Paint()..color = b.$4.withValues(alpha: alpha * 0.5));
    }
  }

  void _drawSoftRain(Canvas canvas, Size size) {
    canvas.drawRect(Rect.fromLTWH(0, 0, size.width, size.height), Paint()..color = isDark ? const Color(0xFF1A1D23) : const Color(0xFFF0F4F8));
    final dropColor = isDark ? const Color(0xFF90CAF9) : const Color(0xFF64B5F6);

    for (int i = 0; i < 24; i++) {
      final seedX = (i * 0.618033988) % 1.0;
      final seedSpeed = 0.6 + (i % 5) * 0.1;
      final x = seedX * size.width;
      final baseY = ((i * 0.3141592 + phase * seedSpeed) % 1.0) * (size.height + 40) - 20;
      final radius = 1.5 + (i % 3) * 0.8;
      final alpha = isDark ? 0.20 : 0.15;

      canvas.drawCircle(Offset(x, baseY), radius, Paint()..color = dropColor.withValues(alpha: alpha - (i % 4) * 0.03));
    }
  }

  void _drawDeepFocus(Canvas canvas, Size size) {
    final colors = isDark
        ? [const Color(0xFF0D0D12), const Color(0xFF1A1A24), const Color(0xFF0D0D12)]
        : [const Color(0xFFF5F5F5), const Color(0xFFEEEEEE), const Color(0xFFE8E8E8)];
    canvas.drawRect(
      Rect.fromLTWH(0, 0, size.width, size.height),
      Paint()..shader = RadialGradient(
        colors: colors,
        center: const Alignment(0, -0.2), // Adjusted from 0.5, 0.4
        radius: 0.8,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height)),
    );
  }

  void _drawWarmSunset(Canvas canvas, Size size) {
    final phaseRad = phase * 2 * math.pi;
    final centerY = size.height * (0.35 + math.sin(phaseRad) * 0.1);
    final blend = (math.sin(phaseRad) + 1.0) / 2.0;

    final colors = isDark
        ? [
            Color.lerp(const Color(0xFF1A0A0A), const Color(0xFF2A1010), blend)!,
            Color.lerp(const Color(0xFF4A1A00), const Color(0xFF3A1500), blend)!,
            Color.lerp(const Color(0xFF2A0D1A), const Color(0xFF1A0A15), blend)!,
          ]
        : [
            Color.lerp(const Color(0xFFFFF3E0), const Color(0xFFFFECB3), blend)!,
            Color.lerp(const Color(0xFFFFCCBC), const Color(0xFFF8BBD0), blend)!,
            Color.lerp(const Color(0xFFFFE0B2), const Color(0xFFFFF9C4), blend)!,
          ];

    canvas.drawRect(
      Rect.fromLTWH(0, 0, size.width, size.height),
      Paint()..shader = RadialGradient(
        colors: colors,
        center: Alignment(0, (centerY / size.height) * 2 - 1),
        radius: 1.2,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height)),
    );
  }

  void _drawStarfield(Canvas canvas, Size size) {
    final bgColors = isDark
        ? [const Color(0xFF0A0A1A), const Color(0xFF0F0F2A), const Color(0xFF0A0A1A)]
        : [const Color(0xFFE8EAF6), const Color(0xFFE0E0F0), const Color(0xFFEDE7F6)];

    canvas.drawRect(
      Rect.fromLTWH(0, 0, size.width, size.height),
      Paint()..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: bgColors,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height)),
    );

    final starColor = isDark ? Colors.white : const Color(0xFF5C6BC0);

    for (int i = 0; i < 30; i++) {
      final x = ((i * 0.618033988 * 7.0) % 1.0) * size.width;
      final y = ((i * 0.381966 * 13.0) % 1.0) * size.height;
      final baseRadius = 0.8 + (i % 4) * 0.4;

      final twinkle = math.sin(phase * math.pi + i * 0.5);
      final alpha = isDark ? (0.3 + twinkle * 0.4) : (0.15 + twinkle * 0.2);

      canvas.drawCircle(
        Offset(x, y),
        baseRadius * (0.8 + twinkle * 0.2),
        Paint()..color = starColor.withValues(alpha: alpha.clamp(0.05, 0.9)),
      );
    }
  }

  @override
  bool shouldRepaint(WallpaperPainter oldDelegate) =>
      oldDelegate.wallpaper != wallpaper ||
      oldDelegate.phase != phase ||
      oldDelegate.isDark != isDark;
}

class ConversationWallpaperWidget extends StatefulWidget {
  final ConversationWallpaper wallpaper;
  final bool reducedMotion;

  const ConversationWallpaperWidget({
    super.key,
    required this.wallpaper,
    this.reducedMotion = false,
  });

  @override
  State<ConversationWallpaperWidget> createState() => _ConversationWallpaperWidgetState();
}

class _ConversationWallpaperWidgetState extends State<ConversationWallpaperWidget>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 8),
      vsync: this,
    );
    if (!widget.reducedMotion && widget.wallpaper != ConversationWallpaper.none) {
      _controller.repeat();
    }
  }

  @override
  void didUpdateWidget(ConversationWallpaperWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.reducedMotion != oldWidget.reducedMotion || widget.wallpaper != oldWidget.wallpaper) {
      if (widget.reducedMotion || widget.wallpaper == ConversationWallpaper.none) {
        _controller.stop();
      } else {
        _controller.repeat();
      }
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.wallpaper == ConversationWallpaper.none) return const SizedBox.shrink();

    final isDark = Theme.of(context).brightness == Brightness.dark;

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return CustomPaint(
          painter: WallpaperPainter(
            wallpaper: widget.wallpaper,
            phase: _controller.value,
            isDark: isDark,
          ),
          size: Size.infinite,
        );
      },
    );
  }
}

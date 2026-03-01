import 'package:flutter/material.dart';
import 'dart:math' as math;
import '../../models/custom_avatar.dart';
import '../../core/theme/app_colors.dart';

import 'avatar_renderer.dart';

/// Widget that renders a custom avatar based on CustomAvatar model
class CustomAvatarWidget extends StatelessWidget {
  final CustomAvatar avatar;
  final double size;
  final VoidCallback? onTap;
  final bool showBorder;
  final Color? borderColor;

  const CustomAvatarWidget({
    super.key,
    required this.avatar,
    this.size = 100,
    this.onTap,
    this.showBorder = false,
    this.borderColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final renderer = _CustomAvatarRenderer(avatar);
    final renderContext = AvatarRenderContext(
      viewportSize: Size.square(size),
      devicePixelRatio: MediaQuery.devicePixelRatioOf(context),
      animation: AvatarAnimationState(profile: null),
    );

    Widget avatarWidget = Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: _getShape(),
        borderRadius: _getBorderRadius(),
        color: avatar.backgroundColor.toColor(),
        border: showBorder
            ? Border.all(
                color: borderColor ?? theme.colorScheme.primary,
                width: 2,
              )
            : null,
      ),
      child: ClipPath(
        clipper: _AvatarClipper(avatar.shape),
        child: CustomPaint(
          size: Size(size, size),
          painter: _LayeredAvatarPainter(renderer: renderer, viewportSize: size, context: renderContext),
        ),
      ),
    );

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: avatarWidget,
      );
    }

    return avatarWidget;
  }

  BoxShape _getShape() {
    switch (avatar.shape) {
      case AvatarShape.circle:
        return BoxShape.circle;
      case AvatarShape.rounded:
      case AvatarShape.square:
        return BoxShape.rectangle;
    }
  }

  BorderRadius? _getBorderRadius() {
    switch (avatar.shape) {
      case AvatarShape.circle:
        return null;
      case AvatarShape.rounded:
        return BorderRadius.circular(size * 0.2);
      case AvatarShape.square:
        return BorderRadius.circular(size * 0.05);
    }
  }
}

/// Clips the avatar based on shape
class _AvatarClipper extends CustomClipper<Path> {
  final AvatarShape shape;

  _AvatarClipper(this.shape);

  @override
  Path getClip(Size size) {
    final path = Path();
    switch (shape) {
      case AvatarShape.circle:
        path.addOval(Rect.fromLTWH(0, 0, size.width, size.height));
        break;
      case AvatarShape.rounded:
        path.addRRect(RRect.fromRectAndRadius(
          Rect.fromLTWH(0, 0, size.width, size.height),
          Radius.circular(size.width * 0.2),
        ));
        break;
      case AvatarShape.square:
        path.addRRect(RRect.fromRectAndRadius(
          Rect.fromLTWH(0, 0, size.width, size.height),
          Radius.circular(size.width * 0.05),
        ));
        break;
    }
    return path;
  }

  @override
  bool shouldReclip(covariant CustomClipper<Path> oldClipper) => false;
}

/// Custom painter to draw the avatar face
class _LayeredAvatarPainter extends CustomPainter {
  final _CustomAvatarRenderer renderer;
  final double viewportSize;
  final AvatarRenderContext context;

  _LayeredAvatarPainter({
    required this.renderer,
    required this.viewportSize,
    required this.context,
  });

  @override
  void paint(Canvas canvas, Size size) {
    renderer.render(canvas, size, context);
  }

  @override
  bool shouldRepaint(covariant _LayeredAvatarPainter oldDelegate) =>
      oldDelegate.renderer.avatar != renderer.avatar ||
      oldDelegate.viewportSize != viewportSize;
}

class _CustomAvatarRenderer {
  final CustomAvatar avatar;
  late final CanvasCustomAvatarRenderer _renderer;

  _CustomAvatarRenderer(this.avatar) {
    _renderer = CanvasCustomAvatarRenderer(
      delegateBuilder: _buildDelegates,
    );
  }

  void render(Canvas canvas, Size size, AvatarRenderContext context) {
    final passes = _renderer.buildPasses(avatar, context.copyWith(viewportSize: size));
    for (final pass in passes..sort((a, b) => a.zIndex.compareTo(b.zIndex))) {
      canvas.saveLayer(null, Paint());
      pass.painter(canvas, size);
      canvas.restore();
    }
  }

  CustomAvatarPaintDelegates _buildDelegates(CustomAvatar avatar) {
    final painter = CustomAvatarBasePainter(avatar: avatar);
    return CustomAvatarPaintDelegates(
      paintFace: painter.paintFace,
      paintHair: painter.paintHair,
      paintEyesAndMouth: painter.paintEyesAndMouth,
      paintFacialHair: painter.paintFacialHair,
      paintAccessory: painter.paintAccessory,
    );
  }
}

/// Expose _AvatarPainter via a wrapper class and drop unused imports.
class CustomAvatarBasePainter extends CustomPainter {
  final CustomAvatar avatar;

  CustomAvatarBasePainter({required this.avatar});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;

    _drawFace(canvas, center, faceRadius);
    _drawHair(canvas, center, size, faceRadius);
    _drawEyes(canvas, center, faceRadius);
    _drawMouth(canvas, center, faceRadius);
    if (avatar.facialHair != null && avatar.facialHair != AvatarFacialHair.none) {
      _drawFacialHair(canvas, center, faceRadius);
    }
    if (avatar.accessory != null && avatar.accessory != AvatarAccessory.none) {
      _drawAccessory(canvas, center, faceRadius, size);
    }
  }

  void paintFace(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;
    _drawFace(canvas, center, faceRadius);
  }

  void paintHair(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;
    _drawHair(canvas, center, size, faceRadius);
  }

  void paintEyesAndMouth(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;
    _drawEyes(canvas, center, faceRadius);
    _drawMouth(canvas, center, faceRadius);
  }

  void paintFacialHair(Canvas canvas, Size size) {
    if (avatar.facialHair == null || avatar.facialHair == AvatarFacialHair.none) return;
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;
    _drawFacialHair(canvas, center, faceRadius);
  }

  void paintAccessory(Canvas canvas, Size size) {
    if (avatar.accessory == null || avatar.accessory == AvatarAccessory.none) return;
    final center = Offset(size.width / 2, size.height / 2);
    final faceRadius = size.width * 0.35;
    _drawAccessory(canvas, center, faceRadius, size);
  }

  void _drawFace(Canvas canvas, Offset center, double radius) {
    final facePaint = Paint()
      ..color = avatar.skinColor.toColor()
      ..style = PaintingStyle.fill;
    final faceCenter = Offset(center.dx, center.dy + radius * 0.15);
    canvas.drawOval(
      Rect.fromCenter(center: faceCenter, width: radius * 2, height: radius * 2.2),
      facePaint,
    );
  }

  void _drawHair(Canvas canvas, Offset center, Size size, double radius) {
    if (avatar.hairStyle == AvatarHairStyle.none) return;

    final hairPaint = Paint()
      ..color = avatar.hairColor.toColor()
      ..style = PaintingStyle.fill;

    final hairCenter = Offset(center.dx, center.dy - radius * 0.3);
    final hairWidth = radius * 2.2;

    switch (avatar.hairStyle) {
      case AvatarHairStyle.short:
        _drawShortHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.medium:
        _drawMediumHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.long:
        _drawLongHair(canvas, hairCenter, hairWidth, radius, hairPaint);
        break;
      case AvatarHairStyle.curly:
        _drawCurlyHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.wavy:
        _drawWavyHair(canvas, hairCenter, hairWidth, radius, hairPaint);
        break;
      case AvatarHairStyle.buzz:
        _drawBuzzHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.ponytail:
        _drawPonytailHair(canvas, hairCenter, hairWidth, radius, hairPaint);
        break;
      case AvatarHairStyle.bun:
        _drawBunHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.mohawk:
        _drawMohawkHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.afro:
        _drawAfroHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.spiky:
        _drawSpikyHair(canvas, hairCenter, hairWidth, hairPaint);
        break;
      case AvatarHairStyle.braids:
        _drawBraidsHair(canvas, hairCenter, hairWidth, radius, hairPaint);
        break;
      case AvatarHairStyle.none:
        break;
    }
  }

  void _drawBuzzHair(Canvas canvas, Offset center, double width, Paint paint) {
    final adjustedPaint = Paint()
      ..color = paint.color.withOpacity(0.7)
      ..style = PaintingStyle.fill;
    canvas.drawArc(
      Rect.fromCenter(center: center, width: width * 0.95, height: width * 0.7),
      math.pi,
      math.pi,
      true,
      adjustedPaint,
    );
  }

  void _drawPonytailHair(Canvas canvas, Offset center, double width, double faceRadius, Paint paint) {
    _drawShortHair(canvas, center, width, paint);
    final ponytailPath = Path();
    ponytailPath.addOval(
      Rect.fromCenter(
        center: Offset(center.dx, center.dy - faceRadius),
        width: width * 0.3,
        height: width * 0.5,
      ),
    );
    canvas.drawPath(ponytailPath, paint);
  }

  void _drawBunHair(Canvas canvas, Offset center, double width, Paint paint) {
    _drawShortHair(canvas, center, width, paint);
    canvas.drawCircle(
      Offset(center.dx, center.dy - width * 0.5),
      width * 0.2,
      paint,
    );
  }

  void _drawMohawkHair(Canvas canvas, Offset center, double width, Paint paint) {
    final path = Path();
    path.moveTo(center.dx - width * 0.1, center.dy + width * 0.1);
    path.lineTo(center.dx, center.dy - width * 0.7);
    path.lineTo(center.dx + width * 0.1, center.dy + width * 0.1);
    path.close();
    canvas.drawPath(path, paint);
  }

  void _drawAfroHair(Canvas canvas, Offset center, double width, Paint paint) {
    canvas.drawCircle(
      Offset(center.dx, center.dy - width * 0.1),
      width * 0.7,
      paint,
    );
  }

  void _drawSpikyHair(Canvas canvas, Offset center, double width, Paint paint) {
    final spikes = 7;
    for (int i = 0; i < spikes; i++) {
      final angle = math.pi + (i / (spikes - 1)) * math.pi;
      final path = Path();
      final baseX = center.dx + math.cos(angle) * width * 0.4;
      final baseY = center.dy + math.sin(angle) * width * 0.3;
      final tipX = center.dx + math.cos(angle) * width * 0.7;
      final tipY = center.dy + math.sin(angle) * width * 0.6 - width * 0.2;

      path.moveTo(baseX - width * 0.05, baseY);
      path.lineTo(tipX, tipY);
      path.lineTo(baseX + width * 0.05, baseY);
      path.close();
      canvas.drawPath(path, paint);
    }
    canvas.drawArc(
      Rect.fromCenter(center: center, width: width * 0.9, height: width * 0.6),
      math.pi,
      math.pi,
      true,
      paint,
    );
  }

  void _drawBraidsHair(Canvas canvas, Offset center, double width, double faceRadius, Paint paint) {
    _drawShortHair(canvas, center, width, paint);
    for (int side = -1; side <= 1; side += 2) {
      final braidX = center.dx + side * width * 0.45;
      for (int i = 0; i < 4; i++) {
        canvas.drawOval(
          Rect.fromCenter(
            center: Offset(braidX, center.dy + i * faceRadius * 0.4),
            width: width * 0.12,
            height: faceRadius * 0.35,
          ),
          paint,
        );
      }
    }
  }

  void _drawShortHair(Canvas canvas, Offset center, double width, Paint paint) {
    final path = Path();
    path.addArc(
      Rect.fromCenter(center: center, width: width, height: width * 0.8),
      math.pi,
      math.pi,
    );
    canvas.drawPath(path, paint);
  }

  void _drawMediumHair(Canvas canvas, Offset center, double width, Paint paint) {
    final path = Path();
    final rect = Rect.fromCenter(center: center, width: width * 1.1, height: width);
    path.addArc(rect, math.pi * 0.8, math.pi * 1.4);
    canvas.drawPath(path, paint);
  }

  void _drawLongHair(Canvas canvas, Offset center, double width, double faceRadius, Paint paint) {
    final path = Path();
    path.addArc(
      Rect.fromCenter(center: center, width: width * 1.2, height: width),
      math.pi * 0.7,
      math.pi * 1.6,
    );
    final leftSide = Offset(center.dx - width * 0.55, center.dy);
    final rightSide = Offset(center.dx + width * 0.55, center.dy);

    canvas.drawPath(path, paint);
    canvas.drawRect(
      Rect.fromPoints(
        leftSide,
        Offset(leftSide.dx + width * 0.15, center.dy + faceRadius * 2),
      ),
      paint,
    );
    canvas.drawRect(
      Rect.fromPoints(
        Offset(rightSide.dx - width * 0.15, rightSide.dy),
        Offset(rightSide.dx, center.dy + faceRadius * 2),
      ),
      paint,
    );
  }

  void _drawCurlyHair(Canvas canvas, Offset center, double width, Paint paint) {
    final random = math.Random(42);
    for (int i = 0; i < 20; i++) {
      final angle = (i / 20) * math.pi * 2;
      final radius = width * 0.4 + random.nextDouble() * width * 0.2;
      final x = center.dx + math.cos(angle) * radius * 0.5;
      final y = center.dy - width * 0.2 + math.sin(angle) * radius * 0.3;
      canvas.drawCircle(Offset(x, y), width * 0.15, paint);
    }
  }

  void _drawWavyHair(Canvas canvas, Offset center, double width, double faceRadius, Paint paint) {
    final path = Path();
    path.moveTo(center.dx - width * 0.5, center.dy);
    path.quadraticBezierTo(
      center.dx, center.dy - width * 0.6,
      center.dx + width * 0.5, center.dy,
    );
    path.quadraticBezierTo(
      center.dx + width * 0.6, center.dy + faceRadius,
      center.dx + width * 0.4, center.dy + faceRadius * 1.5,
    );
    path.lineTo(center.dx - width * 0.4, center.dy + faceRadius * 1.5);
    path.quadraticBezierTo(
      center.dx - width * 0.6, center.dy + faceRadius,
      center.dx - width * 0.5, center.dy,
    );
    path.close();
    canvas.drawPath(path, paint);
  }

  void _drawEyes(Canvas canvas, Offset center, double radius) {
    final eyeRadius = radius * 0.1;
    final eyeOffsetX = radius * 0.2;
    final eyeOffsetY = radius * 0.1;

    final leftEyeCenter = Offset(center.dx - eyeOffsetX, center.dy - eyeOffsetY);
    final rightEyeCenter = Offset(center.dx + eyeOffsetX, center.dy - eyeOffsetY);

    final eyePaint = Paint()
      ..color = avatar.eyeColor.toColor()
      ..style = PaintingStyle.fill;

    canvas.drawCircle(leftEyeCenter, eyeRadius, eyePaint);
    canvas.drawCircle(rightEyeCenter, eyeRadius, eyePaint);
  }

  void _drawMouth(Canvas canvas, Offset center, double radius) {
    final mouthWidth = radius * 0.4;
    final mouthHeight = radius * 0.1;

    final mouthRect = Rect.fromCenter(center: center, width: mouthWidth, height: mouthHeight);
    final mouthPaint = Paint()
      ..color = AppColors.calmPink
      ..style = PaintingStyle.fill;

    canvas.drawArc(mouthRect, 0, math.pi, false, mouthPaint);
  }

  void _drawFacialHair(Canvas canvas, Offset center, double radius) {
    final facialHair = avatar.facialHair;
    if (facialHair == null || facialHair == AvatarFacialHair.none) return;

    final facialHairPaint = Paint()
      ..color = (avatar.facialHairColor ?? avatar.hairColor).toColor()
      ..style = PaintingStyle.fill;

    final facialHairPath = Path();

    switch (facialHair) {
      case AvatarFacialHair.beard:
        facialHairPath.addOval(
          Rect.fromCenter(center: center, width: radius * 1.2, height: radius * 0.6),
        );
        break;
      case AvatarFacialHair.stubble:
        facialHairPath.addOval(
          Rect.fromCenter(center: center, width: radius * 1.1, height: radius * 0.5),
        );
        break;
      case AvatarFacialHair.fullBeard:
        facialHairPath.addOval(
          Rect.fromCenter(center: Offset(center.dx, center.dy + radius * 0.4), width: radius * 1.4, height: radius * 0.9),
        );
        break;
      case AvatarFacialHair.mustache:
        facialHairPath.addOval(
          Rect.fromCenter(center: center, width: radius * 0.8, height: radius * 0.4),
        );
        break;
      case AvatarFacialHair.goatee:
        facialHairPath.moveTo(center.dx, center.dy);
        facialHairPath.lineTo(center.dx - radius * 0.2, center.dy + radius * 0.2);
        facialHairPath.lineTo(center.dx + radius * 0.2, center.dy + radius * 0.2);
        facialHairPath.close();
        break;
      case AvatarFacialHair.none:
        break;
    }

    canvas.drawPath(facialHairPath, facialHairPaint);
  }

  void _drawAccessory(Canvas canvas, Offset center, double radius, Size size) {
    final accessory = avatar.accessory;
    if (accessory == null || accessory == AvatarAccessory.none) return;

    final accessoryPaint = Paint()
      ..color = (avatar.accessoryColor ?? '#000000').toColor()
      ..style = PaintingStyle.fill;

    switch (accessory) {
      case AvatarAccessory.glasses:
        final glassesWidth = radius * 0.8;
        final glassesHeight = radius * 0.2;
        final glassesRect = Rect.fromCenter(center: center, width: glassesWidth, height: glassesHeight);
        canvas.drawOval(glassesRect, accessoryPaint);
        break;
      case AvatarAccessory.hat:
        final hatPath = Path();
        hatPath.moveTo(center.dx - radius, center.dy - radius * 0.5);
        hatPath.lineTo(center.dx + radius, center.dy - radius * 0.5);
        hatPath.lineTo(center.dx + radius * 0.5, center.dy - radius);
        hatPath.lineTo(center.dx - radius * 0.5, center.dy - radius);
        hatPath.close();
        canvas.drawPath(hatPath, accessoryPaint);
        break;
      case AvatarAccessory.cap:
      case AvatarAccessory.beanie:
      case AvatarAccessory.headband:
      case AvatarAccessory.bow:
      case AvatarAccessory.earrings:
      case AvatarAccessory.headphones:
      case AvatarAccessory.sunglasses:
      case AvatarAccessory.none:
        break;
    }
  }

  @override
  bool shouldRepaint(covariant CustomAvatarBasePainter oldDelegate) {
    return avatar != oldDelegate.avatar;
  }
}

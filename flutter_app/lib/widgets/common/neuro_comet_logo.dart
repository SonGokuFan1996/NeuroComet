import 'dart:math' as math;
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/theme_provider.dart';

// ═══════════════════════════════════════════════════════════════
// HOLIDAY THEMING (Matching Android Version)
// ═══════════════════════════════════════════════════════════════

/// Holiday type enum for themed logo variations
enum HolidayType {
  none,
  newYear,           // January 1
  valentines,        // February 14
  stPatricks,        // March 17
  easter,            // Spring (varies)
  earthDay,          // April 22
  prideMonth,        // June
  independenceDay,   // July 4 (US)
  halloween,         // October 31
  thanksgiving,      // November (4th Thursday)
  hanukkah,          // Varies (Jewish calendar)
  christmas,         // December 25
  autismAwareness,   // April 2 - World Autism Awareness Day
  adhdAwareness,     // October - ADHD Awareness Month
  neurodiversity,    // Neurodiversity Celebration Week (March)
}

/// Detect current holiday based on date
HolidayType detectCurrentHoliday() {
  final now = DateTime.now();
  final month = now.month; // 1-12
  final day = now.day;
  final weekday = now.weekday; // 1 = Monday, 7 = Sunday
  final weekOfMonth = (day - 1) ~/ 7 + 1;

  // New Year's Day (Jan 1) or New Year's Eve (Dec 31)
  if ((month == 1 && day == 1) || (month == 12 && day == 31)) {
    return HolidayType.newYear;
  }

  // Valentine's Day (Feb 14) ± 1 day
  if (month == 2 && day >= 13 && day <= 15) {
    return HolidayType.valentines;
  }

  // St. Patrick's Day (March 17) ± 1 day
  if (month == 3 && day >= 16 && day <= 18) {
    return HolidayType.stPatricks;
  }

  // Neurodiversity Celebration Week (mid-March)
  if (month == 3 && day >= 13 && day <= 19) {
    return HolidayType.neurodiversity;
  }

  // World Autism Awareness Day (April 2)
  if (month == 4 && day == 2) {
    return HolidayType.autismAwareness;
  }

  // Earth Day (April 22)
  if (month == 4 && day == 22) {
    return HolidayType.earthDay;
  }

  // Easter season (approximate - late March to late April)
  if (month == 4 && day >= 1 && day <= 21) {
    return HolidayType.easter;
  }

  // Pride Month (June)
  if (month == 6) {
    return HolidayType.prideMonth;
  }

  // Independence Day (July 4) ± 1 day
  if (month == 7 && day >= 3 && day <= 5) {
    return HolidayType.independenceDay;
  }

  // ADHD Awareness Month (October first week)
  if (month == 10 && day >= 1 && day <= 7) {
    return HolidayType.adhdAwareness;
  }

  // Halloween (Oct 31) or week before
  if (month == 10 && day >= 24 && day <= 31) {
    return HolidayType.halloween;
  }

  // Thanksgiving (4th Thursday of November)
  if (month == 11 && weekday == DateTime.thursday && weekOfMonth == 4) {
    return HolidayType.thanksgiving;
  }

  // Hanukkah (approximate - late November to late December)
  if (month == 12 && day >= 1 && day <= 24) {
    return HolidayType.hanukkah;
  }

  // Christmas season (Dec 24-26)
  if (month == 12 && day >= 24 && day <= 26) {
    return HolidayType.christmas;
  }

  return HolidayType.none;
}

/// Get holiday-specific colors for the logo
List<Color> getHolidayColors(HolidayType holiday) {
  switch (holiday) {
    case HolidayType.newYear:
      return const [
        Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFFFFFFF),
        Color(0xFFFFD700), Color(0xFFCB6CE6), Color(0xFF4DABF5),
        Color(0xFFFFD700),
      ];
    case HolidayType.valentines:
      return const [
        Color(0xFFFF6B9D), Color(0xFFFF8FAB), Color(0xFFFFB3C6),
        Color(0xFFFF6B6B), Color(0xFFFF6B9D), Color(0xFFCB6CE6),
        Color(0xFFFF6B9D),
      ];
    case HolidayType.stPatricks:
      return const [
        Color(0xFF6B8E23), Color(0xFF8FBC8F), Color(0xFF98FB98),
        Color(0xFFD4AF37), Color(0xFF6B8E23), Color(0xFF8FBC8F),
      ];
    case HolidayType.easter:
      return const [
        Color(0xFFFFB6C1), Color(0xFFE6E6FA), Color(0xFF98FB98),
        Color(0xFFF0E68C), Color(0xFFB0E0E6), Color(0xFFFFB6C1),
      ];
    case HolidayType.earthDay:
      return const [
        Color(0xFF6B8E23), Color(0xFF6495ED), Color(0xFF8FBC8F),
        Color(0xFF5F9EA0), Color(0xFF6B8E23), Color(0xFF6495ED),
      ];
    case HolidayType.prideMonth:
      return const [
        Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFFE66D),
        Color(0xFF7BC67B), Color(0xFF4DABF5), Color(0xFFCB6CE6),
        Color(0xFFFF6B9D), Color(0xFFFF6B6B),
      ];
    case HolidayType.independenceDay:
      return const [
        Color(0xFFCD5C5C), Color(0xFFF5F5F5), Color(0xFF6495ED),
        Color(0xFFF5F5F5), Color(0xFFCD5C5C), Color(0xFF6495ED),
      ];
    case HolidayType.halloween:
      return const [
        Color(0xFFFF8C42), Color(0xFF2D2D2D), Color(0xFFCB6CE6),
        Color(0xFF7BC67B), Color(0xFFFF8C42), Color(0xFF9B59B6),
        Color(0xFFFF8C42),
      ];
    case HolidayType.thanksgiving:
      return const [
        Color(0xFFE9967A), Color(0xFFA0522D), Color(0xFFD4AF37),
        Color(0xFFBC8F8F), Color(0xFFE9967A), Color(0xFFA0522D),
      ];
    case HolidayType.hanukkah:
      return const [
        Color(0xFF6495ED), Color(0xFFF5F5F5), Color(0xFFB0C4DE),
        Color(0xFFD4AF37), Color(0xFF6495ED), Color(0xFFF5F5F5),
      ];
    case HolidayType.christmas:
      return const [
        Color(0xFFFF6B6B), Color(0xFF6BCB77), Color(0xFFFFD700),
        Color(0xFFFFFFFF), Color(0xFFFF6B6B), Color(0xFF6BCB77),
        Color(0xFFFFD700),
      ];
    case HolidayType.autismAwareness:
      return const [
        Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
        Color(0xFF4D96FF), Color(0xFFCB6CE6), Color(0xFFFF6B6B),
        Color(0xFFFFD93D),
      ];
    case HolidayType.adhdAwareness:
      return const [
        Color(0xFFFF8C42), Color(0xFFFFD93D), Color(0xFFFF6B6B),
        Color(0xFFFFAB4D), Color(0xFFFF8C42), Color(0xFFFFE66D),
        Color(0xFFFF8C42),
      ];
    case HolidayType.neurodiversity:
      return const [
        Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFFE66D),
        Color(0xFF7BC67B), Color(0xFF4DABF5), Color(0xFFCB6CE6),
        Color(0xFFFF6B9D), Color(0xFF7FDBDA), Color(0xFFFF6B6B),
      ];
    case HolidayType.none:
      return _professionalRainbowColors;
  }
}

/// Rainbow infinity symbol - the universal symbol for neurodiversity.
/// Professional, polished implementation matching the Android native version with:
/// - Smooth cubic bezier curves for elegant shape
/// - Organic flowing gradient animation
/// - Subtle breathing/pulse effect
/// - Soft multi-layered glow for depth
/// - Clean appearance when static
class NeuroCometLogo extends ConsumerWidget {
  final double size;
  final bool animated;
  final bool forceReducedMotion;
  final bool showGlow;

  const NeuroCometLogo({
    super.key,
    this.size = 100,
    this.animated = true,
    this.forceReducedMotion = false,
    this.showGlow = true,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reducedMotion = ref.watch(reducedMotionProvider);
    final shouldAnimate = animated && !forceReducedMotion && !reducedMotion;

    if (shouldAnimate) {
      return _AnimatedInfinityLogo(size: size, showGlow: showGlow);
    } else {
      return _StaticInfinityLogo(size: size, showGlow: showGlow);
    }
  }
}

/// Professional rainbow color palette - softer, more refined tones
const _professionalRainbowColors = [
  Color(0xFFE57373), // Soft coral red
  Color(0xFFFFB74D), // Warm amber
  Color(0xFFFFF176), // Gentle yellow
  Color(0xFF81C784), // Fresh green
  Color(0xFF64B5F6), // Sky blue
  Color(0xFFBA68C8), // Soft violet
  Color(0xFFF48FB1), // Rose pink
  Color(0xFFE57373), // Loop back to coral
];

/// Clean 3-color gradient for static logos
const _staticGradientColors = [
  Color(0xFF64B5F6), // Sky blue
  Color(0xFFBA68C8), // Soft violet
  Color(0xFF64B5F6), // Back to blue
];

/// Static rainbow infinity symbol for reduced motion preference
/// Uses a clean horizontal gradient for a polished, professional appearance
class _StaticInfinityLogo extends StatelessWidget {
  final double size;
  final bool showGlow;

  const _StaticInfinityLogo({required this.size, this.showGlow = true});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size * 0.5,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Subtle glow layer for depth
          if (showGlow)
            Opacity(
              opacity: 0.25,
              child: ImageFiltered(
                imageFilter: ui.ImageFilter.blur(sigmaX: 10, sigmaY: 10),
                child: CustomPaint(
                  size: Size(size * 1.05, size * 0.525),
                  painter: _ProfessionalInfinityPainter(
                    flowAngle: 0,
                    breatheScale: 1.0,
                    colors: _staticGradientColors,
                    strokeMultiplier: 0.16,
                    isAnimated: false,
                  ),
                ),
              ),
            ),
          // Main symbol with clean gradient
          CustomPaint(
            size: Size(size, size * 0.5),
            painter: _ProfessionalInfinityPainter(
              flowAngle: 0,
              breatheScale: 1.0,
              colors: _staticGradientColors,
              strokeMultiplier: 0.12,
              isAnimated: false,
            ),
          ),
        ],
      ),
    );
  }
}

/// Animated rainbow infinity symbol with flowing gradient and breathing effect
/// Professional animations with organic, natural movement
class _AnimatedInfinityLogo extends StatefulWidget {
  final double size;
  final bool showGlow;

  const _AnimatedInfinityLogo({required this.size, this.showGlow = true});

  @override
  State<_AnimatedInfinityLogo> createState() => _AnimatedInfinityLogoState();
}

class _AnimatedInfinityLogoState extends State<_AnimatedInfinityLogo>
    with TickerProviderStateMixin {
  late AnimationController _flowController;
  late AnimationController _breatheController;
  late AnimationController _glowController;

  late Animation<double> _breatheAnimation;
  late Animation<double> _glowAnimation;

  // Holiday theming
  late HolidayType _currentHoliday;
  late List<Color> _colors;

  @override
  void initState() {
    super.initState();

    // Detect holiday and get colors
    _currentHoliday = detectCurrentHoliday();
    _colors = getHolidayColors(_currentHoliday);

    // Flowing gradient animation - 10 seconds for smooth, organic flow
    _flowController = AnimationController(
      duration: const Duration(milliseconds: 10000),
      vsync: this,
    )..repeat();

    // Breathing animation - very subtle natural scale (6 seconds like Android)
    _breatheController = AnimationController(
      duration: const Duration(milliseconds: 6000),
      vsync: this,
    )..repeat(reverse: true);

    _breatheAnimation = Tween<double>(begin: 0.99, end: 1.01).animate(
      CurvedAnimation(parent: _breatheController, curve: Curves.easeInOut),
    );

    // Glow intensity pulse - subtle depth effect (5 seconds like Android)
    _glowController = AnimationController(
      duration: const Duration(milliseconds: 5000),
      vsync: this,
    )..repeat(reverse: true);

    _glowAnimation = Tween<double>(begin: 0.25, end: 0.4).animate(
      CurvedAnimation(parent: _glowController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _flowController.dispose();
    _breatheController.dispose();
    _glowController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([_flowController, _breatheController, _glowController]),
      builder: (context, child) {
        final flowAngle = _flowController.value * 360;
        final breatheScale = _breatheAnimation.value;
        final glowOpacity = _glowAnimation.value;

        final scaledWidth = widget.size * breatheScale;
        final scaledHeight = widget.size * 0.5 * breatheScale;

        return SizedBox(
          width: widget.size,
          height: widget.size * 0.5,
          child: Stack(
            alignment: Alignment.center,
            children: [
              // Outer soft glow layer
              if (widget.showGlow)
                Opacity(
                  opacity: glowOpacity * 0.5,
                  child: ImageFiltered(
                    imageFilter: ui.ImageFilter.blur(sigmaX: 16, sigmaY: 16),
                    child: CustomPaint(
                      size: Size(scaledWidth * 1.1, scaledHeight * 1.1),
                      painter: _ProfessionalInfinityPainter(
                        flowAngle: flowAngle,
                        breatheScale: 1.0,
                        colors: _colors,
                        strokeMultiplier: 0.22,
                        isAnimated: true,
                      ),
                    ),
                  ),
                ),
              // Inner glow layer for depth
              if (widget.showGlow)
                Opacity(
                  opacity: glowOpacity,
                  child: ImageFiltered(
                    imageFilter: ui.ImageFilter.blur(sigmaX: 6, sigmaY: 6),
                    child: CustomPaint(
                      size: Size(scaledWidth * 1.02, scaledHeight * 1.02),
                      painter: _ProfessionalInfinityPainter(
                        flowAngle: flowAngle,
                        breatheScale: 1.0,
                        colors: _colors,
                        strokeMultiplier: 0.16,
                        isAnimated: true,
                      ),
                    ),
                  ),
                ),
              // Main infinity symbol - crisp and clean
              CustomPaint(
                size: Size(scaledWidth, scaledHeight),
                painter: _ProfessionalInfinityPainter(
                  flowAngle: flowAngle,
                  breatheScale: 1.0,
                  colors: _colors,
                  strokeMultiplier: 0.12,
                  isAnimated: true,
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

/// Professional infinity symbol painter with smooth curves and organic gradient flow
class _ProfessionalInfinityPainter extends CustomPainter {
  final double flowAngle;
  final double breatheScale;
  final List<Color> colors;
  final double strokeMultiplier;
  final bool isAnimated;

  _ProfessionalInfinityPainter({
    required this.flowAngle,
    required this.breatheScale,
    required this.colors,
    required this.strokeMultiplier,
    required this.isAnimated,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final centerX = size.width / 2;
    final centerY = size.height / 2;

    // Proportions for elegant, balanced infinity shape
    final loopWidth = size.width * 0.35;
    final loopHeight = size.height * 0.38;
    final strokeWidth = size.height * strokeMultiplier;

    // Create smooth infinity path using cubic bezier curves
    // Curves are carefully tuned for a professional, balanced appearance
    final path = Path()
      // Start at center crossing point
      ..moveTo(centerX, centerY)
      // Right loop - upper arc (flowing outward and up)
      ..cubicTo(
        centerX + loopWidth * 0.5, centerY - loopHeight * 0.9,
        centerX + loopWidth * 0.95, centerY - loopHeight * 0.7,
        centerX + loopWidth, centerY,
      )
      // Right loop - lower arc (flowing back to center)
      ..cubicTo(
        centerX + loopWidth * 0.95, centerY + loopHeight * 0.7,
        centerX + loopWidth * 0.5, centerY + loopHeight * 0.9,
        centerX, centerY,
      )
      // Left loop - lower arc (crossing over, flowing outward)
      ..cubicTo(
        centerX - loopWidth * 0.5, centerY + loopHeight * 0.9,
        centerX - loopWidth * 0.95, centerY + loopHeight * 0.7,
        centerX - loopWidth, centerY,
      )
      // Left loop - upper arc (completing the symbol)
      ..cubicTo(
        centerX - loopWidth * 0.95, centerY - loopHeight * 0.7,
        centerX - loopWidth * 0.5, centerY - loopHeight * 0.9,
        centerX, centerY,
      );

    // Create gradient based on animation state
    Shader shader;
    if (isAnimated) {
      // Organic circular gradient flow - creates natural light reflection effect
      final angleRad = flowAngle * math.pi / 180;
      final secondaryAngleRad = (flowAngle + 120) * math.pi / 180;

      shader = LinearGradient(
        colors: colors,
        begin: Alignment(
          math.cos(angleRad) * 0.6,
          math.sin(angleRad) * 0.6,
        ),
        end: Alignment(
          math.cos(secondaryAngleRad) * 0.6,
          math.sin(secondaryAngleRad) * 0.6,
        ),
        tileMode: TileMode.clamp,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height));
    } else {
      // Clean horizontal gradient for static logo
      shader = LinearGradient(
        colors: colors,
        begin: Alignment.centerLeft,
        end: Alignment.centerRight,
      ).createShader(Rect.fromLTWH(0, 0, size.width, size.height));
    }

    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..shader = shader;

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(_ProfessionalInfinityPainter oldDelegate) {
    return oldDelegate.flowAngle != flowAngle ||
           oldDelegate.breatheScale != breatheScale ||
           oldDelegate.strokeMultiplier != strokeMultiplier;
  }
}

// ═══════════════════════════════════════════════════════════════
// NEUROCOMET BRAND LOGO (Infinity + Text)
// ═══════════════════════════════════════════════════════════════

/// Complete brand logo with rainbow infinity symbol and animated "NeuroComet" text
/// Matches the Android native implementation with:
/// - Rainbow infinity symbol (44dp default for proper visibility)
/// - Flashy animated text with shimmer and glow
/// - Breathing/scale animation
class NeuroCometBrandLogo extends ConsumerWidget {
  final double symbolSize;
  final bool animated;
  final bool compact;

  const NeuroCometBrandLogo({
    super.key,
    this.symbolSize = 44,
    this.animated = true,
    this.compact = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final reducedMotion = ref.watch(reducedMotionProvider);
    final shouldAnimate = animated && !reducedMotion;

    // Calculate text size based on symbol size for proper proportions
    final textSize = compact ? (symbolSize * 0.5) : (symbolSize * 0.6);

    return Row(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        NeuroCometLogo(
          size: symbolSize,
          animated: shouldAnimate,
          showGlow: shouldAnimate,
        ),
        SizedBox(width: compact ? 8 : 10),
        _FlashyNeuroCometText(
          animated: shouldAnimate,
          compact: compact,
          fontSize: textSize,
        ),
      ],
    );
  }
}

/// Animated "NeuroComet" text with shimmer gradient and glow effect
/// Matches Android's FlashyNeuroCometText composable
class _FlashyNeuroCometText extends StatefulWidget {
  final bool animated;
  final bool compact;
  final double fontSize;

  const _FlashyNeuroCometText({
    this.animated = true,
    this.compact = false,
    this.fontSize = 26,
  });

  @override
  State<_FlashyNeuroCometText> createState() => _FlashyNeuroCometTextState();
}

class _FlashyNeuroCometTextState extends State<_FlashyNeuroCometText>
    with TickerProviderStateMixin {
  AnimationController? _shimmerController;
  AnimationController? _scaleController;
  AnimationController? _glowController;

  Animation<double>? _shimmerAnimation;
  Animation<double>? _scaleAnimation;
  Animation<double>? _glowAnimation;

  // Holiday theming
  late HolidayType _currentHoliday;
  late List<Color> _gradientColors;

  @override
  void initState() {
    super.initState();

    // Detect holiday and get colors
    _currentHoliday = detectCurrentHoliday();
    _gradientColors = getHolidayColors(_currentHoliday);
    if (widget.animated) {
      _initAnimations();
    }
  }

  void _initAnimations() {
    // Shimmer animation - flows across text (8 seconds like Android)
    _shimmerController = AnimationController(
      duration: const Duration(milliseconds: 8000),
      vsync: this,
    )..repeat();

    _shimmerAnimation = Tween<double>(begin: -500, end: 500).animate(
      CurvedAnimation(parent: _shimmerController!, curve: Curves.linear),
    );

    // Subtle breathing scale animation (6 seconds like Android)
    _scaleController = AnimationController(
      duration: const Duration(milliseconds: 6000),
      vsync: this,
    )..repeat(reverse: true);

    _scaleAnimation = Tween<double>(begin: 1.0, end: 1.008).animate(
      CurvedAnimation(parent: _scaleController!, curve: Curves.easeInOut),
    );

    // Glow intensity pulse (5 seconds like Android)
    _glowController = AnimationController(
      duration: const Duration(milliseconds: 5000),
      vsync: this,
    )..repeat(reverse: true);

    _glowAnimation = Tween<double>(begin: 0.4, end: 0.55).animate(
      CurvedAnimation(parent: _glowController!, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _shimmerController?.dispose();
    _scaleController?.dispose();
    _glowController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final effectiveFontSize = widget.fontSize;

    if (!widget.animated) {
      // Static version with simple gradient
      return ShaderMask(
        shaderCallback: (bounds) {
          return const LinearGradient(
            colors: [Color(0xFF64B5F6), Color(0xFFBA68C8)],
          ).createShader(bounds);
        },
        child: Text(
          'NeuroComet',
          style: TextStyle(
            fontSize: effectiveFontSize,
            fontWeight: FontWeight.w700,
            letterSpacing: -0.5,
            color: Colors.white,
          ),
        ),
      );
    }

    return AnimatedBuilder(
      animation: Listenable.merge([
        _shimmerController!,
        _scaleController!,
        _glowController!,
      ]),
      builder: (context, child) {
        final shimmerOffset = _shimmerAnimation!.value;
        final scale = _scaleAnimation!.value;
        final glowIntensity = _glowAnimation!.value;
        final glowColor = _gradientColors[_gradientColors.length ~/ 2];

        return Transform.scale(
          scale: scale,
          child: Stack(
            children: [
              // Outer glow layer
              Text(
                'NeuroComet',
                style: TextStyle(
                  fontSize: effectiveFontSize,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.5,
                  color: Colors.transparent,
                  shadows: [
                    Shadow(
                      color: glowColor.withValues(alpha: glowIntensity * 0.3),
                      blurRadius: 16,
                    ),
                  ],
                ),
              ),
              // Inner shadow/glow layer
              Text(
                'NeuroComet',
                style: TextStyle(
                  fontSize: effectiveFontSize,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.5,
                  color: Colors.transparent,
                  shadows: [
                    Shadow(
                      color: glowColor.withValues(alpha: glowIntensity),
                      offset: const Offset(0, 1.5),
                      blurRadius: 6,
                    ),
                  ],
                ),
              ),
              // Main gradient text with shimmer
              ShaderMask(
                shaderCallback: (bounds) {
                  return LinearGradient(
                    colors: _gradientColors,
                    begin: Alignment(shimmerOffset / 500, 0),
                    end: Alignment((shimmerOffset + 600) / 500, 0),
                    tileMode: TileMode.repeated,
                  ).createShader(bounds);
                },
                child: Text(
                  'NeuroComet',
                  style: TextStyle(
                    fontSize: effectiveFontSize,
                    fontWeight: FontWeight.w700,
                    letterSpacing: -0.5,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}


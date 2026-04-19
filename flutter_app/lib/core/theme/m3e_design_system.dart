import 'package:flutter/material.dart';

/// Material 3 Expressive (M3E) Design System tokens for NeuroComet.
/// Centralizes shapes, spacing, and animation durations to ensure 1:1 parity with Android.
class M3EDesignSystem {
  M3EDesignSystem._();

  // ─── Shapes ───
  static const double radiusExtraSmall = 4.0;
  static const double radiusSmall = 8.0;
  static const double radiusMedium = 16.0;
  static const double radiusLarge = 24.0;
  static const double radiusExtraLarge = 28.0;
  static const double radiusFull = 1000.0;

  static final BorderRadius shapeExtraSmall = BorderRadius.circular(radiusExtraSmall);
  static final BorderRadius shapeSmall = BorderRadius.circular(radiusSmall);
  static final BorderRadius shapeMedium = BorderRadius.circular(radiusMedium);
  static final BorderRadius shapeLarge = BorderRadius.circular(radiusLarge);
  static final BorderRadius shapeExtraLarge = BorderRadius.circular(radiusExtraLarge);
  static final BorderRadius shapePill = BorderRadius.circular(radiusFull);

  static final BorderRadius shapeBubblyCard = BorderRadius.circular(20.0);
  static final BorderRadius shapeDialog = shapeExtraLarge;
  static const BorderRadius shapeBottomSheet = BorderRadius.vertical(
    top: Radius.circular(radiusExtraLarge),
  );

  // ─── Spacing ───
  static const double spacingXXS = 4.0;
  static const double spacingXS = 8.0;
  static const double spacingSM = 12.0;
  static const double spacingMD = 16.0;
  static const double spacingLG = 20.0;
  static const double spacingXL = 24.0;
  static const double spacingXXL = 32.0;

  /// Standard horizontal padding for screen-level content (matches Android M3EDesignSystem.Spacing.screenHorizontal).
  static const double screenHorizontal = 20.0;

  /// Extra bottom padding to clear the bottom navigation bar (matches Android M3EDesignSystem.Spacing.bottomNavPadding).
  static const double bottomNavClearance = 100.0;

  // ─── Animation Durations ───
  static const Duration animInstant = Duration(milliseconds: 50);
  static const Duration animFast = Duration(milliseconds: 150);
  static const Duration animNormal = Duration(milliseconds: 300);
  static const Duration animSlow = Duration(milliseconds: 450);
  static const Duration animSlower = Duration(milliseconds: 600);

  static const Duration animLike = Duration(milliseconds: 200);
  static const Duration animStaggerDelay = Duration(milliseconds: 50);
  static const Duration animBreathe = Duration(milliseconds: 2000);
  static const Duration animRainbowCycle = Duration(milliseconds: 10000);

  // ─── Avatar Sizes ───
  static const double avatarXS = 24.0;
  static const double avatarSM = 32.0;
  static const double avatarMD = 40.0;
  static const double avatarLG = 48.0;
  static const double avatarXL = 64.0;
  static const double avatarXXL = 96.0;

  static const double avatarPostCard = 44.0;
  static const double avatarStory = 64.0;
  static const double avatarComment = 32.0;
}

/// Extended M3E Color Palette
class M3EColors {
  M3EColors._();

  static const Color primaryPurple = Color(0xFF7C4DFF);
  static const Color secondaryTeal = Color(0xFF00BFA5);
  static const Color accentOrange = Color(0xFFFF6E40);

  static const Color calmBlue = Color(0xFF64B5F6);
  static const Color calmGreen = Color(0xFF81C784);
  static const Color calmPink = Color(0xFFF48FB1);
  static const Color calmYellow = Color(0xFFFFD54F);
  static const Color calmLavender = Color(0xFFCE93D8);

  static const List<Color> rainbowColors = [
    Color(0xFFE57373),
    Color(0xFFFFB74D),
    Color(0xFFFFF176),
    Color(0xFF81C784),
    Color(0xFF64B5F6),
    Color(0xFFBA68C8),
    Color(0xFFF48FB1),
    Color(0xFFE57373),
  ];

  static const List<Color> vibrantRainbowColors = [
    Color(0xFFFF6B6B),
    Color(0xFFFFAB4D),
    Color(0xFFFFE66D),
    Color(0xFF7BC67B),
    Color(0xFF4DABF5),
    Color(0xFFCB6CE6),
    Color(0xFFFF6B9D),
    Color(0xFFFF6B6B),
  ];
}

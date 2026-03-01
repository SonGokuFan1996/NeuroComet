import 'package:flutter/material.dart';

/// App color palette for NeuroComet
/// Designed with neurodivergent users in mind - calming colors with good contrast
class AppColors {
  AppColors._();

  // Primary Colors
  static const Color primaryPurple = Color(0xFF7C4DFF);
  static const Color primaryPurpleLight = Color(0xFFB47CFF);
  static const Color primaryPurpleDark = Color(0xFF5C00E6);

  // Secondary Colors
  static const Color secondaryTeal = Color(0xFF00BFA5);
  static const Color secondaryTealLight = Color(0xFF5DF2D6);
  static const Color secondaryTealDark = Color(0xFF008E76);

  // Accent Colors
  static const Color accentOrange = Color(0xFFFF6E40);
  static const Color accentOrangeLight = Color(0xFFFFA06D);
  static const Color accentOrangeDark = Color(0xFFE64A19);

  // Background Colors - Light Theme
  static const Color backgroundLight = Color(0xFFF8F9FD);
  static const Color surfaceLight = Color(0xFFFFFFFF);
  static const Color surfaceVariantLight = Color(0xFFF1F3F9);

  // Background Colors - Dark Theme
  static const Color backgroundDark = Color(0xFF121218);
  static const Color surfaceDark = Color(0xFF1E1E26);
  static const Color surfaceVariantDark = Color(0xFF2A2A36);

  // Text Colors - Light Theme
  static const Color textPrimaryLight = Color(0xFF1A1A2E);
  static const Color textSecondaryLight = Color(0xFF6B7280);
  static const Color textTertiaryLight = Color(0xFF9CA3AF);

  // Text Colors - Dark Theme
  static const Color textPrimaryDark = Color(0xFFF9FAFB);
  static const Color textSecondaryDark = Color(0xFFD1D5DB);
  static const Color textTertiaryDark = Color(0xFF9CA3AF);

  // Border Colors
  static const Color borderLight = Color(0xFFE5E7EB);
  static const Color borderDark = Color(0xFF374151);

  // Status Colors
  static const Color success = Color(0xFF10B981);
  static const Color successLight = Color(0xFF34D399);
  static const Color warning = Color(0xFFF59E0B);
  static const Color warningLight = Color(0xFFFBBF24);
  static const Color error = Color(0xFFEF4444);
  static const Color errorLight = Color(0xFFF87171);
  static const Color info = Color(0xFF3B82F6);
  static const Color infoLight = Color(0xFF60A5FA);

  // Neurodivergent-Friendly Colors (Calming palette)
  static const Color calmBlue = Color(0xFF64B5F6);
  static const Color calmGreen = Color(0xFF81C784);
  static const Color calmPink = Color(0xFFF48FB1);
  static const Color calmYellow = Color(0xFFFFD54F);
  static const Color calmLavender = Color(0xFFCE93D8);

  // Focus Mode Colors (reduced stimulation)
  static const Color focusModeBackground = Color(0xFFF5F5F0);
  static const Color focusModeBackgroundDark = Color(0xFF1A1A1A);

  // Category Colors
  static const Color categoryADHD = Color(0xFFFF7043);
  static const Color categoryAutism = Color(0xFF42A5F5);
  static const Color categoryDyslexia = Color(0xFF66BB6A);
  static const Color categoryAnxiety = Color(0xFFAB47BC);
  static const Color categoryDepression = Color(0xFF5C6BC0);
  static const Color categoryOCD = Color(0xFF26A69A);
  static const Color categoryBipolar = Color(0xFFFFCA28);
  static const Color categoryGeneral = Color(0xFF78909C);

  // Gradient presets
  static const LinearGradient primaryGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [primaryPurple, secondaryTeal],
  );

  static const LinearGradient warmGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [accentOrange, Color(0xFFFF8A65)],
  );

  static const LinearGradient calmGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [calmBlue, calmLavender],
  );

  static const LinearGradient nightGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF1A1A2E), Color(0xFF16213E)],
  );

  /// Get category color by name
  static Color getCategoryColor(String category) {
    switch (category.toLowerCase()) {
      case 'adhd':
        return categoryADHD;
      case 'autism':
      case 'asd':
        return categoryAutism;
      case 'dyslexia':
        return categoryDyslexia;
      case 'anxiety':
        return categoryAnxiety;
      case 'depression':
        return categoryDepression;
      case 'ocd':
        return categoryOCD;
      case 'bipolar':
        return categoryBipolar;
      default:
        return categoryGeneral;
    }
  }
}


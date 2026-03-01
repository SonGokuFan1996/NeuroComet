import 'package:flutter/material.dart';
import 'neuro_state.dart';

/// Neurodivergent-friendly theming engine for the NeuroComet application.
///
/// This file contains:
/// - Color schemes designed for various neurodivergent needs (ADHD, Autism, Anxiety, etc.)
/// - Mood-based themes that adapt to how the user is feeling
/// - Accessibility-focused themes (Dyslexia-friendly, High Contrast)
///
/// Each color palette is carefully designed to:
/// - Reduce sensory overwhelm when needed
/// - Provide appropriate stimulation for focus
/// - Maintain WCAG accessibility standards
/// - Support both light and dark modes

/// Represents a complete color scheme for both light and dark modes
class NeuroColorScheme {
  final ColorScheme light;
  final ColorScheme dark;

  const NeuroColorScheme({required this.light, required this.dark});
}

// ============================================================================
// NEURODIVERGENT-FRIENDLY COLOR PALETTES
// Each palette is designed with specific sensory and cognitive needs in mind
// ============================================================================

// --- BASIC THEMES ---

/// Default Theme - Balanced, neutral colors
const _lightDefaultColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF5C6BC0),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFE8EAF6),
  onPrimaryContainer: Color(0xFF1A237E),
  secondary: Color(0xFF7E57C2),
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFEDE7F6),
  onSecondaryContainer: Color(0xFF311B92),
  tertiary: Color(0xFF26A69A),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB3261E),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1C1B1F),
  surfaceContainerHighest: Color(0xFFE7E0EC),
  onSurfaceVariant: Color(0xFF49454F),
  outline: Color(0xFF79747E),
);

const _darkDefaultColorScheme = ColorScheme(
  brightness: Brightness.dark,
  // Match Android's NeurodivergentDarkColorScheme
  primary: Color(0xFFD0BCFF),           // Soft purple for dark mode
  onPrimary: Color(0xFF381E72),
  primaryContainer: Color(0xFF4F378B),
  onPrimaryContainer: Color(0xFFEADDFF),
  secondary: Color(0xFFCCC2DC),
  onSecondary: Color(0xFF332D41),
  secondaryContainer: Color(0xFF4A4458),
  onSecondaryContainer: Color(0xFFE8DEF8),
  tertiary: Color(0xFFEFB8C8),          // Warm accent for dark
  onTertiary: Color(0xFF492532),
  tertiaryContainer: Color(0xFF633B48),
  onTertiaryContainer: Color(0xFFFFD8E4),
  error: Color(0xFFF2B8B5),
  onError: Color(0xFF601410),
  // Key dark mode colors matching Android
  surface: Color(0xFF1C1B1F),           // Android's dark surface
  onSurface: Color(0xFFE6E1E5),
  surfaceContainerHighest: Color(0xFF49454F),
  onSurfaceVariant: Color(0xFFCAC4D0),
  outline: Color(0xFF938F99),
);

/// Hyperfocus Theme - High contrast, minimal distractions, focus-enhancing blues
const _lightHyperfocusColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF1565C0),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFF90CAF9),
  onPrimaryContainer: Color(0xFF0D47A1),
  secondary: Color(0xFF0277BD),
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFF81D4FA),
  onSecondaryContainer: Color(0xFF01579B),
  tertiary: Color(0xFF00838F),
  onTertiary: Color(0xFFFFFFFF),
  tertiaryContainer: Color(0xFF80DEEA),
  onTertiaryContainer: Color(0xFF006064),
  error: Color(0xFFC62828),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF0D1627),
  surfaceContainerHighest: Color(0xFFE3EDF5),
  onSurfaceVariant: Color(0xFF2A364A),
  outline: Color(0xFF5C7A99),
);

const _darkHyperfocusColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF64B5F6),
  onPrimary: Color(0xFF0A1929),
  primaryContainer: Color(0xFF1565C0),
  onPrimaryContainer: Color(0xFFBBDEFB),
  secondary: Color(0xFF4FC3F7),
  onSecondary: Color(0xFF01579B),
  secondaryContainer: Color(0xFF0277BD),
  onSecondaryContainer: Color(0xFFB3E5FC),
  tertiary: Color(0xFF4DD0E1),
  onTertiary: Color(0xFF006064),
  tertiaryContainer: Color(0xFF00838F),
  onTertiaryContainer: Color(0xFFB2EBF2),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF102A43),
  onSurface: Color(0xFFE3F2FD),
  surfaceContainerHighest: Color(0xFF1B3A52),
  onSurfaceVariant: Color(0xFFB3D4F0),
  outline: Color(0xFF5C8AB3),
);

/// Overload/Quiet Theme - Muted, desaturated, gentle colors for sensory overwhelm
const _lightOverloadColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF607D8B),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF78909C),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF90A4AE),
  onTertiary: Color(0xFF263238),
  error: Color(0xFF8D6E63),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFAFAFA),
  onSurface: Color(0xFF37474F),
  surfaceContainerHighest: Color(0xFFECEFF1),
  onSurfaceVariant: Color(0xFF546E7A),
  outline: Color(0xFFB0BEC5),
);

const _darkOverloadColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF90A4AE),
  onPrimary: Color(0xFF263238),
  secondary: Color(0xFFB0BEC5),
  onSecondary: Color(0xFF37474F),
  tertiary: Color(0xFFCFD8DC),
  onTertiary: Color(0xFF455A64),
  error: Color(0xFFBCAAA4),
  onError: Color(0xFF3E2723),
  surface: Color(0xFF252B2E),
  onSurface: Color(0xFFCFD8DC),
  surfaceContainerHighest: Color(0xFF37474F),
  onSurfaceVariant: Color(0xFFB0BEC5),
  outline: Color(0xFF78909C),
);

/// Calm Theme - Soft greens and blues, nature-inspired for relaxation
const _lightCalmColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF4CAF50),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFA5D6A7),
  onPrimaryContainer: Color(0xFF1B5E20),
  secondary: Color(0xFF66BB6A),
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFB9F6CA),
  onSecondaryContainer: Color(0xFF2E7D32),
  tertiary: Color(0xFF81C784),
  onTertiary: Color(0xFF1B5E20),
  tertiaryContainer: Color(0xFF81C784),
  onTertiaryContainer: Color(0xFF1B5E20),
  error: Color(0xFFE57373),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFDE7),
  onSurface: Color(0xFF33691E),
  surfaceContainerHighest: Color(0xFFDCEDC8),
  onSurfaceVariant: Color(0xFF558B2F),
  outline: Color(0xFFA5D6A7),
);

const _darkCalmColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF81C784),
  onPrimary: Color(0xFF1B5E20),
  primaryContainer: Color(0xFF2E7D32),
  onPrimaryContainer: Color(0xFFC8E6C9),
  secondary: Color(0xFFA5D6A7),
  onSecondary: Color(0xFF2E7D32),
  secondaryContainer: Color(0xFF388E3C),
  onSecondaryContainer: Color(0xFFB9F6CA),
  tertiary: Color(0xFFC8E6C9),
  onTertiary: Color(0xFF388E3C),
  tertiaryContainer: Color(0xFF43A047),
  onTertiaryContainer: Color(0xFFA5D6A7),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF1A2E1A),
  onSurface: Color(0xFFC8E6C9),
  surfaceContainerHighest: Color(0xFF2E4A2E),
  onSurfaceVariant: Color(0xFFA5D6A7),
  outline: Color(0xFF66BB6A),
);

// --- ADHD THEMES ---

/// ADHD Energized - Bright, engaging colors for productive high-energy days
const _lightADHDEnergizedColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFFFF6D00),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFFFFAB00),
  onSecondary: Color(0xFF3E2723),
  tertiary: Color(0xFFFFD600),
  onTertiary: Color(0xFF3E2723),
  error: Color(0xFFD84315),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF3E2723),
  surfaceContainerHighest: Color(0xFFFFECB3),
  onSurfaceVariant: Color(0xFF5D4037),
  outline: Color(0xFFFFCC80),
);

const _darkADHDEnergizedColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFFFAB40),
  onPrimary: Color(0xFF3E2723),
  secondary: Color(0xFFFFD54F),
  onSecondary: Color(0xFF3E2723),
  tertiary: Color(0xFFFFE082),
  onTertiary: Color(0xFF5D4037),
  error: Color(0xFFFF8A65),
  onError: Color(0xFF3E2723),
  surface: Color(0xFF2A2015),
  onSurface: Color(0xFFFFE0B2),
  surfaceContainerHighest: Color(0xFF3E2F20),
  onSurfaceVariant: Color(0xFFFFCC80),
  outline: Color(0xFFFFB74D),
);

/// ADHD Low Dopamine - Warm, stimulating colors to boost mood and motivation
const _lightADHDLowDopamineColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFFE91E63),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFFF48FB1),
  onSecondary: Color(0xFF880E4F),
  tertiary: Color(0xFFCE93D8),
  onTertiary: Color(0xFF4A148C),
  error: Color(0xFFD32F2F),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF4A0E24),
  surfaceContainerHighest: Color(0xFFF8BBD9),
  onSurfaceVariant: Color(0xFF880E4F),
  outline: Color(0xFFF48FB1),
);

const _darkADHDLowDopamineColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFF48FB1),
  onPrimary: Color(0xFF880E4F),
  secondary: Color(0xFFF8BBD9),
  onSecondary: Color(0xFFC2185B),
  tertiary: Color(0xFFE1BEE7),
  onTertiary: Color(0xFF7B1FA2),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF2A1520),
  onSurface: Color(0xFFF8BBD9),
  surfaceContainerHighest: Color(0xFF4A2030),
  onSurfaceVariant: Color(0xFFF48FB1),
  outline: Color(0xFFEC407A),
);

/// ADHD Task Mode - Minimal distractions, focus-enhancing neutral palette
const _lightADHDTaskModeColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF455A64),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF546E7A),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF607D8B),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF263238),
  surfaceContainerHighest: Color(0xFFECEFF1),
  onSurfaceVariant: Color(0xFF455A64),
  outline: Color(0xFFB0BEC5),
);

const _darkADHDTaskModeColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF90A4AE),
  onPrimary: Color(0xFF263238),
  secondary: Color(0xFFB0BEC5),
  onSecondary: Color(0xFF37474F),
  tertiary: Color(0xFFCFD8DC),
  onTertiary: Color(0xFF455A64),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF1E2428),
  onSurface: Color(0xFFCFD8DC),
  surfaceContainerHighest: Color(0xFF2E3840),
  onSurfaceVariant: Color(0xFFB0BEC5),
  outline: Color(0xFF78909C),
);

// --- AUTISM THEMES ---

/// Autism Routine - Predictable, consistent colors with clear visual hierarchy
const _lightAutismRoutineColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF3949AB),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF5C6BC0),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF7986CB),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1A237E),
  surfaceContainerHighest: Color(0xFFC5CAE9),
  onSurfaceVariant: Color(0xFF303F9F),
  outline: Color(0xFF9FA8DA),
);

const _darkAutismRoutineColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF9FA8DA),
  onPrimary: Color(0xFF1A237E),
  secondary: Color(0xFFC5CAE9),
  onSecondary: Color(0xFF283593),
  tertiary: Color(0xFFE8EAF6),
  onTertiary: Color(0xFF3949AB),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF151A2E),
  onSurface: Color(0xFFC5CAE9),
  surfaceContainerHighest: Color(0xFF232B45),
  onSurfaceVariant: Color(0xFF9FA8DA),
  outline: Color(0xFF5C6BC0),
);

/// Autism Sensory Seeking - Rich, satisfying contrasts and engaging colors
const _lightAutismSensorySeekColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF7B1FA2),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF9C27B0),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF00BCD4),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFD32F2F),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF311B92),
  surfaceContainerHighest: Color(0xFFE1BEE7),
  onSurfaceVariant: Color(0xFF6A1B9A),
  outline: Color(0xFFCE93D8),
);

const _darkAutismSensorySeekColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFCE93D8),
  onPrimary: Color(0xFF4A148C),
  secondary: Color(0xFFE1BEE7),
  onSecondary: Color(0xFF6A1B9A),
  tertiary: Color(0xFF80DEEA),
  onTertiary: Color(0xFF006064),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF1F152A),
  onSurface: Color(0xFFE1BEE7),
  surfaceContainerHighest: Color(0xFF32204A),
  onSurfaceVariant: Color(0xFFCE93D8),
  outline: Color(0xFFAB47BC),
);

/// Autism Low Stimulation - Very muted, gentle colors for sensory rest
const _lightAutismLowStimColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF8D9EAB),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFCFD8DC),
  onPrimaryContainer: Color(0xFF37474F),
  secondary: Color(0xFFA0B0BD),
  onSecondary: Color(0xFF37474F),
  secondaryContainer: Color(0xFFB0BEC5),
  onSecondaryContainer: Color(0xFF455A64),
  tertiary: Color(0xFFB3C2CF),
  onTertiary: Color(0xFF455A64),
  tertiaryContainer: Color(0xFFCFD8DC),
  onTertiaryContainer: Color(0xFF546E7A),
  error: Color(0xFFA1887F),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFCFCFC),
  onSurface: Color(0xFF455A64),
  surfaceContainerHighest: Color(0xFFEFF1F3),
  onSurfaceVariant: Color(0xFF607D8B),
  outline: Color(0xFFCFD8DC),
);

const _darkAutismLowStimColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFB0BEC5),
  onPrimary: Color(0xFF37474F),
  primaryContainer: Color(0xFF546E7A),
  onPrimaryContainer: Color(0xFFCFD8DC),
  secondary: Color(0xFFCFD8DC),
  onSecondary: Color(0xFF455A64),
  secondaryContainer: Color(0xFF607D8B),
  onSecondaryContainer: Color(0xFFECEFF1),
  tertiary: Color(0xFFECEFF1),
  onTertiary: Color(0xFF546E7A),
  tertiaryContainer: Color(0xFF78909C),
  onTertiaryContainer: Color(0xFFCFD8DC),
  error: Color(0xFFBCAAA4),
  onError: Color(0xFF3E2723),
  surface: Color(0xFF1E2225),
  onSurface: Color(0xFFCFD8DC),
  surfaceContainerHighest: Color(0xFF2C3135),
  onSurfaceVariant: Color(0xFFB0BEC5),
  outline: Color(0xFF78909C),
);

// --- ANXIETY/OCD THEMES ---

/// Anxiety Soothe - Cool, reassuring blues to ease worry
const _lightAnxietySootheColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF039BE5),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF4FC3F7),
  onSecondary: Color(0xFF01579B),
  tertiary: Color(0xFF81D4FA),
  onTertiary: Color(0xFF0277BD),
  error: Color(0xFFE57373),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF01579B),
  surfaceContainerHighest: Color(0xFFB3E5FC),
  onSurfaceVariant: Color(0xFF0288D1),
  outline: Color(0xFF4FC3F7),
);

const _darkAnxietySootheColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF4FC3F7),
  onPrimary: Color(0xFF01579B),
  secondary: Color(0xFF81D4FA),
  onSecondary: Color(0xFF0277BD),
  tertiary: Color(0xFFB3E5FC),
  onTertiary: Color(0xFF0288D1),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF0A2030),
  onSurface: Color(0xFFB3E5FC),
  surfaceContainerHighest: Color(0xFF0F2D45),
  onSurfaceVariant: Color(0xFF81D4FA),
  outline: Color(0xFF29B6F6),
);

/// Anxiety Grounding - Earthy, stable colors for centering and grounding
const _lightAnxietyGroundingColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF6D4C41),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF8D6E63),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF795548),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF3E2723),
  surfaceContainerHighest: Color(0xFFD7CCC8),
  onSurfaceVariant: Color(0xFF5D4037),
  outline: Color(0xFFBCAAA4),
);

const _darkAnxietyGroundingColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFBCAAA4),
  onPrimary: Color(0xFF3E2723),
  secondary: Color(0xFFD7CCC8),
  onSecondary: Color(0xFF4E342E),
  tertiary: Color(0xFFEFEBE9),
  onTertiary: Color(0xFF5D4037),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF2A2220),
  onSurface: Color(0xFFD7CCC8),
  surfaceContainerHighest: Color(0xFF3E3230),
  onSurfaceVariant: Color(0xFFBCAAA4),
  outline: Color(0xFF8D6E63),
);

// --- DYSLEXIA FRIENDLY THEME ---

/// Dyslexia Friendly - High readability with optimal contrast, warm background
const _lightDyslexiaFriendlyColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF1565C0),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF0D47A1),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF283593),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFF8E7), // Cream background reduces visual stress
  onSurface: Color(0xFF212121),
  surfaceContainerHighest: Color(0xFFF5F0E0),
  onSurfaceVariant: Color(0xFF424242),
  outline: Color(0xFF757575),
);

const _darkDyslexiaFriendlyColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF90CAF9),
  onPrimary: Color(0xFF0D47A1),
  secondary: Color(0xFF64B5F6),
  onSecondary: Color(0xFF1565C0),
  tertiary: Color(0xFF42A5F5),
  onTertiary: Color(0xFF1976D2),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF252218), // Warm dark background
  onSurface: Color(0xFFF5F0E0),
  surfaceContainerHighest: Color(0xFF3A3528),
  onSurfaceVariant: Color(0xFFE0D8C8),
  outline: Color(0xFF9E9E9E),
);

// --- MOOD-BASED THEMES ---

/// Mood Tired - Gentle colors that don't strain the eyes
const _lightMoodTiredColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF7986CB),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF9FA8DA),
  onSecondary: Color(0xFF303F9F),
  tertiary: Color(0xFFC5CAE9),
  onTertiary: Color(0xFF3949AB),
  error: Color(0xFFE57373),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFAFBFC),
  onSurface: Color(0xFF3C4858),
  surfaceContainerHighest: Color(0xFFE8EAF6),
  onSurfaceVariant: Color(0xFF5C6BC0),
  outline: Color(0xFFC5CAE9),
);

const _darkMoodTiredColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF9FA8DA),
  onPrimary: Color(0xFF283593),
  secondary: Color(0xFFC5CAE9),
  onSecondary: Color(0xFF3949AB),
  tertiary: Color(0xFFE8EAF6),
  onTertiary: Color(0xFF5C6BC0),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF1A1D25),
  onSurface: Color(0xFFE8EAF6),
  surfaceContainerHighest: Color(0xFF252A35),
  onSurfaceVariant: Color(0xFFC5CAE9),
  outline: Color(0xFF7986CB),
);

/// Mood Happy - Cheerful colors to match and amplify positive mood
const _lightMoodHappyColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFFFFA000),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFFFFCA28),
  onSecondary: Color(0xFF5D4037),
  tertiary: Color(0xFFFFF176),
  onTertiary: Color(0xFF5D4037),
  error: Color(0xFFD84315),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF3E2723),
  surfaceContainerHighest: Color(0xFFFFF9C4),
  onSurfaceVariant: Color(0xFF5D4037),
  outline: Color(0xFFFFE082),
);

const _darkMoodHappyColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFFFCA28),
  onPrimary: Color(0xFF5D4037),
  secondary: Color(0xFFFFE082),
  onSecondary: Color(0xFF6D4C41),
  tertiary: Color(0xFFFFF59D),
  onTertiary: Color(0xFF795548),
  error: Color(0xFFFF8A65),
  onError: Color(0xFF3E2723),
  surface: Color(0xFF2A2010),
  onSurface: Color(0xFFFFF9C4),
  surfaceContainerHighest: Color(0xFF3A2D18),
  onSurfaceVariant: Color(0xFFFFE082),
  outline: Color(0xFFFFC107),
);

/// Mood Creative - Inspiring colors to fuel imagination
const _lightMoodCreativeColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF6A1B9A),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF00897B),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFFFF6F00),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFD32F2F),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF311B92),
  surfaceContainerHighest: Color(0xFFE1BEE7),
  onSurfaceVariant: Color(0xFF4A148C),
  outline: Color(0xFFCE93D8),
);

const _darkMoodCreativeColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFCE93D8),
  onPrimary: Color(0xFF4A148C),
  secondary: Color(0xFF80CBC4),
  onSecondary: Color(0xFF004D40),
  tertiary: Color(0xFFFFAB40),
  onTertiary: Color(0xFF5D4037),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF7F0000),
  surface: Color(0xFF1F1528),
  onSurface: Color(0xFFE1BEE7),
  surfaceContainerHighest: Color(0xFF30203D),
  onSurfaceVariant: Color(0xFFCE93D8),
  outline: Color(0xFFAB47BC),
);

// --- COLORBLIND-FRIENDLY THEMES ---

/// Deuteranopia (Green-weak) - Uses blue/orange contrast instead of red/green
const _lightDeuteranopiaColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF0077BB),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFFEE7733),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF009988),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFCC3311),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1A1A2E),
  surfaceContainerHighest: Color(0xFFE8F4F8),
  onSurfaceVariant: Color(0xFF004466),
  outline: Color(0xFF88CCEE),
);

const _darkDeuteranopiaColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF88CCEE),
  onPrimary: Color(0xFF003355),
  secondary: Color(0xFFEE9955),
  onSecondary: Color(0xFF663300),
  tertiary: Color(0xFF44BB99),
  onTertiary: Color(0xFF004D40),
  error: Color(0xFFEE6655),
  onError: Color(0xFF550000),
  surface: Color(0xFF152030),
  onSurface: Color(0xFFE8F4F8),
  surfaceContainerHighest: Color(0xFF1F3040),
  onSurfaceVariant: Color(0xFF88CCEE),
  outline: Color(0xFF5599BB),
);

/// Protanopia (Red-weak) - Uses blue/yellow contrast, avoids red
const _lightProtanopiaColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF0077BB),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFFDDBB00),
  onSecondary: Color(0xFF332200),
  tertiary: Color(0xFF33BBEE),
  onTertiary: Color(0xFF003344),
  error: Color(0xFF994400),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1A1A2E),
  surfaceContainerHighest: Color(0xFFF5F0E0),
  onSurfaceVariant: Color(0xFF444400),
  outline: Color(0xFFBBAA33),
);

const _darkProtanopiaColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF77AADD),
  onPrimary: Color(0xFF002244),
  secondary: Color(0xFFEEDD55),
  onSecondary: Color(0xFF443300),
  tertiary: Color(0xFF77DDEE),
  onTertiary: Color(0xFF004455),
  error: Color(0xFFDD8833),
  onError: Color(0xFF331100),
  surface: Color(0xFF1A1F15),
  onSurface: Color(0xFFF5F0E0),
  surfaceContainerHighest: Color(0xFF2A3020),
  onSurfaceVariant: Color(0xFFDDCC66),
  outline: Color(0xFF99AA44),
);

/// Tritanopia (Blue-yellow weakness) - Uses red/cyan contrast
const _lightTritanopiaColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFFCC3366),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF009999),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFFEE6677),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFF882255),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF2E1A1A),
  surfaceContainerHighest: Color(0xFFF8E8E8),
  onSurfaceVariant: Color(0xFF660033),
  outline: Color(0xFFDD88AA),
);

const _darkTritanopiaColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFEE99AA),
  onPrimary: Color(0xFF660033),
  secondary: Color(0xFF66CCCC),
  onSecondary: Color(0xFF004444),
  tertiary: Color(0xFFFFAAAA),
  onTertiary: Color(0xFF662222),
  error: Color(0xFFDD6699),
  onError: Color(0xFF330022),
  surface: Color(0xFF251518),
  onSurface: Color(0xFFF8E8E8),
  surfaceContainerHighest: Color(0xFF3A2028),
  onSurfaceVariant: Color(0xFFEE99AA),
  outline: Color(0xFFBB6688),
);

/// Monochromacy (Complete color blindness) - High contrast grayscale
const _lightMonochromacyColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF1A1A1A),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF666666),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF333333),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFF444444),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1A1A1A),
  surfaceContainerHighest: Color(0xFFE8E8E8),
  onSurfaceVariant: Color(0xFF333333),
  outline: Color(0xFFAAAAAA),
);

const _darkMonochromacyColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFE0E0E0),
  onPrimary: Color(0xFF1A1A1A),
  secondary: Color(0xFFAAAAAA),
  onSecondary: Color(0xFF1A1A1A),
  tertiary: Color(0xFFCCCCCC),
  onTertiary: Color(0xFF333333),
  error: Color(0xFFBBBBBB),
  onError: Color(0xFF1A1A1A),
  surface: Color(0xFF1E1E1E),
  onSurface: Color(0xFFE0E0E0),
  surfaceContainerHighest: Color(0xFF2A2A2A),
  onSurfaceVariant: Color(0xFFCCCCCC),
  outline: Color(0xFF666666),
);

// --- BLIND / LOW VISION THEMES ---

/// Screen Reader Mode - Maximum contrast with clear semantic boundaries
const _lightScreenReaderColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF0000FF),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF006600),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF660066),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFCC0000),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF000000),
  surfaceContainerHighest: Color(0xFFF0F0F0),
  onSurfaceVariant: Color(0xFF000000),
  outline: Color(0xFF000000),
);

const _darkScreenReaderColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF6699FF),
  onPrimary: Color(0xFF000000),
  secondary: Color(0xFF66FF66),
  onSecondary: Color(0xFF000000),
  tertiary: Color(0xFFFF66FF),
  onTertiary: Color(0xFF000000),
  error: Color(0xFFFF6666),
  onError: Color(0xFF000000),
  surface: Color(0xFF000000),
  onSurface: Color(0xFFFFFFFF),
  surfaceContainerHighest: Color(0xFF1A1A1A),
  onSurfaceVariant: Color(0xFFFFFFFF),
  outline: Color(0xFFFFFFFF),
);

/// Maximum Contrast - Pure black and white only, no grays
const _lightHighContrastBlindColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF000000),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF000000),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF000000),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFF000000),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF000000),
  surfaceContainerHighest: Color(0xFFFFFFFF),
  onSurfaceVariant: Color(0xFF000000),
  outline: Color(0xFF000000),
);

const _darkHighContrastBlindColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFFFFFFF),
  onPrimary: Color(0xFF000000),
  secondary: Color(0xFFFFFFFF),
  onSecondary: Color(0xFF000000),
  tertiary: Color(0xFFFFFFFF),
  onTertiary: Color(0xFF000000),
  error: Color(0xFFFFFFFF),
  onError: Color(0xFF000000),
  surface: Color(0xFF000000),
  onSurface: Color(0xFFFFFFFF),
  surfaceContainerHighest: Color(0xFF000000),
  onSurfaceVariant: Color(0xFFFFFFFF),
  outline: Color(0xFFFFFFFF),
);

/// Large Text Mode - Same as screen reader but designed for larger UI elements
const _lightLargeTextColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF1A237E),
  onPrimary: Color(0xFFFFFFFF),
  secondary: Color(0xFF2E7D32),
  onSecondary: Color(0xFFFFFFFF),
  tertiary: Color(0xFF6A1B9A),
  onTertiary: Color(0xFFFFFFFF),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFDE7), // Warm white, easier on eyes
  onSurface: Color(0xFF000000),
  surfaceContainerHighest: Color(0xFFF5F5DC),
  onSurfaceVariant: Color(0xFF1A1A1A),
  outline: Color(0xFF333333),
);

const _darkLargeTextColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF90CAF9),
  onPrimary: Color(0xFF000000),
  secondary: Color(0xFFA5D6A7),
  onSecondary: Color(0xFF000000),
  tertiary: Color(0xFFCE93D8),
  onTertiary: Color(0xFF000000),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF000000),
  surface: Color(0xFF252510), // Dark warm
  onSurface: Color(0xFFFFFDE7),
  surfaceContainerHighest: Color(0xFF333320),
  onSurfaceVariant: Color(0xFFE0E0D0),
  outline: Color(0xFFCCCCBB),
);

// ============================================================================
// CINNAMON BUN THEME 🥐
// A warm, cozy, comforting theme inspired by freshly baked cinnamon buns
// ============================================================================

const _lightCinnamonBunColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFFA0522D), // Warm cinnamon brown
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFFFE4C9),
  onPrimaryContainer: Color(0xFF5D4037),
  secondary: Color(0xFFD2691E), // Chocolate brown
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFFFDCC0),
  onSecondaryContainer: Color(0xFF4E342E),
  tertiary: Color(0xFFF4A460), // Sandy brown/caramel
  onTertiary: Color(0xFF3E2723),
  tertiaryContainer: Color(0xFFFFE8D6),
  onTertiaryContainer: Color(0xFF5D4037),
  error: Color(0xFFB71C1C),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFAF5),
  onSurface: Color(0xFF3E2723),
  surfaceContainerHighest: Color(0xFFFFECD9), // Light caramel
  onSurfaceVariant: Color(0xFF5D4037),
  outline: Color(0xFFD7A574),
);

const _darkCinnamonBunColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFDEB887), // Burlywood
  onPrimary: Color(0xFF3E2723),
  primaryContainer: Color(0xFF5D4037),
  onPrimaryContainer: Color(0xFFFFE4C4),
  secondary: Color(0xFFF4A460), // Sandy brown
  onSecondary: Color(0xFF3E2723),
  secondaryContainer: Color(0xFF4E342E),
  onSecondaryContainer: Color(0xFFF4A460),
  tertiary: Color(0xFFFFE4C4), // Bisque
  onTertiary: Color(0xFF4E342E),
  tertiaryContainer: Color(0xFF6D4C41),
  onTertiaryContainer: Color(0xFFFFE8D6),
  error: Color(0xFFEF9A9A),
  onError: Color(0xFF3E2723),
  surface: Color(0xFF2A1F1A),
  onSurface: Color(0xFFFFE8D6),
  surfaceContainerHighest: Color(0xFF3D2E25),
  onSurfaceVariant: Color(0xFFDEB887),
  outline: Color(0xFFA0522D),
);

// ============================================================================
// SECRET THEME: RAINBOW BRAIN 🦄
// A celebration of neurodivergent minds with vibrant, joyful colors
// ============================================================================

const _lightRainbowBrainColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF9C27B0), // Vibrant purple (neurodiversity symbol)
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFE1BEE7),
  onPrimaryContainer: Color(0xFF4A148C),
  secondary: Color(0xFFFF6B9D), // Playful pink
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFF8BBD9),
  onSecondaryContainer: Color(0xFF880E4F),
  tertiary: Color(0xFF00BCD4), // Cyan accent
  onTertiary: Color(0xFFFFFFFF),
  tertiaryContainer: Color(0xFFB2EBF2),
  onTertiaryContainer: Color(0xFF006064),
  error: Color(0xFFD32F2F),
  onError: Color(0xFFFFFFFF),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF37474F),
  surfaceContainerHighest: Color(0xFFE1BEE7), // Light purple variant
  onSurfaceVariant: Color(0xFF4A148C),
  outline: Color(0xFFBA68C8),
);

const _darkRainbowBrainColorScheme = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFFCE93D8), // Soft purple
  onPrimary: Color(0xFF000000),
  primaryContainer: Color(0xFF4A148C),
  onPrimaryContainer: Color(0xFFE1BEE7),
  secondary: Color(0xFFF48FB1), // Soft pink
  onSecondary: Color(0xFF000000),
  secondaryContainer: Color(0xFF880E4F),
  onSecondaryContainer: Color(0xFFF8BBD9),
  tertiary: Color(0xFF4DD0E1), // Cyan
  onTertiary: Color(0xFF000000),
  tertiaryContainer: Color(0xFF006064),
  onTertiaryContainer: Color(0xFFB2EBF2),
  error: Color(0xFFEF5350),
  onError: Color(0xFF000000),
  surface: Color(0xFF2D1B3D),
  onSurface: Color(0xFFF3E5F5),
  surfaceContainerHighest: Color(0xFF3D2952),
  onSurfaceVariant: Color(0xFFE1BEE7),
  outline: Color(0xFF9C27B0),
);

// ============================================================================
// THEME SELECTOR
// ============================================================================

/// Get the color scheme for a given NeuroState and brightness
ColorScheme getColorSchemeForState(NeuroState state, bool isDarkMode) {
  switch (state) {
    case NeuroState.defaultState:
      return isDarkMode ? _darkDefaultColorScheme : _lightDefaultColorScheme;
    case NeuroState.hyperfocus:
      return isDarkMode ? _darkHyperfocusColorScheme : _lightHyperfocusColorScheme;
    case NeuroState.overload:
      return isDarkMode ? _darkOverloadColorScheme : _lightOverloadColorScheme;
    case NeuroState.calm:
      return isDarkMode ? _darkCalmColorScheme : _lightCalmColorScheme;
    case NeuroState.adhdEnergized:
      return isDarkMode ? _darkADHDEnergizedColorScheme : _lightADHDEnergizedColorScheme;
    case NeuroState.adhdLowDopamine:
      return isDarkMode ? _darkADHDLowDopamineColorScheme : _lightADHDLowDopamineColorScheme;
    case NeuroState.adhdTaskMode:
      return isDarkMode ? _darkADHDTaskModeColorScheme : _lightADHDTaskModeColorScheme;
    case NeuroState.autismRoutine:
      return isDarkMode ? _darkAutismRoutineColorScheme : _lightAutismRoutineColorScheme;
    case NeuroState.autismSensorySeek:
      return isDarkMode ? _darkAutismSensorySeekColorScheme : _lightAutismSensorySeekColorScheme;
    case NeuroState.autismLowStim:
      return isDarkMode ? _darkAutismLowStimColorScheme : _lightAutismLowStimColorScheme;
    case NeuroState.anxietySoothe:
      return isDarkMode ? _darkAnxietySootheColorScheme : _lightAnxietySootheColorScheme;
    case NeuroState.anxietyGrounding:
      return isDarkMode ? _darkAnxietyGroundingColorScheme : _lightAnxietyGroundingColorScheme;
    case NeuroState.dyslexiaFriendly:
      return isDarkMode ? _darkDyslexiaFriendlyColorScheme : _lightDyslexiaFriendlyColorScheme;
    case NeuroState.colorblindDeuter:
      return isDarkMode ? _darkDeuteranopiaColorScheme : _lightDeuteranopiaColorScheme;
    case NeuroState.colorblindProtan:
      return isDarkMode ? _darkProtanopiaColorScheme : _lightProtanopiaColorScheme;
    case NeuroState.colorblindTritan:
      return isDarkMode ? _darkTritanopiaColorScheme : _lightTritanopiaColorScheme;
    case NeuroState.colorblindMono:
      return isDarkMode ? _darkMonochromacyColorScheme : _lightMonochromacyColorScheme;
    case NeuroState.blindScreenReader:
      return isDarkMode ? _darkScreenReaderColorScheme : _lightScreenReaderColorScheme;
    case NeuroState.blindHighContrast:
      return isDarkMode ? _darkHighContrastBlindColorScheme : _lightHighContrastBlindColorScheme;
    case NeuroState.blindLargeText:
      return isDarkMode ? _darkLargeTextColorScheme : _lightLargeTextColorScheme;
    case NeuroState.moodTired:
      return isDarkMode ? _darkMoodTiredColorScheme : _lightMoodTiredColorScheme;
    case NeuroState.moodAnxious:
      return isDarkMode ? _darkAnxietySootheColorScheme : _lightAnxietySootheColorScheme;
    case NeuroState.moodHappy:
      return isDarkMode ? _darkMoodHappyColorScheme : _lightMoodHappyColorScheme;
    case NeuroState.moodOverwhelmed:
      return isDarkMode ? _darkAutismLowStimColorScheme : _lightAutismLowStimColorScheme;
    case NeuroState.moodCreative:
      return isDarkMode ? _darkMoodCreativeColorScheme : _lightMoodCreativeColorScheme;
    case NeuroState.rainbowBrain:
      return isDarkMode ? _darkRainbowBrainColorScheme : _lightRainbowBrainColorScheme;
    case NeuroState.cinnamonBun:
      return isDarkMode ? _darkCinnamonBunColorScheme : _lightCinnamonBunColorScheme;
  }
}

/// Apply high contrast overlay to a color scheme
ColorScheme highContrastOverlay(ColorScheme colorScheme, bool isDarkMode) {
  const pureWhite = Color(0xFFFFFFFF);
  const pureBlack = Color(0xFF000000);

  if (isDarkMode) {
    return colorScheme.copyWith(
      surface: pureBlack,
      onSurface: pureWhite,
      primary: pureWhite,
      onPrimary: pureBlack,
      secondary: const Color(0xFFFFFF00),
      onSecondary: pureBlack,
    );
  } else {
    return colorScheme.copyWith(
      surface: pureWhite,
      onSurface: pureBlack,
      primary: pureBlack,
      onPrimary: pureWhite,
      secondary: const Color(0xFF0000FF),
      onSecondary: pureWhite,
    );
  }
}

/// Get automatic text scale factor for accessibility themes
double getAutoScaleFactorForState(NeuroState state) {
  switch (state) {
    case NeuroState.blindLargeText:
      return 1.5; // 50% larger text
    case NeuroState.blindScreenReader:
      return 1.2; // 20% larger for screen reader
    case NeuroState.blindHighContrast:
      return 1.3; // 30% larger for high contrast
    case NeuroState.dyslexiaFriendly:
      return 1.1; // 10% larger for dyslexia
    default:
      return 1.0;
  }
}


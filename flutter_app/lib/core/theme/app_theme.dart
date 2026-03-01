import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'neuro_state.dart';
import 'neuro_color_schemes.dart';

/// Main theme configuration for NeuroComet app
/// Implements Material 3 Expressive (M3E) design language with:
/// - More expressive, vibrant colors
/// - Larger, bolder typography using Inter (clean, modern, accessible)
/// - More prominent rounded shapes (up to 28dp)
/// - Enhanced visual hierarchy
/// - Neurodivergent-friendly adaptations
class AppTheme {
  AppTheme._();


  // M3E shape constants - more expressive rounded corners
  static const double shapeExtraSmall = 4.0;
  static const double shapeSmall = 8.0;
  static const double shapeMedium = 16.0;
  static const double shapeLarge = 24.0;
  static const double shapeExtraLarge = 28.0;
  static const double shapeFull = 1000.0; // For pills/circular

  /// Get the primary text theme using Inter font (clean, accessible, modern)
  static TextTheme _getTextTheme({double textScale = 1.0}) {
    return GoogleFonts.interTextTheme().copyWith(
      displayLarge: GoogleFonts.inter(
        fontSize: 57 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: -0.25,
      ),
      displayMedium: GoogleFonts.inter(
        fontSize: 45 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: 0,
      ),
      displaySmall: GoogleFonts.inter(
        fontSize: 36 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: 0,
      ),
      headlineLarge: GoogleFonts.inter(
        fontSize: 32 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0,
      ),
      headlineMedium: GoogleFonts.inter(
        fontSize: 28 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0,
      ),
      headlineSmall: GoogleFonts.inter(
        fontSize: 24 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0,
      ),
      titleLarge: GoogleFonts.inter(
        fontSize: 22 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0,
      ),
      titleMedium: GoogleFonts.inter(
        fontSize: 16 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.15,
      ),
      titleSmall: GoogleFonts.inter(
        fontSize: 14 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.1,
      ),
      bodyLarge: GoogleFonts.inter(
        fontSize: 16 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.5,
      ),
      bodyMedium: GoogleFonts.inter(
        fontSize: 14 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.25,
      ),
      bodySmall: GoogleFonts.inter(
        fontSize: 12 * textScale,
        fontWeight: FontWeight.w400,
        letterSpacing: 0.4,
      ),
      labelLarge: GoogleFonts.inter(
        fontSize: 14 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.1,
      ),
      labelMedium: GoogleFonts.inter(
        fontSize: 12 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.5,
      ),
      labelSmall: GoogleFonts.inter(
        fontSize: 11 * textScale,
        fontWeight: FontWeight.w600,
        letterSpacing: 0.5,
      ),
    );
  }

  /// Get dyslexia-friendly text theme using Lexend Deca
  static TextTheme _getDyslexicTextTheme({double textScale = 1.0}) {
    return GoogleFonts.lexendDecaTextTheme().copyWith(
      displayLarge: GoogleFonts.lexendDeca(fontSize: 57 * textScale, fontWeight: FontWeight.w400),
      displayMedium: GoogleFonts.lexendDeca(fontSize: 45 * textScale, fontWeight: FontWeight.w400),
      displaySmall: GoogleFonts.lexendDeca(fontSize: 36 * textScale, fontWeight: FontWeight.w400),
      headlineLarge: GoogleFonts.lexendDeca(fontSize: 32 * textScale, fontWeight: FontWeight.w600),
      headlineMedium: GoogleFonts.lexendDeca(fontSize: 28 * textScale, fontWeight: FontWeight.w600),
      headlineSmall: GoogleFonts.lexendDeca(fontSize: 24 * textScale, fontWeight: FontWeight.w600),
      titleLarge: GoogleFonts.lexendDeca(fontSize: 22 * textScale, fontWeight: FontWeight.w600),
      titleMedium: GoogleFonts.lexendDeca(fontSize: 16 * textScale, fontWeight: FontWeight.w600),
      titleSmall: GoogleFonts.lexendDeca(fontSize: 14 * textScale, fontWeight: FontWeight.w600),
      bodyLarge: GoogleFonts.lexendDeca(fontSize: 16 * textScale, fontWeight: FontWeight.w400),
      bodyMedium: GoogleFonts.lexendDeca(fontSize: 14 * textScale, fontWeight: FontWeight.w400),
      bodySmall: GoogleFonts.lexendDeca(fontSize: 12 * textScale, fontWeight: FontWeight.w400),
      labelLarge: GoogleFonts.lexendDeca(fontSize: 14 * textScale, fontWeight: FontWeight.w600),
      labelMedium: GoogleFonts.lexendDeca(fontSize: 12 * textScale, fontWeight: FontWeight.w600),
      labelSmall: GoogleFonts.lexendDeca(fontSize: 11 * textScale, fontWeight: FontWeight.w600),
    );
  }

  /// Get theme for a specific NeuroState and brightness
  /// Uses carefully designed color schemes from neuro_color_schemes.dart
  static ThemeData getTheme(NeuroState state, Brightness brightness, {
    bool highContrast = false,
    double textScale = 1.0,
    bool useDyslexicFont = false,
  }) {
    final isDarkMode = brightness == Brightness.dark;

    // Get the carefully designed color scheme for this state
    ColorScheme colorScheme = getColorSchemeForState(state, isDarkMode);

    // Apply high contrast overlay if requested
    if (highContrast) {
      colorScheme = highContrastOverlay(colorScheme, isDarkMode);
    }

    // Calculate effective text scale (user preference or auto-scale for accessibility themes)
    final autoScale = getAutoScaleFactorForState(state);
    final effectiveScale = textScale > autoScale ? textScale : autoScale;

    // Get the appropriate text theme
    final textTheme = useDyslexicFont
        ? _getDyslexicTextTheme(textScale: effectiveScale)
        : _getTextTheme(textScale: effectiveScale);

    return _buildTheme(
      colorScheme: colorScheme,
      brightness: brightness,
      textTheme: textTheme,
      textScale: effectiveScale,
    );
  }

  /// Build a complete ThemeData from a color scheme
  /// Implements M3E design language principles
  static ThemeData _buildTheme({
    required ColorScheme colorScheme,
    required Brightness brightness,
    required TextTheme textTheme,
    double textScale = 1.0,
  }) {
    final bool isDark = brightness == Brightness.dark;

    // M3E elevated surface colors
    final surfaceContainer = isDark
        ? const Color(0xFF252529)
        : colorScheme.surfaceContainerHighest.withOpacity(0.5);
    final surfaceContainerHigh = isDark
        ? const Color(0xFF2B2B30)
        : colorScheme.surfaceContainerHighest;
    final surfaceContainerHighest = isDark
        ? const Color(0xFF36353B)
        : colorScheme.surfaceContainerHighest;

    return ThemeData(
      useMaterial3: true,
      brightness: brightness,
      textTheme: textTheme,
      colorScheme: colorScheme,

      // M3E background - slightly tinted in light mode
      scaffoldBackgroundColor: isDark
          ? const Color(0xFF1C1B1F)
          : const Color(0xFFFFFBFE),

      // Canvas color for dialogs, drawers, etc.
      canvasColor: isDark ? const Color(0xFF1C1B1F) : const Color(0xFFFFFBFE),

      // Card color - elevated surfaces
      cardColor: isDark ? surfaceContainerHigh : Colors.white,

      // Dialog background - elevated
      dialogBackgroundColor: isDark ? surfaceContainerHighest : Colors.white,

      // M3E Splash/Ripple - using InkRipple for stability (InkSparkle can cause glitches)
      splashFactory: InkRipple.splashFactory,

      // ═══════════════════════════════════════════════════════════════
      // APP BAR - M3E Style
      // ═══════════════════════════════════════════════════════════════
      appBarTheme: AppBarTheme(
        elevation: 0,
        centerTitle: true,
        backgroundColor: isDark ? const Color(0xFF1C1B1F) : const Color(0xFFFFFBFE),
        surfaceTintColor: Colors.transparent,
        scrolledUnderElevation: 0,
        foregroundColor: colorScheme.onSurface,
        titleTextStyle: textTheme.titleLarge?.copyWith(
          color: colorScheme.onSurface,
        ),
        iconTheme: IconThemeData(
          color: colorScheme.onSurfaceVariant,
          size: 24,
        ),
        actionsIconTheme: IconThemeData(
          color: colorScheme.onSurfaceVariant,
          size: 24,
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // CARDS - M3E Expressive shapes
      // ═══════════════════════════════════════════════════════════════
      cardTheme: CardThemeData(
        elevation: isDark ? 0 : 1,
        shadowColor: colorScheme.shadow.withOpacity(0.1),
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeLarge), // M3E larger radius
        ),
        color: isDark ? surfaceContainerHigh : Colors.white,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      ),

      // ═══════════════════════════════════════════════════════════════
      // BUTTONS - M3E Expressive with larger touch targets
      // ═══════════════════════════════════════════════════════════════
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: colorScheme.primary,
          foregroundColor: colorScheme.onPrimary,
          elevation: 0,
          shadowColor: Colors.transparent,
          padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
          minimumSize: const Size(64, 52), // M3E larger touch target
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(shapeFull), // Pill shape
          ),
          textStyle: textTheme.labelLarge,
        ),
      ),

      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: colorScheme.primary,
          foregroundColor: colorScheme.onPrimary,
          padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
          minimumSize: const Size(64, 52),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(shapeFull),
          ),
          textStyle: textTheme.labelLarge,
        ),
      ),

      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: colorScheme.primary,
          side: BorderSide(color: colorScheme.outline, width: 1),
          padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
          minimumSize: const Size(64, 52),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(shapeFull),
          ),
          textStyle: textTheme.labelLarge,
        ),
      ),

      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: colorScheme.primary,
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          minimumSize: const Size(48, 44),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(shapeFull),
          ),
          textStyle: textTheme.labelLarge,
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // ICON BUTTONS - M3E Style with proper circular highlight
      // ═══════════════════════════════════════════════════════════════
      iconButtonTheme: IconButtonThemeData(
        style: ButtonStyle(
          foregroundColor: WidgetStatePropertyAll(colorScheme.onSurfaceVariant),
          minimumSize: const WidgetStatePropertyAll(Size(48, 48)),
          maximumSize: const WidgetStatePropertyAll(Size(48, 48)),
          fixedSize: const WidgetStatePropertyAll(Size(48, 48)),
          tapTargetSize: MaterialTapTargetSize.padded,
          shape: const WidgetStatePropertyAll(CircleBorder()),
          backgroundColor: const WidgetStatePropertyAll(Colors.transparent),
          overlayColor: WidgetStateProperty.resolveWith((states) {
            if (states.contains(WidgetState.pressed)) {
              return colorScheme.primary.withOpacity(0.12);
            }
            if (states.contains(WidgetState.hovered)) {
              return colorScheme.onSurfaceVariant.withOpacity(0.08);
            }
            if (states.contains(WidgetState.focused)) {
              return colorScheme.onSurfaceVariant.withOpacity(0.12);
            }
            return Colors.transparent;
          }),
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // FLOATING ACTION BUTTON - M3E Large FAB style
      // ═══════════════════════════════════════════════════════════════
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        elevation: 3,
        highlightElevation: 4,
        backgroundColor: colorScheme.primaryContainer,
        foregroundColor: colorScheme.onPrimaryContainer,
        extendedPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeLarge),
        ),
        extendedTextStyle: textTheme.labelLarge,
      ),

      // ═══════════════════════════════════════════════════════════════
      // INPUT FIELDS - M3E Expressive
      // ═══════════════════════════════════════════════════════════════
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: isDark ? surfaceContainer : colorScheme.surfaceContainerHighest.withOpacity(0.4),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
          borderSide: BorderSide(color: colorScheme.primary, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
          borderSide: BorderSide(color: colorScheme.error, width: 2),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
          borderSide: BorderSide(color: colorScheme.error, width: 2),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
        hintStyle: textTheme.bodyLarge?.copyWith(
          color: colorScheme.onSurfaceVariant.withOpacity(0.7),
        ),
        labelStyle: textTheme.bodyLarge?.copyWith(
          color: colorScheme.onSurfaceVariant,
        ),
        prefixIconColor: colorScheme.onSurfaceVariant,
        suffixIconColor: colorScheme.onSurfaceVariant,
      ),

      // ═══════════════════════════════════════════════════════════════
      // SEARCH BAR - M3E Style
      // ═══════════════════════════════════════════════════════════════
      searchBarTheme: SearchBarThemeData(
        elevation: WidgetStateProperty.all(0),
        backgroundColor: WidgetStateProperty.all(
          isDark ? surfaceContainerHigh : colorScheme.surfaceContainerHighest.withOpacity(0.5),
        ),
        shadowColor: WidgetStateProperty.all(Colors.transparent),
        surfaceTintColor: WidgetStateProperty.all(Colors.transparent),
        shape: WidgetStateProperty.all(
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(shapeFull)),
        ),
        padding: WidgetStateProperty.all(
          const EdgeInsets.symmetric(horizontal: 16),
        ),
        textStyle: WidgetStateProperty.all(textTheme.bodyLarge?.copyWith(
          color: colorScheme.onSurface,
        )),
        hintStyle: WidgetStateProperty.all(textTheme.bodyLarge?.copyWith(
          color: colorScheme.onSurfaceVariant,
        )),
      ),

      // ═══════════════════════════════════════════════════════════════
      // NAVIGATION - M3E Expressive
      // ═══════════════════════════════════════════════════════════════
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF1C1B1F) : const Color(0xFFFFFBFE),
        selectedItemColor: colorScheme.primary,
        unselectedItemColor: colorScheme.onSurfaceVariant,
        type: BottomNavigationBarType.fixed,
        showUnselectedLabels: true,
        selectedLabelStyle: textTheme.labelSmall?.copyWith(
          fontWeight: FontWeight.w600,
        ),
        unselectedLabelStyle: textTheme.labelSmall?.copyWith(
          fontWeight: FontWeight.w500,
        ),
      ),

      navigationBarTheme: NavigationBarThemeData(
        elevation: 0,
        height: 80,
        backgroundColor: isDark ? const Color(0xFF1C1B1F) : const Color(0xFFFFFBFE),
        surfaceTintColor: Colors.transparent,
        indicatorColor: colorScheme.secondaryContainer,
        indicatorShape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeFull),
        ),
        iconTheme: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return IconThemeData(
              color: colorScheme.onSecondaryContainer,
              size: 24,
            );
          }
          return IconThemeData(
            color: colorScheme.onSurfaceVariant,
            size: 24,
          );
        }),
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return textTheme.labelSmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: colorScheme.onSurface,
              height: 1.0,
              letterSpacing: 0.5,
            );
          }
          return textTheme.labelSmall?.copyWith(
            fontWeight: FontWeight.w500,
            color: colorScheme.onSurfaceVariant,
            height: 1.0,
            letterSpacing: 0.5,
          );
        }),
      ),

      navigationRailTheme: NavigationRailThemeData(
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF1C1B1F) : const Color(0xFFFFFBFE),
        indicatorColor: colorScheme.secondaryContainer,
        indicatorShape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeFull),
        ),
        selectedIconTheme: IconThemeData(
          color: colorScheme.onSecondaryContainer,
          size: 24,
        ),
        unselectedIconTheme: IconThemeData(
          color: colorScheme.onSurfaceVariant,
          size: 24,
        ),
        selectedLabelTextStyle: textTheme.labelSmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: colorScheme.onSurface,
        ),
        unselectedLabelTextStyle: textTheme.labelSmall?.copyWith(
          fontWeight: FontWeight.w500,
          color: colorScheme.onSurfaceVariant,
        ),
      ),

      navigationDrawerTheme: NavigationDrawerThemeData(
        elevation: 1,
        backgroundColor: isDark ? surfaceContainerHigh : Colors.white,
        surfaceTintColor: Colors.transparent,
        indicatorColor: colorScheme.secondaryContainer,
        indicatorShape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeFull),
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // DIALOGS & SHEETS - M3E Expressive shapes
      // ═══════════════════════════════════════════════════════════════
      dialogTheme: DialogThemeData(
        elevation: 3,
        backgroundColor: isDark ? surfaceContainerHighest : Colors.white,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeExtraLarge),
        ),
        titleTextStyle: textTheme.headlineSmall?.copyWith(
          color: colorScheme.onSurface,
        ),
        contentTextStyle: textTheme.bodyMedium?.copyWith(
          color: colorScheme.onSurfaceVariant,
        ),
      ),

      bottomSheetTheme: BottomSheetThemeData(
        elevation: 1,
        backgroundColor: isDark ? surfaceContainerHigh : Colors.white,
        surfaceTintColor: Colors.transparent,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(shapeExtraLarge)),
        ),
        dragHandleColor: colorScheme.onSurfaceVariant.withOpacity(0.4),
        dragHandleSize: const Size(32, 4),
        showDragHandle: true,
      ),

      // ═══════════════════════════════════════════════════════════════
      // CHIPS - M3E Expressive
      // ═══════════════════════════════════════════════════════════════
      chipTheme: ChipThemeData(
        elevation: 0,
        pressElevation: 0,
        backgroundColor: isDark ? surfaceContainer : colorScheme.surfaceContainerHighest.withOpacity(0.5),
        selectedColor: colorScheme.secondaryContainer,
        disabledColor: colorScheme.onSurface.withOpacity(0.12),
        labelStyle: textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w500,
        ),
        secondaryLabelStyle: textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w500,
          color: colorScheme.onSecondaryContainer,
        ),
        side: BorderSide.none,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeFull),
        ),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      ),

      // ═══════════════════════════════════════════════════════════════
      // LISTS & TILES
      // ═══════════════════════════════════════════════════════════════
      listTileTheme: ListTileThemeData(
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeMedium),
        ),
        titleTextStyle: textTheme.bodyLarge?.copyWith(
          fontWeight: FontWeight.w500,
          color: colorScheme.onSurface,
        ),
        subtitleTextStyle: textTheme.bodyMedium?.copyWith(
          fontWeight: FontWeight.w400,
          color: colorScheme.onSurfaceVariant,
        ),
        leadingAndTrailingTextStyle: textTheme.bodyMedium?.copyWith(
          color: colorScheme.onSurfaceVariant,
        ),
        iconColor: colorScheme.onSurfaceVariant,
      ),

      // ═══════════════════════════════════════════════════════════════
      // SWITCHES, CHECKBOXES, RADIOS - M3E
      // ═══════════════════════════════════════════════════════════════
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return colorScheme.onPrimary;
          }
          return colorScheme.outline;
        }),
        trackColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return colorScheme.primary;
          }
          return colorScheme.surfaceContainerHighest;
        }),
        trackOutlineColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return Colors.transparent;
          }
          return colorScheme.outline;
        }),
      ),

      checkboxTheme: CheckboxThemeData(
        fillColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return colorScheme.primary;
          }
          return Colors.transparent;
        }),
        checkColor: WidgetStateProperty.all(colorScheme.onPrimary),
        side: BorderSide(color: colorScheme.outline, width: 2),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeExtraSmall),
        ),
      ),

      radioTheme: RadioThemeData(
        fillColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return colorScheme.primary;
          }
          return colorScheme.onSurfaceVariant;
        }),
      ),

      // ═══════════════════════════════════════════════════════════════
      // SLIDERS - M3E
      // ═══════════════════════════════════════════════════════════════
      sliderTheme: SliderThemeData(
        activeTrackColor: colorScheme.primary,
        inactiveTrackColor: colorScheme.surfaceContainerHighest,
        thumbColor: colorScheme.primary,
        overlayColor: colorScheme.primary.withOpacity(0.12),
        valueIndicatorColor: colorScheme.primary,
        valueIndicatorTextStyle: textTheme.labelLarge?.copyWith(
          color: colorScheme.onPrimary,
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // PROGRESS INDICATORS - M3E
      // ═══════════════════════════════════════════════════════════════
      progressIndicatorTheme: ProgressIndicatorThemeData(
        color: colorScheme.primary,
        linearTrackColor: colorScheme.surfaceContainerHighest,
        circularTrackColor: colorScheme.surfaceContainerHighest,
      ),

      // ═══════════════════════════════════════════════════════════════
      // SNACKBAR & BANNER
      // ═══════════════════════════════════════════════════════════════
      snackBarTheme: SnackBarThemeData(
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFFE6E1E5) : const Color(0xFF313033),
        contentTextStyle: textTheme.bodyMedium?.copyWith(
          color: isDark ? const Color(0xFF313033) : const Color(0xFFE6E1E5),
        ),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(shapeSmall),
        ),
        behavior: SnackBarBehavior.floating,
        insetPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      ),

      // ═══════════════════════════════════════════════════════════════
      // TABS - M3E
      // ═══════════════════════════════════════════════════════════════
      tabBarTheme: TabBarThemeData(
        indicator: BoxDecoration(
          borderRadius: BorderRadius.circular(shapeFull),
          color: colorScheme.secondaryContainer,
        ),
        indicatorSize: TabBarIndicatorSize.tab,
        labelColor: colorScheme.onSecondaryContainer,
        unselectedLabelColor: colorScheme.onSurfaceVariant,
        labelStyle: textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w600,
        ),
        unselectedLabelStyle: textTheme.labelLarge?.copyWith(
          fontWeight: FontWeight.w500,
        ),
        dividerColor: Colors.transparent,
      ),

      // ═══════════════════════════════════════════════════════════════
      // DIVIDERS
      // ═══════════════════════════════════════════════════════════════
      dividerTheme: DividerThemeData(
        color: colorScheme.outlineVariant,
        thickness: 1,
        space: 1,
      ),

      // ═══════════════════════════════════════════════════════════════
      // BADGES
      // ═══════════════════════════════════════════════════════════════
      badgeTheme: BadgeThemeData(
        backgroundColor: colorScheme.error,
        textColor: colorScheme.onError,
        textStyle: textTheme.labelSmall?.copyWith(
          fontWeight: FontWeight.w600,
        ),
      ),

      // ═══════════════════════════════════════════════════════════════
      // TOOLTIPS
      // ═══════════════════════════════════════════════════════════════
      tooltipTheme: TooltipThemeData(
        decoration: BoxDecoration(
          color: colorScheme.inverseSurface,
          borderRadius: BorderRadius.circular(shapeSmall),
        ),
        textStyle: textTheme.bodySmall?.copyWith(
          color: colorScheme.onInverseSurface,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      ),
    );
  }


  /// Light theme configuration (default state)
  static ThemeData get lightTheme => getTheme(
    NeuroState.defaultState,
    Brightness.light,
  );

  /// Dark theme configuration (default state)
  static ThemeData get darkTheme => getTheme(
    NeuroState.defaultState,
    Brightness.dark,
  );
}

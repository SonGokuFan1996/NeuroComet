import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// Platform-specific utilities for web and mobile
class PlatformUtils {
  PlatformUtils._();

  /// Check if running on web
  static bool get isWeb => kIsWeb;

  /// Check if running on mobile web (user agent detection would be needed for accuracy)
  static bool get isMobileWeb {
    if (!kIsWeb) return false;
    // On web, we rely on screen size for mobile detection
    return false; // Will be determined by Responsive class
  }

  /// Check if running on desktop web
  static bool get isDesktopWeb => kIsWeb;

  /// Check if hover states should be enabled
  static bool get supportsHover => kIsWeb;

  /// Check if we should use web-specific scrolling
  static bool get useWebScrolling => kIsWeb;

  /// Check if tooltips should be shown (desktop hover)
  static bool get showTooltips => kIsWeb;

  /// Check if keyboard shortcuts should be enabled
  static bool get enableKeyboardShortcuts => kIsWeb;

  /// Check if we should use custom scrollbars
  static bool get useCustomScrollbars => kIsWeb;

  /// Get default animation duration (faster on web)
  static Duration get defaultAnimationDuration =>
      kIsWeb ? const Duration(milliseconds: 150) : const Duration(milliseconds: 300);

  /// Get page transition duration
  static Duration get pageTransitionDuration =>
      kIsWeb ? const Duration(milliseconds: 200) : const Duration(milliseconds: 300);
}

/// Web-specific scroll behavior
class WebScrollBehavior extends MaterialScrollBehavior {
  const WebScrollBehavior();

  @override
  Widget buildScrollbar(
    BuildContext context,
    Widget child,
    ScrollableDetails details,
  ) {
    // Use platform scrollbar on web
    if (kIsWeb) {
      return Scrollbar(
        controller: details.controller,
        thumbVisibility: true,
        thickness: 8,
        radius: const Radius.circular(4),
        child: child,
      );
    }
    return child;
  }

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) {
    // Use bouncing physics for a more native feel
    return const BouncingScrollPhysics();
  }
}

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';

/// Responsive breakpoints for universal screen compatibility
class Breakpoints {
  Breakpoints._();

  /// Extra small (phones in portrait) - < 360px
  static const double xs = 360;

  /// Small (phones) - < 600px
  static const double sm = 600;

  /// Medium (tablets portrait, large phones landscape) - < 900px
  static const double md = 900;

  /// Large (tablets landscape, small laptops) - < 1200px
  static const double lg = 1200;

  /// Extra large (desktops) - < 1536px
  static const double xl = 1536;

  /// Extra extra large (large monitors) - >= 1536px
  static const double xxl = 1920;
}

/// Device type enumeration
enum DeviceType {
  mobileSmall,   // < 360px
  mobile,        // 360-600px
  tablet,        // 600-900px
  desktop,       // 900-1200px
  largeDesktop,  // 1200-1536px
  ultrawide,     // >= 1536px
}

/// Screen orientation
enum ScreenOrientation {
  portrait,
  landscape,
}

enum AuthLayout {
  stacked,
  balanced,
  split,
}

/// Responsive utility class for determining device type and layout parameters
class Responsive {
  final double screenWidth;
  final double screenHeight;

  Responsive({
    required this.screenWidth,
    required this.screenHeight,
  });

  /// Create from MediaQueryData
  factory Responsive.of(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    return Responsive(
      screenWidth: size.width,
      screenHeight: size.height,
    );
  }

  /// Get current device type based on screen width
  DeviceType get deviceType {
    if (screenWidth < Breakpoints.xs) {
      return DeviceType.mobileSmall;
    } else if (screenWidth < Breakpoints.sm) {
      return DeviceType.mobile;
    } else if (screenWidth < Breakpoints.md) {
      return DeviceType.tablet;
    } else if (screenWidth < Breakpoints.lg) {
      return DeviceType.desktop;
    } else if (screenWidth < Breakpoints.xl) {
      return DeviceType.largeDesktop;
    } else {
      return DeviceType.ultrawide;
    }
  }

  /// Get screen orientation
  ScreenOrientation get orientation {
    return screenWidth > screenHeight
        ? ScreenOrientation.landscape
        : ScreenOrientation.portrait;
  }

  /// Check if landscape
  bool get isLandscape => orientation == ScreenOrientation.landscape;

  /// Check if portrait
  bool get isPortrait => orientation == ScreenOrientation.portrait;

  /// Check if current device is mobile (small screens)
  bool get isMobile =>
      deviceType == DeviceType.mobileSmall || deviceType == DeviceType.mobile;

  /// Check if current device is tablet
  bool get isTablet => deviceType == DeviceType.tablet;

  /// Check if current device is desktop or larger
  bool get isDesktop =>
      deviceType == DeviceType.desktop ||
      deviceType == DeviceType.largeDesktop ||
      deviceType == DeviceType.ultrawide;

  /// Check if current device is large desktop
  bool get isLargeDesktop =>
      deviceType == DeviceType.largeDesktop ||
      deviceType == DeviceType.ultrawide;

  /// Auth layout variants matching Android
  AuthLayout get authLayout {
    if (screenWidth > 900) return AuthLayout.split;
    if (screenWidth > 600) return AuthLayout.balanced;
    return AuthLayout.stacked;
  }

  /// Check if we're running on web platform
  bool get isWeb => kIsWeb;

  /// Check if we should use mobile layout
  bool get useMobileLayout => isMobile;

  /// Check if we should use tablet layout
  bool get useTabletLayout => isTablet;

  /// Check if we should use navigation rail instead of bottom nav
  bool get useNavigationRail => !isMobile;

  /// Check if we should show extended navigation rail with labels
  bool get showExtendedNavRail => isLargeDesktop || (isDesktop && screenWidth > 1100);

  /// Check if we should show sidebar
  bool get showSidebar => isDesktop;

  /// Check if we should show extended sidebar with text
  bool get showExtendedSidebar => isLargeDesktop;

  /// Check if we should show right panel
  bool get showRightPanel => isLargeDesktop;

  /// Get content max width for centered layouts
  double get contentMaxWidth {
    if (isMobile) return screenWidth;
    if (isTablet) return 600;
    if (deviceType == DeviceType.desktop) return 720;
    if (deviceType == DeviceType.largeDesktop) return 800;
    return 900; // ultrawide
  }

  /// Get sidebar width
  double get sidebarWidth {
    if (showExtendedSidebar) return 280;
    if (showSidebar) return 72;
    return 0;
  }

  /// Get right panel width (for desktop layouts)
  double get rightPanelWidth {
    if (deviceType == DeviceType.ultrawide) return 400;
    if (deviceType == DeviceType.largeDesktop) return 320;
    if (deviceType == DeviceType.desktop) return 280;
    return 0;
  }

  /// Get grid column count for card layouts
  int get gridColumnCount {
    switch (deviceType) {
      case DeviceType.mobileSmall:
        return 1;
      case DeviceType.mobile:
        return isLandscape ? 2 : 1;
      case DeviceType.tablet:
        return isLandscape ? 3 : 2;
      case DeviceType.desktop:
        return 3;
      case DeviceType.largeDesktop:
        return 4;
      case DeviceType.ultrawide:
        return 5;
    }
  }

  /// Get horizontal padding based on device type
  double get horizontalPadding {
    switch (deviceType) {
      case DeviceType.mobileSmall:
        return 8;
      case DeviceType.mobile:
        return 12;
      case DeviceType.tablet:
        return 16;
      case DeviceType.desktop:
        return 24;
      case DeviceType.largeDesktop:
      case DeviceType.ultrawide:
        return 32;
    }
  }

  /// Get vertical padding based on device type
  double get verticalPadding {
    switch (deviceType) {
      case DeviceType.mobileSmall:
        return 8;
      case DeviceType.mobile:
        return 12;
      case DeviceType.tablet:
        return 16;
      case DeviceType.desktop:
      case DeviceType.largeDesktop:
      case DeviceType.ultrawide:
        return 24;
    }
  }

  /// Get gap between items based on device type
  double get itemGap {
    if (isMobile) return 8;
    if (isTablet) return 12;
    return 16;
  }

  /// Get card border radius
  double get cardRadius {
    if (isMobile) return 12;
    if (isTablet) return 14;
    return 16;
  }

  /// Get icon size
  double get iconSize {
    if (isMobile) return 20;
    if (isTablet) return 22;
    return 24;
  }

  /// Get avatar size
  double get avatarSize {
    if (isMobile) return 36;
    if (isTablet) return 40;
    return 44;
  }

  /// Get button height
  double get buttonHeight {
    if (isMobile) return 44;
    if (isTablet) return 48;
    return 52;
  }

  /// Get modal width
  double get modalWidth {
    if (isMobile) return screenWidth * 0.9;
    if (isTablet) return 500;
    return 560;
  }

  /// Get modal max height
  double get modalMaxHeight {
    if (isMobile) return screenHeight * 0.85;
    return screenHeight * 0.8;
  }

  /// Responsive value helper - returns different values based on screen size
  T value<T>({
    required T mobile,
    T? tablet,
    T? desktop,
    T? largeDesktop,
  }) {
    switch (deviceType) {
      case DeviceType.mobileSmall:
      case DeviceType.mobile:
        return mobile;
      case DeviceType.tablet:
        return tablet ?? mobile;
      case DeviceType.desktop:
        return desktop ?? tablet ?? mobile;
      case DeviceType.largeDesktop:
      case DeviceType.ultrawide:
        return largeDesktop ?? desktop ?? tablet ?? mobile;
    }
  }

  /// Get number of columns for a responsive grid
  int columns({
    int mobile = 1,
    int tablet = 2,
    int desktop = 3,
    int? largeDesktop,
  }) {
    return value(
      mobile: mobile,
      tablet: tablet,
      desktop: desktop,
      largeDesktop: largeDesktop ?? desktop + 1,
    );
  }

  @override
  String toString() {
    return 'Responsive(width: $screenWidth, height: $screenHeight, type: $deviceType, orientation: $orientation)';
  }
}

/// Extension for BuildContext to easily access Responsive
extension ResponsiveExtension on BuildContext {
  Responsive get responsive => Responsive.of(this);
}


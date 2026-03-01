import 'package:flutter/material.dart';
import '../../utils/responsive.dart';

/// A widget that builds different layouts based on screen size
class ResponsiveLayout extends StatelessWidget {
  /// Layout for mobile devices (< 600px)
  final Widget mobile;

  /// Layout for tablet devices (600px - 900px)
  final Widget? tablet;

  /// Layout for desktop devices (900px - 1200px)
  final Widget? desktop;

  /// Layout for large desktop devices (> 1200px)
  final Widget? largeDesktop;

  const ResponsiveLayout({
    super.key,
    required this.mobile,
    this.tablet,
    this.desktop,
    this.largeDesktop,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final responsive = Responsive(
          screenWidth: constraints.maxWidth,
          screenHeight: constraints.maxHeight,
        );

        if (responsive.isLargeDesktop && largeDesktop != null) {
          return largeDesktop!;
        }

        if (responsive.isDesktop && desktop != null) {
          return desktop!;
        }

        if (responsive.isTablet && tablet != null) {
          return tablet!;
        }

        return mobile;
      },
    );
  }
}

/// A widget that applies responsive constraints to its child
class ResponsiveContainer extends StatelessWidget {
  final Widget child;
  final double? maxWidth;
  final EdgeInsetsGeometry? padding;
  final bool center;

  const ResponsiveContainer({
    super.key,
    required this.child,
    this.maxWidth,
    this.padding,
    this.center = true,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final responsive = Responsive(
          screenWidth: constraints.maxWidth,
          screenHeight: constraints.maxHeight,
        );

        final effectiveMaxWidth = maxWidth ?? responsive.contentMaxWidth;
        final effectivePadding = padding ??
            EdgeInsets.symmetric(horizontal: responsive.horizontalPadding);

        Widget content = ConstrainedBox(
          constraints: BoxConstraints(maxWidth: effectiveMaxWidth),
          child: Padding(
            padding: effectivePadding,
            child: child,
          ),
        );

        if (center) {
          content = Center(child: content);
        }

        return content;
      },
    );
  }
}

/// A widget that builds responsive grid layouts
class ResponsiveGrid extends StatelessWidget {
  final List<Widget> children;
  final double spacing;
  final double runSpacing;
  final int? crossAxisCount;
  final double childAspectRatio;

  const ResponsiveGrid({
    super.key,
    required this.children,
    this.spacing = 16,
    this.runSpacing = 16,
    this.crossAxisCount,
    this.childAspectRatio = 1.0,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final responsive = Responsive(
          screenWidth: constraints.maxWidth,
          screenHeight: constraints.maxHeight,
        );

        final columns = crossAxisCount ?? responsive.gridColumnCount;

        return GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: columns,
            crossAxisSpacing: spacing,
            mainAxisSpacing: runSpacing,
            childAspectRatio: childAspectRatio,
          ),
          itemCount: children.length,
          itemBuilder: (context, index) => children[index],
        );
      },
    );
  }
}

/// Wrapper that provides responsive context to child widgets
class ResponsiveBuilder extends StatelessWidget {
  final Widget Function(BuildContext context, Responsive responsive) builder;

  const ResponsiveBuilder({
    super.key,
    required this.builder,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final responsive = Responsive(
          screenWidth: constraints.maxWidth,
          screenHeight: constraints.maxHeight,
        );
        return builder(context, responsive);
      },
    );
  }
}

/// A dialog that's responsive and centered properly on web
class ResponsiveDialog extends StatelessWidget {
  final Widget child;
  final double? maxWidth;
  final double? maxHeight;
  final EdgeInsetsGeometry? padding;

  const ResponsiveDialog({
    super.key,
    required this.child,
    this.maxWidth = 500,
    this.maxHeight,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxWidth: maxWidth ?? 500,
          maxHeight: maxHeight ?? MediaQuery.of(context).size.height * 0.8,
        ),
        child: Padding(
          padding: padding ?? const EdgeInsets.all(24),
          child: child,
        ),
      ),
    );
  }
}


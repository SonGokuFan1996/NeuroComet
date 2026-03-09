import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import '../../utils/responsive.dart';

/// A universal scaffold wrapper that handles safe areas, keyboard,
/// and responsive layouts across all platforms and screen sizes.
class UniversalScaffold extends StatelessWidget {
  final Widget body;
  final PreferredSizeWidget? appBar;
  final Widget? floatingActionButton;
  final FloatingActionButtonLocation? floatingActionButtonLocation;
  final Widget? bottomNavigationBar;
  final Widget? drawer;
  final Widget? endDrawer;
  final Color? backgroundColor;
  final bool resizeToAvoidBottomInset;
  final bool useSafeArea;
  final EdgeInsetsGeometry? padding;

  const UniversalScaffold({
    super.key,
    required this.body,
    this.appBar,
    this.floatingActionButton,
    this.floatingActionButtonLocation,
    this.bottomNavigationBar,
    this.drawer,
    this.endDrawer,
    this.backgroundColor,
    this.resizeToAvoidBottomInset = true,
    this.useSafeArea = true,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    Widget content = body;

    // Apply padding if provided
    if (padding != null) {
      content = Padding(padding: padding!, child: content);
    }

    // Apply safe area on non-web platforms
    if (useSafeArea && !kIsWeb) {
      content = SafeArea(child: content);
    }

    return Scaffold(
      appBar: appBar,
      body: content,
      floatingActionButton: floatingActionButton,
      floatingActionButtonLocation: floatingActionButtonLocation,
      bottomNavigationBar: bottomNavigationBar,
      drawer: drawer,
      endDrawer: endDrawer,
      backgroundColor: backgroundColor,
      resizeToAvoidBottomInset: resizeToAvoidBottomInset,
    );
  }
}

/// A responsive content wrapper that centers content and applies
/// appropriate max-width constraints based on screen size.
class ResponsiveContent extends StatelessWidget {
  final Widget child;
  final double? maxWidth;
  final EdgeInsetsGeometry? padding;
  final bool center;
  final bool scrollable;
  final ScrollController? scrollController;

  const ResponsiveContent({
    super.key,
    required this.child,
    this.maxWidth,
    this.padding,
    this.center = true,
    this.scrollable = false,
    this.scrollController,
  });

  @override
  Widget build(BuildContext context) {
    final responsive = context.responsive;
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
      content = Align(
        alignment: Alignment.topCenter,
        child: content,
      );
    }

    if (scrollable) {
      content = SingleChildScrollView(
        controller: scrollController,
        padding: EdgeInsets.zero,
        child: content,
      );
    }

    return content;
  }
}

/// A responsive list view that adapts to screen size
class ResponsiveListView extends StatelessWidget {
  final List<Widget> children;
  final EdgeInsetsGeometry? padding;
  final ScrollController? controller;
  final double? maxWidth;
  final bool shrinkWrap;
  final ScrollPhysics? physics;

  const ResponsiveListView({
    super.key,
    required this.children,
    this.padding,
    this.controller,
    this.maxWidth,
    this.shrinkWrap = false,
    this.physics,
  });

  @override
  Widget build(BuildContext context) {
    final responsive = context.responsive;
    final effectiveMaxWidth = maxWidth ?? responsive.contentMaxWidth;
    final effectivePadding = padding ??
        EdgeInsets.symmetric(
          horizontal: responsive.horizontalPadding,
          vertical: responsive.verticalPadding,
        );

    return Align(
      alignment: Alignment.topCenter,
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: effectiveMaxWidth),
        child: ListView(
          controller: controller,
          padding: effectivePadding,
          shrinkWrap: shrinkWrap,
          physics: physics,
          children: children,
        ),
      ),
    );
  }
}

/// A responsive grid view that adapts column count to screen size
class ResponsiveGridView extends StatelessWidget {
  final List<Widget> children;
  final int? crossAxisCount;
  final double crossAxisSpacing;
  final double mainAxisSpacing;
  final double childAspectRatio;
  final EdgeInsetsGeometry? padding;
  final ScrollController? controller;
  final bool shrinkWrap;
  final ScrollPhysics? physics;

  const ResponsiveGridView({
    super.key,
    required this.children,
    this.crossAxisCount,
    this.crossAxisSpacing = 16,
    this.mainAxisSpacing = 16,
    this.childAspectRatio = 1.0,
    this.padding,
    this.controller,
    this.shrinkWrap = false,
    this.physics,
  });

  @override
  Widget build(BuildContext context) {
    final responsive = context.responsive;
    final columns = crossAxisCount ?? responsive.gridColumnCount;
    final effectivePadding = padding ??
        EdgeInsets.symmetric(
          horizontal: responsive.horizontalPadding,
          vertical: responsive.verticalPadding,
        );

    return GridView.builder(
      controller: controller,
      padding: effectivePadding,
      shrinkWrap: shrinkWrap,
      physics: physics,
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: columns,
        crossAxisSpacing: crossAxisSpacing,
        mainAxisSpacing: mainAxisSpacing,
        childAspectRatio: childAspectRatio,
      ),
      itemCount: children.length,
      itemBuilder: (context, index) => children[index],
    );
  }
}

/// Platform-aware dialog that displays correctly on all screen sizes
class AdaptiveDialog extends StatelessWidget {
  final String? title;
  final Widget content;
  final List<Widget>? actions;
  final double? maxWidth;
  final EdgeInsetsGeometry? contentPadding;
  final bool scrollable;

  const AdaptiveDialog({
    super.key,
    this.title,
    required this.content,
    this.actions,
    this.maxWidth,
    this.contentPadding,
    this.scrollable = false,
  });

  /// Show a responsive dialog
  static Future<T?> show<T>({
    required BuildContext context,
    required Widget content,
    String? title,
    List<Widget>? actions,
    bool barrierDismissible = true,
  }) {
    final responsive = context.responsive;

    if (responsive.isMobile) {
      // Use bottom sheet on mobile
      return showModalBottomSheet<T>(
        context: context,
        isScrollControlled: true,
        backgroundColor: Colors.transparent,
        builder: (context) => _MobileDialogSheet(
          title: title,
          content: content,
          actions: actions,
        ),
      );
    }

    // Use dialog on larger screens
    return showDialog<T>(
      context: context,
      barrierDismissible: barrierDismissible,
      builder: (context) => AdaptiveDialog(
        title: title,
        content: content,
        actions: actions,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final responsive = context.responsive;
    final effectiveMaxWidth = maxWidth ?? responsive.modalWidth;

    return Dialog(
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxWidth: effectiveMaxWidth,
          maxHeight: responsive.modalMaxHeight,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (title != null)
              Padding(
                padding: const EdgeInsets.fromLTRB(24, 24, 24, 0),
                child: Text(
                  title!,
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
              ),
            Flexible(
              child: Padding(
                padding: contentPadding ?? const EdgeInsets.all(24),
                child: scrollable
                    ? SingleChildScrollView(child: content)
                    : content,
              ),
            ),
            if (actions != null && actions!.isNotEmpty)
              Padding(
                padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: actions!,
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _MobileDialogSheet extends StatelessWidget {
  final String? title;
  final Widget content;
  final List<Widget>? actions;

  const _MobileDialogSheet({
    this.title,
    required this.content,
    this.actions,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.3,
      maxChildSize: 0.9,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              // Drag handle
              Center(
                child: Container(
                  margin: const EdgeInsets.symmetric(vertical: 12),
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              if (title != null)
                Padding(
                  padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
                  child: Text(
                    title!,
                    style: theme.textTheme.headlineSmall,
                  ),
                ),
              Expanded(
                child: SingleChildScrollView(
                  controller: scrollController,
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  child: content,
                ),
              ),
              if (actions != null && actions!.isNotEmpty)
                Padding(
                  padding: EdgeInsets.fromLTRB(
                    24,
                    16,
                    24,
                    MediaQuery.of(context).padding.bottom + 24,
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: actions!,
                  ),
                ),
            ],
          ),
        );
      },
    );
  }
}

/// Adaptive page transitions based on platform
class AdaptivePageRoute<T> extends PageRouteBuilder<T> {
  final Widget page;

  AdaptivePageRoute({required this.page})
      : super(
          pageBuilder: (context, animation, secondaryAnimation) => page,
          transitionsBuilder: (context, animation, secondaryAnimation, child) {
            if (kIsWeb) {
              // Fade transition on web
              return FadeTransition(opacity: animation, child: child);
            }
            // Slide transition on mobile
            return SlideTransition(
              position: Tween<Offset>(
                begin: const Offset(1.0, 0.0),
                end: Offset.zero,
              ).animate(CurvedAnimation(
                parent: animation,
                curve: Curves.easeOutCubic,
              )),
              child: child,
            );
          },
          transitionDuration: kIsWeb
              ? const Duration(milliseconds: 200)
              : const Duration(milliseconds: 300),
        );
}


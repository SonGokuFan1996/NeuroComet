import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter/physics.dart';

/// Custom NavigationBar that fixes Android text baseline issues
/// by wrapping labels in proper text height behavior
class NeuroNavigationBar extends StatelessWidget {
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NeuroNavigationDestination> destinations;
  final double height;
  final NavigationDestinationLabelBehavior labelBehavior;

  /// When true the entire bar renders as frosted glass with a
  /// sliding glass indicator pill behind the selected item.
  final bool isLiquidGlass;

  /// The glass variant style: "frosted" (neutral) or "aurora" (accent-tinted).
  final String glassVariant;

  /// Unified experimental surface variant name.
  /// Supported values: `control`, `frosted`, `aurora`,
  /// `semi_skeumorphic`, `full_skeumorphic`.
  final String surfaceVariant;

  const NeuroNavigationBar({
    super.key,
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.destinations,
    this.height = 56,
    this.labelBehavior = NavigationDestinationLabelBehavior.alwaysShow,
    this.isLiquidGlass = false,
    this.glassVariant = 'frosted',
    this.surfaceVariant = 'control',
  });

  @override
  Widget build(BuildContext context) {
    final effectiveSurfaceVariant = surfaceVariant != 'control'
        ? surfaceVariant
        : (isLiquidGlass ? glassVariant : 'control');

    if (_isLiquidGlassVariant(effectiveSurfaceVariant)) {
      return _LiquidGlassNavBar(
        selectedIndex: selectedIndex,
        onDestinationSelected: onDestinationSelected,
        destinations: destinations,
        height: height,
        variant: effectiveSurfaceVariant,
      );
    }
    if (_isSkeumorphicVariant(effectiveSurfaceVariant)) {
      return _SkeuomorphicNavBar(
        selectedIndex: selectedIndex,
        onDestinationSelected: onDestinationSelected,
        destinations: destinations,
        height: height,
        variant: effectiveSurfaceVariant,
      );
    }
    return _StandardNavBar(
      selectedIndex: selectedIndex,
      onDestinationSelected: onDestinationSelected,
      destinations: destinations,
      height: height,
    );
  }
}

bool _isLiquidGlassVariant(String variant) =>
    variant == 'frosted' || variant == 'aurora';

bool _isSkeumorphicVariant(String variant) =>
    variant == 'semi_skeumorphic' || variant == 'full_skeumorphic';

bool _isFullSkeumorphicVariant(String variant) => variant == 'full_skeumorphic';

// ═══════════════════════════════════════════════════════════════
// STANDARD NAVIGATION BAR (existing)
// ═══════════════════════════════════════════════════════════════

class _StandardNavBar extends StatelessWidget {
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NeuroNavigationDestination> destinations;
  final double height;

  const _StandardNavBar({
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.destinations,
    required this.height,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final bottomPadding = MediaQuery.of(context).padding.bottom;

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        border: Border(
          top: BorderSide(
            color: theme.dividerColor.withValues(alpha: 0.2),
            width: 0.5,
          ),
        ),
      ),
      child: SizedBox(
        height: height + bottomPadding,
        child: Padding(
          padding: EdgeInsets.only(bottom: bottomPadding),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: List.generate(destinations.length, (index) {
              final destination = destinations[index];
              final isSelected = index == selectedIndex;

              return Expanded(
                child: _NeuroNavigationDestination(
                  icon: destination.icon,
                  selectedIcon: destination.selectedIcon ?? destination.icon,
                  label: destination.label,
                  isSelected: isSelected,
                  badgeCount: destination.badgeCount,
                  onTap: () => onDestinationSelected(index),
                ),
              );
            }),
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════

class _LiquidGlassNavBar extends StatelessWidget {
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NeuroNavigationDestination> destinations;
  final double height;
  final String variant;

  const _LiquidGlassNavBar({
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.destinations,
    required this.height,
    this.variant = 'frosted',
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final bottomPadding = MediaQuery.of(context).padding.bottom;

    final tintColor = theme.colorScheme.surface.withValues(
      alpha: isDark ? 0.55 : 0.65,
    );

    // iOS 26–style floating pill: fully rounded, with horizontal margins
    return Padding(
      padding: EdgeInsets.only(left: 16, right: 16, bottom: bottomPadding + 8),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(32),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
          child: Container(
            height: height,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  tintColor,
                  tintColor.withValues(alpha: tintColor.a * 0.6),
                  if (variant == 'aurora')
                    theme.colorScheme.primary.withValues(alpha: 0.08),
                ],
              ),
              borderRadius: BorderRadius.circular(32),
              border: Border.all(
                color: isDark
                    ? Colors.white.withValues(alpha: 0.12)
                    : Colors.white.withValues(alpha: 0.25),
                width: 0.5,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: isDark ? 0.18 : 0.08),
                  blurRadius: 16,
                ),
              ],
            ),
            child: _GlassNavContent(
              selectedIndex: selectedIndex,
              onDestinationSelected: onDestinationSelected,
              destinations: destinations,
              variant: variant,
            ),
          ),
        ),
      ),
    );
  }
}

class _SkeuomorphicNavBar extends StatelessWidget {
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NeuroNavigationDestination> destinations;
  final double height;
  final String variant;

  const _SkeuomorphicNavBar({
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.destinations,
    required this.height,
    required this.variant,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final isFull = _isFullSkeumorphicVariant(variant);
    final bottomPadding = MediaQuery.of(context).padding.bottom;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.16 : 0.08),
      theme.colorScheme.surface,
    );

    return Padding(
      padding: EdgeInsets.only(left: 16, right: 16, bottom: bottomPadding + 8),
      child: DecoratedBox(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(32),
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Color.alphaBlend(
                Colors.white.withValues(alpha: isDark ? 0.06 : 0.22),
                base,
              ),
              base,
              Color.alphaBlend(
                theme.colorScheme.primary.withValues(
                  alpha: isFull ? 0.22 : 0.10,
                ),
                base,
              ),
            ],
          ),
          border: Border.all(
            color: Colors.white.withValues(alpha: isDark ? 0.10 : 0.42),
            width: 1,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: isDark ? 0.28 : 0.12),
              blurRadius: isFull ? 28 : 18,
              offset: Offset(0, isFull ? 16 : 10),
            ),
            BoxShadow(
              color: Colors.white.withValues(alpha: isDark ? 0.03 : 0.50),
              blurRadius: isFull ? 14 : 10,
              offset: const Offset(-5, -5),
            ),
          ],
        ),
        child: SizedBox(
          height: height,
          child: Stack(
            children: [
              LayoutBuilder(
                builder: (context, constraints) {
                  final pillWidth = constraints.maxWidth / destinations.length;
                  return AnimatedPositioned(
                    duration: const Duration(milliseconds: 260),
                    curve: Curves.easeOutCubic,
                    left: pillWidth * selectedIndex + 4,
                    top: 4,
                    bottom: 4,
                    width: pillWidth - 8,
                    child: DecoratedBox(
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(24),
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [
                            Color.alphaBlend(
                              Colors.white.withValues(
                                alpha: isDark ? 0.10 : 0.28,
                              ),
                              theme.colorScheme.surface,
                            ),
                            Color.alphaBlend(
                              theme.colorScheme.primary.withValues(
                                alpha: isFull ? 0.26 : 0.16,
                              ),
                              theme.colorScheme.surface,
                            ),
                          ],
                        ),
                        border: Border.all(
                          color: Colors.white.withValues(
                            alpha: isDark ? 0.08 : 0.36,
                          ),
                          width: 1,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(
                              alpha: isDark ? 0.18 : 0.06,
                            ),
                            blurRadius: isFull ? 18 : 10,
                            offset: Offset(0, isFull ? 10 : 6),
                          ),
                          BoxShadow(
                            color: Colors.white.withValues(
                              alpha: isDark ? 0.02 : 0.35,
                            ),
                            blurRadius: 8,
                            offset: const Offset(-2, -2),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
              Row(
                children: List.generate(destinations.length, (index) {
                  final destination = destinations[index];
                  final isSelected = index == selectedIndex;
                  final itemColor = isSelected
                      ? theme.colorScheme.primary
                      : theme.colorScheme.onSurfaceVariant;

                  return Expanded(
                    child: TweenAnimationBuilder<double>(
                      tween: Tween(begin: 1, end: isSelected ? 1.04 : 1),
                      duration: const Duration(milliseconds: 220),
                      curve: Curves.easeOutCubic,
                      builder: (context, scale, child) =>
                          Transform.scale(scale: scale, child: child),
                      child: _NeuroNavigationDestination(
                        icon: destination.icon,
                        selectedIcon:
                            destination.selectedIcon ?? destination.icon,
                        label: destination.label,
                        isSelected: isSelected,
                        badgeCount: destination.badgeCount,
                        onTap: () => onDestinationSelected(index),
                        customColor: itemColor,
                        showIndicator: false,
                      ),
                    ),
                  );
                }),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// The inner content of the glass navbar: a spring-driven sliding pill + icon/label columns.
class _GlassNavContent extends StatefulWidget {
  final int selectedIndex;
  final ValueChanged<int> onDestinationSelected;
  final List<NeuroNavigationDestination> destinations;
  final String variant;

  const _GlassNavContent({
    required this.selectedIndex,
    required this.onDestinationSelected,
    required this.destinations,
    this.variant = 'frosted',
  });

  @override
  State<_GlassNavContent> createState() => _GlassNavContentState();
}

class _GlassNavContentState extends State<_GlassNavContent>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;
  double _currentLeft = 0;
  double _targetLeft = 0;
  double _itemWidth = 0;
  bool _initialized = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController.unbounded(vsync: this);
    _animation = _controller;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _animateTo(double target) {
    final sim = SpringSimulation(
      const SpringDescription(mass: 1, stiffness: 280, damping: 22),
      _animation.value,
      target,
      (_animation.value - _currentLeft).abs() > 0.01
          ? (_animation.value - _currentLeft) / 0.016 * 0.3
          : 0,
    );
    _currentLeft = _animation.value;
    _targetLeft = target;
    _controller.animateWith(sim);
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final itemCount = widget.destinations.length.clamp(1, 100);
        _itemWidth = constraints.maxWidth / itemCount;
        final newTarget = _itemWidth * widget.selectedIndex;

        if (!_initialized) {
          _controller.value = newTarget;
          _currentLeft = newTarget;
          _targetLeft = newTarget;
          _initialized = true;
        } else if ((newTarget - _targetLeft).abs() > 0.5) {
          _animateTo(newTarget);
        }

        return AnimatedBuilder(
          animation: _animation,
          builder: (context, child) {
            return Stack(
              children: [
                // ── Sliding glass pill ──────────────────────────
                _GlassPill(
                  left: _animation.value,
                  width: _itemWidth,
                  variant: widget.variant,
                ),
                // ── Nav items row ───────────────────────────────
                child!,
              ],
            );
          },
          child: Row(
            children: List.generate(widget.destinations.length, (index) {
              final dest = widget.destinations[index];
              final isSelected = index == widget.selectedIndex;
              return Expanded(
                child: _GlassNavItem(
                  icon: dest.icon,
                  selectedIcon: dest.selectedIcon ?? dest.icon,
                  label: dest.label,
                  isSelected: isSelected,
                  badgeCount: dest.badgeCount,
                  variant: widget.variant,
                  onTap: () => widget.onDestinationSelected(index),
                ),
              );
            }),
          ),
        );
      },
    );
  }
}

/// The static glass pill decoration, positioned by the parent spring animation.
class _GlassPill extends StatelessWidget {
  final double left;
  final double width;
  final String variant;

  const _GlassPill({
    required this.left,
    required this.width,
    this.variant = 'frosted',
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final pillColor = variant == 'aurora'
        ? theme.colorScheme.primary.withValues(alpha: isDark ? 0.20 : 0.14)
        : theme.colorScheme.onSurface.withValues(alpha: isDark ? 0.08 : 0.05);

    final borderColor = isDark
        ? Colors.white.withValues(alpha: 0.12)
        : Colors.white.withValues(alpha: 0.30);

    return Positioned(
      left: left + 4,
      top: 4,
      bottom: 4,
      width: width - 8,
      child: Container(
        decoration: BoxDecoration(
          color: pillColor,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: borderColor, width: 0.5),
          boxShadow: [
            if (variant == 'aurora')
              BoxShadow(
                color: theme.colorScheme.primary.withValues(alpha: 0.10),
                blurRadius: 8,
              ),
            BoxShadow(
              color: Colors.black.withValues(alpha: isDark ? 0.08 : 0.03),
              blurRadius: 4,
            ),
          ],
        ),
        // Inner highlight line at the top for the glass effect
        child: Align(
          alignment: Alignment.topCenter,
          child: Container(
            height: 1,
            margin: const EdgeInsets.symmetric(horizontal: 10, vertical: 1),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(1),
              gradient: LinearGradient(
                colors: [
                  Colors.white.withValues(alpha: isDark ? 0.06 : 0.25),
                  Colors.white.withValues(alpha: isDark ? 0.02 : 0.08),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// A single nav item inside the liquid glass bar.
class _GlassNavItem extends StatelessWidget {
  final Widget icon;
  final Widget selectedIcon;
  final String label;
  final bool isSelected;
  final int? badgeCount;
  final String variant;
  final VoidCallback onTap;

  const _GlassNavItem({
    required this.icon,
    required this.selectedIcon,
    required this.label,
    required this.isSelected,
    this.badgeCount,
    this.variant = 'frosted',
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isAurora = variant == 'aurora';

    final color = isSelected
        ? (isAurora ? theme.colorScheme.primary : theme.colorScheme.onSurface)
        : theme.colorScheme.onSurfaceVariant;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: onTap,
      child: AnimatedScale(
        scale: isSelected ? 1.04 : 1.0,
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOutCubic,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Icon + badge
            SizedBox(
              height: 28,
              child: Stack(
                clipBehavior: Clip.none,
                alignment: Alignment.center,
                children: [
                  AnimatedSwitcher(
                    duration: const Duration(milliseconds: 150),
                    switchInCurve: Curves.easeOut,
                    switchOutCurve: Curves.easeIn,
                    child: IconTheme(
                      key: ValueKey(isSelected),
                      data: IconThemeData(color: color, size: 22),
                      child: isSelected ? selectedIcon : icon,
                    ),
                  ),
                  if (badgeCount != null && badgeCount! > 0)
                    Positioned(
                      right: -6,
                      top: -4,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 4,
                          vertical: 1,
                        ),
                        decoration: BoxDecoration(
                          color: theme.colorScheme.error,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        constraints: const BoxConstraints(minWidth: 14),
                        child: Text(
                          badgeCount! > 99 ? '99+' : badgeCount.toString(),
                          style: TextStyle(
                            color: theme.colorScheme.onError,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 2),
            // Label
            AnimatedDefaultTextStyle(
              duration: const Duration(milliseconds: 150),
              style: TextStyle(
                fontSize: 11,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                color: color,
                height: 1.0,
                leadingDistribution: TextLeadingDistribution.even,
              ),
              child: Text(
                label,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                textHeightBehavior: const TextHeightBehavior(
                  applyHeightToFirstAscent: false,
                  applyHeightToLastDescent: false,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class NeuroNavigationDestination {
  final Widget icon;
  final Widget? selectedIcon;
  final String label;
  final String? tooltip;
  final int? badgeCount;

  const NeuroNavigationDestination({
    required this.icon,
    this.selectedIcon,
    required this.label,
    this.tooltip,
    this.badgeCount,
  });
}

class _NeuroNavigationDestination extends StatelessWidget {
  final Widget icon;
  final Widget selectedIcon;
  final String label;
  final bool isSelected;
  final int? badgeCount;
  final VoidCallback onTap;
  final Color? customColor;
  final bool showIndicator;

  const _NeuroNavigationDestination({
    required this.icon,
    required this.selectedIcon,
    required this.label,
    required this.isSelected,
    this.badgeCount,
    required this.onTap,
    this.customColor,
    this.showIndicator = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color =
        customColor ??
        (isSelected
            ? theme.colorScheme.primary
            : theme.colorScheme.onSurfaceVariant);

    // Use secondaryContainer for reliable visibility across all themes
    final indicatorColor = theme.colorScheme.secondaryContainer;

    return InkWell(
      onTap: onTap,
      customBorder: const StadiumBorder(),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Icon with optional badge
          SizedBox(
            height: 28,
            child: Stack(
              clipBehavior: Clip.none,
              alignment: Alignment.center,
              children: [
                // Indicator background - animated for smooth transitions
                AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  curve: Curves.easeOutCubic,
                  width: showIndicator && isSelected ? 56 : 0,
                  height: 28,
                  decoration: BoxDecoration(
                    color: showIndicator && isSelected
                        ? indicatorColor
                        : indicatorColor.withValues(alpha: 0),
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
                // Icon
                IconTheme(
                  data: IconThemeData(color: color, size: 22),
                  child: isSelected ? selectedIcon : icon,
                ),
                // Badge
                if (badgeCount != null && badgeCount! > 0)
                  Positioned(
                    right: isSelected ? 12 : -4,
                    top: -4,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 4,
                        vertical: 1,
                      ),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.error,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      constraints: const BoxConstraints(minWidth: 14),
                      child: Text(
                        badgeCount! > 99 ? '99+' : badgeCount.toString(),
                        style: TextStyle(
                          color: theme.colorScheme.onError,
                          fontSize: 9,
                          fontWeight: FontWeight.bold,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 2),
          // Label with fixed text rendering
          Text(
            label,
            style: TextStyle(
              fontSize: 11,
              fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
              color: color,
              height: 1.0,
              leadingDistribution: TextLeadingDistribution.even,
            ),
            textHeightBehavior: const TextHeightBehavior(
              applyHeightToFirstAscent: false,
              applyHeightToLastDescent: false,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}

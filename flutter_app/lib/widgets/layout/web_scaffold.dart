import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../utils/responsive.dart';
import '../../core/theme/app_colors.dart';
import '../navigation/neuro_navigation_bar.dart';

/// A production-ready scaffold for web with proper navigation
/// Handles responsive layouts with sidebar, navigation rail, and bottom nav
class WebScaffold extends StatelessWidget {
  final Widget body;
  final String? title;
  final List<WebNavigationItem> navigationItems;
  final int selectedIndex;
  final ValueChanged<int> onNavigationChanged;
  final Widget? floatingActionButton;
  final List<Widget>? actions;
  final Widget? leading;
  final PreferredSizeWidget? bottom;
  final Widget? rightPanel;
  final bool showAppBar;

  const WebScaffold({
    super.key,
    required this.body,
    required this.navigationItems,
    required this.selectedIndex,
    required this.onNavigationChanged,
    this.title,
    this.floatingActionButton,
    this.actions,
    this.leading,
    this.bottom,
    this.rightPanel,
    this.showAppBar = true,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final responsive = Responsive(
          screenWidth: constraints.maxWidth,
          screenHeight: constraints.maxHeight,
        );

        // Mobile layout - use bottom navigation
        if (responsive.useMobileLayout) {
          return _buildMobileLayout(context);
        }

        // Tablet layout - compact navigation rail
        if (responsive.useTabletLayout) {
          return _buildTabletLayout(context, responsive);
        }

        // Desktop layout - use navigation rail or extended sidebar
        return _buildDesktopLayout(context, responsive);
      },
    );
  }

  Widget _buildMobileLayout(BuildContext context) {
    return Scaffold(
      appBar: showAppBar
          ? AppBar(
              title: title != null ? Text(title!) : null,
              leading: leading,
              actions: actions,
              bottom: bottom,
            )
          : null,
      body: SafeArea(child: body),
      floatingActionButton: floatingActionButton,
      bottomNavigationBar: _buildBottomNavigation(context),
    );
  }

  Widget _buildTabletLayout(BuildContext context, Responsive responsive) {
    final theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
        child: Row(
          children: [
            // Compact Navigation Rail
            _buildNavigationRail(context, false, theme),

            // Divider
            VerticalDivider(
              width: 1,
              thickness: 1,
              color: theme.dividerColor.withValues(alpha: 0.3),
            ),

            // Main content area
            Expanded(
              child: Stack(
                children: [
                  body,
                  if (floatingActionButton != null)
                    Positioned(
                      right: 16,
                      bottom: 16,
                      child: floatingActionButton!,
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDesktopLayout(BuildContext context, Responsive responsive) {
    final theme = Theme.of(context);
    final showExtended = responsive.showExtendedNavRail;
    final showRightPanel = responsive.showRightPanel && rightPanel != null;

    return Scaffold(
      body: Row(
        children: [
          // Left Navigation
          _buildNavigationRail(context, showExtended, theme),

          // Divider
          VerticalDivider(
            width: 1,
            thickness: 1,
            color: theme.dividerColor.withValues(alpha: 0.3),
          ),

          // Main content area
          Expanded(
            child: Column(
              children: [
                // App Bar
                if (showAppBar) _buildWebAppBar(context, theme),

                // Content
                Expanded(
                  child: Row(
                    children: [
                      // Main body
                      Expanded(
                        child: Stack(
                          children: [
                            body,
                            if (floatingActionButton != null)
                              Positioned(
                                right: 24,
                                bottom: 24,
                                child: floatingActionButton!,
                              ),
                          ],
                        ),
                      ),

                      // Right panel (if on large screen)
                      if (showRightPanel) ...[
                        VerticalDivider(
                          width: 1,
                          thickness: 1,
                          color: theme.dividerColor.withValues(alpha: 0.3),
                        ),
                        SizedBox(
                          width: responsive.rightPanelWidth,
                          child: rightPanel,
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildNavigationRail(
      BuildContext context, bool extended, ThemeData theme) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      width: extended ? 280 : 72,
      child: Material(
        color: theme.colorScheme.surface,
        child: Column(
          children: [
            // Logo/Brand area
            Container(
              height: 72,
              padding: EdgeInsets.symmetric(
                horizontal: extended ? 20 : 12,
                vertical: 12,
              ),
              child: Row(
                children: [
                  Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          AppColors.primaryPurple,
                          AppColors.secondaryTeal,
                        ],
                      ),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(
                      Icons.psychology,
                      color: Colors.white,
                      size: 24,
                    ),
                  ),
                  if (extended) ...[
                    const SizedBox(width: 12),
                    Text(
                      'NeuroComet',
                      style: theme.textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  ],
                ],
              ),
            ),

            const Divider(height: 1),

            // Navigation items
            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.symmetric(vertical: 8),
                itemCount: navigationItems.length,
                itemBuilder: (context, index) {
                  final item = navigationItems[index];
                  final isSelected = index == selectedIndex;

                  return _WebNavItem(
                    item: item,
                    isSelected: isSelected,
                    extended: extended,
                    onTap: () => onNavigationChanged(index),
                  );
                },
              ),
            ),

            // Bottom section (settings, etc.)
            const Divider(height: 1),
            _buildBottomNavSection(context, extended, theme),
          ],
        ),
      ),
    );
  }

  Widget _buildBottomNavSection(
      BuildContext context, bool extended, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.all(12),
      child: extended
          ? OutlinedButton.icon(
              onPressed: () {
                context.push('/settings');
              },
              icon: const Icon(Icons.settings_outlined),
              label: const Text('Settings'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 12,
                ),
              ),
            )
          : IconButton(
              onPressed: () {
                context.push('/settings');
              },
              icon: const Icon(Icons.settings_outlined),
              tooltip: 'Settings',
            ),
    );
  }

  Widget _buildWebAppBar(BuildContext context, ThemeData theme) {
    return Container(
      height: 64,
      padding: const EdgeInsets.symmetric(horizontal: 24),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        border: Border(
          bottom: BorderSide(
            color: theme.dividerColor.withValues(alpha: 0.3),
          ),
        ),
      ),
      child: Row(
        children: [
          if (leading != null) ...[
            leading!,
            const SizedBox(width: 16),
          ],
          if (title != null)
            Text(
              title!,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          const Spacer(),
          if (actions != null) ...actions!,
        ],
      ),
    );
  }

  Widget _buildBottomNavigation(BuildContext context) {
    return NeuroNavigationBar(
      selectedIndex: selectedIndex,
      onDestinationSelected: onNavigationChanged,
      height: 56,
      destinations: navigationItems.map((item) {
        Widget icon = Icon(item.icon);
        Widget selectedIcon = Icon(item.selectedIcon ?? item.icon);

        return NeuroNavigationDestination(
          icon: icon,
          selectedIcon: selectedIcon,
          label: item.label,
          tooltip: item.tooltip ?? item.label,
          badgeCount: item.badgeCount,
        );
      }).toList(),
    );
  }
}

/// Navigation item for WebScaffold
class WebNavigationItem {
  final IconData icon;
  final IconData? selectedIcon;
  final String label;
  final String? tooltip;
  final int? badgeCount;

  const WebNavigationItem({
    required this.icon,
    this.selectedIcon,
    required this.label,
    this.tooltip,
    this.badgeCount,
  });
}

/// Individual navigation item widget
class _WebNavItem extends StatefulWidget {
  final WebNavigationItem item;
  final bool isSelected;
  final bool extended;
  final VoidCallback onTap;

  const _WebNavItem({
    required this.item,
    required this.isSelected,
    required this.extended,
    required this.onTap,
  });

  @override
  State<_WebNavItem> createState() => _WebNavItemState();
}

class _WebNavItemState extends State<_WebNavItem> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final iconData =
        widget.isSelected ? (widget.item.selectedIcon ?? widget.item.icon) : widget.item.icon;

    return Padding(
      padding: EdgeInsets.symmetric(
        horizontal: widget.extended ? 12 : 8,
        vertical: 2,
      ),
      child: MouseRegion(
        onEnter: (_) => setState(() => _isHovered = true),
        onExit: (_) => setState(() => _isHovered = false),
        cursor: SystemMouseCursors.click,
        child: GestureDetector(
          onTap: widget.onTap,
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            padding: EdgeInsets.symmetric(
              horizontal: widget.extended ? 16 : 12,
              vertical: 12,
            ),
            decoration: BoxDecoration(
              color: widget.isSelected
                  ? theme.colorScheme.primaryContainer
                  : _isHovered
                      ? theme.colorScheme.surfaceContainerHighest
                      : Colors.transparent,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              mainAxisSize: widget.extended ? MainAxisSize.max : MainAxisSize.min,
              mainAxisAlignment:
                  widget.extended ? MainAxisAlignment.start : MainAxisAlignment.center,
              children: [
                Stack(
                  clipBehavior: Clip.none,
                  children: [
                    Icon(
                      iconData,
                      color: widget.isSelected
                          ? theme.colorScheme.primary
                          : theme.colorScheme.onSurfaceVariant,
                      size: 24,
                    ),
                    if (widget.item.badgeCount != null && widget.item.badgeCount! > 0)
                      Positioned(
                        right: -8,
                        top: -4,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 5,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.error,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            widget.item.badgeCount! > 99
                                ? '99+'
                                : widget.item.badgeCount.toString(),
                            style: TextStyle(
                              color: theme.colorScheme.onError,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
                if (widget.extended) ...[
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      widget.item.label,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight:
                            widget.isSelected ? FontWeight.w600 : FontWeight.normal,
                        color: widget.isSelected
                            ? theme.colorScheme.primary
                            : theme.colorScheme.onSurfaceVariant,
                      ),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}


import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/notifications_provider.dart';
import '../../l10n/app_localizations.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../utils/responsive.dart';
import '../../widgets/layout/web_scaffold.dart';
import '../../widgets/navigation/neuro_navigation_bar.dart';
import '../../widgets/brand/liquid_glass.dart';
import '../../services/last_tab_service.dart';

class HomeScreen extends ConsumerStatefulWidget {
  final Widget child;

  const HomeScreen({super.key, required this.child});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  int _calculateSelectedIndex(BuildContext context) {
    final location = GoRouterState.of(context).matchedLocation;
    if (location.startsWith('/explore')) return 1;
    if (location.startsWith('/messages')) return 2;
    if (location.startsWith('/notifications')) return 3;
    if (location.startsWith('/settings')) return 4;
    return 0;
  }

  void _onDestinationSelected(int index) {
    const routes = [
      '/',
      '/explore',
      '/messages',
      '/notifications',
      '/settings',
    ];
    if (index == _calculateSelectedIndex(context)) return; // already here
    final route = routes[index];
    LastTabService.save(route); // fire-and-forget
    context.go(route);
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final unreadCount = ref.watch(unreadNotificationCountProvider);
    final selectedIndex = _calculateSelectedIndex(context);
    final abVariant = ref.watch(devOptionsProvider).abTestVariant;
    final isLiquidGlass = abVariant.isLiquidGlass;
    final isSkeumorphic = abVariant.isSkeumorphic;
    final responsive = context.responsive;

    // Navigation items matching Android version - 5 tabs: Feed, Explore, Messages, Notifications, Settings
    final navigationItems = [
      WebNavigationItem(
        icon: Icons.home_outlined,
        selectedIcon: Icons.home,
        label: l10n.feed,
        tooltip: l10n.home,
      ),
      WebNavigationItem(
        icon: Icons.explore_outlined,
        selectedIcon: Icons.explore,
        label: l10n.explore,
        tooltip: l10n.explore,
      ),
      WebNavigationItem(
        icon: Icons.chat_bubble_outline,
        selectedIcon: Icons.chat_bubble,
        label: l10n.messages,
        tooltip: l10n.messagesTitle,
      ),
      WebNavigationItem(
        icon: Icons.notifications_outlined,
        selectedIcon: Icons.notifications,
        label: l10n.notifications,
        tooltip: l10n.notificationsTitle,
        badgeCount: unreadCount,
      ),
      WebNavigationItem(
        icon: Icons.settings_outlined,
        selectedIcon: Icons.settings,
        label: l10n.settingsTitle,
        tooltip: l10n.settingsTitle,
      ),
    ];

    if (kIsWeb && responsive.useNavigationRail) {
      return WebScaffold(
        body: widget.child,
        navigationItems: navigationItems,
        selectedIndex: selectedIndex,
        onNavigationChanged: _onDestinationSelected,
        showAppBar: false,
        rightPanel: null,
        floatingActionButton: selectedIndex == 0
            ? FloatingActionButton.extended(
                onPressed: () => context.push('/create-post'),
                icon: const Icon(Icons.add),
                label: Text(l10n.createPost),
              )
            : null,
      );
    }

    return Scaffold(
      extendBody: abVariant.usesExperimentalSurfaceChrome,
      body: widget.child,
      bottomNavigationBar: _buildBottomNavigation(
        context,
        selectedIndex,
        navigationItems,
      ),
      floatingActionButton: selectedIndex == 0
          ? isLiquidGlass
                ? LiquidGlassFAB(
                    icon: Icons.add,
                    onPressed: () => context.push('/create-post'),
                    tooltip: l10n.createPost,
                    variant: abVariant.surfaceVariantName,
                  )
                : isSkeumorphic
                ? _SkeuomorphicFab(
                    icon: Icons.add,
                    tooltip: l10n.createPost,
                    isFull: abVariant.isFullSkeumorphic,
                    onPressed: () => context.push('/create-post'),
                  )
                : FloatingActionButton(
                    onPressed: () => context.push('/create-post'),
                    tooltip: l10n.createPost,
                    child: const Icon(Icons.add),
                  )
          : null,
    );
  }

  Widget _buildBottomNavigation(
    BuildContext context,
    int selectedIndex,
    List<WebNavigationItem> items,
  ) {
    final abVariant = ref.watch(devOptionsProvider).abTestVariant;

    return NeuroNavigationBar(
      selectedIndex: selectedIndex,
      onDestinationSelected: _onDestinationSelected,
      height: 56,
      isLiquidGlass: abVariant.usesExperimentalSurfaceChrome,
      glassVariant: abVariant.surfaceVariantName,
      destinations: items.map((item) {
        Widget icon = Icon(item.icon);
        Widget selectedIcon = Icon(item.selectedIcon ?? item.icon);

        return NeuroNavigationDestination(
          icon: icon,
          selectedIcon: selectedIcon,
          label: item.label,
          tooltip: item.tooltip,
          badgeCount: item.badgeCount,
        );
      }).toList(),
    );
  }
}

class _SkeuomorphicFab extends StatelessWidget {
  final IconData icon;
  final String tooltip;
  final bool isFull;
  final VoidCallback onPressed;

  const _SkeuomorphicFab({
    required this.icon,
    required this.tooltip,
    required this.isFull,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.20 : 0.12),
      theme.colorScheme.surface,
    );
    final top = Color.alphaBlend(
      Colors.white.withValues(alpha: isDark ? 0.10 : 0.22),
      base,
    );
    final bottom = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.28 : 0.16),
      base,
    );

    return Tooltip(
      message: tooltip,
      child: DecoratedBox(
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: isDark ? 0.26 : 0.12),
              blurRadius: isFull ? 26 : 18,
              offset: Offset(0, isFull ? 14 : 10),
            ),
            BoxShadow(
              color: Colors.white.withValues(alpha: isDark ? 0.05 : 0.55),
              blurRadius: isFull ? 12 : 8,
              offset: const Offset(-4, -4),
            ),
          ],
        ),
        child: Material(
          color: Colors.transparent,
          shape: const CircleBorder(),
          child: InkWell(
            onTap: onPressed,
            customBorder: const CircleBorder(),
            child: Ink(
              width: 58,
              height: 58,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [top, base, bottom],
                  stops: const [0.0, 0.45, 1.0],
                ),
                border: Border.all(
                  color: Colors.white.withValues(alpha: isDark ? 0.10 : 0.42),
                  width: 1,
                ),
              ),
              child: Icon(
                icon,
                color: isFull
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurface,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

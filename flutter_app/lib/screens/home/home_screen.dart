import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/notifications_provider.dart';
import '../../l10n/app_localizations.dart';
import '../../utils/responsive.dart';
import '../../widgets/layout/web_scaffold.dart';
import '../../widgets/navigation/neuro_navigation_bar.dart';

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
    switch (index) {
      case 0:
        context.go('/');
        break;
      case 1:
        context.go('/explore');
        break;
      case 2:
        context.go('/messages');
        break;
      case 3:
        context.go('/notifications');
        break;
      case 4:
        context.go('/settings');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final unreadCount = ref.watch(unreadNotificationCountProvider);
    final selectedIndex = _calculateSelectedIndex(context);
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
        rightPanel: responsive.isDesktop ? _buildRightPanel(context) : null,
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
      body: widget.child,
      bottomNavigationBar: _buildBottomNavigation(
        context,
        selectedIndex,
        navigationItems,
      ),
      floatingActionButton: selectedIndex == 0
          ? FloatingActionButton(
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
    return NeuroNavigationBar(
      selectedIndex: selectedIndex,
      onDestinationSelected: _onDestinationSelected,
      height: 80,
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

  Widget _buildRightPanel(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Container(
      color: theme.colorScheme.surface,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _RightPanelCard(
            title: 'How are you feeling?',
            icon: Icons.emoji_emotions_outlined,
            child: Wrap(
              spacing: 8,
              children: [
                _MoodChip(emoji: '😊', label: 'Great'),
                _MoodChip(emoji: '😐', label: 'Okay'),
                _MoodChip(emoji: '😔', label: 'Struggling'),
                _MoodChip(emoji: '😰', label: 'Anxious'),
              ],
            ),
            onTap: () => context.push('/settings/neuro-state'),
          ),
          const SizedBox(height: 16),
          _RightPanelCard(
            title: l10n.quickActions,
            icon: Icons.flash_on_outlined,
            child: Column(
              children: [
                _QuickActionTile(
                  icon: Icons.self_improvement,
                  label: l10n.breathingExercise,
                  onTap: () => context.push('/games/breathing_bubbles'),
                ),
                _QuickActionTile(
                  icon: Icons.phone,
                  label: l10n.practiceCall,
                  onTap: () => context.push('/practice-calls'),
                ),
                _QuickActionTile(
                  icon: Icons.add_circle_outline,
                  label: l10n.createStory,
                  onTap: () => context.push('/create-story'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _RightPanelCard(
            title: l10n.trendingTopics,
            icon: Icons.trending_up,
            child: Column(
              children: [
                const _TrendingTopic(
                  tag: '#ADHDTips',
                  count: '2.4k posts',
                ),
                const _TrendingTopic(
                  tag: '#SensoryFriendly',
                  count: '1.8k posts',
                ),
                const _TrendingTopic(
                  tag: '#NeurodivergentWins',
                  count: '1.2k posts',
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.appName,
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: theme.colorScheme.outline,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  l10n.safeSpace,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.outline,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _RightPanelCard extends StatelessWidget {
  final String title;
  final IconData icon;
  final Widget child;
  final VoidCallback? onTap;

  const _RightPanelCard({
    required this.title,
    required this.icon,
    required this.child,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      elevation: 0,
      color: theme.colorScheme.surfaceContainerLow,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(icon, size: 20, color: theme.colorScheme.primary),
                  const SizedBox(width: 8),
                  Text(
                    title,
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              child,
            ],
          ),
        ),
      ),
    );
  }
}

class _MoodChip extends StatelessWidget {
  final String emoji;
  final String label;

  const _MoodChip({required this.emoji, required this.label});

  @override
  Widget build(BuildContext context) {
    return ActionChip(
      avatar: Text(emoji, style: const TextStyle(fontSize: 16)),
      label: Text(label),
      onPressed: () => context.push('/settings/neuro-state'),
    );
  }
}

class _QuickActionTile extends StatefulWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _QuickActionTile({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  State<_QuickActionTile> createState() => _QuickActionTileState();
}

class _QuickActionTileState extends State<_QuickActionTile> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: widget.onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 100),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          decoration: BoxDecoration(
            color: _isHovered
                ? theme.colorScheme.primaryContainer.withOpacity(0.5)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              Icon(
                widget.icon,
                size: 18,
                color: theme.colorScheme.primary,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  widget.label,
                  style: theme.textTheme.bodyMedium,
                ),
              ),
              Icon(
                Icons.chevron_right,
                size: 18,
                color: theme.colorScheme.outline,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TrendingTopic extends StatefulWidget {
  final String tag;
  final String count;

  const _TrendingTopic({required this.tag, required this.count});

  @override
  State<_TrendingTopic> createState() => _TrendingTopicState();
}

class _TrendingTopicState extends State<_TrendingTopic> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: () => context.push('/explore'),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 100),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: _isHovered
                ? theme.colorScheme.surfaceContainerHighest
                : Colors.transparent,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.tag,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                    Text(
                      widget.count,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.outline,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

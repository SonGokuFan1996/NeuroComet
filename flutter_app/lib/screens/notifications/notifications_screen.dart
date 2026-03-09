
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/notification.dart';
import '../../providers/notifications_provider.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../l10n/app_localizations.dart';

/// Notification filter options matching Kotlin version
enum NotificationFilter {
  all,
  unread,
  mentions,
  likes,
  follows,
}

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen>
    with TickerProviderStateMixin {
  NotificationFilter _selectedFilter = NotificationFilter.all;
  late AnimationController _headerAnimationController;
  late Animation<double> _headerFadeAnimation;

  @override
  void initState() {
    super.initState();
    _headerAnimationController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _headerFadeAnimation = CurvedAnimation(
      parent: _headerAnimationController,
      curve: Curves.easeOutCubic,
    );
    _headerAnimationController.forward();
  }

  @override
  void dispose() {
    _headerAnimationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final notificationsState = ref.watch(notificationsProvider);
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: notificationsState.when(
          loading: () => const NeuroLoading(message: 'Loading notifications...'),
          error: (error, stack) => _buildErrorState(context, error.toString(), ref),
          data: (notifications) => _buildContent(context, notifications, ref, l10n),
        ),
      ),
    );
  }

  Widget _buildContent(
    BuildContext context,
    List<AppNotification> notifications,
    WidgetRef ref,
    AppLocalizations l10n,
  ) {
    final unreadCount = notifications.where((n) => !n.isRead).length;
    final filteredNotifications = _filterNotifications(notifications);
    final grouped = _groupNotificationsByTime(filteredNotifications);

    return CustomScrollView(
      physics: const BouncingScrollPhysics(
        parent: AlwaysScrollableScrollPhysics(),
      ),
      slivers: [
        // Custom header with beautiful gradient
        SliverToBoxAdapter(
          child: FadeTransition(
            opacity: _headerFadeAnimation,
            child: _NotificationHeader(
              unreadCount: unreadCount,
              onMarkAllRead: unreadCount > 0
                  ? () {
                      HapticFeedback.lightImpact();
                      ref.read(notificationsProvider.notifier).markAllAsRead();
                    }
                  : null,
              l10n: l10n,
            ),
          ),
        ),

        // Filter pills with smooth scrolling
        SliverToBoxAdapter(
          child: _FilterChipsSection(
            selectedFilter: _selectedFilter,
            unreadCount: unreadCount,
            onFilterChanged: (filter) {
              HapticFeedback.selectionClick();
              setState(() => _selectedFilter = filter);
            },
          ),
        ),

        // Content
        if (filteredNotifications.isEmpty)
          SliverFillRemaining(
            hasScrollBody: false,
            child: _EmptyStateCard(
              filter: _selectedFilter,
              onRefresh: () => ref.read(notificationsProvider.notifier).refresh(),
            ),
          )
        else
          SliverPadding(
            padding: const EdgeInsets.only(bottom: 100),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  // Build the list with section headers
                  int currentIndex = 0;
                  for (final entry in grouped.entries) {
                    // Section header
                    if (index == currentIndex) {
                      return _AnimatedSectionHeader(
                        title: entry.key,
                        count: entry.value.length,
                        animationDelay: currentIndex * 30,
                      );
                    }
                    currentIndex++;

                    // Notifications in this section
                    for (int i = 0; i < entry.value.length; i++) {
                      if (index == currentIndex) {
                        return _EnhancedNotificationTile(
                          notification: entry.value[i],
                          animationDelay: currentIndex * 40,
                          onTap: () => _handleNotificationTap(context, entry.value[i], ref),
                          onDismiss: () {
                            HapticFeedback.mediumImpact();
                            ref.read(notificationsProvider.notifier)
                                .removeNotification(entry.value[i].id);
                          },
                        );
                      }
                      currentIndex++;
                    }
                  }
                  return null;
                },
                childCount: grouped.entries.fold<int>(
                  0,
                  (sum, e) => sum + 1 + e.value.length,
                ),
              ),
            ),
          ),
      ],
    );
  }

  List<AppNotification> _filterNotifications(List<AppNotification> notifications) {
    switch (_selectedFilter) {
      case NotificationFilter.all:
        return notifications;
      case NotificationFilter.unread:
        return notifications.where((n) => !n.isRead).toList();
      case NotificationFilter.mentions:
        return notifications.where((n) => n.type == NotificationType.mention).toList();
      case NotificationFilter.likes:
        return notifications.where((n) => n.type == NotificationType.like).toList();
      case NotificationFilter.follows:
        return notifications.where((n) => n.type == NotificationType.follow).toList();
    }
  }

  Widget _buildErrorState(BuildContext context, String error, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                color: theme.colorScheme.errorContainer.withValues(alpha: 0.3),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.cloud_off_rounded,
                size: 48,
                color: theme.colorScheme.error,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Connection Issue',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'We couldn\'t load your notifications.\nPlease check your connection.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            FilledButton.icon(
              onPressed: () => ref.read(notificationsProvider.notifier).refresh(),
              icon: const Icon(Icons.refresh_rounded),
              label: Text(l10n.retry),
              style: FilledButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Map<String, List<AppNotification>> _groupNotificationsByTime(
    List<AppNotification> notifications,
  ) {
    final today = <AppNotification>[];
    final yesterday = <AppNotification>[];
    final thisWeek = <AppNotification>[];
    final earlier = <AppNotification>[];

    final now = DateTime.now();
    final todayDate = DateTime(now.year, now.month, now.day);
    final yesterdayDate = todayDate.subtract(const Duration(days: 1));
    final weekAgo = todayDate.subtract(const Duration(days: 7));

    for (final notification in notifications) {
      final createdAt = notification.createdAt;
      if (createdAt == null) {
        earlier.add(notification);
        continue;
      }

      final notifDate = DateTime(createdAt.year, createdAt.month, createdAt.day);
      final difference = todayDate.difference(notifDate).inDays;

      if (notifDate.isAtSameMomentAs(todayDate) || difference == 0) {
        today.add(notification);
      } else if (notifDate.isAtSameMomentAs(yesterdayDate) || difference == 1) {
        yesterday.add(notification);
      } else if (notifDate.isAfter(weekAgo) && difference <= 6) {
        thisWeek.add(notification);
      } else {
        earlier.add(notification);
      }
    }

    final result = <String, List<AppNotification>>{};
    if (today.isNotEmpty) result['Today'] = today;
    if (yesterday.isNotEmpty) result['Yesterday'] = yesterday;
    if (thisWeek.isNotEmpty) result['This Week'] = thisWeek;
    if (earlier.isNotEmpty) result['Earlier'] = earlier;

    return result;
  }

  void _handleNotificationTap(
    BuildContext context,
    AppNotification notification,
    WidgetRef ref,
  ) {
    HapticFeedback.lightImpact();
    ref.read(notificationsProvider.notifier).markAsRead(notification.id);

    if (notification.actionUrl != null) {
      Navigator.pushNamed(context, notification.actionUrl!);
      return;
    }

    switch (notification.type) {
      case NotificationType.like:
      case NotificationType.comment:
      case NotificationType.repost:
        if (notification.targetId != null) {
          Navigator.pushNamed(context, '/post', arguments: notification.targetId);
        } else if (notification.relatedPostId != null) {
          Navigator.pushNamed(context, '/post', arguments: notification.relatedPostId.toString());
        }
        break;
      case NotificationType.follow:
        if (notification.actorId != null) {
          Navigator.pushNamed(context, '/profile', arguments: notification.actorId);
        }
        break;
      case NotificationType.message:
        Navigator.pushNamed(context, '/messages');
        break;
      case NotificationType.mention:
        if (notification.targetId != null) {
          Navigator.pushNamed(context, '/post', arguments: notification.targetId);
        }
        break;
      case NotificationType.badge:
      case NotificationType.achievement:
        Navigator.pushNamed(context, '/profile');
        break;
      case NotificationType.welcome:
      case NotificationType.system:
      case NotificationType.safetyAlert:
        break;
    }
  }
}

/// Beautiful header with gradient and animation
class _NotificationHeader extends StatelessWidget {
  final int unreadCount;
  final VoidCallback? onMarkAllRead;
  final AppLocalizations l10n;

  const _NotificationHeader({
    required this.unreadCount,
    this.onMarkAllRead,
    required this.l10n,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        l10n.notificationsTitle,
                        style: theme.textTheme.headlineMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          letterSpacing: -0.5,
                        ),
                      ),
                      if (unreadCount > 0) ...[
                        const SizedBox(width: 12),
                        _UnreadBadge(count: unreadCount),
                      ],
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    unreadCount > 0
                        ? 'You have $unreadCount new ${unreadCount == 1 ? 'notification' : 'notifications'}'
                        : 'You\'re all caught up! ✨',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
              if (onMarkAllRead != null)
                _MarkAllReadButton(onPressed: onMarkAllRead!),
            ],
          ),
        ],
      ),
    );
  }
}

/// Animated unread badge
class _UnreadBadge extends StatefulWidget {
  final int count;

  const _UnreadBadge({required this.count});

  @override
  State<_UnreadBadge> createState() => _UnreadBadgeState();
}

class _UnreadBadgeState extends State<_UnreadBadge>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.1).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _pulseAnimation,
      builder: (context, child) {
        return Transform.scale(
          scale: _pulseAnimation.value,
          child: child,
        );
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [Color(0xFF7C4DFF), Color(0xFF00BFA5)],
          ),
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF7C4DFF).withValues(alpha: 0.3),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Text(
          widget.count > 99 ? '99+' : widget.count.toString(),
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 12,
          ),
        ),
      ),
    );
  }
}

/// Mark all read button with nice styling
class _MarkAllReadButton extends StatelessWidget {
  final VoidCallback onPressed;

  const _MarkAllReadButton({required this.onPressed});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Material(
      color: theme.colorScheme.primaryContainer.withValues(alpha: 0.5),
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onPressed,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.done_all_rounded,
                size: 18,
                color: theme.colorScheme.primary,
              ),
              const SizedBox(width: 6),
              Text(
                'Mark all read',
                style: theme.textTheme.labelLarge?.copyWith(
                  color: theme.colorScheme.primary,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Filter chips with beautiful styling
class _FilterChipsSection extends StatelessWidget {
  final NotificationFilter selectedFilter;
  final int unreadCount;
  final ValueChanged<NotificationFilter> onFilterChanged;

  const _FilterChipsSection({
    required this.selectedFilter,
    required this.unreadCount,
    required this.onFilterChanged,
  });

  @override
  Widget build(BuildContext context) {

    final filters = [
      (NotificationFilter.all, 'All', Icons.notifications_rounded, null),
      (NotificationFilter.unread, 'Unread', Icons.mark_email_unread_rounded, unreadCount > 0 ? unreadCount : null),
      (NotificationFilter.mentions, 'Mentions', Icons.alternate_email_rounded, null),
      (NotificationFilter.likes, 'Likes', Icons.favorite_rounded, null),
      (NotificationFilter.follows, 'Follows', Icons.person_add_rounded, null),
    ];

    return Container(
      height: 56,
      margin: const EdgeInsets.only(top: 8),
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 20),
        separatorBuilder: (_, __) => const SizedBox(width: 10),
        itemCount: filters.length,
        itemBuilder: (context, index) {
          final (filter, label, icon, count) = filters[index];
          final isSelected = selectedFilter == filter;

          return _FilterPill(
            label: label,
            icon: icon,
            count: count,
            isSelected: isSelected,
            onTap: () => onFilterChanged(filter),
          );
        },
      ),
    );
  }
}

/// Individual filter pill with animation
class _FilterPill extends StatefulWidget {
  final String label;
  final IconData icon;
  final int? count;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterPill({
    required this.label,
    required this.icon,
    this.count,
    required this.isSelected,
    required this.onTap,
  });

  @override
  State<_FilterPill> createState() => _FilterPillState();
}

class _FilterPillState extends State<_FilterPill>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 150),
      vsync: this,
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.95).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;

    return GestureDetector(
      onTapDown: (_) => _controller.forward(),
      onTapUp: (_) {
        _controller.reverse();
        widget.onTap();
      },
      onTapCancel: () => _controller.reverse(),
      child: AnimatedBuilder(
        animation: _scaleAnimation,
        builder: (context, child) {
          return Transform.scale(
            scale: _scaleAnimation.value,
            child: child,
          );
        },
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOutCubic,
          padding: EdgeInsets.symmetric(
            horizontal: widget.isSelected ? 16 : 14,
            vertical: 10,
          ),
          decoration: BoxDecoration(
            color: widget.isSelected
                ? primaryColor.withValues(alpha: 0.15)
                : theme.colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(20),
            border: widget.isSelected
                ? Border.all(
                    color: primaryColor.withValues(alpha: 0.4),
                    width: 1.5,
                  )
                : null,
            boxShadow: widget.isSelected
                ? [
                    BoxShadow(
                      color: primaryColor.withValues(alpha: 0.15),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ]
                : null,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                widget.icon,
                size: 16,
                color: widget.isSelected
                    ? primaryColor
                    : theme.colorScheme.onSurfaceVariant,
              ),
              const SizedBox(width: 6),
              Text(
                widget.label,
                style: theme.textTheme.labelLarge?.copyWith(
                  color: widget.isSelected
                      ? primaryColor
                      : theme.colorScheme.onSurface,
                  fontWeight: widget.isSelected ? FontWeight.w600 : FontWeight.w500,
                ),
              ),
              if (widget.count != null) ...[
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: primaryColor.withValues(alpha: 0.2),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    widget.count.toString(),
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: primaryColor,
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Section header with animation
class _AnimatedSectionHeader extends StatefulWidget {
  final String title;
  final int count;
  final int animationDelay;

  const _AnimatedSectionHeader({
    required this.title,
    required this.count,
    this.animationDelay = 0,
  });

  @override
  State<_AnimatedSectionHeader> createState() => _AnimatedSectionHeaderState();
}

class _AnimatedSectionHeaderState extends State<_AnimatedSectionHeader>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<Offset> _slideAnimation;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(-0.1, 0),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutCubic));
    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeOut),
    );

    Future.delayed(Duration(milliseconds: widget.animationDelay), () {
      if (mounted) _controller.forward();
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return SlideTransition(
      position: _slideAnimation,
      child: FadeTransition(
        opacity: _fadeAnimation,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
          child: Row(
            children: [
              Container(
                width: 4,
                height: 18,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [Color(0xFF7C4DFF), Color(0xFF00BFA5)],
                  ),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(width: 10),
              Text(
                widget.title,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.5,
                ),
              ),
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text(
                  widget.count.toString(),
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Beautiful empty state with illustrations
class _EmptyStateCard extends StatefulWidget {
  final NotificationFilter filter;
  final VoidCallback onRefresh;

  const _EmptyStateCard({
    required this.filter,
    required this.onRefresh,
  });

  @override
  State<_EmptyStateCard> createState() => _EmptyStateCardState();
}

class _EmptyStateCardState extends State<_EmptyStateCard>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    );
    _scaleAnimation = Tween<double>(begin: 0.8, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.elasticOut),
    );
    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _controller, curve: const Interval(0, 0.5)),
    );
    _controller.forward();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final primaryColor = theme.colorScheme.primary;
    final tertiaryColor = theme.colorScheme.tertiary;
    final secondaryColor = theme.colorScheme.secondary;

    final (IconData icon, String emoji, String title, String message, Color color) =
        switch (widget.filter) {
      NotificationFilter.all => (
        Icons.notifications_none_rounded,
        '🌟',
        'Your notification center',
        'When someone interacts with your posts or follows you, you\'ll see it here.',
        primaryColor,
      ),
      NotificationFilter.unread => (
        Icons.check_circle_rounded,
        '✨',
        'All caught up!',
        'You\'ve read all your notifications. Great job staying on top of things!',
        secondaryColor,
      ),
      NotificationFilter.mentions => (
        Icons.alternate_email_rounded,
        '💬',
        'No mentions yet',
        'When someone mentions you in a post or comment, you\'ll find it here.',
        tertiaryColor,
      ),
      NotificationFilter.likes => (
        Icons.favorite_rounded,
        '💜',
        'No likes yet',
        'When someone appreciates your content with a like, it\'ll show up here.',
        theme.colorScheme.error,
      ),
      NotificationFilter.follows => (
        Icons.people_rounded,
        '🤝',
        'No new followers',
        'When someone new follows you to join your journey, they\'ll appear here.',
        tertiaryColor,
      ),
    };

    return FadeTransition(
      opacity: _fadeAnimation,
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Decorative icon container
              Stack(
                alignment: Alignment.center,
                children: [
                  // Background glow
                  Container(
                    width: 140,
                    height: 140,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: RadialGradient(
                        colors: [
                          color.withValues(alpha: 0.2),
                          color.withValues(alpha: 0),
                        ],
                      ),
                    ),
                  ),
                  // Main circle
                  Container(
                    width: 100,
                    height: 100,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: isDark
                          ? color.withValues(alpha: 0.15)
                          : color.withValues(alpha: 0.1),
                      border: Border.all(
                        color: color.withValues(alpha: 0.3),
                        width: 2,
                      ),
                    ),
                    child: Center(
                      child: Text(
                        emoji,
                        style: const TextStyle(fontSize: 40),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 32),
              Text(
                title,
                style: theme.textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Text(
                  message,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                    height: 1.5,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              if (widget.filter == NotificationFilter.all) ...[
                const SizedBox(height: 32),
                FilledButton.icon(
                  onPressed: widget.onRefresh,
                  icon: const Icon(Icons.refresh_rounded),
                  label: const Text('Refresh'),
                  style: FilledButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Enhanced notification tile with premium feel
class _EnhancedNotificationTile extends StatefulWidget {
  final AppNotification notification;
  final VoidCallback? onTap;
  final VoidCallback? onDismiss;
  final int animationDelay;

  const _EnhancedNotificationTile({
    required this.notification,
    this.onTap,
    this.onDismiss,
    this.animationDelay = 0,
  });

  @override
  State<_EnhancedNotificationTile> createState() => _EnhancedNotificationTileState();
}

class _EnhancedNotificationTileState extends State<_EnhancedNotificationTile>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeOut),
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0.05, 0),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _animationController, curve: Curves.easeOutCubic));

    Future.delayed(Duration(milliseconds: widget.animationDelay), () {
      if (mounted) _animationController.forward();
    });
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final notification = widget.notification;
    final (icon, color, gradient) = _getNotificationStyle(notification.type);

    return FadeTransition(
      opacity: _fadeAnimation,
      child: SlideTransition(
        position: _slideAnimation,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          child: Material(
            color: notification.isRead
                ? (isDark ? Colors.white.withValues(alpha: 0.03) : Colors.white)
                : (isDark
                    ? theme.colorScheme.primaryContainer.withValues(alpha: 0.15)
                    : theme.colorScheme.primaryContainer.withValues(alpha: 0.3)),
            borderRadius: BorderRadius.circular(16),
            clipBehavior: Clip.antiAlias,
            elevation: notification.isRead ? 0 : 1,
            shadowColor: theme.colorScheme.primary.withValues(alpha: 0.1),
            child: InkWell(
              onTap: widget.onTap,
              borderRadius: BorderRadius.circular(16),
              child: Container(
                padding: const EdgeInsets.fromLTRB(14, 14, 6, 14),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: notification.isRead
                        ? (isDark ? Colors.white.withValues(alpha: 0.06) : Colors.black.withValues(alpha: 0.04))
                        : theme.colorScheme.primary.withValues(alpha: 0.2),
                  ),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Avatar with type indicator
                    _NotificationAvatarEnhanced(
                      avatarUrl: notification.actorAvatarUrl,
                      actorName: notification.actorName,
                      icon: icon,
                      color: color,
                      gradient: gradient,
                    ),
                    const SizedBox(width: 14),

                    // Content
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                child: Text(
                                  notification.title ?? notification.actorName ?? 'Notification',
                                  style: theme.textTheme.titleSmall?.copyWith(
                                    fontWeight: notification.isRead
                                        ? FontWeight.w500
                                        : FontWeight.bold,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              const SizedBox(width: 4),
                              _TimeChip(
                                dateTime: notification.createdAt,
                                isUnread: !notification.isRead,
                              ),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Text(
                            notification.message,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                              height: 1.4,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          // Action hint
                          if (!notification.isRead)
                            Padding(
                              padding: const EdgeInsets.only(top: 10),
                              child: Row(
                                children: [
                                  Icon(
                                    Icons.touch_app_rounded,
                                    size: 14,
                                    color: theme.colorScheme.primary.withValues(alpha: 0.7),
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    'Tap to view',
                                    style: theme.textTheme.labelSmall?.copyWith(
                                      color: theme.colorScheme.primary.withValues(alpha: 0.7),
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                        ],
                      ),
                    ),

                    // Delete button
                    SizedBox(
                      width: 36,
                      height: 36,
                      child: IconButton(
                        onPressed: () {
                          HapticFeedback.mediumImpact();
                          widget.onDismiss?.call();
                        },
                        icon: Icon(
                          Icons.close_rounded,
                          size: 18,
                          color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
                        ),
                        padding: EdgeInsets.zero,
                        constraints: const BoxConstraints(),
                        splashRadius: 18,
                        tooltip: 'Remove',
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  (IconData, Color, List<Color>) _getNotificationStyle(NotificationType type) {
    return switch (type) {
      NotificationType.like => (
        Icons.favorite_rounded,
        const Color(0xFFE91E63),
        [const Color(0xFFE91E63), const Color(0xFFFF5252)],
      ),
      NotificationType.comment => (
        Icons.chat_bubble_rounded,
        const Color(0xFF5C6BC0),
        [const Color(0xFF5C6BC0), const Color(0xFF7986CB)],
      ),
      NotificationType.follow => (
        Icons.person_add_rounded,
        const Color(0xFF0EA5E9),
        [const Color(0xFF0EA5E9), const Color(0xFF38BDF8)],
      ),
      NotificationType.mention => (
        Icons.alternate_email_rounded,
        const Color(0xFF8B5CF6),
        [const Color(0xFF8B5CF6), const Color(0xFFA78BFA)],
      ),
      NotificationType.message => (
        Icons.mail_rounded,
        const Color(0xFF10B981),
        [const Color(0xFF10B981), const Color(0xFF34D399)],
      ),
      NotificationType.repost => (
        Icons.repeat_rounded,
        const Color(0xFF10B981),
        [const Color(0xFF10B981), const Color(0xFF34D399)],
      ),
      NotificationType.badge || NotificationType.achievement => (
        Icons.emoji_events_rounded,
        const Color(0xFFF59E0B),
        [const Color(0xFFF59E0B), const Color(0xFFFBBF24)],
      ),
      NotificationType.system => (
        Icons.shield_rounded,
        const Color(0xFF0D9488),
        [const Color(0xFF0D9488), const Color(0xFF14B8A6)],
      ),
      NotificationType.welcome => (
        Icons.celebration_rounded,
        const Color(0xFF6366F1),
        [const Color(0xFF6366F1), const Color(0xFF818CF8)],
      ),
      NotificationType.safetyAlert => (
        Icons.warning_rounded,
        const Color(0xFFEF4444),
        [const Color(0xFFEF4444), const Color(0xFFF87171)],
      ),
    };
  }
}

/// Time chip showing relative time
class _TimeChip extends StatelessWidget {
  final DateTime? dateTime;
  final bool isUnread;

  const _TimeChip({
    required this.dateTime,
    required this.isUnread,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final timeText = _formatTime(dateTime);

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          timeText,
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.outline,
          ),
        ),
        if (isUnread) ...[
          const SizedBox(width: 6),
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: const LinearGradient(
                colors: [Color(0xFF7C4DFF), Color(0xFF00BFA5)],
              ),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF7C4DFF).withValues(alpha: 0.4),
                  blurRadius: 4,
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  String _formatTime(DateTime? dateTime) {
    if (dateTime == null) return '';

    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 6) {
      return '${dateTime.day}/${dateTime.month}';
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m';
    } else {
      return 'now';
    }
  }
}

/// Enhanced avatar with gradient badge
class _NotificationAvatarEnhanced extends StatelessWidget {
  final String? avatarUrl;
  final String? actorName;
  final IconData icon;
  final Color color;
  final List<Color> gradient;

  const _NotificationAvatarEnhanced({
    required this.avatarUrl,
    this.actorName,
    required this.icon,
    required this.color,
    required this.gradient,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (avatarUrl != null || actorName != null) {
      return SizedBox(
        width: 52,
        height: 52,
        child: Stack(
          children: [
            NeuroAvatar(
              imageUrl: avatarUrl,
              name: actorName,
              size: 48,
            ),
            Positioned(
              right: 0,
              bottom: 0,
              child: Container(
                width: 22,
                height: 22,
                decoration: BoxDecoration(
                  gradient: LinearGradient(colors: gradient),
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: theme.scaffoldBackgroundColor,
                    width: 2,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: color.withValues(alpha: 0.4),
                      blurRadius: 6,
                    ),
                  ],
                ),
                child: Icon(
                  icon,
                  size: 12,
                  color: Colors.white,
                ),
              ),
            ),
          ],
        ),
      );
    }

    // System notification style
    return Container(
      width: 52,
      height: 52,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            color.withValues(alpha: 0.2),
            gradient.last.withValues(alpha: 0.1),
          ],
        ),
        shape: BoxShape.circle,
        border: Border.all(
          color: color.withValues(alpha: 0.3),
          width: 1.5,
        ),
      ),
      child: Icon(
        icon,
        size: 24,
        color: color,
      ),
    );
  }
}

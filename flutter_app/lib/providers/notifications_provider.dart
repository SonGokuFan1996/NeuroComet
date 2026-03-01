import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/notification.dart';
import '../models/dev_options.dart';
import '../screens/settings/dev_options_screen.dart';

final notificationsProvider = NotifierProvider<NotificationsNotifier, AsyncValue<List<AppNotification>>>(
  NotificationsNotifier.new,
);

final unreadNotificationCountProvider = Provider<int>((ref) {
  final notifications = ref.watch(notificationsProvider);
  return notifications.maybeWhen(
    data: (list) => list.where((n) => !n.isRead).length,
    orElse: () => 0,
  );
});

class NotificationsNotifier extends Notifier<AsyncValue<List<AppNotification>>> {
  /// Read current dev options (returns safe defaults in release mode)
  DevOptions get _devOptions => ref.read(devOptionsProvider);

  @override
  AsyncValue<List<AppNotification>> build() {
    // Watch dev options so notifications rebuild when flags change
    ref.watch(devOptionsProvider);
    loadNotifications();
    return const AsyncValue.loading();
  }

  Future<void> loadNotifications() async {
    final opts = _devOptions;
    state = const AsyncValue.loading();

    // Feature flag: infinite loading – never resolve
    if (opts.infiniteLoading) {
      debugPrint('[DevFlag] infiniteLoading active – notifications will stay loading');
      return;
    }

    // Feature flag: simulate loading error
    if (opts.simulateLoadingError) {
      debugPrint('[DevFlag] simulateLoadingError active – notifications will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('Simulated loading error (dev flag)'),
        StackTrace.current,
      );
      return;
    }

    // Feature flag: simulate offline
    if (opts.simulateOffline) {
      debugPrint('[DevFlag] simulateOffline active – notifications will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('No network connection (simulated offline)'),
        StackTrace.current,
      );
      return;
    }

    try {
      final notifications = await _fetchNotifications();
      state = AsyncValue.data(notifications);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    await loadNotifications();
  }

  Future<List<AppNotification>> _fetchNotifications() async {
    final opts = _devOptions;

    // Feature flag: artificial network latency
    final latency = opts.networkLatencyMs;
    if (latency > 0) {
      debugPrint('[DevFlag] Adding ${latency}ms artificial latency to notifications');
      await Future.delayed(Duration(milliseconds: latency));
    }

    // Mock data - replace with Supabase call
    await Future.delayed(const Duration(milliseconds: 500));

    return [
      AppNotification(
        id: 'notif_1',
        type: NotificationType.like,
        message: 'liked your post',
        actorId: 'user_1',
        actorName: 'Alex Thompson',
        actorAvatarUrl: 'https://i.pravatar.cc/150?img=1',
        targetId: 'post_1',
        targetType: 'post',
        isRead: false,
        createdAt: DateTime.now().subtract(const Duration(minutes: 10)),
      ),
      AppNotification(
        id: 'notif_2',
        type: NotificationType.comment,
        message: 'commented on your post: "This is amazing!"',
        actorId: 'user_2',
        actorName: 'Jamie Wilson',
        actorAvatarUrl: 'https://i.pravatar.cc/150?img=2',
        targetId: 'post_1',
        targetType: 'post',
        isRead: false,
        createdAt: DateTime.now().subtract(const Duration(hours: 1)),
      ),
      AppNotification(
        id: 'notif_3',
        type: NotificationType.follow,
        message: 'started following you',
        actorId: 'user_3',
        actorName: 'Sarah Chen',
        actorAvatarUrl: 'https://i.pravatar.cc/150?img=3',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(hours: 3)),
      ),
      AppNotification(
        id: 'notif_4',
        type: NotificationType.achievement,
        message: 'You earned the "First Post" badge! 🎉',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(days: 1)),
      ),
      AppNotification(
        id: 'notif_5',
        type: NotificationType.mention,
        message: 'mentioned you in a comment',
        actorId: 'user_4',
        actorName: 'Mike Roberts',
        actorAvatarUrl: 'https://i.pravatar.cc/150?img=4',
        targetId: 'post_2',
        targetType: 'post',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(days: 2)),
      ),
      AppNotification(
        id: 'notif_6',
        type: NotificationType.system,
        message: 'Welcome to NeuroComet! Start by completing your profile.',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(days: 7)),
      ),
    ];
  }

  void markAsRead(String notificationId) {
    final currentNotifications = state.value;
    if (currentNotifications == null) return;

    state = AsyncValue.data(
      currentNotifications.map((notif) {
        if (notif.id == notificationId) {
          return notif.copyWith(isRead: true);
        }
        return notif;
      }).toList(),
    );
  }

  void markAllAsRead() {
    final currentNotifications = state.value;
    if (currentNotifications == null) return;

    state = AsyncValue.data(
      currentNotifications.map((notif) => notif.copyWith(isRead: true)).toList(),
    );
  }

  void removeNotification(String notificationId) {
    final currentNotifications = state.value;
    if (currentNotifications == null) return;

    state = AsyncValue.data(
      currentNotifications.where((notif) => notif.id != notificationId).toList(),
    );
  }
}

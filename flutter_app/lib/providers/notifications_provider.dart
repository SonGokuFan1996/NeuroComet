import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/notification.dart';
import '../screens/settings/dev_options_screen.dart';
import '../services/app_services.dart';
import '../services/supabase_service.dart';

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
  final List<AppNotification> _devInjectedNotifications = <AppNotification>[];

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
      final queuedNotifications = await _consumeQueuedNotifications();
      state = AsyncValue.data(
        _mergeNotifications([
          ..._devInjectedNotifications,
          ...queuedNotifications,
          ...notifications,
        ]),
      );
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    await loadNotifications();
  }

  Future<int> importQueuedNotifications() async {
    final queuedNotifications = await _consumeQueuedNotifications();
    if (queuedNotifications.isEmpty) return 0;

    final currentNotifications = state.value ?? const <AppNotification>[];
    state = AsyncValue.data(
      _mergeNotifications([
        ..._devInjectedNotifications,
        ...queuedNotifications,
        ...currentNotifications,
      ]),
    );
    return queuedNotifications.length;
  }

  Future<void> resetMockNotifications() async {
    _devInjectedNotifications.clear();
    await NotificationService().clearQueuedNotifications();
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

    // Use Supabase when authenticated with a real user
    // Skip Supabase when devModeSkipAuth is active (no real user) so mock data is used
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && SupabaseService.currentUser != null) {
      try {
        final notifications = await SupabaseService.getNotifications();
        debugPrint('[Notifications] Fetched ${notifications.length} notifications from Supabase');
        return notifications;
      } catch (e) {
        debugPrint('[Notifications] Supabase fetch failed: $e — using demo data');
      }
    }

    // Fallback demo data (only when not authenticated or on error)
    return _buildFallbackNotifications();
  }

  List<AppNotification> _buildFallbackNotifications() {
    return [
      AppNotification(
        id: 'notif_1',
        type: NotificationType.like,
        message: 'liked your post',
        actorId: 'user_1',
        actorName: 'Alex Thompson',
        actorAvatarUrl: 'https://i.pravatar.cc/150?u=alex_thompson',
        targetId: 'post_1',
        targetType: 'post',
        actionUrl: '/post/post_1',
        isRead: false,
        createdAt: DateTime.now().subtract(const Duration(minutes: 10)),
      ),
      AppNotification(
        id: 'notif_2',
        type: NotificationType.comment,
        message: 'commented on your post: "This is amazing!"',
        actorId: 'user_2',
        actorName: 'Jamie Wilson',
        actorAvatarUrl: 'https://i.pravatar.cc/150?u=jamie_wilson',
        targetId: 'post_1',
        targetType: 'post',
        actionUrl: '/post/post_1',
        isRead: false,
        createdAt: DateTime.now().subtract(const Duration(hours: 1)),
      ),
      AppNotification(
        id: 'notif_3',
        type: NotificationType.follow,
        message: 'started following you',
        actorId: 'user_3',
        actorName: 'Sarah Chen',
        actorAvatarUrl: 'https://i.pravatar.cc/150?u=sarahchen',
        actionUrl: '/profile/user_3',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(hours: 3)),
      ),
      AppNotification(
        id: 'notif_4',
        type: NotificationType.achievement,
        message: 'You earned the "First Post" badge! 🎉',
        actionUrl: '/profile',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(days: 1)),
      ),
      AppNotification(
        id: 'notif_5',
        type: NotificationType.mention,
        message: 'mentioned you in a comment',
        actorId: 'user_4',
        actorName: 'Mike Roberts',
        actorAvatarUrl: 'https://i.pravatar.cc/150?u=mike_roberts',
        targetId: 'post_2',
        targetType: 'post',
        actionUrl: '/post/post_2',
        isRead: true,
        createdAt: DateTime.now().subtract(const Duration(days: 2)),
      ),
      AppNotification(
        id: 'notif_6',
        type: NotificationType.welcome,
        message: 'Welcome to NeuroComet! Start by completing your profile.',
        actionUrl: '/profile',
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

    // Persist to Supabase
    _syncMarkRead(notificationId);
  }

  void markAllAsRead() {
    final currentNotifications = state.value;
    if (currentNotifications == null) return;

    state = AsyncValue.data(
      currentNotifications.map((notif) => notif.copyWith(isRead: true)).toList(),
    );

    // Persist all to Supabase
    _syncMarkAllRead();
  }

  void removeNotification(String notificationId) {
    final currentNotifications = state.value;
    if (currentNotifications == null) return;

    state = AsyncValue.data(
      currentNotifications.where((notif) => notif.id != notificationId).toList(),
    );

    // Persist deletion to Supabase
    _syncDeleteNotification(notificationId);
  }

  /// Inject a notification from dev tools (debug only).
  void injectDevNotification(AppNotification notification) {
    _devInjectedNotifications.removeWhere((item) => item.id == notification.id);
    _devInjectedNotifications.insert(0, notification);

    final current = state.value ?? const <AppNotification>[];
    state = AsyncValue.data(
      _mergeNotifications([
        notification,
        ..._devInjectedNotifications,
        ...current,
      ]),
    );
  }

  void clearDevNotifications() {
    _devInjectedNotifications.clear();
    final current = state.value ?? const <AppNotification>[];
    state = AsyncValue.data(
      current.where((notification) => !notification.id.startsWith('dev_')).toList(),
    );
  }

  List<AppNotification> _mergeNotifications(Iterable<AppNotification> notifications) {
    final byId = <String, AppNotification>{};
    for (final notification in notifications) {
      byId[notification.id] = notification;
    }

    final merged = byId.values.toList();
    merged.sort((a, b) {
      final aTime = a.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      final bTime = b.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      return bTime.compareTo(aTime);
    });
    return merged;
  }

  Future<List<AppNotification>> _consumeQueuedNotifications() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final queued = prefs.getStringList('pending_notifications') ?? const <String>[];
      if (queued.isEmpty) return const <AppNotification>[];

      final notifications = queued.map(_queuedItemToNotification).toList();
      await prefs.remove('pending_notifications');
      return notifications;
    } catch (e) {
      debugPrint('[Notifications] Failed to consume queued notifications: $e');
      return const <AppNotification>[];
    }
  }

  AppNotification _queuedItemToNotification(String raw) {
    try {
      final decoded = jsonDecode(raw);
      if (decoded is Map<String, dynamic>) {
        final createdAt = DateTime.tryParse(decoded['createdAt']?.toString() ?? '');
        return AppNotification(
          id: decoded['id']?.toString() ?? 'queued_${(createdAt ?? DateTime.now()).microsecondsSinceEpoch}',
          type: _notificationTypeFromString(decoded['type']?.toString()),
          title: decoded['title']?.toString(),
          message: decoded['message']?.toString() ?? decoded['body']?.toString() ?? '',
          actorId: decoded['actorId']?.toString(),
          actorName: decoded['actorName']?.toString() ?? 'Local Notification',
          actorAvatarUrl: decoded['actorAvatarUrl']?.toString(),
          targetId: decoded['targetId']?.toString(),
          targetType: decoded['targetType']?.toString(),
          actionUrl: decoded['actionUrl']?.toString() ?? '/notifications',
          relatedPostId: decoded['relatedPostId'] is num
              ? (decoded['relatedPostId'] as num).toInt()
              : int.tryParse(decoded['relatedPostId']?.toString() ?? ''),
          isRead: false,
          createdAt: createdAt ?? DateTime.now(),
        );
      }
    } catch (_) {
      // Fall back to legacy delimited payloads.
    }

    final parts = raw.split('|');
    final title = parts.isNotEmpty ? parts[0] : 'Notification';
    final body = parts.length > 1 ? parts[1] : '';
    final timestamp = parts.length > 2 ? DateTime.tryParse(parts[2]) : null;
    final createdAt = timestamp ?? DateTime.now();
    return AppNotification(
      id: 'queued_${createdAt.microsecondsSinceEpoch}',
      type: NotificationType.system,
      title: title,
      message: body,
      actorName: 'Local Notification',
      actionUrl: '/notifications',
      isRead: false,
      createdAt: createdAt,
    );
  }

  NotificationType _notificationTypeFromString(String? rawType) {
    if (rawType == null || rawType.isEmpty) return NotificationType.system;
    try {
      return NotificationType.values.firstWhere(
        (type) => type.name == rawType || type.toString().split('.').last == rawType,
      );
    } catch (_) {
      return NotificationType.system;
    }
  }


  Future<void> _syncMarkRead(String notificationId) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      await SupabaseService.client
          .from('notifications')
          .update({'is_read': true})
          .eq('id', notificationId);
    } catch (e) {
      debugPrint('[Notifications] Mark-read sync error: $e');
    }
  }

  Future<void> _syncMarkAllRead() async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      final uid = SupabaseService.currentUser?.id;
      if (uid == null) return;
      await SupabaseService.client
          .from('notifications')
          .update({'is_read': true})
          .eq('user_id', uid)
          .eq('is_read', false);
    } catch (e) {
      debugPrint('[Notifications] Mark-all-read sync error: $e');
    }
  }

  Future<void> _syncDeleteNotification(String notificationId) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      await SupabaseService.client
          .from('notifications')
          .delete()
          .eq('id', notificationId);
    } catch (e) {
      debugPrint('[Notifications] Delete sync error: $e');
    }
  }
}

import 'package:flutter/foundation.dart';

/// Notification Channels service for NeuroComet.
/// Mirrors the Kotlin NotificationChannels.kt
///
/// Manages notification channel configuration for organized notification settings.
/// Channels are organized by category for better user control.
///
/// Features:
/// - Neurodivergent-friendly defaults (less intrusive)
/// - Grouped channels for organized settings
/// - Customizable importance levels
/// - Support for accessibility needs
class NotificationChannelsService {
  static final NotificationChannelsService _instance =
      NotificationChannelsService._internal();
  factory NotificationChannelsService() => _instance;
  NotificationChannelsService._internal();

  // ============================================================================
  // CHANNEL IDs
  // ============================================================================

  // Messages
  static const channelDirectMessages = 'direct_messages';
  static const channelGroupMessages = 'group_messages';
  static const channelMessageRequests = 'message_requests';

  // Social
  static const channelLikes = 'likes';
  static const channelComments = 'comments';
  static const channelMentions = 'mentions';
  static const channelFollows = 'follows';
  static const channelFriendActivity = 'friend_activity';

  // Community
  static const channelCommunityUpdates = 'community_updates';
  static const channelEventReminders = 'event_reminders';
  static const channelLiveEvents = 'live_events';

  // Account & Security
  static const channelAccountSecurity = 'account_security';
  static const channelParentalAlerts = 'parental_alerts';
  static const channelLoginAlerts = 'login_alerts';

  // App Updates
  static const channelAppUpdates = 'app_updates';
  static const channelFeatureAnnouncements = 'feature_announcements';
  static const channelTipsAndTricks = 'tips_and_tricks';

  // Wellness
  static const channelWellnessReminders = 'wellness_reminders';
  static const channelBreakReminders = 'break_reminders';
  static const channelCalmMode = 'calm_mode';

  // ============================================================================
  // CHANNEL GROUPS
  // ============================================================================

  static const groupMessages = 'group_messages_category';
  static const groupSocial = 'group_social';
  static const groupCommunity = 'group_community';
  static const groupAccount = 'group_account';
  static const groupApp = 'group_app';
  static const groupWellness = 'group_wellness';

  // ============================================================================
  // CHANNEL DEFINITIONS
  // ============================================================================

  /// Get all channel definitions, grouped
  Map<String, List<NotificationChannelInfo>> getAllChannels() {
    return {
      'Messages': [
        NotificationChannelInfo(
          id: channelDirectMessages,
          name: 'Direct Messages',
          description: 'New direct messages from other users',
          importance: ChannelImportance.high,
          group: groupMessages,
        ),
        NotificationChannelInfo(
          id: channelGroupMessages,
          name: 'Group Messages',
          description: 'Messages in group conversations',
          importance: ChannelImportance.medium,
          group: groupMessages,
        ),
        NotificationChannelInfo(
          id: channelMessageRequests,
          name: 'Message Requests',
          description: 'New message requests from people you don\'t follow',
          importance: ChannelImportance.low,
          group: groupMessages,
        ),
      ],
      'Social': [
        NotificationChannelInfo(
          id: channelLikes,
          name: 'Likes',
          description: 'When someone likes your post',
          importance: ChannelImportance.low,
          group: groupSocial,
        ),
        NotificationChannelInfo(
          id: channelComments,
          name: 'Comments',
          description: 'New comments on your posts',
          importance: ChannelImportance.medium,
          group: groupSocial,
        ),
        NotificationChannelInfo(
          id: channelMentions,
          name: 'Mentions',
          description: 'When someone mentions you',
          importance: ChannelImportance.high,
          group: groupSocial,
        ),
        NotificationChannelInfo(
          id: channelFollows,
          name: 'New Followers',
          description: 'When someone follows you',
          importance: ChannelImportance.low,
          group: groupSocial,
        ),
        NotificationChannelInfo(
          id: channelFriendActivity,
          name: 'Friend Activity',
          description: 'Activity from people you follow',
          importance: ChannelImportance.low,
          group: groupSocial,
        ),
      ],
      'Community': [
        NotificationChannelInfo(
          id: channelCommunityUpdates,
          name: 'Community Updates',
          description: 'News and updates from communities you\'re in',
          importance: ChannelImportance.medium,
          group: groupCommunity,
        ),
        NotificationChannelInfo(
          id: channelEventReminders,
          name: 'Event Reminders',
          description: 'Reminders for upcoming events',
          importance: ChannelImportance.high,
          group: groupCommunity,
        ),
        NotificationChannelInfo(
          id: channelLiveEvents,
          name: 'Live Events',
          description: 'When live events start',
          importance: ChannelImportance.high,
          group: groupCommunity,
        ),
      ],
      'Account & Security': [
        NotificationChannelInfo(
          id: channelAccountSecurity,
          name: 'Account Security',
          description: 'Important security alerts for your account',
          importance: ChannelImportance.critical,
          group: groupAccount,
        ),
        NotificationChannelInfo(
          id: channelParentalAlerts,
          name: 'Parental Alerts',
          description: 'Alerts for parental controls',
          importance: ChannelImportance.high,
          group: groupAccount,
        ),
        NotificationChannelInfo(
          id: channelLoginAlerts,
          name: 'Login Alerts',
          description: 'Alerts for new login attempts',
          importance: ChannelImportance.high,
          group: groupAccount,
        ),
      ],
      'App': [
        NotificationChannelInfo(
          id: channelAppUpdates,
          name: 'App Updates',
          description: 'Important app updates and patches',
          importance: ChannelImportance.low,
          group: groupApp,
        ),
        NotificationChannelInfo(
          id: channelFeatureAnnouncements,
          name: 'Feature Announcements',
          description: 'New feature releases',
          importance: ChannelImportance.low,
          group: groupApp,
        ),
        NotificationChannelInfo(
          id: channelTipsAndTricks,
          name: 'Tips & Tricks',
          description: 'Helpful tips for using NeuroComet',
          importance: ChannelImportance.low,
          group: groupApp,
        ),
      ],
      'Wellness': [
        NotificationChannelInfo(
          id: channelWellnessReminders,
          name: 'Wellness Reminders',
          description: 'Reminders to take care of yourself',
          importance: ChannelImportance.medium,
          group: groupWellness,
        ),
        NotificationChannelInfo(
          id: channelBreakReminders,
          name: 'Break Reminders',
          description: 'Reminders to take a break from the screen',
          importance: ChannelImportance.medium,
          group: groupWellness,
        ),
        NotificationChannelInfo(
          id: channelCalmMode,
          name: 'Calm Mode',
          description: 'Gentle notifications during calm mode',
          importance: ChannelImportance.low,
          group: groupWellness,
        ),
      ],
    };
  }

  /// Get channel info by ID
  NotificationChannelInfo? getChannel(String channelId) {
    for (final group in getAllChannels().values) {
      for (final channel in group) {
        if (channel.id == channelId) return channel;
      }
    }
    return null;
  }

  /// Initialize notification channels.
  /// On Android, this would create actual notification channels.
  /// On Flutter, this serves as a registry for notification routing.
  void initialize() {
    final channels = getAllChannels();
    int count = 0;
    for (final group in channels.values) {
      count += group.length;
    }
    debugPrint('NotificationChannels: Registered $count channels');
  }
}

/// Importance levels for notification channels
enum ChannelImportance {
  low,
  medium,
  high,
  critical,
}

/// Information about a notification channel
class NotificationChannelInfo {
  final String id;
  final String name;
  final String description;
  final ChannelImportance importance;
  final String group;

  const NotificationChannelInfo({
    required this.id,
    required this.name,
    required this.description,
    required this.importance,
    required this.group,
  });
}


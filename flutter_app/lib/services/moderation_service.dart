import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'supabase_service.dart';

/// Content moderation service for filtering and reporting
class ModerationService {
  static final ModerationService _instance = ModerationService._internal();
  factory ModerationService() => _instance;
  ModerationService._internal();

  // Blocked words list (basic example)
  final Set<String> _blockedWords = {
    // Add actual blocked words in production
  };

  // Content warning triggers
  final Map<String, List<String>> _triggerWords = {
    'self_harm': ['self harm', 'cutting', 'suicide'],
    'violence': ['violence', 'attack', 'hurt'],
    'sensitive': ['trigger warning', 'tw', 'cw'],
  };

  /// Check if content contains blocked words
  bool containsBlockedContent(String content) {
    final lowerContent = content.toLowerCase();
    for (final word in _blockedWords) {
      if (lowerContent.contains(word.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /// Get content warnings for text
  List<String> getContentWarnings(String content) {
    final warnings = <String>[];
    final lowerContent = content.toLowerCase();

    for (final entry in _triggerWords.entries) {
      for (final trigger in entry.value) {
        if (lowerContent.contains(trigger)) {
          warnings.add(entry.key);
          break;
        }
      }
    }

    return warnings;
  }

  /// Filter content for display
  String filterContent(String content, {bool censorProfanity = true}) {
    if (!censorProfanity) return content;

    String filtered = content;
    for (final word in _blockedWords) {
      final pattern = RegExp(word, caseSensitive: false);
      filtered = filtered.replaceAll(pattern, '*' * word.length);
    }
    return filtered;
  }

  /// Report content to moderation queue
  Future<bool> reportContent({
    required String contentId,
    required String contentType,
    required String reason,
    String? details,
    required String reporterId,
  }) async {
    try {
      final result = await SupabaseService.reportContent(
        contentType: contentType,
        contentId: contentId,
        reason: reason,
        additionalInfo: details,
      );
      debugPrint('Report submitted: $contentType $contentId - $reason');
      return result['success'] == true;
    } catch (e) {
      debugPrint('Failed to submit report: $e');
      return false;
    }
  }

  /// Block a user
  Future<bool> blockUser({
    required String userId,
    required String blockedUserId,
  }) async {
    try {
      final result = await SupabaseService.blockUser(blockedUserId);
      debugPrint('Blocked user: $blockedUserId');
      return result['success'] == true;
    } catch (e) {
      debugPrint('Failed to block user: $e');
      return false;
    }
  }

  /// Unblock a user
  Future<bool> unblockUser({
    required String userId,
    required String blockedUserId,
  }) async {
    try {
      final result = await SupabaseService.unblockUser(blockedUserId);
      debugPrint('Unblocked user: $blockedUserId');
      return result['success'] == true;
    } catch (e) {
      debugPrint('Failed to unblock user: $e');
      return false;
    }
  }

  /// Mute a user
  Future<bool> muteUser({
    required String userId,
    required String mutedUserId,
    bool mutePosts = true,
    bool muteStories = true,
    Duration? duration,
  }) async {
    try {
      // Persist muted users locally via SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      final mutedList = prefs.getStringList('muted_users') ?? [];
      if (!mutedList.contains(mutedUserId)) {
        mutedList.add(mutedUserId);
        await prefs.setStringList('muted_users', mutedList);
      }
      debugPrint('Muted user: $mutedUserId (posts: $mutePosts, stories: $muteStories)');
      return true;
    } catch (e) {
      debugPrint('Failed to mute user: $e');
      return false;
    }
  }

  /// Get list of blocked users
  Future<List<String>> getBlockedUsers(String userId) async {
    try {
      return await SupabaseService.getBlockedUsers();
    } catch (e) {
      debugPrint('Failed to get blocked users: $e');
      return [];
    }
  }

  /// Check if user is blocked
  Future<bool> isUserBlocked(String userId, String targetUserId) async {
    final blocked = await getBlockedUsers(userId);
    return blocked.contains(targetUserId);
  }
}

/// Badge system service
class BadgeService {
  static final BadgeService _instance = BadgeService._internal();
  factory BadgeService() => _instance;
  BadgeService._internal();

  // Badge definitions
  final Map<String, Badge> _badges = {
    'early_adopter': Badge(
      id: 'early_adopter',
      name: 'Early Adopter',
      description: 'Joined during the early access period',
      icon: '🌟',
      color: 0xFFFFD700,
    ),
    'first_post': Badge(
      id: 'first_post',
      name: 'First Post',
      description: 'Created your first post',
      icon: '📝',
      color: 0xFF4CAF50,
    ),
    'community_helper': Badge(
      id: 'community_helper',
      name: 'Community Helper',
      description: 'Helped 10 people in the community',
      icon: '🤝',
      color: 0xFF2196F3,
    ),
    'verified': Badge(
      id: 'verified',
      name: 'Verified',
      description: 'Verified community member',
      icon: '✓',
      color: 0xFF9C27B0,
    ),
    'premium': Badge(
      id: 'premium',
      name: 'Premium',
      description: 'Premium subscriber',
      icon: '💎',
      color: 0xFFE91E63,
    ),
    'game_master': Badge(
      id: 'game_master',
      name: 'Game Master',
      description: 'Completed all sensory games',
      icon: '🎮',
      color: 0xFFFF9800,
    ),
    'streak_7': Badge(
      id: 'streak_7',
      name: 'Week Warrior',
      description: '7-day activity streak',
      icon: '🔥',
      color: 0xFFFF5722,
    ),
    'streak_30': Badge(
      id: 'streak_30',
      name: 'Monthly Master',
      description: '30-day activity streak',
      icon: '🏆',
      color: 0xFFFFEB3B,
    ),
  };

  /// Get all available badges
  List<Badge> getAllBadges() => _badges.values.toList();

  /// Get badge by ID
  Badge? getBadge(String badgeId) => _badges[badgeId];

  /// Award badge to user
  Future<bool> awardBadge(String userId, String badgeId) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final earnedBadges = prefs.getStringList('earned_badges_$userId') ?? [];
      if (!earnedBadges.contains(badgeId)) {
        earnedBadges.add(badgeId);
        await prefs.setStringList('earned_badges_$userId', earnedBadges);
      }
      debugPrint('Awarded badge $badgeId to user $userId');
      return true;
    } catch (e) {
      debugPrint('Failed to award badge: $e');
      return false;
    }
  }

  /// Get user's badges
  Future<List<Badge>> getUserBadges(String userId) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final earnedBadgeIds = prefs.getStringList('earned_badges_$userId') ?? ['early_adopter'];
      return earnedBadgeIds
          .map((id) => _badges[id])
          .whereType<Badge>()
          .toList();
    } catch (e) {
      debugPrint('Failed to get user badges: $e');
      return [];
    }
  }

  /// Check and award achievement badges
  Future<List<Badge>> checkAchievements(String userId, UserStats stats) async {
    final newBadges = <Badge>[];

    // Check first post
    if (stats.postCount >= 1) {
      final badge = _badges['first_post'];
      if (badge != null && !stats.earnedBadges.contains(badge.id)) {
        await awardBadge(userId, badge.id);
        newBadges.add(badge);
      }
    }

    // Check streaks
    if (stats.currentStreak >= 7) {
      final badge = _badges['streak_7'];
      if (badge != null && !stats.earnedBadges.contains(badge.id)) {
        await awardBadge(userId, badge.id);
        newBadges.add(badge);
      }
    }

    if (stats.currentStreak >= 30) {
      final badge = _badges['streak_30'];
      if (badge != null && !stats.earnedBadges.contains(badge.id)) {
        await awardBadge(userId, badge.id);
        newBadges.add(badge);
      }
    }

    return newBadges;
  }
}

class Badge {
  final String id;
  final String name;
  final String description;
  final String icon;
  final int color;

  const Badge({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.color,
  });
}

class UserStats {
  final int postCount;
  final int commentCount;
  final int likeCount;
  final int currentStreak;
  final List<String> earnedBadges;

  const UserStats({
    this.postCount = 0,
    this.commentCount = 0,
    this.likeCount = 0,
    this.currentStreak = 0,
    this.earnedBadges = const [],
  });
}

// AnalyticsService is defined in app_services.dart

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

/// Badge categories - organized by type of achievement
enum BadgeCategory {
  social('Social', Icons.people, Color(0xFF4ECDC4)),
  creative('Creative', Icons.brush, Color(0xFFE91E63)),
  wellness('Wellness', Icons.favorite, Color(0xFF4CAF50)),
  explorer('Explorer', Icons.explore, Color(0xFF2196F3)),
  kindness('Kindness', Icons.volunteer_activism, Color(0xFFFF9800)),
  milestone('Milestone', Icons.flag, Color(0xFF9C27B0));

  final String label;
  final IconData icon;
  final Color color;

  const BadgeCategory(this.label, this.icon, this.color);
}

/// Badge rarity levels - affects visual styling
enum BadgeRarity {
  common('Common', Color(0xFF9E9E9E), 1),
  uncommon('Uncommon', Color(0xFF4CAF50), 2),
  rare('Rare', Color(0xFF2196F3), 3),
  epic('Epic', Color(0xFF9C27B0), 4),
  legendary('Legendary', Color(0xFFFFD700), 5);

  final String label;
  final Color color;
  final int tier;

  const BadgeRarity(this.label, this.color, this.tier);
}

/// Represents an achievement badge
class AchievementBadge {
  final String id;
  final String name;
  final String description;
  final String icon; // Emoji
  final BadgeCategory category;
  final BadgeRarity rarity;
  final String requirement;
  final bool isSecret;
  final int maxProgress;
  final int xpReward;

  const AchievementBadge({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.category,
    required this.rarity,
    required this.requirement,
    this.isSecret = false,
    this.maxProgress = 1,
    this.xpReward = 10,
  });
}

/// User's progress on a badge
class BadgeProgress {
  final String badgeId;
  final int currentProgress;
  final bool isUnlocked;
  final DateTime? unlockedAt;

  const BadgeProgress({
    required this.badgeId,
    this.currentProgress = 0,
    this.isUnlocked = false,
    this.unlockedAt,
  });

  Map<String, dynamic> toJson() => {
    'badgeId': badgeId,
    'currentProgress': currentProgress,
    'isUnlocked': isUnlocked,
    'unlockedAt': unlockedAt?.toIso8601String(),
  };

  factory BadgeProgress.fromJson(Map<String, dynamic> json) => BadgeProgress(
    badgeId: json['badgeId'] as String,
    currentProgress: json['currentProgress'] as int? ?? 0,
    isUnlocked: json['isUnlocked'] as bool? ?? false,
    unlockedAt: json['unlockedAt'] != null
        ? DateTime.parse(json['unlockedAt'] as String)
        : null,
  );

  BadgeProgress copyWith({
    int? currentProgress,
    bool? isUnlocked,
    DateTime? unlockedAt,
  }) => BadgeProgress(
    badgeId: badgeId,
    currentProgress: currentProgress ?? this.currentProgress,
    isUnlocked: isUnlocked ?? this.isUnlocked,
    unlockedAt: unlockedAt ?? this.unlockedAt,
  );
}

/// All available badges in the app
class BadgeRegistry {
  static const List<AchievementBadge> allBadges = [
    // === SOCIAL BADGES ===
    AchievementBadge(
      id: 'first_friend',
      name: 'First Connection',
      description: 'Made your first friend on NeuroComet',
      icon: '🤝',
      category: BadgeCategory.social,
      rarity: BadgeRarity.common,
      requirement: 'Add your first friend',
    ),
    AchievementBadge(
      id: 'conversation_starter',
      name: 'Conversation Starter',
      description: 'Started 5 conversations',
      icon: '💬',
      category: BadgeCategory.social,
      rarity: BadgeRarity.common,
      requirement: 'Start 5 conversations',
      maxProgress: 5,
    ),
    AchievementBadge(
      id: 'good_listener',
      name: 'Good Listener',
      description: 'Read 50 messages from friends',
      icon: '👂',
      category: BadgeCategory.social,
      rarity: BadgeRarity.uncommon,
      requirement: 'Read 50 messages',
      maxProgress: 50,
    ),
    AchievementBadge(
      id: 'social_butterfly',
      name: 'Social Butterfly',
      description: 'Connected with 10 people',
      icon: '🦋',
      category: BadgeCategory.social,
      rarity: BadgeRarity.rare,
      requirement: 'Have 10 friends',
      maxProgress: 10,
    ),

    // === CREATIVE BADGES ===
    AchievementBadge(
      id: 'first_post',
      name: 'Voice Heard',
      description: 'Shared your first post',
      icon: '📝',
      category: BadgeCategory.creative,
      rarity: BadgeRarity.common,
      requirement: 'Create your first post',
    ),
    AchievementBadge(
      id: 'storyteller',
      name: 'Storyteller',
      description: 'Shared 10 posts',
      icon: '📖',
      category: BadgeCategory.creative,
      rarity: BadgeRarity.uncommon,
      requirement: 'Share 10 posts',
      maxProgress: 10,
    ),
    AchievementBadge(
      id: 'artist',
      name: 'Artist',
      description: 'Shared 5 posts with images',
      icon: '🎨',
      category: BadgeCategory.creative,
      rarity: BadgeRarity.uncommon,
      requirement: 'Share 5 image posts',
      maxProgress: 5,
    ),
    AchievementBadge(
      id: 'viral_star',
      name: 'Viral Star',
      description: 'Got 100 likes on a single post',
      icon: '⭐',
      category: BadgeCategory.creative,
      rarity: BadgeRarity.epic,
      requirement: 'Get 100 likes on one post',
    ),

    // === WELLNESS BADGES ===
    AchievementBadge(
      id: 'zen_master',
      name: 'Zen Master',
      description: 'Completed 10 breathing exercises',
      icon: '🧘',
      category: BadgeCategory.wellness,
      rarity: BadgeRarity.uncommon,
      requirement: 'Complete 10 breathing sessions',
      maxProgress: 10,
    ),
    AchievementBadge(
      id: 'mood_tracker',
      name: 'Mood Tracker',
      description: 'Logged your mood for 7 days',
      icon: '📊',
      category: BadgeCategory.wellness,
      rarity: BadgeRarity.rare,
      requirement: 'Track mood for 7 days',
      maxProgress: 7,
    ),
    AchievementBadge(
      id: 'game_lover',
      name: 'Game Lover',
      description: 'Played 5 different stim games',
      icon: '🎮',
      category: BadgeCategory.wellness,
      rarity: BadgeRarity.uncommon,
      requirement: 'Try 5 different games',
      maxProgress: 5,
    ),
    AchievementBadge(
      id: 'bubble_popper',
      name: 'Bubble Popper',
      description: 'Popped 1000 bubbles',
      icon: '🫧',
      category: BadgeCategory.wellness,
      rarity: BadgeRarity.rare,
      requirement: 'Pop 1000 bubbles total',
      maxProgress: 1000,
    ),

    // === EXPLORER BADGES ===
    AchievementBadge(
      id: 'curious_cat',
      name: 'Curious Cat',
      description: 'Explored all app sections',
      icon: '🐱',
      category: BadgeCategory.explorer,
      rarity: BadgeRarity.common,
      requirement: 'Visit all main sections',
    ),
    AchievementBadge(
      id: 'theme_designer',
      name: 'Theme Designer',
      description: 'Tried 5 different themes',
      icon: '🎭',
      category: BadgeCategory.explorer,
      rarity: BadgeRarity.uncommon,
      requirement: 'Switch themes 5 times',
      maxProgress: 5,
    ),
    AchievementBadge(
      id: 'night_owl',
      name: 'Night Owl',
      description: 'Used the app after midnight',
      icon: '🦉',
      category: BadgeCategory.explorer,
      rarity: BadgeRarity.common,
      requirement: 'Be active after midnight',
      isSecret: true,
    ),

    // === KINDNESS BADGES ===
    AchievementBadge(
      id: 'supportive_friend',
      name: 'Supportive Friend',
      description: 'Sent 10 supportive messages',
      icon: '💜',
      category: BadgeCategory.kindness,
      rarity: BadgeRarity.uncommon,
      requirement: 'Send 10 supportive messages',
      maxProgress: 10,
    ),
    AchievementBadge(
      id: 'cheerleader',
      name: 'Cheerleader',
      description: 'Liked 50 posts',
      icon: '📣',
      category: BadgeCategory.kindness,
      rarity: BadgeRarity.common,
      requirement: 'Like 50 posts',
      maxProgress: 50,
    ),
    AchievementBadge(
      id: 'community_hero',
      name: 'Community Hero',
      description: 'Reported harmful content',
      icon: '🦸',
      category: BadgeCategory.kindness,
      rarity: BadgeRarity.rare,
      requirement: 'Help keep the community safe',
    ),

    // === MILESTONE BADGES ===
    AchievementBadge(
      id: 'week_one',
      name: 'One Week Strong',
      description: 'Used NeuroComet for 7 days',
      icon: '📅',
      category: BadgeCategory.milestone,
      rarity: BadgeRarity.common,
      requirement: 'Be active for 7 days',
      maxProgress: 7,
    ),
    AchievementBadge(
      id: 'month_one',
      name: 'Monthly Member',
      description: 'Used NeuroComet for 30 days',
      icon: '🌙',
      category: BadgeCategory.milestone,
      rarity: BadgeRarity.rare,
      requirement: 'Be active for 30 days',
      maxProgress: 30,
    ),
    AchievementBadge(
      id: 'founding_member',
      name: 'Founding Member',
      description: 'Joined during beta',
      icon: '🚀',
      category: BadgeCategory.milestone,
      rarity: BadgeRarity.legendary,
      requirement: 'Be an early adopter',
    ),
    AchievementBadge(
      id: 'rainbow_brain',
      name: 'Rainbow Brain',
      description: 'Unlocked the secret theme',
      icon: '🦄',
      category: BadgeCategory.milestone,
      rarity: BadgeRarity.legendary,
      requirement: '???',
      isSecret: true,
    ),
  ];

  static AchievementBadge? getBadgeById(String id) {
    try {
      return allBadges.firstWhere((b) => b.id == id);
    } catch (_) {
      return null;
    }
  }

  static List<AchievementBadge> getBadgesByCategory(BadgeCategory category) {
    return allBadges.where((b) => b.category == category).toList();
  }
}

/// Badge manager for tracking and unlocking badges
class BadgeManager {
  static const String _prefsKey = 'badge_progress';
  static const String _xpKey = 'total_xp';

  static Future<Map<String, BadgeProgress>> getAllProgress() async {
    final prefs = await SharedPreferences.getInstance();
    final json = prefs.getString(_prefsKey);
    if (json == null) return {};

    final Map<String, dynamic> data = jsonDecode(json);
    return data.map((key, value) =>
        MapEntry(key, BadgeProgress.fromJson(value as Map<String, dynamic>)));
  }

  static Future<BadgeProgress> getProgress(String badgeId) async {
    final all = await getAllProgress();
    return all[badgeId] ?? BadgeProgress(badgeId: badgeId);
  }

  static Future<void> updateProgress(String badgeId, int progress) async {
    final prefs = await SharedPreferences.getInstance();
    final all = await getAllProgress();
    final badge = BadgeRegistry.getBadgeById(badgeId);

    if (badge == null) return;

    final current = all[badgeId] ?? BadgeProgress(badgeId: badgeId);
    final newProgress = current.currentProgress + progress;
    final shouldUnlock = newProgress >= badge.maxProgress && !current.isUnlocked;

    all[badgeId] = current.copyWith(
      currentProgress: newProgress.clamp(0, badge.maxProgress),
      isUnlocked: shouldUnlock || current.isUnlocked,
      unlockedAt: shouldUnlock ? DateTime.now() : current.unlockedAt,
    );

    await prefs.setString(_prefsKey, jsonEncode(
      all.map((key, value) => MapEntry(key, value.toJson())),
    ));

    // Award XP if newly unlocked
    if (shouldUnlock) {
      await _addXp(badge.xpReward);
    }
  }

  static Future<void> unlockBadge(String badgeId) async {
    final badge = BadgeRegistry.getBadgeById(badgeId);
    if (badge == null) return;

    await updateProgress(badgeId, badge.maxProgress);
  }

  static Future<int> getTotalXp() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt(_xpKey) ?? 0;
  }

  static Future<void> _addXp(int amount) async {
    final prefs = await SharedPreferences.getInstance();
    final current = prefs.getInt(_xpKey) ?? 0;
    await prefs.setInt(_xpKey, current + amount);
  }

  static Future<List<AchievementBadge>> getUnlockedBadges() async {
    final progress = await getAllProgress();
    return BadgeRegistry.allBadges.where((badge) {
      final p = progress[badge.id];
      return p?.isUnlocked ?? false;
    }).toList();
  }

  static Future<int> getUnlockedCount() async {
    final unlocked = await getUnlockedBadges();
    return unlocked.length;
  }
}

/// Badge display widget
class BadgeChip extends StatelessWidget {
  final AchievementBadge badge;
  final bool isUnlocked;
  final VoidCallback? onTap;

  const BadgeChip({
    super.key,
    required this.badge,
    this.isUnlocked = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: AnimatedOpacity(
        duration: const Duration(milliseconds: 200),
        opacity: isUnlocked ? 1.0 : 0.5,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: isUnlocked
                ? badge.rarity.color.withOpacity(0.15)
                : theme.colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: isUnlocked
                  ? badge.rarity.color.withOpacity(0.4)
                  : theme.colorScheme.outline.withOpacity(0.2),
              width: isUnlocked ? 2 : 1,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                isUnlocked ? badge.icon : '🔒',
                style: const TextStyle(fontSize: 20),
              ),
              const SizedBox(width: 8),
              Text(
                isUnlocked || !badge.isSecret ? badge.name : '???',
                style: theme.textTheme.labelMedium?.copyWith(
                  fontWeight: isUnlocked ? FontWeight.bold : FontWeight.normal,
                  color: isUnlocked
                      ? badge.rarity.color
                      : theme.colorScheme.outline,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Badge detail dialog
class BadgeDetailDialog extends StatelessWidget {
  final AchievementBadge badge;
  final BadgeProgress progress;

  const BadgeDetailDialog({
    super.key,
    required this.badge,
    required this.progress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final progressPercent = badge.maxProgress > 1
        ? progress.currentProgress / badge.maxProgress
        : (progress.isUnlocked ? 1.0 : 0.0);

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Badge icon
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: progress.isUnlocked
                    ? badge.rarity.color.withOpacity(0.2)
                    : theme.colorScheme.surfaceContainerHighest,
                border: Border.all(
                  color: progress.isUnlocked
                      ? badge.rarity.color
                      : theme.colorScheme.outline,
                  width: 3,
                ),
              ),
              child: Center(
                child: Text(
                  progress.isUnlocked || !badge.isSecret ? badge.icon : '🔒',
                  style: const TextStyle(fontSize: 40),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Rarity tag
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: badge.rarity.color.withOpacity(0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                badge.rarity.label,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: badge.rarity.color,
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Name
            Text(
              progress.isUnlocked || !badge.isSecret ? badge.name : '???',
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),

            // Description
            Text(
              progress.isUnlocked || !badge.isSecret
                  ? badge.description
                  : 'This badge is a secret!',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),

            // Progress bar (if applicable)
            if (badge.maxProgress > 1) ...[
              Row(
                children: [
                  Expanded(
                    child: LinearProgressIndicator(
                      value: progressPercent,
                      backgroundColor: theme.colorScheme.surfaceContainerHighest,
                      color: badge.rarity.color,
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    '${progress.currentProgress}/${badge.maxProgress}',
                    style: theme.textTheme.labelMedium,
                  ),
                ],
              ),
              const SizedBox(height: 12),
            ],

            // Requirement
            Text(
              progress.isUnlocked
                  ? '✅ Completed!'
                  : '📋 ${badge.requirement}',
              style: theme.textTheme.bodySmall?.copyWith(
                color: progress.isUnlocked
                    ? Colors.green
                    : theme.colorScheme.outline,
              ),
            ),

            // XP reward
            if (progress.isUnlocked)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  '+${badge.xpReward} XP earned!',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: badge.rarity.color,
                  ),
                ),
              ),

            const SizedBox(height: 24),
            FilledButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
          ],
        ),
      ),
    );
  }
}


import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../services/badge_service.dart';

/// Provider for badge progress
final badgeProgressProvider = FutureProvider<Map<String, BadgeProgress>>((ref) async {
  return BadgeManager.getAllProgress();
});

/// Provider for total XP
final totalXpProvider = FutureProvider<int>((ref) async {
  return BadgeManager.getTotalXp();
});

/// Screen to display all badges and achievements
class BadgesScreen extends ConsumerWidget {
  const BadgesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final progressAsync = ref.watch(badgeProgressProvider);
    final xpAsync = ref.watch(totalXpProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Achievements'),
        actions: [
          // XP display
          xpAsync.when(
            data: (xp) => Center(
              child: Padding(
                padding: const EdgeInsets.only(right: 16),
                child: Row(
                  children: [
                    const Text('⭐', style: TextStyle(fontSize: 18)),
                    const SizedBox(width: 4),
                    Text(
                      '$xp XP',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),
        ],
      ),
      body: progressAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(child: Text('Error: $error')),
        data: (progress) => _buildBadgesList(context, progress),
      ),
    );
  }

  Widget _buildBadgesList(BuildContext context, Map<String, BadgeProgress> progress) {
    final theme = Theme.of(context);

    // Group badges by category
    final categories = BadgeCategory.values;

    // Calculate stats
    final totalBadges = BadgeRegistry.allBadges.length;
    final unlockedBadges = progress.values.where((p) => p.isUnlocked).length;

    return CustomScrollView(
      slivers: [
        // Stats header
        SliverToBoxAdapter(
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  theme.colorScheme.primaryContainer,
                  theme.colorScheme.secondaryContainer,
                ],
              ),
            ),
            child: Column(
              children: [
                Text(
                  '🏆',
                  style: const TextStyle(fontSize: 48),
                ),
                const SizedBox(height: 12),
                Text(
                  '$unlockedBadges / $totalBadges',
                  style: theme.textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Badges Unlocked',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: 16),
                // Progress bar
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: LinearProgressIndicator(
                    value: totalBadges > 0 ? unlockedBadges / totalBadges : 0,
                    minHeight: 8,
                    backgroundColor: theme.colorScheme.surface.withOpacity(0.5),
                  ),
                ),
              ],
            ),
          ),
        ),

        // Badge categories
        ...categories.map((category) {
          final categoryBadges = BadgeRegistry.getBadgesByCategory(category);
          if (categoryBadges.isEmpty) return const SliverToBoxAdapter(child: SizedBox.shrink());

          return SliverToBoxAdapter(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Category header
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 24, 16, 12),
                  child: Row(
                    children: [
                      Icon(
                        category.icon,
                        color: category.color,
                        size: 24,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        category.label,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const Spacer(),
                      Text(
                        '${categoryBadges.where((b) => progress[b.id]?.isUnlocked ?? false).length}/${categoryBadges.length}',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.outline,
                        ),
                      ),
                    ],
                  ),
                ),

                // Badge grid
                GridView.builder(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    childAspectRatio: 0.8,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                  ),
                  itemCount: categoryBadges.length,
                  itemBuilder: (context, index) {
                    final badge = categoryBadges[index];
                    final badgeProgress = progress[badge.id] ??
                        BadgeProgress(badgeId: badge.id);

                    return _BadgeCard(
                      badge: badge,
                      progress: badgeProgress,
                      onTap: () => _showBadgeDetail(context, badge, badgeProgress),
                    );
                  },
                ),
              ],
            ),
          );
        }),

        // Bottom padding
        const SliverToBoxAdapter(
          child: SizedBox(height: 32),
        ),
      ],
    );
  }

  void _showBadgeDetail(
    BuildContext context,
    AchievementBadge badge,
    BadgeProgress progress,
  ) {
    showDialog(
      context: context,
      builder: (context) => BadgeDetailDialog(
        badge: badge,
        progress: progress,
      ),
    );
  }
}

/// Individual badge card widget
class _BadgeCard extends StatelessWidget {
  final AchievementBadge badge;
  final BadgeProgress progress;
  final VoidCallback onTap;

  const _BadgeCard({
    required this.badge,
    required this.progress,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isUnlocked = progress.isUnlocked;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        decoration: BoxDecoration(
          color: isUnlocked
              ? badge.rarity.color.withOpacity(0.1)
              : theme.colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: isUnlocked
                ? badge.rarity.color.withOpacity(0.3)
                : theme.colorScheme.outline.withOpacity(0.1),
            width: isUnlocked ? 2 : 1,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Badge icon
            AnimatedOpacity(
              duration: const Duration(milliseconds: 200),
              opacity: isUnlocked ? 1.0 : 0.4,
              child: Text(
                isUnlocked || !badge.isSecret ? badge.icon : '🔒',
                style: const TextStyle(fontSize: 36),
              ),
            ),
            const SizedBox(height: 8),

            // Badge name
            Text(
              isUnlocked || !badge.isSecret ? badge.name : '???',
              style: theme.textTheme.labelSmall?.copyWith(
                fontWeight: isUnlocked ? FontWeight.bold : FontWeight.normal,
                color: isUnlocked
                    ? badge.rarity.color
                    : theme.colorScheme.outline,
              ),
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),

            // Progress indicator (if applicable)
            if (badge.maxProgress > 1 && !isUnlocked) ...[
              const SizedBox(height: 6),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: LinearProgressIndicator(
                  value: progress.currentProgress / badge.maxProgress,
                  backgroundColor: theme.colorScheme.surfaceContainerHighest,
                  color: badge.rarity.color.withOpacity(0.5),
                  borderRadius: BorderRadius.circular(2),
                  minHeight: 3,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}


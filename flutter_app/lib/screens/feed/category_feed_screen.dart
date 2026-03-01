import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';

/// Category Feed Screen - Displays posts filtered by a specific category/community.
/// Mirrors the Kotlin CategoryFeedScreen.kt
class CategoryFeedScreen extends ConsumerWidget {
  final String categoryName;

  const CategoryFeedScreen({
    super.key,
    required this.categoryName,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Mock posts for the category
    final posts = _createMockPosts(categoryName);

    return Scaffold(
      appBar: AppBar(
        title: Text(categoryName),
      ),
      body: posts.isEmpty
          ? _EmptyFeedMessage(categoryName: categoryName)
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: posts.length,
              itemBuilder: (context, index) {
                final post = posts[index];
                return _CategoryPostCard(post: post);
              },
            ),
    );
  }

  List<_MockPost> _createMockPosts(String category) {
    if (category == 'Neurobiology') {
      return [
        _MockPost(
          id: '1',
          username: 'NeuroScience101',
          content:
              'Did you know that the neurodivergent brain often has increased connectivity in certain regions? This can lead to unique strengths like pattern recognition and creative thinking! 🧠✨',
          likes: 42,
          timestamp: DateTime.now().subtract(const Duration(hours: 2)),
        ),
        _MockPost(
          id: '2',
          username: 'BrainFacts',
          content:
              'New research shows that ADHD brains have different dopamine receptor densities. Understanding this helps us appreciate why certain tasks feel easier or harder for different people.',
          likes: 38,
          timestamp: DateTime.now().subtract(const Duration(hours: 5)),
        ),
        _MockPost(
          id: '3',
          username: 'MindMatters',
          content:
              'Sensory processing differences in autism are now understood to involve atypical neural gating mechanisms. The brain doesn\'t filter input the same way – that\'s not a deficit, it\'s a difference! 🌈',
          likes: 56,
          timestamp: DateTime.now().subtract(const Duration(hours: 8)),
        ),
        _MockPost(
          id: '4',
          username: 'NeuroDiverse',
          content:
              'Studies on dyslexia reveal fascinating differences in how the brain processes visual information. Many dyslexic individuals have enhanced spatial reasoning abilities.',
          likes: 29,
          timestamp: DateTime.now().subtract(const Duration(hours: 12)),
        ),
        _MockPost(
          id: '5',
          username: 'ScienceOfND',
          content:
              'The concept of "neurodiversity" was coined in 1998 by sociologist Judy Singer. It frames neurological differences as natural human variations rather than disorders. 🧩',
          likes: 67,
          timestamp: DateTime.now().subtract(const Duration(hours: 24)),
        ),
      ];
    }
    return [];
  }
}

class _EmptyFeedMessage extends StatelessWidget {
  final String categoryName;

  const _EmptyFeedMessage({required this.categoryName});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: AppColors.primaryPurple.withAlpha(30),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.article_outlined,
                size: 48,
                color: AppColors.primaryPurple,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'No Posts Yet',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Be the first to post in $categoryName!',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _CategoryPostCard extends StatelessWidget {
  final _MockPost post;

  const _CategoryPostCard({required this.post});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundColor: AppColors.primaryPurple.withAlpha(50),
                  child: Text(
                    post.username[0].toUpperCase(),
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      color: AppColors.primaryPurple,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        post.username,
                        style: theme.textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        _formatTime(post.timestamp),
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            // Content
            Text(
              post.content,
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            // Actions
            Row(
              children: [
                Icon(
                  Icons.thumb_up_outlined,
                  size: 18,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                const SizedBox(width: 6),
                Text(
                  '${post.likes}',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(width: 24),
                Icon(
                  Icons.comment_outlined,
                  size: 18,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                const SizedBox(width: 6),
                Text(
                  'Comment',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const Spacer(),
                Icon(
                  Icons.share_outlined,
                  size: 18,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _formatTime(DateTime time) {
    final diff = DateTime.now().difference(time);
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    return '${diff.inDays}d ago';
  }
}

class _MockPost {
  final String id;
  final String username;
  final String content;
  final int likes;
  final DateTime timestamp;

  const _MockPost({
    required this.id,
    required this.username,
    required this.content,
    required this.likes,
    required this.timestamp,
  });
}


import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/user.dart';
import '../../services/supabase_service.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../l10n/app_localizations.dart';

/// Screen showing followers and following lists
class FollowersScreen extends ConsumerStatefulWidget {
  final String userId;
  final int initialTab;

  const FollowersScreen({
    super.key,
    required this.userId,
    this.initialTab = 0,
  });

  @override
  ConsumerState<FollowersScreen> createState() => _FollowersScreenState();
}

class _FollowersScreenState extends ConsumerState<FollowersScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(
      length: 2,
      vsync: this,
      initialIndex: widget.initialTab,
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Connections'),
        bottom: TabBar(
          controller: _tabController,
          tabs: [
            Tab(text: l10n.followers),
            Tab(text: l10n.following),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _FollowersList(userId: widget.userId, type: 'followers'),
          _FollowersList(userId: widget.userId, type: 'following'),
        ],
      ),
    );
  }
}

class _FollowersList extends ConsumerWidget {
  final String userId;
  final String type;

  const _FollowersList({
    required this.userId,
    required this.type,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final usersState = type == 'followers'
        ? ref.watch(followersProvider(userId))
        : ref.watch(followingProvider(userId));

    return usersState.when(
      loading: () => const NeuroLoading(),
      error: (error, stack) => Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 48,
              color: Theme.of(context).colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text('Error: $error'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                if (type == 'followers') {
                  ref.invalidate(followersProvider(userId));
                } else {
                  ref.invalidate(followingProvider(userId));
                }
              },
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
      data: (users) => users.isEmpty
          ? _buildEmptyState(context, type)
          : ListView.builder(
              padding: const EdgeInsets.symmetric(vertical: 8),
              itemCount: users.length,
              itemBuilder: (context, index) => _UserTile(
                user: users[index],
                onTap: () {
                  context.push('/profile/${users[index].id}');
                },
                onFollow: () {
                  // Toggle follow
                },
              ),
            ),
    );
  }

  Widget _buildEmptyState(BuildContext context, String type) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.people_outline,
              size: 64,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 16),
            Text(
              type == 'followers'
                  ? 'No followers yet'
                  : 'Not following anyone yet',
              style: theme.textTheme.titleMedium?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              type == 'followers'
                  ? 'Share your thoughts and connect with the community!'
                  : 'Find people to follow in the Explore section!',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.outline,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _UserTile extends StatelessWidget {
  final User user;
  final VoidCallback? onTap;
  final VoidCallback? onFollow;

  const _UserTile({
    required this.user,
    this.onTap,
    this.onFollow,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      leading: NeuroAvatar(
        imageUrl: user.avatarUrl,
        name: user.displayName,
        size: 48,
      ),
      title: Row(
        children: [
          Flexible(
            child: Text(
              user.displayName,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w600,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (user.isVerified) ...[
            const SizedBox(width: 4),
            Icon(
              Icons.verified,
              size: 16,
              color: theme.colorScheme.primary,
            ),
          ],
        ],
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (user.username != null)
            Text(
              '@${user.username}',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
          if (user.bio != null && user.bio!.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(
                user.bio!,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.bodySmall,
              ),
            ),
        ],
      ),
      trailing: user.isFollowing
          ? OutlinedButton(
              onPressed: onFollow,
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 16),
              ),
              child: Text(l10n.following),
            )
          : FilledButton(
              onPressed: onFollow,
              style: FilledButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 16),
              ),
              child: Text(l10n.follow),
            ),
    );
  }
}

// Providers
final followersProvider = FutureProvider.family<List<User>, String>((ref, userId) async {
  // Try Supabase first
  try {
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated) {
      final response = await SupabaseService.client
          .from('follows')
          .select('follower:users!follower_id(id, display_name, username, avatar_url, bio, is_verified)')
          .eq('following_id', userId);

      final rows = response as List;
      if (rows.isNotEmpty) {
        return rows.map((row) {
          final u = row['follower'] as Map<String, dynamic>? ?? {};
          return User(
            id: u['id']?.toString() ?? '',
            displayName: u['display_name']?.toString() ?? 'User',
            username: u['username']?.toString(),
            avatarUrl: u['avatar_url']?.toString(),
            bio: u['bio']?.toString(),
            isVerified: u['is_verified'] == true,
          );
        }).toList();
      }
    }
  } catch (e) {
    debugPrint('Followers Supabase fetch failed: $e');
  }

  // Demo fallback
  return List.generate(
    15,
    (index) => User(
      id: 'follower_$index',
      displayName: 'Follower ${index + 1}',
      username: 'follower${index + 1}',
      avatarUrl: 'https://i.pravatar.cc/150?u=follower_${index + 1}',
      bio: index % 2 == 0 ? 'Neurodivergent advocate 🧠' : null,
      isVerified: index % 5 == 0,
      isFollowing: index % 3 == 0,
    ),
  );
});

final followingProvider = FutureProvider.family<List<User>, String>((ref, userId) async {
  // Try Supabase first
  try {
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated) {
      final response = await SupabaseService.client
          .from('follows')
          .select('following:users!following_id(id, display_name, username, avatar_url, bio, is_verified)')
          .eq('follower_id', userId);

      final rows = response as List;
      if (rows.isNotEmpty) {
        return rows.map((row) {
          final u = row['following'] as Map<String, dynamic>? ?? {};
          return User(
            id: u['id']?.toString() ?? '',
            displayName: u['display_name']?.toString() ?? 'User',
            username: u['username']?.toString(),
            avatarUrl: u['avatar_url']?.toString(),
            bio: u['bio']?.toString(),
            isVerified: u['is_verified'] == true,
            isFollowing: true,
          );
        }).toList();
      }
    }
  } catch (e) {
    debugPrint('Following Supabase fetch failed: $e');
  }

  // Demo fallback
  return List.generate(
    10,
    (index) => User(
      id: 'following_$index',
      displayName: 'Following ${index + 1}',
      username: 'following${index + 1}',
      avatarUrl: 'https://i.pravatar.cc/150?u=following_${index + 1}',
      bio: index % 2 == 0 ? 'Community member 💜' : null,
      isVerified: index % 4 == 0,
      isFollowing: true,
    ),
  );
});


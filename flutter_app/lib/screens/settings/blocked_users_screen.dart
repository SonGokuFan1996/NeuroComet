import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/user.dart';
import '../../services/supabase_service.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../services/moderation_service.dart';

/// Screen for managing blocked users
class BlockedUsersScreen extends ConsumerWidget {
  const BlockedUsersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final blockedUsersState = ref.watch(blockedUsersProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Blocked Users'),
      ),
      body: blockedUsersState.when(
        loading: () => const NeuroLoading(),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.error_outline,
                size: 48,
                color: theme.colorScheme.error,
              ),
              const SizedBox(height: 16),
              Text('Error: $error'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.invalidate(blockedUsersProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
        data: (users) => users.isEmpty
            ? _buildEmptyState(context)
            : ListView.builder(
                padding: const EdgeInsets.symmetric(vertical: 8),
                itemCount: users.length,
                itemBuilder: (context, index) => _BlockedUserTile(
                  user: users[index],
                  onUnblock: () async {
                    await _unblockUser(context, ref, users[index]);
                  },
                ),
              ),
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.block,
              size: 64,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 16),
            Text(
              'No Blocked Users',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Users you block will appear here. Blocked users can\'t see your profile or contact you.',
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

  Future<void> _unblockUser(
    BuildContext context,
    WidgetRef ref,
    User user,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Unblock ${user.displayName}?'),
        content: const Text(
          'They will be able to see your profile and send you messages again.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Unblock'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await ModerationService().unblockUser(
          userId: SupabaseService.currentUser?.id ?? 'current_user',
          blockedUserId: user.id,
        );
        ref.invalidate(blockedUsersProvider);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('${user.displayName} has been unblocked'),
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Failed to unblock: $e'),
              behavior: SnackBarBehavior.floating,
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        }
      }
    }
  }
}

class _BlockedUserTile extends StatelessWidget {
  final User user;
  final VoidCallback onUnblock;

  const _BlockedUserTile({
    required this.user,
    required this.onUnblock,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return ListTile(
      leading: NeuroAvatar(
        imageUrl: user.avatarUrl,
        name: user.displayName,
        size: 48,
      ),
      title: Text(
        user.displayName,
        style: theme.textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.w600,
        ),
      ),
      subtitle: user.username != null
          ? Text('@${user.username}')
          : null,
      trailing: OutlinedButton(
        onPressed: onUnblock,
        child: const Text('Unblock'),
      ),
    );
  }
}

// Provider for blocked users
final blockedUsersProvider = FutureProvider<List<User>>((ref) async {
  // Try Supabase first
  try {
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated) {
      final currentUserId = SupabaseService.currentUser?.id;
      if (currentUserId != null) {
        final response = await SupabaseService.client
            .from('blocked_users')
            .select('blocked:users!blocked_id(id, display_name, username, avatar_url)')
            .eq('blocker_id', currentUserId);

        final rows = response as List;
        if (rows.isNotEmpty) {
          return rows.map((row) {
            final u = row['blocked'] as Map<String, dynamic>? ?? {};
            return User(
              id: u['id']?.toString() ?? '',
              displayName: u['display_name']?.toString() ?? 'Blocked User',
              username: u['username']?.toString(),
              avatarUrl: u['avatar_url']?.toString(),
            );
          }).toList();
        }

        // Empty list from Supabase is valid — user hasn't blocked anyone
        return [];
      }
    }
  } catch (e) {
    debugPrint('Blocked users Supabase fetch failed: $e');
  }

  // Not authenticated — no blocked users to show
  return [];
});


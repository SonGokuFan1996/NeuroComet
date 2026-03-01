import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/post.dart';
import '../../models/story.dart';
import '../../providers/feed_provider.dart';
import '../../providers/stories_provider.dart';
import '../../widgets/post/post_card.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../widgets/common/neuro_comet_logo.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../stories/story_viewer_screen.dart';

/// Feed filter options for different content types
enum FeedFilter {
  forYou,
  following,
  trending,
  support,
  wins,
}

class FeedScreen extends ConsumerStatefulWidget {
  const FeedScreen({super.key});

  @override
  ConsumerState<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends ConsumerState<FeedScreen>
    with TickerProviderStateMixin {
  final ScrollController _scrollController = ScrollController();
  bool _isScrolling = false;
  FeedFilter _selectedFilter = FeedFilter.forYou;

  late AnimationController _headerAnimationController;
  late Animation<double> _headerFadeAnimation;
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);

    _headerAnimationController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _headerFadeAnimation = CurvedAnimation(
      parent: _headerAnimationController,
      curve: Curves.easeOutCubic,
    );
    _headerAnimationController.forward();

    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _headerAnimationController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  void _onScroll() {
    final scrolling = _scrollController.position.isScrollingNotifier.value;
    if (scrolling != _isScrolling) {
      setState(() => _isScrolling = scrolling);
    }

    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      ref.read(feedProvider.notifier).loadMore();
    }
  }

  Future<void> _onRefresh() async {
    HapticFeedback.mediumImpact();
    await ref.read(feedProvider.notifier).refresh();
  }

  @override
  Widget build(BuildContext context) {
    final feedState = ref.watch(feedProvider);
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        bottom: false,
        child: feedState.when(
          loading: () => const NeuroLoading(message: 'Loading your feed...'),
          error: (error, stack) => _buildErrorState(error.toString()),
          data: (posts) => _buildFeedContent(posts),
        ),
      ),
    );
  }

  Widget _buildFeedContent(List<Post> posts) {

    return RefreshIndicator(
      onRefresh: _onRefresh,
      color: AppColors.primaryPurple,
      child: CustomScrollView(
        controller: _scrollController,
        physics: const BouncingScrollPhysics(
          parent: AlwaysScrollableScrollPhysics(),
        ),
        slivers: [
          // Premium Header
          SliverToBoxAdapter(
            child: FadeTransition(
              opacity: _headerFadeAnimation,
              child: _FeedHeader(
                onCreatePost: () => _showCreatePost(context),
                onSettings: () => context.push('/settings'),
                isScrolling: _isScrolling,
              ),
            ),
          ),

          // Stories Row with enhanced design (controlled by dev flag)
          if (ref.watch(devOptionsProvider).showStories)
            SliverToBoxAdapter(
              child: _EnhancedStoriesSection(
                onAddStory: () => _showCreateStory(context),
                animate: !_isScrolling,
              ),
            ),

          // Quick Actions Row
          SliverToBoxAdapter(
            child: _QuickActionsRow(
              onCreatePost: () => _showCreatePost(context),
              onCreateStory: () => _showCreateStory(context),
            ),
          ),

          // Filter Pills
          SliverToBoxAdapter(
            child: _FeedFilterChips(
              selectedFilter: _selectedFilter,
              onFilterChanged: (filter) {
                HapticFeedback.selectionClick();
                setState(() => _selectedFilter = filter);
              },
            ),
          ),

          // Content
          if (posts.isEmpty)
            SliverFillRemaining(
              hasScrollBody: false,
              child: _EmptyFeedState(
                filter: _selectedFilter,
                onCreatePost: () => _showCreatePost(context),
                onRefresh: _onRefresh,
              ),
            )
          else
            SliverPadding(
              padding: const EdgeInsets.only(top: 8, bottom: 100),
              sliver: SliverList(
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    if (index == posts.length) {
                      return _LoadingMoreIndicator();
                    }

                    // Add section headers periodically
                    if (index == 0) {
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          _FeedSectionHeader(
                            title: _getSectionTitle(),
                            icon: _getSectionIcon(),
                            count: posts.length,
                          ),
                          _AnimatedPostCard(
                            post: posts[index],
                            animationDelay: index * 50,
                            onLike: () => _handleLike(posts[index]),
                            onComment: () => _showComments(posts[index]),
                            onShare: () => _handleShare(posts[index]),
                            onProfileTap: () => _navigateToProfile(posts[index].authorId),
                            onReport: () => _handleReport(posts[index]),
                            onDelete: () => _handleDelete(posts[index]),
                            onMentionTap: (username) => _navigateToProfile(username),
                            onHashtagTap: (hashtag) => _exploreHashtag(hashtag),
                          ),
                        ],
                      );
                    }

                    return _AnimatedPostCard(
                      post: posts[index],
                      animationDelay: index * 50,
                      onLike: () => _handleLike(posts[index]),
                      onComment: () => _showComments(posts[index]),
                      onShare: () => _handleShare(posts[index]),
                      onProfileTap: () => _navigateToProfile(posts[index].authorId),
                      onReport: () => _handleReport(posts[index]),
                      onDelete: () => _handleDelete(posts[index]),
                      onMentionTap: (username) => _navigateToProfile(username),
                      onHashtagTap: (hashtag) => _exploreHashtag(hashtag),
                    );
                  },
                  childCount: posts.length + 1,
                ),
              ),
            ),
        ],
      ),
    );
  }

  String _getSectionTitle() {
    switch (_selectedFilter) {
      case FeedFilter.forYou:
        return 'For You';
      case FeedFilter.following:
        return 'Following';
      case FeedFilter.trending:
        return 'Trending';
      case FeedFilter.support:
        return 'Support';
      case FeedFilter.wins:
        return 'Wins & Celebrations';
    }
  }

  IconData _getSectionIcon() {
    switch (_selectedFilter) {
      case FeedFilter.forYou:
        return Icons.auto_awesome;
      case FeedFilter.following:
        return Icons.people_alt_rounded;
      case FeedFilter.trending:
        return Icons.trending_up_rounded;
      case FeedFilter.support:
        return Icons.favorite_rounded;
      case FeedFilter.wins:
        return Icons.celebration_rounded;
    }
  }

  Widget _buildErrorState(String error) {
    final l10n = context.l10n;
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 120,
              height: 120,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    theme.colorScheme.errorContainer.withOpacity(0.3),
                    theme.colorScheme.errorContainer.withOpacity(0.1),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                shape: BoxShape.circle,
              ),
              child: Icon(
                Icons.wifi_off_rounded,
                size: 56,
                color: theme.colorScheme.error,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              'Connection Issue',
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            Text(
              'We couldn\'t load your feed.\nCheck your connection and try again.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 32),
            FilledButton.icon(
              onPressed: () {
                HapticFeedback.mediumImpact();
                ref.read(feedProvider.notifier).refresh();
              },
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

  void _handleLike(Post post) {
    HapticFeedback.lightImpact();
    ref.read(feedProvider.notifier).toggleLike(post.id);
  }

  void _showComments(Post post) {
    HapticFeedback.lightImpact();
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => CommentsSheet(postId: post.id),
    );
  }

  void _handleShare(Post post) {
    HapticFeedback.lightImpact();
    // Implement share functionality
  }

  void _handleReport(Post post) {
    HapticFeedback.lightImpact();
    showReportPostDialog(
      context,
      postId: post.id,
      onReport: (reason) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Report submitted. Thank you!'),
            duration: Duration(seconds: 2),
          ),
        );
      },
    );
  }

  void _handleDelete(Post post) {
    HapticFeedback.lightImpact();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Post'),
        content: const Text('Are you sure you want to delete this post? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(context);
              ref.read(feedProvider.notifier).deletePost(post.id);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Post deleted'),
                  duration: Duration(seconds: 1),
                ),
              );
            },
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _navigateToProfile(String userId) {
    HapticFeedback.lightImpact();
    context.push('/profile/$userId');
  }

  void _exploreHashtag(String hashtag) {
    HapticFeedback.lightImpact();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.tag, color: Colors.white, size: 20),
            const SizedBox(width: 8),
            Text('Exploring #$hashtag...'),
          ],
        ),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        backgroundColor: AppColors.primaryPurple,
      ),
    );
  }

  void _showCreatePost(BuildContext context) {
    HapticFeedback.lightImpact();
    context.push('/create-post');
  }

  void _showCreateStory(BuildContext context) {
    HapticFeedback.lightImpact();
    context.push('/create-story');
  }
}

// ============================================================================
// Premium Feed Header
// ============================================================================

class _FeedHeader extends StatelessWidget {
  final VoidCallback onCreatePost;
  final VoidCallback onSettings;
  final bool isScrolling;

  const _FeedHeader({
    required this.onCreatePost,
    required this.onSettings,
    required this.isScrolling,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    return Container(
      padding: const EdgeInsets.fromLTRB(16, 12, 8, 12),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        border: Border(
          bottom: BorderSide(
            color: theme.dividerColor.withOpacity(0.1),
          ),
        ),
      ),
      child: Row(
        children: [
          // Logo
          NeuroCometBrandLogo(
            animated: !isScrolling,
            symbolSize: 40,
          ),
          const Spacer(),
          // Actions
          _HeaderIconButton(
            icon: Icons.add_box_outlined,
            onPressed: onCreatePost,
            tooltip: l10n.createPost,
          ),
          _HeaderIconButton(
            icon: Icons.settings_outlined,
            onPressed: onSettings,
            tooltip: l10n.settingsTitle,
          ),
        ],
      ),
    );
  }
}

class _HeaderIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String tooltip;

  const _HeaderIconButton({
    required this.icon,
    required this.onPressed,
    required this.tooltip,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Tooltip(
      message: tooltip,
      child: Material(
        color: Colors.transparent,
        shape: const CircleBorder(),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: () {
            HapticFeedback.lightImpact();
            onPressed();
          },
          customBorder: const CircleBorder(),
          splashColor: theme.colorScheme.primary.withOpacity(0.12),
          highlightColor: theme.colorScheme.primary.withOpacity(0.08),
          child: Padding(
            padding: const EdgeInsets.all(10),
            child: Icon(
              icon,
              color: theme.colorScheme.onSurfaceVariant,
              size: 24,
            ),
          ),
        ),
      ),
    );
  }
}

// ============================================================================
// Enhanced Stories Section
// ============================================================================

class _EnhancedStoriesSection extends ConsumerWidget {
  final VoidCallback? onAddStory;
  final bool animate;

  const _EnhancedStoriesSection({
    this.onAddStory,
    this.animate = true,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final l10n = context.l10n;
    final storiesState = ref.watch(storiesProvider);

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            theme.scaffoldBackgroundColor,
            theme.colorScheme.primaryContainer.withOpacity(0.05),
          ],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Section label
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                    ),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.auto_stories_rounded,
                    color: Colors.white,
                    size: 16,
                  ),
                ),
                const SizedBox(width: 10),
                Text(
                  'Stories',
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: AppColors.primaryPurple.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    'NEW',
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: AppColors.primaryPurple,
                      fontWeight: FontWeight.bold,
                      fontSize: 10,
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Stories list
          storiesState.when(
            loading: () => SizedBox(
              height: 110,
              child: Center(
                child: SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: AppColors.primaryPurple,
                  ),
                ),
              ),
            ),
            error: (_, __) => SizedBox(
              height: 110,
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.cloud_off, color: theme.colorScheme.outline),
                    const SizedBox(height: 8),
                    Text(
                      'Could not load stories',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.outline,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            data: (storyGroups) => SizedBox(
              height: 115,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                itemCount: storyGroups.length,
                itemBuilder: (context, index) {
                  final group = storyGroups[index];
                  final isCurrentUser = group.userId == 'current_user';
                  final hasStories = group.stories.isNotEmpty;

                  return Padding(
                    padding: const EdgeInsets.only(right: 12),
                    child: _StoryItem(
                      name: isCurrentUser
                          ? (hasStories ? l10n.yourStory : l10n.yourStory)
                          : group.userName,
                      avatarUrl: group.userAvatarUrl,
                      isAddStory: isCurrentUser && !hasStories,
                      hasUnseenStory: group.hasUnseenStories,
                      animate: animate,
                      index: index,
                      onTap: () {
                        HapticFeedback.lightImpact();
                        if (isCurrentUser && !hasStories) {
                          onAddStory?.call();
                        } else if (hasStories || !isCurrentUser) {
                          _openStoryViewer(context, storyGroups, index);
                        }
                      },
                    ),
                  );
                },
              ),
            ),
          ),

          const SizedBox(height: 4),
        ],
      ),
    );
  }

  void _openStoryViewer(BuildContext context, List<StoryGroup> storyGroups, int initialIndex) {
    final groupsWithStories = storyGroups.where((g) => g.stories.isNotEmpty).toList();
    if (groupsWithStories.isEmpty) return;

    final group = storyGroups[initialIndex];
    int viewerIndex = groupsWithStories.indexWhere((g) => g.userId == group.userId);
    if (viewerIndex < 0) viewerIndex = 0;

    Navigator.of(context).push(
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) => StoryViewerScreen(
          storyGroups: groupsWithStories,
          initialGroupIndex: viewerIndex,
        ),
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          return FadeTransition(opacity: animation, child: child);
        },
        transitionDuration: const Duration(milliseconds: 300),
      ),
    );
  }
}

// ============================================================================
// Quick Actions Row
// ============================================================================

class _QuickActionsRow extends StatelessWidget {
  final VoidCallback onCreatePost;
  final VoidCallback onCreateStory;

  const _QuickActionsRow({
    required this.onCreatePost,
    required this.onCreateStory,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          // What's on your mind card
          Expanded(
            child: GestureDetector(
              onTap: () {
                HapticFeedback.lightImpact();
                onCreatePost();
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest.withOpacity(0.5),
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(
                    color: theme.dividerColor.withOpacity(0.1),
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 36,
                      height: 36,
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                        ),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.edit_rounded,
                        color: Colors.white,
                        size: 18,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'What\'s on your mind?',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          // Quick action buttons
          _QuickActionButton(
            icon: Icons.photo_library_rounded,
            color: AppColors.calmGreen,
            onTap: onCreatePost,
          ),
          const SizedBox(width: 8),
          _QuickActionButton(
            icon: Icons.videocam_rounded,
            color: AppColors.accentOrange,
            onTap: onCreateStory,
          ),
        ],
      ),
    );
  }
}

class _QuickActionButton extends StatelessWidget {
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  const _QuickActionButton({
    required this.icon,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        HapticFeedback.lightImpact();
        onTap();
      },
      child: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: color.withOpacity(0.15),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: color, size: 24),
      ),
    );
  }
}

// ============================================================================
// Feed Filter Chips
// ============================================================================

class _FeedFilterChips extends StatelessWidget {
  final FeedFilter selectedFilter;
  final ValueChanged<FeedFilter> onFilterChanged;

  const _FeedFilterChips({
    required this.selectedFilter,
    required this.onFilterChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 8),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: theme.dividerColor.withOpacity(0.1),
          ),
        ),
      ),
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: Row(
          children: FeedFilter.values.map((filter) {
            final isSelected = selectedFilter == filter;
            return Padding(
              padding: const EdgeInsets.only(right: 8),
              child: _FilterPill(
                label: _getFilterLabel(filter),
                icon: _getFilterIcon(filter),
                isSelected: isSelected,
                onTap: () => onFilterChanged(filter),
              ),
            );
          }).toList(),
        ),
      ),
    );
  }

  String _getFilterLabel(FeedFilter filter) {
    switch (filter) {
      case FeedFilter.forYou:
        return 'For You';
      case FeedFilter.following:
        return 'Following';
      case FeedFilter.trending:
        return 'Trending';
      case FeedFilter.support:
        return 'Support';
      case FeedFilter.wins:
        return 'Wins';
    }
  }

  IconData _getFilterIcon(FeedFilter filter) {
    switch (filter) {
      case FeedFilter.forYou:
        return Icons.auto_awesome;
      case FeedFilter.following:
        return Icons.people_alt_rounded;
      case FeedFilter.trending:
        return Icons.trending_up_rounded;
      case FeedFilter.support:
        return Icons.favorite_rounded;
      case FeedFilter.wins:
        return Icons.celebration_rounded;
    }
  }
}

class _FilterPill extends StatefulWidget {
  final String label;
  final IconData icon;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterPill({
    required this.label,
    required this.icon,
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
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          decoration: BoxDecoration(
            gradient: widget.isSelected
                ? LinearGradient(
                    colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                  )
                : null,
            color: widget.isSelected
                ? null
                : theme.colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(24),
            boxShadow: widget.isSelected
                ? [
                    BoxShadow(
                      color: AppColors.primaryPurple.withOpacity(0.3),
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
                size: 18,
                color: widget.isSelected
                    ? Colors.white
                    : theme.colorScheme.onSurfaceVariant,
              ),
              const SizedBox(width: 6),
              Text(
                widget.label,
                style: theme.textTheme.labelLarge?.copyWith(
                  color: widget.isSelected
                      ? Colors.white
                      : theme.colorScheme.onSurfaceVariant,
                  fontWeight: widget.isSelected ? FontWeight.bold : FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ============================================================================
// Feed Section Header
// ============================================================================

class _FeedSectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;
  final int count;

  const _FeedSectionHeader({
    required this.title,
    required this.icon,
    required this.count,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
      child: Row(
        children: [
          Container(
            width: 4,
            height: 24,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 12),
          Icon(
            icon,
            size: 20,
            color: AppColors.primaryPurple,
          ),
          const SizedBox(width: 8),
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const Spacer(),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              '$count posts',
              style: theme.textTheme.labelSmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// ============================================================================
// Animated Post Card Wrapper
// ============================================================================

class _AnimatedPostCard extends StatefulWidget {
  final Post post;
  final int animationDelay;
  final VoidCallback? onLike;
  final VoidCallback? onComment;
  final VoidCallback? onShare;
  final VoidCallback? onProfileTap;
  final VoidCallback? onReport;
  final VoidCallback? onDelete;
  final Function(String)? onMentionTap;
  final Function(String)? onHashtagTap;

  const _AnimatedPostCard({
    required this.post,
    required this.animationDelay,
    this.onLike,
    this.onComment,
    this.onShare,
    this.onProfileTap,
    this.onReport,
    this.onDelete,
    this.onMentionTap,
    this.onHashtagTap,
  });

  @override
  State<_AnimatedPostCard> createState() => _AnimatedPostCardState();
}

class _AnimatedPostCardState extends State<_AnimatedPostCard>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _fadeAnimation = CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.1),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOutCubic,
    ));

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
    return FadeTransition(
      opacity: _fadeAnimation,
      child: SlideTransition(
        position: _slideAnimation,
        child: PostCard(
          post: widget.post,
          onLike: widget.onLike,
          onComment: widget.onComment,
          onShare: widget.onShare,
          onProfileTap: widget.onProfileTap,
          onReport: widget.onReport,
          onDelete: widget.onDelete,
          onMentionTap: widget.onMentionTap,
          onHashtagTap: widget.onHashtagTap,
        ),
      ),
    );
  }
}

// ============================================================================
// Empty Feed State
// ============================================================================

class _EmptyFeedState extends StatelessWidget {
  final FeedFilter filter;
  final VoidCallback onCreatePost;
  final VoidCallback onRefresh;

  const _EmptyFeedState({
    required this.filter,
    required this.onCreatePost,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Animated illustration
          TweenAnimationBuilder<double>(
            tween: Tween(begin: 0.8, end: 1.0),
            duration: const Duration(milliseconds: 600),
            curve: Curves.easeOutBack,
            builder: (context, scale, child) {
              return Transform.scale(scale: scale, child: child);
            },
            child: Container(
              width: 140,
              height: 140,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    AppColors.primaryPurple.withOpacity(0.2),
                    AppColors.secondaryTeal.withOpacity(0.1),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  _getEmoji(),
                  style: const TextStyle(fontSize: 64),
                ),
              ),
            ),
          ),
          const SizedBox(height: 28),
          Text(
            _getTitle(),
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 12),
          Text(
            _getSubtitle(),
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              FilledButton.icon(
                onPressed: () {
                  HapticFeedback.lightImpact();
                  onCreatePost();
                },
                icon: const Icon(Icons.add_rounded),
                label: const Text('Create Post'),
                style: FilledButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                ),
              ),
              const SizedBox(width: 12),
              OutlinedButton.icon(
                onPressed: () {
                  HapticFeedback.lightImpact();
                  onRefresh();
                },
                icon: const Icon(Icons.refresh_rounded),
                label: const Text('Refresh'),
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  String _getEmoji() {
    switch (filter) {
      case FeedFilter.forYou:
        return '✨';
      case FeedFilter.following:
        return '👥';
      case FeedFilter.trending:
        return '📈';
      case FeedFilter.support:
        return '💜';
      case FeedFilter.wins:
        return '🎉';
    }
  }

  String _getTitle() {
    switch (filter) {
      case FeedFilter.forYou:
        return 'Your Feed Awaits!';
      case FeedFilter.following:
        return 'Follow Some Friends';
      case FeedFilter.trending:
        return 'Nothing Trending Yet';
      case FeedFilter.support:
        return 'Support Community';
      case FeedFilter.wins:
        return 'Share Your Wins!';
    }
  }

  String _getSubtitle() {
    switch (filter) {
      case FeedFilter.forYou:
        return 'Be the first to share something with the community!';
      case FeedFilter.following:
        return 'Follow people to see their posts here.';
      case FeedFilter.trending:
        return 'Check back later for trending content.';
      case FeedFilter.support:
        return 'Share or request support from the community.';
      case FeedFilter.wins:
        return 'Celebrate your achievements, big or small!';
    }
  }
}

// ============================================================================
// Loading More Indicator
// ============================================================================

class _LoadingMoreIndicator extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Center(
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(
                strokeWidth: 2,
                color: AppColors.primaryPurple,
              ),
            ),
            const SizedBox(width: 12),
            Text(
              'Loading more...',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
// Story Item (Keep existing implementation)
// ============================================================================

class _StoryItem extends StatefulWidget {
  final String name;
  final String? avatarUrl;
  final bool isAddStory;
  final bool hasUnseenStory;
  final bool animate;
  final VoidCallback? onTap;
  final int index;

  const _StoryItem({
    required this.name,
    this.avatarUrl,
    this.isAddStory = false,
    this.hasUnseenStory = false,
    this.animate = true,
    this.onTap,
    this.index = 0,
  });

  @override
  State<_StoryItem> createState() => _StoryItemState();
}

class _StoryItemState extends State<_StoryItem>
    with SingleTickerProviderStateMixin, AutomaticKeepAliveClientMixin {
  late AnimationController _controller;
  bool _isAnimating = false;

  static const _unseenColors = [
    Color(0xFFFF6B6B),
    Color(0xFFFFE66D),
    Color(0xFF4ECDC4),
    Color(0xFF45B7D1),
    Color(0xFFDDA0DD),
    Color(0xFFFF6B6B),
  ];

  static const _seenColors = [
    Color(0xFF7E57C2),
    Color(0xFF5C6BC0),
    Color(0xFF26A69A),
    Color(0xFF42A5F5),
    Color(0xFFAB47BC),
    Color(0xFF7E57C2),
  ];

  static const _addColors = [
    Color(0xFFE040FB),
    Color(0xFF7C4DFF),
    Color(0xFF536DFE),
    Color(0xFF448AFF),
    Color(0xFFE040FB),
  ];

  @override
  bool get wantKeepAlive => false;

  @override
  void initState() {
    super.initState();
    final duration = widget.hasUnseenStory
        ? const Duration(milliseconds: 2000)
        : widget.isAddStory
            ? const Duration(milliseconds: 2500)
            : const Duration(milliseconds: 3500);

    _controller = AnimationController(duration: duration, vsync: this);

    if (widget.animate) {
      _startAnimationWithDelay();
    }
  }

  void _startAnimationWithDelay() {
    Future.delayed(Duration(milliseconds: widget.index * 100), () {
      if (mounted && !_isAnimating) {
        _isAnimating = true;
        _controller.repeat();
      }
    });
  }

  @override
  void dispose() {
    _isAnimating = false;
    _controller.dispose();
    super.dispose();
  }

  @override
  void didUpdateWidget(_StoryItem oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.animate && !_isAnimating) {
      _startAnimationWithDelay();
    } else if (!widget.animate && _isAnimating) {
      _isAnimating = false;
      _controller.stop();
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final theme = Theme.of(context);

    final colors = widget.hasUnseenStory
        ? _unseenColors
        : widget.isAddStory
            ? _addColors
            : _seenColors;

    final ringSize = widget.hasUnseenStory ? 68.0 : 66.0;
    final strokeWidth = widget.hasUnseenStory ? 3.5 : 2.5;

    return GestureDetector(
      onTap: widget.onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            width: 70,
            height: 70,
            child: Stack(
              alignment: Alignment.center,
              children: [
                AnimatedBuilder(
                  animation: _controller,
                  builder: (context, child) {
                    return CustomPaint(
                      size: Size(ringSize, ringSize),
                      painter: _VibrantRingPainter(
                        rotation: _controller.value * 360,
                        colors: colors,
                        strokeWidth: strokeWidth,
                        isDashed: widget.isAddStory,
                      ),
                    );
                  },
                ),
                Container(
                  width: 60,
                  height: 60,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: theme.colorScheme.surface,
                  ),
                ),
                SizedBox(
                  width: 54,
                  height: 54,
                  child: ClipOval(
                    child: widget.isAddStory
                        ? Container(
                            color: theme.colorScheme.surfaceContainerHighest,
                            child: Icon(
                              Icons.add,
                              size: 28,
                              color: theme.colorScheme.primary,
                            ),
                          )
                        : Opacity(
                            opacity: widget.hasUnseenStory ? 1.0 : 0.85,
                            child: widget.avatarUrl != null
                                ? Image.network(
                                    widget.avatarUrl!,
                                    fit: BoxFit.cover,
                                    errorBuilder: (_, __, ___) => Container(
                                      color: theme.colorScheme.surfaceContainerHighest,
                                      child: Icon(
                                        Icons.person,
                                        size: 32,
                                        color: theme.colorScheme.onSurfaceVariant,
                                      ),
                                    ),
                                  )
                                : Container(
                                    color: theme.colorScheme.surfaceContainerHighest,
                                    child: Icon(
                                      Icons.person,
                                      size: 32,
                                      color: theme.colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                          ),
                  ),
                ),
                if (widget.isAddStory)
                  Positioned(
                    right: 2,
                    bottom: 2,
                    child: Container(
                      width: 22,
                      height: 22,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: const LinearGradient(
                          colors: [Color(0xFFE040FB), Color(0xFF7C4DFF)],
                        ),
                        border: Border.all(
                          color: theme.colorScheme.surface,
                          width: 2,
                        ),
                      ),
                      child: const Icon(
                        Icons.add,
                        size: 14,
                        color: Colors.white,
                      ),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 6),
          SizedBox(
            width: 70,
            child: Text(
              widget.name,
              style: theme.textTheme.labelSmall?.copyWith(
                fontWeight: widget.hasUnseenStory ? FontWeight.w600 : FontWeight.normal,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
            ),
          ),
        ],
      ),
    );
  }
}

class _VibrantRingPainter extends CustomPainter {
  final double rotation;
  final List<Color> colors;
  final double strokeWidth;
  final bool isDashed;

  _VibrantRingPainter({
    required this.rotation,
    required this.colors,
    required this.strokeWidth,
    this.isDashed = false,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = (size.width - strokeWidth) / 2;

    final sweepGradient = SweepGradient(
      colors: colors,
      transform: GradientRotation(rotation * 3.14159 / 180),
    );

    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round
      ..shader = sweepGradient.createShader(
        Rect.fromCircle(center: center, radius: radius),
      );

    if (isDashed) {
      // Draw dashed circle
      const dashLength = 10.0;
      const gapLength = 6.0;
      final circumference = 2 * 3.14159 * radius;
      final dashCount = (circumference / (dashLength + gapLength)).floor();
      final anglePerDash = (2 * 3.14159) / dashCount;
      final dashAngle = anglePerDash * (dashLength / (dashLength + gapLength));

      for (int i = 0; i < dashCount; i++) {
        final startAngle = i * anglePerDash - 3.14159 / 2;
        canvas.drawArc(
          Rect.fromCircle(center: center, radius: radius),
          startAngle,
          dashAngle,
          false,
          paint,
        );
      }
    } else {
      canvas.drawCircle(center, radius, paint);
    }
  }

  @override
  bool shouldRepaint(_VibrantRingPainter oldDelegate) {
    return oldDelegate.rotation != rotation;
  }
}

// ============================================================================
// Comments Sheet (Keep existing implementation)
// ============================================================================

class CommentsSheet extends ConsumerWidget {
  final String postId;

  const CommentsSheet({super.key, required this.postId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
          ),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.dividerColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    Text(
                      'Comments',
                      style: theme.textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const Spacer(),
                    IconButton(
                      icon: const Icon(Icons.close),
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],
                ),
              ),
              const Divider(),
              Expanded(
                child: ListView(
                  controller: scrollController,
                  padding: const EdgeInsets.all(16),
                  children: [
                    Center(
                      child: Column(
                        children: [
                          const SizedBox(height: 40),
                          Icon(
                            Icons.chat_bubble_outline,
                            size: 48,
                            color: theme.colorScheme.outline,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'No comments yet',
                            style: theme.textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'Be the first to share your thoughts!',
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              _buildCommentInput(context),
            ],
          ),
        );
      },
    );
  }

  Widget _buildCommentInput(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(
        left: 16,
        right: 16,
        top: 12,
        bottom: MediaQuery.of(context).viewInsets.bottom + 12,
      ),
      decoration: BoxDecoration(
        color: theme.cardColor,
        border: Border(
          top: BorderSide(color: theme.dividerColor.withOpacity(0.1)),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(24),
              ),
              child: TextField(
                decoration: InputDecoration(
                  hintText: 'Write a comment...',
                  border: InputBorder.none,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 12,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 8),
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
              ),
              shape: BoxShape.circle,
            ),
            child: IconButton(
              icon: const Icon(Icons.send_rounded, color: Colors.white),
              onPressed: () {
                HapticFeedback.lightImpact();
              },
            ),
          ),
        ],
      ),
    );
  }
}

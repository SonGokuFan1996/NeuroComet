import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/user.dart';
import '../../providers/profile_provider.dart';
import '../../services/supabase_service.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../widgets/post/post_card.dart';
import '../../widgets/profile/neuro_traits.dart';
import '../../l10n/app_localizations.dart';

/// Profile tab options matching Kotlin version
enum ProfileTab {
  posts(Icons.grid_view_outlined),
  about(Icons.person_outline),
  interests(Icons.favorite_outline),
  badges(Icons.emoji_events_outlined);

  final IconData icon;

  const ProfileTab(this.icon);
}

class ProfileScreen extends ConsumerStatefulWidget {
  final String? userId;

  const ProfileScreen({super.key, this.userId});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  String _tabLabel(AppLocalizations l10n, ProfileTab tab) {
    switch (tab) {
      case ProfileTab.posts:
        return l10n.posts;
      case ProfileTab.about:
        return l10n.about;
      case ProfileTab.interests:
        return l10n.get('interests');
      case ProfileTab.badges:
        return l10n.get('badges');
    }
  }

  // Per-user profile data — resolved from the fake-profile database when available
  List<NeuroDivergentTrait> get _traits {
    final data = _fakeData;
    return data?.traits ?? _defaultTraits;
  }

  List<String> get _interests {
    final data = _fakeData;
    return data?.interests ?? _defaultInterests;
  }

  String get _communicationNotes {
    final data = _fakeData;
    return data?.communicationNotes ?? _defaultCommunicationNotes;
  }

  String get _pronouns {
    final data = _fakeData;
    return data?.pronouns ?? 'they/them';
  }

  String get _funFact {
    final data = _fakeData;
    return data?.funFact ??
        "I can talk about ${_interests.first} for hours! Feel free to ask me anything about it.";
  }

  String get _location {
    final data = _fakeData;
    return data?.location ?? 'The Hyperfocus Zone';
  }

  String get _joinedDate {
    final data = _fakeData;
    return data?.joinedDate ?? 'January 2024';
  }

  FakeProfileData? get _fakeData {
    final uid = widget.userId;
    if (uid == null) return null;
    return ref.read(fakeProfileDataProvider(uid));
  }

  EnergyStatus get _energyStatus {
    final data = _fakeData;
    return data?.energyStatus ?? _overriddenEnergyStatus ?? EnergyStatus.socialMode;
  }

  // Only used for the current-user energy picker override
  EnergyStatus? _overriddenEnergyStatus;

  // Defaults for users not in the fake-profile database
  static const _defaultTraits = [
    NeuroDivergentTrait.directCommunicator,
    NeuroDivergentTrait.needsProcessingTime,
    NeuroDivergentTrait.socialBattery,
    NeuroDivergentTrait.passionateInterests,
  ];

  static const _defaultInterests = [
    'Coding',
    'Mechanical Keyboards',
    'Cats',
    'Accessibility',
  ];

  static const _defaultCommunicationNotes =
      'I prefer text over calls. Tone indicators appreciated! /gen';


  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  bool get _isCurrentUser => widget.userId == null;

  @override
  Widget build(BuildContext context) {
    final profileState = widget.userId != null
        ? ref.watch(profileProvider(widget.userId!))
        : ref.watch(currentUserProfileProvider);

    return Scaffold(
      body: profileState.when(
        loading: () => const NeuroLoading(),
        error: (error, stack) => _buildErrorState(error.toString()),
        data: (user) => _buildProfileContent(user),
      ),
    );
  }

  Widget _buildProfileContent(User user) {
    final theme = Theme.of(context);
    final l10n = context.l10n;

    // Get custom avatar if this is current user's profile
    final customAvatar = _isCurrentUser ? ref.watch(customAvatarProvider) : null;

    return NestedScrollView(
      headerSliverBuilder: (context, innerBoxIsScrolled) {
        return [
          SliverAppBar(
            expandedHeight: 200,
            pinned: true,
            // Fix translucency - use opaque background color
            backgroundColor: theme.colorScheme.surface,
            surfaceTintColor: Colors.transparent,
            scrolledUnderElevation: 0,
            forceElevated: innerBoxIsScrolled,
            flexibleSpace: FlexibleSpaceBar(
              background: Stack(
                fit: StackFit.expand,
                children: [
                  if (user.bannerUrl != null)
                    Image.network(
                      user.bannerUrl!,
                      fit: BoxFit.cover,
                      errorBuilder: (_, __, ___) => Container(
                        color: theme.colorScheme.primaryContainer,
                      ),
                    )
                  else
                    Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [
                            theme.colorScheme.primary,
                            theme.colorScheme.secondary,
                          ],
                        ),
                      ),
                    ),
                  Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.transparent,
                          Colors.black.withValues(alpha: 0.5),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
            actions: [
              if (_isCurrentUser) ...[
                IconButton(
                  icon: const Icon(Icons.settings_outlined),
                  onPressed: () => context.push('/settings'),
                  tooltip: l10n.settings,
                ),
                IconButton(
                  icon: const Icon(Icons.edit),
                  onPressed: () => context.push('/edit-profile'),
                  tooltip: l10n.editProfile,
                ),
              ],
              IconButton(
                icon: const Icon(Icons.more_vert),
                onPressed: () => _showProfileOptions(context),
              ),
            ],
          ),
          SliverToBoxAdapter(
            child: _buildProfileHeader(user, customAvatar),
          ),
          // Traits section
          if (_traits.isNotEmpty)
            SliverToBoxAdapter(
              child: TraitsSection(
                traits: _traits,
                onTraitTap: (trait) => _showTraitInfo(trait),
              ),
            ),
          // Communication notes
          if (_communicationNotes.isNotEmpty)
            SliverToBoxAdapter(
              child: CommunicationNotesCard(notes: _communicationNotes),
            ),
          // Tab bar
          SliverPersistentHeader(
            pinned: true,
            delegate: _SliverTabBarDelegate(
              TabBar(
                controller: _tabController,
                tabs: ProfileTab.values.map((tab) => Tab(
                  icon: Icon(tab.icon, size: 20),
                  text: _tabLabel(l10n, tab),
                )).toList(),
                labelStyle: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                  height: 1.0,
                  leadingDistribution: TextLeadingDistribution.even,
                ),
                unselectedLabelStyle: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.normal,
                  height: 1.0,
                  leadingDistribution: TextLeadingDistribution.even,
                ),
                indicatorSize: TabBarIndicatorSize.label,
                dividerColor: Colors.transparent,
              ),
              theme.scaffoldBackgroundColor,
            ),
          ),
        ];
      },
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildPostsTab(user.id),
          _buildAboutTab(user),
          _buildInterestsTab(),
          _buildBadgesTab(user),
        ],
      ),
    );
  }

  Widget _buildProfileHeader(User user, dynamic customAvatar) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Avatar with gradient ring
              Container(
                width: 86,
                height: 86,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    colors: [
                      theme.colorScheme.primary,
                      theme.colorScheme.tertiary,
                    ],
                  ),
                ),
                padding: const EdgeInsets.all(3),
                child: NeuroAvatar(
                  imageUrl: user.avatarUrl,
                  name: user.displayName,
                  size: 80,
                  customAvatar: customAvatar,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _buildStatColumn(user.postCount.toString(), l10n.posts),
                    _buildStatColumn(
                      _formatCount(user.followerCount),
                      l10n.followers,
                      onTap: () => _showFollowers(user.id),
                    ),
                    _buildStatColumn(
                      _formatCount(user.followingCount),
                      l10n.following,
                      onTap: () => _showFollowing(user.id),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Name and energy status
          Row(
            children: [
              Text(
                user.displayName,
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(width: 8),
              if (_isCurrentUser)
                EnergyStatusBadge(
                  status: _energyStatus,
                  onTap: () => _showEnergyPicker(),
                )
              else
                EnergyStatusBadge(
                  status: _energyStatus,
                  showLabel: false,
                ),
            ],
          ),

          // Username and pronouns
          if (user.username != null) ...[
            const SizedBox(height: 2),
            Row(
              children: [
                Text(
                  '@${user.username}',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.outline,
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.secondaryContainer,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    _pronouns,
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: theme.colorScheme.onSecondaryContainer,
                    ),
                  ),
                ),
              ],
            ),
          ],

          if (user.bio != null && user.bio!.isNotEmpty) ...[
            const SizedBox(height: 12),
            Text(user.bio!, style: theme.textTheme.bodyMedium),
          ],

          const SizedBox(height: 16),
          if (!_isCurrentUser) _buildActionButtons(user),
        ],
      ),
    );
  }

  Widget _buildStatColumn(String value, String label, {VoidCallback? onTap}) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Text(
            value,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.outline,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildActionButtons(User user) {
    final l10n = context.l10n;
    return Row(
      children: [
        Expanded(
          child: user.isFollowing
              ? OutlinedButton(
                  onPressed: () => _toggleFollow(user.id),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.check, size: 18),
                      const SizedBox(width: 4),
                      Text(l10n.following),
                    ],
                  ),
                )
              : ElevatedButton(
                  onPressed: () => _toggleFollow(user.id),
                  child: Text(l10n.follow),
                ),
        ),
        const SizedBox(width: 12),
        OutlinedButton(
          onPressed: () => _startMessage(user.id),
          child: const Icon(Icons.mail_outline),
        ),
      ],
    );
  }

  Widget _buildPostsTab(String userId) {
    final postsState = ref.watch(userPostsProvider(userId));

    return postsState.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, _) => Center(child: Text(error.toString())),
      data: (posts) {
        if (posts.isEmpty) {
          return _buildEmptyPostsPlaceholder();
        }
        return ListView.builder(
          padding: const EdgeInsets.only(top: 8, bottom: 100),
          itemCount: posts.length,
          itemBuilder: (context, index) => PostCard(post: posts[index]),
        );
      },
    );
  }

  Widget _buildEmptyPostsPlaceholder() {
    final theme = Theme.of(context);
    final l10n = context.l10n;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.article_outlined,
              size: 64,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 16),
            Text(
              _isCurrentUser ? l10n.get('shareYourFirstPost') : l10n.noPosts,
              style: theme.textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              _isCurrentUser
                  ? l10n.get('yourPostsWillAppearHere')
                  : l10n.get('checkBackLaterForNewContent'),
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

  Widget _buildAboutTab(User user) {
    final theme = Theme.of(context);
    final l10n = context.l10n;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
      children: [
        // Bio section
        if (user.bio != null && user.bio!.isNotEmpty) ...[
          _buildAboutSection(l10n.about, Icons.info_outline, user.bio!),
          const SizedBox(height: 16),
        ],

        // Location
        _buildAboutSection(l10n.get('location'), Icons.location_on_outlined, _location),
        const SizedBox(height: 16),

        // Joined date
        _buildAboutSection(l10n.get('joined'), Icons.calendar_today_outlined, _joinedDate),
        const SizedBox(height: 16),

        // Traits expanded
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.psychology, size: 20, color: theme.colorScheme.primary),
                    const SizedBox(width: 8),
                    Text(
                      l10n.get('howIWorkBest'),
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                ..._traits.map((trait) => ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: Container(
                    width: 40,
                    height: 40,
                    decoration: BoxDecoration(
                      color: trait.color.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Center(
                      child: Text(trait.emoji, style: const TextStyle(fontSize: 20)),
                    ),
                  ),
                  title: Text(
                    trait.label,
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: trait.color,
                    ),
                  ),
                  subtitle: Text(
                    trait.description,
                    style: theme.textTheme.bodySmall,
                  ),
                )),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAboutSection(String title, IconData icon, String content) {
    final theme = Theme.of(context);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 20, color: theme.colorScheme.outline),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: theme.textTheme.labelMedium?.copyWith(
                  color: theme.colorScheme.outline,
                ),
              ),
              const SizedBox(height: 2),
              Text(content, style: theme.textTheme.bodyMedium),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildInterestsTab() {
    final theme = Theme.of(context);
    final l10n = context.l10n;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
      children: [
        InterestsSection(interests: _interests),
        const SizedBox(height: 24),

        // Additional interests info
        Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.get('funFact'),
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  _funFact,
                  style: theme.textTheme.bodyMedium,
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildBadgesTab(User user) {
    final theme = Theme.of(context);
    final l10n = context.l10n;
    final badges = user.badges ?? [];

    if (badges.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.emoji_events_outlined,
              size: 64,
              color: theme.colorScheme.outline,
            ),
            const SizedBox(height: 16),
            Text(
              l10n.get('noBadgesYet'),
              style: theme.textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(
              l10n.get('badgesEarnedThroughCommunityParticipation'),
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.outline,
              ),
            ),
          ],
        ),
      );
    }

    return GridView.builder(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        childAspectRatio: 0.85,
      ),
      itemCount: badges.length,
      itemBuilder: (context, index) {
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text('🏆', style: TextStyle(fontSize: 32)),
                const SizedBox(height: 8),
                Text(
                  badges[index],
                  style: theme.textTheme.labelSmall,
                  textAlign: TextAlign.center,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildErrorState(String error) {
    final l10n = context.l10n;
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64),
          const SizedBox(height: 16),
          Text(error),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () {
              if (widget.userId != null) {
                ref.invalidate(profileProvider(widget.userId!));
              } else {
                ref.invalidate(currentUserProfileProvider);
              }
            },
            child: Text(l10n.retry),
          ),
        ],
      ),
    );
  }

  String _formatCount(int count) {
    if (count >= 1000000) {
      return '${(count / 1000000).toStringAsFixed(1)}M';
    } else if (count >= 1000) {
      return '${(count / 1000).toStringAsFixed(1)}K';
    }
    return count.toString();
  }

  void _toggleFollow(String userId) {
    // Optimistic toggle – fire-and-forget backend call
    try {
      SupabaseService.isFollowing(userId).then((following) {
        if (following) {
          SupabaseService.unfollowUser(userId);
        } else {
          SupabaseService.followUser(userId);
        }
      });
    } catch (e) {
      debugPrint('Follow toggle error: $e');
    }
  }

  void _startMessage(String userId) {
    context.push('/chat', extra: {'userId': userId});
  }

  void _showFollowers(String userId) {
    context.push('/followers/$userId');
  }

  void _showFollowing(String userId) {
    context.push('/following/$userId');
  }

  void _showTraitInfo(NeuroDivergentTrait trait) {
    showDialog(
      context: context,
      builder: (context) => TraitInfoDialog(trait: trait),
    );
  }

  void _showEnergyPicker() {
    showDialog(
      context: context,
      builder: (context) => EnergyStatusPickerDialog(
        currentStatus: _energyStatus,
        onStatusSelected: (status) {
          setState(() => _overriddenEnergyStatus = status);
        },
      ),
    );
  }

  void _showProfileOptions(BuildContext context) {
    final l10n = context.l10n;
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.share),
                title: Text(l10n.get('shareProfile')),
                onTap: () => Navigator.pop(context),
              ),
              if (!_isCurrentUser) ...[
                ListTile(
                  leading: const Icon(Icons.block),
                  title: Text(l10n.get('blockUser')),
                  onTap: () => Navigator.pop(context),
                ),
                ListTile(
                  leading: const Icon(Icons.report),
                  title: Text(l10n.get('reportUser')),
                  onTap: () => Navigator.pop(context),
                ),
              ],
            ],
          ),
        );
      },
    );
  }
}

class _SliverTabBarDelegate extends SliverPersistentHeaderDelegate {
  final TabBar tabBar;
  final Color backgroundColor;

  _SliverTabBarDelegate(this.tabBar, this.backgroundColor);

  @override
  double get minExtent => tabBar.preferredSize.height;

  @override
  double get maxExtent => tabBar.preferredSize.height;

  @override
  Widget build(
    BuildContext context,
    double shrinkOffset,
    bool overlapsContent,
  ) {
    return Container(
      color: backgroundColor,
      child: tabBar,
    );
  }

  @override
  bool shouldRebuild(_SliverTabBarDelegate oldDelegate) {
    return tabBar != oldDelegate.tabBar || backgroundColor != oldDelegate.backgroundColor;
  }
}

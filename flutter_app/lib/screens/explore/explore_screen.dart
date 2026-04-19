import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:share_plus/share_plus.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';

/// Premium Explore Screen - Discover content with beautiful animations
class ExploreScreen extends ConsumerStatefulWidget {
  const ExploreScreen({super.key});

  @override
  ConsumerState<ExploreScreen> createState() => _ExploreScreenState();
}

class _ExploreScreenState extends ConsumerState<ExploreScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;
  late AnimationController _headerAnimController;
  late Animation<double> _headerFadeAnimation;
  final _searchController = TextEditingController();
  final _focusNode = FocusNode();
  bool _isSearching = false;
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _searchController.addListener(_onSearchChanged);

    _headerAnimController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _headerFadeAnimation = CurvedAnimation(
      parent: _headerAnimController,
      curve: Curves.easeOutCubic,
    );
    _headerAnimController.forward();
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    _focusNode.dispose();
    _headerAnimController.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    setState(() => _searchQuery = _searchController.text);
  }

  void _startSearch() {
    HapticFeedback.lightImpact();
    setState(() => _isSearching = true);
    _focusNode.requestFocus();
  }

  void _cancelSearch() {
    HapticFeedback.lightImpact();
    setState(() {
      _isSearching = false;
      _searchQuery = '';
    });
    _searchController.clear();
    _focusNode.unfocus();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: Column(
          children: [
            FadeTransition(
              opacity: _headerFadeAnimation,
              child: _ExploreHeader(
                isSearching: _isSearching,
                searchController: _searchController,
                focusNode: _focusNode,
                onStartSearch: _startSearch,
                onCancelSearch: _cancelSearch,
              ),
            ),
            if (!_isSearching)
              _ExploreFilterTabs(tabController: _tabController),
            Expanded(
              child: _isSearching
                  ? _SearchView(searchController: _searchController, searchQuery: _searchQuery)
                  : TabBarView(
                      controller: _tabController,
                      children: const [_ForYouTab(), _TrendingTab(), _PeopleTab(), _TopicsTab()],
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ExploreHeader extends StatelessWidget {
  final bool isSearching;
  final TextEditingController searchController;
  final FocusNode focusNode;
  final VoidCallback onStartSearch;
  final VoidCallback onCancelSearch;

  const _ExploreHeader({
    required this.isSearching,
    required this.searchController,
    required this.focusNode,
    required this.onStartSearch,
    required this.onCancelSearch,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Container(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Title row (hidden when searching)
          if (!isSearching)
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.explore,
                        style: theme.textTheme.headlineMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Discover amazing content ✨',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _HeaderIconButton(
                      icon: Icons.search_rounded,
                      onPressed: onStartSearch,
                      tooltip: 'Search',
                    ),
                    _HeaderIconButton(
                      icon: Icons.tune_rounded,
                      onPressed: () => HapticFeedback.lightImpact(),
                      tooltip: 'Filters',
                    ),
                  ],
                ),
              ],
            ),

          // Search bar (shown when searching)
          if (isSearching)
            _SearchBarExpanded(
              controller: searchController,
              focusNode: focusNode,
              onCancel: onCancelSearch,
            ),
        ],
      ),
    );
  }
}

/// Header icon button matching Notifications/Messages style
class _HeaderIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String? tooltip;

  const _HeaderIconButton({
    required this.icon,
    required this.onPressed,
    this.tooltip,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    Widget button = Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: () {
          HapticFeedback.lightImpact();
          onPressed();
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Icon(
            icon,
            size: 22,
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
      ),
    );

    if (tooltip != null) {
      button = Tooltip(message: tooltip!, child: button);
    }

    return button;
  }
}

/// Expanded search bar when searching
class _SearchBarExpanded extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode focusNode;
  final VoidCallback onCancel;

  const _SearchBarExpanded({
    required this.controller,
    required this.focusNode,
    required this.onCancel,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: theme.colorScheme.primary.withValues(alpha: 0.3),
          width: 2,
        ),
      ),
      child: TextField(
        controller: controller,
        focusNode: focusNode,
        autofocus: true,
        decoration: InputDecoration(
          hintText: 'Search posts, people, topics...',
          prefixIcon: const Icon(Icons.search_rounded),
          suffixIcon: IconButton(
            icon: const Icon(Icons.close_rounded),
            onPressed: onCancel,
          ),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        ),
      ),
    );
  }
}

class _ExploreFilterTabs extends StatelessWidget {
  final TabController tabController;

  const _ExploreFilterTabs({required this.tabController});

  @override
  Widget build(BuildContext context) {
    final filters = [
      (0, 'For You', Icons.auto_awesome_rounded),
      (1, 'Trending', Icons.trending_up_rounded),
      (2, 'People', Icons.people_alt_rounded),
      (3, 'Topics', Icons.tag_rounded),
    ];

    return Container(
      height: 56,
      margin: const EdgeInsets.only(top: 8),
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 20),
        separatorBuilder: (_, __) => const SizedBox(width: 10),
        itemCount: filters.length,
        itemBuilder: (context, index) {
          final (tabIndex, label, icon) = filters[index];

          return AnimatedBuilder(
            animation: tabController,
            builder: (context, child) {
              final isSelected = tabController.index == tabIndex;
              return _FilterPill(
                label: label,
                icon: icon,
                isSelected: isSelected,
                onTap: () {
                  HapticFeedback.selectionClick();
                  tabController.animateTo(tabIndex);
                },
              );
            },
          );
        },
      ),
    );
  }
}

/// Individual filter pill matching Notifications/Messages style
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
    final primaryColor = theme.colorScheme.primary;

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
          curve: Curves.easeOutCubic,
          padding: EdgeInsets.symmetric(
            horizontal: widget.isSelected ? 16 : 14,
            vertical: 10,
          ),
          decoration: BoxDecoration(
            color: widget.isSelected
                ? primaryColor.withValues(alpha: 0.15)
                : theme.colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(20),
            border: widget.isSelected
                ? Border.all(
                    color: primaryColor.withValues(alpha: 0.4),
                    width: 1.5,
                  )
                : null,
            boxShadow: widget.isSelected
                ? [
                    BoxShadow(
                      color: primaryColor.withValues(alpha: 0.15),
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
                size: 16,
                color: widget.isSelected
                    ? primaryColor
                    : theme.colorScheme.onSurfaceVariant,
              ),
              const SizedBox(width: 6),
              Text(
                widget.label,
                style: theme.textTheme.labelLarge?.copyWith(
                  color: widget.isSelected
                      ? primaryColor
                      : theme.colorScheme.onSurface,
                  fontWeight: widget.isSelected ? FontWeight.w600 : FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SearchView extends StatelessWidget {
  final TextEditingController searchController;
  final String searchQuery;

  const _SearchView({required this.searchController, required this.searchQuery});

  @override
  Widget build(BuildContext context) {
    if (searchQuery.isEmpty) return _RecentSearchesView(searchController: searchController);
    return _SearchResultsView(query: searchQuery);
  }
}

class _SearchResultsView extends StatelessWidget {
  final String query;
  const _SearchResultsView({required this.query});

  @override
  Widget build(BuildContext context) {

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _SectionHeader(title: 'Results for "$query"', icon: Icons.search_rounded),
        const SizedBox(height: 16),
        _PersonResultCard(name: 'ADHD Support Group', username: 'adhd_support', bio: 'Community for ADHD tips and support', avatarUrl: 'https://i.pravatar.cc/150?u=adhd'),
        _PersonResultCard(name: 'Mindful Living', username: 'mindful_life', bio: 'Daily mindfulness and self-care tips', avatarUrl: 'https://i.pravatar.cc/150?u=mindful'),
        const SizedBox(height: 16),
        _SectionHeader(title: 'Related Topics', icon: Icons.tag_rounded),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: ['#$query', '#${query}Community', '#${query}Tips', '#${query}Support'].map((tag) => _TopicChip(label: tag)).toList(),
        ),
      ],
    );
  }
}

class _PersonResultCard extends StatelessWidget {
  final String name, username, bio, avatarUrl;
  const _PersonResultCard({required this.name, required this.username, required this.bio, required this.avatarUrl});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16), side: BorderSide(color: theme.dividerColor.withValues(alpha: 0.1))),
      child: InkWell(
        onTap: () => HapticFeedback.lightImpact(),
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              NeuroAvatar(imageUrl: avatarUrl, name: name, size: 56),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                    Text('@$username', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                    const SizedBox(height: 4),
                    Text(bio, style: theme.textTheme.bodySmall, maxLines: 1, overflow: TextOverflow.ellipsis),
                  ],
                ),
              ),
              FilledButton(onPressed: () => HapticFeedback.lightImpact(), style: FilledButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 20)), child: Text(l10n.follow)),
            ],
          ),
        ),
      ),
    );
  }
}

class _TopicChip extends StatelessWidget {
  final String label;
  const _TopicChip({required this.label});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;
    return GestureDetector(
      onTap: () => HapticFeedback.selectionClick(),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(color: primaryColor.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(20), border: Border.all(color: primaryColor.withValues(alpha: 0.3))),
        child: Text(label, style: theme.textTheme.labelLarge?.copyWith(color: primaryColor, fontWeight: FontWeight.w600)),
      ),
    );
  }
}

class _RecentSearchesView extends StatelessWidget {
  final TextEditingController searchController;
  const _RecentSearchesView({required this.searchController});

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _SectionHeader(title: 'Recent Searches', icon: Icons.history_rounded, action: TextButton(onPressed: () => HapticFeedback.lightImpact(), child: const Text('Clear All'))),
          const SizedBox(height: 12),
          Wrap(spacing: 8, runSpacing: 8, children: ['ADHD tips', 'sensory friendly', 'autism community', 'mental health'].map((s) => _SearchChip(label: s, onTap: () { HapticFeedback.selectionClick(); searchController.text = s; })).toList()),
          const SizedBox(height: 32),
          _SectionHeader(title: 'Suggested Topics', icon: Icons.lightbulb_outline),
          const SizedBox(height: 12),
          _TopicsGrid(searchController: searchController),
          const SizedBox(height: 32),
          _SectionHeader(title: 'Trending Now', icon: Icons.local_fire_department_rounded),
          const SizedBox(height: 12),
          _TrendingList(searchController: searchController),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;
  final Widget? action;
  const _SectionHeader({required this.title, required this.icon, this.action});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;
    final tertiaryColor = theme.colorScheme.tertiary;
    return Row(
      children: [
        Container(width: 4, height: 20, decoration: BoxDecoration(gradient: LinearGradient(colors: [primaryColor, tertiaryColor], begin: Alignment.topCenter, end: Alignment.bottomCenter), borderRadius: BorderRadius.circular(2))),
        const SizedBox(width: 10),
        Icon(icon, size: 20, color: primaryColor),
        const SizedBox(width: 8),
        Text(title, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
        const Spacer(),
        ?action,
      ],
    );
  }
}

class _SearchChip extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  const _SearchChip({required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: theme.colorScheme.surfaceContainerHighest,
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Padding(padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10), child: Row(mainAxisSize: MainAxisSize.min, children: [Icon(Icons.history, size: 16, color: theme.colorScheme.onSurfaceVariant), const SizedBox(width: 8), Text(label, style: theme.textTheme.bodyMedium)])),
      ),
    );
  }
}

class _TopicsGrid extends StatelessWidget {
  final TextEditingController searchController;
  const _TopicsGrid({required this.searchController});

  @override
  Widget build(BuildContext context) {
    final topics = [
      _TopicData('ADHD', Icons.bolt_rounded, AppColors.categoryADHD, '12.5K'),
      _TopicData('Autism', Icons.hub_rounded, AppColors.categoryAutism, '9.8K'),
      _TopicData('Anxiety', Icons.psychology_rounded, AppColors.categoryAnxiety, '15.2K'),
      _TopicData('Depression', Icons.cloud_rounded, AppColors.categoryDepression, '11.3K'),
      _TopicData('Self Care', Icons.spa_rounded, AppColors.calmGreen, '8.7K'),
      _TopicData('Sensory', Icons.hearing_rounded, AppColors.calmBlue, '6.4K'),
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 2, childAspectRatio: 1.8, crossAxisSpacing: 12, mainAxisSpacing: 12),
      itemCount: topics.length,
      itemBuilder: (context, index) => _TopicCard(topic: topics[index], onTap: () { HapticFeedback.selectionClick(); searchController.text = topics[index].name; }, animationDelay: index * 50),
    );
  }
}

class _TopicData {
  final String name;
  final IconData icon;
  final Color color;
  final String postCount;
  const _TopicData(this.name, this.icon, this.color, this.postCount);
}

class _TopicCard extends StatefulWidget {
  final _TopicData topic;
  final VoidCallback onTap;
  final int animationDelay;
  const _TopicCard({required this.topic, required this.onTap, required this.animationDelay});

  @override
  State<_TopicCard> createState() => _TopicCardState();
}

class _TopicCardState extends State<_TopicCard> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation, _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(duration: const Duration(milliseconds: 400), vsync: this);
    _scaleAnimation = Tween<double>(begin: 0.8, end: 1.0).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutBack));
    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOut));
    Future.delayed(Duration(milliseconds: widget.animationDelay), () { if (mounted) _controller.forward(); });
  }

  @override
  void dispose() { _controller.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return FadeTransition(
      opacity: _fadeAnimation,
      child: ScaleTransition(
        scale: _scaleAnimation,
        child: Material(
          color: widget.topic.color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(16),
          child: InkWell(
            onTap: widget.onTap,
            borderRadius: BorderRadius.circular(16),
            child: Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(borderRadius: BorderRadius.circular(16), border: Border.all(color: widget.topic.color.withValues(alpha: 0.3))),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(children: [
                    Container(padding: const EdgeInsets.all(6), decoration: BoxDecoration(color: widget.topic.color.withValues(alpha: 0.2), borderRadius: BorderRadius.circular(8)), child: Icon(widget.topic.icon, color: widget.topic.color, size: 18)),
                    const Spacer(),
                    Icon(Icons.arrow_forward_ios_rounded, size: 12, color: widget.topic.color.withValues(alpha: 0.5)),
                  ]),
                  const Spacer(),
                  Text(widget.topic.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold, color: widget.topic.color)),
                  Text('${widget.topic.postCount} posts', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _TrendingList extends StatelessWidget {
  final TextEditingController searchController;
  const _TrendingList({required this.searchController});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final trending = [('#NeuroPositivity', '2.4K today', Icons.favorite_rounded), ('#SensoryFriendly', '1.8K today', Icons.hearing_rounded), ('#ADHDHacks', '3.1K today', Icons.tips_and_updates_rounded), ('#MindfulMoments', '956 today', Icons.self_improvement_rounded)];

    return Column(
      children: trending.asMap().entries.map((e) {
        final i = e.key;
        final item = e.value;
        final primaryColor = theme.colorScheme.primary;
        final tertiaryColor = theme.colorScheme.tertiary;
        return TweenAnimationBuilder<double>(
          tween: Tween(begin: 0.0, end: 1.0),
          duration: Duration(milliseconds: 300 + (i * 100)),
          curve: Curves.easeOutCubic,
          builder: (ctx, v, child) => Opacity(opacity: v, child: Transform.translate(offset: Offset(20 * (1 - v), 0), child: child)),
          child: Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Material(
              color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
              borderRadius: BorderRadius.circular(12),
              child: InkWell(
                onTap: () { HapticFeedback.selectionClick(); searchController.text = item.$1; },
                borderRadius: BorderRadius.circular(12),
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Row(children: [
                    Container(width: 40, height: 40, decoration: BoxDecoration(gradient: LinearGradient(colors: [primaryColor.withValues(alpha: 0.2), tertiaryColor.withValues(alpha: 0.2)]), borderRadius: BorderRadius.circular(10)), child: Center(child: Text('#${i + 1}', style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold, color: primaryColor)))),
                    const SizedBox(width: 12),
                    Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(item.$1, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)), Text(item.$2, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant))])),
                    Icon(item.$3, color: primaryColor, size: 20),
                  ]),
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

// Tab Content - Enhanced with Rich Mock Data
class _ForYouTab extends StatefulWidget {
  const _ForYouTab();

  @override
  State<_ForYouTab> createState() => _ForYouTabState();
}

class _ForYouTabState extends State<_ForYouTab> {
  String? _selectedCategory;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // Rich mock posts for "For You" feed
    final allForYouPosts = [
      _MockPost(
        id: 1,
        username: 'HyperFocusCode',
        displayName: 'Alex Chen',
        avatar: 'https://i.pravatar.cc/150?u=hyperfocuscode',
        content: 'Just discovered that setting 3 alarms for everything actually works! 🎯 First alarm: "Hey, you should start thinking about this". Second: "Okay seriously, transition time". Third: "DO IT NOW" 😅',
        likes: 2847,
        comments: 342,
        shares: 156,
        timeAgo: '2h',
        isLiked: false,
        isVerified: true,
        imageUrl: null,
        tags: ['ADHDHacks', 'ExecutiveFunction'],
        locationTag: 'San Francisco, CA',
      ),
      _MockPost(
        id: 2,
        username: 'SensorySeeker',
        displayName: 'Jordan Rivera',
        avatar: 'https://i.pravatar.cc/150?u=sensoryseeker',
        content: 'My new weighted blanket arrived! 15lbs of pure comfort 💙 Already feeling more grounded. For anyone curious - the pressure really does help with anxiety and sleep!',
        likes: 1923,
        comments: 187,
        shares: 89,
        timeAgo: '4h',
        isLiked: true,
        isVerified: false,
        imageUrl: 'https://picsum.photos/seed/blanket/800/600',
        tags: ['SensoryFriendly', 'AnxietyRelief'],
        locationTag: 'Portland, OR',
      ),
      _MockPost(
        id: 3,
        username: 'NeuroNurse',
        displayName: 'Dr. Sam Kim',
        avatar: 'https://i.pravatar.cc/150?u=neuronurse',
        content: '🧠 Quick reminder: Your brain isn\'t broken - it\'s just wired differently. What society calls "deficits" are often just different ways of processing the world. Embrace your unique neurology! 💜',
        likes: 5621,
        comments: 428,
        shares: 892,
        timeAgo: '6h',
        isLiked: false,
        isVerified: true,
        imageUrl: null,
        tags: ['Neurodiversity', 'SelfAcceptance'],
      ),
      _MockPost(
        id: 4,
        username: 'QuietQueen',
        displayName: 'Maya Thompson',
        avatar: 'https://i.pravatar.cc/150?u=quietqueen',
        content: 'Created a "sensory kit" for my desk at work! Includes: fidget cube, noise-canceling earbuds, lavender roller, chewy snacks, and a small plant 🌱 Game changer for overstimulating days!',
        likes: 3156,
        comments: 276,
        shares: 234,
        timeAgo: '8h',
        isLiked: true,
        isVerified: false,
        imageUrl: 'https://picsum.photos/seed/sensorykit/800/800',
        tags: ['SensoryKit', 'WorkplaceAccommodations'],
        locationTag: 'Austin, TX',
      ),
      _MockPost(
        id: 5,
        username: 'FocusFounder',
        displayName: 'Chris Lee',
        avatar: 'https://i.pravatar.cc/150?u=focusfounder',
        content: 'Body doubling session starting in 30 mins! Join me for 2 hours of focused work. No talking, just vibes and productivity. Link in bio! 💪✨',
        likes: 892,
        comments: 67,
        shares: 45,
        timeAgo: '10h',
        isLiked: false,
        isVerified: true,
        imageUrl: null,
        tags: ['BodyDoubling', 'ADHDCommunity'],
      ),
      _MockPost(
        id: 6,
        username: 'CalmCoach',
        displayName: 'Priya Desai',
        avatar: 'https://i.pravatar.cc/150?u=calmcoach',
        content: '🧘 5-minute grounding exercise that saved me today:\n\n5 things you see\n4 things you hear\n3 things you touch\n2 things you smell\n1 thing you taste\n\nBookmark this for anxious moments 💙',
        likes: 4312,
        comments: 512,
        shares: 1245,
        timeAgo: '3h',
        isLiked: false,
        isVerified: true,
        imageUrl: null,
        tags: ['Mindfulness', 'AnxietyRelief', 'SelfCare'],
      ),
      _MockPost(
        id: 7,
        username: 'SpectrumArtist',
        displayName: 'Liam Park',
        avatar: 'https://i.pravatar.cc/150?u=spectrumartist',
        content: 'Turned my special interest into a career! 🎨 Three years ago I was told my "obsessive" attention to detail was a weakness. Now I\'m a UX designer and it\'s my superpower. Keep going, friends!',
        likes: 7893,
        comments: 634,
        shares: 1567,
        timeAgo: '5h',
        isLiked: true,
        isVerified: false,
        imageUrl: 'https://picsum.photos/seed/artwork_ux/800/600',
        tags: ['AutismLife', 'SpecialInterests', 'Neurodiversity'],
        locationTag: 'Brooklyn, NY',
      ),
      _MockPost(
        id: 8,
        username: 'SleepNerd',
        displayName: 'Tasha Williams',
        avatar: 'https://i.pravatar.cc/150?u=sleepnerd',
        content: 'Game-changing sleep routine for ADHD brains 😴\n\n1. Blue-light glasses at 8pm\n2. Body scan meditation\n3. Weighted blanket\n4. Brown noise on low\n5. No screens in bed (hardest part!)\n\nFinally sleeping before midnight 🎉',
        likes: 3456,
        comments: 289,
        shares: 876,
        timeAgo: '7h',
        isLiked: false,
        isVerified: false,
        imageUrl: null,
        tags: ['SleepTips', 'ADHDLife', 'SensoryFriendly'],
      ),
      _MockPost(
        id: 9,
        username: 'NeuroDad',
        displayName: 'Marcus Johnson',
        avatar: 'https://i.pravatar.cc/150?u=neurodad',
        content: 'My 8yo was diagnosed with ADHD last month. Instead of grief, I felt relief — finally understanding HER means I finally understand ME. Getting diagnosed together has been the most beautiful, healing experience. 🥺💛',
        likes: 12480,
        comments: 1102,
        shares: 3200,
        timeAgo: '9h',
        isLiked: true,
        isVerified: false,
        imageUrl: null,
        tags: ['ADHDDiagnosis', 'Parenting', 'SelfAcceptance'],
        locationTag: 'Denver, CO',
      ),
      _MockPost(
        id: 10,
        username: 'StimQueen',
        displayName: 'Zoe Martinez',
        avatar: 'https://i.pravatar.cc/150?u=stimqueen',
        content: 'Fidget toy tier list 2026 edition! 🏆\n\nS-tier: Infinity cube, magnetic rings\nA-tier: Tangle, textured stones\nB-tier: Pop-its, squishy balls\nC-tier: Spinner rings\n\nFight me in the comments 😂',
        likes: 6234,
        comments: 1847,
        shares: 423,
        timeAgo: '11h',
        isLiked: false,
        isVerified: true,
        imageUrl: 'https://picsum.photos/seed/fidgets/800/800',
        tags: ['Stimming', 'FidgetToys', 'SensoryFriendly'],
      ),
    ];

    // Filter posts based on category
    final forYouPosts = _selectedCategory == null || _selectedCategory == '✨ For You' 
        ? allForYouPosts 
        : allForYouPosts.where((post) {
            // Very simple filtering logic for mock data
            final query = _selectedCategory!.toLowerCase();
            if (query.contains('adhd')) return post.tags.any((t) => t.toLowerCase().contains('adhd') || t.toLowerCase().contains('executive'));
            if (query.contains('sensory') || query.contains('support')) return post.tags.any((t) => t.toLowerCase().contains('sensory'));
            if (query.contains('following')) return post.isVerified; // Just mock logic
            if (query.contains('wins')) return post.likes > 2000;
            if (query.contains('trending')) return post.likes > 2500;
            if (query.contains('mindfulness')) return post.tags.any((t) => t.toLowerCase().contains('mind') || t.toLowerCase().contains('self'));
            if (query.contains('anxiety')) return post.tags.any((t) => t.toLowerCase().contains('anxiety') || t.toLowerCase().contains('sensory'));
            if (query.contains('autism')) return post.tags.any((t) => t.toLowerCase().contains('autism') || t.toLowerCase().contains('neuro'));
            if (query.contains('sleep')) return post.content.toLowerCase().contains('sleep') || post.tags.any((t) => t.toLowerCase().contains('sensory'));
            return false; // hide if it doesn't match
          }).toList();

    return RefreshIndicator(
      onRefresh: () async => HapticFeedback.mediumImpact(),
      color: theme.colorScheme.primary,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 100),
        children: [
          // Stories Row
          _SectionHeader(title: 'Stories', icon: Icons.auto_awesome_rounded),
          const SizedBox(height: 12),
          const _StoriesRow(),
          const SizedBox(height: 24),

          // Quick Access
          _SectionHeader(title: 'Quick Access', icon: Icons.bolt_rounded),
          const SizedBox(height: 12),
          _QuickAccessChips(
            selectedCategory: _selectedCategory,
            onSelected: (cat) {
              setState(() {
                _selectedCategory = cat;
              });
            },
          ),
          const SizedBox(height: 24),

          // For You Posts
          _SectionHeader(title: 'Curated For You', icon: Icons.favorite_rounded),
          const SizedBox(height: 12),

          if (forYouPosts.isEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 48),
              child: Center(
                child: Column(
                  children: [
                     Icon(Icons.search_off_rounded, size: 48, color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5)),
                     const SizedBox(height: 16),
                     Text('No posts found', style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant, fontWeight: FontWeight.bold)),
                     const SizedBox(height: 8),
                     Text('Try selecting a different filter', style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
              ),
            )
          else
            ...forYouPosts.map((post) => _ExplorePostCard(post: post)),
        ],
      ),
    );
  }
}

class _TrendingTab extends StatelessWidget {
  const _TrendingTab();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final trendingHashtags = [
      _TrendingHashtagData('#ADHDAwareness', '12.4K', 'posts today', true),
      _TrendingHashtagData('#NeurodiversityWeek', '8.7K', 'posts today', true),
      _TrendingHashtagData('#SensoryFriendly', '5.2K', 'posts today', false),
      _TrendingHashtagData('#MindfulMonday', '4.8K', 'posts today', false),
      _TrendingHashtagData('#AutismAcceptance', '3.9K', 'posts today', false),
      _TrendingHashtagData('#ExecutiveFunction', '2.1K', 'posts today', false),
    ];

    final viralPosts = [
      _MockPost(
        id: 101,
        username: 'ADHDMemes',
        displayName: 'ADHD Meme Central',
        avatar: 'https://i.pravatar.cc/150?u=adhdmemes',
        content: 'Me: I\'ll just check one thing real quick\n\n*3 hours later*\n\nMe: Wait what was I doing? 🤔😂',
        likes: 24521,
        comments: 1847,
        shares: 5621,
        timeAgo: '5h',
        isLiked: true,
        isVerified: true,
        imageUrl: 'https://picsum.photos/seed/meme1/800/800',
        tags: ['ADHD', 'Relatable'],
      ),
      _MockPost(
        id: 102,
        username: 'AutismAdvocate',
        displayName: 'Emma\'s Autism Journey',
        avatar: 'https://i.pravatar.cc/150?u=autismadvocate',
        content: '🧵 Thread: Things I wish people understood about autism masking:\n\n1. It\'s exhausting\n2. It\'s often unconscious\n3. "You don\'t look autistic" isn\'t a compliment\n4. We do it to survive, not to deceive\n\nPlease RT to spread awareness 💙',
        likes: 18934,
        comments: 2156,
        shares: 8742,
        timeAgo: '8h',
        isLiked: false,
        isVerified: true,
        imageUrl: null,
        tags: ['Autism', 'Masking', 'Awareness'],
      ),
      _MockPost(
        id: 103,
        username: 'TherapyTips',
        displayName: 'Dr. Mental Health',
        avatar: 'https://i.pravatar.cc/150?u=therapytips',
        content: '📊 Study just released: Neurodivergent individuals who found supportive communities reported 67% improvement in mental health outcomes.\n\nCommunity matters. You matter. 💜',
        likes: 15678,
        comments: 892,
        shares: 4521,
        timeAgo: '12h',
        isLiked: true,
        isVerified: true,
        imageUrl: null,
        tags: ['MentalHealth', 'Research', 'Community'],
      ),
      _MockPost(
        id: 104,
        username: 'ADHDHustle',
        displayName: 'Nina Lopez',
        avatar: 'https://i.pravatar.cc/150?u=adhdhustle',
        content: 'POV: You finally clean your room after 3 weeks and find:\n\n🔑 Your car keys (reported missing)\n📱 Old phone (still works)\n🧦 47 individual socks (no pairs)\n💊 3 half-empty water bottles\n📝 A to-do list from 2024\n\nADHD archaeology 🏺😭',
        likes: 31245,
        comments: 3421,
        shares: 9876,
        timeAgo: '3h',
        isLiked: false,
        isVerified: false,
        imageUrl: 'https://picsum.photos/seed/messy_room/800/600',
        tags: ['ADHDLife', 'Relatable', 'Humor'],
      ),
      _MockPost(
        id: 105,
        username: 'NeuroScience101',
        displayName: 'Dr. Raj Patel',
        avatar: 'https://i.pravatar.cc/150?u=neuroscience101',
        content: '🔬 Exciting research update!\n\nNew brain imaging study shows that ADHD brains have STRONGER connectivity in creativity networks. The same trait that makes focus hard also makes you incredibly innovative.\n\nYour brain isn\'t deficit — it\'s DIFFERENT. 🧠✨',
        likes: 28901,
        comments: 1567,
        shares: 12345,
        timeAgo: '6h',
        isLiked: true,
        isVerified: true,
        imageUrl: null,
        tags: ['Neuroscience', 'ADHD', 'Research'],
      ),
    ];

    return RefreshIndicator(
      onRefresh: () async => HapticFeedback.mediumImpact(),
      color: theme.colorScheme.primary,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 100),
        children: [
          // Trending Header with fire
          Row(
            children: [
              const Icon(Icons.local_fire_department_rounded, color: Color(0xFFFF6B35), size: 28),
              const SizedBox(width: 8),
              Text('Trending Now', style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 16),

          // Trending Hashtags
          _TrendingHashtagsSection(hashtags: trendingHashtags),
          const SizedBox(height: 24),

          // Viral Posts
          _SectionHeader(title: 'Going Viral', icon: Icons.whatshot_rounded),
          const SizedBox(height: 12),

          ...viralPosts.map((post) => _ExplorePostCard(post: post, showTrendingBadge: true)),
        ],
      ),
    );
  }
}

class _PeopleTab extends StatelessWidget {
  const _PeopleTab();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final featuredCreators = [
      _EnhancedPersonData(
        name: 'Dr. Sarah Chen',
        username: 'neuropsychologist',
        bio: 'Clinical psychologist specializing in ADHD & autism. Author of \'The Neurodivergent Mind\'. Here to spread knowledge & hope 🧠💜',
        avatar: 'https://i.pravatar.cc/150?u=sarahchen',
        isVerified: true,
        followers: '124K',
        mutualFollowers: 12,
        category: 'Professional',
      ),
      _EnhancedPersonData(
        name: 'Alex Rivera',
        username: 'adhd_alex',
        bio: 'ADHD advocate & content creator 🎯 Late-diagnosed at 28. Sharing my journey & tips that actually work!',
        avatar: 'https://i.pravatar.cc/150?u=alexrivera',
        isVerified: true,
        followers: '89.2K',
        mutualFollowers: 8,
        category: 'Creator',
      ),
      _EnhancedPersonData(
        name: 'Jordan Taylor',
        username: 'mindful_jordan',
        bio: 'Mindfulness coach for neurodivergent adults 🧘 Making meditation accessible for busy brains',
        avatar: 'https://i.pravatar.cc/150?u=jordantaylor',
        isVerified: false,
        followers: '45.6K',
        mutualFollowers: 5,
        category: 'Coach',
      ),
    ];

    final suggestedPeople = [
      _EnhancedPersonData(
        name: 'Sam Kim',
        username: 'sensory_sam',
        bio: 'Sensory processing tips & product reviews ✨ OT student & SPD advocate',
        avatar: 'https://i.pravatar.cc/150?u=samkim',
        isVerified: false,
        followers: '23.1K',
        mutualFollowers: 3,
        category: 'Advocate',
      ),
      _EnhancedPersonData(
        name: 'Chris Morgan',
        username: 'autism_chris',
        bio: 'Autism self-advocate & speaker 🌈 AAC user. Late-diagnosed. Proudly autistic!',
        avatar: 'https://i.pravatar.cc/150?u=chrismorgan',
        isVerified: true,
        followers: '67.8K',
        mutualFollowers: 15,
        category: 'Speaker',
      ),
      _EnhancedPersonData(
        name: 'Taylor Swift-Mind',
        username: 'adhd_swiftie',
        bio: 'Combining special interests: Taylor Swift + ADHD content 🎵 Making executive dysfunction eras fun!',
        avatar: 'https://i.pravatar.cc/150?u=adhdswiftie',
        isVerified: false,
        followers: '31.4K',
        mutualFollowers: 7,
        category: 'Entertainment',
      ),
      _EnhancedPersonData(
        name: 'Dr. Mike Therapy',
        username: 'mike_therapy',
        bio: 'Licensed therapist specializing in anxiety & neurodivergence 🎓 Free resources in bio!',
        avatar: 'https://i.pravatar.cc/150?u=miketherapy',
        isVerified: true,
        followers: '156K',
        mutualFollowers: 21,
        category: 'Professional',
      ),
      _EnhancedPersonData(
        name: 'Stim Queen',
        username: 'stim_queen',
        bio: 'Stimming is self-care! 💫 Fidget reviews & stim toy recommendations',
        avatar: 'https://i.pravatar.cc/150?u=stimqueen',
        isVerified: false,
        followers: '18.9K',
        mutualFollowers: 4,
        category: 'Reviews',
      ),
    ];

    return RefreshIndicator(
      onRefresh: () async => HapticFeedback.mediumImpact(),
      color: theme.colorScheme.primary,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
        children: [
          // Featured Creators Section
          _SectionHeader(title: 'Featured Creators', icon: Icons.star_rounded),
          const SizedBox(height: 12),
          SizedBox(
            height: 200,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: featuredCreators.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (context, index) => _FeaturedCreatorCard(creator: featuredCreators[index]),
            ),
          ),
          const SizedBox(height: 24),

          // Category filter chips
          const _PeopleCategoryChips(),
          const SizedBox(height: 16),

          // Suggested People
          _SectionHeader(title: 'Suggested For You', icon: Icons.person_add_rounded),
          const SizedBox(height: 12),

          ...suggestedPeople.asMap().entries.map((entry) {
            final index = entry.key;
            final person = entry.value;
            return _AnimatedListItem(
              index: index,
              child: _EnhancedPersonCard(person: person),
            );
          }),
        ],
      ),
    );
  }
}

class _TopicsTab extends StatelessWidget {
  const _TopicsTab();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final featuredTopics = [
      _FeaturedTopicData(
        name: 'ADHD Life',
        emoji: '🎯',
        description: 'Tips, memes, and support for living with ADHD',
        memberCount: '45.2K',
        postsToday: 892,
        gradient: const [Color(0xFF7C4DFF), Color(0xFF536DFE)],
        isJoined: true,
        isHot: true,
      ),
      _FeaturedTopicData(
        name: 'Autism Community',
        emoji: '🧩',
        description: 'A safe space for autistic individuals and allies',
        memberCount: '38.7K',
        postsToday: 654,
        gradient: const [Color(0xFF00BFA5), Color(0xFF1DE9B6)],
        isJoined: false,
        isHot: true,
      ),
      _FeaturedTopicData(
        name: 'Sensory World',
        emoji: '✨',
        description: 'Sensory processing, stims, and comfort tips',
        memberCount: '28.1K',
        postsToday: 423,
        gradient: const [Color(0xFFFF6B6B), Color(0xFFFFE66D)],
        isJoined: true,
        isHot: false,
      ),
    ];

    final categories = [
      _CategoryData('Mental Health', Icons.psychology_rounded, AppColors.categoryAnxiety, [
        _SimpleTopicData('Anxiety Support', '12.3K members', '💙'),
        _SimpleTopicData('Depression Recovery', '9.8K members', '🌱'),
        _SimpleTopicData('Therapy Talk', '15.6K members', '🗣️'),
        _SimpleTopicData('Mindfulness', '21.2K members', '🧘'),
      ]),
      _CategoryData('Neurodivergence', Icons.hub_rounded, AppColors.categoryAutism, [
        _SimpleTopicData('ADHD Tips', '34.5K members', '⚡'),
        _SimpleTopicData('Autism Life', '28.9K members', '🌈'),
        _SimpleTopicData('Dyslexia Support', '8.2K members', '📚'),
        _SimpleTopicData('Executive Function', '11.4K members', '🧠'),
      ]),
      _CategoryData('Self Care', Icons.spa_rounded, AppColors.calmGreen, [
        _SimpleTopicData('Sleep Routines', '18.7K members', '😴'),
        _SimpleTopicData('Movement & Exercise', '14.2K members', '🏃'),
        _SimpleTopicData('Nutrition Tips', '9.6K members', '🥗'),
        _SimpleTopicData('Hobby Corner', '22.1K members', '🎨'),
      ]),
      _CategoryData('Productivity', Icons.bolt_rounded, AppColors.categoryADHD, [
        _SimpleTopicData('Focus Hacks', '25.3K members', '🎯'),
        _SimpleTopicData('Body Doubling', '16.8K members', '👥'),
        _SimpleTopicData('Time Management', '19.4K members', '⏰'),
        _SimpleTopicData('Organization', '12.7K members', '📋'),
      ]),
    ];

    return RefreshIndicator(
      onRefresh: () async => HapticFeedback.mediumImpact(),
      color: theme.colorScheme.primary,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 100),
        children: [
          // Featured Topics
          _SectionHeader(title: 'Featured Topics', icon: Icons.local_fire_department_rounded),
          const SizedBox(height: 12),
          SizedBox(
            height: 170,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: featuredTopics.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (context, index) => _FeaturedTopicCard(topic: featuredTopics[index]),
            ),
          ),
          const SizedBox(height: 24),

          // Browse by Category
          _SectionHeader(title: 'Browse by Category', icon: Icons.category_rounded),
          const SizedBox(height: 12),

          ...categories.asMap().entries.map((entry) {
            final index = entry.key;
            final category = entry.value;
            return _AnimatedListItem(
              index: index,
              child: _TopicCategoryCard(category: category),
            );
          }),
        ],
      ),
    );
  }
}

class _CategoryData {
  final String name;
  final IconData icon;
  final Color color;
  final List<_SimpleTopicData> topics;
  const _CategoryData(this.name, this.icon, this.color, this.topics);
}

class _SimpleTopicData {
  final String name;
  final String memberCount;
  final String emoji;
  const _SimpleTopicData(this.name, this.memberCount, this.emoji);
}

// ============================================================================
// Mock Data Classes
// ============================================================================

class _MockPost {
  final int id;
  final String username;
  final String displayName;
  final String avatar;
  final String content;
  final int likes;
  final int comments;
  final int shares;
  final String timeAgo;
  final bool isLiked;
  final bool isVerified;
  final String? imageUrl;
  final List<String> tags;
  final String? locationTag;

  const _MockPost({
    required this.id,
    required this.username,
    required this.displayName,
    required this.avatar,
    required this.content,
    required this.likes,
    required this.comments,
    required this.shares,
    required this.timeAgo,
    required this.isLiked,
    required this.isVerified,
    this.imageUrl,
    this.tags = const [],
    this.locationTag,
  });
}

class _TrendingHashtagData {
  final String tag;
  final String count;
  final String subtitle;
  final bool isHot;
  const _TrendingHashtagData(this.tag, this.count, this.subtitle, this.isHot);
}

class _EnhancedPersonData {
  final String name;
  final String username;
  final String bio;
  final String avatar;
  final bool isVerified;
  final String followers;
  final int mutualFollowers;
  final String category;
  const _EnhancedPersonData({
    required this.name,
    required this.username,
    required this.bio,
    required this.avatar,
    required this.isVerified,
    required this.followers,
    required this.mutualFollowers,
    required this.category,
  });
}

class _FeaturedTopicData {
  final String name;
  final String emoji;
  final String description;
  final String memberCount;
  final int postsToday;
  final List<Color> gradient;
  final bool isJoined;
  final bool isHot;
  const _FeaturedTopicData({
    required this.name,
    required this.emoji,
    required this.description,
    required this.memberCount,
    required this.postsToday,
    required this.gradient,
    required this.isJoined,
    required this.isHot,
  });
}

// ============================================================================
// Stories Row Component
// ============================================================================

class _StoriesRow extends StatelessWidget {
  const _StoriesRow();

  @override
  Widget build(BuildContext context) {
    final stories = [
      _StoryData('Your Story', 'https://i.pravatar.cc/150?u=you', true, false),
      _StoryData('ADHDCoach', 'https://i.pravatar.cc/150?u=coach', false, true),
      _StoryData('SensoryTips', 'https://i.pravatar.cc/150?u=sensory', false, true),
      _StoryData('MindfulMom', 'https://i.pravatar.cc/150?u=mom', false, true),
      _StoryData('NeuroDoc', 'https://i.pravatar.cc/150?u=doc', false, false),
      _StoryData('FocusHacks', 'https://i.pravatar.cc/150?u=focus', false, true),
    ];

    return SizedBox(
      height: 95,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: stories.length,
        separatorBuilder: (_, __) => const SizedBox(width: 12),
        itemBuilder: (context, index) => _StoryItem(story: stories[index]),
      ),
    );
  }
}

class _StoryData {
  final String username;
  final String avatar;
  final bool isYourStory;
  final bool hasNewStory;
  const _StoryData(this.username, this.avatar, this.isYourStory, this.hasNewStory);
}

class _StoryItem extends StatelessWidget {
  final _StoryData story;
  const _StoryItem({required this.story});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: () => HapticFeedback.lightImpact(),
      child: SizedBox(
        width: 72,
        child: Column(
          children: [
            Stack(
              alignment: Alignment.center,
              children: [
                // Gradient ring
                if (story.hasNewStory && !story.isYourStory)
                  Container(
                    width: 68,
                    height: 68,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: LinearGradient(colors: [theme.colorScheme.primary, theme.colorScheme.tertiary]),
                    ),
                  )
                else if (!story.isYourStory)
                  Container(
                    width: 68,
                    height: 68,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: theme.colorScheme.outlineVariant,
                    ),
                  ),

                // Avatar
                Container(
                  width: 64,
                  height: 64,
                  padding: const EdgeInsets.all(2),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: theme.scaffoldBackgroundColor,
                  ),
                  child: ClipOval(
                    child: Image.network(story.avatar, fit: BoxFit.cover, errorBuilder: (_, __, ___) => Container(color: theme.colorScheme.primaryContainer)),
                  ),
                ),

                // Add button
                if (story.isYourStory)
                  Positioned(
                    right: 0,
                    bottom: 0,
                    child: Container(
                      width: 22,
                      height: 22,
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primary,
                        shape: BoxShape.circle,
                        border: Border.all(color: theme.scaffoldBackgroundColor, width: 2),
                      ),
                      child: const Icon(Icons.add, color: Colors.white, size: 14),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              story.isYourStory ? 'Add Story' : story.username,
              style: theme.textTheme.labelSmall,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
// Quick Access Chips
// ============================================================================

class _QuickAccessChips extends StatelessWidget {
  final String? selectedCategory;
  final ValueChanged<String>? onSelected;

  const _QuickAccessChips({this.selectedCategory, this.onSelected});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    
    // Quick access categories
    final chips = [
      ('✨ For You', isDark ? const Color(0xFFC099FF) : AppColors.primaryPurple, 0),
      ('👥 Following', AppColors.secondaryTeal, 1),
      ('🔥 Trending', AppColors.calmGreen, 2),
      ('🫂 Support', AppColors.calmBlue, 3),
      ('🎉 Wins', AppColors.categoryADHD, 4),
      ('🎯 ADHD Tips', AppColors.categoryADHD, null),
      ('🧘 Mindfulness', AppColors.calmGreen, null),
      ('💙 Anxiety', AppColors.calmBlue, null),
      ('🌈 Autism', AppColors.categoryAutism, null),
      ('😴 Sleep', AppColors.categoryAnxiety, null),
    ];

    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: chips.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final item = chips[index];
          final label = item.$1;
          final color = item.$2;
          
          final isSelected = selectedCategory == label;
          final displayColor = isSelected ? theme.colorScheme.onPrimary : color;
          final backgroundColor = isSelected ? color : theme.colorScheme.surfaceContainerHighest;

          return GestureDetector(
            onTap: () {
              HapticFeedback.selectionClick();
              if (onSelected != null) {
                // If clicking the already selected category, unselect it (null) unless it's basic "For You"
                onSelected!(isSelected ? '✨ For You' : label);
              }
            },
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: backgroundColor,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: isSelected ? color : color.withValues(alpha: 0.4),
                  width: isSelected ? 2 : 1,
                ),
                boxShadow: isSelected ? [
                  BoxShadow(
                    color: color.withValues(alpha: 0.3),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  )
                ] : null,
              ),
              child: Text(
                label,
                style: TextStyle(
                  color: displayColor,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

/// Explore Post Card
class _ExplorePostCard extends StatefulWidget {
  final _MockPost post;
  final bool showTrendingBadge;
  const _ExplorePostCard({required this.post, this.showTrendingBadge = false});

  @override
  State<_ExplorePostCard> createState() => _ExplorePostCardState();
}

class _ExplorePostCardState extends State<_ExplorePostCard> with AutomaticKeepAliveClientMixin {
  late bool _isLiked;
  late int _likeCount;
  bool _isSaved = false;

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    super.initState();
    _isLiked = widget.post.isLiked;
    _likeCount = widget.post.likes;
  }

  void _handleShare() async {
    try {
      final box = context.findRenderObject() as RenderBox?;
      final rect = box != null ? box.localToGlobal(Offset.zero) & box.size : null;
      await Share.share(
        'Check out this post by ${widget.post.displayName} on NeuroComet: "${widget.post.content.length > 100 ? '${widget.post.content.substring(0, 100)}...' : widget.post.content}"',
        sharePositionOrigin: rect,
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Unable to share post: $e'), behavior: SnackBarBehavior.floating),
        );
      }
    }
  }

  void _handleProfileView() {
    HapticFeedback.lightImpact();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Viewing ${widget.post.displayName}\'s Profile...'), behavior: SnackBarBehavior.floating),
    );
  }

  String _formatCount(int count) {
    if (count >= 1000000) return '${(count / 1000000).toStringAsFixed(1)}M';
    if (count >= 1000) return '${(count / 1000).toStringAsFixed(1)}K';
    return count.toString();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final theme = Theme.of(context);
    final post = widget.post;

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                // Avatar with gradient ring
                GestureDetector(
                  onTap: _handleProfileView,
                  child: Container(
                    padding: const EdgeInsets.all(2),
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: LinearGradient(colors: [theme.colorScheme.primary, theme.colorScheme.tertiary]),
                    ),
                    child: CircleAvatar(
                      radius: 22,
                      backgroundImage: NetworkImage(post.avatar),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: GestureDetector(
                    onTap: _handleProfileView,
                    behavior: HitTestBehavior.opaque,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Text(post.displayName, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                            if (post.isVerified) ...[
                              const SizedBox(width: 4),
                              Icon(Icons.verified, size: 16, color: theme.colorScheme.primary),
                            ],
                          ],
                        ),
                        Text('@${post.username} • ${post.timeAgo}', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                        if (post.locationTag != null) ...[
                          const SizedBox(height: 2),
                          Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.location_on_outlined, size: 13, color: theme.colorScheme.onSurfaceVariant),
                              const SizedBox(width: 2),
                              Text(post.locationTag!, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant, fontSize: 11)),
                            ],
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
                if (widget.showTrendingBadge)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFF6B35).withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.local_fire_department, size: 14, color: Color(0xFFFF6B35)),
                        const SizedBox(width: 4),
                        Text('Viral', style: theme.textTheme.labelSmall?.copyWith(color: const Color(0xFFFF6B35), fontWeight: FontWeight.bold)),
                      ],
                    ),
                  ),
                IconButton(
                  icon: Icon(Icons.more_horiz, color: theme.colorScheme.onSurfaceVariant),
                  onPressed: () {
                    showModalBottomSheet(
                      context: context,
                      builder: (ctx) => SafeArea(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            ListTile(
                              leading: Icon(_isSaved ? Icons.bookmark : Icons.bookmark_outline),
                              title: Text(_isSaved ? 'Remove from Saved' : 'Save Post'),
                              onTap: () {
                                Navigator.pop(ctx);
                                setState(() => _isSaved = !_isSaved);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(content: Text(_isSaved ? 'Post saved!' : 'Post removed from saves.'), behavior: SnackBarBehavior.floating),
                                );
                              },
                            ),
                            ListTile(
                              leading: const Icon(Icons.share_outlined),
                              title: const Text('Share Post'),
                              onTap: () {
                                Navigator.pop(ctx);
                                _handleShare();
                              },
                            ),
                            ListTile(
                              leading: const Icon(Icons.person_outline),
                              title: Text('View ${post.displayName}\'s Profile'),
                              onTap: () {
                                Navigator.pop(ctx);
                                _handleProfileView();
                              },
                            ),
                            ListTile(
                              leading: Icon(Icons.flag_outlined, color: theme.colorScheme.error),
                              title: Text('Report', style: TextStyle(color: theme.colorScheme.error)),
                              onTap: () {
                                Navigator.pop(ctx);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(content: Text('Report submitted. Thank you.'), behavior: SnackBarBehavior.floating),
                                );
                              },
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Content
            Text(post.content, style: theme.textTheme.bodyMedium?.copyWith(height: 1.5)),

            // Tags
            if (post.tags.isNotEmpty) ...[
              const SizedBox(height: 8),
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: post.tags.take(3).map((tag) => GestureDetector(
                  onTap: () {
                    HapticFeedback.lightImpact();
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Exploring #$tag...'), behavior: SnackBarBehavior.floating));
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.primaryContainer.withValues(alpha: 0.5),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Text('#$tag', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onPrimaryContainer, fontWeight: FontWeight.w600)),
                  ),
                )).toList(),
              ),
            ],

            // Image
            if (post.imageUrl != null) ...[
              const SizedBox(height: 12),
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.network(
                  post.imageUrl!,
                  height: 200,
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(height: 200, color: theme.colorScheme.surfaceContainerHighest),
                ),
              ),
            ],
            const SizedBox(height: 12),

            // Actions
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                // Like
                GestureDetector(
                  onTap: () {
                    HapticFeedback.lightImpact();
                    setState(() {
                      _isLiked = !_isLiked;
                      _likeCount = _isLiked ? _likeCount + 1 : _likeCount - 1;
                    });
                  },
                  behavior: HitTestBehavior.opaque,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                    child: Row(
                      children: [
                        Icon(_isLiked ? Icons.favorite : Icons.favorite_border, size: 22, color: _isLiked ? const Color(0xFFE91E63) : theme.colorScheme.onSurfaceVariant),
                        const SizedBox(width: 6),
                        Text(_formatCount(_likeCount), style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                      ],
                    ),
                  ),
                ),
                // Comment
                GestureDetector(
                  onTap: () {
                    HapticFeedback.lightImpact();
                    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Comments feature coming soon!'), behavior: SnackBarBehavior.floating));
                  },
                  behavior: HitTestBehavior.opaque,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                    child: Row(
                      children: [
                        Icon(Icons.chat_bubble_outline, size: 22, color: theme.colorScheme.onSurfaceVariant),
                        const SizedBox(width: 6),
                        Text(_formatCount(post.comments), style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                      ],
                    ),
                  ),
                ),
                // Share
                GestureDetector(
                  onTap: () {
                    HapticFeedback.lightImpact();
                    _handleShare();
                  },
                  behavior: HitTestBehavior.opaque,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                    child: Row(
                      children: [
                        Icon(Icons.share_outlined, size: 22, color: theme.colorScheme.onSurfaceVariant),
                        const SizedBox(width: 6),
                        Text(_formatCount(post.shares), style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                      ],
                    ),
                  ),
                ),
                // Bookmark
                GestureDetector(
                  onTap: () {
                    HapticFeedback.lightImpact();
                    setState(() => _isSaved = !_isSaved);
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text(_isSaved ? 'Post saved!' : 'Post removed from saves.'), behavior: SnackBarBehavior.floating),
                    );
                  },
                  behavior: HitTestBehavior.opaque,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                    child: Icon(_isSaved ? Icons.bookmark : Icons.bookmark_border, size: 22, color: _isSaved ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
// Trending Hashtags Section
// ============================================================================

class _TrendingHashtagsSection extends StatelessWidget {
  final List<_TrendingHashtagData> hashtags;
  const _TrendingHashtagsSection({required this.hashtags});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: hashtags.asMap().entries.map((entry) {
        final index = entry.key;
        final hashtag = entry.value;

        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: () => HapticFeedback.selectionClick(),
              borderRadius: BorderRadius.circular(12),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
                child: Row(
                  children: [
                    // Rank
                    SizedBox(
                      width: 28,
                      child: Text(
                        '${index + 1}',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: index < 3 ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Text(hashtag.tag, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                              if (hashtag.isHot) ...[
                                const SizedBox(width: 6),
                                const Icon(Icons.local_fire_department, size: 16, color: Color(0xFFFF6B35)),
                              ],
                            ],
                          ),
                          Text('${hashtag.count} ${hashtag.subtitle}', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                        ],
                      ),
                    ),
                    Icon(Icons.trending_up_rounded, size: 20, color: AppColors.calmGreen),
                  ],
                ),
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

// ============================================================================
// Featured Creator Card
// ============================================================================

class _FeaturedCreatorCard extends StatelessWidget {
  final _EnhancedPersonData creator;
  const _FeaturedCreatorCard({required this.creator});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      width: 280,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(radius: 28, backgroundImage: NetworkImage(creator.avatar)),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(child: Text(creator.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold), maxLines: 1, overflow: TextOverflow.ellipsis)),
                        if (creator.isVerified) ...[const SizedBox(width: 4), Icon(Icons.verified, size: 14, color: theme.colorScheme.primary)],
                      ],
                    ),
                    Text('@${creator.username}', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(creator.bio, style: theme.textTheme.bodySmall, maxLines: 2, overflow: TextOverflow.ellipsis),
          const Spacer(),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(creator.followers, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                  Text('followers', style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                ],
              ),
              FilledButton(
                onPressed: () => HapticFeedback.lightImpact(),
                style: FilledButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20))),
                child: const Text('Follow'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

// ============================================================================
// People Category Chips
// ============================================================================

class _PeopleCategoryChips extends StatefulWidget {
  const _PeopleCategoryChips();

  @override
  State<_PeopleCategoryChips> createState() => _PeopleCategoryChipsState();
}

class _PeopleCategoryChipsState extends State<_PeopleCategoryChips> {
  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    final categories = ['All', 'Professionals', 'Creators', 'Coaches', 'Advocates'];

    return SizedBox(
      height: 36,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: categories.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final isSelected = _selectedIndex == index;
          return GestureDetector(
            onTap: () {
              HapticFeedback.selectionClick();
              setState(() => _selectedIndex = index);
            },
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: isSelected ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                categories[index],
                style: TextStyle(
                  color: isSelected ? Theme.of(context).colorScheme.onPrimary : Theme.of(context).colorScheme.onSurface,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

// ============================================================================
// Enhanced Person Card
// ============================================================================

class _EnhancedPersonCard extends StatefulWidget {
  final _EnhancedPersonData person;
  const _EnhancedPersonCard({required this.person});

  @override
  State<_EnhancedPersonCard> createState() => _EnhancedPersonCardState();
}

class _EnhancedPersonCardState extends State<_EnhancedPersonCard> {
  bool _isFollowing = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final person = widget.person;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        onTap: () => HapticFeedback.lightImpact(),
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              CircleAvatar(radius: 28, backgroundImage: NetworkImage(person.avatar)),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(person.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                        if (person.isVerified) ...[const SizedBox(width: 4), Icon(Icons.verified, size: 16, color: theme.colorScheme.primary)],
                      ],
                    ),
                    Text('@${person.username}', style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                    const SizedBox(height: 4),
                    Text(person.bio, style: theme.textTheme.bodySmall, maxLines: 2, overflow: TextOverflow.ellipsis),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Text('${person.followers} followers', style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                        if (person.mutualFollowers > 0)
                          Text(' • ${person.mutualFollowers} mutual', style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.primary)),
                      ],
                    ),
                  ],
                ),
              ),
              _isFollowing
                  ? OutlinedButton(
                      onPressed: () {
                        HapticFeedback.lightImpact();
                        setState(() => _isFollowing = false);
                      },
                      style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8)),
                      child: const Text('Following'),
                    )
                  : FilledButton(
                      onPressed: () {
                        HapticFeedback.lightImpact();
                        setState(() => _isFollowing = true);
                      },
                      style: FilledButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8)),
                      child: const Text('Follow'),
                    ),
            ],
          ),
        ),
      ),
    );
  }
}

// ============================================================================
// Featured Topic Card
// ============================================================================

class _FeaturedTopicCard extends StatelessWidget {
  final _FeaturedTopicData topic;
  const _FeaturedTopicCard({required this.topic});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => HapticFeedback.lightImpact(),
      child: Container(
        width: 260,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          gradient: LinearGradient(colors: topic.gradient),
          borderRadius: BorderRadius.circular(20),
          boxShadow: [BoxShadow(color: topic.gradient.first.withValues(alpha: 0.3), blurRadius: 12, offset: const Offset(0, 4))],
        ),
        child: Stack(
          children: [
            // Large emoji decoration
            Positioned(
              top: -10,
              right: -10,
              child: Text(topic.emoji, style: TextStyle(fontSize: 64, color: Colors.white.withValues(alpha: 0.25))),
            ),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Badges
                Row(
                  children: [
                    if (topic.isHot)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.2), borderRadius: BorderRadius.circular(12)),
                        child: const Text('🔥 Hot', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                      ),
                    if (topic.isHot && topic.isJoined) const SizedBox(width: 6),
                    if (topic.isJoined)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.2), borderRadius: BorderRadius.circular(12)),
                        child: const Text('✓ Joined', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                      ),
                  ],
                ),
                const Spacer(),
                Row(
                  children: [
                    Text(topic.emoji, style: const TextStyle(fontSize: 24)),
                    const SizedBox(width: 8),
                    Text(topic.name, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                  ],
                ),
                const SizedBox(height: 4),
                Text(topic.description, style: TextStyle(color: Colors.white.withValues(alpha: 0.9), fontSize: 13), maxLines: 1, overflow: TextOverflow.ellipsis),
                const SizedBox(height: 8),
                Row(
                  children: [
                    Text('${topic.memberCount} members', style: TextStyle(color: Colors.white.withValues(alpha: 0.8), fontSize: 12)),
                    const SizedBox(width: 16),
                    Text('${topic.postsToday} posts today', style: TextStyle(color: Colors.white.withValues(alpha: 0.8), fontSize: 12)),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ============================================================================
// Topic Category Card
// ============================================================================

class _TopicCategoryCard extends StatelessWidget {
  final _CategoryData category;
  const _TopicCategoryCard({required this.category});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(color: category.color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(10)),
                  child: Icon(category.icon, color: category.color, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(category.name, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                ),
                TextButton(onPressed: () => HapticFeedback.lightImpact(), child: const Text('See all')),
              ],
            ),
            const SizedBox(height: 12),
            // Topic chips
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: category.topics.map((topic) {
                return GestureDetector(
                  onTap: () => HapticFeedback.selectionClick(),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.surfaceContainerHighest,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(topic.emoji, style: const TextStyle(fontSize: 14)),
                        const SizedBox(width: 6),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(topic.name, style: theme.textTheme.labelMedium?.copyWith(fontWeight: FontWeight.w500)),
                            Text(topic.memberCount, style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }
}


class _AnimatedListItem extends StatefulWidget {
  final int index;
  final Widget child;
  const _AnimatedListItem({required this.index, required this.child});

  @override
  State<_AnimatedListItem> createState() => _AnimatedListItemState();
}

class _AnimatedListItemState extends State<_AnimatedListItem> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<Offset> _slideAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(duration: const Duration(milliseconds: 400), vsync: this);
    _fadeAnimation = CurvedAnimation(parent: _controller, curve: Curves.easeOut);
    _slideAnimation = Tween<Offset>(begin: const Offset(0, 0.1), end: Offset.zero).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutCubic));
    Future.delayed(Duration(milliseconds: widget.index * 50), () { if (mounted) _controller.forward(); });
  }

  @override
  void dispose() { _controller.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) => FadeTransition(opacity: _fadeAnimation, child: SlideTransition(position: _slideAnimation, child: widget.child));
}


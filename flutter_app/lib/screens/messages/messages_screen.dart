import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/conversation.dart';
import '../../providers/messages_provider.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';

/// Message tab filter options
enum MessageFilter {
  all,
  primary,
  requests,
}

class MessagesScreen extends ConsumerStatefulWidget {
  const MessagesScreen({super.key});

  @override
  ConsumerState<MessagesScreen> createState() => _MessagesScreenState();
}

class _MessagesScreenState extends ConsumerState<MessagesScreen>
    with TickerProviderStateMixin {
  MessageFilter _selectedFilter = MessageFilter.all;
  final TextEditingController _searchController = TextEditingController();
  bool _isSearching = false;
  late AnimationController _headerAnimationController;
  late Animation<double> _headerFadeAnimation;

  @override
  void initState() {
    super.initState();
    _headerAnimationController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _headerFadeAnimation = CurvedAnimation(
      parent: _headerAnimationController,
      curve: Curves.easeOutCubic,
    );
    _headerAnimationController.forward();
  }

  @override
  void dispose() {
    _headerAnimationController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final messagesState = ref.watch(conversationsProvider);
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final isTablet = MediaQuery.of(context).size.shortestSide >= 600;

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: messagesState.when(
          loading: () => const NeuroLoading(message: 'Loading messages...'),
          error: (error, stack) => _buildErrorState(error.toString()),
          data: (conversations) => isTablet
              ? _buildTabletLayout(context, conversations, l10n, theme)
              : _buildContent(context, conversations, l10n),
        ),
      ),
    );
  }

  /// Two-pane adaptive layout for tablets
  Widget _buildTabletLayout(
    BuildContext context,
    List<Conversation> conversations,
    AppLocalizations l10n,
    ThemeData theme,
  ) {
    return Row(
      children: [
        // Conversation list pane (40% width)
        SizedBox(
          width: MediaQuery.of(context).size.width * 0.4,
          child: _buildContent(context, conversations, l10n),
        ),
        // Divider
        VerticalDivider(
          width: 1,
          color: theme.dividerColor,
        ),
        // Chat detail pane (60% width)
        Expanded(
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: AppColors.primaryPurple.withAlpha(20),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.chat_bubble_outline,
                    size: 48,
                    color: AppColors.primaryPurple,
                  ),
                ),
                const SizedBox(height: 16),
                Text(
                  'Select a conversation',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Choose a conversation to start messaging',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildContent(
    BuildContext context,
    List<Conversation> conversations,
    AppLocalizations l10n,
  ) {
    final unreadCount = conversations.fold<int>(
      0, (sum, c) => sum + c.unreadCount);
    final filteredConversations = _filterConversations(conversations);

    return CustomScrollView(
      physics: const BouncingScrollPhysics(
        parent: AlwaysScrollableScrollPhysics(),
      ),
      slivers: [
        // Custom header matching Notifications style
        SliverToBoxAdapter(
          child: FadeTransition(
            opacity: _headerFadeAnimation,
            child: _MessagesHeader(
              unreadCount: unreadCount,
              onNewMessage: () => _showNewMessageSheet(context),
              onSearch: () => setState(() => _isSearching = true),
              onPracticeCall: () => context.push('/practice-calls'),
              l10n: l10n,
            ),
          ),
        ),

        // Search bar (animated)
        SliverToBoxAdapter(
          child: AnimatedSize(
            duration: const Duration(milliseconds: 200),
            child: _isSearching
                ? _SearchBar(
                    controller: _searchController,
                    onClose: () {
                      setState(() {
                        _isSearching = false;
                        _searchController.clear();
                      });
                    },
                  )
                : const SizedBox.shrink(),
          ),
        ),

        // Filter pills matching Notifications style
        SliverToBoxAdapter(
          child: _FilterChipsSection(
            selectedFilter: _selectedFilter,
            unreadCount: unreadCount,
            onFilterChanged: (filter) {
              HapticFeedback.selectionClick();
              setState(() => _selectedFilter = filter);
            },
          ),
        ),

        // Content
        if (filteredConversations.isEmpty)
          SliverFillRemaining(
            hasScrollBody: false,
            child: _buildEmptyState(),
          )
        else
          SliverPadding(
            padding: const EdgeInsets.only(bottom: 100),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  final conversation = filteredConversations[index];
                  return _ConversationTile(
                    conversation: conversation,
                    onTap: () => _openConversation(conversation),
                    onLongPress: () => _showConversationOptions(conversation),
                  );
                },
                childCount: filteredConversations.length,
              ),
            ),
          ),
      ],
    );
  }

  List<Conversation> _filterConversations(List<Conversation> conversations) {
    switch (_selectedFilter) {
      case MessageFilter.all:
        return conversations;
      case MessageFilter.primary:
        return conversations.where((c) => c.isPrimary).toList();
      case MessageFilter.requests:
        return conversations.where((c) => !c.isPrimary && c.unreadCount > 0).toList();
    }
  }



  Widget _buildEmptyState() {
    final l10n = AppLocalizations.of(context)!;
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
              child: const Icon(Icons.chat_bubble_outline, size: 48, color: AppColors.primaryPurple),
            ),
            const SizedBox(height: 16),
            Text(l10n.noMessages, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text(
              'Start a conversation with someone in the community!',
              style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () => _showNewMessageSheet(context),
              icon: const Icon(Icons.add),
              label: Text(l10n.newMessage),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildErrorState(String error) {
    final l10n = AppLocalizations.of(context)!;
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 64, color: Theme.of(context).colorScheme.error),
          const SizedBox(height: 16),
          Text(error),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () => ref.read(conversationsProvider.notifier).refresh(),
            child: Text(l10n.retry),
          ),
        ],
      ),
    );
  }

  void _openConversation(Conversation conversation) {
    context.push('/chat', extra: {'conversationId': conversation.id, 'userId': conversation.participantId});
  }

  void _showConversationOptions(Conversation conversation) {
    HapticFeedback.mediumImpact();
    showModalBottomSheet(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              margin: const EdgeInsets.symmetric(vertical: 12),
              width: 40, height: 4,
              decoration: BoxDecoration(color: Theme.of(ctx).dividerColor, borderRadius: BorderRadius.circular(2)),
            ),
            ListTile(
              leading: Icon(conversation.isPinned ? Icons.push_pin_outlined : Icons.push_pin),
              title: Text(conversation.isPinned ? 'Unpin' : 'Pin'),
              onTap: () => Navigator.pop(ctx),
            ),
            ListTile(leading: const Icon(Icons.notifications_off_outlined), title: const Text('Mute'), onTap: () => Navigator.pop(ctx)),
            ListTile(leading: const Icon(Icons.archive_outlined), title: const Text('Archive'), onTap: () => Navigator.pop(ctx)),
            ListTile(
              leading: Icon(Icons.delete_outline, color: Theme.of(ctx).colorScheme.error),
              title: Text('Delete', style: TextStyle(color: Theme.of(ctx).colorScheme.error)),
              onTap: () { Navigator.pop(ctx); _confirmDelete(conversation); },
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  void _confirmDelete(Conversation conversation) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete Conversation?'),
        content: const Text('This will permanently delete this conversation.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          FilledButton(
            onPressed: () => Navigator.pop(ctx),
            style: FilledButton.styleFrom(backgroundColor: Theme.of(ctx).colorScheme.error),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _showNewMessageSheet(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => const _NewMessageSheet(),
    );
  }
}

/// Enhanced conversation tile matching the Android version design
class _ConversationTile extends StatelessWidget {
  final Conversation conversation;
  final VoidCallback? onTap;
  final VoidCallback? onLongPress;

  const _ConversationTile({required this.conversation, this.onTap, this.onLongPress});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final hasUnread = conversation.unreadCount > 0;
    final primaryColor = theme.colorScheme.primary;
    final tertiaryColor = theme.colorScheme.tertiary;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Material(
        color: hasUnread
            ? primaryColor.withOpacity(0.08)
            : theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(16),
        child: InkWell(
          onTap: onTap,
          onLongPress: onLongPress,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
            child: Row(
              children: [
                // Avatar with online indicator
                Stack(
                  children: [
                    // Gradient ring for unread
                    if (hasUnread)
                      Container(
                        width: 58,
                        height: 58,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: LinearGradient(
                            colors: [primaryColor, tertiaryColor],
                          ),
                        ),
                      ),
                    Padding(
                      padding: EdgeInsets.all(hasUnread ? 2 : 0),
                      child: NeuroAvatar(
                        imageUrl: conversation.avatarUrl,
                        name: conversation.displayName,
                        size: hasUnread ? 54 : 54,
                      ),
                    ),
                    if (conversation.isOnline)
                      Positioned(
                        right: hasUnread ? 4 : 2,
                        bottom: hasUnread ? 4 : 2,
                        child: Container(
                          width: 14,
                          height: 14,
                          decoration: BoxDecoration(
                            color: AppColors.success,
                            shape: BoxShape.circle,
                            border: Border.all(color: theme.scaffoldBackgroundColor, width: 2),
                          ),
                        ),
                      ),
                    // Moderation status badge overlay
                    if (conversation.moderationStatus != ModerationStatus.none)
                      Positioned(
                        left: hasUnread ? 0 : -2,
                        top: hasUnread ? 0 : -2,
                        child: _ModerationBadge(status: conversation.moderationStatus),
                      ),
                  ],
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Row(
                              children: [
                                Flexible(
                                  child: Text(
                                    conversation.displayName,
                                    style: theme.textTheme.titleSmall?.copyWith(
                                      fontWeight: hasUnread ? FontWeight.bold : FontWeight.w500,
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                if (conversation.isVerified) ...[
                                  const SizedBox(width: 4),
                                  Icon(Icons.verified, size: 16, color: primaryColor),
                                ],
                              ],
                            ),
                          ),
                          Text(
                            _formatTime(conversation.lastMessageAt),
                            style: theme.textTheme.labelSmall?.copyWith(
                              color: hasUnread ? primaryColor : theme.colorScheme.outline,
                              fontWeight: hasUnread ? FontWeight.w600 : null,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          Expanded(
                            child: conversation.isTyping
                                ? Row(
                                    children: [
                                      Text(
                                        'typing',
                                        style: theme.textTheme.bodyMedium?.copyWith(
                                          color: primaryColor,
                                          fontStyle: FontStyle.italic,
                                        ),
                                      ),
                                      const SizedBox(width: 4),
                                      _TypingDots(color: primaryColor),
                                    ],
                                  )
                                : Text(
                                    conversation.lastMessage ?? '',
                                    style: theme.textTheme.bodySmall?.copyWith(
                                      color: hasUnread
                                          ? theme.colorScheme.onSurface
                                          : theme.colorScheme.onSurfaceVariant,
                                      fontWeight: hasUnread ? FontWeight.w500 : null,
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                          ),
                          if (conversation.isMuted)
                            Padding(
                              padding: const EdgeInsets.only(left: 8),
                              child: Icon(Icons.notifications_off, size: 16, color: theme.colorScheme.outline),
                            ),
                          if (hasUnread)
                            Container(
                              margin: const EdgeInsets.only(left: 8),
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                              decoration: BoxDecoration(
                                gradient: LinearGradient(
                                  colors: [primaryColor, tertiaryColor],
                                ),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                conversation.unreadCount > 99 ? '99+' : conversation.unreadCount.toString(),
                                style: TextStyle(color: theme.colorScheme.onPrimary, fontSize: 11, fontWeight: FontWeight.bold),
                              ),
                            ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _formatTime(DateTime? dateTime) {
    if (dateTime == null) return '';
    final now = DateTime.now();
    final difference = now.difference(dateTime);
    if (difference.inDays > 7) return '${dateTime.month}/${dateTime.day}';
    if (difference.inDays > 0) return '${difference.inDays}d';
    if (difference.inHours > 0) return '${difference.inHours}h';
    if (difference.inMinutes > 0) return '${difference.inMinutes}m';
    return 'Now';
  }
}

class _TypingDots extends StatefulWidget {
  final Color color;
  const _TypingDots({required this.color});
  @override
  State<_TypingDots> createState() => _TypingDotsState();
}

class _TypingDotsState extends State<_TypingDots> with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(milliseconds: 1000))..repeat();
  }

  @override
  void dispose() { _controller.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: List.generate(3, (index) {
            final delay = index * 0.2;
            final animValue = ((_controller.value + delay) % 1.0);
            final opacity = animValue < 0.5 ? animValue * 2 : 2 - animValue * 2;
            return Container(
              margin: const EdgeInsets.symmetric(horizontal: 1),
              child: Opacity(
                opacity: 0.3 + opacity * 0.7,
                child: Container(
                  width: 4, height: 4,
                  decoration: BoxDecoration(color: widget.color, shape: BoxShape.circle),
                ),
              ),
            );
          }),
        );
      },
    );
  }
}

class _NewMessageSheet extends StatefulWidget {
  const _NewMessageSheet();
  @override
  State<_NewMessageSheet> createState() => _NewMessageSheetState();
}

class _NewMessageSheetState extends State<_NewMessageSheet> {
  final TextEditingController _searchController = TextEditingController();
  final List<_User> _suggestedUsers = [
    _User(id: '1', name: 'Alex Thompson', username: 'alex_t', avatarUrl: 'https://i.pravatar.cc/150?img=1', isOnline: true),
    _User(id: '2', name: 'Jordan Lee', username: 'jordanlee', avatarUrl: 'https://i.pravatar.cc/150?img=2'),
    _User(id: '3', name: 'Sam Rivera', username: 'samr', avatarUrl: 'https://i.pravatar.cc/150?img=3', isOnline: true),
  ];

  @override
  void dispose() { _searchController.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return DraggableScrollableSheet(
      initialChildSize: 0.9, minChildSize: 0.5, maxChildSize: 0.95,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40, height: 4,
                decoration: BoxDecoration(color: theme.dividerColor, borderRadius: BorderRadius.circular(2)),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    Text(l10n.newMessage, style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
                    const Spacer(),
                    IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.pop(context)),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: TextField(
                  controller: _searchController,
                  autofocus: true,
                  decoration: InputDecoration(
                    hintText: 'Search by name or username...',
                    prefixIcon: const Icon(Icons.search),
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                    filled: true,
                    fillColor: theme.colorScheme.surfaceContainerHighest,
                  ),
                ),
              ),
              Expanded(
                child: ListView(
                  controller: scrollController,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  children: [
                    Text('Suggested', style: theme.textTheme.labelMedium?.copyWith(
                      color: theme.colorScheme.outline, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 8),
                    ..._suggestedUsers.map((user) => ListTile(
                      onTap: () {
                        Navigator.pop(context);
                        context.push('/chat', extra: {'userId': user.id});
                      },
                      contentPadding: EdgeInsets.zero,
                      leading: Stack(
                        children: [
                          NeuroAvatar(imageUrl: user.avatarUrl, name: user.name, size: 48),
                          if (user.isOnline)
                            Positioned(right: 0, bottom: 0, child: Container(
                              width: 12, height: 12,
                              decoration: BoxDecoration(
                                color: AppColors.success, shape: BoxShape.circle,
                                border: Border.all(color: theme.scaffoldBackgroundColor, width: 2),
                              ),
                            )),
                        ],
                      ),
                      title: Text(user.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
                      subtitle: Text('@${user.username}'),
                      trailing: const Icon(Icons.chat_bubble_outline),
                    )),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _User {
  final String id;
  final String name;
  final String username;
  final String avatarUrl;
  final bool isOnline;
  _User({required this.id, required this.name, required this.username, required this.avatarUrl, this.isOnline = false});
}


/// Beautiful header matching Notifications style
class _MessagesHeader extends StatelessWidget {
  final int unreadCount;
  final VoidCallback onNewMessage;
  final VoidCallback onSearch;
  final VoidCallback onPracticeCall;
  final AppLocalizations l10n;

  const _MessagesHeader({
    required this.unreadCount,
    required this.onNewMessage,
    required this.onSearch,
    required this.onPracticeCall,
    required this.l10n,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text(
                          l10n.messagesTitle,
                          style: theme.textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            letterSpacing: -0.5,
                          ),
                        ),
                        if (unreadCount > 0) ...[
                          const SizedBox(width: 12),
                          _UnreadBadge(count: unreadCount),
                        ],
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      unreadCount > 0
                          ? 'You have $unreadCount unread ${unreadCount == 1 ? 'message' : 'messages'}'
                          : 'Your conversations ✨',
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
                    onPressed: onSearch,
                    tooltip: 'Search',
                  ),
                  _HeaderIconButton(
                    icon: Icons.phone_rounded,
                    onPressed: onPracticeCall,
                    tooltip: 'Practice Calls',
                  ),
                  _HeaderIconButton(
                    icon: Icons.edit_square,
                    onPressed: onNewMessage,
                    tooltip: l10n.newMessage,
                    isPrimary: true,
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Animated unread badge matching Notifications style
class _UnreadBadge extends StatefulWidget {
  final int count;

  const _UnreadBadge({required this.count});

  @override
  State<_UnreadBadge> createState() => _UnreadBadgeState();
}

class _UnreadBadgeState extends State<_UnreadBadge>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _pulseAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    _pulseAnimation = Tween<double>(begin: 1.0, end: 1.1).animate(
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
    final tertiaryColor = theme.colorScheme.tertiary;

    return AnimatedBuilder(
      animation: _pulseAnimation,
      builder: (context, child) {
        return Transform.scale(
          scale: _pulseAnimation.value,
          child: child,
        );
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [primaryColor, tertiaryColor],
          ),
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: primaryColor.withOpacity(0.3),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Text(
          widget.count > 99 ? '99+' : widget.count.toString(),
          style: TextStyle(
            color: theme.colorScheme.onPrimary,
            fontWeight: FontWeight.bold,
            fontSize: 12,
          ),
        ),
      ),
    );
  }
}

/// Header icon button with optional primary styling
class _HeaderIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String? tooltip;
  final bool isPrimary;

  const _HeaderIconButton({
    required this.icon,
    required this.onPressed,
    this.tooltip,
    this.isPrimary = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;

    Widget button = Material(
      color: isPrimary
          ? primaryColor.withOpacity(0.15)
          : theme.colorScheme.surfaceContainerHighest,
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
            color: isPrimary
                ? primaryColor
                : theme.colorScheme.onSurfaceVariant,
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

/// Search bar with animation
class _SearchBar extends StatelessWidget {
  final TextEditingController controller;
  final VoidCallback onClose;

  const _SearchBar({
    required this.controller,
    required this.onClose,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
      child: Container(
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(16),
        ),
        child: TextField(
          controller: controller,
          autofocus: true,
          decoration: InputDecoration(
            hintText: 'Search messages...',
            prefixIcon: const Icon(Icons.search_rounded),
            suffixIcon: IconButton(
              icon: const Icon(Icons.close_rounded),
              onPressed: onClose,
            ),
            border: InputBorder.none,
            contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          ),
        ),
      ),
    );
  }
}

/// Filter chips section matching Notifications style
class _FilterChipsSection extends StatelessWidget {
  final MessageFilter selectedFilter;
  final int unreadCount;
  final ValueChanged<MessageFilter> onFilterChanged;

  const _FilterChipsSection({
    required this.selectedFilter,
    required this.unreadCount,
    required this.onFilterChanged,
  });

  @override
  Widget build(BuildContext context) {
    final filters = [
      (MessageFilter.all, 'All', Icons.chat_bubble_rounded, null),
      (MessageFilter.primary, 'Primary', Icons.star_rounded, null),
      (MessageFilter.requests, 'Requests', Icons.mark_email_unread_rounded, null),
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
          final (filter, label, icon, count) = filters[index];
          final isSelected = selectedFilter == filter;

          return _FilterPill(
            label: label,
            icon: icon,
            count: count,
            isSelected: isSelected,
            onTap: () => onFilterChanged(filter),
          );
        },
      ),
    );
  }
}

/// Individual filter pill matching Android version style
class _FilterPill extends StatelessWidget {
  final String label;
  final IconData icon;
  final int? count;
  final bool isSelected;
  final VoidCallback onTap;

  const _FilterPill({
    required this.label,
    required this.icon,
    this.count,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;

    return Material(
      color: isSelected
          ? primaryColor.withOpacity(0.15)
          : theme.colorScheme.surfaceContainerHighest,
      borderRadius: BorderRadius.circular(24),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          decoration: isSelected
              ? BoxDecoration(
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(
                    color: primaryColor.withOpacity(0.3),
                    width: 1.5,
                  ),
                )
              : null,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (isSelected)
                Icon(
                  Icons.check,
                  size: 16,
                  color: primaryColor,
                ),
              if (isSelected) const SizedBox(width: 6),
              Text(
                label,
                style: theme.textTheme.labelMedium?.copyWith(
                  color: isSelected
                      ? primaryColor
                      : theme.colorScheme.onSurfaceVariant,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                ),
              ),
              if (count != null && count! > 0) ...[
                const SizedBox(width: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: primaryColor.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    count! > 99 ? '99+' : count.toString(),
                    style: TextStyle(
                      color: primaryColor,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Moderation status badge overlay for user avatars
class _ModerationBadge extends StatelessWidget {
  final ModerationStatus status;

  const _ModerationBadge({required this.status});

  @override
  Widget build(BuildContext context) {
    final (icon, color, tooltip) = _badgeInfo(status);

    return Tooltip(
      message: tooltip,
      child: Container(
        width: 18,
        height: 18,
        decoration: BoxDecoration(
          color: color,
          shape: BoxShape.circle,
          border: Border.all(
            color: Theme.of(context).scaffoldBackgroundColor,
            width: 1.5,
          ),
          boxShadow: [
            BoxShadow(
              color: color.withOpacity(0.3),
              blurRadius: 4,
              spreadRadius: 1,
            ),
          ],
        ),
        child: Icon(
          icon,
          size: 10,
          color: Colors.white,
        ),
      ),
    );
  }

  (IconData, Color, String) _badgeInfo(ModerationStatus status) {
    switch (status) {
      case ModerationStatus.none:
        return (Icons.circle, Colors.transparent, '');
      case ModerationStatus.verified:
        return (Icons.verified, AppColors.primaryPurple, 'Verified User');
      case ModerationStatus.moderator:
        return (Icons.shield, AppColors.secondaryTeal, 'Community Moderator');
      case ModerationStatus.admin:
        return (Icons.admin_panel_settings, AppColors.accentOrange, 'Admin');
      case ModerationStatus.warned:
        return (Icons.warning_amber, AppColors.warning, 'Account Warning');
      case ModerationStatus.restricted:
        return (Icons.block, AppColors.error, 'Restricted Account');
    }
  }
}

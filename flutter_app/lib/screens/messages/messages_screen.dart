import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/conversation.dart';
import '../../providers/messages_provider.dart';
import '../../providers/message_delete_mode_provider.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../widgets/common/neuro_loading.dart';
import '../../widgets/brand/liquid_glass.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../services/contacts_picker_service.dart';
import '../../services/webrtc_call_service.dart';
import '../../services/contacts_call_service.dart';
import '../calling/active_call_screen.dart';

/// Message tab filter options
enum MessageFilter {
  all,
  primary,
  calls,
  requests,
}

Widget _buildExperimentalSheetHandle(BuildContext context) {
  final theme = Theme.of(context);
  return Container(
    margin: const EdgeInsets.symmetric(vertical: 12),
    width: 40,
    height: 4,
    decoration: BoxDecoration(
      color: theme.dividerColor,
      borderRadius: BorderRadius.circular(2),
    ),
  );
}

Widget _wrapExperimentalActionSheet(
  BuildContext context, {
  required ABTestVariant variant,
  required Widget child,
}) {
  if (variant.isLiquidGlass) {
    return LiquidGlassBottomSheet(
      variant: variant.surfaceVariantName,
      child: child,
    );
  }

  if (variant.isSkeumorphic) {
    return SkeuomorphicPanel(
      isFull: variant.isFullSkeumorphic,
      borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
      child: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.only(top: 28),
            child: child,
          ),
          Positioned(
            top: 12,
            left: 0,
            right: 0,
            child: Center(child: _buildExperimentalSheetHandle(context)),
          ),
        ],
      ),
    );
  }

  return child;
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
    final abVariant = ref.watch(devOptionsProvider).abTestVariant;
    final backgroundColor = abVariant.isSkeumorphic
        ? Color.alphaBlend(
            theme.colorScheme.primary.withValues(
              alpha: abVariant.isFullSkeumorphic ? 0.08 : 0.04,
            ),
            theme.colorScheme.surface,
          )
        : theme.colorScheme.surface;

    return Scaffold(
      backgroundColor: backgroundColor,
      body: SafeArea(
        child: messagesState.when(
          loading: () => NeuroLoading(message: l10n.get('loadingMessages')),
          error: (error, stack) => _buildErrorState(error.toString()),
          data: (conversations) => isTablet
              ? _buildTabletLayout(context, conversations, l10n, theme, abVariant)
              : _buildContent(context, conversations, l10n, abVariant),
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
    ABTestVariant abVariant,
  ) {
    return Row(
      children: [
        // Conversation list pane (40% width)
        SizedBox(
          width: MediaQuery.of(context).size.width * 0.4,
          child: _buildContent(context, conversations, l10n, abVariant),
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
                  l10n.get('selectConversation'),
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  l10n.get('chooseConversationToStartMessaging'),
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
    ABTestVariant abVariant,
  ) {
    final unreadCount = conversations.fold<int>(
      0, (sum, c) => sum + c.unreadCount);
    final filteredConversations = _filterConversations(conversations);
    final deleteMode = ref.watch(messageDeleteModeProvider);

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
              onDeleteModeSettings: () => _showDeleteModeSheet(context),
              l10n: l10n,
              abTestVariant: abVariant,
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
                    abTestVariant: abVariant,
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
            abTestVariant: abVariant,
            onFilterChanged: (filter) {
              HapticFeedback.selectionClick();
              setState(() => _selectedFilter = filter);
            },
          ),
        ),

        // Content
        if (_selectedFilter == MessageFilter.calls)
          SliverFillRemaining(
            hasScrollBody: true,
            child: _InlineCallHistoryView(
              onOpenPracticeCall: () => context.push('/practice-calls'),
            ),
          )
        else if (filteredConversations.isEmpty)
          SliverFillRemaining(
            hasScrollBody: false,
            child: _buildEmptyState(abVariant),
          )
        else
          SliverPadding(
            padding: const EdgeInsets.only(bottom: 100),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  final conversation = filteredConversations[index];
                  final tile = _ConversationTile(
                    conversation: conversation,
                    abTestVariant: abVariant,
                    onTap: () => _openConversation(conversation),
                    onLongPress: deleteMode == MessageDeleteMode.longPress
                        ? () => _showConversationOptions(conversation)
                        : null,
                  );

                  // Swipe-to-delete mode: wrap with Dismissible
                  if (deleteMode == MessageDeleteMode.swipe) {
                    return Dismissible(
                      key: Key(conversation.id),
                      direction: DismissDirection.endToStart,
                      confirmDismiss: (_) => _confirmDeleteSwipe(conversation),
                      background: Container(
                        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                        decoration: BoxDecoration(
                          color: Theme.of(context).colorScheme.errorContainer,
                          borderRadius: BorderRadius.circular(16),
                        ),
                        alignment: Alignment.centerRight,
                        padding: const EdgeInsets.only(right: 24),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.delete_rounded,
                              color: Theme.of(context).colorScheme.onErrorContainer,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              l10n.delete,
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.onErrorContainer,
                                fontSize: 12,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ],
                        ),
                      ),
                      child: tile,
                    );
                  }

                  return tile;
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
      case MessageFilter.calls:
        return []; // Calls tab shows its own view, not conversations
      case MessageFilter.requests:
        return conversations.where((c) => !c.isPrimary && c.unreadCount > 0).toList();
    }
  }



  Widget _buildEmptyState(ABTestVariant abVariant) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final isSkeuomorphic = abVariant.isSkeumorphic;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (isSkeuomorphic)
              SkeuomorphicPanel(
                isFull: abVariant.isFullSkeumorphic,
                borderRadius: BorderRadius.circular(40),
                child: const SizedBox(
                  width: 96,
                  height: 96,
                  child: Icon(
                    Icons.chat_bubble_outline,
                    size: 48,
                    color: AppColors.primaryPurple,
                  ),
                ),
              )
            else Container(
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
              l10n.get('startConversationHint'),
              style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            if (isSkeuomorphic)
              SkeuomorphicPanel(
                isFull: abVariant.isFullSkeumorphic,
                isActive: true,
                borderRadius: BorderRadius.circular(24),
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    borderRadius: BorderRadius.circular(24),
                    onTap: () => _showNewMessageSheet(context),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.add, color: AppColors.primaryPurple),
                          const SizedBox(width: 8),
                          Text(
                            l10n.newMessage,
                            style: theme.textTheme.labelLarge?.copyWith(
                              color: AppColors.primaryPurple,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              )
            else FilledButton.icon(
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
    context.push('/chat/${Uri.encodeComponent(conversation.id)}');
  }

  void _showConversationOptions(Conversation conversation) {
    final l10n = AppLocalizations.of(context)!;
    final abVariant = ref.read(devOptionsProvider).abTestVariant;
    HapticFeedback.mediumImpact();
    showModalBottomSheet(
      context: context,
      backgroundColor: abVariant.usesExperimentalSurfaceChrome
          ? Colors.transparent
          : null,
      builder: (ctx) => _wrapExperimentalActionSheet(
        ctx,
        variant: abVariant,
        child: SafeArea(
          top: false,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (!abVariant.usesExperimentalSurfaceChrome)
                _buildExperimentalSheetHandle(ctx),
              ListTile(
                leading: Icon(conversation.isPinned ? Icons.push_pin_outlined : Icons.push_pin),
                title: Text(conversation.isPinned ? l10n.unpin : l10n.pin),
                onTap: () => Navigator.pop(ctx),
              ),
              ListTile(leading: const Icon(Icons.notifications_off_outlined), title: Text(l10n.mute), onTap: () => Navigator.pop(ctx)),
              ListTile(leading: const Icon(Icons.archive_outlined), title: Text(l10n.archive), onTap: () => Navigator.pop(ctx)),
              ListTile(
                leading: Icon(Icons.delete_outline, color: Theme.of(ctx).colorScheme.error),
                title: Text(l10n.delete, style: TextStyle(color: Theme.of(ctx).colorScheme.error)),
                onTap: () { Navigator.pop(ctx); _confirmDelete(conversation); },
              ),
              const SizedBox(height: 16),
            ],
          ),
        ),
      ),
    );
  }

  void _confirmDelete(Conversation conversation) {
    final l10n = AppLocalizations.of(context)!;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.deleteConversation),
        content: Text(l10n.deleteConversationDesc),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: Text(l10n.cancel)),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(conversationsProvider.notifier).deleteConversation(conversation.id);
            },
            style: FilledButton.styleFrom(backgroundColor: Theme.of(ctx).colorScheme.error),
            child: Text(l10n.delete),
          ),
        ],
      ),
    );
  }

  /// Used by Dismissible.confirmDismiss – returns true only if user taps "Delete".
  Future<bool> _confirmDeleteSwipe(Conversation conversation) async {
    final l10n = AppLocalizations.of(context)!;
    final result = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.deleteConversation),
        content: Text(l10n.deleteConversationDesc),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(l10n.cancel),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: FilledButton.styleFrom(backgroundColor: Theme.of(ctx).colorScheme.error),
            child: Text(l10n.delete),
          ),
        ],
      ),
    );
    if (result == true) {
      ref.read(conversationsProvider.notifier).deleteConversation(conversation.id);
      return true;
    }
    return false;
  }

  void _showDeleteModeSheet(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final currentMode = ref.read(messageDeleteModeProvider);
    final abVariant = ref.read(devOptionsProvider).abTestVariant;

    showModalBottomSheet(
      context: context,
      backgroundColor: abVariant.usesExperimentalSurfaceChrome
          ? Colors.transparent
          : null,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => _wrapExperimentalActionSheet(
        ctx,
        variant: abVariant,
        child: SafeArea(
          top: false,
          child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (!abVariant.usesExperimentalSurfaceChrome)
                _buildExperimentalSheetHandle(ctx),
              const SizedBox(height: 20),
              // Title
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppColors.primaryPurple.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(
                      Icons.delete_sweep_rounded,
                      color: AppColors.primaryPurple,
                      size: 22,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n.messageDeleteMode,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          l10n.messageDeleteModeDesc,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              // Swipe option
              _DeleteModeOptionTile(
                icon: Icons.swipe_left_rounded,
                title: l10n.swipeToDelete,
                description: l10n.swipeToDeleteDesc,
                isSelected: currentMode == MessageDeleteMode.swipe,
                abTestVariant: abVariant,
                onTap: () {
                  HapticFeedback.selectionClick();
                  ref.read(messageDeleteModeProvider.notifier).setMode(MessageDeleteMode.swipe);
                  Navigator.pop(ctx);
                },
              ),
              const SizedBox(height: 10),
              // Long-press option
              _DeleteModeOptionTile(
                icon: Icons.touch_app_rounded,
                title: l10n.longPressToDelete,
                description: l10n.longPressToDeleteDesc,
                isSelected: currentMode == MessageDeleteMode.longPress,
                abTestVariant: abVariant,
                onTap: () {
                  HapticFeedback.selectionClick();
                  ref.read(messageDeleteModeProvider.notifier).setMode(MessageDeleteMode.longPress);
                  Navigator.pop(ctx);
                },
              ),
            ],
          ),
          ),
        ),
      ),
    );
  }

  void _showNewMessageSheet(BuildContext context) {
    final abVariant = ref.read(devOptionsProvider).abTestVariant;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: abVariant.usesExperimentalSurfaceChrome
          ? Colors.transparent
          : Colors.transparent,
      builder: (context) => _NewMessageSheet(abTestVariant: abVariant),
    );
  }
}

/// Enhanced conversation tile matching the Android version design
class _ConversationTile extends StatelessWidget {
  final Conversation conversation;
  final ABTestVariant abTestVariant;
  final VoidCallback? onTap;
  final VoidCallback? onLongPress;

  const _ConversationTile({
    required this.conversation,
    required this.abTestVariant,
    this.onTap,
    this.onLongPress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final hasUnread = conversation.unreadCount > 0;
    final primaryColor = theme.colorScheme.primary;
    final tertiaryColor = theme.colorScheme.tertiary;
    final isSkeuomorphic = abTestVariant.isSkeumorphic;

    final tileBody = Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      child: Row(
        children: [
          // Avatar with online indicator
          Stack(
            children: [
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
                  size: 54,
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
              if (conversation.moderationStatus != ModerationStatus.none)
                Positioned(
                  left: hasUnread ? 0 : -2,
                  top: hasUnread ? 0 : -2,
                  child: _ModerationBadge(status: conversation.moderationStatus),
                ),
              if (conversation.isGroup && (conversation.participantIds?.length ?? 0) > 2)
                Positioned(
                  right: 0,
                  bottom: 0,
                  child: Container(
                    width: 20,
                    height: 20,
                    decoration: BoxDecoration(
                      color: theme.colorScheme.tertiaryContainer,
                      shape: BoxShape.circle,
                      border: Border.all(color: theme.scaffoldBackgroundColor, width: 1.5),
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      '${conversation.participantIds!.length}',
                      style: TextStyle(
                        fontSize: 9,
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.onTertiaryContainer,
                      ),
                    ),
                  ),
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
                          if (conversation.isGroup && conversation.memberNames != null) ...[
                            const SizedBox(width: 6),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: theme.colorScheme.secondaryContainer,
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                '${conversation.memberNames!.length} members',
                                style: TextStyle(
                                  fontSize: 10,
                                  fontWeight: FontWeight.w600,
                                  color: theme.colorScheme.onSecondaryContainer,
                                ),
                              ),
                            ),
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
          if (!conversation.isGroup)
            Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                SizedBox(
                  width: 32,
                  height: 32,
                  child: IconButton(
                    padding: EdgeInsets.zero,
                    iconSize: 18,
                    icon: Icon(Icons.phone, color: theme.colorScheme.onSurfaceVariant),
                    onPressed: () {
                      HapticFeedback.selectionClick();
                      WebRTCCallService.instance.startCall(
                        recipientId: conversation.id,
                        recipientName: conversation.displayName,
                        recipientAvatar: conversation.avatarUrl ?? '',
                        callType: CallType.voice,
                      );
                      Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
                      );
                    },
                  ),
                ),
                SizedBox(
                  width: 32,
                  height: 32,
                  child: IconButton(
                    padding: EdgeInsets.zero,
                    iconSize: 18,
                    icon: Icon(Icons.videocam, color: theme.colorScheme.onSurfaceVariant),
                    onPressed: () {
                      HapticFeedback.selectionClick();
                      WebRTCCallService.instance.startCall(
                        recipientId: conversation.id,
                        recipientName: conversation.displayName,
                        recipientAvatar: conversation.avatarUrl ?? '',
                        callType: CallType.video,
                      );
                      Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
                      );
                    },
                  ),
                ),
              ],
            ),
        ],
      ),
    );

    final tile = Material(
      color: isSkeuomorphic
          ? Colors.transparent
          : hasUnread
              ? primaryColor.withValues(alpha: 0.08)
              : theme.colorScheme.surface,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        onLongPress: onLongPress,
        borderRadius: BorderRadius.circular(16),
        child: tileBody,
      ),
    );

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
      child: isSkeuomorphic
          ? SkeuomorphicPanel(
              isFull: abTestVariant.isFullSkeumorphic,
              isActive: hasUnread,
              borderRadius: BorderRadius.circular(16),
              child: tile,
            )
          : tile,
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
  final ABTestVariant abTestVariant;

  const _NewMessageSheet({required this.abTestVariant});
  @override
  State<_NewMessageSheet> createState() => _NewMessageSheetState();
}

class _NewMessageSheetState extends State<_NewMessageSheet> {
  final TextEditingController _searchController = TextEditingController();
  final ContactsPickerService _contactsService = ContactsPickerService();
  bool _isPickingContact = false;
  final List<_User> _suggestedUsers = [
    _User(id: '1', name: 'Alex Thompson', username: 'alex_t', avatarUrl: 'https://i.pravatar.cc/150?u=alex_thompson', isOnline: true),
    _User(id: '2', name: 'Jordan Lee', username: 'jordanlee', avatarUrl: 'https://i.pravatar.cc/150?u=jordan_lee'),
    _User(id: '3', name: 'Sam Rivera', username: 'samr', avatarUrl: 'https://i.pravatar.cc/150?u=sam_rivera', isOnline: true),
  ];

  @override
  void dispose() { _searchController.dispose(); super.dispose(); }

  Future<void> _pickFromContacts() async {
    HapticFeedback.selectionClick();
    setState(() => _isPickingContact = true);

    try {
      final contact = await _contactsService.pickContact();
      if (contact != null && mounted) {
        setState(() {
          _isPickingContact = false;
        });
        // Navigate to chat with the picked contact
        if (mounted) {
          Navigator.pop(context);
          context.push('/chat', extra: {
            'userId': contact.displayName.replaceAll(' ', '_').toLowerCase(),
            'contactName': contact.displayName,
            'contactPhone': contact.phoneNumber,
            'contactEmail': contact.email,
          });
        }
      } else {
        if (mounted) setState(() => _isPickingContact = false);
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isPickingContact = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not access contacts: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final isSkeuomorphic = widget.abTestVariant.isSkeumorphic;
    final isFull = widget.abTestVariant.isFullSkeumorphic;

    return DraggableScrollableSheet(
      initialChildSize: 0.9, minChildSize: 0.5, maxChildSize: 0.95,
      builder: (context, scrollController) {
        final headerCloseButton = isSkeuomorphic
            ? SkeuomorphicIconButton(
                icon: Icons.close,
                onPressed: () => Navigator.pop(context),
                tooltip: 'Close',
                isFull: isFull,
                size: 40,
                iconSize: 20,
              )
            : IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => Navigator.pop(context),
              );

        final content = Column(
            children: [
              if (!widget.abTestVariant.usesExperimentalSurfaceChrome)
                _buildExperimentalSheetHandle(context),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            l10n.newMessage,
                            style: theme.textTheme.titleLarge?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          if (isSkeuomorphic)
                            Padding(
                              padding: const EdgeInsets.only(top: 2),
                              child: Text(
                                isFull
                                    ? 'Deeper conversation chrome with clearer focus'
                                    : 'Gentle depth for quick, calm starts',
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                            ),
                        ],
                      ),
                    ),
                    const Spacer(),
                    headerCloseButton,
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: isSkeuomorphic
                    ? SkeuomorphicPanel(
                        isFull: isFull,
                        borderRadius: BorderRadius.circular(18),
                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
                        child: Row(
                          children: [
                            Icon(
                              Icons.search,
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: TextField(
                                controller: _searchController,
                                autofocus: true,
                                decoration: InputDecoration(
                                  hintText: l10n.searchByUsername,
                                  border: InputBorder.none,
                                  isCollapsed: true,
                                ),
                              ),
                            ),
                          ],
                        ),
                      )
                    : TextField(
                        controller: _searchController,
                        autofocus: true,
                        decoration: InputDecoration(
                          hintText: l10n.searchByUsername,
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
                    // ── Pick from device contacts ──
                    if (isSkeuomorphic)
                      SkeuomorphicPanel(
                        isFull: isFull,
                        isActive: _isPickingContact,
                        borderRadius: BorderRadius.circular(20),
                        child: Material(
                          color: Colors.transparent,
                          child: InkWell(
                            onTap: _isPickingContact ? null : _pickFromContacts,
                            borderRadius: BorderRadius.circular(20),
                            child: Padding(
                              padding: const EdgeInsets.all(14),
                              child: Row(
                                children: [
                                  Container(
                                    width: 48,
                                    height: 48,
                                    decoration: BoxDecoration(
                                      gradient: const LinearGradient(
                                        colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                                      ),
                                      borderRadius: BorderRadius.circular(14),
                                    ),
                                    child: _isPickingContact
                                        ? const Padding(
                                            padding: EdgeInsets.all(12),
                                            child: CircularProgressIndicator(
                                              strokeWidth: 2,
                                              color: Colors.white,
                                            ),
                                          )
                                        : const Icon(Icons.contacts_rounded, color: Colors.white, size: 24),
                                  ),
                                  const SizedBox(width: 14),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          l10n.get('pickFromContacts'),
                                          style: theme.textTheme.titleSmall?.copyWith(
                                            fontWeight: FontWeight.bold,
                                            color: AppColors.primaryPurple,
                                          ),
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          l10n.get('chooseContactFromPhone'),
                                          style: theme.textTheme.bodySmall?.copyWith(
                                            color: theme.colorScheme.onSurfaceVariant,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  Icon(
                                    Icons.chevron_right_rounded,
                                    color: AppColors.primaryPurple.withValues(alpha: 0.6),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      )
                    else Material(
                        color: AppColors.primaryPurple.withValues(alpha: 0.08),
                        borderRadius: BorderRadius.circular(16),
                        child: InkWell(
                          onTap: _isPickingContact ? null : _pickFromContacts,
                          borderRadius: BorderRadius.circular(16),
                          child: Padding(
                            padding: const EdgeInsets.all(14),
                            child: Row(
                              children: [
                                Container(
                                  width: 48,
                                  height: 48,
                                  decoration: BoxDecoration(
                                    gradient: const LinearGradient(
                                      colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                                    ),
                                    borderRadius: BorderRadius.circular(14),
                                  ),
                                  child: _isPickingContact
                                      ? const Padding(
                                          padding: EdgeInsets.all(12),
                                          child: CircularProgressIndicator(
                                            strokeWidth: 2,
                                            color: Colors.white,
                                          ),
                                        )
                                      : const Icon(Icons.contacts_rounded, color: Colors.white, size: 24),
                                ),
                                const SizedBox(width: 14),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        l10n.get('pickFromContacts'),
                                        style: theme.textTheme.titleSmall?.copyWith(
                                          fontWeight: FontWeight.bold,
                                          color: AppColors.primaryPurple,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        l10n.get('chooseContactFromPhone'),
                                        style: theme.textTheme.bodySmall?.copyWith(
                                          color: theme.colorScheme.onSurfaceVariant,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                Icon(
                                  Icons.chevron_right_rounded,
                                  color: AppColors.primaryPurple.withValues(alpha: 0.6),
                                ),
                              ],
                            ),
                          ),
                        ),
                    ),
                    const SizedBox(height: 20),

                    // ── Suggested users ──
                    Row(
                      children: [
                        Text(l10n.get('suggested'), style: theme.textTheme.labelMedium?.copyWith(
                          color: theme.colorScheme.outline, fontWeight: FontWeight.w600)),
                        if (isSkeuomorphic) ...[
                          const SizedBox(width: 10),
                          SkeuomorphicPanel(
                            isFull: isFull,
                            borderRadius: BorderRadius.circular(12),
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                            child: Text(
                              '${_suggestedUsers.length} ready',
                              style: theme.textTheme.labelSmall?.copyWith(
                                color: AppColors.primaryPurple,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                    const SizedBox(height: 8),
                    ..._suggestedUsers.map(
                      (user) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _SuggestedUserTile(
                          user: user,
                          abTestVariant: widget.abTestVariant,
                          onTap: () {
                            Navigator.pop(context);
                            context.push('/chat', extra: {'userId': user.id});
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          );

        if (widget.abTestVariant.isLiquidGlass) {
          return LiquidGlassBottomSheet(
            variant: widget.abTestVariant.surfaceVariantName,
            child: content,
          );
        }

        if (isSkeuomorphic) {
          return SkeuomorphicPanel(
            isFull: widget.abTestVariant.isFullSkeumorphic,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
            child: content,
          );
        }

        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: content,
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

class _SuggestedUserTile extends StatelessWidget {
  final _User user;
  final ABTestVariant abTestVariant;
  final VoidCallback onTap;

  const _SuggestedUserTile({
    required this.user,
    required this.abTestVariant,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final tileBody = Row(
      children: [
        Stack(
          children: [
            NeuroAvatar(imageUrl: user.avatarUrl, name: user.name, size: 48),
            if (user.isOnline)
              Positioned(
                right: 0,
                bottom: 0,
                child: Container(
                  width: 12,
                  height: 12,
                  decoration: BoxDecoration(
                    color: AppColors.success,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: theme.scaffoldBackgroundColor,
                      width: 2,
                    ),
                  ),
                ),
              ),
          ],
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                user.name,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                '@${user.username}',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
        Icon(
          Icons.chat_bubble_outline,
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ],
    );

    if (abTestVariant.isSkeumorphic) {
      return SkeuomorphicPanel(
        isFull: abTestVariant.isFullSkeumorphic,
        borderRadius: BorderRadius.circular(18),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: onTap,
            borderRadius: BorderRadius.circular(18),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              child: tileBody,
            ),
          ),
        ),
      );
    }

    return ListTile(
      onTap: onTap,
      contentPadding: EdgeInsets.zero,
      leading: Stack(
        children: [
          NeuroAvatar(imageUrl: user.avatarUrl, name: user.name, size: 48),
          if (user.isOnline)
            Positioned(
              right: 0,
              bottom: 0,
              child: Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  color: AppColors.success,
                  shape: BoxShape.circle,
                  border: Border.all(color: theme.scaffoldBackgroundColor, width: 2),
                ),
              ),
            ),
        ],
      ),
      title: Text(
        user.name,
        style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
      ),
      subtitle: Text('@${user.username}'),
      trailing: const Icon(Icons.chat_bubble_outline),
    );
  }
}


/// Messages header — skeuomorphic uses Android-matching 4-button layout;
/// default keeps the original compact design with PopupMenuButton.
class _MessagesHeader extends StatelessWidget {
  final int unreadCount;
  final VoidCallback onNewMessage;
  final VoidCallback onSearch;
  final VoidCallback onPracticeCall;
  final VoidCallback onDeleteModeSettings;
  final AppLocalizations l10n;
  final ABTestVariant abTestVariant;

  const _MessagesHeader({
    required this.unreadCount,
    required this.onNewMessage,
    required this.onSearch,
    required this.onPracticeCall,
    required this.onDeleteModeSettings,
    required this.l10n,
    required this.abTestVariant,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isSkeuomorphic = abTestVariant.isSkeumorphic;

    // ── Title column (shared between both layouts) ──
    Widget titleSection() => Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Flexible(
                  child: Text(
                    l10n.messagesTitle,
                    style: theme.textTheme.headlineMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                      letterSpacing: -0.5,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
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
                  ? l10n
                      .get('youHaveUnreadMessages')
                      .replaceAll('{count}', unreadCount.toString())
                  : l10n.get('yourConversations'),
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        );

    // ── Shared action buttons (same structure for both themes) ──
    Widget actionButtons() => Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            _HeaderIconButton(
              icon: Icons.search_rounded,
              onPressed: onSearch,
              tooltip: 'Search',
              compact: true,
              abTestVariant: abTestVariant,
            ),
            const SizedBox(width: 4),
            _HeaderIconButton(
              icon: Icons.edit_square,
              onPressed: onNewMessage,
              tooltip: l10n.newMessage,
              isPrimary: true,
              compact: true,
              abTestVariant: abTestVariant,
            ),
            const SizedBox(width: 4),
            PopupMenuButton<String>(
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(minWidth: 36, minHeight: 36),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12)),
              onSelected: (value) {
                switch (value) {
                  case 'video':
                  case 'calls':
                    onPracticeCall();
                    break;
                  case 'options':
                    onDeleteModeSettings();
                    break;
                }
              },
              itemBuilder: (context) => const [
                PopupMenuItem(
                    value: 'video',
                    child: ListTile(
                        leading: Icon(Icons.videocam_outlined),
                        title: Text('Video Calls'),
                        dense: true,
                        contentPadding: EdgeInsets.zero)),
                PopupMenuItem(
                    value: 'calls',
                    child: ListTile(
                        leading: Icon(Icons.phone_rounded),
                        title: Text('Call History'),
                        dense: true,
                        contentPadding: EdgeInsets.zero)),
                PopupMenuItem(
                    value: 'options',
                    child: ListTile(
                        leading: Icon(Icons.tune_rounded),
                        title: Text('Message Options'),
                        dense: true,
                        contentPadding: EdgeInsets.zero)),
              ],
              child: isSkeuomorphic
                  ? SkeuomorphicIconButton(
                      icon: Icons.more_horiz_rounded,
                      onPressed: () {},
                      isFull: abTestVariant.isFullSkeumorphic,
                      size: 36,
                      iconSize: 20,
                    )
                  : Padding(
                      padding: const EdgeInsets.all(8),
                      child: Icon(
                        Icons.more_horiz_rounded,
                        size: 20,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
            ),
          ],
        );

    // ══════════════════════════════════════════════════════════════════════
    // SKEUOMORPHIC
    // ══════════════════════════════════════════════════════════════════════
    if (isSkeuomorphic) {
      final skeuContent = Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Expanded(child: titleSection()),
          actionButtons(),
        ],
      );

      return Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
        child: SkeuomorphicPanel(
          isFull: abTestVariant.isFullSkeumorphic,
          borderRadius: BorderRadius.circular(28),
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
          child: skeuContent,
        ),
      );
    }

    // ══════════════════════════════════════════════════════════════════════
    // DEFAULT
    // ══════════════════════════════════════════════════════════════════════
    final defaultContent = Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(child: titleSection()),
        actionButtons(),
      ],
    );

    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
      child: defaultContent,
    );
  }
}

// ---------------------------------------------------------------------------
// Animated unread badge — mirrors Android UnreadBadge exactly.
// ---------------------------------------------------------------------------
class _UnreadBadge extends StatefulWidget {
  final int count;
  const _UnreadBadge({required this.count});

  @override
  State<_UnreadBadge> createState() => _UnreadBadgeState();
}

class _UnreadBadgeState extends State<_UnreadBadge>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _scale;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    _scale = Tween<double>(begin: 1.0, end: 1.1).animate(
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
    return AnimatedBuilder(
      animation: _scale,
      builder: (context, child) =>
          Transform.scale(scale: _scale.value, child: child),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
          ),
          borderRadius: BorderRadius.circular(100), // fully rounded pill
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

// ---------------------------------------------------------------------------
// Header icon button — full-size (padding 10, icon 22, radius 12) for
// skeuomorphic; compact (padding 8, icon 20, radius 10) for default.
// ---------------------------------------------------------------------------
class _HeaderIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String? tooltip;
  final bool isPrimary;
  final bool compact;
  final ABTestVariant abTestVariant;

  const _HeaderIconButton({
    required this.icon,
    required this.onPressed,
    this.tooltip,
    this.isPrimary = false,
    this.compact = false,
    required this.abTestVariant,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;
    final double iconSz = compact ? 20 : 22;
    final double pad = compact ? 8 : 10;
    final double radius = compact ? 10 : 12;

    if (abTestVariant.isSkeumorphic) {
      return SkeuomorphicIconButton(
        icon: icon,
        onPressed: () {
          HapticFeedback.lightImpact();
          onPressed();
        },
        tooltip: tooltip,
        isFull: abTestVariant.isFullSkeumorphic,
        isPrimary: isPrimary,
        size: 42,       // padding 10 + icon 22 = 42
        iconSize: 22,
      );
    }

    Widget button = Material(
      color: isPrimary
          ? primaryColor.withValues(alpha: 0.15)
          : theme.colorScheme.surfaceContainerHighest,
      borderRadius: BorderRadius.circular(radius),
      child: InkWell(
        onTap: () {
          HapticFeedback.lightImpact();
          onPressed();
        },
        borderRadius: BorderRadius.circular(radius),
        child: Padding(
          padding: EdgeInsets.all(pad),
          child: Icon(
            icon,
            size: iconSz,
            color: isPrimary ? primaryColor : theme.colorScheme.onSurfaceVariant,
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
  final ABTestVariant abTestVariant;

  const _SearchBar({
    required this.controller,
    required this.onClose,
    required this.abTestVariant,
  });

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final searchField = TextField(
      controller: controller,
      autofocus: true,
      decoration: InputDecoration(
        hintText: l10n.searchByUsername,
        prefixIcon: const Icon(Icons.search),
        suffixIcon: IconButton(
          icon: const Icon(Icons.close_rounded),
          onPressed: onClose,
        ),
        border: InputBorder.none,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      ),
    );

    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
      child: abTestVariant.isSkeumorphic
          ? SkeuomorphicPanel(
              isFull: abTestVariant.isFullSkeumorphic,
              borderRadius: BorderRadius.circular(16),
              child: searchField,
            )
          : Container(
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(16),
              ),
              child: searchField,
            ),
    );
  }
}

/// Filter chips section matching Notifications style
class _FilterChipsSection extends StatelessWidget {
  final MessageFilter selectedFilter;
  final int unreadCount;
  final ValueChanged<MessageFilter> onFilterChanged;
  final ABTestVariant abTestVariant;

  const _FilterChipsSection({
    required this.selectedFilter,
    required this.unreadCount,
    required this.onFilterChanged,
    required this.abTestVariant,
  });

  @override
  Widget build(BuildContext context) {
    final filters = [
      (MessageFilter.all, 'All', Icons.chat_bubble_rounded, null),
      (MessageFilter.primary, 'Primary', Icons.star_rounded, null),
      (MessageFilter.calls, 'Calls', Icons.phone_rounded, null),
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
            abTestVariant: abTestVariant,
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
  final ABTestVariant abTestVariant;
  final VoidCallback onTap;

  const _FilterPill({
    required this.label,
    required this.icon,
    this.count,
    required this.isSelected,
    required this.abTestVariant,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;
    final pillContent = Row(
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
              color: primaryColor.withValues(alpha: 0.2),
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
    );

    if (abTestVariant.isSkeumorphic) {
      return GestureDetector(
        onTap: onTap,
        child: SkeuomorphicPanel(
          isFull: abTestVariant.isFullSkeumorphic,
          isActive: isSelected,
          borderRadius: BorderRadius.circular(24),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          child: pillContent,
        ),
      );
    }

    return Material(
      color: isSelected
          ? primaryColor.withValues(alpha: 0.15)
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
                    color: primaryColor.withValues(alpha: 0.3),
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
                    color: primaryColor.withValues(alpha: 0.2),
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
              color: color.withValues(alpha: 0.3),
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

/// Radio-style option tile for delete mode selection in the Messages tab
class _DeleteModeOptionTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final bool isSelected;
  final ABTestVariant abTestVariant;
  final VoidCallback onTap;

  const _DeleteModeOptionTile({
    required this.icon,
    required this.title,
    required this.description,
    required this.isSelected,
    required this.abTestVariant,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;
    final content = Padding(
      padding: const EdgeInsets.all(14),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: isSelected
                  ? primaryColor.withValues(alpha: 0.15)
                  : theme.colorScheme.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              icon,
              color: isSelected ? primaryColor : theme.colorScheme.onSurfaceVariant,
              size: 22,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                    color: isSelected ? primaryColor : null,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  description,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          Radio<bool>(
            value: true,
            groupValue: isSelected,
            onChanged: (_) => onTap(),
            activeColor: primaryColor,
          ),
        ],
      ),
    );

    if (abTestVariant.isSkeumorphic) {
      return GestureDetector(
        onTap: onTap,
        child: SkeuomorphicPanel(
          isFull: abTestVariant.isFullSkeumorphic,
          isActive: isSelected,
          borderRadius: BorderRadius.circular(16),
          child: content,
        ),
      );
    }

    return Material(
      color: isSelected
          ? primaryColor.withValues(alpha: 0.1)
          : theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: content,
      ),
    );
  }
}

/// Inline call history view shown when "Calls" filter pill is selected.
/// Matches the native Android InlineCallHistoryView.
class _InlineCallHistoryView extends StatefulWidget {
  final VoidCallback onOpenPracticeCall;

  const _InlineCallHistoryView({required this.onOpenPracticeCall});

  @override
  State<_InlineCallHistoryView> createState() => _InlineCallHistoryViewState();
}

class _InlineCallHistoryViewState extends State<_InlineCallHistoryView> {
  final _callService = WebRTCCallService.instance;
  final _contactsService = ContactsCallService.instance;
  bool _loadingContacts = false;

  @override
  void initState() {
    super.initState();
    _loadContacts();
  }

  Future<void> _loadContacts() async {
    if (_contactsService.hasPermission && _contactsService.callableContacts.isEmpty) {
      setState(() => _loadingContacts = true);
      await _contactsService.loadContacts();
      if (mounted) setState(() => _loadingContacts = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    final callHistory = _callService.callHistory;
    final contacts = _contactsService.callableContacts;

    return ListView(
      padding: const EdgeInsets.symmetric(vertical: 8),
      children: [
        // Permission prompt if not granted
        if (!_contactsService.hasPermission)
          _ContactsPermissionCard(
            onGrant: () async {
              HapticFeedback.selectionClick();
              final granted = await _contactsService.requestPermissionAndLoad();
              if (mounted && granted) setState(() {});
            },
          ),

        // Action buttons
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          child: Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: widget.onOpenPracticeCall,
                  icon: const Icon(Icons.headset, size: 18),
                  label: Text(l10n.practiceCall),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: FilledButton.tonalIcon(
                  onPressed: () {
                    HapticFeedback.selectionClick();
                  },
                  icon: const Icon(Icons.history, size: 18),
                  label: Text(l10n.get('callHistory')),
                ),
              ),
            ],
          ),
        ),

        if (callHistory.isEmpty && contacts.isEmpty && !_loadingContacts) ...[
          const SizedBox(height: 32),
          Center(
            child: Column(
              children: [
                Icon(
                  Icons.phone_outlined,
                  size: 64,
                  color: theme.colorScheme.primary.withValues(alpha: 0.6),
                ),
                const SizedBox(height: 16),
                Text(
                  l10n.get('noCallsYet'),
                  style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 8),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 48),
                  child: Text(
                    l10n.get('callContactsHint'),
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
              ],
            ),
          ),
        ],

        if (_loadingContacts)
          const Padding(
            padding: EdgeInsets.all(32),
            child: Center(child: CircularProgressIndicator()),
          ),

        // Contacts section
        if (contacts.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.fromLTRB(24, 20, 24, 8),
            child: Text(
              'Contacts',
              style: theme.textTheme.labelLarge?.copyWith(
                fontWeight: FontWeight.w600,
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
          ...contacts.take(20).map((contact) => _CallableContactRow(
            name: contact.name,
            subtitle: contact.isAppUser
                ? (contact.phoneNumber != null ? 'NeuroComet · ${contact.phoneNumber}' : 'NeuroComet')
                : (contact.phoneNumber ?? 'Contact'),
            photoBytes: contact.photoBytes,
            isAppUser: contact.isAppUser,
            onVoiceCall: () {
              HapticFeedback.selectionClick();
              _contactsService.startVoiceCall(contact);
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
              );
            },
            onVideoCall: () {
              HapticFeedback.selectionClick();
              _contactsService.startVideoCall(contact);
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
              );
            },
          )),
        ],

        // Call history section
        if (callHistory.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.fromLTRB(24, 20, 24, 8),
            child: Text(
              'Recent',
              style: theme.textTheme.labelMedium?.copyWith(
                fontWeight: FontWeight.w600,
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
          ...callHistory.map((entry) => _CallHistoryRow(
            entry: entry,
            onCallBack: () {
              HapticFeedback.selectionClick();
              WebRTCCallService.instance.startCall(
                recipientId: entry.recipientId,
                recipientName: entry.recipientName,
                recipientAvatar: entry.recipientAvatar,
                callType: entry.callType,
              );
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
              );
            },
          )),
        ],

        const SizedBox(height: 100),
      ],
    );
  }
}

/// Full-width contact row with avatar, name, phone, and call buttons.
class _CallableContactRow extends StatelessWidget {
  final String name;
  final String subtitle;
  final Uint8List? photoBytes;
  final bool isAppUser;
  final VoidCallback onVoiceCall;
  final VoidCallback onVideoCall;

  const _CallableContactRow({
    required this.name,
    required this.subtitle,
    this.photoBytes,
    this.isAppUser = false,
    required this.onVoiceCall,
    required this.onVideoCall,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return InkWell(
      onTap: onVoiceCall,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 10),
        child: Row(
          children: [
            // Avatar
            Stack(
              children: [
                if (photoBytes != null)
                  ClipOval(
                    child: Image.memory(photoBytes!, width: 48, height: 48, fit: BoxFit.cover),
                  )
                else
                  CircleAvatar(
                    radius: 24,
                    backgroundColor: theme.colorScheme.primaryContainer,
                    child: Text(
                      name.isNotEmpty ? name[0].toUpperCase() : '?',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.onPrimaryContainer,
                      ),
                    ),
                  ),
                if (isAppUser)
                  Positioned(
                    right: 0,
                    bottom: 0,
                    child: Container(
                      width: 16,
                      height: 16,
                      decoration: BoxDecoration(
                        color: AppColors.success,
                        shape: BoxShape.circle,
                        border: Border.all(color: theme.scaffoldBackgroundColor, width: 1.5),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(name, style: theme.textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w500)),
                  Text(subtitle, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                ],
              ),
            ),
            IconButton(
              icon: Icon(Icons.phone_outlined, color: theme.colorScheme.primary, size: 22),
              onPressed: onVoiceCall,
              constraints: const BoxConstraints(minWidth: 40, minHeight: 40),
            ),
            IconButton(
              icon: Icon(Icons.videocam_outlined, color: theme.colorScheme.primary, size: 22),
              onPressed: onVideoCall,
              constraints: const BoxConstraints(minWidth: 40, minHeight: 40),
            ),
          ],
        ),
      ),
    );
  }
}

/// Call history row — shows call direction, outcome, and callback button.
class _CallHistoryRow extends StatelessWidget {
  final CallHistoryEntry entry;
  final VoidCallback onCallBack;

  const _CallHistoryRow({required this.entry, required this.onCallBack});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isMissed = entry.outcome == CallOutcome.missed || entry.outcome == CallOutcome.declined;
    final callIcon = entry.callType == CallType.video ? Icons.videocam : Icons.phone;
    final outcomeColor = isMissed ? theme.colorScheme.error : theme.colorScheme.primary;

    final outcomeText = switch (entry.outcome) {
      CallOutcome.completed => entry.formattedDuration.isNotEmpty ? entry.formattedDuration : 'Connected',
      CallOutcome.missed => 'Missed',
      CallOutcome.declined => 'Declined',
      CallOutcome.noAnswer => 'No answer',
      CallOutcome.cancelled => 'Cancelled',
      CallOutcome.failed => 'Failed',
    };
    final directionLabel = entry.isOutgoing ? 'Outgoing' : 'Incoming';

    return InkWell(
      onTap: onCallBack,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 10),
        child: Row(
          children: [
            CircleAvatar(
              radius: 24,
              backgroundColor: theme.colorScheme.primaryContainer,
              child: Text(
                entry.recipientName.isNotEmpty ? entry.recipientName[0].toUpperCase() : '?',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.onPrimaryContainer,
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    entry.recipientName,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.w500,
                      color: isMissed ? theme.colorScheme.error : null,
                    ),
                  ),
                  Row(
                    children: [
                      Icon(callIcon, size: 14, color: outcomeColor),
                      const SizedBox(width: 4),
                      Text(
                        '$directionLabel · $outcomeText',
                        style: theme.textTheme.bodySmall?.copyWith(color: outcomeColor),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            IconButton(
              icon: Icon(callIcon, color: theme.colorScheme.primary),
              onPressed: onCallBack,
            ),
          ],
        ),
      ),
    );
  }
}

/// Card prompting user to grant contacts permission.
class _ContactsPermissionCard extends StatelessWidget {
  final VoidCallback onGrant;

  const _ContactsPermissionCard({required this.onGrant});

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Icon(Icons.contact_phone_outlined, size: 36, color: theme.colorScheme.primary),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.get('accessYourContacts'),
                  style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                ),
                Text(
                  l10n.get('accessContactsDirectly'),
                  style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          FilledButton.tonal(
            onPressed: onGrant,
            child: Text(l10n.get('allow')),
          ),
        ],
      ),
    );
  }
}


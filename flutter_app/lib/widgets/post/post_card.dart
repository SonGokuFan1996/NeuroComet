import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/dev_options.dart';
import '../../models/post.dart';
import '../../providers/theme_provider.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/m3e_design_system.dart';
import '../../services/supabase_service.dart';
import '../../services/moderation_service.dart';
import '../brand/liquid_glass.dart';

/// Emotional tone detection for neurodivergent-friendly content awareness.
/// Ported from Android M3EEmotionalTone for 1:1 parity.
enum EmotionalTone {
  neutral('💭', 'Neutral', Color(0xFF9E9E9E), Color(0xFF616161), false),
  happy('😊', 'Happy', Color(0xFF81C784), Color(0xFF2E7D32), false),
  excited('🎉', 'Excited', Color(0xFFFFD54F), Color(0xFFF57F17), false),
  sad('💙', 'Sad', Color(0xFF64B5F6), Color(0xFF1565C0), true),
  anxious('🫂', 'Anxious', Color(0xFFCE93D8), Color(0xFF7B1FA2), true),
  frustrated('😤', 'Frustrated', Color(0xFFFFAB91), Color(0xFFD84315), true),
  supportive('💜', 'Supportive', Color(0xFFB39DDB), Color(0xFF512DA8), false),
  question('❓', 'Question', Color(0xFF80DEEA), Color(0xFF00838F), false),
  celebration('✨', 'Celebration', Color(0xFFF48FB1), Color(0xFFC2185B), false),
  informative('📚', 'Informative', Color(0xFF90CAF9), Color(0xFF1976D2), false);

  final String emoji;
  final String label;
  final Color backgroundColor;
  final Color textColor;
  final bool showWarning;

  const EmotionalTone(
    this.emoji,
    this.label,
    this.backgroundColor,
    this.textColor,
    this.showWarning,
  );
}

/// Detects the emotional tone of post content for neurodivergent users.
/// Logic synced with detectM3EEmotionalTone in Android.
EmotionalTone detectEmotionalTone(String content) {
  final lowerContent = content.toLowerCase();

  // Celebration patterns
  if (lowerContent.contains('congrat') ||
      lowerContent.contains('achieved') ||
      lowerContent.contains('finally did it') ||
      lowerContent.contains('so proud') ||
      content.contains('🎉') ||
      content.contains('🎊') ||
      content.contains('🥳')) {
    return EmotionalTone.celebration;
  }

  // Excited/happy patterns
  if (lowerContent.contains('so happy') ||
      lowerContent.contains('amazing') ||
      lowerContent.contains('love this') ||
      lowerContent.contains('best day') ||
      content.contains('😊') ||
      content.contains('😄') ||
      content.contains('❤️')) {
    return EmotionalTone.happy;
  }

  // Excited patterns
  if (lowerContent.contains("can't wait") ||
      lowerContent.contains('so excited') ||
      lowerContent.contains('omg') ||
      content.contains('🔥') ||
      content.contains('⚡')) {
    return EmotionalTone.excited;
  }

  // Supportive patterns
  if (lowerContent.contains("you've got this") ||
      lowerContent.contains('proud of you') ||
      lowerContent.contains('here for you') ||
      lowerContent.contains('sending love') ||
      lowerContent.contains("you're not alone")) {
    return EmotionalTone.supportive;
  }

  // Question/help seeking patterns
  if (lowerContent.contains('does anyone') ||
      lowerContent.contains('how do i') ||
      lowerContent.contains('any tips') ||
      lowerContent.contains('help me') ||
      lowerContent.contains('advice') ||
      (content.contains('?') && content.length < 200)) {
    return EmotionalTone.question;
  }

  // Informative patterns
  if (lowerContent.contains('did you know') ||
      lowerContent.contains('research shows') ||
      lowerContent.contains('fun fact') ||
      lowerContent.contains('psa') ||
      lowerContent.contains('reminder')) {
    return EmotionalTone.informative;
  }

  // Sad/emotional patterns (with warning)
  if (lowerContent.contains('struggling') ||
      lowerContent.contains('hard day') ||
      lowerContent.contains('feeling down') ||
      lowerContent.contains('crying') ||
      (lowerContent.contains('miss') && lowerContent.contains('so much')) ||
      content.contains('😢') ||
      content.contains('😭') ||
      content.contains('💔')) {
    return EmotionalTone.sad;
  }

  // Check anxious patterns
  if (lowerContent.contains('anxious') ||
      lowerContent.contains('panic') ||
      lowerContent.contains('overwhelm') ||
      lowerContent.contains("can't cope") ||
      lowerContent.contains('sensory overload')) {
    return EmotionalTone.anxious;
  }

  // Frustrated/venting patterns
  if (lowerContent.contains('rant') ||
      lowerContent.contains('so frustrated') ||
      lowerContent.contains('hate when') ||
      lowerContent.contains('ugh') ||
      lowerContent.contains('annoyed') ||
      content.contains('😤') ||
      content.contains('🙄')) {
    return EmotionalTone.frustrated;
  }

  return EmotionalTone.neutral;
}

/// Bubbly Post Card matching Kotlin version with all animations
class PostCard extends ConsumerStatefulWidget {
  final Post post;
  final VoidCallback? onLike;
  final VoidCallback? onComment;
  final VoidCallback? onShare;
  final VoidCallback? onProfileTap;
  final VoidCallback? onBookmark;
  final VoidCallback? onMoreOptions;
  final VoidCallback? onDelete;
  final VoidCallback? onReport;
  final Function(String)? onMentionTap;
  final Function(String)? onHashtagTap;
  final int animationIndex;
  final bool isFollowing;
  final ABTestVariant abTestVariant;

  const PostCard({
    super.key,
    required this.post,
    this.onLike,
    this.onComment,
    this.onShare,
    this.onProfileTap,
    this.onBookmark,
    this.onMoreOptions,
    this.onDelete,
    this.onReport,
    this.onMentionTap,
    this.onHashtagTap,
    this.animationIndex = 0,
    this.isFollowing = false,
    this.abTestVariant = ABTestVariant.control,
  });

  @override
  ConsumerState<PostCard> createState() => _PostCardState();
}

class _PostCardState extends ConsumerState<PostCard>
    with SingleTickerProviderStateMixin {
  AnimationController? _animationController;
  Animation<double>? _fadeAnimation;
  Animation<double>? _scaleAnimation;
  Animation<Offset>? _slideAnimation;
  bool _isBookmarked = false;
  bool _animationsInitialized = false;

  @override
  void initState() {
    super.initState();
    _isBookmarked = widget.post.isBookmarked;
  }

  void _initAnimations() {
    if (_animationsInitialized) return;
    _animationsInitialized = true;

    final reducedMotion = ref.read(reducedMotionProvider);
    if (reducedMotion) return;

    _animationController = AnimationController(
      duration: M3EDesignSystem.animNormal,
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _animationController!, curve: Curves.easeOut),
    );

    _scaleAnimation = Tween<double>(begin: 0.95, end: 1).animate(
      CurvedAnimation(parent: _animationController!, curve: Curves.easeOut),
    );

    _slideAnimation =
        Tween<Offset>(begin: const Offset(0, 0.05), end: Offset.zero).animate(
          CurvedAnimation(parent: _animationController!, curve: Curves.easeOut),
        );

    // Staggered animation delay matching Android
    final delay = widget.animationIndex * 50;
    Future.delayed(Duration(milliseconds: delay), () {
      if (mounted && _animationController != null) {
        _animationController!.forward();
      }
    });
  }

  @override
  void dispose() {
    _animationController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final emotionalTone = detectEmotionalTone(widget.post.content);
    final reducedMotion = ref.watch(reducedMotionProvider);
    final isCompact = widget.abTestVariant == ABTestVariant.compactCards;
    final isLiquidGlass = widget.abTestVariant.isLiquidGlass;
    final isSkeumorphic = widget.abTestVariant.isSkeumorphic;
    final isFullSkeumorphic = widget.abTestVariant.isFullSkeumorphic;

    if (!reducedMotion && !_animationsInitialized) {
      _initAnimations();
    }

    // Compact variant: Infinity-for-Reddit style flat list item
    if (isCompact) {
      return _buildCompactLayout(context, theme, emotionalTone);
    }

    // Normal card layout for control / liquidGlass / boldTypography
    final cardPadding = isSkeumorphic ? 18.0 : M3EDesignSystem.spacingMD;
    final cardMarginH = isLiquidGlass ? 0.0 : M3EDesignSystem.spacingSM;
    final cardMarginV = isLiquidGlass ? 0.0 : M3EDesignSystem.spacingXS;
    final baseSurface = Color.alphaBlend(
      theme.colorScheme.primary.withValues(
        alpha: isFullSkeumorphic ? 0.12 : 0.06,
      ),
      theme.colorScheme.surface,
    );
    final shape = BorderRadius.circular(isFullSkeumorphic ? 28 : 24);

    final cardWidget = Card(
      margin: EdgeInsets.symmetric(
        horizontal: cardMarginH,
        vertical: cardMarginV,
      ),
      elevation: 0,
      shadowColor: Colors.transparent,
      shape: RoundedRectangleBorder(
        borderRadius: shape,
        side: isLiquidGlass
            ? BorderSide.none
            : isSkeumorphic
            ? BorderSide(
                color: Colors.white.withValues(
                  alpha: theme.brightness == Brightness.dark ? 0.08 : 0.32,
                ),
                width: 1,
              )
            : BorderSide(
                color: theme.colorScheme.outlineVariant.withValues(alpha: 0.18),
                width: 0.5,
              ),
      ),
      color: isLiquidGlass || isSkeumorphic
          ? Colors.transparent
          : (widget.post.backgroundColor != null
                ? Color(widget.post.backgroundColor!)
                : theme.colorScheme.surfaceContainerLow),
      child: DecoratedBox(
        decoration: BoxDecoration(
          borderRadius: shape,
          gradient: isSkeumorphic
              ? LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Color.alphaBlend(
                      Colors.white.withValues(
                        alpha: theme.brightness == Brightness.dark
                            ? 0.08
                            : 0.20,
                      ),
                      baseSurface,
                    ),
                    baseSurface,
                    Color.alphaBlend(
                      theme.colorScheme.primary.withValues(
                        alpha: isFullSkeumorphic ? 0.18 : 0.10,
                      ),
                      baseSurface,
                    ),
                  ],
                )
              : null,
          boxShadow: isLiquidGlass
              ? null
              : isSkeumorphic
              ? [
                  BoxShadow(
                    color: Colors.black.withValues(
                      alpha: theme.brightness == Brightness.dark ? 0.24 : 0.10,
                    ),
                    blurRadius: isFullSkeumorphic ? 22 : 14,
                    offset: Offset(0, isFullSkeumorphic ? 14 : 8),
                  ),
                  BoxShadow(
                    color: Colors.white.withValues(
                      alpha: theme.brightness == Brightness.dark ? 0.03 : 0.48,
                    ),
                    blurRadius: isFullSkeumorphic ? 12 : 8,
                    offset: const Offset(-4, -4),
                  ),
                ]
              : [
                  BoxShadow(
                    color: theme.colorScheme.shadow.withValues(alpha: 0.08),
                    blurRadius: 10,
                    offset: const Offset(0, 2),
                  ),
                  BoxShadow(
                    color: theme.colorScheme.shadow.withValues(alpha: 0.03),
                    blurRadius: 3,
                    offset: const Offset(0, 1),
                  ),
                ],
        ),
        child: Padding(
          padding: EdgeInsets.symmetric(
            horizontal: cardPadding,
            vertical: 14.0,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildHeader(context),
              const SizedBox(height: M3EDesignSystem.spacingSM),
              _buildContent(context),
              if (widget.post.mediaUrls != null &&
                  widget.post.mediaUrls!.isNotEmpty)
                _buildMedia(context),
              const SizedBox(height: M3EDesignSystem.spacingSM),
              _buildActions(context),
              if (emotionalTone != EmotionalTone.neutral) ...[
                const SizedBox(height: M3EDesignSystem.spacingXS),
                _M3EEmotionalToneTag(tone: emotionalTone),
              ],
            ],
          ),
        ),
      ), // DecoratedBox
    );

    if (reducedMotion || _animationController == null) {
      return cardWidget;
    }

    return AnimatedBuilder(
      animation: _animationController!,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation!,
          child: SlideTransition(
            position: _slideAnimation!,
            child: ScaleTransition(scale: _scaleAnimation!, child: child),
          ),
        );
      },
      child: cardWidget,
    );
  }

  /// Infinity-for-Reddit style compact layout: flat, dense, no card decoration.
  Widget _buildCompactLayout(
    BuildContext context,
    ThemeData theme,
    EmotionalTone emotionalTone,
  ) {
    final post = widget.post;
    final hasMedia = post.mediaUrls != null && post.mediaUrls!.isNotEmpty;

    return Container(
      padding: const EdgeInsets.fromLTRB(14, 12, 14, 4),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        border: Border(
          bottom: BorderSide(
            color: theme.dividerColor.withValues(alpha: 0.12),
            width: 0.5,
          ),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        GestureDetector(
                          onTap: widget.onProfileTap,
                          child: CircleAvatar(
                            radius: 10,
                            backgroundColor:
                                theme.colorScheme.surfaceContainerHighest,
                            backgroundImage: post.authorAvatarUrl != null
                                ? NetworkImage(post.authorAvatarUrl!)
                                : null,
                            child: post.authorAvatarUrl == null
                                ? Icon(
                                    Icons.person,
                                    size: 12,
                                    color: theme.colorScheme.onSurfaceVariant,
                                  )
                                : null,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: GestureDetector(
                            onTap: widget.onProfileTap,
                            child: Text(
                              '${post.authorName} • ${_formatTimeAgo(post.createdAt)}${post.locationTag != null ? ' 📍 ${post.locationTag}' : ''}',
                              style: theme.textTheme.labelSmall?.copyWith(
                                color: theme.colorScheme.onSurfaceVariant,
                                fontWeight: FontWeight.w600,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(
                      post.content,
                      style: theme.textTheme.bodyMedium?.copyWith(height: 1.3),
                      maxLines: 4,
                      overflow: TextOverflow.ellipsis,
                    ),
                    if (emotionalTone != EmotionalTone.neutral) ...[
                      const SizedBox(height: 6),
                      _M3EEmotionalToneTag(tone: emotionalTone),
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  GestureDetector(
                    onTap: () => _showOptionsMenu(context),
                    child: Padding(
                      padding: const EdgeInsets.only(bottom: 8.0, left: 8.0),
                      child: Icon(
                        Icons.more_vert,
                        size: 18,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ),
                  if (hasMedia)
                    ClipRRect(
                      borderRadius: BorderRadius.circular(6),
                      child: Image.network(
                        post.mediaUrls!.first,
                        width: 70,
                        height: 70,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Container(
                          width: 70,
                          height: 70,
                          color: theme.colorScheme.surfaceContainerHighest,
                          child: Icon(
                            Icons.image,
                            size: 20,
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ),
                    ),
                ],
              ),
            ],
          ),
          Row(
            children: [
              _AnimatedLikeButton(
                isLiked: post.isLiked,
                likeCount: post.likeCount,
                onTap: widget.onLike,
              ),
              const SizedBox(width: 8),
              _ActionButton(
                icon: Icons.chat_bubble_outline,
                label: post.commentCount > 0
                    ? post.commentCount.toString()
                    : '',
                onTap: widget.onComment,
              ),
              const SizedBox(width: 8),
              _ActionButton(
                icon: Icons.share_outlined,
                label: '',
                onTap: widget.onShare,
              ),
              const Spacer(),
              IconButton(
                icon: Icon(
                  _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                  size: 20,
                  color: _isBookmarked
                      ? theme.colorScheme.primary
                      : theme.colorScheme.onSurfaceVariant,
                ),
                padding: EdgeInsets.zero,
                visualDensity: VisualDensity.compact,
                onPressed: () {
                  HapticFeedback.lightImpact();
                  final wasBookmarked = _isBookmarked;
                  setState(() => _isBookmarked = !_isBookmarked);
                  widget.onBookmark?.call();
                  _syncBookmark(wasBookmarked);
                },
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    final theme = Theme.of(context);
    final isBold = widget.abTestVariant == ABTestVariant.boldTypography;
    final isSkeumorphic = widget.abTestVariant.isSkeumorphic;
    final isFullSkeumorphic = widget.abTestVariant.isFullSkeumorphic;
    final avatarSize = M3EDesignSystem.avatarMD;
    const ringPadding = 2.5;

    return Row(
      children: [
        // Avatar with gradient ring
        GestureDetector(
          onTap: widget.onProfileTap,
          child: Container(
            width: avatarSize + ringPadding * 2,
            height: avatarSize + ringPadding * 2,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: isSkeumorphic
                    ? [
                        theme.colorScheme.surface,
                        Color.alphaBlend(
                          theme.colorScheme.primary.withValues(
                            alpha: isFullSkeumorphic ? 0.22 : 0.14,
                          ),
                          theme.colorScheme.surface,
                        ),
                      ]
                    : const [AppColors.primaryPurple, AppColors.secondaryTeal],
              ),
              boxShadow: isSkeumorphic
                  ? [
                      BoxShadow(
                        color: Colors.black.withValues(
                          alpha: theme.brightness == Brightness.dark
                              ? 0.16
                              : 0.08,
                        ),
                        blurRadius: isFullSkeumorphic ? 14 : 8,
                        offset: Offset(0, isFullSkeumorphic ? 8 : 5),
                      ),
                    ]
                  : null,
            ),
            padding: const EdgeInsets.all(ringPadding),
            child: CircleAvatar(
              backgroundColor: theme.colorScheme.surface,
              backgroundImage: widget.post.authorAvatarUrl != null
                  ? NetworkImage(widget.post.authorAvatarUrl!)
                  : null,
              child: widget.post.authorAvatarUrl == null
                  ? const Icon(Icons.account_circle, size: 36)
                  : null,
            ),
          ),
        ),
        const SizedBox(width: M3EDesignSystem.spacingSM),

        // User info
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Flexible(
                    child: GestureDetector(
                      onTap: widget.onProfileTap,
                      child: Text(
                        widget.post.authorName,
                        style:
                            (isBold
                                    ? theme.textTheme.titleMedium
                                    : theme.textTheme.titleSmall)
                                ?.copyWith(fontWeight: FontWeight.bold),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ),
                  if (widget.isFollowing) ...[
                    const SizedBox(width: M3EDesignSystem.spacingXS),
                    const _M3EFollowingChip(),
                  ],
                ],
              ),
              const SizedBox(height: 2),
              Text(
                _formatTimeAgo(widget.post.createdAt),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              if (widget.post.locationTag != null &&
                  widget.post.locationTag!.isNotEmpty) ...[
                const SizedBox(height: 2),
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.location_on_outlined,
                      size: 14,
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                    const SizedBox(width: 2),
                    Flexible(
                      child: Text(
                        widget.post.locationTag!,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),

        // Menu button — use compact density to save space
        isSkeumorphic
            ? _SkeuomorphicIconButton(
                icon: Icons.more_vert,
                onPressed: () => _showOptionsMenu(context),
                isFull: isFullSkeumorphic,
              )
            : IconButton(
                icon: Icon(
                  Icons.more_vert,
                  size: 22,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                visualDensity: VisualDensity.compact,
                onPressed: () => _showOptionsMenu(context),
              ),
      ],
    );
  }

  Widget _buildContent(BuildContext context) {
    final isBold = widget.abTestVariant == ABTestVariant.boldTypography;
    final isCompact = widget.abTestVariant == ABTestVariant.compactCards;
    return _LinkedText(
      text: widget.post.content,
      onMentionTap: widget.onMentionTap,
      onHashtagTap: widget.onHashtagTap,
      isBold: isBold,
      maxLines: isCompact ? 3 : null,
    );
  }

  Widget _buildMedia(BuildContext context) {
    final mediaUrls = widget.post.mediaUrls!;

    return Container(
      margin: const EdgeInsets.only(top: M3EDesignSystem.spacingSM),
      height: 220,
      child: _MediaCarousel(mediaUrls: mediaUrls),
    );
  }

  Widget _buildActions(BuildContext context) {
    final theme = Theme.of(context);
    final isSkeumorphic = widget.abTestVariant.isSkeumorphic;
    final isFullSkeumorphic = widget.abTestVariant.isFullSkeumorphic;

    return Row(
      children: [
        Flexible(
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Like button with heart animation
              Flexible(
                child: _AnimatedLikeButton(
                  isLiked: widget.post.isLiked,
                  likeCount: widget.post.likeCount,
                  onTap: widget.onLike,
                  isSkeumorphic: isSkeumorphic,
                  isFull: isFullSkeumorphic,
                ),
              ),
              const SizedBox(width: 4),

              // Comment button
              Flexible(
                child: _ActionButton(
                  icon: Icons.chat_bubble_outline,
                  label: widget.post.commentCount > 0
                      ? widget.post.commentCount.toString()
                      : '',
                  onTap: widget.onComment,
                  isSkeumorphic: isSkeumorphic,
                  isFull: isFullSkeumorphic,
                ),
              ),
              const SizedBox(width: 4),

              // Share button
              Flexible(
                child: _ActionButton(
                  icon: Icons.share_outlined,
                  label: '',
                  onTap: widget.onShare,
                  isSkeumorphic: isSkeumorphic,
                  isFull: isFullSkeumorphic,
                ),
              ),
            ],
          ),
        ),

        // Bookmark button
        isSkeumorphic
            ? _SkeuomorphicIconButton(
                icon: _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                onPressed: () {
                  HapticFeedback.lightImpact();
                  final wasBookmarked = _isBookmarked;
                  setState(() => _isBookmarked = !_isBookmarked);
                  widget.onBookmark?.call();
                  _syncBookmark(wasBookmarked);
                },
                isFull: isFullSkeumorphic,
                iconColor: _isBookmarked
                    ? theme.colorScheme.primary
                    : theme.colorScheme.onSurfaceVariant,
              )
            : IconButton(
                icon: Icon(
                  _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                  color: _isBookmarked ? theme.colorScheme.primary : null,
                  size: 22,
                ),
                visualDensity: VisualDensity.compact,
                onPressed: () {
                  HapticFeedback.lightImpact();
                  final wasBookmarked = _isBookmarked;
                  setState(() => _isBookmarked = !_isBookmarked);
                  widget.onBookmark?.call();
                  // Persist to Supabase, revert on failure
                  _syncBookmark(wasBookmarked);
                },
              ),
      ],
    );
  }

  void _showOptionsMenu(BuildContext context) {
    final sheetBody = SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: Icon(
              _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
            ),
            title: Text(_isBookmarked ? 'Unsave' : 'Save'),
            onTap: () {
              Navigator.pop(context);
              _handleMenuAction('bookmark');
            },
          ),
          ListTile(
            leading: const Icon(Icons.content_copy),
            title: const Text('Copy link'),
            onTap: () {
              Navigator.pop(context);
              _handleMenuAction('copy');
            },
          ),
          ListTile(
            leading: const Icon(Icons.share),
            title: const Text('Share'),
            onTap: () {
              Navigator.pop(context);
              _handleMenuAction('share');
            },
          ),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.flag, color: Colors.red),
            title: const Text('Report', style: TextStyle(color: Colors.red)),
            onTap: () {
              Navigator.pop(context);
              widget.onReport?.call();
            },
          ),
          if (widget.onDelete != null)
            ListTile(
              leading: const Icon(Icons.delete, color: Colors.red),
              title: const Text(
                'Delete',
                style: TextStyle(color: Colors.red),
              ),
              onTap: () {
                Navigator.pop(context);
                widget.onDelete?.call();
              },
            ),
        ],
      ),
    );

    if (widget.abTestVariant.usesExperimentalSurfaceChrome) {
      showLiquidGlassBottomSheet(
        context: context,
        variant: widget.abTestVariant.surfaceVariantName,
        builder: (_) => sheetBody,
      );
      return;
    }

    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: M3EDesignSystem.shapeBottomSheet,
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: Icon(
                _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
              ),
              title: Text(_isBookmarked ? 'Unsave' : 'Save'),
              onTap: () {
                Navigator.pop(context);
                _handleMenuAction('bookmark');
              },
            ),
            ListTile(
              leading: const Icon(Icons.content_copy),
              title: const Text('Copy link'),
              onTap: () {
                Navigator.pop(context);
                _handleMenuAction('copy');
              },
            ),
            ListTile(
              leading: const Icon(Icons.share),
              title: const Text('Share'),
              onTap: () {
                Navigator.pop(context);
                _handleMenuAction('share');
              },
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.flag, color: Colors.red),
              title: const Text('Report', style: TextStyle(color: Colors.red)),
              onTap: () {
                Navigator.pop(context);
                widget.onReport?.call();
              },
            ),
            if (widget.onDelete != null)
              ListTile(
                leading: const Icon(Icons.delete, color: Colors.red),
                title: const Text(
                  'Delete',
                  style: TextStyle(color: Colors.red),
                ),
                onTap: () {
                  Navigator.pop(context);
                  widget.onDelete?.call();
                },
              ),
          ],
        ),
      ),
    );
  }

  void _handleMenuAction(String action) {
    switch (action) {
      case 'bookmark':
        final wasBookmarked = _isBookmarked;
        setState(() => _isBookmarked = !_isBookmarked);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(_isBookmarked ? 'Saved!' : 'Removed')),
        );
        widget.onBookmark?.call();
        _syncBookmark(wasBookmarked);
        break;
      case 'copy':
        Clipboard.setData(
          ClipboardData(text: 'https://getneurocomet.com/post/${widget.post.id}'),
        );
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Link copied!')));
        break;
      case 'share':
        widget.onShare?.call();
        break;
    }
  }

  /// Persist bookmark toggle to Supabase, revert on failure
  Future<void> _syncBookmark(bool wasBookmarked) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) {
        return;
      }
      final result = _isBookmarked
          ? await SupabaseService.bookmarkPost(widget.post.id)
          : await SupabaseService.removeBookmark(widget.post.id);
      if (result['success'] != true && mounted) {
        // Revert on failure
        setState(() => _isBookmarked = wasBookmarked);
      }
    } catch (e) {
      if (mounted) setState(() => _isBookmarked = wasBookmarked);
    }
  }

  String _formatTimeAgo(DateTime? dateTime) {
    if (dateTime == null) return 'Just now';
    final diff = DateTime.now().difference(dateTime);
    if (diff.inDays > 0) return '${diff.inDays}d ago';
    if (diff.inHours > 0) return '${diff.inHours}h ago';
    if (diff.inMinutes > 0) return '${diff.inMinutes}m ago';
    return 'Just now';
  }
}

class _M3EEmotionalToneTag extends StatelessWidget {
  final EmotionalTone tone;

  const _M3EEmotionalToneTag({required this.tone});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            tone.backgroundColor.withValues(alpha: 0.22),
            tone.backgroundColor.withValues(alpha: 0.08),
          ],
        ),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: tone.backgroundColor.withValues(alpha: 0.30),
          width: 0.5,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(tone.emoji, style: const TextStyle(fontSize: 12)),
          const SizedBox(width: 4),
          Text(
            tone.label,
            style: TextStyle(
              fontSize: 11,
              color: tone.textColor,
              fontWeight: FontWeight.bold,
            ),
          ),
          if (tone.showWarning) ...[
            const SizedBox(width: 4),
            Icon(Icons.info, size: 14, color: tone.textColor),
          ],
        ],
      ),
    );
  }
}

class _M3EFollowingChip extends StatelessWidget {
  const _M3EFollowingChip();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: theme.colorScheme.primaryContainer,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: theme.colorScheme.onPrimaryContainer.withValues(alpha: 0.15),
          width: 0.5,
        ),
      ),
      child: Text(
        'Following',
        style: theme.textTheme.labelSmall?.copyWith(
          color: theme.colorScheme.onPrimaryContainer,
          fontSize: 10,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

/// Animated like button with heart burst effect
class _AnimatedLikeButton extends StatefulWidget {
  final bool isLiked;
  final int likeCount;
  final VoidCallback? onTap;
  final bool isSkeumorphic;
  final bool isFull;

  const _AnimatedLikeButton({
    required this.isLiked,
    required this.likeCount,
    this.onTap,
    this.isSkeumorphic = false,
    this.isFull = false,
  });

  @override
  State<_AnimatedLikeButton> createState() => _AnimatedLikeButtonState();
}

class _AnimatedLikeButtonState extends State<_AnimatedLikeButton>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );
    _scaleAnimation = TweenSequence<double>([
      TweenSequenceItem(tween: Tween(begin: 1.0, end: 1.3), weight: 50),
      TweenSequenceItem(tween: Tween(begin: 1.3, end: 1.0), weight: 50),
    ]).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  void didUpdateWidget(_AnimatedLikeButton oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isLiked && !oldWidget.isLiked) {
      _controller.forward(from: 0);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = widget.isLiked
        ? const Color(0xFFE91E63)
        : theme.colorScheme.onSurfaceVariant;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: widget.isFull ? 0.14 : 0.08),
      theme.colorScheme.surface,
    );

    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        onTap: () {
          _controller.forward(from: 0);
          widget.onTap?.call();
        },
        borderRadius: BorderRadius.circular(20),
        child: Ink(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          decoration: widget.isSkeumorphic
              ? BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      Color.alphaBlend(
                        Colors.white.withValues(
                          alpha: theme.brightness == Brightness.dark
                              ? 0.06
                              : 0.22,
                        ),
                        base,
                      ),
                      base,
                    ],
                  ),
                  border: Border.all(
                    color: Colors.white.withValues(
                      alpha: theme.brightness == Brightness.dark ? 0.06 : 0.30,
                    ),
                  ),
                )
              : null,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              ScaleTransition(
                scale: _scaleAnimation,
                child: Icon(
                  widget.isLiked ? Icons.favorite : Icons.favorite_border,
                  size: 22,
                  color: color,
                ),
              ),
              if (widget.likeCount > 0) ...[
                const SizedBox(width: 4),
                Text(
                  _formatCount(widget.likeCount),
                  style: TextStyle(fontSize: 13, color: color),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ],
          ),
        ),
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
}

/// Action button for comment/share
class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback? onTap;
  final bool isSkeumorphic;
  final bool isFull;

  const _ActionButton({
    required this.icon,
    required this.label,
    this.onTap,
    this.isSkeumorphic = false,
    this.isFull = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = theme.colorScheme.onSurfaceVariant;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.12 : 0.06),
      theme.colorScheme.surface,
    );

    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Ink(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: isSkeumorphic
              ? BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      Color.alphaBlend(
                        Colors.white.withValues(
                          alpha: theme.brightness == Brightness.dark
                              ? 0.06
                              : 0.20,
                        ),
                        base,
                      ),
                      base,
                    ],
                  ),
                  border: Border.all(
                    color: Colors.white.withValues(
                      alpha: theme.brightness == Brightness.dark ? 0.06 : 0.24,
                    ),
                  ),
                )
              : null,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, size: 20, color: color),
              if (label.isNotEmpty) ...[
                const SizedBox(width: 4),
                Text(
                  label,
                  style: theme.textTheme.bodySmall?.copyWith(color: color),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _SkeuomorphicIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final bool isFull;
  final Color? iconColor;

  const _SkeuomorphicIconButton({
    required this.icon,
    required this.onPressed,
    required this.isFull,
    this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.14 : 0.08),
      theme.colorScheme.surface,
    );

    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(18),
      child: InkWell(
        onTap: onPressed,
        borderRadius: BorderRadius.circular(18),
        child: Ink(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                Color.alphaBlend(
                  Colors.white.withValues(
                    alpha: theme.brightness == Brightness.dark ? 0.06 : 0.20,
                  ),
                  base,
                ),
                base,
              ],
            ),
            border: Border.all(
              color: Colors.white.withValues(
                alpha: theme.brightness == Brightness.dark ? 0.06 : 0.28,
              ),
            ),
          ),
          child: Icon(
            icon,
            size: 20,
            color: iconColor ?? theme.colorScheme.onSurfaceVariant,
          ),
        ),
      ),
    );
  }
}

/// Linked text that parses @mentions and #hashtags
class _LinkedText extends StatelessWidget {
  final String text;
  final Function(String)? onMentionTap;
  final Function(String)? onHashtagTap;
  final bool isBold;
  final int? maxLines;

  const _LinkedText({
    required this.text,
    this.onMentionTap,
    this.onHashtagTap,
    this.isBold = false,
    this.maxLines,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final baseStyle = isBold
        ? theme.textTheme.bodyLarge?.copyWith(
            height: 1.5,
            fontWeight: FontWeight.w700,
            fontSize: 16,
          )
        : theme.textTheme.bodyMedium?.copyWith(height: 1.5);
    final spans = _parseText(text, theme, baseStyle);

    return Text.rich(
      TextSpan(children: spans),
      style: baseStyle,
      maxLines: maxLines,
      overflow: maxLines != null ? TextOverflow.ellipsis : null,
    );
  }

  List<InlineSpan> _parseText(
    String text,
    ThemeData theme,
    TextStyle? baseStyle,
  ) {
    final spans = <InlineSpan>[];
    final mentionRegex = RegExp(r'@\w+');
    final hashtagRegex = RegExp(r'#\w+');

    int lastEnd = 0;

    // Find all matches
    final allMatches = <_LinkMatch>[];

    for (final match in mentionRegex.allMatches(text)) {
      allMatches.add(
        _LinkMatch(match.start, match.end, match.group(0)!, _LinkType.mention),
      );
    }
    for (final match in hashtagRegex.allMatches(text)) {
      allMatches.add(
        _LinkMatch(match.start, match.end, match.group(0)!, _LinkType.hashtag),
      );
    }

    // Sort by position
    allMatches.sort((a, b) => a.start.compareTo(b.start));

    for (final match in allMatches) {
      if (match.start > lastEnd) {
        spans.add(TextSpan(text: text.substring(lastEnd, match.start)));
      }

      final linkColor = match.type == _LinkType.mention
          ? theme.colorScheme.primary
          : theme.colorScheme.secondary;

      spans.add(
        WidgetSpan(
          alignment: PlaceholderAlignment.baseline,
          baseline: TextBaseline.alphabetic,
          child: GestureDetector(
            onTap: () {
              if (match.type == _LinkType.mention) {
                onMentionTap?.call(match.text.substring(1));
              } else {
                onHashtagTap?.call(match.text.substring(1));
              }
            },
            child: Text(
              match.text,
              style:
                  baseStyle?.copyWith(
                    color: linkColor,
                    fontWeight: FontWeight.w600,
                  ) ??
                  TextStyle(color: linkColor, fontWeight: FontWeight.w600),
            ),
          ),
        ),
      );

      lastEnd = match.end;
    }

    if (lastEnd < text.length) {
      spans.add(TextSpan(text: text.substring(lastEnd)));
    }

    return spans;
  }
}

enum _LinkType { mention, hashtag }

class _LinkMatch {
  final int start;
  final int end;
  final String text;
  final _LinkType type;

  _LinkMatch(this.start, this.end, this.text, this.type);
}

class _MediaCarousel extends StatefulWidget {
  final List<String> mediaUrls;

  const _MediaCarousel({required this.mediaUrls});

  @override
  State<_MediaCarousel> createState() => _MediaCarouselState();
}

class _MediaCarouselState extends State<_MediaCarousel> {
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Stack(
      children: [
        PageView.builder(
          itemCount: widget.mediaUrls.length,
          onPageChanged: (index) => setState(() => _currentIndex = index),
          itemBuilder: (context, index) {
            return ClipRRect(
              borderRadius: M3EDesignSystem.shapeMedium,
              child: Image.network(
                widget.mediaUrls[index],
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  color: theme.colorScheme.surfaceContainerHighest,
                  child: const Icon(Icons.broken_image, size: 48),
                ),
              ),
            );
          },
        ),

        // Page indicator
        if (widget.mediaUrls.length > 1)
          Positioned(
            bottom: 12,
            left: 0,
            right: 0,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(widget.mediaUrls.length, (index) {
                final isSelected = index == _currentIndex;
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  curve: Curves.easeOutCubic,
                  width: isSelected ? 16 : 6,
                  height: 6,
                  margin: const EdgeInsets.symmetric(horizontal: 2.5),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(3),
                    color: isSelected
                        ? theme.colorScheme.primary
                        : theme.colorScheme.onSurfaceVariant.withValues(
                            alpha: 0.35,
                          ),
                    boxShadow: isSelected
                        ? [
                            BoxShadow(
                              color: theme.colorScheme.primary.withValues(
                                alpha: 0.3,
                              ),
                              blurRadius: 4,
                            ),
                          ]
                        : null,
                  ),
                );
              }),
            ),
          ),
      ],
    );
  }
}

/// Report post dialog with neurodivergent-friendly options.
/// Includes clear, specific options to reduce decision fatigue.
class ReportPostDialog extends StatefulWidget {
  final String postId;
  final VoidCallback? onDismiss;
  final Function(String reason)? onReport;

  const ReportPostDialog({
    super.key,
    required this.postId,
    this.onDismiss,
    this.onReport,
  });

  @override
  State<ReportPostDialog> createState() => _ReportPostDialogState();
}

class _ReportPostDialogState extends State<ReportPostDialog> {
  String? _selectedReason;
  final _additionalInfoController = TextEditingController();

  static const _reportReasons = [
    ('🚫', 'Spam or scam'),
    ('😠', 'Harassment or bullying'),
    ('⚠️', 'Harmful misinformation'),
    ('🔞', 'Inappropriate content'),
    ('💔', 'Self-harm or suicide content'),
    ('🎭', 'Impersonation'),
    ('📢', 'Hate speech'),
    ('🤖', 'Bot or fake account'),
    ('❓', 'Something else'),
  ];

  @override
  void dispose() {
    _additionalInfoController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AlertDialog(
      title: Text(
        'Report Post',
        style: theme.textTheme.titleLarge?.copyWith(
          fontWeight: FontWeight.bold,
        ),
      ),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Why are you reporting this post?',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'Your report is anonymous and helps keep our community safe.',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant.withValues(
                  alpha: 0.7,
                ),
              ),
            ),
            const SizedBox(height: 16),
            ..._reportReasons.map((reason) {
              final (emoji, text) = reason;
              final isSelected = _selectedReason == text;
              return Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Material(
                  color: isSelected
                      ? theme.colorScheme.primaryContainer
                      : theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(12),
                  child: InkWell(
                    onTap: () => setState(() => _selectedReason = text),
                    borderRadius: BorderRadius.circular(12),
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Row(
                        children: [
                          Text(emoji, style: const TextStyle(fontSize: 18)),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              text,
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: isSelected
                                    ? theme.colorScheme.onPrimaryContainer
                                    : theme.colorScheme.onSurfaceVariant,
                              ),
                            ),
                          ),
                          if (isSelected)
                            Icon(
                              Icons.check_circle,
                              color: theme.colorScheme.primary,
                              size: 20,
                            ),
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
            if (_selectedReason == 'Something else') ...[
              const SizedBox(height: 8),
              TextField(
                controller: _additionalInfoController,
                maxLines: 3,
                decoration: InputDecoration(
                  hintText: 'Please describe the issue...',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () {
            Navigator.of(context).pop();
            widget.onDismiss?.call();
          },
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _selectedReason != null
              ? () {
                  final reason = _selectedReason == 'Something else'
                      ? _additionalInfoController.text.isNotEmpty
                            ? _additionalInfoController.text
                            : _selectedReason!
                      : _selectedReason!;
                  Navigator.of(context).pop();
                  widget.onReport?.call(reason);
                }
              : null,
          child: const Text('Submit'),
        ),
      ],
    );
  }
}

/// Shows the report post dialog
void showReportPostDialog(
  BuildContext context, {
  required String postId,
  VoidCallback? onDismiss,
  Function(String reason)? onReport,
}) {
  showDialog(
    context: context,
    builder: (dialogContext) => ReportPostDialog(
      postId: postId,
      onDismiss: onDismiss,
      onReport:
          onReport ??
          (reason) async {
            // Submit report to Supabase backend
            try {
              final result = await SupabaseService.reportContent(
                contentType: 'post',
                contentId: postId,
                reason: reason,
              );
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text(
                      result['message'] as String? ??
                          'Report submitted. Thank you!',
                    ),
                    duration: const Duration(seconds: 2),
                  ),
                );
              }
            } catch (e) {
              // Fallback: also try ModerationService
              try {
                await ModerationService().reportContent(
                  contentId: postId,
                  contentType: 'post',
                  reason: reason,
                  reporterId: SupabaseService.currentUser?.id ?? 'anonymous',
                );
              } catch (_) {}
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Report submitted. Thank you!'),
                    duration: Duration(seconds: 2),
                  ),
                );
              }
            }
          },
    ),
  );
}

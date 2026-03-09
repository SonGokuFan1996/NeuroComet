import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/post.dart';
import '../../providers/theme_provider.dart';

/// Emotional tone detection for neurodivergent-friendly content awareness.
/// Helps users prepare for potentially intense content.
enum EmotionalTone {
  neutral('💭', 'Neutral', Color(0xFFBDBDBD), Color(0xFF757575), false),
  happy('😊', 'Happy', Color(0xFFA5C9A0), Color(0xFF4A7C4F), false),
  excited('🎉', 'Excited', Color(0xFFD4C49A), Color(0xFF8A7540), false),
  sad('💙', 'Sad', Color(0xFF9FBCD4), Color(0xFF4A6E87), true),
  anxious('🫂', 'Anxious', Color(0xFFBEA8CB), Color(0xFF6D5179), true),
  frustrated('😤', 'Frustrated', Color(0xFFCFADA0), Color(0xFF7D5449), true),
  supportive('💜', 'Supportive', Color(0xFFB0A4C7), Color(0xFF5E5280), false),
  question('❓', 'Question', Color(0xFF9ECAD3), Color(0xFF487A83), false),
  celebration('✨', 'Celebration', Color(0xFFCBA8B4), Color(0xFF7A4D5D), false),
  informative('📚', 'Informative', Color(0xFFA3BDD8), Color(0xFF4A6A8A), false);

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
EmotionalTone detectEmotionalTone(String content) {
  final lowerContent = content.toLowerCase();

  if (lowerContent.contains('congrat') ||
      lowerContent.contains('achieved') ||
      lowerContent.contains('finally did it') ||
      lowerContent.contains('so proud') ||
      content.contains('🎉') ||
      content.contains('🎊') ||
      content.contains('🥳')) {
    return EmotionalTone.celebration;
  }

  if (lowerContent.contains('so happy') ||
      lowerContent.contains('amazing') ||
      lowerContent.contains('love this') ||
      lowerContent.contains('best day') ||
      content.contains('😊') ||
      content.contains('😄') ||
      content.contains('❤️')) {
    return EmotionalTone.happy;
  }

  if (lowerContent.contains("can't wait") ||
      lowerContent.contains('so excited') ||
      lowerContent.contains('omg') ||
      content.contains('🔥') ||
      content.contains('⚡')) {
    return EmotionalTone.excited;
  }

  if (lowerContent.contains("you've got this") ||
      lowerContent.contains('proud of you') ||
      lowerContent.contains('here for you') ||
      lowerContent.contains('sending love') ||
      lowerContent.contains("you're not alone")) {
    return EmotionalTone.supportive;
  }

  if (lowerContent.contains('does anyone') ||
      lowerContent.contains('how do i') ||
      lowerContent.contains('any tips') ||
      lowerContent.contains('help me') ||
      lowerContent.contains('advice') ||
      (content.contains('?') && content.length < 200)) {
    return EmotionalTone.question;
  }

  if (lowerContent.contains('did you know') ||
      lowerContent.contains('research shows') ||
      lowerContent.contains('fun fact') ||
      lowerContent.contains('psa') ||
      lowerContent.contains('reminder')) {
    return EmotionalTone.informative;
  }

  // Check anxious BEFORE sad - anxiety keywords should take priority
  if (lowerContent.contains('anxious') ||
      lowerContent.contains('anxiety') ||
      lowerContent.contains('panic') ||
      lowerContent.contains('overwhelm') ||
      lowerContent.contains("can't cope") ||
      lowerContent.contains('sensory overload') ||
      lowerContent.contains('stressed') ||
      lowerContent.contains('nervous')) {
    return EmotionalTone.anxious;
  }

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
  final int animationDelay;

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
    this.animationDelay = 0,
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
  bool _isFollowing = false;
  bool _animationsInitialized = false;

  @override
  void initState() {
    super.initState();
    _isBookmarked = widget.post.isBookmarked;
  }

  void _initAnimations() {
    if (_animationsInitialized) return;
    _animationsInitialized = true;

    // Check if animations are disabled
    final reducedMotion = ref.read(reducedMotionProvider);
    if (reducedMotion) {
      // Skip animation setup for better performance
      return;
    }

    _animationController = AnimationController(
      duration: const Duration(milliseconds: 300), // Reduced from 400ms
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(
        parent: _animationController!,
        curve: Curves.easeOut,
      ),
    );

    _scaleAnimation = Tween<double>(begin: 0.98, end: 1).animate(
      CurvedAnimation(
        parent: _animationController!,
        curve: Curves.easeOut,
      ),
    );

    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, 0.02),
      end: Offset.zero,
    ).animate(
      CurvedAnimation(
        parent: _animationController!,
        curve: Curves.easeOut,
      ),
    );

    // Staggered animation delay - reduced for snappier feel
    final delay = widget.animationDelay.clamp(0, 150); // Cap max delay
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

    // Initialize animations lazily on first build (allows ref.read to work)
    if (!reducedMotion && !_animationsInitialized) {
      _initAnimations();
    }

    final cardWidget = Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      elevation: 0.5,
      shadowColor: theme.colorScheme.shadow.withValues(alpha: 0.1),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
        side: BorderSide(
          color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5),
          width: 1.0,
        ),
      ),
      color: theme.colorScheme.surfaceContainerLow,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeader(context),
            const SizedBox(height: 10),
            _buildContent(context),
            if (widget.post.mediaUrls != null &&
                widget.post.mediaUrls!.isNotEmpty)
              _buildMedia(context),
            const SizedBox(height: 10),
            Divider(
              height: 1,
              thickness: 0.5,
              color: theme.colorScheme.outlineVariant.withValues(alpha: 0.3),
            ),
            const SizedBox(height: 4),
            _buildActions(context),
            if (emotionalTone != EmotionalTone.neutral)
                _buildEmotionalToneTag(context, emotionalTone),
            ],
          ),
        ),
      );

    // If reduced motion is enabled or animations not initialized, just return the card
    if (reducedMotion || _animationController == null) {
      return cardWidget;
    }

    // Wrap with animations
    return AnimatedBuilder(
      animation: _animationController!,
      builder: (context, child) {
        return FadeTransition(
          opacity: _fadeAnimation!,
          child: SlideTransition(
            position: _slideAnimation!,
            child: ScaleTransition(
              scale: _scaleAnimation!,
              child: child,
            ),
          ),
        );
      },
      child: cardWidget,
    );
  }

  Widget _buildHeader(BuildContext context) {
    final theme = Theme.of(context);

    return Row(
      children: [
        // Avatar with refined subtle ring
        GestureDetector(
          onTap: widget.onProfileTap,
          child: Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                color: theme.colorScheme.outline.withValues(alpha: 0.18),
                width: 1.5,
              ),
              boxShadow: [
                BoxShadow(
                  color: theme.colorScheme.shadow.withValues(alpha: 0.06),
                  blurRadius: 6,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: theme.colorScheme.surface,
              ),
              padding: const EdgeInsets.all(1.5),
              child: ClipOval(
                child: widget.post.authorAvatarUrl != null
                    ? Image.network(
                        widget.post.authorAvatarUrl!,
                        fit: BoxFit.cover,
                        errorBuilder: (_, __, ___) => Icon(
                          Icons.account_circle,
                          size: 40,
                          color: theme.colorScheme.onSurfaceVariant
                              .withValues(alpha: 0.4),
                        ),
                      )
                    : Icon(
                        Icons.account_circle,
                        size: 40,
                        color:
                            theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
                      ),
              ),
            ),
          ),
        ),
        const SizedBox(width: 12),

        // User info
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  GestureDetector(
                    onTap: widget.onProfileTap,
                    child: Text(
                      widget.post.authorName,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w600,
                        letterSpacing: -0.1,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  if (_isFollowing) ...[
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primaryContainer.withValues(alpha: 0.7),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Text(
                        'Following',
                        style: theme.textTheme.labelSmall?.copyWith(
                          color: theme.colorScheme.onPrimaryContainer,
                          fontWeight: FontWeight.w500,
                          fontSize: 10,
                        ),
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 2),
              Text(
                _formatTimeAgo(widget.post.createdAt),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.6),
                  fontSize: 12,
                  letterSpacing: 0.1,
                ),
              ),
            ],
          ),
        ),

        // Menu button
        PopupMenuButton<String>(
          icon: Icon(
            Icons.more_vert,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          onSelected: _handleMenuAction,
          itemBuilder: (context) => [
            PopupMenuItem(
              value: 'bookmark',
              child: Row(
                children: [
                  Icon(
                    _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                    size: 20,
                  ),
                  const SizedBox(width: 12),
                  Text(_isBookmarked ? 'Unsave' : 'Save'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'copy',
              child: Row(
                children: [
                  const Icon(Icons.content_copy, size: 20),
                  const SizedBox(width: 12),
                  const Text('Copy text'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'share',
              child: Row(
                children: [
                  const Icon(Icons.share, size: 20),
                  const SizedBox(width: 12),
                  const Text('Share'),
                ],
              ),
            ),
            const PopupMenuDivider(),
            PopupMenuItem(
              value: 'follow',
              child: Row(
                children: [
                  Icon(
                    _isFollowing ? Icons.person_off : Icons.person_add,
                    size: 20,
                  ),
                  const SizedBox(width: 12),
                  Text(_isFollowing ? 'Unfollow' : 'Follow'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'not_interested',
              child: Row(
                children: [
                  const Icon(Icons.not_interested, size: 20),
                  const SizedBox(width: 12),
                  const Text('Not interested'),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'hide',
              child: Row(
                children: [
                  const Icon(Icons.visibility_off, size: 20),
                  const SizedBox(width: 12),
                  const Text('Hide post'),
                ],
              ),
            ),
            const PopupMenuDivider(),
            PopupMenuItem(
              value: 'block',
              child: Row(
                children: [
                  Icon(Icons.block, size: 20, color: theme.colorScheme.error),
                  const SizedBox(width: 12),
                  Text('Block user', style: TextStyle(color: theme.colorScheme.error)),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'report',
              child: Row(
                children: [
                  Icon(Icons.flag, size: 20, color: theme.colorScheme.error),
                  const SizedBox(width: 12),
                  Text('Report', style: TextStyle(color: theme.colorScheme.error)),
                ],
              ),
            ),
            PopupMenuItem(
              value: 'delete',
              child: Row(
                children: [
                  Icon(Icons.delete, size: 20, color: theme.colorScheme.error),
                  const SizedBox(width: 12),
                  Text('Delete', style: TextStyle(color: theme.colorScheme.error)),
                ],
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildContent(BuildContext context) {
    return _LinkedText(
      text: widget.post.content,
      onMentionTap: widget.onMentionTap,
      onHashtagTap: widget.onHashtagTap,
    );
  }

  Widget _buildMedia(BuildContext context) {
    final mediaUrls = widget.post.mediaUrls!;

    if (mediaUrls.length == 1) {
      return Container(
        margin: const EdgeInsets.only(top: 12),
        constraints: const BoxConstraints(maxHeight: 280),
        width: double.infinity,
        child: ClipRRect(
          borderRadius: BorderRadius.circular(16),
          child: Image.network(
            mediaUrls.first,
            fit: BoxFit.cover,
            errorBuilder: (_, __, ___) => Container(
              height: 200,
              color: Theme.of(context).colorScheme.surfaceContainerHighest,
              child: const Icon(Icons.broken_image, size: 48),
            ),
          ),
        ),
      );
    }

    // Multiple media - carousel
    return Container(
      margin: const EdgeInsets.only(top: 12),
      height: 280,
      child: _MediaCarousel(mediaUrls: mediaUrls),
    );
  }

  Widget _buildActions(BuildContext context) {
    final theme = Theme.of(context);

    return Row(
      children: [
        // Like button with animation
        _AnimatedLikeButton(
          isLiked: widget.post.isLiked,
          likeCount: widget.post.likeCount,
          onTap: () {
            HapticFeedback.lightImpact();
            widget.onLike?.call();
          },
        ),
        const SizedBox(width: 8),

        // Comment button
        _ActionButton(
          icon: Icons.chat_bubble_outline,
          label: _formatCount(widget.post.commentCount),
          onTap: widget.onComment,
        ),

        // Share button
        _ActionButton(
          icon: Icons.share_outlined,
          label: '',
          onTap: widget.onShare,
        ),

        const Spacer(),

        // Bookmark button
        IconButton(
          icon: Icon(
            _isBookmarked ? Icons.bookmark : Icons.bookmark_border,
            color: _isBookmarked ? theme.colorScheme.primary : null,
          ),
          onPressed: () {
            HapticFeedback.lightImpact();
            setState(() => _isBookmarked = !_isBookmarked);
            widget.onBookmark?.call();
          },
        ),
      ],
    );
  }

  Widget _buildEmotionalToneTag(BuildContext context, EmotionalTone tone) {
    final theme = Theme.of(context);
    // Blend the tone color with the theme's onSurface for balanced text readability
    final blendedTextColor = Color.lerp(
      tone.textColor,
      theme.colorScheme.onSurfaceVariant,
      0.35,
    )!;

    return Padding(
      padding: const EdgeInsets.only(top: 10),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: tone.backgroundColor.withValues(alpha: 0.12),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: tone.backgroundColor.withValues(alpha: 0.22),
            width: 0.75,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(tone.emoji, style: const TextStyle(fontSize: 13)),
            const SizedBox(width: 5),
            Text(
              tone.label,
              style: TextStyle(
                fontSize: 11.5,
                fontWeight: FontWeight.w500,
                color: blendedTextColor,
                letterSpacing: 0.1,
              ),
            ),
            if (tone.showWarning) ...[
              const SizedBox(width: 5),
              Text(
                '·',
                style: TextStyle(
                  fontSize: 10,
                  color: blendedTextColor.withValues(alpha: 0.5),
                ),
              ),
              const SizedBox(width: 4),
              Text(
                'sensitive',
                style: TextStyle(
                  fontSize: 10.5,
                  color: blendedTextColor.withValues(alpha: 0.6),
                  letterSpacing: 0.1,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  void _handleMenuAction(String action) {
    switch (action) {
      case 'bookmark':
        setState(() => _isBookmarked = !_isBookmarked);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(_isBookmarked ? 'Saved!' : 'Removed'),
            duration: const Duration(seconds: 1),
          ),
        );
        widget.onBookmark?.call();
        break;
      case 'copy':
        Clipboard.setData(ClipboardData(text: widget.post.content));
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Copied to clipboard'),
            duration: Duration(seconds: 1),
          ),
        );
        break;
      case 'share':
        widget.onShare?.call();
        break;
      case 'follow':
        setState(() => _isFollowing = !_isFollowing);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              _isFollowing
                  ? 'Now following ${widget.post.authorName}'
                  : 'Unfollowed ${widget.post.authorName}',
            ),
            duration: const Duration(seconds: 1),
          ),
        );
        break;
      case 'not_interested':
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("We'll show you less content like this"),
            duration: Duration(seconds: 2),
          ),
        );
        break;
      case 'hide':
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Post hidden'),
            duration: Duration(seconds: 1),
          ),
        );
        break;
      case 'block':
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('@${widget.post.authorName} blocked'),
            duration: const Duration(seconds: 2),
          ),
        );
        break;
      case 'report':
        widget.onReport?.call();
        break;
      case 'delete':
        widget.onDelete?.call();
        break;
    }
  }

  String _formatTimeAgo(DateTime? dateTime) {
    if (dateTime == null) return '';

    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 7) {
      return '${dateTime.day}/${dateTime.month}/${dateTime.year}';
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d ago';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h ago';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m ago';
    } else {
      return 'Just now';
    }
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

/// Animated like button with heart burst effect
class _AnimatedLikeButton extends StatefulWidget {
  final bool isLiked;
  final int likeCount;
  final VoidCallback? onTap;

  const _AnimatedLikeButton({
    required this.isLiked,
    required this.likeCount,
    this.onTap,
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
    final color = widget.isLiked
        ? const Color(0xFFE91E63)
        : Theme.of(context).colorScheme.onSurfaceVariant;

    return InkWell(
      onTap: () {
        _controller.forward(from: 0);
        widget.onTap?.call();
      },
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Row(
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
              ),
            ],
          ],
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

  const _ActionButton({
    required this.icon,
    required this.label,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = theme.colorScheme.onSurfaceVariant;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Row(
          children: [
            Icon(icon, size: 20, color: color),
            if (label.isNotEmpty) ...[
              const SizedBox(width: 4),
              Text(
                label,
                style: theme.textTheme.bodySmall?.copyWith(color: color),
              ),
            ],
          ],
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

  const _LinkedText({
    required this.text,
    this.onMentionTap,
    this.onHashtagTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final spans = _parseText(text, theme);

    return Text.rich(
      TextSpan(children: spans),
      style: theme.textTheme.bodyLarge?.copyWith(
        height: 1.5,
      ),
    );
  }

  List<InlineSpan> _parseText(String text, ThemeData theme) {
    final spans = <InlineSpan>[];
    final mentionRegex = RegExp(r'@\w+');
    final hashtagRegex = RegExp(r'#\w+');

    int lastEnd = 0;

    // Find all matches
    final allMatches = <_LinkMatch>[];

    for (final match in mentionRegex.allMatches(text)) {
      allMatches.add(_LinkMatch(match.start, match.end, match.group(0)!, _LinkType.mention));
    }
    for (final match in hashtagRegex.allMatches(text)) {
      allMatches.add(_LinkMatch(match.start, match.end, match.group(0)!, _LinkType.hashtag));
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
              style: TextStyle(
                color: linkColor,
                fontWeight: FontWeight.w500,
              ),
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

/// Media carousel for multiple images/videos
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
    return Stack(
      children: [
        PageView.builder(
          itemCount: widget.mediaUrls.length,
          onPageChanged: (index) => setState(() => _currentIndex = index),
          itemBuilder: (context, index) {
            return ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: Image.network(
                widget.mediaUrls[index],
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  child: const Icon(Icons.broken_image, size: 48),
                ),
              ),
            );
          },
        ),

        // Page indicator
        Positioned(
          bottom: 12,
          left: 0,
          right: 0,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(widget.mediaUrls.length, (index) {
              return Container(
                width: 8,
                height: 8,
                margin: const EdgeInsets.symmetric(horizontal: 3),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: index == _currentIndex
                      ? Theme.of(context).colorScheme.primary
                      : Theme.of(context).colorScheme.outline.withValues(alpha: 0.5),
                ),
              );
            }),
          ),
        ),

        // Counter
        Positioned(
          top: 12,
          right: 12,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.black54,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Text(
              '${_currentIndex + 1}/${widget.mediaUrls.length}',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
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
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
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
    builder: (context) => ReportPostDialog(
      postId: postId,
      onDismiss: onDismiss,
      onReport: onReport ?? (reason) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Report submitted. Thank you!'),
            duration: Duration(seconds: 2),
          ),
        );
      },
    ),
  );
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/post.dart';
import '../../providers/feed_provider.dart';
import '../../widgets/post/post_card.dart';
import '../../widgets/common/neuro_avatar.dart';
import 'package:share_plus/share_plus.dart';
import '../../services/supabase_service.dart';
import '../../l10n/app_localizations.dart';
import '../../widgets/common/neuro_loading.dart';

/// Screen for viewing a single post with its comments
class PostDetailScreen extends ConsumerStatefulWidget {
  final String postId;

  const PostDetailScreen({super.key, required this.postId});

  @override
  ConsumerState<PostDetailScreen> createState() => _PostDetailScreenState();
}

class _PostDetailScreenState extends ConsumerState<PostDetailScreen> {
  final _commentController = TextEditingController();
  final _scrollController = ScrollController();
  final _focusNode = FocusNode();
  // ignore: unused_field - used for reply functionality
  String? _replyToCommentId;
  String? _replyToAuthor;

  @override
  void dispose() {
    _commentController.dispose();
    _scrollController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _startReply(String commentId, String authorName) {
    setState(() {
      _replyToCommentId = commentId;
      _replyToAuthor = authorName;
    });
    _focusNode.requestFocus();
  }

  void _cancelReply() {
    setState(() {
      _replyToCommentId = null;
      _replyToAuthor = null;
    });
  }

  Future<void> _submitComment() async {
    final text = _commentController.text.trim();
    if (text.isEmpty) return;

    try {
      await SupabaseService.addComment(
        postId: widget.postId,
        content: text,
        parentCommentId: _replyToCommentId,
      );
      _commentController.clear();
      _cancelReply();
      // Invalidate the provider to refresh comments
      ref.invalidate(commentsProvider(widget.postId));
      // Also refresh the post to update comment count
      ref.invalidate(postDetailProvider(widget.postId));
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to add comment: $e'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final postState = ref.watch(postDetailProvider(widget.postId));
    final commentsState = ref.watch(commentsProvider(widget.postId));

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.post),
      ),
      body: Column(
        children: [
          Expanded(
            child: postState.when(
              loading: () => const NeuroLoading(),
              error: (error, stack) => Center(child: Text('Error: $error')),
              data: (post) => _buildPostWithComments(post, commentsState),
            ),
          ),
          _buildCommentInput(context),
        ],
      ),
    );
  }

  Widget _buildPostWithComments(Post post, AsyncValue<List<Comment>> commentsState) {
    final theme = Theme.of(context);

    return CustomScrollView(
      controller: _scrollController,
      slivers: [
        // Post
        SliverToBoxAdapter(
          child: PostCard(
            post: post,
            onLike: () => ref.read(feedProvider.notifier).toggleLike(post.id),
            onComment: () => _focusNode.requestFocus(),
            onShare: () {
              SharePlus.instance.share(
                ShareParams(
                  text: '${post.content}\n\nShared from NeuroComet',
                  subject: 'Check out this post on NeuroComet',
                ),
              );
            },
            onProfileTap: () {
              Navigator.pushNamed(context, '/profile/${post.authorId}');
            },
          ),
        ),

        // Comments header
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Text(
                  'Comments',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(width: 8),
                Text(
                  '(${post.commentCount})',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.outline,
                  ),
                ),
              ],
            ),
          ),
        ),

        // Comments list
        commentsState.when(
          loading: () => const SliverToBoxAdapter(
            child: Center(child: CircularProgressIndicator()),
          ),
          error: (error, stack) => SliverToBoxAdapter(
            child: Center(child: Text('Error loading comments: $error')),
          ),
          data: (comments) => comments.isEmpty
              ? SliverToBoxAdapter(child: _buildEmptyComments())
              : SliverList(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) => _CommentTile(
                      comment: comments[index],
                      onReply: () => _startReply(
                        comments[index].id,
                        comments[index].authorName,
                      ),
                      onLike: () {
                        // Toggle like on the comment's associated post
                        ref.read(feedProvider.notifier).toggleLike(widget.postId);
                      },
                    ),
                    childCount: comments.length,
                  ),
                ),
        ),

        // Bottom padding
        const SliverPadding(padding: EdgeInsets.only(bottom: 16)),
      ],
    );
  }

  Widget _buildEmptyComments() {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        children: [
          Icon(
            Icons.chat_bubble_outline,
            size: 48,
            color: theme.colorScheme.outline,
          ),
          const SizedBox(height: 16),
          Text(
            'No comments yet',
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.outline,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Be the first to share your thoughts!',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.outline,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCommentInput(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(
        left: 16,
        right: 16,
        top: 8,
        bottom: MediaQuery.of(context).padding.bottom + 8,
      ),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Reply indicator
          if (_replyToAuthor != null) ...[
            Row(
              children: [
                Text(
                  'Replying to @$_replyToAuthor',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.primary,
                  ),
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close, size: 16),
                  onPressed: _cancelReply,
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                ),
              ],
            ),
            const SizedBox(height: 8),
          ],

          // Input field
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _commentController,
                  focusNode: _focusNode,
                  decoration: InputDecoration(
                    hintText: 'Write a comment...',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(24),
                      borderSide: BorderSide.none,
                    ),
                    filled: true,
                    fillColor: theme.colorScheme.surfaceContainerHighest,
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 8,
                    ),
                  ),
                  maxLines: 4,
                  minLines: 1,
                ),
              ),
              const SizedBox(width: 8),
              IconButton.filled(
                onPressed: _submitComment,
                icon: const Icon(Icons.send),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Comment tile widget
class _CommentTile extends StatelessWidget {
  final Comment comment;
  final VoidCallback? onReply;
  final VoidCallback? onLike;

  const _CommentTile({
    required this.comment,
    this.onReply,
    this.onLike,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: EdgeInsets.only(
        left: comment.parentCommentId != null ? 48 : 16,
        right: 16,
        top: 8,
        bottom: 8,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          NeuroAvatar(
            imageUrl: comment.authorAvatarUrl,
            name: comment.authorName,
            size: 36,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Author and time
                Row(
                  children: [
                    Text(
                      comment.authorName,
                      style: theme.textTheme.bodySmall?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      _formatTimeAgo(comment.createdAt),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.outline,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),

                // Content
                Text(
                  comment.content,
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 8),

                // Actions
                Row(
                  children: [
                    GestureDetector(
                      onTap: onLike,
                      child: Row(
                        children: [
                          Icon(
                            comment.isLiked
                                ? Icons.favorite
                                : Icons.favorite_border,
                            size: 16,
                            color: comment.isLiked
                                ? Colors.red
                                : theme.colorScheme.outline,
                          ),
                          const SizedBox(width: 4),
                          Text(
                            comment.likeCount.toString(),
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.outline,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 16),
                    GestureDetector(
                      onTap: onReply,
                      child: Text(
                        'Reply',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.primary,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),

                // Nested replies
                if (comment.replies.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  ...comment.replies.map((reply) => Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: _CommentTile(
                      comment: reply,
                      onReply: () {
                        // Reply to this reply
                      },
                      onLike: () {
                        // Like this reply
                      },
                    ),
                  )),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatTimeAgo(DateTime? dateTime) {
    if (dateTime == null) return '';

    final now = DateTime.now();
    final difference = now.difference(dateTime);

    if (difference.inDays > 7) {
      return '${dateTime.day}/${dateTime.month}/${dateTime.year}';
    } else if (difference.inDays > 0) {
      return '${difference.inDays}d';
    } else if (difference.inHours > 0) {
      return '${difference.inHours}h';
    } else if (difference.inMinutes > 0) {
      return '${difference.inMinutes}m';
    } else {
      return 'now';
    }
  }
}

// Providers for post detail and comments
final postDetailProvider = FutureProvider.family<Post, String>((ref, postId) async {
  await Future.delayed(const Duration(milliseconds: 300));

  // Mock data - replace with actual API call
  return Post(
    id: postId,
    authorId: 'user_1',
    authorName: 'Alex Thompson',
    authorAvatarUrl: 'https://i.pravatar.cc/150?img=1',
    content: 'Just had an amazing breakthrough with my ADHD management strategy! '
        'Turns out that breaking tasks into tiny 5-minute chunks works so much better for me. '
        'Anyone else tried this approach? Would love to hear your experiences! 🧠✨',
    likeCount: 42,
    commentCount: 15,
    shareCount: 5,
    createdAt: DateTime.now().subtract(const Duration(hours: 2)),
    category: 'adhd',
    tags: ['Tips', 'ADHD', 'Win'],
  );
});

final commentsProvider = FutureProvider.family<List<Comment>, String>((ref, postId) async {
  await Future.delayed(const Duration(milliseconds: 300));

  return List.generate(
    5,
    (index) => Comment(
      id: 'comment_$index',
      postId: postId,
      authorId: 'user_$index',
      authorName: 'User ${index + 1}',
      authorAvatarUrl: 'https://i.pravatar.cc/150?img=${index + 10}',
      content: 'This is comment #${index + 1}. Great post! 💪',
      likeCount: (index * 3) % 20,
      createdAt: DateTime.now().subtract(Duration(hours: index)),
    ),
  );
});


import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';

/// Message bubble widget for chat messages
class MessageBubble extends StatelessWidget {
  final String content;
  final bool isMe;
  final DateTime? time;
  final bool showTail;
  final ChatMessageStatus? status;
  final VoidCallback? onLongPress;
  final List<ChatReaction>? reactions;

  const MessageBubble({
    super.key,
    required this.content,
    required this.isMe,
    this.time,
    this.showTail = true,
    this.status,
    this.onLongPress,
    this.reactions,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final bubbleColor = isMe
        ? AppColors.primaryPurple
        : isDark
            ? AppColors.surfaceVariantDark
            : AppColors.surfaceVariantLight;

    final textColor = isMe
        ? Colors.white
        : theme.colorScheme.onSurface;

    return GestureDetector(
      onLongPress: onLongPress,
      child: Align(
        alignment: isMe ? Alignment.centerRight : Alignment.centerLeft,
        child: Container(
          margin: EdgeInsets.only(
            left: isMe ? 64 : 16,
            right: isMe ? 16 : 64,
            top: 2,
            bottom: 2,
          ),
          child: Column(
            crossAxisAlignment:
                isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
            children: [
              // Message bubble
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 10,
                ),
                decoration: BoxDecoration(
                  color: bubbleColor,
                  borderRadius: BorderRadius.only(
                    topLeft: const Radius.circular(20),
                    topRight: const Radius.circular(20),
                    bottomLeft: Radius.circular(isMe || !showTail ? 20 : 4),
                    bottomRight: Radius.circular(isMe && showTail ? 4 : 20),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withAlpha(10),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      content,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: textColor,
                      ),
                    ),
                    if (time != null || status != null)
                      Padding(
                        padding: const EdgeInsets.only(top: 4),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            if (time != null)
                              Text(
                                _formatTime(time!),
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: isMe
                                      ? Colors.white70
                                      : theme.colorScheme.outline,
                                  fontSize: 11,
                                ),
                              ),
                            if (isMe && status != null) ...[
                              const SizedBox(width: 4),
                              _buildStatusIcon(status!),
                            ],
                          ],
                        ),
                      ),
                  ],
                ),
              ),

              // Reactions
              if (reactions != null && reactions!.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Wrap(
                    spacing: 4,
                    children: reactions!.map((r) => _ReactionChip(reaction: r)).toList(),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusIcon(ChatMessageStatus status) {
    switch (status) {
      case ChatMessageStatus.sending:
        return const SizedBox(
          width: 12,
          height: 12,
          child: CircularProgressIndicator(
            strokeWidth: 1.5,
            color: Colors.white70,
          ),
        );
      case ChatMessageStatus.sent:
        return const Icon(Icons.check, size: 14, color: Colors.white70);
      case ChatMessageStatus.delivered:
        return const Icon(Icons.done_all, size: 14, color: Colors.white70);
      case ChatMessageStatus.read:
        return const Icon(Icons.done_all, size: 14, color: Colors.lightBlueAccent);
      case ChatMessageStatus.failed:
        return const Icon(Icons.error_outline, size: 14, color: Colors.redAccent);
    }
  }

  String _formatTime(DateTime time) {
    final hour = time.hour > 12 ? time.hour - 12 : time.hour;
    final period = time.hour >= 12 ? 'PM' : 'AM';
    final minute = time.minute.toString().padLeft(2, '0');
    return '$hour:$minute $period';
  }
}

enum ChatMessageStatus { sending, sent, delivered, read, failed }

class ChatReaction {
  final String emoji;
  final int count;
  final bool isByMe;

  const ChatReaction({
    required this.emoji,
    required this.count,
    this.isByMe = false,
  });
}

class _ReactionChip extends StatelessWidget {
  final ChatReaction reaction;

  const _ReactionChip({required this.reaction});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(
        color: reaction.isByMe
            ? AppColors.primaryPurple.withAlpha(30)
            : theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(12),
        border: reaction.isByMe
            ? Border.all(color: AppColors.primaryPurple.withAlpha(100))
            : null,
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(reaction.emoji, style: const TextStyle(fontSize: 12)),
          if (reaction.count > 1) ...[
            const SizedBox(width: 2),
            Text(
              '${reaction.count}',
              style: theme.textTheme.labelSmall,
            ),
          ],
        ],
      ),
    );
  }
}

/// Chat input bar widget
class ChatInputBar extends StatefulWidget {
  final TextEditingController controller;
  final VoidCallback onSend;
  final VoidCallback? onAttachment;
  final VoidCallback? onVoiceMessage;
  final String hintText;

  const ChatInputBar({
    super.key,
    required this.controller,
    required this.onSend,
    this.onAttachment,
    this.onVoiceMessage,
    this.hintText = 'Type a message...',
  });

  @override
  State<ChatInputBar> createState() => _ChatInputBarState();
}

class _ChatInputBarState extends State<ChatInputBar> {
  bool _hasText = false;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_onTextChanged);
  }

  void _onTextChanged() {
    final hasText = widget.controller.text.trim().isNotEmpty;
    if (hasText != _hasText) {
      setState(() => _hasText = hasText);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.only(
        left: 8,
        right: 8,
        top: 8,
        bottom: MediaQuery.of(context).padding.bottom + 8,
      ),
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withAlpha(10),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          // Attachment button
          if (widget.onAttachment != null)
            IconButton(
              icon: const Icon(Icons.add_circle_outline),
              onPressed: widget.onAttachment,
              color: theme.colorScheme.primary,
            ),

          // Text field
          Expanded(
            child: Container(
              constraints: const BoxConstraints(maxHeight: 120),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(24),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Expanded(
                    child: TextField(
                      controller: widget.controller,
                      maxLines: 5,
                      minLines: 1,
                      textCapitalization: TextCapitalization.sentences,
                      decoration: InputDecoration(
                        hintText: widget.hintText,
                        border: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 10,
                        ),
                      ),
                    ),
                  ),
                  // Emoji button
                  IconButton(
                    icon: const Icon(Icons.emoji_emotions_outlined),
                    onPressed: () {
                      // Show emoji picker
                    },
                    padding: const EdgeInsets.only(right: 4),
                    constraints: const BoxConstraints(),
                    iconSize: 22,
                    color: theme.colorScheme.outline,
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(width: 8),

          // Send or voice button
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 200),
            child: _hasText
                ? FloatingActionButton.small(
                    key: const ValueKey('send'),
                    onPressed: widget.onSend,
                    backgroundColor: AppColors.primaryPurple,
                    child: const Icon(Icons.send, color: Colors.white),
                  )
                : widget.onVoiceMessage != null
                    ? FloatingActionButton.small(
                        key: const ValueKey('voice'),
                        onPressed: widget.onVoiceMessage,
                        backgroundColor: AppColors.primaryPurple,
                        child: const Icon(Icons.mic, color: Colors.white),
                      )
                    : FloatingActionButton.small(
                        key: const ValueKey('send_disabled'),
                        onPressed: null,
                        backgroundColor: AppColors.primaryPurple.withAlpha(100),
                        child: const Icon(Icons.send, color: Colors.white70),
                      ),
          ),
        ],
      ),
    );
  }
}

/// Date separator for chat messages
class ChatDateSeparator extends StatelessWidget {
  final DateTime date;

  const ChatDateSeparator({super.key, required this.date});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 16),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(
          _formatDate(date),
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.outline,
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));
    final messageDate = DateTime(date.year, date.month, date.day);

    if (messageDate == today) {
      return 'Today';
    } else if (messageDate == yesterday) {
      return 'Yesterday';
    } else if (now.difference(date).inDays < 7) {
      const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
      return days[date.weekday - 1];
    } else {
      return '${date.month}/${date.day}/${date.year}';
    }
  }
}

/// Typing indicator widget
class TypingIndicator extends StatefulWidget {
  final String? userName;

  const TypingIndicator({super.key, this.userName});

  @override
  State<TypingIndicator> createState() => _TypingIndicatorState();
}

class _TypingIndicatorState extends State<TypingIndicator>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      margin: const EdgeInsets.only(left: 16, right: 64, top: 2, bottom: 2),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: isDark ? AppColors.surfaceVariantDark : AppColors.surfaceVariantLight,
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(20),
          topRight: Radius.circular(20),
          bottomLeft: Radius.circular(4),
          bottomRight: Radius.circular(20),
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (widget.userName != null) ...[
            Text(
              '${widget.userName} is typing',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.outline,
                fontStyle: FontStyle.italic,
              ),
            ),
            const SizedBox(width: 8),
          ],
          AnimatedBuilder(
            animation: _controller,
            builder: (context, child) {
              return Row(
                mainAxisSize: MainAxisSize.min,
                children: List.generate(3, (index) {
                  final delay = index * 0.2;
                  final animValue = ((_controller.value + delay) % 1.0);
                  final scale = 0.5 + (animValue < 0.5 ? animValue : 1 - animValue);

                  return Container(
                    margin: const EdgeInsets.symmetric(horizontal: 2),
                    child: Transform.scale(
                      scale: scale,
                      child: Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          color: theme.colorScheme.outline,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                  );
                }),
              );
            },
          ),
        ],
      ),
    );
  }
}

/// Message container for organizing messages by date
class MessageContainer extends StatelessWidget {
  final List<MessageGroup> messageGroups;
  final ScrollController scrollController;
  final bool showTypingIndicator;
  final String? typingUserName;

  const MessageContainer({
    super.key,
    required this.messageGroups,
    required this.scrollController,
    this.showTypingIndicator = false,
    this.typingUserName,
  });

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      controller: scrollController,
      padding: const EdgeInsets.symmetric(vertical: 16),
      reverse: true,
      itemCount: messageGroups.length + (showTypingIndicator ? 1 : 0),
      itemBuilder: (context, index) {
        if (showTypingIndicator && index == 0) {
          return TypingIndicator(userName: typingUserName);
        }

        final adjustedIndex = showTypingIndicator ? index - 1 : index;
        final group = messageGroups[adjustedIndex];

        return Column(
          children: [
            ChatDateSeparator(date: group.date),
            ...group.messages.asMap().entries.map((entry) {
              final msgIndex = entry.key;
              final msg = entry.value;
              final showTail = msgIndex == group.messages.length - 1 ||
                  group.messages[msgIndex + 1].isMe != msg.isMe;

              return MessageBubble(
                content: msg.content,
                isMe: msg.isMe,
                time: msg.time,
                showTail: showTail,
                status: msg.status,
                reactions: msg.reactions,
              );
            }),
          ],
        );
      },
    );
  }
}

/// Message group for organizing by date
class MessageGroup {
  final DateTime date;
  final List<MessageData> messages;

  const MessageGroup({required this.date, required this.messages});
}

/// Message data for display
class MessageData {
  final String content;
  final bool isMe;
  final DateTime? time;
  final ChatMessageStatus? status;
  final List<ChatReaction>? reactions;

  const MessageData({
    required this.content,
    required this.isMe,
    this.time,
    this.status,
    this.reactions,
  });
}


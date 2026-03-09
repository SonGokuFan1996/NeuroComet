import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../models/conversation.dart';
import '../../models/dev_options.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../widgets/common/neuro_avatar.dart';
import '../../core/theme/app_colors.dart';
import '../../services/webrtc_call_service.dart';
import '../calling/active_call_screen.dart';

/// Chat screen for individual conversations
class ChatScreen extends ConsumerStatefulWidget {
  final String? conversationId;
  final String? userId;

  const ChatScreen({
    super.key,
    this.conversationId,
    this.userId,
  });

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final List<Message> _messages = [];
  bool _isLoading = true;
  bool _isTyping = false;

  @override
  void initState() {
    super.initState();
    _loadMessages();
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _loadMessages() async {
    // Mock messages for now
    await Future.delayed(const Duration(milliseconds: 500));
    if (!mounted) return;

    setState(() {
      _messages.addAll([
        Message(
          id: 'msg_1',
          conversationId: widget.conversationId ?? '',
          senderId: 'other_user',
          content: 'Hey! How are you doing today? 😊',
          createdAt: DateTime.now().subtract(const Duration(minutes: 30)),
        ),
        Message(
          id: 'msg_2',
          conversationId: widget.conversationId ?? '',
          senderId: 'current_user',
          content: 'I\'m doing great, thanks for asking! Just finished my sensory break.',
          createdAt: DateTime.now().subtract(const Duration(minutes: 25)),
        ),
        Message(
          id: 'msg_3',
          conversationId: widget.conversationId ?? '',
          senderId: 'other_user',
          content: 'That\'s awesome! I love that NeuroComet has those features.',
          createdAt: DateTime.now().subtract(const Duration(minutes: 20)),
        ),
        Message(
          id: 'msg_4',
          conversationId: widget.conversationId ?? '',
          senderId: 'current_user',
          content: 'Right? It really helps when I need to decompress.',
          createdAt: DateTime.now().subtract(const Duration(minutes: 15)),
        ),
      ]);
      _isLoading = false;
    });

    _scrollToBottom();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      try {
        if (_scrollController.hasClients) {
          _scrollController.animateTo(
            _scrollController.position.maxScrollExtent,
            duration: const Duration(milliseconds: 300),
            curve: Curves.easeOut,
          );
        }
      } catch (_) {
        // Scroll controller may not be ready yet
      }
    });
  }

  void _sendMessage() async {
    final text = _messageController.text.trim();
    if (text.isEmpty) return;

    final opts = ref.read(devOptionsProvider);

    // Feature flag: force DM send failure
    if (opts.forceSendFailure) {
      debugPrint('[DevFlag] forceSendFailure active – message will fail');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Message failed to send (dev flag: forceSendFailure)'),
            backgroundColor: Colors.red,
          ),
        );
      }
      return;
    }

    // Feature flag: artificial DM send delay
    if (opts.artificialDelayMs > 0) {
      debugPrint('[DevFlag] Adding ${opts.artificialDelayMs}ms artificial delay to message send');
      await Future.delayed(Duration(milliseconds: opts.artificialDelayMs));
      if (!mounted) return;
    }

    // Feature flag: moderation override for sent content
    final moderationStatus = _effectiveModerationStatus(text, opts);
    if (moderationStatus == 'blocked') {
      debugPrint('[DevFlag] Message blocked by moderation: $text');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Message blocked by content moderation'),
            backgroundColor: Colors.orange,
          ),
        );
      }
      return;
    }

    setState(() {
      _messages.add(Message(
        id: 'msg_${DateTime.now().millisecondsSinceEpoch}',
        conversationId: widget.conversationId ?? '',
        senderId: 'current_user',
        content: text,
        createdAt: DateTime.now(),
        status: moderationStatus == 'flagged' ? MessageStatus.sent : MessageStatus.sent,
      ));
    });

    _messageController.clear();
    _scrollToBottom();
  }

  /// Content moderation check, respecting dev override.
  /// Mirrors the Kotlin FeedViewModel.effectiveModerationStatus()
  String _effectiveModerationStatus(String content, DevOptions opts) {
    switch (opts.moderationOverride) {
      case ModerationOverride.off:
        return _performContentModeration(content);
      case ModerationOverride.clean:
        return 'clean';
      case ModerationOverride.flagged:
        return 'flagged';
      case ModerationOverride.blocked:
        return 'blocked';
    }
  }

  String _performContentModeration(String content) {
    final lower = content.toLowerCase();
    const blockedKeywords = ['kill', 'harm', 'abuse', 'underage', 'threat', 'illegal', 'criminal'];
    const flaggedKeywords = ['scam', 'phishing', 'hate', 'link', 'spam'];

    if (blockedKeywords.any((k) => lower.contains(k))) return 'blocked';
    if (flaggedKeywords.any((k) => lower.contains(k))) return 'flagged';
    return 'clean';
  }

  // ═══════════════════════════════════════════════════════════════
  // DEVELOPER OPTIONS — Bottom Sheet (no inline overlay)
  // ═══════════════════════════════════════════════════════════════

  void _showDevOptionsSheet() {
    if (!mounted) return;
    HapticFeedback.mediumImpact();

    // Capture scroll info safely before opening sheet
    String scrollInfo;
    try {
      scrollInfo = _scrollController.hasClients
          ? _scrollController.position.pixels.toStringAsFixed(0)
          : 'N/A';
    } catch (_) {
      scrollInfo = 'N/A';
    }

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (sheetContext) {
        // StatefulBuilder so toggles update inside the sheet
        return StatefulBuilder(
          builder: (sheetContext, setSheetState) {
            final theme = Theme.of(sheetContext);
            final isDark = theme.brightness == Brightness.dark;

            return Container(
              constraints: BoxConstraints(
                maxHeight: MediaQuery.of(sheetContext).size.height * 0.7,
              ),
              decoration: BoxDecoration(
                color: theme.colorScheme.surface,
                borderRadius: const BorderRadius.vertical(
                  top: Radius.circular(24),
                ),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.1),
                    blurRadius: 20,
                    offset: const Offset(0, -4),
                  ),
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Handle bar
                  Padding(
                    padding: const EdgeInsets.only(top: 12, bottom: 8),
                    child: Container(
                      width: 40,
                      height: 4,
                      decoration: BoxDecoration(
                        color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.3),
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                  ),

                  // Header
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                    child: Row(
                      children: [
                        Container(
                          width: 42,
                          height: 42,
                          decoration: BoxDecoration(
                            color: AppColors.primaryPurple.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: const Icon(
                            Icons.build_rounded,
                            color: AppColors.primaryPurple,
                            size: 22,
                          ),
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Developer Options',
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(
                                'Chat debug tools',
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                            ],
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.close_rounded),
                          onPressed: () => Navigator.pop(sheetContext),
                          style: IconButton.styleFrom(
                            backgroundColor: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.08),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 8),

                  // Scrollable content
                  Flexible(
                    child: SingleChildScrollView(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // ── Debug Info Card ──
                          _DevCardContainer(
                            isDark: isDark,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const _DevCardHeader(
                                  icon: Icons.info_outline_rounded,
                                  title: 'Chat Info',
                                  iconColor: AppColors.info,
                                ),
                                const SizedBox(height: 12),
                                _DevInfoRow(label: 'Messages', value: '${_messages.length}'),
                                _DevInfoRow(label: 'Conversation ID', value: widget.conversationId ?? 'null'),
                                _DevInfoRow(
                                  label: 'Typing Indicator',
                                  value: _isTyping ? 'Active' : 'Inactive',
                                  valueColor: _isTyping ? AppColors.success : null,
                                ),
                                _DevInfoRow(label: 'Scroll Position', value: scrollInfo),
                              ],
                            ),
                          ),

                          const SizedBox(height: 12),

                          // ── Toggles Card ──
                          _DevCardContainer(
                            isDark: isDark,
                            child: _DevToggleRow(
                              icon: Icons.keyboard_rounded,
                              iconColor: AppColors.secondaryTeal,
                              title: 'Show Typing Indicator',
                              description: 'Simulates another user typing',
                              value: _isTyping,
                              onChanged: (value) {
                                setState(() => _isTyping = value);
                                setSheetState(() {});
                                if (value) _scrollToBottom();
                              },
                            ),
                          ),

                          const SizedBox(height: 12),

                          // ── Actions Card ──
                          _DevCardContainer(
                            isDark: isDark,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const _DevCardHeader(
                                  icon: Icons.science_rounded,
                                  title: 'Test Actions',
                                  iconColor: AppColors.accentOrange,
                                ),
                                const SizedBox(height: 12),
                                _DevActionButton(
                                  icon: Icons.checklist_rounded,
                                  label: 'Test All Message Statuses',
                                  description: 'Adds 5 messages with sending, sent, delivered, read & failed states',
                                  onPressed: () {
                                    Navigator.pop(sheetContext);
                                    _testAllMessageStatuses();
                                  },
                                ),
                                const SizedBox(height: 8),
                                _DevActionButton(
                                  icon: Icons.delete_sweep_rounded,
                                  label: 'Clear All Messages',
                                  description: 'Remove all messages and reload defaults',
                                  isDestructive: true,
                                  onPressed: () {
                                    Navigator.pop(sheetContext);
                                    _clearAndReload();
                                  },
                                ),
                              ],
                            ),
                          ),

                          SizedBox(height: MediaQuery.of(sheetContext).padding.bottom + 16),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  void _testAllMessageStatuses() {
    if (!mounted) return;

    final timestamp = DateTime.now().millisecondsSinceEpoch;
    final testMessages = [
      ('Testing sending status...', MessageStatus.sending),
      ('Testing sent status ✓', MessageStatus.sent),
      ('Testing delivered status ✓✓', MessageStatus.delivered),
      ('Testing read status 💙', MessageStatus.read),
      ('Testing failed status ❌', MessageStatus.failed),
    ];

    setState(() {
      for (int i = 0; i < testMessages.length; i++) {
        final (content, status) = testMessages[i];
        _messages.add(Message(
          id: 'test_status_${timestamp}_$i',
          conversationId: widget.conversationId ?? '',
          senderId: 'current_user',
          content: content,
          status: status,
          createdAt: DateTime.now().add(Duration(seconds: i)),
        ));
      }
    });

    _scrollToBottom();

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Added test messages with different statuses'),
          duration: const Duration(seconds: 2),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
    }
  }

  void _clearAndReload() {
    if (!mounted) return;
    setState(() {
      _messages.clear();
      _isLoading = true;
      _isTyping = false;
    });
    _loadMessages();
  }

  // ═══════════════════════════════════════════════════════════════
  // BUILD
  // ═══════════════════════════════════════════════════════════════

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: Row(
          children: [
            const NeuroAvatar(
              imageUrl: 'https://i.pravatar.cc/150?img=1',
              name: 'Alex Thompson',
              size: 40,
              isOnline: true,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Alex Thompson',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  Text(
                    _isTyping ? 'typing...' : 'Online',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: _isTyping ? AppColors.primaryPurple : AppColors.success,
                      fontStyle: _isTyping ? FontStyle.italic : FontStyle.normal,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.videocam_outlined),
            onPressed: () {
              HapticFeedback.selectionClick();
              WebRTCCallService.instance.startCall(
                recipientId: widget.userId ?? widget.conversationId ?? '',
                recipientName: 'User',
                recipientAvatar: '',
                callType: CallType.video,
              );
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.call_outlined),
            onPressed: () {
              HapticFeedback.selectionClick();
              WebRTCCallService.instance.startCall(
                recipientId: widget.userId ?? widget.conversationId ?? '',
                recipientName: 'User',
                recipientAvatar: '',
                callType: CallType.voice,
              );
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ActiveCallScreen()),
              );
            },
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert),
            onSelected: (value) {
              switch (value) {
                case 'mute':
                  break;
                case 'block':
                  break;
                case 'report':
                  break;
                case 'dev_options':
                  _showDevOptionsSheet();
                  break;
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'mute',
                child: Row(
                  children: [
                    Icon(Icons.notifications_off_outlined, size: 20),
                    SizedBox(width: 12),
                    Text('Mute'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'block',
                child: Row(
                  children: [
                    Icon(Icons.block, size: 20),
                    SizedBox(width: 12),
                    Text('Block'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'report',
                child: Row(
                  children: [
                    Icon(Icons.flag_outlined, size: 20),
                    SizedBox(width: 12),
                    Text('Report'),
                  ],
                ),
              ),
              if (kDebugMode) ...[
                const PopupMenuDivider(),
                PopupMenuItem(
                  value: 'dev_options',
                  child: Row(
                    children: [
                      Container(
                        width: 20,
                        height: 20,
                        decoration: BoxDecoration(
                          gradient: AppColors.primaryGradient,
                          borderRadius: BorderRadius.circular(5),
                        ),
                        child: const Icon(
                          Icons.build_rounded,
                          size: 13,
                          color: Colors.white,
                        ),
                      ),
                      const SizedBox(width: 12),
                      const Text('Developer Options'),
                    ],
                  ),
                ),
              ],
            ],
          ),
        ],
      ),
      body: Column(
        children: [
          // Messages List
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    itemCount: _messages.length + (_isTyping ? 1 : 0),
                    itemBuilder: (context, index) {
                      // Show typing indicator as last item
                      if (_isTyping && index == _messages.length) {
                        return const _TypingIndicator(
                          key: ValueKey('typing_indicator'),
                        );
                      }
                      final message = _messages[index];
                      final isMe = message.senderId == 'current_user';
                      return _MessageBubble(
                        message: message,
                        isMe: isMe,
                        status: message.status,
                      );
                    },
                  ),
          ),

          // Input Area
          _buildInputArea(context),
        ],
      ),
    );
  }

  Widget _buildInputArea(BuildContext context) {
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
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            onPressed: () => _showAttachmentOptions(),
          ),
          Expanded(
            child: TextField(
              controller: _messageController,
              textCapitalization: TextCapitalization.sentences,
              maxLines: 4,
              minLines: 1,
              decoration: InputDecoration(
                hintText: 'Type a message...',
                filled: true,
                fillColor: theme.colorScheme.surfaceContainerHighest,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(24),
                  borderSide: BorderSide.none,
                ),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 10,
                ),
              ),
              onSubmitted: (_) => _sendMessage(),
            ),
          ),
          const SizedBox(width: 8),
          IconButton.filled(
            icon: const Icon(Icons.send),
            onPressed: _sendMessage,
          ),
        ],
      ),
    );
  }

  void _showAttachmentOptions() {
    showModalBottomSheet(
      context: context,
      builder: (context) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primaryPurple.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.photo, color: AppColors.primaryPurple),
                ),
                title: const Text('Photo'),
                subtitle: const Text('Share an image'),
                onTap: () async {
                  Navigator.pop(context);
                  final picker = ImagePicker();
                  final image = await picker.pickImage(
                    source: ImageSource.gallery,
                    maxWidth: 1920,
                    maxHeight: 1920,
                    imageQuality: 85,
                  );
                  if (image != null && mounted) {
                    ScaffoldMessenger.of(this.context).showSnackBar(
                      SnackBar(
                        content: Text('Image selected: ${image.name}'),
                        behavior: SnackBarBehavior.floating,
                      ),
                    );
                  }
                },
              ),
              ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.secondaryTeal.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.camera_alt, color: AppColors.secondaryTeal),
                ),
                title: const Text('Camera'),
                subtitle: const Text('Take a photo'),
                onTap: () async {
                  Navigator.pop(context);
                  final picker = ImagePicker();
                  final photo = await picker.pickImage(
                    source: ImageSource.camera,
                    maxWidth: 1920,
                    maxHeight: 1920,
                    imageQuality: 85,
                  );
                  if (photo != null && mounted) {
                    ScaffoldMessenger.of(this.context).showSnackBar(
                      SnackBar(
                        content: Text('Photo taken: ${photo.name}'),
                        behavior: SnackBarBehavior.floating,
                      ),
                    );
                  }
                },
              ),
              ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.accentOrange.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.attach_file, color: AppColors.accentOrange),
                ),
                title: const Text('File'),
                subtitle: const Text('Share a document'),
                onTap: () {
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('File sharing requires file_picker package configuration'),
                      behavior: SnackBarBehavior.floating,
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// MESSAGE BUBBLE
// ═══════════════════════════════════════════════════════════════

class _MessageBubble extends StatefulWidget {
  final Message message;
  final bool isMe;
  final MessageStatus status;

  const _MessageBubble({
    required this.message,
    required this.isMe,
    this.status = MessageStatus.delivered,
  });

  @override
  State<_MessageBubble> createState() => _MessageBubbleState();
}

class _MessageBubbleState extends State<_MessageBubble> {
  final List<String> _reactions = [];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: GestureDetector(
        onLongPress: () => _showReactionPicker(context),
        child: Row(
          mainAxisAlignment: widget.isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
          children: [
            if (!widget.isMe) ...[
              const NeuroAvatar(
                imageUrl: 'https://i.pravatar.cc/150?img=1',
                name: 'Alex',
                size: 32,
              ),
              const SizedBox(width: 8),
            ],
            Flexible(
              child: Column(
                crossAxisAlignment: widget.isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                    decoration: BoxDecoration(
                      color: widget.isMe
                          ? theme.colorScheme.primary
                          : theme.colorScheme.surfaceContainerHighest,
                      borderRadius: BorderRadius.only(
                        topLeft: const Radius.circular(18),
                        topRight: const Radius.circular(18),
                        bottomLeft: Radius.circular(widget.isMe ? 18 : 4),
                        bottomRight: Radius.circular(widget.isMe ? 4 : 18),
                      ),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          widget.message.content,
                          style: theme.textTheme.bodyMedium?.copyWith(
                            color: widget.isMe
                                ? Colors.white
                                : theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              _formatTime(widget.message.createdAt),
                              style: theme.textTheme.labelSmall?.copyWith(
                                color: widget.isMe
                                    ? Colors.white.withValues(alpha: 0.7)
                                    : theme.colorScheme.outline,
                              ),
                            ),
                            if (widget.isMe) ...[
                              const SizedBox(width: 4),
                              _buildStatusIcon(theme),
                            ],
                          ],
                        ),
                      ],
                    ),
                  ),
                  // Emoji reactions
                  if (_reactions.isNotEmpty)
                    Padding(
                      padding: const EdgeInsets.only(top: 4),
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: theme.colorScheme.surfaceContainerHighest,
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: theme.colorScheme.outline.withValues(alpha: 0.2),
                          ),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: _reactions.map((r) => Text(r, style: const TextStyle(fontSize: 14))).toList(),
                        ),
                      ),
                    ),
                ],
              ),
            ),
            if (widget.isMe) const SizedBox(width: 40),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusIcon(ThemeData theme) {
    switch (widget.status) {
      case MessageStatus.sending:
        return SizedBox(
          width: 12,
          height: 12,
          child: CircularProgressIndicator(
            strokeWidth: 1.5,
            color: Colors.white.withValues(alpha: 0.7),
          ),
        );
      case MessageStatus.sent:
        return Icon(
          Icons.check,
          size: 14,
          color: Colors.white.withValues(alpha: 0.7),
        );
      case MessageStatus.delivered:
        return Icon(
          Icons.done_all,
          size: 14,
          color: Colors.white.withValues(alpha: 0.7),
        );
      case MessageStatus.read:
        return const Icon(
          Icons.done_all,
          size: 14,
          color: AppColors.success,
        );
      case MessageStatus.failed:
        return Icon(
          Icons.error_outline,
          size: 14,
          color: theme.colorScheme.error,
        );
    }
  }

  void _showReactionPicker(BuildContext context) {
    final reactions = ['❤️', '😂', '😮', '😢', '😡', '👍'];

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              'Quick Reaction',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: reactions.map((emoji) {
                return GestureDetector(
                  onTap: () {
                    setState(() {
                      if (_reactions.contains(emoji)) {
                        _reactions.remove(emoji);
                      } else {
                        _reactions.add(emoji);
                      }
                    });
                    Navigator.pop(context);
                  },
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: _reactions.contains(emoji)
                          ? AppColors.primaryPurple.withValues(alpha: 0.2)
                          : Theme.of(context).colorScheme.surfaceContainerHighest,
                      shape: BoxShape.circle,
                    ),
                    child: Text(emoji, style: const TextStyle(fontSize: 24)),
                  ),
                );
              }).toList(),
            ),
            SizedBox(height: MediaQuery.of(context).padding.bottom + 8),
          ],
        ),
      ),
    );
  }

  String _formatTime(DateTime? time) {
    if (time == null) return '';
    final hour = time.hour > 12 ? time.hour - 12 : time.hour;
    final period = time.hour >= 12 ? 'PM' : 'AM';
    return '${hour == 0 ? 12 : hour}:${time.minute.toString().padLeft(2, '0')} $period';
  }
}

// ═══════════════════════════════════════════════════════════════
// TYPING INDICATOR — Each dot manages its own lifecycle
// ═══════════════════════════════════════════════════════════════

/// Typing indicator with animated dots.
/// Each dot is a self-contained StatefulWidget with its own controller.
class _TypingIndicator extends StatelessWidget {
  const _TypingIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          const NeuroAvatar(
            imageUrl: 'https://i.pravatar.cc/150?img=1',
            name: 'Alex',
            size: 32,
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest,
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(18),
                topRight: Radius.circular(18),
                bottomLeft: Radius.circular(4),
                bottomRight: Radius.circular(18),
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: List.generate(3, (index) {
                return _PulsingDot(
                  delay: Duration(milliseconds: index * 200),
                  color: theme.colorScheme.onSurfaceVariant,
                );
              }),
            ),
          ),
        ],
      ),
    );
  }
}

/// A single pulsing dot with its own animation controller.
/// Fully self-contained — creates and disposes its own controller safely.
class _PulsingDot extends StatefulWidget {
  final Duration delay;
  final Color color;

  const _PulsingDot({required this.delay, required this.color});

  @override
  State<_PulsingDot> createState() => _PulsingDotState();
}

class _PulsingDotState extends State<_PulsingDot>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  bool _started = false;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _startAfterDelay();
  }

  Future<void> _startAfterDelay() async {
    await Future.delayed(widget.delay);
    if (mounted && !_started) {
      _started = true;
      _controller.repeat(reverse: true);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 2),
      child: FadeTransition(
        opacity: Tween<double>(begin: 0.3, end: 1.0).animate(
          CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
        ),
        child: Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            color: widget.color,
            shape: BoxShape.circle,
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// DEV OPTIONS SHEET WIDGETS
// Matches the app's Modern Settings Card design language
// ═══════════════════════════════════════════════════════════════

class _DevCardContainer extends StatelessWidget {
  final Widget child;
  final bool isDark;

  const _DevCardContainer({required this.child, required this.isDark});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isDark ? Colors.white.withValues(alpha: 0.05) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isDark
              ? Colors.white.withValues(alpha: 0.08)
              : Colors.black.withValues(alpha: 0.06),
          width: 1,
        ),
        boxShadow: isDark
            ? null
            : [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.04),
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
      ),
      child: child,
    );
  }
}

class _DevCardHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color iconColor;

  const _DevCardHeader({
    required this.icon,
    required this.title,
    required this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Container(
          width: 32,
          height: 32,
          decoration: BoxDecoration(
            color: iconColor.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: iconColor, size: 18),
        ),
        const SizedBox(width: 10),
        Text(
          title,
          style: theme.textTheme.titleSmall?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}

class _DevInfoRow extends StatelessWidget {
  final String label;
  final String value;
  final Color? valueColor;

  const _DevInfoRow({
    required this.label,
    required this.value,
    this.valueColor,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          Text(
            value,
            style: theme.textTheme.bodySmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: valueColor,
            ),
          ),
        ],
      ),
    );
  }
}

class _DevToggleRow extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final String title;
  final String description;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _DevToggleRow({
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.description,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: () => onChanged(!value),
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: iconColor.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(icon, color: iconColor, size: 20),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  Text(
                    description,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
            Switch(
              value: value,
              onChanged: onChanged,
              activeColor: AppColors.primaryPurple,
            ),
          ],
        ),
      ),
    );
  }
}

class _DevActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final String description;
  final VoidCallback onPressed;
  final bool isDestructive;

  const _DevActionButton({
    required this.icon,
    required this.label,
    required this.description,
    required this.onPressed,
    this.isDestructive = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = isDestructive ? theme.colorScheme.error : AppColors.primaryPurple;

    return Material(
      color: color.withValues(alpha: 0.08),
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: () {
          HapticFeedback.lightImpact();
          onPressed();
        },
        borderRadius: BorderRadius.circular(14),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: color, size: 20),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      label,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: color,
                      ),
                    ),
                    Text(
                      description,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 11,
                      ),
                    ),
                  ],
                ),
              ),
              Icon(Icons.chevron_right_rounded, color: color, size: 20),
            ],
          ),
        ),
      ),
    );
  }
}

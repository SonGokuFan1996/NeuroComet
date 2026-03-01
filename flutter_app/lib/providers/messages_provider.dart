import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/conversation.dart';
import '../models/dev_options.dart';
import '../screens/settings/dev_options_screen.dart';

final conversationsProvider = NotifierProvider<ConversationsNotifier, AsyncValue<List<Conversation>>>(
  ConversationsNotifier.new,
);

class ConversationsNotifier extends Notifier<AsyncValue<List<Conversation>>> {
  /// Read current dev options (returns safe defaults in release mode)
  DevOptions get _devOptions => ref.read(devOptionsProvider);

  @override
  AsyncValue<List<Conversation>> build() {
    // Watch dev options so conversations rebuild when flags change
    ref.watch(devOptionsProvider);
    loadConversations();
    return const AsyncValue.loading();
  }

  Future<void> loadConversations() async {
    final opts = _devOptions;
    state = const AsyncValue.loading();

    // Feature flag: infinite loading – never resolve
    if (opts.infiniteLoading) {
      debugPrint('[DevFlag] infiniteLoading active – messages will stay loading');
      return;
    }

    // Feature flag: simulate loading error
    if (opts.simulateLoadingError) {
      debugPrint('[DevFlag] simulateLoadingError active – messages will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('Simulated loading error (dev flag)'),
        StackTrace.current,
      );
      return;
    }

    // Feature flag: simulate offline
    if (opts.simulateOffline) {
      debugPrint('[DevFlag] simulateOffline active – messages will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('No network connection (simulated offline)'),
        StackTrace.current,
      );
      return;
    }

    try {
      final conversations = await _fetchConversations();
      state = AsyncValue.data(conversations);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    await loadConversations();
  }

  Future<List<Conversation>> _fetchConversations() async {
    final opts = _devOptions;

    // Feature flag: artificial network latency
    final latency = opts.networkLatencyMs;
    if (latency > 0) {
      debugPrint('[DevFlag] Adding ${latency}ms artificial latency to messages');
      await Future.delayed(Duration(milliseconds: latency));
    }

    await Future.delayed(const Duration(milliseconds: 500));

    return [
      Conversation(
        id: 'conv_1',
        displayName: 'Alex Thompson',
        avatarUrl: 'https://i.pravatar.cc/150?img=1',
        lastMessage: 'Hey! How are you doing today? 😊',
        lastMessageAt: DateTime.now().subtract(const Duration(minutes: 5)),
        unreadCount: 2,
        isOnline: true,
        isPinned: true,
        isPrimary: true,
        isVerified: true,
        participantId: 'user_1',
      ),
      Conversation(
        id: 'conv_2',
        displayName: 'NeuroSupport Group',
        avatarUrl: 'https://i.pravatar.cc/150?img=2',
        lastMessage: 'Sarah: Thanks for sharing that article!',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 1)),
        unreadCount: 0,
        isOnline: false,
        isGroup: true,
        isPrimary: true,
        participantId: 'group_1',
      ),
      Conversation(
        id: 'conv_3',
        displayName: 'Jamie Wilson',
        avatarUrl: 'https://i.pravatar.cc/150?img=3',
        lastMessage: 'The new sensory room is amazing!',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 3)),
        unreadCount: 0,
        isOnline: true,
        isTyping: true,
        isPrimary: true,
        participantId: 'user_2',
      ),
      Conversation(
        id: 'conv_4',
        displayName: 'Dr. Emily Chen',
        avatarUrl: 'https://i.pravatar.cc/150?img=4',
        lastMessage: 'Your appointment is confirmed for Thursday',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 1)),
        unreadCount: 1,
        isOnline: false,
        isVerified: true,
        isPrimary: true,
        participantId: 'user_3',
      ),
      Conversation(
        id: 'conv_5',
        displayName: 'Mindfulness Circle',
        avatarUrl: 'https://i.pravatar.cc/150?img=5',
        lastMessage: 'Next session starts in 30 minutes',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 2)),
        unreadCount: 0,
        isOnline: false,
        isGroup: true,
        isMuted: true,
        participantId: 'group_2',
      ),
      Conversation(
        id: 'conv_6',
        displayName: 'Sam Rivera',
        avatarUrl: 'https://i.pravatar.cc/150?img=6',
        lastMessage: 'See you at the meetup!',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 3)),
        unreadCount: 0,
        isOnline: false,
        participantId: 'user_4',
      ),
    ];
  }

  void markAsRead(String conversationId) {
    final currentConversations = state.value;
    if (currentConversations == null) return;

    state = AsyncValue.data(
      currentConversations.map((conv) {
        if (conv.id == conversationId) {
          return conv.copyWith(unreadCount: 0);
        }
        return conv;
      }).toList(),
    );
  }

  void muteConversation(String conversationId, bool muted) {
    final currentConversations = state.value;
    if (currentConversations == null) return;

    state = AsyncValue.data(
      currentConversations.map((conv) {
        if (conv.id == conversationId) {
          return conv.copyWith(isMuted: muted);
        }
        return conv;
      }).toList(),
    );
  }

  void deleteConversation(String conversationId) {
    final currentConversations = state.value;
    if (currentConversations == null) return;

    state = AsyncValue.data(
      currentConversations.where((conv) => conv.id != conversationId).toList(),
    );
  }
}

// Provider for individual chat messages
final chatMessagesProvider = FutureProvider.family<List<Message>, String>((ref, conversationId) async {
  await Future.delayed(const Duration(milliseconds: 300));

  return List.generate(
    20,
    (index) => Message(
      id: 'msg_${conversationId}_$index',
      conversationId: conversationId,
      senderId: index % 3 == 0 ? 'current_user' : 'other_user',
      content: 'This is message #${index + 1} in the conversation.',
      createdAt: DateTime.now().subtract(Duration(minutes: index * 5)),
      status: MessageStatus.read,
    ),
  ).reversed.toList();
});

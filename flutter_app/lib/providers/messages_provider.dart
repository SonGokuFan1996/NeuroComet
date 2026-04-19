import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/conversation.dart';
import '../screens/settings/dev_options_screen.dart';
import '../services/supabase_service.dart';
import '../widgets/chat/wallpaper_engine.dart';

final conversationsProvider = NotifierProvider<ConversationsNotifier, AsyncValue<List<Conversation>>>(
  ConversationsNotifier.new,
);

final devChatMessageOverridesProvider = NotifierProvider<DevChatMessageOverridesNotifier, Map<String, List<Message>>>(
  DevChatMessageOverridesNotifier.new,
);

class DevChatMessageOverridesNotifier extends Notifier<Map<String, List<Message>>> {
  @override
  Map<String, List<Message>> build() => const <String, List<Message>>{};
}

final conversationByIdProvider = Provider.family<Conversation?, String>((ref, conversationId) {
  final conversations = ref.watch(conversationsProvider).value ?? const <Conversation>[];
  for (final conversation in conversations) {
    if (conversation.id == conversationId) return conversation;
  }
  return null;
});

final conversationByParticipantProvider = Provider.family<Conversation?, String>((ref, participantId) {
  final conversations = ref.watch(conversationsProvider).value ?? const <Conversation>[];
  for (final conversation in conversations) {
    if (conversation.participantId == participantId) return conversation;
    final participantIds = conversation.participantIds;
    if (participantIds != null && participantIds.contains(participantId)) {
      return conversation;
    }
  }
  return null;
});

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
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] infiniteLoading active – messages will stay loading');
      return;
    }

    // Feature flag: simulate loading error
    if (opts.simulateLoadingError) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] simulateLoadingError active – messages will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('Simulated loading error (dev flag)'),
        StackTrace.current,
      );
      return;
    }

    // Feature flag: simulate offline
    if (opts.simulateOffline) {
      if (opts.enableVerboseLogging) debugPrint('[DevFlag] simulateOffline active – messages will error');
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

    // Use Supabase when authenticated with a real user
    // Skip Supabase when devModeSkipAuth is active (no real user) so mock data is used
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && SupabaseService.currentUser != null) {
      try {
        final conversations = await SupabaseService.getConversations();
        debugPrint('[Messages] Fetched ${conversations.length} conversations from Supabase');
        return conversations;
      } catch (e) {
        debugPrint('[Messages] Supabase fetch failed: $e — using demo data');
      }
    }

    // Fallback demo data (only when not authenticated or on error)
    return [
      Conversation(
        id: 'conv_1',
        displayName: 'Alex Thompson',
        avatarUrl: 'https://i.pravatar.cc/150?u=alex_thompson',
        lastMessage: 'Hey! The new weighted blanket is a game changer 😊',
        lastMessageAt: DateTime.now().subtract(const Duration(minutes: 2)),
        unreadCount: 3,
        isOnline: true,
        isPinned: true,
        isPrimary: true,
        isVerified: true,
        participantId: 'user_1',
      ),
      Conversation(
        id: 'conv_2',
        displayName: 'ADHD Support Circle',
        avatarUrl: 'https://i.pravatar.cc/150?u=adhd_support_circle',
        lastMessage: 'Sarah: Body doubling session in 10 mins! 💪',
        lastMessageAt: DateTime.now().subtract(const Duration(minutes: 15)),
        unreadCount: 7,
        isOnline: false,
        isGroup: true,
        isPrimary: true,
        participantId: 'group_1',
        participantIds: ['user_1', 'user_2', 'user_5', 'user_6', 'user_7'],
        groupName: 'ADHD Support Circle',
        memberNames: ['You', 'Alex Thompson', 'Jamie Wilson', 'Morgan Blue', 'Taylor Kim', 'Riley Chen'],
      ),
      Conversation(
        id: 'conv_3',
        displayName: 'Jamie Wilson',
        avatarUrl: 'https://i.pravatar.cc/150?u=jamie_wilson',
        lastMessage: 'That fidget cube you recommended is perfect for meetings!',
        lastMessageAt: DateTime.now().subtract(const Duration(minutes: 45)),
        unreadCount: 0,
        isOnline: true,
        isTyping: true,
        isPrimary: true,
        participantId: 'user_2',
      ),
      Conversation(
        id: 'conv_4',
        displayName: 'Dr. Emily Chen',
        avatarUrl: 'https://i.pravatar.cc/150?u=dr_emily_chen',
        lastMessage: 'Your assessment results are ready. Shall we schedule a follow-up?',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 2)),
        unreadCount: 1,
        isOnline: false,
        isVerified: true,
        isPrimary: true,
        participantId: 'user_3',
        moderationStatus: ModerationStatus.verified,
      ),
      Conversation(
        id: 'conv_5',
        displayName: 'Sensory-Friendly Spaces',
        avatarUrl: 'https://i.pravatar.cc/150?u=sensory_spaces',
        lastMessage: 'New quiet room added to the downtown location! 🎧',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 5)),
        unreadCount: 0,
        isOnline: false,
        isGroup: true,
        isMuted: true,
        participantId: 'group_2',
        participantIds: ['user_1', 'user_3', 'user_8'],
        groupName: 'Sensory-Friendly Spaces',
        memberNames: ['You', 'Dr. Emily Chen', 'Casey Park'],
      ),
      Conversation(
        id: 'conv_6',
        displayName: 'Sam Rivera',
        avatarUrl: 'https://i.pravatar.cc/150?u=sam_rivera',
        lastMessage: 'My special interest presentation went great! 🎉',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 8)),
        unreadCount: 0,
        isOnline: true,
        participantId: 'user_4',
      ),
      Conversation(
        id: 'conv_7',
        displayName: 'Morgan Blue',
        avatarUrl: 'https://i.pravatar.cc/150?u=morgan_blue',
        lastMessage: 'The noise-canceling earbuds arrived! Testing them tomorrow',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 12)),
        unreadCount: 0,
        isOnline: false,
        isPinned: true,
        participantId: 'user_5',
      ),
      Conversation(
        id: 'conv_8',
        displayName: 'Neurodivergent Gamers',
        avatarUrl: 'https://i.pravatar.cc/150?u=nd_gamers',
        lastMessage: 'Riley: Anyone want to co-op tonight? Low pressure, no voice chat required 🎮',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 1)),
        unreadCount: 12,
        isOnline: false,
        isGroup: true,
        participantId: 'group_3',
        participantIds: ['user_2', 'user_4', 'user_6', 'user_9'],
        groupName: 'Neurodivergent Gamers',
        memberNames: ['You', 'Jamie Wilson', 'Sam Rivera', 'Taylor Kim', 'Riley Chen'],
      ),
      Conversation(
        id: 'conv_11',
        displayName: 'Stim Toy Reviews 🧸',
        avatarUrl: 'https://i.pravatar.cc/150?u=group_stimtoy',
        lastMessage: 'Jordan: The texture on the new infinity cube is amazing!',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 3)),
        unreadCount: 4,
        isOnline: false,
        isGroup: true,
        participantId: 'group_4',
        participantIds: ['user_1', 'user_2', 'user_4', 'user_8'],
        groupName: 'Stim Toy Reviews 🧸',
        memberNames: ['You', 'Alex Thompson', 'Jamie Wilson', 'Sam Rivera', 'Casey Park'],
      ),
      Conversation(
        id: 'conv_12',
        displayName: 'Queer ND Squad 🌈♾️',
        avatarUrl: 'https://i.pravatar.cc/150?u=group_queernd',
        lastMessage: 'Robin: Built a rainbow gradient animation for my app! 🌈',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 1)),
        unreadCount: 5,
        isOnline: false,
        isGroup: true,
        isPrimary: true,
        participantId: 'group_5',
        participantIds: ['user_1', 'user_4', 'user_7', 'user_8', 'user_10'],
        groupName: 'Queer ND Squad 🌈♾️',
        memberNames: ['You', 'Alex Thompson', 'Sam Rivera', 'Taylor Kim', 'Casey Park', 'Robin Lee'],
      ),
      Conversation(
        id: 'conv_13',
        displayName: 'Focus Friends 🎯',
        avatarUrl: 'https://i.pravatar.cc/150?u=group_focusfriends',
        lastMessage: 'Alex: Body doubling session starting in 10! Who\'s in?',
        lastMessageAt: DateTime.now().subtract(const Duration(hours: 6)),
        unreadCount: 0,
        isOnline: false,
        isGroup: true,
        participantId: 'group_6',
        participantIds: ['user_1', 'user_3', 'user_5', 'user_6'],
        groupName: 'Focus Friends 🎯',
        memberNames: ['You', 'Alex Thompson', 'Dr. Emily Chen', 'Morgan Blue', 'Taylor Kim'],
      ),
      Conversation(
        id: 'conv_9',
        displayName: 'Taylor Kim',
        avatarUrl: 'https://i.pravatar.cc/150?u=taylor_kim',
        lastMessage: 'Reminder: You\'re doing great. Executive dysfunction doesn\'t define you 💜',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 2)),
        unreadCount: 0,
        isOnline: false,
        participantId: 'user_6',
      ),
      Conversation(
        id: 'conv_10',
        displayName: 'Blocked User',
        avatarUrl: 'https://i.pravatar.cc/150?u=blocked_user',
        lastMessage: 'This conversation has been blocked.',
        lastMessageAt: DateTime.now().subtract(const Duration(days: 5)),
        unreadCount: 0,
        isOnline: false,
        isBlocked: true,
        participantId: 'user_blocked',
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

    final overrides = Map<String, List<Message>>.from(ref.read(devChatMessageOverridesProvider));
    overrides.remove(conversationId);
    ref.read(devChatMessageOverridesProvider.notifier).state = overrides;
  }

  void injectDevConversation(
    Conversation conversation, {
    List<Message> messages = const <Message>[],
  }) {
    final currentConversations = state.value ?? const <Conversation>[];
    final merged = <Conversation>[
      conversation,
      ...currentConversations.where((conv) => conv.id != conversation.id),
    ]
      ..sort((a, b) {
        final aTime = a.lastMessageAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        final bTime = b.lastMessageAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        return bTime.compareTo(aTime);
      });

    state = AsyncValue.data(merged);

    if (messages.isNotEmpty) {
      ref.read(devChatMessageOverridesProvider.notifier).state = {
        ...ref.read(devChatMessageOverridesProvider),
        conversation.id: messages,
      };
    }
  }

  void clearDevInjectedConversations() {
    final currentConversations = state.value ?? const <Conversation>[];
    state = AsyncValue.data(
      currentConversations.where((conv) => !conv.id.startsWith('dev_')).toList(),
    );

    final overrides = Map<String, List<Message>>.from(ref.read(devChatMessageOverridesProvider));
    overrides.removeWhere((conversationId, _) => conversationId.startsWith('dev_'));
    ref.read(devChatMessageOverridesProvider.notifier).state = overrides;
  }
}

// Provider for individual chat messages
final chatMessagesProvider = FutureProvider.family<List<Message>, String>((ref, conversationId) async {
  final devOverrides = ref.watch(devChatMessageOverridesProvider);
  final overrideMessages = devOverrides[conversationId];
  if (overrideMessages != null) {
    return overrideMessages;
  }

  // Use Supabase when authenticated with a real user
  if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && SupabaseService.currentUser != null) {
    try {
      final messages = await SupabaseService.getMessages(conversationId);
      debugPrint('[Chat] Fetched ${messages.length} messages from Supabase for $conversationId');
      return messages;
    } catch (e) {
      debugPrint('[Chat] Supabase fetch failed for $conversationId: $e');
    }
  }

  // Fallback demo data (only when not authenticated or on error)
  // Provide realistic ND-themed conversations so every chat feature can be tested
  final now = DateTime.now();
  final sampleMessages = <String, List<Message>>{
    'conv_1': [
      Message(id: 'msg_1_1', conversationId: 'conv_1', senderId: 'user_1', content: 'Hey! Have you tried that new weighted blanket yet?', createdAt: now.subtract(const Duration(hours: 2)), status: MessageStatus.read),
      Message(id: 'msg_1_2', conversationId: 'conv_1', senderId: 'current_user', content: 'Yes!! It\'s amazing. 15 lbs of pure comfort 💙', createdAt: now.subtract(const Duration(hours: 1, minutes: 55)), status: MessageStatus.read),
      Message(id: 'msg_1_3', conversationId: 'conv_1', senderId: 'user_1', content: 'Right?? I slept through the whole night for the first time in weeks', createdAt: now.subtract(const Duration(hours: 1, minutes: 50)), status: MessageStatus.read),
      Message(id: 'msg_1_4', conversationId: 'conv_1', senderId: 'current_user', content: 'The deep pressure really does help with regulation. My OT was right!', createdAt: now.subtract(const Duration(hours: 1, minutes: 45)), status: MessageStatus.read),
      Message(id: 'msg_1_5', conversationId: 'conv_1', senderId: 'user_1', content: 'Also got noise-canceling earbuds to go with it. My sensory kit is complete 🎧', createdAt: now.subtract(const Duration(hours: 1, minutes: 30)), status: MessageStatus.read),
      Message(id: 'msg_1_6', conversationId: 'conv_1', senderId: 'current_user', content: 'Smart! Which ones did you get?', createdAt: now.subtract(const Duration(hours: 1, minutes: 25)), status: MessageStatus.read),
      Message(id: 'msg_1_7', conversationId: 'conv_1', senderId: 'user_1', content: 'Sony WH-1000XM5. The active noise canceling is incredible for grocery stores', createdAt: now.subtract(const Duration(hours: 1, minutes: 20)), status: MessageStatus.read),
      Message(id: 'msg_1_8', conversationId: 'conv_1', senderId: 'current_user', content: 'Adding to my wishlist right now 📝', createdAt: now.subtract(const Duration(hours: 1, minutes: 10)), status: MessageStatus.read, reactions: [const MessageReaction(emoji: '❤️', userId: 'user_1')]),
      Message(id: 'msg_1_9', conversationId: 'conv_1', senderId: 'user_1', content: 'Hey! The new weighted blanket is a game changer 😊', createdAt: now.subtract(const Duration(minutes: 2)), status: MessageStatus.delivered),
    ],
    'conv_2': [
      Message(id: 'msg_2_1', conversationId: 'conv_2', senderId: 'user_5', content: 'Welcome to the ADHD Support Circle! Please introduce yourself 🌟', createdAt: now.subtract(const Duration(days: 3)), status: MessageStatus.read),
      Message(id: 'msg_2_2', conversationId: 'conv_2', senderId: 'user_6', content: 'Hi! I\'m Riley. Diagnosed at 27. Still figuring things out!', createdAt: now.subtract(const Duration(days: 3)).add(const Duration(minutes: 5)), status: MessageStatus.read),
      Message(id: 'msg_2_3', conversationId: 'conv_2', senderId: 'current_user', content: 'Hey Riley! Late diagnosis club here too. This group has been amazing for me.', createdAt: now.subtract(const Duration(days: 3)).add(const Duration(minutes: 10)), status: MessageStatus.read),
      Message(id: 'msg_2_4', conversationId: 'conv_2', senderId: 'user_1', content: 'Anyone tried the Pomodoro technique with shorter intervals? 15 min work / 5 min break works way better for me than 25/5', createdAt: now.subtract(const Duration(hours: 6)), status: MessageStatus.read),
      Message(id: 'msg_2_5', conversationId: 'conv_2', senderId: 'user_7', content: 'I do 10/3! Super short bursts match my attention span 😅', createdAt: now.subtract(const Duration(hours: 5, minutes: 50)), status: MessageStatus.read),
      Message(id: 'msg_2_6', conversationId: 'conv_2', senderId: 'current_user', content: 'Body doubling has been my game changer. Just being in a virtual room with others who are working helps so much', createdAt: now.subtract(const Duration(hours: 3)), status: MessageStatus.read),
      Message(id: 'msg_2_7', conversationId: 'conv_2', senderId: 'user_2', content: 'Sarah: Body doubling session in 10 mins! 💪', createdAt: now.subtract(const Duration(minutes: 15)), status: MessageStatus.delivered),
    ],
    'conv_3': [
      Message(id: 'msg_3_1', conversationId: 'conv_3', senderId: 'current_user', content: 'I found this amazing fidget cube for meetings', createdAt: now.subtract(const Duration(days: 1)), status: MessageStatus.read),
      Message(id: 'msg_3_2', conversationId: 'conv_3', senderId: 'user_2', content: 'Ooh link please! I need something quiet for Zoom calls', createdAt: now.subtract(const Duration(days: 1)).add(const Duration(minutes: 5)), status: MessageStatus.read),
      Message(id: 'msg_3_3', conversationId: 'conv_3', senderId: 'current_user', content: 'It\'s the Infinity Cube — silent, satisfying, fits in your palm 🌀', createdAt: now.subtract(const Duration(days: 1)).add(const Duration(minutes: 8)), status: MessageStatus.read),
      Message(id: 'msg_3_4', conversationId: 'conv_3', senderId: 'user_2', content: 'Ordered! Also my therapist suggested a "sensory menu" — a list of stim tools matched to different situations', createdAt: now.subtract(const Duration(hours: 4)), status: MessageStatus.read),
      Message(id: 'msg_3_5', conversationId: 'conv_3', senderId: 'current_user', content: 'That\'s brilliant! I should make one. Meeting stims, commute stims, bedtime stims...', createdAt: now.subtract(const Duration(hours: 3, minutes: 50)), status: MessageStatus.read),
      Message(id: 'msg_3_6', conversationId: 'conv_3', senderId: 'user_2', content: 'That fidget cube you recommended is perfect for meetings!', createdAt: now.subtract(const Duration(minutes: 45)), status: MessageStatus.delivered),
    ],
    'conv_4': [
      Message(id: 'msg_4_1', conversationId: 'conv_4', senderId: 'user_3', content: 'Hi! Just following up on your recent assessment. All results look good.', createdAt: now.subtract(const Duration(days: 5)), status: MessageStatus.read),
      Message(id: 'msg_4_2', conversationId: 'conv_4', senderId: 'current_user', content: 'Thank you Dr. Chen! The new strategies are helping a lot.', createdAt: now.subtract(const Duration(days: 5)).add(const Duration(hours: 2)), status: MessageStatus.read),
      Message(id: 'msg_4_3', conversationId: 'conv_4', senderId: 'user_3', content: 'Glad to hear that. Remember: progress isn\'t linear, especially with ADHD. Be patient with yourself.', createdAt: now.subtract(const Duration(days: 4)), status: MessageStatus.read),
      Message(id: 'msg_4_4', conversationId: 'conv_4', senderId: 'user_3', content: 'Your assessment results are ready. Shall we schedule a follow-up?', createdAt: now.subtract(const Duration(hours: 2)), status: MessageStatus.delivered),
    ],
  };

  return sampleMessages[conversationId] ?? List.generate(
    15,
    (index) {
      final isMe = index % 3 == 0;
      final contents = [
        'Have you tried the 5-4-3-2-1 grounding technique?',
        'My executive function said "not today" 😅',
        'Found a great new stim toy at the store!',
        'Reminder: rest is productive too 💜',
        'The sensory room at the library is a lifesaver',
        'Anyone else get overwhelmed by fluorescent lights?',
        'Pro tip: timer apps with visual countdowns help so much',
        'Just had the best therapy session!',
        'Sharing this article about masking — really eye-opening',
        'Your brain isn\'t broken, it\'s just wired differently 🧠',
        'New noise machine arrived. It plays brown noise! 🎵',
        'Body doubling works even over video call!',
        'Made it through the grocery store without a meltdown today 🎉',
        'The texture of this new shirt is chef\'s kiss ✨',
        'Sometimes I need to remind myself that "good enough" IS enough',
      ];
      return Message(
        id: 'msg_${conversationId}_$index',
        conversationId: conversationId,
        senderId: isMe ? 'current_user' : 'other_user',
        content: contents[index % contents.length],
        createdAt: now.subtract(Duration(minutes: (15 - index) * 8)),
        status: isMe ? MessageStatus.read : MessageStatus.delivered,
        reactions: index == 3 ? [const MessageReaction(emoji: '💜', userId: 'other_user')] : [],
      );
    },
  );
});

// Provider for conversation wallpaper
final conversationWallpaperProvider = NotifierProvider<ConversationWallpaperNotifier, Map<String, ConversationWallpaper>>(
  ConversationWallpaperNotifier.new,
);

class ConversationWallpaperNotifier extends Notifier<Map<String, ConversationWallpaper>> {
  @override
  Map<String, ConversationWallpaper> build() {
    _loadFromPrefs();
    return {};
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final keys = prefs.getKeys().where((k) => k.startsWith('wallpaper_'));
    final map = <String, ConversationWallpaper>{};
    for (final key in keys) {
      final convId = key.replaceFirst('wallpaper_', '');
      final value = prefs.getString(key);
      if (value != null) {
        map[convId] = ConversationWallpaper.values.firstWhere(
          (e) => e.name == value,
          orElse: () => ConversationWallpaper.none,
        );
      }
    }
    state = map;
  }

  Future<void> setWallpaper(String conversationId, ConversationWallpaper wallpaper) async {
    final prefs = await SharedPreferences.getInstance();
    if (wallpaper == ConversationWallpaper.none) {
      await prefs.remove('wallpaper_$conversationId');
      final newState = Map<String, ConversationWallpaper>.from(state);
      newState.remove(conversationId);
      state = newState;
    } else {
      await prefs.setString('wallpaper_$conversationId', wallpaper.name);
      state = {
        ...state,
        conversationId: wallpaper,
      };
    }
  }

  ConversationWallpaper getWallpaper(String? conversationId) {
    if (conversationId == null) return ConversationWallpaper.none;
    return state[conversationId] ?? ConversationWallpaper.none;
  }
}

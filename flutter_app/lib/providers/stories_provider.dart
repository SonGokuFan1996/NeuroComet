import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/story.dart';
import '../screens/settings/dev_options_screen.dart';
import '../services/supabase_service.dart';

/// Provider for managing stories
final storiesProvider = NotifierProvider<StoriesNotifier, AsyncValue<List<StoryGroup>>>(
  StoriesNotifier.new,
);

/// Provider for the current user's stories
final myStoriesProvider = Provider<List<Story>>((ref) {
  final storiesState = ref.watch(storiesProvider);
  return storiesState.maybeWhen(
    data: (groups) {
      final myGroup = groups.where((g) => g.userId == 'current_user').firstOrNull;
      return myGroup?.stories ?? [];
    },
    orElse: () => [],
  );
});

/// Provider for story creation state
final storyCreationProvider = NotifierProvider<StoryCreationNotifier, StoryCreationState>(
  StoryCreationNotifier.new,
);

/// Notifier for managing story groups
class StoriesNotifier extends Notifier<AsyncValue<List<StoryGroup>>> {
  /// Read current dev options (returns safe defaults in release mode)
  DevOptions get _devOptions => ref.read(devOptionsProvider);

  @override
  AsyncValue<List<StoryGroup>> build() {
    // Watch dev options so stories rebuild when flags change
    ref.watch(devOptionsProvider);
    loadStories();
    return const AsyncValue.loading();
  }

  Future<void> loadStories() async {
    final opts = _devOptions;
    state = const AsyncValue.loading();

    // Feature flag: infinite loading – never resolve
    if (opts.infiniteLoading) {
      debugPrint('[DevFlag] infiniteLoading active – stories will stay loading');
      return;
    }

    // Feature flag: simulate loading error
    if (opts.simulateLoadingError) {
      debugPrint('[DevFlag] simulateLoadingError active – stories will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('Simulated loading error (dev flag)'),
        StackTrace.current,
      );
      return;
    }

    // Feature flag: simulate offline
    if (opts.simulateOffline) {
      debugPrint('[DevFlag] simulateOffline active – stories will error');
      await Future.delayed(const Duration(milliseconds: 300));
      state = AsyncValue.error(
        Exception('No network connection (simulated offline)'),
        StackTrace.current,
      );
      return;
    }

    try {
      final stories = await _fetchStories();
      state = AsyncValue.data(stories);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> refresh() async {
    final opts = _devOptions;
    if (opts.infiniteLoading || opts.simulateLoadingError || opts.simulateOffline) {
      await loadStories();
      return;
    }
    try {
      final stories = await _fetchStories();
      state = AsyncValue.data(stories);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  /// Inject a local test story (for dev tools)
  void injectLocalStory(Story story) {
    final currentGroups = state.value ?? [];
    final existingGroupIndex = currentGroups.indexWhere((g) => g.userId == story.authorId);
    final updatedGroups = List<StoryGroup>.from(currentGroups);

    if (existingGroupIndex >= 0) {
      final existing = updatedGroups[existingGroupIndex];
      updatedGroups[existingGroupIndex] = StoryGroup(
        userId: existing.userId,
        userName: existing.userName,
        userAvatarUrl: existing.userAvatarUrl,
        stories: [...existing.stories, story],
        hasUnseenStories: true,
      );
    } else {
      updatedGroups.insert(0, StoryGroup(
        userId: story.authorId,
        userName: story.authorName,
        userAvatarUrl: story.authorAvatarUrl,
        stories: [story],
        hasUnseenStories: true,
      ));
    }

    state = AsyncValue.data(updatedGroups);
  }

  Future<List<StoryGroup>> _fetchStories() async {
    final opts = _devOptions;

    // Feature flag: artificial network latency
    final latency = opts.networkLatencyMs;
    if (latency > 0) {
      debugPrint('[DevFlag] Adding ${latency}ms artificial latency to stories');
      await Future.delayed(Duration(milliseconds: latency));
    }

    // Try fetching real stories from Supabase when authenticated with a real user
    if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && SupabaseService.currentUser != null) {
      try {
        final response = await SupabaseService.client
            .from('stories')
            .select('*')
            .gte('expires_at', DateTime.now().toIso8601String())
            .order('created_at', ascending: false)
            .limit(50);

        debugPrint('[Stories] Fetched ${(response as List).length} stories from Supabase');
        final groups = _parseSupabaseStories(response);
        // Always ensure "Your Story" placeholder exists
        final currentUid = SupabaseService.currentUser?.id ?? 'current_user';
        if (!groups.any((g) => g.userId == currentUid)) {
          groups.insert(0, StoryGroup(
            userId: currentUid,
            userName: 'Your Story',
            userAvatarUrl: null,
            stories: [],
            hasUnseenStories: false,
          ));
        }
        return groups;
      } catch (e) {
        debugPrint('[Stories] Supabase fetch failed: $e — falling back to demo data');
      }
    }

    // Fallback: demo data (only when not authenticated or on error)
    final now = DateTime.now();
    final expiresAt = now.add(const Duration(hours: 24));

    return [
      // Current user's stories (if any)
      StoryGroup(
        userId: 'current_user',
        userName: 'Your Story',
        userAvatarUrl: null,
        stories: [],
        hasUnseenStories: false,
      ),
      // Other users' stories
      StoryGroup(
        userId: 'user_1',
        userName: 'Alex',
        userAvatarUrl: 'https://i.pravatar.cc/150?u=story_alex',
        stories: [
          Story(
            id: 'story_1',
            authorId: 'user_1',
            authorName: 'Alex',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=story_alex',
            contentType: StoryContentType.text,
            caption: 'Feeling grateful today! 🌟',
            backgroundColor: '#7C4DFF',
            backgroundGradient: '["#7C4DFF", "#536DFE"]',
            mood: '😊',
            createdAt: now.subtract(const Duration(hours: 2)),
            expiresAt: expiresAt,
            viewCount: 45,
          ),
        ],
        hasUnseenStories: true,
      ),
      StoryGroup(
        userId: 'user_2',
        userName: 'Sam',
        userAvatarUrl: 'https://i.pravatar.cc/150?u=story_sam',
        stories: [
          Story(
            id: 'story_2',
            authorId: 'user_2',
            authorName: 'Sam',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=story_sam',
            contentType: StoryContentType.photo,
            mediaUrl: 'https://picsum.photos/400/700?random=1',
            caption: 'Beautiful day for a walk! 🌳',
            filter: 'warm',
            createdAt: now.subtract(const Duration(hours: 5)),
            expiresAt: expiresAt,
            viewCount: 89,
          ),
          Story(
            id: 'story_2b',
            authorId: 'user_2',
            authorName: 'Sam',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=story_sam',
            contentType: StoryContentType.text,
            caption: 'Self-care Sunday 💜',
            backgroundColor: '#00BFA5',
            backgroundGradient: '["#00BFA5", "#26C6DA"]',
            createdAt: now.subtract(const Duration(hours: 3)),
            expiresAt: expiresAt,
            viewCount: 62,
          ),
        ],
        hasUnseenStories: true,
      ),
      StoryGroup(
        userId: 'user_3',
        userName: 'Jordan',
        userAvatarUrl: 'https://i.pravatar.cc/150?u=story_jordan',
        stories: [
          Story(
            id: 'story_3',
            authorId: 'user_3',
            authorName: 'Jordan',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=story_jordan',
            contentType: StoryContentType.text,
            caption: 'New hyperfocus: learning guitar! 🎸',
            backgroundColor: '#FF6E40',
            backgroundGradient: '["#FF6E40", "#FFAB40"]',
            mood: '🎉',
            createdAt: now.subtract(const Duration(hours: 8)),
            expiresAt: expiresAt,
            viewCount: 34,
            isViewed: true,
          ),
        ],
        hasUnseenStories: false,
      ),
      StoryGroup(
        userId: 'user_4',
        userName: 'Taylor',
        userAvatarUrl: 'https://i.pravatar.cc/150?u=story_taylor',
        stories: [
          Story(
            id: 'story_4',
            authorId: 'user_4',
            authorName: 'Taylor',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=story_taylor',
            contentType: StoryContentType.photo,
            mediaUrl: 'https://picsum.photos/400/700?random=2',
            caption: 'Art therapy session ✨',
            filter: 'vintage',
            createdAt: now.subtract(const Duration(hours: 12)),
            expiresAt: expiresAt,
            viewCount: 156,
          ),
        ],
        hasUnseenStories: true,
      ),
    ];
  }

  /// Mark a story as viewed
  void markStoryViewed(String storyId) {
    final currentGroups = state.value;
    if (currentGroups == null) return;

    state = AsyncValue.data(
      currentGroups.map((group) {
        return StoryGroup(
          userId: group.userId,
          userName: group.userName,
          userAvatarUrl: group.userAvatarUrl,
          stories: group.stories.map((story) {
            if (story.id == storyId) {
              return story.copyWith(
                isViewed: true,
                viewCount: story.viewCount + 1,
              );
            }
            return story;
          }).toList(),
          hasUnseenStories: group.stories.any((s) => !s.isViewed && s.id != storyId),
        );
      }).toList(),
    );
  }

  /// Add a new story (after creation)
  void addStory(Story story) {
    final currentGroups = state.value;
    if (currentGroups == null) return;

    final updatedGroups = currentGroups.map((group) {
      if (group.userId == 'current_user') {
        return StoryGroup(
          userId: group.userId,
          userName: group.userName,
          userAvatarUrl: group.userAvatarUrl,
          stories: [...group.stories, story],
          hasUnseenStories: group.hasUnseenStories,
        );
      }
      return group;
    }).toList();

    state = AsyncValue.data(updatedGroups);
  }

  /// Delete a story
  void deleteStory(String storyId) {
    final currentGroups = state.value;
    if (currentGroups == null) return;

    state = AsyncValue.data(
      currentGroups.map((group) {
        return StoryGroup(
          userId: group.userId,
          userName: group.userName,
          userAvatarUrl: group.userAvatarUrl,
          stories: group.stories.where((s) => s.id != storyId).toList(),
          hasUnseenStories: group.hasUnseenStories,
        );
      }).toList(),
    );
  }

  // ─── Dev injection helpers (parity with Android FeedViewModel) ───

  /// Inject a single image story – verifies basic display, progress bar, auto-advance.
  void devAddSingleImageStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    injectLocalStory(Story(
      id: 'si-dev-$ts',
      authorId: 'dev_tester',
      authorName: 'DevTester',
      authorAvatarUrl: 'https://i.pravatar.cc/150?u=dev_tester',
      contentType: StoryContentType.photo,
      mediaUrl: 'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080',
      caption: 'Single image story from dev tools',
      createdAt: DateTime.now(),
      expiresAt: DateTime.now().add(const Duration(hours: 24)),
    ));
  }

  /// Inject a multi-item story (3 pages) – tests navigation, progress bar segments, auto-advance.
  void devAddMultiItemStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    final now = DateTime.now();
    final expires = now.add(const Duration(hours: 24));
    const authorId = 'dev_multi';
    const authorName = 'MultiStory';
    const avatar = 'https://i.pravatar.cc/150?u=dev_multistory';

    final stories = [
      Story(
        id: 'si-m1-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.photo,
        mediaUrl: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1080',
        caption: 'Page 1 of 3',
        createdAt: now.subtract(const Duration(minutes: 2)), expiresAt: expires,
      ),
      Story(
        id: 'si-m2-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.photo,
        mediaUrl: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1080',
        caption: 'Page 2 of 3',
        createdAt: now.subtract(const Duration(minutes: 1)), expiresAt: expires,
      ),
      Story(
        id: 'si-m3-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.photo,
        mediaUrl: 'https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=1080',
        caption: 'Page 3 of 3',
        createdAt: now, expiresAt: expires,
      ),
    ];
    for (final s in stories) {
      injectLocalStory(s);
    }
  }

  /// Inject a text-only story with gradient background.
  void devAddTextOnlyStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    injectLocalStory(Story(
      id: 'si-txt-$ts',
      authorId: 'dev_text',
      authorName: 'TextPoster',
      authorAvatarUrl: 'https://i.pravatar.cc/150?u=dev_textposter',
      contentType: StoryContentType.text,
      caption: "Today's vibe: hyperfocusing on accessibility improvements 🧠✨",
      backgroundColor: '#6A1B9A',
      backgroundGradient: '["#6A1B9A", "#0D47A1"]',
      mood: '🧠',
      createdAt: DateTime.now(),
      expiresAt: DateTime.now().add(const Duration(hours: 24)),
    ));
  }

  /// Inject a link-preview story.
  void devAddLinkPreviewStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    injectLocalStory(Story(
      id: 'si-link-$ts',
      authorId: 'dev_link',
      authorName: 'LinkSharer',
      authorAvatarUrl: 'https://i.pravatar.cc/150?u=dev_linksharer',
      contentType: StoryContentType.link,
      mediaUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080',
      textOverlay: 'Check out this awesome resource!',
      linkPreview: const LinkPreviewData(
        url: 'https://www.neurodivergent-resources.org',
        title: 'Neurodivergent Resources Hub',
        description: 'A curated collection of tools, tips, and communities for neurodivergent individuals.',
        imageUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400',
        siteName: 'ND Resources',
      ),
      createdAt: DateTime.now(),
      expiresAt: DateTime.now().add(const Duration(hours: 24)),
    ));
  }

  /// Inject a story as the current user (tests own-story UI: delete button, "Your Story").
  void devAddOwnStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    final uid = SupabaseService.currentUser?.id ?? 'current_user';
    addStory(Story(
      id: 'si-own-$ts',
      authorId: uid,
      authorName: 'You',
      contentType: StoryContentType.photo,
      mediaUrl: 'https://images.unsplash.com/photo-1484480974693-6ca0a78fb36b?w=1080',
      textOverlay: 'My dev test story! 🚀',
      caption: 'My dev test story! 🚀',
      createdAt: DateTime.now(),
      expiresAt: DateTime.now().add(const Duration(hours: 24)),
    ));
  }

  /// Kitchen-sink story: 5 pages covering every content type in one multi-page story.
  void devAddKitchenSinkStory() {
    final ts = DateTime.now().millisecondsSinceEpoch;
    final now = DateTime.now();
    final expires = now.add(const Duration(hours: 24));
    const authorId = 'dev_kitchen';
    const authorName = 'KitchenSink';
    const avatar = 'https://i.pravatar.cc/150?u=dev_kitchensink';

    final pages = [
      Story(
        id: 'si-ks1-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.photo,
        mediaUrl: 'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1080',
        textOverlay: 'Page 1: Image with overlay',
        caption: 'Page 1: Image with overlay',
        createdAt: now.subtract(const Duration(minutes: 4)), expiresAt: expires,
      ),
      Story(
        id: 'si-ks2-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.text,
        caption: 'Page 2: Text-only with gradient 🎨',
        backgroundColor: '#E91E63',
        backgroundGradient: '["#E91E63", "#FF9800"]',
        createdAt: now.subtract(const Duration(minutes: 3)), expiresAt: expires,
      ),
      Story(
        id: 'si-ks3-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.link,
        mediaUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1080',
        textOverlay: 'Page 3: Link preview',
        caption: 'Page 3: Link preview',
        linkPreview: const LinkPreviewData(
          url: 'https://github.com/neurocomet',
          title: 'NeuroComet on GitHub',
          description: 'Open-source neurodivergent social platform',
          siteName: 'GitHub',
        ),
        createdAt: now.subtract(const Duration(minutes: 2)), expiresAt: expires,
      ),
      Story(
        id: 'si-ks4-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.photo,
        mediaUrl: 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1080',
        caption: 'Page 4: Another image',
        createdAt: now.subtract(const Duration(minutes: 1)), expiresAt: expires,
      ),
      Story(
        id: 'si-ks5-$ts', authorId: authorId, authorName: authorName, authorAvatarUrl: avatar,
        contentType: StoryContentType.text,
        caption: 'Page 5: Final page — does auto-dismiss work? ✅',
        backgroundColor: '#1B5E20',
        backgroundGradient: '["#1B5E20", "#004D40"]',
        createdAt: now, expiresAt: expires,
      ),
    ];
    for (final page in pages) {
      injectLocalStory(page);
    }
  }

  /// Bulk-inject stories to stress-test the stories bar, scrolling, and performance.
  void devFloodStories(int count) {
    final ts = DateTime.now().millisecondsSinceEpoch;
    final now = DateTime.now();
    final expires = now.add(const Duration(hours: 24));
    for (int i = 1; i <= count; i++) {
      injectLocalStory(Story(
        id: 'si-flood-$ts-$i',
        authorId: 'dev_flood_$i',
        authorName: 'Flood #$i',
        authorAvatarUrl: 'https://i.pravatar.cc/150?u=flood_$i',
        contentType: StoryContentType.photo,
        mediaUrl: 'https://picsum.photos/seed/nc$i/1080/1920',
        caption: 'Flood story $i of $count',
        createdAt: now.subtract(Duration(minutes: count - i)),
        expiresAt: expires,
      ));
    }
  }

  /// Clear all dev/injected stories, resetting back to demo data.
  Future<void> devClearStories() async {
    state = const AsyncValue.loading();
    await Future.delayed(const Duration(milliseconds: 200));
    await loadStories();
  }

  /// Add a reaction to a story
  void addReaction(String storyId, String emoji) {
    final currentGroups = state.value;
    if (currentGroups == null) return;

    final reaction = StoryReaction(
      id: 'reaction_${DateTime.now().millisecondsSinceEpoch}',
      storyId: storyId,
      userId: 'current_user',
      userName: 'You',
      emoji: emoji,
      createdAt: DateTime.now(),
    );

    state = AsyncValue.data(
      currentGroups.map((group) {
        return StoryGroup(
          userId: group.userId,
          userName: group.userName,
          userAvatarUrl: group.userAvatarUrl,
          stories: group.stories.map((story) {
            if (story.id == storyId) {
              return story.copyWith(
                reactions: [...story.reactions, reaction],
              );
            }
            return story;
          }).toList(),
          hasUnseenStories: group.hasUnseenStories,
        );
      }).toList(),
    );

    // Persist reaction to Supabase
    _persistReaction(storyId, emoji);
  }

  Future<void> _persistReaction(String storyId, String emoji) async {
    try {
      if (!SupabaseService.isInitialized || !SupabaseService.isAuthenticated) return;
      await SupabaseService.client.from('story_reactions').insert({
        'story_id': storyId,
        'user_id': SupabaseService.currentUser?.id,
        'emoji': emoji,
        'created_at': DateTime.now().toIso8601String(),
      });
    } catch (e) {
      debugPrint('[Stories] Failed to persist reaction: $e');
    }
  }

  /// Parse Supabase rows into StoryGroups
  List<StoryGroup> _parseSupabaseStories(List<dynamic> rows) {
    final groupMap = <String, List<Story>>{};
    final userNames = <String, String>{};
    final userAvatars = <String, String?>{};

    for (final row in rows) {
      final authorId = row['author_id'] as String? ?? 'unknown';
      final story = Story(
        id: row['id'].toString(),
        authorId: authorId,
        authorName: row['author_name'] as String? ?? 'User',
        authorAvatarUrl: row['author_avatar_url'] as String?,
        contentType: _parseContentType(row['content_type'] as String?),
        mediaUrl: row['media_url'] as String?,
        caption: row['caption'] as String?,
        backgroundColor: row['background_color'] as String?,
        backgroundGradient: row['background_gradient'] as String?,
        filter: row['filter'] as String?,
        mood: row['mood'] as String?,
        createdAt: DateTime.tryParse(row['created_at'] as String? ?? '') ?? DateTime.now(),
        expiresAt: DateTime.tryParse(row['expires_at'] as String? ?? '') ?? DateTime.now().add(const Duration(hours: 24)),
        viewCount: (row['view_count'] as num?)?.toInt() ?? 0,
      );

      groupMap.putIfAbsent(authorId, () => []).add(story);
      userNames[authorId] = story.authorName;
      userAvatars[authorId] = story.authorAvatarUrl;
    }

    // Add empty "Your Story" placeholder
    final currentUid = SupabaseService.currentUser?.id ?? 'current_user';
    final groups = <StoryGroup>[
      StoryGroup(
        userId: currentUid,
        userName: 'Your Story',
        userAvatarUrl: null,
        stories: groupMap.remove(currentUid) ?? [],
        hasUnseenStories: false,
      ),
    ];

    for (final entry in groupMap.entries) {
      groups.add(StoryGroup(
        userId: entry.key,
        userName: userNames[entry.key] ?? 'User',
        userAvatarUrl: userAvatars[entry.key],
        stories: entry.value,
        hasUnseenStories: entry.value.any((s) => !s.isViewed),
      ));
    }

    return groups;
  }

  StoryContentType _parseContentType(String? type) {
    switch (type) {
      case 'photo': return StoryContentType.photo;
      case 'video': return StoryContentType.video;
      case 'document': return StoryContentType.document;
      case 'link': return StoryContentType.link;
      case 'audio': return StoryContentType.audio;
      default: return StoryContentType.text;
    }
  }
}

/// State for story creation
class StoryCreationState {
  final bool isCreating;
  final double uploadProgress;
  final String? error;
  final Story? createdStory;

  const StoryCreationState({
    this.isCreating = false,
    this.uploadProgress = 0.0,
    this.error,
    this.createdStory,
  });

  StoryCreationState copyWith({
    bool? isCreating,
    double? uploadProgress,
    String? error,
    Story? createdStory,
  }) {
    return StoryCreationState(
      isCreating: isCreating ?? this.isCreating,
      uploadProgress: uploadProgress ?? this.uploadProgress,
      error: error,
      createdStory: createdStory,
    );
  }
}

/// Notifier for story creation
class StoryCreationNotifier extends Notifier<StoryCreationState> {
  @override
  StoryCreationState build() => const StoryCreationState();

  /// Create a new story
  Future<Story?> createStory(CreateStoryData data) async {
    state = state.copyWith(isCreating: true, uploadProgress: 0.0, error: null);

    try {
      final now = DateTime.now();
      String? mediaUrl = data.mediaPath;

      // Upload media to Supabase Storage if available
      if (SupabaseService.isInitialized && SupabaseService.isAuthenticated && data.mediaPath != null) {
        try {
          final uid = SupabaseService.currentUser!.id;
          final ext = data.mediaPath!.split('.').last;
          final storagePath = 'stories/$uid/${now.millisecondsSinceEpoch}.$ext';
          final file = await _readFileBytes(data.mediaPath!);
          if (file != null) {
            state = state.copyWith(uploadProgress: 0.3);
            await SupabaseService.client.storage
                .from('stories')
                .uploadBinary(storagePath, Uint8List.fromList(file));
            mediaUrl = SupabaseService.client.storage
                .from('stories')
                .getPublicUrl(storagePath);
            state = state.copyWith(uploadProgress: 0.7);
          }
        } catch (e) {
          debugPrint('[Stories] Media upload failed, using local path: $e');
        }
      }

      final story = Story(
        id: 'story_${now.millisecondsSinceEpoch}',
        authorId: SupabaseService.currentUser?.id ?? 'current_user',
        authorName: 'You',
        contentType: data.contentType,
        mediaUrl: mediaUrl,
        caption: data.caption,
        backgroundColor: data.backgroundColor,
        backgroundGradient: data.backgroundGradient
            ?.map((c) => '#${c.value.toRadixString(16).substring(2)}').join(','),
        filter: data.filter,
        mood: data.mood,
        createdAt: now,
        expiresAt: now.add(const Duration(hours: 24)),
      );

      state = state.copyWith(uploadProgress: 0.8);

      // Persist to Supabase
      try {
        if (SupabaseService.isInitialized && SupabaseService.isAuthenticated) {
          final response = await SupabaseService.client.from('stories').insert({
            'author_id': story.authorId,
            'author_name': story.authorName,
            'content_type': data.contentType.name,
            'media_url': mediaUrl,
            'caption': data.caption,
            'background_color': data.backgroundColor,
            'background_gradient': story.backgroundGradient,
            'filter': data.filter,
            'mood': data.mood,
            'created_at': now.toIso8601String(),
            'expires_at': story.expiresAt.toIso8601String(),
          }).select().single();
          // Use the server-generated ID if available
          final serverId = response['id']?.toString();
          if (serverId != null) {
            // Update story with real ID — still add the local one below
            debugPrint('[Stories] Story created in Supabase: $serverId');
          }
        }
      } catch (e) {
        debugPrint('[Stories] Supabase insert failed (story still added locally): $e');
      }

      state = state.copyWith(uploadProgress: 1.0);

      // Add to stories provider
      ref.read(storiesProvider.notifier).addStory(story);

      state = state.copyWith(
        isCreating: false,
        uploadProgress: 1.0,
        createdStory: story,
      );

      return story;
    } catch (e) {
      state = state.copyWith(
        isCreating: false,
        error: e.toString(),
      );
      return null;
    }
  }

  /// Read file bytes (for uploading media)
  Future<Uint8List?> _readFileBytes(String path) async {
    try {
      if (kIsWeb || path.startsWith('http')) return null;
      final file = File(path);
      if (await file.exists()) {
        return await file.readAsBytes();
      }
      return null;
    } catch (_) {
      return null;
    }
  }

  /// Reset state
  void reset() {
    state = const StoryCreationState();
  }
}



import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/story.dart';
import '../models/dev_options.dart';
import '../screens/settings/dev_options_screen.dart';

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

  Future<List<StoryGroup>> _fetchStories() async {
    final opts = _devOptions;

    // Feature flag: artificial network latency
    final latency = opts.networkLatencyMs;
    if (latency > 0) {
      debugPrint('[DevFlag] Adding ${latency}ms artificial latency to stories');
      await Future.delayed(Duration(milliseconds: latency));
    }

    // Simulate API call
    await Future.delayed(const Duration(milliseconds: 500));

    // Generate mock story data
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
        userAvatarUrl: 'https://i.pravatar.cc/150?u=alex',
        stories: [
          Story(
            id: 'story_1',
            authorId: 'user_1',
            authorName: 'Alex',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=alex',
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
        userAvatarUrl: 'https://i.pravatar.cc/150?u=sam',
        stories: [
          Story(
            id: 'story_2',
            authorId: 'user_2',
            authorName: 'Sam',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=sam',
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
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=sam',
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
        userAvatarUrl: 'https://i.pravatar.cc/150?u=jordan',
        stories: [
          Story(
            id: 'story_3',
            authorId: 'user_3',
            authorName: 'Jordan',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=jordan',
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
        userAvatarUrl: 'https://i.pravatar.cc/150?u=taylor',
        stories: [
          Story(
            id: 'story_4',
            authorId: 'user_4',
            authorName: 'Taylor',
            authorAvatarUrl: 'https://i.pravatar.cc/150?u=taylor',
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
      // Simulate upload progress
      for (int i = 1; i <= 10; i++) {
        await Future.delayed(const Duration(milliseconds: 100));
        state = state.copyWith(uploadProgress: i / 10);
      }

      // Create the story
      final now = DateTime.now();
      final story = Story(
        id: 'story_${now.millisecondsSinceEpoch}',
        authorId: 'current_user',
        authorName: 'You',
        contentType: data.contentType,
        mediaUrl: data.mediaPath,
        caption: data.caption,
        backgroundColor: data.backgroundColor,
        backgroundGradient: data.backgroundGradient
            ?.map((c) => '#${c.value.toRadixString(16).substring(2)}').join(','),
        filter: data.filter,
        mood: data.mood,
        createdAt: now,
        expiresAt: now.add(const Duration(hours: 24)),
      );

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

  /// Reset state
  void reset() {
    state = const StoryCreationState();
  }
}



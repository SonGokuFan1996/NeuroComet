import 'package:flutter/material.dart';

/// Represents a user's story (ephemeral content that expires after 24 hours)
class Story {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorAvatarUrl;
  final StoryContentType contentType;
  final String? mediaUrl;
  final String? caption;
  final String? backgroundColor; // Hex color for text stories
  final String? backgroundGradient; // Comma-separated hex colors
  final String? filter; // Filter ID for photo stories
  final String? mood; // Mood emoji
  final DateTime createdAt;
  final DateTime expiresAt;
  final int viewCount;
  final List<String> viewerIds;
  final bool isViewed; // Whether current user has viewed
  final List<StoryReaction> reactions;

  const Story({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorAvatarUrl,
    required this.contentType,
    this.mediaUrl,
    this.caption,
    this.backgroundColor,
    this.backgroundGradient,
    this.filter,
    this.mood,
    required this.createdAt,
    required this.expiresAt,
    this.viewCount = 0,
    this.viewerIds = const [],
    this.isViewed = false,
    this.reactions = const [],
  });

  /// Check if the story has expired
  bool get isExpired => DateTime.now().isAfter(expiresAt);

  /// Get remaining time until expiration
  Duration get remainingTime => expiresAt.difference(DateTime.now());

  /// Get human-readable time since posted
  String get timeAgo {
    final difference = DateTime.now().difference(createdAt);
    if (difference.inMinutes < 1) return 'Just now';
    if (difference.inMinutes < 60) return '${difference.inMinutes}m ago';
    if (difference.inHours < 24) return '${difference.inHours}h ago';
    return '${difference.inDays}d ago';
  }

  Story copyWith({
    String? id,
    String? authorId,
    String? authorName,
    String? authorAvatarUrl,
    StoryContentType? contentType,
    String? mediaUrl,
    String? caption,
    String? backgroundColor,
    String? backgroundGradient,
    String? filter,
    String? mood,
    DateTime? createdAt,
    DateTime? expiresAt,
    int? viewCount,
    List<String>? viewerIds,
    bool? isViewed,
    List<StoryReaction>? reactions,
  }) {
    return Story(
      id: id ?? this.id,
      authorId: authorId ?? this.authorId,
      authorName: authorName ?? this.authorName,
      authorAvatarUrl: authorAvatarUrl ?? this.authorAvatarUrl,
      contentType: contentType ?? this.contentType,
      mediaUrl: mediaUrl ?? this.mediaUrl,
      caption: caption ?? this.caption,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      backgroundGradient: backgroundGradient ?? this.backgroundGradient,
      filter: filter ?? this.filter,
      mood: mood ?? this.mood,
      createdAt: createdAt ?? this.createdAt,
      expiresAt: expiresAt ?? this.expiresAt,
      viewCount: viewCount ?? this.viewCount,
      viewerIds: viewerIds ?? this.viewerIds,
      isViewed: isViewed ?? this.isViewed,
      reactions: reactions ?? this.reactions,
    );
  }
}

/// Type of story content
enum StoryContentType {
  text,
  photo,
  video,
}

/// Reaction to a story
class StoryReaction {
  final String id;
  final String storyId;
  final String userId;
  final String userName;
  final String? userAvatarUrl;
  final String emoji;
  final DateTime createdAt;

  const StoryReaction({
    required this.id,
    required this.storyId,
    required this.userId,
    required this.userName,
    this.userAvatarUrl,
    required this.emoji,
    required this.createdAt,
  });
}

/// Groups stories by user for display
class StoryGroup {
  final String userId;
  final String userName;
  final String? userAvatarUrl;
  final List<Story> stories;
  final bool hasUnseenStories;

  const StoryGroup({
    required this.userId,
    required this.userName,
    this.userAvatarUrl,
    required this.stories,
    required this.hasUnseenStories,
  });

  /// Get the most recent story
  Story? get latestStory =>
      stories.isNotEmpty ? stories.reduce((a, b) =>
          a.createdAt.isAfter(b.createdAt) ? a : b) : null;

  /// Total view count across all stories
  int get totalViews =>
      stories.fold(0, (sum, story) => sum + story.viewCount);
}

/// Data class for creating a new story
class CreateStoryData {
  final StoryContentType contentType;
  final String? mediaPath;
  final String? caption;
  final String? backgroundColor;
  final List<Color>? backgroundGradient;
  final String? filter;
  final String? mood;

  const CreateStoryData({
    required this.contentType,
    this.mediaPath,
    this.caption,
    this.backgroundColor,
    this.backgroundGradient,
    this.filter,
    this.mood,
  });
}


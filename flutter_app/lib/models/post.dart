import 'package:json_annotation/json_annotation.dart';

part 'post.g.dart';

@JsonSerializable()
class Post {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorAvatarUrl;
  final String content;
  final List<String>? mediaUrls;
  final int likeCount;
  final int commentCount;
  final int shareCount;
  final bool isLiked;
  final bool isBookmarked;
  final String? category;
  final List<String>? tags;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final String moderationStatus;
  final int? backgroundColor;
  final String? tone;
  final String? locationTag;

  const Post({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorAvatarUrl,
    required this.content,
    this.mediaUrls,
    this.likeCount = 0,
    this.commentCount = 0,
    this.shareCount = 0,
    this.isLiked = false,
    this.isBookmarked = false,
    this.category,
    this.tags,
    this.createdAt,
    this.updatedAt,
    this.moderationStatus = 'clean',
    this.backgroundColor,
    this.tone,
    this.locationTag,
  });

  factory Post.fromJson(Map<String, dynamic> json) => _$PostFromJson(json);
  Map<String, dynamic> toJson() => _$PostToJson(this);

  Post copyWith({
    String? id,
    String? authorId,
    String? authorName,
    String? authorAvatarUrl,
    String? content,
    List<String>? mediaUrls,
    int? likeCount,
    int? commentCount,
    int? shareCount,
    bool? isLiked,
    bool? isBookmarked,
    String? category,
    List<String>? tags,
    DateTime? createdAt,
    DateTime? updatedAt,
    String? moderationStatus,
    int? backgroundColor,
    String? tone,
    String? locationTag,
  }) {
    return Post(
      id: id ?? this.id,
      authorId: authorId ?? this.authorId,
      authorName: authorName ?? this.authorName,
      authorAvatarUrl: authorAvatarUrl ?? this.authorAvatarUrl,
      content: content ?? this.content,
      mediaUrls: mediaUrls ?? this.mediaUrls,
      likeCount: likeCount ?? this.likeCount,
      commentCount: commentCount ?? this.commentCount,
      shareCount: shareCount ?? this.shareCount,
      isLiked: isLiked ?? this.isLiked,
      isBookmarked: isBookmarked ?? this.isBookmarked,
      category: category ?? this.category,
      tags: tags ?? this.tags,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      moderationStatus: moderationStatus ?? this.moderationStatus,
      backgroundColor: backgroundColor ?? this.backgroundColor,
      tone: tone ?? this.tone,
      locationTag: locationTag ?? this.locationTag,
    );
  }
}

@JsonSerializable()
class Comment {
  final String id;
  final String postId;
  final String authorId;
  final String authorName;
  final String? authorAvatarUrl;
  final String content;
  final int likeCount;
  final bool isLiked;
  final String? parentCommentId;
  final List<Comment> replies;
  final DateTime? createdAt;

  const Comment({
    required this.id,
    required this.postId,
    required this.authorId,
    required this.authorName,
    this.authorAvatarUrl,
    required this.content,
    this.likeCount = 0,
    this.isLiked = false,
    this.parentCommentId,
    this.replies = const [],
    this.createdAt,
  });

  factory Comment.fromJson(Map<String, dynamic> json) => _$CommentFromJson(json);
  Map<String, dynamic> toJson() => _$CommentToJson(this);

  Comment copyWith({
    String? id,
    String? postId,
    String? authorId,
    String? authorName,
    String? authorAvatarUrl,
    String? content,
    int? likeCount,
    bool? isLiked,
    String? parentCommentId,
    List<Comment>? replies,
    DateTime? createdAt,
  }) {
    return Comment(
      id: id ?? this.id,
      postId: postId ?? this.postId,
      authorId: authorId ?? this.authorId,
      authorName: authorName ?? this.authorName,
      authorAvatarUrl: authorAvatarUrl ?? this.authorAvatarUrl,
      content: content ?? this.content,
      likeCount: likeCount ?? this.likeCount,
      isLiked: isLiked ?? this.isLiked,
      parentCommentId: parentCommentId ?? this.parentCommentId,
      replies: replies ?? this.replies,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

@JsonSerializable()
class Story {
  final String id;
  final String authorId;
  final String authorName;
  final String? authorAvatarUrl;
  final List<StoryItem> items;
  final bool isViewed;
  final DateTime? createdAt;
  final DateTime? expiresAt;

  const Story({
    required this.id,
    required this.authorId,
    required this.authorName,
    this.authorAvatarUrl,
    required this.items,
    this.isViewed = false,
    this.createdAt,
    this.expiresAt,
  });

  factory Story.fromJson(Map<String, dynamic> json) => _$StoryFromJson(json);
  Map<String, dynamic> toJson() => _$StoryToJson(this);
}

@JsonSerializable()
class StoryItem {
  final String id;
  final String mediaUrl;
  final StoryMediaType mediaType;
  final String? caption;
  final int durationSeconds;
  final DateTime? createdAt;

  const StoryItem({
    required this.id,
    required this.mediaUrl,
    this.mediaType = StoryMediaType.image,
    this.caption,
    this.durationSeconds = 5,
    this.createdAt,
  });

  factory StoryItem.fromJson(Map<String, dynamic> json) => _$StoryItemFromJson(json);
  Map<String, dynamic> toJson() => _$StoryItemToJson(this);
}

enum StoryMediaType {
  @JsonValue('image')
  image,
  @JsonValue('video')
  video,
}


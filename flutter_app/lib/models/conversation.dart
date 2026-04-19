import 'package:json_annotation/json_annotation.dart';

part 'conversation.g.dart';

@JsonSerializable()
class Conversation {
  final String id;
  final String displayName;
  final String? avatarUrl;
  final String? lastMessage;
  final DateTime? lastMessageAt;
  final int unreadCount;
  final bool isOnline;
  final bool isMuted;
  final bool isBlocked;
  final bool isGroup;
  final bool isPinned;
  final bool isPrimary;
  final bool isVerified;
  final bool isTyping;
  final String? participantId;
  final List<String>? participantIds;
  final DateTime? createdAt;
  final ModerationStatus moderationStatus;
  final String? groupName;
  final List<String>? memberNames;

  const Conversation({
    required this.id,
    required this.displayName,
    this.avatarUrl,
    this.lastMessage,
    this.lastMessageAt,
    this.unreadCount = 0,
    this.isOnline = false,
    this.isMuted = false,
    this.isBlocked = false,
    this.isGroup = false,
    this.isPinned = false,
    this.isPrimary = false,
    this.isVerified = false,
    this.isTyping = false,
    this.participantId,
    this.participantIds,
    this.createdAt,
    this.moderationStatus = ModerationStatus.none,
    this.groupName,
    this.memberNames,
  });

  factory Conversation.fromJson(Map<String, dynamic> json) =>
      _$ConversationFromJson(json);
  Map<String, dynamic> toJson() => _$ConversationToJson(this);

  Conversation copyWith({
    String? id,
    String? displayName,
    String? avatarUrl,
    String? lastMessage,
    DateTime? lastMessageAt,
    int? unreadCount,
    bool? isOnline,
    bool? isMuted,
    bool? isBlocked,
    bool? isGroup,
    bool? isPinned,
    bool? isPrimary,
    bool? isVerified,
    bool? isTyping,
    String? participantId,
    List<String>? participantIds,
    DateTime? createdAt,
    ModerationStatus? moderationStatus,
    String? groupName,
    List<String>? memberNames,
  }) {
    return Conversation(
      id: id ?? this.id,
      displayName: displayName ?? this.displayName,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      lastMessage: lastMessage ?? this.lastMessage,
      lastMessageAt: lastMessageAt ?? this.lastMessageAt,
      unreadCount: unreadCount ?? this.unreadCount,
      isOnline: isOnline ?? this.isOnline,
      isMuted: isMuted ?? this.isMuted,
      isBlocked: isBlocked ?? this.isBlocked,
      isGroup: isGroup ?? this.isGroup,
      isPinned: isPinned ?? this.isPinned,
      isPrimary: isPrimary ?? this.isPrimary,
      isVerified: isVerified ?? this.isVerified,
      isTyping: isTyping ?? this.isTyping,
      participantId: participantId ?? this.participantId,
      participantIds: participantIds ?? this.participantIds,
      createdAt: createdAt ?? this.createdAt,
      moderationStatus: moderationStatus ?? this.moderationStatus,
      groupName: groupName ?? this.groupName,
      memberNames: memberNames ?? this.memberNames,
    );
  }
}

@JsonSerializable()
class Message {
  final String id;
  final String conversationId;
  final String senderId;
  final String content;
  final MessageType type;
  final String? mediaUrl;
  final MessageStatus status;
  final DateTime? createdAt;
  final DateTime? readAt;
  final String? replyToMessageId;
  final List<MessageReaction> reactions;

  const Message({
    required this.id,
    required this.conversationId,
    required this.senderId,
    required this.content,
    this.type = MessageType.text,
    this.mediaUrl,
    this.status = MessageStatus.sent,
    this.createdAt,
    this.readAt,
    this.replyToMessageId,
    this.reactions = const [],
  });

  factory Message.fromJson(Map<String, dynamic> json) => _$MessageFromJson(json);
  Map<String, dynamic> toJson() => _$MessageToJson(this);
}

enum MessageType {
  @JsonValue('text')
  text,
  @JsonValue('image')
  image,
  @JsonValue('video')
  video,
  @JsonValue('audio')
  audio,
  @JsonValue('file')
  file,
  @JsonValue('sticker')
  sticker,
}

enum MessageStatus {
  @JsonValue('sending')
  sending,
  @JsonValue('sent')
  sent,
  @JsonValue('delivered')
  delivered,
  @JsonValue('read')
  read,
  @JsonValue('failed')
  failed,
}

@JsonSerializable()
class MessageReaction {
  final String emoji;
  final String userId;

  const MessageReaction({
    required this.emoji,
    required this.userId,
  });

  factory MessageReaction.fromJson(Map<String, dynamic> json) =>
      _$MessageReactionFromJson(json);
  Map<String, dynamic> toJson() => _$MessageReactionToJson(this);
}

/// Moderation status for user accounts in conversations
enum ModerationStatus {
  @JsonValue('none')
  none,
  @JsonValue('verified')
  verified,
  @JsonValue('moderator')
  moderator,
  @JsonValue('admin')
  admin,
  @JsonValue('warned')
  warned,
  @JsonValue('restricted')
  restricted,
}


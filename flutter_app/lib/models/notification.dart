import 'package:json_annotation/json_annotation.dart';

part 'notification.g.dart';

enum NotificationType {
  @JsonValue('like')
  like,
  @JsonValue('comment')
  comment,
  @JsonValue('follow')
  follow,
  @JsonValue('mention')
  mention,
  @JsonValue('message')
  message,
  @JsonValue('achievement')
  achievement,
  @JsonValue('system')
  system,
  @JsonValue('repost')
  repost,
  @JsonValue('badge')
  badge,
  @JsonValue('welcome')
  welcome,
  @JsonValue('safety_alert')
  safetyAlert,
}

@JsonSerializable()
class AppNotification {
  final String id;
  final NotificationType type;
  final String? title;
  final String message;
  final String? actorId;
  final String? actorName;
  final String? actorAvatarUrl;
  final String? targetId;
  final String? targetType;
  final String? actionUrl;
  final int? relatedPostId;
  final bool isRead;
  final DateTime? createdAt;

  const AppNotification({
    required this.id,
    required this.type,
    this.title,
    required this.message,
    this.actorId,
    this.actorName,
    this.actorAvatarUrl,
    this.targetId,
    this.targetType,
    this.actionUrl,
    this.relatedPostId,
    this.isRead = false,
    this.createdAt,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) =>
      _$AppNotificationFromJson(json);
  Map<String, dynamic> toJson() => _$AppNotificationToJson(this);

  AppNotification copyWith({
    String? id,
    NotificationType? type,
    String? title,
    String? message,
    String? actorId,
    String? actorName,
    String? actorAvatarUrl,
    String? targetId,
    String? targetType,
    String? actionUrl,
    int? relatedPostId,
    bool? isRead,
    DateTime? createdAt,
  }) {
    return AppNotification(
      id: id ?? this.id,
      type: type ?? this.type,
      title: title ?? this.title,
      message: message ?? this.message,
      actorId: actorId ?? this.actorId,
      actorName: actorName ?? this.actorName,
      actorAvatarUrl: actorAvatarUrl ?? this.actorAvatarUrl,
      targetId: targetId ?? this.targetId,
      targetType: targetType ?? this.targetType,
      actionUrl: actionUrl ?? this.actionUrl,
      relatedPostId: relatedPostId ?? this.relatedPostId,
      isRead: isRead ?? this.isRead,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}


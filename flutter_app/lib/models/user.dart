import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User {
  final String id;
  final String displayName;
  final String? username;
  final String? email;
  final String? avatarUrl;
  final String? bannerUrl;
  final String? bio;
  final int postCount;
  final int followerCount;
  final int followingCount;
  final bool isFollowing;
  final bool isFollowedBy;
  final bool isBlocked;
  final bool isPremium;
  final bool isVerified;
  final List<String>? badges;
  final DateTime? createdAt;
  final DateTime? lastActiveAt;
  final UserPreferences? preferences;

  const User({
    required this.id,
    required this.displayName,
    this.username,
    this.email,
    this.avatarUrl,
    this.bannerUrl,
    this.bio,
    this.postCount = 0,
    this.followerCount = 0,
    this.followingCount = 0,
    this.isFollowing = false,
    this.isFollowedBy = false,
    this.isBlocked = false,
    this.isPremium = false,
    this.isVerified = false,
    this.badges,
    this.createdAt,
    this.lastActiveAt,
    this.preferences,
  });

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);
  Map<String, dynamic> toJson() => _$UserToJson(this);

  User copyWith({
    String? id,
    String? displayName,
    String? username,
    String? email,
    String? avatarUrl,
    String? bannerUrl,
    String? bio,
    int? postCount,
    int? followerCount,
    int? followingCount,
    bool? isFollowing,
    bool? isFollowedBy,
    bool? isBlocked,
    bool? isPremium,
    bool? isVerified,
    List<String>? badges,
    DateTime? createdAt,
    DateTime? lastActiveAt,
    UserPreferences? preferences,
  }) {
    return User(
      id: id ?? this.id,
      displayName: displayName ?? this.displayName,
      username: username ?? this.username,
      email: email ?? this.email,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      bannerUrl: bannerUrl ?? this.bannerUrl,
      bio: bio ?? this.bio,
      postCount: postCount ?? this.postCount,
      followerCount: followerCount ?? this.followerCount,
      followingCount: followingCount ?? this.followingCount,
      isFollowing: isFollowing ?? this.isFollowing,
      isFollowedBy: isFollowedBy ?? this.isFollowedBy,
      isBlocked: isBlocked ?? this.isBlocked,
      isPremium: isPremium ?? this.isPremium,
      isVerified: isVerified ?? this.isVerified,
      badges: badges ?? this.badges,
      createdAt: createdAt ?? this.createdAt,
      lastActiveAt: lastActiveAt ?? this.lastActiveAt,
      preferences: preferences ?? this.preferences,
    );
  }
}

@JsonSerializable()
class UserPreferences {
  final bool reducedMotion;
  final bool highContrast;
  final double textScale;
  final String theme;
  final String language;
  final bool pushNotifications;
  final bool emailNotifications;
  final bool showOnlineStatus;
  final String messagePrivacy;

  const UserPreferences({
    this.reducedMotion = false,
    this.highContrast = false,
    this.textScale = 1.0,
    this.theme = 'system',
    this.language = 'en',
    this.pushNotifications = true,
    this.emailNotifications = true,
    this.showOnlineStatus = true,
    this.messagePrivacy = 'everyone',
  });

  factory UserPreferences.fromJson(Map<String, dynamic> json) =>
      _$UserPreferencesFromJson(json);
  Map<String, dynamic> toJson() => _$UserPreferencesToJson(this);
}

@JsonSerializable()
class UserBadge {
  final String id;
  final String name;
  final String icon;
  final String? description;
  final DateTime? earnedAt;

  const UserBadge({
    required this.id,
    required this.name,
    required this.icon,
    this.description,
    this.earnedAt,
  });

  factory UserBadge.fromJson(Map<String, dynamic> json) =>
      _$UserBadgeFromJson(json);
  Map<String, dynamic> toJson() => _$UserBadgeToJson(this);
}


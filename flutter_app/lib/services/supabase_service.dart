import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../core/constants/app_constants.dart';
import '../models/user.dart' as app_models;
import '../models/post.dart';
import '../models/conversation.dart';
import '../models/notification.dart';

class AccountStatus {
  final bool isActive;
  final DateTime? deletionScheduledAt;
  final DateTime? detoxStartedAt;
  final DateTime? detoxUntil;

  const AccountStatus({
    required this.isActive,
    this.deletionScheduledAt,
    this.detoxStartedAt,
    this.detoxUntil,
  });

  bool get hasDeletionScheduled => deletionScheduledAt != null;
  bool get isDetoxActive => detoxUntil != null && detoxUntil!.isAfter(DateTime.now());
  Duration? get detoxRemaining =>
      isDetoxActive ? detoxUntil!.difference(DateTime.now()) : null;
}

class SupabaseService {
  static const _detoxBackupAppliedKey = 'detox_backup_applied';
  static const _detoxBackupPushKey = 'detox_backup_push_enabled';
  static const _detoxBackupQuietHoursKey = 'detox_backup_quiet_hours';
  static const _detoxBackupBreakRemindersKey = 'detox_backup_break_reminders';
  static const _detoxBackupCalmModeKey = 'detox_backup_calm_auto';

  /// Whether Supabase.initialize() completed successfully.
  static bool get isInitialized {
    try {
      Supabase.instance;
      return true;
    } catch (_) {
      return false;
    }
  }

  static SupabaseClient get client => Supabase.instance.client;

  static User? get currentUser {
    if (!isInitialized) return null;
    return client.auth.currentUser;
  }

  static Session? get currentSession {
    if (!isInitialized) return null;
    return client.auth.currentSession;
  }

  /// Developer mode flag to skip authentication
  static bool devModeSkipAuth = false;

  /// Returns true if user is authenticated OR if dev mode skip is enabled
  static bool get isAuthenticated => currentUser != null || devModeSkipAuth;

  static Map<String, dynamic> _row(dynamic value) =>
      Map<String, dynamic>.from(value as Map);

  static String? _string(dynamic value) {
    final text = value?.toString();
    if (text == null || text.isEmpty || text == 'null') return null;
    return text;
  }

  static DateTime? _dateTime(dynamic value) {
    final text = _string(value);
    if (text == null) return null;
    return DateTime.tryParse(text);
  }

  static bool _bool(dynamic value) {
    if (value is bool) return value;
    if (value is num) return value != 0;
    final text = _string(value)?.toLowerCase();
    return text == 'true' || text == '1';
  }

  static MessageType _messageType(dynamic value) {
    switch (_string(value)) {
      case 'image':
        return MessageType.image;
      case 'video':
        return MessageType.video;
      case 'audio':
        return MessageType.audio;
      case 'file':
        return MessageType.file;
      case 'sticker':
        return MessageType.sticker;
      default:
        return MessageType.text;
    }
  }

  static String _profileName(Map<String, dynamic>? profile, String fallback) {
    final displayName = _string(profile?['display_name']);
    if (displayName != null) return displayName;
    final username = _string(profile?['username']);
    if (username != null) return username;
    return fallback;
  }

  static Message _mapMessageRow(Map<String, dynamic> row) {
    final readAt = _dateTime(row['read_at']);
    final isRead = _bool(row['is_read']) || readAt != null;
    return Message(
      id: _string(row['id']) ?? '',
      conversationId: _string(row['conversation_id']) ?? '',
      senderId: _string(row['sender_id']) ?? '',
      content: _string(row['content']) ?? '',
      type: _messageType(row['type']),
      mediaUrl: _string(row['media_url']),
      status: isRead ? MessageStatus.read : MessageStatus.sent,
      createdAt: _dateTime(row['created_at']),
      readAt: readAt,
    );
  }

  // ============ Authentication ============

  static Future<AuthResponse> signInWithEmail({
    required String email,
    required String password,
  }) async {
    return await client.auth.signInWithPassword(
      email: email,
      password: password,
    );
  }

  static Future<AuthResponse> signUpWithEmail({
    required String email,
    required String password,
    String? displayName,
  }) async {
    final response = await client.auth.signUp(
      email: email,
      password: password,
      emailRedirectTo: AppConstants.supabaseCallbackUrl,
      data: {
        'display_name': displayName,
      },
    );
    return response;
  }

  static Future<void> signOut() async {
    await client.auth.signOut();
  }

  static Future<void> resetPassword(String email) async {
    await client.auth.resetPasswordForEmail(
      email,
      redirectTo: AppConstants.supabaseCallbackUrl,
    );
  }

  static Future<bool> signInWithGoogle() async {
    return await client.auth.signInWithOAuth(
      OAuthProvider.google,
      redirectTo: AppConstants.supabaseCallbackUrl,
    );
  }

  static Future<bool> signInWithApple() async {
    return await client.auth.signInWithOAuth(
      OAuthProvider.apple,
      redirectTo: AppConstants.supabaseCallbackUrl,
    );
  }

  // ============ User Profile ============

  /// Map a raw Supabase `users` row (snake_case) → app User model.
  /// The DB schema uses snake_case columns while the Dart model uses camelCase.
  static app_models.User _userFromSupabaseRow(Map<String, dynamic> row) {
    return app_models.User(
      id: (row['id'] ?? '').toString(),
      displayName: _string(row['display_name']) ??
          _string(row['displayName']) ??
          _string(row['username']) ??
          'User',
      username: _string(row['username']),
      email: _string(row['email']),
      avatarUrl: _string(row['avatar_url']) ?? _string(row['avatarUrl']),
      bannerUrl: _string(row['banner_url']) ?? _string(row['bannerUrl']),
      bio: _string(row['bio']),
      postCount: (row['post_count'] as num?)?.toInt() ??
          (row['postCount'] as num?)?.toInt() ??
          0,
      followerCount: (row['follower_count'] as num?)?.toInt() ??
          (row['followerCount'] as num?)?.toInt() ??
          0,
      followingCount: (row['following_count'] as num?)?.toInt() ??
          (row['followingCount'] as num?)?.toInt() ??
          0,
      isPremium: _bool(row['is_premium'] ?? row['isPremium'] ?? false),
      isVerified: _bool(row['is_verified'] ?? row['isVerified'] ?? false),
      createdAt: _dateTime(row['created_at'] ?? row['createdAt']),
      lastActiveAt: _dateTime(row['last_active_at'] ?? row['lastActiveAt']),
    );
  }

  static Future<app_models.User?> getUserProfile(String userId) async {
    try {
      final response = await client
          .from('users')
          .select()
          .eq('id', userId)
          .maybeSingle();

      if (response == null) {
        debugPrint('[SupabaseService] No user row found for id=$userId');
        return null;
      }

      final user = _userFromSupabaseRow(response);
      debugPrint('[SupabaseService] getUserProfile OK: ${user.displayName} (${user.id})');
      return user;
    } on PostgrestException catch (e) {
      debugPrint('[SupabaseService] getUserProfile DB error for $userId: ${e.message} (code ${e.code})');
      return null;
    }
  }

  static Future<void> updateUserProfile({
    required String userId,
    String? displayName,
    String? bio,
    String? avatarUrl,
    String? bannerUrl,
  }) async {
    final updates = <String, dynamic>{
      'updated_at': DateTime.now().toIso8601String(),
    };

    if (displayName != null) updates['display_name'] = displayName;
    if (bio != null) updates['bio'] = bio;
    if (avatarUrl != null) updates['avatar_url'] = avatarUrl;
    if (bannerUrl != null) updates['banner_url'] = bannerUrl;

    await client.from('users').update(updates).eq('id', userId);
  }

  /// Ensures a row exists in the `users` table for the currently
  /// authenticated user. Call this after sign-in / sign-up so that
  /// `getUserProfile` has something to return.
  static Future<void> ensureUserProfile() async {
    final user = currentUser;
    if (user == null) return;

    try {
      final existing = await client
          .from('users')
          .select('id')
          .eq('id', user.id)
          .maybeSingle();

      if (existing != null) return; // row already exists

      final meta = user.userMetadata ?? {};
      final now = DateTime.now().toUtc().toIso8601String();
      await client.from('users').insert({
        'id': user.id,
        'username': (meta['username'] as String?) ??
            user.email?.split('@').first ??
            'user_${user.id.substring(0, 8)}',
        'display_name': (meta['display_name'] as String?) ??
            (meta['full_name'] as String?) ??
            user.email?.split('@').first ??
            'New User',
        'email': user.email,
        'avatar_url': meta['avatar_url'] as String?,
        'bio': 'New to NeuroComet! 🧠✨',
        'created_at': now,
        'updated_at': now,
      });
      debugPrint('[SupabaseService] ✅ Created users row for ${user.id}');
    } on PostgrestException catch (e) {
      // 23505 = unique_violation — row was created concurrently, that's fine
      if (e.code == '23505') return;
      debugPrint('[SupabaseService] ensureUserProfile error: ${e.message}');
    } catch (e) {
      debugPrint('[SupabaseService] ensureUserProfile error: $e');
    }
  }

  // ============ Posts ============

  static Future<List<Post>> getFeedPosts({
    int limit = 20,
    int offset = 0,
  }) async {
    // Join the users table so posts come back with author name/avatar
    final response = await client
        .from('posts')
        .select('*, users(display_name, username, avatar_url)')
        .order('created_at', ascending: false)
        .range(offset, offset + limit - 1);

    debugPrint('[SupabaseService] getFeedPosts returned ${(response as List).length} rows');
    return (response).map((json) => _postFromSupabaseRow(Map<String, dynamic>.from(json))).toList();
  }

  static Future<List<Post>> getUserPosts(String userId, {
    int limit = 20,
    int offset = 0,
  }) async {
    final response = await client
        .from('posts')
        .select('*, users(display_name, username, avatar_url)')
        .eq('user_id', userId)
        .order('created_at', ascending: false)
        .range(offset, offset + limit - 1);

    debugPrint('[SupabaseService] getUserPosts($userId) returned ${(response as List).length} rows');
    return (response).map((json) => _postFromSupabaseRow(Map<String, dynamic>.from(json))).toList();
  }

  /// Convert a raw Supabase posts row to a Post model.
  /// The DB schema (id, user_id, content, image_url, video_url, likes, comments, shares, category, created_at)
  /// differs from the app model, so we map manually.
  static Post _postFromSupabaseRow(Map<String, dynamic> json) {
    // If a user profile was joined, extract name/avatar from it
    final profile = json['users'] is Map ? json['users'] as Map<String, dynamic> : null;
    final authorName = _string(profile?['display_name']) ??
        _string(profile?['username']) ??
        'User ${(json['user_id'] as String?)?.substring(0, 8) ?? 'anon'}';
    final authorAvatar = _string(profile?['avatar_url']);

    return Post(
      id: json['id'].toString(),
      authorId: (json['user_id'] as String?) ?? 'unknown',
      authorName: authorName,
      authorAvatarUrl: authorAvatar,
      content: json['content'] as String? ?? '',
      mediaUrls: [
        if (json['image_url'] != null) json['image_url'] as String,
        if (json['video_url'] != null) json['video_url'] as String,
      ],
      likeCount: (json['likes'] as num?)?.toInt() ?? 0,
      commentCount: (json['comments'] as num?)?.toInt() ?? 0,
      shareCount: (json['shares'] as num?)?.toInt() ?? 0,
      isLiked: _bool(json['is_liked_by_me'] ?? false),
      category: json['category'] as String?,
      tags: (json['tags'] as List?)?.map((e) => e.toString()).toList(),
      locationTag: _string(json['location_tag']) ?? _string(json['locationTag']),
      createdAt: json['created_at'] != null
          ? DateTime.tryParse(json['created_at'] as String)
          : null,
    );
  }

  static Future<Post> createPost({
    required String content,
    List<String>? mediaUrls,
    String? category,
    List<String>? tags,
    String? locationTag,
  }) async {
    final payload = {
      'user_id': currentUser?.id,
      'content': content,
      'image_url': mediaUrls?.isNotEmpty == true ? mediaUrls!.first : null,
      'category': category,
      'tags': tags,
      'location_tag': locationTag,
      'is_liked_by_me': false,
    }..removeWhere((key, value) => value == null);

    dynamic response;
    try {
      response = await client.from('posts').insert(payload).select().single();
    } on PostgrestException catch (e) {
      debugPrint('createPost with location/tag metadata failed, retrying minimal payload: ${e.message}');
      final fallbackPayload = {
        'user_id': currentUser?.id,
        'content': content,
        'image_url': mediaUrls?.isNotEmpty == true ? mediaUrls!.first : null,
        'is_liked_by_me': false,
      }..removeWhere((key, value) => value == null);
      response = await client.from('posts').insert(fallbackPayload).select().single();
    }

    final post = _postFromSupabaseRow(response);
    return post.copyWith(
      category: post.category ?? category,
      tags: post.tags ?? tags,
      locationTag: post.locationTag ?? locationTag,
    );
  }

  static Future<void> deletePost(String postId) async {
    await client.from('posts').delete().eq('id', postId);
  }

  static Future<void> toggleLike(String postId) async {
    final userId = currentUser?.id;
    if (userId == null) throw Exception('Not logged in');

    // Check if already liked
    final existing = await client
        .from('post_likes')
        .select()
        .eq('post_id', postId)
        .eq('user_id', userId)
        .maybeSingle();

    if (existing != null) {
      await client.from('post_likes').delete()
          .eq('post_id', postId)
          .eq('user_id', userId);
    } else {
      await client.from('post_likes').insert({
        'post_id': postId,
        'user_id': userId,
      });
    }
  }

  // ============ Comments ============

  static Future<List<Comment>> getComments(String postId) async {
    try {
      final response = await client
          .from('post_comments')
          .select('*')
          .eq('post_id', postId)
          .order('created_at', ascending: true);

      return (response as List).map((json) => Comment.fromJson(json)).toList();
    } on PostgrestException catch (e) {
      debugPrint('getComments failed (table may not exist): ${e.message}');
      return [];
    }
  }

  static Future<Comment> addComment({
    required String postId,
    required String content,
    String? parentCommentId,
  }) async {
    final response = await client.from('post_comments').insert({
      'post_id': postId,
      'user_id': currentUser?.id,
      'content': content,
      'parent_comment_id': parentCommentId,
    }).select().single();

    return Comment.fromJson(response);
  }

  // ============ Conversations & Messages ============

  static Future<List<Conversation>> getConversations() async {
    final uid = currentUser?.id;
    if (uid == null) return [];

    try {
      // Fetch pre-computed summary from the SQL view
      final response = await client
          .from('vw_conversations_summary')
          .select('''
            *,
            conversation_participants!inner(user_id)
          ''')
          .eq('conversation_participants.user_id', uid)
          .order('updated_at', ascending: false);

      final rows = (response as List).map(_row).toList();

      // Get all participant IDs for these conversations to fetch their profiles
      // Simplified mapping using the new view data
      return rows.map((row) {
        final conversationId = _string(row['conversation_id']) ?? '';
        final isGroup = _bool(row['is_group']);
        
        return Conversation(
          id: conversationId,
          displayName: isGroup
              ? (_string(row['group_name']) ?? 'Group chat')
              : 'User', // You might need a small helper to resolve the other participant's profile
          avatarUrl: null, // Resolve via profile join
          lastMessage: _string(row['last_message']),
          lastMessageAt: _dateTime(row['last_message_at']),
          unreadCount: (row['unread_count'] as num?)?.toInt() ?? 0,
          isGroup: isGroup,
          participantId: null, // Resolve via participant join
          participantIds: [], // Resolve via participant join
          createdAt: _dateTime(row['updated_at']),
        );
      }).toList();
    } catch (e) {
      debugPrint('Error getting conversations from view: $e');
      return [];
    }
  }

  static Future<List<Message>> getMessages(String conversationId) async {
    final response = await client
        .from('dm_messages')
        .select('id, conversation_id, sender_id, content, type, media_url, is_read, created_at, read_at')
        .eq('conversation_id', conversationId)
        .order('created_at', ascending: true);

    return (response as List).map(_row).map(_mapMessageRow).toList();
  }

  static Future<Message> sendMessage({
    required String conversationId,
    required String content,
    MessageType type = MessageType.text,
    String? mediaUrl,
  }) async {
    final uid = currentUser?.id;
    if (uid == null) throw Exception('Not logged in');
    final response = await client.from('dm_messages').insert({
      'conversation_id': conversationId,
      'sender_id': uid,
      'content': content,
      'type': type.name,
      'media_url': mediaUrl,
    }).select().single();

    return _mapMessageRow(_row(response));
  }

  // ============ Notifications ============

  static Future<List<AppNotification>> getNotifications() async {
    final uid = currentUser?.id;
    if (uid == null) return [];
    final response = await client
        .from('notifications')
        .select('''
          *,
          actor:users!actor_id(id, display_name, avatar_url)
        ''')
        .eq('user_id', uid)
        .order('created_at', ascending: false);

    return (response as List).map((json) {
      final row = Map<String, dynamic>.from(json);
      final actor = row['actor'] is Map
          ? row['actor'] as Map<String, dynamic>
          : null;
      // Map snake_case DB columns → camelCase expected by AppNotification.fromJson
      return AppNotification.fromJson({
        'id': row['id']?.toString() ?? '',
        'type': row['type'] ?? 'system',
        'title': row['title'],
        'message': row['message'] ?? row['content'] ?? '',
        'actorId': _string(row['actor_id']),
        'actorName': _string(actor?['display_name']),
        'actorAvatarUrl': _string(actor?['avatar_url']),
        'targetId': _string(row['target_id']),
        'targetType': _string(row['target_type']),
        'actionUrl': _string(row['action_url']),
        'relatedPostId': row['related_post_id'] ?? row['post_id'],
        'isRead': _bool(row['is_read'] ?? false),
        'createdAt': _string(row['created_at']),
      });
    }).toList();
  }

  static Future<void> markNotificationAsRead(String notificationId) async {
    await client.from('notifications')
        .update({'is_read': true})
        .eq('id', notificationId);
  }

  static Future<void> markAllNotificationsAsRead() async {
    final uid = currentUser?.id;
    if (uid == null) return;
    await client.from('notifications')
        .update({'is_read': true})
        .eq('user_id', uid)
        .eq('is_read', false);
  }

  static Future<AccountStatus?> getCurrentAccountStatus() async {
    final userId = currentUser?.id;
    if (userId == null) return null;

    try {
      final response = await client
          .from('users')
          .select('is_active, deletion_scheduled_at, detox_started_at, detox_until')
          .eq('id', userId)
          .maybeSingle();
      if (response == null) return null;
      final row = _row(response);
      final status = AccountStatus(
        isActive: row['is_active'] == null ? true : _bool(row['is_active']),
        deletionScheduledAt: _dateTime(row['deletion_scheduled_at']),
        detoxStartedAt: _dateTime(row['detox_started_at']),
        detoxUntil: _dateTime(row['detox_until']),
      );

      if (status.detoxUntil != null && !status.isDetoxActive) {
        await client.from('users').update({
          'detox_started_at': null,
          'detox_until': null,
          'updated_at': DateTime.now().toIso8601String(),
        }).eq('id', userId);
        await _restoreLocalDetoxDefaults();
        return const AccountStatus(isActive: true);
      }

      return status;
    } catch (e) {
      debugPrint('Error loading account status: $e');
      return null;
    }
  }

  static Future<Map<String, dynamic>> startDetoxMode({
    required Duration duration,
  }) async {
    try {
      final userId = currentUser?.id;
      if (userId == null) {
        return {'success': false, 'message': 'No user logged in'};
      }

      final now = DateTime.now();
      final until = now.add(duration);
      await client.from('users').update({
        'detox_started_at': now.toIso8601String(),
        'detox_until': until.toIso8601String(),
        'updated_at': now.toIso8601String(),
      }).eq('id', userId);

      await _applyLocalDetoxDefaults();
      await signOut();
      return {
        'success': true,
        'message': 'Detox mode is on until ${until.toLocal()}. We signed you out so your break can stick.',
        'until': until.toIso8601String(),
      };
    } catch (e) {
      debugPrint('Error starting detox mode: $e');
      return {'success': false, 'message': 'Failed to start detox mode: $e'};
    }
  }

  static Future<Map<String, dynamic>> endDetoxMode() async {
    try {
      final userId = currentUser?.id;
      if (userId == null) {
        return {'success': false, 'message': 'No user logged in'};
      }

      await client.from('users').update({
        'detox_started_at': null,
        'detox_until': null,
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', userId);
      await _restoreLocalDetoxDefaults();
      return {'success': true, 'message': 'Detox mode ended. Welcome back.'};
    } catch (e) {
      debugPrint('Error ending detox mode: $e');
      return {'success': false, 'message': 'Failed to end detox mode: $e'};
    }
  }

  static Future<void> _applyLocalDetoxDefaults() async {
    final prefs = await SharedPreferences.getInstance();
    final alreadyBackedUp = prefs.getBool(_detoxBackupAppliedKey) ?? false;
    if (!alreadyBackedUp) {
      await prefs.setBool(_detoxBackupPushKey, prefs.getBool('push_enabled') ?? true);
      await prefs.setBool(_detoxBackupQuietHoursKey, prefs.getBool('quiet_hours') ?? false);
      await prefs.setBool(_detoxBackupBreakRemindersKey, prefs.getBool('break_reminders') ?? false);
      await prefs.setBool(_detoxBackupCalmModeKey, prefs.getBool('calm_auto') ?? false);
      await prefs.setBool(_detoxBackupAppliedKey, true);
    }

    await prefs.setBool('push_enabled', false);
    await prefs.setBool('quiet_hours', true);
    await prefs.setBool('break_reminders', true);
    await prefs.setBool('calm_auto', true);
  }

  static Future<void> _restoreLocalDetoxDefaults() async {
    final prefs = await SharedPreferences.getInstance();
    if (!(prefs.getBool(_detoxBackupAppliedKey) ?? false)) return;

    await prefs.setBool('push_enabled', prefs.getBool(_detoxBackupPushKey) ?? true);
    await prefs.setBool('quiet_hours', prefs.getBool(_detoxBackupQuietHoursKey) ?? false);
    await prefs.setBool('break_reminders', prefs.getBool(_detoxBackupBreakRemindersKey) ?? false);
    await prefs.setBool('calm_auto', prefs.getBool(_detoxBackupCalmModeKey) ?? false);

    await prefs.remove(_detoxBackupPushKey);
    await prefs.remove(_detoxBackupQuietHoursKey);
    await prefs.remove(_detoxBackupBreakRemindersKey);
    await prefs.remove(_detoxBackupCalmModeKey);
    await prefs.remove(_detoxBackupAppliedKey);
  }

  // ============ Account Management ============

  /// Delete user account (GDPR compliance)
  static Future<Map<String, dynamic>> deleteAccount({bool immediate = false}) async {
    try {
      final userId = currentUser?.id;
      if (userId == null) {
        return {'success': false, 'message': 'No user logged in'};
      }

      // Use Edge Function for secure account deletion
      final response = await client.functions.invoke(
        'process-account-deletions',
        body: {'userId': userId, 'immediate': immediate},
      );

      if (response.status != 200) {
        throw Exception('Server returned ${response.status}: ${response.data}');
      }

      if (!immediate) {
        await client.from('users').update({
          'deletion_scheduled_at': DateTime.now().add(const Duration(days: 14)).toIso8601String(),
          'is_active': false,
          'updated_at': DateTime.now().toIso8601String(),
        }).eq('id', userId);
        await _restoreLocalDetoxDefaults();
      }

      await signOut();
      return {
        'success': true,
        'message': 'Account deletion request processed successfully.',
      };
    } catch (e) {
      debugPrint('Error deleting account: $e');
      return {'success': false, 'message': 'Failed to delete account: $e'};
    }
  }

  /// Cancel scheduled account deletion
  static Future<Map<String, dynamic>> cancelAccountDeletion() async {
    try {
      final userId = currentUser?.id;
      if (userId == null) {
        return {'success': false, 'message': 'No user logged in'};
      }

      await client.from('users').update({
        'deletion_scheduled_at': null,
        'is_active': true,
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id', userId);

      return {'success': true, 'message': 'Account deletion cancelled'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to cancel deletion: $e'};
    }
  }

  /// Helper to delete all user data
  static Future<void> _deleteUserData(String userId) async {
    // Delete in order of dependencies – each wrapped in try-catch
    // so a missing table doesn't abort the entire deletion
    Future<void> tryDelete(String table, String filter) async {
      try {
        await client.from(table).delete().or(filter);
      } on PostgrestException catch (e) {
        debugPrint('_deleteUserData: skipping $table – ${e.message}');
      }
    }

    Future<void> tryDeleteEq(String table, String col, String val) async {
      try {
        await client.from(table).delete().eq(col, val);
      } on PostgrestException catch (e) {
        debugPrint('_deleteUserData: skipping $table – ${e.message}');
      }
    }

    await tryDeleteEq('post_likes', 'user_id', userId);
    await tryDeleteEq('post_comments', 'user_id', userId);
    await tryDeleteEq('bookmarks', 'user_id', userId.toString());
    await tryDeleteEq('dm_messages', 'sender_id', userId);
    await tryDeleteEq('conversation_participants', 'user_id', userId);
    await tryDeleteEq('notifications', 'user_id', userId);
    await tryDelete('follows', 'follower_id.eq.$userId,following_id.eq.$userId');
    await tryDelete('blocked_users', 'blocker_id.eq.$userId,blocked_id.eq.$userId');
    await tryDeleteEq('reports', 'reporter_id', userId);
    await tryDeleteEq('posts', 'user_id', userId);
    await tryDeleteEq('profiles', 'id', userId);
    await tryDeleteEq('users', 'id', userId);
  }

  /// Resend email verification
  static Future<Map<String, dynamic>> resendEmailVerification() async {
    try {
      final email = currentUser?.email;
      if (email == null) {
        return {'success': false, 'message': 'No email found'};
      }

      await client.auth.resend(type: OtpType.signup, email: email);
      return {'success': true, 'message': 'Verification email sent to $email'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to send verification: $e'};
    }
  }

  /// Check if email is verified
  static bool get isEmailVerified => currentUser?.emailConfirmedAt != null;

  /// Update password
  static Future<Map<String, dynamic>> updatePassword(String newPassword) async {
    try {
      await client.auth.updateUser(UserAttributes(password: newPassword));
      return {'success': true, 'message': 'Password updated successfully'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to update password: $e'};
    }
  }

  /// Update email
  static Future<Map<String, dynamic>> updateEmail(String newEmail) async {
    try {
      await client.auth.updateUser(UserAttributes(email: newEmail));
      return {'success': true, 'message': 'Verification email sent to $newEmail'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to update email: $e'};
    }
  }

  // ============ Blocking & Muting ============

  /// Block a user
  static Future<Map<String, dynamic>> blockUser(String userId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('blocked_users').insert({
        'blocker_id': currentUserId,
        'blocked_id': userId,
        'created_at': DateTime.now().toIso8601String(),
      });

      // Also unfollow if following
      await unfollowUser(userId);

      return {'success': true, 'message': 'User blocked'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to block user: $e'};
    }
  }

  /// Unblock a user
  static Future<Map<String, dynamic>> unblockUser(String userId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('blocked_users').delete()
          .eq('blocker_id', currentUserId)
          .eq('blocked_id', userId);

      return {'success': true, 'message': 'User unblocked'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to unblock user: $e'};
    }
  }

  /// Get list of blocked users
  static Future<List<String>> getBlockedUsers() async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) return [];

      final response = await client
          .from('blocked_users')
          .select('blocked_id')
          .eq('blocker_id', currentUserId);

      return (response as List).map((e) => e['blocked_id'] as String).toList();
    } catch (e) {
      debugPrint('Error getting blocked users: $e');
      return [];
    }
  }

  /// Check if a user is blocked
  static Future<bool> isUserBlocked(String userId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) return false;

      final response = await client
          .from('blocked_users')
          .select()
          .eq('blocker_id', currentUserId)
          .eq('blocked_id', userId)
          .maybeSingle();

      return response != null;
    } catch (e) {
      return false;
    }
  }

  /// Mute a user (hide their posts but don't block)
  static Future<Map<String, dynamic>> muteUser(String userId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('muted_users').insert({
        'muter_id': currentUserId,
        'muted_id': userId,
        'created_at': DateTime.now().toIso8601String(),
      });

      return {'success': true, 'message': 'User muted'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to mute user: $e'};
    }
  }

  /// Unmute a user
  static Future<Map<String, dynamic>> unmuteUser(String userId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('muted_users').delete()
          .eq('muter_id', currentUserId)
          .eq('muted_id', userId);

      return {'success': true, 'message': 'User unmuted'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to unmute user: $e'};
    }
  }

  // ============ Reporting ============

  /// Report content (post, comment, user, or message)
  static Future<Map<String, dynamic>> reportContent({
    required String contentType, // 'post', 'comment', 'user', 'message'
    required String contentId,
    required String reason,
    String? additionalInfo,
  }) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('reports').insert({
        'reporter_id': currentUserId,
        'content_type': contentType,
        'content_id': contentId,
        'reason': reason,
        'additional_info': additionalInfo,
        'status': 'pending',
        'created_at': DateTime.now().toIso8601String(),
      });

      return {'success': true, 'message': 'Report submitted. We\'ll review it shortly.'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to submit report: $e'};
    }
  }

  // ============ Bookmarks ============

  /// Bookmark a post
  static Future<Map<String, dynamic>> bookmarkPost(String postId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('bookmarks').insert({
        'user_id': currentUserId,
        'post_id': postId,
        'created_at': DateTime.now().toIso8601String(),
      });

      return {'success': true, 'message': 'Post bookmarked'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to bookmark: $e'};
    }
  }

  /// Remove bookmark
  static Future<Map<String, dynamic>> removeBookmark(String postId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) {
        return {'success': false, 'message': 'Not logged in'};
      }

      await client.from('bookmarks').delete()
          .eq('user_id', currentUserId)
          .eq('post_id', postId);

      return {'success': true, 'message': 'Bookmark removed'};
    } catch (e) {
      return {'success': false, 'message': 'Failed to remove bookmark: $e'};
    }
  }

  /// Get bookmarked posts
  static Future<List<Post>> getBookmarkedPosts({int limit = 20, int offset = 0}) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) return [];

      final response = await client
          .from('bookmarks')
          .select('post_id, posts(*)')
          .eq('user_id', currentUserId)
          .order('created_at', ascending: false)
          .range(offset, offset + limit - 1);

      return (response as List)
          .where((e) => e['posts'] != null)
          .map((e) => _postFromSupabaseRow(e['posts']))
          .toList();
    } catch (e) {
      debugPrint('Error getting bookmarks: $e');
      return [];
    }
  }

  /// Check if post is bookmarked
  static Future<bool> isPostBookmarked(String postId) async {
    try {
      final currentUserId = currentUser?.id;
      if (currentUserId == null) return false;

      final response = await client
          .from('bookmarks')
          .select()
          .eq('user_id', currentUserId)
          .eq('post_id', postId)
          .maybeSingle();

      return response != null;
    } catch (e) {
      return false;
    }
  }

  // ============ Search ============

  /// Search users
  static Future<List<app_models.User>> searchUsers(String query, {int limit = 20}) async {
    try {
      final response = await client
          .from('users')
          .select()
          .or('username.ilike.%$query%,display_name.ilike.%$query%')
          .limit(limit);

      return (response as List).map((e) => _userFromSupabaseRow(Map<String, dynamic>.from(e))).toList();
    } catch (e) {
      debugPrint('[SupabaseService] Error searching users: $e');
      return [];
    }
  }

  /// Search posts
  static Future<List<Post>> searchPosts(String query, {int limit = 20}) async {
    try {
      final response = await client
          .from('posts')
          .select()
          .ilike('content', '%$query%')
          .order('created_at', ascending: false)
          .limit(limit);

      return (response as List).map((e) => _postFromSupabaseRow(e)).toList();
    } catch (e) {
      debugPrint('Error searching posts: $e');
      return [];
    }
  }

  // ============ Session Management ============

  /// Refresh the current session
  static Future<Map<String, dynamic>> refreshSession() async {
    try {
      final response = await client.auth.refreshSession();
      if (response.session != null) {
        return {'success': true, 'message': 'Session refreshed'};
      }
      return {'success': false, 'message': 'Failed to refresh session'};
    } catch (e) {
      return {'success': false, 'message': 'Session refresh error: $e'};
    }
  }

  /// Listen to auth state changes
  static Stream<AuthState> get authStateChanges => client.auth.onAuthStateChange;

  // ============ Follow System ============

  static Future<void> followUser(String userId) async {
    final uid = currentUser?.id;
    if (uid == null) throw Exception('Not logged in');
    await client.from('follows').insert({
      'follower_id': uid,
      'following_id': userId,
    });
  }

  static Future<void> unfollowUser(String userId) async {
    final uid = currentUser?.id;
    if (uid == null) return;
    await client.from('follows').delete()
        .eq('follower_id', uid)
        .eq('following_id', userId);
  }

  static Future<bool> isFollowing(String userId) async {
    final uid = currentUser?.id;
    if (uid == null) return false;
    final response = await client
        .from('follows')
        .select()
        .eq('follower_id', uid)
        .eq('following_id', userId)
        .maybeSingle();

    return response != null;
  }

  // ============ Storage ============

  static Future<String> uploadImage({
    required String bucket,
    required String path,
    required List<int> bytes,
    String? contentType,
  }) async {
    await client.storage.from(bucket).uploadBinary(
      path,
      bytes as dynamic,
      fileOptions: FileOptions(contentType: contentType ?? 'image/jpeg'),
    );

    return client.storage.from(bucket).getPublicUrl(path);
  }

  // ============ Realtime ============

  static RealtimeChannel subscribeToMessages(
    String conversationId,
    void Function(Map<String, dynamic>) onMessage,
  ) {
    return client.channel('messages:$conversationId')
        .onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'dm_messages',
          filter: PostgresChangeFilter(
            type: PostgresChangeFilterType.eq,
            column: 'conversation_id',
            value: conversationId,
          ),
          callback: (payload) => onMessage(payload.newRecord),
        )
        .subscribe();
  }

  static void unsubscribe(RealtimeChannel channel) {
    client.removeChannel(channel);
  }

  // ============ Dev/Test Data Methods ============

  /// Create a test post for debugging purposes
  static Future<Map<String, dynamic>> createTestPost() async {
    try {
      // Check if Supabase SDK is initialized at all
      if (!isInitialized) {
        return {
          'success': false,
          'message': 'Supabase SDK not initialized. Check your SUPABASE_URL and SUPABASE_ANON_KEY.',
          'error': 'Not initialized',
        };
      }

      // Check if Supabase is properly configured
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return {
          'success': false,
          'message': 'Supabase not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY environment variables.',
          'error': 'Invalid Supabase URL',
        };
      }

      // Generate a unique test post
      final timestamp = DateTime.now().millisecondsSinceEpoch;
      final now = DateTime.now().toUtc();
      final testContent = '''
🧪 Test Post #$timestamp

This is a test post from NeuroComet Flutter Dev Options.
Created at: ${now.toIso8601String()}

#testing #neurocomet
''';

      final userId = currentUser?.id;

      // Build post data matching the actual database schema.
      final Map<String, dynamic> postData = {
        'content': testContent,
        'created_at': now.toIso8601String(),
        'likes': 0,
        'comments': 0,
        'shares': 0,
        'is_liked_by_me': false,
      };

      // Add user_id if logged in (stored as TEXT in DB)
      if (userId != null) {
        postData['user_id'] = userId;
      }

      // Debug: print what we're sending
      debugPrint('Creating test post with data: $postData');

      final response = await client.from('posts').insert(postData).select().single();

      return {
        'success': true,
        'message': 'Test post created successfully! ID: ${response['id']}',
        'data': response,
        'postId': response['id'],
      };
    } on PostgrestException catch (e) {
      debugPrint('PostgrestException: ${e.message}, code: ${e.code}, details: ${e.details}');
      return {
        'success': false,
        'message': 'Database error: ${e.message}\n\nCode: ${e.code}',
        'error': e.toString(),
      };
    } catch (e) {
      debugPrint('Exception creating test post: $e');
      return {
        'success': false,
        'message': 'Failed to create test post: $e',
        'error': e.toString(),
      };
    }
  }

  /// Create a test user profile for debugging purposes
  static Future<Map<String, dynamic>> createTestUser() async {
    try {
      if (!isInitialized) {
        return {
          'success': false,
          'message': 'Supabase SDK not initialized.',
          'error': 'Not initialized',
        };
      }

      // Check if Supabase is properly initialized
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return {
          'success': false,
          'message': 'Supabase not configured. Please set SUPABASE_URL and SUPABASE_ANON_KEY environment variables.',
          'error': 'Invalid Supabase URL',
        };
      }

      final timestamp = DateTime.now().millisecondsSinceEpoch;
      final now = DateTime.now().toUtc();

      // Insert into users table with required fields
      Map<String, dynamic> userData = {
        'username': 'testuser_$timestamp',
        'email': 'testuser_$timestamp@neurocomet.test',
        'created_at': now.toIso8601String(),
      };

      debugPrint('Creating test user with data: $userData');

      final response = await client.from('users').insert(userData).select().single();

      return {
        'success': true,
        'message': 'Test user created successfully! ID: ${response['id']}',
        'data': response,
        'userId': response['id'],
      };
    } on PostgrestException catch (e) {
      debugPrint('PostgrestException creating user: ${e.message}, code: ${e.code}');
      return {
        'success': false,
        'message': 'Database error: ${e.message}\n\nCode: ${e.code}',
        'error': e.toString(),
      };
    } catch (e) {
      debugPrint('Exception creating test user: $e');
      return {
        'success': false,
        'message': 'Failed to create test user: $e',
        'error': e.toString(),
      };
    }
  }

  /// Get the schema of a table (for debugging)
  static Future<Map<String, dynamic>> getTableSchema(String tableName) async {
    try {
      // Try to get one row to see the columns
      final response = await client.from(tableName).select().limit(1);

      if ((response as List).isNotEmpty) {
        final columns = response.first.keys.toList();
        return {
          'success': true,
          'message': 'Table "$tableName" columns:\n${columns.join(", ")}',
          'columns': columns,
          'sampleRow': response.first,
        };
      } else {
        // Table exists but is empty
        return {
          'success': true,
          'message': 'Table "$tableName" exists but is empty.\n\nTo see the schema, add a row in Supabase dashboard first, or just try "Send Test Post" - the error message will show which columns are required.',
          'columns': <String>[],
        };
      }
    } on PostgrestException catch (e) {
      if (e.code == '42P01' || e.message.contains('does not exist')) {
        return {
          'success': false,
          'message': 'Table "$tableName" does not exist in your Supabase database.\n\nPlease create the table first in the Supabase dashboard.',
          'error': e.toString(),
        };
      }
      return {
        'success': false,
        'message': 'Database error for "$tableName": ${e.message}\nCode: ${e.code}',
        'error': e.toString(),
      };
    } catch (e) {
      return {
        'success': false,
        'message': 'Failed to get schema for "$tableName": $e',
        'error': e.toString(),
      };
    }
  }

  /// Get count of posts in database
  static Future<int> getPostCount() async {
    try {
      if (!isInitialized) return -1;
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return -1;
      }
      final response = await client
          .from('posts')
          .select('id')
          .count(CountOption.exact);
      return response.count;
    } catch (e) {
      debugPrint('Error getting post count: $e');
      return -1;
    }
  }

  /// Get count of users in database
  static Future<int> getUserCount() async {
    try {
      if (!isInitialized) return -1;
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return -1;
      }
      final response = await client
          .from('users')
          .select('id')
          .count(CountOption.exact);
      return response.count;
    } catch (e) {
      debugPrint('Error getting user count: $e');
      return -1;
    }
  }

  /// List all available tables in the database
  static Future<Map<String, dynamic>> listTables() async {
    try {
      if (!isInitialized) {
        return {'success': false, 'message': 'Supabase SDK not initialized.'};
      }
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return {
          'success': false,
          'message': 'Supabase not configured',
        };
      }

      // Try to query common table names to see which exist
      final existingTables = <String>[];
      final missingTables = <String>[];

      final tablesToCheck = [
        'posts',
        'users',
        'profiles',
        'post_likes',
        'post_comments',
        'stories',
        'conversations',
        'conversation_participants',
        'dm_messages',
        'message_reactions',
        'notifications',
        'follows',
        'blocked_users',
        'muted_users',
        'bookmarks',
        'reports',
      ];

      for (final table in tablesToCheck) {
        try {
          await client.from(table).select('id').limit(1);
          existingTables.add(table);
        } catch (e) {
          missingTables.add(table);
        }
      }

      return {
        'success': true,
        'message': 'Found tables: ${existingTables.join(", ")}\n\nNot found: ${missingTables.join(", ")}',
        'existingTables': existingTables,
        'missingTables': missingTables,
      };
    } catch (e) {
      return {
        'success': false,
        'message': 'Error listing tables: $e',
      };
    }
  }

  /// Test database connectivity
  static Future<Map<String, dynamic>> testConnection() async {
    try {
      if (!isInitialized) {
        return {
          'success': false,
          'message': 'Supabase SDK not initialized.',
          'error': 'Not initialized',
        };
      }

      // Check if URL is configured
      final url = client.rest.url;
      if (url.isEmpty || url.contains('your-project')) {
        return {
          'success': false,
          'message': 'Supabase URL not configured.\n\nPlease run flutter with:\n--dart-define=SUPABASE_URL=<your-url>\n--dart-define=SUPABASE_ANON_KEY=<your-key>',
          'error': 'Invalid configuration',
        };
      }

      // Simple query to test connectivity using posts table
      final response = await client.from('posts').select('id').limit(1);
      final authStatus = currentUser != null
          ? 'Logged in as ${currentUser!.email ?? currentUser!.id}'
          : (devModeSkipAuth ? 'Dev-mode skip (no real user)' : 'Not authenticated');
      return {
        'success': true,
        'message': 'Database connection successful!\n'
            'URL: $url\n'
            'Auth: $authStatus\n'
            'Posts found: ${(response as List).length}',
        'data': response,
      };
    } on PostgrestException catch (e) {
      return {
        'success': false,
        'message': 'Database error: ${e.message}\nCode: ${e.code}',
        'error': e.toString(),
      };
    } catch (e) {
      return {
        'success': false,
        'message': 'Database connection failed: $e',
        'error': e.toString(),
      };
    }
  }

  /// Delete test data (posts with 'test' category)
  static Future<Map<String, dynamic>> deleteTestPosts() async {
    try {
      await client.from('posts').delete().eq('category', 'test');
      return {
        'success': true,
        'message': 'Test posts deleted successfully!',
      };
    } catch (e) {
      return {
        'success': false,
        'message': 'Failed to delete test posts: $e',
        'error': e.toString(),
      };
    }
  }

  /// Test like functionality - adds a like to a post
  static Future<Map<String, dynamic>> testLike() async {
    try {
      if (!isInitialized) {
        return {
          'success': false,
          'message': 'Supabase SDK not initialized.',
          'error': 'Not initialized',
        };
      }

      // Check if Supabase is properly initialized
      if (client.rest.url.isEmpty || client.rest.url.contains('your-project')) {
        return {
          'success': false,
          'message': 'Supabase not configured.',
          'error': 'Invalid Supabase URL',
        };
      }

      // First, get a post to like
      final postsResponse = await client.from('posts').select('id, likes').limit(1);

      String postId;
      int currentLikes;

      if ((postsResponse as List).isEmpty) {
        // No posts exist, create one first
        final createResult = await createTestPost();
        if (!createResult['success']) {
          return {
            'success': false,
            'message': 'No posts to like and failed to create one: ${createResult['message']}',
            'error': createResult['error'],
          };
        }
        postId = createResult['postId'];
        currentLikes = 0;
      } else {
        final post = postsResponse.first;
        postId = post['id'].toString();
        currentLikes = post['likes'] ?? 0;
      }

      // Try to add a like to the post_likes table first (like Android version)
      try {
        final timestamp = DateTime.now().millisecondsSinceEpoch;
        await client.from('post_likes').insert({
          'post_id': postId,
          'user_id': 'test_user_$timestamp',
          'created_at': DateTime.now().toUtc().toIso8601String(),
        });

        // Also update the likes count on the post
        final newLikes = currentLikes + 1;
        await client.from('posts').update({
          'likes': newLikes,
        }).eq('id', postId);

        return {
          'success': true,
          'message': '❤️ Like added to post!\nPost ID: $postId\nLikes: $currentLikes → $newLikes',
          'postId': postId,
          'likes': newLikes,
        };
      } on PostgrestException catch (e) {
        // post_likes table might not exist, try direct update
        if (e.code == 'PGRST205' || e.message.contains('does not exist')) {
          debugPrint('post_likes table not found, updating post directly');

          final newLikes = currentLikes + 1;
          await client.from('posts').update({
            'likes': newLikes,
          }).eq('id', postId);

          return {
            'success': true,
            'message': '❤️ Like added (direct update)!\nPost ID: $postId\nLikes: $currentLikes → $newLikes',
            'postId': postId,
            'likes': newLikes,
          };
        }
        rethrow;
      }
    } on PostgrestException catch (e) {
      debugPrint('PostgrestException testing like: ${e.message}, code: ${e.code}');
      return {
        'success': false,
        'message': 'Database error: ${e.message}\n\nCode: ${e.code}',
        'error': e.toString(),
      };
    } catch (e) {
      debugPrint('Exception testing like: $e');
      return {
        'success': false,
        'message': 'Failed to test like: $e',
        'error': e.toString(),
      };
    }
  }

  /// Get like count for a specific post
  static Future<Map<String, dynamic>> getPostLikeStatus(String postId) async {
    try {
      final response = await client
          .from('posts')
          .select('id, likes')
          .eq('id', postId)
          .single();

      return {
        'success': true,
        'postId': response['id'],
        'likes': response['likes'],
        'message': 'Post has ${response['likes']} likes.',
      };
    } catch (e) {
      return {
        'success': false,
        'message': 'Failed to get like status: $e',
        'error': e.toString(),
      };
    }
  }
}


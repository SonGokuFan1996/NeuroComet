import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/user.dart' as app_models;
import '../models/post.dart';
import '../models/conversation.dart';
import '../models/notification.dart';

class SupabaseService {
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
    await client.auth.resetPasswordForEmail(email);
  }

  static Future<bool> signInWithGoogle() async {
    return await client.auth.signInWithOAuth(
      OAuthProvider.google,
      redirectTo: 'io.neurocomet.app://callback',
    );
  }

  static Future<bool> signInWithApple() async {
    return await client.auth.signInWithOAuth(
      OAuthProvider.apple,
      redirectTo: 'io.neurocomet.app://callback',
    );
  }

  // ============ User Profile ============

  static Future<app_models.User?> getUserProfile(String userId) async {
    final response = await client
        .from('users')
        .select()
        .eq('id', userId)
        .single();

    return app_models.User.fromJson(response);
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

  // ============ Posts ============

  static Future<List<Post>> getFeedPosts({
    int limit = 20,
    int offset = 0,
  }) async {
    // Query the actual posts table schema:
    //   id, user_id, content, image_url, video_url, likes, created_at
    final response = await client
        .from('posts')
        .select('*')
        .order('created_at', ascending: false)
        .range(offset, offset + limit - 1);

    return (response as List).map((json) => _postFromSupabaseRow(json)).toList();
  }

  static Future<List<Post>> getUserPosts(String userId, {
    int limit = 20,
    int offset = 0,
  }) async {
    final response = await client
        .from('posts')
        .select('*')
        .eq('user_id', userId)
        .order('created_at', ascending: false)
        .range(offset, offset + limit - 1);

    return (response as List).map((json) => _postFromSupabaseRow(json)).toList();
  }

  /// Convert a raw Supabase posts row to a Post model.
  /// The DB schema (id, user_id, content, image_url, video_url, likes, comments, shares, category, created_at)
  /// differs from the app model, so we map manually.
  static Post _postFromSupabaseRow(Map<String, dynamic> json) {
    return Post(
      id: json['id'].toString(),
      authorId: (json['user_id'] as String?) ?? 'unknown',
      authorName: 'User ${(json['user_id'] as String?)?.substring(0, 8) ?? 'anon'}',
      authorAvatarUrl: null,
      content: json['content'] as String? ?? '',
      mediaUrls: [
        if (json['image_url'] != null) json['image_url'] as String,
        if (json['video_url'] != null) json['video_url'] as String,
      ],
      likeCount: (json['likes'] as num?)?.toInt() ?? 0,
      commentCount: (json['comments'] as num?)?.toInt() ?? 0,
      shareCount: (json['shares'] as num?)?.toInt() ?? 0,
      category: json['category'] as String?,
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
  }) async {
    // The actual posts schema: id, user_id (TEXT), content, image_url, video_url, likes, created_at
    final response = await client.from('posts').insert({
      'user_id': currentUser?.id,
      'content': content,
      'image_url': mediaUrls?.isNotEmpty == true ? mediaUrls!.first : null,
    }).select().single();

    return _postFromSupabaseRow(response);
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
    final response = await client
        .from('conversations')
        .select('''
          *,
          participants:conversation_participants(
            user:users(id, display_name, avatar_url)
          ),
          last_message:messages(content, created_at)
        ''')
        .order('updated_at', ascending: false);

    return (response as List).map((json) => Conversation.fromJson(json)).toList();
  }

  static Future<List<Message>> getMessages(String conversationId) async {
    final response = await client
        .from('messages')
        .select('''
          *,
          sender:users!sender_id(id, display_name, avatar_url)
        ''')
        .eq('conversation_id', conversationId)
        .order('created_at', ascending: true);

    return (response as List).map((json) => Message.fromJson(json)).toList();
  }

  static Future<Message> sendMessage({
    required String conversationId,
    required String content,
    MessageType type = MessageType.text,
    String? mediaUrl,
  }) async {
    final uid = currentUser?.id;
    if (uid == null) throw Exception('Not logged in');
    final response = await client.from('messages').insert({
      'conversation_id': conversationId,
      'sender_id': uid,
      'content': content,
      'type': type.name,
      'media_url': mediaUrl,
    }).select().single();

    return Message.fromJson(response);
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

    return (response as List).map((json) => AppNotification.fromJson(json)).toList();
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

  // ============ Account Management ============

  /// Delete user account (GDPR compliance)
  /// This schedules the account for deletion with a 14-day grace period.
  /// NOTE: Immediate deletion of the auth user requires a Supabase Edge Function
  /// with the service-role key. The anon key cannot call admin.deleteUser().
  static Future<Map<String, dynamic>> deleteAccount({bool immediate = false}) async {
    try {
      final userId = currentUser?.id;
      if (userId == null) {
        return {'success': false, 'message': 'No user logged in'};
      }

      if (immediate) {
        // Delete all user data from tables
        await _deleteUserData(userId);
        // NOTE: We cannot call client.auth.admin.deleteUser() with the anon key.
        // To fully remove the auth record, deploy a Supabase Edge Function:
        //   import { createClient } from '@supabase/supabase-js'
        //   const supabase = createClient(url, SERVICE_ROLE_KEY)
        //   await supabase.auth.admin.deleteUser(userId)
      } else {
        // Soft delete - mark account for deletion in 14 days
        await client.from('users').update({
          'deletion_scheduled_at': DateTime.now().add(const Duration(days: 14)).toIso8601String(),
          'is_active': false,
        }).eq('id', userId);
      }

      await signOut();
      return {
        'success': true,
        'message': immediate
            ? 'Account deleted immediately'
            : 'Account scheduled for deletion in 14 days. Log in to cancel.',
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
    await tryDeleteEq('messages', 'sender_id', userId);
    await tryDeleteEq('notifications', 'user_id', userId);
    await tryDelete('follows', 'follower_id.eq.$userId,following_id.eq.$userId');
    await tryDelete('blocked_users', 'blocker_id.eq.$userId,blocked_id.eq.$userId');
    await tryDeleteEq('reports', 'reporter_id', userId);
    await tryDeleteEq('posts', 'user_id', userId);
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

      return (response as List).map((e) => app_models.User.fromJson(e)).toList();
    } catch (e) {
      debugPrint('Error searching users: $e');
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
          table: 'messages',
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

      // Build post data matching the actual database schema:
      //   posts (id BIGSERIAL, user_id TEXT, content TEXT, image_url TEXT,
      //          video_url TEXT, likes INTEGER, comments INTEGER, shares INTEGER,
      //          category TEXT, created_at TIMESTAMPTZ)
      final Map<String, dynamic> postData = {
        'content': testContent,
        'created_at': now.toIso8601String(),
        'likes': 0,
        'comments': 0,
        'shares': 0,
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
        'post_likes',
        'post_comments',
        'stories',
        'conversations',
        'conversation_participants',
        'messages',
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
      return {
        'success': true,
        'message': 'Database connection successful!\nURL: $url\nPosts found: ${(response as List).length}',
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


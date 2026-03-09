import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../models/backup_metadata.dart';
import 'supabase_service.dart';
import 'local_backup_service.dart';

/// Progress state for backup operations
class BackupProgress {
  final String stage;
  final double progress; // 0.0 to 1.0
  final bool isComplete;
  final String? error;

  const BackupProgress({
    required this.stage,
    required this.progress,
    this.isComplete = false,
    this.error,
  });

  factory BackupProgress.idle() => const BackupProgress(
    stage: 'Idle',
    progress: 0.0,
  );

  factory BackupProgress.error(String message) => BackupProgress(
    stage: 'Error',
    progress: 0.0,
    error: message,
  );

  factory BackupProgress.complete() => const BackupProgress(
    stage: 'Complete',
    progress: 1.0,
    isComplete: true,
  );
}

/// Core backup orchestrator — exports & imports all user data
class BackupService {
  static const String _backupVersion = '1.0.0';
  static const String _settingsKey = 'backup_settings';

  // Progress notifier for UI
  static final progressNotifier = ValueNotifier<BackupProgress>(BackupProgress.idle());

  // ═══════════════════════════════════════════════════════════════
  // SETTINGS PERSISTENCE
  // ═══════════════════════════════════════════════════════════════

  /// Load backup settings from SharedPreferences
  static Future<BackupSettings> loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    final json = prefs.getString(_settingsKey);
    if (json == null) return const BackupSettings();
    try {
      return BackupSettings.fromJson(jsonDecode(json) as Map<String, dynamic>);
    } catch (e) {
      debugPrint('BackupService: failed to load settings: $e');
      return const BackupSettings();
    }
  }

  /// Save backup settings to SharedPreferences
  static Future<void> saveSettings(BackupSettings settings) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_settingsKey, jsonEncode(settings.toJson()));
  }

  // ═══════════════════════════════════════════════════════════════
  // EXPORT / CREATE BACKUP
  // ═══════════════════════════════════════════════════════════════

  /// Create a full backup of all user data
  /// Returns the BackupMetadata on success, null on failure
  ///
  /// SECURITY: Only available in debug builds for dev testing.
  static Future<BackupMetadata?> createBackup({
    BackupScope scope = const BackupScope(),
    BackupStorageLocation storageLocation = BackupStorageLocation.local,
  }) async {
    // SECURITY: Block in release builds
    if (!kDebugMode) {
      progressNotifier.value = BackupProgress.error('Backup not available');
      return null;
    }

    try {
      final userId = SupabaseService.currentUser?.id;
      if (userId == null) {
        progressNotifier.value = BackupProgress.error('Not logged in');
        return null;
      }

      final backupId = '${DateTime.now().millisecondsSinceEpoch}_$userId';
      final manifest = <String, int>{};
      final backupData = <String, dynamic>{
        'backup_version': _backupVersion,
        'backup_id': backupId,
        'user_id': userId,
        'created_at': DateTime.now().toIso8601String(),
        'app_version': '1.0.0',
      };

      final client = SupabaseService.client;
      int step = 0;
      final totalSteps = _countSteps(scope);

      // ---- Profile ----
      if (scope.includeProfile) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up profile...',
          progress: step / totalSteps,
        );
        try {
          final profile = await client
              .from('users')
              .select()
              .eq('id', userId)
              .maybeSingle();
          if (profile != null) {
            backupData['profile'] = profile;
            manifest['profile'] = 1;
          }
        } catch (e) {
          debugPrint('Backup: failed to fetch profile: $e');
        }
        step++;
      }

      // ---- Posts ----
      if (scope.includePosts) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up posts...',
          progress: step / totalSteps,
        );
        try {
          final posts = await _fetchAllPaginated(
            client.from('posts').select().eq('user_id', userId),
          );
          backupData['posts'] = posts;
          manifest['posts'] = posts.length;

          // Also fetch comments on user's posts
          final comments = await _fetchAllPaginated(
            client.from('post_comments').select().eq('user_id', userId),
          );
          backupData['post_comments'] = comments;
          manifest['post_comments'] = comments.length;

          // Post likes
          final likes = await _fetchAllPaginated(
            client.from('post_likes').select().eq('user_id', userId),
          );
          backupData['post_likes'] = likes;
          manifest['post_likes'] = likes.length;
        } catch (e) {
          debugPrint('Backup: failed to fetch posts: $e');
        }
        step++;
      }

      // ---- Messages & Conversations ----
      if (scope.includeMessages) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up messages...',
          progress: step / totalSteps,
        );
        try {
          // Get conversation participant records for this user
          final participantRecords = await _fetchAllPaginated(
            client.from('conversation_participants')
                .select('conversation_id')
                .eq('user_id', userId),
          );
          final conversationIds = participantRecords
              .map((r) => r['conversation_id'] as String)
              .toList();

          if (conversationIds.isNotEmpty) {
            // Fetch conversations
            final conversations = await client
                .from('conversations')
                .select()
                .inFilter('id', conversationIds);
            backupData['conversations'] = conversations;
            manifest['conversations'] = (conversations as List).length;

            // Fetch all messages in those conversations
            final allMessages = <Map<String, dynamic>>[];
            for (final convId in conversationIds) {
              final messages = await _fetchAllPaginated(
                client.from('dm_messages')
                    .select()
                    .eq('conversation_id', convId),
              );
              allMessages.addAll(messages);
            }
            backupData['messages'] = allMessages;
            manifest['messages'] = allMessages.length;
          }
        } catch (e) {
          debugPrint('Backup: failed to fetch messages: $e');
        }
        step++;
      }

      // ---- Bookmarks ----
      if (scope.includeBookmarks) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up bookmarks...',
          progress: step / totalSteps,
        );
        try {
          final bookmarks = await _fetchAllPaginated(
            client.from('bookmarks').select().eq('user_id', userId),
          );
          backupData['bookmarks'] = bookmarks;
          manifest['bookmarks'] = bookmarks.length;
        } catch (e) {
          debugPrint('Backup: failed to fetch bookmarks: $e');
        }
        step++;
      }

      // ---- Follows ----
      if (scope.includeFollows) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up follows...',
          progress: step / totalSteps,
        );
        try {
          final following = await _fetchAllPaginated(
            client.from('follows').select().eq('follower_id', userId),
          );
          backupData['following'] = following;
          manifest['following'] = following.length;

          final followers = await _fetchAllPaginated(
            client.from('follows').select().eq('following_id', userId),
          );
          backupData['followers'] = followers;
          manifest['followers'] = followers.length;
        } catch (e) {
          debugPrint('Backup: failed to fetch follows: $e');
        }
        step++;
      }

      // ---- Notifications ----
      if (scope.includeNotifications) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up notifications...',
          progress: step / totalSteps,
        );
        try {
          final notifications = await _fetchAllPaginated(
            client.from('notifications').select().eq('user_id', userId),
          );
          backupData['notifications'] = notifications;
          manifest['notifications'] = notifications.length;
        } catch (e) {
          debugPrint('Backup: failed to fetch notifications: $e');
        }
        step++;
      }

      // ---- Local Settings (SharedPreferences) ----
      if (scope.includeSettings) {
        progressNotifier.value = BackupProgress(
          stage: 'Backing up settings...',
          progress: step / totalSteps,
        );
        try {
          final prefs = await SharedPreferences.getInstance();
          final settingsData = <String, dynamic>{};
          final keysToBackup = [
            'theme_mode',
            'high_contrast',
            'reduced_motion',
            'font_scale',
            'font_family',
            'neuro_state',
            'tutorial_completed',
            'dm_privacy_mode',
            'blocked_words',
            'content_filter_level',
            'animation_speed',
            'haptic_feedback',
            'auto_play_videos',
            'notification_sound',
            'break_reminder_minutes',
          ];
          for (final key in keysToBackup) {
            final value = prefs.get(key);
            if (value != null) {
              settingsData[key] = value;
            }
          }
          backupData['local_settings'] = settingsData;
          manifest['local_settings'] = settingsData.length;
        } catch (e) {
          debugPrint('Backup: failed to fetch local settings: $e');
        }
        step++;
      }

      // ---- Serialize ----
      progressNotifier.value = const BackupProgress(
        stage: 'Saving backup...',
        progress: 0.95,
      );

      final jsonString = jsonEncode(backupData);
      final sizeBytes = utf8.encode(jsonString).length;

      final metadata = BackupMetadata(
        backupId: backupId,
        createdAt: DateTime.now(),
        appVersion: '1.0.0',
        sizeBytes: sizeBytes,
        isEncrypted: false,
        storageLocation: storageLocation,
        dataManifest: manifest,
        scope: scope,
      );

      // Save locally
      if (storageLocation == BackupStorageLocation.local) {
        await LocalBackupService.saveBackup(
          backupId: backupId,
          jsonData: jsonString,
        );
        await LocalBackupService.saveMetadata(metadata);
      }

      // Google Drive would be handled here in the future
      // if (storageLocation == BackupStorageLocation.googleDrive) { ... }

      // Update settings with last backup info
      final settings = await loadSettings();
      await saveSettings(settings.copyWith(
        lastBackupAt: DateTime.now(),
        lastBackupId: backupId,
        lastBackupSizeBytes: sizeBytes,
      ));

      progressNotifier.value = BackupProgress.complete();
      return metadata;
    } catch (e) {
      debugPrint('BackupService: createBackup failed: $e');
      progressNotifier.value = BackupProgress.error('Backup failed: $e');
      return null;
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // IMPORT / RESTORE BACKUP
  // ═══════════════════════════════════════════════════════════════

  /// Restore data from a backup
  /// Returns true on success
  ///
  /// SECURITY: Only available in debug builds for dev testing.
  static Future<bool> restoreBackup(String backupId) async {
    // SECURITY: Block in release builds
    if (!kDebugMode) {
      progressNotifier.value = BackupProgress.error('Restore not available');
      return false;
    }

    try {
      final userId = SupabaseService.currentUser?.id;
      if (userId == null) {
        progressNotifier.value = BackupProgress.error('Not logged in');
        return false;
      }

      progressNotifier.value = const BackupProgress(
        stage: 'Loading backup...',
        progress: 0.05,
      );

      final jsonString = await LocalBackupService.readBackup(backupId);
      if (jsonString == null) {
        progressNotifier.value = BackupProgress.error('Backup file not found');
        return false;
      }

      final backupData = jsonDecode(jsonString) as Map<String, dynamic>;
      final backupUserId = backupData['user_id'] as String?;

      // Verify the backup belongs to this user
      if (backupUserId != userId) {
        progressNotifier.value = BackupProgress.error(
          'This backup belongs to a different account',
        );
        return false;
      }

      final client = SupabaseService.client;
      int step = 0;
      const totalSteps = 7;

      // ---- Restore Profile ----
      if (backupData.containsKey('profile')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring profile...',
          progress: step / totalSteps,
        );
        try {
          final profile = backupData['profile'] as Map<String, dynamic>;
          // Only restore safe fields
          await client.from('users').upsert({
            'id': userId,
            'display_name': profile['display_name'],
            'bio': profile['bio'],
            'avatar_url': profile['avatar_url'],
            'banner_url': profile['banner_url'],
            'updated_at': DateTime.now().toIso8601String(),
          });
        } catch (e) {
          debugPrint('Restore: failed to restore profile: $e');
        }
        step++;
      }

      // ---- Restore Posts ----
      if (backupData.containsKey('posts')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring posts...',
          progress: step / totalSteps,
        );
        try {
          final posts = (backupData['posts'] as List)
              .cast<Map<String, dynamic>>();
          for (final post in posts) {
            post['user_id'] = userId; // Ensure correct ownership
            await client.from('posts').upsert(post);
          }
        } catch (e) {
          debugPrint('Restore: failed to restore posts: $e');
        }
        step++;
      }

      // ---- Restore Bookmarks ----
      if (backupData.containsKey('bookmarks')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring bookmarks...',
          progress: step / totalSteps,
        );
        try {
          final bookmarks = (backupData['bookmarks'] as List)
              .cast<Map<String, dynamic>>();
          for (final bookmark in bookmarks) {
            bookmark['user_id'] = userId;
            try {
              await client.from('bookmarks').upsert(bookmark);
            } catch (_) {}
          }
        } catch (e) {
          debugPrint('Restore: failed to restore bookmarks: $e');
        }
        step++;
      }

      // ---- Restore Follows ----
      if (backupData.containsKey('following')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring follows...',
          progress: step / totalSteps,
        );
        try {
          final following = (backupData['following'] as List)
              .cast<Map<String, dynamic>>();
          for (final follow in following) {
            follow['follower_id'] = userId;
            try {
              await client.from('follows').upsert(follow);
            } catch (_) {}
          }
        } catch (e) {
          debugPrint('Restore: failed to restore follows: $e');
        }
        step++;
      }

      // ---- Restore Messages ----
      if (backupData.containsKey('messages')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring messages...',
          progress: step / totalSteps,
        );
        // NOTE: Message restore is best-effort. Conversations must already
        // exist or be re-created. For now we upsert what we can.
        try {
          final messages = (backupData['messages'] as List)
              .cast<Map<String, dynamic>>();
          for (final msg in messages) {
            try {
              await client.from('dm_messages').upsert(msg);
            } catch (_) {}
          }
        } catch (e) {
          debugPrint('Restore: failed to restore messages: $e');
        }
        step++;
      }

      // ---- Restore Local Settings ----
      if (backupData.containsKey('local_settings')) {
        progressNotifier.value = BackupProgress(
          stage: 'Restoring settings...',
          progress: step / totalSteps,
        );
        try {
          final prefs = await SharedPreferences.getInstance();
          final settings = (backupData['local_settings'] as Map<String, dynamic>);
          for (final entry in settings.entries) {
            final value = entry.value;
            if (value is bool) {
              await prefs.setBool(entry.key, value);
            } else if (value is int) {
              await prefs.setInt(entry.key, value);
            } else if (value is double) {
              await prefs.setDouble(entry.key, value);
            } else if (value is String) {
              await prefs.setString(entry.key, value);
            }
          }
        } catch (e) {
          debugPrint('Restore: failed to restore local settings: $e');
        }
        step++;
      }

      progressNotifier.value = BackupProgress.complete();
      return true;
    } catch (e) {
      debugPrint('BackupService: restoreBackup failed: $e');
      progressNotifier.value = BackupProgress.error('Restore failed: $e');
      return false;
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // HELPERS
  // ═══════════════════════════════════════════════════════════════

  /// Count the number of backup steps for progress calculation
  static int _countSteps(BackupScope scope) {
    int steps = 0;
    if (scope.includeProfile) steps++;
    if (scope.includePosts) steps++;
    if (scope.includeMessages) steps++;
    if (scope.includeBookmarks) steps++;
    if (scope.includeFollows) steps++;
    if (scope.includeNotifications) steps++;
    if (scope.includeSettings) steps++;
    return steps.clamp(1, 100);
  }

  /// Fetch all rows from a query, paginated in batches of 1000
  static Future<List<Map<String, dynamic>>> _fetchAllPaginated(
    PostgrestFilterBuilder query,
  ) async {
    final results = <Map<String, dynamic>>[];
    int offset = 0;
    const batchSize = 1000;

    while (true) {
      try {
        final batch = await query
            .range(offset, offset + batchSize - 1)
            .order('created_at', ascending: true);

        if (batch is List && batch.isEmpty) break;

        final rows = (batch as List).cast<Map<String, dynamic>>();
        results.addAll(rows);

        if (rows.length < batchSize) break;
        offset += batchSize;
      } catch (e) {
        debugPrint('BackupService: pagination fetch error at offset $offset: $e');
        break;
      }
    }

    return results;
  }
}


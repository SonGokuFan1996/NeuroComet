import 'package:flutter/foundation.dart';
import '../models/backup_metadata.dart';

/// Placeholder Google Drive backup service.
///
/// Full integration requires adding `google_sign_in` and `googleapis`
/// packages and configuring OAuth client IDs for Android/iOS.
///
/// For now, this provides the interface so the UI is wired up,
/// and falls back to local backup with a user-friendly message.
class GoogleDriveBackupService {
  static bool _isConnected = false;
  static String? _connectedEmail;

  /// Whether the user has connected their Google account
  static bool get isConnected => _isConnected;
  static String? get connectedEmail => _connectedEmail;

  /// Connect / sign in to Google Drive
  /// Returns the connected email or null on failure
  static Future<String?> connect() async {
    // TODO: Implement with google_sign_in package
    // final googleSignIn = GoogleSignIn(scopes: ['https://www.googleapis.com/auth/drive.file']);
    // final account = await googleSignIn.signIn();
    // if (account != null) {
    //   _isConnected = true;
    //   _connectedEmail = account.email;
    //   return account.email;
    // }
    debugPrint('GoogleDriveBackupService: Google Sign-In not yet configured');
    return null;
  }

  /// Disconnect from Google Drive
  static Future<void> disconnect() async {
    // TODO: Implement with google_sign_in package
    // await GoogleSignIn().signOut();
    _isConnected = false;
    _connectedEmail = null;
  }

  /// Upload a backup to Google Drive
  /// Returns the Drive file ID on success
  static Future<String?> uploadBackup({
    required String backupId,
    required String jsonData,
    required BackupMetadata metadata,
  }) async {
    if (!_isConnected) {
      debugPrint('GoogleDriveBackupService: not connected');
      return null;
    }

    // TODO: Implement with googleapis DriveApi
    // 1. Get auth headers from GoogleSignIn
    // 2. Create/find "NeuroComet Backups" folder
    // 3. Upload backup_<id>.ncb file
    // 4. Upload meta_<id>.json file
    // 5. Return file ID

    debugPrint('GoogleDriveBackupService: upload not yet implemented');
    return null;
  }

  /// List backups stored on Google Drive
  static Future<List<BackupMetadata>> listBackups() async {
    if (!_isConnected) return [];

    // TODO: Implement with googleapis DriveApi
    // 1. List files in "NeuroComet Backups" folder
    // 2. Download and parse metadata files
    // 3. Return sorted list

    return [];
  }

  /// Download a backup from Google Drive
  /// Returns the backup JSON string
  static Future<String?> downloadBackup(String backupId) async {
    if (!_isConnected) return null;

    // TODO: Implement with googleapis DriveApi
    // 1. Find the file by name
    // 2. Download content
    // 3. Return as string

    return null;
  }

  /// Delete a backup from Google Drive
  static Future<void> deleteBackup(String backupId) async {
    if (!_isConnected) return;

    // TODO: Implement with googleapis DriveApi
    // 1. Find files by name
    // 2. Delete both data and metadata files
  }

  /// Get storage usage on Google Drive for backups
  static Future<int> getStorageUsed() async {
    if (!_isConnected) return 0;
    // TODO: Sum file sizes from Drive
    return 0;
  }
}


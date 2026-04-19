import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:googleapis/drive/v3.dart' as drive;
import 'package:http/http.dart' as http;
import '../models/backup_metadata.dart';

/// Google Drive backup service.
///
/// Uses `google_sign_in` for authentication and `googleapis` Drive API
/// for file management. All backups are stored in a dedicated
/// "NeuroComet Backups" folder on the user's Google Drive.
class GoogleDriveBackupService {
  static bool _isConnected = false;
  static String? _connectedEmail;
  static GoogleSignInAccount? _currentAccount;

  /// Stores the last connection error for diagnostics
  static String? lastError;

  static final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['https://www.googleapis.com/auth/drive.file'],
  );

  /// App folder name on Google Drive
  static const String _folderName = 'NeuroComet Backups';

  /// Whether the user has connected their Google account
  static bool get isConnected => _isConnected;
  static String? get connectedEmail => _connectedEmail;

  /// Connect / sign in to Google Drive
  /// Returns the connected email or null on failure
  static Future<String?> connect() async {
    lastError = null;
    try {
      final account = await _googleSignIn.signIn();
      if (account != null) {
        _currentAccount = account;
        _isConnected = true;
        _connectedEmail = account.email;
        debugPrint('GoogleDriveBackupService: Connected as ${account.email}');
        return account.email;
      }
      lastError = 'Sign-in was cancelled.';
      debugPrint('GoogleDriveBackupService: Sign-in cancelled by user');
      return null;
    } catch (e) {
      final errorStr = e.toString();
      if (errorStr.contains('ApiException: 10')) {
        lastError =
            'Google Sign-In is not configured for this app. '
            'Add an OAuth 2.0 Client ID in Google Cloud Console '
            'with your debug SHA-1 fingerprint.';
      } else if (errorStr.contains('ApiException: 12500') ||
          errorStr.contains('ApiException: 12501')) {
        lastError = 'Sign-in was cancelled or timed out.';
      } else if (errorStr.contains('network_error') ||
          errorStr.contains('NETWORK_ERROR')) {
        lastError =
            'Network error. Please check your internet connection.';
      } else {
        lastError = 'Google Sign-In failed: $errorStr';
      }
      debugPrint('GoogleDriveBackupService: Sign-in failed: $e');
      return null;
    }
  }

  /// Disconnect from Google Drive
  static Future<void> disconnect() async {
    try {
      await _googleSignIn.signOut();
    } catch (e) {
      debugPrint('GoogleDriveBackupService: Sign-out error: $e');
    }
    _isConnected = false;
    _connectedEmail = null;
    _currentAccount = null;
  }

  /// Upload a backup to Google Drive
  /// Returns the Drive file ID on success
  static Future<String?> uploadBackup({
    required String backupId,
    required String jsonData,
    required BackupMetadata metadata,
  }) async {
    if (!_isConnected || _currentAccount == null) {
      debugPrint('GoogleDriveBackupService: not connected');
      return null;
    }

    try {
      final driveApi = await _getDriveApi();
      if (driveApi == null) return null;

      // Find or create the backups folder
      final folderId = await _findOrCreateFolder(driveApi);
      if (folderId == null) {
        debugPrint('GoogleDriveBackupService: could not create folder');
        return null;
      }

      // Upload the backup data file
      final dataFileName = 'backup_$backupId.ncb';
      final dataBytes = utf8.encode(jsonData);
      final dataFile = drive.File()
        ..name = dataFileName
        ..parents = [folderId]
        ..mimeType = 'application/octet-stream';

      final dataMedia = drive.Media(
        Stream.value(dataBytes),
        dataBytes.length,
      );

      final uploadedData = await driveApi.files.create(
        dataFile,
        uploadMedia: dataMedia,
      );

      // Upload the metadata file
      final metaFileName = 'meta_$backupId.json';
      final metaJson = jsonEncode(metadata.toJson());
      final metaBytes = utf8.encode(metaJson);
      final metaFile = drive.File()
        ..name = metaFileName
        ..parents = [folderId]
        ..mimeType = 'application/json';

      final metaMedia = drive.Media(
        Stream.value(metaBytes),
        metaBytes.length,
      );

      await driveApi.files.create(metaFile, uploadMedia: metaMedia);

      debugPrint('GoogleDriveBackupService: uploaded backup $backupId');
      return uploadedData.id;
    } catch (e) {
      debugPrint('GoogleDriveBackupService: upload failed: $e');
      return null;
    }
  }

  /// List backups stored on Google Drive
  static Future<List<BackupMetadata>> listBackups() async {
    if (!_isConnected) return [];

    try {
      final driveApi = await _getDriveApi();
      if (driveApi == null) return [];

      final folderId = await _findFolder(driveApi);
      if (folderId == null) return [];

      // List metadata files in the folder
      final fileList = await driveApi.files.list(
        q: "'$folderId' in parents and name contains 'meta_' and trashed = false",
        $fields: 'files(id, name, size)',
        orderBy: 'createdTime desc',
      );

      if (fileList.files == null || fileList.files!.isEmpty) return [];

      final backups = <BackupMetadata>[];
      for (final file in fileList.files!) {
        try {
          final content = await _downloadFileContent(driveApi, file.id!);
          if (content != null) {
            final json = jsonDecode(content) as Map<String, dynamic>;
            backups.add(BackupMetadata.fromJson(json));
          }
        } catch (e) {
          debugPrint('GoogleDriveBackupService: failed to parse metadata for ${file.name}: $e');
        }
      }

      return backups;
    } catch (e) {
      debugPrint('GoogleDriveBackupService: listBackups failed: $e');
      return [];
    }
  }

  /// Download a backup from Google Drive
  /// Returns the backup JSON string
  static Future<String?> downloadBackup(String backupId) async {
    if (!_isConnected) return null;

    try {
      final driveApi = await _getDriveApi();
      if (driveApi == null) return null;

      final folderId = await _findFolder(driveApi);
      if (folderId == null) return null;

      // Find the data file
      final fileName = 'backup_$backupId.ncb';
      final fileList = await driveApi.files.list(
        q: "'$folderId' in parents and name = '$fileName' and trashed = false",
        $fields: 'files(id, name)',
      );

      if (fileList.files == null || fileList.files!.isEmpty) {
        debugPrint('GoogleDriveBackupService: backup file not found: $fileName');
        return null;
      }

      return await _downloadFileContent(driveApi, fileList.files!.first.id!);
    } catch (e) {
      debugPrint('GoogleDriveBackupService: downloadBackup failed: $e');
      return null;
    }
  }

  /// Delete a backup from Google Drive
  static Future<void> deleteBackup(String backupId) async {
    if (!_isConnected) return;

    try {
      final driveApi = await _getDriveApi();
      if (driveApi == null) return;

      final folderId = await _findFolder(driveApi);
      if (folderId == null) return;

      // Delete both data and metadata files
      for (final name in ['backup_$backupId.ncb', 'meta_$backupId.json']) {
        final fileList = await driveApi.files.list(
          q: "'$folderId' in parents and name = '$name' and trashed = false",
          $fields: 'files(id)',
        );
        if (fileList.files != null) {
          for (final file in fileList.files!) {
            await driveApi.files.delete(file.id!);
          }
        }
      }

      debugPrint('GoogleDriveBackupService: deleted backup $backupId');
    } catch (e) {
      debugPrint('GoogleDriveBackupService: deleteBackup failed: $e');
    }
  }

  /// Get storage usage on Google Drive for backups (in bytes)
  static Future<int> getStorageUsed() async {
    if (!_isConnected) return 0;

    try {
      final driveApi = await _getDriveApi();
      if (driveApi == null) return 0;

      final folderId = await _findFolder(driveApi);
      if (folderId == null) return 0;

      final fileList = await driveApi.files.list(
        q: "'$folderId' in parents and trashed = false",
        $fields: 'files(size)',
      );

      if (fileList.files == null) return 0;

      int totalSize = 0;
      for (final file in fileList.files!) {
        if (file.size != null) {
          totalSize += int.tryParse(file.size!) ?? 0;
        }
      }

      return totalSize;
    } catch (e) {
      debugPrint('GoogleDriveBackupService: getStorageUsed failed: $e');
      return 0;
    }
  }

  // ═══════════════════════════════════════════════════════════════
  // PRIVATE HELPERS
  // ═══════════════════════════════════════════════════════════════

  /// Get an authenticated Drive API client
  static Future<drive.DriveApi?> _getDriveApi() async {
    try {
      final account = _currentAccount ?? await _googleSignIn.signInSilently();
      if (account == null) {
        debugPrint('GoogleDriveBackupService: no signed-in account');
        _isConnected = false;
        return null;
      }

      _currentAccount = account;
      final authHeaders = await account.authHeaders;
      final authenticatedClient = _GoogleAuthClient(authHeaders);
      return drive.DriveApi(authenticatedClient);
    } catch (e) {
      debugPrint('GoogleDriveBackupService: failed to get Drive API: $e');
      return null;
    }
  }

  /// Find the "NeuroComet Backups" folder, or null if it doesn't exist
  static Future<String?> _findFolder(drive.DriveApi driveApi) async {
    try {
      final folderList = await driveApi.files.list(
        q: "name = '$_folderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
        $fields: 'files(id)',
      );
      if (folderList.files != null && folderList.files!.isNotEmpty) {
        return folderList.files!.first.id;
      }
      return null;
    } catch (e) {
      debugPrint('GoogleDriveBackupService: _findFolder error: $e');
      return null;
    }
  }

  /// Find or create the "NeuroComet Backups" folder
  static Future<String?> _findOrCreateFolder(drive.DriveApi driveApi) async {
    final existingId = await _findFolder(driveApi);
    if (existingId != null) return existingId;

    try {
      final folder = drive.File()
        ..name = _folderName
        ..mimeType = 'application/vnd.google-apps.folder';

      final created = await driveApi.files.create(folder);
      debugPrint('GoogleDriveBackupService: created folder with id ${created.id}');
      return created.id;
    } catch (e) {
      debugPrint('GoogleDriveBackupService: _findOrCreateFolder error: $e');
      return null;
    }
  }

  /// Download file content as a string
  static Future<String?> _downloadFileContent(
      drive.DriveApi driveApi, String fileId) async {
    try {
      final media = await driveApi.files.get(
        fileId,
        downloadOptions: drive.DownloadOptions.fullMedia,
      ) as drive.Media;

      final bytes = <int>[];
      await for (final chunk in media.stream) {
        bytes.addAll(chunk);
      }
      return utf8.decode(bytes);
    } catch (e) {
      debugPrint('GoogleDriveBackupService: download error for $fileId: $e');
      return null;
    }
  }
}

/// HTTP client that adds Google auth headers to every request.
class _GoogleAuthClient extends http.BaseClient {
  final Map<String, String> _headers;
  final http.Client _inner = http.Client();

  _GoogleAuthClient(this._headers);

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    request.headers.addAll(_headers);
    return _inner.send(request);
  }
}

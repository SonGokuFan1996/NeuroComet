import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../models/backup_metadata.dart';

/// Manages local backup files on the device
class LocalBackupService {
  static const _backupDirName = 'neurocomet_backups';

  /// Get the local backup directory, creating it if needed
  static Future<Directory> _getBackupDir() async {
    final appDir = await getApplicationDocumentsDirectory();
    final backupDir = Directory('${appDir.path}/$_backupDirName');
    if (!await backupDir.exists()) {
      await backupDir.create(recursive: true);
    }
    return backupDir;
  }

  /// Save a backup JSON string to a local file
  /// Returns the file path
  static Future<String> saveBackup({
    required String backupId,
    required String jsonData,
  }) async {
    final dir = await _getBackupDir();
    final fileName = 'backup_$backupId.ncb'; // .ncb = NeuroComet Backup
    final file = File('${dir.path}/$fileName');
    await file.writeAsString(jsonData);
    debugPrint('LocalBackupService: saved backup to ${file.path}');
    return file.path;
  }

  /// Save backup metadata alongside the backup file
  static Future<void> saveMetadata(BackupMetadata metadata) async {
    final dir = await _getBackupDir();
    final file = File('${dir.path}/meta_${metadata.backupId}.json');
    await file.writeAsString(jsonEncode(metadata.toJson()));
  }

  /// List all local backups with their metadata
  static Future<List<BackupMetadata>> listBackups() async {
    final dir = await _getBackupDir();
    if (!await dir.exists()) return [];

    final metaFiles = dir.listSync()
        .whereType<File>()
        .where((f) => f.path.endsWith('.json') && f.path.contains('meta_'))
        .toList();

    final backups = <BackupMetadata>[];
    for (final file in metaFiles) {
      try {
        final content = await file.readAsString();
        final json = jsonDecode(content) as Map<String, dynamic>;
        backups.add(BackupMetadata.fromJson(json));
      } catch (e) {
        debugPrint('LocalBackupService: failed to read metadata: $e');
      }
    }

    // Sort by date, newest first
    backups.sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return backups;
  }

  /// Read backup data from a local file
  static Future<String?> readBackup(String backupId) async {
    final dir = await _getBackupDir();
    final file = File('${dir.path}/backup_$backupId.ncb');
    if (!await file.exists()) return null;
    return await file.readAsString();
  }

  /// Delete a local backup
  static Future<void> deleteBackup(String backupId) async {
    final dir = await _getBackupDir();
    final dataFile = File('${dir.path}/backup_$backupId.ncb');
    final metaFile = File('${dir.path}/meta_$backupId.json');

    if (await dataFile.exists()) await dataFile.delete();
    if (await metaFile.exists()) await metaFile.delete();
  }

  /// Share a backup file (e.g. via email, cloud, etc.)
  static Future<void> shareBackup(String backupId) async {
    final dir = await _getBackupDir();
    final file = File('${dir.path}/backup_$backupId.ncb');
    if (!await file.exists()) {
      throw Exception('Backup file not found');
    }
    await Share.shareXFiles(
      [XFile(file.path)],
      subject: 'NeuroComet Backup',
      text: 'NeuroComet backup file',
    );
  }

  /// Import a backup from a file path (e.g. from file picker)
  static Future<String?> importBackup(String filePath) async {
    final sourceFile = File(filePath);
    if (!await sourceFile.exists()) return null;
    return await sourceFile.readAsString();
  }

  /// Get total size of all local backups
  static Future<int> getTotalBackupSize() async {
    final dir = await _getBackupDir();
    if (!await dir.exists()) return 0;

    int totalSize = 0;
    for (final file in dir.listSync().whereType<File>()) {
      totalSize += await file.length();
    }
    return totalSize;
  }

  /// Delete all local backups
  static Future<void> deleteAllBackups() async {
    final dir = await _getBackupDir();
    if (await dir.exists()) {
      await dir.delete(recursive: true);
    }
  }
}


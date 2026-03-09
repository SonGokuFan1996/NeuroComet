import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/backup_metadata.dart';
import '../services/backup_service.dart';
import '../services/local_backup_service.dart';
import '../services/google_drive_backup_service.dart';

/// State for the backup feature
class BackupState {
  final BackupSettings settings;
  final List<BackupMetadata> localBackups;
  final List<BackupMetadata> driveBackups;
  final bool isLoading;
  final bool isBackingUp;
  final bool isRestoring;
  final String? errorMessage;
  final String? successMessage;
  final BackupProgress progress;

  const BackupState({
    this.settings = const BackupSettings(),
    this.localBackups = const [],
    this.driveBackups = const [],
    this.isLoading = false,
    this.isBackingUp = false,
    this.isRestoring = false,
    this.errorMessage,
    this.successMessage,
    this.progress = const BackupProgress(stage: 'Idle', progress: 0.0),
  });

  BackupState copyWith({
    BackupSettings? settings,
    List<BackupMetadata>? localBackups,
    List<BackupMetadata>? driveBackups,
    bool? isLoading,
    bool? isBackingUp,
    bool? isRestoring,
    String? errorMessage,
    String? successMessage,
    BackupProgress? progress,
  }) {
    return BackupState(
      settings: settings ?? this.settings,
      localBackups: localBackups ?? this.localBackups,
      driveBackups: driveBackups ?? this.driveBackups,
      isLoading: isLoading ?? this.isLoading,
      isBackingUp: isBackingUp ?? this.isBackingUp,
      isRestoring: isRestoring ?? this.isRestoring,
      errorMessage: errorMessage,
      successMessage: successMessage,
      progress: progress ?? this.progress,
    );
  }
}

/// Provider for backup state management
class BackupNotifier extends Notifier<BackupState> {
  @override
  BackupState build() {
    _init();
    return const BackupState(isLoading: true);
  }

  Future<void> _init() async {
    try {
      final settings = await BackupService.loadSettings();
      final localBackups = await LocalBackupService.listBackups();
      final driveBackups = await GoogleDriveBackupService.listBackups();
      state = state.copyWith(
        settings: settings,
        localBackups: localBackups,
        driveBackups: driveBackups,
        isLoading: false,
      );
    } catch (e) {
      debugPrint('BackupNotifier: init failed: $e');
      state = state.copyWith(isLoading: false);
    }
  }

  /// Refresh the backup lists
  Future<void> refresh() async {
    final localBackups = await LocalBackupService.listBackups();
    final driveBackups = await GoogleDriveBackupService.listBackups();
    state = state.copyWith(
      localBackups: localBackups,
      driveBackups: driveBackups,
    );
  }

  /// Update backup settings
  Future<void> updateSettings(BackupSettings newSettings) async {
    state = state.copyWith(settings: newSettings);
    await BackupService.saveSettings(newSettings);
  }

  /// Create a new backup
  Future<BackupMetadata?> createBackup({
    BackupStorageLocation location = BackupStorageLocation.local,
  }) async {
    if (state.isBackingUp) return null;

    state = state.copyWith(
      isBackingUp: true,
      errorMessage: null,
      successMessage: null,
    );

    // Listen to progress updates
    BackupService.progressNotifier.addListener(_onProgressUpdate);

    try {
      final metadata = await BackupService.createBackup(
        scope: state.settings.scope,
        storageLocation: location,
      );

      if (metadata != null) {
        await refresh();
        state = state.copyWith(
          isBackingUp: false,
          successMessage: 'Backup completed successfully!',
          settings: state.settings.copyWith(
            lastBackupAt: metadata.createdAt,
            lastBackupId: metadata.backupId,
            lastBackupSizeBytes: metadata.sizeBytes,
          ),
        );
        return metadata;
      } else {
        state = state.copyWith(
          isBackingUp: false,
          errorMessage: 'Backup failed. Please try again.',
        );
        return null;
      }
    } catch (e) {
      state = state.copyWith(
        isBackingUp: false,
        errorMessage: 'Backup failed: $e',
      );
      return null;
    } finally {
      BackupService.progressNotifier.removeListener(_onProgressUpdate);
    }
  }

  /// Restore from a backup
  Future<bool> restoreBackup(String backupId) async {
    if (state.isRestoring) return false;

    state = state.copyWith(
      isRestoring: true,
      errorMessage: null,
      successMessage: null,
    );

    BackupService.progressNotifier.addListener(_onProgressUpdate);

    try {
      final success = await BackupService.restoreBackup(backupId);
      if (success) {
        state = state.copyWith(
          isRestoring: false,
          successMessage: 'Data restored successfully! Restart the app for all changes to take effect.',
        );
      } else {
        state = state.copyWith(
          isRestoring: false,
          errorMessage: 'Restore failed. Please try again.',
        );
      }
      return success;
    } catch (e) {
      state = state.copyWith(
        isRestoring: false,
        errorMessage: 'Restore failed: $e',
      );
      return false;
    } finally {
      BackupService.progressNotifier.removeListener(_onProgressUpdate);
    }
  }

  /// Delete a local backup
  Future<void> deleteLocalBackup(String backupId) async {
    await LocalBackupService.deleteBackup(backupId);
    await refresh();
  }

  /// Share a backup file
  Future<void> shareBackup(String backupId) async {
    try {
      await LocalBackupService.shareBackup(backupId);
    } catch (e) {
      state = state.copyWith(errorMessage: 'Failed to share backup: $e');
    }
  }

  /// Delete all local backups
  Future<void> deleteAllLocalBackups() async {
    await LocalBackupService.deleteAllBackups();
    await refresh();
  }

  /// Connect Google Drive
  Future<bool> connectGoogleDrive() async {
    final email = await GoogleDriveBackupService.connect();
    if (email != null) {
      await updateSettings(state.settings.copyWith(
        googleAccountEmail: email,
      ));
      await refresh();
      return true;
    }
    return false;
  }

  /// Disconnect Google Drive
  Future<void> disconnectGoogleDrive() async {
    await GoogleDriveBackupService.disconnect();
    await updateSettings(state.settings.copyWith(
      googleAccountEmail: null,
    ));
  }

  void _onProgressUpdate() {
    state = state.copyWith(
      progress: BackupService.progressNotifier.value,
    );
  }

  /// Clear any messages
  void clearMessages() {
    state = state.copyWith(
      errorMessage: null,
      successMessage: null,
    );
  }
}

/// Riverpod provider for backup state
final backupProvider = NotifierProvider<BackupNotifier, BackupState>(
  BackupNotifier.new,
);

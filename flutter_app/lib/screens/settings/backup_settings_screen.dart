import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';
import '../../models/backup_metadata.dart';
import '../../providers/backup_provider.dart';
import '../../services/backup_service.dart';
import '../../services/device_authorization_service.dart';
import '../../services/google_drive_backup_service.dart';

/// WhatsApp-style Backup & Storage settings screen.
///
/// SECURITY: This screen is only accessible in debug builds for dev testing
/// on whitelisted devices. In release builds, the route is guarded and the
/// settings entry is hidden.
class BackupSettingsScreen extends ConsumerStatefulWidget {
  const BackupSettingsScreen({super.key});

  @override
  ConsumerState<BackupSettingsScreen> createState() => _BackupSettingsScreenState();
}

class _BackupSettingsScreenState extends ConsumerState<BackupSettingsScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<double> _fadeAnimation;
  bool _isCheckingAccess = !kDebugMode;
  bool _hasAccess = kDebugMode;

  @override
  void initState() {
    super.initState();
    _checkAccess();

    _animController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _fadeAnimation = CurvedAnimation(
      parent: _animController,
      curve: Curves.easeOutCubic,
    );
    _animController.forward();
  }

  Future<void> _checkAccess() async {
    if (kDebugMode) return;

    final hasAccess = await DeviceAuthorizationService.canUseDeveloperTools();
    if (!mounted) return;
    setState(() {
      _hasAccess = hasAccess;
      _isCheckingAccess = false;
    });
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (_isCheckingAccess) {
      return Scaffold(
        backgroundColor: theme.colorScheme.surface,
        appBar: AppBar(title: const Text('Backup & Storage')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (!_hasAccess) {
      return Scaffold(
        backgroundColor: theme.colorScheme.surface,
        appBar: AppBar(title: const Text('Backup & Storage')),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: const [
                    Icon(Icons.lock_outline, size: 40),
                    SizedBox(height: 12),
                    Text(
                      'Backup tools are restricted to authorized developer devices.',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
    }

    final backupState = ref.watch(backupProvider);

    // Listen for error/success messages
    ref.listen<BackupState>(backupProvider, (previous, next) {
      if (next.errorMessage != null && next.errorMessage != previous?.errorMessage) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(next.errorMessage!),
            backgroundColor: AppColors.error,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        );
        ref.read(backupProvider.notifier).clearMessages();
      }
      if (next.successMessage != null && next.successMessage != previous?.successMessage) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(next.successMessage!),
            backgroundColor: AppColors.success,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        );
        ref.read(backupProvider.notifier).clearMessages();
      }
    });

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_rounded, color: theme.colorScheme.onSurface),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: Text(
          'Backup & Storage',
          style: theme.textTheme.titleLarge?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        centerTitle: true,
      ),
      body: FadeTransition(
        opacity: _fadeAnimation,
        child: backupState.isLoading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                physics: const BouncingScrollPhysics(
                  parent: AlwaysScrollableScrollPhysics(),
                ),
                children: [
                  // ═══════════════════════════════════════════
                  // LAST BACKUP STATUS CARD
                  // ═══════════════════════════════════════════
                  _LastBackupCard(settings: backupState.settings),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // BACK UP NOW
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'Back Up',
                    icon: Icons.cloud_upload_rounded,
                  ),
                  _BackupNowCard(
                    isBackingUp: backupState.isBackingUp,
                    progress: backupState.progress,
                    onBackupLocal: () {
                      HapticFeedback.mediumImpact();
                      ref.read(backupProvider.notifier).createBackup(
                        location: BackupStorageLocation.local,
                      );
                    },
                    onBackupDrive: GoogleDriveBackupService.isConnected
                        ? () {
                            HapticFeedback.mediumImpact();
                            ref.read(backupProvider.notifier).createBackup(
                              location: BackupStorageLocation.googleDrive,
                            );
                          }
                        : null,
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // GOOGLE DRIVE
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'Google Drive',
                    icon: Icons.cloud_outlined,
                  ),
                  _GoogleDriveCard(
                    email: backupState.settings.googleAccountEmail,
                    isConnected: GoogleDriveBackupService.isConnected,
                    onConnect: () async {
                      HapticFeedback.lightImpact();
                      final success = await ref.read(backupProvider.notifier).connectGoogleDrive();
                      if (!success && context.mounted) {
                        final errorDetail = GoogleDriveBackupService.lastError
                            ?? 'Could not connect to Google Drive. Please check your network connection and try again.';
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(errorDetail),
                            behavior: SnackBarBehavior.floating,
                            duration: const Duration(seconds: 5),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                        );
                      }
                    },
                    onDisconnect: () async {
                      HapticFeedback.lightImpact();
                      await ref.read(backupProvider.notifier).disconnectGoogleDrive();
                    },
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // AUTO-BACKUP SETTINGS
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'Auto-Backup',
                    icon: Icons.schedule_rounded,
                  ),
                  _AutoBackupCard(
                    settings: backupState.settings,
                    onFrequencyChanged: (frequency) {
                      HapticFeedback.selectionClick();
                      ref.read(backupProvider.notifier).updateSettings(
                        backupState.settings.copyWith(
                          autoBackupFrequency: frequency,
                        ),
                      );
                    },
                    onWifiOnlyChanged: (value) {
                      HapticFeedback.selectionClick();
                      ref.read(backupProvider.notifier).updateSettings(
                        backupState.settings.copyWith(wifiOnly: value),
                      );
                    },
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // WHAT TO BACK UP
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'What to Back Up',
                    icon: Icons.checklist_rounded,
                  ),
                  _BackupScopeCard(
                    scope: backupState.settings.scope,
                    onScopeChanged: (scope) {
                      ref.read(backupProvider.notifier).updateSettings(
                        backupState.settings.copyWith(scope: scope),
                      );
                    },
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // ENCRYPTION
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'Security',
                    icon: Icons.lock_outline_rounded,
                  ),
                  _EncryptionCard(
                    isEncrypted: backupState.settings.encryptBackups,
                    onChanged: (value) {
                      HapticFeedback.selectionClick();
                      if (value) {
                        _showEncryptionSetupDialog(context);
                      } else {
                        ref.read(backupProvider.notifier).updateSettings(
                          backupState.settings.copyWith(encryptBackups: false),
                        );
                      }
                    },
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // RESTORE FROM BACKUP
                  // ═══════════════════════════════════════════
                  _SectionHeader(
                    title: 'Restore',
                    icon: Icons.restore_rounded,
                  ),
                  _RestoreCard(
                    localBackups: backupState.localBackups,
                    driveBackups: backupState.driveBackups,
                    isRestoring: backupState.isRestoring,
                    progress: backupState.progress,
                    onRestore: (backupId) {
                      _showRestoreConfirmDialog(context, backupId);
                    },
                    onDelete: (backupId) {
                      _showDeleteConfirmDialog(context, backupId);
                    },
                    onShare: (backupId) {
                      ref.read(backupProvider.notifier).shareBackup(backupId);
                    },
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════
                  // DANGER ZONE
                  // ═══════════════════════════════════════════
                  if (backupState.localBackups.isNotEmpty) ...[
                    _SectionHeader(
                      title: 'Storage Management',
                      icon: Icons.storage_rounded,
                    ),
                    _StorageManagementCard(
                      backupCount: backupState.localBackups.length,
                      onDeleteAll: () => _showDeleteAllConfirmDialog(context),
                    ),
                    const SizedBox(height: 20),
                  ],

                  const SizedBox(height: 80),
                ],
              ),
      ),
    );
  }

  void _showEncryptionSetupDialog(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Row(
          children: [
            Icon(Icons.lock_rounded, color: AppColors.primaryPurple),
            const SizedBox(width: 8),
            const Text('Encrypt Backups'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              'Encrypted backups protect your data with a passphrase. '
              'If you forget your passphrase, you will not be able to restore your backup.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.warning.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: AppColors.warning.withValues(alpha: 0.3)),
              ),
              child: Row(
                children: [
                  Icon(Icons.warning_amber_rounded, color: AppColors.warning, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Passphrase cannot be recovered if lost!',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: AppColors.warning,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              final state = ref.read(backupProvider);
              ref.read(backupProvider.notifier).updateSettings(
                state.settings.copyWith(encryptBackups: true),
              );
              Navigator.pop(ctx);
            },
            child: const Text('Enable'),
          ),
        ],
      ),
    );
  }

  void _showRestoreConfirmDialog(BuildContext context, String backupId) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: Row(
          children: [
            Icon(Icons.restore_rounded, color: AppColors.primaryPurple),
            const SizedBox(width: 8),
            const Text('Restore Backup'),
          ],
        ),
        content: Text(
          'This will restore your data from the selected backup. '
          'Current data that conflicts with the backup will be overwritten.\n\n'
          'Are you sure you want to continue?',
          style: theme.textTheme.bodyMedium?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(backupProvider.notifier).restoreBackup(backupId);
            },
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.primaryPurple,
            ),
            child: const Text('Restore'),
          ),
        ],
      ),
    );
  }

  void _showDeleteConfirmDialog(BuildContext context, String backupId) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Delete Backup'),
        content: const Text('This backup will be permanently deleted. This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(backupProvider.notifier).deleteLocalBackup(backupId);
            },
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.error,
            ),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _showDeleteAllConfirmDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Delete All Backups'),
        content: const Text(
          'All local backups will be permanently deleted. This action cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              ref.read(backupProvider.notifier).deleteAllLocalBackups();
            },
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.error,
            ),
            child: const Text('Delete All'),
          ),
        ],
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════
// WIDGET COMPONENTS
// ═══════════════════════════════════════════════════════════════════

class _SectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;

  const _SectionHeader({required this.title, required this.icon});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8, left: 4),
      child: Row(
        children: [
          Icon(icon, size: 18, color: AppColors.primaryPurple),
          const SizedBox(width: 8),
          Text(
            title,
            style: theme.textTheme.titleSmall?.copyWith(
              color: AppColors.primaryPurple,
              fontWeight: FontWeight.bold,
              letterSpacing: 0.5,
            ),
          ),
        ],
      ),
    );
  }
}

class _LastBackupCard extends StatelessWidget {
  final BackupSettings settings;

  const _LastBackupCard({required this.settings});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final hasBackup = settings.lastBackupAt != null;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: hasBackup
              ? [AppColors.primaryPurple, AppColors.secondaryTeal]
              : [
                  theme.colorScheme.surfaceContainerHighest,
                  theme.colorScheme.surfaceContainerHighest,
                ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        boxShadow: hasBackup
            ? [
                BoxShadow(
                  color: AppColors.primaryPurple.withValues(alpha: 0.3),
                  blurRadius: 16,
                  offset: const Offset(0, 8),
                ),
              ]
            : null,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                hasBackup ? Icons.cloud_done_rounded : Icons.cloud_off_rounded,
                color: hasBackup ? Colors.white : theme.colorScheme.onSurfaceVariant,
                size: 28,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  hasBackup ? 'Last Backup' : 'No Backups Yet',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: hasBackup ? Colors.white : theme.colorScheme.onSurface,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          if (hasBackup) ...[
            const SizedBox(height: 12),
            Text(
              _formatDate(settings.lastBackupAt!),
              style: theme.textTheme.bodyLarge?.copyWith(
                color: Colors.white.withValues(alpha: 0.9),
              ),
            ),
            if (settings.lastBackupSizeBytes != null) ...[
              const SizedBox(height: 4),
              Text(
                'Size: ${_formatSize(settings.lastBackupSizeBytes!)}',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: Colors.white.withValues(alpha: 0.7),
                ),
              ),
            ],
          ] else ...[
            const SizedBox(height: 8),
            Text(
              'Back up your data to keep it safe. You can restore it on this or another device.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ],
      ),
    );
  }

  String _formatDate(DateTime date) {
    final months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return '${months[date.month - 1]} ${date.day}, ${date.year} at '
        '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }

  String _formatSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
  }
}

class _BackupNowCard extends StatelessWidget {
  final bool isBackingUp;
  final BackupProgress progress;
  final VoidCallback onBackupLocal;
  final VoidCallback? onBackupDrive;

  const _BackupNowCard({
    required this.isBackingUp,
    required this.progress,
    required this.onBackupLocal,
    this.onBackupDrive,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          if (isBackingUp) ...[
            Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  SizedBox(
                    width: 60,
                    height: 60,
                    child: CircularProgressIndicator(
                      value: progress.progress > 0 ? progress.progress : null,
                      strokeWidth: 4,
                      color: AppColors.primaryPurple,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    progress.stage,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(
                    value: progress.progress > 0 ? progress.progress : null,
                    borderRadius: BorderRadius.circular(4),
                    color: AppColors.primaryPurple,
                    backgroundColor: AppColors.primaryPurple.withValues(alpha: 0.15),
                  ),
                ],
              ),
            ),
          ] else ...[
            _SettingsListTile(
              icon: Icons.phone_android_rounded,
              iconColor: AppColors.secondaryTeal,
              title: 'Back Up to Device',
              subtitle: 'Save backup on this device',
              onTap: onBackupLocal,
              trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16),
            ),
            Divider(height: 1, indent: 56, endIndent: 16, color: theme.dividerColor.withValues(alpha: 0.3)),
            _SettingsListTile(
              icon: Icons.cloud_upload_rounded,
              iconColor: const Color(0xFF4285F4),
              title: 'Back Up to Google Drive',
              subtitle: onBackupDrive != null
                  ? 'Save backup to your Google Drive'
                  : 'Connect Google Drive first',
              onTap: onBackupDrive ?? () {},
              enabled: onBackupDrive != null,
              trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16),
            ),
          ],
        ],
      ),
    );
  }
}

class _GoogleDriveCard extends StatelessWidget {
  final String? email;
  final bool isConnected;
  final VoidCallback onConnect;
  final VoidCallback onDisconnect;

  const _GoogleDriveCard({
    this.email,
    required this.isConnected,
    required this.onConnect,
    required this.onDisconnect,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          if (isConnected && email != null) ...[
            _SettingsListTile(
              icon: Icons.account_circle_rounded,
              iconColor: const Color(0xFF4285F4),
              title: email!,
              subtitle: 'Connected',
              onTap: () {},
              trailing: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.success.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  'Active',
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: AppColors.success,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
            Divider(height: 1, indent: 56, endIndent: 16, color: theme.dividerColor.withValues(alpha: 0.3)),
            _SettingsListTile(
              icon: Icons.link_off_rounded,
              iconColor: AppColors.error,
              title: 'Disconnect',
              subtitle: 'Remove Google Drive connection',
              onTap: onDisconnect,
            ),
          ] else ...[
            _SettingsListTile(
              icon: Icons.add_circle_outline_rounded,
              iconColor: const Color(0xFF4285F4),
              title: 'Connect Google Account',
              subtitle: 'Back up your data to Google Drive',
              onTap: onConnect,
              trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16),
            ),
          ],
        ],
      ),
    );
  }
}

class _AutoBackupCard extends StatelessWidget {
  final BackupSettings settings;
  final ValueChanged<BackupFrequency> onFrequencyChanged;
  final ValueChanged<bool> onWifiOnlyChanged;

  const _AutoBackupCard({
    required this.settings,
    required this.onFrequencyChanged,
    required this.onWifiOnlyChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          _SettingsListTile(
            icon: Icons.autorenew_rounded,
            iconColor: AppColors.primaryPurple,
            title: 'Backup Frequency',
            subtitle: _frequencyLabel(settings.autoBackupFrequency),
            onTap: () => _showFrequencyPicker(context),
            trailing: const Icon(Icons.arrow_forward_ios_rounded, size: 16),
          ),
          Divider(height: 1, indent: 56, endIndent: 16, color: theme.dividerColor.withValues(alpha: 0.3)),
          _SettingsToggleTile(
            icon: Icons.wifi_rounded,
            iconColor: AppColors.secondaryTeal,
            title: 'Wi-Fi Only',
            subtitle: 'Only back up when connected to Wi-Fi',
            value: settings.wifiOnly,
            onChanged: onWifiOnlyChanged,
          ),
        ],
      ),
    );
  }

  String _frequencyLabel(BackupFrequency frequency) {
    switch (frequency) {
      case BackupFrequency.off:
        return 'Off';
      case BackupFrequency.daily:
        return 'Daily';
      case BackupFrequency.weekly:
        return 'Weekly';
      case BackupFrequency.monthly:
        return 'Monthly';
    }
  }

  void _showFrequencyPicker(BuildContext context) {
    final theme = Theme.of(context);
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
                child: Text(
                  'Backup Frequency',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              for (final frequency in BackupFrequency.values)
                RadioListTile<BackupFrequency>(
                  value: frequency,
                  groupValue: settings.autoBackupFrequency,
                  title: Text(_frequencyLabel(frequency)),
                  activeColor: AppColors.primaryPurple,
                  onChanged: (value) {
                    if (value != null) {
                      onFrequencyChanged(value);
                      Navigator.pop(ctx);
                    }
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _BackupScopeCard extends StatelessWidget {
  final BackupScope scope;
  final ValueChanged<BackupScope> onScopeChanged;

  const _BackupScopeCard({
    required this.scope,
    required this.onScopeChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          _SettingsToggleTile(
            icon: Icons.person_rounded,
            iconColor: AppColors.primaryPurple,
            title: 'Profile',
            subtitle: 'Display name, bio, avatar',
            value: scope.includeProfile,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: v,
              includeMessages: scope.includeMessages,
              includePosts: scope.includePosts,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: scope.includeFollows,
              includeSettings: scope.includeSettings,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.chat_rounded,
            iconColor: const Color(0xFF42A5F5),
            title: 'Messages',
            subtitle: 'Conversations and direct messages',
            value: scope.includeMessages,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: v,
              includePosts: scope.includePosts,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: scope.includeFollows,
              includeSettings: scope.includeSettings,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.article_rounded,
            iconColor: AppColors.accentOrange,
            title: 'Posts & Comments',
            subtitle: 'Your posts, comments, and likes',
            value: scope.includePosts,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: scope.includeMessages,
              includePosts: v,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: scope.includeFollows,
              includeSettings: scope.includeSettings,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.bookmark_rounded,
            iconColor: const Color(0xFFFFD700),
            title: 'Bookmarks',
            subtitle: 'Saved posts',
            value: scope.includeBookmarks,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: scope.includeMessages,
              includePosts: scope.includePosts,
              includeBookmarks: v,
              includeFollows: scope.includeFollows,
              includeSettings: scope.includeSettings,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.people_rounded,
            iconColor: AppColors.secondaryTeal,
            title: 'Follows',
            subtitle: 'Following and followers list',
            value: scope.includeFollows,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: scope.includeMessages,
              includePosts: scope.includePosts,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: v,
              includeSettings: scope.includeSettings,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.settings_rounded,
            iconColor: const Color(0xFF78909C),
            title: 'App Settings',
            subtitle: 'Theme, accessibility, preferences',
            value: scope.includeSettings,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: scope.includeMessages,
              includePosts: scope.includePosts,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: scope.includeFollows,
              includeSettings: v,
              includeNotifications: scope.includeNotifications,
            )),
          ),
          _divider(theme),
          _SettingsToggleTile(
            icon: Icons.notifications_rounded,
            iconColor: const Color(0xFFFF9800),
            title: 'Notifications',
            subtitle: 'Notification history',
            value: scope.includeNotifications,
            onChanged: (v) => onScopeChanged(BackupScope(
              includeProfile: scope.includeProfile,
              includeMessages: scope.includeMessages,
              includePosts: scope.includePosts,
              includeBookmarks: scope.includeBookmarks,
              includeFollows: scope.includeFollows,
              includeSettings: scope.includeSettings,
              includeNotifications: v,
            )),
          ),
        ],
      ),
    );
  }

  Widget _divider(ThemeData theme) => Divider(
    height: 1,
    indent: 56,
    endIndent: 16,
    color: theme.dividerColor.withValues(alpha: 0.3),
  );
}

class _EncryptionCard extends StatelessWidget {
  final bool isEncrypted;
  final ValueChanged<bool> onChanged;

  const _EncryptionCard({
    required this.isEncrypted,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          _SettingsToggleTile(
            icon: Icons.enhanced_encryption_rounded,
            iconColor: AppColors.primaryPurple,
            title: 'End-to-End Encryption',
            subtitle: isEncrypted
                ? 'Backups are encrypted with your passphrase'
                : 'Protect backups with a passphrase',
            value: isEncrypted,
            onChanged: onChanged,
          ),
          if (isEncrypted) ...[
            Divider(height: 1, indent: 56, endIndent: 16, color: theme.dividerColor.withValues(alpha: 0.3)),
            Padding(
              padding: const EdgeInsets.fromLTRB(56, 8, 16, 12),
              child: Row(
                children: [
                  Icon(Icons.info_outline_rounded, size: 14, color: theme.colorScheme.onSurfaceVariant),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      'If you forget your passphrase, you won\'t be able to restore encrypted backups.',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 11,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _RestoreCard extends StatelessWidget {
  final List<BackupMetadata> localBackups;
  final List<BackupMetadata> driveBackups;
  final bool isRestoring;
  final BackupProgress progress;
  final ValueChanged<String> onRestore;
  final ValueChanged<String> onDelete;
  final ValueChanged<String> onShare;

  const _RestoreCard({
    required this.localBackups,
    required this.driveBackups,
    required this.isRestoring,
    required this.progress,
    required this.onRestore,
    required this.onDelete,
    required this.onShare,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final allBackups = [...localBackups, ...driveBackups];

    if (isRestoring) {
      return Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          children: [
            SizedBox(
              width: 60,
              height: 60,
              child: CircularProgressIndicator(
                value: progress.progress > 0 ? progress.progress : null,
                strokeWidth: 4,
                color: AppColors.primaryPurple,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              progress.stage,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 8),
            LinearProgressIndicator(
              value: progress.progress > 0 ? progress.progress : null,
              borderRadius: BorderRadius.circular(4),
              color: AppColors.primaryPurple,
              backgroundColor: AppColors.primaryPurple.withValues(alpha: 0.15),
            ),
          ],
        ),
      );
    }

    if (allBackups.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Center(
          child: Column(
            children: [
              Icon(
                Icons.inventory_2_outlined,
                size: 48,
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
              ),
              const SizedBox(height: 12),
              Text(
                'No backups found',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Create a backup to see it here',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        children: [
          for (int i = 0; i < allBackups.length; i++) ...[
            if (i > 0) Divider(height: 1, indent: 56, endIndent: 16, color: theme.dividerColor.withValues(alpha: 0.3)),
            _BackupListItem(
              metadata: allBackups[i],
              onRestore: () => onRestore(allBackups[i].backupId),
              onDelete: () => onDelete(allBackups[i].backupId),
              onShare: () => onShare(allBackups[i].backupId),
            ),
          ],
        ],
      ),
    );
  }
}

class _BackupListItem extends StatelessWidget {
  final BackupMetadata metadata;
  final VoidCallback onRestore;
  final VoidCallback onDelete;
  final VoidCallback onShare;

  const _BackupListItem({
    required this.metadata,
    required this.onRestore,
    required this.onDelete,
    required this.onShare,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isLocal = metadata.storageLocation == BackupStorageLocation.local;

    return InkWell(
      onTap: onRestore,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: (isLocal ? AppColors.secondaryTeal : const Color(0xFF4285F4))
                    .withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                isLocal ? Icons.phone_android_rounded : Icons.cloud_rounded,
                color: isLocal ? AppColors.secondaryTeal : const Color(0xFF4285F4),
                size: 20,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    metadata.formattedDate,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Row(
                    children: [
                      Text(
                        metadata.formattedSize,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                      if (metadata.isEncrypted) ...[
                        const SizedBox(width: 8),
                        Icon(Icons.lock_rounded, size: 12, color: AppColors.primaryPurple),
                        const SizedBox(width: 2),
                        Text(
                          'Encrypted',
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: AppColors.primaryPurple,
                            fontSize: 10,
                          ),
                        ),
                      ],
                    ],
                  ),
                ],
              ),
            ),
            PopupMenuButton<String>(
              onSelected: (value) {
                switch (value) {
                  case 'restore':
                    onRestore();
                    break;
                  case 'share':
                    onShare();
                    break;
                  case 'delete':
                    onDelete();
                    break;
                }
              },
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              itemBuilder: (ctx) => [
                const PopupMenuItem(value: 'restore', child: Row(
                  children: [
                    Icon(Icons.restore_rounded, size: 18),
                    SizedBox(width: 8),
                    Text('Restore'),
                  ],
                )),
                if (isLocal) const PopupMenuItem(value: 'share', child: Row(
                  children: [
                    Icon(Icons.share_rounded, size: 18),
                    SizedBox(width: 8),
                    Text('Share'),
                  ],
                )),
                PopupMenuItem(value: 'delete', child: Row(
                  children: [
                    Icon(Icons.delete_rounded, size: 18, color: AppColors.error),
                    const SizedBox(width: 8),
                    Text('Delete', style: TextStyle(color: AppColors.error)),
                  ],
                )),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _StorageManagementCard extends StatelessWidget {
  final int backupCount;
  final VoidCallback onDeleteAll;

  const _StorageManagementCard({
    required this.backupCount,
    required this.onDeleteAll,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
        borderRadius: BorderRadius.circular(16),
      ),
      child: _SettingsListTile(
        icon: Icons.delete_sweep_rounded,
        iconColor: AppColors.error,
        title: 'Delete All Local Backups',
        subtitle: '$backupCount backup${backupCount == 1 ? '' : 's'} stored',
        onTap: onDeleteAll,
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════════
// REUSABLE TILE WIDGETS
// ═══════════════════════════════════════════════════════════════════

class _SettingsListTile extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final Widget? trailing;
  final bool enabled;

  const _SettingsListTile({
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.subtitle,
    required this.onTap,
    this.trailing,
    this.enabled = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final opacity = enabled ? 1.0 : 0.4;

    return InkWell(
      onTap: enabled ? onTap : null,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Opacity(
          opacity: opacity,
          child: Row(
            children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: iconColor.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(icon, color: iconColor, size: 20),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              ?trailing,
            ],
          ),
        ),
      ),
    );
  }
}

class _SettingsToggleTile extends StatelessWidget {
  final IconData icon;
  final Color iconColor;
  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _SettingsToggleTile({
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      child: Row(
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: iconColor.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: iconColor, size: 20),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          Switch.adaptive(
            value: value,
            onChanged: onChanged,
            activeColor: AppColors.primaryPurple,
          ),
        ],
      ),
    );
  }
}


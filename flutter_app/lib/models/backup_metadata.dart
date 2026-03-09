import 'package:json_annotation/json_annotation.dart';

part 'backup_metadata.g.dart';

/// Frequency for automatic backups
enum BackupFrequency {
  @JsonValue('off')
  off,
  @JsonValue('daily')
  daily,
  @JsonValue('weekly')
  weekly,
  @JsonValue('monthly')
  monthly,
}

/// Where the backup is stored
enum BackupStorageLocation {
  @JsonValue('local')
  local,
  @JsonValue('googleDrive')
  googleDrive,
}

/// What data to include in the backup
@JsonSerializable()
class BackupScope {
  final bool includeProfile;
  final bool includeMessages;
  final bool includePosts;
  final bool includeBookmarks;
  final bool includeFollows;
  final bool includeSettings;
  final bool includeNotifications;

  const BackupScope({
    this.includeProfile = true,
    this.includeMessages = true,
    this.includePosts = true,
    this.includeBookmarks = true,
    this.includeFollows = true,
    this.includeSettings = true,
    this.includeNotifications = true,
  });

  factory BackupScope.fromJson(Map<String, dynamic> json) =>
      _$BackupScopeFromJson(json);
  Map<String, dynamic> toJson() => _$BackupScopeToJson(this);
}

/// Metadata describing a completed backup
@JsonSerializable()
class BackupMetadata {
  final String backupId;
  final DateTime createdAt;
  final String appVersion;
  final int sizeBytes;
  final bool isEncrypted;
  final BackupStorageLocation storageLocation;
  final Map<String, int> dataManifest; // table_name -> row_count
  final BackupScope scope;

  const BackupMetadata({
    required this.backupId,
    required this.createdAt,
    required this.appVersion,
    required this.sizeBytes,
    this.isEncrypted = false,
    required this.storageLocation,
    required this.dataManifest,
    required this.scope,
  });

  factory BackupMetadata.fromJson(Map<String, dynamic> json) =>
      _$BackupMetadataFromJson(json);
  Map<String, dynamic> toJson() => _$BackupMetadataToJson(this);

  String get formattedSize {
    if (sizeBytes < 1024) return '$sizeBytes B';
    if (sizeBytes < 1024 * 1024) return '${(sizeBytes / 1024).toStringAsFixed(1)} KB';
    if (sizeBytes < 1024 * 1024 * 1024) {
      return '${(sizeBytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    return '${(sizeBytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }

  String get formattedDate {
    final d = createdAt;
    final months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return '${months[d.month - 1]} ${d.day}, ${d.year} at ${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
  }
}

/// User's backup preferences (persisted in SharedPreferences)
@JsonSerializable()
class BackupSettings {
  final BackupFrequency autoBackupFrequency;
  final bool wifiOnly;
  final bool encryptBackups;
  final BackupScope scope;
  final String? googleAccountEmail;
  final DateTime? lastBackupAt;
  final String? lastBackupId;
  final int? lastBackupSizeBytes;

  const BackupSettings({
    this.autoBackupFrequency = BackupFrequency.off,
    this.wifiOnly = true,
    this.encryptBackups = false,
    this.scope = const BackupScope(),
    this.googleAccountEmail,
    this.lastBackupAt,
    this.lastBackupId,
    this.lastBackupSizeBytes,
  });

  factory BackupSettings.fromJson(Map<String, dynamic> json) =>
      _$BackupSettingsFromJson(json);
  Map<String, dynamic> toJson() => _$BackupSettingsToJson(this);

  BackupSettings copyWith({
    BackupFrequency? autoBackupFrequency,
    bool? wifiOnly,
    bool? encryptBackups,
    BackupScope? scope,
    String? googleAccountEmail,
    DateTime? lastBackupAt,
    String? lastBackupId,
    int? lastBackupSizeBytes,
  }) {
    return BackupSettings(
      autoBackupFrequency: autoBackupFrequency ?? this.autoBackupFrequency,
      wifiOnly: wifiOnly ?? this.wifiOnly,
      encryptBackups: encryptBackups ?? this.encryptBackups,
      scope: scope ?? this.scope,
      googleAccountEmail: googleAccountEmail ?? this.googleAccountEmail,
      lastBackupAt: lastBackupAt ?? this.lastBackupAt,
      lastBackupId: lastBackupId ?? this.lastBackupId,
      lastBackupSizeBytes: lastBackupSizeBytes ?? this.lastBackupSizeBytes,
    );
  }
}


import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/theme/app_colors.dart';

/// DM Privacy Settings screen.
/// Mirrors the Kotlin DmPrivacySettings.kt
///
/// In-memory DM privacy toggles (block/mute) with persistence.
class DmPrivacySettingsService {
  static final DmPrivacySettingsService _instance =
      DmPrivacySettingsService._internal();
  factory DmPrivacySettingsService() => _instance;
  DmPrivacySettingsService._internal();

  final Set<String> _blocked = {};
  final Set<String> _muted = {};

  bool isBlocked(String userId) => _blocked.contains(userId);
  bool isMuted(String userId) => _muted.contains(userId);

  void block(String userId) => _blocked.add(userId);
  void unblock(String userId) => _blocked.remove(userId);
  void mute(String userId) => _muted.add(userId);
  void unmute(String userId) => _muted.remove(userId);
}

/// DM Privacy Settings Screen
class DmPrivacySettingsScreen extends StatefulWidget {
  const DmPrivacySettingsScreen({super.key});

  @override
  State<DmPrivacySettingsScreen> createState() =>
      _DmPrivacySettingsScreenState();
}

class _DmPrivacySettingsScreenState extends State<DmPrivacySettingsScreen> {
  String _dmPermission = 'everyone';
  bool _showReadReceipts = true;
  bool _showTypingIndicator = true;
  bool _allowGroupInvites = true;
  bool _filterMessageRequests = false;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _dmPermission = prefs.getString('dm_permission') ?? 'everyone';
      _showReadReceipts = prefs.getBool('dm_read_receipts') ?? true;
      _showTypingIndicator = prefs.getBool('dm_typing_indicator') ?? true;
      _allowGroupInvites = prefs.getBool('dm_group_invites') ?? true;
      _filterMessageRequests = prefs.getBool('dm_filter_requests') ?? false;
    });
  }

  Future<void> _saveSetting(String key, dynamic value) async {
    final prefs = await SharedPreferences.getInstance();
    if (value is bool) {
      await prefs.setBool(key, value);
    } else if (value is String) {
      await prefs.setString(key, value);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('DM Privacy'),
      ),
      body: ListView(
        children: [
          // Who can message you
          _buildSectionHeader(theme, 'Who Can Message You'),
          _buildDmPermissionTile(theme, 'Everyone', 'everyone',
              'Anyone on NeuroComet can send you a message'),
          _buildDmPermissionTile(theme, 'Followers Only', 'followers',
              'Only people who follow you can message you'),
          _buildDmPermissionTile(theme, 'People You Follow', 'following',
              'Only people you follow can message you'),
          _buildDmPermissionTile(theme, 'Mutual Followers', 'mutuals',
              'Only mutual followers can message you'),
          _buildDmPermissionTile(theme, 'Nobody', 'nobody',
              'Disable DMs completely'),
          const Divider(height: 32),

          // Message indicators
          _buildSectionHeader(theme, 'Message Indicators'),
          SwitchListTile(
            title: const Text('Read Receipts'),
            subtitle: const Text(
                'Let others know when you\'ve read their messages'),
            value: _showReadReceipts,
            onChanged: (value) {
              HapticFeedback.selectionClick();
              setState(() => _showReadReceipts = value);
              _saveSetting('dm_read_receipts', value);
            },
          ),
          SwitchListTile(
            title: const Text('Typing Indicator'),
            subtitle: const Text(
                'Show others when you\'re typing a message'),
            value: _showTypingIndicator,
            onChanged: (value) {
              HapticFeedback.selectionClick();
              setState(() => _showTypingIndicator = value);
              _saveSetting('dm_typing_indicator', value);
            },
          ),
          const Divider(height: 32),

          // Group settings
          _buildSectionHeader(theme, 'Group Messages'),
          SwitchListTile(
            title: const Text('Allow Group Invites'),
            subtitle: const Text(
                'Let others add you to group conversations'),
            value: _allowGroupInvites,
            onChanged: (value) {
              HapticFeedback.selectionClick();
              setState(() => _allowGroupInvites = value);
              _saveSetting('dm_group_invites', value);
            },
          ),
          const Divider(height: 32),

          // Filtering
          _buildSectionHeader(theme, 'Filtering'),
          SwitchListTile(
            title: const Text('Filter Message Requests'),
            subtitle: const Text(
                'Automatically filter low-quality message requests'),
            value: _filterMessageRequests,
            onChanged: (value) {
              HapticFeedback.selectionClick();
              setState(() => _filterMessageRequests = value);
              _saveSetting('dm_filter_requests', value);
            },
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(ThemeData theme, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title,
        style: theme.textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.bold,
          color: AppColors.primaryPurple,
        ),
      ),
    );
  }

  Widget _buildDmPermissionTile(
      ThemeData theme, String title, String value, String description) {
    final isSelected = _dmPermission == value;

    return RadioListTile<String>(
      title: Text(title),
      subtitle: Text(
        description,
        style: theme.textTheme.bodySmall?.copyWith(
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ),
      value: value,
      groupValue: _dmPermission,
      activeColor: AppColors.primaryPurple,
      onChanged: (newValue) {
        HapticFeedback.selectionClick();
        setState(() => _dmPermission = newValue ?? 'everyone');
        _saveSetting('dm_permission', newValue ?? 'everyone');
      },
    );
  }
}


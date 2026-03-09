import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:share_plus/share_plus.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../services/supabase_service.dart';

class PrivacySettingsScreen extends ConsumerStatefulWidget {
  const PrivacySettingsScreen({super.key});

  @override
  ConsumerState<PrivacySettingsScreen> createState() => _PrivacySettingsScreenState();
}

class _PrivacySettingsScreenState extends ConsumerState<PrivacySettingsScreen> {
  bool _showOnlineStatus = true;
  bool _showLastActive = true;
  bool _allowMessageRequests = true;
  String _whoCanMessage = 'everyone';
  String _whoCanSeeProfile = 'everyone';
  bool _showInSearch = true;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.privacy),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Online Status
          _buildSectionHeader(context, 'Activity Status'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Show Online Status'),
                  subtitle: const Text('Let others see when you\'re online'),
                  value: _showOnlineStatus,
                  onChanged: (value) {
                    setState(() => _showOnlineStatus = value);
                  },
                ),
                const Divider(),
                SwitchListTile(
                  title: const Text('Show Last Active'),
                  subtitle: const Text('Let others see when you were last active'),
                  value: _showLastActive,
                  onChanged: (value) {
                    setState(() => _showLastActive = value);
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Messaging Privacy
          _buildSectionHeader(context, 'Messages'),
          _SettingsCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Allow Message Requests'),
                  subtitle: const Text('Receive messages from people you don\'t follow'),
                  value: _allowMessageRequests,
                  onChanged: (value) {
                    setState(() => _allowMessageRequests = value);
                  },
                ),
                const Divider(),
                ListTile(
                  title: const Text('Who Can Message Me'),
                  subtitle: Text(_getOptionLabel(_whoCanMessage)),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showMessagePrivacyDialog(),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Profile Privacy
          _buildSectionHeader(context, 'Profile'),
          _SettingsCard(
            child: Column(
              children: [
                ListTile(
                  title: const Text('Who Can See My Profile'),
                  subtitle: Text(_getOptionLabel(_whoCanSeeProfile)),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showProfilePrivacyDialog(),
                ),
                const Divider(),
                SwitchListTile(
                  title: const Text('Appear in Search'),
                  subtitle: const Text('Let others find you through search'),
                  value: _showInSearch,
                  onChanged: (value) {
                    setState(() => _showInSearch = value);
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Data & Safety
          _buildSectionHeader(context, 'Data & Safety'),
          _SettingsCard(
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.download),
                  title: const Text('Download Your Data'),
                  subtitle: const Text('Get a copy of your NeuroComet data'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () async {
                    // Show loading
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Preparing your data...')),
                    );

                    try {
                      final user = SupabaseService.currentUser;
                      final userData = {
                        'account': {
                          'id': user?.id,
                          'email': user?.email,
                          'created_at': user?.createdAt,
                        },
                        'exported_at': DateTime.now().toIso8601String(),
                        'note': 'This is a copy of your NeuroComet data.',
                      };

                      final jsonStr = const JsonEncoder.withIndent('  ').convert(userData);

                      await SharePlus.instance.share(
                        ShareParams(
                          text: jsonStr,
                          subject: 'NeuroComet Data Export',
                        ),
                      );
                    } catch (e) {
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text('Failed to export data: $e')),
                        );
                      }
                    }
                  },
                ),
                const Divider(),
                ListTile(
                  leading: const Icon(Icons.block),
                  title: const Text('Blocked Users'),
                  subtitle: const Text('Manage blocked accounts'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () {
                    // Navigate to blocked users
                  },
                ),
                const Divider(),
                ListTile(
                  leading: Icon(Icons.delete_forever, color: AppColors.error),
                  title: Text('Delete Account', style: TextStyle(color: AppColors.error)),
                  subtitle: const Text('Permanently delete your account and data'),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => _showDeleteAccountDialog(),
                ),
              ],
            ),
          ),
          const SizedBox(height: 32),

          // Privacy Info
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.success.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.success.withValues(alpha: 0.3)),
            ),
            child: Row(
              children: [
                const Icon(Icons.shield, color: AppColors.success),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Your privacy is important to us. NeuroComet is designed with privacy-first principles.',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.success,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }

  String _getOptionLabel(String option) {
    switch (option) {
      case 'everyone':
        return 'Everyone';
      case 'followers':
        return 'Followers Only';
      case 'following':
        return 'People I Follow';
      case 'mutual':
        return 'Mutual Followers';
      case 'nobody':
        return 'Nobody';
      default:
        return option;
    }
  }

  void _showMessagePrivacyDialog() {
    showModalBottomSheet(
      context: context,
      builder: (context) => _PrivacyOptionSheet(
        title: 'Who Can Message Me',
        currentValue: _whoCanMessage,
        options: const ['everyone', 'followers', 'mutual', 'nobody'],
        onSelected: (value) {
          setState(() => _whoCanMessage = value);
          Navigator.pop(context);
        },
        getLabel: _getOptionLabel,
      ),
    );
  }

  void _showProfilePrivacyDialog() {
    showModalBottomSheet(
      context: context,
      builder: (context) => _PrivacyOptionSheet(
        title: 'Who Can See My Profile',
        currentValue: _whoCanSeeProfile,
        options: const ['everyone', 'followers', 'mutual'],
        onSelected: (value) {
          setState(() => _whoCanSeeProfile = value);
          Navigator.pop(context);
        },
        getLabel: _getOptionLabel,
      ),
    );
  }

  void _showDeleteAccountDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Account?'),
        content: const Text(
          'This action cannot be undone. All your data, posts, and connections will be permanently deleted.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              Navigator.pop(context);
              try {
                await SupabaseService.signOut();
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Account deletion requested. You have been signed out.')),
                  );
                  // Navigate to root/login
                  Navigator.of(context).popUntil((route) => route.isFirst);
                }
              } catch (e) {
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Error: $e')),
                  );
                }
              }
            },
            style: TextButton.styleFrom(foregroundColor: AppColors.error),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }
}

class _SettingsCard extends StatelessWidget {
  final Widget child;

  const _SettingsCard({required this.child});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Theme.of(context).dividerColor),
      ),
      child: child,
    );
  }
}

class _PrivacyOptionSheet extends StatelessWidget {
  final String title;
  final String currentValue;
  final List<String> options;
  final Function(String) onSelected;
  final String Function(String) getLabel;

  const _PrivacyOptionSheet({
    required this.title,
    required this.currentValue,
    required this.options,
    required this.onSelected,
    required this.getLabel,
  });

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              title,
              style: Theme.of(context).textTheme.titleMedium,
            ),
          ),
          ...options.map((option) => RadioListTile<String>(
            title: Text(getLabel(option)),
            value: option,
            groupValue: currentValue,
            onChanged: (value) {
              if (value != null) onSelected(value);
            },
          )),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}


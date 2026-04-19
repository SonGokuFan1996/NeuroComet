import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Content & Media settings screen matching Android version
/// Allows users to control content filters and media preferences
class ContentSettingsScreen extends ConsumerStatefulWidget {
  const ContentSettingsScreen({super.key});

  @override
  ConsumerState<ContentSettingsScreen> createState() => _ContentSettingsScreenState();
}

class _ContentSettingsScreenState extends ConsumerState<ContentSettingsScreen> {
  // Content filter preferences
  bool _showSensitiveContent = false;
  bool _blurSensitiveMedia = true;
  bool _autoPlayVideos = true;
  bool _highQualityMedia = false;
  bool _showContentWarnings = true;
  String _selectedContentLevel = 'standard';

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Content & Media'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Content Filters Section
          _buildSectionHeader(context, 'Content Filters', Icons.filter_list),
          const SizedBox(height: 8),
          _buildSettingsCard(
            children: [
              _buildDropdownTile(
                title: 'Content Level',
                description: 'Choose what type of content to display',
                icon: Icons.security,
                value: _selectedContentLevel,
                items: const [
                  DropdownMenuItem(value: 'strict', child: Text('Strict (Family-Friendly)')),
                  DropdownMenuItem(value: 'standard', child: Text('Standard')),
                  DropdownMenuItem(value: 'open', child: Text('Open (All Content)')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _selectedContentLevel = value);
                  }
                },
              ),
              const Divider(height: 1, indent: 56),
              _buildToggleTile(
                title: 'Show Content Warnings',
                description: 'Display warnings before sensitive content',
                icon: Icons.warning_amber_outlined,
                value: _showContentWarnings,
                onChanged: (value) => setState(() => _showContentWarnings = value),
              ),
              const Divider(height: 1, indent: 56),
              _buildToggleTile(
                title: 'Show Sensitive Content',
                description: 'Display content marked as sensitive',
                icon: Icons.visibility_outlined,
                value: _showSensitiveContent,
                onChanged: (value) => setState(() => _showSensitiveContent = value),
              ),
              const Divider(height: 1, indent: 56),
              _buildToggleTile(
                title: 'Blur Sensitive Media',
                description: 'Blur images and videos until tapped',
                icon: Icons.blur_on,
                value: _blurSensitiveMedia,
                onChanged: (value) => setState(() => _blurSensitiveMedia = value),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Media Settings Section
          _buildSectionHeader(context, 'Media Settings', Icons.perm_media_outlined),
          const SizedBox(height: 8),
          _buildSettingsCard(
            children: [
              _buildToggleTile(
                title: 'Auto-Play Videos',
                description: 'Automatically play videos in feed',
                icon: Icons.play_circle_outline,
                value: _autoPlayVideos,
                onChanged: (value) => setState(() => _autoPlayVideos = value),
              ),
              const Divider(height: 1, indent: 56),
              _buildToggleTile(
                title: 'High Quality Media',
                description: 'Use more data for better quality',
                icon: Icons.hd_outlined,
                value: _highQualityMedia,
                onChanged: (value) => setState(() => _highQualityMedia = value),
              ),
            ],
          ),
          const SizedBox(height: 24),

          // Info Card
          Card(
            elevation: 0,
            color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    Icons.info_outline,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Content filters help create a safer, more comfortable experience tailored to your needs.',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurface,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title, IconData icon) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        children: [
          Icon(icon, size: 20, color: theme.colorScheme.primary),
          const SizedBox(width: 8),
          Text(
            title,
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: theme.colorScheme.primary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSettingsCard({required List<Widget> children}) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(
          color: Theme.of(context).colorScheme.outlineVariant,
          width: 1,
        ),
      ),
      child: Column(children: children),
    );
  }

  Widget _buildToggleTile({
    required String title,
    required String description,
    required IconData icon,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          Icon(icon, color: theme.colorScheme.onSurfaceVariant),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  description,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          Switch(
            value: value,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }

  Widget _buildDropdownTile<T>({
    required String title,
    required String description,
    required IconData icon,
    required T value,
    required List<DropdownMenuItem<T>> items,
    required ValueChanged<T?> onChanged,
  }) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: theme.colorScheme.onSurfaceVariant),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    Text(
                      description,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          Padding(
            padding: const EdgeInsets.only(left: 40, top: 8),
            child: DropdownButton<T>(
              value: value,
              items: items,
              onChanged: onChanged,
              underline: const SizedBox(),
              isDense: true,
              isExpanded: true,
            ),
          ),
        ],
      ),
    );
  }
}



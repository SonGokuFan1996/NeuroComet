import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/theme_provider.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';

/// Accessibility Settings Screen for neurodivergent-friendly customization
class AccessibilitySettingsScreen extends ConsumerWidget {
  const AccessibilitySettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.accessibility),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Introduction card
          _buildInfoCard(context),
          const SizedBox(height: 24),

          // Motion & Animation
          _buildSectionHeader(context, l10n.get('accessibilityMotionAndAnimation')),
          _SettingsCard(
            child: Column(
              children: [
                _ReducedMotionTile(),
                const Divider(height: 1),
                _AnimationTogglesTile(), // New detailed toggles
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Visual
          _buildSectionHeader(context, l10n.get('accessibilityVisual')),
          _SettingsCard(
            child: Column(
              children: [
                _HighContrastTile(),
                const Divider(height: 1),
                _DyslexicFontTile(),
                const Divider(height: 1),
                _FontSizeTile(),
              ],
            ),
          ),
          const SizedBox(height: 24),

          // Reading & Content
          _buildSectionHeader(context, l10n.get('accessibilityReadingAndContent')),
          _SettingsCard(
            child: Column(
              children: [
                _TextSpacingTile(),
                const Divider(height: 1),
                _LineHeightTile(),
              ],
            ),
          ),
          const SizedBox(height: 32),

          // Reset button
          Center(
            child: TextButton(
              onPressed: () => _showResetDialog(context, ref),
              child: Text(l10n.get('accessibilityResetToDefaults')),
            ),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildInfoCard(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.calmBlue.withAlpha(50),
            AppColors.calmLavender.withAlpha(50),
          ],
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Icon(
            Icons.accessibility_new,
            size: 40,
            color: theme.colorScheme.primary,
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.get('accessibilityComfortMatters'),
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  l10n.get('accessibilityCustomizeDescription'),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
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
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }

  void _showResetDialog(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.get('accessibilityResetSettingsTitle')),
        content: Text(l10n.get('accessibilityResetSettingsMessage')),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(l10n.cancel),
          ),
          FilledButton(
            onPressed: () {
              ref.read(animationSettingsProvider.notifier).updateSettings(const AnimationSettings());
              ref.read(highContrastProvider.notifier).setHighContrast(false);
              ref.read(fontSettingsProvider.notifier).updateSettings(const FontSettings());
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(l10n.get('accessibilityResetSettingsSuccess')),
                  behavior: SnackBarBehavior.floating,
                ),
              );
            },
            child: Text(l10n.get('reset')),
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
        side: BorderSide(
          color: Theme.of(context).dividerColor,
        ),
      ),
      child: child,
    );
  }
}

class _ReducedMotionTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final settings = ref.watch(animationSettingsProvider);

    return SwitchListTile(
      title: Text(l10n.reducedMotion),
      subtitle: Text(l10n.get('minimizeAnimationsAndTransitions')),
      secondary: const Icon(Icons.animation),
      value: settings.disableAll,
      onChanged: (value) {
        ref.read(animationSettingsProvider.notifier).setReducedMotion(value);
      },
    );
  }
}

class _AnimationTogglesTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    return ExpansionTile(
      title: Text(l10n.get('customizeAnimations')),
      subtitle: Text(l10n.get('chooseAnimationsToDisable')),
      leading: const Icon(Icons.motion_photos_on),
      children: [
        _buildToggle(context, ref, l10n.get('logoAnimations'), AnimationType.logo),
        _buildToggle(context, ref, l10n.get('storySpinning'), AnimationType.story),
        _buildToggle(context, ref, l10n.get('feedTransitions'), AnimationType.feed),
        _buildToggle(context, ref, l10n.get('screenTransitions'), AnimationType.transition),
        _buildToggle(context, ref, l10n.get('buttonEffects'), AnimationType.button),
        _buildToggle(context, ref, l10n.get('loadingSpinners'), AnimationType.loading),
      ],
    );
  }

  Widget _buildToggle(BuildContext context, WidgetRef ref, String title, AnimationType type) {
    final settings = ref.watch(animationSettingsProvider);
    final disabled = !settings.shouldAnimate(type);

    return SwitchListTile(
      title: Text(title),
      value: disabled,
      onChanged: settings.disableAll
          ? null // Disable individual toggles if master switch is on
          : (value) {
              final notifier = ref.read(animationSettingsProvider.notifier);
              final current = settings;
              AnimationSettings newSettings;

              switch (type) {
                case AnimationType.logo:
                  newSettings = current.copyWith(disableLogo: value);
                  break;
                case AnimationType.story:
                  newSettings = current.copyWith(disableStory: value);
                  break;
                case AnimationType.feed:
                  newSettings = current.copyWith(disableFeed: value);
                  break;
                case AnimationType.transition:
                  newSettings = current.copyWith(disableTransitions: value);
                  break;
                case AnimationType.button:
                  newSettings = current.copyWith(disableButtons: value);
                  break;
                case AnimationType.loading:
                  newSettings = current.copyWith(disableLoading: value);
                  break;
              }
              notifier.updateSettings(newSettings);
            },
      contentPadding: const EdgeInsets.only(left: 32, right: 16),
      visualDensity: VisualDensity.compact,
    );
  }
}

class _HighContrastTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final highContrast = ref.watch(highContrastProvider);

    return SwitchListTile(
      title: Text(l10n.highContrast),
      subtitle: Text(l10n.get('increaseColorContrast')),
      secondary: const Icon(Icons.contrast),
      value: highContrast,
      onChanged: (value) {
        ref.read(highContrastProvider.notifier).setHighContrast(value);
      },
    );
  }
}

class _DyslexicFontTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final fontSettings = ref.watch(fontSettingsProvider);

    return SwitchListTile(
      title: Text(l10n.dyslexicFont),
      subtitle: Text(l10n.get('useOpenDyslexicFont')),
      secondary: const Icon(Icons.font_download),
      value: fontSettings.useDyslexicFont,
      onChanged: (value) {
        ref.read(fontSettingsProvider.notifier).setUseDyslexicFont(value);
      },
    );
  }
}

class _FontSizeTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final fontSettings = ref.watch(fontSettingsProvider);
    final scale = fontSettings.scale;

    return ListTile(
      leading: const Icon(Icons.text_fields),
      title: Text(l10n.fontSize),
      subtitle: Slider(
        value: scale,
        min: 0.8,
        max: 1.5,
        divisions: 7,
        label: '${(scale * 100).round()}%',
        onChanged: (value) {
          ref.read(fontSettingsProvider.notifier).setScale(value);
        },
      ),
      trailing: Text('${(scale * 100).round()}%'),
    );
  }
}

class _TextSpacingTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final fontSettings = ref.watch(fontSettingsProvider);
    final spacing = fontSettings.letterSpacing;

    return ListTile(
      leading: const Icon(Icons.space_bar),
      title: Text(l10n.get('letterSpacing')),
      subtitle: Slider(
        value: spacing,
        min: 0.0,
        max: 2.0,
        divisions: 10,
        label: spacing.toStringAsFixed(1),
        onChanged: (value) {
          final current = fontSettings;
          ref.read(fontSettingsProvider.notifier).updateSettings(
                current.copyWith(letterSpacing: value),
              );
        },
      ),
      trailing: Text(spacing.toStringAsFixed(1)),
    );
  }
}

class _LineHeightTile extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;
    final fontSettings = ref.watch(fontSettingsProvider);
    final height = fontSettings.lineHeight;

    return ListTile(
      leading: const Icon(Icons.format_line_spacing),
      title: Text(l10n.get('lineHeight')),
      subtitle: Slider(
        value: height,
        min: 1.0,
        max: 2.0,
        divisions: 10,
        label: '${height.toStringAsFixed(1)}x',
        onChanged: (value) {
          final current = fontSettings;
          ref.read(fontSettingsProvider.notifier).updateSettings(
                current.copyWith(lineHeight: value),
              );
        },
      ),
      trailing: Text('${height.toStringAsFixed(1)}x'),
    );
  }
}

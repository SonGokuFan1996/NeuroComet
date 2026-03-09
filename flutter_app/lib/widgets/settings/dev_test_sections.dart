import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:package_info_plus/package_info_plus.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../services/supabase_service.dart';
import '../../utils/stress_test_utils.dart';
import '../../providers/theme_provider.dart';
import '../../core/theme/app_colors.dart';
import 'package:shared_preferences/shared_preferences.dart';

class DevSectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;

  const DevSectionHeader({super.key, required this.title, required this.icon});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(top: 8, bottom: 8),
      child: Row(
        children: [
          Icon(icon, size: 20, color: theme.colorScheme.primary),
          const SizedBox(width: 8),
          Text(
            title.toUpperCase(),
            style: theme.textTheme.labelLarge?.copyWith(
              fontWeight: FontWeight.bold,
              letterSpacing: 1.2,
              color: theme.colorScheme.primary,
            ),
          ),
        ],
      ),
    );
  }
}

class DevTestCard extends StatelessWidget {
  final List<Widget> children;
  final String? title;
  final IconData? icon;

  const DevTestCard({super.key, required this.children, this.title, this.icon});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (title != null) ...[
               DevSectionHeader(title: title!, icon: icon ?? Icons.settings),
               const Divider(),
               const SizedBox(height: 8),
            ],
            ...children,
          ],
        ),
      ),
    );
  }
}

class SupabaseDevSection extends ConsumerWidget {
  final Function(String, {bool isError}) showResult;
  final int? postsCount;
  final int? usersCount;
  final VoidCallback onRefresh;

  const SupabaseDevSection({
    super.key,
    required this.showResult,
    this.postsCount,
    this.usersCount,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DevTestCard(
      title: 'Supabase Data Management',
      icon: Icons.storage,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _StatItem(label: 'Posts', count: postsCount),
            _StatItem(label: 'Users', count: usersCount),
          ],
        ),
        const SizedBox(height: 16),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: onRefresh,
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Counts'),
            ),
            ElevatedButton.icon(
              onPressed: () async {
                try {
                  final res = await SupabaseService.createTestPost();
                  if (!context.mounted) return;
                  final message = (res['message'] as String?) ?? 'Unknown result';
                  final success = (res['success'] as bool?) ?? false;
                  showResult(message, isError: !success);
                  if (success) onRefresh();
                } catch (e) {
                  if (!context.mounted) return;
                  showResult('Crash creating test post: $e', isError: true);
                }
              },
              icon: const Icon(Icons.add_comment, size: 18),
              label: const Text('Create Test Post'),
            ),
            ElevatedButton.icon(
              onPressed: () async {
                try {
                  final res = await SupabaseService.createTestUser();
                  if (!context.mounted) return;
                  final message = (res['message'] as String?) ?? 'Unknown result';
                  final success = (res['success'] as bool?) ?? false;
                  showResult(message, isError: !success);
                  if (success) onRefresh();
                } catch (e) {
                  if (!context.mounted) return;
                  showResult('Crash creating test user: $e', isError: true);
                }
              },
              icon: const Icon(Icons.person_add, size: 18),
              label: const Text('Create Test User'),
            ),
          ],
        ),
      ],
    );
  }
}

class _StatItem extends StatelessWidget {
  final String label;
  final int? count;

  const _StatItem({required this.label, this.count});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Text(
          count?.toString() ?? '--',
          style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
        ),
        Text(label, style: theme.textTheme.bodySmall),
      ],
    );
  }
}

class AuthDevSection extends ConsumerWidget {
  const AuthDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = SupabaseService.currentUser;

    return DevTestCard(
      title: 'Auth & User Session',
      icon: Icons.lock_person,
      children: [
        if (user != null) ...[
          Text('Logged in: ${user.email}', style: const TextStyle(fontWeight: FontWeight.bold)),
          Text('ID: ${user.id}', style: const TextStyle(fontSize: 10)),
          const SizedBox(height: 12),
          ElevatedButton.icon(
            onPressed: () async {
              await SupabaseService.signOut();
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Signed out successfully'))
                );
              }
            },
            icon: const Icon(Icons.logout),
            label: const Text('Sign Out'),
            style: ElevatedButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.errorContainer,
              foregroundColor: Theme.of(context).colorScheme.onErrorContainer,
            ),
          ),
        ] else ...[
          const Text('Status: Not Authenticated'),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              ElevatedButton.icon(
                onPressed: () async {
                  try {
                    await SupabaseService.signInWithEmail(
                      email: 'dev@neurocomet.app',
                      password: 'devtest123',
                    );
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Sign-in attempted with dev credentials')),
                      );
                    }
                  } catch (e) {
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('Auth error: ${e.toString().split('\n').first}')),
                      );
                    }
                  }
                },
                icon: const Icon(Icons.person_outline, size: 18),
                label: const Text('Dev Sign In'),
              ),
              OutlinedButton.icon(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Navigate to login screen for full authentication')),
                  );
                },
                icon: const Icon(Icons.login, size: 18),
                label: const Text('Full Login'),
              ),
            ],
          ),
        ],
      ],
    );
  }
}

class LocaleDevSection extends ConsumerWidget {
  const LocaleDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentLocale = ref.watch(localeProvider);
    final notifier = ref.read(localeProvider.notifier);

    final languages = [
      {'name': 'English', 'code': 'en'},
      {'name': 'Spanish', 'code': 'es'},
      {'name': 'French', 'code': 'fr'},
      {'name': 'Arabic', 'code': 'ar'},
      {'name': 'Hindi', 'code': 'hi'},
      {'name': 'Turkish', 'code': 'tr'},
    ];

    final translationProgress = [
      {'name': 'Urdu', 'count': 1100, 'total': 1708},
      {'name': 'Russian', 'count': 1257, 'total': 1708},
      {'name': 'Italian', 'count': 1036, 'total': 1708},
      {'name': 'Dutch', 'count': 1201, 'total': 1708},
      {'name': 'Polish', 'count': 899, 'total': 1708},
      {'name': 'Vietnamese', 'count': 718, 'total': 1708},
      {'name': 'Thai', 'count': 655, 'total': 1708},
      {'name': 'Indonesian', 'count': 631, 'total': 1708},
      {'name': 'Malaysian', 'count': 1278, 'total': 1708},
      {'name': 'Swedish', 'count': 1098, 'total': 1708},
      {'name': 'Danish', 'count': 827, 'total': 1708},
      {'name': 'Nordic', 'count': 607, 'total': 1708},
      {'name': 'Finnish', 'count': 857, 'total': 1708},
      {'name': 'Icelandic', 'count': 785, 'total': 1708},
      {'name': 'Hebrew', 'count': 527, 'total': 1708},
      {'name': 'Greek', 'count': 919, 'total': 1708},
      {'name': 'Czech', 'count': 1074, 'total': 1708},
      {'name': 'Hungarian', 'count': 1012, 'total': 1708},
      {'name': 'Romanian', 'count': 1061, 'total': 1708},
      {'name': 'Ukrainian', 'count': 770, 'total': 1708},
    ];

    return DevTestCard(
      title: 'Regional & Language',
      icon: Icons.language,
      children: [
        Text('Current: ${currentLocale?.languageCode ?? 'System default (en)'}',
          style: const TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: languages.map((lang) {
            final isSelected = currentLocale?.languageCode == lang['code'] ||
              (currentLocale == null && lang['code'] == 'en');

            return FilterChip(
              label: Text(lang['name']!),
              selected: isSelected,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : Theme.of(context).colorScheme.onSurface,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (selected) {
                if (selected) {
                  notifier.setLocale(Locale(lang['code']!));
                }
              },
            );
          }).toList(),
        ),
        const Divider(height: 24),
        const Text('Translation Progress', style: TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        ...translationProgress.map((item) {
          final count = item['count'] as int;
          final total = item['total'] as int;
          final percent = count / total;
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(item['name'] as String, style: const TextStyle(fontSize: 12)),
                    Text('${(percent * 100).toStringAsFixed(1)}% ($count/$total)', style: const TextStyle(fontSize: 10)),
                  ],
                ),
                const SizedBox(height: 2),
                LinearProgressIndicator(value: percent, minHeight: 4),
              ],
            ),
          );
        }),
      ],
    );
  }
}

class StorageDevSection extends ConsumerWidget {
  const StorageDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DevTestCard(
      title: 'Local Storage Management',
      icon: Icons.data_usage,
      children: [
        const Text('Manage app preferences and cached data.', style: TextStyle(fontSize: 12)),
        const SizedBox(height: 12),
        ElevatedButton.icon(
          onPressed: () async {
            final prefs = await SharedPreferences.getInstance();
            final keys = prefs.getKeys();
            if (context.mounted) {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  title: const Text('Stored Keys'),
                  content: SizedBox(
                    width: double.maxFinite,
                    child: ListView(
                      shrinkWrap: true,
                      children: keys.map((k) => ListTile(
                        title: Text(k, style: const TextStyle(fontSize: 12)),
                        subtitle: Text(prefs.get(k).toString(), maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 10)),
                        trailing: IconButton(
                          icon: const Icon(Icons.delete_outline, size: 18),
                          onPressed: () async {
                            await prefs.remove(k);
                            if (context.mounted) Navigator.pop(context);
                          },
                        ),
                      )).toList(),
                    ),
                  ),
                  actions: [
                    TextButton(onPressed: () => Navigator.pop(context), child: const Text('Close')),
                  ],
                ),
              );
            }
          },
          icon: const Icon(Icons.list_alt),
          label: const Text('Inspect All Keys'),
        ),
        const SizedBox(height: 8),
        OutlinedButton.icon(
          onPressed: () async {
            final prefs = await SharedPreferences.getInstance();
            await prefs.clear();
            if (context.mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('All local storage cleared'))
              );
            }
          },
          icon: const Icon(Icons.cleaning_services),
          label: const Text('Wipe All Storage'),
          style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
        ),
      ],
    );
  }
}

class GeneralOptionsDevSection extends ConsumerWidget {
  const GeneralOptionsDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'General Debug Settings',
      icon: Icons.bug_report,
      children: [
        SwitchListTile(
          title: const Text('Show Debug Overlay'),
          value: options.showDebugOverlay,
          onChanged: notifier.setShowDebugOverlay,
        ),
        SwitchListTile(
          title: const Text('Verbose Logging'),
          value: options.enableVerboseLogging,
          onChanged: notifier.setEnableVerboseLogging,
        ),
      ],
    );
  }
}

class ContentSafetyDevSection extends ConsumerWidget {
  const ContentSafetyDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Content & Safety',
      icon: Icons.security,
      children: [
        DropdownButtonFormField<Audience?>(
          value: options.forcedAudience,
          decoration: const InputDecoration(labelText: 'Force Audience Content'),
          onChanged: notifier.setForcedAudience,
          items: <DropdownMenuItem<Audience?>>[
             const DropdownMenuItem<Audience?>(value: null, child: Text("None (User Selected)")),
             ...Audience.values.map((a) => DropdownMenuItem<Audience?>(value: a, child: Text(a.name))),
          ],
        ),
        const SizedBox(height: 8),
        DropdownButtonFormField<KidsFilterLevel>(
          value: options.kidsFilterLevel,
          decoration: const InputDecoration(labelText: 'Kids Filter Level'),
          onChanged: (v) { if (v != null) notifier.setKidsFilterLevel(v); },
          items: KidsFilterLevel.values.map((l) => DropdownMenuItem<KidsFilterLevel>(value: l, child: Text(l.name))).toList(),
        ),
        SwitchListTile(
          title: const Text('Is Kids Mode'),
          subtitle: const Text('Enable child-safe content filtering'),
          value: options.isKidsMode,
          onChanged: notifier.setIsKidsMode,
        ),
        SwitchListTile(
          title: const Text('Bypass Age Verification'),
          subtitle: const Text('Skip age gate checks'),
          value: options.bypassAgeVerification,
          onChanged: notifier.setBypassAgeVerification,
        ),
        SwitchListTile(
          title: const Text('Force Parental PIN Set'),
          subtitle: const Text('Simulate parental PIN being configured'),
          value: options.forcePinSet,
          onChanged: (v) => notifier.setForcePinSet(v),
        ),
        SwitchListTile(
          title: const Text('Force PIN Verify Success'),
          subtitle: const Text('Auto-pass PIN verification checks'),
          value: options.forcePinVerifySuccess,
          onChanged: (v) => notifier.setForcePinVerifySuccess(v),
        ),
      ],
    );
  }
}

class DmDebugDevSection extends ConsumerStatefulWidget {
  const DmDebugDevSection({super.key});

  @override
  ConsumerState<DmDebugDevSection> createState() => _DmDebugDevSectionState();
}

class _DmDebugDevSectionState extends ConsumerState<DmDebugDevSection> {
  late TextEditingController _delayController;

  @override
  void initState() {
    super.initState();
    _delayController = TextEditingController(text: '0');
  }

  @override
  void dispose() {
    _delayController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'DM Delivery Simulation',
      icon: Icons.chat_bubble_outline,
      children: [
        const Text(
          'Control direct message behavior for testing edge cases.',
          style: TextStyle(fontSize: 12),
        ),
        SwitchListTile(
          title: const Text('Show DM Debug Overlay'),
          subtitle: const Text('Display delivery status info on DM screen'),
          value: options.showDmDebugOverlay,
          onChanged: notifier.setShowDmDebugOverlay,
        ),
        SwitchListTile(
          title: const Text('Force Send Failure'),
          subtitle: const Text('Simulate message send failures'),
          value: options.forceSendFailure,
          onChanged: notifier.setForceSendFailure,
        ),
        SwitchListTile(
          title: const Text('Disable Rate Limiting'),
          subtitle: const Text('Remove message throttling'),
          value: options.disableRateLimit,
          onChanged: notifier.setDisableRateLimit,
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _delayController,
                decoration: const InputDecoration(
                  labelText: 'Artificial Delay (ms)',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.number,
              ),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: () {
                final delay = int.tryParse(_delayController.text) ?? 0;
                notifier.setArtificialDelayMs(delay.clamp(0, 15000));
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Delay set to ${delay}ms')),
                );
              },
              child: const Text('Apply'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        const Text('Moderation Override', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
        const SizedBox(height: 4),
        Wrap(
          spacing: 8,
          children: ModerationOverride.values.map((override) {
            return FilterChip(
              label: Text(override.name),
              selected: options.moderationOverride == override,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: options.moderationOverride == override ? Colors.white : Theme.of(context).colorScheme.onSurface,
                fontWeight: options.moderationOverride == override ? FontWeight.bold : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (_) => notifier.setModerationOverride(override),
            );
          }).toList(),
        ),
      ],
    );
  }
}

class RenderingNetworkDevSection extends ConsumerStatefulWidget {
  const RenderingNetworkDevSection({super.key});

  @override
  ConsumerState<RenderingNetworkDevSection> createState() => _RenderingNetworkDevSectionState();
}

class _RenderingNetworkDevSectionState extends ConsumerState<RenderingNetworkDevSection> {
  late TextEditingController _latencyController;
  late TextEditingController _postCountController;

  @override
  void initState() {
    super.initState();
    _latencyController = TextEditingController(text: '0');
    _postCountController = TextEditingController(text: '10');
  }

  @override
  void dispose() {
    _latencyController.dispose();
    _postCountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Rendering & Network',
      icon: Icons.network_check,
      children: [
        const Text(
          'Test rendering edge cases and network conditions.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          title: const Text('Simulate Loading Error'),
          subtitle: const Text('Force data fetches to fail'),
          value: options.simulateLoadingError,
          onChanged: notifier.setSimulateLoadingError,
        ),
        SwitchListTile(
          title: const Text('Infinite Loading'),
          subtitle: const Text('Keep loading spinners running forever'),
          value: options.infiniteLoading,
          onChanged: notifier.setInfiniteLoading,
        ),
        SwitchListTile(
          title: const Text('Fallback UI Mode'),
          subtitle: const Text('Show fallback/placeholder UI components'),
          value: options.isFallbackUiEnabled,
          onChanged: notifier.setIsFallbackUiEnabled,
        ),
        SwitchListTile(
          title: const Text('Simulate Offline'),
          subtitle: const Text('Pretend device has no network'),
          value: options.simulateOffline,
          onChanged: notifier.setSimulateOffline,
        ),
        SwitchListTile(
          title: const Text('Show Sponsored Posts'),
          subtitle: const Text('Include ad-like sponsored content in feed'),
          value: options.showSponsoredPosts,
          onChanged: (v) => notifier.setShowSponsoredPosts(v),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _latencyController,
                decoration: const InputDecoration(
                  labelText: 'Network Latency (ms)',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.number,
              ),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: () {
                final latency = int.tryParse(_latencyController.text) ?? 0;
                notifier.setNetworkLatencyMs(latency.clamp(0, 30000));
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Latency set to ${latency}ms')),
                );
              },
              child: const Text('Apply'),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _postCountController,
                decoration: const InputDecoration(
                  labelText: 'Mock Post Count',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.number,
              ),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: () {
                final count = int.tryParse(_postCountController.text) ?? 10;
                notifier.setMockPostCount(count.clamp(1, 500));
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Mock post count set to $count')),
                );
              },
              child: const Text('Apply'),
            ),
          ],
        ),
      ],
    );
  }
}

class AuthOverrideDevSection extends ConsumerWidget {
  const AuthOverrideDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Authentication Overrides',
      icon: Icons.admin_panel_settings,
      children: [
        const Text(
          'Override authentication behavior for testing.',
          style: TextStyle(fontSize: 12),
        ),
        SwitchListTile(
          title: const Text('Force Logged Out'),
          subtitle: const Text('Simulate unauthenticated state'),
          value: options.forceLoggedOut,
          onChanged: notifier.setForceLoggedOut,
        ),
        SwitchListTile(
          title: const Text('Bypass Biometric'),
          subtitle: const Text('Skip biometric checks'),
          value: options.bypassBiometric,
          onChanged: notifier.setBypassBiometric,
        ),
        SwitchListTile(
          title: const Text('Force 2FA'),
          subtitle: const Text('Always require two-factor authentication'),
          value: options.force2FA,
          onChanged: notifier.setForce2FA,
        ),
      ],
    );
  }
}

class ResetDevSection extends ConsumerWidget {
  const ResetDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Reset',
      icon: Icons.restart_alt,
      children: [
        const Text(
          'Reset all developer options to their default values.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: OutlinedButton.icon(
            onPressed: () {
              notifier.resetAll();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('All developer options reset to defaults')),
              );
            },
            icon: const Icon(Icons.restart_alt),
            label: const Text('Reset All Dev Options'),
            style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
          ),
        ),
      ],
    );
  }
}

class StressTestingDevSection extends ConsumerWidget {
  const StressTestingDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Performance Stress Tests',
      icon: Icons.speed,
      children: [
        SwitchListTile(
          title: const Text('Show Performance Overlay'),
          value: options.showPerformanceOverlay,
          onChanged: notifier.setShowPerformanceOverlay,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton(
              onPressed: () => _runStressTest(context, 'Widget Creation', () => StressTester.runWidgetStressTest(1000)),
              child: const Text('Widgets (1000)'),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(context, 'Animation Stress', StressTester.runAnimationStressTest),
              child: const Text('Animations'),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(context, 'Memory Pressure', StressTester.runMemoryStressTest),
              child: const Text('Memory Pressure'),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(context, 'Network Simulation', () => StressTester.runNetworkStressTest(20)),
              child: const Text('Network (20 req)'),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(context, 'Rapid Nav', StressTester.runNavigationStressTest),
              child: const Text('Rapid Nav'),
            ),
          ],
        ),
      ],
    );
  }

  Future<void> _runStressTest(BuildContext context, String title, Future<StressTestResult> Function() testFn) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );

    final result = await testFn();

    if (context.mounted) Navigator.pop(context);

    if (context.mounted) {
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          title: Text(result.testName),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(result.message),
              const SizedBox(height: 8),
              Text('Duration: ${result.durationMs}ms', style: const TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('OK'),
            ),
          ],
        ),
      );
    }
  }
}

// ═══════════════════════════════════════════════════════════════
//  NEW SECTIONS
// ═══════════════════════════════════════════════════════════════

class AppInfoDevSection extends StatefulWidget {
  const AppInfoDevSection({super.key});

  @override
  State<AppInfoDevSection> createState() => _AppInfoDevSectionState();
}

class _AppInfoDevSectionState extends State<AppInfoDevSection> {
  PackageInfo? _packageInfo;
  int _currentMemoryMB = 0;

  @override
  void initState() {
    super.initState();
    _loadInfo();
  }

  Future<void> _loadInfo() async {
    final info = await PackageInfo.fromPlatform();
    if (mounted) {
      setState(() {
        _packageInfo = info;
        _refreshMemory();
      });
    }
  }

  void _refreshMemory() {
    // ProcessInfo is not readily available on all platforms;
    // use a rough estimate from the Dart runtime.
    _currentMemoryMB = (ProcessInfo.currentRss / (1024 * 1024)).round();
  }

  @override
  Widget build(BuildContext context) {
    final info = _packageInfo;
    final rows = <MapEntry<String, String>>[
      MapEntry('Package', info?.packageName ?? '—'),
      MapEntry('Version', info?.version ?? '—'),
      MapEntry('Build', info?.buildNumber ?? '—'),
      MapEntry('Platform', '${Platform.operatingSystem} ${Platform.operatingSystemVersion}'),
      MapEntry('Dart', Platform.version.split(' ').first),
      MapEntry('Memory (RSS)', '$_currentMemoryMB MB'),
    ];

    return DevTestCard(
      title: 'App Info & Diagnostics',
      icon: Icons.info,
      children: [
        ...rows.map((e) => Padding(
          padding: const EdgeInsets.symmetric(vertical: 2),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(e.key, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12)),
              Flexible(child: Text(e.value, style: const TextStyle(fontSize: 12), textAlign: TextAlign.end)),
            ],
          ),
        )),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: () {
            setState(() => _refreshMemory());
          },
          icon: const Icon(Icons.refresh, size: 16),
          label: const Text('Refresh Memory Stats'),
        ),
      ],
    );
  }
}

class EnvironmentPickerDevSection extends ConsumerWidget {
  const EnvironmentPickerDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'Environment',
      icon: Icons.cloud,
      children: [
        const Text(
          'Switch between backend environments. May require app restart.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: DevEnvironmentTarget.values.map((env) {
            final isSelected = options.environment == env;
            return ChoiceChip(
              label: Text(env.name.toUpperCase()),
              selected: isSelected,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : Theme.of(context).colorScheme.onSurface,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (selected) {
                if (selected && options.environment != env) {
                  showDialog(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      title: const Text('Switch Environment?'),
                      content: Text('Switching to ${env.name}. The app may need to be restarted.'),
                      actions: [
                        TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                        TextButton(
                          onPressed: () {
                            notifier.setEnvironment(env);
                            Navigator.pop(ctx);
                          },
                          child: const Text('Switch'),
                        ),
                      ],
                    ),
                  );
                }
              },
            );
          }).toList(),
        ),
        const SizedBox(height: 8),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            children: [
              Icon(
                options.environment == DevEnvironmentTarget.production
                    ? Icons.verified_user
                    : options.environment == DevEnvironmentTarget.staging
                        ? Icons.science
                        : Icons.computer,
                size: 18,
              ),
              const SizedBox(width: 8),
              Text('Active: ${options.environment.name.toUpperCase()}',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
            ],
          ),
        ),
      ],
    );
  }
}

class FeatureFlagsDevSection extends ConsumerWidget {
  const FeatureFlagsDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    final flags = [
      _FlagItem('New Feed Layout', 'Experimental feed grid layout', options.enableNewFeedLayout, notifier.setEnableNewFeedLayout),
      _FlagItem('Video Chat', 'Enable video calling feature', options.enableVideoChat, notifier.setEnableVideoChat),
      _FlagItem('Story Reactions', 'Allow emoji reactions on stories', options.enableStoryReactions, notifier.setEnableStoryReactions),
      _FlagItem('Advanced Search', 'Full-text and filter search', options.enableAdvancedSearch, notifier.setEnableAdvancedSearch),
      _FlagItem('AI Suggestions', 'AI-powered content suggestions', options.enableAiSuggestions, notifier.setEnableAiSuggestions),
    ];

    final activeCount = flags.where((f) => f.value).length;

    return DevTestCard(
      title: 'Feature Flags',
      icon: Icons.flag,
      children: [
        const Text('Toggle experimental features. Persisted across restarts.', style: TextStyle(fontSize: 12)),
        const SizedBox(height: 8),
        ...flags.map((flag) => SwitchListTile(
          title: Text(flag.label),
          subtitle: Text(flag.subtitle, style: const TextStyle(fontSize: 11)),
          value: flag.value,
          onChanged: (v) => flag.onChanged(v),
          dense: true,
        )),
        const SizedBox(height: 4),
        Text('$activeCount of ${flags.length} flags enabled',
          style: TextStyle(fontSize: 11, color: Theme.of(context).colorScheme.onSurfaceVariant)),
      ],
    );
  }
}

class _FlagItem {
  final String label;
  final String subtitle;
  final bool value;
  final void Function(bool) onChanged;
  const _FlagItem(this.label, this.subtitle, this.value, this.onChanged);
}

class ABTestingDevSection extends ConsumerWidget {
  const ABTestingDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final theme = Theme.of(context);

    final variants = [
      (ABTestVariant.control, 'Control', Icons.science_outlined, 'Default production UI'),
      (ABTestVariant.liquidGlass, 'Liquid Glass', Icons.blur_on_rounded, 'Frosted glass translucent UI'),
      (ABTestVariant.compactCards, 'Compact Cards', Icons.view_agenda_rounded, 'Smaller, denser card layout'),
      (ABTestVariant.boldTypography, 'Bold Type', Icons.format_bold_rounded, 'Larger, high-contrast text'),
    ];

    return DevTestCard(
      title: 'A/B Test Variants',
      icon: Icons.science,
      children: [
        const Text(
          'Switch between experimental UI variants. Affects feed, explore, and messages.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: variants.map((variant) {
            final (value, label, icon, description) = variant;
            final isSelected = options.abTestVariant == value;
            return ChoiceChip(
              avatar: Icon(
                icon,
                size: 18,
                color: isSelected ? Colors.white : theme.colorScheme.onSurface,
              ),
              label: Text(label),
              selected: isSelected,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: theme.colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : theme.colorScheme.onSurface,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (selected) {
                if (selected) {
                  HapticFeedback.selectionClick();
                  notifier.setAbTestVariant(value);
                }
              },
            );
          }).toList(),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
            borderRadius: BorderRadius.circular(8),
            border: Border.all(
              color: options.abTestVariant != ABTestVariant.control
                  ? AppColors.primaryPurple.withValues(alpha: 0.5)
                  : theme.colorScheme.outlineVariant,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    variants.firstWhere((v) => v.$1 == options.abTestVariant).$3,
                    size: 18,
                    color: options.abTestVariant != ABTestVariant.control
                        ? AppColors.primaryPurple
                        : theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Active: ${variants.firstWhere((v) => v.$1 == options.abTestVariant).$2}',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                      color: options.abTestVariant != ABTestVariant.control
                          ? AppColors.primaryPurple
                          : null,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text(
                variants.firstWhere((v) => v.$1 == options.abTestVariant).$4,
                style: TextStyle(fontSize: 11, color: theme.colorScheme.onSurfaceVariant),
              ),
            ],
          ),
        ),
      ],
    );
  }
}


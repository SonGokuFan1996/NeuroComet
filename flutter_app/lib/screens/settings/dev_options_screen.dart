import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../models/dev_options.dart';
import '../../services/supabase_service.dart';
import '../../widgets/settings/dev_test_sections.dart';

export '../../models/dev_options.dart';

const _kDevOptionsKey = 'dev_options_state';

/// Dev options notifier with SharedPreferences persistence.
///
/// SECURITY: In release mode, always returns hardened defaults.
class DevOptionsNotifier extends Notifier<DevOptions> {
  @override
  DevOptions build() {
    // In release mode, always return defaults (no dev options)
    if (kReleaseMode) return const DevOptions();
    _loadFromDisk();
    return const DevOptions();
  }

  Future<void> _loadFromDisk() async {
    if (kReleaseMode) return;
    try {
      final prefs = await SharedPreferences.getInstance();
      final json = prefs.getString(_kDevOptionsKey);
      if (json != null) {
        final map = jsonDecode(json) as Map<String, dynamic>;
        state = DevOptions.fromMap(map);
      }
    } catch (_) {
      // Corrupted data → keep defaults
    }
  }

  Future<void> _persist() async {
    if (kReleaseMode) return;
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_kDevOptionsKey, jsonEncode(state.toMap()));
    } catch (_) {}
  }

  void _update(DevOptions Function(DevOptions) updater) {
    state = updater(state);
    _persist();
  }

  // ── General ──────────────────────────────────────────
  void setShowDebugOverlay(bool value) => _update((s) => s.copyWith(showDebugOverlay: value));
  void setEnableVerboseLogging(bool value) => _update((s) => s.copyWith(enableVerboseLogging: value));

  // ── Environment ──────────────────────────────────────
  void setEnvironment(DevEnvironmentTarget value) => _update((s) => s.copyWith(environment: value));

  // ── Feature Flags ────────────────────────────────────
  void setEnableNewFeedLayout(bool value) => _update((s) => s.copyWith(enableNewFeedLayout: value));
  void setEnableVideoChat(bool value) => _update((s) => s.copyWith(enableVideoChat: value));
  void setEnableStoryReactions(bool value) => _update((s) => s.copyWith(enableStoryReactions: value));
  void setEnableAdvancedSearch(bool value) => _update((s) => s.copyWith(enableAdvancedSearch: value));
  void setEnableAiSuggestions(bool value) => _update((s) => s.copyWith(enableAiSuggestions: value));

  // ── Content & Safety ─────────────────────────────────
  void setForcedAudience(Audience? value) => _update((s) => s.copyWith(forcedAudience: value, clearForcedAudience: value == null));
  void setBypassAgeVerification(bool value) => _update((s) => s.copyWith(bypassAgeVerification: value));
  void setIsKidsMode(bool value) => _update((s) => s.copyWith(isKidsMode: value));
  void setForcePinSet(bool value) => _update((s) => s.copyWith(forcePinSet: value));
  void setForcePinVerifySuccess(bool value) => _update((s) => s.copyWith(forcePinVerifySuccess: value));
  void setKidsFilterLevel(KidsFilterLevel value) => _update((s) => s.copyWith(kidsFilterLevel: value));

  // ── DM Debug ─────────────────────────────────────────
  void setShowDmDebugOverlay(bool value) => _update((s) => s.copyWith(showDmDebugOverlay: value));
  void setForceSendFailure(bool value) => _update((s) => s.copyWith(forceSendFailure: value));
  void setDisableRateLimit(bool value) => _update((s) => s.copyWith(disableRateLimit: value));
  void setArtificialDelayMs(int value) => _update((s) => s.copyWith(artificialDelayMs: value));
  void setModerationOverride(ModerationOverride value) => _update((s) => s.copyWith(moderationOverride: value));

  // ── Rendering & Performance ──────────────────────────
  void setShowPerformanceOverlay(bool value) => _update((s) => s.copyWith(showPerformanceOverlay: value));
  void setSimulateLoadingError(bool value) => _update((s) => s.copyWith(simulateLoadingError: value));
  void setInfiniteLoading(bool value) => _update((s) => s.copyWith(infiniteLoading: value));
  void setIsFallbackUiEnabled(bool value) => _update((s) => s.copyWith(isFallbackUiEnabled: value));

  // ── Auth ─────────────────────────────────────────────
  void setForceLoggedOut(bool value) => _update((s) => s.copyWith(forceLoggedOut: value));
  void setBypassBiometric(bool value) => _update((s) => s.copyWith(bypassBiometric: value));
  void setForce2FA(bool value) => _update((s) => s.copyWith(force2FA: value));

  // ── Network ──────────────────────────────────────────
  void setSimulateOffline(bool value) => _update((s) => s.copyWith(simulateOffline: value));
  void setNetworkLatencyMs(int value) => _update((s) => s.copyWith(networkLatencyMs: value));

  // ── Feed ─────────────────────────────────────────────
  void setMockPostCount(int value) => _update((s) => s.copyWith(mockPostCount: value));
  void setShowSponsoredPosts(bool value) => _update((s) => s.copyWith(showSponsoredPosts: value));

  // ── Reset ────────────────────────────────────────────
  void resetAll() {
    state = const DevOptions();
    _persist();
  }
}

final devOptionsProvider = NotifierProvider<DevOptionsNotifier, DevOptions>(
  DevOptionsNotifier.new,
);

// ═══════════════════════════════════════════════════════════════
//  SCREEN
// ═══════════════════════════════════════════════════════════════

class DevOptionsScreen extends ConsumerStatefulWidget {
  const DevOptionsScreen({super.key});

  @override
  ConsumerState<DevOptionsScreen> createState() => _DevOptionsScreenState();
}

class _DevOptionsScreenState extends ConsumerState<DevOptionsScreen> {
  int? _postsCount;
  int? _usersCount;
  final _searchController = TextEditingController();
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _refreshCounts();
    _searchController.addListener(() {
      setState(() => _searchQuery = _searchController.text.toLowerCase());
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _refreshCounts() async {
    try {
      final posts = await SupabaseService.getPostCount();
      final users = await SupabaseService.getUserCount();
      if (mounted) {
        setState(() {
          _postsCount = posts >= 0 ? posts : null;
          _usersCount = users >= 0 ? users : null;
        });
      }
    } catch (e) {
      debugPrint('Error refreshing counts: $e');
    }
  }

  void _showResult(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Colors.red : Colors.green,
      ),
    );
  }

  bool _matchesSearch(String title, String searchTerms) {
    if (_searchQuery.isEmpty) return true;
    return title.toLowerCase().contains(_searchQuery) ||
        searchTerms.contains(_searchQuery);
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final theme = Theme.of(context);

    final sections = <_SectionEntry>[
      _SectionEntry('App Info & Diagnostics', 'version build device os memory', Icons.info, const AppInfoDevSection()),
      _SectionEntry('Environment', 'staging production local backend', Icons.cloud, EnvironmentPickerDevSection()),
      _SectionEntry('Feature Flags', 'flags feed video chat story search ai', Icons.flag, const FeatureFlagsDevSection()),
      _SectionEntry('Authentication', 'auth login sign in session user', Icons.lock, const AuthDevSection()),
      _SectionEntry('Content Safety', 'content safety audience kids filter pin parental', Icons.security, const ContentSafetyDevSection()),
      _SectionEntry('DM Delivery', 'dm message overlay send failure delay rate limit moderation', Icons.chat_bubble_outline, const DmDebugDevSection()),
      _SectionEntry('Rendering & Network', 'rendering offline loading error latency fallback network', Icons.network_check, const RenderingNetworkDevSection()),
      _SectionEntry('Auth Overrides', 'auth biometric 2fa logout bypass', Icons.admin_panel_settings, const AuthOverrideDevSection()),
      _SectionEntry('Language & Locale', 'language locale translation i18n', Icons.language, const LocaleDevSection()),
      _SectionEntry('Local Storage', 'storage preferences keys data', Icons.data_usage, const StorageDevSection()),
      _SectionEntry('General Debug', 'debug overlay verbose logging', Icons.bug_report, const GeneralOptionsDevSection()),
      _SectionEntry('Supabase', 'supabase database posts backend', Icons.storage,
        SupabaseDevSection(showResult: _showResult, postsCount: _postsCount, usersCount: _usersCount, onRefresh: _refreshCounts)),
      _SectionEntry('Stress Testing', 'stress test performance widgets animations memory', Icons.speed, const StressTestingDevSection()),
      _SectionEntry('Reset', 'reset clear defaults', Icons.restart_alt, const ResetDevSection()),
    ];

    final filtered = sections.where((s) => _matchesSearch(s.title, s.searchTerms)).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Developer Options', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Header
          _DevHeader(),
          const SizedBox(height: 12),

          // Search bar
          TextField(
            controller: _searchController,
            decoration: InputDecoration(
              hintText: 'Search dev options…',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: _searchQuery.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear),
                      onPressed: () => _searchController.clear(),
                    )
                  : null,
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
          const SizedBox(height: 12),

          // Quick actions
          const _QuickActionsBar(),
          const SizedBox(height: 12),

          // Active overrides indicator
          if (options.activeOverrideCount > 0)
            Card(
              color: theme.colorScheme.tertiaryContainer.withOpacity(0.3),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    Icon(Icons.tune, size: 18, color: theme.colorScheme.tertiary),
                    const SizedBox(width: 8),
                    Text(
                      '${options.activeOverrideCount} override${options.activeOverrideCount != 1 ? "s" : ""} active',
                      style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.tertiary, fontSize: 13),
                    ),
                  ],
                ),
              ),
            ),
          const SizedBox(height: 12),

          // Sections
          ...filtered.map((entry) => Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: _CollapsibleSection(
              title: entry.title,
              icon: entry.icon,
              forceExpanded: _searchQuery.isNotEmpty,
              child: entry.widget,
            ),
          )),

          const SizedBox(height: 40),
        ],
      ),
    );
  }
}

// ─── Supporting types ────────────────────────────────────────

class _SectionEntry {
  final String title;
  final String searchTerms;
  final IconData icon;
  final Widget widget;
  const _SectionEntry(this.title, this.searchTerms, this.icon, this.widget);
}

class _CollapsibleSection extends StatefulWidget {
  final String title;
  final IconData icon;
  final Widget child;
  final bool forceExpanded;

  const _CollapsibleSection({
    required this.title,
    required this.icon,
    required this.child,
    this.forceExpanded = false,
  });

  @override
  State<_CollapsibleSection> createState() => _CollapsibleSectionState();
}

class _CollapsibleSectionState extends State<_CollapsibleSection>
    with SingleTickerProviderStateMixin {
  bool _expanded = false;

  @override
  Widget build(BuildContext context) {
    final isExpanded = _expanded || widget.forceExpanded;
    final theme = Theme.of(context);

    return Card(
      elevation: 0,
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant.withOpacity(0.5)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: () => setState(() => _expanded = !_expanded),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(widget.icon, size: 22, color: theme.colorScheme.primary),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(widget.title, style: const TextStyle(fontWeight: FontWeight.bold)),
                  ),
                  Icon(isExpanded ? Icons.expand_less : Icons.expand_more, color: theme.colorScheme.onSurfaceVariant),
                ],
              ),
            ),
          ),
          ClipRect(
            child: AnimatedSize(
              duration: const Duration(milliseconds: 200),
              curve: Curves.easeInOut,
              alignment: Alignment.topCenter,
              child: isExpanded
                  ? Padding(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                      child: widget.child,
                    )
                  : const SizedBox.shrink(),
            ),
          ),
        ],
      ),
    );
  }
}

class _QuickActionsBar extends ConsumerWidget {
  const _QuickActionsBar();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: [
          ActionChip(
            avatar: const Icon(Icons.restart_alt, size: 16),
            label: const Text('Reset All', style: TextStyle(fontSize: 11)),
            onPressed: () {
              notifier.resetAll();
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('All dev options reset')));
            },
          ),
          const SizedBox(width: 8),
          ActionChip(
            avatar: const Icon(Icons.layers, size: 16),
            label: const Text('Toggle Overlay', style: TextStyle(fontSize: 11)),
            onPressed: () => notifier.setShowDebugOverlay(!options.showDebugOverlay),
          ),
          const SizedBox(width: 8),
          ActionChip(
            avatar: const Icon(Icons.terminal, size: 16),
            label: const Text('Verbose Log', style: TextStyle(fontSize: 11)),
            onPressed: () => notifier.setEnableVerboseLogging(!options.enableVerboseLogging),
          ),
          const SizedBox(width: 8),
          ActionChip(
            avatar: const Icon(Icons.wifi_off, size: 16),
            label: const Text('Toggle Offline', style: TextStyle(fontSize: 11)),
            onPressed: () => notifier.setSimulateOffline(!options.simulateOffline),
          ),
        ],
      ),
    );
  }
}

class _DevHeader extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      color: theme.colorScheme.errorContainer.withOpacity(0.1),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.error.withOpacity(0.5)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(Icons.warning_amber_rounded, color: theme.colorScheme.error),
            const SizedBox(width: 12),
            const Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Developer Mode Active', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                  Text('Internal tools only. Changes persist across restarts.', style: TextStyle(fontSize: 11)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

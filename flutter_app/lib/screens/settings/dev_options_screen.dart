import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../models/dev_options.dart';
import '../../providers/feed_provider.dart';
import '../../providers/messages_provider.dart';
import '../../providers/notifications_provider.dart';
import '../../providers/stories_provider.dart';
import '../../services/device_authorization_service.dart';
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

  // ── Ads Testing ──────────────────────────────────────────
  void setIsAdsPremium(bool value) => _update((s) => s.copyWith(isAdsPremium: value));
  void setForceShowAds(bool value) => _update((s) => s.copyWith(forceShowAds: value));
  void setSimulateAdFailure(bool value) => _update((s) => s.copyWith(simulateAdFailure: value));
  void setUseTestAds(bool value) => _update((s) => s.copyWith(useTestAds: value));
  void setTotalAdsShown(int value) => _update((s) => s.copyWith(totalAdsShown: value));
  void incrementAdsShown() => _update((s) => s.copyWith(totalAdsShown: s.totalAdsShown + 1));

  // ── Environment ──────────────────────────────────────
  void setEnvironment(DevEnvironmentTarget value) => _update((s) => s.copyWith(environment: value));

  // ── Feature Flags ────────────────────────────────────
  void setEnableNewFeedLayout(bool value) => _update((s) => s.copyWith(enableNewFeedLayout: value));
  void setEnableVideoChat(bool value) => _update((s) => s.copyWith(enableVideoChat: value));
  void setEnableStoryReactions(bool value) => _update((s) => s.copyWith(enableStoryReactions: value));
  void setEnableAdvancedSearch(bool value) => _update((s) => s.copyWith(enableAdvancedSearch: value));
  void setEnableAiSuggestions(bool value) => _update((s) => s.copyWith(enableAiSuggestions: value));
  void setEnableHandoff(bool value) => _update((s) => s.copyWith(enableHandoff: value));

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

  // ── A/B Testing ─────────────────────────────────────────
  void setAbTestVariant(ABTestVariant value) => _update((s) => s.copyWith(abTestVariant: value));

  // ── Feedback & Beta Testing ────────────────────────────
  void setBypassFeedbackRateLimit(bool value) => _update((s) => s.copyWith(bypassFeedbackRateLimit: value));
  void setForceFeedbackSubmitFailure(bool value) => _update((s) => s.copyWith(forceFeedbackSubmitFailure: value));

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
  bool _isCheckingAccess = !kDebugMode;
  bool _hasDeveloperAccess = kDebugMode;

  @override
  void initState() {
    super.initState();
    _checkDeveloperAccess();
    _refreshCounts();
    _searchController.addListener(() {
      setState(() => _searchQuery = _searchController.text.toLowerCase());
    });
  }

  Future<void> _checkDeveloperAccess() async {
    if (kDebugMode) return;

    final hasAccess = await DeviceAuthorizationService.canUseDeveloperTools();
    if (!mounted) return;
    setState(() {
      _hasDeveloperAccess = hasAccess;
      _isCheckingAccess = false;
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
    if (_isCheckingAccess) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Developer Options', style: TextStyle(fontWeight: FontWeight.bold)),
        ),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (!_hasDeveloperAccess) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Developer Options', style: TextStyle(fontWeight: FontWeight.bold)),
        ),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: const [
                    Icon(Icons.lock_outline, size: 42),
                    SizedBox(height: 12),
                    Text(
                      'Developer tools are restricted to authorized devices.',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'Use the Android household-authorized build/device pairing to access parity diagnostics and internal testing tools.',
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
    }

    final options = ref.watch(devOptionsProvider);
    final theme = Theme.of(context);

    final sections = <_SectionEntry>[
      _SectionEntry('App Info & Diagnostics', 'version build device os memory', Icons.info, const AppInfoDevSection()),
      _SectionEntry('Notifications Testing', 'notifications push permission unread inject badge alert', Icons.notifications_active, const NotificationsDevSection()),
      _SectionEntry('Profile & Edit Profile', 'profile avatar edit followers following bio user', Icons.person, const ProfileDevSection()),
      _SectionEntry('Feed Interactions', 'feed post create like comment bookmark share delete', Icons.dynamic_feed, const FeedInteractionsDevSection()),
      _SectionEntry('Accessibility & Theme', 'accessibility theme dark light contrast font dyslexic motion scale', Icons.accessibility_new, const AccessibilityThemeDevSection()),
      _SectionEntry('Live Session Lab', 'live session handoff ongoing notification channels regulation', Icons.timer, const LiveSessionLabDevSection()),
      _SectionEntry('Environment', 'staging production local backend', Icons.cloud, EnvironmentPickerDevSection()),
      _SectionEntry('Feature Flags', 'flags feed video chat story search ai handoff', Icons.flag, const FeatureFlagsDevSection()),
      _SectionEntry('Rendering & Network', 'rendering offline loading error latency fallback network', Icons.network_check, const RenderingNetworkDevSection()),
      _SectionEntry('Payments & Subs', 'subscription purchase premium billing transaction restore', Icons.payment, const PaymentsSubscriptionsDevSection()),
      _SectionEntry('Moderation Heuristics', 'moderation heuristics flagged blocked kids filter sanitization content', Icons.gpp_good_outlined, const ModerationHeuristicsDevSection()),
      _SectionEntry('Content Safety', 'content safety audience kids filter pin parental', Icons.security, const ContentSafetyDevSection()),
      _SectionEntry('Privacy & Account', 'privacy account blocked muted data export deletion detox auth', Icons.shield, const PrivacyAccountDevSection()),
      _SectionEntry('Wellbeing & Detox', 'wellbeing detox break reminders quiet hours bedtime positivity', Icons.self_improvement, const WellbeingDetoxDevSection()),
      _SectionEntry('DM Delivery', 'dm message overlay send failure delay rate limit moderation', Icons.chat_bubble_outline, const DmDebugDevSection()),
      _SectionEntry('Neurodivergent Widgets', 'widgets neurodivergent accessible typewriter parallax phone formatting', Icons.widgets, const WidgetsDevSection()),
      _SectionEntry('Image Customization', 'images customization filters stories media', Icons.photo_filter, const ImagesDevSection()),
      _SectionEntry('Explore Page Views', 'explore discover trending layout preview feed explore', Icons.explore, const ExploreViewsDevSection()),
      _SectionEntry('Multi-Media Posts', 'multimedia video audio posts carousel media', Icons.collections, const MultiMediaDevSection()),
      _SectionEntry('Adaptive Navigation', 'navigation adaptive drawer tabs routes responsive', Icons.dashboard, const NavigationDevSection()),
      _SectionEntry('Deep Links & Routing', 'deep link routing navigation test route invalid url path', Icons.link, const DeepLinksRoutingDevSection()),
      _SectionEntry('Dialogs', 'dialogs popup message input choice loading', Icons.chat, const DialogsDevSection()),
      _SectionEntry('Location & Sensors', 'location gps sensors permission status', Icons.sensors, const LocationSensorsDevSection()),
      _SectionEntry('Contact Picker', 'contacts picker permission android 17 privacy picker', Icons.contacts, const ContactPickerDevSection()),
      _SectionEntry('Local Storage', 'storage preferences keys data credentials', Icons.storage, const StorageDevSection()),
      _SectionEntry('Supabase DB Testing', 'supabase database posts users tables test', Icons.cloud_sync, const SupabaseDbTestingDevSection()),
      _SectionEntry('Supabase', 'supabase database posts backend', Icons.cloud_upload,
        SupabaseDevSection(showResult: _showResult, postsCount: _postsCount, usersCount: _usersCount, onRefresh: _refreshCounts)),
      _SectionEntry('Google Ads Testing', 'ads ad interstitial rewarded premium banner', Icons.tv, const GoogleAdsDevSection()),
      _SectionEntry('A/B Testing', 'ab test experiment variant split traffic control liquid glass compact bold', Icons.science, const ABTestingDevSection()),
      _SectionEntry('Biometric, FIDO2 & MFA', 'auth login biometric fido passkey mfa totp backup', Icons.fingerprint, const BiometricFidoDevSection()),
      _SectionEntry('Stories Testing', 'stories story test inject flood text link preview multi-item kitchen sink', Icons.auto_stories, const StoriesTestingDevSection()),
      _SectionEntry('Games', 'games achievements badges', Icons.sports_esports, const GamesDevSection()),
      _SectionEntry('Language', 'language locale translation i18n', Icons.language, const LocaleDevSection()),
      _SectionEntry('Backup & Restore', 'backup restore export import storage drive', Icons.backup_outlined, const BackupRestoreDevSection()),
      _SectionEntry('Feedback & Beta', 'feedback beta bug report feature request offline queue rate limit submission', Icons.feedback, const FeedbackBetaDevSection()),
      _SectionEntry('System Stress Testing', 'stress test performance widgets animations memory', Icons.speed, const StressTestingDevSection()),
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
              color: theme.colorScheme.tertiaryContainer.withValues(alpha: 0.3),
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
  void didUpdateWidget(covariant _CollapsibleSection oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.forceExpanded && !oldWidget.forceExpanded) {
      _expanded = true;
    }
  }

  @override
  Widget build(BuildContext context) {
    final isExpanded = _expanded;
    final theme = Theme.of(context);

    return Card(
      elevation: 0,
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5)),
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
            label: const Text('Reset Overrides', style: TextStyle(fontSize: 11)),
            onPressed: () {
              notifier.resetAll();
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('All settings reset')));
            },
          ),
          const SizedBox(width: 8),
          ActionChip(
            avatar: const Icon(Icons.data_usage, size: 16),
            label: const Text('Reset Mock Data', style: TextStyle(fontSize: 11)),
            onPressed: () async {
              ref.invalidate(feedProvider);
              ref.invalidate(conversationsProvider);
              ref.invalidate(chatMessagesProvider);
              ref.invalidate(storiesProvider);
              await ref.read(notificationsProvider.notifier).resetMockNotifications();
              if (!context.mounted) return;
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Mock data and notifications reset')),
              );
            },
          ),
          const SizedBox(width: 8),
          ActionChip(
            avatar: const Icon(Icons.layers, size: 16),
            label: const Text('Toggle Overlay', style: TextStyle(fontSize: 11)),
            onPressed: () => notifier.setShowDmDebugOverlay(!options.showDmDebugOverlay),
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
      color: theme.colorScheme.errorContainer.withValues(alpha: 0.1),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.error.withValues(alpha: 0.5)),
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

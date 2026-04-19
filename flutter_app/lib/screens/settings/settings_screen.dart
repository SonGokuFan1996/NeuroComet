import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../providers/theme_provider.dart';
import '../../providers/message_delete_mode_provider.dart';
import '../../services/device_authorization_service.dart';
import '../../services/subscription_service.dart';
import '../../services/supabase_service.dart';
import '../../l10n/app_localizations.dart';
import '../../core/theme/app_colors.dart';
import '../../core/constants/app_constants.dart';

/// Premium Settings screen with modern design language
/// Matches the visual style of Notifications and Messages screens
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen>
    with TickerProviderStateMixin {
  static const _devOptionsUnlockedKey = 'dev_options_unlocked';

  // Easter egg state - tap version info 7 times!
  int _easterEggTapCount = 0;
  DateTime _lastTapTime = DateTime.now();
  bool _devOptionsUnlocked = kDebugMode;
  bool _devOptionsAvailable = kDebugMode;
  bool _devAccessChecked = kDebugMode;

  // Premium status (would come from provider in real app)
  bool _isFakePremiumEnabled = false;

  // Animation controllers
  late AnimationController _headerAnimController;
  late Animation<double> _headerFadeAnimation;

  final List<String> _easterEggMessages = const [
    "🧠✨ Your brain is not broken, it's a feature!",
    "🌈 Different minds build different worlds!",
    "🦋 Neurodivergence is evolution's art project!",
    "💫 You're not too much, the world is too little!",
    "🎭 Normal is just a setting on the dryer!",
    "🚀 Your brain has premium features, not bugs!",
    "🌟 Divergent paths lead to undiscovered galaxies!",
  ];

  @override
  void initState() {
    super.initState();
    _loadDevOptionsUnlocked();
    _loadFakePremiumState();
    _headerAnimController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    );
    _headerFadeAnimation = CurvedAnimation(
      parent: _headerAnimController,
      curve: Curves.easeOutCubic,
    );
    _headerAnimController.forward();
  }

  @override
  void dispose() {
    _headerAnimController.dispose();
    super.dispose();
  }

  Future<void> _loadDevOptionsUnlocked() async {
    final prefs = await SharedPreferences.getInstance();
    final hasAccess = kDebugMode
        ? true
        : await DeviceAuthorizationService.canUseDeveloperTools();
    final unlocked = hasAccess && (prefs.getBool(_devOptionsUnlockedKey) ?? false);
    if (!hasAccess) {
      await prefs.remove(_devOptionsUnlockedKey);
    }
    if (!mounted) return;
    setState(() {
      _devOptionsAvailable = hasAccess;
      _devAccessChecked = true;
      _devOptionsUnlocked = unlocked;
    });
  }

  Future<void> _saveDevOptionsUnlocked(bool unlocked) async {
    if (!kDebugMode && !_devOptionsAvailable) return;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_devOptionsUnlockedKey, unlocked);
  }

  Future<void> _loadFakePremiumState() async {
    final isPremium = await SubscriptionService.instance.checkPremiumStatus();
    if (!mounted) return;
    setState(() => _isFakePremiumEnabled = isPremium);
  }

  Future<void> _handleVersionTap() async {
    HapticFeedback.lightImpact();
    if (!_devAccessChecked) return;
    if (!kDebugMode && !_devOptionsAvailable) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Developer tools are restricted to authorized devices.'),
          duration: const Duration(milliseconds: 1200),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
      return;
    }

    final now = DateTime.now();
    if (now.difference(_lastTapTime).inSeconds > 3) {
      _easterEggTapCount = 1;
    } else {
      _easterEggTapCount++;
    }
    _lastTapTime = now;

    if (_easterEggTapCount >= 7) {
      setState(() {
        _devOptionsUnlocked = true;
        _easterEggTapCount = 0;
      });
      _saveDevOptionsUnlocked(true);
      _showEasterEggDialog(context);
    } else if (_easterEggTapCount >= 3) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('${7 - _easterEggTapCount} more taps to unlock...'),
          duration: const Duration(milliseconds: 800),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final theme = Theme.of(context);
    final themeMode = ref.watch(themeModeProvider);
    final highContrast = ref.watch(highContrastProvider);
    final reducedMotion = ref.watch(reducedMotionProvider);
    final deleteMode = ref.watch(messageDeleteModeProvider);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: CustomScrollView(
          physics: const BouncingScrollPhysics(
            parent: AlwaysScrollableScrollPhysics(),
          ),
          slivers: [
            // Modern header matching Notifications style
            SliverToBoxAdapter(
              child: FadeTransition(
                opacity: _headerFadeAnimation,
                child: _SettingsHeader(
                  email: SupabaseService.currentUser?.email,
                  onLogout: () => _showSignOutDialog(context),
                  l10n: l10n,
                ),
              ),
            ),

            // Settings content
            SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  // ═══════════════════════════════════════════════════════════════
                  // ACCOUNT SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.account,
                    icon: Icons.account_circle_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: l10n.editProfile,
                        description: 'View and edit your profile',
                        icon: Icons.person_outline_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => context.push('/edit-profile'),
                      ),
                    ],
                  ),
                  _AccountInfoCard(
                    email: SupabaseService.currentUser?.email ?? 'Not signed in',
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // PREMIUM SUBSCRIPTION SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.subscriptionTitle,
                    icon: Icons.star_rounded,
                  ),
                  if (_isFakePremiumEnabled)
                    _PremiumActiveCard()
                  else
                    _ModernSettingsCard(
                      children: [
                        _ModernSettingsItem(
                          title: l10n.get('goPremium'),
                          description: l10n.get('premiumPricing'),
                          icon: Icons.star_rounded,
                          iconColor: const Color(0xFFFFD700),
                          showGradientIcon: true,
                          onTap: () => context.push('/subscription'),
                        ),
                      ],
                    ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // APPEARANCE & DISPLAY SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.appearance,
                    icon: Icons.palette_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: l10n.themeSettings,
                        description: 'Customize colors and themes',
                        icon: Icons.palette_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => context.push('/settings/neuro-state'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: 'Animation Settings',
                        description: reducedMotion ? 'Animations disabled' : 'Customize animations',
                        icon: Icons.animation_rounded,
                        iconColor: AppColors.secondaryTeal,
                        onTap: () => context.push('/settings/accessibility'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsToggle(
                        title: l10n.darkTheme,
                        description: 'Use dark color scheme',
                        icon: Icons.dark_mode_rounded,
                        iconColor: const Color(0xFF5C6BC0),
                        value: themeMode == ThemeMode.dark,
                        onChanged: (value) {
                          HapticFeedback.selectionClick();
                          ref.read(themeModeProvider.notifier).setThemeMode(
                            value ? ThemeMode.dark : ThemeMode.light,
                          );
                        },
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsToggle(
                        title: 'High Contrast',
                        description: 'Increase color contrast for visibility',
                        icon: Icons.contrast_rounded,
                        iconColor: const Color(0xFFFF7043),
                        value: highContrast,
                        onChanged: (value) {
                          HapticFeedback.selectionClick();
                          ref.read(highContrastProvider.notifier).setHighContrast(value);
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // PRIVACY & SECURITY SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.privacySafety,
                    icon: Icons.lock_outline_rounded,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: l10n.privacySettings,
                        description: 'Control who can see your content',
                        icon: Icons.lock_outline_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => context.push('/settings/privacy'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: l10n.parentalControls,
                        description: 'Set up parental controls',
                        icon: Icons.shield_rounded,
                        iconColor: AppColors.success,
                        onTap: () => context.push('/settings/parental'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: l10n.blockedUsers,
                        description: 'Manage blocked accounts',
                        icon: Icons.block_rounded,
                        iconColor: AppColors.error,
                        onTap: () => context.push('/settings/blocked'),
                      ),
                    ],
                  ),
                  // ═══════════════════════════════════════════════════════════════
                  // BACKUP & STORAGE SECTION — dev testing only
                  // ═══════════════════════════════════════════════════════════════
                  if (kDebugMode) ...[
                    const SizedBox(height: 20),
                    _SettingsSectionHeader(
                      title: 'Backup & Storage (Dev)',
                      icon: Icons.cloud_sync_rounded,
                      subtitle: 'Dev testing only',
                    ),
                    _ModernSettingsCard(
                      children: [
                        _ModernSettingsItem(
                          title: 'Backup & Storage',
                          description: 'Back up and restore your data — dev testing only',
                          icon: Icons.cloud_sync_rounded,
                          iconColor: const Color(0xFF42A5F5),
                          onTap: () => context.push('/settings/backup'),
                        ),
                      ],
                    ),
                  ],
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // NOTIFICATIONS SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.notifications,
                    icon: Icons.notifications_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: l10n.pushNotifications,
                        description: 'Configure notification preferences',
                        icon: Icons.notifications_active_rounded,
                        iconColor: const Color(0xFFFF9800),
                        onTap: () => context.push('/settings/notifications'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // MESSAGES SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.messagesSettings,
                    icon: Icons.chat_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: l10n.messageDeleteMode,
                        description: deleteMode == MessageDeleteMode.swipe
                            ? l10n.swipeToDelete
                            : l10n.longPressToDelete,
                        icon: deleteMode == MessageDeleteMode.swipe
                            ? Icons.swipe_left_rounded
                            : Icons.touch_app_rounded,
                        iconColor: const Color(0xFF42A5F5),
                        onTap: () => _showDeleteModeDialog(context, deleteMode),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // ACCESSIBILITY & WELLBEING SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.accessibility,
                    icon: Icons.accessibility_new_rounded,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Text & Display',
                        description: 'Font size, dyslexia-friendly options',
                        icon: Icons.text_fields_rounded,
                        iconColor: AppColors.secondaryTeal,
                        onTap: () => context.push('/settings/accessibility'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsToggle(
                        title: l10n.reducedMotion,
                        description: 'Minimize animations',
                        icon: Icons.accessibility_new_rounded,
                        iconColor: AppColors.primaryPurple,
                        value: reducedMotion,
                        onChanged: (value) {
                          HapticFeedback.selectionClick();
                          ref.read(animationSettingsProvider.notifier).setReducedMotion(value);
                        },
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: 'Break Reminders',
                        description: 'Set reminders to take breaks',
                        icon: Icons.spa_rounded,
                        iconColor: const Color(0xFF66BB6A),
                        onTap: () => context.push('/settings/wellbeing'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // CONTENT & MEDIA SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: 'Content & Media',
                    icon: Icons.play_circle_outline_rounded,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Content Filters',
                        description: 'Manage content preferences and filters',
                        icon: Icons.tune_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => context.push('/settings/content'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // TIPS & HELP SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: 'Tips & Help',
                    icon: Icons.lightbulb_outline_rounded,
                  ),
                  _ModernTipsCard(),
                  const SizedBox(height: 8),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Replay Tutorial',
                        description: 'Learn how to use NeuroComet',
                        icon: Icons.school_rounded,
                        iconColor: AppColors.secondaryTeal,
                        onTap: () => context.push('/tutorial'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // GAMES SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: l10n.gamesHubTitle,
                    icon: Icons.sports_esports_rounded,
                    subtitle: 'Brain-friendly games for all',
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Play Now',
                        description: 'Games designed for neurodivergent minds',
                        icon: Icons.sports_esports_rounded,
                        iconColor: const Color(0xFFE91E63),
                        onTap: () => context.push('/games'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // FEEDBACK & SUPPORT SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: 'Feedback & Support',
                    icon: Icons.feedback_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Feedback Hub',
                        description: 'Report bugs, request features & share feedback',
                        icon: Icons.feedback_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => context.push('/feedback'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: l10n.helpAndSupport,
                        description: 'Get help and support',
                        icon: Icons.help_outline_rounded,
                        iconColor: AppColors.secondaryTeal,
                        onTap: () => context.push('/help'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),

                  // ═══════════════════════════════════════════════════════════════
                  // LEGAL & ABOUT SECTION
                  // ═══════════════════════════════════════════════════════════════
                  _SettingsSectionHeader(
                    title: 'Legal & About',
                    icon: Icons.shield_outlined,
                  ),
                  _ModernSettingsCard(
                    children: [
                      _ModernSettingsItem(
                        title: 'Privacy Policy',
                        description: 'Read our privacy policy',
                        icon: Icons.privacy_tip_rounded,
                        iconColor: AppColors.primaryPurple,
                        onTap: () => _launchUrl('https://getneurocomet.com/privacy'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: 'Terms of Service',
                        description: 'Read our terms of service',
                        icon: Icons.description_rounded,
                        iconColor: AppColors.secondaryTeal,
                        onTap: () => _launchUrl('https://getneurocomet.com/terms'),
                      ),
                      _ModernSettingsDivider(),
                      _ModernSettingsItem(
                        title: 'Open Source Licenses',
                        description: 'Third-party software licenses',
                        icon: Icons.code_rounded,
                        iconColor: const Color(0xFF66BB6A),
                        onTap: () => _showLicensesDialog(context),
                      ),
                    ],
                  ),

                  // ═══════════════════════════════════════════════════════════════
                  // DEVELOPER OPTIONS
                  // ═══════════════════════════════════════════════════════════════
                  if ((_devOptionsUnlocked || kDebugMode) && _devOptionsAvailable) ...[
                    const SizedBox(height: 20),
                    _SettingsSectionHeader(
                      title: l10n.developerOptions,
                      icon: Icons.build_rounded,
                      subtitle: 'For testing and debugging',
                    ),
                    _ModernSettingsCard(
                      children: [
                        _ModernSettingsToggle(
                          title: 'Fake Premium',
                          description: _isFakePremiumEnabled ? 'Premium features enabled' : 'Test premium features',
                          icon: Icons.star_rounded,
                          iconColor: const Color(0xFFFFD700),
                          value: _isFakePremiumEnabled,
                          onChanged: (value) async {
                            HapticFeedback.selectionClick();
                            if (value) {
                              await SubscriptionService.instance.simulateTestSuccess();
                            } else {
                              SubscriptionService.instance.resetTestPurchase();
                            }
                            if (!mounted) return;
                            setState(() => _isFakePremiumEnabled = value);
                          },
                        ),
                        _ModernSettingsDivider(),
                        _ModernSettingsItem(
                          title: 'Developer Options',
                          description: l10n.debugToolsDesc,
                          icon: Icons.build_rounded,
                          iconColor: AppColors.primaryPurple,
                          onTap: () => context.push('/settings/dev-options'),
                        ),
                      ],
                    ),
                  ],

                  // ═══════════════════════════════════════════════════════════════
                  // SIGN OUT BUTTON
                  // ═══════════════════════════════════════════════════════════════
                  const SizedBox(height: 24),
                  _SignOutButton(onTap: () => _showSignOutDialog(context)),

                  // ═══════════════════════════════════════════════════════════════
                  // APP INFO & VERSION (Easter Egg)
                  // ═══════════════════════════════════════════════════════════════
                  const SizedBox(height: 32),
                  _AppVersionInfo(
                    onTap: () {
                      _handleVersionTap();
                    },
                    tapCount: _easterEggTapCount,
                    l10n: l10n,
                  ),
                  const SizedBox(height: 16),
                  // Extra clearance for bottom navigation bar
                  const SizedBox(height: 100),
                ]),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showEasterEggDialog(BuildContext context) {
    HapticFeedback.heavyImpact();
    final theme = Theme.of(context);
    final randomMessage = (_easterEggMessages..shuffle()).first;

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: Text(
            '🎉 Secret Unlocked!',
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
            textAlign: TextAlign.center,
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                randomMessage,
                style: theme.textTheme.bodyLarge,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                  ),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Text(
                  '🌈 Rainbow Brain theme unlocked!',
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 12),
              Text(
                'Made with 💜 for neurodivergent minds',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("You're awesome! 🌟"),
            ),
          ],
        );
      },
    );
  }

  Future<void> _launchUrl(String url) async {
    HapticFeedback.lightImpact();
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Could not open link'),
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
    }
  }

  void _showLicensesDialog(BuildContext context) {
    HapticFeedback.lightImpact();
    showLicensePage(
      context: context,
      applicationName: context.l10n.appName,
      applicationVersion: AppConstants.appVersion,
    );
  }

  void _showSignOutDialog(BuildContext context) {
    final l10n = context.l10n;

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: Text(l10n.signOut),
          content: Text(l10n.signOutConfirm),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text(l10n.cancel),
            ),
            FilledButton(
              onPressed: () async {
                await SupabaseService.signOut();
                if (!context.mounted) return;
                Navigator.pop(context);
                context.go('/auth');
              },
              style: FilledButton.styleFrom(
                backgroundColor: Theme.of(context).colorScheme.error,
              ),
              child: Text(l10n.signOut),
            ),
          ],
        );
      },
    );
  }

  void _showDeleteModeDialog(BuildContext context, MessageDeleteMode currentMode) {
    final l10n = context.l10n;
    final theme = Theme.of(context);

    showDialog(
      context: context,
      builder: (ctx) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: Text(l10n.deleteMethodLabel),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                l10n.messageDeleteModeDesc,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 16),
              _DeleteModeOption(
                icon: Icons.swipe_left_rounded,
                title: l10n.swipeToDelete,
                description: l10n.swipeToDeleteDesc,
                isSelected: currentMode == MessageDeleteMode.swipe,
                onTap: () {
                  HapticFeedback.selectionClick();
                  ref.read(messageDeleteModeProvider.notifier).setMode(MessageDeleteMode.swipe);
                  Navigator.pop(ctx);
                },
              ),
              const SizedBox(height: 8),
              _DeleteModeOption(
                icon: Icons.touch_app_rounded,
                title: l10n.longPressToDelete,
                description: l10n.longPressToDeleteDesc,
                isSelected: currentMode == MessageDeleteMode.longPress,
                onTap: () {
                  HapticFeedback.selectionClick();
                  ref.read(messageDeleteModeProvider.notifier).setMode(MessageDeleteMode.longPress);
                  Navigator.pop(ctx);
                },
              ),
            ],
          ),
        );
      },
    );
  }
}

/// Radio-style option card for delete mode selection
class _DeleteModeOption extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final bool isSelected;
  final VoidCallback onTap;

  const _DeleteModeOption({
    required this.icon,
    required this.title,
    required this.description,
    required this.isSelected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final primaryColor = theme.colorScheme.primary;

    return Material(
      color: isSelected
          ? primaryColor.withValues(alpha: 0.1)
          : theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: isSelected
                      ? primaryColor.withValues(alpha: 0.15)
                      : theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  icon,
                  color: isSelected ? primaryColor : theme.colorScheme.onSurfaceVariant,
                  size: 22,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                        color: isSelected ? primaryColor : null,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      description,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              Radio<bool>(
                value: true,
                groupValue: isSelected,
                onChanged: (_) => onTap(),
                activeColor: primaryColor,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// MODERN SETTINGS UI COMPONENTS
// ═══════════════════════════════════════════════════════════════

/// Modern header matching Notifications/Messages style
class _SettingsHeader extends StatelessWidget {
  final String? email;
  final VoidCallback onLogout;
  final AppLocalizations l10n;

  const _SettingsHeader({
    this.email,
    required this.onLogout,
    required this.l10n,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.settingsTitle,
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        letterSpacing: -0.5,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Customize your NeuroComet experience ✨',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              _HeaderIconButton(
                icon: Icons.logout_rounded,
                onPressed: onLogout,
                tooltip: 'Sign out',
                isDestructive: true,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

/// Header icon button matching other screens
class _HeaderIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String? tooltip;
  final bool isDestructive;

  const _HeaderIconButton({
    required this.icon,
    required this.onPressed,
    this.tooltip,
    this.isDestructive = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Tooltip(
      message: tooltip ?? '',
      child: Material(
        color: isDestructive
            ? theme.colorScheme.errorContainer.withValues(alpha: 0.5)
            : (isDark ? Colors.white.withValues(alpha: 0.08) : Colors.black.withValues(alpha: 0.05)),
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: () {
            HapticFeedback.lightImpact();
            onPressed();
          },
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.all(10),
            child: Icon(
              icon,
              size: 22,
              color: isDestructive
                  ? theme.colorScheme.error
                  : theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ),
      ),
    );
  }
}

class _SettingsSectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;
  final String? subtitle;

  const _SettingsSectionHeader({
    required this.title,
    required this.icon,
    this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 10, top: 4, left: 4),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: AppColors.primaryPurple.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, size: 16, color: AppColors.primaryPurple),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: theme.colorScheme.onSurface,
                    letterSpacing: 0.3,
                  ),
                ),
                if (subtitle != null)
                  Text(
                    subtitle!,
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
}

class _ModernSettingsCard extends StatelessWidget {
  final List<Widget> children;

  const _ModernSettingsCard({required this.children});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      decoration: BoxDecoration(
        color: isDark ? Colors.white.withValues(alpha: 0.05) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isDark
              ? Colors.white.withValues(alpha: 0.08)
              : Colors.black.withValues(alpha: 0.06),
          width: 1,
        ),
        boxShadow: isDark
            ? null
            : [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.04),
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(20),
        child: Column(children: children),
      ),
    );
  }
}

class _ModernSettingsItem extends StatelessWidget {
  final String title;
  final String? description;
  final IconData icon;
  final Color iconColor;
  final VoidCallback? onTap;
  final bool showGradientIcon;

  const _ModernSettingsItem({
    required this.title,
    this.description,
    required this.icon,
    required this.iconColor,
    this.onTap,
    this.showGradientIcon = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () {
          HapticFeedback.lightImpact();
          onTap?.call();
        },
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          child: Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: iconColor.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(12),
                  gradient: showGradientIcon
                      ? LinearGradient(
                          colors: [
                            const Color(0xFFD4AF37).withValues(alpha: 0.2),
                            const Color(0xFFE8C547).withValues(alpha: 0.2),
                          ],
                        )
                      : null,
                ),
                child: Icon(icon, color: iconColor, size: 22),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (description != null)
                      Text(
                        description!,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                  ],
                ),
              ),
              Icon(
                Icons.chevron_right_rounded,
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ModernSettingsToggle extends StatelessWidget {
  final String title;
  final String? description;
  final IconData icon;
  final Color iconColor;
  final bool value;
  final ValueChanged<bool> onChanged;

  const _ModernSettingsToggle({
    required this.title,
    this.description,
    required this.icon,
    required this.iconColor,
    required this.value,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () => onChanged(!value),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: iconColor.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: iconColor, size: 22),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (description != null)
                      Text(
                        description!,
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
                activeColor: AppColors.primaryPurple,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ModernSettingsDivider extends StatelessWidget {
  const _ModernSettingsDivider();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(left: 72),
      child: Divider(
        height: 1,
        color: theme.colorScheme.outlineVariant.withValues(alpha: 0.4),
      ),
    );
  }
}

/// Modern account info card
class _AccountInfoCard extends StatelessWidget {
  final String email;

  const _AccountInfoCard({required this.email});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      margin: const EdgeInsets.only(top: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: isDark
              ? [AppColors.primaryPurple.withValues(alpha: 0.15), AppColors.secondaryTeal.withValues(alpha: 0.1)]
              : [AppColors.primaryPurple.withValues(alpha: 0.08), AppColors.secondaryTeal.withValues(alpha: 0.05)],
        ),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: AppColors.primaryPurple.withValues(alpha: 0.2),
          width: 1,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
              ),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(
              Icons.person_rounded,
              color: Colors.white,
              size: 26,
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  email,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                Row(
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      decoration: const BoxDecoration(
                        color: AppColors.success,
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      'Signed in',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// Modern premium active card with golden gradient
class _PremiumActiveCard extends StatelessWidget {
  const _PremiumActiveCard();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: isDark
              ? [const Color(0xFF2D2510), const Color(0xFF1A1508)]
              : [const Color(0xFFFFF8E1), const Color(0xFFFFF3CD)],
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: const Color(0xFFD4AF37).withValues(alpha: 0.4),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFFD4AF37).withValues(alpha: 0.2),
            blurRadius: 15,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 50,
            height: 50,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFD4AF37), Color(0xFFE8C547)],
              ),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.star_rounded,
              color: Color(0xFF1A1A1A),
              size: 28,
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Premium Active',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFFD4AF37),
                  ),
                ),
                Text(
                  'Thank you for supporting NeuroComet!',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          const Icon(
            Icons.verified_rounded,
            color: Color(0xFF4CAF50),
            size: 28,
          ),
        ],
      ),
    );
  }
}

/// Modern tips card with expandable content
class _ModernTipsCard extends StatefulWidget {
  const _ModernTipsCard();

  @override
  State<_ModernTipsCard> createState() => _ModernTipsCardState();
}

class _ModernTipsCardState extends State<_ModernTipsCard>
    with SingleTickerProviderStateMixin {
  bool _expanded = false;
  late AnimationController _controller;
  late Animation<double> _rotationAnimation;

  static const List<_TipItem> _tips = [
    _TipItem('🔄', 'Animation Toggle', 'Disable animations for a calmer experience'),
    _TipItem('🦄', 'Rainbow Brain', 'Tap version info 7 times for a secret!'),
    _TipItem('🌙', 'Eye Strain', 'Dark mode reduces blue light exposure'),
    _TipItem('📖', 'Readability', 'OpenDyslexic font helps with reading'),
    _TipItem('🧘', 'Calm Mode', 'Ocean Calm theme for relaxation'),
    _TipItem('⚡', 'Focus Mode', 'Hyperfocus Neon for concentration'),
    _TipItem('👶', 'Kids Mode', 'Safe browsing for younger users'),
    _TipItem('🎄', 'Holiday Themes', 'Seasonal themes unlock automatically'),
    _TipItem('🎨', 'Custom Themes', 'Unlock more themes as you use the app'),
    _TipItem('🔤', 'Font Options', 'Multiple font sizes and styles available'),
  ];

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );
    _rotationAnimation = Tween<double>(begin: 0, end: 0.5).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggleExpand() {
    HapticFeedback.selectionClick();
    setState(() {
      _expanded = !_expanded;
      if (_expanded) {
        _controller.forward();
      } else {
        _controller.reverse();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      decoration: BoxDecoration(
        color: isDark ? Colors.white.withValues(alpha: 0.05) : Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isDark
              ? Colors.white.withValues(alpha: 0.08)
              : Colors.black.withValues(alpha: 0.06),
          width: 1,
        ),
        boxShadow: isDark
            ? null
            : [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.04),
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
      ),
      child: Column(
        children: [
          // Header
          Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: _toggleExpand,
              borderRadius: BorderRadius.circular(20),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Container(
                      width: 42,
                      height: 42,
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFB300).withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Center(
                        child: Text('💡', style: TextStyle(fontSize: 22)),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Tips & Tricks',
                            style: theme.textTheme.bodyLarge?.copyWith(
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          Text(
                            _expanded
                                ? 'Tap to collapse'
                                : 'Tap to see ${_tips.length} tips',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    ),
                    RotationTransition(
                      turns: _rotationAnimation,
                      child: Icon(
                        Icons.expand_more_rounded,
                        color: AppColors.primaryPurple,
                        size: 26,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),

          // Tips content
          AnimatedCrossFade(
            firstChild: const SizedBox.shrink(),
            secondChild: Column(
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Divider(
                    color: theme.colorScheme.outlineVariant.withValues(alpha: 0.4),
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
                  child: Column(
                    children: _tips
                        .map((tip) => _TipRow(tip: tip))
                        .toList(),
                  ),
                ),
              ],
            ),
            crossFadeState: _expanded
                ? CrossFadeState.showSecond
                : CrossFadeState.showFirst,
            duration: const Duration(milliseconds: 200),
          ),
        ],
      ),
    );
  }
}

class _TipRow extends StatelessWidget {
  final _TipItem tip;

  const _TipRow({required this.tip});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(tip.emoji, style: const TextStyle(fontSize: 20)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  tip.title,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  tip.description,
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
}

class _TipItem {
  final String emoji;
  final String title;
  final String description;

  const _TipItem(this.emoji, this.title, this.description);
}

/// Modern sign out button
class _SignOutButton extends StatelessWidget {
  final VoidCallback onTap;

  const _SignOutButton({required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: theme.colorScheme.error.withValues(alpha: 0.5),
          width: 1.5,
        ),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            HapticFeedback.mediumImpact();
            onTap();
          },
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.logout_rounded,
                  color: theme.colorScheme.error,
                  size: 20,
                ),
                const SizedBox(width: 10),
                Text(
                  'Sign Out',
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: theme.colorScheme.error,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// App version info with easter egg support
class _AppVersionInfo extends StatelessWidget {
  final VoidCallback onTap;
  final int tapCount;
  final AppLocalizations l10n;

  const _AppVersionInfo({
    required this.onTap,
    required this.tapCount,
    required this.l10n,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Text(
            l10n.appName,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Version ${AppConstants.appVersion}',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
            ),
          ),
          if (tapCount >= 3 && tapCount < 7)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(
                '${7 - tapCount} more taps...',
                style: theme.textTheme.labelSmall?.copyWith(
                  color: AppColors.primaryPurple,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
        ],
      ),
    );
  }
}

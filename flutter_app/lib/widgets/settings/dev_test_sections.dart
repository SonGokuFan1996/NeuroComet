import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/theme/app_colors.dart';
import '../../models/backup_metadata.dart';
import '../../models/conversation.dart';
import '../../models/post.dart' as post_models;
import '../../models/story.dart' as story_models;
import '../../providers/backup_provider.dart';
import '../../providers/stories_provider.dart';
import '../../providers/theme_provider.dart';
import '../../screens/settings/dev_options_screen.dart';
import '../../services/content_filtering_service.dart' as content_filtering;
import '../../services/contacts_picker_service.dart';
import '../../services/device_authorization_service.dart';
import '../../services/badge_service.dart';
import '../../services/notification_channels_service.dart';
import '../../services/subscription_service.dart';
import '../../services/app_services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../providers/feed_provider.dart';
import '../../providers/notifications_provider.dart';
import '../../providers/profile_provider.dart';
import '../../models/notification.dart';
import '../../screens/settings/feedback_screen.dart' show flushPendingFeedback;
import '../../utils/responsive.dart';
import 'nd_widget_dashboard.dart';
import '../../services/google_ads_service.dart';
import '../../services/supabase_service.dart';
import '../../services/last_tab_service.dart';
import '../../utils/phone_number_formatter.dart';
import '../../utils/stress_test_utils.dart';
import '../../widgets/common/neuro_parallax.dart';
import '../../widgets/common/typewriter_text.dart';
import '../../providers/messages_provider.dart';
import '../../services/webrtc_call_service.dart';

const _feedTabRoute = '/';
const _shellTabRoutes = <String>{
  _feedTabRoute,
  '/explore',
  '/messages',
  '/notifications',
  '/settings',
};

const _requiredSupabaseTables = <String>[
  'users',
  'posts',
  'post_likes',
  'post_comments',
  'conversations',
  'conversation_participants',
  'dm_messages',
  'profiles',
  'follows',
  'blocked_users',
  'muted_users',
  'bookmarks',
  'notifications',
  'reports',
  'call_signals',
  'call_history',
  'stories',
];

String _normalizeDevRoute(String route) {
  final trimmed = route.trim();
  if (trimmed.isEmpty) return trimmed;
  final normalized = trimmed.startsWith('/') ? trimmed : '/$trimmed';
  if (normalized == '/feed') return _feedTabRoute;
  return normalized;
}

Future<void> _openDevRoute(BuildContext context, String route) async {
  final normalizedRoute = _normalizeDevRoute(route);
  if (normalizedRoute.isEmpty || !context.mounted) return;

  if (normalizedRoute == '/active-call') {
    await _openActiveCallPreview(context);
    return;
  }

  if (_shellTabRoutes.contains(normalizedRoute)) {
    LastTabService.save(normalizedRoute);
    if (context.mounted) {
      context.go(normalizedRoute);
    }
    return;
  }

  context.push(normalizedRoute);
}

String _navigationModeLabel(Responsive responsive) {
  if (!kIsWeb || !responsive.useNavigationRail) {
    return 'BOTTOM_NAV';
  }
  if (responsive.isDesktop) {
    return responsive.showExtendedNavRail ? 'EXTENDED_SIDEBAR' : 'NAV_RAIL';
  }
  return 'NAV_RAIL';
}

Future<void> _openActiveCallPreview(
  BuildContext context, {
  CallType callType = CallType.video,
}) async {
  await WebRTCCallService.instance.prepareDebugPreviewCall(
    recipientName: callType == CallType.video ? 'Taylor Kim' : 'Sam Rivera',
    recipientAvatar: callType == CallType.video
        ? 'https://i.pravatar.cc/150?u=flutter_dev_preview_taylor'
        : 'https://i.pravatar.cc/150?u=flutter_dev_preview_sam',
    callType: callType,
    initialState: CallState.connected,
    initialDurationSeconds: callType == CallType.video ? 94 : 38,
  );
  if (context.mounted) {
    context.push('/active-call');
  }
}

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

Conversation _buildDevFocusCompanionConversation() {
  final now = DateTime.now();
  return Conversation(
    id: 'dev_focus_companion',
    displayName: 'Focus Companion',
    avatarUrl: 'https://i.pravatar.cc/150?u=focus_companion',
    lastMessage: 'Want to do a 20-minute body-doubling sprint together?',
    lastMessageAt: now,
    unreadCount: 2,
    isOnline: true,
    isPinned: true,
    isPrimary: true,
    participantId: 'dev_focus_companion',
  );
}

List<Message> _buildDevFocusCompanionMessages(String conversationId) {
  final now = DateTime.now();
  return [
    Message(
      id: 'dev_focus_companion_1',
      conversationId: conversationId,
      senderId: 'dev_focus_companion',
      content: 'I made a tiny “start ritual” playlist for work transitions.',
      createdAt: now.subtract(const Duration(minutes: 18)),
      status: MessageStatus.read,
    ),
    Message(
      id: 'dev_focus_companion_2',
      conversationId: conversationId,
      senderId: 'current_user',
      content:
          'Please share it. Transitioning into focus mode is the hardest part for me.',
      createdAt: now.subtract(const Duration(minutes: 15)),
      status: MessageStatus.read,
    ),
    Message(
      id: 'dev_focus_companion_3',
      conversationId: conversationId,
      senderId: 'dev_focus_companion',
      content: 'Want to do a 20-minute body-doubling sprint together?',
      createdAt: now.subtract(const Duration(minutes: 2)),
      status: MessageStatus.delivered,
    ),
  ];
}

Conversation _buildDevGroupLabConversation() {
  final now = DateTime.now();
  return Conversation(
    id: 'dev_group_lab',
    displayName: 'Late-Night Focus Lab 🌙',
    avatarUrl: 'https://i.pravatar.cc/150?u=late_night_focus_lab',
    lastMessage: 'Robin: dropping a low-stim co-working room link here ✨',
    lastMessageAt: now,
    unreadCount: 4,
    isGroup: true,
    isPrimary: true,
    participantId: 'dev_group_lab',
    participantIds: const [
      'current_user',
      'dev_robin',
      'dev_jules',
      'dev_mika',
    ],
    groupName: 'Late-Night Focus Lab 🌙',
    memberNames: const ['You', 'Robin Lee', 'Jules Park', 'Mika Santos'],
  );
}

List<Message> _buildDevGroupLabMessages(String conversationId) {
  final now = DateTime.now();
  return [
    Message(
      id: 'dev_group_lab_1',
      conversationId: conversationId,
      senderId: 'dev_robin',
      content: 'Check-in thread: what are we trying to finish before bed?',
      createdAt: now.subtract(const Duration(minutes: 24)),
      status: MessageStatus.read,
    ),
    Message(
      id: 'dev_group_lab_2',
      conversationId: conversationId,
      senderId: 'current_user',
      content:
          'Just one small admin task. Keeping the bar intentionally low tonight 💜',
      createdAt: now.subtract(const Duration(minutes: 20)),
      status: MessageStatus.read,
    ),
    Message(
      id: 'dev_group_lab_3',
      conversationId: conversationId,
      senderId: 'dev_jules',
      content:
          'Love that. “Small enough to start” is my entire strategy this week.',
      createdAt: now.subtract(const Duration(minutes: 12)),
      status: MessageStatus.read,
      reactions: const [MessageReaction(emoji: '💯', userId: 'dev_robin')],
    ),
    Message(
      id: 'dev_group_lab_4',
      conversationId: conversationId,
      senderId: 'dev_robin',
      content: 'Robin: dropping a low-stim co-working room link here ✨',
      createdAt: now.subtract(const Duration(minutes: 1)),
      status: MessageStatus.delivered,
    ),
  ];
}

post_models.Post _buildDevLocationTaggedPost() {
  final now = DateTime.now();
  return post_models.Post(
    id: 'dev_location_post',
    authorId: 'dev_creator',
    authorName: 'Dev Scenario Bot',
    authorAvatarUrl: 'https://i.pravatar.cc/150?u=dev_scenario_bot',
    content:
        'Testing a location-tagged post: this sensory-friendly café has dim lighting, quiet corners, and predictable seating. Perfect for low-overwhelm meetups.',
    likeCount: 18,
    commentCount: 4,
    shareCount: 2,
    isLiked: false,
    isBookmarked: false,
    tags: const ['location', 'sensory', 'meetup'],
    category: 'community',
    tone: 'supportive',
    locationTag: 'Portland, OR • Quiet Current Café',
    createdAt: now,
    updatedAt: now,
  );
}

post_models.Post _buildDevLowStimCheckInPost() {
  final now = DateTime.now();
  return post_models.Post(
    id: 'dev_low_stim_post',
    authorId: 'current_user',
    authorName: 'You',
    content:
        'Low-stimulation check-in post for feed QA: soft colors, short copy, gentle encouragement, and no location tag on this one.',
    likeCount: 7,
    commentCount: 1,
    shareCount: 0,
    isLiked: false,
    isBookmarked: true,
    tags: const ['check-in', 'low-stim'],
    category: 'wellbeing',
    tone: 'gentle',
    createdAt: now.subtract(const Duration(minutes: 1)),
    updatedAt: now.subtract(const Duration(minutes: 1)),
  );
}

class GoogleAdsDevSection extends ConsumerWidget {
  const GoogleAdsDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final adsService = GoogleAdsService();
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    // Sync dev options to service
    adsService.updateDevOptions(
      forceShowAds: options.forceShowAds,
      simulateAdFailure: options.simulateAdFailure,
      useTestAds: options.useTestAds,
      isAdsPremium: options.isAdsPremium,
    );

    final adsEnabled = adsService.adsEnabled;
    final isPremium = adsService.isPremium;

    return DevTestCard(
      title: 'Google Ads Testing',
      icon: Icons.tv,
      children: [
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: isPremium
                ? Colors.green.withValues(alpha: 0.1)
                : adsEnabled
                ? Colors.orange.withValues(alpha: 0.1)
                : Colors.grey.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    isPremium
                        ? '🌟 Premium User'
                        : adsEnabled
                        ? '📺 Ads Enabled'
                        : '🚫 Ads Disabled',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                  Text(
                    'Total ads shown: ${options.totalAdsShown}',
                    style: TextStyle(
                      fontSize: 12,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
              if (adsService.isLoading)
                const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            _AdStatusChip(
              label: 'Interstitial',
              isLoaded: adsService.interstitialLoaded,
            ),
            _AdStatusChip(
              label: 'Rewarded',
              isLoaded: adsService.rewardedLoaded,
            ),
          ],
        ),
        const SizedBox(height: 12),
        SwitchListTile(
          title: const Text('Simulate Premium', style: TextStyle(fontSize: 14)),
          subtitle: const Text(
            'Pretend user has sub',
            style: TextStyle(fontSize: 11),
          ),
          value: options.isAdsPremium,
          onChanged: (v) {
            notifier.setIsAdsPremium(v);
            if (v) {
              SubscriptionService.instance.simulateTestSuccess();
            } else {
              SubscriptionService.instance.resetTestPurchase();
            }
          },
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        SwitchListTile(
          title: const Text('Force Show Ads', style: TextStyle(fontSize: 14)),
          subtitle: const Text(
            'Ignore premium status',
            style: TextStyle(fontSize: 11),
          ),
          value: options.forceShowAds,
          onChanged: notifier.setForceShowAds,
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        SwitchListTile(
          title: const Text(
            'Simulate Ad Failure',
            style: TextStyle(fontSize: 14),
          ),
          value: options.simulateAdFailure,
          onChanged: notifier.setSimulateAdFailure,
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ElevatedButton(
                onPressed: () {
                  adsService.forceLoadAllAds();
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Ads load requested')),
                  );
                },
                child: const Text('Load All'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton(
                onPressed: () {
                  notifier.setTotalAdsShown(0);
                },
                child: const Text('Reset Count'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: adsService.interstitialLoaded
                    ? () {
                        adsService.showInterstitialAd();
                        notifier.incrementAdsShown();
                      }
                    : null,
                icon: const Icon(Icons.play_circle_outline, size: 18),
                label: const Text('Show Interstitial'),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class _AdStatusChip extends StatelessWidget {
  final String label;
  final bool isLoaded;

  const _AdStatusChip({required this.label, required this.isLoaded});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: isLoaded
            ? Colors.green.withValues(alpha: 0.15)
            : Colors.grey.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(
              color: isLoaded ? Colors.green : Colors.grey,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
          ),
        ],
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
      title: 'Supabase',
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
                  final message =
                      (res['message'] as String?) ?? 'Unknown result';
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
                  final message =
                      (res['message'] as String?) ?? 'Unknown result';
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
          style: theme.textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.bold,
          ),
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
      title: 'Authentication',
      icon: Icons.lock_person,
      children: [
        if (user != null) ...[
          Text(
            'Logged in: ${user.email}',
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          Text('ID: ${user.id}', style: const TextStyle(fontSize: 10)),
          const SizedBox(height: 12),
          ElevatedButton.icon(
            onPressed: () async {
              await SupabaseService.signOut();
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Signed out successfully')),
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
                      email: 'dev@getneurocomet.com',
                      password: 'devtest123',
                    );
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text(
                            'Sign-in attempted with dev credentials',
                          ),
                        ),
                      );
                    }
                  } catch (e) {
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            'Auth error: ${e.toString().split('\n').first}',
                          ),
                        ),
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
                    const SnackBar(
                      content: Text(
                        'Navigate to login screen for full authentication',
                      ),
                    ),
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
      title: 'Language',
      icon: Icons.language,
      children: [
        Text(
          'Current: ${currentLocale?.languageCode ?? 'System default (en)'}',
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          children: languages.map((lang) {
            final isSelected =
                currentLocale?.languageCode == lang['code'] ||
                (currentLocale == null && lang['code'] == 'en');

            return FilterChip(
              label: Text(lang['name']!),
              selected: isSelected,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: Theme.of(
                context,
              ).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: isSelected
                    ? Colors.white
                    : Theme.of(context).colorScheme.onSurface,
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
        const Text(
          'Translation Progress',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
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
                    Text(
                      item['name'] as String,
                      style: const TextStyle(fontSize: 12),
                    ),
                    Text(
                      '${(percent * 100).toStringAsFixed(1)}% ($count/$total)',
                      style: const TextStyle(fontSize: 10),
                    ),
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
      title: 'Local Storage',
      icon: Icons.data_usage,
      children: [
        const Text(
          'Manage app preferences and cached data.',
          style: TextStyle(fontSize: 12),
        ),
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
                      children: keys
                          .map(
                            (k) => ListTile(
                              title: Text(
                                k,
                                style: const TextStyle(fontSize: 12),
                              ),
                              subtitle: Text(
                                prefs.get(k).toString(),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(fontSize: 10),
                              ),
                              trailing: IconButton(
                                icon: const Icon(
                                  Icons.delete_outline,
                                  size: 18,
                                ),
                                onPressed: () async {
                                  await prefs.remove(k);
                                  if (context.mounted) Navigator.pop(context);
                                },
                              ),
                            ),
                          )
                          .toList(),
                    ),
                  ),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('Close'),
                    ),
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
                const SnackBar(content: Text('All local storage cleared')),
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
      title: 'Content Safety',
      icon: Icons.security,
      children: [
        DropdownButtonFormField<Audience?>(
          value: options.forcedAudience,
          decoration: const InputDecoration(
            labelText: 'Force Audience Content',
          ),
          onChanged: notifier.setForcedAudience,
          items: <DropdownMenuItem<Audience?>>[
            const DropdownMenuItem<Audience?>(
              value: null,
              child: Text("None (User Selected)"),
            ),
            ...Audience.values.map(
              (a) => DropdownMenuItem<Audience?>(value: a, child: Text(a.name)),
            ),
          ],
        ),
        const SizedBox(height: 8),
        DropdownButtonFormField<KidsFilterLevel>(
          value: options.kidsFilterLevel,
          decoration: const InputDecoration(labelText: 'Kids Filter Level'),
          onChanged: (v) {
            if (v != null) notifier.setKidsFilterLevel(v);
          },
          items: KidsFilterLevel.values
              .map(
                (l) => DropdownMenuItem<KidsFilterLevel>(
                  value: l,
                  child: Text(l.name),
                ),
              )
              .toList(),
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
        const SizedBox(height: 8),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Active Effects',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12),
              ),
              const SizedBox(height: 4),
              Text(
                '• Feed filtering: ${options.forcedAudience != null ? "forced to ${options.forcedAudience!.name}" : "user default"}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                '• Kids mode: ${options.isKidsMode ? "ON – mature content hidden from feed" : "OFF"}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                '• Age verification: ${options.bypassAgeVerification ? "BYPASSED" : "normal"}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                '• Parental PIN: ${options.forcePinSet ? "forced as set (0000)" : "normal"}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                '• PIN verify: ${options.forcePinVerifySuccess ? "auto-passes" : "normal"}',
                style: const TextStyle(fontSize: 11),
              ),
            ],
          ),
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
    final conversationsAsync = ref.watch(conversationsProvider);
    final totalConversations = conversationsAsync.value?.length ?? 0;
    final devConversationCount =
        conversationsAsync.value
            ?.where((conv) => conv.id.startsWith('dev_'))
            .length ??
        0;

    return DevTestCard(
      title: 'DM Delivery',
      icon: Icons.chat_bubble_outline,
      children: [
        const Text(
          'Control direct message behavior for testing edge cases.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 8),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Text(
            'Conversations: $totalConversations  •  Dev-seeded: $devConversationCount',
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
          ),
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
        const Text(
          'Moderation Override',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 4),
        Wrap(
          spacing: 8,
          children: ModerationOverride.values.map((override) {
            return FilterChip(
              label: Text(override.name),
              selected: options.moderationOverride == override,
              selectedColor: AppColors.primaryPurple,
              backgroundColor: Theme.of(
                context,
              ).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: options.moderationOverride == override
                    ? Colors.white
                    : Theme.of(context).colorScheme.onSurface,
                fontWeight: options.moderationOverride == override
                    ? FontWeight.bold
                    : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (_) => notifier.setModerationOverride(override),
            );
          }).toList(),
        ),
        const SizedBox(height: 12),
        const DevSectionHeader(
          title: 'Scenario Seeders',
          icon: Icons.science_outlined,
        ),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () {
                final conversation = _buildDevFocusCompanionConversation();
                ref
                    .read(conversationsProvider.notifier)
                    .injectDevConversation(
                      conversation,
                      messages: _buildDevFocusCompanionMessages(
                        conversation.id,
                      ),
                    );
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Seeded focus-companion DM scenario'),
                  ),
                );
                context.push('/chat/${conversation.id}');
              },
              icon: const Icon(Icons.person_add_alt_1, size: 16),
              label: const Text('Seed 1:1 DM', style: TextStyle(fontSize: 12)),
            ),
            ElevatedButton.icon(
              onPressed: () {
                final conversation = _buildDevGroupLabConversation();
                ref
                    .read(conversationsProvider.notifier)
                    .injectDevConversation(
                      conversation,
                      messages: _buildDevGroupLabMessages(conversation.id),
                    );
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Seeded group-chat lab scenario'),
                  ),
                );
                context.push('/chat/${conversation.id}');
              },
              icon: const Icon(Icons.group_add, size: 16),
              label: const Text(
                'Seed Group Chat',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref
                    .read(conversationsProvider.notifier)
                    .clearDevInjectedConversations();
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Cleared dev-seeded chat scenarios'),
                  ),
                );
              },
              icon: const Icon(Icons.delete_sweep_outlined, size: 16),
              label: const Text(
                'Clear Seeded Chats',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/messages'),
              icon: const Icon(Icons.open_in_new, size: 16),
              label: const Text(
                'Open Messages',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class RenderingNetworkDevSection extends ConsumerStatefulWidget {
  const RenderingNetworkDevSection({super.key});

  @override
  ConsumerState<RenderingNetworkDevSection> createState() =>
      _RenderingNetworkDevSectionState();
}

class _RenderingNetworkDevSectionState
    extends ConsumerState<RenderingNetworkDevSection> {
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

class BiometricFidoDevSection extends ConsumerStatefulWidget {
  const BiometricFidoDevSection({super.key});

  @override
  ConsumerState<BiometricFidoDevSection> createState() =>
      _BiometricFidoDevSectionState();
}

class _BiometricFidoDevSectionState
    extends ConsumerState<BiometricFidoDevSection> {
  bool _biometricEnabled = false;
  bool _fido2Enabled = false;
  bool _totpEnabled = false;
  int _backupCodesRemaining = 5;
  String? _authTestResult;
  final _totpController = TextEditingController();
  List<String> _generatedBackupCodes = [];

  @override
  void dispose() {
    _totpController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DevTestCard(
      title: 'Biometric, FIDO2 & MFA',
      icon: Icons.fingerprint,
      children: [
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: theme.colorScheme.surfaceContainerHighest.withValues(
              alpha: 0.3,
            ),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Hardware Detected: ✅ Yes',
                style: TextStyle(fontSize: 12),
              ),
              const Text(
                'Biometric Status: Strong',
                style: TextStyle(fontSize: 12),
              ),
              const Text('FIDO2 Supported: ✅', style: TextStyle(fontSize: 12)),
              const Divider(height: 16),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _MfaStatusIcon(label: 'Biometric', active: _biometricEnabled),
                  _MfaStatusIcon(label: 'FIDO2', active: _fido2Enabled),
                  _MfaStatusIcon(label: 'TOTP', active: _totpEnabled),
                  Column(
                    children: [
                      Text(
                        '$_backupCodesRemaining',
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                      const Text('Backup', style: TextStyle(fontSize: 10)),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        const Text(
          'Biometric Authentication',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        SwitchListTile(
          title: const Text(
            'Biometric Enabled',
            style: TextStyle(fontSize: 14),
          ),
          subtitle: const Text(
            'Allow fingerprint/face unlock',
            style: TextStyle(fontSize: 11),
          ),
          value: _biometricEnabled,
          onChanged: (v) => setState(() => _biometricEnabled = v),
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        ElevatedButton.icon(
          onPressed: _biometricEnabled
              ? () {
                  // Feature flag: bypass biometric auth
                  if (ref.read(devOptionsProvider).bypassBiometric) {
                    setState(
                      () => _authTestResult =
                          '✅ Biometric auth bypassed (dev flag)',
                    );
                    return;
                  }
                  showDialog(
                    context: context,
                    barrierDismissible: false,
                    builder: (ctx) => AlertDialog(
                      title: const Row(
                        children: [
                          Icon(
                            Icons.fingerprint,
                            size: 28,
                            color: Colors.deepPurple,
                          ),
                          SizedBox(width: 12),
                          Text('Biometric Authentication'),
                        ],
                      ),
                      content: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            width: 80,
                            height: 80,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.deepPurple.withValues(alpha: 0.1),
                            ),
                            child: const Icon(
                              Icons.fingerprint,
                              size: 48,
                              color: Colors.deepPurple,
                            ),
                          ),
                          const SizedBox(height: 16),
                          const Text(
                            'Confirm your identity',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Touch the fingerprint sensor or use face recognition',
                            textAlign: TextAlign.center,
                            style: TextStyle(fontSize: 12, color: Colors.grey),
                          ),
                        ],
                      ),
                      actions: [
                        TextButton(
                          onPressed: () {
                            Navigator.pop(ctx);
                            setState(
                              () => _authTestResult =
                                  '❌ Biometric auth cancelled',
                            );
                          },
                          child: const Text('Cancel'),
                        ),
                        ElevatedButton(
                          onPressed: () {
                            Navigator.pop(ctx);
                            setState(
                              () => _authTestResult =
                                  '✅ Biometric auth succeeded',
                            );
                          },
                          child: const Text('Authenticate'),
                        ),
                      ],
                    ),
                  );
                }
              : null,
          icon: const Icon(Icons.lock_open, size: 18),
          label: const Text('Test Biometric Prompt'),
        ),
        const Divider(height: 24),
        const Text(
          'FIDO2 / Passkey Credentials',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 8),
        if (_fido2Enabled)
          ListTile(
            title: const Text('Dev Test Key', style: TextStyle(fontSize: 12)),
            subtitle: const Text(
              'ID: fido2_dev_test_key_...',
              style: TextStyle(fontSize: 10),
            ),
            trailing: IconButton(
              icon: const Icon(Icons.delete, size: 18, color: Colors.red),
              onPressed: () => setState(() {
                _fido2Enabled = false;
                _authTestResult = '🗑️ Removed credential: Dev Test Key';
              }),
            ),
            dense: true,
            contentPadding: EdgeInsets.zero,
          )
        else
          const Text(
            'No FIDO2 credentials registered',
            style: TextStyle(fontSize: 11, color: Colors.grey),
          ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ElevatedButton(
                onPressed: () => setState(() {
                  _fido2Enabled = true;
                  _authTestResult = '✅ FIDO2 credential registered';
                }),
                child: const Text(
                  'Register Key',
                  style: TextStyle(fontSize: 12),
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton(
                onPressed: _fido2Enabled
                    ? () => setState(
                        () => _authTestResult = '✅ FIDO2 auth succeeded',
                      )
                    : null,
                child: const Text('Test Auth', style: TextStyle(fontSize: 12)),
              ),
            ),
          ],
        ),
        const Divider(height: 24),
        const Text(
          'TOTP (MFA)',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 8),
        if (_totpEnabled) ...[
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Row(
              children: [
                Text('Current Code: ', style: TextStyle(fontSize: 11)),
                Text(
                  '123 456',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontFamily: 'monospace',
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _totpController,
                  decoration: const InputDecoration(
                    labelText: 'Verify Code',
                    border: OutlineInputBorder(),
                  ),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 8),
              ElevatedButton(
                onPressed: () {
                  setState(
                    () => _authTestResult =
                        _totpController.text.replaceAll(' ', '') == '123456'
                        ? '✅ TOTP code valid'
                        : '❌ Invalid code',
                  );
                  _totpController.clear();
                },
                child: const Text('Verify'),
              ),
            ],
          ),
          const SizedBox(height: 8),
          OutlinedButton(
            onPressed: () => setState(() {
              _totpEnabled = false;
              _authTestResult = '🗑️ TOTP disabled';
            }),
            style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Disable TOTP'),
          ),
        ] else
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () => setState(() {
                _totpEnabled = true;
                _authTestResult = '📱 TOTP setup started';
              }),
              child: const Text('Setup TOTP'),
            ),
          ),
        const Divider(height: 24),
        const Text(
          'Backup Codes',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        Text(
          'Remaining: $_backupCodesRemaining',
          style: const TextStyle(fontSize: 11),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: ElevatedButton(
                onPressed: () {
                  final random = DateTime.now().millisecondsSinceEpoch;
                  setState(() {
                    _backupCodesRemaining = 10;
                    _generatedBackupCodes = List.generate(10, (i) {
                      final code = ((random + i * 73939) % 90000000 + 10000000)
                          .toString();
                      return '${code.substring(0, 4)}-${code.substring(4)}';
                    });
                    _authTestResult = '🔑 Generated 10 backup codes';
                  });
                },
                child: const Text('Generate', style: TextStyle(fontSize: 12)),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton(
                onPressed: _backupCodesRemaining > 0
                    ? () => setState(() {
                        _backupCodesRemaining--;
                        if (_generatedBackupCodes.isNotEmpty) {
                          _generatedBackupCodes = List.from(
                            _generatedBackupCodes,
                          )..removeLast();
                        }
                        _authTestResult = '✅ Backup code accepted';
                      })
                    : null,
                child: const Text('Test Code', style: TextStyle(fontSize: 12)),
              ),
            ),
          ],
        ),
        if (_generatedBackupCodes.isNotEmpty) ...[
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Your Backup Codes (save these!)',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 12,
                  runSpacing: 6,
                  children: _generatedBackupCodes
                      .asMap()
                      .entries
                      .map(
                        (entry) => Text(
                          '${entry.key + 1}. ${entry.value}',
                          style: const TextStyle(
                            fontFamily: 'monospace',
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      )
                      .toList(),
                ),
              ],
            ),
          ),
        ],
        if (_authTestResult != null) ...[
          const SizedBox(height: 12),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: theme.colorScheme.secondaryContainer.withValues(
                alpha: 0.5,
              ),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Text(
              _authTestResult!,
              style: const TextStyle(fontSize: 11, fontFamily: 'monospace'),
            ),
          ),
        ],
      ],
    );
  }
}

class _MfaStatusIcon extends StatelessWidget {
  final String label;
  final bool active;

  const _MfaStatusIcon({required this.label, required this.active});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(active ? '🟢' : '🔴', style: const TextStyle(fontSize: 16)),
        Text(label, style: const TextStyle(fontSize: 10)),
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
                const SnackBar(
                  content: Text('All developer options reset to defaults'),
                ),
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

class StressTestingDevSection extends ConsumerStatefulWidget {
  const StressTestingDevSection({super.key});

  @override
  ConsumerState<StressTestingDevSection> createState() =>
      _StressTestingDevSectionState();
}

class _StressTestingDevSectionState
    extends ConsumerState<StressTestingDevSection> {
  bool _isRunning = false;
  final List<_StressResult> _results = [];
  String? _currentTest;

  Future<void> _runSuite() async {
    setState(() {
      _isRunning = true;
      _results.clear();
      _currentTest = 'Initializing...';
    });

    final tests = <(String, Future<StressTestResult> Function())>[
      ('UI Responsiveness', () => StressTester.runWidgetStressTest(500)),
      ('Memory Pressure', StressTester.runMemoryStressTest),
      ('Rapid Navigation', StressTester.runRapidNavigationTest),
      ('Data Loading', () => StressTester.runNetworkStressTest(10)),
      ('Theme Switching', StressTester.runThemeSwitchingTest),
      ('Concurrent Ops', StressTester.runConcurrentOperationsTest),
      ('Storage I/O', StressTester.runStorageIOTest),
      ('Network Sim', StressTester.runNetworkSimulationTest),
      ('State Management', StressTester.runStateManagementTest),
      ('Input Validation', StressTester.runInputValidationTest),
      ('JSON Parsing', StressTester.runJsonParsingTest),
      ('String Operations', StressTester.runStringOperationsTest),
      ('DateTime Operations', StressTester.runDateTimeOperationsTest),
      ('Collection Operations', StressTester.runCollectionOperationsTest),
      ('Security Operations', StressTester.runSecurityOperationsTest),
      ('File System', StressTester.runFileSystemTest),
      ('Exception Handling', StressTester.runExceptionHandlingTest),
      ('Parental Controls', StressTester.runParentalControlsTest),
      ('Notification Channel', StressTester.runNotificationChannelTest),
      ('List Scroll', StressTester.runListScrollStressTest),
      ('TOTP Lifecycle', StressTester.runTotpLifecycleTest),
      ('TOTP Window Tolerance', StressTester.runTotpWindowToleranceTest),
      ('Backup Codes', StressTester.runBackupCodesTest),
      ('Email Verification Flow', StressTester.runEmailVerificationFlowTest),
      (
        'FIDO2 Credential Lifecycle',
        StressTester.runFido2CredentialLifecycleTest,
      ),
      ('Biometric Status', StressTester.runBiometricStatusTest),
      ('Biometric Toggle', StressTester.runBiometricToggleTest),
      ('Security Utils Round Trip', StressTester.runSecurityUtilsRoundTripTest),
      ('Device Authority', StressTester.runDeviceAuthorityTest),
      ('Session State', StressTester.runSessionStateTest),
      (
        'Account Lifecycle Parsing',
        StressTester.runAccountLifecycleParsingTest,
      ),
      ('Auth Enum Coverage', StressTester.runAuthEnumCoverageTest),
      ('Concurrent Auth Ops', StressTester.runConcurrentAuthOpsTest),
      ('TOTP Secret Edge Cases', StressTester.runTotpSecretEdgeCasesTest),
    ];

    for (final test in tests) {
      if (!mounted) return;
      setState(() => _currentTest = test.$1);
      try {
        final result = await test.$2();
        if (!mounted) return;
        setState(() {
          _results.add(
            _StressResult(
              test.$1,
              result.success,
              result.durationMs,
              result.message,
            ),
          );
        });
      } catch (e) {
        if (!mounted) return;
        setState(() {
          _results.add(_StressResult(test.$1, false, 0, 'Error: $e'));
        });
      }
    }

    if (mounted) {
      setState(() {
        _isRunning = false;
        _currentTest = null;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);

    return DevTestCard(
      title: 'System Stress Testing',
      icon: Icons.speed,
      children: [
        const Text(
          'Execute automated stress tests to verify application stability parity.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: _isRunning ? null : _runSuite,
            icon: _isRunning
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      color: Colors.white,
                    ),
                  )
                : const Icon(Icons.play_arrow, size: 18),
            label: Text(
              _isRunning ? 'Running: $_currentTest' : 'Run Comprehensive Suite',
            ),
          ),
        ),
        if (_results.isNotEmpty) ...[
          const SizedBox(height: 16),
          Row(
            children: [
              Text(
                'Results: ${_results.where((r) => r.passed).length} / ${_results.length} Passed',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 13,
                  color: _results.every((r) => r.passed)
                      ? Colors.green
                      : Colors.orange,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Theme.of(
                context,
              ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: _results
                  .map(
                    (r) => Padding(
                      padding: const EdgeInsets.symmetric(vertical: 3),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: const EdgeInsets.only(top: 1),
                            child: Icon(
                              r.passed ? Icons.check_circle : Icons.error,
                              color: r.passed ? Colors.green : Colors.red,
                              size: 14,
                            ),
                          ),
                          const SizedBox(width: 6),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        r.name,
                                        style: const TextStyle(
                                          fontSize: 11,
                                          fontWeight: FontWeight.w600,
                                        ),
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ),
                                    const SizedBox(width: 4),
                                    Text(
                                      '${r.durationMs}ms',
                                      style: TextStyle(
                                        fontSize: 10,
                                        fontWeight: FontWeight.w500,
                                        color: r.passed
                                            ? Colors.grey
                                            : Colors.red,
                                      ),
                                    ),
                                  ],
                                ),
                                Text(
                                  r.details,
                                  style: TextStyle(
                                    fontSize: 10,
                                    color: r.passed ? Colors.grey : Colors.red,
                                  ),
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  )
                  .toList(),
            ),
          ),
        ],
        const Divider(height: 32),
        SwitchListTile(
          title: const Text('Show Performance Overlay'),
          value: options.showPerformanceOverlay,
          onChanged: notifier.setShowPerformanceOverlay,
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        const SizedBox(height: 8),
        const Text(
          'Manual Triggers:',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11),
        ),
        const SizedBox(height: 4),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton(
              onPressed: () => _runStressTest(
                context,
                'Widget Creation',
                () => StressTester.runWidgetStressTest(1000),
              ),
              child: const Text(
                'Widgets (1000)',
                style: TextStyle(fontSize: 10),
              ),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(
                context,
                'Animation Stress',
                StressTester.runAnimationStressTest,
              ),
              child: const Text('Animations', style: TextStyle(fontSize: 10)),
            ),
            ElevatedButton(
              onPressed: () => _runStressTest(
                context,
                'Memory Pressure',
                StressTester.runMemoryStressTest,
              ),
              child: const Text('Memory', style: TextStyle(fontSize: 10)),
            ),
          ],
        ),
      ],
    );
  }

  Future<void> _runStressTest(
    BuildContext context,
    String title,
    Future<StressTestResult> Function() testFn,
  ) async {
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
              Text(
                'Duration: ${result.durationMs}ms',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
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

class _StressResult {
  final String name;
  final bool passed;
  final int durationMs;
  final String details;
  _StressResult(this.name, this.passed, this.durationMs, this.details);
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
  bool _isLoadingDeveloperAuth = !kDebugMode;
  bool _hasDeveloperAccess = kDebugMode;
  String? _deviceHash;
  String? _signatureHash;

  @override
  void initState() {
    super.initState();
    _loadInfo();
    _loadDeveloperAuthInfo();
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
    _currentMemoryMB = (ProcessInfo.currentRss / (1024 * 1024)).round();
  }

  Future<void> _loadDeveloperAuthInfo() async {
    final hasAccess = await DeviceAuthorizationService.canUseDeveloperTools();
    final deviceHash = await DeviceAuthorizationService.getDeviceHash();
    final signatureHash =
        await DeviceAuthorizationService.getAppSignatureHash();

    if (!mounted) return;
    setState(() {
      _hasDeveloperAccess = hasAccess;
      _deviceHash = deviceHash;
      _signatureHash = signatureHash;
      _isLoadingDeveloperAuth = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final info = _packageInfo;
    final rows = <MapEntry<String, String>>[
      MapEntry('Package', info?.packageName ?? '—'),
      MapEntry('Version', info?.version ?? '—'),
      MapEntry('Build', info?.buildNumber ?? '—'),
      MapEntry('Build Type', kDebugMode ? 'DEBUG' : 'RELEASE'),
      MapEntry(
        'OS',
        '${Platform.operatingSystem} ${Platform.operatingSystemVersion}',
      ),
      MapEntry('Dart', Platform.version.split(' ').first),
      MapEntry(
        'Heap Used',
        '$_currentMemoryMB MB / ${ProcessInfo.maxRss ~/ (1024 * 1024)} MB max',
      ),
    ];

    return DevTestCard(
      title: 'App Info & Diagnostics',
      icon: Icons.info,
      children: [
        ...rows.map(
          (e) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 2),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  e.key,
                  style: const TextStyle(
                    fontWeight: FontWeight.w600,
                    fontSize: 12,
                  ),
                ),
                Flexible(
                  child: Text(
                    e.value,
                    style: const TextStyle(fontSize: 12),
                    textAlign: TextAlign.end,
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: _hasDeveloperAccess
                ? Colors.green.withValues(alpha: 0.08)
                : Theme.of(
                    context,
                  ).colorScheme.errorContainer.withValues(alpha: 0.2),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: _hasDeveloperAccess
                  ? Colors.green.withValues(alpha: 0.25)
                  : Theme.of(context).colorScheme.error.withValues(alpha: 0.25),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    _hasDeveloperAccess
                        ? Icons.verified_user
                        : Icons.lock_outline,
                    size: 18,
                    color: _hasDeveloperAccess
                        ? Colors.green
                        : Theme.of(context).colorScheme.error,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _hasDeveloperAccess
                          ? 'Authorized developer device'
                          : 'Developer access restricted',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              if (_isLoadingDeveloperAuth) ...[
                const LinearProgressIndicator(minHeight: 4),
                const SizedBox(height: 8),
                const Text(
                  'Loading diagnostics…',
                  style: TextStyle(fontSize: 11),
                ),
              ] else ...[
                if (_deviceHash != null) ...[
                  const Text(
                    'Device Hash',
                    style: TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
                  ),
                  const SizedBox(height: 4),
                  SelectableText(
                    _deviceHash!,
                    style: const TextStyle(
                      fontSize: 10,
                      fontFamily: 'monospace',
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
                if (_signatureHash != null) ...[
                  const Text(
                    'App Signature Hash',
                    style: TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
                  ),
                  const SizedBox(height: 4),
                  SelectableText(
                    _signatureHash!,
                    style: const TextStyle(
                      fontSize: 10,
                      fontFamily: 'monospace',
                    ),
                  ),
                  const SizedBox(height: 10),
                ],
                OutlinedButton.icon(
                  onPressed: (_deviceHash == null && _signatureHash == null)
                      ? null
                      : () async {
                          await Clipboard.setData(
                            ClipboardData(
                              text: [
                                if (_deviceHash != null)
                                  'DEVELOPER_DEVICE_HASH=$_deviceHash',
                                if (_signatureHash != null)
                                  'INTERNAL_SIGNATURE_HASH=$_signatureHash',
                              ].join('\n'),
                            ),
                          );
                          if (!context.mounted) return;
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Copied for local.properties'),
                            ),
                          );
                        },
                  icon: const Icon(Icons.copy_all_outlined, size: 16),
                  label: const Text(
                    'Copy Access Info',
                    style: TextStyle(fontSize: 11),
                  ),
                ),
              ],
            ],
          ),
        ),
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
              backgroundColor: Theme.of(
                context,
              ).colorScheme.surfaceContainerHighest,
              labelStyle: TextStyle(
                color: isSelected
                    ? Colors.white
                    : Theme.of(context).colorScheme.onSurface,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
              ),
              checkmarkColor: Colors.white,
              onSelected: (selected) {
                if (selected && options.environment != env) {
                  showDialog(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      title: const Text('Switch Environment?'),
                      content: Text(
                        'Switching to ${env.name}. The app may need to be restarted.',
                      ),
                      actions: [
                        TextButton(
                          onPressed: () => Navigator.pop(ctx),
                          child: const Text('Cancel'),
                        ),
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
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
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
              Text(
                'Active: ${options.environment.name.toUpperCase()}',
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 13,
                ),
              ),
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

    final flags = <(String, String, bool, ValueChanged<bool>)>[
      (
        'New Feed Layout',
        'Experimental feed grid layout',
        options.enableNewFeedLayout,
        notifier.setEnableNewFeedLayout,
      ),
      (
        'Video Chat',
        'Enable video calling feature',
        options.enableVideoChat,
        notifier.setEnableVideoChat,
      ),
      (
        'Story Reactions',
        'Allow emoji reactions on stories',
        options.enableStoryReactions,
        notifier.setEnableStoryReactions,
      ),
      (
        'Advanced Search',
        'Full-text and filter search',
        options.enableAdvancedSearch,
        notifier.setEnableAdvancedSearch,
      ),
      (
        'AI Suggestions',
        'AI-powered content suggestions',
        options.enableAiSuggestions,
        notifier.setEnableAiSuggestions,
      ),
      (
        'Cross-Device Handoff',
        'Enable Android handoff hooks on supported devices',
        options.enableHandoff,
        notifier.setEnableHandoff,
      ),
    ];

    final activeCount = flags.where((f) => f.$3).length;

    return DevTestCard(
      title: 'Feature Flags',
      icon: Icons.flag,
      children: [
        const Text(
          'Toggle experimental features. Persisted across restarts.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 8),
        ...flags.map(
          (flag) => SwitchListTile(
            title: Text(flag.$1),
            subtitle: Text(flag.$2, style: const TextStyle(fontSize: 11)),
            value: flag.$3,
            onChanged: (v) => flag.$4(v),
            dense: true,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          '$activeCount of ${flags.length} flags enabled',
          style: TextStyle(
            fontSize: 11,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
      ],
    );
  }
}

class ABTestingDevSection extends ConsumerWidget {
  const ABTestingDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final theme = Theme.of(context);

    final variants = [
      (
        ABTestVariant.control,
        'Control',
        Icons.science_outlined,
        'Default production UI',
      ),
      (
        ABTestVariant.liquidGlassFrosted,
        'Glass · Frosted',
        Icons.blur_on_rounded,
        'iOS 26 frosted glass on navbar, headers, FAB & sheets',
      ),
      (
        ABTestVariant.liquidGlassAurora,
        'Glass · Aurora',
        Icons.auto_awesome_rounded,
        'iOS 26 glass with accent-tinted aurora overlay',
      ),
      (
        ABTestVariant.compactCards,
        'Compact Cards',
        Icons.view_agenda_rounded,
        'Smaller, denser card layout',
      ),
      (
        ABTestVariant.boldTypography,
        'Bold Type',
        Icons.format_bold_rounded,
        'Larger, high-contrast text',
      ),
      (
        ABTestVariant.semiSkeumorphic,
        'Semi-Skeumorphic',
        Icons.layers_rounded,
        'Soft depth, subtle bevels, and tactile controls with modern restraint',
      ),
      (
        ABTestVariant.fullSkeumorphic,
        'Full-Skeumorphic',
        Icons.album_rounded,
        'High-depth chrome with pronounced tactile surfaces, sculpted pills, and richer shadows',
      ),
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
            color: theme.colorScheme.surfaceContainerHighest.withValues(
              alpha: 0.5,
            ),
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
                    variants
                        .firstWhere((v) => v.$1 == options.abTestVariant)
                        .$3,
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
                style: TextStyle(
                  fontSize: 11,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        _ABVariantPreviewLab(variant: options.abTestVariant),
      ],
    );
  }
}

class _ABVariantPreviewLab extends StatelessWidget {
  final ABTestVariant variant;

  const _ABVariantPreviewLab({required this.variant});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final isLiquid = variant.isLiquidGlass;
    final isSkeuo = variant.isSkeumorphic;
    final isFull = variant.isFullSkeumorphic;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(alpha: isFull ? 0.16 : 0.08),
      theme.colorScheme.surfaceContainerLow,
    );

    final decoration = BoxDecoration(
      borderRadius: BorderRadius.circular(24),
      gradient: isLiquid
          ? LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                theme.colorScheme.surface.withValues(
                  alpha: isDark ? 0.68 : 0.82,
                ),
                theme.colorScheme.surface.withValues(
                  alpha: isDark ? 0.48 : 0.62,
                ),
                if (variant == ABTestVariant.liquidGlassAurora)
                  AppColors.primaryPurple.withValues(alpha: 0.16),
              ],
            )
          : isSkeuo
          ? LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                Color.alphaBlend(
                  Colors.white.withValues(alpha: isDark ? 0.08 : 0.24),
                  base,
                ),
                base,
                Color.alphaBlend(
                  theme.colorScheme.primary.withValues(
                    alpha: isFull ? 0.18 : 0.10,
                  ),
                  base,
                ),
              ],
            )
          : null,
      color: !isLiquid && !isSkeuo
          ? theme.colorScheme.surfaceContainerLow
          : null,
      border: Border.all(
        color: isLiquid
            ? Colors.white.withValues(alpha: isDark ? 0.10 : 0.28)
            : isSkeuo
            ? Colors.white.withValues(alpha: isDark ? 0.08 : 0.36)
            : theme.colorScheme.outlineVariant,
      ),
      boxShadow: isLiquid
          ? [
              BoxShadow(
                color: Colors.black.withValues(alpha: isDark ? 0.16 : 0.06),
                blurRadius: 18,
                offset: const Offset(0, 10),
              ),
            ]
          : isSkeuo
          ? [
              BoxShadow(
                color: Colors.black.withValues(alpha: isDark ? 0.24 : 0.10),
                blurRadius: isFull ? 26 : 16,
                offset: Offset(0, isFull ? 14 : 9),
              ),
              BoxShadow(
                color: Colors.white.withValues(alpha: isDark ? 0.03 : 0.42),
                blurRadius: isFull ? 12 : 8,
                offset: const Offset(-4, -4),
              ),
            ]
          : null,
    );

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: decoration,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Preview Lab',
            style: theme.textTheme.labelLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            isSkeuo
                ? 'Testing tactile depth on shell chrome, hero cards, and action clusters.'
                : isLiquid
                ? 'Testing immersive translucent chrome across the same surfaces.'
                : 'Switch variants above to preview experimental shell chrome.',
            style: TextStyle(
              fontSize: 11,
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
                  ),
                ),
                child: const Icon(
                  Icons.auto_awesome,
                  color: Colors.white,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Support-focused shell chrome',
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    Text(
                      'Comfortable depth without harming readability.',
                      style: TextStyle(
                        fontSize: 11,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              _previewIconButton(context, Icons.add),
              const SizedBox(width: 8),
              _previewIconButton(context, Icons.settings_outlined),
            ],
          ),
          const SizedBox(height: 14),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(20),
              gradient: isSkeuo
                  ? LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        Color.alphaBlend(
                          Colors.white.withValues(alpha: isDark ? 0.08 : 0.22),
                          base,
                        ),
                        base,
                      ],
                    )
                  : null,
              color: isSkeuo ? null : theme.colorScheme.surface,
              border: Border.all(
                color: isSkeuo
                    ? Colors.white.withValues(alpha: isDark ? 0.08 : 0.30)
                    : theme.colorScheme.outlineVariant.withValues(alpha: 0.5),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'A softer, lower-friction social loop',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  'Reduced glare, clearer touch targets, and calmer motion cues for feed + messaging.',
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurfaceVariant,
                    height: 1.35,
                  ),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    _previewPill(context, Icons.favorite_border, '218'),
                    _previewPill(context, Icons.chat_bubble_outline, '32'),
                    _previewPill(context, Icons.bookmark_border, 'Save'),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(18),
              color: isLiquid
                  ? Colors.white.withValues(alpha: isDark ? 0.08 : 0.16)
                  : isSkeuo
                  ? null
                  : theme.colorScheme.surfaceContainerHighest,
              gradient: isSkeuo
                  ? LinearGradient(
                      colors: [
                        Color.alphaBlend(
                          Colors.white.withValues(alpha: isDark ? 0.06 : 0.18),
                          base,
                        ),
                        base,
                      ],
                    )
                  : null,
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: const [
                _PreviewNavItem(
                  icon: Icons.home_filled,
                  label: 'Feed',
                  selected: true,
                ),
                _PreviewNavItem(icon: Icons.explore_outlined, label: 'Explore'),
                _PreviewNavItem(
                  icon: Icons.chat_bubble_outline,
                  label: 'Messages',
                ),
                _PreviewNavItem(icon: Icons.person_outline, label: 'Profile'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _previewIconButton(BuildContext context, IconData icon) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final isSkeuo = variant.isSkeumorphic;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(
        alpha: variant.isFullSkeumorphic ? 0.14 : 0.08,
      ),
      theme.colorScheme.surface,
    );

    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: !isSkeuo ? Colors.white.withValues(alpha: 0.14) : null,
        gradient: isSkeuo
            ? LinearGradient(
                colors: [
                  Color.alphaBlend(
                    Colors.white.withValues(alpha: isDark ? 0.06 : 0.20),
                    base,
                  ),
                  base,
                ],
              )
            : null,
        border: Border.all(
          color: Colors.white.withValues(alpha: isDark ? 0.08 : 0.26),
        ),
      ),
      child: Icon(icon, size: 20, color: theme.colorScheme.primary),
    );
  }

  Widget _previewPill(BuildContext context, IconData icon, String label) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final isSkeuo = variant.isSkeumorphic;
    final base = Color.alphaBlend(
      theme.colorScheme.primary.withValues(
        alpha: variant.isFullSkeumorphic ? 0.12 : 0.06,
      ),
      theme.colorScheme.surface,
    );

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(999),
        color: !isSkeuo ? theme.colorScheme.surfaceContainerHighest : null,
        gradient: isSkeuo
            ? LinearGradient(
                colors: [
                  Color.alphaBlend(
                    Colors.white.withValues(alpha: isDark ? 0.06 : 0.18),
                    base,
                  ),
                  base,
                ],
              )
            : null,
        border: Border.all(
          color: isSkeuo
              ? Colors.white.withValues(alpha: isDark ? 0.08 : 0.28)
              : theme.colorScheme.outlineVariant,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: theme.colorScheme.onSurfaceVariant),
          const SizedBox(width: 6),
          Text(label, style: theme.textTheme.labelMedium),
        ],
      ),
    );
  }
}

class _PreviewNavItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool selected;

  const _PreviewNavItem({
    required this.icon,
    required this.label,
    this.selected = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = selected
        ? theme.colorScheme.primary
        : theme.colorScheme.onSurfaceVariant;
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 20, color: color),
        const SizedBox(height: 4),
        Text(
          label,
          style: theme.textTheme.labelSmall?.copyWith(
            color: color,
            fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
          ),
        ),
      ],
    );
  }
}

class FeedbackBetaDevSection extends ConsumerStatefulWidget {
  const FeedbackBetaDevSection({super.key});

  @override
  ConsumerState<FeedbackBetaDevSection> createState() =>
      _FeedbackBetaDevSectionState();
}

class _FeedbackBetaDevSectionState
    extends ConsumerState<FeedbackBetaDevSection> {
  int _pendingCount = 0;
  int _remainingSubmissions = 5;
  final List<String> _sessionReports = [];

  @override
  void initState() {
    super.initState();
    _loadPendingCount();
    _loadRemainingSubmissions();
  }

  Future<void> _loadPendingCount() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _pendingCount = prefs.getStringList('pending_feedback')?.length ?? 0;
    });
  }

  Future<void> _loadRemainingSubmissions() async {
    final prefs = await SharedPreferences.getInstance();
    final today = DateTime.now().toIso8601String().substring(0, 10);
    final todayCount = prefs.getInt('feedback_count_$today') ?? 0;
    setState(() {
      _remainingSubmissions = (5 - todayCount).clamp(0, 5);
    });
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final theme = Theme.of(context);

    return DevTestCard(
      title: 'Feedback & Beta Testing',
      icon: Icons.feedback,
      children: [
        Text(
          'Test feedback submission, offline queuing, and rate-limit behaviour parity.',
          style: TextStyle(
            fontSize: 12,
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text(
            'Bypass Feedback Rate Limit',
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
          ),
          subtitle: const Text(
            'Skip the 5/day submission cap',
            style: TextStyle(fontSize: 11),
          ),
          value: options.bypassFeedbackRateLimit,
          onChanged: notifier.setBypassFeedbackRateLimit,
          dense: true,
        ),
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text(
            'Force Submission Failure',
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
          ),
          subtitle: const Text(
            'Always fail remote insert → queue offline',
            style: TextStyle(fontSize: 11),
          ),
          value: options.forceFeedbackSubmitFailure,
          onChanged: notifier.setForceFeedbackSubmitFailure,
          dense: true,
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: _pendingCount > 0
                ? theme.colorScheme.tertiaryContainer.withValues(alpha: 0.4)
                : theme.colorScheme.surfaceContainerHighest.withValues(
                    alpha: 0.3,
                  ),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    Icons.cloud_queue,
                    size: 18,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '$_pendingCount item${_pendingCount != 1 ? "s" : ""} in offline queue',
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () async {
                        final prefs = await SharedPreferences.getInstance();
                        await prefs.remove('pending_feedback');
                        await _loadPendingCount();
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              content: Text('Offline queue cleared'),
                            ),
                          );
                        }
                      },
                      icon: const Icon(Icons.delete_sweep, size: 16),
                      label: const Text(
                        'Clear',
                        style: TextStyle(fontSize: 11),
                      ),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.red,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: _pendingCount > 0
                          ? () async {
                              setState(
                                () => _sessionReports.add(
                                  '[${DateTime.now().toIso8601String().substring(11, 19)}] Flushing $_pendingCount pending items…',
                                ),
                              );
                              await flushPendingFeedback();
                              await _loadPendingCount();
                              if (context.mounted) {
                                setState(
                                  () => _sessionReports.add(
                                    '[${DateTime.now().toIso8601String().substring(11, 19)}] Flush complete. Remaining: $_pendingCount',
                                  ),
                                );
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Queue flushed successfully'),
                                  ),
                                );
                              }
                            }
                          : null,
                      icon: const Icon(Icons.cloud_sync, size: 16),
                      label: const Text(
                        'Flush',
                        style: TextStyle(fontSize: 11),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Rate limit: $_remainingSubmissions / 5 submissions remaining today${options.bypassFeedbackRateLimit ? " (bypassed)" : ""}',
          style: TextStyle(
            fontSize: 11,
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: FilledButton.tonalIcon(
            onPressed: () {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  title: const Text('Submitted Reports Log'),
                  content: SizedBox(
                    height: 200,
                    width: double.maxFinite,
                    child: _sessionReports.isEmpty
                        ? const Center(
                            child: Text(
                              'No reports logged in this session.',
                              style: TextStyle(
                                color: Colors.grey,
                                fontSize: 12,
                              ),
                            ),
                          )
                        : ListView.builder(
                            itemCount: _sessionReports.length,
                            itemBuilder: (context, index) => Padding(
                              padding: const EdgeInsets.symmetric(vertical: 2),
                              child: Text(
                                _sessionReports[index],
                                style: const TextStyle(
                                  fontFamily: 'monospace',
                                  fontSize: 11,
                                ),
                              ),
                            ),
                          ),
                  ),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text('Close'),
                    ),
                  ],
                ),
              );
            },
            icon: const Icon(Icons.list_alt, size: 18),
            label: const Text('View Reports Log'),
          ),
        ),
        const Divider(height: 24),
        const Text(
          'Quick Navigate',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 8),
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: [
              ActionChip(
                avatar: const Icon(Icons.feedback, size: 16),
                label: const Text('Hub', style: TextStyle(fontSize: 11)),
                onPressed: () => context.push('/feedback'),
              ),
              const SizedBox(width: 8),
              ActionChip(
                avatar: const Icon(Icons.bug_report, size: 16),
                label: const Text('Bug', style: TextStyle(fontSize: 11)),
                onPressed: () => context.push('/feedback/bug'),
              ),
              const SizedBox(width: 8),
              ActionChip(
                avatar: const Icon(Icons.lightbulb, size: 16),
                label: const Text('Feature', style: TextStyle(fontSize: 11)),
                onPressed: () => context.push('/feedback/feature'),
              ),
              const SizedBox(width: 8),
              ActionChip(
                avatar: const Icon(Icons.chat, size: 16),
                label: const Text('General', style: TextStyle(fontSize: 11)),
                onPressed: () => context.push('/feedback/general'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class LiveSessionLabDevSection extends ConsumerWidget {
  const LiveSessionLabDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final channelGroups = NotificationChannelsService().getAllChannels();
    final channelCount = channelGroups.values.fold<int>(
      0,
      (sum, group) => sum + group.length,
    );

    return DevTestCard(
      title: 'Live Session Lab',
      icon: Icons.timer,
      children: [
        const Text(
          'Parity surface for Android live-session work: notification-channel coverage, ongoing-task readiness, and cross-device handoff toggles.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Platform: ${Platform.operatingSystem}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 4),
              Text(
                'Notification channel groups: ${channelGroups.length}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Total channels mirrored from Android: $channelCount',
                style: const TextStyle(fontSize: 12),
              ),
            ],
          ),
        ),
        SwitchListTile(
          contentPadding: EdgeInsets.zero,
          title: const Text('Enable Cross-Device Handoff'),
          subtitle: const Text(
            'Matches the Android dev flag for supported host builds.',
          ),
          value: options.enableHandoff,
          onChanged: notifier.setEnableHandoff,
        ),
        OutlinedButton.icon(
          onPressed: () => context.push('/practice-calls'),
          icon: const Icon(Icons.call_outlined, size: 18),
          label: const Text('Open Practice Calls'),
        ),
      ],
    );
  }
}

class PaymentsSubscriptionsDevSection extends StatefulWidget {
  const PaymentsSubscriptionsDevSection({super.key});

  @override
  State<PaymentsSubscriptionsDevSection> createState() =>
      _PaymentsSubscriptionsDevSectionState();
}

class _PaymentsSubscriptionsDevSectionState
    extends State<PaymentsSubscriptionsDevSection> {
  void _showPopup(String message) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Simulation Result'),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final subscriptionService = SubscriptionService.instance;

    return ValueListenableBuilder<SubscriptionState>(
      valueListenable: subscriptionService.stateNotifier,
      builder: (context, state, _) {
        return DevTestCard(
          title: 'Payments & Subscriptions',
          icon: Icons.payment,
          children: [
            Text(
              'Simulate payment outcomes without reaching the real payment sheet. Useful for verifying UI properly reacts to success, decline, or timeout events.',
              style: TextStyle(
                fontSize: 12,
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(
                  context,
                ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Current State',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Premium active: ${state.isPremium}',
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  Text(
                    'Loading: ${state.isLoading}',
                    style: const TextStyle(fontSize: 12),
                  ),
                  Text(
                    'Last transaction: ${state.purchaseType ?? '—'}',
                    style: const TextStyle(fontSize: 12),
                  ),
                  Text(
                    'Error: ${state.error ?? 'None'}',
                    style: const TextStyle(fontSize: 12),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () async {
                  await subscriptionService.simulateTestSuccess();
                  if (context.mounted) {
                    _showPopup(
                      '🎉 Woohoo! Simulation successful. The dopamine hit has been deposited, and your premium powers are now fully unlocked.',
                    );
                  }
                },
                icon: const Icon(Icons.check_circle_outline, size: 18),
                label: const Text('Simulate Successful Payment'),
              ),
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () async {
                  await subscriptionService.simulateTestDeclined();
                  if (context.mounted) {
                    _showPopup(
                      '🚫 Payment declined. Simulation complete. Sometimes the executive function to find the right credit card just isn\'t there today.',
                    );
                  }
                },
                icon: const Icon(Icons.cancel_outlined, size: 18),
                label: const Text('Simulate Declined Payment'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Theme.of(context).colorScheme.error,
                  foregroundColor: Theme.of(context).colorScheme.onError,
                ),
              ),
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () async {
                  await subscriptionService.simulateTestTimedOut();
                  if (context.mounted) {
                    _showPopup(
                      '⏳ Timed-Out! The server got distracted by a shiny side quest and forgot to finish your transaction. Simulation complete.',
                    );
                  }
                },
                icon: const Icon(Icons.hourglass_bottom_outlined, size: 18),
                label: const Text('Simulate Timed-Out Payment'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Theme.of(context).colorScheme.tertiary,
                  foregroundColor: Theme.of(context).colorScheme.onTertiary,
                ),
              ),
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () {
                  subscriptionService.resetTestPurchase();
                  _showPopup(
                    '🔄 Reset complete. The simulation chalkboard has been wiped clean. You are back to the free tier.',
                  );
                },
                icon: const Icon(Icons.restart_alt, size: 18),
                label: const Text('Reset Premium Status'),
              ),
            ),
            const SizedBox(height: 8),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: () => context.push('/subscription'),
                icon: const Icon(Icons.open_in_new, size: 18),
                label: const Text('Open Subscription Screen'),
              ),
            ),
          ],
        );
      },
    );
  }
}

class ModerationHeuristicsDevSection extends ConsumerStatefulWidget {
  const ModerationHeuristicsDevSection({super.key});

  @override
  ConsumerState<ModerationHeuristicsDevSection> createState() =>
      _ModerationHeuristicsDevSectionState();
}

class _ModerationHeuristicsDevSectionState
    extends ConsumerState<ModerationHeuristicsDevSection> {
  static const _samples = <String, String>{
    'Clean': 'Celebrating a calm, supportive day with friends and family.',
    'Flagged': 'Please review this spammy scam link before anyone clicks it.',
    'Blocked':
        'This threat encourages illegal harm against an underage target.',
    'Kids Filter':
        'bad word and violence should be reviewed before kids can read it.',
  };

  late final TextEditingController _controller;
  String _sampleText = _samples.values.first;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: _sampleText);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final notifier = ref.read(devOptionsProvider.notifier);
    final report = _analyzeModerationText(_sampleText);
    final effectiveStatus = switch (options.moderationOverride) {
      ModerationOverride.off => report.status,
      ModerationOverride.clean => 'clean',
      ModerationOverride.flagged => 'flagged',
      ModerationOverride.blocked => 'blocked',
    };

    final kidsFilterLevel = switch (options.kidsFilterLevel) {
      KidsFilterLevel.strict => content_filtering.KidsFilterLevel.strict,
      KidsFilterLevel.moderate ||
      KidsFilterLevel.relaxed => content_filtering.KidsFilterLevel.moderate,
    };
    final contentFilter = content_filtering.ContentFilteringService();
    final shouldHideForKids = contentFilter.shouldHideTextForKids(
      _sampleText,
      kidsFilterLevel,
    );
    final sanitizedForKids = contentFilter.sanitizeForKids(
      _sampleText,
      kidsFilterLevel,
    );

    return DevTestCard(
      title: 'Moderation Heuristics',
      icon: Icons.gpp_good_outlined,
      children: [
        const Text(
          'Preview the same keyword-based moderation + kids-filter behaviour surfaced in the Android tools.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: _samples.entries.map((entry) {
            return ActionChip(
              label: Text(entry.key),
              onPressed: () {
                setState(() {
                  _sampleText = entry.value;
                  _controller.text = entry.value;
                });
              },
            );
          }).toList(),
        ),
        const SizedBox(height: 12),
        TextField(
          controller: _controller,
          minLines: 4,
          maxLines: 6,
          decoration: const InputDecoration(
            labelText: 'Sample content',
            border: OutlineInputBorder(),
          ),
          onChanged: (value) => setState(() => _sampleText = value),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: _moderationStatusColor(
              context,
              report.status,
            ).withValues(alpha: 0.14),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Heuristic result: ${report.status.toUpperCase()}',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                report.blockedMatches.isNotEmpty
                    ? 'Blocked keywords: ${report.blockedMatches.join(', ')}'
                    : report.flaggedMatches.isNotEmpty
                    ? 'Flagged keywords: ${report.flaggedMatches.join(', ')}'
                    : 'No blocked or flagged keywords matched.',
                style: const TextStyle(fontSize: 12),
              ),
              const SizedBox(height: 4),
              Text(
                'Override applied: ${options.moderationOverride.name} → effective status: ${effectiveStatus.toUpperCase()}',
                style: TextStyle(
                  fontSize: 12,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        const Text(
          'Moderation Override',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 4),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: ModerationOverride.values.map((item) {
            final selected = options.moderationOverride == item;
            return FilterChip(
              label: Text(item.name),
              selected: selected,
              onSelected: (_) => notifier.setModerationOverride(item),
              selectedColor: AppColors.primaryPurple,
              labelStyle: TextStyle(color: selected ? Colors.white : null),
              checkmarkColor: Colors.white,
            );
          }).toList(),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Kids filter preview (${options.kidsFilterLevel.name})',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(
                shouldHideForKids
                    ? 'This sample would be hidden for kids.'
                    : 'This sample would remain visible for kids.',
                style: const TextStyle(fontSize: 12),
              ),
              const SizedBox(height: 4),
              Text(
                'Sanitized preview: $sanitizedForKids',
                style: TextStyle(
                  fontSize: 12,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class ContactPickerDevSection extends StatefulWidget {
  const ContactPickerDevSection({super.key});

  @override
  State<ContactPickerDevSection> createState() =>
      _ContactPickerDevSectionState();
}

class _ContactPickerDevSectionState extends State<ContactPickerDevSection> {
  final ContactsPickerService _contactsPickerService = ContactsPickerService();
  bool? _supportsPrivacyPicker;
  bool? _needsPermission;
  bool _isLoading = false;
  int _pickCount = 0;
  String? _resultLog;
  String? _errorLog;

  @override
  void initState() {
    super.initState();
    _refreshCapabilities();
  }

  Future<void> _refreshCapabilities() async {
    final supportsPrivacyPicker = await _contactsPickerService
        .supportsPrivacyPicker();
    final needsPermission = await _contactsPickerService
        .needsContactsPermission();
    if (!mounted) return;
    setState(() {
      _supportsPrivacyPicker = supportsPrivacyPicker;
      _needsPermission = needsPermission;
    });
  }

  Future<void> _pickContact() async {
    setState(() {
      _isLoading = true;
      _errorLog = null;
      _resultLog = null;
    });

    final contact = await _contactsPickerService.pickContact();
    if (!mounted) return;

    setState(() {
      _isLoading = false;
      if (contact == null) {
        _resultLog = 'Picker cancelled or no contact returned.';
        return;
      }

      _pickCount++;
      _resultLog = [
        'Pick #$_pickCount',
        'Name: ${contact.displayName}',
        'Phone: ${contact.phoneNumber ?? '—'}',
        'Email: ${contact.email ?? '—'}',
        'URI: ${contact.contactUri ?? '—'}',
      ].join('\n');
    });
  }

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Contact Picker',
      icon: Icons.contacts,
      children: [
        const Text(
          'Exercise the privacy-preserving contacts picker path already wired into the Flutter Android host.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Platform: ${Platform.operatingSystem}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 4),
              Text(
                'Android privacy picker available: ${_supportsPrivacyPicker ?? '…'}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'READ_CONTACTS required: ${_needsPermission ?? '…'}',
                style: const TextStyle(fontSize: 12),
              ),
              const SizedBox(height: 4),
              Text(
                'Single-select is surfaced in Flutter; Android native multi-select testing remains available in the Kotlin app.',
                style: TextStyle(
                  fontSize: 11,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: _isLoading ? null : _pickContact,
              icon: const Icon(Icons.person_search_outlined, size: 18),
              label: Text(_isLoading ? 'Picking…' : 'Pick Contact'),
            ),
            OutlinedButton.icon(
              onPressed: _refreshCapabilities,
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Capabilities'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/messages'),
              icon: const Icon(Icons.chat_bubble_outline, size: 18),
              label: const Text('Open Messages'),
            ),
          ],
        ),
        if (_resultLog != null) ...[
          const SizedBox(height: 12),
          SelectableText(
            _resultLog!,
            style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
          ),
        ],
        if (_errorLog != null) ...[
          const SizedBox(height: 8),
          Text(
            _errorLog!,
            style: TextStyle(
              fontSize: 12,
              color: Theme.of(context).colorScheme.error,
            ),
          ),
        ],
      ],
    );
  }
}

class StoriesAndCallsDevSection extends ConsumerWidget {
  const StoriesAndCallsDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final options = ref.watch(devOptionsProvider);
    final storiesAsync = ref.watch(storiesProvider);
    final groups = storiesAsync.maybeWhen(
      data: (value) => value,
      orElse: () => const <story_models.StoryGroup>[],
    );
    final totalStories = groups.fold<int>(
      0,
      (sum, group) => sum + group.stories.length,
    );
    final unseenGroups = groups.where((group) => group.hasUnseenStories).length;

    return DevTestCard(
      title: 'Stories & Calls',
      icon: Icons.auto_stories,
      children: [
        Text(
          'Quick-launch the user-facing story and call flows while keeping parity flags visible.',
          style: TextStyle(
            fontSize: 12,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Story groups: ${groups.length}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              Text(
                'Total story items: $totalStories',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Unseen groups: $unseenGroups',
                style: const TextStyle(fontSize: 12),
              ),
              const SizedBox(height: 4),
              Text(
                'Story reactions flag: ${options.enableStoryReactions}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Video chat flag: ${options.enableVideoChat}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Handoff flag: ${options.enableHandoff}',
                style: const TextStyle(fontSize: 12),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => ref.read(storiesProvider.notifier).refresh(),
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Stories'),
            ),
            ElevatedButton.icon(
              onPressed: () => context.push('/create-story'),
              icon: const Icon(Icons.add_circle_outline, size: 18),
              label: const Text('Create Story'),
            ),
            ElevatedButton.icon(
              onPressed: () => context.push('/practice-calls'),
              icon: const Icon(Icons.call_outlined, size: 18),
              label: const Text('Practice Calls'),
            ),
            OutlinedButton.icon(
              onPressed: () => _openActiveCallPreview(context),
              icon: const Icon(Icons.videocam_outlined, size: 18),
              label: const Text('Open Active Call'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/messages'),
              icon: const Icon(Icons.chat_outlined, size: 18),
              label: const Text('Open Messages'),
            ),
          ],
        ),
      ],
    );
  }
}

class BackupRestoreDevSection extends ConsumerWidget {
  const BackupRestoreDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final backupState = ref.watch(backupProvider);

    return DevTestCard(
      title: 'Backup & Restore',
      icon: Icons.backup_outlined,
      children: [
        Text(
          'Mirror the Android backup lab with quick launch into backup settings plus one-tap local backup creation.',
          style: TextStyle(
            fontSize: 12,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Local backups: ${backupState.localBackups.length}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              Text(
                'Drive backups: ${backupState.driveBackups.length}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Auto-backup: ${backupState.settings.autoBackupFrequency.name}',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Last backup ID: ${backupState.settings.lastBackupId ?? '—'}',
                style: const TextStyle(fontSize: 12),
              ),
              if (backupState.isBackingUp) ...[
                const SizedBox(height: 6),
                Text(
                  'Progress: ${backupState.progress.stage}',
                  style: const TextStyle(fontSize: 12),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: backupState.isBackingUp
                  ? null
                  : () => ref
                        .read(backupProvider.notifier)
                        .createBackup(location: BackupStorageLocation.local),
              icon: const Icon(Icons.cloud_upload_outlined, size: 18),
              label: Text(
                backupState.isBackingUp ? 'Backing Up…' : 'Quick Local Backup',
              ),
            ),
            ElevatedButton.icon(
              onPressed:
                  (backupState.isRestoring || backupState.localBackups.isEmpty)
                  ? null
                  : () async {
                      final latestId = backupState.localBackups.first.backupId;
                      final success = await ref
                          .read(backupProvider.notifier)
                          .restoreBackup(latestId);
                      if (context.mounted) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                              success
                                  ? 'Restore completed!'
                                  : 'Restore failed.',
                            ),
                          ),
                        );
                      }
                    },
              icon: Icon(
                backupState.isRestoring ? Icons.hourglass_top : Icons.restore,
                size: 18,
              ),
              label: Text(
                backupState.isRestoring ? 'Restoring…' : 'Test Restore',
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => ref.read(backupProvider.notifier).refresh(),
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Backups'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/backup'),
              icon: const Icon(
                Icons.settings_backup_restore_outlined,
                size: 18,
              ),
              label: const Text('Open Backup Settings'),
            ),
          ],
        ),
        if (backupState.successMessage != null) ...[
          const SizedBox(height: 8),
          Text(
            backupState.successMessage!,
            style: TextStyle(fontSize: 12, color: Colors.green.shade700),
          ),
        ],
        if (backupState.errorMessage != null) ...[
          const SizedBox(height: 8),
          Text(
            backupState.errorMessage!,
            style: TextStyle(
              fontSize: 12,
              color: Theme.of(context).colorScheme.error,
            ),
          ),
        ],
      ],
    );
  }
}

class UserFacingPreviewDevSection extends StatefulWidget {
  const UserFacingPreviewDevSection({super.key});

  @override
  State<UserFacingPreviewDevSection> createState() =>
      _UserFacingPreviewDevSectionState();
}

class _UserFacingPreviewDevSectionState
    extends State<UserFacingPreviewDevSection> {
  final TextEditingController _phoneController = TextEditingController(
    text: '5551234567',
  );

  @override
  void dispose() {
    _phoneController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'User-Facing Preview Lab',
      icon: Icons.preview_outlined,
      children: [
        Text(
          'Preview Flutter ports of the same Android-facing UX additions: animated text, parallax cards, formatted phone fields, and QA lab shortcuts.',
          style: TextStyle(
            fontSize: 12,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.primaryContainer.withValues(alpha: 0.25),
            borderRadius: BorderRadius.circular(12),
          ),
          child: const TypewriterText(
            text:
                'NeuroComet parity preview: smooth, readable, and sensory-aware UI primitives.',
            delayPerCharacter: Duration(milliseconds: 18),
          ),
        ),
        const SizedBox(height: 12),
        NeuroParallaxCard(
          scrollOffset: 24,
          itemExtent: 200,
          index: 0,
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.primaryPurple, AppColors.secondaryTeal],
              ),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Parallax Preview',
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  'Shared motion treatment used across feed, explore, and detail surfaces.',
                  style: TextStyle(color: Colors.white),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),
        PhoneNumberField(
          controller: _phoneController,
          format: PhoneFormat.international,
          labelText: 'Phone Number Preview',
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.push('/settings/feature-test'),
              icon: const Icon(Icons.science_outlined, size: 18),
              label: const Text('Open Feature Test Lab'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.go(_feedTabRoute),
              icon: const Icon(Icons.dynamic_feed_outlined, size: 18),
              label: const Text('Open Feed'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/explore'),
              icon: const Icon(Icons.explore_outlined, size: 18),
              label: const Text('Open Explore'),
            ),
          ],
        ),
      ],
    );
  }
}

class WidgetsDevSection extends StatefulWidget {
  const WidgetsDevSection({super.key});

  @override
  State<WidgetsDevSection> createState() => _WidgetsDevSectionState();
}

class _WidgetsDevSectionState extends State<WidgetsDevSection> {
  bool _previewHighContrast = false;
  bool _previewReducedMotion = false;

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Neurodivergent Widgets',
      icon: Icons.widgets,
      children: [
        const Text(
          'Test accessibility-focused widgets designed for neurodivergent users.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        SwitchListTile(
          title: const Text(
            'High Contrast Mode',
            style: TextStyle(fontSize: 14),
          ),
          subtitle: const Text(
            'Preview widgets in high contrast',
            style: TextStyle(fontSize: 11),
          ),
          value: _previewHighContrast,
          onChanged: (v) => setState(() => _previewHighContrast = v),
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        SwitchListTile(
          title: const Text('Reduced Motion', style: TextStyle(fontSize: 14)),
          subtitle: const Text(
            'Disable animations in preview',
            style: TextStyle(fontSize: 11),
          ),
          value: _previewReducedMotion,
          onChanged: (v) => setState(() => _previewReducedMotion = v),
          dense: true,
          contentPadding: EdgeInsets.zero,
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              showDialog(
                context: context,
                useRootNavigator: true,
                builder: (context) => Scaffold(
                  appBar: AppBar(
                    title: const Text('Widget Dashboard Preview'),
                    actions: [
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(context),
                      ),
                    ],
                  ),
                  body: NeurodivergentWidgetDashboard(
                    highContrast: _previewHighContrast,
                    reducedMotion: _previewReducedMotion,
                  ),
                ),
              );
            },
            icon: const Icon(Icons.preview),
            label: const Text('Preview Widget Dashboard'),
          ),
        ),
        const Divider(height: 32),
        const Text(
          'Quick Previews',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
        ),
        const SizedBox(height: 8),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.25),
            borderRadius: BorderRadius.circular(12),
          ),
          child: const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TypewriterText(
                text: 'TypewriterText parity preview',
                delayPerCharacter: Duration(milliseconds: 20),
              ),
              SizedBox(height: 8),
              SmoothTypewriterText(
                text: 'SmoothTypewriterText accessibility preview',
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class ImagesDevSection extends StatefulWidget {
  const ImagesDevSection({super.key});

  @override
  State<ImagesDevSection> createState() => _ImagesDevSectionState();
}

class _ImagesDevSectionState extends State<ImagesDevSection> {
  final ImagePicker _picker = ImagePicker();
  XFile? _selectedImage;
  bool _showEditor = false;

  Future<void> _pickImage() async {
    final image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      setState(() {
        _selectedImage = image;
        _showEditor = true;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_showEditor) {
      // Mock editor "overlay" for parity
      return DevTestCard(
        title: 'Image Customization Editor',
        icon: Icons.edit,
        children: [
          Container(
            height: 200,
            width: double.infinity,
            decoration: BoxDecoration(
              color: Colors.black12,
              borderRadius: BorderRadius.circular(12),
              image: _selectedImage != null
                  ? DecorationImage(
                      image: FileImage(File(_selectedImage!.path)),
                      fit: BoxFit.contain,
                    )
                  : null,
            ),
            child: _selectedImage == null
                ? const Center(child: Text('Blank Canvas (Drawing Mode)'))
                : null,
          ),
          const SizedBox(height: 16),
          const Text(
            'Editor Tools Parity:',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: const [
              _MiniCapabilityChip(label: 'Filters', icon: Icons.auto_awesome),
              _MiniCapabilityChip(label: 'Stickers', icon: Icons.sticky_note_2),
              _MiniCapabilityChip(label: 'Draw', icon: Icons.brush),
              _MiniCapabilityChip(label: 'Text', icon: Icons.text_fields),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  onPressed: () {
                    setState(() => _showEditor = false);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Image saved!')),
                    );
                  },
                  child: const Text('Save'),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: OutlinedButton(
                  onPressed: () => setState(() => _showEditor = false),
                  child: const Text('Cancel'),
                ),
              ),
            ],
          ),
        ],
      );
    }

    return DevTestCard(
      title: 'Image Customization',
      icon: Icons.photo_filter,
      children: [
        const Text(
          'Test image filters, stickers, and drawing tools parity.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: _pickImage,
                icon: const Icon(Icons.image, size: 18),
                label: const Text('Select'),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () => setState(() {
                  _selectedImage = null;
                  _showEditor = true;
                }),
                icon: const Icon(Icons.brush, size: 18),
                label: const Text('Draw'),
              ),
            ),
          ],
        ),
        const Divider(height: 24),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.push('/create-story'),
              icon: const Icon(Icons.add_photo_alternate_outlined, size: 18),
              label: const Text('Story Creator'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/feature-test'),
              icon: const Icon(Icons.preview_outlined, size: 18),
              label: const Text('Preview Lab'),
            ),
          ],
        ),
      ],
    );
  }
}

class ExploreViewsDevSection extends ConsumerStatefulWidget {
  const ExploreViewsDevSection({super.key});

  @override
  ConsumerState<ExploreViewsDevSection> createState() =>
      _ExploreViewsDevSectionState();
}

class _ExploreViewsDevSectionState
    extends ConsumerState<ExploreViewsDevSection> {
  String _selectedLayout = 'Standard';

  @override
  Widget build(BuildContext context) {
    final layouts = ['Standard', 'Grid', 'Compact', 'Large Cards'];
    final postsAsync = ref.watch(feedProvider);
    final previewPosts =
        postsAsync.value?.take(12).toList() ?? const <post_models.Post>[];

    return DevTestCard(
      title: 'Explore Page Views',
      icon: Icons.explore,
      children: [
        const Text(
          'Test different view layouts for the Explore page parity.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        const Text(
          'Layout:',
          style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 4),
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: layouts.map((layout) {
              final isSelected = _selectedLayout == layout;
              return Padding(
                padding: const EdgeInsets.only(right: 8),
                child: FilterChip(
                  label: Text(layout, style: const TextStyle(fontSize: 10)),
                  selected: isSelected,
                  onSelected: (v) => setState(() => _selectedLayout = layout),
                ),
              );
            }).toList(),
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: postsAsync.when(
            data: (posts) {
              final count = posts.length;
              final mediaCount = posts
                  .where((post) => (post.mediaUrls?.isNotEmpty ?? false))
                  .length;
              final locationCount = posts
                  .where((post) => (post.locationTag ?? '').isNotEmpty)
                  .length;
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Preview source: $count feed posts',
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Media-rich posts: $mediaCount',
                    style: const TextStyle(fontSize: 12),
                  ),
                  Text(
                    'Location-tagged posts: $locationCount',
                    style: const TextStyle(fontSize: 12),
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Preview now renders real mock/live feed content instead of a placeholder screen.',
                    style: TextStyle(fontSize: 12),
                  ),
                ],
              );
            },
            loading: () => const Row(
              children: [
                SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
                SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'Loading feed posts for preview…',
                    style: TextStyle(fontSize: 12),
                  ),
                ),
              ],
            ),
            error: (e, _) => Text(
              'Preview source unavailable: $e',
              style: const TextStyle(fontSize: 12, color: Colors.red),
            ),
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: previewPosts.isEmpty
                ? null
                : () {
                    Navigator.of(context).push(
                      MaterialPageRoute<void>(
                        fullscreenDialog: true,
                        builder: (context) => _ExploreLayoutPreviewScreen(
                          layoutName: _selectedLayout,
                          posts: previewPosts,
                        ),
                      ),
                    );
                  },
            icon: const Icon(Icons.preview),
            label: Text('Preview $_selectedLayout'),
          ),
        ),
        const Divider(height: 24),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => _openDevRoute(context, '/explore'),
              icon: const Icon(Icons.explore_outlined, size: 18),
              label: const Text('Open Explore'),
            ),
            OutlinedButton.icon(
              onPressed: () => _openDevRoute(context, _feedTabRoute),
              icon: const Icon(Icons.dynamic_feed_outlined, size: 18),
              label: const Text('Open Feed'),
            ),
          ],
        ),
      ],
    );
  }
}

class MultiMediaDevSection extends ConsumerWidget {
  const MultiMediaDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final postsAsync = ref.watch(feedProvider);

    return DevTestCard(
      title: 'Multi-Media Posts',
      icon: Icons.collections,
      children: [
        const Text(
          'Test posts with multiple media items (images/video/audio).',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        postsAsync.when(
          data: (posts) {
            final multiMediaPosts = posts.take(3).toList();
            if (multiMediaPosts.isEmpty) {
              return const Text(
                'No mock posts available',
                style: TextStyle(fontSize: 11),
              );
            }
            return Column(
              children: multiMediaPosts
                  .map(
                    (post) => Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Text(
                              post.authorName,
                              style: const TextStyle(fontSize: 12),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          Text(
                            '📷 ${post.mediaUrls?.length ?? 0} items',
                            style: TextStyle(
                              fontSize: 11,
                              color: Theme.of(context).colorScheme.primary,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  )
                  .toList(),
            );
          },
          loading: () => const SizedBox(
            height: 40,
            child: Center(child: CircularProgressIndicator(strokeWidth: 2)),
          ),
          error: (e, _) => Text(
            'Error loading mock posts: $e',
            style: const TextStyle(fontSize: 11, color: Colors.red),
          ),
        ),
        const Divider(height: 24),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.go(_feedTabRoute),
              icon: const Icon(Icons.slideshow_outlined, size: 18),
              label: const Text('Open Feed Media'),
            ),
            OutlinedButton.icon(
              onPressed: () => _openActiveCallPreview(context),
              icon: const Icon(Icons.call_outlined, size: 18),
              label: const Text('Open Call UI'),
            ),
          ],
        ),
      ],
    );
  }
}

class NavigationDevSection extends StatelessWidget {
  const NavigationDevSection({super.key});

  @override
  Widget build(BuildContext context) {
    final responsive = context.responsive;
    final theme = Theme.of(context);
    final currentRoute = GoRouterState.of(context).matchedLocation;
    final navMode = _navigationModeLabel(responsive);
    final usesShellRail = kIsWeb && responsive.useNavigationRail;

    final routes = <(String, String, IconData)>[
      ('Feed', _feedTabRoute, Icons.dynamic_feed_outlined),
      ('Explore', '/explore', Icons.explore_outlined),
      ('Messages', '/messages', Icons.chat_bubble_outline),
      ('Notifications', '/notifications', Icons.notifications_outlined),
      ('Settings', '/settings', Icons.settings_outlined),
    ];

    return DevTestCard(
      title: 'Adaptive Navigation',
      icon: Icons.dashboard,
      children: [
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: theme.colorScheme.primaryContainer.withValues(alpha: 0.2),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Navigation', style: theme.textTheme.labelSmall),
                  Text(
                    navMode,
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text('Content', style: theme.textTheme.labelSmall),
                  Text(
                    responsive.deviceType.name.toUpperCase(),
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Text(
          'Current route: $currentRoute\nActual shell behavior: ${usesShellRail ? 'web rail/sidebar navigation' : 'bottom navigation'}',
          style: const TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 8),
        _AdaptiveNavItem(
          label: 'BOTTOM_NAV',
          desc: 'All native builds and web widths < 600px',
          isActive: !usesShellRail,
        ),
        _AdaptiveNavItem(
          label: 'NAV_RAIL',
          desc: 'Web 600–900px layouts use compact rail',
          isActive: usesShellRail && !responsive.isDesktop,
        ),
        _AdaptiveNavItem(
          label: 'EXTENDED_SIDEBAR',
          desc: 'Web desktop layouts use rail/sidebar variants',
          isActive: usesShellRail && responsive.isDesktop,
        ),
        const Divider(height: 24),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: routes.map((entry) {
            return OutlinedButton.icon(
              onPressed: () => _openDevRoute(context, entry.$2),
              icon: Icon(entry.$3, size: 18),
              label: Text(entry.$1),
            );
          }).toList(),
        ),
      ],
    );
  }
}

class _AdaptiveNavItem extends StatelessWidget {
  final String label;
  final String desc;
  final bool isActive;

  const _AdaptiveNavItem({
    required this.label,
    required this.desc,
    required this.isActive,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Text(
          isActive ? '▶ ' : '  ',
          style: TextStyle(color: theme.colorScheme.primary, fontSize: 10),
        ),
        Text(
          '$label — $desc',
          style: TextStyle(
            fontSize: 11,
            fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
            color: isActive
                ? theme.colorScheme.primary
                : theme.colorScheme.onSurfaceVariant,
          ),
        ),
      ],
    );
  }
}

class DialogsDevSection extends StatefulWidget {
  const DialogsDevSection({super.key});

  @override
  State<DialogsDevSection> createState() => _DialogsDevSectionState();
}

class _DialogsDevSectionState extends State<DialogsDevSection> {
  Future<void> _showLoadingDialog() async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const AlertDialog(
        content: Row(
          children: [
            CircularProgressIndicator(),
            SizedBox(width: 16),
            Expanded(child: Text('Processing…')),
          ],
        ),
      ),
    );
    await Future<void>.delayed(const Duration(milliseconds: 1200));
    if (!mounted) return;
    Navigator.of(context, rootNavigator: true).pop();
  }

  @override
  Widget build(BuildContext context) {
    final inputController = TextEditingController();
    return DevTestCard(
      title: 'Dialogs',
      icon: Icons.chat,
      children: [
        const Text(
          'Preview message, input, choice, and loading dialogs similar to the Android dialog test section.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('Message Dialog'),
                    content: const Text(
                      'This is a neurodivergent-friendly message dialog preview.',
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Got it'),
                      ),
                    ],
                  ),
                );
              },
              child: const Text('Message'),
            ),
            ElevatedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('Input Dialog'),
                    content: TextField(
                      controller: inputController,
                      decoration: const InputDecoration(
                        labelText: 'Type something',
                      ),
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Cancel'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Save'),
                      ),
                    ],
                  ),
                );
              },
              child: const Text('Input'),
            ),
            ElevatedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('Choice Dialog'),
                    content: const Text(
                      'Choose the option that best matches your current state.',
                    ),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Happy'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Calm'),
                      ),
                    ],
                  ),
                );
              },
              child: const Text('Choice'),
            ),
            ElevatedButton(
              onPressed: _showLoadingDialog,
              child: const Text('Loading'),
            ),
          ],
        ),
      ],
    );
  }
}

class LocationSensorsDevSection extends StatefulWidget {
  const LocationSensorsDevSection({super.key});

  @override
  State<LocationSensorsDevSection> createState() =>
      _LocationSensorsDevSectionState();
}

class _LocationSensorsDevSectionState extends State<LocationSensorsDevSection> {
  PermissionStatus? _locationStatus;

  @override
  void initState() {
    super.initState();
    _loadStatus();
  }

  Future<void> _loadStatus() async {
    final status = await Permission.location.status;
    if (!mounted) return;
    setState(() => _locationStatus = status);
  }

  Future<void> _requestLocation() async {
    final status = await Permission.location.request();
    if (!mounted) return;
    setState(() => _locationStatus = status);
  }

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Location & Sensors',
      icon: Icons.sensors,
      children: [
        Text(
          'Platform status preview for location permissions and host sensor readiness.',
          style: TextStyle(
            fontSize: 12,
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Platform: ${Platform.operatingSystem}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              Text(
                'Location permission: ${_locationStatus ?? 'unknown'}',
                style: const TextStyle(fontSize: 12),
              ),
              const Text(
                'Advanced GPS/sensor streaming remains platform-specific, but permission-state validation is mirrored here.',
                style: TextStyle(fontSize: 12),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: _requestLocation,
              icon: const Icon(Icons.my_location_outlined, size: 18),
              label: const Text('Request Location'),
            ),
            OutlinedButton.icon(
              onPressed: _loadStatus,
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Status'),
            ),
          ],
        ),
      ],
    );
  }
}

class SupabaseDbTestingDevSection extends StatefulWidget {
  const SupabaseDbTestingDevSection({super.key});

  @override
  State<SupabaseDbTestingDevSection> createState() =>
      _SupabaseDbTestingDevSectionState();
}

class _SupabaseDbTestingDevSectionState
    extends State<SupabaseDbTestingDevSection> {
  final Map<String, int?> _tableCounts = {};
  bool _loading = false;

  final List<String> _tables = List<String>.from(_requiredSupabaseTables);

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  Future<void> _refresh() async {
    if (!SupabaseService.isInitialized) {
      if (mounted) {
        setState(() {
          for (final table in _tables) {
            _tableCounts[table] = null;
          }
          _loading = false;
        });
      }
      return;
    }
    setState(() => _loading = true);
    try {
      for (final table in _tables) {
        try {
          final response = await SupabaseService.client
              .from(table)
              .select('id')
              .count(CountOption.exact);
          if (!mounted) return;
          setState(() => _tableCounts[table] = response.count);
        } catch (_) {
          if (!mounted) return;
          setState(() => _tableCounts[table] = null);
        }
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DevTestCard(
      title: 'Supabase DB Testing',
      icon: Icons.cloud_sync,
      children: [
        const Text(
          'Table-health parity for all major backend collections.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 8),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
          decoration: BoxDecoration(
            color: SupabaseService.isInitialized
                ? Colors.green.withValues(alpha: 0.15)
                : Colors.red.withValues(alpha: 0.15),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                SupabaseService.isInitialized
                    ? Icons.cloud_done
                    : Icons.cloud_off,
                size: 16,
                color: SupabaseService.isInitialized
                    ? Colors.green
                    : Colors.red,
              ),
              const SizedBox(width: 6),
              Text(
                SupabaseService.isInitialized
                    ? 'SDK Initialized'
                    : 'SDK NOT Initialized',
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  color: SupabaseService.isInitialized
                      ? Colors.green
                      : Colors.red,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        if (_tableCounts.isNotEmpty)
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest.withValues(
                alpha: 0.3,
              ),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: _tableCounts.entries
                  .map(
                    (e) => Padding(
                      padding: const EdgeInsets.symmetric(vertical: 2),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            e.key,
                            style: const TextStyle(
                              fontSize: 11,
                              fontFamily: 'monospace',
                            ),
                          ),
                          Text(
                            e.value?.toString() ?? '—',
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.bold,
                              color: e.value != null
                                  ? theme.colorScheme.primary
                                  : Colors.red,
                            ),
                          ),
                        ],
                      ),
                    ),
                  )
                  .toList(),
            ),
          ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: _loading ? null : _refresh,
            icon: _loading
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.refresh, size: 18),
            label: Text(_loading ? 'Fetching Counts…' : 'Fetch All Row Counts'),
          ),
        ),
      ],
    );
  }
}

class StoriesTestingDevSection extends ConsumerStatefulWidget {
  const StoriesTestingDevSection({super.key});

  @override
  ConsumerState<StoriesTestingDevSection> createState() =>
      _StoriesTestingDevSectionState();
}

class _StoriesTestingDevSectionState
    extends ConsumerState<StoriesTestingDevSection> {
  String? _statusMessage;
  String? _supabaseStatus;

  void _setStatus(String msg) {
    if (!mounted) return;
    setState(() => _statusMessage = msg);
  }

  // ── Auth diagnostic ──
  Map<String, dynamic> _getAuthDiagnostic() {
    final supabaseAvailable = SupabaseService.isInitialized;
    final userId = SupabaseService.currentUser?.id;
    final hasSession = supabaseAvailable && SupabaseService.isAuthenticated;

    if (!supabaseAvailable) {
      return {
        'text': '🔴 Supabase not configured → stories are local-only',
        'color': Colors.red,
      };
    } else if (userId == null) {
      return {
        'text':
            '🟡 Supabase available but not signed in → stories are local-only',
        'color': Colors.orange,
      };
    } else if (!hasSession) {
      return {
        'text':
            '🟡 Signed in (${userId.substring(0, userId.length.clamp(0, 8))}…) but no active JWT → stories are local-only',
        'color': Colors.orange,
      };
    } else {
      return {
        'text':
            '🟢 Authenticated (${userId.substring(0, userId.length.clamp(0, 8))}…) → stories will persist to Supabase',
        'color': Theme.of(context).colorScheme.primary,
      };
    }
  }

  @override
  Widget build(BuildContext context) {
    final options = ref.watch(devOptionsProvider);
    final storyGroups = ref
        .watch(storiesProvider)
        .maybeWhen(
          data: (value) => value,
          orElse: () => const <story_models.StoryGroup>[],
        );
    final totalStories = storyGroups.fold<int>(
      0,
      (sum, group) => sum + group.stories.length,
    );
    final notifier = ref.read(storiesProvider.notifier);
    final diag = _getAuthDiagnostic();
    final cs = Theme.of(context).colorScheme;

    return DevTestCard(
      title: 'Stories Testing',
      icon: Icons.auto_stories,
      children: [
        Text(
          'Inject test stories to verify the stories bar, viewer, progress bars, content types, deletion, and navigation.',
          style: TextStyle(fontSize: 12, color: cs.onSurfaceVariant),
        ),
        const SizedBox(height: 8),

        // ── Auth / connectivity diagnostic ──
        Text(
          diag['text'] as String,
          style: TextStyle(
            fontSize: 11,
            color: diag['color'] as Color,
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 12),

        // ── Stats card ──
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: cs.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Story groups: ${storyGroups.length}',
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              Text(
                'Total story items: $totalStories',
                style: const TextStyle(fontSize: 12),
              ),
              Text(
                'Story reactions enabled: ${options.enableStoryReactions}',
                style: const TextStyle(fontSize: 12),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),

        // ── Single Image Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddSingleImageStory();
              _setStatus('Added single-image story');
            },
            icon: const Icon(Icons.image, size: 18),
            label: const Text('Add Single Image Story'),
          ),
        ),
        const SizedBox(height: 6),

        // ── Multi-Item Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddMultiItemStory();
              _setStatus('Added multi-item story (3 pages)');
            },
            icon: const Icon(Icons.view_carousel, size: 18),
            label: const Text('Add Multi-Item Story (3 pages)'),
          ),
        ),
        const SizedBox(height: 6),

        // ── Text-Only Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddTextOnlyStory();
              _setStatus('Added text-only story');
            },
            icon: const Icon(Icons.text_fields, size: 18),
            label: const Text('Add Text-Only Story'),
          ),
        ),
        const SizedBox(height: 6),

        // ── Link Preview Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddLinkPreviewStory();
              _setStatus('Added link-preview story');
            },
            icon: const Icon(Icons.link, size: 18),
            label: const Text('Add Link Preview Story'),
          ),
        ),
        const SizedBox(height: 6),

        // ── Own Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddOwnStory();
              _setStatus('Added story as current user');
            },
            icon: const Icon(Icons.person, size: 18),
            label: const Text('Add Own Story (Your Story)'),
          ),
        ),
        const SizedBox(height: 6),

        // ── Kitchen Sink Story ──
        SizedBox(
          width: double.infinity,
          child: ElevatedButton.icon(
            onPressed: () {
              notifier.devAddKitchenSinkStory();
              _setStatus('Added kitchen-sink story (5 pages, mixed types)');
            },
            icon: const Icon(Icons.science, size: 18),
            label: const Text('Add Kitchen Sink Story (all types)'),
            style: ElevatedButton.styleFrom(
              backgroundColor: cs.tertiary,
              foregroundColor: cs.onTertiary,
            ),
          ),
        ),
        const SizedBox(height: 10),

        // ── Flood buttons ──
        Row(
          children: [
            Expanded(
              child: ElevatedButton(
                onPressed: () {
                  notifier.devFloodStories(10);
                  _setStatus('Flooded +10 stories');
                },
                child: const Text('Flood +10', style: TextStyle(fontSize: 12)),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ElevatedButton(
                onPressed: () {
                  notifier.devFloodStories(50);
                  _setStatus('Flooded +50 stories');
                },
                child: const Text('Flood +50', style: TextStyle(fontSize: 12)),
              ),
            ),
          ],
        ),
        const SizedBox(height: 6),

        // ── Clear All Dev Stories ──
        SizedBox(
          width: double.infinity,
          child: OutlinedButton.icon(
            onPressed: () async {
              await notifier.devClearStories();
              _setStatus('Cleared dev stories → back to mocks');
            },
            icon: Icon(Icons.delete_sweep, size: 18, color: cs.error),
            label: Text(
              'Clear All Dev Stories',
              style: TextStyle(color: cs.error),
            ),
            style: OutlinedButton.styleFrom(
              side: BorderSide(color: cs.error.withValues(alpha: 0.5)),
            ),
          ),
        ),
        const SizedBox(height: 12),

        // ── Extra type injection chips ──
        const Text(
          'Inject by Content Type',
          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11),
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            _StoryInjectChip(
              icon: Icons.text_fields,
              label: 'Text',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.text),
            ),
            _StoryInjectChip(
              icon: Icons.image,
              label: 'Photo',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.photo),
            ),
            _StoryInjectChip(
              icon: Icons.videocam,
              label: 'Video',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.video),
            ),
            _StoryInjectChip(
              icon: Icons.link,
              label: 'Link',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.link),
            ),
            _StoryInjectChip(
              icon: Icons.description,
              label: 'Document',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.document),
            ),
            _StoryInjectChip(
              icon: Icons.headphones,
              label: 'Audio',
              onTap: () =>
                  _injectTypedStory(story_models.StoryContentType.audio),
            ),
          ],
        ),
        const SizedBox(height: 12),

        // ── Quick actions ──
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () => ref.read(storiesProvider.notifier).refresh(),
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Stories'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/create-story'),
              icon: const Icon(Icons.add_circle_outline, size: 18),
              label: const Text('Create Story'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.go(_feedTabRoute),
              icon: const Icon(Icons.visibility_outlined, size: 18),
              label: const Text('Open Feed'),
            ),
          ],
        ),

        // ── Status messages ──
        if (_statusMessage != null) ...[
          const SizedBox(height: 8),
          Text(
            _statusMessage!,
            style: TextStyle(
              fontSize: 12,
              color: cs.primary,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
        if (_supabaseStatus != null) ...[
          const SizedBox(height: 4),
          Text(
            _supabaseStatus!,
            style: TextStyle(
              fontSize: 11,
              color: _supabaseStatus!.startsWith('✅')
                  ? cs.primary
                  : _supabaseStatus!.startsWith('❌')
                  ? cs.error
                  : cs.tertiary,
            ),
          ),
        ],
      ],
    );
  }

  void _injectTypedStory(story_models.StoryContentType type) {
    final now = DateTime.now();
    final typeName = type.name;
    ref
        .read(storiesProvider.notifier)
        .injectLocalStory(
          story_models.Story(
            id: 'test_${typeName}_${now.millisecondsSinceEpoch}',
            authorId: 'dev_test',
            authorName: 'Dev Tester',
            contentType: type,
            caption: 'Test $typeName story from dev tools',
            createdAt: now,
            expiresAt: now.add(const Duration(hours: 24)),
            backgroundColor: type == story_models.StoryContentType.text
                ? '#7C4DFF'
                : null,
            backgroundGradient: type == story_models.StoryContentType.text
                ? '["#7C4DFF", "#536DFE"]'
                : null,
            mediaUrl: type == story_models.StoryContentType.photo
                ? 'https://picsum.photos/seed/${now.millisecondsSinceEpoch}/1080/1920'
                : null,
            linkPreview: type == story_models.StoryContentType.link
                ? const story_models.LinkPreviewData(
                    url: 'https://example.com',
                    title: 'Example Link',
                    description: 'A test link story',
                    siteName: 'Example',
                  )
                : null,
            fileName: type == story_models.StoryContentType.document
                ? 'test_document.pdf'
                : type == story_models.StoryContentType.audio
                ? 'audio_story.mp3'
                : null,
            fileSize: type == story_models.StoryContentType.document
                ? 1024000
                : null,
            durationSeconds: type == story_models.StoryContentType.audio
                ? 125
                : null,
          ),
        );
    _setStatus('Injected $typeName story');
  }
}

class _StoryInjectChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _StoryInjectChip({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ActionChip(
      avatar: Icon(icon, size: 16),
      label: Text(label, style: const TextStyle(fontSize: 11)),
      onPressed: onTap,
    );
  }
}

class GamesDevSection extends StatefulWidget {
  const GamesDevSection({super.key});

  @override
  State<GamesDevSection> createState() => _GamesDevSectionState();
}

class _GamesDevSectionState extends State<GamesDevSection> {
  late Future<RewardSnapshot> _rewardSnapshotFuture;

  @override
  void initState() {
    super.initState();
    _refreshRewards();
  }

  void _refreshRewards() {
    _rewardSnapshotFuture = BadgeManager.getRewardSnapshot();
  }

  Future<void> _applyRewardProgress(
    BuildContext context,
    String badgeId,
    int amount,
    String label,
  ) async {
    final unlockedNow = await BadgeManager.addProgress(badgeId, amount);
    if (!mounted) return;
    setState(_refreshRewards);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          unlockedNow
              ? '$label unlocked a badge reward'
              : '$label progress added',
        ),
      ),
    );
  }

  Future<void> _resetRewards(BuildContext context) async {
    await BadgeManager.resetAll();
    if (!mounted) return;
    setState(_refreshRewards);
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('Reward progress reset')));
  }

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Games',
      icon: Icons.sports_esports,
      children: [
        const Text(
          'Launch the Flutter games hub and badge surfaces that parallel the Android games test section.',
          style: TextStyle(fontSize: 12),
        ),
        const SizedBox(height: 12),
        const Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            _MiniCapabilityChip(
              label: 'Bubble Pop',
              icon: Icons.bubble_chart_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Fidget Spinner',
              icon: Icons.rotate_right_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Color Flow',
              icon: Icons.gradient_outlined,
            ),
            _MiniCapabilityChip(label: 'Breathing', icon: Icons.air_outlined),
            _MiniCapabilityChip(
              label: 'Pattern Tap',
              icon: Icons.grid_on_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Infinity Draw',
              icon: Icons.draw_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Sensory Rain',
              icon: Icons.water_drop_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Zen Sand',
              icon: Icons.landscape_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Emotion Garden',
              icon: Icons.local_florist_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Texture Tiles',
              icon: Icons.view_module_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Sound Garden',
              icon: Icons.music_note_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Stim Sequencer',
              icon: Icons.equalizer_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Safe Space',
              icon: Icons.shield_moon_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Worry Jar',
              icon: Icons.water_drop_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Constellation',
              icon: Icons.auto_awesome_outlined,
            ),
            _MiniCapabilityChip(
              label: 'Mood Mixer',
              icon: Icons.palette_outlined,
            ),
          ],
        ),
        const SizedBox(height: 12),
        FutureBuilder<RewardSnapshot>(
          future: _rewardSnapshotFuture,
          builder: (context, snapshot) {
            if (!snapshot.hasData) {
              return const Padding(
                padding: EdgeInsets.symmetric(vertical: 8),
                child: LinearProgressIndicator(minHeight: 4),
              );
            }

            final rewardSnapshot = snapshot.data!;
            final levelInfo = rewardSnapshot.levelInfo;
            return Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(
                  context,
                ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Rewards · Level ${levelInfo.level} · ${rewardSnapshot.totalXp} XP',
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Badges: ${rewardSnapshot.unlockedCount}/${rewardSnapshot.totalBadges}',
                    style: TextStyle(
                      fontSize: 12,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(
                    value: levelInfo.progress,
                    minHeight: 6,
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${levelInfo.xpNeededForNextLevel} XP until next level',
                    style: TextStyle(
                      fontSize: 11,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  if (rewardSnapshot.recentlyUnlocked.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Text(
                      'Recent: ${rewardSnapshot.recentlyUnlocked.map((badge) => badge.name).join(' • ')}',
                      style: TextStyle(
                        fontSize: 11,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                    ),
                  ],
                ],
              ),
            );
          },
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () =>
                  _applyRewardProgress(context, 'first_post', 1, 'First Post'),
              icon: const Icon(Icons.workspace_premium_outlined, size: 18),
              label: const Text('Unlock First Post'),
            ),
            OutlinedButton.icon(
              onPressed: () => _applyRewardProgress(
                context,
                'conversation_starter',
                1,
                'Conversation Starter',
              ),
              icon: const Icon(Icons.chat_bubble_outline, size: 18),
              label: const Text('+1 Conversation'),
            ),
            OutlinedButton.icon(
              onPressed: () =>
                  _applyRewardProgress(context, 'game_lover', 1, 'Game Lover'),
              icon: const Icon(Icons.sports_esports_outlined, size: 18),
              label: const Text('+1 Game'),
            ),
            OutlinedButton.icon(
              onPressed: () => _resetRewards(context),
              icon: const Icon(Icons.restart_alt, size: 18),
              label: const Text('Reset Rewards'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.push('/games'),
              icon: const Icon(Icons.sports_esports_outlined, size: 18),
              label: const Text('Open Games Hub'),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/badges'),
              icon: const Icon(Icons.workspace_premium_outlined, size: 18),
              label: const Text('Open Badges'),
            ),
          ],
        ),
      ],
    );
  }
}

class _MiniCapabilityChip extends StatelessWidget {
  final String label;
  final IconData icon;

  const _MiniCapabilityChip({required this.label, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(
        color: Theme.of(
          context,
        ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.35),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16),
          const SizedBox(width: 6),
          Text(
            label,
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
          ),
        ],
      ),
    );
  }
}

class _ModerationHeuristicReport {
  final String status;
  final List<String> blockedMatches;
  final List<String> flaggedMatches;

  const _ModerationHeuristicReport({
    required this.status,
    required this.blockedMatches,
    required this.flaggedMatches,
  });
}

_ModerationHeuristicReport _analyzeModerationText(String text) {
  final normalized = text.toLowerCase();
  const blockedKeywords = [
    'threat',
    'illegal harm',
    'predator',
    'underage target',
    'hate speech',
  ];
  const flaggedKeywords = [
    'spam',
    'scam',
    'violence',
    'attack',
    'bad word',
    'scam link',
  ];

  final blockedMatches = blockedKeywords.where(normalized.contains).toList();
  final flaggedMatches = flaggedKeywords.where(normalized.contains).toList();
  final status = blockedMatches.isNotEmpty
      ? 'blocked'
      : flaggedMatches.isNotEmpty
      ? 'flagged'
      : 'clean';

  return _ModerationHeuristicReport(
    status: status,
    blockedMatches: blockedMatches,
    flaggedMatches: flaggedMatches,
  );
}

Color _moderationStatusColor(BuildContext context, String status) {
  switch (status) {
    case 'blocked':
      return Theme.of(context).colorScheme.error;
    case 'flagged':
      return Theme.of(context).colorScheme.tertiary;
    default:
      return Colors.green;
  }
}

// ═══════════════════════════════════════════════════════════════
//  NOTIFICATIONS TESTING
// ═══════════════════════════════════════════════════════════════

class NotificationsDevSection extends ConsumerStatefulWidget {
  const NotificationsDevSection({super.key});

  @override
  ConsumerState<NotificationsDevSection> createState() =>
      _NotificationsDevSectionState();
}

class _NotificationsDevSectionState
    extends ConsumerState<NotificationsDevSection> {
  String _status = '';

  void _setStatus(String s, {bool isError = false}) {
    setState(() => _status = s);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(s),
        behavior: SnackBarBehavior.floating,
        backgroundColor: isError ? Theme.of(context).colorScheme.error : null,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final notifState = ref.watch(notificationsProvider);
    final unreadCount = ref.watch(unreadNotificationCountProvider);
    final totalCount = notifState.maybeWhen(
      data: (l) => l.length,
      orElse: () => 0,
    );
    final channelCount = NotificationChannelsService()
        .getAllChannels()
        .values
        .fold<int>(0, (sum, group) => sum + group.length);

    return DevTestCard(
      title: 'Notifications',
      icon: Icons.notifications_active,
      children: [
        // Status display
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Total: $totalCount  |  Unread: $unreadCount',
                style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                'Channels registered: $channelCount',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'State: ${notifState.isLoading
                    ? "Loading"
                    : notifState.hasError
                    ? "Error"
                    : "Loaded"}',
                style: const TextStyle(fontSize: 11),
              ),
              if (_status.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(
                  _status,
                  style: TextStyle(
                    fontSize: 11,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 12),

        // Permission check
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () async {
                final status = await Permission.notification.status;
                _setStatus('Permission: ${status.name}');
              },
              icon: const Icon(Icons.shield_outlined, size: 16),
              label: const Text(
                'Check Permission',
                style: TextStyle(fontSize: 12),
              ),
            ),
            ElevatedButton.icon(
              onPressed: () async {
                final status = await Permission.notification.request();
                _setStatus('Requested → ${status.name}');
              },
              icon: const Icon(Icons.notifications_none, size: 16),
              label: const Text(
                'Request Permission',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Inject mock notifications
        const DevSectionHeader(
          title: 'Inject Notifications',
          icon: Icons.add_alert,
        ),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () async {
                final notificationService = NotificationService();
                const title = 'Flutter test alert';
                const body =
                    'Queued a local test notification and added it to the in-app inbox.';
                final payload = notificationService.encodeNotificationPayload(
                  type: NotificationType.message,
                  actionUrl: '/messages',
                  actorId: 'dev_focus_companion',
                  actorName: 'Focus Companion',
                  actorAvatarUrl: 'https://i.pravatar.cc/150?u=focus_companion',
                  targetId: 'dev_focus_companion',
                  targetType: 'conversation',
                  title: title,
                  message: body,
                );
                await notificationService.showLocalNotification(
                  title: title,
                  body: body,
                  payload: payload,
                );
                final imported = await ref
                    .read(notificationsProvider.notifier)
                    .importQueuedNotifications();
                _setStatus(
                  imported > 0
                      ? 'Queued and imported $imported local test notification${imported == 1 ? '' : 's'}'
                      : 'Queued local test notification, but nothing was imported',
                  isError: imported == 0,
                );
              },
              icon: const Icon(Icons.notifications_active_outlined, size: 16),
              label: const Text(
                'Local Test Alert',
                style: TextStyle(fontSize: 12),
              ),
            ),
            _notifButton(
              'Like',
              NotificationType.like,
              'liked your post',
              Icons.favorite,
            ),
            _notifButton(
              'Comment',
              NotificationType.comment,
              'commented: "Great post!"',
              Icons.comment,
            ),
            _notifButton(
              'Follow',
              NotificationType.follow,
              'started following you',
              Icons.person_add,
            ),
            _notifButton(
              'Mention',
              NotificationType.mention,
              'mentioned you',
              Icons.alternate_email,
            ),
            _notifButton(
              'DM Ping',
              NotificationType.message,
              'sent a new message about tonight\'s focus sprint',
              Icons.chat_bubble,
              actionUrl: '/messages',
              title: 'New message',
              targetType: 'conversation',
            ),
            _notifButton(
              'Group Mention',
              NotificationType.mention,
              'tagged you in Late-Night Focus Lab 🌙',
              Icons.groups,
              actorName: 'Robin Lee',
              actorId: 'dev_robin',
              actionUrl: '/messages',
              targetType: 'conversation',
            ),
            _notifButton(
              'Location Tag',
              NotificationType.system,
              'New sensory-friendly check-in near Quiet Current Café 📍',
              Icons.place,
              title: 'Nearby community post',
              actorName: 'Community Radar',
              actorId: 'dev_radar',
              actionUrl: '/explore',
              targetType: 'location',
            ),
            _notifButton(
              'Achievement',
              NotificationType.achievement,
              'You earned a badge! 🎉',
              Icons.emoji_events,
            ),
            _notifButton(
              'System',
              NotificationType.system,
              'App update available',
              Icons.system_update,
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Actions
        const DevSectionHeader(title: 'Actions', icon: Icons.touch_app),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () {
                ref.read(notificationsProvider.notifier).markAllAsRead();
                _setStatus('All marked as read');
              },
              icon: const Icon(Icons.done_all, size: 16),
              label: const Text(
                'Mark All Read',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref.invalidate(notificationsProvider);
                _setStatus('Refreshed notifications');
              },
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text(
                'Force Refresh',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/notifications'),
              icon: const Icon(Icons.open_in_new, size: 16),
              label: const Text('Open Screen', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref
                    .read(notificationsProvider.notifier)
                    .clearDevNotifications();
                _setStatus('Cleared dev-injected notifications');
              },
              icon: const Icon(Icons.delete_sweep_outlined, size: 16),
              label: const Text(
                'Clear Dev Items',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _notifButton(
    String label,
    NotificationType type,
    String message,
    IconData icon, {
    String? title,
    String? actionUrl,
    String? actorId,
    String? actorName,
    String? actorAvatarUrl,
    String? targetId,
    String? targetType,
  }) {
    return OutlinedButton.icon(
      onPressed: () {
        final id = 'dev_${DateTime.now().millisecondsSinceEpoch}';
        final newNotif = AppNotification(
          id: id,
          type: type,
          title: title ?? _defaultNotificationTitle(label, type),
          message: message,
          actorId: actorId ?? 'dev_tester',
          actorName: actorName ?? 'Dev Tester',
          actorAvatarUrl:
              actorAvatarUrl ?? 'https://i.pravatar.cc/150?u=dev_tester',
          targetId: targetId,
          targetType: targetType,
          actionUrl: actionUrl ?? _defaultNotificationAction(type),
          isRead: false,
          createdAt: DateTime.now(),
        );
        // Directly inject into the notification state via notifier
        ref
            .read(notificationsProvider.notifier)
            .injectDevNotification(newNotif);
        _setStatus('Injected "$label" notification');
      },
      icon: Icon(icon, size: 14),
      label: Text(label, style: const TextStyle(fontSize: 11)),
    );
  }

  String _defaultNotificationTitle(String label, NotificationType type) {
    return switch (type) {
      NotificationType.like => 'New like',
      NotificationType.comment => 'New comment',
      NotificationType.follow => 'New follower',
      NotificationType.mention => 'You were mentioned',
      NotificationType.message => 'New message',
      NotificationType.achievement => 'Achievement unlocked',
      NotificationType.system => 'System update',
      NotificationType.repost => 'Post reposted',
      NotificationType.badge => 'Badge earned',
      NotificationType.welcome => 'Welcome to NeuroComet',
      NotificationType.safetyAlert => 'Safety alert',
    };
  }

  String _defaultNotificationAction(NotificationType type) {
    return switch (type) {
      NotificationType.follow => '/profile/dev_tester',
      NotificationType.message => '/messages',
      NotificationType.achievement || NotificationType.badge => '/profile',
      NotificationType.welcome => '/',
      NotificationType.system || NotificationType.safetyAlert => '/settings',
      NotificationType.like ||
      NotificationType.comment ||
      NotificationType.mention ||
      NotificationType.repost => '/post/dev_post',
    };
  }
}

// ═══════════════════════════════════════════════════════════════
//  PROFILE TESTING
// ═══════════════════════════════════════════════════════════════

class ProfileDevSection extends ConsumerStatefulWidget {
  const ProfileDevSection({super.key});

  @override
  ConsumerState<ProfileDevSection> createState() => _ProfileDevSectionState();
}

class _ProfileDevSectionState extends ConsumerState<ProfileDevSection> {
  String _status = '';

  void _setStatus(String s) => setState(() => _status = s);

  @override
  Widget build(BuildContext context) {
    final profileAsync = ref.watch(currentUserProfileProvider);

    return DevTestCard(
      title: 'Profile',
      icon: Icons.person,
      children: [
        // Profile info
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: profileAsync.when(
            data: (user) => Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Display Name: ${user.displayName}',
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Text(
                  'Username: @${user.username ?? "—"}',
                  style: const TextStyle(fontSize: 11),
                ),
                Text(
                  'Email: ${user.email ?? "—"}',
                  style: const TextStyle(fontSize: 11),
                ),
                Text(
                  'ID: ${user.id}',
                  style: const TextStyle(fontSize: 10, fontFamily: 'monospace'),
                ),
                Text(
                  'Bio: ${user.bio ?? "—"}',
                  style: const TextStyle(fontSize: 11),
                ),
                Text(
                  'Followers: ${user.followerCount}  |  Following: ${user.followingCount}',
                  style: const TextStyle(fontSize: 11),
                ),
                if (_status.isNotEmpty) ...[
                  const SizedBox(height: 4),
                  Text(
                    _status,
                    style: TextStyle(
                      fontSize: 11,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ],
              ],
            ),
            loading: () => const Text(
              'Loading profile...',
              style: TextStyle(fontSize: 12),
            ),
            error: (e, _) => Text(
              'Error: $e',
              style: const TextStyle(fontSize: 11, color: Colors.red),
            ),
          ),
        ),
        const SizedBox(height: 12),

        // Navigation
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.push('/profile'),
              icon: const Icon(Icons.person, size: 16),
              label: const Text('View Profile', style: TextStyle(fontSize: 12)),
            ),
            ElevatedButton.icon(
              onPressed: () => context.push('/edit-profile'),
              icon: const Icon(Icons.edit, size: 16),
              label: const Text('Edit Profile', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref.invalidate(currentUserProfileProvider);
                _setStatus('Profile refreshed');
              },
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text('Refresh', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () => context.push('/profile/fake_user_001'),
              icon: const Icon(Icons.person_search, size: 16),
              label: const Text(
                'View Other Profile',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/followers/current_user'),
              icon: const Icon(Icons.people, size: 16),
              label: const Text(
                'Followers List',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════
//  FEED INTERACTIONS TESTING
// ═══════════════════════════════════════════════════════════════

class FeedInteractionsDevSection extends ConsumerStatefulWidget {
  const FeedInteractionsDevSection({super.key});

  @override
  ConsumerState<FeedInteractionsDevSection> createState() =>
      _FeedInteractionsDevSectionState();
}

class _FeedInteractionsDevSectionState
    extends ConsumerState<FeedInteractionsDevSection> {
  String _status = '';

  void _setStatus(String s) => setState(() => _status = s);

  @override
  Widget build(BuildContext context) {
    final feedState = ref.watch(feedProvider);
    final postCount = feedState.maybeWhen(
      data: (l) => l.length,
      orElse: () => 0,
    );
    final locationTaggedCount =
        feedState.value
            ?.where((post) => (post.locationTag ?? '').isNotEmpty)
            .length ??
        0;

    return DevTestCard(
      title: 'Feed Interactions',
      icon: Icons.dynamic_feed,
      children: [
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Posts in feed: $postCount',
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                'Location-tagged posts: $locationTaggedCount',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'State: ${feedState.isLoading
                    ? "Loading"
                    : feedState.hasError
                    ? "Error"
                    : "Loaded"}',
                style: const TextStyle(fontSize: 11),
              ),
              if (_status.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(
                  _status,
                  style: TextStyle(
                    fontSize: 11,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 12),

        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () => context.push('/create-post'),
              icon: const Icon(Icons.add, size: 16),
              label: const Text('Create Post', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref.invalidate(feedProvider);
                _setStatus('Feed refreshed');
              },
              icon: const Icon(Icons.refresh, size: 16),
              label: const Text('Refresh Feed', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () => context.go(_feedTabRoute),
              icon: const Icon(Icons.open_in_new, size: 16),
              label: const Text('Open Feed', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
        const SizedBox(height: 8),

        const DevSectionHeader(
          title: 'Scenario Seeders',
          icon: Icons.auto_fix_high,
        ),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () {
                ref
                    .read(feedProvider.notifier)
                    .injectDevPost(_buildDevLocationTaggedPost());
                _setStatus('Injected location-tagged post scenario');
              },
              icon: const Icon(Icons.place_outlined, size: 16),
              label: const Text(
                'Inject Location Post',
                style: TextStyle(fontSize: 12),
              ),
            ),
            ElevatedButton.icon(
              onPressed: () {
                ref
                    .read(feedProvider.notifier)
                    .injectDevPost(_buildDevLowStimCheckInPost());
                _setStatus('Injected low-stimulation check-in post');
              },
              icon: const Icon(Icons.self_improvement_outlined, size: 16),
              label: const Text(
                'Inject Low-Stim Post',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref.read(feedProvider.notifier).clearDevInjectedPosts();
                _setStatus('Cleared dev-seeded posts');
              },
              icon: const Icon(Icons.delete_sweep_outlined, size: 16),
              label: const Text(
                'Clear Seeded Posts',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),

        const DevSectionHeader(title: 'Post Actions', icon: Icons.touch_app),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () {
                final posts = feedState.value;
                if (posts == null || posts.isEmpty) {
                  _setStatus('No posts in feed');
                  return;
                }
                final top = posts.first;
                _setStatus(
                  'Top post: "${top.content.substring(0, top.content.length.clamp(0, 50))}..."',
                );
              },
              icon: const Icon(Icons.info_outline, size: 14),
              label: const Text(
                'Inspect Top Post',
                style: TextStyle(fontSize: 11),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                final posts = feedState.value;
                if (posts == null || posts.isEmpty) {
                  _setStatus('No posts');
                  return;
                }
                context.push('/post/${posts.first.id}');
              },
              icon: const Icon(Icons.open_in_new, size: 14),
              label: const Text(
                'View Top Post',
                style: TextStyle(fontSize: 11),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════
//  ACCESSIBILITY & THEME TESTING
// ═══════════════════════════════════════════════════════════════

class AccessibilityThemeDevSection extends ConsumerWidget {
  const AccessibilityThemeDevSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);
    final highContrast = ref.watch(highContrastProvider);
    final animSettings = ref.watch(animationSettingsProvider);
    final fontSettings = ref.watch(fontSettingsProvider);

    return DevTestCard(
      title: 'Accessibility & Theme',
      icon: Icons.accessibility_new,
      children: [
        // Current state display
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Theme: ${themeMode.name}',
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                'High Contrast: $highContrast',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'Reduced Motion: ${animSettings.disableAll}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'Dyslexic Font: ${fontSettings.useDyslexicFont}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'Font Scale: ${(fontSettings.scale * 100).round()}%',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'Letter Spacing: ${fontSettings.letterSpacing.toStringAsFixed(1)}',
                style: const TextStyle(fontSize: 11),
              ),
              Text(
                'Line Height: ${fontSettings.lineHeight.toStringAsFixed(1)}x',
                style: const TextStyle(fontSize: 11),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),

        // Theme mode
        const DevSectionHeader(title: 'Theme Mode', icon: Icons.brightness_6),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            _themeChip(
              context,
              ref,
              'Light',
              ThemeMode.light,
              Icons.light_mode,
            ),
            _themeChip(context, ref, 'Dark', ThemeMode.dark, Icons.dark_mode),
            _themeChip(
              context,
              ref,
              'System',
              ThemeMode.system,
              Icons.settings_brightness,
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Toggles
        const DevSectionHeader(title: 'Toggles', icon: Icons.toggle_on),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            FilterChip(
              label: const Text(
                'High Contrast',
                style: TextStyle(fontSize: 11),
              ),
              selected: highContrast,
              onSelected: (v) =>
                  ref.read(highContrastProvider.notifier).setHighContrast(v),
            ),
            FilterChip(
              label: const Text(
                'Reduced Motion',
                style: TextStyle(fontSize: 11),
              ),
              selected: animSettings.disableAll,
              onSelected: (v) => ref
                  .read(animationSettingsProvider.notifier)
                  .setReducedMotion(v),
            ),
            FilterChip(
              label: const Text(
                'Dyslexic Font',
                style: TextStyle(fontSize: 11),
              ),
              selected: fontSettings.useDyslexicFont,
              onSelected: (v) =>
                  ref.read(fontSettingsProvider.notifier).setUseDyslexicFont(v),
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Font scale presets
        const DevSectionHeader(title: 'Font Scale', icon: Icons.text_fields),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            for (final scale in [0.8, 1.0, 1.2, 1.5])
              ChoiceChip(
                label: Text(
                  '${(scale * 100).round()}%',
                  style: const TextStyle(fontSize: 11),
                ),
                selected: (fontSettings.scale - scale).abs() < 0.05,
                onSelected: (_) =>
                    ref.read(fontSettingsProvider.notifier).setScale(scale),
              ),
          ],
        ),
        const SizedBox(height: 8),

        // Actions
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/accessibility'),
              icon: const Icon(Icons.open_in_new, size: 16),
              label: const Text(
                'Open Settings',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () {
                ref
                    .read(animationSettingsProvider.notifier)
                    .updateSettings(const AnimationSettings());
                ref.read(highContrastProvider.notifier).setHighContrast(false);
                ref
                    .read(fontSettingsProvider.notifier)
                    .updateSettings(const FontSettings());
                ref
                    .read(themeModeProvider.notifier)
                    .setThemeMode(ThemeMode.system);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text(
                      'All accessibility settings reset to defaults',
                    ),
                  ),
                );
              },
              icon: const Icon(Icons.restart_alt, size: 16),
              label: const Text('Reset All', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
      ],
    );
  }

  Widget _themeChip(
    BuildContext context,
    WidgetRef ref,
    String label,
    ThemeMode mode,
    IconData icon,
  ) {
    final current = ref.watch(themeModeProvider);
    return ChoiceChip(
      avatar: Icon(icon, size: 16),
      label: Text(label, style: const TextStyle(fontSize: 11)),
      selected: current == mode,
      onSelected: (_) =>
          ref.read(themeModeProvider.notifier).setThemeMode(mode),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
//  PRIVACY & ACCOUNT TESTING
// ═══════════════════════════════════════════════════════════════

class PrivacyAccountDevSection extends ConsumerStatefulWidget {
  const PrivacyAccountDevSection({super.key});

  @override
  ConsumerState<PrivacyAccountDevSection> createState() =>
      _PrivacyAccountDevSectionState();
}

class _PrivacyAccountDevSectionState
    extends ConsumerState<PrivacyAccountDevSection> {
  String _status = '';
  bool _loading = false;

  void _setStatus(String s) => setState(() {
    _status = s;
    _loading = false;
  });

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Privacy & Account',
      icon: Icons.shield,
      children: [
        if (_status.isNotEmpty)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(10),
            margin: const EdgeInsets.only(bottom: 12),
            decoration: BoxDecoration(
              color: Theme.of(
                context,
              ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              _status,
              style: const TextStyle(fontSize: 11, fontFamily: 'monospace'),
            ),
          ),

        const DevSectionHeader(
          title: 'Account Status',
          icon: Icons.account_circle,
        ),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: _loading
                  ? null
                  : () async {
                      setState(() => _loading = true);
                      try {
                        final status =
                            await SupabaseService.getCurrentAccountStatus();
                        if (status == null) {
                          _setStatus(
                            'No account status available (not authenticated)',
                          );
                          return;
                        }
                        _setStatus(
                          'Active: ${status.isActive}\n'
                          'Deletion Scheduled: ${status.hasDeletionScheduled}\n'
                          'Detox Active: ${status.isDetoxActive}\n'
                          'Detox Until: ${status.detoxUntil ?? "—"}',
                        );
                      } catch (e) {
                        _setStatus('Error: $e');
                      }
                    },
              icon: _loading
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.info_outline, size: 16),
              label: const Text('Check Status', style: TextStyle(fontSize: 12)),
            ),
            ElevatedButton.icon(
              onPressed: () {
                final user = SupabaseService.currentUser;
                _setStatus(
                  'User ID: ${user?.id ?? "null"}\n'
                  'Email: ${user?.email ?? "null"}\n'
                  'Created: ${user?.createdAt ?? "null"}\n'
                  'Session: ${SupabaseService.currentSession != null ? "active" : "none"}\n'
                  'Initialized: ${SupabaseService.isInitialized}\n'
                  'DevSkip: ${SupabaseService.devModeSkipAuth}',
                );
              },
              icon: const Icon(Icons.person_outline, size: 16),
              label: const Text('Auth Info', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
        const SizedBox(height: 8),

        const DevSectionHeader(title: 'Navigation', icon: Icons.open_in_new),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/privacy'),
              icon: const Icon(Icons.privacy_tip, size: 16),
              label: const Text(
                'Privacy Settings',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/blocked'),
              icon: const Icon(Icons.block, size: 16),
              label: const Text(
                'Blocked Users',
                style: TextStyle(fontSize: 12),
              ),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/wellbeing'),
              icon: const Icon(Icons.self_improvement, size: 16),
              label: const Text('Wellbeing', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/parental'),
              icon: const Icon(Icons.family_restroom, size: 16),
              label: const Text('Parental', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════
//  WELLBEING & DETOX TESTING
// ═══════════════════════════════════════════════════════════════

class WellbeingDetoxDevSection extends ConsumerStatefulWidget {
  const WellbeingDetoxDevSection({super.key});

  @override
  ConsumerState<WellbeingDetoxDevSection> createState() =>
      _WellbeingDetoxDevSectionState();
}

class _WellbeingDetoxDevSectionState
    extends ConsumerState<WellbeingDetoxDevSection> {
  Map<String, dynamic> _prefs = {};
  String _detoxStatus = '';

  @override
  void initState() {
    super.initState();
    _loadPrefs();
  }

  Future<void> _loadPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _prefs = {
        'break_reminders': prefs.getBool('break_reminders') ?? false,
        'quiet_hours': prefs.getBool('quiet_hours') ?? false,
        'push_enabled': prefs.getBool('push_enabled') ?? true,
        'break_reminder_minutes': prefs.getInt('break_reminder_minutes') ?? 30,
        'bedtime_mode': prefs.getBool('bedtime_mode') ?? false,
        'positivity_boost': prefs.getBool('positivity_boost') ?? true,
        'usage_stats': prefs.getBool('usage_stats') ?? true,
      };
    });
  }

  Future<void> _togglePref(String key, bool current) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(key, !current);
    await _loadPrefs();
  }

  @override
  Widget build(BuildContext context) {
    return DevTestCard(
      title: 'Wellbeing & Detox',
      icon: Icons.self_improvement,
      children: [
        // Prefs display
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: Theme.of(
              context,
            ).colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'SharedPreferences',
                style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              ..._prefs.entries.map(
                (e) => Text(
                  '${e.key}: ${e.value}',
                  style: const TextStyle(fontSize: 11, fontFamily: 'monospace'),
                ),
              ),
              if (_detoxStatus.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(
                  _detoxStatus,
                  style: TextStyle(
                    fontSize: 11,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 12),

        // Toggle prefs
        const DevSectionHeader(title: 'Quick Toggles', icon: Icons.toggle_on),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            FilterChip(
              label: const Text(
                'Break Reminders',
                style: TextStyle(fontSize: 11),
              ),
              selected: _prefs['break_reminders'] == true,
              onSelected: (_) => _togglePref(
                'break_reminders',
                _prefs['break_reminders'] == true,
              ),
            ),
            FilterChip(
              label: const Text('Quiet Hours', style: TextStyle(fontSize: 11)),
              selected: _prefs['quiet_hours'] == true,
              onSelected: (_) =>
                  _togglePref('quiet_hours', _prefs['quiet_hours'] == true),
            ),
            FilterChip(
              label: const Text('Bedtime Mode', style: TextStyle(fontSize: 11)),
              selected: _prefs['bedtime_mode'] == true,
              onSelected: (_) =>
                  _togglePref('bedtime_mode', _prefs['bedtime_mode'] == true),
            ),
            FilterChip(
              label: const Text(
                'Positivity Boost',
                style: TextStyle(fontSize: 11),
              ),
              selected: _prefs['positivity_boost'] == true,
              onSelected: (_) => _togglePref(
                'positivity_boost',
                _prefs['positivity_boost'] == true,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Detox check
        const DevSectionHeader(title: 'Detox Mode', icon: Icons.spa),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            ElevatedButton.icon(
              onPressed: () async {
                try {
                  final status =
                      await SupabaseService.getCurrentAccountStatus();
                  setState(() {
                    if (status == null) {
                      _detoxStatus = 'No account status (not authenticated)';
                    } else {
                      _detoxStatus = status.isDetoxActive
                          ? 'Detox ACTIVE until ${status.detoxUntil?.toLocal()}'
                          : 'Detox not active';
                    }
                  });
                } catch (e) {
                  setState(() => _detoxStatus = 'Error: $e');
                }
              },
              icon: const Icon(Icons.search, size: 16),
              label: const Text('Check Detox', style: TextStyle(fontSize: 12)),
            ),
            OutlinedButton.icon(
              onPressed: () => context.push('/settings/wellbeing'),
              icon: const Icon(Icons.open_in_new, size: 16),
              label: const Text(
                'Open Wellbeing',
                style: TextStyle(fontSize: 12),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

// ═══════════════════════════════════════════════════════════════
//  DEEP LINKS & ROUTING TESTING
// ═══════════════════════════════════════════════════════════════

class DeepLinksRoutingDevSection extends StatefulWidget {
  const DeepLinksRoutingDevSection({super.key});

  @override
  State<DeepLinksRoutingDevSection> createState() =>
      _DeepLinksRoutingDevSectionState();
}

class _DeepLinksRoutingDevSectionState
    extends State<DeepLinksRoutingDevSection> {
  String _result = '';
  final _customRouteController = TextEditingController();

  static const _testRoutes = <_RouteInfo>[
    _RouteInfo(_feedTabRoute, 'Feed / Home', Icons.dynamic_feed),
    _RouteInfo('/feed', 'Feed Alias', Icons.alt_route),
    _RouteInfo('/explore', 'Explore', Icons.explore),
    _RouteInfo('/messages', 'Messages', Icons.chat),
    _RouteInfo('/notifications', 'Notifications', Icons.notifications),
    _RouteInfo('/profile', 'Profile', Icons.person),
    _RouteInfo('/create-post', 'Create Post', Icons.add_circle),
    _RouteInfo('/games', 'Games Hub', Icons.sports_esports),
    _RouteInfo('/practice-calls', 'Practice Calls', Icons.call),
    _RouteInfo('/active-call', 'Active Call Preview', Icons.videocam),
    _RouteInfo('/settings', 'Settings', Icons.settings),
    _RouteInfo(
      '/settings/accessibility',
      'Accessibility',
      Icons.accessibility_new,
    ),
    _RouteInfo('/settings/privacy', 'Privacy', Icons.privacy_tip),
    _RouteInfo(
      '/settings/notifications',
      'Notif Settings',
      Icons.notifications_active,
    ),
    _RouteInfo('/settings/content', 'Content', Icons.filter_list),
    _RouteInfo('/settings/social', 'Social', Icons.people),
    _RouteInfo('/settings/wellbeing', 'Wellbeing', Icons.self_improvement),
    _RouteInfo('/settings/dm-privacy', 'DM Privacy', Icons.chat_bubble),
    _RouteInfo('/settings/backup', 'Backup', Icons.backup),
    _RouteInfo('/settings/parental', 'Parental', Icons.family_restroom),
    _RouteInfo('/settings/blocked', 'Blocked', Icons.block),
    _RouteInfo('/settings/dev-options', 'Dev Options', Icons.developer_mode),
    _RouteInfo('/settings/feature-test', 'Feature Test', Icons.science),
    _RouteInfo('/help', 'Help', Icons.help),
    _RouteInfo('/feedback', 'Feedback', Icons.feedback),
    _RouteInfo('/about', 'About', Icons.info),
    _RouteInfo('/privacy', 'Privacy Policy', Icons.policy),
    _RouteInfo('/terms', 'Terms', Icons.description),
    _RouteInfo('/subscription', 'Subscription', Icons.star),
    _RouteInfo('/tutorial', 'Tutorial', Icons.school),
    _RouteInfo('/settings/neuro-state', 'Neuro State', Icons.psychology),
  ];

  @override
  void dispose() {
    _customRouteController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DevTestCard(
      title: 'Deep Links & Routing',
      icon: Icons.link,
      children: [
        if (_result.isNotEmpty)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(10),
            margin: const EdgeInsets.only(bottom: 12),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainerHighest.withValues(
                alpha: 0.3,
              ),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(_result, style: const TextStyle(fontSize: 11)),
          ),

        // Custom route input
        Row(
          children: [
            Expanded(
              child: TextField(
                controller: _customRouteController,
                decoration: InputDecoration(
                  hintText: '/custom/route',
                  isDense: true,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 10,
                  ),
                ),
                style: const TextStyle(fontSize: 13, fontFamily: 'monospace'),
              ),
            ),
            const SizedBox(width: 8),
            ElevatedButton(
              onPressed: () {
                final route = _normalizeDevRoute(_customRouteController.text);
                if (route.isEmpty) return;
                try {
                  _openDevRoute(context, route);
                  setState(() => _result = '✅ Navigated to $route');
                } catch (e) {
                  setState(() => _result = '❌ Failed: $e');
                }
              },
              child: const Text('Go', style: TextStyle(fontSize: 12)),
            ),
          ],
        ),
        const SizedBox(height: 8),

        // Error route test
        OutlinedButton.icon(
          onPressed: () {
            context.push('/this-route-does-not-exist-12345');
            setState(
              () => _result =
                  'Navigated to invalid route → should show error page',
            );
          },
          icon: const Icon(Icons.error_outline, size: 16),
          label: const Text(
            'Test Invalid Route',
            style: TextStyle(fontSize: 12),
          ),
        ),
        const SizedBox(height: 8),

        // Route grid
        DevSectionHeader(
          title: 'All Routes (${_testRoutes.length})',
          icon: Icons.map,
        ),
        const Text(
          'Tap any route to navigate:',
          style: TextStyle(fontSize: 11),
        ),
        const SizedBox(height: 8),

        ...List.generate((_testRoutes.length / 2).ceil(), (i) {
          final idx1 = i * 2;
          final idx2 = i * 2 + 1;
          return Padding(
            padding: const EdgeInsets.only(bottom: 4),
            child: Row(
              children: [
                Expanded(child: _routeButton(_testRoutes[idx1])),
                const SizedBox(width: 4),
                if (idx2 < _testRoutes.length)
                  Expanded(child: _routeButton(_testRoutes[idx2]))
                else
                  const Expanded(child: SizedBox()),
              ],
            ),
          );
        }),
      ],
    );
  }

  Widget _routeButton(_RouteInfo info) {
    return SizedBox(
      height: 36,
      child: OutlinedButton(
        onPressed: () {
          try {
            _openDevRoute(context, info.path);
            setState(
              () =>
                  _result = '✅ ${info.label}: ${_normalizeDevRoute(info.path)}',
            );
          } catch (e) {
            setState(() => _result = '❌ ${info.label}: $e');
          }
        },
        style: OutlinedButton.styleFrom(
          padding: const EdgeInsets.symmetric(horizontal: 8),
          visualDensity: VisualDensity.compact,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(info.icon, size: 14),
            const SizedBox(width: 4),
            Flexible(
              child: Text(
                info.label,
                style: const TextStyle(fontSize: 10),
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RouteInfo {
  final String path;
  final String label;
  final IconData icon;
  const _RouteInfo(this.path, this.label, this.icon);
}

class _ExploreLayoutPreviewScreen extends StatelessWidget {
  final String layoutName;
  final List<post_models.Post> posts;

  const _ExploreLayoutPreviewScreen({
    required this.layoutName,
    required this.posts,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: Text('$layoutName Preview')),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12),
            color: theme.colorScheme.surfaceContainerHighest.withValues(
              alpha: 0.35,
            ),
            child: Text(
              'Rendering ${posts.length} real feed posts in the $layoutName explore preview.',
              style: theme.textTheme.bodySmall,
            ),
          ),
          Expanded(child: _buildLayout(context)),
        ],
      ),
    );
  }

  Widget _buildLayout(BuildContext context) {
    switch (layoutName) {
      case 'Grid':
        return GridView.builder(
          padding: const EdgeInsets.all(12),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 12,
            mainAxisSpacing: 12,
            childAspectRatio: 0.82,
          ),
          itemCount: posts.length,
          itemBuilder: (context, index) =>
              _ExploreGridPreviewCard(post: posts[index]),
        );
      case 'Compact':
        return ListView.separated(
          padding: const EdgeInsets.all(12),
          itemCount: posts.length,
          separatorBuilder: (_, __) => const SizedBox(height: 8),
          itemBuilder: (context, index) =>
              _ExploreCompactPreviewCard(post: posts[index]),
        );
      case 'Large Cards':
        return ListView.separated(
          padding: const EdgeInsets.all(12),
          itemCount: posts.length,
          separatorBuilder: (_, __) => const SizedBox(height: 16),
          itemBuilder: (context, index) =>
              _ExploreLargePreviewCard(post: posts[index]),
        );
      case 'Standard':
      default:
        return ListView.separated(
          padding: const EdgeInsets.all(12),
          itemCount: posts.length,
          separatorBuilder: (_, __) => const SizedBox(height: 12),
          itemBuilder: (context, index) =>
              _ExploreStandardPreviewCard(post: posts[index]),
        );
    }
  }
}

class _ExploreStandardPreviewCard extends StatelessWidget {
  final post_models.Post post;

  const _ExploreStandardPreviewCard({required this.post});

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _ExplorePreviewMedia(url: post.mediaUrls?.firstOrNull, height: 220),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _ExplorePreviewHeader(post: post),
                const SizedBox(height: 8),
                Text(
                  post.content,
                  maxLines: 4,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 10),
                _ExplorePreviewMetrics(post: post),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ExploreCompactPreviewCard extends StatelessWidget {
  final post_models.Post post;

  const _ExploreCompactPreviewCard({required this.post});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _ExplorePreviewHeader(post: post, dense: true),
                  const SizedBox(height: 6),
                  Text(
                    post.content,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 8),
                  _ExplorePreviewMetrics(post: post, dense: true),
                ],
              ),
            ),
            const SizedBox(width: 10),
            SizedBox(
              width: 96,
              height: 96,
              child: ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: _ExplorePreviewMedia(
                  url: post.mediaUrls?.firstOrNull,
                  height: 96,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ExploreLargePreviewCard extends StatelessWidget {
  final post_models.Post post;

  const _ExploreLargePreviewCard({required this.post});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      elevation: 1,
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Stack(
            children: [
              _ExplorePreviewMedia(
                url: post.mediaUrls?.firstOrNull,
                height: 280,
              ),
              Positioned(
                left: 12,
                top: 12,
                child: Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.black.withValues(alpha: 0.55),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: const Text(
                    'Featured Layout',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            ],
          ),
          Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _ExplorePreviewHeader(post: post),
                const SizedBox(height: 10),
                Text(
                  post.content,
                  style: theme.textTheme.bodyLarge,
                  maxLines: 5,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 12),
                _ExplorePreviewMetrics(post: post),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ExploreGridPreviewCard extends StatelessWidget {
  final post_models.Post post;

  const _ExploreGridPreviewCard({required this.post});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: _ExplorePreviewMedia(
              url: post.mediaUrls?.firstOrNull,
              height: double.infinity,
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  post.authorName,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 4),
                Text(
                  post.content,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodySmall,
                ),
                if ((post.locationTag ?? '').isNotEmpty) ...[
                  const SizedBox(height: 6),
                  Text(
                    post.locationTag!,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.labelSmall,
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ExplorePreviewHeader extends StatelessWidget {
  final post_models.Post post;
  final bool dense;

  const _ExplorePreviewHeader({required this.post, this.dense = false});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        CircleAvatar(
          radius: dense ? 16 : 18,
          backgroundImage: (post.authorAvatarUrl ?? '').isNotEmpty
              ? NetworkImage(post.authorAvatarUrl!)
              : null,
          child: (post.authorAvatarUrl ?? '').isEmpty
              ? Text(
                  post.authorName.isNotEmpty
                      ? post.authorName.characters.first
                      : '?',
                )
              : null,
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                post.authorName,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
              if ((post.locationTag ?? '').isNotEmpty)
                Text(
                  post.locationTag!,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.labelSmall,
                ),
            ],
          ),
        ),
      ],
    );
  }
}

class _ExplorePreviewMetrics extends StatelessWidget {
  final post_models.Post post;
  final bool dense;

  const _ExplorePreviewMetrics({required this.post, this.dense = false});

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.labelSmall;

    Widget metric(IconData icon, String value) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: dense ? 14 : 16),
          const SizedBox(width: 4),
          Text(value, style: style),
        ],
      );
    }

    return Wrap(
      spacing: dense ? 10 : 14,
      runSpacing: 6,
      children: [
        metric(Icons.favorite_border, '${post.likeCount}'),
        metric(Icons.chat_bubble_outline, '${post.commentCount}'),
        metric(Icons.share_outlined, '${post.shareCount}'),
      ],
    );
  }
}

class _ExplorePreviewMedia extends StatelessWidget {
  final String? url;
  final double height;

  const _ExplorePreviewMedia({required this.url, required this.height});

  @override
  Widget build(BuildContext context) {
    final fallback = Container(
      height: height,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Theme.of(context).colorScheme.primaryContainer,
            Theme.of(context).colorScheme.secondaryContainer,
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: const Center(child: Icon(Icons.explore_outlined, size: 42)),
    );

    if (url == null || url!.isEmpty) {
      return fallback;
    }

    return SizedBox(
      height: height,
      width: double.infinity,
      child: Image.network(
        url!,
        fit: BoxFit.cover,
        errorBuilder: (_, __, ___) => fallback,
        loadingBuilder: (context, child, progress) {
          if (progress == null) return child;
          return fallback;
        },
      ),
    );
  }
}

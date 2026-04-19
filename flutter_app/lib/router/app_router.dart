import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../screens/auth/auth_screen.dart';
import '../screens/home/home_screen.dart';
import '../screens/feed/feed_screen.dart';
import '../screens/feed/create_post_screen.dart';
import '../screens/feed/post_detail_screen.dart';
import '../screens/explore/explore_screen.dart';
import '../screens/messages/messages_screen.dart';
import '../screens/chat/chat_screen.dart';
import '../screens/notifications/notifications_screen.dart';
import '../screens/settings/settings_screen.dart';
import '../screens/settings/accessibility_settings_screen.dart';
import '../screens/settings/privacy_settings_screen.dart';
import '../screens/settings/parental_controls_screen.dart';
import '../screens/settings/blocked_users_screen.dart';
import '../screens/settings/dev_options_screen.dart';
import '../screens/settings/feature_test_screen.dart';
import '../screens/settings/help_screen.dart';
import '../screens/settings/feedback_screen.dart';
import '../screens/settings/legal_screens.dart';
import '../screens/settings/content_settings_screen.dart';
import '../screens/profile/profile_screen.dart';
import '../screens/profile/edit_profile_screen.dart';
import '../screens/profile/followers_screen.dart';
import '../screens/games/games_hub_screen.dart';
import '../screens/games/bubble_pop_game.dart';
import '../screens/games/breathing_bubbles_game.dart';
import '../screens/games/fidget_spinner_game.dart';
import '../screens/games/color_flow_game.dart';
import '../screens/games/pattern_tap_game.dart';
import '../screens/games/infinity_draw_game.dart';
import '../screens/games/sensory_rain_game.dart';
import '../screens/games/zen_sand_game.dart';
import '../screens/games/emotion_garden_game.dart';
import '../screens/games/texture_tiles_game.dart';
import '../screens/games/sound_garden_game.dart';
import '../screens/games/stim_sequencer_game.dart';
import '../screens/games/safe_space_game.dart';
import '../screens/games/worry_jar_game.dart';
import '../screens/games/constellation_connect_game.dart';
import '../screens/games/mood_mixer_game.dart';
import '../screens/badges/badges_screen.dart';
import '../screens/stories/create_story_screen.dart';
import '../screens/subscription/subscription_screen.dart';
import '../screens/neuro_state/neuro_state_screen.dart';
import '../screens/calling/practice_calls_screen.dart';
import '../screens/calling/active_call_screen.dart';
import '../screens/onboarding/tutorial_screen.dart';
import '../screens/feed/category_feed_screen.dart';
import '../screens/settings/parental_blocked_screen.dart';
import '../screens/settings/dm_privacy_settings_screen.dart';
import '../screens/settings/social_settings_screen.dart';
import '../screens/settings/wellbeing_settings_screen.dart';
import '../screens/settings/backup_settings_screen.dart';
import '../services/supabase_service.dart';
import '../services/last_tab_service.dart';

class AppRouter {
  static final _rootNavigatorKey = GlobalKey<NavigatorState>();
  static final _shellNavigatorKey = GlobalKey<NavigatorState>();

  /// The persisted initial location, loaded before the router is created.
  static String _initialLocation = '/';

  /// Call once in main() before runApp() to restore the last-visited tab.
  static Future<void> init() async {
    _initialLocation = await LastTabService.load();
  }

  static final router = GoRouter(
    navigatorKey: _rootNavigatorKey,
    initialLocation: _initialLocation,
    redirect: (context, state) {
      final isAuthenticated = SupabaseService.isAuthenticated;
      final isAuthRoute = state.matchedLocation == '/auth';

      if (!isAuthenticated && !isAuthRoute) {
        return '/auth';
      }

      if (isAuthenticated && isAuthRoute) {
        return _initialLocation;
      }

      return null;
    },
    routes: [
      // Auth Route
      GoRoute(
        path: '/auth',
        builder: (context, state) => const AuthScreen(),
      ),

      // Main Shell Route with Bottom Navigation - matches Android: Feed, Explore, Messages, Notifications, Settings
      ShellRoute(
        navigatorKey: _shellNavigatorKey,
        builder: (context, state, child) => HomeScreen(child: child),
        routes: [
          GoRoute(
            path: '/',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: FeedScreen(),
            ),
          ),
          GoRoute(
            path: '/feed',
            redirect: (context, state) => '/',
          ),
          GoRoute(
            path: '/explore',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: ExploreScreen(),
            ),
          ),
          GoRoute(
            path: '/messages',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: MessagesScreen(),
            ),
          ),
          GoRoute(
            path: '/notifications',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: NotificationsScreen(),
            ),
          ),
          GoRoute(
            path: '/settings',
            pageBuilder: (context, state) => const NoTransitionPage(
              child: SettingsScreen(),
            ),
          ),
        ],
      ),

      // Full screen routes (outside shell)
      GoRoute(
        path: '/create-post',
        builder: (context, state) => const CreatePostScreen(),
      ),
      GoRoute(
        path: '/post/:postId',
        builder: (context, state) {
          final postId = state.pathParameters['postId']!;
          return PostDetailScreen(postId: postId);
        },
      ),
      // Profile is now a full-screen route (accessed from settings or feed)
      GoRoute(
        path: '/profile',
        builder: (context, state) => const ProfileScreen(),
      ),
      // Games is now a full-screen route (accessed from settings)
      GoRoute(
        path: '/games',
        builder: (context, state) => const GamesHubScreen(),
      ),
      GoRoute(
        path: '/profile/:userId',
        builder: (context, state) {
          final userId = state.pathParameters['userId']!;
          return ProfileScreen(userId: userId);
        },
      ),
      GoRoute(
        path: '/edit-profile',
        builder: (context, state) => const EditProfileScreen(),
      ),
      GoRoute(
        path: '/followers/:userId',
        builder: (context, state) {
          final userId = state.pathParameters['userId']!;
          return FollowersScreen(userId: userId, initialTab: 0);
        },
      ),
      GoRoute(
        path: '/following/:userId',
        builder: (context, state) {
          final userId = state.pathParameters['userId']!;
          return FollowersScreen(userId: userId, initialTab: 1);
        },
      ),
      GoRoute(
        path: '/chat/:conversationId',
        builder: (context, state) {
          final conversationId = state.pathParameters['conversationId'];
          return ChatScreen(
            conversationId: conversationId,
          );
        },
      ),
      GoRoute(
        path: '/chat',
        builder: (context, state) {
          final args = state.extra as Map<String, dynamic>?;
          return ChatScreen(
            conversationId: args?['conversationId'],
            userId: args?['userId'],
            displayName: args?['displayName'],
            avatarUrl: args?['avatarUrl'],
            isGroup: args?['isGroup'] ?? false,
            participantIds: (args?['participantIds'] as List?)?.map((e) => e.toString()).toList() ?? const [],
            memberNames: (args?['memberNames'] as List<String>?) ?? const [],
            groupName: args?['groupName'],
          );
        },
      ),

      // Story routes
      GoRoute(
        path: '/create-story',
        builder: (context, state) => const CreateStoryScreen(),
      ),

      // Subscription
      GoRoute(
        path: '/subscription',
        builder: (context, state) => const SubscriptionScreen(),
      ),

      // Games routes
      GoRoute(
        path: '/games/bubble_pop',
        builder: (context, state) => const BubblePopGame(),
      ),
      GoRoute(
        path: '/games/breathing_bubbles',
        builder: (context, state) => const BreathingBubblesGame(),
      ),
      GoRoute(
        path: '/games/fidget_spinner',
        builder: (context, state) => const FidgetSpinnerGame(),
      ),
      GoRoute(
        path: '/games/color_flow',
        builder: (context, state) => const ColorFlowGame(),
      ),
      GoRoute(
        path: '/games/pattern_tap',
        builder: (context, state) => const PatternTapGame(),
      ),
      GoRoute(
        path: '/games/infinity_draw',
        builder: (context, state) => const InfinityDrawGame(),
      ),
      GoRoute(
        path: '/games/sensory_rain',
        builder: (context, state) => const SensoryRainGame(),
      ),
      GoRoute(
        path: '/games/zen_sand',
        builder: (context, state) => const ZenSandGame(),
      ),
      GoRoute(
        path: '/games/emotion_garden',
        builder: (context, state) => const EmotionGardenGame(),
      ),
      GoRoute(
        path: '/games/texture_tiles',
        builder: (context, state) => const TextureTilesGame(),
      ),
      GoRoute(
        path: '/games/sound_garden',
        builder: (context, state) => const SoundGardenGame(),
      ),
      GoRoute(
        path: '/games/stim_sequencer',
        builder: (context, state) => const StimSequencerGame(),
      ),
      GoRoute(
        path: '/games/safe_space',
        builder: (context, state) => const SafeSpaceGame(),
      ),
      GoRoute(
        path: '/games/worry_jar',
        builder: (context, state) => const WorryJarGame(),
      ),
      GoRoute(
        path: '/games/constellation_connect',
        builder: (context, state) => const ConstellationConnectGame(),
      ),
      GoRoute(
        path: '/games/mood_mixer',
        builder: (context, state) => const MoodMixerGame(),
      ),

      // Badges/Achievements
      GoRoute(
        path: '/badges',
        builder: (context, state) => const BadgesScreen(),
      ),

      // Practice Calls
      GoRoute(
        path: '/practice-calls',
        builder: (context, state) => const PracticeCallsScreen(),
      ),

      // Active Call (voice/video)
      GoRoute(
        path: '/active-call',
        builder: (context, state) => const ActiveCallScreen(),
      ),

      // Settings sub-routes
      GoRoute(
        path: '/settings/accessibility',
        builder: (context, state) => const AccessibilitySettingsScreen(),
      ),
      GoRoute(
        path: '/settings/privacy',
        builder: (context, state) => const PrivacySettingsScreen(),
      ),
      GoRoute(
        path: '/settings/parental',
        builder: (context, state) => const ParentalControlsScreen(),
      ),
      GoRoute(
        path: '/settings/blocked',
        builder: (context, state) => const BlockedUsersScreen(),
      ),
      GoRoute(
        path: '/settings/dev-options',
        builder: (context, state) => const DevOptionsScreen(),
      ),
      GoRoute(
        path: '/settings/feature-test',
        builder: (context, state) => const FeatureTestScreen(),
      ),
      GoRoute(
        path: '/settings/content',
        builder: (context, state) => const ContentSettingsScreen(),
      ),
      GoRoute(
        path: '/tutorial',
        builder: (context, state) => const TutorialScreen(),
      ),
      GoRoute(
        path: '/help',
        builder: (context, state) => const HelpScreen(),
      ),
      GoRoute(
        path: '/settings/neuro-state',
        builder: (context, state) => const NeuroStateScreen(),
      ),
      GoRoute(
        path: '/report',
        builder: (context, state) => const FeedbackBugReportScreen(),
      ),
      GoRoute(
        path: '/feedback',
        builder: (context, state) => const FeedbackScreen(),
      ),
      GoRoute(
        path: '/feedback/bug',
        builder: (context, state) => const FeedbackBugReportScreen(),
      ),
      GoRoute(
        path: '/feedback/feature',
        builder: (context, state) => const FeedbackFeatureRequestScreen(),
      ),
      GoRoute(
        path: '/feedback/general',
        builder: (context, state) => const FeedbackGeneralScreen(),
      ),
      // Legal pages for app store compliance
      GoRoute(
        path: '/privacy',
        builder: (context, state) => const PrivacyPolicyScreen(),
      ),
      GoRoute(
        path: '/terms',
        builder: (context, state) => const TermsOfServiceScreen(),
      ),
      GoRoute(
        path: '/about',
        builder: (context, state) => const AboutScreen(),
      ),

      // Category Feed
      GoRoute(
        path: '/category/:name',
        builder: (context, state) {
          final name = state.pathParameters['name'] ?? 'Unknown';
          return CategoryFeedScreen(categoryName: name);
        },
      ),

      // Social Settings
      GoRoute(
        path: '/settings/social',
        builder: (context, state) => const SocialSettingsScreen(),
      ),

      // Notification Settings (opens Social Settings on Notifications tab)
      GoRoute(
        path: '/settings/notifications',
        builder: (context, state) => const SocialSettingsScreen(initialTabIndex: 1),
      ),

      GoRoute(
        path: '/settings/wellbeing',
        builder: (context, state) => const WellbeingSettingsScreen(),
      ),

      // DM Privacy Settings
      GoRoute(
        path: '/settings/dm-privacy',
        builder: (context, state) => const DmPrivacySettingsScreen(),
      ),

      // Backup & Storage Settings
      GoRoute(
        path: '/settings/backup',
        builder: (context, state) => const BackupSettingsScreen(),
      ),

      // Parental Blocked Screen
      GoRoute(
        path: '/parental-blocked',
        builder: (context, state) {
          final type = state.extra as RestrictionType? ?? RestrictionType.featureBlocked;
          return ParentalBlockedScreen(restrictionType: type);
        },
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64),
            const SizedBox(height: 16),
            Text('Page not found: ${state.matchedLocation}'),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => context.go('/'),
              child: const Text('Go Home'),
            ),
          ],
        ),
      ),
    ),
  );
}



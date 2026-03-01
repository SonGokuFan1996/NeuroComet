/// NeuroComet App - A safe space for neurodivergent individuals
///
/// This file exports all public modules for the app.
library;

// Core
export 'core/constants/app_constants.dart';
export 'core/theme/app_colors.dart';
export 'core/theme/app_theme.dart';
export 'core/theme/neuro_color_schemes.dart';
export 'core/theme/neuro_state.dart';
export 'core/utils/app_utils.dart';

// Models
export 'models/user.dart';
export 'models/post.dart';
export 'models/conversation.dart';
export 'models/notification.dart';

// Providers
export 'providers/auth_provider.dart';
export 'providers/feed_provider.dart';
export 'providers/messages_provider.dart';
export 'providers/notifications_provider.dart';
export 'providers/profile_provider.dart';
export 'providers/theme_provider.dart';

// Services
export 'services/supabase_service.dart';
export 'services/moderation_service.dart';
export 'services/app_services.dart';

// Localization
export 'l10n/app_localizations.dart';

// Router
export 'router/app_router.dart';

// Screens - Splash & Onboarding
export 'screens/splash/splash_screen.dart';
export 'screens/onboarding/onboarding_screen.dart';

// Screens - Auth
export 'screens/auth/auth_screen.dart';

// Screens - Home
export 'screens/home/home_screen.dart';

// Screens - Feed
export 'screens/feed/feed_screen.dart';
export 'screens/feed/create_post_screen.dart';
export 'screens/feed/post_detail_screen.dart';

// Screens - Explore
export 'screens/explore/explore_screen.dart';

// Screens - Profile
export 'screens/profile/profile_screen.dart';
export 'screens/profile/edit_profile_screen.dart';
export 'screens/profile/followers_screen.dart';

// Screens - Messages & Chat
export 'screens/messages/messages_screen.dart';
export 'screens/chat/chat_screen.dart';

// Screens - Calling
export 'screens/calling/practice_calls_screen.dart';

// Screens - Notifications
export 'screens/notifications/notifications_screen.dart';

// Screens - Settings
export 'screens/settings/settings_screen.dart';
export 'screens/settings/accessibility_settings_screen.dart';
export 'screens/settings/privacy_settings_screen.dart';
export 'screens/settings/parental_controls_screen.dart';
export 'screens/settings/blocked_users_screen.dart';
export 'screens/settings/dev_options_screen.dart';
export 'screens/settings/help_screen.dart';
export 'screens/settings/feedback_screen.dart';

// Screens - NeuroState
export 'screens/neuro_state/neuro_state_screen.dart';

// Screens - Games
export 'screens/games/games_hub_screen.dart';
export 'screens/games/bubble_pop_game.dart';
export 'screens/games/breathing_bubbles_game.dart';
export 'screens/games/fidget_spinner_game.dart';
export 'screens/games/color_flow_game.dart';
export 'screens/games/pattern_tap_game.dart';

// Screens - Stories
export 'screens/stories/story_viewer_screen.dart';
export 'screens/stories/create_story_screen.dart';

// Screens - Subscription
export 'screens/subscription/subscription_screen.dart';

// Widgets - Common
export 'widgets/common/neuro_avatar.dart';
export 'widgets/common/neuro_loading.dart';
export 'widgets/common/neuro_widgets.dart';
export 'widgets/common/report_sheet.dart';

// Widgets - Post
export 'widgets/post/post_card.dart';

// Widgets - Media
export 'widgets/media/video_player.dart';

// Widgets - Messages
export 'widgets/messages/message_widgets.dart';


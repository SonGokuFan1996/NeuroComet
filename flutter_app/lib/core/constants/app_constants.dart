/// App-wide constants
class AppConstants {
  AppConstants._();

  // App Info
  static const String appName = 'NeuroComet';
  static const String appVersion = '1.0.0';
  static const String appBuildNumber = '1';

  // API Endpoints
  static const String supabaseCallbackUrl = 'io.neurocomet.app://callback';

  // Storage Keys
  static const String keyThemeMode = 'theme_mode';
  static const String keyLocale = 'locale';
  static const String keyFontScale = 'font_scale';
  static const String keyReducedMotion = 'reduced_motion';
  static const String keyHighContrast = 'high_contrast';
  static const String keyDyslexicFont = 'dyslexic_font';
  static const String keyOnboardingCompleted = 'onboarding_completed';
  static const String keyLastSyncTime = 'last_sync_time';

  // Limits
  static const int maxPostLength = 500;
  static const int maxBioLength = 160;
  static const int maxUsernameLength = 30;
  static const int maxDisplayNameLength = 50;
  static const int maxCommentLength = 280;
  static const int maxMediaPerPost = 4;
  static const int maxStoryItems = 10;
  static const int maxTagsPerPost = 10;

  // Pagination
  static const int defaultPageSize = 20;
  static const int notificationsPageSize = 30;
  static const int messagesPageSize = 50;

  // Timeouts
  static const Duration apiTimeout = Duration(seconds: 30);
  static const Duration cacheExpiry = Duration(hours: 1);
  static const Duration storyExpiry = Duration(hours: 24);

  // Animation Durations
  static const Duration shortAnimation = Duration(milliseconds: 200);
  static const Duration mediumAnimation = Duration(milliseconds: 350);
  static const Duration longAnimation = Duration(milliseconds: 500);

  // UI Sizes
  static const double avatarSizeSmall = 32;
  static const double avatarSizeMedium = 48;
  static const double avatarSizeLarge = 80;
  static const double avatarSizeXLarge = 120;

  static const double borderRadiusSmall = 8;
  static const double borderRadiusMedium = 12;
  static const double borderRadiusLarge = 16;
  static const double borderRadiusXLarge = 24;

  static const double paddingSmall = 8;
  static const double paddingMedium = 16;
  static const double paddingLarge = 24;
  static const double paddingXLarge = 32;

  // Font Sizes (for accessibility)
  static const double minFontScale = 0.8;
  static const double maxFontScale = 1.5;
  static const double defaultFontScale = 1.0;

  // Accessibility
  static const int defaultStoryDurationSeconds = 5;
  static const int minStoryDurationSeconds = 3;
  static const int maxStoryDurationSeconds = 15;

  // Deep Link Paths
  static const String deepLinkPost = '/post/';
  static const String deepLinkProfile = '/profile/';
  static const String deepLinkMessage = '/chat/';
}

/// Category constants for neurodivergent conditions
class NeurodivergentCategories {
  NeurodivergentCategories._();

  static const List<String> all = [
    'ADHD',
    'Autism',
    'Dyslexia',
    'Dyspraxia',
    'Dyscalculia',
    'Dysgraphia',
    'OCD',
    'Tourette',
    'Anxiety',
    'Depression',
    'Bipolar',
    'General',
  ];

  static const Map<String, String> descriptions = {
    'ADHD': 'Attention Deficit Hyperactivity Disorder',
    'Autism': 'Autism Spectrum Disorder',
    'Dyslexia': 'Reading and Language Processing Differences',
    'Dyspraxia': 'Motor Coordination Differences',
    'Dyscalculia': 'Mathematical Processing Differences',
    'Dysgraphia': 'Writing and Fine Motor Differences',
    'OCD': 'Obsessive-Compulsive Disorder',
    'Tourette': 'Tourette Syndrome',
    'Anxiety': 'Anxiety Disorders',
    'Depression': 'Depressive Disorders',
    'Bipolar': 'Bipolar Disorder',
    'General': 'General Neurodivergent Support',
  };
}


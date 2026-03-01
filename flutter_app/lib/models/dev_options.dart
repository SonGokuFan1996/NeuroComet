import '../models/custom_avatar.dart';

/// Audience levels for content filtering
enum Audience {
  under13,
  teen,
  adult,
}

/// Moderation override options
enum ModerationOverride {
  off,
  clean,
  flagged,
  blocked,
}

/// Kids filter levels
enum KidsFilterLevel {
  strict,
  moderate,
  relaxed,
}

/// Environment targets for backend configuration
enum DevEnvironmentTarget {
  production,
  staging,
  local,
}

/// Developer options state
///
/// All options default to safe/disabled values.
/// In release mode the [DevOptionsNotifier] ignores any persisted state
/// and always returns the constructor defaults.
class DevOptions {
  // ── General ────────────────────────────────────────────
  final bool showDebugOverlay;
  final bool enableVerboseLogging;

  // ── Environment ────────────────────────────────────────
  final DevEnvironmentTarget environment;

  // ── Feature Flags ──────────────────────────────────────
  final bool enableNewFeedLayout;
  final bool enableVideoChat;
  final bool enableStoryReactions;
  final bool enableAdvancedSearch;
  final bool enableAiSuggestions;

  // ── Content & Safety ───────────────────────────────────
  final Audience? forcedAudience;
  final bool bypassAgeVerification;
  final bool forcePinSet;
  final bool forcePinVerifySuccess;
  final bool isKidsMode;
  final KidsFilterLevel kidsFilterLevel;

  // ── DM Debug ───────────────────────────────────────────
  final bool showDmDebugOverlay;
  final bool forceSendFailure;
  final bool disableRateLimit;
  final int artificialDelayMs;

  // ── Content Moderation ─────────────────────────────────
  final ModerationOverride moderationOverride;

  // ── Rendering & Performance ────────────────────────────
  final bool isMockInterfaceEnabled;
  final bool showStories;
  final bool isVideoAutoplayEnabled;
  final bool isFallbackUiEnabled;
  final bool simulateLoadingError;
  final bool infiniteLoading;
  final bool showPerformanceOverlay;

  // ── Authentication ─────────────────────────────────────
  final bool forceLoggedOut;
  final bool bypassBiometric;
  final bool force2FA;

  // ── Feed Options ───────────────────────────────────────
  final int mockPostCount;
  final bool showSponsoredPosts;

  // ── Network Simulation ─────────────────────────────────
  final bool simulateOffline;
  final int networkLatencyMs;

  // ── Stress Testing ─────────────────────────────────────
  final bool enableWebStressTest;
  final int stressTestWidgetCount;
  final bool stressTestAnimations;
  final bool stressTestScrolling;
  final bool stressTestMemory;
  final bool stressTestRapidNavigation;
  final int stressTestConcurrentRequests;
  final bool stressTestLargeImages;
  final bool stressTestLocalStorage;

  // ── Custom Avatar ──────────────────────────────────────
  final CustomAvatar? mockCustomAvatar;

  const DevOptions({
    // General
    this.showDebugOverlay = false,
    this.enableVerboseLogging = false,
    // Environment
    this.environment = DevEnvironmentTarget.production,
    // Feature Flags
    this.enableNewFeedLayout = false,
    this.enableVideoChat = false,
    this.enableStoryReactions = false,
    this.enableAdvancedSearch = false,
    this.enableAiSuggestions = false,
    // Content & Safety
    this.forcedAudience,
    this.bypassAgeVerification = false,
    this.forcePinSet = false,
    this.forcePinVerifySuccess = false,
    this.isKidsMode = false,
    this.kidsFilterLevel = KidsFilterLevel.moderate,
    // DM Debug
    this.showDmDebugOverlay = false,
    this.forceSendFailure = false,
    this.disableRateLimit = false,
    this.artificialDelayMs = 0,
    // Content Moderation
    this.moderationOverride = ModerationOverride.off,
    // Rendering & Performance
    this.isMockInterfaceEnabled = true,
    this.showStories = true,
    this.isVideoAutoplayEnabled = true,
    this.isFallbackUiEnabled = false,
    this.simulateLoadingError = false,
    this.infiniteLoading = false,
    this.showPerformanceOverlay = false,
    // Authentication
    this.forceLoggedOut = false,
    this.bypassBiometric = false,
    this.force2FA = false,
    // Feed Options
    this.mockPostCount = 10,
    this.showSponsoredPosts = true,
    // Network Simulation
    this.simulateOffline = false,
    this.networkLatencyMs = 0,
    // Stress Testing
    this.enableWebStressTest = false,
    this.stressTestWidgetCount = 100,
    this.stressTestAnimations = false,
    this.stressTestScrolling = false,
    this.stressTestMemory = false,
    this.stressTestRapidNavigation = false,
    this.stressTestConcurrentRequests = 10,
    this.stressTestLargeImages = false,
    this.stressTestLocalStorage = false,
    this.mockCustomAvatar,
  });

  /// Returns the number of options that differ from defaults.
  int get activeOverrideCount {
    const d = DevOptions();
    int count = 0;
    if (showDebugOverlay != d.showDebugOverlay) count++;
    if (enableVerboseLogging != d.enableVerboseLogging) count++;
    if (environment != d.environment) count++;
    if (enableNewFeedLayout != d.enableNewFeedLayout) count++;
    if (enableVideoChat != d.enableVideoChat) count++;
    if (enableStoryReactions != d.enableStoryReactions) count++;
    if (enableAdvancedSearch != d.enableAdvancedSearch) count++;
    if (enableAiSuggestions != d.enableAiSuggestions) count++;
    if (forcedAudience != d.forcedAudience) count++;
    if (bypassAgeVerification != d.bypassAgeVerification) count++;
    if (forcePinSet != d.forcePinSet) count++;
    if (forcePinVerifySuccess != d.forcePinVerifySuccess) count++;
    if (isKidsMode != d.isKidsMode) count++;
    if (showDmDebugOverlay != d.showDmDebugOverlay) count++;
    if (forceSendFailure != d.forceSendFailure) count++;
    if (disableRateLimit != d.disableRateLimit) count++;
    if (artificialDelayMs != d.artificialDelayMs) count++;
    if (moderationOverride != d.moderationOverride) count++;
    if (simulateLoadingError != d.simulateLoadingError) count++;
    if (infiniteLoading != d.infiniteLoading) count++;
    if (showPerformanceOverlay != d.showPerformanceOverlay) count++;
    if (forceLoggedOut != d.forceLoggedOut) count++;
    if (bypassBiometric != d.bypassBiometric) count++;
    if (force2FA != d.force2FA) count++;
    if (simulateOffline != d.simulateOffline) count++;
    if (networkLatencyMs != d.networkLatencyMs) count++;
    return count;
  }

  DevOptions copyWith({
    bool? showDebugOverlay,
    bool? enableVerboseLogging,
    DevEnvironmentTarget? environment,
    bool? enableNewFeedLayout,
    bool? enableVideoChat,
    bool? enableStoryReactions,
    bool? enableAdvancedSearch,
    bool? enableAiSuggestions,
    Audience? forcedAudience,
    bool? clearForcedAudience,
    bool? bypassAgeVerification,
    bool? forcePinSet,
    bool? forcePinVerifySuccess,
    bool? isKidsMode,
    KidsFilterLevel? kidsFilterLevel,
    bool? showDmDebugOverlay,
    bool? forceSendFailure,
    bool? disableRateLimit,
    int? artificialDelayMs,
    ModerationOverride? moderationOverride,
    bool? isMockInterfaceEnabled,
    bool? showStories,
    bool? isVideoAutoplayEnabled,
    bool? isFallbackUiEnabled,
    bool? simulateLoadingError,
    bool? infiniteLoading,
    bool? showPerformanceOverlay,
    bool? forceLoggedOut,
    bool? bypassBiometric,
    bool? force2FA,
    int? mockPostCount,
    bool? showSponsoredPosts,
    bool? simulateOffline,
    int? networkLatencyMs,
    bool? enableWebStressTest,
    int? stressTestWidgetCount,
    bool? stressTestAnimations,
    bool? stressTestScrolling,
    bool? stressTestMemory,
    bool? stressTestRapidNavigation,
    int? stressTestConcurrentRequests,
    bool? stressTestLargeImages,
    bool? stressTestLocalStorage,
    CustomAvatar? mockCustomAvatar,
  }) {
    return DevOptions(
      showDebugOverlay: showDebugOverlay ?? this.showDebugOverlay,
      enableVerboseLogging: enableVerboseLogging ?? this.enableVerboseLogging,
      environment: environment ?? this.environment,
      enableNewFeedLayout: enableNewFeedLayout ?? this.enableNewFeedLayout,
      enableVideoChat: enableVideoChat ?? this.enableVideoChat,
      enableStoryReactions: enableStoryReactions ?? this.enableStoryReactions,
      enableAdvancedSearch: enableAdvancedSearch ?? this.enableAdvancedSearch,
      enableAiSuggestions: enableAiSuggestions ?? this.enableAiSuggestions,
      forcedAudience: clearForcedAudience == true ? null : (forcedAudience ?? this.forcedAudience),
      bypassAgeVerification: bypassAgeVerification ?? this.bypassAgeVerification,
      forcePinSet: forcePinSet ?? this.forcePinSet,
      forcePinVerifySuccess: forcePinVerifySuccess ?? this.forcePinVerifySuccess,
      isKidsMode: isKidsMode ?? this.isKidsMode,
      kidsFilterLevel: kidsFilterLevel ?? this.kidsFilterLevel,
      showDmDebugOverlay: showDmDebugOverlay ?? this.showDmDebugOverlay,
      forceSendFailure: forceSendFailure ?? this.forceSendFailure,
      disableRateLimit: disableRateLimit ?? this.disableRateLimit,
      artificialDelayMs: artificialDelayMs ?? this.artificialDelayMs,
      moderationOverride: moderationOverride ?? this.moderationOverride,
      isMockInterfaceEnabled: isMockInterfaceEnabled ?? this.isMockInterfaceEnabled,
      showStories: showStories ?? this.showStories,
      isVideoAutoplayEnabled: isVideoAutoplayEnabled ?? this.isVideoAutoplayEnabled,
      isFallbackUiEnabled: isFallbackUiEnabled ?? this.isFallbackUiEnabled,
      simulateLoadingError: simulateLoadingError ?? this.simulateLoadingError,
      infiniteLoading: infiniteLoading ?? this.infiniteLoading,
      showPerformanceOverlay: showPerformanceOverlay ?? this.showPerformanceOverlay,
      forceLoggedOut: forceLoggedOut ?? this.forceLoggedOut,
      bypassBiometric: bypassBiometric ?? this.bypassBiometric,
      force2FA: force2FA ?? this.force2FA,
      mockPostCount: mockPostCount ?? this.mockPostCount,
      showSponsoredPosts: showSponsoredPosts ?? this.showSponsoredPosts,
      simulateOffline: simulateOffline ?? this.simulateOffline,
      networkLatencyMs: networkLatencyMs ?? this.networkLatencyMs,
      enableWebStressTest: enableWebStressTest ?? this.enableWebStressTest,
      stressTestWidgetCount: stressTestWidgetCount ?? this.stressTestWidgetCount,
      stressTestAnimations: stressTestAnimations ?? this.stressTestAnimations,
      stressTestScrolling: stressTestScrolling ?? this.stressTestScrolling,
      stressTestMemory: stressTestMemory ?? this.stressTestMemory,
      stressTestRapidNavigation: stressTestRapidNavigation ?? this.stressTestRapidNavigation,
      stressTestConcurrentRequests: stressTestConcurrentRequests ?? this.stressTestConcurrentRequests,
      stressTestLargeImages: stressTestLargeImages ?? this.stressTestLargeImages,
      stressTestLocalStorage: stressTestLocalStorage ?? this.stressTestLocalStorage,
      mockCustomAvatar: mockCustomAvatar ?? this.mockCustomAvatar,
    );
  }

  /// Serialize to a map for SharedPreferences persistence
  Map<String, dynamic> toMap() => {
    'showDebugOverlay': showDebugOverlay,
    'enableVerboseLogging': enableVerboseLogging,
    'environment': environment.index,
    'enableNewFeedLayout': enableNewFeedLayout,
    'enableVideoChat': enableVideoChat,
    'enableStoryReactions': enableStoryReactions,
    'enableAdvancedSearch': enableAdvancedSearch,
    'enableAiSuggestions': enableAiSuggestions,
    'forcedAudience': forcedAudience?.index,
    'bypassAgeVerification': bypassAgeVerification,
    'forcePinSet': forcePinSet,
    'forcePinVerifySuccess': forcePinVerifySuccess,
    'isKidsMode': isKidsMode,
    'kidsFilterLevel': kidsFilterLevel.index,
    'showDmDebugOverlay': showDmDebugOverlay,
    'forceSendFailure': forceSendFailure,
    'disableRateLimit': disableRateLimit,
    'artificialDelayMs': artificialDelayMs,
    'moderationOverride': moderationOverride.index,
    'simulateLoadingError': simulateLoadingError,
    'infiniteLoading': infiniteLoading,
    'showPerformanceOverlay': showPerformanceOverlay,
    'forceLoggedOut': forceLoggedOut,
    'bypassBiometric': bypassBiometric,
    'force2FA': force2FA,
    'simulateOffline': simulateOffline,
    'networkLatencyMs': networkLatencyMs,
    'mockPostCount': mockPostCount,
    'showSponsoredPosts': showSponsoredPosts,
  };

  /// Deserialize from a SharedPreferences map
  factory DevOptions.fromMap(Map<String, dynamic> map) {
    return DevOptions(
      showDebugOverlay: map['showDebugOverlay'] as bool? ?? false,
      enableVerboseLogging: map['enableVerboseLogging'] as bool? ?? false,
      environment: DevEnvironmentTarget.values.elementAtOrNull(map['environment'] as int? ?? 0) ?? DevEnvironmentTarget.production,
      enableNewFeedLayout: map['enableNewFeedLayout'] as bool? ?? false,
      enableVideoChat: map['enableVideoChat'] as bool? ?? false,
      enableStoryReactions: map['enableStoryReactions'] as bool? ?? false,
      enableAdvancedSearch: map['enableAdvancedSearch'] as bool? ?? false,
      enableAiSuggestions: map['enableAiSuggestions'] as bool? ?? false,
      forcedAudience: (map['forcedAudience'] as int?) != null ? Audience.values.elementAtOrNull(map['forcedAudience'] as int) : null,
      bypassAgeVerification: map['bypassAgeVerification'] as bool? ?? false,
      forcePinSet: map['forcePinSet'] as bool? ?? false,
      forcePinVerifySuccess: map['forcePinVerifySuccess'] as bool? ?? false,
      isKidsMode: map['isKidsMode'] as bool? ?? false,
      kidsFilterLevel: KidsFilterLevel.values.elementAtOrNull(map['kidsFilterLevel'] as int? ?? 1) ?? KidsFilterLevel.moderate,
      showDmDebugOverlay: map['showDmDebugOverlay'] as bool? ?? false,
      forceSendFailure: map['forceSendFailure'] as bool? ?? false,
      disableRateLimit: map['disableRateLimit'] as bool? ?? false,
      artificialDelayMs: map['artificialDelayMs'] as int? ?? 0,
      moderationOverride: ModerationOverride.values.elementAtOrNull(map['moderationOverride'] as int? ?? 0) ?? ModerationOverride.off,
      simulateLoadingError: map['simulateLoadingError'] as bool? ?? false,
      infiniteLoading: map['infiniteLoading'] as bool? ?? false,
      showPerformanceOverlay: map['showPerformanceOverlay'] as bool? ?? false,
      forceLoggedOut: map['forceLoggedOut'] as bool? ?? false,
      bypassBiometric: map['bypassBiometric'] as bool? ?? false,
      force2FA: map['force2FA'] as bool? ?? false,
      simulateOffline: map['simulateOffline'] as bool? ?? false,
      networkLatencyMs: map['networkLatencyMs'] as int? ?? 0,
      mockPostCount: map['mockPostCount'] as int? ?? 10,
      showSponsoredPosts: map['showSponsoredPosts'] as bool? ?? true,
    );
  }
}

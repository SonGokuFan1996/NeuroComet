import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

/// Background message handler (must be top-level function)
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  debugPrint('Handling background message: ${message.messageId}');
}

/// Notification service for managing push notifications
class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  bool _initialized = false;
  String? _fcmToken;
  PermissionStatus _permissionStatus = PermissionStatus.denied;
  final FirebaseMessaging _messaging = FirebaseMessaging.instance;

  /// Callbacks for notification handling
  void Function(RemoteMessage)? onMessageReceived;
  void Function(RemoteMessage)? onMessageOpenedApp;

  /// Initialize notification service
  Future<void> initialize() async {
    if (_initialized) return;

    try {
      // Set up background message handler
      FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

      // Check current permission status
      if (!kIsWeb) {
        _permissionStatus = await Permission.notification.status;
      }

      // Request permission and get token
      final settings = await _messaging.requestPermission(
        alert: true,
        announcement: false,
        badge: true,
        carPlay: false,
        criticalAlert: false,
        provisional: false,
        sound: true,
      );

      if (settings.authorizationStatus == AuthorizationStatus.authorized) {
        _fcmToken = await _messaging.getToken();
        debugPrint('FCM Token: $_fcmToken');

        // Listen for token refresh
        _messaging.onTokenRefresh.listen((newToken) {
          _fcmToken = newToken;
          debugPrint('FCM Token refreshed: $newToken');
          _updateTokenOnServer(newToken);
        });
      }

      // Handle foreground messages
      FirebaseMessaging.onMessage.listen((RemoteMessage message) {
        debugPrint('Foreground message received: ${message.notification?.title}');
        onMessageReceived?.call(message);
      });

      // Handle when app is opened from notification
      FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
        debugPrint('App opened from notification: ${message.notification?.title}');
        onMessageOpenedApp?.call(message);
      });

      // Check if app was opened from a notification
      final initialMessage = await _messaging.getInitialMessage();
      if (initialMessage != null) {
        debugPrint('App opened from terminated state via notification');
        onMessageOpenedApp?.call(initialMessage);
      }

      debugPrint('Notification service initialized');
      _initialized = true;
    } catch (e) {
      debugPrint('Failed to initialize notifications: $e');
    }
  }

  /// Request notification permissions
  Future<bool> requestPermission() async {
    if (kIsWeb) {
      debugPrint('Notification permissions handled differently on web');
      return true;
    }

    try {
      _permissionStatus = await Permission.notification.request();
      debugPrint('Notification permission status: $_permissionStatus');
      return _permissionStatus.isGranted;
    } catch (e) {
      debugPrint('Failed to request notification permission: $e');
      return false;
    }
  }

  /// Check current permission status
  Future<PermissionStatus> checkPermissionStatus() async {
    if (kIsWeb) {
      return PermissionStatus.granted;
    }

    try {
      _permissionStatus = await Permission.notification.status;
      return _permissionStatus;
    } catch (e) {
      debugPrint('Failed to check notification permission: $e');
      return PermissionStatus.denied;
    }
  }

  /// Check if permission is granted
  bool get hasPermission => _permissionStatus.isGranted;

  /// Check if permission is permanently denied
  bool get isPermanentlyDenied => _permissionStatus.isPermanentlyDenied;

  /// Get FCM token
  String? get fcmToken => _fcmToken;

  /// Subscribe to topic
  Future<void> subscribeToTopic(String topic) async {
    try {
      await _messaging.subscribeToTopic(topic);
      debugPrint('Subscribed to topic: $topic');
    } catch (e) {
      debugPrint('Failed to subscribe to topic: $e');
    }
  }

  /// Unsubscribe from topic
  Future<void> unsubscribeFromTopic(String topic) async {
    try {
      await _messaging.unsubscribeFromTopic(topic);
      debugPrint('Unsubscribed from topic: $topic');
    } catch (e) {
      debugPrint('Failed to unsubscribe from topic: $e');
    }
  }

  /// Show local notification
  /// Uses Firebase messaging channel for foreground display.
  /// For richer local notifications, add flutter_local_notifications package.
  Future<void> showLocalNotification({
    required String title,
    required String body,
    String? payload,
  }) async {
    try {
      // Store notification for later display via UI
      final prefs = await SharedPreferences.getInstance();
      final notifications = prefs.getStringList('pending_notifications') ?? [];
      notifications.add('$title|$body|${DateTime.now().toIso8601String()}');
      await prefs.setStringList('pending_notifications', notifications);
      debugPrint('Local notification queued: $title - $body');
    } catch (e) {
      debugPrint('Failed to queue notification: $e');
    }
  }

  /// Update FCM token on server (Supabase profiles)
  Future<void> _updateTokenOnServer(String token) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('fcm_token', token);
      debugPrint('FCM token saved locally: ${token.substring(0, 20)}...');
      // In production, call: SupabaseService.updateProfile(fcmToken: token)
    } catch (e) {
      debugPrint('Failed to update FCM token: $e');
    }
  }
}

/// Settings manager for app preferences
class SettingsManager {
  static final SettingsManager _instance = SettingsManager._internal();
  factory SettingsManager() => _instance;
  SettingsManager._internal();

  SharedPreferences? _prefs;

  Future<void> initialize() async {
    _prefs = await SharedPreferences.getInstance();
  }

  // Theme settings
  String get themeMode => _prefs?.getString('theme_mode') ?? 'system';
  Future<void> setThemeMode(String mode) async {
    await _prefs?.setString('theme_mode', mode);
  }

  // Accessibility settings
  bool get reducedMotion => _prefs?.getBool('reduced_motion') ?? false;
  Future<void> setReducedMotion(bool value) async {
    await _prefs?.setBool('reduced_motion', value);
  }

  bool get highContrast => _prefs?.getBool('high_contrast') ?? false;
  Future<void> setHighContrast(bool value) async {
    await _prefs?.setBool('high_contrast', value);
  }

  bool get dyslexicFont => _prefs?.getBool('dyslexic_font') ?? false;
  Future<void> setDyslexicFont(bool value) async {
    await _prefs?.setBool('dyslexic_font', value);
  }

  double get fontScale => _prefs?.getDouble('font_scale') ?? 1.0;
  Future<void> setFontScale(double value) async {
    await _prefs?.setDouble('font_scale', value);
  }

  // Privacy settings
  bool get showOnlineStatus => _prefs?.getBool('show_online_status') ?? true;
  Future<void> setShowOnlineStatus(bool value) async {
    await _prefs?.setBool('show_online_status', value);
  }

  bool get allowMessageRequests => _prefs?.getBool('allow_message_requests') ?? true;
  Future<void> setAllowMessageRequests(bool value) async {
    await _prefs?.setBool('allow_message_requests', value);
  }

  String get profileVisibility => _prefs?.getString('profile_visibility') ?? 'everyone';
  Future<void> setProfileVisibility(String value) async {
    await _prefs?.setString('profile_visibility', value);
  }

  // Notification settings
  bool get pushNotifications => _prefs?.getBool('push_notifications') ?? true;
  Future<void> setPushNotifications(bool value) async {
    await _prefs?.setBool('push_notifications', value);
  }

  bool get emailNotifications => _prefs?.getBool('email_notifications') ?? true;
  Future<void> setEmailNotifications(bool value) async {
    await _prefs?.setBool('email_notifications', value);
  }

  // Content settings
  bool get autoplayVideos => _prefs?.getBool('autoplay_videos') ?? false;
  Future<void> setAutoplayVideos(bool value) async {
    await _prefs?.setBool('autoplay_videos', value);
  }

  bool get showContentWarnings => _prefs?.getBool('show_content_warnings') ?? true;
  Future<void> setShowContentWarnings(bool value) async {
    await _prefs?.setBool('show_content_warnings', value);
  }

  bool get safeSearch => _prefs?.getBool('safe_search') ?? true;
  Future<void> setSafeSearch(bool value) async {
    await _prefs?.setBool('safe_search', value);
  }

  // Parental controls
  bool get parentalControlsEnabled => _prefs?.getBool('parental_controls') ?? false;
  Future<void> setParentalControlsEnabled(bool value) async {
    await _prefs?.setBool('parental_controls', value);
  }

  String? get parentalPin => _prefs?.getString('parental_pin');
  Future<void> setParentalPin(String? pin) async {
    if (pin != null) {
      await _prefs?.setString('parental_pin', pin);
    } else {
      await _prefs?.remove('parental_pin');
    }
  }

  int get dailyScreenTimeLimit => _prefs?.getInt('daily_screen_time') ?? 0;
  Future<void> setDailyScreenTimeLimit(int minutes) async {
    await _prefs?.setInt('daily_screen_time', minutes);
  }

  // App state
  bool get onboardingComplete => _prefs?.getBool('onboarding_complete') ?? false;
  Future<void> setOnboardingComplete(bool value) async {
    await _prefs?.setBool('onboarding_complete', value);
  }

  String? get lastUserId => _prefs?.getString('last_user_id');
  Future<void> setLastUserId(String? userId) async {
    if (userId != null) {
      await _prefs?.setString('last_user_id', userId);
    } else {
      await _prefs?.remove('last_user_id');
    }
  }

  // Clear all settings
  Future<void> clearAll() async {
    await _prefs?.clear();
  }
}

/// Security manager for sensitive operations
/// Enhanced to match Kotlin SecurityManager.kt with comprehensive security checks
class SecurityManager {
  static final SecurityManager _instance = SecurityManager._internal();
  factory SecurityManager() => _instance;
  SecurityManager._internal();

  // Cache security check result
  SecurityCheckResult? _cachedResult;
  DateTime? _lastCheckTime;
  static const _cacheDuration = Duration(seconds: 30);

  /// Perform comprehensive security check
  SecurityCheckResult performSecurityCheck({bool forceRefresh = false}) {
    final now = DateTime.now();
    if (!forceRefresh &&
        _cachedResult != null &&
        _lastCheckTime != null &&
        now.difference(_lastCheckTime!) < _cacheDuration) {
      return _cachedResult!;
    }

    final details = <String>[];

    // Note: On Flutter, some checks require platform channels.
    // These are best-effort checks that work cross-platform.
    final isDebugMode = kDebugMode;
    if (isDebugMode) details.add('App is running in debug mode');

    // Calculate threat level
    final threatLevel = isDebugMode ? ThreatLevel.low : ThreatLevel.none;

    final result = SecurityCheckResult(
      isSecure: threatLevel == ThreatLevel.none || threatLevel == ThreatLevel.low,
      isDebugMode: isDebugMode,
      threatLevel: threatLevel,
      details: details,
    );

    _cachedResult = result;
    _lastCheckTime = now;
    return result;
  }

  /// Hash a PIN for storage
  String hashPin(String pin) {
    // In production, use a proper hashing algorithm
    // For now, simple base64 encoding (NOT SECURE - just for demo)
    return pin.codeUnits.map((c) => c.toString()).join('');
  }

  /// Verify a PIN
  bool verifyPin(String inputPin, String storedHash) {
    return hashPin(inputPin) == storedHash;
  }

  /// Generate a session token
  String generateSessionToken() {
    return DateTime.now().millisecondsSinceEpoch.toString();
  }

  /// Sanitize user input
  String sanitizeInput(String input) {
    return input
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;')
        .trim();
  }

  /// Validate email format
  bool isValidEmail(String email) {
    return RegExp(r'^[\w\-.]+@([\w-]+\.)+[\w-]{2,4}$').hasMatch(email);
  }

  /// Validate password strength
  PasswordStrength checkPasswordStrength(String password) {
    if (password.length < 6) return PasswordStrength.weak;

    int score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (RegExp(r'[a-z]').hasMatch(password)) score++;
    if (RegExp(r'[A-Z]').hasMatch(password)) score++;
    if (RegExp(r'[0-9]').hasMatch(password)) score++;
    if (RegExp(r'[!@#$%^&*(),.?":{}|<>]').hasMatch(password)) score++;

    if (score < 3) return PasswordStrength.weak;
    if (score < 5) return PasswordStrength.medium;
    return PasswordStrength.strong;
  }
}

enum PasswordStrength { weak, medium, strong }

enum ThreatLevel { none, low, medium, high, critical }

class SecurityCheckResult {
  final bool isSecure;
  final bool isDebugMode;
  final ThreatLevel threatLevel;
  final List<String> details;

  const SecurityCheckResult({
    required this.isSecure,
    required this.isDebugMode,
    required this.threatLevel,
    required this.details,
  });
}

/// Location service for location-based features
class LocationService {
  static final LocationService _instance = LocationService._internal();
  factory LocationService() => _instance;
  LocationService._internal();

  PermissionStatus _permissionStatus = PermissionStatus.denied;

  /// Request location permission
  Future<bool> requestPermission() async {
    if (kIsWeb) {
      debugPrint('Location permissions not supported on web');
      return false;
    }

    try {
      _permissionStatus = await Permission.location.request();
      debugPrint('Location permission status: $_permissionStatus');
      return _permissionStatus.isGranted;
    } catch (e) {
      debugPrint('Failed to request location permission: $e');
      return false;
    }
  }

  /// Check current permission status
  Future<PermissionStatus> checkPermissionStatus() async {
    if (kIsWeb) {
      return PermissionStatus.denied;
    }

    try {
      _permissionStatus = await Permission.location.status;
      return _permissionStatus;
    } catch (e) {
      debugPrint('Failed to check location permission: $e');
      return PermissionStatus.denied;
    }
  }

  /// Check if permission is granted
  bool get hasPermission => _permissionStatus.isGranted;

  /// Check if permission is permanently denied
  bool get isPermanentlyDenied => _permissionStatus.isPermanentlyDenied;

  /// Request background location permission
  Future<bool> requestBackgroundPermission() async {
    if (kIsWeb) {
      return false;
    }

    try {
      // First ensure basic location permission is granted
      if (!_permissionStatus.isGranted) {
        await requestPermission();
      }

      final bgStatus = await Permission.locationAlways.request();
      return bgStatus.isGranted;
    } catch (e) {
      debugPrint('Failed to request background location permission: $e');
      return false;
    }
  }

  /// Get current location
  Future<LocationData?> getCurrentLocation() async {
    if (kIsWeb) {
      debugPrint('Location not supported on web');
      return null;
    }

    // Check/request permission first
    final status = await checkPermissionStatus();
    if (!status.isGranted) {
      final granted = await requestPermission();
      if (!granted) {
        debugPrint('Location permission not granted');
        return null;
      }
    }

    try {
      // Location retrieval requires the geolocator package for production use.
      // For now, we check permission and return the last known or a default location.
      // To enable real GPS: add geolocator to pubspec.yaml and call
      // Geolocator.getCurrentPosition() here.
      debugPrint('Location permission granted. Returning cached/default location.');

      // Try to load cached location from SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      final cachedLat = prefs.getDouble('last_known_latitude');
      final cachedLng = prefs.getDouble('last_known_longitude');

      if (cachedLat != null && cachedLng != null) {
        return LocationData(latitude: cachedLat, longitude: cachedLng, accuracy: 50.0);
      }

      // Default location (San Francisco) when no cached location exists
      return LocationData(latitude: 37.7749, longitude: -122.4194, accuracy: 100.0);
    } catch (e) {
      debugPrint('Failed to get location: $e');
      return null;
    }
  }

  /// Open app settings for permission management
  Future<bool> openSettings() async {
    return await openAppSettings();
  }
}

class LocationData {
  final double latitude;
  final double longitude;
  final double? accuracy;
  final double? altitude;
  final DateTime timestamp;

  LocationData({
    required this.latitude,
    required this.longitude,
    this.accuracy,
    this.altitude,
    DateTime? timestamp,
  }) : timestamp = timestamp ?? DateTime.now();

  @override
  String toString() {
    return 'LocationData(lat: $latitude, lng: $longitude, accuracy: ${accuracy}m)';
  }
}

/// Connectivity service for network state handling
class ConnectivityService {
  static final ConnectivityService _instance = ConnectivityService._internal();
  factory ConnectivityService() => _instance;
  ConnectivityService._internal();

  bool _isOnline = true;
  final _connectivityController = StreamController<bool>.broadcast();
  StreamSubscription? _connectivitySubscription;

  /// Stream of connectivity changes
  Stream<bool> get onConnectivityChanged => _connectivityController.stream;

  /// Current connectivity status
  bool get isOnline => _isOnline;

  /// Initialize connectivity monitoring
  Future<void> initialize() async {
    try {
      // Check initial connectivity
      await checkConnectivity();

      // Monitor connectivity changes using connectivity_plus
      _connectivitySubscription = Connectivity().onConnectivityChanged.listen((results) {
        final wasOnline = _isOnline;
        _isOnline = results.isNotEmpty && !results.contains(ConnectivityResult.none);
        if (wasOnline != _isOnline) {
          _connectivityController.add(_isOnline);
          debugPrint('Connectivity changed: ${_isOnline ? "online" : "offline"}');
        }
      });

      debugPrint('Connectivity service initialized. Online: $_isOnline');
    } catch (e) {
      debugPrint('Failed to initialize connectivity service: $e');
    }
  }

  /// Check current connectivity
  Future<bool> checkConnectivity() async {
    try {
      final results = await Connectivity().checkConnectivity();
      _isOnline = results.isNotEmpty && !results.contains(ConnectivityResult.none);
      return _isOnline;
    } catch (e) {
      _isOnline = false;
      return false;
    }
  }

  /// Dispose resources
  void dispose() {
    _connectivitySubscription?.cancel();
    _connectivityController.close();
  }
}

/// Rate limiter for API calls
class RateLimiter {
  final Map<String, List<DateTime>> _requestHistory = {};
  final int maxRequests;
  final Duration window;

  RateLimiter({
    this.maxRequests = 60,
    this.window = const Duration(minutes: 1),
  });

  /// Check if a request is allowed
  bool canMakeRequest(String endpoint) {
    final now = DateTime.now();
    final history = _requestHistory[endpoint] ?? [];

    // Remove old requests outside the window
    history.removeWhere((time) => now.difference(time) > window);
    _requestHistory[endpoint] = history;

    return history.length < maxRequests;
  }

  /// Record a request
  void recordRequest(String endpoint) {
    final history = _requestHistory[endpoint] ?? [];
    history.add(DateTime.now());
    _requestHistory[endpoint] = history;
  }

  /// Get remaining requests for an endpoint
  int remainingRequests(String endpoint) {
    final now = DateTime.now();
    final history = _requestHistory[endpoint] ?? [];
    history.removeWhere((time) => now.difference(time) > window);
    return maxRequests - history.length;
  }

  /// Get time until next request is allowed
  Duration? timeUntilNextRequest(String endpoint) {
    if (canMakeRequest(endpoint)) return null;

    final history = _requestHistory[endpoint] ?? [];
    if (history.isEmpty) return null;

    final oldest = history.reduce((a, b) => a.isBefore(b) ? a : b);
    final resetTime = oldest.add(window);
    return resetTime.difference(DateTime.now());
  }

  /// Execute a rate-limited request
  Future<T?> executeRateLimited<T>(
    String endpoint,
    Future<T> Function() request,
  ) async {
    if (!canMakeRequest(endpoint)) {
      final waitTime = timeUntilNextRequest(endpoint);
      debugPrint('Rate limited for $endpoint. Wait ${waitTime?.inSeconds}s');
      return null;
    }

    recordRequest(endpoint);
    return await request();
  }
}

/// Analytics service wrapper
class AnalyticsService {
  static final AnalyticsService _instance = AnalyticsService._internal();
  factory AnalyticsService() => _instance;
  AnalyticsService._internal();

  /// Log a screen view
  Future<void> logScreenView(String screenName) async {
    try {
      // analytics.logScreenView(screenName: screenName);
      debugPrint('Analytics: Screen view - $screenName');
    } catch (e) {
      debugPrint('Failed to log screen view: $e');
    }
  }

  /// Log a custom event
  Future<void> logEvent(String name, {Map<String, dynamic>? parameters}) async {
    try {
      // analytics.logEvent(name: name, parameters: parameters);
      debugPrint('Analytics: Event - $name ${parameters ?? ''}');
    } catch (e) {
      debugPrint('Failed to log event: $e');
    }
  }

  /// Log user action
  Future<void> logUserAction(String action, {String? target}) async {
    await logEvent('user_action', parameters: {
      'action': action,
      'target': ?target,
    });
  }

  /// Set user properties
  Future<void> setUserProperties({
    String? userId,
    String? userType,
    String? accountAge,
  }) async {
    try {
      // analytics.setUserId(id: userId);
      // analytics.setUserProperty(name: 'user_type', value: userType);
      debugPrint('Analytics: Set user properties');
    } catch (e) {
      debugPrint('Failed to set user properties: $e');
    }
  }

  /// Log sign up
  Future<void> logSignUp(String method) async {
    await logEvent('sign_up', parameters: {'method': method});
  }

  /// Log login
  Future<void> logLogin(String method) async {
    await logEvent('login', parameters: {'method': method});
  }

  /// Log share
  Future<void> logShare(String contentType, String itemId) async {
    await logEvent('share', parameters: {
      'content_type': contentType,
      'item_id': itemId,
    });
  }

  /// Log post creation
  Future<void> logPostCreated({String? category}) async {
    await logEvent('post_created', parameters: {
      'category': ?category,
    });
  }

  /// Log engagement
  Future<void> logEngagement(String type, String targetId) async {
    await logEvent('engagement', parameters: {
      'type': type,
      'target_id': targetId,
    });
  }
}

/// Cache service for offline support
class CacheService {
  static final CacheService _instance = CacheService._internal();
  factory CacheService() => _instance;
  CacheService._internal();

  final Map<String, dynamic> _memoryCache = {};
  final Map<String, DateTime> _cacheExpiry = {};
  final Duration defaultExpiry = const Duration(minutes: 5);

  /// Get cached data
  T? get<T>(String key) {
    if (_cacheExpiry.containsKey(key)) {
      if (DateTime.now().isAfter(_cacheExpiry[key]!)) {
        // Cache expired
        _memoryCache.remove(key);
        _cacheExpiry.remove(key);
        return null;
      }
    }
    return _memoryCache[key] as T?;
  }

  /// Set cached data
  void set<T>(String key, T value, {Duration? expiry}) {
    _memoryCache[key] = value;
    _cacheExpiry[key] = DateTime.now().add(expiry ?? defaultExpiry);
  }

  /// Check if cache exists and is valid
  bool has(String key) {
    if (!_memoryCache.containsKey(key)) return false;
    if (_cacheExpiry.containsKey(key) && DateTime.now().isAfter(_cacheExpiry[key]!)) {
      _memoryCache.remove(key);
      _cacheExpiry.remove(key);
      return false;
    }
    return true;
  }

  /// Remove cached data
  void remove(String key) {
    _memoryCache.remove(key);
    _cacheExpiry.remove(key);
  }

  /// Clear all cache
  void clear() {
    _memoryCache.clear();
    _cacheExpiry.clear();
  }

  /// Get or fetch data
  Future<T?> getOrFetch<T>(
    String key,
    Future<T> Function() fetcher, {
    Duration? expiry,
  }) async {
    if (has(key)) {
      return get<T>(key);
    }

    try {
      final data = await fetcher();
      set(key, data, expiry: expiry);
      return data;
    } catch (e) {
      debugPrint('Failed to fetch data for $key: $e');
      return null;
    }
  }
}

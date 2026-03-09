import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:firebase_analytics/firebase_analytics.dart';
import 'core/theme/app_theme.dart';
import 'providers/theme_provider.dart';
import 'l10n/app_localizations.dart';
import 'router/app_router.dart';
import 'core/utils/security_utils.dart';
import 'screens/settings/dev_options_screen.dart';

/// Global analytics instance
late final FirebaseAnalytics analytics;

void main() async {
  // Ensure Flutter bindings are initialized FIRST
  WidgetsFlutterBinding.ensureInitialized();

  // Set system UI overlay style immediately for faster perceived load
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.dark,
    systemNavigationBarColor: Color(0xFF1C1B1F),
    systemNavigationBarIconBrightness: Brightness.light,
  ));

  // Initialize Firebase first (required for Crashlytics)
  await _initializeFirebase();

  // Run initialization in parallel where possible
  await Future.wait([
    // Set preferred orientations
    if (!kIsWeb)
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.portraitUp,
        DeviceOrientation.portraitDown,
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]),
    // Initialize Supabase (non-blocking if it fails)
    _initializeSupabase(),
    // Pre-warm image cache
    if (!kIsWeb) _preWarmCaches(),
  ].whereType<Future>());

  // Enable more aggressive garbage collection on low-memory devices
  if (!kIsWeb) {
    // Reduce image cache size for better RAM management
    PaintingBinding.instance.imageCache.maximumSize = 50; // Max 50 images
    PaintingBinding.instance.imageCache.maximumSizeBytes = 50 * 1024 * 1024; // 50MB max
  }

  // Run app with error handling
  runZonedGuarded(() {
    runApp(
      const ProviderScope(
        child: NeuroCometApp(),
      ),
    );
  }, (error, stackTrace) {
    debugPrint('Uncaught error: $error');
    debugPrint('Stack trace: $stackTrace');
    // Log to Crashlytics in release mode
    if (!kDebugMode) {
      FirebaseCrashlytics.instance.recordError(error, stackTrace, fatal: true);
    }
  });
}

/// Initialize Firebase services (Crashlytics, Analytics)
Future<void> _initializeFirebase() async {
  try {
    await Firebase.initializeApp();

    // Initialize Analytics
    analytics = FirebaseAnalytics.instance;
    await analytics.setAnalyticsCollectionEnabled(!kDebugMode);

    // Initialize Crashlytics
    if (!kIsWeb) {
      // Pass all uncaught errors to Crashlytics
      FlutterError.onError = (errorDetails) {
        if (kDebugMode) {
          FlutterError.dumpErrorToConsole(errorDetails);
        } else {
          FirebaseCrashlytics.instance.recordFlutterFatalError(errorDetails);
        }
      };

      // Pass all uncaught asynchronous errors to Crashlytics
      PlatformDispatcher.instance.onError = (error, stack) {
        if (!kDebugMode) {
          FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
        }
        return true;
      };

      // Disable Crashlytics in debug mode
      await FirebaseCrashlytics.instance.setCrashlyticsCollectionEnabled(!kDebugMode);

      debugPrint('Firebase Crashlytics initialized');
    }

    debugPrint('Firebase initialized successfully');
  } catch (e) {
    debugPrint('Firebase initialization error: $e');
  }
}

/// Initialize Supabase without blocking app startup
Future<void> _initializeSupabase() async {
  try {
    // Read compile-time config only; do not ship checked-in fallback credentials.
    final envUrl = const String.fromEnvironment('SUPABASE_URL');
    final envKey = const String.fromEnvironment('SUPABASE_ANON_KEY');

    final url = SecurityUtils.decrypt(envUrl).ifEmpty(() => envUrl).trim();
    final key = SecurityUtils.decrypt(envKey).ifEmpty(() => envKey).trim();

    final hasValidUrl = url.isNotEmpty && !url.contains('your-project');
    final hasValidKey = key.isNotEmpty && !key.contains('your-key');

    if (!hasValidUrl || !hasValidKey) {
      debugPrint(
        'Supabase not initialized: missing SUPABASE_URL and/or SUPABASE_ANON_KEY dart-define values.',
      );
      return;
    }

    await Supabase.initialize(
      url: url,
      anonKey: key,
    );

    debugPrint('Supabase initialized successfully');
  } catch (e) {
    debugPrint('Supabase initialization error: $e');
  }
}

/// Pre-warm caches for faster initial render
Future<void> _preWarmCaches() async {
  // This runs in parallel with other initialization
  // Add any pre-warming logic here if needed
}

extension StringExtension on String {
  String ifEmpty(String Function() defaultValue) =>
      isEmpty || this == 'null' ? defaultValue() : this;
}

class NeuroCometApp extends ConsumerWidget {
  const NeuroCometApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);
    final neuroState = ref.watch(neuroStateProvider);
    final locale = ref.watch(localeProvider);
    final highContrast = ref.watch(highContrastProvider);
    final fontSettings = ref.watch(fontSettingsProvider);
    final devOptions = ref.watch(devOptionsProvider);

    // Determine if dark mode is active
    final isDarkMode = themeMode == ThemeMode.dark ||
        (themeMode == ThemeMode.system &&
            WidgetsBinding.instance.platformDispatcher.platformBrightness == Brightness.dark);

    // Update system UI colors based on theme
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: isDarkMode ? Brightness.light : Brightness.dark,
      statusBarBrightness: isDarkMode ? Brightness.dark : Brightness.light,
      systemNavigationBarColor: isDarkMode ? const Color(0xFF1C1B1F) : Colors.white,
      systemNavigationBarIconBrightness: isDarkMode ? Brightness.light : Brightness.dark,
    ));

    return MaterialApp.router(
      title: 'NeuroComet',
      debugShowCheckedModeBanner: false,

      // Feature flag: show Flutter performance overlay
      showPerformanceOverlay: devOptions.showPerformanceOverlay,

      // Dynamic Theme based on NeuroState with accessibility settings
      theme: AppTheme.getTheme(
        neuroState,
        Brightness.light,
        highContrast: highContrast,
        textScale: fontSettings.scale,
        useDyslexicFont: fontSettings.useDyslexicFont,
      ),
      darkTheme: AppTheme.getTheme(
        neuroState,
        Brightness.dark,
        highContrast: highContrast,
        textScale: fontSettings.scale,
        useDyslexicFont: fontSettings.useDyslexicFont,
      ),
      themeMode: themeMode,

      // Localization
      locale: locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: AppLocalizations.supportedLocales,

      scrollBehavior: const _AdaptiveScrollBehavior(),
      routerConfig: AppRouter.router,

      builder: (context, child) {
        final mediaQuery = MediaQuery.of(context);
        final fontScale = ref.watch(fontScaleProvider);

        // Combine system text scaling with app-specific scaling
        final constrainedTextScaler = mediaQuery.textScaler.clamp(
          minScaleFactor: 0.8 * fontScale,
          maxScaleFactor: 1.4 * fontScale,
        );

        return MediaQuery(
          data: mediaQuery.copyWith(textScaler: constrainedTextScaler),
          child: child ?? const SizedBox.shrink(),
        );
      },
    );
  }
}

/// Adaptive scroll behavior that works across all platforms
class _AdaptiveScrollBehavior extends MaterialScrollBehavior {
  const _AdaptiveScrollBehavior();

  @override
  Set<PointerDeviceKind> get dragDevices {
    return <PointerDeviceKind>{
      PointerDeviceKind.touch,
      PointerDeviceKind.mouse,
      PointerDeviceKind.trackpad,
      PointerDeviceKind.stylus,
    };
  }

  @override
  Widget buildScrollbar(
    BuildContext context,
    Widget child,
    ScrollableDetails details,
  ) {
    if (kIsWeb) {
      return Scrollbar(
        controller: details.controller,
        thumbVisibility: false,
        child: child,
      );
    }
    return child;
  }

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) {
    return const BouncingScrollPhysics(
      parent: AlwaysScrollableScrollPhysics(),
    );
  }
}


import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../core/theme/neuro_state.dart';
import '../core/theme/theme_settings_models.dart';

export '../core/theme/theme_settings_models.dart';

/// Theme mode provider for app-wide theme state
final themeModeProvider = NotifierProvider<ThemeModeNotifier, ThemeMode>(
  ThemeModeNotifier.new,
);

/// Neuro state provider for neuro-centric theming
final neuroStateProvider = NotifierProvider<NeuroStateNotifier, NeuroState>(
  NeuroStateNotifier.new,
);

/// Locale provider for app-wide language state
final localeProvider = NotifierProvider<LocaleNotifier, Locale?>(
  LocaleNotifier.new,
);

/// Font scale provider for accessibility (now part of FontSettings, kept for backward compatibility)
final fontScaleProvider = Provider<double>((ref) {
  return ref.watch(fontSettingsProvider).scale;
});

/// Dyslexic font provider (now part of FontSettings, kept for backward compatibility)
final useDyslexicFontProvider = Provider<bool>((ref) {
  return ref.watch(fontSettingsProvider).useDyslexicFont;
});

/// Reduced motion provider for accessibility
/// This now maps to the master toggle in AnimationSettings
final reducedMotionProvider = Provider<bool>((ref) {
  return ref.watch(animationSettingsProvider).disableAll;
});

/// High contrast mode provider
final highContrastProvider = NotifierProvider<HighContrastNotifier, bool>(
  HighContrastNotifier.new,
);

/// Detailed animation settings provider
final animationSettingsProvider = NotifierProvider<AnimationSettingsNotifier, AnimationSettings>(
  AnimationSettingsNotifier.new,
);

/// Detailed font settings provider
final fontSettingsProvider = NotifierProvider<FontSettingsNotifier, FontSettings>(
  FontSettingsNotifier.new,
);


/// Theme mode state notifier
class ThemeModeNotifier extends Notifier<ThemeMode> {
  static const _key = 'theme_mode';

  @override
  ThemeMode build() {
    _loadFromPrefs();
    return ThemeMode.system;
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final value = prefs.getString(_key);
    if (value != null) {
      state = ThemeMode.values.firstWhere(
        (mode) => mode.name == value,
        orElse: () => ThemeMode.system,
      );
    }
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    state = mode;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, mode.name);
  }

  void toggleTheme() {
    if (state == ThemeMode.light) {
      setThemeMode(ThemeMode.dark);
    } else {
      setThemeMode(ThemeMode.light);
    }
  }
}

/// Neuro state state notifier
class NeuroStateNotifier extends Notifier<NeuroState> {
  static const _key = 'neuro_state';

  @override
  NeuroState build() {
    _loadFromPrefs();
    return NeuroState.defaultState;
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final value = prefs.getString(_key);
    if (value != null) {
      state = NeuroState.values.firstWhere(
        (s) => s.name == value,
        orElse: () => NeuroState.defaultState,
      );
    }
  }

  Future<void> setNeuroState(NeuroState neuroState) async {
    state = neuroState;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, neuroState.name);
  }
}

/// Locale state notifier for language settings
class LocaleNotifier extends Notifier<Locale?> {
  static const _key = 'locale';

  @override
  Locale? build() {
    _loadFromPrefs();
    return null;
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final languageCode = prefs.getString(_key);
    if (languageCode != null) {
      state = Locale(languageCode);
    }
  }

  Future<void> setLocale(Locale? locale) async {
    state = locale;
    final prefs = await SharedPreferences.getInstance();
    if (locale != null) {
      await prefs.setString(_key, locale.languageCode);
    } else {
      await prefs.remove(_key);
    }
  }
}

/// High contrast mode notifier
class HighContrastNotifier extends Notifier<bool> {
  static const _key = 'high_contrast';

  @override
  bool build() {
    _loadFromPrefs();
    return false;
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getBool(_key) ?? false;
  }

  Future<void> setHighContrast(bool value) async {
    state = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_key, value);
  }

  void toggle() {
    setHighContrast(!state);
  }
}

/// Animation Settings Notifier
class AnimationSettingsNotifier extends Notifier<AnimationSettings> {
  static const _keyPrefix = 'anim_';

  @override
  AnimationSettings build() {
    _loadFromPrefs();
    return const AnimationSettings();
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    state = AnimationSettings(
      disableAll: prefs.getBool('${_keyPrefix}all') ?? false,
      disableLogo: prefs.getBool('${_keyPrefix}logo') ?? false,
      disableStory: prefs.getBool('${_keyPrefix}story') ?? false,
      disableFeed: prefs.getBool('${_keyPrefix}feed') ?? false,
      disableTransitions: prefs.getBool('${_keyPrefix}trans') ?? false,
      disableButtons: prefs.getBool('${_keyPrefix}btn') ?? false,
      disableLoading: prefs.getBool('${_keyPrefix}load') ?? false,
    );
  }

  Future<void> updateSettings(AnimationSettings newSettings) async {
    state = newSettings;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('${_keyPrefix}all', newSettings.disableAll);
    await prefs.setBool('${_keyPrefix}logo', newSettings.disableLogo);
    await prefs.setBool('${_keyPrefix}story', newSettings.disableStory);
    await prefs.setBool('${_keyPrefix}feed', newSettings.disableFeed);
    await prefs.setBool('${_keyPrefix}trans', newSettings.disableTransitions);
    await prefs.setBool('${_keyPrefix}btn', newSettings.disableButtons);
    await prefs.setBool('${_keyPrefix}load', newSettings.disableLoading);
  }

  Future<void> setReducedMotion(bool value) async {
    await updateSettings(state.copyWith(disableAll: value));
  }
}

/// Font Settings Notifier
class FontSettingsNotifier extends Notifier<FontSettings> {
  static const _keyPrefix = 'font_';

  @override
  FontSettings build() {
    _loadFromPrefs();
    return const FontSettings();
  }

  Future<void> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    state = FontSettings(
      scale: prefs.getDouble('${_keyPrefix}scale') ?? 1.0,
      letterSpacing: prefs.getDouble('${_keyPrefix}spacing') ?? 0.0,
      lineHeight: prefs.getDouble('${_keyPrefix}height') ?? 1.0,
      useDyslexicFont: prefs.getBool('${_keyPrefix}dyslexic') ?? false,
      // FontWeight is tricky to persist directly, simplified for now
    );
  }

  Future<void> updateSettings(FontSettings newSettings) async {
    state = newSettings;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble('${_keyPrefix}scale', newSettings.scale);
    await prefs.setDouble('${_keyPrefix}spacing', newSettings.letterSpacing);
    await prefs.setDouble('${_keyPrefix}height', newSettings.lineHeight);
    await prefs.setBool('${_keyPrefix}dyslexic', newSettings.useDyslexicFont);
  }

  Future<void> setScale(double scale) async {
    await updateSettings(state.copyWith(scale: scale));
  }

  Future<void> setUseDyslexicFont(bool value) async {
    await updateSettings(state.copyWith(useDyslexicFont: value));
  }
}

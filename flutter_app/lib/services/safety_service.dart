import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/dev_options.dart';

class SafetyState {
  final Audience audience;
  final KidsFilterLevel kidsFilterLevel;
  final bool isParentalPinSet;
  final bool hasVerified;

  const SafetyState({
    this.audience = Audience.adult,
    this.kidsFilterLevel = KidsFilterLevel.moderate,
    this.isParentalPinSet = false,
    this.hasVerified = false,
  });

  bool get isKidsMode => audience == Audience.under13;

  SafetyState copyWith({
    Audience? audience,
    KidsFilterLevel? kidsFilterLevel,
    bool? isParentalPinSet,
    bool? hasVerified,
  }) {
    return SafetyState(
      audience: audience ?? this.audience,
      kidsFilterLevel: kidsFilterLevel ?? this.kidsFilterLevel,
      isParentalPinSet: isParentalPinSet ?? this.isParentalPinSet,
      hasVerified: hasVerified ?? this.hasVerified,
    );
  }
}

class SafetyService {
  static final SafetyService _instance = SafetyService._internal();
  factory SafetyService() => _instance;
  SafetyService._internal();

  static const _keyAudience = 'verified_audience';
  static const _keyPin = 'parental_pin';

  SafetyState _state = const SafetyState();
  SafetyState get state => _state;

  final _listeners = <VoidCallback>[];

  void addListener(VoidCallback listener) => _listeners.add(listener);
  void removeListener(VoidCallback listener) => _listeners.remove(listener);

  void _notify() {
    for (final listener in _listeners) {
      listener();
    }
  }

  Future<void> initialize() async {
    final prefs = await SharedPreferences.getInstance();
    final audienceStr = prefs.getString(_keyAudience);
    final pin = prefs.getString(_keyPin);

    Audience audience = Audience.adult;
    bool hasVerified = false;
    if (audienceStr != null) {
      hasVerified = true;
      audience = Audience.values.firstWhere(
        (e) => e.name == audienceStr,
        orElse: () => Audience.adult,
      );
    }

    _state = SafetyState(
      audience: audience,
      kidsFilterLevel: audience == Audience.under13 ? KidsFilterLevel.strict : KidsFilterLevel.moderate,
      isParentalPinSet: pin != null,
      hasVerified: hasVerified,
    );
    _notify();
  }

  Future<void> setAudience(Audience? audience) async {
    if (audience == null) return;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyAudience, audience.name);
    
    _state = _state.copyWith(
      audience: audience,
      kidsFilterLevel: audience == Audience.under13 ? KidsFilterLevel.strict : KidsFilterLevel.moderate,
      hasVerified: true,
    );
    _notify();
  }

  bool needsVerification(DevOptions opts) {
    if (opts.bypassAgeVerification) return false;
    return !_state.hasVerified;
  }
}

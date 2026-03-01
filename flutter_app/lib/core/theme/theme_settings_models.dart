import 'package:flutter/foundation.dart';
import 'package:flutter/painting.dart' show FontWeight;

/// Detailed animation settings for neurodivergent-friendly experience.
@immutable
class AnimationSettings {
  final bool disableAll; // Master toggle
  final bool disableLogo; // Rainbow infinity, shimmer text
  final bool disableStory; // Story circle spinning
  final bool disableFeed; // Post card animations
  final bool disableTransitions; // Screen transitions
  final bool disableButtons; // Button press effects
  final bool disableLoading; // Loading spinners

  const AnimationSettings({
    this.disableAll = false,
    this.disableLogo = false,
    this.disableStory = false,
    this.disableFeed = false,
    this.disableTransitions = false,
    this.disableButtons = false,
    this.disableLoading = false,
  });

  AnimationSettings copyWith({
    bool? disableAll,
    bool? disableLogo,
    bool? disableStory,
    bool? disableFeed,
    bool? disableTransitions,
    bool? disableButtons,
    bool? disableLoading,
  }) {
    return AnimationSettings(
      disableAll: disableAll ?? this.disableAll,
      disableLogo: disableLogo ?? this.disableLogo,
      disableStory: disableStory ?? this.disableStory,
      disableFeed: disableFeed ?? this.disableFeed,
      disableTransitions: disableTransitions ?? this.disableTransitions,
      disableButtons: disableButtons ?? this.disableButtons,
      disableLoading: disableLoading ?? this.disableLoading,
    );
  }

  bool shouldAnimate(AnimationType type) {
    if (disableAll) return false;
    switch (type) {
      case AnimationType.logo:
        return !disableLogo;
      case AnimationType.story:
        return !disableStory;
      case AnimationType.feed:
        return !disableFeed;
      case AnimationType.transition:
        return !disableTransitions;
      case AnimationType.button:
        return !disableButtons;
      case AnimationType.loading:
        return !disableLoading;
    }
  }
}

enum AnimationType {
  logo,
  story,
  feed,
  transition,
  button,
  loading,
}

/// Font and reading settings
@immutable
class FontSettings {
  final double scale;
  final double letterSpacing;
  final double lineHeight;
  final FontWeight fontWeightDelta; // e.g., 0 for normal, 1 for bold
  final bool useDyslexicFont;

  const FontSettings({
    this.scale = 1.0,
    this.letterSpacing = 0.0,
    this.lineHeight = 1.0, // Multiplier
    this.fontWeightDelta = FontWeight.normal,
    this.useDyslexicFont = false,
  });

  FontSettings copyWith({
    double? scale,
    double? letterSpacing,
    double? lineHeight,
    FontWeight? fontWeightDelta,
    bool? useDyslexicFont,
  }) {
    return FontSettings(
      scale: scale ?? this.scale,
      letterSpacing: letterSpacing ?? this.letterSpacing,
      lineHeight: lineHeight ?? this.lineHeight,
      fontWeightDelta: fontWeightDelta ?? this.fontWeightDelta,
      useDyslexicFont: useDyslexicFont ?? this.useDyslexicFont,
    );
  }
}

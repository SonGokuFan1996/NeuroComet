import 'neuro_state.dart';

enum SplashAnimationStyle {
  calmWaves,
  focusPulse,
  routineGrid,
  energyBurst,
  groundingEarth,
  creativeSwirl,
  gentleFloat,
  sensorySparkle,
  contrastRings,
  patternShapes,
  rainbowSparkle,
}

class SplashConfig {
  final List<String> messages;
  final String tagline;
  final SplashAnimationStyle animationStyle;
  final int durationMs;

  const SplashConfig({
    required this.messages,
    required this.tagline,
    required this.animationStyle,
    this.durationMs = 2000,
  });
}

SplashConfig getSplashConfigForState(NeuroState state) {
  switch (state) {
    case NeuroState.defaultState:
      return const SplashConfig(
        messages: ['Welcome back', 'Your space awaits', 'Let\'s begin'],
        tagline: 'A space designed for you',
        animationStyle: SplashAnimationStyle.calmWaves,
      );
    case NeuroState.hyperfocus:
      return const SplashConfig(
        messages: ['Focus mode', 'Clear mind ahead', 'Ready to dive deep'],
        tagline: 'Distraction-free zone',
        animationStyle: SplashAnimationStyle.focusPulse,
        durationMs: 1500,
      );
    case NeuroState.overload:
      return const SplashConfig(
        messages: ['Taking it slow', 'Breathe with me', 'Gentle pace'],
        tagline: 'Low stimulation mode',
        animationStyle: SplashAnimationStyle.calmWaves,
        durationMs: 2500,
      );
    case NeuroState.calm:
      return const SplashConfig(
        messages: ['Peace awaits', 'Serenity mode', 'Calm waters'],
        tagline: 'Your tranquil space',
        animationStyle: SplashAnimationStyle.gentleFloat,
      );
    case NeuroState.adhdEnergized:
      return const SplashConfig(
        messages: ['Let\'s go! ⚡', 'Energy unlocked', 'Time to shine'],
        tagline: 'Ride the wave',
        animationStyle: SplashAnimationStyle.energyBurst,
        durationMs: 1500,
      );
    case NeuroState.adhdLowDopamine:
      return const SplashConfig(
        messages: ['Warming up', 'Finding your spark', 'You\'ve got this'],
        tagline: 'Gentle motivation',
        animationStyle: SplashAnimationStyle.creativeSwirl,
      );
    case NeuroState.adhdTaskMode:
      return const SplashConfig(
        messages: ['Task mode', 'One thing at a time', 'Focus activated'],
        tagline: 'Minimal distractions',
        animationStyle: SplashAnimationStyle.focusPulse,
        durationMs: 1200,
      );
    case NeuroState.autismRoutine:
      return const SplashConfig(
        messages: ['Same as always', 'Familiar patterns', 'Comfort in routine'],
        tagline: 'Predictable & safe',
        animationStyle: SplashAnimationStyle.routineGrid,
      );
    case NeuroState.autismSensorySeek:
      return const SplashConfig(
        messages: ['Sensory joy', 'Feel the patterns', 'Satisfying vibes'],
        tagline: 'Rich experiences await',
        animationStyle: SplashAnimationStyle.sensorySparkle,
      );
    case NeuroState.autismLowStim:
      return const SplashConfig(
        messages: ['Quiet mode', 'Soft & gentle', 'Rest your senses'],
        tagline: 'Minimal stimulation',
        animationStyle: SplashAnimationStyle.gentleFloat,
        durationMs: 2500,
      );
    case NeuroState.anxietySoothe:
      return const SplashConfig(
        messages: ['You\'re safe here', 'Breathe in, breathe out', 'All is well'],
        tagline: 'Calming your mind',
        animationStyle: SplashAnimationStyle.calmWaves,
        durationMs: 2500,
      );
    case NeuroState.anxietyGrounding:
      return const SplashConfig(
        messages: ['Feet on the ground', 'Present moment', 'Stable & secure'],
        tagline: 'Rooted in now',
        animationStyle: SplashAnimationStyle.groundingEarth,
      );
    case NeuroState.dyslexiaFriendly:
      return const SplashConfig(
        messages: ['Clear & readable', 'Your way', 'Easy reading ahead'],
        tagline: 'Designed for clarity',
        animationStyle: SplashAnimationStyle.focusPulse,
      );
    case NeuroState.colorblindDeuter:
      return const SplashConfig(
        messages: ['Colors optimized', 'Blue & orange clarity', 'See with confidence'],
        tagline: 'Deuteranopia friendly',
        animationStyle: SplashAnimationStyle.contrastRings,
      );
    case NeuroState.colorblindProtan:
      return const SplashConfig(
        messages: ['Colors adapted', 'Blue & yellow harmony', 'Clear distinction'],
        tagline: 'Protanopia friendly',
        animationStyle: SplashAnimationStyle.contrastRings,
      );
    case NeuroState.colorblindTritan:
      return const SplashConfig(
        messages: ['Colors refined', 'Pink & teal contrast', 'Visual clarity'],
        tagline: 'Tritanopia friendly',
        animationStyle: SplashAnimationStyle.contrastRings,
      );
    case NeuroState.colorblindMono:
      return const SplashConfig(
        messages: ['Shapes & patterns', 'High contrast', 'Clear boundaries'],
        tagline: 'Pattern-based design',
        animationStyle: SplashAnimationStyle.patternShapes,
      );
    case NeuroState.blindScreenReader:
      return const SplashConfig(
        messages: ['Screen reader ready', 'Accessibility first', 'Welcome'],
        tagline: 'Optimized for TalkBack',
        animationStyle: SplashAnimationStyle.focusPulse,
        durationMs: 2500,
      );
    case NeuroState.blindHighContrast:
      return const SplashConfig(
        messages: ['Maximum contrast', 'Clear and bold', 'Easy to see'],
        tagline: 'Pure black and white',
        animationStyle: SplashAnimationStyle.contrastRings,
      );
    case NeuroState.blindLargeText:
      return const SplashConfig(
        messages: ['Large and clear', 'Easy reading', 'Your way'],
        tagline: 'Extra large text mode',
        animationStyle: SplashAnimationStyle.focusPulse,
        durationMs: 2500,
      );
    case NeuroState.moodTired:
      return const SplashConfig(
        messages: ['Take it easy', 'No rush', 'Gentle start'],
        tagline: 'Rest is okay',
        animationStyle: SplashAnimationStyle.gentleFloat,
        durationMs: 2500,
      );
    case NeuroState.moodAnxious:
      return const SplashConfig(
        messages: ['You\'re okay', 'This too shall pass', 'Safe space'],
        tagline: 'Breathe with us',
        animationStyle: SplashAnimationStyle.calmWaves,
        durationMs: 2500,
      );
    case NeuroState.moodHappy:
      return const SplashConfig(
        messages: ['Hello sunshine! ☀️', 'Great vibes', 'Joy awaits'],
        tagline: 'Let\'s celebrate you',
        animationStyle: SplashAnimationStyle.energyBurst,
        durationMs: 1500,
      );
    case NeuroState.moodOverwhelmed:
      return const SplashConfig(
        messages: ['One breath', 'Slowing down', 'We\'ve got you'],
        tagline: 'Taking it gentle',
        animationStyle: SplashAnimationStyle.calmWaves,
        durationMs: 3000,
      );
    case NeuroState.moodCreative:
      return const SplashConfig(
        messages: ['Inspiration incoming', 'Create freely', 'Imagination mode'],
        tagline: 'Let ideas flow',
        animationStyle: SplashAnimationStyle.creativeSwirl,
      );
    case NeuroState.cinnamonBun:
      return const SplashConfig(
        messages: ['Warm and cozy', 'Sweet comfort', 'Safe and warm'],
        tagline: 'Cozy and comforting',
        animationStyle: SplashAnimationStyle.gentleFloat,
      );
    case NeuroState.rainbowBrain:
      return const SplashConfig(
        messages: [
          '🦄 You found the secret!',
          '🌈 Celebrate your unique mind',
          '✨ Neurodivergence is magic',
          '🧠 Your brain is beautiful',
          '💜 Different is powerful'
        ],
        tagline: 'Embrace your rainbow brain',
        animationStyle: SplashAnimationStyle.rainbowSparkle,
      );
  }
}

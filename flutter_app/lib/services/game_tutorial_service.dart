import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Game Tutorial step
class GameTutorialStep {
  final String emoji;
  final String title;
  final String description;

  const GameTutorialStep({
    required this.emoji,
    required this.title,
    required this.description,
  });
}

/// Game Tutorial data
class GameTutorial {
  final String gameId;
  final String title;
  final List<GameTutorialStep> steps;

  const GameTutorial({
    required this.gameId,
    required this.title,
    required this.steps,
  });
}

/// Manages game tutorial state - tracks which tutorials have been seen
class GameTutorialManager {
  static const String _keyPrefix = 'tutorial_seen_';

  static Future<bool> hasSeenTutorial(String gameId) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool('$_keyPrefix$gameId') ?? false;
  }

  static Future<void> markTutorialSeen(String gameId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('$_keyPrefix$gameId', true);
  }

  static Future<void> resetAllTutorials() async {
    final prefs = await SharedPreferences.getInstance();
    final keys = prefs.getKeys().where((k) => k.startsWith(_keyPrefix));
    for (final key in keys) {
      await prefs.remove(key);
    }
  }

  static GameTutorial? getTutorial(String gameId) => _gameTutorials[gameId];
}

/// All game tutorials
final Map<String, GameTutorial> _gameTutorials = {
  'bubble_pop': const GameTutorial(
    gameId: 'bubble_pop',
    title: 'Bubble Pop',
    steps: [
      GameTutorialStep(
        emoji: '🫧',
        title: 'Pop the Bubbles!',
        description: 'Tap on the colorful bubbles to pop them. Each pop gives satisfying haptic feedback!',
      ),
      GameTutorialStep(
        emoji: '✨',
        title: 'No Pressure',
        description: "There's no time limit or score to beat. Just pop bubbles at your own pace.",
      ),
      GameTutorialStep(
        emoji: '🔄',
        title: 'Endless Fun',
        description: 'New bubbles appear automatically. Pop as many or as few as you like!',
      ),
    ],
  ),
  'fidget_spinner': const GameTutorial(
    gameId: 'fidget_spinner',
    title: 'Fidget Spinner',
    steps: [
      GameTutorialStep(
        emoji: '🌀',
        title: 'Spin It!',
        description: 'Place your finger on the spinner and swipe in a circular motion to spin.',
      ),
      GameTutorialStep(
        emoji: '💨',
        title: 'Build Momentum',
        description: 'The faster you swipe, the faster it spins! Release to watch it keep spinning.',
      ),
      GameTutorialStep(
        emoji: '🎯',
        title: 'Feel the Spin',
        description: "You'll feel gentle haptic feedback as it spins. Very satisfying!",
      ),
    ],
  ),
  'color_flow': const GameTutorial(
    gameId: 'color_flow',
    title: 'Color Flow',
    steps: [
      GameTutorialStep(
        emoji: '🎨',
        title: 'Watch the Colors',
        description: 'Relaxing color gradients flow and mix on your screen.',
      ),
      GameTutorialStep(
        emoji: '👆',
        title: 'Tap to Change',
        description: 'Tap anywhere to shuffle to a new color combination.',
      ),
      GameTutorialStep(
        emoji: '😌',
        title: 'Just Relax',
        description: 'No goals here - just enjoy the soothing visual experience.',
      ),
    ],
  ),
  'pattern_tap': const GameTutorial(
    gameId: 'pattern_tap',
    title: 'Pattern Tap',
    steps: [
      GameTutorialStep(
        emoji: '👀',
        title: 'Watch Carefully',
        description: 'A pattern will light up on the grid. Pay attention to the sequence!',
      ),
      GameTutorialStep(
        emoji: '👆',
        title: 'Repeat the Pattern',
        description: 'After the pattern shows, tap the tiles in the same order.',
      ),
      GameTutorialStep(
        emoji: '📈',
        title: 'Level Up',
        description: 'Each correct sequence adds one more tile. How far can you go?',
      ),
      GameTutorialStep(
        emoji: '💚',
        title: 'No Stress',
        description: "Made a mistake? Just tap 'Try Again' - practice makes progress!",
      ),
    ],
  ),
  'breathing_bubbles': const GameTutorial(
    gameId: 'breathing_bubbles',
    title: 'Breathing Bubbles',
    steps: [
      GameTutorialStep(
        emoji: '🫁',
        title: 'Guided Breathing',
        description: 'The bubble guides you through a calming breathing exercise.',
      ),
      GameTutorialStep(
        emoji: '⬆️',
        title: 'Breathe In',
        description: 'As the bubble grows, breathe in slowly through your nose.',
      ),
      GameTutorialStep(
        emoji: '⏸️',
        title: 'Hold',
        description: 'When it says "Hold", pause your breath gently.',
      ),
      GameTutorialStep(
        emoji: '⬇️',
        title: 'Breathe Out',
        description: 'As the bubble shrinks, exhale slowly through your mouth.',
      ),
      GameTutorialStep(
        emoji: '🔁',
        title: 'Repeat',
        description: 'Each cycle is counted. Great for anxiety relief!',
      ),
    ],
  ),
  'infinity_draw': const GameTutorial(
    gameId: 'infinity_draw',
    title: 'Infinity Draw',
    steps: [
      GameTutorialStep(
        emoji: '✏️',
        title: 'Draw Freely',
        description: 'Drag your finger across the screen to create beautiful lines.',
      ),
      GameTutorialStep(
        emoji: '🎨',
        title: 'Pick Colors',
        description: 'Tap the color circles at the top to change your drawing color.',
      ),
      GameTutorialStep(
        emoji: '♾️',
        title: 'Endless Canvas',
        description: 'Your drawings stay on screen. Create patterns, shapes, anything!',
      ),
      GameTutorialStep(
        emoji: '🔄',
        title: 'Start Fresh',
        description: 'Tap the refresh button to clear and start a new drawing.',
      ),
    ],
  ),
  'sensory_rain': const GameTutorial(
    gameId: 'sensory_rain',
    title: 'Sensory Rain',
    steps: [
      GameTutorialStep(
        emoji: '🌧️',
        title: 'Watch the Rain',
        description: 'Calming rain drops fall down the screen.',
      ),
      GameTutorialStep(
        emoji: '🎚️',
        title: 'Adjust Intensity',
        description: 'Use the slider to control how much rain falls.',
      ),
      GameTutorialStep(
        emoji: '💧',
        title: 'Create Ripples',
        description: 'Tap anywhere on the screen to create water ripples.',
      ),
      GameTutorialStep(
        emoji: '🎧',
        title: 'Best with Sound',
        description: 'For the full experience, try with headphones and rain sounds!',
      ),
    ],
  ),
  'emotion_garden': const GameTutorial(
    gameId: 'emotion_garden',
    title: 'Emotion Garden',
    steps: [
      GameTutorialStep(
        emoji: '😊',
        title: 'Pick an Emotion',
        description: 'Select how you\'re feeling from the emotion chips.',
      ),
      GameTutorialStep(
        emoji: '🌸',
        title: 'Plant Your Feeling',
        description: 'Tap in the garden to plant a flower representing that emotion.',
      ),
      GameTutorialStep(
        emoji: '🌱',
        title: 'Watch It Grow',
        description: 'Your emotion flowers grow and bloom over time.',
      ),
      GameTutorialStep(
        emoji: '🌈',
        title: 'Express Yourself',
        description: 'Create a garden that reflects your emotional journey!',
      ),
    ],
  ),
  'zen_sand': const GameTutorial(
    gameId: 'zen_sand',
    title: 'Zen Sand',
    steps: [
      GameTutorialStep(
        emoji: '🏖️',
        title: 'Draw in Sand',
        description: 'Drag your finger to create patterns in the virtual sand.',
      ),
      GameTutorialStep(
        emoji: '🪨',
        title: 'Add Stones',
        description: 'Place decorative stones and rocks in your garden.',
      ),
      GameTutorialStep(
        emoji: '🌊',
        title: 'Rake Waves',
        description: 'Create calming wave patterns like a traditional zen garden.',
      ),
      GameTutorialStep(
        emoji: '🧘',
        title: 'Find Peace',
        description: 'This is your space for mindful creation.',
      ),
    ],
  ),
};

/// Game tutorial dialog widget
class GameTutorialDialog extends StatefulWidget {
  final GameTutorial tutorial;
  final VoidCallback onComplete;

  const GameTutorialDialog({
    super.key,
    required this.tutorial,
    required this.onComplete,
  });

  @override
  State<GameTutorialDialog> createState() => _GameTutorialDialogState();
}

class _GameTutorialDialogState extends State<GameTutorialDialog> {
  int _currentStep = 0;

  void _nextStep() {
    if (_currentStep < widget.tutorial.steps.length - 1) {
      setState(() => _currentStep++);
    } else {
      GameTutorialManager.markTutorialSeen(widget.tutorial.gameId);
      widget.onComplete();
    }
  }

  void _previousStep() {
    if (_currentStep > 0) {
      setState(() => _currentStep--);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final step = widget.tutorial.steps[_currentStep];
    final isLast = _currentStep == widget.tutorial.steps.length - 1;

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Emoji
            Text(
              step.emoji,
              style: const TextStyle(fontSize: 64),
            ),
            const SizedBox(height: 16),

            // Title
            Text(
              step.title,
              style: theme.textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),

            // Description
            Text(
              step.description,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),

            // Progress dots
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(widget.tutorial.steps.length, (index) {
                return Container(
                  width: 8,
                  height: 8,
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: index == _currentStep
                        ? theme.colorScheme.primary
                        : theme.colorScheme.outline.withOpacity(0.3),
                  ),
                );
              }),
            ),
            const SizedBox(height: 24),

            // Buttons
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                if (_currentStep > 0)
                  TextButton(
                    onPressed: _previousStep,
                    child: const Text('Back'),
                  )
                else
                  TextButton(
                    onPressed: () {
                      GameTutorialManager.markTutorialSeen(widget.tutorial.gameId);
                      Navigator.pop(context);
                    },
                    child: const Text('Skip'),
                  ),
                FilledButton(
                  onPressed: _nextStep,
                  child: Text(isLast ? "Let's Play!" : 'Next'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Shows game tutorial if not seen before
Future<void> showGameTutorialIfNeeded(
  BuildContext context,
  String gameId,
) async {
  final hasSeen = await GameTutorialManager.hasSeenTutorial(gameId);
  if (hasSeen) return;

  final tutorial = GameTutorialManager.getTutorial(gameId);
  if (tutorial == null) return;

  if (context.mounted) {
    await showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => GameTutorialDialog(
        tutorial: tutorial,
        onComplete: () => Navigator.pop(context),
      ),
    );
  }
}


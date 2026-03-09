import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Tutorial step definitions for first-time users
enum AppTutorialStep {
  welcome(
    'Welcome to NeuroComet!',
    'A safe space designed with neurodivergent minds in mind. Let\'s take a gentle tour to help you feel at home.',
    Icons.celebration,
    '☄️',
    Color(0xFF9B59B6),
  ),
  feed(
    'Your Home Feed',
    'This is where you\'ll see posts from people you follow. It\'s a calm, supportive space with no algorithmic pressure.',
    Icons.home,
    '🏠',
    Color(0xFF4ECDC4),
  ),
  explore(
    'Explore Communities',
    'Discover topics and communities that match your interests. Find your people!',
    Icons.explore,
    '🔍',
    Color(0xFF3498DB),
  ),
  messages(
    'Messages & Practice Calls',
    'Chat with friends and practice phone calls with AI personas. Great for building communication confidence!',
    Icons.mail,
    '💬',
    Color(0xFFF39C12),
  ),
  games(
    'Calming Games',
    'Stim-friendly games designed for relaxation and sensory needs. No competition, just fun!',
    Icons.games,
    '🎮',
    Color(0xFF27AE60),
  ),
  notifications(
    'Stay Updated',
    'Check notifications for likes, comments, and new followers. You control what notifications you receive.',
    Icons.notifications,
    '🔔',
    Color(0xFFE74C3C),
  ),
  settings(
    'Personalize Your Experience',
    'Customize themes, fonts, animations, and accessibility options to make the app comfortable for you.',
    Icons.settings,
    '⚙️',
    Color(0xFF8E44AD),
  ),
  complete(
    'You\'re All Set!',
    'You can always access help from Settings. Enjoy connecting with your community!',
    Icons.check_circle,
    '✨',
    Color(0xFF9B59B6),
  );

  final String title;
  final String description;
  final IconData icon;
  final String emoji;
  final Color accentColor;

  const AppTutorialStep(
    this.title,
    this.description,
    this.icon,
    this.emoji,
    this.accentColor,
  );
}

/// Tutorial state manager
class TutorialManager {
  static const String _keyCompleted = 'tutorial_completed';
  static const String _keySkipped = 'tutorial_skipped';

  static Future<bool> hasCompletedTutorial() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyCompleted) ?? false;
  }

  static Future<bool> hasSkippedTutorial() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keySkipped) ?? false;
  }

  static Future<bool> shouldShowTutorial() async {
    final completed = await hasCompletedTutorial();
    final skipped = await hasSkippedTutorial();
    return !completed && !skipped;
  }

  static Future<void> markCompleted() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyCompleted, true);
  }

  static Future<void> markSkipped() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keySkipped, true);
  }

  static Future<void> resetTutorial() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyCompleted);
    await prefs.remove(_keySkipped);
  }
}

/// Full-screen tutorial overlay widget
class AppTutorialOverlay extends StatefulWidget {
  final VoidCallback onComplete;
  final VoidCallback? onSkip;

  const AppTutorialOverlay({
    super.key,
    required this.onComplete,
    this.onSkip,
  });

  @override
  State<AppTutorialOverlay> createState() => _AppTutorialOverlayState();
}

class _AppTutorialOverlayState extends State<AppTutorialOverlay>
    with SingleTickerProviderStateMixin {
  int _currentStepIndex = 0;
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;

  AppTutorialStep get _currentStep => AppTutorialStep.values[_currentStepIndex];
  bool get _isFirstStep => _currentStepIndex == 0;
  bool get _isLastStep => _currentStepIndex == AppTutorialStep.values.length - 1;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );
    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeOut),
    );
    _scaleAnimation = Tween<double>(begin: 0.8, end: 1).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeOutBack),
    );
    _animationController.forward();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _goToStep(int index) async {
    await _animationController.reverse();
    setState(() => _currentStepIndex = index);
    _animationController.forward();
  }

  void _nextStep() {
    if (_isLastStep) {
      TutorialManager.markCompleted();
      widget.onComplete();
    } else {
      _goToStep(_currentStepIndex + 1);
    }
  }

  void _previousStep() {
    if (!_isFirstStep) {
      _goToStep(_currentStepIndex - 1);
    }
  }

  void _skip() {
    TutorialManager.markSkipped();
    if (widget.onSkip != null) {
      widget.onSkip!();
    } else {
      widget.onComplete();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Material(
      color: Colors.black.withValues(alpha: 0.85),
      child: SafeArea(
        child: AnimatedBuilder(
          animation: _animationController,
          builder: (context, child) {
            return FadeTransition(
              opacity: _fadeAnimation,
              child: ScaleTransition(
                scale: _scaleAnimation,
                child: child,
              ),
            );
          },
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                // Skip button
                if (!_isLastStep)
                  Align(
                    alignment: Alignment.topRight,
                    child: TextButton(
                      onPressed: _skip,
                      child: const Text(
                        'Skip Tour',
                        style: TextStyle(color: Colors.white70),
                      ),
                    ),
                  ),

                const Spacer(),

                // Emoji
                Container(
                  width: 120,
                  height: 120,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: _currentStep.accentColor.withValues(alpha: 0.2),
                    border: Border.all(
                      color: _currentStep.accentColor,
                      width: 3,
                    ),
                  ),
                  child: Center(
                    child: Text(
                      _currentStep.emoji,
                      style: const TextStyle(fontSize: 56),
                    ),
                  ),
                ),
                const SizedBox(height: 32),

                // Title
                Text(
                  _currentStep.title,
                  style: theme.textTheme.headlineSmall?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),

                // Description
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: Text(
                    _currentStep.description,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: Colors.white70,
                      height: 1.5,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),

                const Spacer(),

                // Progress dots
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: List.generate(AppTutorialStep.values.length, (index) {
                    final isActive = index == _currentStepIndex;
                    return AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      width: isActive ? 24 : 8,
                      height: 8,
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      decoration: BoxDecoration(
                        color: isActive
                            ? _currentStep.accentColor
                            : Colors.white30,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    );
                  }),
                ),
                const SizedBox(height: 32),

                // Navigation buttons
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    // Back button
                    _isFirstStep
                        ? const SizedBox(width: 100)
                        : TextButton.icon(
                            onPressed: _previousStep,
                            icon: const Icon(Icons.arrow_back, color: Colors.white70),
                            label: const Text(
                              'Back',
                              style: TextStyle(color: Colors.white70),
                            ),
                          ),

                    // Next/Complete button
                    FilledButton.icon(
                      onPressed: _nextStep,
                      style: FilledButton.styleFrom(
                        backgroundColor: _currentStep.accentColor,
                        padding: const EdgeInsets.symmetric(
                          horizontal: 24,
                          vertical: 12,
                        ),
                      ),
                      icon: Icon(_isLastStep ? Icons.check : Icons.arrow_forward),
                      label: Text(_isLastStep ? 'Get Started!' : 'Next'),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Shows tutorial if needed
Future<void> showAppTutorialIfNeeded(BuildContext context) async {
  final shouldShow = await TutorialManager.shouldShowTutorial();
  if (!shouldShow) return;

  if (context.mounted) {
    await Navigator.of(context).push(
      PageRouteBuilder(
        opaque: false,
        pageBuilder: (context, animation, secondaryAnimation) {
          return AppTutorialOverlay(
            onComplete: () => Navigator.of(context).pop(),
            onSkip: () => Navigator.of(context).pop(),
          );
        },
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          return FadeTransition(opacity: animation, child: child);
        },
      ),
    );
  }
}


import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../services/tutorial_service.dart';

/// Tutorial/Onboarding screen matching Android version
/// Teaches users how to use NeuroComet's features
class TutorialScreen extends StatefulWidget {
  const TutorialScreen({super.key});

  @override
  State<TutorialScreen> createState() => _TutorialScreenState();
}

class _TutorialScreenState extends State<TutorialScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  final List<_TutorialStep> _steps = const [
    _TutorialStep(
      icon: Icons.favorite_outline,
      emoji: '🧠',
      title: 'Welcome to NeuroComet',
      description: 'A safe, supportive space designed for neurodivergent minds. Let\'s show you around!',
      color: Color(0xFF6366F1),
    ),
    _TutorialStep(
      icon: Icons.explore_outlined,
      emoji: '🔍',
      title: 'Explore Communities',
      description: 'Find communities that match your interests. From special interests to support groups, there\'s a place for everyone.',
      color: Color(0xFF10B981),
    ),
    _TutorialStep(
      icon: Icons.sports_esports,
      emoji: '🎮',
      title: 'Stim-Friendly Games',
      description: 'Take a break with games designed for neurodivergent minds. Calm, satisfying, and no pressure!',
      color: Color(0xFFF59E0B),
    ),
    _TutorialStep(
      icon: Icons.palette_outlined,
      emoji: '🎨',
      title: 'Customize Your Experience',
      description: 'Adjust colors, fonts, animations, and more. Make NeuroComet work the way YOUR brain works.',
      color: Color(0xFFEC4899),
    ),
    _TutorialStep(
      icon: Icons.shield_outlined,
      emoji: '🛡️',
      title: 'Stay Safe',
      description: 'Parental controls, content filters, and moderation tools keep you protected.',
      color: Color(0xFF8B5CF6),
    ),
    _TutorialStep(
      icon: Icons.accessibility_new,
      emoji: '✨',
      title: 'You\'re Ready!',
      description: 'That\'s the basics! Remember, you can always access settings to customize your experience.',
      color: Color(0xFF06B6D4),
    ),
  ];

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  void _nextPage() {
    if (_currentPage < _steps.length - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      _completeTutorial();
    }
  }

  void _previousPage() {
    if (_currentPage > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  void _completeTutorial() {
    TutorialManager.markCompleted();
    if (context.mounted) {
      context.pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('🎉 Tutorial complete! Welcome to NeuroComet!'),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Tutorial'),
        actions: [
          TextButton(
            onPressed: _completeTutorial,
            child: const Text('Skip'),
          ),
        ],
      ),
      body: Column(
        children: [
          // Page content
          Expanded(
            child: PageView.builder(
              controller: _pageController,
              onPageChanged: (index) {
                setState(() => _currentPage = index);
              },
              itemCount: _steps.length,
              itemBuilder: (context, index) {
                final step = _steps[index];
                return _TutorialPageContent(step: step);
              },
            ),
          ),

          // Progress indicators
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: List.generate(
                _steps.length,
                (index) => AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  width: _currentPage == index ? 24 : 8,
                  height: 8,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(4),
                    color: _currentPage == index
                        ? _steps[_currentPage].color
                        : theme.colorScheme.outlineVariant,
                  ),
                ),
              ),
            ),
          ),

          // Navigation buttons
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  if (_currentPage > 0)
                    Expanded(
                      child: OutlinedButton(
                        onPressed: _previousPage,
                        child: const Text('Back'),
                      ),
                    )
                  else
                    const Spacer(),
                  const SizedBox(width: 16),
                  Expanded(
                    flex: 2,
                    child: FilledButton(
                      onPressed: _nextPage,
                      style: FilledButton.styleFrom(
                        backgroundColor: _steps[_currentPage].color,
                      ),
                      child: Text(
                        _currentPage == _steps.length - 1 ? 'Get Started' : 'Next',
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _TutorialStep {
  final IconData icon;
  final String emoji;
  final String title;
  final String description;
  final Color color;

  const _TutorialStep({
    required this.icon,
    required this.emoji,
    required this.title,
    required this.description,
    required this.color,
  });
}

class _TutorialPageContent extends StatelessWidget {
  final _TutorialStep step;

  const _TutorialPageContent({required this.step});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Large emoji
          Text(
            step.emoji,
            style: const TextStyle(fontSize: 80),
          ),
          const SizedBox(height: 16),
          // Icon in colored circle
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: step.color.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: Icon(
              step.icon,
              size: 40,
              color: step.color,
            ),
          ),
          const SizedBox(height: 32),
          // Title
          Text(
            step.title,
            style: theme.textTheme.headlineMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          // Description
          Text(
            step.description,
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}



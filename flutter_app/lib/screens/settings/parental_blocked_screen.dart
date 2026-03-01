import 'package:flutter/material.dart';
import '../../core/theme/app_colors.dart';

/// Types of parental control restrictions.
/// Mirrors the Kotlin ParentalBlockedScreen.kt
enum RestrictionType {
  featureBlocked,
  bedtimeActive,
  timeLimitReached,
  contentFiltered,
}

/// A full-screen widget that displays when content or features are blocked
/// by parental controls.
class ParentalBlockedScreen extends StatelessWidget {
  final RestrictionType restrictionType;
  final String featureName;

  const ParentalBlockedScreen({
    super.key,
    required this.restrictionType,
    this.featureName = 'This feature',
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final (icon, title, message) = _getContent();

    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: 80,
                color: theme.colorScheme.primary.withOpacity(0.7),
              ),
              const SizedBox(height: 24),
              Text(
                title,
                style: theme.textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                message,
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 40),
              // Decorative element
              Container(
                width: 120,
                height: 4,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      AppColors.primaryPurple.withAlpha(50),
                      AppColors.primaryPurple,
                      AppColors.primaryPurple.withAlpha(50),
                    ],
                  ),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(height: 24),
              // Tip for kids
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: theme.colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.lightbulb_outline,
                      color: AppColors.accentOrange,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        _getTip(),
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  (IconData, String, String) _getContent() {
    switch (restrictionType) {
      case RestrictionType.featureBlocked:
        return (
          Icons.lock,
          '$featureName is Restricted',
          'Parental controls have disabled access to this feature. Ask a parent or guardian to unlock it.',
        );
      case RestrictionType.bedtimeActive:
        return (
          Icons.nights_stay,
          'Bedtime Mode Active',
          'It\'s time to rest! The app is currently blocked during bedtime hours. Come back tomorrow!',
        );
      case RestrictionType.timeLimitReached:
        return (
          Icons.timer,
          'Daily Limit Reached',
          'You\'ve used all your screen time for today. Take a break and come back tomorrow!',
        );
      case RestrictionType.contentFiltered:
        return (
          Icons.block,
          'Content Not Available',
          'This content has been filtered by parental controls.',
        );
    }
  }

  String _getTip() {
    switch (restrictionType) {
      case RestrictionType.featureBlocked:
        return 'Tip: Your parent or guardian can change these settings in the Parental Controls section.';
      case RestrictionType.bedtimeActive:
        return 'Tip: A good night\'s sleep helps your brain work at its best! 🌙';
      case RestrictionType.timeLimitReached:
        return 'Tip: Try reading a book, going outside, or doing a creative activity! 🎨';
      case RestrictionType.contentFiltered:
        return 'Tip: There\'s lots of other great content to explore on NeuroComet!';
    }
  }
}


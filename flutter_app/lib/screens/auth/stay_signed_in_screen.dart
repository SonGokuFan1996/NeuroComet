import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/theme/app_colors.dart';

/// Manages the "Stay Signed In" preference persistence.
/// Mirrors the Kotlin StaySignedInScreen.kt
class StaySignedInSettings {
  static const _keyStaySignedIn = 'stay_signed_in';
  static const _keyDontShowAgain = 'stay_signed_in_dont_show';
  static const _keyShownOnce = 'stay_signed_in_shown';

  /// Check if the Stay Signed In prompt should be displayed
  static Future<bool> shouldShowPrompt() async {
    final prefs = await SharedPreferences.getInstance();
    return !(prefs.getBool(_keyDontShowAgain) ?? false) &&
        !(prefs.getBool(_keyShownOnce) ?? false);
  }

  /// Check if the user has previously chosen to stay signed in
  static Future<bool> isStaySignedIn() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyStaySignedIn) ?? false;
  }

  /// Save the user's preference
  static Future<void> savePreference(bool staySignedIn, bool dontShowAgain) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyStaySignedIn, staySignedIn);
    await prefs.setBool(_keyDontShowAgain, dontShowAgain);
    await prefs.setBool(_keyShownOnce, true);
  }

  /// Mark that the prompt has been shown
  static Future<void> markAsShown() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyShownOnce, true);
  }

  /// Reset the shown flag for testing
  static Future<void> resetShownFlag() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyShownOnce, false);
    await prefs.setBool(_keyDontShowAgain, false);
  }

  /// Clear all stay signed in preferences (used on sign out)
  static Future<void> clearAll() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyStaySignedIn);
    await prefs.remove(_keyDontShowAgain);
    await prefs.remove(_keyShownOnce);
  }
}

/// Microsoft-style "Stay Signed In" prompt screen.
/// Displayed after successful authentication to ask users if they want
/// to remain signed in on this device.
class StaySignedInScreen extends StatefulWidget {
  final String? displayName;
  final String? email;
  final VoidCallback onYes;
  final VoidCallback onNo;

  const StaySignedInScreen({
    super.key,
    this.displayName,
    this.email,
    required this.onYes,
    required this.onNo,
  });

  @override
  State<StaySignedInScreen> createState() => _StaySignedInScreenState();
}

class _StaySignedInScreenState extends State<StaySignedInScreen> {
  bool _dontShowAgain = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(32),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Security icon
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    color: AppColors.primaryPurple.withAlpha(30),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.security,
                    size: 40,
                    color: AppColors.primaryPurple,
                  ),
                ),
                const SizedBox(height: 32),

                // Title
                Text(
                  'Stay signed in?',
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),

                // User info
                if (widget.displayName != null || widget.email != null) ...[
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.surfaceContainerHighest,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        CircleAvatar(
                          radius: 20,
                          backgroundColor: AppColors.primaryPurple.withAlpha(50),
                          child: const Icon(Icons.person, color: AppColors.primaryPurple),
                        ),
                        const SizedBox(width: 12),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (widget.displayName != null)
                              Text(
                                widget.displayName!,
                                style: theme.textTheme.titleSmall?.copyWith(
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            if (widget.email != null)
                              Text(
                                widget.email!,
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                ],

                // Description
                Text(
                  'Do you want to stay signed in on this device? '
                  'You won\'t be asked to sign in again until you sign out.',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),

                // Don't show again checkbox
                GestureDetector(
                  onTap: () => setState(() => _dontShowAgain = !_dontShowAgain),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Checkbox(
                        value: _dontShowAgain,
                        onChanged: (v) => setState(() => _dontShowAgain = v ?? false),
                      ),
                      Text(
                        'Don\'t show this again',
                        style: theme.textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 32),

                // Buttons
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: () async {
                      await StaySignedInSettings.savePreference(true, _dontShowAgain);
                      widget.onYes();
                    },
                    child: const Text('Yes'),
                  ),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton(
                    onPressed: () async {
                      await StaySignedInSettings.savePreference(false, _dontShowAgain);
                      widget.onNo();
                    },
                    child: const Text('No'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}


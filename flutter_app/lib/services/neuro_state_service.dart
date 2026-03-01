import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../l10n/app_localizations.dart';

/// Neurodivergent-friendly theming states that adapt the UI based on:
/// - Condition/Disorder type
/// - Current mood/energy level
/// - Sensory needs
enum NeuroState {
  // Basic states
  defaultState('Default', 'Standard balanced theme', '🎨', NeuroStateCategory.basic),
  hyperfocus('Hyperfocus', 'Minimal distractions, clear focus', '🎯', NeuroStateCategory.basic),
  overload('Overload', 'Reduced visual noise', '🌊', NeuroStateCategory.basic),
  calm('Calm', 'Soothing and peaceful', '🌿', NeuroStateCategory.basic),

  // ADHD-focused states
  adhdEnergized('Energized', 'Vibrant colors for active moments', '⚡', NeuroStateCategory.adhd),
  adhdLowDopamine('Low Dopamine', 'Extra stimulating elements', '🌅', NeuroStateCategory.adhd),
  adhdTaskMode('Task Mode', 'Clear structure and focus aids', '📋', NeuroStateCategory.adhd),

  // Autism-focused states
  autismRoutine('Routine', 'Predictable and structured', '🔄', NeuroStateCategory.autism),
  autismSensorySeek('Sensory Seek', 'More visual stimulation', '✨', NeuroStateCategory.autism),
  autismLowStim('Low Stim', 'Reduced sensory input', '🤫', NeuroStateCategory.autism),

  // Anxiety/OCD-focused states
  anxietySoothe('Soothe', 'Calming colors and gentle transitions', '💙', NeuroStateCategory.anxiety),
  anxietyGrounding('Grounding', 'Stable, earthy tones', '🌍', NeuroStateCategory.anxiety),

  // Accessibility states
  dyslexiaFriendly('Dyslexia Friendly', 'Optimized fonts and spacing', '📖', NeuroStateCategory.accessibility),

  // Colorblind-friendly states
  colorblindDeuteranopia('Deuteranopia', 'Red-green colorblind friendly', '👁️', NeuroStateCategory.colorblind),
  colorblindProtanopia('Protanopia', 'Red-weak colorblind friendly', '👁️', NeuroStateCategory.colorblind),
  colorblindTritanopia('Tritanopia', 'Blue-yellow colorblind friendly', '👁️', NeuroStateCategory.colorblind),
  colorblindMonochromacy('Monochromacy', 'Full colorblind friendly', '⚫', NeuroStateCategory.colorblind),

  // Blind/Screen Reader accessibility states
  blindScreenReader('Screen Reader', 'Optimized for screen readers', '🔊', NeuroStateCategory.blind),
  blindHighContrast('High Contrast', 'Maximum contrast for visibility', '◐', NeuroStateCategory.blind),
  blindLargeText('Large Text', 'Larger text throughout the app', '🔤', NeuroStateCategory.blind),

  // Energy/Mood-based states
  moodTired('Tired', 'Low energy, gentle colors', '😴', NeuroStateCategory.mood),
  moodAnxious('Anxious', 'Calming and supportive', '🫂', NeuroStateCategory.mood),
  moodHappy('Happy', 'Bright and cheerful', '😊', NeuroStateCategory.mood),
  moodOverwhelmed('Overwhelmed', 'Simple and minimal', '🧘', NeuroStateCategory.mood),
  moodCreative('Creative', 'Inspiring and colorful', '🎨', NeuroStateCategory.mood),

  // Secret unlockable theme
  rainbowBrain('Rainbow Brain', 'Celebrate neurodiversity!', '🦄', NeuroStateCategory.secret);

  final String displayName;
  final String description;
  final String emoji;
  final NeuroStateCategory category;

  const NeuroState(this.displayName, this.description, this.emoji, this.category);
}

/// Categorized groups for easier UI navigation
enum NeuroStateCategory {
  basic('Basic', false),
  adhd('ADHD', false),
  autism('Autism', false),
  anxiety('Anxiety & OCD', false),
  accessibility('Accessibility', false),
  colorblind('Colorblind', false),
  blind('Visual Impairment', false),
  mood('Mood', false),
  secret('Secret', true);

  final String displayName;
  final bool isSecret;

  const NeuroStateCategory(this.displayName, this.isSecret);

  List<NeuroState> get states =>
      NeuroState.values.where((s) => s.category == this).toList();
}

/// Manager for NeuroState persistence
class NeuroStateManager {
  static const String _prefsKey = 'neuro_state';
  static const String _secretUnlockedKey = 'rainbow_brain_unlocked';

  static Future<NeuroState> getCurrentState() async {
    final prefs = await SharedPreferences.getInstance();
    final stateId = prefs.getString(_prefsKey);
    if (stateId == null) return NeuroState.defaultState;

    try {
      return NeuroState.values.firstWhere((s) => s.name == stateId);
    } catch (_) {
      return NeuroState.defaultState;
    }
  }

  static Future<void> setCurrentState(NeuroState state) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefsKey, state.name);
  }

  static Future<bool> isSecretUnlocked() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_secretUnlockedKey) ?? false;
  }

  static Future<void> unlockSecret() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_secretUnlockedKey, true);
  }
}

/// NeuroState picker widget
class NeuroStatePicker extends StatefulWidget {
  final NeuroState currentState;
  final ValueChanged<NeuroState> onStateChanged;

  const NeuroStatePicker({
    super.key,
    required this.currentState,
    required this.onStateChanged,
  });

  @override
  State<NeuroStatePicker> createState() => _NeuroStatePickerState();
}

class _NeuroStatePickerState extends State<NeuroStatePicker> {
  bool _secretUnlocked = false;
  NeuroStateCategory? _expandedCategory;

  @override
  void initState() {
    super.initState();
    _checkSecret();
  }

  void _checkSecret() async {
    final unlocked = await NeuroStateManager.isSecretUnlocked();
    setState(() => _secretUnlocked = unlocked);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final categories = NeuroStateCategory.values.where((c) {
      if (c.isSecret && !_secretUnlocked) return false;
      return true;
    }).toList();

    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: categories.length,
      itemBuilder: (context, index) {
        final category = categories[index];
        final isExpanded = _expandedCategory == category;

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Category header
            InkWell(
              onTap: () {
                setState(() {
                  _expandedCategory = isExpanded ? null : category;
                });
              },
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: Row(
                  children: [
                    Icon(
                      isExpanded ? Icons.expand_less : Icons.expand_more,
                      color: theme.colorScheme.primary,
                    ),
                    const SizedBox(width: 12),
                    Text(
                      category.displayName,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    if (category.isSecret) ...[
                      const SizedBox(width: 8),
                      const Text('✨', style: TextStyle(fontSize: 16)),
                    ],
                  ],
                ),
              ),
            ),

            // States list (when expanded)
            if (isExpanded)
              Padding(
                padding: const EdgeInsets.only(left: 16, right: 16, bottom: 8),
                child: Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: category.states.map((state) {
                    final isSelected = state == widget.currentState;
                    return ChoiceChip(
                      label: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(state.emoji),
                          const SizedBox(width: 6),
                          Text(state.displayName),
                        ],
                      ),
                      selected: isSelected,
                      onSelected: (_) => widget.onStateChanged(state),
                    );
                  }).toList(),
                ),
              ),
          ],
        );
      },
    );
  }
}

/// NeuroState quick picker bottom sheet
class NeuroStateQuickPicker extends StatelessWidget {
  final NeuroState currentState;
  final ValueChanged<NeuroState> onStateChanged;

  const NeuroStateQuickPicker({
    super.key,
    required this.currentState,
    required this.onStateChanged,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // Show only mood states for quick access
    final moodStates = NeuroStateCategory.mood.states;

    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Handle
          Center(
            child: Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: theme.colorScheme.outline.withOpacity(0.3),
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          const SizedBox(height: 16),

          Text(
            AppLocalizations.of(context)?.get('howAreYouFeeling') ?? 'How are you feeling?',
            style: theme.textTheme.titleLarge?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            AppLocalizations.of(context)?.get('selectMoodHint') ?? "Select your current mood to personalize your experience",
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 16),

          // Mood grid
          GridView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              childAspectRatio: 1,
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
            ),
            itemCount: moodStates.length,
            itemBuilder: (context, index) {
              final state = moodStates[index];
              final isSelected = state == currentState;

              return InkWell(
                onTap: () {
                  onStateChanged(state);
                  Navigator.pop(context);
                },
                borderRadius: BorderRadius.circular(16),
                child: Container(
                  decoration: BoxDecoration(
                    color: isSelected
                        ? theme.colorScheme.primaryContainer
                        : theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(16),
                    border: isSelected
                        ? Border.all(
                            color: theme.colorScheme.primary,
                            width: 2,
                          )
                        : null,
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        state.emoji,
                        style: const TextStyle(fontSize: 32),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        state.displayName,
                        style: theme.textTheme.labelSmall?.copyWith(
                          fontWeight:
                              isSelected ? FontWeight.bold : FontWeight.normal,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 16),

          // Link to full picker
          Center(
            child: TextButton.icon(
              onPressed: () {
                Navigator.pop(context);
                context.push('/settings/neuro-state');
              },
              icon: const Icon(Icons.tune),
              label: Text(AppLocalizations.of(context)?.get('moreOptions') ?? 'More options'),
            ),
          ),
        ],
      ),
    );
  }
}

/// Shows the quick picker bottom sheet
void showNeuroStateQuickPicker(
  BuildContext context, {
  required NeuroState currentState,
  required ValueChanged<NeuroState> onStateChanged,
}) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (context) => NeuroStateQuickPicker(
      currentState: currentState,
      onStateChanged: onStateChanged,
    ),
  );
}
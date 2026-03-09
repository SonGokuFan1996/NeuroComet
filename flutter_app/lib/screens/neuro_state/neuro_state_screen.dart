import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';
import '../../core/theme/neuro_state.dart';
import '../../providers/theme_provider.dart';
import '../../l10n/app_localizations.dart';

/// NeuroState Screen - Track and manage emotional/sensory state
class NeuroStateScreen extends ConsumerStatefulWidget {
  const NeuroStateScreen({super.key});

  @override
  ConsumerState<NeuroStateScreen> createState() => _NeuroStateScreenState();
}

class _NeuroStateScreenState extends ConsumerState<NeuroStateScreen> {
  int _energyLevel = 50;
  int _stressLevel = 30;
  int _sensoryLoad = 40;

  @override
  Widget build(BuildContext context) {
    final l10n = context.l10n;
    final currentNeuroState = ref.watch(neuroStateProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.neuroState),
        actions: [
          IconButton(
            icon: const Icon(Icons.history),
            onPressed: () => _showMoodHistory(context),
            tooltip: l10n.moodHistory,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Current theme preview card
            _buildThemePreviewCard(context, currentNeuroState),
            const SizedBox(height: 24),

            // Neuro State Selector
            _buildSectionHeader(context, l10n.get('chooseYourState')),
            _buildNeuroStateGrid(context),
            const SizedBox(height: 24),

            // Energy level
            _buildSectionHeader(context, l10n.energyLevel),
            _buildSliderCard(
              context,
              value: _energyLevel,
              lowLabel: l10n.low,
              highLabel: l10n.high,
              color: AppColors.accentOrange,
              icon: Icons.bolt,
              onChanged: (value) {
                HapticFeedback.selectionClick();
                setState(() => _energyLevel = value);
              },
            ),
            const SizedBox(height: 16),

            // Stress level
            _buildSectionHeader(context, l10n.stressLevel),
            _buildSliderCard(
              context,
              value: _stressLevel,
              lowLabel: l10n.calm,
              highLabel: l10n.stressed,
              color: AppColors.categoryAnxiety,
              icon: Icons.psychology,
              onChanged: (value) {
                HapticFeedback.selectionClick();
                setState(() => _stressLevel = value);
              },
            ),
            const SizedBox(height: 16),

            // Sensory load
            _buildSectionHeader(context, l10n.sensoryLoad),
            _buildSliderCard(
              context,
              value: _sensoryLoad,
              lowLabel: l10n.understimulated,
              highLabel: l10n.overstimulated,
              color: AppColors.secondaryTeal,
              icon: Icons.hearing,
              onChanged: (value) {
                HapticFeedback.selectionClick();
                setState(() => _sensoryLoad = value);
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildThemePreviewCard(BuildContext context, NeuroState state) {
    final l10n = context.l10n;
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: colorScheme.primaryContainer.withValues(alpha: 0.3),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: colorScheme.primary.withValues(alpha: 0.5),
          width: 2,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: colorScheme.primary.withValues(alpha: 0.2),
                  shape: BoxShape.circle,
                ),
                child: Text(
                  state.emoji,
                  style: const TextStyle(fontSize: 32),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.get('activeTheme'),
                      style: theme.textTheme.labelMedium?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),
                    Text(
                      state.name,
                      style: theme.textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: colorScheme.primary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          // Color swatches row
          Row(
            children: [
              _buildColorSwatch(l10n.primary, colorScheme.primary),
              const SizedBox(width: 8),
              _buildColorSwatch(l10n.get('secondary'), colorScheme.secondary),
              const SizedBox(width: 8),
              _buildColorSwatch(l10n.get('tertiary'), colorScheme.tertiary),
              const SizedBox(width: 8),
              _buildColorSwatch(l10n.get('surface'), colorScheme.surface),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            l10n.get('themePreviewDescription'),
            style: theme.textTheme.bodySmall?.copyWith(
              color: colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildColorSwatch(String label, Color color) {
    return Expanded(
      child: Column(
        children: [
          Container(
            height: 32,
            decoration: BoxDecoration(
              color: color,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: Colors.black12,
                width: 1,
              ),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: const TextStyle(fontSize: 9),
            textAlign: TextAlign.center,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  Widget _buildNeuroStateGrid(BuildContext context) {
    final l10n = context.l10n;
    // Group states by category - matching the Android version's comprehensive themes
    final categories = {
      '✨ ${l10n.get('neuroCategoryBasic')}': [
        NeuroState.defaultState,
        NeuroState.hyperfocus,
        NeuroState.overload,
        NeuroState.calm,
      ],
      '⚡ ${l10n.get('neuroCategoryAdhd')}': [
        NeuroState.adhdEnergized,
        NeuroState.adhdLowDopamine,
        NeuroState.adhdTaskMode,
      ],
      '🧩 ${l10n.get('neuroCategoryAutism')}': [
        NeuroState.autismRoutine,
        NeuroState.autismSensorySeek,
        NeuroState.autismLowStim,
      ],
      '💭 ${l10n.get('neuroCategoryAnxiety')}': [
        NeuroState.anxietySoothe,
        NeuroState.anxietyGrounding,
      ],
      '😊 ${l10n.get('neuroCategoryMood')}': [
        NeuroState.moodHappy,
        NeuroState.moodAnxious,
        NeuroState.moodTired,
        NeuroState.moodOverwhelmed,
        NeuroState.moodCreative,
      ],
      '👁️ ${l10n.get('neuroCategoryAccessibility')}': [
        NeuroState.dyslexiaFriendly,
        NeuroState.colorblindDeuter,
        NeuroState.colorblindProtan,
        NeuroState.colorblindTritan,
        NeuroState.colorblindMono,
      ],
      '🔊 ${l10n.get('neuroCategoryBlind')}': [
        NeuroState.blindScreenReader,
        NeuroState.blindHighContrast,
        NeuroState.blindLargeText,
      ],
      '🌟 ${l10n.get('neuroCategorySpecial')}': [
        NeuroState.cinnamonBun,
        NeuroState.rainbowBrain,
      ],
    };

    return Column(
      children: categories.entries.map((entry) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(
                entry.key,
                style: Theme.of(context).textTheme.labelLarge?.copyWith(
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
            ),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: entry.value.map((state) => _NeuroStateChip(state: state)).toList(),
            ),
            const SizedBox(height: 16),
          ],
        );
      }).toList(),
    );
  }

  Widget _buildSliderCard(
    BuildContext context, {
    required int value,
    required String lowLabel,
    required String highLabel,
    required Color color,
    required IconData icon,
    required ValueChanged<int> onChanged,
  }) {
    final theme = Theme.of(context);

    return Card(
      elevation: 0,
      color: theme.colorScheme.surfaceContainer,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                Icon(icon, color: color),
                const SizedBox(width: 8),
                Expanded(
                  child: SliderTheme(
                    data: SliderThemeData(
                      activeTrackColor: color,
                      thumbColor: color,
                      inactiveTrackColor: color.withValues(alpha: 0.2),
                      trackHeight: 4,
                    ),
                    child: Slider(
                      value: value.toDouble(),
                      max: 100,
                      divisions: 10,
                      onChanged: (v) => onChanged(v.round()),
                    ),
                  ),
                ),
                SizedBox(
                  width: 40,
                  child: Text(
                    '$value%',
                    style: theme.textTheme.bodySmall?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: color,
                    ),
                    textAlign: TextAlign.right,
                  ),
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  lowLabel,
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                Text(
                  highLabel,
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showMoodHistory(BuildContext context) {
    final l10n = context.l10n;
    final theme = Theme.of(context);
    final now = DateTime.now();

    // Generate sample mood history entries (in a real app, these would be loaded from storage)
    final history = [
      _MoodEntry(state: NeuroState.calm, energy: 60, stress: 25, sensory: 30, timestamp: now.subtract(const Duration(hours: 1))),
      _MoodEntry(state: NeuroState.hyperfocus, energy: 85, stress: 40, sensory: 50, timestamp: now.subtract(const Duration(hours: 4))),
      _MoodEntry(state: NeuroState.adhdEnergized, energy: 90, stress: 35, sensory: 55, timestamp: now.subtract(const Duration(hours: 8))),
      _MoodEntry(state: NeuroState.moodTired, energy: 25, stress: 60, sensory: 70, timestamp: now.subtract(const Duration(days: 1))),
      _MoodEntry(state: NeuroState.overload, energy: 30, stress: 85, sensory: 90, timestamp: now.subtract(const Duration(days: 1, hours: 6))),
      _MoodEntry(state: NeuroState.calm, energy: 50, stress: 20, sensory: 25, timestamp: now.subtract(const Duration(days: 2))),
      _MoodEntry(state: NeuroState.moodHappy, energy: 75, stress: 15, sensory: 35, timestamp: now.subtract(const Duration(days: 2, hours: 12))),
    ];

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.7,
        maxChildSize: 0.9,
        minChildSize: 0.4,
        builder: (context, scrollController) => Column(
          children: [
            // Handle bar
            Padding(
              padding: const EdgeInsets.only(top: 12, bottom: 8),
              child: Container(
                width: 40, height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Row(
                children: [
                  Icon(Icons.history, color: theme.colorScheme.primary),
                  const SizedBox(width: 12),
                  Text(l10n.moodHistory, style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
                  const Spacer(),
                  Text(
                    l10n.get('entriesCount').replaceAll('{count}', history.length.toString()),
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            const Divider(),
            Expanded(
              child: ListView.separated(
                controller: scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: history.length,
                separatorBuilder: (_, __) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final entry = history[index];
                  final timeAgo = _formatTimeAgo(l10n, now.difference(entry.timestamp));
                  return Card(
                    elevation: 0,
                    color: theme.colorScheme.surfaceContainer,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  color: entry.state.color.withValues(alpha: 0.2),
                                  shape: BoxShape.circle,
                                ),
                                child: Text(entry.state.emoji, style: const TextStyle(fontSize: 20)),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(entry.state.name, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                                    Text(timeAgo, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                                  ],
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 12),
                          Row(
                            children: [
                              _buildMiniStat(theme, Icons.bolt, entry.energy, AppColors.accentOrange),
                              const SizedBox(width: 12),
                              _buildMiniStat(theme, Icons.psychology, entry.stress, AppColors.categoryAnxiety),
                              const SizedBox(width: 12),
                              _buildMiniStat(theme, Icons.hearing, entry.sensory, AppColors.secondaryTeal),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMiniStat(ThemeData theme, IconData icon, int value, Color color) {
    return Expanded(
      child: Row(
        children: [
          Icon(icon, size: 14, color: color),
          const SizedBox(width: 4),
          Text('$value%', style: theme.textTheme.labelSmall?.copyWith(fontWeight: FontWeight.bold, color: color)),
        ],
      ),
    );
  }

  String _formatTimeAgo(AppLocalizations l10n, Duration diff) {
    if (diff.inMinutes < 60) return l10n.get('minutesAgo').replaceAll('{n}', diff.inMinutes.toString());
    if (diff.inHours < 24) return l10n.get('hoursAgo').replaceAll('{n}', diff.inHours.toString());
    if (diff.inDays == 1) return l10n.get('yesterday');
    return l10n.get('daysAgo').replaceAll('{n}', diff.inDays.toString());
  }
}

class _MoodEntry {
  final NeuroState state;
  final int energy;
  final int stress;
  final int sensory;
  final DateTime timestamp;

  const _MoodEntry({
    required this.state,
    required this.energy,
    required this.stress,
    required this.sensory,
    required this.timestamp,
  });
}

class _NeuroStateChip extends ConsumerWidget {
  final NeuroState state;

  const _NeuroStateChip({required this.state});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentNeuroState = ref.watch(neuroStateProvider);
    final isSelected = currentNeuroState == state;

    return FilterChip(
      selected: isSelected,
      label: Text(state.name),
      avatar: Text(state.emoji),
      onSelected: (selected) {
        if (selected) {
          HapticFeedback.mediumImpact();
          ref.read(neuroStateProvider.notifier).setNeuroState(state);
        }
      },
      selectedColor: state.color.withValues(alpha: 0.2),
      checkmarkColor: state.color,
      side: isSelected ? BorderSide(color: state.color) : null,
    );
  }
}

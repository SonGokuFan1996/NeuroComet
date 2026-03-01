import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

/// Neurodivergent-friendly profile traits that users can display
/// These help others understand communication preferences and needs
enum NeuroDivergentTrait {
  // Communication style preferences
  directCommunicator('💬', 'Direct Communicator', 'I prefer clear, literal communication', Color(0xFF2196F3)),
  needsProcessingTime('⏳', 'Processing Time', 'I may need extra time to respond', Color(0xFF9C27B0)),
  textPreferred('📱', 'Text Preferred', "I'm more comfortable with written communication", Color(0xFF4CAF50)),

  // Sensory preferences
  sensorySensitive('🎧', 'Sensory Sensitive', "I'm sensitive to lights, sounds, or textures", Color(0xFFFF9800)),
  needsQuietSpaces('🤫', 'Needs Quiet', 'I function better in calm environments', Color(0xFF607D8B)),

  // Social preferences
  socialBattery('🔋', 'Social Battery', 'I need alone time to recharge', Color(0xFFE91E63)),
  parallelPlay('🎮', 'Parallel Play', 'I enjoy being together while doing separate things', Color(0xFF00BCD4)),
  smallGroups('👥', 'Small Groups', 'I prefer 1-on-1 or small group interactions', Color(0xFF795548)),

  // Routine and structure
  routineOriented('📅', 'Routine Oriented', 'I thrive with structure and predictability', Color(0xFF3F51B5)),
  flexibleTiming('🕐', 'Flexible Timing', 'I may be early, late, or need schedule adjustments', Color(0xFFFF5722)),

  // Special interests
  passionateInterests('🌟', 'Special Interests', 'I have deep, passionate interests I love to share', Color(0xFFFFC107)),
  infoDumpingWelcome('📚', 'Info Dump Friendly', 'Feel free to share everything about your interests!', Color(0xFF8BC34A)),

  // Support needs
  needsReminders('⏰', 'Reminder Helpful', 'Gentle reminders help me stay on track', Color(0xFF9E9E9E)),
  explicitExpectations('📋', 'Clear Expectations', 'I need explicit instructions and expectations', Color(0xFF673AB7)),
  stimmingPositive('🌀', 'Stim-Friendly', 'Stimming is welcome and celebrated here', Color(0xFFCDDC39));

  final String emoji;
  final String label;
  final String description;
  final Color color;

  const NeuroDivergentTrait(this.emoji, this.label, this.description, this.color);
}

/// User's current energy/availability status
enum EnergyStatus {
  fullyCharged('🔋', 'Fully Charged', "I'm feeling energized and social", Color(0xFF4CAF50)),
  socialMode('💚', 'Social Mode', 'Open to interactions', Color(0xFF8BC34A)),
  neutral('😊', 'Neutral', 'Doing okay, typical day', Color(0xFFFFC107)),
  lowBattery('🪫', 'Low Battery', 'Limited energy, may be slow to respond', Color(0xFFFF9800)),
  recharging('💤', 'Recharging', 'Need alone time, will return soon', Color(0xFF9E9E9E)),
  overwhelmed('🫂', 'Need Support', 'Having a tough time, be gentle', Color(0xFFE91E63)),
  hyperfocus('🎯', 'Hyperfocusing', 'Deep in a project, may not respond quickly', Color(0xFF2196F3)),
  doNotDisturb('🔕', 'Do Not Disturb', 'Please no notifications right now', Color(0xFFF44336));

  final String emoji;
  final String label;
  final String description;
  final Color color;

  const EnergyStatus(this.emoji, this.label, this.description, this.color);
}

/// Trait chip widget matching Kotlin TraitsSection
class TraitChip extends StatelessWidget {
  final NeuroDivergentTrait trait;
  final VoidCallback? onTap;

  const TraitChip({
    super.key,
    required this.trait,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: trait.color.withOpacity(0.15),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: trait.color.withOpacity(0.3)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(trait.emoji, style: const TextStyle(fontSize: 14)),
            const SizedBox(width: 6),
            Text(
              trait.label,
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: trait.color,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Traits section widget for profile
class TraitsSection extends StatelessWidget {
  final List<NeuroDivergentTrait> traits;
  final Function(NeuroDivergentTrait)? onTraitTap;

  const TraitsSection({
    super.key,
    required this.traits,
    this.onTraitTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                Icons.psychology,
                size: 18,
                color: theme.colorScheme.primary,
              ),
              const SizedBox(width: 8),
              Text(
                'About Me',
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: traits.map((trait) => TraitChip(
              trait: trait,
              onTap: () => onTraitTap?.call(trait),
            )).toList(),
          ),
        ],
      ),
    );
  }
}

/// Energy status badge widget
class EnergyStatusBadge extends StatelessWidget {
  final EnergyStatus status;
  final VoidCallback? onTap;
  final bool showLabel;

  const EnergyStatusBadge({
    super.key,
    required this.status,
    this.onTap,
    this.showLabel = true,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: status.color.withOpacity(0.15),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: status.color.withOpacity(0.4)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(status.emoji, style: const TextStyle(fontSize: 16)),
            if (showLabel) ...[
              const SizedBox(width: 6),
              Text(
                status.label,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: status.color,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

/// Communication notes card
class CommunicationNotesCard extends StatelessWidget {
  final String notes;

  const CommunicationNotesCard({
    super.key,
    required this.notes,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final containerColor = theme.colorScheme.secondaryContainer;
    final onContainerColor = theme.colorScheme.onSecondaryContainer;

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: containerColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: onContainerColor.withOpacity(0.12),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                Icons.chat_bubble_outline,
                size: 18,
                color: onContainerColor.withOpacity(0.72),
              ),
              const SizedBox(width: 8),
              Text(
                'Communication Notes',
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: onContainerColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            notes,
            style: theme.textTheme.bodyMedium?.copyWith(
              height: 1.5,
              color: onContainerColor.withOpacity(0.82),
            ),
          ),
        ],
      ),
    );
  }
}

/// Special interests section
class InterestsSection extends StatelessWidget {
  final List<String> interests;

  const InterestsSection({
    super.key,
    required this.interests,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Text('🌟', style: TextStyle(fontSize: 18)),
              const SizedBox(width: 8),
              Text(
                'Special Interests',
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: interests.map((interest) => Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: theme.colorScheme.secondaryContainer,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Text(
                interest,
                style: TextStyle(
                  fontSize: 13,
                  color: theme.colorScheme.onSecondaryContainer,
                ),
              ),
            )).toList(),
          ),
        ],
      ),
    );
  }
}

/// Trait info dialog matching Kotlin TraitInfoDialog
class TraitInfoDialog extends StatelessWidget {
  final NeuroDivergentTrait trait;

  const TraitInfoDialog({
    super.key,
    required this.trait,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return AlertDialog(
      shape: RoundedCornerShape.circular(20),
      title: Row(
        children: [
          Text(trait.emoji, style: const TextStyle(fontSize: 28)),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              trait.label,
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: trait.color,
              ),
            ),
          ),
        ],
      ),
      content: Text(
        trait.description,
        style: theme.textTheme.bodyLarge?.copyWith(
          height: 1.5,
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Got it!'),
        ),
      ],
    );
  }
}

/// Energy status picker dialog
class EnergyStatusPickerDialog extends StatelessWidget {
  final EnergyStatus currentStatus;
  final Function(EnergyStatus)? onStatusSelected;

  const EnergyStatusPickerDialog({
    super.key,
    required this.currentStatus,
    this.onStatusSelected,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return AlertDialog(
      shape: RoundedCornerShape.circular(20),
      title: Row(
        children: [
          Icon(Icons.battery_charging_full, color: theme.colorScheme.primary),
          const SizedBox(width: 12),
          Text(l10n.get('howAreYouFeeling')),
        ],
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: EnergyStatus.values.map((status) {
          final isSelected = status == currentStatus;
          return ListTile(
            shape: RoundedCornerShape.circular(12),
            selected: isSelected,
            selectedTileColor: status.color.withOpacity(0.15),
            leading: Text(status.emoji, style: const TextStyle(fontSize: 24)),
            title: Text(
              status.label,
              style: TextStyle(
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                color: isSelected ? status.color : null,
              ),
            ),
            subtitle: Text(
              status.description,
              style: theme.textTheme.bodySmall,
            ),
            trailing: isSelected
                ? Icon(Icons.check_circle, color: status.color)
                : null,
            onTap: () {
              onStatusSelected?.call(status);
              Navigator.pop(context);
            },
          );
        }).toList(),
      ),
    );
  }
}

/// Extension for RoundedCornerShape
extension RoundedCornerShape on ShapeBorder {
  static RoundedRectangleBorder circular(double radius) {
    return RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(radius),
    );
  }
}


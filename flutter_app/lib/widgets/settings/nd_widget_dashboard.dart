import 'package:flutter/material.dart';
import '../common/neuro_widgets.dart';
import '../common/typewriter_text.dart';

enum SensoryLevel { green, yellow, red }

class SimpleTask {
  final String id;
  final String title;
  final String emoji;
  bool isCompleted;

  SimpleTask(this.id, this.title, {this.emoji = '', this.isCompleted = false});
}

class NeurodivergentWidgetDashboard extends StatefulWidget {
  final bool highContrast;
  final bool reducedMotion;

  const NeurodivergentWidgetDashboard({
    super.key,
    this.highContrast = false,
    this.reducedMotion = false,
  });

  @override
  State<NeurodivergentWidgetDashboard> createState() => _NeurodivergentWidgetDashboardState();
}

class _NeurodivergentWidgetDashboardState extends State<NeurodivergentWidgetDashboard> {
  int _focusMinutes = 25;
  bool _isTimerRunning = false;
  double _energyLevel = 70;
  String? _selectedMood;
  SensoryLevel _sensoryLevel = SensoryLevel.green;
  final List<SimpleTask> _tasks = [
    SimpleTask("1", "Morning routine", emoji: "🌅"),
    SimpleTask("2", "Take meds", emoji: "💊"),
    SimpleTask("3", "Drink water", emoji: "💧"),
    SimpleTask("4", "Movement break", emoji: "🏃"),
  ];

  final List<Map<String, String>> _moods = [
    {'id': 'happy', 'label': 'Happy', 'emoji': '😊'},
    {'id': 'calm', 'label': 'Calm', 'emoji': '😌'},
    {'id': 'focused', 'label': 'Focused', 'emoji': '🧐'},
    {'id': 'tired', 'label': 'Tired', 'emoji': '😴'},
    {'id': 'anxious', 'label': 'Anxious', 'emoji': '😰'},
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    
    return Theme(
      data: theme.copyWith(
        // Simple high contrast simulation for preview
        visualDensity: widget.highContrast ? VisualDensity.comfortable : theme.visualDensity,
      ),
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const TypewriterText(
            text: "Welcome to your neurodivergent-friendly dashboard.",
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          
          // Focus Timer Card
          _buildFocusTimer(),
          
          // Energy & Sensory Card
          _buildEnergySensory(),
          
          // Mood Picker
          _buildMoodPicker(),
          
          // Tasks
          _buildTasks(),
          
          const SizedBox(height: 32),
        ],
      ),
    );
  }

  Widget _buildFocusTimer() {
    return NeuroCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.timer, size: 20),
              const SizedBox(width: 8),
              Text("Focus Timer", style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("${_focusMinutes}m", style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold)),
              Row(
                children: [
                  IconButton(
                    onPressed: _isTimerRunning ? null : () => setState(() => _focusMinutes = (_focusMinutes - 5).clamp(5, 60)),
                    icon: const Icon(Icons.remove_circle_outline),
                  ),
                  IconButton(
                    onPressed: _isTimerRunning ? null : () => setState(() => _focusMinutes = (_focusMinutes + 5).clamp(5, 60)),
                    icon: const Icon(Icons.add_circle_outline),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 8),
          SizedBox(
            width: double.infinity,
            child: NeuroButton(
              onPressed: () => setState(() => _isTimerRunning = !_isTimerRunning),
              backgroundColor: _isTimerRunning ? Colors.red.withValues(alpha: 0.1) : null,
              foregroundColor: _isTimerRunning ? Colors.red : null,
              child: Text(_isTimerRunning ? "Stop Focus Session" : "Start Focus Session"),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEnergySensory() {
    return NeuroCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text("Energy Level", style: Theme.of(context).textTheme.labelLarge?.copyWith(fontWeight: FontWeight.bold)),
          Slider(
            value: _energyLevel,
            max: 100,
            divisions: 10,
            label: _energyLevel.round().toString(),
            onChanged: (v) => setState(() => _energyLevel = v),
          ),
          const Divider(),
          const SizedBox(height: 8),
          Text("Sensory Environment", style: Theme.of(context).textTheme.labelLarge?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: SensoryLevel.values.map((level) {
              final isSelected = _sensoryLevel == level;
              final color = level == SensoryLevel.green 
                  ? Colors.green 
                  : level == SensoryLevel.yellow 
                      ? Colors.orange 
                      : Colors.red;
              
              return InkWell(
                onTap: () => setState(() => _sensoryLevel = level),
                child: Column(
                  children: [
                    Container(
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        color: isSelected ? color : color.withValues(alpha: 0.1),
                        shape: BoxShape.circle,
                        border: Border.all(color: color, width: 2),
                      ),
                      child: Icon(
                        level == SensoryLevel.green ? Icons.check : level == SensoryLevel.yellow ? Icons.warning : Icons.error,
                        color: isSelected ? Colors.white : color,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(level.name.toUpperCase(), style: TextStyle(fontSize: 10, fontWeight: isSelected ? FontWeight.bold : null)),
                  ],
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildMoodPicker() {
    return NeuroCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text("How are you feeling?", style: Theme.of(context).textTheme.labelLarge?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _moods.map((mood) {
              final isSelected = _selectedMood == mood['id'];
              return NeuroChip(
                label: mood['label']!,
                emoji: mood['emoji'],
                selected: isSelected,
                onTap: () => setState(() => _selectedMood = mood['id']),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildTasks() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const NeuroSectionHeader(title: "Today's Routine"),
        ..._tasks.map((task) => NeuroCard(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Row(
            children: [
              Checkbox(
                value: task.isCompleted,
                onChanged: (v) => setState(() => task.isCompleted = v ?? false),
              ),
              Text(task.emoji, style: const TextStyle(fontSize: 20)),
              const SizedBox(width: 12),
              Text(
                task.title,
                style: TextStyle(
                  decoration: task.isCompleted ? TextDecoration.lineThrough : null,
                  color: task.isCompleted ? Theme.of(context).colorScheme.onSurfaceVariant : null,
                ),
              ),
            ],
          ),
        )),
      ],
    );
  }
}

extension on NeuroChip {
  // Added helper to match internal constructor if needed, but here we just pass label/icon
}

// Fixed NeuroChip for emoji support
class NeuroChip extends StatelessWidget {
  final String label;
  final String? emoji;
  final bool selected;
  final VoidCallback? onTap;

  const NeuroChip({
    super.key,
    required this.label,
    this.emoji,
    this.selected = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = theme.colorScheme.primary;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: selected ? color : color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color, width: 1),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (emoji != null) ...[
              Text(emoji!),
              const SizedBox(width: 6),
            ],
            Text(
              label,
              style: TextStyle(
                color: selected ? Colors.white : color,
                fontWeight: FontWeight.bold,
                fontSize: 13,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

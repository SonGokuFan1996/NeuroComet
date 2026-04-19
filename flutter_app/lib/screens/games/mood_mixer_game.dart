import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Mood Mixer Game - Blend colors/emotions to explore and create moods
class MoodMixerGame extends ConsumerStatefulWidget {
  const MoodMixerGame({super.key});

  @override
  ConsumerState<MoodMixerGame> createState() => _MoodMixerGameState();
}

class _MoodMixerGameState extends ConsumerState<MoodMixerGame>
    with TickerProviderStateMixin {
  final List<_MoodIngredient> _ingredients = [];
  Color _mixedColor = Colors.grey;
  String _mixedMoodName = 'Neutral';
  String _mixedMoodEmoji = '😐';
  late AnimationController _mixController;
  late Animation<double> _mixAnimation;

  static const _availableIngredients = [
    _MoodIngredient(name: 'Joy', emoji: '😊', color: Color(0xFFFFEB3B), weight: 0),
    _MoodIngredient(name: 'Calm', emoji: '😌', color: Color(0xFF4FC3F7), weight: 0),
    _MoodIngredient(name: 'Energy', emoji: '⚡', color: Color(0xFFFF5722), weight: 0),
    _MoodIngredient(name: 'Love', emoji: '💕', color: Color(0xFFE91E63), weight: 0),
    _MoodIngredient(name: 'Wonder', emoji: '✨', color: Color(0xFF9C27B0), weight: 0),
    _MoodIngredient(name: 'Focus', emoji: '🎯', color: Color(0xFF2196F3), weight: 0),
    _MoodIngredient(name: 'Peace', emoji: '🕊️', color: Color(0xFF66BB6A), weight: 0),
    _MoodIngredient(name: 'Courage', emoji: '🦁', color: Color(0xFFFF9800), weight: 0),
  ];

  @override
  void initState() {
    super.initState();
    _mixController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );
    _mixAnimation = CurvedAnimation(
      parent: _mixController,
      curve: Curves.elasticOut,
    );
  }

  @override
  void dispose() {
    _mixController.dispose();
    super.dispose();
  }

  void _addIngredient(_MoodIngredient ingredient) {
    if (!kIsWeb) HapticFeedback.lightImpact();
    setState(() {
      final existing = _ingredients.indexWhere((i) => i.name == ingredient.name);
      if (existing >= 0) {
        _ingredients[existing] = ingredient.copyWith(weight: _ingredients[existing].weight + 1);
      } else {
        _ingredients.add(ingredient.copyWith(weight: 1));
      }
      _recalculateMix();
    });
    _mixController.forward(from: 0);
  }

  void _removeIngredient(int index) {
    if (!kIsWeb) HapticFeedback.selectionClick();
    setState(() {
      if (_ingredients[index].weight > 1) {
        _ingredients[index] = _ingredients[index].copyWith(weight: _ingredients[index].weight - 1);
      } else {
        _ingredients.removeAt(index);
      }
      _recalculateMix();
    });
  }

  void _recalculateMix() {
    if (_ingredients.isEmpty) {
      _mixedColor = Colors.grey;
      _mixedMoodName = 'Neutral';
      _mixedMoodEmoji = '😐';
      return;
    }

    double r = 0, g = 0, b = 0;
    int totalWeight = 0;
    for (final ing in _ingredients) {
      r += ing.color.red * ing.weight;
      g += ing.color.green * ing.weight;
      b += ing.color.blue * ing.weight;
      totalWeight += ing.weight;
    }
    _mixedColor = Color.fromRGBO(
      (r / totalWeight).round().clamp(0, 255),
      (g / totalWeight).round().clamp(0, 255),
      (b / totalWeight).round().clamp(0, 255),
      1,
    );

    // Determine dominant mood
    final dominant = _ingredients.reduce((a, b) => a.weight >= b.weight ? a : b);
    _mixedMoodEmoji = dominant.emoji;
    if (_ingredients.length == 1) {
      _mixedMoodName = dominant.name;
    } else {
      final sorted = List.of(_ingredients)..sort((a, b) => b.weight.compareTo(a.weight));
      _mixedMoodName = '${sorted[0].name} + ${sorted[1].name}';
    }
  }

  void _clearMix() {
    if (!kIsWeb) HapticFeedback.mediumImpact();
    setState(() {
      _ingredients.clear();
      _recalculateMix();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mood Mixer'),
        actions: [
          if (_ingredients.isNotEmpty)
            IconButton(icon: const Icon(Icons.refresh), onPressed: _clearMix, tooltip: 'Reset'),
        ],
      ),
      body: Column(
        children: [
          // Result orb
          Expanded(
            flex: 3,
            child: Center(
              child: AnimatedBuilder(
                animation: _mixAnimation,
                builder: (context, _) {
                  final scale = 0.8 + _mixAnimation.value * 0.2;
                  return Transform.scale(
                    scale: _ingredients.isEmpty ? 1.0 : scale,
                    child: Container(
                      width: 180,
                      height: 180,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: _mixedColor,
                        boxShadow: [
                          BoxShadow(
                            color: _mixedColor.withAlpha(100),
                            blurRadius: 40,
                            spreadRadius: 10,
                          ),
                        ],
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(_mixedMoodEmoji, style: const TextStyle(fontSize: 40)),
                          const SizedBox(height: 8),
                          Text(
                            _mixedMoodName,
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
          // Current mix
          if (_ingredients.isNotEmpty)
            Container(
              height: 60,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _ingredients.length,
                itemBuilder: (context, index) {
                  final ing = _ingredients[index];
                  return GestureDetector(
                    onTap: () => _removeIngredient(index),
                    child: Container(
                      margin: const EdgeInsets.symmetric(horizontal: 4),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: ing.color.withAlpha(40),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: ing.color.withAlpha(100)),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(ing.emoji, style: const TextStyle(fontSize: 18)),
                          if (ing.weight > 1) ...[
                            const SizedBox(width: 4),
                            Text('×${ing.weight}', style: TextStyle(fontSize: 12, color: ing.color, fontWeight: FontWeight.bold)),
                          ],
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
          const Divider(),
          // Ingredients palette
          Expanded(
            flex: 2,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Add Ingredients', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  Expanded(
                    child: GridView.builder(
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 4,
                        crossAxisSpacing: 12,
                        mainAxisSpacing: 12,
                        childAspectRatio: 0.85,
                      ),
                      itemCount: _availableIngredients.length,
                      itemBuilder: (context, index) {
                        final ing = _availableIngredients[index];
                        return GestureDetector(
                          onTap: () => _addIngredient(ing),
                          child: Container(
                            decoration: BoxDecoration(
                              color: ing.color.withAlpha(25),
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(color: ing.color.withAlpha(80)),
                            ),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(ing.emoji, style: const TextStyle(fontSize: 24)),
                                const SizedBox(height: 4),
                                Text(ing.name, style: TextStyle(fontSize: 10, color: ing.color, fontWeight: FontWeight.bold)),
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
          ),
        ],
      ),
    );
  }
}

class _MoodIngredient {
  final String name;
  final String emoji;
  final Color color;
  final int weight;

  const _MoodIngredient({
    required this.name,
    required this.emoji,
    required this.color,
    required this.weight,
  });

  _MoodIngredient copyWith({int? weight}) => _MoodIngredient(
    name: name,
    emoji: emoji,
    color: color,
    weight: weight ?? this.weight,
  );
}


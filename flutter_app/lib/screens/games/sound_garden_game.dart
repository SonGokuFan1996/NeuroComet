import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Sound Garden Game - Create calming soundscapes by placing elements
class SoundGardenGame extends ConsumerStatefulWidget {
  const SoundGardenGame({super.key});

  @override
  ConsumerState<SoundGardenGame> createState() => _SoundGardenGameState();
}

class _SoundGardenGameState extends ConsumerState<SoundGardenGame>
    with TickerProviderStateMixin {
  final List<_SoundElement> _elements = [];
  String _selectedType = 'rain';
  late AnimationController _pulseController;

  static const _soundTypes = {
    'rain': (Icons.water_drop, Color(0xFF42A5F5), 'Rain'),
    'wind': (Icons.air, Color(0xFF66BB6A), 'Wind'),
    'birds': (Icons.flutter_dash, Color(0xFFFFCA28), 'Birds'),
    'waves': (Icons.waves, Color(0xFF26C6DA), 'Waves'),
    'chimes': (Icons.music_note, Color(0xFFAB47BC), 'Chimes'),
    'thunder': (Icons.flash_on, Color(0xFF78909C), 'Thunder'),
    'crickets': (Icons.grass, Color(0xFF8BC34A), 'Crickets'),
    'fireplace': (Icons.local_fire_department, Color(0xFFFF7043), 'Fire'),
  };

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 2),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  void _addElement(Offset position) {
    if (!kIsWeb) HapticFeedback.lightImpact();
    final type = _soundTypes[_selectedType]!;
    setState(() {
      _elements.add(_SoundElement(
        position: position,
        type: _selectedType,
        icon: type.$1,
        color: type.$2,
        label: type.$3,
        volume: 0.7,
      ));
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sound Garden'),
        actions: [
          IconButton(
            icon: const Icon(Icons.delete_sweep),
            onPressed: () => setState(() => _elements.clear()),
            tooltip: 'Clear all',
          ),
        ],
      ),
      body: Column(
        children: [
          // Sound palette
          Container(
            height: 72,
            padding: const EdgeInsets.symmetric(horizontal: 8),
            child: ListView(
              scrollDirection: Axis.horizontal,
              children: _soundTypes.entries.map((entry) {
                final isSelected = _selectedType == entry.key;
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 8),
                  child: GestureDetector(
                    onTap: () => setState(() => _selectedType = entry.key),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: isSelected ? entry.value.$2.withAlpha(50) : Theme.of(context).colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(20),
                        border: isSelected ? Border.all(color: entry.value.$2, width: 2) : null,
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(entry.value.$1, size: 20, color: isSelected ? entry.value.$2 : null),
                          const SizedBox(width: 6),
                          Text(entry.value.$3, style: TextStyle(fontSize: 12, fontWeight: isSelected ? FontWeight.bold : FontWeight.normal)),
                        ],
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          // Garden canvas
          Expanded(
            child: GestureDetector(
              onTapDown: (details) => _addElement(details.localPosition),
              child: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.indigo.shade900.withAlpha(30),
                      Colors.green.shade900.withAlpha(30),
                    ],
                  ),
                ),
                child: Stack(
                  children: [
                    if (_elements.isEmpty)
                      const Center(
                        child: Text(
                          'Tap anywhere to place sounds\nin your garden 🌿',
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 16, color: Colors.grey),
                        ),
                      ),
                    ..._elements.map((element) => Positioned(
                      left: element.position.dx - 24,
                      top: element.position.dy - 24,
                      child: AnimatedBuilder(
                        animation: _pulseController,
                        builder: (context, child) {
                          final scale = 1.0 + _pulseController.value * 0.15;
                          return Transform.scale(
                            scale: scale,
                            child: GestureDetector(
                              onLongPress: () {
                                setState(() => _elements.remove(element));
                                if (!kIsWeb) HapticFeedback.mediumImpact();
                              },
                              child: Container(
                                width: 48,
                                height: 48,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: element.color.withAlpha(180),
                                  boxShadow: [
                                    BoxShadow(
                                      color: element.color.withAlpha(80),
                                      blurRadius: 12 + _pulseController.value * 8,
                                      spreadRadius: 2,
                                    ),
                                  ],
                                ),
                                child: Icon(element.icon, color: Colors.white, size: 24),
                              ),
                            ),
                          );
                        },
                      ),
                    )),
                  ],
                ),
              ),
            ),
          ),
          // Status bar
          Container(
            padding: const EdgeInsets.all(12),
            child: Text(
              '${_elements.length} sound${_elements.length != 1 ? 's' : ''} in garden • Long press to remove',
              style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.onSurfaceVariant),
            ),
          ),
        ],
      ),
    );
  }
}

class _SoundElement {
  final Offset position;
  final String type;
  final IconData icon;
  final Color color;
  final String label;
  double volume;

  _SoundElement({
    required this.position,
    required this.type,
    required this.icon,
    required this.color,
    required this.label,
    required this.volume,
  });
}


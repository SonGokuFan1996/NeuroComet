import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Safe Space Game - A virtual sensory retreat room
class SafeSpaceGame extends ConsumerStatefulWidget {
  const SafeSpaceGame({super.key});

  @override
  ConsumerState<SafeSpaceGame> createState() => _SafeSpaceGameState();
}

class _SafeSpaceGameState extends ConsumerState<SafeSpaceGame>
    with TickerProviderStateMixin {
  String _environment = 'cozy_room';
  double _lightLevel = 0.6;
  bool _showStars = true;
  bool _showFireplace = true;
  bool _showPlants = true;
  Color _ambientColor = const Color(0xFF1A1A2E);
  late AnimationController _breatheController;

  final _environments = {
    'cozy_room': ('Cozy Room', Icons.weekend, const Color(0xFF1A1A2E)),
    'forest': ('Forest', Icons.park, const Color(0xFF1B5E20)),
    'beach': ('Beach', Icons.beach_access, const Color(0xFF01579B)),
    'clouds': ('Clouds', Icons.cloud, const Color(0xFF283593)),
    'space': ('Space', Icons.auto_awesome, const Color(0xFF0D0D1A)),
  };

  @override
  void initState() {
    super.initState();
    _breatheController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 4),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _breatheController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text('Safe Space', style: TextStyle(color: Colors.white)),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Stack(
        fit: StackFit.expand,
        children: [
          // Background
          AnimatedContainer(
            duration: const Duration(seconds: 1),
            decoration: BoxDecoration(
              gradient: RadialGradient(
                center: Alignment.center,
                radius: 1.2,
                colors: [
                  _ambientColor.withAlpha((_lightLevel * 255).round()),
                  _ambientColor,
                  Colors.black,
                ],
              ),
            ),
          ),
          // Stars
          if (_showStars)
            AnimatedBuilder(
              animation: _breatheController,
              builder: (context, _) => CustomPaint(
                painter: _StarsPainter(_breatheController.value),
              ),
            ),
          // Fireplace glow
          if (_showFireplace)
            Positioned(
              bottom: 200,
              left: 0,
              right: 0,
              child: AnimatedBuilder(
                animation: _breatheController,
                builder: (context, _) {
                  return Center(
                    child: Container(
                      width: 100 + _breatheController.value * 20,
                      height: 100 + _breatheController.value * 20,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: RadialGradient(
                          colors: [
                            Colors.orange.withAlpha(60),
                            Colors.orange.withAlpha(20),
                            Colors.transparent,
                          ],
                        ),
                      ),
                      child: const Icon(Icons.local_fire_department, color: Colors.orange, size: 48),
                    ),
                  );
                },
              ),
            ),
          // Plants
          if (_showPlants) ...[
            Positioned(
              bottom: 100,
              left: 20,
              child: Icon(Icons.eco, color: Colors.green.withAlpha(150), size: 40),
            ),
            Positioned(
              bottom: 120,
              right: 30,
              child: Icon(Icons.local_florist, color: Colors.green.withAlpha(120), size: 36),
            ),
          ],
          // Breathing guide
          Positioned(
            bottom: 320,
            left: 0,
            right: 0,
            child: AnimatedBuilder(
              animation: _breatheController,
              builder: (context, _) {
                final scale = 0.8 + _breatheController.value * 0.4;
                return Center(
                  child: Transform.scale(
                    scale: scale,
                    child: Container(
                      width: 80,
                      height: 80,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(color: Colors.white.withAlpha(100), width: 2),
                      ),
                      child: Center(
                        child: Text(
                          _breatheController.value < 0.5 ? 'Breathe\nin' : 'Breathe\nout',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.white.withAlpha(180), fontSize: 12),
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
          // Controls panel at bottom
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Colors.transparent, Colors.black.withAlpha(200)],
                ),
              ),
              child: SafeArea(
                top: false,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Environment selector
                    SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: _environments.entries.map((e) {
                          final isSelected = _environment == e.key;
                          return Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 4),
                            child: ChoiceChip(
                              avatar: Icon(e.value.$2, size: 16),
                              label: Text(e.value.$1, style: const TextStyle(fontSize: 11)),
                              selected: isSelected,
                              onSelected: (_) {
                                if (!kIsWeb) HapticFeedback.selectionClick();
                                setState(() {
                                  _environment = e.key;
                                  _ambientColor = e.value.$3;
                                });
                              },
                            ),
                          );
                        }).toList(),
                      ),
                    ),
                    const SizedBox(height: 12),
                    // Light slider
                    Row(
                      children: [
                        const Icon(Icons.dark_mode, color: Colors.white54, size: 18),
                        Expanded(
                          child: Slider(
                            value: _lightLevel,
                            onChanged: (v) => setState(() => _lightLevel = v),
                          ),
                        ),
                        const Icon(Icons.light_mode, color: Colors.white54, size: 18),
                      ],
                    ),
                    // Toggles
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _ToggleChip(label: 'Stars', active: _showStars, onTap: () => setState(() => _showStars = !_showStars)),
                        _ToggleChip(label: 'Fire', active: _showFireplace, onTap: () => setState(() => _showFireplace = !_showFireplace)),
                        _ToggleChip(label: 'Plants', active: _showPlants, onTap: () => setState(() => _showPlants = !_showPlants)),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ToggleChip extends StatelessWidget {
  final String label;
  final bool active;
  final VoidCallback onTap;

  const _ToggleChip({required this.label, required this.active, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(20),
          color: active ? Colors.white.withAlpha(40) : Colors.white.withAlpha(15),
        ),
        child: Text(label, style: TextStyle(color: active ? Colors.white : Colors.white54, fontSize: 12)),
      ),
    );
  }
}

class _StarsPainter extends CustomPainter {
  final double animValue;
  _StarsPainter(this.animValue);

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..color = Colors.white;
    final random = [
      Offset(size.width * 0.1, size.height * 0.15),
      Offset(size.width * 0.3, size.height * 0.08),
      Offset(size.width * 0.5, size.height * 0.2),
      Offset(size.width * 0.7, size.height * 0.12),
      Offset(size.width * 0.85, size.height * 0.25),
      Offset(size.width * 0.2, size.height * 0.35),
      Offset(size.width * 0.6, size.height * 0.3),
      Offset(size.width * 0.9, size.height * 0.4),
      Offset(size.width * 0.15, size.height * 0.5),
      Offset(size.width * 0.45, size.height * 0.45),
    ];
    for (int i = 0; i < random.length; i++) {
      final alpha = (100 + (animValue * 155 * (i.isEven ? 1 : -1)).abs()).clamp(50, 255).toInt();
      paint.color = Colors.white.withAlpha(alpha);
      canvas.drawCircle(random[i], 1.5, paint);
    }
  }

  @override
  bool shouldRepaint(covariant _StarsPainter old) => old.animValue != animValue;
}


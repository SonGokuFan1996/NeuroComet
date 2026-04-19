import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';

void _safeHaptic() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

/// Texture Tiles Game - Match tactile textures for sensory exploration
class TextureTilesGame extends ConsumerStatefulWidget {
  const TextureTilesGame({super.key});

  @override
  ConsumerState<TextureTilesGame> createState() => _TextureTilesGameState();
}

class _TextureTilesGameState extends ConsumerState<TextureTilesGame>
    with TickerProviderStateMixin {
  static const _tilePatterns = [
    ('Smooth', Icons.circle, Color(0xFF7C4DFF)),
    ('Bumpy', Icons.grain, Color(0xFFFF6E40)),
    ('Wavy', Icons.water, Color(0xFF00BFA5)),
    ('Dotted', Icons.blur_on, Color(0xFFFFAB40)),
    ('Striped', Icons.view_stream, Color(0xFF536DFE)),
    ('Cross', Icons.close, Color(0xFFFF4081)),
    ('Spiral', Icons.cyclone, Color(0xFF00BCD4)),
    ('Diamond', Icons.diamond, Color(0xFFE040FB)),
  ];

  late List<_TileData> _tiles;
  _TileData? _firstSelection;
  _TileData? _secondSelection;
  int _matches = 0;
  int _moves = 0;
  bool _isChecking = false;

  @override
  void initState() {
    super.initState();
    _initGame();
  }

  void _initGame() {
    final patterns = _tilePatterns.take(8).toList();
    final pairs = [...patterns, ...patterns];
    pairs.shuffle(Random());
    _tiles = pairs.asMap().entries.map((e) => _TileData(
      index: e.key,
      name: e.value.$1,
      icon: e.value.$2,
      color: e.value.$3,
    )).toList();
    _firstSelection = null;
    _secondSelection = null;
    _matches = 0;
    _moves = 0;
    _isChecking = false;
  }

  void _onTileTap(_TileData tile) {
    if (_isChecking || tile.isMatched || tile == _firstSelection) return;
    _safeHaptic();

    setState(() {
      if (_firstSelection == null) {
        _firstSelection = tile;
        tile.isRevealed = true;
      } else {
        _secondSelection = tile;
        tile.isRevealed = true;
        _moves++;
        _isChecking = true;

        if (_firstSelection!.name == _secondSelection!.name) {
          _firstSelection!.isMatched = true;
          _secondSelection!.isMatched = true;
          _matches++;
          _firstSelection = null;
          _secondSelection = null;
          _isChecking = false;
          if (!kIsWeb) HapticFeedback.mediumImpact();
        } else {
          Future.delayed(const Duration(milliseconds: 800), () {
            if (mounted) {
              setState(() {
                _firstSelection?.isRevealed = false;
                _secondSelection?.isRevealed = false;
                _firstSelection = null;
                _secondSelection = null;
                _isChecking = false;
              });
            }
          });
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final isComplete = _matches == _tilePatterns.length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Texture Tiles'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => setState(() => _initGame()),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _StatChip(label: 'Matches', value: '$_matches/${_tilePatterns.length}'),
                _StatChip(label: 'Moves', value: '$_moves'),
              ],
            ),
            const SizedBox(height: 16),
            if (isComplete) ...[
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: AppColors.success.withAlpha(30),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Column(
                  children: [
                    const Text('🎉 Complete!', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                    Text('Finished in $_moves moves', style: const TextStyle(fontSize: 14)),
                    const SizedBox(height: 12),
                    ElevatedButton(
                      onPressed: () => setState(() => _initGame()),
                      child: const Text('Play Again'),
                    ),
                  ],
                ),
              ),
            ],
            const SizedBox(height: 16),
            Expanded(
              child: GridView.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 4,
                  crossAxisSpacing: 8,
                  mainAxisSpacing: 8,
                ),
                itemCount: _tiles.length,
                itemBuilder: (context, index) {
                  final tile = _tiles[index];
                  return GestureDetector(
                    onTap: () => _onTileTap(tile),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 300),
                      decoration: BoxDecoration(
                        color: tile.isRevealed || tile.isMatched
                            ? tile.color.withAlpha(tile.isMatched ? 60 : 200)
                            : Theme.of(context).colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(12),
                        border: tile.isMatched
                            ? Border.all(color: AppColors.success, width: 2)
                            : null,
                      ),
                      child: Center(
                        child: tile.isRevealed || tile.isMatched
                            ? Icon(tile.icon, size: 32, color: tile.isMatched ? tile.color : Colors.white)
                            : Icon(Icons.help_outline, size: 28, color: Theme.of(context).colorScheme.onSurfaceVariant.withAlpha(100)),
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
}

class _TileData {
  final int index;
  final String name;
  final IconData icon;
  final Color color;
  bool isRevealed = false;
  bool isMatched = false;

  _TileData({
    required this.index,
    required this.name,
    required this.icon,
    required this.color,
  });
}

class _StatChip extends StatelessWidget {
  final String label;
  final String value;

  const _StatChip({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(value, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
        Text(label, style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.onSurfaceVariant)),
      ],
    );
  }
}


import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';

/// Stim Sequencer Game - Create rhythmic stim patterns
class StimSequencerGame extends ConsumerStatefulWidget {
  const StimSequencerGame({super.key});

  @override
  ConsumerState<StimSequencerGame> createState() => _StimSequencerGameState();
}

class _StimSequencerGameState extends ConsumerState<StimSequencerGame>
    with TickerProviderStateMixin {
  static const int _rows = 4;
  static const int _cols = 8;
  late List<List<bool>> _grid;
  int _currentStep = -1;
  bool _isPlaying = false;
  int _bpm = 120;
  late AnimationController _stepController;

  final _rowColors = [
    const Color(0xFFE040FB),
    const Color(0xFF536DFE),
    const Color(0xFF00BFA5),
    const Color(0xFFFFAB40),
  ];

  final _rowLabels = ['High', 'Mid', 'Low', 'Bass'];

  @override
  void initState() {
    super.initState();
    _grid = List.generate(_rows, (_) => List.generate(_cols, (_) => false));
    _stepController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: (60000 / _bpm).round()),
    );
  }

  @override
  void dispose() {
    _stepController.dispose();
    super.dispose();
  }

  void _toggleCell(int row, int col) {
    if (!kIsWeb) HapticFeedback.lightImpact();
    setState(() => _grid[row][col] = !_grid[row][col]);
  }

  void _togglePlay() {
    setState(() {
      _isPlaying = !_isPlaying;
      if (_isPlaying) {
        _runSequencer();
      } else {
        _currentStep = -1;
      }
    });
  }

  Future<void> _runSequencer() async {
    while (_isPlaying && mounted) {
      for (int col = 0; col < _cols && _isPlaying && mounted; col++) {
        setState(() => _currentStep = col);
        // Haptic feedback for active cells
        for (int row = 0; row < _rows; row++) {
          if (_grid[row][col] && !kIsWeb) {
            HapticFeedback.selectionClick();
          }
        }
        await Future.delayed(Duration(milliseconds: (60000 / _bpm).round()));
      }
    }
  }

  void _clearGrid() {
    setState(() {
      _grid = List.generate(_rows, (_) => List.generate(_cols, (_) => false));
      _currentStep = -1;
    });
  }

  void _randomize() {
    final random = Random();
    setState(() {
      _grid = List.generate(_rows, (_) => List.generate(_cols, (_) => random.nextBool() && random.nextBool()));
    });
    if (!kIsWeb) HapticFeedback.mediumImpact();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Stim Sequencer'),
        actions: [
          IconButton(icon: const Icon(Icons.casino), onPressed: _randomize, tooltip: 'Randomize'),
          IconButton(icon: const Icon(Icons.delete_sweep), onPressed: _clearGrid, tooltip: 'Clear'),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // Controls
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: Icon(_isPlaying ? Icons.pause_circle_filled : Icons.play_circle_filled, size: 48),
                  color: AppColors.primaryPurple,
                  onPressed: _togglePlay,
                ),
                const SizedBox(width: 24),
                Column(
                  children: [
                    Text('$_bpm BPM', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    Row(
                      children: [
                        IconButton(
                          icon: const Icon(Icons.remove_circle_outline),
                          onPressed: _bpm > 60 ? () => setState(() => _bpm -= 10) : null,
                        ),
                        IconButton(
                          icon: const Icon(Icons.add_circle_outline),
                          onPressed: _bpm < 200 ? () => setState(() => _bpm += 10) : null,
                        ),
                      ],
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 24),
            // Grid
            Expanded(
              child: Row(
                children: [
                  // Row labels
                  Column(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: List.generate(_rows, (row) => SizedBox(
                      width: 40,
                      child: Text(_rowLabels[row], style: TextStyle(fontSize: 10, color: _rowColors[row], fontWeight: FontWeight.bold)),
                    )),
                  ),
                  // Grid cells
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: List.generate(_rows, (row) => Row(
                        children: List.generate(_cols, (col) {
                          final isActive = _grid[row][col];
                          final isCurrent = col == _currentStep;
                          return Expanded(
                            child: GestureDetector(
                              onTap: () => _toggleCell(row, col),
                              child: AnimatedContainer(
                                duration: const Duration(milliseconds: 100),
                                margin: const EdgeInsets.all(2),
                                decoration: BoxDecoration(
                                  color: isActive
                                      ? _rowColors[row].withAlpha(isCurrent ? 255 : 180)
                                      : isCurrent
                                          ? Theme.of(context).colorScheme.surfaceContainerHighest
                                          : Theme.of(context).colorScheme.surfaceContainerHighest.withAlpha(100),
                                  borderRadius: BorderRadius.circular(6),
                                  border: isCurrent ? Border.all(color: Colors.white, width: 1.5) : null,
                                ),
                                child: AspectRatio(
                                  aspectRatio: 1,
                                  child: isActive && isCurrent
                                      ? Center(child: Container(width: 8, height: 8, decoration: const BoxDecoration(shape: BoxShape.circle, color: Colors.white)))
                                      : null,
                                ),
                              ),
                            ),
                          );
                        }),
                      )),
                    ),
                  ),
                ],
              ),
            ),
            // Step indicators
            Padding(
              padding: const EdgeInsets.only(left: 40, top: 8),
              child: Row(
                children: List.generate(_cols, (col) => Expanded(
                  child: Center(
                    child: Container(
                      width: 6,
                      height: 6,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: col == _currentStep ? AppColors.primaryPurple : Colors.grey.withAlpha(80),
                      ),
                    ),
                  ),
                )),
              ),
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}


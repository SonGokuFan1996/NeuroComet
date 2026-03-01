import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback helpers that only trigger on supported platforms
void _safeHapticLight() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

void _safeHapticMedium() {
  if (!kIsWeb) HapticFeedback.mediumImpact();
}

void _safeHapticHeavy() {
  if (!kIsWeb) HapticFeedback.heavyImpact();
}

void _safeHapticSelection() {
  if (!kIsWeb) HapticFeedback.selectionClick();
}

/// Pattern Tap Game - Memory pattern game
class PatternTapGame extends ConsumerStatefulWidget {
  const PatternTapGame({super.key});

  @override
  ConsumerState<PatternTapGame> createState() => _PatternTapGameState();
}

class _PatternTapGameState extends ConsumerState<PatternTapGame> {
  static const int _gridSize = 3;
  final Random _random = Random();

  List<int> _pattern = [];
  List<int> _userInput = [];
  int _level = 1;
  int _highlightedIndex = -1;
  bool _isShowingPattern = false;
  bool _isGameOver = false;
  bool _isWaiting = false;
  int _bestLevel = 1;

  final List<Color> _tileColors = [
    AppColors.categoryADHD,
    AppColors.categoryAutism,
    AppColors.categoryDyslexia,
    AppColors.categoryAnxiety,
    AppColors.categoryDepression,
    AppColors.categoryOCD,
    AppColors.categoryBipolar,
    AppColors.secondaryTeal,
    AppColors.primaryPurple,
  ];

  @override
  void initState() {
    super.initState();
    _startNewGame();
  }

  void _startNewGame() {
    setState(() {
      _pattern = [];
      _userInput = [];
      _level = 1;
      _isGameOver = false;
      _highlightedIndex = -1;
    });
    _addToPattern();
  }

  void _addToPattern() {
    setState(() {
      _pattern.add(_random.nextInt(_gridSize * _gridSize));
      _userInput = [];
    });
    _showPattern();
  }

  Future<void> _showPattern() async {
    setState(() => _isShowingPattern = true);

    await Future.delayed(const Duration(milliseconds: 500));

    for (int i = 0; i < _pattern.length; i++) {
      setState(() => _highlightedIndex = _pattern[i]);
      _safeHapticLight();
      await Future.delayed(const Duration(milliseconds: 600));
      setState(() => _highlightedIndex = -1);
      await Future.delayed(const Duration(milliseconds: 200));
    }

    setState(() {
      _isShowingPattern = false;
      _isWaiting = true;
    });
  }

  void _onTileTap(int index) {
    if (_isShowingPattern || _isGameOver || !_isWaiting) return;

    _safeHapticSelection();

    setState(() {
      _highlightedIndex = index;
      _userInput.add(index);
    });

    Future.delayed(const Duration(milliseconds: 200), () {
      if (mounted) {
        setState(() => _highlightedIndex = -1);
      }
    });

    // Check if correct
    final currentIndex = _userInput.length - 1;
    if (_userInput[currentIndex] != _pattern[currentIndex]) {
      // Wrong!
      _safeHapticHeavy();
      setState(() {
        _isGameOver = true;
        _isWaiting = false;
        if (_level > _bestLevel) {
          _bestLevel = _level;
        }
      });
      return;
    }

    // Check if pattern complete
    if (_userInput.length == _pattern.length) {
      // Correct!
      _safeHapticMedium();
      setState(() {
        _level++;
        _isWaiting = false;
      });

      Future.delayed(const Duration(milliseconds: 500), () {
        if (mounted && !_isGameOver) {
          _addToPattern();
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.get('gamePatternTap')),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _startNewGame,
            tooltip: l10n.get('restart'),
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () => _showTutorial(context),
          ),
        ],
      ),
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              theme.colorScheme.surface,
              AppColors.calmLavender.withAlpha(30),
            ],
          ),
        ),
        child: Column(
          children: [
            const SizedBox(height: 24),

            // Level and status
            _buildHeader(context),

            const SizedBox(height: 24),

            // Grid
            Expanded(
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: AspectRatio(
                    aspectRatio: 1,
                    child: GridView.builder(
                      physics: const NeverScrollableScrollPhysics(),
                      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: _gridSize,
                        crossAxisSpacing: 8,
                        mainAxisSpacing: 8,
                      ),
                      itemCount: _gridSize * _gridSize,
                      itemBuilder: (context, index) => _buildTile(index),
                    ),
                  ),
                ),
              ),
            ),

            // Status message
            Padding(
              padding: const EdgeInsets.all(24),
              child: _buildStatusMessage(context),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          Column(
            children: [
              Text(
                l10n.get('level'),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                '$_level',
                style: theme.textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.primaryPurple,
                ),
              ),
            ],
          ),
          Column(
            children: [
              Text(
                l10n.get('pattern'),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                '${_pattern.length}',
                style: theme.textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          Column(
            children: [
              Text(
                l10n.get('best'),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              Text(
                '$_bestLevel',
                style: theme.textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppColors.success,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTile(int index) {
    final isHighlighted = _highlightedIndex == index;
    final color = _tileColors[index % _tileColors.length];

    return GestureDetector(
      onTap: () => _onTileTap(index),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 150),
        decoration: BoxDecoration(
          color: isHighlighted ? color : color.withAlpha(80),
          borderRadius: BorderRadius.circular(16),
          boxShadow: isHighlighted
              ? [
                  BoxShadow(
                    color: color.withAlpha(150),
                    blurRadius: 20,
                    spreadRadius: 2,
                  ),
                ]
              : null,
        ),
        child: isHighlighted
            ? Center(
                child: Icon(
                  Icons.touch_app,
                  color: Colors.white,
                  size: 32,
                ),
              )
            : null,
      ),
    );
  }

  Widget _buildStatusMessage(BuildContext context) {
    final theme = Theme.of(context);

    String message;
    Color color;

    if (_isGameOver) {
      message = 'Game Over! Tap restart to try again 💚';
      color = AppColors.error;
    } else if (_isShowingPattern) {
      message = 'Watch the pattern... 👀';
      color = AppColors.info;
    } else if (_isWaiting) {
      message = 'Your turn! Tap the pattern (${_userInput.length}/${_pattern.length})';
      color = AppColors.success;
    } else {
      message = 'Get ready...';
      color = theme.colorScheme.onSurfaceVariant;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
      decoration: BoxDecoration(
        color: color.withAlpha(30),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        message,
        style: theme.textTheme.bodyLarge?.copyWith(
          color: color,
          fontWeight: FontWeight.w500,
        ),
        textAlign: TextAlign.center,
      ),
    );
  }

  void _showTutorial(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Text('🎯 '),
            Text('Pattern Tap'),
          ],
        ),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('• Watch the pattern light up'),
            SizedBox(height: 8),
            Text('• Tap the tiles in the same order'),
            SizedBox(height: 8),
            Text('• Each level adds one more tile'),
            SizedBox(height: 8),
            Text('• No pressure - just practice makes progress! 💚'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text(AppLocalizations.of(context)!.get('gotIt')),
          ),
        ],
      ),
    );
  }
}


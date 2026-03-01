import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../l10n/app_localizations.dart';

/// Safe haptic feedback helpers that only trigger on supported platforms
void _safeHapticLight() {
  if (!kIsWeb) HapticFeedback.lightImpact();
}

void _safeHapticMedium() {
  if (!kIsWeb) HapticFeedback.mediumImpact();
}

enum BreathingPhase { inhale, hold, exhale, rest }

class BreathingBubblesGame extends StatefulWidget {
  const BreathingBubblesGame({super.key});

  @override
  State<BreathingBubblesGame> createState() => _BreathingBubblesGameState();
}

class _BreathingBubblesGameState extends State<BreathingBubblesGame>
    with TickerProviderStateMixin {
  late AnimationController _breathController;
  late Animation<double> _scaleAnimation;

  BreathingPhase _currentPhase = BreathingPhase.inhale;
  int _cycleCount = 0;
  bool _isActive = false;

  // Breathing pattern: 4-7-8 technique
  static const int _inhaleSeconds = 4;
  static const int _holdSeconds = 7;
  static const int _exhaleSeconds = 8;
  static const int _restSeconds = 2;

  @override
  void initState() {
    super.initState();
    _setupAnimation();
  }

  void _setupAnimation() {
    _breathController = AnimationController(
      vsync: this,
      duration: Duration(seconds: _inhaleSeconds),
    );

    _scaleAnimation = Tween<double>(begin: 0.5, end: 1.0).animate(
      CurvedAnimation(parent: _breathController, curve: Curves.easeInOut),
    );

    _breathController.addStatusListener(_onAnimationStatus);
  }

  void _onAnimationStatus(AnimationStatus status) {
    if (!_isActive) return;

    if (status == AnimationStatus.completed || status == AnimationStatus.dismissed) {
      _nextPhase();
    }
  }

  void _nextPhase() {
    switch (_currentPhase) {
      case BreathingPhase.inhale:
        setState(() => _currentPhase = BreathingPhase.hold);
        _breathController.duration = Duration(seconds: _holdSeconds);
        _breathController.forward(from: 0);
        break;
      case BreathingPhase.hold:
        setState(() => _currentPhase = BreathingPhase.exhale);
        _breathController.duration = Duration(seconds: _exhaleSeconds);
        _breathController.reverse(from: 1);
        break;
      case BreathingPhase.exhale:
        setState(() {
          _currentPhase = BreathingPhase.rest;
          _cycleCount++;
        });
        _safeHapticLight();
        _breathController.duration = Duration(seconds: _restSeconds);
        _breathController.forward(from: 0);
        break;
      case BreathingPhase.rest:
        setState(() => _currentPhase = BreathingPhase.inhale);
        _breathController.duration = Duration(seconds: _inhaleSeconds);
        _breathController.forward(from: 0);
        break;
    }
  }

  void _toggleBreathing() {
    _safeHapticMedium();
    setState(() {
      _isActive = !_isActive;
      if (_isActive) {
        _currentPhase = BreathingPhase.inhale;
        _breathController.duration = Duration(seconds: _inhaleSeconds);
        _breathController.forward(from: 0);
      } else {
        _breathController.stop();
        _breathController.value = 0.5;
      }
    });
  }

  @override
  void dispose() {
    _breathController.dispose();
    super.dispose();
  }

  String _getPhaseText() {
    switch (_currentPhase) {
      case BreathingPhase.inhale:
        return 'Breathe In';
      case BreathingPhase.hold:
        return 'Hold';
      case BreathingPhase.exhale:
        return 'Breathe Out';
      case BreathingPhase.rest:
        return 'Rest';
    }
  }

  Color _getPhaseColor() {
    switch (_currentPhase) {
      case BreathingPhase.inhale:
        return Colors.cyan;
      case BreathingPhase.hold:
        return Colors.purple;
      case BreathingPhase.exhale:
        return Colors.teal;
      case BreathingPhase.rest:
        return Colors.indigo;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final l10n = AppLocalizations.of(context)!;
    final size = MediaQuery.of(context).size;
    final bubbleMaxSize = size.width * 0.7;

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: AppBar(
        title: Text(l10n.gameBreathingBubbles),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _StatCard(
                  label: l10n.get('cycles'),
                  value: _cycleCount.toString(),
                  icon: Icons.loop,
                ),
                const SizedBox(width: 16),
                _StatCard(
                  label: l10n.get('pattern'),
                  value: '$_inhaleSeconds-$_holdSeconds-$_exhaleSeconds',
                  icon: Icons.air,
                ),
              ],
            ),
          ),
          Expanded(
            child: Center(
              child: GestureDetector(
                onTap: _toggleBreathing,
                child: AnimatedBuilder(
                  animation: _scaleAnimation,
                  builder: (context, child) {
                    final scale = _isActive ? _scaleAnimation.value : 0.5;
                    final color = _getPhaseColor();

                    return Container(
                      width: bubbleMaxSize * scale,
                      height: bubbleMaxSize * scale,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: RadialGradient(
                          colors: [
                            color.withOpacity(0.4),
                            color.withOpacity(0.6),
                            color.withOpacity(0.8),
                          ],
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: color.withOpacity(0.3),
                            blurRadius: 30,
                            spreadRadius: 10,
                          ),
                        ],
                      ),
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            AnimatedSwitcher(
                              duration: const Duration(milliseconds: 300),
                              child: Text(
                                _isActive ? _getPhaseText() : 'Tap to Start',
                                key: ValueKey(_getPhaseText()),
                                style: theme.textTheme.headlineSmall?.copyWith(
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            if (_isActive) ...[
                              const SizedBox(height: 8),
                              Icon(
                                _currentPhase == BreathingPhase.inhale
                                    ? Icons.arrow_upward
                                    : _currentPhase == BreathingPhase.exhale
                                        ? Icons.arrow_downward
                                        : Icons.pause,
                                color: Colors.white.withOpacity(0.8),
                                size: 32,
                              ),
                            ],
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                Text(
                  _isActive ? 'Tap the bubble to pause' : 'Tap the bubble to begin',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.outline,
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    _PhaseIndicator(
                      phase: 'In',
                      seconds: _inhaleSeconds,
                      isActive: _isActive && _currentPhase == BreathingPhase.inhale,
                    ),
                    _PhaseIndicator(
                      phase: 'Hold',
                      seconds: _holdSeconds,
                      isActive: _isActive && _currentPhase == BreathingPhase.hold,
                    ),
                    _PhaseIndicator(
                      phase: 'Out',
                      seconds: _exhaleSeconds,
                      isActive: _isActive && _currentPhase == BreathingPhase.exhale,
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;

  const _StatCard({
    required this.label,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Icon(icon, size: 20, color: theme.colorScheme.primary),
          const SizedBox(width: 8),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.outline,
                ),
              ),
              Text(
                value,
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _PhaseIndicator extends StatelessWidget {
  final String phase;
  final int seconds;
  final bool isActive;

  const _PhaseIndicator({
    required this.phase,
    required this.seconds,
    required this.isActive,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: isActive
            ? theme.colorScheme.primary
            : theme.colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        children: [
          Text(
            phase,
            style: theme.textTheme.labelMedium?.copyWith(
              color: isActive
                  ? theme.colorScheme.onPrimary
                  : theme.colorScheme.onSurfaceVariant,
              fontWeight: FontWeight.bold,
            ),
          ),
          Text(
            '${seconds}s',
            style: theme.textTheme.bodySmall?.copyWith(
              color: isActive
                  ? theme.colorScheme.onPrimary.withOpacity(0.8)
                  : theme.colorScheme.outline,
            ),
          ),
        ],
      ),
    );
  }
}


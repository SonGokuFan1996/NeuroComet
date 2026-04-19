import 'dart:math';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/theme/app_colors.dart';

/// Worry Jar Game - Write worries on notes and seal them in a jar
class WorryJarGame extends ConsumerStatefulWidget {
  const WorryJarGame({super.key});

  @override
  ConsumerState<WorryJarGame> createState() => _WorryJarGameState();
}

class _WorryJarGameState extends ConsumerState<WorryJarGame>
    with TickerProviderStateMixin {
  final List<_Worry> _worries = [];
  final _controller = TextEditingController();
  late AnimationController _floatController;

  final _colors = [
    const Color(0xFFFFCDD2),
    const Color(0xFFC5CAE9),
    const Color(0xFFB2DFDB),
    const Color(0xFFFFF9C4),
    const Color(0xFFE1BEE7),
    const Color(0xFFFFE0B2),
    const Color(0xFFB3E5FC),
    const Color(0xFFDCEDC8),
  ];

  @override
  void initState() {
    super.initState();
    _floatController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _controller.dispose();
    _floatController.dispose();
    super.dispose();
  }

  void _addWorry() {
    if (_controller.text.trim().isEmpty) return;
    if (!kIsWeb) HapticFeedback.mediumImpact();
    setState(() {
      _worries.add(_Worry(
        text: _controller.text.trim(),
        color: _colors[Random().nextInt(_colors.length)],
        addedAt: DateTime.now(),
      ));
      _controller.clear();
    });
  }

  void _releaseWorry(int index) {
    if (!kIsWeb) HapticFeedback.lightImpact();
    setState(() => _worries.removeAt(index));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Worry released 🍃 Let it go...'),
        behavior: SnackBarBehavior.floating,
        duration: Duration(seconds: 2),
      ),
    );
  }

  void _releaseAll() {
    if (_worries.isEmpty) return;
    if (!kIsWeb) HapticFeedback.heavyImpact();
    setState(() => _worries.clear());
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('All worries released ✨ Fresh start!'),
        behavior: SnackBarBehavior.floating,
        duration: Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Worry Jar'),
        actions: [
          if (_worries.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.air),
              onPressed: _releaseAll,
              tooltip: 'Release all worries',
            ),
        ],
      ),
      body: Column(
        children: [
          // Info card
          Container(
            margin: const EdgeInsets.all(16),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.primaryPurple.withAlpha(20),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Row(
              children: [
                Text('🫙', style: TextStyle(fontSize: 28)),
                SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Write your worries and seal them in the jar. When you\'re ready, release them.',
                    style: TextStyle(fontSize: 13),
                  ),
                ),
              ],
            ),
          ),
          // Jar contents
          Expanded(
            child: _worries.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.sentiment_satisfied_alt, size: 64, color: theme.colorScheme.onSurfaceVariant.withAlpha(80)),
                        const SizedBox(height: 12),
                        Text('Your jar is empty!', style: TextStyle(fontSize: 16, color: theme.colorScheme.onSurfaceVariant)),
                        const SizedBox(height: 4),
                        Text('That\'s a good thing 💜', style: TextStyle(fontSize: 13, color: theme.colorScheme.onSurfaceVariant.withAlpha(150))),
                      ],
                    ),
                  )
                : AnimatedBuilder(
                    animation: _floatController,
                    builder: (context, _) => ListView.builder(
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      itemCount: _worries.length,
                      itemBuilder: (context, index) {
                        final worry = _worries[index];
                        final offset = sin((_floatController.value + index * 0.3) * pi * 2) * 4;
                        return Transform.translate(
                          offset: Offset(0, offset),
                          child: Dismissible(
                            key: ValueKey(worry.addedAt),
                            direction: DismissDirection.endToStart,
                            onDismissed: (_) => _releaseWorry(index),
                            background: Container(
                              alignment: Alignment.centerRight,
                              padding: const EdgeInsets.only(right: 20),
                              child: const Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text('Release', style: TextStyle(color: Colors.grey)),
                                  SizedBox(width: 8),
                                  Icon(Icons.air, color: Colors.grey),
                                ],
                              ),
                            ),
                            child: Card(
                              color: worry.color,
                              margin: const EdgeInsets.symmetric(vertical: 6),
                              child: Padding(
                                padding: const EdgeInsets.all(16),
                                child: Row(
                                  children: [
                                    Expanded(
                                      child: Text(
                                        worry.text,
                                        style: const TextStyle(fontSize: 14, color: Colors.black87),
                                      ),
                                    ),
                                    Text(
                                      _formatTime(worry.addedAt),
                                      style: const TextStyle(fontSize: 10, color: Colors.black45),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                  ),
          ),
          // Input area
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              boxShadow: [BoxShadow(color: Colors.black.withAlpha(20), blurRadius: 8, offset: const Offset(0, -2))],
            ),
            child: SafeArea(
              top: false,
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _controller,
                      decoration: InputDecoration(
                        hintText: 'What\'s worrying you?',
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(24)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      ),
                      onSubmitted: (_) => _addWorry(),
                      textInputAction: TextInputAction.send,
                    ),
                  ),
                  const SizedBox(width: 8),
                  FloatingActionButton.small(
                    onPressed: _addWorry,
                    child: const Icon(Icons.add),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _formatTime(DateTime dt) {
    final diff = DateTime.now().difference(dt);
    if (diff.inMinutes < 1) return 'now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m';
    return '${diff.inHours}h';
  }
}

class _Worry {
  final String text;
  final Color color;
  final DateTime addedAt;

  const _Worry({required this.text, required this.color, required this.addedAt});
}


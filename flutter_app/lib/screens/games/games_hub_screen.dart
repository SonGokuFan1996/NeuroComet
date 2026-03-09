import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../l10n/app_localizations.dart';
import '../../services/game_tutorial_service.dart';

/// Safe haptic feedback that only triggers on supported platforms
void _safeHapticFeedback() {
  if (!kIsWeb) {
    HapticFeedback.mediumImpact();
  }
}

class GamesHubScreen extends ConsumerWidget {
  const GamesHubScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context)!;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.gamesHubTitle),
        actions: [
          IconButton(
            icon: const Icon(Icons.emoji_events),
            onPressed: () => _showAchievements(context),
            tooltip: l10n.gamesAchievements,
          ),
        ],
      ),
      body: LayoutBuilder(
        builder: (context, constraints) {
          // Calculate responsive column count based on screen width
          final crossAxisCount = _getResponsiveCrossAxisCount(constraints.maxWidth);
          final padding = constraints.maxWidth > 600 ? 24.0 : 16.0;

          return SingleChildScrollView(
            padding: EdgeInsets.all(padding),
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 1200),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildSectionHeader(context, l10n.gamesUnlocked),
                    const SizedBox(height: 12),
                    _buildGameGrid(context, _getUnlockedGames(l10n), crossAxisCount: crossAxisCount),
                    const SizedBox(height: 24),
                    _buildSectionHeader(context, l10n.gamesLocked),
                    const SizedBox(height: 12),
                    _buildGameGrid(context, _getLockedGames(l10n), locked: true, crossAxisCount: crossAxisCount),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  int _getResponsiveCrossAxisCount(double width) {
    if (width >= 1200) return 4;
    if (width >= 900) return 3;
    if (width >= 600) return 2;
    return 2;
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Text(
      title,
      style: Theme.of(context).textTheme.titleMedium?.copyWith(
        fontWeight: FontWeight.bold,
      ),
    );
  }

  Widget _buildGameGrid(
    BuildContext context,
    List<_GameInfo> games, {
    bool locked = false,
    int crossAxisCount = 2,
  }) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: crossAxisCount,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
        childAspectRatio: 0.85,
      ),
      itemCount: games.length,
      itemBuilder: (context, index) {
        return _GameCard(
          game: games[index],
          locked: locked,
          onTap: locked ? null : () => _openGame(context, games[index]),
        );
      },
    );
  }

  List<_GameInfo> _getUnlockedGames(AppLocalizations l10n) {
    return [
      _GameInfo(
        id: 'bubble_pop',
        name: l10n.gameBubblePop,
        description: l10n.gameBubblePopDesc,
        icon: Icons.bubble_chart,
        color: Colors.pink,
      ),
      _GameInfo(
        id: 'fidget_spinner',
        name: l10n.gameFidgetSpinner,
        description: l10n.gameFidgetSpinnerDesc,
        icon: Icons.rotate_right,
        color: Colors.blue,
      ),
      _GameInfo(
        id: 'color_flow',
        name: l10n.gameColorFlow,
        description: l10n.gameColorFlowDesc,
        icon: Icons.gradient,
        color: Colors.purple,
      ),
      _GameInfo(
        id: 'breathing_bubbles',
        name: l10n.gameBreathingBubbles,
        description: l10n.gameBreathingBubblesDesc,
        icon: Icons.air,
        color: Colors.teal,
      ),
    ];
  }

  List<_GameInfo> _getLockedGames(AppLocalizations l10n) {
    return [
      _GameInfo(
        id: 'infinity_draw',
        name: l10n.gameInfinityDraw,
        description: l10n.gameInfinityDrawDesc,
        icon: Icons.gesture,
        color: Colors.orange,
      ),
      _GameInfo(
        id: 'sensory_rain',
        name: l10n.gameSensoryRain,
        description: l10n.gameSensoryRainDesc,
        icon: Icons.water_drop,
        color: Colors.cyan,
      ),
      _GameInfo(
        id: 'zen_sand',
        name: l10n.gameZenSand,
        description: l10n.gameZenSandDesc,
        icon: Icons.landscape,
        color: Colors.amber,
      ),
      _GameInfo(
        id: 'emotion_garden',
        name: l10n.gameEmotionGarden,
        description: l10n.gameEmotionGardenDesc,
        icon: Icons.local_florist,
        color: Colors.green,
      ),
    ];
  }

  void _openGame(BuildContext context, _GameInfo game) async {
    _safeHapticFeedback();
    // Show tutorial if not seen before
    await showGameTutorialIfNeeded(context, game.id);
    // Then navigate to the game
    if (context.mounted) {
      context.push('/games/${game.id}');
    }
  }

  void _showAchievements(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => const _AchievementsSheet(),
    );
  }
}

class _GameInfo {
  final String id;
  final String name;
  final String description;
  final IconData icon;
  final Color color;

  _GameInfo({
    required this.id,
    required this.name,
    required this.description,
    required this.icon,
    required this.color,
  });
}

class _GameCard extends StatefulWidget {
  final _GameInfo game;
  final bool locked;
  final VoidCallback? onTap;

  const _GameCard({
    required this.game,
    this.locked = false,
    this.onTap,
  });

  @override
  State<_GameCard> createState() => _GameCardState();
}

class _GameCardState extends State<_GameCard> with SingleTickerProviderStateMixin {
  bool _isHovered = false;
  late AnimationController _animationController;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 150),
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.97).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _handleTapDown(TapDownDetails details) {
    if (!widget.locked) {
      _animationController.forward();
    }
  }

  void _handleTapUp(TapUpDetails details) {
    _animationController.reverse();
    if (!widget.locked && widget.onTap != null) {
      _safeHapticFeedback();
      widget.onTap!();
    }
  }

  void _handleTapCancel() {
    _animationController.reverse();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isInteractive = !widget.locked && widget.onTap != null;

    return MouseRegion(
      cursor: isInteractive ? SystemMouseCursors.click : SystemMouseCursors.basic,
      onEnter: (_) {
        if (isInteractive) setState(() => _isHovered = true);
      },
      onExit: (_) {
        if (isInteractive) setState(() => _isHovered = false);
      },
      child: Focus(
        canRequestFocus: isInteractive,
        child: GestureDetector(
          onTapDown: isInteractive ? _handleTapDown : null,
          onTapUp: isInteractive ? _handleTapUp : null,
          onTapCancel: isInteractive ? _handleTapCancel : null,
          child: AnimatedBuilder(
            animation: _scaleAnimation,
            builder: (context, child) => Transform.scale(
              scale: _scaleAnimation.value,
              child: child,
            ),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              curve: Curves.easeOut,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                gradient: widget.locked
                    ? null
                    : LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [
                          widget.game.color.withValues(alpha: _isHovered ? 0.9 : 0.8),
                          widget.game.color,
                        ],
                      ),
                color: widget.locked ? theme.colorScheme.surfaceContainerHighest : null,
                boxShadow: _isHovered && !widget.locked
                    ? [
                        BoxShadow(
                          color: widget.game.color.withValues(alpha: 0.4),
                          blurRadius: 16,
                          offset: const Offset(0, 8),
                        ),
                      ]
                    : [
                        BoxShadow(
                          color: Colors.black.withValues(alpha: 0.1),
                          blurRadius: 8,
                          offset: const Offset(0, 4),
                        ),
                      ],
              ),
              transform: _isHovered && !widget.locked
                  ? (Matrix4.identity()..translate(0.0, -4.0))
                  : Matrix4.identity(),
              child: Material(
                color: Colors.transparent,
                child: Stack(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: widget.locked
                                  ? theme.colorScheme.outline.withValues(alpha: 0.2)
                                  : Colors.white.withValues(alpha: _isHovered ? 0.3 : 0.2),
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              widget.game.icon,
                              size: 32,
                              color: widget.locked ? theme.colorScheme.outline : Colors.white,
                            ),
                          ),
                          const Spacer(),
                          Text(
                            widget.game.name,
                            style: theme.textTheme.titleSmall?.copyWith(
                              color: widget.locked ? theme.colorScheme.outline : Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            widget.game.description,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: widget.locked
                                  ? theme.colorScheme.outline.withValues(alpha: 0.7)
                                  : Colors.white.withValues(alpha: 0.8),
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                    if (widget.locked)
                      Positioned(
                        top: 12,
                        right: 12,
                        child: Container(
                          padding: const EdgeInsets.all(6),
                          decoration: BoxDecoration(
                            color: theme.colorScheme.outline.withValues(alpha: 0.2),
                            shape: BoxShape.circle,
                          ),
                          child: Icon(
                            Icons.lock,
                            size: 16,
                            color: theme.colorScheme.outline,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _AchievementsSheet extends StatelessWidget {
  const _AchievementsSheet();

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: theme.scaffoldBackgroundColor,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.symmetric(vertical: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.dividerColor,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  children: [
                    Icon(Icons.emoji_events, color: Colors.amber),
                    const SizedBox(width: 8),
                    Text(
                      l10n.gamesYourAchievements,
                      style: theme.textTheme.titleLarge,
                    ),
                  ],
                ),
              ),
              const Divider(height: 24),
              Expanded(
                child: ListView(
                  controller: scrollController,
                  padding: const EdgeInsets.all(16),
                  children: [
                    _AchievementTile(
                      icon: Icons.bubble_chart,
                      title: 'Bubble Master',
                      description: 'Pop 1000 bubbles',
                      progress: 0.75,
                      isUnlocked: false,
                    ),
                    _AchievementTile(
                      icon: Icons.rotate_right,
                      title: 'Spinner Pro',
                      description: 'Spin for 10 minutes total',
                      progress: 1.0,
                      isUnlocked: true,
                    ),
                    _AchievementTile(
                      icon: Icons.air,
                      title: 'Zen Breather',
                      description: 'Complete 50 breathing sessions',
                      progress: 0.4,
                      isUnlocked: false,
                    ),
                    _AchievementTile(
                      icon: Icons.gradient,
                      title: 'Color Explorer',
                      description: 'Discover all color combinations',
                      progress: 0.2,
                      isUnlocked: false,
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _AchievementTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final double progress;
  final bool isUnlocked;

  const _AchievementTile({
    required this.icon,
    required this.title,
    required this.description,
    required this.progress,
    required this.isUnlocked,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: isUnlocked
                    ? Colors.amber.withValues(alpha: 0.2)
                    : theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                icon,
                size: 28,
                color: isUnlocked ? Colors.amber : theme.colorScheme.outline,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          title,
                          style: theme.textTheme.titleSmall?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      if (isUnlocked)
                        Icon(Icons.check_circle, color: Colors.green, size: 20),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.outline,
                    ),
                  ),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(
                    value: progress,
                    backgroundColor: theme.colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}


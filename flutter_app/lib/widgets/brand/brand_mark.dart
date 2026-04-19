import 'package:flutter/material.dart';
import '../common/neuro_comet_logo.dart';

class NeuroCometBrandMark extends StatefulWidget {
  final double size;
  final Color haloColor;
  final Color accentColor;
  final bool motionEnabled;

  const NeuroCometBrandMark({
    super.key,
    this.size = 100,
    required this.haloColor,
    required this.accentColor,
    this.motionEnabled = true,
  });

  @override
  State<NeuroCometBrandMark> createState() => _NeuroCometBrandMarkState();
}

class _NeuroCometBrandMarkState extends State<NeuroCometBrandMark>
    with SingleTickerProviderStateMixin {
  late AnimationController _glowController;
  late Animation<double> _glowScale;

  @override
  void initState() {
    super.initState();
    _glowController = AnimationController(
      duration: const Duration(milliseconds: 3200),
      vsync: this,
    );

    _glowScale = Tween<double>(begin: 0.96, end: 1.04).animate(
      CurvedAnimation(parent: _glowController, curve: Curves.easeInOut),
    );

    if (widget.motionEnabled) {
      _glowController.repeat(reverse: true);
    }
  }

  @override
  void dispose() {
    _glowController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: widget.size,
      height: widget.size,
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Background Glow
          AnimatedBuilder(
            animation: _glowScale,
            builder: (context, child) {
              return Transform.scale(
                scale: widget.motionEnabled ? _glowScale.value : 1.0,
                child: Container(
                  width: widget.size,
                  height: widget.size,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: RadialGradient(
                      colors: [
                        widget.haloColor.withValues(alpha: 0.34),
                        widget.accentColor.withValues(alpha: 0.22),
                        Colors.transparent,
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
          // Surface with shadow
          Container(
            width: widget.size - 10,
            height: widget.size - 10,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Theme.of(context).colorScheme.surface,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Center(
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: NeuroCometLogo(
                  size: widget.size * 0.6,
                  animated: widget.motionEnabled,
                  showGlow: false,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

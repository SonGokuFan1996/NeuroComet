import 'dart:async';
import 'package:flutter/material.dart';

/// A widget that animates text appearing character-by-character (typewriter effect).
/// Mirrors the Kotlin TypewriterText.kt
///
/// Features:
/// - Configurable delay between characters
/// - Optional cursor blink
/// - Callback when animation completes
/// - Supports custom text styles
class TypewriterText extends StatefulWidget {
  /// The full text to animate
  final String text;

  /// Text style to apply
  final TextStyle? style;

  /// Text color
  final Color? color;

  /// Font weight
  final FontWeight? fontWeight;

  /// Delay in milliseconds between each character appearing
  final Duration delayPerCharacter;

  /// Delay before animation starts
  final Duration initialDelay;

  /// Whether to show a blinking cursor at the end
  final bool showCursor;

  /// The character to use as cursor
  final String cursorChar;

  /// Callback when full text has been displayed
  final VoidCallback? onAnimationComplete;

  const TypewriterText({
    super.key,
    required this.text,
    this.style,
    this.color,
    this.fontWeight,
    this.delayPerCharacter = const Duration(milliseconds: 50),
    this.initialDelay = Duration.zero,
    this.showCursor = false,
    this.cursorChar = '▌',
    this.onAnimationComplete,
  });

  @override
  State<TypewriterText> createState() => _TypewriterTextState();
}

class _TypewriterTextState extends State<TypewriterText> {
  int _displayedCharCount = 0;
  bool _showCursorState = true;
  Timer? _typeTimer;
  Timer? _cursorTimer;

  @override
  void initState() {
    super.initState();
    _startAnimation();
  }

  @override
  void didUpdateWidget(TypewriterText oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.text != widget.text) {
      _typeTimer?.cancel();
      _cursorTimer?.cancel();
      _displayedCharCount = 0;
      _startAnimation();
    }
  }

  void _startAnimation() {
    Future.delayed(widget.initialDelay, () {
      if (!mounted) return;
      int index = 0;
      _typeTimer = Timer.periodic(widget.delayPerCharacter, (timer) {
        if (!mounted) {
          timer.cancel();
          return;
        }
        if (index < widget.text.length) {
          setState(() {
            _displayedCharCount = index + 1;
          });
          index++;
        } else {
          timer.cancel();
          widget.onAnimationComplete?.call();
        }
      });

      // Cursor blink
      if (widget.showCursor) {
        _cursorTimer = Timer.periodic(const Duration(milliseconds: 500), (_) {
          if (!mounted) return;
          setState(() {
            _showCursorState = !_showCursorState;
          });
        });
      }
    });
  }

  @override
  void dispose() {
    _typeTimer?.cancel();
    _cursorTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final displayText = StringBuffer(widget.text.substring(0, _displayedCharCount));
    if (widget.showCursor &&
        _displayedCharCount < widget.text.length &&
        _showCursorState) {
      displayText.write(widget.cursorChar);
    }

    return Text(
      displayText.toString(),
      style: (widget.style ?? const TextStyle()).copyWith(
        color: widget.color,
        fontWeight: widget.fontWeight,
      ),
    );
  }
}

/// A typewriter text that fades in each character with a smooth animation.
/// More visually pleasing for accessibility-focused apps.
class SmoothTypewriterText extends StatefulWidget {
  final String text;
  final TextStyle? style;
  final Duration delayPerCharacter;
  final Duration fadeInDuration;
  final Duration initialDelay;
  final VoidCallback? onAnimationComplete;

  const SmoothTypewriterText({
    super.key,
    required this.text,
    this.style,
    this.delayPerCharacter = const Duration(milliseconds: 40),
    this.fadeInDuration = const Duration(milliseconds: 300),
    this.initialDelay = Duration.zero,
    this.onAnimationComplete,
  });

  @override
  State<SmoothTypewriterText> createState() => _SmoothTypewriterTextState();
}

class _SmoothTypewriterTextState extends State<SmoothTypewriterText>
    with TickerProviderStateMixin {
  final List<AnimationController> _controllers = [];
  final List<Animation<double>> _animations = [];
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _initAnimations();
  }

  void _initAnimations() {
    for (int i = 0; i < widget.text.length; i++) {
      final controller = AnimationController(
        duration: widget.fadeInDuration,
        vsync: this,
      );
      _controllers.add(controller);
      _animations.add(CurvedAnimation(
        parent: controller,
        curve: Curves.easeOut,
      ));
    }

    Future.delayed(widget.initialDelay, () {
      if (!mounted) return;
      int index = 0;
      _timer = Timer.periodic(widget.delayPerCharacter, (timer) {
        if (!mounted) {
          timer.cancel();
          return;
        }
        if (index < _controllers.length) {
          _controllers[index].forward();
          index++;
        } else {
          timer.cancel();
          widget.onAnimationComplete?.call();
        }
      });
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    for (final c in _controllers) {
      c.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return RichText(
      text: TextSpan(
        children: List.generate(widget.text.length, (index) {
          return WidgetSpan(
            child: FadeTransition(
              opacity: index < _animations.length
                  ? _animations[index]
                  : const AlwaysStoppedAnimation(0.0),
              child: Text(
                widget.text[index],
                style: widget.style,
              ),
            ),
          );
        }),
      ),
    );
  }
}


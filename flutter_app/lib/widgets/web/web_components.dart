import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// A card widget optimized for web with hover effects and smooth animations
class WebCard extends StatefulWidget {
  final Widget child;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final double borderRadius;
  final Color? backgroundColor;
  final Color? hoverColor;
  final double elevation;
  final double hoverElevation;
  final bool enableHover;
  final Border? border;
  final Gradient? gradient;

  const WebCard({
    super.key,
    required this.child,
    this.onTap,
    this.padding,
    this.margin,
    this.borderRadius = 16,
    this.backgroundColor,
    this.hoverColor,
    this.elevation = 0,
    this.hoverElevation = 4,
    this.enableHover = true,
    this.border,
    this.gradient,
  });

  /// Creates a WebCard with elevated style
  factory WebCard.elevated({
    Key? key,
    required Widget child,
    VoidCallback? onTap,
    EdgeInsetsGeometry? padding,
    EdgeInsetsGeometry? margin,
    double borderRadius = 16,
  }) {
    return WebCard(
      key: key,
      onTap: onTap,
      padding: padding,
      margin: margin,
      borderRadius: borderRadius,
      elevation: 2,
      hoverElevation: 6,
      child: child,
    );
  }

  /// Creates a WebCard with outlined style
  factory WebCard.outlined({
    Key? key,
    required Widget child,
    VoidCallback? onTap,
    EdgeInsetsGeometry? padding,
    EdgeInsetsGeometry? margin,
    double borderRadius = 16,
    Color? borderColor,
  }) {
    return WebCard(
      key: key,
      onTap: onTap,
      padding: padding,
      margin: margin,
      borderRadius: borderRadius,
      elevation: 0,
      hoverElevation: 2,
      border: Border.all(
        color: borderColor ?? Colors.grey.withOpacity(0.3),
        width: 1,
      ),
      child: child,
    );
  }

  /// Creates a WebCard with filled style
  factory WebCard.filled({
    Key? key,
    required Widget child,
    VoidCallback? onTap,
    EdgeInsetsGeometry? padding,
    EdgeInsetsGeometry? margin,
    double borderRadius = 16,
    required Color color,
  }) {
    return WebCard(
      key: key,
      onTap: onTap,
      padding: padding,
      margin: margin,
      borderRadius: borderRadius,
      backgroundColor: color,
      elevation: 0,
      hoverElevation: 2,
      child: child,
    );
  }

  @override
  State<WebCard> createState() => _WebCardState();
}

class _WebCardState extends State<WebCard> with SingleTickerProviderStateMixin {
  bool _isHovered = false;
  late AnimationController _animationController;
  late Animation<double> _elevationAnimation;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 150),
      vsync: this,
    );
    _elevationAnimation = Tween<double>(
      begin: widget.elevation,
      end: widget.hoverElevation,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOut,
    ));
    _scaleAnimation = Tween<double>(
      begin: 1.0,
      end: 1.01,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeOut,
    ));
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _onHoverEnter() {
    if (widget.enableHover && kIsWeb) {
      setState(() => _isHovered = true);
      _animationController.forward();
    }
  }

  void _onHoverExit() {
    if (widget.enableHover && kIsWeb) {
      setState(() => _isHovered = false);
      _animationController.reverse();
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final backgroundColor =
        widget.backgroundColor ?? theme.colorScheme.surface;
    final hoverColor =
        widget.hoverColor ?? theme.colorScheme.surfaceContainerHighest;

    return Padding(
      padding: widget.margin ?? EdgeInsets.zero,
      child: MouseRegion(
        onEnter: (_) => _onHoverEnter(),
        onExit: (_) => _onHoverExit(),
        cursor: widget.onTap != null
            ? SystemMouseCursors.click
            : SystemMouseCursors.basic,
        child: GestureDetector(
          onTap: widget.onTap,
          child: AnimatedBuilder(
            animation: _animationController,
            builder: (context, child) {
              return Transform.scale(
                scale: _scaleAnimation.value,
                child: Material(
                  elevation: _elevationAnimation.value,
                  borderRadius: BorderRadius.circular(widget.borderRadius),
                  color: Colors.transparent,
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 150),
                    decoration: BoxDecoration(
                      color: widget.gradient == null
                          ? (_isHovered ? hoverColor : backgroundColor)
                          : null,
                      gradient: widget.gradient,
                      borderRadius: BorderRadius.circular(widget.borderRadius),
                      border: widget.border,
                    ),
                    padding: widget.padding,
                    child: child,
                  ),
                ),
              );
            },
            child: widget.child,
          ),
        ),
      ),
    );
  }
}

/// A clickable list tile optimized for web with hover effects
class WebListTile extends StatefulWidget {
  final Widget? leading;
  final Widget? title;
  final Widget? subtitle;
  final Widget? trailing;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry? contentPadding;
  final bool selected;

  const WebListTile({
    super.key,
    this.leading,
    this.title,
    this.subtitle,
    this.trailing,
    this.onTap,
    this.contentPadding,
    this.selected = false,
  });

  @override
  State<WebListTile> createState() => _WebListTileState();
}

class _WebListTileState extends State<WebListTile> {
  bool _isHovered = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: widget.onTap != null
          ? SystemMouseCursors.click
          : SystemMouseCursors.basic,
      child: GestureDetector(
        onTap: widget.onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 100),
          decoration: BoxDecoration(
            color: widget.selected
                ? theme.colorScheme.primaryContainer
                : _isHovered
                    ? theme.colorScheme.surfaceContainerHighest
                    : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          padding: widget.contentPadding ??
              const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              if (widget.leading != null) ...[
                widget.leading!,
                const SizedBox(width: 16),
              ],
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (widget.title != null) widget.title!,
                    if (widget.subtitle != null) ...[
                      const SizedBox(height: 4),
                      DefaultTextStyle(
                        style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ) ??
                            const TextStyle(),
                        child: widget.subtitle!,
                      ),
                    ],
                  ],
                ),
              ),
              if (widget.trailing != null) ...[
                const SizedBox(width: 16),
                widget.trailing!,
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// A button optimized for web with proper hover states
class WebButton extends StatefulWidget {
  final Widget child;
  final VoidCallback? onPressed;
  final bool isPrimary;
  final bool isOutlined;
  final bool isLoading;
  final EdgeInsetsGeometry? padding;
  final double borderRadius;
  final IconData? icon;

  const WebButton({
    super.key,
    required this.child,
    this.onPressed,
    this.isPrimary = true,
    this.isOutlined = false,
    this.isLoading = false,
    this.padding,
    this.borderRadius = 12,
    this.icon,
  });

  /// Creates a primary filled button
  factory WebButton.primary({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    bool isLoading = false,
    IconData? icon,
  }) {
    return WebButton(
      key: key,
      onPressed: onPressed,
      isPrimary: true,
      isLoading: isLoading,
      icon: icon,
      child: child,
    );
  }

  /// Creates a secondary outlined button
  factory WebButton.secondary({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    bool isLoading = false,
    IconData? icon,
  }) {
    return WebButton(
      key: key,
      onPressed: onPressed,
      isPrimary: false,
      isOutlined: true,
      isLoading: isLoading,
      icon: icon,
      child: child,
    );
  }

  /// Creates a text button
  factory WebButton.text({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    IconData? icon,
  }) {
    return WebButton(
      key: key,
      onPressed: onPressed,
      isPrimary: false,
      icon: icon,
      child: child,
    );
  }

  @override
  State<WebButton> createState() => _WebButtonState();
}

class _WebButtonState extends State<WebButton> {
  bool _isHovered = false;
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDisabled = widget.onPressed == null || widget.isLoading;

    Color backgroundColor;
    Color foregroundColor;
    Border? border;

    if (widget.isPrimary) {
      backgroundColor = isDisabled
          ? theme.colorScheme.primary.withOpacity(0.5)
          : _isPressed
              ? theme.colorScheme.primary.withOpacity(0.8)
              : _isHovered
                  ? theme.colorScheme.primary.withOpacity(0.9)
                  : theme.colorScheme.primary;
      foregroundColor = theme.colorScheme.onPrimary;
    } else if (widget.isOutlined) {
      backgroundColor = _isHovered
          ? theme.colorScheme.primary.withOpacity(0.1)
          : Colors.transparent;
      foregroundColor = theme.colorScheme.primary;
      border = Border.all(
        color: isDisabled
            ? theme.colorScheme.primary.withOpacity(0.5)
            : theme.colorScheme.primary,
        width: 1.5,
      );
    } else {
      backgroundColor = _isHovered
          ? theme.colorScheme.primary.withOpacity(0.1)
          : Colors.transparent;
      foregroundColor = theme.colorScheme.primary;
    }

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: isDisabled ? SystemMouseCursors.forbidden : SystemMouseCursors.click,
      child: GestureDetector(
        onTapDown: (_) => setState(() => _isPressed = true),
        onTapUp: (_) => setState(() => _isPressed = false),
        onTapCancel: () => setState(() => _isPressed = false),
        onTap: isDisabled ? null : widget.onPressed,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 100),
          padding: widget.padding ??
              const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          decoration: BoxDecoration(
            color: backgroundColor,
            borderRadius: BorderRadius.circular(widget.borderRadius),
            border: border,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (widget.isLoading) ...[
                SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    valueColor: AlwaysStoppedAnimation(foregroundColor),
                  ),
                ),
                const SizedBox(width: 8),
              ] else if (widget.icon != null) ...[
                Icon(widget.icon, color: foregroundColor, size: 18),
                const SizedBox(width: 8),
              ],
              DefaultTextStyle(
                style: theme.textTheme.labelLarge?.copyWith(
                      color: foregroundColor,
                      fontWeight: FontWeight.w600,
                    ) ??
                    TextStyle(color: foregroundColor),
                child: widget.child,
              ),
            ],
          ),
        ),
      ),
    );
  }
}


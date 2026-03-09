import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/theme/app_colors.dart';

/// A button with sensory feedback designed for neurodivergent users
/// Provides haptic feedback and visual confirmation
class NeuroButton extends StatefulWidget {
  final Widget child;
  final VoidCallback? onPressed;
  final bool isLoading;
  final bool isEnabled;
  final ButtonStyle? style;
  final Color? backgroundColor;
  final Color? foregroundColor;
  final EdgeInsetsGeometry? padding;
  final BorderRadius? borderRadius;
  final bool enableHapticFeedback;

  const NeuroButton({
    super.key,
    required this.child,
    this.onPressed,
    this.isLoading = false,
    this.isEnabled = true,
    this.style,
    this.backgroundColor,
    this.foregroundColor,
    this.padding,
    this.borderRadius,
    this.enableHapticFeedback = true,
  });

  /// Creates a primary filled button
  factory NeuroButton.primary({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    bool isLoading = false,
    bool isEnabled = true,
    EdgeInsetsGeometry? padding,
    bool enableHapticFeedback = true,
  }) {
    return NeuroButton(
      key: key,
      onPressed: onPressed,
      isLoading: isLoading,
      isEnabled: isEnabled,
      padding: padding,
      enableHapticFeedback: enableHapticFeedback,
      backgroundColor: AppColors.primaryPurple,
      foregroundColor: Colors.white,
      child: child,
    );
  }

  /// Creates a secondary outlined button
  factory NeuroButton.secondary({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    bool isLoading = false,
    bool isEnabled = true,
    EdgeInsetsGeometry? padding,
    bool enableHapticFeedback = true,
  }) {
    return NeuroButton(
      key: key,
      onPressed: onPressed,
      isLoading: isLoading,
      isEnabled: isEnabled,
      padding: padding,
      enableHapticFeedback: enableHapticFeedback,
      child: child,
    );
  }

  /// Creates a calm/soft button for sensitive interactions
  factory NeuroButton.calm({
    Key? key,
    required Widget child,
    VoidCallback? onPressed,
    bool isLoading = false,
    bool isEnabled = true,
    EdgeInsetsGeometry? padding,
    bool enableHapticFeedback = true,
  }) {
    return NeuroButton(
      key: key,
      onPressed: onPressed,
      isLoading: isLoading,
      isEnabled: isEnabled,
      padding: padding,
      enableHapticFeedback: enableHapticFeedback,
      backgroundColor: AppColors.calmBlue.withValues(alpha: 0.2),
      foregroundColor: AppColors.calmBlue,
      child: child,
    );
  }

  @override
  State<NeuroButton> createState() => _NeuroButtonState();
}

class _NeuroButtonState extends State<NeuroButton>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 100),
    );
    _scaleAnimation = Tween<double>(begin: 1.0, end: 0.95).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  void _handleTapDown(TapDownDetails details) {
    if (widget.isEnabled && !widget.isLoading) {
      _animationController.forward();
    }
  }

  void _handleTapUp(TapUpDetails details) {
    _animationController.reverse();
    if (widget.isEnabled && !widget.isLoading && widget.onPressed != null) {
      if (widget.enableHapticFeedback) {
        HapticFeedback.lightImpact();
      }
      widget.onPressed!();
    }
  }

  void _handleTapCancel() {
    _animationController.reverse();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final effectiveEnabled = widget.isEnabled && !widget.isLoading;

    return GestureDetector(
      onTapDown: _handleTapDown,
      onTapUp: _handleTapUp,
      onTapCancel: _handleTapCancel,
      child: AnimatedBuilder(
        animation: _scaleAnimation,
        builder: (context, child) => Transform.scale(
          scale: _scaleAnimation.value,
          child: child,
        ),
        child: AnimatedOpacity(
          duration: const Duration(milliseconds: 200),
          opacity: effectiveEnabled ? 1.0 : 0.5,
          child: Container(
            padding: widget.padding ??
                const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
            decoration: BoxDecoration(
              color: widget.backgroundColor ?? theme.colorScheme.primary,
              borderRadius: widget.borderRadius ?? BorderRadius.circular(12),
              boxShadow: effectiveEnabled
                  ? [
                      BoxShadow(
                        color: (widget.backgroundColor ?? theme.colorScheme.primary)
                            .withValues(alpha: 0.3),
                        blurRadius: 8,
                        offset: const Offset(0, 4),
                      ),
                    ]
                  : null,
            ),
            child: widget.isLoading
                ? SizedBox(
                    height: 20,
                    width: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      valueColor: AlwaysStoppedAnimation(
                        widget.foregroundColor ?? Colors.white,
                      ),
                    ),
                  )
                : DefaultTextStyle(
                    style: TextStyle(
                      color: widget.foregroundColor ?? Colors.white,
                      fontWeight: FontWeight.w600,
                      fontSize: 16,
                    ),
                    child: widget.child,
                  ),
          ),
        ),
      ),
    );
  }
}

/// A calm card with reduced visual stimulation
class NeuroCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final Color? backgroundColor;
  final BorderRadius? borderRadius;
  final VoidCallback? onTap;
  final bool showShadow;

  const NeuroCard({
    super.key,
    required this.child,
    this.padding,
    this.margin,
    this.backgroundColor,
    this.borderRadius,
    this.onTap,
    this.showShadow = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      margin: margin ?? const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: backgroundColor ?? theme.colorScheme.surface,
        borderRadius: borderRadius ?? BorderRadius.circular(16),
        boxShadow: showShadow
            ? [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ]
            : null,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: borderRadius ?? BorderRadius.circular(16),
          child: Padding(
            padding: padding ?? const EdgeInsets.all(16),
            child: child,
          ),
        ),
      ),
    );
  }
}

/// A text field with neurodivergent-friendly design
class NeuroTextField extends StatefulWidget {
  final TextEditingController? controller;
  final String? hintText;
  final String? labelText;
  final String? errorText;
  final bool obscureText;
  final TextInputType? keyboardType;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onChanged;
  final VoidCallback? onEditingComplete;
  final ValueChanged<String>? onSubmitted;
  final FormFieldValidator<String>? validator;
  final int? maxLines;
  final int? minLines;
  final int? maxLength;
  final Widget? prefixIcon;
  final Widget? suffixIcon;
  final bool enabled;
  final FocusNode? focusNode;
  final bool autofocus;

  const NeuroTextField({
    super.key,
    this.controller,
    this.hintText,
    this.labelText,
    this.errorText,
    this.obscureText = false,
    this.keyboardType,
    this.textInputAction,
    this.onChanged,
    this.onEditingComplete,
    this.onSubmitted,
    this.validator,
    this.maxLines = 1,
    this.minLines,
    this.maxLength,
    this.prefixIcon,
    this.suffixIcon,
    this.enabled = true,
    this.focusNode,
    this.autofocus = false,
  });

  @override
  State<NeuroTextField> createState() => _NeuroTextFieldState();
}

class _NeuroTextFieldState extends State<NeuroTextField> {
  late FocusNode _focusNode;
  bool _isFocused = false;

  @override
  void initState() {
    super.initState();
    _focusNode = widget.focusNode ?? FocusNode();
    _focusNode.addListener(_onFocusChange);
  }

  @override
  void dispose() {
    if (widget.focusNode == null) {
      _focusNode.dispose();
    }
    super.dispose();
  }

  void _onFocusChange() {
    setState(() {
      _isFocused = _focusNode.hasFocus;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (widget.labelText != null) ...[
          Text(
            widget.labelText!,
            style: theme.textTheme.bodySmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: _isFocused
                  ? theme.colorScheme.primary
                  : theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 8),
        ],
        AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            boxShadow: _isFocused
                ? [
                    BoxShadow(
                      color: theme.colorScheme.primary.withValues(alpha: 0.15),
                      blurRadius: 8,
                      offset: const Offset(0, 2),
                    ),
                  ]
                : null,
          ),
          child: TextFormField(
            controller: widget.controller,
            focusNode: _focusNode,
            obscureText: widget.obscureText,
            keyboardType: widget.keyboardType,
            textInputAction: widget.textInputAction,
            onChanged: widget.onChanged,
            onEditingComplete: widget.onEditingComplete,
            onFieldSubmitted: widget.onSubmitted,
            validator: widget.validator,
            maxLines: widget.maxLines,
            minLines: widget.minLines,
            maxLength: widget.maxLength,
            enabled: widget.enabled,
            autofocus: widget.autofocus,
            decoration: InputDecoration(
              hintText: widget.hintText,
              errorText: widget.errorText,
              prefixIcon: widget.prefixIcon,
              suffixIcon: widget.suffixIcon,
              filled: true,
              fillColor: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: theme.colorScheme.outline.withValues(alpha: 0.2),
                ),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: theme.colorScheme.primary,
                  width: 2,
                ),
              ),
              errorBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: theme.colorScheme.error,
                ),
              ),
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 14,
              ),
            ),
          ),
        ),
      ],
    );
  }
}

/// An empty state widget with calming visuals
class NeuroEmptyState extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? description;
  final Widget? action;

  const NeuroEmptyState({
    super.key,
    required this.icon,
    required this.title,
    this.description,
    this.action,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
                shape: BoxShape.circle,
              ),
              child: Icon(
                icon,
                size: 48,
                color: theme.colorScheme.primary.withValues(alpha: 0.7),
              ),
            ),
            const SizedBox(height: 24),
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w600,
              ),
              textAlign: TextAlign.center,
            ),
            if (description != null) ...[
              const SizedBox(height: 8),
              Text(
                description!,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                textAlign: TextAlign.center,
              ),
            ],
            if (action != null) ...[
              const SizedBox(height: 24),
              action!,
            ],
          ],
        ),
      ),
    );
  }
}

/// A chip for tags and categories with neurodivergent-friendly colors
class NeuroChip extends StatelessWidget {
  final String label;
  final IconData? icon;
  final Color? color;
  final bool selected;
  final VoidCallback? onTap;
  final bool showBorder;

  const NeuroChip({
    super.key,
    required this.label,
    this.icon,
    this.color,
    this.selected = false,
    this.onTap,
    this.showBorder = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final chipColor = color ?? theme.colorScheme.primary;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color: selected ? chipColor : chipColor.withValues(alpha: 0.1),
            borderRadius: BorderRadius.circular(20),
            border: showBorder
                ? Border.all(
                    color: selected ? chipColor : chipColor.withValues(alpha: 0.3),
                  )
                : null,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (icon != null) ...[
                Icon(
                  icon,
                  size: 16,
                  color: selected ? Colors.white : chipColor,
                ),
                const SizedBox(width: 4),
              ],
              Text(
                label,
                style: theme.textTheme.labelMedium?.copyWith(
                  color: selected ? Colors.white : chipColor,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// A section header with optional action
class NeuroSectionHeader extends StatelessWidget {
  final String title;
  final String? actionLabel;
  final VoidCallback? onAction;
  final EdgeInsetsGeometry? padding;

  const NeuroSectionHeader({
    super.key,
    required this.title,
    this.actionLabel,
    this.onAction,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Padding(
      padding: padding ?? const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            title,
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          if (actionLabel != null)
            TextButton(
              onPressed: onAction,
              child: Text(actionLabel!),
            ),
        ],
      ),
    );
  }
}


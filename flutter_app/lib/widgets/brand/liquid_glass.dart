import 'dart:ui';
import 'package:flutter/material.dart';

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS COMPONENTS (Flutter)
//
// Reusable glass-morphism widgets that mirror the native
// kyant0/backdrop-powered Compose components in BrandComponents.kt.
//
// Flutter uses BackdropFilter + ImageFilter.blur for the blur
// pass, combined with gradient overlays, inner highlights, and
// box shadows to approximate the same visual.
// ═══════════════════════════════════════════════════════════════

/// A glass-morphism panel with real backdrop blur, frosted tint,
/// inner highlight, and configurable shadow.
///
/// This is the Flutter equivalent of [NeuroGlassPanel] on the
/// Kotlin/Compose side. It wraps its [child] in a [ClipRRect] →
/// [BackdropFilter] → decorated [Container] pipeline.
///
/// ```dart
/// NeuroGlassPanel(
///   borderRadius: BorderRadius.circular(20),
///   blurSigma: 15,
///   child: Text('Hello Glass'),
/// )
/// ```
class NeuroGlassPanel extends StatelessWidget {
  /// The content inside the glass panel.
  final Widget child;

  /// Border radius of the panel (default: 24).
  final BorderRadius borderRadius;

  /// Gaussian blur sigma (both axes). Higher = more frosted.
  final double blurSigma;

  /// Base tint colour drawn over the blurred backdrop.
  /// Defaults to the surface colour at 55 % opacity.
  final Color? tintColor;

  /// Optional accent colour blended into the tint gradient
  /// (e.g. for the "aurora" header variant).
  final Color? accentColor;

  /// Outer box shadow radius (default: 16).
  final double shadowRadius;

  /// Outer box shadow colour opacity (default: auto based on brightness).
  final double? shadowOpacity;

  /// Whether to draw the subtle inner highlight along the top edge.
  final bool showHighlight;

  /// Whether to draw a faint border around the panel.
  final bool showBorder;

  /// The border width (default: 1).
  final double borderWidth;

  /// Optional padding applied inside the glass container.
  final EdgeInsetsGeometry? padding;

  const NeuroGlassPanel({
    super.key,
    required this.child,
    this.borderRadius = const BorderRadius.all(Radius.circular(24)),
    this.blurSigma = 15.0,
    this.tintColor,
    this.accentColor,
    this.shadowRadius = 16.0,
    this.shadowOpacity,
    this.showHighlight = true,
    this.showBorder = true,
    this.borderWidth = 1.0,
    this.padding,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final effectiveTint = tintColor ??
        theme.colorScheme.surface.withValues(alpha: isDark ? 0.50 : 0.55);

    final effectiveShadowOpacity =
        shadowOpacity ?? (isDark ? 0.18 : 0.08);

    // Build gradient colours for the frosted overlay
    final gradientColors = accentColor != null
        ? [
            effectiveTint,
            effectiveTint.withValues(alpha: effectiveTint.a * 0.7),
            accentColor!.withValues(alpha: 0.08),
          ]
        : [
            effectiveTint,
            effectiveTint.withValues(alpha: effectiveTint.a * 0.6),
          ];

    return Container(
      decoration: BoxDecoration(
        borderRadius: borderRadius,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: effectiveShadowOpacity),
            blurRadius: shadowRadius,
            spreadRadius: 0,
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: borderRadius,
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: blurSigma, sigmaY: blurSigma),
          child: Container(
            padding: padding,
            decoration: BoxDecoration(
              borderRadius: borderRadius,
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: gradientColors,
              ),
              border: showBorder
                  ? Border.all(
                      color: isDark
                          ? Colors.white.withValues(alpha: 0.10)
                          : Colors.white.withValues(alpha: 0.25),
                      width: borderWidth,
                    )
                  : null,
            ),
            child: showHighlight
                ? Stack(
                    children: [
                      // Inner highlight along the top edge
                      Positioned(
                        top: 0,
                        left: 0,
                        right: 0,
                        height: 1.5,
                        child: Container(
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.only(
                              topLeft: borderRadius.topLeft,
                              topRight: borderRadius.topRight,
                            ),
                            gradient: LinearGradient(
                              colors: [
                                Colors.white.withValues(
                                    alpha: isDark ? 0.08 : 0.35),
                                Colors.white.withValues(
                                    alpha: isDark ? 0.02 : 0.10),
                              ],
                            ),
                          ),
                        ),
                      ),
                      child,
                    ],
                  )
                : child,
          ),
        ),
      ),
    );
  }
}

class SkeuomorphicPaletteData {
  final Color surfaceTop;
  final Color surfaceMid;
  final Color surfaceBottom;
  final Color border;
  final Color shadow;
  final Color highlight;
  final Color innerShadow;
  final Color accent;
  final Color activeTop;
  final Color activeBottom;

  const SkeuomorphicPaletteData({
    required this.surfaceTop,
    required this.surfaceMid,
    required this.surfaceBottom,
    required this.border,
    required this.shadow,
    required this.highlight,
    required this.innerShadow,
    required this.accent,
    required this.activeTop,
    required this.activeBottom,
  });

  factory SkeuomorphicPaletteData.fromTheme(
    ThemeData theme, {
    required bool isFull,
  }) {
    final scheme = theme.colorScheme;
    final isDark = theme.brightness == Brightness.dark;
    final base = Color.alphaBlend(
      scheme.primary.withValues(alpha: isFull ? 0.16 : 0.09),
      scheme.surface,
    );
    final accent = Color.alphaBlend(
      scheme.secondary.withValues(alpha: isFull ? 0.32 : 0.18),
      scheme.primary,
    );

    return SkeuomorphicPaletteData(
      surfaceTop: Color.alphaBlend(
        Colors.white.withValues(alpha: isDark ? 0.06 : (isFull ? 0.26 : 0.16)),
        base,
      ),
      surfaceMid: base,
      surfaceBottom: Color.alphaBlend(
        accent.withValues(alpha: isFull ? 0.26 : 0.14),
        base,
      ),
      border: Colors.white.withValues(alpha: isDark ? 0.08 : (isFull ? 0.40 : 0.28)),
      shadow: Colors.black.withValues(alpha: isDark ? (isFull ? 0.34 : 0.24) : (isFull ? 0.12 : 0.08)),
      highlight: Colors.white.withValues(alpha: isDark ? 0.05 : (isFull ? 0.24 : 0.14)),
      innerShadow: (isDark ? Colors.black : accent)
          .withValues(alpha: isDark ? (isFull ? 0.18 : 0.10) : (isFull ? 0.12 : 0.06)),
      accent: accent,
      activeTop: Color.alphaBlend(
        Colors.white.withValues(alpha: isDark ? 0.08 : 0.28),
        base,
      ),
      activeBottom: Color.alphaBlend(
        accent.withValues(alpha: isFull ? 0.34 : 0.20),
        base,
      ),
    );
  }
}

class SkeuomorphicPanel extends StatelessWidget {
  final Widget child;
  final BorderRadius borderRadius;
  final bool isFull;
  final EdgeInsetsGeometry? padding;
  final bool isActive;

  const SkeuomorphicPanel({
    super.key,
    required this.child,
    this.borderRadius = const BorderRadius.all(Radius.circular(24)),
    this.isFull = false,
    this.padding,
    this.isActive = false,
  });

  @override
  Widget build(BuildContext context) {
    final palette = SkeuomorphicPaletteData.fromTheme(
      Theme.of(context),
      isFull: isFull,
    );

    final gradientColors = isActive
        ? [palette.activeTop, palette.surfaceMid, palette.activeBottom]
        : [palette.surfaceTop, palette.surfaceMid, palette.surfaceBottom];

    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: borderRadius,
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: gradientColors,
        ),
        border: Border.all(color: palette.border, width: 1),
        boxShadow: [
          BoxShadow(
            color: palette.shadow,
            blurRadius: isFull ? 26 : 16,
            offset: Offset(0, isFull ? 14 : 9),
          ),
          BoxShadow(
            color: palette.highlight,
            blurRadius: isFull ? 14 : 8,
            offset: const Offset(-4, -4),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: borderRadius,
        child: Stack(
          children: [
            Positioned.fill(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [palette.highlight, Colors.transparent],
                  ),
                ),
              ),
            ),
            Positioned.fill(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Colors.transparent, palette.innerShadow],
                  ),
                ),
              ),
            ),
            if (padding != null) Padding(padding: padding!, child: child) else child,
          ],
        ),
      ),
    );
  }
}

class SkeuomorphicIconButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String? tooltip;
  final bool isFull;
  final bool isPrimary;
  final double size;
  final double iconSize;

  const SkeuomorphicIconButton({
    super.key,
    required this.icon,
    required this.onPressed,
    this.tooltip,
    this.isFull = false,
    this.isPrimary = false,
    this.size = 42,
    this.iconSize = 20,
  });

  @override
  Widget build(BuildContext context) {
    final palette = SkeuomorphicPaletteData.fromTheme(
      Theme.of(context),
      isFull: isFull,
    );

    Widget button = SkeuomorphicPanel(
      borderRadius: BorderRadius.circular(size / 2.6),
      isFull: isFull,
      isActive: isPrimary,
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(size / 2.6),
          onTap: onPressed,
          child: SizedBox(
            width: size,
            height: size,
            child: Icon(
              icon,
              size: iconSize,
              color: isPrimary ? palette.accent : Theme.of(context).colorScheme.onSurfaceVariant,
            ),
          ),
        ),
      ),
    );

    if (tooltip != null) {
      button = Tooltip(message: tooltip!, child: button);
    }

    return button;
  }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS TOGGLE
// ═══════════════════════════════════════════════════════════════

/// A settings-style toggle row with a **Liquid Glass** card aesthetic.
///
/// Mirrors the Kotlin [LiquidGlassToggle] from BrandComponents.kt.
/// Uses backdrop blur for the card background and a custom frosted
/// thumb on the switch.
class LiquidGlassToggle extends StatelessWidget {
  final String title;
  final String description;
  final IconData icon;
  final bool isChecked;
  final bool enabled;
  final ValueChanged<bool> onCheckedChange;

  const LiquidGlassToggle({
    super.key,
    required this.title,
    required this.description,
    required this.icon,
    required this.isChecked,
    this.enabled = true,
    required this.onCheckedChange,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final effectiveAlpha = enabled ? 1.0 : 0.5;

    final checkedAccent = isChecked && enabled
        ? theme.colorScheme.primary.withValues(alpha: 0.12)
        : Colors.transparent;

    final iconColor = isChecked && enabled
        ? theme.colorScheme.primary
        : theme.colorScheme.onSurfaceVariant;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Semantics(
        toggled: isChecked,
        label: '$title, $description',
        child: NeuroGlassPanel(
          borderRadius: BorderRadius.circular(20),
          blurSigma: 12,
          shadowRadius: 10,
          tintColor: theme.colorScheme.surface
              .withValues(alpha: isDark ? 0.50 : 0.60),
          showHighlight: true,
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              borderRadius: BorderRadius.circular(20),
              onTap: enabled ? () => onCheckedChange(!isChecked) : null,
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 250),
                curve: Curves.easeOutCubic,
                decoration: BoxDecoration(
                  color: checkedAccent,
                  borderRadius: BorderRadius.circular(20),
                ),
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                child: Row(
                  children: [
                    AnimatedScale(
                      scale: enabled ? 1.0 : 0.85,
                      duration: const Duration(milliseconds: 200),
                      child: Icon(
                        icon,
                        size: 24,
                        color: iconColor.withValues(alpha: effectiveAlpha),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            title,
                            style: theme.textTheme.bodyLarge?.copyWith(
                              fontWeight: FontWeight.w500,
                              color: theme.colorScheme.onSurface
                                  .withValues(alpha: effectiveAlpha),
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          if (description.isNotEmpty)
                            Text(
                              description,
                              style: theme.textTheme.bodySmall?.copyWith(
                                color: theme.colorScheme.onSurfaceVariant
                                    .withValues(alpha: effectiveAlpha),
                              ),
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    _LiquidGlassSwitch(
                      checked: isChecked,
                      enabled: enabled,
                      isDark: isDark,
                      primaryColor: theme.colorScheme.primary,
                      onSurfaceVariant: theme.colorScheme.onSurfaceVariant,
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

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS SWITCH
// ═══════════════════════════════════════════════════════════════

/// A custom Switch styled with a Liquid Glass aesthetic.
///
/// The track is a frosted pill shape and the thumb is a translucent
/// circle with a gradient and an inner highlight that slides smoothly.
class _LiquidGlassSwitch extends StatelessWidget {
  final bool checked;
  final bool enabled;
  final bool isDark;
  final Color primaryColor;
  final Color onSurfaceVariant;

  static const _trackWidth = 52.0;
  static const _trackHeight = 30.0;
  static const _thumbSize = 24.0;
  static const _thumbPadding = 3.0;

  const _LiquidGlassSwitch({
    required this.checked,
    required this.enabled,
    required this.isDark,
    required this.primaryColor,
    required this.onSurfaceVariant,
  });

  @override
  Widget build(BuildContext context) {
    final trackColor = checked && enabled
        ? primaryColor.withValues(alpha: isDark ? 0.35 : 0.25)
        : onSurfaceVariant.withValues(alpha: isDark ? 0.12 : 0.08);

    final thumbColor = checked && enabled
        ? primaryColor
        : onSurfaceVariant.withValues(alpha: 0.6);

    final borderColor = isDark
        ? Colors.white.withValues(alpha: 0.10)
        : Colors.black.withValues(alpha: 0.06);


    return SizedBox(
      width: _trackWidth,
      height: _trackHeight,
      child: DecoratedBox(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(_trackHeight / 2),
          color: trackColor,
          border: Border.all(color: borderColor, width: 0.5),
        ),
        child: Padding(
          padding: const EdgeInsets.all(_thumbPadding),
          child: AnimatedAlign(
            duration: const Duration(milliseconds: 300),
            curve: Curves.easeOutBack,
            alignment:
                checked ? Alignment.centerRight : Alignment.centerLeft,
            child: AnimatedScale(
              scale: enabled ? 1.0 : 0.85,
              duration: const Duration(milliseconds: 200),
              child: Container(
                width: _thumbSize,
                height: _thumbSize,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      thumbColor,
                      thumbColor.withValues(alpha: 0.85),
                    ],
                  ),
                  border: Border.all(
                    color: Colors.white
                        .withValues(alpha: isDark ? 0.18 : 0.30),
                    width: 0.5,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color:
                          thumbColor.withValues(alpha: 0.3),
                      blurRadius: checked ? 4 : 2,
                    ),
                  ],
                ),
                child: checked
                    ? const Icon(Icons.check,
                        size: 14, color: Colors.white)
                    : null,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS APP BAR
// ═══════════════════════════════════════════════════════════════

/// An edge-to-edge app bar with a Liquid Glass frosted backdrop, matching
/// how iOS 26 renders its navigation bar.
///
/// [variant] can be `"frosted"` (neutral tint) or `"aurora"` (accent-tinted).
class LiquidGlassAppBar extends StatelessWidget implements PreferredSizeWidget {
  final Widget? title;
  final Widget? leading;
  final List<Widget>? actions;
  final String variant;

  const LiquidGlassAppBar({
    super.key,
    this.title,
    this.leading,
    this.actions,
    this.variant = 'frosted',
  });

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final statusBarHeight = MediaQuery.of(context).padding.top;

    final accentColor = variant == 'aurora'
        ? theme.colorScheme.primary.withValues(alpha: 0.08)
        : null;

    final titleColor = variant == 'aurora'
        ? theme.colorScheme.primary
        : theme.colorScheme.onSurface;

    return NeuroGlassPanel(
      borderRadius: BorderRadius.zero,
      blurSigma: 15,
      shadowRadius: 0,
      showHighlight: true,
      showBorder: false,
      accentColor: accentColor,
      child: Padding(
        padding: EdgeInsets.only(top: statusBarHeight),
        child: SizedBox(
          height: kToolbarHeight,
          child: NavigationToolbar(
            leading: leading,
            middle: title != null
                ? DefaultTextStyle.merge(
                    style: TextStyle(
                      color: titleColor,
                      fontWeight: FontWeight.w600,
                      fontSize: 20,
                    ),
                    child: title!,
                  )
                : null,
            trailing: actions != null
                ? Row(mainAxisSize: MainAxisSize.min, children: actions!)
                : null,
          ),
        ),
      ),
    );
  }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS FAB
// ═══════════════════════════════════════════════════════════════

/// A floating action button with a Liquid Glass aesthetic — circular
/// frosted glass matching iOS 26's treatment of prominent actions.
class LiquidGlassFAB extends StatelessWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final String variant;
  final String? tooltip;

  const LiquidGlassFAB({
    super.key,
    required this.icon,
    required this.onPressed,
    this.variant = 'frosted',
    this.tooltip,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final iconColor = variant == 'aurora'
        ? theme.colorScheme.primary
        : theme.colorScheme.onSurface;

    Widget fab = NeuroGlassPanel(
      borderRadius: BorderRadius.circular(28),
      blurSigma: 12,
      shadowRadius: 12,
      showHighlight: true,
      accentColor: variant == 'aurora'
          ? theme.colorScheme.primary.withValues(alpha: 0.06)
          : null,
      child: Material(
        color: Colors.transparent,
        shape: const CircleBorder(),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onPressed,
          customBorder: const CircleBorder(),
          child: SizedBox(
            width: 56,
            height: 56,
            child: Center(
              child: Icon(icon, color: iconColor, size: 24),
            ),
          ),
        ),
      ),
    );

    if (tooltip != null) {
      fab = Tooltip(message: tooltip!, child: fab);
    }

    return fab;
  }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════

/// Wraps bottom-sheet content in a frosted glass panel with a
/// built-in drag handle — matching the iOS 26 sheet appearance.
///
/// Use this inside [showModalBottomSheet] as the direct child.
class LiquidGlassBottomSheet extends StatelessWidget {
  final Widget child;
  final String variant;

  const LiquidGlassBottomSheet({
    super.key,
    required this.child,
    this.variant = 'frosted',
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return NeuroGlassPanel(
      borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
      blurSigma: 18,
      shadowRadius: 0,
      showHighlight: true,
      showBorder: true,
      accentColor: variant == 'aurora'
          ? theme.colorScheme.primary.withValues(alpha: 0.06)
          : null,
      child: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.only(top: 28),
            child: child,
          ),
          Positioned(
            top: 12,
            left: 0,
            right: 0,
            child: Center(
              child: Container(
                width: 32,
                height: 4,
                decoration: BoxDecoration(
                  color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.4),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Convenience helper: shows a modal bottom sheet wrapped in
/// [LiquidGlassBottomSheet] with a transparent background.
Future<T?> showLiquidGlassBottomSheet<T>({
  required BuildContext context,
  required WidgetBuilder builder,
  String variant = 'frosted',
  bool isScrollControlled = true,
  bool useSafeArea = true,
}) {
  return showModalBottomSheet<T>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: isScrollControlled,
    useSafeArea: useSafeArea,
    builder: (ctx) => LiquidGlassBottomSheet(
      variant: variant,
      child: builder(ctx),
    ),
  );
}

import 'dart:ui';

import '../../models/custom_avatar.dart';
import '../../models/full_avatar.dart' hide HexColorExtension;

/// Describes the rendering target so we can bridge Canvas, SceneKit, etc.
enum AvatarRenderSurface {
  canvas2d,
  vector,
  mesh3d,
}

/// Blending hints for compositing passes.
enum AvatarLayerBlend {
  normal,
  multiply,
  screen,
  additive,
}

/// High-level pipeline phases to keep ordering predictable.
enum AvatarLayerPhase {
  background,
  body,
  face,
  hair,
  apparel,
  accessories,
  props,
  fx,
}

/// Animation metadata that can be shared between 2D and 3D implementations.
class AvatarAnimationState {
  final AvatarAnimationProfile? profile;
  final double normalizedTime;

  const AvatarAnimationState({
    this.profile,
    this.normalizedTime = 0,
  });

  AvatarAnimationState copyWith({
    AvatarAnimationProfile? profile,
    double? normalizedTime,
  }) {
    return AvatarAnimationState(
      profile: profile ?? this.profile,
      normalizedTime: normalizedTime ?? this.normalizedTime,
    );
  }
}

/// Context passed to renderers so they can react to device/scene data.
class AvatarRenderContext {
  final Size viewportSize;
  final double devicePixelRatio;
  final AvatarRenderSurface surface;
  final Duration elapsed;
  final AvatarAnimationState animation;

  const AvatarRenderContext({
    required this.viewportSize,
    this.devicePixelRatio = 1,
    this.surface = AvatarRenderSurface.canvas2d,
    this.elapsed = Duration.zero,
    this.animation = const AvatarAnimationState(),
  });

  AvatarRenderContext copyWith({
    Size? viewportSize,
    double? devicePixelRatio,
    AvatarRenderSurface? surface,
    Duration? elapsed,
    AvatarAnimationState? animation,
  }) {
    return AvatarRenderContext(
      viewportSize: viewportSize ?? this.viewportSize,
      devicePixelRatio: devicePixelRatio ?? this.devicePixelRatio,
      surface: surface ?? this.surface,
      elapsed: elapsed ?? this.elapsed,
      animation: animation ?? this.animation,
    );
  }
}

/// One renderable pass in the pipeline.
class AvatarRenderPass {
  final AvatarLayerPhase phase;
  final int zIndex;
  final AvatarLayerBlend blend;
  final void Function(Canvas canvas, Size size) painter;

  const AvatarRenderPass({
    required this.phase,
    required this.painter,
    this.zIndex = 0,
    this.blend = AvatarLayerBlend.normal,
  });
}

/// Result of compiling a renderer for a non-Canvas surface.
class AvatarRenderable {
  final dynamic handle;
  final AvatarRenderSurface surface;

  const AvatarRenderable({required this.handle, required this.surface});
}

/// Contract for building render passes or compiled assets from avatar data.
abstract class AvatarRenderer<TAvatar> {
  List<AvatarRenderPass> buildPasses(TAvatar avatar, AvatarRenderContext context);

  Future<AvatarRenderable?> compile(TAvatar avatar, AvatarRenderSurface surface) async => null;
}

typedef AvatarPaintStep = void Function(Canvas canvas, Size size);

class CustomAvatarPaintDelegates {
  final AvatarPaintStep paintFace;
  final AvatarPaintStep paintHair;
  final AvatarPaintStep paintEyesAndMouth;
  final AvatarPaintStep paintFacialHair;
  final AvatarPaintStep paintAccessory;

  const CustomAvatarPaintDelegates({
    required this.paintFace,
    required this.paintHair,
    required this.paintEyesAndMouth,
    required this.paintFacialHair,
    required this.paintAccessory,
  });
}

/// Canvas-first renderer for the lightweight `CustomAvatar` model.
class CanvasCustomAvatarRenderer extends AvatarRenderer<CustomAvatar> {
  final CustomAvatarPaintDelegates Function(CustomAvatar avatar) delegateBuilder;

  CanvasCustomAvatarRenderer({required this.delegateBuilder});

  @override
  List<AvatarRenderPass> buildPasses(CustomAvatar avatar, AvatarRenderContext context) {
    final delegates = delegateBuilder(avatar);
    return [
      AvatarRenderPass(
        phase: AvatarLayerPhase.background,
        zIndex: 0,
        painter: (canvas, size) => canvas.drawColor(avatar.backgroundColor.toColor(), BlendMode.src),
      ),
      AvatarRenderPass(
        phase: AvatarLayerPhase.face,
        zIndex: 1,
        painter: delegates.paintFace,
      ),
      AvatarRenderPass(
        phase: AvatarLayerPhase.hair,
        zIndex: 2,
        painter: delegates.paintHair,
      ),
      AvatarRenderPass(
        phase: AvatarLayerPhase.face,
        zIndex: 3,
        painter: delegates.paintEyesAndMouth,
      ),
      if (avatar.facialHair != null && avatar.facialHair != AvatarFacialHair.none)
        AvatarRenderPass(
          phase: AvatarLayerPhase.face,
          zIndex: 4,
          painter: delegates.paintFacialHair,
        ),
      if (avatar.accessory != null && avatar.accessory != AvatarAccessory.none)
        AvatarRenderPass(
          phase: AvatarLayerPhase.accessories,
          zIndex: 5,
          painter: delegates.paintAccessory,
        ),
    ];
  }
}

CustomAvatarPaintDelegates buildCustomAvatarDelegates(CustomAvatar avatar) {
  throw UnimplementedError('Provide a delegate builder from the widget layer');
}

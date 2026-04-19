package com.kyilmaz.neurocomet.ui.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kyilmaz.neurocomet.AnimationSettings

enum class M3EPhysicsRole {
    EMPHASIZED,
    STANDARD,
    CALM,
    SNAP
}

@Immutable
data class M3EPhysicsSystem(
    val reducedMotion: Boolean,
    val pressScale: Float,
    val emphasizedScale: Float,
    val settleDurationMillis: Int
) {
    fun floatSpec(role: M3EPhysicsRole = M3EPhysicsRole.STANDARD): FiniteAnimationSpec<Float> {
        if (reducedMotion) {
            return tween(durationMillis = settleDurationMillis, easing = FastOutSlowInEasing)
        }

        return when (role) {
            M3EPhysicsRole.EMPHASIZED -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )

            M3EPhysicsRole.STANDARD -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )

            M3EPhysicsRole.CALM -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )

            M3EPhysicsRole.SNAP -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        }
    }

    fun dpSpec(role: M3EPhysicsRole = M3EPhysicsRole.STANDARD): FiniteAnimationSpec<Dp> {
        if (reducedMotion) {
            return tween(durationMillis = settleDurationMillis, easing = FastOutSlowInEasing)
        }

        return when (role) {
            M3EPhysicsRole.EMPHASIZED -> spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )

            M3EPhysicsRole.STANDARD -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )

            M3EPhysicsRole.CALM -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )

            M3EPhysicsRole.SNAP -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        }
    }

    fun colorSpec(role: M3EPhysicsRole = M3EPhysicsRole.STANDARD): FiniteAnimationSpec<Color> {
        if (reducedMotion) {
            return tween(durationMillis = settleDurationMillis, easing = FastOutSlowInEasing)
        }

        return when (role) {
            M3EPhysicsRole.EMPHASIZED -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )

            M3EPhysicsRole.SNAP -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )

            M3EPhysicsRole.STANDARD,
            M3EPhysicsRole.CALM -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        }
    }
}

val LocalM3EPhysics = staticCompositionLocalOf {
    M3EPhysicsSystem(
        reducedMotion = false,
        pressScale = 0.98f,
        emphasizedScale = 1.18f,
        settleDurationMillis = 90
    )
}

@Composable
fun rememberM3EPhysics(
    animationSettings: AnimationSettings
): M3EPhysicsSystem {
    val reducedMotion = animationSettings.disableAllAnimations ||
        animationSettings.disableTransitionAnimations ||
        animationSettings.disableButtonAnimations

    return remember(reducedMotion) {
        M3EPhysicsSystem(
            reducedMotion = reducedMotion,
            pressScale = if (reducedMotion) 0.995f else 0.975f,
            emphasizedScale = if (reducedMotion) 1.03f else 1.18f,
            settleDurationMillis = if (reducedMotion) 70 else 110
        )
    }
}

@Composable
fun rememberM3EPressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = LocalM3EPhysics.current.pressScale,
    role: M3EPhysicsRole = M3EPhysicsRole.STANDARD
): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    val physics = LocalM3EPhysics.current
    return animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else 1f,
        animationSpec = physics.floatSpec(role),
        label = "m3ePressScale"
    )
}

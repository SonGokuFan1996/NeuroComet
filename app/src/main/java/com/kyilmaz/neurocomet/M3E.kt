@file:Suppress("unused")

package com.kyilmaz.neurocomet

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem

/**
 * Minimal, production-safe Material 3 Expressive-style tokens.
 *
 * Delegating to M3EDesignSystem for consistency across the app and with the Flutter version.
 */

@Immutable
object M3EShapes {
    /**
     * Expressive rounded shapes that still work across the entire app.
     */
    val shapes: Shapes = M3EDesignSystem.Shapes.materialShapes

    /**
     * Common radii to reuse in custom components.
     */
    val radiusChip: Dp = M3EDesignSystem.Shapes.full // Pill shape
    val radiusCard: Dp = M3EDesignSystem.Shapes.large
    val radiusSheet: Dp = M3EDesignSystem.Shapes.extraLarge
    val radiusInput: Dp = M3EDesignSystem.Shapes.medium
}

@Immutable
object M3EMotion {
    // Durations tuned to feel snappy but gentle.
    const val durationShort: Int = M3EDesignSystem.AnimationDuration.fast
    const val durationMedium: Int = M3EDesignSystem.AnimationDuration.normal
    const val durationLong: Int = M3EDesignSystem.AnimationDuration.slow

    /**
     * Default spring used for small affordance animations (chips/buttons/cards).
     */
    fun defaultSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * A calmer spring for larger transitions.
     */
    fun calmSpring() = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}

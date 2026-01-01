@file:Suppress("unused")

package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Minimal, production-safe Material 3 Expressive-style tokens.
 *
 * This is intentionally lightweight:
 * - Centralizes shapes (so the UI feels consistently "expressive" and friendly)
 * - Centralizes motion specs (so easing/springs feel consistent, and can be disabled)
 *
 * Note: This isn't an official "M3E" library. It's a token layer that makes your app
 * feel cohesive and makes future iteration safer.
 */

@Immutable
object M3EShapes {
    /**
     * Expressive rounded shapes that still work across the entire app.
     */
    val shapes: Shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )

    /**
     * Common radii to reuse in custom components.
     */
    val radiusChip: Dp = 16.dp
    val radiusCard: Dp = 24.dp
    val radiusSheet: Dp = 28.dp
    val radiusInput: Dp = 22.dp
}

@Immutable
object M3EMotion {
    // Durations tuned to feel snappy but gentle.
    const val durationShort: Int = 150
    const val durationMedium: Int = 220
    const val durationLong: Int = 360

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

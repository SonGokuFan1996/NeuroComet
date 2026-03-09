package com.kyilmaz.neurocomet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// =============================================================================
// NEURODIVERGENT-FRIENDLY WALLPAPER ENGINE
// Sensory-conscious animated backgrounds for Messages conversations.
// All rendering uses DrawScope (GPU-accelerated Canvas), zero recomposition.
// =============================================================================

/**
 * Available conversation wallpapers, each designed with sensory sensitivity in mind.
 */
enum class ConversationWallpaper(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    NONE("None", "Plain background", "🚫"),
    CALM_OCEAN("Calm Ocean", "Slow-moving blue gradient waves", "🌊"),
    AURORA_BOREALIS("Aurora Borealis", "Gentle shifting northern lights", "🌌"),
    BREATHING_BUBBLES("Breathing Bubbles", "Soft pulsing circles for grounding", "🫧"),
    SOFT_RAIN("Soft Rain", "Gentle dots drifting downward", "🌧️"),
    DEEP_FOCUS("Deep Focus", "Minimal dark gradient, reduced stimulation", "🎯"),
    WARM_SUNSET("Warm Sunset", "Slow warm gradient cycle", "🌅"),
    STARFIELD("Starfield", "Twinkling stars on a calm night sky", "✨");

    companion object {
        fun fromKey(key: String?): ConversationWallpaper =
            entries.find { it.name == key } ?: NONE
    }
}

// =============================================================================
// Composable Modifier — attach to the Box behind the message list
// =============================================================================

/**
 * Applies a neurodivergent-friendly animated wallpaper behind the content.
 *
 * @param wallpaper The selected wallpaper preset.
 * @param isDark Whether the app is in dark mode.
 * @param reducedMotion If true, all animations are frozen at their initial frame.
 * @param dataSaver If true, treated as implicit reduced-motion (saves battery).
 */
@Composable
fun Modifier.conversationWallpaper(
    wallpaper: ConversationWallpaper,
    isDark: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    dataSaver: Boolean = false
): Modifier {
    if (wallpaper == ConversationWallpaper.NONE) return this

    val freeze = reducedMotion || dataSaver

    // ── Animation drivers (all use rememberInfiniteTransition for efficiency) ──
    val transition = rememberInfiniteTransition(label = "wallpaper_${wallpaper.name}")

    val slowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (freeze) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowPhase"
    )

    val breathPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (freeze) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathPhase"
    )

    val gentlePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (freeze) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gentlePhase"
    )

    return this.drawBehind {
        when (wallpaper) {
            ConversationWallpaper.NONE -> { /* no-op */ }
            ConversationWallpaper.CALM_OCEAN -> drawCalmOcean(slowPhase, isDark)
            ConversationWallpaper.AURORA_BOREALIS -> drawAuroraBorealis(gentlePhase, isDark)
            ConversationWallpaper.BREATHING_BUBBLES -> drawBreathingBubbles(breathPhase, isDark)
            ConversationWallpaper.SOFT_RAIN -> drawSoftRain(slowPhase, isDark)
            ConversationWallpaper.DEEP_FOCUS -> drawDeepFocus(isDark)
            ConversationWallpaper.WARM_SUNSET -> drawWarmSunset(gentlePhase, isDark)
            ConversationWallpaper.STARFIELD -> drawStarfield(breathPhase, isDark)
        }
    }
}

// =============================================================================
// Individual Wallpaper Renderers (DrawScope extensions)
// =============================================================================

/**
 * Calm Ocean — Two overlapping sine-wave fills with translucent blues.
 */
private fun DrawScope.drawCalmOcean(phase: Float, isDark: Boolean) {
    val w = size.width
    val h = size.height

    // Base gradient
    val baseColors = if (isDark) {
        listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF0D253A))
    } else {
        listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFFE1F5FE))
    }
    drawRect(brush = Brush.verticalGradient(baseColors))

    // Wave 1 — slow
    val wave1Alpha = if (isDark) 0.15f else 0.12f
    val wave1Color = if (isDark) Color(0xFF42A5F5) else Color(0xFF1976D2)
    val path1 = Path().apply {
        moveTo(0f, h * 0.65f)
        val phaseOffset = phase * 2f * PI.toFloat()
        for (x in 0..w.toInt() step 4) {
            val xf = x.toFloat()
            val y = h * 0.65f + sin(xf / w * 2f * PI.toFloat() + phaseOffset) * h * 0.04f
            lineTo(xf, y)
        }
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path1, color = wave1Color.copy(alpha = wave1Alpha))

    // Wave 2 — offset
    val wave2Color = if (isDark) Color(0xFF64B5F6) else Color(0xFF42A5F5)
    val path2 = Path().apply {
        moveTo(0f, h * 0.72f)
        val phaseOffset2 = phase * 2f * PI.toFloat() + PI.toFloat() * 0.7f
        for (x in 0..w.toInt() step 4) {
            val xf = x.toFloat()
            val y = h * 0.72f + sin(xf / w * 3f * PI.toFloat() + phaseOffset2) * h * 0.03f
            lineTo(xf, y)
        }
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path2, color = wave2Color.copy(alpha = wave1Alpha * 0.7f))
}

/**
 * Aurora Borealis — Vertical gradient with shifting color stops.
 */
private fun DrawScope.drawAuroraBorealis(phase: Float, isDark: Boolean) {
    val phaseRad = phase * 2f * PI.toFloat()

    // Background
    drawRect(color = if (isDark) Color(0xFF0A0E1A) else Color(0xFFF5F5F5))

    // Aurora bands — 3 overlapping vertical gradients with shifting positions
    val band1Y = size.height * (0.15f + sin(phaseRad) * 0.08f)
    val band1H = size.height * 0.25f
    val color1 = if (isDark) Color(0xFF00E676) else Color(0xFF81C784)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color1.copy(alpha = 0.12f), Color.Transparent),
            startY = band1Y,
            endY = band1Y + band1H
        )
    )

    val band2Y = size.height * (0.30f + cos(phaseRad * 0.8f) * 0.06f)
    val band2H = size.height * 0.20f
    val color2 = if (isDark) Color(0xFF7C4DFF) else Color(0xFFBA68C8)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color2.copy(alpha = 0.10f), Color.Transparent),
            startY = band2Y,
            endY = band2Y + band2H
        )
    )

    val band3Y = size.height * (0.50f + sin(phaseRad * 1.3f) * 0.05f)
    val band3H = size.height * 0.18f
    val color3 = if (isDark) Color(0xFF00BCD4) else Color(0xFF80CBC4)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color3.copy(alpha = 0.08f), Color.Transparent),
            startY = band3Y,
            endY = band3Y + band3H
        )
    )
}

/**
 * Breathing Bubbles — Soft pulsing circles for anxiety grounding.
 * Follows a 4-second inhale/exhale rhythm.
 */
private fun DrawScope.drawBreathingBubbles(breathPhase: Float, isDark: Boolean) {
    // Background
    drawRect(color = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF8F0FF))

    // breathPhase goes 0→1→0 (Reverse mode) matching inhale/exhale
    val scale = 0.7f + breathPhase * 0.3f
    val alpha = 0.06f + breathPhase * 0.08f

    data class Bubble(val cx: Float, val cy: Float, val baseRadius: Float, val color: Color)

    val bubbles = listOf(
        Bubble(0.25f, 0.30f, 0.12f, if (isDark) Color(0xFF7C4DFF) else Color(0xFFCE93D8)),
        Bubble(0.70f, 0.20f, 0.09f, if (isDark) Color(0xFF536DFE) else Color(0xFF90CAF9)),
        Bubble(0.50f, 0.55f, 0.14f, if (isDark) Color(0xFF00BFA5) else Color(0xFFA5D6A7)),
        Bubble(0.15f, 0.75f, 0.08f, if (isDark) Color(0xFFFF80AB) else Color(0xFFF8BBD0)),
        Bubble(0.80f, 0.65f, 0.10f, if (isDark) Color(0xFF64B5F6) else Color(0xFFBBDEFB))
    )

    bubbles.forEach { b ->
        val r = b.baseRadius * size.width * scale
        drawCircle(
            color = b.color.copy(alpha = alpha),
            radius = r,
            center = Offset(b.cx * size.width, b.cy * size.height)
        )
        // Inner glow
        drawCircle(
            color = b.color.copy(alpha = alpha * 0.5f),
            radius = r * 0.6f,
            center = Offset(b.cx * size.width, b.cy * size.height)
        )
    }
}

/**
 * Soft Rain — Gentle dots drifting downward, wrapping around.
 */
private fun DrawScope.drawSoftRain(phase: Float, isDark: Boolean) {
    // Background
    drawRect(color = if (isDark) Color(0xFF1A1D23) else Color(0xFFF0F4F8))

    val dropColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF64B5F6)

    // Deterministic raindrop positions using seed-based math (no Random in draw)
    for (i in 0 until 24) {
        val seedX = (i * 0.618033988f) % 1f  // Golden ratio spacing
        val seedSpeed = 0.6f + (i % 5) * 0.1f
        val x = seedX * size.width
        val baseY = ((i * 0.3141592f + phase * seedSpeed) % 1f) * (size.height + 40f) - 20f
        val radius = 1.5f + (i % 3) * 0.8f
        val alpha = if (isDark) 0.20f else 0.15f

        drawCircle(
            color = dropColor.copy(alpha = alpha - (i % 4) * 0.03f),
            radius = radius,
            center = Offset(x, baseY)
        )
    }
}

/**
 * Deep Focus — Minimal dark gradient with no animation. Reduced stimulation.
 */
private fun DrawScope.drawDeepFocus(isDark: Boolean) {
    val colors = if (isDark) {
        listOf(Color(0xFF0D0D12), Color(0xFF1A1A24), Color(0xFF0D0D12))
    } else {
        listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE8E8E8))
    }
    drawRect(
        brush = Brush.radialGradient(
            colors = colors,
            center = Offset(size.width * 0.5f, size.height * 0.4f),
            radius = size.width * 0.8f
        )
    )
}

/**
 * Warm Sunset — Slow warm gradient cycle with oranges, peaches, pinks.
 */
private fun DrawScope.drawWarmSunset(phase: Float, isDark: Boolean) {
    val phaseRad = phase * 2f * PI.toFloat()

    // Shift gradient center slowly
    val centerY = size.height * (0.35f + sin(phaseRad) * 0.1f)

    val colors = if (isDark) {
        val blend = (sin(phaseRad) + 1f) / 2f
        listOf(
            lerpColor(Color(0xFF1A0A0A), Color(0xFF2A1010), blend),
            lerpColor(Color(0xFF4A1A00), Color(0xFF3A1500), blend),
            lerpColor(Color(0xFF2A0D1A), Color(0xFF1A0A15), blend)
        )
    } else {
        val blend = (sin(phaseRad) + 1f) / 2f
        listOf(
            lerpColor(Color(0xFFFFF3E0), Color(0xFFFFECB3), blend),
            lerpColor(Color(0xFFFFCCBC), Color(0xFFF8BBD0), blend),
            lerpColor(Color(0xFFFFE0B2), Color(0xFFFFF9C4), blend)
        )
    }

    drawRect(
        brush = Brush.radialGradient(
            colors = colors,
            center = Offset(size.width * 0.5f, centerY),
            radius = size.width * 1.2f
        )
    )
}

/**
 * Starfield — Twinkling stars on a calm night sky.
 */
private fun DrawScope.drawStarfield(breathPhase: Float, isDark: Boolean) {
    // Background — deep night sky
    val bgColors = if (isDark) {
        listOf(Color(0xFF0A0A1A), Color(0xFF0F0F2A), Color(0xFF0A0A1A))
    } else {
        listOf(Color(0xFFE8EAF6), Color(0xFFE0E0F0), Color(0xFFEDE7F6))
    }
    drawRect(brush = Brush.verticalGradient(bgColors))

    val starColor = if (isDark) Color.White else Color(0xFF5C6BC0)

    // 30 stars with deterministic positions
    for (i in 0 until 30) {
        val x = ((i * 0.618033988f * 7f) % 1f) * size.width
        val y = ((i * 0.381966f * 13f) % 1f) * size.height
        val baseRadius = 0.8f + (i % 4) * 0.4f

        // Twinkle: different stars phase-shift differently
        val twinkle = sin(breathPhase * PI.toFloat() + i * 0.5f)
        val alpha = if (isDark) {
            0.3f + twinkle * 0.4f
        } else {
            0.15f + twinkle * 0.2f
        }

        drawCircle(
            color = starColor.copy(alpha = alpha.coerceIn(0.05f, 0.9f)),
            radius = baseRadius * (0.8f + twinkle * 0.2f),
            center = Offset(x, y)
        )
    }
}

// =============================================================================
// Utility
// =============================================================================

/** Simple linear interpolation between two colors. */
private fun lerpColor(a: Color, b: Color, t: Float): Color {
    return Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t
    )
}


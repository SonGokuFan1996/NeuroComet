package com.kyilmaz.neurocomet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * A calming, neurodivergent-friendly splash screen that adapts to the user's NeuroState.
 *
 * Design principles:
 * - Gentle, non-jarring animations
 * - Soothing color palette adapted to user's needs
 * - Affirmative, welcoming messaging personalized by state
 * - Predictable timing (longer for overwhelm, shorter for focus)
 * - No flashing or rapid movements
 *
 * @param onFinished Callback when splash is complete
 * @param neuroState The user's current NeuroState for personalization
 * @param minDurationMs Fallback minimum time (overridden by state config)
 */
@Composable
fun NeuroSplashScreen(
    onFinished: () -> Unit,
    neuroState: NeuroState = NeuroState.DEFAULT,
    minDurationMs: Long = 2000L
) {
    val config = remember(neuroState) { getSplashConfigForState(neuroState) }
    val effectiveDuration = config.durationMs

    var visible by remember { mutableStateOf(true) }
    var messageIndex by remember { mutableIntStateOf(0) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val logoScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        // Cycle through messages based on config
        val messageDelay = (effectiveDuration - 800) / config.messages.size.coerceAtLeast(1)
        config.messages.indices.forEach { index ->
            messageIndex = index
            delay(messageDelay)
        }

        visible = false
        delay(400)
        onFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            // Animated background based on style
            when (config.animationStyle) {
                SplashAnimationStyle.CALM_WAVES -> CalmWavesBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.FOCUS_PULSE -> FocusPulseBackground(primaryColor)
                SplashAnimationStyle.ROUTINE_GRID -> RoutineGridBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.ENERGY_BURST -> EnergyBurstBackground(primaryColor, secondaryColor, tertiaryColor)
                SplashAnimationStyle.GROUNDING_EARTH -> GroundingEarthBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.CREATIVE_SWIRL -> CreativeSwirlBackground(primaryColor, secondaryColor, tertiaryColor)
                SplashAnimationStyle.GENTLE_FLOAT -> GentleFloatBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.SENSORY_SPARKLE -> SensorySparkleBackground(primaryColor, secondaryColor, tertiaryColor)
                SplashAnimationStyle.CONTRAST_RINGS -> ContrastRingsBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.PATTERN_SHAPES -> PatternShapesBackground(primaryColor, secondaryColor)
                SplashAnimationStyle.RAINBOW_SPARKLE -> RainbowSparkleBackground()
            }

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Logo container
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    secondaryColor.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SplashIcon(
                        style = config.animationStyle,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "NeuroComet",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = config.tagline,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(48.dp))

                val messageAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "messageAlpha"
                )

                Text(
                    text = config.messages.getOrElse(messageIndex) { config.messages.first() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(messageAlpha)
                )

                Spacer(Modifier.height(16.dp))

                // Loading indicator matching the style
                when (config.animationStyle) {
                    SplashAnimationStyle.ENERGY_BURST,
                    SplashAnimationStyle.CREATIVE_SWIRL,
                    SplashAnimationStyle.SENSORY_SPARKLE -> BouncingDotsIndicator(primaryColor)
                    SplashAnimationStyle.ROUTINE_GRID,
                    SplashAnimationStyle.PATTERN_SHAPES -> SequentialBlocksIndicator(primaryColor)
                    SplashAnimationStyle.CONTRAST_RINGS -> ContrastRingsIndicator(primaryColor, secondaryColor)
                    else -> GentleLoadingIndicator(primaryColor)
                }
            }
        }
    }
}

/**
 * A calming background animation with slowly moving circles.
 * Uses very slow, predictable movements to avoid overstimulation.
 */
@Composable
private fun CalmingBackgroundAnimation(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnimation")

    // Very slow rotation for calming effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw soft, blurred circles that slowly rotate
        val radius1 = size.minDimension * 0.3f
        val radius2 = size.minDimension * 0.25f
        val radius3 = size.minDimension * 0.2f

        val angle1 = Math.toRadians(rotation.toDouble())
        val angle2 = Math.toRadians((rotation + 120).toDouble())
        val angle3 = Math.toRadians((rotation + 240).toDouble())

        val offset1 = size.minDimension * 0.15f
        val offset2 = size.minDimension * 0.12f
        val offset3 = size.minDimension * 0.1f

        drawCircle(
            color = primaryColor.copy(alpha = 0.05f),
            radius = radius1,
            center = Offset(
                centerX + (cos(angle1) * offset1).toFloat(),
                centerY + (sin(angle1) * offset1).toFloat()
            )
        )

        drawCircle(
            color = secondaryColor.copy(alpha = 0.04f),
            radius = radius2,
            center = Offset(
                centerX + (cos(angle2) * offset2).toFloat(),
                centerY + (sin(angle2) * offset2).toFloat()
            )
        )

        drawCircle(
            color = tertiaryColor.copy(alpha = 0.03f),
            radius = radius3,
            center = Offset(
                centerX + (cos(angle3) * offset3).toFloat(),
                centerY + (sin(angle3) * offset3).toFloat()
            )
        )
    }
}

/**
 * A custom neural network-inspired icon representing connection and neurodiversity.
 */
@Composable
private fun NeuralNetworkIcon(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val nodeRadius = size.minDimension * 0.08f
        val connectionRadius = size.minDimension * 0.35f * pulse

        // Draw connections (lines between nodes)
        val nodes = listOf(
            Offset(centerX, centerY - connectionRadius),
            Offset(centerX + connectionRadius * 0.866f, centerY + connectionRadius * 0.5f),
            Offset(centerX - connectionRadius * 0.866f, centerY + connectionRadius * 0.5f)
        )

        // Draw connecting lines
        nodes.forEachIndexed { i, node ->
            val nextNode = nodes[(i + 1) % nodes.size]
            drawLine(
                color = primaryColor.copy(alpha = 0.4f),
                start = node,
                end = nextNode,
                strokeWidth = 3f
            )
            // Connect to center
            drawLine(
                color = secondaryColor.copy(alpha = 0.3f),
                start = node,
                end = Offset(centerX, centerY),
                strokeWidth = 2f
            )
        }

        // Draw center node (larger, representing the self)
        drawCircle(
            color = primaryColor,
            radius = nodeRadius * 1.5f,
            center = Offset(centerX, centerY)
        )

        // Draw outer nodes
        nodes.forEach { node ->
            drawCircle(
                color = secondaryColor,
                radius = nodeRadius,
                center = node
            )
        }
    }
}

/**
 * A gentle, non-distracting loading indicator.
 * Uses a simple fade animation rather than spinning to reduce visual stress.
 */
@Composable
private fun GentleLoadingIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingIndicator")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing, delayMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Canvas(modifier = modifier.size(48.dp, 16.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = 16.dp.toPx()
        val startX = (size.width - spacing * 2) / 2

        drawCircle(
            color = color.copy(alpha = alpha1),
            radius = dotRadius,
            center = Offset(startX, size.height / 2)
        )

        drawCircle(
            color = color.copy(alpha = alpha2),
            radius = dotRadius,
            center = Offset(startX + spacing, size.height / 2)
        )

        drawCircle(
            color = color.copy(alpha = alpha3),
            radius = dotRadius,
            center = Offset(startX + spacing * 2, size.height / 2)
        )
    }
}

// ============================================================================
// NEW BACKGROUND ANIMATIONS FOR DIFFERENT NEURO STATES
// ============================================================================

@Composable
private fun CalmWavesBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (i in 0..2) {
            val path = Path()
            val waveHeight = height * 0.04f
            val yOffset = height * (0.4f + i * 0.15f)
            val phaseOffset = phase + i * 40

            path.moveTo(0f, yOffset)
            for (x in 0..width.toInt() step 10) {
                val y = yOffset + sin(Math.toRadians((x * 0.4 + phaseOffset).toDouble())).toFloat() * waveHeight
                path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = path,
                color = if (i % 2 == 0) primaryColor.copy(alpha = 0.08f) else secondaryColor.copy(alpha = 0.06f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun FocusPulseBackground(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * 0.4f

        for (i in 0..2) {
            val radius = maxRadius * scale * (0.6f + i * 0.2f)
            drawCircle(
                color = primaryColor.copy(alpha = 0.04f - i * 0.01f),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
private fun RoutineGridBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val highlight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridHighlight"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 60.dp.toPx()
        val cols = (size.width / gridSize).toInt() + 1
        val rows = (size.height / gridSize).toInt() + 1
        val highlightIndex = ((cols * rows) * highlight).toInt()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val isHighlighted = index == highlightIndex
                val alpha = if (isHighlighted) 0.15f else 0.03f

                drawCircle(
                    color = if (index % 2 == 0) primaryColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha * 0.7f),
                    radius = 4.dp.toPx(),
                    center = Offset(col * gridSize + gridSize / 2, row * gridSize + gridSize / 2)
                )
            }
        }
    }
}

@Composable
private fun EnergyBurstBackground(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "burst")
    val expansion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "burstExpansion"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.maxDimension * 0.6f
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)

        for (i in 0..2) {
            val delay = i * 0.2f
            val adjustedExpansion = ((expansion + delay) % 1f)
            val radius = maxRadius * adjustedExpansion
            val alpha = (1f - adjustedExpansion) * 0.08f

            drawCircle(
                color = colors[i].copy(alpha = alpha),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
private fun GroundingEarthBackground(primaryColor: Color, secondaryColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineCount = 5
        for (i in 0 until lineCount) {
            val y = size.height - (i * 40.dp.toPx()) - 60.dp.toPx()
            val alpha = 0.1f - (i * 0.015f)
            drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
        }

        drawCircle(
            color = secondaryColor.copy(alpha = 0.05f),
            radius = size.width * 0.8f,
            center = Offset(size.width / 2, size.height + size.width * 0.4f)
        )
    }
}

@Composable
private fun CreativeSwirlBackground(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "swirl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swirlRotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)

        for (i in 0..5) {
            val angle = Math.toRadians((rotation + i * 60).toDouble())
            val radius = size.minDimension * 0.25f
            val x = center.x + (cos(angle) * radius).toFloat()
            val y = center.y + (sin(angle) * radius).toFloat()

            drawCircle(
                color = colors[i % 3].copy(alpha = 0.06f),
                radius = 40.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun GentleFloatBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        drawCircle(
            color = primaryColor.copy(alpha = 0.05f),
            radius = size.minDimension * 0.3f,
            center = Offset(center.x, center.y - floatOffset)
        )

        drawCircle(
            color = secondaryColor.copy(alpha = 0.03f),
            radius = size.minDimension * 0.25f,
            center = Offset(center.x + 30, center.y + floatOffset * 0.5f)
        )
    }
}

@Composable
private fun SensorySparkleBackground(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val sparkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)
        val positions = listOf(
            0.1f to 0.2f, 0.8f to 0.15f, 0.3f to 0.4f, 0.7f to 0.5f, 0.2f to 0.7f,
            0.9f to 0.6f, 0.5f to 0.3f, 0.4f to 0.8f, 0.6f to 0.9f, 0.15f to 0.5f
        )

        positions.forEachIndexed { index, (xRatio, yRatio) ->
            val particleAlpha = sparkle * (if (index % 2 == 0) 1f else 0.7f) * 0.15f
            drawCircle(
                color = colors[index % 3].copy(alpha = particleAlpha),
                radius = (4 + index % 3 * 2).dp.toPx(),
                center = Offset(size.width * xRatio, size.height * yRatio)
            )
        }
    }
}

// ============================================================================
// SPLASH ICONS FOR DIFFERENT STATES
// ============================================================================

@Composable
private fun SplashIcon(
    style: SplashAnimationStyle,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconAnim")
    val animValue by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        when (style) {
            SplashAnimationStyle.CALM_WAVES -> {
                val path = Path()
                path.moveTo(size.width * 0.2f, centerY)
                path.cubicTo(
                    size.width * 0.35f, centerY - 20 * animValue,
                    size.width * 0.65f, centerY + 20 * animValue,
                    size.width * 0.8f, centerY
                )
                drawPath(path, primaryColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
            }

            SplashAnimationStyle.FOCUS_PULSE -> {
                drawCircle(primaryColor, radius = size.minDimension * 0.3f * animValue, center = Offset(centerX, centerY), style = Stroke(4f))
                drawCircle(secondaryColor, radius = size.minDimension * 0.15f, center = Offset(centerX, centerY))
            }

            SplashAnimationStyle.ROUTINE_GRID -> {
                val spacing = size.minDimension / 4
                for (row in 0..2) {
                    for (col in 0..2) {
                        val x = centerX - spacing + col * spacing
                        val y = centerY - spacing + row * spacing
                        drawCircle(
                            color = if ((row + col) % 2 == 0) primaryColor else secondaryColor,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            SplashAnimationStyle.ENERGY_BURST -> {
                for (i in 0..5) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val length = size.minDimension * 0.3f * animValue
                    drawLine(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        start = Offset(centerX, centerY),
                        end = Offset(
                            centerX + (cos(angle) * length).toFloat(),
                            centerY + (sin(angle) * length).toFloat()
                        ),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            SplashAnimationStyle.GROUNDING_EARTH -> {
                val path = Path()
                path.moveTo(size.width * 0.1f, size.height * 0.7f)
                path.lineTo(size.width * 0.4f, size.height * 0.3f)
                path.lineTo(size.width * 0.6f, size.height * 0.5f)
                path.lineTo(size.width * 0.9f, size.height * 0.2f)
                path.lineTo(size.width * 0.9f, size.height * 0.7f)
                path.close()
                drawPath(path, primaryColor.copy(alpha = 0.5f))
            }

            SplashAnimationStyle.CREATIVE_SWIRL -> {
                val path = Path()
                path.moveTo(centerX, centerY)
                for (i in 0..720 step 15) {
                    val angle = Math.toRadians(i.toDouble())
                    val radius = (i / 720f) * size.minDimension * 0.35f * animValue
                    path.lineTo(
                        centerX + (cos(angle) * radius).toFloat(),
                        centerY + (sin(angle) * radius).toFloat()
                    )
                }
                drawPath(path, primaryColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }

            SplashAnimationStyle.GENTLE_FLOAT -> {
                drawCircle(primaryColor.copy(alpha = 0.6f), radius = size.minDimension * 0.15f, center = Offset(centerX - 15, centerY))
                drawCircle(secondaryColor.copy(alpha = 0.5f), radius = size.minDimension * 0.12f, center = Offset(centerX + 15, centerY + 5))
                drawCircle(primaryColor.copy(alpha = 0.4f), radius = size.minDimension * 0.1f, center = Offset(centerX, centerY - 10))
            }

            SplashAnimationStyle.SENSORY_SPARKLE -> {
                val positions = listOf(
                    Offset(centerX, centerY - 25),
                    Offset(centerX + 20, centerY),
                    Offset(centerX, centerY + 25),
                    Offset(centerX - 20, centerY),
                    Offset(centerX, centerY)
                )
                positions.forEachIndexed { i, pos ->
                    val scale = if (i == 4) 1.2f else 0.8f
                    drawCircle(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        radius = 8f * scale * animValue,
                        center = pos
                    )
                }
            }

            SplashAnimationStyle.CONTRAST_RINGS -> {
                // Concentric rings with high contrast
                for (i in 3 downTo 1) {
                    val radius = size.minDimension * 0.12f * i * animValue
                    drawCircle(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 4f)
                    )
                }
                // Center dot
                drawCircle(
                    color = primaryColor,
                    radius = size.minDimension * 0.08f,
                    center = Offset(centerX, centerY)
                )
            }

            SplashAnimationStyle.PATTERN_SHAPES -> {
                // Geometric shapes for pattern recognition
                val shapeSize = size.minDimension * 0.15f
                // Square
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(centerX - shapeSize * 1.5f - shapeSize/2, centerY - shapeSize/2),
                    size = androidx.compose.ui.geometry.Size(shapeSize, shapeSize),
                    style = Stroke(width = 3f)
                )
                // Circle
                drawCircle(
                    color = secondaryColor,
                    radius = shapeSize / 2,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3f)
                )
                // Triangle
                val trianglePath = Path().apply {
                    moveTo(centerX + shapeSize * 1.5f, centerY - shapeSize/2)
                    lineTo(centerX + shapeSize * 1.5f + shapeSize, centerY + shapeSize/2)
                    lineTo(centerX + shapeSize * 1.5f - shapeSize, centerY + shapeSize/2)
                    close()
                }
                drawPath(trianglePath, primaryColor, style = Stroke(width = 3f))
            }

            SplashAnimationStyle.RAINBOW_SPARKLE -> {
                // Rainbow sparkle effect - magical celebration
                val rainbowColors = listOf(
                    Color(0xFF9C27B0), // Purple
                    Color(0xFFE91E63), // Pink
                    Color(0xFFFF5722), // Orange
                    Color(0xFFFFEB3B), // Yellow
                    Color(0xFF4CAF50), // Green
                    Color(0xFF2196F3), // Blue
                    Color(0xFF673AB7)  // Deep Purple
                )

                // Draw radiating rainbow lines
                rainbowColors.forEachIndexed { i, color ->
                    val angle = (360f / rainbowColors.size * i + animValue * 360) * (Math.PI / 180).toFloat()
                    val innerRadius = size.minDimension * 0.1f
                    val outerRadius = size.minDimension * 0.35f * animValue

                    drawLine(
                        color = color.copy(alpha = 0.8f),
                        start = Offset(
                            centerX + innerRadius * cos(angle),
                            centerY + innerRadius * sin(angle)
                        ),
                        end = Offset(
                            centerX + outerRadius * cos(angle),
                            centerY + outerRadius * sin(angle)
                        ),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                // Center sparkle
                drawCircle(
                    color = Color(0xFF9C27B0),
                    radius = size.minDimension * 0.08f * animValue,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

// ============================================================================
// ADDITIONAL LOADING INDICATORS
// ============================================================================

@Composable
private fun BouncingDotsIndicator(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounceDots")

    val offsets = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing, delayMillis = index * 150),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce$index"
        )
    }

    Canvas(modifier = modifier.size(48.dp, 24.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = 16.dp.toPx()
        val startX = (size.width - spacing * 2) / 2
        val baseY = size.height / 2 + 4.dp.toPx()

        offsets.forEachIndexed { index, offset ->
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(startX + index * spacing, baseY + offset.value)
            )
        }
    }
}

@Composable
private fun SequentialBlocksIndicator(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blocks")

    val fills = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing, delayMillis = index * 300),
                repeatMode = RepeatMode.Reverse
            ),
            label = "block$index"
        )
    }

    Canvas(modifier = modifier.size(56.dp, 16.dp)) {
        val blockWidth = 12.dp.toPx()
        val blockHeight = 12.dp.toPx()
        val spacing = 6.dp.toPx()
        val startX = (size.width - (blockWidth * 3 + spacing * 2)) / 2

        fills.forEachIndexed { index, fill ->
            drawRect(
                color = color.copy(alpha = fill.value),
                topLeft = Offset(startX + index * (blockWidth + spacing), (size.height - blockHeight) / 2),
                size = androidx.compose.ui.geometry.Size(blockWidth, blockHeight)
            )
        }
    }
}

// ============================================================================
// COLORBLIND-FRIENDLY ANIMATIONS
// ============================================================================

/**
 * High contrast concentric rings animation for colorblind users.
 * Uses distinct ring patterns rather than color differentiation.
 */
@Composable
private fun ContrastRingsBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val expansion by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringExpansion"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * 0.4f

        // Draw alternating colored rings
        for (i in 5 downTo 1) {
            val radius = maxRadius * (i / 5f) * expansion
            val strokeWidth = if (i % 2 == 0) 2.5f else 4f
            drawCircle(
                color = if (i % 2 == 0) primaryColor.copy(alpha = 0.12f) else secondaryColor.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Geometric pattern animation for monochromacy/achromatopsia.
 * Uses shapes and patterns instead of colors for differentiation.
 */
@Composable
private fun PatternShapesBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "shapes")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shapeRotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val shapeSize = size.minDimension * 0.08f

        // Draw geometric shapes in a circular pattern
        for (i in 0..5) {
            val angle = Math.toRadians((rotation + i * 60).toDouble())
            val distance = size.minDimension * 0.25f
            val x = center.x + (cos(angle) * distance).toFloat()
            val y = center.y + (sin(angle) * distance).toFloat()

            when (i % 3) {
                0 -> {
                    // Square
                    drawRect(
                        color = primaryColor.copy(alpha = 0.08f),
                        topLeft = Offset(x - shapeSize/2, y - shapeSize/2),
                        size = androidx.compose.ui.geometry.Size(shapeSize, shapeSize)
                    )
                }
                1 -> {
                    // Circle
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.06f),
                        radius = shapeSize / 2,
                        center = Offset(x, y)
                    )
                }
                2 -> {
                    // Triangle
                    val path = Path().apply {
                        moveTo(x, y - shapeSize/2)
                        lineTo(x + shapeSize/2, y + shapeSize/2)
                        lineTo(x - shapeSize/2, y + shapeSize/2)
                        close()
                    }
                    drawPath(path, primaryColor.copy(alpha = 0.07f))
                }
            }
        }
    }
}

/**
 * Alternating rings loading indicator for colorblind modes.
 * Uses both color and pattern differentiation.
 */
@Composable
private fun ContrastRingsIndicator(primaryColor: Color, secondaryColor: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ringsIndicator")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Canvas(modifier = modifier.size(48.dp, 24.dp)) {
        val centerY = size.height / 2
        val spacing = 16.dp.toPx()
        val startX = (size.width - spacing * 2) / 2

        for (i in 0..2) {
            val x = startX + i * spacing
            val radius = 6.dp.toPx() * (if (i == 1) scale else 1f)

            // Outer ring
            drawCircle(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                radius = radius,
                center = Offset(x, centerY),
                style = Stroke(width = 2f)
            )
            // Inner dot
            drawCircle(
                color = if (i % 2 == 0) secondaryColor else primaryColor,
                radius = radius * 0.4f,
                center = Offset(x, centerY)
            )
        }
    }
}

// ============================================================================
// SECRET THEME: RAINBOW BRAIN ANIMATION
// ============================================================================

/**
 * Magical rainbow sparkle animation for the secret Rainbow Brain theme.
 * Celebrates neurodiversity with vibrant, joyful colors.
 * Uses smooth, calming animation speeds consistent with the app.
 */
@Composable
private fun RainbowSparkleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbowSparkle")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowRotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rainbowPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * 0.4f

        val rainbowColors = listOf(
            Color(0xFF9C27B0), // Purple (neurodiversity symbol)
            Color(0xFFE91E63), // Pink
            Color(0xFFFF5722), // Orange
            Color(0xFFFFEB3B), // Yellow
            Color(0xFF4CAF50), // Green
            Color(0xFF2196F3), // Blue
            Color(0xFF673AB7)  // Deep Purple
        )

        // Draw rotating rainbow rays
        rainbowColors.forEachIndexed { i, color ->
            val angle = Math.toRadians((rotation + i * (360.0 / rainbowColors.size))).toFloat()
            val innerRadius = maxRadius * 0.2f
            val outerRadius = maxRadius * pulse

            drawLine(
                color = color.copy(alpha = 0.12f),
                start = Offset(
                    center.x + innerRadius * cos(angle),
                    center.y + innerRadius * sin(angle)
                ),
                end = Offset(
                    center.x + outerRadius * cos(angle),
                    center.y + outerRadius * sin(angle)
                ),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Draw pulsing center circles
        for (i in 3 downTo 1) {
            val radius = maxRadius * 0.15f * i * pulse
            val colorIndex = i % rainbowColors.size
            drawCircle(
                color = rainbowColors[colorIndex].copy(alpha = 0.08f - i * 0.015f),
                radius = radius,
                center = center
            )
        }

        // Sparkle particles around the center
        for (i in 0..11) {
            val sparkleAngle = Math.toRadians((rotation * 1.5 + i * 30).toDouble()).toFloat()
            val distance = maxRadius * 0.35f * (0.8f + (i % 3) * 0.1f)
            val sparkleX = center.x + distance * cos(sparkleAngle)
            val sparkleY = center.y + distance * sin(sparkleAngle)
            val sparkleSize = 3f * (1f + (i % 2) * 0.5f) * pulse

            drawCircle(
                color = rainbowColors[i % rainbowColors.size].copy(alpha = 0.25f),
                radius = sparkleSize,
                center = Offset(sparkleX, sparkleY)
            )
        }
    }
}



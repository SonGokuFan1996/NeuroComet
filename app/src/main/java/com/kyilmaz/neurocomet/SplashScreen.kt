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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    val context = LocalContext.current
    val config = remember(neuroState) { getSplashConfigForState(neuroState) }
    val effectiveDuration = config.durationMs
    val messages = remember(config, context) { config.getMessages(context) }
    val tagline = remember(config, context) { config.getTagline(context) }

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
        val messageDelay = (effectiveDuration - 800) / messages.size.coerceAtLeast(1)
        messages.indices.forEach { index ->
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
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = tagline,
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
                    text = messages.getOrElse(messageIndex) { messages.first() },
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
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw soft, layered ocean-like waves from bottom
        for (layer in 0..4) {
            val path = Path()
            val waveHeight = height * (0.015f + layer * 0.008f)
            val baseY = height * (0.65f + layer * 0.07f)
            val phaseOffset = phase + layer * 25
            val frequency = 0.008f - layer * 0.001f
            val alpha = 0.08f - layer * 0.012f

            path.moveTo(-50f, height)
            path.lineTo(-50f, baseY)

            // Smooth sinusoidal wave with multiple harmonics for natural feel
            var x = -50f
            while (x <= width + 50) {
                val primary = sin(Math.toRadians((x * frequency * 100 + phaseOffset).toDouble())).toFloat()
                val secondary = sin(Math.toRadians((x * frequency * 50 + phaseOffset * 0.7).toDouble())).toFloat() * 0.3f
                val y = baseY + (primary + secondary) * waveHeight
                path.lineTo(x, y)
                x += 3f
            }

            path.lineTo(width + 50, height)
            path.close()

            drawPath(
                path = path,
                color = if (layer % 2 == 0) primaryColor.copy(alpha = alpha)
                       else secondaryColor.copy(alpha = alpha * 0.8f)
            )
        }

        // Gentle floating orbs above waves
        for (i in 0..2) {
            val orbPhase = (phase * 0.3f + i * 120) % 360
            val orbX = width * (0.2f + i * 0.3f) + sin(Math.toRadians(orbPhase.toDouble())).toFloat() * 15
            val orbY = height * 0.35f + cos(Math.toRadians((orbPhase * 0.5).toDouble())).toFloat() * 20

            drawCircle(
                color = primaryColor.copy(alpha = 0.04f),
                radius = 40f + i * 15f,
                center = Offset(orbX, orbY)
            )
        }
    }
}

@Composable
private fun FocusPulseBackground(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing, delayMillis = 1333),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing, delayMillis = 2666),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * 0.45f

        // Draw expanding and fading pulse rings
        listOf(pulse1, pulse2, pulse3).forEach { pulse ->
            val radius = maxRadius * pulse
            val alpha = (1f - pulse) * 0.12f
            val strokeWidth = 3f * (1f - pulse * 0.5f)

            drawCircle(
                color = primaryColor.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }

        // Stable center glow that breathes subtly
        val breathe = (pulse1 * 0.1f) + 0.95f
        drawCircle(
            color = primaryColor.copy(alpha = 0.06f),
            radius = size.minDimension * 0.15f * breathe,
            center = center
        )
        drawCircle(
            color = primaryColor.copy(alpha = 0.03f),
            radius = size.minDimension * 0.25f * breathe,
            center = center
        )
    }
}

@Composable
private fun RoutineGridBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridWave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 48.dp.toPx()
        val cols = (size.width / gridSize).toInt() + 2
        val rows = (size.height / gridSize).toInt() + 2
        val totalDots = cols * rows
        val wavePosition = wave * (cols + rows) // Wave moves diagonally

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * gridSize + gridSize / 2
                val y = row * gridSize + gridSize / 2

                // Calculate distance from the wave diagonal
                val dotDiagonal = (col + row).toFloat()
                val distFromWave = kotlin.math.abs(dotDiagonal - wavePosition)

                // Dots light up as wave passes
                val isNearWave = distFromWave < 2f
                val waveIntensity = if (isNearWave) (1f - distFromWave / 2f) * 0.15f else 0f
                val baseAlpha = 0.04f
                val finalAlpha = baseAlpha + waveIntensity

                // Alternate colors in a checkerboard pattern for visual structure
                val useSecondary = (row + col) % 2 == 0
                val color = if (useSecondary) secondaryColor else primaryColor

                // Dot size varies slightly based on wave
                val dotRadius = 3.dp.toPx() * (1f + waveIntensity * 0.5f)

                drawCircle(
                    color = color.copy(alpha = finalAlpha),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }

        // Subtle connecting lines for structure (very faint)
        for (row in 0 until rows step 2) {
            drawLine(
                color = primaryColor.copy(alpha = 0.02f),
                start = Offset(0f, row * gridSize + gridSize / 2),
                end = Offset(size.width, row * gridSize + gridSize / 2),
                strokeWidth = 1f
            )
        }
        for (col in 0 until cols step 2) {
            drawLine(
                color = secondaryColor.copy(alpha = 0.02f),
                start = Offset(col * gridSize + gridSize / 2, 0f),
                end = Offset(col * gridSize + gridSize / 2, size.height),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun EnergyBurstBackground(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "burst")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "burstRotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "burstPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val rayCount = 12
        val maxLength = size.minDimension * 0.42f
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)

        // Draw soft radial gradient background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.08f),
                    secondaryColor.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                center = center,
                radius = size.minDimension * 0.5f
            ),
            radius = size.minDimension * 0.5f,
            center = center
        )

        // Draw energy rays emanating from center
        for (i in 0 until rayCount) {
            val baseAngle = (360f / rayCount * i) + rotation
            val angle = Math.toRadians(baseAngle.toDouble())
            val rayLength = maxLength * pulse * (0.7f + (i % 3) * 0.15f)
            val color = colors[i % colors.size]

            // Ray glow (wider, more transparent)
            drawLine(
                color = color.copy(alpha = 0.06f),
                start = center,
                end = Offset(
                    center.x + (cos(angle) * rayLength).toFloat(),
                    center.y + (sin(angle) * rayLength).toFloat()
                ),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )

            // Main ray
            drawLine(
                color = color.copy(alpha = 0.12f),
                start = center,
                end = Offset(
                    center.x + (cos(angle) * rayLength).toFloat(),
                    center.y + (sin(angle) * rayLength).toFloat()
                ),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Small particle at end of ray
            drawCircle(
                color = color.copy(alpha = 0.15f),
                radius = 5f + (i % 3) * 2f,
                center = Offset(
                    center.x + (cos(angle) * rayLength).toFloat(),
                    center.y + (sin(angle) * rayLength).toFloat()
                )
            )
        }

        // Center energy orb
        drawCircle(
            color = primaryColor.copy(alpha = 0.1f),
            radius = 25f * pulse,
            center = center
        )
    }
}

@Composable
private fun GroundingEarthBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "ground")
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "groundBreathe"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw layered earth strata at the bottom - represents grounding
        for (layer in 0..5) {
            val path = Path()
            val baseY = height * (0.75f + layer * 0.05f)
            val waveAmplitude = 8f - layer * 1f

            path.moveTo(-20f, height)
            path.lineTo(-20f, baseY)

            var x = -20f
            while (x <= width + 20) {
                val y = baseY + sin(x * 0.01 + layer * 0.5).toFloat() * waveAmplitude * breathe
                path.lineTo(x, y)
                x += 5f
            }

            path.lineTo(width + 20, height)
            path.close()

            val alpha = 0.05f - layer * 0.006f
            drawPath(
                path = path,
                color = if (layer % 2 == 0) primaryColor.copy(alpha = alpha)
                       else secondaryColor.copy(alpha = alpha * 0.8f)
            )
        }

        // Stable vertical anchor lines - roots
        val rootCount = 7
        for (i in 0 until rootCount) {
            val x = width * (0.15f + i * 0.12f)
            val startY = height * 0.7f
            val length = height * (0.15f + (i % 3) * 0.05f)

            drawLine(
                color = primaryColor.copy(alpha = 0.03f),
                start = Offset(x, startY),
                end = Offset(x + (i % 2 * 2 - 1) * 5f, startY + length),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Central grounding circle - represents the present moment
        drawCircle(
            color = secondaryColor.copy(alpha = 0.04f),
            radius = size.minDimension * 0.25f * breathe,
            center = Offset(width / 2, height * 0.45f)
        )
        drawCircle(
            color = primaryColor.copy(alpha = 0.06f),
            radius = size.minDimension * 0.12f * breathe,
            center = Offset(width / 2, height * 0.45f)
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
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swirlRotation"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swirlBreathe"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)
        val maxRadius = size.minDimension * 0.4f

        // Draw multiple flowing spiral arms
        for (arm in 0..2) {
            val armOffset = arm * 120f
            val path = Path()
            var isFirst = true

            // Create a logarithmic spiral for each arm
            for (t in 0..720 step 5) {
                val angle = Math.toRadians((t + rotation + armOffset).toDouble())
                val radius = (t / 720f) * maxRadius * breathe
                val x = center.x + (cos(angle) * radius).toFloat()
                val y = center.y + (sin(angle) * radius).toFloat()

                if (isFirst) {
                    path.moveTo(x, y)
                    isFirst = false
                } else {
                    path.lineTo(x, y)
                }
            }

            // Glow layer
            drawPath(
                path = path,
                color = colors[arm].copy(alpha = 0.04f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
            // Main spiral
            drawPath(
                path = path,
                color = colors[arm].copy(alpha = 0.08f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // Floating creative orbs at varying distances
        for (i in 0..5) {
            val orbitAngle = Math.toRadians((rotation * 0.5 + i * 60).toDouble())
            val orbitRadius = size.minDimension * (0.15f + (i % 3) * 0.08f)
            val x = center.x + (cos(orbitAngle) * orbitRadius).toFloat()
            val y = center.y + (sin(orbitAngle) * orbitRadius).toFloat()

            drawCircle(
                color = colors[i % 3].copy(alpha = 0.06f),
                radius = 20f + (i % 3) * 10f,
                center = Offset(x, y)
            )
        }

        // Center creativity core
        drawCircle(
            color = primaryColor.copy(alpha = 0.05f),
            radius = 30f * breathe,
            center = center
        )
    }
}

@Composable
private fun GentleFloatBackground(primaryColor: Color, secondaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatPhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Multiple gently floating bubbles at different depths
        val bubbles = listOf(
            Triple(0.3f, 0.3f, 0.08f),  // x ratio, y ratio, size ratio
            Triple(0.7f, 0.25f, 0.06f),
            Triple(0.2f, 0.6f, 0.10f),
            Triple(0.8f, 0.65f, 0.07f),
            Triple(0.5f, 0.45f, 0.12f),
            Triple(0.35f, 0.75f, 0.05f),
            Triple(0.65f, 0.8f, 0.04f)
        )

        bubbles.forEachIndexed { index, (xRatio, yRatio, sizeRatio) ->
            val phaseOffset = index * 50f
            val floatAmount = 12f * (1f + (index % 3) * 0.3f)

            // Calculate gentle floating motion
            val xOffset = sin(Math.toRadians((phase + phaseOffset).toDouble())).toFloat() * floatAmount * 0.5f
            val yOffset = sin(Math.toRadians((phase * 0.7 + phaseOffset * 1.3).toDouble())).toFloat() * floatAmount

            val x = width * xRatio + xOffset
            val y = height * yRatio + yOffset
            val radius = size.minDimension * sizeRatio

            // Outer glow
            drawCircle(
                color = if (index % 2 == 0) primaryColor.copy(alpha = 0.03f)
                       else secondaryColor.copy(alpha = 0.025f),
                radius = radius * 1.5f,
                center = Offset(x, y)
            )
            // Inner bubble
            drawCircle(
                color = if (index % 2 == 0) primaryColor.copy(alpha = 0.05f)
                       else secondaryColor.copy(alpha = 0.04f),
                radius = radius,
                center = Offset(x, y)
            )
            // Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.03f),
                radius = radius * 0.3f,
                center = Offset(x - radius * 0.2f, y - radius * 0.2f)
            )
        }

        // Very subtle connecting wisps between some bubbles
        val path = Path().apply {
            moveTo(width * 0.3f, height * 0.3f)
            quadraticBezierTo(width * 0.5f, height * 0.35f, width * 0.5f, height * 0.45f)
        }
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.02f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun SensorySparkleBackground(primaryColor: Color, secondaryColor: Color, tertiaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparklePhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val colors = listOf(primaryColor, secondaryColor, tertiaryColor)

        // Create a field of twinkling star-like particles
        val stars = listOf(
            Triple(0.1f, 0.15f, 0.0f), Triple(0.85f, 0.1f, 0.1f), Triple(0.25f, 0.35f, 0.2f),
            Triple(0.7f, 0.28f, 0.3f), Triple(0.15f, 0.55f, 0.4f), Triple(0.9f, 0.45f, 0.5f),
            Triple(0.4f, 0.2f, 0.6f), Triple(0.55f, 0.65f, 0.7f), Triple(0.3f, 0.8f, 0.8f),
            Triple(0.75f, 0.75f, 0.9f), Triple(0.5f, 0.42f, 0.15f), Triple(0.2f, 0.68f, 0.35f),
            Triple(0.8f, 0.58f, 0.55f), Triple(0.45f, 0.88f, 0.75f), Triple(0.65f, 0.12f, 0.95f)
        )

        stars.forEachIndexed { index, (xRatio, yRatio, timeOffset) ->
            // Each star twinkles at its own rhythm
            val twinkle = sin(Math.toRadians(((phase + timeOffset) * 360).toDouble())).toFloat()
            val baseAlpha = 0.04f + (index % 3) * 0.02f
            val alpha = baseAlpha + twinkle * 0.08f
            val baseRadius = 4f + (index % 4) * 2f
            val radius = baseRadius * (1f + twinkle * 0.3f)

            val x = width * xRatio
            val y = height * yRatio
            val color = colors[index % 3]

            // Glow around star
            drawCircle(
                color = color.copy(alpha = alpha * 0.4f),
                radius = radius * 2.5f,
                center = Offset(x, y)
            )

            // Star core
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )

            // Draw subtle cross sparkle for larger stars
            if (index % 3 == 0 && twinkle > 0.5f) {
                val sparkleLength = radius * 3f * twinkle
                drawLine(
                    color = color.copy(alpha = alpha * 0.5f),
                    start = Offset(x - sparkleLength, y),
                    end = Offset(x + sparkleLength, y),
                    strokeWidth = 1f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color.copy(alpha = alpha * 0.5f),
                    start = Offset(x, y - sparkleLength),
                    end = Offset(x, y + sparkleLength),
                    strokeWidth = 1f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Subtle nebula-like background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(width * 0.3f, height * 0.4f),
                radius = width * 0.4f
            ),
            radius = width * 0.4f,
            center = Offset(width * 0.3f, height * 0.4f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                center = Offset(width * 0.7f, height * 0.6f),
                radius = width * 0.35f
            ),
            radius = width * 0.35f,
            center = Offset(width * 0.7f, height * 0.6f)
        )
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

    // Define the neural network logo colors based on style
    val (nodeColor, connection1Color, connection2Color, connection3Color) = when (style) {
        SplashAnimationStyle.RAINBOW_SPARKLE -> {
            listOf(
                Color(0xFFFFEB3B), // Yellow center
                Color(0xFFE91E63), // Pink
                Color(0xFF2196F3), // Blue
                Color(0xFF4CAF50)  // Green
            )
        }
        else -> {
            listOf(
                primaryColor,
                secondaryColor,
                primaryColor.copy(alpha = 0.7f),
                secondaryColor.copy(alpha = 0.7f)
            )
        }
    }

    Canvas(modifier = modifier) {
        drawNeuroCometLogo(
            centerX = size.width / 2,
            centerY = size.height / 2,
            scale = size.minDimension / 108f * animValue,
            nodeColor = nodeColor,
            connection1Color = connection1Color,
            connection2Color = connection2Color,
            connection3Color = connection3Color
        )
    }
}

/**
 * Draws the NeuroComet neural network logo.
 * This matches the design from neuro_comet_icon_foreground_vector.xml:
 * - Gold center node with glow rings
 * - Three neural connections (pink top-left, blue top-right, green bottom)
 * - Small nodes at the end of each connection
 */
private fun DrawScope.drawNeuroCometLogo(
    centerX: Float,
    centerY: Float,
    scale: Float,
    nodeColor: Color,
    connection1Color: Color,
    connection2Color: Color,
    connection3Color: Color
) {
    // Outer glow ring
    drawCircle(
        color = nodeColor.copy(alpha = 0.15f),
        radius = 38f * scale,
        center = Offset(centerX, centerY)
    )

    // Middle glow ring
    drawCircle(
        color = nodeColor.copy(alpha = 0.25f),
        radius = 32f * scale,
        center = Offset(centerX, centerY)
    )

    // Neural connection 1 (top-left) with glow
    val connection1End = Offset(centerX - 35f * scale, centerY - 35f * scale)
    val connection1Path = Path().apply {
        moveTo(connection1End.x, connection1End.y)
        quadraticBezierTo(centerX - 10f * scale, centerY, centerX, centerY)
    }
    // Glow
    drawPath(
        path = connection1Path,
        color = connection1Color.copy(alpha = 0.3f),
        style = Stroke(width = 10f * scale, cap = StrokeCap.Round)
    )
    // Main line
    drawPath(
        path = connection1Path,
        color = connection1Color,
        style = Stroke(width = 5f * scale, cap = StrokeCap.Round)
    )

    // Neural connection 2 (top-right) with glow
    val connection2End = Offset(centerX + 35f * scale, centerY - 35f * scale)
    val connection2Path = Path().apply {
        moveTo(connection2End.x, connection2End.y)
        quadraticBezierTo(centerX + 10f * scale, centerY, centerX, centerY)
    }
    // Glow
    drawPath(
        path = connection2Path,
        color = connection2Color.copy(alpha = 0.3f),
        style = Stroke(width = 10f * scale, cap = StrokeCap.Round)
    )
    // Main line
    drawPath(
        path = connection2Path,
        color = connection2Color,
        style = Stroke(width = 5f * scale, cap = StrokeCap.Round)
    )

    // Neural connection 3 (bottom) with glow
    val connection3End = Offset(centerX, centerY + 40f * scale)
    val connection3Path = Path().apply {
        moveTo(connection3End.x, connection3End.y)
        quadraticBezierTo(centerX, centerY + 10f * scale, centerX, centerY)
    }
    // Glow
    drawPath(
        path = connection3Path,
        color = connection3Color.copy(alpha = 0.3f),
        style = Stroke(width = 10f * scale, cap = StrokeCap.Round)
    )
    // Main line
    drawPath(
        path = connection3Path,
        color = connection3Color,
        style = Stroke(width = 5f * scale, cap = StrokeCap.Round)
    )

    // Small node at end of connection 1
    drawCircle(
        color = connection1Color,
        radius = 5f * scale,
        center = connection1End
    )

    // Small node at end of connection 2
    drawCircle(
        color = connection2Color,
        radius = 5f * scale,
        center = connection2End
    )

    // Small node at end of connection 3
    drawCircle(
        color = connection3Color,
        radius = 5f * scale,
        center = connection3End
    )

    // Center circle - main node
    drawCircle(
        color = nodeColor,
        radius = 25f * scale,
        center = Offset(centerX, centerY)
    )

    // Inner highlight on center node
    drawCircle(
        color = nodeColor.copy(alpha = 0.7f),
        radius = 8f * scale,
        center = Offset(centerX - 8f * scale, centerY - 8f * scale)
    )
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
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringExpansion"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * 0.42f

        // Draw multiple concentric rings with varying patterns
        for (i in 7 downTo 1) {
            val radius = maxRadius * (i / 7f) * expansion
            val isDashed = i % 2 == 0
            val strokeWidth = if (isDashed) 2f else 3.5f
            val alpha = 0.04f + (7 - i) * 0.015f
            val color = if (i % 2 == 0) primaryColor else secondaryColor

            if (isDashed) {
                // Create dashed ring effect using segments
                val segments = 24
                for (seg in 0 until segments step 2) {
                    val startAngle = (seg * (360f / segments) + rotation * 0.5f) % 360f
                    val sweepAngle = 360f / segments - 2f

                    val startRad = Math.toRadians(startAngle.toDouble())
                    val endRad = Math.toRadians((startAngle + sweepAngle).toDouble())

                    val path = Path()
                    path.moveTo(
                        center.x + (cos(startRad) * radius).toFloat(),
                        center.y + (sin(startRad) * radius).toFloat()
                    )

                    // Draw arc segment
                    for (step in 0..10) {
                        val angle = startRad + (endRad - startRad) * (step / 10.0)
                        path.lineTo(
                            center.x + (cos(angle) * radius).toFloat(),
                            center.y + (sin(angle) * radius).toFloat()
                        )
                    }

                    drawPath(
                        path = path,
                        color = color.copy(alpha = alpha),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            } else {
                // Solid ring
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Central focal point with high contrast
        drawCircle(
            color = primaryColor.copy(alpha = 0.08f),
            radius = maxRadius * 0.12f * expansion,
            center = center
        )
        drawCircle(
            color = secondaryColor.copy(alpha = 0.12f),
            radius = maxRadius * 0.06f * expansion,
            center = center
        )
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
            animation = tween(durationMillis = 45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shapeRotation"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shapeBreathe"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        // Draw outer ring of shapes (larger, slower)
        val outerCount = 8
        val outerDistance = size.minDimension * 0.35f * breathe
        val outerSize = size.minDimension * 0.06f

        for (i in 0 until outerCount) {
            val angle = Math.toRadians((rotation * 0.3 + i * (360.0 / outerCount)).toDouble())
            val x = center.x + (cos(angle) * outerDistance).toFloat()
            val y = center.y + (sin(angle) * outerDistance).toFloat()

            when (i % 4) {
                0 -> {
                    // Square (solid fill)
                    drawRect(
                        color = primaryColor.copy(alpha = 0.06f),
                        topLeft = Offset(x - outerSize/2, y - outerSize/2),
                        size = androidx.compose.ui.geometry.Size(outerSize, outerSize)
                    )
                }
                1 -> {
                    // Circle (outline only - pattern differentiation)
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.08f),
                        radius = outerSize / 2,
                        center = Offset(x, y),
                        style = Stroke(width = 2f)
                    )
                }
                2 -> {
                    // Triangle (solid fill)
                    val path = Path().apply {
                        moveTo(x, y - outerSize/2)
                        lineTo(x + outerSize/2, y + outerSize/2)
                        lineTo(x - outerSize/2, y + outerSize/2)
                        close()
                    }
                    drawPath(path, primaryColor.copy(alpha = 0.07f))
                }
                3 -> {
                    // Diamond (outline only)
                    val path = Path().apply {
                        moveTo(x, y - outerSize/2)
                        lineTo(x + outerSize/2, y)
                        lineTo(x, y + outerSize/2)
                        lineTo(x - outerSize/2, y)
                        close()
                    }
                    drawPath(path, secondaryColor.copy(alpha = 0.07f), style = Stroke(width = 2f))
                }
            }
        }

        // Draw inner ring of shapes (smaller, opposite rotation)
        val innerCount = 6
        val innerDistance = size.minDimension * 0.18f * breathe
        val innerSize = size.minDimension * 0.04f

        for (i in 0 until innerCount) {
            val angle = Math.toRadians((-rotation * 0.5 + i * (360.0 / innerCount)).toDouble())
            val x = center.x + (cos(angle) * innerDistance).toFloat()
            val y = center.y + (sin(angle) * innerDistance).toFloat()

            when (i % 3) {
                0 -> {
                    // Filled square
                    drawRect(
                        color = primaryColor.copy(alpha = 0.05f),
                        topLeft = Offset(x - innerSize/2, y - innerSize/2),
                        size = androidx.compose.ui.geometry.Size(innerSize, innerSize)
                    )
                }
                1 -> {
                    // Filled circle
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.05f),
                        radius = innerSize / 2,
                        center = Offset(x, y)
                    )
                }
                2 -> {
                    // Cross/plus pattern
                    val halfSize = innerSize / 2
                    drawLine(
                        color = primaryColor.copy(alpha = 0.06f),
                        start = Offset(x - halfSize, y),
                        end = Offset(x + halfSize, y),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.06f),
                        start = Offset(x, y - halfSize),
                        end = Offset(x, y + halfSize),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Central element - hexagon
        val centerSize = size.minDimension * 0.05f * breathe
        val hexPath = Path().apply {
            for (i in 0..5) {
                val angle = Math.toRadians((i * 60 + rotation * 0.2).toDouble())
                val px = center.x + (cos(angle) * centerSize).toFloat()
                val py = center.y + (sin(angle) * centerSize).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(hexPath, primaryColor.copy(alpha = 0.08f), style = Stroke(width = 2f))
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
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbowRotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rainbowPulse"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val maxRadius = size.minDimension * 0.42f

        val rainbowColors = listOf(
            Color(0xFFE040FB), // Vibrant Purple (neurodiversity symbol)
            Color(0xFFFF4081), // Pink
            Color(0xFFFF6E40), // Deep Orange
            Color(0xFFFFD740), // Amber/Yellow
            Color(0xFF69F0AE), // Green
            Color(0xFF40C4FF), // Light Blue
            Color(0xFF7C4DFF)  // Deep Purple/Violet
        )

        // Soft radial glow at center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE040FB).copy(alpha = 0.08f),
                    Color(0xFF40C4FF).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius
            ),
            radius = maxRadius,
            center = center
        )

        // Draw flowing rainbow arcs with glow
        rainbowColors.forEachIndexed { i, color ->
            val arcRadius = maxRadius * (0.4f + i * 0.08f) * pulse
            val startAngle = rotation + i * 15f
            val sweepAngle = 120f + (i % 3) * 20f

            // Arc glow (wider)
            val glowPath = Path()
            for (step in 0..20) {
                val angle = Math.toRadians((startAngle + sweepAngle * step / 20f).toDouble())
                val x = center.x + (cos(angle) * arcRadius).toFloat()
                val y = center.y + (sin(angle) * arcRadius).toFloat()
                if (step == 0) glowPath.moveTo(x, y) else glowPath.lineTo(x, y)
            }
            drawPath(
                path = glowPath,
                color = color.copy(alpha = 0.04f),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Main arc
            drawPath(
                path = glowPath,
                color = color.copy(alpha = 0.12f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }

        // Floating sparkle orbs at various distances
        val sparklePositions = listOf(
            Triple(0.15f, 0.25f, 0), Triple(0.85f, 0.2f, 1), Triple(0.1f, 0.7f, 2),
            Triple(0.9f, 0.75f, 3), Triple(0.3f, 0.15f, 4), Triple(0.7f, 0.85f, 5),
            Triple(0.25f, 0.55f, 6), Triple(0.75f, 0.45f, 0), Triple(0.5f, 0.1f, 1),
            Triple(0.5f, 0.9f, 2)
        )

        sparklePositions.forEach { (xRatio, yRatio, colorIndex) ->
            val x = width * xRatio + sin((shimmer + xRatio) * Math.PI * 2).toFloat() * 8f
            val y = height * yRatio + cos((shimmer + yRatio) * Math.PI * 2).toFloat() * 8f
            val sparkleAlpha = 0.1f + sin((shimmer + xRatio + yRatio) * Math.PI * 2).toFloat() * 0.08f

            // Outer glow
            drawCircle(
                color = rainbowColors[colorIndex].copy(alpha = sparkleAlpha * 0.3f),
                radius = 18f,
                center = Offset(x, y)
            )
            // Core
            drawCircle(
                color = rainbowColors[colorIndex].copy(alpha = sparkleAlpha),
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Pulsing center with rainbow gradient
        for (ring in 3 downTo 1) {
            val ringRadius = maxRadius * 0.12f * ring * pulse
            val colorIndex = ring % rainbowColors.size
            drawCircle(
                color = rainbowColors[colorIndex].copy(alpha = 0.06f - ring * 0.012f),
                radius = ringRadius,
                center = center
            )
        }

        // Central neurodiversity infinity symbol suggestion (simplified as overlapping circles)
        val symbolSize = size.minDimension * 0.06f * pulse
        drawCircle(
            color = Color(0xFFE040FB).copy(alpha = 0.08f),
            radius = symbolSize,
            center = Offset(center.x - symbolSize * 0.6f, center.y)
        )
        drawCircle(
            color = Color(0xFF40C4FF).copy(alpha = 0.08f),
            radius = symbolSize,
            center = Offset(center.x + symbolSize * 0.6f, center.y)
        )
    }
}



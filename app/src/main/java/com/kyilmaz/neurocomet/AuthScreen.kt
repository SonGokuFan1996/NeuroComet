package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentication screen for user sign-in and sign-up.
 *
 * Features:
 * - Flashy neurodivergent-themed design
 * - Animated rainbow infinity symbol
 * - Calming gradient background
 * - Email/password authentication
 * - Sign up with age verification
 * - 2FA verification support
 * - Skip option for guest access
 */
@Composable
fun AuthScreen(
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, Audience?) -> Unit,
    onVerify2FA: (String) -> Unit,
    is2FARequired: Boolean,
    error: String?,
    showDevBypass: Boolean = false,
    onSkip: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var twoFactorCode by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAgeDialog by remember { mutableStateOf(false) }
    var pendingEmail by remember { mutableStateOf("") }
    var pendingPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Animation toggle and tutorial states
    var animationsEnabled by remember { mutableStateOf(true) }
    var showTutorial by remember { mutableStateOf(true) }
    var tutorialStep by remember { mutableIntStateOf(0) }

    val isSignIn = selectedTabIndex == 0

    // Animated background gradient (only when animations enabled)
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationsEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    // Rainbow colors for neurodiversity
    val rainbowColors = listOf(
        Color(0xFFE40303), // Red
        Color(0xFFFF8C00), // Orange
        Color(0xFFFFED00), // Yellow
        Color(0xFF008026), // Green
        Color(0xFF24408E), // Blue
        Color(0xFF732982)  // Purple
    )

    // Calming background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1a1a2e).copy(alpha = 0.95f + (if (animationsEnabled) gradientOffset * 0.05f else 0f)),
            Color(0xFF16213e),
            Color(0xFF0f3460).copy(alpha = 0.9f + (if (animationsEnabled) gradientOffset * 0.1f else 0f))
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Animated floating orbs in background (only when animations enabled)
        if (animationsEnabled) {
            FloatingOrbsBackground(
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tappable logo with tutorial highlight
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Tutorial pointer arrow (shown on step 1 - tap the logo)
                if (showTutorial && tutorialStep == 1) {
                    TutorialPointer(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(140.dp) // Larger to prevent glow cutoff
                        .clickable {
                            animationsEnabled = !animationsEnabled
                            if (showTutorial && tutorialStep == 1) {
                                tutorialStep = 2
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (animationsEnabled) {
                        AnimatedRainbowInfinity(
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        StaticRainbowInfinity(
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    // Tutorial highlight ring (shown on step 1 - tap the logo)
                    if (showTutorial && tutorialStep == 1) {
                        TutorialHighlightRing(
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // App name with rainbow shimmer
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            // Tagline
            Text(
                text = stringResource(R.string.auth_app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Main card with glassmorphism effect
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2FA Mode
                    if (is2FARequired) {
                        Text(
                            text = stringResource(R.string.auth_2fa_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.auth_2fa_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        NeuroTextField(
                            value = twoFactorCode,
                            onValueChange = { twoFactorCode = it },
                            label = stringResource(R.string.auth_2fa_code_label),
                            placeholder = stringResource(R.string.auth_2fa_code_placeholder)
                        )

                        NeuroButton(
                            text = stringResource(R.string.auth_2fa_verify),
                            onClick = { onVerify2FA(twoFactorCode) },
                            rainbowColors = rainbowColors
                        )
                    } else {
                        // Tab Row with custom styling
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NeuroTabButton(
                                text = stringResource(R.string.auth_sign_in),
                                isSelected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0; localError = null },
                                modifier = Modifier.weight(1f)
                            )
                            NeuroTabButton(
                                text = stringResource(R.string.auth_sign_up),
                                isSelected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1; localError = null },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Error message
                        if (error != null || localError != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFF6B6B).copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = error ?: localError.orEmpty(),
                                    color = Color(0xFFFF6B6B),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Email field
                        NeuroTextField(
                            value = email,
                            onValueChange = { email = it; localError = null },
                            label = stringResource(R.string.auth_email_label),
                            placeholder = stringResource(R.string.auth_email_placeholder)
                        )

                        // Password field
                        NeuroTextField(
                            value = password,
                            onValueChange = { password = it; localError = null },
                            label = stringResource(R.string.auth_password_label),
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
                        )

                        // Confirm password for sign up - with smooth animation
                        AnimatedVisibility(
                            visible = !isSignIn,
                            enter = fadeIn(animationSpec = tween(300)) +
                                    slideInHorizontally(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                                        initialOffsetX = { it / 2 }
                                    ),
                            exit = fadeOut(animationSpec = tween(200)) +
                                   slideOutHorizontally(
                                       animationSpec = tween(200, easing = FastOutSlowInEasing),
                                       targetOffsetX = { it / 2 }
                                   )
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                NeuroTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; localError = null },
                                    label = stringResource(R.string.auth_confirm_password_label),
                                    isPassword = true,
                                    passwordVisible = confirmPasswordVisible,
                                    onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                                )

                                // Password requirements
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF4ECDC4).copy(alpha = 0.15f)
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.auth_password_requirements),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4ECDC4),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Get error message strings
                        val passwordsNotMatchError = stringResource(R.string.auth_passwords_not_match)
                        val passwordWeakError = stringResource(R.string.auth_password_weak)

                        // Primary action button
                        NeuroButton(
                            text = if (isSignIn) stringResource(R.string.auth_sign_in) else stringResource(R.string.auth_create_account),
                            onClick = {
                                if (isSignIn) {
                                    onSignIn(email, password)
                                } else {
                                    val strongEnough = isStrongPassword(password, email)
                                    if (password != confirmPassword) {
                                        localError = passwordsNotMatchError
                                        return@NeuroButton
                                    }
                                    if (!strongEnough) {
                                        localError = passwordWeakError
                                        return@NeuroButton
                                    }
                                    pendingEmail = email
                                    pendingPassword = password
                                    showAgeDialog = true
                                }
                            },
                            rainbowColors = rainbowColors
                        )
                    }
                }
            }

            // Skip button
            if (onSkip != null && !is2FARequired) {
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showDevBypass) stringResource(R.string.auth_skip_dev) else stringResource(R.string.auth_skip),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer with security message
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.auth_data_secure),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showAgeDialog) {
        AgeVerificationDialog(
            onDismiss = { showAgeDialog = false },
            onConfirm = { audience ->
                onSignUp(pendingEmail, pendingPassword, audience)
                showAgeDialog = false
            },
            onSkip = if (showDevBypass) {
                {
                    onSignUp(pendingEmail, pendingPassword, null)
                    showAgeDialog = false
                }
            } else null
        )
    }

    // Tutorial overlay
    if (showTutorial) {
        AuthTutorialOverlay(
            currentStep = tutorialStep,
            totalSteps = 7, // Must match the number of TutorialStep items in AuthTutorialOverlay
            animationsEnabled = animationsEnabled,
            onDismiss = { showTutorial = false },
            onNextStep = {
                if (tutorialStep < 6) { // 7 total steps (0-6)
                    tutorialStep++
                } else {
                    showTutorial = false
                }
            }
        )
    }
}

// ============================================================================
// NEURODIVERGENT-THEMED UI COMPONENTS
// ============================================================================

/**
 * Smooth, fluid animated rainbow infinity symbol for the auth screen.
 * Features:
 * - Flowing gradient that travels along the path
 * - Gentle breathing/pulse animation
 * - Soft glow effect
 * - Ultra-smooth easing curves
 */
@Composable
private fun AnimatedRainbowInfinity(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinity")

    // Smooth flowing gradient animation - slower for fluidity
    val flowPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPosition"
    )

    // Gentle breathing animation with smooth easing
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Subtle glow pulsation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Rainbow colors with better transitions
    val rainbowColors = listOf(
        Color(0xFFFF6B6B), // Soft red
        Color(0xFFFFB347), // Soft orange
        Color(0xFFFFE66D), // Soft yellow
        Color(0xFF6BCB77), // Soft green
        Color(0xFF4D96FF), // Soft blue
        Color(0xFF9B59B6), // Soft purple
        Color(0xFFFF6B9D), // Soft pink
        Color(0xFFFF6B6B)  // Loop back to red
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow layer (behind)
        Canvas(
            modifier = Modifier
                .size(110.dp)
                .blur(12.dp)
                .alpha(glowAlpha)
        ) {
            drawFluidInfinityPath(
                width = size.width * breathe,
                height = size.height * breathe,
                flowPosition = flowPosition,
                rainbowColors = rainbowColors,
                strokeWidthMultiplier = 0.2f,
                isGlow = true
            )
        }

        // Main infinity symbol
        Canvas(
            modifier = Modifier.size(100.dp)
        ) {
            drawFluidInfinityPath(
                width = size.width * breathe,
                height = size.height * breathe,
                flowPosition = flowPosition,
                rainbowColors = rainbowColors,
                strokeWidthMultiplier = 0.12f,
                isGlow = false
            )
        }
    }
}

/**
 * Draws a fluid, smooth infinity path with animated gradient
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFluidInfinityPath(
    width: Float,
    height: Float,
    flowPosition: Float,
    rainbowColors: List<Color>,
    strokeWidthMultiplier: Float,
    isGlow: Boolean
) {
    val offsetX = (size.width - width) / 2
    val offsetY = (size.height - height) / 2
    val strokeWidth = height * strokeWidthMultiplier
    val centerX = width / 2 + offsetX
    val centerY = height / 2 + offsetY

    // Calculate control points for smoother curves
    val loopWidth = width * 0.35f
    val loopHeight = height * 0.38f

    // Create smooth infinity path using more control points
    val infinityPath = Path().apply {
        // Start at center
        moveTo(centerX, centerY)

        // Right loop - top curve (smoother with adjusted control points)
        cubicTo(
            x1 = centerX + loopWidth * 0.5f, y1 = centerY - loopHeight * 0.9f,
            x2 = centerX + loopWidth * 0.95f, y2 = centerY - loopHeight * 0.7f,
            x3 = centerX + loopWidth, y3 = centerY
        )

        // Right loop - bottom curve
        cubicTo(
            x1 = centerX + loopWidth * 0.95f, y1 = centerY + loopHeight * 0.7f,
            x2 = centerX + loopWidth * 0.5f, y2 = centerY + loopHeight * 0.9f,
            x3 = centerX, y3 = centerY
        )

        // Left loop - bottom curve (crossing over)
        cubicTo(
            x1 = centerX - loopWidth * 0.5f, y1 = centerY + loopHeight * 0.9f,
            x2 = centerX - loopWidth * 0.95f, y2 = centerY + loopHeight * 0.7f,
            x3 = centerX - loopWidth, y3 = centerY
        )

        // Left loop - top curve (back to center)
        cubicTo(
            x1 = centerX - loopWidth * 0.95f, y1 = centerY - loopHeight * 0.7f,
            x2 = centerX - loopWidth * 0.5f, y2 = centerY - loopHeight * 0.9f,
            x3 = centerX, y3 = centerY
        )
    }

    // Create flowing sweep gradient based on animation position
    val sweepGradient = Brush.sweepGradient(
        colors = rainbowColors,
        center = Offset(centerX, centerY)
    )

    // Create a linear gradient that flows along the path
    val flowingGradient = Brush.linearGradient(
        colors = rainbowColors,
        start = Offset(
            centerX + cos(Math.toRadians(flowPosition.toDouble())).toFloat() * width,
            centerY + sin(Math.toRadians(flowPosition.toDouble())).toFloat() * height
        ),
        end = Offset(
            centerX + cos(Math.toRadians((flowPosition + 180).toDouble())).toFloat() * width,
            centerY + sin(Math.toRadians((flowPosition + 180).toDouble())).toFloat() * height
        )
    )

    drawPath(
        path = infinityPath,
        brush = flowingGradient,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Floating orbs background animation
 */
@Composable
private fun FloatingOrbsBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Canvas(modifier = modifier.alpha(0.3f)) {
        val orbs = listOf(
            Triple(Color(0xFF732982), 150.dp.toPx(), offset1),
            Triple(Color(0xFF24408E), 100.dp.toPx(), -offset2),
            Triple(Color(0xFF4ECDC4), 80.dp.toPx(), offset1 * 0.7f),
            Triple(Color(0xFFFF8C00), 120.dp.toPx(), -offset1 * 0.5f)
        )

        orbs.forEachIndexed { index, (color, radius, offset) ->
            val angle = Math.toRadians((offset + index * 90).toDouble())
            val x = size.width * (0.3f + 0.4f * cos(angle).toFloat())
            val y = size.height * (0.2f + 0.6f * sin(angle).toFloat())

            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x, y),
                alpha = 0.4f
            )
        }
    }
}

/**
 * Static rainbow infinity symbol (no animations) for users who prefer reduced motion
 */
@Composable
private fun StaticRainbowInfinity(modifier: Modifier = Modifier) {
    val rainbowColors = listOf(
        Color(0xFFFF6B6B), // Soft red
        Color(0xFFFFB347), // Soft orange
        Color(0xFFFFE66D), // Soft yellow
        Color(0xFF6BCB77), // Soft green
        Color(0xFF4D96FF), // Soft blue
        Color(0xFF9B59B6), // Soft purple
        Color(0xFFFF6B9D), // Soft pink
        Color(0xFFFF6B6B)  // Loop back to red
    )

    Canvas(modifier = modifier.size(100.dp)) {
        val width = size.width
        val height = size.height
        val strokeWidth = height * 0.12f
        val centerX = width / 2
        val centerY = height / 2
        val loopWidth = width * 0.35f
        val loopHeight = height * 0.38f

        val infinityPath = Path().apply {
            moveTo(centerX, centerY)
            cubicTo(
                x1 = centerX + loopWidth * 0.5f, y1 = centerY - loopHeight * 0.9f,
                x2 = centerX + loopWidth * 0.95f, y2 = centerY - loopHeight * 0.7f,
                x3 = centerX + loopWidth, y3 = centerY
            )
            cubicTo(
                x1 = centerX + loopWidth * 0.95f, y1 = centerY + loopHeight * 0.7f,
                x2 = centerX + loopWidth * 0.5f, y2 = centerY + loopHeight * 0.9f,
                x3 = centerX, y3 = centerY
            )
            cubicTo(
                x1 = centerX - loopWidth * 0.5f, y1 = centerY + loopHeight * 0.9f,
                x2 = centerX - loopWidth * 0.95f, y2 = centerY + loopHeight * 0.7f,
                x3 = centerX - loopWidth, y3 = centerY
            )
            cubicTo(
                x1 = centerX - loopWidth * 0.95f, y1 = centerY - loopHeight * 0.7f,
                x2 = centerX - loopWidth * 0.5f, y2 = centerY - loopHeight * 0.9f,
                x3 = centerX, y3 = centerY
            )
        }

        val staticGradient = Brush.horizontalGradient(rainbowColors)

        drawPath(
            path = infinityPath,
            brush = staticGradient,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Pulsing highlight ring for tutorial focus
 */
@Composable
private fun TutorialHighlightRing(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = modifier) {
        val radius = (size.minDimension / 2) * pulseScale

        // Outer glow
        drawCircle(
            color = Color(0xFF4ECDC4),
            radius = radius + 8.dp.toPx(),
            alpha = pulseAlpha * 0.3f
        )

        // Main ring
        drawCircle(
            color = Color(0xFF4ECDC4),
            radius = radius,
            style = Stroke(width = 3.dp.toPx()),
            alpha = pulseAlpha
        )
    }
}

/**
 * Animated pointer/arrow for tutorial to draw attention to the logo
 */
@Composable
private fun TutorialPointer(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pointer")

    // Bounce animation
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val pointerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pointerAlpha"
    )

    Row(
        modifier = modifier.offset(x = bounceOffset.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Tap here" text
        Text(
            text = stringResource(R.string.auth_tutorial_tap_here),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4ECDC4).copy(alpha = pointerAlpha)
        )

        Spacer(Modifier.width(4.dp))

        // Arrow pointing right
        Text(
            text = "→",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4ECDC4).copy(alpha = pointerAlpha)
        )
    }
}

/**
 * Tutorial overlay for the auth screen
 */
@Composable
private fun AuthTutorialOverlay(
    currentStep: Int,
    totalSteps: Int,
    animationsEnabled: Boolean,
    onDismiss: () -> Unit,
    onNextStep: () -> Unit
) {
    // Production-ready, neurodivergent-centric tutorial steps with LGBTQ+ inclusion
    val tutorialSteps = listOf(
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_welcome_title),
            description = stringResource(R.string.auth_tutorial_welcome_desc),
            emoji = "🏠",
            showRainbowInfinity = false,
            accentColor = Color(0xFF9B59B6) // Purple for inclusivity
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_comfort_title),
            description = if (animationsEnabled)
                stringResource(R.string.auth_tutorial_comfort_desc_anim)
            else
                stringResource(R.string.auth_tutorial_comfort_desc_static),
            emoji = "∞",
            showRainbowInfinity = true,
            accentColor = Color(0xFF4ECDC4) // Teal for calm
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_themes_title),
            description = stringResource(R.string.auth_tutorial_themes_desc),
            emoji = "🎨",
            showRainbowInfinity = false,
            accentColor = Color(0xFFE74C3C) // Warm red
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_fonts_title),
            description = stringResource(R.string.auth_tutorial_fonts_desc),
            emoji = "📖",
            showRainbowInfinity = false,
            accentColor = Color(0xFF3498DB) // Blue for clarity
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_community_title),
            description = stringResource(R.string.auth_tutorial_community_desc),
            emoji = "🌈",
            showRainbowInfinity = false,
            accentColor = Color(0xFFF39C12) // Gold for celebration
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_safety_title),
            description = stringResource(R.string.auth_tutorial_safety_desc),
            emoji = "🛡️",
            showRainbowInfinity = false,
            accentColor = Color(0xFF27AE60) // Green for safety
        ),
        TutorialStep(
            title = stringResource(R.string.auth_tutorial_ready_title),
            description = stringResource(R.string.auth_tutorial_ready_desc),
            emoji = "✨",
            showRainbowInfinity = false,
            accentColor = Color(0xFF9B59B6) // Return to purple
        )
    )

    val step = tutorialSteps.getOrNull(currentStep) ?: return

    // Smooth fade animation for step transitions
    val stepAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "stepAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(onClick = onNextStep),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .alpha(stepAlpha),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1a1a2e)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Accent bar at top
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(step.accentColor)
                )

                Spacer(Modifier.height(20.dp))

                // Icon container with subtle glow effect
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(step.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (step.showRainbowInfinity) {
                        StaticRainbowInfinity(modifier = Modifier.size(56.dp))
                    } else {
                        Text(
                            text = step.emoji,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Title with accent color
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Description with better readability
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(28.dp))

                // Progress indicator - pills instead of dots for better visibility
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    repeat(tutorialSteps.size) { index ->
                        val isActive = index == currentStep
                        val isPast = index < currentStep
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .weight(if (isActive) 1.5f else 1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isActive -> step.accentColor
                                        isPast -> step.accentColor.copy(alpha = 0.5f)
                                        else -> Color.White.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Step counter
                Text(
                    text = stringResource(R.string.auth_tutorial_step_count, currentStep + 1, tutorialSteps.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(Modifier.height(24.dp))

                // Buttons with enhanced styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.auth_tutorial_skip),
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = onNextStep,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = step.accentColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = if (currentStep == tutorialSteps.size - 1) stringResource(R.string.auth_tutorial_lets_go) else stringResource(R.string.auth_tutorial_continue),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (currentStep < tutorialSteps.size - 1) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for tutorial steps with enhanced styling
 */
private data class TutorialStep(
    val title: String,
    val description: String,
    val emoji: String,
    val showRainbowInfinity: Boolean = false,
    val accentColor: Color = Color(0xFF4ECDC4)
)

/**
 * Custom styled text field for the auth screen
 */
@Composable
private fun NeuroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4ECDC4),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = Color(0xFF4ECDC4),
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color(0xFF4ECDC4),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
            {
                val hidePasswordDesc = stringResource(R.string.auth_hide_password)
                val showPasswordDesc = stringResource(R.string.auth_show_password)
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) hidePasswordDesc else showPasswordDesc,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else null
    )
}

/**
 * Custom tab button for sign in/sign up toggle with smooth animations
 */
@Composable
private fun NeuroTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF4ECDC4) else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "tabBgColor"
    )

    // Animated text color
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1a1a2e) else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "tabTextColor"
    )

    // Animated scale for a subtle "pop" effect
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "tabScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Gradient action button - static gradient (no animation for better accessibility)
 */
@Composable
private fun NeuroButton(
    text: String,
    onClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") rainbowColors: List<Color>,
    modifier: Modifier = Modifier
) {
    // Static gradient for cleaner look and better accessibility
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4ECDC4),
            Color(0xFF44A08D),
            Color(0xFF3D9A85)
        )
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(buttonGradient, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

fun isStrongPassword(pass: String, emailValue: String): Boolean {
    val hasUpper = pass.any { it.isUpperCase() }
    val hasLower = pass.any { it.isLowerCase() }
    val hasDigit = pass.any { it.isDigit() }
    val hasSymbol = pass.any { !it.isLetterOrDigit() }
    val longEnough = pass.length >= 12
    val emailLocal = emailValue.substringBefore("@").lowercase()
    val containsEmailPart = emailLocal.isNotBlank() && pass.lowercase().contains(emailLocal)
    return longEnough && hasUpper && hasLower && hasDigit && hasSymbol && !containsEmailPart
}

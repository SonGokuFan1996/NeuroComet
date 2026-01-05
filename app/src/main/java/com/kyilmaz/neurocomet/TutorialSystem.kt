package com.kyilmaz.neurocomet

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import kotlin.math.abs

/**
 * Tutorial step definitions for first-time users
 * Enhanced with accent colors and emojis for better visual hierarchy
 */
enum class AppTutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val emoji: String,
    val accentColor: Color,
    val highlightArea: HighlightArea
) {
    WELCOME(
        title = "Welcome to NeuroComet!",
        description = "A safe space designed with neurodivergent minds in mind. Let's take a gentle tour to help you feel at home.",
        icon = Icons.Filled.Celebration,
        emoji = "☄️",
        accentColor = Color(0xFF9B59B6), // Purple for inclusivity
        highlightArea = HighlightArea.NONE
    ),
    FEED(
        title = "Your Home Feed",
        description = "This is where you'll see posts from people you follow. It's a calm, supportive space with no algorithmic pressure.",
        icon = Icons.Filled.Home,
        emoji = "🏠",
        accentColor = Color(0xFF4ECDC4), // Teal for calm
        highlightArea = HighlightArea.BOTTOM_NAV_FEED
    ),
    EXPLORE(
        title = "Explore Communities",
        description = "Discover topics and communities that match your interests. Find your people!",
        icon = Icons.Filled.Search,
        emoji = "🔍",
        accentColor = Color(0xFF3498DB), // Blue for discovery
        highlightArea = HighlightArea.BOTTOM_NAV_EXPLORE
    ),
    MESSAGES(
        title = "Messages & Practice Calls",
        description = "Chat with friends and practice phone calls with AI personas. Great for building communication confidence!",
        icon = Icons.Filled.Mail,
        emoji = "💬",
        accentColor = Color(0xFFF39C12), // Gold for connection
        highlightArea = HighlightArea.BOTTOM_NAV_MESSAGES
    ),
    PRACTICE_CALLS(
        title = "Practice Calls Feature",
        description = "Tap the phone icon in Messages to access Practice Calls. You can practice conversations with AI personas who understand neurodivergent experiences.",
        icon = Icons.Filled.Phone,
        emoji = "📞",
        accentColor = Color(0xFF27AE60), // Green for growth
        highlightArea = HighlightArea.NONE
    ),
    NOTIFICATIONS(
        title = "Stay Updated",
        description = "Check notifications for likes, comments, and new followers. You control what notifications you receive.",
        icon = Icons.Filled.Notifications,
        emoji = "🔔",
        accentColor = Color(0xFFE74C3C), // Red for alerts
        highlightArea = HighlightArea.BOTTOM_NAV_NOTIFICATIONS
    ),
    SETTINGS(
        title = "Personalize Your Experience",
        description = "Customize themes, fonts, animations, and accessibility options to make the app comfortable for you.",
        icon = Icons.Filled.Settings,
        emoji = "⚙️",
        accentColor = Color(0xFF8E44AD), // Deep purple
        highlightArea = HighlightArea.BOTTOM_NAV_SETTINGS
    ),
    COMPLETE(
        title = "You're All Set!",
        description = "You can always access help from Settings. Enjoy connecting with your community!",
        icon = Icons.Filled.CheckCircle,
        emoji = "✨",
        accentColor = Color(0xFF9B59B6), // Return to purple
        highlightArea = HighlightArea.NONE
    )
}

enum class HighlightArea {
    NONE,
    BOTTOM_NAV_FEED,
    BOTTOM_NAV_EXPLORE,
    BOTTOM_NAV_MESSAGES,
    BOTTOM_NAV_NOTIFICATIONS,
    BOTTOM_NAV_SETTINGS,
    TOP_BAR,
    FAB
}

/**
 * Tutorial state manager - handles tutorial progress and persistence
 * Uses index-based navigation for reliable step tracking
 */
object TutorialManager {
    private const val PREFS_NAME = "neurocomet_tutorial_prefs"
    private const val KEY_COMPLETED = "tutorial_completed"
    private const val KEY_SKIPPED = "tutorial_skipped"

    // Use index-based tracking for reliable navigation
    private val _currentStepIndex = mutableIntStateOf(0)
    val currentStepIndex: State<Int> = _currentStepIndex

    // Computed step from index
    val currentStep: AppTutorialStep
        get() = AppTutorialStep.entries.getOrElse(_currentStepIndex.intValue) { AppTutorialStep.WELCOME }

    private val _isActive = mutableStateOf(false)
    val isActive: State<Boolean> = _isActive

    private val _showTutorial = mutableStateOf(false)
    val showTutorial: State<Boolean> = _showTutorial
    
    // Track navigation direction for animations (1 = forward, -1 = backward)
    private val _navigationDirection = mutableIntStateOf(1)
    val navigationDirection: State<Int> = _navigationDirection

    // Unique key to force recomposition on navigation
    private val _transitionKey = mutableIntStateOf(0)
    val transitionKey: State<Int> = _transitionKey

    /**
     * Check if tutorial should be shown for first-time users
     */
    fun shouldShowTutorial(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val completed = prefs.getBoolean(KEY_COMPLETED, false)
        val skipped = prefs.getBoolean(KEY_SKIPPED, false)
        return !completed && !skipped
    }

    /**
     * Start the tutorial from the beginning
     */
    fun startTutorial() {
        _currentStepIndex.intValue = 0
        _isActive.value = true
        _showTutorial.value = true
        _navigationDirection.intValue = 1
        _transitionKey.intValue = 0
    }

    /**
     * Move to next tutorial step
     * @return true if moved to next step, false if already at last step
     */
    fun nextStep(): Boolean {
        val maxIndex = AppTutorialStep.entries.lastIndex
        return if (_currentStepIndex.intValue < maxIndex) {
            _navigationDirection.intValue = 1
            _currentStepIndex.intValue++
            _transitionKey.intValue++
            true
        } else {
            false
        }
    }

    /**
     * Move to previous tutorial step
     * @return true if moved to previous step, false if already at first step
     */
    fun previousStep(): Boolean {
        return if (_currentStepIndex.intValue > 0) {
            _navigationDirection.intValue = -1
            _currentStepIndex.intValue--
            _transitionKey.intValue++
            true
        } else {
            false
        }
    }
    
    /**
     * Go to a specific step by index
     */
    fun goToStep(stepIndex: Int) {
        val steps = AppTutorialStep.entries
        if (stepIndex in steps.indices && stepIndex != _currentStepIndex.intValue) {
            _navigationDirection.intValue = if (stepIndex > _currentStepIndex.intValue) 1 else -1
            _currentStepIndex.intValue = stepIndex
            _transitionKey.intValue++
        }
    }

    /**
     * Skip the tutorial and remember preference
     */
    fun skipTutorial(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_SKIPPED, true)
        }
        _isActive.value = false
        _showTutorial.value = false
    }

    /**
     * Complete the tutorial and remember preference
     */
    fun completeTutorial(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_COMPLETED, true)
        }
        _isActive.value = false
        _showTutorial.value = false
    }

    /**
     * Reset tutorial for testing or re-watching
     */
    fun resetTutorial(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_COMPLETED, false)
            putBoolean(KEY_SKIPPED, false)
        }
        _currentStepIndex.intValue = 0
        _navigationDirection.intValue = 1
        _transitionKey.intValue = 0
    }
}

/**
 * Neurodivergent-friendly easing curves - gentler, less jarring transitions
 */
private val gentleEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private val calmEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

/**
 * Rainbow gradient colors matching the feed's neurodiversity theme
 */
private val rainbowGradient = listOf(
    Color(0xFF9F70FD), // Soft purple
    Color(0xFF487DE7), // Calm blue
    Color(0xFF5AC8FA), // Sky blue
    Color(0xFF78C850), // Soft green
    Color(0xFFFFD700), // Warm gold
    Color(0xFFFF9F43), // Gentle orange
    Color(0xFFFF6B9D), // Soft pink
    Color(0xFF9F70FD)  // Back to purple (seamless)
)

/**
 * Tutorial overlay that guides users through the app with neurodivergent-friendly design
 * Overhauled to match the AuthScreen tutorial's clean, production-ready style
 */
@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val showTutorial by TutorialManager.showTutorial
    val currentStepIndex by TutorialManager.currentStepIndex
    val navigationDirection by TutorialManager.navigationDirection
    val transitionKey by TutorialManager.transitionKey

    if (!showTutorial) return

    val currentStep = TutorialManager.currentStep
    val totalSteps = AppTutorialStep.entries.size
    val isFirstStep = currentStepIndex == 0
    val isLastStep = currentStepIndex == totalSteps - 1

    // Swipe gesture tracking
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Smooth fade animation for step transitions
    val stepAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "stepAlpha"
    )

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D1A))
                .pointerInput(currentStepIndex) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            accumulatedDrag = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            when {
                                accumulatedDrag < -80f && !isLastStep -> TutorialManager.nextStep()
                                accumulatedDrag > 80f && !isFirstStep -> TutorialManager.previousStep()
                            }
                            accumulatedDrag = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            accumulatedDrag = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle animated background
            CleanAnimatedBackground(accentColor = currentStep.accentColor)

            // Tutorial card with key-based recomposition
            key(transitionKey) {
                CleanTutorialCard(
                    step = currentStep,
                    stepIndex = currentStepIndex,
                    totalSteps = totalSteps,
                    direction = navigationDirection,
                    onNext = {
                        if (isLastStep) {
                            TutorialManager.completeTutorial(context)
                            onDismiss()
                        } else {
                            TutorialManager.nextStep()
                        }
                    },
                    onBack = { TutorialManager.previousStep() },
                    onSkip = {
                        TutorialManager.skipTutorial(context)
                        onDismiss()
                    },
                    isFirstStep = isFirstStep,
                    isLastStep = isLastStep,
                    modifier = Modifier.alpha(stepAlpha)
                )
            }

            // Swipe hint for first step
            if (isFirstStep) {
                Text(
                    text = "Swipe to navigate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

/**
 * Clean animated background with subtle accent color glow
 */
@Composable
private fun CleanAnimatedBackground(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = calmEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Large subtle glow in top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = pulseAlpha),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(
                    size.width * 0.85f,
                    size.height * 0.15f + glowOffset
                ),
                radius = size.minDimension * 0.6f
            ),
            radius = size.minDimension * 0.6f,
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.85f,
                size.height * 0.15f + glowOffset
            )
        )

        // Smaller glow in bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = pulseAlpha * 0.5f),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(
                    size.width * 0.15f,
                    size.height * 0.85f - glowOffset
                ),
                radius = size.minDimension * 0.4f
            ),
            radius = size.minDimension * 0.4f,
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.15f,
                size.height * 0.85f - glowOffset
            )
        )
    }
}

/**
 * Clean, production-ready tutorial card matching AuthScreen style
 */
@Composable
private fun CleanTutorialCard(
    step: AppTutorialStep,
    stepIndex: Int,
    totalSteps: Int,
    direction: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    modifier: Modifier = Modifier
) {
    // Entrance animation states
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = false
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    // Smooth scale entrance
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    // Fade in
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, easing = calmEasing),
        label = "cardAlpha"
    )

    // Slide from direction
    val offsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else (direction * 60f),
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardOffset"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationX = offsetX
            },
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

            Spacer(Modifier.height(24.dp))

            // Icon container with glow effect
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(step.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            // Title with accent color influence
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
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
            )

            // Show navigation preview for steps that highlight navigation items
            if (step.highlightArea != HighlightArea.NONE) {
                Spacer(Modifier.height(20.dp))
                NavigationPreview(
                    highlightArea = step.highlightArea,
                    accentColor = step.accentColor
                )
            }

            // Show Practice Calls preview for that specific step
            if (step == AppTutorialStep.PRACTICE_CALLS) {
                Spacer(Modifier.height(20.dp))
                PracticeCallsPreview(accentColor = step.accentColor)
            }

            Spacer(Modifier.height(28.dp))

            // Progress indicator - pills for better visibility
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(totalSteps) { index ->
                    val isActive = index == stepIndex
                    val isPast = index < stepIndex

                    val pillWidth by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "pillWidth"
                    )

                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isActive -> step.accentColor
                                    isPast -> step.accentColor.copy(alpha = 0.5f)
                                    else -> Color.White.copy(alpha = 0.2f)
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { TutorialManager.goToStep(index) }
                            )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Step counter
            Text(
                text = "Step ${stepIndex + 1} of $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(28.dp))

            // Navigation buttons with enhanced styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFirstStep) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            "Skip",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Back",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = step.accentColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (isLastStep) "Get Started!" else "Continue",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (!isLastStep) {
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

/**
 * Visual preview of the bottom navigation showing which item to look for
 * This provides guidance without overlaying the actual interface
 */
@Composable
private fun NavigationPreview(
    highlightArea: HighlightArea,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "navPreview")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "navPulse"
    )

    // Navigation items with their icons and labels
    data class NavItem(
        val icon: ImageVector,
        val label: String,
        val area: HighlightArea
    )

    val navItems = listOf(
        NavItem(Icons.Filled.Home, "Feed", HighlightArea.BOTTOM_NAV_FEED),
        NavItem(Icons.Filled.Search, "Explore", HighlightArea.BOTTOM_NAV_EXPLORE),
        NavItem(Icons.Filled.Mail, "Messages", HighlightArea.BOTTOM_NAV_MESSAGES),
        NavItem(Icons.Filled.Notifications, "Alerts", HighlightArea.BOTTOM_NAV_NOTIFICATIONS),
        NavItem(Icons.Filled.Settings, "Settings", HighlightArea.BOTTOM_NAV_SETTINGS)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = "Look for this in the navigation bar:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(12.dp))

        // Mini navigation bar preview
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF252540),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isHighlighted = item.area == highlightArea

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .then(
                                if (isHighlighted) {
                                    Modifier.scale(pulseScale)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        // Icon with highlight effect
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Glow effect for highlighted item
                            if (isHighlighted) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.3f))
                                )
                            }

                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp),
                                tint = if (isHighlighted) accentColor else Color.White.copy(alpha = 0.4f)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Label
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isHighlighted) accentColor else Color.White.copy(alpha = 0.3f),
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Arrow pointing down to indicate "look at bottom of screen"
        Spacer(Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    translationY = (pulseScale - 1f) * 20f
                },
            tint = accentColor.copy(alpha = 0.7f)
        )
    }
}

/**
 * Visual preview showing how to access Practice Calls from Messages
 * Shows a mock Messages header with the phone icon highlighted
 */
@Composable
private fun PracticeCallsPreview(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "practiceCallsPreview")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phonePulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phoneGlow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = "In Messages, look for the phone icon:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(12.dp))

        // Mock Messages header preview
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF252540),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Mock header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Messages title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mail,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Messages",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Highlighted phone icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.scale(pulseScale)
                    ) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = glowAlpha))
                        )

                        // Phone icon
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Practice Calls",
                            modifier = Modifier.size(24.dp),
                            tint = accentColor
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Mock conversation list hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Instruction text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tap the",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = accentColor
            )
            Text(
                text = "icon to start practicing!",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Step indicator badge with rainbow accent
 */
@Composable
private fun StepIndicatorBadge(stepIndex: Int, totalSteps: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mini progress dots
            repeat(totalSteps) { index ->
                val color = if (index <= stepIndex) {
                    rainbowGradient[index % rainbowGradient.size]
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .size(if (index == stepIndex) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

/**
 * Animated icon for each step
 */
@Composable
private fun AnimatedStepIcon(step: AppTutorialStep) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon")

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = calmEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconGlow"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(iconScale),
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Icon container
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Navigation buttons matching feed style
 */
@Composable
private fun TutorialNavigationButtons(
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isFirstStep) {
            TextButton(
                onClick = onSkip,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Skip")
            }
        } else {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Back")
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastStep)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                if (isLastStep) "Get Started!" else "Next",
                fontWeight = FontWeight.SemiBold
            )
            if (!isLastStep) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Rainbow-themed progress indicator
 */
@Composable
private fun RainbowProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPast = index < currentStep

            val animatedSize by animateDpAsState(
                targetValue = if (isActive) 12.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dotSize"
            )

            // Use colors with better contrast and a softer appearance
            val baseColor = rainbowGradient[index % rainbowGradient.size]
            val dotColor = when {
                isActive -> baseColor
                isPast -> baseColor.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onStepClick(index) }
                    )
            ) {
                // Soft glow for active dot
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(animatedSize + 8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                // The actual dot
                Box(
                    modifier = Modifier
                        .size(animatedSize)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

/**
 * Tutorial card content with icon animations
 */
@Composable
private fun TutorialCardContent(
    step: AppTutorialStep,
    stepIndex: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    isFirstStep: Boolean,
    isLastStep: Boolean
) {
    // Animated icon with gentle pulse
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )
    
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    val iconGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = calmEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconGlow"
    )

    Card(
        modifier = Modifier
            .padding(24.dp)
            .widthIn(max = 380.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step counter with pill badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Step ${stepIndex + 1} of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Icon with animated glow background
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(iconScale)
                    .rotate(iconRotation),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect - use a softer, more pleasant color with better visibility
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = iconGlow * 0.6f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = iconGlow * 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Main icon container - use solid background for better contrast
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    // Add a contrasting inner circle for depth
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons with improved styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip/Back button
                if (isFirstStep) {
                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Skip")
                    }
                } else {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back")
                    }
                }

                // Next/Done button
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastStep) 
                            MaterialTheme.colorScheme.tertiary 
                        else 
                            MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        if (isLastStep) "Get Started!" else "Next",
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isLastStep) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive progress indicator that allows tapping to navigate
 */
@Composable
private fun InteractiveTutorialProgress(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPast = index < currentStep
            
            val animatedSize by animateDpAsState(
                targetValue = if (isActive) 14.dp else 10.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dotSize"
            )
            
            val animatedColor by animateColorAsState(
                targetValue = when {
                    isActive -> MaterialTheme.colorScheme.primary
                    isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.35f)
                },
                animationSpec = tween(300, easing = gentleEasing),
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .clip(CircleShape)
                    .background(animatedColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onStepClick(index) }
                    )
            )
        }
    }
}

/**
 * Swipe hint animation for first-time users
 */
@Composable
private fun SwipeHint(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeAlpha"
    )
    
    Row(
        modifier = modifier
            .alpha(alpha)
            .offset { IntOffset(offsetX.toInt(), 0) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
        Text(
            text = "Swipe or tap to navigate",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun HighlightArrow(
    highlightArea: HighlightArea,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arrow")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowBounce"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = gentleEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowScale"
    )

    // Position the arrow based on which nav item is highlighted
    val horizontalBias = when (highlightArea) {
        HighlightArea.BOTTOM_NAV_FEED -> -0.8f
        HighlightArea.BOTTOM_NAV_EXPLORE -> -0.4f
        HighlightArea.BOTTOM_NAV_MESSAGES -> 0f
        HighlightArea.BOTTOM_NAV_NOTIFICATIONS -> 0.4f
        HighlightArea.BOTTOM_NAV_SETTINGS -> 0.8f
        else -> 0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Look here",
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .align(Alignment.BottomCenter)
                .offset { IntOffset((horizontalBias * 150).toInt(), offsetY.toInt()) },
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Trigger tutorial check for first-time users
 */
@Composable
fun TutorialTrigger() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (TutorialManager.shouldShowTutorial(context)) {
            // Small delay so the main UI loads first
            kotlinx.coroutines.delay(800)
            TutorialManager.startTutorial()
        }
    }
}

/**
 * Button to restart tutorial from settings
 */
@Composable
fun RestartTutorialButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            TutorialManager.resetTutorial(context)
            TutorialManager.startTutorial()
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Filled.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Watch Tutorial Again")
    }
}

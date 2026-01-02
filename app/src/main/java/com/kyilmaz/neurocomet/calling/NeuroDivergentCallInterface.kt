package com.kyilmaz.neurocomet.calling

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.kyilmaz.neurocomet.avatarUrl
import com.kyilmaz.neurocomet.CallType
import com.kyilmaz.neurocomet.CallState
import com.kyilmaz.neurocomet.CallOutcome
import com.kyilmaz.neurocomet.CallHistoryEntry
import com.kyilmaz.neurocomet.MockCallManager
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * NEURODIVERGENT-CENTRIC CALL SYSTEM
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This module provides a production-ready call interface designed specifically
 * for neurodivergent users with the following accessibility features:
 *
 * - Clear visual hierarchy with high contrast
 * - Haptic feedback for important actions
 * - Reduced cognitive load with simple, obvious controls
 * - Breathing/grounding exercises during calls
 * - Adjustable speech rate for AI responses
 * - Visual cues for call state changes
 * - Sensory-friendly color schemes
 * - Clear, unambiguous iconography
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// ═══════════════════════════════════════════════════════════════════════════════
// ACCESSIBILITY SETTINGS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Call accessibility preferences
 */
object CallAccessibilitySettings {
    var hapticFeedbackEnabled by mutableStateOf(true)
    var speechRate by mutableFloatStateOf(1.0f) // 0.5 = slow, 1.0 = normal, 1.5 = fast
    var showBreathingExercise by mutableStateOf(true)
    var showCallScript by mutableStateOf(true)
    var autoAnswerDelay by mutableLongStateOf(0L) // 0 = manual, >0 = auto-answer after delay
    var reducedMotion by mutableStateOf(false)
    var highContrast by mutableStateOf(false)
    var largeControls by mutableStateOf(false)
}

/**
 * Speech rate options for TTS
 */
enum class SpeechRateOption(
    val label: String,
    val rate: Float,
    val icon: ImageVector
) {
    SLOW("Slow", 0.7f, Icons.Outlined.Speed),
    NORMAL("Normal", 1.0f, Icons.Outlined.Speed),
    FAST("Fast", 1.3f, Icons.Outlined.Speed)
}

// ═══════════════════════════════════════════════════════════════════════════════
// CALL PREPARATION SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Pre-call preparation screen with grounding exercises and call scripts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallPreparationScreen(
    persona: NeurodivergentPersona,
    onStartCall: () -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showBreathingExercise by remember { mutableStateOf(false) }
    var selectedTip by remember { mutableIntStateOf(0) }

    val tips = remember(persona) {
        getCallTipsForPersona(persona)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prepare for Call") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Persona info card
            item {
                PersonaInfoCard(persona = persona)
            }

            // Quick tips section
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tips for this call",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        tips.forEachIndexed { index, tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (index == selectedTip)
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedTip = index }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Breathing exercise button
            item {
                OutlinedCard(
                    onClick = { showBreathingExercise = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Air,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Breathing Exercise",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Take a moment to calm your nerves",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                        )
                    }
                }
            }

            // Speech rate settings
            item {
                SpeechRateSelector()
            }

            // Start call button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (CallAccessibilitySettings.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onStartCall()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start Practice Call",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Reminder text
            item {
                Text(
                    "💡 Remember: This is practice! There's no pressure. You can end the call anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Breathing exercise dialog
    if (showBreathingExercise) {
        BreathingExerciseDialog(
            onDismiss = { showBreathingExercise = false }
        )
    }
}

@Composable
private fun PersonaInfoCard(persona: NeurodivergentPersona) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl(persona.avatarSeed),
                contentDescription = persona.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = persona.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = persona.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeechRateSelector() {
    var expanded by remember { mutableStateOf(false) }
    val currentRate = SpeechRateOption.entries.find {
        it.rate == CallAccessibilitySettings.speechRate
    } ?: SpeechRateOption.NORMAL

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI Speech Speed",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(onClick = { expanded = !expanded }) {
                    Text(currentRate.label)
                    Icon(
                        if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    SpeechRateOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    CallAccessibilitySettings.speechRate = option.rate
                                    expanded = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == currentRate,
                                onClick = {
                                    CallAccessibilitySettings.speechRate = option.rate
                                    expanded = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option.label)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BREATHING EXERCISE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun BreathingExerciseDialog(
    onDismiss: () -> Unit
) {
    var phase by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycleCount by remember { mutableIntStateOf(0) }
    val maxCycles = 3

    val infiniteTransition = rememberInfiniteTransition(label = "breath")

    // Breathing animation - 4 seconds inhale, 4 seconds hold, 4 seconds exhale
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathProgress"
    )

    // Update phase based on progress
    LaunchedEffect(progress) {
        phase = when {
            progress < 0.33f -> BreathPhase.INHALE
            progress < 0.5f -> BreathPhase.HOLD
            progress < 0.83f -> BreathPhase.EXHALE
            else -> BreathPhase.REST
        }
    }

    // Count cycles
    LaunchedEffect(Unit) {
        while (cycleCount < maxCycles) {
            delay(12000)
            cycleCount++
        }
        delay(1000)
        onDismiss()
    }

    val scale by animateFloatAsState(
        targetValue = when (phase) {
            BreathPhase.INHALE -> 1.3f
            BreathPhase.HOLD -> 1.3f
            BreathPhase.EXHALE -> 1.0f
            BreathPhase.REST -> 1.0f
        },
        animationSpec = tween(
            durationMillis = when (phase) {
                BreathPhase.INHALE -> 4000
                BreathPhase.HOLD -> 0
                BreathPhase.EXHALE -> 4000
                BreathPhase.REST -> 0
            },
            easing = EaseInOut
        ),
        label = "breathScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1a237e),
                            Color(0xFF0d47a1),
                            Color(0xFF01579b)
                        )
                    )
                )
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Breathing circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF64b5f6),
                                    Color(0xFF42a5f5).copy(alpha = 0.7f),
                                    Color(0xFF2196f3).copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phase.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Cycle ${cycleCount + 1} of $maxCycles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                TextButton(onClick = onDismiss) {
                    Text("Skip", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

private enum class BreathPhase(val label: String) {
    INHALE("Breathe In"),
    HOLD("Hold"),
    EXHALE("Breathe Out"),
    REST("...")
}

// ═══════════════════════════════════════════════════════════════════════════════
// PRODUCTION-READY CALL HISTORY SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Production-ready Call History Screen with proper empty state handling
 * and neurodivergent-friendly design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroDivergentCallHistoryScreen(
    onBack: () -> Unit,
    onCallUser: (userId: String, userName: String, userAvatar: String, CallType) -> Unit = { _, _, _, _ -> },
    onOpenPracticeCallSelection: () -> Unit = {}
) {
    // Safely observe call history with proper null handling
    val callHistory by remember {
        derivedStateOf { MockCallManager.callHistory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Call History",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Practice calls button
                    IconButton(
                        onClick = onOpenPracticeCallSelection,
                        modifier = Modifier.semantics {
                            contentDescription = "Open practice calls"
                        }
                    ) {
                        Icon(Icons.Filled.Headset, "Practice Calls")
                    }
                    // Clear history button (only shown when there's history)
                    if (callHistory.isNotEmpty()) {
                        IconButton(
                            onClick = { MockCallManager.clearCallHistory() },
                            modifier = Modifier.semantics {
                                contentDescription = "Clear all call history"
                            }
                        ) {
                            Icon(Icons.Filled.DeleteSweep, "Clear History")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB for quick access to practice calls
            ExtendedFloatingActionButton(
                onClick = onOpenPracticeCallSelection,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Practice Call") }
            )
        }
    ) { padding ->
        if (callHistory.isEmpty()) {
            // Empty state with helpful guidance
            EmptyCallHistoryState(
                modifier = Modifier.padding(padding),
                onStartPractice = onOpenPracticeCallSelection
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = callHistory,
                    key = { it.id }
                ) { entry ->
                    NeuroDivergentCallHistoryItem(
                        entry = entry,
                        onCallBack = {
                            onCallUser(
                                entry.recipientId,
                                entry.recipientName,
                                entry.recipientAvatar,
                                entry.callType
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCallHistoryState(
    modifier: Modifier = Modifier,
    onStartPractice: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Friendly illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "No call history yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your voice and video calls will appear here.\nWant to practice first?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartPractice,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Headset, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Practice Calls")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Practice calls let you rehearse phone conversations with AI personas. " +
                                "It's a safe way to build confidence!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun NeuroDivergentCallHistoryItem(
    entry: CallHistoryEntry,
    onCallBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val callIcon = when (entry.callType) {
        CallType.VOICE -> Icons.Filled.Phone
        CallType.VIDEO -> Icons.Filled.Videocam
    }

    val directionIcon = when {
        entry.outcome == CallOutcome.MISSED -> Icons.AutoMirrored.Filled.CallMissed
        entry.isOutgoing -> Icons.AutoMirrored.Filled.CallMade
        else -> Icons.AutoMirrored.Filled.CallReceived
    }

    val outcomeColor = when (entry.outcome) {
        CallOutcome.MISSED -> MaterialTheme.colorScheme.error
        CallOutcome.DECLINED -> MaterialTheme.colorScheme.error
        CallOutcome.NO_ANSWER -> MaterialTheme.colorScheme.onSurfaceVariant
        CallOutcome.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
        CallOutcome.COMPLETED -> MaterialTheme.colorScheme.primary
    }

    val outcomeText = when (entry.outcome) {
        CallOutcome.COMPLETED -> if (entry.formattedDuration.isNotEmpty()) entry.formattedDuration else "Connected"
        CallOutcome.MISSED -> "Missed"
        CallOutcome.DECLINED -> "Declined"
        CallOutcome.NO_ANSWER -> "No answer"
        CallOutcome.CANCELLED -> "Cancelled"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (CallAccessibilitySettings.hapticFeedbackEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onCallBack()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics {
                contentDescription = "${entry.recipientName}, ${entry.callType.name} call, $outcomeText, ${entry.formattedTime}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with status indicator
        Box {
            AsyncImage(
                model = entry.recipientAvatar,
                contentDescription = entry.recipientName,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            // Call type badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(outcomeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        directionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = outcomeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Call info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.recipientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (entry.outcome == CallOutcome.MISSED)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    callIcon,
                    contentDescription = "${entry.callType.name} call",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    outcomeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = outcomeColor
                )
                Text(
                    " • ${entry.formattedTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Call back button - larger touch target for accessibility
        IconButton(
            onClick = {
                if (CallAccessibilitySettings.hapticFeedbackEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onCallBack()
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Icon(
                callIcon,
                contentDescription = "Call ${entry.recipientName}",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 82.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Get context-specific tips based on the persona
 */
private fun getCallTipsForPersona(persona: NeurodivergentPersona): List<String> {
    return when (persona) {
        NeurodivergentPersona.ADHD_FRIEND -> listOf(
            "Alex may jump between topics - that's okay! Just go with the flow.",
            "If you lose track, it's fine to say 'Wait, what were we talking about?'",
            "Short responses work great. No pressure for long answers.",
            "Alex is friendly and won't judge if you need things repeated."
        )
        NeurodivergentPersona.AUTISTIC_COLLEAGUE -> listOf(
            "Jordan prefers direct, clear communication.",
            "Be specific rather than using hints or suggestions.",
            "If Jordan asks for clarification, it's just to understand better.",
            "Literal language works best - avoid idioms if possible."
        )
        NeurodivergentPersona.ANXIOUS_CALLER -> listOf(
            "Sam may seem nervous - patience and reassurance help.",
            "Let Sam finish sentences, even if there are pauses.",
            "A calm, friendly tone will help Sam feel more comfortable.",
            "It's okay to acknowledge awkward moments with kindness."
        )
        NeurodivergentPersona.DYSLEXIC_TUTOR -> listOf(
            "Riley prefers verbal communication over text.",
            "Riley may pause to find the right words - that's normal.",
            "Patience is appreciated. Riley is very understanding too.",
            "Feel free to ask Riley to explain things differently."
        )
        NeurodivergentPersona.CUSTOMER_SERVICE -> listOf(
            "Practice stating what you need clearly.",
            "It's okay to say 'I'm not sure' or ask questions.",
            "Have your information ready (name, dates, etc.).",
            "Morgan is trained to be helpful and patient."
        )
    }
}


package com.kyilmaz.neurocomet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Neurodivergent-Friendly Widgets for NeuroComet
 *
 * Design Principles:
 * - Clear, unambiguous visuals
 * - High contrast options
 * - Reduced motion support
 * - Predictable interactions
 * - Minimal sensory overload
 * - Focus state indicators
 * - Larger touch targets
 * - Clear feedback on actions
 */

// ═══════════════════════════════════════════════════════════════
// FOCUS TIMER WIDGET - Helps with time blindness
// ═══════════════════════════════════════════════════════════════

/**
 * A focus timer widget that helps users with time blindness
 * Features:
 * - Large, clear time display
 * - Visual progress indicator
 * - Gentle color transitions
 * - Optional haptic feedback at intervals
 */
@Composable
fun FocusTimerWidget(
    remainingMinutes: Int,
    totalMinutes: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    highContrast: Boolean = false
) {
    val progress = if (totalMinutes > 0) remainingMinutes.toFloat() / totalMinutes else 0f

    // Animated progress color
    val progressColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> if (highContrast) Color.Green else Color(0xFF4CAF50)
            progress > 0.2f -> if (highContrast) Color.Yellow else Color(0xFFFFC107)
            else -> if (highContrast) Color.Red else Color(0xFFF44336)
        },
        animationSpec = if (reducedMotion) snap() else tween(500),
        label = "progressColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Focus timer: $remainingMinutes minutes remaining" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "🎯 Focus Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large time display
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (highContrast) Color.DarkGray
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 8.dp,
                        color = progressColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$remainingMinutes",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "minutes remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = if (highContrast) Color.White.copy(alpha = 0.7f)
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons - large touch targets
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start/Pause button
                LargeTouchButton(
                    onClick = if (isRunning) onPause else onStart,
                    icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause timer" else "Start timer",
                    backgroundColor = progressColor,
                    highContrast = highContrast
                )

                // Reset button
                LargeTouchButton(
                    onClick = onReset,
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Reset timer",
                    backgroundColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.outline,
                    highContrast = highContrast
                )
            }
        }
    }
}

/**
 * Large touch target button for accessibility
 */
@Composable
fun LargeTouchButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    highContrast: Boolean = false
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highContrast) Color.Black else Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ENERGY LEVEL WIDGET - Track social/sensory battery
// ═══════════════════════════════════════════════════════════════

/**
 * Energy level tracker widget
 * Helps users monitor and communicate their energy levels
 */
@Composable
fun EnergyLevelWidget(
    currentLevel: Int, // 0-100
    label: String = "Social Battery",
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    val levelColor = when {
        currentLevel >= 70 -> Color(0xFF4CAF50)
        currentLevel >= 40 -> Color(0xFFFFC107)
        currentLevel >= 20 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val emoji = when {
        currentLevel >= 70 -> "🔋"
        currentLevel >= 40 -> "🪫"
        currentLevel >= 20 -> "⚡"
        else -> "💤"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$label at $currentLevel percent" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$emoji $label",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentLevel%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large slider for easy interaction
            Slider(
                value = currentLevel.toFloat(),
                onValueChange = { onLevelChange(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = levelColor,
                    activeTrackColor = levelColor,
                    inactiveTrackColor = if (highContrast) Color.DarkGray
                                         else MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Quick select buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(25 to "Low", 50 to "Mid", 75 to "Good", 100 to "Full").forEach { (level, text) ->
                    FilterChip(
                        selected = currentLevel in (level - 12)..(level + 12),
                        onClick = { onLevelChange(level) },
                        label = { Text(text, fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TASK CHECKLIST WIDGET - Simple, clear task tracking
// ═══════════════════════════════════════════════════════════════

data class SimpleTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val emoji: String = "📝"
)

/**
 * Simple checklist widget with clear visual feedback
 */
@Composable
fun TaskChecklistWidget(
    tasks: List<SimpleTask>,
    onToggleTask: (String) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false,
    reducedMotion: Boolean = false
) {
    val completedCount = tasks.count { it.isCompleted }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ Today's Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$completedCount/${tasks.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (completedCount == tasks.size) Color(0xFF4CAF50)
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task list
            tasks.take(5).forEach { task ->
                TaskItem(
                    task = task,
                    onToggle = { onToggleTask(task.id) },
                    highContrast = highContrast,
                    reducedMotion = reducedMotion
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add task button
            OutlinedButton(
                onClick = onAddTask,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Task")
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: SimpleTask,
    onToggle: () -> Unit,
    highContrast: Boolean,
    reducedMotion: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            if (highContrast) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
        } else {
            if (highContrast) Color.DarkGray else MaterialTheme.colorScheme.surface
        },
        animationSpec = if (reducedMotion) snap() else tween(200),
        label = "taskBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onToggle)
            .padding(12.dp)
            .semantics {
                contentDescription = "${task.title}, ${if (task.isCompleted) "completed" else "not completed"}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Large checkbox area
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (task.isCompleted) Color(0xFF4CAF50)
                    else if (highContrast) Color.Gray else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${task.emoji} ${task.title}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.isCompleted) {
                if (highContrast) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            } else {
                if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// CALM CORNER WIDGET - Quick access to calming tools
// ═══════════════════════════════════════════════════════════════

data class CalmingTool(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String
)

val DEFAULT_CALMING_TOOLS = listOf(
    CalmingTool("breathe", "🌬️", "Breathing", "Guided breathing"),
    CalmingTool("ground", "🌍", "Grounding", "5-4-3-2-1 senses"),
    CalmingTool("music", "🎵", "Calm Sounds", "Relaxing audio"),
    CalmingTool("stim", "🌀", "Visual Stim", "Soothing visuals"),
    CalmingTool("timer", "⏳", "Break Timer", "Take a pause"),
    CalmingTool("affirm", "💜", "Affirmations", "Positive words")
)

@Composable
fun CalmCornerWidget(
    tools: List<CalmingTool> = DEFAULT_CALMING_TOOLS,
    onToolSelect: (CalmingTool) -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.Black
                            else Color(0xFFF3E5F5) // Soft lavender
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🧘 Calm Corner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highContrast) Color.White else Color(0xFF4A148C)
            )

            Text(
                text = "Take a moment when you need it",
                style = MaterialTheme.typography.bodySmall,
                color = if (highContrast) Color.White.copy(alpha = 0.7f) else Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tool grid - 3 columns
            val rows = tools.chunked(3)
            rows.forEach { rowTools ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowTools.forEach { tool ->
                        CalmingToolButton(
                            tool = tool,
                            onClick = { onToolSelect(tool) },
                            highContrast = highContrast
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CalmingToolButton(
    tool: CalmingTool,
    onClick: () -> Unit,
    highContrast: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .semantics { contentDescription = "${tool.name}: ${tool.description}" }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (highContrast) Color.DarkGray else Color.White
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tool.emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (highContrast) Color.White else Color(0xFF4A148C),
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// DAILY CHECK-IN WIDGET - Quick mood/state logging
// ═══════════════════════════════════════════════════════════════

data class MoodOption(
    val emoji: String,
    val label: String,
    val color: Color
)

val MOOD_OPTIONS = listOf(
    MoodOption("😊", "Great", Color(0xFF4CAF50)),
    MoodOption("🙂", "Good", Color(0xFF8BC34A)),
    MoodOption("😐", "Okay", Color(0xFFFFC107)),
    MoodOption("😔", "Low", Color(0xFFFF9800)),
    MoodOption("😣", "Rough", Color(0xFFF44336))
)

@Composable
fun DailyCheckInWidget(
    selectedMood: MoodOption?,
    onMoodSelect: (MoodOption) -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (highContrast) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large mood buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MOOD_OPTIONS.forEach { mood ->
                    val isSelected = selectedMood == mood

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onMoodSelect(mood) }
                            .background(
                                if (isSelected) mood.color.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .padding(8.dp)
                            .semantics { contentDescription = "Mood: ${mood.label}" }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 52.dp else 44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) mood.color
                                    else if (highContrast) Color.DarkGray
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) mood.color else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mood.emoji,
                                fontSize = if (isSelected) 28.sp else 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mood.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (highContrast) Color.White
                                   else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SENSORY ALERT WIDGET - Quick sensory status communication
// ═══════════════════════════════════════════════════════════════

enum class SensoryLevel(val emoji: String, val label: String, val color: Color) {
    GREEN("🟢", "All good", Color(0xFF4CAF50)),
    YELLOW("🟡", "Getting overwhelmed", Color(0xFFFFC107)),
    ORANGE("🟠", "Need a break soon", Color(0xFFFF9800)),
    RED("🔴", "Need help now", Color(0xFFF44336))
}

@Composable
fun SensoryAlertWidget(
    currentLevel: SensoryLevel,
    onLevelChange: (SensoryLevel) -> Unit,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = currentLevel.color.copy(alpha = if (highContrast) 0.8f else 0.15f)
        ),
        border = if (highContrast) {
            androidx.compose.foundation.BorderStroke(2.dp, currentLevel.color)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sensory Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) Color.White else currentLevel.color
                )
                Text(
                    text = "${currentLevel.emoji} ${currentLevel.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highContrast) Color.White else currentLevel.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SensoryLevel.entries.forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (level == currentLevel) level.color
                                else level.color.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (level == currentLevel) 3.dp else 0.dp,
                                color = if (highContrast) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onLevelChange(level) }
                            .semantics { contentDescription = "Set status to ${level.label}" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.emoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WIDGET DASHBOARD - Shows all widgets in one view
// ═══════════════════════════════════════════════════════════════

@Composable
fun NeurodivergentWidgetDashboard(
    modifier: Modifier = Modifier,
    highContrast: Boolean = false,
    reducedMotion: Boolean = false
) {
    var focusMinutes by remember { mutableIntStateOf(25) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var energyLevel by remember { mutableIntStateOf(70) }
    var selectedMood by remember { mutableStateOf<MoodOption?>(null) }
    var sensoryLevel by remember { mutableStateOf(SensoryLevel.GREEN) }
    var tasks by remember {
        mutableStateOf(
            listOf(
                SimpleTask("1", "Morning routine", emoji = "🌅"),
                SimpleTask("2", "Take meds", emoji = "💊"),
                SimpleTask("3", "Drink water", emoji = "💧"),
                SimpleTask("4", "Movement break", emoji = "🏃")
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "✨ Your Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DailyCheckInWidget(
                selectedMood = selectedMood,
                onMoodSelect = { selectedMood = it },
                highContrast = highContrast
            )
        }

        item {
            SensoryAlertWidget(
                currentLevel = sensoryLevel,
                onLevelChange = { sensoryLevel = it },
                highContrast = highContrast
            )
        }

        item {
            EnergyLevelWidget(
                currentLevel = energyLevel,
                onLevelChange = { energyLevel = it },
                highContrast = highContrast
            )
        }

        item {
            FocusTimerWidget(
                remainingMinutes = focusMinutes,
                totalMinutes = 25,
                isRunning = isTimerRunning,
                onStart = { isTimerRunning = true },
                onPause = { isTimerRunning = false },
                onReset = { focusMinutes = 25; isTimerRunning = false },
                highContrast = highContrast,
                reducedMotion = reducedMotion
            )
        }

        item {
            TaskChecklistWidget(
                tasks = tasks,
                onToggleTask = { taskId ->
                    tasks = tasks.map {
                        if (it.id == taskId) it.copy(isCompleted = !it.isCompleted)
                        else it
                    }
                },
                onAddTask = { /* Show add task dialog */ },
                highContrast = highContrast,
                reducedMotion = reducedMotion
            )
        }

        item {
            CalmCornerWidget(
                onToolSelect = { tool ->
                    // Navigate to calming tool
                },
                highContrast = highContrast
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}


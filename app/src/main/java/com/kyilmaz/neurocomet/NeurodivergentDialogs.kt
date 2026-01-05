package com.kyilmaz.neurocomet

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Neurodivergent-Friendly Dialog System
 *
 * Design Principles:
 * - Clear, unambiguous messaging
 * - Large touch targets (min 48dp)
 * - High contrast options
 * - Predictable button placement
 * - Optional haptic feedback for confirmations
 * - Reduced cognitive load
 * - Progress indicators that don't cause anxiety
 * - Gentle animations (can be disabled)
 * - Clear focus management
 * - Screen reader optimized
 */

// ═══════════════════════════════════════════════════════════════
// DIALOG TYPES
// ═══════════════════════════════════════════════════════════════

enum class DialogType {
    INFO,       // Blue - Information
    SUCCESS,    // Green - Success/confirmation
    WARNING,    // Orange - Caution/attention
    ERROR,      // Red - Error/problem
    QUESTION,   // Purple - Decision needed
    CUSTOM      // Custom styling
}

data class DialogConfig(
    val type: DialogType = DialogType.INFO,
    val title: String,
    val message: String,
    val emoji: String? = null,
    val primaryButtonText: String = "OK",
    val secondaryButtonText: String? = null,
    val showCloseButton: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val hapticOnConfirm: Boolean = true,
    val autoDismissMs: Long? = null,
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
// SIMPLE MESSAGE DIALOG
// ═══════════════════════════════════════════════════════════════

/**
 * A simple, clear pop-up message with neurodivergent-friendly design
 */
@Composable
fun NeurodivergentMessageDialog(
    config: DialogConfig,
    onDismiss: () -> Unit,
    onPrimaryClick: () -> Unit = onDismiss,
    onSecondaryClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Auto-dismiss timer
    LaunchedEffect(config.autoDismissMs) {
        config.autoDismissMs?.let { delay ->
            delay(delay)
            onDismiss()
        }
    }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = if (config.reducedMotion) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (config.reducedMotion) snap() else tween(200),
        label = "dialogAlpha"
    )

    Dialog(
        onDismissRequest = { if (config.dismissOnClickOutside) onDismiss() },
        properties = DialogProperties(
            dismissOnClickOutside = config.dismissOnClickOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .scale(scale)
                .semantics { contentDescription = "${config.title}: ${config.message}" },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.highContrast) Color.Black
                                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                if (config.showCloseButton) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = if (config.highContrast) Color.White
                                      else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Icon or emoji
                DialogIcon(
                    type = config.type,
                    emoji = config.emoji,
                    highContrast = config.highContrast
                )

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (config.highContrast) Color.White
                           else MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(12.dp))

                // Message
                Text(
                    text = config.message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (config.highContrast) Color.White.copy(alpha = 0.9f)
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(24.dp))

                // Buttons
                DialogButtons(
                    primaryText = config.primaryButtonText,
                    secondaryText = config.secondaryButtonText,
                    type = config.type,
                    highContrast = config.highContrast,
                    onPrimaryClick = {
                        if (config.hapticOnConfirm) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onPrimaryClick()
                    },
                    onSecondaryClick = onSecondaryClick
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun DialogIcon(
    type: DialogType,
    emoji: String?,
    highContrast: Boolean
) {
    val (icon, colors) = when (type) {
        DialogType.INFO -> Icons.Outlined.Info to listOf(Color(0xFF2196F3), Color(0xFF1976D2))
        DialogType.SUCCESS -> Icons.Outlined.CheckCircle to listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
        DialogType.WARNING -> Icons.Outlined.Warning to listOf(Color(0xFFFF9800), Color(0xFFF57C00))
        DialogType.ERROR -> Icons.Outlined.Error to listOf(Color(0xFFF44336), Color(0xFFD32F2F))
        DialogType.QUESTION -> Icons.Outlined.HelpOutline to listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2))
        DialogType.CUSTOM -> Icons.Outlined.Star to listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors.map { if (highContrast) it else it.copy(alpha = 0.15f) }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (emoji != null) {
            Text(
                text = emoji,
                fontSize = 36.sp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highContrast) Color.White else colors.first(),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun DialogButtons(
    primaryText: String,
    secondaryText: String?,
    type: DialogType,
    highContrast: Boolean,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)?
) {
    val primaryColor = when (type) {
        DialogType.INFO -> Color(0xFF2196F3)
        DialogType.SUCCESS -> Color(0xFF4CAF50)
        DialogType.WARNING -> Color(0xFFFF9800)
        DialogType.ERROR -> Color(0xFFF44336)
        DialogType.QUESTION -> Color(0xFF9C27B0)
        DialogType.CUSTOM -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary button - large touch target
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (highContrast) Color.White else primaryColor,
                contentColor = if (highContrast) Color.Black else Color.White
            )
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Secondary button
        if (secondaryText != null && onSecondaryClick != null) {
            OutlinedButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(
                        if (highContrast) listOf(Color.White, Color.White)
                        else listOf(primaryColor, primaryColor)
                    )
                )
            ) {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (highContrast) Color.White else primaryColor
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// INPUT DIALOG
// ═══════════════════════════════════════════════════════════════

/**
 * Dialog for requesting user input with neurodivergent-friendly design
 */
@Composable
fun NeurodivergentInputDialog(
    title: String,
    message: String,
    placeholder: String = "",
    initialValue: String = "",
    inputType: InputType = InputType.TEXT,
    maxLength: Int? = null,
    confirmButtonText: String = "Confirm",
    cancelButtonText: String = "Cancel",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    emoji: String? = null,
    highContrast: Boolean = false,
    reducedMotion: Boolean = false,
    validation: ((String) -> String?)? = null // Returns error message or null if valid
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the input
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                if (emoji != null) {
                    Text(emoji, fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (highContrast) Color.White.copy(alpha = 0.8f)
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                // Input field
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { newValue ->
                        if (maxLength == null || newValue.length <= maxLength) {
                            inputValue = newValue
                            errorMessage = validation?.invoke(newValue)
                        }
                    },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = inputType != InputType.MULTILINE,
                    maxLines = if (inputType == InputType.MULTILINE) 4 else 1,
                    isError = errorMessage != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                            if (maxLength != null) {
                                Text(
                                    text = "${inputValue.length}/$maxLength",
                                    color = if (inputValue.length >= maxLength) MaterialTheme.colorScheme.error
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    visualTransformation = if (inputType == InputType.PASSWORD && !showPassword)
                        PasswordVisualTransformation()
                    else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when (inputType) {
                            InputType.EMAIL -> KeyboardType.Email
                            InputType.NUMBER -> KeyboardType.Number
                            InputType.PHONE -> KeyboardType.Phone
                            InputType.PASSWORD -> KeyboardType.Password
                            else -> KeyboardType.Text
                        },
                        imeAction = if (inputType == InputType.MULTILINE) ImeAction.Default else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (errorMessage == null && inputValue.isNotBlank()) {
                                onConfirm(inputValue)
                            }
                        }
                    ),
                    trailingIcon = if (inputType == InputType.PASSWORD) {
                        {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(cancelButtonText)
                    }

                    Button(
                        onClick = { onConfirm(inputValue) },
                        enabled = inputValue.isNotBlank() && errorMessage == null,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                            contentColor = if (highContrast) Color.Black else Color.White
                        )
                    ) {
                        Text(confirmButtonText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

enum class InputType {
    TEXT,
    EMAIL,
    NUMBER,
    PHONE,
    PASSWORD,
    MULTILINE
}

// ═══════════════════════════════════════════════════════════════
// CHOICE DIALOG
// ═══════════════════════════════════════════════════════════════

data class DialogChoice(
    val id: String,
    val label: String,
    val description: String? = null,
    val emoji: String? = null,
    val icon: ImageVector? = null
)

/**
 * Dialog for selecting from multiple options
 */
@Composable
fun NeurodivergentChoiceDialog(
    title: String,
    message: String? = null,
    choices: List<DialogChoice>,
    onDismiss: () -> Unit,
    onSelect: (DialogChoice) -> Unit,
    multiSelect: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    confirmButtonText: String = "Confirm",
    highContrast: Boolean = false
) {
    var selectedChoices by remember { mutableStateOf(selectedIds) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )

                if (message != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (highContrast) Color.White.copy(alpha = 0.8f)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Choices
                choices.forEach { choice ->
                    val isSelected = choice.id in selectedChoices

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (multiSelect) {
                                    selectedChoices = if (isSelected) {
                                        selectedChoices - choice.id
                                    } else {
                                        selectedChoices + choice.id
                                    }
                                } else {
                                    onSelect(choice)
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (highContrast) Color.White.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                if (highContrast) Color.DarkGray
                                else MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (highContrast) Color.White else MaterialTheme.colorScheme.primary
                            )
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon or emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (highContrast) Color.Gray.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (choice.emoji != null) {
                                    Text(choice.emoji, fontSize = 20.sp)
                                } else if (choice.icon != null) {
                                    Icon(
                                        choice.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = choice.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (highContrast) Color.White
                                           else MaterialTheme.colorScheme.onSurface
                                )
                                if (choice.description != null) {
                                    Text(
                                        text = choice.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (highContrast) Color.Gray
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (multiSelect) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = if (highContrast) Color.White
                                                      else MaterialTheme.colorScheme.primary,
                                        checkmarkColor = if (highContrast) Color.Black else Color.White
                                    )
                                )
                            } else if (isSelected) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = if (highContrast) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Confirm button for multi-select
                if (multiSelect) {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            choices.filter { it.id in selectedChoices }.forEach(onSelect)
                        },
                        enabled = selectedChoices.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(confirmButtonText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LOADING DIALOG (Anxiety-Reduced)
// ═══════════════════════════════════════════════════════════════

/**
 * A calming loading dialog that doesn't induce anxiety
 */
@Composable
fun NeurodivergentLoadingDialog(
    message: String = "Please wait...",
    subMessage: String? = null,
    showProgress: Boolean = true,
    progress: Float? = null, // null for indeterminate
    emoji: String = "⏳",
    onDismiss: (() -> Unit)? = null,
    highContrast: Boolean = false
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = onDismiss != null
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calming emoji animation
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "emojiScale"
                )

                Text(
                    text = emoji,
                    fontSize = 48.sp,
                    modifier = Modifier.scale(scale)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (highContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )

                if (subMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = if (highContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showProgress) {
                    Spacer(Modifier.height(24.dp))

                    if (progress != null) {
                        // Determinate progress
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
                            trackColor = if (highContrast) Color.Gray else MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (highContrast) Color.White else MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // Indeterminate - gentle pulsing dots instead of spinning
                        CalmingLoadingIndicator(highContrast = highContrast)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalmingLoadingIndicator(
    highContrast: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        (if (highContrast) Color.White else MaterialTheme.colorScheme.primary)
                            .copy(alpha = alpha)
                    )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TOAST-STYLE SNACKBAR (Less Intrusive)
// ═══════════════════════════════════════════════════════════════

/**
 * Show a brief, non-intrusive message
 */
@Composable
fun NeurodivergentToast(
    message: String,
    emoji: String? = null,
    type: DialogType = DialogType.INFO,
    durationMs: Long = 3000L,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(durationMs)
        isVisible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        val backgroundColor = when (type) {
            DialogType.SUCCESS -> Color(0xFF4CAF50)
            DialogType.ERROR -> Color(0xFFF44336)
            DialogType.WARNING -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.inverseSurface
        }

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (emoji != null) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HAPTIC FEEDBACK HELPER
// ═══════════════════════════════════════════════════════════════

/**
 * Provides gentle haptic feedback for confirmations
 */
fun provideGentleHaptic(context: Context, type: HapticType = HapticType.CONFIRM) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = when (type) {
            HapticType.CONFIRM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            HapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            HapticType.ERROR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            HapticType.WARNING -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}

enum class HapticType {
    CONFIRM,
    SUCCESS,
    ERROR,
    WARNING
}


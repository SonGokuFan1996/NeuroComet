package com.kyilmaz.neurocomet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Parental Controls Screen - Full-featured parental control management.
 *
 * Features:
 * - PIN setup and verification with lockout protection
 * - Daily screen time limits
 * - Bedtime restrictions
 * - Content filtering levels
 * - Feature blocking (DMs, Explore, Posting)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentalControlsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var parentalState by remember { mutableStateOf(ParentalControlsSettings.getState(context)) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showPinVerify by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    // Developer bypass: Auto-authenticate in debug builds
    val isDevDevice = remember { ParentalControlsSettings.isDevDevice(context) }
    var isAuthenticated by remember { mutableStateOf(isDevDevice) }

    // Refresh state periodically
    LaunchedEffect(Unit) {
        while (true) {
            parentalState = ParentalControlsSettings.getState(context)
            delay(1000)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Parental Controls", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            StatusCard(parentalState)

            if (!parentalState.isPinSet) {
                // No PIN set - show setup prompt
                SetupPromptCard(onSetupClick = { showPinSetup = true })
            } else if (!isAuthenticated) {
                // PIN set but not authenticated - show verify prompt
                VerifyPromptCard(
                    isLockedOut = parentalState.isLockedOut,
                    lockoutRemainingMs = parentalState.lockoutRemainingMs,
                    onVerifyClick = { showPinVerify = true }
                )
            } else {
                // Authenticated - show all controls

                // Enable/Disable Toggle
                ControlCard(
                    title = "Parental Controls",
                    icon = Icons.Default.Shield,
                    description = "Enable or disable all parental restrictions"
                ) {
                    Switch(
                        checked = parentalState.isEnabled,
                        onCheckedChange = {
                            ParentalControlsSettings.setEnabled(context, it)
                            parentalState = ParentalControlsSettings.getState(context)
                        }
                    )
                }

                AnimatedVisibility(visible = parentalState.isEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Screen Time Limits
                        ScreenTimeLimitCard(
                            maxMinutes = parentalState.maxDailyMinutes,
                            currentUsage = parentalState.dailyUsageMinutes,
                            onMaxMinutesChange = { minutes ->
                                ParentalControlsSettings.updateSettings(context, maxDailyMinutes = minutes)
                                parentalState = ParentalControlsSettings.getState(context)
                            }
                        )

                        // Bedtime Restrictions
                        BedtimeCard(
                            isEnabled = parentalState.bedtimeEnabled,
                            startHour = parentalState.bedtimeStartHour,
                            startMinute = parentalState.bedtimeStartMinute,
                            endHour = parentalState.bedtimeEndHour,
                            endMinute = parentalState.bedtimeEndMinute,
                            onEnabledChange = { enabled ->
                                ParentalControlsSettings.updateSettings(context, bedtimeEnabled = enabled)
                                parentalState = ParentalControlsSettings.getState(context)
                            },
                            onTimeChange = { startH, startM, endH, endM ->
                                ParentalControlsSettings.updateSettings(
                                    context,
                                    bedtimeStartHour = startH,
                                    bedtimeStartMinute = startM,
                                    bedtimeEndHour = endH,
                                    bedtimeEndMinute = endM
                                )
                                parentalState = ParentalControlsSettings.getState(context)
                            }
                        )

                        // Content Filtering
                        ContentFilterCard(
                            currentLevel = parentalState.contentFilterLevel,
                            onLevelChange = { level ->
                                ParentalControlsSettings.updateSettings(context, contentFilterLevel = level)
                                parentalState = ParentalControlsSettings.getState(context)
                            }
                        )

                        // Feature Restrictions
                        FeatureRestrictionsCard(
                            blockDMs = parentalState.blockDMs,
                            blockExplore = parentalState.blockExplore,
                            blockPosting = parentalState.blockPosting,
                            requireApprovalForFollows = parentalState.requireApprovalForFollows,
                            onBlockDMsChange = {
                                ParentalControlsSettings.updateSettings(context, blockDMs = it)
                                parentalState = ParentalControlsSettings.getState(context)
                            },
                            onBlockExploreChange = {
                                ParentalControlsSettings.updateSettings(context, blockExplore = it)
                                parentalState = ParentalControlsSettings.getState(context)
                            },
                            onBlockPostingChange = {
                                ParentalControlsSettings.updateSettings(context, blockPosting = it)
                                parentalState = ParentalControlsSettings.getState(context)
                            },
                            onRequireApprovalChange = {
                                ParentalControlsSettings.updateSettings(context, requireApprovalForFollows = it)
                                parentalState = ParentalControlsSettings.getState(context)
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // PIN Management
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Change PIN")
                    }

                    OutlinedButton(
                        onClick = { showRemoveDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onPinSet = { pin ->
                if (ParentalControlsSettings.setPin(context, pin)) {
                    parentalState = ParentalControlsSettings.getState(context)
                    isAuthenticated = true
                    showPinSetup = false
                }
            }
        )
    }

    // PIN Verify Dialog
    if (showPinVerify) {
        PinVerifyDialog(
            onDismiss = { showPinVerify = false },
            onVerify = { pin ->
                when (val result = ParentalControlsSettings.verifyPin(context, pin)) {
                    is ParentalControlsSettings.PinVerifyResult.Success -> {
                        isAuthenticated = true
                        showPinVerify = false
                        true
                    }
                    else -> false
                }
            },
            onStateUpdate = { parentalState = ParentalControlsSettings.getState(context) }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onPinChanged = {
                parentalState = ParentalControlsSettings.getState(context)
                showChangePinDialog = false
            }
        )
    }

    // Remove Parental Controls Dialog
    if (showRemoveDialog) {
        RemoveControlsDialog(
            onDismiss = { showRemoveDialog = false },
            onRemoved = {
                parentalState = ParentalControlsSettings.getState(context)
                isAuthenticated = false
                showRemoveDialog = false
            }
        )
    }
}

@Composable
private fun StatusCard(state: ParentalControlsSettings.ParentalControlState) {
    val (statusText, statusColor, statusIcon) = when {
        !state.isPinSet -> Triple("Not Set Up", MaterialTheme.colorScheme.outline, Icons.Default.LockOpen)
        !state.isEnabled -> Triple("Disabled", MaterialTheme.colorScheme.outline, Icons.Default.LockOpen)
        state.isLockedOut -> Triple("Locked Out", MaterialTheme.colorScheme.error, Icons.Default.Block)
        state.isDuringBedtime -> Triple("Bedtime Active", MaterialTheme.colorScheme.tertiary, Icons.Default.NightsStay)
        state.isOverDailyLimit -> Triple("Time Limit Reached", MaterialTheme.colorScheme.error, Icons.Default.AccessTime)
        else -> Triple("Active", MaterialTheme.colorScheme.primary, Icons.Default.Shield)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
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
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, contentDescription = null, tint = statusColor)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Status: $statusText",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (state.isEnabled && state.maxDailyMinutes > 0) {
                    Text(
                        "Usage: ${state.dailyUsageMinutes}/${state.maxDailyMinutes} minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupPromptCard(onSetupClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Set Up Parental Controls",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Create a PIN to manage screen time limits, bedtime restrictions, and content filtering.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onSetupClick) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create PIN")
            }
        }
    }
}

@Composable
private fun VerifyPromptCard(
    isLockedOut: Boolean,
    lockoutRemainingMs: Long,
    onVerifyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLockedOut)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isLockedOut) Icons.Default.Block else Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isLockedOut) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            if (isLockedOut) {
                val remainingMinutes = (lockoutRemainingMs / 60000).toInt()
                Text(
                    "Too Many Attempts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Please try again in $remainingMinutes minutes.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    "Enter PIN to Continue",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Verify your parental control PIN to view and modify settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onVerifyClick) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enter PIN")
                }
            }
        }
    }
}

@Composable
private fun ControlCard(
    title: String,
    icon: ImageVector,
    description: String,
    trailing: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}

@Composable
private fun ScreenTimeLimitCard(
    maxMinutes: Int,
    currentUsage: Int,
    onMaxMinutesChange: (Int) -> Unit
) {
    var sliderValue by remember(maxMinutes) { mutableStateOf(maxMinutes.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text("Daily Screen Time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            val limitText = if (maxMinutes == 0) "Unlimited" else "${maxMinutes / 60}h ${maxMinutes % 60}m"
            Text(
                "Limit: $limitText",
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onMaxMinutesChange(sliderValue.toInt()) },
                valueRange = 0f..480f, // 0 to 8 hours
                steps = 15
            )

            if (maxMinutes > 0) {
                val progress = (currentUsage.toFloat() / maxMinutes).coerceIn(0f, 1f)
                Text(
                    "Today: $currentUsage/$maxMinutes minutes (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BedtimeCard(
    isEnabled: Boolean,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bedtime Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Block app during bedtime hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isEnabled, onCheckedChange = onEnabledChange)
            }

            AnimatedVisibility(visible = isEnabled) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimeSelector(
                            label = "Start",
                            hour = startHour,
                            minute = startMinute,
                            onTimeChange = { h, m -> onTimeChange(h, m, endHour, endMinute) }
                        )
                        TimeSelector(
                            label = "End",
                            hour = endHour,
                            minute = endMinute,
                            onTimeChange = { h, m -> onTimeChange(startHour, startMinute, h, m) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSelector(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    var showHourPicker by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { showHourPicker = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = String.format("%02d:%02d", hour, minute),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showHourPicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showHourPicker = false },
            onConfirm = { h, m ->
                onTimeChange(h, m)
                showHourPicker = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hour picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { hour = (hour + 1) % 24 }) {
                        Text("▲")
                    }
                    Text(
                        String.format("%02d", hour),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { hour = if (hour > 0) hour - 1 else 23 }) {
                        Text("▼")
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))

                // Minute picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 15) % 60 }) {
                        Text("▲")
                    }
                    Text(
                        String.format("%02d", minute),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { minute = if (minute >= 15) minute - 15 else 45 }) {
                        Text("▼")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hour, minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ContentFilterCard(
    currentLevel: ParentalControlsSettings.ContentFilterLevel,
    onLevelChange: (ParentalControlsSettings.ContentFilterLevel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text("Content Filtering", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            ParentalControlsSettings.ContentFilterLevel.entries.forEach { level ->
                val (title, description) = when (level) {
                    ParentalControlsSettings.ContentFilterLevel.OFF -> "Off" to "No content filtering"
                    ParentalControlsSettings.ContentFilterLevel.MODERATE -> "Moderate" to "Filter explicit content"
                    ParentalControlsSettings.ContentFilterLevel.STRICT -> "Strict" to "Filter explicit & suggestive content"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLevelChange(level) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (currentLevel == level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentLevel == level) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = if (currentLevel == level) FontWeight.Bold else FontWeight.Normal)
                        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRestrictionsCard(
    blockDMs: Boolean,
    blockExplore: Boolean,
    blockPosting: Boolean,
    requireApprovalForFollows: Boolean,
    onBlockDMsChange: (Boolean) -> Unit,
    onBlockExploreChange: (Boolean) -> Unit,
    onBlockPostingChange: (Boolean) -> Unit,
    onRequireApprovalChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text("Feature Restrictions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            FeatureToggleRow("Block Direct Messages", "Prevent sending and receiving DMs", blockDMs, onBlockDMsChange)
            FeatureToggleRow("Block Explore", "Hide the Explore tab", blockExplore, onBlockExploreChange)
            FeatureToggleRow("Block Posting", "Prevent creating new posts", blockPosting, onBlockPostingChange)
            FeatureToggleRow("Approve Follows", "Require approval for new follows", requireApprovalForFollows, onRequireApprovalChange)
        }
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Parental PIN") },
        text = {
            Column {
                Text(
                    "Create a 4-8 digit PIN. Keep this PIN secret from your child.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("PIN") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        pin.length < 4 -> error = "PIN must be at least 4 digits"
                        pin != confirmPin -> error = "PINs don't match"
                        else -> onPinSet(pin)
                    }
                }
            ) {
                Text("Create PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PinVerifyDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean,
    onStateUpdate: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Parental PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("PIN") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (val result = ParentalControlsSettings.verifyPin(context, pin)) {
                        is ParentalControlsSettings.PinVerifyResult.Success -> {
                            onVerify(pin)
                        }
                        is ParentalControlsSettings.PinVerifyResult.Incorrect -> {
                            error = "Incorrect PIN. ${result.attemptsRemaining} attempts remaining."
                            pin = ""
                            onStateUpdate()
                        }
                        is ParentalControlsSettings.PinVerifyResult.LockedOut -> {
                            error = "Too many attempts. Try again later."
                            onStateUpdate()
                            onDismiss()
                        }
                        is ParentalControlsSettings.PinVerifyResult.NoPinSet -> {
                            error = "No PIN set."
                        }
                    }
                }
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onPinChanged: () -> Unit
) {
    val context = LocalContext.current
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPins by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) oldPin = it },
                    label = { Text("Current PIN") },
                    visualTransformation = if (showPins) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New PIN") },
                    visualTransformation = if (showPins) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = if (showPins) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showPins = !showPins }) {
                            Icon(
                                if (showPins) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPin.length < 4 -> error = "New PIN must be at least 4 digits"
                        newPin != confirmPin -> error = "New PINs don't match"
                        else -> {
                            if (ParentalControlsSettings.changePin(context, oldPin, newPin)) {
                                onPinChanged()
                            } else {
                                error = "Current PIN is incorrect"
                            }
                        }
                    }
                }
            ) {
                Text("Change PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RemoveControlsDialog(
    onDismiss: () -> Unit,
    onRemoved: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Remove Parental Controls?") },
        text = {
            Column {
                Text(
                    "This will remove all parental control settings including time limits, bedtime restrictions, and content filters.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("Enter PIN to confirm") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ParentalControlsSettings.removeParentalControls(context, pin)) {
                        onRemoved()
                    } else {
                        error = "Incorrect PIN"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


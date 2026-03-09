@file:Suppress("unused")

package com.kyilmaz.neurocomet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ═══════════════════════════════════════════════════════════════════════════════
// BACKUP SETTINGS SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onBack: () -> Unit,
    backupViewModel: BackupViewModel = viewModel()
) {
    // SECURITY: Only whitelisted dev devices may access backup testing
    val context = LocalContext.current
    if (!DeviceAuthority.isAuthorizedDevice(context)) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val state by backupViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error/success messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            backupViewModel.clearMessages()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            backupViewModel.clearMessages()
        }
    }

    // Dialog states
    var showRestoreConfirm by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<String?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showEncryptionInfo by remember { mutableStateOf(false) }
    var showFrequencyPicker by remember { mutableStateOf(false) }
    var showBackupDestinationPicker by remember { mutableStateOf(false) }
    var pendingExportBackupId by remember { mutableStateOf<String?>(null) }
    var showImportOptions by remember { mutableStateOf(false) }

    // SAF launcher: create a new backup file at a user-chosen location (Google Drive, Downloads, etc.)
    val exportNewBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { backupViewModel.exportBackupToUri(it) }
    }

    // SAF launcher: export an existing backup to a user-chosen location
    val exportExistingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val backupId = pendingExportBackupId
        pendingExportBackupId = null
        if (uri != null && backupId != null) {
            backupViewModel.exportExistingBackupToUri(backupId, uri)
        }
    }

    // SAF launcher: pick a .ncb file to import (from Google Drive, Downloads, etc.)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { backupViewModel.importBackupFromUri(it) }
    }

    // SAF launcher: pick a .ncb file to import and immediately restore
    val importAndRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { backupViewModel.importAndRestoreFromUri(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup & Storage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ═══════════════════════════════════════
                // LAST BACKUP STATUS CARD
                // ═══════════════════════════════════════
                item {
                    LastBackupCard(settings = state.settings)
                }

                // ═══════════════════════════════════════
                // BACK UP NOW
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("Back Up", Icons.Default.CloudUpload)
                }
                item {
                    BackupDestinationsCard(
                        isBackingUp = state.isBackingUp,
                        progress = state.progress,
                        onBackupToDevice = { backupViewModel.createBackup() },
                        onBackupToFile = {
                            exportNewBackupLauncher.launch(backupViewModel.getSuggestedExportFilename())
                        },
                        onShowDestinationPicker = { showBackupDestinationPicker = true }
                    )
                }

                // ═══════════════════════════════════════
                // IMPORT / RESTORE FROM FILE
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("Import", Icons.Default.FileDownload)
                }
                item {
                    ImportCard(
                        isRestoring = state.isRestoring,
                        onImportOnly = {
                            importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        },
                        onImportAndRestore = {
                            importAndRestoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        }
                    )
                }

                // ═══════════════════════════════════════
                // AUTO-BACKUP
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("Auto-Backup", Icons.Default.Schedule)
                }
                item {
                    AutoBackupCard(
                        settings = state.settings,
                        onFrequencyClick = { showFrequencyPicker = true },
                        onWifiOnlyChanged = { value ->
                            backupViewModel.updateSettings { it.copy(wifiOnly = value) }
                        }
                    )
                }

                // ═══════════════════════════════════════
                // WHAT TO BACK UP
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("What to Back Up", Icons.Default.Checklist)
                }
                item {
                    BackupScopeCard(
                        scope = state.settings.scope,
                        onScopeChanged = { scope ->
                            backupViewModel.updateSettings { it.copy(scope = scope) }
                        }
                    )
                }

                // ═══════════════════════════════════════
                // SECURITY
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("Security", Icons.Default.Lock)
                }
                item {
                    EncryptionCard(
                        isEncrypted = state.settings.encryptBackups,
                        onChanged = { value ->
                            if (value) {
                                showEncryptionInfo = true
                            } else {
                                backupViewModel.updateSettings { it.copy(encryptBackups = false) }
                            }
                        }
                    )
                }

                // ═══════════════════════════════════════
                // RESTORE
                // ═══════════════════════════════════════
                item {
                    BackupSectionHeader("Restore", Icons.Default.Restore)
                }

                if (state.isRestoring) {
                    item {
                        ProgressCard(progress = state.progress)
                    }
                } else if (state.localBackups.isEmpty()) {
                    item {
                        EmptyBackupsCard()
                    }
                } else {
                    items(state.localBackups, key = { it.backupId }) { metadata ->
                        BackupListItem(
                            metadata = metadata,
                            onRestore = { showRestoreConfirm = metadata.backupId },
                            onDelete = { showDeleteConfirm = metadata.backupId },
                            onShare = { backupViewModel.shareBackup(metadata.backupId) },
                            onExport = {
                                pendingExportBackupId = metadata.backupId
                                val ts = metadata.formattedDate.replace(Regex("[^a-zA-Z0-9]"), "_")
                                exportExistingLauncher.launch("NeuroComet_Backup_$ts.ncb")
                            }
                        )
                    }
                }

                // ═══════════════════════════════════════
                // STORAGE MANAGEMENT
                // ═══════════════════════════════════════
                if (state.localBackups.isNotEmpty()) {
                    item {
                        BackupSectionHeader("Storage Management", Icons.Default.Storage)
                    }
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text("Delete All Local Backups") },
                                supportingContent = {
                                    Text("${state.localBackups.size} backup${if (state.localBackups.size == 1) "" else "s"} stored")
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                modifier = Modifier.clickable { showDeleteAllConfirm = true }
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ═══════════════════════════════════════
    // DIALOGS
    // ═══════════════════════════════════════

    showRestoreConfirm?.let { backupId ->
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = null },
            icon = { Icon(Icons.Default.Restore, contentDescription = null) },
            title = { Text("Restore Backup") },
            text = {
                Text("This will restore your data from the selected backup. " +
                     "Current data that conflicts with the backup will be overwritten.\n\n" +
                     "Are you sure you want to continue?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = null
                    backupViewModel.restoreBackup(backupId)
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = null }) { Text("Cancel") }
            }
        )
    }

    showDeleteConfirm?.let { backupId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Backup") },
            text = { Text("This backup will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = null
                        backupViewModel.deleteBackup(backupId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("Delete All Backups") },
            text = { Text("All local backups will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllConfirm = false
                        backupViewModel.deleteAllBackups()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showEncryptionInfo) {
        AlertDialog(
            onDismissRequest = { showEncryptionInfo = false },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text("Encrypt Backups") },
            text = {
                Column {
                    Text(
                        "Encrypted backups protect your data with a passphrase. " +
                        "If you forget your passphrase, you will not be able to restore your backup."
                    )
                    Spacer(Modifier.height(12.dp))
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Passphrase cannot be recovered if lost!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEncryptionInfo = false
                    backupViewModel.updateSettings { it.copy(encryptBackups = true) }
                }) { Text("Enable") }
            },
            dismissButton = {
                TextButton(onClick = { showEncryptionInfo = false }) { Text("Cancel") }
            }
        )
    }

    if (showFrequencyPicker) {
        val options = BackupFrequency.entries
        val labels = mapOf(
            BackupFrequency.OFF to "Off",
            BackupFrequency.DAILY to "Daily",
            BackupFrequency.WEEKLY to "Weekly",
            BackupFrequency.MONTHLY to "Monthly"
        )
        val descriptions = mapOf(
            BackupFrequency.OFF to "Manual backups only",
            BackupFrequency.DAILY to "Every 24 hours",
            BackupFrequency.WEEKLY to "Once a week",
            BackupFrequency.MONTHLY to "Once a month"
        )
        AlertDialog(
            onDismissRequest = { showFrequencyPicker = false },
            title = { Text("Backup Frequency", style = MaterialTheme.typography.titleSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    options.forEach { freq ->
                        val isSelected = state.settings.autoBackupFrequency == freq
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    backupViewModel.updateSettings { it.copy(autoBackupFrequency = freq) }
                                    showFrequencyPicker = false
                                }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    backupViewModel.updateSettings { it.copy(autoBackupFrequency = freq) }
                                    showFrequencyPicker = false
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    labels[freq] ?: freq.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    descriptions[freq] ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SUB-COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BackupSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LastBackupCard(settings: BackupSettings) {
    val hasBackup = settings.lastBackupAt != null

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (hasBackup)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (hasBackup) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (hasBackup)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (hasBackup) "Last Backup" else "No Backups Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasBackup)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            if (hasBackup) {
                Spacer(Modifier.height(12.dp))
                val formattedDate = try {
                    val instant = java.time.Instant.parse(settings.lastBackupAt)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")
                        .withZone(java.time.ZoneId.systemDefault())
                    formatter.format(instant)
                } catch (_: Exception) { settings.lastBackupAt }

                Text(
                    formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
                settings.lastBackupSizeBytes?.let { bytes ->
                    Spacer(Modifier.height(4.dp))
                    val sizeStr = when {
                        bytes < 1024 -> "$bytes B"
                        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
                        else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
                    }
                    Text(
                        "Size: $sizeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Back up your data to keep it safe. You can restore it on this or another device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BackupDestinationsCard(
    isBackingUp: Boolean,
    progress: BackupProgress,
    onBackupToDevice: () -> Unit,
    onBackupToFile: () -> Unit,
    onShowDestinationPicker: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isBackingUp) {
            ProgressCard(progress = progress)
        } else {
            Column {
                // ── Back up to this device ──
                ListItem(
                    headlineContent = { Text("Back Up to Device") },
                    supportingContent = { Text("Save locally on this phone") },
                    leadingContent = {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onBackupToDevice)
                )

                HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))

                // ── Export to Google Drive / file ──
                ListItem(
                    headlineContent = { Text("Export to Google Drive or File") },
                    supportingContent = { Text("Save to Google Drive, Downloads, USB, or any cloud storage") },
                    leadingContent = {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF4285F4))
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onBackupToFile)
                )

                // Hint text
                Row(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "The file picker lets you choose Google Drive, Downloads, or any installed cloud app.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportCard(
    isRestoring: Boolean,
    onImportOnly: () -> Unit,
    onImportAndRestore: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isRestoring) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                Spacer(Modifier.height(12.dp))
                Text("Importing...", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column {
                ListItem(
                    headlineContent = { Text("Import from File") },
                    supportingContent = { Text("Import a .ncb backup from Google Drive, Downloads, or another location") },
                    leadingContent = {
                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onImportOnly)
                )

                HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))

                ListItem(
                    headlineContent = { Text("Import & Restore") },
                    supportingContent = { Text("Import a backup file and immediately restore all data") },
                    leadingContent = {
                        Icon(Icons.Default.RestorePage, contentDescription = null, tint = Color(0xFFFF6E40))
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onImportAndRestore)
                )
            }
        }
    }
}

@Composable
private fun BackupNowCard(
    isBackingUp: Boolean,
    progress: BackupProgress,
    onBackup: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isBackingUp) {
            ProgressCard(progress = progress)
        } else {
            ListItem(
                headlineContent = { Text("Back Up to Device") },
                supportingContent = { Text("Save a backup to this device") },
                leadingContent = {
                    Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                trailingContent = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onBackup)
            )
        }
    }
}

@Composable
private fun ProgressCard(progress: BackupProgress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = { if (progress.progress > 0) progress.progress else 0f },
            modifier = Modifier.size(60.dp),
            strokeWidth = 4.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            progress.stage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { if (progress.progress > 0) progress.progress else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun AutoBackupCard(
    settings: BackupSettings,
    onFrequencyClick: () -> Unit,
    onWifiOnlyChanged: (Boolean) -> Unit
) {
    val freqLabels = mapOf(
        BackupFrequency.OFF to "Off",
        BackupFrequency.DAILY to "Daily",
        BackupFrequency.WEEKLY to "Weekly",
        BackupFrequency.MONTHLY to "Monthly"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        ListItem(
            headlineContent = { Text("Backup Frequency") },
            supportingContent = { Text(freqLabels[settings.autoBackupFrequency] ?: "Off") },
            leadingContent = {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            },
            modifier = Modifier.clickable(onClick = onFrequencyClick)
        )
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
        ListItem(
            headlineContent = { Text("Wi-Fi Only") },
            supportingContent = { Text("Only back up when connected to Wi-Fi") },
            leadingContent = {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            },
            trailingContent = {
                Switch(
                    checked = settings.wifiOnly,
                    onCheckedChange = onWifiOnlyChanged
                )
            }
        )
    }
}

@Composable
private fun BackupScopeCard(
    scope: BackupScope,
    onScopeChanged: (BackupScope) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        ScopeToggle("Profile", "Display name, bio, avatar", Icons.Default.Person, MaterialTheme.colorScheme.primary, scope.includeProfile) {
            onScopeChanged(scope.copy(includeProfile = it))
        }
        ScopeDivider()
        ScopeToggle("Messages", "Conversations and direct messages", Icons.Default.Chat, Color(0xFF42A5F5), scope.includeMessages) {
            onScopeChanged(scope.copy(includeMessages = it))
        }
        ScopeDivider()
        ScopeToggle("Posts & Comments", "Your posts, comments, and likes", Icons.Default.Article, Color(0xFFFF6E40), scope.includePosts) {
            onScopeChanged(scope.copy(includePosts = it))
        }
        ScopeDivider()
        ScopeToggle("Bookmarks", "Saved posts", Icons.Default.Bookmark, Color(0xFFFFD700), scope.includeBookmarks) {
            onScopeChanged(scope.copy(includeBookmarks = it))
        }
        ScopeDivider()
        ScopeToggle("Follows", "Following and followers list", Icons.Default.People, MaterialTheme.colorScheme.tertiary, scope.includeFollows) {
            onScopeChanged(scope.copy(includeFollows = it))
        }
        ScopeDivider()
        ScopeToggle("App Settings", "Theme, accessibility, preferences", Icons.Default.Settings, Color(0xFF78909C), scope.includeSettings) {
            onScopeChanged(scope.copy(includeSettings = it))
        }
        ScopeDivider()
        ScopeToggle("Notifications", "Notification history", Icons.Default.Notifications, Color(0xFFFF9800), scope.includeNotifications) {
            onScopeChanged(scope.copy(includeNotifications = it))
        }
    }
}

@Composable
private fun ScopeToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = iconTint) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
private fun ScopeDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
}

@Composable
private fun EncryptionCard(
    isEncrypted: Boolean,
    onChanged: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        ListItem(
            headlineContent = { Text("End-to-End Encryption") },
            supportingContent = {
                Text(
                    if (isEncrypted) "Backups are encrypted with your passphrase"
                    else "Protect backups with a passphrase"
                )
            },
            leadingContent = {
                Icon(
                    Icons.Default.EnhancedEncryption,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(checked = isEncrypted, onCheckedChange = onChanged)
            }
        )
        if (isEncrypted) {
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp, end = 16.dp))
            Row(
                modifier = Modifier.padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "If you forget your passphrase, you won't be able to restore encrypted backups.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyBackupsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No backups found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Create a backup to see it here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BackupListItem(
    metadata: BackupMetadata,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    metadata.formattedDate,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(metadata.formattedSize)
                    Spacer(Modifier.width(6.dp))
                    val locationLabel = when (metadata.storageLocation) {
                        "exported" -> "Exported"
                        "imported" -> "Imported"
                        else -> "Local"
                    }
                    val locationIcon = when (metadata.storageLocation) {
                        "exported" -> Icons.Default.Upload
                        "imported" -> Icons.Default.Download
                        else -> Icons.Default.PhoneAndroid
                    }
                    Icon(locationIcon, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(2.dp))
                    Text(locationLabel, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (metadata.isEncrypted) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(2.dp))
                        Text("Encrypted", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            leadingContent = {
                Icon(
                    when (metadata.storageLocation) {
                        "exported" -> Icons.Default.CloudDone
                        "imported" -> Icons.Default.CloudDownload
                        else -> Icons.Default.PhoneAndroid
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            trailingContent = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Restore") },
                            onClick = { showMenu = false; onRestore() },
                            leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export to File") },
                            onClick = { showMenu = false; onExport() },
                            leadingIcon = { Icon(Icons.Default.SaveAlt, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { showMenu = false; onShare() },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            },
            modifier = Modifier.clickable(onClick = onRestore)
        )
    }
}

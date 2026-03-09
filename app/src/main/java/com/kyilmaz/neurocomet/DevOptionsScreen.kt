package com.kyilmaz.neurocomet

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Data model for a collapsible section group ─────────────

private data class DevSectionGroup(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val searchTerms: String,
    val content: @Composable () -> Unit
)

// ─── Collapsible group composable ───────────────────────────

@Composable
private fun CollapsibleGroup(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    forceExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val isExpanded = expanded || forceExpanded

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

// ─── Main Screen ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsScreen(
    onBack: () -> Unit,
    devOptionsViewModel: DevOptionsViewModel,
    safetyViewModel: SafetyViewModel,
    feedViewModel: FeedViewModel? = null,
    authViewModel: AuthViewModel? = null,
    themeViewModel: ThemeViewModel? = null,
    onNavigateToGame: (String) -> Unit = {},
    onNavigateToBackup: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { devOptionsViewModel.init(application) }

    val sectionGroups = remember(devOptionsViewModel, safetyViewModel, feedViewModel, authViewModel, themeViewModel) {
        listOf(
            DevSectionGroup("app_info", "App Info & Diagnostics", Icons.Filled.Info, "version build device os memory") { AppInfoDevSection() },
            DevSectionGroup("live_session_lab", "Live Session Lab", Icons.Filled.Timer, "live activity live session regulation recharge focus sprint stim braille notification") { RegulationLiveSessionLabSection() },
            DevSectionGroup("environment", "Environment", Icons.Filled.Cloud, "staging production local backend") { EnvironmentPickerDevSection(devOptionsViewModel) },
            DevSectionGroup("feature_flags", "Feature Flags", Icons.Filled.Flag, "flags feed video chat story search ai") { FeatureFlagsDevSection(devOptionsViewModel) },
            DevSectionGroup("ab_testing", "A/B Testing", Icons.Filled.Science, "ab test experiment variant split traffic control") { ABTestingDevSection() },
            DevSectionGroup("ads", "Google Ads", Icons.Filled.Tv, "ads banner interstitial rewarded premium") { GoogleAdsDevTestSection() },
            DevSectionGroup("auth", "Authentication", Icons.Filled.Lock, "auth login sign in session user 2fa") { AuthenticationTestingSection(authViewModel) },
            DevSectionGroup("biometric", "Biometric & MFA", Icons.Filled.Fingerprint, "biometric fido passkey totp backup codes mfa") { BiometricFidoDevSection(authViewModel) },
            DevSectionGroup("safety", "Content Safety", Icons.Filled.Security, "content safety audience kids filter pin parental") { ContentSafetyDevSection(devOptionsViewModel, safetyViewModel) },
            DevSectionGroup("dm_debug", "DM Delivery", Icons.Filled.Chat, "dm message overlay send failure delay rate limit moderation") { DmDebugDevSection(devOptionsViewModel) },
            DevSectionGroup("rendering", "Rendering & Network", Icons.Filled.NetworkCheck, "rendering offline loading error latency fallback network") { RenderingNetworkDevSection(devOptionsViewModel) },
            DevSectionGroup("widgets", "Widgets", Icons.Filled.Widgets, "widgets neurodivergent accessible") { NeurodivergentWidgetsDevSection() },
            DevSectionGroup("images", "Images", Icons.Filled.Image, "images customization filters") { ImageCustomizationDevSection() },
            DevSectionGroup("explore", "Explore Views", Icons.Filled.Explore, "explore discover trending") { ExploreViewsDevSection() },
            DevSectionGroup("multimedia", "Multi-Media", Icons.Filled.PlayCircle, "multimedia video audio posts") { MultiMediaPostDevSection() },
            DevSectionGroup("nav", "Navigation", Icons.Filled.Navigation, "navigation adaptive drawer") { AdaptiveNavigationDevSection() },
            DevSectionGroup("dialogs", "Dialogs", Icons.Filled.ChatBubble, "dialogs popup message input choice") { NeurodivergentDialogsDevSection() },
            DevSectionGroup("location", "Location & Sensors", Icons.Filled.Sensors, "location gps sensors pressure") { EnhancedLocationSensorsDevSection() },
            DevSectionGroup("contact_picker", "Contact Picker", Icons.Filled.Contacts, "contact picker android 17 cinnamonbun cinnamon bun session privacy contacts api 36.1") { ContactPickerDevSection() },
            DevSectionGroup("storage", "Local Storage", Icons.Filled.Storage, "storage preferences credentials") { LocalStorageDevSection() },
            DevSectionGroup("supabase", "Supabase", Icons.Filled.CloudUpload, "supabase database posts") { SupabaseTestDataSection() },
            DevSectionGroup("games", "Games", Icons.Filled.SportsEsports, "games achievements") { GamesTestingSection(onNavigateToGame) },
            DevSectionGroup("language", "Language", Icons.Filled.Language, "language locale translation") { LanguageTestingSection(themeViewModel) },
            DevSectionGroup("backup", "Backup & Restore", Icons.Filled.CloudUpload, "backup restore export import data") { BackupDevTestSection(onNavigateToBackup) },
            DevSectionGroup("stress", "Stress Testing", Icons.Filled.Speed, "stress test performance diagnostic") { StressTestingSection(feedViewModel, context) }
        )
    }

    val filteredGroups = if (searchQuery.isBlank()) sectionGroups
    else {
        val q = searchQuery.lowercase()
        sectionGroups.filter { group ->
            group.title.lowercase().contains(q) || group.searchTerms.contains(q)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Options", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item(key = "header") { DevHeader() }

            item(key = "search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search dev options…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item(key = "quick_actions") { QuickActionsBar(devOptionsViewModel, safetyViewModel, context, feedViewModel) }
            item(key = "active_overrides") { ActiveOverridesIndicator(devOptionsViewModel) }

            filteredGroups.forEach { group ->
                item(key = group.key) {
                    CollapsibleGroup(
                        title = group.title,
                        icon = group.icon,
                        forceExpanded = searchQuery.isNotBlank()
                    ) {
                        group.content()
                    }
                }
            }

            item(key = "footer") { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// ─── Quick Actions Bar ──────────────────────────────────────

@Composable
private fun QuickActionsBar(
    devOptionsViewModel: DevOptionsViewModel,
    safetyViewModel: SafetyViewModel,
    context: android.content.Context,
    feedViewModel: FeedViewModel? = null
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {
                devOptionsViewModel.resetAll()
                safetyViewModel.refresh(context.applicationContext as Application)
                Toast.makeText(context, "All settings reset", Toast.LENGTH_SHORT).show()
            },
            label = { Text("Reset Overrides", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Filled.RestartAlt, null, Modifier.size(16.dp)) }
        )
        if (feedViewModel != null) {
            AssistChip(
                onClick = {
                    devOptionsViewModel.resetMockData(feedViewModel)
                    Toast.makeText(context, "Mock data reset", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Reset Mock Data", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Filled.DataUsage, null, Modifier.size(16.dp)) }
            )
        }
        AssistChip(
            onClick = {
                val opt = devOptionsViewModel.options.value
                devOptionsViewModel.setShowDmDebugOverlay(!opt.showDmDebugOverlay)
                Toast.makeText(context, "Debug overlay ${if (!opt.showDmDebugOverlay) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
            },
            label = { Text("Toggle Overlay", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Filled.Layers, null, Modifier.size(16.dp)) }
        )
        AssistChip(
            onClick = {
                devOptionsViewModel.setVerboseLogging(!devOptionsViewModel.options.value.verboseLogging)
                Toast.makeText(context, "Verbose logging toggled", Toast.LENGTH_SHORT).show()
            },
            label = { Text("Verbose Log", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Filled.Terminal, null, Modifier.size(16.dp)) }
        )
        AssistChip(
            onClick = {
                devOptionsViewModel.setSimulateOffline(!devOptionsViewModel.options.value.simulateOffline)
                Toast.makeText(context, "Offline mode toggled", Toast.LENGTH_SHORT).show()
            },
            label = { Text("Toggle Offline", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Filled.WifiOff, null, Modifier.size(16.dp)) }
        )
    }
}

// ─── Active overrides indicator ─────────────────────────────

@Composable
private fun ActiveOverridesIndicator(devOptionsViewModel: DevOptionsViewModel) {
    val options by devOptionsViewModel.options.collectAsState()
    val defaults = DevOptions()

    val activeCount = listOf(
        options.verboseLogging != defaults.verboseLogging,
        options.environment != defaults.environment,
        options.showDmDebugOverlay != defaults.showDmDebugOverlay,
        options.dmForceSendFailure != defaults.dmForceSendFailure,
        options.dmArtificialSendDelayMs != defaults.dmArtificialSendDelayMs,
        options.moderationOverride != defaults.moderationOverride,
        options.forceAudience != defaults.forceAudience,
        options.forceKidsFilterLevel != defaults.forceKidsFilterLevel,
        options.forcePinSet != defaults.forcePinSet,
        options.forcePinVerifySuccess != defaults.forcePinVerifySuccess,
        options.simulateOffline != defaults.simulateOffline,
        options.simulateLoadingError != defaults.simulateLoadingError,
        options.infiniteLoading != defaults.infiniteLoading,
        options.showFallbackUi != defaults.showFallbackUi,
        options.networkLatencyMs != defaults.networkLatencyMs,
        options.forceLoggedOut != defaults.forceLoggedOut,
        options.bypassBiometric != defaults.bypassBiometric,
        options.force2FA != defaults.force2FA,
        options.enableNewFeedLayout != defaults.enableNewFeedLayout,
        options.enableVideoChat != defaults.enableVideoChat,
        options.enableStoryReactions != defaults.enableStoryReactions,
        options.enableAdvancedSearch != defaults.enableAdvancedSearch,
        options.enableAiSuggestions != defaults.enableAiSuggestions,
        options.showPerformanceOverlay != defaults.showPerformanceOverlay
    ).count { it }

    if (activeCount > 0) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "$activeCount override${if (activeCount != 1) "s" else ""} active",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// ─── Header ─────────────────────────────────────────────────

@Composable
private fun DevHeader() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Developer Mode Active", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text("Internal tools only. Changes persist across restarts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RegulationLiveSessionLabSection() {
    val context = LocalContext.current
    LaunchedEffect(context) {
        RegulationLiveSessionManager.initialize(context)
    }
    val session by RegulationLiveSessionManager.session.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "NeuroComet’s Live Activities-style surface is a Regulation Session: an ongoing Android notification plus an in-app live banner tuned to spoons, calming states, and sensory breaks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { RegulationLiveSessionManager.startPreset(context, RegulationSessionPreset.RECHARGE_WINDOW) },
                modifier = Modifier.weight(1f)
            ) { Text("Recharge") }
            Button(
                onClick = { RegulationLiveSessionManager.startPreset(context, RegulationSessionPreset.FOCUS_SPRINT) },
                modifier = Modifier.weight(1f)
            ) { Text("Focus") }
            Button(
                onClick = { RegulationLiveSessionManager.startPreset(context, RegulationSessionPreset.STIM_BREAK) },
                modifier = Modifier.weight(1f)
            ) { Text("Stim") }
        }

        if (session != null) {
            val activeSession = session!!
            val remaining by rememberDevLiveSessionRemaining(activeSession)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(activeSession.preset.title, fontWeight = FontWeight.Bold)
                    Text(
                        "${activeSession.preset.subtitle}\n${activeSession.preset.neuroState.getDisplayName(context)} • ${formatDuration(remaining)} remaining • ${activeSession.spoonsRemaining}/${activeSession.spoonsTotal} spoons${if (activeSession.brailleOptimized) " • braille mode" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { RegulationLiveSessionManager.togglePause(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (activeSession.isPaused) "Resume" else "Pause")
                        }
                        OutlinedButton(
                            onClick = { RegulationLiveSessionManager.adjustMinutes(context, 2) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+2 min")
                        }
                        OutlinedButton(
                            onClick = { RegulationLiveSessionManager.adjustMinutes(context, -2) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-2 min")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { RegulationLiveSessionManager.spendSpoon(context) },
                            label = { Text("Spend spoon") }
                        )
                        AssistChip(
                            onClick = { RegulationLiveSessionManager.restoreSpoon(context) },
                            label = { Text("Restore spoon") }
                        )
                        AssistChip(
                            onClick = { RegulationLiveSessionManager.refreshAccessibilityMode(context) },
                            label = { Text("Refresh braille mode") }
                        )
                    }

                    TextButton(onClick = { RegulationLiveSessionManager.end(context) }) {
                        Text("End live session")
                    }
                }
            }
        } else {
            Text(
                "No regulation session is active. Start one above to test the ongoing notification and the in-app live banner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun rememberDevLiveSessionRemaining(session: RegulationLiveSession): State<Long> = produceState(
    initialValue = RegulationLiveSessionManager.remainingMillis(session),
    key1 = session
) {
    while (true) {
        value = RegulationLiveSessionManager.remainingMillis(session)
        if (session.isPaused || value <= 0L) break
        kotlinx.coroutines.delay(1_000L)
    }
}

package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyilmaz.neurocomet.ads.GoogleAdsManager
import com.kyilmaz.neurocomet.auth.AuthResult
import io.github.jan.supabase.auth.auth
import com.kyilmaz.neurocomet.utils.StressTester
import com.kyilmaz.neurocomet.utils.StressTestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GoogleAdsDevTestSection() {
    val adsState by GoogleAdsManager.adsState.collectAsState()
    DevSectionCard(title = "Google Ads Testing", icon = Icons.Filled.Tv) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (adsState.isPremium) Color(0xFF1B5E20).copy(alpha = 0.2f)
                else if (adsState.adsEnabled) Color(0xFFFFF3E0)
                else Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = if (adsState.isPremium) "🌟 Premium User" else if (adsState.adsEnabled) "📺 Ads Enabled" else "🚫 Ads Disabled", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Total ads shown: ${adsState.totalAdsShown}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (adsState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdStatusChip("Banner", adsState.bannerLoaded)
            AdStatusChip("Interstitial", adsState.interstitialLoaded)
            AdStatusChip("Rewarded", adsState.rewardedLoaded)
        }
        Spacer(Modifier.height(12.dp))
        DevToggleRowSimple(
            title = "Simulate Premium",
            subtitle = "Pretend user has sub",
            isChecked = adsState.isPremium,
            onCheckedChange = { 
                GoogleAdsManager.devSetSimulatePremium(it)
                if (it) SubscriptionManager.simulateTestSuccess() else SubscriptionManager.resetTestPurchase()
            }
        )
        DevToggleRowSimple(title = "Force Show Ads", subtitle = "Ignore premium status", isChecked = adsState.forceShowAds, onCheckedChange = { GoogleAdsManager.devSetForceShowAds(it) })
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { GoogleAdsManager.devForceLoadAllAds() }, modifier = Modifier.weight(1f)) { Text("Load All", fontSize = 12.sp) }
            Button(onClick = { GoogleAdsManager.devResetSessionCounters() }, modifier = Modifier.weight(1f)) { Text("Reset", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun AdStatusChip(label: String, isLoaded: Boolean) {
    Surface(color = if (isLoaded) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFF9E9E9E).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (isLoaded) Color(0xFF4CAF50) else Color(0xFF9E9E9E), CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DevSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DevToggleRowSimple(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun NeurodivergentWidgetsDevSection() {
    var showWidgetPreview by remember { mutableStateOf(false) }
    var previewHighContrast by remember { mutableStateOf(false) }
    var previewReducedMotion by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Neurodivergent Widgets",
        icon = Icons.Filled.Widgets
    ) {
        Text(
            "Test the accessibility-focused widgets for neurodivergent users.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        DevToggleRowSimple(
            title = "High Contrast Mode",
            subtitle = "Preview widgets in high contrast",
            isChecked = previewHighContrast,
            onCheckedChange = { previewHighContrast = it }
        )

        DevToggleRowSimple(
            title = "Reduced Motion",
            subtitle = "Disable animations in preview",
            isChecked = previewReducedMotion,
            onCheckedChange = { previewReducedMotion = it }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { showWidgetPreview = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Preview, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Preview Widget Dashboard")
        }
    }

    if (showWidgetPreview) {
        Dialog(
            onDismissRequest = { showWidgetPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Widget Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showWidgetPreview = false }) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }
                    NeurodivergentWidgetDashboard(
                        highContrast = previewHighContrast,
                        reducedMotion = previewReducedMotion
                    )
                }
            }
        }
    }
}

@Composable
fun ImageCustomizationDevSection() {
    var showEditor by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            showEditor = true
        }
    }

    DevSectionCard(
        title = "Image Customization",
        icon = Icons.Filled.PhotoFilter
    ) {
        Text(
            "Test image filters, stickers, and drawing tools.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Select", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    selectedImageUri = null
                    showEditor = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Draw, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Draw", fontSize = 12.sp)
            }
        }
    }

    if (showEditor) {
        ImageCustomizationEditor(
            imageUri = selectedImageUri,
            onSave = { _ ->
                showEditor = false
                Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditor = false },
            title = if (selectedImageUri != null) "Edit Image" else "Blank Canvas"
        )
    }
}

@Composable
fun ExploreViewsDevSection() {
    var currentViewType by remember { mutableStateOf(ExploreViewType.STANDARD) }
    var showPreview by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Explore Page Views",
        icon = Icons.Filled.Explore
    ) {
        Text(
            "Test different view layouts for the Explore page.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layout:", style = MaterialTheme.typography.labelMedium)
            ExploreViewType.entries.forEach { viewType ->
                FilterChip(
                    selected = viewType == currentViewType,
                    onClick = { currentViewType = viewType },
                    label = { Text(viewType.label, fontSize = 10.sp, maxLines = 1, softWrap = false) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { showPreview = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Preview, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Preview ${currentViewType.label}")
        }
    }

    if (showPreview) {
        Dialog(onDismissRequest = { showPreview = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val mockPosts = remember { generateMockExplorePostsWithMedia() }
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${currentViewType.label} Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showPreview = false }) { Icon(Icons.Filled.Close, "Close") }
                    }
                    when (currentViewType) {
                        ExploreViewType.GRID -> ExploreGridView(posts = mockPosts, onPostClick = { _ -> }, onProfileClick = { _ -> })
                        ExploreViewType.COMPACT -> ExploreCompactView(posts = mockPosts, onPostClick = { _ -> }, onLikePost = { _ -> }, onProfileClick = { _ -> })
                        ExploreViewType.STANDARD -> ExploreStandardView(posts = mockPosts, onPostClick = { _ -> }, onLikePost = { _ -> }, onSharePost = { _, _ -> }, onCommentPost = { _ -> }, onProfileClick = { _ -> })
                        ExploreViewType.LARGE_CARDS -> ExploreLargeCardView(posts = mockPosts, onPostClick = { _ -> }, onLikePost = { _ -> }, onSharePost = { _, _ -> }, onCommentPost = { _ -> }, onProfileClick = { _ -> })
                    }
                }
            }
        }
    }
}

@Composable
fun MultiMediaPostDevSection() {
    val mockPosts = remember { generateMockExplorePostsWithMedia() }

    DevSectionCard(
        title = "Multi-Media Posts",
        icon = Icons.Filled.Collections
    ) {
        Text(
            "Test posts with up to ${Post.MAX_MEDIA_ITEMS} items.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            mockPosts.take(3).forEach { post ->
                val mediaCount = post.mediaCount()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = post.userId ?: "User", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(text = "📷 $mediaCount items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun AdaptiveNavigationDevSection() {
    val navigationType = calculateNavigationType()
    val contentType = calculateContentType()

    DevSectionCard(
        title = "Adaptive Navigation",
        icon = Icons.Filled.Dashboard
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Navigation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(navigationType.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Content", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(contentType.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Navigation adapts based on screen width:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            val entries = listOf(
                "BOTTOM_NAV" to "< 600dp (phones)",
                "NAV_RAIL" to "600–840dp (small tablets)",
                "DRAWER" to "> 840dp (large tablets/desktop)"
            )
            entries.forEach { (type, desc) ->
                val isActive = navigationType.name == type
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isActive) "▶ " else "  ", fontSize = 10.sp, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                    Text(
                        "$type — $desc",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeurodivergentDialogsDevSection() {
    var showMessageDialog by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showChoiceDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var selectedDialogType by remember { mutableStateOf(DialogType.INFO) }

    DevSectionCard(
        title = "Neurodivergent Dialogs",
        icon = Icons.Filled.ChatBubble
    ) {
        Text("Test accessible pop-up messages and input dialogs.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DialogType.entries.take(5).forEach { type ->
                FilterChip(selected = type == selectedDialogType, onClick = { selectedDialogType = type }, label = { Text(type.name, fontSize = 10.sp) })
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showMessageDialog = true }, modifier = Modifier.weight(1f)) { Text("Message", fontSize = 11.sp) }
            Button(onClick = { showInputDialog = true }, modifier = Modifier.weight(1f)) { Text("Input", fontSize = 11.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showChoiceDialog = true }, modifier = Modifier.weight(1f)) { Text("Choice", fontSize = 11.sp) }
            Button(onClick = { showLoadingDialog = true }, modifier = Modifier.weight(1f)) { Text("Loading", fontSize = 11.sp) }
        }
    }

    if (showMessageDialog) {
        NeurodivergentMessageDialog(
            config = DialogConfig(
                type = selectedDialogType,
                title = "Test ${selectedDialogType.name}",
                message = "This is a neurodivergent-friendly dialog.",
                primaryButtonText = "Got it!"
            ),
            onDismiss = { showMessageDialog = false }
        )
    }
    if (showInputDialog) {
        NeurodivergentInputDialog(
            title = "Name?", message = "Enter name", placeholder = "Name...",
            onDismiss = { showInputDialog = false },
            onConfirm = { _ -> showInputDialog = false }
        )
    }
    if (showChoiceDialog) {
        NeurodivergentChoiceDialog(
            title = "Mood", message = "How are you?",
            choices = listOf(DialogChoice("happy", "Happy", emoji = "😊"), DialogChoice("calm", "Calm", emoji = "😌")),
            onDismiss = { showChoiceDialog = false },
            onSelect = { _ -> showChoiceDialog = false }
        )
    }
    if (showLoadingDialog) {
        LaunchedEffect(Unit) { delay(2000); showLoadingDialog = false }
        NeurodivergentLoadingDialog(message = "Processing...", onDismiss = { showLoadingDialog = false })
    }
}

@Composable
fun EnhancedLocationSensorsDevSection() {
    val context = LocalContext.current
    val locationStatus by LocationService.locationStatus.collectAsState()
    val currentLocation by LocationService.currentLocation.collectAsState()
    val sensorData by LocationService.sensorData.collectAsState()
    var isMonitoring by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    DevSectionCard(
        title = "Location & Sensors",
        icon = Icons.Filled.Sensors
    ) {
        Text("Status: ${locationStatus.name}", style = MaterialTheme.typography.labelMedium)
        currentLocation?.let { Text("📍 ${it.latitude}, ${it.longitude}", style = MaterialTheme.typography.bodySmall) }
        if (isMonitoring) {
            Text("Moving: ${sensorData.isMoving} | Pressure: ${sensorData.pressure}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    isMonitoring = !isMonitoring
                    if (isMonitoring) {
                        LocationService.initialize(context)
                        LocationService.startSensorMonitoring(context)
                        LocationService.startLocationUpdates(context, LocationPriority.HIGH_ACCURACY, 5000L) {}
                    } else {
                        LocationService.stopLocationUpdates()
                        LocationService.stopSensorMonitoring()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (isMonitoring) "Stop" else "Start") }

            Button(
                onClick = {
                    scope.launch {
                        val loc = LocationService.getCurrentLocation(context, LocationPriority.HIGH_ACCURACY)
                        testResult = if (loc != null) "✅ Found" else "❌ Failed"
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Get GPS") }
        }
        testResult?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

/**
 * Cloud Sync & Supabase dev section.
 *
 * Surfaces the tables and columns that were recently added to Supabase:
 *   • user_preferences  – theme / accessibility settings sync
 *   • user_game_achievements – cross-device game achievement sync
 *   • ab_test_assignments    – stable experiment variants per user
 *   • users.is_premium / premium_until columns (Premium status)
 *
 * Lets developers pretend-toggle premium, force-push the local preferences /
 * achievements payloads, and read back the rows from Supabase.
 */
/**
 * Renders a Supabase auth-status card with an inline dev sign-in form when
 * the user is not authenticated. Returns the live-tracked UID (null when
 * signed out). Shared between [CloudSyncDevSection] and [SupabaseTestDataSection]
 * so every table-seeding button in Developer Options can gate on a real JWT.
 */
@Composable
private fun SupabaseAuthGate(authViewModel: AuthViewModel): String? {
    var currentUid by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        while (true) {
            currentUid = try {
                AppSupabaseClient.client?.auth?.currentUserOrNull()?.id
            } catch (_: Throwable) { null }
            kotlinx.coroutines.delay(1500)
        }
    }
    val devAuthStatus by authViewModel.devAuthStatus.collectAsState()
    var devEmail by remember { mutableStateOf("dev+neurocomet@example.com") }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (currentUid != null)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (currentUid != null) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    null,
                    Modifier.size(18.dp),
                    tint = if (currentUid != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = currentUid?.let { "Signed in: ${it.take(8)}\u2026" }
                        ?: "Not signed in \u2014 sign in to write",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (currentUid == null) {
                Text(
                    "These tables enforce RLS (auth.uid() = user_id); a real Supabase JWT is required for inserts.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = devEmail,
                    onValueChange = { devEmail = it },
                    label = { Text("Dev email", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { authViewModel.devSignInForTesting(devEmail) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Login, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Dev sign in / sign up")
                }
            }
            devAuthStatus?.let {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    return currentUid
}

@Composable
fun CloudSyncDevSection(@Suppress("UNUSED_PARAMETER") devOptionsViewModel: DevOptionsViewModel, authViewModel: AuthViewModel) {
    val scope = rememberCoroutineScope()
    val isAvailable = remember { SupabaseTestData.isSupabaseAvailable() }

    var prefsCount by remember { mutableStateOf<Int?>(null) }
    var achievementsCount by remember { mutableStateOf<Int?>(null) }
    var abCount by remember { mutableStateOf<Int?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    suspend fun refreshCounts() {
        if (!isAvailable) return
        SupabaseTestData.getTableRowCount("user_preferences").onSuccess { prefsCount = it }
        SupabaseTestData.getTableRowCount("user_game_achievements").onSuccess { achievementsCount = it }
        SupabaseTestData.getTableRowCount("ab_test_assignments").onSuccess { abCount = it }
    }

    fun run(label: String, action: suspend () -> Result<String>) {
        if (isBusy) return
        isBusy = true
        status = "\u23F3 $label\u2026"
        scope.launch {
            try {
                action().fold(
                    onSuccess = { status = it; refreshCounts() },
                    onFailure = { status = "Error: ${it.message}" }
                )
            } catch (e: Throwable) {
                status = "Crash: ${e.javaClass.simpleName}: ${e.message}"
            } finally {
                isBusy = false
            }
        }
    }

    DevSectionCard(title = "Cloud Sync & Supabase", icon = Icons.Filled.CloudSync) {
        if (!isAvailable) {
            Text("\u26A0\uFE0F Supabase not configured", color = MaterialTheme.colorScheme.error)
            return@DevSectionCard
        }

        val currentUid = SupabaseAuthGate(authViewModel)
        LaunchedEffect(currentUid) { refreshCounts() }
        Spacer(Modifier.height(12.dp))

        Text(
            "Tables tied to the freshly-added migrate_feedback_and_extras.sql: " +
                "preferences, achievements, and A/B assignments. Use these to " +
                "verify cross-device sync and premium gating.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // Row counts
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                listOf(
                    "user_preferences" to prefsCount,
                    "user_game_achievements" to achievementsCount,
                    "ab_test_assignments" to abCount
                ).forEach { (name, count) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        Text(
                            count?.toString() ?: "\u2014",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (count != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Push sample rows", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        val canPush = !isBusy && currentUid != null
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { run("Sync preferences") { SupabaseTestData.sendTestPreference() } },
                enabled = canPush,
                modifier = Modifier.weight(1f)
            ) { Text("Prefs", fontSize = 11.sp) }
            Button(
                onClick = { run("Sync achievements") { SupabaseTestData.sendTestGameAchievement() } },
                enabled = canPush,
                modifier = Modifier.weight(1f)
            ) { Text("Achievements", fontSize = 11.sp) }
            Button(
                onClick = { run("Assign A/B") { SupabaseTestData.sendTestAbAssignment() } },
                enabled = canPush,
                modifier = Modifier.weight(1f)
            ) { Text("A/B", fontSize = 11.sp) }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { scope.launch { refreshCounts() } },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Refresh row counts")
        }

        if (isBusy) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        status?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.startsWith("Error") || it.startsWith("Crash"))
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SupabaseDbTestDevSection() {
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<String>?>(null) }
    val isAvailable = remember { SupabaseTestData.isSupabaseAvailable() }

    // Table row counts — mirrors SupabaseTestData.REQUIRED_TABLES so new tables
    // (stories, feedback, user_game_achievements, practice_call_logs,
    // user_preferences, ab_test_assignments) are monitored automatically.
    val tables = remember { SupabaseTestData.REQUIRED_TABLES }
    var tableCounts by remember { mutableStateOf<Map<String, Int?>>(emptyMap()) }

    DevSectionCard(title = "Supabase DB Testing", icon = Icons.Filled.CloudSync) {
        if (!isAvailable) {
            Text("⚠️ Supabase not configured", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            return@DevSectionCard
        }

        // Run all table tests
        Button(
            onClick = {
                isRunning = true
                results = null
                scope.launch {
                    try {
                        val res = SupabaseTestData.testAllTables()
                        res.onSuccess { results = it }
                        res.onFailure { results = listOf("❌ ${it.message}") }
                    } catch (e: Throwable) {
                        results = listOf("❌ Crash: ${e.javaClass.simpleName}: ${e.message}")
                    } finally {
                        isRunning = false
                    }
                }
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRunning) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            else Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Test All Tables")
        }

        results?.let { list ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    list.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = when {
                                line.startsWith("✅") -> Color(0xFF2E7D32)
                                line.startsWith("❌") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Row counts
        Text("Table Row Counts", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))

        var countLoading by remember { mutableStateOf(false) }
        Button(
            onClick = {
                countLoading = true
                scope.launch {
                    val counts = mutableMapOf<String, Int?>()
                    for (table in tables) {
                        try {
                            SupabaseTestData.getTableRowCount(table)
                                .onSuccess { counts[table] = it }
                                .onFailure { counts[table] = null }
                        } catch (_: Throwable) {
                            counts[table] = null
                        }
                    }
                    tableCounts = counts
                    countLoading = false
                }
            },
            enabled = !countLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (countLoading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            else Icon(Icons.Filled.Storage, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Fetch Row Counts")
        }

        if (tableCounts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    tableCounts.forEach { (table, count) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(table, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                            Text(
                                text = count?.toString() ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (count != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupabaseTestDataSection(authViewModel: AuthViewModel) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var postsCount by remember { mutableStateOf<Int?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val isAvailable = remember { SupabaseTestData.isSupabaseAvailable() }

    LaunchedEffect(Unit) {
        try {
            if (isAvailable) SupabaseTestData.getTableRowCount("posts").onSuccess { postsCount = it }
        } catch (e: Throwable) {
            Log.e("DevTestSections", "Failed to get table row count: ${e.javaClass.simpleName}", e)
        }
    }

    // Helper that runs a suspend block and updates loading/status state.
    fun runAction(label: String, block: suspend () -> Result<String>) {
        isLoading = true
        statusMessage = "\u23F3 $label\u2026"
        scope.launch {
            try {
                val result = block()
                result.onSuccess { msg ->
                    statusMessage = msg
                    // Refresh posts count if user inserted into posts.
                    if (label.contains("post", ignoreCase = true)) {
                        SupabaseTestData.getTableRowCount("posts").onSuccess { postsCount = it }
                    }
                }
                result.onFailure { err ->
                    statusMessage = "Error: ${err.message}"
                    Log.e("DevTestSections", "$label failed", err)
                }
            } catch (e: Throwable) {
                statusMessage = "Crash: ${e.javaClass.simpleName}: ${e.message}"
                Log.e("DevTestSections", "Uncaught error in $label", e)
            } finally {
                isLoading = false
            }
        }
    }

    DevSectionCard(title = "Supabase Data", icon = Icons.Filled.CloudUpload) {
        if (!isAvailable) {
            Text("Not Configured", color = MaterialTheme.colorScheme.error)
            return@DevSectionCard
        }

        val currentUid = SupabaseAuthGate(authViewModel)
        Spacer(Modifier.height(12.dp))

        Text("Posts: ${postsCount ?: "\u2014"}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Seed the newly-added Supabase tables with test rows. Posts now include " +
                "media_items / min_audience / background_color columns, and stories is a brand-new table.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        val signedIn = currentUid != null
        // Posts/stories use anon-insertable policies (no FK to auth.users required),
        // so they work without sign-in. Feedback is also nullable-user_id.
        // Practice-call/achievements/prefs/ab REQUIRE a real JWT.
        // Posts & stories (content tables)
        Text("Content", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { runAction("Send test post") { SupabaseTestData.sendTestPost() } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.action_post), fontSize = 12.sp) }
            Button(
                onClick = { runAction("Bulk posts") { SupabaseTestData.sendBulkTestPosts(5) } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) { Text("x5 Posts", fontSize = 12.sp) }
            Button(
                onClick = { runAction("Send test story") { SupabaseTestData.sendTestStory() } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.label_story), fontSize = 12.sp) }
        }

        Spacer(Modifier.height(8.dp))

        // Feedback + practice calls
        Text("Feedback & AI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { runAction("Send feedback") { SupabaseTestData.sendTestFeedback() } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) { Text("Feedback", fontSize = 12.sp) }
            Button(
                onClick = { runAction("Practice call log") { SupabaseTestData.sendTestPracticeCall() } },
                enabled = !isLoading && signedIn,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.call_practice_title), fontSize = 12.sp) }
        }

        Spacer(Modifier.height(8.dp))

        // Games / preferences / A-B
        Text("User State", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { runAction("Game achievement") { SupabaseTestData.sendTestGameAchievement() } },
                enabled = !isLoading && signedIn,
                modifier = Modifier.weight(1f)
            ) { Text("Achievements", fontSize = 11.sp) }
            Button(
                onClick = { runAction("Preferences") { SupabaseTestData.sendTestPreference() } },
                enabled = !isLoading && signedIn,
                modifier = Modifier.weight(1f)
            ) { Text("Prefs", fontSize = 11.sp) }
            Button(
                onClick = { runAction("A/B assignment") { SupabaseTestData.sendTestAbAssignment() } },
                enabled = !isLoading && signedIn,
                modifier = Modifier.weight(1f)
            ) { Text("A/B", fontSize = 11.sp) }
        }

        if (isLoading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.startsWith("Error") || it.startsWith("Crash"))
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun GamesTestingSection(onNavigateToGame: (String) -> Unit) {
    val context = LocalContext.current
    var achievements by remember { mutableIntStateOf(com.kyilmaz.neurocomet.games.GameUnlockManager.getAchievementCount(context)) }

    DevSectionCard(title = "Games", icon = Icons.Filled.SportsEsports) {
        Text("Achievements: $achievements", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { com.kyilmaz.neurocomet.games.GameUnlockManager.addAchievement(context, 1); achievements++ }) { Text("+1") }
            Button(onClick = { com.kyilmaz.neurocomet.games.GameUnlockManager.setAchievementCount(context, 0); achievements = 0 }) { Text("Reset") }
        }
        Spacer(Modifier.height(12.dp))
        com.kyilmaz.neurocomet.games.ALL_GAMES.take(3).forEach { game ->
            TextButton(onClick = { onNavigateToGame(game.id) }) { Text("Launch ${stringResource(game.nameRes)}") }
        }
    }
}

@Composable
fun AuthenticationTestingSection(authViewModel: AuthViewModel?, devOptionsViewModel: DevOptionsViewModel? = null) {
    val user by authViewModel?.user?.collectAsState() ?: remember { mutableStateOf(null) }
    val is2FARequired by authViewModel?.is2FARequired?.collectAsState() ?: remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var authResult by remember { mutableStateOf<String?>(null) }

    DevSectionCard(title = "Auth & User session", icon = Icons.Filled.Lock) {
        if (user != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(user?.name?.take(1)?.uppercase() ?: "?", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(user?.name ?: "Unknown User", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("ID: ${user?.id?.take(12)}...", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { authViewModel?.signOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }
        } else {
            Text("Current Status: Not Authenticated", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            authResult = "🔄 Logging in as guest..."
                            delay(800)
                            authViewModel?.skipAuth()
                            authResult = "✅ Logged in as Guest"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quick Login", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val newState = !is2FARequired
                        authViewModel?.toggle2FA(newState)
                        authResult = "2FA ${if (newState) "Enabled" else "Disabled"}"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Toggle 2FA", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            authResult = "🔄 Signing in with mock credentials..."
                            authViewModel?.signIn("dev@getneurocomet.com", "password123")
                            delay(1200)
                            authResult = if (authViewModel?.user?.value != null) "✅ Signed in" else "⏳ 2FA Required"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mock Sign In", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            authResult = "🔄 Verifying 2FA code..."
                            authViewModel?.verify2FA("123456")
                            delay(600)
                            authResult = if (authViewModel?.user?.value != null) "✅ 2FA Verified" else "❌ Verification failed"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = is2FARequired
                ) {
                    Text("Verify 2FA", fontSize = 12.sp)
                }
            }
        }
        authResult?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun LocalStorageDevSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<String?>(null) }
    var showInspector by remember { mutableStateOf(false) }

    DevSectionCard(title = "Local Storage Management", icon = Icons.Filled.Storage) {
        Text("Manage app preferences and encrypted credentials.", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(12.dp))

        Text("Secure Credentials", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                scope.launch {
                    val timestamp = System.currentTimeMillis()
                    CredentialStorage.storeCredential(context, "dev_test_key", "secret_$timestamp")
                    result = "Stored dev_test_key"
                }
            }, modifier = Modifier.weight(1f)) { Text("Save Test", fontSize = 11.sp) }

            Button(onClick = {
                scope.launch {
                    val v = CredentialStorage.retrieveCredential(context, "dev_test_key")
                    result = v ?: "Not Found"
                }
            }, modifier = Modifier.weight(1f)) { Text("Load Test", fontSize = 11.sp) }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(modifier = Modifier.alpha(0.2f))
        Spacer(Modifier.height(12.dp))

        Text("General Preferences", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showInspector = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Inspect Keys", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        CredentialStorage.clearAll(context)
                        result = "All storage cleared"
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.CleaningServices, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Wipe All", fontSize = 11.sp)
            }
        }

        result?.let {
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }

    if (showInspector) {
        val prefs = context.getSharedPreferences("neurocomet_secure_prefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all
        AlertDialog(
            onDismissRequest = { showInspector = false },
            title = { Text("SharedPrefs Inspector") },
            text = {
                Box(Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(allEntries.keys.toList()) { key ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text(key, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Text(allEntries[key].toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp).alpha(0.1f))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showInspector = false }) { Text(stringResource(R.string.cd_close)) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageTestingSection(themeViewModel: ThemeViewModel?) {
    val context = LocalContext.current
    val currentLanguage = themeViewModel?.themeState?.collectAsState()?.value?.languageCode ?: "en"

    val languages = listOf(
        "English" to "en",
        "Turkish" to "tr",
        "Hindi" to "hi",
        "Arabic" to "ar",
        "Spanish" to "es"
    )

    DevSectionCard(title = "Language & Localization", icon = Icons.Filled.Language) {
        Text("Active: $currentLanguage", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { (name, code) ->
                FilterChip(
                    selected = currentLanguage == code,
                    onClick = {
                        themeViewModel?.setLanguageCode(code)
                        val appLocale = LocaleListCompat.forLanguageTags(code)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                        Toast.makeText(context, "Locale set to $code", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text(name) }
                )
            }
        }
    }
}

@Composable
fun BiometricFidoDevSection(authViewModel: AuthViewModel?, devOptionsViewModel: DevOptionsViewModel? = null) {
    val context = LocalContext.current
    val hasHardware = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT)
    val biometricStatus = authViewModel?.checkBiometricStatus() ?: com.kyilmaz.neurocomet.auth.BiometricStatus.Unsupported
    val biometricEnabled by authViewModel?.biometricEnabled?.collectAsState() ?: remember { mutableStateOf(false) }
    val fido2Enabled by authViewModel?.fido2Enabled?.collectAsState() ?: remember { mutableStateOf(false) }
    val fido2Credentials by authViewModel?.fido2Credentials?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val totpEnabled by authViewModel?.totpEnabled?.collectAsState() ?: remember { mutableStateOf(false) }
    val backupCodesRemaining by authViewModel?.backupCodesRemaining?.collectAsState() ?: remember { mutableStateOf(0) }
    var authTestResult by remember { mutableStateOf<String?>(null) }
    var showBackupCodes by remember { mutableStateOf(false) }
    var generatedBackupCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    var totpCode by remember { mutableStateOf("") }

    DevSectionCard(title = "Biometric, FIDO2 & MFA", icon = Icons.Filled.Fingerprint) {
        // --- Status Overview ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Hardware Detected: ${if (hasHardware) "✅ Yes" else "❌ No"}", style = MaterialTheme.typography.bodySmall)
                Text("Biometric Status: $biometricStatus", style = MaterialTheme.typography.bodySmall)
                Text("FIDO2 Supported: ${if (authViewModel?.isFido2Supported() == true) "✅" else "❌"}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(Modifier.padding(vertical = 4.dp).alpha(0.2f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (biometricEnabled) "🟢" else "🔴", fontSize = 16.sp)
                        Text("Biometric", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (fido2Enabled) "🟢" else "🔴", fontSize = 16.sp)
                        Text("FIDO2", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (totpEnabled) "🟢" else "🔴", fontSize = 16.sp)
                        Text("TOTP", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$backupCodesRemaining", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Backup", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- Biometric Authentication ---
        Text("Biometric Authentication", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        DevToggleRowSimple(
            title = "Biometric Enabled",
            subtitle = "Allow fingerprint/face unlock",
            isChecked = biometricEnabled,
            onCheckedChange = { authViewModel?.setBiometricEnabled(it) }
        )
        Button(
            onClick = {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    authViewModel?.authenticateWithBiometric(activity) { result ->
                        authTestResult = when (result) {
                            is AuthResult.Success -> "✅ Biometric auth succeeded"
                            is AuthResult.Error -> "❌ ${result.message}"
                            is AuthResult.NotAvailable -> "⚠️ Biometric not available"
                            else -> "Result: $result"
                        }
                    }
                } else {
                    authTestResult = "❌ Context is not FragmentActivity"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hasHardware && biometricEnabled
        ) {
            Icon(Icons.Filled.LockOpen, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Test Biometric Prompt")
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(Modifier.alpha(0.2f))
        Spacer(Modifier.height(12.dp))

        // --- FIDO2 / Passkey ---
        Text("FIDO2 / Passkey Credentials", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        if (fido2Credentials.isNotEmpty()) {
            fido2Credentials.forEach { cred ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(cred.displayName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text("ID: ${cred.credentialId.take(16)}...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        authViewModel?.removeFido2Credential(cred.credentialId)
                        authTestResult = "🗑️ Removed credential: ${cred.displayName}"
                    }) {
                        Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        } else {
            Text("No FIDO2 credentials registered", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    authViewModel?.registerFido2Credential("Dev Test Key") { result ->
                        authTestResult = when (result) {
                            is AuthResult.Success -> "✅ FIDO2 credential registered"
                            is AuthResult.Error -> "❌ ${result.message}"
                            else -> "Result: $result"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Register Key", fontSize = 11.sp) }
            OutlinedButton(
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        authViewModel?.authenticateWithFido2(activity) { result ->
                            authTestResult = when (result) {
                                is AuthResult.Success -> "✅ FIDO2 auth succeeded"
                                is AuthResult.Error -> "❌ ${result.message}"
                                else -> "Result: $result"
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = fido2Credentials.isNotEmpty()
            ) { Text("Test Auth", fontSize = 11.sp) }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(Modifier.alpha(0.2f))
        Spacer(Modifier.height(12.dp))

        // --- TOTP ---
        Text("TOTP (Time-based One-Time Password)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        if (totpEnabled) {
            val currentCode = authViewModel?.getCurrentTotpCode()
            if (currentCode != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Current Code: ", style = MaterialTheme.typography.bodySmall)
                        Text(currentCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = totpCode,
                    onValueChange = { totpCode = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Verify Code") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val valid = authViewModel?.verifyTotpCode(totpCode) == true
                        authTestResult = if (valid) "✅ TOTP code valid" else "❌ Invalid code"
                        totpCode = ""
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                    enabled = totpCode.length == 6
                ) { Text(stringResource(R.string.settings_pin_verify)) }
            }
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    authViewModel?.disableTotp()
                    authTestResult = "🗑️ TOTP disabled"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Disable TOTP", fontSize = 12.sp) }
        } else {
            Button(
                onClick = {
                    authViewModel?.startTotpSetup("dev@getneurocomet.com")
                    authTestResult = "📱 TOTP setup started — use authenticator app to scan"
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Setup TOTP") }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(Modifier.alpha(0.2f))
        Spacer(Modifier.height(12.dp))

        // --- Backup Codes ---
        Text("Backup Codes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text("Remaining: $backupCodesRemaining", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    generatedBackupCodes = authViewModel?.generateBackupCodes() ?: emptyList()
                    showBackupCodes = true
                    authTestResult = "🔑 Generated ${generatedBackupCodes.size} backup codes"
                },
                modifier = Modifier.weight(1f)
            ) { Text("Generate", fontSize = 11.sp) }
            OutlinedButton(
                onClick = {
                    authViewModel?.verifyBackupCode("BACKUP-001") { result ->
                        authTestResult = when (result) {
                            is AuthResult.Success -> "✅ Backup code accepted"
                            is AuthResult.Error -> "❌ ${result.message}"
                            else -> "Result: $result"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = backupCodesRemaining > 0
            ) { Text("Test Code", fontSize = 11.sp) }
        }

        // --- Result Display ---
        authTestResult?.let {
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }

    if (showBackupCodes && generatedBackupCodes.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBackupCodes = false },
            title = { Text("Backup Codes") },
            text = {
                Column {
                    Text("Save these codes securely. Each can only be used once.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    generatedBackupCodes.forEachIndexed { i, code ->
                        Text("${i + 1}. $code", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showBackupCodes = false }) { Text(stringResource(R.string.action_done)) } }
        )
    }
}

@Composable
fun PaymentsTestingSection() {
    val context = LocalContext.current
    val subState by SubscriptionManager.subscriptionState.collectAsState()

    var showTestPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }
    
    if (showTestPopup) {
        AlertDialog(
            onDismissRequest = { showTestPopup = false },
            title = { Text("Simulation Result") },
            text = { Text(popupMessage) },
            confirmButton = {
                TextButton(onClick = { showTestPopup = false }) {
                    Text("OK")
                }
            }
        )
    }

    DevSectionCard(
        title = "Payments & Subscriptions",
        icon = Icons.Filled.Payment
    ) {
        val coroutineScope = rememberCoroutineScope()
        
        Text(
            text = "Simulate payment outcomes without reaching the real payment sheet. Useful for verifying UI properly reacts to success, decline, or timeout events.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Current State", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Is Premium: ${subState.isPremium}", style = MaterialTheme.typography.bodySmall)
                Text("Is Loading: ${subState.isLoading}", style = MaterialTheme.typography.bodySmall)
                Text("Error: ${subState.error ?: "None"}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = {
                SubscriptionManager.simulateTestSuccess()
                popupMessage = "🎉 Woohoo! Simulation successful. The dopamine hit has been deposited, and your premium powers are now fully unlocked."
                showTestPopup = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Simulate Successful Payment")
        }

        Button(
            onClick = {
                SubscriptionManager.simulateTestDeclined()
                popupMessage = "🚫 Payment declined. Simulation complete. Sometimes the executive function to find the right credit card just isn't there today."
                showTestPopup = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Simulate Declined Payment")
        }

        Button(
            onClick = {
                SubscriptionManager.simulateTestTimedOut()
                popupMessage = "⏳ Timed-Out! The server got distracted by a shiny side quest and forgot to finish your transaction. Simulation complete."
                showTestPopup = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Simulate Timed-Out Payment")
        }

        OutlinedButton(
            onClick = {
                SubscriptionManager.resetTestPurchase()
                popupMessage = "🔄 Reset complete. The simulation chalkboard has been wiped clean. You are back to the free tier."
                showTestPopup = true
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Reset Premium Status")
        }
    }
}

@Composable
fun DmDebugDevSection(devOptionsViewModel: DevOptionsViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val options by devOptionsViewModel.options.collectAsState()
    var delayInput by remember { mutableStateOf(options.dmArtificialSendDelayMs.toString()) }

    LaunchedEffect(Unit) { devOptionsViewModel.refresh(application) }

    DevSectionCard(title = "DM Delivery Simulation", icon = Icons.AutoMirrored.Filled.Chat) {
        Text("Control direct message behavior for testing edge cases.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        DevToggleRowSimple(
            title = "Show DM Debug Overlay",
            subtitle = "Display delivery status info on DM screen",
            isChecked = options.showDmDebugOverlay,
            onCheckedChange = { devOptionsViewModel.setShowDmDebugOverlay(application, it) }
        )

        DevToggleRowSimple(
            title = "Force Send Failure",
            subtitle = "Simulate message send failures",
            isChecked = options.dmForceSendFailure,
            onCheckedChange = { devOptionsViewModel.setDmForceSendFailure(application, it) }
        )

        DevToggleRowSimple(
            title = "Disable Rate Limiting",
            subtitle = "Remove message throttling",
            isChecked = options.dmDisableRateLimit,
            onCheckedChange = { devOptionsViewModel.setDmDisableRateLimit(application, it) }
        )

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = delayInput,
                onValueChange = { delayInput = it.filter { c -> c.isDigit() }.take(5) },
                label = { Text("Send Delay (ms)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Button(onClick = {
                val delay = delayInput.toLongOrNull() ?: 0L
                devOptionsViewModel.setDmSendDelayMs(application, delay)
                Toast.makeText(context, "Delay set to ${delay}ms", Toast.LENGTH_SHORT).show()
            }) { Text(stringResource(R.string.action_apply)) }
        }

        Spacer(Modifier.height(8.dp))
        Text("Moderation Override", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DevModerationOverride.entries.forEach { override ->
                FilterChip(
                    selected = options.moderationOverride == override,
                    onClick = { devOptionsViewModel.setModerationOverride(application, override) },
                    label = { Text(override.name, fontSize = 11.sp) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentSafetyDevSection(devOptionsViewModel: DevOptionsViewModel, safetyViewModel: SafetyViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val options by devOptionsViewModel.options.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        devOptionsViewModel.refresh(application)
        safetyViewModel.refresh(application)
    }

    DevSectionCard(title = "Content Safety & Age Filtering", icon = Icons.Filled.Security) {
        Text("Control audience targeting, kids mode, and parental PIN simulation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        // Current state display
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Active Audience: ${safetyState.audience.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Kids Mode: ${if (safetyState.isKidsMode) "ON" else "OFF"}", style = MaterialTheme.typography.bodySmall)
                Text("Filter Level: ${safetyState.kidsFilterLevel.name}", style = MaterialTheme.typography.bodySmall)
                Text("Parental PIN Set: ${if (safetyState.isParentalPinSet) "Yes" else "No"}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Force Audience", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = options.forceAudience == null,
                onClick = {
                    devOptionsViewModel.setForceAudience(application, null)
                    safetyViewModel.refresh(application)
                },
                label = { Text("None", fontSize = 11.sp) }
            )
            Audience.entries.forEach { audience ->
                FilterChip(
                    selected = options.forceAudience == audience,
                    onClick = {
                        devOptionsViewModel.setForceAudience(application, audience)
                        safetyViewModel.refresh(application)
                    },
                    label = { Text(audience.name, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Force Kids Filter Level", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = options.forceKidsFilterLevel == null,
                onClick = {
                    devOptionsViewModel.setForceKidsFilterLevel(application, null)
                    safetyViewModel.refresh(application)
                },
                label = { Text("None", fontSize = 11.sp) }
            )
            KidsFilterLevel.entries.forEach { level ->
                FilterChip(
                    selected = options.forceKidsFilterLevel == level,
                    onClick = {
                        devOptionsViewModel.setForceKidsFilterLevel(application, level)
                        safetyViewModel.refresh(application)
                    },
                    label = { Text(level.name, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        DevToggleRowSimple(
            title = "Force Parental PIN Set",
            subtitle = "Simulate parental PIN being configured",
            isChecked = options.forcePinSet,
            onCheckedChange = {
                devOptionsViewModel.setForcePinSet(application, it)
                safetyViewModel.refresh(application)
            }
        )

        DevToggleRowSimple(
            title = "Force PIN Verify Success",
            subtitle = "Auto-pass PIN verification checks",
            isChecked = options.forcePinVerifySuccess,
            onCheckedChange = {
                devOptionsViewModel.setForcePinVerifySuccess(application, it)
                safetyViewModel.refresh(application)
            }
        )

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                devOptionsViewModel.resetAll(application)
                safetyViewModel.refresh(application)
                Toast.makeText(context, "All dev options reset to defaults", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.RestartAlt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Reset All Dev Options")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NEW SECTIONS
// ═══════════════════════════════════════════════════════════════

@Composable
fun AppInfoDevSection(devOptionsViewModel: DevOptionsViewModel? = null) {
    val context = LocalContext.current
    val packageInfo = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }.getOrNull()
    }
    val runtime = Runtime.getRuntime()
    var memoryInfo by remember {
        mutableStateOf(
            Triple(
                runtime.totalMemory() / (1024 * 1024),
                runtime.freeMemory() / (1024 * 1024),
                runtime.maxMemory() / (1024 * 1024)
            )
        )
    }

    DevSectionCard(title = "App Info & Diagnostics", icon = Icons.Filled.Info) {
        val deviceHash = remember { DeviceAuthority.computeDeviceHash(context) }
        val isAuthorized = remember { DeviceAuthority.isAuthorizedDevice(context) }
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

        val buildCode = if (Build.VERSION.SDK_INT >= 28) {
            packageInfo?.longVersionCode?.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toString()
        } ?: "—"

        val infoRows = listOf(
            "Package" to context.packageName,
            "Version" to (packageInfo?.versionName ?: "—"),
            "Build" to buildCode,
            "Build Type" to if (BuildConfig.DEBUG) "DEBUG" else "RELEASE",
            "Device" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "OS" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Heap Used" to "${memoryInfo.first - memoryInfo.second} MB / ${memoryInfo.third} MB max"
        )

        infoRows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Device Authority Hash ─────────────────────────────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isAuthorized) Color(0xFF1B5E20).copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isAuthorized) Icons.Filled.VerifiedUser else Icons.Filled.GppBad,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isAuthorized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isAuthorized) "Authorized Developer Device" else "Unauthorized Device",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAuthorized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Device Hash:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    deviceHash,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        clipboardManager.setPrimaryClip(android.content.ClipData.newPlainText("Device Hash", deviceHash))
                        Toast.makeText(context, "Device hash copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ContentCopy, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Device Hash", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                val r = Runtime.getRuntime()
                memoryInfo = Triple(
                    r.totalMemory() / (1024 * 1024),
                    r.freeMemory() / (1024 * 1024),
                    r.maxMemory() / (1024 * 1024)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Refresh Memory Stats")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnvironmentPickerDevSection(devOptionsViewModel: DevOptionsViewModel) {
    val options by devOptionsViewModel.options.collectAsState()
    var showConfirmDialog by remember { mutableStateOf<DevEnvironment?>(null) }

    DevSectionCard(title = "Environment", icon = Icons.Filled.Cloud) {
        Text(
            "Switch between backend environments. Requires app restart to take effect.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DevEnvironment.entries.forEach { env ->
                FilterChip(
                    selected = options.environment == env,
                    onClick = {
                        if (options.environment != env) {
                            showConfirmDialog = env
                        }
                    },
                    label = { Text(env.name) },
                    leadingIcon = {
                        if (options.environment == env) {
                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (options.environment) {
                    DevEnvironment.PRODUCTION -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    DevEnvironment.STAGING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    DevEnvironment.LOCAL -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                }
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (options.environment) {
                        DevEnvironment.PRODUCTION -> Icons.Filled.VerifiedUser
                        DevEnvironment.STAGING -> Icons.Filled.Science
                        DevEnvironment.LOCAL -> Icons.Filled.Computer
                    },
                    null,
                    Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Active: ${options.environment.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    showConfirmDialog?.let { targetEnv ->
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Switch Environment?") },
            text = { Text("Switching to ${targetEnv.name} environment. The app may need to be restarted for all changes to take effect.") },
            confirmButton = {
                TextButton(onClick = {
                    devOptionsViewModel.setEnvironment(targetEnv)
                    showConfirmDialog = null
                }) { Text("Switch") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
fun FeatureFlagsDevSection(devOptionsViewModel: DevOptionsViewModel) {
    val options by devOptionsViewModel.options.collectAsState()

    DevSectionCard(title = "Feature Flags", icon = Icons.Filled.Flag) {
        Text(
            "Toggle experimental features on/off. These are persisted across restarts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        DevToggleRowSimple(
            title = "New Feed Layout",
            subtitle = "Use experimental feed grid layout",
            isChecked = options.enableNewFeedLayout,
            onCheckedChange = { devOptionsViewModel.setEnableNewFeedLayout(it) }
        )
        DevToggleRowSimple(
            title = "Video Chat",
            subtitle = "Enable video calling feature",
            isChecked = options.enableVideoChat,
            onCheckedChange = { devOptionsViewModel.setEnableVideoChat(it) }
        )
        DevToggleRowSimple(
            title = "Story Reactions",
            subtitle = "Allow emoji reactions on stories",
            isChecked = options.enableStoryReactions,
            onCheckedChange = { devOptionsViewModel.setEnableStoryReactions(it) }
        )
        DevToggleRowSimple(
            title = "Advanced Search",
            subtitle = "Enable full-text and filter search",
            isChecked = options.enableAdvancedSearch,
            onCheckedChange = { devOptionsViewModel.setEnableAdvancedSearch(it) }
        )
        DevToggleRowSimple(
            title = "AI Suggestions",
            subtitle = "Show AI-powered content suggestions",
            isChecked = options.enableAiSuggestions,
            onCheckedChange = { devOptionsViewModel.setEnableAiSuggestions(it) }
        )

        Spacer(Modifier.height(8.dp))
        val activeFlags = listOf(
            options.enableNewFeedLayout,
            options.enableVideoChat,
            options.enableStoryReactions,
            options.enableAdvancedSearch,
            options.enableAiSuggestions
        ).count { it }
        Text(
            "$activeFlags of 5 flags enabled",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Cross-Device section (API 37+ features, graceful on API 36)
    DevSectionCard(title = "Cross-Device", icon = Icons.Filled.Devices) {
        Text(
            if (Build.VERSION.SDK_INT >= 37)
                "Android 17 cross-device handoff allows seamless activity transfer to nearby devices."
            else
                "Requires Android 17 (API 37). Not available on this device (API ${Build.VERSION.SDK_INT}).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        DevToggleRowSimple(
            title = "Device Handoff",
            subtitle = "Allow transferring the current screen to a nearby device",
            isChecked = options.enableHandoff,
            onCheckedChange = { devOptionsViewModel.setEnableHandoff(it) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RenderingNetworkDevSection(devOptionsViewModel: DevOptionsViewModel) {
    val context = LocalContext.current
    val options by devOptionsViewModel.options.collectAsState()
    var latencyInput by remember { mutableStateOf(options.networkLatencyMs.toString()) }

    DevSectionCard(title = "Rendering & Network Simulation", icon = Icons.Filled.NetworkCheck) {
        Text(
            "Test rendering edge cases and simulate network conditions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        DevToggleRowSimple(
            title = "Simulate Offline",
            subtitle = "Pretend device has no network",
            isChecked = options.simulateOffline,
            onCheckedChange = { devOptionsViewModel.setSimulateOffline(it) }
        )
        DevToggleRowSimple(
            title = "Simulate Loading Error",
            subtitle = "Force data fetches to fail",
            isChecked = options.simulateLoadingError,
            onCheckedChange = { devOptionsViewModel.setSimulateLoadingError(it) }
        )
        DevToggleRowSimple(
            title = "Infinite Loading",
            subtitle = "Keep loading spinners running forever",
            isChecked = options.infiniteLoading,
            onCheckedChange = { devOptionsViewModel.setInfiniteLoading(it) }
        )
        DevToggleRowSimple(
            title = "Show Fallback UI",
            subtitle = "Show fallback/placeholder UI components",
            isChecked = options.showFallbackUi,
            onCheckedChange = { devOptionsViewModel.setShowFallbackUi(it) }
        )
        DevToggleRowSimple(
            title = "Performance Overlay",
            subtitle = "Show FPS and rendering metrics",
            isChecked = options.showPerformanceOverlay,
            onCheckedChange = { devOptionsViewModel.setShowPerformanceOverlay(it) }
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = latencyInput,
                onValueChange = { latencyInput = it.filter { c -> c.isDigit() }.take(5) },
                label = { Text("Network Latency (ms)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Button(onClick = {
                val ms = latencyInput.toLongOrNull() ?: 0L
                devOptionsViewModel.setNetworkLatencyMs(ms)
                Toast.makeText(context, "Latency set to ${ms}ms", Toast.LENGTH_SHORT).show()
            }) { Text(stringResource(R.string.action_apply)) }
        }
    }
}

// ─── Contact Picker (API 37 + Legacy) ──────────────────

/**
 * Developer-options section that exercises the API 37 ContactsPickerSessionContract
 * as well as the legacy PickContact() fallback — with runtime API checks.
 */
@Composable
fun ContactPickerDevSection() {
    val context = LocalContext.current
    val isApi37Plus = Build.VERSION.SDK_INT >= 37

    // ── State ───────────────────────────────────────────────
    var forceLegacy by remember { mutableStateOf(false) }
    var allowMultiple by remember { mutableStateOf(false) }
    var resultLog by remember { mutableStateOf<String?>(null) }
    var errorLog by remember { mutableStateOf<String?>(null) }
    var pickCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // ── Permission launcher (legacy path, API ≤ 36) ────────
    var pendingLegacyLaunch by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingLegacyLaunch = true
        } else {
            errorLog = "READ_CONTACTS permission denied"
        }
    }

    // ── Legacy contact picker launcher ──────────────────────
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        isLoading = false
        if (uri != null) {
            var displayName = "Unknown"
            var resolveWarning: String? = null
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                        if (idx >= 0) displayName = cursor.getString(idx) ?: "Unknown"
                    }
                }
            } catch (se: SecurityException) {
                resolveWarning = "SecurityException: READ_CONTACTS not granted — name unavailable"
                displayName = "(permission denied)"
            } catch (e: Exception) {
                resolveWarning = "Resolve error: ${e.message}"
                displayName = "(error)"
            }
            pickCount++
            resultLog = buildString {
                appendLine("✅ Legacy picker result #$pickCount")
                appendLine("Name: $displayName")
                appendLine("URI: $uri")
                resolveWarning?.let {
                    appendLine()
                    appendLine("⚠️ $it")
                }
            }
            if (resolveWarning != null && errorLog == null) errorLog = resolveWarning
        } else {
            resultLog = "Picker cancelled (no URI returned)"
        }
    }

    // ── API 37+ session-based picker launcher ───────────────
    val api37PickerLauncher = rememberLauncherForActivityResult(
        contract = object : androidx.activity.result.contract.ActivityResultContract<Intent, Uri?>() {
            override fun createIntent(context: Context, input: Intent): Intent = input
            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                return if (resultCode == android.app.Activity.RESULT_OK) intent?.data else null
            }
        }
    ) { sessionUri: Uri? ->
        isLoading = false
        if (sessionUri != null) {
            try {
                val contacts = AttachmentHelper.queryContactPickerSession(
                    context.contentResolver, sessionUri
                )
                pickCount++
                if (contacts.isNotEmpty()) {
                    resultLog = buildString {
                        appendLine("✅ API 37 picker result #$pickCount (${contacts.size} contact${if (contacts.size != 1) "s" else ""})")
                        appendLine()
                        contacts.forEachIndexed { idx, c ->
                            appendLine("── Contact ${idx + 1} ──")
                            appendLine("  Name:  ${c.displayName}")
                            appendLine("  Phone: ${c.phone ?: "—"}")
                            appendLine("  Email: ${c.email ?: "—"}")
                        }
                        appendLine()
                        appendLine("Session URI: $sessionUri")
                    }
                    errorLog = null
                } else {
                    resultLog = "Session returned 0 contacts"
                    errorLog = "Empty cursor from session URI"
                }
            } catch (e: Exception) {
                errorLog = "Session query failed: ${e.message}"
                resultLog = null
            }
        } else {
            resultLog = "Picker cancelled (no session URI returned)"
        }
    }

    // ── Launch after permission granted (deferred) ──────────
    LaunchedEffect(pendingLegacyLaunch) {
        if (pendingLegacyLaunch) {
            pendingLegacyLaunch = false
            isLoading = true
            legacyPickerLauncher.launch(null)
        }
    }

    // ── Pick action ─────────────────────────────────────────
    fun pickContact() {
        errorLog = null
        resultLog = null
        if (isApi37Plus && !forceLegacy) {
            try {
                val intent = AttachmentHelper.buildContactsPickerIntent(allowMultiple = allowMultiple)
                isLoading = true
                api37PickerLauncher.launch(intent)
            } catch (e: Exception) {
                errorLog = "API 37 picker failed: ${e.message}\nFalling back to legacy…"
                isLoading = true
                legacyPickerLauncher.launch(null)
            }
        } else {
            if (isApi37Plus) {
                errorLog = "⚠️ Force Legacy on API 37+: using legacy picker. READ_CONTACTS may be needed."
            }
            isLoading = true
            legacyPickerLauncher.launch(null)
        }
    }

    // ── UI ──────────────────────────────────────────────────
    DevSectionCard(title = "Contact Picker (API 37 + Legacy)", icon = Icons.Filled.Contacts) {
        Text(
            "Test the Android 17 privacy-preserving Contact Picker and the legacy fallback.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // ── Device info card ────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isApi37Plus) Color(0xFF1B5E20).copy(alpha = 0.15f)
                else Color(0xFFFFF3E0)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    if (isApi37Plus) "✅ API 37+ detected — session picker available"
                    else "⚠️ API ${Build.VERSION.SDK_INT} — using legacy picker",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "SDK: ${Build.VERSION.SDK_INT} • Release: ${Build.VERSION.RELEASE} • Device: ${Build.MODEL}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "READ_CONTACTS granted: ${AttachmentHelper.hasContactsPermission(context)}" +
                        if (isApi37Plus) " (not needed — session picker)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Toggles ─────────────────────────────────────────
        DevToggleRowSimple(
            title = "Force Legacy Picker",
            subtitle = "Bypass API 37 picker even on Android 17+",
            isChecked = forceLegacy,
            onCheckedChange = { forceLegacy = it }
        )

        DevToggleRowSimple(
            title = "Allow Multiple",
            subtitle = "Request multi-select (API 37+ only)",
            isChecked = allowMultiple,
            onCheckedChange = { allowMultiple = it }
        )

        Spacer(Modifier.height(12.dp))

        // ── Pick button ─────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { pickContact() },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isApi37Plus && !forceLegacy) "Pick Contact (API 37)"
                    else "Pick Contact (Legacy)",
                    fontSize = 12.sp
                )
            }
            OutlinedButton(
                onClick = {
                    resultLog = null
                    errorLog = null
                    pickCount = 0
                    isLoading = false
                },
                modifier = Modifier.weight(0.5f)
            ) {
                Text(stringResource(R.string.cd_clear), fontSize = 12.sp)
            }
        }

        // ── Picks counter ───────────────────────────────────
        if (pickCount > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Total picks this session: $pickCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ── Error display ───────────────────────────────────
        errorLog?.let { err ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // ── Result display ──────────────────────────────────
        resultLog?.let { result ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Result",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        result,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// A/B Testing Dev Section
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ABTestingDevSection() {
    val context = LocalContext.current
    val isAuthorized = remember { DeviceAuthority.isAuthorizedDevice(context) }

    // Refresh experiment state on first composition
    LaunchedEffect(Unit) {
        if (isAuthorized) ABTestManager.refreshState(context)
    }

    val experimentsMap by ABTestManager.experiments.collectAsState()
    val experiments = experimentsMap.values.toList().sortedBy { it.experiment.displayName }

    DevSectionCard(title = "A/B Testing", icon = Icons.Filled.Science) {
        if (!isAuthorized) {
            Text(
                "⚠️ A/B testing is restricted to authorized developer devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            return@DevSectionCard
        }

        Text(
            "Manage experiment variants for this device. Overrides are device-local and persist across restarts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // ── Summary chips ──────────────────────────────────────
        val totalExperiments = experiments.size
        val overriddenCount = experiments.count { it.isOverridden }
        val nonControlCount = experiments.count { it.activeVariant != it.experiment.variants.first() }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {},
                label = { Text("$totalExperiments experiments") },
                leadingIcon = { Icon(Icons.Filled.Science, null, Modifier.size(16.dp)) }
            )
            if (overriddenCount > 0) {
                AssistChip(
                    onClick = {},
                    label = { Text("$overriddenCount overridden") },
                    leadingIcon = { Icon(Icons.Filled.Edit, null, Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
            if (nonControlCount > 0) {
                AssistChip(
                    onClick = {},
                    label = { Text("$nonControlCount non-control") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null, Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Quick actions ──────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    ABTestManager.clearAllOverrides(context)
                    Toast.makeText(context, "All overrides cleared", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                enabled = overriddenCount > 0
            ) {
                Icon(Icons.Filled.RestartAlt, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.games_clear_all), fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = {
                    ABTestManager.rerollAllAssignments(context)
                    Toast.makeText(context, "Re-rolled all assignments", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Casino, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Re-roll", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // ── Per-experiment controls ────────────────────────────
        experiments.forEach { state ->
            ABExperimentRow(state, context)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ABExperimentRow(state: ExperimentState, context: Context) {
    val exp = state.experiment
    var expanded by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Hard-coded dev-tool palette that stays legible in any theme
    val overriddenCardBg = if (isDark) Color(0xFF2D1F4E) else Color(0xFFEDE7F6)
    val defaultCardBg = if (isDark) Color(0xFF2A2A2E) else Color(0xFFF0F0F4)
    val badgeControlBg = if (isDark) Color(0xFF555560) else Color(0xFFBDBDC7)
    val badgeControlText = if (isDark) Color(0xFFE8E8EC) else Color(0xFF2A2A2E)
    val badgeActiveBg = if (isDark) Color(0xFF7C4DFF) else Color(0xFF5C35CC)
    val badgeActiveText = Color.White
    val titleColor = if (isDark) Color(0xFFF0F0F4) else Color(0xFF1A1A24)
    val descColor = if (isDark) Color(0xFFB0B0BC) else Color(0xFF5A5A6A)
    val metaColor = if (isDark) Color(0xFF9898A8) else Color(0xFF6E6E7E)
    val metaIconColor = if (isDark) Color(0xFF8888A0) else Color(0xFF7A7A8E)
    val overriddenLabelColor = if (isDark) Color(0xFFCE93D8) else Color(0xFF8E24AA)
    val dividerColor = if (isDark) Color(0xFF444450) else Color(0xFFD0D0DA)

    // Chip palette — fully opaque, high contrast
    val chipAutoSelectedBg = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val chipAutoSelectedText = Color.White
    val chipVariantSelectedBg = if (isDark) Color(0xFF7C4DFF) else Color(0xFF5C35CC)
    val chipVariantSelectedText = Color.White
    val chipUnselectedBg = if (isDark) Color(0xFF3A3A42) else Color(0xFFE4E4EC)
    val chipUnselectedText = if (isDark) Color(0xFFCCCCD6) else Color(0xFF3A3A4A)
    val chipUnselectedBorder = if (isDark) Color(0xFF555562) else Color(0xFFC0C0CC)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (state.isOverridden) overriddenCardBg else defaultCardBg
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Title row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        exp.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    Text(
                        exp.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = descColor,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))

                // Active variant badge — solid background, no alpha
                val isControl = state.activeVariant == exp.variants.first()
                Surface(
                    color = if (isControl) badgeControlBg else badgeActiveBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        state.activeVariant.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isControl) badgeControlText else badgeActiveText
                    )
                }

                Spacer(Modifier.width(4.dp))

                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Toggle details",
                    modifier = Modifier.size(20.dp),
                    tint = metaIconColor
                )
            }

            // Expanded: variant selector
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(Modifier.height(8.dp))

                // Assigned info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Tag, null, Modifier.size(14.dp), tint = metaIconColor)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Assigned: ${state.assignedVariant}",
                        style = MaterialTheme.typography.labelSmall,
                        color = metaColor
                    )
                    if (state.isOverridden) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "→ overridden",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = overriddenLabelColor
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Traffic split info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PieChart, null, Modifier.size(14.dp), tint = metaIconColor)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Split: ${exp.variants.zip(exp.trafficSplit).joinToString(" · ") { (v, p) -> "$v ${p}%" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = metaColor
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Variant buttons
                Text(
                    "Select variant:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // "Auto" button (clears override)
                    val isAuto = !state.isOverridden
                    FilterChip(
                        selected = isAuto,
                        onClick = {
                            ABTestManager.setOverride(context, exp, null)
                        },
                        label = {
                            Text(
                                "Auto",
                                fontSize = 11.sp,
                                fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAuto) chipAutoSelectedText else chipUnselectedText
                            )
                        },
                        leadingIcon = if (isAuto) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(14.dp), tint = chipAutoSelectedText) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipAutoSelectedBg,
                            selectedLabelColor = chipAutoSelectedText,
                            selectedLeadingIconColor = chipAutoSelectedText,
                            containerColor = chipUnselectedBg,
                            labelColor = chipUnselectedText
                        ),
                        border = if (!isAuto) FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = chipUnselectedBorder
                        ) else null
                    )

                    // One chip per variant
                    exp.variants.forEach { variant ->
                        val isSelected = state.isOverridden && state.overrideVariant == variant
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                ABTestManager.setOverride(context, exp, variant)
                            },
                            label = {
                                Text(
                                    variant.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) chipVariantSelectedText else chipUnselectedText
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(14.dp), tint = chipVariantSelectedText) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipVariantSelectedBg,
                                selectedLabelColor = chipVariantSelectedText,
                                selectedLeadingIconColor = chipVariantSelectedText,
                                containerColor = chipUnselectedBg,
                                labelColor = chipUnselectedText
                            ),
                            border = if (!isSelected) FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = chipUnselectedBorder
                            ) else null
                        )
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// BACKUP & RESTORE — DEV TESTING SECTION
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun BackupDevTestSection(onNavigateToBackup: () -> Unit) {
    val context = LocalContext.current
    val isAuthorized = remember { DeviceAuthority.isAuthorizedDevice(context) }

    DevSectionCard(title = "Backup & Restore Testing", icon = Icons.Filled.CloudUpload) {
        if (!isAuthorized) {
            Text(
                "⛔ This device is not authorized for backup testing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                "Test backup creation, restore, and local storage on this whitelisted device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onNavigateToBackup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Backup Settings")
            }

            Spacer(Modifier.height(8.dp))

            val backupViewModel: BackupViewModel = viewModel()
            val backupState by backupViewModel.state.collectAsState()

            OutlinedButton(
                onClick = { backupViewModel.createBackup() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !backupState.isBackingUp && !backupState.isRestoring
            ) {
                if (backupState.isBackingUp) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(backupState.progress.stage)
                } else {
                    Icon(Icons.Filled.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Quick Backup (All Data)")
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Test suite button ──────────────────────────
            OutlinedButton(
                onClick = { backupViewModel.createTestBackups() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !backupState.isBackingUp && !backupState.isRestoring
            ) {
                if (backupState.isBackingUp) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(backupState.progress.stage)
                } else {
                    Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Create 4 Test Backups")
                }
            }

            // ── Status messages ─────────────────────────────
            backupState.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            backupState.successMessage?.let { success ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF1B5E20).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("Result", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(success, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                    }
                }
            }

            if (backupState.localBackups.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${backupState.localBackups.size} local backup(s) stored",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show each backup with validate + restore buttons
                backupState.localBackups.take(6).forEach { backup ->
                    val scopeLabel = buildString {
                        if (backup.scope.includeProfile) append("P")
                        if (backup.scope.includeMessages) append("M")
                        if (backup.scope.includePosts) append("T")
                        if (backup.scope.includeBookmarks) append("B")
                        if (backup.scope.includeFollows) append("F")
                        if (backup.scope.includeSettings) append("S")
                        if (backup.scope.includeNotifications) append("N")
                    }.ifEmpty { "?" }

                    val scopeCount = listOf(
                        backup.scope.includeProfile, backup.scope.includeMessages,
                        backup.scope.includePosts, backup.scope.includeBookmarks,
                        backup.scope.includeFollows, backup.scope.includeSettings,
                        backup.scope.includeNotifications
                    ).count { it }

                    val scopeColor = when {
                        scopeCount == 7 -> Color(0xFF4CAF50)   // Full — green
                        scopeCount >= 4 -> Color(0xFF2196F3)   // Most — blue
                        scopeCount >= 2 -> Color(0xFFFF9800)   // Partial — orange
                        scopeCount == 1 -> Color(0xFFE91E63)   // Single — pink
                        else -> MaterialTheme.colorScheme.outline
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        border = BorderStroke(1.dp, scopeColor.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Scope indicator dot
                                Box(
                                    Modifier.size(8.dp)
                                        .background(scopeColor, shape = CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Column(Modifier.weight(1f)) {
                                    // Show label if present (test backups)
                                    if (backup.label != null) {
                                        Text(
                                            backup.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = scopeColor
                                        )
                                    }
                                    Text(
                                        backup.formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "${backup.formattedSize} | Scope: $scopeLabel ($scopeCount/7) | ${backup.storageLocation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { backupViewModel.validateBackup(backup.backupId) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Validate", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(
                                    onClick = { backupViewModel.restoreBackup(backup.backupId) },
                                    modifier = Modifier.height(32.dp),
                                    enabled = !backupState.isRestoring,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Restore", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(
                                    onClick = { backupViewModel.deleteBackup(backup.backupId) },
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                if (backupState.localBackups.size > 6) {
                    Text(
                        "+${backupState.localBackups.size - 6} more — open Backup Settings to see all",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { backupViewModel.deleteAllBackups() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete All Backups")
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════
// FEEDBACK & BETA TESTING
// ═══════════════════════════════════════════════════════════════

@Composable
fun FeedbackBetaDevSection(
    devOptionsViewModel: DevOptionsViewModel,
    onNavigateBugReport: () -> Unit = {},
    onNavigateFeatureRequest: () -> Unit = {},
    onNavigateGeneralFeedback: () -> Unit = {},
    onNavigateFeedbackHub: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val options by devOptionsViewModel.options.collectAsState()

    var pendingCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("neurocomet_feedback", Context.MODE_PRIVATE)
        pendingCount = prefs.getStringSet("pending_feedback", emptySet())?.size ?: 0
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Test feedback submission, offline queuing, and rate-limit behaviour for the closed-beta feedback system.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bypass Feedback Rate Limit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Skip the 5/day submission cap", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = options.bypassFeedbackRateLimit,
                onCheckedChange = { devOptionsViewModel.setBypassFeedbackRateLimit(it) }
            )
        }

        HorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Force Submission Failure", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Always fail Supabase insert → queue offline", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = options.forceFeedbackSubmitFailure,
                onCheckedChange = { devOptionsViewModel.setForceFeedbackSubmitFailure(it) }
            )
        }

        HorizontalDivider()

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (pendingCount > 0)
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$pendingCount item${if (pendingCount != 1) "s" else ""} in offline queue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                FeedbackManager.flushPendingFeedback(context)
                                val prefs = context.getSharedPreferences("neurocomet_feedback", Context.MODE_PRIVATE)
                                pendingCount = prefs.getStringSet("pending_feedback", emptySet())?.size ?: 0
                                Toast.makeText(context, "Flush complete – $pendingCount remaining", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CloudSync, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Flush Queue", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = {
                            val prefs = context.getSharedPreferences("neurocomet_feedback", Context.MODE_PRIVATE)
                            prefs.edit().remove("pending_feedback").apply()
                            pendingCount = 0
                            Toast.makeText(context, "Offline queue cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.DeleteSweep, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear Queue", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        val remaining = FeedbackManager.getRemainingSubmissions(context)
        Text(
            "Rate limit: $remaining / 5 submissions remaining today${if (options.bypassFeedbackRateLimit) " (bypassed)" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Text("Quick Navigate", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = onNavigateFeedbackHub,
                label = { Text("Hub", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Filled.Feedback, null, Modifier.size(16.dp)) }
            )
            AssistChip(
                onClick = onNavigateBugReport,
                label = { Text("Bug", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Filled.BugReport, null, Modifier.size(16.dp)) }
            )
            AssistChip(
                onClick = onNavigateFeatureRequest,
                label = { Text("Feature", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Filled.Lightbulb, null, Modifier.size(16.dp)) }
            )
            AssistChip(
                onClick = onNavigateGeneralFeedback,
                label = { Text("General", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Filled.Chat, null, Modifier.size(16.dp)) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// STRESS TESTING
// ═══════════════════════════════════════════════════════════════

@Composable
fun StressTestingSection(feedViewModel: FeedViewModel?, context: Context) {
    val scope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(false) }
    val results = remember { mutableStateListOf<StressTestResult>() }
    var currentTestName by remember { mutableStateOf<String?>(null) }

    DevSectionCard(title = "System Stress Testing", icon = Icons.Filled.Speed) {
        Text(
            "Execute automated stress tests to verify application stability under various conditions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                isRunning = true
                results.clear()
                scope.launch {
                    try {
                        val tests = listOf<suspend () -> StressTestResult>(
                            { StressTester.runUIResponsivenessTest() },
                            { StressTester.runMemoryPressureTest(context) },
                            { StressTester.runRapidNavigationTest() },
                            { StressTester.runDataLoadingStressTest(feedViewModel) },
                            { StressTester.runThemeSwitchingTest() },
                            { StressTester.runConcurrentOperationsTest() },
                            { StressTester.runStorageIOTest(context) },
                            { StressTester.runNetworkSimulationTest() },
                            { StressTester.runNotificationChannelTest(context) },
                            { StressTester.runListScrollStressTest() },
                            { StressTester.runStateManagementTest() },
                            { StressTester.runJsonParsingTest() },
                            { StressTester.runStringOperationsTest() },
                            { StressTester.runDateTimeOperationsTest() },
                            { StressTester.runCollectionOperationsTest() },
                            { StressTester.runSecurityOperationsTest() },
                            { StressTester.runFileSystemTest(context) },
                            { StressTester.runExceptionHandlingTest() },
                            { StressTester.runParentalControlsTest(context) },
                            { StressTester.runTotpLifecycleTest(context) },
                            { StressTester.runTotpWindowToleranceTest(context) },
                            { StressTester.runBackupCodesTest(context) },
                            { StressTester.runEmailVerificationFlowTest(context) },
                            { StressTester.runFido2CredentialLifecycleTest(context) },
                            { StressTester.runBiometricStatusTest(context) },
                            { StressTester.runBiometricToggleTest(context) },
                            { StressTester.runInputValidationTest() },
                            { StressTester.runSecurityUtilsRoundTripTest() },
                            { StressTester.runDeviceAuthorityTest(context) },
                            { StressTester.runSessionStateTest() },
                            { StressTester.runAccountLifecycleParsingTest() },
                            { StressTester.runAuthEnumCoverageTest() },
                            { StressTester.runConcurrentAuthOpsTest(context) },
                            { StressTester.runTotpSecretEdgeCasesTest() }
                        )

                        tests.forEach { test ->
                            // Use a placeholder or small delay to show progress
                            delay(50)
                            val result = test()
                            results.add(result)
                            currentTestName = result.testName
                        }
                    } finally {
                        isRunning = false
                        currentTestName = null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Running: ${currentTestName ?: "..."}")
            } else {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Run Comprehensive Suite")
            }
        }

        if (results.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            val passedCount = results.count { it.passed }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Results: $passedCount / ${results.size} Passed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (passedCount == results.size) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    results.forEach { result ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    if (result.passed) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (result.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(result.testName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${result.duration}ms | ${result.details}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (result.errorMessage != null) {
                            Text(
                                "Error: ${result.errorMessage}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


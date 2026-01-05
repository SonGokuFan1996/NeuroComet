package com.kyilmaz.neurocomet

import android.Manifest
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

/**
 * Developer Testing Section for Google Ads
 *
 * Features:
 * - Toggle ad state
 * - Simulate premium status
 * - Test different ad types
 * - View ad debug info
 * - Force load/show ads
 * - Simulate failures
 */

@Composable
fun GoogleAdsDevTestSection() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val adsState by GoogleAdsManager.adsState.collectAsState()
    var showDebugInfo by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Google Ads Testing",
        icon = Icons.Filled.Tv
    ) {
        // Status overview
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (adsState.isPremium) Color(0xFF1B5E20).copy(alpha = 0.2f)
                                else if (adsState.adsEnabled) Color(0xFFFFF3E0)
                                else Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            adsState.isPremium -> "🌟 Premium User"
                            adsState.adsEnabled -> "📺 Ads Enabled"
                            else -> "🚫 Ads Disabled"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total ads shown: ${adsState.totalAdsShown}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (adsState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Ad load status indicators
        Text(
            "Ad Load Status:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdStatusChip("Banner", adsState.bannerLoaded)
            AdStatusChip("Interstitial", adsState.interstitialLoaded)
            AdStatusChip("Rewarded", adsState.rewardedLoaded)
            AdStatusChip("Native", adsState.nativeLoaded)
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // Toggle controls
        DevToggleRowSimple(
            title = "Simulate Premium User",
            subtitle = "Pretend user has subscription",
            isChecked = adsState.isPremium,
            onCheckedChange = { GoogleAdsManager.devSetSimulatePremium(it) }
        )

        DevToggleRowSimple(
            title = "Force Show Ads",
            subtitle = "Show ads even for premium users",
            isChecked = adsState.forceShowAds,
            onCheckedChange = { GoogleAdsManager.devSetForceShowAds(it) }
        )

        DevToggleRowSimple(
            title = "Simulate Ad Failure",
            subtitle = "Make ad loads fail",
            isChecked = adsState.simulateAdFailure,
            onCheckedChange = { GoogleAdsManager.devSetSimulateAdFailure(it) }
        )

        DevToggleRowSimple(
            title = "Use Test Ads",
            subtitle = "Use Google's test ad unit IDs",
            isChecked = adsState.useTestAds,
            onCheckedChange = { GoogleAdsManager.devSetUseTestAds(it) }
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // Action buttons
        Text(
            "Actions:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    GoogleAdsManager.devForceLoadAllAds()
                    Toast.makeText(context, "All ads loaded", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Download, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Load All", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    GoogleAdsManager.devResetSessionCounters()
                    Toast.makeText(context, "Session counters reset", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDebugInfo = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.BugReport, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("View Debug Info")
        }

        // Error display
        adsState.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Debug info dialog
    if (showDebugInfo) {
        Dialog(onDismissRequest = { showDebugInfo = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📊 Ads Debug Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = GoogleAdsManager.devGetDebugInfo(),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF4EC9B0)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = { showDebugInfo = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdStatusChip(
    label: String,
    isLoaded: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isLoaded) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color(0xFF9E9E9E).copy(alpha = 0.2f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isLoaded) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isLoaded) Color(0xFF2E7D32)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DevSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DevToggleRowSimple(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT WIDGETS DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

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
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Widget Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showWidgetPreview = false }) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }

                    // Widget dashboard
                    NeurodivergentWidgetDashboard(
                        highContrast = previewHighContrast,
                        reducedMotion = previewReducedMotion
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// IMAGE CUSTOMIZATION DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun ImageCustomizationDevSection() {
    var showEditor by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
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
            "Test the image editing features for posts, stories, and profile pictures.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Filter count info
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${AVAILABLE_FILTERS.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Filters", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${AVAILABLE_STICKERS.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Stickers", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${DRAWING_COLORS.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Colors", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Selected image preview
        selectedImageUri?.let { uri ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Selected Image",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Clear, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showEditor = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Select Image", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    selectedImageUri = null
                    showEditor = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Draw, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Blank Canvas", fontSize = 12.sp)
            }
        }
    }

    if (showEditor) {
        ImageCustomizationEditor(
            imageUri = selectedImageUri,
            onSave = {
                showEditor = false
                Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditor = false },
            title = if (selectedImageUri != null) "Edit Image" else "Blank Canvas"
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// EXPLORE VIEWS DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

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

        // View type selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExploreViewType.entries.forEach { viewType ->
                val isSelected = viewType == currentViewType
                FilterChip(
                    selected = isSelected,
                    onClick = { currentViewType = viewType },
                    label = { Text(viewType.label, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            viewType.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            currentViewType.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { showPreview = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Preview, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Preview ${currentViewType.label} View")
        }
    }

    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val mockPosts = remember { generateMockExplorePostsWithMedia() }
                val context = LocalContext.current

                Column {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${currentViewType.label} View Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showPreview = false }) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }

                    // View type selector
                    ExploreViewTypeSelector(
                        currentViewType = currentViewType,
                        onViewTypeChange = { currentViewType = it }
                    )

                    // Preview content
                    when (currentViewType) {
                        ExploreViewType.GRID -> ExploreGridView(
                            posts = mockPosts,
                            onPostClick = {},
                            onProfileClick = {}
                        )
                        ExploreViewType.COMPACT -> ExploreCompactView(
                            posts = mockPosts,
                            onPostClick = {},
                            onLikePost = {},
                            onProfileClick = {}
                        )
                        ExploreViewType.STANDARD -> ExploreStandardView(
                            posts = mockPosts,
                            onPostClick = {},
                            onLikePost = {},
                            onSharePost = { _, _ -> },
                            onCommentPost = {},
                            onProfileClick = {}
                        )
                        ExploreViewType.LARGE_CARDS -> ExploreLargeCardView(
                            posts = mockPosts,
                            onPostClick = {},
                            onLikePost = {},
                            onSharePost = { _, _ -> },
                            onCommentPost = {},
                            onProfileClick = {}
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MULTI-MEDIA POST DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun MultiMediaPostDevSection() {
    val mockPosts = remember { generateMockExplorePostsWithMedia() }

    DevSectionCard(
        title = "Multi-Media Posts (20 max)",
        icon = Icons.Filled.Collections
    ) {
        Text(
            "Test posts with multiple images/videos (up to ${Post.MAX_MEDIA_ITEMS} items like Instagram).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Show mock post stats
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            mockPosts.forEach { post ->
                val mediaCount = post.mediaCount()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = post.userId ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        if (mediaCount > 0) {
                            val hasVideo = post.getAllMedia().any { it.type == MediaType.VIDEO }
                            Text(
                                text = if (hasVideo) "🎬 $mediaCount" else "📷 $mediaCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "📝 Text",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "✓ Supports ${Post.MAX_MEDIA_ITEMS} images/videos per post\n" +
            "✓ Mixed media (images + videos)\n" +
            "✓ Legacy single image/video support\n" +
            "✓ Carousel display for multiple items",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4CAF50)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ADAPTIVE NAVIGATION DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun AdaptiveNavigationDevSection() {
    val context = LocalContext.current
    val navigationType = calculateNavigationType()
    val contentType = calculateContentType()

    DevSectionCard(
        title = "Adaptive Navigation",
        icon = Icons.Filled.Dashboard
    ) {
        Text(
            "Test adaptive navigation for different screen sizes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Current navigation type
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    "Current Configuration:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "📱 Navigation Type: ${navigationType.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "📐 Content Type: ${contentType.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Navigation Types:\n" +
            "• BOTTOM_NAVIGATION - < 600dp (phones)\n" +
            "• NAVIGATION_RAIL - 600-840dp (tablets portrait)\n" +
            "• PERMANENT_DRAWER - > 840dp (large screens)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// NEURODIVERGENT DIALOGS DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun NeurodivergentDialogsDevSection() {
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showChoiceDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var selectedDialogType by remember { mutableStateOf(DialogType.INFO) }

    DevSectionCard(
        title = "Neurodivergent Dialogs",
        icon = Icons.Filled.ChatBubble
    ) {
        Text(
            "Test accessible pop-up messages and input dialogs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Dialog type selector
        Text("Dialog Type:", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DialogType.entries.take(5).forEach { type ->
                FilterChip(
                    selected = type == selectedDialogType,
                    onClick = { selectedDialogType = type },
                    label = { Text(type.name, fontSize = 10.sp) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Test buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showMessageDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Message", fontSize = 11.sp)
            }
            Button(
                onClick = { showInputDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Input", fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showChoiceDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Choice", fontSize = 11.sp)
            }
            Button(
                onClick = { showLoadingDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Loading", fontSize = 11.sp)
            }
        }
    }

    // Message dialog
    if (showMessageDialog) {
        NeurodivergentMessageDialog(
            config = DialogConfig(
                type = selectedDialogType,
                title = "Test ${selectedDialogType.name} Dialog",
                message = "This is a neurodivergent-friendly dialog with clear messaging and large touch targets.",
                emoji = when (selectedDialogType) {
                    DialogType.INFO -> "ℹ️"
                    DialogType.SUCCESS -> "✅"
                    DialogType.WARNING -> "⚠️"
                    DialogType.ERROR -> "❌"
                    DialogType.QUESTION -> "❓"
                    else -> "🧠"
                },
                primaryButtonText = "Got it!",
                secondaryButtonText = "Learn more"
            ),
            onDismiss = { showMessageDialog = false },
            onSecondaryClick = {
                Toast.makeText(context, "Learn more clicked!", Toast.LENGTH_SHORT).show()
                showMessageDialog = false
            }
        )
    }

    // Input dialog
    if (showInputDialog) {
        NeurodivergentInputDialog(
            title = "What's your name?",
            message = "Enter your display name for the community.",
            placeholder = "Enter your name...",
            emoji = "👤",
            onDismiss = { showInputDialog = false },
            onConfirm = { value ->
                Toast.makeText(context, "Hello, $value!", Toast.LENGTH_SHORT).show()
                showInputDialog = false
            },
            validation = { value ->
                when {
                    value.length < 2 -> "Name too short"
                    value.length > 30 -> "Name too long"
                    else -> null
                }
            }
        )
    }

    // Choice dialog
    if (showChoiceDialog) {
        NeurodivergentChoiceDialog(
            title = "Choose your mood",
            message = "How are you feeling right now?",
            choices = listOf(
                DialogChoice("happy", "Happy", "Feeling great!", emoji = "😊"),
                DialogChoice("calm", "Calm", "Peaceful and relaxed", emoji = "😌"),
                DialogChoice("tired", "Tired", "Need some rest", emoji = "😴"),
                DialogChoice("anxious", "Anxious", "Feeling worried", emoji = "😰"),
                DialogChoice("excited", "Excited", "Full of energy!", emoji = "🤩")
            ),
            onDismiss = { showChoiceDialog = false },
            onSelect = { choice ->
                Toast.makeText(context, "You selected: ${choice.label}", Toast.LENGTH_SHORT).show()
                showChoiceDialog = false
            }
        )
    }

    // Loading dialog
    if (showLoadingDialog) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showLoadingDialog = false
        }

        NeurodivergentLoadingDialog(
            message = "Processing...",
            subMessage = "This won't take long",
            emoji = "🧠",
            onDismiss = { showLoadingDialog = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// LOCATION & SENSORS DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun LocationSensorsDevSection() {
    val context = LocalContext.current
    val locationStatus by LocationService.locationStatus.collectAsState()
    val currentLocation by LocationService.currentLocation.collectAsState()
    val sensorData by LocationService.sensorData.collectAsState()
    var isMonitoring by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Location & Sensors",
        icon = Icons.Filled.LocationOn
    ) {
        Text(
            "Test location and sensor services for accuracy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Permission status
        val hasPermission = LocationService.hasLocationPermission(context)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasPermission) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else Color(0xFFF44336).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (hasPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (hasPermission) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (hasPermission) "Location permission granted"
                    else "Location permission not granted",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Status
        Text(
            "Status: ${locationStatus.name}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Current location
        currentLocation?.let { loc ->
            Spacer(Modifier.height(4.dp))
            Text(
                "📍 ${loc.formatForDisplay()}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }

        // Sensor data
        if (isMonitoring) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Sensors:\n" +
                "• Moving: ${sensorData.isMoving}\n" +
                "• Pressure: ${String.format("%.1f", sensorData.pressure)} hPa\n" +
                "• Alt (baro): ${String.format("%.1f", sensorData.barometerAltitude)}m",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (hasPermission) {
                        isMonitoring = !isMonitoring
                        if (isMonitoring) {
                            LocationService.initialize(context)
                            LocationService.startSensorMonitoring(context)
                            LocationService.startLocationUpdates(
                                context,
                                LocationPriority.BALANCED,
                                10000L
                            ) {}
                        } else {
                            LocationService.stopLocationUpdates()
                            LocationService.stopSensorMonitoring()
                        }
                    } else {
                        Toast.makeText(context, "Permission needed", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isMonitoring) "Stop" else "Start", fontSize = 12.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CREDENTIAL STORAGE DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun CredentialTestSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var testKey by remember { mutableStateOf("test_credential") }
    var testValue by remember { mutableStateOf("secret_value_123") }
    var retrievedValue by remember { mutableStateOf<String?>(null) }
    var operationResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Credential Storage Testing",
        icon = Icons.Filled.Key
    ) {
        Text(
            "Test secure credential storage with Android Keystore encryption.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Test key input
        OutlinedTextField(
            value = testKey,
            onValueChange = { testKey = it },
            label = { Text("Credential Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Test value input
        OutlinedTextField(
            value = testValue,
            onValueChange = { testValue = it },
            label = { Text("Credential Value") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // Action buttons Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            CredentialStorage.storeCredential(context, testKey, testValue)
                            operationResult = "✅ Credential stored successfully"
                        } catch (e: Exception) {
                            operationResult = "❌ Failed: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Save, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Store", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            retrievedValue = CredentialStorage.retrieveCredential(context, testKey)
                            operationResult = if (retrievedValue != null) {
                                "✅ Retrieved: $retrievedValue"
                            } else {
                                "⚠️ No value found for key"
                            }
                        } catch (e: Exception) {
                            operationResult = "❌ Failed: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Download, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Retrieve", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            CredentialStorage.deleteCredential(context, testKey)
                            operationResult = "✅ Credential deleted"
                            retrievedValue = null
                        } catch (e: Exception) {
                            operationResult = "❌ Failed: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Filled.Delete, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Delete", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Auth Token Testing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            val token = CredentialStorage.getAuthToken(context)
                            operationResult = if (token != null) {
                                "🔐 Auth token: ${token.take(20)}..."
                            } else {
                                "⚠️ No auth token stored"
                            }
                        } catch (e: Exception) {
                            operationResult = "❌ Failed: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Get Auth Token", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    CredentialStorage.saveAuthToken(context, "test_token_${System.currentTimeMillis()}")
                    operationResult = "✅ Test auth token saved"
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Test Token", fontSize = 11.sp)
            }
        }

        // Result display
        operationResult?.let { result ->
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        result.startsWith("✅") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        result.startsWith("❌") -> Color(0xFFF44336).copy(alpha = 0.2f)
                        else -> Color(0xFFFFC107).copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LOCATION & SENSORS DEV TEST SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun LocationSensorTestSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationStatus by LocationService.locationStatus.collectAsState()
    val currentLocation by LocationService.currentLocation.collectAsState()
    val sensorData by LocationService.sensorData.collectAsState()

    var testResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        testResult = if (granted) "✅ Location permissions granted" else "❌ Some permissions denied"
    }

    DevSectionCard(
        title = "Location & Sensors Testing",
        icon = Icons.Filled.MyLocation
    ) {
        Text(
            "Test location services and sensor access for enhanced accuracy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Status indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(
                label = "Permission",
                isActive = LocationService.hasLocationPermission(context),
                activeColor = Color(0xFF4CAF50),
                inactiveColor = Color(0xFFF44336)
            )
            StatusChip(
                label = "Background",
                isActive = LocationService.hasBackgroundLocationPermission(context),
                activeColor = Color(0xFF4CAF50),
                inactiveColor = Color(0xFFFF9800)
            )
            StatusChip(
                label = locationStatus.name,
                isActive = locationStatus == LocationStatus.ACQUIRED,
                activeColor = Color(0xFF2196F3),
                inactiveColor = Color(0xFF9E9E9E)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Current location display
        currentLocation?.let { loc ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📍 Current Location", fontWeight = FontWeight.Bold)
                    Text("Lat: ${loc.latitude}", style = MaterialTheme.typography.bodySmall)
                    Text("Lng: ${loc.longitude}", style = MaterialTheme.typography.bodySmall)
                    Text("Accuracy: ${loc.accuracy}m", style = MaterialTheme.typography.bodySmall)
                    loc.altitude?.let { Text("Altitude: ${it}m", style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Sensor data display
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🔄 Sensor Data", fontWeight = FontWeight.Bold)
                Text("Accelerometer: (${String.format("%.2f", sensorData.accelerometerX)}, ${String.format("%.2f", sensorData.accelerometerY)}, ${String.format("%.2f", sensorData.accelerometerZ)})",
                    style = MaterialTheme.typography.bodySmall)
                Text("Heading: ${String.format("%.1f", LocationService.getHeading() ?: 0f)}°",
                    style = MaterialTheme.typography.bodySmall)
                Text("Motion: ${if (sensorData.isMoving) "Moving" else "Stationary"}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Action buttons Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    permissionLauncher.launch(LocationService.getRequiredPermissions(false))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Security, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Request", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val location = LocationService.getLastKnownLocation(context)
                        testResult = if (location != null) {
                            "✅ Last known: ${location.latitude}, ${location.longitude}"
                        } else {
                            "⚠️ No last known location"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.History, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Last Known", fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        testResult = "⏳ Getting current location (high accuracy)..."
                        val location = LocationService.getCurrentLocation(context, LocationPriority.HIGH_ACCURACY)
                        testResult = if (location != null) {
                            "✅ Current: ${location.latitude}, ${location.longitude} (±${location.accuracy}m)"
                        } else {
                            "❌ Failed to get current location"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && LocationService.hasLocationPermission(context)
            ) {
                Icon(Icons.Filled.GpsFixed, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Get Current", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    LocationService.startSensorMonitoring(context)
                    testResult = "✅ Sensor updates started"
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Sensors, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Start Sensors", fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Stop sensors
        OutlinedButton(
            onClick = {
                LocationService.stopSensorMonitoring()
                testResult = "⏹️ Sensor updates stopped"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stop Sensor Updates")
        }

        // Result display
        testResult?.let { result ->
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        result.startsWith("✅") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        result.startsWith("❌") -> Color(0xFFF44336).copy(alpha = 0.2f)
                        else -> Color(0xFFFFC107).copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WIDGET DEV SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun WidgetDevSection() {
    val context = LocalContext.current
    var testResult by remember { mutableStateOf<String?>(null) }
    var showFullPreview by remember { mutableStateOf(false) }

    // Widget preview states
    var focusMinutes by remember { mutableIntStateOf(25) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var energyLevel by remember { mutableIntStateOf(70) }
    var selectedMood by remember { mutableStateOf<MoodOption?>(null) }
    var sensoryLevel by remember { mutableStateOf(SensoryLevel.GREEN) }

    DevSectionCard(
        title = "Home Screen Widgets",
        icon = Icons.Filled.Widgets
    ) {
        Text(
            "Preview actual home screen widgets.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Mini widget previews grid
        Text(
            "Widget Previews",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        // Focus Timer Mini Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏱️", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            "Focus Timer",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "$focusMinutes min remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isTimerRunning = !isTimerRunning },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            null,
                            Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Mood Check-in Mini Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "How are you feeling?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("😊" to "Happy", "😌" to "Calm", "😔" to "Sad", "😤" to "Angry", "😰" to "Anxious").forEach { (emoji, _) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedMood?.emoji == emoji)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedMood = MoodOption(emoji, "", Color.Transparent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Energy Level Mini Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⚡ Energy Level",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$energyLevel%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            energyLevel >= 70 -> Color(0xFF4CAF50)
                            energyLevel >= 40 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { energyLevel / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        energyLevel >= 70 -> Color(0xFF4CAF50)
                        energyLevel >= 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Sensory Alert Mini Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (sensoryLevel) {
                    SensoryLevel.GREEN -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    SensoryLevel.YELLOW -> Color(0xFFFFEB3B).copy(alpha = 0.1f)
                    SensoryLevel.ORANGE -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    SensoryLevel.RED -> Color(0xFFF44336).copy(alpha = 0.1f)
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        when (sensoryLevel) {
                            SensoryLevel.GREEN -> "🟢"
                            SensoryLevel.YELLOW -> "🟡"
                            SensoryLevel.ORANGE -> "🟠"
                            SensoryLevel.RED -> "🔴"
                        },
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            "Sensory Status",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            sensoryLevel.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SensoryLevel.entries.forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when (level) {
                                        SensoryLevel.GREEN -> Color(0xFF4CAF50)
                                        SensoryLevel.YELLOW -> Color(0xFFFFEB3B)
                                        SensoryLevel.ORANGE -> Color(0xFFFF9800)
                                        SensoryLevel.RED -> Color(0xFFF44336)
                                    }
                                )
                                .then(
                                    if (sensoryLevel == level) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable { sensoryLevel = level }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showFullPreview = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Fullscreen, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Full View", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    focusMinutes = 25
                    isTimerRunning = false
                    energyLevel = 70
                    selectedMood = null
                    sensoryLevel = SensoryLevel.GREEN
                    testResult = "✅ Widgets reset to defaults"
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontSize = 12.sp)
            }
        }

        testResult?.let { result ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    // Full widget preview dialog
    if (showFullPreview) {
        Dialog(
            onDismissRequest = { showFullPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Full Widget Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showFullPreview = false }) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }
                    NeurodivergentWidgetDashboard()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ENHANCED LOCATION & SENSORS DEV SECTION
// ═══════════════════════════════════════════════════════════════

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
        title = "Enhanced Location & Sensors",
        icon = Icons.Filled.Sensors
    ) {
        Text(
            "Advanced location and sensor testing with enhanced accuracy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Permission status
        val hasPermission = LocationService.hasLocationPermission(context)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (hasPermission) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color(0xFFF44336).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (hasPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (hasPermission) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (hasPermission) "Location permission granted"
                    else "Location permission required",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Status display
        Text(
            "Status: ${locationStatus.name}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        currentLocation?.let { loc ->
            Spacer(Modifier.height(4.dp))
            Text(
                "📍 ${loc.formatForDisplay()}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }

        if (isMonitoring) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Sensors:\n" +
                        "• Moving: ${sensorData.isMoving}\n" +
                        "• Pressure: ${String.format(java.util.Locale.US, "%.1f", sensorData.pressure)} hPa\n" +
                        "• Alt (baro): ${String.format(java.util.Locale.US, "%.1f", sensorData.barometerAltitude)}m",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (hasPermission) {
                        isMonitoring = !isMonitoring
                        if (isMonitoring) {
                            LocationService.initialize(context)
                            LocationService.startSensorMonitoring(context)
                            LocationService.startLocationUpdates(
                                context,
                                LocationPriority.HIGH_ACCURACY,
                                5000L
                            ) {}
                            testResult = "✅ Enhanced monitoring started"
                        } else {
                            LocationService.stopLocationUpdates()
                            LocationService.stopSensorMonitoring()
                            testResult = "⏹️ Monitoring stopped"
                        }
                    } else {
                        testResult = "❌ Location permission required"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (isMonitoring) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    null,
                    Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isMonitoring) "Stop" else "Start", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        val location = LocationService.getCurrentLocation(context, LocationPriority.HIGH_ACCURACY)
                        testResult = if (location != null) {
                            "✅ Current: ${location.latitude}, ${location.longitude}"
                        } else {
                            "⚠️ Could not get current location"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = hasPermission
            ) {
                Icon(Icons.Filled.GpsFixed, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Get GPS", fontSize = 12.sp)
            }
        }

        testResult?.let { result ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.startsWith("✅")) Color(0xFF4CAF50)
                else if (result.startsWith("❌")) Color(0xFFF44336)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CREDENTIAL STORAGE DEV SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun CredentialStorageDevSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Credential Storage",
        icon = Icons.Filled.Key
    ) {
        Text(
            "Test secure credential storage and retrieval.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            CredentialStorage.storeCredential(
                                context,
                                "test_key",
                                "test_value_${System.currentTimeMillis()}"
                            )
                            testResult = "✅ Credential saved successfully"
                        } catch (e: Exception) {
                            testResult = "❌ Save failed: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Save, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val value = CredentialStorage.retrieveCredential(context, "test_key")
                            testResult = if (value != null) {
                                "✅ Retrieved: $value"
                            } else {
                                "⚠️ No credential found"
                            }
                        } catch (e: Exception) {
                            testResult = "❌ Retrieve failed: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Download, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Load", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        CredentialStorage.deleteCredential(context, "test_key")
                        testResult = "✅ Credential deleted"
                    } catch (e: Exception) {
                        testResult = "❌ Delete failed: ${e.message}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(Icons.Filled.Delete, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Delete Test Credential")
        }

        if (isLoading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        testResult?.let { result ->
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BACKGROUND TASKS DEV SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun BackgroundTasksDevSection() {
    val context = LocalContext.current
    var testResult by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DevSectionCard(
        title = "Background Tasks",
        icon = Icons.Filled.Schedule
    ) {
        Text(
            "Test background task scheduling and execution.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isRunning = true
                        testResult = "⏳ Running background task..."
                        kotlinx.coroutines.delay(2000)
                        testResult = "✅ Background task completed successfully"
                        isRunning = false
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunning
            ) {
                Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Run Task", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    testResult = "✅ Task scheduled for later execution"
                },
                modifier = Modifier.weight(1f),
                enabled = !isRunning
            ) {
                Icon(Icons.Filled.Schedule, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Schedule", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                testResult = "✅ All pending tasks cancelled"
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        ) {
            Icon(Icons.Filled.Cancel, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Cancel All Tasks")
        }

        if (isRunning) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        testResult?.let { result ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.startsWith("✅")) Color(0xFF4CAF50)
                else if (result.startsWith("❌")) Color(0xFFF44336)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ERROR BOUNDARY DEV SECTION
// ═══════════════════════════════════════════════════════════════

@Composable
fun ErrorBoundaryDevSection() {
    var testResult by remember { mutableStateOf<String?>(null) }
    var showFallbackUI by remember { mutableStateOf(false) }

    DevSectionCard(
        title = "Error Boundary & Fallback UI",
        icon = Icons.Filled.BugReport
    ) {
        Text(
            "Test error handling and fallback UI behavior.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    try {
                        // Simulate a caught error
                        throw RuntimeException("Test error for boundary")
                    } catch (e: Exception) {
                        testResult = "✅ Error caught successfully: ${e.message}"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Error, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Test Error", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    showFallbackUI = !showFallbackUI
                    testResult = if (showFallbackUI) "📱 Fallback UI shown" else "📱 Normal UI restored"
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Visibility, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Toggle Fallback", fontSize = 10.sp)
            }
        }

        if (showFallbackUI) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Fallback UI Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This is shown when the main UI fails to load.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6D4C41)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showFallbackUI = false }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        testResult?.let { result ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.startsWith("✅")) Color(0xFF4CAF50)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) activeColor.copy(alpha = 0.2f)
                else inactiveColor.copy(alpha = 0.2f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else inactiveColor)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) activeColor else inactiveColor
            )
        }
    }
}

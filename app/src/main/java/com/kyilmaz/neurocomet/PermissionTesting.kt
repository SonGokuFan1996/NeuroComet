package com.kyilmaz.neurocomet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Permission Testing UI for Developer Settings
 *
 * Features:
 * - View all app permissions and their status
 * - Request permissions individually
 * - Open system settings for denied permissions
 * - Check "Don't ask again" state
 * - Neurodivergent-friendly design
 */

// ═══════════════════════════════════════════════════════════════
// PERMISSION DATA
// ═══════════════════════════════════════════════════════════════

data class PermissionInfo(
    val permission: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: PermissionCategory,
    val minSdk: Int = 1
)

enum class PermissionCategory {
    LOCATION,
    CAMERA_MICROPHONE,
    STORAGE,
    CONTACTS,
    NOTIFICATIONS,
    OTHER
}

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NEVER_ASKED,
    PERMANENTLY_DENIED
}

val APP_PERMISSIONS = listOf(
    // Location
    PermissionInfo(
        Manifest.permission.ACCESS_FINE_LOCATION,
        "Fine Location",
        "Precise GPS location for location sharing",
        Icons.Filled.MyLocation,
        PermissionCategory.LOCATION
    ),
    PermissionInfo(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        "Coarse Location",
        "Approximate location for nearby features",
        Icons.Filled.LocationOn,
        PermissionCategory.LOCATION
    ),
    PermissionInfo(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        "Background Location",
        "Location access when app is in background",
        Icons.Filled.LocationSearching,
        PermissionCategory.LOCATION,
        minSdk = Build.VERSION_CODES.Q
    ),

    // Camera & Microphone
    PermissionInfo(
        Manifest.permission.CAMERA,
        "Camera",
        "Take photos and videos for posts and calls",
        Icons.Filled.CameraAlt,
        PermissionCategory.CAMERA_MICROPHONE
    ),
    PermissionInfo(
        Manifest.permission.RECORD_AUDIO,
        "Microphone",
        "Record audio for voice messages and calls",
        Icons.Filled.Mic,
        PermissionCategory.CAMERA_MICROPHONE
    ),

    // Storage
    PermissionInfo(
        Manifest.permission.READ_MEDIA_IMAGES,
        "Read Images",
        "Access photos from gallery",
        Icons.Filled.Image,
        PermissionCategory.STORAGE,
        minSdk = Build.VERSION_CODES.TIRAMISU
    ),
    PermissionInfo(
        Manifest.permission.READ_MEDIA_VIDEO,
        "Read Videos",
        "Access videos from gallery",
        Icons.Filled.VideoLibrary,
        PermissionCategory.STORAGE,
        minSdk = Build.VERSION_CODES.TIRAMISU
    ),
    PermissionInfo(
        Manifest.permission.READ_MEDIA_AUDIO,
        "Read Audio",
        "Access audio files",
        Icons.Filled.Audiotrack,
        PermissionCategory.STORAGE,
        minSdk = Build.VERSION_CODES.TIRAMISU
    ),

    // Contacts
    PermissionInfo(
        Manifest.permission.READ_CONTACTS,
        "Read Contacts",
        "Find friends in your contacts",
        Icons.Filled.Contacts,
        PermissionCategory.CONTACTS
    ),

    // Notifications
    PermissionInfo(
        Manifest.permission.POST_NOTIFICATIONS,
        "Notifications",
        "Show notifications for messages and updates",
        Icons.Filled.Notifications,
        PermissionCategory.NOTIFICATIONS,
        minSdk = Build.VERSION_CODES.TIRAMISU
    ),

    // Other
    PermissionInfo(
        Manifest.permission.VIBRATE,
        "Vibrate",
        "Haptic feedback for interactions",
        Icons.Filled.Vibration,
        PermissionCategory.OTHER
    ),
    PermissionInfo(
        Manifest.permission.BLUETOOTH_CONNECT,
        "Bluetooth Connect",
        "Connect to Bluetooth devices for calls",
        Icons.Filled.Bluetooth,
        PermissionCategory.OTHER,
        minSdk = Build.VERSION_CODES.S
    )
)

// ═══════════════════════════════════════════════════════════════
// PERMISSION TESTING SECTION (for Settings)
// ═══════════════════════════════════════════════════════════════

@Composable
fun PermissionTestingSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionStates by remember { mutableStateOf(checkAllPermissions(context)) }
    var expandedCategory by remember { mutableStateOf<PermissionCategory?>(null) }
    var showBackgroundLocationDialog by remember { mutableStateOf(false) }

    // Refresh permission states
    fun refreshPermissions() {
        permissionStates = checkAllPermissions(context)
    }

    // Check if foreground location is granted
    fun hasForegroundLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Permission launcher for regular permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        refreshPermissions()
        val granted = results.count { it.value }
        val denied = results.count { !it.value }
        if (granted > 0) {
            Toast.makeText(context, "$granted permission(s) granted!", Toast.LENGTH_SHORT).show()
        }
        if (denied > 0) {
            Toast.makeText(context, "$denied permission(s) denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Separate launcher for background location (Android 11+ requirement)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        refreshPermissions()
        if (granted) {
            Toast.makeText(context, "Background location granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Background location denied. You may need to enable it in Settings.", Toast.LENGTH_LONG).show()
        }
    }

    // Helper to request background location with proper flow
    fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasForegroundLocationPermission()) {
                // Must have foreground location first
                Toast.makeText(
                    context,
                    "Please grant foreground location permission first",
                    Toast.LENGTH_LONG
                ).show()
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+: Show dialog explaining background location, then open settings
                showBackgroundLocationDialog = true
            } else {
                // Android 10: Can request directly
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    // Background location dialog for Android 11+
    if (showBackgroundLocationDialog) {
        AlertDialog(
            onDismissRequest = { showBackgroundLocationDialog = false },
            title = { Text("Background Location Access") },
            text = {
                Text(
                    "Android requires you to grant background location in Settings.\n\n" +
                    "Please select \"Allow all the time\" in the location permission settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackgroundLocationDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackgroundLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Permission Testing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Check and manage app permissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { refreshPermissions() }) {
                    Icon(Icons.Filled.Refresh, "Refresh")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Summary
            val granted = permissionStates.count { it.value == PermissionStatus.GRANTED }
            val total = permissionStates.size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PermissionStatBadge(
                    count = granted,
                    label = "Granted",
                    color = Color(0xFF4CAF50)
                )
                PermissionStatBadge(
                    count = total - granted,
                    label = "Not Granted",
                    color = Color(0xFFF44336)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // On Android 11+, exclude background location from batch request
                        val notGranted = APP_PERMISSIONS
                            .filter { Build.VERSION.SDK_INT >= it.minSdk }
                            .filter { permissionStates[it.permission] != PermissionStatus.GRANTED }
                            .filter {
                                // Exclude background location on Android 11+ (must be requested separately)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    it.permission != Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                } else {
                                    true
                                }
                            }
                            .map { it.permission }
                            .toTypedArray()

                        if (notGranted.isNotEmpty()) {
                            permissionLauncher.launch(notGranted)
                        } else {
                            Toast.makeText(context, "All permissions already granted!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Request All", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("App Settings", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Permission categories
            PermissionCategory.entries.forEach { category ->
                val categoryPermissions = APP_PERMISSIONS
                    .filter { it.category == category && Build.VERSION.SDK_INT >= it.minSdk }

                if (categoryPermissions.isNotEmpty()) {
                    PermissionCategorySection(
                        category = category,
                        permissions = categoryPermissions,
                        permissionStates = permissionStates,
                        isExpanded = expandedCategory == category,
                        onToggleExpand = {
                            expandedCategory = if (expandedCategory == category) null else category
                        },
                        onRequestPermission = { permission ->
                            // Special handling for background location on Android 11+
                            if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                requestBackgroundLocation()
                            } else {
                                permissionLauncher.launch(arrayOf(permission))
                            }
                        },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatBadge(
    count: Int,
    label: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PermissionCategorySection(
    category: PermissionCategory,
    permissions: List<PermissionInfo>,
    permissionStates: Map<String, PermissionStatus>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onRequestPermission: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val categoryIcon = when (category) {
        PermissionCategory.LOCATION -> Icons.Filled.LocationOn
        PermissionCategory.CAMERA_MICROPHONE -> Icons.Filled.CameraAlt
        PermissionCategory.STORAGE -> Icons.Filled.Folder
        PermissionCategory.CONTACTS -> Icons.Filled.Contacts
        PermissionCategory.NOTIFICATIONS -> Icons.Filled.Notifications
        PermissionCategory.OTHER -> Icons.Filled.Settings
    }

    val categoryName = when (category) {
        PermissionCategory.LOCATION -> "Location"
        PermissionCategory.CAMERA_MICROPHONE -> "Camera & Microphone"
        PermissionCategory.STORAGE -> "Storage"
        PermissionCategory.CONTACTS -> "Contacts"
        PermissionCategory.NOTIFICATIONS -> "Notifications"
        PermissionCategory.OTHER -> "Other"
    }

    val grantedCount = permissions.count { permissionStates[it.permission] == PermissionStatus.GRANTED }
    val allGranted = grantedCount == permissions.size

    Column {
        // Category header
        Surface(
            onClick = onToggleExpand,
            shape = RoundedCornerShape(8.dp),
            color = if (allGranted) Color(0xFF4CAF50).copy(alpha = 0.1f)
                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = null,
                    tint = if (allGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$grantedCount/${permissions.size} granted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }

        // Expanded permission list
        if (isExpanded) {
            Spacer(Modifier.height(8.dp))
            permissions.forEach { permissionInfo ->
                val status = permissionStates[permissionInfo.permission] ?: PermissionStatus.NEVER_ASKED
                PermissionItem(
                    permissionInfo = permissionInfo,
                    status = status,
                    onRequest = { onRequestPermission(permissionInfo.permission) },
                    onOpenSettings = onOpenSettings
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PermissionItem(
    permissionInfo: PermissionInfo,
    status: PermissionStatus,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            PermissionStatus.GRANTED -> Color(0xFF4CAF50)
            PermissionStatus.DENIED -> Color(0xFFFF9800)
            PermissionStatus.NEVER_ASKED -> Color(0xFF9E9E9E)
            PermissionStatus.PERMANENTLY_DENIED -> Color(0xFFF44336)
        },
        label = "statusColor"
    )

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(Modifier.width(12.dp))

            Icon(
                permissionInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    permissionInfo.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    permissionInfo.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (status) {
                PermissionStatus.GRANTED -> {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Granted",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
                PermissionStatus.PERMANENTLY_DENIED -> {
                    TextButton(onClick = onOpenSettings) {
                        Text("Settings", fontSize = 11.sp)
                    }
                }
                else -> {
                    TextButton(onClick = onRequest) {
                        Text("Request", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════

private fun checkAllPermissions(context: Context): Map<String, PermissionStatus> {
    return APP_PERMISSIONS
        .filter { Build.VERSION.SDK_INT >= it.minSdk }
        .associate { info ->
            info.permission to checkPermissionStatus(context, info.permission)
        }
}

private fun checkPermissionStatus(context: Context, permission: String): PermissionStatus {
    return when {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
            PermissionStatus.GRANTED
        }
        context is Activity && !ActivityCompat.shouldShowRequestPermissionRationale(context, permission) -> {
            // Could be never asked or permanently denied
            // We can check SharedPreferences to know if we've asked before
            val prefs = context.getSharedPreferences("permission_tracking", Context.MODE_PRIVATE)
            if (prefs.getBoolean("asked_$permission", false)) {
                PermissionStatus.PERMANENTLY_DENIED
            } else {
                PermissionStatus.NEVER_ASKED
            }
        }
        else -> {
            PermissionStatus.DENIED
        }
    }
}

/**
 * Track that we've asked for a permission (call after requesting)
 */
fun trackPermissionAsked(context: Context, permission: String) {
    context.getSharedPreferences("permission_tracking", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("asked_$permission", true)
        .apply()
}


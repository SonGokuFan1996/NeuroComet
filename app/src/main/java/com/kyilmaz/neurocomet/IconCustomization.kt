package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Detects the current default launcher app.
 * Returns the package name or null if unable to detect.
 */
fun detectCurrentLauncher(context: Context): String? {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo: ResolveInfo? = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        resolveInfo?.activityInfo?.packageName
    } catch (e: Exception) {
        null
    }
}

/**
 * Returns launcher-specific tip based on the detected launcher.
 */
fun getLauncherSpecificTip(context: Context): String {
    val launcherPackage = detectCurrentLauncher(context) ?: return "If the icon doesn't update, try restarting your launcher."

    return when {
        launcherPackage.contains("samsung") || launcherPackage.contains("sec.android") ->
            "Samsung: Long-press home screen → Settings → Clear cache, then restart launcher."
        launcherPackage.contains("google") || launcherPackage.contains("nexuslauncher") ->
            "Pixel Launcher: Go to Settings → Apps → Pixel Launcher → Storage → Clear Cache, then return to home screen."
        launcherPackage.contains("nova") ->
            "Nova Launcher: Go to Nova Settings → Look & Feel → Icon Style and back to refresh."
        launcherPackage.contains("microsoft") ->
            "Microsoft Launcher: The icon should update automatically."
        launcherPackage.contains("lawnchair") ->
            "Lawnchair: Restart the launcher from settings to refresh icons."
        launcherPackage.contains("oneplus") ->
            "OnePlus Launcher: Long-press home → Settings → refresh to update icons."
        launcherPackage.contains("miui") || launcherPackage.contains("xiaomi") ->
            "MIUI: The icon may take a moment. Try clearing launcher cache in Settings."
        launcherPackage.contains("huawei") || launcherPackage.contains("emui") ->
            "Huawei: Restart the launcher or device to see the new icon."
        launcherPackage.contains("oppo") || launcherPackage.contains("coloros") ->
            "OPPO/ColorOS: Clear launcher cache in Settings to refresh the icon."
        launcherPackage.contains("action") ->
            "Action Launcher: The icon should update. If not, restart the launcher."
        launcherPackage.contains("poco") ->
            "POCO Launcher: Clear launcher cache to refresh the icon."
        launcherPackage.contains("realme") ->
            "Realme: Restart the launcher to see the new icon."
        launcherPackage.contains("vivo") ->
            "Vivo: Restart your device to ensure the icon updates."
        launcherPackage.contains("asus") ->
            "ASUS: The icon should update. Restart launcher if needed."
        launcherPackage.contains("motorola") || launcherPackage.contains("moto") ->
            "Motorola: The icon should update automatically."
        launcherPackage.contains("nothing") ->
            "Nothing Launcher: Restart the launcher to refresh icons."
        else ->
            "If the icon doesn't update, try restarting your launcher or device."
    }
}

/**
 * App Icon Customization Screen
 *
 * Neurodivergent-Centric Features:
 * - Visual previews with clear differentiation
 * - Descriptive labels explaining each icon's purpose/mood
 * - Sensory-friendly options for those sensitive to bright colors
 * - Pride option celebrating neurodivergent identity
 * - Smooth, predictable animations
 * - Clear feedback when icon is applied
 */

// Icon style options with neurodivergent-centric themes
enum class AppIconStyle(
    val aliasName: String,
    val titleRes: Int,
    val descRes: Int,
    val gradientColors: List<Color>,
    val emoji: String,
    val category: IconCategory
) {
    DEFAULT(
        aliasName = "com.kyilmaz.neurocomet.MainActivityDefaultAlias",
        titleRes = R.string.icon_style_default,
        descRes = R.string.icon_style_default_desc,
        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        emoji = "☄️",
        category = IconCategory.STANDARD
    ),
    CALM(
        aliasName = "com.kyilmaz.neurocomet.MainActivityCalmAlias",
        titleRes = R.string.icon_style_calm,
        descRes = R.string.icon_style_calm_desc,
        gradientColors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)),
        emoji = "🌊",
        category = IconCategory.SENSORY
    ),
    FOCUS(
        aliasName = "com.kyilmaz.neurocomet.MainActivityFocusAlias",
        titleRes = R.string.icon_style_focus,
        descRes = R.string.icon_style_focus_desc,
        gradientColors = listOf(Color(0xFF0f0c29), Color(0xFF302b63), Color(0xFF24243e)),
        emoji = "🎯",
        category = IconCategory.FOCUS
    ),
    ENERGY(
        aliasName = "com.kyilmaz.neurocomet.MainActivityEnergyAlias",
        titleRes = R.string.icon_style_energy,
        descRes = R.string.icon_style_energy_desc,
        gradientColors = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        emoji = "⚡",
        category = IconCategory.MOTIVATION
    ),
    SENSORY_FRIENDLY(
        aliasName = "com.kyilmaz.neurocomet.MainActivitySensoryAlias",
        titleRes = R.string.icon_style_sensory,
        descRes = R.string.icon_style_sensory_desc,
        gradientColors = listOf(Color(0xFF434343), Color(0xFF000000)),
        emoji = "🧘",
        category = IconCategory.SENSORY
    ),
    NEURODIVERSITY_PRIDE(
        aliasName = "com.kyilmaz.neurocomet.MainActivityPrideAlias",
        titleRes = R.string.icon_style_pride,
        descRes = R.string.icon_style_pride_desc,
        gradientColors = listOf(Color(0xFFff6b6b), Color(0xFFffd93d), Color(0xFF6bcb77)),
        emoji = "🌈",
        category = IconCategory.PRIDE
    )
}

enum class IconCategory(val label: String, val emoji: String) {
    STANDARD("Standard", "⭐"),
    SENSORY("Sensory-Friendly", "🧘"),
    FOCUS("Focus & Productivity", "🎯"),
    MOTIVATION("Motivation", "⚡"),
    PRIDE("Identity & Pride", "🌈")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconCustomizationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Load saved preference
    var selectedIcon by remember {
        mutableStateOf(getSelectedIconStyle(context))
    }
    var showApplyDialog by remember { mutableStateOf(false) }

    // Pre-fetch strings for Toast
    val iconAppliedMessage = stringResource(R.string.icon_applied)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_app_icon)) },
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
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨",
                        fontSize = 48.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.icon_customization_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.icon_customization_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Neurodivergent tips card
            item {
                NeurodivergentTipsCard()
            }

            // Group icons by category
            IconCategory.entries.forEach { category ->
                val iconsInCategory = AppIconStyle.entries.filter { it.category == category }
                if (iconsInCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = "${category.emoji} ${category.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(iconsInCategory) { iconStyle ->
                        IconOptionCard(
                            iconStyle = iconStyle,
                            isSelected = selectedIcon == iconStyle,
                            onClick = {
                                selectedIcon = iconStyle
                                showApplyDialog = true
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Apply confirmation dialog
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(selectedIcon.gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedIcon.emoji, fontSize = 32.sp)
                }
            },
            title = {
                Text(
                    "Create ${stringResource(selectedIcon.titleRes)} Shortcut?",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(selectedIcon.descRes),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This will create a new home screen shortcut with this icon style.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "💡 After adding the new shortcut, you can remove the old one from your home screen.",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        applyAppIcon(context, selectedIcon)
                        showApplyDialog = false
                    }
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Create Shortcut")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NeurodivergentTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tips for Choosing Your Icon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))

            TipItem(
                emoji = "🧘",
                text = "Sensory-friendly icons use muted colors that are gentler on the eyes"
            )
            TipItem(
                emoji = "🎯",
                text = "Focus icons are minimal to reduce visual distractions"
            )
            TipItem(
                emoji = "⚡",
                text = "Energy icons can help motivate on low-energy days"
            )
            TipItem(
                emoji = "🌈",
                text = "Pride icons celebrate your neurodivergent identity"
            )
        }
    }
}

@Composable
private fun TipItem(emoji: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconOptionCard(
    iconStyle: AppIconStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Load the actual adaptive icon and render it to a bitmap for preview
    val iconBitmap = remember(iconStyle) {
        try {
            val iconResId = when (iconStyle) {
                AppIconStyle.DEFAULT -> R.mipmap.neuro_comet_icon
                AppIconStyle.CALM -> R.mipmap.ic_launcher_calm
                AppIconStyle.FOCUS -> R.mipmap.ic_launcher_focus
                AppIconStyle.ENERGY -> R.mipmap.ic_launcher_energy
                AppIconStyle.SENSORY_FRIENDLY -> R.mipmap.ic_launcher_sensory
                AppIconStyle.NEURODIVERSITY_PRIDE -> R.mipmap.ic_launcher_pride
            }

            val drawable = androidx.core.content.ContextCompat.getDrawable(context, iconResId)
            if (drawable != null) {
                val size = 168
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)

                // For adaptive icons, we need to handle them specially
                if (drawable is android.graphics.drawable.AdaptiveIconDrawable) {
                    // Draw background layer
                    drawable.background?.let { bg ->
                        bg.setBounds(0, 0, size, size)
                        bg.draw(canvas)
                    }
                    // Draw foreground layer
                    drawable.foreground?.let { fg ->
                        fg.setBounds(0, 0, size, size)
                        fg.draw(canvas)
                    }
                } else {
                    drawable.setBounds(0, 0, size, size)
                    drawable.draw(canvas)
                }
                bitmap
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon preview - show actual rendered icon
            if (iconBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = stringResource(iconStyle.titleRes),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(iconStyle.gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconStyle.emoji, fontSize = 28.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(iconStyle.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(iconStyle.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Get the icon resource ID for the given icon style
 */
fun getIconResourceId(iconStyle: AppIconStyle): Int {
    return when (iconStyle) {
        AppIconStyle.DEFAULT -> R.mipmap.neuro_comet_icon
        AppIconStyle.CALM -> R.mipmap.ic_launcher_calm
        AppIconStyle.FOCUS -> R.mipmap.ic_launcher_focus
        AppIconStyle.ENERGY -> R.mipmap.ic_launcher_energy
        AppIconStyle.SENSORY_FRIENDLY -> R.mipmap.ic_launcher_sensory
        AppIconStyle.NEURODIVERSITY_PRIDE -> R.mipmap.ic_launcher_pride
    }
}

/**
 * Get the background drawable resource for each icon style
 */
fun getIconBackgroundResource(iconStyle: AppIconStyle): Int {
    return when (iconStyle) {
        AppIconStyle.DEFAULT -> R.drawable.neuro_comet_icon_background
        AppIconStyle.CALM -> R.drawable.icon_calm_background
        AppIconStyle.FOCUS -> R.drawable.icon_focus_background
        AppIconStyle.ENERGY -> R.drawable.icon_energy_background
        AppIconStyle.SENSORY_FRIENDLY -> R.drawable.icon_sensory_background
        AppIconStyle.NEURODIVERSITY_PRIDE -> R.drawable.icon_pride_background
    }
}

/**
 * Apply the selected app icon by creating a pinned shortcut.
 *
 * This is the ONLY reliable way to have custom app icons on Android.
 * The component-alias approach is fundamentally broken because:
 * 1. Launchers cache component references
 * 2. When you disable an alias, the cached shortcut becomes invalid
 * 3. Tapping a cached shortcut to a disabled alias = crash
 *
 * Instead, we:
 * 1. Save the preference for next app reinstall
 * 2. Create a new pinned shortcut with the custom icon
 * 3. User can replace their existing shortcut with the new one
 */
fun applyAppIcon(context: Context, iconStyle: AppIconStyle) {
    val TAG = "IconCustomization"

    // Save the selected icon preference
    val prefs = context.getSharedPreferences("icon_preferences", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_icon", iconStyle.name).apply()

    android.util.Log.d(TAG, "Saving icon preference: ${iconStyle.name}")

    // Create a pinned shortcut with the selected icon
    createPinnedShortcut(context, iconStyle)
}

/**
 * Create a pinned shortcut with the selected icon style
 */
fun createPinnedShortcut(context: Context, iconStyle: AppIconStyle) {
    val TAG = "IconCustomization"

    try {
        val shortcutManager = context.getSystemService(android.content.pm.ShortcutManager::class.java)

        if (shortcutManager == null) {
            android.util.Log.e(TAG, "ShortcutManager not available")
            Toast.makeText(context, "Shortcuts not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        if (!shortcutManager.isRequestPinShortcutSupported) {
            android.util.Log.e(TAG, "Pinned shortcuts not supported")
            Toast.makeText(context, "Your launcher doesn't support pinned shortcuts", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the shortcut intent
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create the icon by manually compositing background and foreground
        val icon = createCompositeIcon(context, iconStyle)

        android.util.Log.d(TAG, "Icon created for style: ${iconStyle.name}")

        // Create the shortcut info with a unique ID to prevent caching issues
        val timestamp = System.currentTimeMillis()
        val shortcutId = "neurocomet_${iconStyle.name.lowercase()}_$timestamp"

        android.util.Log.d(TAG, "Creating shortcut with ID: $shortcutId")

        val shortcutInfo = android.content.pm.ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel("NeuroComet - ${context.getString(iconStyle.titleRes)}")
            .setIcon(icon)
            .setIntent(intent)
            .build()

        // Request to pin the shortcut
        val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcutInfo)
        val successCallback = android.app.PendingIntent.getBroadcast(
            context,
            0,
            pinnedShortcutCallbackIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        shortcutManager.requestPinShortcut(shortcutInfo, successCallback.intentSender)

        android.util.Log.d(TAG, "Requested pinned shortcut for ${iconStyle.name}")

        Toast.makeText(
            context,
            "Add the new shortcut to your home screen, then remove the old one",
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to create pinned shortcut", e)
        Toast.makeText(context, "Failed to create shortcut: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

/**
 * Create the icon for the shortcut using the pre-built adaptive icon resources.
 * This uses the actual mipmap resources which have proper foreground transparency.
 */
private fun createCompositeIcon(context: Context, iconStyle: AppIconStyle): android.graphics.drawable.Icon {
    val TAG = "IconCustomization"

    // Use the pre-built adaptive icon resources directly
    // These have the correct foreground with transparency
    val iconResId = when (iconStyle) {
        AppIconStyle.DEFAULT -> R.mipmap.neuro_comet_icon
        AppIconStyle.CALM -> R.mipmap.ic_launcher_calm
        AppIconStyle.FOCUS -> R.mipmap.ic_launcher_focus
        AppIconStyle.ENERGY -> R.mipmap.ic_launcher_energy
        AppIconStyle.SENSORY_FRIENDLY -> R.mipmap.ic_launcher_sensory
        AppIconStyle.NEURODIVERSITY_PRIDE -> R.mipmap.ic_launcher_pride
    }

    android.util.Log.d(TAG, "Using pre-built icon resource for: ${iconStyle.name}")

    return android.graphics.drawable.Icon.createWithResource(context, iconResId)
}


/**
 * Get the currently selected icon style from preferences
 */
fun getSelectedIconStyle(context: Context): AppIconStyle {
    val prefs = context.getSharedPreferences("icon_preferences", Context.MODE_PRIVATE)
    val savedName = prefs.getString("selected_icon", AppIconStyle.DEFAULT.name)
    return try {
        AppIconStyle.valueOf(savedName ?: AppIconStyle.DEFAULT.name)
    } catch (e: Exception) {
        AppIconStyle.DEFAULT
    }
}


/**
 * Dialog version for use in Settings without full navigation
 */
@Composable
fun IconCustomizationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedIcon by remember { mutableStateOf(getSelectedIconStyle(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.icon_customization_title))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    "Choose an icon style and add it to your home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // Info card about how it works
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "This will create a new shortcut. Add it to your home screen, then remove the old icon.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Scrollable icon list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppIconStyle.entries.toList()) { iconStyle ->
                        IconOptionCardCompact(
                            iconStyle = iconStyle,
                            isSelected = selectedIcon == iconStyle,
                            onClick = { selectedIcon = iconStyle }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    applyAppIcon(context, selectedIcon)
                    onDismiss()
                }
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Create Shortcut")
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
private fun IconOptionCardCompact(
    iconStyle: AppIconStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Load the actual adaptive icon and render it to a bitmap for preview
    val iconBitmap = remember(iconStyle) {
        try {
            val iconResId = when (iconStyle) {
                AppIconStyle.DEFAULT -> R.mipmap.neuro_comet_icon
                AppIconStyle.CALM -> R.mipmap.ic_launcher_calm
                AppIconStyle.FOCUS -> R.mipmap.ic_launcher_focus
                AppIconStyle.ENERGY -> R.mipmap.ic_launcher_energy
                AppIconStyle.SENSORY_FRIENDLY -> R.mipmap.ic_launcher_sensory
                AppIconStyle.NEURODIVERSITY_PRIDE -> R.mipmap.ic_launcher_pride
            }

            val drawable = androidx.core.content.ContextCompat.getDrawable(context, iconResId)
            if (drawable != null) {
                val size = 160
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)

                // For adaptive icons, we need to handle them specially
                if (drawable is android.graphics.drawable.AdaptiveIconDrawable) {
                    // Draw background layer
                    drawable.background?.let { bg ->
                        bg.setBounds(0, 0, size, size)
                        bg.draw(canvas)
                    }
                    // Draw foreground layer
                    drawable.foreground?.let { fg ->
                        fg.setBounds(0, 0, size, size)
                        fg.draw(canvas)
                    }
                } else {
                    drawable.setBounds(0, 0, size, size)
                    drawable.draw(canvas)
                }
                bitmap
            } else null
        } catch (e: Exception) {
            android.util.Log.e("IconCustomization", "Error creating preview for ${iconStyle.name}", e)
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show the rendered icon
            if (iconBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = stringResource(iconStyle.titleRes),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                // Fallback to gradient with emoji
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(iconStyle.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconStyle.emoji, fontSize = 24.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(iconStyle.titleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(iconStyle.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


// Extension function to convert Drawable to Bitmap
private fun android.graphics.drawable.Drawable.toBitmap(width: Int, height: Int): android.graphics.Bitmap {
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    this.setBounds(0, 0, width, height)
    this.draw(canvas)
    return bitmap
}



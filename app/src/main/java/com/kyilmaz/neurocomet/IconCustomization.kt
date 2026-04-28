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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

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
    CALM(
        aliasName = "com.kyilmaz.neurocomet.LauncherCalm",
        titleRes = R.string.icon_style_calm,
        descRes = R.string.icon_style_calm_desc,
        gradientColors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)),
        emoji = "🌊",
        category = IconCategory.SENSORY
    ),
    SENSORY_FRIENDLY(
        aliasName = "com.kyilmaz.neurocomet.LauncherSensory",
        titleRes = R.string.icon_style_sensory,
        descRes = R.string.icon_style_sensory_desc,
        gradientColors = listOf(Color(0xFF434343), Color(0xFF000000)),
        emoji = "🧘",
        category = IconCategory.SENSORY
    ),
    FOCUS(
        aliasName = "com.kyilmaz.neurocomet.LauncherFocus",
        titleRes = R.string.icon_style_focus,
        descRes = R.string.icon_style_focus_desc,
        gradientColors = listOf(Color(0xFF0f0c29), Color(0xFF302b63), Color(0xFF24243e)),
        emoji = "🎯",
        category = IconCategory.FOCUS
    ),
    ENERGY(
        aliasName = "com.kyilmaz.neurocomet.LauncherEnergy",
        titleRes = R.string.icon_style_energy,
        descRes = R.string.icon_style_energy_desc,
        gradientColors = listOf(Color(0xFFf093fb), Color(0xFFf5576c)),
        emoji = "⚡",
        category = IconCategory.MOTIVATION
    ),
    NEURODIVERSITY_PRIDE(
        aliasName = "com.kyilmaz.neurocomet.LauncherPride",
        titleRes = R.string.icon_style_pride,
        descRes = R.string.icon_style_pride_desc,
        gradientColors = listOf(Color(0xFFff6b6b), Color(0xFFffd93d), Color(0xFF6bcb77)),
        emoji = "🌈",
        category = IconCategory.PRIDE
    ),
    DEFAULT(
        aliasName = "com.kyilmaz.neurocomet.LauncherDefault",
        titleRes = R.string.icon_style_default,
        descRes = R.string.icon_style_default_desc,
        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        emoji = "☄️",
        category = IconCategory.STANDARD
    )
}

enum class IconCategory(val label: String, val emoji: String) {
    STANDARD("Standard", "⭐"),
    SENSORY("Sensory-Friendly", "🧘"),
    FOCUS("Focus & Productivity", "🎯"),
    MOTIVATION("Motivation", "⚡"),
    PRIDE("Identity & Pride", "🌈")
}

private data class IconDepthProfile(
    val shadowAlpha: Float,
    val glossAlpha: Float,
    val rimAlpha: Float,
    val vignetteAlpha: Float
)

private data class IconPreviewProfile(
    val scale: Float
)

private fun AppIconStyle.depthProfile(): IconDepthProfile = when (this) {
    AppIconStyle.DEFAULT -> IconDepthProfile(0.30f, 0.24f, 0.22f, 0.16f)
    AppIconStyle.CALM -> IconDepthProfile(0.24f, 0.20f, 0.20f, 0.14f)
    AppIconStyle.FOCUS -> IconDepthProfile(0.34f, 0.20f, 0.26f, 0.20f)
    AppIconStyle.ENERGY -> IconDepthProfile(0.28f, 0.28f, 0.22f, 0.16f)
    AppIconStyle.SENSORY_FRIENDLY -> IconDepthProfile(0.22f, 0.14f, 0.18f, 0.22f)
    AppIconStyle.NEURODIVERSITY_PRIDE -> IconDepthProfile(0.28f, 0.30f, 0.22f, 0.16f)
}

private fun AppIconStyle.previewProfile(): IconPreviewProfile = when (this) {
    AppIconStyle.DEFAULT -> IconPreviewProfile(scale = 1.09f)
    AppIconStyle.CALM -> IconPreviewProfile(scale = 1.12f)
    AppIconStyle.FOCUS -> IconPreviewProfile(scale = 1.10f)
    AppIconStyle.ENERGY -> IconPreviewProfile(scale = 1.10f)
    AppIconStyle.NEURODIVERSITY_PRIDE -> IconPreviewProfile(scale = 1.09f)
    AppIconStyle.SENSORY_FRIENDLY -> IconPreviewProfile(scale = 1.13f)
}

private fun getIconDrawableResources(iconStyle: AppIconStyle): Pair<Int, Int> = when (iconStyle) {
    AppIconStyle.DEFAULT -> R.drawable.neuro_comet_icon_background to R.drawable.neuro_comet_icon_foreground_padded
    AppIconStyle.CALM -> R.drawable.icon_calm_background to R.drawable.calm_waters_1
    AppIconStyle.FOCUS -> R.drawable.icon_focus_background to R.drawable.icon_focus_foreground_padded
    AppIconStyle.ENERGY -> R.drawable.icon_energy_background to R.drawable.energy_burst_1
    AppIconStyle.SENSORY_FRIENDLY -> R.drawable.icon_sensory_background to R.drawable.icon_sensory_foreground_padded
    AppIconStyle.NEURODIVERSITY_PRIDE -> R.drawable.icon_pride_background to R.drawable.neurodiversity_pride_4
}

internal fun getAdaptiveIconDrawable(
    context: Context,
    iconStyle: AppIconStyle
): android.graphics.drawable.Drawable? {
    return androidx.core.content.ContextCompat.getDrawable(context, getIconResourceId(iconStyle))
}

internal fun renderDepthEnhancedIconBitmap(
    context: Context,
    iconStyle: AppIconStyle,
    size: Int,
    previewOptimized: Boolean = false
): android.graphics.Bitmap? {
    return try {
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val depth = iconStyle.depthProfile()
        val previewScale = if (previewOptimized) iconStyle.previewProfile().scale else 1.0f

        val ambientShadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb((depth.shadowAlpha * 0.55f * 255).toInt(), 6, 10, 20)
            maskFilter = android.graphics.BlurMaskFilter(size * 0.09f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawOval(
            android.graphics.RectF(size * 0.12f, size * 0.16f, size * 0.88f, size * 0.94f),
            ambientShadowPaint
        )

        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb((depth.shadowAlpha * 255).toInt(), 10, 16, 28)
            maskFilter = android.graphics.BlurMaskFilter(size * 0.06f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawOval(
            android.graphics.RectF(size * 0.18f, size * 0.24f, size * 0.82f, size * 0.92f),
            shadowPaint
        )

        val maskPath = android.graphics.Path().apply {
            addCircle(size / 2f, size / 2f, size * 0.47f, android.graphics.Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(maskPath)

        val drawInset = ((size - (size * previewScale)) / 2f).toInt()
        getAdaptiveIconDrawable(context, iconStyle)?.apply {
            setBounds(drawInset, drawInset, size - drawInset, size - drawInset)
            draw(canvas)
        }


        val glossPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(
                size * 0.12f,
                size * 0.06f,
                size * 0.7f,
                size * 0.72f,
                android.graphics.Color.argb((depth.glossAlpha * 255).toInt(), 255, 255, 255),
                android.graphics.Color.TRANSPARENT,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(size / 2f, size / 2f, size * 0.47f, glossPaint)

        val specularPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.RadialGradient(
                size * 0.32f,
                size * 0.28f,
                size * 0.18f,
                intArrayOf(
                    android.graphics.Color.argb((depth.glossAlpha * 0.9f * 255).toInt(), 255, 255, 255),
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(size * 0.32f, size * 0.28f, size * 0.18f, specularPaint)

        canvas.restore()
        bitmap
    } catch (e: Exception) {
        android.util.Log.e("IconCustomization", "Error rendering depth icon for ${iconStyle.name}", e)
        null
    }
}

@Composable
private fun DepthIconPreview(
    iconBitmap: android.graphics.Bitmap?,
    iconStyle: AppIconStyle,
    size: Dp,
    isSelected: Boolean = false
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size + 14.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            iconStyle.gradientColors.first().copy(alpha = if (isSelected) 0.24f else 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (isSelected) 10.dp else 6.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = stringResource(iconStyle.titleRes),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(iconStyle.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconStyle.emoji, fontSize = if (size >= 56.dp) 28.sp else 24.sp)
                }
            }
        }
    }
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

    val dialogIconBitmap = remember(selectedIcon) {
        renderDepthEnhancedIconBitmap(context, selectedIcon, 220, previewOptimized = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_app_icon)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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

    // Apply confirmation dialog with two options
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            icon = {
                DepthIconPreview(
                    iconBitmap = dialogIconBitmap,
                    iconStyle = selectedIcon,
                    size = 64.dp,
                    isSelected = true
                )
            },
            title = {
                Text(
                    stringResource(selectedIcon.titleRes),
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
                    Spacer(Modifier.height(12.dp))

                    // Option 1: Change App Icon
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎨", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.icon_change_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Changes the main app icon in your launcher. May take a few seconds to refresh.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    applyAppIcon(context, selectedIcon)
                                    showApplyDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.icon_apply_button))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Divider with "OR"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            "  OR  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Option 2: Create Shortcut
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📌", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.icon_add_shortcut_button),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Creates a themed shortcut. After tapping, look at the BOTTOM of your screen for 'Add to Home screen' popup!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "💡 Tip: Long-press app icon to see shortcuts in menu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    // Launch the ShortcutRequestActivity to handle the shortcut
                                    // This works better for triggering the system's "Add to Home" dialog
                                    val intent = Intent(context, ShortcutRequestActivity::class.java).apply {
                                        putExtra(ShortcutRequestActivity.EXTRA_ICON_STYLE, selectedIcon.name)
                                    }
                                    context.startActivity(intent)
                                    showApplyDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.icon_create_shortcut_button))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
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

    val iconBitmap = remember(iconStyle) {
        renderDepthEnhancedIconBitmap(context, iconStyle, 168, previewOptimized = true)
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
            DepthIconPreview(
                iconBitmap = iconBitmap,
                iconStyle = iconStyle,
                size = 56.dp,
                isSelected = isSelected
            )

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
                        contentDescription = stringResource(R.string.cd_selected_state),
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
 * Apply the selected app icon using Android's activity-alias mechanism.
 *
 * This enables/disables activity-alias components to change the launcher icon.
 * The icon change will take effect after the launcher refreshes (usually 5-10 seconds,
 * or after restarting the launcher).
 */
fun applyAppIcon(context: Context, iconStyle: AppIconStyle) {
    val TAG = "IconCustomization"

    // Save the selected icon preference
    val prefs = context.getSharedPreferences("icon_preferences", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_icon", iconStyle.name).apply()

    android.util.Log.d(TAG, "Applying icon style: ${iconStyle.name}")

    // Change the app icon using activity-alias
    changeAppIcon(context, iconStyle)
}

/**
 * Map of AppIconStyle to their corresponding activity-alias class names
 */
    // Map of AppIconStyle to their corresponding activity-alias class names
    // Note: The manifest must contain these aliases.
    private val iconAliasMap = mapOf(
        AppIconStyle.DEFAULT to "com.kyilmaz.neurocomet.LauncherDefault",
        AppIconStyle.CALM to "com.kyilmaz.neurocomet.LauncherCalm",
        AppIconStyle.FOCUS to "com.kyilmaz.neurocomet.LauncherFocus",
        AppIconStyle.ENERGY to "com.kyilmaz.neurocomet.LauncherEnergy",
        AppIconStyle.SENSORY_FRIENDLY to "com.kyilmaz.neurocomet.LauncherSensory",
        AppIconStyle.NEURODIVERSITY_PRIDE to "com.kyilmaz.neurocomet.LauncherPride"
    )

private val stableEntryActivities = listOf(
    "com.kyilmaz.neurocomet.MainActivityDefault",
    "com.kyilmaz.neurocomet.MainActivityDefaultAlias",
    "com.kyilmaz.neurocomet.MainActivityDefaultLauncherAlias",
    "com.kyilmaz.neurocomet.MainActivityDefaultIconAlias",
    "com.kyilmaz.neurocomet.MainActivityDefaultVisualAlias",
    "com.kyilmaz.neurocomet.MainActivityCalm",
    "com.kyilmaz.neurocomet.MainActivityFocus",
    "com.kyilmaz.neurocomet.MainActivityEnergy",
    "com.kyilmaz.neurocomet.MainActivitySensory",
    "com.kyilmaz.neurocomet.MainActivityPride"
)

private fun ensureStableEntryActivitiesEnabled(context: Context) {
    val packageManager = context.packageManager
    stableEntryActivities.forEach { className ->
        val componentName = android.content.ComponentName(context, className)
        if (packageManager.getComponentEnabledSetting(componentName) != android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(
                componentName,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        }
    }
}

/**
 * Change the app icon by enabling/disabling activity-alias components.
 * This is the proper Android way to change app icons dynamically.
 */
fun changeAppIcon(context: Context, iconStyle: AppIconStyle) {
    val TAG = "IconCustomization"
    val packageManager = context.packageManager

    android.util.Log.d(TAG, "Changing app icon to: ${iconStyle.name}")

    try {
        ensureStableEntryActivitiesEnabled(context)

        // Get the target alias for the selected style
        val targetAlias = iconAliasMap[iconStyle] ?: iconAliasMap[AppIconStyle.DEFAULT]!!

        // 1. Enable the target alias FIRST to ensure a launcher component is always present
        packageManager.setComponentEnabledSetting(
            android.content.ComponentName(context, targetAlias),
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            android.content.pm.PackageManager.DONT_KILL_APP
        )

        // 2. Disable all other aliases
        iconAliasMap.forEach { (_, aliasName) ->
            if (aliasName != targetAlias) {
                val componentName = android.content.ComponentName(context, aliasName)
                
                // Only change if currently enabled
                if (packageManager.getComponentEnabledSetting(componentName) != android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    android.util.Log.d(TAG, "Disabling: $aliasName")
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                }
            }
        }

        android.util.Log.d(TAG, "Icon changed to: ${iconStyle.name}")

        // Get launcher-specific tip for icon change
        val launcherInfo = LauncherDetector.detectLauncher(context)
        val tip = launcherInfo.getIconChangeTip()

        Toast.makeText(
            context,
            context.getString(R.string.icon_change_success, tip),
            Toast.LENGTH_LONG
        ).show()

    } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to change app icon", e)
        e.printStackTrace()

        // Fallback to creating a pinned shortcut
        Toast.makeText(
            context,
            "Icon change failed. Creating shortcut instead...",
            Toast.LENGTH_SHORT
        ).show()
        createPinnedShortcut(context, iconStyle)
    }
}

/**
 * Create a pinned shortcut with the selected icon style using ShortcutManagerCompat.
 * This uses AndroidX's compatibility library for better cross-device support.
 */
fun createPinnedShortcut(context: Context, iconStyle: AppIconStyle) {
    val TAG = "IconCustomization"

    try {
        android.util.Log.d(TAG, "Starting shortcut creation for: ${iconStyle.name}")

        // Detect launcher and log diagnostics
        val launcherInfo = LauncherDetector.detectLauncher(context)
        android.util.Log.d(TAG, "Detected launcher: ${launcherInfo.launcherType.displayName} (${launcherInfo.packageName})")
        android.util.Log.d(TAG, "Supports pinned: ${launcherInfo.supportsPinnedShortcuts}, Legacy: ${launcherInfo.supportsLegacyBroadcast}")

        // Log full diagnostics in debug
        LauncherDetector.logDiagnostics(context)

        // Check if pinned shortcuts are supported using Compat library
        val isPinSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(context)
        android.util.Log.d(TAG, "ShortcutManagerCompat.isRequestPinShortcutSupported: $isPinSupported")

        // Create the shortcut intent - explicitly target the currently enabled activity alias
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
            // Don't set a specific component - let the system resolve it
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        android.util.Log.d(TAG, "Created launch intent: $launchIntent")

        // Create the icon bitmap for the shortcut
        val iconBitmap = createCompositeIconBitmap(context, iconStyle)
        val iconCompat = IconCompat.createWithBitmap(iconBitmap)

        android.util.Log.d(TAG, "Icon bitmap created for style: ${iconStyle.name}, size: ${iconBitmap.width}x${iconBitmap.height}")

        // Create a unique shortcut ID
        val shortcutId = "neurocomet_${iconStyle.name.lowercase()}_${System.currentTimeMillis()}"

        android.util.Log.d(TAG, "Building ShortcutInfoCompat with ID: $shortcutId")

        // Build the shortcut info using the Compat library
        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(context.getString(R.string.app_name))
            .setLongLabel("NeuroComet - ${context.getString(iconStyle.titleRes)}")
            .setIcon(iconCompat)
            .setIntent(launchIntent)
            .build()

        android.util.Log.d(TAG, "ShortcutInfoCompat built successfully")

        // First, try to add as a dynamic shortcut (shows in long-press menu)
        try {
            // Push this shortcut as a dynamic shortcut
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcutInfo)
            android.util.Log.d(TAG, "Added dynamic shortcut successfully")
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Could not add dynamic shortcut: ${e.message}")
        }

        if (isPinSupported) {
            // Request to pin the shortcut using Compat library
            val result = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)

            android.util.Log.d(TAG, "ShortcutManagerCompat.requestPinShortcut returned: $result")

            if (result) {
                // Show clear instructions
                Toast.makeText(
                    context,
                    context.getString(R.string.shortcut_instruction_pin),
                    Toast.LENGTH_LONG
                ).show()

                // Also show a follow-up message
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    Toast.makeText(
                        context,
                        context.getString(R.string.shortcut_instruction_long_press),
                        Toast.LENGTH_LONG
                    ).show()
                }, 3500)
                return
            } else {
                android.util.Log.w(TAG, "requestPinShortcut returned false, trying alternatives")
            }
        }

        // If pinned shortcut failed or not supported, try alternatives
        android.util.Log.d(TAG, "Trying alternative shortcut methods...")

        // Try launcher-specific methods
        if (tryLauncherSpecificShortcut(context, launcherInfo, launchIntent, iconBitmap)) {
            return
        }

        // Try legacy broadcast method as fallback
        tryLegacyShortcut(context, iconStyle, launchIntent, iconBitmap)

    } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to create pinned shortcut", e)
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.shortcut_failed_generic, e.message), Toast.LENGTH_LONG).show()
    }
}

/**
 * Try launcher-specific shortcut creation methods using LauncherDetector info.
 * Returns true if a method was attempted (doesn't guarantee success)
 */
private fun tryLauncherSpecificShortcut(
    context: Context,
    launcherInfo: LauncherDetector.LauncherInfo,
    launchIntent: Intent,
    iconBitmap: android.graphics.Bitmap
): Boolean {
    val TAG = "IconCustomization"
    val legacyShortcutIntentExtra = "android.intent.extra.shortcut.INTENT"
    val legacyShortcutNameExtra = "android.intent.extra.shortcut.NAME"
    val legacyShortcutIconExtra = "android.intent.extra.shortcut.ICON"

    android.util.Log.d(TAG, "Trying launcher-specific shortcut for: ${launcherInfo.launcherType.displayName}")

    // Check if this launcher has a custom shortcut action
    val customAction = launcherInfo.customShortcutAction

    if (customAction != null) {
        try {
            val intent = Intent(customAction).apply {
                putExtra(legacyShortcutIntentExtra, launchIntent)
                putExtra(legacyShortcutNameExtra, context.getString(R.string.app_name))
                putExtra(legacyShortcutIconExtra, iconBitmap)
                putExtra("duplicate", false)
            }
            context.sendBroadcast(intent)
            android.util.Log.d(TAG, "Sent custom launcher broadcast: $customAction")
            Toast.makeText(context, context.getString(R.string.shortcut_added, launcherInfo.getShortcutTip()), Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Custom launcher broadcast failed", e)
        }
    }

    // Fallback for launchers that support legacy broadcast
    if (launcherInfo.supportsLegacyBroadcast) {
        try {
            val intent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(legacyShortcutIntentExtra, launchIntent)
                putExtra(legacyShortcutNameExtra, context.getString(R.string.app_name))
                putExtra(legacyShortcutIconExtra, iconBitmap)
                putExtra("duplicate", false)
            }
            context.sendBroadcast(intent)
            android.util.Log.d(TAG, "Sent legacy launcher broadcast for: ${launcherInfo.launcherType.displayName}")
            Toast.makeText(context, context.getString(R.string.shortcut_added, launcherInfo.getShortcutTip()), Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Legacy launcher broadcast failed", e)
        }
    }

    return false
}


/**
 * Legacy shortcut creation for older launchers.
 * Creates a new intent to avoid issues with modified intents.
 */
private fun tryLegacyShortcut(context: Context, iconStyle: AppIconStyle, launchIntent: Intent, iconBitmap: android.graphics.Bitmap) {
    val TAG = "IconCustomization"
    try {
        val legacyShortcutIntentExtra = "android.intent.extra.shortcut.INTENT"
        val legacyShortcutNameExtra = "android.intent.extra.shortcut.NAME"
        val legacyShortcutIconExtra = "android.intent.extra.shortcut.ICON"

        // Create a fresh intent for the shortcut target
        val shortcutTargetIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(legacyShortcutIntentExtra, shortcutTargetIntent)
            putExtra(legacyShortcutNameExtra, context.getString(R.string.app_name))
            putExtra(legacyShortcutIconExtra, iconBitmap)
            putExtra("duplicate", false)
        }
        context.sendBroadcast(shortcutIntent)
        android.util.Log.d(TAG, "Legacy shortcut broadcast sent")
        Toast.makeText(
            context,
            context.getString(R.string.shortcut_created),
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        android.util.Log.e(TAG, "Legacy shortcut also failed", e)
        Toast.makeText(
            context,
            context.getString(R.string.shortcut_failed),
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Create the icon for the shortcut using the pre-built adaptive icon resources.
 * This renders the adaptive icon to a bitmap for proper shortcut support.
 */
private fun createCompositeIcon(context: Context, iconStyle: AppIconStyle): android.graphics.drawable.Icon {
    val bitmap = renderDepthEnhancedIconBitmap(context, iconStyle, 432, previewOptimized = true)
        ?: android.graphics.Bitmap.createBitmap(432, 432, android.graphics.Bitmap.Config.ARGB_8888)
    return android.graphics.drawable.Icon.createWithBitmap(bitmap)
}

/**
 * Create a bitmap icon for shortcuts - used by multiple launcher implementations.
 * Returns a Bitmap that can be used with various shortcut APIs.
 */
private fun createCompositeIconBitmap(context: Context, iconStyle: AppIconStyle): android.graphics.Bitmap {
    return renderDepthEnhancedIconBitmap(context, iconStyle, 192, previewOptimized = true)
        ?: android.graphics.Bitmap.createBitmap(192, 192, android.graphics.Bitmap.Config.ARGB_8888)
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
                Text(stringResource(R.string.icon_create_shortcut_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
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

    val iconBitmap = remember(iconStyle) {
        renderDepthEnhancedIconBitmap(context, iconStyle, 160, previewOptimized = true)
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
            DepthIconPreview(
                iconBitmap = iconBitmap,
                iconStyle = iconStyle,
                size = 48.dp,
                isSelected = isSelected
            )

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
                    contentDescription = stringResource(R.string.cd_selected_state),
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

private val newIconPreviewStyles = listOf(
    AppIconStyle.CALM,
    AppIconStyle.SENSORY_FRIENDLY,
    AppIconStyle.FOCUS,
    AppIconStyle.ENERGY,
    AppIconStyle.NEURODIVERSITY_PRIDE,
)

@Preview(name = "New icon cards", showBackground = true, backgroundColor = 0xFFF6F2FF)
@Composable
private fun NewIconCardsPreview() {
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                newIconPreviewStyles.forEachIndexed { index, iconStyle ->
                    IconOptionCard(
                        iconStyle = iconStyle,
                        isSelected = index == 0,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(name = "Icon customization screen", showBackground = true, widthDp = 412, heightDp = 1280)
@Composable
private fun IconCustomizationScreenPreview() {
    MaterialTheme {
        Surface {
            IconCustomizationScreen(onBack = {})
        }
    }
}


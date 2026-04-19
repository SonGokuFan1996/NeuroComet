package com.kyilmaz.neurocomet

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.cos
import kotlin.math.sin

private fun renderDrawableResourceBitmap(
    context: android.content.Context,
    resourceId: Int,
    size: Int
): android.graphics.Bitmap? {
    val drawable = ContextCompat.getDrawable(context, resourceId) ?: return null
    return android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
    }
}



@Composable
fun NeuroCometAmbientBackground(
    modifier: Modifier = Modifier,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    motionEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brandBackdrop")
    val drift = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (motionEnabled) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    ).value

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    lerp(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, 0.14f),
                    MaterialTheme.colorScheme.background,
                    lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant, 0.30f).copy(alpha = 0.96f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val angle = Math.toRadians(drift.toDouble())
            val offsetA = Offset(
                x = width * (0.18f + 0.08f * cos(angle).toFloat()),
                y = height * (0.2f + 0.06f * sin(angle).toFloat())
            )
            val offsetB = Offset(
                x = width * (0.82f + 0.05f * cos(angle + 1.8).toFloat()),
                y = height * (0.18f + 0.07f * sin(angle + 1.8).toFloat())
            )
            val offsetC = Offset(
                x = width * (0.52f + 0.04f * cos(angle + 3.1).toFloat()),
                y = height * (0.82f + 0.05f * sin(angle + 3.1).toFloat())
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                    center = offsetA,
                    radius = width * 0.42f
                ),
                radius = width * 0.42f,
                center = offsetA
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = 0.14f), Color.Transparent),
                    center = offsetB,
                    radius = width * 0.36f
                ),
                radius = width * 0.36f,
                center = offsetB
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiary.copy(alpha = 0.12f), Color.Transparent),
                    center = offsetC,
                    radius = width * 0.46f
                ),
                radius = width * 0.46f,
                center = offsetC
            )
        }
        content()
    }
}

@Composable
fun NeuroCometBrandMark(
    modifier: Modifier = Modifier,
    haloColor: Color,
    accentColor: Color,
    motionEnabled: Boolean = true
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "brandMark")
    val glowScale = infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (motionEnabled) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    ).value

    val isTablet = LocalCanonicalLayout.current.deviceFamily == CanonicalDeviceFamily.TABLET
    // Use current icon preference
    val iconResourceId = getIconResourceId(getSelectedIconStyle(context))
    val iconBitmap = remember(context, iconResourceId) {
        renderDrawableResourceBitmap(context, iconResourceId, 512)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(if (motionEnabled) glowScale else 1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            haloColor.copy(alpha = 0.34f),
                            accentColor.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
                .then(if (isTablet) Modifier else Modifier.blur(28.dp))
        )

        Surface(
            modifier = Modifier
                .matchParentSize()
                .padding(10.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                iconBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(if (motionEnabled) glowScale else 1f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS COMPONENTS (powered by kyant0/backdrop)
// ═══════════════════════════════════════════════════════════════

/**
 * Whether the device supports real-time RenderEffect-based backdrop blur.
 * Requires Android 12 (API 31) or higher.
 */
private fun supportsBackdropBlur(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * A glass-morphism panel that uses real backdrop blur via the kyant0/backdrop library.
 *
 * On API 31+ it captures the content behind this composable and applies a
 * gaussian blur + subtle saturation boost + frosted-glass highlight + inner shadow,
 * creating a true "liquid glass" appearance.
 *
 * On older devices it falls back to a semi-transparent surface.
 *
 * @param modifier Standard modifier
 * @param shape The clip shape (default: 24dp rounded rect)
 * @param blurRadius Gaussian blur radius in pixels (default: 40f)
 * @param tintColor Frosted tint overlay color (default: surface @ 0.55 alpha)
 * @param content Content drawn inside the glass panel
 */
@Composable
fun NeuroGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = M3EDesignSystem.Shapes.LargeShape,
    blurRadius: Float = 40f,
    tintColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
    content: @Composable BoxScope.() -> Unit
) {
    val isTablet = LocalCanonicalLayout.current.deviceFamily == CanonicalDeviceFamily.TABLET
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    if (supportsBackdropBlur()) {
        // ── Real backdrop blur path (API 31+) ──────────────────
        val canvasBackdrop = rememberCanvasBackdrop {
            drawRect(tintColor)
        }

        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = canvasBackdrop,
                    shape = { shape },
                    effects = {
                        blur(radius = blurRadius)
                        colorControls(saturation = if (isDark) 1.2f else 1.4f)
                    },
                    highlight = { Highlight(alpha = if (isDark) 0.50f else 0.70f) },
                    shadow = {
                        Shadow(
                            radius = if (isTablet) M3EDesignSystem.Spacing.sm else M3EDesignSystem.Spacing.md,
                            color = Color.Black.copy(alpha = if (isDark) 0.18f else 0.08f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = M3EDesignSystem.Spacing.xs,
                            color = Color.White.copy(alpha = if (isDark) 0.06f else 0.12f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(tintColor)
                    }
                ),
            content = content
        )
    } else {
        // ── Fallback for older devices ─────────────────────────
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isTablet) 0.9f else 0.95f),
            tonalElevation = if (isTablet) 0.dp else 4.dp,
            shadowElevation = if (isTablet) 0.dp else 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isTablet) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                        else Modifier
                    ),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS TOGGLE
// ═══════════════════════════════════════════════════════════════

/**
 * A settings-style toggle row with a **Liquid Glass** aesthetic.
 *
 * Uses the kyant0/backdrop library for real-time backdrop blur on the card
 * background and a custom frosted-glass thumb on the switch.
 *
 * On older devices (< API 31) the glass card gracefully degrades to a
 * semi-transparent surface while keeping the same layout and colours.
 *
 * @param title Primary label
 * @param description Secondary label
 * @param icon Leading icon
 * @param isChecked Current toggle state
 * @param enabled Whether the toggle is interactive
 * @param onCheckedChange Callback when toggle changes
 */
@Composable
fun LiquidGlassToggle(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val shape = M3EDesignSystem.Shapes.MediumShape

    // Animate tint based on checked state
    val checkedTint by animateColorAsState(
        targetValue = if (isChecked && enabled) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(250),
        label = "checkedTint"
    )

    val cardTint = MaterialTheme.colorScheme.surface.copy(
        alpha = if (isDark) 0.50f else 0.60f
    )

    val iconTint by animateColorAsState(
        targetValue = if (isChecked && enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "iconTint"
    )

    if (supportsBackdropBlur()) {
        // ── Glass card with backdrop blur ──────────────────────
        val canvasBackdrop = rememberCanvasBackdrop {
            drawRect(cardTint)
            drawRect(checkedTint)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .semantics(mergeDescendants = true) {
                    role = Role.Switch
                    contentDescription =
                        "$title, $description, ${if (isChecked) "enabled" else "disabled"}"
                }
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCheckedChange(!isChecked) }
                .drawBackdrop(
                    backdrop = canvasBackdrop,
                    shape = { shape },
                    effects = {
                        blur(radius = 32f)
                        colorControls(saturation = if (isDark) 1.15f else 1.3f)
                    },
                    highlight = { Highlight(alpha = 0.65f) },
                    shadow = {
                        Shadow(
                            radius = M3EDesignSystem.Spacing.sm,
                            color = Color.Black.copy(alpha = if (isDark) 0.14f else 0.06f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = M3EDesignSystem.Spacing.xs,
                            color = Color.White.copy(alpha = if (isDark) 0.04f else 0.10f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(cardTint)
                        drawRect(checkedTint)
                    }
                )
                .padding(horizontal = M3EDesignSystem.Spacing.md, vertical = 14.dp)
        ) {
            LiquidGlassToggleContent(
                title = title,
                description = description,
                icon = icon,
                iconTint = iconTint,
                isChecked = isChecked,
                enabled = enabled,
                alpha = alpha,
                isDark = isDark
            )
        }
    } else {
        // ── Fallback: card without real blur ───────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .semantics(mergeDescendants = true) {
                    role = Role.Switch
                    contentDescription =
                        "$title, $description, ${if (isChecked) "enabled" else "disabled"}"
                },
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
                    .drawBehind { drawRect(checkedTint) }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                LiquidGlassToggleContent(
                    title = title,
                    description = description,
                    icon = icon,
                    iconTint = iconTint,
                    isChecked = isChecked,
                    enabled = enabled,
                    alpha = alpha,
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * Shared inner content for both backdrop-blur and fallback paths of [LiquidGlassToggle].
 */
@Composable
private fun LiquidGlassToggleContent(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    enabled: Boolean,
    alpha: Float,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .scale(alpha),
            tint = iconTint
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // ── Frosted glass toggle thumb ─────────────────────
        LiquidGlassSwitch(
            checked = isChecked,
            enabled = enabled,
            isDark = isDark
        )
    }
}

/**
 * A custom Switch styled with a Liquid Glass aesthetic.
 *
 * The track is a frosted pill shape with backdrop blur, and the thumb
 * is a translucent circle with an inner highlight that slides smoothly.
 */
@Composable
private fun LiquidGlassSwitch(
    checked: Boolean,
    enabled: Boolean,
    isDark: Boolean
) {
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    val thumbPadding = 3.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding * 2 else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked && enabled) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.25f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.12f else 0.08f)
        },
        animationSpec = tween(250),
        label = "trackColor"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked && enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(250),
        label = "thumbColor"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.85f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "thumbScale"
    )

    val trackShape = RoundedCornerShape(50)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(trackShape)
            .background(trackColor)
            .border(width = 0.5.dp, color = borderColor, shape = trackShape)
            .padding(thumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        // ── Thumb ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .scale(thumbScale)
                .shadow(
                    elevation = if (checked) 4.dp else 2.dp,
                    shape = CircleShape,
                    ambientColor = thumbColor.copy(alpha = 0.3f),
                    spotColor = thumbColor.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            thumbColor,
                            thumbColor.copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = if (isDark) 0.18f else 0.30f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Check icon when toggled on
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun BrandPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val shape = RoundedCornerShape(999.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS NAVIGATION BAR
// ═══════════════════════════════════════════════════════════════

/**
 * Data class for a single navigation destination inside [LiquidGlassNavigationBar].
 */
data class GlassNavItem(
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String,
    val badgeCount: Int = 0
)

internal fun isSkeumorphicVariant(variant: String): Boolean =
    variant == "semi_skeumorphic" || variant == "full_skeumorphic"

internal fun isFullSkeumorphicVariant(variant: String): Boolean =
    variant == "full_skeumorphic"

internal data class SkeuomorphicPalette(
    val surfaceTop: Color,
    val surfaceMid: Color,
    val surfaceBottom: Color,
    val border: Color,
    val shadow: Color,
    val highlight: Color,
    val innerShadow: Color,
    val accent: Color,
    val activeTop: Color,
    val activeBottom: Color
)

@Composable
internal fun rememberSkeuomorphicPalette(variant: String): SkeuomorphicPalette {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.surface.luminance() < 0.5f
    val isFull = isFullSkeumorphicVariant(variant)
    val base = lerp(scheme.surface, scheme.surfaceVariant, if (isFull) 0.56f else 0.34f)
    val accent = lerp(scheme.primary, scheme.secondary, if (isFull) 0.36f else 0.20f)
    return SkeuomorphicPalette(
        surfaceTop = lerp(base, Color.White, if (isDark) 0.06f else if (isFull) 0.24f else 0.16f),
        surfaceMid = base,
        surfaceBottom = lerp(base, accent, if (isFull) 0.28f else 0.14f),
        border = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = if (isFull) 0.38f else 0.26f),
        shadow = if (isDark) Color.Black.copy(alpha = if (isFull) 0.34f else 0.24f) else Color.Black.copy(alpha = if (isFull) 0.12f else 0.08f),
        highlight = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = if (isFull) 0.22f else 0.14f),
        innerShadow = if (isDark) Color.Black.copy(alpha = if (isFull) 0.18f else 0.10f) else accent.copy(alpha = if (isFull) 0.12f else 0.06f),
        accent = accent,
        activeTop = lerp(base, Color.White, if (isDark) 0.08f else 0.26f),
        activeBottom = lerp(base, accent, if (isFull) 0.34f else 0.20f)
    )
}

@Composable
internal fun SkeuomorphicPanel(
    modifier: Modifier = Modifier,
    shape: Shape,
    variant: String,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = rememberSkeuomorphicPalette(variant)
    val isFull = isFullSkeumorphicVariant(variant)
    Box(
        modifier = modifier
            .shadow(if (isFull) 20.dp else 12.dp, shape, clip = false)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(palette.surfaceTop, palette.surfaceMid, palette.surfaceBottom),
                    start = Offset.Zero,
                    end = Offset.Infinite
                ),
                shape = shape
            )
            .border(1.dp, palette.border, shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(palette.highlight, Color.Transparent),
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, palette.innerShadow),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        )
        Box(content = content)
    }
}

/**
 * A bottom navigation bar rendered on top of a [NeuroGlassPanel] with a
 * smoothly-animating frosted-glass slider pill that tracks the selected tab.
 *
 * The slider glides behind the selected item using a fast spring animation,
 * its width snapping to each item's allocated column.  The pill itself uses
 * the kyant0/backdrop [drawBackdrop] API so it looks like glass-on-glass.
 *
 * @param items           Ordered list of navigation destinations.
 * @param selectedIndex   Currently selected tab (0-based).
 * @param onItemSelected  Callback when a tab is tapped.
 * @param variant         "frosted" (neutral tint) or "aurora" (accent-tinted).
 * @param modifier        Standard Modifier.
 */
@Composable
fun LiquidGlassNavigationBar(
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    variant: String = "frosted",
    modifier: Modifier = Modifier
) {
    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicNavigationBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = onItemSelected,
            variant = variant,
            modifier = modifier
        )
        return
    }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // iOS 26–style floating pill: full rounded corners, horizontal margins,
    // sits above the system nav-bar area with spacing.
    NeuroGlassPanel(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        blurRadius = 48f,
        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.55f else 0.65f),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    val itemCount = items.size.coerceAtLeast(1)

                    // ── Sliding glass indicator pill ──────────────────
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val totalWidth = maxWidth
                        val pillWidth = totalWidth / itemCount
                        val targetOffset = pillWidth * selectedIndex

                        // Fluid glass-like spring: medium stiffness + low bounce
                        val animatedOffset by animateDpAsState(
                            targetValue = targetOffset,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = 400f
                            ),
                            label = "sliderOffset"
                        )

                        val pillTint = if (variant == "aurora") {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.20f else 0.14f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.08f else 0.05f)
                        }

                        val pillShape = RoundedCornerShape(22.dp)

                        if (supportsBackdropBlur()) {
                            val pillBackdrop = rememberCanvasBackdrop { drawRect(pillTint) }
                            val pillShadowColor = if (variant == "aurora")
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                Color.Black.copy(alpha = if (isDark) 0.10f else 0.04f)

                            Box(
                                modifier = Modifier
                                    .offset(x = animatedOffset)
                                    .width(pillWidth)
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .drawBackdrop(
                                        backdrop = pillBackdrop,
                                        shape = { pillShape },
                                        effects = {
                                            blur(radius = 20f)
                                            colorControls(saturation = if (isDark) 1.15f else 1.3f)
                                        },
                                        highlight = { Highlight(alpha = if (isDark) 0.45f else 0.65f) },
                                        shadow = {
                                            Shadow(
                                                radius = 6.dp,
                                                color = pillShadowColor
                                            )
                                        },
                                        innerShadow = {
                                            InnerShadow(
                                                radius = 4.dp,
                                                color = Color.White.copy(alpha = if (isDark) 0.04f else 0.10f)
                                            )
                                        },
                                        onDrawSurface = { drawRect(pillTint) }
                                    )
                            )
                        } else {
                            val sliderBorder = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.30f)
                            Box(
                                modifier = Modifier
                                    .offset(x = animatedOffset)
                                    .width(pillWidth)
                                    .fillMaxHeight()
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .clip(pillShape)
                                    .background(pillTint)
                                    .border(0.5.dp, sliderBorder, pillShape)
                            )
                        }
                    }

                    // ── Actual nav items ──────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEachIndexed { index, item ->
                            val selected = index == selectedIndex
                            val itemColor by animateColorAsState(
                                targetValue = if (selected) {
                                    if (variant == "aurora") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                animationSpec = tween(180),
                                label = "navItemColor_$index"
                            )
                            val itemScale by animateFloatAsState(
                                targetValue = if (selected) 1.04f else 1f,
                                animationSpec = spring(
                                    dampingRatio = 0.7f,
                                    stiffness = 600f
                                ),
                                label = "navItemScale_$index"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { onItemSelected(index) }
                                    .scale(itemScale),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp),
                                        tint = itemColor
                                    )
                                    // Badge
                                    if (item.badgeCount > 0) {
                                        Badge(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 10.dp, y = (-4).dp)
                                        ) {
                                            Text(
                                                text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.size(2.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = itemColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SkeuomorphicNavigationBar(
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    variant: String,
    modifier: Modifier = Modifier
) {
    val palette = rememberSkeuomorphicPalette(variant)
    val isFull = isFullSkeumorphicVariant(variant)

    SkeuomorphicPanel(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        variant = variant
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                val itemCount = items.size.coerceAtLeast(1)
                val pillWidth = totalWidth / itemCount
                val targetOffset = pillWidth * selectedIndex
                val animatedOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 460f),
                    label = "skeuoNavOffset"
                )

                SkeuomorphicPanel(
                    modifier = Modifier
                        .offset(x = animatedOffset)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    variant = if (isFull) "full_skeumorphic" else "semi_skeumorphic"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        palette.activeTop.copy(alpha = 0.92f),
                                        palette.activeBottom.copy(alpha = 0.96f)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                )
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    val itemColor by animateColorAsState(
                        targetValue = if (selected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(180),
                        label = "skeuoNavColor_$index"
                    )
                    val itemScale by animateFloatAsState(
                        targetValue = if (selected) 1.04f else 1f,
                        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
                        label = "skeuoNavScale_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onItemSelected(index) }
                            .scale(itemScale),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp),
                                tint = itemColor
                            )
                            if (item.badgeCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 10.dp, y = (-4).dp)
                                ) {
                                    Text(
                                        text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.size(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = itemColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS TOP APP BAR
// ═══════════════════════════════════════════════════════════════

/**
 * A top app bar with a Liquid Glass backdrop – matches how iOS 26
 * renders its navigation bar.  The glass spans edge-to-edge including
 * the status bar area.
 *
 * @param title           Primary title composable.
 * @param navigationIcon  Optional leading icon (e.g. back arrow).
 * @param actions         Trailing actions row.
 * @param variant         "frosted" (neutral) or "aurora" (accent-tinted).
 */
@Composable
fun LiquidGlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    variant: String = "frosted"
) {
    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            variant = variant
        )
        return
    }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    NeuroGlassPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        blurRadius = 40f,
        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.55f else 0.65f),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Status bar spacer
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                )
                // Toolbar row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navigationIcon()
                    Box(
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val titleColor = if (variant == "aurora")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                        CompositionLocalProvider(
                            LocalContentColor provides titleColor
                        ) {
                            title()
                        }
                    }
                    actions()
                }
            }
        }
    )
}

@Composable
private fun SkeuomorphicTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    variant: String
) {
    val palette = rememberSkeuomorphicPalette(variant)
    SkeuomorphicPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        variant = variant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationIcon()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CompositionLocalProvider(LocalContentColor provides palette.accent) {
                        title()
                    }
                }
                actions()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS FAB
// ═══════════════════════════════════════════════════════════════

/**
 * A floating action button with a Liquid Glass aesthetic – circular
 * frosted glass matching iOS 26's treatment of prominent actions.
 *
 * @param icon       The glyph to display inside the FAB.
 * @param onClick    Tap callback.
 * @param variant    "frosted" or "aurora".
 * @param contentDescription Accessibility label.
 */
@Composable
fun LiquidGlassFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: String = "frosted",
    contentDescription: String? = null
) {
    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicFAB(
            icon = icon,
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            contentDescription = contentDescription
        )
        return
    }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val iconTint = if (variant == "aurora")
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface

    NeuroGlassPanel(
        modifier = modifier
            .size(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        blurRadius = 32f,
        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.60f else 0.70f),
        content = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }
        }
    )
}

@Composable
private fun SkeuomorphicFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: String,
    contentDescription: String? = null
) {
    val palette = rememberSkeuomorphicPalette(variant)
    SkeuomorphicPanel(
        modifier = modifier
            .size(58.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        variant = variant
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = palette.accent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS BOTTOM SHEET CONTAINER
// ═══════════════════════════════════════════════════════════════

/**
 * A glass-morphism wrapper for bottom-sheet content.
 *
 * Wrap the inner [Column] / content of a [ModalBottomSheet] in this
 * composable to give the sheet an iOS 26-style frosted glass look.
 *
 * @param variant "frosted" or "aurora".
 * @param content The sheet body.
 */
@Composable
fun LiquidGlassSheetContent(
    modifier: Modifier = Modifier,
    variant: String = "frosted",
    content: @Composable BoxScope.() -> Unit
) {
    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicSheetContent(
            modifier = modifier,
            variant = variant,
            content = content
        )
        return
    }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    NeuroGlassPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        blurRadius = 48f,
        tintColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.60f else 0.72f),
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                    )
                }
                // Actual sheet body
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    content = content
                )
            }
        }
    )
}

@Composable
private fun SkeuomorphicSheetContent(
    modifier: Modifier = Modifier,
    variant: String,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = rememberSkeuomorphicPalette(variant)
    SkeuomorphicPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        variant = variant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(palette.highlight, palette.innerShadow)
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                content = content
            )
        }
    }
}

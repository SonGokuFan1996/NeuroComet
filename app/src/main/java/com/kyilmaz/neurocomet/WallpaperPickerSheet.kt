@file:OptIn(ExperimentalMaterial3Api::class)

package com.kyilmaz.neurocomet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

// =============================================================================
// WALLPAPER PICKER — Bottom sheet for selecting conversation wallpapers
// =============================================================================

/**
 * Modal bottom sheet that lets users pick a neurodivergent-friendly wallpaper
 * for their message conversations.
 *
 * @param currentWallpaper The currently active wallpaper key.
 * @param conversationId If non-null, allows per-conversation override.
 * @param onSelect Called when a wallpaper is selected. Passes the enum and whether it's per-conversation.
 * @param onDismiss Called when the sheet should close.
 */
@Composable
fun WallpaperPickerSheet(
    currentWallpaper: ConversationWallpaper,
    conversationId: String? = null,
    onSelect: (ConversationWallpaper, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val reducedMotion = SettingsManager.reducedMotion
    var perConversationOnly by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Chat Wallpaper",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Choose a sensory-friendly background for your conversations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Wallpaper grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(ConversationWallpaper.entries.toList()) { wallpaper ->
                    WallpaperPreviewCard(
                        wallpaper = wallpaper,
                        isSelected = wallpaper == currentWallpaper,
                        isDark = isDark,
                        reducedMotion = reducedMotion,
                        onClick = { onSelect(wallpaper, perConversationOnly) }
                    )
                }
            }

            // Per-conversation toggle (only if we have a conversation context)
            if (conversationId != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { perConversationOnly = !perConversationOnly }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apply to this chat only",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Other conversations will keep their current wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = perConversationOnly,
                        onCheckedChange = { perConversationOnly = it }
                    )
                }
            }

            // Reduced motion hint
            if (reducedMotion) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Animations paused — Reduced Motion is enabled in Accessibility settings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// =============================================================================
// Preview Card — Shows a live mini-preview of each wallpaper
// =============================================================================

@Composable
private fun WallpaperPreviewCard(
    wallpaper: ConversationWallpaper,
    isSelected: Boolean,
    isDark: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Mini animation for preview thumbnails
    val transition = rememberInfiniteTransition(label = "preview_${wallpaper.name}")
    val previewPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "previewPhase"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 2.5.dp,
                    color = primaryColor,
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Draw mini wallpaper preview
                    drawWallpaperPreview(wallpaper, previewPhase, isDark)
                },
            contentAlignment = Alignment.Center
        ) {
            // Overlay content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                // Selected checkmark
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                } else {
                    Text(
                        text = wallpaper.emoji,
                        fontSize = 28.sp
                    )
                    Spacer(Modifier.height(2.dp))
                }

                Text(
                    text = wallpaper.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = getPreviewTextColor(wallpaper, isDark),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = getPreviewTextColor(wallpaper, isDark).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Get appropriate text color for readability on each wallpaper preview.
 */
private fun getPreviewTextColor(wallpaper: ConversationWallpaper, isDark: Boolean): Color {
    return when {
        wallpaper == ConversationWallpaper.NONE -> if (isDark) Color.White else Color(0xFF1A1A2E)
        wallpaper == ConversationWallpaper.DEEP_FOCUS && isDark -> Color.White.copy(alpha = 0.9f)
        wallpaper == ConversationWallpaper.STARFIELD && isDark -> Color.White.copy(alpha = 0.9f)
        wallpaper == ConversationWallpaper.AURORA_BOREALIS && isDark -> Color.White.copy(alpha = 0.9f)
        isDark -> Color.White.copy(alpha = 0.85f)
        else -> Color(0xFF1A1A2E)
    }
}

// =============================================================================
// Mini preview renderers (simplified versions for the picker thumbnails)
// =============================================================================

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWallpaperPreview(
    wallpaper: ConversationWallpaper,
    phase: Float,
    isDark: Boolean
) {
    when (wallpaper) {
        ConversationWallpaper.NONE -> {
            drawRect(color = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF5F5F5))
        }
        ConversationWallpaper.CALM_OCEAN -> {
            val bg = if (isDark) listOf(Color(0xFF0D1B2A), Color(0xFF1B2838)) else listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
            drawRect(brush = Brush.verticalGradient(bg))
            val waveColor = if (isDark) Color(0xFF42A5F5) else Color(0xFF1976D2)
            val path = Path().apply {
                moveTo(0f, size.height * 0.6f)
                val p = phase * 2f * PI.toFloat()
                for (x in 0..size.width.toInt() step 6) {
                    lineTo(x.toFloat(), size.height * 0.6f + sin(x / size.width * 4f * PI.toFloat() + p) * size.height * 0.06f)
                }
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(path, color = waveColor.copy(alpha = 0.15f))
        }
        ConversationWallpaper.AURORA_BOREALIS -> {
            drawRect(color = if (isDark) Color(0xFF0A0E1A) else Color(0xFFF5F5F5))
            val c = if (isDark) Color(0xFF00E676) else Color(0xFF81C784)
            val y = size.height * (0.2f + sin(phase * 2f * PI.toFloat()) * 0.1f)
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, c.copy(alpha = 0.15f), Color.Transparent), startY = y, endY = y + size.height * 0.3f))
        }
        ConversationWallpaper.BREATHING_BUBBLES -> {
            drawRect(color = if (isDark) Color(0xFF1A1A2E) else Color(0xFFF8F0FF))
            val scale = 0.7f + phase * 0.3f
            val alpha = 0.08f + phase * 0.08f
            val c1 = if (isDark) Color(0xFF7C4DFF) else Color(0xFFCE93D8)
            val c2 = if (isDark) Color(0xFF00BFA5) else Color(0xFFA5D6A7)
            drawCircle(c1.copy(alpha = alpha), size.width * 0.15f * scale, Offset(size.width * 0.3f, size.height * 0.35f))
            drawCircle(c2.copy(alpha = alpha), size.width * 0.12f * scale, Offset(size.width * 0.7f, size.height * 0.60f))
        }
        ConversationWallpaper.SOFT_RAIN -> {
            drawRect(color = if (isDark) Color(0xFF1A1D23) else Color(0xFFF0F4F8))
            val dc = if (isDark) Color(0xFF90CAF9) else Color(0xFF64B5F6)
            for (i in 0 until 12) {
                val x = ((i * 0.618f) % 1f) * size.width
                val y = ((i * 0.314f + phase * 0.8f) % 1f) * size.height
                drawCircle(dc.copy(alpha = 0.18f), 1.5f, Offset(x, y))
            }
        }
        ConversationWallpaper.DEEP_FOCUS -> {
            val c = if (isDark) listOf(Color(0xFF0D0D12), Color(0xFF1A1A24), Color(0xFF0D0D12)) else listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE8E8E8))
            drawRect(brush = Brush.radialGradient(c, center = Offset(size.width * 0.5f, size.height * 0.4f), radius = size.width * 0.8f))
        }
        ConversationWallpaper.WARM_SUNSET -> {
            val p = phase * 2f * PI.toFloat()
            val c = if (isDark) listOf(Color(0xFF2A1010), Color(0xFF4A1A00), Color(0xFF1A0A15)) else listOf(Color(0xFFFFF3E0), Color(0xFFFFCCBC), Color(0xFFFFE0B2))
            drawRect(brush = Brush.radialGradient(c, center = Offset(size.width * 0.5f, size.height * (0.35f + sin(p) * 0.1f)), radius = size.width))
        }
        ConversationWallpaper.STARFIELD -> {
            val bg = if (isDark) listOf(Color(0xFF0A0A1A), Color(0xFF0F0F2A)) else listOf(Color(0xFFE8EAF6), Color(0xFFEDE7F6))
            drawRect(brush = Brush.verticalGradient(bg))
            val sc = if (isDark) Color.White else Color(0xFF5C6BC0)
            for (i in 0 until 15) {
                val x = ((i * 0.618f * 7f) % 1f) * size.width
                val y = ((i * 0.382f * 13f) % 1f) * size.height
                val twinkle = sin(phase * PI.toFloat() + i * 0.5f)
                drawCircle(sc.copy(alpha = (0.2f + twinkle * 0.4f).coerceIn(0.05f, 0.8f)), 1f + (i % 3) * 0.3f, Offset(x, y))
            }
        }
    }
}


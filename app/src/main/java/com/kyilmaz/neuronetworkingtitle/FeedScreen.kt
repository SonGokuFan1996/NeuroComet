@file:Suppress(
    "UnusedImport",
    "AssignmentToStateVariable",
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "UNUSED_PARAMETER"
)

package com.kyilmaz.neuronetworkingtitle

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition // Restored missing import
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

// Explicit imports for symbols in the same package (to address compiler ambiguity in various contexts)
import com.kyilmaz.neuronetworkingtitle.SafetyState
import com.kyilmaz.neuronetworkingtitle.ContentFiltering

/**
 * Holiday type enum for themed logo variations
 */
enum class HolidayType {
    NONE,
    NEW_YEAR,           // January 1
    VALENTINES,         // February 14
    ST_PATRICKS,        // March 17
    EASTER,             // Spring (varies)
    EARTH_DAY,          // April 22
    PRIDE_MONTH,        // June
    INDEPENDENCE_DAY,   // July 4 (US)
    HALLOWEEN,          // October 31
    THANKSGIVING,       // November (4th Thursday)
    HANUKKAH,           // Varies (Jewish calendar)
    CHRISTMAS,          // December 25
    AUTISM_AWARENESS,   // April 2 - World Autism Awareness Day
    ADHD_AWARENESS,     // October - ADHD Awareness Month
    NEURODIVERSITY      // Neurodiversity Celebration Week (March)
}

/**
 * Detect current holiday based on date
 */
private fun detectCurrentHoliday(): HolidayType {
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH) // 0-indexed
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val weekOfMonth = calendar.get(java.util.Calendar.WEEK_OF_MONTH)

    return when {
        // New Year's Day (Jan 1) or New Year's Eve (Dec 31)
        (month == 0 && day == 1) || (month == 11 && day == 31) -> HolidayType.NEW_YEAR

        // Valentine's Day (Feb 14) ± 1 day
        month == 1 && day in 13..15 -> HolidayType.VALENTINES

        // St. Patrick's Day (March 17) ± 1 day
        month == 2 && day in 16..18 -> HolidayType.ST_PATRICKS

        // Neurodiversity Celebration Week (mid-March)
        month == 2 && day in 13..19 -> HolidayType.NEURODIVERSITY

        // World Autism Awareness Day (April 2)
        month == 3 && day == 2 -> HolidayType.AUTISM_AWARENESS

        // Earth Day (April 22)
        month == 3 && day == 22 -> HolidayType.EARTH_DAY

        // Easter season (approximate - late March to late April)
        month == 3 && day in 1..21 -> HolidayType.EASTER

        // Pride Month (June)
        month == 5 -> HolidayType.PRIDE_MONTH

        // Independence Day (July 4) ± 1 day
        month == 6 && day in 3..5 -> HolidayType.INDEPENDENCE_DAY

        // ADHD Awareness Month (October)
        month == 9 && day in 1..7 -> HolidayType.ADHD_AWARENESS

        // Halloween (Oct 31) or week before
        month == 9 && day in 24..31 -> HolidayType.HALLOWEEN

        // Thanksgiving (4th Thursday of November)
        month == 10 && dayOfWeek == java.util.Calendar.THURSDAY && weekOfMonth == 4 -> HolidayType.THANKSGIVING

        // Hanukkah (approximate - late November to late December)
        month == 11 && day in 1..24 -> HolidayType.HANUKKAH

        // Christmas season (Dec 24-26)
        month == 11 && day in 24..26 -> HolidayType.CHRISTMAS

        else -> HolidayType.NONE
    }
}

/**
 * Get holiday-specific colors for the logo
 * Colors are softened and balanced to be less "in your face"
 * and more neurodivergent-friendly (avoiding harsh saturation)
 */
private fun getHolidayColors(holiday: HolidayType): List<Color> {
    return when (holiday) {
        HolidayType.NEW_YEAR -> listOf(
            Color(0xFFD4AF37), // Soft gold
            Color(0xFFB8B8B8), // Soft silver
            Color(0xFFF5F5F5), // Off-white
            Color(0xFFD4AF37), // Soft gold
            Color(0xFF9370DB), // Muted purple
            Color(0xFFD4AF37)  // Soft gold
        )
        HolidayType.VALENTINES -> listOf(
            Color(0xFFDB7093), // Pale violet red
            Color(0xFFF08080), // Light coral
            Color(0xFFFFB6C1), // Light pink
            Color(0xFFCD5C5C), // Indian red (soft)
            Color(0xFFDB7093), // Pale violet red
            Color(0xFFBC8F8F)  // Rosy brown
        )
        HolidayType.ST_PATRICKS -> listOf(
            Color(0xFF6B8E23), // Olive drab
            Color(0xFF8FBC8F), // Dark sea green
            Color(0xFF98FB98), // Pale green
            Color(0xFFD4AF37), // Soft gold
            Color(0xFF6B8E23), // Olive drab
            Color(0xFF8FBC8F)  // Dark sea green
        )
        HolidayType.EASTER -> listOf(
            Color(0xFFFFB6C1), // Light pink
            Color(0xFFE6E6FA), // Lavender
            Color(0xFF98FB98), // Pale green
            Color(0xFFF0E68C), // Khaki (soft yellow)
            Color(0xFFB0E0E6), // Powder blue
            Color(0xFFFFB6C1)  // Light pink
        )
        HolidayType.EARTH_DAY -> listOf(
            Color(0xFF6B8E23), // Olive drab
            Color(0xFF6495ED), // Cornflower blue
            Color(0xFF8FBC8F), // Dark sea green
            Color(0xFF5F9EA0), // Cadet blue
            Color(0xFF6B8E23), // Olive drab
            Color(0xFF6495ED)  // Cornflower blue
        )
        HolidayType.PRIDE_MONTH -> listOf(
            Color(0xFFE57373), // Soft red
            Color(0xFFFFB74D), // Soft orange
            Color(0xFFFFF176), // Soft yellow
            Color(0xFF81C784), // Soft green
            Color(0xFF64B5F6), // Soft blue
            Color(0xFFBA68C8), // Soft violet
            Color(0xFFE57373)  // Soft red
        )
        HolidayType.INDEPENDENCE_DAY -> listOf(
            Color(0xFFCD5C5C), // Indian red (soft)
            Color(0xFFF5F5F5), // Off-white
            Color(0xFF6495ED), // Cornflower blue
            Color(0xFFF5F5F5), // Off-white
            Color(0xFFCD5C5C), // Indian red (soft)
            Color(0xFF6495ED)  // Cornflower blue
        )
        HolidayType.HALLOWEEN -> listOf(
            Color(0xFFE9967A), // Dark salmon (soft orange)
            Color(0xFF4A4A4A), // Dark gray (soft black)
            Color(0xFF9370DB), // Medium purple
            Color(0xFFE9967A), // Dark salmon
            Color(0xFF8FBC8F), // Dark sea green
            Color(0xFFE9967A)  // Dark salmon
        )
        HolidayType.THANKSGIVING -> listOf(
            Color(0xFFE9967A), // Dark salmon
            Color(0xFFA0522D), // Sienna
            Color(0xFFD4AF37), // Soft gold
            Color(0xFFBC8F8F), // Rosy brown
            Color(0xFFE9967A), // Dark salmon
            Color(0xFFA0522D)  // Sienna
        )
        HolidayType.HANUKKAH -> listOf(
            Color(0xFF6495ED), // Cornflower blue
            Color(0xFFF5F5F5), // Off-white
            Color(0xFFB0C4DE), // Light steel blue
            Color(0xFFD4AF37), // Soft gold
            Color(0xFF6495ED), // Cornflower blue
            Color(0xFFF5F5F5)  // Off-white
        )
        HolidayType.CHRISTMAS -> listOf(
            Color(0xFFCD5C5C), // Indian red (soft)
            Color(0xFF6B8E23), // Olive drab (soft green)
            Color(0xFFD4AF37), // Soft gold
            Color(0xFFCD5C5C), // Indian red
            Color(0xFF6B8E23), // Olive drab
            Color(0xFFD4AF37)  // Soft gold
        )
        HolidayType.AUTISM_AWARENESS -> listOf(
            Color(0xFFE57373), // Soft red
            Color(0xFFD4AF37), // Soft gold
            Color(0xFF81C784), // Soft green
            Color(0xFF64B5F6), // Soft blue
            Color(0xFFE57373), // Soft red
            Color(0xFFD4AF37)  // Soft gold
        )
        HolidayType.ADHD_AWARENESS -> listOf(
            Color(0xFFFFB74D), // Soft orange
            Color(0xFFD4AF37), // Soft gold
            Color(0xFFE9967A), // Dark salmon
            Color(0xFFFFA726), // Muted orange
            Color(0xFFFFB74D), // Soft orange
            Color(0xFFD4AF37)  // Soft gold
        )
        HolidayType.NEURODIVERSITY -> listOf(
            Color(0xFFE57373), // Soft red
            Color(0xFFFFB74D), // Soft orange
            Color(0xFFFFF176), // Soft yellow
            Color(0xFF81C784), // Soft green
            Color(0xFF64B5F6), // Soft blue
            Color(0xFFBA68C8), // Soft purple
            Color(0xFFF48FB1), // Soft pink
            Color(0xFFE57373)  // Loop back
        )
        HolidayType.NONE -> listOf(
            Color(0xFFE57373), // Soft red
            Color(0xFFFFB74D), // Soft orange
            Color(0xFFFFF176), // Soft yellow
            Color(0xFF81C784), // Soft green
            Color(0xFF64B5F6), // Soft blue
            Color(0xFFBA68C8), // Soft purple
            Color(0xFFF48FB1), // Soft pink
            Color(0xFFE57373)  // Loop back
        )
    }
}

/**
 * Get holiday-specific emoji/icon for logo
 */
private fun getHolidayEmoji(holiday: HolidayType): String? {
    return when (holiday) {
        HolidayType.NEW_YEAR -> "🎆"
        HolidayType.VALENTINES -> "💖"
        HolidayType.ST_PATRICKS -> "☘️"
        HolidayType.EASTER -> "🐣"
        HolidayType.EARTH_DAY -> "🌍"
        HolidayType.PRIDE_MONTH -> "🏳️‍🌈"
        HolidayType.INDEPENDENCE_DAY -> "🎆"
        HolidayType.HALLOWEEN -> "🎃"
        HolidayType.THANKSGIVING -> "🦃"
        HolidayType.HANUKKAH -> "🕎"
        HolidayType.CHRISTMAS -> "🎄"
        HolidayType.AUTISM_AWARENESS -> "♾️"
        HolidayType.ADHD_AWARENESS -> "🧠"
        HolidayType.NEURODIVERSITY -> "🌈"
        HolidayType.NONE -> null
    }
}

/**
 * Neurodivergent-centric logo for NeuroNet with holiday theming.
 * Features the rainbow infinity symbol and flashy animated text.
 * The infinity symbol represents the infinite variations of the human mind.
 *
 * Holiday variants provide themed colors while maintaining the core neurodiversity identity.
 */
@Composable
private fun NeuroNetLogo(
    modifier: Modifier = Modifier,
    animateLogos: Boolean = true
) {
    val currentHoliday = remember { detectCurrentHoliday() }
    val holidayEmoji = remember { getHolidayEmoji(currentHoliday) }
    val holidayColors = remember { getHolidayColors(currentHoliday) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Holiday emoji (if applicable)
        if (holidayEmoji != null) {
            Text(
                text = holidayEmoji,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // Rainbow Infinity Symbol - The Neurodiversity Symbol (with holiday colors)
        RainbowInfinitySymbol(
            modifier = Modifier.size(32.dp),
            animated = animateLogos,
            colors = holidayColors
        )

        // Flashy animated app name text (with holiday colors)
        FlashyNeuroNetText(
            animated = animateLogos,
            colors = holidayColors
        )
    }
}

/**
 * Flashy animated NeuroNet text with rainbow gradient and shimmer effect.
 * Supports custom colors for holiday theming.
 * When static (not animated), uses a simpler two-tone gradient for cleaner appearance.
 */
@Composable
private fun FlashyNeuroNetText(
    animated: Boolean = true,
    colors: List<Color> = listOf(
        Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176),
        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8),
        Color(0xFFF48FB1), Color(0xFFE57373)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neuronet-text")

    // Shimmer animation for the text - only animate if enabled
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 1000f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 2500 else 1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Gentle scale pulse - only animate if enabled
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animated) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 2000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // For static logos, use a simpler two-tone gradient that looks clean without animation
    // For animated logos, use the full color palette
    val staticColors = listOf(
        colors.getOrElse(0) { Color(0xFF64B5F6) },  // Primary color
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) }  // Secondary color
    )

    // Create brush - static uses simple gradient, animated uses flowing shimmer
    val brush = if (animated) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(shimmerOffset - 500f, 0f),
            end = Offset(shimmerOffset, 0f)
        )
    } else {
        // Static: clean horizontal gradient with just 2-3 colors
        Brush.horizontalGradient(staticColors)
    }

    // Get glow color from the palette (use middle color for glow)
    val glowColor = if (animated) {
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) }
    } else {
        staticColors.first().copy(alpha = 0.3f) // Subtle glow for static
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        // Shadow/glow layer - more subtle for static
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = glowColor.copy(alpha = if (animated) 0.5f else 0.25f),
                    offset = Offset(0f, if (animated) 2f else 1f),
                    blurRadius = if (animated) 8f else 4f
                )
            ),
            color = Color.Transparent
        )

        // Main gradient text
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                brush = brush
            )
        )
    }
}

/**
 * Rainbow infinity symbol - the universal symbol for neurodiversity.
 * Represents the infinite spectrum of neurological differences.
 * Features smooth animation with flowing gradient and gentle glow.
 * Supports custom colors for holiday theming.
 * When static, uses a simpler two-tone gradient for cleaner appearance.
 */
@Composable
fun RainbowInfinitySymbol(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    colors: List<Color> = listOf(
        Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176),
        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8),
        Color(0xFFF48FB1), Color(0xFFE57373)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinity-feed")

    // Smooth flowing gradient animation
    val flowPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 4000 else 1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPosition"
    )

    // Gentle breathing animation
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = if (animated) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 3000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // For static logos, use a simpler two-tone gradient
    // For animated logos, use the full color palette
    val staticColors = listOf(
        colors.getOrElse(0) { Color(0xFF64B5F6) },  // Primary color
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) },  // Secondary color
        colors.getOrElse(0) { Color(0xFF64B5F6) }   // Back to primary for smooth loop
    )

    val displayColors = if (animated) colors else staticColors

    Canvas(modifier = modifier) {
        val width = size.width * breathe
        val height = size.height * breathe
        val offsetX = (size.width - width) / 2
        val offsetY = (size.height - height) / 2
        val strokeWidth = height * 0.14f
        val centerX = width / 2 + offsetX
        val centerY = height / 2 + offsetY

        // Calculate control points for smoother curves
        val loopWidth = width * 0.35f
        val loopHeight = height * 0.38f

        // Create smooth infinity path
        val infinityPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(centerX, centerY)

            // Right loop - top curve
            cubicTo(
                x1 = centerX + loopWidth * 0.5f, y1 = centerY - loopHeight * 0.9f,
                x2 = centerX + loopWidth * 0.95f, y2 = centerY - loopHeight * 0.7f,
                x3 = centerX + loopWidth, y3 = centerY
            )

            // Right loop - bottom curve
            cubicTo(
                x1 = centerX + loopWidth * 0.95f, y1 = centerY + loopHeight * 0.7f,
                x2 = centerX + loopWidth * 0.5f, y2 = centerY + loopHeight * 0.9f,
                x3 = centerX, y3 = centerY
            )

            // Left loop - bottom curve
            cubicTo(
                x1 = centerX - loopWidth * 0.5f, y1 = centerY + loopHeight * 0.9f,
                x2 = centerX - loopWidth * 0.95f, y2 = centerY + loopHeight * 0.7f,
                x3 = centerX - loopWidth, y3 = centerY
            )

            // Left loop - top curve
            cubicTo(
                x1 = centerX - loopWidth * 0.95f, y1 = centerY - loopHeight * 0.7f,
                x2 = centerX - loopWidth * 0.5f, y2 = centerY - loopHeight * 0.9f,
                x3 = centerX, y3 = centerY
            )
        }

        // Create gradient - flowing for animated, simple horizontal for static
        val gradient = if (animated) {
            Brush.linearGradient(
                colors = displayColors,
                start = Offset(
                    centerX + kotlin.math.cos(Math.toRadians(flowPosition.toDouble())).toFloat() * width,
                    centerY + kotlin.math.sin(Math.toRadians(flowPosition.toDouble())).toFloat() * height
                ),
                end = Offset(
                    centerX + kotlin.math.cos(Math.toRadians((flowPosition + 180).toDouble())).toFloat() * width,
                    centerY + kotlin.math.sin(Math.toRadians((flowPosition + 180).toDouble())).toFloat() * height
                )
            )
        } else {
            // Static: simple horizontal gradient
            Brush.horizontalGradient(
                colors = displayColors,
                startX = offsetX,
                endX = offsetX + width
            )
        }

        drawPath(
            path = infinityPath,
            brush = gradient,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun FeedScreen(
    posts: List<Post>,
    stories: List<Story>,
    currentUser: User,
    onLikePost: (Long) -> Unit,
    onReplyPost: (Post) -> Unit,
    onSharePost: (Context, Post) -> Unit,
    onAddPost: (String, String, String?, String?) -> Unit,
    onDeletePost: (Long) -> Unit,
    @Suppress("unused") onProfileClick: () -> Unit,
    onViewStory: (Story) -> Unit,
    onAddStory: (String, Long) -> Unit,
    @Suppress("unused") isPremium: Boolean,
    @Suppress("unused") onUpgradeClick: () -> Unit,
    isMockInterfaceEnabled: Boolean,
    animationSettings: AnimationSettings = AnimationSettings(),
    modifier: Modifier = Modifier,
    safetyState: SafetyState = SafetyState()
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }
    val isPostingBlocked = shouldBlockFeature(parentalState, BlockableFeature.POSTING) != null

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var showPostingBlockedMessage by remember { mutableStateOf(false) }

    // Animation flags
    val animateLogos = animationSettings.shouldAnimate(AnimationType.LOGO)
    val animateStories = animationSettings.shouldAnimate(AnimationType.STORY)

    Column(modifier = modifier.fillMaxSize()) {
        // App Bar / Header with status bar padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()  // Prevent overlap with status bar
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Use the neurodivergent-centric logo
            NeuroNetLogo(animateLogos = animateLogos)

            IconButton(onClick = {
                if (isPostingBlocked) {
                    showPostingBlockedMessage = true
                } else {
                    showCreatePostDialog = true
                }
            }) {
                Icon(Icons.Default.Create, contentDescription = stringResource(R.string.create_post_title))
            }
        }

        // Horizontal divider
        HorizontalDivider()

        // Get navbar height for bottom padding
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        val navBarHeight = navBarPadding.calculateBottomPadding()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Bottom padding includes navbar height so content scrolls behind it
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 80.dp + navBarHeight // FAB/bottom bar + navbar
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stories Row at the top
            item(key = "stories_row") {
                StoriesRow(
                    stories = stories,
                    currentUser = currentUser,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    animateStories = animateStories,
                    onViewStory = onViewStory,
                    onAddStoryClick = { showCreateStoryDialog = true } // Pass callback to open story dialog
                )
            }

            // Divider before feed starts
            item(key = "feed_divider") { HorizontalDivider() }

            // Feed Posts - using stable keys for efficient recomposition
            items(
                items = posts,
                key = { post -> post.id ?: post.hashCode() }
            ) { post ->
                BubblyPostCard(
                    post = post,
                    onLike = { post.id?.let(onLikePost) },
                    onDelete = { post.id?.let(onDeletePost) },
                    onReplyPost = { onReplyPost(post) },
                    onShare = onSharePost,
                    isMockInterfaceEnabled = isMockInterfaceEnabled, // Passed down
                    safetyState = safetyState
                )
            }
        }
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    run {
        // In kids mode, disable creating posts/stories unless app is in mock mode.
        if (showCreatePostDialog) {
            EnhancedCreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onPost = { content, tone, imageUrl, videoUrl ->
                    onAddPost(content, tone, imageUrl, videoUrl)
                    showCreatePostDialog = false
                },
                isPremium = isPremium,
                safetyState = safetyState
            )
        }

        if (showCreateStoryDialog) {
            CreateStoryDialog(
                onDismiss = { showCreateStoryDialog = false },
                onPost = { _contentType, contentUri, duration, _textOverlay, _linkPreview ->
                    // For now, pass the content URI as the image URL
                    // The enhanced version would handle all content types
                    onAddStory(contentUri, duration)
                    showCreateStoryDialog = false
                },
                safetyState = safetyState
            )
        }

        if (showPostingBlockedMessage) {
            AlertDialog(
                onDismissRequest = { showPostingBlockedMessage = false },
                title = { Text("Posting Restricted") },
                text = { Text("Posting has been disabled by parental controls. Ask a parent or guardian to change this setting.") },
                confirmButton = {
                    TextButton(onClick = { showPostingBlockedMessage = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun BubblyPostCard(
    post: Post,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReplyPost: () -> Unit,
    onShare: (Context, Post) -> Unit,
    isMockInterfaceEnabled: Boolean, // Added flag
    safetyState: SafetyState = SafetyState()
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val context = LocalContext.current
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                
                val avatarUrl = if (isMockInterfaceEnabled) {
                    post.userAvatar ?: "https://i.pravatar.cc/150?u=${post.userId}"
                } else {
                    // In real mode, use the provided avatar or treat null as a missing avatar (which we'll handle below)
                    post.userAvatar
                }

                if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                    // Show generic icon when mock is off and no real avatar is available
                    Icon(
                        Icons.Default.AccountCircle, 
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    // Use AsyncImage for all other cases (mock or real avatar available)
                    AsyncImage(
                        model = avatarUrl ?: "https://i.pravatar.cc/150?u=${post.userId}", // Fallback only if mock is on
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                }

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.userId ?: stringResource(R.string.unknown_user_id), fontWeight = FontWeight.Bold)
                    Text(post.timeAgo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_content_description))
                }
            }
            Spacer(Modifier.height(16.dp))

            val shouldHide = safetyState.isKidsMode && ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)
            if (shouldHide) {
                Text(
                    text = "Content hidden for kids mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val textToShow = if (safetyState.isKidsMode) {
                    ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel)
                } else post.content
                Text(textToShow)
            }

            // Media Display logic updated for video/image support
            // In UNDER_13 mode, hide media previews by default.
            val showMedia = !safetyState.isKidsMode

            if (showMedia && post.videoUrl != null) {
                Spacer(Modifier.height(16.dp))
                VideoPlayerView(
                    videoUrl = post.videoUrl,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardDefaults.shape)
                )
            } else if (showMedia && post.imageUrl != null) {
                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = stringResource(R.string.post_image_content_description),
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(CardDefaults.shape),
                    contentScale = ContentScale.Crop
                )
            }
            // End Media Display logic

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onShare(context, post) }) { Icon(Icons.Default.Share, stringResource(R.string.share_button_content_description)) }
                IconButton(onClick = onReplyPost) { Icon(Icons.AutoMirrored.Outlined.Comment, stringResource(R.string.comment_button_content_description)) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onLike) {
                    Icon(
                        if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        stringResource(R.string.like_button_content_description),
                        tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(post.likes.toString(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * NeuroMoments - A uniquely neurodivergent story bar
 *
 * Design philosophy:
 * - Hexagonal shapes represent the interconnected neural pathways
 * - Soft pulsing animations are calming rather than distracting
 * - "Moments" terminology emphasizes capturing authentic experiences
 * - Neurodivergent-friendly color gradients (avoiding harsh contrasts)
 */
@Composable
fun StoriesRow(
    stories: List<Story>,
    currentUser: User,
    isMockInterfaceEnabled: Boolean,
    animateStories: Boolean = true,
    onViewStory: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header with neurodivergent branding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "✨ NeuroMoments",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Share your world",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // "Add Moment" item - current user creates a new moment
            item {
                NeuroMomentItem(
                    userAvatar = currentUser.avatarUrl,
                    username = "Share",
                    subtext = "Your Moment",
                    isAddButton = true,
                    isViewed = false,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    animationEnabled = animateStories,
                    onClick = onAddStoryClick
                )
            }

            // Display actual moments/stories
            items(stories, key = { it.id }) { story ->
                NeuroMomentItem(
                    userAvatar = story.userAvatar,
                    username = story.userName,
                    subtext = if (story.isViewed) "Viewed" else "${story.items.size} moments",
                    isAddButton = false,
                    isViewed = story.isViewed,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    animationEnabled = animateStories,
                    onClick = { onViewStory(story) }
                )
            }
        }
    }
}


/**
 * NeuroMoment Item - A unique hexagon-inspired story bubble
 *
 * Features:
 * - Soft gradient borders with neurodivergent-friendly colors
 * - Gentle pulse animation instead of spinning (less overwhelming)
 * - Clear visual distinction between viewed/unviewed
 * - Accessible tap targets
 */
@Composable
fun NeuroMomentItem(
    userAvatar: String?,
    username: String,
    subtext: String = "",
    isAddButton: Boolean,
    isViewed: Boolean,
    isMockInterfaceEnabled: Boolean,
    animationEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Neurodivergent-friendly color palettes
    val unviewedGradient = remember {
        listOf(
            Color(0xFF9F70FD), // Soft purple
            Color(0xFF487DE7), // Calm blue
            Color(0xFF5AC8FA), // Sky blue
            Color(0xFF78C850), // Soft green
            Color(0xFFFFD700), // Warm gold
            Color(0xFFFF9F43), // Gentle orange
            Color(0xFFFF6B9D), // Soft pink
            Color(0xFF9F70FD)  // Back to purple (seamless)
        )
    }
    
    val viewedColor = MaterialTheme.colorScheme.outlineVariant
    val addButtonGradient = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    // Gentle pulse animation for unviewed moments
    val infiniteTransition = rememberInfiniteTransition(label = "moment-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animationEnabled && !isViewed && !isAddButton) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Gradient rotation for unviewed stories
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationEnabled && !isViewed && !isAddButton) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient-rotation"
    )

    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer glow/border container
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            // Gradient border ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isViewed) 2.dp.toPx() else 3.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                if (isAddButton) {
                    // Add button uses primary gradient
                    drawCircle(
                        brush = Brush.linearGradient(addButtonGradient),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                } else if (!isViewed) {
                    // Unviewed: rotating rainbow gradient
                    rotate(gradientRotation) {
                        drawCircle(
                            brush = Brush.sweepGradient(unviewedGradient),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                } else {
                    // Viewed: simple outline
                    drawCircle(
                        color = viewedColor,
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            // Inner content circle
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (isAddButton) {
                    // Add button with gradient icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.story_add_button_content_description),
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    // User avatar
                    val avatarUrl = if (isMockInterfaceEnabled) {
                        userAvatar ?: "https://i.pravatar.cc/150?u=$username"
                    } else {
                        userAvatar
                    }

                    if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.story_user_story_content_description, username),
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.story_user_story_content_description, username),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Username
        Text(
            text = username,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (!isViewed || isAddButton) FontWeight.Medium else FontWeight.Normal,
            color = if (!isViewed || isAddButton)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtext (time ago or "Your Moment")
        if (subtext.isNotEmpty()) {
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

@Suppress("unused")
@Composable
fun StoryItem(
    userAvatar: String?,
    username: String,
    isAddButton: Boolean,
    isViewed: Boolean,
    isMockInterfaceEnabled: Boolean,
    animationEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Legacy StoryItem - redirect to new NeuroMomentItem
    NeuroMomentItem(
        userAvatar = userAvatar,
        username = username,
        isAddButton = isAddButton,
        isViewed = isViewed,
        isMockInterfaceEnabled = isMockInterfaceEnabled,
        animationEnabled = animationEnabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Suppress("unused")
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String?, String?) -> Unit,
    @Suppress("unused") isPremium: Boolean,
    @Suppress("unused") safetyState: SafetyState
) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.create_post_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.create_post_hint)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image URL field
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.create_post_image_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Video URL field
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text(stringResource(R.string.create_post_video_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_post_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) onPost(
                                text,
                                "/gen",
                                imageUrl.ifBlank { null },
                                videoUrl.ifBlank { null }
                            )
                        },
                        // Enable if content is not blank
                        enabled = text.isNotBlank()
                    ) { Text(stringResource(R.string.create_post_button)) }
                }
            }
        }
    }
}

@Suppress("unused")
@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onPost: (String, Long) -> Unit,
    @Suppress("unused") safetyState: SafetyState
) {
    var imageUrl by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("5") } // Default to 5 seconds

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.create_story_title), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.create_story_image_url_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) { // Max 2 digits for seconds
                            durationText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.create_story_duration_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.create_story_cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val duration = durationText.toLongOrNull() ?: 5 // Default to 5s if invalid
                            if (imageUrl.isNotBlank()) {
                                onPost(imageUrl, duration * 1000L) // Convert to milliseconds
                                onDismiss()
                            }
                        },
                        enabled = imageUrl.isNotBlank() && durationText.isNotBlank()
                    ) { Text(stringResource(R.string.create_story_add_button)) }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerView(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = false // Don't auto-play on load
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}
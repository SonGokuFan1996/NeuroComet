@file:Suppress(
    "UnusedImport",
    "AssignmentToStateVariable",
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "UNUSED_PARAMETER"
)

package com.kyilmaz.neurocomet

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
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
import com.kyilmaz.neurocomet.SafetyState
import com.kyilmaz.neurocomet.ContentFiltering

/**
 * Emotional tone detection for neurodivergent-friendly content awareness.
 * Helps users prepare for potentially intense content.
 */
enum class EmotionalTone(
    val emoji: String,
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val showWarning: Boolean = false
) {
    NEUTRAL("💭", "Neutral", Color(0xFF9E9E9E), Color(0xFF616161)),
    HAPPY("😊", "Positive vibes", Color(0xFF81C784), Color(0xFF2E7D32)),
    EXCITED("🎉", "Exciting!", Color(0xFFFFD54F), Color(0xFFF57F17)),
    SAD("💙", "Emotional content", Color(0xFF64B5F6), Color(0xFF1565C0), true),
    ANXIOUS("🫂", "May feel intense", Color(0xFFCE93D8), Color(0xFF7B1FA2), true),
    FRUSTRATED("😤", "Venting", Color(0xFFFFAB91), Color(0xFFD84315), true),
    SUPPORTIVE("💜", "Supportive", Color(0xFFB39DDB), Color(0xFF512DA8)),
    QUESTION("❓", "Seeking help", Color(0xFF80DEEA), Color(0xFF00838F)),
    CELEBRATION("✨", "Celebrating!", Color(0xFFF48FB1), Color(0xFFC2185B)),
    INFORMATIVE("📚", "Informative", Color(0xFF90CAF9), Color(0xFF1976D2))
}

/**
 * Detects the emotional tone of post content for neurodivergent users.
 * This helps users who may be sensitive to certain emotional content
 * to prepare themselves before reading.
 */
private fun detectEmotionalTone(content: String): EmotionalTone {
    val lowerContent = content.lowercase()

    return when {
        // Celebration patterns
        lowerContent.contains("congrat") ||
        lowerContent.contains("achieved") ||
        lowerContent.contains("finally did it") ||
        lowerContent.contains("so proud") ||
        content.contains("🎉") || content.contains("🎊") || content.contains("🥳") -> EmotionalTone.CELEBRATION

        // Excited/happy patterns
        lowerContent.contains("so happy") ||
        lowerContent.contains("amazing") ||
        lowerContent.contains("love this") ||
        lowerContent.contains("best day") ||
        content.contains("😊") || content.contains("😄") || content.contains("❤️") -> EmotionalTone.HAPPY

        // Excited patterns
        lowerContent.contains("can't wait") ||
        lowerContent.contains("so excited") ||
        lowerContent.contains("omg") ||
        content.contains("🔥") || content.contains("⚡") -> EmotionalTone.EXCITED

        // Supportive patterns
        lowerContent.contains("you've got this") ||
        lowerContent.contains("proud of you") ||
        lowerContent.contains("here for you") ||
        lowerContent.contains("sending love") ||
        lowerContent.contains("you're not alone") -> EmotionalTone.SUPPORTIVE

        // Question/help seeking patterns
        lowerContent.contains("does anyone") ||
        lowerContent.contains("how do i") ||
        lowerContent.contains("any tips") ||
        lowerContent.contains("help me") ||
        lowerContent.contains("advice") ||
        content.contains("?") && content.length < 200 -> EmotionalTone.QUESTION

        // Informative patterns
        lowerContent.contains("did you know") ||
        lowerContent.contains("research shows") ||
        lowerContent.contains("fun fact") ||
        lowerContent.contains("psa") ||
        lowerContent.contains("reminder") -> EmotionalTone.INFORMATIVE

        // Sad/emotional patterns (with warning)
        lowerContent.contains("struggling") ||
        lowerContent.contains("hard day") ||
        lowerContent.contains("feeling down") ||
        lowerContent.contains("crying") ||
        lowerContent.contains("miss") && lowerContent.contains("so much") ||
        content.contains("😢") || content.contains("😭") || content.contains("💔") -> EmotionalTone.SAD

        // Anxious patterns (with warning)
        lowerContent.contains("anxious") ||
        lowerContent.contains("panic") ||
        lowerContent.contains("overwhelm") ||
        lowerContent.contains("can't cope") ||
        lowerContent.contains("sensory overload") -> EmotionalTone.ANXIOUS

        // Frustrated/venting patterns (with warning)
        lowerContent.contains("rant") ||
        lowerContent.contains("so frustrated") ||
        lowerContent.contains("hate when") ||
        lowerContent.contains("ugh") ||
        lowerContent.contains("annoyed") ||
        content.contains("😤") || content.contains("🙄") -> EmotionalTone.FRUSTRATED

        else -> EmotionalTone.NEUTRAL
    }
}

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
            Color(0xFFFFD700), // Bright gold
            Color(0xFFC0C0C0), // Silver
            Color(0xFFFFFFFF), // White sparkle
            Color(0xFFFFD700), // Bright gold
            Color(0xFFCB6CE6), // Festive purple
            Color(0xFF4DABF5), // Celebration blue
            Color(0xFFFFD700)  // Bright gold
        )
        HolidayType.VALENTINES -> listOf(
            Color(0xFFFF6B9D), // Hot pink
            Color(0xFFFF8FAB), // Rose pink
            Color(0xFFFFB3C6), // Light pink
            Color(0xFFFF6B6B), // Coral heart
            Color(0xFFFF6B9D), // Hot pink
            Color(0xFFCB6CE6), // Love purple
            Color(0xFFFF6B9D)  // Hot pink
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
            Color(0xFFFF6B6B), // Vibrant coral red
            Color(0xFFFFAB4D), // Bright orange
            Color(0xFFFFE66D), // Sunny yellow
            Color(0xFF7BC67B), // Fresh green
            Color(0xFF4DABF5), // Sky blue
            Color(0xFFCB6CE6), // Vivid violet
            Color(0xFFFF6B9D), // Hot pink
            Color(0xFFFF6B6B)  // Loop back
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
            Color(0xFFFF8C42), // Pumpkin orange
            Color(0xFF2D2D2D), // Deep shadow
            Color(0xFFCB6CE6), // Spooky purple
            Color(0xFF7BC67B), // Witch green
            Color(0xFFFF8C42), // Pumpkin orange
            Color(0xFF9B59B6), // Dark purple
            Color(0xFFFF8C42)  // Pumpkin orange
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
            Color(0xFFFF6B6B), // Festive red
            Color(0xFF6BCB77), // Bright green
            Color(0xFFFFD700), // Golden star
            Color(0xFFFFFFFF), // Snow white
            Color(0xFFFF6B6B), // Festive red
            Color(0xFF6BCB77), // Bright green
            Color(0xFFFFD700)  // Golden star
        )
        HolidayType.AUTISM_AWARENESS -> listOf(
            Color(0xFFFF6B6B), // Vibrant red
            Color(0xFFFFD93D), // Golden yellow
            Color(0xFF6BCB77), // Fresh green
            Color(0xFF4D96FF), // Bright blue
            Color(0xFFCB6CE6), // Violet
            Color(0xFFFF6B6B), // Vibrant red
            Color(0xFFFFD93D)  // Golden yellow
        )
        HolidayType.ADHD_AWARENESS -> listOf(
            Color(0xFFFF8C42), // Electric orange
            Color(0xFFFFD93D), // Bright yellow
            Color(0xFFFF6B6B), // Coral red
            Color(0xFFFFAB4D), // Tangerine
            Color(0xFFFF8C42), // Electric orange
            Color(0xFFFFE66D), // Sunny yellow
            Color(0xFFFF8C42)  // Electric orange
        )
        HolidayType.NEURODIVERSITY -> listOf(
            Color(0xFFFF6B6B), // Vibrant coral
            Color(0xFFFFAB4D), // Bright orange
            Color(0xFFFFE66D), // Sunny yellow
            Color(0xFF7BC67B), // Fresh green
            Color(0xFF4DABF5), // Sky blue
            Color(0xFFCB6CE6), // Vivid violet
            Color(0xFFFF6B9D), // Hot pink
            Color(0xFF7FDBDA), // Teal
            Color(0xFFFF6B6B)  // Loop back
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
 * Get secondary decorative emojis that appear around the logo for extra flair
 */
private fun getHolidayDecorations(holiday: HolidayType): List<String> {
    return when (holiday) {
        HolidayType.NEW_YEAR -> listOf("✨", "🎊", "🥳", "🎉", "⭐")
        HolidayType.VALENTINES -> listOf("💕", "💝", "💗", "💓", "🌹")
        HolidayType.ST_PATRICKS -> listOf("🍀", "🌈", "💚", "🪙", "✨")
        HolidayType.EASTER -> listOf("🐰", "🌷", "🥚", "🌸", "🦋")
        HolidayType.EARTH_DAY -> listOf("🌱", "🌿", "🌳", "💚", "🦋")
        HolidayType.PRIDE_MONTH -> listOf("🏳️‍⚧️", "✨", "💖", "💜", "💙")
        HolidayType.INDEPENDENCE_DAY -> listOf("🇺🇸", "✨", "🎇", "⭐", "🎆")
        HolidayType.HALLOWEEN -> listOf("👻", "🦇", "🕷️", "🌙", "✨")
        HolidayType.THANKSGIVING -> listOf("🍂", "🌽", "🥧", "🍁", "✨")
        HolidayType.HANUKKAH -> listOf("✡️", "✨", "🕯️", "💙", "⭐")
        HolidayType.CHRISTMAS -> listOf("🎅", "❄️", "⭐", "🎁", "✨")
        HolidayType.AUTISM_AWARENESS -> listOf("💙", "💛", "❤️", "✨", "🧩")
        HolidayType.ADHD_AWARENESS -> listOf("⚡", "💡", "✨", "🔥", "💫")
        HolidayType.NEURODIVERSITY -> listOf("✨", "💜", "💛", "💚", "💙")
        HolidayType.NONE -> emptyList()
    }
}

/**
 * Get a fun, neurodivergent-friendly holiday greeting/tagline
 */
private fun getHolidayTagline(holiday: HolidayType): String? {
    return when (holiday) {
        HolidayType.NEW_YEAR -> "New year, same awesome brain!"
        HolidayType.VALENTINES -> "Loving your unique self!"
        HolidayType.ST_PATRICKS -> "Lucky to be ND!"
        HolidayType.EASTER -> "Celebrating new beginnings!"
        HolidayType.EARTH_DAY -> "Our different brains, one planet!"
        HolidayType.PRIDE_MONTH -> "Proud to be neurodivergent!"
        HolidayType.INDEPENDENCE_DAY -> "Free to think differently!"
        HolidayType.HALLOWEEN -> "Embrace the spooky brains!"
        HolidayType.THANKSGIVING -> "Grateful for our ND community!"
        HolidayType.HANUKKAH -> "8 nights of ND joy!"
        HolidayType.CHRISTMAS -> "The most wonderful ND time!"
        HolidayType.AUTISM_AWARENESS -> "Autism is a superpower!"
        HolidayType.ADHD_AWARENESS -> "ADHD brains are amazing!"
        HolidayType.NEURODIVERSITY -> "All brains are beautiful!"
        HolidayType.NONE -> null
    }
}

/**
 * Determine if this holiday should have extra celebration mode
 * (more animations, sparkles, and excitement!)
 * These are the neurodivergent community's special days!
 */
private fun isSpecialNDCelebration(holiday: HolidayType): Boolean {
    return when (holiday) {
        HolidayType.AUTISM_AWARENESS,
        HolidayType.ADHD_AWARENESS,
        HolidayType.NEURODIVERSITY,
        HolidayType.PRIDE_MONTH -> true
        else -> false
    }
}

/**
 * Get the animation speed multiplier for a holiday
 * Keep animations smooth and calming, even for special celebrations
 */
private fun getHolidayAnimationSpeed(holiday: HolidayType): Float {
    return when {
        isSpecialNDCelebration(holiday) -> 1.0f  // Normal speed for celebrations (was too fast at 1.3f)
        holiday != HolidayType.NONE -> 1.0f      // Normal holiday speed
        else -> 1.0f                              // Default speed
    }
}

/**
 * Neurodivergent-centric logo for NeuroComet with holiday theming.
 * Features the rainbow infinity symbol and flashy animated text.
 * The infinity symbol represents the infinite variations of the human mind.
 *
 * Holiday variants provide themed colors while maintaining the core neurodiversity identity.
 * Enhanced with floating decorations, bouncy emojis, and festive glow effects!
 */
@Composable
private fun NeuroCometLogo(
    modifier: Modifier = Modifier,
    animateLogos: Boolean = true
) {
    val currentHoliday = remember { detectCurrentHoliday() }
    val holidayEmoji = remember { getHolidayEmoji(currentHoliday) }
    val holidayColors = remember { getHolidayColors(currentHoliday) }
    val holidayDecorations = remember { getHolidayDecorations(currentHoliday) }
    val isHoliday = currentHoliday != HolidayType.NONE
    val isSpecialCelebration = remember { isSpecialNDCelebration(currentHoliday) }
    val animSpeed = remember { getHolidayAnimationSpeed(currentHoliday) }

    // Animations for holiday decorations
    val infiniteTransition = rememberInfiniteTransition(label = "holiday-decorations")

    // Gentle breathing animation for the main emoji - smooth and calming
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animateLogos && isHoliday) {
            if (isSpecialCelebration) 1.08f else 1.05f
        } else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) (2000 / animSpeed).toInt() else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Gentle rotation animation for subtle movement - not shaky!
    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = if (isSpecialCelebration) -3f else -2f,
        targetValue = if (animateLogos && isHoliday) {
            if (isSpecialCelebration) 3f else 2f
        } else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) (2500 / animSpeed).toInt() else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    // Gentle floating animation for decorations - slow and calming
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateLogos && isHoliday) {
            if (isSpecialCelebration) 3f else 2f
        } else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) (3000 / animSpeed).toInt() else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Secondary decoration animation (offset from primary) - gentle movement
    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animateLogos && isHoliday) {
            if (isSpecialCelebration) -2f else -1f
        } else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) (3500 / animSpeed).toInt() else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )

    // Extra sparkle rotation for special celebrations
    val sparkleDecorationRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateLogos && isSpecialCelebration) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) 3000 else 1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle-rotation"
    )

    // Glow pulse for holiday background - more intense for special celebrations
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isSpecialCelebration) 0.15f else 0.1f,
        targetValue = if (animateLogos && isHoliday) {
            if (isSpecialCelebration) 0.35f else 0.25f
        } else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animateLogos) (2000 / animSpeed).toInt() else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Get primary glow color from holiday palette
    val glowColor = if (isHoliday && holidayColors.isNotEmpty()) {
        holidayColors[0].copy(alpha = glowAlpha)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Festive glow background for holidays
        if (isHoliday && animateLogos) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.3f
                        scaleY = 1.3f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rainbow Infinity Symbol - The Neurodiversity Symbol (with holiday colors)
            RainbowInfinitySymbol(
                modifier = Modifier.size(32.dp),
                animated = animateLogos,
                colors = holidayColors
            )

            // Flashy animated app name text (with holiday colors)
            FlashyNeuroCometText(
                animated = animateLogos,
                colors = holidayColors
            )
        }
    }
}

/**
 * Semi-realistic animated NeuroComet text with smooth gradient shimmer and subtle glow.
 * Features organic, natural-feeling animations that are calming yet engaging.
 * Supports custom colors for holiday theming.
 * When static (not animated), uses a clean gradient for a polished appearance.
 */
@Composable
private fun FlashyNeuroCometText(
    animated: Boolean = true,
    colors: List<Color> = listOf(
        Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176),
        Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8),
        Color(0xFFF48FB1), Color(0xFFE57373)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuroComet-text")

    // Smooth shimmer animation - flows naturally like light reflection
    // Starts off-screen left (-500f) and moves to off-screen right (500f)
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = if (animated) 500f else -500f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 8000 else 1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Subtle breathing scale - mimics natural, organic movement
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animated) 1.008f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 6000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Glow intensity pulse - subtle pulsing like a gentle heartbeat
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (animated) 0.55f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 5000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // For static logos, use a simpler two-tone gradient that looks clean without animation
    val staticColors = listOf(
        colors.getOrElse(0) { Color(0xFF64B5F6) },
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) }
    )

    // Create brush - static uses simple gradient, animated uses smooth flowing shimmer
    // Wider gradient (600f) for smoother transition across the text
    val brush = if (animated) {
        Brush.linearGradient(
            colors = colors,
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 600f, 0f)
        )
    } else {
        Brush.horizontalGradient(staticColors)
    }

    // Get glow color from the palette (use middle color for glow)
    val glowColor = if (animated) {
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) }
    } else {
        staticColors.first().copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        // Soft outer glow layer for depth
        if (animated) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(
                        color = glowColor.copy(alpha = glowIntensity * 0.3f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                ),
                color = Color.Transparent
            )
        }

        // Inner shadow/glow layer - creates depth
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = glowColor.copy(alpha = if (animated) glowIntensity else 0.25f),
                    offset = Offset(0f, if (animated) 1.5f else 1f),
                    blurRadius = if (animated) 6f else 3f
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
 * Semi-realistic rainbow infinity symbol - the universal symbol for neurodiversity.
 * Represents the infinite spectrum of neurological differences.
 * Features smooth, organic animations with natural gradient flow and soft glow.
 * Supports custom colors for holiday theming.
 * When static, uses a clean gradient for a polished appearance.
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

    // Smooth flowing gradient animation - organic circular motion
    val flowPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 10000 else 1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowPosition"
    )

    // Natural breathing animation - subtle organic scaling
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = if (animated) 1.01f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 6000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Glow intensity for depth effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (animated) 0.4f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) 5000 else 1,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // For static logos, use a simpler gradient
    val staticColors = listOf(
        colors.getOrElse(0) { Color(0xFF64B5F6) },
        colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) },
        colors.getOrElse(0) { Color(0xFF64B5F6) }
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

        // Create gradient - organic circular flow for animated, clean horizontal for static
        val gradient = if (animated) {
            // Smooth circular gradient flow - creates natural light reflection effect
            val angle = Math.toRadians(flowPosition.toDouble())
            val secondaryAngle = Math.toRadians((flowPosition + 120).toDouble())
            Brush.linearGradient(
                colors = displayColors,
                start = Offset(
                    centerX + kotlin.math.cos(angle).toFloat() * width * 0.6f,
                    centerY + kotlin.math.sin(angle).toFloat() * height * 0.6f
                ),
                end = Offset(
                    centerX + kotlin.math.cos(secondaryAngle).toFloat() * width * 0.6f,
                    centerY + kotlin.math.sin(secondaryAngle).toFloat() * height * 0.6f
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = displayColors,
                startX = offsetX,
                endX = offsetX + width
            )
        }

        // Draw soft glow layer for depth (animated only)
        if (animated) {
            drawPath(
                path = infinityPath,
                brush = Brush.linearGradient(
                    colors = displayColors.map { it.copy(alpha = glowAlpha * 0.5f) },
                    start = Offset(centerX - width * 0.3f, centerY),
                    end = Offset(centerX + width * 0.3f, centerY)
                ),
                style = Stroke(
                    width = strokeWidth * 2.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw main infinity symbol
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
    onProfileClick: (String) -> Unit,
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
            NeuroCometLogo(animateLogos = animateLogos)

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

        // Optimized scroll state for high refresh rate displays
        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            // Bottom padding includes navbar height so content scrolls behind it
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 80.dp + navBarHeight // FAB/bottom bar + navbar
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // Use default fling behavior which is optimized for the display refresh rate
            flingBehavior = ScrollableDefaults.flingBehavior()
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
                    safetyState = safetyState,
                    onProfileClick = onProfileClick
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
    isMockInterfaceEnabled: Boolean,
    safetyState: SafetyState = SafetyState(),
    currentUserId: String = "",
    onProfileClick: (String) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }
    var showContentWarning by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }

    // Neurodivergent feature: Detect emotional tone for content warning
    val emotionalTone = remember(post.content) {
        detectEmotionalTone(post.content)
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Neurodivergent feature: Emotional tone indicator
            if (emotionalTone != EmotionalTone.NEUTRAL && !safetyState.isKidsMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(
                            emotionalTone.backgroundColor.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = emotionalTone.emoji,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = emotionalTone.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = emotionalTone.textColor
                    )
                    Spacer(Modifier.weight(1f))
                    if (emotionalTone.showWarning) {
                        Text(
                            text = "May be intense",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                
                val avatarUrl = if (isMockInterfaceEnabled) {
                    post.userAvatar ?: "https://i.pravatar.cc/150?u=${post.userId}"
                } else {
                    post.userAvatar
                }

                if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                    Icon(
                        Icons.Default.AccountCircle, 
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { post.userId?.let { onProfileClick(it) } },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    AsyncImage(
                        model = avatarUrl ?: "https://i.pravatar.cc/150?u=${post.userId}",
                        contentDescription = stringResource(R.string.user_avatar_content_description),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { post.userId?.let { onProfileClick(it) } }
                    )
                }

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { post.userId?.let { onProfileClick(it) } }
                    ) {
                        Text(
                            post.userId ?: stringResource(R.string.unknown_user_id),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Following indicator
                        if (isFollowing) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "• Following",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        post.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }

                // 3-dot menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_content_description))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Bookmark/Save
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        if (isBookmarked) "Remove from Saved" else "Save Post",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            onClick = {
                                isBookmarked = !isBookmarked
                                showMenu = false
                                Toast.makeText(
                                    context,
                                    if (isBookmarked) "Post saved" else "Post removed from saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )

                        // Copy text
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Copy Text", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(post.content))
                                showMenu = false
                                Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Share
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Share", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                onShare(context, post)
                                showMenu = false
                            }
                        )

                        HorizontalDivider()

                        // Follow/Unfollow user
                        if (post.userId != currentUserId) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isFollowing) Icons.Filled.PersonOff else Icons.Filled.AddCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            if (isFollowing) "Unfollow @${post.userId}" else "Follow @${post.userId}",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    isFollowing = !isFollowing
                                    showMenu = false
                                    Toast.makeText(
                                        context,
                                        if (isFollowing) "Following @${post.userId}" else "Unfollowed @${post.userId}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        // Not interested (ND-friendly: helps curate feed)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.NotInterested, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Not Interested", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "We'll show you less content like this", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Hide post
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Hide Post", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Post hidden", Toast.LENGTH_SHORT).show()
                            }
                        )

                        HorizontalDivider()

                        // Block user
                        if (post.userId != currentUserId) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Block,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text("Block @${post.userId}", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, "@${post.userId} blocked", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Report
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text("Report Post", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = {
                                showMenu = false
                                showReportDialog = true
                            }
                        )

                        // Delete (only for own posts)
                        if (post.userId == currentUserId || currentUserId.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text("Delete Post", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
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
                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15f
                )
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

            // Neurodivergent feature: Reading time estimate
            val wordCount = post.content.split("\\s+".toRegex()).size
            val readingTimeSeconds = (wordCount / 3.5).toInt() // ~200 words per minute

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reading time indicator (helpful for ADHD/time blindness)
                if (wordCount > 20) {
                    Text(
                        text = "⏱️ ~${if (readingTimeSeconds < 60) "${readingTimeSeconds}s" else "${readingTimeSeconds / 60}m"} read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookmark button (quick access)
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            Toast.makeText(
                                context,
                                if (isBookmarked) "Saved!" else "Removed from saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            "Save post",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onShare(context, post) }) {
                        Icon(Icons.Default.Share, stringResource(R.string.share_button_content_description))
                    }
                    IconButton(onClick = onReplyPost) {
                        Icon(Icons.AutoMirrored.Outlined.Comment, stringResource(R.string.comment_button_content_description))
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLike) {
                        Icon(
                            if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            stringResource(R.string.like_button_content_description),
                            tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        post.likes.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }

    // Report Dialog
    if (showReportDialog) {
        ReportPostDialog(
            postId = post.id?.toString() ?: "",
            onDismiss = { showReportDialog = false },
            onReport = { reason ->
                showReportDialog = false
                Toast.makeText(context, "Report submitted. Thank you for helping keep NeuroComet safe.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

/**
 * Report post dialog with neurodivergent-friendly options.
 * Includes clear, specific options to reduce decision fatigue.
 */
@Composable
private fun ReportPostDialog(
    postId: String,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var additionalInfo by remember { mutableStateOf("") }

    val reportReasons = listOf(
        "🚫" to "Spam or scam",
        "😠" to "Harassment or bullying",
        "⚠️" to "Harmful misinformation",
        "🔞" to "Inappropriate content",
        "💔" to "Self-harm or suicide content",
        "🎭" to "Impersonation",
        "📢" to "Hate speech",
        "🤖" to "Bot or fake account",
        "❓" to "Something else"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Report Post",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Why are you reporting this post?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Your report is anonymous and helps keep our community safe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(8.dp))

                reportReasons.forEach { (emoji, reason) ->
                    val isSelected = selectedReason == reason
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (selectedReason == "Something else") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        label = { Text("Tell us more (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedReason?.let { onReport(it) }
                },
                enabled = selectedReason != null
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "✨ NeuroMoments",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "·",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Share your world ☄️",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
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
                    username = "Your story",
                    subtext = "Add new",
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
                    subtext = if (story.isViewed) "Seen" else if (story.items.size == 1) "New" else "${story.items.size} new",
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

        Spacer(Modifier.height(6.dp))

        // Username - clean, professional styling
        Text(
            text = username,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (!isViewed || isAddButton) FontWeight.Medium else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtext - subtle status indicator
        if (subtext.isNotEmpty()) {
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = when {
                    isAddButton -> MaterialTheme.colorScheme.primary
                    !isViewed -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
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

 @file:Suppress(
    "UnusedImport",
    "AssignmentToStateVariable",
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "UNUSED_PARAMETER",
    "UNUSED_VARIABLE",
    "DEPRECATION"
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kyilmaz.neurocomet.ui.components.NeuroLinkedText
import com.kyilmaz.neurocomet.ui.components.defaultNeuroLinkStyle
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest

// Explicit imports for symbols in the same package (to address compiler ambiguity in various contexts)
import com.kyilmaz.neurocomet.SafetyState
import com.kyilmaz.neurocomet.ContentFiltering
import com.kyilmaz.neurocomet.ads.BannerAd
import com.kyilmaz.neurocomet.ads.GoogleAdsManager
import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// Feed filter options - uses MaterialTheme.colorScheme for dynamic colors
enum class FeedFilter(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FOR_YOU("For You", Icons.Outlined.AutoAwesome),
    FOLLOWING("Following", Icons.Outlined.People),
    TRENDING("Trending", Icons.Outlined.TrendingUp),
    SUPPORT("Support", Icons.Outlined.Favorite),
    WINS("Wins", Icons.Outlined.Celebration)
}

/**
 * Emotional tone detection for neurodivergent-friendly content awareness.
 * Helps users prepare for potentially intense content.
 */
enum class EmotionalTone(
    val emoji: String,
    val labelResId: Int,
    val backgroundColor: Color,
    val textColor: Color,
    val showWarning: Boolean = false
) {
    NEUTRAL("💭", R.string.tone_neutral, Color(0xFFE8E8E8), Color(0xFF555555)),
    HAPPY("😊", R.string.tone_happy, Color(0xFFCCEDD0), Color(0xFF1B6B25)),
    EXCITED("🎉", R.string.tone_excited, Color(0xFFFFE4B0), Color(0xFF7A4F00)),
    SAD("💙", R.string.tone_sad, Color(0xFFCBDDF2), Color(0xFF1E5285), true),
    ANXIOUS("🫂", R.string.tone_anxious, Color(0xFFDFCEEB), Color(0xFF5C2D7E), true),
    FRUSTRATED("😤", R.string.tone_frustrated, Color(0xFFF5CFC5), Color(0xFF8B2C1A), true),
    SUPPORTIVE("💜", R.string.tone_supportive, Color(0xFFD5CFF0), Color(0xFF3F2D7A)),
    QUESTION("❓", R.string.tone_question, Color(0xFFC2E6E9), Color(0xFF1A5F66)),
    CELEBRATION("✨", R.string.tone_celebration, Color(0xFFF5CAD9), Color(0xFF852E50)),
    INFORMATIVE("📚", R.string.tone_informative, Color(0xFFC5D9EF), Color(0xFF1A4E7A))
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
    return 1.0f
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
    val holidayColors = remember { getHolidayColors(currentHoliday) }
    val isHoliday = currentHoliday != HolidayType.NONE
    val isSpecialCelebration = remember { isSpecialNDCelebration(currentHoliday) }

    // Only create animations when needed to save resources
    val shouldAnimate = animateLogos && isHoliday

    // Single animation values - only created when holiday is active
    val glowAlpha: Float

    if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "holiday-decorations")

        val animatedGlow by infiniteTransition.animateFloat(
            initialValue = if (isSpecialCelebration) 0.15f else 0.1f,
            targetValue = if (isSpecialCelebration) 0.35f else 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
        glowAlpha = animatedGlow
    } else {
        glowAlpha = 0.1f
    }

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
    isLoading: Boolean = false,
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
    safetyState: SafetyState = SafetyState(),
    enableNewFeedLayout: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }
    val isPostingBlocked = shouldBlockFeature(parentalState, BlockableFeature.POSTING) != null
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var showPostingBlockedMessage by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(FeedFilter.FOR_YOU) }

    // Filter posts based on selected pill — matches Flutter _filterPosts logic
    val filteredPosts = remember(posts, selectedFilter) {
        when (selectedFilter) {
            FeedFilter.FOR_YOU -> posts
            FeedFilter.FOLLOWING -> {
                // No category/tag fields on Android Post model yet, show first 5 as fallback
                posts.take(5)
            }
            FeedFilter.TRENDING -> {
                posts.sortedByDescending { it.likes + it.comments }
            }
            FeedFilter.SUPPORT -> {
                val supportPosts = posts.filter { post ->
                    val lower = post.content.lowercase()
                    lower.contains("support") || lower.contains("help") || lower.contains("reminder")
                }
                supportPosts.ifEmpty { emptyList() }
            }
            FeedFilter.WINS -> {
                val winsPosts = posts.filter { post ->
                    val lower = post.content.lowercase()
                    lower.contains("win") || lower.contains("celebration") ||
                        lower.contains("achievement") || post.content.contains("🎉") ||
                        post.content.contains("✨")
                }
                winsPosts.ifEmpty { emptyList() }
            }
        }
    }

    // Animation flags
    val animateLogos = animationSettings.shouldAnimate(AnimationType.LOGO)
    val animateStories = animationSettings.shouldAnimate(AnimationType.STORY)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Header matching Flutter design
        FeedHeader(
            animateLogos = animateLogos,
            isPostingBlocked = isPostingBlocked,
            onCreatePost = {
                if (isPostingBlocked) {
                    showPostingBlockedMessage = true
                } else {
                    showCreatePostDialog = true
                }
            },
            onSettings = onSettingsClick,
            isDark = isDark
        )

        // Get navbar height for bottom padding
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
        val navBarHeight = navBarPadding.calculateBottomPadding()

        // Optimized scroll state for high refresh rate displays
        val lazyListState = rememberLazyListState()

        // Track scroll state to pause animations during scroll for smoother performance
        val isFeedScrolling by remember {
            derivedStateOf { lazyListState.isScrollInProgress }
        }

        // Show loading indicator when loading and no posts yet
        if (isLoading && posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Loading your feed...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Main content
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 0.dp,
                    bottom = 80.dp + navBarHeight
                ),
                flingBehavior = ScrollableDefaults.flingBehavior()
            ) {
                // Enhanced Stories Section
                item(key = "stories_section") {
                    EnhancedStoriesSection(
                        stories = stories,
                        currentUser = currentUser,
                        isMockInterfaceEnabled = isMockInterfaceEnabled,
                        animateStories = animateStories && !isFeedScrolling,
                        onViewStory = onViewStory,
                        onAddStoryClick = { showCreateStoryDialog = true },
                        isDark = isDark
                    )
                }

                // Quick Actions Row
                item(key = "quick_actions") {
                    QuickActionsRow(
                        onCreatePost = {
                            if (isPostingBlocked) {
                                showPostingBlockedMessage = true
                            } else {
                                showCreatePostDialog = true
                            }
                        },
                        onCreateStory = { showCreateStoryDialog = true },
                        isDark = isDark
                    )
                }

                // Filter Pills
                item(key = "filter_pills") {
                    val haptic = LocalHapticFeedback.current
                    FeedFilterPills(
                        selectedFilter = selectedFilter,
                        onFilterSelected = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedFilter = it
                        },
                        isDark = isDark
                    )
                }

                // Section Header
                item(key = "section_header") {
                    FeedSectionHeader(
                        title = selectedFilter.label,
                        icon = selectedFilter.icon,
                        count = filteredPosts.size,
                        isDark = isDark
                    )
                }

                // Feed Posts with Banner Ads
                val showAds = GoogleAdsManager.shouldShowAds()

                itemsIndexed(
                    items = filteredPosts,
                    key = { _, post -> post.id ?: post.hashCode() }
                ) { index, post ->
                    // Show banner ad after post at index 4, 9, 14, etc. (every 5 posts)
                    if (showAds && index > 0 && index % 5 == 4) {
                        BannerAd(
                            modifier = Modifier.padding(
                                horizontal = if (enableNewFeedLayout) 8.dp else 12.dp,
                                vertical = if (enableNewFeedLayout) 4.dp else 8.dp
                            ),
                            adKey = "feed_banner_$index"
                        )
                    }

                    BubblyPostCard(
                        post = post,
                        onLike = { post.id?.let(onLikePost) },
                        onDelete = { post.id?.let(onDeletePost) },
                        onReplyPost = { onReplyPost(post) },
                        onShare = onSharePost,
                        isMockInterfaceEnabled = isMockInterfaceEnabled,
                        safetyState = safetyState,
                        onProfileClick = onProfileClick,
                        onHashtagClick = onHashtagClick,
                        compactMode = enableNewFeedLayout
                    )
                }
            }
        }
    }

    // Dialogs
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
                onAddStory(contentUri, duration)
                showCreateStoryDialog = false
            },
            safetyState = safetyState
        )
    }

    if (showPostingBlockedMessage) {
        AlertDialog(
            onDismissRequest = { showPostingBlockedMessage = false },
            title = { Text(stringResource(R.string.posting_restricted_title)) },
            text = { Text(stringResource(R.string.posting_restricted_message)) },
            confirmButton = {
                TextButton(onClick = { showPostingBlockedMessage = false }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
    }
}

// ============================================================================
// MODERN FEED HEADER - Matching Flutter design
// ============================================================================

@Composable
private fun FeedHeader(
    animateLogos: Boolean,
    isPostingBlocked: Boolean,
    onCreatePost: () -> Unit,
    onSettings: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo
        NeuroCometLogo(animateLogos = animateLogos)

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FeedHeaderIconButton(
                icon = Icons.Outlined.AddBox,
                onClick = onCreatePost,
                contentDescription = "Create Post",
                isDark = isDark
            )
            FeedHeaderIconButton(
                icon = Icons.Outlined.Settings,
                onClick = onSettings,
                contentDescription = "Settings",
                isDark = isDark
            )
        }
    }
}

@Composable
private fun FeedHeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF666680)
            )
        }
    }
}

// ============================================================================
// ENHANCED STORIES SECTION - Matching Flutter design
// ============================================================================

@Composable
private fun EnhancedStoriesSection(
    stories: List<Story>,
    currentUser: User,
    isMockInterfaceEnabled: Boolean,
    animateStories: Boolean,
    onViewStory: (Story) -> Unit,
    onAddStoryClick: () -> Unit,
    isDark: Boolean
) {
    // Use a clean surface-level background instead of a gradient overlay.
    // This avoids the ugly tinted band that clashes across different themes.
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Stories list — StoriesRow already has its own header
        StoriesRow(
            stories = stories,
            currentUser = currentUser,
            isMockInterfaceEnabled = isMockInterfaceEnabled,
            animateStories = animateStories,
            onViewStory = onViewStory,
            onAddStoryClick = onAddStoryClick
        )

        // Thin separator line to cleanly delineate the section
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

// ============================================================================
// QUICK ACTIONS ROW - Matching Flutter design
// ============================================================================

@Composable
private fun QuickActionsRow(
    onCreatePost: () -> Unit,
    onCreateStory: () -> Unit,
    isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "What's on your mind?" card
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .clickable(onClick = onCreatePost),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryColor, tertiaryColor)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "What's on your mind?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Quick action buttons
        QuickActionButton(
            icon = Icons.Default.Add,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onCreatePost
        )
        Spacer(Modifier.width(8.dp))
        QuickActionButton(
            icon = Icons.Outlined.AutoAwesome,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onCreateStory
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// FEED FILTER PILLS - Matching Flutter/Notifications design
// ============================================================================

@Composable
private fun FeedFilterPills(
    selectedFilter: FeedFilter,
    onFilterSelected: (FeedFilter) -> Unit,
    isDark: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(FeedFilter.entries.size) { index ->
            val filter = FeedFilter.entries[index]
            FeedFilterPill(
                filter = filter,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                isDark = isDark
            )
        }
    }
}

@Composable
private fun FeedFilterPill(
    filter: FeedFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    @Suppress("unused") isDark: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Scale animation on press — matches Notifications/Messages style
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                coroutineScope.launch {
                    scale.animateTo(0.95f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                    scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                }
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            primaryColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        border = if (isSelected) {
            BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f))
        } else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isSelected) 16.dp else 14.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = filter.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================================================
// FEED SECTION HEADER - Matching Flutter design with gradient accent
// ============================================================================

@Composable
private fun FeedSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary

        // Gradient accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, tertiaryColor)
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = primaryColor
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$count posts",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ============================================================================
// DIALOGS
// ============================================================================

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
@Composable
private fun FeedDialogs(
    showCreatePostDialog: Boolean,
    showCreateStoryDialog: Boolean,
    showPostingBlockedMessage: Boolean,
    onDismissCreatePost: () -> Unit,
    onDismissCreateStory: () -> Unit,
    onDismissBlocked: () -> Unit,
    onPost: (String, String, String?, String?) -> Unit,
    onAddStory: (String, Long) -> Unit,
    isPremium: Boolean,
    safetyState: SafetyState
) {
    if (showCreatePostDialog) {
        EnhancedCreatePostDialog(
            onDismiss = onDismissCreatePost,
            onPost = { content, tone, imageUrl, videoUrl ->
                onPost(content, tone, imageUrl, videoUrl)
                onDismissCreatePost()
            },
            isPremium = isPremium,
            safetyState = safetyState
        )
    }

    if (showCreateStoryDialog) {
        CreateStoryDialog(
            onDismiss = onDismissCreateStory,
            onPost = { _contentType, contentUri, duration, _textOverlay, _linkPreview ->
                onAddStory(contentUri, duration)
                onDismissCreateStory()
            },
            safetyState = safetyState
        )
    }

    if (showPostingBlockedMessage) {
        AlertDialog(
            onDismissRequest = onDismissBlocked,
            title = { Text(stringResource(R.string.posting_restricted_title)) },
            text = { Text(stringResource(R.string.posting_restricted_message)) },
            confirmButton = {
                TextButton(onClick = onDismissBlocked) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
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
    onProfileClick: (String) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    compactMode: Boolean = false
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

    // Read content preferences to respect hide-like-counts setting
    val contentPrefs = remember { SocialSettingsManager.getContentPreferences(context) }

    // Pre-compute strings for Toast messages
    val bookmarkedText = stringResource(R.string.post_bookmarked)
    val unbookmarkedText = stringResource(R.string.post_unbookmarked)
    val copiedText = stringResource(R.string.post_copied)
    val hideText = stringResource(R.string.post_hide)
    val nowFollowingText = stringResource(R.string.post_now_following, post.userId ?: "")
    val unfollowedText = stringResource(R.string.post_unfollowed, post.userId ?: "")

    // ========================================================================
    // BUBBLY POST CARD - Clean Dynamic Color Design
    // ========================================================================

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (compactMode) 8.dp else 12.dp,
                vertical = if (compactMode) 4.dp else 6.dp
            ),
        shape = RoundedCornerShape(if (compactMode) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compactMode) 12.dp else 16.dp)
        ) {
            // ============ HEADER ROW ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with colorful gradient ring
                val avatarUrl = if (isMockInterfaceEnabled) {
                    post.userAvatar ?: "https://i.pravatar.cc/150?u=${post.userId}"
                } else {
                    post.userAvatar
                }

                Box(
                            modifier = Modifier
                                .size(if (compactMode) 42.dp else 50.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { post.userId?.let { onProfileClick(it) } },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(44.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(avatarUrl ?: "https://i.pravatar.cc/150?u=${post.userId}")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(if (compactMode) 10.dp else 12.dp))

                        // User info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = post.userId ?: "Unknown",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isFollowing) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Following",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = post.timeAgo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Menu button
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                                if (isBookmarked) stringResource(R.string.post_unsave) else stringResource(R.string.post_save),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        isBookmarked = !isBookmarked
                                        showMenu = false
                                        Toast.makeText(context, if (isBookmarked) bookmarkedText else unbookmarkedText, Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Copy text
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(R.string.post_copy_text), color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(post.content))
                                showMenu = false
                                Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Share
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(stringResource(R.string.post_share), color = MaterialTheme.colorScheme.onSurface)
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
                                            stringResource(
                                                if (isFollowing) R.string.post_unfollow_user else R.string.post_follow_user,
                                                post.userId ?: ""
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = {
                                    isFollowing = !isFollowing
                                    showMenu = false
                                    Toast.makeText(
                                        context,
                                        if (isFollowing) nowFollowingText else unfollowedText,
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
                                    Text(stringResource(R.string.post_not_interested), color = MaterialTheme.colorScheme.onSurface)
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
                                    Text(stringResource(R.string.post_hide), color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, hideText, Toast.LENGTH_SHORT).show()
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
                                        Text(stringResource(R.string.post_block_user, post.userId ?: ""), color = MaterialTheme.colorScheme.error)
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
                                    Text(stringResource(R.string.post_report_post), color = MaterialTheme.colorScheme.error)
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
                                        Text(stringResource(R.string.post_delete_post), color = MaterialTheme.colorScheme.error)
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

            // ============ CONTENT ============
            Spacer(Modifier.height(12.dp))

            val shouldHide = safetyState.isKidsMode && ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)
            if (shouldHide) {
                Text(
                    text = "🔒 Content hidden for kids mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val textToShow = if (safetyState.isKidsMode) {
                    ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel)
                } else post.content

                NeuroLinkedText(
                    text = textToShow,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    ),
                    linkStyle = defaultNeuroLinkStyle(),
                    onLinkClick = { link ->
                        when (link.type) {
                            com.kyilmaz.neurocomet.ui.components.LinkType.MENTION -> {
                                val username = link.text.removePrefix("@")
                                onProfileClick(username)
                            }
                            com.kyilmaz.neurocomet.ui.components.LinkType.HASHTAG -> {
                                val hashtag = link.text.removePrefix("#")
                                onHashtagClick(hashtag)
                            }
                            else -> Unit
                        }
                    }
                )
            }

            // ============ MEDIA CAROUSEL (up to 20 images/videos) ============
            val showMedia = !safetyState.isKidsMode
            val mediaItems = post.getAllMedia()

            if (showMedia && mediaItems.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))

                if (mediaItems.size == 1) {
                    // Single media item - simple display
                    val item = mediaItems.first()
                    when (item.type) {
                        MediaType.VIDEO -> {
                            VideoPlayerView(
                                videoUrl = item.url,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        MediaType.IMAGE -> {
                            AsyncImage(
                                model = item.url,
                                contentDescription = item.altText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // Multiple media items - carousel with pager
                    PostMediaCarousel(
                        mediaItems = mediaItems,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }
            }

            // ============ ACTION BAR ============
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Like, Comment, Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Like button
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (post.likes > 0 && !contentPrefs.hideLikeCounts) {
                        Text(
                            text = formatCount(post.likes),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Comment button
                    IconButton(onClick = onReplyPost) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Comment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share button
                    IconButton(onClick = { onShare(context, post) }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Bookmark
                IconButton(
                    onClick = {
                        isBookmarked = !isBookmarked
                        Toast.makeText(context, if (isBookmarked) "Saved!" else "Removed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Emotional tone tag (if present) - neurodivergent-friendly indicator
            if (emotionalTone != EmotionalTone.NEUTRAL && !safetyState.isKidsMode) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = emotionalTone.backgroundColor,
                    border = BorderStroke(1.dp, emotionalTone.textColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = emotionalTone.emoji,
                            fontSize = 13.sp
                        )
                        Text(
                            text = stringResource(emotionalTone.labelResId),
                            style = MaterialTheme.typography.labelMedium,
                            color = emotionalTone.textColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (emotionalTone.showWarning) {
                            Text(
                                text = "·",
                                color = emotionalTone.textColor.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "sensitive",
                                style = MaterialTheme.typography.labelSmall,
                                color = emotionalTone.textColor.copy(alpha = 0.75f)
                            )
                        }
                    }
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
                Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

/**
 * Format count for display (e.g., 1.2K, 3.4M)
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

/**
 * Media carousel for posts with multiple images/videos (up to 20 like Instagram).
 * Features:
 * - Smooth horizontal paging
 * - Page indicators
 * - Optimized media loading
 * - Support for mixed image/video content
 */
@Composable
fun PostMediaCarousel(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { mediaItems.size.coerceAtMost(Post.MAX_MEDIA_ITEMS) }
    )

    Box(modifier = modifier) {
        // Horizontal pager for swiping through media
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1 // Preload adjacent pages for smooth scrolling
        ) { page ->
            val item = mediaItems[page]

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (item.type) {
                    MediaType.VIDEO -> {
                        VideoPlayerView(
                            videoUrl = item.url,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    MediaType.IMAGE -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.altText,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Page indicator dots (only show if more than 1 item)
        if (mediaItems.size > 1) {
            // Counter badge in top right
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${mediaItems.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Dot indicators at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(mediaItems.size.coerceAtMost(10)) { index ->
                    // For more than 10 items, show condensed indicators
                    val displayIndex = if (mediaItems.size <= 10) {
                        index
                    } else {
                        // Map to visible range around current page
                        val start = (pagerState.currentPage - 4).coerceAtLeast(0)
                        start + index
                    }

                    if (displayIndex < mediaItems.size) {
                        val isSelected = pagerState.currentPage == displayIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .background(
                                    color = if (isSelected)
                                        Color.White
                                    else
                                        Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Show ellipsis indicator if there are more pages
                if (mediaItems.size > 10 && pagerState.currentPage < mediaItems.size - 5) {
                    Text(
                        text = "•••",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
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
                stringResource(R.string.report_dialog_title),
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
                        label = { Text(stringResource(R.string.report_dialog_reason_hint)) },
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
                Text(stringResource(R.string.report_dialog_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.report_dialog_cancel))
            }
        }
    )
}

/**
 * Format time indicator for moments - provides balanced, readable time text
 */
private fun formatMomentTime(itemCount: Int): String {
    // For mock data, generate realistic time strings based on item count
    return when {
        itemCount <= 1 -> "Just now"
        itemCount == 2 -> "2h"
        itemCount == 3 -> "5h"
        else -> "12h"
    }
}

/**
 * NeuroMoments - A uniquely neurodivergent story bar
 *
 * Design philosophy:
 * - Clean, modern design with subtle gradient accents
 * - Soft pulsing animations are calming rather than distracting
 * - "Moments" terminology emphasizes capturing authentic experiences
 * - Neurodivergent-friendly with clear visual hierarchy
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
    val unseenCount = stories.count { !it.isViewed }

    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gradient icon badge — vivid colors for clear visibility
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6C63FF), // Vivid indigo
                                    Color(0xFF7C4DFF), // Deep purple
                                    Color(0xFFB388FF)  // Lavender accent
                                )
                            ),
                            shape = RoundedCornerShape(7.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    stringResource(R.string.moments_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (unseenCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = stringResource(R.string.moments_new_count, unseenCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        letterSpacing = 0.25.sp
                    )
                }
            }
        }

        // Optimized LazyRow with smooth scrolling for high-refresh rate displays
        val momentsListState = rememberLazyListState()

        // Pause animations during scroll for smoother performance
        val isScrolling by remember {
            derivedStateOf { momentsListState.isScrollInProgress }
        }

        LazyRow(
            state = momentsListState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            flingBehavior = ScrollableDefaults.flingBehavior()
        ) {
            // "Add Moment" item - current user creates a new moment
            item(key = "add_moment") {
                NeuroMomentItem(
                    userAvatar = currentUser.avatarUrl,
                    username = stringResource(R.string.moments_your_story),
                    timeAgo = null,
                    isAddButton = true,
                    isViewed = false,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    animationEnabled = animateStories && !isScrolling,
                    onClick = onAddStoryClick
                )
            }

            // Display actual moments/stories
            items(stories, key = { it.id }) { story ->
                NeuroMomentItem(
                    userAvatar = story.userAvatar,
                    username = story.userName.split(" ").firstOrNull()?.take(10) ?: story.userName.take(10),
                    timeAgo = formatMomentTime(story.items.size),
                    isAddButton = false,
                    isViewed = story.isViewed,
                    isMockInterfaceEnabled = isMockInterfaceEnabled,
                    animationEnabled = animateStories && !isScrolling,
                    onClick = { onViewStory(story) }
                )
            }
        }
    }
}


/**
 * NeuroMoment Item - Production-ready story bubble
 *
 * Features:
 * - Elegant gradient ring for unseen stories
 * - Subtle dimming for viewed stories
 * - Clean visual hierarchy with balanced time indicators
 * - Accessible tap targets (minimum 48dp)
 * - Optimized for high-refresh rate displays
 */
@Composable
fun NeuroMomentItem(
    userAvatar: String?,
    username: String,
    timeAgo: String? = null,
    isAddButton: Boolean,
    isViewed: Boolean,
    isMockInterfaceEnabled: Boolean,
    animationEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Clean gradient colors — cohesive vivid palette for story rings
    val unviewedGradient = remember {
        listOf(
            Color(0xFF6C63FF), // Vivid indigo
            Color(0xFF7C4DFF), // Deep purple
            Color(0xFFB388FF), // Lavender
            Color(0xFF6C63FF)  // Loop back for seamless sweep
        )
    }
    
    val viewedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val addButtonColor = MaterialTheme.colorScheme.primary

    // Only animate gradient rotation when enabled and story is unseen
    val shouldAnimate = animationEnabled && !isViewed && !isAddButton

    // Use a stable rotation value when not animating to avoid recomposition
    val gradientRotation = if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "moment-glow")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gradient-rotation"
        )
        rotation
    } else {
        0f
    }

    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(66.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ring border
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isViewed) 1.5.dp.toPx() else 2.8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                if (isAddButton) {
                    // Add button: dashed primary ring
                    drawCircle(
                        color = addButtonColor,
                        radius = radius,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 6f), 0f
                            )
                        )
                    )
                } else if (!isViewed) {
                    // Unseen: rotating gradient
                    rotate(gradientRotation) {
                        drawCircle(
                            brush = Brush.sweepGradient(unviewedGradient),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                } else {
                    // Viewed: subtle outline
                    drawCircle(
                        color = viewedColor,
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }

            // Avatar container
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isAddButton) {
                    // Add button icon
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.story_add_button_content_description),
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // User avatar with dimming for viewed
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    } else {
                        val context = LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUrl)
                                .crossfade(100)
                                .memoryCacheKey(avatarUrl)
                                .size(116) // 58dp * 2 for density
                                .build(),
                            contentDescription = stringResource(R.string.story_user_story_content_description, username),
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = if (isViewed) 0.75f else 1f },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Plus badge for add button
            if (isAddButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Username - clean, legible, and centered
        Text(
            text = username,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isViewed) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 11.sp,
            lineHeight = 13.sp
        )

        // Time indicator - balanced, subtle but readable
        if (!isAddButton && !timeAgo.isNullOrEmpty()) {
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                lineHeight = 12.sp
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
            setMediaItem(ExoMediaItem.fromUri(videoUrl))
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


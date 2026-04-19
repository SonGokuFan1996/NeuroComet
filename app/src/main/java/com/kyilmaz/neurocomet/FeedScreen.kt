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
import android.content.ClipData
import android.content.Intent
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import com.kyilmaz.neurocomet.ui.design.M3ESurfaceVariant
import com.kyilmaz.neurocomet.ui.design.M3ESurface
import androidx.compose.ui.draw.scale
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem
import com.kyilmaz.neurocomet.ui.design.M3ETopAppBar
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

enum class FeedFilter(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FOR_YOU("For You", Icons.Outlined.AutoAwesome),
    FOLLOWING("Following", Icons.Outlined.People),
    TRENDING("Trending", Icons.Outlined.TrendingUp),
    SUPPORT("Support", Icons.Outlined.Favorite),
    WINS("Wins", Icons.Outlined.Celebration)
}

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

private fun detectEmotionalTone(content: String): EmotionalTone {
    val lowerContent = content.lowercase()
    return when {
        lowerContent.contains("congrat") || lowerContent.contains("achieved") || lowerContent.contains("finally did it") || lowerContent.contains("so proud") || content.contains("🎉") || content.contains("🎊") || content.contains("🥳") -> EmotionalTone.CELEBRATION
        lowerContent.contains("so happy") || lowerContent.contains("amazing") || lowerContent.contains("love this") || lowerContent.contains("best day") || content.contains("😊") || content.contains("😄") || content.contains("❤️") -> EmotionalTone.HAPPY
        lowerContent.contains("can't wait") || lowerContent.contains("so excited") || lowerContent.contains("omg") || content.contains("🔥") || content.contains("⚡") -> EmotionalTone.EXCITED
        lowerContent.contains("you've got this") || lowerContent.contains("proud of you") || lowerContent.contains("here for you") || lowerContent.contains("sending love") || lowerContent.contains("you're not alone") -> EmotionalTone.SUPPORTIVE
        lowerContent.contains("does anyone") || lowerContent.contains("how do i") || lowerContent.contains("any tips") || lowerContent.contains("help me") || lowerContent.contains("advice") || (content.contains("?") && content.length < 200) -> EmotionalTone.QUESTION
        lowerContent.contains("did you know") || lowerContent.contains("research shows") || lowerContent.contains("fun fact") || lowerContent.contains("psa") || lowerContent.contains("reminder") -> EmotionalTone.INFORMATIVE
        lowerContent.contains("struggling") || lowerContent.contains("hard day") || lowerContent.contains("feeling down") || lowerContent.contains("crying") || (lowerContent.contains("miss") && lowerContent.contains("so much")) || content.contains("😢") || content.contains("😭") || content.contains("💔") -> EmotionalTone.SAD
        lowerContent.contains("anxious") || lowerContent.contains("panic") || lowerContent.contains("overwhelm") || lowerContent.contains("can't cope") || lowerContent.contains("sensory overload") -> EmotionalTone.ANXIOUS
        lowerContent.contains("rant") || lowerContent.contains("so frustrated") || lowerContent.contains("hate when") || lowerContent.contains("ugh") || lowerContent.contains("annoyed") || content.contains("😤") || content.contains("🙄") -> EmotionalTone.FRUSTRATED
        else -> EmotionalTone.NEUTRAL
    }
}

enum class HolidayType {
    NONE, NEW_YEAR, VALENTINES, ST_PATRICKS, EASTER, EARTH_DAY, PRIDE_MONTH, INDEPENDENCE_DAY, HALLOWEEN, THANKSGIVING, HANUKKAH, CHRISTMAS, AUTISM_AWARENESS, ADHD_AWARENESS, NEURODIVERSITY
}

private fun detectCurrentHoliday(): HolidayType {
    val calendar = java.util.Calendar.getInstance()
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    val weekOfMonth = calendar.get(java.util.Calendar.WEEK_OF_MONTH)
    return when {
        (month == 0 && day == 1) || (month == 11 && day == 31) -> HolidayType.NEW_YEAR
        month == 1 && day in 13..15 -> HolidayType.VALENTINES
        month == 2 && day in 16..18 -> HolidayType.ST_PATRICKS
        month == 2 && day in 13..19 -> HolidayType.NEURODIVERSITY
        month == 3 && day == 2 -> HolidayType.AUTISM_AWARENESS
        month == 3 && day == 22 -> HolidayType.EARTH_DAY
        month == 3 && day in 1..21 -> HolidayType.EASTER
        month == 5 -> HolidayType.PRIDE_MONTH
        month == 6 && day in 3..5 -> HolidayType.INDEPENDENCE_DAY
        month == 9 && day in 1..7 -> HolidayType.ADHD_AWARENESS
        month == 9 && day in 24..31 -> HolidayType.HALLOWEEN
        month == 10 && dayOfWeek == java.util.Calendar.THURSDAY && weekOfMonth == 4 -> HolidayType.THANKSGIVING
        month == 11 && day in 1..24 -> HolidayType.HANUKKAH
        month == 11 && day in 24..26 -> HolidayType.CHRISTMAS
        else -> HolidayType.NONE
    }
}

private fun getHolidayColors(holiday: HolidayType): List<Color> {
    return when (holiday) {
        HolidayType.NEW_YEAR -> listOf(Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0xFFCB6CE6), Color(0xFF4DABF5), Color(0xFFFFD700))
        HolidayType.VALENTINES -> listOf(Color(0xFFFF6B9D), Color(0xFFFF8FAB), Color(0xFFFFB3C6), Color(0xFFFF6B6B), Color(0xFFFF6B9D), Color(0xFFCB6CE6), Color(0xFFFF6B9D))
        HolidayType.ST_PATRICKS -> listOf(Color(0xFF6B8E23), Color(0xFF8FBC8F), Color(0xFF98FB98), Color(0xFFD4AF37), Color(0xFF6B8E23), Color(0xFF8FBC8F))
        HolidayType.EASTER -> listOf(Color(0xFFFFB6C1), Color(0xFFE6E6FA), Color(0xFF98FB98), Color(0xFFF0E68C), Color(0xFFB0E0E6), Color(0xFFFFB6C1))
        HolidayType.EARTH_DAY -> listOf(Color(0xFF6B8E23), Color(0xFF6495ED), Color(0xFF8FBC8F), Color(0xFF5F9EA0), Color(0xFF6B8E23), Color(0xFF6495ED))
        HolidayType.PRIDE_MONTH -> listOf(Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFFE66D), Color(0xFF7BC67B), Color(0xFF4DABF5), Color(0xFFCB6CE6), Color(0xFFFF6B9D), Color(0xFFFF6B6B))
        HolidayType.INDEPENDENCE_DAY -> listOf(Color(0xFFCD5C5C), Color(0xFFF5F5F5), Color(0xFF6495ED), Color(0xFFF5F5F5), Color(0xFFCD5C5C), Color(0xFF6495ED))
        HolidayType.HALLOWEEN -> listOf(Color(0xFFFF8C42), Color(0xFF2D2D2D), Color(0xFFCB6CE6), Color(0xFF7BC67B), Color(0xFFFF8C42), Color(0xFF9B59B6), Color(0xFFFF8C42))
        HolidayType.THANKSGIVING -> listOf(Color(0xFFE9967A), Color(0xFFA0522D), Color(0xFFD4AF37), Color(0xFFBC8F8F), Color(0xFFE9967A), Color(0xFFA0522D))
        HolidayType.HANUKKAH -> listOf(Color(0xFF6495ED), Color(0xFFF5F5F5), Color(0xFFB0C4DE), Color(0xFFD4AF37), Color(0xFF6495ED), Color(0xFFF5F5F5))
        HolidayType.CHRISTMAS -> listOf(Color(0xFFFF6B6B), Color(0xFF6BCB77), Color(0xFFFFD700), Color(0xFFFFFFFF), Color(0xFFFF6B6B), Color(0xFF6BCB77), Color(0xFFFFD700))
        HolidayType.AUTISM_AWARENESS -> listOf(Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFFCB6CE6), Color(0xFFFF6B6B), Color(0xFFFFD93D))
        HolidayType.ADHD_AWARENESS -> listOf(Color(0xFFFF8C42), Color(0xFFFFD93D), Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFF8C42), Color(0xFFFFE66D), Color(0xFFFF8C42))
        HolidayType.NEURODIVERSITY -> listOf(Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFFE66D), Color(0xFF7BC67B), Color(0xFF4DABF5), Color(0xFFCB6CE6), Color(0xFFFF6B9D), Color(0xFF7FDBDA), Color(0xFFFF6B6B))
        HolidayType.NONE -> listOf(Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFF48FB1), Color(0xFFE57373))
    }
}

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

private fun isSpecialNDCelebration(holiday: HolidayType): Boolean {
    return holiday == HolidayType.AUTISM_AWARENESS || holiday == HolidayType.ADHD_AWARENESS || holiday == HolidayType.NEURODIVERSITY || holiday == HolidayType.PRIDE_MONTH
}

@Composable
private fun NeuroCometLogo(modifier: Modifier = Modifier, animateLogos: Boolean = true) {
    val currentHoliday = remember { detectCurrentHoliday() }
    val holidayColors = remember { getHolidayColors(currentHoliday) }
    val isHoliday = currentHoliday != HolidayType.NONE
    val isSpecialCelebration = remember { isSpecialNDCelebration(currentHoliday) }
    val shouldAnimate = animateLogos && isHoliday
    val glowAlpha: Float
    if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "holiday-decorations")
        val animatedGlow by infiniteTransition.animateFloat(
            initialValue = if (isSpecialCelebration) 0.15f else 0.1f,
            targetValue = if (isSpecialCelebration) 0.35f else 0.25f,
            animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "glow"
        )
        glowAlpha = animatedGlow
    } else {
        glowAlpha = 0.1f
    }
    val glowColor = if (isHoliday && holidayColors.isNotEmpty()) holidayColors[0].copy(alpha = glowAlpha) else Color.Transparent
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isHoliday && animateLogos) {
            Box(modifier = Modifier.matchParentSize().graphicsLayer { scaleX = 1.3f; scaleY = 1.3f }.background(brush = Brush.radialGradient(colors = listOf(glowColor, Color.Transparent)), shape = M3EDesignSystem.Shapes.MediumShape))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(M3EDesignSystem.Spacing.xs)) {
            RainbowInfinitySymbol(modifier = Modifier.size(M3EDesignSystem.IconSize.xl), animated = animateLogos, colors = holidayColors)
            FlashyNeuroCometText(animated = animateLogos, colors = holidayColors)
        }
    }
}

@Composable
private fun FlashyNeuroCometText(
    animated: Boolean = true,
    colors: List<Color> = listOf(Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFF48FB1), Color(0xFFE57373))
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeuroComet-text")
    val shimmerOffset by infiniteTransition.animateFloat(initialValue = -500f, targetValue = if (animated) 500f else -500f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 8000 else 1, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "shimmer")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = if (animated) 1.008f else 1f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 6000 else 1, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "scale")
    val glowIntensity by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = if (animated) 0.55f else 0.4f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 5000 else 1, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "glow")
    val staticColors = listOf(colors.getOrElse(0) { Color(0xFF64B5F6) }, colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) })
    val brush = if (animated) Brush.linearGradient(colors = colors, start = Offset(shimmerOffset, 0f), end = Offset(shimmerOffset + 600f, 0f)) else Brush.horizontalGradient(staticColors)
    val glowColor = if (animated) colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) } else staticColors.first().copy(alpha = 0.25f)
    Box(modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        if (animated) {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, shadow = Shadow(color = glowColor.copy(alpha = glowIntensity * 0.3f), offset = Offset(0f, 0f), blurRadius = 16f)), color = Color.Transparent)
        }
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, shadow = Shadow(color = glowColor.copy(alpha = if (animated) glowIntensity else 0.25f), offset = Offset(0f, if (animated) 1.5f else 1f), blurRadius = if (animated) 6f else 3f)), color = Color.Transparent)
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, brush = brush))
    }
}

@Composable
fun RainbowInfinitySymbol(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    colors: List<Color> = listOf(Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFF48FB1), Color(0xFFE57373))
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinity-feed")
    val flowPosition by infiniteTransition.animateFloat(initialValue = 0f, targetValue = if (animated) 360f else 0f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 10000 else 1, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "flowPosition")
    val breathe by infiniteTransition.animateFloat(initialValue = 0.99f, targetValue = if (animated) 1.01f else 1f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 6000 else 1, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "breathe")
    val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.25f, targetValue = if (animated) 0.4f else 0.25f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = if (animated) 5000 else 1, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "glowAlpha")
    val staticColors = listOf(colors.getOrElse(0) { Color(0xFF64B5F6) }, colors.getOrElse(colors.size / 2) { Color(0xFFBA68C8) }, colors.getOrElse(0) { Color(0xFF64B5F6) })
    val displayColors = if (animated) colors else staticColors
    Canvas(modifier = modifier) {
        val width = size.width * breathe
        val height = size.height * breathe
        val offsetX = (size.width - width) / 2
        val offsetY = (size.height - height) / 2
        val strokeWidth = height * 0.14f
        val centerX = width / 2 + offsetX
        val centerY = height / 2 + offsetY
        val loopWidth = width * 0.35f
        val loopHeight = height * 0.38f
        val infinityPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(centerX, centerY)
            cubicTo(x1 = centerX + loopWidth * 0.5f, y1 = centerY - loopHeight * 0.9f, x2 = centerX + loopWidth * 0.95f, y2 = centerY - loopHeight * 0.7f, x3 = centerX + loopWidth, y3 = centerY)
            cubicTo(x1 = centerX + loopWidth * 0.95f, y1 = centerY + loopHeight * 0.7f, x2 = centerX + loopWidth * 0.5f, y2 = centerY + loopHeight * 0.9f, x3 = centerX, y3 = centerY)
            cubicTo(x1 = centerX - loopWidth * 0.5f, y1 = centerY + loopHeight * 0.9f, x2 = centerX - loopWidth * 0.95f, y2 = centerY + loopHeight * 0.7f, x3 = centerX - loopWidth, y3 = centerY)
            cubicTo(x1 = centerX - loopWidth * 0.95f, y1 = centerY - loopHeight * 0.7f, x2 = centerX - loopWidth * 0.5f, y2 = centerY - loopHeight * 0.9f, x3 = centerX, y3 = centerY)
        }
        val gradient = if (animated) {
            val angle = Math.toRadians(flowPosition.toDouble())
            val secondaryAngle = Math.toRadians((flowPosition + 120).toDouble())
            Brush.linearGradient(colors = displayColors, start = Offset(centerX + kotlin.math.cos(angle).toFloat() * width * 0.6f, centerY + kotlin.math.sin(angle).toFloat() * height * 0.6f), end = Offset(centerX + kotlin.math.cos(secondaryAngle).toFloat() * width * 0.6f, centerY + kotlin.math.sin(secondaryAngle).toFloat() * height * 0.6f))
        } else {
            Brush.horizontalGradient(colors = displayColors, startX = offsetX, endX = offsetX + width)
        }
        if (animated) {
            drawPath(path = infinityPath, brush = Brush.linearGradient(colors = displayColors.map { it.copy(alpha = glowAlpha * 0.5f) }, start = Offset(centerX - width * 0.3f, centerY), end = Offset(centerX + width * 0.3f, centerY)), style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        drawPath(path = infinityPath, brush = gradient, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    feedUiState: FeedUiState,
    onAddPost: (String, String, String?, String?, Long?, String?) -> Unit,
    onAddStory: (StoryContentType, String, Long, String?, Long, Long?, LinkPreviewData?) -> Unit,
    onLikePost: (Long) -> Unit,
    onReplyPost: (Post) -> Unit,
    onSharePost: (Context, Post) -> Unit,
    onDeletePost: (Long) -> Unit,
    onProfileClick: (String) -> Unit,
    onViewStory: (Story) -> Unit,
    @Suppress("unused") isPremium: Boolean,
    @Suppress("unused") onUpgradeClick: () -> Unit,
    isMockInterfaceEnabled: Boolean,
    animationSettings: AnimationSettings = AnimationSettings(),
    modifier: Modifier = Modifier,
    safetyState: SafetyState = SafetyState(),
    enableNewFeedLayout: Boolean = true,
    onSettingsClick: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onReportPost: (Long, String) -> Unit = { _, _ -> },
    onHidePost: ((Long) -> Unit)? = null,
    onBlockUser: ((String) -> Unit)? = null,
    onBookmarkToggle: ((Long) -> Unit)? = null,
    onFollowToggle: ((String) -> Unit)? = null
) {
    val adsState by com.kyilmaz.neurocomet.ads.GoogleAdsManager.adsState.collectAsState()
    val context = LocalContext.current
    val canonicalLayout = LocalCanonicalLayout.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }
    val isPostingBlocked = shouldBlockFeature(parentalState, BlockableFeature.POSTING) != null
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val feedContentMaxWidth = ((canonicalLayout.maxContentWidthDp ?: canonicalLayout.widthDp).coerceAtMost(920)).dp
    val liquidGlassVariant = remember(context) { ABTestManager.getVariant(context, ABExperiment.LIQUID_GLASS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var showPostingBlockedMessage by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(FeedFilter.FOR_YOU) }
    val visibleStories = remember(feedUiState.stories, feedUiState.blockedUserIds) { feedUiState.stories.filterNot { story -> story.userId.isNotBlank() && feedUiState.blockedUserIds.contains(story.userId) } }
    val filteredPosts = remember(feedUiState.posts, feedUiState.blockedUserIds, selectedFilter, safetyState.audience) {
        val basePosts = feedUiState.posts.filterNot { post -> post.userId != null && feedUiState.blockedUserIds.contains(post.userId) }
        val categoryFiltered = when (selectedFilter) {
            FeedFilter.FOR_YOU -> basePosts
            FeedFilter.FOLLOWING -> basePosts.take(5)
            FeedFilter.TRENDING -> basePosts.sortedByDescending { it.likes + it.comments }
            FeedFilter.SUPPORT -> basePosts.filter { post -> val lower = post.content.lowercase(); lower.contains("support") || lower.contains("help") || lower.contains("reminder") }.ifEmpty { emptyList() }
            FeedFilter.WINS -> basePosts.filter { post -> val lower = post.content.lowercase(); lower.contains("win") || lower.contains("celebration") || lower.contains("achievement") || post.content.contains("🎉") || post.content.contains("✨") }.ifEmpty { emptyList() }
        }
        ContentFiltering.filterPostsByAudience(categoryFiltered, safetyState.audience)
    }
    val animateLogos = animationSettings.shouldAnimate(AnimationType.LOGO)
    val animateStories = animationSettings.shouldAnimate(AnimationType.STORY)
    val animateLoading = animationSettings.shouldAnimate(AnimationType.LOADING)

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        FeedHeader(animateLogos = animateLogos, isPostingBlocked = isPostingBlocked, onCreatePost = { if (isPostingBlocked) showPostingBlockedMessage = true else showCreatePostDialog = true }, onSettings = onSettingsClick, isDark = isDark, liquidGlassVariant = liquidGlassVariant)
        val lazyListState = rememberLazyListState()
        val isFeedScrolling by remember { derivedStateOf { lazyListState.isScrollInProgress } }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val contentModifier = Modifier.fillMaxSize().widthIn(max = feedContentMaxWidth)
            if (feedUiState.isLoading && feedUiState.posts.isEmpty()) {
                Box(modifier = contentModifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        if (animateLoading) NeuroLoadingAnimation(modifier = Modifier.widthIn(max = 120.dp), color = MaterialTheme.colorScheme.primary)
                        else NeuroCometBrandMark(modifier = Modifier.size(56.dp), haloColor = MaterialTheme.colorScheme.primary, accentColor = MaterialTheme.colorScheme.secondary, motionEnabled = false)
                        Spacer(Modifier.height(16.dp))
                        Text(text = stringResource(R.string.feed_loading), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(state = lazyListState, modifier = contentModifier, contentPadding = PaddingValues(bottom = M3EDesignSystem.Spacing.bottomNavPadding), flingBehavior = ScrollableDefaults.flingBehavior()) {
                    item(key = "stories_section") { EnhancedStoriesSection(stories = visibleStories, currentUser = CURRENT_USER, isMockInterfaceEnabled = isMockInterfaceEnabled, animateStories = animateStories && !isFeedScrolling, onViewStory = onViewStory, onAddStoryClick = { showCreateStoryDialog = true }, isDark = isDark) }
                    item(key = "quick_actions") { QuickActionsRow(modifier = Modifier.fillMaxWidth().padding(horizontal = M3EDesignSystem.Spacing.md, vertical = M3EDesignSystem.Spacing.sm), onCreatePost = { if (isPostingBlocked) showPostingBlockedMessage = true else showCreatePostDialog = true }, onCreateStory = { showCreateStoryDialog = true }, isDark = isDark, variant = liquidGlassVariant) }
                    item(key = "filter_pills") {
                        val haptic = LocalHapticFeedback.current
                        FeedFilterPills(selectedFilter = selectedFilter, onFilterSelected = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedFilter = it }, isDark = isDark, variant = liquidGlassVariant)
                    }
                    item(key = "section_header") { FeedSectionHeader(title = selectedFilter.label, icon = selectedFilter.icon, count = filteredPosts.size, isDark = isDark, variant = liquidGlassVariant) }
                    val currentAdsState = adsState
                    itemsIndexed(items = filteredPosts, key = { _, post -> post.id ?: post.hashCode() }) { index, post ->
                        if (index > 0 && index % 5 == 4) {
                            com.kyilmaz.neurocomet.ads.BannerAd(modifier = Modifier.padding(horizontal = if (enableNewFeedLayout) 8.dp else 12.dp, vertical = if (enableNewFeedLayout) 4.dp else 8.dp), adKey = "feed_banner_$index")
                        }
                        BubblyPostCard(post = post, onLike = { post.id?.let(onLikePost) }, onDelete = { post.id?.let(onDeletePost) }, onReplyPost = { onReplyPost(post) }, onShare = onSharePost, isMockInterfaceEnabled = isMockInterfaceEnabled, safetyState = safetyState, onProfileClick = onProfileClick, onHashtagClick = onHashtagClick, compactMode = enableNewFeedLayout, onReport = { reason -> onReportPost(post.id ?: 0L, reason) }, onHidePost = onHidePost, onBlockUser = onBlockUser, bookmarkedPostIds = feedUiState.bookmarkedPostIds, onBookmarkToggle = onBookmarkToggle, followingUserIds = feedUiState.followingUserIds, onFollowToggle = onFollowToggle)
                    }
                }
            }
        }
    }
    if (showCreatePostDialog) EnhancedCreatePostDialog(onDismiss = { showCreatePostDialog = false }, onPost = { content, tone, imageUrl, videoUrl, backgroundColor, locationTag -> onAddPost(content, tone, imageUrl, videoUrl, backgroundColor, locationTag); showCreatePostDialog = false }, isPremium = isPremium, safetyState = safetyState)
    if (showCreateStoryDialog) CreateStoryDialog(onDismiss = { showCreateStoryDialog = false }, onPost = { contentType, contentUri, duration, textOverlay, bgColor, bgColorEnd, linkPreview -> onAddStory(contentType, contentUri, duration, textOverlay, bgColor, bgColorEnd, linkPreview); showCreateStoryDialog = false }, safetyState = safetyState)
    if (showPostingBlockedMessage) AlertDialog(onDismissRequest = { showPostingBlockedMessage = false }, title = { Text(stringResource(R.string.posting_restricted_title)) }, text = { Text(stringResource(R.string.posting_restricted_message)) }, confirmButton = { TextButton(onClick = { showPostingBlockedMessage = false }) { Text(stringResource(R.string.button_ok)) } })
}

@Composable
private fun FeedHeader(animateLogos: Boolean, isPostingBlocked: Boolean, onCreatePost: () -> Unit, onSettings: () -> Unit, isDark: Boolean, liquidGlassVariant: String = "control") {
    val useExperimentalSurface = liquidGlassVariant != "control"
    val useSkeuomorphic = isSkeumorphicVariant(liquidGlassVariant)
    val palette = if (useSkeuomorphic) rememberSkeuomorphicPalette(liquidGlassVariant) else null
    val headerContent: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                NeuroCometLogo(animateLogos = animateLogos)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "A calmer way to connect ✨", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FeedHeaderIconButton(icon = Icons.Outlined.AddBox, onClick = onCreatePost, contentDescription = "Create Post", isDark = isDark, variant = liquidGlassVariant)
                FeedHeaderIconButton(icon = Icons.Outlined.Settings, onClick = onSettings, contentDescription = "Settings", isDark = isDark, variant = liquidGlassVariant)
            }
        }
    }
    if (useExperimentalSurface && !useSkeuomorphic) {
        NeuroGlassPanel(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), blurRadius = 40f, tintColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.55f else 0.65f), content = { Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) { headerContent() } })
    } else if (useSkeuomorphic && palette != null) {
        SkeuomorphicPanel(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp), variant = liquidGlassVariant) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                headerContent()
                Text(text = if (isFullSkeumorphicVariant(liquidGlassVariant)) "Sculpted surfaces for calmer focus" else "Gentle depth with modern tactility", style = MaterialTheme.typography.labelMedium, color = palette.accent.copy(alpha = 0.85f))
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) { headerContent() }
    }
}

@Composable
private fun FeedHeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, contentDescription: String?, isDark: Boolean, variant: String = "control") {
    if (isSkeumorphicVariant(variant)) {
        val palette = rememberSkeuomorphicPalette(variant)
        SkeuomorphicPanel(modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), variant = variant) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp), tint = palette.accent) }
        }
    } else {
        Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun EnhancedStoriesSection(stories: List<Story>, currentUser: User, isMockInterfaceEnabled: Boolean, animateStories: Boolean, onViewStory: (Story) -> Unit, onAddStoryClick: () -> Unit, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StoriesRow(stories = stories, currentUser = currentUser, isMockInterfaceEnabled = isMockInterfaceEnabled, animateStories = animateStories, onViewStory = onViewStory, onAddStoryClick = onAddStoryClick)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun QuickActionsRow(modifier: Modifier = Modifier, onCreatePost: () -> Unit, onCreateStory: () -> Unit, isDark: Boolean, variant: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val useSkeuomorphic = isSkeumorphicVariant(variant)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.weight(1f), shape = M3EDesignSystem.Shapes.ExtraLargeShape, color = Color.Transparent, border = null) {
            val composerContent: @Composable () -> Unit = {
                Row(modifier = Modifier.padding(horizontal = M3EDesignSystem.Spacing.md, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(M3EDesignSystem.IconSize.xl).background(brush = Brush.linearGradient(colors = listOf(primaryColor, tertiaryColor)), shape = CircleShape), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Create, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(M3EDesignSystem.IconSize.sm))
                    }
                    Spacer(Modifier.width(M3EDesignSystem.Spacing.sm))
                    Text(text = "What's on your mind?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (useSkeuomorphic) {
                SkeuomorphicPanel(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable(onClick = onCreatePost), shape = RoundedCornerShape(28.dp), variant = variant) { composerContent() }
            } else {
                Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable(onClick = onCreatePost), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) { composerContent() }
            }
        }
        Spacer(Modifier.width(12.dp))
        QuickActionButton(icon = Icons.Default.Add, color = MaterialTheme.colorScheme.secondary, onClick = onCreatePost, variant = variant)
        Spacer(Modifier.width(8.dp))
        QuickActionButton(icon = Icons.Outlined.AutoAwesome, color = MaterialTheme.colorScheme.tertiary, onClick = onCreateStory, variant = variant)
    }
}

@Composable
private fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, variant: String) {
    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicPanel(modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick), shape = CircleShape, variant = variant) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) }
        }
    } else {
        Surface(modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick), shape = CircleShape, color = color.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) }
        }
    }
}

@Composable
private fun FeedFilterPills(selectedFilter: FeedFilter, onFilterSelected: (FeedFilter) -> Unit, isDark: Boolean, variant: String) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(count = FeedFilter.entries.size, key = { FeedFilter.entries[it].name }) { index ->
            val filter = FeedFilter.entries[index]
            FeedFilterPill(filter = filter, isSelected = selectedFilter == filter, onClick = { onFilterSelected(filter) }, isDark = isDark, variant = variant)
        }
    }
}

@Composable
private fun FeedFilterPill(filter: FeedFilter, isSelected: Boolean, onClick: () -> Unit, isDark: Boolean, variant: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val useSkeuomorphic = isSkeumorphicVariant(variant)
    val palette = if (useSkeuomorphic) rememberSkeuomorphicPalette(variant) else null
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    if (useSkeuomorphic && palette != null) {
        SkeuomorphicPanel(modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }.clip(RoundedCornerShape(20.dp)).clickable { coroutineScope.launch { scale.animateTo(0.95f, animationSpec = spring(stiffness = Spring.StiffnessMedium)); scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) }; onClick() }, shape = RoundedCornerShape(20.dp), variant = variant) {
            if (isSelected) Box(modifier = Modifier.matchParentSize().background(Brush.linearGradient(colors = listOf(palette.activeTop.copy(alpha = 0.92f), palette.activeBottom.copy(alpha = 0.96f)))))
            Row(modifier = Modifier.padding(horizontal = if (isSelected) 12.dp else 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = filter.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isSelected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = filter.label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, color = if (isSelected) palette.accent else MaterialTheme.colorScheme.onSurface)
            }
        }
    } else {
        Surface(modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }.clip(RoundedCornerShape(20.dp)).clickable { coroutineScope.launch { scale.animateTo(0.95f, animationSpec = spring(stiffness = Spring.StiffnessMedium)); scale.animateTo(1f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) }; onClick() }, shape = RoundedCornerShape(20.dp), color = if (isSelected) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest, border = if (isSelected) BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.4f)) else null, shadowElevation = if (isSelected) 4.dp else 0.dp, tonalElevation = if (isSelected) 2.dp else 0.dp) {
            Row(modifier = Modifier.padding(horizontal = if (isSelected) 12.dp else 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = filter.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = filter.label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun FeedSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, isDark: Boolean, variant: String) {
    val useSkeuomorphic = isSkeumorphicVariant(variant)
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        Box(modifier = Modifier.width(4.dp).height(24.dp).background(brush = Brush.verticalGradient(colors = listOf(primaryColor, tertiaryColor)), shape = RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(12.dp))
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = primaryColor)
        Spacer(Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.width(8.dp))
        if (useSkeuomorphic) {
            SkeuomorphicPanel(shape = RoundedCornerShape(10.dp), variant = variant) {
                Text(text = "$count posts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        } else {
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(text = "$count posts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
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
    onProfileClick: (String) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    compactMode: Boolean = false,
    onReport: (String) -> Unit = {},
    onHidePost: ((Long) -> Unit)? = null,
    onBlockUser: ((String) -> Unit)? = null,
    bookmarkedPostIds: Set<Long> = emptySet(),
    onBookmarkToggle: ((Long) -> Unit)? = null,
    followingUserIds: Set<String> = emptySet(),
    onFollowToggle: ((String) -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val postId = post.id ?: 0L
    var isBookmarked by remember(postId, bookmarkedPostIds) { mutableStateOf(bookmarkedPostIds.contains(postId)) }
    var showContentWarning by remember { mutableStateOf(false) }
    var isFollowing by remember(post.userId, followingUserIds) { mutableStateOf(followingUserIds.contains(post.userId)) }
    val emotionalTone = remember(post.content) { detectEmotionalTone(post.content) }
    val context = LocalContext.current
    val clipboardManager = remember(context) { context.getSystemService(android.content.ClipboardManager::class.java) }
    val contentPrefs = remember { SocialSettingsManager.getContentPreferences(context) }
    val bookmarkedText = stringResource(R.string.post_bookmarked)
    val unbookmarkedText = stringResource(R.string.post_unbookmarked)
    val copiedText = stringResource(R.string.post_copied)
    val hideText = stringResource(R.string.post_hide)
    val removedText = stringResource(R.string.toast_removed)
    val savedText = stringResource(R.string.toast_saved)
    val nowFollowingText = stringResource(R.string.post_now_following, post.userId ?: "")
    val unfollowedText = stringResource(R.string.post_unfollowed, post.userId ?: "")
    val blockedUserText = stringResource(R.string.toast_user_blocked_post, post.userId ?: "")
    val reportedText = stringResource(R.string.toast_reported)

    M3ESurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (compactMode) M3EDesignSystem.Spacing.xs else M3EDesignSystem.Spacing.sm, vertical = if (compactMode) M3EDesignSystem.Spacing.xxs else M3EDesignSystem.Spacing.xs),
        shape = if (compactMode) M3EDesignSystem.Shapes.MediumShape else RoundedCornerShape(20.dp),
        variant = if (compactMode) M3ESurfaceVariant.Settings else M3ESurfaceVariant.Feed,
        shadowElevation = if (compactMode) M3EDesignSystem.Elevation.level4 else M3EDesignSystem.Elevation.level5,
        contentPadding = PaddingValues(if (compactMode) M3EDesignSystem.Spacing.sm else M3EDesignSystem.Spacing.md),
        containerColor = post.backgroundColor?.let { Color(it.toInt()) }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val avatarUrl = if (isMockInterfaceEnabled) post.userAvatar ?: "https://i.pravatar.cc/150?u=${post.userId}" else post.userAvatar
                Box(modifier = Modifier.size(if (compactMode) M3EDesignSystem.AvatarSize.md else M3EDesignSystem.AvatarSize.lg).background(brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)), shape = CircleShape).padding(3.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.surface).clickable { post.userId?.let { onProfileClick(it) } }, contentAlignment = Alignment.Center) {
                        if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
                        else { AsyncImage(model = ImageRequest.Builder(context).data(avatarUrl ?: "https://i.pravatar.cc/150?u=${post.userId}").crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    }
                }
                Spacer(Modifier.width(if (compactMode) 10.dp else 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = post.userId ?: "Unknown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (isFollowing) { Spacer(Modifier.width(8.dp)); Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) { Text(text = "Following", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) } }
                    }
                    Text(text = post.timeAgo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!post.locationTag.isNullOrBlank()) { Spacer(Modifier.height(2.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.LocationOn, contentDescription = "Location", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(2.dp)); Text(text = post.locationTag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(if (isBookmarked) stringResource(R.string.post_unsave) else stringResource(R.string.post_save), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { isBookmarked = !isBookmarked; showMenu = false; post.id?.let { onBookmarkToggle?.invoke(it) }; Toast.makeText(context, if (isBookmarked) bookmarkedText else unbookmarkedText, Toast.LENGTH_SHORT).show() })
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_copy_text), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { clipboardManager?.setPrimaryClip(ClipData.newPlainText("NeuroComet post text", post.content)); showMenu = false; Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show() })
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_share), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { onShare(context, post); showMenu = false })
                        HorizontalDivider()
                        if (post.userId != currentUserId) { DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (isFollowing) Icons.Filled.PersonOff else Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(if (isFollowing) R.string.post_unfollow_user else R.string.post_follow_user, post.userId ?: ""), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { isFollowing = !isFollowing; showMenu = false; post.userId?.let { onFollowToggle?.invoke(it) }; Toast.makeText(context, if (isFollowing) nowFollowingText else unfollowedText, Toast.LENGTH_SHORT).show() }) }
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.NotInterested, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_not_interested), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { showMenu = false; if (postId != 0L) onHidePost?.invoke(postId); Toast.makeText(context, removedText, Toast.LENGTH_SHORT).show() })
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_hide), color = MaterialTheme.colorScheme.onSurface) } }, onClick = { showMenu = false; if (postId != 0L) onHidePost?.invoke(postId); Toast.makeText(context, hideText, Toast.LENGTH_SHORT).show() })
                        HorizontalDivider()
                        if (post.userId != currentUserId) { DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_block_user, post.userId ?: ""), color = MaterialTheme.colorScheme.error) } }, onClick = { showMenu = false; post.userId?.let { uid -> onBlockUser?.invoke(uid) }; Toast.makeText(context, blockedUserText, Toast.LENGTH_SHORT).show() }) }
                        DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_report_post), color = MaterialTheme.colorScheme.error) } }, onClick = { showMenu = false; showReportDialog = true })
                        if (post.userId == currentUserId || currentUserId.isEmpty()) { DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(12.dp)); Text(stringResource(R.string.post_delete_post), color = MaterialTheme.colorScheme.error) } }, onClick = { showMenu = false; onDelete() }) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            val shouldHide = safetyState.isKidsMode && ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)
            if (shouldHide) { Text(text = "🔒 Content hidden for kids mode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else {
                val textToShow = if (safetyState.isKidsMode) ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel) else post.content
                NeuroLinkedText(text = textToShow, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp), linkStyle = defaultNeuroLinkStyle(), onLinkClick = { link -> when (link.type) { com.kyilmaz.neurocomet.ui.components.LinkType.MENTION -> onProfileClick(link.text.removePrefix("@")); com.kyilmaz.neurocomet.ui.components.LinkType.HASHTAG -> onHashtagClick(link.text.removePrefix("#")); else -> Unit } })
            }
            val showMedia = !safetyState.isKidsMode
            val mediaItems = post.getAllMedia()
            if (showMedia && mediaItems.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                if (mediaItems.size == 1) {
                    val item = mediaItems.first()
                    when (item.type) {
                        MediaType.VIDEO -> VideoPlayerView(videoUrl = item.url, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)))
                        MediaType.IMAGE -> AsyncImage(model = item.url, contentDescription = item.altText, modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit)
                    }
                } else { PostMediaCarousel(mediaItems = mediaItems, modifier = Modifier.fillMaxWidth().height(280.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onLike) { Icon(imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (post.likes > 0 && !contentPrefs.hideLikeCounts) { Text(text = formatCount(post.likes), style = MaterialTheme.typography.labelMedium, color = if (post.isLikedByMe) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onReplyPost) { Icon(imageVector = Icons.AutoMirrored.Outlined.Comment, contentDescription = "Comment", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = { onShare(context, post) }) { Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                IconButton(onClick = { isBookmarked = !isBookmarked; post.id?.let { onBookmarkToggle?.invoke(it) }; Toast.makeText(context, if (isBookmarked) savedText else removedText, Toast.LENGTH_SHORT).show() }) { Icon(imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark", tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (emotionalTone != EmotionalTone.NEUTRAL && !safetyState.isKidsMode) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = emotionalTone.backgroundColor, border = BorderStroke(1.dp, emotionalTone.textColor.copy(alpha = 0.35f))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = emotionalTone.emoji, fontSize = 13.sp)
                        Text(text = stringResource(emotionalTone.labelResId), style = MaterialTheme.typography.labelMedium, color = emotionalTone.textColor, fontWeight = FontWeight.Medium)
                        if (emotionalTone.showWarning) { Text(text = "·", color = emotionalTone.textColor.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium); Text(text = "sensitive", style = MaterialTheme.typography.labelSmall, color = emotionalTone.textColor.copy(alpha = 0.75f)) }
                    }
                }
            }
        }
    }
    if (showReportDialog) { ReportPostDialog(postId = post.id?.toString() ?: "", onDismiss = { showReportDialog = false }, onReport = { reason -> onReport(reason); showReportDialog = false; Toast.makeText(context, reportedText, Toast.LENGTH_LONG).show() }) }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
fun PostMediaCarousel(mediaItems: List<MediaItem>, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { mediaItems.size.coerceAtMost(Post.MAX_MEDIA_ITEMS) })
    Box(modifier = modifier) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), pageSpacing = 0.dp, beyondViewportPageCount = 1) { page ->
            val item = mediaItems.getOrNull(page) ?: return@HorizontalPager
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (item.type) {
                    MediaType.VIDEO -> VideoPlayerView(videoUrl = item.url, modifier = Modifier.fillMaxSize())
                    MediaType.IMAGE -> AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(item.url).crossfade(true).build(), contentDescription = item.altText, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        }
        if (mediaItems.size > 1) {
            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.6f)) { Text(text = "${pagerState.currentPage + 1}/${mediaItems.size}", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(mediaItems.size.coerceAtMost(10)) { index ->
                    val displayIndex = if (mediaItems.size <= 10) index else { val start = (pagerState.currentPage - 4).coerceAtLeast(0); start + index }
                    if (displayIndex < mediaItems.size) { val isSelected = pagerState.currentPage == displayIndex; Box(modifier = Modifier.size(if (isSelected) 8.dp else 6.dp).background(color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f), shape = CircleShape)) }
                }
                if (mediaItems.size > 10 && pagerState.currentPage < mediaItems.size - 5) { Text(text = "•••", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
private fun ReportPostDialog(postId: String, onDismiss: () -> Unit, onReport: (String) -> Unit) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var additionalInfo by remember { mutableStateOf("") }
    val reportReasons = listOf("🚫" to "Spam or scam", "😠" to "Harassment or bullying", "⚠️" to "Harmful misinformation", "🔞" to "Inappropriate content", "💔" to "Self-harm or suicide content", "🎭" to "Impersonation", "📢" to "Hate speech", "🤖" to "Bot or fake account", "❓" to "Something else")
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.report_dialog_title), fontWeight = FontWeight.Bold) }, text = { Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Why are you reporting this post?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Your report is anonymous and helps keep our community safe.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)); Spacer(Modifier.height(8.dp)); reportReasons.forEach { (emoji, reason) -> val isSelected = selectedReason == reason; Card(modifier = Modifier.fillMaxWidth().clickable { selectedReason = reason }, colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) { Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(emoji, style = MaterialTheme.typography.bodyLarge); Spacer(Modifier.width(12.dp)); Text(reason, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) } } }; if (selectedReason == "Something else") { Spacer(Modifier.height(8.dp)); OutlinedTextField(value = additionalInfo, onValueChange = { additionalInfo = it }, label = { Text(stringResource(R.string.report_dialog_reason_hint)) }, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), maxLines = 4, minLines = 2) } } }, confirmButton = { Button(onClick = { selectedReason?.let { reason -> val finalReason = if (reason == "Something else" && additionalInfo.isNotBlank()) "$reason: ${additionalInfo.trim()}" else reason; onReport(finalReason) } }, enabled = selectedReason != null) { Text(stringResource(R.string.report_dialog_submit)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.report_dialog_cancel)) } })
}

private fun formatMomentTime(itemCount: Int): String {
    return when { itemCount <= 1 -> "Just now"; itemCount == 2 -> "2h"; itemCount == 3 -> "5h"; else -> "12h" }
}

@Composable
fun StoriesRow(stories: List<Story>, currentUser: User, isMockInterfaceEnabled: Boolean, animateStories: Boolean = true, onViewStory: (Story) -> Unit, onAddStoryClick: () -> Unit, modifier: Modifier = Modifier) {
    val unseenCount = stories.count { !it.isViewed }
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(brush = Brush.linearGradient(colors = listOf(Color(0xFF6C63FF), Color(0xFF7C4DFF), Color(0xFFB388FF))), shape = RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                Text(stringResource(R.string.moments_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            if (unseenCount > 0) { Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) { Text(text = stringResource(R.string.moments_new_count, unseenCount), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), letterSpacing = 0.25.sp) } }
        }
        val momentsListState = rememberLazyListState()
        val isScrolling by remember { derivedStateOf { momentsListState.isScrollInProgress } }
        LazyRow(state = momentsListState, horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp), flingBehavior = ScrollableDefaults.flingBehavior()) {
            item(key = "add_moment") { NeuroMomentItem(userAvatar = currentUser.avatarUrl, username = stringResource(R.string.moments_your_story), timeAgo = null, isAddButton = true, isViewed = false, isMockInterfaceEnabled = isMockInterfaceEnabled, animationEnabled = animateStories && !isScrolling, onClick = onAddStoryClick) }
            items(stories, key = { it.id }) { story -> NeuroMomentItem(userAvatar = story.userAvatar, username = story.userName.split(" ").firstOrNull()?.take(10) ?: story.userName.take(10), timeAgo = formatMomentTime(story.items.size), isAddButton = false, isViewed = story.isViewed, isMockInterfaceEnabled = isMockInterfaceEnabled, animationEnabled = animateStories && !isScrolling, onClick = { onViewStory(story) }) }
        }
    }
}

@Composable
fun NeuroMomentItem(userAvatar: String?, username: String, timeAgo: String? = null, isAddButton: Boolean, isViewed: Boolean, isMockInterfaceEnabled: Boolean, animationEnabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unviewedGradient = remember { listOf(Color(0xFF6C63FF), Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFF6C63FF)) }
    val viewedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val addButtonColor = MaterialTheme.colorScheme.primary
    val shouldAnimate = animationEnabled && !isViewed && !isAddButton
    val gradientRotation = if (shouldAnimate) { val infiniteTransition = rememberInfiniteTransition(label = "moment-glow"); val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "gradient-rotation"); rotation } else 0f
    Column(modifier = modifier.width(72.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(66.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isViewed) 1.5.dp.toPx() else 2.8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                if (isAddButton) drawCircle(color = addButtonColor, radius = radius, style = Stroke(width = strokeWidth, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)))
                else if (!isViewed) rotate(gradientRotation) { drawCircle(brush = Brush.sweepGradient(unviewedGradient), radius = radius, style = Stroke(width = strokeWidth)) }
                else drawCircle(color = viewedColor, radius = radius, style = Stroke(width = strokeWidth))
            }
            Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (isAddButton) Icon(Icons.Default.Add, contentDescription = stringResource(R.string.story_add_button_content_description), modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
                else {
                    val avatarUrl = if (isMockInterfaceEnabled) userAvatar ?: "https://i.pravatar.cc/150?u=$username" else userAvatar
                    if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) Icon(Icons.Default.AccountCircle, contentDescription = stringResource(R.string.story_user_story_content_description, username), modifier = Modifier.fillMaxSize(), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
                    else AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(100).memoryCacheKey(avatarUrl).size(116).build(), contentDescription = stringResource(R.string.story_user_story_content_description, username), modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (isViewed) 0.75f else 1f }, contentScale = ContentScale.Crop)
                }
            }
            if (isAddButton) Box(modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).background(color = MaterialTheme.colorScheme.primary, shape = CircleShape).border(width = 2.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary) }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = username, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = if (isViewed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 11.sp, lineHeight = 13.sp)
        if (!isAddButton && !timeAgo.isNullOrEmpty()) Text(text = timeAgo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 10.sp, lineHeight = 12.sp)
    }
}

@Composable
fun VideoPlayerView(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { setMediaItem(ExoMediaItem.fromUri(videoUrl)); prepare(); playWhenReady = false } }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    AndroidView(modifier = modifier, factory = { PlayerView(context).apply { player = exoPlayer; useController = true; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) } })
}

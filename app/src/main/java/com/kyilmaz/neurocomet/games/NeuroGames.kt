package com.kyilmaz.neurocomet.games

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyilmaz.neurocomet.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * NeuroGames - Stim-friendly, neurodivergent-centric mini-games
 *
 * These games are designed to:
 * - Provide satisfying sensory feedback (haptic, visual, audio)
 * - Be non-competitive and stress-free
 * - Support stimming behaviors
 * - Have no time pressure unless opted in
 * - Be accessible with clear visual feedback
 */

// =============================================================================
// GAME DATA MODELS
// =============================================================================

data class NeuroGame(
    val id: String,
    val nameRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val requiredAchievements: Int,
    val isStimFriendly: Boolean = true,
    val tags: List<String> = emptyList()
)

/**
 * Achievement tracker for unlocking games
 */
object GameUnlockManager {
    private const val PREFS_NAME = "neuro_games_prefs"
    private const val KEY_ACHIEVEMENTS = "achievement_count"
    private const val KEY_UNLOCKED_GAMES = "unlocked_games"

    fun getAchievementCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ACHIEVEMENTS, 0)
    }

    fun addAchievement(context: Context, count: Int = 1) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_ACHIEVEMENTS, 0)
        prefs.edit().putInt(KEY_ACHIEVEMENTS, current + count).apply()
    }

    fun isGameUnlocked(context: Context, game: NeuroGame): Boolean {
        return getAchievementCount(context) >= game.requiredAchievements
    }

    fun getUnlockedGames(context: Context): List<NeuroGame> {
        val count = getAchievementCount(context)
        return ALL_GAMES.filter { it.requiredAchievements <= count }
    }

    fun getLockedGames(context: Context): List<NeuroGame> {
        val count = getAchievementCount(context)
        return ALL_GAMES.filter { it.requiredAchievements > count }
    }

    // For testing purposes
    fun setAchievementCount(context: Context, count: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ACHIEVEMENTS, count).apply()
    }
}

// All available games
val ALL_GAMES = listOf(
    NeuroGame(
        id = "bubble_pop",
        nameRes = R.string.game_bubble_pop,
        descriptionRes = R.string.game_bubble_pop_desc,
        icon = Icons.Outlined.BubbleChart,
        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
        requiredAchievements = 0, // Free to play
        tags = listOf("stim", "relaxing", "satisfying")
    ),
    NeuroGame(
        id = "fidget_spinner",
        nameRes = R.string.game_fidget_spinner,
        descriptionRes = R.string.game_fidget_spinner_desc,
        icon = Icons.Outlined.RotateRight,
        gradientColors = listOf(Color(0xFF11998e), Color(0xFF38ef7d)),
        requiredAchievements = 3,
        tags = listOf("stim", "calming", "spin")
    ),
    NeuroGame(
        id = "color_flow",
        nameRes = R.string.game_color_flow,
        descriptionRes = R.string.game_color_flow_desc,
        icon = Icons.Outlined.Palette,
        gradientColors = listOf(Color(0xFFfc4a1a), Color(0xFFf7b733)),
        requiredAchievements = 5,
        tags = listOf("visual", "relaxing", "colors")
    ),
    NeuroGame(
        id = "pattern_tap",
        nameRes = R.string.game_pattern_tap,
        descriptionRes = R.string.game_pattern_tap_desc,
        icon = Icons.Outlined.GridView,
        gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
        requiredAchievements = 8,
        tags = listOf("focus", "memory", "patterns")
    ),
    NeuroGame(
        id = "infinity_draw",
        nameRes = R.string.game_infinity_draw,
        descriptionRes = R.string.game_infinity_draw_desc,
        icon = Icons.Outlined.Draw,
        gradientColors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
        requiredAchievements = 12,
        tags = listOf("stim", "creative", "calming")
    ),
    NeuroGame(
        id = "sensory_rain",
        nameRes = R.string.game_sensory_rain,
        descriptionRes = R.string.game_sensory_rain_desc,
        icon = Icons.Outlined.WaterDrop,
        gradientColors = listOf(Color(0xFF00c6ff), Color(0xFF0072ff)),
        requiredAchievements = 15,
        tags = listOf("relaxing", "asmr", "visual")
    ),
    NeuroGame(
        id = "breathing_bubbles",
        nameRes = R.string.game_breathing_bubbles,
        descriptionRes = R.string.game_breathing_bubbles_desc,
        icon = Icons.Outlined.Air,
        gradientColors = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)),
        requiredAchievements = 18,
        tags = listOf("calming", "breathing", "anxiety")
    ),
    NeuroGame(
        id = "texture_tiles",
        nameRes = R.string.game_texture_tiles,
        descriptionRes = R.string.game_texture_tiles_desc,
        icon = Icons.Outlined.TouchApp,
        gradientColors = listOf(Color(0xFFf953c6), Color(0xFFb91d73)),
        requiredAchievements = 22,
        tags = listOf("stim", "haptic", "sensory")
    ),
    NeuroGame(
        id = "sound_garden",
        nameRes = R.string.game_sound_garden,
        descriptionRes = R.string.game_sound_garden_desc,
        icon = Icons.Outlined.MusicNote,
        gradientColors = listOf(Color(0xFF43cea2), Color(0xFF185a9d)),
        requiredAchievements = 25,
        tags = listOf("audio", "creative", "relaxing")
    ),
    // Newly unlocked games (formerly in-development)
    NeuroGame(
        id = "stim_sequencer",
        nameRes = R.string.game_stim_sequencer,
        descriptionRes = R.string.game_stim_sequencer_desc,
        icon = Icons.Outlined.Piano,
        gradientColors = listOf(Color(0xFF654ea3), Color(0xFFeaafc8)),
        requiredAchievements = 28,
        tags = listOf("stim", "music", "rhythm")
    ),
    NeuroGame(
        id = "emotion_garden",
        nameRes = R.string.game_emotion_garden,
        descriptionRes = R.string.game_emotion_garden_desc,
        icon = Icons.Outlined.LocalFlorist,
        gradientColors = listOf(Color(0xFFff9966), Color(0xFFff5e62)),
        requiredAchievements = 32,
        tags = listOf("emotional", "creative", "mindfulness")
    ),
    NeuroGame(
        id = "safe_space",
        nameRes = R.string.game_safe_space,
        descriptionRes = R.string.game_safe_space_desc,
        icon = Icons.Outlined.Home,
        gradientColors = listOf(Color(0xFF7F7FD5), Color(0xFF86A8E7)),
        requiredAchievements = 36,
        tags = listOf("calming", "creative", "safe")
    ),
    NeuroGame(
        id = "worry_jar",
        nameRes = R.string.game_worry_jar,
        descriptionRes = R.string.game_worry_jar_desc,
        icon = Icons.Outlined.Psychology,
        gradientColors = listOf(Color(0xFFa8c0ff), Color(0xFF3f2b96)),
        requiredAchievements = 40,
        tags = listOf("anxiety", "calming", "mindfulness")
    ),
    NeuroGame(
        id = "constellation_connect",
        nameRes = R.string.game_constellation_connect,
        descriptionRes = R.string.game_constellation_connect_desc,
        icon = Icons.Outlined.AutoAwesome,
        gradientColors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
        requiredAchievements = 45,
        tags = listOf("relaxing", "visual", "creative")
    ),
    NeuroGame(
        id = "zen_sand",
        nameRes = R.string.game_zen_sand,
        descriptionRes = R.string.game_zen_sand_desc,
        icon = Icons.Outlined.Waves,
        gradientColors = listOf(Color(0xFFC9B18E), Color(0xFFE8D9B5)),
        requiredAchievements = 50,
        tags = listOf("stim", "calming", "zen")
    ),
    NeuroGame(
        id = "mood_mixer",
        nameRes = R.string.game_mood_mixer,
        descriptionRes = R.string.game_mood_mixer_desc,
        icon = Icons.Outlined.BlurOn,
        gradientColors = listOf(Color(0xFFee9ca7), Color(0xFFffdde1)),
        requiredAchievements = 55,
        tags = listOf("colors", "creative", "calming")
    )
)

// Games currently in development - shown as coming soon
// All previously in-development games have been completed and moved to ALL_GAMES!
val IN_DEVELOPMENT_GAMES = emptyList<NeuroGame>()

// =============================================================================
// GAMES HUB SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesHubScreen(
    onBack: () -> Unit,
    onGameSelected: (NeuroGame) -> Unit
) {
    val context = LocalContext.current
    val achievementCount = remember { mutableIntStateOf(GameUnlockManager.getAchievementCount(context)) }
    val unlockedGames = remember(achievementCount.intValue) { GameUnlockManager.getUnlockedGames(context) }
    val lockedGames = remember(achievementCount.intValue) { GameUnlockManager.getLockedGames(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.SportsEsports,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.games_hub_title),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Achievement progress card
            item {
                AchievementProgressCard(
                    achievementCount = achievementCount.intValue,
                    nextUnlock = lockedGames.firstOrNull()
                )
            }

            // Unlocked games section
            if (unlockedGames.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.games_unlocked),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(unlockedGames) { game ->
                    GameCard(
                        game = game,
                        isUnlocked = true,
                        achievementCount = achievementCount.intValue,
                        onClick = { onGameSelected(game) }
                    )
                }
            }

            // Locked games section
            if (lockedGames.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.games_locked),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(lockedGames) { game ->
                    GameCard(
                        game = game,
                        isUnlocked = false,
                        achievementCount = achievementCount.intValue,
                        onClick = { /* Show locked message */ }
                    )
                }
            }

            // Coming Soon / In Development section
            if (IN_DEVELOPMENT_GAMES.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.game_in_development),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "These games are being crafted with care! ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(IN_DEVELOPMENT_GAMES) { game ->
                    ComingSoonGameCard(game = game)
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AchievementProgressCard(
    achievementCount: Int,
    nextUnlock: NeuroGame?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.games_your_achievements),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "$achievementCount ${stringResource(R.string.games_achievements)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (nextUnlock != null) {
                Spacer(Modifier.height(16.dp))
                val progress = achievementCount.toFloat() / nextUnlock.requiredAchievements
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.games_next_unlock, nextUnlock.requiredAchievements - achievementCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    game: NeuroGame,
    isUnlocked: Boolean,
    achievementCount: Int,
    onClick: () -> Unit
) {
    val alpha = if (isUnlocked) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (isUnlocked) game.gradientColors
                        else listOf(Color.Gray, Color.DarkGray)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Icon(
                        game.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Game info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(game.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    stringResource(game.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )

                if (!isUnlocked) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.games_requires_achievements, game.requiredAchievements),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Tags
            if (isUnlocked && game.isStimFriendly) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "✨",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonGameCard(game: NeuroGame) {
    val infiniteTransition = rememberInfiniteTransition(label = "coming_soon")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = 0.7f },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        game.gradientColors.map { it.copy(alpha = shimmerAlpha) }
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon with construction badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    game.icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(28.dp)
                )
                // Construction badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚧", fontSize = 10.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            // Game info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(game.nameRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            stringResource(R.string.game_coming_soon),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(game.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    game.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Coming soon icon
            Icon(
                Icons.Outlined.Upcoming,
                contentDescription = "Coming Soon",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// =============================================================================
// BUBBLE POP GAME - Free stim-friendly game
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblePopGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Game state
    var bubbles by remember { mutableStateOf(generateBubbles(12)) }
    var poppedCount by remember { mutableIntStateOf(0) }
    var totalPopped by remember { mutableIntStateOf(0) }

    // Auto-regenerate bubbles
    LaunchedEffect(poppedCount) {
        if (poppedCount >= 8) {
            delay(500)
            bubbles = generateBubbles(12)
            poppedCount = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_bubble_pop)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "🫧 $totalPopped",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bubbles.size) { index ->
                    val bubble = bubbles[index]
                    if (!bubble.isPopped) {
                        BubbleItem(
                            bubble = bubble,
                            onPop = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                bubbles = bubbles.toMutableList().also {
                                    it[index] = bubble.copy(isPopped = true)
                                }
                                poppedCount++
                                totalPopped++
                            }
                        )
                    } else {
                        Spacer(Modifier.size(70.dp))
                    }
                }
            }

            // Hint text
            Text(
                stringResource(R.string.game_bubble_pop_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

data class Bubble(
    val id: Int,
    val color: Color,
    val size: Float,
    val isPopped: Boolean = false
)

private fun generateBubbles(count: Int): List<Bubble> {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA),
        Color(0xFFFCBAD3),
        Color(0xFFA8D8EA)
    )
    return List(count) { index ->
        Bubble(
            id = index,
            color = colors.random(),
            size = Random.nextFloat() * 20f + 50f
        )
    }
}

@Composable
private fun BubbleItem(
    bubble: Bubble,
    onPop: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bubbleScale"
    )

    Box(
        modifier = Modifier
            .size(bubble.size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        bubble.color.copy(alpha = 0.9f),
                        bubble.color.copy(alpha = 0.6f),
                        bubble.color.copy(alpha = 0.3f)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onPop()
                    }
                )
            },
        contentAlignment = Alignment.TopStart
    ) {
        // Bubble shine effect
        Box(
            modifier = Modifier
                .size((bubble.size * 0.3f).dp)
                .offset(x = 8.dp, y = 8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.4f))
        )
    }
}

// =============================================================================
// FIDGET SPINNER GAME
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidgetSpinnerGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var rotation by remember { mutableFloatStateOf(0f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var totalSpins by remember { mutableFloatStateOf(0f) }

    // Physics update
    LaunchedEffect(velocity) {
        while (abs(velocity) > 0.1f) {
            delay(16) // ~60fps
            rotation += velocity
            totalSpins += abs(velocity) / 360f
            velocity *= 0.995f // Friction
        }
        velocity = 0f
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(16, easing = LinearEasing),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_fidget_spinner)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "🌀 ${totalSpins.toInt()} spins",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF11998e),
                            Color(0xFF38ef7d),
                            Color(0xFF0f9b0f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Spinner
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .rotate(animatedRotation)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Calculate rotation from drag
                                val dragRotation = (dragAmount.x + dragAmount.y) * 0.5f
                                velocity += dragRotation * 0.1f
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Spinner arms
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .offset(
                                x = (80 * kotlin.math.cos(Math.toRadians(index * 120.0))).dp,
                                y = (80 * kotlin.math.sin(Math.toRadians(index * 120.0))).dp
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color.White, Color.White.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                // Center circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1a1a2e))
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("♾️", fontSize = 24.sp)
                }
            }

            // Instructions
            Text(
                stringResource(R.string.game_fidget_spinner_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

// =============================================================================
// COLOR FLOW GAME - Relaxing color mixing
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorFlowGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    var currentColors by remember {
        mutableStateOf(listOf(Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D)))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "colorFlow")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowProgress"
    )

    fun shuffleColors() {
        val allColors = listOf(
            Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
            Color(0xFF95E1D3), Color(0xFFF38181), Color(0xFFAA96DA),
            Color(0xFFFCBAD3), Color(0xFFA8D8EA), Color(0xFF667eea),
            Color(0xFF764ba2), Color(0xFF11998e), Color(0xFF38ef7d)
        )
        currentColors = allColors.shuffled().take(3)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_color_flow)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = currentColors,
                        startY = animatedProgress * 1000f
                    )
                )
                .clickable { shuffleColors() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎨",
                    fontSize = 64.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.game_color_flow_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============================================================================
// GAME SCREEN ROUTER
// =============================================================================

@Composable
fun GameScreen(
    gameId: String,
    onBack: () -> Unit
) {
    when (gameId) {
        "bubble_pop" -> BubblePopGame(onBack)
        "fidget_spinner" -> FidgetSpinnerGame(onBack)
        "color_flow" -> ColorFlowGame(onBack)
        // In-Development Games with functional prototypes
        "stim_sequencer" -> StimSequencerGame(onBack)
        "emotion_garden" -> EmotionGardenGame(onBack)
        "safe_space" -> SafeSpaceBuilderGame(onBack)
        "worry_jar" -> WorryJarGame(onBack)
        "constellation_connect" -> ConstellationConnectGame(onBack)
        "zen_sand" -> ZenSandGardenGame(onBack)
        "mood_mixer" -> MoodMixerGame(onBack)
        else -> {
            // Coming soon placeholder
            ComingSoonGame(onBack)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComingSoonGame(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.games_coming_soon)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🚧", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.games_coming_soon_message),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============================================================================
// STIM SEQUENCER GAME - Create satisfying rhythm patterns
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StimSequencerGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // 4x4 grid of pads
    var pads by remember { mutableStateOf(List(16) { false }) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(-1) }
    var bpm by remember { mutableFloatStateOf(120f) }

    val padColors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D), Color(0xFF95E1D3),
        Color(0xFFF38181), Color(0xFFAA96DA), Color(0xFFFCBAD3), Color(0xFFA8D8EA),
        Color(0xFF667eea), Color(0xFF764ba2), Color(0xFF11998e), Color(0xFF38ef7d),
        Color(0xFFfc4a1a), Color(0xFFf7b733), Color(0xFF8E2DE2), Color(0xFF4A00E0)
    )

    // Playback loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                currentStep = (currentStep + 1) % 16
                if (pads[currentStep]) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                delay((60000 / bpm / 4).toLong())
            }
        } else {
            currentStep = -1
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_stim_sequencer)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF654ea3), Color(0xFFeaafc8))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BPM Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("BPM: ${bpm.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                Slider(
                    value = bpm,
                    onValueChange = { bpm = it },
                    valueRange = 60f..200f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 4x4 Pad Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(16) { index ->
                    val isActive = pads[index]
                    val isCurrent = currentStep == index

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) padColors[index]
                                else Color.White.copy(alpha = 0.2f)
                            )
                            .border(
                                width = if (isCurrent) 4.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .scale(if (isCurrent && isActive) 1.1f else 1f)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                pads = pads.toMutableList().also { it[index] = !it[index] }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Text("♪", fontSize = 24.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Clear button
            OutlinedButton(
                onClick = { pads = List(16) { false } },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Clear, null)
                Spacer(Modifier.width(8.dp))
                Text("Clear All")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.game_stim_sequencer_hint),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// =============================================================================
// EMOTION GARDEN GAME - Grow flowers representing feelings
// =============================================================================

data class EmotionFlower(
    val id: Int,
    val emotion: String,
    val emoji: String,
    val color: Color,
    val x: Float,
    val y: Float,
    var growth: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionGardenGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var flowers by remember { mutableStateOf(listOf<EmotionFlower>()) }
    var selectedEmotion by remember { mutableStateOf<Pair<String, String>?>(null) }
    var nextId by remember { mutableIntStateOf(0) }

    val emotions = listOf(
        "Happy" to "😊", "Calm" to "😌", "Excited" to "🤩",
        "Loved" to "🥰", "Grateful" to "🙏", "Peaceful" to "☮️",
        "Hopeful" to "🌈", "Proud" to "💪", "Curious" to "🤔"
    )

    val emotionColors = mapOf(
        "Happy" to Color(0xFFFFD700),
        "Calm" to Color(0xFF87CEEB),
        "Excited" to Color(0xFFFF6B6B),
        "Loved" to Color(0xFFFF69B4),
        "Grateful" to Color(0xFF90EE90),
        "Peaceful" to Color(0xFFE6E6FA),
        "Hopeful" to Color(0xFFFFB347),
        "Proud" to Color(0xFFDDA0DD),
        "Curious" to Color(0xFF98D8C8)
    )

    // Grow flowers animation
    LaunchedEffect(flowers) {
        while (true) {
            delay(100)
            flowers = flowers.map {
                it.copy(growth = (it.growth + 0.02f).coerceAtMost(1f))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_emotion_garden)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { flowers = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Emotion selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emotions.forEach { (name, emoji) ->
                    FilterChip(
                        selected = selectedEmotion?.first == name,
                        onClick = {
                            selectedEmotion = name to emoji
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        label = { Text("$emoji $name") }
                    )
                }
            }

            // Garden area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF87CEEB), Color(0xFF90EE90), Color(0xFF228B22))
                        )
                    )
                    .pointerInput(selectedEmotion) {
                        detectTapGestures { offset ->
                            selectedEmotion?.let { (emotion, emoji) ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                flowers = flowers + EmotionFlower(
                                    id = nextId++,
                                    emotion = emotion,
                                    emoji = emoji,
                                    color = emotionColors[emotion] ?: Color.White,
                                    x = offset.x,
                                    y = offset.y
                                )
                            }
                        }
                    }
            ) {
                // Draw flowers
                flowers.forEach { flower ->
                    val scale by animateFloatAsState(
                        targetValue = flower.growth,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "flowerGrowth"
                    )

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (flower.x - 24).dp,
                                y = (flower.y - 24).dp
                            )
                            .scale(scale)
                    ) {
                        Text(
                            text = flower.emoji,
                            fontSize = 48.sp
                        )
                    }
                }

                // Hint overlay
                if (flowers.isEmpty()) {
                    Text(
                        stringResource(R.string.game_emotion_garden_hint),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Flower count
            Text(
                "🌸 ${flowers.size} flowers planted",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// =============================================================================
// SAFE SPACE BUILDER GAME - Design your calming room
// =============================================================================

data class RoomItem(
    val id: Int,
    val emoji: String,
    val name: String,
    var x: Float,
    var y: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeSpaceBuilderGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var roomItems by remember { mutableStateOf(listOf<RoomItem>()) }
    var nextId by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<String?>(null) }

    val availableItems = listOf(
        "🛋️" to "Cozy Couch",
        "🕯️" to "Candle",
        "🪴" to "Plant",
        "📚" to "Books",
        "🧸" to "Plushie",
        "🎵" to "Music",
        "☕" to "Hot Drink",
        "🌙" to "Moon Light",
        "🔮" to "Crystal",
        "🧘" to "Meditation",
        "🎨" to "Art",
        "🌸" to "Flowers"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_safe_space)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { roomItems = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Item palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableItems.forEach { (emoji, name) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedItem == emoji)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                selectedItem = emoji
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(12.dp)
                    ) {
                        Text(emoji, fontSize = 28.sp)
                        Text(name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Room area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF7F7FD5), Color(0xFF86A8E7), Color(0xFF91EAE4))
                        )
                    )
                    .pointerInput(selectedItem) {
                        detectTapGestures { offset ->
                            selectedItem?.let { emoji ->
                                val name = availableItems.find { it.first == emoji }?.second ?: ""
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                roomItems = roomItems + RoomItem(
                                    id = nextId++,
                                    emoji = emoji,
                                    name = name,
                                    x = offset.x,
                                    y = offset.y
                                )
                            }
                        }
                    }
            ) {
                // Draw room items
                roomItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (item.x - 24).dp,
                                y = (item.y - 24).dp
                            )
                    ) {
                        Text(
                            text = item.emoji,
                            fontSize = 48.sp
                        )
                    }
                }

                // Hint overlay
                if (roomItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏠", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_safe_space_hint),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// WORRY JAR GAME - Write worries and release them
// =============================================================================

data class Worry(
    val id: Int,
    val text: String,
    var yOffset: Float = 0f,
    var alpha: Float = 1f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorryJarGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var worryText by remember { mutableStateOf("") }
    var worries by remember { mutableStateOf(listOf<Worry>()) }
    var nextId by remember { mutableIntStateOf(0) }
    var releasedCount by remember { mutableIntStateOf(0) }

    // Float worries upward
    LaunchedEffect(worries) {
        while (worries.isNotEmpty()) {
            delay(50)
            worries = worries.mapNotNull { worry ->
                val newY = worry.yOffset - 2f
                val newAlpha = worry.alpha - 0.005f
                if (newAlpha <= 0) {
                    releasedCount++
                    null
                } else {
                    worry.copy(yOffset = newY, alpha = newAlpha)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_worry_jar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "✨ $releasedCount released",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFa8c0ff), Color(0xFF3f2b96))
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Worry jar visualization
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Jar shape
                Box(
                    modifier = Modifier
                        .size(250.dp, 350.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Floating worries
                    worries.forEach { worry ->
                        Text(
                            text = worry.text.take(20) + if (worry.text.length > 20) "..." else "",
                            modifier = Modifier
                                .offset(y = worry.yOffset.dp)
                                .graphicsLayer { alpha = worry.alpha },
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (worries.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🫙", fontSize = 64.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Empty jar",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Input area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.game_worry_jar_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = worryText,
                        onValueChange = { worryText = it },
                        placeholder = { Text("What's worrying you?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (worryText.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                worries = worries + Worry(
                                    id = nextId++,
                                    text = worryText
                                )
                                worryText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = worryText.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Air, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Release Worry")
                    }
                }
            }
        }
    }
}

// =============================================================================
// CONSTELLATION CONNECT GAME - Draw lines between stars
// =============================================================================

data class Star(
    val id: Int,
    val x: Float,
    val y: Float,
    var isConnected: Boolean = false
)

data class StarConnection(
    val from: Int,
    val to: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstellationConnectGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var stars by remember { mutableStateOf(generateStars(15)) }
    var connections by remember { mutableStateOf(listOf<StarConnection>()) }
    var selectedStar by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_constellation_connect)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        "⭐ ${connections.size} lines",
                        modifier = Modifier.padding(end = 16.dp),
                        color = Color.White
                    )
                    IconButton(onClick = {
                        stars = generateStars(15)
                        connections = emptyList()
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "New Stars", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F2027)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    )
                )
        ) {
            // Draw connections
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                connections.forEach { conn ->
                    val fromStar = stars.find { it.id == conn.from }
                    val toStar = stars.find { it.id == conn.to }
                    if (fromStar != null && toStar != null) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = androidx.compose.ui.geometry.Offset(fromStar.x, fromStar.y),
                            end = androidx.compose.ui.geometry.Offset(toStar.x, toStar.y),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // Draw stars
            stars.forEach { star ->
                val isSelected = selectedStar == star.id
                val starScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.5f else 1f,
                    label = "starScale"
                )

                Box(
                    modifier = Modifier
                        .offset(
                            x = (star.x - 16).dp,
                            y = (star.y - 16).dp
                        )
                        .scale(starScale)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.Yellow
                            else Color.White.copy(alpha = 0.8f)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (selectedStar == null) {
                                selectedStar = star.id
                            } else if (selectedStar != star.id) {
                                // Create connection
                                val newConn = StarConnection(selectedStar!!, star.id)
                                if (!connections.any {
                                    (it.from == newConn.from && it.to == newConn.to) ||
                                    (it.from == newConn.to && it.to == newConn.from)
                                }) {
                                    connections = connections + newConn
                                }
                                selectedStar = null
                            } else {
                                selectedStar = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", fontSize = 16.sp, color = Color(0xFF0F2027))
                }
            }

            // Hint
            if (connections.isEmpty()) {
                Text(
                    stringResource(R.string.game_constellation_connect_hint),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun generateStars(count: Int): List<Star> {
    return List(count) { index ->
        Star(
            id = index,
            x = Random.nextFloat() * 800 + 50,
            y = Random.nextFloat() * 1000 + 100
        )
    }
}

// =============================================================================
// ZEN SAND GARDEN GAME - Draw patterns in sand
// =============================================================================

data class SandLine(
    val points: List<Pair<Float, Float>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenSandGardenGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var lines by remember { mutableStateOf(listOf<SandLine>()) }
    var currentLine by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_zen_sand)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { lines = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE8D9B5)) // Sand color
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentLine = listOf(offset.x to offset.y)
                        },
                        onDrag = { change, _ ->
                            currentLine = currentLine + (change.position.x to change.position.y)
                        },
                        onDragEnd = {
                            if (currentLine.size > 2) {
                                lines = lines + SandLine(currentLine)
                            }
                            currentLine = emptyList()
                        }
                    )
                }
        ) {
            // Draw completed lines
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val lineColor = Color(0xFF8B7355) // Dark sand

                // Draw all lines
                (lines.map { it.points } + listOf(currentLine)).forEach { points ->
                    if (points.size >= 2) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = androidx.compose.ui.geometry.Offset(points[i].first, points[i].second),
                                end = androidx.compose.ui.geometry.Offset(points[i + 1].first, points[i + 1].second),
                                strokeWidth = 8f
                            )
                        }
                    }
                }
            }

            // Decorative rocks
            Text("🪨", modifier = Modifier.offset(x = 50.dp, y = 100.dp), fontSize = 32.sp)
            Text("🪨", modifier = Modifier.offset(x = 280.dp, y = 400.dp), fontSize = 24.sp)
            Text("🪨", modifier = Modifier.offset(x = 100.dp, y = 600.dp), fontSize = 28.sp)

            // Hint
            if (lines.isEmpty() && currentLine.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("☯️", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.game_zen_sand_hint),
                        color = Color(0xFF5D4E37),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// =============================================================================
// MOOD MIXER GAME - Blend colors to match feelings
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodMixerGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var color1 by remember { mutableStateOf(Color(0xFFFF6B6B)) }
    var color2 by remember { mutableStateOf(Color(0xFF4ECDC4)) }
    var mixRatio by remember { mutableFloatStateOf(0.5f) }

    val moodColors = listOf(
        Color(0xFFFF6B6B) to "Energetic",
        Color(0xFF4ECDC4) to "Calm",
        Color(0xFFFFE66D) to "Happy",
        Color(0xFF95E1D3) to "Peaceful",
        Color(0xFFAA96DA) to "Dreamy",
        Color(0xFFF38181) to "Passionate",
        Color(0xFF6B5B95) to "Thoughtful",
        Color(0xFF88D8B0) to "Refreshed"
    )

    val mixedColor = remember(color1, color2, mixRatio) {
        Color(
            red = color1.red * (1 - mixRatio) + color2.red * mixRatio,
            green = color1.green * (1 - mixRatio) + color2.green * mixRatio,
            blue = color1.blue * (1 - mixRatio) + color2.blue * mixRatio
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_mood_mixer)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFee9ca7), Color(0xFFffdde1))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mixed color display
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(mixedColor)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Your Mood",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // Mix slider
            Text(
                "Blend the moods",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Slider(
                value = mixRatio,
                onValueChange = {
                    mixRatio = it
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Color 1 selector
            Text("First Mood:", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                moodColors.forEach { (color, name) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (color1 == color) Color.White.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable {
                                color1 = color
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Color 2 selector
            Text("Second Mood:", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                moodColors.forEach { (color, name) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (color2 == color) Color.White.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable {
                                color2 = color
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Text(name, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                stringResource(R.string.game_mood_mixer_hint),
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



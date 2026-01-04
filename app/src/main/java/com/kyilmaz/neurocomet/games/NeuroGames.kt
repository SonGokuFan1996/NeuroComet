package com.kyilmaz.neurocomet.games

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
    )
)

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


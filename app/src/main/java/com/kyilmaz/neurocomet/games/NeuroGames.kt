package com.kyilmaz.neurocomet.games

import android.content.Context
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyilmaz.neurocomet.PerformanceOptimizations
import com.kyilmaz.neurocomet.R
import com.kyilmaz.neurocomet.AppSupabaseClient
import com.kyilmaz.neurocomet.safeUpsert
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import kotlin.math.abs
import kotlin.math.pow
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
        return try { prefs.getInt(KEY_ACHIEVEMENTS, 0) } catch (_: ClassCastException) {
            try { prefs.getLong(KEY_ACHIEVEMENTS, 0).toInt() } catch (_: Exception) { 0 }
        }
    }

    fun addAchievement(context: Context, count: Int = 1) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = try { prefs.getInt(KEY_ACHIEVEMENTS, 0) } catch (_: ClassCastException) {
            try { prefs.getLong(KEY_ACHIEVEMENTS, 0).toInt() } catch (_: Exception) { 0 }
        }
        val newCount = current + count
        prefs.edit().putInt(KEY_ACHIEVEMENTS, newCount).apply()

        // Sync to Supabase
        syncAchievementsToSupabase(newCount)
    }

    private fun syncAchievementsToSupabase(count: Int) {
        val client = AppSupabaseClient.client ?: return
        val userId = try { client.auth.currentUserOrNull()?.id } catch (_: Exception) { null } ?: return

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val payload = kotlinx.serialization.json.buildJsonObject {
                    put("user_id", userId)
                    put("achievement_count", count)
                    put("updated_at", java.time.Instant.now().toString())
                }
                // upsert by user_id
                client.safeUpsert("user_game_achievements", payload, "user_id")
            } catch (e: Exception) {
                android.util.Log.w("NeuroGames", "Failed to sync achievements: ${e.message}")
            }
        }
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

/**
 * Game Tutorial data class
 */
data class GameTutorial(
    val gameId: String,
    @StringRes val titleRes: Int,
    val steps: List<GameTutorialStep>
)

data class GameTutorialStep(
    val emoji: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

/**
 * Manages game tutorial state - tracks which tutorials have been seen
 */
object GameTutorialManager {
    private const val PREFS_NAME = "neuro_games_tutorial_prefs"
    private const val KEY_PREFIX = "tutorial_seen_"

    fun hasSeenTutorial(context: Context, gameId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("$KEY_PREFIX$gameId", false)
    }

    fun markTutorialSeen(context: Context, gameId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("$KEY_PREFIX$gameId", true).apply()
    }

    fun resetAllTutorials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun getTutorial(gameId: String): GameTutorial? = gameTutorials[gameId]
}

// Define tutorials for each game
private val gameTutorials = mapOf(
    "bubble_pop" to GameTutorial(
        gameId = "bubble_pop",
        titleRes = R.string.game_bubble_pop,
        steps = listOf(
            GameTutorialStep("🫧", R.string.tutorial_bubble_pop_step1_title, R.string.tutorial_bubble_pop_step1_desc),
            GameTutorialStep("✨", R.string.tutorial_bubble_pop_step2_title, R.string.tutorial_bubble_pop_step2_desc),
            GameTutorialStep("🔄", R.string.tutorial_bubble_pop_step3_title, R.string.tutorial_bubble_pop_step3_desc)
        )
    ),
    "fidget_spinner" to GameTutorial(
        gameId = "fidget_spinner",
        titleRes = R.string.game_fidget_spinner,
        steps = listOf(
            GameTutorialStep("🌀", R.string.tutorial_fidget_step1_title, R.string.tutorial_fidget_step1_desc),
            GameTutorialStep("💨", R.string.tutorial_fidget_step2_title, R.string.tutorial_fidget_step2_desc),
            GameTutorialStep("🎯", R.string.tutorial_fidget_step3_title, R.string.tutorial_fidget_step3_desc)
        )
    ),
    "color_flow" to GameTutorial(
        gameId = "color_flow",
        titleRes = R.string.game_color_flow,
        steps = listOf(
            GameTutorialStep("🎨", R.string.tutorial_color_flow_step1_title, R.string.tutorial_color_flow_step1_desc),
            GameTutorialStep("👆", R.string.tutorial_color_flow_step2_title, R.string.tutorial_color_flow_step2_desc),
            GameTutorialStep("😌", R.string.tutorial_color_flow_step3_title, R.string.tutorial_color_flow_step3_desc)
        )
    ),
    "pattern_tap" to GameTutorial(
        gameId = "pattern_tap",
        titleRes = R.string.game_pattern_tap,
        steps = listOf(
            GameTutorialStep("👀", R.string.tutorial_pattern_step1_title, R.string.tutorial_pattern_step1_desc),
            GameTutorialStep("👆", R.string.tutorial_pattern_step2_title, R.string.tutorial_pattern_step2_desc),
            GameTutorialStep("📈", R.string.tutorial_pattern_step3_title, R.string.tutorial_pattern_step3_desc),
            GameTutorialStep("💚", R.string.tutorial_pattern_step4_title, R.string.tutorial_pattern_step4_desc)
        )
    ),
    "infinity_draw" to GameTutorial(
        gameId = "infinity_draw",
        titleRes = R.string.game_infinity_draw,
        steps = listOf(
            GameTutorialStep("✏️", R.string.tutorial_infinity_step1_title, R.string.tutorial_infinity_step1_desc),
            GameTutorialStep("🎨", R.string.tutorial_infinity_step2_title, R.string.tutorial_infinity_step2_desc),
            GameTutorialStep("♾️", R.string.tutorial_infinity_step3_title, R.string.tutorial_infinity_step3_desc),
            GameTutorialStep("🔄", R.string.tutorial_infinity_step4_title, R.string.tutorial_infinity_step4_desc)
        )
    ),
    "sensory_rain" to GameTutorial(
        gameId = "sensory_rain",
        titleRes = R.string.game_sensory_rain,
        steps = listOf(
            GameTutorialStep("🌧️", R.string.tutorial_rain_step1_title, R.string.tutorial_rain_step1_desc),
            GameTutorialStep("🎚️", R.string.tutorial_rain_step2_title, R.string.tutorial_rain_step2_desc),
            GameTutorialStep("💧", R.string.tutorial_rain_step3_title, R.string.tutorial_rain_step3_desc),
            GameTutorialStep("🎧", R.string.tutorial_rain_step4_title, R.string.tutorial_rain_step4_desc)
        )
    ),
    "breathing_bubbles" to GameTutorial(
        gameId = "breathing_bubbles",
        titleRes = R.string.game_breathing_bubbles,
        steps = listOf(
            GameTutorialStep("🫁", R.string.tutorial_breathing_step1_title, R.string.tutorial_breathing_step1_desc),
            GameTutorialStep("⬆️", R.string.tutorial_breathing_step2_title, R.string.tutorial_breathing_step2_desc),
            GameTutorialStep("⏸️", R.string.tutorial_breathing_step3_title, R.string.tutorial_breathing_step3_desc),
            GameTutorialStep("⬇️", R.string.tutorial_breathing_step4_title, R.string.tutorial_breathing_step4_desc),
            GameTutorialStep("🔁", R.string.tutorial_breathing_step5_title, R.string.tutorial_breathing_step5_desc)
        )
    ),
    "texture_tiles" to GameTutorial(
        gameId = "texture_tiles",
        titleRes = R.string.game_texture_tiles,
        steps = listOf(
            GameTutorialStep("👆", R.string.tutorial_texture_step1_title, R.string.tutorial_texture_step1_desc),
            GameTutorialStep("🪨", R.string.tutorial_texture_step2_title, R.string.tutorial_texture_step2_desc),
            GameTutorialStep("⭐", R.string.tutorial_texture_step3_title, R.string.tutorial_texture_step3_desc),
            GameTutorialStep("🔄", R.string.tutorial_texture_step4_title, R.string.tutorial_texture_step4_desc)
        )
    ),
    "sound_garden" to GameTutorial(
        gameId = "sound_garden",
        titleRes = R.string.game_sound_garden,
        steps = listOf(
            GameTutorialStep("🎵", R.string.tutorial_sound_step1_title, R.string.tutorial_sound_step1_desc),
            GameTutorialStep("🌱", R.string.tutorial_sound_step2_title, R.string.tutorial_sound_step2_desc),
            GameTutorialStep("▶️", R.string.tutorial_sound_step3_title, R.string.tutorial_sound_step3_desc),
            GameTutorialStep("🎼", R.string.tutorial_sound_step4_title, R.string.tutorial_sound_step4_desc)
        )
    ),
    "stim_sequencer" to GameTutorial(
        gameId = "stim_sequencer",
        titleRes = R.string.game_stim_sequencer,
        steps = listOf(
            GameTutorialStep("🎹", R.string.tutorial_stim_step1_title, R.string.tutorial_stim_step1_desc),
            GameTutorialStep("▶️", R.string.tutorial_stim_step2_title, R.string.tutorial_stim_step2_desc),
            GameTutorialStep("🎚️", R.string.tutorial_stim_step3_title, R.string.tutorial_stim_step3_desc),
            GameTutorialStep("🔁", R.string.tutorial_stim_step4_title, R.string.tutorial_stim_step4_desc)
        )
    ),
    "emotion_garden" to GameTutorial(
        gameId = "emotion_garden",
        titleRes = R.string.game_emotion_garden,
        steps = listOf(
            GameTutorialStep("😊", R.string.tutorial_emotion_step1_title, R.string.tutorial_emotion_step1_desc),
            GameTutorialStep("🌸", R.string.tutorial_emotion_step2_title, R.string.tutorial_emotion_step2_desc),
            GameTutorialStep("🌱", R.string.tutorial_emotion_step3_title, R.string.tutorial_emotion_step3_desc),
            GameTutorialStep("🌈", R.string.tutorial_emotion_step4_title, R.string.tutorial_emotion_step4_desc)
        )
    ),
    "safe_space" to GameTutorial(
        gameId = "safe_space",
        titleRes = R.string.game_safe_space,
        steps = listOf(
            GameTutorialStep("🏠", R.string.tutorial_safe_step1_title, R.string.tutorial_safe_step1_desc),
            GameTutorialStep("🛋️", R.string.tutorial_safe_step2_title, R.string.tutorial_safe_step2_desc),
            GameTutorialStep("👆", R.string.tutorial_safe_step3_title, R.string.tutorial_safe_step3_desc),
            GameTutorialStep("✨", R.string.tutorial_safe_step4_title, R.string.tutorial_safe_step4_desc)
        )
    ),
    "worry_jar" to GameTutorial(
        gameId = "worry_jar",
        titleRes = R.string.game_worry_jar,
        steps = listOf(
            GameTutorialStep("📝", R.string.tutorial_worry_step1_title, R.string.tutorial_worry_step1_desc),
            GameTutorialStep("🫙", R.string.tutorial_worry_step2_title, R.string.tutorial_worry_step2_desc),
            GameTutorialStep("💨", R.string.tutorial_worry_step3_title, R.string.tutorial_worry_step3_desc),
            GameTutorialStep("💚", R.string.tutorial_worry_step4_title, R.string.tutorial_worry_step4_desc)
        )
    ),
    "constellation_connect" to GameTutorial(
        gameId = "constellation_connect",
        titleRes = R.string.game_constellation_connect,
        steps = listOf(
            GameTutorialStep("⭐", R.string.tutorial_constellation_step1_title, R.string.tutorial_constellation_step1_desc),
            GameTutorialStep("✨", R.string.tutorial_constellation_step2_title, R.string.tutorial_constellation_step2_desc),
            GameTutorialStep("🌌", R.string.tutorial_constellation_step3_title, R.string.tutorial_constellation_step3_desc),
            GameTutorialStep("🔄", R.string.tutorial_constellation_step4_title, R.string.tutorial_constellation_step4_desc)
        )
    ),
    "zen_sand" to GameTutorial(
        gameId = "zen_sand",
        titleRes = R.string.game_zen_sand,
        steps = listOf(
            GameTutorialStep("🏝️", R.string.tutorial_zen_step1_title, R.string.tutorial_zen_step1_desc),
            GameTutorialStep("👆", R.string.tutorial_zen_step2_title, R.string.tutorial_zen_step2_desc),
            GameTutorialStep("☯️", R.string.tutorial_zen_step3_title, R.string.tutorial_zen_step3_desc),
            GameTutorialStep("🔄", R.string.tutorial_zen_step4_title, R.string.tutorial_zen_step4_desc)
        )
    ),
    "mood_mixer" to GameTutorial(
        gameId = "mood_mixer",
        titleRes = R.string.game_mood_mixer,
        steps = listOf(
            GameTutorialStep("🎨", R.string.tutorial_mood_step1_title, R.string.tutorial_mood_step1_desc),
            GameTutorialStep("🎚️", R.string.tutorial_mood_step2_title, R.string.tutorial_mood_step2_desc),
            GameTutorialStep("💭", R.string.tutorial_mood_step3_title, R.string.tutorial_mood_step3_desc),
            GameTutorialStep("🌈", R.string.tutorial_mood_step4_title, R.string.tutorial_mood_step4_desc)
        )
    )
)

/**
 * Game Tutorial Dialog Composable
 */
@Composable
fun GameTutorialDialog(
    tutorial: GameTutorial,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val step = tutorial.steps[currentStep]
    val isLastStep = currentStep == tutorial.steps.lastIndex

    val infiniteTransition = rememberInfiniteTransition(label = "tutorial")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with game title
                Text(
                    text = stringResource(R.string.game_tutorial_how_to_play),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(tutorial.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                // Animated emoji
                Text(
                    text = step.emoji,
                    fontSize = 64.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = emojiScale
                        scaleY = emojiScale
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Step title
                Text(
                    text = stringResource(step.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Step description
                Text(
                    text = stringResource(step.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // Step indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tutorial.steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Skip/Back button
                    TextButton(
                        onClick = {
                            if (currentStep > 0) {
                                currentStep--
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Text(if (currentStep > 0) stringResource(R.string.game_tutorial_back) else stringResource(R.string.game_tutorial_skip))
                    }

                    // Next/Got it button
                    Button(
                        onClick = {
                            if (isLastStep) {
                                onDismiss()
                            } else {
                                currentStep++
                            }
                        }
                    ) {
                        Text(if (isLastStep) stringResource(R.string.game_tutorial_got_it) else stringResource(R.string.game_tutorial_next))
                        if (!isLastStep) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wrapper composable that shows tutorial on first launch of a game
 */
@Composable
fun GameWithTutorial(
    gameId: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showTutorial by remember {
        mutableStateOf(!GameTutorialManager.hasSeenTutorial(context, gameId))
    }

    val tutorial = GameTutorialManager.getTutorial(gameId)

    // Show the game content
    content()

    // Show tutorial dialog if needed
    if (showTutorial && tutorial != null) {
        GameTutorialDialog(
            tutorial = tutorial,
            onDismiss = {
                GameTutorialManager.markTutorialSeen(context, gameId)
                showTutorial = false
            }
        )
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
        icon = Icons.Outlined.Refresh,
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
    var showResetTutorialsDialog by remember { mutableStateOf(false) }

    // Reset tutorials confirmation dialog
    if (showResetTutorialsDialog) {
        AlertDialog(
            onDismissRequest = { showResetTutorialsDialog = false },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.games_reset_tutorials_title)) },
            text = { Text(stringResource(R.string.games_reset_tutorials_desc)) },
            confirmButton = {
                Button(onClick = {
                    GameTutorialManager.resetAllTutorials(context)
                    showResetTutorialsDialog = false
                }) {
                    Text(stringResource(R.string.games_reset_tutorials_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetTutorialsDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showResetTutorialsDialog = true }) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.game_cd_reset_tutorials),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                        stringResource(R.string.games_in_dev_subtitle),
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
                contentDescription = stringResource(R.string.game_coming_soon),
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
    if (poppedCount >= 8) {
        LaunchedEffect(Unit) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
    var totalSpinsInt by remember { mutableIntStateOf(0) }
    var lastAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var spinnerCenter by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // Performance-aware frame delay
    val frameDelay = remember { PerformanceOptimizations.getGameLoopDelayMs() }

    // Physics update - using withFrameNanos for VSync-aligned updates
    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val frameTimeMillis = frameTimeNanos / 1_000_000L
                // Calculate delta time for frame-rate independent physics
                val deltaTime = if (lastFrameTime > 0) {
                    ((frameTimeMillis - lastFrameTime) / 16.67f).coerceIn(0.5f, 3f)
                } else 1f
                lastFrameTime = frameTimeMillis

                if (!isDragging && abs(velocity) > 0.05f) {
                    rotation += velocity * deltaTime
                    totalSpins += abs(velocity * deltaTime) / 360f
                    if (totalSpins.toInt() > totalSpinsInt) {
                        totalSpinsInt = totalSpins.toInt()
                    }
                    // Frame-rate independent friction
                    val frictionPerFrame = 0.992f.pow(deltaTime)
                    velocity *= frictionPerFrame

                    // Haptic feedback at certain speeds for tactile response
                    if (abs(velocity) > 5f && totalSpinsInt > 0 && totalSpinsInt % 30 == 0) {
                        // Ensure we only trigger it once per 30 spins
                        if ((totalSpins - totalSpinsInt) < (abs(velocity) * deltaTime / 360f)) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                } else if (!isDragging) {
                    velocity = 0f
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_fidget_spinner)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        "🌀 $totalSpinsInt spins",
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
                    .onGloballyPositioned { coordinates ->
                        val size = coordinates.size
                        val position = coordinates.positionInParent()
                        spinnerCenter = androidx.compose.ui.geometry.Offset(
                            position.x + size.width / 2f,
                            position.y + size.height / 2f
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                // Calculate initial angle from center
                                val centerOffset = offset - androidx.compose.ui.geometry.Offset(
                                    size.width / 2f,
                                    size.height / 2f
                                )
                                lastAngle = kotlin.math.atan2(
                                    centerOffset.y.toDouble(),
                                    centerOffset.x.toDouble()
                                ).toFloat() * (180f / Math.PI.toFloat())
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                // Calculate current angle from center
                                val centerOffset = change.position - androidx.compose.ui.geometry.Offset(
                                    size.width / 2f,
                                    size.height / 2f
                                )
                                val currentAngle = kotlin.math.atan2(
                                    centerOffset.y.toDouble(),
                                    centerOffset.x.toDouble()
                                ).toFloat() * (180f / Math.PI.toFloat())

                                // Calculate angle difference
                                var angleDelta = currentAngle - lastAngle

                                // Handle wrap-around at 180/-180 degrees
                                if (angleDelta > 180f) angleDelta -= 360f
                                if (angleDelta < -180f) angleDelta += 360f

                                // Apply rotation directly while dragging
                                rotation += angleDelta

                                // Build up velocity based on drag speed (for momentum when released)
                                velocity = velocity * 0.7f + angleDelta * 0.5f

                                lastAngle = currentAngle
                            },
                            onDragEnd = {
                                isDragging = false
                                // Give a bit of boost to the velocity on release
                                velocity *= 1.2f
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragCancel = {
                                isDragging = false
                            }
                        )
                    }
                    .graphicsLayer { rotationZ = rotation },
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = currentColors,
                            startY = animatedProgress * 1000f
                        )
                    )
                }
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
    // Wrap each game with tutorial support
    GameWithTutorial(gameId = gameId) {
        when (gameId) {
            "bubble_pop" -> BubblePopGame(onBack)
            "fidget_spinner" -> FidgetSpinnerGame(onBack)
            "color_flow" -> ColorFlowGame(onBack)
            "pattern_tap" -> PatternTapGame(onBack)
            "infinity_draw" -> InfinityDrawGame(onBack)
            "sensory_rain" -> SensoryRainGame(onBack)
            "breathing_bubbles" -> BreathingBubblesGame(onBack)
            "texture_tiles" -> TextureTilesGame(onBack)
            "sound_garden" -> SoundGardenGame(onBack)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) stringResource(R.string.game_cd_pause) else stringResource(R.string.game_cd_play)
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
                Text(stringResource(R.string.game_stim_bpm_label, bpm.toInt()), color = Color.White, fontWeight = FontWeight.Bold)
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
                Text(stringResource(R.string.games_clear_all))
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    var flowers by remember { mutableStateOf(listOf<EmotionFlower>()) }
    var selectedEmotion by remember { mutableStateOf<Triple<String, String, Int>?>(null) }
    var nextId by remember { mutableIntStateOf(0) }

    // key, emoji, nameRes
    val emotions: List<Triple<String, String, Int>> = listOf(
        Triple("happy", "😊", R.string.game_emotion_happy),
        Triple("calm", "😌", R.string.game_emotion_calm),
        Triple("excited", "🤩", R.string.game_emotion_excited),
        Triple("loved", "🥰", R.string.game_emotion_loved),
        Triple("grateful", "🙏", R.string.game_emotion_grateful),
        Triple("peaceful", "☮️", R.string.game_emotion_peaceful),
        Triple("hopeful", "🌈", R.string.game_emotion_hopeful),
        Triple("proud", "💪", R.string.game_emotion_proud),
        Triple("curious", "🤔", R.string.game_emotion_curious)
    )

    val emotionColors = mapOf(
        "happy" to Color(0xFFFFD700),
        "calm" to Color(0xFF87CEEB),
        "excited" to Color(0xFFFF6B6B),
        "loved" to Color(0xFFFF69B4),
        "grateful" to Color(0xFF90EE90),
        "peaceful" to Color(0xFFE6E6FA),
        "hopeful" to Color(0xFFFFB347),
        "proud" to Color(0xFFDDA0DD),
        "curious" to Color(0xFF98D8C8)
    )

    // Grow flowers animation with proper cancellation
    LaunchedEffect(flowers) {
        while (isActive) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { flowers = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_clear))
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
                emotions.forEach { (key, emoji, nameRes) ->
                    val name = stringResource(nameRes)
                    FilterChip(
                        selected = selectedEmotion?.first == key,
                        onClick = {
                            selectedEmotion = Triple(key, emoji, nameRes)
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
                            selectedEmotion?.let { (key, emoji, _) ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                flowers = flowers + EmotionFlower(
                                    id = nextId++,
                                    emotion = key,
                                    emoji = emoji,
                                    color = emotionColors[key] ?: Color.White,
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
                                x = with(density) { (flower.x - 24).toDp() },
                                y = with(density) { (flower.y - 24).toDp() }
                            )
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
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
                stringResource(R.string.game_emotion_flower_count, flowers.size),
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    var roomItems by remember { mutableStateOf(listOf<RoomItem>()) }
    var nextId by remember { mutableIntStateOf(0) }
    var selectedItem by remember { mutableStateOf<String?>(null) }

    val availableItems: List<Pair<String, Int>> = listOf(
        "🛋️" to R.string.game_safe_cozy_couch,
        "🕯️" to R.string.game_safe_candle,
        "🪴" to R.string.game_safe_plant,
        "📚" to R.string.game_safe_books,
        "🧸" to R.string.game_safe_plushie,
        "🎵" to R.string.game_safe_music,
        "☕" to R.string.game_safe_hot_drink,
        "🌙" to R.string.game_safe_moon_light,
        "🔮" to R.string.game_safe_crystal,
        "🧘" to R.string.game_safe_meditation,
        "🎨" to R.string.game_safe_art,
        "🌸" to R.string.game_safe_flowers
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_safe_space)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { roomItems = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_clear))
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
                availableItems.forEach { (emoji, nameRes) ->
                    val name = stringResource(nameRes)
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
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                roomItems = roomItems + RoomItem(
                                    id = nextId++,
                                    emoji = emoji,
                                    name = emoji,
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
                                x = with(density) { (item.x - 24).toDp() },
                                y = with(density) { (item.y - 24).toDp() }
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
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(50)
            if (worries.isNotEmpty()) {
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_worry_jar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.game_worry_released_count, releasedCount),
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
                                .graphicsLayer {
                                    translationY = worry.yOffset.dp.toPx()
                                    alpha = worry.alpha
                                },
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
                                stringResource(R.string.game_worry_empty_jar),
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
                        placeholder = { Text(stringResource(R.string.games_worry_placeholder)) },
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
                        Text(stringResource(R.string.games_release_worry))
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    var stars by remember { mutableStateOf<List<Star>>(emptyList()) }
    var connections by remember { mutableStateOf(listOf<StarConnection>()) }
    var selectedStar by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Generate stars when canvas size is known
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0 && stars.isEmpty()) {
            stars = generateStarsForSize(15, canvasSize.width, canvasSize.height)
        }
    }

    fun regenerateStars() {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            stars = generateStarsForSize(15, canvasSize.width, canvasSize.height)
            connections = emptyList()
            selectedStar = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_constellation_connect)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.game_constellation_lines_count, connections.size),
                        modifier = Modifier.padding(end = 16.dp),
                        color = Color.White
                    )
                    IconButton(onClick = { regenerateStars() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_new_stars), tint = Color.White)
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
                .onGloballyPositioned { coordinates ->
                    canvasSize = androidx.compose.ui.geometry.Size(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                }
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
                            x = with(density) { star.x.toDp() } - 16.dp,
                            y = with(density) { star.y.toDp() } - 16.dp
                        )
                        .graphicsLayer {
                            scaleX = starScale
                            scaleY = starScale
                        }
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

private fun generateStarsForSize(count: Int, width: Float, height: Float): List<Star> {
    val padding = 50f
    return List(count) { index ->
        Star(
            id = index,
            x = Random.nextFloat() * (width - padding * 2) + padding,
            y = Random.nextFloat() * (height - padding * 2) + padding
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
    val isDrawing by remember { derivedStateOf { currentLine.isNotEmpty() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_zen_sand)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { lines = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_clear))
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
            if (lines.isEmpty() && !isDrawing) {
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
// PATTERN TAP GAME - Memorize and repeat patterns
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternTapGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var pattern by remember { mutableStateOf(listOf<Int>()) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var isShowingPattern by remember { mutableStateOf(false) }
    var currentShowIndex by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var gameState by remember { mutableStateOf("waiting") } // waiting, showing, input, success, fail

    val gridColors = listOf(
        Color(0xFF8E2DE2), Color(0xFF4A00E0), Color(0xFFFF6B6B), Color(0xFF4ECDC4),
        Color(0xFFFFE66D), Color(0xFF95E1D3), Color(0xFFF38181), Color(0xFFAA96DA),
        Color(0xFF667eea)
    )

    fun startNewRound() {
        val newPattern = if (pattern.isEmpty()) {
            listOf(Random.nextInt(9))
        } else {
            pattern + Random.nextInt(9)
        }
        pattern = newPattern
        playerInput = emptyList()
        gameState = "showing"
        isShowingPattern = true

        scope.launch {
            delay(500)
            for (i in newPattern.indices) {
                currentShowIndex = newPattern[i]
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(600)
                currentShowIndex = -1
                delay(300)
            }
            isShowingPattern = false
            gameState = "input"
        }
    }

    fun onTileTap(index: Int) {
        if (gameState != "input" || playerInput.size >= pattern.size) return

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val newInput = playerInput + index

        if (newInput.lastIndex < pattern.size && newInput[newInput.lastIndex] != pattern[newInput.lastIndex]) {
            gameState = "fail"
            return
        }

        playerInput = newInput

        if (newInput.size == pattern.size) {
            score++
            gameState = "success"
            scope.launch {
                delay(1000)
                startNewRound()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_pattern_tap)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.game_pattern_level_count, score),
                        style = MaterialTheme.typography.titleMedium,
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
                        listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status text
            Text(
                when (gameState) {
                    "waiting" -> stringResource(R.string.game_pattern_state_waiting)
                    "showing" -> stringResource(R.string.game_pattern_state_showing)
                    "input" -> stringResource(R.string.game_pattern_state_input, playerInput.size, pattern.size)
                    "success" -> stringResource(R.string.game_pattern_state_success)
                    "fail" -> stringResource(R.string.game_pattern_state_fail)
                    else -> ""
                },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            // 3x3 Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.size(280.dp)
            ) {
                items(9) { index ->
                    val isHighlighted = currentShowIndex == index
                    val tileScale by animateFloatAsState(
                        targetValue = if (isHighlighted) 1.1f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "tileScale"
                    )

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .graphicsLayer {
                                scaleX = tileScale
                                scaleY = tileScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isHighlighted) gridColors[index]
                                else gridColors[index].copy(alpha = 0.4f)
                            )
                            .border(
                                width = if (isHighlighted) 4.dp else 2.dp,
                                color = Color.White.copy(alpha = if (isHighlighted) 1f else 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = gameState == "input") { onTileTap(index) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Start/Restart button
            if (gameState == "waiting" || gameState == "fail") {
                Button(
                    onClick = {
                        pattern = emptyList()
                        score = 0
                        startNewRound()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        if (gameState == "fail") stringResource(R.string.game_pattern_try_again_button) else stringResource(R.string.game_pattern_start_button),
                        color = Color(0xFF8E2DE2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.game_pattern_tap_hint),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================================
// INFINITY DRAW GAME - Calming infinite drawing
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfinityDrawGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var lines by remember { mutableStateOf(listOf<List<Pair<Float, Float>>>()) }
    var currentLine by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }
    val isDrawing by remember { derivedStateOf { currentLine.isNotEmpty() } }
    var currentColor by remember { mutableStateOf(Color(0xFFFF416C)) }

    val colors = listOf(
        Color(0xFFFF416C), Color(0xFFFF4B2B), Color(0xFF667eea),
        Color(0xFF764ba2), Color(0xFF11998e), Color(0xFF38ef7d),
        Color(0xFFFFE66D), Color(0xFFF38181)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_infinity_draw)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { lines = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_clear))
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
            // Color palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (currentColor == color) 3.dp else 1.dp,
                                color = if (currentColor == color) Color.White else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                currentColor = color
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    )
                }
            }

            // Drawing canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1a1a2e), Color(0xFF16213e))
                        )
                    )
                    .pointerInput(currentColor) {
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
                                    lines = lines + listOf(currentLine)
                                }
                                currentLine = emptyList()
                            }
                        )
                    }
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Draw all completed lines
                    lines.forEachIndexed { lineIndex, points ->
                        if (points.size >= 2) {
                            val lineColor = colors[lineIndex % colors.size]
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = lineColor,
                                    start = androidx.compose.ui.geometry.Offset(points[i].first, points[i].second),
                                    end = androidx.compose.ui.geometry.Offset(points[i + 1].first, points[i + 1].second),
                                    strokeWidth = 6f
                                )
                            }
                        }
                    }

                    // Draw current line
                    if (currentLine.size >= 2) {
                        for (i in 0 until currentLine.size - 1) {
                            drawLine(
                                color = currentColor,
                                start = androidx.compose.ui.geometry.Offset(currentLine[i].first, currentLine[i].second),
                                end = androidx.compose.ui.geometry.Offset(currentLine[i + 1].first, currentLine[i + 1].second),
                                strokeWidth = 6f
                            )
                        }
                    }
                }

                // Hint
                if (lines.isEmpty() && !isDrawing) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_infinity_draw_hint),
                            color = Color.White.copy(alpha = 0.7f),
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
// SENSORY RAIN GAME - Relaxing rain visualization
// =============================================================================

data class RainDrop(
    val id: Int,
    val x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensoryRainGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var rainDrops by remember { mutableStateOf<List<RainDrop>>(emptyList()) }
    var intensity by remember { mutableFloatStateOf(0.5f) }
    var ripples by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    // Generate rain drops when canvas size is known
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0 && rainDrops.isEmpty()) {
            rainDrops = generateRainDropsForSize(100, canvasSize.width, canvasSize.height)
        }
    }

    // Animate rain with frame-aware timing
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            while (isActive) {
                withFrameNanos { _ ->
                    rainDrops = rainDrops.map { drop ->
                        val newY = drop.y + drop.speed * intensity * 2
                        if (newY > canvasSize.height) {
                            drop.copy(y = -20f, x = Random.nextFloat() * canvasSize.width)
                        } else {
                            drop.copy(y = newY)
                        }
                    }
                }
            }
        }
    }

    // Fade ripples
    LaunchedEffect(ripples) {
        if (ripples.isNotEmpty()) {
            delay(500)
            ripples = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_sensory_rain)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            // Intensity slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1a1a2e))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌧️", fontSize = 20.sp)
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                Text("⛈️", fontSize = 20.sp)
            }

            // Rain canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0c0c1e), Color(0xFF1a1a3e), Color(0xFF2a2a5e))
                        )
                    )
                    .onGloballyPositioned { coordinates ->
                        canvasSize = androidx.compose.ui.geometry.Size(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ripples = ripples + (offset.x to offset.y)
                        }
                    }
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Draw rain drops
                    rainDrops.forEach { drop ->
                        drawLine(
                            color = Color(0xFF87CEEB).copy(alpha = drop.alpha * intensity),
                            start = androidx.compose.ui.geometry.Offset(drop.x, drop.y),
                            end = androidx.compose.ui.geometry.Offset(drop.x, drop.y + drop.size),
                            strokeWidth = 2f
                        )
                    }

                    // Draw ripples
                    ripples.forEach { (x, y) ->
                        drawCircle(
                            color = Color(0xFF87CEEB).copy(alpha = 0.3f),
                            radius = 40f,
                            center = androidx.compose.ui.geometry.Offset(x, y),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }

                // Hint
                Text(
                    stringResource(R.string.game_sensory_rain_hint),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun generateRainDropsForSize(count: Int, width: Float, height: Float): List<RainDrop> {
    return List(count) { id ->
        RainDrop(
            id = id,
            x = Random.nextFloat() * width,
            y = Random.nextFloat() * height,
            speed = Random.nextFloat() * 5 + 3,
            size = Random.nextFloat() * 20 + 10,
            alpha = Random.nextFloat() * 0.5f + 0.3f
        )
    }
}

// =============================================================================
// BREATHING BUBBLES GAME - Guided breathing exercise
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingBubblesGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var isBreathing by remember { mutableStateOf(false) }
    var breathPhase by remember { mutableStateOf("inhale") } // inhale, hold, exhale
    var cycleCount by remember { mutableIntStateOf(0) }

    // Animated scale based on breath phase
    val targetScale = when {
        !isBreathing -> 0.8f
        breathPhase == "inhale" -> 1.2f
        breathPhase == "hold" -> 1.2f
        else -> 0.5f  // exhale
    }

    val bubbleScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = when {
                !isBreathing -> 300
                breathPhase == "hold" -> 100
                else -> 4000
            },
            easing = EaseInOutSine
        ),
        label = "bubbleScale"
    )

    // Breathing cycle
    LaunchedEffect(isBreathing) {
        if (isBreathing) {
            while (isBreathing) {
                // Inhale
                breathPhase = "inhale"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(4000)

                // Hold
                breathPhase = "hold"
                delay(2000)

                // Exhale
                breathPhase = "exhale"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(4000)

                cycleCount++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_breathing_bubbles)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.game_breathing_count, cycleCount),
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
                        listOf(Color(0xFF56CCF2), Color(0xFF2F80ED), Color(0xFF1a1a4e))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Breathing bubble
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = bubbleScale
                            scaleY = bubbleScale
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.8f),
                                    Color(0xFF87CEEB).copy(alpha = 0.6f),
                                    Color(0xFF56CCF2).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when {
                            !isBreathing -> "🫧"
                            breathPhase == "inhale" -> stringResource(R.string.game_breathing_inhale)
                            breathPhase == "hold" -> stringResource(R.string.game_breathing_hold)
                            else -> stringResource(R.string.game_breathing_exhale)
                        },
                        color = if (isBreathing) Color(0xFF1a1a4e) else Color.Transparent,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isBreathing) 18.sp else 64.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(48.dp))

                // Start/Stop button
                Button(
                    onClick = {
                        isBreathing = !isBreathing
                        if (!isBreathing) {
                            breathPhase = "inhale"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        if (isBreathing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF2F80ED)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isBreathing) stringResource(R.string.game_breathing_pause) else stringResource(R.string.game_breathing_start),
                        color = Color(0xFF2F80ED),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    stringResource(R.string.game_breathing_bubbles_hint),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

// =============================================================================
// TEXTURE TILES GAME - Satisfying haptic textures
// =============================================================================

data class TextureTile(
    val id: Int,
    val emoji: String,
    @StringRes val nameRes: Int,
    val color: Color,
    val hapticPattern: Int // 1-5 intensity
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextureTilesGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var selectedTile by remember { mutableStateOf<TextureTile?>(null) }
    var tapCount by remember { mutableIntStateOf(0) }

    val tiles = listOf(
        TextureTile(0, "🪨", R.string.game_texture_stone, Color(0xFF6B7280), 3),
        TextureTile(1, "🧸", R.string.game_texture_fluffy, Color(0xFFFBBF24), 1),
        TextureTile(2, "🧊", R.string.game_texture_ice, Color(0xFF60A5FA), 4),
        TextureTile(3, "🌿", R.string.game_texture_leaf, Color(0xFF34D399), 2),
        TextureTile(4, "⚡", R.string.game_texture_electric, Color(0xFFF472B6), 5),
        TextureTile(5, "🌊", R.string.game_texture_wave, Color(0xFF3B82F6), 2),
        TextureTile(6, "🔥", R.string.game_texture_warm, Color(0xFFEF4444), 3),
        TextureTile(7, "❄️", R.string.game_texture_cold, Color(0xFF93C5FD), 4),
        TextureTile(8, "🍃", R.string.game_texture_breeze, Color(0xFF86EFAC), 1)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_texture_tiles)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    Text(
                        stringResource(R.string.game_texture_taps_count, tapCount),
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
                        listOf(Color(0xFFf953c6), Color(0xFFb91d73))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selected tile display
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(selectedTile?.color ?: Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        selectedTile?.emoji ?: "?",
                        fontSize = 48.sp
                    )
                    selectedTile?.let { tile ->
                        Text(
                            stringResource(tile.nameRes),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Texture grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(tiles.size) { index ->
                    val tile = tiles[index]
                    val isSelected = selectedTile?.id == tile.id
                    val tileScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "tileScale"
                    )

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .graphicsLayer {
                                scaleX = tileScale
                                scaleY = tileScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(tile.color.copy(alpha = if (isSelected) 1f else 0.7f))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedTile = tile
                                tapCount++
                                // Perform haptic based on tile intensity with delays
                                scope.launch {
                                    repeat(tile.hapticPattern) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (it < tile.hapticPattern - 1) {
                                            delay(50)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tile.emoji, fontSize = 32.sp)
                            Text(
                                stringResource(tile.nameRes),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.game_texture_tiles_hint),
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// =============================================================================
// SOUND GARDEN GAME - Create musical patterns
// =============================================================================

data class GardenNote(
    val id: Int,
    val emoji: String,
    val name: String,
    val color: Color,
    var x: Float,
    var y: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundGardenGame(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    var notes by remember { mutableStateOf(listOf<GardenNote>()) }
    var selectedNote by remember { mutableStateOf<Pair<String, String>?>(null) }
    var nextId by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playIndex by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    val noteTypes: List<Pair<String, Int>> = listOf(
        "🎵" to R.string.game_sound_note,
        "🎶" to R.string.game_sound_melody,
        "🔔" to R.string.game_sound_bell,
        "🎹" to R.string.game_sound_piano,
        "🥁" to R.string.game_sound_drum,
        "🎺" to R.string.game_sound_horn,
        "🎸" to R.string.game_sound_guitar,
        "🪇" to R.string.game_sound_chime
    )

    val noteColors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
        Color(0xFF95E1D3), Color(0xFFF38181), Color(0xFFAA96DA),
        Color(0xFF667eea), Color(0xFF43cea2)
    )

    fun playGarden() {
        if (notes.isEmpty()) return
        isPlaying = true
        scope.launch {
            notes.sortedBy { it.x }.forEachIndexed { index, note ->
                playIndex = note.id
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(300)
            }
            playIndex = -1
            isPlaying = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_sound_garden)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { playGarden() }, enabled = !isPlaying && notes.isNotEmpty()) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.game_cd_play))
                    }
                    IconButton(onClick = { notes = emptyList() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.game_cd_clear))
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
            // Note palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                noteTypes.forEach { (emoji, nameRes) ->
                    val name = stringResource(nameRes)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedNote?.first == emoji)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                selectedNote = emoji to name
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(12.dp)
                    ) {
                        Text(emoji, fontSize = 28.sp)
                        Text(name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Garden area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF43cea2), Color(0xFF185a9d))
                        )
                    )
                    .pointerInput(selectedNote) {
                        detectTapGestures { offset ->
                            selectedNote?.let { (emoji, name) ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                notes = notes + GardenNote(
                                    id = nextId++,
                                    emoji = emoji,
                                    name = name,
                                    color = noteColors[notes.size % noteColors.size],
                                    x = offset.x,
                                    y = offset.y
                                )
                            }
                        }
                    }
            ) {
                // Draw notes
                notes.forEach { note ->
                    val isCurrentlyPlaying = playIndex == note.id
                    val noteScale by animateFloatAsState(
                        targetValue = if (isCurrentlyPlaying) 1.5f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "noteScale"
                    )

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(density) { (note.x - 24).toDp() },
                                y = with(density) { (note.y - 24).toDp() }
                            )
                            .graphicsLayer {
                                scaleX = noteScale
                                scaleY = noteScale
                            }
                    ) {
                        Text(
                            text = note.emoji,
                            fontSize = 48.sp
                        )
                    }
                }

                // Hint
                if (notes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎼", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.game_sound_garden_hint),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Note count
            Text(
                stringResource(R.string.game_sound_notes_count, notes.size),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
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

    val moodColors: List<Triple<Color, Int, String>> = listOf(
        Triple(Color(0xFF4ECDC4), R.string.game_mood_calm, "calm"),
        Triple(Color(0xFFFFE66D), R.string.game_mood_happy, "happy"),
        Triple(Color(0xFF95E1D3), R.string.game_mood_peaceful, "peaceful"),
        Triple(Color(0xFFAA96DA), R.string.game_mood_dreamy, "dreamy"),
        Triple(Color(0xFFF38181), R.string.game_mood_passionate, "passionate"),
        Triple(Color(0xFF6B5B95), R.string.game_mood_thoughtful, "thoughtful"),
        Triple(Color(0xFF88D8B0), R.string.game_mood_refreshed, "refreshed")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                    stringResource(R.string.game_mood_your_mood),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // Mix slider
            Text(
                stringResource(R.string.game_mood_blend),
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
            Text(stringResource(R.string.game_mood_first), color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                moodColors.forEach { (color, nameRes, _) ->
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
                        Text(stringResource(nameRes), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Color 2 selector
            Text(stringResource(R.string.game_mood_second), color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                moodColors.forEach { (color, nameRes, _) ->
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
                        Text(stringResource(nameRes), color = Color.White, style = MaterialTheme.typography.labelSmall)
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



@file:OptIn(ExperimentalMaterial3Api::class)

package com.kyilmaz.neurocomet

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Badge categories - organized by type of achievement
 */
enum class BadgeCategory {
    SOCIAL,      // Connecting with others
    CREATIVE,    // Creating content
    WELLNESS,    // Self-care and mindfulness
    EXPLORER,    // Discovering features
    KINDNESS,    // Being supportive
    MILESTONE    // Usage milestones
}

/**
 * Badge rarity levels - affects visual styling
 */
enum class BadgeRarity {
    COMMON,      // Easy to get, subtle styling
    UNCOMMON,    // Moderate effort, nice styling
    RARE,        // Takes time, special styling
    EPIC,        // Significant achievement, fancy styling
    LEGENDARY    // Major milestone, premium styling
}

/**
 * Represents an achievement badge in the app (renamed to avoid conflict with DataModels.AchievementBadge)
 */
data class AchievementBadge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String, // Emoji icon
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val requirement: String, // How to earn it
    val isSecret: Boolean = false, // Hidden until earned
    val maxProgress: Int = 1, // For progressive badges
    val xpReward: Int = 10 // XP earned when unlocked
)

/**
 * User's progress on a badge
 */
data class AchievementProgress(
    val badgeId: String,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

/**
 * All available badges in the app
 */
object AchievementRegistry {

    val allBadges = listOf(
        // === SOCIAL BADGES ===
        AchievementBadge(
            id = "first_friend",
            name = "First Connection",
            description = "Made your first friend on NeuroComet",
            icon = "🤝",
            category = BadgeCategory.SOCIAL,
            rarity = BadgeRarity.COMMON,
            requirement = "Add your first friend"
        ),
        AchievementBadge(
            id = "conversation_starter",
            name = "Conversation Starter",
            description = "Started 5 conversations",
            icon = "💬",
            category = BadgeCategory.SOCIAL,
            rarity = BadgeRarity.COMMON,
            requirement = "Start 5 conversations",
            maxProgress = 5
        ),
        AchievementBadge(
            id = "good_listener",
            name = "Good Listener",
            description = "Read 50 messages from friends",
            icon = "👂",
            category = BadgeCategory.SOCIAL,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Read 50 messages",
            maxProgress = 50
        ),
        AchievementBadge(
            id = "social_butterfly",
            name = "Social Butterfly",
            description = "Connected with 10 people",
            icon = "🦋",
            category = BadgeCategory.SOCIAL,
            rarity = BadgeRarity.RARE,
            requirement = "Have 10 friends",
            maxProgress = 10
        ),

        // === CREATIVE BADGES ===
        AchievementBadge(
            id = "first_post",
            name = "Voice Heard",
            description = "Shared your first post",
            icon = "📝",
            category = BadgeCategory.CREATIVE,
            rarity = BadgeRarity.COMMON,
            requirement = "Create your first post"
        ),
        AchievementBadge(
            id = "storyteller",
            name = "Storyteller",
            description = "Shared 10 posts",
            icon = "📖",
            category = BadgeCategory.CREATIVE,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Share 10 posts",
            maxProgress = 10
        ),
        AchievementBadge(
            id = "emoji_artist",
            name = "Emoji Artist",
            description = "Used 20 different emojis",
            icon = "🎨",
            category = BadgeCategory.CREATIVE,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Use 20 unique emojis",
            maxProgress = 20
        ),
        AchievementBadge(
            id = "photographer",
            name = "Photographer",
            description = "Shared 5 photos",
            icon = "📸",
            category = BadgeCategory.CREATIVE,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Share 5 photos",
            maxProgress = 5
        ),

        // === WELLNESS BADGES ===
        AchievementBadge(
            id = "calm_mode",
            name = "Finding Peace",
            description = "Used calm mode for the first time",
            icon = "🧘",
            category = BadgeCategory.WELLNESS,
            rarity = BadgeRarity.COMMON,
            requirement = "Enable calm mode"
        ),
        AchievementBadge(
            id = "break_taker",
            name = "Break Taker",
            description = "Took a mindful break",
            icon = "☕",
            category = BadgeCategory.WELLNESS,
            rarity = BadgeRarity.COMMON,
            requirement = "Use the break reminder"
        ),
        AchievementBadge(
            id = "night_owl",
            name = "Night Owl",
            description = "Used the app after 10 PM",
            icon = "🦉",
            category = BadgeCategory.WELLNESS,
            rarity = BadgeRarity.COMMON,
            requirement = "Be active late at night",
            isSecret = true
        ),
        AchievementBadge(
            id = "early_bird",
            name = "Early Bird",
            description = "Used the app before 7 AM",
            icon = "🐦",
            category = BadgeCategory.WELLNESS,
            rarity = BadgeRarity.COMMON,
            requirement = "Be active early morning",
            isSecret = true
        ),
        AchievementBadge(
            id = "zen_master",
            name = "Zen Master",
            description = "Used calm mode 7 days in a row",
            icon = "🪷",
            category = BadgeCategory.WELLNESS,
            rarity = BadgeRarity.RARE,
            requirement = "Use calm mode daily for a week",
            maxProgress = 7
        ),

        // === EXPLORER BADGES ===
        AchievementBadge(
            id = "curious_mind",
            name = "Curious Mind",
            description = "Explored all main sections",
            icon = "🔍",
            category = BadgeCategory.EXPLORER,
            rarity = BadgeRarity.COMMON,
            requirement = "Visit all tabs"
        ),
        AchievementBadge(
            id = "theme_changer",
            name = "Style Explorer",
            description = "Tried 3 different themes",
            icon = "🎭",
            category = BadgeCategory.EXPLORER,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Change themes 3 times",
            maxProgress = 3
        ),
        AchievementBadge(
            id = "settings_guru",
            name = "Settings Guru",
            description = "Customized 5 settings",
            icon = "⚙️",
            category = BadgeCategory.EXPLORER,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Modify 5 settings",
            maxProgress = 5
        ),
        AchievementBadge(
            id = "easter_egg",
            name = "Secret Finder",
            description = "Found a hidden easter egg!",
            icon = "🥚",
            category = BadgeCategory.EXPLORER,
            rarity = BadgeRarity.EPIC,
            requirement = "Discover a secret",
            isSecret = true
        ),

        // === KINDNESS BADGES ===
        AchievementBadge(
            id = "first_like",
            name = "Appreciator",
            description = "Liked your first post",
            icon = "❤️",
            category = BadgeCategory.KINDNESS,
            rarity = BadgeRarity.COMMON,
            requirement = "Like a post"
        ),
        AchievementBadge(
            id = "supporter",
            name = "Supporter",
            description = "Liked 25 posts",
            icon = "💪",
            category = BadgeCategory.KINDNESS,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Like 25 posts",
            maxProgress = 25
        ),
        AchievementBadge(
            id = "cheerleader",
            name = "Cheerleader",
            description = "Left 10 encouraging comments",
            icon = "📣",
            category = BadgeCategory.KINDNESS,
            rarity = BadgeRarity.RARE,
            requirement = "Comment on 10 posts",
            maxProgress = 10
        ),
        AchievementBadge(
            id = "reaction_master",
            name = "Reaction Master",
            description = "Used all reaction types",
            icon = "🎯",
            category = BadgeCategory.KINDNESS,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Use every reaction emoji"
        ),

        // === MILESTONE BADGES ===
        AchievementBadge(
            id = "welcome",
            name = "Welcome!",
            description = "Joined the NeuroComet community",
            icon = "🎉",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.COMMON,
            requirement = "Create an account"
        ),
        AchievementBadge(
            id = "one_week",
            name = "Weekly Warrior",
            description = "Been here for a week!",
            icon = "📅",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.UNCOMMON,
            requirement = "Use app for 7 days",
            maxProgress = 7
        ),
        AchievementBadge(
            id = "one_month",
            name = "Monthly Member",
            description = "A month of being awesome!",
            icon = "🗓️",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.RARE,
            requirement = "Use app for 30 days",
            maxProgress = 30
        ),
        AchievementBadge(
            id = "century",
            name = "Century Club",
            description = "Sent 100 messages!",
            icon = "💯",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.RARE,
            requirement = "Send 100 messages",
            maxProgress = 100
        ),
        AchievementBadge(
            id = "neuro_champion",
            name = "NeuroComet Champion",
            description = "Earned 10 badges",
            icon = "🏆",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.EPIC,
            requirement = "Collect 10 badges",
            maxProgress = 10
        ),
        AchievementBadge(
            id = "legendary",
            name = "Legendary User",
            description = "Earned all common badges",
            icon = "⭐",
            category = BadgeCategory.MILESTONE,
            rarity = BadgeRarity.LEGENDARY,
            requirement = "Unlock all common badges"
        )
    )

    fun getBadgeById(id: String): AchievementBadge? = allBadges.find { it.id == id }

    fun getBadgesByCategory(category: BadgeCategory): List<AchievementBadge> =
        allBadges.filter { it.category == category }

    fun getBadgesByRarity(rarity: BadgeRarity): List<AchievementBadge> =
        allBadges.filter { it.rarity == rarity }
}

/**
 * Get colors for badge rarity
 */
@Composable
fun BadgeRarity.getColors(): Pair<Color, Color> {
    return when (this) {
        BadgeRarity.COMMON -> Color(0xFF9E9E9E) to Color(0xFFBDBDBD)
        BadgeRarity.UNCOMMON -> Color(0xFF4CAF50) to Color(0xFF81C784)
        BadgeRarity.RARE -> Color(0xFF2196F3) to Color(0xFF64B5F6)
        BadgeRarity.EPIC -> Color(0xFF9C27B0) to Color(0xFFBA68C8)
        BadgeRarity.LEGENDARY -> Color(0xFFFF9800) to Color(0xFFFFB74D)
    }
}

/**
 * Badge display card - shows a single badge
 */
@Composable
fun BadgeCard(
    badge: AchievementBadge,
    progress: AchievementProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = progress?.isUnlocked == true
    val (primaryColor, secondaryColor) = badge.rarity.getColors()

    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "badgeScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isUnlocked)
            MaterialTheme.colorScheme.surfaceContainerHigh
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (isUnlocked) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge icon with glow effect for unlocked
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .then(
                        if (isUnlocked) {
                            Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Border ring
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .border(
                            width = 2.dp,
                            color = if (isUnlocked) primaryColor else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .background(
                            color = if (isUnlocked)
                                primaryColor.copy(alpha = 0.1f)
                            else
                                Color.Gray.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUnlocked || !badge.isSecret) badge.icon else "❓",
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Badge name
            Text(
                text = if (isUnlocked || !badge.isSecret) badge.name else "???",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isUnlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Progress bar for progressive badges
            if (!isUnlocked && badge.maxProgress > 1 && progress != null) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.currentProgress.toFloat() / badge.maxProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = primaryColor,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
                Text(
                    text = "${progress.currentProgress}/${badge.maxProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Rarity indicator
            if (isUnlocked) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = primaryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Badge detail dialog - shows full badge info
 */
@Composable
fun BadgeDetailDialog(
    badge: AchievementBadge,
    progress: AchievementProgress?,
    onDismiss: () -> Unit
) {
    val isUnlocked = progress?.isUnlocked == true
    val (primaryColor, secondaryColor) = badge.rarity.getColors()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large badge icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isUnlocked) listOf(
                                    primaryColor.copy(alpha = 0.4f),
                                    primaryColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                ) else listOf(
                                    Color.Gray.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = if (isUnlocked) listOf(primaryColor, secondaryColor)
                                    else listOf(Color.Gray, Color.Gray.copy(alpha = 0.5f))
                                ),
                                shape = CircleShape
                            )
                            .background(
                                color = if (isUnlocked) primaryColor.copy(alpha = 0.15f)
                                else Color.Gray.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked || !badge.isSecret) badge.icon else "❓",
                            fontSize = 40.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Badge name
                Text(
                    text = if (isUnlocked || !badge.isSecret) badge.name else "Secret Badge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Rarity chip
                Surface(
                    color = primaryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = badge.rarity.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Description
                Text(
                    text = if (isUnlocked || !badge.isSecret) badge.description
                           else "Complete a secret action to unlock this badge!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Requirement
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isUnlocked) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                            contentDescription = null,
                            tint = if (isUnlocked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isUnlocked) "Unlocked!"
                                   else if (badge.isSecret) "Keep exploring..."
                                   else badge.requirement,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUnlocked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress for progressive badges
                if (!isUnlocked && badge.maxProgress > 1 && progress != null) {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.currentProgress.toFloat() / badge.maxProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = primaryColor,
                            trackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${progress.currentProgress} / ${badge.maxProgress}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // XP reward
                if (isUnlocked) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Text(
                            text = "+${badge.xpReward} XP earned",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryColor
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Close button
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/**
 * Horizontal badge showcase - shows recent/featured badges
 */
@Composable
fun BadgeShowcase(
    badges: List<AchievementBadge>,
    progressMap: Map<String, AchievementProgress>,
    onBadgeClick: (AchievementBadge) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Your Badges"
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            val unlockedCount = progressMap.count { it.value.isUnlocked }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$unlockedCount / ${badges.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(badges) { badge ->
                BadgeCard(
                    badge = badge,
                    progress = progressMap[badge.id],
                    onClick = { onBadgeClick(badge) }
                )
            }
        }
    }
}

/**
 * Full badges screen - grid view of all badges
 */
@Composable
fun BadgesScreen(
    progressMap: Map<String, AchievementProgress>,
    onBack: () -> Unit
) {
    var selectedBadge by remember { mutableStateOf<AchievementBadge?>(null) }
    var selectedCategory by remember { mutableStateOf<BadgeCategory?>(null) }

    val filteredBadges = remember(selectedCategory) {
        if (selectedCategory == null) AchievementRegistry.allBadges
        else AchievementRegistry.getBadgesByCategory(selectedCategory!!)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Badges")
                        Text(
                            text = "${progressMap.count { it.value.isUnlocked }} unlocked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        leadingIcon = if (selectedCategory == null) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
                items(BadgeCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Badges grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBadges) { badge ->
                    BadgeCard(
                        badge = badge,
                        progress = progressMap[badge.id],
                        onClick = { selectedBadge = badge }
                    )
                }
            }
        }
    }

    // Badge detail dialog
    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            progress = progressMap[badge.id],
            onDismiss = { selectedBadge = null }
        )
    }
}

/**
 * Badge unlock celebration - shown when a new badge is earned
 */
@Composable
fun BadgeUnlockCelebration(
    badge: AchievementBadge,
    onDismiss: () -> Unit
) {
    val (primaryColor, secondaryColor) = badge.rarity.getColors()

    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = showContent,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Celebration text
                    Text(
                        text = "🎉 Badge Unlocked! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    Spacer(Modifier.height(24.dp))

                    // Animated badge icon
                    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.4f),
                                        secondaryColor.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .border(
                                    width = 4.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(primaryColor, secondaryColor)
                                    ),
                                    shape = CircleShape
                                )
                                .background(
                                    color = primaryColor.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.icon,
                                fontSize = 48.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Badge name
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    // Description
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    // XP reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 20.sp)
                        Text(
                            text = "+${badge.xpReward} XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Continue button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text("Awesome!", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


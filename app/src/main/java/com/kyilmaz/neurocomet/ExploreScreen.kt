package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.rememberLazyListState
import coil.compose.AsyncImage

/**
 * Production-ready data class for explore topic categories
 */
data class ExploreTopic(
    val id: String,
    val emoji: String,
    val nameRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val memberCount: Int,
    val postCount: Int,
    val gradientColors: List<Color>,
    val backgroundColor: Color,
    val isPopular: Boolean = false,
    val isNew: Boolean = false
)

/**
 * Category section grouping related topics
 */
data class ExploreCategorySection(
    val titleRes: Int,
    val categories: List<ExploreTopic>
)

// Production-ready explore topics with comprehensive categories
private val EXPLORE_CATEGORY_SECTIONS = listOf(
    ExploreCategorySection(
        titleRes = R.string.explore_section_daily_living,
        categories = listOf(
            ExploreTopic(
                id = "adhd_hacks",
                emoji = "🧠",
                nameRes = R.string.explore_topic_adhd_hacks,
                descriptionRes = R.string.explore_topic_adhd_hacks_desc,
                icon = Icons.Outlined.Lightbulb,
                memberCount = 45200,
                postCount = 12400,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isPopular = true
            ),
            ExploreTopic(
                id = "safe_foods",
                emoji = "🍽️",
                nameRes = R.string.explore_topic_safe_foods,
                descriptionRes = R.string.explore_topic_safe_foods_desc,
                icon = Icons.Outlined.Restaurant,
                memberCount = 28900,
                postCount = 8700,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isPopular = true
            ),
            ExploreTopic(
                id = "sleep_rest",
                emoji = "😴",
                nameRes = R.string.explore_topic_sleep,
                descriptionRes = R.string.explore_topic_sleep_desc,
                icon = Icons.Outlined.NightsStay,
                memberCount = 32100,
                postCount = 9200,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isPopular = true
            ),
            ExploreTopic(
                id = "executive_function",
                emoji = "📋",
                nameRes = R.string.explore_topic_executive_function,
                descriptionRes = R.string.explore_topic_executive_function_desc,
                icon = Icons.Outlined.AccountTree,
                memberCount = 21500,
                postCount = 6800,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_sensory,
        categories = listOf(
            ExploreTopic(
                id = "stimming",
                emoji = "✨",
                nameRes = R.string.explore_topic_stimming,
                descriptionRes = R.string.explore_topic_stimming_desc,
                icon = Icons.Outlined.Toys,
                memberCount = 38700,
                postCount = 11200,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isPopular = true
            ),
            ExploreTopic(
                id = "sensory_tips",
                emoji = "👁️",
                nameRes = R.string.explore_topic_sensory_tips,
                descriptionRes = R.string.explore_topic_sensory_tips_desc,
                icon = Icons.Outlined.Hearing,
                memberCount = 41200,
                postCount = 13500,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isPopular = true
            ),
            ExploreTopic(
                id = "meltdown_support",
                emoji = "💙",
                nameRes = R.string.explore_topic_meltdown,
                descriptionRes = R.string.explore_topic_meltdown_desc,
                icon = Icons.Outlined.Psychology,
                memberCount = 19800,
                postCount = 5400,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "sensory_diet",
                emoji = "⚖️",
                nameRes = R.string.explore_topic_sensory_diet,
                descriptionRes = R.string.explore_topic_sensory_diet_desc,
                icon = Icons.Outlined.Balance,
                memberCount = 15600,
                postCount = 4200,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isNew = true
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_social,
        categories = listOf(
            ExploreTopic(
                id = "social_skills",
                emoji = "🤝",
                nameRes = R.string.explore_topic_social_skills,
                descriptionRes = R.string.explore_topic_social_skills_desc,
                icon = Icons.Outlined.Groups,
                memberCount = 35400,
                postCount = 10100,
                gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
                backgroundColor = Color(0xFFE0F7FA)
            ),
            ExploreTopic(
                id = "masking",
                emoji = "🎭",
                nameRes = R.string.explore_topic_masking,
                descriptionRes = R.string.explore_topic_masking_desc,
                icon = Icons.Outlined.TheaterComedy,
                memberCount = 27300,
                postCount = 7800,
                gradientColors = listOf(Color(0xFF84CC16), Color(0xFF65A30D)),
                backgroundColor = Color(0xFFF1F8E9)
            ),
            ExploreTopic(
                id = "relationships",
                emoji = "💕",
                nameRes = R.string.explore_topic_relationships,
                descriptionRes = R.string.explore_topic_relationships_desc,
                icon = Icons.Outlined.Favorite,
                memberCount = 29100,
                postCount = 8900,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "communication_aac",
                emoji = "💬",
                nameRes = R.string.explore_topic_aac,
                descriptionRes = R.string.explore_topic_aac_desc,
                icon = Icons.Outlined.RecordVoiceOver,
                memberCount = 12400,
                postCount = 3600,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                backgroundColor = Color(0xFFE8EAF6),
                isNew = true
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_work,
        categories = listOf(
            ExploreTopic(
                id = "work_school",
                emoji = "💼",
                nameRes = R.string.explore_topic_work_school,
                descriptionRes = R.string.explore_topic_work_school_desc,
                icon = Icons.Outlined.School,
                memberCount = 42600,
                postCount = 14200,
                gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "disclosure",
                emoji = "📢",
                nameRes = R.string.explore_topic_disclosure,
                descriptionRes = R.string.explore_topic_disclosure_desc,
                icon = Icons.Outlined.Campaign,
                memberCount = 18900,
                postCount = 5100,
                gradientColors = listOf(Color(0xFFA855F7), Color(0xFF9333EA)),
                backgroundColor = Color(0xFFF3E5F5)
            ),
            ExploreTopic(
                id = "career_paths",
                emoji = "📈",
                nameRes = R.string.explore_topic_career,
                descriptionRes = R.string.explore_topic_career_desc,
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                memberCount = 23700,
                postCount = 6400,
                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                backgroundColor = Color(0xFFE8F5E9)
            ),
            ExploreTopic(
                id = "college_transition",
                emoji = "🏫",
                nameRes = R.string.explore_topic_college,
                descriptionRes = R.string.explore_topic_college_desc,
                icon = Icons.Outlined.Apartment,
                memberCount = 16200,
                postCount = 4800,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                backgroundColor = Color(0xFFE3F2FD)
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_interests,
        categories = listOf(
            ExploreTopic(
                id = "special_interests",
                emoji = "🎮",
                nameRes = R.string.explore_topic_gaming,
                descriptionRes = R.string.explore_topic_gaming_desc,
                icon = Icons.Outlined.Star,
                memberCount = 51200,
                postCount = 18700,
                gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                backgroundColor = Color(0xFFFFF8E1),
                isPopular = true
            ),
            ExploreTopic(
                id = "gaming",
                emoji = "🕹️",
                nameRes = R.string.explore_topic_gaming,
                descriptionRes = R.string.explore_topic_gaming_desc,
                icon = Icons.Outlined.SportsEsports,
                memberCount = 38400,
                postCount = 12100,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
                backgroundColor = Color(0xFFEDE7F6)
            ),
            ExploreTopic(
                id = "creative_arts",
                emoji = "🎨",
                nameRes = R.string.explore_topic_art,
                descriptionRes = R.string.explore_topic_art_desc,
                icon = Icons.Outlined.Palette,
                memberCount = 29800,
                postCount = 9400,
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "tech_science",
                emoji = "💻",
                nameRes = R.string.explore_topic_coding,
                descriptionRes = R.string.explore_topic_coding_desc,
                icon = Icons.Outlined.Code,
                memberCount = 34100,
                postCount = 10800,
                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
                backgroundColor = Color(0xFFE0F7FA)
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_health,
        categories = listOf(
            ExploreTopic(
                id = "mental_health",
                emoji = "🧘",
                nameRes = R.string.explore_topic_mental_health,
                descriptionRes = R.string.explore_topic_mental_health_desc,
                icon = Icons.Outlined.SelfImprovement,
                memberCount = 47800,
                postCount = 15600,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                backgroundColor = Color(0xFFE8F5E9)
            ),
            ExploreTopic(
                id = "therapy_resources",
                emoji = "🏥",
                nameRes = R.string.explore_topic_mental_health,
                descriptionRes = R.string.explore_topic_mental_health_desc,
                icon = Icons.Outlined.MedicalServices,
                memberCount = 21400,
                postCount = 6200,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                backgroundColor = Color(0xFFE8EAF6)
            ),
            ExploreTopic(
                id = "medication",
                emoji = "💊",
                nameRes = R.string.explore_topic_medication,
                descriptionRes = R.string.explore_topic_medication_desc,
                icon = Icons.Outlined.Medication,
                memberCount = 25600,
                postCount = 7100,
                gradientColors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48)),
                backgroundColor = Color(0xFFFFEBEE)
            ),
            ExploreTopic(
                id = "burnout_recovery",
                emoji = "🔋",
                nameRes = R.string.explore_topic_burnout,
                descriptionRes = R.string.explore_topic_burnout_desc,
                icon = Icons.Outlined.BatteryAlert,
                memberCount = 31200,
                postCount = 8900,
                gradientColors = listOf(Color(0xFFEAB308), Color(0xFFCA8A04)),
                backgroundColor = Color(0xFFFFFDE7)
            )
        )
    ),
    ExploreCategorySection(
        titleRes = R.string.explore_section_family,
        categories = listOf(
            ExploreTopic(
                id = "late_diagnosis",
                emoji = "🔍",
                nameRes = R.string.explore_topic_late_diagnosed,
                descriptionRes = R.string.explore_topic_late_diagnosed_desc,
                icon = Icons.Outlined.Explore,
                memberCount = 28700,
                postCount = 8200,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "parenting",
                emoji = "👨‍👩‍👧",
                nameRes = R.string.explore_topic_parenting,
                descriptionRes = R.string.explore_topic_parenting_desc,
                icon = Icons.Outlined.FamilyRestroom,
                memberCount = 22100,
                postCount = 6700,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isNew = true
            ),
            ExploreTopic(
                id = "independent_living",
                emoji = "🏠",
                nameRes = R.string.explore_topic_parenting,
                descriptionRes = R.string.explore_topic_parenting_desc,
                icon = Icons.Outlined.Home,
                memberCount = 19400,
                postCount = 5500,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "adulting",
                emoji = "✅",
                nameRes = R.string.explore_topic_executive_function,
                descriptionRes = R.string.explore_topic_executive_function_desc,
                icon = Icons.Outlined.Checklist,
                memberCount = 35800,
                postCount = 11400,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isNew = true
            )
        )
    ),
    // LGBTQ+ & Identity Section
    ExploreCategorySection(
        titleRes = R.string.explore_section_lgbtq,
        categories = listOf(
            ExploreTopic(
                id = "lgbtq_nd",
                emoji = "🏳️‍🌈",
                nameRes = R.string.explore_topic_lgbtq_nd,
                descriptionRes = R.string.explore_topic_lgbtq_nd_desc,
                icon = Icons.Outlined.Groups,
                memberCount = 42300,
                postCount = 15800,
                gradientColors = listOf(Color(0xFFE40303), Color(0xFF004DFF)),
                backgroundColor = Color(0xFFFFF3E0),
                isPopular = true
            ),
            ExploreTopic(
                id = "trans_nd",
                emoji = "🏳️‍⚧️",
                nameRes = R.string.explore_topic_trans_nd,
                descriptionRes = R.string.explore_topic_trans_nd_desc,
                icon = Icons.Outlined.Favorite,
                memberCount = 28900,
                postCount = 9400,
                gradientColors = listOf(Color(0xFF5BCEFA), Color(0xFFF5A9B8)),
                backgroundColor = Color(0xFFE3F2FD),
                isPopular = true
            ),
            ExploreTopic(
                id = "queer_pride",
                emoji = "✨",
                nameRes = R.string.explore_topic_queer_pride,
                descriptionRes = R.string.explore_topic_queer_pride_desc,
                icon = Icons.Outlined.Star,
                memberCount = 35600,
                postCount = 12100,
                gradientColors = listOf(Color(0xFFFF69B4), Color(0xFF9B59B6)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "ace_aro_spectrum",
                emoji = "🖤",
                nameRes = R.string.explore_topic_ace_aro,
                descriptionRes = R.string.explore_topic_ace_aro_desc,
                icon = Icons.Outlined.Psychology,
                memberCount = 18700,
                postCount = 5200,
                gradientColors = listOf(Color(0xFF000000), Color(0xFF810081)),
                backgroundColor = Color(0xFFF3E5F5),
                isNew = true
            ),
            ExploreTopic(
                id = "nonbinary_nd",
                emoji = "💛",
                nameRes = R.string.explore_topic_nonbinary,
                descriptionRes = R.string.explore_topic_nonbinary_desc,
                icon = Icons.Outlined.Balance,
                memberCount = 24100,
                postCount = 7800,
                gradientColors = listOf(Color(0xFFFFF430), Color(0xFF9C59D1)),
                backgroundColor = Color(0xFFFFF8E1)
            ),
            ExploreTopic(
                id = "coming_out",
                emoji = "🚪",
                nameRes = R.string.explore_topic_coming_out,
                descriptionRes = R.string.explore_topic_coming_out_desc,
                icon = Icons.Outlined.Campaign,
                memberCount = 31400,
                postCount = 10200,
                gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44A08D)),
                backgroundColor = Color(0xFFE0F2F1)
            )
        )
    )
)

// Flat list for backward compatibility
private val EXPLORE_TOPICS = EXPLORE_CATEGORY_SECTIONS.flatMap { it.categories }

@Composable
private fun rememberExploreTopicsThemed(): List<ExploreCategorySection> {
    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminance() < 0.5f

    // Softer, more harmonious gradients that work with dynamic theming
    // Use container colors with adjusted opacity for better visual harmony
    val slotGradients = if (isDark) {
        // Dark mode: Use softer, muted gradients
        listOf(
            listOf(cs.primaryContainer.copy(alpha = 0.9f), cs.primary.copy(alpha = 0.7f)),
            listOf(cs.secondaryContainer.copy(alpha = 0.9f), cs.secondary.copy(alpha = 0.7f)),
            listOf(cs.tertiaryContainer.copy(alpha = 0.9f), cs.tertiary.copy(alpha = 0.7f)),
            listOf(cs.surfaceContainerHighest, cs.surfaceContainerHigh)
        )
    } else {
        // Light mode: Use tinted versions that aren't too bright
        listOf(
            listOf(cs.primaryContainer, cs.primary.copy(alpha = 0.6f)),
            listOf(cs.secondaryContainer, cs.secondary.copy(alpha = 0.6f)),
            listOf(cs.tertiaryContainer, cs.tertiary.copy(alpha = 0.6f)),
            listOf(cs.surfaceContainerHigh, cs.surfaceContainerHighest)
        )
    }

    val slotBackgrounds = listOf(
        cs.primaryContainer,
        cs.secondaryContainer,
        cs.tertiaryContainer,
        cs.surfaceContainerHigh
    )

    var slotIndex = 0

    return EXPLORE_CATEGORY_SECTIONS.map { section ->
        section.copy(
            categories = section.categories.map { topic ->
                val grad = slotGradients[slotIndex % slotGradients.size]
                val bg = slotBackgrounds[slotIndex % slotBackgrounds.size]
                slotIndex++

                topic.copy(
                    gradientColors = if (topic.gradientColors.all { it == Color.Unspecified }) grad else topic.gradientColors,
                    backgroundColor = if (topic.backgroundColor == Color.Unspecified) bg else topic.backgroundColor
                )
            }
        )
    }
}

@Composable
fun ExploreScreen(
    posts: List<Post>,
    safetyState: SafetyState,
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {},
    onPostClick: (Post) -> Unit = {},
    onLikePost: (Long) -> Unit = {},
    onSharePost: (Context, Post) -> Unit = { _, _ -> },
    onCommentPost: (Post) -> Unit = {},
    onProfileClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val parentalState = remember { ParentalControlsSettings.getState(context) }

    val themedSections = rememberExploreTopicsThemed()
    val themedTopics = remember(themedSections) { themedSections.flatMap { it.categories } }

    // Check if Explore is blocked by parental controls
    val restriction = shouldBlockFeature(parentalState, BlockableFeature.EXPLORE)

    if (restriction != null) {
        ParentalBlockedScreen(
            restrictionType = restriction,
            featureName = "Explore"
        )
        return
    }

    // Filter posts by audience level
    val filteredPosts = remember(posts, safetyState.audience) {
        ContentFiltering.filterPostsByAudience(posts, safetyState.audience)
    }

    // Featured topics (popular ones)
    val featuredTopics = remember(themedTopics) {
        themedTopics.filter { it.isPopular }.take(5)
    }

    // LazyList state for parallax effects
    val lazyListState = rememberLazyListState()

    // Calculate parallax offset based on scroll
    val parallaxOffset by remember {
        derivedStateOf {
            val firstItemOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
            val firstIndex = lazyListState.firstVisibleItemIndex
            if (firstIndex == 0) firstItemOffset * 0.5f else 0f
        }
    }

    val headerAlpha by remember {
        derivedStateOf {
            val firstItemOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
            val firstIndex = lazyListState.firstVisibleItemIndex
            if (firstIndex == 0) 1f - (firstItemOffset / 500f).coerceIn(0f, 0.3f) else 0.7f
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header with status bar padding and parallax effect
        item(key = "explore_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .graphicsLayer {
                        translationY = -parallaxOffset * 0.3f
                        alpha = headerAlpha
                    }
            ) {
                Text(
                    text = stringResource(R.string.explore_title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.explore_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Featured Categories Carousel
        item(key = "featured_carousel") {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.explore_featured),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {}) {
                        Text(stringResource(R.string.explore_see_all))
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredTopics) { topic ->
                        FeaturedCategoryCard(
                            topic = topic,
                            onClick = { onTopicClick(topic.id) }
                        )
                    }
                }
            }
        }

        // Category Sections
        themedSections.forEach { section ->
            item(key = "section_header_${section.titleRes}") {
                Text(
                    text = stringResource(section.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            items(
                items = section.categories.chunked(2),
                key = { rowCategories -> "section_${section.titleRes}_${rowCategories.map { it.id }.joinToString("_")}" }
            ) { rowCategories ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCategories.forEach { topic ->
                        CategoryCard(
                            topic = topic,
                            onClick = { onTopicClick(topic.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty space if odd number of categories
                    if (rowCategories.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Divider before suggested posts
        item(key = "suggested_header") {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.explore_for_you),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Suggested Posts List (filtered by audience)
        if (filteredPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No posts available for your content preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredPosts.take(10), key = { it.id ?: it.hashCode() }) { post ->
                ExplorePostCard(
                    post = post,
                    onCardClick = { onPostClick(post) },
                    onLike = { post.id?.let(onLikePost) },
                    onShare = { onSharePost(context, post) },
                    onComment = { onCommentPost(post) },
                    onProfileClick = onProfileClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedCategoryCard(
    topic: ExploreTopic,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(topic.gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topic.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (topic.isNew) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = stringResource(R.string.explore_new).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (topic.isPopular) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🔥",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = stringResource(topic.nameRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(topic.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.explore_members, formatCount(topic.memberCount)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.explore_posts, formatCount(topic.postCount)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    topic: ExploreTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = topic.emoji,
                    style = MaterialTheme.typography.headlineMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (topic.isNew) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = stringResource(R.string.explore_new).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (topic.isPopular) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = stringResource(R.string.explore_popular),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = stringResource(topic.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(topic.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.explore_members, formatCount(topic.memberCount)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

/**
 * Share a category/topic via system share sheet
 */
private fun shareCategory(context: Context, topic: ExploreTopic) {
    val topicName = context.getString(topic.nameRes)
    val topicDescription = context.getString(topic.descriptionRes)
    val membersFormatted = formatCount(topic.memberCount)
    val postsFormatted = formatCount(topic.postCount)

    val shareText = context.getString(
        R.string.share_explore_topic_text,
        topicName, topic.emoji, topicDescription, membersFormatted, postsFormatted, topic.id
    )
    val shareSubject = context.getString(R.string.share_explore_topic_subject, topic.emoji, topicName)
    val shareChooser = context.getString(R.string.share_explore_topic_chooser, topicName)

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, shareSubject)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, shareChooser)
    context.startActivity(shareIntent)
}

/**
 * 3-dot menu options for category cards
 */
@Composable
private fun CategoryOptionsMenu(
    topic: ExploreTopic,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onHide: () -> Unit,
    onReport: () -> Unit,
    isSaved: Boolean = false,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        // Share option
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_share)) },
            onClick = {
                onShare()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        // Save/Bookmark option
        DropdownMenuItem(
            text = { Text(stringResource(if (isSaved) R.string.post_unbookmark else R.string.post_bookmark)) },
            onClick = {
                onSave()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Not interested option
        DropdownMenuItem(
            text = { Text(stringResource(R.string.post_not_interested)) },
            onClick = {
                onHide()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Report option
        DropdownMenuItem(
            text = { Text(stringResource(R.string.menu_report)) },
            onClick = {
                onReport()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

/**
 * Report dialog for categories
 */
@Composable
private fun ReportCategoryDialog(
    topic: ExploreTopic,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }

    val reportReasons = listOf(
        "Misinformation",
        "Harassment or bullying",
        "Hate speech",
        "Spam or scam",
        "Inappropriate content",
        "Privacy violation",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Report ${stringResource(topic.nameRes)}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Why are you reporting this community?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                reportReasons.forEach { reason ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason },
                        shape = MaterialTheme.shapes.small,
                        color = if (selectedReason == reason)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedReason == reason)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedReason == reason) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedReason?.let { onReport(it) }
                    onDismiss()
                },
                enabled = selectedReason != null
            ) {
                Text(stringResource(R.string.report_dialog_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
private fun ExplorePostCard(
    post: Post,
    onCardClick: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onComment: () -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(post.isLikedByMe) }
    var likeCount by remember { mutableStateOf(post.likes) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onCardClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with avatar and username
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userAvatar
                        ?: "https://i.pravatar.cc/150?u=${post.userId ?: "anon"}",
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { post.userId?.let { onProfileClick(it) } }
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { post.userId?.let { onProfileClick(it) } }
                ) {
                    Text(
                        text = post.userId ?: "Anonymous",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                            onLike()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "$likeCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comments count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onComment)
                ) {
                    IconButton(
                        onClick = onComment,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.shares}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================================
// TOPIC DETAIL SCREEN - Mock interfaces for each category
// ============================================================================

/**
 * Mock posts specific to each topic for realistic preview
 */
private fun getMockPostsForTopic(topicId: String): List<TopicPost> {
    return when (topicId.lowercase().replace(" ", "_")) {
        "adhd_hacks", "adhd hacks" -> listOf(
            TopicPost("1", R.string.post_adhd_1_author, R.string.post_adhd_1_content, 2, 234, 45, avatarUrl("finn")),
            TopicPost("2", R.string.post_adhd_2_author, R.string.post_adhd_2_content, 4, 189, 32, avatarUrl("tina")),
            TopicPost("3", R.string.post_adhd_3_author, R.string.post_adhd_3_content, 6, 312, 56, avatarUrl("harry")),
            TopicPost("4", R.string.post_adhd_4_author, R.string.post_adhd_4_content, 8, 167, 28, avatarUrl("sally")),
            TopicPost("5", R.string.post_adhd_5_author, R.string.post_adhd_5_content, 12, 245, 41, avatarUrl("ivy"))
        )
        "safe_foods", "safe foods" -> listOf(
            TopicPost("1", R.string.post_safefood_1_author, R.string.post_safefood_1_content, 1, 156, 89, avatarUrl("texture")),
            TopicPost("2", R.string.post_safefood_2_author, R.string.post_safefood_2_content, 3, 423, 67, avatarUrl("beige")),
            TopicPost("3", R.string.post_safefood_3_author, R.string.post_safefood_3_content, 5, 567, 123, avatarUrl("chef")),
            TopicPost("4", R.string.post_safefood_4_author, R.string.post_safefood_4_content, 7, 234, 78, avatarUrl("pete")),
            TopicPost("5", R.string.post_safefood_5_author, R.string.post_safefood_5_content, 10, 189, 45, avatarUrl("molly"))
        )
        "sleep_rest", "sleep & rest" -> listOf(
            TopicPost("1", R.string.post_sleep_1_author, R.string.post_sleep_1_content, 1, 345, 78, avatarUrl("nate")),
            TopicPost("2", R.string.post_sleep_2_author, R.string.post_sleep_2_content, 2, 278, 56, avatarUrl("rachel")),
            TopicPost("3", R.string.post_sleep_3_author, R.string.post_sleep_3_content, 4, 412, 89, avatarUrl("ian")),
            TopicPost("4", R.string.post_sleep_4_author, R.string.post_sleep_4_content, 6, 234, 67, avatarUrl("steph")),
            TopicPost("5", R.string.post_sleep_5_author, R.string.post_sleep_5_content, 9, 167, 34, avatarUrl("dan"))
        )
        "stimming" -> listOf(
            TopicPost("1", R.string.post_stim_1_author, R.string.post_stim_1_content, 1, 456, 112, avatarUrl("stimsquad")),
            TopicPost("2", R.string.post_stim_2_author, R.string.post_stim_2_content, 2, 678, 145, avatarUrl("flapper")),
            TopicPost("3", R.string.post_stim_3_author, R.string.post_stim_3_content, 4, 234, 67, avatarUrl("chewy")),
            TopicPost("4", R.string.post_stim_4_author, R.string.post_stim_4_content, 6, 189, 45, avatarUrl("sophie")),
            TopicPost("5", R.string.post_stim_5_author, R.string.post_stim_5_content, 8, 345, 78, avatarUrl("seeker"))
        )
        "sensory_tips" -> listOf(
            TopicPost("1", R.string.post_sensory_1_author, R.string.post_sensory_1_content, 1, 567, 134, avatarUrl("ollie")),
            TopicPost("2", R.string.post_sensory_2_author, R.string.post_sensory_2_content, 3, 345, 89, avatarUrl("luna")),
            TopicPost("3", R.string.post_sensory_3_author, R.string.post_sensory_3_content, 5, 234, 78, avatarUrl("taylor")),
            TopicPost("4", R.string.post_sensory_4_author, R.string.post_sensory_4_content, 7, 189, 56, avatarUrl("alex")),
            TopicPost("5", R.string.post_sensory_5_author, R.string.post_sensory_5_content, 10, 423, 97, avatarUrl("quinn"))
        )
        "meltdown_support" -> listOf(
            TopicPost("1", R.string.post_meltdown_1_author, R.string.post_meltdown_1_content, 2, 678, 156, avatarUrl("ren")),
            TopicPost("2", R.string.post_meltdown_2_author, R.string.post_meltdown_2_content, 4, 456, 123, avatarUrl("carla")),
            TopicPost("3", R.string.post_meltdown_3_author, R.string.post_meltdown_3_content, 6, 345, 89, avatarUrl("pat")),
            TopicPost("4", R.string.post_meltdown_4_author, R.string.post_meltdown_4_content, 8, 234, 67, avatarUrl("sam")),
            TopicPost("5", R.string.post_meltdown_5_author, R.string.post_meltdown_5_content, 12, 512, 134, avatarUrl("sue"))
        )
        "social_skills" -> listOf(
            TopicPost("1", R.string.post_social_1_author, R.string.post_social_1_content, 1, 456, 112, avatarUrl("coach")),
            TopicPost("2", R.string.post_social_2_author, R.string.post_social_2_content, 3, 567, 134, avatarUrl("fred")),
            TopicPost("3", R.string.post_social_3_author, R.string.post_social_3_content, 5, 389, 89, avatarUrl("andy")),
            TopicPost("4", R.string.post_social_4_author, R.string.post_social_4_content, 7, 423, 97, avatarUrl("bella")),
            TopicPost("5", R.string.post_social_5_author, R.string.post_social_5_content, 10, 345, 78, avatarUrl("paul"))
        )
        "masking" -> listOf(
            TopicPost("1", R.string.post_mask_1_author, R.string.post_mask_1_content, 1, 678, 167, avatarUrl("uri")),
            TopicPost("2", R.string.post_mask_2_author, R.string.post_mask_2_content, 3, 567, 145, avatarUrl("beth")),
            TopicPost("3", R.string.post_mask_3_author, R.string.post_mask_3_content, 5, 456, 123, avatarUrl("ava")),
            TopicPost("4", R.string.post_mask_4_author, R.string.post_mask_4_content, 7, 389, 89, avatarUrl("eli")),
            TopicPost("5", R.string.post_mask_5_author, R.string.post_mask_5_content, 10, 423, 97, avatarUrl("grace"))
        )
        "special_interests" -> listOf(
            TopicPost("1", R.string.post_special_1_author, R.string.post_special_1_content, 1, 567, 145, avatarUrl("dave")),
            TopicPost("2", R.string.post_special_2_author, R.string.post_special_2_content, 2, 478, 234, avatarUrl("tracker")),
            TopicPost("3", R.string.post_special_3_author, R.string.post_special_3_content, 4, 345, 89, avatarUrl("mike")),
            TopicPost("4", R.string.post_special_4_author, R.string.post_special_4_content, 6, 512, 123, avatarUrl("lore")),
            TopicPost("5", R.string.post_special_5_author, R.string.post_special_5_content, 9, 423, 97, avatarUrl("star"))
        )
        "mental_health" -> listOf(
            TopicPost("1", R.string.post_mental_1_author, R.string.post_mental_1_content, 1, 456, 123, avatarUrl("ally")),
            TopicPost("2", R.string.post_mental_2_author, R.string.post_mental_2_content, 3, 678, 178, avatarUrl("diana")),
            TopicPost("3", R.string.post_mental_3_author, R.string.post_mental_3_content, 5, 345, 89, avatarUrl("tom")),
            TopicPost("4", R.string.post_mental_4_author, R.string.post_mental_4_content, 7, 234, 67, avatarUrl("hannah")),
            TopicPost("5", R.string.post_mental_5_author, R.string.post_mental_5_content, 10, 567, 145, avatarUrl("chris"))
        )
        "work_school" -> listOf(
            TopicPost("1", R.string.post_work_1_author, R.string.post_work_1_content, 1, 567, 145, avatarUrl("ace")),
            TopicPost("2", R.string.post_work_2_author, R.string.post_work_2_content, 3, 423, 112, avatarUrl("sara")),
            TopicPost("3", R.string.post_work_3_author, R.string.post_work_3_content, 5, 345, 89, avatarUrl("rick")),
            TopicPost("4", R.string.post_work_4_author, R.string.post_work_4_content, 7, 234, 67, avatarUrl("mary")),
            TopicPost("5", R.string.post_work_5_author, R.string.post_work_5_content, 10, 456, 123, avatarUrl("ccoach"))
        )
        "late_diagnosis" -> listOf(
            TopicPost("1", R.string.post_late_1_author, R.string.post_late_1_content, 1, 678, 189, avatarUrl("lisa")),
            TopicPost("2", R.string.post_late_2_author, R.string.post_late_2_content, 3, 567, 156, avatarUrl("midlife")),
            TopicPost("3", R.string.post_late_3_author, R.string.post_late_3_content, 5, 456, 123, avatarUrl("identity")),
            TopicPost("4", R.string.post_late_4_author, R.string.post_late_4_content, 7, 389, 97, avatarUrl("grief")),
            TopicPost("5", R.string.post_late_5_author, R.string.post_late_5_content, 10, 512, 145, avatarUrl("chapter"))
        )
        "lgbtq_nd" -> listOf(
            TopicPost("1", R.string.post_lgbtq_1_author, R.string.post_lgbtq_1_content, 1, 567, 145, avatarUrl("rainbow")),
            TopicPost("2", R.string.post_lgbtq_2_author, R.string.post_lgbtq_2_content, 3, 456, 123, avatarUrl("proud")),
            TopicPost("3", R.string.post_lgbtq_3_author, R.string.post_lgbtq_3_content, 5, 345, 89, avatarUrl("intersect")),
            TopicPost("4", R.string.post_lgbtq_4_author, R.string.post_lgbtq_4_content, 7, 234, 67, avatarUrl("community")),
            TopicPost("5", R.string.post_lgbtq_5_author, R.string.post_lgbtq_5_content, 10, 389, 97, avatarUrl("allied"))
        )
        "trans_nd" -> listOf(
            TopicPost("1", R.string.post_trans_1_author, R.string.post_trans_1_content, 1, 456, 123, avatarUrl("trans1")),
            TopicPost("2", R.string.post_trans_2_author, R.string.post_trans_2_content, 3, 389, 97, avatarUrl("gender")),
            TopicPost("3", R.string.post_trans_3_author, R.string.post_trans_3_content, 5, 345, 89, avatarUrl("support")),
            TopicPost("4", R.string.post_trans_4_author, R.string.post_trans_4_content, 7, 278, 67, avatarUrl("tips")),
            TopicPost("5", R.string.post_trans_5_author, R.string.post_trans_5_content, 10, 512, 145, avatarUrl("authentic"))
        )
        "queer_pride" -> listOf(
            TopicPost("1", R.string.post_pride_1_author, R.string.post_pride_1_content, 1, 567, 145, avatarUrl("parade")),
            TopicPost("2", R.string.post_pride_2_author, R.string.post_pride_2_content, 3, 456, 123, avatarUrl("queercommunity")),
            TopicPost("3", R.string.post_pride_3_author, R.string.post_pride_3_content, 5, 389, 97, avatarUrl("love")),
            TopicPost("4", R.string.post_pride_4_author, R.string.post_pride_4_content, 7, 345, 89, avatarUrl("joyful")),
            TopicPost("5", R.string.post_pride_5_author, R.string.post_pride_5_content, 10, 423, 112, avatarUrl("history"))
        )
        "ace_aro_spectrum" -> listOf(
            TopicPost("1", R.string.post_ace_1_author, R.string.post_ace_1_content, 1, 456, 123, avatarUrl("acespace")),
            TopicPost("2", R.string.post_ace_2_author, R.string.post_ace_2_content, 3, 389, 97, avatarUrl("aroace")),
            TopicPost("3", R.string.post_ace_3_author, R.string.post_ace_3_content, 5, 345, 89, avatarUrl("spectrum")),
            TopicPost("4", R.string.post_ace_4_author, R.string.post_ace_4_content, 7, 278, 67, avatarUrl("normalize")),
            TopicPost("5", R.string.post_ace_5_author, R.string.post_ace_5_content, 10, 423, 112, avatarUrl("acepride"))
        )
        "nonbinary_nd" -> listOf(
            TopicPost("1", R.string.post_nb_1_author, R.string.post_nb_1_content, 1, 567, 145, avatarUrl("nbninja")),
            TopicPost("2", R.string.post_nb_2_author, R.string.post_nb_2_content, 3, 456, 123, avatarUrl("theythem")),
            TopicPost("3", R.string.post_nb_3_author, R.string.post_nb_3_content, 5, 389, 97, avatarUrl("fluid")),
            TopicPost("4", R.string.post_nb_4_author, R.string.post_nb_4_content, 7, 345, 89, avatarUrl("enby")),
            TopicPost("5", R.string.post_nb_5_author, R.string.post_nb_5_content, 10, 423, 112, avatarUrl("journey"))
        )
        "coming_out" -> listOf(
            TopicPost("1", R.string.post_coming_1_author, R.string.post_coming_1_content, 1, 567, 156, avatarUrl("cameout")),
            TopicPost("2", R.string.post_coming_2_author, R.string.post_coming_2_content, 3, 456, 123, avatarUrl("safeharbor")),
            TopicPost("3", R.string.post_coming_3_author, R.string.post_coming_3_content, 5, 389, 97, avatarUrl("journeys")),
            TopicPost("4", R.string.post_coming_4_author, R.string.post_coming_4_content, 7, 345, 89, avatarUrl("figuring")),
            TopicPost("5", R.string.post_coming_5_author, R.string.post_coming_5_content, 10, 512, 145, avatarUrl("family"))
        )
        else -> listOf(
            TopicPost("1", R.string.post_default_1_author, R.string.post_default_1_content, 1, 234, 56, avatarUrl("community")),
            TopicPost("2", R.string.post_default_2_author, R.string.post_default_2_content, 3, 189, 45, avatarUrl("helen")),
            TopicPost("3", R.string.post_default_3_author, R.string.post_default_3_content, 5, 156, 34, avatarUrl("ron")),
            TopicPost("4", R.string.post_default_4_author, R.string.post_default_4_content, 7, 278, 67, avatarUrl("sue")),
            TopicPost("5", R.string.post_default_5_author, R.string.post_default_5_content, 10, 312, 89, avatarUrl("will"))
        )
    }
}

/**
 * Data class for topic-specific posts
 */
data class TopicPost(
    val id: String,
    val authorRes: Int,
    val contentRes: Int,
    val timeAgoHours: Int,
    val likes: Int,
    val comments: Int,
    val avatarUrl: String
)

/**
 * Get topic details by name/ID
 */
fun getTopicByName(name: String): ExploreTopic? {
    return EXPLORE_TOPICS.find {
        it.id.equals(name, ignoreCase = true) ||
        it.id.equals(name.lowercase().replace(" ", "_"), ignoreCase = true)
    }
}

// Make EXPLORE_TOPICS accessible
val ALL_EXPLORE_TOPICS = EXPLORE_TOPICS

/**
 * Comprehensive Topic Detail Screen with mock interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicName: String,
    onBack: () -> Unit,
    onLikePost: (String) -> Unit = {},
    onCommentPost: (String) -> Unit = {},
    onSharePost: (String) -> Unit = {}
) {
    val topic = remember(topicName) { getTopicByName(topicName) }
    val mockPosts = remember(topicName) { getMockPostsForTopic(topicName) }
    val context = LocalContext.current

    var isJoined by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabPosts = stringResource(R.string.topic_tab_posts)
    val tabResources = stringResource(R.string.topic_tab_resources)
    val tabEvents = stringResource(R.string.topic_tab_events)
    val tabMembers = stringResource(R.string.topic_tab_members)
    val tabs = remember(tabPosts, tabResources, tabEvents, tabMembers) {
        listOf(tabPosts, tabResources, tabEvents, tabMembers)
    }

    if (topic == null) {
        // Fallback for unknown topics
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text(topicName) },
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
                Text(stringResource(R.string.topic_not_found), style = MaterialTheme.typography.titleLarge)
            }
        }
        return
    }

    // Pre-compute string resources for use in callbacks
    val topicDisplayName = stringResource(topic.nameRes)
    val shareTextFormatted = stringResource(R.string.topic_share_text, topicDisplayName) + " #${topicName.replace(" ", "")}"
    val shareTopicLabel = stringResource(R.string.topic_share)
    val moreOptionsMessage = stringResource(R.string.topic_more_options_soon)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareTextFormatted)
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, shareTopicLabel)
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(
                            context,
                            moreOptionsMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Brush.linearGradient(topic.gradientColors))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic.emoji,
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(topic.nameRes),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.topic_members_posts, formatCount(topic.memberCount), formatCount(topic.postCount)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Description & Join Button
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(topic.descriptionRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isJoined = !isJoined },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isJoined) MaterialTheme.colorScheme.surfaceVariant
                                           else topic.gradientColors.first()
                        )
                    ) {
                        Icon(
                            imageVector = if (isJoined) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isJoined) stringResource(R.string.topic_joined) else stringResource(R.string.topic_join_community),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Tab Row
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Posts Tab
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(mockPosts) { post ->
                        TopicPostCard(
                            post = post,
                            topicColor = topic.gradientColors.first(),
                            onLike = { onLikePost(post.id) },
                            onComment = { onCommentPost(post.id) },
                            onShare = { onSharePost(post.id) }
                        )
                    }
                }
                1 -> {
                    // Resources Tab
                    item {
                        ResourcesSection(topicName = stringResource(topic.nameRes))
                    }
                }
                2 -> {
                    // Events Tab
                    item {
                        EventsSection(topicName = stringResource(topic.nameRes))
                    }
                }
                3 -> {
                    // Members Tab
                    item {
                        MembersSection(topicName = stringResource(topic.nameRes))
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun TopicPostCard(
    post: TopicPost,
    topicColor: Color,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(post.likes) }

    val author = stringResource(post.authorRes)
    val content = stringResource(post.contentRes)
    val timeAgo = stringResource(R.string.time_hours_ago, post.timeAgoHours)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = author,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        onLike()
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) topicColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$likeCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onComment)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onShare)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.member_share),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourcesSection(topicName: String) {
    val resources = listOf(
        ResourceItem("📚", R.string.resource_getting_started, R.string.resource_getting_started_desc, R.string.resource_pinned),
        ResourceItem("🎨", R.string.resource_themes, R.string.resource_themes_desc, R.string.resource_new),
        ResourceItem("🔤", R.string.resource_fonts, R.string.resource_fonts_desc, R.string.resource_fonts_count),
        ResourceItem("🎬", R.string.resource_animation, R.string.resource_animation_desc, R.string.resource_animation_count),
        ResourceItem("🎄", R.string.resource_events, R.string.resource_events_desc, R.string.resource_events_count),
        ResourceItem("🛡️", R.string.resource_safety, R.string.resource_safety_desc, R.string.resource_essential),
        ResourceItem("💡", R.string.resource_tips, R.string.resource_tips_desc, R.string.resource_tips_count),
        ResourceItem("🔗", R.string.resource_links, R.string.resource_links_desc, R.string.resource_links_updated),
        ResourceItem("📝", R.string.resource_wiki, R.string.resource_wiki_desc, R.string.resource_wiki_count),
        ResourceItem("🎥", R.string.resource_videos, R.string.resource_videos_desc, R.string.resource_videos_count),
        ResourceItem("📖", R.string.resource_reading, R.string.resource_reading_desc, R.string.resource_reading_count)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.topic_resources_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        resources.forEach { resource ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open resource */ },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = resource.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(resource.titleRes),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(resource.descriptionRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(resource.metaRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private data class ResourceItem(
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val metaRes: Int
)

@Composable
private fun EventsSection(topicName: String) {
    val events = listOf(
        EventItem("🎤", R.string.event_weekly_chat, R.string.event_weekly_chat_time, R.string.event_type_discussion, true),
        EventItem("📚", R.string.event_book_club, R.string.event_book_club_time, R.string.event_type_reading, false),
        EventItem("🧘", R.string.event_mindfulness, R.string.event_mindfulness_time, R.string.event_type_meditation, false),
        EventItem("🎮", R.string.event_game_night, R.string.event_game_night_time, R.string.event_type_social, false),
        EventItem("💬", R.string.event_qa, R.string.event_qa_time, R.string.event_type_ama, false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.topic_events_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        events.forEach { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open event */ },
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = if (event.isLive)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(event.titleRes),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (event.isLive)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.isLive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = Color(0xFFEF4444)
                                ) {
                                    Text(
                                        text = stringResource(R.string.event_live),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(event.datetimeRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (event.isLive)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(event.typeRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (event.isLive)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!event.isLive) {
                        Button(
                            onClick = { /* RSVP */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(stringResource(R.string.event_rsvp), style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = { /* Join */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            )
                        ) {
                            Text(stringResource(R.string.event_join), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

private data class EventItem(
    val emoji: String,
    val titleRes: Int,
    val datetimeRes: Int,
    val typeRes: Int,
    val isLive: Boolean
)

@Composable
private fun MembersSection(topicName: String) {
    val members = listOf(
        MemberItem("CommunityMod", R.string.member_role_moderator, "https://i.pravatar.cc/150?u=mod1", true),
        MemberItem("HelpfulHelper", R.string.member_role_contributor, "https://i.pravatar.cc/150?u=helper", true),
        MemberItem("NewbieFriend", R.string.member_role_active, "https://i.pravatar.cc/150?u=newbie", false),
        MemberItem("WiseOwl", R.string.member_role_veteran, "https://i.pravatar.cc/150?u=owl", true),
        MemberItem("SupportiveSoul", R.string.member_role_member, "https://i.pravatar.cc/150?u=soul", false),
        MemberItem("CuriousCat", R.string.member_role_new, "https://i.pravatar.cc/150?u=cat", false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.topic_members_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* See all members */ }) {
                Text(stringResource(R.string.explore_see_all))
            }
        }

        // Online indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.explore_online_now, members.count { it.isOnline }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        members.forEach { member ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Open profile */ }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(
                        model = member.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    if (member.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.username,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(member.roleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = { /* Follow/Message */ },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(stringResource(R.string.member_follow), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private data class MemberItem(
    val username: String,
    val roleRes: Int,
    val avatarUrl: String,
    val isOnline: Boolean
)

package com.kyilmaz.neuronetworkingtitle

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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Production-ready data class for explore topic categories
 */
data class ExploreTopic(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
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
    val title: String,
    val categories: List<ExploreTopic>
)

// Production-ready explore topics with comprehensive categories
private val EXPLORE_CATEGORY_SECTIONS = listOf(
    ExploreCategorySection(
        title = "Daily Living",
        categories = listOf(
            ExploreTopic(
                id = "adhd_hacks",
                emoji = "🧠",
                name = "ADHD Hacks",
                description = "Productivity tips, time management, and life hacks for ADHD brains",
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
                name = "Safe Foods",
                description = "Share and discover sensory-friendly foods and eating strategies",
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
                name = "Sleep & Rest",
                description = "Tips for better sleep, rest routines, and managing fatigue",
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
                name = "Executive Function",
                description = "Strategies for planning, organizing, and task management",
                icon = Icons.Outlined.AccountTree,
                memberCount = 21500,
                postCount = 6800,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            )
        )
    ),
    ExploreCategorySection(
        title = "Sensory & Regulation",
        categories = listOf(
            ExploreTopic(
                id = "stimming",
                emoji = "✨",
                name = "Stimming",
                description = "Stim toys, movement, and self-regulation through stimming",
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
                name = "Sensory Tips",
                description = "Managing sensory sensitivities and creating sensory-friendly spaces",
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
                name = "Meltdown Support",
                description = "Prevention strategies, recovery tips, and peer support",
                icon = Icons.Outlined.Psychology,
                memberCount = 19800,
                postCount = 5400,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "sensory_diet",
                emoji = "⚖️",
                name = "Sensory Diet",
                description = "Building personalized sensory routines for regulation",
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
        title = "Social & Communication",
        categories = listOf(
            ExploreTopic(
                id = "social_skills",
                emoji = "🤝",
                name = "Social Skills",
                description = "Navigating social situations, making friends, and communication tips",
                icon = Icons.Outlined.Groups,
                memberCount = 35400,
                postCount = 10100,
                gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
                backgroundColor = Color(0xFFE0F7FA)
            ),
            ExploreTopic(
                id = "masking",
                emoji = "🎭",
                name = "Masking & Unmasking",
                description = "Experiences with masking, burnout, and authentic self-expression",
                icon = Icons.Outlined.TheaterComedy,
                memberCount = 27300,
                postCount = 7800,
                gradientColors = listOf(Color(0xFF84CC16), Color(0xFF65A30D)),
                backgroundColor = Color(0xFFF1F8E9)
            ),
            ExploreTopic(
                id = "relationships",
                emoji = "💕",
                name = "Relationships",
                description = "Dating, friendships, family dynamics, and communication in relationships",
                icon = Icons.Outlined.Favorite,
                memberCount = 29100,
                postCount = 8900,
                gradientColors = listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "communication_aac",
                emoji = "💬",
                name = "AAC & Communication",
                description = "Augmentative communication tools and non-verbal communication",
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
        title = "Work & Education",
        categories = listOf(
            ExploreTopic(
                id = "work_school",
                emoji = "💼",
                name = "Work & School",
                description = "Accommodations, advocacy, and thriving in academic and professional settings",
                icon = Icons.Outlined.School,
                memberCount = 42600,
                postCount = 14200,
                gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "disclosure",
                emoji = "📢",
                name = "Disclosure & Advocacy",
                description = "When and how to disclose, self-advocacy strategies",
                icon = Icons.Outlined.Campaign,
                memberCount = 18900,
                postCount = 5100,
                gradientColors = listOf(Color(0xFFA855F7), Color(0xFF9333EA)),
                backgroundColor = Color(0xFFF3E5F5)
            ),
            ExploreTopic(
                id = "career_paths",
                emoji = "📈",
                name = "Career Paths",
                description = "Finding neurodivergent-friendly careers and workplace success",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                memberCount = 23700,
                postCount = 6400,
                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                backgroundColor = Color(0xFFE8F5E9)
            ),
            ExploreTopic(
                id = "college_transition",
                emoji = "🏫",
                name = "College Transition",
                description = "Preparing for and navigating higher education",
                icon = Icons.Outlined.Apartment,
                memberCount = 16200,
                postCount = 4800,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                backgroundColor = Color(0xFFE3F2FD)
            )
        )
    ),
    ExploreCategorySection(
        title = "Special Interests & Hobbies",
        categories = listOf(
            ExploreTopic(
                id = "special_interests",
                emoji = "🎮",
                name = "Special Interests",
                description = "Share your passions and connect over deep interests",
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
                name = "Gaming",
                description = "Video games, board games, and gaming communities",
                icon = Icons.Outlined.SportsEsports,
                memberCount = 38400,
                postCount = 12100,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
                backgroundColor = Color(0xFFEDE7F6)
            ),
            ExploreTopic(
                id = "creative_arts",
                emoji = "🎨",
                name = "Creative Arts",
                description = "Art, music, writing, and creative expression",
                icon = Icons.Outlined.Palette,
                memberCount = 29800,
                postCount = 9400,
                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                backgroundColor = Color(0xFFFCE4EC)
            ),
            ExploreTopic(
                id = "tech_science",
                emoji = "💻",
                name = "Tech & Science",
                description = "Technology, programming, science, and research interests",
                icon = Icons.Outlined.Code,
                memberCount = 34100,
                postCount = 10800,
                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
                backgroundColor = Color(0xFFE0F7FA)
            )
        )
    ),
    ExploreCategorySection(
        title = "Health & Wellness",
        categories = listOf(
            ExploreTopic(
                id = "mental_health",
                emoji = "🧘",
                name = "Mental Health",
                description = "Anxiety, depression, and mental health support for neurodivergent folks",
                icon = Icons.Outlined.SelfImprovement,
                memberCount = 47800,
                postCount = 15600,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                backgroundColor = Color(0xFFE8F5E9)
            ),
            ExploreTopic(
                id = "therapy_resources",
                emoji = "🏥",
                name = "Therapy & Resources",
                description = "Finding ND-affirming therapists and helpful resources",
                icon = Icons.Outlined.MedicalServices,
                memberCount = 21400,
                postCount = 6200,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                backgroundColor = Color(0xFFE8EAF6)
            ),
            ExploreTopic(
                id = "medication",
                emoji = "💊",
                name = "Medication Experiences",
                description = "Sharing experiences with medications (not medical advice)",
                icon = Icons.Outlined.Medication,
                memberCount = 25600,
                postCount = 7100,
                gradientColors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48)),
                backgroundColor = Color(0xFFFFEBEE)
            ),
            ExploreTopic(
                id = "burnout_recovery",
                emoji = "🔋",
                name = "Burnout & Recovery",
                description = "Recognizing, preventing, and recovering from autistic burnout",
                icon = Icons.Outlined.BatteryAlert,
                memberCount = 31200,
                postCount = 8900,
                gradientColors = listOf(Color(0xFFEAB308), Color(0xFFCA8A04)),
                backgroundColor = Color(0xFFFFFDE7)
            )
        )
    ),
    ExploreCategorySection(
        title = "Life Stages",
        categories = listOf(
            ExploreTopic(
                id = "late_diagnosis",
                emoji = "🔍",
                name = "Late Diagnosis",
                description = "Support for those diagnosed as teens or adults",
                icon = Icons.Outlined.Explore,
                memberCount = 28700,
                postCount = 8200,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "parenting",
                emoji = "👨‍👩‍👧",
                name = "ND Parenting",
                description = "Neurodivergent parents and parenting ND children",
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
                name = "Independent Living",
                description = "Skills and strategies for living independently",
                icon = Icons.Outlined.Home,
                memberCount = 19400,
                postCount = 5500,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified
            ),
            ExploreTopic(
                id = "adulting",
                emoji = "✅",
                name = "Adulting 101",
                description = "Navigating adult life skills and responsibilities",
                icon = Icons.Outlined.Checklist,
                memberCount = 35800,
                postCount = 11400,
                gradientColors = listOf(Color.Unspecified, Color.Unspecified),
                backgroundColor = Color.Unspecified,
                isNew = true
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
    onCommentPost: (Post) -> Unit = {}
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header with status bar padding
        item(key = "explore_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Discover communities and resources",
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
                        text = "Featured",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {}) {
                        Text("See all")
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredTopics) { topic ->
                        FeaturedCategoryCard(
                            topic = topic,
                            onClick = { onTopicClick(topic.name) }
                        )
                    }
                }
            }
        }

        // Category Sections
        themedSections.forEach { section ->
            item(key = "section_header_${section.title}") {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            items(
                items = section.categories.chunked(2),
                key = { rowCategories -> "section_${section.title}_${rowCategories.map { it.id }.joinToString("_")}" }
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
                            onClick = { onTopicClick(topic.name) },
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
                "Suggested For You",
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
                                    text = "NEW",
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
                        text = topic.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = topic.description,
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
                            text = "${formatCount(topic.memberCount)} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${formatCount(topic.postCount)} posts",
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
                                text = "NEW",
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
                            contentDescription = "Trending",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatCount(topic.memberCount)} members",
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
    val shareText = buildString {
        append("Check out the ${topic.name} community on NeuroNet! ${topic.emoji}\n\n")
        append("${topic.description}\n\n")
        append("👥 ${formatCount(topic.memberCount)} members\n")
        append("📝 ${formatCount(topic.postCount)} posts\n\n")
        append("Join the conversation: neuronet.app/explore/${topic.id}")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "${topic.emoji} ${topic.name} - NeuroNet Community")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share ${topic.name}")
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
            text = { Text("Share") },
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
            text = { Text(if (isSaved) "Unsave" else "Save") },
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
            text = { Text("Not interested") },
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
            text = { Text("Report") },
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
                "Report ${topic.name}",
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

@Composable
private fun ExplorePostCard(
    post: Post,
    onCardClick: () -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onComment: () -> Unit = {},
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
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
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
            TopicPost("1", "FocusedFinn", "🧠 Just discovered the power of body doubling! Working alongside someone (even virtually) helps me stay on task so much better.", "2h ago", 234, 45, avatarUrl("finn")),
            TopicPost("2", "TimeBlindTina", "Anyone else use timers for EVERYTHING? I set 25-min work blocks and it's a game changer. Pomodoro for the win! 🍅", "4h ago", 189, 32, avatarUrl("tina")),
            TopicPost("3", "HyperfocusHarry", "TIP: Put your phone in another room during deep work. Out of sight = out of mind. Finally finishing projects!", "6h ago", 312, 56, avatarUrl("harry")),
            TopicPost("4", "ScatterbrainSally", "External scaffolding is my superpower! Visual reminders, apps, alarms - my environment remembers so I don't have to 📝", "8h ago", 167, 28, avatarUrl("sally")),
            TopicPost("5", "ImpulsiveIvy", "Started using 'implementation intentions' - instead of 'I'll exercise' I say 'At 7am, I put on shoes and walk'. Works! 🏃‍♀️", "12h ago", 245, 41, avatarUrl("ivy"))
        )
        "safe_foods", "safe foods" -> listOf(
            TopicPost("1", "TextureSensitive", "Found a new safe food! These rice crackers have the perfect crunch without being too loud. Sharing the brand in comments 🍘", "1h ago", 156, 89, avatarUrl("texture")),
            TopicPost("2", "BeigeFoodClub", "Reminder that 'safe foods' are valid! Sometimes plain pasta is self-care. No shame in eating what works for you 🍝", "3h ago", 423, 67, avatarUrl("beige")),
            TopicPost("3", "SensoryChef", "I made a sensory-friendly recipe book! All recipes avoid common texture triggers. Would anyone want me to share it?", "5h ago", 567, 123, avatarUrl("chef")),
            TopicPost("4", "PicyEaterPete", "Does anyone else have 'safe restaurants'? Places where you know exactly what to order and it's always consistent?", "7h ago", 234, 78, avatarUrl("pete")),
            TopicPost("5", "MealPrepMolly", "Batch cooking my safe foods on Sunday = less decision fatigue all week! Currently rotating 5 meals I actually enjoy 💪", "10h ago", 189, 45, avatarUrl("molly"))
        )
        "sleep_rest", "sleep & rest" -> listOf(
            TopicPost("1", "NightOwlNate", "Finally found a weighted blanket that doesn't make me overheat! Deep pressure + cool fabric = actually sleeping 😴", "45m ago", 345, 78, avatarUrl("nate")),
            TopicPost("2", "RestlessRachel", "Wind-down routine that works for my brain: dim lights, no screens 1hr before, same playlist every night. Consistency is key!", "2h ago", 278, 56, avatarUrl("rachel")),
            TopicPost("3", "InsomniaIan", "Anyone else's brain do the 'remember every embarrassing moment' thing at 2am? Started journaling before bed to 'close tabs'", "4h ago", 412, 89, avatarUrl("ian")),
            TopicPost("4", "SleepySteph", "White noise vs brown noise vs pink noise - which works for you? I'm team brown noise, it's like a hug for my ears 🔊", "6h ago", 234, 67, avatarUrl("steph")),
            TopicPost("5", "DreamyDan", "Started using blackout curtains AND a sleep mask. Totally dark room = finally getting deep sleep cycles!", "9h ago", 167, 34, avatarUrl("dan"))
        )
        "stimming" -> listOf(
            TopicPost("1", "StimSquad", "New stim toy review! This textured fidget ring is SO satisfying. Quiet enough for meetings, stimmy enough for regulation ✨", "30m ago", 456, 112, avatarUrl("stimsquad")),
            TopicPost("2", "HappyFlapper", "Normalize stimming in public! I was rocking at the grocery store and a kid said 'she's dancing!' Made my day 💃", "2h ago", 678, 145, avatarUrl("flapper")),
            TopicPost("3", "ChewySam", "For my fellow oral stimmers - found chewable necklaces that look like actual jewelry. Discreet AND functional!", "4h ago", 234, 67, avatarUrl("chewy")),
            TopicPost("4", "SpinnerSophie", "Movement is medicine! When I can't stim openly, I do toe-tapping under my desk. Subtle but still helps 🦶", "6h ago", 189, 45, avatarUrl("sophie")),
            TopicPost("5", "SensorySeeker", "Built a 'stim kit' for my bag: putty, textured cards, fidget cube. Always prepared for regulation needs!", "8h ago", 345, 78, avatarUrl("seeker"))
        )
        "sensory_tips" -> listOf(
            TopicPost("1", "OverwhelmOllie", "Loop earplugs CHANGED MY LIFE. Still hear conversations but takes the edge off sensory-overload environments 🎧", "1h ago", 567, 134, avatarUrl("ollie")),
            TopicPost("2", "LightSensitiveLuna", "Warm-toned light bulbs + dimmer switches made my home a safe haven. Harsh fluorescents are banned!", "3h ago", 345, 89, avatarUrl("luna")),
            TopicPost("3", "TagHaterTaylor", "Spent an hour removing all clothing tags today. Worth every second. Why do brands make them so scratchy?! 👕", "5h ago", 234, 78, avatarUrl("taylor")),
            TopicPost("4", "ScentSensitiveAlex", "Unscented everything is the way. Made a list of truly fragrance-free products, happy to share!", "7h ago", 189, 56, avatarUrl("alex")),
            TopicPost("5", "QuietQuinn", "Created a 'sensory map' of my city - quiet cafes, low-stim stores, accessible escape routes. Super helpful! 🗺️", "10h ago", 423, 97, avatarUrl("quinn"))
        )
        "meltdown_support" -> listOf(
            TopicPost("1", "RecoveryRen", "Reminder: Meltdowns aren't 'bad behavior' - they're nervous system overload. Be gentle with yourself after 💙", "2h ago", 678, 156, avatarUrl("ren")),
            TopicPost("2", "CopingCarla", "My meltdown kit: noise-canceling headphones, sunglasses, weighted lap pad, water, and a 'safe person' contact", "4h ago", 456, 123, avatarUrl("carla")),
            TopicPost("3", "PreventionPat", "Learning my warning signs: irritability spike, sound sensitivity increase, physical tension. Catching it early helps!", "6h ago", 345, 89, avatarUrl("pat")),
            TopicPost("4", "SafeSpaceSam", "Created a 'calm corner' in my home with dim lights, soft textures, and NO demands. Essential recovery zone 🏠", "8h ago", 234, 67, avatarUrl("sam")),
            TopicPost("5", "SupportiveSue", "Post-meltdown self-care: rest, hydration, comfort food, and absolutely NO self-criticism. Recovery takes time 🌸", "12h ago", 512, 134, avatarUrl("sue"))
        )
        "social_skills" -> listOf(
            TopicPost("1", "ConvoCoach", "Scripts for small talk! 'How's your day going?' → 'Any fun weekend plans?' → 'That sounds nice!' Memorized patterns help! 💬", "1h ago", 456, 112, avatarUrl("coach")),
            TopicPost("2", "FriendshipFred", "Quality over quantity. Having 2-3 close friends who 'get it' is worth more than 100 acquaintances 👥", "3h ago", 567, 134, avatarUrl("fred")),
            TopicPost("3", "AwkwardAndy", "It's okay to leave social events early! 'I had a great time, heading out now' - no long explanation needed 👋", "5h ago", 389, 89, avatarUrl("andy")),
            TopicPost("4", "BoundaryBella", "'I need to think about that' is a complete sentence. No pressure to respond immediately!", "7h ago", 423, 97, avatarUrl("bella")),
            TopicPost("5", "ParallelPaul", "Parallel play is valid adult friendship! Gaming together, crafting in the same room - connection without constant interaction", "10h ago", 345, 78, avatarUrl("paul"))
        )
        "masking" -> listOf(
            TopicPost("1", "UnmaskingUri", "Finally dropping the mask at home. It's exhausting pretending to be 'normal' all day. Safe spaces matter 🎭", "1h ago", 678, 167, avatarUrl("uri")),
            TopicPost("2", "BurnoutBeth", "Masking led to burnout. Now I'm slowly learning who I am without the performance. It's scary but freeing", "3h ago", 567, 145, avatarUrl("beth")),
            TopicPost("3", "AuthenticAva", "Found friends who accept the real me - stims, infodumps, and all. They say I'm 'more interesting' unmasked! ✨", "5h ago", 456, 123, avatarUrl("ava")),
            TopicPost("4", "ExhaustedEli", "The price of masking: migraines, fatigue, identity confusion. Is 'fitting in' worth my wellbeing? Reconsidering...", "7h ago", 389, 89, avatarUrl("eli")),
            TopicPost("5", "GrowingGrace", "Started stimming publicly. Heart raced at first, but literally no one cared. The fear was bigger than reality 💪", "10h ago", 423, 97, avatarUrl("grace"))
        )
        "special_interests" -> listOf(
            TopicPost("1", "DinoDave", "Three hours researching dinosaur feather evolution and I regret NOTHING! Who wants to hear about Archaeopteryx? 🦕", "30m ago", 567, 145, avatarUrl("dave")),
            TopicPost("2", "TrainTracker", "My special interest is train schedules. I can tell you every stop on 47 metro lines. Ask me anything! 🚂", "2h ago", 478, 234, avatarUrl("tracker")),
            TopicPost("3", "MushroomMike", "Fungi are INCREDIBLE. Did you know there's a mushroom that can break down plastic? Mycelium is the future! 🍄", "4h ago", 345, 89, avatarUrl("mike")),
            TopicPost("4", "LoreExplorer", "When your special interest has lore, you BECOME the wiki. 400 hours in this game and still learning! 🎮", "6h ago", 512, 123, avatarUrl("lore")),
            TopicPost("5", "StarStruck", "Space facts incoming! A day on Venus is longer than a year on Venus. Mind = blown every time I think about it ✨", "9h ago", 423, 97, avatarUrl("star"))
        )
        "mental_health" -> listOf(
            TopicPost("1", "AnxietyAlly", "ADHD + anxiety is a rough combo. Tips that help me: structured routines, movement breaks, and lots of self-compassion 🧘", "1h ago", 456, 123, avatarUrl("ally")),
            TopicPost("2", "DepressionDiana", "Some days, just existing is an achievement. Be proud of the small wins. You're doing better than you think 💙", "3h ago", 678, 178, avatarUrl("diana")),
            TopicPost("3", "TherapyTom", "Finally found a neurodivergent-affirming therapist! They understand that 'just try harder' isn't helpful. Game changer!", "5h ago", 345, 89, avatarUrl("tom")),
            TopicPost("4", "HealingHannah", "Journaling prompts for rough days: What's draining me? What's one kind thing I can do for myself right now?", "7h ago", 234, 67, avatarUrl("hannah")),
            TopicPost("5", "CopingChris", "Medication isn't a failure - it's a tool. Just like glasses for vision. No shame in chemical support 💊", "10h ago", 567, 145, avatarUrl("chris"))
        )
        "work_school" -> listOf(
            TopicPost("1", "AccommodationAce", "Got my workplace accommodations approved! Noise-canceling headphones, flexible hours, written instructions. Know your rights! 💼", "1h ago", 567, 145, avatarUrl("ace")),
            TopicPost("2", "StudentSara", "College disability services changed everything. Extra time, quiet testing rooms, note-taking support. Ask for help! 🎓", "3h ago", 423, 112, avatarUrl("sara")),
            TopicPost("3", "RemoteRick", "WFH is my dream. No fluorescent lights, no open offices, no surprise social interactions. Productivity skyrocketed!", "5h ago", 345, 89, avatarUrl("rick")),
            TopicPost("4", "MeetingMary", "Cameras off in meetings when I need them. Standing during calls. These 'small' accommodations make huge differences", "7h ago", 234, 67, avatarUrl("mary")),
            TopicPost("5", "CareerCoach", "Neurodivergent-friendly career paths: programming, research, art, writing, data analysis. Play to your strengths! 📈", "10h ago", 456, 123, avatarUrl("ccoach"))
        )
        "late_diagnosis" -> listOf(
            TopicPost("1", "LateBloomLisa", "Diagnosed at 35. Everything suddenly makes sense - the struggles, the differences, the 'quirks'. Grief and relief mixed 🔍", "1h ago", 678, 189, avatarUrl("lisa")),
            TopicPost("2", "MidlifeMike", "Spent decades thinking I was broken. Turns out I'm just wired differently. Late diagnosis is still valid diagnosis 💜", "3h ago", 567, 156, avatarUrl("midlife")),
            TopicPost("3", "IdentityCrisis", "Unmasking after 40 years is HARD. Who am I without the performance? Slowly rediscovering myself", "5h ago", 456, 123, avatarUrl("identity")),
            TopicPost("4", "GriefAndGrowth", "Grieving the support I could have had growing up. But also grateful for finally understanding myself 🌱", "7h ago", 389, 97, avatarUrl("grief")),
            TopicPost("5", "NewChapter", "It's never too late to learn about yourself. Late-diagnosed at 52, and life is finally making sense!", "10h ago", 512, 145, avatarUrl("chapter"))
        )
        else -> listOf(
            TopicPost("1", "CommunityMember", "Welcome to this community! Share your experiences and support each other. We're all in this together 💙", "1h ago", 234, 56, avatarUrl("community")),
            TopicPost("2", "HelpfulHelen", "Remember: everyone's journey is different. What works for one person might not work for another, and that's okay!", "3h ago", 189, 45, avatarUrl("helen")),
            TopicPost("3", "ResourceRon", "Great resources in the pinned post! Check them out if you're new here 📚", "5h ago", 156, 34, avatarUrl("ron")),
            TopicPost("4", "SupportiveSue", "This is a judgment-free zone. Ask questions, share struggles, celebrate wins. We've got you! 🌟", "7h ago", 278, 67, avatarUrl("sue")),
            TopicPost("5", "WelcomingWill", "New members: introduce yourself! What brings you here? What do you hope to learn or share?", "10h ago", 312, 89, avatarUrl("will"))
        )
    }
}

/**
 * Data class for topic-specific posts
 */
data class TopicPost(
    val id: String,
    val author: String,
    val content: String,
    val timeAgo: String,
    val likes: Int,
    val comments: Int,
    val avatarUrl: String
)

/**
 * Get topic details by name/ID
 */
fun getTopicByName(name: String): ExploreTopic? {
    return EXPLORE_TOPICS.find {
        it.name.equals(name, ignoreCase = true) ||
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
    val context = androidx.compose.ui.platform.LocalContext.current

    var isJoined by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Posts", "Resources", "Events", "Members")

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
                Text("Topic not found", style = MaterialTheme.typography.titleLarge)
            }
        }
        return
    }

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
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT,
                                "Check out the $topicName community on NeuroNet! 🧠✨ #${topicName.replace(" ", "")}")
                            type = "text/plain"
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(shareIntent, "Share Topic")
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "More options coming soon! ⚙️",
                            android.widget.Toast.LENGTH_SHORT
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
                                    text = topic.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${formatCount(topic.memberCount)} members • ${formatCount(topic.postCount)} posts",
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
                        text = topic.description,
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
                            text = if (isJoined) "Joined" else "Join Community",
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
                        ResourcesSection(topicName = topic.name)
                    }
                }
                2 -> {
                    // Events Tab
                    item {
                        EventsSection(topicName = topic.name)
                    }
                }
                3 -> {
                    // Members Tab
                    item {
                        MembersSection(topicName = topic.name)
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
                        text = post.author,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = post.timeAgo,
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
                text = post.content,
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
                        text = "Share",
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
        ResourceItem("📚", "Getting Started Guide", "Learn the basics and community guidelines", "Pinned"),
        ResourceItem("🔗", "Helpful Links Collection", "Curated external resources and tools", "Updated 2d ago"),
        ResourceItem("📝", "Community Wiki", "Collaboratively written knowledge base", "156 articles"),
        ResourceItem("🎥", "Video Library", "Educational videos and tutorials", "23 videos"),
        ResourceItem("📖", "Recommended Reading", "Books and articles recommended by members", "45 items")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Resources",
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
                            text = resource.title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = resource.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = resource.meta,
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
    val title: String,
    val description: String,
    val meta: String
)

@Composable
private fun EventsSection(topicName: String) {
    val events = listOf(
        EventItem("🎤", "Weekly Community Chat", "Every Saturday at 3pm EST", "Live Discussion", true),
        EventItem("📚", "Book Club Meeting", "Jan 5, 2026 at 7pm EST", "Reading Discussion", false),
        EventItem("🧘", "Mindfulness Session", "Jan 8, 2026 at 10am EST", "Guided Meditation", false),
        EventItem("🎮", "Game Night", "Jan 12, 2026 at 8pm EST", "Social Gaming", false),
        EventItem("💬", "Q&A with Expert", "Jan 15, 2026 at 6pm EST", "Ask Me Anything", false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Upcoming Events",
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
                                text = event.title,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (event.isLive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = Color(0xFFEF4444)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = event.datetime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = event.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!event.isLive) {
                        Button(
                            onClick = { /* RSVP */ },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("RSVP", style = MaterialTheme.typography.labelMedium)
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
                            Text("Join", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

private data class EventItem(
    val emoji: String,
    val title: String,
    val datetime: String,
    val type: String,
    val isLive: Boolean
)

@Composable
private fun MembersSection(topicName: String) {
    val members = listOf(
        MemberItem("CommunityMod", "Moderator", "https://i.pravatar.cc/150?u=mod1", true),
        MemberItem("HelpfulHelper", "Top Contributor", "https://i.pravatar.cc/150?u=helper", true),
        MemberItem("NewbieFriend", "Active Member", "https://i.pravatar.cc/150?u=newbie", false),
        MemberItem("WiseOwl", "Veteran", "https://i.pravatar.cc/150?u=owl", true),
        MemberItem("SupportiveSoul", "Member", "https://i.pravatar.cc/150?u=soul", false),
        MemberItem("CuriousCat", "New Member", "https://i.pravatar.cc/150?u=cat", false)
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
                text = "Community Members",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* See all members */ }) {
                Text("See All")
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
                text = "${members.count { it.isOnline }} online now",
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
                        text = member.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = { /* Follow/Message */ },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Follow", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private data class MemberItem(
    val username: String,
    val role: String,
    val avatarUrl: String,
    val isOnline: Boolean
)

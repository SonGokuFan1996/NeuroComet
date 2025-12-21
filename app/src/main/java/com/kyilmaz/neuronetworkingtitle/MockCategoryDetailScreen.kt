package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockCategoryDetailScreen(
    categoryName: String,
    onBack: () -> Unit,
    isQuietMode: Boolean = false
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    // Generate mock posts based on category
    val mockPosts = remember(categoryName) {
        generateMockPostsForCategory(categoryName)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        categoryName, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            // Mock Banner / Resource Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Community Resources",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Tap to view curated guides for $categoryName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(mockPosts) { post ->
                BubblyPostCard(
                    post = post,
                    isQuietMode = isQuietMode,
                    isAutoVideoPlayback = false,
                    onLike = {},
                    onDelete = {},
                    onReplyClick = {},
                    onShare = {}
                )
            }
        }
    }
}

private fun generateMockPostsForCategory(category: String): List<Post> {
    return when (category) {
        "ADHD Hacks" -> listOf(
            Post(
                id = 101,
                userId = "NeuroHacker",
                userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=NeuroHacker",
                content = "Body doubling saved my thesis! Just having someone on zoom while I work made all the difference.",
                tone = "/gen",
                likes = 1242,
                community = "r/ADHD",
                createdAt = "2h ago"
            ),
            Post(
                id = 102,
                userId = "DopamineMiner",
                userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Dopamine",
                content = "Tip: Keep a 'doom box' for cleaning. Throw everything in a box to sort later, just clear the surfaces now!",
                tone = "/pos",
                likes = 853,
                community = "r/CleaningTips",
                createdAt = "5h ago"
            ),
             Post(
                id = 103,
                userId = "TimeBlindness",
                content = "Does anyone else set alarms for every step of their morning routine? Shower: 7:00, Dry off: 7:15, Dress: 7:20...",
                tone = "/gen",
                likes = 2300,
                community = "r/ADHD",
                createdAt = "1d ago"
            )
        )
        "Safe Foods" -> listOf(
            Post(
                id = 201,
                userId = "TexturePerson",
                content = "Mac and Cheese is the ultimate safe food. Consistent texture every time.",
                tone = "/srs",
                likes = 5000,
                imageUrl = "https://picsum.photos/seed/macncheese/400/300",
                community = "r/ARFID",
                createdAt = "10m ago"
            ),
            Post(
                id = 202,
                userId = "NuggetLover",
                content = "Dino nuggets simply taste better than regular shapes. It's science.",
                tone = "/j",
                likes = 342,
                community = "r/SafeFoods",
                createdAt = "3h ago"
            )
        )
        "Stimming" -> listOf(
             Post(
                id = 301,
                userId = "FidgetSpinner99",
                content = "Just got this new infinity cube and it's so satisfying.",
                tone = "/happy",
                likes = 89,
                videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4", // Mock video
                community = "r/Stimming",
                createdAt = "1h ago"
            ),
            Post(
                id = 302,
                userId = "RockingChair",
                content = "Visual stims >> anyone else love watching lava lamps for hours?",
                tone = "/gen",
                likes = 404,
                community = "r/Autism",
                createdAt = "4h ago"
            )
        )
        else -> listOf(
            Post(
                id = 999,
                userId = "MockUser",
                content = "This is a mock post for the category: $category. Explore and enjoy!",
                tone = "/test",
                likes = 42,
                community = "r/$category",
                createdAt = "Now"
            )
        )
    }
}

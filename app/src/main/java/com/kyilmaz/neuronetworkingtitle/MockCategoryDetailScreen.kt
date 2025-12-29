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
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.kyilmaz.neuronetworkingtitle.Post
import com.kyilmaz.neuronetworkingtitle.BubblyPostCard // Assuming this is defined in MainActivity.kt or a common composables file
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockCategoryDetailScreen(
    categoryName: String,
    onBack: () -> Unit,
    isQuietMode: Boolean = false,
    onSharePost: (Context, Post) -> Unit = { _, _ -> } // MODIFIED: onSharePost now accepts Context and Post
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current // Added back the context variable
    
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
                    onShare = { onSharePost(context, post) } // FIXED: passed a () -> Unit lambda that uses captured context and post
                )
            }
        }
    }
}

private fun generateMockPostsForCategory(category: String): List<Post> {
    val userAvatarUrl = { seed: String -> "https://api.dicebear.com/7.x/avataaars/svg?seed=$seed" }
    return when (category) {
        "ADHD Hacks" -> listOf(
            Post(
                id = 101L,
                userId = "NeuroHacker",
                userAvatar = userAvatarUrl("NeuroHacker"),
                content = "Body doubling saved my thesis! Just having someone on zoom while I work made all the difference.",
                likes = 1242,
                comments = 56,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            ),
            Post(
                id = 102L,
                userId = "DopamineMiner",
                userAvatar = userAvatarUrl("Dopamine"),
                content = "Tip: Keep a 'doom box' for cleaning. Throw everything in a box to sort later, just clear the surfaces now!",
                likes = 853,
                comments = 30,
                shares = 8,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            ),
             Post(
                id = 103L,
                userId = "TimeBlindness",
                userAvatar = userAvatarUrl("TimeBlindness"),
                content = "Does anyone else set alarms for every step of their morning routine? Shower: 7:00, Dry off: 7:15, Dress: 7:20...",
                likes = 2300,
                comments = 150,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            )
        )
        "Safe Foods" -> listOf(
            Post(
                id = 201L,
                userId = "TexturePerson",
                userAvatar = userAvatarUrl("TexturePerson"),
                content = "Mac and Cheese is the ultimate safe food. Consistent texture every time.",
                likes = 5000,
                comments = 700,
                shares = 200,
                imageUrl = "https://picsum.photos/seed/macncheese/400/300",
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            ),
            Post(
                id = 202L,
                userId = "NuggetLover",
                userAvatar = userAvatarUrl("NuggetLover"),
                content = "Dino nuggets simply taste better than regular shapes. It's science.",
                likes = 342,
                comments = 50,
                shares = 10,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            )
        )
        "Stimming" -> listOf(
             Post(
                id = 301L,
                userId = "FidgetSpinner99",
                userAvatar = userAvatarUrl("FidgetSpinner99"),
                content = "Just got this new infinity cube and it's so satisfying.",
                likes = 89,
                comments = 10,
                shares = 5,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            ),
            Post(
                id = 302L,
                userId = "RockingChair",
                userAvatar = userAvatarUrl("RockingChair"),
                content = "Visual stims >> anyone else love watching lava lamps for hours?",
                likes = 404,
                comments = 60,
                shares = 15,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            )
        )
        else -> listOf(
            Post(
                id = 999L,
                userId = "MockUser",
                userAvatar = userAvatarUrl("MockUser"),
                content = "This is a mock post for the category: $category. Explore and enjoy!",
                likes = 42,
                comments = 5,
                shares = 2,
                createdAt = Instant.now().toString(),
                isLikedByMe = false
            )
        )
    }
}
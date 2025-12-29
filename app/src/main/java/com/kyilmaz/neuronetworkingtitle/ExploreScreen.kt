package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kyilmaz.neuronetworkingtitle.ui.theme.NeuroNetWorkingTitleTheme
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Helper function to format Instant to a human-readable "time ago" string.
fun getPostTimeAgo(createdAt: String?): String {
    return try {
        val instant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(createdAt ?: Instant.now().toString()))
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            minutes < 60 * 24 -> "${ChronoUnit.HOURS.between(instant, now)} hours ago"
            minutes < 60 * 24 * 7 -> "${ChronoUnit.DAYS.between(instant, now)} days ago"
            else -> "${ChronoUnit.WEEKS.between(instant, now)} weeks ago"
        }
    } catch (_: Exception) {
        createdAt ?: "Unknown time"
    }
}

@Composable
fun PostCard(
    post: Post,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    safetyState: SafetyState = SafetyState()
) {
    // We use a local state for mocking interactivity in the preview
    var isLiked by remember { mutableStateOf(post.isLikedByMe) }
    var likesCount by remember { mutableStateOf(post.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // ADDED: subtle elevation for depth
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User Info and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mock Avatar
                Image(
                    painter = rememberAsyncImagePainter(post.userAvatar ?: "https://example.com/mock_avatar.png"),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        // Since Post data class doesn't have an authorName, we use userId as a placeholder
                        text = "User ${post.userId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getPostTimeAgo(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            val shouldHide = safetyState.isKidsMode && ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)
            if (shouldHide) {
                Text(
                    text = "Content hidden for kids mode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val contentText = if (safetyState.isKidsMode) {
                    ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel)
                } else post.content

                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val showMedia = !safetyState.isKidsMode
            if (showMedia && post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                // Mock Image Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Text(
                        "Image Mock",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: Like, Comment, Share
            val interactionsEnabled = !safetyState.isKidsMode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .clickable(enabled = interactionsEnabled) {
                            isLiked = !isLiked
                            likesCount += if (isLiked) 1 else -1
                            onLikeToggle()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = likesCount.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Comment Button
                Row(
                    modifier = Modifier.clickable(enabled = interactionsEnabled) { onCommentClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.comments.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Share Button
                Row(
                    modifier = Modifier.clickable(enabled = interactionsEnabled) { onShareClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.shares.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreScreen(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    safetyState: SafetyState = SafetyState()
) {
    val mockCategories = listOf("ADHD", "Anxiety", "Depression", "Bipolar", "Autism", "OCD")
    var searchText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            "Explore NeuroNet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Posts and Users") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                // Border colors are kept transparent as per the original clean design
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            )
        )

        // Category Chips
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockCategories) { category ->
                AssistChip(
                    onClick = { /* Handle category selection */ },
                    label = { Text(category) },
                    border = null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Trending Posts List Header
        Text(
            "Trending Posts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        )

        // Posts List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(posts, key = { it.id ?: "post-${it.content}" }) { post ->
                PostCard(
                    post = post,
                    onLikeToggle = { /* Handle actual like state change */ },
                    onCommentClick = { /* Handle navigation to comments */ },
                    onShareClick = { /* Handle share action */ },
                    safetyState = safetyState
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    val mockPosts = listOf(
        Post(
            id = 1L,
            createdAt = Instant.now().minus(1, ChronoUnit.HOURS).toString(),
            content = "Just had a breakthrough on my project! Hyperfocus is a superpower when you can direct it. Anyone else find music essential for flow state?",
            userId = "NeuroThinker",
            likes = 125,
            comments = 18,
            shares = 5,
            isLikedByMe = true,
            imageUrl = "https://example.com/image1.jpg"
        ),
        Post(
            id = 2L,
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS).toString(),
            content = "Trying out some new grounding techniques. The 5-4-3-2-1 method is a lifesaver when I start feeling overstimulated. What are your go-to strategies for sensory calm?",
            userId = "Calm_Sensation",
            likes = 302,
            comments = 45,
            shares = 12,
            isLikedByMe = false
        ),
        Post(
            id = 3L,
            createdAt = Instant.now().minus(30, ChronoUnit.MINUTES).toString(),
            content = "Reminder: Progress is not linear. Be kind to yourself today. You are doing great.",
            userId = "AdminBot",
            likes = 999,
            comments = 0,
            shares = 200,
            isLikedByMe = false
        )
    )

    NeuroNetWorkingTitleTheme {
        ExploreScreen(posts = mockPosts)
    }
}
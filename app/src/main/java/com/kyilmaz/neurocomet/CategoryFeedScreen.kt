package com.kyilmaz.neurocomet

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFeedScreen(
    categoryName: String, // Maps to 'community' in Post model
    onBack: () -> Unit
) {
    // Placeholder posts for initial implementation/preview. 
    val posts = createMockPosts(categoryName).take(if (categoryName == "Neurobiology") 5 else 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (posts.isEmpty()) {
            EmptyFeedMessage(categoryName, Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Use post.id as the key, but fall back to a hash code if it's null
                items(posts, key = { it.id ?: it.hashCode() }) { post ->
                    PostCard(post = post)
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Post Image (if available)
            post.imageUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(model = url),
                    contentDescription = "Post image", 
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Title: Using Community name and ID as a title placeholder
            Text(
                text = "Post #${post.id ?: "Unknown"}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Metadata: User and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time and User
                Text(
                    text = "u/${post.userId ?: "Anonymous"} • ${formatTimestamp(post.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Snippet
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Likes and Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Likes
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Likes",
                    tint = if (post.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likes}",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Comments
                // Using a placeholder icon for comments (assuming a comment bubble icon is not available by default)
                Icon(
                    Icons.Default.ThumbUp, 
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.comments}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun EmptyFeedMessage(categoryName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No posts found in '$categoryName'",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Be the first to share an insight!",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Utility function to create mock posts that align with the DataModels.Post structure.
 */
fun createMockPosts(communityName: String): List<Post> {
    return (1..10).map {
        Post(
            id = it.toLong(),
            userId = "user${it % 3}",
            content = "This is the content of the post. It provides detailed information and discussion points about $communityName. This post is post number $it in the mock data.",
            imageUrl = if (it % 5 == 0) "https://via.placeholder.com/600x400.png?text=Image+for+Post+$it" else null,
            // Mock created_at as an ISO string (Supabase/Postgrest default)
            createdAt = "2024-07-25T${it}2:00:00.000Z", 
            likes = 100 - it,
            comments = 5 + it,
            shares = 0,
            isLikedByMe = it % 2 == 0
        )
    }
}

/**
 * Utility function to format the ISO timestamp string into a readable date string.
 * For simplicity, converts "YYYY-MM-DDTHH..." to "MMM dd, yyyy".
 */
fun formatTimestamp(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return "Unknown time"
    
    return try {
        val datePart = isoString.substringBefore("T")
        // Input format expected to be "YYYY-MM-DD"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(datePart)
        
        // Output format "MMM dd, yyyy"
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        // Fallback in case of parsing error
        isoString.substringBefore("T")
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCategoryFeedScreen() {
    NeuroThemeApplication(themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        CategoryFeedScreen(categoryName = "Neurobiology", onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryFeedScreenEmpty() {
    NeuroThemeApplication(themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        CategoryFeedScreen(categoryName = "Quantum Physics", onBack = {})
    }
}

@Preview
@Composable
fun PreviewPostCard() {
    NeuroThemeApplication(themeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
        PostCard(
            post = Post(
                id = 1,
                userId = "bkyil",
                content = "Recent studies suggest that astrocytes play a far more active role in modulating synaptic strength than previously thought. This opens up new avenues for targeting neurological disorders. This text is long enough to test the ellipsis overflow.",
                imageUrl = "https://via.placeholder.com/600x400.png?text=Preview+Image",
                createdAt = "2024-07-24T10:30:00.000Z",
                likes = 452,
                comments = 12,
                shares = 0,
                isLikedByMe = true
            )
        )
    }
}

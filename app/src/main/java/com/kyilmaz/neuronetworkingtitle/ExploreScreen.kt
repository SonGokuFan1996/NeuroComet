package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Explicit imports for symbols in the same package
import androidx.compose.foundation.layout.PaddingValues 
import com.kyilmaz.neuronetworkingtitle.Post
import com.kyilmaz.neuronetworkingtitle.SafetyState

// Placeholder topics to satisfy the original UI layout
private val MOCK_EXPLORE_TOPICS = listOf(
    "Hyperfocus Hacks" to Color(0xFFFEE2E2), // Red 100
    "Sensory-Friendly Spaces" to Color(0xFFD1FAE5), // Teal 100
    "Executive Function Tools" to Color(0xFFEFF6FF), // Blue 100
    "Stimming & Fidgets" to Color(0xFFFEF3C7), // Yellow 100
)

@Composable
fun ExploreScreen(
    posts: List<Post>,
    safetyState: SafetyState, // Added from calling site
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {}
) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)) {
        item {
            Text("Explore Topics", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Topics Grid
        item {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(400.dp) // Fixed height to avoid inner scrolling conflict
            ) {
                items(MOCK_EXPLORE_TOPICS) { (topic, color) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = color),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(if (topic.length > 12) 200.dp else 140.dp)
                            .clickable { onTopicClick(topic) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                topic,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("Suggested Posts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Suggested Posts List (using MOCK_EXPLORE_POSTS)
        items(posts) { post ->
            // Re-using the BubblyPostCard from FeedScreen (assuming it's available in the package)
            Text("Post: ${post.content}", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
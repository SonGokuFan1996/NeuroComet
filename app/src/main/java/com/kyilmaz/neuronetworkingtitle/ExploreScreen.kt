package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// NOTE: EXPLORE_TOPICS must be imported from MainActivity or defined here if it's not a shared asset.
// Since it's a mock asset in MainActivity, we will assume it is an argument here for modularity.

@Composable
fun ExploreScreen(
    exploreTopics: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
    onTopicClick: (String) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Explore Topics", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(exploreTopics) { (topic, color) ->
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
    }
}
package com.kyilmaz.neurocomet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyilmaz.neurocomet.ui.theme.NeuroCometWorkingTitleTheme

@Composable
fun PlayStoreScreenshotFrame(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF0F172A)  // Slate 900
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = title,
                color = Color.White,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = subtitle,
                color = Color(0xFFCBD5E1), // Slate 300
                fontSize = 17.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Outer device bezel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.Black)
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
                    // Inner screen
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}

// 1080x1920 aspect ratio is 9:16
// We use a dp size that matches the aspect ratio. 
// For example, 540x960.
@Preview(name = "Play Store Screenshot - Feed", widthDp = 540, heightDp = 960)
@Composable
fun PlayStoreScreenshotFeed() {
    NeuroCometWorkingTitleTheme {
        PlayStoreScreenshotFrame(
            title = "A Calmer Way to Connect",
            subtitle = "Sensory-friendly social media built for neurodivergent minds.",
            content = {
                FeedScreen(
                    feedUiState = FeedUiState(
                        posts = MOCK_FEED_POSTS,
                        stories = MOCK_STORIES
                    ),
                    onLikePost = {},
                    onReplyPost = {},
                    onSharePost = { _, _ -> },
                    onAddPost = { _, _, _, _, _, _ -> },
                    onDeletePost = {},
                    onProfileClick = {},
                    onViewStory = {},
                    onAddStory = { _, _, _, _, _, _, _ -> },
                    isPremium = false,
                    onUpgradeClick = {},
                    isMockInterfaceEnabled = true,
                    enableNewFeedLayout = true
                )
            }
        )
    }
}

@Preview(name = "Play Store Screenshot - Explore", widthDp = 540, heightDp = 960)
@Composable
fun PlayStoreScreenshotExplore() {
    NeuroCometWorkingTitleTheme {
        PlayStoreScreenshotFrame(
            title = "Explore Safely",
            subtitle = "Find topics you love without the noise.",
            content = {
                ExploreScreen(
                    posts = MOCK_FEED_POSTS,
                    onLikePost = {},
                    onSharePost = { _, _ -> },
                    onCommentPost = {},
                    onTopicClick = {},
                    onProfileClick = {}
                )
            }
        )
    }
}

@Preview(name = "Play Store Screenshot - Messages", widthDp = 540, heightDp = 960)
@Composable
fun PlayStoreScreenshotMessages() {
    NeuroCometWorkingTitleTheme {
        PlayStoreScreenshotFrame(
            title = "Meaningful Connections",
            subtitle = "Connect through text, voice, or practice calls with AI.",
            content = {
                NeuroInboxScreen(
                    conversations = MOCK_CONVERSATIONS,
                    safetyState = SafetyState(),
                    onOpenConversation = {},
                    onStartNewChat = {},
                    onOpenCallHistory = {},
                    onOpenPracticeCall = {}
                )
            }
        )
    }
}

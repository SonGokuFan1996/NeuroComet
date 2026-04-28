package com.kyilmaz.neurocomet

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Minimal destination for deep links of the form
 * `https://getneurocomet.com/post/{postId}`.
 *
 * Tries to locate the post in the already-loaded feed state; if it's not
 * present, triggers a refresh and shows a loading placeholder. On failure
 * or missing post, displays a friendly "post not found" message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    feedUiState: FeedUiState,
    feedViewModel: FeedViewModel,
    safetyState: SafetyState,
    currentUserId: String,
    isMockInterfaceEnabled: Boolean,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onReplyPost: (Post) -> Unit = {},
    onSharePost: (android.content.Context, Post) -> Unit = { _, _ -> },
) {
    val post = feedUiState.posts.firstOrNull { it.id == postId }

    // If we don't have the post in state yet, kick off a refresh once.
    LaunchedEffect(postId) {
        if (post == null) {
            feedViewModel.fetchPosts()
        }
    }

    // Predictive Back (Android 14+). Shows the user how much they've swiped
    // by subtly scaling + fading the PostDetail surface, so they can commit
    // or cancel with full gesture feedback — less jarring for users with
    // sensory sensitivities than the stock instant dismissal.
    var backProgress by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { event ->
                backProgress = event.progress
            }
            // Gesture completed — navigate back.
            backProgress = 0f
            onBack()
        } catch (_: kotlinx.coroutines.CancellationException) {
            // Gesture cancelled — snap back.
            backProgress = 0f
        }
    }

    Scaffold(
        modifier = Modifier.graphicsLayer {
            val scale = 1f - (backProgress * 0.08f)
            scaleX = scale
            scaleY = scale
            alpha = 1f - (backProgress * 0.35f)
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_post)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                post != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BubblyPostCard(
                            post = post,
                            onLike = { post.id?.let { feedViewModel.toggleLike(it) } },
                            onDelete = { post.id?.let { feedViewModel.deletePost(it) } },
                            onReplyPost = { onReplyPost(post) },
                            onShare = onSharePost,
                            isMockInterfaceEnabled = isMockInterfaceEnabled,
                            safetyState = safetyState,
                            currentUserId = currentUserId,
                            onProfileClick = onProfileClick,
                            onHashtagClick = onHashtagClick,
                            bookmarkedPostIds = feedUiState.bookmarkedPostIds,
                            onBookmarkToggle = { feedViewModel.toggleBookmark(it) },
                            followingUserIds = feedUiState.followingUserIds,
                            onFollowToggle = { feedViewModel.toggleFollow(it) }
                        )
                    }
                }
                feedUiState.isLoading -> {
                    Column(
                        modifier = Modifier.padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading post…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Post not found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "This post may have been removed or is no longer available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


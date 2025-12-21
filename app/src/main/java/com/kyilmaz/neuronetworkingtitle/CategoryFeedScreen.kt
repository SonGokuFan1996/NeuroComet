package com.kyilmaz.neuronetworkingtitle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFeedScreen(
    categoryName: String,
    posts: List<Post>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onLikePost: (Long) -> Unit,
    onDeletePost: (Long) -> Unit,
    bottomPadding: Dp
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        categoryName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }) { inner ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (posts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SearchOff, null, Modifier.size(64.dp))
                    Text("No posts here yet!")
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + 16.dp,
                    bottom = bottomPadding,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = posts, key = { it.id ?: 0 }) { post ->
                    BubblyPostCard(
                        post = post,
                        isQuietMode = false,
                        isAutoVideoPlayback = false,
                        onLike = { onLikePost(post.id ?: 0) },
                        onDelete = { onDeletePost(post.id ?: 0) },
                        onReplyClick = {},
                        onShare = {}
                    )
                }
            }
        }
    }
}

@file:Suppress(
    "AssignedValueIsNeverRead",
    "AssignmentToStateVariable",
    "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE",
    "UNUSED_VALUE"
)

package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel()

@Composable
fun StoryScreen(
    story: Story,
    onDismiss: () -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    var currentItemIndex by remember { mutableStateOf(0) }
    var rawProgress by remember { mutableStateOf(0f) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val currentItem = story.items[currentItemIndex]

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 120),
        label = "storyProgressAnimation"
    )

    fun navigateTo(index: Int) {
        if (index in story.items.indices) {
            currentItemIndex = index
            rawProgress = 0f
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(currentItemIndex) {
        rawProgress = 0f
        val startTime = System.currentTimeMillis()
        val durationMs = currentItem.duration.coerceAtLeast(1L)
        while (System.currentTimeMillis() < startTime + durationMs) {
            rawProgress = ((System.currentTimeMillis() - startTime).toFloat() / durationMs).coerceIn(0f, 1f)
            delay(50)
        }
        navigateTo(currentItemIndex + 1)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures {
                        offset ->
                        if (offset.x > size.width / 2) {
                            navigateTo(currentItemIndex + 1)
                        } else {
                            navigateTo(currentItemIndex - 1)
                        }
                    }
                }
        ) {
            AsyncImage(
                model = currentItem.imageUrl,
                contentDescription = stringResource(R.string.story_content_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top bar with user info, progress bars, and close button
            Column(modifier = Modifier.fillMaxWidth()) {
                // Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Replace LinearProgressIndicator with a simple, non-deprecated custom bar.
                    story.items.forEachIndexed { index, _ ->
                        val itemProgress = when {
                            index < currentItemIndex -> 1f
                            index == currentItemIndex -> animatedProgress
                            else -> 0f
                        }
                        StoryProgressBar(
                            progress = itemProgress,
                            modifier = Modifier.weight(1f),
                            barColor = Color.White,
                            trackColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }

                // User Info and Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = story.userAvatar,
                        contentDescription = stringResource(R.string.story_user_story_content_description, story.userName),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = story.userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))

                    // Delete button (only for current user's stories)
                    if (story.userName == CURRENT_USER.name) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.story_delete_button_content_description), tint = Color.White)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.story_close_button_content_description), tint = Color.White)
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(stringResource(R.string.story_delete_dialog_title)) },
                text = { Text(stringResource(R.string.story_delete_dialog_text)) },
                confirmButton = { 
                    Button(onClick = {
                        // If deleteStory is implemented in the ViewModel, prefer it; otherwise just dismiss.
                        runCatching { feedViewModel.deleteStory(story.id) }
                        showDeleteConfirmation = false
                        onDismiss()
                    }) { Text(stringResource(R.string.story_delete_dialog_confirm)) }
                },
                dismissButton = { 
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.story_delete_dialog_cancel)) } 
                }
            )
        }
    }
}

@Composable
private fun StoryProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color,
    trackColor: Color
) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(100))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(barColor)
        )
    }
}

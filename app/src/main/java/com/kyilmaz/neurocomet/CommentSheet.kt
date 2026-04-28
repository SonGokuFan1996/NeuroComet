package com.kyilmaz.neurocomet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant

/**
 * A bottom sheet for displaying and adding comments to a post.
 *
 * @param isVisible Whether the sheet is visible
 * @param comments List of comments to display
 * @param onDismiss Callback when the sheet is dismissed
 * @param onAddComment Callback when a new comment is submitted
 * @param postAuthor The author of the post being commented on
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    isVisible: Boolean,
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    postAuthor: String? = null,
    draftText: String = "",
    onDraftChange: (String) -> Unit = {}
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val brailleOptimized = SocialSettingsManager.isBrailleOptimized(context)

    // ── Unified Liquid Glass A/B experiment ────────────────
    val liquidGlassVariant = remember(context) {
        ABTestManager.getVariant(context, ABExperiment.LIQUID_GLASS)
    }
    val useGlass = liquidGlassVariant != "control"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (useGlass) Color.Transparent else MaterialTheme.colorScheme.surface,
        scrimColor = if (useGlass) Color.Black.copy(alpha = 0.32f) else BottomSheetDefaults.ScrimColor,
        dragHandle = if (useGlass) {
            // Glass sheet has its own drag handle inside LiquidGlassSheetContent
            null
        } else {
            {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    ) {
        if (useGlass) {
            LiquidGlassSheetContent(variant = liquidGlassVariant) {
                CommentSheetBody(
                    comments = comments,
                    onAddComment = onAddComment,
                    onDismiss = onDismiss,
                    postAuthor = postAuthor,
                    draftText = draftText,
                    onDraftChange = onDraftChange,
                    brailleOptimized = brailleOptimized,
                    variant = liquidGlassVariant
                )
            }
        } else {
            CommentSheetBody(
                comments = comments,
                onAddComment = onAddComment,
                onDismiss = onDismiss,
                postAuthor = postAuthor,
                draftText = draftText,
                onDraftChange = onDraftChange,
                brailleOptimized = brailleOptimized,
                variant = "control"
            )
        }
    }
}

/**
 * Inner body content of the comment sheet, extracted to avoid duplication
 * between glass and non-glass rendering paths.
 */
@Composable
private fun CommentSheetBody(
    comments: List<Comment>,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit,
    postAuthor: String?,
    draftText: String,
    onDraftChange: (String) -> Unit,
    brailleOptimized: Boolean,
    variant: String
) {
    val useSkeuomorphic = isSkeumorphicVariant(variant)
    val palette = if (useSkeuomorphic) rememberSkeuomorphicPalette(variant) else null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.comments_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (useSkeuomorphic && palette != null) {
                    Text(
                        text = if (isFullSkeumorphicVariant(variant)) "Richer depth for slower, calmer replies" else "Soft depth for thoughtful replies",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.accent.copy(alpha = 0.85f)
                    )
                }
            }
            if (useSkeuomorphic) {
                SkeuomorphicPanel(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable(onClick = onDismiss),
                    shape = CircleShape,
                    variant = variant
                ) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.comments_close),
                            tint = palette?.accent ?: MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.comments_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (postAuthor != null) {
            if (useSkeuomorphic) {
                SkeuomorphicPanel(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    variant = variant
                ) {
                    Text(
                        text = stringResource(R.string.comments_replying_to, postAuthor),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.comments_replying_to, postAuthor),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Comments list
        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (useSkeuomorphic) {
                        SkeuomorphicPanel(
                            modifier = Modifier.size(88.dp),
                            shape = RoundedCornerShape(28.dp),
                            variant = variant
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = palette?.accent ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Text(
                        text = stringResource(R.string.comments_no_comments_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.comments_be_first),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    CommentItem(comment = comment, variant = variant)
                }
            }
        }

        // Comment input
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (useSkeuomorphic) {
                SkeuomorphicPanel(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    variant = variant
                ) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = onDraftChange,
                        placeholder = {
                            Text(
                                stringResource(R.string.comments_add_comment_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = if (brailleOptimized) "Comment input" else "Add a comment"
                                stateDescription = if (draftText.isBlank()) "Empty" else "${draftText.length} characters entered"
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
                SkeuomorphicPanel(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = draftText.isNotBlank()) {
                            val trimmedComment = draftText.trim()
                            if (trimmedComment.isNotEmpty()) {
                                onAddComment(trimmedComment)
                                onDraftChange("")
                            }
                        },
                    shape = CircleShape,
                    variant = variant
                ) {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().semantics {
                        contentDescription = if (draftText.isBlank()) "Send comment disabled" else "Send comment"
                    }, contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.comments_send_comment),
                            tint = if (draftText.isNotBlank()) palette?.accent ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = draftText,
                    onValueChange = onDraftChange,
                    placeholder = {
                        Text(
                            stringResource(R.string.comments_add_comment_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = if (brailleOptimized) "Comment input" else "Add a comment"
                            stateDescription = if (draftText.isBlank()) "Empty" else "${draftText.length} characters entered"
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
                IconButton(
                    modifier = Modifier.semantics {
                        contentDescription = if (draftText.isBlank()) "Send comment disabled" else "Send comment"
                    },
                    onClick = {
                        val trimmedComment = draftText.trim()
                        if (trimmedComment.isNotEmpty()) {
                            onAddComment(trimmedComment)
                            onDraftChange("")
                        }
                    },
                    enabled = draftText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.comments_send_comment),
                        tint = if (draftText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * A single comment item displaying user avatar, name, content, and timestamp.
 */
@Composable
private fun CommentItem(comment: Comment, variant: String = "control") {
    val timeAgo = remember(comment.timestamp) {
        try {
            val commentTime = Instant.parse(comment.timestamp)
            val now = Instant.now()
            val diff = Duration.between(commentTime, now)
            when {
                diff.toDays() >= 365 -> "${diff.toDays() / 365}y"
                diff.toDays() >= 30 -> "${diff.toDays() / 30}mo"
                diff.toDays() >= 7 -> "${diff.toDays() / 7}w"
                diff.toDays() > 0 -> "${diff.toDays()}d"
                diff.toHours() > 0 -> "${diff.toHours()}h"
                diff.toMinutes() > 0 -> "${diff.toMinutes()}m"
                else -> "now"
            }
        } catch (_: Exception) {
            ""
        }
    }

    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = "${comment.userId}, ${comment.content}, $timeAgo"
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = comment.userAvatar.ifEmpty { avatarUrl(comment.userId) },
                contentDescription = stringResource(R.string.cd_user_avatar),
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = comment.userId,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    if (isSkeumorphicVariant(variant)) {
        SkeuomorphicPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            variant = variant
        ) {
            rowContent()
        }
    } else {
        rowContent()
    }
}


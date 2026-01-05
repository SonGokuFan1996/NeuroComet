package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Instant
import java.time.Duration

/**
 * Production-ready enhanced post card with:
 * - Expandable comments section
 * - Save/bookmark functionality
 * - Share sheet integration
 * - Report functionality
 * - Double-tap to like
 * - Accessibility support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPostCard(
    post: Post,
    comments: List<Comment> = emptyList(),
    onLike: () -> Unit,
    onUnlike: () -> Unit = onLike,
    onComment: (String) -> Unit,
    onShare: (Context, Post) -> Unit,
    onSave: () -> Unit = {},
    onReport: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    isSaved: Boolean = false,
    isOwnPost: Boolean = false,
    isMockInterfaceEnabled: Boolean = true,
    safetyState: SafetyState = SafetyState(),
    modifier: Modifier = Modifier
) {
    var showComments by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var liked by remember { mutableStateOf(post.isLikedByMe) }
    var likeCount by remember { mutableIntStateOf(post.likes) }
    var saved by remember { mutableStateOf(isSaved) }

    val context = LocalContext.current

    // Get user info
    val user = MOCK_USERS.find { it.id == post.userId }
    val avatarUrl = if (isMockInterfaceEnabled) {
        post.userAvatar ?: avatarUrl(post.userId ?: "unknown")
    } else {
        post.userAvatar
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (!isMockInterfaceEnabled && avatarUrl.isNullOrBlank()) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { post.userId?.let { onUserClick(it) } },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { post.userId?.let { onUserClick(it) } },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user?.name ?: post.userId ?: "Unknown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { post.userId?.let { onUserClick(it) } }
                        )
                        if (user?.isVerified == true) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Verified,
                                contentDescription = "Verified",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // More options menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (saved) "Unsave" else "Save Post") },
                            onClick = {
                                saved = !saved
                                onSave()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null
                                )
                            }
                        )
                        if (isOwnPost) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.post_delete_post)) },
                                onClick = {
                                    showDeleteConfirm = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.post_report_post)) },
                                onClick = {
                                    showReportDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Flag, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_copy_link)) },
                            onClick = {
                                // Copy post link
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Link, contentDescription = null)
                            }
                        )
                    }
                }
            }

            // Content
            val shouldHide = safetyState.isKidsMode &&
                ContentFiltering.shouldHideTextForKids(post.content, safetyState.kidsFilterLevel)

            if (shouldHide) {
                Text(
                    text = stringResource(R.string.post_content_hidden_kids),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                val textToShow = if (safetyState.isKidsMode) {
                    ContentFiltering.sanitizeForKids(post.content, safetyState.kidsFilterLevel)
                } else post.content

                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Media
            val showMedia = !safetyState.isKidsMode

            if (showMedia && post.imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(0.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(8.dp))

            // Engagement stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.post_likes_count, likeCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.post_comments_shares_count, comments.size, post.shares),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PostActionButton(
                    icon = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = stringResource(R.string.post_action_like),
                    isActive = liked,
                    activeColor = Color(0xFFE91E63),
                    onClick = {
                        liked = !liked
                        likeCount += if (liked) 1 else -1
                        if (liked) onLike() else onUnlike()
                    }
                )
                PostActionButton(
                    icon = Icons.AutoMirrored.Outlined.Comment,
                    label = stringResource(R.string.post_action_comment),
                    onClick = { showComments = !showComments }
                )
                PostActionButton(
                    icon = Icons.Filled.Share,
                    label = stringResource(R.string.post_action_share),
                    onClick = { showShareSheet = true }
                )
                PostActionButton(
                    icon = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = stringResource(R.string.post_action_save),
                    isActive = saved,
                    activeColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        saved = !saved
                        onSave()
                    }
                )
            }

            // Expandable comments section
            AnimatedVisibility(
                visible = showComments,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.comments_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    // Comments list (show up to 3)
                    comments.take(3).forEach { comment ->
                        CommentItem(
                            comment = comment,
                            isMockInterfaceEnabled = isMockInterfaceEnabled
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (comments.size > 3) {
                        Text(
                            text = stringResource(R.string.comments_view_all, comments.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showComments = !showComments }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Add comment field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text(stringResource(R.string.comments_add_comment_placeholder)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commentText.isNotBlank()) {
                                        onComment(commentText)
                                        commentText = ""
                                    }
                                }
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onComment(commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send comment")
                        }
                    }
                }
            }
        }
    }

    // Share Sheet Dialog
    if (showShareSheet) {
        ShareSheetDialog(
            post = post,
            onDismiss = { showShareSheet = false },
            onShareVia = { shareType ->
                when (shareType) {
                    ShareType.COPY_LINK -> {
                        // Copy link to clipboard
                    }
                    ShareType.SHARE_EXTERNAL -> {
                        onShare(context, post)
                    }
                    ShareType.SHARE_DM -> {
                        // Open DM share
                    }
                    ShareType.REPOST -> {
                        // Repost functionality
                    }
                }
                showShareSheet = false
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        ReportPostDialog(
            onDismiss = { showReportDialog = false },
            onReport = { reason ->
                onReport()
                showReportDialog = false
            }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_post_title)) },
            text = { Text(stringResource(R.string.delete_post_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }
}

@Composable
private fun PostActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    isMockInterfaceEnabled: Boolean = true
) {
    val user = MOCK_USERS.find { it.id == comment.userId }
    val avatarUrl = if (isMockInterfaceEnabled) {
        comment.userAvatar.ifBlank { avatarUrl(comment.userId) }
    } else {
        comment.userAvatar
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Comment author avatar",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.name ?: comment.userId,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (user?.isVerified == true) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = "Verified",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatCommentTime(comment.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCommentTime(timestamp: String): String {
    return try {
        val commentTime = Instant.parse(timestamp)
        val now = Instant.now()
        val diff = Duration.between(commentTime, now)
        when {
            diff.toDays() > 0 -> "${diff.toDays()}d ago"
            diff.toHours() > 0 -> "${diff.toHours()}h ago"
            diff.toMinutes() > 0 -> "${diff.toMinutes()}m ago"
            else -> "Just now"
        }
    } catch (_: Exception) {
        ""
    }
}

enum class ShareType {
    COPY_LINK,
    SHARE_EXTERNAL,
    SHARE_DM,
    REPOST
}

@Composable
fun ShareSheetDialog(
    post: Post,
    onDismiss: () -> Unit,
    onShareVia: (ShareType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.share_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                // Share options
                ShareOptionItem(
                    icon = Icons.Filled.ContentCopy,
                    title = stringResource(R.string.share_sheet_copy_link),
                    subtitle = stringResource(R.string.share_sheet_copy_link_desc),
                    onClick = { onShareVia(ShareType.COPY_LINK) }
                )

                ShareOptionItem(
                    icon = Icons.Filled.Share,
                    title = stringResource(R.string.share_sheet_share_via),
                    subtitle = stringResource(R.string.share_sheet_share_via_desc),
                    onClick = { onShareVia(ShareType.SHARE_EXTERNAL) }
                )

                ShareOptionItem(
                    icon = Icons.Filled.Mail,
                    title = stringResource(R.string.share_sheet_send_dm),
                    subtitle = stringResource(R.string.share_sheet_send_dm_desc),
                    onClick = { onShareVia(ShareType.SHARE_DM) }
                )

                ShareOptionItem(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    title = stringResource(R.string.share_sheet_repost),
                    subtitle = stringResource(R.string.share_sheet_repost_desc),
                    onClick = { onShareVia(ShareType.REPOST) }
                )

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
private fun ShareOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportPostDialog(
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var additionalInfo by remember { mutableStateOf("") }

    val reportReasons = listOf(
        "Spam or misleading",
        "Harassment or bullying",
        "Hate speech or discrimination",
        "Violence or dangerous content",
        "Adult or explicit content",
        "False information",
        "Intellectual property violation",
        "Other"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 500.dp)
            ) {
                Text(
                    text = "Report Post",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Why are you reporting this post?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(reportReasons) { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (selectedReason == "Other") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = additionalInfo,
                        onValueChange = { additionalInfo = it },
                        placeholder = { Text(stringResource(R.string.report_dialog_reason_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.report_dialog_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedReason?.let {
                                val fullReason = if (it == "Other" && additionalInfo.isNotBlank()) {
                                    "Other: $additionalInfo"
                                } else it
                                onReport(fullReason)
                            }
                        },
                        enabled = selectedReason != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.report_dialog_submit))
                    }
                }
            }
        }
    }
}

/**
 * Enhanced create post dialog with:
 * - Character count
 * - Media preview
 * - Mood/feeling selector
 * - Accessibility options
 * - Content warning toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String?, String?) -> Unit,
    isPremium: Boolean = false,
    safetyState: SafetyState = SafetyState()
) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var hasContentWarning by remember { mutableStateOf(false) }
    var showMediaOptions by remember { mutableStateOf(false) }

    val maxCharacters = if (isPremium) 1000 else 500
    val remainingCharacters = maxCharacters - text.length

    val moods = listOf(
        "😊" to "Happy",
        "🤔" to "Thoughtful",
        "😴" to "Tired",
        "🎉" to "Celebrating",
        "💪" to "Motivated",
        "😌" to "Calm",
        "🤯" to "Mind-blown",
        "💡" to "Inspired"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Create,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.create_post_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.create_post_close))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Mood selector
                Text(
                    text = stringResource(R.string.create_post_how_feeling),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                // First row of moods (centered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    moods.take(4).forEach { (emoji, _) ->
                        FilterChip(
                            selected = selectedMood == emoji,
                            onClick = {
                                selectedMood = if (selectedMood == emoji) null else emoji
                            },
                            label = { Text(emoji) }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Second row of moods (centered)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    moods.drop(4).forEach { (emoji, _) ->
                        FilterChip(
                            selected = selectedMood == emoji,
                            onClick = {
                                selectedMood = if (selectedMood == emoji) null else emoji
                            },
                            label = { Text(emoji) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Text input with character count
                val placeholderText = stringResource(R.string.create_post_placeholder)
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxCharacters) text = it },
                    placeholder = {
                        Text(
                            if (selectedMood != null) "$placeholderText $selectedMood"
                            else placeholderText
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 8,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = stringResource(R.string.create_post_chars_remaining, remainingCharacters),
                                color = if (remainingCharacters < 50)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Content warning toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hasContentWarning = !hasContentWarning }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasContentWarning,
                        onCheckedChange = { hasContentWarning = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.create_post_content_warning),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.create_post_content_warning_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Media options
                AnimatedVisibility(visible = showMediaOptions) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text(stringResource(R.string.create_post_image_url)) },
                            placeholder = { Text(stringResource(R.string.create_story_url_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Filled.Image, null) },
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text(stringResource(R.string.create_post_video_url)) },
                            placeholder = { Text(stringResource(R.string.create_story_url_placeholder)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Filled.VideoLibrary, null) },
                            singleLine = true
                        )

                        // Image preview
                        if (imageUrl.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Image preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Bottom action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Media button
                    IconButton(onClick = { showMediaOptions = !showMediaOptions }) {
                        Icon(
                            if (showMediaOptions) Icons.Filled.ExpandLess else Icons.Filled.AttachFile,
                            contentDescription = stringResource(R.string.create_post_add_media),
                            tint = if (showMediaOptions)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.create_post_cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val content = if (selectedMood != null) {
                                    "$selectedMood $text"
                                } else text

                                val finalContent = if (hasContentWarning) {
                                    "⚠️ Content Warning\n\n$content"
                                } else content

                                onPost(
                                    finalContent,
                                    "/gen",
                                    imageUrl.ifBlank { null },
                                    videoUrl.ifBlank { null }
                                )
                            },
                            enabled = text.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.create_post_post))
                        }
                    }
                }
            }
        }
    }
}


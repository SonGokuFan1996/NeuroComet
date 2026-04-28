package com.kyilmaz.neurocomet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.launch

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
    onReport: (String) -> Unit = {},
    onDelete: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    isSaved: Boolean = false,
    isOwnPost: Boolean = false,
    isMockInterfaceEnabled: Boolean = true,
    safetyState: SafetyState = SafetyState(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brailleOptimized = SocialSettingsManager.isBrailleOptimized(context)
    var showComments by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var liked by remember { mutableStateOf(post.isLikedByMe) }
    var likeCount by remember { mutableIntStateOf(post.likes) }
    var saved by remember { mutableStateOf(isSaved) }

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
                        contentDescription = stringResource(R.string.cd_user_avatar),
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
                        contentDescription = stringResource(R.string.cd_user_avatar),
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
                                contentDescription = stringResource(R.string.cd_verified),
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
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
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
                    contentDescription = stringResource(R.string.cd_post_image),
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
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = if (brailleOptimized) "Inline comment input" else "Add a comment"
                                    stateDescription = if (commentText.isBlank()) "Empty" else "${commentText.length} characters entered"
                                },
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
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier.semantics {
                                contentDescription = if (commentText.isBlank()) "Send comment disabled" else "Send comment"
                            },
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.cd_send_comment))
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
                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText(
                            "NeuroComet Post",
                            AppLinks.postUrl(post.id)
                        )
                        clipboardManager.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, context.getString(R.string.toast_link_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ShareType.SHARE_EXTERNAL -> {
                        onShare(context, post)
                    }
                    ShareType.SHARE_DM -> {
                        android.widget.Toast.makeText(context, context.getString(R.string.toast_select_conversation), android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ShareType.REPOST -> {
                        android.widget.Toast.makeText(context, context.getString(R.string.toast_reposted), android.widget.Toast.LENGTH_SHORT).show()
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
                onReport(reason)
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
    val context = LocalContext.current
    val brailleOptimized = SocialSettingsManager.isBrailleOptimized(context)
    TextButton(
        onClick = onClick,
        modifier = Modifier.semantics(mergeDescendants = true) {
            role = Role.Button
            contentDescription = label
            stateDescription = if (isActive) {
                if (brailleOptimized) "On" else "Selected"
            } else {
                if (brailleOptimized) "Off" else "Not selected"
            }
        },
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
            contentDescription = stringResource(R.string.cd_comment_avatar),
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
                        contentDescription = stringResource(R.string.cd_verified),
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
 * - Media preview (Camera, Image, Video)
 * - Mood/feeling selector
 * - Accessibility options
 * - Content warning toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String?, String?, Long?, String?) -> Unit,
    isPremium: Boolean = false,
    @Suppress("UNUSED_PARAMETER") safetyState: SafetyState = SafetyState()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("post_drafts", android.content.Context.MODE_PRIVATE)
    var text by remember { mutableStateOf(prefs.getString("draft_text", "") ?: "") }
    var imageUrl by remember { mutableStateOf<String?>(prefs.getString("draft_image_url", null)) }
    var videoUrl by remember { mutableStateOf<String?>(prefs.getString("draft_video_url", null)) }
    var selectedMood by remember { mutableStateOf<String?>(prefs.getString("draft_mood", null)) }
    var hasContentWarning by remember { mutableStateOf(prefs.getBoolean("draft_cw", false)) }
    var selectedBackgroundColor by remember { 
        val bg = prefs.getInt("draft_bg", -1)
        mutableStateOf<Color?>(if (bg != -1) Color(bg) else null) 
    }
    var locationTag by remember { mutableStateOf<String?>(prefs.getString("draft_location", null)) }
    var isResolvingLocation by remember { mutableStateOf(false) }

    LaunchedEffect(text, imageUrl, videoUrl, selectedMood, hasContentWarning, selectedBackgroundColor, locationTag) {
        prefs.edit()
            .putString("draft_text", text)
            .putString("draft_image_url", imageUrl)
            .putString("draft_video_url", videoUrl)
            .putString("draft_mood", selectedMood)
            .putBoolean("draft_cw", hasContentWarning)
            .putInt("draft_bg", selectedBackgroundColor?.toArgb() ?: -1)
            .putString("draft_location", locationTag)
            .apply()
    }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val resolveCurrentLocation = {
        if (!isResolvingLocation) {
            scope.launch {
                isResolvingLocation = true
                val resolved = LocationHelper.getLocationTag(context)
                if (resolved == null) {
                    Toast.makeText(
                        context,
                        "Unable to get your current location right now.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    locationTag = resolved
                }
                isResolvingLocation = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            resolveCurrentLocation()
        } else {
            Toast.makeText(
                context,
                "Location permission is needed to tag posts with your current location.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val attachmentState = rememberAttachmentState { attachment ->
        when (attachment.type) {
            AttachmentType.IMAGE -> {
                imageUrl = attachment.uri?.toString()
                videoUrl = null
            }
            AttachmentType.VIDEO -> {
                videoUrl = attachment.uri?.toString()
                imageUrl = null
            }
            AttachmentType.LOCATION -> {
                // Not supported yet for posts attachment type
            }
            else -> {}
        }
    }

    val backgroundColors = listOf(
        Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460),
        Color(0xFF4ECDC4), Color(0xFF6BCB77), Color(0xFFFFB347),
        Color(0xFFFF6B6B), Color(0xFF9B59B6), Color(0xFFE91E63),
        Color(0xFF3F51B5), Color(0xFF009688), Color(0xFF795548)
    )

    val maxCharacters = if (isPremium) 1000 else 500
    val remainingCharacters = maxCharacters - text.length

    val moods = listOf(
        "\uD83D\uDE0A" to stringResource(R.string.mood_happy),
        "\uD83E\uDD14" to stringResource(R.string.mood_thoughtful),
        "\uD83D\uDE34" to stringResource(R.string.mood_tired),
        "\uD83C\uDF89" to stringResource(R.string.mood_celebrating),
        "\uD83D\uDCAA" to stringResource(R.string.mood_motivated),
        "\uD83D\uDE0C" to stringResource(R.string.mood_calm),
        "\uD83E\uDD2F" to stringResource(R.string.mood_mind_blown),
        "\uD83D\uDCA1" to stringResource(R.string.mood_inspired)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.create_post_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            val content = if (selectedMood != null) {
                                "$selectedMood $text"
                            } else text

                            val finalContent = if (hasContentWarning) {
                                "⚠️ Content Warning\n\n$content"
                            } else content

                            onPost(
                                finalContent,
                                "/gen",
                                imageUrl,
                                videoUrl,
                                selectedBackgroundColor?.toArgb()?.toLong()?.and(0xFFFFFFFFL),
                                locationTag
                            )
                            prefs.edit().clear().apply()
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    enabled = text.isNotBlank() || imageUrl != null || videoUrl != null,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(stringResource(R.string.create_post_post), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Text input
            val creativePrompts = stringArrayResource(R.array.create_post_prompts)
            val placeholderText = remember { creativePrompts.random() }

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= maxCharacters) text = it },
                placeholder = {
                    Text(
                        if (selectedMood != null) "$placeholderText $selectedMood"
                        else placeholderText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 250.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ),
                maxLines = 10,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "$remainingCharacters",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (remainingCharacters < 50)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // Media preview
            if (imageUrl != null || videoUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Attached Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (videoUrl != null) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(
                        onClick = {
                            imageUrl = null
                            videoUrl = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove Media", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Quick Actions Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilledTonalIconButton(
                        onClick = { attachmentState.onTakePhoto() },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo")
                    }
                }
                item {
                    FilledTonalIconButton(
                        onClick = { attachmentState.onPickImage() },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = "Upload Image")
                    }
                }
                item {
                    FilledTonalIconButton(
                        onClick = {
                            if (locationTag != null) locationTag = null
                            else {
                                val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasLocationPermission) resolveCurrentLocation()
                                else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isResolvingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(if (locationTag == null) Icons.Outlined.LocationOn else Icons.Filled.LocationOff, contentDescription = stringResource(R.string.cd_location))
                        }
                    }
                }

                item {
                    // Content Warning Toggle
                    Surface(
                        color = if (hasContentWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .clickable { hasContentWarning = !hasContentWarning }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = if (hasContentWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("CW", fontWeight = FontWeight.Medium, color = if (hasContentWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (locationTag != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = locationTag.orEmpty(),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Moods
            Text(text = stringResource(R.string.feed_mood_prompt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moods.size) { index ->
                    val (emoji, _) = moods[index]
                    FilterChip(
                        selected = selectedMood == emoji,
                        onClick = { selectedMood = if (selectedMood == emoji) null else emoji },
                        label = { Text(emoji, fontSize = 18.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Background Colors (for text-only)
            AnimatedVisibility(visible = imageUrl == null && videoUrl == null) {
                Column {
                    Text(
                        text = "Post Background",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .then(if (selectedBackgroundColor == null) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                    .clickable { selectedBackgroundColor = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Block, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        items(backgroundColors.size) { index ->
                            val color = backgroundColors[index]
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(if (selectedBackgroundColor == color) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                    .clickable { selectedBackgroundColor = color }
                            )
                        }
                    }
                }
            }
        }
    }
}

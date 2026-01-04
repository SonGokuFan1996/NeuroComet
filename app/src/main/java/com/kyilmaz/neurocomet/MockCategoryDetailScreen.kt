package com.kyilmaz.neurocomet

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kyilmaz.neurocomet.Post
import com.kyilmaz.neurocomet.BubblyPostCard
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockCategoryDetailScreen(
    categoryName: String,
    onBack: () -> Unit,
    onSharePost: (Context, Post) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }
    var showComposeDialog by remember { mutableStateOf(false) }
    var isJoined by remember { mutableStateOf(false) }
    var isNotificationsOn by remember { mutableStateOf(false) }

    // Generate mock posts based on category
    val mockPosts = remember(categoryName) {
        generateMockPostsForCategory(categoryName)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        categoryName, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.community_back))
                    }
                },
                actions = {
                    // Share button
                    IconButton(onClick = {
                        shareCommunity(context, categoryName)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.community_share))
                    }

                    // 3-dot menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.community_more_options))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Join/Leave
                            DropdownMenuItem(
                                text = { Text(if (isJoined) stringResource(R.string.community_leave) else stringResource(R.string.community_join)) },
                                onClick = {
                                    isJoined = !isJoined
                                    showMenu = false
                                    Toast.makeText(
                                        context,
                                        if (isJoined) context.getString(R.string.community_joined_toast, categoryName) else context.getString(R.string.community_left_toast, categoryName),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isJoined) Icons.AutoMirrored.Filled.ExitToApp else Icons.Filled.Add,
                                        contentDescription = null
                                    )
                                }
                            )

                            // Notifications
                            DropdownMenuItem(
                                text = { Text(if (isNotificationsOn) stringResource(R.string.community_mute_notifications) else stringResource(R.string.community_turn_on_notifications)) },
                                onClick = {
                                    isNotificationsOn = !isNotificationsOn
                                    showMenu = false
                                    Toast.makeText(
                                        context,
                                        if (isNotificationsOn) context.getString(R.string.community_notifications_on_toast, categoryName) else context.getString(R.string.community_notifications_muted_toast, categoryName),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isNotificationsOn) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                                        contentDescription = null
                                    )
                                }
                            )

                            HorizontalDivider()

                            // Save/Bookmark
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.community_save)) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, context.getString(R.string.community_saved_toast, categoryName), Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.BookmarkBorder, contentDescription = null)
                                }
                            )

                            // Copy Link
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_copy_link)) },
                                onClick = {
                                    showMenu = false
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Community Link", "https://NeuroComet.app/community/${categoryName.lowercase().replace(" ", "-")}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, context.getString(R.string.community_link_copied), Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Link, contentDescription = null)
                                }
                            )

                            HorizontalDivider()

                            // Community Rules
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.community_rules)) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, context.getString(R.string.community_rules_coming_soon), Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Gavel, contentDescription = null)
                                }
                            )

                            // Report
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.community_report)) },
                                onClick = {
                                    showMenu = false
                                    Toast.makeText(context, context.getString(R.string.community_reported_toast), Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Flag, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showComposeDialog = true },
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text(stringResource(R.string.community_compose)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            // Mock Banner / Resource Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Community Resources",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Tap to view curated guides for $categoryName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            items(mockPosts) { post ->
                BubblyPostCard(
                    post = post,
                    onLike = {},
                    onDelete = {},
                    onReplyPost = {},
                    onShare = onSharePost,
                    isMockInterfaceEnabled = true,
                    safetyState = SafetyState()
                )
            }
        }
    }

    // Compose Dialog
    if (showComposeDialog) {
        ComposePostDialog(
            categoryName = categoryName,
            onDismiss = { showComposeDialog = false },
            onPost = { content ->
                showComposeDialog = false
                Toast.makeText(context, "Post shared to $categoryName!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Dialog for composing a new post in a community
 */
@Composable
private fun ComposePostDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onPost: (String) -> Unit
) {
    var postContent by remember { mutableStateOf("") }
    val maxLength = 500

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Post to $categoryName")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = postContent,
                    onValueChange = { if (it.length <= maxLength) postContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Share your thoughts with the community...") },
                    supportingText = {
                        Text("${postContent.length}/$maxLength")
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Quick action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { postContent += " 🧠" },
                        label = { Text("🧠") }
                    )
                    AssistChip(
                        onClick = { postContent += " ✨" },
                        label = { Text("✨") }
                    )
                    AssistChip(
                        onClick = { postContent += " 💙" },
                        label = { Text("💙") }
                    )
                    AssistChip(
                        onClick = { postContent += " 🎯" },
                        label = { Text("🎯") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPost(postContent) },
                enabled = postContent.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Share a community via system share sheet
 */
private fun shareCommunity(context: Context, categoryName: String) {
    val categorySlug = categoryName.lowercase().replace(" ", "-")
    val shareText = context.getString(R.string.share_category_text, categoryName, categorySlug)
    val shareSubject = context.getString(R.string.share_category_subject, categoryName)
    val shareChooser = context.getString(R.string.share_category_chooser, categoryName)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, shareSubject)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, shareChooser))
}

private fun generateMockPostsForCategory(category: String): List<Post> {
    val userAvatarUrl = { seed: String -> "https://i.pravatar.cc/150?u=$seed" }
    return when (category) {
        "ADHD Hacks" -> listOf(
            // UNDER_13 - Kid-friendly ADHD tips (5 posts)
            Post(
                id = 101L,
                userId = "FocusFriend",
                userAvatar = userAvatarUrl("FocusFriend"),
                content = "🎨 Try coloring while listening to audiobooks! It helps your brain focus better. Works great for homework time!",
                likes = 342,
                comments = 28,
                shares = 15,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 102L,
                userId = "TimerKid",
                userAvatar = userAvatarUrl("TimerKid"),
                content = "🍅 My teacher taught me the tomato timer thing! Work for 15 minutes, then take a 5 minute break. It really helps!",
                likes = 567,
                comments = 45,
                shares = 22,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 108L,
                userId = "HomeWorkHero",
                userAvatar = userAvatarUrl("HomeWorkHero"),
                content = "📚 I made a special homework corner with all my supplies! Now I don't have to get up and get distracted looking for things!",
                likes = 234,
                comments = 19,
                shares = 8,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 109L,
                userId = "MusicLearner",
                userAvatar = userAvatarUrl("MusicLearner"),
                content = "🎵 Listening to music without words helps me do my math! My favorite is piano music!",
                likes = 456,
                comments = 34,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 110L,
                userId = "ChecklistChamp",
                userAvatar = userAvatarUrl("ChecklistChamp"),
                content = "✅ I love checking things off my list! My mom helps me make a picture checklist for my morning routine!",
                likes = 678,
                comments = 52,
                shares = 25,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN - School/social focused ADHD tips (5 posts)
            Post(
                id = 103L,
                userId = "NeuroHacker",
                userAvatar = userAvatarUrl("NeuroHacker"),
                content = "Body doubling saved my thesis! Just having someone on zoom while I work made all the difference.",
                likes = 1242,
                comments = 56,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 104L,
                userId = "DopamineMiner",
                userAvatar = userAvatarUrl("Dopamine"),
                content = "Tip: Keep a 'doom box' for cleaning. Throw everything in a box to sort later, just clear the surfaces now!",
                likes = 853,
                comments = 30,
                shares = 8,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 105L,
                userId = "TimeBlindness",
                userAvatar = userAvatarUrl("TimeBlindness"),
                content = "Does anyone else set alarms for every step of their morning routine? Shower: 7:00, Dry off: 7:15, Dress: 7:20...",
                likes = 2300,
                comments = 150,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 111L,
                userId = "StudyBuddy",
                userAvatar = userAvatarUrl("StudyBuddy"),
                content = "Found out my school library has private study rooms! Game changer for when the cafeteria is too loud and distracting.",
                likes = 789,
                comments = 67,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 112L,
                userId = "NotionNerd",
                userAvatar = userAvatarUrl("NotionNerd"),
                content = "Finally organized my Notion for school! Color-coded by class, automated reminders for assignments. My executive dysfunction can't stop me now 💪",
                likes = 1567,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT - Workplace/medication/therapy focused (5 posts)
            Post(
                id = 106L,
                userId = "ADHDProfessional",
                userAvatar = userAvatarUrl("ADHDProfessional"),
                content = "Finally got workplace accommodations approved! Noise-canceling headphones and flexible deadlines have been game-changers. HR was surprisingly supportive once I provided documentation.",
                likes = 1890,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 107L,
                userId = "MedicationJourney",
                userAvatar = userAvatarUrl("MedicationJourney"),
                content = "Month 3 on Vyvanse: The 'honeymoon phase' is over but still finding it helpful. Working with my psychiatrist on timing - the afternoon crash is real.",
                likes = 456,
                comments = 89,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 113L,
                userId = "TaxesSuck",
                userAvatar = userAvatarUrl("TaxesSuck"),
                content = "Hired an accountant because doing my own taxes with ADHD was a disaster waiting to happen. Best money I've ever spent. The peace of mind is worth it.",
                likes = 2345,
                comments = 312,
                shares = 145,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 114L,
                userId = "ParentingWithADHD",
                userAvatar = userAvatarUrl("ParentingWithADHD"),
                content = "Parenting with ADHD is wild. Forgot my kid's show-and-tell AGAIN. Started a shared calendar with my spouse and set 3 alarms. We're getting better.",
                likes = 1234,
                comments = 189,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 115L,
                userId = "LateToTherapy",
                userAvatar = userAvatarUrl("LateToTherapy"),
                content = "My therapist specializes in ADHD and it makes SUCH a difference. She doesn't judge when I'm late or forget homework. She actually helps me build systems that work for MY brain.",
                likes = 3456,
                comments = 456,
                shares = 234,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )
        "Safe Foods" -> listOf(
            // UNDER_13 - Simple, relatable kid foods (5 posts)
            Post(
                id = 201L,
                userId = "ChickenNuggetFan",
                userAvatar = userAvatarUrl("ChickenNuggetFan"),
                content = "🦖 Dino nuggets are the BEST! They taste way better than boring regular shapes!",
                likes = 789,
                comments = 56,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 202L,
                userId = "PBJLover",
                userAvatar = userAvatarUrl("PBJLover"),
                content = "🥪 Peanut butter and jelly sandwich cut into triangles hits different! Anyone else?",
                likes = 432,
                comments = 34,
                shares = 8,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 207L,
                userId = "PizzaKid",
                userAvatar = userAvatarUrl("PizzaKid"),
                content = "🍕 Plain cheese pizza is the ONLY pizza! No weird toppings please!",
                likes = 876,
                comments = 67,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 208L,
                userId = "CerealChamp",
                userAvatar = userAvatarUrl("CerealChamp"),
                content = "🥣 I like my cereal with the EXACT right amount of milk. Not too soggy!",
                likes = 345,
                comments = 28,
                shares = 9,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 209L,
                userId = "AppleSlices",
                userAvatar = userAvatarUrl("AppleSlices"),
                content = "🍎 Apple slices with caramel dip is my favorite snack! What's yours?",
                likes = 567,
                comments = 89,
                shares = 15,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN - More complex food discussions (5 posts)
            Post(
                id = 203L,
                userId = "TexturePerson",
                userAvatar = userAvatarUrl("TexturePerson"),
                content = "Mac and Cheese is the ultimate safe food. Consistent texture every time. Kraft specifically - the store brand just isn't the same.",
                likes = 5000,
                comments = 700,
                shares = 200,
                imageUrl = "https://picsum.photos/seed/macncheese/400/300",
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 204L,
                userId = "SensoryEater",
                userAvatar = userAvatarUrl("SensoryEater"),
                content = "PSA: It's okay to eat the same lunch every day for months. Your body, your rules. My current rotation is 3 different meals and that's valid.",
                likes = 2341,
                comments = 189,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 210L,
                userId = "BlandFoodClub",
                userAvatar = userAvatarUrl("BlandFoodClub"),
                content = "Why do people act like wanting plain food is weird? Not everyone needs 15 spices on everything. Plain pasta with butter is a complete meal, fight me.",
                likes = 3456,
                comments = 456,
                shares = 123,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 211L,
                userId = "SchoolLunchSurvivor",
                userAvatar = userAvatarUrl("SchoolLunchSurvivor"),
                content = "Anyone else bring the exact same lunch every day? PB&J, chips, apple. Been eating this for 3 years. The cafeteria staff knows me by now.",
                likes = 1234,
                comments = 189,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 212L,
                userId = "TextureIssues",
                userAvatar = userAvatarUrl("TextureIssues"),
                content = "The way my brain rejects food based on texture is so frustrating. Looks good, smells good, but one bite and my brain says 'absolutely not.' 😭",
                likes = 4567,
                comments = 567,
                shares = 234,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT - Nutrition, cooking, social aspects (5 posts)
            Post(
                id = 205L,
                userId = "NutritionWorrier",
                userAvatar = userAvatarUrl("NutritionWorrier"),
                content = "Working with a dietitian who understands ARFID has been life-changing. She doesn't shame me for my limited diet - just helps me find ways to get nutrients from foods I can actually eat.",
                likes = 1567,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 206L,
                userId = "DatingWithARFID",
                userAvatar = userAvatarUrl("DatingWithARFID"),
                content = "The anxiety of going to a new restaurant on a date is REAL. I've started suggesting places I've already vetted. If they judge my 'boring' order, they're not the one.",
                likes = 892,
                comments = 156,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 213L,
                userId = "MealPrepMaster",
                userAvatar = userAvatarUrl("MealPrepMaster"),
                content = "Discovered that batch cooking my safe foods on Sunday saves my sanity all week. 5 containers of the same pasta? Don't care, I'll actually eat it.",
                likes = 2345,
                comments = 312,
                shares = 145,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 214L,
                userId = "FamilyDinnerStress",
                userAvatar = userAvatarUrl("FamilyDinnerStress"),
                content = "Holiday dinners with extended family are a nightmare. 'Just try it!' No, I won't. I'm 35 and I know what I can eat. Brought my own food this year and it was liberating.",
                likes = 3456,
                comments = 567,
                shares = 234,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 215L,
                userId = "SupplementLife",
                userAvatar = userAvatarUrl("SupplementLife"),
                content = "My doctor finally understood that I can't eat most vegetables and worked with me on supplements instead of shaming me. Vitamin levels are finally normal for the first time in years.",
                likes = 1890,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )
        "Stimming" -> listOf(
            // UNDER_13 - Fun, playful stim content (5 posts)
            Post(
                id = 301L,
                userId = "SpinnerKid",
                userAvatar = userAvatarUrl("SpinnerKid"),
                content = "✨ My new glitter wand is SO satisfying to watch! The sparkles fall so slowly!",
                likes = 234,
                comments = 19,
                shares = 5,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 302L,
                userId = "PopItPro",
                userAvatar = userAvatarUrl("PopItPro"),
                content = "🫧 Pop-its are the best! I can do a whole rainbow one in under a minute!",
                likes = 567,
                comments = 45,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 307L,
                userId = "SlimeTime",
                userAvatar = userAvatarUrl("SlimeTime"),
                content = "🌈 Made rainbow slime today! The stretchy sounds are so satisfying!",
                likes = 789,
                comments = 67,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 308L,
                userId = "BubblePopper",
                userAvatar = userAvatarUrl("BubblePopper"),
                content = "🫧 Bubble wrap is the BEST! I saved a big piece from a package!",
                likes = 456,
                comments = 34,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 309L,
                userId = "SandPlayer",
                userAvatar = userAvatarUrl("SandPlayer"),
                content = "🏖️ Kinetic sand is amazing! It sticks together but also falls apart! So cool!",
                likes = 345,
                comments = 28,
                shares = 9,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN - Social acceptance, school context (5 posts)
            Post(
                id = 303L,
                userId = "FidgetSpinner99",
                userAvatar = userAvatarUrl("FidgetSpinner99"),
                content = "Just got this new infinity cube and it's so satisfying. Perfect for boring classes - quiet enough that teachers don't notice.",
                likes = 89,
                comments = 10,
                shares = 5,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 304L,
                userId = "RockingChair",
                userAvatar = userAvatarUrl("RockingChair"),
                content = "Visual stims >> anyone else love watching lava lamps for hours? Got one for my room and it's so calming.",
                likes = 404,
                comments = 60,
                shares = 15,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 310L,
                userId = "QuietStimmer",
                userAvatar = userAvatarUrl("QuietStimmer"),
                content = "Found these silent fidget rings on Amazon. Look like normal jewelry but I can spin them during class. Teacher-proof stimming!",
                likes = 2345,
                comments = 312,
                shares = 145,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 311L,
                userId = "HeadphoneLife",
                userAvatar = userAvatarUrl("HeadphoneLife"),
                content = "Noise-canceling headphones are a form of self-care. The school hallways are so overwhelming without them.",
                likes = 1567,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 312L,
                userId = "TextureLover",
                userAvatar = userAvatarUrl("TextureLover"),
                content = "I have a specific hoodie I wear when stressed. The fabric texture is just *right*. Been wearing it for 4 years and will cry when it wears out.",
                likes = 3456,
                comments = 456,
                shares = 178,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT - Workplace stims, self-advocacy (5 posts)
            Post(
                id = 305L,
                userId = "CorporateStimmer",
                userAvatar = userAvatarUrl("CorporateStimmer"),
                content = "Discovered 'professional' stim toys: stress balls, desk kinetic sculptures, even those magnetic desk toys. Makes stimming in meetings less awkward.",
                likes = 1234,
                comments = 189,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 306L,
                userId = "StimmingAdvocate",
                userAvatar = userAvatarUrl("StimmingAdvocate"),
                content = "Had to have 'the stimming talk' with my new manager. Explained that my leg bouncing and pen clicking actually helps me focus. She was surprisingly understanding.",
                likes = 2100,
                comments = 312,
                shares = 98,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 313L,
                userId = "WFHStimmer",
                userAvatar = userAvatarUrl("WFHStimmer"),
                content = "Working from home means I can stim freely! Rocking in my chair, verbal stims, full body movements. Most productive I've ever been because I'm not masking all day.",
                likes = 4567,
                comments = 567,
                shares = 234,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 314L,
                userId = "StimShaming",
                userAvatar = userAvatarUrl("StimShaming"),
                content = "Unlearning the shame around stimming as an adult is HARD. Years of 'sit still' and 'stop making that noise' really did a number on me. Therapy is helping.",
                likes = 5678,
                comments = 789,
                shares = 345,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 315L,
                userId = "VocalStimmer",
                userAvatar = userAvatarUrl("VocalStimmer"),
                content = "Started doing vocal stims again after suppressing them for 20+ years. My spouse is supportive and my therapist encouraged it. It's like rediscovering a part of myself.",
                likes = 3456,
                comments = 456,
                shares = 189,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )
        "Sensory Tips" -> listOf(
            // UNDER_13 (5 posts)
            Post(
                id = 401L,
                userId = "CozyCorner",
                userAvatar = userAvatarUrl("CozyCorner"),
                content = "🏠 I made a cozy corner with pillows and blankets! It's my calm down spot!",
                likes = 567,
                comments = 45,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 402L,
                userId = "SunglassesKid",
                userAvatar = userAvatarUrl("SunglassesKid"),
                content = "😎 Sunglasses help when the lights are too bright! Even inside!",
                likes = 345,
                comments = 28,
                shares = 9,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 403L,
                userId = "QuietHeadphones",
                userAvatar = userAvatarUrl("QuietHeadphones"),
                content = "🎧 My headphones help when places are too loud! Like the grocery store!",
                likes = 678,
                comments = 56,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 404L,
                userId = "SoftClothes",
                userAvatar = userAvatarUrl("SoftClothes"),
                content = "👕 I only like soft clothes with no tags! My mom cuts all the tags out for me!",
                likes = 432,
                comments = 34,
                shares = 11,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 405L,
                userId = "WeightedBlanket",
                userAvatar = userAvatarUrl("WeightedBlanket"),
                content = "🛏️ My heavy blanket helps me sleep! It feels like a hug!",
                likes = 789,
                comments = 67,
                shares = 34,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN (5 posts)
            Post(
                id = 406L,
                userId = "LightSensitive",
                userAvatar = userAvatarUrl("LightSensitive"),
                content = "Got prescription tinted glasses for light sensitivity. Game changer for school hallways with those awful fluorescent lights.",
                likes = 1234,
                comments = 156,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 407L,
                userId = "EarPlugLife",
                userAvatar = userAvatarUrl("EarPlugLife"),
                content = "Loop earplugs for concerts and parties! Can still hear conversations but the overwhelming background noise is filtered out. Life changing!",
                likes = 2345,
                comments = 312,
                shares = 145,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 408L,
                userId = "TextureCheck",
                userAvatar = userAvatarUrl("TextureCheck"),
                content = "Started only buying clothes online from places with good return policies. I can try textures at home without the stress of fitting rooms.",
                likes = 1567,
                comments = 234,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 409L,
                userId = "ScentSensitive",
                userAvatar = userAvatarUrl("ScentSensitive"),
                content = "Perfume in the hallways makes me nauseous. Started taking the long way around to avoid the area near the bathrooms where everyone sprays body spray.",
                likes = 890,
                comments = 123,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 410L,
                userId = "TemperatureStruggle",
                userAvatar = userAvatarUrl("TemperatureStruggle"),
                content = "Anyone else dysregulated by temperature? I keep a small fan AND a jacket in my backpack because classrooms are either freezing or boiling, never in between.",
                likes = 1345,
                comments = 189,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT (5 posts)
            Post(
                id = 411L,
                userId = "OfficeHell",
                userAvatar = userAvatarUrl("OfficeHell"),
                content = "Open office plans are sensory nightmares. Finally negotiated a desk in a corner facing a wall. The reduction in visual input has helped my focus immensely.",
                likes = 3456,
                comments = 456,
                shares = 189,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 412L,
                userId = "GroceryAnxiety",
                userAvatar = userAvatarUrl("GroceryAnxiety"),
                content = "Started doing grocery pickup instead of going inside. The lights, sounds, people, decisions - it was ruining my whole day. $5 pickup fee is worth my sanity.",
                likes = 4567,
                comments = 567,
                shares = 234,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 413L,
                userId = "SensoryDiet",
                userAvatar = userAvatarUrl("SensoryDiet"),
                content = "My OT helped me create a 'sensory diet' - scheduled sensory input throughout the day to stay regulated. Morning: weighted vest. Lunch: walk outside. Evening: pressure stim. Game changer.",
                likes = 2345,
                comments = 345,
                shares = 156,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 414L,
                userId = "SocialSensory",
                userAvatar = userAvatarUrl("SocialSensory"),
                content = "Had to explain to family why I leave gatherings early. It's not that I don't love them - the sensory overwhelm just builds up and I need to regulate before meltdown.",
                likes = 3456,
                comments = 456,
                shares = 189,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 415L,
                userId = "HomeDesign",
                userAvatar = userAvatarUrl("HomeDesign"),
                content = "Redesigned my apartment with sensory needs in mind: dimmable warm lights, all soft textures, quiet appliances, minimal visual clutter. My home is finally a true safe space.",
                likes = 5678,
                comments = 678,
                shares = 345,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )
        "Executive Function" -> listOf(
            // UNDER_13
            Post(
                id = 641L,
                userId = "RoutineRanger",
                userAvatar = userAvatarUrl("RoutineRanger"),
                content = "🧩 I made a picture schedule for after school: snack → homework → play. It helps me remember what's next!",
                likes = 412,
                comments = 36,
                shares = 18,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 642L,
                userId = "BackpackBuddy",
                userAvatar = userAvatarUrl("BackpackBuddy"),
                content = "🎒 I keep my pencils in the front pocket and my books in the big pocket. Same spots every time = less stress.",
                likes = 305,
                comments = 20,
                shares = 9,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 643L,
                userId = "TimerTiny",
                userAvatar = userAvatarUrl("TimerTiny"),
                content = "⏲️ My mom sets a 10-minute cleanup timer. We race the timer and it makes cleaning less boring!",
                likes = 511,
                comments = 44,
                shares = 21,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 644L,
                userId = "PlannerPanic",
                userAvatar = userAvatarUrl("PlannerPanic"),
                content = "High school hack: if I don't write it down the second it’s assigned, it doesn’t exist. Phone reminders + paper planner combo finally works.",
                likes = 1834,
                comments = 167,
                shares = 74,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 645L,
                userId = "TaskChunker",
                userAvatar = userAvatarUrl("TaskChunker"),
                content = "Breaking assignments into micro-steps (open doc → title → bullet outline) is the only way I start. Starting is the hardest part.",
                likes = 1467,
                comments = 132,
                shares = 61,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 646L,
                userId = "ShowerSchedule",
                userAvatar = userAvatarUrl("ShowerSchedule"),
                content = "I put recurring reminders for self-care because otherwise time just… disappears. It’s not laziness, it’s executive function.",
                likes = 1299,
                comments = 119,
                shares = 55,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 647L,
                userId = "CalendarCaptain",
                userAvatar = userAvatarUrl("CalendarCaptain"),
                content = "My survival stack: calendar blocks, a single capture inbox, and a weekly review. Not perfect, but it keeps the chaos contained.",
                likes = 2104,
                comments = 246,
                shares = 103,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 648L,
                userId = "OneTouchRule",
                userAvatar = userAvatarUrl("OneTouchRule"),
                content = "The 'one-touch' rule helped my clutter: when I pick something up, it either gets put away, scheduled, or binned. No limbo piles.",
                likes = 1688,
                comments = 193,
                shares = 88,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 649L,
                userId = "EnergyAccounting",
                userAvatar = userAvatarUrl("EnergyAccounting"),
                content = "I plan based on energy, not time. Two 'heavy' tasks in a day guarantees burnout. My calendar is now energy-aware.",
                likes = 1950,
                comments = 211,
                shares = 96,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Meltdown Support" -> listOf(
            // UNDER_13
            Post(
                id = 421L,
                userId = "CalmCorner",
                userAvatar = userAvatarUrl("CalmCorner"),
                content = "🧸 I have a 'calm corner' with my soft blanket and headphones. If I feel too big feelings, I go there.",
                likes = 633,
                comments = 58,
                shares = 31,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 422L,
                userId = "BreathingBuddy",
                userAvatar = userAvatarUrl("BreathingBuddy"),
                content = "🌬️ My teacher taught me 'smell the flower, blow the candle' breathing. It helps when my body feels buzzy.",
                likes = 540,
                comments = 41,
                shares = 22,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 423L,
                userId = "SignalWords",
                userAvatar = userAvatarUrl("SignalWords"),
                content = "My family and I agreed on a code word for 'I’m about to melt down'. It stops arguments before they start.",
                likes = 2140,
                comments = 201,
                shares = 84,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 424L,
                userId = "RecoveryRitual",
                userAvatar = userAvatarUrl("RecoveryRitual"),
                content = "Post-meltdown recovery is not optional. I need low light, water, and 30–60 minutes of quiet. Then I can talk.",
                likes = 1899,
                comments = 174,
                shares = 71,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 425L,
                userId = "TriggerTracker",
                userAvatar = userAvatarUrl("TriggerTracker"),
                content = "Tracking patterns helped: hunger + noise + unexpected change is my perfect storm. Now I plan snacks + earplugs + backup plans.",
                likes = 2305,
                comments = 263,
                shares = 118,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 426L,
                userId = "CompassionFirst",
                userAvatar = userAvatarUrl("CompassionFirst"),
                content = "Reminder: meltdowns aren’t tantrums. I’m not trying to manipulate anyone. My nervous system is overloaded.",
                likes = 2844,
                comments = 310,
                shares = 152,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Sensory Diet" -> listOf(
            // UNDER_13
            Post(
                id = 431L,
                userId = "SwingTime",
                userAvatar = userAvatarUrl("SwingTime"),
                content = "🛝 Swinging for 5 minutes before homework helps my body feel calm. It’s like a reset button!",
                likes = 501,
                comments = 39,
                shares = 17,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 432L,
                userId = "CrunchCrew",
                userAvatar = userAvatarUrl("CrunchCrew"),
                content = "🥨 Crunchy snacks help me focus. Pretzels and carrots (only the tiny ones) are my go-to.",
                likes = 422,
                comments = 30,
                shares = 14,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 433L,
                userId = "VestVibes",
                userAvatar = userAvatarUrl("VestVibes"),
                content = "Weighted hoodie/blanket combo is my sensory diet MVP. Deep pressure = brain quiet.",
                likes = 1712,
                comments = 140,
                shares = 66,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 434L,
                userId = "SensoryMenu",
                userAvatar = userAvatarUrl("SensoryMenu"),
                content = "I built a 'sensory menu' for school days: morning movement, midday headphones, afternoon heavy work. It reduced shutdowns.",
                likes = 1620,
                comments = 128,
                shares = 59,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 435L,
                userId = "OccupationalOT",
                userAvatar = userAvatarUrl("OccupationalOT"),
                content = "A sensory diet isn't a food diet—it's planned sensory input. I schedule input the way I schedule meetings.",
                likes = 2055,
                comments = 224,
                shares = 101,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 436L,
                userId = "NoiseBudget",
                userAvatar = userAvatarUrl("NoiseBudget"),
                content = "If I know a loud event is coming, I prep with quiet time before and after. I treat noise like a budget I can spend.",
                likes = 1777,
                comments = 186,
                shares = 83,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Masking & Unmasking" -> listOf(
            // UNDER_13
            Post(
                id = 441L,
                userId = "BeMyself",
                userAvatar = userAvatarUrl("BeMyself"),
                content = "🌈 I’m practicing using my real voice at home. My family says it's okay to be me.",
                likes = 620,
                comments = 48,
                shares = 19,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 442L,
                userId = "ScriptedSocial",
                userAvatar = userAvatarUrl("ScriptedSocial"),
                content = "I didn’t realize I was masking until I got home exhausted every day. I thought everyone felt that drained after talking.",
                likes = 2540,
                comments = 277,
                shares = 132,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 443L,
                userId = "UnmaskingSteps",
                userAvatar = userAvatarUrl("UnmaskingSteps"),
                content = "Unmasking for me is small: letting myself stim, not forcing eye contact, asking for repeats. It’s scary but freeing.",
                likes = 2211,
                comments = 210,
                shares = 98,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 444L,
                userId = "BurnoutTruth",
                userAvatar = userAvatarUrl("BurnoutTruth"),
                content = "Years of masking led to burnout that looked like 'depression' to doctors. Learning my sensory needs changed my entire recovery.",
                likes = 3090,
                comments = 334,
                shares = 175,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 445L,
                userId = "AuthenticAtWork",
                userAvatar = userAvatarUrl("AuthenticAtWork"),
                content = "Unmasking at work doesn't have to mean oversharing. For me it's using accommodations and dropping the 'always smiling' performance.",
                likes = 1988,
                comments = 201,
                shares = 92,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Relationships" -> listOf(
            // UNDER_13
            Post(
                id = 451L,
                userId = "FriendshipGuide",
                userAvatar = userAvatarUrl("FriendshipGuide"),
                content = "🤗 I asked my friend if we could play the same game every recess. They said yes! It makes recess easier.",
                likes = 480,
                comments = 39,
                shares = 15,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 452L,
                userId = "BoundaryBasics",
                userAvatar = userAvatarUrl("BoundaryBasics"),
                content = "Learning boundaries feels like learning a whole new language. Practicing 'I need a break' has helped my friendships.",
                likes = 1644,
                comments = 166,
                shares = 71,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 453L,
                userId = "DirectCommunicator",
                userAvatar = userAvatarUrl("DirectCommunicator"),
                content = "I do best with direct communication. Subtle hints are invisible to me. Saying this upfront improved my relationships a lot.",
                likes = 1902,
                comments = 192,
                shares = 86,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 454L,
                userId = "HouseholdSystems",
                userAvatar = userAvatarUrl("HouseholdSystems"),
                content = "My partner and I stopped fighting when we wrote down expectations. Invisible rules become conflict; visible rules become teamwork.",
                likes = 2207,
                comments = 271,
                shares = 117,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 455L,
                userId = "SensoryDate",
                userAvatar = userAvatarUrl("SensoryDate"),
                content = "Date nights don't have to be loud restaurants. Our best dates are quiet walks, museums, and takeout at home.",
                likes = 1877,
                comments = 206,
                shares = 90,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "AAC & Communication" -> listOf(
            // UNDER_13
            Post(
                id = 461L,
                userId = "TalkTablet",
                userAvatar = userAvatarUrl("TalkTablet"),
                content = "🗣️ My talker app helps me tell people what I want. Today I asked for 'quiet' and my class understood!",
                likes = 520,
                comments = 44,
                shares = 20,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 462L,
                userId = "TypingIsVoice",
                userAvatar = userAvatarUrl("TypingIsVoice"),
                content = "Typing is still communication. I wish people understood that AAC isn't 'less than' speech—it's access.",
                likes = 2100,
                comments = 233,
                shares = 121,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 463L,
                userId = "LowSpeechDay",
                userAvatar = userAvatarUrl("LowSpeechDay"),
                content = "On low-speech days, texting is my best friend. I tell people upfront so they don't assume I'm 'mad' or 'rude'.",
                likes = 1765,
                comments = 151,
                shares = 79,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 464L,
                userId = "AccessibleMeetings",
                userAvatar = userAvatarUrl("AccessibleMeetings"),
                content = "Best accommodation in meetings: allow written input. Give agendas ahead of time. Not everyone processes speech in real time.",
                likes = 1996,
                comments = 197,
                shares = 98,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 465L,
                userId = "CommunicationPartners",
                userAvatar = userAvatarUrl("CommunicationPartners"),
                content = "AAC works best when communication partners slow down, wait, and don't grab the device. 'I’ll wait' is powerful.",
                likes = 1655,
                comments = 168,
                shares = 84,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Disclosure & Advocacy" -> listOf(
            // UNDER_13
            Post(
                id = 471L,
                userId = "AskForHelp",
                userAvatar = userAvatarUrl("AskForHelp"),
                content = "📚 I told my teacher I need instructions one step at a time. She made a checklist for me!",
                likes = 590,
                comments = 49,
                shares = 26,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 472L,
                userId = "IEPJourney",
                userAvatar = userAvatarUrl("IEPJourney"),
                content = "Advocacy tip: write down what helps BEFORE the meeting. When I'm anxious, my brain goes blank.",
                likes = 1432,
                comments = 145,
                shares = 67,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 473L,
                userId = "ScriptsForAsking",
                userAvatar = userAvatarUrl("ScriptsForAsking"),
                content = "I keep a note with scripts: 'Can we move to a quieter spot?' 'Can I get that in writing?' It makes asking doable.",
                likes = 1520,
                comments = 139,
                shares = 72,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 474L,
                userId = "WorkplaceRights",
                userAvatar = userAvatarUrl("WorkplaceRights"),
                content = "Disclosure is a tool, not a moral obligation. Decide based on safety, needs, and support. Small accommodations can be requested without full disclosure.",
                likes = 2060,
                comments = 231,
                shares = 111,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 475L,
                userId = "SelfAdvocate",
                userAvatar = userAvatarUrl("SelfAdvocate"),
                content = "I started bringing a one-page 'how I work best' sheet to hard conversations. It reduced misunderstandings instantly.",
                likes = 1710,
                comments = 176,
                shares = 87,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Career Paths" -> listOf(
            // UNDER_13
            Post(
                id = 481L,
                userId = "DreamJobs",
                userAvatar = userAvatarUrl("DreamJobs"),
                content = "🚀 When I grow up I want to be a scientist who studies space rocks! What job do you want?",
                likes = 405,
                comments = 68,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 482L,
                userId = "StrengthsFirst",
                userAvatar = userAvatarUrl("StrengthsFirst"),
                content = "I’m trying to pick a career based on what my brain is good at (pattern spotting, deep focus) instead of what looks impressive.",
                likes = 1210,
                comments = 144,
                shares = 55,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 483L,
                userId = "JobShadow",
                userAvatar = userAvatarUrl("JobShadow"),
                content = "Job shadowing was way more helpful than career quizzes. Seeing the environment matters (noise, lighting, social demands).",
                likes = 998,
                comments = 92,
                shares = 41,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 484L,
                userId = "NDFriendlyWork",
                userAvatar = userAvatarUrl("NDFriendlyWork"),
                content = "Green flags for ND-friendly roles: written expectations, predictable meetings, async communication, and respect for quiet focus time.",
                likes = 1875,
                comments = 208,
                shares = 101,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 485L,
                userId = "CareerSwitch",
                userAvatar = userAvatarUrl("CareerSwitch"),
                content = "Switching careers at 30+ isn't failure. It's self-knowledge. I optimized for sensory fit and work culture, not titles.",
                likes = 1602,
                comments = 175,
                shares = 89,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "College Transition" -> listOf(
            // UNDER_13
            Post(
                id = 491L,
                userId = "CampusKid",
                userAvatar = userAvatarUrl("CampusKid"),
                content = "🏫 I visited my cousin's college and the library was so quiet and cozy! I liked the big comfy chairs.",
                likes = 230,
                comments = 22,
                shares = 7,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 492L,
                userId = "DormPrep",
                userAvatar = userAvatarUrl("DormPrep"),
                content = "College prep tip: practice doing your own routines now (laundry, meds, appointments). Future you will thank you.",
                likes = 1321,
                comments = 164,
                shares = 60,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 493L,
                userId = "DisabilityOffice",
                userAvatar = userAvatarUrl("DisabilityOffice"),
                content = "Email the disability office BEFORE semester starts. Getting accommodations set up mid-term while overwhelmed was brutal.",
                likes = 1490,
                comments = 181,
                shares = 77,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 494L,
                userId = "ReturningStudent",
                userAvatar = userAvatarUrl("ReturningStudent"),
                content = "Going back to school as an adult: pick class times that match your energy, and build downtime into your schedule. It's not cheating.",
                likes = 1102,
                comments = 133,
                shares = 58,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 495L,
                userId = "QuietHousing",
                userAvatar = userAvatarUrl("QuietHousing"),
                content = "If you can, request housing that matches your sensory needs. Roommates + noise + no recovery space is a recipe for burnout.",
                likes = 1266,
                comments = 140,
                shares = 69,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Gaming" -> listOf(
            // UNDER_13
            Post(
                id = 501L,
                userId = "CozyGamer",
                userAvatar = userAvatarUrl("CozyGamer"),
                content = "🎮 I like games where you can build stuff and take your time. No scary monsters please!",
                likes = 690,
                comments = 84,
                shares = 21,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 502L,
                userId = "SensorySettings",
                userAvatar = userAvatarUrl("SensorySettings"),
                content = "Game accessibility settings are everything: motion blur off, subtitles on, camera shake off. Instant less nausea/overload.",
                likes = 1705,
                comments = 163,
                shares = 74,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 503L,
                userId = "HyperfocusQuest",
                userAvatar = userAvatarUrl("HyperfocusQuest"),
                content = "I blinked and played for 6 hours. Hyperfocus is real—setting an alarm is my only defense.",
                likes = 2012,
                comments = 198,
                shares = 93,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 504L,
                userId = "CoopOverComp",
                userAvatar = userAvatarUrl("CoopOverComp"),
                content = "Co-op games >>> competitive games for my nervous system. I want teamwork, not adrenaline.",
                likes = 1488,
                comments = 144,
                shares = 60,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 505L,
                userId = "GameNightHost",
                userAvatar = userAvatarUrl("GameNightHost"),
                content = "Hosting ND-friendly game nights: predictable schedule, snack options, and permission to take breaks without explaining.",
                likes = 1329,
                comments = 118,
                shares = 57,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Creative Arts" -> listOf(
            // UNDER_13
            Post(
                id = 511L,
                userId = "ColorStorm",
                userAvatar = userAvatarUrl("ColorStorm"),
                content = "🎨 Painting with my fingers is fun! I like making swirls and dots.",
                likes = 410,
                comments = 39,
                shares = 13,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 512L,
                userId = "ArtAsStims",
                userAvatar = userAvatarUrl("ArtAsStims"),
                content = "Drawing repetitive patterns is basically a stim for me. It calms my brain more than almost anything.",
                likes = 1750,
                comments = 150,
                shares = 62,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 513L,
                userId = "MusicLoop",
                userAvatar = userAvatarUrl("MusicLoop"),
                content = "I listen to the same song on repeat while I write. People think it's weird but it's how I regulate.",
                likes = 1600,
                comments = 139,
                shares = 55,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 514L,
                userId = "StudioSetup",
                userAvatar = userAvatarUrl("StudioSetup"),
                content = "ND-friendly art setup: consistent supplies, labeled bins, and a 'good enough' station so perfectionism doesn't block me.",
                likes = 1402,
                comments = 121,
                shares = 49,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 515L,
                userId = "CreativeCareer",
                userAvatar = userAvatarUrl("CreativeCareer"),
                content = "Turning art into work is tricky. I batch admin tasks on one day and keep the rest protected for creative flow.",
                likes = 1120,
                comments = 98,
                shares = 41,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Tech & Science" -> listOf(
            // UNDER_13
            Post(
                id = 521L,
                userId = "BugHunter",
                userAvatar = userAvatarUrl("BugHunter"),
                content = "🔬 I looked at a leaf with a magnifying glass and saw tiny lines! Science is awesome.",
                likes = 360,
                comments = 40,
                shares = 10,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 522L,
                userId = "CodeFocus",
                userAvatar = userAvatarUrl("CodeFocus"),
                content = "Coding is the rare thing that matches my brain: clear feedback loops, patterns, and hyperfocus. Debugging is basically a puzzle.",
                likes = 1822,
                comments = 188,
                shares = 80,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 523L,
                userId = "LabNoise",
                userAvatar = userAvatarUrl("LabNoise"),
                content = "Science labs are sensory chaos sometimes (fluorescent lights + clangs). Noise-canceling headphones saved my grades.",
                likes = 1310,
                comments = 120,
                shares = 54,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 524L,
                userId = "DeepDiveResearch",
                userAvatar = userAvatarUrl("DeepDiveResearch"),
                content = "My special interest is neuroscience papers. I make annotated summaries so future-me can re-enter the topic quickly.",
                likes = 1550,
                comments = 149,
                shares = 66,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 525L,
                userId = "AutomationLife",
                userAvatar = userAvatarUrl("AutomationLife"),
                content = "Automating boring tasks isn't laziness—it's accessibility. If a script can do it, my brain can stay for the interesting parts.",
                likes = 1709,
                comments = 172,
                shares = 78,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Mental Health" -> listOf(
            // UNDER_13
            Post(
                id = 531L,
                userId = "FeelingsFinder",
                userAvatar = userAvatarUrl("FeelingsFinder"),
                content = "💛 I use a feelings chart to pick words when I feel mixed up inside.",
                likes = 540,
                comments = 41,
                shares = 18,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 532L,
                userId = "AnxietySignals",
                userAvatar = userAvatarUrl("AnxietySignals"),
                content = "My anxiety shows up as stomach aches and irritability. Naming the signs early helps me take breaks before I spiral.",
                likes = 2103,
                comments = 240,
                shares = 101,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 533L,
                userId = "CopingToolbox",
                userAvatar = userAvatarUrl("CopingToolbox"),
                content = "My coping toolbox: music, comfy clothes, dark room, safe show, and texting a friend instead of forcing a phone call.",
                likes = 1884,
                comments = 175,
                shares = 82,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 534L,
                userId = "NDInTherapy",
                userAvatar = userAvatarUrl("NDInTherapy"),
                content = "Finding ND-affirming therapy mattered. CBT worksheets alone didn't help until someone validated my sensory reality.",
                likes = 2019,
                comments = 231,
                shares = 110,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 535L,
                userId = "GentleGoals",
                userAvatar = userAvatarUrl("GentleGoals"),
                content = "My mental health improved when I stopped using shame as motivation. Gentle goals > punishment goals.",
                likes = 1777,
                comments = 190,
                shares = 92,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Therapy & Resources" -> listOf(
            // UNDER_13
            Post(
                id = 541L,
                userId = "HelperBooks",
                userAvatar = userAvatarUrl("HelperBooks"),
                content = "📖 I like books that teach about feelings. My favorite one has a dragon that learns to calm down.",
                likes = 330,
                comments = 28,
                shares = 9,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 542L,
                userId = "ResourceList",
                userAvatar = userAvatarUrl("ResourceList"),
                content = "I made a doc of ND-friendly resources (sensory tools, study supports, crisis lines). Having it saved reduced panic.",
                likes = 1208,
                comments = 129,
                shares = 77,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 543L,
                userId = "TherapyFit",
                userAvatar = userAvatarUrl("TherapyFit"),
                content = "Therapy isn't one-size-fits-all. It's okay to interview therapists and ask 'are you neurodiversity-affirming?'",
                likes = 1550,
                comments = 176,
                shares = 88,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 544L,
                userId = "InsuranceMaze",
                userAvatar = userAvatarUrl("InsuranceMaze"),
                content = "Tip: ask for 'superbills' if you're out-of-network. The admin side is exhausting, but it can make therapy affordable.",
                likes = 980,
                comments = 110,
                shares = 52,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 545L,
                userId = "SupportGroups",
                userAvatar = userAvatarUrl("SupportGroups"),
                content = "Peer support groups helped me more than I expected. Being understood without a 20-minute explanation is healing.",
                likes = 1432,
                comments = 151,
                shares = 70,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Medication Experiences" -> listOf(
            // UNDER_13
            Post(
                id = 551L,
                userId = "PillReminder",
                userAvatar = userAvatarUrl("PillReminder"),
                content = "💊 I take my medicine with applesauce because swallowing is hard. My grown-up says that's okay.",
                likes = 250,
                comments = 19,
                shares = 7,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 552L,
                userId = "SideEffectNotes",
                userAvatar = userAvatarUrl("SideEffectNotes"),
                content = "Tracking side effects in a notes app helped me talk to my doctor. I forget details otherwise.",
                likes = 1120,
                comments = 133,
                shares = 56,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 553L,
                userId = "NotMedicalAdvice",
                userAvatar = userAvatarUrl("NotMedicalAdvice"),
                content = "Not medical advice, just my experience: dose timing mattered a lot. A small change made sleep and appetite manageable.",
                likes = 980,
                comments = 120,
                shares = 45,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 554L,
                userId = "DoctorPrep",
                userAvatar = userAvatarUrl("DoctorPrep"),
                content = "I bring a one-page summary to appointments: goals, current meds, side effects, questions. It keeps me from freezing.",
                likes = 1340,
                comments = 144,
                shares = 68,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 555L,
                userId = "MedicationStigma",
                userAvatar = userAvatarUrl("MedicationStigma"),
                content = "Medication isn't a moral failure. For some of us, it’s the support that makes skills and therapy possible.",
                likes = 1622,
                comments = 201,
                shares = 101,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Burnout & Recovery" -> listOf(
            // UNDER_13
            Post(
                id = 561L,
                userId = "RestIsOkay",
                userAvatar = userAvatarUrl("RestIsOkay"),
                content = "🛌 When I feel tired, I take a quiet break with my stuffed animal. Breaks help me be kind.",
                likes = 410,
                comments = 36,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 562L,
                userId = "BurnoutSigns",
                userAvatar = userAvatarUrl("BurnoutSigns"),
                content = "My burnout signs are losing words, headaches, and wanting to hide. It’s not drama—my body is tapping out.",
                likes = 2210,
                comments = 240,
                shares = 122,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 563L,
                userId = "RecoveryPlan",
                userAvatar = userAvatarUrl("RecoveryPlan"),
                content = "Recovery plan: reduce demands, protect sleep, eat safe foods, and stop forcing social performance. Slow is still progress.",
                likes = 2080,
                comments = 198,
                shares = 110,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 564L,
                userId = "LeaveTaken",
                userAvatar = userAvatarUrl("LeaveTaken"),
                content = "I took medical leave for burnout. Hardest decision, best decision. Rest isn't optional maintenance—it's survival.",
                likes = 2455,
                comments = 260,
                shares = 140,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 565L,
                userId = "DemandShaping",
                userAvatar = userAvatarUrl("DemandShaping"),
                content = "I redesigned my life around lower demand: fewer commitments, more buffers, and consistent routines. My capacity came back.",
                likes = 1900,
                comments = 210,
                shares = 112,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Late Diagnosis" -> listOf(
            // UNDER_13
            Post(
                id = 571L,
                userId = "UnderstandingMe",
                userAvatar = userAvatarUrl("UnderstandingMe"),
                content = "🔎 Learning new words about my brain helps me understand myself. I like learning!",
                likes = 260,
                comments = 22,
                shares = 7,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 572L,
                userId = "TeenDx",
                userAvatar = userAvatarUrl("TeenDx"),
                content = "Getting diagnosed as a teen was a rollercoaster: relief + grief + anger that no one noticed earlier.",
                likes = 1543,
                comments = 180,
                shares = 78,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 573L,
                userId = "RewritingHistory",
                userAvatar = userAvatarUrl("RewritingHistory"),
                content = "I keep remembering childhood moments and going 'ohhhh'. Late diagnosis is like re-reading your life with new subtitles.",
                likes = 2011,
                comments = 230,
                shares = 110,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 574L,
                userId = "DxAt38",
                userAvatar = userAvatarUrl("DxAt38"),
                content = "Diagnosed at 38. I’m grieving lost support AND celebrating self-understanding. Both can be true.",
                likes = 2580,
                comments = 290,
                shares = 150,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 575L,
                userId = "CommunityFound",
                userAvatar = userAvatarUrl("CommunityFound"),
                content = "Late diagnosis taught me to stop chasing 'normal'. I now chase accommodations and community.",
                likes = 2204,
                comments = 240,
                shares = 130,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "ND Parenting" -> listOf(
            // UNDER_13
            Post(
                id = 581L,
                userId = "FamilyTeam",
                userAvatar = userAvatarUrl("FamilyTeam"),
                content = "👨‍👩‍👧 My parent and I made a morning chart. We both get stickers when we finish!",
                likes = 355,
                comments = 33,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 582L,
                userId = "SiblingsSupport",
                userAvatar = userAvatarUrl("SiblingsSupport"),
                content = "Living in an ND family means we have lots of accommodations at home. It’s messy but it’s love.",
                likes = 980,
                comments = 102,
                shares = 39,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 583L,
                userId = "ParentWithND",
                userAvatar = userAvatarUrl("ParentWithND"),
                content = "Parenting while ND: I parent with systems, not willpower. Visual schedules, sensory breaks, and lots of repair after hard moments.",
                likes = 1890,
                comments = 210,
                shares = 95,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 584L,
                userId = "GentleRoutines",
                userAvatar = userAvatarUrl("GentleRoutines"),
                content = "We do 'predictable days' when everyone is overloaded: same meals, same plan, quiet evening. It prevents meltdowns for all of us.",
                likes = 1602,
                comments = 180,
                shares = 82,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Independent Living" -> listOf(
            // UNDER_13
            Post(
                id = 591L,
                userId = "PracticeChef",
                userAvatar = userAvatarUrl("PracticeChef"),
                content = "🥪 I learned to make my own sandwich! I put the cheese in a perfect square.",
                likes = 420,
                comments = 44,
                shares = 16,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 592L,
                userId = "LifeSkills",
                userAvatar = userAvatarUrl("LifeSkills"),
                content = "Learning life skills is hard when executive function is hard. I’m starting with one routine at a time: laundry → dishes → budgeting.",
                likes = 1330,
                comments = 150,
                shares = 58,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 593L,
                userId = "BillsAndBrains",
                userAvatar = userAvatarUrl("BillsAndBrains"),
                content = "Auto-pay + calendar reminders + a single 'finance day' per week. It’s the only way bills don’t ambush me.",
                likes = 1755,
                comments = 190,
                shares = 90,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 594L,
                userId = "HomeSetup",
                userAvatar = userAvatarUrl("HomeSetup"),
                content = "My apartment is designed for my brain: open shelves, labels, and duplicates of essentials so I don’t lose momentum.",
                likes = 1621,
                comments = 170,
                shares = 84,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        "Adulting 101" -> listOf(
            // UNDER_13
            Post(
                id = 601L,
                userId = "ChoreChart",
                userAvatar = userAvatarUrl("ChoreChart"),
                content = "✅ I checked off my chores today: feed pet → put toys away → homework. I like the check marks!",
                likes = 390,
                comments = 35,
                shares = 14,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            // TEEN
            Post(
                id = 602L,
                userId = "AdultingPrep",
                userAvatar = userAvatarUrl("AdultingPrep"),
                content = "No one teaches you how to make appointments. I wrote a phone-call script and practiced. It’s awkward but it works.",
                likes = 1402,
                comments = 160,
                shares = 70,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 603L,
                userId = "CleaningLoop",
                userAvatar = userAvatarUrl("CleaningLoop"),
                content = "I do a 10-minute 'reset' every night: trash, dishes, quick tidy. It keeps mess from becoming a crisis.",
                likes = 1555,
                comments = 176,
                shares = 75,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            // ADULT
            Post(
                id = 604L,
                userId = "SystemsNotGoals",
                userAvatar = userAvatarUrl("SystemsNotGoals"),
                content = "Adulting is mostly systems: autopay, routines, and buffers. Motivation comes and goes; systems stay.",
                likes = 2100,
                comments = 240,
                shares = 130,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 605L,
                userId = "KitchenBasics",
                userAvatar = userAvatarUrl("KitchenBasics"),
                content = "I keep 'safe meals' stocked for low-function days: frozen rice, eggs, pasta. Feeding yourself counts as a win.",
                likes = 1804,
                comments = 199,
                shares = 100,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )

        // Default posts for other categories - multiple for each age group
        else -> listOf(
            Post(
                id = 901L,
                userId = "HappyHelper",
                userAvatar = userAvatarUrl("HappyHelper"),
                content = "🌟 Welcome to $category! This is a fun place to share and learn together!",
                likes = 42,
                comments = 5,
                shares = 2,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 904L,
                userId = "FriendlyFace",
                userAvatar = userAvatarUrl("FriendlyFace"),
                content = "😊 I love being part of this community! Everyone is so nice!",
                likes = 123,
                comments = 15,
                shares = 5,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 905L,
                userId = "CuriousKid",
                userAvatar = userAvatarUrl("CuriousKid"),
                content = "❓ I have so many questions about $category! Can someone help me learn?",
                likes = 89,
                comments = 23,
                shares = 3,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.UNDER_13
            ),
            Post(
                id = 902L,
                userId = "CommunityMember",
                userAvatar = userAvatarUrl("CommunityMember"),
                content = "Just discovered this $category community! So great to find others who understand. Drop your best tips below!",
                likes = 156,
                comments = 34,
                shares = 12,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 906L,
                userId = "TeenAdvocate",
                userAvatar = userAvatarUrl("TeenAdvocate"),
                content = "Being a teen and dealing with $category stuff is hard sometimes, but this community makes it easier. You're not alone!",
                likes = 567,
                comments = 89,
                shares = 34,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 907L,
                userId = "HighSchoolLife",
                userAvatar = userAvatarUrl("HighSchoolLife"),
                content = "Navigating $category in high school is a whole thing. Anyone else feel like teachers don't get it?",
                likes = 345,
                comments = 56,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.TEEN
            ),
            Post(
                id = 903L,
                userId = "ExperiencedVoice",
                userAvatar = userAvatarUrl("ExperiencedVoice"),
                content = "Been part of this $category journey for years now. Happy to share resources and answer questions for those just starting out. The learning curve is real but worth it.",
                likes = 234,
                comments = 67,
                shares = 23,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 908L,
                userId = "AdultDiagnosis",
                userAvatar = userAvatarUrl("AdultDiagnosis"),
                content = "Got my $category diagnosis at 40. It explains so much about my life. Late diagnosis grief is real, but so is the relief of finally understanding yourself.",
                likes = 789,
                comments = 123,
                shares = 56,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            ),
            Post(
                id = 909L,
                userId = "ParentPerspective",
                userAvatar = userAvatarUrl("ParentPerspective"),
                content = "Parenting while dealing with my own $category stuff AND supporting my kid who has similar experiences is exhausting but also beautiful. We get each other.",
                likes = 456,
                comments = 78,
                shares = 34,
                createdAt = Instant.now().toString(),
                isLikedByMe = false,
                minAudience = Audience.ADULT
            )
        )
    }
}


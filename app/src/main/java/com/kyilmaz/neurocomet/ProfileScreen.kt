@file:Suppress("UnusedImport", "UNUSED_PARAMETER")

package com.kyilmaz.neurocomet

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Neurodivergent-friendly profile traits that users can display
 * These help others understand communication preferences and needs
 */
enum class NeuroDivergentTrait(
    val emoji: String,
    val label: String,
    val description: String,
    val color: Color
) {
    // Communication style preferences
    DIRECT_COMMUNICATOR("💬", "Direct Communicator", "I prefer clear, literal communication", Color(0xFF2196F3)),
    NEEDS_PROCESSING_TIME("⏳", "Processing Time", "I may need extra time to respond", Color(0xFF9C27B0)),
    TEXT_PREFERRED("📱", "Text Preferred", "I'm more comfortable with written communication", Color(0xFF4CAF50)),

    // Sensory preferences
    SENSORY_SENSITIVE("🎧", "Sensory Sensitive", "I'm sensitive to lights, sounds, or textures", Color(0xFFFF9800)),
    NEEDS_QUIET_SPACES("🤫", "Needs Quiet", "I function better in calm environments", Color(0xFF607D8B)),

    // Social preferences
    SOCIAL_BATTERY("🔋", "Social Battery", "I need alone time to recharge", Color(0xFFE91E63)),
    PARALLEL_PLAY("🎮", "Parallel Play", "I enjoy being together while doing separate things", Color(0xFF00BCD4)),
    SMALL_GROUPS("👥", "Small Groups", "I prefer 1-on-1 or small group interactions", Color(0xFF795548)),

    // Routine and structure
    ROUTINE_ORIENTED("📅", "Routine Oriented", "I thrive with structure and predictability", Color(0xFF3F51B5)),
    FLEXIBLE_TIMING("🕐", "Flexible Timing", "I may be early, late, or need schedule adjustments", Color(0xFFFF5722)),

    // Special interests
    PASSIONATE_INTERESTS("🌟", "Special Interests", "I have deep, passionate interests I love to share", Color(0xFFFFC107)),
    INFO_DUMPING_WELCOME("📚", "Info Dump Friendly", "Feel free to share everything about your interests!", Color(0xFF8BC34A)),

    // Support needs
    NEEDS_REMINDERS("⏰", "Reminder Helpful", "Gentle reminders help me stay on track", Color(0xFF9E9E9E)),
    EXPLICIT_EXPECTATIONS("📋", "Clear Expectations", "I need explicit instructions and expectations", Color(0xFF673AB7)),
    STIMMING_POSITIVE("🌀", "Stim-Friendly", "Stimming is welcome and celebrated here", Color(0xFFCDDC39))
}

/**
 * User's current energy/availability status
 */
enum class EnergyStatus(
    val emoji: String,
    val label: String,
    val description: String,
    val color: Color
) {
    FULLY_CHARGED("🔋", "Fully Charged", "I'm feeling energized and social", Color(0xFF4CAF50)),
    SOCIAL_MODE("💚", "Social Mode", "Open to interactions", Color(0xFF8BC34A)),
    NEUTRAL("😊", "Neutral", "Doing okay, typical day", Color(0xFFFFC107)),
    LOW_BATTERY("🪫", "Low Battery", "Limited energy, may be slow to respond", Color(0xFFFF9800)),
    RECHARGING("💤", "Recharging", "Need alone time, will return soon", Color(0xFF9E9E9E)),
    OVERWHELMED("🫂", "Need Support", "Having a tough time, be gentle", Color(0xFFE91E63)),
    HYPERFOCUS("🎯", "Hyperfocusing", "Deep in a project, may not respond quickly", Color(0xFF2196F3)),
    DO_NOT_DISTURB("🔕", "Do Not Disturb", "Please no notifications right now", Color(0xFFF44336))
}

/**
 * Profile tab options
 */
enum class ProfileTab(val label: String, val icon: ImageVector) {
    POSTS("Posts", Icons.Outlined.GridView),
    ABOUT("About", Icons.Outlined.Person),
    INTERESTS("Interests", Icons.Outlined.Favorite),
    BADGES("Badges", Icons.Outlined.EmojiEvents)
}

/**
 * Extended user profile data for display
 */
data class UserProfile(
    val user: User,
    val bio: String = "",
    val pronouns: String = "",
    val location: String = "",
    val joinedDate: String = "",
    val website: String = "",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val traits: List<NeuroDivergentTrait> = emptyList(),
    val specialInterests: List<String> = emptyList(),
    val energyStatus: EnergyStatus = EnergyStatus.NEUTRAL,
    val communicationNotes: String = "",
    val badges: List<Badge> = emptyList(),
    val isFollowing: Boolean = false,
    val isFollowedByMe: Boolean = false,
    val isMutual: Boolean = false,
    val posts: List<Post> = emptyList()
)

/**
 * Get a mock extended profile for a user
 */
fun getMockUserProfile(userId: String): UserProfile {
    val user = MOCK_USERS.find { it.id == userId } ?: CURRENT_USER

    return when (userId) {
        "me" -> UserProfile(
            user = CURRENT_USER,
            bio = "Just a neurodivergent soul navigating the world one special interest at a time. ✨",
            pronouns = "they/them",
            location = "The Hyperfocus Zone",
            joinedDate = "January 2024",
            followerCount = 42,
            followingCount = 28,
            postCount = 15,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.PASSIONATE_INTERESTS
            ),
            specialInterests = listOf("Coding", "Mechanical Keyboards", "Cats", "Accessibility"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I prefer text over calls. Tone indicators appreciated! /gen",
            badges = MOCK_BADGES.filter { it.isEarned },
            posts = emptyList()
        )
        "NeuroNaut" -> UserProfile(
            user = user,
            bio = "Exploring the neurodivergent experience one post at a time. Autistic advocate & community builder. 🧠🌈",
            pronouns = "she/her",
            location = "Somewhere quiet",
            joinedDate = "March 2023",
            followerCount = 1250,
            followingCount = 340,
            postCount = 89,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.STIMMING_POSITIVE
            ),
            specialInterests = listOf("Neuroscience", "Advocacy", "Fiber Arts", "Star Trek"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I'm very literal - please say what you mean! I love info dumps about your interests. 💜",
            badges = MOCK_BADGES,
            isFollowing = true,
            posts = emptyList()
        )
        "HyperFocusCode" -> UserProfile(
            user = user,
            bio = "ADHD developer turning chaos into code. Hyperfocus is my superpower ⚡ Building accessibility tools.",
            pronouns = "he/him",
            location = "Terminal Window",
            joinedDate = "June 2023",
            followerCount = 890,
            followingCount = 156,
            postCount = 234,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.NEEDS_REMINDERS
            ),
            specialInterests = listOf("Rust", "Accessibility", "Mechanical Keyboards", "Coffee"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "If I don't reply, I'm probably hyperfocused on a project. Feel free to send reminders!",
            badges = MOCK_BADGES.take(3),
            isFollowing = false,
            isMutual = false,
            posts = emptyList()
        )
        "SensorySeeker" -> UserProfile(
            user = user,
            bio = "Artist finding beauty in texture and color. Sensory processing warrior. 🎨",
            pronouns = "they/she",
            location = "Art Studio",
            joinedDate = "September 2023",
            followerCount = 567,
            followingCount = 234,
            postCount = 78,
            traits = listOf(
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.TEXT_PREFERRED,
                NeuroDivergentTrait.PARALLEL_PLAY
            ),
            specialInterests = listOf("Oil Painting", "Textile Art", "Synesthesia", "Nature"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "Voice notes work better for me than video calls. I may take time to respond during sensory overwhelm days.",
            badges = MOCK_BADGES.take(2),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "CalmObserver" -> UserProfile(
            user = user,
            bio = "Mindfulness practitioner. Finding peace in the chaos. Low-stimulation lifestyle advocate. 🧘",
            pronouns = "he/they",
            location = "Quiet Corner",
            joinedDate = "April 2023",
            followerCount = 345,
            followingCount = 89,
            postCount = 45,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.SMALL_GROUPS,
                NeuroDivergentTrait.ROUTINE_ORIENTED
            ),
            specialInterests = listOf("Meditation", "Tea", "Minimalism", "Nature Photography"),
            energyStatus = EnergyStatus.RECHARGING,
            communicationNotes = "I check messages twice daily. Urgent? Use the 🚨 emoji.",
            badges = MOCK_BADGES.take(4),
            isFollowing = false,
            posts = emptyList()
        )
        "DinoLover99" -> UserProfile(
            user = user,
            bio = "🦕 Paleontology enthusiast! Did you know that dinosaurs are closely related to birds? Ask me anything about dinos! 🦖",
            pronouns = "he/him",
            location = "Natural History Museum",
            joinedDate = "February 2023",
            followerCount = 2340,
            followingCount = 156,
            postCount = 456,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.EXPLICIT_EXPECTATIONS
            ),
            specialInterests = listOf("Dinosaurs", "Fossils", "Evolution", "Birds", "Museums"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "I LOVE talking about dinosaurs! Feel free to ask me anything. I might info dump. 🦕",
            badges = MOCK_BADGES,
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "Alex_Stims" -> UserProfile(
            user = user,
            bio = "Stim toy collector and reviewer. Clicky, squishy, spinny - I love them all! 🌀✨",
            pronouns = "they/them",
            location = "Fidget Heaven",
            joinedDate = "July 2023",
            followerCount = 678,
            followingCount = 234,
            postCount = 123,
            traits = listOf(
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.FLEXIBLE_TIMING
            ),
            specialInterests = listOf("Stim Toys", "Mechanical Keyboards", "ASMR", "Crafts"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "Always happy to recommend stim toys! I review new fidgets weekly. 🌀",
            badges = MOCK_BADGES.take(3),
            isFollowing = false,
            posts = emptyList()
        )
        "SpoonCounter" -> UserProfile(
            user = user,
            bio = "Chronic illness & neurodivergent. Counting spoons, saving energy, sharing wisdom. 🥄💜",
            pronouns = "she/her",
            location = "Cozy Bed",
            joinedDate = "August 2023",
            followerCount = 890,
            followingCount = 345,
            postCount = 67,
            traits = listOf(
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.TEXT_PREFERRED,
                NeuroDivergentTrait.NEEDS_REMINDERS
            ),
            specialInterests = listOf("Spoon Theory", "Chronic Illness Advocacy", "Cozy Gaming", "Audiobooks"),
            energyStatus = EnergyStatus.LOW_BATTERY,
            communicationNotes = "Response times vary based on my health. I always reply eventually! 🥄",
            badges = MOCK_BADGES.take(2),
            isFollowing = true,
            posts = emptyList()
        )
        else -> UserProfile(
            user = user,
            bio = user.personality,
            pronouns = "",
            joinedDate = "2024",
            followerCount = (50..500).random(),
            followingCount = (20..200).random(),
            postCount = (10..100).random(),
            traits = NeuroDivergentTrait.entries.shuffled().take(3),
            specialInterests = listOf("Community", "Sharing", "Connection"),
            energyStatus = EnergyStatus.entries.random(),
            badges = MOCK_BADGES.take(2),
            posts = emptyList()
        )
    }
}

/**
 * Main Profile Screen composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onMessageUser: (String) -> Unit = {},
    onFollowToggle: (String) -> Unit = {},
    onPostClick: (Long) -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val profile = remember(userId) { getMockUserProfile(userId) }
    val isOwnProfile = userId == "me"
    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }
    var isFollowing by remember { mutableStateOf(profile.isFollowing) }
    var showStatusPicker by remember { mutableStateOf(false) }
    var showTraitsInfo by remember { mutableStateOf(false) }
    var selectedTrait by remember { mutableStateOf<NeuroDivergentTrait?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isOwnProfile) "My Profile" else profile.user.name,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile")
                        }
                    } else {
                        IconButton(onClick = { /* Share profile */ }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share Profile")
                        }
                        IconButton(onClick = { /* More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    profile = profile,
                    isOwnProfile = isOwnProfile,
                    isFollowing = isFollowing,
                    onFollowToggle = {
                        isFollowing = !isFollowing
                        onFollowToggle(userId)
                    },
                    onMessageUser = { onMessageUser(userId) },
                    onStatusClick = { if (isOwnProfile) showStatusPicker = true }
                )
            }

            // Bio Section
            if (profile.bio.isNotEmpty()) {
                item {
                    BioSection(profile = profile)
                }
            }

            // Traits Section - Neurodivergent-specific
            if (profile.traits.isNotEmpty()) {
                item {
                    TraitsSection(
                        traits = profile.traits,
                        onTraitClick = { trait ->
                            selectedTrait = trait
                            showTraitsInfo = true
                        }
                    )
                }
            }

            // Communication Notes - Important for ND users
            if (profile.communicationNotes.isNotEmpty()) {
                item {
                    CommunicationNotesCard(notes = profile.communicationNotes)
                }
            }

            // Tab Bar
            item {
                ProfileTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // Tab Content
            when (selectedTab) {
                ProfileTab.POSTS -> {
                    if (profile.posts.isEmpty()) {
                        item {
                            EmptyPostsPlaceholder(isOwnProfile = isOwnProfile)
                        }
                    } else {
                        items(profile.posts) { post ->
                            // Post grid item
                        }
                    }
                }
                ProfileTab.ABOUT -> {
                    item {
                        AboutSection(profile = profile)
                    }
                }
                ProfileTab.INTERESTS -> {
                    item {
                        InterestsSection(interests = profile.specialInterests)
                    }
                }
                ProfileTab.BADGES -> {
                    item {
                        BadgesSection(badges = profile.badges)
                    }
                }
            }
        }
    }

    // Status Picker Dialog
    if (showStatusPicker) {
        EnergyStatusPickerDialog(
            currentStatus = profile.energyStatus,
            onStatusSelected = { /* Update status */ },
            onDismiss = { showStatusPicker = false }
        )
    }

    // Trait Info Dialog
    if (showTraitsInfo && selectedTrait != null) {
        TraitInfoDialog(
            trait = selectedTrait!!,
            onDismiss = {
                showTraitsInfo = false
                selectedTrait = null
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onMessageUser: () -> Unit,
    onStatusClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with Energy Status Indicator
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = profile.user.avatarUrl,
                contentDescription = "${profile.user.name}'s avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                profile.energyStatus.color,
                                profile.energyStatus.color.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )

            // Energy Status Badge
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(enabled = isOwnProfile, onClick = onStatusClick)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.energyStatus.emoji,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Name and Verification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = profile.user.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (profile.user.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Pronouns
        if (profile.pronouns.isNotEmpty()) {
            Text(
                text = profile.pronouns,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Energy Status Label
        Surface(
            modifier = Modifier.padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = profile.energyStatus.color.copy(alpha = 0.15f),
            onClick = onStatusClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.energyStatus.emoji,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = profile.energyStatus.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = profile.energyStatus.color
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = profile.postCount.toString(), label = "Posts")
            StatItem(value = formatCount(profile.followerCount), label = "Followers")
            StatItem(value = formatCount(profile.followingCount), label = "Following")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        if (!isOwnProfile) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Follow Button
                Button(
                    onClick = onFollowToggle,
                    modifier = Modifier.weight(1f),
                    colors = if (isFollowing) {
                        ButtonDefaults.outlinedButtonColors()
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    border = if (isFollowing) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Icon(
                        if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFollowing) "Following" else "Follow")
                }

                // Message Button
                OutlinedButton(
                    onClick = onMessageUser,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Message")
                }
            }

            // Mutual/Follows You indicator
            if (profile.isMutual) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Mutual Friends",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        } else {
            // Edit Profile Button for own profile
            OutlinedButton(
                onClick = { /* Edit profile */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BioSection(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (profile.location.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = profile.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TraitsSection(
    traits: List<NeuroDivergentTrait>,
    onTraitClick: (NeuroDivergentTrait) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Communication & Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trait Chips - Scrollable horizontally
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(traits) { trait ->
                TraitChip(
                    trait = trait,
                    onClick = { onTraitClick(trait) }
                )
            }
        }
    }
}

@Composable
private fun TraitChip(
    trait: NeuroDivergentTrait,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = trait.color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, trait.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = trait.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trait.label,
                style = MaterialTheme.typography.labelMedium,
                color = trait.color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CommunicationNotesCard(notes: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How to communicate with me",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ProfileTabBar(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    TabRow(
        selectedTabIndex = ProfileTab.entries.indexOf(selectedTab),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        ProfileTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.label) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun EmptyPostsPlaceholder(isOwnProfile: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isOwnProfile) "Share your first post!" else "No posts yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isOwnProfile) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When you share photos or videos, they'll appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AboutSection(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User's personality/description
        if (profile.user.personality.isNotEmpty()) {
            AboutItem(
                icon = Icons.Outlined.Person,
                label = "About",
                value = profile.user.personality
            )
        }

        // Joined date
        if (profile.joinedDate.isNotEmpty()) {
            AboutItem(
                icon = Icons.Outlined.CalendarMonth,
                label = "Joined",
                value = profile.joinedDate
            )
        }

        // Website
        if (profile.website.isNotEmpty()) {
            AboutItem(
                icon = Icons.Outlined.Link,
                label = "Website",
                value = profile.website
            )
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InterestsSection(interests: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Special Interests ✨",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "These topics bring them joy - ask about them!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Interest chips in a flow layout
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.forEach { interest ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "✨ $interest",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple implementation using Row with wrapping
    // In production, use Accompanist FlowRow or Compose 1.4+ built-in
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Row(
            horizontalArrangement = horizontalArrangement,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
private fun BadgesSection(badges: List<Badge>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Badges & Achievements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (badges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No badges yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            badges.forEach { badge ->
                BadgeItem(badge = badge)
                if (badge != badges.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: Badge) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = badge.iconUrl,
            contentDescription = badge.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badge.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (badge.isEarned) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Earned",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EnergyStatusPickerDialog(
    currentStatus: EnergyStatus,
    onStatusSelected: (EnergyStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "How are you feeling?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EnergyStatus.entries) { status ->
                    Surface(
                        onClick = {
                            onStatusSelected(status)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (status == currentStatus) {
                            status.color.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = if (status == currentStatus) {
                            BorderStroke(2.dp, status.color)
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.emoji,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = status.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = status.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (status == currentStatus) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = status.color
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TraitInfoDialog(
    trait: NeuroDivergentTrait,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = trait.emoji,
                fontSize = 48.sp
            )
        },
        title = {
            Text(
                text = trait.label,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = trait.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!")
            }
        },
        containerColor = trait.color.copy(alpha = 0.1f)
    )
}

/**
 * Helper function to format large numbers
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}


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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.auth.auth
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem

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
    // Create a proper fallback user instead of CURRENT_USER so unknown
    // profiles don't show as "You" / "me"
    val user = MOCK_USERS.find { it.id == userId }
        ?: User(
            id = userId,
            name = userId,
            avatarUrl = avatarUrl(userId.lowercase()),
            isVerified = false,
            personality = "Neurodivergent community member sharing their unique perspective. ✨"
        )

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
        "RainbowNerd" -> UserProfile(
            user = user,
            bio = "Proudly queer and autistic! Sharing memes, resources, and celebrating the intersection of LGBTQ+ and neurodivergent identities. 🏳️‍🌈♾️",
            pronouns = "they/them",
            location = "Pride Parade (in spirit)",
            joinedDate = "June 2023",
            followerCount = 3450,
            followingCount = 678,
            postCount = 312,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.STIMMING_POSITIVE
            ),
            specialInterests = listOf("Queer History", "Meme Culture", "Intersectionality", "Community Building", "Glitter"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Always up for a chat about queer ND experiences! I use tone indicators and appreciate them back. 🏳️‍🌈",
            badges = MOCK_BADGES.take(4),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "TransTechie" -> UserProfile(
            user = user,
            bio = "Trans woman in tech. ADHD warrior. Building accessible software one hyperfocus session at a time. She/Her 🏳️‍⚧️💻",
            pronouns = "she/her",
            location = "VS Code",
            joinedDate = "May 2023",
            followerCount = 2180,
            followingCount = 412,
            postCount = 198,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.NEEDS_REMINDERS
            ),
            specialInterests = listOf("Accessible Software", "Cats", "Open Source", "Trans Healthcare Advocacy", "Mechanical Keyboards"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "DMs open! I might reply at 3 AM because time blindness. Please use she/her pronouns. 🏳️‍⚧️",
            badges = MOCK_BADGES.take(3),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "NonBinaryNinja" -> UserProfile(
            user = user,
            bio = "Enby with AuDHD. Martial arts historian by day, stim toy reviewer by night. They/Them 💜🥋",
            pronouns = "they/them",
            location = "The Dojo",
            joinedDate = "October 2023",
            followerCount = 945,
            followingCount = 267,
            postCount = 87,
            traits = listOf(
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.SENSORY_SENSITIVE
            ),
            specialInterests = listOf("Martial Arts History", "Stim Toys", "Kata Practice", "Japanese Culture", "Fidget Rings"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "I stim between messages — pauses are normal! Best communication: short, clear texts. They/them only please. 💜",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )
        "BiBookworm" -> UserProfile(
            user = user,
            bio = "Bisexual booklover with dyslexia. Audiobook evangelist. Reviewing queer YA so you don't have to. 💗💜💙📚",
            pronouns = "she/her",
            location = "Library Corner",
            joinedDate = "September 2023",
            followerCount = 1670,
            followingCount = 534,
            postCount = 145,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.TEXT_PREFERRED,
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME
            ),
            specialInterests = listOf("Queer YA Novels", "Audiobooks", "Reading Accessibility", "Fanfiction", "Bookbinding"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I process text slowly due to dyslexia — please be patient! Always happy to trade book recs. 📖💗",
            badges = MOCK_BADGES.take(3),
            isFollowing = true,
            posts = emptyList()
        )
        "AceExplorer" -> UserProfile(
            user = user,
            bio = "Asexual & autistic adventurer! Solo traveler documenting sensory-friendly destinations worldwide. 🖤🤍💜🌍",
            pronouns = "he/him",
            location = "Somewhere Quiet & Beautiful",
            joinedDate = "April 2023",
            followerCount = 4120,
            followingCount = 189,
            postCount = 267,
            traits = listOf(
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.EXPLICIT_EXPECTATIONS
            ),
            specialInterests = listOf("Solo Travel", "Sensory-Friendly Tourism", "Photography", "Ace Advocacy", "National Parks"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I plan replies like I plan trips — thoroughly. May take a day but I always respond! 🗺️",
            badges = MOCK_BADGES.take(4),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "PanPride_Sam" -> UserProfile(
            user = user,
            bio = "Pansexual, ADHD, and proud! Creating neurodivergent-affirming LGBTQ+ art. Commissions always open! 💖💛💙🎨",
            pronouns = "he/they",
            location = "Art Studio",
            joinedDate = "July 2023",
            followerCount = 1890,
            followingCount = 623,
            postCount = 234,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.PARALLEL_PLAY
            ),
            specialInterests = listOf("Digital Art", "Pride Art", "Neurodivergent Representation", "Color Theory", "Commissions"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Commission inquiries welcome! I hyperfocus on art so replies may be delayed. He/they pronouns! 💖",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )
        "QueerCoder" -> UserProfile(
            user = user,
            bio = "Queer software engineer building inclusive apps. Autistic & proud. Open source accessibility advocate. 🌈💻♾️",
            pronouns = "they/he",
            location = "GitHub",
            joinedDate = "March 2023",
            followerCount = 5670,
            followingCount = 234,
            postCount = 378,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.EXPLICIT_EXPECTATIONS
            ),
            specialInterests = listOf("Accessibility Engineering", "Open Source", "Inclusive Design", "Rust", "Code Reviews"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "I communicate very directly — not rude, just autistic. PRs and issues preferred over DMs for code stuff. 🌈",
            badges = MOCK_BADGES.take(5),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "LesbianLuna" -> UserProfile(
            user = user,
            bio = "Lesbian artist with autism. Creating cozy, sensory-friendly digital art. Cat mom x3. She/They 🧡🤍💗🐱",
            pronouns = "she/they",
            location = "Cozy Studio",
            joinedDate = "November 2023",
            followerCount = 1230,
            followingCount = 456,
            postCount = 112,
            traits = listOf(
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.SMALL_GROUPS,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES
            ),
            specialInterests = listOf("Digital Art", "Cats", "Cozy Games", "Sensory-Friendly Aesthetics", "Cottagecore"),
            energyStatus = EnergyStatus.RECHARGING,
            communicationNotes = "I'm an introvert who creates best in silence. DMs welcome but replies may come in waves. She/they 🧡",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )

        // ── Feed post authors (MOCK_FEED_POSTS / localized feed) ─────────

        "FocusQueen" -> UserProfile(
            user = user,
            bio = "Sensory-friendly workspace enthusiast 🎧💡 ADHD & autism. Turning my environment into a focus zone, one upgrade at a time!",
            pronouns = "she/her",
            location = "My Optimized Desk",
            joinedDate = "January 2024",
            followerCount = 4820,
            followingCount = 312,
            postCount = 187,
            traits = listOf(
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES
            ),
            specialInterests = listOf("Workspace Design", "Noise-Canceling Tech", "Weighted Gear", "Productivity", "Lighting"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "I share workspace tips daily! DMs open for setup questions. I type fast but think slow — give me a moment! 🎧",
            badges = MOCK_BADGES.take(3),
            isFollowing = false,
            posts = emptyList()
        )
        "NeuroDivergentPride" -> UserProfile(
            user = user,
            bio = "Your brain works differently, not wrong. 🧠💜 Spreading neurodivergent positivity and self-acceptance every day!",
            pronouns = "they/them",
            location = "Everywhere & Nowhere",
            joinedDate = "October 2023",
            followerCount = 12300,
            followingCount = 456,
            postCount = 342,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.STIMMING_POSITIVE
            ),
            specialInterests = listOf("Neurodiversity Advocacy", "Affirmations", "Community Building", "Mental Health", "Art"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Positivity is my brand but I keep it real. DMs always open for those who need encouragement! 💜",
            badges = MOCK_BADGES.take(4),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "ADHDStudent" -> UserProfile(
            user = user,
            bio = "College student with ADHD discovering what works for my brain 📚🎉 Body doubling convert. Sharing the study hacks that actually stick!",
            pronouns = "he/him",
            location = "Campus Library",
            joinedDate = "September 2024",
            followerCount = 2340,
            followingCount = 567,
            postCount = 98,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_REMINDERS,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.PASSIONATE_INTERESTS
            ),
            specialInterests = listOf("Study Techniques", "Body Doubling", "ADHD Hacks", "Gaming", "Ramen"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "Always down to body-double for studying! I reply between classes — might be a few hours. No hard feelings! 📚",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )
        "StimHappy" -> UserProfile(
            user = user,
            bio = "Stim toy collector & zero-shame stimmer! 🌈✨ My meetings are 50% strategy, 50% fidget cube. And that's how I do my best work!",
            pronouns = "she/they",
            location = "Fidget Paradise",
            joinedDate = "March 2024",
            followerCount = 5670,
            followingCount = 389,
            postCount = 213,
            traits = listOf(
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME
            ),
            specialInterests = listOf("Stim Toys", "Fidget Reviews", "Sensory Products", "Crafting", "ASMR"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Send me your favorite stim toys! I'll review anything. No stimming shame here — ever! 🌈✨",
            badges = MOCK_BADGES.take(3),
            isFollowing = true,
            posts = emptyList()
        )
        "LifeHacker_ND" -> UserProfile(
            user = user,
            bio = "ADHD life hacks that actually work 🚀 248 hacks and counting. Launch pad inventor. If I can remember it, it's worth sharing!",
            pronouns = "he/him",
            location = "The Launch Pad",
            joinedDate = "February 2024",
            followerCount = 8900,
            followingCount = 234,
            postCount = 312,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_REMINDERS,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.DIRECT_COMMUNICATOR
            ),
            specialInterests = listOf("Life Hacks", "Organization Systems", "ADHD Strategies", "Home Optimization", "Gadgets"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "Got a life hack? Share it! I compile the best ones weekly. Replies may be delayed — ironic, I know 😅",
            badges = MOCK_BADGES.take(3),
            isFollowing = false,
            posts = emptyList()
        )
        "SelfCareSunday" -> UserProfile(
            user = user,
            bio = "Celebrating the small wins 🎊 Remembering to eat IS self-care. Setting alarms for basic needs since 2022. No shame, just progress! 💪",
            pronouns = "they/she",
            location = "Kitchen (finally)",
            joinedDate = "May 2024",
            followerCount = 3450,
            followingCount = 445,
            postCount = 156,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_REMINDERS,
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.TEXT_PREFERRED,
                NeuroDivergentTrait.FLEXIBLE_TIMING
            ),
            specialInterests = listOf("Self-Care", "Executive Function Hacks", "Meal Reminders", "Gentle Productivity", "Cozy Living"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "I celebrate ALL wins, especially the tiny ones. Be kind to yourself in my comments! 💜",
            badges = MOCK_BADGES.take(2),
            isFollowing = true,
            posts = emptyList()
        )

        // ── Explore / localized post authors ─────────────────────────────

        "NeuroThinker" -> UserProfile(
            user = user,
            bio = "Deep thinker with a hyperfocused mind 🧠 Exploring how neurodivergent brains process the world differently. Come think with me!",
            pronouns = "he/they",
            location = "Thought Bubble",
            joinedDate = "June 2024",
            followerCount = 1560,
            followingCount = 234,
            postCount = 78,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.SMALL_GROUPS
            ),
            specialInterests = listOf("Psychology", "Philosophy", "Hyperfocus", "Writing", "Puzzles"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "I think deeply before responding. Silence means I'm processing, not ignoring you! 🧠",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )
        "ADHDDater" -> UserProfile(
            user = user,
            bio = "Navigating love with ADHD 💕 Sharing the real, messy, beautiful side of neurodivergent relationships. You're not too much!",
            pronouns = "she/her",
            location = "The Dating App Trenches",
            joinedDate = "August 2024",
            followerCount = 3210,
            followingCount = 567,
            postCount = 134,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_REMINDERS
            ),
            specialInterests = listOf("Relationships", "ADHD in Love", "Communication Tips", "Self-Worth", "Journaling"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I overshare about dating with ADHD and I'm not sorry! Ask me anything. 💕",
            badges = MOCK_BADGES.take(2),
            isFollowing = true,
            posts = emptyList()
        )
        "QuietStudier" -> UserProfile(
            user = user,
            bio = "Surviving school one quiet corner at a time 📖 Sensory-friendly study tips for anxious brains. You can do hard things — gently.",
            pronouns = "he/him",
            location = "Quietest Spot in School",
            joinedDate = "September 2024",
            followerCount = 890,
            followingCount = 123,
            postCount = 56,
            traits = listOf(
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.TEXT_PREFERRED
            ),
            specialInterests = listOf("Study Tips", "Quiet Spaces", "Anxiety Management", "Lo-fi Music", "Tea"),
            energyStatus = EnergyStatus.LOW_BATTERY,
            communicationNotes = "I recharge alone. Messages are welcome but I reply on my own schedule 📖",
            badges = MOCK_BADGES.take(1),
            isFollowing = false,
            posts = emptyList()
        )
        "WorkAdvocate" -> UserProfile(
            user = user,
            bio = "Fighting for neurodivergent workplace rights 💼 HR shouldn't be scary. Know your accommodations. Know your worth!",
            pronouns = "she/her",
            location = "The Office (with accommodations)",
            joinedDate = "April 2024",
            followerCount = 4560,
            followingCount = 312,
            postCount = 198,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.EXPLICIT_EXPECTATIONS,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.ROUTINE_ORIENTED
            ),
            specialInterests = listOf("Workplace Rights", "Accommodations", "HR Advocacy", "Career Development", "Mentoring"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "Ask me about workplace accommodations — I've helped 50+ people navigate the process! 💼",
            badges = MOCK_BADGES.take(3),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "ChaoticAdult" -> UserProfile(
            user = user,
            bio = "Adulting with ADHD: chaotic but somehow functional? 🤷‍♂️ Sharing the relatable struggles and occasional wins of grown-up ND life.",
            pronouns = "he/him",
            location = "Laundry Room (procrastinating)",
            joinedDate = "July 2024",
            followerCount = 6780,
            followingCount = 534,
            postCount = 267,
            traits = listOf(
                NeuroDivergentTrait.FLEXIBLE_TIMING,
                NeuroDivergentTrait.NEEDS_REMINDERS,
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.INFO_DUMPING_WELCOME
            ),
            specialInterests = listOf("Adulting Hacks", "ADHD Humor", "Meal Prep", "Time Blindness", "Relatable Content"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "I'm the friend who replies 3 days later with a paragraph. It's not personal — it's executive dysfunction! 😅",
            badges = MOCK_BADGES.take(2),
            isFollowing = false,
            posts = emptyList()
        )
        "LateDiscovery" -> UserProfile(
            user = user,
            bio = "Diagnosed autistic at 34. Everything finally makes sense. Sharing my late-discovery journey for those who need to hear it. 💙",
            pronouns = "they/them",
            location = "Finally Home",
            joinedDate = "June 2024",
            followerCount = 8900,
            followingCount = 345,
            postCount = 178,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_PROCESSING_TIME,
                NeuroDivergentTrait.SENSORY_SENSITIVE
            ),
            specialInterests = listOf("Late Diagnosis", "Self-Discovery", "Autism Advocacy", "Writing", "Identity"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "If my story resonates, my DMs are open. Late discovery is valid. You are valid. 💙",
            badges = MOCK_BADGES.take(3),
            isFollowing = true,
            posts = emptyList()
        )

        // ── Kids / younger audience post authors ─────────────────────────

        "PuzzleKid" -> UserProfile(
            user = user,
            bio = "I love puzzles, riddles, and brain teasers! 🧩 My record is 500 pieces in one afternoon. Always looking for new challenges!",
            pronouns = "he/him",
            location = "Puzzle Corner",
            joinedDate = "January 2025",
            followerCount = 234,
            followingCount = 89,
            postCount = 34,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.ROUTINE_ORIENTED
            ),
            specialInterests = listOf("Jigsaw Puzzles", "Riddles", "Logic Games", "Patterns"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "I get really focused when solving puzzles — I might not reply right away! 🧩",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        "CatLover" -> UserProfile(
            user = user,
            bio = "Cats are the best companions ever! 🐱 They understand the need for quiet time and never judge your stims. Cat pics always welcome!",
            pronouns = "she/her",
            location = "Cat Café",
            joinedDate = "December 2024",
            followerCount = 456,
            followingCount = 178,
            postCount = 67,
            traits = listOf(
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.SMALL_GROUPS
            ),
            specialInterests = listOf("Cats", "Animal Behavior", "Cozy Vibes", "Photography"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "Send me cat pics anytime! I might respond with 10 of my own 🐱💜",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        "GameKid" -> UserProfile(
            user = user,
            bio = "Gamer with a love for creative and cozy games! 🎮 Building worlds in Minecraft and finding secret levels is my thing!",
            pronouns = "he/him",
            location = "Gaming Den",
            joinedDate = "February 2025",
            followerCount = 312,
            followingCount = 145,
            postCount = 45,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.FLEXIBLE_TIMING
            ),
            specialInterests = listOf("Minecraft", "Cozy Games", "Game Design", "Pixel Art"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Always down to play co-op! I like games where we can do our own thing together 🎮",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        "CraftyKid" -> UserProfile(
            user = user,
            bio = "Making friendship bracelets, slime, and all kinds of crafts! ✨ Crafting is my favorite way to stim and be creative!",
            pronouns = "she/they",
            location = "Craft Table",
            joinedDate = "November 2024",
            followerCount = 389,
            followingCount = 201,
            postCount = 78,
            traits = listOf(
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.SENSORY_SENSITIVE
            ),
            specialInterests = listOf("Friendship Bracelets", "Slime Making", "Crafts", "Colors"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I love trading craft ideas! Tell me your favorite colors and I'll make you a bracelet pattern 🌈",
            badges = MOCK_BADGES.take(2),
            posts = emptyList()
        )
        "DoggieFriend" -> UserProfile(
            user = user,
            bio = "Dogs are the best therapy! 🐕 My golden retriever helps me with anxiety every day. Sharing puppy pics and animal-assisted tips!",
            pronouns = "he/him",
            location = "Dog Park",
            joinedDate = "January 2025",
            followerCount = 567,
            followingCount = 234,
            postCount = 56,
            traits = listOf(
                NeuroDivergentTrait.SENSORY_SENSITIVE,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.SOCIAL_BATTERY
            ),
            specialInterests = listOf("Dogs", "Animal Therapy", "Nature Walks", "Photography"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "Dog pics heal all wounds! Share yours and I'll share mine 🐕💛",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        "BookWorm" -> UserProfile(
            user = user,
            bio = "Reading is my superpower! 📚 I can read for hours when I find the right book. Always looking for new recommendations!",
            pronouns = "she/her",
            location = "Library Nook",
            joinedDate = "October 2024",
            followerCount = 345,
            followingCount = 123,
            postCount = 89,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.NEEDS_QUIET_SPACES,
                NeuroDivergentTrait.TEXT_PREFERRED
            ),
            specialInterests = listOf("Reading", "Fantasy Books", "Book Reviews", "Writing Stories"),
            energyStatus = EnergyStatus.NEUTRAL,
            communicationNotes = "Always happy to swap book recommendations! I prefer messages over voice chats 📖",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        "ArtistKid" -> UserProfile(
            user = user,
            bio = "Drawing is how I express everything! 🎨 My sketchbook goes everywhere with me. Art is the best stim!",
            pronouns = "they/them",
            location = "Art Room",
            joinedDate = "December 2024",
            followerCount = 412,
            followingCount = 167,
            postCount = 94,
            traits = listOf(
                NeuroDivergentTrait.PASSIONATE_INTERESTS,
                NeuroDivergentTrait.STIMMING_POSITIVE,
                NeuroDivergentTrait.PARALLEL_PLAY
            ),
            specialInterests = listOf("Drawing", "Watercolors", "Art Supplies", "Animation"),
            energyStatus = EnergyStatus.HYPERFOCUS,
            communicationNotes = "I draw when I'm thinking, so replies might come with a doodle! 🎨✨",
            badges = MOCK_BADGES.take(2),
            posts = emptyList()
        )

        // ── Notification / DM-only users ─────────────────────────────────

        "NeuroDiverse_Dan" -> UserProfile(
            user = user,
            bio = "Neurodiversity advocate & ally 🌈 Following amazing ND creators and boosting their voices. Community first!",
            pronouns = "he/him",
            location = "Online",
            joinedDate = "August 2024",
            followerCount = 567,
            followingCount = 890,
            postCount = 23,
            traits = listOf(
                NeuroDivergentTrait.INFO_DUMPING_WELCOME,
                NeuroDivergentTrait.SOCIAL_BATTERY,
                NeuroDivergentTrait.PASSIONATE_INTERESTS
            ),
            specialInterests = listOf("Neurodiversity", "Community Support", "Advocacy", "Podcasts"),
            energyStatus = EnergyStatus.SOCIAL_MODE,
            communicationNotes = "I'm more of a listener than a poster! Always reading, always supporting 💜",
            badges = MOCK_BADGES.take(1),
            isFollowing = true,
            posts = emptyList()
        )
        "FocusFriend" -> UserProfile(
            user = user,
            bio = "Body doubling buddy & accountability partner! 🎯 Let's get stuff done together. Reposting the best productivity tips!",
            pronouns = "they/them",
            location = "Focus Room",
            joinedDate = "July 2024",
            followerCount = 1230,
            followingCount = 456,
            postCount = 67,
            traits = listOf(
                NeuroDivergentTrait.PARALLEL_PLAY,
                NeuroDivergentTrait.NEEDS_REMINDERS,
                NeuroDivergentTrait.ROUTINE_ORIENTED,
                NeuroDivergentTrait.DIRECT_COMMUNICATOR
            ),
            specialInterests = listOf("Body Doubling", "Productivity", "Pomodoro", "Accountability", "Lo-fi Music"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "Always available for body doubling sessions! I send gentle reminders if you want 🎯",
            badges = MOCK_BADGES.take(2),
            isFollowing = true,
            isMutual = true,
            posts = emptyList()
        )
        "Therapy_Bot" -> UserProfile(
            user = user,
            bio = "🤖 NeuroComet's wellness companion. I share grounding exercises, breathing techniques, and gentle reminders to take care of yourself. Not a replacement for real therapy! 💙",
            pronouns = "",
            location = "NeuroComet HQ",
            joinedDate = "January 2023",
            followerCount = 45600,
            followingCount = 0,
            postCount = 890,
            traits = listOf(
                NeuroDivergentTrait.DIRECT_COMMUNICATOR,
                NeuroDivergentTrait.EXPLICIT_EXPECTATIONS
            ),
            specialInterests = listOf("Wellness", "Grounding Exercises", "Breathing Techniques", "Self-Care Reminders"),
            energyStatus = EnergyStatus.FULLY_CHARGED,
            communicationNotes = "I'm an automated wellness companion — not a therapist. For real support, please reach out to a professional. 💙",
            badges = MOCK_BADGES.take(1),
            posts = emptyList()
        )
        else -> UserProfile(
            user = user,
            bio = user.personality,
            pronouns = listOf("they/them", "she/her", "he/him", "he/they", "she/they").random(),
            location = listOf("NeuroComet", "Online", "Somewhere Safe", "The Quiet Zone", "Community Hub").random(),
            joinedDate = listOf("2023", "2024", "Early 2024", "Late 2023", "Summer 2024").random(),
            followerCount = (50..500).random(),
            followingCount = (20..200).random(),
            postCount = (10..100).random(),
            traits = NeuroDivergentTrait.entries.shuffled().take(3),
            specialInterests = listOf("Community", "Sharing", "Connection", "Self-Expression"),
            energyStatus = EnergyStatus.entries.random(),
            communicationNotes = "Still setting up my profile! Feel free to say hello 👋",
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
    // Determine current authenticated user for follow-relationship checks
    val currentUserId = remember {
        try {
            AppSupabaseClient.client?.auth?.currentSessionOrNull()?.user?.id
        } catch (_: Exception) { null }
    }

    // Start with mock data, then overlay Supabase profile when available
    val mockProfile = remember(userId) { getMockUserProfile(userId) }
    var profile by remember { mutableStateOf(mockProfile) }

    // Fetch real profile from Supabase if authenticated
    LaunchedEffect(userId) {
        if (AppSupabaseClient.isAvailable() && currentUserId != null && userId != "me") {
            val real = ProfileRepository.fetchProfile(userId, currentUserId)
            if (real != null) {
                // Merge: keep mock traits/interests/badges as fallbacks (Supabase doesn't store those yet)
                profile = real.copy(
                    traits = real.traits.ifEmpty { mockProfile.traits },
                    specialInterests = real.specialInterests.ifEmpty { mockProfile.specialInterests },
                    energyStatus = mockProfile.energyStatus,
                    communicationNotes = real.communicationNotes.ifBlank { mockProfile.communicationNotes },
                    badges = real.badges.ifEmpty { mockProfile.badges }
                )
            }
        }
    }

    val isOwnProfile = userId == "me" || userId == currentUserId
    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }
    var isFollowing by remember(profile) { mutableStateOf(profile.isFollowing) }
    var showStatusPicker by remember { mutableStateOf(false) }
    var showTraitsInfo by remember { mutableStateOf(false) }
    var selectedTrait by remember { mutableStateOf<NeuroDivergentTrait?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isOwnProfile) stringResource(R.string.profile_my_profile) else profile.user.name,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.badges_back_button_description))
                    }
                },
                actions = {
                    if (isOwnProfile) {
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.profile_edit))
                        }
                    } else {
                        IconButton(onClick = { /* Share profile */ }) {
                            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.profile_share))
                        }
                        IconButton(onClick = { /* More options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.profile_more_options))
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
            contentPadding = PaddingValues(bottom = M3EDesignSystem.Spacing.bottomNavPadding)
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
                    contentDescription = stringResource(R.string.cd_verified),
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
            StatItem(value = profile.postCount.toString(), label = stringResource(R.string.profile_posts))
            StatItem(value = formatCount(profile.followerCount), label = stringResource(R.string.profile_followers))
            StatItem(value = formatCount(profile.followingCount), label = stringResource(R.string.profile_following))
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
                    Text(if (isFollowing) stringResource(R.string.profile_following) else stringResource(R.string.profile_follow))
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
                    Text(stringResource(R.string.profile_message))
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
                            stringResource(R.string.profile_mutual_friends),
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
                Text(stringResource(R.string.profile_edit))
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
                text = stringResource(R.string.profile_communication_preferences),
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
        color = trait.color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, trait.color.copy(alpha = 0.5f))
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
                color = MaterialTheme.colorScheme.onSurface,
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
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
                    text = stringResource(R.string.profile_how_to_communicate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileTabBar(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    PrimaryTabRow(
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
            text = if (isOwnProfile) stringResource(R.string.profile_share_first_post) else stringResource(R.string.profile_no_posts_yet),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isOwnProfile) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_posts_will_appear),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AboutSection(profile: UserProfile) {
    val aboutLabel = stringResource(R.string.profile_about)
    val joinedLabel = stringResource(R.string.profile_joined)
    val websiteLabel = stringResource(R.string.profile_website)

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
                label = aboutLabel,
                value = profile.user.personality
            )
        }

        // Joined date
        if (profile.joinedDate.isNotEmpty()) {
            AboutItem(
                icon = Icons.Outlined.CalendarMonth,
                label = joinedLabel,
                value = profile.joinedDate
            )
        }

        // Website
        if (profile.website.isNotEmpty()) {
            AboutItem(
                icon = Icons.Outlined.Link,
                label = websiteLabel,
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
            text = stringResource(R.string.profile_special_interests),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.profile_interests_subtitle),
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
            text = stringResource(R.string.profile_badges_achievements),
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
                        text = stringResource(R.string.profile_no_badges_yet),
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
                contentDescription = stringResource(R.string.profile_earned),
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
                stringResource(R.string.profile_how_feeling),
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
                                    contentDescription = stringResource(R.string.profile_selected),
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
                Text(stringResource(R.string.profile_cancel))
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
                Text(stringResource(R.string.profile_got_it))
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


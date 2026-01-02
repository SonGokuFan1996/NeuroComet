package com.kyilmaz.neurocomet

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Settings screen providing user preferences for appearance, safety, and app configuration.
 *
 * Features:
 * - Account management (logout)
 * - Appearance settings (dark mode, high contrast, theme selection)
 * - Safety controls (parental controls, kid mode)
 * - Developer options access (when enabled)
 */

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    safetyViewModel: SafetyViewModel,
    devOptionsViewModel: DevOptionsViewModel,
    canShowDevOptions: Boolean,
    onOpenDevOptions: () -> Unit,
    onOpenParentalControls: () -> Unit,
    onOpenThemeSettings: () -> Unit = {},
    onOpenAnimationSettings: () -> Unit = {},
    onOpenPrivacySettings: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onOpenContentSettings: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
    onOpenWellbeingSettings: () -> Unit = {},
    onOpenFontSettings: () -> Unit = {},
    onOpenSubscription: () -> Unit = {},
    onOpenMyProfile: () -> Unit = {},
    isPremium: Boolean = false,
    isFakePremiumEnabled: Boolean = false,
    onFakePremiumToggle: (Boolean) -> Unit = {},
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val authUser by authViewModel.user.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()

    // Easter egg state - tap version info 7 times!
    var easterEggTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showEasterEggDialog by remember { mutableStateOf(false) }

    val easterEggMessages = listOf(
        "🧠✨ Your brain is not broken, it's a feature!",
        "🌈 Different minds build different worlds!",
        "🦋 Neurodivergence is evolution's art project!",
        "💫 You're not too much, the world is too little!",
        "🎭 Normal is just a setting on the dryer!",
        "🚀 Your brain has premium features, not bugs!",
        "🌟 Divergent paths lead to undiscovered galaxies!"
    )

    val selectedMessage = remember { mutableStateOf(easterEggMessages.random()) }

    // Easter egg dialog
    if (showEasterEggDialog) {
        // Unlock the Rainbow Brain theme when easter egg is triggered
        if (!themeState.rainbowBrainUnlocked) {
            themeViewModel.unlockRainbowBrain()
        }

        AlertDialog(
            onDismissRequest = { showEasterEggDialog = false },
            title = {
                Text(
                    text = if (!themeState.rainbowBrainUnlocked) "🎊 Secret Unlocked! 🎊" else "🦄 Welcome Back! 🦄",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = selectedMessage.value,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    if (!themeState.rainbowBrainUnlocked) {
                        Text(
                            text = "🦄 You've unlocked the Rainbow Brain theme!\nFind it in Theme Settings → Secret Themes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Made with 💜 for every unique mind",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEasterEggDialog = false
                    selectedMessage.value = easterEggMessages.random()
                }) {
                    Text("You're Awesome! 🙌")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ═══════════════════════════════════════════════════════════════
            // ACCOUNT SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "account_header") {
                SettingsSectionHeader(
                    title = "Account",
                    icon = Icons.Outlined.AccountCircle
                )
            }
            item(key = "my_profile") {
                SettingsCard {
                    SettingsItem(
                        title = "My Profile",
                        description = "View and edit your profile, energy status & preferences",
                        icon = Icons.Outlined.AccountCircle,
                        onClick = onOpenMyProfile
                    )
                }
            }
            item(key = "account_card") {
                authUser?.let {
                    AccountInfoCard(user = it, onLogout = onLogout)
                } ?: run {
                    Text("User not authenticated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item(key = "spacer_after_account") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // PREMIUM SUBSCRIPTION SECTION
            // ═══════════════════════════════════════════════════════════════
            // PREMIUM SECTION - Show "Go Premium" or "Premium Active" based on status
            // ═══════════════════════════════════════════════════════════════
            item(key = "premium_header") {
                SettingsSectionHeader(
                    title = "Premium",
                    icon = Icons.Filled.Star
                )
            }
            item(key = "premium_card") {
                SettingsCard {
                    if (isPremium) {
                        // Premium is active - show thank you message
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFD4AF37),
                                                Color(0xFFE8C547)
                                            )
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF1A1A1A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Premium Active ✨",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    "Thank you for your support!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Premium active",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Not premium - show upgrade option
                        SettingsItem(
                            title = "Go Premium ⭐",
                            description = "Ad-free experience • $2/month or $60 lifetime",
                            icon = Icons.Filled.Star,
                            onClick = onOpenSubscription
                        )
                    }
                }
            }

            item(key = "spacer_after_premium") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // APPEARANCE & DISPLAY SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "appearance_header") {
                SettingsSectionHeader(
                    title = "Appearance & Display",
                    icon = Icons.Default.Palette
                )
            }
            item(key = "appearance_card") {
                SettingsCard {
                    SettingsItem(
                        title = "Theme Settings",
                        description = "Colors, styles & neurodivergent themes",
                        icon = Icons.Default.Palette,
                        onClick = onOpenThemeSettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Animation Settings",
                        description = if (themeState.animationSettings.disableAllAnimations)
                            "All animations disabled"
                        else
                            "Control motion and visual effects",
                        icon = Icons.Default.Animation,
                        onClick = onOpenAnimationSettings
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = "Dark Mode",
                        description = "Use dark theme for low-light environments",
                        icon = Icons.Default.DarkMode,
                        isChecked = themeState.isDarkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = "High Contrast",
                        description = "Maximum contrast for visibility",
                        icon = Icons.Default.Contrast,
                        isChecked = themeState.isHighContrast,
                        onCheckedChange = { themeViewModel.setIsHighContrast(it) }
                    )
                }
            }

            item(key = "spacer_after_appearance") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // PRIVACY & SECURITY SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "privacy_header") {
                SettingsSectionHeader(
                    title = "Privacy & Security",
                    icon = Icons.Default.Lock
                )
            }
            item(key = "privacy_card") {
                SettingsCard {
                    SettingsItem(
                        title = "Privacy Settings",
                        description = "Who can see your content & message you",
                        icon = Icons.Default.Lock,
                        onClick = onOpenPrivacySettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Parental Controls",
                        description = if (safetyState.isParentalPinSet) "PIN is set • Tap to manage" else "Set up PIN & controls",
                        icon = Icons.Default.Shield,
                        onClick = onOpenParentalControls
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = "Kid Mode",
                        description = "Age-appropriate content filtering",
                        icon = Icons.Default.ChildFriendly,
                        isChecked = safetyState.isKidsMode,
                        onCheckedChange = { safetyViewModel.setAudience(if (it) Audience.UNDER_13 else Audience.ADULT, app) }
                    )
                }
            }

            item(key = "spacer_after_privacy") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // NOTIFICATIONS SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "notifications_header") {
                SettingsSectionHeader(
                    title = "Notifications",
                    icon = Icons.Default.Notifications
                )
            }
            item(key = "notifications_card") {
                SettingsCard {
                    SettingsItem(
                        title = "Notification Preferences",
                        description = "Push notifications, sounds & quiet hours",
                        icon = Icons.Default.Notifications,
                        onClick = onOpenNotificationSettings
                    )
                }
            }

            item(key = "spacer_after_notifications") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // CONTENT & MEDIA SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "content_header") {
                SettingsSectionHeader(
                    title = "Content & Media",
                    icon = Icons.Default.PlayCircle
                )
            }
            item(key = "content_card") {
                SettingsCard {
                    SettingsItem(
                        title = "Content Preferences",
                        description = "Video autoplay, data saver & feed settings",
                        icon = Icons.Default.PlayCircle,
                        onClick = onOpenContentSettings
                    )
                }
            }

            item(key = "spacer_after_content") { Spacer(Modifier.height(8.dp)) }

            // ═══════════════════════════════════════════════════════════════
            // ACCESSIBILITY & WELLBEING SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "accessibility_header") {
                SettingsSectionHeader(
                    title = "Accessibility & Wellbeing",
                    icon = Icons.Default.Accessibility,
                    subtitle = "Neurodivergent-friendly options"
                )
            }
            item(key = "accessibility_card") {
                SettingsCard {
                    SettingsItem(
                        title = "Font & Reading",
                        description = "Neurodivergent-friendly fonts (Lexend, OpenDyslexic & more)",
                        icon = Icons.Default.TextFields,
                        onClick = onOpenFontSettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "Accessibility Options",
                        description = "Motion reduction & cognitive support",
                        icon = Icons.Default.Accessibility,
                        onClick = onOpenAccessibilitySettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = "NeuroBalance",
                        description = "Break reminders, usage stats & calm mode",
                        icon = Icons.Default.Spa,
                        onClick = onOpenWellbeingSettings
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // TIPS & HELP
            // ═══════════════════════════════════════════════════════════════
            item(key = "spacer_before_tips") { Spacer(Modifier.height(8.dp)) }
            item(key = "tips_header") {
                SettingsSectionHeader(
                    title = "Tips & Help",
                    icon = Icons.Default.Star,
                    subtitle = "Quick tips for a better experience"
                )
            }
            item(key = "tips_card") {
                TipsAndHelpCard()
            }

            // Tutorial replay option
            item(key = "tutorial_card") {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Tutorial",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Watch the getting started guide again",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RestartTutorialButton()
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // DEVELOPER OPTIONS (if enabled)
            // ═══════════════════════════════════════════════════════════════
            if (canShowDevOptions) {
                item(key = "spacer_before_dev") { Spacer(Modifier.height(8.dp)) }
                item(key = "dev_header") {
                    SettingsSectionHeader(
                        title = "Developer",
                        icon = Icons.Default.Build,
                        subtitle = "Debug & testing tools"
                    )
                }
                item(key = "dev_card") {
                    SettingsCard {
                        // Fake Premium Toggle for testing
                        SettingsToggle(
                            title = "Fake Premium Mode",
                            description = if (isFakePremiumEnabled) "Premium features unlocked for testing" else "Enable to test premium features",
                            icon = Icons.Default.Star,
                            isChecked = isFakePremiumEnabled,
                            onCheckedChange = onFakePremiumToggle
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        SettingsItem(
                            title = stringResource(R.string.settings_developer_options_group),
                            description = "Advanced options for testing & debugging",
                            icon = Icons.Default.Build,
                            onClick = onOpenDevOptions
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // APP INFO & VERSION
            // ═══════════════════════════════════════════════════════════════
            item(key = "spacer_before_footer") { Spacer(Modifier.height(24.dp)) }
            item(key = "app_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Easter egg: tap 7 times within 3 seconds
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime > 3000) {
                                easterEggTapCount = 1
                            } else {
                                easterEggTapCount++
                            }
                            lastTapTime = currentTime

                            if (easterEggTapCount >= 7) {
                                showEasterEggDialog = true
                                easterEggTapCount = 0
                            }
                        }
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NeuroComet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (easterEggTapCount in 3..6) {
                        Text(
                            text = "${7 - easterEggTapCount} more taps...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Bottom padding for navigation bar
            item(key = "bottom_spacer") { Spacer(Modifier.height(32.dp)) }
        }
    }
}

/**
 * Displays user account information with a logout button.
 *
 * @param user The authenticated user to display
 * @param onLogout Callback when the logout button is pressed
 */
@Composable
fun AccountInfoCard(user: User, onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "User avatar",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.id,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}

/**
 * A clickable settings row with icon, title, description, and navigation arrow.
 */
@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "$title, $description"
            }
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Tips and Help card with expandable tips for users
 */
@Composable
private fun TipsAndHelpCard() {
    var expanded by remember { mutableStateOf(false) }

    val tips = listOf(
        TipItem("🔄", "Quick Animation Toggle", "Tap the ♾️ infinity logo on the login screen to quickly enable/disable animations"),
        TipItem("🦄", "Unlock Rainbow Brain", "Tap the version info 7 times in Settings to unlock the secret Rainbow Brain theme!"),
        TipItem("🌙", "Reduce Eye Strain", "Enable Dark Mode + Autism Low Stimulation theme for the calmest experience"),
        TipItem("📖", "Better Readability", "Try Lexend or Atkinson Hyperlegible fonts - designed for neurodivergent readers"),
        TipItem("🧘", "Calm Experience", "Disable all animations in Settings > Animations and use the Calm or Overload theme"),
        TipItem("⚡", "Stay Focused", "Use ADHD Task Mode theme + Hyperfocus for minimal distractions"),
        TipItem("👶", "Protect Kids", "Set up Parental Controls with a PIN to manage screen time and content"),
        TipItem("🎄", "Holiday Themes", "The logo automatically celebrates holidays and ND awareness events with special colors!"),
        TipItem("🎨", "25+ Themes", "Explore themes for ADHD, Autism, Anxiety, Colorblind, Low Vision, and your current mood"),
        TipItem("🔤", "12+ Fonts", "Choose from dyslexia-friendly, ADHD-friendly, and accessibility fonts in Settings > Font & Reading")
    )

    SettingsCard {
        Column {
            // Header row - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quick Tips & Tricks",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (expanded) "Tap to collapse" else "Tap to see ${tips.size} helpful tips",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expandable tips list
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                tips.forEach { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = tip.emoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tip.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = tip.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TipItem(
    val emoji: String,
    val title: String,
    val description: String
)

/**
 * A toggle setting row with icon, title, description, and switch.
 * Fully accessible with proper semantics for screen readers.
 */
@Composable
fun SettingsToggle(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val stateDescription = if (isChecked) "enabled" else "disabled"
    val alpha = if (enabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                contentDescription = "$title, $description, $stateDescription"
            }
            .clickable(
                role = Role.Switch,
                enabled = enabled,
                onClick = { onCheckedChange(!isChecked) }
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null, // Handled by row click
            enabled = enabled
        )
    }
}


/**
 * Card container for grouping related settings.
 */
@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            content = content
        )
    }
}

/**
 * Divider for separating items within a SettingsCard.
 */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 40.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * Toggle setting designed to be used inside a SettingsCard.
 */
@Composable
fun SettingsToggleInCard(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val stateDescription = if (isChecked) "enabled" else "disabled"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                contentDescription = "$title, $description, $stateDescription"
            }
            .clickable(
                role = Role.Switch,
                onClick = { onCheckedChange(!isChecked) }
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null // Handled by row click
        )
    }
}

/**
 * Comprehensive theme picker organized by neurodivergent categories.
 *
 * Displays collapsible category sections with horizontally scrollable
 * theme options. Each theme is designed with specific sensory and
 * cognitive needs in mind.
 *
 * @param selectedState The currently selected NeuroState theme
 * @param onStateSelected Callback when a new theme is selected
 * @param isSecretUnlocked Whether secret themes are unlocked (via easter egg)
 * @param isDynamicColorEnabled Whether dynamic colors are enabled (disables custom themes)
 */
@Composable
fun NeuroThemePicker(
    selectedState: NeuroState,
    onStateSelected: (NeuroState) -> Unit,
    isSecretUnlocked: Boolean = false,
    isDynamicColorEnabled: Boolean = false
) {
    var expandedCategory by remember { mutableStateOf<NeuroStateCategory?>(null) }

    // Filter categories based on unlock status - use derivedStateOf to prevent recomposition
    val visibleCategories by remember(isSecretUnlocked) {
        derivedStateOf {
            NeuroStateCategory.entries.filter {
                !it.isSecret || isSecretUnlocked
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show message when dynamic colors are enabled
        if (isDynamicColorEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Dynamic Colors Active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Disable Dynamic Colors in Quick Settings below to use custom themes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        visibleCategories.forEach { category ->
            key(category.name) {
                ThemeCategoryCard(
                    category = category,
                    selectedState = selectedState,
                    isExpanded = expandedCategory == category && !isDynamicColorEnabled,
                    onToggleExpand = {
                        if (!isDynamicColorEnabled) {
                            expandedCategory = if (expandedCategory == category) null else category
                        }
                    },
                    onStateSelected = { state ->
                        if (!isDynamicColorEnabled) {
                            onStateSelected(state)
                        }
                    },
                    isDisabled = isDynamicColorEnabled
                )
            }
        }
    }
}

/**
 * A collapsible card displaying a theme category with its options.
 */
@Composable
private fun ThemeCategoryCard(
    category: NeuroStateCategory,
    selectedState: NeuroState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onStateSelected: (NeuroState) -> Unit,
    isDisabled: Boolean = false
) {
    val selectedInCategory = category.states.find { it == selectedState }
    val alpha = if (isDisabled) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isDisabled, onClick = onToggleExpand)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedInCategory != null && !isDisabled) {
                        Text(
                            text = "${selectedInCategory.emoji} ${selectedInCategory.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (isDisabled) {
                        Text(
                            text = "Disabled (Dynamic Colors active)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse ${category.displayName}" else "Expand ${category.displayName}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded content with theme options
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = category.states,
                        key = { it.name }
                    ) { state ->
                        ThemeOptionChip(
                            state = state,
                            isSelected = state == selectedState,
                            onClick = { onStateSelected(state) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A selectable chip displaying a theme option with emoji, name, and selection state.
 */
@Composable
private fun ThemeOptionChip(
    state: NeuroState,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    val selectionState = if (isSelected) "selected" else "not selected"

    Card(
        modifier = Modifier
            .width(120.dp)
            .semantics {
                role = Role.RadioButton
                contentDescription = "${state.displayName}, ${state.description}, $selectionState"
            }
            .clickable(
                role = Role.RadioButton,
                onClick = onClick
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = state.emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = state.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null, // Already announced in semantics
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Dedicated Theme Settings Screen with all neurodivergent-friendly themes.
 *
 * Provides a full-screen experience for browsing and selecting themes,
 * organized by category for easy navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()

    // Easter egg state - tap the current theme card multiple times!
    var easterEggTapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showEasterEggDialog by remember { mutableStateOf(false) }

    val easterEggMessages = listOf(
        "🧠✨ Your brain is not broken, it's a feature!",
        "🌈 Different minds build different worlds!",
        "🦋 Neurodivergence is evolution's art project!",
        "💫 You're not too much, the world is too little!",
        "🎭 Normal is just a setting on the dryer!",
        "🚀 Your brain has premium features, not bugs!",
        "🌟 Divergent paths lead to undiscovered galaxies!"
    )

    val selectedThemeMessage = remember { mutableStateOf(easterEggMessages.random()) }

    // Easter egg dialog
    if (showEasterEggDialog) {
        // Unlock the Rainbow Brain theme when easter egg is triggered
        if (!themeState.rainbowBrainUnlocked) {
            themeViewModel.unlockRainbowBrain()
        }

        AlertDialog(
            onDismissRequest = { showEasterEggDialog = false },
            title = {
                Text(
                    text = if (!themeState.rainbowBrainUnlocked) "🎊 Secret Unlocked! 🎊" else "🦄 Welcome Back! 🦄",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = selectedThemeMessage.value,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    if (!themeState.rainbowBrainUnlocked) {
                        Text(
                            text = "🦄 You've unlocked the Rainbow Brain theme!\nScroll down to Secret Themes to try it!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Made with 💜 for every unique mind",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEasterEggDialog = false
                    selectedThemeMessage.value = easterEggMessages.random()
                }) {
                    Text("You're Awesome! 🙌")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Theme Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose a theme that works for you",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current theme indicator
            item(key = "current_theme") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = themeState.selectedState.emoji,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Theme",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = themeState.selectedState.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = themeState.selectedState.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item(key = "select_theme_title") {
                val isDynamicEnabled = themeState.useDynamicColor &&
                    com.kyilmaz.neurocomet.ui.theme.isDynamicColorAvailable()
                Text(
                    text = if (isDynamicEnabled) "Custom Themes (Disabled)" else "Select a Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDynamicEnabled)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            item(key = "select_theme_desc") {
                val isDynamicEnabled = themeState.useDynamicColor &&
                    com.kyilmaz.neurocomet.ui.theme.isDynamicColorAvailable()
                Text(
                    text = if (isDynamicEnabled)
                        "Custom themes are disabled while Dynamic Colors is enabled. Turn off Dynamic Colors in Quick Settings below to use custom neuro-state themes."
                    else
                        "Themes are designed for specific neurodivergent needs and moods. Expand a category to see available options.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Theme picker with categories
            item(key = "theme_picker") {
                NeuroThemePicker(
                    selectedState = themeState.selectedState,
                    onStateSelected = { themeViewModel.setSelectedState(it) },
                    isSecretUnlocked = themeState.rainbowBrainUnlocked,
                    isDynamicColorEnabled = themeState.useDynamicColor &&
                        com.kyilmaz.neurocomet.ui.theme.isDynamicColorAvailable()
                )
            }

            // Quick access to common settings
            item(key = "spacer_quick_settings") { Spacer(Modifier.padding(8.dp)) }
            item(key = "quick_settings_title") {
                Text(
                    text = "Quick Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item(key = "quick_settings_toggles") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dynamic Color toggle (Material You / M3E)
                    SettingsToggle(
                        title = "Dynamic Colors",
                        description = if (com.kyilmaz.neurocomet.ui.theme.isDynamicColorAvailable())
                            "Use colors from your device wallpaper (Material You)"
                        else
                            "Requires Android 12 or higher",
                        icon = Icons.Default.Palette,
                        isChecked = themeState.useDynamicColor,
                        onCheckedChange = { themeViewModel.setUseDynamicColor(it) },
                        enabled = com.kyilmaz.neurocomet.ui.theme.isDynamicColorAvailable()
                    )

                    SettingsToggle(
                        title = "Dark Mode",
                        description = "Use a dark theme for low-light environments.",
                        icon = Icons.Default.DarkMode,
                        isChecked = themeState.isDarkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                    SettingsToggle(
                        title = "High Contrast",
                        description = "Maximum contrast for visibility.",
                        icon = Icons.Default.Contrast,
                        isChecked = themeState.isHighContrast,
                        onCheckedChange = { themeViewModel.setIsHighContrast(it) }
                    )
                }
            }

            // Version info with easter egg
            item(key = "spacer_footer") { Spacer(Modifier.padding(24.dp)) }
            item(key = "footer_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Easter egg: tap 7 times within 3 seconds
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime > 3000) {
                                easterEggTapCount = 1
                            } else {
                                easterEggTapCount++
                            }
                            lastTapTime = currentTime

                            if (easterEggTapCount >= 7) {
                                showEasterEggDialog = true
                                easterEggTapCount = 0
                            }
                        }
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NeuroComet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (easterEggTapCount in 3..6) {
                        Text(
                            text = "${7 - easterEggTapCount} more taps...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}


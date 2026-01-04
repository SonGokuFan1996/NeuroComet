package com.kyilmaz.neurocomet

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import com.kyilmaz.neurocomet.ui.components.PhoneNumberTextField
import com.kyilmaz.neurocomet.ui.components.PhoneFormat
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.AlertDialog
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
    onOpenGames: () -> Unit = {},
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
                    Text(stringResource(R.string.settings_youre_awesome))
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
                    title = stringResource(R.string.settings_section_account),
                    icon = Icons.Outlined.AccountCircle
                )
            }
            item(key = "my_profile") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_my_profile),
                        description = stringResource(R.string.settings_my_profile_desc),
                        icon = Icons.Outlined.AccountCircle,
                        onClick = onOpenMyProfile
                    )
                }
            }
            item(key = "account_card") {
                authUser?.let {
                    AccountInfoCard(user = it, onLogout = onLogout)
                } ?: run {
                    Text(stringResource(R.string.settings_not_authenticated), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    title = stringResource(R.string.settings_section_premium),
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
                                    stringResource(R.string.settings_premium_active),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    stringResource(R.string.settings_premium_thanks),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = stringResource(R.string.settings_premium_active),
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Not premium - show upgrade option
                        SettingsItem(
                            title = stringResource(R.string.settings_go_premium),
                            description = stringResource(R.string.settings_premium_price),
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
                    title = stringResource(R.string.settings_section_appearance),
                    icon = Icons.Default.Palette
                )
            }
            item(key = "appearance_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_theme),
                        description = stringResource(R.string.settings_theme_desc),
                        icon = Icons.Default.Palette,
                        onClick = onOpenThemeSettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = stringResource(R.string.settings_animation),
                        description = if (themeState.animationSettings.disableAllAnimations)
                            stringResource(R.string.settings_animation_disabled)
                        else
                            stringResource(R.string.settings_animation_desc),
                        icon = Icons.Default.Animation,
                        onClick = onOpenAnimationSettings
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = stringResource(R.string.settings_dark_mode),
                        description = stringResource(R.string.settings_dark_mode_desc),
                        icon = Icons.Default.DarkMode,
                        isChecked = themeState.isDarkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = stringResource(R.string.settings_high_contrast),
                        description = stringResource(R.string.settings_high_contrast_desc),
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
                    title = stringResource(R.string.settings_section_privacy),
                    icon = Icons.Default.Lock
                )
            }
            item(key = "privacy_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_privacy),
                        description = stringResource(R.string.settings_privacy_desc),
                        icon = Icons.Default.Lock,
                        onClick = onOpenPrivacySettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = stringResource(R.string.settings_parental),
                        description = if (safetyState.isParentalPinSet) stringResource(R.string.settings_parental_pin_set) else stringResource(R.string.settings_parental_desc),
                        icon = Icons.Default.Shield,
                        onClick = onOpenParentalControls
                    )
                    SettingsDivider()
                    SettingsToggleInCard(
                        title = stringResource(R.string.settings_kid_mode),
                        description = stringResource(R.string.settings_kid_mode_desc),
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
                    title = stringResource(R.string.settings_section_notifications),
                    icon = Icons.Default.Notifications
                )
            }
            item(key = "notifications_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_notif_prefs),
                        description = stringResource(R.string.settings_notif_desc),
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
                    title = stringResource(R.string.settings_content_filters),
                    icon = Icons.Default.PlayCircle
                )
            }
            item(key = "content_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_content_filters),
                        description = stringResource(R.string.settings_content_filters_desc),
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
                    title = stringResource(R.string.settings_section_accessibility),
                    icon = Icons.Default.Accessibility,
                    subtitle = null
                )
            }
            item(key = "accessibility_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.settings_text_display),
                        description = stringResource(R.string.settings_text_display_desc),
                        icon = Icons.Default.TextFields,
                        onClick = onOpenFontSettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = stringResource(R.string.settings_reduce_motion),
                        description = stringResource(R.string.settings_reduce_motion_desc),
                        icon = Icons.Default.Accessibility,
                        onClick = onOpenAccessibilitySettings
                    )
                    SettingsDivider()
                    SettingsItem(
                        title = stringResource(R.string.settings_break_reminders),
                        description = stringResource(R.string.settings_break_reminders_desc),
                        icon = Icons.Default.Spa,
                        onClick = onOpenWellbeingSettings
                    )
                    SettingsDivider()
                    ScreenTimeoutToggle()
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // TIPS & HELP
            // ═══════════════════════════════════════════════════════════════
            item(key = "spacer_before_tips") { Spacer(Modifier.height(8.dp)) }
            item(key = "tips_header") {
                SettingsSectionHeader(
                    title = stringResource(R.string.settings_section_support),
                    icon = Icons.Default.Star,
                    subtitle = null
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
                                text = stringResource(R.string.tutorial_title),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.tutorial_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RestartTutorialButton()
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // GAMES SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "spacer_before_games") { Spacer(Modifier.height(8.dp)) }
            item(key = "games_header") {
                SettingsSectionHeader(
                    title = stringResource(R.string.games_hub_title),
                    icon = Icons.Default.SportsEsports,
                    subtitle = stringResource(R.string.games_subtitle)
                )
            }
            item(key = "games_card") {
                SettingsCard {
                    SettingsItem(
                        title = stringResource(R.string.games_play_now),
                        description = stringResource(R.string.games_play_now_desc),
                        icon = Icons.Default.SportsEsports,
                        onClick = onOpenGames
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // DEVELOPER OPTIONS (if enabled)
            // ═══════════════════════════════════════════════════════════════
            if (canShowDevOptions) {
                item(key = "spacer_before_dev") { Spacer(Modifier.height(8.dp)) }
                item(key = "dev_header") {
                    SettingsSectionHeader(
                        title = stringResource(R.string.settings_section_developer),
                        icon = Icons.Default.Build,
                        subtitle = stringResource(R.string.settings_developer_subtitle)
                    )
                }
                item(key = "dev_card") {
                    SettingsCard {
                        // Fake Premium Toggle for testing
                        SettingsToggle(
                            title = stringResource(R.string.settings_fake_premium),
                            description = if (isFakePremiumEnabled) stringResource(R.string.settings_fake_premium_enabled) else stringResource(R.string.settings_fake_premium_disabled),
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
                            description = stringResource(R.string.settings_dev_options_desc),
                            icon = Icons.Default.Build,
                            onClick = onOpenDevOptions
                        )
                    }
                }

                // Phone Number Format Testing Card
                item(key = "dev_phone_test") {
                    PhoneFormatTestCard()
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
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (easterEggTapCount in 3..6) {
                        Text(
                            text = stringResource(R.string.settings_more_taps, 7 - easterEggTapCount),
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
                Text(stringResource(R.string.settings_logout))
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

    data class TipItemRes(val emoji: String, val titleRes: Int, val descRes: Int)

    val tips = listOf(
        TipItemRes("🔄", R.string.tip_animation_toggle_title, R.string.tip_animation_toggle_desc),
        TipItemRes("🦄", R.string.tip_rainbow_brain_title, R.string.tip_rainbow_brain_desc),
        TipItemRes("🌙", R.string.tip_eye_strain_title, R.string.tip_eye_strain_desc),
        TipItemRes("📖", R.string.tip_readability_title, R.string.tip_readability_desc),
        TipItemRes("🧘", R.string.tip_calm_title, R.string.tip_calm_desc),
        TipItemRes("⚡", R.string.tip_focus_title, R.string.tip_focus_desc),
        TipItemRes("👶", R.string.tip_kids_title, R.string.tip_kids_desc),
        TipItemRes("🎄", R.string.tip_holiday_title, R.string.tip_holiday_desc),
        TipItemRes("🎨", R.string.tip_themes_title, R.string.tip_themes_desc),
        TipItemRes("🔤", R.string.tip_fonts_title, R.string.tip_fonts_desc)
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
                        text = stringResource(R.string.tips_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (expanded) stringResource(R.string.tips_tap_collapse) else stringResource(R.string.tips_tap_expand, tips.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) stringResource(R.string.tips_collapse) else stringResource(R.string.tips_expand),
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
                                text = stringResource(tip.titleRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(tip.descRes),
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
                    Text(stringResource(R.string.settings_youre_awesome))
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
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (easterEggTapCount in 3..6) {
                        Text(
                            text = stringResource(R.string.settings_more_taps, 7 - easterEggTapCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Developer testing card for phone number formatting.
 * Allows testing different phone format styles with live preview.
 */
@Composable
private fun PhoneFormatTestCard() {
    var testPhoneNumber by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(PhoneFormat.US) }
    var isExpanded by remember { mutableStateOf(false) }

    SettingsCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dev_phone_format_test),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.dev_phone_format_test_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Format selector
                    Text(
                        text = stringResource(R.string.dev_phone_format_style),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhoneFormat.entries.forEach { format ->
                            FilterChip(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                                label = {
                                    Text(
                                        when (format) {
                                            PhoneFormat.US -> "US"
                                            PhoneFormat.UK -> "UK"
                                            PhoneFormat.INTERNATIONAL -> "Int'l"
                                            PhoneFormat.SIMPLE -> "Simple"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Phone input field
                    PhoneNumberTextField(
                        value = testPhoneNumber,
                        onValueChange = { testPhoneNumber = it },
                        format = selectedFormat,
                        label = { Text(stringResource(R.string.dev_phone_test_input)) },
                        placeholder = {
                            Text(
                                when (selectedFormat) {
                                    PhoneFormat.US -> "(555) 123-4567"
                                    PhoneFormat.UK -> "07700 900123"
                                    PhoneFormat.INTERNATIONAL -> "+1 555 123 4567"
                                    PhoneFormat.SIMPLE -> "555-123-4567"
                                }
                            )
                        }
                    )

                    // Format info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.dev_phone_format_pattern),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (selectedFormat) {
                                    PhoneFormat.US -> "(XXX) XXX-XXXX"
                                    PhoneFormat.UK -> "XXXXX XXXXXX"
                                    PhoneFormat.INTERNATIONAL -> "+X XXX XXX XXXX"
                                    PhoneFormat.SIMPLE -> "XXX-XXX-XXXX"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen Timeout Toggle for keeping the screen on.
 * Neurodivergent-friendly: Prevents screen dimming during focus time, reading, or meditation.
 */
@Composable
private fun ScreenTimeoutToggle() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var isKeptOn by remember {
        mutableStateOf(
            activity?.window?.attributes?.flags?.and(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            ) != 0
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                activity?.let {
                    isKeptOn = !isKeptOn
                    if (isKeptOn) {
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        it.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isKeptOn) Icons.Default.LightMode else Icons.Default.NightsStay,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_keep_screen_on),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.settings_keep_screen_on_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isKeptOn,
            onCheckedChange = { enabled ->
                activity?.let {
                    isKeptOn = enabled
                    if (enabled) {
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        it.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        )
    }
}


package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextField

private fun displayVersionLabel(versionName: String): String {
    val betaMatch = Regex("^(.*)-beta0*(\\d+)$").matchEntire(versionName)
    if (betaMatch != null) {
        val (baseVersion, betaNumber) = betaMatch.destructured
        return "$baseVersion Beta ${betaNumber.toInt()}"
    }

    val rcMatch = Regex("^(.*)-rc0*(\\d+)$", RegexOption.IGNORE_CASE).matchEntire(versionName)
    if (rcMatch != null) {
        val (baseVersion, rcNumber) = rcMatch.destructured
        return "$baseVersion RC ${rcNumber.toInt()}"
    }

    return versionName
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
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
    onOpenIconCustomization: () -> Unit = {},
    onOpenPrivacySettings: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onOpenContentSettings: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
    onOpenWellbeingSettings: () -> Unit = {},
    onOpenFontSettings: () -> Unit = {},
    onOpenSubscription: () -> Unit = {},
    onOpenMyProfile: () -> Unit = {},
    onOpenGames: () -> Unit = {},
    onOpenBugReport: () -> Unit = {},
    onOpenFeatureRequest: () -> Unit = {},
    onOpenGeneralFeedback: () -> Unit = {},
    onOpenBackupSettings: () -> Unit = {},
    isPremium: Boolean = false,
    isFakePremiumEnabled: Boolean = false,
    onFakePremiumToggle: (Boolean) -> Unit = {},
    themeViewModel: ThemeViewModel,
    showSearchBar: Boolean = false
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val authUser by authViewModel.user.collectAsState()
    val safetyState by safetyViewModel.state.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()
    val displayVersion = remember { displayVersionLabel(BuildConfig.VERSION_NAME) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var searchQuery by remember(showSearchBar) { mutableStateOf("") }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(stringResource(R.string.settings_logout)) },
            text = { Text(stringResource(R.string.settings_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text(stringResource(R.string.settings_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    fun matchesSearch(title: String, subtitle: String = ""): Boolean {
        if (!showSearchBar || searchQuery.isBlank()) return true
        return title.contains(searchQuery, ignoreCase = true) ||
            subtitle.contains(searchQuery, ignoreCase = true)
    }

    val accountSectionVisible = matchesSearch(stringResource(R.string.settings_my_profile), authUser?.name ?: stringResource(R.string.settings_not_authenticated)) ||
        matchesSearch(stringResource(R.string.settings_logout), authUser?.id ?: "")
    val premiumSectionVisible = matchesSearch(
        if (isPremium) stringResource(R.string.settings_premium_active) else stringResource(R.string.settings_go_premium),
        if (isPremium) stringResource(R.string.settings_premium_thanks) else stringResource(R.string.settings_premium_price)
    )
    val appearanceSectionVisible =
        matchesSearch(stringResource(R.string.settings_theme), stringResource(R.string.settings_theme_desc)) ||
        matchesSearch(stringResource(R.string.settings_animation), stringResource(R.string.settings_animation_desc)) ||
        matchesSearch(stringResource(R.string.settings_app_icon), stringResource(R.string.settings_app_icon_desc)) ||
        matchesSearch(stringResource(R.string.settings_dark_mode), stringResource(R.string.settings_dark_mode_desc)) ||
        matchesSearch(stringResource(R.string.settings_high_contrast), stringResource(R.string.settings_high_contrast_desc))
    val privacySectionVisible =
        matchesSearch(stringResource(R.string.settings_privacy), stringResource(R.string.settings_privacy_desc)) ||
        matchesSearch(stringResource(R.string.settings_parental), stringResource(R.string.settings_parental_desc)) ||
        matchesSearch(stringResource(R.string.settings_kid_mode), stringResource(R.string.settings_kid_mode_desc))
    val backupSectionVisible = DeviceAuthority.isAuthorizedDevice(context) &&
        matchesSearch("Backup & Storage", "Back up and restore your data — dev testing only")
    val notificationsSectionVisible = matchesSearch(stringResource(R.string.settings_notif_prefs), stringResource(R.string.settings_notif_desc))
    val contentSectionVisible = matchesSearch(stringResource(R.string.settings_content_filters), stringResource(R.string.settings_content_filters_desc))
    val accessibilitySectionVisible =
        matchesSearch(stringResource(R.string.settings_text_display), stringResource(R.string.settings_text_display_desc)) ||
        matchesSearch(stringResource(R.string.settings_reduce_motion), stringResource(R.string.settings_reduce_motion_desc)) ||
        matchesSearch(stringResource(R.string.settings_break_reminders), stringResource(R.string.settings_break_reminders_desc))
    val gamesSectionVisible = matchesSearch(stringResource(R.string.games_play_now), stringResource(R.string.games_play_now_desc))
    val feedbackSectionVisible =
        matchesSearch(stringResource(R.string.feedback_report_bug_title), stringResource(R.string.feedback_report_bug_desc)) ||
        matchesSearch(stringResource(R.string.feedback_request_feature_title), stringResource(R.string.feedback_request_feature_desc)) ||
        matchesSearch(stringResource(R.string.feedback_send_title), stringResource(R.string.feedback_send_desc))
    val legalSectionVisible =
        matchesSearch(stringResource(R.string.settings_privacy_policy), stringResource(R.string.settings_privacy_policy_desc)) ||
        matchesSearch(stringResource(R.string.settings_terms), stringResource(R.string.settings_terms_desc)) ||
        matchesSearch(stringResource(R.string.app_name), stringResource(R.string.settings_version, displayVersion))
    val developerSectionVisible = canShowDevOptions && (
        matchesSearch(stringResource(R.string.settings_developer_options_group), stringResource(R.string.settings_dev_options_desc)) ||
            matchesSearch(
                stringResource(R.string.settings_fake_premium),
                if (isFakePremiumEnabled) stringResource(R.string.settings_fake_premium_enabled) else stringResource(R.string.settings_fake_premium_disabled)
            )
        )
    val hasAnyResults = accountSectionVisible || premiumSectionVisible || appearanceSectionVisible ||
        privacySectionVisible || backupSectionVisible || notificationsSectionVisible || contentSectionVisible ||
        accessibilitySectionVisible || gamesSectionVisible || feedbackSectionVisible || legalSectionVisible ||
        developerSectionVisible

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search settings") },
                    singleLine = true
                )
            }

            if (accountSectionVisible) {
                SettingsSectionTitle(stringResource(R.string.settings_section_account))
                if (matchesSearch(stringResource(R.string.settings_my_profile), authUser?.name ?: stringResource(R.string.settings_not_authenticated))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_my_profile),
                        subtitle = authUser?.name ?: stringResource(R.string.settings_not_authenticated),
                        onClick = onOpenMyProfile,
                        icon = Icons.Default.Person
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_logout), authUser?.id ?: "")) {
                    SettingsRow(
                        title = stringResource(R.string.settings_logout),
                        subtitle = authUser?.id ?: "",
                        onClick = { showLogoutConfirm = true },
                        icon = Icons.AutoMirrored.Filled.Logout
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            if (premiumSectionVisible) {
                SettingsSectionTitle(stringResource(R.string.settings_section_premium))
                SettingsRow(
                    title = if (isPremium) stringResource(R.string.settings_premium_active) else stringResource(R.string.settings_go_premium),
                    subtitle = if (isPremium) stringResource(R.string.settings_premium_thanks) else stringResource(R.string.settings_premium_price),
                    onClick = if (isPremium) { {} } else onOpenSubscription,
                    enabled = !isPremium,
                    icon = Icons.Default.Star
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            if (appearanceSectionVisible) {
                SettingsSectionTitle(stringResource(R.string.settings_section_appearance))
                if (matchesSearch(stringResource(R.string.settings_theme), stringResource(R.string.settings_theme_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_theme),
                        subtitle = stringResource(R.string.settings_theme_desc),
                        onClick = onOpenThemeSettings,
                        icon = Icons.Default.Palette
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_animation), stringResource(R.string.settings_animation_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_animation),
                        subtitle = stringResource(R.string.settings_animation_desc),
                        onClick = onOpenAnimationSettings,
                        icon = Icons.Default.Animation
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_app_icon), stringResource(R.string.settings_app_icon_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_app_icon),
                        subtitle = stringResource(R.string.settings_app_icon_desc),
                        onClick = onOpenIconCustomization,
                        icon = Icons.Default.AppShortcut
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_dark_mode), stringResource(R.string.settings_dark_mode_desc))) {
                    SettingsToggleRow(
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = stringResource(R.string.settings_dark_mode_desc),
                        checked = themeState.isDarkMode,
                        onCheckedChange = themeViewModel::setDarkMode,
                        icon = Icons.Default.DarkMode
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_high_contrast), stringResource(R.string.settings_high_contrast_desc))) {
                    SettingsToggleRow(
                        title = stringResource(R.string.settings_high_contrast),
                        subtitle = stringResource(R.string.settings_high_contrast_desc),
                        checked = themeState.isHighContrast,
                        onCheckedChange = themeViewModel::setIsHighContrast,
                        icon = Icons.Default.Tonality
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            if (privacySectionVisible) {
                SettingsSectionTitle(stringResource(R.string.settings_section_privacy))
                if (matchesSearch(stringResource(R.string.settings_privacy), stringResource(R.string.settings_privacy_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_privacy),
                        subtitle = stringResource(R.string.settings_privacy_desc),
                        onClick = onOpenPrivacySettings,
                        icon = Icons.Default.Lock
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_parental), stringResource(R.string.settings_parental_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_parental),
                        subtitle = stringResource(R.string.settings_parental_desc),
                        onClick = onOpenParentalControls,
                        icon = Icons.Default.Shield
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_kid_mode), stringResource(R.string.settings_kid_mode_desc))) {
                    SettingsToggleRow(
                        title = stringResource(R.string.settings_kid_mode),
                        subtitle = stringResource(R.string.settings_kid_mode_desc),
                        checked = safetyState.isKidsMode,
                        onCheckedChange = { enabled ->
                            safetyViewModel.setAudience(if (enabled) Audience.UNDER_13 else Audience.ADULT, app)
                        },
                        icon = Icons.Default.Visibility
                    )
                }
            }

            if (backupSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle("Backup & Storage (Dev)")
                SettingsRow(
                    title = "Backup & Storage",
                    subtitle = "Back up and restore your data — dev testing only",
                    onClick = onOpenBackupSettings,
                    icon = Icons.Default.CloudUpload
                )
            }

            if (notificationsSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_notifications))
                SettingsRow(
                    title = stringResource(R.string.settings_notif_prefs),
                    subtitle = stringResource(R.string.settings_notif_desc),
                    onClick = onOpenNotificationSettings,
                    icon = Icons.Default.Notifications
                )
            }

            if (contentSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_content_filters))
                SettingsRow(
                    title = stringResource(R.string.settings_content_filters),
                    subtitle = stringResource(R.string.settings_content_filters_desc),
                    onClick = onOpenContentSettings,
                    icon = Icons.Default.FilterList
                )
            }

            if (accessibilitySectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_accessibility))
                if (matchesSearch(stringResource(R.string.settings_text_display), stringResource(R.string.settings_text_display_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_text_display),
                        subtitle = stringResource(R.string.settings_text_display_desc),
                        onClick = onOpenFontSettings,
                        icon = Icons.Default.FormatSize
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_reduce_motion), stringResource(R.string.settings_reduce_motion_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_reduce_motion),
                        subtitle = stringResource(R.string.settings_reduce_motion_desc),
                        onClick = onOpenAccessibilitySettings,
                        icon = Icons.Default.Accessibility
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_break_reminders), stringResource(R.string.settings_break_reminders_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_break_reminders),
                        subtitle = stringResource(R.string.settings_break_reminders_desc),
                        onClick = onOpenWellbeingSettings,
                        icon = Icons.Default.Bedtime
                    )
                }
            }

            if (gamesSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.games_hub_title))
                SettingsRow(
                    title = stringResource(R.string.games_play_now),
                    subtitle = stringResource(R.string.games_play_now_desc),
                    onClick = onOpenGames,
                    icon = Icons.Default.Games
                )
            }

            if (feedbackSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.feedback_hub_title))
                if (matchesSearch(stringResource(R.string.feedback_report_bug_title), stringResource(R.string.feedback_report_bug_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.feedback_report_bug_title),
                        subtitle = stringResource(R.string.feedback_report_bug_desc),
                        onClick = onOpenBugReport,
                        icon = Icons.Default.BugReport
                    )
                }
                if (matchesSearch(stringResource(R.string.feedback_request_feature_title), stringResource(R.string.feedback_request_feature_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.feedback_request_feature_title),
                        subtitle = stringResource(R.string.feedback_request_feature_desc),
                        onClick = onOpenFeatureRequest,
                        icon = Icons.Default.Lightbulb
                    )
                }
                if (matchesSearch(stringResource(R.string.feedback_send_title), stringResource(R.string.feedback_send_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.feedback_send_title),
                        subtitle = stringResource(R.string.feedback_send_desc),
                        onClick = onOpenGeneralFeedback,
                        icon = Icons.Default.Feedback
                    )
                }
            }

            if (legalSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_legal))
                if (matchesSearch(stringResource(R.string.app_name), stringResource(R.string.settings_version, displayVersion))) {
                    SettingsStaticRow(
                        title = stringResource(R.string.app_name),
                        subtitle = stringResource(R.string.settings_version, displayVersion),
                        icon = Icons.Default.Build
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_privacy_policy), stringResource(R.string.settings_privacy_policy_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_privacy_policy),
                        subtitle = stringResource(R.string.settings_privacy_policy_desc),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://neurocomet.github.io/NeuroComet/privacy.html".toUri())
                            context.startActivity(intent)
                        },
                        icon = Icons.Default.Lock
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_terms), stringResource(R.string.settings_terms_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_terms),
                        subtitle = stringResource(R.string.settings_terms_desc),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://neurocomet.github.io/NeuroComet/terms.html".toUri())
                            context.startActivity(intent)
                        },
                        icon = Icons.Default.Description
                    )
                }
            }

            if (developerSectionVisible) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(stringResource(R.string.settings_section_developer))
                if (matchesSearch(stringResource(R.string.settings_developer_options_group), stringResource(R.string.settings_dev_options_desc))) {
                    SettingsRow(
                        title = stringResource(R.string.settings_developer_options_group),
                        subtitle = stringResource(R.string.settings_dev_options_desc),
                        onClick = onOpenDevOptions,
                        icon = Icons.Default.Build
                    )
                }
                if (matchesSearch(stringResource(R.string.settings_fake_premium), if (isFakePremiumEnabled) stringResource(R.string.settings_fake_premium_enabled) else stringResource(R.string.settings_fake_premium_disabled))) {
                    SettingsToggleRow(
                        title = stringResource(R.string.settings_fake_premium),
                        subtitle = if (isFakePremiumEnabled)
                            stringResource(R.string.settings_fake_premium_enabled)
                        else
                            stringResource(R.string.settings_fake_premium_disabled),
                        checked = isFakePremiumEnabled,
                        onCheckedChange = onFakePremiumToggle,
                        icon = Icons.Default.Star
                    )
                }
            }

            if (showSearchBar && searchQuery.isNotBlank() && !hasAnyResults) {
                Text(
                    text = "No matching settings found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSectionTitle(stringResource(R.string.theme_quick_settings))
            SettingsToggleRow(
                title = stringResource(R.string.theme_dynamic_colors),
                subtitle = stringResource(R.string.theme_dynamic_colors_desc),
                checked = themeState.useDynamicColor,
                onCheckedChange = themeViewModel::setUseDynamicColor,
                icon = Icons.Default.Palette
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_dark_mode),
                subtitle = stringResource(R.string.settings_dark_mode_desc),
                checked = themeState.isDarkMode,
                onCheckedChange = themeViewModel::setDarkMode,
                icon = Icons.Default.DarkMode
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_high_contrast),
                subtitle = stringResource(R.string.settings_high_contrast_desc),
                checked = themeState.isHighContrast,
                onCheckedChange = themeViewModel::setIsHighContrast,
                icon = Icons.Default.Tonality
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsStaticRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle.isNotBlank()) Text(subtitle) },
        leadingContent = { icon?.let { Icon(it, contentDescription = null) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle.isNotBlank()) Text(subtitle) },
        leadingContent = { icon?.let { Icon(it, contentDescription = null) } },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle.isNotBlank()) Text(subtitle) },
        leadingContent = { icon?.let { Icon(it, contentDescription = null) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
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
    var showLogoutConfirm by remember { mutableStateOf(false) }

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

    Scaffold(
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
            SettingsSectionTitle(stringResource(R.string.settings_section_account))
            SettingsRow(
                title = stringResource(R.string.settings_my_profile),
                subtitle = authUser?.name ?: stringResource(R.string.settings_not_authenticated),
                onClick = onOpenMyProfile
            )
            SettingsRow(
                title = stringResource(R.string.settings_logout),
                subtitle = authUser?.id ?: "",
                onClick = { showLogoutConfirm = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_premium))
            SettingsRow(
                title = if (isPremium) stringResource(R.string.settings_premium_active) else stringResource(R.string.settings_go_premium),
                subtitle = if (isPremium) stringResource(R.string.settings_premium_thanks) else stringResource(R.string.settings_premium_price),
                onClick = if (isPremium) { {} } else onOpenSubscription,
                enabled = !isPremium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_appearance))
            SettingsRow(
                title = stringResource(R.string.settings_theme),
                subtitle = stringResource(R.string.settings_theme_desc),
                onClick = onOpenThemeSettings
            )
            SettingsRow(
                title = stringResource(R.string.settings_animation),
                subtitle = stringResource(R.string.settings_animation_desc),
                onClick = onOpenAnimationSettings
            )
            SettingsRow(
                title = stringResource(R.string.settings_app_icon),
                subtitle = stringResource(R.string.settings_app_icon_desc),
                onClick = onOpenIconCustomization
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_dark_mode),
                subtitle = stringResource(R.string.settings_dark_mode_desc),
                checked = themeState.isDarkMode,
                onCheckedChange = themeViewModel::setDarkMode
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_high_contrast),
                subtitle = stringResource(R.string.settings_high_contrast_desc),
                checked = themeState.isHighContrast,
                onCheckedChange = themeViewModel::setIsHighContrast
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_privacy))
            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                subtitle = stringResource(R.string.settings_privacy_desc),
                onClick = onOpenPrivacySettings,
                icon = Icons.Default.Lock
            )
            SettingsRow(
                title = stringResource(R.string.settings_parental),
                subtitle = stringResource(R.string.settings_parental_desc),
                onClick = onOpenParentalControls,
                icon = Icons.Default.Shield
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_kid_mode),
                subtitle = stringResource(R.string.settings_kid_mode_desc),
                checked = safetyState.isKidsMode,
                onCheckedChange = { enabled ->
                    safetyViewModel.setAudience(if (enabled) Audience.UNDER_13 else Audience.ADULT, app)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_notifications))
            SettingsRow(
                title = stringResource(R.string.settings_notif_prefs),
                subtitle = stringResource(R.string.settings_notif_desc),
                onClick = onOpenNotificationSettings,
                icon = Icons.Default.Notifications
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_content_filters))
            SettingsRow(
                title = stringResource(R.string.settings_content_filters),
                subtitle = stringResource(R.string.settings_content_filters_desc),
                onClick = onOpenContentSettings
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_accessibility))
            SettingsRow(
                title = stringResource(R.string.settings_text_display),
                subtitle = stringResource(R.string.settings_text_display_desc),
                onClick = onOpenFontSettings
            )
            SettingsRow(
                title = stringResource(R.string.settings_reduce_motion),
                subtitle = stringResource(R.string.settings_reduce_motion_desc),
                onClick = onOpenAccessibilitySettings
            )
            SettingsRow(
                title = stringResource(R.string.settings_break_reminders),
                subtitle = stringResource(R.string.settings_break_reminders_desc),
                onClick = onOpenWellbeingSettings
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.games_hub_title))
            SettingsRow(
                title = stringResource(R.string.games_play_now),
                subtitle = stringResource(R.string.games_play_now_desc),
                onClick = onOpenGames
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.feedback_hub_title))
            SettingsRow(
                title = stringResource(R.string.feedback_report_bug_title),
                subtitle = stringResource(R.string.feedback_report_bug_desc),
                onClick = onOpenBugReport
            )
            SettingsRow(
                title = stringResource(R.string.feedback_request_feature_title),
                subtitle = stringResource(R.string.feedback_request_feature_desc),
                onClick = onOpenFeatureRequest
            )
            SettingsRow(
                title = stringResource(R.string.feedback_send_title),
                subtitle = stringResource(R.string.feedback_send_desc),
                onClick = onOpenGeneralFeedback
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle(stringResource(R.string.settings_section_legal))
            SettingsRow(
                title = stringResource(R.string.settings_privacy_policy),
                subtitle = stringResource(R.string.settings_privacy_policy_desc),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://songokufan1996.github.io/NeuroComet/privacy.html"))
                    context.startActivity(intent)
                },
                icon = Icons.Default.Lock
            )
            SettingsRow(
                title = stringResource(R.string.settings_terms),
                subtitle = stringResource(R.string.settings_terms_desc),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://songokufan1996.github.io/NeuroComet/terms.html"))
                    context.startActivity(intent)
                }
            )

            if (canShowDevOptions) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsSectionTitle(stringResource(R.string.settings_section_developer))
                SettingsRow(
                    title = stringResource(R.string.settings_developer_options_group),
                    subtitle = stringResource(R.string.settings_dev_options_desc),
                    onClick = onOpenDevOptions,
                    icon = Icons.Default.Build
                )
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
                onCheckedChange = themeViewModel::setDarkMode
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_high_contrast),
                subtitle = stringResource(R.string.settings_high_contrast_desc),
                checked = themeState.isHighContrast,
                onCheckedChange = themeViewModel::setIsHighContrast
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

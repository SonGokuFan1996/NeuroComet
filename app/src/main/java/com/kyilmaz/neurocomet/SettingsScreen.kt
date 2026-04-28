package com.kyilmaz.neurocomet

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.kyilmaz.neurocomet.ui.design.M3EDesignSystem
import com.kyilmaz.neurocomet.ui.design.M3ETopAppBar

// ═══════════════════════════════════════════════════════════════
// VERSION DISPLAY HELPER
// ═══════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════
// MAIN SETTINGS SCREEN
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onRequireAuth: () -> Unit = {},
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
    val isUserPremium = isPremium || isFakePremiumEnabled
    val contentMaxWidth = canonicalSettingsPaneMaxWidth()
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val authUser by authViewModel.user.collectAsState()
    val isGuestAccount = authUser?.id == "guest_user" || authUser == null
    val safetyState by safetyViewModel.state.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()
    val displayVersion = remember { displayVersionLabel(BuildConfig.VERSION_NAME) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var searchQuery by remember(showSearchBar) { mutableStateOf("") }
    var versionTapCount by remember { mutableStateOf(0) }
    var lastVersionTapTime by remember { mutableStateOf(0L) }
    val devOptions by devOptionsViewModel.options.collectAsState()

    var showPinPromptForKidsToggle by remember { mutableStateOf(false) }
    var pinEntry by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // ── PIN prompt dialog ──
    if (showPinPromptForKidsToggle) {
        AlertDialog(
            onDismissRequest = {
                showPinPromptForKidsToggle = false
                pinEntry = ""
                pinError = null
            },
            title = { Text(stringResource(R.string.settings_pin_required_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.settings_pin_required_desc))
                    OutlinedTextField(
                        value = pinEntry,
                        onValueChange = { pinEntry = it; pinError = null },
                        label = { Text(stringResource(R.string.parental_pin_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    pinError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when (val result = ParentalControlsSettings.verifyPin(context, pinEntry)) {
                        is ParentalControlsSettings.PinVerifyResult.Success -> {
                            safetyViewModel.setAudience(Audience.ADULT, app)
                            showPinPromptForKidsToggle = false
                            pinEntry = ""
                            pinError = null
                        }
                        is ParentalControlsSettings.PinVerifyResult.Incorrect -> {
                            pinError = context.getString(
                                R.string.settings_pin_incorrect_attempts,
                                result.attemptsRemaining
                            )
                            pinEntry = ""
                        }
                        is ParentalControlsSettings.PinVerifyResult.LockedOut -> {
                            val mins = (result.remainingMs / 60_000).coerceAtLeast(1)
                            pinError = context.getString(R.string.settings_pin_locked_minutes, mins)
                            pinEntry = ""
                        }
                        is ParentalControlsSettings.PinVerifyResult.NoPinSet -> {
                            safetyViewModel.setAudience(Audience.ADULT, app)
                            showPinPromptForKidsToggle = false
                            pinEntry = ""
                            pinError = null
                        }
                    }
                }) { Text(stringResource(R.string.settings_pin_verify)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinPromptForKidsToggle = false
                    pinEntry = ""
                    pinError = null
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    // ── Logout confirmation dialog ──
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

    if (showLanguageDialog) {
        var selectedTag by remember { mutableStateOf(LanguagePreferences.getCurrentTag(context)) }
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language_title)) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    Text(
                        stringResource(R.string.settings_language_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn {
                        items(LanguagePreferences.SUPPORTED.size) { i ->
                            val lang = LanguagePreferences.SUPPORTED[i]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTag = lang.tag }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = lang.tag == selectedTag,
                                    onClick = { selectedTag = lang.tag },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (lang.tag.isBlank())
                                        stringResource(R.string.settings_language_system_default)
                                    else lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    LanguagePreferences.apply(context, selectedTag)
                    showLanguageDialog = false
                }) {
                    Text(stringResource(R.string.settings_language_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.settings_language_cancel))
                }
            },
        )
    }

    // ── Search helper ──
    fun matchesSearch(title: String, subtitle: String = ""): Boolean {
        if (!showSearchBar || searchQuery.isBlank()) return true
        return title.contains(searchQuery, ignoreCase = true) ||
            subtitle.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            M3ETopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth),
                contentPadding = PaddingValues(
                    start = M3EDesignSystem.Spacing.screenHorizontal,
                    end = M3EDesignSystem.Spacing.screenHorizontal,
                    top = 0.dp,
                    bottom = M3EDesignSystem.Spacing.bottomNavPadding
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ── Search bar (optional) ──
                if (showSearchBar) {
                    item(key = "search_bar") {
                        SettingsSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ── Account section ──
                val accountPrimaryTitle = if (isGuestAccount) context.getString(R.string.auth_sign_in) else context.getString(R.string.settings_my_profile)
                val accountPrimarySubtitle = if (isGuestAccount) {
                    context.getString(R.string.settings_sign_in_desc)
                } else {
                    authUser?.name ?: context.getString(R.string.settings_not_authenticated)
                }
                if (matchesSearch(accountPrimaryTitle, accountPrimarySubtitle) ||
                    (!isGuestAccount && matchesSearch(context.getString(R.string.settings_logout)))
                ) {
                    item(key = "section_account") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_account))
                    }
                    if (matchesSearch(accountPrimaryTitle, accountPrimarySubtitle)) {
                        item(key = "account_primary") {
                            SettingsNavRow(
                                icon = if (isGuestAccount) Icons.Default.Lock else Icons.Default.Person,
                                title = accountPrimaryTitle,
                                subtitle = accountPrimarySubtitle,
                                onClick = if (isGuestAccount) onRequireAuth else onOpenMyProfile
                            )
                        }
                    }
                    if (!isGuestAccount && matchesSearch(context.getString(R.string.settings_logout), authUser?.id ?: "")) {
                        item(key = "account_logout") {
                            SettingsNavRow(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = stringResource(R.string.settings_logout),
                                subtitle = authUser?.id ?: "",
                                onClick = { showLogoutConfirm = true }
                            )
                        }
                    }
                    item(key = "div_account") { SettingsDivider() }
                }

                // ── Premium section ──
                val premiumTitle = if (isUserPremium) context.getString(R.string.settings_premium_active) else context.getString(R.string.settings_go_premium)
                val premiumSubtitle = if (isUserPremium) context.getString(R.string.settings_premium_thanks) else context.getString(R.string.settings_premium_price)
                if (matchesSearch(premiumTitle, premiumSubtitle)) {
                    item(key = "section_premium") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_premium))
                    }
                    item(key = "premium_row") {
                        SettingsNavRow(
                            icon = Icons.Default.Star,
                            title = premiumTitle,
                            subtitle = premiumSubtitle,
                            onClick = if (isUserPremium) { {} } else onOpenSubscription,
                            enabled = !isUserPremium
                        )
                    }
                    item(key = "div_premium") { SettingsDivider() }
                }

                // ── Appearance section ──
                val themeTitle = context.getString(R.string.settings_theme)
                val themeDesc = context.getString(R.string.settings_theme_desc)
                val animTitle = context.getString(R.string.settings_animation)
                val animDesc = context.getString(R.string.settings_animation_desc)
                val iconTitle = context.getString(R.string.settings_app_icon)
                val iconDesc = context.getString(R.string.settings_app_icon_desc)
                val darkTitle = context.getString(R.string.settings_dark_mode)
                val darkDesc = context.getString(R.string.settings_dark_mode_desc)
                val hcTitle = context.getString(R.string.settings_high_contrast)
                val hcDesc = context.getString(R.string.settings_high_contrast_desc)
                val appearanceVisible = matchesSearch(themeTitle, themeDesc) ||
                    matchesSearch(animTitle, animDesc) || matchesSearch(iconTitle, iconDesc) ||
                    matchesSearch(darkTitle, darkDesc) || matchesSearch(hcTitle, hcDesc)

                if (appearanceVisible) {
                    item(key = "section_appearance") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))
                    }
                    if (matchesSearch(themeTitle, themeDesc)) {
                        item(key = "appearance_theme") {
                            SettingsNavRow(
                                icon = Icons.Default.Palette,
                                title = themeTitle,
                                subtitle = if (isUserPremium) themeDesc else stringResource(R.string.settings_theme_premium_locked),
                                onClick = if (isUserPremium) onOpenThemeSettings else onOpenSubscription,
                                trailingContent = {
                                    if (!isUserPremium) {
                                        Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.accessibility_locked), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            )
                        }
                    }
                    if (matchesSearch(animTitle, animDesc)) {
                        item(key = "appearance_anim") {
                            SettingsNavRow(
                                icon = Icons.Default.Animation,
                                title = animTitle,
                                subtitle = animDesc,
                                onClick = onOpenAnimationSettings
                            )
                        }
                    }
                    if (matchesSearch(iconTitle, iconDesc)) {
                        item(key = "appearance_icon") {
                            SettingsNavRow(
                                icon = Icons.Default.AppShortcut,
                                title = iconTitle,
                                subtitle = iconDesc,
                                onClick = onOpenIconCustomization
                            )
                        }
                    }
                    if (matchesSearch(darkTitle, darkDesc)) {
                        item(key = "appearance_dark") {
                            SettingsSwitchRow(
                                icon = Icons.Default.DarkMode,
                                title = darkTitle,
                                subtitle = darkDesc,
                                checked = themeState.isDarkMode,
                                onCheckedChange = themeViewModel::setDarkMode
                            )
                        }
                    }
                    if (matchesSearch(hcTitle, hcDesc)) {
                        item(key = "appearance_hc") {
                            SettingsSwitchRow(
                                icon = Icons.Default.Tonality,
                                title = hcTitle,
                                subtitle = hcDesc,
                                checked = themeState.isHighContrast,
                                onCheckedChange = themeViewModel::setIsHighContrast
                            )
                        }
                    }
                    item(key = "div_appearance") { SettingsDivider() }
                }

                // ── Privacy section ──
                val privTitle = context.getString(R.string.settings_privacy)
                val privDesc = context.getString(R.string.settings_privacy_desc)
                val parentTitle = context.getString(R.string.settings_parental)
                val parentDesc = context.getString(R.string.settings_parental_desc)
                val kidTitle = context.getString(R.string.settings_kid_mode)
                val kidDesc = context.getString(R.string.settings_kid_mode_desc)
                val privacyVisible = matchesSearch(privTitle, privDesc) ||
                    matchesSearch(parentTitle, parentDesc) || matchesSearch(kidTitle, kidDesc)

                if (privacyVisible) {
                    item(key = "section_privacy") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_privacy))
                    }
                    if (matchesSearch(privTitle, privDesc)) {
                        item(key = "privacy_main") {
                            SettingsNavRow(
                                icon = Icons.Default.Lock,
                                title = privTitle,
                                subtitle = privDesc,
                                onClick = onOpenPrivacySettings
                            )
                        }
                    }
                    if (matchesSearch(parentTitle, parentDesc)) {
                        item(key = "privacy_parental") {
                            SettingsNavRow(
                                icon = Icons.Default.Shield,
                                title = parentTitle,
                                subtitle = parentDesc,
                                onClick = onOpenParentalControls
                            )
                        }
                    }
                    if (matchesSearch(kidTitle, kidDesc)) {
                        item(key = "privacy_kid") {
                            SettingsSwitchRow(
                                icon = Icons.Default.Visibility,
                                title = kidTitle,
                                subtitle = kidDesc,
                                checked = safetyState.isKidsMode,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        safetyViewModel.setAudience(Audience.UNDER_13, app)
                                    } else {
                                        if (ParentalControlsSettings.isPinSet(context)) {
                                            showPinPromptForKidsToggle = true
                                        } else {
                                            safetyViewModel.setAudience(Audience.ADULT, app)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    item(key = "div_privacy") { SettingsDivider() }
                }

                // ── Backup section (dev-only) ──
                val backupTitle = context.getString(R.string.backup_title)
                val backupDesc = context.getString(R.string.settings_backup_testing_only)
                if (DeviceAuthority.isAuthorizedDevice(context) && matchesSearch(backupTitle, backupDesc)) {
                    item(key = "section_backup") {
                        SettingsSectionHeader(title = backupTitle)
                    }
                    item(key = "backup_row") {
                        SettingsNavRow(
                            icon = Icons.Default.CloudUpload,
                            title = backupTitle,
                            subtitle = backupDesc,
                            onClick = onOpenBackupSettings
                        )
                    }
                    item(key = "div_backup") { SettingsDivider() }
                }

                // ── Notifications section ──
                val notifTitle = context.getString(R.string.settings_notif_prefs)
                val notifDesc = context.getString(R.string.settings_notif_desc)
                if (matchesSearch(notifTitle, notifDesc)) {
                    item(key = "section_notif") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_notifications))
                    }
                    item(key = "notif_row") {
                        SettingsNavRow(
                            icon = Icons.Default.Notifications,
                            title = notifTitle,
                            subtitle = notifDesc,
                            onClick = onOpenNotificationSettings
                        )
                    }
                    item(key = "div_notif") { SettingsDivider() }
                }

                // ── Content Filters section ──
                val contentTitle = context.getString(R.string.settings_content_filters)
                val contentDesc = context.getString(R.string.settings_content_filters_desc)
                if (matchesSearch(contentTitle, contentDesc)) {
                    item(key = "section_content") {
                        SettingsSectionHeader(title = contentTitle)
                    }
                    item(key = "content_row") {
                        SettingsNavRow(
                            icon = Icons.Default.FilterList,
                            title = contentTitle,
                            subtitle = contentDesc,
                            onClick = onOpenContentSettings
                        )
                    }
                    item(key = "div_content") { SettingsDivider() }
                }

                // ── Accessibility section ──
                val fontTitle = context.getString(R.string.settings_text_display)
                val fontDesc = context.getString(R.string.settings_text_display_desc)
                val motionTitle = context.getString(R.string.settings_reduce_motion)
                val motionDesc = context.getString(R.string.settings_reduce_motion_desc)
                val breakTitle = context.getString(R.string.settings_break_reminders)
                val breakDesc = context.getString(R.string.settings_break_reminders_desc)
                val a11yVisible = matchesSearch(fontTitle, fontDesc) ||
                    matchesSearch(motionTitle, motionDesc) || matchesSearch(breakTitle, breakDesc)

                if (a11yVisible) {
                    item(key = "section_a11y") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_accessibility))
                    }
                    if (matchesSearch(fontTitle, fontDesc)) {
                        item(key = "a11y_font") {
                            SettingsNavRow(
                                icon = Icons.Default.FormatSize,
                                title = fontTitle,
                                subtitle = fontDesc,
                                onClick = onOpenFontSettings
                            )
                        }
                    }
                    if (matchesSearch(motionTitle, motionDesc)) {
                        item(key = "a11y_motion") {
                            SettingsNavRow(
                                icon = Icons.Default.Accessibility,
                                title = motionTitle,
                                subtitle = motionDesc,
                                onClick = onOpenAccessibilitySettings
                            )
                        }
                    }
                    if (matchesSearch(breakTitle, breakDesc)) {
                        item(key = "a11y_break") {
                            SettingsNavRow(
                                icon = Icons.Default.Bedtime,
                                title = breakTitle,
                                subtitle = breakDesc,
                                onClick = onOpenWellbeingSettings
                            )
                        }
                    }

                    // Per-App Language (Android 13+ Per-App Language Preferences,
                    // back-compat via AppCompatDelegate on older APIs).
                    val langTitle = context.getString(R.string.settings_language_title)
                    val langDesc = context.getString(R.string.settings_language_subtitle)
                    if (matchesSearch(langTitle, langDesc)) {
                        item(key = "a11y_language") {
                            val currentTag = remember { LanguagePreferences.getCurrentTag(context) }
                            val currentDisplay = remember(currentTag) {
                                if (currentTag.isBlank())
                                    context.getString(R.string.settings_language_system_default)
                                else
                                    LanguagePreferences.displayNameFor(currentTag)
                            }
                            SettingsNavRow(
                                icon = Icons.Default.Language,
                                title = langTitle,
                                subtitle = "$langDesc — $currentDisplay",
                                onClick = { showLanguageDialog = true }
                            )
                        }
                    }

                    item(key = "div_a11y") { SettingsDivider() }
                }

                // ── Games section ──
                val gamesTitle = context.getString(R.string.games_play_now)
                val gamesDesc = context.getString(R.string.games_play_now_desc)
                if (matchesSearch(gamesTitle, gamesDesc)) {
                    item(key = "section_games") {
                        SettingsSectionHeader(title = stringResource(R.string.games_hub_title))
                    }
                    item(key = "games_row") {
                        SettingsNavRow(
                            icon = Icons.Default.Games,
                            title = gamesTitle,
                            subtitle = gamesDesc,
                            onClick = onOpenGames
                        )
                    }
                    item(key = "div_games") { SettingsDivider() }
                }

                // ── Feedback section ──
                val feedbackHubTitle = context.getString(R.string.feedback_hub_title)
                val feedbackHubDesc = context.getString(R.string.feedback_hub_desc)
                val feedbackVisible = matchesSearch(feedbackHubTitle, feedbackHubDesc)

                if (feedbackVisible) {
                    item(key = "section_feedback") {
                        SettingsSectionHeader(title = stringResource(R.string.feedback_hub_title))
                    }
                    item(key = "feedback_hub") {
                        SettingsNavRow(
                            icon = Icons.Default.Feedback,
                            title = feedbackHubTitle,
                            subtitle = feedbackHubDesc,
                            onClick = onOpenBugReport  // All three callbacks now go to the hub
                        )
                    }
                    item(key = "div_feedback") { SettingsDivider() }
                }

                // ── Legal section ──
                val appName = context.getString(R.string.app_name)
                val versionSubtitle = context.getString(R.string.settings_version, displayVersion)
                val policyTitle = context.getString(R.string.settings_privacy_policy)
                val policyDesc = context.getString(R.string.settings_privacy_policy_desc)
                val termsTitle = context.getString(R.string.settings_terms)
                val termsDesc = context.getString(R.string.settings_terms_desc)
                val legalVisible = matchesSearch(policyTitle, policyDesc) ||
                    matchesSearch(termsTitle, termsDesc) || matchesSearch(appName, versionSubtitle)

                if (legalVisible) {
                    item(key = "section_legal") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_legal))
                    }
                    if (matchesSearch(appName, versionSubtitle)) {
                        item(key = "legal_version") {
                            SettingsNavRow(
                                icon = Icons.Default.Build,
                                title = appName,
                                subtitle = versionSubtitle,
                                onClick = {
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastVersionTapTime > 1500) {
                                        versionTapCount = 1
                                    } else {
                                        versionTapCount++
                                    }
                                    lastVersionTapTime = currentTime

                                    if (versionTapCount >= 7) {
                                        versionTapCount = 0
                                        try {
                                            devOptionsViewModel.setDevMenuEnabled(true)
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.settings_developer_mode_activated),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: SecurityException) {
                                            // Handle unauthorized device
                                            DeviceAuthority.logDevAccessInfo(context)
                                            
                                            // Automatically copy the formatted info to clipboard for convenience
                                            runCatching {
                                                val dHash = DeviceAuthority.computeDeviceHash(context)
                                                val sHash = DeviceAuthority.getAppSignatureHash(context)
                                                val text = "DEVELOPER_DEVICE_HASH=$dHash\nINTERNAL_SIGNATURE_HASH=$sHash"
                                                val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Dev Access Info", text))
                                                
                                                Toast.makeText(
                                                    context,
                                                    "Device not authorized. Info copied to clipboard and printed to Logcat.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }.onFailure {
                                                Log.e("Settings", "Failed to copy dev info to clipboard", it)
                                                Toast.makeText(context, "Device not authorized. See Logcat for info.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                showChevron = false
                            )
                        }
                    }
                    if (matchesSearch(policyTitle, policyDesc)) {
                        item(key = "legal_privacy") {
                            SettingsNavRow(
                                icon = Icons.Default.Lock,
                                title = policyTitle,
                                subtitle = policyDesc,
                                onClick = {
                                    openTrustedExternalUrl(
                                        context = context,
                                        url = AppLinks.legalUrl("privacy"),
                                        allowedHosts = setOf(AppLinks.CANONICAL_DOMAIN)
                                    )
                                }
                            )
                        }
                    }
                    if (matchesSearch(termsTitle, termsDesc)) {
                        item(key = "legal_terms") {
                            SettingsNavRow(
                                icon = Icons.Default.Description,
                                title = termsTitle,
                                subtitle = termsDesc,
                                onClick = {
                                    openTrustedExternalUrl(
                                        context = context,
                                        url = AppLinks.legalUrl("terms"),
                                        allowedHosts = setOf(AppLinks.CANONICAL_DOMAIN)
                                    )
                                }
                            )
                        }
                    }
                    item(key = "div_legal") { SettingsDivider() }
                }

                // ── Developer section ──
                val devGroupTitle = context.getString(R.string.settings_developer_options_group)
                val devGroupDesc = context.getString(R.string.settings_dev_options_desc)
                val fakePremTitle = context.getString(R.string.settings_fake_premium)
                val fakePremSubtitle = if (isFakePremiumEnabled) context.getString(R.string.settings_fake_premium_enabled) else context.getString(R.string.settings_fake_premium_disabled)
                val devVisible = canShowDevOptions && (matchesSearch(devGroupTitle, devGroupDesc) || matchesSearch(fakePremTitle, fakePremSubtitle))

                if (devVisible) {
                    item(key = "section_dev") {
                        SettingsSectionHeader(title = stringResource(R.string.settings_section_developer))
                    }
                    if (matchesSearch(devGroupTitle, devGroupDesc)) {
                        item(key = "dev_options") {
                            SettingsNavRow(
                                icon = Icons.Default.Build,
                                title = devGroupTitle,
                                subtitle = devGroupDesc,
                                onClick = onOpenDevOptions
                            )
                        }
                    }
                    if (matchesSearch(fakePremTitle, fakePremSubtitle)) {
                        item(key = "dev_fake_premium") {
                            SettingsSwitchRow(
                                icon = Icons.Default.Star,
                                title = fakePremTitle,
                                subtitle = fakePremSubtitle,
                                checked = isFakePremiumEnabled,
                                onCheckedChange = onFakePremiumToggle
                            )
                        }
                    }
                }

                // ── No results ──
                if (showSearchBar && searchQuery.isNotBlank()) {
                    val noResults = !matchesSearch(accountPrimaryTitle) && !matchesSearch(premiumTitle) &&
                        !appearanceVisible && !privacyVisible && !matchesSearch(notifTitle) &&
                        !matchesSearch(contentTitle) && !a11yVisible && !matchesSearch(gamesTitle) &&
                        !feedbackVisible && !legalVisible && !devVisible
                    if (noResults) {
                        item(key = "no_results") {
                            Text(
                                text = stringResource(R.string.settings_no_matching_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// THEME SETTINGS SCREEN
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeViewModel: ThemeViewModel,
    isPremium: Boolean = false,
    isFakePremiumEnabled: Boolean = false,
    onBack: () -> Unit
) {
    val isUserPremium = isPremium || isFakePremiumEnabled
    val themeState by themeViewModel.themeState.collectAsState()
    val contentMaxWidth = canonicalSettingsPaneMaxWidth()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            M3ETopAppBar(
                title = {
                    Text(
                        stringResource(R.string.theme_settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth),
                contentPadding = PaddingValues(
                    start = M3EDesignSystem.Spacing.screenHorizontal,
                    end = M3EDesignSystem.Spacing.screenHorizontal,
                    top = 0.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    SettingsSectionHeader(title = stringResource(R.string.theme_quick_settings))
                }
                item {
                    SettingsSwitchRow(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.theme_dynamic_colors),
                        subtitle = stringResource(R.string.theme_dynamic_colors_desc),
                        checked = themeState.useDynamicColor,
                        onCheckedChange = themeViewModel::setUseDynamicColor
                    )
                }
                item {
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = stringResource(R.string.settings_dark_mode_desc),
                        checked = themeState.isDarkMode,
                        onCheckedChange = themeViewModel::setDarkMode
                    )
                }
                item {
                    SettingsSwitchRow(
                        icon = Icons.Default.Tonality,
                        title = stringResource(R.string.settings_high_contrast),
                        subtitle = stringResource(R.string.settings_high_contrast_desc),
                        checked = themeState.isHighContrast,
                        onCheckedChange = themeViewModel::setIsHighContrast
                    )
                }

                item {
                    SettingsSectionHeader(title = stringResource(R.string.settings_neuro_adaptive_themes))
                }

                // List of themes, some are premium
                NeuroState.entries.forEach { neuroState ->
                    val isPremiumTheme = when (neuroState) {
                        NeuroState.DEFAULT, NeuroState.HYPERFOCUS, NeuroState.CALM, NeuroState.OVERLOAD -> false
                        else -> true
                    }

                    item(key = "theme_${neuroState.name}") {
                        SettingsNavRow(
                            title = stringResource(neuroState.displayNameResId),
                            subtitle = if (isPremiumTheme && !isUserPremium) stringResource(R.string.theme_settings_premium_theme) else stringResource(neuroState.descriptionResId),
                            icon = Icons.Default.Palette,
                            onClick = {
                                if (!isPremiumTheme || isUserPremium) {
                                    themeViewModel.setSelectedState(neuroState)
                                }
                            },
                            showChevron = false,
                            trailingContent = {
                                if (themeState.selectedState == neuroState) {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.accessibility_selected), tint = MaterialTheme.colorScheme.primary)
                                } else if (isPremiumTheme && !isUserPremium) {
                                    Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.accessibility_locked), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// REUSABLE SETTINGS COMPONENTS  (fully self-contained — no M3ESurfaceCard)
// ═══════════════════════════════════════════════════════════════

/**
 * Section header with bold primary-colored title.
 */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
    )
}

/**
 * Search bar card for filtering settings.
 */
@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = M3EDesignSystem.Shapes.ExtraLargeShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            placeholder = { Text(stringResource(R.string.settings_search_placeholder)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent
            )
        )
    }
}

/**
 * A thin divider between settings sections.
 */
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

/**
 * Tinted icon circle — guaranteed to render the glyph visibly.
 */
@Composable
private fun SettingsIconGlyph(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    val resolvedTint = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val resolvedBg = if (enabled) tint.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .size(M3EDesignSystem.AvatarSize.md)
            .clip(M3EDesignSystem.Shapes.SmallShape)
            .background(resolvedBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(M3EDesignSystem.IconSize.md),
            tint = resolvedTint
        )
    }
}

/**
 * A clickable settings row with a leading icon glyph, title, subtitle, and optional chevron.
 * Does NOT use M3ESurfaceCard — renders directly and reliably.
 */
@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showChevron: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val contentAlpha = if (enabled) 1f else 0.55f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = M3EDesignSystem.Spacing.xxxs),
        shape = M3EDesignSystem.Shapes.MediumShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = M3EDesignSystem.Spacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconGlyph(icon = icon, enabled = enabled)

            Spacer(modifier = Modifier.width(M3EDesignSystem.Spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            } else if (showChevron) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * A settings row with a toggle switch.
 * Does NOT use M3ESurfaceCard — renders directly and reliably.
 */
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val iconTint by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250),
        label = "iconTint"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = M3EDesignSystem.Spacing.xxxs),
        shape = M3EDesignSystem.Shapes.MediumShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = M3EDesignSystem.Spacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconGlyph(icon = icon, tint = iconTint)

            Spacer(modifier = Modifier.width(M3EDesignSystem.Spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BACKWARD-COMPAT ALIASES  (keep so other files still compile)
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    SettingsNavRow(
        icon = icon ?: Icons.Default.ChevronRight,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
internal fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null
) {
    SettingsSwitchRow(
        icon = icon ?: Icons.Default.ChevronRight,
        title = title,
        subtitle = subtitle,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
internal fun SettingsSectionTitle(title: String) {
    SettingsSectionHeader(title = title)
}

@Composable
internal fun SettingsStaticRow(
    title: String,
    subtitle: String,
    icon: ImageVector? = null
) {
    SettingsNavRow(
        icon = icon ?: Icons.Default.ChevronRight,
        title = title,
        subtitle = subtitle,
        onClick = {},
        showChevron = false
    )
}

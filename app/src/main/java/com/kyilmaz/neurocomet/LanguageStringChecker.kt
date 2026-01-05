package com.kyilmaz.neurocomet

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*

/**
 * Language String Checker for Developers
 *
 * This utility helps identify which strings exist in each language
 * and which are still missing (falling back to English).
 */

// Supported languages in the app (including neurodivergent-majority regions)
val SUPPORTED_LANGUAGES = listOf(
    "en" to "English",
    "tr" to "Türkçe",
    "es" to "Español",
    "de" to "Deutsch",
    "fr" to "Français",
    "pt" to "Português",
    "ja" to "日本語",
    "ko" to "한국어",
    "zh" to "中文",
    "ar" to "العربية",
    "hi" to "हिन्दी",
    "ur" to "اردو",
    "ru" to "Русский",
    "it" to "Italiano",
    "nl" to "Nederlands",
    "pl" to "Polski",
    "vi" to "Tiếng Việt",
    "th" to "ไทย",
    "id" to "Indonesia",
    "ms" to "Bahasa Melayu",
    // Nordic countries (high neurodivergent diagnosis rates)
    "sv" to "Svenska",
    "da" to "Dansk",
    "nb" to "Norsk",
    "fi" to "Suomi",
    "is" to "Íslenska",
    // Other high-awareness regions
    "iw" to "עברית",
    "el" to "Ελληνικά",
    "cs" to "Čeština",
    "hu" to "Magyar",
    "ro" to "Română",
    "uk" to "Українська"
)

// Key string resources to check (representative sample for core UI)
val CORE_STRINGS_TO_CHECK: List<Pair<Int, String>> = listOf(
    // Core UI
    Pair(R.string.app_name, "app_name"),
    Pair(R.string.nav_feed, "nav_feed"),
    Pair(R.string.nav_explore, "nav_explore"),
    Pair(R.string.nav_messages, "nav_messages"),
    Pair(R.string.nav_notifications, "nav_notifications"),
    Pair(R.string.nav_settings, "nav_settings"),

    // Auth
    Pair(R.string.auth_sign_in, "auth_sign_in"),
    Pair(R.string.auth_sign_up, "auth_sign_up"),
    Pair(R.string.auth_email_label, "auth_email_label"),
    Pair(R.string.auth_password_label, "auth_password_label"),
    Pair(R.string.auth_create_account, "auth_create_account"),

    // Settings
    Pair(R.string.settings_section_appearance, "settings_section_appearance"),
    Pair(R.string.settings_section_privacy, "settings_section_privacy"),
    Pair(R.string.settings_section_notifications, "settings_section_notifications"),
    Pair(R.string.settings_section_accessibility, "settings_section_accessibility"),

    // Explore
    Pair(R.string.explore_title, "explore_title"),
    Pair(R.string.explore_autism, "explore_autism"),
    Pair(R.string.explore_adhd, "explore_adhd"),
    Pair(R.string.explore_anxiety, "explore_anxiety"),
    Pair(R.string.explore_sensory, "explore_sensory"),

    // Games
    Pair(R.string.games_hub_title, "games_hub_title"),
    Pair(R.string.game_bubble_pop, "game_bubble_pop"),
    Pair(R.string.game_fidget_spinner, "game_fidget_spinner"),

    // Feedback
    Pair(R.string.feedback_hub_title, "feedback_hub_title"),
    Pair(R.string.feedback_report_bug_title, "feedback_report_bug_title"),
    Pair(R.string.feedback_request_feature_title, "feedback_request_feature_title")
)

// Developer section string resources to check (includes all developer-related strings)
val DEV_OPTIONS_STRINGS_TO_CHECK: List<Pair<Int, String>> = listOf(
    // Developer Section Header
    Pair(R.string.settings_section_developer, "settings_section_developer"),
    Pair(R.string.settings_developer_subtitle, "settings_developer_subtitle"),
    Pair(R.string.settings_developer_options_group, "settings_developer_options_group"),
    Pair(R.string.settings_dev_options_desc, "settings_dev_options_desc"),

    // Developer Settings Toggles
    Pair(R.string.settings_force_verify_title, "settings_force_verify_title"),
    Pair(R.string.settings_force_verify_subtitle, "settings_force_verify_subtitle"),
    Pair(R.string.settings_fake_premium_title, "settings_fake_premium_title"),
    Pair(R.string.settings_fake_premium_subtitle, "settings_fake_premium_subtitle"),
    Pair(R.string.settings_fake_premium, "settings_fake_premium"),
    Pair(R.string.settings_fake_premium_enabled, "settings_fake_premium_enabled"),
    Pair(R.string.settings_fake_premium_disabled, "settings_fake_premium_disabled"),
    Pair(R.string.settings_mock_interface_title, "settings_mock_interface_title"),
    Pair(R.string.settings_mock_interface_subtitle, "settings_mock_interface_subtitle"),
    Pair(R.string.settings_simulate_badge_title, "settings_simulate_badge_title"),
    Pair(R.string.settings_simulate_badge_subtitle, "settings_simulate_badge_subtitle"),

    // Nuke Database
    Pair(R.string.settings_nuke_db_title, "settings_nuke_db_title"),
    Pair(R.string.settings_nuke_db_subtitle, "settings_nuke_db_subtitle"),
    Pair(R.string.settings_nuke_db_dialog_title, "settings_nuke_db_dialog_title"),
    Pair(R.string.settings_nuke_db_dialog_text, "settings_nuke_db_dialog_text"),
    Pair(R.string.settings_nuke_db_dialog_confirm, "settings_nuke_db_dialog_confirm"),
    Pair(R.string.settings_nuke_db_dialog_cancel, "settings_nuke_db_dialog_cancel"),

    // Language Test
    Pair(R.string.settings_language_test_title, "settings_language_test_title"),
    Pair(R.string.settings_language_test_subtitle, "settings_language_test_subtitle"),

    // Dev Options Screen
    Pair(R.string.dev_options_title, "dev_options_title"),
    Pair(R.string.dev_phone_format_test, "dev_phone_format_test"),
    Pair(R.string.dev_phone_format_test_desc, "dev_phone_format_test_desc"),
    Pair(R.string.dev_phone_format_style, "dev_phone_format_style"),
    Pair(R.string.dev_phone_test_input, "dev_phone_test_input"),
    Pair(R.string.dev_phone_format_pattern, "dev_phone_format_pattern"),

    // Credential Storage Dev Options
    Pair(R.string.dev_credential_title, "dev_credential_title"),
    Pair(R.string.dev_credential_test_save, "dev_credential_test_save"),
    Pair(R.string.dev_credential_test_retrieve, "dev_credential_test_retrieve"),
    Pair(R.string.dev_credential_clear_all, "dev_credential_clear_all"),
    Pair(R.string.dev_credential_session_status, "dev_credential_session_status"),
    Pair(R.string.dev_credential_biometric_status, "dev_credential_biometric_status"),
    Pair(R.string.dev_credential_create_session, "dev_credential_create_session"),
    Pair(R.string.dev_credential_end_session, "dev_credential_end_session"),
    Pair(R.string.dev_credential_extend_session, "dev_credential_extend_session"),

    // Debug Tools
    Pair(R.string.debug_tools_title, "debug_tools_title"),
    Pair(R.string.debug_performance_overlay, "debug_performance_overlay"),
    Pair(R.string.debug_performance_desc, "debug_performance_desc"),
    Pair(R.string.debug_reset_counters, "debug_reset_counters")
)

// General settings string resources to check
val GENERAL_SETTINGS_STRINGS_TO_CHECK: List<Pair<Int, String>> = listOf(
    // Profile Group
    Pair(R.string.settings_profile_group, "settings_profile_group"),
    Pair(R.string.settings_badges_title, "settings_badges_title"),
    Pair(R.string.settings_badges_subtitle, "settings_badges_subtitle"),

    // Visual Comfort Group
    Pair(R.string.settings_visual_comfort_group, "settings_visual_comfort_group"),
    Pair(R.string.settings_text_size_title, "settings_text_size_title"),
    Pair(R.string.settings_dark_mode_title, "settings_dark_mode_title"),
    Pair(R.string.settings_dark_mode_enabled, "settings_dark_mode_enabled"),
    Pair(R.string.settings_dark_mode_disabled, "settings_dark_mode_disabled"),
    Pair(R.string.settings_high_contrast_title, "settings_high_contrast_title"),
    Pair(R.string.settings_high_contrast_subtitle_enabled, "settings_high_contrast_subtitle_enabled"),
    Pair(R.string.settings_high_contrast_subtitle_disabled, "settings_high_contrast_subtitle_disabled"),
    Pair(R.string.settings_neuro_centric_theme_title, "settings_neuro_centric_theme_title"),

    // Premium & Account
    Pair(R.string.settings_go_premium_title, "settings_go_premium_title"),
    Pair(R.string.settings_go_premium_subtitle, "settings_go_premium_subtitle"),
    Pair(R.string.settings_account_group, "settings_account_group"),
    Pair(R.string.settings_logout, "settings_logout"),

    // Verification & Security
    Pair(R.string.settings_verified_human_title, "settings_verified_human_title"),
    Pair(R.string.settings_verified_human_subtitle_verified, "settings_verified_human_subtitle_verified"),
    Pair(R.string.settings_verified_human_subtitle_unverified, "settings_verified_human_subtitle_unverified"),
    Pair(R.string.settings_2fa_title, "settings_2fa_title"),
    Pair(R.string.settings_2fa_subtitle, "settings_2fa_subtitle")
)

data class LanguageStatus(
    val languageCode: String,
    val languageName: String,
    val totalStrings: Int,
    val translatedStrings: Int,
    val missingStrings: List<String>
) {
    val percentage: Float get() = if (totalStrings > 0) translatedStrings.toFloat() / totalStrings else 0f
    val isComplete: Boolean get() = percentage >= 1f
}

/**
 * Check string resources for a specific language.
 * This is a simplified check - in reality you'd need to compare values.
 */
fun checkLanguageStrings(
    context: Context,
    languageCode: String,
    stringsToCheck: List<Pair<Int, String>> = CORE_STRINGS_TO_CHECK
): LanguageStatus {
    val languageName = SUPPORTED_LANGUAGES.find { it.first == languageCode }?.second ?: languageCode

    // For English, everything exists
    if (languageCode == "en") {
        return LanguageStatus(
            languageCode = languageCode,
            languageName = languageName,
            totalStrings = stringsToCheck.size,
            translatedStrings = stringsToCheck.size,
            missingStrings = emptyList()
        )
    }

    // Create a configuration for the target language
    val config = android.content.res.Configuration(context.resources.configuration)
    config.setLocale(Locale.forLanguageTag(languageCode))
    val localizedContext = context.createConfigurationContext(config)

    // Get English strings for comparison
    val englishConfig = android.content.res.Configuration(context.resources.configuration)
    englishConfig.setLocale(Locale.ENGLISH)
    val englishContext = context.createConfigurationContext(englishConfig)

    val missingStrings = mutableListOf<String>()
    var translatedCount = 0

    stringsToCheck.forEach { (resId, name) ->
        try {
            val englishValue = englishContext.getString(resId)
            val localizedValue = localizedContext.getString(resId)

            // If the localized value is the same as English, it might be missing
            // (This is a heuristic - not perfect)
            if (localizedValue != englishValue) {
                translatedCount++
            } else {
                // These strings are often intentionally the same across languages
                val internationalTerms = setOf(
                    "app_name",                     // Brand name
                    "nav_feed",                     // "Feed" is used in many languages
                    "nav_explore",                  // "Explore/Explorer" is similar in many languages
                    "nav_messages",                 // "Messages" is same in many languages
                    "explore_title",                // "Explore/Explorer" is similar in many languages
                    "explore_adhd",                 // ADHD is used internationally
                    "explore_autism",               // Autism/Autisme is similar in many languages
                    "explore_anxiety",              // Anxiety/Anxiété is similar in many languages
                    "explore_sensory",              // Sensory/Sensoriel is similar in many languages
                    "game_fidget_spinner",          // Fidget Spinner is used globally
                    "auth_email_label",             // Email/E-mail is similar in many languages
                    "auth_password_label",          // Password is similar in many languages
                    "auth_sign_in",                 // Sign In is used in many languages
                    "auth_sign_up",                 // Sign Up is used in many languages
                    "auth_create_account",          // Create Account is similar in many languages
                    "game_bubble_pop",              // Often kept as English or similar
                    "games_hub_title",              // Games Hub or similar
                    "feedback_hub_title",           // Feedback Hub or similar
                    "feedback_report_bug_title",    // Bug is often kept in English
                    "feedback_request_feature_title", // Feature is often kept in English
                    "settings_section_notifications", // Notifications is same in many languages
                    "settings_section_appearance",  // Appearance is similar in many languages
                    "settings_section_privacy",     // Privacy is similar in many languages
                    "settings_section_accessibility", // Accessibility is similar in many languages
                    // General Settings - often similar across languages
                    "settings_profile_group",       // Profile/Profil is similar
                    "settings_badges_title",        // Badge is used internationally
                    "settings_account_group",       // Account is similar in many languages
                    "settings_logout",              // Logout is used in many languages
                    "settings_go_premium_title",    // Premium is international
                    "settings_2fa_title",           // 2FA/Two-Factor is often similar
                    // Developer Section - technical terms often kept in English
                    "settings_fake_premium_title",  // "Fake Premium" is technical
                    "settings_fake_premium",        // "Fake Premium" mode
                    "settings_mock_interface_title", // "Mock" is technical jargon
                    "debug_tools_title",            // "Debug" is universal dev term
                    "debug_performance_overlay",    // "Overlay" is technical
                    "debug_reset_counters"          // "Counters" is technical
                )

                if (name in internationalTerms) {
                    translatedCount++ // These can be same
                } else {
                    missingStrings.add(name)
                }
            }
        } catch (_: Exception) {
            missingStrings.add(name)
        }
    }

    return LanguageStatus(
        languageCode = languageCode,
        languageName = languageName,
        totalStrings = stringsToCheck.size,
        translatedStrings = translatedCount,
        missingStrings = missingStrings
    )
}

/**
 * String checker category enum
 */
enum class StringCheckerCategory(val title: String, val icon: @Composable () -> Unit) {
    CORE("Core UI", { Icon(Icons.Outlined.Translate, contentDescription = null) }),
    GENERAL_SETTINGS("General Settings", { Icon(Icons.Outlined.Settings, contentDescription = null) }),
    DEV_OPTIONS("Developer Section", { Icon(Icons.Outlined.Code, contentDescription = null) })
}

/**
 * Unified Language Strings Checker Card for Developer Section
 * Consolidates Core, General Settings, and Dev Options string checkers into one expandable menu
 */
@Composable
fun LanguageStringsCheckerCard() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(StringCheckerCategory.CORE) }
    var languageStatuses by remember { mutableStateOf<Map<StringCheckerCategory, List<LanguageStatus>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf<LanguageStatus?>(null) }

    // Load data when expanded
    LaunchedEffect(expanded, selectedCategory) {
        if (expanded && !languageStatuses.containsKey(selectedCategory)) {
            isLoading = true
            val stringsToCheck = when (selectedCategory) {
                StringCheckerCategory.CORE -> CORE_STRINGS_TO_CHECK
                StringCheckerCategory.GENERAL_SETTINGS -> GENERAL_SETTINGS_STRINGS_TO_CHECK
                StringCheckerCategory.DEV_OPTIONS -> DEV_OPTIONS_STRINGS_TO_CHECK
            }
            val statuses = SUPPORTED_LANGUAGES.map { (code, _) ->
                checkLanguageStrings(context, code, stringsToCheck)
            }
            languageStatuses = languageStatuses + (selectedCategory to statuses)
            isLoading = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Language Strings Checker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val totalStrings = CORE_STRINGS_TO_CHECK.size +
                            GENERAL_SETTINGS_STRINGS_TO_CHECK.size +
                            DEV_OPTIONS_STRINGS_TO_CHECK.size
                        Text(
                            text = "$totalStrings total strings · ${SUPPORTED_LANGUAGES.size} languages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expanded content
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Category tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StringCheckerCategory.entries.forEach { category ->
                            val isSelected = selectedCategory == category
                            val stringCount = when (category) {
                                StringCheckerCategory.CORE -> CORE_STRINGS_TO_CHECK.size
                                StringCheckerCategory.GENERAL_SETTINGS -> GENERAL_SETTINGS_STRINGS_TO_CHECK.size
                                StringCheckerCategory.DEV_OPTIONS -> DEV_OPTIONS_STRINGS_TO_CHECK.size
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text("${category.title} ($stringCount)")
                                },
                                leadingIcon = {
                                    category.icon()
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Content for selected category
                    val currentStatuses = languageStatuses[selectedCategory]

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (currentStatuses != null) {
                        // Summary
                        val completeCount = currentStatuses.count { it.isComplete }
                        val partialCount = currentStatuses.count { it.percentage >= 0.5f && !it.isComplete }
                        val needsWorkCount = currentStatuses.count { it.percentage < 0.5f }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusChip(
                                count = completeCount,
                                label = "Complete",
                                color = Color(0xFF4CAF50)
                            )
                            StatusChip(
                                count = partialCount,
                                label = "Partial",
                                color = Color(0xFFFF9800)
                            )
                            StatusChip(
                                count = needsWorkCount,
                                label = "Needs Work",
                                color = Color(0xFFF44336)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        // Language list
                        currentStatuses.forEach { status ->
                            LanguageStatusRow(
                                status = status,
                                onClick = { selectedLanguage = status }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
    selectedLanguage?.let { status ->
        LanguageDetailDialog(
            status = status,
            onDismiss = { selectedLanguage = null }
        )
    }
}

@Composable
private fun StatusChip(
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun LanguageStatusRow(
    status: LanguageStatus,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Language flag/indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.languageCode.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.languageName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { status.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    status.isComplete -> Color(0xFF4CAF50)
                    status.percentage >= 0.5f -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "${status.translatedStrings}/${status.totalStrings} strings",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // Status icon
        Icon(
            when {
                status.isComplete -> Icons.Filled.CheckCircle
                status.percentage >= 0.5f -> Icons.Outlined.Warning
                else -> Icons.Outlined.Error
            },
            contentDescription = null,
            tint = when {
                status.isComplete -> Color(0xFF4CAF50)
                status.percentage >= 0.5f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )
    }
}

@Composable
private fun LanguageDetailDialog(
    status: LanguageStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = status.languageName,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "(${status.languageCode})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // Progress summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Translated:")
                    Text(
                        "${(status.percentage * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            status.isComplete -> Color(0xFF4CAF50)
                            status.percentage >= 0.5f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { status.percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                if (status.missingStrings.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Missing Strings (${status.missingStrings.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    // Scrollable list of missing strings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Column {
                            status.missingStrings.forEach { stringName ->
                                Text(
                                    text = "• $stringName",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "All key strings translated!",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


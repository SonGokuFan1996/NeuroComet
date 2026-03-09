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
 *
 * COMPREHENSIVE VERSION: Dynamically discovers ALL string resources using reflection.
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

/**
 * String categories for organized checking
 */
enum class StringCategory(val prefix: String, val displayName: String) {
    NAVIGATION("nav_", "Navigation"),
    AUTH("auth_", "Authentication"),
    SETTINGS("settings_", "Settings"),
    EXPLORE("explore_", "Explore"),
    GAMES("game", "Games"),
    FEEDBACK("feedback_", "Feedback"),
    MESSAGES("message", "Messages/DM"),
    STORY("story", "Stories"),
    PROFILE("profile_", "Profile"),
    POST("post_", "Posts"),
    COMMENT("comment_", "Comments"),
    NEURO_STATE("neuro_state_", "Neuro States"),
    BADGE("badge_", "Badges"),
    PARENTAL("parental_", "Parental Controls"),
    SUBSCRIPTION("subscription_", "Subscription"),
    DEV("dev_", "Developer"),
    DEBUG("debug_", "Debug"),
    ACCESSIBILITY("accessibility_", "Accessibility"),
    THEME("theme_", "Themes"),
    FONT("font_", "Fonts"),
    NOTIFICATION("notification_", "Notifications"),
    CALL("call_", "Calls"),
    SAFETY("safety_", "Safety"),
    CONTENT("content_", "Content"),
    COMMON("common_", "Common"),
    ERROR("error_", "Errors"),
    SUCCESS("success_", "Success Messages"),
    TUTORIAL("tutorial_", "Tutorial"),
    WIDGET("widget_", "Widgets"),
    OTHER("", "Other/Misc")
}

/**
 * Dynamically discover ALL string resources using reflection
 */
fun getAllStringResources(): List<Pair<Int, String>> {
    val stringFields = R.string::class.java.fields
    return stringFields.mapNotNull { field ->
        try {
            val resId = field.getInt(null)
            val name = field.name
            Pair(resId, name)
        } catch (_: Exception) {
            null
        }
    }.sortedBy { it.second }
}

/**
 * Get string resources by category
 */
fun getStringsByCategory(category: StringCategory): List<Pair<Int, String>> {
    val allStrings = getAllStringResources()
    return if (category == StringCategory.OTHER) {
        // Get strings that don't match any other category prefix
        val allPrefixes = StringCategory.entries
            .filter { it != StringCategory.OTHER }
            .map { it.prefix }
        allStrings.filter { (_, name) ->
            allPrefixes.none { prefix -> name.startsWith(prefix) }
        }
    } else {
        allStrings.filter { (_, name) -> name.startsWith(category.prefix) }
    }
}

/**
 * Get count of strings per category
 */
fun getStringCountByCategory(): Map<StringCategory, Int> {
    return StringCategory.entries.associateWith { category ->
        getStringsByCategory(category).size
    }
}

// Legacy lists for backward compatibility - now dynamically generated
val CORE_STRINGS_TO_CHECK: List<Pair<Int, String>> by lazy {
    val categories = listOf(
        StringCategory.NAVIGATION,
        StringCategory.AUTH,
        StringCategory.EXPLORE,
        StringCategory.GAMES,
        StringCategory.FEEDBACK
    )
    categories.flatMap { getStringsByCategory(it) }
}

val DEV_OPTIONS_STRINGS_TO_CHECK: List<Pair<Int, String>> by lazy {
    getStringsByCategory(StringCategory.DEV) + getStringsByCategory(StringCategory.DEBUG)
}

val GENERAL_SETTINGS_STRINGS_TO_CHECK: List<Pair<Int, String>> by lazy {
    getStringsByCategory(StringCategory.SETTINGS)
}

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
                // Terms that are often the same or very similar across languages
                // This prevents false positives in translation checking
                val internationalTerms = setOf(
                    // Brand/App names
                    "app_name",                     // Brand name - NeuroComet

                    // Navigation - often kept in English or similar
                    "nav_feed",                     // "Feed" is used in many languages
                    "nav_explore",                  // "Explore/Explorer" is similar

                    // Medical/Technical terms - internationally recognized
                    "explore_adhd",                 // ADHD is used internationally
                    "explore_autism",               // Autism/Autisme is similar
                    "explore_anxiety",              // Anxiety is similar in many languages
                    "explore_sensory",              // Sensory is similar

                    // Tech terms - often kept in English
                    "auth_email_label",             // Email/E-mail is universal
                    "game_fidget_spinner",          // Fidget Spinner is used globally

                    // Developer/Debug terms - always English in dev contexts
                    "debug_tools_title",
                    "debug_performance_overlay",
                    "debug_reset_counters",
                    "dev_options_title",
                    "dev_phone_format_test",
                    "dev_credential_title",
                    "settings_mock_interface_title",
                    "settings_fake_premium",
                    "settings_fake_premium_title",

                    // Premium/subscription - international marketing terms
                    "settings_go_premium_title",
                    "subscription_premium",

                    // Two-factor auth - technical abbreviation
                    "settings_2fa_title",

                    // Numbers/formats that don't change
                    "app_version",

                    // Emoji-based strings that are universal
                    // (strings that are primarily emojis)
                )

                // Also consider strings that contain only symbols, numbers, or emojis
                val isSymbolicOrEmpty = englishValue.all {
                    it.isWhitespace() ||
                    !it.isLetter() ||
                    isEmojiCodePoint(it.code)
                }

                if (name in internationalTerms || isSymbolicOrEmpty) {
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
 * Helper to check if a code point is an emoji
 */
private fun isEmojiCodePoint(codePoint: Int): Boolean {
    return codePoint in 0x1F600..0x1F64F || // Emoticons
           codePoint in 0x1F300..0x1F5FF || // Misc Symbols and Pictographs
           codePoint in 0x1F680..0x1F6FF || // Transport and Map
           codePoint in 0x1F1E0..0x1F1FF || // Flags
           codePoint in 0x2600..0x26FF ||   // Misc symbols
           codePoint in 0x2700..0x27BF ||   // Dingbats
           codePoint in 0xFE00..0xFE0F ||   // Variation Selectors
           codePoint in 0x1F900..0x1F9FF || // Supplemental Symbols and Pictographs
           codePoint in 0x1FA00..0x1FA6F || // Chess Symbols
           codePoint in 0x1FA70..0x1FAFF || // Symbols and Pictographs Extended-A
           codePoint in 0x231A..0x231B ||   // Watch, Hourglass
           codePoint in 0x23E9..0x23F3 ||   // Various symbols
           codePoint in 0x23F8..0x23FA ||   // Various symbols
           codePoint in 0x25AA..0x25AB ||   // Squares
           codePoint in 0x25B6..0x25C0 ||   // Play buttons
           codePoint in 0x25FB..0x25FE ||   // Squares
           codePoint in 0x2614..0x2615 ||   // Umbrella, Hot Beverage
           codePoint in 0x2648..0x2653 ||   // Zodiac
           codePoint in 0x267F..0x267F ||   // Wheelchair
           codePoint in 0x2693..0x2693 ||   // Anchor
           codePoint in 0x26A1..0x26A1 ||   // High Voltage
           codePoint in 0x26AA..0x26AB ||   // Circles
           codePoint in 0x26BD..0x26BE ||   // Soccer, Baseball
           codePoint in 0x26C4..0x26C5 ||   // Snowman, Sun
           codePoint in 0x26CE..0x26CE ||   // Ophiuchus
           codePoint in 0x26D4..0x26D4 ||   // No Entry
           codePoint in 0x26EA..0x26EA ||   // Church
           codePoint in 0x26F2..0x26F3 ||   // Fountain, Golf
           codePoint in 0x26F5..0x26F5 ||   // Sailboat
           codePoint in 0x26FA..0x26FA ||   // Tent
           codePoint in 0x26FD..0x26FD ||   // Fuel Pump
           codePoint == 0x2702 ||            // Scissors
           codePoint == 0x2705 ||            // Check Mark
           codePoint in 0x2708..0x270D ||   // Airplane to Writing Hand
           codePoint == 0x270F ||            // Pencil
           codePoint == 0x2712 ||            // Black Nib
           codePoint == 0x2714 ||            // Check Mark
           codePoint == 0x2716 ||            // X Mark
           codePoint in 0x271D..0x271D ||   // Latin Cross
           codePoint in 0x2721..0x2721 ||   // Star of David
           codePoint in 0x2728..0x2728 ||   // Sparkles
           codePoint in 0x2733..0x2734 ||   // Eight Spoked Asterisk
           codePoint in 0x2744..0x2744 ||   // Snowflake
           codePoint in 0x2747..0x2747 ||   // Sparkle
           codePoint in 0x274C..0x274C ||   // Cross Mark
           codePoint in 0x274E..0x274E ||   // Cross Mark
           codePoint in 0x2753..0x2755 ||   // Question Marks
           codePoint in 0x2757..0x2757 ||   // Exclamation Mark
           codePoint in 0x2763..0x2764 ||   // Heart Exclamation, Heart
           codePoint in 0x2795..0x2797 ||   // Plus, Minus, Divide
           codePoint in 0x27A1..0x27A1 ||   // Right Arrow
           codePoint in 0x27B0..0x27B0 ||   // Curly Loop
           codePoint in 0x27BF..0x27BF ||   // Double Curly Loop
           codePoint in 0x2934..0x2935 ||   // Arrows
           codePoint in 0x2B05..0x2B07 ||   // Arrows
           codePoint in 0x2B1B..0x2B1C ||   // Squares
           codePoint in 0x2B50..0x2B50 ||   // Star
           codePoint in 0x2B55..0x2B55 ||   // Circle
           codePoint in 0x3030..0x3030 ||   // Wavy Dash
           codePoint in 0x303D..0x303D ||   // Part Alternation Mark
           codePoint in 0x3297..0x3297 ||   // Circled Ideograph Congratulation
           codePoint in 0x3299..0x3299      // Circled Ideograph Secret
}

/**
 * String checker category enum - now uses comprehensive categories
 */
enum class StringCheckerCategory(val title: String, val icon: @Composable () -> Unit, val stringCategory: StringCategory?) {
    ALL("All Strings", { Icon(Icons.Outlined.Translate, contentDescription = null) }, null),
    NAVIGATION("Navigation", { Icon(Icons.Outlined.Navigation, contentDescription = null) }, StringCategory.NAVIGATION),
    AUTH("Authentication", { Icon(Icons.Outlined.Login, contentDescription = null) }, StringCategory.AUTH),
    SETTINGS("Settings", { Icon(Icons.Outlined.Settings, contentDescription = null) }, StringCategory.SETTINGS),
    EXPLORE("Explore", { Icon(Icons.Outlined.Explore, contentDescription = null) }, StringCategory.EXPLORE),
    GAMES("Games", { Icon(Icons.Outlined.SportsEsports, contentDescription = null) }, StringCategory.GAMES),
    MESSAGES("Messages", { Icon(Icons.Outlined.Message, contentDescription = null) }, StringCategory.MESSAGES),
    NEURO_STATES("Neuro States", { Icon(Icons.Outlined.Psychology, contentDescription = null) }, StringCategory.NEURO_STATE),
    BADGES("Badges", { Icon(Icons.Outlined.MilitaryTech, contentDescription = null) }, StringCategory.BADGE),
    PARENTAL("Parental", { Icon(Icons.Outlined.FamilyRestroom, contentDescription = null) }, StringCategory.PARENTAL),
    DEV_OPTIONS("Developer", { Icon(Icons.Outlined.Code, contentDescription = null) }, StringCategory.DEV),
    ACCESSIBILITY("Accessibility", { Icon(Icons.Outlined.Accessibility, contentDescription = null) }, StringCategory.ACCESSIBILITY),
    NOTIFICATIONS("Notifications", { Icon(Icons.Outlined.Notifications, contentDescription = null) }, StringCategory.NOTIFICATION),
    CALLS("Calls", { Icon(Icons.Outlined.Call, contentDescription = null) }, StringCategory.CALL),
    TUTORIAL("Tutorial", { Icon(Icons.Outlined.School, contentDescription = null) }, StringCategory.TUTORIAL),
    OTHER("Other", { Icon(Icons.Outlined.MoreHoriz, contentDescription = null) }, StringCategory.OTHER)
}

/**
 * Get strings for a checker category
 */
fun getStringsForCheckerCategory(category: StringCheckerCategory): List<Pair<Int, String>> {
    return if (category == StringCheckerCategory.ALL) {
        getAllStringResources()
    } else {
        category.stringCategory?.let { getStringsByCategory(it) } ?: emptyList()
    }
}

/**
 * Unified Language Strings Checker Card for Developer Section
 * COMPREHENSIVE VERSION: Dynamically discovers ALL string resources
 * Organizes them by category for easy translation tracking
 */
@Composable
fun LanguageStringsCheckerCard() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(StringCheckerCategory.ALL) }
    var languageStatuses by remember { mutableStateOf<Map<StringCheckerCategory, List<LanguageStatus>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf<LanguageStatus?>(null) }

    // Cache the total string count
    val totalStringCount = remember { getAllStringResources().size }
    val categoryCounts = remember {
        StringCheckerCategory.entries.associateWith { cat ->
            getStringsForCheckerCategory(cat).size
        }
    }

    // Load data when expanded
    LaunchedEffect(expanded, selectedCategory) {
        if (expanded && !languageStatuses.containsKey(selectedCategory)) {
            isLoading = true
            val stringsToCheck = getStringsForCheckerCategory(selectedCategory)
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
                            text = "Comprehensive String Checker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalStringCount total strings · ${SUPPORTED_LANGUAGES.size} languages · ${StringCheckerCategory.entries.size} categories",
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
                    // Category tabs - scrollable since we have many categories
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StringCheckerCategory.entries.forEach { category ->
                            val isSelected = selectedCategory == category
                            val stringCount = categoryCounts[category] ?: 0

                            // Only show categories that have strings
                            if (stringCount > 0 || category == StringCheckerCategory.ALL) {
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            "${category.title} ($stringCount)",
                                            maxLines = 1
                                        )
                                    },
                                    leadingIcon = {
                                        category.icon()
                                    }
                                )
                            }
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

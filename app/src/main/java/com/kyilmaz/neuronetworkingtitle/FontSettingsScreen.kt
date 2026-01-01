package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Font Settings Screen
 *
 * Allows users to customize fonts for optimal reading experience based on their
 * neurodivergent needs - dyslexia, ADHD, autism, anxiety, low vision, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit
) {
    val themeState by themeViewModel.themeState.collectAsState()
    val fontSettings = themeState.fontSettings

    var expandedCategory by remember { mutableStateOf<FontCategory?>(FontCategory.DYSLEXIA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Font & Reading") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === QUICK PRESETS ===
            item {
                Text(
                    "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "One-tap settings optimized for your needs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        PresetChip(
                            label = "📖 Dyslexia",
                            isSelected = fontSettings == FontPresets.dyslexiaFriendly,
                            onClick = { themeViewModel.applyFontPreset(FontPresets.dyslexiaFriendly) }
                        )
                    }
                    item {
                        PresetChip(
                            label = "⚡ ADHD",
                            isSelected = fontSettings == FontPresets.adhdFocus,
                            onClick = { themeViewModel.applyFontPreset(FontPresets.adhdFocus) }
                        )
                    }
                    item {
                        PresetChip(
                            label = "🔄 Autism",
                            isSelected = fontSettings == FontPresets.autismConsistent,
                            onClick = { themeViewModel.applyFontPreset(FontPresets.autismConsistent) }
                        )
                    }
                    item {
                        PresetChip(
                            label = "👁️ Low Vision",
                            isSelected = fontSettings == FontPresets.lowVision,
                            onClick = { themeViewModel.applyFontPreset(FontPresets.lowVision) }
                        )
                    }
                    item {
                        PresetChip(
                            label = "🌿 Calm",
                            isSelected = fontSettings == FontPresets.anxietyCalm,
                            onClick = { themeViewModel.applyFontPreset(FontPresets.anxietyCalm) }
                        )
                    }
                }
            }

            // === LIVE PREVIEW ===
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                FontPreviewCard(fontSettings = fontSettings)
            }

            // === FONT SELECTION BY CATEGORY ===
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Choose Your Font",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Group fonts by category
            FontCategory.entries.forEach { category ->
                val fontsInCategory = AccessibilityFont.entries.filter { it.category == category }
                if (fontsInCategory.isNotEmpty()) {
                    item {
                        FontCategorySection(
                            category = category,
                            fonts = fontsInCategory,
                            selectedFont = fontSettings.selectedFont,
                            isExpanded = expandedCategory == category,
                            onToggle = {
                                expandedCategory = if (expandedCategory == category) null else category
                            },
                            onFontSelected = { font ->
                                themeViewModel.setSelectedFont(font)
                            }
                        )
                    }
                }
            }

            // === FINE-TUNE SETTINGS ===
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Fine-Tune Reading Experience",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Letter Spacing
            item {
                SettingSliderCard(
                    icon = Icons.Default.SpaceBar,
                    title = "Letter Spacing",
                    description = "Wider spacing can help with dyslexia and visual crowding",
                    options = LetterSpacingLevel.entries,
                    selectedOption = fontSettings.letterSpacing,
                    optionLabel = { it.displayName },
                    onOptionSelected = { themeViewModel.setLetterSpacing(it) }
                )
            }

            // Line Height
            item {
                SettingSliderCard(
                    icon = Icons.Default.VerticalAlignCenter,
                    title = "Line Height",
                    description = "More vertical space between lines reduces visual stress",
                    options = LineHeightLevel.entries,
                    selectedOption = fontSettings.lineHeight,
                    optionLabel = { it.displayName },
                    onOptionSelected = { themeViewModel.setLineHeight(it) }
                )
            }

            // Font Weight
            item {
                SettingSliderCard(
                    icon = Icons.Default.FormatSize,
                    title = "Font Weight",
                    description = "Bolder text can improve readability for some users",
                    options = FontWeightLevel.entries,
                    selectedOption = fontSettings.fontWeight,
                    optionLabel = { it.displayName },
                    onOptionSelected = { themeViewModel.setFontWeight(it) }
                )
            }

            // Bottom spacing
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun FontPreviewCard(fontSettings: FontSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simulate a message conversation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Hey! How are you doing today? 💜",
                        style = NeuroDivergentTypography.messageBody(fontSettings),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "I'm doing great! This font is so much easier to read! ✨",
                        style = NeuroDivergentTypography.messageBody(fontSettings),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                text = "Current: ${fontSettings.selectedFont.displayName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FontCategorySection(
    category: FontCategory,
    fonts: List<AccessibilityFont>,
    selectedFont: AccessibilityFont,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onFontSelected: (AccessibilityFont) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(category.emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            category.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${fonts.size} font${if (fonts.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    fonts.forEach { font ->
                        FontOption(
                            font = font,
                            isSelected = selectedFont == font,
                            onClick = { onFontSelected(font) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FontOption(
    font: AccessibilityFont,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    // Use proper contrast colors based on selection state
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val secondaryTextColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    .then(
                        if (!isSelected) Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${font.emoji} ${font.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
                Text(
                    font.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun <T> SettingSliderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Option chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(options) { option ->
                    FilterChip(
                        selected = option == selectedOption,
                        onClick = { onOptionSelected(option) },
                        label = { Text(optionLabel(option), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}


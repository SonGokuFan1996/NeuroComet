package com.kyilmaz.neurocomet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Types of parental control restrictions.
 */
enum class RestrictionType {
    FEATURE_BLOCKED,    // Feature is disabled by parental controls
    BEDTIME_ACTIVE,     // App is blocked during bedtime hours
    TIME_LIMIT_REACHED, // Daily time limit has been reached
    CONTENT_FILTERED    // Content is filtered due to content filter level
}

/**
 * A full-screen composable that displays when content or features are blocked
 * by parental controls.
 */
@Composable
fun ParentalBlockedScreen(
    restrictionType: RestrictionType,
    featureName: String = "This feature",
    modifier: Modifier = Modifier
) {
    val (icon, title, message) = when (restrictionType) {
        RestrictionType.FEATURE_BLOCKED -> Triple(
            Icons.Default.Lock,
            "$featureName is Restricted",
            "Parental controls have disabled access to this feature. Ask a parent or guardian to unlock it."
        )
        RestrictionType.BEDTIME_ACTIVE -> Triple(
            Icons.Default.NightsStay,
            "Bedtime Mode Active",
            "It's time to rest! The app is currently blocked during bedtime hours. Come back tomorrow!"
        )
        RestrictionType.TIME_LIMIT_REACHED -> Triple(
            Icons.Default.Timer,
            "Daily Limit Reached",
            "You've used all your screen time for today. Take a break and come back tomorrow!"
        )
        RestrictionType.CONTENT_FILTERED -> Triple(
            Icons.Default.Block,
            "Content Not Available",
            "This content has been filtered by parental controls."
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A smaller composable for inline blocked content messages.
 */
@Composable
fun ParentalBlockedBanner(
    restrictionType: RestrictionType,
    featureName: String = "This feature",
    modifier: Modifier = Modifier
) {
    val (icon, message) = when (restrictionType) {
        RestrictionType.FEATURE_BLOCKED -> Icons.Default.Lock to "$featureName is restricted by parental controls"
        RestrictionType.BEDTIME_ACTIVE -> Icons.Default.NightsStay to "Bedtime mode is active"
        RestrictionType.TIME_LIMIT_REACHED -> Icons.Default.Timer to "Daily time limit reached"
        RestrictionType.CONTENT_FILTERED -> Icons.Default.Block to "Content filtered"
    }

    androidx.compose.material3.Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Helper function to check if a feature should be blocked based on parental controls.
 */
fun shouldBlockFeature(
    parentalState: ParentalControlsSettings.ParentalControlState,
    feature: BlockableFeature
): RestrictionType? {
    if (!parentalState.isEnabled) return null

    // Check time-based restrictions first
    if (parentalState.isDuringBedtime) return RestrictionType.BEDTIME_ACTIVE
    if (parentalState.isOverDailyLimit) return RestrictionType.TIME_LIMIT_REACHED

    // Check feature-specific blocks
    return when (feature) {
        BlockableFeature.DMS -> if (parentalState.blockDMs) RestrictionType.FEATURE_BLOCKED else null
        BlockableFeature.EXPLORE -> if (parentalState.blockExplore) RestrictionType.FEATURE_BLOCKED else null
        BlockableFeature.POSTING -> if (parentalState.blockPosting) RestrictionType.FEATURE_BLOCKED else null
        BlockableFeature.FOLLOWS -> if (parentalState.requireApprovalForFollows) RestrictionType.FEATURE_BLOCKED else null
    }
}

enum class BlockableFeature {
    DMS,
    EXPLORE,
    POSTING,
    FOLLOWS
}


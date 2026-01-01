package com.kyilmaz.neuronetworkingtitle

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Messages (DM) UI design helpers.
 *
 * Goals:
 * - Reduce cognitive load (clear hierarchy, consistent spacing)
 * - Reduce sensory overload (muted separators, calm containers)
 * - Increase motor accessibility (comfortable touch targets)
 */
object MessagesTokens {
    val touchTarget: Dp = 48.dp
    val pagePadding: Dp = 16.dp
    val itemSpacing: Dp = 10.dp
    val chipHeight: Dp = 34.dp
    val avatarSize: Dp = 52.dp
    val cornerLarge: Dp = 24.dp
    val cornerMedium: Dp = 18.dp
}

@Composable
fun messagesContainerColor(): Color = MaterialTheme.colorScheme.surface

@Composable
fun messagesMutedTextColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant


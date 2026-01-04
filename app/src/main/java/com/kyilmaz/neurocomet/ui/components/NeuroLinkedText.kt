package com.kyilmaz.neurocomet.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.util.regex.Pattern

/**
 * Link types for different visual treatments
 */
enum class LinkType {
    URL,            // External web links
    MENTION,        // @username mentions
    HASHTAG,        // #hashtag tags
    EMAIL,          // Email addresses
    PHONE,          // Phone numbers
    INTERNAL        // Internal app navigation
}

/**
 * Data class representing a detected link in text
 */
data class DetectedLink(
    val text: String,
    val url: String,
    val type: LinkType,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Link style configuration for neurodivergent-friendly design
 * - Clear visual distinction from regular text
 * - Consistent, predictable styling
 * - Reduced visual noise while maintaining clarity
 * - High contrast options
 */
data class NeuroLinkStyle(
    val urlColor: Color,
    val mentionColor: Color,
    val hashtagColor: Color,
    val emailColor: Color,
    val phoneColor: Color,
    val internalColor: Color,
    val useUnderline: Boolean = false,  // Some neurodivergent users find underlines distracting
    val useBoldWeight: Boolean = true,  // Makes links easier to identify
    val useSubtleBackground: Boolean = true,  // Gentle highlight for link areas
    val backgroundAlpha: Float = 0.08f
)

/**
 * Neurodivergent-friendly linked text component
 *
 * Features:
 * - Automatic link detection (URLs, mentions, hashtags, emails, phones)
 * - Custom link styling with reduced visual noise
 * - Gentle hover/press feedback
 * - Clear visual distinction without overwhelming
 * - Predictable interaction patterns
 * - Screen reader friendly
 */
@Composable
fun NeuroLinkedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    linkStyle: NeuroLinkStyle = defaultNeuroLinkStyle(),
    onLinkClick: ((DetectedLink) -> Unit)? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Detect all links in the text
    val links = remember(text) { detectLinks(text) }

    // Build annotated string with clickable links
    val annotatedText = remember(text, links, linkStyle) {
        buildLinkedAnnotatedString(text, links, linkStyle)
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = style.copy(color = MaterialTheme.colorScheme.onSurface),
        maxLines = maxLines,
        onClick = { offset ->
            // Find which link was clicked
            annotatedText.getStringAnnotations(
                tag = "LINK",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                val linkIndex = annotation.item.toIntOrNull() ?: return@let
                val link = links.getOrNull(linkIndex) ?: return@let

                if (onLinkClick != null) {
                    onLinkClick(link)
                } else {
                    // Default handling
                    handleLinkClick(context, link, uriHandler)
                }
            }
        }
    )
}

/**
 * A more visual link pill component for individual links
 * Great for displaying links in a list or as standalone elements
 */
@Composable
fun NeuroLinkPill(
    text: String,
    url: String,
    type: LinkType = LinkType.URL,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    val backgroundColor = when (type) {
        LinkType.URL -> MaterialTheme.colorScheme.primaryContainer
        LinkType.MENTION -> MaterialTheme.colorScheme.secondaryContainer
        LinkType.HASHTAG -> MaterialTheme.colorScheme.tertiaryContainer
        LinkType.EMAIL -> MaterialTheme.colorScheme.primaryContainer
        LinkType.PHONE -> MaterialTheme.colorScheme.secondaryContainer
        LinkType.INTERNAL -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (type) {
        LinkType.URL -> MaterialTheme.colorScheme.onPrimaryContainer
        LinkType.MENTION -> MaterialTheme.colorScheme.onSecondaryContainer
        LinkType.HASHTAG -> MaterialTheme.colorScheme.onTertiaryContainer
        LinkType.EMAIL -> MaterialTheme.colorScheme.onPrimaryContainer
        LinkType.PHONE -> MaterialTheme.colorScheme.onSecondaryContainer
        LinkType.INTERNAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when (type) {
        LinkType.URL -> "🔗"
        LinkType.MENTION -> "@"
        LinkType.HASHTAG -> "#"
        LinkType.EMAIL -> "✉️"
        LinkType.PHONE -> "📞"
        LinkType.INTERNAL -> "→"
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (onClick != null) {
                    onClick()
                } else {
                    val link = DetectedLink(text, url, type, 0, text.length)
                    handleLinkClick(context, link, uriHandler)
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        androidx.compose.material3.Text(
            text = "$icon $text",
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Creates default neurodivergent-friendly link style using Material3 colors
 */
@Composable
fun defaultNeuroLinkStyle(): NeuroLinkStyle {
    return NeuroLinkStyle(
        urlColor = MaterialTheme.colorScheme.primary,
        mentionColor = MaterialTheme.colorScheme.secondary,
        hashtagColor = MaterialTheme.colorScheme.tertiary,
        emailColor = MaterialTheme.colorScheme.primary,
        phoneColor = MaterialTheme.colorScheme.secondary,
        internalColor = MaterialTheme.colorScheme.tertiary,
        useUnderline = false,
        useBoldWeight = true,
        useSubtleBackground = false,
        backgroundAlpha = 0.1f
    )
}

/**
 * High contrast link style for users who need more visual distinction
 */
@Composable
fun highContrastNeuroLinkStyle(): NeuroLinkStyle {
    return NeuroLinkStyle(
        urlColor = MaterialTheme.colorScheme.primary,
        mentionColor = MaterialTheme.colorScheme.tertiary,
        hashtagColor = MaterialTheme.colorScheme.error,
        emailColor = MaterialTheme.colorScheme.primary,
        phoneColor = MaterialTheme.colorScheme.secondary,
        internalColor = MaterialTheme.colorScheme.tertiary,
        useUnderline = true,
        useBoldWeight = true,
        useSubtleBackground = true,
        backgroundAlpha = 0.15f
    )
}

/**
 * Minimal link style for users who prefer reduced visual noise
 */
@Composable
fun minimalNeuroLinkStyle(): NeuroLinkStyle {
    return NeuroLinkStyle(
        urlColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        mentionColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        hashtagColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        emailColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        phoneColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        internalColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        useUnderline = false,
        useBoldWeight = false,
        useSubtleBackground = false,
        backgroundAlpha = 0f
    )
}

// ============================================================================
// INTERNAL FUNCTIONS
// ============================================================================

private fun detectLinks(text: String): List<DetectedLink> {
    val links = mutableListOf<DetectedLink>()

    // URL pattern (http, https, www)
    val urlPattern = Pattern.compile(
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)|(www\\.[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
        Pattern.CASE_INSENSITIVE
    )

    // Mention pattern (@username)
    val mentionPattern = Pattern.compile("@([\\w._]+)")

    // Hashtag pattern (#hashtag)
    val hashtagPattern = Pattern.compile("#([\\w]+)")

    // Email pattern
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        Pattern.CASE_INSENSITIVE
    )

    // Phone pattern (basic)
    val phonePattern = Pattern.compile(
        "\\+?[0-9]{1,4}?[-.\\s]?\\(?[0-9]{1,3}?\\)?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}"
    )

    // Find URLs
    urlPattern.matcher(text).let { matcher ->
        while (matcher.find()) {
            val matchText = matcher.group()
            val url = if (matchText.startsWith("www")) "https://$matchText" else matchText
            links.add(DetectedLink(matchText, url, LinkType.URL, matcher.start(), matcher.end()))
        }
    }

    // Find mentions
    mentionPattern.matcher(text).let { matcher ->
        while (matcher.find()) {
            val matchText = matcher.group()
            links.add(DetectedLink(matchText, "neurocomet://user/${matcher.group(1)}", LinkType.MENTION, matcher.start(), matcher.end()))
        }
    }

    // Find hashtags
    hashtagPattern.matcher(text).let { matcher ->
        while (matcher.find()) {
            val matchText = matcher.group()
            links.add(DetectedLink(matchText, "neurocomet://hashtag/${matcher.group(1)}", LinkType.HASHTAG, matcher.start(), matcher.end()))
        }
    }

    // Find emails
    emailPattern.matcher(text).let { matcher ->
        while (matcher.find()) {
            val matchText = matcher.group()
            links.add(DetectedLink(matchText, "mailto:$matchText", LinkType.EMAIL, matcher.start(), matcher.end()))
        }
    }

    // Find phone numbers (only if not overlapping with other patterns)
    phonePattern.matcher(text).let { matcher ->
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val overlaps = links.any { it.startIndex <= end && it.endIndex >= start }
            if (!overlaps) {
                val matchText = matcher.group()
                val cleanPhone = matchText.replace(Regex("[^0-9+]"), "")
                links.add(DetectedLink(matchText, "tel:$cleanPhone", LinkType.PHONE, start, end))
            }
        }
    }

    // Sort by start index and remove overlaps
    return links.sortedBy { it.startIndex }.fold(mutableListOf()) { acc, link ->
        if (acc.isEmpty() || acc.last().endIndex <= link.startIndex) {
            acc.add(link)
        }
        acc
    }
}

private fun buildLinkedAnnotatedString(
    text: String,
    links: List<DetectedLink>,
    style: NeuroLinkStyle
): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0

        links.forEachIndexed { index, link ->
            // Add text before this link
            if (link.startIndex > lastIndex) {
                append(text.substring(lastIndex, link.startIndex))
            }

            // Get color for this link type
            val linkColor = when (link.type) {
                LinkType.URL -> style.urlColor
                LinkType.MENTION -> style.mentionColor
                LinkType.HASHTAG -> style.hashtagColor
                LinkType.EMAIL -> style.emailColor
                LinkType.PHONE -> style.phoneColor
                LinkType.INTERNAL -> style.internalColor
            }

            // Add the link with styling
            val linkStyle = SpanStyle(
                color = linkColor,
                fontWeight = if (style.useBoldWeight) FontWeight.Medium else FontWeight.Normal,
                textDecoration = if (style.useUnderline) TextDecoration.Underline else TextDecoration.None,
                background = if (style.useSubtleBackground)
                    linkColor.copy(alpha = style.backgroundAlpha)
                else
                    Color.Transparent
            )

            pushStringAnnotation(tag = "LINK", annotation = index.toString())
            withStyle(linkStyle) {
                append(link.text)
            }
            pop()

            lastIndex = link.endIndex
        }

        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

private fun handleLinkClick(
    context: android.content.Context,
    link: DetectedLink,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    when (link.type) {
        LinkType.URL -> {
            try {
                uriHandler.openUri(link.url)
            } catch (e: Exception) {
                // Fallback to intent
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                    context.startActivity(intent)
                } catch (_: Exception) { }
            }
        }
        LinkType.EMAIL -> {
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(link.url))
                context.startActivity(intent)
            } catch (_: Exception) { }
        }
        LinkType.PHONE -> {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(link.url))
                context.startActivity(intent)
            } catch (_: Exception) { }
        }
        LinkType.MENTION, LinkType.HASHTAG, LinkType.INTERNAL -> {
            // These would typically be handled by the app's navigation
            // The onLinkClick callback should be used for custom handling
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                context.startActivity(intent)
            } catch (_: Exception) { }
        }
    }
}


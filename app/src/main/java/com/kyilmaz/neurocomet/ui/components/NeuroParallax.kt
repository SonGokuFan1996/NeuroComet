package com.kyilmaz.neurocomet.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.min

/**
 * Parallax scroll effect configuration
 * Designed with neurodivergent users in mind:
 * - Subtle, non-distracting motion
 * - Predictable, smooth animations
 * - Optional reduced motion support
 */
data class ParallaxConfig(
    val parallaxRatio: Float = 0.5f,      // How much slower the background moves (0-1)
    val maxOffset: Dp = 100.dp,            // Maximum parallax offset
    val fadeOnScroll: Boolean = true,      // Fade content as it scrolls
    val scaleOnScroll: Boolean = false,    // Subtle scale effect on scroll
    val blurOnScroll: Boolean = false,     // Blur background on scroll
    val reducedMotion: Boolean = false     // Respect reduced motion preferences
)

/**
 * Neurodivergent-friendly parallax container for LazyList
 *
 * Creates a subtle depth effect that enhances visual hierarchy
 * without being overwhelming or distracting.
 */
@Composable
fun NeuroParallaxContainer(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    config: ParallaxConfig = ParallaxConfig(),
    backgroundContent: @Composable BoxScope.() -> Unit,
    foregroundContent: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val maxOffsetPx = with(density) { config.maxOffset.toPx() }

    // Calculate scroll progress
    val scrollProgress by remember(lazyListState) {
        derivedStateOf {
            if (config.reducedMotion) return@derivedStateOf 0f

            val firstVisibleItem = lazyListState.firstVisibleItemIndex
            val firstVisibleOffset = lazyListState.firstVisibleItemScrollOffset

            // Calculate overall scroll progress (0 to 1 for first screen worth of content)
            val totalOffset = (firstVisibleItem * 500) + firstVisibleOffset // Approximate
            (totalOffset / 1000f).coerceIn(0f, 1f)
        }
    }

    // Animated values for smooth transitions
    val parallaxOffset by animateFloatAsState(
        targetValue = scrollProgress * maxOffsetPx * config.parallaxRatio,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "parallaxOffset"
    )

    val fadeAlpha by animateFloatAsState(
        targetValue = if (config.fadeOnScroll) 1f - (scrollProgress * 0.3f) else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "fadeAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (config.scaleOnScroll) 1f + (scrollProgress * 0.05f) else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )

    Box(modifier = modifier) {
        // Background layer with parallax effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = -parallaxOffset
                    scaleX = scale
                    scaleY = scale
                    alpha = fadeAlpha
                }
        ) {
            backgroundContent()
        }

        // Foreground content
        Box(modifier = Modifier.fillMaxSize()) {
            foregroundContent()
        }
    }
}

/**
 * Parallax header for scrollable content
 * Perfect for profile headers, story headers, or any hero section
 */
@Composable
fun NeuroParallaxHeader(
    scrollState: ScrollState,
    headerHeight: Dp = 300.dp,
    modifier: Modifier = Modifier,
    config: ParallaxConfig = ParallaxConfig(),
    overlayGradient: Boolean = true,
    headerContent: @Composable BoxScope.() -> Unit,
    stickyContent: (@Composable BoxScope.(progress: Float) -> Unit)? = null
) {
    val density = LocalDensity.current
    val headerHeightPx = with(density) { headerHeight.toPx() }
    val maxOffsetPx = with(density) { config.maxOffset.toPx() }

    // Calculate scroll progress
    val scrollProgress by remember(scrollState) {
        derivedStateOf {
            if (config.reducedMotion) return@derivedStateOf 0f
            (scrollState.value / headerHeightPx).coerceIn(0f, 1f)
        }
    }

    val parallaxOffset by animateFloatAsState(
        targetValue = scrollState.value * config.parallaxRatio,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing),
        label = "headerParallax"
    )

    val fadeAlpha by animateFloatAsState(
        targetValue = if (config.fadeOnScroll) 1f - scrollProgress else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "headerFade"
    )

    val scale by animateFloatAsState(
        targetValue = if (config.scaleOnScroll) 1f + (scrollProgress * 0.1f) else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "headerScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // Parallax header content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = parallaxOffset
                    scaleX = scale
                    scaleY = scale
                    alpha = fadeAlpha
                }
        ) {
            headerContent()
        }

        // Optional gradient overlay for better text readability
        if (overlayGradient) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f * scrollProgress),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f * scrollProgress)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        // Sticky content that transforms as you scroll
        stickyContent?.invoke(this, scrollProgress)
    }
}

/**
 * Parallax image specifically optimized for Coil AsyncImage
 */
@Composable
fun NeuroParallaxImage(
    imageUrl: String,
    contentDescription: String?,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    config: ParallaxConfig = ParallaxConfig(parallaxRatio = 0.3f),
    contentScale: ContentScale = ContentScale.Crop,
    overlayColor: Color = Color.Black.copy(alpha = 0.2f)
) {
    val scrollProgress by remember(scrollState) {
        derivedStateOf {
            if (config.reducedMotion) return@derivedStateOf 0f
            (scrollState.value / 500f).coerceIn(0f, 1f)
        }
    }

    val parallaxOffset by animateFloatAsState(
        targetValue = scrollState.value * config.parallaxRatio,
        animationSpec = tween(durationMillis = 50),
        label = "imageParallax"
    )

    val scale by animateFloatAsState(
        targetValue = 1f + (scrollProgress * 0.1f),
        animationSpec = tween(durationMillis = 100),
        label = "imageScale"
    )

    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = parallaxOffset
                    scaleX = scale
                    scaleY = scale
                },
            contentScale = contentScale
        )

        // Subtle overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )
    }
}

/**
 * Parallax card effect for items in a list
 * Applies subtle depth as items scroll into/out of view
 */
@Composable
fun NeuroParallaxCard(
    lazyListState: LazyListState,
    itemIndex: Int,
    modifier: Modifier = Modifier,
    config: ParallaxConfig = ParallaxConfig(
        parallaxRatio = 0.1f,
        scaleOnScroll = true,
        fadeOnScroll = false
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current

    // Calculate this item's visibility and position
    val itemProgress by remember(lazyListState, itemIndex) {
        derivedStateOf {
            if (config.reducedMotion) return@derivedStateOf 0f

            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val thisItem = visibleItems.find { it.index == itemIndex }

            if (thisItem == null) return@derivedStateOf 0f

            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val itemCenter = thisItem.offset + (thisItem.size / 2)
            val viewportCenter = viewportHeight / 2

            // Calculate how far from center this item is (-1 to 1)
            val distanceFromCenter = (itemCenter - viewportCenter).toFloat() / viewportCenter
            distanceFromCenter.coerceIn(-1f, 1f)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (config.scaleOnScroll) {
            1f - (abs(itemProgress) * 0.03f)
        } else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (config.fadeOnScroll) {
            1f - (abs(itemProgress) * 0.2f)
        } else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "cardAlpha"
    )

    val translationY by animateFloatAsState(
        targetValue = itemProgress * with(density) { 10.dp.toPx() } * config.parallaxRatio,
        animationSpec = tween(durationMillis = 100),
        label = "cardTranslation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                this.translationY = translationY
            }
    ) {
        content()
    }
}

/**
 * Layered parallax effect with multiple depth layers
 * Great for creating immersive backgrounds
 */
@Composable
fun NeuroLayeredParallax(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    layers: List<@Composable (parallaxOffset: Float, alpha: Float) -> Unit>
) {
    val scrollProgress by remember(scrollState) {
        derivedStateOf {
            if (reducedMotion) return@derivedStateOf 0f
            (scrollState.value / 1000f).coerceIn(0f, 1f)
        }
    }

    Box(modifier = modifier) {
        layers.forEachIndexed { index, layerContent ->
            val layerRatio = (index + 1f) / layers.size

            val layerOffset by animateFloatAsState(
                targetValue = scrollState.value * layerRatio * 0.3f,
                animationSpec = tween(durationMillis = 50 + (index * 20)),
                label = "layer${index}Offset"
            )

            val layerAlpha by animateFloatAsState(
                targetValue = 1f - (scrollProgress * (1f - layerRatio) * 0.5f),
                animationSpec = tween(durationMillis = 100),
                label = "layer${index}Alpha"
            )

            layerContent(layerOffset, layerAlpha)
        }
    }
}

/**
 * Simple parallax modifier that can be applied to any composable
 */
fun Modifier.neuroParallax(
    scrollState: ScrollState,
    ratio: Float = 0.5f,
    maxOffset: Float = 200f,
    reducedMotion: Boolean = false
): Modifier = this.graphicsLayer {
    if (!reducedMotion) {
        val offset = (scrollState.value * ratio).coerceIn(-maxOffset, maxOffset)
        translationY = offset
    }
}

/**
 * Parallax modifier for LazyList
 */
fun Modifier.neuroParallax(
    lazyListState: LazyListState,
    ratio: Float = 0.5f,
    maxOffset: Float = 200f,
    reducedMotion: Boolean = false
): Modifier = this.graphicsLayer {
    if (!reducedMotion) {
        val firstOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
        val offset = (firstOffset * ratio).coerceIn(-maxOffset, maxOffset)
        translationY = offset
    }
}


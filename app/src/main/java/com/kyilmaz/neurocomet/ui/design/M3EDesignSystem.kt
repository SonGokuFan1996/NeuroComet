package com.kyilmaz.neurocomet.ui.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyilmaz.neurocomet.R

/**
 * Material 3 Expressive (M3E) Design System for NeuroComet Android
 *
 * This implements the overhauled design language across the app with:
 * - More expressive, vibrant colors
 * - Larger, bolder typography using Inter font
 * - More prominent rounded shapes (up to 28dp)
 * - Enhanced visual hierarchy
 * - Neurodivergent-friendly adaptations
 *
 * Matches the Flutter version's design language for consistency.
 */
object M3EDesignSystem {

    // ========================================================================
    // SHAPE CONSTANTS - M3E uses more expressive rounded corners
    // ========================================================================

    object Shapes {
        /** Extra small radius - 4dp - for tiny elements like badges */
        val extraSmall: Dp = 4.dp

        /** Small radius - 8dp - for chips, small buttons */
        val small: Dp = 8.dp

        /** Medium radius - 16dp - for cards, dialogs */
        val medium: Dp = 16.dp

        /** Large radius - 24dp - for prominent cards, bottom sheets */
        val large: Dp = 24.dp

        /** Extra large radius - 28dp - for hero elements, feature cards */
        val extraLarge: Dp = 28.dp

        /** Full radius - 1000dp - for pills, circular elements */
        val full: Dp = 1000.dp

        // Pre-built shape objects
        val ExtraSmallShape: Shape = RoundedCornerShape(extraSmall)
        val SmallShape: Shape = RoundedCornerShape(small)
        val MediumShape: Shape = RoundedCornerShape(medium)
        val LargeShape: Shape = RoundedCornerShape(large)
        val ExtraLargeShape: Shape = RoundedCornerShape(extraLarge)
        val PillShape: Shape = RoundedCornerShape(full)
        val CircularShape: Shape = CircleShape

        /** Bubbly card shape - 20dp radius for post cards */
        val BubblyCard: Shape = RoundedCornerShape(20.dp)

        /** Story ring shape - circular */
        val StoryRing: Shape = CircleShape

        /** Avatar shape - circular with optional border */
        val Avatar: Shape = CircleShape

        /** Chip shape - pill-like */
        val Chip: Shape = RoundedCornerShape(full)

        /** Button shape - medium rounded */
        val Button: Shape = RoundedCornerShape(medium)

        /** FAB shape - large rounded */
        val FAB: Shape = RoundedCornerShape(large)

        /** Dialog shape - extra large for prominence */
        val Dialog: Shape = RoundedCornerShape(extraLarge)

        /** Bottom sheet shape - top corners only */
        val BottomSheet: Shape = RoundedCornerShape(
            topStart = extraLarge,
            topEnd = extraLarge,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        /** Snackbar shape - medium rounded */
        val Snackbar: Shape = RoundedCornerShape(medium)

        /** Navigation bar indicator shape */
        val NavIndicator: Shape = RoundedCornerShape(full)

        /**
         * Material 3 Shapes object for theme integration.
         */
        val materialShapes: androidx.compose.material3.Shapes = androidx.compose.material3.Shapes(
            extraSmall = RoundedCornerShape(extraSmall),
            small = RoundedCornerShape(small),
            medium = RoundedCornerShape(medium),
            large = RoundedCornerShape(large),
            extraLarge = RoundedCornerShape(extraLarge)
        )
    }

    // ========================================================================
    // SPACING CONSTANTS - Consistent spacing throughout the app
    // ========================================================================

    object Spacing {
        val xxxs: Dp = 2.dp
        val xxs: Dp = 4.dp
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 20.dp
        val xl: Dp = 24.dp
        val xxl: Dp = 32.dp
        val xxxl: Dp = 48.dp

        // Content padding
        val screenHorizontal: Dp = 16.dp
        val screenVertical: Dp = 16.dp
        val cardPadding: Dp = 16.dp
        val listItemPadding: Dp = 16.dp
        val chipPadding: Dp = 8.dp

        /** Standard bottom spacing to clear the navigation bar/chrome */
        val bottomNavPadding: Dp = 100.dp
    }

    // ========================================================================
    // ELEVATION CONSTANTS - M3E shadow levels
    // ========================================================================

    object Elevation {
        val none: Dp = 0.dp
        val level1: Dp = 1.dp
        val level2: Dp = 3.dp
        val level3: Dp = 6.dp
        val level4: Dp = 8.dp
        val level5: Dp = 12.dp

        // Semantic elevations
        val card: Dp = level2
        val dialog: Dp = level3
        val bottomSheet: Dp = level4
        val fab: Dp = level3
        val appBar: Dp = level2
        val navigation: Dp = level2
    }

    // ========================================================================
    // ICON SIZES - Consistent icon sizing
    // ========================================================================

    object IconSize {
        val xs: Dp = 16.dp
        val sm: Dp = 20.dp
        val md: Dp = 24.dp
        val lg: Dp = 32.dp
        val xl: Dp = 40.dp
        val xxl: Dp = 48.dp

        // Semantic sizes
        val navBar: Dp = md
        val appBar: Dp = md
        val listItem: Dp = md
        val fab: Dp = md
        val badge: Dp = xs
        val avatar: Dp = xl
    }

    // ========================================================================
    // AVATAR SIZES - Standard avatar dimensions
    // ========================================================================

    object AvatarSize {
        val xs: Dp = 24.dp
        val sm: Dp = 32.dp
        val md: Dp = 40.dp
        val lg: Dp = 48.dp
        val xl: Dp = 64.dp
        val xxl: Dp = 96.dp

        // Context-specific
        val listItem: Dp = lg
        val postCard: Dp = 44.dp
        val profile: Dp = xxl
        val story: Dp = 64.dp
        val notification: Dp = 44.dp
        val message: Dp = md
        val comment: Dp = 32.dp
    }

    // ========================================================================
    // ANIMATION DURATIONS - Consistent timing
    // ========================================================================

    object AnimationDuration {
        const val instant: Int = 50
        const val fast: Int = 150
        const val normal: Int = 300
        const val slow: Int = 450
        const val slower: Int = 600

        // Specific animations
        const val staggerDelay: Int = 50
        const val cardEntry: Int = 300
        const val likeAnimation: Int = 200
        const val shimmer: Int = 1500
        const val breathe: Int = 2000
        const val logoGlow: Int = 2500
        const val rainbowCycle: Int = 10000
    }

    // ========================================================================
    // COMPONENT HEIGHTS - Standard component dimensions
    // ========================================================================

    object ComponentHeight {
        val appBar: Dp = 64.dp
        val navigationBar: Dp = 56.dp
        val searchBar: Dp = 56.dp
        val chip: Dp = 32.dp
        val button: Dp = 48.dp
        val buttonSmall: Dp = 36.dp
        val listItem: Dp = 56.dp
        val listItemLarge: Dp = 72.dp
        val tabBar: Dp = 48.dp
        val bottomSheet: Dp = 56.dp
    }
}

// ============================================================================
// M3E COLOR PALETTE - Neurodivergent-friendly with vibrant accents
// ============================================================================

/**
 * Extended color palette for M3E design system.
 * These colors complement the MaterialTheme colorScheme.
 */
object M3EColors {
    // Primary brand colors (matching Flutter)
    val primaryPurple = Color(0xFF7C4DFF)
    val primaryPurpleLight = Color(0xFFB47CFF)
    val primaryPurpleDark = Color(0xFF5C00E6)

    // Secondary colors
    val secondaryTeal = Color(0xFF00BFA5)
    val secondaryTealLight = Color(0xFF5DF2D6)
    val secondaryTealDark = Color(0xFF008E76)

    // Accent colors
    val accentOrange = Color(0xFFFF6E40)
    val accentOrangeLight = Color(0xFFFFA06D)
    val accentOrangeDark = Color(0xFFE64A19)

    // Neurodivergent-friendly calming colors
    val calmBlue = Color(0xFF64B5F6)
    val calmGreen = Color(0xFF81C784)
    val calmPink = Color(0xFFF48FB1)
    val calmYellow = Color(0xFFFFD54F)
    val calmLavender = Color(0xFFCE93D8)

    // Status colors
    val success = Color(0xFF10B981)
    val successLight = Color(0xFF34D399)
    val warning = Color(0xFFF59E0B)
    val warningLight = Color(0xFFFBBF24)
    val error = Color(0xFFEF4444)
    val errorLight = Color(0xFFF87171)
    val info = Color(0xFF3B82F6)
    val infoLight = Color(0xFF60A5FA)

    // Category colors
    val categoryADHD = Color(0xFFFF7043)
    val categoryAutism = Color(0xFF42A5F5)
    val categoryDyslexia = Color(0xFF66BB6A)
    val categoryAnxiety = Color(0xFFAB47BC)
    val categoryDepression = Color(0xFF5C6BC0)
    val categoryOCD = Color(0xFF26A69A)
    val categoryBipolar = Color(0xFFFFCA28)
    val categoryGeneral = Color(0xFF78909C)

    // Rainbow gradient colors (for infinity symbol, etc.)
    val rainbowColors = listOf(
        Color(0xFFE57373), // Soft red
        Color(0xFFFFB74D), // Soft orange
        Color(0xFFFFF176), // Soft yellow
        Color(0xFF81C784), // Soft green
        Color(0xFF64B5F6), // Soft blue
        Color(0xFFBA68C8), // Soft purple
        Color(0xFFF48FB1), // Soft pink
        Color(0xFFE57373)  // Loop back
    )

    // Vibrant rainbow (for Pride, celebrations)
    val vibrantRainbowColors = listOf(
        Color(0xFFFF6B6B), // Vibrant coral red
        Color(0xFFFFAB4D), // Bright orange
        Color(0xFFFFE66D), // Sunny yellow
        Color(0xFF7BC67B), // Fresh green
        Color(0xFF4DABF5), // Sky blue
        Color(0xFFCB6CE6), // Vivid violet
        Color(0xFFFF6B9D), // Hot pink
        Color(0xFFFF6B6B)  // Loop back
    )

    /**
     * Get category color by name.
     */
    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "adhd" -> categoryADHD
            "autism", "asd" -> categoryAutism
            "dyslexia" -> categoryDyslexia
            "anxiety" -> categoryAnxiety
            "depression" -> categoryDepression
            "ocd" -> categoryOCD
            "bipolar" -> categoryBipolar
            else -> categoryGeneral
        }
    }

    /**
     * Create a primary to tertiary gradient brush.
     */
    @Composable
    fun avatarGradientBrush(): Brush {
        val primary = MaterialTheme.colorScheme.primary
        val tertiary = MaterialTheme.colorScheme.tertiary
        return Brush.linearGradient(listOf(primary, tertiary))
    }

    /**
     * Create a rainbow gradient brush.
     */
    fun rainbowGradientBrush(): Brush {
        return Brush.linearGradient(rainbowColors)
    }

    /**
     * Create a vibrant rainbow sweep gradient.
     */
    fun vibrantRainbowSweepBrush(): Brush {
        return Brush.sweepGradient(vibrantRainbowColors)
    }
}

// ============================================================================
// GRADIENT PRESETS
// ============================================================================

/**
 * Pre-built gradient brushes for consistent styling.
 */
object M3EGradients {

    val primary = Brush.linearGradient(
        colors = listOf(M3EColors.primaryPurple, M3EColors.secondaryTeal)
    )

    val warm = Brush.linearGradient(
        colors = listOf(M3EColors.accentOrange, Color(0xFFFF8A65))
    )

    val calm = Brush.linearGradient(
        colors = listOf(M3EColors.calmBlue, M3EColors.calmLavender)
    )

    val night = Brush.linearGradient(
        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
    )

    val sunset = Brush.linearGradient(
        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFAB4D), Color(0xFFFFE66D))
    )

    val ocean = Brush.linearGradient(
        colors = listOf(Color(0xFF4DABF5), Color(0xFF00BFA5))
    )

    val forest = Brush.linearGradient(
        colors = listOf(Color(0xFF66BB6A), Color(0xFF26A69A))
    )

    val lavender = Brush.linearGradient(
        colors = listOf(Color(0xFFCE93D8), Color(0xFF9575CD))
    )

    /**
     * Create a gradient from theme colors.
     */
    @Composable
    fun themeGradient(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        )
    }

    /**
     * Create a subtle surface gradient for cards.
     */
    @Composable
    fun cardGradient(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        )
    }
}

// ============================================================================
// M3E TYPOGRAPHY - Inter font family with expressive styles
// ============================================================================

/**
 * M3E Typography using Inter font (clean, modern, accessible).
 * Falls back to system default if Inter is not available.
 */
object M3ETypography {

    // Note: In production, you would load Inter from Google Fonts or embed it
    // For now, we use the system default with proper M3E sizing
    private val fontFamily = FontFamily.Default

    /**
     * Get M3E scaled typography.
     * @param scale Text scale factor (1.0 = normal, 1.5 = 50% larger)
     */
    fun getTypography(scale: Float = 1.0f): Typography {
        return Typography(
            displayLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (57 * scale).sp,
                lineHeight = (64 * scale).sp,
                letterSpacing = (-0.25).sp
            ),
            displayMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (45 * scale).sp,
                lineHeight = (52 * scale).sp,
                letterSpacing = 0.sp
            ),
            displaySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (36 * scale).sp,
                lineHeight = (44 * scale).sp,
                letterSpacing = 0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (32 * scale).sp,
                lineHeight = (40 * scale).sp,
                letterSpacing = 0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (28 * scale).sp,
                lineHeight = (36 * scale).sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (24 * scale).sp,
                lineHeight = (32 * scale).sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (22 * scale).sp,
                lineHeight = (28 * scale).sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (16 * scale).sp,
                lineHeight = (24 * scale).sp,
                letterSpacing = 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (14 * scale).sp,
                lineHeight = (20 * scale).sp,
                letterSpacing = 0.1.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (16 * scale).sp,
                lineHeight = (24 * scale).sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (14 * scale).sp,
                lineHeight = (20 * scale).sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W400,
                fontSize = (12 * scale).sp,
                lineHeight = (16 * scale).sp,
                letterSpacing = 0.4.sp
            ),
            labelLarge = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (14 * scale).sp,
                lineHeight = (20 * scale).sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (12 * scale).sp,
                lineHeight = (16 * scale).sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = (11 * scale).sp,
                lineHeight = (16 * scale).sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}

// ============================================================================
// M3E COMPONENT STYLES - Reusable modifiers and compositions
// ============================================================================

/**
 * Common modifier extensions for M3E styling.
 */
object M3EModifiers {

    /**
     * Apply bubbly card styling with gradient background.
     */
    @Composable
    fun Modifier.bubblyCard(
        elevation: Dp = M3EDesignSystem.Elevation.card,
        shape: Shape = M3EDesignSystem.Shapes.BubblyCard
    ): Modifier = this
        .shadow(elevation, shape)
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)

    /**
     * Apply gradient avatar ring styling.
     */
    @Composable
    fun Modifier.avatarRing(
        size: Dp = M3EDesignSystem.AvatarSize.postCard,
        ringWidth: Dp = 3.dp
    ): Modifier {
        val brush = M3EColors.avatarGradientBrush()
        return this
            .size(size)
            .background(brush, CircleShape)
            .padding(ringWidth)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
    }

    /**
     * Apply story ring styling with animated gradient.
     */
    @Composable
    fun Modifier.storyRing(
        size: Dp = M3EDesignSystem.AvatarSize.story,
        hasUnseenStory: Boolean = false,
        ringWidth: Dp = 3.dp
    ): Modifier {
        val colors = if (hasUnseenStory) {
            M3EColors.vibrantRainbowColors
        } else {
            listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.outlineVariant
            )
        }
        val brush = Brush.sweepGradient(colors)
        return this
            .size(size)
            .background(brush, CircleShape)
            .padding(ringWidth)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
    }

    /**
     * Apply pill-shaped chip styling.
     */
    @Composable
    fun Modifier.chip(): Modifier = this
        .clip(M3EDesignSystem.Shapes.Chip)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(horizontal = M3EDesignSystem.Spacing.xs, vertical = M3EDesignSystem.Spacing.xxs)
}

// ============================================================================
// ANIMATION UTILITIES
// ============================================================================

/**
 * Animation utilities for consistent motion throughout the app.
 */
object M3EAnimations {

    /**
     * Easing curve for standard animations.
     */
    val standardEasing = FastOutSlowInEasing

    /**
     * Easing curve for deceleration (entering elements).
     */
    val decelerateEasing = LinearOutSlowInEasing

    /**
     * Easing curve for acceleration (exiting elements).
     */
    val accelerateEasing = FastOutLinearInEasing

    /**
     * Create a breathing scale animation for calm effects.
     */
    @Composable
    fun breathingScale(
        initialScale: Float = 1f,
        targetScale: Float = 1.008f,
        durationMillis: Int = M3EDesignSystem.AnimationDuration.breathe
    ): State<Float> {
        val physics = LocalM3EPhysics.current
        if (physics.reducedMotion) {
            return rememberUpdatedState(initialScale)
        }
        val infiniteTransition = rememberInfiniteTransition(label = "breathing")
        return infiniteTransition.animateFloat(
            initialValue = initialScale,
            targetValue = targetScale,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathingScale"
        )
    }

    /**
     * Create a glow pulse animation for emphasis.
     */
    @Composable
    fun glowPulse(
        initialAlpha: Float = 0.3f,
        targetAlpha: Float = 0.6f,
        durationMillis: Int = M3EDesignSystem.AnimationDuration.logoGlow
    ): State<Float> {
        val physics = LocalM3EPhysics.current
        if (physics.reducedMotion) {
            return rememberUpdatedState(initialAlpha)
        }
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        return infiniteTransition.animateFloat(
            initialValue = initialAlpha,
            targetValue = targetAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowPulse"
        )
    }

    /**
     * Create a rotation animation for rainbow gradients.
     */
    @Composable
    fun rainbowRotation(
        durationMillis: Int = M3EDesignSystem.AnimationDuration.rainbowCycle
    ): State<Float> {
        val physics = LocalM3EPhysics.current
        if (physics.reducedMotion) {
            return rememberUpdatedState(0f)
        }
        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rainbowRotation"
        )
    }

    /**
     * Create a shimmer animation for loading states.
     */
    @Composable
    fun shimmerOffset(
        durationMillis: Int = M3EDesignSystem.AnimationDuration.shimmer
    ): State<Float> {
        val physics = LocalM3EPhysics.current
        if (physics.reducedMotion) {
            return rememberUpdatedState(0f)
        }
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        return infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerOffset"
        )
    }

    /**
     * Create a staggered entry animation for lists.
     */
    fun staggeredDelay(index: Int): Int {
        return (index * M3EDesignSystem.AnimationDuration.staggerDelay)
            .coerceAtMost(500) // Cap maximum delay
    }
}


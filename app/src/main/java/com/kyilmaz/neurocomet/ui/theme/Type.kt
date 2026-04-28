package com.kyilmaz.neurocomet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive (M3E) Typography for NeuroComet
 *
 * This typography system is designed to be:
 * - Clean and modern using system fonts (optimized for readability)
 * - Accessible with appropriate line heights and letter spacing
 * - Consistent with the Flutter version's Inter-based typography
 * - Scalable for accessibility needs
 */

// Default font family - uses system default for optimal rendering
// In production, you can replace with Inter from Google Fonts
private val M3EFontFamily = FontFamily.Default

/**
 * M3E Typography - full implementation matching Flutter version
 */
val Typography = Typography(
    // Display styles - for hero text and large headlines
    displayLarge = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles - for section headers and important text
    headlineLarge = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles - for cards, dialogs, and component headers
    titleLarge = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles - for main content text
    bodyLarge = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles - for buttons, chips, and small UI elements
    labelLarge = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = M3EFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Creates a scaled version of the typography for accessibility.
 *
 * @param scale The scale factor (1.0 = normal, 1.5 = 50% larger)
 * @return Typography with all font sizes scaled
 */
fun scaledTypography(scale: Float): Typography {
    return Typography(
        displayLarge = Typography.displayLarge.copy(
            fontSize = Typography.displayLarge.fontSize * scale,
            lineHeight = Typography.displayLarge.lineHeight * scale
        ),
        displayMedium = Typography.displayMedium.copy(
            fontSize = Typography.displayMedium.fontSize * scale,
            lineHeight = Typography.displayMedium.lineHeight * scale
        ),
        displaySmall = Typography.displaySmall.copy(
            fontSize = Typography.displaySmall.fontSize * scale,
            lineHeight = Typography.displaySmall.lineHeight * scale
        ),
        headlineLarge = Typography.headlineLarge.copy(
            fontSize = Typography.headlineLarge.fontSize * scale,
            lineHeight = Typography.headlineLarge.lineHeight * scale
        ),
        headlineMedium = Typography.headlineMedium.copy(
            fontSize = Typography.headlineMedium.fontSize * scale,
            lineHeight = Typography.headlineMedium.lineHeight * scale
        ),
        headlineSmall = Typography.headlineSmall.copy(
            fontSize = Typography.headlineSmall.fontSize * scale,
            lineHeight = Typography.headlineSmall.lineHeight * scale
        ),
        titleLarge = Typography.titleLarge.copy(
            fontSize = Typography.titleLarge.fontSize * scale,
            lineHeight = Typography.titleLarge.lineHeight * scale
        ),
        titleMedium = Typography.titleMedium.copy(
            fontSize = Typography.titleMedium.fontSize * scale,
            lineHeight = Typography.titleMedium.lineHeight * scale
        ),
        titleSmall = Typography.titleSmall.copy(
            fontSize = Typography.titleSmall.fontSize * scale,
            lineHeight = Typography.titleSmall.lineHeight * scale
        ),
        bodyLarge = Typography.bodyLarge.copy(
            fontSize = Typography.bodyLarge.fontSize * scale,
            lineHeight = Typography.bodyLarge.lineHeight * scale
        ),
        bodyMedium = Typography.bodyMedium.copy(
            fontSize = Typography.bodyMedium.fontSize * scale,
            lineHeight = Typography.bodyMedium.lineHeight * scale
        ),
        bodySmall = Typography.bodySmall.copy(
            fontSize = Typography.bodySmall.fontSize * scale,
            lineHeight = Typography.bodySmall.lineHeight * scale
        ),
        labelLarge = Typography.labelLarge.copy(
            fontSize = Typography.labelLarge.fontSize * scale,
            lineHeight = Typography.labelLarge.lineHeight * scale
        ),
        labelMedium = Typography.labelMedium.copy(
            fontSize = Typography.labelMedium.fontSize * scale,
            lineHeight = Typography.labelMedium.lineHeight * scale
        ),
        labelSmall = Typography.labelSmall.copy(
            fontSize = Typography.labelSmall.fontSize * scale,
            lineHeight = Typography.labelSmall.lineHeight * scale
        )
    )
}

package com.kyilmaz.neurocomet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

/**
 * A composable that animates text appearing character-by-character (typewriter effect).
 *
 * Features:
 * - Configurable delay between characters
 * - Optional cursor blink
 * - Callback when animation completes
 * - Supports custom text styles
 *
 * @param text The full text to animate
 * @param modifier Modifier for the text
 * @param style Text style to apply
 * @param delayPerCharacter Delay in milliseconds between each character appearing
 * @param initialDelay Delay before animation starts
 * @param showCursor Whether to show a blinking cursor at the end
 * @param cursorChar The character to use as cursor
 * @param onAnimationComplete Callback when full text has been displayed
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    delayPerCharacter: Long = 50L,
    initialDelay: Long = 0L,
    showCursor: Boolean = false,
    cursorChar: Char = '▌',
    onAnimationComplete: (() -> Unit)? = null
) {
    var displayedCharCount by remember(text) { mutableIntStateOf(0) }
    var showCursorState by remember { mutableIntStateOf(1) }

    // Animate character appearance
    LaunchedEffect(text) {
        displayedCharCount = 0
        if (initialDelay > 0) {
            delay(initialDelay)
        }

        for (i in text.indices) {
            delay(delayPerCharacter)
            displayedCharCount = i + 1
        }

        onAnimationComplete?.invoke()
    }

    // Cursor blink animation
    LaunchedEffect(showCursor, displayedCharCount) {
        if (showCursor && displayedCharCount < text.length) {
            while (true) {
                delay(500)
                showCursorState = if (showCursorState == 1) 0 else 1
            }
        }
    }

    val displayText = buildString {
        append(text.take(displayedCharCount))
        if (showCursor && displayedCharCount < text.length && showCursorState == 1) {
            append(cursorChar)
        }
    }

    Text(
        text = displayText,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

/**
 * A typewriter text that fades in each character with a smooth animation.
 * More visually pleasing for accessibility-focused apps.
 */
@Composable
fun SmoothTypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    delayPerCharacter: Long = 30L,
    initialDelay: Long = 0L,
    onAnimationComplete: (() -> Unit)? = null
) {
    var displayedCharCount by remember(text) { mutableIntStateOf(0) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(text) {
        displayedCharCount = 0
        if (initialDelay > 0) {
            delay(initialDelay)
        }

        for (i in text.indices) {
            // Quick fade for each character
            alphaAnim.snapTo(0.5f)
            alphaAnim.animateTo(1f, animationSpec = tween(durationMillis = 100))
            displayedCharCount = i + 1
            delay(delayPerCharacter)
        }

        onAnimationComplete?.invoke()
    }

    Text(
        text = text.take(displayedCharCount),
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

/**
 * Animated text that reveals word-by-word instead of character-by-character.
 * Less overwhelming for users who may find rapid character animation distracting.
 */
@Composable
fun WordByWordText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    delayPerWord: Long = 150L,
    initialDelay: Long = 0L,
    onAnimationComplete: (() -> Unit)? = null
) {
    val words = remember(text) { text.split(" ") }
    var displayedWordCount by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        displayedWordCount = 0
        if (initialDelay > 0) {
            delay(initialDelay)
        }

        for (i in words.indices) {
            delay(delayPerWord)
            displayedWordCount = i + 1
        }

        onAnimationComplete?.invoke()
    }

    Text(
        text = words.take(displayedWordCount).joinToString(" "),
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}


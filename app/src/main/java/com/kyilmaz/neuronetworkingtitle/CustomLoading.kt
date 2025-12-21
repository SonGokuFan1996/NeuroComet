package com.kyilmaz.neuronetworkingtitle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NeuroLoadingAnimation(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(16.dp)
                    .scale(0.5f + animatable.value) // Pulse scale 0.5 to 1.5
                    .background(color = color.copy(alpha = 0.5f + (animatable.value / 2)), shape = CircleShape)
            )
        }
    }
}

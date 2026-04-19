package com.kyilmaz.neurocomet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A high-fidelity implementation of the NeuroComet Feature Graphic (1024x500).
 * This can be used to generate the final asset via a screenshot or automated test.
 */
@Composable
fun FeatureGraphicContent() {
    val WIDTH = 1024.dp
    val HEIGHT = 500.dp
    
    val baseColor = Color(0xFF1a1a2e)
    val secondaryColor = Color(0xFF2e2e56)
    
    val rainbowColors = listOf(
        Color(0xFFE57373), // Soft coral red
        Color(0xFFFFB74D), // Warm amber
        Color(0xFFFFF176), // Gentle yellow
        Color(0xFF81C784), // Fresh green
        Color(0xFF64B5F6), // Sky blue
        Color(0xFFBA68C8), // Soft violet
        Color(0xFFF48FB1)  // Rose pink
    )

    Box(
        modifier = Modifier
            .size(WIDTH, HEIGHT)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(baseColor, secondaryColor)
                )
            )
    ) {
        // 1. Rainbow Infinity Logo (Left Side)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(450.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow layer
            Canvas(modifier = Modifier.size(320.dp).blur(20.dp)) {
                val path = createInfinityPath(size.width, size.height)
                rainbowColors.forEachIndexed { i, color ->
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.15f),
                        style = Stroke(width = 40f + (i * 4))
                    )
                }
            }
            
            // Main path layer
            Canvas(modifier = Modifier.size(300.dp)) {
                val path = createInfinityPath(size.width, size.height)
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(rainbowColors),
                    style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // 2. Text Content (Right Side)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 480.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NeuroComet",
                fontSize = 84.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-2).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "A safe space for every mind ✨",
                fontSize = 32.sp,
                color = Color(0xFFCCCCFF),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val features = listOf("Sensory-Friendly", "ND-Affirming", "LGBTQ+ Inclusive")
            features.forEach { feature ->
                Text(
                    text = "• $feature",
                    fontSize = 24.sp,
                    color = Color(0xFFB0B0E6),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

private fun createInfinityPath(width: Float, height: Float): Path {
    val centerX = width / 2
    val centerY = height / 2
    val loopWidth = width * 0.45f
    val loopHeight = height * 0.4f
    
    return Path().apply {
        moveTo(centerX, centerY)
        // Right loop
        cubicTo(
            centerX + loopWidth * 0.5f, centerY - loopHeight,
            centerX + loopWidth, centerY - loopHeight * 0.5f,
            centerX + loopWidth, centerY
        )
        cubicTo(
            centerX + loopWidth, centerY + loopHeight * 0.5f,
            centerX + loopWidth * 0.5f, centerY + loopHeight,
            centerX, centerY
        )
        // Left loop
        cubicTo(
            centerX - loopWidth * 0.5f, centerY - loopHeight,
            centerX - loopWidth, centerY - loopHeight * 0.5f,
            centerX - loopWidth, centerY
        )
        cubicTo(
            centerX - loopWidth, centerY + loopHeight * 0.5f,
            centerX - loopWidth * 0.5f, centerY + loopHeight,
            centerX, centerY
        )
    }
}

@Preview(showBackground = true, widthDp = 1024, heightDp = 500)
@Composable
fun PreviewFeatureGraphic() {
    FeatureGraphicContent()
}

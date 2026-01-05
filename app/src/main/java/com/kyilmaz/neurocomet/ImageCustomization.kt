package com.kyilmaz.neurocomet

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.roundToInt

/**
 * Image Customization System for NeuroComet
 *
 * Features:
 * - Filters and effects (Instagram-style)
 * - Cropping and rotation
 * - Text overlays with fonts
 * - Stickers and emojis
 * - Drawing tools
 * - Brightness, contrast, saturation adjustments
 * - Neurodivergent-friendly UI (clear icons, good contrast)
 *
 * Used for:
 * - Story creation
 * - Post images
 * - Profile picture editing
 */

// ═══════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════

/**
 * Represents an image filter
 */
data class ImageFilter(
    val id: String,
    val name: String,
    val emoji: String,
    val colorMatrix: FloatArray? = null, // For color adjustments
    val intensity: Float = 1f,
    val description: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageFilter) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Text overlay configuration
 */
data class TextOverlay(
    val id: String,
    val text: String,
    val position: Offset = Offset(0.5f, 0.5f), // Normalized 0-1
    val fontSize: Float = 24f,
    val color: Color = Color.White,
    val fontFamily: String = "default",
    val rotation: Float = 0f,
    val shadowEnabled: Boolean = true
)

/**
 * Sticker overlay
 */
data class StickerOverlay(
    val id: String,
    val emoji: String,
    val position: Offset = Offset(0.5f, 0.5f),
    val scale: Float = 1f,
    val rotation: Float = 0f
)

/**
 * Drawing path
 */
data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

/**
 * Complete image customization state
 */
data class ImageCustomizationState(
    val originalImageUri: Uri? = null,
    val filter: ImageFilter = AVAILABLE_FILTERS.first(),
    val brightness: Float = 0f, // -100 to 100
    val contrast: Float = 0f, // -100 to 100
    val saturation: Float = 0f, // -100 to 100
    val rotation: Float = 0f, // Degrees
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickers: List<StickerOverlay> = emptyList(),
    val drawings: List<DrawingPath> = emptyList(),
    val cropAspectRatio: Float? = null // null = free crop
)

// ═══════════════════════════════════════════════════════════════
// AVAILABLE FILTERS
// ═══════════════════════════════════════════════════════════════

val AVAILABLE_FILTERS = listOf(
    ImageFilter(
        id = "normal",
        name = "Normal",
        emoji = "🖼️",
        description = "Original image"
    ),
    ImageFilter(
        id = "warm",
        name = "Warm",
        emoji = "🌅",
        colorMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.1f, 0f, 0f, 5f,
            0f, 0f, 0.9f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Warm, cozy tones"
    ),
    ImageFilter(
        id = "cool",
        name = "Cool",
        emoji = "❄️",
        colorMatrix = floatArrayOf(
            0.9f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 5f,
            0f, 0f, 1.2f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Cool, calming blue tones"
    ),
    ImageFilter(
        id = "vintage",
        name = "Vintage",
        emoji = "📷",
        colorMatrix = floatArrayOf(
            0.9f, 0.1f, 0.1f, 0f, 20f,
            0.1f, 0.9f, 0.1f, 0f, 15f,
            0.1f, 0.1f, 0.7f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Retro film look"
    ),
    ImageFilter(
        id = "fade",
        name = "Fade",
        emoji = "🌫️",
        colorMatrix = floatArrayOf(
            1f, 0f, 0f, 0f, 30f,
            0f, 1f, 0f, 0f, 30f,
            0f, 0f, 1f, 0f, 30f,
            0f, 0f, 0f, 0.9f, 0f
        ),
        description = "Soft, faded look"
    ),
    ImageFilter(
        id = "vivid",
        name = "Vivid",
        emoji = "🌈",
        colorMatrix = floatArrayOf(
            1.3f, 0f, 0f, 0f, 0f,
            0f, 1.3f, 0f, 0f, 0f,
            0f, 0f, 1.3f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Vibrant, saturated colors"
    ),
    ImageFilter(
        id = "noir",
        name = "Noir",
        emoji = "🖤",
        colorMatrix = floatArrayOf(
            0.3f, 0.6f, 0.1f, 0f, 0f,
            0.3f, 0.6f, 0.1f, 0f, 0f,
            0.3f, 0.6f, 0.1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Black and white"
    ),
    ImageFilter(
        id = "sepia",
        name = "Sepia",
        emoji = "🤎",
        colorMatrix = floatArrayOf(
            0.4f, 0.77f, 0.2f, 0f, 0f,
            0.35f, 0.69f, 0.17f, 0f, 0f,
            0.27f, 0.53f, 0.13f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Warm brown tones"
    ),
    ImageFilter(
        id = "soft",
        name = "Soft",
        emoji = "☁️",
        colorMatrix = floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 10f,
            0.05f, 0.95f, 0f, 0f, 10f,
            0f, 0.05f, 0.95f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "Gentle, soft appearance"
    ),
    ImageFilter(
        id = "dramatic",
        name = "Dramatic",
        emoji = "🎭",
        colorMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, -20f,
            0f, 1.2f, 0f, 0f, -20f,
            0f, 0f, 1.2f, 0f, -20f,
            0f, 0f, 0f, 1f, 0f
        ),
        description = "High contrast drama"
    )
)

val AVAILABLE_STICKERS = listOf(
    "✨", "🌟", "💫", "⭐", "🔥", "❤️", "💜", "💙", "💚", "💛",
    "🦋", "🌈", "☀️", "🌙", "⚡", "🎉", "🎊", "🎈", "🎁", "🏆",
    "👑", "💎", "🔮", "🧠", "💪", "🙌", "👏", "🤗", "😊", "🥰",
    "🤔", "😌", "🎯", "💡", "📚", "🎮", "🎨", "🎵", "🌸", "🌺"
)

val DRAWING_COLORS = listOf(
    Color.White,
    Color.Black,
    Color.Red,
    Color(0xFFFF9800),
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color(0xFF9C27B0),
    Color(0xFFE91E63)
)

// ═══════════════════════════════════════════════════════════════
// MAIN IMAGE EDITOR COMPOSABLE
// ═══════════════════════════════════════════════════════════════

enum class EditorTab {
    FILTERS, ADJUST, TEXT, STICKERS, DRAW, CROP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCustomizationEditor(
    imageUri: Uri?,
    onSave: (ImageCustomizationState) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialState: ImageCustomizationState = ImageCustomizationState(),
    title: String = "Edit Image"
) {
    var state by remember { mutableStateOf(initialState.copy(originalImageUri = imageUri)) }
    var selectedTab by remember { mutableStateOf(EditorTab.FILTERS) }
    var isDrawing by remember { mutableStateOf(false) }
    var currentDrawingColor by remember { mutableStateOf(Color.White) }
    var currentStrokeWidth by remember { mutableFloatStateOf(5f) }
    var currentDrawingPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar
                TopAppBar(
                    title = { Text(title, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cancel",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { onSave(state) }) {
                            Text(
                                "Done",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black
                    )
                )

                // Image preview area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ImagePreview(
                        state = state,
                        isDrawing = isDrawing,
                        drawingColor = currentDrawingColor,
                        strokeWidth = currentStrokeWidth,
                        currentPoints = currentDrawingPoints,
                        onPointsChange = { currentDrawingPoints = it },
                        onDrawingComplete = { path ->
                            if (path.points.isNotEmpty()) {
                                state = state.copy(
                                    drawings = state.drawings + path
                                )
                            }
                            currentDrawingPoints = emptyList()
                        }
                    )
                }

                // Editor tabs
                EditorTabBar(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it }
                )

                // Tab content
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                ) {
                    when (selectedTab) {
                        EditorTab.FILTERS -> FiltersPanel(
                            selectedFilter = state.filter,
                            onFilterSelect = { state = state.copy(filter = it) }
                        )
                        EditorTab.ADJUST -> AdjustmentsPanel(
                            brightness = state.brightness,
                            contrast = state.contrast,
                            saturation = state.saturation,
                            onBrightnessChange = { state = state.copy(brightness = it) },
                            onContrastChange = { state = state.copy(contrast = it) },
                            onSaturationChange = { state = state.copy(saturation = it) }
                        )
                        EditorTab.TEXT -> TextOverlayPanel(
                            onAddText = { text, color, size ->
                                val newOverlay = TextOverlay(
                                    id = System.currentTimeMillis().toString(),
                                    text = text,
                                    color = color,
                                    fontSize = size
                                )
                                state = state.copy(
                                    textOverlays = state.textOverlays + newOverlay
                                )
                            }
                        )
                        EditorTab.STICKERS -> StickersPanel(
                            onStickerSelect = { emoji ->
                                val newSticker = StickerOverlay(
                                    id = System.currentTimeMillis().toString(),
                                    emoji = emoji
                                )
                                state = state.copy(
                                    stickers = state.stickers + newSticker
                                )
                            }
                        )
                        EditorTab.DRAW -> DrawingPanel(
                            isDrawing = isDrawing,
                            onToggleDrawing = { isDrawing = it },
                            selectedColor = currentDrawingColor,
                            onColorSelect = { currentDrawingColor = it },
                            strokeWidth = currentStrokeWidth,
                            onStrokeWidthChange = { currentStrokeWidth = it },
                            onUndo = {
                                if (state.drawings.isNotEmpty()) {
                                    state = state.copy(
                                        drawings = state.drawings.dropLast(1)
                                    )
                                }
                            }
                        )
                        EditorTab.CROP -> CropPanel(
                            rotation = state.rotation,
                            onRotate = { state = state.copy(rotation = state.rotation + it) },
                            onAspectRatioSelect = { state = state.copy(cropAspectRatio = it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(
    state: ImageCustomizationState,
    isDrawing: Boolean,
    drawingColor: Color,
    strokeWidth: Float,
    currentPoints: List<Offset>,
    onPointsChange: (List<Offset>) -> Unit,
    onDrawingComplete: (DrawingPath) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .rotate(state.rotation)
            .pointerInput(isDrawing) {
                if (isDrawing) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            onPointsChange(listOf(offset))
                        },
                        onDrag = { change, _ ->
                            onPointsChange(currentPoints + change.position)
                        },
                        onDragEnd = {
                            onDrawingComplete(
                                DrawingPath(
                                    points = currentPoints,
                                    color = drawingColor,
                                    strokeWidth = strokeWidth
                                )
                            )
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Base image with filter
        state.originalImageUri?.let { uri ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image being edited",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                colorFilter = state.filter.colorMatrix?.let { matrixValues ->
                    val colorMatrix = ColorMatrix()
                    matrixValues.copyInto(colorMatrix.values)
                    ColorFilter.colorMatrix(colorMatrix)
                }
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("No image selected", color = Color.White)
        }

        // Drawings overlay
        if (state.drawings.isNotEmpty() || currentPoints.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Completed drawings
                state.drawings.forEach { path ->
                    if (path.points.size > 1) {
                        for (i in 0 until path.points.size - 1) {
                            drawLine(
                                color = path.color,
                                start = path.points[i],
                                end = path.points[i + 1],
                                strokeWidth = path.strokeWidth
                            )
                        }
                    }
                }

                // Current drawing
                if (currentPoints.size > 1) {
                    for (i in 0 until currentPoints.size - 1) {
                        drawLine(
                            color = drawingColor,
                            start = currentPoints[i],
                            end = currentPoints[i + 1],
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        }

        // Text overlays - positioned at center for simplicity
        state.textOverlays.forEach { overlay ->
            Text(
                text = overlay.text,
                color = overlay.color,
                fontSize = overlay.fontSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = ((overlay.position.x - 0.5f) * 200).dp,
                        y = ((overlay.position.y - 0.5f) * 200).dp
                    )
            )
        }

        // Stickers - positioned at center for simplicity
        state.stickers.forEach { sticker ->
            Text(
                text = sticker.emoji,
                fontSize = (32 * sticker.scale).sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = ((sticker.position.x - 0.5f) * 200).dp,
                        y = ((sticker.position.y - 0.5f) * 200).dp
                    )
                    .rotate(sticker.rotation)
            )
        }

        // Drawing indicator
        if (isDrawing) {
            Text(
                text = "🎨 Drawing mode",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EditorTabBar(
    selectedTab: EditorTab,
    onTabSelect: (EditorTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EditorTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelect(tab) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = when (tab) {
                        EditorTab.FILTERS -> Icons.Outlined.FilterVintage
                        EditorTab.ADJUST -> Icons.Outlined.Tune
                        EditorTab.TEXT -> Icons.Outlined.TextFields
                        EditorTab.STICKERS -> Icons.Outlined.EmojiEmotions
                        EditorTab.DRAW -> Icons.Outlined.Brush
                        EditorTab.CROP -> Icons.Outlined.Crop
                    },
                    contentDescription = tab.name,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.name,
                    fontSize = 10.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    selectedFilter: ImageFilter,
    onFilterSelect: (ImageFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(AVAILABLE_FILTERS) { filter ->
            FilterItem(
                filter = filter,
                isSelected = filter.id == selectedFilter.id,
                onClick = { onFilterSelect(filter) }
            )
        }
    }
}

@Composable
private fun FilterItem(
    filter: ImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(filter.emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = filter.name,
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AdjustmentsPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdjustmentSlider(
            label = "☀️ Brightness",
            value = brightness,
            onValueChange = onBrightnessChange
        )
        AdjustmentSlider(
            label = "🎚️ Contrast",
            value = contrast,
            onValueChange = onContrastChange
        )
        AdjustmentSlider(
            label = "🌈 Saturation",
            value = saturation,
            onValueChange = onSaturationChange
        )
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text("${value.toInt()}", color = Color.Gray, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -100f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TextOverlayPanel(
    onAddText: (String, Color, Float) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color.White) }
    var fontSize by remember { mutableFloatStateOf(24f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.image_editor_enter_text), color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color picker
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DRAWING_COLORS.take(6).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == selectedColor) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onAddText(text, selectedColor, fontSize)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@Composable
private fun StickersPanel(
    onStickerSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AVAILABLE_STICKERS) { sticker ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .clickable { onStickerSelect(sticker) },
                contentAlignment = Alignment.Center
            ) {
                Text(sticker, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun DrawingPanel(
    isDrawing: Boolean,
    onToggleDrawing: (Boolean) -> Unit,
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isDrawing,
                    onClick = { onToggleDrawing(!isDrawing) },
                    label = { Text(if (isDrawing) "Drawing On" else "Drawing Off") },
                    leadingIcon = {
                        Icon(
                            if (isDrawing) Icons.Filled.Brush else Icons.Outlined.Brush,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                IconButton(onClick = onUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, "Undo", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Color picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DRAWING_COLORS.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 3.dp else 1.dp,
                            color = if (color == selectedColor) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stroke width
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size: ", color = Color.White, fontSize = 12.sp)
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..20f,
                modifier = Modifier.weight(1f)
            )
            Text("${strokeWidth.toInt()}px", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CropPanel(
    rotation: Float,
    onRotate: (Float) -> Unit,
    onAspectRatioSelect: (Float?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Rotation: ${rotation.toInt()}°", color = Color.White)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { onRotate(-90f) }) {
                Icon(Icons.AutoMirrored.Filled.RotateLeft, "Rotate left", tint = Color.White)
            }
            OutlinedButton(onClick = { onRotate(90f) }) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, "Rotate right", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Aspect Ratio:", color = Color.White, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Free" to null,
                "1:1" to 1f,
                "4:3" to 4f/3f,
                "16:9" to 16f/9f,
                "9:16" to 9f/16f
            ).forEach { (label, ratio) ->
                FilterChip(
                    selected = false,
                    onClick = { onAspectRatioSelect(ratio) },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PROFILE PICTURE EDITOR
// ═══════════════════════════════════════════════════════════════

@Composable
fun ProfilePictureEditor(
    currentImageUri: Uri?,
    onSave: (ImageCustomizationState) -> Unit,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf(currentImageUri) }

    if (showEditor && selectedUri != null) {
        ImageCustomizationEditor(
            imageUri = selectedUri,
            onSave = { state ->
                onSave(state)
                showEditor = false
            },
            onDismiss = { showEditor = false },
            title = "Edit Profile Picture"
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Current avatar preview
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onPickImage) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose")
                        }

                        if (selectedUri != null) {
                            Button(onClick = { showEditor = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}


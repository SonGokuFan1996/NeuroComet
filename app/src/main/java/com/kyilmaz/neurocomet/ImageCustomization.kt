package com.kyilmaz.neurocomet

import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
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
                                contentDescription = stringResource(R.string.action_cancel),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { onSave(state) }) {
                            Text(stringResource(R.string.action_done),
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
                            selectedAspectRatio = state.cropAspectRatio,
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
                contentDescription = stringResource(R.string.cd_image_editing),
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
            Text(stringResource(R.string.label_no_image_selected), color = Color.White)
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
                Text(stringResource(R.string.action_add))
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
    selectedAspectRatio: Float? = null,
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
                    selected = selectedAspectRatio == ratio,
                    onClick = { onAspectRatioSelect(ratio) },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PROFILE PICTURE EDITOR & AVATAR MAKER
// ═══════════════════════════════════════════════════════════════

// Avatar configuration data
data class AvatarConfig(
    val skinTone: Color = Color(0xFFF5D6B8),
    val faceShape: String = "circle",   // circle, rounded, square
    val eyes: String = "😊",
    val mouth: String = "smile",       // smile, grin, neutral, open
    val hair: String = "none",          // none, short, long, curly, spiky, bun
    val hairColor: Color = Color(0xFF4A3728),
    val accessory: String = "none",     // none, glasses, sunglasses, headphones, hat, bow
    val bgColor: Color = Color(0xFF7C4DFF)
)

private val SKIN_TONES = listOf(
    Color(0xFFFDEBD0), Color(0xFFF5D6B8), Color(0xFFE8B88A),
    Color(0xFFD4956A), Color(0xFFB87333), Color(0xFF8B5E3C),
    Color(0xFF6B4226), Color(0xFF3D2B1F)
)

private val HAIR_COLORS = listOf(
    Color(0xFF1A1A1A), Color(0xFF4A3728), Color(0xFF8B6914),
    Color(0xFFD4A017), Color(0xFFCC5500), Color(0xFFB22222),
    Color(0xFF8B008B), Color(0xFF4169E1), Color(0xFF2E8B57),
    Color(0xFFFF69B4)
)

private val BG_COLORS = listOf(
    Color(0xFF7C4DFF), Color(0xFF448AFF), Color(0xFF00BCD4),
    Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFE91E63),
    Color(0xFF9C27B0), Color(0xFF607D8B), Color(0xFFFF5722),
    Color(0xFF3F51B5), Color(0xFF009688), Color(0xFF795548)
)

private val FACE_SHAPES = listOf("circle", "rounded", "square")
private val EYE_OPTIONS = listOf("😊", "😄", "🙂", "😎", "🤓", "😌", "🥹", "😏")
private val MOUTH_OPTIONS = listOf("smile", "grin", "neutral", "open")
private val HAIR_OPTIONS = listOf("none", "short", "long", "curly", "spiky", "bun")
private val ACCESSORY_OPTIONS = listOf("none", "glasses", "sunglasses", "headphones", "hat", "bow")

private enum class AvatarTab { SKIN, HAIR, EYES, MOUTH, ACCESSORY, BACKGROUND }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureEditor(
    currentImageUri: Uri?,
    onSave: (ImageCustomizationState) -> Unit,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit
) {
    var showPhotoEditor by remember { mutableStateOf(false) }
    var showAvatarMaker by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf(currentImageUri) }

    if (showPhotoEditor && selectedUri != null) {
        ImageCustomizationEditor(
            imageUri = selectedUri,
            onSave = { state ->
                onSave(state)
                showPhotoEditor = false
            },
            onDismiss = { showPhotoEditor = false },
            title = "Edit Profile Picture"
        )
    } else if (showAvatarMaker) {
        AvatarMakerSheet(
            onDismiss = { showAvatarMaker = false },
            onSave = {
                // For now, the avatar is drawn in-app — dismiss and pass a placeholder
                showAvatarMaker = false
                onSave(ImageCustomizationState())
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Choose how to set your profile picture",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current avatar preview
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = stringResource(R.string.cd_profile_picture),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Action cards
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Choose Photo
                        Surface(
                            onClick = onPickImage,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.label_gallery),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        // Avatar Maker
                        Surface(
                            onClick = { showAvatarMaker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Face,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.cd_avatar),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        // Edit Photo (if image selected)
                        if (selectedUri != null) {
                            Surface(
                                onClick = { showPhotoEditor = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(stringResource(R.string.action_edit),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// AVATAR MAKER - Full composable avatar builder
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarMakerSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var config by remember { mutableStateOf(AvatarConfig()) }
    var selectedTab by remember { mutableStateOf(AvatarTab.SKIN) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                TopAppBar(
                    title = {
                        Text("Create Avatar ✨", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, "Cancel")
                        }
                    },
                    actions = {
                        Button(
                            onClick = onSave,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                )

                // Avatar preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarPreview(config = config, size = 200)
                }

                // Tab selector
                PrimaryScrollableTabRow(
                    selectedTabIndex = AvatarTab.entries.indexOf(selectedTab),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    divider = {}
                ) {
                    AvatarTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    when (tab) {
                                        AvatarTab.SKIN -> "🧑 Skin"
                                        AvatarTab.HAIR -> "💇 Hair"
                                        AvatarTab.EYES -> "👀 Eyes"
                                        AvatarTab.MOUTH -> "👄 Mouth"
                                        AvatarTab.ACCESSORY -> "🎀 Extras"
                                        AvatarTab.BACKGROUND -> "🎨 BG"
                                    },
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Tab content
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    when (selectedTab) {
                        AvatarTab.SKIN -> AvatarSkinPanel(config) { config = it }
                        AvatarTab.HAIR -> AvatarHairPanel(config) { config = it }
                        AvatarTab.EYES -> AvatarEyesPanel(config) { config = it }
                        AvatarTab.MOUTH -> AvatarMouthPanel(config) { config = it }
                        AvatarTab.ACCESSORY -> AvatarAccessoryPanel(config) { config = it }
                        AvatarTab.BACKGROUND -> AvatarBackgroundPanel(config) { config = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarPreview(config: AvatarConfig, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(config.bgColor)
            .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Face Group
        Box(
            modifier = Modifier
                .size((size * 0.75f).dp)
                .offset(y = (size * 0.05f).dp),
            contentAlignment = Alignment.Center
        ) {
            // Face Shape
            val faceShape = when (config.faceShape) {
                "rounded" -> RoundedCornerShape(35)
                "square" -> RoundedCornerShape(20)
                else -> CircleShape
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(faceShape)
                    .background(config.skinTone)
            )

            // Features Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Eyes
                Text(
                    text = config.eyes,
                    fontSize = (size * 0.2f).sp,
                    modifier = Modifier.padding(bottom = (size * 0.02f).dp)
                )

                // Mouth
                val mouthEmoji = when (config.mouth) {
                    "smile" -> "👄"
                    "grin" -> "😃"
                    "neutral" -> "😐"
                    "open" -> "😮"
                    else -> "👄"
                }

                if (mouthEmoji.length > 1) {
                    Text(mouthEmoji, fontSize = (size * 0.12f).sp)
                } else {
                    // Simple mouth line
                    Box(
                        modifier = Modifier
                            .width((size * 0.15f).dp)
                            .height((size * 0.02f).dp)
                            .background(Color(0xFF8B5E3C).copy(alpha = 0.6f), RoundedCornerShape(50))
                    )
                }
            }

            // Hair (layered on top of face)
            if (config.hair != "none") {
                val hairEmoji = when (config.hair) {
                    "short" -> "💇‍♂️"
                    "long" -> "💇‍♀️"
                    "curly" -> "👨‍🦱"
                    "spiky" -> "🌵"
                    "bun" -> "👱‍♀️"
                    else -> ""
                }

                // Hair positioning - covering top
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-size * 0.1f).dp)
                ) {
                    Text(
                        text = hairEmoji,
                        fontSize = (size * 0.45f).sp
                    )
                }
            }
        }

        // Accessory overlay (layered on very top)
        when (config.accessory) {
            "glasses" -> Text("👓", fontSize = (size * 0.25f).sp, modifier = Modifier.offset(y = (size * 0.02f).dp))
            "sunglasses" -> Text("🕶️", fontSize = (size * 0.25f).sp, modifier = Modifier.offset(y = (size * 0.02f).dp))
            "headphones" -> Text("🎧", fontSize = (size * 0.35f).sp, modifier = Modifier.offset(y = (size * 0.05f).dp))
            "hat" -> Text("🎩", fontSize = (size * 0.35f).sp, modifier = Modifier.offset(y = (-size * 0.25f).dp))
            "bow" -> Text("🎀", fontSize = (size * 0.2f).sp, modifier = Modifier.offset(x = (size * 0.2f).dp, y = (-size * 0.25f).dp))
        }
    }
}

@Composable
private fun AvatarSkinPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Skin Tone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SKIN_TONES.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (config.skinTone == color) 3.dp else 1.dp,
                            color = if (config.skinTone == color) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onChange(config.copy(skinTone = color)) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Face Shape", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FACE_SHAPES.forEach { shape ->
                val shapeCompose = when (shape) {
                    "rounded" -> RoundedCornerShape(30)
                    "square" -> RoundedCornerShape(15)
                    else -> CircleShape
                }
                Surface(
                    onClick = { onChange(config.copy(faceShape = shape)) },
                    shape = shapeCompose,
                    color = if (config.faceShape == shape) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (config.faceShape == shape) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(shape.replaceFirstChar { it.uppercaseChar() }, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarHairPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Hair Style", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HAIR_OPTIONS.forEach { hair ->
                val label = when (hair) {
                    "none" -> "❌"
                    "short" -> "〰️"
                    "long" -> "🌊"
                    "curly" -> "〽️"
                    "spiky" -> "⚡"
                    "bun" -> "🔵"
                    else -> "?"
                }
                Surface(
                    onClick = { onChange(config.copy(hair = hair)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (config.hair == hair) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (config.hair == hair) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(label, fontSize = 20.sp) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Hair Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HAIR_COLORS.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (config.hairColor == color) 3.dp else 1.dp,
                            color = if (config.hairColor == color) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onChange(config.copy(hairColor = color)) }
                )
            }
        }
    }
}

@Composable
private fun AvatarEyesPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eyes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EYE_OPTIONS.forEach { eyes ->
                Surface(
                    onClick = { onChange(config.copy(eyes = eyes)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (config.eyes == eyes) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (config.eyes == eyes) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(eyes, fontSize = 26.sp) }
                }
            }
        }
    }
}

@Composable
private fun AvatarMouthPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Mouth", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MOUTH_OPTIONS.forEach { mouth ->
                val display = when (mouth) {
                    "smile" -> "😊"
                    "grin" -> "😄"
                    "neutral" -> "😐"
                    "open" -> "😮"
                    else -> "🙂"
                }
                Surface(
                    onClick = { onChange(config.copy(mouth = mouth)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (config.mouth == mouth) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (config.mouth == mouth) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(display, fontSize = 28.sp) }
                }
            }
        }
    }
}

@Composable
private fun AvatarAccessoryPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Accessories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ACCESSORY_OPTIONS.forEach { acc ->
                val display = when (acc) {
                    "none" -> "❌"
                    "glasses" -> "👓"
                    "sunglasses" -> "🕶️"
                    "headphones" -> "🎧"
                    "hat" -> "🎩"
                    "bow" -> "🎀"
                    else -> "?"
                }
                Surface(
                    onClick = { onChange(config.copy(accessory = acc)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (config.accessory == acc) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (config.accessory == acc) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(display, fontSize = 24.sp) }
                }
            }
        }
    }
}

@Composable
private fun AvatarBackgroundPanel(config: AvatarConfig, onChange: (AvatarConfig) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Background Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(BG_COLORS) { color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (config.bgColor == color) 3.dp else 1.dp,
                            color = if (config.bgColor == color) Color.White else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onChange(config.copy(bgColor = color)) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Tip: Your avatar is unique to you! Express yourself however feels right 💜",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


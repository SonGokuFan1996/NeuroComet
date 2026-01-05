package com.kyilmaz.neurocomet

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Full-featured Image Editor for Stories
 *
 * Features:
 * - Filters (brightness, contrast, saturation, etc.)
 * - Drawing tools with multiple brush sizes and colors
 * - Text overlays with multiple fonts and styles
 * - Stickers and emoji
 * - Crop and rotate
 * - Undo/Redo support
 */

// ============================================================================
// Data Models
// ============================================================================

data class ImageEditState(
    val filter: StoryImageFilter = StoryImageFilter.NONE,
    val brightness: Float = 0f,      // -1 to 1
    val contrast: Float = 1f,        // 0.5 to 2
    val saturation: Float = 1f,      // 0 to 2
    val temperature: Float = 0f,     // -1 to 1 (warm/cool)
    val vignette: Float = 0f,        // 0 to 1
    val rotation: Float = 0f,        // degrees
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val textOverlays: List<StoryTextOverlay> = emptyList(),
    val drawingPaths: List<StoryDrawingPath> = emptyList(),
    val stickers: List<StoryStickerOverlay> = emptyList()
)

enum class StoryImageFilter(val displayName: String, val colorMatrix: FloatArray?) {
    NONE("Original", null),
    VIVID("Vivid", floatArrayOf(
        1.3f, 0f, 0f, 0f, 10f,
        0f, 1.3f, 0f, 0f, 10f,
        0f, 0f, 1.3f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    )),
    WARM("Warm", floatArrayOf(
        1.2f, 0f, 0f, 0f, 20f,
        0f, 1.1f, 0f, 0f, 10f,
        0f, 0f, 0.9f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
    )),
    COOL("Cool", floatArrayOf(
        0.9f, 0f, 0f, 0f, -10f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.2f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    )),
    GRAYSCALE("B&W", floatArrayOf(
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0.33f, 0.33f, 0.33f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )),
    SEPIA("Sepia", floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )),
    VINTAGE("Vintage", floatArrayOf(
        0.9f, 0.1f, 0.1f, 0f, 20f,
        0.1f, 0.8f, 0.1f, 0f, 10f,
        0.1f, 0.1f, 0.6f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )),
    DRAMATIC("Dramatic", floatArrayOf(
        1.5f, -0.2f, -0.2f, 0f, -30f,
        -0.2f, 1.5f, -0.2f, 0f, -30f,
        -0.2f, -0.2f, 1.5f, 0f, -30f,
        0f, 0f, 0f, 1f, 0f
    )),
    CALM("Calm", floatArrayOf(
        0.8f, 0.1f, 0.1f, 0f, 10f,
        0.1f, 0.9f, 0.1f, 0f, 10f,
        0.1f, 0.1f, 1f, 0f, 20f,
        0f, 0f, 0f, 1f, 0f
    )),
    FOCUS("Focus", floatArrayOf(
        1.1f, 0f, 0f, 0f, 5f,
        0f, 1.1f, 0f, 0f, 5f,
        0f, 0f, 1.1f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    )),
    DREAMY("Dreamy", floatArrayOf(
        1.1f, 0.1f, 0.1f, 0f, 30f,
        0.1f, 1.1f, 0.1f, 0f, 30f,
        0.1f, 0.1f, 1.2f, 0f, 40f,
        0f, 0f, 0f, 1f, 0f
    ))
}

data class StoryTextOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val position: Offset,
    val color: Color = Color.White,
    val fontSize: Float = 24f,
    val fontFamily: StoryFont = StoryFont.DEFAULT,
    val isBold: Boolean = false,
    val hasBackground: Boolean = true,
    val backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    val rotation: Float = 0f,
    val scale: Float = 1f
)

data class StoryDrawingPath(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

data class StoryStickerOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val emoji: String,
    val position: Offset,
    val scale: Float = 1f,
    val rotation: Float = 0f
)

enum class StoryFont(val displayName: String, val fontFamily: FontFamily) {
    DEFAULT("Default", FontFamily.Default),
    SERIF("Serif", FontFamily.Serif),
    MONO("Mono", FontFamily.Monospace),
    CURSIVE("Cursive", FontFamily.Cursive)
}

enum class EditorTool {
    NONE, FILTER, ADJUST, DRAW, TEXT, STICKER, CROP
}

// ============================================================================
// Main Image Editor Composable
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onSave: (ImageEditState) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var editState by remember { mutableStateOf(ImageEditState()) }
    var selectedTool by remember { mutableStateOf(EditorTool.NONE) }
    var undoStack by remember { mutableStateOf(listOf<ImageEditState>()) }
    var redoStack by remember { mutableStateOf(listOf<ImageEditState>()) }

    // Drawing state
    var currentDrawingPath by remember { mutableStateOf(listOf<Offset>()) }
    var drawingColor by remember { mutableStateOf(Color.White) }
    var brushSize by remember { mutableStateOf(8f) }
    var isHighlighter by remember { mutableStateOf(false) }

    // Text overlay state
    var showTextInput by remember { mutableStateOf(false) }
    var currentText by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(Color.White) }
    var textFont by remember { mutableStateOf(StoryFont.DEFAULT) }
    var textHasBackground by remember { mutableStateOf(true) }

    // Canvas size for coordinate mapping
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    fun saveState() {
        undoStack = undoStack + editState
        redoStack = emptyList()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack = redoStack + editState
            editState = undoStack.last()
            undoStack = undoStack.dropLast(1)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack = undoStack + editState
            editState = redoStack.last()
            redoStack = redoStack.dropLast(1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Image", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    },
                    actions = {
                        // Undo/Redo
                        IconButton(
                            onClick = { undo() },
                            enabled = undoStack.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                        }
                        IconButton(
                            onClick = { redo() },
                            enabled = redoStack.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, "Redo")
                        }
                        // Save
                        TextButton(
                            onClick = { onSave(editState) }
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Image Preview with overlays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(selectedTool) {
                            if (selectedTool == EditorTool.DRAW) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentDrawingPath = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        currentDrawingPath = currentDrawingPath + change.position
                                    },
                                    onDragEnd = {
                                        if (currentDrawingPath.size > 1) {
                                            saveState()
                                            editState = editState.copy(
                                                drawingPaths = editState.drawingPaths + StoryDrawingPath(
                                                    points = currentDrawingPath,
                                                    color = drawingColor,
                                                    strokeWidth = brushSize,
                                                    isHighlighter = isHighlighter
                                                )
                                            )
                                        }
                                        currentDrawingPath = emptyList()
                                    }
                                )
                            } else if (selectedTool == EditorTool.TEXT) {
                                detectTapGestures { offset ->
                                    if (currentText.isNotBlank()) {
                                        saveState()
                                        editState = editState.copy(
                                            textOverlays = editState.textOverlays + StoryTextOverlay(
                                                text = currentText,
                                                position = offset,
                                                color = textColor,
                                                fontFamily = textFont,
                                                hasBackground = textHasBackground
                                            )
                                        )
                                        currentText = ""
                                    } else {
                                        showTextInput = true
                                    }
                                }
                            } else if (selectedTool == EditorTool.STICKER) {
                                // Handled separately
                            }
                        }
                ) {
                    // Base image with filter
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Editing image",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = editState.rotation
                                scaleX = if (editState.flipHorizontal) -1f else 1f
                                scaleY = if (editState.flipVertical) -1f else 1f
                            }
                            .then(
                                if (editState.filter.colorMatrix != null) {
                                    Modifier.drawWithContent {
                                        drawContent()
                                    }
                                } else Modifier
                            ),
                        contentScale = ContentScale.Fit,
                        colorFilter = editState.filter.colorMatrix?.let {
                            ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(it))
                        }
                    )

                    // Drawing overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Existing paths
                        editState.drawingPaths.forEach { path ->
                            if (path.points.size > 1) {
                                val pathColor = if (path.isHighlighter)
                                    path.color.copy(alpha = 0.4f)
                                else
                                    path.color

                                for (i in 0 until path.points.size - 1) {
                                    drawLine(
                                        color = pathColor,
                                        start = path.points[i],
                                        end = path.points[i + 1],
                                        strokeWidth = path.strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        // Current drawing path
                        if (currentDrawingPath.size > 1) {
                            val pathColor = if (isHighlighter)
                                drawingColor.copy(alpha = 0.4f)
                            else
                                drawingColor

                            for (i in 0 until currentDrawingPath.size - 1) {
                                drawLine(
                                    color = pathColor,
                                    start = currentDrawingPath[i],
                                    end = currentDrawingPath[i + 1],
                                    strokeWidth = brushSize,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Text overlays
                    editState.textOverlays.forEach { textOverlay ->
                        TextOverlayItem(
                            overlay = textOverlay,
                            onMove = { newPosition ->
                                saveState()
                                editState = editState.copy(
                                    textOverlays = editState.textOverlays.map {
                                        if (it.id == textOverlay.id) it.copy(position = newPosition)
                                        else it
                                    }
                                )
                            },
                            onDelete = {
                                saveState()
                                editState = editState.copy(
                                    textOverlays = editState.textOverlays.filter { it.id != textOverlay.id }
                                )
                            }
                        )
                    }

                    // Sticker overlays
                    editState.stickers.forEach { sticker ->
                        StickerOverlayItem(
                            sticker = sticker,
                            onMove = { newPosition ->
                                saveState()
                                editState = editState.copy(
                                    stickers = editState.stickers.map {
                                        if (it.id == sticker.id) it.copy(position = newPosition)
                                        else it
                                    }
                                )
                            },
                            onDelete = {
                                saveState()
                                editState = editState.copy(
                                    stickers = editState.stickers.filter { it.id != sticker.id }
                                )
                            }
                        )
                    }
                }

                // Tool panels
                AnimatedVisibility(
                    visible = selectedTool != EditorTool.NONE,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ) {
                        when (selectedTool) {
                            EditorTool.FILTER -> FilterPanel(
                                selectedFilter = editState.filter,
                                onFilterSelect = { filter ->
                                    saveState()
                                    editState = editState.copy(filter = filter)
                                }
                            )
                            EditorTool.ADJUST -> AdjustPanel(
                                brightness = editState.brightness,
                                contrast = editState.contrast,
                                saturation = editState.saturation,
                                onBrightnessChange = {
                                    editState = editState.copy(brightness = it)
                                },
                                onContrastChange = {
                                    editState = editState.copy(contrast = it)
                                },
                                onSaturationChange = {
                                    editState = editState.copy(saturation = it)
                                },
                                onCommit = { saveState() }
                            )
                            EditorTool.DRAW -> DrawPanel(
                                selectedColor = drawingColor,
                                onColorSelect = { drawingColor = it },
                                brushSize = brushSize,
                                onBrushSizeChange = { brushSize = it },
                                isHighlighter = isHighlighter,
                                onHighlighterToggle = { isHighlighter = it },
                                onClearAll = {
                                    saveState()
                                    editState = editState.copy(drawingPaths = emptyList())
                                }
                            )
                            EditorTool.TEXT -> TextPanel(
                                text = currentText,
                                onTextChange = { currentText = it },
                                selectedColor = textColor,
                                onColorSelect = { textColor = it },
                                selectedFont = textFont,
                                onFontSelect = { textFont = it },
                                hasBackground = textHasBackground,
                                onBackgroundToggle = { textHasBackground = it }
                            )
                            EditorTool.STICKER -> StickerPanel(
                                onStickerSelect = { emoji ->
                                    saveState()
                                    editState = editState.copy(
                                        stickers = editState.stickers + StoryStickerOverlay(
                                            emoji = emoji,
                                            position = Offset(
                                                canvasSize.width / 2f,
                                                canvasSize.height / 2f
                                            )
                                        )
                                    )
                                }
                            )
                            EditorTool.CROP -> CropRotatePanel(
                                rotation = editState.rotation,
                                onRotate = { angle ->
                                    saveState()
                                    editState = editState.copy(rotation = editState.rotation + angle)
                                },
                                onFlipHorizontal = {
                                    saveState()
                                    editState = editState.copy(flipHorizontal = !editState.flipHorizontal)
                                },
                                onFlipVertical = {
                                    saveState()
                                    editState = editState.copy(flipVertical = !editState.flipVertical)
                                }
                            )
                            else -> {}
                        }
                    }
                }

                // Bottom toolbar
                EditorToolbar(
                    selectedTool = selectedTool,
                    onToolSelect = { tool ->
                        selectedTool = if (selectedTool == tool) EditorTool.NONE else tool
                    }
                )
            }
        }
    }

    // Text input dialog
    if (showTextInput) {
        TextInputDialog(
            onDismiss = { showTextInput = false },
            onConfirm = { text ->
                currentText = text
                showTextInput = false
            }
        )
    }
}

// ============================================================================
// Tool Panels
// ============================================================================

@Composable
private fun FilterPanel(
    selectedFilter: StoryImageFilter,
    onFilterSelect: (StoryImageFilter) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Filters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(StoryImageFilter.entries.toList()) { filter ->
                FilterItem(
                    filter = filter,
                    isSelected = filter == selectedFilter,
                    onClick = { onFilterSelect(filter) }
                )
            }
        }
    }
}

@Composable
private fun FilterItem(
    filter: StoryImageFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val filterColor = when (filter) {
        StoryImageFilter.NONE -> Color.Gray
        StoryImageFilter.VIVID -> Color(0xFFFF6B6B)
        StoryImageFilter.WARM -> Color(0xFFFFB347)
        StoryImageFilter.COOL -> Color(0xFF4ECDC4)
        StoryImageFilter.GRAYSCALE -> Color.DarkGray
        StoryImageFilter.SEPIA -> Color(0xFF8B7355)
        StoryImageFilter.VINTAGE -> Color(0xFFD4A574)
        StoryImageFilter.DRAMATIC -> Color(0xFF2C3E50)
        StoryImageFilter.CALM -> Color(0xFF7FB3D5)
        StoryImageFilter.FOCUS -> Color(0xFF48C9B0)
        StoryImageFilter.DREAMY -> Color(0xFFBB8FCE)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(filterColor)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                filter.displayName.first().toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            filter.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AdjustPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onCommit: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Adjustments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // Brightness
        AdjustSlider(
            label = "Brightness",
            icon = Icons.Outlined.WbSunny,
            value = brightness,
            valueRange = -1f..1f,
            onValueChange = onBrightnessChange,
            onValueChangeFinished = onCommit
        )

        // Contrast
        AdjustSlider(
            label = "Contrast",
            icon = Icons.Outlined.Contrast,
            value = contrast,
            valueRange = 0.5f..2f,
            onValueChange = onContrastChange,
            onValueChangeFinished = onCommit
        )

        // Saturation
        AdjustSlider(
            label = "Saturation",
            icon = Icons.Outlined.Palette,
            value = saturation,
            valueRange = 0f..2f,
            onValueChange = onSaturationChange,
            onValueChangeFinished = onCommit
        )
    }
}

@Composable
private fun AdjustSlider(
    label: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DrawPanel(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    isHighlighter: Boolean,
    onHighlighterToggle: (Boolean) -> Unit,
    onClearAll: () -> Unit
) {
    val colors = listOf(
        Color.White, Color.Black, Color.Red, Color.Yellow,
        Color.Green, Color.Cyan, Color.Blue, Color.Magenta,
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Draw",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                FilterChip(
                    selected = isHighlighter,
                    onClick = { onHighlighterToggle(!isHighlighter) },
                    label = { Text("Highlighter") },
                    leadingIcon = if (isHighlighter) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Filled.Delete, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Colors
        Text("Color", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            if (color == selectedColor) 3.dp else 1.dp,
                            if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                        .clickable { onColorSelect(color) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Brush size
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Size", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(12.dp))
            Slider(
                value = brushSize,
                onValueChange = onBrushSizeChange,
                valueRange = 2f..30f,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(brushSize.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
            )
        }
    }
}

@Composable
private fun TextPanel(
    text: String,
    onTextChange: (String) -> Unit,
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    selectedFont: StoryFont,
    onFontSelect: (StoryFont) -> Unit,
    hasBackground: Boolean,
    onBackgroundToggle: (Boolean) -> Unit
) {
    val colors = listOf(
        Color.White, Color.Black, Color.Red, Color.Yellow,
        Color.Green, Color.Cyan, Color.Blue, Color.Magenta
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Add Text",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.image_editor_type_text)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2
        )

        Spacer(Modifier.height(12.dp))

        // Font selection
        Text("Font", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(StoryFont.entries) { font ->
                FilterChip(
                    selected = font == selectedFont,
                    onClick = { onFontSelect(font) },
                    label = {
                        Text(
                            font.displayName,
                            fontFamily = font.fontFamily
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Color and background
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.take(6).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                if (color == selectedColor) 2.dp else 1.dp,
                                if (color == selectedColor) MaterialTheme.colorScheme.primary else Color.Gray,
                                CircleShape
                            )
                            .clickable { onColorSelect(color) }
                    )
                }
            }
            FilterChip(
                selected = hasBackground,
                onClick = { onBackgroundToggle(!hasBackground) },
                label = { Text("Background") }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Tap on image to place text",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StickerPanel(
    onStickerSelect: (String) -> Unit
) {
    val stickerCategories = listOf(
        "Reactions" to listOf("😊", "😂", "🥰", "😍", "🤩", "😎", "🥳", "😇", "🤗", "🫶"),
        "ND Friendly" to listOf("🧠", "♾️", "💜", "🌈", "✨", "💫", "⭐", "🌟", "💖", "🦋"),
        "Nature" to listOf("🌸", "🌺", "🌻", "🌼", "🌷", "🌹", "🍀", "🌿", "🌴", "🌙"),
        "Objects" to listOf("📚", "🎨", "🎵", "🎮", "☕", "🧸", "🎈", "🎁", "💡", "🔮"),
        "Celebration" to listOf("🎉", "🎊", "🥳", "🎂", "🍰", "🎆", "🎇", "🏆", "🥇", "👑")
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Stickers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stickerCategories.forEach { (category, stickers) ->
                Text(
                    category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stickers) { emoji ->
                        Surface(
                            onClick = { onStickerSelect(emoji) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                emoji,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CropRotatePanel(
    rotation: Float,
    onRotate: (Float) -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Transform",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            @Suppress("DEPRECATION")
            TransformButton(
                icon = Icons.Filled.RotateLeft,
                label = "Rotate Left",
                onClick = { onRotate(-90f) }
            )
            @Suppress("DEPRECATION")
            TransformButton(
                icon = Icons.Filled.RotateRight,
                label = "Rotate Right",
                onClick = { onRotate(90f) }
            )
            TransformButton(
                icon = Icons.Filled.Flip,
                label = "Flip H",
                onClick = onFlipHorizontal
            )
            TransformButton(
                icon = Icons.Outlined.SwapVert,
                label = "Flip V",
                onClick = onFlipVertical
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Current rotation: ${rotation.toInt()}°",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TransformButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ============================================================================
// Bottom Toolbar
// ============================================================================

@Composable
private fun EditorToolbar(
    selectedTool: EditorTool,
    onToolSelect: (EditorTool) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolbarItem(
                icon = Icons.Outlined.AutoAwesome,
                label = "Filters",
                isSelected = selectedTool == EditorTool.FILTER,
                onClick = { onToolSelect(EditorTool.FILTER) }
            )
            ToolbarItem(
                icon = Icons.Outlined.Tune,
                label = "Adjust",
                isSelected = selectedTool == EditorTool.ADJUST,
                onClick = { onToolSelect(EditorTool.ADJUST) }
            )
            ToolbarItem(
                icon = Icons.Outlined.Draw,
                label = "Draw",
                isSelected = selectedTool == EditorTool.DRAW,
                onClick = { onToolSelect(EditorTool.DRAW) }
            )
            ToolbarItem(
                icon = Icons.Outlined.TextFields,
                label = "Text",
                isSelected = selectedTool == EditorTool.TEXT,
                onClick = { onToolSelect(EditorTool.TEXT) }
            )
            ToolbarItem(
                icon = Icons.Outlined.EmojiEmotions,
                label = "Sticker",
                isSelected = selectedTool == EditorTool.STICKER,
                onClick = { onToolSelect(EditorTool.STICKER) }
            )
            ToolbarItem(
                icon = Icons.Outlined.Crop,
                label = "Crop",
                isSelected = selectedTool == EditorTool.CROP,
                onClick = { onToolSelect(EditorTool.CROP) }
            )
        }
    }
}

@Composable
private fun ToolbarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "toolScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// Overlay Items
// ============================================================================

@Composable
private fun TextOverlayItem(
    overlay: StoryTextOverlay,
    onMove: (Offset) -> Unit,
    onDelete: () -> Unit
) {
    var offset by remember { mutableStateOf(overlay.position) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { offset.x.toDp() - 50.dp },
                y = with(LocalDensity.current) { offset.y.toDp() - 20.dp }
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDrag = { change, dragAmount ->
                        offset = Offset(
                            offset.x + dragAmount.x,
                            offset.y + dragAmount.y
                        )
                    },
                    onDragEnd = {
                        isDragging = false
                        onMove(offset)
                    }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isDragging) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (overlay.hasBackground) overlay.backgroundColor else Color.Transparent
            ) {
                Text(
                    overlay.text,
                    color = overlay.color,
                    fontSize = overlay.fontSize.sp,
                    fontFamily = overlay.fontFamily.fontFamily,
                    fontWeight = if (overlay.isBold) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun StickerOverlayItem(
    sticker: StoryStickerOverlay,
    onMove: (Offset) -> Unit,
    onDelete: () -> Unit
) {
    var offset by remember { mutableStateOf(sticker.position) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { offset.x.toDp() - 24.dp },
                y = with(LocalDensity.current) { offset.y.toDp() - 24.dp }
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDrag = { change, dragAmount ->
                        offset = Offset(
                            offset.x + dragAmount.x,
                            offset.y + dragAmount.y
                        )
                    },
                    onDragEnd = {
                        isDragging = false
                        onMove(offset)
                    }
                )
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isDragging) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Text(
                sticker.emoji,
                fontSize = (48 * sticker.scale).sp,
                modifier = Modifier.graphicsLayer { rotationZ = sticker.rotation }
            )
        }
    }
}

// ============================================================================
// Text Input Dialog
// ============================================================================

@Composable
private fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Text") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.image_editor_enter_text)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


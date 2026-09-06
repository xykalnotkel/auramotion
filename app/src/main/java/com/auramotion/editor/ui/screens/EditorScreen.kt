package com.auramotion.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.model.CurveData
import com.auramotion.editor.model.LayerType
import com.auramotion.editor.ui.components.*
import com.auramotion.editor.ui.theme.*
import com.auramotion.editor.viewmodel.EditorViewModel

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0: Inspector, 1: Graph Editor

    val selectedLayer = uiState.project.layers.find { it.id == uiState.selectedLayerId }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = uiState.project.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = TextPrimary)
                    }
                    IconButton(onClick = { viewModel.redo() }) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo", tint = TextPrimary)
                    }
                    IconButton(onClick = { viewModel.setDialogVisible("effects", true) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "FX", tint = NeonCyan)
                    }
                    Button(
                        onClick = { viewModel.setDialogVisible("export", true) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBg),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Export", fontSize = 12.sp)
                    }
                }
            }
        },
        containerColor = DarkBg
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Viewport Canvas Area (Top Half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
            ) {
                CanvasViewport(
                    project = uiState.project,
                    currentTime = uiState.currentTime,
                    selectedLayerId = uiState.selectedLayerId,
                    onUpdateLayerTransform = { id, x, y, sx, sy, rot ->
                        viewModel.updateLayerTransform(id, x, y, sx, sy, rot, null)
                    },
                    onSelectLayer = { viewModel.selectLayer(it) }
                )
            }

            // Bottom Section: Timeline & Property/Graph Inspector (Bottom Half)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(DarkSurface)
            ) {
                // Timeline Track View
                TimelineView(
                    layers = uiState.project.layers,
                    selectedLayerId = uiState.selectedLayerId,
                    currentTime = uiState.currentTime,
                    duration = uiState.project.duration,
                    fps = uiState.project.fps,
                    isPlaying = uiState.isPlaying,
                    onSeek = { viewModel.seek(it) },
                    onTogglePlay = { viewModel.togglePlayback() },
                    onSelectLayer = { viewModel.selectLayer(it) },
                    onToggleKeyframe = { layerId, prop -> viewModel.toggleKeyframeAtPlayhead(layerId, prop) },
                    onDeleteLayer = { viewModel.deleteSelectedLayer() },
                    onAddLayerClick = { viewModel.setDialogVisible("shape", true) }
                )

                // Tab Switcher between Inspector & Easing Curve Graph
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton("Transform & Keyframes", selected = selectedBottomTab == 0) {
                        selectedBottomTab = 0
                    }
                    TabButton("Bézier Graph Editor", selected = selectedBottomTab == 1) {
                        selectedBottomTab = 1
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (selectedBottomTab == 0) {
                        if (selectedLayer != null) {
                            LayerInspector(
                                layer = selectedLayer,
                                currentTime = uiState.currentTime,
                                onUpdateTransform = { x, y, sx, sy, rot, op ->
                                    viewModel.updateLayerTransform(selectedLayer.id, x, y, sx, sy, rot, op)
                                },
                                onToggleKeyframe = { prop ->
                                    viewModel.toggleKeyframeAtPlayhead(selectedLayer.id, prop)
                                },
                                onOpenCurveEditor = { prop ->
                                    selectedBottomTab = 1
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Select a layer from the timeline to edit properties", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    } else {
                        // Bézier Graph Editor
                        CurveGraphEditor(
                            curve = CurveData.EaseInOut,
                            onCurveChanged = { newCurve ->
                                if (selectedLayer != null) {
                                    val firstKf = selectedLayer.posKeyframes.firstOrNull()?.id
                                    if (firstKf != null) {
                                        viewModel.updateKeyframeCurve(selectedLayer.id, "position", firstKf, newCurve)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showShapeDialog) {
        ShapePickerDialog(
            onDismiss = { viewModel.setDialogVisible("shape", false) },
            onSelectShape = { type -> viewModel.addShapeLayer(type) }
        )
    }

    if (uiState.showEffectsDialog) {
        EffectsBrowserDialog(
            onDismiss = { viewModel.setDialogVisible("effects", false) },
            onSelectEffect = { fxType -> viewModel.addEffectToSelected(fxType) }
        )
    }

    if (uiState.showExportDialog) {
        ExportDialog(
            onDismiss = { viewModel.setDialogVisible("export", false) },
            onStartExport = { res, fps, fmt ->
                // Trigger Export Pipeline
            }
        )
    }
}

@Composable
private fun TabButton(title: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = if (selected) NeonCyan else TextSecondary)
    ) {
        Text(text = title, fontSize = 12.sp, style = if (selected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
    }
}

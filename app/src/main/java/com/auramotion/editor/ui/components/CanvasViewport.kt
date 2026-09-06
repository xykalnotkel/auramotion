package com.auramotion.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.model.LayerData
import com.auramotion.editor.model.LayerType
import com.auramotion.editor.model.ProjectConfig
import com.auramotion.editor.model.ShapeType
import com.auramotion.editor.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CanvasViewport(
    project: ProjectConfig,
    currentTime: Float,
    selectedLayerId: String?,
    onUpdateLayerTransform: (layerId: String, posX: Float?, posY: Float?, scaleX: Float?, scaleY: Float?, rotZ: Float?) -> Unit,
    onSelectLayer: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasZoom by remember { mutableFloatStateOf(1.0f) }
    var canvasPan by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    canvasZoom = (canvasZoom * zoom).coerceIn(0.25f, 4.0f)
                    canvasPan += pan
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val selectedLayer = project.layers.find { it.id == selectedLayerId }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedLayerId) {
                    if (selectedLayer != null) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = selectedLayer.posX + dragAmount.x / canvasZoom
                            val newY = selectedLayer.posY + dragAmount.y / canvasZoom
                            onUpdateLayerTransform(selectedLayer.id, newX, newY, null, null, null)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f + canvasPan.x, size.height / 2f + canvasPan.y)

            // Canvas aspect ratio bounds (e.g. 1080x1920)
            val aspect = project.width.toFloat() / project.height.toFloat()
            val baseCanvasHeight = size.height * 0.75f * canvasZoom
            val baseCanvasWidth = baseCanvasHeight * aspect

            val canvasTopLeft = Offset(
                center.x - baseCanvasWidth / 2f,
                center.y - baseCanvasHeight / 2f
            )

            // 1. Draw Canvas Background
            drawRect(
                color = Color(project.backgroundColor),
                topLeft = canvasTopLeft,
                size = Size(baseCanvasWidth, baseCanvasHeight)
            )

            // 2. Draw Canvas Border Frame
            drawRect(
                color = DarkSurfaceBorder,
                topLeft = canvasTopLeft,
                size = Size(baseCanvasWidth, baseCanvasHeight),
                style = Stroke(width = 2f)
            )

            // 3. Render Layers
            project.layers.forEach { layer ->
                if (!layer.isVisible || currentTime < layer.startTime || currentTime > layer.endTime) return@forEach

                val layerCenter = Offset(
                    center.x + layer.posX * canvasZoom,
                    center.y + layer.posY * canvasZoom
                )

                rotate(layer.rotationZ, pivot = layerCenter) {
                    when (layer.type) {
                        LayerType.SHAPE -> {
                            val cfg = layer.shapeConfig
                            val shapeW = (cfg?.width ?: 200f) * layer.scaleX * canvasZoom
                            val shapeH = (cfg?.height ?: 200f) * layer.scaleY * canvasZoom
                            val topLeft = Offset(layerCenter.x - shapeW / 2f, layerCenter.y - shapeH / 2f)

                            when (cfg?.shapeType) {
                                ShapeType.CIRCLE -> {
                                    if (cfg.hasFill) {
                                        drawCircle(
                                            color = Color(cfg.fillColor).copy(alpha = layer.opacity),
                                            radius = shapeW / 2f,
                                            center = layerCenter
                                        )
                                    }
                                    if (cfg.hasStroke) {
                                        drawCircle(
                                            color = Color(cfg.strokeColor).copy(alpha = layer.opacity),
                                            radius = shapeW / 2f,
                                            center = layerCenter,
                                            style = Stroke(width = cfg.strokeWidth * canvasZoom)
                                        )
                                    }
                                }
                                ShapeType.STAR -> {
                                    val starPath = Path()
                                    val pts = 5
                                    val rOuter = shapeW / 2f
                                    val rInner = rOuter * 0.5f
                                    for (i in 0 until pts * 2) {
                                        val angle = (i * Math.PI / pts) - Math.PI / 2
                                        val r = if (i % 2 == 0) rOuter else rInner
                                        val px = layerCenter.x + (r * cos(angle)).toFloat()
                                        val py = layerCenter.y + (r * sin(angle)).toFloat()
                                        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                                    }
                                    starPath.close()

                                    if (cfg.hasFill) {
                                        drawPath(starPath, Color(cfg.fillColor).copy(alpha = layer.opacity))
                                    }
                                    if (cfg.hasStroke) {
                                        drawPath(
                                            starPath,
                                            Color(cfg.strokeColor).copy(alpha = layer.opacity),
                                            style = Stroke(width = cfg.strokeWidth * canvasZoom)
                                        )
                                    }
                                }
                                else -> {
                                    // Rectangle / Rounded Card
                                    val cornerR = (cfg?.cornerRadius ?: 0f) * canvasZoom
                                    if (cfg?.hasFill == true) {
                                        drawRoundRect(
                                            color = Color(cfg.fillColor).copy(alpha = layer.opacity),
                                            topLeft = topLeft,
                                            size = Size(shapeW, shapeH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR, cornerR)
                                        )
                                    }
                                    if (cfg?.hasStroke == true) {
                                        drawRoundRect(
                                            color = Color(cfg.strokeColor).copy(alpha = layer.opacity),
                                            topLeft = topLeft,
                                            size = Size(shapeW, shapeH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR, cornerR),
                                            style = Stroke(width = cfg.strokeWidth * canvasZoom)
                                        )
                                    }
                                }
                            }
                        }
                        LayerType.TEXT -> {
                            val textCfg = layer.textConfig
                            // Direct preview placeholder for text bounds
                            val textW = 320f * layer.scaleX * canvasZoom
                            val textH = (textCfg?.fontSize ?: 48f) * layer.scaleY * canvasZoom
                            val topLeft = Offset(layerCenter.x - textW / 2f, layerCenter.y - textH / 2f)

                            drawRoundRect(
                                color = DarkSurfaceVariant.copy(alpha = 0.5f * layer.opacity),
                                topLeft = topLeft,
                                size = Size(textW, textH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                            )
                        }
                        else -> {}
                    }
                }
            }

            // 4. Draw Transform Gizmo Handles for Selected Layer
            if (selectedLayer != null) {
                val layerCenter = Offset(
                    center.x + selectedLayer.posX * canvasZoom,
                    center.y + selectedLayer.posY * canvasZoom
                )
                val boxW = 260f * selectedLayer.scaleX * canvasZoom
                val boxH = 260f * selectedLayer.scaleY * canvasZoom
                val boxTopLeft = Offset(layerCenter.x - boxW / 2f, layerCenter.y - boxH / 2f)

                rotate(selectedLayer.rotationZ, pivot = layerCenter) {
                    // Bounding Box
                    drawRect(
                        color = NeonCyan,
                        topLeft = boxTopLeft,
                        size = Size(boxW, boxH),
                        style = Stroke(width = 2f)
                    )

                    // 4 Corner Handles
                    val handleRadius = 6f
                    drawCircle(color = Color.White, radius = handleRadius, center = boxTopLeft)
                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(boxTopLeft.x + boxW, boxTopLeft.y))
                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(boxTopLeft.x, boxTopLeft.y + boxH))
                    drawCircle(color = Color.White, radius = handleRadius, center = Offset(boxTopLeft.x + boxW, boxTopLeft.y + boxH))

                    // Rotation Stem & Handle
                    val rotHandlePos = Offset(layerCenter.x, boxTopLeft.y - 30f)
                    drawLine(
                        color = NeonCyan,
                        start = Offset(layerCenter.x, boxTopLeft.y),
                        end = rotHandlePos,
                        strokeWidth = 2f
                    )
                    drawCircle(color = NeonPink, radius = 7f, center = rotHandlePos)

                    // Anchor Point Crosshair
                    drawCircle(color = KeyframeDiamond, radius = 4f, center = layerCenter)
                    drawLine(color = KeyframeDiamond, start = Offset(layerCenter.x - 8f, layerCenter.y), end = Offset(layerCenter.x + 8f, layerCenter.y), strokeWidth = 1.5f)
                    drawLine(color = KeyframeDiamond, start = Offset(layerCenter.x, layerCenter.y - 8f), end = Offset(layerCenter.x, layerCenter.y + 8f), strokeWidth = 1.5f)
                }
            }
        }

        // Viewport HUD Indicator (Zoom factor)
        Text(
            text = "${(canvasZoom * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(DarkSurfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

package com.auramotion.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.model.LayerData
import com.auramotion.editor.model.LayerType
import com.auramotion.editor.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TimelineView(
    layers: List<LayerData>,
    selectedLayerId: String?,
    currentTime: Float,
    duration: Float,
    fps: Int,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleKeyframe: (String, String) -> Unit,
    onDeleteLayer: () -> Unit,
    onAddLayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
    ) {
        // --- Timeline Header / Controls Toolbar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timecode Display
            val totalFrames = (currentTime * fps).roundToInt()
            val seconds = currentTime.toInt()
            val frames = totalFrames % fps
            val timecode = String.format("%02d:%02d.%02d", seconds / 60, seconds % 60, frames)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timecode,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(duration * fps).toInt()}f / ${duration}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Transport Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Step Back 1 Frame
                IconButton(
                    onClick = { onSeek((currentTime - 1f / fps).coerceAtLeast(0f)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Step Back", tint = TextPrimary)
                }

                // Play / Pause
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isPlaying) NeonPink else NeonCyan, RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = DarkBg
                    )
                }

                // Step Forward 1 Frame
                IconButton(
                    onClick = { onSeek((currentTime + 1f / fps).coerceAtMost(duration)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Step Forward", tint = TextPrimary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Keyframe Add Diamond Button (Alight Motion signature action)
                if (selectedLayerId != null) {
                    IconButton(
                        onClick = { onToggleKeyframe(selectedLayerId, "position") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Diamond, contentDescription = "Toggle Keyframe", tint = KeyframeDiamond)
                    }
                }

                // Add Layer Button
                IconButton(
                    onClick = onAddLayerClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Layer", tint = NeonEmerald)
                }

                // Delete Layer Button
                if (selectedLayerId != null) {
                    IconButton(
                        onClick = onDeleteLayer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Layer", tint = TextSecondary)
                    }
                }
            }
        }

        // --- Ruler & Multi-Track Scrubber Canvas ---
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkBg)
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val trackHeight = 36f
            val rulerHeight = 24f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(duration) {
                        detectTapGestures { offset ->
                            val seekRatio = (offset.x / canvasWidth).coerceIn(0f, 1f)
                            onSeek(seekRatio * duration)
                        }
                    }
                    .pointerInput(duration) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val seekRatio = (change.position.x / canvasWidth).coerceIn(0f, 1f)
                            onSeek(seekRatio * duration)
                        }
                    }
            ) {
                // 1. Draw Ruler Background
                drawRect(
                    color = DarkSurfaceVariant,
                    topLeft = Offset(0f, 0f),
                    size = Size(canvasWidth, rulerHeight)
                )

                // 2. Draw Ruler Ticks (Every Second & Sub-second)
                val totalSeconds = duration.toInt() + 1
                for (s in 0..totalSeconds) {
                    val x = (s / duration) * canvasWidth
                    drawLine(
                        color = TextSecondary,
                        start = Offset(x, 0f),
                        end = Offset(x, rulerHeight),
                        strokeWidth = 2f
                    )
                    // Sub-ticks
                    for (sub in 1..3) {
                        val subX = ((s + sub * 0.25f) / duration) * canvasWidth
                        if (subX <= canvasWidth) {
                            drawLine(
                                color = TextMuted,
                                start = Offset(subX, rulerHeight * 0.5f),
                                end = Offset(subX, rulerHeight),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // 3. Draw Tracks & Layer Clips
                layers.forEachIndexed { index, layer ->
                    val y = rulerHeight + index * (trackHeight + 6f) + 4f
                    val isSelected = layer.id == selectedLayerId

                    // Track Background Strip
                    drawRect(
                        color = if (isSelected) DarkSurfaceBorder else DarkSurface,
                        topLeft = Offset(0f, y),
                        size = Size(canvasWidth, trackHeight)
                    )

                    // Layer Clip
                    val clipStart = (layer.startTime / duration) * canvasWidth
                    val clipEnd = (layer.endTime / duration) * canvasWidth
                    val clipWidth = (clipEnd - clipStart).coerceAtLeast(10f)

                    val clipColor = when (layer.type) {
                        LayerType.SHAPE -> NeonCyan.copy(alpha = 0.35f)
                        LayerType.TEXT -> NeonPurple.copy(alpha = 0.35f)
                        LayerType.IMAGE -> NeonEmerald.copy(alpha = 0.35f)
                        LayerType.VIDEO -> NeonAmber.copy(alpha = 0.35f)
                        LayerType.ADJUSTMENT -> NeonPink.copy(alpha = 0.35f)
                        else -> TextMuted.copy(alpha = 0.35f)
                    }

                    val borderColor = if (isSelected) NeonCyan else Color.Transparent

                    drawRoundRect(
                        color = clipColor,
                        topLeft = Offset(clipStart, y + 2f),
                        size = Size(clipWidth, trackHeight - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    if (isSelected) {
                        drawRoundRect(
                            color = borderColor,
                            topLeft = Offset(clipStart, y + 2f),
                            size = Size(clipWidth, trackHeight - 4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }

                    // Keyframe Diamonds on Clip
                    val allKeyframes = (layer.posKeyframes.map { it.time } +
                            layer.scaleKeyframes.map { it.time } +
                            layer.rotKeyframes.map { it.time } +
                            layer.opacityKeyframes.map { it.time }).distinct()

                    allKeyframes.forEach { kfTime ->
                        val kfX = (kfTime / duration) * canvasWidth
                        val kfY = y + trackHeight / 2f
                        val diamondSize = 6f

                        val diamondPath = Path().apply {
                            moveTo(kfX, kfY - diamondSize)
                            lineTo(kfX + diamondSize, kfY)
                            lineTo(kfX, kfY + diamondSize)
                            lineTo(kfX - diamondSize, kfY)
                            close()
                        }
                        drawPath(diamondPath, KeyframeDiamond)
                    }
                }

                // 4. Draw Playhead Vertical Scrubber
                val playheadX = (currentTime / duration) * canvasWidth
                drawLine(
                    color = PlayheadRed,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 3f
                )

                // Playhead Head Triangle
                val playheadHead = Path().apply {
                    moveTo(playheadX - 8f, 0f)
                    lineTo(playheadX + 8f, 0f)
                    lineTo(playheadX, rulerHeight)
                    close()
                }
                drawPath(playheadHead, PlayheadRed)
            }
        }
    }
}

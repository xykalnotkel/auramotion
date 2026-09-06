package com.auramotion.editor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.model.CurveData
import com.auramotion.editor.ui.theme.*

@Composable
fun CurveGraphEditor(
    curve: CurveData,
    onCurveChanged: (CurveData) -> Unit,
    modifier: Modifier = Modifier
) {
    var p1x by remember(curve) { mutableFloatStateOf(curve.cp1x) }
    var p1y by remember(curve) { mutableFloatStateOf(curve.cp1y) }
    var p2x by remember(curve) { mutableFloatStateOf(curve.cp2x) }
    var p2y by remember(curve) { mutableFloatStateOf(curve.cp2y) }

    Column(
        modifier = modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Easing Graph Editor",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Text(
                text = "(${String.format("%.2f", p1x)}, ${String.format("%.2f", p1y)}) -> (${String.format("%.2f", p2x)}, ${String.format("%.2f", p2y)})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Bézier Graph Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(DarkBg, RoundedCornerShape(8.dp))
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val padding = 24f
            val graphW = width - padding * 2
            val graphH = height - padding * 2

            // Mapping helpers: math coords (0..1, 0..1) -> screen coords (padding..padding+graphW, padding+graphH..padding)
            fun toScreenX(x: Float) = padding + x * graphW
            fun toScreenY(y: Float) = padding + (1f - y) * graphH
            fun fromScreenX(sx: Float) = ((sx - padding) / graphW).coerceIn(0f, 1f)
            fun fromScreenY(sy: Float) = (1f - (sy - padding) / graphH).coerceIn(-0.5f, 1.5f)

            var draggingHandle by remember { mutableIntStateOf(0) } // 0: none, 1: P1, 2: P2

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val sP1 = Offset(toScreenX(p1x), toScreenY(p1y))
                                val sP2 = Offset(toScreenX(p2x), toScreenY(p2y))
                                val d1 = (offset - sP1).getDistance()
                                val d2 = (offset - sP2).getDistance()

                                draggingHandle = when {
                                    d1 < 40f -> 1
                                    d2 < 40f -> 2
                                    else -> 0
                                }
                            },
                            onDragEnd = { draggingHandle = 0 },
                            onDragCancel = { draggingHandle = 0 },
                            onDrag = { change, _ ->
                                change.consume()
                                when (draggingHandle) {
                                    1 -> {
                                        p1x = fromScreenX(change.position.x)
                                        p1y = fromScreenY(change.position.y)
                                        onCurveChanged(CurveData(p1x, p1y, p2x, p2y))
                                    }
                                    2 -> {
                                        p2x = fromScreenX(change.position.x)
                                        p2y = fromScreenY(change.position.y)
                                        onCurveChanged(CurveData(p1x, p1y, p2x, p2y))
                                    }
                                }
                            }
                        )
                    }
            ) {
                // 1. Draw Grid Lines
                drawLine(
                    color = DarkSurfaceBorder,
                    start = Offset(padding, padding),
                    end = Offset(padding + graphW, padding),
                    strokeWidth = 1f
                )
                drawLine(
                    color = DarkSurfaceBorder,
                    start = Offset(padding, padding + graphH),
                    end = Offset(padding + graphW, padding + graphH),
                    strokeWidth = 1f
                )
                drawLine(
                    color = DarkSurfaceBorder,
                    start = Offset(padding, padding + graphH / 2f),
                    end = Offset(padding + graphW, padding + graphH / 2f),
                    strokeWidth = 1f
                )

                // Diagonal Linear Reference Line
                drawLine(
                    color = TextMuted.copy(alpha = 0.4f),
                    start = Offset(toScreenX(0f), toScreenY(0f)),
                    end = Offset(toScreenX(1f), toScreenY(1f)),
                    strokeWidth = 1.5f
                )

                // 2. Tangent Arms
                val p0 = Offset(toScreenX(0f), toScreenY(0f))
                val cp1 = Offset(toScreenX(p1x), toScreenY(p1y))
                val cp2 = Offset(toScreenX(p2x), toScreenY(p2y))
                val p3 = Offset(toScreenX(1f), toScreenY(1f))

                // Arm P0 -> CP1
                drawLine(
                    color = NeonPink,
                    start = p0,
                    end = cp1,
                    strokeWidth = 2f
                )

                // Arm P3 -> CP2
                drawLine(
                    color = NeonCyan,
                    start = p3,
                    end = cp2,
                    strokeWidth = 2f
                )

                // 3. Draw Cubic Bézier Curve
                val curvePath = Path().apply {
                    moveTo(p0.x, p0.y)
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p3.x, p3.y)
                }
                drawPath(
                    path = curvePath,
                    color = NeonEmerald,
                    style = Stroke(width = 4f)
                )

                // 4. Control Point Knobs (Handles)
                drawCircle(color = NeonPink, radius = 8f, center = cp1)
                drawCircle(color = Color.White, radius = 4f, center = cp1)

                drawCircle(color = NeonCyan, radius = 8f, center = cp2)
                drawCircle(color = Color.White, radius = 4f, center = cp2)

                // Start & End Anchor dots
                drawCircle(color = TextPrimary, radius = 5f, center = p0)
                drawCircle(color = TextPrimary, radius = 5f, center = p3)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Curves Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PresetButton("Linear") {
                onCurveChanged(CurveData.Linear)
            }
            PresetButton("Ease In") {
                onCurveChanged(CurveData.EaseIn)
            }
            PresetButton("Ease Out") {
                onCurveChanged(CurveData.EaseOut)
            }
            PresetButton("Ease In-Out") {
                onCurveChanged(CurveData.EaseInOut)
            }
            PresetButton("Overshoot") {
                onCurveChanged(CurveData.Overshoot)
            }
        }
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurface,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Text(text = label, fontSize = 10.sp)
    }
}

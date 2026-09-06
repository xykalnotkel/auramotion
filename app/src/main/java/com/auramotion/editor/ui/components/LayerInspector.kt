package com.auramotion.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.model.BlendMode
import com.auramotion.editor.model.LayerData
import com.auramotion.editor.ui.theme.*

@Composable
fun LayerInspector(
    layer: LayerData,
    currentTime: Float,
    onUpdateTransform: (posX: Float?, posY: Float?, scaleX: Float?, scaleY: Float?, rotZ: Float?, opacity: Float?) -> Unit,
    onToggleKeyframe: (String) -> Unit,
    onOpenCurveEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        // Layer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = layer.name,
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan
            )
            Text(
                text = "${layer.type.name}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Divider(color = DarkSurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Position Slider & Keyframe Toggle
        PropertyRow(
            label = "Position X/Y",
            valueText = "(${layer.posX.toInt()}, ${layer.posY.toInt()})",
            hasKeyframes = layer.posKeyframes.isNotEmpty(),
            onToggleKeyframe = { onToggleKeyframe("position") },
            onOpenCurve = { onOpenCurveEditor("position") }
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Slider(
                    value = layer.posX,
                    onValueChange = { onUpdateTransform(it, null, null, null, null, null) },
                    valueRange = -500f..500f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
                Slider(
                    value = layer.posY,
                    onValueChange = { onUpdateTransform(null, it, null, null, null, null) },
                    valueRange = -800f..800f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }
        }

        // Scale Slider & Keyframe Toggle
        PropertyRow(
            label = "Scale",
            valueText = "${String.format("%.2f", layer.scaleX)}x",
            hasKeyframes = layer.scaleKeyframes.isNotEmpty(),
            onToggleKeyframe = { onToggleKeyframe("scale") },
            onOpenCurve = { onOpenCurveEditor("scale") }
        ) {
            Slider(
                value = layer.scaleX,
                onValueChange = { onUpdateTransform(null, null, it, it, null, null) },
                valueRange = 0.1f..3.0f,
                colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
            )
        }

        // Rotation Slider & Keyframe Toggle
        PropertyRow(
            label = "Rotation",
            valueText = "${layer.rotationZ.toInt()}°",
            hasKeyframes = layer.rotKeyframes.isNotEmpty(),
            onToggleKeyframe = { onToggleKeyframe("rotation") },
            onOpenCurve = { onOpenCurveEditor("rotation") }
        ) {
            Slider(
                value = layer.rotationZ,
                onValueChange = { onUpdateTransform(null, null, null, null, it, null) },
                valueRange = -360f..360f,
                colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
            )
        }

        // Opacity Slider & Keyframe Toggle
        PropertyRow(
            label = "Opacity",
            valueText = "${(layer.opacity * 100).toInt()}%",
            hasKeyframes = layer.opacityKeyframes.isNotEmpty(),
            onToggleKeyframe = { onToggleKeyframe("opacity") },
            onOpenCurve = { onOpenCurveEditor("opacity") }
        ) {
            Slider(
                value = layer.opacity,
                onValueChange = { onUpdateTransform(null, null, null, null, null, it) },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = NeonEmerald, activeTrackColor = NeonEmerald)
            )
        }
    }
}

@Composable
private fun PropertyRow(
    label: String,
    valueText: String,
    hasKeyframes: Boolean,
    onToggleKeyframe: () -> Unit,
    onOpenCurve: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = valueText, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))

                // Keyframe toggle icon
                IconButton(onClick = onToggleKeyframe, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Keyframe",
                        tint = if (hasKeyframes) KeyframeDiamond else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Curve Graph icon
                IconButton(onClick = onOpenCurve, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Curve",
                        tint = if (hasKeyframes) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        content()
    }
}

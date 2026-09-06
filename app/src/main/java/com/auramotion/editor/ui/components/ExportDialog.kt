package com.auramotion.editor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.auramotion.editor.ui.theme.*

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onStartExport: (resolution: String, fps: Int, format: String) -> Unit
) {
    var selectedResolution by remember { mutableStateOf("1080p Full HD (1080x1920)") }
    var selectedFps by remember { mutableIntStateOf(60) }
    var selectedFormat by remember { mutableStateOf("MP4 (H.264 High Profile)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Motion Video",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Resolution", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("720p", "1080p", "4K").forEach { res ->
                        FilterChip(
                            selected = selectedResolution.startsWith(res),
                            onClick = { selectedResolution = "$res Full HD" },
                            label = { Text(res, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Frame Rate (FPS)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(24, 30, 60).forEach { fpsVal ->
                        FilterChip(
                            selected = selectedFps == fpsVal,
                            onClick = { selectedFps = fpsVal },
                            label = { Text("${fpsVal} fps", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Format & Codec", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MP4", "GIF", "PNG Sequence").forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat.startsWith(fmt),
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onStartExport(selectedResolution, selectedFps, selectedFormat)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Render & Export", style = MaterialTheme.typography.titleMedium, fontSize = 14.sp)
                }
            }
        }
    }
}

package com.auramotion.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auramotion.editor.ui.theme.*

data class AspectPreset(
    val name: String,
    val ratio: String,
    val width: Int,
    val height: Int
)

val AspectPresets = listOf(
    AspectPreset("9:16 Vertical", "TikTok / Shorts / Reels", 1080, 1920),
    AspectPreset("16:9 Landscape", "YouTube / Standard", 1920, 1080),
    AspectPreset("1:1 Square", "Instagram Feed", 1080, 1080),
    AspectPreset("4:5 Portrait", "Social Portrait", 1080, 1350),
    AspectPreset("21:9 Cinema", "Ultrawide Movie", 2560, 1080)
)

@Composable
fun ProjectBrowserScreen(
    onOpenProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MotionPhotosAuto, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AuraMotion",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = { /* Settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = NeonCyan,
                contentColor = DarkBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Project", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = DarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProject() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cyberpunk Title Motion", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "1080x1920 (9:16) • 60 FPS • 5.0s", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }
        }
    }

    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Motion Project", color = NeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AspectPresets.forEach { preset ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNewProjectDialog = false
                                    onOpenProject()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(preset.name, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                    Text(preset.ratio, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                                Text("${preset.width}x${preset.height}", color = NeonCyan, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

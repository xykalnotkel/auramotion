package com.auramotion.editor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.auramotion.editor.model.EffectType
import com.auramotion.editor.ui.theme.*

data class EffectItem(
    val type: EffectType,
    val title: String,
    val category: String,
    val icon: ImageVector
)

val AvailableEffects = listOf(
    EffectItem(EffectType.GLOW, "Bloom & Glow", "Light & Color", Icons.Default.Flare),
    EffectItem(EffectType.CHROMATIC_ABERRATION, "RGB Split", "Distortion", Icons.Default.FilterVintage),
    EffectItem(EffectType.GAUSSIAN_BLUR, "Gaussian Blur", "Blur", Icons.Default.BlurOn),
    EffectItem(EffectType.COLOR_GRADING, "Color Grading", "Color", Icons.Default.ColorLens),
    EffectItem(EffectType.WAVE_WARP, "Wave Warp", "Distortion", Icons.Default.Waves),
    EffectItem(EffectType.VIGNETTE, "Vignette Dark", "Stylize", Icons.Default.Tonality)
)

@Composable
fun EffectsBrowserDialog(
    onDismiss: () -> Unit,
    onSelectEffect: (EffectType) -> Unit
) {
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
                        text = "Visual Effects Library",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(AvailableEffects) { fx ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectEffect(fx.type) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = fx.icon,
                                    contentDescription = fx.title,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fx.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = fx.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

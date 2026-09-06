package com.auramotion.editor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.auramotion.editor.model.ShapeType
import com.auramotion.editor.ui.theme.*

data class ShapeOption(
    val type: ShapeType,
    val title: String,
    val icon: ImageVector
)

val ShapeOptions = listOf(
    ShapeOption(ShapeType.ROUNDED_RECTANGLE, "Rounded Card", Icons.Default.CropPortrait),
    ShapeOption(ShapeType.RECTANGLE, "Rectangle", Icons.Default.Square),
    ShapeOption(ShapeType.CIRCLE, "Circle / Ring", Icons.Default.Circle),
    ShapeOption(ShapeType.STAR, "Star 5-Point", Icons.Default.Star),
    ShapeOption(ShapeType.POLYGON, "Hexagon", Icons.Default.Hexagon),
    ShapeOption(ShapeType.HEART, "Heart", Icons.Default.Favorite)
)

@Composable
fun ShapePickerDialog(
    onDismiss: () -> Unit,
    onSelectShape: (ShapeType) -> Unit
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
                        text = "Add Vector Shape",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShapeOptions.forEach { opt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectShape(opt.type) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(opt.icon, contentDescription = opt.title, tint = NeonCyan, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(opt.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BlueprintStructures

@Composable
fun BlueprintLegendDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .testTag("dialog_blueprint_legend")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Title Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JAIN IRRIGATION SYSTEMS LTD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Irrigation Blueprint Legend",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Drawings MIS/610/24-25 & MIS/604/24-25 • Scale 1:8500",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_legend")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Site Metadata Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LegendMetaRow("Client", "FE INDORAMA AGRO LLC")
                            LegendMetaRow("Location", "Sardoba, Saikhunabad, Uzbekistan")
                            LegendMetaRow("Total Area", "1,748.66 HA (Rashido: 1099.16 Ha, T. Malik: 649.50 Ha)")
                            LegendMetaRow("Crop / Spacing", "Cotton • GL 0.9m • Dripper 0.3m @ 1.25 Lph")
                            LegendMetaRow("Machine Route", "Tentative planting route plan at every 10.8m")
                        }
                    }

                    // 2. Pipe Outer Diameter Specifications
                    LegendSectionHeader("PIPE OUTER DIAMETER (PVC) HIERARCHY")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BlueprintStructures.PIPE_SPECS.forEach { spec ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height((spec.strokeWidthDp * 1.6f).coerceIn(4f, 10f).dp)
                                            .background(Color(spec.color), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${spec.diameterMm} mm ${spec.material}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Text(
                                    text = if (spec.diameterMm >= 250) "Trunk Mainline" else if (spec.diameterMm >= 110) "Submain Feeder" else "Lateral Manifold",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    // 3. Valves & Flow Controls
                    LegendSectionHeader("VALVES & FIELD CONTROLS")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendSymbolRow(
                            symbol = "⧓",
                            symbolColor = Color(0xFF10B981),
                            title = "Submain Control Valve",
                            subtitle = "Automatic hydraulic valve controlling shift flow (Open = Green, Closed = Red)"
                        )
                        LegendSymbolRow(
                            symbol = "||",
                            symbolColor = Color(0xFF0EA5E9),
                            title = "Submain Flush Valve",
                            subtitle = "Dual hash marker at distal lateral pipe end for scouring and silt discharge"
                        )
                    }

                    // 4. Canal & Water Network
                    LegendSectionHeader("CANAL & WATER DRAINAGE NETWORK")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendLineRow(
                            lineColor = Color(0xFF06B6D4),
                            lineWidth = 5.dp,
                            title = "Canal - LaToc (Branch Canal)",
                            subtitle = "Main primary supply canal providing fresh water intake to pump houses"
                        )
                        LegendLineRow(
                            lineColor = Color(0xFF0284C7),
                            lineWidth = 3.dp,
                            title = "Collector Canal",
                            subtitle = "Secondary perimeter collector channels"
                        )
                        LegendLineRow(
                            lineColor = Color(0xFF0D9488),
                            lineWidth = 2.dp,
                            title = "Drain Collector",
                            subtitle = "Sub-surface and boundary agricultural runoff drainage network"
                        )
                    }

                    // 5. Civil Infrastructure at Pump Stations
                    LegendSectionHeader("PUMP HOUSE CIVIL STRUCTURES")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendBoxRow(
                            fillColor = Color(0xFF8B5CF6),
                            borderColor = Color(0xFF6D28D9),
                            title = "Fertigation Chemical Station (82 m³ Tank)",
                            subtitle = "High-precision fertilizer injection and acid dosing facility"
                        )
                        LegendBoxRow(
                            fillColor = Color(0xFF1E293B),
                            borderColor = Color(0xFF475569),
                            title = "Pump Station Building (15m x 10m / 12m x 14m)",
                            subtitle = "Enclosure for centrifugal pumps, manifold header, and automatic backwash filters"
                        )
                    }

                    // 6. Road Network
                    LegendSectionHeader("TRANSPORT & INTERNAL ROAD NETWORK")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendLineRow(
                            lineColor = Color(0xFFD97706),
                            lineWidth = 4.dp,
                            title = "Internal Road 9.0m Wide",
                            subtitle = "Heavy transport & tractor machinery arterial connecting pump stations"
                        )
                        LegendLineRow(
                            lineColor = Color(0xFF94A3B8),
                            lineWidth = 2.dp,
                            title = "Internal Road 1.8m Wide",
                            subtitle = "Field service access path along submain lateral corridors"
                        )
                    }

                    // 7. Agronomic Layout & Route Plan
                    LegendSectionHeader("PLANTING MACHINE & CROP LAYOUT")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendLineRow(
                            lineColor = Color(0xFF10B981),
                            lineWidth = 1.5f.dp,
                            title = "Planting Machine Route (Every 10.8m)",
                            subtitle = "Parallel machine navigation tracks for planting and harvesting equipment"
                        )
                        LegendLineRow(
                            lineColor = Color(0xFF3B82F6),
                            lineWidth = 1.dp,
                            title = "Cotton Dripline Rows (GL 0.9m)",
                            subtitle = "Underground drip lateral tubing spaced at 90cm cotton plant rows"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Blueprint Legend", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LegendSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun LegendMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
    }
}

@Composable
private fun LegendSymbolRow(symbol: String, symbolColor: Color, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(symbolColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = symbol, color = symbolColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        }
    }
}

@Composable
private fun LegendLineRow(lineColor: Color, lineWidth: androidx.compose.ui.unit.Dp, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(lineWidth)
                .background(lineColor, RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        }
    }
}

@Composable
private fun LegendBoxRow(fillColor: Color, borderColor: Color, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(fillColor.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
        }
    }
}

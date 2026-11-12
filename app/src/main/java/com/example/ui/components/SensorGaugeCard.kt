package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.ui.theme.ElectricBolt
import com.example.ui.theme.Grass
import com.example.ui.theme.Layers
import com.example.ui.theme.Opacity
import com.example.ui.theme.Science
import com.example.ui.theme.Speed
import com.example.ui.theme.Water
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PumpHouseEntity
import com.example.ui.theme.ValveActiveGreen

@Composable
fun SensorGaugeCard(
    pumpHouse: PumpHouseEntity,
    totalFlowLps: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sensor_gauge_card_${pumpHouse.id}")
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pumpHouse.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pumpHouse.siteLocation} • ${pumpHouse.dimensions}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ValveActiveGreen.copy(alpha = 0.12f))
                        .border(1.dp, ValveActiveGreen.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ValveActiveGreen)
                        )
                        Text(
                            text = "LIVE SENSORS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = ValveActiveGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 1: Header Pressure Arc Gauge & Flow Rate Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pressure Arc Gauge
                ArcPressureGauge(
                    currentPressure = pumpHouse.headerPressureBar,
                    minPressure = 0f,
                    maxPressure = 6.0f,
                    targetMin = 2.8f,
                    targetMax = 3.8f,
                    modifier = Modifier.weight(1f)
                )

                // Active Flow Rate Card
                TelemetryMetricCard(
                    title = "TOTAL FLOW",
                    value = String.format("%.1f", totalFlowLps),
                    unit = "lps",
                    secondaryText = "≈ ${String.format("%.0f", totalFlowLps * 3.6)} m³/hr",
                    icon = Icons.Default.Speed,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Station Command Area & Discharge Capacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TelemetryMetricCard(
                    title = "COMMAND AREA",
                    value = "%.1f".format(pumpHouse.totalAreaHa),
                    unit = "Ha",
                    secondaryText = "Commanded Field Area",
                    icon = Icons.Default.Layers,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricCard(
                    title = "TARGET DISCHARGE",
                    value = "%.0f".format(pumpHouse.ratedDischargeM3Hr),
                    unit = "m³/h",
                    secondaryText = "Rated Station Capacity",
                    icon = Icons.Default.WaterDrop,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Fertigation EC/pH & Pump Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TelemetryMetricCard(
                    title = "FERTIGATION (EC/pH)",
                    value = "${pumpHouse.fertigationEcMsCm} / ${pumpHouse.fertigationPh}",
                    unit = "mS/cm",
                    secondaryText = "Dosing Tank: ${pumpHouse.fertigationTankCapacityM3.toInt()}m³",
                    icon = Icons.Default.Science,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricCard(
                    title = "PUMP TELEMETRY",
                    value = "${pumpHouse.pumpRpm} RPM",
                    unit = "${pumpHouse.pumpPowerKw} kW",
                    secondaryText = pumpHouse.pumpState.name,
                    icon = Icons.Default.ElectricBolt,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ArcPressureGauge(
    currentPressure: Float,
    minPressure: Float,
    maxPressure: Float,
    targetMin: Float,
    targetMax: Float,
    modifier: Modifier = Modifier
) {
    val animatedPressure by animateFloatAsState(
        targetValue = currentPressure,
        animationSpec = tween(700),
        label = "pressure"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "HEADER PRESSURE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val greenColor = ValveActiveGreen

                Canvas(modifier = Modifier.size(80.dp)) {
                    val sweep = 240f
                    val startAngle = 150f
                    val stroke = 10.dp.toPx()

                    // Background track
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )

                    // Target range indication (2.8 to 3.8 bar)
                    val targetStart = startAngle + (targetMin / maxPressure) * sweep
                    val targetSweep = ((targetMax - targetMin) / maxPressure) * sweep
                    drawArc(
                        color = greenColor.copy(alpha = 0.3f),
                        startAngle = targetStart,
                        sweepAngle = targetSweep,
                        useCenter = false,
                        style = Stroke(width = stroke + 2, cap = StrokeCap.Butt)
                    )

                    // Value arc
                    val progress = (animatedPressure / maxPressure).coerceIn(0f, 1f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, greenColor)
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweep * progress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.2f", animatedPressure),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "bar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Target: 2.8 - 3.8 bar",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    unit: String,
    secondaryText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = secondaryText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TankLevelMetric(
    tankName: String,
    dimensions: String,
    levelPct: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = levelPct,
        animationSpec = tween(800),
        label = "tankLevel"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vertical liquid tank cylinder
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = (animatedLevel / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(bottomStart = 11.dp, bottomEnd = 11.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(color.copy(alpha = 0.7f), color)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = tankName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dimensions,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${String.format("%.1f", animatedLevel)}%",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = color
                )
            }
        }
    }
}

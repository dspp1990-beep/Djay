package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.ui.theme.CheckCircle
import com.example.ui.theme.ElectricBolt
import com.example.ui.theme.FilterAlt
import com.example.ui.theme.Grass
import com.example.ui.theme.Refresh
import com.example.ui.theme.Science
import com.example.ui.theme.Speed
import com.example.ui.theme.Thermostat
import com.example.ui.theme.Water
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PumpHouseEntity
import com.example.data.model.SubmainValveEntity
import com.example.ui.components.SensorGaugeCard
import com.example.ui.theme.ValveActiveGreen

@Composable
fun RealtimeSensorsScreen(
    pumpHouses: List<PumpHouseEntity>,
    selectedPumpHouseId: String?,
    allValves: List<SubmainValveEntity>,
    onSelectPumpHouse: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePumpHouse = pumpHouses.find { it.id == selectedPumpHouseId } ?: pumpHouses.firstOrNull()
    val activeValvesInPh = allValves.filter { it.pumpHouseId == activePumpHouse?.id && it.isControlValveOpen }
    val totalFlow = activeValvesInPh.sumOf { it.actualFlowLps }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("realtime_sensors_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Pump House Selector Bar
            Column {
                Text(
                    text = "Select Pump House Telemetry Station",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pumpHouses.forEach { ph ->
                        SelectableChip(
                            label = ph.name,
                            subLabel = "${ph.headerPressureBar} bar",
                            isSelected = activePumpHouse?.id == ph.id,
                            onClick = { onSelectPumpHouse(ph.id) },
                            testTag = "sensor_ph_${ph.id}"
                        )
                    }
                }
            }
        }

        if (activePumpHouse != null) {
            // Main Sensor Gauge Card
            item {
                SensorGaugeCard(
                    pumpHouse = activePumpHouse,
                    totalFlowLps = totalFlow
                )
            }

            // Filtration Bank DP & Backwash Card
            item {
                FiltrationTelemetryCard(pumpHouse = activePumpHouse)
            }

            // Soil Moisture Station (20cm vs 40cm)
            item {
                SoilMoistureStationCard(
                    valves = allValves.filter { it.pumpHouseId == activePumpHouse.id }
                )
            }

            // Water Intake & Reservoir Capacity Card
            item {
                WaterIntakeTelemetryCard(pumpHouse = activePumpHouse)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FiltrationTelemetryCard(
    pumpHouse: PumpHouseEntity,
    modifier: Modifier = Modifier
) {
    val dp = pumpHouse.filterInletPressureBar - pumpHouse.filterOutletPressureBar
    val isDpNormal = dp < 0.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filtration",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Automatic Sand & Disc Filtration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isDpNormal) ValveActiveGreen.copy(alpha = 0.12f) else Color(0x22FFA000))
                        .border(
                            1.dp,
                            if (isDpNormal) ValveActiveGreen.copy(alpha = 0.35f) else Color(0x55FFA000),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDpNormal) "CLEAN (ΔP Normal)" else "BACKWASH REQ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isDpNormal) ValveActiveGreen else Color(0xFFFFA000)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterPressureBox(
                    title = "INLET PRESSURE",
                    pressure = pumpHouse.filterInletPressureBar,
                    modifier = Modifier.weight(1f)
                )
                FilterPressureBox(
                    title = "OUTLET PRESSURE",
                    pressure = pumpHouse.filterOutletPressureBar,
                    modifier = Modifier.weight(1f)
                )
                FilterPressureBox(
                    title = "DIFFERENTIAL ΔP",
                    pressure = Math.round(dp * 100f) / 100f,
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FilterPressureBox(
    title: String,
    pressure: Float,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isHighlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
            .border(
                1.dp,
                if (isHighlight) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$pressure bar",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SoilMoistureStationCard(
    valves: List<SubmainValveEntity>,
    modifier: Modifier = Modifier
) {
    val avg20cm = if (valves.isNotEmpty()) valves.map { it.soilMoisture20cmPct }.average().toFloat() else 25.4f
    val avg40cm = if (valves.isNotEmpty()) valves.map { it.soilMoisture40cmPct }.average().toFloat() else 30.1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = "Soil",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Field Soil Moisture Profile (FDR Sensors)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Cotton Field",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 20cm Root Zone Depth
            Text(
                text = "Root Zone (20 cm Depth) • Active Feeder Layer",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (avg20cm / 50f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = ValveActiveGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format("%.1f", avg20cm)}% VWC",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = ValveActiveGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 40cm Subsoil Depth
            Text(
                text = "Deep Subsoil (40 cm Depth) • Moisture Reserve",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (avg40cm / 50f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format("%.1f", avg40cm)}% VWC",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Optimal cotton moisture range: 22% - 32% volumetric water content (VWC)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WaterIntakeTelemetryCard(
    pumpHouse: PumpHouseEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = "Canal Intake",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Canal Intake & Storage Dimensions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Intake Source:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = pumpHouse.intakeCanalStatus,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Commanded Field Area:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "%.1f Ha".format(pumpHouse.totalAreaHa),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Station Design Capacity:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "%.0f m³/h (%.0f lps)".format(pumpHouse.ratedDischargeM3Hr, pumpHouse.ratedDischargeM3Hr / 3.6),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Fertigation Tank:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${pumpHouse.fertigationTankCapacityM3.toInt()} m³ capacity",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

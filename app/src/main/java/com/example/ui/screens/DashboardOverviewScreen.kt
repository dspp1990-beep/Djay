package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.ui.theme.Bolt
import com.example.ui.theme.CheckCircle
import com.example.ui.theme.Info
import com.example.ui.theme.Pause
import com.example.ui.theme.PlayArrow
import com.example.ui.theme.PowerSettingsNew
import com.example.ui.theme.Schedule
import com.example.ui.theme.Speed
import com.example.ui.theme.Water
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.DayWiseProgress
import com.example.data.model.PumpHouseEntity
import com.example.data.model.SubmainValveEntity
import com.example.ui.components.DayWiseAreaTrackerCard
import com.example.ui.components.PumpHouseSummaryCard
import com.example.ui.components.ValveCard
import com.example.ui.theme.ValveActiveGreen
import com.example.ui.theme.ValveAlertRed
import com.example.ui.viewmodel.IrrigationUiStats

@Composable
fun DashboardOverviewScreen(
    stats: IrrigationUiStats,
    activeValves: List<SubmainValveEntity>,
    pumpHouses: List<PumpHouseEntity>,
    selectedPumpHouseId: String?,
    isFastDemoMode: Boolean,
    dayWiseRecords: List<DayWiseProgress> = emptyList(),
    onLocateValveOnMap: (SubmainValveEntity) -> Unit = {},
    onSelectPumpHouse: (String?) -> Unit,
    onToggleControlValve: (String, Boolean) -> Unit,
    onToggleFlushValve: (String, Boolean) -> Unit,
    onEmergencyShutoff: () -> Unit,
    onToggleFastDemoMode: () -> Unit,
    onNavigateToValves: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Text(
                    text = "Emergency Shutoff All Valves?",
                    fontWeight = FontWeight.Bold,
                    color = ValveAlertRed
                )
            },
            text = {
                Text("This will immediately send close signals to all submain control valves and flush valves across all pump houses and systems.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyDialog = false
                        onEmergencyShutoff()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ValveAlertRed),
                    modifier = Modifier.testTag("confirm_emergency_shutoff")
                ) {
                    Text("Confirm Emergency Stop", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_overview_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Status Banner
            DashboardHeroBanner(
                stats = stats,
                isFastDemoMode = isFastDemoMode,
                onEmergencyClick = { showEmergencyDialog = true },
                onToggleFastDemo = onToggleFastDemoMode
            )
        }

        // Live Open Valves & Day-Wise Area Completed Tracker
        if (dayWiseRecords.isNotEmpty()) {
            item {
                DayWiseAreaTrackerCard(
                    dayWiseRecords = dayWiseRecords,
                    activeValves = activeValves,
                    onToggleValve = onToggleControlValve,
                    onLocateValveOnMap = onLocateValveOnMap
                )
            }
        }

        // Live GIS Map & KML Tracking Shortcut Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToMap)
                    .testTag("card_dashboard_gis_shortcut")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Water,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "LIVE GIS & KML TRACKING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Sardoba Scheme (Zones 2, 3 & 4)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "12 Pump Stations • 20 Systems • 1,619.3 Ha • Live GPS & Valve Pins",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToMap,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_dashboard_launch_gis")
                    ) {
                        Text("Open Map", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Irrigation Spotlight
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Irrigation Live Feed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${activeValves.size} valves currently open & delivering water",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onNavigateToValves,
                    modifier = Modifier.testTag("btn_view_all_valves")
                ) {
                    Text("View All Valves")
                }
            }
        }

        if (activeValves.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No valves actively irrigating right now",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Tap on any pump house below or open Valves tab to activate irrigation shifts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Show up to first 4 active valves in spotlight
            items(activeValves.take(4), key = { "active_${it.tag}" }) { valve ->
                ValveCard(
                    valve = valve,
                    onToggleControlValve = { open -> onToggleControlValve(valve.tag, open) },
                    onToggleFlushValve = { open -> onToggleFlushValve(valve.tag, open) }
                )
            }
        }

        // Pump House Wise Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pump Houses (Sardoba Site)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Hierarchical control: Pump House → System → Shift → Valves",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onNavigateToSensors,
                    modifier = Modifier.testTag("btn_view_telemetry")
                ) {
                    Text("Sensors Telemetry")
                }
            }
        }

        items(pumpHouses, key = { it.id }) { ph ->
            val activeForPh = activeValves.filter { it.pumpHouseId == ph.id }
            PumpHouseSummaryCard(
                pumpHouse = ph,
                activeValvesCount = activeForPh.size,
                activeFlowLps = activeForPh.sumOf { it.actualFlowLps },
                isSelected = ph.id == selectedPumpHouseId,
                onSelect = {
                    onSelectPumpHouse(ph.id)
                    onNavigateToValves()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardHeroBanner(
    stats: IrrigationUiStats,
    isFastDemoMode: Boolean,
    onEmergencyClick: () -> Unit,
    onToggleFastDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Title + Status Badge & Emergency Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "CENTRAL IRRIGATION STATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Main Pump System",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Sardoba Cotton Project • Automated Drip Line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge (Directly from Professional Polish HTML: rounded-full, bg-[#006D3B], white dot + ACTIVE)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ValveActiveGreen)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = onEmergencyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ValveAlertRed),
                        shape = RoundedCornerShape(50),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_emergency_shutoff")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Emergency Shutoff",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "STOP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Stats Metrics in 2x2 Grid (Design: bg-white/50 rounded-2xl p-3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricBox(
                    label = "ACTIVE VALVES",
                    value = "${stats.activeValvesCount}",
                    unit = "/ ${stats.totalValvesCount}",
                    subValue = "Active Delivery",
                    icon = Icons.Default.WaterDrop,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatMetricBox(
                    label = "OUTPUT FLOW",
                    value = String.format("%.1f", stats.activeFlowLps),
                    unit = "L/s",
                    subValue = "≈ ${String.format("%.0f", stats.activeFlowM3Hr)} m³/h",
                    icon = Icons.Default.Speed,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricBox(
                    label = "ACTIVE AREA",
                    value = String.format("%.1f", stats.activeAreaHa),
                    unit = "Ha",
                    subValue = "${stats.activeValvesCount} Valves Irrigating",
                    icon = Icons.Default.WaterDrop,
                    accentColor = if (stats.activeAreaHa > 0) ValveActiveGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatMetricBox(
                    label = "DAY COMPLETED",
                    value = String.format("%.1f", stats.todayCompletedAreaHa),
                    unit = "Ha",
                    subValue = "${stats.todayCompletionPct}% of ${stats.dailyTargetAreaHa.toInt()} Ha",
                    icon = Icons.Default.CheckCircle,
                    accentColor = if (stats.todayCompletionPct >= 90) ValveActiveGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Demo mode toggle banner with pill styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggleFastDemo)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFastDemoMode) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Fast Demo",
                            tint = if (isFastDemoMode) ValveActiveGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isFastDemoMode) "Auto-Shift Demonstration Active (15s rotation)" else "Standard Irrigation Schedule (2.0 hrs / shift)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFastDemoMode) "Tap to pause rapid cycle rotation" else "Tap to preview sequential shift cycle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isFastDemoMode) ValveActiveGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFastDemoMode) "RUNNING" else "DEMO MODE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isFastDemoMode) ValveActiveGreen else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatMetricBox(
    label: String,
    value: String,
    unit: String = "",
    subValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

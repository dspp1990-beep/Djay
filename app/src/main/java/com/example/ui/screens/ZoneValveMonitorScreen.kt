package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import com.example.ui.theme.FilterList
import com.example.ui.theme.PlayArrow
import androidx.compose.material.icons.filled.Search
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.PumpHouseEntity
import com.example.data.model.SubmainValveEntity
import com.example.ui.components.ShiftTimelineBar
import com.example.ui.components.ValveCard
import com.example.ui.theme.ValveActiveGreen
import com.example.ui.theme.ValveAlertRed
import com.example.ui.theme.ValveClosedGray
import com.example.ui.theme.ValveFlushingBlue
import com.example.ui.viewmodel.ValveFilterType

@Composable
fun ZoneValveMonitorScreen(
    pumpHouses: List<PumpHouseEntity>,
    systems: List<IrrigationSystemEntity>,
    valves: List<SubmainValveEntity>,
    selectedZone: String?,
    selectedPumpHouseId: String?,
    selectedSystemId: String?,
    selectedShift: Int?,
    valveFilter: ValveFilterType,
    searchQuery: String,
    onSelectZone: (String?) -> Unit,
    onSelectPumpHouse: (String?) -> Unit,
    onSelectSystem: (String?) -> Unit,
    onSelectShift: (Int?) -> Unit,
    onSetValveFilter: (ValveFilterType) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onToggleControlValve: (String, Boolean) -> Unit,
    onToggleFlushValve: (String, Boolean) -> Unit,
    onToggleShiftBatch: (String, Int, Boolean) -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSystem = systems.find { it.id == selectedSystemId } ?: systems.firstOrNull()
    val activeShift = activeSystem?.activeShift ?: 1

    val activeValvesCount = valves.count { it.isControlValveOpen }
    val totalFlow = valves.filter { it.isControlValveOpen }.sumOf { it.actualFlowLps }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("zone_valve_monitor_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSetSearchQuery,
                    placeholder = { Text("Search valves...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.clickable { onSetSearchQuery("") }
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("valve_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Map quick button
                Button(
                    onClick = onNavigateToMap,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.testTag("btn_goto_gis_map")
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GIS Map", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hierarchy Level 0: Zone Wise Selector Chips
        item {
            Column {
                Text(
                    text = "1. Zone Selection (KML Sardoba Scheme)",
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
                    listOf("All", "Zone 2", "Zone 3", "Zone 4").forEach { zone ->
                        val isSelected = (selectedZone == zone) || (zone == "All" && (selectedZone == null || selectedZone == "All"))
                        SelectableChip(
                            label = when (zone) {
                                "Zone 2" -> "Zone 2 (537 Ha)"
                                "Zone 3" -> "Zone 3 (489 Ha)"
                                "Zone 4" -> "Zone 4 (593 Ha)"
                                else -> "All Zones (1,619 Ha)"
                            },
                            isSelected = isSelected,
                            onClick = { onSelectZone(if (zone == "All") null else zone) },
                            testTag = "zone_chip_$zone"
                        )
                    }
                }
            }
        }

        // Hierarchy Level 1: Pump House Selector Chips
        item {
            val phsForZone = if (selectedZone == null || selectedZone == "All") pumpHouses
            else pumpHouses.filter { it.zoneName == selectedZone }

            Column {
                Text(
                    text = "2. Pump House Wise Selection",
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
                    // All Pump Houses chip
                    SelectableChip(
                        label = "All PHs",
                        isSelected = selectedPumpHouseId == null,
                        onClick = { onSelectPumpHouse(null) },
                        testTag = "ph_chip_all"
                    )

                    phsForZone.forEach { ph ->
                        SelectableChip(
                            label = ph.id,
                            subLabel = ph.zoneName,
                            isSelected = selectedPumpHouseId == ph.id,
                            onClick = { onSelectPumpHouse(ph.id) },
                            testTag = "ph_chip_${ph.id}"
                        )
                    }
                }
            }
        }

        // Hierarchy Level 2: System Wise Selector Chips
        item {
            val systemsForSelectedPh = if (selectedPumpHouseId == null) systems
            else systems.filter { it.pumpHouseId == selectedPumpHouseId }

            Column {
                Text(
                    text = "2. System Wise Selection (${activeSystem?.name ?: "All Systems"})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        label = "All Systems",
                        isSelected = selectedSystemId == null,
                        onClick = { onSelectSystem(null) },
                        testTag = "sys_chip_all"
                    )

                    systemsForSelectedPh.forEach { sys ->
                        SelectableChip(
                            label = "Sys ${sys.systemNumber}",
                            subLabel = "${sys.irrigatedAreaHa} ha",
                            isSelected = selectedSystemId == sys.id,
                            onClick = { onSelectSystem(sys.id) },
                            testTag = "sys_chip_${sys.id}"
                        )
                    }
                }
            }
        }

        // Hierarchy Level 3: Zone / Shift Timeline (S1 to S12)
        item {
            ShiftTimelineBar(
                activeShift = activeShift,
                selectedShift = selectedShift,
                onShiftSelected = onSelectShift,
                elapsedMinutes = activeSystem?.shiftElapsedMinutes ?: 45,
                shiftDurationMinutes = activeSystem?.shiftDurationMinutes ?: 120
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterPill(
                    label = "All (${valves.size})",
                    isSelected = valveFilter == ValveFilterType.ALL,
                    onClick = { onSetValveFilter(ValveFilterType.ALL) }
                )
                FilterPill(
                    label = "Open / Irrigating ($activeValvesCount)",
                    color = ValveActiveGreen,
                    isSelected = valveFilter == ValveFilterType.OPEN_ACTIVE,
                    onClick = { onSetValveFilter(ValveFilterType.OPEN_ACTIVE) }
                )
                FilterPill(
                    label = "Closed",
                    color = ValveClosedGray,
                    isSelected = valveFilter == ValveFilterType.CLOSED,
                    onClick = { onSetValveFilter(ValveFilterType.CLOSED) }
                )
                FilterPill(
                    label = "Flushing",
                    color = ValveFlushingBlue,
                    isSelected = valveFilter == ValveFilterType.FLUSHING,
                    onClick = { onSetValveFilter(ValveFilterType.FLUSHING) }
                )
                FilterPill(
                    label = "Alerts",
                    color = ValveAlertRed,
                    isSelected = valveFilter == ValveFilterType.ALERTS,
                    onClick = { onSetValveFilter(ValveFilterType.ALERTS) }
                )
            }
        }

        // Batch Action Header for Current Shift
        if (selectedShift != null && activeSystem != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shift S$selectedShift Controls",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Batch toggle all valves for this shift section",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onToggleShiftBatch(activeSystem.id, selectedShift, false) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_close_shift_$selectedShift")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Close All", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { onToggleShiftBatch(activeSystem.id, selectedShift, true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ValveActiveGreen),
                                modifier = Modifier.testTag("btn_open_shift_$selectedShift")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open All", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Valve Summary Counter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Displaying ${valves.size} Submain Valves",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Live Flow: ${String.format("%.2f", totalFlow)} lps",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (totalFlow > 0) ValveActiveGreen else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Valves List
        if (valves.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No valves match the selected filter or search criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(valves, key = { it.tag }) { valve ->
                ValveCard(
                    valve = valve,
                    onToggleControlValve = { open -> onToggleControlValve(valve.tag, open) },
                    onToggleFlushValve = { open -> onToggleFlushValve(valve.tag, open) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SelectableChip(
    label: String,
    subLabel: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.3.sp
            ),
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

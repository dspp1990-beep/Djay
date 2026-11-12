package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.ui.theme.AccountTree
import com.example.ui.theme.GridView
import com.example.ui.theme.Map
import com.example.ui.theme.PowerSettingsNew
import com.example.ui.theme.Schedule
import com.example.ui.theme.Speed
import com.example.ui.theme.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AutomatedSchedulerScreen
import com.example.ui.screens.DashboardOverviewScreen
import com.example.ui.screens.GisMapScreen
import com.example.ui.screens.PumpHousesScreen
import com.example.ui.screens.RealtimeSensorsScreen
import com.example.ui.screens.ZoneValveMonitorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ValveActiveGreen
import com.example.ui.theme.ValveAlertRed
import com.example.ui.viewmodel.IrrigationViewModel
import com.example.ui.theme.*

enum class NavigationScreen(val label: String, val icon: ImageVector, val tag: String) {
    OVERVIEW("Overview", Icons.Default.GridView, "nav_overview"),
    MAP("KML", Icons.Default.Public, "nav_kml_tab"),
    VALVES("Valves", Icons.Default.WaterDrop, "nav_valves"),
    PUMP_HOUSES("Stations", Icons.Default.AccountTree, "nav_pump_houses"),
    SENSORS("Sensors", Icons.Default.Speed, "nav_sensors"),
    SCHEDULER("Schedule", Icons.Default.Schedule, "nav_scheduler")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: IrrigationViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(NavigationScreen.OVERVIEW) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val pumpHouses by viewModel.pumpHouses.collectAsState()
    val systems by viewModel.systems.collectAsState()
    val allValves by viewModel.allValves.collectAsState()
    val activeValves by viewModel.activeValves.collectAsState()
    val filteredValves by viewModel.filteredValves.collectAsState()
    val stats by viewModel.uiStats.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val scheduleConfigs by viewModel.scheduleConfigs.collectAsState()
    val isFastDemoMode by viewModel.isFastDemoMode.collectAsState()
    val dayWiseRecords by viewModel.dayWiseProgressList.collectAsState()

    val selectedZone by viewModel.selectedZone.collectAsState()
    val selectedPumpHouseId by viewModel.selectedPumpHouseId.collectAsState()
    val selectedSystemId by viewModel.selectedSystemId.collectAsState()
    val selectedShiftFilter by viewModel.selectedShiftFilter.collectAsState()
    val valveFilter by viewModel.valveFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                Text("Immediately sends a shutoff signal to all submain control valves and flush valves across all pump houses.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyDialog = false
                        viewModel.emergencyShutoff()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ValveAlertRed),
                    modifier = Modifier.testTag("confirm_topbar_shutoff")
                ) {
                    Text("Emergency Shutoff", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ValveActiveGreen)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SCADA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.testTag("rail_navigation")
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    NavigationScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .testTag("btn_rail_emergency")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Emergency Shutoff",
                            tint = ValveAlertRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .testTag("main_scaffold_wide"),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(ValveActiveGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CENTRAL STATION - SARDOBA AUTOMATED IRRIGATION SCADA",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 1.2.sp,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = currentScreen.label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentScreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        label = "screen_crossfade_wide"
                    ) { screen ->
                        when (screen) {
                            NavigationScreen.OVERVIEW -> {
                                DashboardOverviewScreen(
                                    stats = stats,
                                    activeValves = activeValves,
                                    pumpHouses = pumpHouses,
                                    selectedPumpHouseId = selectedPumpHouseId,
                                    isFastDemoMode = isFastDemoMode,
                                    dayWiseRecords = dayWiseRecords,
                                    onLocateValveOnMap = { valve ->
                                        viewModel.selectMapValve(valve)
                                        viewModel.jumpToTarget(com.example.data.model.GeoPoint(valve.latitude, valve.longitude))
                                        currentScreen = NavigationScreen.MAP
                                    },
                                    onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) },
                                    onToggleControlValve = { tag, open -> viewModel.toggleControlValve(tag, open) },
                                    onToggleFlushValve = { tag, open -> viewModel.toggleFlushValve(tag, open) },
                                    onEmergencyShutoff = { viewModel.emergencyShutoff() },
                                    onToggleFastDemoMode = { viewModel.toggleFastDemoMode() },
                                    onNavigateToValves = { currentScreen = NavigationScreen.VALVES },
                                    onNavigateToSensors = { currentScreen = NavigationScreen.SENSORS },
                                    onNavigateToMap = { currentScreen = NavigationScreen.MAP }
                                )
                            }
                            NavigationScreen.MAP -> {
                                GisMapScreen(viewModel = viewModel)
                            }
                            NavigationScreen.VALVES -> {
                                ZoneValveMonitorScreen(
                                    pumpHouses = pumpHouses,
                                    systems = systems,
                                    valves = filteredValves,
                                    selectedZone = selectedZone,
                                    selectedPumpHouseId = selectedPumpHouseId,
                                    selectedSystemId = selectedSystemId,
                                    selectedShift = selectedShiftFilter,
                                    valveFilter = valveFilter,
                                    searchQuery = searchQuery,
                                    onSelectZone = { zone -> viewModel.selectZone(zone) },
                                    onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) },
                                    onSelectSystem = { sysId -> viewModel.selectSystem(sysId) },
                                    onSelectShift = { shift -> viewModel.selectShift(shift) },
                                    onSetValveFilter = { filter -> viewModel.setValveFilter(filter) },
                                    onSetSearchQuery = { q -> viewModel.setSearchQuery(q) },
                                    onToggleControlValve = { tag, open -> viewModel.toggleControlValve(tag, open) },
                                    onToggleFlushValve = { tag, open -> viewModel.toggleFlushValve(tag, open) },
                                    onToggleShiftBatch = { sysId, shift, open -> viewModel.setShiftValves(sysId, shift, open) },
                                    onNavigateToMap = { currentScreen = NavigationScreen.MAP }
                                )
                            }
                            NavigationScreen.PUMP_HOUSES -> {
                                PumpHousesScreen(
                                    pumpHouses = pumpHouses,
                                    systems = systems,
                                    valves = allValves,
                                    selectedPumpHouseId = selectedPumpHouseId,
                                    onSelectPumpHouseAndSystem = { phId, sysId ->
                                        viewModel.selectPumpHouse(phId)
                                        viewModel.selectSystem(sysId)
                                        currentScreen = NavigationScreen.VALVES
                                    }
                                )
                            }
                            NavigationScreen.SENSORS -> {
                                RealtimeSensorsScreen(
                                    pumpHouses = pumpHouses,
                                    selectedPumpHouseId = selectedPumpHouseId,
                                    allValves = allValves,
                                    onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) }
                                )
                            }
                            NavigationScreen.SCHEDULER -> {
                                AutomatedSchedulerScreen(
                                    systems = systems,
                                    selectedSystemId = selectedSystemId,
                                    scheduleConfigs = scheduleConfigs,
                                    recentLogs = recentLogs,
                                    isFastDemoMode = isFastDemoMode,
                                    onSelectSystem = { sysId -> viewModel.selectSystem(sysId) },
                                    onSwitchShift = { sysId, shift -> viewModel.switchShift(sysId, shift) },
                                    onToggleAutoSequence = { sysId, isRunning -> viewModel.toggleAutoSequence(sysId, isRunning) },
                                    onToggleFastDemoMode = { viewModel.toggleFastDemoMode() },
                                    onUpdateScheduleConfig = { config -> viewModel.updateScheduleConfig(config) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("main_scaffold"),
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(ValveActiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CENTRAL STATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.2.sp,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Smart Irrigation SCADA",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { showEmergencyDialog = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("btn_topbar_emergency")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Emergency Shutoff",
                                        tint = ValveAlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .testTag("bottom_navigation")
                    ) {
                        NavigationScreen.values().forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                alwaysShowLabel = false,
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 10.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(screen.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        NavigationScreen.OVERVIEW -> {
                            DashboardOverviewScreen(
                                stats = stats,
                                activeValves = activeValves,
                                pumpHouses = pumpHouses,
                                selectedPumpHouseId = selectedPumpHouseId,
                                isFastDemoMode = isFastDemoMode,
                                dayWiseRecords = dayWiseRecords,
                                onLocateValveOnMap = { valve ->
                                    viewModel.selectMapValve(valve)
                                    viewModel.jumpToTarget(com.example.data.model.GeoPoint(valve.latitude, valve.longitude))
                                    currentScreen = NavigationScreen.MAP
                                },
                                onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) },
                                onToggleControlValve = { tag, open -> viewModel.toggleControlValve(tag, open) },
                                onToggleFlushValve = { tag, open -> viewModel.toggleFlushValve(tag, open) },
                                onEmergencyShutoff = { viewModel.emergencyShutoff() },
                                onToggleFastDemoMode = { viewModel.toggleFastDemoMode() },
                                onNavigateToValves = { currentScreen = NavigationScreen.VALVES },
                                onNavigateToSensors = { currentScreen = NavigationScreen.SENSORS },
                                onNavigateToMap = { currentScreen = NavigationScreen.MAP }
                            )
                        }

                        NavigationScreen.MAP -> {
                            GisMapScreen(viewModel = viewModel)
                        }

                        NavigationScreen.VALVES -> {
                            ZoneValveMonitorScreen(
                                pumpHouses = pumpHouses,
                                systems = systems,
                                valves = filteredValves,
                                selectedZone = selectedZone,
                                selectedPumpHouseId = selectedPumpHouseId,
                                selectedSystemId = selectedSystemId,
                                selectedShift = selectedShiftFilter,
                                valveFilter = valveFilter,
                                searchQuery = searchQuery,
                                onSelectZone = { zone -> viewModel.selectZone(zone) },
                                onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) },
                                onSelectSystem = { sysId -> viewModel.selectSystem(sysId) },
                                onSelectShift = { shift -> viewModel.selectShift(shift) },
                                onSetValveFilter = { filter -> viewModel.setValveFilter(filter) },
                                onSetSearchQuery = { q -> viewModel.setSearchQuery(q) },
                                onToggleControlValve = { tag, open -> viewModel.toggleControlValve(tag, open) },
                                onToggleFlushValve = { tag, open -> viewModel.toggleFlushValve(tag, open) },
                                onToggleShiftBatch = { sysId, shift, open -> viewModel.setShiftValves(sysId, shift, open) },
                                onNavigateToMap = { currentScreen = NavigationScreen.MAP }
                            )
                        }

                        NavigationScreen.PUMP_HOUSES -> {
                            PumpHousesScreen(
                                pumpHouses = pumpHouses,
                                systems = systems,
                                valves = allValves,
                                selectedPumpHouseId = selectedPumpHouseId,
                                onSelectPumpHouseAndSystem = { phId, sysId ->
                                    viewModel.selectPumpHouse(phId)
                                    viewModel.selectSystem(sysId)
                                    currentScreen = NavigationScreen.VALVES
                                }
                            )
                        }

                        NavigationScreen.SENSORS -> {
                            RealtimeSensorsScreen(
                                pumpHouses = pumpHouses,
                                selectedPumpHouseId = selectedPumpHouseId,
                                allValves = allValves,
                                onSelectPumpHouse = { phId -> viewModel.selectPumpHouse(phId) }
                            )
                        }

                        NavigationScreen.SCHEDULER -> {
                            AutomatedSchedulerScreen(
                                systems = systems,
                                selectedSystemId = selectedSystemId,
                                scheduleConfigs = scheduleConfigs,
                                recentLogs = recentLogs,
                                isFastDemoMode = isFastDemoMode,
                                onSelectSystem = { sysId -> viewModel.selectSystem(sysId) },
                                onSwitchShift = { sysId, shift -> viewModel.switchShift(sysId, shift) },
                                onToggleAutoSequence = { sysId, isRunning -> viewModel.toggleAutoSequence(sysId, isRunning) },
                                onToggleFastDemoMode = { viewModel.toggleFastDemoMode() },
                                onUpdateScheduleConfig = { config -> viewModel.updateScheduleConfig(config) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

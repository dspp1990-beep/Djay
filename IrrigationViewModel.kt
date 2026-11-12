package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DayWiseProgress
import com.example.data.model.GeoPoint
import com.example.data.model.IrrigationLogEntity
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.MapBaseOverlayMode
import com.example.data.model.PumpHouseEntity
import com.example.data.model.ScheduleConfigEntity
import com.example.data.model.SubmainValveEntity
import com.example.data.model.ValveState
import com.example.data.repository.IrrigationRepository
import com.example.util.LiveUserLocation
import com.example.util.LocationTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ValveFilterType {
    ALL,
    OPEN_ACTIVE,
    CLOSED,
    FLUSHING,
    ALERTS
}

data class IrrigationUiStats(
    val activeValvesCount: Int = 0,
    val totalValvesCount: Int = 0,
    val activeFlowLps: Double = 0.0,
    val activeFlowM3Hr: Double = 0.0,
    val totalPumpHousesCount: Int = 0,
    val runningPumpsCount: Int = 0,
    val totalAreaHa: Double = 0.0,
    val activeAreaHa: Double = 0.0,
    val todayCompletedAreaHa: Double = 0.0,
    val dailyTargetAreaHa: Double = 240.0,
    val todayCompletionPct: Int = 0
)

class IrrigationViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = IrrigationRepository(database.irrigationDao())
    val locationTracker = LocationTracker(application)

    val pumpHouses: StateFlow<List<PumpHouseEntity>> = repository.allPumpHouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systems: StateFlow<List<IrrigationSystemEntity>> = repository.allSystems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allValves: StateFlow<List<SubmainValveEntity>> = repository.allValves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeValves: StateFlow<List<SubmainValveEntity>> = repository.activeValves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<IrrigationLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleConfigs: StateFlow<List<ScheduleConfigEntity>> = repository.allScheduleConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- REAL field valve log (not touched by the demo telemetry loop) ----------
    // Synced live between all phones through the Cloudflare sync server (see util/SyncManager).
    val syncManager = com.example.util.SyncManager(application, database.irrigationDao(), viewModelScope)
    val syncStatus: StateFlow<com.example.util.SyncManager.Status> = syncManager.status

    val fieldLogs: StateFlow<List<com.example.data.model.FieldValveLogEntity>> = repository.fieldLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Records saved on this phone that have not reached the server yet. */
    val unsyncedCount: StateFlow<Int> = database.irrigationDao().countUnsyncedFieldLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logFieldValve(
        tag: String,
        open: Boolean,
        operatorName: String,
        note: String,
        userLat: Double?,
        userLng: Double?
    ) {
        val valvePos = com.example.data.model.KmlGeoData.SURVEYED_VALVE_COORDINATES[tag]
        val dist = if (valvePos != null && userLat != null && userLng != null)
            com.example.data.model.GeoUtils.distanceMeters(userLat, userLng, valvePos.latitude, valvePos.longitude) else null
        viewModelScope.launch {
            repository.insertFieldLog(
                com.example.data.model.FieldValveLogEntity(
                    uuid = java.util.UUID.randomUUID().toString(),
                    valveTag = tag,
                    systemNumber = com.example.data.model.KmlGeoData.systemOfTag(tag),
                    pumpHouseId = com.example.data.model.KmlGeoData.pumpHouseOfTag(tag),
                    newBlock = com.example.data.model.KmlGeoData.VALVE_NEW_BLOCK[tag] ?: "",
                    action = if (open) "OPEN" else "CLOSE",
                    operatorName = operatorName.ifBlank { "Unknown" },
                    note = note,
                    areaHa = com.example.data.model.KmlGeoData.valveAreaHa(tag),
                    userLatitude = userLat,
                    userLongitude = userLng,
                    distanceToValveM = dist,
                    deviceId = syncManager.deviceId,
                    synced = false
                )
            )
            syncManager.requestSync()
        }
    }

    fun deleteFieldLog(uuid: String) {
        viewModelScope.launch {
            repository.deleteFieldLog(uuid)
            syncManager.requestSync()
        }
    }

    fun setValveShift(tag: String, shift: Int) {
        val s = shift.coerceIn(1, 12)
        viewModelScope.launch {
            repository.setValveShift(tag, s)
            syncManager.queueShift(tag, s)
        }
    }

    fun configureSync(url: String, key: String) = syncManager.configure(url, key)

    override fun onCleared() {
        syncManager.stop()
        super.onCleared()
    }


    init {
        syncManager.start()
        // Ensure database is populated on IO thread before starting background telemetry
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.populateDatabase(database.irrigationDao())
            startTelemetryLoop()
        }

        // Keep proximity tracking instantly updated whenever valves or locations change
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(allValves, pumpHouses, locationTracker.userLocation) { valves, phs, _ ->
                if (valves.isNotEmpty() && phs.isNotEmpty()) {
                    locationTracker.recalculateProximities(valves, phs)
                }
            }.collect { }
        }
    }

    val userLocation: StateFlow<LiveUserLocation> = locationTracker.userLocation

    // Selection & Filter States
    val selectedZone = MutableStateFlow<String?>("All") // "All", "Zone 2", "Zone 3", "Zone 4"
    val selectedPumpHouseId = MutableStateFlow<String?>("PH-15")
    val selectedSystemId = MutableStateFlow<String?>("SYS-32")
    val selectedShiftFilter = MutableStateFlow<Int?>(1) // null = all shifts
    val valveFilter = MutableStateFlow(ValveFilterType.ALL)
    val searchQuery = MutableStateFlow("")

    // Map Specific Controls
    val selectedMapValve = MutableStateFlow<SubmainValveEntity?>(null)
    val selectedMapPumpHouse = MutableStateFlow<PumpHouseEntity?>(null)
    val showPipelinesLayer = MutableStateFlow(true)
    val showValvesLayer = MutableStateFlow(true)
    val showBoundariesLayer = MutableStateFlow(true)
    val showPumpHousesLayer = MutableStateFlow(true)

    // Blueprint Layers (Canal - LaToc, Roads, Civil Structures, Surveyed Plots, 10.8m Planting Routes)
    val showCanalsLayer = MutableStateFlow(true)
    val showRoadsLayer = MutableStateFlow(true)
    val showCivilStructuresLayer = MutableStateFlow(true)
    val showSurveyedPlotsLayer = MutableStateFlow(true)
    val showPlantingRouteLayer = MutableStateFlow(true)
    val showPipeSpecsLayer = MutableStateFlow(true)
    val showContourElevationLayer = MutableStateFlow(true)
    val showCadFrameOverlay = MutableStateFlow(true)
    val showNorthArrowCompass = MutableStateFlow(true)
    val mapOverlayMode = MutableStateFlow(MapBaseOverlayMode.CAD_BLUEPRINT)
    val overlayOpacity = MutableStateFlow(0.85f)
    val selectedSite = MutableStateFlow("ALL") // "ALL", "SH_RASHIDO", "TMALIK"
    val showBlueprintLegendDialog = MutableStateFlow(false)

    val jumpTarget = MutableStateFlow<GeoPoint?>(null)

    // Automated schedule timer fast demo mode (seconds per shift) for instant testing
    private val _isFastDemoMode = MutableStateFlow(false)
    val isFastDemoMode: StateFlow<Boolean> = _isFastDemoMode.asStateFlow()

    // Live Open Valves Map Filter & Day-Wise Area Progress Tracking
    val showOnlyOpenValvesOnMap = MutableStateFlow(false)
    val selectedDayProgress = MutableStateFlow<DayWiseProgress?>(null)

    // Google Earth KML Document & Feature Selection State
    val kmlDocument = MutableStateFlow<com.example.data.model.KmlDocument?>(null)
    val selectedKmlFeature = MutableStateFlow<com.example.data.model.KmlFeature?>(null)

    init {
        // Load cached KML or generate default sample KML
        viewModelScope.launch(Dispatchers.IO) {
            val cacheFile = java.io.File(getApplication<Application>().filesDir, "saved_kml.kml")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val kmlText = cacheFile.readText()
                    val doc = com.example.data.model.KmlParser.parseKmlText(kmlText, cacheFile.name)
                    kmlDocument.value = doc
                } catch (_: Exception) {
                    kmlDocument.value = loadBundledLayoutKml()
                }
            } else {
                kmlDocument.value = loadBundledLayoutKml()
            }
        }
    }

    /**
     * Default map layer = merged real layout shipped in assets: CAD "Sardoba Rasido - final"
     * (PH 12-18) + CAD "Combine PH-7 to 11" + georeferenced Jain design PDF layers
     * (elevation contours, canal, mains, valves). Falls back to the old demo sample
     * only if the asset is missing.
     */
    private fun loadBundledLayoutKml(): com.example.data.model.KmlDocument {
        return try {
            val text = getApplication<Application>().assets
                .open("sardoba_layout_merged.kml").bufferedReader().use { it.readText() }
            com.example.data.model.KmlParser.parseKmlText(text, "sardoba_layout_merged.kml")
        } catch (_: Exception) {
            com.example.data.model.KmlParser.createSampleKml()
        }
    }


    fun loadKmlFile(inputStream: java.io.InputStream, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = com.example.data.model.KmlParser.parse(inputStream, fileName)
                kmlDocument.value = doc
                selectedKmlFeature.value = null
                // Cache to disk
                if (!doc.rawContent.isNullOrBlank()) {
                    val cacheFile = java.io.File(getApplication<Application>().filesDir, "saved_kml.kml")
                    cacheFile.writeText(doc.rawContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadKmlString(content: String, fileName: String = "Pasted_Data.kml") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = com.example.data.model.KmlParser.parseKmlText(content, fileName)
                kmlDocument.value = doc
                selectedKmlFeature.value = null
                val cacheFile = java.io.File(getApplication<Application>().filesDir, "saved_kml.kml")
                cacheFile.writeText(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSampleKml() {
        viewModelScope.launch(Dispatchers.IO) {
            val doc = loadBundledLayoutKml()
            kmlDocument.value = doc
            selectedKmlFeature.value = null
            if (!doc.rawContent.isNullOrBlank()) {
                val cacheFile = java.io.File(getApplication<Application>().filesDir, "saved_kml.kml")
                cacheFile.writeText(doc.rawContent)
            }
        }
    }

    fun selectKmlFeature(feature: com.example.data.model.KmlFeature?) {
        selectedKmlFeature.value = feature
        if (feature != null) {
            jumpTarget.value = feature.center
        }
    }

    fun toggleFeatureVisibility(featureId: String) {
        val currentDoc = kmlDocument.value ?: return
        val updatedFeatures = currentDoc.features.map { feat ->
            if (feat.id == featureId) feat.copy(isVisible = !feat.isVisible) else feat
        }
        val updatedFolders = currentDoc.folders.map { folder ->
            val updatedList = folder.features.map { f ->
                if (f.id == featureId) f.copy(isVisible = !f.isVisible) else f
            }
            folder.copy(features = updatedList)
        }
        kmlDocument.value = currentDoc.copy(features = updatedFeatures, folders = updatedFolders)
    }

    fun toggleFolderVisibility(folderName: String) {
        val currentDoc = kmlDocument.value ?: return
        val targetFolder = currentDoc.folders.find { it.name == folderName } ?: return
        val newVisible = !targetFolder.isVisible
        val updatedFolders = currentDoc.folders.map { folder ->
            if (folder.name == folderName) {
                val updatedList = folder.features.map { it.copy(isVisible = newVisible) }
                folder.copy(features = updatedList, isVisible = newVisible)
            } else folder
        }
        val updatedFeatures = currentDoc.features.map { feat ->
            if (feat.folderName == folderName || (folderName == "Ungrouped Places" && feat.folderName == null)) {
                feat.copy(isVisible = newVisible)
            } else feat
        }
        kmlDocument.value = currentDoc.copy(features = updatedFeatures, folders = updatedFolders)
    }

    /**
     * Unconditionally sets all layers, folders, and features in the KML file to visible without restrictions.
     */
    fun setAllLayersVisible(visible: Boolean = true) {
        val currentDoc = kmlDocument.value ?: return
        val updatedFeatures = currentDoc.features.map { it.copy(isVisible = visible) }
        val updatedFolders = currentDoc.folders.map { folder ->
            val updatedList = folder.features.map { it.copy(isVisible = visible) }
            folder.copy(features = updatedList, isVisible = visible)
        }
        kmlDocument.value = currentDoc.copy(features = updatedFeatures, folders = updatedFolders)
    }

    fun clearKml() {
        viewModelScope.launch(Dispatchers.IO) {
            kmlDocument.value = null
            selectedKmlFeature.value = null
            val cacheFile = java.io.File(getApplication<Application>().filesDir, "saved_kml.kml")
            if (cacheFile.exists()) cacheFile.delete()
        }
    }

    fun toggleShowOnlyOpenValvesOnMap() {
        showOnlyOpenValvesOnMap.value = !showOnlyOpenValvesOnMap.value
    }

    fun setMapOverlayMode(mode: MapBaseOverlayMode) {
        mapOverlayMode.value = mode
    }

    fun setOverlayOpacity(opacity: Float) {
        overlayOpacity.value = opacity.coerceIn(0.1f, 1.0f)
    }

    fun selectSite(site: String) {
        selectedSite.value = site
    }

    // Calculated Overview Stats (Tracking Live Open Valves and Day-Wise Area Hectares)
    val uiStats: StateFlow<IrrigationUiStats> = combine(
        pumpHouses,
        allValves,
        activeValves,
        systems
    ) { phList, allV, activeV, sysList ->
        val activeFlow = activeV.sumOf { it.actualFlowLps }
        val flowM3Hr = activeFlow * 3.6 // 1 lps = 3.6 m3/hr
        val runningPumps = phList.count { it.pumpState.name == "RUNNING" }
        val totalArea = phList.sumOf { it.totalAreaHa }
        val activeArea = activeV.sumOf { it.areaHa }

        // Day-wise completed area: accumulated completed shifts + active shift proportion
        var completedHa = 0.0
        sysList.forEach { sys ->
            val areaPerShift = if (sys.totalShifts > 0) sys.irrigatedAreaHa / sys.totalShifts else 0.0
            val completedShifts = (sys.activeShift - 1).coerceAtLeast(0)
            val elapsedFraction = (sys.shiftElapsedMinutes.toDouble() / sys.shiftDurationMinutes.coerceAtLeast(1).toDouble()).coerceIn(0.0, 1.0)
            completedHa += (completedShifts * areaPerShift) + (elapsedFraction * areaPerShift)
        }
        val roundedCompleted = Math.round(completedHa * 10.0) / 10.0
        val targetHa = 240.0
        val pct = if (targetHa > 0) ((roundedCompleted / targetHa) * 100).toInt().coerceIn(0, 100) else 0

        IrrigationUiStats(
            activeValvesCount = activeV.size,
            totalValvesCount = allV.size,
            activeFlowLps = activeFlow,
            activeFlowM3Hr = flowM3Hr,
            totalPumpHousesCount = phList.size,
            runningPumpsCount = runningPumps,
            totalAreaHa = totalArea,
            activeAreaHa = Math.round(activeArea * 10.0) / 10.0,
            todayCompletedAreaHa = roundedCompleted,
            dailyTargetAreaHa = targetHa,
            todayCompletionPct = pct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IrrigationUiStats())

    // 7-Day Day-Wise Hectares Completion History & Live Track
    val dayWiseProgressList: StateFlow<List<DayWiseProgress>> = combine(
        uiStats,
        activeValves
    ) { stats, activeV ->
        val todayWaterM3 = stats.todayCompletedAreaHa * 50.0

        listOf(
            DayWiseProgress(
                dateIso = "2026-09-11",
                dayLabel = "Today",
                isToday = true,
                completedAreaHa = stats.todayCompletedAreaHa,
                targetAreaHa = stats.dailyTargetAreaHa,
                activeAreaHa = stats.activeAreaHa,
                openValvesCount = stats.activeValvesCount,
                totalValvesRun = stats.activeValvesCount + (stats.todayCompletedAreaHa / 2.2).toInt(),
                waterDeliveredM3 = Math.round(todayWaterM3 * 10.0) / 10.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to Math.round(stats.todayCompletedAreaHa * 0.35 * 10.0) / 10.0,
                    "Zone 3 (T. Malik)" to Math.round(stats.todayCompletedAreaHa * 0.31 * 10.0) / 10.0,
                    "Zone 4 (Combine)" to Math.round(stats.todayCompletedAreaHa * 0.34 * 10.0) / 10.0
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-10",
                dayLabel = "Yesterday",
                isToday = false,
                completedAreaHa = 238.4,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 118,
                waterDeliveredM3 = 11920.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 78.5,
                    "Zone 3 (T. Malik)" to 73.2,
                    "Zone 4 (Combine)" to 86.7
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-09",
                dayLabel = "Wed, Sep 9",
                isToday = false,
                completedAreaHa = 242.1,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 120,
                waterDeliveredM3 = 12105.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 81.2,
                    "Zone 3 (T. Malik)" to 75.4,
                    "Zone 4 (Combine)" to 85.5
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-08",
                dayLabel = "Tue, Sep 8",
                isToday = false,
                completedAreaHa = 228.5,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 114,
                waterDeliveredM3 = 11425.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 74.0,
                    "Zone 3 (T. Malik)" to 71.0,
                    "Zone 4 (Combine)" to 83.5
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-07",
                dayLabel = "Mon, Sep 7",
                isToday = false,
                completedAreaHa = 239.0,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 119,
                waterDeliveredM3 = 11950.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 79.5,
                    "Zone 3 (T. Malik)" to 74.0,
                    "Zone 4 (Combine)" to 85.5
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-06",
                dayLabel = "Sun, Sep 6",
                isToday = false,
                completedAreaHa = 215.4,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 108,
                waterDeliveredM3 = 10770.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 71.2,
                    "Zone 3 (T. Malik)" to 66.8,
                    "Zone 4 (Combine)" to 77.4
                )
            ),
            DayWiseProgress(
                dateIso = "2026-09-05",
                dayLabel = "Sat, Sep 5",
                isToday = false,
                completedAreaHa = 244.2,
                targetAreaHa = 240.0,
                activeAreaHa = 0.0,
                openValvesCount = 0,
                totalValvesRun = 122,
                waterDeliveredM3 = 12210.0,
                zoneBreakdown = mapOf(
                    "Zone 2 (Sh. Rashido)" to 82.0,
                    "Zone 3 (T. Malik)" to 76.2,
                    "Zone 4 (Combine)" to 86.0
                )
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FilterParams(
        val zone: String? = null,
        val phId: String? = null,
        val sysId: String? = null,
        val shift: Int? = null,
        val filter: ValveFilterType = ValveFilterType.ALL,
        val query: String = ""
    )

    private val filterParams = combine(
        selectedZone,
        selectedPumpHouseId,
        selectedSystemId,
        selectedShiftFilter,
        valveFilter
    ) { zone, phId, sysId, shift, filter ->
        FilterParams(zone, phId, sysId, shift, filter, searchQuery.value)
    }.combine(searchQuery) { params, query ->
        params.copy(query = query)
    }

    // Filtered Valves for the current view
    val filteredValves: StateFlow<List<SubmainValveEntity>> = combine(
        allValves,
        filterParams
    ) { valves, params ->
        valves.filter { valve ->
            // Zone filter
            val zoneMatches = params.zone == null || params.zone == "All" || valve.zoneName == params.zone
            // Pump house filter
            val phMatches = params.phId == null || valve.pumpHouseId == params.phId
            // System filter
            val sysMatches = params.sysId == null || valve.systemId == params.sysId
            // Shift filter
            val shiftMatches = params.shift == null || valve.shiftNumber == params.shift
            // Status filter
            val statusMatches = when (params.filter) {
                ValveFilterType.ALL -> true
                ValveFilterType.OPEN_ACTIVE -> valve.isControlValveOpen
                ValveFilterType.CLOSED -> !valve.isControlValveOpen && !valve.isFlushValveOpen
                ValveFilterType.FLUSHING -> valve.isFlushValveOpen
                ValveFilterType.ALERTS -> valve.hasAlert
            }
            // Search query filter
            val queryMatches = params.query.isBlank() ||
                    valve.tag.contains(params.query, ignoreCase = true) ||
                    valve.pumpHouseId.contains(params.query, ignoreCase = true) ||
                    valve.systemId.contains(params.query, ignoreCase = true) ||
                    valve.zoneName.contains(params.query, ignoreCase = true) ||
                    "Shift ${valve.shiftNumber}".contains(params.query, ignoreCase = true)

            zoneMatches && phMatches && sysMatches && shiftMatches && statusMatches && queryMatches
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectZone(zone: String?) {
        selectedZone.value = zone
        val currentPhs = pumpHouses.value.filter { zone == null || zone == "All" || it.zoneName == zone }
        val newPhId = currentPhs.firstOrNull()?.id
        selectedPumpHouseId.value = newPhId
        val currentSystems = systems.value.filter { newPhId == null || it.pumpHouseId == newPhId }
        selectedSystemId.value = currentSystems.firstOrNull()?.id
    }

    fun selectPumpHouse(id: String?) {
        selectedPumpHouseId.value = id
        val ph = pumpHouses.value.find { it.id == id }
        if (ph != null) {
            selectedZone.value = ph.zoneName
        }
        val currentSystems = systems.value.filter { id == null || it.pumpHouseId == id }
        selectedSystemId.value = currentSystems.firstOrNull()?.id
    }

    fun selectSystem(id: String?) {
        selectedSystemId.value = id
        val system = systems.value.find { it.id == id }
        if (system != null) {
            selectedZone.value = system.zoneName
            selectedPumpHouseId.value = system.pumpHouseId
            selectedShiftFilter.value = system.activeShift
        }
    }

    fun selectShift(shift: Int?) {
        selectedShiftFilter.value = shift
    }

    fun setValveFilter(filter: ValveFilterType) {
        valveFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleFastDemoMode() {
        _isFastDemoMode.value = !_isFastDemoMode.value
    }

    fun selectMapValve(valve: SubmainValveEntity?) {
        selectedMapValve.value = valve
        if (valve != null) {
            selectedMapPumpHouse.value = null
        }
    }

    fun selectMapPumpHouse(ph: PumpHouseEntity?) {
        selectedMapPumpHouse.value = ph
        if (ph != null) {
            selectedMapValve.value = null
        }
    }

    fun simulateLocation(latitude: Double, longitude: Double) {
        locationTracker.simulateLocation(latitude, longitude)
        locationTracker.recalculateProximities(allValves.value, pumpHouses.value)
    }

    fun enableRealGps(hasFine: Boolean, hasCoarse: Boolean) {
        locationTracker.enableRealGps(hasFine, hasCoarse)
    }

    fun startLocationUpdates(hasFine: Boolean, hasCoarse: Boolean) {
        locationTracker.startLocationUpdates(hasFine, hasCoarse)
    }

    fun stopLocationUpdates() {
        locationTracker.stopLocationUpdates()
    }

    fun toggleControlValve(tag: String, open: Boolean) {
        viewModelScope.launch {
            repository.toggleControlValve(tag, open)
            // Update selectedMapValve if open/closed from map
            if (selectedMapValve.value?.tag == tag) {
                selectedMapValve.value = selectedMapValve.value?.copy(
                    isControlValveOpen = open,
                    valveState = if (open) ValveState.OPEN_IRRIGATING else ValveState.CLOSED
                )
            }
        }
    }

    fun toggleFlushValve(tag: String, open: Boolean) {
        viewModelScope.launch {
            repository.toggleFlushValve(tag, open)
        }
    }

    fun switchShift(systemId: String, newShift: Int) {
        viewModelScope.launch {
            repository.switchShift(systemId, newShift)
            selectedShiftFilter.value = newShift
        }
    }

    fun setShiftValves(systemId: String, shiftNumber: Int, open: Boolean) {
        viewModelScope.launch {
            repository.setShiftValves(systemId, shiftNumber, open)
        }
    }

    fun emergencyShutoff() {
        viewModelScope.launch {
            repository.emergencyShutoff()
            selectedMapValve.value = selectedMapValve.value?.copy(
                isControlValveOpen = false,
                isFlushValveOpen = false,
                valveState = ValveState.CLOSED
            )
        }
    }

    fun toggleAutoSequence(systemId: String, isRunning: Boolean) {
        viewModelScope.launch {
            repository.updateSystemAutoSequence(systemId, isRunning)
        }
    }

    fun updateScheduleConfig(config: ScheduleConfigEntity) {
        viewModelScope.launch {
            repository.updateScheduleConfig(config)
        }
    }

    fun jumpToP29Location(): GeoPoint {
        val p29Coord = GeoPoint(40.446028, 68.285828)
        jumpTarget.value = p29Coord
        val p29Valve = allValves.value.find { it.tag.equals("P29-1", ignoreCase = true) }
        if (p29Valve != null) {
            selectedMapValve.value = p29Valve
        }
        return p29Coord
    }

    fun jumpToLocationOrValve(query: String): GeoPoint? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        // 1. Try direct valve tag match
        val valve = allValves.value.find { it.tag.equals(trimmed, ignoreCase = true) }
        if (valve != null) {
            val point = GeoPoint(valve.latitude, valve.longitude)
            selectedMapValve.value = valve
            jumpTarget.value = point
            return point
        }

        // 2. Try pump house id or name match
        val ph = pumpHouses.value.find { it.id.equals(trimmed, ignoreCase = true) || it.name.contains(trimmed, ignoreCase = true) }
        if (ph != null) {
            val point = GeoPoint(ph.latitude, ph.longitude)
            selectedMapPumpHouse.value = ph
            jumpTarget.value = point
            return point
        }

        // 3. Try parsing coordinates (DMS or decimal)
        val parsed = com.example.data.model.GeoUtils.parseCoordinates(trimmed)
        if (parsed != null) {
            jumpTarget.value = parsed
            return parsed
        }

        return null
    }

    fun jumpToTarget(point: GeoPoint) {
        jumpTarget.value = point
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            var counter = 0
            while (isActive) {
                delay(5000) // 5-second live sensor update
                repository.simulateTelemetryTick()
                if (allValves.value.isNotEmpty()) {
                    locationTracker.recalculateProximities(allValves.value, pumpHouses.value)
                }

                // Fast Demo Mode automatic shift rotation every 15 seconds
                if (_isFastDemoMode.value) {
                    counter++
                    if (counter >= 3) {
                        counter = 0
                        val activeSys = selectedSystemId.value ?: "SYS-32"
                        val sys = systems.value.find { it.id == activeSys }
                        if (sys != null && sys.isAutoSequenceRunning) {
                            val nextShift = if (sys.activeShift >= 12) 1 else sys.activeShift + 1
                            repository.switchShift(activeSys, nextShift)
                            selectedShiftFilter.value = nextShift
                        }
                    }
                }
            }
        }
    }
}


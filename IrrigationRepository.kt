package com.example.data.repository

import com.example.data.local.IrrigationDao
import com.example.data.model.IrrigationLogEntity
import com.example.data.model.FieldValveLogEntity
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.PumpHouseEntity
import com.example.data.model.ScheduleConfigEntity
import com.example.data.model.SubmainValveEntity
import com.example.data.model.ValveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.random.Random

class IrrigationRepository(private val dao: IrrigationDao) {

    val allPumpHouses: Flow<List<PumpHouseEntity>> = dao.getAllPumpHouses()
    val allSystems: Flow<List<IrrigationSystemEntity>> = dao.getAllSystems()
    val allValves: Flow<List<SubmainValveEntity>> = dao.getAllValves()
    val activeValves: Flow<List<SubmainValveEntity>> = dao.getActiveIrrigatingValves()
    val recentLogs: Flow<List<IrrigationLogEntity>> = dao.getRecentLogs(50)
    val allScheduleConfigs: Flow<List<ScheduleConfigEntity>> = dao.getAllScheduleConfigs()

    fun getSystemsForPumpHouse(pumpHouseId: String): Flow<List<IrrigationSystemEntity>> =
        dao.getSystemsForPumpHouse(pumpHouseId)

    fun getValvesForSystem(systemId: String): Flow<List<SubmainValveEntity>> =
        dao.getValvesForSystem(systemId)

    fun getValvesForPumpHouse(pumpHouseId: String): Flow<List<SubmainValveEntity>> =
        dao.getValvesForPumpHouse(pumpHouseId)

    fun getScheduleConfig(systemId: String): Flow<ScheduleConfigEntity?> =
        dao.getScheduleConfig(systemId)

    val fieldLogs: Flow<List<FieldValveLogEntity>> = dao.getAllFieldLogs()

    suspend fun insertFieldLog(log: FieldValveLogEntity) = dao.upsertFieldLog(log)

    /** Soft delete: kept as a tombstone until the server has it, so the delete reaches every phone. */
    suspend fun deleteFieldLog(uuid: String) = dao.markFieldLogDeleted(uuid)

    suspend fun setValveShift(tag: String, shift: Int) = dao.setValveShift(tag, shift)

    suspend fun toggleControlValve(tag: String, open: Boolean) {
        val valve = dao.getValveByTag(tag).firstOrNull() ?: return
        dao.setControlValveState(tag, open)

        val action = if (open) "CONTROL_VALVE_OPEN" else "CONTROL_VALVE_CLOSE"
        val desc = if (open) "Manually opened Submain Control Valve $tag (Flow: ${valve.designFlowLps} lps)"
        else "Manually closed Submain Control Valve $tag"
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = valve.pumpHouseId,
                systemId = valve.systemId,
                submainTag = tag,
                action = action,
                details = desc,
                sectionalFlowLps = if (open) valve.designFlowLps else 0.0
            )
        )
    }

    suspend fun toggleFlushValve(tag: String, open: Boolean) {
        val valve = dao.getValveByTag(tag).firstOrNull() ?: return
        dao.setFlushValveState(tag, open)

        val action = if (open) "FLUSH_VALVE_OPEN" else "FLUSH_VALVE_CLOSE"
        val desc = if (open) "Opened Submain Flush Valve $tag for line cleaning"
        else "Closed Submain Flush Valve $tag"
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = valve.pumpHouseId,
                systemId = valve.systemId,
                submainTag = tag,
                action = action,
                details = desc,
                sectionalFlowLps = 0.0
            )
        )
    }

    suspend fun switchShift(systemId: String, newShift: Int) {
        val system = dao.getSystemById(systemId).firstOrNull() ?: return
        val currentShift = system.activeShift

        // 1. Close all valves in old shift
        dao.setShiftValvesState(systemId, currentShift, false)

        // 2. Open all valves in new shift
        dao.setShiftValvesState(systemId, newShift, true)

        // 3. Update system activeShift and reset elapsed time
        val updatedSystem = system.copy(
            activeShift = newShift,
            shiftElapsedMinutes = 0
        )
        dao.updateSystem(updatedSystem)

        // 4. Update schedule config if exists
        val config = dao.getScheduleConfig(systemId).firstOrNull()
        if (config != null) {
            dao.updateScheduleConfig(
                config.copy(
                    currentShift = newShift,
                    nextShiftScheduledTime = System.currentTimeMillis() + (config.shiftDurationMinutes * 60 * 1000)
                )
            )
        }

        // Log transition
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = system.pumpHouseId,
                systemId = systemId,
                submainTag = "Shift S$newShift",
                action = "SHIFT_TRANSITION",
                details = "Automated shift switched from S$currentShift to S$newShift. Valves updated.",
                sectionalFlowLps = system.targetDischargeLps
            )
        )
    }

    suspend fun setShiftValves(systemId: String, shiftNumber: Int, open: Boolean) {
        val system = dao.getSystemById(systemId).firstOrNull()
        dao.setShiftValvesState(systemId, shiftNumber, open)
        if (system != null) {
            dao.insertLog(
                IrrigationLogEntity(
                    pumpHouseId = system.pumpHouseId,
                    systemId = systemId,
                    submainTag = "Shift S$shiftNumber",
                    action = if (open) "SHIFT_VALVES_OPENED" else "SHIFT_VALVES_CLOSED",
                    details = "Shift S$shiftNumber valves all set to ${if (open) "OPEN" else "CLOSED"}",
                    sectionalFlowLps = if (open) system.targetDischargeLps else 0.0
                )
            )
        }
    }

    suspend fun emergencyShutoff() {
        dao.emergencyShutoffAll()
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = "ALL",
                systemId = "ALL",
                submainTag = "ALL_VALVES",
                action = "EMERGENCY_SHUTOFF",
                details = "Emergency shutdown triggered! All control & flush valves closed immediately.",
                sectionalFlowLps = 0.0
            )
        )
    }

    suspend fun updateScheduleConfig(config: ScheduleConfigEntity) {
        dao.updateScheduleConfig(config)
    }

    suspend fun updateSystemAutoSequence(systemId: String, isRunning: Boolean) {
        val system = dao.getSystemById(systemId).firstOrNull() ?: return
        dao.updateSystem(system.copy(isAutoSequenceRunning = isRunning))
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = system.pumpHouseId,
                systemId = systemId,
                submainTag = "Scheduler",
                action = if (isRunning) "AUTO_SCHEDULE_RESUMED" else "AUTO_SCHEDULE_PAUSED",
                details = "Automated shift progression ${if (isRunning) "resumed" else "paused"} for ${system.name}"
            )
        )
    }

    suspend fun saveValve(valve: SubmainValveEntity, oldTag: String? = null) {
        if (oldTag != null && oldTag != valve.tag) {
            dao.deleteValveByTag(oldTag)
        }
        dao.insertValve(valve)
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = valve.pumpHouseId,
                systemId = valve.systemId,
                submainTag = valve.tag,
                action = "VALVE_STRUCTURE_SAVED",
                details = "Valve ${valve.tag} (Submain ${valve.submainNumber}, Shift ${valve.shiftNumber}, Ø${valve.pipeDiameterMm}mm) saved at GPS: ${valve.latitude}, ${valve.longitude}",
                sectionalFlowLps = if (valve.isControlValveOpen) valve.actualFlowLps else 0.0
            )
        )
    }

    suspend fun deleteValve(tag: String) {
        val valve = dao.getValveByTag(tag).firstOrNull()
        dao.deleteValveByTag(tag)
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = valve?.pumpHouseId ?: "SYSTEM",
                systemId = valve?.systemId ?: "GIS",
                submainTag = tag,
                action = "VALVE_DELETED",
                details = "Submain Valve $tag removed from GIS layout",
                sectionalFlowLps = 0.0
            )
        )
    }

    suspend fun updateValveLocation(tag: String, lat: Double, lng: Double) {
        dao.updateValveLocation(tag, lat, lng)
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = "GIS",
                systemId = "GIS",
                submainTag = tag,
                action = "VALVE_REPOSITIONED",
                details = "Valve $tag repositioned on map to Lat: $lat, Lng: $lng",
                sectionalFlowLps = 0.0
            )
        )
    }

    suspend fun savePumpHouse(pumpHouse: PumpHouseEntity) {
        dao.insertPumpHouse(pumpHouse)
        dao.insertLog(
            IrrigationLogEntity(
                pumpHouseId = pumpHouse.id,
                systemId = "PUMP_STATION",
                submainTag = pumpHouse.id,
                action = "STATION_STRUCTURE_SAVED",
                details = "Pump House ${pumpHouse.name} (${pumpHouse.siteLocation}) updated at GPS: ${pumpHouse.latitude}, ${pumpHouse.longitude}",
                sectionalFlowLps = 0.0
            )
        )
    }

    suspend fun simulateTelemetryTick() = withContext(Dispatchers.IO) {
        val pumpHouses = dao.getAllPumpHouses().firstOrNull() ?: return@withContext
        val updatedList = pumpHouses.map { ph ->
            // Subtle real-world sensor fluctuations:
            val deltaPress = (Random.nextFloat() - 0.5f) * 0.06f
            val newPress = (ph.headerPressureBar + deltaPress).coerceIn(2.8f, 4.0f)
            ph.copy(
                headerPressureBar = Math.round(newPress * 100f) / 100f,
                waterStorageLevelPct = (ph.waterStorageLevelPct - 0.02f).coerceIn(60f, 99f)
            )
        }
        dao.updatePumpHouses(updatedList)
    }
}

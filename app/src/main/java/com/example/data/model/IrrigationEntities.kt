package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ValveState {
    OPEN_IRRIGATING,
    CLOSED,
    FLUSHING,
    SCHEDULED,
    FAULT_ALERT
}

enum class PumpState {
    RUNNING,
    STANDBY,
    MAINTENANCE,
    STOPPED
}

@Entity(tableName = "pump_houses")
data class PumpHouseEntity(
    @PrimaryKey val id: String, // e.g. "PH-15", "PH-7"
    val name: String, // "Pump House 15"
    val zoneName: String, // "Zone 2", "Zone 3", "Zone 4"
    val siteLocation: String, // "Sardoba (Sh. Rashido Site)"
    val latitude: Double,
    val longitude: Double,
    val dimensions: String = "15.0m x 10.0m",
    val waterStorageDimensions: String = "Top 25m x 50m",
    val sedimentationTankDimensions: String = "Top 15m x 25m",
    val fertigationTankCapacityM3: Double = 82.0,
    val waterStorageLevelPct: Float = 86.4f, // 0 - 100%
    val sedimentationLevelPct: Float = 92.0f,
    val fertigationEcMsCm: Float = 1.45f, // Electrical conductivity
    val fertigationPh: Float = 6.8f,
    val pumpState: PumpState = PumpState.RUNNING,
    val pumpPowerKw: Float = 45.0f,
    val pumpRpm: Int = 1450,
    val pumpFrequencyHz: Float = 50.0f,
    val headerPressureBar: Float = 3.4f,
    val filterInletPressureBar: Float = 3.6f,
    val filterOutletPressureBar: Float = 3.25f,
    val intakeCanalStatus: String = "Flow Normal (Canal)",
    val totalAreaHa: Double = 156.3,
    val totalSystemsCount: Int = 3,
    val ratedDischargeM3Hr: Double = 350.0
)

@Entity(tableName = "irrigation_systems")
data class IrrigationSystemEntity(
    @PrimaryKey val id: String, // e.g. "SYS-32", "SYS-24"
    val pumpHouseId: String, // Foreign key to pump house
    val zoneName: String, // "Zone 2", "Zone 3", "Zone 4"
    val systemNumber: Int, // 32
    val name: String, // "System 32"
    val crop: String = "Cotton",
    val rowSpacingM: Double = 0.90,
    val driplineSpacingM: Double = 0.90,
    val driplineType: String = "Drip Tape - Thin Wall (22 mm)",
    val dripperDischargeLph: Double = 1.25,
    val dripperSpacingM: Double = 0.30,
    val applicationRateMmPerHr: Double = 4.63,
    val peakWaterReqMmPerDay: Double = 9.31,
    val grossAreaHa: Double = 57.0,
    val irrigatedAreaHa: Double = 52.1,
    val totalShifts: Int = 12,
    val activeShift: Int = 1, // 1 to 12
    val isAutoSequenceRunning: Boolean = true,
    val shiftDurationMinutes: Int = 120, // 2.0 hrs standard
    val shiftElapsedMinutes: Int = 42,
    val targetDischargeLps: Double = 61.79,
    val actualDischargeLps: Double = 51.75,
    val latitude: Double = 40.42,
    val longitude: Double = 68.31
)

@Entity(tableName = "submain_valves")
data class SubmainValveEntity(
    @PrimaryKey val tag: String, // e.g. "P32-S1", "P24-S5"
    val systemId: String, // "SYS-32"
    val pumpHouseId: String, // "PH-15"
    val zoneName: String, // "Zone 2", "Zone 3", "Zone 4"
    val submainNumber: Int, // 1
    val shiftNumber: Int, // 1 to 12
    val areaHa: Double, // 1.9 ha
    val designFlowLps: Double, // 23.81 lps
    val actualFlowLps: Double, // 23.81 when open, 0 when closed
    val submainLengthM: Double, // 112 m
    val pipeDiameterMm: Int = 110, // Outer diameter 110mm, 160mm, 200mm, etc.
    val isControlValveOpen: Boolean = true,
    val isFlushValveOpen: Boolean = false,
    val valveState: ValveState = ValveState.OPEN_IRRIGATING,
    val linePressureBar: Float = 1.82f,
    val soilMoisture20cmPct: Float = 26.5f,
    val soilMoisture40cmPct: Float = 31.0f,
    val hasAlert: Boolean = false,
    val alertMessage: String? = null,
    val lastStateChangedTimestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 40.42,
    val longitude: Double = 68.31
)

@Entity(tableName = "schedule_configs")
data class ScheduleConfigEntity(
    @PrimaryKey val systemId: String,
    val autoAdvanceEnabled: Boolean = true,
    val shiftDurationMinutes: Int = 120, // 2 hours
    val cycleActive: Boolean = true,
    val currentShift: Int = 1,
    val autoMoistureTriggerEnabled: Boolean = true,
    val moistureTriggerThresholdPct: Float = 22.0f,
    val maxShiftsPerCycle: Int = 12,
    val cycleStartDate: Long = System.currentTimeMillis(),
    val nextShiftScheduledTime: Long = System.currentTimeMillis() + (78 * 60 * 1000)
)

@Entity(tableName = "irrigation_logs")
data class IrrigationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pumpHouseId: String,
    val systemId: String,
    val submainTag: String,
    val action: String, // "CONTROL_VALVE_OPEN", "CONTROL_VALVE_CLOSE", "SHIFT_ADVANCE", "ALERT"
    val details: String,
    val sectionalFlowLps: Double = 0.0
)

data class LiveSensorsSnapshot(
    val headerPressureBar: Float,
    val filterDpBar: Float,
    val totalFlowLps: Double,
    val activeAreaHa: Double = 0.0,
    val soilMoistureAvgPct: Float,
    val fertigationEc: Float,
    val fertigationPh: Float,
    val pumpPowerKw: Float,
    val pumpRpm: Int,
    val waterStorageLevelPct: Float = 0f,
    val sedimentationLevelPct: Float = 0f
)

data class DayWiseProgress(
    val dateIso: String,
    val dayLabel: String,
    val isToday: Boolean,
    val completedAreaHa: Double,
    val targetAreaHa: Double,
    val activeAreaHa: Double,
    val openValvesCount: Int,
    val totalValvesRun: Int,
    val waterDeliveredM3: Double,
    val completionPercentage: Int = if (targetAreaHa > 0) ((completedAreaHa / targetAreaHa) * 100).toInt().coerceIn(0, 100) else 0,
    val zoneBreakdown: Map<String, Double> = emptyMap()
)

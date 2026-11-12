package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.IrrigationLogEntity
import com.example.data.model.FieldValveLogEntity
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.PumpHouseEntity
import com.example.data.model.ScheduleConfigEntity
import com.example.data.model.SubmainValveEntity
import com.example.data.model.ValveState
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationDao {

    // --- Pump Houses ---
    @Query("SELECT * FROM pump_houses ORDER BY id ASC")
    fun getAllPumpHouses(): Flow<List<PumpHouseEntity>>

    @Query("SELECT * FROM pump_houses WHERE id = :id LIMIT 1")
    fun getPumpHouseById(id: String): Flow<PumpHouseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPumpHouse(pumpHouse: PumpHouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPumpHouses(pumpHouses: List<PumpHouseEntity>)

    @Update
    suspend fun updatePumpHouse(pumpHouse: PumpHouseEntity)

    @Update
    suspend fun updatePumpHouses(pumpHouses: List<PumpHouseEntity>)

    @Query("DELETE FROM pump_houses WHERE id = :id")
    suspend fun deletePumpHouse(id: String)

    // --- Irrigation Systems ---
    @Query("SELECT * FROM irrigation_systems ORDER BY systemNumber ASC")
    fun getAllSystems(): Flow<List<IrrigationSystemEntity>>

    @Query("SELECT * FROM irrigation_systems WHERE pumpHouseId = :pumpHouseId ORDER BY systemNumber ASC")
    fun getSystemsForPumpHouse(pumpHouseId: String): Flow<List<IrrigationSystemEntity>>

    @Query("SELECT * FROM irrigation_systems WHERE id = :id LIMIT 1")
    fun getSystemById(id: String): Flow<IrrigationSystemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystems(systems: List<IrrigationSystemEntity>)

    @Update
    suspend fun updateSystem(system: IrrigationSystemEntity)

    // --- Submain Valves ---
    @Query("SELECT * FROM submain_valves ORDER BY tag ASC")
    fun getAllValves(): Flow<List<SubmainValveEntity>>

    @Query("SELECT * FROM submain_valves WHERE systemId = :systemId ORDER BY submainNumber ASC")
    fun getValvesForSystem(systemId: String): Flow<List<SubmainValveEntity>>

    @Query("SELECT * FROM submain_valves WHERE pumpHouseId = :pumpHouseId ORDER BY submainNumber ASC")
    fun getValvesForPumpHouse(pumpHouseId: String): Flow<List<SubmainValveEntity>>

    @Query("SELECT * FROM submain_valves WHERE systemId = :systemId AND shiftNumber = :shiftNumber ORDER BY submainNumber ASC")
    fun getValvesForShift(systemId: String, shiftNumber: Int): Flow<List<SubmainValveEntity>>

    @Query("SELECT * FROM submain_valves WHERE isControlValveOpen = 1 ORDER BY tag ASC")
    fun getActiveIrrigatingValves(): Flow<List<SubmainValveEntity>>

    @Query("SELECT * FROM submain_valves WHERE tag = :tag LIMIT 1")
    fun getValveByTag(tag: String): Flow<SubmainValveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValve(valve: SubmainValveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValves(valves: List<SubmainValveEntity>)

    @Query("DELETE FROM submain_valves WHERE tag = :tag")
    suspend fun deleteValveByTag(tag: String)

    @Query("DELETE FROM submain_valves")
    suspend fun deleteAllValves()

    @Update
    suspend fun updateValve(valve: SubmainValveEntity)

    @Update
    suspend fun updateValves(valves: List<SubmainValveEntity>)

    @Query("UPDATE submain_valves SET latitude = :lat, longitude = :lng WHERE tag = :tag")
    suspend fun updateValveLocation(tag: String, lat: Double, lng: Double)

    @Query("UPDATE submain_valves SET isControlValveOpen = :isOpen, actualFlowLps = CASE WHEN :isOpen = 1 THEN designFlowLps ELSE 0.0 END, valveState = CASE WHEN :isOpen = 1 THEN 'OPEN_IRRIGATING' ELSE 'CLOSED' END WHERE tag = :tag")
    suspend fun setControlValveState(tag: String, isOpen: Boolean)

    @Query("UPDATE submain_valves SET isFlushValveOpen = :isFlushOpen, valveState = CASE WHEN :isFlushOpen = 1 THEN 'FLUSHING' WHEN isControlValveOpen = 1 THEN 'OPEN_IRRIGATING' ELSE 'CLOSED' END WHERE tag = :tag")
    suspend fun setFlushValveState(tag: String, isFlushOpen: Boolean)

    @Query("UPDATE submain_valves SET isControlValveOpen = 0, isFlushValveOpen = 0, actualFlowLps = 0.0, valveState = 'CLOSED'")
    suspend fun emergencyShutoffAll()

    @Query("UPDATE submain_valves SET isControlValveOpen = :isOpen, actualFlowLps = CASE WHEN :isOpen = 1 THEN designFlowLps ELSE 0.0 END, valveState = CASE WHEN :isOpen = 1 THEN 'OPEN_IRRIGATING' ELSE 'CLOSED' END WHERE systemId = :systemId AND shiftNumber = :shiftNumber")
    suspend fun setShiftValvesState(systemId: String, shiftNumber: Int, isOpen: Boolean)

    // --- Scheduling Config ---
    @Query("SELECT * FROM schedule_configs WHERE systemId = :systemId LIMIT 1")
    fun getScheduleConfig(systemId: String): Flow<ScheduleConfigEntity?>

    @Query("SELECT * FROM schedule_configs")
    fun getAllScheduleConfigs(): Flow<List<ScheduleConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleConfigs(configs: List<ScheduleConfigEntity>)

    @Update
    suspend fun updateScheduleConfig(config: ScheduleConfigEntity)

    // --- Irrigation Activity Logs ---
    @Query("SELECT * FROM irrigation_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<IrrigationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: IrrigationLogEntity)

    @Query("SELECT COUNT(*) FROM submain_valves")
    suspend fun getValvesCount(): Int

    @Query("UPDATE submain_valves SET shiftNumber = :shift WHERE tag = :tag")
    suspend fun setValveShift(tag: String, shift: Int)

    // --- REAL field valve log (separate from demo telemetry logs) ---
    @Insert
    suspend fun insertFieldLog(log: FieldValveLogEntity)

    @Query("SELECT * FROM field_valve_log ORDER BY timestamp DESC")
    fun getAllFieldLogs(): Flow<List<FieldValveLogEntity>>

    @Query("DELETE FROM field_valve_log WHERE id = :id")
    suspend fun deleteFieldLog(id: Long)
}

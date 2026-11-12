package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * REAL field record of a valve being opened or closed by a person in the field.
 * Kept separate from [IrrigationLogEntity], which the demo telemetry loop also writes to.
 *
 * Synced between all phones through the Cloudflare sync server:
 *  - [uuid] is created on the phone, so the same record is never duplicated
 *  - [synced] = false means "saved on this phone, not yet on the server" (offline queue)
 *  - [deleted] records are kept as tombstones so a delete also reaches the other phones
 */
@Entity(tableName = "field_valve_log")
data class FieldValveLogEntity(
    @PrimaryKey val uuid: String,
    val timestamp: Long = System.currentTimeMillis(),
    val valveTag: String,          // Jain drawing tag, e.g. "P29-3"
    val systemNumber: Int,         // 29
    val pumpHouseId: String,       // "PH-12"
    val newBlock: String,          // "PH12-1" (from Drip New block 2026 Nov)
    val action: String,            // "OPEN" or "CLOSE"
    val operatorName: String,
    val note: String = "",
    val areaHa: Double,            // gross area served by this valve (system area / valves)
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val distanceToValveM: Double? = null,
    val deviceId: String = "",
    val synced: Boolean = false,
    val serverSeq: Long? = null,
    val deleted: Boolean = false
)

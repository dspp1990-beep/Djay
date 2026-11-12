package com.example.data.local

import com.example.data.model.GeoPoint
import com.example.data.model.IrrigationSystemEntity
import com.example.data.model.KmlGeoData
import com.example.data.model.PumpHouseEntity
import com.example.data.model.PumpState
import com.example.data.model.ScheduleConfigEntity
import com.example.data.model.SubmainValveEntity
import com.example.data.model.ValveState

object SeedData {

    fun getInitialPumpHouses(): List<PumpHouseEntity> {
        return listOf(
            // === ZONE 4 (Combine PH 7 - 11) ===
            PumpHouseEntity(
                id = "PH-7",
                name = "Pump House 7",
                zoneName = "Zone 4",
                siteLocation = "Sardoba (Combine PH 7-11)",
                latitude = 40.421494,
                longitude = 68.344360,
                totalAreaHa = 110.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.35f,
                waterStorageLevelPct = 88.5f,
                sedimentationLevelPct = 91.0f
            ),
            PumpHouseEntity(
                id = "PH-8",
                name = "Pump House 8",
                zoneName = "Zone 4",
                siteLocation = "Sardoba (Combine PH 7-11)",
                latitude = 40.419718,
                longitude = 68.353501,
                totalAreaHa = 120.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.42f,
                waterStorageLevelPct = 86.0f,
                sedimentationLevelPct = 93.5f
            ),
            PumpHouseEntity(
                id = "PH-9",
                name = "Pump House 9",
                zoneName = "Zone 4",
                siteLocation = "Sardoba (Combine PH 7-11)",
                latitude = 40.417640,
                longitude = 68.362336,
                totalAreaHa = 108.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.30f,
                waterStorageLevelPct = 84.5f,
                sedimentationLevelPct = 89.0f
            ),
            PumpHouseEntity(
                id = "PH-10",
                name = "Pump House 10",
                zoneName = "Zone 4",
                siteLocation = "Sardoba (Combine PH 7-11)",
                latitude = 40.418189,
                longitude = 68.361789,
                totalAreaHa = 126.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.48f,
                waterStorageLevelPct = 90.0f,
                sedimentationLevelPct = 94.0f
            ),
            PumpHouseEntity(
                id = "PH-11",
                name = "Pump House 11",
                zoneName = "Zone 4",
                siteLocation = "Sardoba (Combine PH 7-11)",
                latitude = 40.416222,
                longitude = 68.371534,
                totalAreaHa = 129.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.52f,
                waterStorageLevelPct = 92.4f,
                sedimentationLevelPct = 95.0f
            ),

            // === ZONE 2 (Sardoba Rasido) ===
            PumpHouseEntity(
                id = "PH-12",
                name = "Pump House 12",
                zoneName = "Zone 2",
                siteLocation = "Sardoba Rasido",
                latitude = 40.430834,
                longitude = 68.283354,
                totalAreaHa = 89.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.28f,
                waterStorageLevelPct = 83.0f,
                sedimentationLevelPct = 88.0f
            ),
            PumpHouseEntity(
                id = "PH-13",
                name = "Pump House 13",
                zoneName = "Zone 2",
                siteLocation = "Sardoba Rasido",
                latitude = 40.427553,
                longitude = 68.290054,
                totalAreaHa = 129.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.40f,
                waterStorageLevelPct = 81.0f,
                sedimentationLevelPct = 87.5f
            ),
            PumpHouseEntity(
                id = "PH-14",
                name = "Pump House 14",
                zoneName = "Zone 2",
                siteLocation = "Sardoba Rasido",
                latitude = 40.423337,
                longitude = 68.297305,
                totalAreaHa = 133.0,
                totalSystemsCount = 1,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.44f,
                waterStorageLevelPct = 85.0f,
                sedimentationLevelPct = 90.0f
            ),
            PumpHouseEntity(
                id = "PH-18",
                name = "Pump House 18",
                zoneName = "Zone 2",
                siteLocation = "Sardoba Rasido",
                latitude = 40.424442,
                longitude = 68.323110,
                totalAreaHa = 186.0, // 76.5 + 62.5 + 47.0
                totalSystemsCount = 3,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.55f,
                waterStorageLevelPct = 93.0f,
                sedimentationLevelPct = 96.0f
            ),

            // === ZONE 3 (Sardoba Rasido) ===
            PumpHouseEntity(
                id = "PH-15",
                name = "Pump House 15",
                zoneName = "Zone 3",
                siteLocation = "Sardoba Rasido",
                latitude = 40.426506,
                longitude = 68.291219,
                totalAreaHa = 164.7, // 48.3 + 58.5 + 57.9
                totalSystemsCount = 3,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.42f,
                waterStorageLevelPct = 87.5f,
                sedimentationLevelPct = 94.0f
            ),
            PumpHouseEntity(
                id = "PH-16",
                name = "Pump House 16",
                zoneName = "Zone 3",
                siteLocation = "Sardoba Rasido",
                latitude = 40.421500,
                longitude = 68.300500,
                totalAreaHa = 159.3, // 46.8 + 54.0 + 58.5
                totalSystemsCount = 3,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.38f,
                waterStorageLevelPct = 82.1f,
                sedimentationLevelPct = 89.5f
            ),
            PumpHouseEntity(
                id = "PH-17",
                name = "Pump House 17",
                zoneName = "Zone 3",
                siteLocation = "Sardoba Rasido",
                latitude = 40.416563,
                longitude = 68.309460,
                totalAreaHa = 165.3, // 48.0 + 63.3 + 54.0
                totalSystemsCount = 3,
                pumpState = PumpState.RUNNING,
                headerPressureBar = 3.46f,
                waterStorageLevelPct = 88.0f,
                sedimentationLevelPct = 92.0f
            )
        )
    }

    fun getInitialSystems(): List<IrrigationSystemEntity> {
        return listOf(
            // === Zone 4: Systems 24 to 28 ===
            IrrigationSystemEntity(
                id = "SYS-24",
                pumpHouseId = "PH-7",
                zoneName = "Zone 4",
                systemNumber = 24,
                name = "Drip System 24",
                grossAreaHa = 110.0,
                irrigatedAreaHa = 101.5,
                latitude = 40.421494,
                longitude = 68.344360,
                activeShift = 1,
                targetDischargeLps = 126.0,
                actualDischargeLps = 118.5
            ),
            IrrigationSystemEntity(
                id = "SYS-25",
                pumpHouseId = "PH-8",
                zoneName = "Zone 4",
                systemNumber = 25,
                name = "Drip System 25",
                grossAreaHa = 120.0,
                irrigatedAreaHa = 110.2,
                latitude = 40.419718,
                longitude = 68.353501,
                activeShift = 1,
                targetDischargeLps = 138.0,
                actualDischargeLps = 129.4
            ),
            IrrigationSystemEntity(
                id = "SYS-26",
                pumpHouseId = "PH-9",
                zoneName = "Zone 4",
                systemNumber = 26,
                name = "Drip System 26",
                grossAreaHa = 108.0,
                irrigatedAreaHa = 99.4,
                latitude = 40.417640,
                longitude = 68.362336,
                activeShift = 1,
                targetDischargeLps = 124.0,
                actualDischargeLps = 116.8
            ),
            IrrigationSystemEntity(
                id = "SYS-27",
                pumpHouseId = "PH-10",
                zoneName = "Zone 4",
                systemNumber = 27,
                name = "Drip System 27",
                grossAreaHa = 126.0,
                irrigatedAreaHa = 115.8,
                latitude = 40.418189,
                longitude = 68.361789,
                activeShift = 1,
                targetDischargeLps = 144.5,
                actualDischargeLps = 136.2
            ),
            IrrigationSystemEntity(
                id = "SYS-28",
                pumpHouseId = "PH-11",
                zoneName = "Zone 4",
                systemNumber = 28,
                name = "Drip System 28",
                grossAreaHa = 129.0,
                irrigatedAreaHa = 119.0,
                latitude = 40.416222,
                longitude = 68.371534,
                activeShift = 1,
                targetDischargeLps = 148.0,
                actualDischargeLps = 141.0
            ),

            // === Zone 2: Systems 29, 30, 31, 41, 42, 43 ===
            IrrigationSystemEntity(
                id = "SYS-29",
                pumpHouseId = "PH-12",
                zoneName = "Zone 2",
                systemNumber = 29,
                name = "Drip System 29",
                grossAreaHa = 89.0,
                irrigatedAreaHa = 81.8,
                latitude = 40.430834,
                longitude = 68.283354,
                activeShift = 1,
                targetDischargeLps = 102.0,
                actualDischargeLps = 96.4
            ),
            IrrigationSystemEntity(
                id = "SYS-30",
                pumpHouseId = "PH-13",
                zoneName = "Zone 2",
                systemNumber = 30,
                name = "Drip System 30",
                grossAreaHa = 129.0,
                irrigatedAreaHa = 118.5,
                latitude = 40.427553,
                longitude = 68.290054,
                activeShift = 1,
                targetDischargeLps = 148.0,
                actualDischargeLps = 139.8
            ),
            IrrigationSystemEntity(
                id = "SYS-31",
                pumpHouseId = "PH-14",
                zoneName = "Zone 2",
                systemNumber = 31,
                name = "Drip System 31",
                grossAreaHa = 133.0,
                irrigatedAreaHa = 122.4,
                latitude = 40.423337,
                longitude = 68.297305,
                activeShift = 1,
                targetDischargeLps = 152.0,
                actualDischargeLps = 144.1
            ),
            IrrigationSystemEntity(
                id = "SYS-41",
                pumpHouseId = "PH-18",
                zoneName = "Zone 2",
                systemNumber = 41,
                name = "Drip System 41",
                grossAreaHa = 76.5,
                irrigatedAreaHa = 70.4,
                latitude = 40.426500,
                longitude = 68.324500,
                activeShift = 1,
                targetDischargeLps = 88.0,
                actualDischargeLps = 82.5
            ),
            IrrigationSystemEntity(
                id = "SYS-42",
                pumpHouseId = "PH-18",
                zoneName = "Zone 2",
                systemNumber = 42,
                name = "Drip System 42",
                grossAreaHa = 62.5,
                irrigatedAreaHa = 57.5,
                latitude = 40.423500,
                longitude = 68.318500,
                activeShift = 2,
                targetDischargeLps = 72.0,
                actualDischargeLps = 67.2
            ),
            IrrigationSystemEntity(
                id = "SYS-43",
                pumpHouseId = "PH-18",
                zoneName = "Zone 2",
                systemNumber = 43,
                name = "Drip System 43",
                grossAreaHa = 47.0,
                irrigatedAreaHa = 43.2,
                latitude = 40.418500,
                longitude = 68.314500,
                activeShift = 1,
                targetDischargeLps = 54.0,
                actualDischargeLps = 50.4
            ),

            // === Zone 3: Systems 32 to 40 ===
            IrrigationSystemEntity(
                id = "SYS-32",
                pumpHouseId = "PH-15",
                zoneName = "Zone 3",
                systemNumber = 32,
                name = "Drip System 32",
                grossAreaHa = 48.3,
                irrigatedAreaHa = 44.5,
                latitude = 40.426506,
                longitude = 68.291219,
                activeShift = 1,
                targetDischargeLps = 55.5,
                actualDischargeLps = 51.75
            ),
            IrrigationSystemEntity(
                id = "SYS-33",
                pumpHouseId = "PH-15",
                zoneName = "Zone 3",
                systemNumber = 33,
                name = "Drip System 33",
                grossAreaHa = 58.5,
                irrigatedAreaHa = 53.8,
                latitude = 40.424800,
                longitude = 68.287500,
                activeShift = 2,
                targetDischargeLps = 67.2,
                actualDischargeLps = 62.8
            ),
            IrrigationSystemEntity(
                id = "SYS-34",
                pumpHouseId = "PH-15",
                zoneName = "Zone 3",
                systemNumber = 34,
                name = "Drip System 34",
                grossAreaHa = 57.9,
                irrigatedAreaHa = 53.2,
                latitude = 40.421500,
                longitude = 68.283500,
                activeShift = 1,
                targetDischargeLps = 66.5,
                actualDischargeLps = 61.9
            ),
            IrrigationSystemEntity(
                id = "SYS-35",
                pumpHouseId = "PH-16",
                zoneName = "Zone 3",
                systemNumber = 35,
                name = "Drip System 35",
                grossAreaHa = 46.8,
                irrigatedAreaHa = 43.0,
                latitude = 40.421500,
                longitude = 68.300500,
                activeShift = 1,
                targetDischargeLps = 53.8,
                actualDischargeLps = 49.5
            ),
            IrrigationSystemEntity(
                id = "SYS-36",
                pumpHouseId = "PH-16",
                zoneName = "Zone 3",
                systemNumber = 36,
                name = "Drip System 36",
                grossAreaHa = 54.0,
                irrigatedAreaHa = 49.6,
                latitude = 40.418500,
                longitude = 68.296500,
                activeShift = 1,
                targetDischargeLps = 62.0,
                actualDischargeLps = 57.4
            ),
            IrrigationSystemEntity(
                id = "SYS-37",
                pumpHouseId = "PH-16",
                zoneName = "Zone 3",
                systemNumber = 37,
                name = "Drip System 37",
                grossAreaHa = 58.5,
                irrigatedAreaHa = 53.8,
                latitude = 40.414500,
                longitude = 68.293500,
                activeShift = 3,
                targetDischargeLps = 67.2,
                actualDischargeLps = 62.5
            ),
            IrrigationSystemEntity(
                id = "SYS-38",
                pumpHouseId = "PH-17",
                zoneName = "Zone 3",
                systemNumber = 38,
                name = "Drip System 38",
                grossAreaHa = 48.0,
                irrigatedAreaHa = 44.1,
                latitude = 40.416563,
                longitude = 68.309460,
                activeShift = 1,
                targetDischargeLps = 55.2,
                actualDischargeLps = 51.0
            ),
            IrrigationSystemEntity(
                id = "SYS-39",
                pumpHouseId = "PH-17",
                zoneName = "Zone 3",
                systemNumber = 39,
                name = "Drip System 39",
                grossAreaHa = 63.3,
                irrigatedAreaHa = 58.2,
                latitude = 40.411500,
                longitude = 68.304500,
                activeShift = 1,
                targetDischargeLps = 72.8,
                actualDischargeLps = 68.0
            ),
            IrrigationSystemEntity(
                id = "SYS-40",
                pumpHouseId = "PH-17",
                zoneName = "Zone 3",
                systemNumber = 40,
                name = "Drip System 40",
                grossAreaHa = 54.0,
                irrigatedAreaHa = 49.6,
                latitude = 40.406500,
                longitude = 68.299500,
                activeShift = 1,
                targetDischargeLps = 62.0,
                actualDischargeLps = 58.2
            )
        )
    }

    fun getInitialValves(): List<SubmainValveEntity> {
        val valves = mutableListOf<SubmainValveEntity>()
        val systems = getInitialSystems()

        // Generate exact engineering submain valves matching the Jain Irrigation project blueprints
        // Total = 714 valves positioned directly at pipeline manifold takeoffs
        systems.forEach { system ->
            val spec = KmlGeoData.SYSTEM_SPECS[system.id]
            val totalValves = spec?.totalValves ?: 12
            val startNum = spec?.startValveNumber ?: 1
            val endNum = spec?.endValveNumber ?: totalValves

            val areaPerValve = (system.grossAreaHa / totalValves).coerceIn(1.2, 5.0)
            val baseFlow = (system.targetDischargeLps / 2.0).coerceIn(18.0, 38.0)

            for (vNum in startNum..endNum) {
                val localIndex = vNum - startNum + 1
                val shift = ((localIndex - 1) % 12) + 1
                val isActive = shift == system.activeShift
                val tag = "P${system.systemNumber}-$vNum"

                // Position valve coordinates exactly along surveyed ground truth or KML delivery pipeline corridor
                val valveCoord = KmlGeoData.SURVEYED_VALVE_COORDINATES[tag]
                    ?: if (tag == "P29-1") {
                        GeoPoint(40.446028, 68.285828) // 40°26'45.70"N 68°17'08.98"E
                    } else {
                        KmlGeoData.getValveCoordinates(system.id, vNum)
                    }
                val valveLat = valveCoord.latitude
                val valveLng = valveCoord.longitude

                val lengthM = spec?.submainLengthMeters ?: (90.0 + ((localIndex % 10) * 3.0))
                val isAlert = vNum in listOf(2, 42, 98, 146, 220, 255)
                val alertMsg = if (isAlert) "High Filter Differential Pressure warning" else null

                val flow = baseFlow + ((localIndex % 5) * 0.35)
                val actualFlow = if (isActive) flow else 0.0
                val pressure = if (isActive) 1.82f + ((localIndex % 3) * 0.05f) else 0.05f
                val soilM20 = 24.0f + ((localIndex % 6) * 1.5f)
                val soilM40 = soilM20 + 4.5f

                val valveState = when {
                    isAlert -> ValveState.FAULT_ALERT
                    isActive -> ValveState.OPEN_IRRIGATING
                    shift == (system.activeShift % 12) + 1 -> ValveState.SCHEDULED
                    else -> ValveState.CLOSED
                }

                valves.add(
                    SubmainValveEntity(
                        tag = tag,
                        systemId = system.id,
                        pumpHouseId = system.pumpHouseId,
                        zoneName = system.zoneName,
                        submainNumber = vNum,
                        shiftNumber = shift,
                        areaHa = Math.round(areaPerValve * 10.0) / 10.0,
                        designFlowLps = Math.round(flow * 100.0) / 100.0,
                        actualFlowLps = Math.round(actualFlow * 100.0) / 100.0,
                        submainLengthM = lengthM,
                        pipeDiameterMm = spec?.pipeDiameterMm ?: (if (lengthM > 120) 160 else 110),
                        isControlValveOpen = isActive,
                        isFlushValveOpen = false,
                        valveState = valveState,
                        linePressureBar = pressure,
                        soilMoisture20cmPct = soilM20,
                        soilMoisture40cmPct = soilM40,
                        hasAlert = isAlert,
                        alertMessage = alertMsg,
                        latitude = valveLat,
                        longitude = valveLng
                    )
                )
            }
        }

        return valves
    }

    fun getInitialScheduleConfigs(): List<ScheduleConfigEntity> {
        val configs = mutableListOf<ScheduleConfigEntity>()
        getInitialSystems().forEach { system ->
            configs.add(
                ScheduleConfigEntity(
                    systemId = system.id,
                    autoAdvanceEnabled = true,
                    currentShift = system.activeShift
                )
            )
        }
        return configs
    }
}

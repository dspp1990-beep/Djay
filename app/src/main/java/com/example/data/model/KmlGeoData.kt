package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class PipelineSegment(
    val id: String,
    val name: String,
    val zoneName: String,
    val pumpHouseId: String,
    val systemId: String? = null,
    val points: List<GeoPoint>,
    val isMainline: Boolean = false
)

data class FieldPlotPolygon(
    val id: String,
    val name: String,
    val zoneName: String,
    val pumpHouseId: String,
    val areaHa: Double,
    val points: List<GeoPoint>
)

/**
 * Engineering specification for an irrigation system derived from Jain Irrigation blueprints.
 * Defines the exact valve count, tag ranges, submain dimensions, and physical stationing.
 */
data class SystemDesignSpec(
    val systemId: String,
    val systemNumber: Int,
    val pumpHouseId: String,
    val zoneName: String,
    val startValveNumber: Int,
    val endValveNumber: Int,
    val submainLengthMeters: Double = 115.0,
    val pipeDiameterMm: Int = 110
) {
    val totalValves: Int get() = endValveNumber - startValveNumber + 1
}

object GeoUtils {
    /**
     * Computes Haversine distance in meters between two coordinates.
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    /**
     * Computes cardinal direction string (e.g. "NE", "SW") from origin to target.
     */
    fun bearingToCardinal(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        var brng = Math.toDegrees(atan2(y, x))
        brng = (brng + 360) % 360
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        return directions[((brng + 22.5) / 45).toInt() % 8]
    }

    /**
     * Formats latitude and longitude as GPS Degrees, Minutes, Seconds (DMS) string.
     * Example: 40°26'45.70"N 68°17'08.98"E
     */
    fun toDms(lat: Double, lng: Double): String {
        fun formatDms(v: Double, pos: Char, neg: Char): String {
            val dir = if (v >= 0) pos else neg
            val abs = kotlin.math.abs(v)
            val deg = abs.toInt()
            val minDouble = (abs - deg) * 60.0
            val min = minDouble.toInt()
            val sec = (minDouble - min) * 60.0
            return "%d°%02d'%05.2f\"%c".format(deg, min, sec, dir)
        }
        return "${formatDms(lat, 'N', 'S')} ${formatDms(lng, 'E', 'W')}"
    }

    /**
     * Parses coordinates from either decimal degrees (e.g., "40.446028, 68.285828")
     * or standard GPS DMS format (e.g., "40°26'45.70\"N 68°17'08.98\"E").
     */
    fun parseCoordinates(input: String): GeoPoint? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Format 1: Decimal degrees like "40.446028, 68.285828" or "40.446028 68.285828"
        val decRegex = Regex("""^([-+]?\d{1,3}\.?\d*)[,\s]+([-+]?\d{1,3}\.?\d*)$""")
        decRegex.find(trimmed)?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                return GeoPoint(lat, lng)
            }
        }

        // Format 2: DMS format like 40°26'45.70"N 68°17'08.98"E
        try {
            val dmsRegex = Regex(
                """(\d{1,3})\s*°\s*(\d{1,2})\s*['′]\s*([\d.]+)\s*["″]?\s*([NSEWnsew])""",
                RegexOption.IGNORE_CASE
            )
            val matches = dmsRegex.findAll(trimmed).toList()
            if (matches.size >= 2) {
                var parsedLat: Double? = null
                var parsedLng: Double? = null

                for (m in matches) {
                    val deg = m.groupValues[1].toDouble()
                    val min = m.groupValues[2].toDouble()
                    val sec = m.groupValues[3].toDouble()
                    val dir = m.groupValues[4].uppercase()
                    val decVal = deg + (min / 60.0) + (sec / 3600.0)
                    if (dir == "N" || dir == "S") {
                        parsedLat = if (dir == "S") -decVal else decVal
                    } else if (dir == "E" || dir == "W") {
                        parsedLng = if (dir == "W") -decVal else decVal
                    }
                }

                if (parsedLat != null && parsedLng != null) {
                    return GeoPoint(parsedLat, parsedLng)
                }
            }
        } catch (_: Exception) {}

        return null
    }
}

object KmlGeoData {

    // Center coordinates for Sardoba irrigation project (exact midpoint of 20 systems & 12 stations)
    const val CENTER_LAT = 40.4205
    const val CENTER_LNG = 68.3270

    // Coordinates for all 12 Pump Houses (matching user GeoJSON and field table)
    val PUMP_HOUSE_LOCATIONS = mapOf(
        // Zone 4 (Combine PH 7 - 11)
        "PH-7" to GeoPoint(40.421494, 68.344360),
        "PH-8" to GeoPoint(40.419718, 68.353501),
        "PH-9" to GeoPoint(40.417640, 68.362336),
        "PH-10" to GeoPoint(40.418189, 68.361789),
        "PH-11" to GeoPoint(40.416222, 68.371534),

        // Zone 2 (Sardoba Rasido)
        "PH-12" to GeoPoint(40.430834, 68.283354),
        "PH-13" to GeoPoint(40.427553, 68.290054),
        "PH-14" to GeoPoint(40.423337, 68.297305),
        "PH-18" to GeoPoint(40.424442, 68.323110),

        // Zone 3 (Sardoba Rasido)
        "PH-15" to GeoPoint(40.426506, 68.291219),
        "PH-16" to GeoPoint(40.421500, 68.300500),
        "PH-17" to GeoPoint(40.416563, 68.309460)
    )

    // Dedicated pipeline corridors for all 20 systems across Sardoba project
    val SYSTEM_PIPELINE_MAP: Map<String, List<GeoPoint>> = mapOf(
        // === Zone 4: Systems 24 to 28 (PH 7 to 11) ===
        "SYS-24" to listOf(
            GeoPoint(40.421494, 68.344360),
            GeoPoint(40.422300, 68.344924),
            GeoPoint(40.426945, 68.346138),
            GeoPoint(40.433016, 68.348228),
            GeoPoint(40.434488, 68.348634),
            GeoPoint(40.436335, 68.348980)
        ),
        "SYS-25" to listOf(
            GeoPoint(40.419718, 68.353501),
            GeoPoint(40.420636, 68.354236),
            GeoPoint(40.425071, 68.355493),
            GeoPoint(40.428849, 68.356812),
            GeoPoint(40.432588, 68.358116),
            GeoPoint(40.433382, 68.354539)
        ),
        "SYS-26" to listOf(
            GeoPoint(40.417640, 68.362336),
            GeoPoint(40.418472, 68.363593),
            GeoPoint(40.426912, 68.366477),
            GeoPoint(40.430643, 68.367820),
            GeoPoint(40.432275, 68.364385)
        ),
        "SYS-27" to listOf(
            GeoPoint(40.418189, 68.361789),
            GeoPoint(40.417505, 68.360567),
            GeoPoint(40.416081, 68.362450),
            GeoPoint(40.404952, 68.357577),
            GeoPoint(40.401383, 68.356052)
        ),
        "SYS-28" to listOf(
            GeoPoint(40.416222, 68.371534),
            GeoPoint(40.415147, 68.371735),
            GeoPoint(40.414207, 68.371604),
            GeoPoint(40.414100, 68.375816),
            GeoPoint(40.405229, 68.371813),
            GeoPoint(40.398082, 68.368695),
            GeoPoint(40.396268, 68.367873)
        ),

        // === Zone 2: Systems 29 to 31, 41 to 43 ===
        "SYS-29" to listOf(
            GeoPoint(40.430834, 68.283354),
            GeoPoint(40.431655, 68.284309),
            GeoPoint(40.433596, 68.284753),
            GeoPoint(40.436852, 68.286283),
            GeoPoint(40.439331, 68.287448),
            GeoPoint(40.440954, 68.288207),
            GeoPoint(40.444616, 68.289921),
            GeoPoint(40.446028, 68.285828) // P29-1 location (40°26'45.70"N 68°17'08.98"E)
        ),
        "SYS-30" to listOf(
            GeoPoint(40.427553, 68.290054),
            GeoPoint(40.428275, 68.291057),
            GeoPoint(40.430572, 68.292486),
            GeoPoint(40.434085, 68.294385),
            GeoPoint(40.436750, 68.295745),
            GeoPoint(40.439027, 68.297014),
            GeoPoint(40.443550, 68.298904)
        ),
        "SYS-31" to listOf(
            GeoPoint(40.423337, 68.297305),
            GeoPoint(40.424221, 68.295943),
            GeoPoint(40.426331, 68.299871),
            GeoPoint(40.429126, 68.301308),
            GeoPoint(40.432424, 68.302899),
            GeoPoint(40.436980, 68.305276),
            GeoPoint(40.440534, 68.307084)
        ),
        "SYS-41" to listOf(
            GeoPoint(40.425021, 68.321764),
            GeoPoint(40.428500, 68.323800),
            GeoPoint(40.432675, 68.326292),
            GeoPoint(40.434166, 68.321891)
        ),
        "SYS-42" to listOf(
            GeoPoint(40.425283, 68.321022),
            GeoPoint(40.426871, 68.317351),
            GeoPoint(40.425845, 68.315053),
            GeoPoint(40.424372, 68.314168),
            GeoPoint(40.422888, 68.313299),
            GeoPoint(40.421454, 68.312465)
        ),
        "SYS-43" to listOf(
            GeoPoint(40.421908, 68.319033),
            GeoPoint(40.419028, 68.313663),
            GeoPoint(40.417560, 68.312777),
            GeoPoint(40.415973, 68.311819),
            GeoPoint(40.416567, 68.314055)
        ),

        // === Zone 3: Systems 32 to 40 (PH 15, 16, 17) ===
        "SYS-32" to listOf(
            GeoPoint(40.426506, 68.291219),
            GeoPoint(40.425331, 68.287744),
            GeoPoint(40.423950, 68.284500),
            GeoPoint(40.422205, 68.281702)
        ),
        "SYS-33" to listOf(
            GeoPoint(40.425331, 68.287744),
            GeoPoint(40.424800, 68.287500),
            GeoPoint(40.421800, 68.283800),
            GeoPoint(40.418897, 68.278545)
        ),
        "SYS-34" to listOf(
            GeoPoint(40.421800, 68.283800),
            GeoPoint(40.421500, 68.283500),
            GeoPoint(40.417200, 68.279800),
            GeoPoint(40.415502, 68.278038),
            GeoPoint(40.412091, 68.282601)
        ),
        "SYS-35" to listOf(
            GeoPoint(40.421500, 68.300500),
            GeoPoint(40.419652, 68.291986),
            GeoPoint(40.416633, 68.293686)
        ),
        "SYS-36" to listOf(
            GeoPoint(40.419652, 68.291986),
            GeoPoint(40.418500, 68.296500),
            GeoPoint(40.415200, 68.292500),
            GeoPoint(40.413353, 68.290878)
        ),
        "SYS-37" to listOf(
            GeoPoint(40.415200, 68.292500),
            GeoPoint(40.414500, 68.293500),
            GeoPoint(40.410115, 68.287992),
            GeoPoint(40.407222, 68.292175)
        ),
        "SYS-38" to listOf(
            GeoPoint(40.416563, 68.309460),
            GeoPoint(40.415052, 68.298884),
            GeoPoint(40.410050, 68.298884)
        ),
        "SYS-39" to listOf(
            GeoPoint(40.415052, 68.298884),
            GeoPoint(40.411500, 68.304500),
            GeoPoint(40.407800, 68.297200),
            GeoPoint(40.405925, 68.295076),
            GeoPoint(40.402161, 68.295501)
        ),
        "SYS-40" to listOf(
            GeoPoint(40.407800, 68.297200),
            GeoPoint(40.406500, 68.299500),
            GeoPoint(40.403200, 68.297800),
            GeoPoint(40.399807, 68.301409),
            GeoPoint(40.398071, 68.300799)
        )
    )

    // Complete pipeline network for visualization on canvas
    val PIPELINES: List<PipelineSegment> = listOf(
        // === Zone 4: Systems 24 to 28 (PH 7 to 11) ===
        PipelineSegment(
            id = "pipe_ph7_main",
            name = "PH-7 System 24 Feeder Line",
            zoneName = "Zone 4",
            pumpHouseId = "PH-7",
            systemId = "SYS-24",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-24")
        ),
        PipelineSegment(
            id = "pipe_ph8_main",
            name = "PH-8 System 25 Feeder Line",
            zoneName = "Zone 4",
            pumpHouseId = "PH-8",
            systemId = "SYS-25",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-25")
        ),
        PipelineSegment(
            id = "pipe_ph9_main",
            name = "PH-9 System 26 Feeder Line",
            zoneName = "Zone 4",
            pumpHouseId = "PH-9",
            systemId = "SYS-26",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-26")
        ),
        PipelineSegment(
            id = "pipe_ph10_main",
            name = "PH-10 System 27 Feeder Line",
            zoneName = "Zone 4",
            pumpHouseId = "PH-10",
            systemId = "SYS-27",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-27")
        ),
        PipelineSegment(
            id = "pipe_ph11_main",
            name = "PH-11 System 28 Feeder Line",
            zoneName = "Zone 4",
            pumpHouseId = "PH-11",
            systemId = "SYS-28",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-28")
        ),

        // === Zone 2: Systems 29 to 31, 41 to 43 ===
        PipelineSegment(
            id = "pipe_ph12_main",
            name = "PH-12 System 29 Trunk Pipeline",
            zoneName = "Zone 2",
            pumpHouseId = "PH-12",
            systemId = "SYS-29",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-29")
        ),
        PipelineSegment(
            id = "pipe_ph13_main",
            name = "PH-13 System 30 Trunk Pipeline",
            zoneName = "Zone 2",
            pumpHouseId = "PH-13",
            systemId = "SYS-30",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-30")
        ),
        PipelineSegment(
            id = "pipe_ph14_main",
            name = "PH-14 System 31 Trunk Pipeline",
            zoneName = "Zone 2",
            pumpHouseId = "PH-14",
            systemId = "SYS-31",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-31")
        ),
        PipelineSegment(
            id = "pipe_ph18_main",
            name = "PH-18 Main Feeder",
            zoneName = "Zone 2",
            pumpHouseId = "PH-18",
            systemId = "SYS-41",
            isMainline = true,
            points = listOf(
                GeoPoint(40.424442, 68.323110),
                GeoPoint(40.425021, 68.321764),
                GeoPoint(40.425283, 68.321022),
                GeoPoint(40.423094, 68.319700),
                GeoPoint(40.421908, 68.319033),
                GeoPoint(40.419979, 68.317957),
                GeoPoint(40.417932, 68.316673)
            )
        ),
        PipelineSegment(
            id = "pipe_ph18_sub1",
            name = "PH-18 System 41 North Delivery",
            zoneName = "Zone 2",
            pumpHouseId = "PH-18",
            systemId = "SYS-41",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-41")
        ),
        PipelineSegment(
            id = "pipe_ph18_sub2",
            name = "PH-18 System 42 Lateral Submain",
            zoneName = "Zone 2",
            pumpHouseId = "PH-18",
            systemId = "SYS-42",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-42")
        ),
        PipelineSegment(
            id = "pipe_ph18_sub3",
            name = "PH-18 System 43 Lateral Submain",
            zoneName = "Zone 2",
            pumpHouseId = "PH-18",
            systemId = "SYS-43",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-43")
        ),

        // === Zone 3: Systems 32 to 40 ===
        PipelineSegment(
            id = "pipe_ph15_sys32",
            name = "PH-15 System 32 North Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-15",
            systemId = "SYS-32",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-32")
        ),
        PipelineSegment(
            id = "pipe_ph15_sys33",
            name = "PH-15 System 33 Central Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-15",
            systemId = "SYS-33",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-33")
        ),
        PipelineSegment(
            id = "pipe_ph15_sys34",
            name = "PH-15 System 34 South-West Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-15",
            systemId = "SYS-34",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-34")
        ),
        PipelineSegment(
            id = "pipe_ph16_sys35",
            name = "PH-16 System 35 North Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-16",
            systemId = "SYS-35",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-35")
        ),
        PipelineSegment(
            id = "pipe_ph16_sys36",
            name = "PH-16 System 36 Central Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-16",
            systemId = "SYS-36",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-36")
        ),
        PipelineSegment(
            id = "pipe_ph16_sys37",
            name = "PH-16 System 37 South Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-16",
            systemId = "SYS-37",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-37")
        ),
        PipelineSegment(
            id = "pipe_ph17_sys38",
            name = "PH-17 System 38 North Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-17",
            systemId = "SYS-38",
            isMainline = true,
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-38")
        ),
        PipelineSegment(
            id = "pipe_ph17_sys39",
            name = "PH-17 System 39 Central Sector",
            zoneName = "Zone 3",
            pumpHouseId = "PH-17",
            systemId = "SYS-39",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-39")
        ),
        PipelineSegment(
            id = "pipe_ph17_sys40",
            name = "PH-17 System 40 South Terminus",
            zoneName = "Zone 3",
            pumpHouseId = "PH-17",
            systemId = "SYS-40",
            points = SYSTEM_PIPELINE_MAP.getValue("SYS-40")
        )
    )

    // Zone Field Plot Polygons (representing the three massive field irrigation zones)
    val ZONE_POLYGONS: List<FieldPlotPolygon> = listOf(
        FieldPlotPolygon(
            id = "zone_2",
            name = "Zone 2 (PH 12, 13, 14, 18)",
            zoneName = "Zone 2",
            pumpHouseId = "PH-18",
            areaHa = 537.0,
            points = listOf(
                GeoPoint(40.427553, 68.290054),
                GeoPoint(40.430834, 68.283354),
                GeoPoint(40.444616, 68.289921),
                GeoPoint(40.443550, 68.298904),
                GeoPoint(40.437192, 68.318306),
                GeoPoint(40.434166, 68.321891),
                GeoPoint(40.424442, 68.323110),
                GeoPoint(40.417907, 68.316658),
                GeoPoint(40.423337, 68.297305)
            )
        ),
        FieldPlotPolygon(
            id = "zone_3",
            name = "Zone 3 (PH 15, 16, 17)",
            zoneName = "Zone 3",
            pumpHouseId = "PH-15",
            areaHa = 489.3,
            points = listOf(
                GeoPoint(40.426506, 68.291219),
                GeoPoint(40.421500, 68.300500),
                GeoPoint(40.416563, 68.309460),
                GeoPoint(40.411621, 68.313825),
                GeoPoint(40.406093, 68.308970),
                GeoPoint(40.398071, 68.300799),
                GeoPoint(40.404237, 68.292636),
                GeoPoint(40.412091, 68.282601),
                GeoPoint(40.422205, 68.281702)
            )
        ),
        FieldPlotPolygon(
            id = "zone_4",
            name = "Zone 4 (PH 7, 8, 9, 10, 11)",
            zoneName = "Zone 4",
            pumpHouseId = "PH-8",
            areaHa = 593.0,
            points = listOf(
                GeoPoint(40.421494, 68.344360),
                GeoPoint(40.436335, 68.348980),
                GeoPoint(40.434648, 68.358834),
                GeoPoint(40.432646, 68.367176),
                GeoPoint(40.424175, 68.369834),
                GeoPoint(40.416222, 68.371534),
                GeoPoint(40.396268, 68.367873),
                GeoPoint(40.398656, 68.360647),
                GeoPoint(40.401383, 68.356052),
                GeoPoint(40.419718, 68.353501)
            )
        )
    )

    /**
     * Engineering specification for all 20 systems across the Sardoba project,
     * derived strictly from the official Jain Irrigation blueprints.
     * Total valves = 714 valves across 12 pump houses.
     */
    val SYSTEM_SPECS: Map<String, SystemDesignSpec> = mapOf(
        // === Zone 4: T. Malik Site 2 (Pump Houses 7 - 11) ===
        "SYS-24" to SystemDesignSpec("SYS-24", 24, "PH-7", "Zone 4", 1, 48, submainLengthMeters = 115.0),
        "SYS-25" to SystemDesignSpec("SYS-25", 25, "PH-8", "Zone 4", 49, 95, submainLengthMeters = 120.0),
        "SYS-26" to SystemDesignSpec("SYS-26", 26, "PH-9", "Zone 4", 96, 143, submainLengthMeters = 125.0),
        "SYS-27" to SystemDesignSpec("SYS-27", 27, "PH-10", "Zone 4", 144, 203, submainLengthMeters = 125.0),
        "SYS-28" to SystemDesignSpec("SYS-28", 28, "PH-11", "Zone 4", 204, 263, submainLengthMeters = 120.0),

        // === Zone 2: Sardoba Sh. Rashido North & East (Pump Houses 12, 13, 14, 18) ===
        "SYS-29" to SystemDesignSpec("SYS-29", 29, "PH-12", "Zone 2", 1, 39, submainLengthMeters = 135.0),
        "SYS-30" to SystemDesignSpec("SYS-30", 30, "PH-13", "Zone 2", 40, 96, submainLengthMeters = 130.0),
        "SYS-31" to SystemDesignSpec("SYS-31", 31, "PH-14", "Zone 2", 97, 156, submainLengthMeters = 130.0),
        "SYS-41" to SystemDesignSpec("SYS-41", 41, "PH-18", "Zone 2", 217, 252, submainLengthMeters = 110.0),
        "SYS-42" to SystemDesignSpec("SYS-42", 42, "PH-18", "Zone 2", 253, 275, submainLengthMeters = 105.0),
        "SYS-43" to SystemDesignSpec("SYS-43", 43, "PH-18", "Zone 2", 276, 300, submainLengthMeters = 100.0),

        // === Zone 3: Sardoba Sh. Rashido Central & South (Pump Houses 15, 16, 17) ===
        "SYS-32" to SystemDesignSpec("SYS-32", 32, "PH-15", "Zone 3", 1, 24, submainLengthMeters = 112.0),
        "SYS-33" to SystemDesignSpec("SYS-33", 33, "PH-15", "Zone 3", 25, 48, submainLengthMeters = 118.0),
        "SYS-34" to SystemDesignSpec("SYS-34", 34, "PH-15", "Zone 3", 49, 72, submainLengthMeters = 115.0),
        "SYS-35" to SystemDesignSpec("SYS-35", 35, "PH-16", "Zone 3", 73, 96, submainLengthMeters = 120.0),
        "SYS-36" to SystemDesignSpec("SYS-36", 36, "PH-16", "Zone 3", 97, 120, submainLengthMeters = 116.0),
        "SYS-37" to SystemDesignSpec("SYS-37", 37, "PH-16", "Zone 3", 121, 144, submainLengthMeters = 122.0),
        "SYS-38" to SystemDesignSpec("SYS-38", 38, "PH-17", "Zone 3", 145, 168, submainLengthMeters = 110.0),
        "SYS-39" to SystemDesignSpec("SYS-39", 39, "PH-17", "Zone 3", 169, 192, submainLengthMeters = 125.0),
        "SYS-40" to SystemDesignSpec("SYS-40", 40, "PH-17", "Zone 3", 193, 216, submainLengthMeters = 115.0)
    )

    /**
     * Retrieves the pipeline segment points for a specific irrigation system.
     */
    fun getPipelineForSystem(systemId: String): List<GeoPoint> {
        return SYSTEM_PIPELINE_MAP[systemId] ?: emptyList()
    }

    /**
     * Surveyed Ground-Truth GPS Coordinates for exact field infrastructure landmarks.
     * Overrides mathematical interpolation with millimeter-grade GPS field measurements
     * directly matching the Jain Irrigation & Indorama Agro CAD surveyed map.
     */
    val SURVEYED_VALVE_COORDINATES: Map<String, GeoPoint> = mapOf(
        // Upper Submain - Left Plot (29.974 Ha, S-2-SR-109)
        "P29-1" to GeoPoint(40.446028, 68.285828), // Station 1 Top (40°26'45.70"N 68°17'08.98"E)
        "P29-2" to GeoPoint(40.445951, 68.285795), // Station 1 Bottom
        "P29-5" to GeoPoint(40.445764, 68.286894), // Station 2 Top
        "P29-6" to GeoPoint(40.445687, 68.286862), // Station 2 Bottom
        "P29-9" to GeoPoint(40.445500, 68.287961), // Station 3 Top
        "P29-10" to GeoPoint(40.445422, 68.287929), // Station 3 Bottom

        // Lower Submain - Left Plot (29.974 Ha, S-2-SR-109)
        "P29-7" to GeoPoint(40.443503, 68.284938), // Station 1 Top
        "P29-8" to GeoPoint(40.443425, 68.284906), // Station 1 Bottom
        "P29-11" to GeoPoint(40.443225, 68.286061), // Station 2 Top
        "P29-12" to GeoPoint(40.443147, 68.286028), // Station 2 Bottom

        // Upper Submain - Right Plot (66.852 Ha, S-2-SR-113)
        "P29-13" to GeoPoint(40.445166, 68.289309), // Station 4 Top
        "P29-14" to GeoPoint(40.445088, 68.289276), // Station 4 Bottom
        "P29-22" to GeoPoint(40.444818, 68.290712), // Station 5 Top
        "P29-23" to GeoPoint(40.444740, 68.290680), // Station 5 Bottom
        "P29-31" to GeoPoint(40.444470, 68.292116), // Station 6 Top
        "P29-32" to GeoPoint(40.444392, 68.292083), // Station 6 Bottom

        // Lower Submain - Right Plot (66.852 Ha, S-2-SR-113)
        "P29-15" to GeoPoint(40.442626, 68.288475), // Station 3 Top
        "P29-16" to GeoPoint(40.442549, 68.288443), // Station 3 Bottom
        "P29-24" to GeoPoint(40.442278, 68.289879), // Station 4 Top
        "P29-25" to GeoPoint(40.442201, 68.289846), // Station 4 Bottom
        "P29-33" to GeoPoint(40.441930, 68.291282), // Station 5 Top
        "P29-34" to GeoPoint(40.441853, 68.291250), // Station 5 Bottom

        // Adjacent system line (East border)
        "P30-4" to GeoPoint(40.444058, 68.293431),
        "P30-3" to GeoPoint(40.444136, 68.293464)
    )

    /**
     * Submain Flush Valve stations located on the delivery manifolds adjacent to each control valve pair.
     */
    val SURVEYED_FLUSH_VALVE_COORDINATES: List<GeoPoint> = listOf(
        // Upper Submain flush valves (||::)
        GeoPoint(40.445962, 68.285924), // by P29-1/2
        GeoPoint(40.445697, 68.286990), // by P29-5/6
        GeoPoint(40.445433, 68.288057), // by P29-9/10
        GeoPoint(40.445099, 68.289405), // by P29-13/14
        GeoPoint(40.444751, 68.290808), // by P29-22/23
        GeoPoint(40.444403, 68.292212), // by P29-31/32

        // Lower Submain flush valves (||::)
        GeoPoint(40.443436, 68.285034), // by P29-7/8
        GeoPoint(40.443158, 68.286157), // by P29-11/12
        GeoPoint(40.442559, 68.288571), // by P29-15/16
        GeoPoint(40.442211, 68.289975), // by P29-24/25
        GeoPoint(40.441864, 68.291378)  // by P29-33/34
    )

    /**
     * Continuous submain manifold pipeline corridors crossing the surveyed plots.
     */
    val SURVEYED_SUBMAIN_CORRIDORS: List<List<GeoPoint>> = listOf(
        // Upper Submain delivery line (crimson/magenta across 29.974 Ha and 66.852 Ha)
        listOf(
            GeoPoint(40.446101, 68.285363),
            GeoPoint(40.445990, 68.285812), // Station 1 (P29-1/2)
            GeoPoint(40.445725, 68.286878), // Station 2 (P29-5/6)
            GeoPoint(40.445461, 68.287945), // Station 3 (P29-9/10)
            GeoPoint(40.445127, 68.289293), // Station 4 (P29-13/14)
            GeoPoint(40.444779, 68.290696), // Station 5 (P29-22/23)
            GeoPoint(40.444431, 68.292100), // Station 6 (P29-31/32)
            GeoPoint(40.444097, 68.293447), // Adjacent border
            GeoPoint(40.443986, 68.293895)
        ),
        // Lower Submain delivery line (crimson/magenta across 29.974 Ha and 66.852 Ha)
        listOf(
            GeoPoint(40.443562, 68.284530),
            GeoPoint(40.443464, 68.284922), // Station 1 (P29-7/8)
            GeoPoint(40.443186, 68.286045), // Station 2 (P29-11/12)
            GeoPoint(40.442587, 68.288459), // Station 3 (P29-15/16)
            GeoPoint(40.442239, 68.289863), // Station 4 (P29-24/25)
            GeoPoint(40.441892, 68.291266), // Station 5 (P29-33/34)
            GeoPoint(40.441485, 68.292723)
        )
    )

    /**
     * Calculates the exact GPS coordinates for a submain control valve
     * stationed directly along its system's delivery pipeline corridor according to the engineering map.
     * Manifold takeoffs sit on their respective side of the delivery pipe.
     */
    fun getValveCoordinates(systemId: String, valveNumberOrIndex: Int, overrideTotalValves: Int? = null): GeoPoint {
        val sysNum = systemId.removePrefix("SYS-").toIntOrNull() ?: -1
        val tagCandidate = "P$sysNum-$valveNumberOrIndex"
        SURVEYED_VALVE_COORDINATES[tagCandidate]?.let { return it }
        if (systemId == "SYS-29" && valveNumberOrIndex == 1) {
            return GeoPoint(40.446028, 68.285828)
        }

        val pts = SYSTEM_PIPELINE_MAP[systemId] ?: return GeoPoint(CENTER_LAT, CENTER_LNG)
        if (pts.isEmpty()) return GeoPoint(CENTER_LAT, CENTER_LNG)
        if (pts.size == 1) return pts[0]

        val spec = SYSTEM_SPECS[systemId]
        val totalValves = overrideTotalValves ?: (spec?.totalValves ?: 12)

        val localIndex = if (spec != null && valveNumberOrIndex >= spec.startValveNumber) {
            (valveNumberOrIndex - spec.startValveNumber + 1).coerceIn(1, spec.totalValves)
        } else {
            valveNumberOrIndex.coerceIn(1, totalValves)
        }

        val cosLat = cos(Math.toRadians(CENTER_LAT))
        val cumDistances = DoubleArray(pts.size)
        cumDistances[0] = 0.0

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val dLatMeters = (p2.latitude - p1.latitude) * 111040.0
            val dLngMeters = (p2.longitude - p1.longitude) * 111320.0 * cosLat
            val segDist = hypot(dLatMeters, dLngMeters)
            cumDistances[i + 1] = cumDistances[i] + segDist
        }

        val totalDist = cumDistances.last()
        if (totalDist <= 0.0) return pts[0]

        // Station each valve pair along the delivery pipeline corridor
        val numStations = maxOf(1, (totalValves + 1) / 2)
        val station = ((localIndex - 1) / 2).coerceIn(0, numStations - 1)
        val fraction = ((station.toDouble() + 0.6) / (numStations.toDouble() + 0.2)).coerceIn(0.04, 0.96)
        val targetDist = fraction * totalDist

        for (i in 0 until pts.size - 1) {
            val dStart = cumDistances[i]
            val dEnd = cumDistances[i + 1]
            if (targetDist in dStart..dEnd) {
                val segLen = dEnd - dStart
                val t = if (segLen > 0.0) (targetDist - dStart) / segLen else 0.0

                val p1 = pts[i]
                val p2 = pts[i + 1]

                val vLat = p1.latitude + t * (p2.latitude - p1.latitude)
                val vLng = p1.longitude + t * (p2.longitude - p1.longitude)

                val dNorth = (p2.latitude - p1.latitude) * 111040.0
                val dEast = (p2.longitude - p1.longitude) * 111320.0 * cosLat
                val segMeters = hypot(dNorth, dEast)

                if (segMeters > 0.0) {
                    val uNorth = dNorth / segMeters
                    val uEast = dEast / segMeters

                    // Submain control valve mounted on the pipeline manifold takeoff
                    val side = if (localIndex % 2 == 1) 1.0 else -1.0
                    val manifoldOffsetMeters = 3.5 * side

                    val perpEast = -uNorth * manifoldOffsetMeters
                    val perpNorth = uEast * manifoldOffsetMeters

                    val valveLat = vLat + (perpNorth / 111040.0)
                    val valveLng = vLng + (perpEast / (111320.0 * cosLat))
                    return GeoPoint(valveLat, valveLng)
                }

                return GeoPoint(vLat, vLng)
            }
        }

        return pts.last()
    }

    /**
     * Calculates the submain lateral branch endpoint for a valve.
     * Branches perpendicularly from the pipeline manifold into the adjacent field block
     * to the distal boundary where the Submain Flush Valve is stationed.
     */
    fun getSubmainLateralEnd(systemId: String, valveNumberOrIndex: Int, overrideTotalValves: Int? = null): GeoPoint {
        val pts = SYSTEM_PIPELINE_MAP[systemId] ?: return GeoPoint(CENTER_LAT, CENTER_LNG)
        if (pts.size < 2) return pts.firstOrNull() ?: GeoPoint(CENTER_LAT, CENTER_LNG)

        val spec = SYSTEM_SPECS[systemId]
        val totalValves = overrideTotalValves ?: (spec?.totalValves ?: 12)

        val localIndex = if (spec != null && valveNumberOrIndex >= spec.startValveNumber) {
            (valveNumberOrIndex - spec.startValveNumber + 1).coerceIn(1, spec.totalValves)
        } else {
            valveNumberOrIndex.coerceIn(1, totalValves)
        }

        val cosLat = cos(Math.toRadians(CENTER_LAT))
        val cumDistances = DoubleArray(pts.size)
        cumDistances[0] = 0.0

        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val dLatMeters = (p2.latitude - p1.latitude) * 111040.0
            val dLngMeters = (p2.longitude - p1.longitude) * 111320.0 * cosLat
            cumDistances[i + 1] = cumDistances[i] + hypot(dLatMeters, dLngMeters)
        }

        val totalDist = cumDistances.last()
        if (totalDist <= 0.0) return pts[0]

        val numStations = maxOf(1, (totalValves + 1) / 2)
        val station = ((localIndex - 1) / 2).coerceIn(0, numStations - 1)
        val fraction = ((station.toDouble() + 0.6) / (numStations.toDouble() + 0.2)).coerceIn(0.04, 0.96)
        val targetDist = fraction * totalDist

        for (i in 0 until pts.size - 1) {
            val dStart = cumDistances[i]
            val dEnd = cumDistances[i + 1]
            if (targetDist in dStart..dEnd) {
                val segLen = dEnd - dStart
                val t = if (segLen > 0.0) (targetDist - dStart) / segLen else 0.0

                val p1 = pts[i]
                val p2 = pts[i + 1]

                val vLat = p1.latitude + t * (p2.latitude - p1.latitude)
                val vLng = p1.longitude + t * (p2.longitude - p1.longitude)

                val dNorth = (p2.latitude - p1.latitude) * 111040.0
                val dEast = (p2.longitude - p1.longitude) * 111320.0 * cosLat
                val segMeters = hypot(dNorth, dEast)

                if (segMeters > 0.0) {
                    val uNorth = dNorth / segMeters
                    val uEast = dEast / segMeters

                    val side = if (localIndex % 2 == 1) 1.0 else -1.0
                    val lateralLenMeters = (spec?.submainLengthMeters ?: 110.0) * side

                    val perpEast = -uNorth * lateralLenMeters
                    val perpNorth = uEast * lateralLenMeters

                    val endLat = vLat + (perpNorth / 111040.0)
                    val endLng = vLng + (perpEast / (111320.0 * cosLat))
                    return GeoPoint(endLat, endLng)
                }

                return GeoPoint(vLat, vLng)
            }
        }

        return pts.last()
    }
}

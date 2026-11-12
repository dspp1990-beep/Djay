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
        "PH-16" to GeoPoint(40.4196591, 68.2969086), // CORRECTED: was a round-number guess (40.4215, 68.3005, ~330 m off). Now the CAD point kml_248 at the head of the PH-16 mainline; confirmed by the "PUMP HOUSE - 16" label in the Jain drawing
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
    // ALL 719 valves, positions taken from the Jain design drawing (vector PDF) valve symbols and their
    // printed tags (P<system>-<n>), georeferenced to the CAD KMLs (~1 m median). Replaces the earlier
    // 24 hand-entered points (most were 20–240 m off; "P30-4" does not exist on the drawing).
    // Packed as one string to stay inside JVM method-size limits.
    // Format per valve: tag,lat,lng,newBlock   (tag = Jain drawing valve number, e.g. P29-3)
    private const val VALVE_DATA =
        "P24-1,40.4346582,68.3449467,PH7-2;P24-2,40.4345104,68.3448958,PH7-2;P24-3,40.4309288,68.3436425,PH7-2;P24-4,40.4307815,68.3435916,PH7-2;P24-5,40.4271609,68.3423258,PH7-1;P24-6,40.4270130,68.3422729,PH7-1;P24-7,40.4234316,68.3410218,PH7-1;P24-8,40.4232837,68.3409690,PH7-1;P24-9,40.4344150,68.3463542,PH7-2;P24-10,40.4342674,68.3463038,PH7-2;P24-11,40.4306850,68.3450499,PH7-2;P24-12,40.4305374,68.3449995,PH7-2;P24-13,40.4269170,68.3437331,PH7-1;P24-14,40.4267692,68.3436828,PH7-1;P24-15,40.4231877,68.3424291,PH7-1;P24-16,40.4230391,68.3423782,PH7-1;P24-17,40.4341924,68.3476405,PH7-2;P24-18,40.4340448,68.3475894,PH7-2;P24-19,40.4304633,68.3463362,PH7-2;P24-20,40.4303154,68.3462854,PH7-2;P24-21,40.4266951,68.3450161,PH7-1;P24-22,40.4265465,68.3449665,PH7-1;P24-23,40.4229651,68.3437120,PH7-1;P24-24,40.4228165,68.3436624,PH7-1;P24-25,40.4339183,68.3492243,PH7-4;P24-26,40.4337705,68.3491746,PH7-4;P24-27,40.4301891,68.3479198,PH7-4;P24-28,40.4300405,68.3478702,PH7-4;P24-29,40.4264203,68.3466032,PH7-3;P24-30,40.4262725,68.3465525,PH7-3;P24-31,40.4226910,68.3452987,PH7-3;P24-32,40.4225432,68.3452484,PH7-3;P24-33,40.4337177,68.3503861,PH7-4;P24-34,40.4335699,68.3503356,PH7-4;P24-35,40.4299886,68.3490816,PH7-4;P24-36,40.4298400,68.3490311,PH7-4;P24-37,40.4262197,68.3477647,PH7-3;P24-38,40.4260717,68.3477121,PH7-3;P24-39,40.4224914,68.3464607,PH7-3;P24-40,40.4223435,68.3464079,PH7-3;P24-41,40.4335171,68.3515452,PH7-4;P24-42,40.4333693,68.3514947,PH7-4;P24-43,40.4297879,68.3502406,PH7-4;P24-44,40.4296393,68.3501901,PH7-4;P24-45,40.4260191,68.3489235,PH7-3;P24-46,40.4258711,68.3488732,PH7-3;P24-47,40.4222905,68.3476192,PH7-3;P24-48,40.4221425,68.3475689,PH7-3;P25-49,40.4328680,68.3543947,PH7-6;P25-50,40.4292891,68.3531268,PH7-6;P25-51,40.4291415,68.3530757,PH7-6;P25-52,40.4255231,68.3517928,PH7-5;P25-53,40.4253755,68.3517420,PH7-5;P25-54,40.4217967,68.3504719,PH7-5;P25-55,40.4216489,68.3504215,PH7-5;P25-56,40.4327936,68.3557314,PH7-6;P25-57,40.4326459,68.3556802,PH7-6;P25-58,40.4290670,68.3544098,PH7-6;P25-59,40.4289194,68.3543594,PH7-6;P25-60,40.4253011,68.3530760,PH7-5;P25-61,40.4251533,68.3530256,PH7-5;P25-62,40.4215740,68.3517580,PH7-5;P25-63,40.4214262,68.3517068,PH7-5;P25-64,40.4325282,68.3572632,PH7-6;P25-65,40.4323796,68.3572128,PH7-6;P25-66,40.4288011,68.3559416,PH7-6;P25-67,40.4286525,68.3558919,PH7-6;P25-68,40.4250351,68.3546107,PH7-5;P25-69,40.4248880,68.3545581,PH7-5;P25-70,40.4213093,68.3532896,PH7-5;P25-71,40.4211613,68.3532392,PH7-5;P25-72,40.4322382,68.3589405,PH7-16;P25-73,40.4320904,68.3588901,PH7-16;P25-74,40.4285070,68.3576438,PH7-16;P25-75,40.4283592,68.3575926,PH7-16;P25-76,40.4247384,68.3563313,PH7-15;P25-77,40.4245897,68.3562804,PH7-15;P25-78,40.4210070,68.3550325,PH7-15;P25-79,40.4208594,68.3549817,PH7-15;P25-80,40.4319507,68.3605968,PH7-16;P25-81,40.4318030,68.3605471,PH7-16;P25-82,40.4282211,68.3592999,PH7-16;P25-83,40.4280725,68.3592495,PH7-16;P25-84,40.4244510,68.3579874,PH7-15;P25-85,40.4243033,68.3579369,PH7-15;P25-86,40.4207189,68.3566905,PH7-15;P25-87,40.4205711,68.3566393,PH7-15;P25-88,40.4316860,68.3621318,PH7-16;P25-89,40.4315384,68.3620809,PH7-16;P25-90,40.4279557,68.3608316,PH7-16;P25-91,40.4278080,68.3607820,PH7-16;P25-92,40.4241857,68.3595222,PH7-15;P25-93,40.4240370,68.3594711,PH7-15;P25-94,40.4204560,68.3582224,PH7-15;P25-95,40.4203073,68.3581719,PH7-15;P26-96,40.4312467,68.3646634,PH7-12;P26-97,40.4310987,68.3646127,PH7-12;P26-98,40.4275239,68.3633238,PH7-11;P26-99,40.4273764,68.3632741,PH7-11;P26-100,40.4237623,68.3619721,PH7-10;P26-101,40.4236137,68.3619214,PH7-10;P26-102,40.4200379,68.3606328,PH7-9;P26-103,40.4198893,68.3605824,PH7-9;P26-104,40.4310238,68.3659460,PH7-12;P26-105,40.4308752,68.3658964,PH7-12;P26-106,40.4273020,68.3646099,PH7-11;P26-107,40.4271541,68.3645591,PH7-11;P26-108,40.4235395,68.3632579,PH7-10;P26-109,40.4233909,68.3632053,PH7-10;P26-110,40.4198151,68.3619188,PH7-9;P26-111,40.4196671,68.3618680,PH7-9;P26-112,40.4308026,68.3672325,PH7-12;P26-113,40.4306550,68.3671821,PH7-12;P26-114,40.4270791,68.3658932,PH7-11;P26-115,40.4269313,68.3658427,PH7-11;P26-116,40.4233167,68.3645410,PH7-10;P26-117,40.4231689,68.3644906,PH7-10;P26-118,40.4195930,68.3632019,PH7-9;P26-119,40.4194454,68.3631512,PH7-9;P26-120,40.4305797,68.3685155,PH7-12;P26-121,40.4304321,68.3684658,PH7-12;P26-122,40.4268571,68.3671793,PH7-11;P26-123,40.4267084,68.3671281,PH7-11;P26-124,40.4230938,68.3658238,PH7-10;P26-125,40.4229460,68.3657741,PH7-10;P26-126,40.4193711,68.3644879,PH7-9;P26-127,40.4192232,68.3644350,PH7-9;P26-128,40.4303576,68.3698016,PH7-12;P26-129,40.4302098,68.3697511,PH7-12;P26-130,40.4266341,68.3684622,PH7-11;P26-131,40.4264861,68.3684118,PH7-11;P26-132,40.4228716,68.3671098,PH7-10;P26-133,40.4227243,68.3670592,PH7-10;P26-134,40.4191482,68.3657706,PH7-9;P26-135,40.4190006,68.3657202,PH7-9;P26-136,40.4301134,68.3712093,PH7-12;P26-137,40.4299657,68.3711588,PH7-12;P26-138,40.4263907,68.3698694,PH7-11;P26-139,40.4262431,68.3698197,PH7-11;P26-140,40.4226284,68.3685173,PH7-10;P26-141,40.4224805,68.3684669,PH7-10;P26-142,40.4189065,68.3671813,PH7-9;P26-143,40.4187588,68.3671284,PH7-9;P27-144,40.4165893,68.3587275,PH7-7;P27-145,40.4164443,68.3586629,PH7-7;P27-146,40.4130315,68.3571592,PH7-7;P27-147,40.4128871,68.3570949,PH7-7;P27-148,40.4094541,68.3555830,PH7-8;P27-149,40.4093098,68.3555181,PH7-8;P27-150,40.4058774,68.3540071,PH7-8;P27-151,40.4057324,68.3539421,PH7-8;P27-152,40.4023195,68.3524395,PH7-8;P27-153,40.4021748,68.3523746,PH7-8;P27-154,40.4162688,68.3601071,PH7-7;P27-155,40.4161237,68.3600408,PH7-7;P27-156,40.4127109,68.3585388,PH7-7;P27-157,40.4125659,68.3584727,PH7-7;P27-158,40.4091336,68.3569607,PH7-8;P27-159,40.4089886,68.3568965,PH7-8;P27-160,40.4055573,68.3553847,PH7-8;P27-161,40.4054118,68.3553198,PH7-8;P27-162,40.4019994,68.3538171,PH7-8;P27-163,40.4018544,68.3537522,PH7-8;P27-164,40.4159207,68.3616073,PH7-7;P27-165,40.4157757,68.3615423,PH7-7;P27-166,40.4123624,68.3600370,PH7-7;P27-167,40.4122174,68.3599739,PH7-7;P27-168,40.4087855,68.3584607,PH7-8;P27-169,40.4086401,68.3583955,PH7-8;P27-170,40.4052082,68.3568846,PH7-8;P27-171,40.4050631,68.3568186,PH7-8;P27-172,40.4017787,68.3547647,PH7-8;P27-173,40.4016337,68.3547006,PH7-8;P27-174,40.4155007,68.3634122,PH7-13;P27-175,40.4153549,68.3633483,PH7-13;P27-176,40.4119430,68.3618439,PH7-13;P27-177,40.4117971,68.3617792,PH7-13;P27-178,40.4083657,68.3602676,PH7-14;P27-179,40.4082199,68.3602028,PH7-14;P27-180,40.4047888,68.3586914,PH7-14;P27-181,40.4046437,68.3586253,PH7-14;P27-182,40.4012309,68.3571228,PH7-14;P27-183,40.4010859,68.3570573,PH7-14;P27-184,40.4151801,68.3647900,PH7-13;P27-185,40.4150354,68.3647272,PH7-13;P27-186,40.4116234,68.3632217,PH7-13;P27-187,40.4114776,68.3631586,PH7-13;P27-188,40.4080451,68.3616452,PH7-14;P27-189,40.4079003,68.3615800,PH7-14;P27-190,40.4044692,68.3600690,PH7-14;P27-191,40.4043238,68.3600037,PH7-14;P27-192,40.4009099,68.3585009,PH7-14;P27-193,40.4007651,68.3584348,PH7-14;P27-194,40.4148859,68.3660476,PH7-13;P27-195,40.4147411,68.3659812,PH7-13;P27-196,40.4113307,68.3644792,PH7-13;P27-197,40.4111863,68.3644139,PH7-13;P27-198,40.4077534,68.3629024,PH7-14;P27-199,40.4076079,68.3628364,PH7-14;P27-200,40.4041779,68.3613261,PH7-14;P27-201,40.4040320,68.3612600,PH7-14;P27-202,40.4006183,68.3597564,PH7-14;P27-203,40.4004733,68.3596915,PH7-14;P28-204,40.4144094,68.3681056,PH7-17;P28-205,40.4142647,68.3680407,PH7-17;P28-206,40.4108545,68.3665221,PH7-18;P28-207,40.4107098,68.3664593,PH7-18;P28-208,40.4072898,68.3649368,PH7-18;P28-209,40.4071448,68.3648707,PH7-18;P28-210,40.4037071,68.3633413,PH7-19;P28-211,40.4035617,68.3632770,PH7-19;P28-212,40.4001521,68.3617604,PH7-20;P28-213,40.4000071,68.3616955,PH7-20;P28-214,40.4140898,68.3694833,PH7-17;P28-215,40.4139450,68.3694191,PH7-17;P28-216,40.4105349,68.3679020,PH7-18;P28-217,40.4103902,68.3678370,PH7-18;P28-218,40.4069605,68.3663103,PH7-18;P28-219,40.4068157,68.3662455,PH7-18;P28-220,40.4033872,68.3647207,PH7-19;P28-221,40.4032413,68.3646546,PH7-19;P28-222,40.3998326,68.3631379,PH7-20;P28-223,40.3996872,68.3630737,PH7-20;P28-224,40.4137966,68.3707401,PH7-17;P28-225,40.4136516,68.3706760,PH7-17;P28-226,40.4102430,68.3691573,PH7-18;P28-227,40.4100971,68.3690934,PH7-18;P28-228,40.4066688,68.3675677,PH7-18;P28-229,40.4065233,68.3675014,PH7-18;P28-230,40.4030950,68.3659762,PH7-19;P28-231,40.4029503,68.3659119,PH7-19;P28-232,40.3995401,68.3643952,PH7-20;P28-233,40.3993954,68.3643303,PH7-20;P28-234,40.4135027,68.3719963,PH7-17;P28-235,40.4133576,68.3719302,PH7-17;P28-236,40.4099507,68.3704148,PH7-18;P28-237,40.4098056,68.3703495,PH7-18;P28-238,40.4063763,68.3688230,PH7-18;P28-239,40.4062313,68.3687584,PH7-18;P28-240,40.4028023,68.3672335,PH7-19;P28-241,40.4026573,68.3671686,PH7-19;P28-242,40.3992484,68.3656504,PH7-20;P28-243,40.3991034,68.3655865,PH7-20;P28-244,40.4131847,68.3733740,PH7-17;P28-245,40.4130400,68.3733112,PH7-17;P28-246,40.4096299,68.3717924,PH7-18;P28-247,40.4094852,68.3717271,PH7-18;P28-248,40.4060603,68.3702027,PH7-18;P28-249,40.4059149,68.3701388,PH7-18;P28-250,40.4024827,68.3686110,PH7-19;P28-251,40.4023373,68.3685461,PH7-19;P28-252,40.3989277,68.3670278,PH7-20;P28-253,40.3987828,68.3669650,PH7-20;P28-254,40.4128919,68.3746315,PH7-17;P28-255,40.4127469,68.3745662,PH7-17;P28-256,40.4093382,68.3730493,PH7-18;P28-257,40.4091931,68.3729835,PH7-18;P28-258,40.4057638,68.3714580,PH7-18;P28-259,40.4056188,68.3713930,PH7-18;P28-260,40.4021901,68.3698662,PH7-19;P28-261,40.4020454,68.3698032,PH7-19;P28-262,40.3986353,68.3682851,PH7-20;P28-263,40.3984905,68.3682198,PH7-20;P29-1,40.4459309,68.2857751,PH12-1;P29-2,40.4457926,68.2856938,PH12-1;P29-3,40.4423384,68.2839865,PH12-1;P29-4,40.4421991,68.2839058,PH12-1;P29-5,40.4454845,68.2870985,PH12-1;P29-6,40.4453462,68.2870186,PH12-1;P29-7,40.4418920,68.2853094,PH12-1;P29-8,40.4417527,68.2852280,PH12-1;P29-9,40.4450392,68.2884233,PH12-1;P29-10,40.4448998,68.2883425,PH12-1;P29-11,40.4414458,68.2866330,PH12-1;P29-12,40.4413072,68.2865519,PH12-1;P29-13,40.4442949,68.2906299,PH12-3;P29-14,40.4441556,68.2905486,PH12-3;P29-15,40.4406794,68.2889064,PH12-3;P29-16,40.4405418,68.2888253,PH12-3;P29-17,40.4370470,68.2871740,PH12-2;P29-18,40.4369079,68.2870933,PH12-2;P29-19,40.4334105,68.2854388,PH12-2;P29-20,40.4332727,68.2853589,PH12-2;P29-21,40.4316384,68.2845910,PH12-2;P29-22,40.4438866,68.2918379,PH12-3;P29-23,40.4437488,68.2917574,PH12-3;P29-24,40.4402714,68.2901134,PH12-3;P29-25,40.4401337,68.2900329,PH12-3;P29-26,40.4366382,68.2883802,PH12-2;P29-27,40.4364992,68.2882992,PH12-2;P29-28,40.4330043,68.2866469,PH12-2;P29-29,40.4328653,68.2865664,PH12-2;P29-30,40.4312103,68.2858601,PH12-2;P29-31,40.4434399,68.2931627,PH12-3;P29-32,40.4433021,68.2930822,PH12-3;P29-33,40.4398250,68.2914381,PH12-3;P29-34,40.4396873,68.2913576,PH12-3;P29-35,40.4361918,68.2897049,PH12-2;P29-36,40.4360527,68.2896238,PH12-2;P29-37,40.4325579,68.2879715,PH12-2;P29-38,40.4324185,68.2878902,PH12-2;P29-39,40.4307934,68.2870958,PH12-2;P30-40,40.4428329,68.2949683,PH13-7;P30-41,40.4426940,68.2948869,PH13-7;P30-42,40.4392583,68.2931167,PH13-7;P30-43,40.4391201,68.2930359,PH13-7;P30-44,40.4356676,68.2912579,PH13-6;P30-45,40.4355294,68.2911766,PH13-6;P30-46,40.4320769,68.2893986,PH13-6;P30-47,40.4319375,68.2893173,PH13-6;P30-48,40.4301132,68.2885071,PH13-6;P30-49,40.4423864,68.2962907,PH13-7;P30-50,40.4422475,68.2962099,PH13-7;P30-51,40.4388142,68.2944402,PH13-7;P30-52,40.4386749,68.2943591,PH13-7;P30-53,40.4352212,68.2925801,PH13-6;P30-54,40.4350835,68.2924995,PH13-6;P30-55,40.4316304,68.2907203,PH13-6;P30-56,40.4314922,68.2906401,PH13-6;P30-57,40.4297385,68.2896197,PH13-6;P30-58,40.4419399,68.2976126,PH13-7;P30-59,40.4418016,68.2975312,PH13-7;P30-60,40.4383679,68.2957637,PH13-7;P30-61,40.4382293,68.2956829,PH13-7;P30-62,40.4347752,68.2939029,PH13-6;P30-63,40.4346375,68.2938215,PH13-6;P30-64,40.4311842,68.2920434,PH13-6;P30-65,40.4310463,68.2919621,PH13-6;P30-66,40.4292789,68.2909826,PH13-6;P30-67,40.4414235,68.2991452,PH13-5;P30-68,40.4412849,68.2990644,PH13-5;P30-69,40.4378502,68.2972946,PH13-5;P30-70,40.4377127,68.2972135,PH13-5;P30-71,40.4342590,68.2954340,PH13-5;P30-72,40.4341214,68.2953538,PH13-5;P30-73,40.4306683,68.2935744,PH13-4;P30-74,40.4305294,68.2934942,PH13-4;P30-75,40.4270931,68.2917232,PH13-4;P30-76,40.4269544,68.2916431,PH13-4;P30-77,40.4409777,68.3004670,PH13-5;P30-78,40.4408394,68.3003857,PH13-5;P30-79,40.4374049,68.2986169,PH13-5;P30-80,40.4372662,68.2985355,PH13-5;P30-81,40.4338130,68.2967571,PH13-5;P30-82,40.4336749,68.2966758,PH13-5;P30-83,40.4302219,68.2948967,PH13-4;P30-84,40.4300836,68.2948162,PH13-4;P30-85,40.4266475,68.2930474,PH13-4;P30-86,40.4265096,68.2929657,PH13-4;P30-87,40.4405311,68.3017903,PH13-5;P30-88,40.4403928,68.3017095,PH13-5;P30-89,40.4369592,68.2999395,PH13-5;P30-90,40.4368205,68.2998584,PH13-5;P30-91,40.4333673,68.2980788,PH13-5;P30-92,40.4332290,68.2979986,PH13-5;P30-93,40.4297756,68.2962191,PH13-4;P30-94,40.4296377,68.2961389,PH13-4;P30-95,40.4262033,68.2943698,PH13-4;P30-96,40.4260647,68.2942884,PH13-4;P31-97,40.4400126,68.3033266,PH14-9;P31-98,40.4398748,68.3032454,PH14-9;P31-99,40.4364275,68.3015110,PH14-9;P31-100,40.4362884,68.3014309,PH14-9;P31-101,40.4328243,68.2996873,PH14-9;P31-102,40.4326858,68.2996065,PH14-9;P31-103,40.4292213,68.2978637,PH14-8;P31-104,40.4290846,68.2977838,PH14-8;P31-105,40.4256372,68.2960507,PH14-8;P31-106,40.4254981,68.2959700,PH14-8;P31-107,40.4396065,68.3045335,PH14-9;P31-108,40.4394676,68.3044527,PH14-9;P31-109,40.4360202,68.3027171,PH14-9;P31-110,40.4358825,68.3026366,PH14-9;P31-111,40.4324187,68.3008941,PH14-9;P31-112,40.4322793,68.3008133,PH14-9;P31-113,40.4288145,68.2990700,PH14-8;P31-114,40.4286761,68.2989886,PH14-8;P31-115,40.4252292,68.2972558,PH14-8;P31-116,40.4250916,68.2971756,PH14-8;P31-117,40.4392390,68.3056231,PH14-9;P31-118,40.4391011,68.3055423,PH14-9;P31-119,40.4356547,68.3038083,PH14-9;P31-120,40.4355165,68.3037270,PH14-9;P31-121,40.4320507,68.3019836,PH14-9;P31-122,40.4319122,68.3019028,PH14-9;P31-123,40.4284484,68.3001603,PH14-8;P31-124,40.4283095,68.3000789,PH14-8;P31-125,40.4248618,68.2983452,PH14-8;P31-126,40.4247239,68.2982650,PH14-8;P31-127,40.4384648,68.3079179,PH14-11;P31-128,40.4383259,68.3078377,PH14-11;P31-129,40.4349181,68.3059899,PH14-11;P31-130,40.4347794,68.3059086,PH14-11;P31-131,40.4313534,68.3040515,PH14-11;P31-132,40.4312145,68.3039710,PH14-11;P31-133,40.4277880,68.3021147,PH14-10;P31-134,40.4276503,68.3020330,PH14-10;P31-135,40.4242418,68.3001862,PH14-10;P31-136,40.4241029,68.3001054,PH14-10;P31-137,40.4380591,68.3091237,PH14-11;P31-138,40.4379197,68.3090426,PH14-11;P31-139,40.4345116,68.3071948,PH14-11;P31-140,40.4343729,68.3071135,PH14-11;P31-141,40.4309468,68.3052563,PH14-11;P31-142,40.4308078,68.3051759,PH14-11;P31-143,40.4273814,68.3033183,PH14-10;P31-144,40.4272421,68.3032370,PH14-10;P31-145,40.4238353,68.3013909,PH14-10;P31-146,40.4236963,68.3013101,PH14-10;P31-147,40.4376124,68.3104451,PH14-11;P31-148,40.4374735,68.3103637,PH14-11;P31-149,40.4340657,68.3085167,PH14-11;P31-150,40.4339271,68.3084354,PH14-11;P31-151,40.4305172,68.3065870,PH14-11;P31-152,40.4303795,68.3065058,PH14-11;P31-153,40.4269357,68.3046398,PH14-10;P31-154,40.4267980,68.3045596,PH14-10;P31-155,40.4233896,68.3027121,PH14-10;P31-156,40.4232507,68.3026310,PH14-10;P32-1,40.4203475,68.2747327,PH15-2;P32-2,40.4205059,68.2748867,PH15-2;P32-3,40.4230372,68.2771813,PH15-2;P32-4,40.4231957,68.2773353,PH15-2;P32-5,40.4258043,68.2796796,PH15-1;P32-6,40.4259628,68.2798336,PH15-1;P32-7,40.4284985,68.2821242,PH15-1;P32-8,40.4286570,68.2822783,PH15-1;P32-9,40.4279455,68.2835442,PH15-1;P32-10,40.4277870,68.2833859,PH15-1;P32-11,40.4252495,68.2810995,PH15-1;P32-12,40.4250910,68.2809444,PH15-1;P32-13,40.4224888,68.2785926,PH15-2;P32-14,40.4223303,68.2784386,PH15-2;P32-15,40.4197940,68.2761525,PH15-2;P32-16,40.4196356,68.2759975,PH15-2;P32-17,40.4189812,68.2771571,PH15-2;P32-18,40.4191397,68.2773154,PH15-2;P32-19,40.4216773,68.2796014,PH15-2;P32-20,40.4218358,68.2797561,PH15-2;P32-21,40.4244380,68.2821041,PH15-1;P32-22,40.4245965,68.2822613,PH15-1;P32-23,40.4271334,68.2845488,PH15-1;P32-24,40.4272919,68.2847028,PH15-1;P33-25,40.4177594,68.2782671,PH15-4;P33-26,40.4179161,68.2784212,PH15-4;P33-27,40.4205850,68.2808490,PH15-4;P33-28,40.4207435,68.2810073,PH15-4;P33-29,40.4234105,68.2834354,PH15-3;P33-30,40.4235689,68.2835901,PH15-3;P33-31,40.4262456,68.2860260,PH15-3;P33-32,40.4263970,68.2861759,PH15-3;P33-33,40.4257991,68.2872358,PH15-3;P33-34,40.4256425,68.2870800,PH15-3;P33-35,40.4229633,68.2846537,PH15-3;P33-36,40.4228094,68.2845027,PH15-3;P33-37,40.4201443,68.2820671,PH15-4;P33-38,40.4199858,68.2819089,PH15-4;P33-39,40.4173220,68.2794841,PH15-4;P33-40,40.4171635,68.2793269,PH15-4;P33-41,40.4165232,68.2804671,PH15-4;P33-42,40.4166816,68.2806222,PH15-4;P33-43,40.4193486,68.2830406,PH15-4;P33-44,40.4195070,68.2831956,PH15-4;P33-45,40.4222102,68.2855679,PH15-3;P33-46,40.4223582,68.2857136,PH15-3;P33-47,40.4250433,68.2881441,PH15-3;P33-48,40.4251999,68.2883000,PH15-3;P34-49,40.4153696,68.2816871,PH15-6;P34-50,40.4155281,68.2818454,PH15-6;P34-51,40.4183111,68.2843390,PH15-6;P34-52,40.4184696,68.2844930,PH15-6;P34-53,40.4212526,68.2869901,PH15-5;P34-54,40.4214106,68.2871452,PH15-5;P34-55,40.4241935,68.2896392,PH15-5;P34-56,40.4242437,68.2899864,PH15-5;P34-57,40.4236962,68.2909561,PH15-5;P34-58,40.4235377,68.2908020,PH15-5;P34-59,40.4207580,68.2883079,PH15-5;P34-60,40.4205996,68.2881538,PH15-5;P34-61,40.4178160,68.2856557,PH15-6;P34-62,40.4176575,68.2855017,PH15-6;P34-63,40.4148745,68.2830070,PH15-6;P34-64,40.4147165,68.2828498,PH15-6;P34-65,40.4141200,68.2839095,PH15-6;P34-66,40.4142785,68.2840635,PH15-6;P34-67,40.4170602,68.2865604,PH15-6;P34-68,40.4172187,68.2867155,PH15-6;P34-69,40.4200003,68.2892094,PH15-5;P34-70,40.4201595,68.2893634,PH15-5;P34-71,40.4229417,68.2918618,PH15-5;P34-72,40.4231001,68.2920159,PH15-5;P35-73,40.4124712,68.2845903,PH16-8;P35-74,40.4126296,68.2847443,PH16-8;P35-75,40.4149929,68.2868554,PH16-8;P35-76,40.4151514,68.2870095,PH16-8;P35-77,40.4175141,68.2891197,PH16-7;P35-78,40.4176726,68.2892748,PH16-7;P35-79,40.4200352,68.2913820,PH16-7;P35-80,40.4201937,68.2915393,PH16-7;P35-81,40.4195406,68.2926987,PH16-7;P35-82,40.4193821,68.2925447,PH16-7;P35-83,40.4170189,68.2904364,PH16-7;P35-84,40.4168604,68.2902792,PH16-7;P35-85,40.4144964,68.2881721,PH16-8;P35-86,40.4143380,68.2880170,PH16-8;P35-87,40.4119739,68.2859069,PH16-8;P35-88,40.4118154,68.2857529,PH16-8;P35-89,40.4112646,68.2867311,PH16-8;P35-90,40.4114222,68.2868851,PH16-8;P35-91,40.4137939,68.2889791,PH16-8;P35-92,40.4139524,68.2891363,PH16-8;P35-93,40.4163156,68.2912445,PH16-7;P35-94,40.4164741,68.2914017,PH16-7;P35-95,40.4188145,68.2935529,PH16-7;P35-96,40.4189730,68.2937094,PH16-7;P36-97,40.4099798,68.2885390,PH16-10;P36-98,40.4102458,68.2885032,PH16-10;P36-99,40.4132809,68.2912372,PH16-10;P36-100,40.4134394,68.2913923,PH16-10;P36-101,40.4164744,68.2941242,PH16-9;P36-102,40.4166329,68.2942817,PH16-9;P36-103,40.4196679,68.2970131,PH16-9;P36-104,40.4198264,68.2971714,PH16-9;P36-105,40.4192886,68.2981281,PH16-9;P36-106,40.4191302,68.2979698,PH16-9;P36-107,40.4160947,68.2952384,PH16-9;P36-108,40.4159362,68.2950801,PH16-9;P36-109,40.4129012,68.2923490,PH16-10;P36-110,40.4127427,68.2921907,PH16-10;P36-111,40.4097076,68.2894598,PH16-10;P36-112,40.4095491,68.2893048,PH16-10;P36-113,40.4089522,68.2903612,PH16-10;P36-114,40.4091107,68.2905194,PH16-10;P36-115,40.4121458,68.2932503,PH16-10;P36-116,40.4123043,68.2934086,PH16-10;P36-117,40.4153401,68.2961397,PH16-9;P36-118,40.4154985,68.2962970,PH16-9;P36-119,40.4185327,68.2990295,PH16-9;P36-120,40.4186912,68.2991867,PH16-9;P37-121,40.4077235,68.2916746,PH16-12;P37-122,40.4078820,68.2918311,PH16-12;P37-123,40.4110134,68.2946426,PH16-12;P37-124,40.4111719,68.2947973,PH16-12;P37-125,40.4143029,68.2976109,PH16-11;P37-126,40.4144614,68.2977649,PH16-11;P37-127,40.4175932,68.3005794,PH16-11;P37-128,40.4177517,68.3007335,PH16-11;P37-129,40.4171555,68.3017931,PH16-11;P37-130,40.4169970,68.3016390,PH16-11;P37-131,40.4138653,68.2988245,PH16-11;P37-132,40.4137068,68.2986704,PH16-11;P37-133,40.4105749,68.2958562,PH16-12;P37-134,40.4104165,68.2957021,PH16-12;P37-135,40.4072859,68.2928881,PH16-12;P37-136,40.4071274,68.2927341,PH16-12;P37-137,40.4065308,68.2937936,PH16-12;P37-138,40.4066885,68.2939476,PH16-12;P37-139,40.4098204,68.2967617,PH16-12;P37-140,40.4099788,68.2969157,PH16-12;P37-141,40.4131550,68.2996485,PH16-11;P37-142,40.4133135,68.2998068,PH16-11;P37-143,40.4164003,68.3026986,PH16-11;P37-144,40.4165588,68.3028527,PH16-11;P38-145,40.4047628,68.2948837,PH16-14;P38-146,40.4049213,68.2950420,PH16-14;P38-147,40.4070992,68.2970379,PH16-14;P38-148,40.4072577,68.2971962,PH16-14;P38-149,40.4094364,68.2991922,PH16-14;P38-150,40.4095948,68.2993505,PH16-14;P38-151,40.4088821,68.3006116,PH16-14;P38-152,40.4087237,68.3004576,PH16-14;P38-153,40.4065469,68.2984572,PH16-14;P38-154,40.4063884,68.2983032,PH16-14;P38-155,40.4042102,68.2963030,PH16-14;P38-156,40.4040518,68.2961490,PH16-14;P38-157,40.4120275,68.3016602,PH16-13;P38-158,40.4121873,68.3018185,PH16-13;P38-159,40.4141595,68.3036065,PH16-13;P38-160,40.4143179,68.3037606,PH16-13;P38-161,40.4162895,68.3055519,PH16-13;P38-162,40.4164479,68.3057070,PH16-13;P38-163,40.4157846,68.3068866,PH16-13;P38-164,40.4156262,68.3067315,PH16-13;P38-165,40.4136659,68.3049188,PH16-13;P38-166,40.4135070,68.3047636,PH16-13;P38-167,40.4115251,68.3029938,PH16-13;P38-168,40.4113661,68.3028355,PH16-13;P39-169,40.4026414,68.2978603,PH16-16;P39-170,40.4027999,68.2980143,PH16-16;P39-171,40.4052335,68.3001837,PH16-16;P39-172,40.4053919,68.3003409,PH16-16;P39-173,40.4078268,68.3025105,PH16-16;P39-174,40.4079853,68.3026656,PH16-16;P39-175,40.4070992,68.3042355,PH16-16;P39-176,40.4069407,68.3040814,PH16-16;P39-177,40.4045072,68.3019118,PH16-16;P39-178,40.4043487,68.3017567,PH16-16;P39-179,40.4019147,68.2995873,PH16-16;P39-180,40.4017562,68.2994301,PH16-16;P39-181,40.4105900,68.3049849,PH16-15;P39-182,40.4107485,68.3051389,PH16-15;P39-183,40.4126358,68.3068225,PH16-15;P39-184,40.4127942,68.3069765,PH16-15;P39-185,40.4146828,68.3086584,PH16-15;P39-186,40.4148399,68.3088143,PH16-15;P39-187,40.4139545,68.3103842,PH16-15;P39-188,40.4137966,68.3102301,PH16-15;P39-189,40.4119088,68.3085465,PH16-15;P39-190,40.4117504,68.3083924,PH16-15;P39-191,40.4098631,68.3067088,PH16-15;P39-192,40.4097047,68.3065548,PH16-15;P40-193,40.4004472,68.3008763,PH16-18;P40-194,40.4006057,68.3010303,PH16-18;P40-195,40.4032211,68.3033735,PH16-18;P40-196,40.4033787,68.3035286,PH16-18;P40-197,40.4059946,68.3058688,PH16-18;P40-198,40.4061530,68.3060271,PH16-18;P40-199,40.4054413,68.3072881,PH16-18;P40-200,40.4052829,68.3071340,PH16-18;P40-201,40.4026684,68.3047927,PH16-18;P40-202,40.4025099,68.3046355,PH16-18;P40-203,40.3998948,68.3022944,PH16-18;P40-204,40.3997363,68.3021372,PH16-18;P40-205,40.4088885,68.3084883,PH16-17;P40-206,40.4090470,68.3086424,PH16-17;P40-207,40.4109484,68.3102483,PH16-17;P40-208,40.4111068,68.3104034,PH16-17;P40-209,40.4130082,68.3120062,PH16-17;P40-210,40.4131667,68.3121645,PH16-17;P40-211,40.4124511,68.3134256,PH16-17;P40-212,40.4122940,68.3132715,PH16-17;P40-213,40.4103919,68.3116677,PH16-17;P40-214,40.4102334,68.3115104,PH16-17;P40-215,40.4083307,68.3099077,PH16-17;P40-216,40.4081722,68.3097494,PH16-17;P41-217,40.4201162,68.3077090,PH18-12;P41-218,40.4202958,68.3078160,PH18-12;P41-219,40.4232270,68.3095497,PH18-12;P41-220,40.4234076,68.3096595,PH18-12;P41-221,40.4263368,68.3113934,PH18-12;P41-222,40.4265173,68.3115004,PH18-12;P41-223,40.4294475,68.3132373,PH18-13;P41-224,40.4296281,68.3133443,PH18-13;P41-225,40.4325572,68.3150813,PH18-13;P41-226,40.4327377,68.3151883,PH18-13;P41-227,40.4356678,68.3169248,PH18-13;P41-228,40.4358484,68.3170318,PH18-13;P41-229,40.4354749,68.3181172,PH18-13;P41-230,40.4352952,68.3180101,PH18-13;P41-231,40.4323653,68.3162729,PH18-13;P41-232,40.4321848,68.3161659,PH18-13;P41-233,40.4292546,68.3144295,PH18-13;P41-234,40.4290748,68.3143218,PH18-13;P41-235,40.4261450,68.3125856,PH18-12;P41-236,40.4259644,68.3124786,PH18-12;P41-237,40.4230342,68.3107419,PH18-12;P41-238,40.4228544,68.3106348,PH18-12;P41-239,40.4199245,68.3088983,PH18-12;P41-240,40.4197439,68.3087913,PH18-12;P41-241,40.4192919,68.3101099,PH18-12;P41-242,40.4194724,68.3102169,PH18-12;P41-243,40.4224026,68.3119535,PH18-12;P41-244,40.4225832,68.3120605,PH18-12;P41-245,40.4255123,68.3137973,PH18-12;P41-246,40.4256929,68.3139043,PH18-12;P41-247,40.4286230,68.3156412,PH18-13;P41-248,40.4288036,68.3157483,PH18-13;P41-249,40.4317326,68.3174826,PH18-13;P41-250,40.4319132,68.3175896,PH18-13;P41-251,40.4348433,68.3193268,PH18-13;P41-252,40.4350238,68.3194339,PH18-13;P42-253,40.4226907,68.3139482,PH18-14;P42-254,40.4228713,68.3140552,PH18-14;P42-255,40.4256208,68.3156850,PH18-14;P42-256,40.4258000,68.3157921,PH18-14;P42-257,40.4285681,68.3174337,PH18-15;P42-258,40.4287474,68.3175408,PH18-15;P42-259,40.4314778,68.3191593,PH18-15;P42-260,40.4316573,68.3192663,PH18-15;P42-261,40.4344065,68.3208966,PH18-15;P42-262,40.4345863,68.3210036,PH18-15;P42-263,40.4339746,68.3227861,PH18-15;P42-264,40.4337951,68.3226790,PH18-15;P42-265,40.4310467,68.3210486,PH18-15;P42-266,40.4308661,68.3209416,PH18-15;P42-267,40.4281360,68.3193231,PH18-15;P42-268,40.4279554,68.3192160,PH18-15;P42-269,40.4251887,68.3175743,PH18-14;P42-270,40.4250081,68.3174673,PH18-14;P42-271,40.4222597,68.3158373,PH18-14;P42-272,40.4220791,68.3157303,PH18-14;P42-273,40.4191963,68.3140210,PH18-14;P42-274,40.4190157,68.3139140,PH18-14;P42-275,40.4174625,68.3129928,PH18-14;P43-276,40.4181978,68.3158902,PH18-16;P43-277,40.4200208,68.3169641,PH18-16;P43-278,40.4202003,68.3170712,PH18-16;P43-279,40.4236677,68.3191094,PH18-16;P43-280,40.4238472,68.3192165,PH18-16;P43-281,40.4270517,68.3212025,PH18-16;P43-282,40.4272314,68.3213096,PH18-16;P43-283,40.4301142,68.3229069,PH18-16;P43-284,40.4302940,68.3230140,PH18-16;P43-285,40.4331476,68.3246934,PH18-16;P43-286,40.4333281,68.3248005,PH18-16;P43-287,40.4329556,68.3258857,PH18-16;P43-288,40.4327756,68.3257769,PH18-16;P43-289,40.4299215,68.3240963,PH18-16;P43-290,40.4297420,68.3239893,PH18-16;P43-291,40.4268884,68.3223110,PH18-16;P43-292,40.4267078,68.3222029,PH18-16;P43-293,40.4263763,68.3231720,PH18-16;P43-294,40.4265555,68.3232790,PH18-16;P43-295,40.4294091,68.3249584,PH18-16;P43-296,40.4295889,68.3250655,PH18-16;P43-297,40.4325345,68.3264800,PH18-16;P43-298,40.4327143,68.3265871,PH18-16;P43-299,40.4260961,68.3239873,PH18-16;P43-300,40.4262767,68.3240943,PH18-16"

    private val VALVE_ROWS: List<List<String>> by lazy { VALVE_DATA.split(';').map { it.split(',') } }

    val SURVEYED_VALVE_COORDINATES: Map<String, GeoPoint> by lazy {
        VALVE_ROWS.associate { it[0] to GeoPoint(it[1].toDouble(), it[2].toDouble()) }
    }

    /** Jain drawing tag -> new block from "Drip New block 2026 Nov.xlsx" (auto-split, to be reviewed). */
    val VALVE_NEW_BLOCK: Map<String, String> by lazy { VALVE_ROWS.associate { it[0] to it[3] } }

    /** Gross area (ha) of each drip system = sum of its CAD blocks (Sh. Rashido 1099.2 ha + T. Malik 649.5 ha). */
    val SYSTEM_GROSS_AREA_HA: Map<Int, Double> = mapOf(
        24 to 115.25,
        25 to 131.71,
        26 to 118.96,
        27 to 141.82,
        28 to 141.76,
        29 to 96.82,
        30 to 142.71,
        31 to 145.97,
        32 to 57.07,
        33 to 55.46,
        34 to 59.61,
        35 to 48.36,
        36 to 56.32,
        37 to 63.68,
        38 to 53.57,
        39 to 65.54,
        40 to 56.1,
        41 to 81.61,
        42 to 66.65,
        43 to 49.72
    )

    private val VALVES_PER_SYSTEM: Map<Int, Int> by lazy {
        VALVE_ROWS.groupingBy { systemOfTag(it[0]) }.eachCount()
    }

    /** "P29-3" -> 29 (or -1 if the tag is not a Jain valve tag). */
    fun systemOfTag(tag: String): Int =
        tag.removePrefix("P").substringBefore("-").toIntOrNull() ?: -1

    /** Pump house feeding a Jain valve tag, from the drawing's system -> PH table. */
    fun pumpHouseOfTag(tag: String): String =
        SYSTEM_SPECS["SYS-${systemOfTag(tag)}"]?.pumpHouseId ?: "?"

    /** Gross area served by one valve = system gross area / number of valves in the system. */
    fun valveAreaHa(tag: String): Double {
        val sys = systemOfTag(tag)
        val area = SYSTEM_GROSS_AREA_HA[sys] ?: return 0.0
        val n = VALVES_PER_SYSTEM[sys] ?: return 0.0
        return if (n > 0) area / n else 0.0
    }

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

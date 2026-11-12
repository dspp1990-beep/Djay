package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * High-fidelity representation of the official Jain Irrigation Systems Ltd blueprints:
 * 1. DRIP IRRIGATION SYSTEM SARDOBA (SH. RASHIDO) - 1,099.16 HA (Drawing No. MIS/610/24-25)
 * 2. DRIP IRRIGATION SYSTEM T.MALIK (SARDOBA SITE 2) - 649.50 HA (Drawing No. MIS/604/24-25)
 */

enum class CanalType(val label: String, val color: Long) {
    BRANCH_CANAL_LATOC("Canal - LaToc (Branch Canal)", 0xFF06B6D4), // Bright Cyan
    COLLECTOR("Collector Canal", 0xFF0284C7),                    // Deep Blue
    PROPOSED_CANAL("Proposed Canal", 0xFF38BDF8),                // Light Cyan/dashed
    DRAIN_COLLECTOR("Drain Collector", 0xFF0D9488)               // Dark Teal
}

enum class RoadType(val label: String, val widthMeters: Double, val color: Long) {
    INTERNAL_ROAD_9M("Internal Road 9.0m Wide", 9.0, 0xFFD97706),   // Amber/Brown Main Corridor
    INTERNAL_ROAD_1_8M("Internal Road 1.8m Wide", 1.8, 0xFF94A3B8)  // Slate/Grey Service Corridor
}

enum class CivilType(val label: String, val defaultDim: String) {
    FERTIGATION("Fertigation Chemical Station", "82 m³ Tank"),
    PUMP_STATION("Pump House Building", "15.0m x 10.0m"),
    INTAKE_LINE("Canal Intake Pipe", "Diameter 400mm Steel")
}

data class CivilBuilding(
    val id: String,
    val pumpHouseId: String,
    val type: CivilType,
    val label: String,
    val center: GeoPoint,
    val widthMeters: Double,
    val lengthMeters: Double,
    val rotationDeg: Float = 0f
)

data class CanalLine(
    val id: String,
    val name: String,
    val type: CanalType,
    val points: List<GeoPoint>
)

data class RoadLine(
    val id: String,
    val name: String,
    val type: RoadType,
    val points: List<GeoPoint>
)

data class BlueprintPlot(
    val id: String,
    val siteName: String, // "Sh. Rashido" or "T. Malik (Site 2)"
    val pumpHouseId: String,
    val systemNumber: Int,
    val areaHa: Double,
    val label: String,
    val center: GeoPoint,
    val boundary: List<GeoPoint>,
    val cadastralTag: String? = null,
    val cadastralHa: Double? = null,
    val badgePosition: GeoPoint? = null
)

data class ContourLine(
    val elevationMeters: Double,
    val label: String,
    val points: List<GeoPoint>
)

enum class MapBaseOverlayMode(val label: String, val description: String) {
    HYBRID_ENGINEERING("Hybrid CAD + Satellite", "High-res agricultural orthophoto blended with CAD engineering vectors"),
    CAD_BLUEPRINT("CAD Blueprint Drawing", "Pure technical engineering drawing canvas with blueprint grid & title block"),
    SATELLITE_AERIAL("Satellite Aerial", "Photorealistic satellite imagery of cotton crop parcels & waterways"),
    TOPOGRAPHIC_TERRAIN("Topographic Contours", "Terrain elevation model with 278.5m–283.0m isolines & hypsometric tinting")
}

data class PipeDiameterSpec(
    val diameterMm: Int,
    val material: String = "PVC",
    val color: Long,
    val strokeWidthDp: Float
)

object BlueprintStructures {

    // Blueprint Outer Diameter hierarchy from the drawing legend:
    val PIPE_SPECS = listOf(
        PipeDiameterSpec(355, "PVC", 0xFF7E22CE, 4.5f), // Purple/Maroon
        PipeDiameterSpec(315, "PVC", 0xFF1E40AF, 4.0f), // Navy Blue
        PipeDiameterSpec(250, "PVC", 0xFF2563EB, 3.5f), // Blue
        PipeDiameterSpec(200, "PVC", 0xFF0284C7, 3.0f), // Sky Blue
        PipeDiameterSpec(160, "PVC", 0xFF0D9488, 2.5f), // Teal
        PipeDiameterSpec(110, "PVC", 0xFF16A34A, 2.0f), // Green
        PipeDiameterSpec(90, "PVC", 0xFFEA580C, 1.6f),  // Orange
        PipeDiameterSpec(63, "PVC", 0xFFDC2626, 1.2f)   // Red
    )

    fun getColorForDiameter(diameterMm: Int): Long {
        return PIPE_SPECS.find { it.diameterMm == diameterMm }?.color ?: 0xFF2563EB
    }

    // ==========================================
    // CANAL NETWORK (Canal - LaToc & Collectors)
    // ==========================================
    val CANALS: List<CanalLine> = listOf(
        // Canal - LaToc (Branch Canal) supplying Sh. Rashido site
        CanalLine(
            id = "canal_latoc_rashido",
            name = "Canal - LaToc (Branch Canal)",
            type = CanalType.BRANCH_CANAL_LATOC,
            points = listOf(
                GeoPoint(40.434500, 68.280000),
                GeoPoint(40.431200, 68.282800), // Intake for PH-12
                GeoPoint(40.428000, 68.289500), // Intake for PH-13
                GeoPoint(40.426800, 68.290800), // Intake for PH-15
                GeoPoint(40.423800, 68.296800), // Intake for PH-14
                GeoPoint(40.421800, 68.300000), // Intake for PH-16
                GeoPoint(40.416800, 68.309000), // Intake for PH-17
                GeoPoint(40.412000, 68.318000),
                GeoPoint(40.424800, 68.322500)  // Intake for PH-18
            )
        ),
        // Canal - LaToc (Branch Canal) supplying T. Malik Site 2
        CanalLine(
            id = "canal_latoc_tmalik",
            name = "Canal - LaToc (T. Malik Sector)",
            type = CanalType.BRANCH_CANAL_LATOC,
            points = listOf(
                GeoPoint(40.422000, 68.343800), // Intake for PH-7
                GeoPoint(40.420200, 68.353000), // Intake for PH-8
                GeoPoint(40.418500, 68.361200), // Intake for PH-10
                GeoPoint(40.418000, 68.361800), // Intake for PH-9
                GeoPoint(40.416600, 68.371000), // Intake for PH-11
                GeoPoint(40.413500, 68.376000)
            )
        ),
        // Collector Canals along Field Boundaries
        CanalLine(
            id = "collector_rashido_west",
            name = "Collector Canal West",
            type = CanalType.COLLECTOR,
            points = listOf(
                GeoPoint(40.447500, 68.284000),
                GeoPoint(40.441000, 68.287000),
                GeoPoint(40.422000, 68.280500),
                GeoPoint(40.411000, 68.281500),
                GeoPoint(40.397000, 68.299500)
            )
        ),
        CanalLine(
            id = "collector_tmalik_east",
            name = "Collector Canal East (T. Malik)",
            type = CanalType.COLLECTOR,
            points = listOf(
                GeoPoint(40.437500, 68.348000),
                GeoPoint(40.433500, 68.368000),
                GeoPoint(40.424000, 68.371000),
                GeoPoint(40.395500, 68.367000)
            )
        ),
        // Drain Collectors (Boundary Drainage Network)
        CanalLine(
            id = "drain_rashido_north",
            name = "Drain Collector North",
            type = CanalType.DRAIN_COLLECTOR,
            points = listOf(
                GeoPoint(40.447000, 68.284500),
                GeoPoint(40.445500, 68.299500),
                GeoPoint(40.438000, 68.319000),
                GeoPoint(40.425000, 68.324500)
            )
        ),
        CanalLine(
            id = "drain_tmalik_south",
            name = "Drain Collector South",
            type = CanalType.DRAIN_COLLECTOR,
            points = listOf(
                GeoPoint(40.404500, 68.357000),
                GeoPoint(40.401000, 68.355500),
                GeoPoint(40.395800, 68.367500)
            )
        )
    )

    // ==========================================
    // ROAD NETWORK (9.0m Main & 1.8m Internal)
    // ==========================================
    val ROADS: List<RoadLine> = listOf(
        // 9.0m Wide Main Road along Sh. Rashido Site Corridor
        RoadLine(
            id = "road_9m_rashido_main",
            name = "Internal Road 9m Wide (Rashido Highway)",
            type = RoadType.INTERNAL_ROAD_9M,
            points = listOf(
                GeoPoint(40.435000, 68.280500),
                GeoPoint(40.431000, 68.283500),
                GeoPoint(40.427800, 68.290200),
                GeoPoint(40.426700, 68.291500),
                GeoPoint(40.423500, 68.297500),
                GeoPoint(40.421600, 68.300700),
                GeoPoint(40.416600, 68.309600),
                GeoPoint(40.424600, 68.323300),
                GeoPoint(40.421700, 68.344500) // Connecting link to T. Malik
            )
        ),
        // 9.0m Wide Main Road connecting T. Malik Pump Stations
        RoadLine(
            id = "road_9m_tmalik_main",
            name = "Internal Road 9m Wide (T. Malik Arterial)",
            type = RoadType.INTERNAL_ROAD_9M,
            points = listOf(
                GeoPoint(40.421700, 68.344500),
                GeoPoint(40.419900, 68.353700),
                GeoPoint(40.418300, 68.361900),
                GeoPoint(40.417800, 68.362500),
                GeoPoint(40.416400, 68.371700),
                GeoPoint(40.396500, 68.368000)
            )
        ),
        // 1.8m Wide Internal Service Roads along manifolds
        RoadLine(
            id = "road_1_8m_rashido_p29_p30",
            name = "Service Road 1.8m (P29/P30 Blocks)",
            type = RoadType.INTERNAL_ROAD_1_8M,
            points = listOf(
                GeoPoint(40.431000, 68.283500),
                GeoPoint(40.437000, 68.286500),
                GeoPoint(40.446200, 68.286000)
            )
        ),
        RoadLine(
            id = "road_1_8m_rashido_p31",
            name = "Service Road 1.8m (P31 Sector)",
            type = RoadType.INTERNAL_ROAD_1_8M,
            points = listOf(
                GeoPoint(40.423500, 68.297500),
                GeoPoint(40.432500, 68.303000),
                GeoPoint(40.440700, 68.307200)
            )
        ),
        RoadLine(
            id = "road_1_8m_tmalik_p24_p25",
            name = "Service Road 1.8m (T. Malik P24-P25)",
            type = RoadType.INTERNAL_ROAD_1_8M,
            points = listOf(
                GeoPoint(40.421700, 68.344500),
                GeoPoint(40.427000, 68.346200),
                GeoPoint(40.436500, 68.349100)
            )
        ),
        RoadLine(
            id = "road_1_8m_tmalik_p27_p28",
            name = "Service Road 1.8m (T. Malik P27-P28)",
            type = RoadType.INTERNAL_ROAD_1_8M,
            points = listOf(
                GeoPoint(40.418300, 68.361900),
                GeoPoint(40.405000, 68.357700),
                GeoPoint(40.401500, 68.356200)
            )
        )
    )

    // =========================================================================
    // EXACT SURVEYED FIELD PLOTS WITH HECTARAGE LABELS FROM JAIN BLUEPRINTS
    // =========================================================================
    val SURVEYED_PLOTS: List<BlueprintPlot> = listOf(
        // === SH. RASHIDO SITE (Total Area: 1099.16 HA) ===
        // PH-12 / Drip System 29 - Exact Surveyed Parcel (MIS/610/24-25)
        BlueprintPlot(
            id = "plot_p29_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 29.974,
            label = "29.974 Ha",
            center = GeoPoint(40.444447, 68.286481),
            boundary = listOf(
                GeoPoint(40.446714, 68.285741), // NW corner by canal
                GeoPoint(40.445948, 68.288825), // NE corner
                GeoPoint(40.442180, 68.287222), // SE corner
                GeoPoint(40.442946, 68.284138)  // SW corner
            ),
            cadastralTag = "S-2-SR-109",
            cadastralHa = 23.08,
            badgePosition = GeoPoint(40.445200, 68.286600)
        ),
        // Buffer Parcel S-2-SR-110 (1.088 Ha) above System 29 along the canal
        BlueprintPlot(
            id = "plot_s2_sr_110",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 1.088,
            label = "1.088 Ha",
            center = GeoPoint(40.446700, 68.287200),
            boundary = listOf(
                GeoPoint(40.446714, 68.285741),
                GeoPoint(40.447450, 68.287100),
                GeoPoint(40.445948, 68.288825)
            ),
            cadastralTag = "S-2-SR-110",
            cadastralHa = 1.088,
            badgePosition = GeoPoint(40.446700, 68.287200)
        ),
        // Buffer Parcel S-2-SR-111 (1.497 Ha) below System 29
        BlueprintPlot(
            id = "plot_s2_sr_111",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 1.497,
            label = "1.497 Ha",
            center = GeoPoint(40.442100, 68.285200),
            boundary = listOf(
                GeoPoint(40.442946, 68.284138),
                GeoPoint(40.442180, 68.287222),
                GeoPoint(40.441600, 68.286950),
                GeoPoint(40.442366, 68.283866)
            ),
            cadastralTag = "S-2-SR-111",
            cadastralHa = 1.497,
            badgePosition = GeoPoint(40.442100, 68.285200)
        ),
        BlueprintPlot(
            id = "plot_p29_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 56.324,
            label = "56.324 Ha",
            center = GeoPoint(40.4420, 68.2870),
            boundary = listOf(
                GeoPoint(40.4390, 68.2872),
                GeoPoint(40.4465, 68.2905),
                GeoPoint(40.4470, 68.2845),
                GeoPoint(40.4410, 68.2830)
            )
        ),

        // PH-13 / Drip System 30 - Exact Surveyed Parcel (66.852 Ha, S-2-SR-113)
        BlueprintPlot(
            id = "plot_p30_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-13",
            systemNumber = 30,
            areaHa = 66.852,
            label = "66.852 Ha",
            center = GeoPoint(40.443527, 68.290184),
            boundary = listOf(
                GeoPoint(40.445948, 68.288825), // NW corner
                GeoPoint(40.444874, 68.293147), // NE corner
                GeoPoint(40.441106, 68.291544), // SE corner
                GeoPoint(40.442180, 68.287222)  // SW corner
            ),
            cadastralTag = "S-2-SR-113",
            cadastralHa = 35.82,
            badgePosition = GeoPoint(40.442800, 68.290500)
        ),
        // Buffer Parcel S-2-SR-115 (12.13 Ha) on east border of System 30
        BlueprintPlot(
            id = "plot_s2_sr_115",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-13",
            systemNumber = 30,
            areaHa = 12.13,
            label = "12.13 Ha",
            center = GeoPoint(40.442680, 68.293600),
            boundary = listOf(
                GeoPoint(40.444874, 68.293147),
                GeoPoint(40.444567, 68.294382),
                GeoPoint(40.440799, 68.292779),
                GeoPoint(40.441106, 68.291544)
            ),
            cadastralTag = "S-2-SR-115",
            cadastralHa = 12.13,
            badgePosition = GeoPoint(40.442680, 68.293600)
        ),
        BlueprintPlot(
            id = "plot_p30_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-13",
            systemNumber = 30,
            areaHa = 63.687,
            label = "63.687 Ha",
            center = GeoPoint(40.4405, 68.2960),
            boundary = listOf(
                GeoPoint(40.4365, 68.2955),
                GeoPoint(40.4445, 68.2995),
                GeoPoint(40.4460, 68.2940),
                GeoPoint(40.4385, 68.2910)
            )
        ),

        // PH-14 / Drip System 31
        BlueprintPlot(
            id = "plot_p31_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-14",
            systemNumber = 31,
            areaHa = 70.935,
            label = "70.935 Ha",
            center = GeoPoint(40.4315, 68.3010),
            boundary = listOf(
                GeoPoint(40.4233, 68.2973),
                GeoPoint(40.4325, 68.3030),
                GeoPoint(40.4345, 68.2980),
                GeoPoint(40.4250, 68.2930)
            )
        ),
        BlueprintPlot(
            id = "plot_p31_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-14",
            systemNumber = 31,
            areaHa = 48.360,
            label = "48.360 Ha",
            center = GeoPoint(40.4375, 68.3045),
            boundary = listOf(
                GeoPoint(40.4325, 68.3030),
                GeoPoint(40.4410, 68.3075),
                GeoPoint(40.4430, 68.3020),
                GeoPoint(40.4345, 68.2980)
            )
        ),

        // PH-15 / Drip Systems 32, 33, 34
        BlueprintPlot(
            id = "plot_p32_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 32,
            areaHa = 49.722,
            label = "49.722 Ha",
            center = GeoPoint(40.4245, 68.2860),
            boundary = listOf(
                GeoPoint(40.4265, 68.2912),
                GeoPoint(40.4222, 68.2817),
                GeoPoint(40.4250, 68.2790),
                GeoPoint(40.4290, 68.2870)
            )
        ),
        BlueprintPlot(
            id = "plot_p32_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 32,
            areaHa = 23.496,
            label = "23.496 Ha",
            center = GeoPoint(40.4215, 68.2840),
            boundary = listOf(
                GeoPoint(40.4222, 68.2817),
                GeoPoint(40.4190, 68.2785),
                GeoPoint(40.4215, 68.2760),
                GeoPoint(40.4250, 68.2790)
            )
        ),
        BlueprintPlot(
            id = "plot_p33_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 33,
            areaHa = 32.609,
            label = "32.609 Ha",
            center = GeoPoint(40.4195, 68.2820),
            boundary = listOf(
                GeoPoint(40.4190, 68.2785),
                GeoPoint(40.4155, 68.2780),
                GeoPoint(40.4175, 68.2840),
                GeoPoint(40.4210, 68.2845)
            )
        ),
        BlueprintPlot(
            id = "plot_p33_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 33,
            areaHa = 28.701,
            label = "28.701 Ha",
            center = GeoPoint(40.4160, 68.2810),
            boundary = listOf(
                GeoPoint(40.4155, 68.2780),
                GeoPoint(40.4120, 68.2826),
                GeoPoint(40.4140, 68.2865),
                GeoPoint(40.4175, 68.2840)
            )
        ),
        BlueprintPlot(
            id = "plot_p34_a",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 34,
            areaHa = 24.867,
            label = "24.867 Ha",
            center = GeoPoint(40.4240, 68.2930),
            boundary = listOf(
                GeoPoint(40.4265, 68.2912),
                GeoPoint(40.4215, 68.3005),
                GeoPoint(40.4195, 68.2970),
                GeoPoint(40.4245, 68.2890)
            )
        ),
        BlueprintPlot(
            id = "plot_p34_b",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 34,
            areaHa = 36.381,
            label = "36.381 Ha",
            center = GeoPoint(40.4210, 68.2950),
            boundary = listOf(
                GeoPoint(40.4215, 68.3005),
                GeoPoint(40.4165, 68.2935),
                GeoPoint(40.4185, 68.2900),
                GeoPoint(40.4230, 68.2965)
            )
        ),

        // PH-16 / Drip Systems 35, 36, 37
        BlueprintPlot(
            id = "plot_p35",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 35,
            areaHa = 59.608,
            label = "59.608 Ha",
            center = GeoPoint(40.4170, 68.2960),
            boundary = listOf(
                GeoPoint(40.4215, 68.3005),
                GeoPoint(40.4166, 68.2936),
                GeoPoint(40.4135, 68.2910),
                GeoPoint(40.4170, 68.3030)
            )
        ),
        BlueprintPlot(
            id = "plot_p36",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 36,
            areaHa = 55.462,
            label = "55.462 Ha",
            center = GeoPoint(40.4130, 68.2940),
            boundary = listOf(
                GeoPoint(40.4166, 68.2936),
                GeoPoint(40.4101, 68.2880),
                GeoPoint(40.4072, 68.2922),
                GeoPoint(40.4135, 68.2910)
            )
        ),
        BlueprintPlot(
            id = "plot_p37",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 37,
            areaHa = 71.779,
            label = "71.779 Ha",
            center = GeoPoint(40.4135, 68.3040),
            boundary = listOf(
                GeoPoint(40.4170, 68.3030),
                GeoPoint(40.4100, 68.2990),
                GeoPoint(40.4078, 68.2972),
                GeoPoint(40.4130, 68.3090)
            )
        ),

        // PH-17 / Drip Systems 38, 39, 40
        BlueprintPlot(
            id = "plot_p38",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-17",
            systemNumber = 38,
            areaHa = 68.965,
            label = "68.965 Ha",
            center = GeoPoint(40.4080, 68.3020),
            boundary = listOf(
                GeoPoint(40.4100, 68.2990),
                GeoPoint(40.4021, 68.2955),
                GeoPoint(40.4050, 68.3060),
                GeoPoint(40.4130, 68.3090)
            )
        ),
        BlueprintPlot(
            id = "plot_p39_40",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-17",
            systemNumber = 39,
            areaHa = 77.011,
            label = "77.011 Ha",
            center = GeoPoint(40.4020, 68.2990),
            boundary = listOf(
                GeoPoint(40.4050, 68.3060),
                GeoPoint(40.3980, 68.3008),
                GeoPoint(40.4021, 68.2955),
                GeoPoint(40.4060, 68.2980)
            )
        ),

        // PH-18 / Drip Systems 41, 42, 43
        BlueprintPlot(
            id = "plot_p41",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-18",
            systemNumber = 41,
            areaHa = 81.610,
            label = "81.610 Ha",
            center = GeoPoint(40.4290, 68.3210),
            boundary = listOf(
                GeoPoint(40.4244, 68.3231),
                GeoPoint(40.4342, 68.3219),
                GeoPoint(40.4327, 68.3263),
                GeoPoint(40.4250, 68.3280)
            )
        ),
        BlueprintPlot(
            id = "plot_p42_43",
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-18",
            systemNumber = 42,
            areaHa = 66.651,
            label = "66.651 Ha",
            center = GeoPoint(40.4190, 68.3160),
            boundary = listOf(
                GeoPoint(40.4244, 68.3231),
                GeoPoint(40.4159, 68.3118),
                GeoPoint(40.4175, 68.3128),
                GeoPoint(40.4253, 68.3210)
            )
        ),

        // === T. MALIK (SARDOBA SITE 2) (Total Area: 649.50 HA) ===
        // PH-7 / System 24
        BlueprintPlot(
            id = "plot_p24_a",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-7",
            systemNumber = 24,
            areaHa = 52.913,
            label = "52.913 Ha",
            center = GeoPoint(40.4260, 68.3440),
            boundary = listOf(
                GeoPoint(40.4215, 68.3444),
                GeoPoint(40.4300, 68.3465),
                GeoPoint(40.4310, 68.3415),
                GeoPoint(40.4225, 68.3395)
            )
        ),
        BlueprintPlot(
            id = "plot_p24_b",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-7",
            systemNumber = 24,
            areaHa = 62.339,
            label = "62.339 Ha",
            center = GeoPoint(40.4335, 68.3470),
            boundary = listOf(
                GeoPoint(40.4300, 68.3465),
                GeoPoint(40.4363, 68.3490),
                GeoPoint(40.4375, 68.3435),
                GeoPoint(40.4310, 68.3415)
            )
        ),

        // PH-8 / System 25
        BlueprintPlot(
            id = "plot_p25",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-8",
            systemNumber = 25,
            areaHa = 59.156,
            label = "59.156 Ha",
            center = GeoPoint(40.4270, 68.3555),
            boundary = listOf(
                GeoPoint(40.4197, 68.3535),
                GeoPoint(40.4334, 68.3545),
                GeoPoint(40.4345, 68.3585),
                GeoPoint(40.4210, 68.3570)
            )
        ),

        // PH-9 / System 26
        BlueprintPlot(
            id = "plot_p26",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-9",
            systemNumber = 26,
            areaHa = 72.555,
            label = "72.555 Ha",
            center = GeoPoint(40.4255, 68.3645),
            boundary = listOf(
                GeoPoint(40.4176, 68.3623),
                GeoPoint(40.4323, 68.3644),
                GeoPoint(40.4330, 68.3685),
                GeoPoint(40.4190, 68.3665)
            )
        ),

        // PH-10 / System 27
        BlueprintPlot(
            id = "plot_p27_a",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-10",
            systemNumber = 27,
            areaHa = 118.963,
            label = "118.963 Ha",
            center = GeoPoint(40.4110, 68.3600),
            boundary = listOf(
                GeoPoint(40.4182, 68.3618),
                GeoPoint(40.4050, 68.3576),
                GeoPoint(40.4075, 68.3640),
                GeoPoint(40.4195, 68.3670)
            )
        ),
        BlueprintPlot(
            id = "plot_p27_b",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-10",
            systemNumber = 27,
            areaHa = 71.060,
            label = "71.060 Ha",
            center = GeoPoint(40.4035, 68.3570),
            boundary = listOf(
                GeoPoint(40.4050, 68.3576),
                GeoPoint(40.4014, 68.3561),
                GeoPoint(40.4030, 68.3615),
                GeoPoint(40.4075, 68.3640)
            )
        ),

        // PH-11 / System 28
        BlueprintPlot(
            id = "plot_p28_a",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-11",
            systemNumber = 28,
            areaHa = 70.764,
            label = "70.764 Ha",
            center = GeoPoint(40.4080, 68.3735),
            boundary = listOf(
                GeoPoint(40.4162, 68.3715),
                GeoPoint(40.4052, 68.3718),
                GeoPoint(40.4070, 68.3775),
                GeoPoint(40.4175, 68.3765)
            )
        ),
        BlueprintPlot(
            id = "plot_p28_b",
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-11",
            systemNumber = 28,
            areaHa = 141.758,
            label = "141.758 Ha",
            center = GeoPoint(40.4000, 68.3690),
            boundary = listOf(
                GeoPoint(40.4052, 68.3718),
                GeoPoint(40.3963, 68.3679),
                GeoPoint(40.3980, 68.3745),
                GeoPoint(40.4070, 68.3775)
            )
        )
    )

    // =========================================================================
    // CIVIL STATION BUILDINGS & STRUCTURES (Water Storage, Sedimentation, Tanks)
    // =========================================================================
    fun getCivilStructuresForStation(
        phId: String,
        baseLat: Double,
        baseLng: Double,
        buildingDim: String = "15.0m x 10.0m"
    ): List<CivilBuilding> {
        val cosLat = kotlin.math.cos(Math.toRadians(baseLat))
        val latPerMeter = 1.0 / 111040.0
        val lngPerMeter = 1.0 / (111320.0 * cosLat)

        return listOf(
            // 1. Pump House Station Building (15m x 10m / 12m x 13.8-16.3m)
            CivilBuilding(
                id = "${phId}_bldg",
                pumpHouseId = phId,
                type = CivilType.PUMP_STATION,
                label = "$phId Station ($buildingDim)",
                center = GeoPoint(baseLat, baseLng),
                widthMeters = 15.0,
                lengthMeters = 10.0
            ),
            // 2. Fertigation Chemical Station (82 m³ Tank) - placed 14m south
            CivilBuilding(
                id = "${phId}_fertigation",
                pumpHouseId = phId,
                type = CivilType.FERTIGATION,
                label = "Fertigation Tank 82 m³",
                center = GeoPoint(baseLat - (14.0 * latPerMeter), baseLng),
                widthMeters = 6.0,
                lengthMeters = 8.0
            ),
            // 3. Intake Pipe From Canal - LaToc - pipe line from Canal to Station
            CivilBuilding(
                id = "${phId}_intake",
                pumpHouseId = phId,
                type = CivilType.INTAKE_LINE,
                label = "Canal Intake Pipe (400mm)",
                center = GeoPoint(baseLat, baseLng - (35.0 * lngPerMeter)),
                widthMeters = 3.0,
                lengthMeters = 25.0
            )
        )
    }

    /**
     * Generates tentative planting machine guidance route lines spaced every 10.8m
     * inside the given surveyed plot, representing the machine route plan from the drawing.
     */
    fun getPlantingMachineRouteLines(plot: BlueprintPlot): List<Pair<GeoPoint, GeoPoint>> {
        val b = plot.boundary
        if (b.size < 4) return emptyList()

        val p1 = b[0]
        val p2 = b[1]
        val p3 = b[2]
        val p4 = b[3]

        val routes = mutableListOf<Pair<GeoPoint, GeoPoint>>()
        // Draw 6 representative 10.8m parallel tractor routes across each plot
        for (step in 1..5) {
            val fraction = step / 6.0
            val startLat = p1.latitude + fraction * (p4.latitude - p1.latitude)
            val startLng = p1.longitude + fraction * (p4.longitude - p1.longitude)

            val endLat = p2.latitude + fraction * (p3.latitude - p2.latitude)
            val endLng = p2.longitude + fraction * (p3.longitude - p2.longitude)

            routes.add(Pair(GeoPoint(startLat, startLng), GeoPoint(endLat, endLng)))
        }
        return routes
    }

    // =========================================================================
    // ELEVATION CONTOURS (From Jain Irrigation Drawing Topography: 278.500m to 283.000m)
    // =========================================================================
    val ELEVATION_CONTOURS: List<ContourLine> = listOf(
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(
                GeoPoint(40.4485, 68.2810),
                GeoPoint(40.4470, 68.2930),
                GeoPoint(40.4440, 68.3070),
                GeoPoint(40.4390, 68.3240),
                GeoPoint(40.4370, 68.3480),
                GeoPoint(40.4330, 68.3750)
            )
        ),
        ContourLine(
            elevationMeters = 279.000,
            label = "279.000 m",
            points = listOf(
                GeoPoint(40.4435, 68.2800),
                GeoPoint(40.4415, 68.2910),
                GeoPoint(40.4385, 68.3050),
                GeoPoint(40.4335, 68.3220),
                GeoPoint(40.4310, 68.3470),
                GeoPoint(40.4270, 68.3730)
            )
        ),
        ContourLine(
            elevationMeters = 279.500,
            label = "279.500 m",
            points = listOf(
                GeoPoint(40.4385, 68.2790),
                GeoPoint(40.4360, 68.2890),
                GeoPoint(40.4330, 68.3030),
                GeoPoint(40.4280, 68.3200),
                GeoPoint(40.4250, 68.3450),
                GeoPoint(40.4210, 68.3710)
            )
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(
                GeoPoint(40.4320, 68.2780),
                GeoPoint(40.4295, 68.2880),
                GeoPoint(40.4260, 68.3010),
                GeoPoint(40.4215, 68.3180),
                GeoPoint(40.4185, 68.3430),
                GeoPoint(40.4140, 68.3690)
            )
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(
                GeoPoint(40.4260, 68.2770),
                GeoPoint(40.4230, 68.2865),
                GeoPoint(40.4195, 68.2995),
                GeoPoint(40.4150, 68.3160),
                GeoPoint(40.4120, 68.3410),
                GeoPoint(40.4075, 68.3670)
            )
        ),
        ContourLine(
            elevationMeters = 281.500,
            label = "281.500 m",
            points = listOf(
                GeoPoint(40.4200, 68.2760),
                GeoPoint(40.4165, 68.2850),
                GeoPoint(40.4130, 68.2980),
                GeoPoint(40.4085, 68.3140),
                GeoPoint(40.4055, 68.3390),
                GeoPoint(40.4010, 68.3650)
            )
        ),
        ContourLine(
            elevationMeters = 282.500,
            label = "282.500 m",
            points = listOf(
                GeoPoint(40.4130, 68.2750),
                GeoPoint(40.4095, 68.2835),
                GeoPoint(40.4060, 68.2965),
                GeoPoint(40.4015, 68.3120),
                GeoPoint(40.3985, 68.3370),
                GeoPoint(40.3940, 68.3630)
            )
        ),
        ContourLine(
            elevationMeters = 283.000,
            label = "283.000 m",
            points = listOf(
                GeoPoint(40.4060, 68.2740),
                GeoPoint(40.4025, 68.2820),
                GeoPoint(40.3990, 68.2950),
                GeoPoint(40.3945, 68.3100),
                GeoPoint(40.3915, 68.3350),
                GeoPoint(40.3870, 68.3610)
            )
        )
    )
}

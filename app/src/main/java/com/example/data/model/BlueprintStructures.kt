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
            id = "plot_p29_a", // GEOMETRY = real CAD block R:kml_381 (29.97 ha). Pipe-network trace: PH-12. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 29.974,
            label = "29.974 Ha",
            center = GeoPoint(40.4433690, 68.2859303),
            boundary = listOf(
                GeoPoint(40.4406454, 68.2821835),
                GeoPoint(40.4420213, 68.2828755),
                GeoPoint(40.4421871, 68.2829700),
                GeoPoint(40.4445895, 68.2841502),
                GeoPoint(40.4459970, 68.2848507),
                GeoPoint(40.4475301, 68.2856378),
                GeoPoint(40.4472880, 68.2861351),
                GeoPoint(40.4471125, 68.2869943),
                GeoPoint(40.4468995, 68.2874681),
                GeoPoint(40.4465439, 68.2878179),
                GeoPoint(40.4459899, 68.2886818),
                GeoPoint(40.4456126, 68.2894344),
                GeoPoint(40.4438368, 68.2886338),
                GeoPoint(40.4400781, 68.2868695),
                GeoPoint(40.4394986, 68.2865194),
                GeoPoint(40.4399312, 68.2851631)
            ),
            cadastralTag = "S-2-SR-109",
            cadastralHa = 23.08,
            badgePosition = GeoPoint(40.4430120, 68.2858109)
        ),
        // CAD block R:kml_208 – not present in the old app data. Pipe-network trace: unresolved
        BlueprintPlot(
            id = "plot_cad_kml_208",
            siteName = "Sh. Rashido",
            pumpHouseId = "UNASSIGNED",
            systemNumber = 0,
            areaHa = 57.070,
            label = "57.07 Ha",
            center = GeoPoint(40.4237152, 68.2797633),
            boundary = listOf(
                GeoPoint(40.4208062, 68.2798263),
                GeoPoint(40.4228615, 68.2817597),
                GeoPoint(40.4245880, 68.2832825),
                GeoPoint(40.4281963, 68.2865757),
                GeoPoint(40.4291722, 68.2847556),
                GeoPoint(40.4299515, 68.2833280),
                GeoPoint(40.4300983, 68.2829638),
                GeoPoint(40.4301432, 68.2827661),
                GeoPoint(40.4283023, 68.2810649),
                GeoPoint(40.4243662, 68.2775958),
                GeoPoint(40.4237707, 68.2771711),
                GeoPoint(40.4237318, 68.2770105),
                GeoPoint(40.4228862, 68.2761310),
                GeoPoint(40.4227274, 68.2762225),
                GeoPoint(40.4216358, 68.2751198),
                GeoPoint(40.4199009, 68.2736321),
                GeoPoint(40.4194751, 68.2733670),
                GeoPoint(40.4192232, 68.2733710),
                GeoPoint(40.4171912, 68.2759530),
                GeoPoint(40.4168520, 68.2762935),
                GeoPoint(40.4191834, 68.2783133)
            ),
            badgePosition = GeoPoint(40.4233090, 68.2793626)
        ),
        // CAD block R:kml_334 – not present in the old app data. Pipe-network trace: unresolved
        BlueprintPlot(
            id = "plot_cad_kml_334",
            siteName = "Sh. Rashido",
            pumpHouseId = "UNASSIGNED",
            systemNumber = 0,
            areaHa = 29.159,
            label = "29.16 Ha",
            center = GeoPoint(40.4120063, 68.3075333),
            boundary = listOf(
                GeoPoint(40.4152246, 68.3103659),
                GeoPoint(40.4156926, 68.3095046),
                GeoPoint(40.4159573, 68.3089570),
                GeoPoint(40.4159211, 68.3087899),
                GeoPoint(40.4156212, 68.3084656),
                GeoPoint(40.4136591, 68.3065696),
                GeoPoint(40.4120407, 68.3050358),
                GeoPoint(40.4115832, 68.3046456),
                GeoPoint(40.4099577, 68.3031995),
                GeoPoint(40.4096360, 68.3031452),
                GeoPoint(40.4089086, 68.3046704),
                GeoPoint(40.4080155, 68.3064185),
                GeoPoint(40.4113032, 68.3093622),
                GeoPoint(40.4118630, 68.3098492),
                GeoPoint(40.4142306, 68.3120004),
                GeoPoint(40.4143783, 68.3118166)
            ),
            badgePosition = GeoPoint(40.4119519, 68.3074450)
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
            id = "plot_p29_b", // GEOMETRY = real CAD block R:kml_270 (56.32 ha). Pipe-network trace: PH-16. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-12",
            systemNumber = 29,
            areaHa = 56.324,
            label = "56.324 Ha",
            center = GeoPoint(40.4142670, 68.2938339),
            boundary = listOf(
                GeoPoint(40.4149281, 68.2920428),
                GeoPoint(40.4140625, 68.2913237),
                GeoPoint(40.4134584, 68.2908415),
                GeoPoint(40.4125558, 68.2900733),
                GeoPoint(40.4098009, 68.2875797),
                GeoPoint(40.4095006, 68.2873535),
                GeoPoint(40.4088267, 68.2866696),
                GeoPoint(40.4067262, 68.2894062),
                GeoPoint(40.4068041, 68.2894891),
                GeoPoint(40.4085619, 68.2910269),
                GeoPoint(40.4108610, 68.2930757),
                GeoPoint(40.4115796, 68.2937260),
                GeoPoint(40.4125357, 68.2946279),
                GeoPoint(40.4167974, 68.2983624),
                GeoPoint(40.4178825, 68.2993607),
                GeoPoint(40.4181793, 68.2997454),
                GeoPoint(40.4197024, 68.3011593),
                GeoPoint(40.4207191, 68.2993134),
                GeoPoint(40.4213337, 68.2982370),
                GeoPoint(40.4213970, 68.2980637),
                GeoPoint(40.4213669, 68.2979071),
                GeoPoint(40.4213013, 68.2977749),
                GeoPoint(40.4206261, 68.2970916),
                GeoPoint(40.4185191, 68.2951857),
                GeoPoint(40.4161847, 68.2931722)
            )
        ),

        // PH-13 / Drip System 30 - Exact Surveyed Parcel (66.852 Ha, S-2-SR-113)
        BlueprintPlot(
            id = "plot_p30_a", // GEOMETRY = real CAD block R:kml_380 (66.85 ha). Pipe-network trace: PH-12. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-13",
            systemNumber = 30,
            areaHa = 66.852,
            label = "66.852 Ha",
            center = GeoPoint(40.4380514, 68.2890608),
            boundary = listOf(
                GeoPoint(40.4376691, 68.2866624),
                GeoPoint(40.4346578, 68.2852358),
                GeoPoint(40.4319632, 68.2839925),
                GeoPoint(40.4309169, 68.2834703),
                GeoPoint(40.4303351, 68.2844498),
                GeoPoint(40.4300656, 68.2849406),
                GeoPoint(40.4290204, 68.2869268),
                GeoPoint(40.4337525, 68.2891555),
                GeoPoint(40.4425964, 68.2934521),
                GeoPoint(40.4451698, 68.2947622),
                GeoPoint(40.4453693, 68.2948424),
                GeoPoint(40.4454865, 68.2946517),
                GeoPoint(40.4459923, 68.2929716),
                GeoPoint(40.4463920, 68.2915646),
                GeoPoint(40.4464901, 68.2910688),
                GeoPoint(40.4465570, 68.2908499),
                GeoPoint(40.4452174, 68.2901952)
            ),
            cadastralTag = "S-2-SR-113",
            cadastralHa = 35.82,
            badgePosition = GeoPoint(40.4401328, 68.2900355)
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
            id = "plot_p30_b", // GEOMETRY = real CAD block R:kml_269 (63.68 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-13",
            systemNumber = 30,
            areaHa = 63.687,
            label = "63.687 Ha",
            center = GeoPoint(40.4119796, 68.2971055),
            boundary = listOf(
                GeoPoint(40.4086010, 68.2966039),
                GeoPoint(40.4097814, 68.2976455),
                GeoPoint(40.4114126, 68.2991229),
                GeoPoint(40.4143654, 68.3017442),
                GeoPoint(40.4161421, 68.3034437),
                GeoPoint(40.4171771, 68.3043879),
                GeoPoint(40.4175847, 68.3046733),
                GeoPoint(40.4180244, 68.3042623),
                GeoPoint(40.4185689, 68.3030590),
                GeoPoint(40.4194148, 68.3015645),
                GeoPoint(40.4195031, 68.3013966),
                GeoPoint(40.4156449, 68.2977934),
                GeoPoint(40.4152558, 68.2974614),
                GeoPoint(40.4124895, 68.2948189),
                GeoPoint(40.4123104, 68.2949594),
                GeoPoint(40.4104910, 68.2932057),
                GeoPoint(40.4089069, 68.2917598),
                GeoPoint(40.4064850, 68.2896633),
                GeoPoint(40.4042375, 68.2926359),
                GeoPoint(40.4064173, 68.2946977)
            )
        ),

        // PH-14 / Drip System 31
        BlueprintPlot(
            id = "plot_p31_a", // GEOMETRY = real CAD block R:kml_161 (70.93 ha). Pipe-network trace: PH-13, PH-15. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-14",
            systemNumber = 31,
            areaHa = 70.935,
            label = "70.935 Ha",
            center = GeoPoint(40.4362948, 68.2931374),
            boundary = listOf(
                GeoPoint(40.4295102, 68.2875545),
                GeoPoint(40.4291638, 68.2873572),
                GeoPoint(40.4290258, 68.2873298),
                GeoPoint(40.4287403, 68.2878714),
                GeoPoint(40.4282181, 68.2888194),
                GeoPoint(40.4271212, 68.2908548),
                GeoPoint(40.4280842, 68.2912888),
                GeoPoint(40.4305720, 68.2924865),
                GeoPoint(40.4307211, 68.2927177),
                GeoPoint(40.4323448, 68.2935096),
                GeoPoint(40.4349306, 68.2948064),
                GeoPoint(40.4350502, 68.2949045),
                GeoPoint(40.4367501, 68.2957452),
                GeoPoint(40.4369799, 68.2959579),
                GeoPoint(40.4390268, 68.2970137),
                GeoPoint(40.4391709, 68.2968427),
                GeoPoint(40.4435499, 68.2989040),
                GeoPoint(40.4437287, 68.2985677),
                GeoPoint(40.4444662, 68.2968707),
                GeoPoint(40.4451903, 68.2951140),
                GeoPoint(40.4439294, 68.2944681),
                GeoPoint(40.4376910, 68.2915136),
                GeoPoint(40.4361105, 68.2907379),
                GeoPoint(40.4338365, 68.2896721),
                GeoPoint(40.4323424, 68.2889557),
                GeoPoint(40.4316717, 68.2885597)
            )
        ),
        BlueprintPlot(
            id = "plot_p31_b", // GEOMETRY = real CAD block R:kml_271 (48.36 ha). Pipe-network trace: PH-16. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-14",
            systemNumber = 31,
            areaHa = 48.360,
            label = "48.360 Ha",
            center = GeoPoint(40.4155064, 68.2890986),
            boundary = listOf(
                GeoPoint(40.4208360, 68.2913380),
                GeoPoint(40.4138382, 68.2850046),
                GeoPoint(40.4116070, 68.2830522),
                GeoPoint(40.4111792, 68.2835856),
                GeoPoint(40.4095187, 68.2857557),
                GeoPoint(40.4093566, 68.2860328),
                GeoPoint(40.4105265, 68.2871294),
                GeoPoint(40.4122224, 68.2886969),
                GeoPoint(40.4168936, 68.2927913),
                GeoPoint(40.4196353, 68.2953309),
                GeoPoint(40.4197444, 68.2952839),
                GeoPoint(40.4207295, 68.2937199),
                GeoPoint(40.4214256, 68.2925938),
                GeoPoint(40.4206632, 68.2919173),
                GeoPoint(40.4207398, 68.2917001),
                GeoPoint(40.4208374, 68.2914875)
            )
        ),

        // PH-15 / Drip Systems 32, 33, 34
        BlueprintPlot(
            id = "plot_p32_a", // GEOMETRY = real CAD block R:kml_63 (49.72 ha). Pipe-network trace: PH-18. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 32,
            areaHa = 49.722,
            label = "49.722 Ha",
            center = GeoPoint(40.4264797, 68.3218975),
            boundary = listOf(
                GeoPoint(40.4229919, 68.3183651),
                GeoPoint(40.4231033, 68.3180173),
                GeoPoint(40.4216553, 68.3171234),
                GeoPoint(40.4193973, 68.3156691),
                GeoPoint(40.4171654, 68.3143598),
                GeoPoint(40.4170137, 68.3141835),
                GeoPoint(40.4165277, 68.3139184),
                GeoPoint(40.4159508, 68.3154367),
                GeoPoint(40.4159109, 68.3155975),
                GeoPoint(40.4167748, 68.3160934),
                GeoPoint(40.4178977, 68.3167689),
                GeoPoint(40.4199454, 68.3180539),
                GeoPoint(40.4230614, 68.3197972),
                GeoPoint(40.4251755, 68.3210741),
                GeoPoint(40.4241335, 68.3240394),
                GeoPoint(40.4262703, 68.3247697),
                GeoPoint(40.4341444, 68.3275653),
                GeoPoint(40.4349590, 68.3253567),
                GeoPoint(40.4345099, 68.3250640),
                GeoPoint(40.4321267, 68.3236514),
                GeoPoint(40.4259223, 68.3200196),
                GeoPoint(40.4256979, 68.3199841),
                GeoPoint(40.4238508, 68.3189844)
            )
        ),
        BlueprintPlot(
            id = "plot_p32_b", // GEOMETRY = real CAD block R:kml_330 (23.50 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 32,
            areaHa = 23.496,
            label = "23.496 Ha",
            center = GeoPoint(40.4104037, 68.3109254),
            boundary = listOf(
                GeoPoint(40.4126017, 68.3108251),
                GeoPoint(40.4090629, 68.3076853),
                GeoPoint(40.4087145, 68.3077856),
                GeoPoint(40.4086022, 68.3077583),
                GeoPoint(40.4082315, 68.3074546),
                GeoPoint(40.4078109, 68.3070294),
                GeoPoint(40.4067565, 68.3089944),
                GeoPoint(40.4064245, 68.3095082),
                GeoPoint(40.4071959, 68.3101258),
                GeoPoint(40.4080323, 68.3108242),
                GeoPoint(40.4097066, 68.3122928),
                GeoPoint(40.4117614, 68.3140734),
                GeoPoint(40.4128169, 68.3149658),
                GeoPoint(40.4136386, 68.3134831),
                GeoPoint(40.4140454, 68.3126883),
                GeoPoint(40.4141655, 68.3124068),
                GeoPoint(40.4141703, 68.3121983),
                GeoPoint(40.4140029, 68.3120130)
            )
        ),
        BlueprintPlot(
            id = "plot_p33_a", // GEOMETRY = real CAD block R:kml_331 (32.61 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 33,
            areaHa = 32.609,
            label = "32.609 Ha",
            center = GeoPoint(40.4026656, 68.3039715),
            boundary = listOf(
                GeoPoint(40.4007418, 68.3002121),
                GeoPoint(40.3995112, 68.2991093),
                GeoPoint(40.3994113, 68.2990786),
                GeoPoint(40.3990885, 68.2993842),
                GeoPoint(40.3985381, 68.3000018),
                GeoPoint(40.3982268, 68.3004540),
                GeoPoint(40.3975036, 68.3013627),
                GeoPoint(40.4003109, 68.3038889),
                GeoPoint(40.4011774, 68.3046878),
                GeoPoint(40.4033665, 68.3066541),
                GeoPoint(40.4043452, 68.3075152),
                GeoPoint(40.4059771, 68.3090035),
                GeoPoint(40.4061162, 68.3091115),
                GeoPoint(40.4072612, 68.3069479),
                GeoPoint(40.4075439, 68.3062850),
                GeoPoint(40.4073111, 68.3060295),
                GeoPoint(40.4063135, 68.3051290),
                GeoPoint(40.4040681, 68.3031245)
            )
        ),
        BlueprintPlot(
            id = "plot_p33_b", // GEOMETRY = real CAD block R:kml_332 (28.70 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 33,
            areaHa = 28.701,
            label = "28.701 Ha",
            center = GeoPoint(40.4064812, 68.2976230),
            boundary = listOf(
                GeoPoint(40.4100988, 68.2987991),
                GeoPoint(40.4059709, 68.2949889),
                GeoPoint(40.4039184, 68.2931911),
                GeoPoint(40.4037949, 68.2931681),
                GeoPoint(40.4020972, 68.2954347),
                GeoPoint(40.4020065, 68.2956160),
                GeoPoint(40.4023100, 68.2959560),
                GeoPoint(40.4047445, 68.2981997),
                GeoPoint(40.4087178, 68.3016446),
                GeoPoint(40.4093740, 68.3022534),
                GeoPoint(40.4095154, 68.3021698),
                GeoPoint(40.4108178, 68.2995649),
                GeoPoint(40.4105175, 68.2992163),
                GeoPoint(40.4103040, 68.2990115)
            )
        ),
        BlueprintPlot(
            id = "plot_p34_a", // GEOMETRY = real CAD block R:kml_333 (24.87 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 34,
            areaHa = 24.867,
            label = "24.867 Ha",
            center = GeoPoint(40.4136716, 68.3041481),
            boundary = listOf(
                GeoPoint(40.4130745, 68.3014909),
                GeoPoint(40.4110236, 68.2996957),
                GeoPoint(40.4109556, 68.2997729),
                GeoPoint(40.4109118, 68.3001060),
                GeoPoint(40.4104589, 68.3010350),
                GeoPoint(40.4100058, 68.3021311),
                GeoPoint(40.4097799, 68.3026012),
                GeoPoint(40.4099358, 68.3027851),
                GeoPoint(40.4109627, 68.3036900),
                GeoPoint(40.4130023, 68.3055234),
                GeoPoint(40.4140575, 68.3065170),
                GeoPoint(40.4159091, 68.3082868),
                GeoPoint(40.4161911, 68.3084980),
                GeoPoint(40.4165849, 68.3078849),
                GeoPoint(40.4169468, 68.3072504),
                GeoPoint(40.4175235, 68.3060561),
                GeoPoint(40.4175180, 68.3059097),
                GeoPoint(40.4175590, 68.3057580),
                GeoPoint(40.4171755, 68.3053869),
                GeoPoint(40.4165888, 68.3048428),
                GeoPoint(40.4142566, 68.3027084),
                GeoPoint(40.4139723, 68.3025053),
                GeoPoint(40.4137719, 68.3022572)
            )
        ),
        BlueprintPlot(
            id = "plot_p34_b", // GEOMETRY = real CAD block R:kml_335 (36.38 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-15",
            systemNumber = 34,
            areaHa = 36.381,
            label = "36.381 Ha",
            center = GeoPoint(40.4045572, 68.3008596),
            boundary = listOf(
                GeoPoint(40.4015748, 68.2961130),
                GeoPoint(40.4007745, 68.2970732),
                GeoPoint(40.4003400, 68.2977939),
                GeoPoint(40.3996822, 68.2987434),
                GeoPoint(40.3998253, 68.2990642),
                GeoPoint(40.4007271, 68.2998378),
                GeoPoint(40.4028830, 68.3018070),
                GeoPoint(40.4046796, 68.3034154),
                GeoPoint(40.4064068, 68.3049917),
                GeoPoint(40.4077173, 68.3059924),
                GeoPoint(40.4092855, 68.3029122),
                GeoPoint(40.4093046, 68.3027096),
                GeoPoint(40.4036983, 68.2976190),
                GeoPoint(40.4035290, 68.2973531),
                GeoPoint(40.4020001, 68.2959595),
                GeoPoint(40.4018463, 68.2958782),
                GeoPoint(40.4017321, 68.2959478)
            )
        ),

        // PH-16 / Drip Systems 35, 36, 37
        BlueprintPlot(
            id = "plot_p35", // GEOMETRY = real CAD block R:kml_206 (59.61 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 35,
            areaHa = 59.608,
            label = "59.608 Ha",
            center = GeoPoint(40.4190637, 68.2868446),
            boundary = listOf(
                GeoPoint(40.4170210, 68.2824078),
                GeoPoint(40.4161995, 68.2816926),
                GeoPoint(40.4142588, 68.2798619),
                GeoPoint(40.4141438, 68.2798372),
                GeoPoint(40.4130923, 68.2813019),
                GeoPoint(40.4120911, 68.2826005),
                GeoPoint(40.4118908, 68.2829655),
                GeoPoint(40.4150477, 68.2858222),
                GeoPoint(40.4169507, 68.2875032),
                GeoPoint(40.4196284, 68.2899802),
                GeoPoint(40.4232929, 68.2933281),
                GeoPoint(40.4240407, 68.2939101),
                GeoPoint(40.4242320, 68.2938070),
                GeoPoint(40.4249706, 68.2925008),
                GeoPoint(40.4259597, 68.2905581),
                GeoPoint(40.4238851, 68.2884812),
                GeoPoint(40.4177519, 68.2830240)
            )
        ),
        BlueprintPlot(
            id = "plot_p36", // GEOMETRY = real CAD block R:kml_207 (55.46 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 36,
            areaHa = 55.462,
            label = "55.462 Ha",
            center = GeoPoint(40.4212639, 68.2832641),
            boundary = listOf(
                GeoPoint(40.4146734, 68.2796889),
                GeoPoint(40.4146673, 68.2798172),
                GeoPoint(40.4174649, 68.2824417),
                GeoPoint(40.4184013, 68.2833368),
                GeoPoint(40.4191944, 68.2839287),
                GeoPoint(40.4231100, 68.2873189),
                GeoPoint(40.4246572, 68.2887709),
                GeoPoint(40.4260545, 68.2903683),
                GeoPoint(40.4270270, 68.2887125),
                GeoPoint(40.4280555, 68.2869094),
                GeoPoint(40.4222190, 68.2815855),
                GeoPoint(40.4166463, 68.2764779),
                GeoPoint(40.4155021, 68.2780381),
                GeoPoint(40.4144154, 68.2794267)
            )
        ),
        BlueprintPlot(
            id = "plot_p37", // GEOMETRY = real CAD block R:kml_162 (71.78 ha). Pipe-network trace: PH-13, PH-15. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-16",
            systemNumber = 37,
            areaHa = 71.779,
            label = "71.779 Ha",
            center = GeoPoint(40.4340638, 68.2968829),
            boundary = listOf(
                GeoPoint(40.4291066, 68.2919912),
                GeoPoint(40.4277641, 68.2913731),
                GeoPoint(40.4269785, 68.2909769),
                GeoPoint(40.4268880, 68.2909577),
                GeoPoint(40.4266366, 68.2915883),
                GeoPoint(40.4261208, 68.2926137),
                GeoPoint(40.4250732, 68.2944210),
                GeoPoint(40.4253550, 68.2946718),
                GeoPoint(40.4275922, 68.2959602),
                GeoPoint(40.4302402, 68.2973232),
                GeoPoint(40.4325399, 68.2984697),
                GeoPoint(40.4330564, 68.2986977),
                GeoPoint(40.4351550, 68.2998440),
                GeoPoint(40.4383593, 68.3014498),
                GeoPoint(40.4385516, 68.3014998),
                GeoPoint(40.4386441, 68.3011572),
                GeoPoint(40.4421219, 68.3029260),
                GeoPoint(40.4427661, 68.3010231),
                GeoPoint(40.4429114, 68.3003622),
                GeoPoint(40.4430618, 68.2998478),
                GeoPoint(40.4430965, 68.2996891),
                GeoPoint(40.4430678, 68.2995422),
                GeoPoint(40.4429569, 68.2994504),
                GeoPoint(40.4427084, 68.2992809),
                GeoPoint(40.4393017, 68.2977376),
                GeoPoint(40.4392508, 68.2975660),
                GeoPoint(40.4392550, 68.2975442),
                GeoPoint(40.4392222, 68.2975254),
                GeoPoint(40.4391415, 68.2973624),
                GeoPoint(40.4390353, 68.2970230),
                GeoPoint(40.4374459, 68.2962217),
                GeoPoint(40.4370020, 68.2959733),
                GeoPoint(40.4367364, 68.2957485),
                GeoPoint(40.4350502, 68.2949045),
                GeoPoint(40.4349306, 68.2948064),
                GeoPoint(40.4323448, 68.2935096),
                GeoPoint(40.4318097, 68.2932611),
                GeoPoint(40.4306546, 68.2926891)
            )
        ),

        // PH-17 / Drip Systems 38, 39, 40
        BlueprintPlot(
            id = "plot_p38", // GEOMETRY = real CAD block R:kml_116 (68.96 ha). Pipe-network trace: PH-14. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-17",
            systemNumber = 38,
            areaHa = 68.965,
            label = "68.965 Ha",
            center = GeoPoint(40.4325376, 68.3008942),
            boundary = listOf(
                GeoPoint(40.4242206, 68.2959430),
                GeoPoint(40.4230339, 68.2981691),
                GeoPoint(40.4239889, 68.2986312),
                GeoPoint(40.4263310, 68.2998705),
                GeoPoint(40.4291257, 68.3013080),
                GeoPoint(40.4324242, 68.3028994),
                GeoPoint(40.4369802, 68.3052756),
                GeoPoint(40.4405337, 68.3070843),
                GeoPoint(40.4407233, 68.3067191),
                GeoPoint(40.4418374, 68.3033824),
                GeoPoint(40.4409161, 68.3029352),
                GeoPoint(40.4394578, 68.3022918),
                GeoPoint(40.4392467, 68.3026760),
                GeoPoint(40.4379308, 68.3019641),
                GeoPoint(40.4380776, 68.3015468),
                GeoPoint(40.4357385, 68.3003143),
                GeoPoint(40.4332014, 68.2990082),
                GeoPoint(40.4288136, 68.2968598),
                GeoPoint(40.4251210, 68.2950284),
                GeoPoint(40.4247885, 68.2949632)
            )
        ),
        BlueprintPlot(
            id = "plot_p39_40", // GEOMETRY = real CAD block R:kml_117 (77.01 ha). Pipe-network trace: PH-14. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-17",
            systemNumber = 39,
            areaHa = 77.011,
            label = "77.011 Ha",
            center = GeoPoint(40.4310656, 68.3054251),
            boundary = listOf(
                GeoPoint(40.4214652, 68.3024221),
                GeoPoint(40.4220654, 68.3028225),
                GeoPoint(40.4228883, 68.3032272),
                GeoPoint(40.4276363, 68.3058500),
                GeoPoint(40.4288547, 68.3065024),
                GeoPoint(40.4391779, 68.3120887),
                GeoPoint(40.4405296, 68.3083882),
                GeoPoint(40.4400478, 68.3081431),
                GeoPoint(40.4402030, 68.3077318),
                GeoPoint(40.4314128, 68.3033873),
                GeoPoint(40.4314213, 68.3033555),
                GeoPoint(40.4311525, 68.3032115),
                GeoPoint(40.4300655, 68.3026682),
                GeoPoint(40.4284797, 68.3018387),
                GeoPoint(40.4256818, 68.3004353),
                GeoPoint(40.4245293, 68.2998360),
                GeoPoint(40.4240416, 68.2995958),
                GeoPoint(40.4231834, 68.2991569),
                GeoPoint(40.4228368, 68.2988796),
                GeoPoint(40.4227117, 68.2988178),
                GeoPoint(40.4219291, 68.3003865),
                GeoPoint(40.4214717, 68.3012199),
                GeoPoint(40.4210505, 68.3018331),
                GeoPoint(40.4209202, 68.3021029)
            )
        ),

        // PH-18 / Drip Systems 41, 42, 43
        BlueprintPlot(
            id = "plot_p41", // GEOMETRY = real CAD block R:kml_61 (81.61 ha). Pipe-network trace: PH-17. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-18",
            systemNumber = 41,
            areaHa = 81.610,
            label = "81.610 Ha",
            center = GeoPoint(40.4278645, 68.3137651),
            boundary = listOf(
                GeoPoint(40.4267502, 68.3109865),
                GeoPoint(40.4242970, 68.3095438),
                GeoPoint(40.4208772, 68.3074349),
                GeoPoint(40.4196809, 68.3066718),
                GeoPoint(40.4191712, 68.3062987),
                GeoPoint(40.4190368, 68.3062217),
                GeoPoint(40.4187447, 68.3068520),
                GeoPoint(40.4185482, 68.3071958),
                GeoPoint(40.4183201, 68.3076830),
                GeoPoint(40.4182430, 68.3078121),
                GeoPoint(40.4180450, 68.3080163),
                GeoPoint(40.4173984, 68.3093289),
                GeoPoint(40.4172689, 68.3094778),
                GeoPoint(40.4196421, 68.3108953),
                GeoPoint(40.4216449, 68.3121084),
                GeoPoint(40.4215249, 68.3123875),
                GeoPoint(40.4241578, 68.3139256),
                GeoPoint(40.4333841, 68.3194233),
                GeoPoint(40.4362565, 68.3211593),
                GeoPoint(40.4371922, 68.3183057),
                GeoPoint(40.4376783, 68.3169165),
                GeoPoint(40.4327863, 68.3140700),
                GeoPoint(40.4305606, 68.3131203),
                GeoPoint(40.4292804, 68.3125420),
                GeoPoint(40.4292665, 68.3124514),
                GeoPoint(40.4281649, 68.3117755)
            )
        ),
        BlueprintPlot(
            id = "plot_p42_43", // GEOMETRY = real CAD block R:kml_62 (66.65 ha). Pipe-network trace: PH-17. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "Sh. Rashido",
            pumpHouseId = "PH-18",
            systemNumber = 42,
            areaHa = 66.651,
            label = "66.651 Ha",
            center = GeoPoint(40.4271366, 68.3179829),
            boundary = listOf(
                GeoPoint(40.4214168, 68.3123269),
                GeoPoint(40.4205861, 68.3144856),
                GeoPoint(40.4159417, 68.3116839),
                GeoPoint(40.4154712, 68.3126709),
                GeoPoint(40.4153727, 68.3128922),
                GeoPoint(40.4168237, 68.3137918),
                GeoPoint(40.4198134, 68.3155775),
                GeoPoint(40.4225073, 68.3172679),
                GeoPoint(40.4291737, 68.3211316),
                GeoPoint(40.4313476, 68.3224834),
                GeoPoint(40.4350978, 68.3247012),
                GeoPoint(40.4362516, 68.3211645),
                GeoPoint(40.4238385, 68.3137355)
            )
        ),

        // === T. MALIK (SARDOBA SITE 2) (Total Area: 649.50 HA) ===
        // PH-7 / System 24
        BlueprintPlot(
            id = "plot_p24_a", // GEOMETRY = real CAD block M:kml_219 (52.91 ha). Pipe-network trace: PH-7. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-7",
            systemNumber = 24,
            areaHa = 52.913,
            label = "52.913 Ha",
            center = GeoPoint(40.4285498, 68.3485440),
            boundary = listOf(
                GeoPoint(40.4214832, 68.3442498),
                GeoPoint(40.4210907, 68.3458431),
                GeoPoint(40.4208099, 68.3471142),
                GeoPoint(40.4207348, 68.3475339),
                GeoPoint(40.4226156, 68.3481665),
                GeoPoint(40.4227112, 68.3484642),
                GeoPoint(40.4235949, 68.3487265),
                GeoPoint(40.4290920, 68.3507400),
                GeoPoint(40.4341103, 68.3523581),
                GeoPoint(40.4348575, 68.3525343),
                GeoPoint(40.4355249, 68.3524581),
                GeoPoint(40.4356478, 68.3522388),
                GeoPoint(40.4358844, 68.3510588),
                GeoPoint(40.4362085, 68.3497030),
                GeoPoint(40.4362965, 68.3491860),
                GeoPoint(40.4338695, 68.3484449),
                GeoPoint(40.4330157, 68.3482278),
                GeoPoint(40.4306669, 68.3473693)
            )
        ),
        BlueprintPlot(
            id = "plot_p24_b", // GEOMETRY = real CAD block M:kml_220 (62.34 ha). Pipe-network trace: PH-7. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-7",
            systemNumber = 24,
            areaHa = 62.339,
            label = "62.339 Ha",
            center = GeoPoint(40.4293173, 68.3443863),
            boundary = listOf(
                GeoPoint(40.4297386, 68.3423198),
                GeoPoint(40.4296563, 68.3422667),
                GeoPoint(40.4292282, 68.3421322),
                GeoPoint(40.4278527, 68.3416675),
                GeoPoint(40.4271128, 68.3413757),
                GeoPoint(40.4259298, 68.3410367),
                GeoPoint(40.4255582, 68.3408359),
                GeoPoint(40.4229470, 68.3399621),
                GeoPoint(40.4223036, 68.3397888),
                GeoPoint(40.4221128, 68.3400041),
                GeoPoint(40.4217157, 68.3419594),
                GeoPoint(40.4214526, 68.3433082),
                GeoPoint(40.4214429, 68.3436283),
                GeoPoint(40.4214673, 68.3438858),
                GeoPoint(40.4296476, 68.3466545),
                GeoPoint(40.4316108, 68.3473489),
                GeoPoint(40.4337191, 68.3481404),
                GeoPoint(40.4363525, 68.3490966),
                GeoPoint(40.4364023, 68.3490627),
                GeoPoint(40.4367062, 68.3476156),
                GeoPoint(40.4370210, 68.3462918),
                GeoPoint(40.4372329, 68.3454757),
                GeoPoint(40.4373614, 68.3451076),
                GeoPoint(40.4373959, 68.3449344),
                GeoPoint(40.4360527, 68.3445241),
                GeoPoint(40.4354363, 68.3442991),
                GeoPoint(40.4340175, 68.3438522),
                GeoPoint(40.4330154, 68.3434474),
                GeoPoint(40.4319711, 68.3430920),
                GeoPoint(40.4315161, 68.3429888),
                GeoPoint(40.4311271, 68.3430476),
                GeoPoint(40.4301075, 68.3427523),
                GeoPoint(40.4299598, 68.3424397)
            )
        ),

        // PH-8 / System 25
        BlueprintPlot(
            id = "plot_p25", // GEOMETRY = real CAD block M:kml_171 (59.15 ha). Pipe-network trace: PH-8. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-8",
            systemNumber = 25,
            areaHa = 59.156,
            label = "59.156 Ha",
            center = GeoPoint(40.4269615, 68.3537172),
            boundary = listOf(
                GeoPoint(40.4276881, 68.3515454),
                GeoPoint(40.4335954, 68.3536118),
                GeoPoint(40.4333493, 68.3553396),
                GeoPoint(40.4339761, 68.3556257),
                GeoPoint(40.4336471, 68.3584040),
                GeoPoint(40.4270411, 68.3560179),
                GeoPoint(40.4197386, 68.3534732),
                GeoPoint(40.4206195, 68.3491244),
                GeoPoint(40.4260938, 68.3510054),
                GeoPoint(40.4260201, 68.3515619),
                GeoPoint(40.4268988, 68.3518704),
                GeoPoint(40.4270322, 68.3514915),
                GeoPoint(40.4276339, 68.3517854)
            )
        ),

        // PH-9 / System 26
        BlueprintPlot(
            id = "plot_p26", // GEOMETRY = real CAD block M:kml_170 (72.55 ha). Pipe-network trace: PH-8. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-9",
            systemNumber = 26,
            areaHa = 72.555,
            label = "72.555 Ha",
            center = GeoPoint(40.4266301, 68.3586466),
            boundary = listOf(
                GeoPoint(40.4230826, 68.3547430),
                GeoPoint(40.4210959, 68.3541043),
                GeoPoint(40.4198176, 68.3536684),
                GeoPoint(40.4196907, 68.3537933),
                GeoPoint(40.4190642, 68.3565305),
                GeoPoint(40.4189859, 68.3570517),
                GeoPoint(40.4187048, 68.3580343),
                GeoPoint(40.4187515, 68.3583078),
                GeoPoint(40.4217431, 68.3594604),
                GeoPoint(40.4234296, 68.3600981),
                GeoPoint(40.4239064, 68.3603069),
                GeoPoint(40.4254645, 68.3609129),
                GeoPoint(40.4257329, 68.3607694),
                GeoPoint(40.4260617, 68.3609946),
                GeoPoint(40.4322615, 68.3634411),
                GeoPoint(40.4334068, 68.3637632),
                GeoPoint(40.4335535, 68.3636599),
                GeoPoint(40.4338056, 68.3624154),
                GeoPoint(40.4339419, 68.3618829),
                GeoPoint(40.4330790, 68.3615287),
                GeoPoint(40.4332895, 68.3605616),
                GeoPoint(40.4340697, 68.3609022),
                GeoPoint(40.4346482, 68.3588335),
                GeoPoint(40.4261562, 68.3558678),
                GeoPoint(40.4248857, 68.3554008),
                GeoPoint(40.4238477, 68.3550543)
            )
        ),

        // PH-10 / System 27
        BlueprintPlot(
            id = "plot_p27_a", // GEOMETRY = real CAD block M:kml_109 (118.96 ha). Pipe-network trace: PH-10, PH-9. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-10",
            systemNumber = 27,
            areaHa = 118.963,
            label = "118.963 Ha",
            center = GeoPoint(40.4248553, 68.3658671),
            boundary = listOf(
                GeoPoint(40.4309400, 68.3714239),
                GeoPoint(40.4316153, 68.3716670),
                GeoPoint(40.4317110, 68.3715701),
                GeoPoint(40.4325389, 68.3680320),
                GeoPoint(40.4328926, 68.3664884),
                GeoPoint(40.4328156, 68.3663998),
                GeoPoint(40.4326162, 68.3663539),
                GeoPoint(40.4323703, 68.3661940),
                GeoPoint(40.4322846, 68.3656698),
                GeoPoint(40.4324931, 68.3649153),
                GeoPoint(40.4324503, 68.3644321),
                GeoPoint(40.4323119, 68.3642878),
                GeoPoint(40.4315596, 68.3640469),
                GeoPoint(40.4304479, 68.3635438),
                GeoPoint(40.4288077, 68.3629379),
                GeoPoint(40.4276589, 68.3624912),
                GeoPoint(40.4233867, 68.3610165),
                GeoPoint(40.4224544, 68.3608011),
                GeoPoint(40.4208304, 68.3600829),
                GeoPoint(40.4184566, 68.3591560),
                GeoPoint(40.4182763, 68.3595615),
                GeoPoint(40.4176019, 68.3629599),
                GeoPoint(40.4168527, 68.3666316),
                GeoPoint(40.4168775, 68.3670281),
                GeoPoint(40.4172090, 68.3674415),
                GeoPoint(40.4218577, 68.3691080),
                GeoPoint(40.4305552, 68.3722451),
                GeoPoint(40.4307833, 68.3722247)
            )
        ),
        BlueprintPlot(
            id = "plot_p27_b", // GEOMETRY = real CAD block M:kml_72 (71.06 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-10",
            systemNumber = 27,
            areaHa = 71.060,
            label = "71.060 Ha",
            center = GeoPoint(40.4095625, 68.3571917),
            boundary = listOf(
                GeoPoint(40.4071820, 68.3540665),
                GeoPoint(40.4051926, 68.3530749),
                GeoPoint(40.4038934, 68.3524671),
                GeoPoint(40.4023396, 68.3517791),
                GeoPoint(40.4015940, 68.3513983),
                GeoPoint(40.4009589, 68.3531842),
                GeoPoint(40.4005205, 68.3545753),
                GeoPoint(40.4015237, 68.3550320),
                GeoPoint(40.4025725, 68.3554556),
                GeoPoint(40.4031085, 68.3556914),
                GeoPoint(40.4027509, 68.3566525),
                GeoPoint(40.4041077, 68.3572326),
                GeoPoint(40.4042066, 68.3572407),
                GeoPoint(40.4061161, 68.3581129),
                GeoPoint(40.4062309, 68.3581806),
                GeoPoint(40.4091476, 68.3594231),
                GeoPoint(40.4101022, 68.3598543),
                GeoPoint(40.4110347, 68.3601978),
                GeoPoint(40.4170314, 68.3628737),
                GeoPoint(40.4175828, 68.3605948),
                GeoPoint(40.4179638, 68.3584959),
                GeoPoint(40.4147667, 68.3569880),
                GeoPoint(40.4120450, 68.3558266),
                GeoPoint(40.4116743, 68.3557433),
                GeoPoint(40.4092143, 68.3548669),
                GeoPoint(40.4078390, 68.3544094)
            )
        ),

        // PH-11 / System 28
        BlueprintPlot(
            id = "plot_p28_a", // GEOMETRY = real CAD block M:kml_70 (70.76 ha). Pipe-network trace: unresolved. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-11",
            systemNumber = 28,
            areaHa = 70.764,
            label = "70.764 Ha",
            center = GeoPoint(40.4080544, 68.3614984),
            boundary = listOf(
                GeoPoint(40.4134276, 68.3616382),
                GeoPoint(40.4112635, 68.3606894),
                GeoPoint(40.4108489, 68.3605671),
                GeoPoint(40.4074582, 68.3591679),
                GeoPoint(40.4041112, 68.3575502),
                GeoPoint(40.4001732, 68.3557476),
                GeoPoint(40.3988474, 68.3596867),
                GeoPoint(40.4020878, 68.3610918),
                GeoPoint(40.4035920, 68.3616736),
                GeoPoint(40.4039453, 68.3618172),
                GeoPoint(40.4043104, 68.3620407),
                GeoPoint(40.4096125, 68.3643975),
                GeoPoint(40.4103137, 68.3646393),
                GeoPoint(40.4110393, 68.3649750),
                GeoPoint(40.4122150, 68.3654620),
                GeoPoint(40.4132157, 68.3659423),
                GeoPoint(40.4136209, 68.3661643),
                GeoPoint(40.4151250, 68.3667949),
                GeoPoint(40.4160727, 68.3672862),
                GeoPoint(40.4162992, 68.3664439),
                GeoPoint(40.4164035, 68.3659612),
                GeoPoint(40.4168163, 68.3642709),
                GeoPoint(40.4169609, 68.3635749),
                GeoPoint(40.4170692, 68.3631194),
                GeoPoint(40.4168167, 68.3629919),
                GeoPoint(40.4149681, 68.3621283),
                GeoPoint(40.4148939, 68.3622893),
                GeoPoint(40.4147954, 68.3623211),
                GeoPoint(40.4144438, 68.3621381)
            )
        ),
        BlueprintPlot(
            id = "plot_p28_b", // GEOMETRY = real CAD block M:kml_26 (141.76 ha). Pipe-network trace: PH-11. pumpHouseId/systemNumber below are the OLD values, not verified
            siteName = "T. Malik (Site 2)",
            pumpHouseId = "PH-11",
            systemNumber = 28,
            areaHa = 141.758,
            label = "141.758 Ha",
            center = GeoPoint(40.4063909, 68.3681818),
            boundary = listOf(
                GeoPoint(40.3973906, 68.3645740),
                GeoPoint(40.3972448, 68.3645801),
                GeoPoint(40.3966598, 68.3663888),
                GeoPoint(40.3962856, 68.3674771),
                GeoPoint(40.3961629, 68.3679378),
                GeoPoint(40.3969831, 68.3683058),
                GeoPoint(40.3976188, 68.3686055),
                GeoPoint(40.3985885, 68.3690265),
                GeoPoint(40.3996950, 68.3694175),
                GeoPoint(40.4019488, 68.3704544),
                GeoPoint(40.4031996, 68.3709463),
                GeoPoint(40.4043148, 68.3714644),
                GeoPoint(40.4050179, 68.3718306),
                GeoPoint(40.4058959, 68.3722034),
                GeoPoint(40.4060199, 68.3722393),
                GeoPoint(40.4141505, 68.3759518),
                GeoPoint(40.4151496, 68.3721413),
                GeoPoint(40.4157002, 68.3693827),
                GeoPoint(40.4159187, 68.3678759),
                GeoPoint(40.4091699, 68.3651017),
                GeoPoint(40.4046102, 68.3631105),
                GeoPoint(40.3986085, 68.3605153),
                GeoPoint(40.3978363, 68.3630757),
                GeoPoint(40.3986115, 68.3634208),
                GeoPoint(40.3982414, 68.3649292)
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
    // ELEVATION CONTOURS – extracted from "Jain Design MAP Sardoba.pdf" (vector), georeferenced
    // to the CAD KMLs (median fit ~1 m). Only contours with an elevation label on the drawing
    // are included; ~3 m simplification. (Replaces the earlier invented placeholder lines.)
    // =========================================================================
    val ELEVATION_CONTOURS: List<ContourLine> = listOf(
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4167702, 68.2739758), GeoPoint(40.4174734, 68.2748409), GeoPoint(40.4170495, 68.2748448), GeoPoint(40.4172868, 68.2761009), GeoPoint(40.4174117, 68.2760603), GeoPoint(40.4174766, 68.2761481), GeoPoint(40.4174694, 68.2760422), GeoPoint(40.4177160, 68.2759568), GeoPoint(40.4180024, 68.2755948), GeoPoint(40.4180346, 68.2756069), GeoPoint(40.4180632, 68.2751310), GeoPoint(40.4183588, 68.2747220), GeoPoint(40.4184377, 68.2745338), GeoPoint(40.4182030, 68.2744958), GeoPoint(40.4174970, 68.2732573), GeoPoint(40.4175090, 68.2731424), GeoPoint(40.4192268, 68.2726765), GeoPoint(40.4188336, 68.2733419), GeoPoint(40.4185651, 68.2735211), GeoPoint(40.4182373, 68.2734807), GeoPoint(40.4186635, 68.2742323), GeoPoint(40.4190285, 68.2745267), GeoPoint(40.4201178, 68.2746204), GeoPoint(40.4205205, 68.2739208), GeoPoint(40.4199324, 68.2738641), GeoPoint(40.4198995, 68.2737587), GeoPoint(40.4195453, 68.2736254), GeoPoint(40.4194024, 68.2733989), GeoPoint(40.4198151, 68.2727586), GeoPoint(40.4199378, 68.2732783), GeoPoint(40.4203095, 68.2727406))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4154431, 68.3297638), GeoPoint(40.4146624, 68.3227496), GeoPoint(40.4126430, 68.3218956), GeoPoint(40.4107112, 68.3225467), GeoPoint(40.4101487, 68.3151173), GeoPoint(40.4101413, 68.3145190), GeoPoint(40.4107569, 68.3125468), GeoPoint(40.4107578, 68.3122115), GeoPoint(40.4108681, 68.3119251), GeoPoint(40.4111106, 68.3116957), GeoPoint(40.4114480, 68.3117233), GeoPoint(40.4115560, 68.3111101), GeoPoint(40.4120016, 68.3110000), GeoPoint(40.4123892, 68.3113153), GeoPoint(40.4125719, 68.3112652), GeoPoint(40.4128326, 68.3113495), GeoPoint(40.4132502, 68.3113714), GeoPoint(40.4132125, 68.3110326), GeoPoint(40.4137214, 68.3107939), GeoPoint(40.4137627, 68.3102796), GeoPoint(40.4137133, 68.3100980), GeoPoint(40.4139956, 68.3091547), GeoPoint(40.4139616, 68.3088880), GeoPoint(40.4141860, 68.3088244), GeoPoint(40.4144411, 68.3085734), GeoPoint(40.4146236, 68.3080395), GeoPoint(40.4145812, 68.3079427), GeoPoint(40.4142291, 68.3076479), GeoPoint(40.4138311, 68.3072224), GeoPoint(40.4136908, 68.3069194), GeoPoint(40.4136701, 68.3067076), GeoPoint(40.4138195, 68.3064969), GeoPoint(40.4137920, 68.3062470), GeoPoint(40.4141106, 68.3063386), GeoPoint(40.4144886, 68.3066541), GeoPoint(40.4145566, 68.3071876), GeoPoint(40.4147084, 68.3073121), GeoPoint(40.4150463, 68.3074161), GeoPoint(40.4154307, 68.3072859), GeoPoint(40.4155616, 68.3071816), GeoPoint(40.4156955, 68.3065891), GeoPoint(40.4160845, 68.3061998), GeoPoint(40.4162250, 68.3056327), GeoPoint(40.4167734, 68.3050706), GeoPoint(40.4168696, 68.3046019), GeoPoint(40.4168500, 68.3036515), GeoPoint(40.4171749, 68.3032762), GeoPoint(40.4172506, 68.3026296), GeoPoint(40.4177949, 68.3024071), GeoPoint(40.4177940, 68.3018299), GeoPoint(40.4177122, 68.3016192), GeoPoint(40.4177735, 68.3012106), GeoPoint(40.4175256, 68.3011134), GeoPoint(40.4172759, 68.3012242), GeoPoint(40.4171279, 68.3011930), GeoPoint(40.4168268, 68.3012963), GeoPoint(40.4166947, 68.3012394), GeoPoint(40.4166501, 68.3008327), GeoPoint(40.4163475, 68.3007323), GeoPoint(40.4159666, 68.3004509), GeoPoint(40.4157651, 68.3005692), GeoPoint(40.4155210, 68.3010025), GeoPoint(40.4154995, 68.3016014), GeoPoint(40.4157754, 68.3020079), GeoPoint(40.4162963, 68.3016585), GeoPoint(40.4165466, 68.3020825), GeoPoint(40.4162344, 68.3024237), GeoPoint(40.4161597, 68.3027689), GeoPoint(40.4160484, 68.3029238), GeoPoint(40.4157589, 68.3028486), GeoPoint(40.4152810, 68.3038380), GeoPoint(40.4151488, 68.3037556), GeoPoint(40.4150060, 68.3035503), GeoPoint(40.4151485, 68.3028176), GeoPoint(40.4149599, 68.3029272), GeoPoint(40.4148025, 68.3029217), GeoPoint(40.4146663, 68.3031917), GeoPoint(40.4143285, 68.3031089), GeoPoint(40.4141705, 68.3030100), GeoPoint(40.4139947, 68.3035862), GeoPoint(40.4134932, 68.3035023), GeoPoint(40.4133088, 68.3033148), GeoPoint(40.4132126, 68.3028836), GeoPoint(40.4135493, 68.3028137), GeoPoint(40.4138214, 68.3026897), GeoPoint(40.4137526, 68.3025000), GeoPoint(40.4138901, 68.3024168), GeoPoint(40.4140585, 68.3017218), GeoPoint(40.4140162, 68.3016335), GeoPoint(40.4139993, 68.3010651), GeoPoint(40.4144147, 68.3007771), GeoPoint(40.4145324, 68.3001680), GeoPoint(40.4148343, 68.2997251), GeoPoint(40.4148076, 68.2991399), GeoPoint(40.4145892, 68.2986899), GeoPoint(40.4145556, 68.2984741), GeoPoint(40.4143507, 68.2985713), GeoPoint(40.4141057, 68.2984359), GeoPoint(40.4141019, 68.2983468), GeoPoint(40.4145008, 68.2979912), GeoPoint(40.4149622, 68.2978552), GeoPoint(40.4155530, 68.2969443), GeoPoint(40.4156933, 68.2963644), GeoPoint(40.4156300, 68.2955974), GeoPoint(40.4157509, 68.2949881), GeoPoint(40.4160183, 68.2946605), GeoPoint(40.4156299, 68.2946933), GeoPoint(40.4152621, 68.2940042), GeoPoint(40.4151866, 68.2937764), GeoPoint(40.4153430, 68.2932005), GeoPoint(40.4151217, 68.2923430), GeoPoint(40.4149954, 68.2921926), GeoPoint(40.4149219, 68.2917951), GeoPoint(40.4147125, 68.2912600), GeoPoint(40.4147543, 68.2903806), GeoPoint(40.4146862, 68.2894015), GeoPoint(40.4144358, 68.2889691), GeoPoint(40.4140737, 68.2886279), GeoPoint(40.4139568, 68.2884434), GeoPoint(40.4135800, 68.2883020), GeoPoint(40.4132792, 68.2880021), GeoPoint(40.4132132, 68.2877572), GeoPoint(40.4130442, 68.2879259), GeoPoint(40.4127423, 68.2879189), GeoPoint(40.4126386, 68.2877893), GeoPoint(40.4127270, 68.2871298), GeoPoint(40.4124625, 68.2869693), GeoPoint(40.4123666, 68.2865934), GeoPoint(40.4123131, 68.2858517), GeoPoint(40.4125727, 68.2853459), GeoPoint(40.4126365, 68.2848354), GeoPoint(40.4123205, 68.2846632), GeoPoint(40.4122104, 68.2845379), GeoPoint(40.4119438, 68.2840889), GeoPoint(40.4117744, 68.2833154), GeoPoint(40.4109446, 68.2822575), GeoPoint(40.4107291, 68.2826521), GeoPoint(40.4105647, 68.2825576), GeoPoint(40.4105893, 68.2832871), GeoPoint(40.4103967, 68.2833035), GeoPoint(40.4098811, 68.2830544), GeoPoint(40.4102459, 68.2824363), GeoPoint(40.4104185, 68.2823269), GeoPoint(40.4107239, 68.2819307), GeoPoint(40.4108839, 68.2818640), GeoPoint(40.4111297, 68.2820927), GeoPoint(40.4118727, 68.2831480), GeoPoint(40.4121392, 68.2826931), GeoPoint(40.4123118, 68.2825879), GeoPoint(40.4126172, 68.2821874), GeoPoint(40.4127262, 68.2821683), GeoPoint(40.4127813, 68.2822309), GeoPoint(40.4128100, 68.2822007), GeoPoint(40.4131437, 68.2826060), GeoPoint(40.4135329, 68.2826920), GeoPoint(40.4139279, 68.2827057), GeoPoint(40.4139981, 68.2826407), GeoPoint(40.4144019, 68.2825268), GeoPoint(40.4144783, 68.2824278), GeoPoint(40.4143263, 68.2822864), GeoPoint(40.4143114, 68.2824437), GeoPoint(40.4139464, 68.2825907), GeoPoint(40.4138125, 68.2822834), GeoPoint(40.4140682, 68.2821300), GeoPoint(40.4137130, 68.2818524), GeoPoint(40.4137603, 68.2817369), GeoPoint(40.4135813, 68.2814050), GeoPoint(40.4136410, 68.2812341), GeoPoint(40.4137970, 68.2810444), GeoPoint(40.4139758, 68.2809009), GeoPoint(40.4141237, 68.2809321), GeoPoint(40.4143594, 68.2811016), GeoPoint(40.4145910, 68.2811608), GeoPoint(40.4146848, 68.2816896), GeoPoint(40.4145734, 68.2818190), GeoPoint(40.4146930, 68.2819313), GeoPoint(40.4148432, 68.2818309), GeoPoint(40.4151009, 68.2815076), GeoPoint(40.4151192, 68.2813672), GeoPoint(40.4158194, 68.2809549), GeoPoint(40.4159248, 68.2808680), GeoPoint(40.4162330, 68.2804164), GeoPoint(40.4164886, 68.2802418), GeoPoint(40.4166060, 68.2796113), GeoPoint(40.4166115, 68.2781681), GeoPoint(40.4166851, 68.2781285), GeoPoint(40.4167465, 68.2781825), GeoPoint(40.4170680, 68.2782443), GeoPoint(40.4173791, 68.2781789), GeoPoint(40.4179119, 68.2768274), GeoPoint(40.4171571, 68.2763792), GeoPoint(40.4163001, 68.2746809), GeoPoint(40.4165476, 68.2742772), GeoPoint(40.4164932, 68.2743079))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4261068, 68.2772477), GeoPoint(40.4257236, 68.2775352), GeoPoint(40.4257834, 68.2778143), GeoPoint(40.4251202, 68.2788967), GeoPoint(40.4250825, 68.2790078), GeoPoint(40.4252133, 68.2797652), GeoPoint(40.4248182, 68.2801930), GeoPoint(40.4248848, 68.2809558), GeoPoint(40.4247155, 68.2815193), GeoPoint(40.4247323, 68.2816167), GeoPoint(40.4250850, 68.2819835), GeoPoint(40.4251901, 68.2823041), GeoPoint(40.4254770, 68.2828929), GeoPoint(40.4254847, 68.2830711), GeoPoint(40.4253012, 68.2834566), GeoPoint(40.4249024, 68.2838123), GeoPoint(40.4242879, 68.2840872), GeoPoint(40.4240868, 68.2842523), GeoPoint(40.4235454, 68.2844409), GeoPoint(40.4236513, 68.2853091), GeoPoint(40.4235137, 68.2853754), GeoPoint(40.4233008, 68.2856851), GeoPoint(40.4232365, 68.2861235), GeoPoint(40.4234227, 68.2861157), GeoPoint(40.4234219, 68.2864426), GeoPoint(40.4233093, 68.2864151), GeoPoint(40.4232020, 68.2862388), GeoPoint(40.4231664, 68.2866427), GeoPoint(40.4231222, 68.2858583), GeoPoint(40.4230837, 68.2858590), GeoPoint(40.4231467, 68.2856965), GeoPoint(40.4231068, 68.2855063), GeoPoint(40.4229814, 68.2850417), GeoPoint(40.4226827, 68.2845975), GeoPoint(40.4224189, 68.2845218), GeoPoint(40.4220879, 68.2844984), GeoPoint(40.4219429, 68.2844248), GeoPoint(40.4219425, 68.2848068), GeoPoint(40.4218288, 68.2850721), GeoPoint(40.4217328, 68.2851249), GeoPoint(40.4214393, 68.2849522), GeoPoint(40.4211857, 68.2854070), GeoPoint(40.4210189, 68.2854314), GeoPoint(40.4210330, 68.2860339), GeoPoint(40.4209764, 68.2866504), GeoPoint(40.4213692, 68.2867873), GeoPoint(40.4216326, 68.2867993), GeoPoint(40.4217656, 68.2869793), GeoPoint(40.4219215, 68.2867853), GeoPoint(40.4221202, 68.2867263), GeoPoint(40.4222689, 68.2868593), GeoPoint(40.4223029, 68.2871303), GeoPoint(40.4221620, 68.2876169), GeoPoint(40.4224659, 68.2879083), GeoPoint(40.4228560, 68.2881216), GeoPoint(40.4228989, 68.2882779), GeoPoint(40.4232064, 68.2886158), GeoPoint(40.4234848, 68.2884832), GeoPoint(40.4236586, 68.2885309), GeoPoint(40.4236745, 68.2889466), GeoPoint(40.4237500, 68.2891786), GeoPoint(40.4234172, 68.2893420), GeoPoint(40.4229505, 68.2887438), GeoPoint(40.4223831, 68.2884490), GeoPoint(40.4223050, 68.2882976), GeoPoint(40.4218015, 68.2879549), GeoPoint(40.4217802, 68.2881123), GeoPoint(40.4211292, 68.2882393), GeoPoint(40.4211964, 68.2886455), GeoPoint(40.4208294, 68.2885167), GeoPoint(40.4204733, 68.2881244), GeoPoint(40.4200545, 68.2879413), GeoPoint(40.4197212, 68.2880368), GeoPoint(40.4194513, 68.2880122), GeoPoint(40.4193632, 68.2878229), GeoPoint(40.4191369, 68.2876107), GeoPoint(40.4190610, 68.2873235), GeoPoint(40.4188466, 68.2869838), GeoPoint(40.4185519, 68.2866413), GeoPoint(40.4183567, 68.2863055), GeoPoint(40.4176447, 68.2855380), GeoPoint(40.4177537, 68.2855147), GeoPoint(40.4181609, 68.2858635), GeoPoint(40.4186338, 68.2859904), GeoPoint(40.4185635, 68.2855885), GeoPoint(40.4183455, 68.2852021), GeoPoint(40.4182536, 68.2849237), GeoPoint(40.4182993, 68.2845875), GeoPoint(40.4181053, 68.2844002), GeoPoint(40.4180058, 68.2839649), GeoPoint(40.4180861, 68.2839719), GeoPoint(40.4182620, 68.2838667), GeoPoint(40.4186401, 68.2842033), GeoPoint(40.4188138, 68.2842467), GeoPoint(40.4189642, 68.2846046), GeoPoint(40.4191973, 68.2848676), GeoPoint(40.4194136, 68.2845833), GeoPoint(40.4193537, 68.2842958), GeoPoint(40.4191465, 68.2840621), GeoPoint(40.4189115, 68.2839859), GeoPoint(40.4187780, 68.2837337), GeoPoint(40.4186261, 68.2835966), GeoPoint(40.4189311, 68.2831451), GeoPoint(40.4187778, 68.2823797), GeoPoint(40.4184830, 68.2824745), GeoPoint(40.4184138, 68.2826668), GeoPoint(40.4180704, 68.2826946), GeoPoint(40.4178214, 68.2824659), GeoPoint(40.4177612, 68.2821317), GeoPoint(40.4172942, 68.2823911), GeoPoint(40.4171703, 68.2825717), GeoPoint(40.4169756, 68.2831781), GeoPoint(40.4167860, 68.2835934), GeoPoint(40.4165677, 68.2835976), GeoPoint(40.4161674, 68.2837495), GeoPoint(40.4161480, 68.2841828), GeoPoint(40.4160622, 68.2843075), GeoPoint(40.4158927, 68.2843957), GeoPoint(40.4157812, 68.2845166), GeoPoint(40.4154070, 68.2842946), GeoPoint(40.4151240, 68.2842278), GeoPoint(40.4150438, 68.2838007), GeoPoint(40.4149854, 68.2837169), GeoPoint(40.4144919, 68.2838579), GeoPoint(40.4145262, 68.2841628), GeoPoint(40.4146959, 68.2845373), GeoPoint(40.4148409, 68.2846024), GeoPoint(40.4148882, 68.2849326), GeoPoint(40.4147114, 68.2849020), GeoPoint(40.4142051, 68.2850475), GeoPoint(40.4141619, 68.2861773), GeoPoint(40.4145796, 68.2870903), GeoPoint(40.4149430, 68.2871683), GeoPoint(40.4152603, 68.2870774), GeoPoint(40.4153616, 68.2868760), GeoPoint(40.4154820, 68.2870987), GeoPoint(40.4158937, 68.2871842), GeoPoint(40.4162319, 68.2877720), GeoPoint(40.4162935, 68.2882929), GeoPoint(40.4164332, 68.2885152), GeoPoint(40.4166076, 68.2886605), GeoPoint(40.4167489, 68.2891077), GeoPoint(40.4164537, 68.2895759), GeoPoint(40.4164775, 68.2897622), GeoPoint(40.4167792, 68.2901852), GeoPoint(40.4169471, 68.2903094), GeoPoint(40.4173397, 68.2908749), GeoPoint(40.4174941, 68.2909102), GeoPoint(40.4175868, 68.2912862), GeoPoint(40.4175214, 68.2915803), GeoPoint(40.4175576, 68.2921399), GeoPoint(40.4176067, 68.2922705), GeoPoint(40.4175512, 68.2925899), GeoPoint(40.4173956, 68.2932763), GeoPoint(40.4168573, 68.2947890), GeoPoint(40.4168380, 68.2952308), GeoPoint(40.4166719, 68.2957984), GeoPoint(40.4167546, 68.2965863), GeoPoint(40.4166710, 68.2979206), GeoPoint(40.4161561, 68.2977606), GeoPoint(40.4159922, 68.2981923), GeoPoint(40.4158207, 68.2984587), GeoPoint(40.4158815, 68.2988778), GeoPoint(40.4160363, 68.2994266), GeoPoint(40.4164318, 68.2994871), GeoPoint(40.4165997, 68.2991656), GeoPoint(40.4169901, 68.2989842), GeoPoint(40.4171777, 68.2991632), GeoPoint(40.4174220, 68.2991968), GeoPoint(40.4176806, 68.2994339), GeoPoint(40.4181799, 68.2996622), GeoPoint(40.4185606, 68.2994555), GeoPoint(40.4189055, 68.2991901), GeoPoint(40.4190657, 68.2991404), GeoPoint(40.4196843, 68.2994259), GeoPoint(40.4199189, 68.2998969), GeoPoint(40.4199622, 68.3001210), GeoPoint(40.4199291, 68.3004315), GeoPoint(40.4200821, 68.3007088), GeoPoint(40.4205213, 68.3010571), GeoPoint(40.4212908, 68.3017939), GeoPoint(40.4212980, 68.3018999), GeoPoint(40.4212055, 68.3019950), GeoPoint(40.4203463, 68.3012853), GeoPoint(40.4206073, 68.3018620), GeoPoint(40.4204769, 68.3020469), GeoPoint(40.4199310, 68.3024944), GeoPoint(40.4200690, 68.3024833), GeoPoint(40.4191421, 68.3058030), GeoPoint(40.4186463, 68.3033632), GeoPoint(40.4184991, 68.3034423), GeoPoint(40.4183685, 68.3035933), GeoPoint(40.4183665, 68.3037589), GeoPoint(40.4190225, 68.3061533), GeoPoint(40.4187507, 68.3063112), GeoPoint(40.4183612, 68.3061784), GeoPoint(40.4181658, 68.3062542), GeoPoint(40.4177400, 68.3059821), GeoPoint(40.4176008, 68.3058234), GeoPoint(40.4174833, 68.3051040), GeoPoint(40.4172395, 68.3055882), GeoPoint(40.4174376, 68.3059113), GeoPoint(40.4175131, 68.3061349), GeoPoint(40.4172547, 68.3063774), GeoPoint(40.4171075, 68.3064523), GeoPoint(40.4170440, 68.3070095), GeoPoint(40.4175376, 68.3077855), GeoPoint(40.4169900, 68.3075453), GeoPoint(40.4170525, 68.3073022), GeoPoint(40.4167810, 68.3070484), GeoPoint(40.4164355, 68.3072416), GeoPoint(40.4163373, 68.3078844), GeoPoint(40.4158964, 68.3081982), GeoPoint(40.4156546, 68.3085253), GeoPoint(40.4159051, 68.3089748), GeoPoint(40.4160595, 68.3090186), GeoPoint(40.4163180, 68.3087845), GeoPoint(40.4169330, 68.3094734), GeoPoint(40.4170672, 68.3102901), GeoPoint(40.4172861, 68.3103624), GeoPoint(40.4180599, 68.3098895), GeoPoint(40.4180907, 68.3092565), GeoPoint(40.4181348, 68.3091326), GeoPoint(40.4179624, 68.3088217), GeoPoint(40.4180164, 68.3091900), GeoPoint(40.4176505, 68.3092223), GeoPoint(40.4166794, 68.3081241), GeoPoint(40.4167967, 68.3079097), GeoPoint(40.4174762, 68.3081772), GeoPoint(40.4179419, 68.3086481), GeoPoint(40.4181726, 68.3085801), GeoPoint(40.4181131, 68.3083393), GeoPoint(40.4175056, 68.3073447), GeoPoint(40.4177541, 68.3070641), GeoPoint(40.4183895, 68.3079139), GeoPoint(40.4185683, 68.3082204), GeoPoint(40.4185381, 68.3084841), GeoPoint(40.4190414, 68.3083644), GeoPoint(40.4191572, 68.3084004), GeoPoint(40.4194906, 68.3087762), GeoPoint(40.4196837, 68.3101903), GeoPoint(40.4194514, 68.3104918), GeoPoint(40.4192337, 68.3105892), GeoPoint(40.4192678, 68.3113229), GeoPoint(40.4193362, 68.3114659), GeoPoint(40.4196267, 68.3116770), GeoPoint(40.4197897, 68.3124762), GeoPoint(40.4199364, 68.3127918), GeoPoint(40.4200189, 68.3131129), GeoPoint(40.4212736, 68.3129791), GeoPoint(40.4212601, 68.3133402), GeoPoint(40.4213957, 68.3129938), GeoPoint(40.4213853, 68.3128837), GeoPoint(40.4216084, 68.3131002), GeoPoint(40.4216613, 68.3137742), GeoPoint(40.4214821, 68.3143208), GeoPoint(40.4212319, 68.3148221), GeoPoint(40.4210374, 68.3150125), GeoPoint(40.4207560, 68.3151790), GeoPoint(40.4207654, 68.3160660), GeoPoint(40.4203725, 68.3163619), GeoPoint(40.4204577, 68.3166066), GeoPoint(40.4208148, 68.3171602), GeoPoint(40.4217398, 68.3176482), GeoPoint(40.4218163, 68.3175661), GeoPoint(40.4219061, 68.3180101), GeoPoint(40.4217764, 68.3182885), GeoPoint(40.4217462, 68.3185692), GeoPoint(40.4216012, 68.3189539), GeoPoint(40.4216154, 68.3191489), GeoPoint(40.4213228, 68.3195576), GeoPoint(40.4211306, 68.3200832), GeoPoint(40.4204198, 68.3208477), GeoPoint(40.4203048, 68.3204678), GeoPoint(40.4203153, 68.3201280), GeoPoint(40.4204033, 68.3194006), GeoPoint(40.4205351, 68.3189524), GeoPoint(40.4205137, 68.3186472), GeoPoint(40.4202688, 68.3185329), GeoPoint(40.4201739, 68.3187341), GeoPoint(40.4199497, 68.3188147), GeoPoint(40.4198076, 68.3191654), GeoPoint(40.4192994, 68.3195059), GeoPoint(40.4190685, 68.3200068), GeoPoint(40.4189156, 68.3201964), GeoPoint(40.4187939, 68.3202411), GeoPoint(40.4185363, 68.3210608), GeoPoint(40.4185544, 68.3213576), GeoPoint(40.4186898, 68.3214315), GeoPoint(40.4184354, 68.3222512), GeoPoint(40.4182999, 68.3221688), GeoPoint(40.4181782, 68.3222135), GeoPoint(40.4180753, 68.3221941), GeoPoint(40.4173330, 68.3216561), GeoPoint(40.4172591, 68.3216447), GeoPoint(40.4190488, 68.3296976))
        ),
        ContourLine(
            elevationMeters = 279.500,
            label = "279.500 m",
            points = listOf(GeoPoint(40.4280553, 68.3295321), GeoPoint(40.4276998, 68.3273439), GeoPoint(40.4285580, 68.3274596), GeoPoint(40.4289078, 68.3269650), GeoPoint(40.4291968, 68.3260385), GeoPoint(40.4291738, 68.3254870), GeoPoint(40.4293499, 68.3249489), GeoPoint(40.4296573, 68.3243573), GeoPoint(40.4299445, 68.3240973), GeoPoint(40.4300825, 68.3240821), GeoPoint(40.4303628, 68.3237585), GeoPoint(40.4300876, 68.3234324), GeoPoint(40.4304606, 68.3235019), GeoPoint(40.4305321, 68.3236237), GeoPoint(40.4310717, 68.3231808), GeoPoint(40.4312352, 68.3231480), GeoPoint(40.4311088, 68.3229805), GeoPoint(40.4310854, 68.3228366), GeoPoint(40.4308559, 68.3226201), GeoPoint(40.4306510, 68.3222503), GeoPoint(40.4303163, 68.3221504), GeoPoint(40.4302319, 68.3220161), GeoPoint(40.4295246, 68.3214391), GeoPoint(40.4289172, 68.3213612), GeoPoint(40.4286673, 68.3209880), GeoPoint(40.4281619, 68.3208148), GeoPoint(40.4277216, 68.3202965), GeoPoint(40.4276109, 68.3200736), GeoPoint(40.4271831, 68.3195127), GeoPoint(40.4271050, 68.3193740), GeoPoint(40.4270639, 68.3190055), GeoPoint(40.4272327, 68.3188071), GeoPoint(40.4276210, 68.3187616), GeoPoint(40.4280629, 68.3185879), GeoPoint(40.4287303, 68.3180449), GeoPoint(40.4287580, 68.3174161), GeoPoint(40.4289632, 68.3173656), GeoPoint(40.4291660, 68.3174467), GeoPoint(40.4290013, 68.3173054), GeoPoint(40.4288379, 68.3169009), GeoPoint(40.4283625, 68.3164215), GeoPoint(40.4285780, 68.3164812), GeoPoint(40.4288596, 68.3163359), GeoPoint(40.4286776, 68.3155666), GeoPoint(40.4288213, 68.3154451), GeoPoint(40.4289883, 68.3154505), GeoPoint(40.4292208, 68.3156415), GeoPoint(40.4294536, 68.3154036), GeoPoint(40.4294329, 68.3147460), GeoPoint(40.4298120, 68.3143102), GeoPoint(40.4296655, 68.3135615), GeoPoint(40.4309739, 68.3105781), GeoPoint(40.4312656, 68.3104963), GeoPoint(40.4317220, 68.3096514), GeoPoint(40.4323646, 68.3083318), GeoPoint(40.4324318, 68.3078423), GeoPoint(40.4325598, 68.3077720), GeoPoint(40.4326590, 68.3072734), GeoPoint(40.4329222, 68.3077058), GeoPoint(40.4331141, 68.3076088), GeoPoint(40.4332193, 68.3074922), GeoPoint(40.4331337, 68.3071966), GeoPoint(40.4326422, 68.3071761), GeoPoint(40.4323871, 68.3065144), GeoPoint(40.4320448, 68.3062406), GeoPoint(40.4320408, 68.3061345), GeoPoint(40.4322402, 68.3057232), GeoPoint(40.4323000, 68.3051023), GeoPoint(40.4326276, 68.3046546), GeoPoint(40.4324789, 68.3036172), GeoPoint(40.4324652, 68.3021401), GeoPoint(40.4324231, 68.3020942), GeoPoint(40.4322175, 68.3020768), GeoPoint(40.4319937, 68.3022254), GeoPoint(40.4318826, 68.3024015), GeoPoint(40.4319565, 68.3019501), GeoPoint(40.4318303, 68.3013751), GeoPoint(40.4314753, 68.3011271), GeoPoint(40.4313987, 68.3011879), GeoPoint(40.4310933, 68.3011342), GeoPoint(40.4309861, 68.3009707), GeoPoint(40.4306210, 68.3006592), GeoPoint(40.4311371, 68.3005263), GeoPoint(40.4314920, 68.2998616), GeoPoint(40.4318492, 68.2999610), GeoPoint(40.4319461, 68.3000484), GeoPoint(40.4323613, 68.3001807), GeoPoint(40.4327094, 68.3003651), GeoPoint(40.4327793, 68.3002577), GeoPoint(40.4327608, 68.2999142), GeoPoint(40.4325147, 68.2996301), GeoPoint(40.4324733, 68.2996691), GeoPoint(40.4323336, 68.2994509), GeoPoint(40.4324648, 68.2993763), GeoPoint(40.4325132, 68.2989721), GeoPoint(40.4327249, 68.2984884), GeoPoint(40.4328145, 68.2984357), GeoPoint(40.4331133, 68.2975640), GeoPoint(40.4336243, 68.2971680), GeoPoint(40.4335171, 68.2965502), GeoPoint(40.4335637, 68.2958785), GeoPoint(40.4334806, 68.2954895), GeoPoint(40.4334980, 68.2952345), GeoPoint(40.4333082, 68.2947371), GeoPoint(40.4331916, 68.2945949), GeoPoint(40.4327241, 68.2943406), GeoPoint(40.4326704, 68.2940232), GeoPoint(40.4324758, 68.2937509), GeoPoint(40.4324513, 68.2934712), GeoPoint(40.4326123, 68.2930860), GeoPoint(40.4326893, 68.2926303), GeoPoint(40.4326396, 68.2924190), GeoPoint(40.4323258, 68.2920938), GeoPoint(40.4325804, 68.2917748), GeoPoint(40.4325863, 68.2917067), GeoPoint(40.4321960, 68.2914679), GeoPoint(40.4320513, 68.2909824), GeoPoint(40.4319972, 68.2906183), GeoPoint(40.4312262, 68.2901193), GeoPoint(40.4308434, 68.2900331), GeoPoint(40.4307174, 68.2899336), GeoPoint(40.4305501, 68.2894359), GeoPoint(40.4299010, 68.2885015), GeoPoint(40.4295349, 68.2876127), GeoPoint(40.4294222, 68.2871224), GeoPoint(40.4293757, 68.2864652), GeoPoint(40.4286423, 68.2858594), GeoPoint(40.4280318, 68.2871276), GeoPoint(40.4278522, 68.2871565), GeoPoint(40.4285415, 68.2856830), GeoPoint(40.4286329, 68.2849978), GeoPoint(40.4291238, 68.2844960), GeoPoint(40.4295185, 68.2835715), GeoPoint(40.4297070, 68.2834405), GeoPoint(40.4300694, 68.2833827), GeoPoint(40.4307326, 68.2822875), GeoPoint(40.4309027, 68.2822715), GeoPoint(40.4309310, 68.2821946), GeoPoint(40.4308695, 68.2821278), GeoPoint(40.4350526, 68.2806512), GeoPoint(40.4349744, 68.2804913), GeoPoint(40.4344336, 68.2803106), GeoPoint(40.4342300, 68.2801362), GeoPoint(40.4338721, 68.2799435), GeoPoint(40.4347791, 68.2792638))
        ),
        ContourLine(
            elevationMeters = 279.000,
            label = "279.000 m",
            points = listOf(GeoPoint(40.4308134, 68.3294814), GeoPoint(40.4307420, 68.3284383), GeoPoint(40.4304145, 68.3270476), GeoPoint(40.4307161, 68.3260869), GeoPoint(40.4306123, 68.3259402), GeoPoint(40.4306140, 68.3257109), GeoPoint(40.4309198, 68.3253614), GeoPoint(40.4312822, 68.3252868), GeoPoint(40.4315529, 68.3249719), GeoPoint(40.4315774, 68.3243346), GeoPoint(40.4318429, 68.3241727), GeoPoint(40.4323494, 68.3240529), GeoPoint(40.4325026, 68.3239100), GeoPoint(40.4325026, 68.3234515), GeoPoint(40.4324423, 68.3231002), GeoPoint(40.4325962, 68.3221379), GeoPoint(40.4327252, 68.3217450), GeoPoint(40.4331235, 68.3213088), GeoPoint(40.4331292, 68.3212025), GeoPoint(40.4330546, 68.3206350), GeoPoint(40.4330481, 68.3201597), GeoPoint(40.4329404, 68.3199154), GeoPoint(40.4322650, 68.3188454), GeoPoint(40.4319015, 68.3187545), GeoPoint(40.4318173, 68.3186456), GeoPoint(40.4317725, 68.3186677), GeoPoint(40.4317615, 68.3184726), GeoPoint(40.4316283, 68.3182501), GeoPoint(40.4315781, 68.3179666), GeoPoint(40.4318263, 68.3176435), GeoPoint(40.4321134, 68.3178208), GeoPoint(40.4322861, 68.3181869), GeoPoint(40.4324989, 68.3178518), GeoPoint(40.4324558, 68.3171988), GeoPoint(40.4320711, 68.3168324), GeoPoint(40.4316013, 68.3166840), GeoPoint(40.4313183, 68.3161756), GeoPoint(40.4312549, 68.3158244), GeoPoint(40.4313851, 68.3156267), GeoPoint(40.4318211, 68.3155125), GeoPoint(40.4319991, 68.3152544), GeoPoint(40.4320841, 68.3150109), GeoPoint(40.4322534, 68.3148888), GeoPoint(40.4328684, 68.3142109), GeoPoint(40.4330678, 68.3137953), GeoPoint(40.4339376, 68.3128069), GeoPoint(40.4340229, 68.3135356), GeoPoint(40.4352358, 68.3106599), GeoPoint(40.4356004, 68.3099823), GeoPoint(40.4359090, 68.3100275), GeoPoint(40.4363894, 68.3103115), GeoPoint(40.4365285, 68.3104532), GeoPoint(40.4365783, 68.3111401), GeoPoint(40.4366547, 68.3110453), GeoPoint(40.4366730, 68.3122719), GeoPoint(40.4369205, 68.3118513), GeoPoint(40.4374406, 68.3109287), GeoPoint(40.4376131, 68.3103438), GeoPoint(40.4376604, 68.3097613), GeoPoint(40.4372754, 68.3089023), GeoPoint(40.4374243, 68.3086151), GeoPoint(40.4375709, 68.3084468), GeoPoint(40.4374439, 68.3081986), GeoPoint(40.4373116, 68.3076619), GeoPoint(40.4376650, 68.3072307), GeoPoint(40.4378553, 68.3064374), GeoPoint(40.4384821, 68.3051604), GeoPoint(40.4383874, 68.3044956), GeoPoint(40.4382798, 68.3042599), GeoPoint(40.4381081, 68.3040508), GeoPoint(40.4380265, 68.3043156), GeoPoint(40.4378699, 68.3044247), GeoPoint(40.4375947, 68.3041029), GeoPoint(40.4375841, 68.3035172), GeoPoint(40.4375998, 68.3034702), GeoPoint(40.4379204, 68.3033920), GeoPoint(40.4381497, 68.3031287), GeoPoint(40.4387252, 68.3032198), GeoPoint(40.4385871, 68.3027766), GeoPoint(40.4384058, 68.3025549), GeoPoint(40.4383693, 68.3024028), GeoPoint(40.4384542, 68.3021507), GeoPoint(40.4388268, 68.3017021), GeoPoint(40.4390284, 68.3016049), GeoPoint(40.4394719, 68.3016603), GeoPoint(40.4395512, 68.3015144), GeoPoint(40.4395117, 68.3013835), GeoPoint(40.4391921, 68.3011518), GeoPoint(40.4393136, 68.3006187), GeoPoint(40.4395328, 68.3002834), GeoPoint(40.4399283, 68.2999151), GeoPoint(40.4400360, 68.2996965), GeoPoint(40.4400798, 68.2990928), GeoPoint(40.4405347, 68.2989441), GeoPoint(40.4409052, 68.2986569), GeoPoint(40.4411806, 68.2985583), GeoPoint(40.4406233, 68.2978555), GeoPoint(40.4402263, 68.2975742), GeoPoint(40.4397407, 68.2974815), GeoPoint(40.4391664, 68.2975518), GeoPoint(40.4391187, 68.2976163), GeoPoint(40.4390288, 68.2976138), GeoPoint(40.4385061, 68.2981671), GeoPoint(40.4381060, 68.2983530), GeoPoint(40.4378880, 68.2983953), GeoPoint(40.4376950, 68.2979064), GeoPoint(40.4377135, 68.2977957), GeoPoint(40.4376494, 68.2978096), GeoPoint(40.4374412, 68.2974399), GeoPoint(40.4374851, 68.2968405), GeoPoint(40.4369845, 68.2964423), GeoPoint(40.4367039, 68.2958277), GeoPoint(40.4367950, 68.2955501), GeoPoint(40.4367033, 68.2952970), GeoPoint(40.4368740, 68.2953660), GeoPoint(40.4370105, 68.2951469), GeoPoint(40.4369569, 68.2948422), GeoPoint(40.4363903, 68.2946406), GeoPoint(40.4360732, 68.2943112), GeoPoint(40.4359819, 68.2941134), GeoPoint(40.4358633, 68.2941411), GeoPoint(40.4351821, 68.2936403), GeoPoint(40.4349634, 68.2935892), GeoPoint(40.4347486, 68.2936485), GeoPoint(40.4344003, 68.2938843), GeoPoint(40.4343422, 68.2938387), GeoPoint(40.4341850, 68.2934129), GeoPoint(40.4340040, 68.2932592), GeoPoint(40.4340536, 68.2930035), GeoPoint(40.4341175, 68.2929556), GeoPoint(40.4340587, 68.2928209), GeoPoint(40.4340399, 68.2924392), GeoPoint(40.4342607, 68.2923288), GeoPoint(40.4344347, 68.2919689), GeoPoint(40.4342631, 68.2917811), GeoPoint(40.4338442, 68.2915810), GeoPoint(40.4337307, 68.2914261), GeoPoint(40.4337423, 68.2912518), GeoPoint(40.4339013, 68.2910407), GeoPoint(40.4343135, 68.2907442), GeoPoint(40.4343170, 68.2912409), GeoPoint(40.4344045, 68.2913496), GeoPoint(40.4344814, 68.2913227), GeoPoint(40.4344478, 68.2906780), GeoPoint(40.4346740, 68.2904190), GeoPoint(40.4348148, 68.2899111), GeoPoint(40.4350110, 68.2895125), GeoPoint(40.4351648, 68.2894587), GeoPoint(40.4351574, 68.2897772), GeoPoint(40.4352337, 68.2901112), GeoPoint(40.4357737, 68.2901816), GeoPoint(40.4358568, 68.2905664), GeoPoint(40.4362883, 68.2898492), GeoPoint(40.4364223, 68.2901736), GeoPoint(40.4367565, 68.2902054), GeoPoint(40.4369604, 68.2899765), GeoPoint(40.4370202, 68.2898141), GeoPoint(40.4370054, 68.2895426), GeoPoint(40.4369377, 68.2894972), GeoPoint(40.4370807, 68.2892949), GeoPoint(40.4374268, 68.2891907), GeoPoint(40.4375875, 68.2892174), GeoPoint(40.4377312, 68.2891043), GeoPoint(40.4378996, 68.2888591), GeoPoint(40.4380104, 68.2886277), GeoPoint(40.4377833, 68.2878720), GeoPoint(40.4378047, 68.2877272), GeoPoint(40.4375713, 68.2874175), GeoPoint(40.4376106, 68.2870813), GeoPoint(40.4377204, 68.2867268), GeoPoint(40.4386505, 68.2852401), GeoPoint(40.4393155, 68.2848283), GeoPoint(40.4383379, 68.2846474), GeoPoint(40.4381215, 68.2844774), GeoPoint(40.4383642, 68.2842860), GeoPoint(40.4382033, 68.2842339), GeoPoint(40.4380869, 68.2841257), GeoPoint(40.4381231, 68.2838108), GeoPoint(40.4380959, 68.2836033), GeoPoint(40.4377409, 68.2833638), GeoPoint(40.4379000, 68.2831527), GeoPoint(40.4393707, 68.2818297), GeoPoint(40.4363997, 68.2817505), GeoPoint(40.4392120, 68.2807627), GeoPoint(40.4391827, 68.2807081), GeoPoint(40.4387927, 68.2805118), GeoPoint(40.4391152, 68.2802678))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4439091, 68.2819596), GeoPoint(40.4432873, 68.2834406), GeoPoint(40.4429855, 68.2834464), GeoPoint(40.4423969, 68.2837548), GeoPoint(40.4427052, 68.2846491), GeoPoint(40.4425146, 68.2849372), GeoPoint(40.4424907, 68.2851839), GeoPoint(40.4423288, 68.2854333), GeoPoint(40.4423636, 68.2857978), GeoPoint(40.4425192, 68.2860029), GeoPoint(40.4427871, 68.2861974), GeoPoint(40.4429450, 68.2862750), GeoPoint(40.4433855, 68.2859015), GeoPoint(40.4438108, 68.2856513), GeoPoint(40.4440718, 68.2853364), GeoPoint(40.4439913, 68.2848709), GeoPoint(40.4437731, 68.2844462), GeoPoint(40.4441971, 68.2844593), GeoPoint(40.4443066, 68.2845124), GeoPoint(40.4446140, 68.2848335), GeoPoint(40.4451319, 68.2849511), GeoPoint(40.4454612, 68.2851953), GeoPoint(40.4458786, 68.2847457), GeoPoint(40.4464973, 68.2832647))
        ),
        ContourLine(
            elevationMeters = 281.500,
            label = "281.500 m",
            points = listOf(GeoPoint(40.4081673, 68.3298975), GeoPoint(40.4078314, 68.3254391), GeoPoint(40.4078152, 68.3240219), GeoPoint(40.4079157, 68.3218514), GeoPoint(40.4078105, 68.3205675), GeoPoint(40.4079979, 68.3175084), GeoPoint(40.4078253, 68.3162554), GeoPoint(40.4080801, 68.3127494), GeoPoint(40.4086957, 68.3107773), GeoPoint(40.4091914, 68.3104879), GeoPoint(40.4091199, 68.3099206), GeoPoint(40.4093461, 68.3092118), GeoPoint(40.4097643, 68.3088560), GeoPoint(40.4098276, 68.3087233), GeoPoint(40.4098254, 68.3088676), GeoPoint(40.4102024, 68.3090473), GeoPoint(40.4103478, 68.3091719), GeoPoint(40.4105458, 68.3094696), GeoPoint(40.4110307, 68.3094775), GeoPoint(40.4114024, 68.3098228), GeoPoint(40.4114568, 68.3097921), GeoPoint(40.4110338, 68.3094647), GeoPoint(40.4106798, 68.3089026), GeoPoint(40.4105409, 68.3087821), GeoPoint(40.4105334, 68.3086210), GeoPoint(40.4107451, 68.3085916), GeoPoint(40.4111796, 68.3087277), GeoPoint(40.4119891, 68.3092262), GeoPoint(40.4117449, 68.3087469), GeoPoint(40.4116376, 68.3081123), GeoPoint(40.4115922, 68.3080495), GeoPoint(40.4112302, 68.3081708), GeoPoint(40.4110971, 68.3079611), GeoPoint(40.4108115, 68.3070837), GeoPoint(40.4106335, 68.3068833), GeoPoint(40.4104812, 68.3062453), GeoPoint(40.4104259, 68.3056903), GeoPoint(40.4109958, 68.3054547), GeoPoint(40.4110102, 68.3047584), GeoPoint(40.4107301, 68.3042035), GeoPoint(40.4107515, 68.3040588), GeoPoint(40.4106639, 68.3034832), GeoPoint(40.4103430, 68.3030521), GeoPoint(40.4102788, 68.3030660), GeoPoint(40.4101116, 68.3034808), GeoPoint(40.4099057, 68.3034295), GeoPoint(40.4096545, 68.3024242), GeoPoint(40.4094609, 68.3022962), GeoPoint(40.4094595, 68.3020968), GeoPoint(40.4091005, 68.3017470), GeoPoint(40.4087021, 68.3017163), GeoPoint(40.4085667, 68.3016425), GeoPoint(40.4084942, 68.3009351), GeoPoint(40.4083642, 68.3002670), GeoPoint(40.4085985, 68.3002414), GeoPoint(40.4094653, 68.3006665), GeoPoint(40.4097231, 68.3012388), GeoPoint(40.4101888, 68.3012470), GeoPoint(40.4097726, 68.3018702), GeoPoint(40.4100677, 68.3022721), GeoPoint(40.4102259, 68.3024007), GeoPoint(40.4103122, 68.3023439), GeoPoint(40.4103752, 68.3021730), GeoPoint(40.4103586, 68.3020926), GeoPoint(40.4100961, 68.3017538), GeoPoint(40.4102853, 68.3012664), GeoPoint(40.4103653, 68.3012310), GeoPoint(40.4102910, 68.3011645), GeoPoint(40.4102400, 68.3007707), GeoPoint(40.4100199, 68.3005287), GeoPoint(40.4097338, 68.3004874), GeoPoint(40.4096208, 68.2999463), GeoPoint(40.4103952, 68.3000251), GeoPoint(40.4107117, 68.3002780), GeoPoint(40.4108273, 68.3007215), GeoPoint(40.4109852, 68.3007907), GeoPoint(40.4110484, 68.3006537), GeoPoint(40.4110469, 68.3004500), GeoPoint(40.4110161, 68.3001704), GeoPoint(40.4109384, 68.3000913), GeoPoint(40.4109387, 68.2996838), GeoPoint(40.4112120, 68.2992840), GeoPoint(40.4113554, 68.2986702), GeoPoint(40.4111238, 68.2981695), GeoPoint(40.4110965, 68.2970538), GeoPoint(40.4114515, 68.2968518), GeoPoint(40.4117902, 68.2970577), GeoPoint(40.4118930, 68.2970600), GeoPoint(40.4119880, 68.2964343), GeoPoint(40.4120831, 68.2962585), GeoPoint(40.4116001, 68.2960809), GeoPoint(40.4115458, 68.2961201), GeoPoint(40.4115368, 68.2953096), GeoPoint(40.4116064, 68.2951598), GeoPoint(40.4119485, 68.2949538), GeoPoint(40.4121180, 68.2944074), GeoPoint(40.4121004, 68.2941998), GeoPoint(40.4122289, 68.2942016), GeoPoint(40.4125707, 68.2944031), GeoPoint(40.4125831, 68.2943307), GeoPoint(40.4130412, 68.2941777), GeoPoint(40.4134601, 68.2943863), GeoPoint(40.4131552, 68.2939591), GeoPoint(40.4127867, 68.2936223), GeoPoint(40.4126148, 68.2929380), GeoPoint(40.4129801, 68.2928250), GeoPoint(40.4133581, 68.2931446), GeoPoint(40.4137024, 68.2932485), GeoPoint(40.4137472, 68.2932264), GeoPoint(40.4134423, 68.2928035), GeoPoint(40.4134654, 68.2924381), GeoPoint(40.4133941, 68.2923460), GeoPoint(40.4134124, 68.2922099), GeoPoint(40.4137271, 68.2917752), GeoPoint(40.4136944, 68.2916825), GeoPoint(40.4130593, 68.2909051), GeoPoint(40.4129728, 68.2909322), GeoPoint(40.4127514, 68.2904992), GeoPoint(40.4126452, 68.2904673), GeoPoint(40.4124688, 68.2900547), GeoPoint(40.4122955, 68.2900707), GeoPoint(40.4119047, 68.2897725), GeoPoint(40.4114800, 68.2896533), GeoPoint(40.4114667, 68.2895899), GeoPoint(40.4111464, 68.2892649), GeoPoint(40.4111263, 68.2891507), GeoPoint(40.4108271, 68.2886301), GeoPoint(40.4104174, 68.2883663), GeoPoint(40.4099045, 68.2875993), GeoPoint(40.4102200, 68.2868252), GeoPoint(40.4104156, 68.2867875), GeoPoint(40.4105383, 68.2868743), GeoPoint(40.4107533, 68.2868660), GeoPoint(40.4111042, 68.2869781), GeoPoint(40.4113279, 68.2868296), GeoPoint(40.4112456, 68.2865510), GeoPoint(40.4112988, 68.2863548), GeoPoint(40.4110181, 68.2852949), GeoPoint(40.4109692, 68.2851939), GeoPoint(40.4109185, 68.2852883), GeoPoint(40.4105966, 68.2851798), GeoPoint(40.4104931, 68.2850757), GeoPoint(40.4105688, 68.2848960), GeoPoint(40.4109195, 68.2845413), GeoPoint(40.4110263, 68.2842125), GeoPoint(40.4107984, 68.2842295), GeoPoint(40.4107275, 68.2841927), GeoPoint(40.4104934, 68.2846725), GeoPoint(40.4098120, 68.2845921), GeoPoint(40.4090790, 68.2840374), GeoPoint(40.4091330, 68.2839557))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4477892, 68.2864928), GeoPoint(40.4471818, 68.2859863), GeoPoint(40.4469505, 68.2859822), GeoPoint(40.4466205, 68.2860819), GeoPoint(40.4463209, 68.2859475), GeoPoint(40.4462618, 68.2857745), GeoPoint(40.4458522, 68.2855318), GeoPoint(40.4458197, 68.2854730), GeoPoint(40.4458806, 68.2854591), GeoPoint(40.4467358, 68.2833833))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4478239, 68.2912906), GeoPoint(40.4475657, 68.2911129), GeoPoint(40.4467091, 68.2916642), GeoPoint(40.4478317, 68.2923733))
        ),
        ContourLine(
            elevationMeters = 282.500,
            label = "282.500 m",
            points = listOf(GeoPoint(40.4040445, 68.3299732), GeoPoint(40.4042296, 68.3274703), GeoPoint(40.4038873, 68.3276675), GeoPoint(40.4049930, 68.3103116), GeoPoint(40.4051352, 68.3104363), GeoPoint(40.4051508, 68.3103808), GeoPoint(40.4052408, 68.3103876), GeoPoint(40.4054670, 68.3105871), GeoPoint(40.4056170, 68.3104612), GeoPoint(40.4057057, 68.3098230), GeoPoint(40.4060825, 68.3086150), GeoPoint(40.4062326, 68.3084934), GeoPoint(40.4062344, 68.3073857), GeoPoint(40.4061609, 68.3069924), GeoPoint(40.4064029, 68.3057954), GeoPoint(40.4062212, 68.3055272), GeoPoint(40.4057372, 68.3052010), GeoPoint(40.4053063, 68.3051157), GeoPoint(40.4051365, 68.3051783), GeoPoint(40.4047725, 68.3050111), GeoPoint(40.4044856, 68.3053178), GeoPoint(40.4041208, 68.3050403), GeoPoint(40.4040021, 68.3050595), GeoPoint(40.4037733, 68.3049492), GeoPoint(40.4036176, 68.3047187), GeoPoint(40.4035246, 68.3042876), GeoPoint(40.4035347, 68.3039055), GeoPoint(40.4034720, 68.3036690), GeoPoint(40.4033718, 68.3035775), GeoPoint(40.4030266, 68.3033549), GeoPoint(40.4029907, 68.3032707), GeoPoint(40.4027203, 68.3031781), GeoPoint(40.4024732, 68.3031997), GeoPoint(40.4022383, 68.3031320), GeoPoint(40.4020888, 68.3028887), GeoPoint(40.4015089, 68.3026153), GeoPoint(40.4008881, 68.3020286), GeoPoint(40.4007352, 68.3017514), GeoPoint(40.4003810, 68.3020593), GeoPoint(40.4003157, 68.3019163), GeoPoint(40.4003449, 68.3010543), GeoPoint(40.4006300, 68.3009598), GeoPoint(40.4010252, 68.3009779), GeoPoint(40.4007255, 68.2994940), GeoPoint(40.4009791, 68.2990267), GeoPoint(40.4010003, 68.2988524), GeoPoint(40.4016039, 68.2979244), GeoPoint(40.4017284, 68.2973831), GeoPoint(40.4019975, 68.2972847), GeoPoint(40.4025692, 68.2972994), GeoPoint(40.4025829, 68.2965310), GeoPoint(40.4027812, 68.2959671), GeoPoint(40.4027771, 68.2958442), GeoPoint(40.4029734, 68.2954500), GeoPoint(40.4034319, 68.2953523), GeoPoint(40.4035822, 68.2952688), GeoPoint(40.4035484, 68.2950360), GeoPoint(40.4034179, 68.2947457), GeoPoint(40.4032436, 68.2946259), GeoPoint(40.4031159, 68.2947302), GeoPoint(40.4029742, 68.2946650), GeoPoint(40.4030556, 68.2948205), GeoPoint(40.4029543, 68.2950345), GeoPoint(40.4028893, 68.2953625), GeoPoint(40.4025783, 68.2954363), GeoPoint(40.4019181, 68.2942818), GeoPoint(40.4021908, 68.2951338), GeoPoint(40.4012227, 68.2944731))
        ),
        ContourLine(
            elevationMeters = 282.500,
            label = "282.500 m",
            points = listOf(GeoPoint(40.3998259, 68.2962860), GeoPoint(40.4005406, 68.2965271), GeoPoint(40.4008519, 68.2965000), GeoPoint(40.4004876, 68.2954163))
        ),
        ContourLine(
            elevationMeters = 283.000,
            label = "283.000 m",
            points = listOf(GeoPoint(40.3986374, 68.3300725), GeoPoint(40.3986785, 68.3197689), GeoPoint(40.3978359, 68.3036135), GeoPoint(40.3981937, 68.3038104), GeoPoint(40.3988091, 68.3018427), GeoPoint(40.3985403, 68.3010670), GeoPoint(40.3984238, 68.3009504), GeoPoint(40.3984586, 68.3008691), GeoPoint(40.3983914, 68.3009085), GeoPoint(40.3979633, 68.3007596), GeoPoint(40.3975175, 68.3021894))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4412748, 68.3118337), GeoPoint(40.4410968, 68.3120876), GeoPoint(40.4394235, 68.3106921), GeoPoint(40.4390900, 68.3107578), GeoPoint(40.4389023, 68.3110033), GeoPoint(40.4388535, 68.3113694), GeoPoint(40.4366265, 68.3170917), GeoPoint(40.4365615, 68.3174453), GeoPoint(40.4367866, 68.3179421), GeoPoint(40.4371602, 68.3181007), GeoPoint(40.4388365, 68.3194920))
        ),
        ContourLine(
            elevationMeters = 282.500,
            label = "282.500 m",
            points = listOf(GeoPoint(40.4006850, 68.2951792), GeoPoint(40.4017192, 68.2965473), GeoPoint(40.4020597, 68.2965536), GeoPoint(40.4023504, 68.2963445), GeoPoint(40.4023809, 68.2956777), GeoPoint(40.4012428, 68.2946001), GeoPoint(40.4011623, 68.2945591), GeoPoint(40.4006850, 68.2951792))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4444087, 68.2964168), GeoPoint(40.4460785, 68.2977653), GeoPoint(40.4460917, 68.2978118), GeoPoint(40.4462498, 68.2974606), GeoPoint(40.4462213, 68.2975163), GeoPoint(40.4458828, 68.2973359), GeoPoint(40.4444615, 68.2961780), GeoPoint(40.4444039, 68.2962004), GeoPoint(40.4444087, 68.2964168))
        ),
        ContourLine(
            elevationMeters = 279.000,
            label = "279.000 m",
            points = listOf(GeoPoint(40.4437548, 68.3051380), GeoPoint(40.4439036, 68.3034877), GeoPoint(40.4438434, 68.3036035), GeoPoint(40.4425652, 68.3031264), GeoPoint(40.4437152, 68.3040815), GeoPoint(40.4437548, 68.3051380))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4110460, 68.3153043), GeoPoint(40.4116744, 68.3133361), GeoPoint(40.4122170, 68.3128549), GeoPoint(40.4124293, 68.3129019), GeoPoint(40.4126918, 68.3127867), GeoPoint(40.4126588, 68.3126727), GeoPoint(40.4122868, 68.3122849), GeoPoint(40.4120854, 68.3119533), GeoPoint(40.4119140, 68.3117825), GeoPoint(40.4115578, 68.3118146), GeoPoint(40.4116468, 68.3125939), GeoPoint(40.4113110, 68.3128039), GeoPoint(40.4110767, 68.3128210), GeoPoint(40.4104579, 68.3147890), GeoPoint(40.4110460, 68.3153043))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4153219, 68.3196857), GeoPoint(40.4154879, 68.3195510), GeoPoint(40.4155543, 68.3189344), GeoPoint(40.4159375, 68.3177134), GeoPoint(40.4161035, 68.3175702), GeoPoint(40.4164308, 68.3175260), GeoPoint(40.4168303, 68.3177223), GeoPoint(40.4173876, 68.3170498), GeoPoint(40.4175955, 68.3169313), GeoPoint(40.4176456, 68.3172063), GeoPoint(40.4178992, 68.3176600), GeoPoint(40.4180519, 68.3174534), GeoPoint(40.4182384, 68.3174797), GeoPoint(40.4185098, 68.3172624), GeoPoint(40.4185167, 68.3164049), GeoPoint(40.4181125, 68.3164633), GeoPoint(40.4178501, 68.3166083), GeoPoint(40.4177002, 68.3162927), GeoPoint(40.4175913, 68.3158745), GeoPoint(40.4176383, 68.3152455), GeoPoint(40.4172759, 68.3148490), GeoPoint(40.4171620, 68.3146389), GeoPoint(40.4169559, 68.3145536), GeoPoint(40.4168120, 68.3141785), GeoPoint(40.4172857, 68.3139702), GeoPoint(40.4174528, 68.3135214), GeoPoint(40.4175627, 68.3127129), GeoPoint(40.4174424, 68.3115819), GeoPoint(40.4169245, 68.3119099), GeoPoint(40.4167327, 68.3124949), GeoPoint(40.4161948, 68.3127257), GeoPoint(40.4161766, 68.3128746), GeoPoint(40.4159622, 68.3129889), GeoPoint(40.4158604, 68.3131266), GeoPoint(40.4153443, 68.3132466), GeoPoint(40.4152109, 68.3139112), GeoPoint(40.4149506, 68.3143532), GeoPoint(40.4148867, 68.3143926), GeoPoint(40.4146227, 68.3152209), GeoPoint(40.4146393, 68.3152970), GeoPoint(40.4149972, 68.3154940), GeoPoint(40.4149187, 68.3162086), GeoPoint(40.4150812, 68.3169525), GeoPoint(40.4144656, 68.3189206), GeoPoint(40.4150153, 68.3194664), GeoPoint(40.4153219, 68.3196857))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4169855, 68.3211022), GeoPoint(40.4174015, 68.3213407), GeoPoint(40.4180172, 68.3193726), GeoPoint(40.4180038, 68.3192922), GeoPoint(40.4176522, 68.3190653), GeoPoint(40.4175044, 68.3190638), GeoPoint(40.4168887, 68.3210319), GeoPoint(40.4169855, 68.3211022))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4335669, 68.3369613), GeoPoint(40.4333070, 68.3391690), GeoPoint(40.4331260, 68.3400061), GeoPoint(40.4329048, 68.3422260), GeoPoint(40.4325836, 68.3438696), GeoPoint(40.4330215, 68.3444797), GeoPoint(40.4329067, 68.3447252), GeoPoint(40.4325334, 68.3449059), GeoPoint(40.4327043, 68.3456011), GeoPoint(40.4322014, 68.3460704), GeoPoint(40.4325582, 68.3467586), GeoPoint(40.4326520, 68.3471168), GeoPoint(40.4327844, 68.3469267), GeoPoint(40.4333409, 68.3473126), GeoPoint(40.4335584, 68.3472924), GeoPoint(40.4338557, 68.3478104), GeoPoint(40.4339761, 68.3482024), GeoPoint(40.4343437, 68.3485996), GeoPoint(40.4346413, 68.3490191), GeoPoint(40.4348924, 68.3491611), GeoPoint(40.4350060, 68.3493179), GeoPoint(40.4350903, 68.3494579), GeoPoint(40.4350859, 68.3497490), GeoPoint(40.4346903, 68.3497117), GeoPoint(40.4347066, 68.3499426), GeoPoint(40.4347638, 68.3500359), GeoPoint(40.4352867, 68.3502597), GeoPoint(40.4355270, 68.3502478), GeoPoint(40.4364344, 68.3505690), GeoPoint(40.4364544, 68.3504831), GeoPoint(40.4364431, 68.3505346))
        ),
        ContourLine(
            elevationMeters = 279.000,
            label = "279.000 m",
            points = listOf(GeoPoint(40.4237928, 68.3750757), GeoPoint(40.4235606, 68.3750318), GeoPoint(40.4241100, 68.3740573), GeoPoint(40.4249557, 68.3735708), GeoPoint(40.4247048, 68.3734330), GeoPoint(40.4254564, 68.3729093), GeoPoint(40.4271058, 68.3719240), GeoPoint(40.4272581, 68.3705269), GeoPoint(40.4270369, 68.3700849), GeoPoint(40.4269971, 68.3698158), GeoPoint(40.4271475, 68.3696940), GeoPoint(40.4275672, 68.3696754), GeoPoint(40.4279676, 68.3695415), GeoPoint(40.4281472, 68.3689828), GeoPoint(40.4286668, 68.3688687), GeoPoint(40.4290112, 68.3686885), GeoPoint(40.4291549, 68.3685539), GeoPoint(40.4294028, 68.3684735), GeoPoint(40.4295217, 68.3682579), GeoPoint(40.4302560, 68.3679140), GeoPoint(40.4303536, 68.3672922), GeoPoint(40.4307495, 68.3673381), GeoPoint(40.4313506, 68.3669362), GeoPoint(40.4316872, 68.3665935), GeoPoint(40.4319148, 68.3661410), GeoPoint(40.4322238, 68.3661795), GeoPoint(40.4323747, 68.3654029), GeoPoint(40.4327155, 68.3648718), GeoPoint(40.4325243, 68.3648060), GeoPoint(40.4323421, 68.3643805), GeoPoint(40.4320884, 68.3632585), GeoPoint(40.4326888, 68.3624970), GeoPoint(40.4329172, 68.3619632), GeoPoint(40.4332256, 68.3615353), GeoPoint(40.4331500, 68.3615834), GeoPoint(40.4331631, 68.3608129), GeoPoint(40.4332085, 68.3606068), GeoPoint(40.4330591, 68.3604334), GeoPoint(40.4329130, 68.3601487), GeoPoint(40.4329236, 68.3595152), GeoPoint(40.4327438, 68.3588415), GeoPoint(40.4327629, 68.3586144), GeoPoint(40.4327261, 68.3587861), GeoPoint(40.4324978, 68.3583228), GeoPoint(40.4321594, 68.3580451), GeoPoint(40.4320989, 68.3586151), GeoPoint(40.4318615, 68.3587253), GeoPoint(40.4317595, 68.3586369), GeoPoint(40.4316776, 68.3584668), GeoPoint(40.4317703, 68.3581232), GeoPoint(40.4321420, 68.3579982), GeoPoint(40.4327792, 68.3580664), GeoPoint(40.4325740, 68.3577397), GeoPoint(40.4321428, 68.3575788), GeoPoint(40.4319626, 68.3575599), GeoPoint(40.4317144, 68.3574093), GeoPoint(40.4315267, 68.3570182), GeoPoint(40.4315255, 68.3568641), GeoPoint(40.4315437, 68.3563846), GeoPoint(40.4319740, 68.3559549), GeoPoint(40.4320633, 68.3554915), GeoPoint(40.4321819, 68.3553786), GeoPoint(40.4324274, 68.3553281), GeoPoint(40.4328461, 68.3550527), GeoPoint(40.4329586, 68.3553935), GeoPoint(40.4332784, 68.3554704), GeoPoint(40.4335680, 68.3553893), GeoPoint(40.4335946, 68.3553119), GeoPoint(40.4335204, 68.3534085), GeoPoint(40.4334825, 68.3534304), GeoPoint(40.4334174, 68.3529520), GeoPoint(40.4332940, 68.3525686), GeoPoint(40.4330977, 68.3525456), GeoPoint(40.4329706, 68.3521451), GeoPoint(40.4329745, 68.3511693), GeoPoint(40.4329058, 68.3505669), GeoPoint(40.4332370, 68.3500401), GeoPoint(40.4329654, 68.3495218), GeoPoint(40.4329218, 68.3493470), GeoPoint(40.4329325, 68.3490515), GeoPoint(40.4332806, 68.3483320), GeoPoint(40.4326226, 68.3479860), GeoPoint(40.4321855, 68.3478466), GeoPoint(40.4314507, 68.3473947), GeoPoint(40.4313575, 68.3473917), GeoPoint(40.4309814, 68.3471402), GeoPoint(40.4308083, 68.3469244), GeoPoint(40.4306065, 68.3468245), GeoPoint(40.4306409, 68.3470166), GeoPoint(40.4303666, 68.3470675), GeoPoint(40.4297551, 68.3468878), GeoPoint(40.4297336, 68.3466998), GeoPoint(40.4297750, 68.3465794), GeoPoint(40.4300075, 68.3464092), GeoPoint(40.4302838, 68.3459817), GeoPoint(40.4301308, 68.3453505), GeoPoint(40.4301933, 68.3447419), GeoPoint(40.4300881, 68.3440972), GeoPoint(40.4302683, 68.3438979), GeoPoint(40.4306109, 68.3436534), GeoPoint(40.4304654, 68.3432831), GeoPoint(40.4304579, 68.3431335), GeoPoint(40.4302655, 68.3428024), GeoPoint(40.4304792, 68.3414342), GeoPoint(40.4305809, 68.3414028), GeoPoint(40.4303764, 68.3370065))
        ),
        ContourLine(
            elevationMeters = 279.500,
            label = "279.500 m",
            points = listOf(GeoPoint(40.4203806, 68.3757767), GeoPoint(40.4202452, 68.3757529), GeoPoint(40.4205686, 68.3751752), GeoPoint(40.4210736, 68.3748859), GeoPoint(40.4209236, 68.3748023), GeoPoint(40.4231375, 68.3733688), GeoPoint(40.4236206, 68.3728787), GeoPoint(40.4236631, 68.3725700), GeoPoint(40.4235816, 68.3723015), GeoPoint(40.4230708, 68.3711575), GeoPoint(40.4231698, 68.3710320), GeoPoint(40.4213208, 68.3699833), GeoPoint(40.4212668, 68.3700011), GeoPoint(40.4214148, 68.3697937), GeoPoint(40.4215670, 68.3683925), GeoPoint(40.4216196, 68.3683276), GeoPoint(40.4216841, 68.3683395), GeoPoint(40.4221113, 68.3685818), GeoPoint(40.4226196, 68.3687417), GeoPoint(40.4230554, 68.3687272), GeoPoint(40.4234327, 68.3687948), GeoPoint(40.4235032, 68.3686826), GeoPoint(40.4234330, 68.3690302), GeoPoint(40.4235343, 68.3693155), GeoPoint(40.4237143, 68.3692189), GeoPoint(40.4238615, 68.3688703), GeoPoint(40.4237411, 68.3685895), GeoPoint(40.4236034, 68.3684844), GeoPoint(40.4236736, 68.3682481), GeoPoint(40.4239481, 68.3682015), GeoPoint(40.4243559, 68.3684399), GeoPoint(40.4245134, 68.3683393), GeoPoint(40.4244086, 68.3681524), GeoPoint(40.4240351, 68.3681019), GeoPoint(40.4240124, 68.3674262), GeoPoint(40.4242696, 68.3672215), GeoPoint(40.4244025, 68.3670485), GeoPoint(40.4245487, 68.3666657), GeoPoint(40.4245472, 68.3665031), GeoPoint(40.4248834, 68.3661476), GeoPoint(40.4252976, 68.3661590), GeoPoint(40.4256889, 68.3663805), GeoPoint(40.4258871, 68.3663564), GeoPoint(40.4258065, 68.3662334), GeoPoint(40.4259390, 68.3658208), GeoPoint(40.4263141, 68.3658113), GeoPoint(40.4267858, 68.3657064), GeoPoint(40.4269703, 68.3657638), GeoPoint(40.4272606, 68.3655972), GeoPoint(40.4277402, 68.3656548), GeoPoint(40.4279923, 68.3651635), GeoPoint(40.4279742, 68.3648685), GeoPoint(40.4283728, 68.3648973), GeoPoint(40.4286201, 68.3651292), GeoPoint(40.4288787, 68.3651984), GeoPoint(40.4291512, 68.3649679), GeoPoint(40.4294848, 68.3648563), GeoPoint(40.4296398, 68.3648927), GeoPoint(40.4298597, 68.3648426), GeoPoint(40.4300106, 68.3646265), GeoPoint(40.4300856, 68.3625500), GeoPoint(40.4305451, 68.3623554), GeoPoint(40.4307940, 68.3620866), GeoPoint(40.4308957, 68.3620553), GeoPoint(40.4312144, 68.3623162), GeoPoint(40.4314443, 68.3620563), GeoPoint(40.4318880, 68.3609803), GeoPoint(40.4317181, 68.3600925), GeoPoint(40.4315933, 68.3601028), GeoPoint(40.4312889, 68.3598930), GeoPoint(40.4309908, 68.3594563), GeoPoint(40.4309856, 68.3588316), GeoPoint(40.4306763, 68.3581169), GeoPoint(40.4305447, 68.3574469), GeoPoint(40.4300134, 68.3570434), GeoPoint(40.4302071, 68.3568653), GeoPoint(40.4302854, 68.3565775), GeoPoint(40.4301765, 68.3562495), GeoPoint(40.4298129, 68.3557667), GeoPoint(40.4297924, 68.3556129), GeoPoint(40.4299750, 68.3546047), GeoPoint(40.4302308, 68.3544642), GeoPoint(40.4306476, 68.3543472), GeoPoint(40.4307228, 68.3542819), GeoPoint(40.4307272, 68.3541021), GeoPoint(40.4305552, 68.3535910), GeoPoint(40.4307609, 68.3531602), GeoPoint(40.4308485, 68.3530819), GeoPoint(40.4307694, 68.3530103), GeoPoint(40.4303508, 68.3528449), GeoPoint(40.4302034, 68.3528513), GeoPoint(40.4300691, 68.3534180), GeoPoint(40.4297262, 68.3537651), GeoPoint(40.4294239, 68.3536281), GeoPoint(40.4292921, 68.3536171), GeoPoint(40.4290766, 68.3537100), GeoPoint(40.4289527, 68.3543065), GeoPoint(40.4287564, 68.3541723), GeoPoint(40.4286179, 68.3539303), GeoPoint(40.4287298, 68.3535864), GeoPoint(40.4286689, 68.3529197), GeoPoint(40.4283870, 68.3519351), GeoPoint(40.4284058, 68.3518107), GeoPoint(40.4282109, 68.3508378), GeoPoint(40.4280985, 68.3497225), GeoPoint(40.4281618, 68.3493621), GeoPoint(40.4279345, 68.3492669), GeoPoint(40.4274006, 68.3493300), GeoPoint(40.4274826, 68.3487255), GeoPoint(40.4274541, 68.3485162), GeoPoint(40.4276906, 68.3481534), GeoPoint(40.4277570, 68.3476775), GeoPoint(40.4275948, 68.3469523), GeoPoint(40.4277292, 68.3466081), GeoPoint(40.4276676, 68.3462538), GeoPoint(40.4274463, 68.3463638), GeoPoint(40.4273219, 68.3466095), GeoPoint(40.4271399, 68.3464195), GeoPoint(40.4270866, 68.3462405), GeoPoint(40.4277417, 68.3458205), GeoPoint(40.4279922, 68.3457186), GeoPoint(40.4279662, 68.3454879), GeoPoint(40.4280968, 68.3452336), GeoPoint(40.4280963, 68.3448827), GeoPoint(40.4282060, 68.3446843), GeoPoint(40.4281412, 68.3442187), GeoPoint(40.4280324, 68.3440063), GeoPoint(40.4278189, 68.3438338), GeoPoint(40.4278234, 68.3433246), GeoPoint(40.4279828, 68.3430698), GeoPoint(40.4280838, 68.3427903), GeoPoint(40.4279105, 68.3423477), GeoPoint(40.4275959, 68.3418942), GeoPoint(40.4275912, 68.3416247), GeoPoint(40.4278065, 68.3404192), GeoPoint(40.4280964, 68.3382498), GeoPoint(40.4279843, 68.3370403))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4226230, 68.3371162), GeoPoint(40.4225857, 68.3385930), GeoPoint(40.4223704, 68.3397984), GeoPoint(40.4222117, 68.3414052), GeoPoint(40.4221644, 68.3416541), GeoPoint(40.4219083, 68.3418930), GeoPoint(40.4216642, 68.3424356), GeoPoint(40.4217810, 68.3430330), GeoPoint(40.4219766, 68.3432527), GeoPoint(40.4220724, 68.3436793), GeoPoint(40.4217404, 68.3438465), GeoPoint(40.4213743, 68.3437233), GeoPoint(40.4210150, 68.3435016), GeoPoint(40.4208865, 68.3434948), GeoPoint(40.4199878, 68.3430325), GeoPoint(40.4196197, 68.3451642), GeoPoint(40.4188531, 68.3462746), GeoPoint(40.4171754, 68.3480480), GeoPoint(40.4156696, 68.3495537), GeoPoint(40.4151170, 68.3501904), GeoPoint(40.4153934, 68.3502079), GeoPoint(40.4155693, 68.3502996), GeoPoint(40.4152495, 68.3508902), GeoPoint(40.4151901, 68.3512761), GeoPoint(40.4180550, 68.3502863), GeoPoint(40.4184458, 68.3500455), GeoPoint(40.4188953, 68.3501719), GeoPoint(40.4191458, 68.3500700), GeoPoint(40.4191601, 68.3500099), GeoPoint(40.4200445, 68.3505324), GeoPoint(40.4200619, 68.3504679), GeoPoint(40.4204135, 68.3516440), GeoPoint(40.4204735, 68.3524989), GeoPoint(40.4205854, 68.3530408), GeoPoint(40.4209717, 68.3533092), GeoPoint(40.4214365, 68.3534097), GeoPoint(40.4217217, 68.3537310), GeoPoint(40.4220875, 68.3539569), GeoPoint(40.4223723, 68.3542653), GeoPoint(40.4227236, 68.3543246), GeoPoint(40.4228318, 68.3544044), GeoPoint(40.4229172, 68.3545830), GeoPoint(40.4228903, 68.3546518), GeoPoint(40.4223805, 68.3544407), GeoPoint(40.4220741, 68.3543807), GeoPoint(40.4217363, 68.3545694), GeoPoint(40.4206304, 68.3542639), GeoPoint(40.4204133, 68.3540786), GeoPoint(40.4199403, 68.3542521), GeoPoint(40.4196103, 68.3544877), GeoPoint(40.4194120, 68.3547301), GeoPoint(40.4191865, 68.3551397), GeoPoint(40.4182846, 68.3546773), GeoPoint(40.4180065, 68.3550448), GeoPoint(40.4171608, 68.3558652), GeoPoint(40.4175181, 68.3559073), GeoPoint(40.4180491, 68.3558529), GeoPoint(40.4189476, 68.3563111), GeoPoint(40.4190493, 68.3564979), GeoPoint(40.4188666, 68.3568385), GeoPoint(40.4188294, 68.3571043), GeoPoint(40.4179147, 68.3566420), GeoPoint(40.4176503, 68.3572618), GeoPoint(40.4176456, 68.3573217), GeoPoint(40.4177637, 68.3574143), GeoPoint(40.4186623, 68.3578725), GeoPoint(40.4187204, 68.3582182), GeoPoint(40.4186106, 68.3586390), GeoPoint(40.4185275, 68.3586488), GeoPoint(40.4183979, 68.3584922), GeoPoint(40.4184216, 68.3583122), GeoPoint(40.4177182, 68.3579497), GeoPoint(40.4174638, 68.3584709), GeoPoint(40.4173563, 68.3593025), GeoPoint(40.4170863, 68.3596185), GeoPoint(40.4166656, 68.3596029), GeoPoint(40.4170485, 68.3599742), GeoPoint(40.4170637, 68.3603933), GeoPoint(40.4165830, 68.3614096), GeoPoint(40.4163945, 68.3622166), GeoPoint(40.4163271, 68.3625470), GeoPoint(40.4164787, 68.3626861), GeoPoint(40.4165149, 68.3628310), GeoPoint(40.4163213, 68.3627909), GeoPoint(40.4162023, 68.3626685), GeoPoint(40.4162086, 68.3627754), GeoPoint(40.4156536, 68.3625520), GeoPoint(40.4158181, 68.3626952), GeoPoint(40.4161121, 68.3632131), GeoPoint(40.4161516, 68.3631355), GeoPoint(40.4161982, 68.3633060), GeoPoint(40.4159925, 68.3634030), GeoPoint(40.4160601, 68.3632994), GeoPoint(40.4157004, 68.3635097), GeoPoint(40.4155965, 68.3634641), GeoPoint(40.4152794, 68.3634813), GeoPoint(40.4151677, 68.3632774), GeoPoint(40.4149728, 68.3634170), GeoPoint(40.4148042, 68.3639114), GeoPoint(40.4149561, 68.3645083), GeoPoint(40.4150257, 68.3645843), GeoPoint(40.4144120, 68.3644387), GeoPoint(40.4142312, 68.3646209), GeoPoint(40.4143299, 68.3652656), GeoPoint(40.4142907, 68.3655742), GeoPoint(40.4141747, 68.3655544), GeoPoint(40.4138822, 68.3656482), GeoPoint(40.4136773, 68.3656596), GeoPoint(40.4134497, 68.3655558), GeoPoint(40.4131890, 68.3655251), GeoPoint(40.4131486, 68.3654572), GeoPoint(40.4127613, 68.3655994), GeoPoint(40.4128481, 68.3659405), GeoPoint(40.4127001, 68.3668153), GeoPoint(40.4125559, 68.3668216), GeoPoint(40.4126689, 68.3669569), GeoPoint(40.4126580, 68.3671368), GeoPoint(40.4127528, 68.3675333), GeoPoint(40.4126470, 68.3680910), GeoPoint(40.4126882, 68.3688562), GeoPoint(40.4126056, 68.3695504), GeoPoint(40.4127343, 68.3694545), GeoPoint(40.4130674, 68.3696596), GeoPoint(40.4131555, 68.3701590), GeoPoint(40.4130574, 68.3704256), GeoPoint(40.4131943, 68.3708387), GeoPoint(40.4135120, 68.3711766), GeoPoint(40.4137108, 68.3715076), GeoPoint(40.4135144, 68.3717071), GeoPoint(40.4138209, 68.3717671), GeoPoint(40.4140652, 68.3714515), GeoPoint(40.4140923, 68.3717249), GeoPoint(40.4139169, 68.3719840), GeoPoint(40.4141351, 68.3724303), GeoPoint(40.4144679, 68.3728493), GeoPoint(40.4151806, 68.3730920), GeoPoint(40.4152182, 68.3730616), GeoPoint(40.4160590, 68.3738545), GeoPoint(40.4163541, 68.3737435), GeoPoint(40.4164618, 68.3738062), GeoPoint(40.4165926, 68.3742280), GeoPoint(40.4165425, 68.3747164), GeoPoint(40.4157523, 68.3753518), GeoPoint(40.4149206, 68.3749866), GeoPoint(40.4154228, 68.3750483), GeoPoint(40.4149056, 68.3745761), GeoPoint(40.4147188, 68.3743262), GeoPoint(40.4144246, 68.3746939), GeoPoint(40.4145856, 68.3741569), GeoPoint(40.4146696, 68.3735054), GeoPoint(40.4145385, 68.3731863), GeoPoint(40.4145878, 68.3735621), GeoPoint(40.4142169, 68.3736057), GeoPoint(40.4140917, 68.3739368), GeoPoint(40.4140715, 68.3743478), GeoPoint(40.4143931, 68.3747114), GeoPoint(40.4143763, 68.3749085), GeoPoint(40.4144331, 68.3752114), GeoPoint(40.4146076, 68.3753674), GeoPoint(40.4143168, 68.3752943), GeoPoint(40.4142602, 68.3758940), GeoPoint(40.4139130, 68.3770967))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4086713, 68.3747464), GeoPoint(40.4090184, 68.3735395), GeoPoint(40.4089625, 68.3722654), GeoPoint(40.4090680, 68.3716950), GeoPoint(40.4092740, 68.3711617), GeoPoint(40.4090666, 68.3708651), GeoPoint(40.4089152, 68.3707303), GeoPoint(40.4086543, 68.3702461), GeoPoint(40.4085502, 68.3698625), GeoPoint(40.4085484, 68.3695759), GeoPoint(40.4088719, 68.3693362), GeoPoint(40.4090565, 68.3690642), GeoPoint(40.4092779, 68.3689542), GeoPoint(40.4093628, 68.3687819), GeoPoint(40.4096368, 68.3684958), GeoPoint(40.4097524, 68.3684985), GeoPoint(40.4096968, 68.3681271), GeoPoint(40.4097095, 68.3677889), GeoPoint(40.4098067, 68.3677106), GeoPoint(40.4099957, 68.3673658), GeoPoint(40.4098022, 68.3671074), GeoPoint(40.4095264, 68.3668888), GeoPoint(40.4090890, 68.3669589), GeoPoint(40.4089339, 68.3669225), GeoPoint(40.4087837, 68.3670529), GeoPoint(40.4086341, 68.3669823), GeoPoint(40.4086717, 68.3667251), GeoPoint(40.4088827, 68.3665896), GeoPoint(40.4089497, 68.3666913), GeoPoint(40.4090497, 68.3664846), GeoPoint(40.4091874, 68.3663672), GeoPoint(40.4098733, 68.3661225), GeoPoint(40.4097550, 68.3660257), GeoPoint(40.4095356, 68.3656480), GeoPoint(40.4093992, 68.3652563), GeoPoint(40.4100957, 68.3641558), GeoPoint(40.4103194, 68.3639046), GeoPoint(40.4102596, 68.3637214), GeoPoint(40.4099370, 68.3634350), GeoPoint(40.4100412, 68.3631554), GeoPoint(40.4102114, 68.3630504), GeoPoint(40.4104856, 68.3631023), GeoPoint(40.4106913, 68.3630053), GeoPoint(40.4108007, 68.3630167), GeoPoint(40.4111570, 68.3628021), GeoPoint(40.4115945, 68.3626207), GeoPoint(40.4124268, 68.3618948), GeoPoint(40.4125967, 68.3621149), GeoPoint(40.4128534, 68.3621156), GeoPoint(40.4130859, 68.3625018), GeoPoint(40.4132995, 68.3622336), GeoPoint(40.4132927, 68.3619983), GeoPoint(40.4133351, 68.3619122), GeoPoint(40.4136179, 68.3618185), GeoPoint(40.4136494, 68.3613517), GeoPoint(40.4137953, 68.3609603), GeoPoint(40.4141880, 68.3610063), GeoPoint(40.4145052, 68.3606596), GeoPoint(40.4148975, 68.3605815), GeoPoint(40.4144111, 68.3600662), GeoPoint(40.4138218, 68.3596551), GeoPoint(40.4137376, 68.3594038), GeoPoint(40.4139286, 68.3592429), GeoPoint(40.4140512, 68.3589331), GeoPoint(40.4141265, 68.3583203), GeoPoint(40.4140465, 68.3576583), GeoPoint(40.4141573, 68.3568310), GeoPoint(40.4146063, 68.3560504), GeoPoint(40.4154227, 68.3551063), GeoPoint(40.4158319, 68.3545017), GeoPoint(40.4137441, 68.3546291), GeoPoint(40.4132755, 68.3547340), GeoPoint(40.4135470, 68.3544692), GeoPoint(40.4128591, 68.3547526), GeoPoint(40.4127975, 68.3547321), GeoPoint(40.4117385, 68.3553842), GeoPoint(40.4116099, 68.3553732), GeoPoint(40.4115312, 68.3559818), GeoPoint(40.4116278, 68.3562157), GeoPoint(40.4114170, 68.3563598), GeoPoint(40.4112361, 68.3563153), GeoPoint(40.4108808, 68.3560079), GeoPoint(40.4107790, 68.3558168), GeoPoint(40.4108865, 68.3549853), GeoPoint(40.4112674, 68.3542870), GeoPoint(40.4108893, 68.3540826), GeoPoint(40.4102982, 68.3540523), GeoPoint(40.4114838, 68.3526754), GeoPoint(40.4125587, 68.3515737), GeoPoint(40.4173931, 68.3444895), GeoPoint(40.4179093, 68.3439260), GeoPoint(40.4159776, 68.3445693), GeoPoint(40.4161650, 68.3442843), GeoPoint(40.4161392, 68.3441691), GeoPoint(40.4158589, 68.3442372), GeoPoint(40.4159900, 68.3437818), GeoPoint(40.4159046, 68.3437103), GeoPoint(40.4160827, 68.3432158), GeoPoint(40.4163118, 68.3429302), GeoPoint(40.4155461, 68.3372164))
        ),
        ContourLine(
            elevationMeters = 281.500,
            label = "281.500 m",
            points = listOf(GeoPoint(40.4071304, 68.3740358), GeoPoint(40.4074807, 68.3728289), GeoPoint(40.4075973, 68.3727589), GeoPoint(40.4076835, 68.3722957), GeoPoint(40.4076237, 68.3720013), GeoPoint(40.4073743, 68.3716967), GeoPoint(40.4072573, 68.3708598), GeoPoint(40.4072279, 68.3702870), GeoPoint(40.4072758, 68.3697217), GeoPoint(40.4072168, 68.3691193), GeoPoint(40.4074858, 68.3694364), GeoPoint(40.4075982, 68.3691055), GeoPoint(40.4075651, 68.3688493), GeoPoint(40.4073769, 68.3686636), GeoPoint(40.4073401, 68.3687197), GeoPoint(40.4071962, 68.3687388), GeoPoint(40.4064323, 68.3692797), GeoPoint(40.4062959, 68.3694441), GeoPoint(40.4061458, 68.3693564), GeoPoint(40.4059244, 68.3694663), GeoPoint(40.4052449, 68.3690393), GeoPoint(40.4049472, 68.3687268), GeoPoint(40.4045716, 68.3687148), GeoPoint(40.4042606, 68.3689458), GeoPoint(40.4034015, 68.3672935), GeoPoint(40.4038762, 68.3669576), GeoPoint(40.4040104, 68.3662713), GeoPoint(40.4041544, 68.3658116), GeoPoint(40.4043874, 68.3656587), GeoPoint(40.4046323, 68.3656982), GeoPoint(40.4050856, 68.3656235), GeoPoint(40.4046770, 68.3645768), GeoPoint(40.4046238, 68.3641796), GeoPoint(40.4047977, 68.3640917), GeoPoint(40.4050212, 68.3640544), GeoPoint(40.4055347, 68.3641757), GeoPoint(40.4056912, 68.3642634), GeoPoint(40.4058072, 68.3640565), GeoPoint(40.4064511, 68.3620241), GeoPoint(40.4063076, 68.3616069), GeoPoint(40.4065132, 68.3612832), GeoPoint(40.4066317, 68.3604987), GeoPoint(40.4068353, 68.3599954), GeoPoint(40.4068383, 68.3593195), GeoPoint(40.4065000, 68.3588236), GeoPoint(40.4070988, 68.3584560), GeoPoint(40.4073603, 68.3579561), GeoPoint(40.4075411, 68.3577739), GeoPoint(40.4078456, 68.3576542), GeoPoint(40.4079073, 68.3576791), GeoPoint(40.4080055, 68.3576392), GeoPoint(40.4079611, 68.3574345), GeoPoint(40.4078231, 68.3573209), GeoPoint(40.4075971, 68.3568235), GeoPoint(40.4076923, 68.3567879), GeoPoint(40.4078602, 68.3566059), GeoPoint(40.4080408, 68.3566376), GeoPoint(40.4080729, 68.3564147), GeoPoint(40.4078336, 68.3559047), GeoPoint(40.4075583, 68.3555920), GeoPoint(40.4074697, 68.3556360), GeoPoint(40.4071272, 68.3555509), GeoPoint(40.4070653, 68.3554063), GeoPoint(40.4066924, 68.3552660), GeoPoint(40.4065640, 68.3552635), GeoPoint(40.4065488, 68.3550712), GeoPoint(40.4067102, 68.3549962), GeoPoint(40.4071436, 68.3552298), GeoPoint(40.4075254, 68.3548994), GeoPoint(40.4076824, 68.3542256), GeoPoint(40.4080222, 68.3541053), GeoPoint(40.4081821, 68.3539791), GeoPoint(40.4113847, 68.3492499), GeoPoint(40.4130949, 68.3473778), GeoPoint(40.4118939, 68.3477796), GeoPoint(40.4122657, 68.3472140), GeoPoint(40.4122111, 68.3469880), GeoPoint(40.4116599, 68.3471198), GeoPoint(40.4117443, 68.3468234), GeoPoint(40.4115767, 68.3466846), GeoPoint(40.4116957, 68.3463620), GeoPoint(40.4121507, 68.3457909), GeoPoint(40.4110121, 68.3372805))
        ),
        ContourLine(
            elevationMeters = 282.500,
            label = "282.500 m",
            points = listOf(GeoPoint(40.3975802, 68.3697771), GeoPoint(40.3979305, 68.3685746), GeoPoint(40.3979379, 68.3679415), GeoPoint(40.3981966, 68.3674546), GeoPoint(40.3982039, 68.3668172), GeoPoint(40.3986310, 68.3667215), GeoPoint(40.3989437, 68.3665504), GeoPoint(40.3989962, 68.3663700), GeoPoint(40.3990491, 68.3659801), GeoPoint(40.3990530, 68.3653341), GeoPoint(40.3989793, 68.3645566), GeoPoint(40.3991083, 68.3642469), GeoPoint(40.3991285, 68.3637248), GeoPoint(40.3989253, 68.3635736), GeoPoint(40.3991912, 68.3635614), GeoPoint(40.3996730, 68.3632510), GeoPoint(40.3998818, 68.3628161), GeoPoint(40.4000507, 68.3627753), GeoPoint(40.4001783, 68.3626410), GeoPoint(40.4003255, 68.3622925), GeoPoint(40.4010445, 68.3613073), GeoPoint(40.4013037, 68.3607305), GeoPoint(40.4017689, 68.3602878), GeoPoint(40.4017718, 68.3597188), GeoPoint(40.4020346, 68.3592660), GeoPoint(40.4020316, 68.3591634), GeoPoint(40.4022722, 68.3588307), GeoPoint(40.4023636, 68.3587738), GeoPoint(40.4022338, 68.3586088), GeoPoint(40.4024983, 68.3582159), GeoPoint(40.4024990, 68.3581303), GeoPoint(40.4029191, 68.3577909), GeoPoint(40.4028209, 68.3572789), GeoPoint(40.4027169, 68.3573445), GeoPoint(40.4026567, 68.3570373), GeoPoint(40.4028404, 68.3569535), GeoPoint(40.4029050, 68.3570810), GeoPoint(40.4031497, 68.3564445), GeoPoint(40.4037434, 68.3562309), GeoPoint(40.4035888, 68.3555443), GeoPoint(40.4038639, 68.3551855), GeoPoint(40.4040130, 68.3551278), GeoPoint(40.4040531, 68.3545155), GeoPoint(40.4039597, 68.3540591), GeoPoint(40.4032574, 68.3536240), GeoPoint(40.4029257, 68.3533548), GeoPoint(40.4028942, 68.3532654), GeoPoint(40.4031449, 68.3528384), GeoPoint(40.4033051, 68.3521646), GeoPoint(40.4038314, 68.3515071), GeoPoint(40.4036716, 68.3503073), GeoPoint(40.4035297, 68.3481747), GeoPoint(40.4033227, 68.3468985), GeoPoint(40.4030141, 68.3467660), GeoPoint(40.4027250, 68.3468641), GeoPoint(40.4026151, 68.3457278), GeoPoint(40.4026112, 68.3440425), GeoPoint(40.4025681, 68.3437736), GeoPoint(40.4027520, 68.3373973))
        ),
        ContourLine(
            elevationMeters = 283.000,
            label = "283.000 m",
            points = listOf(GeoPoint(40.3960760, 68.3374917), GeoPoint(40.3980663, 68.3580376), GeoPoint(40.3981430, 68.3576944), GeoPoint(40.3968249, 68.3647145), GeoPoint(40.3975563, 68.3649355), GeoPoint(40.3976769, 68.3647798), GeoPoint(40.3978551, 68.3640631), GeoPoint(40.3974909, 68.3637815), GeoPoint(40.3971611, 68.3636877), GeoPoint(40.3981545, 68.3576472), GeoPoint(40.3990133, 68.3572846), GeoPoint(40.3998875, 68.3575633), GeoPoint(40.3998827, 68.3575078), GeoPoint(40.4002267, 68.3571993), GeoPoint(40.4002376, 68.3571350), GeoPoint(40.4002039, 68.3569686), GeoPoint(40.4001010, 68.3570684), GeoPoint(40.3998356, 68.3570978), GeoPoint(40.3991501, 68.3569105), GeoPoint(40.3981845, 68.3575740), GeoPoint(40.3983348, 68.3572255), GeoPoint(40.3982138, 68.3574795), GeoPoint(40.3978492, 68.3374666))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4332430, 68.3692683), GeoPoint(40.4323355, 68.3689469), GeoPoint(40.4320947, 68.3689373), GeoPoint(40.4319175, 68.3687985), GeoPoint(40.4316137, 68.3688326), GeoPoint(40.4315069, 68.3689111), GeoPoint(40.4313788, 68.3689172), GeoPoint(40.4313325, 68.3695383), GeoPoint(40.4308609, 68.3696475), GeoPoint(40.4306529, 68.3695562), GeoPoint(40.4304373, 68.3696447), GeoPoint(40.4300010, 68.3696421), GeoPoint(40.4296585, 68.3698907), GeoPoint(40.4296112, 68.3701439), GeoPoint(40.4290450, 68.3705324), GeoPoint(40.4287000, 68.3710293), GeoPoint(40.4285477, 68.3724264), GeoPoint(40.4276483, 68.3729436), GeoPoint(40.4268761, 68.3743149), GeoPoint(40.4272018, 68.3743747))
        ),
        ContourLine(
            elevationMeters = 281.500,
            label = "281.500 m",
            points = listOf(GeoPoint(40.4009309, 68.3712884), GeoPoint(40.4012779, 68.3700816), GeoPoint(40.4010260, 68.3697985), GeoPoint(40.4008231, 68.3698825), GeoPoint(40.4004760, 68.3710849))
        ),
        ContourLine(
            elevationMeters = 281.500,
            label = "281.500 m",
            points = listOf(GeoPoint(40.4026010, 68.3720270), GeoPoint(40.4030309, 68.3703571), GeoPoint(40.4029537, 68.3695668), GeoPoint(40.4029854, 68.3691087), GeoPoint(40.4030366, 68.3689925), GeoPoint(40.4029893, 68.3689118), GeoPoint(40.4024434, 68.3688894), GeoPoint(40.4021726, 68.3689572), GeoPoint(40.4020668, 68.3691811), GeoPoint(40.4021205, 68.3698220), GeoPoint(40.4018725, 68.3701205), GeoPoint(40.4016713, 68.3702645), GeoPoint(40.4013242, 68.3714669))
        ),
        ContourLine(
            elevationMeters = 281.000,
            label = "281.000 m",
            points = listOf(GeoPoint(40.4083301, 68.3745927), GeoPoint(40.4086805, 68.3733901), GeoPoint(40.4085342, 68.3732124), GeoPoint(40.4084561, 68.3732862), GeoPoint(40.4081090, 68.3744888))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4133640, 68.3768560), GeoPoint(40.4137091, 68.3755807), GeoPoint(40.4136640, 68.3755727), GeoPoint(40.4133744, 68.3750975), GeoPoint(40.4130264, 68.3747086), GeoPoint(40.4126725, 68.3747819), GeoPoint(40.4124357, 68.3749134), GeoPoint(40.4123601, 68.3744053), GeoPoint(40.4122841, 68.3742139), GeoPoint(40.4123132, 68.3737771), GeoPoint(40.4124407, 68.3731892), GeoPoint(40.4124953, 68.3729703), GeoPoint(40.4126508, 68.3726858), GeoPoint(40.4124534, 68.3719569), GeoPoint(40.4129112, 68.3717025), GeoPoint(40.4132542, 68.3716936), GeoPoint(40.4126133, 68.3712702), GeoPoint(40.4125434, 68.3709588), GeoPoint(40.4125157, 68.3703303), GeoPoint(40.4124200, 68.3701262), GeoPoint(40.4121699, 68.3703521), GeoPoint(40.4119827, 68.3704231), GeoPoint(40.4118027, 68.3706352), GeoPoint(40.4117171, 68.3710043), GeoPoint(40.4118456, 68.3716828), GeoPoint(40.4114983, 68.3718758), GeoPoint(40.4113931, 68.3721211), GeoPoint(40.4110791, 68.3725788), GeoPoint(40.4108000, 68.3736950), GeoPoint(40.4105682, 68.3740019), GeoPoint(40.4103476, 68.3741375), GeoPoint(40.4100005, 68.3753444))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4217729, 68.3407653), GeoPoint(40.4218455, 68.3405032), GeoPoint(40.4217425, 68.3402694), GeoPoint(40.4215229, 68.3402168), GeoPoint(40.4214086, 68.3405907), GeoPoint(40.4215898, 68.3406438), GeoPoint(40.4211932, 68.3411286), GeoPoint(40.4202945, 68.3406663), GeoPoint(40.4203304, 68.3404690), GeoPoint(40.4200578, 68.3413543), GeoPoint(40.4201661, 68.3413271), GeoPoint(40.4201221, 68.3416914), GeoPoint(40.4210209, 68.3421537), GeoPoint(40.4212922, 68.3422183), GeoPoint(40.4214295, 68.3421950), GeoPoint(40.4218824, 68.3418848), GeoPoint(40.4217729, 68.3407653))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4206336, 68.3453939), GeoPoint(40.4197350, 68.3449358), GeoPoint(40.4196119, 68.3454510), GeoPoint(40.4196420, 68.3453821), GeoPoint(40.4201305, 68.3456320), GeoPoint(40.4205138, 68.3459090), GeoPoint(40.4207071, 68.3457180), GeoPoint(40.4207848, 68.3455201), GeoPoint(40.4206336, 68.3453939))
        ),
        ContourLine(
            elevationMeters = 278.500,
            label = "278.500 m",
            points = listOf(GeoPoint(40.4357506, 68.3523247), GeoPoint(40.4360520, 68.3524317), GeoPoint(40.4361512, 68.3519767), GeoPoint(40.4352437, 68.3516555), GeoPoint(40.4350983, 68.3515078), GeoPoint(40.4350110, 68.3518171), GeoPoint(40.4351445, 68.3521106), GeoPoint(40.4357506, 68.3523247))
        ),
        ContourLine(
            elevationMeters = 280.500,
            label = "280.500 m",
            points = listOf(GeoPoint(40.4178821, 68.3684258), GeoPoint(40.4177812, 68.3682603), GeoPoint(40.4177483, 68.3685688), GeoPoint(40.4179005, 68.3685068), GeoPoint(40.4178821, 68.3684258))
        )
    )

}

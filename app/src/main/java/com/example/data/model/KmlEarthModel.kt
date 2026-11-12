package com.example.data.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class KmlGeometryType {
    POINT,
    LINE_STRING,
    POLYGON,
    MULTI_GEOMETRY
}

data class KmlStyle(
    val id: String = "default",
    val lineColor: Color = Color(0xFF00E5FF),
    val lineWidth: Float = 3f,
    val polyFillColor: Color = Color(0x4000E5FF),
    val isPolyFilled: Boolean = true,
    val isPolyOutlined: Boolean = true,
    val iconColor: Color = Color(0xFFFF5252),
    val iconScale: Float = 1.0f
)

data class KmlFeature(
    val id: String,
    val name: String,
    val description: String? = null,
    val folderName: String? = null,
    val geometryType: KmlGeometryType,
    val points: List<GeoPoint>,
    val innerBoundaries: List<List<GeoPoint>> = emptyList(),
    val style: KmlStyle = KmlStyle(),
    val extendedData: Map<String, String> = emptyMap(),
    val isVisible: Boolean = true
) {
    /**
     * Approximate center point of this feature.
     */
    val center: GeoPoint by lazy {
        if (points.isEmpty()) GeoPoint(0.0, 0.0)
        else if (points.size == 1) points[0]
        else {
            val avgLat = points.map { it.latitude }.average()
            val avgLng = points.map { it.longitude }.average()
            GeoPoint(avgLat, avgLng)
        }
    }

    /**
     * Path length in meters for LineString or Polygon perimeter.
     */
    val pathLengthMeters: Double by lazy {
        if (points.size < 2) 0.0
        else {
            var sum = 0.0
            for (i in 0 until points.size - 1) {
                sum += GeoUtils.distanceMeters(
                    points[i].latitude, points[i].longitude,
                    points[i + 1].latitude, points[i + 1].longitude
                )
            }
            if (geometryType == KmlGeometryType.POLYGON && points.size > 2) {
                sum += GeoUtils.distanceMeters(
                    points.last().latitude, points.last().longitude,
                    points.first().latitude, points.first().longitude
                )
            }
            sum
        }
    }

    /**
     * Geodesic polygon area in square meters using spherical excess.
     */
    val polygonAreaMeters: Double by lazy {
        if (geometryType != KmlGeometryType.POLYGON || points.size < 3) 0.0
        else calculateSphericalPolygonArea(points)
    }

    val polygonAreaHectares: Double get() = polygonAreaMeters / 10000.0
    val polygonAreaAcres: Double get() = polygonAreaMeters / 4046.85642
}

data class KmlFolder(
    val name: String,
    val features: List<KmlFeature>,
    val isVisible: Boolean = true
)

data class KmlBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
) {
    val centerLat: Double get() = (minLat + maxLat) / 2.0
    val centerLng: Double get() = (minLng + maxLng) / 2.0
    val spanLat: Double get() = (maxLat - minLat).coerceAtLeast(0.0001)
    val spanLng: Double get() = (maxLng - minLng).coerceAtLeast(0.0001)
}

data class KmlDocument(
    val id: String,
    val fileName: String,
    val name: String,
    val description: String? = null,
    val features: List<KmlFeature> = emptyList(),
    val folders: List<KmlFolder> = emptyList(),
    val bounds: KmlBounds = KmlBounds(0.0, 0.0, 0.0, 0.0),
    val rawContent: String? = null
) {
    val totalPoints: Int get() = features.count { it.geometryType == KmlGeometryType.POINT }
    val totalLines: Int get() = features.count { it.geometryType == KmlGeometryType.LINE_STRING }
    val totalPolygons: Int get() = features.count { it.geometryType == KmlGeometryType.POLYGON }
    val totalFeatures: Int get() = features.size

    val placemarks: List<KmlFeature> get() = features.filter { it.geometryType == KmlGeometryType.POINT }
    val lines: List<KmlFeature> get() = features.filter { it.geometryType == KmlGeometryType.LINE_STRING }
    val polygons: List<KmlFeature> get() = features.filter { it.geometryType == KmlGeometryType.POLYGON }

    val typedPlacemarks: List<KmlPlacemark> get() = placemarks.map {
        KmlPlacemark(
            id = it.id,
            name = it.name,
            description = it.description,
            folderName = it.folderName,
            coordinate = it.points.firstOrNull() ?: GeoPoint(0.0, 0.0),
            style = it.style,
            extendedData = it.extendedData
        )
    }

    val typedLines: List<KmlLine> get() = lines.map {
        KmlLine(
            id = it.id,
            name = it.name,
            description = it.description,
            folderName = it.folderName,
            points = it.points,
            style = it.style,
            extendedData = it.extendedData
        )
    }

    val typedPolygons: List<KmlPolygon> get() = polygons.map {
        KmlPolygon(
            id = it.id,
            name = it.name,
            description = it.description,
            folderName = it.folderName,
            outerBoundary = it.points,
            innerBoundaries = it.innerBoundaries,
            style = it.style,
            extendedData = it.extendedData
        )
    }
}

/**
 * Dedicated structure representing a KML Point Placemark (e.g., pump stations, valves, weather sensors, POIs).
 */
data class KmlPlacemark(
    val id: String,
    val name: String,
    val description: String? = null,
    val folderName: String? = null,
    val coordinate: GeoPoint,
    val altitude: Double = 0.0,
    val style: KmlStyle = KmlStyle(),
    val extendedData: Map<String, String> = emptyMap()
)

/**
 * Dedicated structure representing a KML LineString (e.g., feeder canals, mainlines, lateral pipelines, roads).
 */
data class KmlLine(
    val id: String,
    val name: String,
    val description: String? = null,
    val folderName: String? = null,
    val points: List<GeoPoint>,
    val style: KmlStyle = KmlStyle(),
    val extendedData: Map<String, String> = emptyMap()
) {
    val vertexCount: Int get() = points.size
    val lengthMeters: Double by lazy {
        if (points.size < 2) 0.0
        else {
            var sum = 0.0
            for (i in 0 until points.size - 1) {
                sum += GeoUtils.distanceMeters(
                    points[i].latitude, points[i].longitude,
                    points[i + 1].latitude, points[i + 1].longitude
                )
            }
            sum
        }
    }
    val lengthKm: Double get() = lengthMeters / 1000.0
}

/**
 * Dedicated structure representing a KML Polygon (e.g., agricultural field plots, administrative parcels, lakes).
 */
data class KmlPolygon(
    val id: String,
    val name: String,
    val description: String? = null,
    val folderName: String? = null,
    val outerBoundary: List<GeoPoint>,
    val innerBoundaries: List<List<GeoPoint>> = emptyList(),
    val style: KmlStyle = KmlStyle(),
    val extendedData: Map<String, String> = emptyMap()
) {
    val vertexCount: Int get() = outerBoundary.size
    val perimeterMeters: Double by lazy {
        if (outerBoundary.size < 2) 0.0
        else {
            var sum = 0.0
            for (i in 0 until outerBoundary.size - 1) {
                sum += GeoUtils.distanceMeters(
                    outerBoundary[i].latitude, outerBoundary[i].longitude,
                    outerBoundary[i + 1].latitude, outerBoundary[i + 1].longitude
                )
            }
            if (outerBoundary.size > 2) {
                sum += GeoUtils.distanceMeters(
                    outerBoundary.last().latitude, outerBoundary.last().longitude,
                    outerBoundary.first().latitude, outerBoundary.first().longitude
                )
            }
            sum
        }
    }
    val areaMeters: Double by lazy {
        calculateSphericalPolygonArea(outerBoundary)
    }
    val areaHectares: Double get() = areaMeters / 10000.0
    val areaAcres: Double get() = areaMeters / 4046.85642
}


data class MeasurePoint(
    val id: String,
    val geoPoint: GeoPoint,
    val label: String
)

/**
 * Calculates geodesic area of a spherical polygon using Girard's theorem.
 */
fun calculateSphericalPolygonArea(points: List<GeoPoint>): Double {
    if (points.size < 3) return 0.0
    val earthRadius = 6378137.0 // WGS84 Major Axis in meters
    var total = 0.0

    for (i in points.indices) {
        val p1 = points[i]
        val p2 = points[(i + 1) % points.size]
        val lat1 = Math.toRadians(p1.latitude)
        val lat2 = Math.toRadians(p2.latitude)
        val lon1 = Math.toRadians(p1.longitude)
        val lon2 = Math.toRadians(p2.longitude)

        total += (lon2 - lon1) * (2.0 + sin(lat1) + sin(lat2))
    }

    val area = abs(total * earthRadius * earthRadius / 2.0)
    return area
}

/**
 * Parses KML standard 8-digit hex color 'aabbggrr' into Compose Color.
 */
fun parseKmlColor(hex: String?, defaultColor: Color = Color(0xFF00E5FF)): Color {
    if (hex.isNullOrBlank()) return defaultColor
    val clean = hex.trim().removePrefix("#")
    if (clean.length == 8) {
        try {
            val a = clean.substring(0, 2).toInt(16)
            val b = clean.substring(2, 4).toInt(16)
            val g = clean.substring(4, 6).toInt(16)
            val r = clean.substring(6, 8).toInt(16)
            return Color(r, g, b, a)
        } catch (_: Exception) {}
    } else if (clean.length == 6) {
        try {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            return Color(r, g, b, 255)
        } catch (_: Exception) {}
    }
    return defaultColor
}

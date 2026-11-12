package com.example.data.model

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipInputStream

object KmlParser {

    /**
     * Parses either KML text or KMZ zip archive.
     */
    fun parse(inputStream: InputStream, sourceName: String): KmlDocument {
        // Read header bytes to detect KMZ (starts with PK\x03\x04 = 0x50, 0x4B, 0x03, 0x04)
        val bytes = inputStream.readBytes()
        if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()) {
            return parseKmzBytes(bytes, sourceName)
        }
        val text = String(bytes, StandardCharsets.UTF_8)
        return parseKmlText(text, sourceName)
    }

    /**
     * Parses KML string content.
     */
    fun parseKmlText(kmlString: String, sourceName: String = "Uploaded.kml"): KmlDocument {
        val parser = Xml.newPullParser()
        parser.setInput(kmlString.reader())

        var docName = sourceName.substringBeforeLast(".")
        var docDesc: String? = null
        val styles = mutableMapOf<String, KmlStyle>()
        val styleMaps = mutableMapOf<String, String>() // styleMapId -> targetStyleId
        val features = mutableListOf<KmlFeature>()

        val folderStack = mutableListOf<String>()
        var inPlacemark = false
        var currentPlacemarkName: String? = null
        var currentPlacemarkDesc: String? = null
        var currentStyleUrl: String? = null
        var currentInlineStyle: KmlStyle? = null
        val currentExtendedData = mutableMapOf<String, String>()
        val currentGeometries = mutableListOf<Pair<KmlGeometryType, List<GeoPoint>>>()

        // Style tracking
        var activeStyleId: String? = null
        var activeStyleMapId: String? = null
        var currentStyleSection: String? = null // "linestyle", "polystyle", "iconstyle"
        var styleLineColor: androidx.compose.ui.graphics.Color? = null
        var styleLineWidth: Float? = null
        var stylePolyFillColor: androidx.compose.ui.graphics.Color? = null
        var stylePolyFill: Boolean = true
        var stylePolyOutline: Boolean = true
        var styleIconColor: androidx.compose.ui.graphics.Color? = null
        var styleIconScale: Float = 1.0f

        var inCoordinates = false
        var currentCoordsText = StringBuilder()
        var currentActiveGeom: KmlGeometryType? = null

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tag = if (parser.name != null) parser.name.lowercase() else ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "folder" -> {
                            folderStack.add("Layer ${folderStack.size + 1}")
                        }
                        "stylemap" -> {
                            activeStyleMapId = parser.getAttributeValue(null, "id")
                        }
                        "style" -> {
                            activeStyleId = parser.getAttributeValue(null, "id") ?: UUID.randomUUID().toString()
                            currentStyleSection = null
                            styleLineColor = null
                            styleLineWidth = null
                            stylePolyFillColor = null
                            stylePolyFill = true
                            stylePolyOutline = true
                            styleIconColor = null
                            styleIconScale = 1.0f
                        }
                        "linestyle" -> currentStyleSection = "linestyle"
                        "polystyle" -> currentStyleSection = "polystyle"
                        "iconstyle" -> currentStyleSection = "iconstyle"
                        "placemark" -> {
                            inPlacemark = true
                            currentPlacemarkName = null
                            currentPlacemarkDesc = null
                            currentStyleUrl = null
                            currentInlineStyle = null
                            currentExtendedData.clear()
                            currentGeometries.clear()
                            currentCoordsText.clear()
                            currentActiveGeom = null
                        }
                        "point" -> currentActiveGeom = KmlGeometryType.POINT
                        "linestring", "multitrack", "track" -> currentActiveGeom = KmlGeometryType.LINE_STRING
                        "polygon", "linearring", "outerboundaryis", "innerboundaryis" -> currentActiveGeom = KmlGeometryType.POLYGON
                        "coordinates" -> {
                            inCoordinates = true
                            currentCoordsText.clear()
                        }
                        "data" -> {
                            val key = parser.getAttributeValue(null, "name")
                            if (key != null) {
                                val value = readTagText(parser, "value")
                                if (value.isNotBlank()) currentExtendedData[key] = value
                            }
                        }
                        "simpledata" -> {
                            val key = parser.getAttributeValue(null, "name")
                            val value = parser.nextText()
                            if (key != null && value.isNotBlank()) {
                                currentExtendedData[key] = value
                            }
                        }
                        "name" -> {
                            val text = parser.nextText().trim()
                            if (inPlacemark) {
                                currentPlacemarkName = text
                            } else if (folderStack.isNotEmpty()) {
                                folderStack[folderStack.size - 1] = text
                            } else {
                                docName = text
                            }
                        }
                        "description" -> {
                            val text = parser.nextText().trim()
                            if (inPlacemark) {
                                currentPlacemarkDesc = text
                            } else if (docDesc == null) {
                                docDesc = text
                            }
                        }
                        "styleurl" -> {
                            val rawUrl = parser.nextText().trim().removePrefix("#")
                            if (activeStyleMapId != null) {
                                styleMaps[activeStyleMapId!!] = rawUrl
                            } else {
                                currentStyleUrl = rawUrl
                            }
                        }
                        "color" -> {
                            val colStr = parser.nextText().trim()
                            if (activeStyleId != null) {
                                val parsed = parseKmlColor(colStr)
                                when (currentStyleSection) {
                                    "linestyle" -> styleLineColor = parsed
                                    "polystyle" -> stylePolyFillColor = parsed
                                    "iconstyle" -> styleIconColor = parsed
                                    else -> {
                                        if (styleLineColor == null) styleLineColor = parsed
                                        if (stylePolyFillColor == null) stylePolyFillColor = parsed
                                    }
                                }
                            }
                        }
                        "width" -> {
                            val w = parser.nextText().trim().toFloatOrNull() ?: 3f
                            if (currentStyleSection == "linestyle") styleLineWidth = w
                        }
                        "scale" -> {
                            val s = parser.nextText().trim().toFloatOrNull() ?: 1.0f
                            if (currentStyleSection == "iconstyle") styleIconScale = s
                        }
                        "fill" -> {
                            stylePolyFill = parser.nextText().trim() != "0"
                        }
                        "outline" -> {
                            stylePolyOutline = parser.nextText().trim() != "0"
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inCoordinates) {
                        currentCoordsText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tag) {
                        "coordinates" -> {
                            inCoordinates = false
                            if (currentCoordsText.isNotBlank()) {
                                val pts = parseCoordinatesString(currentCoordsText.toString())
                                if (pts.isNotEmpty()) {
                                    val determinedGeom = currentActiveGeom ?: when {
                                        pts.size == 1 -> KmlGeometryType.POINT
                                        pts.size == 2 -> KmlGeometryType.LINE_STRING
                                        else -> KmlGeometryType.POLYGON
                                    }
                                    currentGeometries.add(determinedGeom to pts)
                                }
                            }
                            currentCoordsText.clear()
                        }
                        "point", "linestring", "polygon", "linearring" -> {
                            currentActiveGeom = null
                        }
                        "linestyle", "polystyle", "iconstyle" -> {
                            currentStyleSection = null
                        }
                        "stylemap" -> {
                            activeStyleMapId = null
                        }
                        "style" -> {
                            if (activeStyleId != null) {
                                val s = KmlStyle(
                                    id = activeStyleId!!,
                                    lineColor = styleLineColor ?: androidx.compose.ui.graphics.Color(0xFF00E5FF),
                                    lineWidth = styleLineWidth ?: 3f,
                                    polyFillColor = (stylePolyFillColor ?: androidx.compose.ui.graphics.Color(0xFF00E5FF)).copy(alpha = 0.35f),
                                    isPolyFilled = stylePolyFill,
                                    isPolyOutlined = stylePolyOutline,
                                    iconColor = styleIconColor ?: androidx.compose.ui.graphics.Color(0xFFFF5252),
                                    iconScale = styleIconScale
                                )
                                styles[activeStyleId!!] = s
                                if (currentPlacemarkName != null) {
                                    currentInlineStyle = s
                                }
                                activeStyleId = null
                            }
                        }
                        "folder" -> {
                            if (folderStack.isNotEmpty()) {
                                folderStack.removeAt(folderStack.size - 1)
                            }
                        }
                        "placemark" -> {
                            // Resolve style from StyleMap if needed
                            val resolvedStyleUrl = currentStyleUrl?.let { styleMaps[it] ?: it }
                            val matchedStyle = (resolvedStyleUrl?.let { styles[it] }
                                ?: currentInlineStyle
                                ?: KmlStyle())

                            val assignedFolder = if (folderStack.isNotEmpty()) folderStack.joinToString(" > ") else null

                            if (currentGeometries.isNotEmpty()) {
                                currentGeometries.forEachIndexed { geomIdx, (geomType, coords) ->
                                    val baseName = currentPlacemarkName?.ifBlank { null }
                                        ?: when (geomType) {
                                            KmlGeometryType.POINT -> "WayPoint #${features.size + 1}"
                                            KmlGeometryType.LINE_STRING -> "Path #${features.size + 1}"
                                            KmlGeometryType.POLYGON -> "Plot #${features.size + 1}"
                                            else -> "Feature #${features.size + 1}"
                                        }

                                    val featName = if (currentGeometries.size > 1) "$baseName (Part ${geomIdx + 1})" else baseName

                                    val feature = KmlFeature(
                                        id = UUID.randomUUID().toString(),
                                        name = featName,
                                        description = currentPlacemarkDesc,
                                        folderName = assignedFolder,
                                        geometryType = geomType,
                                        points = coords,
                                        style = matchedStyle,
                                        extendedData = currentExtendedData.toMap(),
                                        isVisible = true
                                    )
                                    features.add(feature)
                                }
                            }
                            inPlacemark = false
                            currentPlacemarkName = null
                            currentPlacemarkDesc = null
                            currentStyleUrl = null
                            currentInlineStyle = null
                            currentGeometries.clear()
                            currentCoordsText.clear()
                            currentExtendedData.clear()
                            currentActiveGeom = null
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        // Compute Bounding Box
        val allPoints = features.flatMap { it.points }
        val bounds = if (allPoints.isNotEmpty()) {
            KmlBounds(
                minLat = allPoints.minOf { it.latitude },
                maxLat = allPoints.maxOf { it.latitude },
                minLng = allPoints.minOf { it.longitude },
                maxLng = allPoints.maxOf { it.longitude }
            )
        } else {
            KmlBounds(40.40, 40.45, 68.25, 68.40)
        }

        // Group into Folders
        val groupedFolders = features.groupBy { it.folderName ?: "Ungrouped Places" }
            .map { (fName, fList) ->
                KmlFolder(name = fName, features = fList, isVisible = true)
            }

        return KmlDocument(
            id = UUID.randomUUID().toString(),
            fileName = sourceName,
            name = docName.ifBlank { sourceName },
            description = docDesc,
            features = features,
            folders = groupedFolders,
            bounds = bounds,
            rawContent = kmlString
        )
    }

    private fun readTagText(parser: XmlPullParser, targetTag: String): String {
        var text = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name.equals(targetTag, ignoreCase = true)) {
                text = parser.nextText().trim()
            }
        }
        return text
    }

    private fun parseCoordinatesString(text: String): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()
        val tokens = text.trim().split(Regex("\\s+"))
        for (token in tokens) {
            if (token.isBlank()) continue
            val parts = token.split(",")
            if (parts.size >= 2) {
                val lng = parts[0].toDoubleOrNull()
                val lat = parts[1].toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    points.add(GeoPoint(lat, lng))
                }
            }
        }
        return points
    }

    private fun parseKmzBytes(bytes: ByteArray, sourceName: String): KmlDocument {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".kml", ignoreCase = true)) {
                    val kmlBytes = zis.readBytes()
                    val text = String(kmlBytes, StandardCharsets.UTF_8)
                    return parseKmlText(text, sourceName)
                }
                entry = zis.nextEntry
            }
        }
        throw IllegalArgumentException("No .kml entry found in KMZ archive")
    }

    /**
     * Generates a sample Google Earth KML for instant demonstration and offline testing.
     */
    fun createSampleKml(): KmlDocument {
        val sampleXml = """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
  <Document>
    <name>Sardoba Smart Irrigation Project</name>
    <description>Google Earth Project Export: Comprehensive GIS survey with Canal, Pump Stations, Parcel Boundaries and Pipeline Mainlines.</description>
    
    <Style id="fieldStyleGreen">
      <LineStyle>
        <color>ff00e676</color>
        <width>2.5</width>
      </LineStyle>
      <PolyStyle>
        <color>4000e676</color>
        <fill>1</fill>
        <outline>1</outline>
      </PolyStyle>
    </Style>

    <Style id="fieldStyleOrange">
      <LineStyle>
        <color>ffff9100</color>
        <width>2.5</width>
      </LineStyle>
      <PolyStyle>
        <color>40ff9100</color>
        <fill>1</fill>
        <outline>1</outline>
      </PolyStyle>
    </Style>

    <Style id="pipelineBlue">
      <LineStyle>
        <color>ff00b0ff</color>
        <width>4.0</width>
      </LineStyle>
    </Style>

    <Style id="canalCyan">
      <LineStyle>
        <color>ff00e5ff</color>
        <width>5.0</width>
      </LineStyle>
    </Style>

    <Folder>
      <name>Canals &amp; Water Supply</name>
      <Placemark>
        <name>South Feeder Canal Supply</name>
        <description>Primary concrete-lined irrigation feeder canal providing 14.5 m3/s water flow directly from reservoir.</description>
        <styleUrl>#canalCyan</styleUrl>
        <ExtendedData>
          <Data name="FlowRate"><value>14.5 m³/s</value></Data>
          <Data name="Lining"><value>Reinforced Concrete</value></Data>
          <Data name="BedWidth"><value>6.2 m</value></Data>
        </ExtendedData>
        <LineString>
          <tessellate>1</tessellate>
          <coordinates>
            68.2700,40.4350,280.0
            68.2900,40.4310,279.5
            68.3200,40.4260,278.8
            68.3500,40.4210,278.2
            68.3800,40.4160,277.5
          </coordinates>
        </LineString>
      </Placemark>
    </Folder>

    <Folder>
      <name>Surveyed Agricultural Plots</name>
      <Placemark>
        <name>Plot Alpha - High Yield Cotton</name>
        <description>High efficiency drip irrigated parcel. Precision monitored with capacitive soil moisture sensors.</description>
        <styleUrl>#fieldStyleGreen</styleUrl>
        <ExtendedData>
          <Data name="CropType"><value>Cotton (Sultan)</value></Data>
          <Data name="DripTapeSpacing"><value>10.8 m machine</value></Data>
          <Data name="Zone"><value>Zone 2</value></Data>
        </ExtendedData>
        <Polygon>
          <outerBoundaryIs>
            <LinearRing>
              <coordinates>
                68.2800,40.4320,0
                68.2950,40.4300,0
                68.2920,40.4230,0
                68.2770,40.4250,0
                68.2800,40.4320,0
              </coordinates>
            </LinearRing>
          </outerBoundaryIs>
        </Polygon>
      </Placemark>

      <Placemark>
        <name>Plot Beta - Wheat Rotation</name>
        <description>Automated submain controlled field section with pulse irrigation cycle.</description>
        <styleUrl>#fieldStyleOrange</styleUrl>
        <ExtendedData>
          <Data name="CropType"><value>Winter Wheat</value></Data>
          <Data name="SoilMoisture"><value>28.4%</value></Data>
          <Data name="Zone"><value>Zone 3</value></Data>
        </ExtendedData>
        <Polygon>
          <outerBoundaryIs>
            <LinearRing>
              <coordinates>
                68.3000,40.4280,0
                68.3180,40.4250,0
                68.3150,40.4180,0
                68.2970,40.4210,0
                68.3000,40.4280,0
              </coordinates>
            </LinearRing>
          </outerBoundaryIs>
        </Polygon>
      </Placemark>

      <Placemark>
        <name>Plot Gamma - Intensive Maize</name>
        <description>Irrigation sector connected to PH-9 pump booster station.</description>
        <styleUrl>#fieldStyleGreen</styleUrl>
        <ExtendedData>
          <Data name="CropType"><value>Silage Maize</value></Data>
          <Data name="TargetET"><value>6.2 mm/day</value></Data>
        </ExtendedData>
        <Polygon>
          <outerBoundaryIs>
            <LinearRing>
              <coordinates>
                68.3300,40.4240,0
                68.3500,40.4210,0
                68.3470,40.4140,0
                68.3270,40.4170,0
                68.3300,40.4240,0
              </coordinates>
            </LinearRing>
          </outerBoundaryIs>
        </Polygon>
      </Placemark>
    </Folder>

    <Folder>
      <name>Mainline Pipelines</name>
      <Placemark>
        <name>Main Distribution Pipe DN400</name>
        <description>HDPE PN10 heavy pressurized delivery pipeline connecting Intake to Booster Station 1.</description>
        <styleUrl>#pipelineBlue</styleUrl>
        <ExtendedData>
          <Data name="Diameter"><value>400 mm</value></Data>
          <Data name="Material"><value>HDPE 100 PN10</value></Data>
          <Data name="NominalPressure"><value>4.8 bar</value></Data>
        </ExtendedData>
        <LineString>
          <tessellate>1</tessellate>
          <coordinates>
            68.2830,40.4300,0
            68.2980,40.4270,0
            68.3220,40.4220,0
            68.3450,40.4180,0
            68.3620,40.4150,0
          </coordinates>
        </LineString>
      </Placemark>
    </Folder>

    <Folder>
      <name>Key Infrastructure &amp; Stations</name>
      <Placemark>
        <name>Central Pump Station PH-12</name>
        <description>Intake pump station equipped with 4x 110kW centrifugal variable-speed pumps, disc filtration battery, and SCADA RTU.</description>
        <ExtendedData>
          <Data name="Capacity"><value>1,800 m³/hr</value></Data>
          <Data name="Power"><value>440 kW</value></Data>
          <Data name="Status"><value>Operational</value></Data>
        </ExtendedData>
        <Point>
          <coordinates>68.283354,40.430834,282.0</coordinates>
        </Point>
      </Placemark>

      <Placemark>
        <name>Booster Station PH-14</name>
        <description>Mid-system inline booster pump station with fertigation injection system.</description>
        <ExtendedData>
          <Data name="Status"><value>Operational</value></Data>
          <Data name="Fertigation"><value>Dual Venturi</value></Data>
        </ExtendedData>
        <Point>
          <coordinates>68.297305,40.423337,280.5</coordinates>
        </Point>
      </Placemark>

      <Placemark>
        <name>East Terminal PH-9</name>
        <description>Terminal pressure regulating station and weather observation station.</description>
        <Point>
          <coordinates>68.362336,40.417640,277.8</coordinates>
        </Point>
      </Placemark>

      <Placemark>
        <name>Submain Control Valve V-101</name>
        <description>Hydraulic actuator valve with LoRaWAN telemetry and downstream pressure regulator.</description>
        <ExtendedData>
          <Data name="Diameter"><value>160 mm</value></Data>
          <Data name="Flow"><value>120 m³/h</value></Data>
        </ExtendedData>
        <Point>
          <coordinates>68.2910,40.4270,281.0</coordinates>
        </Point>
      </Placemark>

      <Placemark>
        <name>Weather &amp; Soil Station AWS-1</name>
        <description>Solar-powered automatic weather station measuring solar radiation, wind speed, air humidity, and multi-depth soil moisture.</description>
        <Point>
          <coordinates>68.3120,40.4220,279.0</coordinates>
        </Point>
      </Placemark>
    </Folder>

  </Document>
</kml>
"""
        return parseKmlText(sampleXml, "Sardoba_Project.kml")
    }
}

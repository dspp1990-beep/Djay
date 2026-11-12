package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SeedData
import com.example.data.model.KmlGeoData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Irrigation", appName)
  }

  @Test
  fun `verify all 20 systems have exact engineering specs from blueprint`() {
    assertEquals(20, KmlGeoData.SYSTEM_SPECS.size)
    val totalValves = KmlGeoData.SYSTEM_SPECS.values.sumOf { it.totalValves }
    assertEquals(719, totalValves)

    // Verify Zone 4 System 24 has 48 valves (1..48)
    val sys24 = KmlGeoData.SYSTEM_SPECS["SYS-24"]!!
    assertEquals(1, sys24.startValveNumber)
    assertEquals(48, sys24.endValveNumber)
    assertEquals(48, sys24.totalValves)

    // Verify Zone 2 System 30 has 57 valves (40..96)
    val sys30 = KmlGeoData.SYSTEM_SPECS["SYS-30"]!!
    assertEquals(40, sys30.startValveNumber)
    assertEquals(96, sys30.endValveNumber)
    assertEquals(57, sys30.totalValves)
  }

  @Test
  fun `verify valves are positioned along pipeline coordinates`() {
    val valves = SeedData.getInitialValves()
    assertEquals(719, valves.size)

    valves.forEach { valve ->
      // All valves must fall within the project geographic bounding box
      assertTrue("Latitude out of range: ${valve.latitude}", valve.latitude in 40.35..40.48)
      assertTrue("Longitude out of range: ${valve.longitude}", valve.longitude in 68.20..68.42)
      assertTrue("Tag format invalid: ${valve.tag}", valve.tag.startsWith("P"))
    }
  }

  @Test
  fun `verify kml parser extracts placemarks lines and polygons correctly`() {
    val sampleKml = """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
  <Document>
    <name>Test Document</name>
    <Placemark>
      <name>Test Station Point</name>
      <description>Sample Point Placemark</description>
      <Point>
        <coordinates>68.3000,40.4200,280.0</coordinates>
      </Point>
    </Placemark>
    <Placemark>
      <name>Mainline Pipe</name>
      <LineString>
        <coordinates>
          68.3000,40.4200,0
          68.3100,40.4250,0
          68.3200,40.4300,0
        </coordinates>
      </LineString>
    </Placemark>
    <Placemark>
      <name>Field Parcel Alpha</name>
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
  </Document>
</kml>"""

    val doc = com.example.data.model.KmlParser.parseKmlText(sampleKml, "test.kml")
    assertEquals("Test Document", doc.name)
    assertEquals(3, doc.totalFeatures)
    assertEquals(1, doc.totalPoints)
    assertEquals(1, doc.totalLines)
    assertEquals(1, doc.totalPolygons)

    // Check placemark structure
    val placemarks = doc.typedPlacemarks
    assertEquals(1, placemarks.size)
    assertEquals("Test Station Point", placemarks[0].name)
    assertEquals(40.4200, placemarks[0].coordinate.latitude, 0.0001)
    assertEquals(68.3000, placemarks[0].coordinate.longitude, 0.0001)

    // Check line structure
    val lines = doc.typedLines
    assertEquals(1, lines.size)
    assertEquals("Mainline Pipe", lines[0].name)
    assertEquals(3, lines[0].vertexCount)
    assertTrue("Line length should be positive", lines[0].lengthMeters > 500.0)

    // Check polygon structure
    val polygons = doc.typedPolygons
    assertEquals(1, polygons.size)
    assertEquals("Field Parcel Alpha", polygons[0].name)
    assertEquals(5, polygons[0].vertexCount)
    assertTrue("Polygon area should be positive", polygons[0].areaHectares > 10.0)
  }
}


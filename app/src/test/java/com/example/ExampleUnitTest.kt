package com.example

import com.example.data.local.SeedData
import com.example.data.model.GeoUtils
import com.example.data.model.KmlGeoData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPumpHousesInitialization() {
        val pumpHouses = SeedData.getInitialPumpHouses()
        assertEquals(12, pumpHouses.size)

        // Verify PH 7 to 18 IDs
        val ids = pumpHouses.map { it.id }.toSet()
        for (i in 7..18) {
            assertTrue(ids.contains("PH-$i"))
        }

        // Verify zones
        val zone4Phs = pumpHouses.filter { it.zoneName == "Zone 4" }
        assertEquals(5, zone4Phs.size) // PH 7-11

        val zone3Phs = pumpHouses.filter { it.zoneName == "Zone 3" }
        assertEquals(3, zone3Phs.size) // PH 15-17

        val zone2Phs = pumpHouses.filter { it.zoneName == "Zone 2" }
        assertEquals(4, zone2Phs.size) // PH 12-14, 18
    }

    @Test
    fun testSystemsInitialization() {
        val systems = SeedData.getInitialSystems()
        assertEquals(20, systems.size) // Systems 24 to 43

        val totalHa = systems.sumOf { it.grossAreaHa }
        // 1619.3 ha total
        assertEquals(1619.3, totalHa, 0.5)
    }

    @Test
    fun testKmlPipelinesAndPolygons() {
        assertTrue(KmlGeoData.PIPELINES.isNotEmpty())
        assertTrue(KmlGeoData.ZONE_POLYGONS.size >= 3)

        // Test Sardoba distance calculation
        // PH-15 (~40.4265, 68.2912) to PH-16 (~40.4215, 68.3005)
        val dist = GeoUtils.distanceMeters(40.426506, 68.291219, 40.421500, 68.300500)
        assertTrue(dist in 800.0..1500.0)

        val cardinal = GeoUtils.bearingToCardinal(40.426506, 68.291219, 40.421500, 68.300500)
        assertNotNull(cardinal)
    }

    @Test
    fun testP29ValveGroundTruthCoordinate() {
        // Verify P29-1 surveyed override in KmlGeoData
        val p29Coord = KmlGeoData.getValveCoordinates("SYS-29", 1)
        assertEquals(40.446028, p29Coord.latitude, 0.000001)
        assertEquals(68.285828, p29Coord.longitude, 0.000001)

        // Verify DMS formatting
        val dms = GeoUtils.toDms(p29Coord.latitude, p29Coord.longitude)
        assertTrue(dms.contains("40°26'45"))
        assertTrue(dms.contains("68°17'08"))
    }

    @Test
    fun testCoordinateParsing() {
        // Test parsing user input formatted in DMS: 40°26'45.70"N 68°17'08.98"E
        val parsedDms = GeoUtils.parseCoordinates("40°26'45.70\"N 68°17'08.98\"E")
        assertNotNull(parsedDms)
        assertEquals(40.446028, parsedDms!!.latitude, 0.0001)
        assertEquals(68.285828, parsedDms.longitude, 0.0001)

        // Test parsing decimal coordinates: "40.446028, 68.285828"
        val parsedDecimal = GeoUtils.parseCoordinates("40.446028, 68.285828")
        assertNotNull(parsedDecimal)
        assertEquals(40.446028, parsedDecimal!!.latitude, 0.00001)
        assertEquals(68.285828, parsedDecimal.longitude, 0.00001)
    }

    @Test
    fun testValveStructureModification() {
        val originalValves = SeedData.getInitialValves()
        val p29Valve = originalValves.find { it.tag == "P29-1" }
        assertNotNull(p29Valve)
        assertEquals(40.446028, p29Valve!!.latitude, 0.0001)
        assertEquals(68.285828, p29Valve.longitude, 0.0001)

        // Modify valve structure & number
        val editedValve = p29Valve.copy(
            tag = "P29-1-MODIFIED",
            submainNumber = 101,
            shiftNumber = 3,
            pipeDiameterMm = 160,
            areaHa = 14.5
        )
        assertEquals("P29-1-MODIFIED", editedValve.tag)
        assertEquals(101, editedValve.submainNumber)
        assertEquals(3, editedValve.shiftNumber)
        assertEquals(160, editedValve.pipeDiameterMm)
        assertEquals(14.5, editedValve.areaHa, 0.01)
    }
}


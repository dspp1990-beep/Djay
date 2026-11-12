package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.data.model.GeoPoint
import com.example.data.model.GeoUtils
import com.example.data.model.KmlGeoData
import com.example.data.model.PumpHouseEntity
import com.example.data.model.SubmainValveEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveUserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 5.0f,
    val altitudeMeters: Double = 280.0,
    val bearingDegrees: Float = 0.0f,
    val speedMps: Float = 0.0f,
    val isGpsActive: Boolean = false,
    val isSimulated: Boolean = false,
    val nearestValveTag: String? = null,
    val distanceToValveMeters: Double? = null,
    val valveDirection: String? = null,
    val nearestPumpHouseId: String? = null,
    val distanceToPumpHouseMeters: Double? = null,
    val pumpHouseDirection: String? = null
)

class LocationTracker(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    // Start in simulation locked mode to keep user positioned in Sardoba irrigation project
    var isSimulationLocked: Boolean = true
        private set

    // Default to Sardoba project field near PH-15 / System 32
    private val _userLocation = MutableStateFlow(
        LiveUserLocation(
            latitude = 40.426506,
            longitude = 68.291219,
            isSimulated = true
        )
    )
    val userLocation: StateFlow<LiveUserLocation> = _userLocation.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateRealLocation(location)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(hasFineLocation: Boolean, hasCoarseLocation: Boolean) {
        if (!hasFineLocation && !hasCoarseLocation) return
        val lm = locationManager ?: return

        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L, // every 2 seconds
                    2.0f,  // or 2 meters
                    locationListener,
                    Looper.getMainLooper()
                )
                val lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnown != null && !isSimulationLocked) updateRealLocation(lastKnown)
            } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    5.0f,
                    locationListener,
                    Looper.getMainLooper()
                )
                val lastKnown = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null && !isSimulationLocked) updateRealLocation(lastKnown)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enableRealGps(hasFineLocation: Boolean, hasCoarseLocation: Boolean) {
        isSimulationLocked = false
        startLocationUpdates(hasFineLocation, hasCoarseLocation)
    }

    fun stopLocationUpdates() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateRealLocation(location: Location) {
        if (isSimulationLocked) return
        val current = _userLocation.value
        _userLocation.value = current.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            altitudeMeters = location.altitude,
            bearingDegrees = location.bearing,
            speedMps = location.speed,
            isGpsActive = true,
            isSimulated = false
        )
    }

    /**
     * Teleport / Simulate user location to an exact valve or pump house.
     * Essential for field technician inspection testing and browser streaming emulators.
     */
    fun simulateLocation(latitude: Double, longitude: Double) {
        isSimulationLocked = true
        val current = _userLocation.value
        _userLocation.value = current.copy(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = 3.5f,
            isGpsActive = true,
            isSimulated = true
        )
    }

    /**
     * Recalculate proximity to nearest valves and pump houses.
     */
    fun recalculateProximities(valves: List<SubmainValveEntity>, pumpHouses: List<PumpHouseEntity>) {
        if (valves.isEmpty() || pumpHouses.isEmpty()) return
        val current = _userLocation.value
        val userLat = current.latitude
        val userLng = current.longitude

        var closestValve: SubmainValveEntity? = null
        var minValveDist = Double.MAX_VALUE

        valves.forEach { valve ->
            val d = GeoUtils.distanceMeters(userLat, userLng, valve.latitude, valve.longitude)
            if (d < minValveDist) {
                minValveDist = d
                closestValve = valve
            }
        }

        var closestPH: PumpHouseEntity? = null
        var minPHDist = Double.MAX_VALUE

        pumpHouses.forEach { ph ->
            val d = GeoUtils.distanceMeters(userLat, userLng, ph.latitude, ph.longitude)
            if (d < minPHDist) {
                minPHDist = d
                closestPH = ph
            }
        }

        val valveTag = closestValve?.tag
        val phId = closestPH?.id
        val newValveDist = if (closestValve != null) Math.round(minValveDist * 10.0) / 10.0 else null
        val newPhDist = if (closestPH != null) Math.round(minPHDist * 10.0) / 10.0 else null

        // Avoid re-emitting identical proximity state
        if (current.nearestValveTag == valveTag &&
            current.nearestPumpHouseId == phId &&
            current.distanceToValveMeters == newValveDist &&
            current.distanceToPumpHouseMeters == newPhDist
        ) {
            return
        }

        val valveDir = closestValve?.let {
            GeoUtils.bearingToCardinal(userLat, userLng, it.latitude, it.longitude)
        }
        val phDir = closestPH?.let {
            GeoUtils.bearingToCardinal(userLat, userLng, it.latitude, it.longitude)
        }

        _userLocation.value = current.copy(
            nearestValveTag = valveTag,
            distanceToValveMeters = newValveDist,
            valveDirection = valveDir,
            nearestPumpHouseId = phId,
            distanceToPumpHouseMeters = newPhDist,
            pumpHouseDirection = phDir
        )
    }
}

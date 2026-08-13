package com.example.londonbuses.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
data class DeviceLocation(val latitude: Double, val longitude: Double)

object LocationHelper {

    // Default location: Central London (Charing Cross / Trafalgar Square)
    val defaultLocation = DeviceLocation(51.5074, -0.1278)

    private val _currentLocation = MutableStateFlow(defaultLocation)
    val currentLocation: StateFlow<DeviceLocation> = _currentLocation

    fun setSimulatedLocation(latitude: Double, longitude: Double) {
        _currentLocation.value = DeviceLocation(latitude, longitude)
    }

    @SuppressLint("MissingPermission")
    fun fetchRealLocation(context: Context) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    _currentLocation.value = DeviceLocation(location.latitude, location.longitude)
                } else {
                    // Fallback to legacy LocationManager
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val finalLoc = gpsLocation ?: networkLocation
                    if (finalLoc != null) {
                        _currentLocation.value = DeviceLocation(finalLoc.latitude, finalLoc.longitude)
                    }
                }
            }.addOnFailureListener {
                // If it fails, keep default Central London location
            }
        } catch (e: Exception) {
            // Keep default
        }
    }

    /**
     * Calculates the distance in meters between two coordinates using the Haversine formula.
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return r * c
    }
}

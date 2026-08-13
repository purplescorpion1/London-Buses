package com.example.londonbuses

import com.example.londonbuses.utils.LocationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationHelperTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Trafalgar Square, London
        val lat1 = 51.5080
        val lon1 = -0.1281

        // Piccadilly Circus, London
        val lat2 = 51.5101
        val lon2 = -0.1349

        // Calculate distance using our formula
        val distance = LocationHelper.calculateDistanceMeters(lat1, lon1, lat2, lon2)

        // Expected distance is ~525 meters
        assertTrue("Distance should be positive", distance > 0.0)
        assertEquals(524.8, distance, 5.0) // Tolerance of 5 meters is standard for geographic checks
    }

    @Test
    fun testSameCoordinatesHaveZeroDistance() {
        val lat = 51.5074
        val lon = -0.1278
        val distance = LocationHelper.calculateDistanceMeters(lat, lon, lat, lon)
        assertEquals(0.0, distance, 0.001)
    }
}

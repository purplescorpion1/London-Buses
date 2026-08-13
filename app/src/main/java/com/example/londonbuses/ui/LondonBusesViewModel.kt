package com.example.londonbuses.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.londonbuses.data.CredentialManager
import com.example.londonbuses.data.api.TflApiClient
import com.example.londonbuses.data.api.TflApiService
import com.example.londonbuses.data.models.ArrivalPrediction
import com.example.londonbuses.data.models.LineRouteSequence
import com.example.londonbuses.data.models.StopPoint
import com.example.londonbuses.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LondonBusesViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: TflApiService = TflApiClient.createService(application)
    private val credentialManager = CredentialManager(application)

    // Api Key State
    private val _apiKey = MutableStateFlow(credentialManager.getApiKey())
    val apiKey: StateFlow<String> = _apiKey

    // Location State
    val deviceLocation = LocationHelper.currentLocation

    // Route Search Screen states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError

    private val _lineRouteSequence = MutableStateFlow<LineRouteSequence?>(null)
    val lineRouteSequence: StateFlow<LineRouteSequence?> = _lineRouteSequence

    // Arrivals mapped to stop ids for the searched route
    private val _lineArrivals = MutableStateFlow<Map<String, List<ArrivalPrediction>>>(emptyMap())
    val lineArrivals: StateFlow<Map<String, List<ArrivalPrediction>>> = _lineArrivals

    // Nearby Screen states
    private val _isNearbyLoading = MutableStateFlow(false)
    val isNearbyLoading: StateFlow<Boolean> = _isNearbyLoading

    private val _nearbyError = MutableStateFlow<String?>(null)
    val nearbyError: StateFlow<String?> = _nearbyError

    private val _nearbyStops = MutableStateFlow<List<StopPoint>>(emptyList())
    val nearbyStops: StateFlow<List<StopPoint>> = _nearbyStops

    // Selected Stop (Overlay / BottomSheet) Predictions
    private val _selectedStop = MutableStateFlow<StopPoint?>(null)
    val selectedStop: StateFlow<StopPoint?> = _selectedStop

    private val _selectedStopPredictions = MutableStateFlow<List<ArrivalPrediction>>(emptyList())
    val selectedStopPredictions: StateFlow<List<ArrivalPrediction>> = _selectedStopPredictions

    private val _isSelectedStopLoading = MutableStateFlow(false)
    val isSelectedStopLoading: StateFlow<Boolean> = _isSelectedStopLoading

    init {
        // Fetch location on startup
        fetchLocation()
    }

    fun saveApiKey(key: String) {
        credentialManager.saveApiKey(key)
        _apiKey.value = key
    }

    fun fetchLocation() {
        LocationHelper.fetchRealLocation(getApplication())
    }

    fun setSimulatedLocation(latitude: Double, longitude: Double) {
        LocationHelper.setSimulatedLocation(latitude, longitude)
        // Refresh nearby stops if we update the location
        fetchNearbyStops()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchBusRoute(lineId: String) {
        val trimmed = lineId.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _isSearchLoading.value = true
            _searchError.value = null
            _lineRouteSequence.value = null
            _lineArrivals.value = emptyMap()

            try {
                // Fetch route sequence (try lowercase lineId)
                val cleanLineId = trimmed.lowercase()
                val sequence = apiService.getRouteSequence(cleanLineId)
                _lineRouteSequence.value = sequence

                // Fetch live predictions for the entire line to map times along the stops
                try {
                    val arrivalsList = apiService.getLineArrivals(cleanLineId)
                    // Group by naptanId (stop point id)
                    _lineArrivals.value = arrivalsList.groupBy { it.naptanId }
                } catch (e: Exception) {
                    // Mapped predictions could fail or be empty, still show sequence stops
                    _lineArrivals.value = emptyMap()
                }

            } catch (e: Exception) {
                _searchError.value = "Bus route '$trimmed' not found or API request failed. Please check your API key in settings."
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    fun fetchNearbyStops() {
        viewModelScope.launch {
            _isNearbyLoading.value = true
            _nearbyError.value = null
            _nearbyStops.value = emptyList()

            try {
                val loc = deviceLocation.value
                val response = apiService.getStopPointsByGeo(lat = loc.latitude, lon = loc.longitude)
                _nearbyStops.value = response.stopPoints
            } catch (e: Exception) {
                _nearbyError.value = "Failed to load nearby stops. Please check your network connection and API key."
            } finally {
                _isNearbyLoading.value = false
            }
        }
    }

    fun selectStop(stop: StopPoint?) {
        _selectedStop.value = stop
        _selectedStopPredictions.value = emptyList()
        if (stop == null) return

        viewModelScope.launch {
            _isSelectedStopLoading.value = true
            try {
                val predictions = apiService.getStopPointArrivals(stop.id)
                // Sort predictions by arrival time
                _selectedStopPredictions.value = predictions.sortedBy { it.timeToStation ?: Int.MAX_VALUE }
            } catch (e: Exception) {
                _selectedStopPredictions.value = emptyList()
            } finally {
                _isSelectedStopLoading.value = false
            }
        }
    }
}

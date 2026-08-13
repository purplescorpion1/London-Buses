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
import com.example.londonbuses.data.models.LineStatusResponse
import com.example.londonbuses.data.models.StopDisruption
import com.example.londonbuses.data.models.TimetableResponse
import com.example.londonbuses.data.models.JourneyResponse
import com.example.londonbuses.data.models.MatchedStop
import com.example.londonbuses.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LondonBusesViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: TflApiService = TflApiClient.createService(application)
    private val credentialManager = CredentialManager(application)
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

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

    // Line Status
    private val _lineStatus = MutableStateFlow<LineStatusResponse?>(null)
    val lineStatus: StateFlow<LineStatusResponse?> = _lineStatus

    private val _isLineStatusLoading = MutableStateFlow(false)
    val isLineStatusLoading: StateFlow<Boolean> = _isLineStatusLoading

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

    // Stop Disruptions
    private val _stopDisruptions = MutableStateFlow<List<StopDisruption>>(emptyList())
    val stopDisruptions: StateFlow<List<StopDisruption>> = _stopDisruptions

    private val _isStopDisruptionsLoading = MutableStateFlow(false)
    val isStopDisruptionsLoading: StateFlow<Boolean> = _isStopDisruptionsLoading

    // Timetable Fallback
    private val _selectedStopTimetable = MutableStateFlow<TimetableResponse?>(null)
    val selectedStopTimetable: StateFlow<TimetableResponse?> = _selectedStopTimetable

    private val _isSelectedStopTimetableLoading = MutableStateFlow(false)
    val isSelectedStopTimetableLoading: StateFlow<Boolean> = _isSelectedStopTimetableLoading

    // Journey Planner
    private val _journeyFromQuery = MutableStateFlow("")
    val journeyFromQuery: StateFlow<String> = _journeyFromQuery

    private val _journeyToQuery = MutableStateFlow("")
    val journeyToQuery: StateFlow<String> = _journeyToQuery

    private val _journeyResults = MutableStateFlow<JourneyResponse?>(null)
    val journeyResults: StateFlow<JourneyResponse?> = _journeyResults

    private val _isJourneyLoading = MutableStateFlow(false)
    val isJourneyLoading: StateFlow<Boolean> = _isJourneyLoading

    private val _journeyError = MutableStateFlow<String?>(null)
    val journeyError: StateFlow<String?> = _journeyError

    // Autocomplete Suggestions State
    private val _fromSuggestions = MutableStateFlow<List<MatchedStop>>(emptyList())
    val fromSuggestions: StateFlow<List<MatchedStop>> = _fromSuggestions

    private val _toSuggestions = MutableStateFlow<List<MatchedStop>>(emptyList())
    val toSuggestions: StateFlow<List<MatchedStop>> = _toSuggestions

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

    fun updateJourneyFromQuery(query: String) {
        _journeyFromQuery.value = query
        val trimmed = query.trim()
        if (trimmed.length >= 3 && trimmed.lowercase() != "current location" && trimmed.lowercase() != "my current location") {
            viewModelScope.launch {
                try {
                    val response = apiService.searchStopPoints(trimmed)
                    _fromSuggestions.value = response.matches
                } catch (e: Exception) {
                    _fromSuggestions.value = emptyList()
                }
            }
        } else {
            _fromSuggestions.value = emptyList()
        }
    }

    fun updateJourneyToQuery(query: String) {
        _journeyToQuery.value = query
        val trimmed = query.trim()
        if (trimmed.length >= 3 && trimmed.lowercase() != "current location" && trimmed.lowercase() != "my current location") {
            viewModelScope.launch {
                try {
                    val response = apiService.searchStopPoints(trimmed)
                    _toSuggestions.value = response.matches
                } catch (e: Exception) {
                    _toSuggestions.value = emptyList()
                }
            }
        } else {
            _toSuggestions.value = emptyList()
        }
    }

    fun clearFromSuggestions() {
        _fromSuggestions.value = emptyList()
    }

    fun clearToSuggestions() {
        _toSuggestions.value = emptyList()
    }

    fun searchJourney() {
        var from = _journeyFromQuery.value.trim()
        var to = _journeyToQuery.value.trim()
        if (from.isEmpty() || to.isEmpty()) return

        if (from.lowercase() == "current location" || from.lowercase() == "my current location") {
            val loc = deviceLocation.value
            from = "${loc.latitude},${loc.longitude}"
        }

        if (to.lowercase() == "current location" || to.lowercase() == "my current location") {
            val loc = deviceLocation.value
            to = "${loc.latitude},${loc.longitude}"
        }

        viewModelScope.launch {
            _isJourneyLoading.value = true
            _journeyError.value = null
            _journeyResults.value = null

            try {
                val results = apiService.getJourneyResults(from = from, to = to)
                _journeyResults.value = results
                if (results.journeys.isEmpty()) {
                    // Check if there is disambiguation in a 200 OK response (though TfL usually returns 300)
                    if (results.fromLocationDisambiguation?.disambiguationOptions?.isNotEmpty() == true ||
                        results.toLocationDisambiguation?.disambiguationOptions?.isNotEmpty() == true) {
                        _journeyResults.value = results
                    } else {
                        _journeyError.value = "No journeys found between '${_journeyFromQuery.value.trim()}' and '${_journeyToQuery.value.trim()}'."
                    }
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    try {
                        val parsed = json.decodeFromString<JourneyResponse>(errorBody)
                        if (parsed.fromLocationDisambiguation?.disambiguationOptions?.isNotEmpty() == true ||
                            parsed.toLocationDisambiguation?.disambiguationOptions?.isNotEmpty() == true) {
                            _journeyResults.value = parsed
                        } else {
                            _journeyError.value = "Failed to plan journey. Please check your locations and try again."
                        }
                    } catch (parseEx: Exception) {
                        _journeyError.value = "Failed to plan journey. Please check your locations and try again."
                    }
                } else {
                    _journeyError.value = "Failed to plan journey. Please check your locations and try again."
                }
            } catch (e: Exception) {
                _journeyError.value = "Failed to plan journey. Please check your locations and try again."
            } finally {
                _isJourneyLoading.value = false
            }
        }
    }

    fun fetchLineStatus(lineId: String) {
        viewModelScope.launch {
            _isLineStatusLoading.value = true
            _lineStatus.value = null
            try {
                val response = apiService.getLineStatus(lineId)
                _lineStatus.value = response.firstOrNull()
            } catch (e: Exception) {
                _lineStatus.value = null
            } finally {
                _isLineStatusLoading.value = false
            }
        }
    }

    fun fetchStopDisruptions(stopId: String) {
        viewModelScope.launch {
            _isStopDisruptionsLoading.value = true
            _stopDisruptions.value = emptyList()
            try {
                val response = apiService.getStopDisruptions(stopId)
                _stopDisruptions.value = response
            } catch (e: Exception) {
                _stopDisruptions.value = emptyList()
            } finally {
                _isStopDisruptionsLoading.value = false
            }
        }
    }

    fun fetchStopTimetable(lineId: String, stopId: String) {
        viewModelScope.launch {
            _isSelectedStopTimetableLoading.value = true
            _selectedStopTimetable.value = null
            try {
                val response = apiService.getTimetable(lineId, stopId)
                _selectedStopTimetable.value = response
            } catch (e: Exception) {
                _selectedStopTimetable.value = null
            } finally {
                _isSelectedStopTimetableLoading.value = false
            }
        }
    }

    fun searchBusRoute(lineId: String) {
        val trimmed = lineId.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _isSearchLoading.value = true
            _searchError.value = null
            _lineRouteSequence.value = null
            _lineArrivals.value = emptyMap()
            _lineStatus.value = null

            try {
                // Fetch route sequence (try lowercase lineId)
                val cleanLineId = trimmed.lowercase()
                val sequence = apiService.getRouteSequence(cleanLineId)
                _lineRouteSequence.value = sequence

                // Fetch line status
                fetchLineStatus(cleanLineId)

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
        _stopDisruptions.value = emptyList()
        _selectedStopTimetable.value = null
        if (stop == null) return

        // Fetch stop disruptions
        fetchStopDisruptions(stop.id)

        // Fetch timetable fallback if we have a searched line context
        lineRouteSequence.value?.lineId?.let { lineId ->
            fetchStopTimetable(lineId.lowercase(), stop.id)
        }

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

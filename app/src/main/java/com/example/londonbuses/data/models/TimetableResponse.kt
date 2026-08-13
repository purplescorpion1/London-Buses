package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class KnownJourney(
    val hour: String? = null,
    val minute: String? = null
) {
    val displayTime: String
        get() {
            val h = hour?.padStart(2, '0') ?: "00"
            val m = minute?.padStart(2, '0') ?: "00"
            return "$h:$m"
        }
}

@Serializable
data class TimetableSchedule(
    val name: String? = null,
    val knownJourneys: List<KnownJourney> = emptyList()
)

@Serializable
data class TimetableRoute(
    val schedules: List<TimetableSchedule> = emptyList()
)

@Serializable
data class TimetableData(
    val departureStopId: String? = null,
    val routes: List<TimetableRoute> = emptyList()
)

@Serializable
data class TimetableResponse(
    val lineId: String? = null,
    val lineName: String? = null,
    val direction: String? = null,
    val timetable: TimetableData? = null
)

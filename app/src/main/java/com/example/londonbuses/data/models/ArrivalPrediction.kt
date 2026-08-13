package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ArrivalPrediction(
    val id: String,
    val naptanId: String,
    val stationName: String? = null,
    val lineId: String,
    val lineName: String,
    val platformName: String? = null,
    val direction: String? = null,
    val destinationNaptanId: String? = null,
    val destinationName: String? = null,
    val expectedArrival: String? = null,
    val timeToStation: Int? = null,
    val towards: String? = null
) {
    val minutesToArrival: Int
        get() = if (timeToStation != null) {
            (timeToStation / 60)
        } else {
            0
        }

    val displayArrival: String
        get() = when {
            minutesToArrival <= 0 -> "Due"
            minutesToArrival == 1 -> "1 min"
            else -> "$minutesToArrival mins"
        }
}

package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StopPoint(
    val id: String,
    val name: String? = null,
    val commonName: String? = null,
    val lat: Double,
    val lon: Double,
    val towards: String? = null,
    val stopLetter: String? = null,
    val direction: String? = null,
    val distance: Double? = null
) {
    val displayName: String
        get() = commonName ?: name ?: "Unknown Stop"
}

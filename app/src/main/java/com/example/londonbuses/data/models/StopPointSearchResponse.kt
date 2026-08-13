package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class MatchedStop(
    val id: String? = null,
    val name: String? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

@Serializable
data class StopPointSearchResponse(
    val query: String? = null,
    val total: Int? = null,
    val matches: List<MatchedStop> = emptyList()
)

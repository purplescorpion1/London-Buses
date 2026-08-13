package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StopPointsResponse(
    val stopPoints: List<StopPoint> = emptyList(),
    val pageSize: Int? = null,
    val total: Int? = null,
    val page: Int? = null
)

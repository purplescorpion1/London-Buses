package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LineRouteSequence(
    val lineId: String,
    val lineName: String,
    val direction: String? = null,
    val stations: List<StopPoint> = emptyList(),
    val stopPointSequences: List<StopPointSequence> = emptyList()
)

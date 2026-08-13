package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StopPointSequence(
    val lineId: String,
    val lineName: String,
    val direction: String,
    val branchId: Int? = null,
    val stopPoint: List<StopPoint> = emptyList()
)

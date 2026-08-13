package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LineStatus(
    val id: Int? = null,
    val statusSeverity: Int? = null,
    val statusSeverityDescription: String? = null,
    val reason: String? = null
)

@Serializable
data class LineDisruption(
    val category: String? = null,
    val type: String? = null,
    val categoryDescription: String? = null,
    val description: String? = null,
    val summary: String? = null,
    val additionalInfo: String? = null
)

@Serializable
data class LineStatusResponse(
    val id: String? = null,
    val name: String? = null,
    val modeName: String? = null,
    val lineStatuses: List<LineStatus> = emptyList(),
    val disruptions: List<LineDisruption> = emptyList()
)

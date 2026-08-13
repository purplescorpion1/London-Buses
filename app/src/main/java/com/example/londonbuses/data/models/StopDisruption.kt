package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class StopDisruption(
    val atcoCode: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val description: String? = null,
    val commonName: String? = null,
    val type: String? = null,
    val mode: String? = null,
    val additionalInformation: String? = null
)

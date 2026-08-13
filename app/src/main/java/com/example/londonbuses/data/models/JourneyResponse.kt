package com.example.londonbuses.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LegMode(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class LegPoint(
    val commonName: String? = null
)

@Serializable
data class LegInstruction(
    val summary: String? = null,
    val detailed: String? = null
)

@Serializable
data class JourneyLeg(
    val duration: Int? = null,
    val instruction: LegInstruction? = null,
    val departurePoint: LegPoint? = null,
    val arrivalPoint: LegPoint? = null,
    val mode: LegMode? = null
)

@Serializable
data class Journey(
    val startDateTime: String? = null,
    val duration: Int? = null,
    val arrivalDateTime: String? = null,
    val legs: List<JourneyLeg> = emptyList()
)

@Serializable
data class DisambiguationOption(
    val parameterValue: String? = null,
    val place: LegPoint? = null,
    val matchQuality: Int? = null
)

@Serializable
data class Disambiguation(
    val matchStatus: String? = null,
    val disambiguationOptions: List<DisambiguationOption> = emptyList()
)

@Serializable
data class JourneyResponse(
    val journeys: List<Journey> = emptyList(),
    val fromLocationDisambiguation: Disambiguation? = null,
    val toLocationDisambiguation: Disambiguation? = null
)

package com.example.londonbuses.data.api

import com.example.londonbuses.data.models.ArrivalPrediction
import com.example.londonbuses.data.models.LineRouteSequence
import com.example.londonbuses.data.models.StopPointsResponse
import com.example.londonbuses.data.models.LineStatusResponse
import com.example.londonbuses.data.models.StopDisruption
import com.example.londonbuses.data.models.TimetableResponse
import com.example.londonbuses.data.models.JourneyResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TflApiService {

    @GET("Line/{ids}/Status")
    suspend fun getLineStatus(
        @Path("ids") lineIds: String,
        @Query("detail") detail: Boolean = true
    ): List<LineStatusResponse>

    @GET("StopPoint/{ids}/Disruption")
    suspend fun getStopDisruptions(
        @Path("ids") stopPointIds: String
    ): List<StopDisruption>

    @GET("Line/{id}/Timetable/{fromStopPointId}")
    suspend fun getTimetable(
        @Path("id") lineId: String,
        @Path("fromStopPointId") fromStopPointId: String
    ): TimetableResponse

    @GET("Journey/JourneyResults/{from}/to/{to}")
    suspend fun getJourneyResults(
        @Path("from") from: String,
        @Path("to") to: String,
        @Query("mode") mode: String = "bus"
    ): JourneyResponse

    @GET("Line/{id}/Route/Sequence/{direction}")
    suspend fun getRouteSequence(
        @Path("id") lineId: String,
        @Path("direction") direction: String = "all",
        @Query("serviceTypes") serviceTypes: String = "Regular,Night"
    ): LineRouteSequence

    @GET("Line/{ids}/Arrivals")
    suspend fun getLineArrivals(
        @Path("ids") lineIds: String
    ): List<ArrivalPrediction>

    @GET("StopPoint")
    suspend fun getStopPointsByGeo(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("stopTypes") stopTypes: String = "NaptanPublicBusCoachTram",
        @Query("radius") radius: Int = 1000,
        @Query("returnLines") returnLines: Boolean = true
    ): StopPointsResponse

    @GET("StopPoint/{stopPointId}/Arrivals")
    suspend fun getStopPointArrivals(
        @Path("stopPointId") stopPointId: String
    ): List<ArrivalPrediction>
}

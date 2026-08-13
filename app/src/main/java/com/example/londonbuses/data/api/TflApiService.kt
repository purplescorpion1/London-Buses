package com.example.londonbuses.data.api

import com.example.londonbuses.data.models.ArrivalPrediction
import com.example.londonbuses.data.models.LineRouteSequence
import com.example.londonbuses.data.models.StopPointsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TflApiService {

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

package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApiService {
    @POST("ai/trip-plan")
    suspend fun generateTripPlan(@Body request: TripPlanRequest): Response<TripPlanResponse>
}

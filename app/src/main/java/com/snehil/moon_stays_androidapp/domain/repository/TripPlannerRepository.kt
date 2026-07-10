package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanResponse
import kotlinx.coroutines.flow.Flow

interface TripPlannerRepository {
    fun generateTripPlan(request: TripPlanRequest): Flow<NetworkResult<TripPlanResponse>>
}

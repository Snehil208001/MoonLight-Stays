package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.AiApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanResponse
import com.snehil.moon_stays_androidapp.domain.repository.TripPlannerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripPlannerRepositoryImpl @Inject constructor(
    private val aiApiService: AiApiService
) : TripPlannerRepository {

    override fun generateTripPlan(request: TripPlanRequest): Flow<NetworkResult<TripPlanResponse>> {
        return safeApiCall { aiApiService.generateTripPlan(request) }
    }
}

package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanResponse
import com.snehil.moon_stays_androidapp.domain.repository.TripPlannerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateTripPlanUseCase @Inject constructor(
    private val tripPlannerRepository: TripPlannerRepository
) {
    operator fun invoke(request: TripPlanRequest): Flow<NetworkResult<TripPlanResponse>> {
        return tripPlannerRepository.generateTripPlan(request)
    }
}

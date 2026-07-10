package com.snehil.moon_stays_androidapp.mainui.tripplanner.viewmodel

import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.TripPlanResponse
import com.snehil.moon_stays_androidapp.domain.usecase.GenerateTripPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val generateTripPlanUseCase: GenerateTripPlanUseCase
) : BaseViewModel() {

    private val _tripPlan = MutableStateFlow<TripPlanResponse?>(null)
    val tripPlan: StateFlow<TripPlanResponse?> = _tripPlan.asStateFlow()

    fun generateTripPlan(
        city: String,
        checkInDate: String?,
        checkOutDate: String?,
        numberOfGuests: Int,
        interests: List<String>,
        budgetLevel: String,
        hotelId: Long? = null
    ) {
        _errorMessage.value = null
        _tripPlan.value = null
        val request = TripPlanRequest(
            city = city.trim(),
            checkInDate = checkInDate?.ifBlank { null },
            checkOutDate = checkOutDate?.ifBlank { null },
            numberOfGuests = numberOfGuests,
            interests = interests.ifEmpty { null },
            budgetLevel = budgetLevel.ifBlank { null },
            hotelId = hotelId
        )
        launchSafe {
            generateTripPlanUseCase(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _tripPlan.value = result.data
                    }
                }
            }
        }
    }

    fun reset() {
        _tripPlan.value = null
        _errorMessage.value = null
    }
}

package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchHotelsUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(request: HotelSearchRequest): Flow<NetworkResult<List<HotelPriceDto>>> {
        return hotelRepository.searchHotels(request)
    }
}

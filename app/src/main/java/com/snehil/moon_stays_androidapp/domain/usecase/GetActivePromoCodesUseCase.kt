package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoCodeDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivePromoCodesUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<PromoCodeDto>>> {
        return hotelRepository.getActivePromoCodes()
    }
}

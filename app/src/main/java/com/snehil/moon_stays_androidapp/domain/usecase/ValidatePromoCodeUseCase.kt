package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoValidationDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ValidatePromoCodeUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(code: String): Flow<NetworkResult<PromoValidationDto>> {
        return hotelRepository.validatePromoCode(code.trim())
    }
}

package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelInfoDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHotelInfoUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotelId: Long): Flow<NetworkResult<HotelInfoDto>> {
        return hotelRepository.getHotelInfo(hotelId)
    }
}

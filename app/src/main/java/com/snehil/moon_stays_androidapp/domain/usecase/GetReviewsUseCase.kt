package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.PageDto
import com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotelId: Long, page: Int = 0, size: Int = 20): Flow<NetworkResult<PageDto<ReviewDto>>> {
        return hotelRepository.getHotelReviews(hotelId, page, size)
    }
}

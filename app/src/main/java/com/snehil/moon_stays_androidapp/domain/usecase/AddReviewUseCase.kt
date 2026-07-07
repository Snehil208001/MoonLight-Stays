package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotelId: Long, rating: Int, content: String): Flow<NetworkResult<ReviewDto>> {
        val request = ReviewDto(
            id = null,
            rating = rating,
            content = content,
            hotelId = hotelId,
            userId = null
        )
        return hotelRepository.addReview(hotelId, request)
    }
}

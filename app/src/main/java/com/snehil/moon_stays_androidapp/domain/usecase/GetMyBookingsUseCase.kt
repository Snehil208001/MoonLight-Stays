package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PageDto
import com.snehil.moon_stays_androidapp.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(page: Int = 0, size: Int = 20, status: List<String>? = null): Flow<NetworkResult<PageDto<BookingDto>>> {
        return bookingRepository.getMyBookings(page, size, status)
    }
}

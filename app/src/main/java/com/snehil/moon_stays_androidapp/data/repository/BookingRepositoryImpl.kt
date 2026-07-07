package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.BookingApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingDto
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PageDto
import com.snehil.moon_stays_androidapp.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingApiService: BookingApiService
) : BookingRepository {

    override fun initialiseBooking(request: BookingRequest): Flow<NetworkResult<BookingDto>> {
        return safeApiCall { bookingApiService.initialiseBooking(request) }
    }

    override fun addGuests(bookingId: Long, guests: List<GuestDto>): Flow<NetworkResult<BookingDto>> {
        return safeApiCall { bookingApiService.addGuests(bookingId, guests) }
    }

    override fun getBookingById(bookingId: Long): Flow<NetworkResult<BookingDto>> {
        return safeApiCall { bookingApiService.getBookingById(bookingId) }
    }

    override fun getMyBookings(page: Int, size: Int, status: List<String>?): Flow<NetworkResult<PageDto<BookingDto>>> {
        return safeApiCall { bookingApiService.getMyBookings(page, size, status) }
    }

    override fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>> {
        return safeApiCall { bookingApiService.cancelBooking(bookingId) }
    }

    override fun initiatePayment(bookingId: Long): Flow<NetworkResult<Map<String, String>>> {
        return safeApiCall { bookingApiService.initiatePayment(bookingId) }
    }
}

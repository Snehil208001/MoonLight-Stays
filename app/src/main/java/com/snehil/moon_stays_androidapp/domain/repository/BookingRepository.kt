package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingDto
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PageDto
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun initialiseBooking(request: BookingRequest): Flow<NetworkResult<BookingDto>>
    fun addGuests(bookingId: Long, guests: List<GuestDto>): Flow<NetworkResult<BookingDto>>
    fun getBookingById(bookingId: Long): Flow<NetworkResult<BookingDto>>
    fun getMyBookings(page: Int, size: Int, status: List<String>? = null): Flow<NetworkResult<PageDto<BookingDto>>>
    fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>>
    fun initiatePayment(bookingId: Long): Flow<NetworkResult<Map<String, String>>>
}

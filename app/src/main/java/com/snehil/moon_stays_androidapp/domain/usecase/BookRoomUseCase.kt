package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingDto
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto
import com.snehil.moon_stays_androidapp.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class BookRoomUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(request: BookingRequest, guests: List<GuestDto>): Flow<NetworkResult<BookingDto>> = flow {
        emit(NetworkResult.Loading)
        bookingRepository.initialiseBooking(request).collect { initResult ->
            when (initResult) {
                is NetworkResult.Loading -> emit(NetworkResult.Loading)
                is NetworkResult.Error -> emit(NetworkResult.Error(initResult.message, initResult.cause))
                is NetworkResult.Success -> {
                    val bookingId = initResult.data.id
                    bookingRepository.addGuests(bookingId, guests).collect { guestResult ->
                        when (guestResult) {
                            is NetworkResult.Loading -> emit(NetworkResult.Loading)
                            is NetworkResult.Error -> emit(NetworkResult.Error(guestResult.message, guestResult.cause))
                            is NetworkResult.Success -> {
                                // Once guests are added, proceed to payment
                                bookingRepository.initiatePayment(bookingId).collect { paymentResult ->
                                    when (paymentResult) {
                                        is NetworkResult.Loading -> emit(NetworkResult.Loading)
                                        is NetworkResult.Error -> emit(NetworkResult.Error(paymentResult.message, paymentResult.cause))
                                        is NetworkResult.Success -> {
                                            // Return success with the completed BookingDto
                                            emit(NetworkResult.Success(guestResult.data))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

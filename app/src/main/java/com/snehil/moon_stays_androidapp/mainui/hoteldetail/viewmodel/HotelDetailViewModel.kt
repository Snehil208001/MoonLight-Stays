package com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel

import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.domain.usecase.AddReviewUseCase
import com.snehil.moon_stays_androidapp.domain.usecase.BookRoomUseCase
import com.snehil.moon_stays_androidapp.domain.usecase.GetHotelInfoUseCase
import com.snehil.moon_stays_androidapp.domain.usecase.GetReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import javax.inject.Inject

data class RoomDto(
    val id: Int,
    val types: String,
    val basePrice: Double,
    val capacity: Int,
    val amenities: List<String>
)

data class ReviewDto(
    val id: Int,
    val rating: Int,
    val content: String,
    val hotelId: Int,
    val userName: String = "Voyager"
)

data class BookingRequest(
    val hotelId: Int,
    val roomId: Int,
    val checkInDate: String,
    val checkOutDate: String,
    val roomsCount: Int,
    val totalAmount: Double
)

// Helper mapping extensions
fun com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto.toDomain(): RoomDto {
    return RoomDto(
        id = this.id.toInt(),
        types = this.types,
        basePrice = this.basePrice.toDouble(),
        capacity = this.capacity ?: 1,
        amenities = this.amenities ?: emptyList()
    )
}

fun com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto.toDomain(): ReviewDto {
    return ReviewDto(
        id = this.id?.toInt() ?: 0,
        rating = this.rating,
        content = this.content,
        hotelId = this.hotelId?.toInt() ?: 0,
        userName = "Voyager"
    )
}

@HiltViewModel
class HotelDetailViewModel @Inject constructor(
    private val getHotelInfoUseCase: GetHotelInfoUseCase,
    private val bookRoomUseCase: BookRoomUseCase,
    private val addReviewUseCase: AddReviewUseCase,
    private val getReviewsUseCase: GetReviewsUseCase
) : BaseViewModel() {

    private val _isBookingLoading = MutableStateFlow(false)
    val isBookingLoading: StateFlow<Boolean> = _isBookingLoading.asStateFlow()

    private val _isBookingSuccess = MutableStateFlow(false)
    val isBookingSuccess: StateFlow<Boolean> = _isBookingSuccess.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomDto>>(emptyList())
    val rooms: StateFlow<List<RoomDto>> = _rooms.asStateFlow()

    // Reviews list state mapping hotelId to reviews
    private val _reviews = MutableStateFlow<Map<Int, List<ReviewDto>>>(emptyMap())
    val reviews: StateFlow<Map<Int, List<ReviewDto>>> = _reviews.asStateFlow()

    fun getRoomsForHotel(hotelId: Int): List<RoomDto> {
        // Return cached list for UI compat, but we load dynamically
        return _rooms.value
    }

    fun getReviewsForHotel(hotelId: Int): List<ReviewDto> {
        return _reviews.value[hotelId] ?: emptyList()
    }

    fun fetchHotelDetails(hotelId: Int) {
        launchSafe {
            getHotelInfoUseCase(hotelId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _rooms.value = result.data.rooms.map { it.toDomain() }
                    }
                }
            }
        }
        fetchReviews(hotelId)
    }

    fun fetchReviews(hotelId: Int) {
        launchSafe {
            getReviewsUseCase(hotelId.toLong(), 0, 50).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val list = result.data.content.map { it.toDomain() }
                        _reviews.value = _reviews.value + (hotelId to list)
                    }
                    else -> {}
                }
            }
        }
    }

    fun addReview(hotelId: Int, rating: Int, content: String) {
        launchSafe {
            addReviewUseCase(hotelId.toLong(), rating, content).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        // Refresh reviews
                        fetchReviews(hotelId)
                    }
                    is NetworkResult.Error -> {
                        _errorMessage.value = result.message
                    }
                    else -> {}
                }
            }
        }
    }

    fun bookRoom(
        hotelId: Int,
        roomId: Int,
        checkInDate: String,
        checkOutDate: String,
        roomsCount: Int,
        totalAmount: Double,
        onSuccess: (BookingRequest) -> Unit
    ) {
        launchSafe {
            val request = com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest(
                hotelId = hotelId.toLong(),
                roomId = roomId.toLong(),
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                roomsCount = roomsCount
            )
            // Empty guest details template
            val guests = listOf(
                com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto(
                    name = "Primary Guest",
                    gender = "MALE",
                    age = 30
                )
            )
            bookRoomUseCase(request, guests).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isBookingLoading.value = true
                    }
                    is NetworkResult.Error -> {
                        _isBookingLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isBookingLoading.value = false
                        _isBookingSuccess.value = true
                        onSuccess(
                            BookingRequest(
                                hotelId = hotelId,
                                roomId = roomId,
                                checkInDate = checkInDate,
                                checkOutDate = checkOutDate,
                                roomsCount = roomsCount,
                                totalAmount = totalAmount
                            )
                        )
                    }
                }
            }
        }
    }

    fun resetSuccessState() {
        _isBookingSuccess.value = false
    }
}

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
    val amenities: List<String>,
    val totalCount: Int = 1,
    val photos: List<String> = emptyList()
)

data class ReviewDto(
    val id: Int,
    val rating: Int,
    val content: String,
    val hotelId: Int,
    val userId: Int = 0
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
        id = this.id?.toInt() ?: 0,
        types = this.types?.trim()?.ifBlank { "Unknown Room Type" } ?: "Unknown Room Type",
        basePrice = this.basePrice?.toDouble() ?: 0.0,
        capacity = this.capacity ?: 1,
        amenities = this.amenities ?: emptyList(),
        totalCount = this.totalCount ?: 1,
        photos = this.photos ?: emptyList()
    )
}

fun com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto.toDomain(): ReviewDto {
    return ReviewDto(
        id = this.id?.toInt() ?: 0,
        rating = this.rating,
        content = this.content,
        hotelId = this.hotelId?.toInt() ?: 0,
        userId = this.userId?.toInt() ?: 0
    )
}

data class RoomPrice(
    val roomId: Int,
    val pricePerNight: Double,
    val totalForStay: Double
)

@HiltViewModel
class HotelDetailViewModel @Inject constructor(
    private val getHotelInfoUseCase: GetHotelInfoUseCase,
    private val bookRoomUseCase: BookRoomUseCase,
    private val addReviewUseCase: AddReviewUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val hotelRepository: com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
) : BaseViewModel() {

    private val _isBookingLoading = MutableStateFlow(false)
    val isBookingLoading: StateFlow<Boolean> = _isBookingLoading.asStateFlow()

    private val _isBookingSuccess = MutableStateFlow(false)
    val isBookingSuccess: StateFlow<Boolean> = _isBookingSuccess.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomDto>>(emptyList())
    val rooms: StateFlow<List<RoomDto>> = _rooms.asStateFlow()

    private val _roomPrices = MutableStateFlow<List<RoomPrice>>(emptyList())
    val roomPrices: StateFlow<List<RoomPrice>> = _roomPrices.asStateFlow()

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating.asStateFlow()

    // Reviews list state mapping hotelId to reviews
    private val _reviews = MutableStateFlow<Map<Int, List<ReviewDto>>>(emptyMap())
    val reviews: StateFlow<Map<Int, List<ReviewDto>>> = _reviews.asStateFlow()

    fun getRoomsForHotel(hotelId: Int): List<RoomDto> {
        return _rooms.value
    }

    fun getReviewsForHotel(hotelId: Int): List<ReviewDto> {
        return _reviews.value[hotelId] ?: emptyList()
    }

    fun fetchHotelDetails(hotelId: Int) {
        android.util.Log.d("HotelDetailViewModel", "fetchHotelDetails - hotelId: $hotelId")
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
        fetchAverageRating(hotelId)
    }

    fun fetchRoomPrices(hotelId: Int, checkIn: String, checkOut: String, roomsCount: Int) {
        android.util.Log.d("HotelDetailViewModel", "fetchRoomPrices - hotelId: $hotelId, checkIn: $checkIn, checkOut: $checkOut, roomsCount: $roomsCount")
        launchSafe {
            hotelRepository.getRoomPrices(hotelId.toLong(), checkIn, checkOut, roomsCount).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {}
                    is NetworkResult.Error -> {
                        android.util.Log.e("HotelDetailViewModel", "fetchRoomPrices - Failed: ${result.message}")
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        val prices = result.data.map { dto ->
                            RoomPrice(
                                roomId = dto.roomId.toInt(),
                                pricePerNight = dto.pricePerNight.toDouble(),
                                totalForStay = dto.totalForStay.toDouble()
                            )
                        }
                        android.util.Log.d("HotelDetailViewModel", "fetchRoomPrices - Success! Loaded ${prices.size} room prices")
                        _roomPrices.value = prices
                    }
                }
            }
        }
    }

    fun fetchAverageRating(hotelId: Int) {
        android.util.Log.d("HotelDetailViewModel", "fetchAverageRating - hotelId: $hotelId")
        launchSafe {
            hotelRepository.getHotelAverageRating(hotelId.toLong()).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        android.util.Log.d("HotelDetailViewModel", "fetchAverageRating - Success! Average: ${result.data}")
                        _averageRating.value = result.data
                    }
                    is NetworkResult.Error -> {
                        android.util.Log.e("HotelDetailViewModel", "fetchAverageRating - Error: ${result.message}")
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
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
        guests: List<com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto>,
        promoCode: String? = null,
        onSuccess: (String) -> Unit
    ) {
        launchSafe {
            val request = com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest(
                hotelId = hotelId.toLong(),
                roomId = roomId.toLong(),
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                roomsCount = roomsCount,
                // Sent to the backend so the discount is applied to the real Stripe charge,
                // matching the web client (which passes promoCode to /bookings/init).
                promoCode = promoCode?.trim()?.ifBlank { null }
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
                        onSuccess(result.data)
                    }
                }
            }
        }
    }

    fun resetSuccessState() {
        _isBookingSuccess.value = false
    }
}

package com.snehil.moon_stays_androidapp.mainui.hoteldetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

@HiltViewModel
class HotelDetailViewModel @Inject constructor() : ViewModel() {

    private val _isBookingLoading = MutableStateFlow(false)
    val isBookingLoading: StateFlow<Boolean> = _isBookingLoading.asStateFlow()

    private val _isBookingSuccess = MutableStateFlow(false)
    val isBookingSuccess: StateFlow<Boolean> = _isBookingSuccess.asStateFlow()

    // Mock room list source
    private val allRooms = mapOf(
        1 to listOf(
            RoomDto(11, "Standard Capsule", 850.0, 1, listOf("Quantum Wifi", "Clean Air")),
            RoomDto(12, "Zenith Sky Suite", 1450.0, 2, listOf("Gravity Control", "Personal Spa", "Transparent Ceiling"))
        ),
        2 to listOf(
            RoomDto(21, "Neptune Sub Room", 1200.0, 2, listOf("Underwater View", "Private Bath")),
            RoomDto(22, "Ocean Abyssal Suite", 2200.0, 4, listOf("Panoramic Dome View", "Private Submarine Ride"))
        ),
        3 to listOf(
            RoomDto(31, "Neon Cyber Bed", 540.0, 1, listOf("Holo Screens", "Soundproof")),
            RoomDto(32, "Retro Neon Loft", 980.0, 2, listOf("Infinity Bath", "Private Bar Access", "Synth Sound System"))
        ),
        4 to listOf(
            RoomDto(41, "Green Bio Capsule", 620.0, 2, listOf("Natural Light", "Lush Forest Air")),
            RoomDto(42, "Starlight Lake Villa", 1150.0, 3, listOf("Bio-dome lake access", "Organic breakfast", "Yoga terrace"))
        )
    )

    // Reviews list state
    private val _reviews = MutableStateFlow<Map<Int, List<ReviewDto>>>(
        mapOf(
            1 to listOf(
                ReviewDto(1, 5, "Breathtaking sky view! The gravity control felt perfectly tuned.", 1, "Alice Chen"),
                ReviewDto(2, 4, "Excellent experience, but the oxygen loop pressure fluctuated slightly in the morning.", 1, "Marcus Aurelius")
            ),
            2 to listOf(
                ReviewDto(3, 5, "Sleeping under the Mariana Basin was unforgettable. Felt totally safe in the steel structure.", 2, "Bob D.")
            )
        )
    )
    val reviews: StateFlow<Map<Int, List<ReviewDto>>> = _reviews.asStateFlow()

    fun getRoomsForHotel(hotelId: Int): List<RoomDto> {
        return allRooms[hotelId] ?: emptyList()
    }

    fun getReviewsForHotel(hotelId: Int): List<ReviewDto> {
        return _reviews.value[hotelId] ?: emptyList()
    }

    fun addReview(hotelId: Int, rating: Int, content: String) {
        val currentReviews = _reviews.value[hotelId] ?: emptyList()
        val nextId = (currentReviews.maxOfOrNull { it.id } ?: 0) + 1
        val newReview = ReviewDto(nextId, rating, content, hotelId, "Voyager Snehi")
        _reviews.value = _reviews.value + (hotelId to (listOf(newReview) + currentReviews))
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
        viewModelScope.launch {
            _isBookingLoading.value = true
            delay(1500)
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

    fun resetSuccessState() {
        _isBookingSuccess.value = false
    }
}

data class BookingRequest(
    val hotelId: Int,
    val roomId: Int,
    val checkInDate: String,
    val checkOutDate: String,
    val roomsCount: Int,
    val totalAmount: Double
)

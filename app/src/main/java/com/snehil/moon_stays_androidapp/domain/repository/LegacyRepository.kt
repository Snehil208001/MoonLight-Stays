package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface LegacyRepository {

    // Auth Services
    fun signup(request: SignUpRequestDto): Flow<NetworkResult<UserDto>>
    fun login(request: LoginDto): Flow<NetworkResult<LoginResponseDto>>

    // Hotel & Inventory Administration
    fun createHotel(hotel: HotelDto): Flow<NetworkResult<HotelDto>>
    fun getHotelById(hotelId: Long): Flow<NetworkResult<HotelDto>>
    fun updateHotel(hotelId: Long, hotel: HotelDto): Flow<NetworkResult<HotelDto>>
    fun deleteHotel(hotelId: Long): Flow<NetworkResult<Unit>>

    // Room Layouts Manager
    fun createRoom(hotelId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>>
    fun getRoomById(hotelId: Long, roomId: Long): Flow<NetworkResult<RoomDto>>
    fun updateRoom(hotelId: Long, roomId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>>
    fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>>

    // Dynamic Operations & Overlays
    fun updateSurge(request: SurgeUpdateDto): Flow<NetworkResult<Unit>>
    fun searchHotels(request: HotelSearchRequest): Flow<NetworkResult<List<HotelPriceDto>>>
    fun getHotelInfo(hotelId: Long): Flow<NetworkResult<HotelInfoDto>>
    fun initiateBooking(request: BookingRequest): Flow<NetworkResult<BookingDto>>
    fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>>
    fun getMyBookings(page: Int, size: Int): Flow<NetworkResult<PageDto<BookingDto>>>

    // Extras Engines
    fun createPromoCode(promo: PromoCodeDto): Flow<NetworkResult<PromoCodeDto>>
    fun getPromoCodes(): Flow<NetworkResult<List<PromoCodeDto>>>
    fun addReview(review: ReviewDto): Flow<NetworkResult<ReviewDto>>
    fun getReviews(hotelId: Long, page: Int, size: Int): Flow<NetworkResult<PageDto<ReviewDto>>>
    fun getProfile(): Flow<NetworkResult<UserDto>>
    fun updateProfile(request: ProfileUpdateDto): Flow<NetworkResult<UserDto>>
    fun uploadImage(file: MultipartBody.Part): Flow<NetworkResult<Map<String, String>>>
    fun handleStripeWebhook(payload: String): Flow<NetworkResult<Unit>>
}

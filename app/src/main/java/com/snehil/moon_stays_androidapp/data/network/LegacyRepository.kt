package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.network.dto.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface LegacyRepository {
    fun signup(request: LegacySignUpRequest): Flow<NetworkResult<LegacyUserDto>>
    fun login(request: LegacyLoginRequest): Flow<NetworkResult<LegacyLoginResponse>>
    fun createHotel(hotel: LegacyHotelDto): Flow<NetworkResult<LegacyHotelDto>>
    fun getAdminHotel(hotelId: Long): Flow<NetworkResult<LegacyHotelDto>>
    fun updateHotel(hotelId: Long, hotel: LegacyHotelDto): Flow<NetworkResult<LegacyHotelDto>>
    fun deleteHotel(hotelId: Long): Flow<NetworkResult<Unit>>
    fun createRoom(hotelId: Long, room: LegacyRoomDto): Flow<NetworkResult<LegacyRoomDto>>
    fun getRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<LegacyRoomDto>>
    fun updateRoom(hotelId: Long, roomId: Long, room: LegacyRoomDto): Flow<NetworkResult<LegacyRoomDto>>
    fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>>
    fun updateSurge(request: LegacySurgeUpdateDto): Flow<NetworkResult<Unit>>
    fun searchHotels(request: LegacyHotelSearchRequest): Flow<NetworkResult<List<LegacyHotelPriceDto>>>
    fun getHotelDetails(hotelId: Long): Flow<NetworkResult<LegacyHotelDto>>
    fun initiateBooking(request: LegacyBookingRequest): Flow<NetworkResult<LegacyBookingDto>>
    fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>>
    fun getMyBookings(): Flow<NetworkResult<List<LegacyBookingDto>>>
    fun createPromo(promo: LegacyPromoCodeDto): Flow<NetworkResult<LegacyPromoCodeDto>>
    fun getPromos(): Flow<NetworkResult<List<LegacyPromoCodeDto>>>
    fun postReview(review: LegacyReviewDto): Flow<NetworkResult<LegacyReviewDto>>
    fun getReviews(hotelId: Long): Flow<NetworkResult<List<LegacyReviewDto>>>
    fun getProfile(): Flow<NetworkResult<LegacyUserDto>>
    fun updateProfile(profile: LegacyProfileUpdateDto): Flow<NetworkResult<LegacyUserDto>>
    fun uploadImage(file: MultipartBody.Part): Flow<NetworkResult<Map<String, String>>>
    fun handleStripeWebhook(payload: RequestBody): Flow<NetworkResult<Unit>>
}

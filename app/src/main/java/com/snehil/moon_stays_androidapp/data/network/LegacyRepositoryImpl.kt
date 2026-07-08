package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.network.dto.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyRepositoryImpl @Inject constructor(
    private val legacyApiService: LegacyApiService
) : LegacyRepository {

    override fun signup(request: LegacySignUpRequest): Flow<NetworkResult<LegacyUserDto>> =
        safeApiCall { legacyApiService.signup(request) }

    override fun login(request: LegacyLoginRequest): Flow<NetworkResult<LegacyLoginResponse>> =
        safeApiCall { legacyApiService.login(request) }

    override fun createHotel(hotel: LegacyHotelDto): Flow<NetworkResult<LegacyHotelDto>> =
        safeApiCall { legacyApiService.createHotel(hotel) }

    override fun getAdminHotel(hotelId: Long): Flow<NetworkResult<LegacyHotelDto>> =
        safeApiCall { legacyApiService.getAdminHotel(hotelId) }

    override fun updateHotel(hotelId: Long, hotel: LegacyHotelDto): Flow<NetworkResult<LegacyHotelDto>> =
        safeApiCall { legacyApiService.updateHotel(hotelId, hotel) }

    override fun deleteHotel(hotelId: Long): Flow<NetworkResult<Unit>> =
        safeApiCall { legacyApiService.deleteHotel(hotelId) }

    override fun createRoom(hotelId: Long, room: LegacyRoomDto): Flow<NetworkResult<LegacyRoomDto>> =
        safeApiCall { legacyApiService.createRoom(hotelId, room) }

    override fun getRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<LegacyRoomDto>> =
        safeApiCall { legacyApiService.getRoom(hotelId, roomId) }

    override fun updateRoom(hotelId: Long, roomId: Long, room: LegacyRoomDto): Flow<NetworkResult<LegacyRoomDto>> =
        safeApiCall { legacyApiService.updateRoom(hotelId, roomId, room) }

    override fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>> =
        safeApiCall { legacyApiService.deleteRoom(hotelId, roomId) }

    override fun updateSurge(request: LegacySurgeUpdateDto): Flow<NetworkResult<Unit>> =
        safeApiCall { legacyApiService.updateSurge(request) }

    override fun searchHotels(request: LegacyHotelSearchRequest): Flow<NetworkResult<List<LegacyHotelPriceDto>>> =
        safeApiCall { legacyApiService.searchHotels(request) }

    override fun getHotelDetails(hotelId: Long): Flow<NetworkResult<LegacyHotelDto>> =
        safeApiCall { legacyApiService.getHotelDetails(hotelId) }

    override fun initiateBooking(request: LegacyBookingRequest): Flow<NetworkResult<LegacyBookingDto>> =
        safeApiCall { legacyApiService.initiateBooking(request) }

    override fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>> =
        safeApiCall { legacyApiService.cancelBooking(bookingId) }

    override fun getMyBookings(): Flow<NetworkResult<List<LegacyBookingDto>>> =
        safeApiCall { legacyApiService.getMyBookings() }

    override fun createPromo(promo: LegacyPromoCodeDto): Flow<NetworkResult<LegacyPromoCodeDto>> =
        safeApiCall { legacyApiService.createPromo(promo) }

    override fun getPromos(): Flow<NetworkResult<List<LegacyPromoCodeDto>>> =
        safeApiCall { legacyApiService.getPromos() }

    override fun postReview(review: LegacyReviewDto): Flow<NetworkResult<LegacyReviewDto>> =
        safeApiCall { legacyApiService.postReview(review) }

    override fun getReviews(hotelId: Long): Flow<NetworkResult<List<LegacyReviewDto>>> =
        safeApiCall { legacyApiService.getReviews(hotelId) }

    override fun getProfile(): Flow<NetworkResult<LegacyUserDto>> =
        safeApiCall { legacyApiService.getProfile() }

    override fun updateProfile(profile: LegacyProfileUpdateDto): Flow<NetworkResult<LegacyUserDto>> =
        safeApiCall { legacyApiService.updateProfile(profile) }

    override fun uploadImage(file: MultipartBody.Part): Flow<NetworkResult<Map<String, String>>> =
        safeApiCall { legacyApiService.uploadImage(file) }

    override fun handleStripeWebhook(payload: RequestBody): Flow<NetworkResult<Unit>> =
        safeApiCall { legacyApiService.handleStripeWebhook(payload) }
}

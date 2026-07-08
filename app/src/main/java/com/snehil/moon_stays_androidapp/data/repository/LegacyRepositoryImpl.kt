package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.LegacyApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.*
import com.snehil.moon_stays_androidapp.domain.repository.LegacyRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyRepositoryImpl @Inject constructor(
    private val legacyApiService: LegacyApiService
) : LegacyRepository {

    override fun signup(request: SignUpRequestDto): Flow<NetworkResult<UserDto>> {
        return safeApiCall { legacyApiService.signup(request) }
    }

    override fun login(request: LoginDto): Flow<NetworkResult<LoginResponseDto>> {
        return safeApiCall { legacyApiService.login(request) }
    }

    override fun createHotel(hotel: HotelDto): Flow<NetworkResult<HotelDto>> {
        return safeApiCall { legacyApiService.createHotel(hotel) }
    }

    override fun getHotelById(hotelId: Long): Flow<NetworkResult<HotelDto>> {
        return safeApiCall { legacyApiService.getHotelById(hotelId) }
    }

    override fun updateHotel(hotelId: Long, hotel: HotelDto): Flow<NetworkResult<HotelDto>> {
        return safeApiCall { legacyApiService.updateHotel(hotelId, hotel) }
    }

    override fun deleteHotel(hotelId: Long): Flow<NetworkResult<Unit>> {
        return safeApiCall { legacyApiService.deleteHotel(hotelId) }
    }

    override fun createRoom(hotelId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>> {
        return safeApiCall { legacyApiService.createRoom(hotelId, room) }
    }

    override fun getRoomById(hotelId: Long, roomId: Long): Flow<NetworkResult<RoomDto>> {
        return safeApiCall { legacyApiService.getRoomById(hotelId, roomId) }
    }

    override fun updateRoom(hotelId: Long, roomId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>> {
        return safeApiCall { legacyApiService.updateRoom(hotelId, roomId, room) }
    }

    override fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>> {
        return safeApiCall { legacyApiService.deleteRoom(hotelId, roomId) }
    }

    override fun updateSurge(request: SurgeUpdateDto): Flow<NetworkResult<Unit>> {
        return safeApiCall { legacyApiService.updateSurge(request) }
    }

    override fun searchHotels(request: HotelSearchRequest): Flow<NetworkResult<List<HotelPriceDto>>> {
        return safeApiCall { legacyApiService.searchHotels(request) }
    }

    override fun getHotelInfo(hotelId: Long): Flow<NetworkResult<HotelInfoDto>> {
        return safeApiCall { legacyApiService.getHotelInfo(hotelId) }
    }

    override fun initiateBooking(request: BookingRequest): Flow<NetworkResult<BookingDto>> {
        return safeApiCall { legacyApiService.initiateBooking(request) }
    }

    override fun cancelBooking(bookingId: Long): Flow<NetworkResult<Unit>> {
        return safeApiCall { legacyApiService.cancelBooking(bookingId) }
    }

    override fun getMyBookings(page: Int, size: Int): Flow<NetworkResult<PageDto<BookingDto>>> {
        return safeApiCall { legacyApiService.getMyBookings(page, size) }
    }

    override fun createPromoCode(promo: PromoCodeDto): Flow<NetworkResult<PromoCodeDto>> {
        return safeApiCall { legacyApiService.createPromoCode(promo) }
    }

    override fun getPromoCodes(): Flow<NetworkResult<List<PromoCodeDto>>> {
        return safeApiCall { legacyApiService.getPromoCodes() }
    }

    override fun addReview(review: ReviewDto): Flow<NetworkResult<ReviewDto>> {
        return safeApiCall { legacyApiService.addReview(review) }
    }

    override fun getReviews(hotelId: Long, page: Int, size: Int): Flow<NetworkResult<PageDto<ReviewDto>>> {
        return safeApiCall { legacyApiService.getReviews(hotelId, page, size) }
    }

    override fun getProfile(): Flow<NetworkResult<UserDto>> {
        return safeApiCall { legacyApiService.getProfile() }
    }

    override fun updateProfile(request: ProfileUpdateDto): Flow<NetworkResult<UserDto>> {
        return safeApiCall { legacyApiService.updateProfile(request) }
    }

    override fun uploadImage(file: MultipartBody.Part): Flow<NetworkResult<Map<String, String>>> {
        // Try the standard images/upload first; if that fails or behaves unexpectedly, fallback can be handled.
        // But for safeApiCall, we wrap the Retrofit call directly.
        return safeApiCall { legacyApiService.uploadImage(file) }
    }

    override fun handleStripeWebhook(payload: String): Flow<NetworkResult<Unit>> {
        return safeApiCall { legacyApiService.handleStripeWebhook(payload) }
    }
}

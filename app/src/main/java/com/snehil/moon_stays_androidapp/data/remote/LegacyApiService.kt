package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface LegacyApiService {

    // Auth Services
    @POST("auth/signup")
    suspend fun signup(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginDto): Response<LoginResponseDto>

    // Hotel & Inventory Administration (Host View Controls)
    @POST("admin/hotels")
    suspend fun createHotel(@Body hotel: HotelDto): Response<HotelDto>

    @GET("admin/hotels/{hotelId}")
    suspend fun getHotelById(@Path("hotelId") hotelId: Long): Response<HotelDto>

    @PUT("admin/hotels/{hotelId}")
    suspend fun updateHotel(@Path("hotelId") hotelId: Long, @Body hotel: HotelDto): Response<HotelDto>

    @DELETE("admin/hotels/{hotelId}")
    suspend fun deleteHotel(@Path("hotelId") hotelId: Long): Response<Unit>

    // Granular Inventory Layouts Manager
    @POST("admin/hotels/{hotelId}/rooms")
    suspend fun createRoom(@Path("hotelId") hotelId: Long, @Body room: RoomDto): Response<RoomDto>

    @GET("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun getRoomById(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<RoomDto>

    @PUT("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun updateRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long, @Body room: RoomDto): Response<RoomDto>

    @DELETE("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun deleteRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<Unit>

    // Dynamic Operations & Overlays
    @PUT("admin/hotels/surge")
    suspend fun updateSurge(@Body request: SurgeUpdateDto): Response<Unit>

    @POST("browse/search")
    suspend fun searchHotels(@Body request: HotelSearchRequest): Response<List<HotelPriceDto>>

    @GET("browse/hotels/{hotelId}")
    suspend fun getHotelInfo(@Path("hotelId") hotelId: Long): Response<HotelInfoDto>

    @POST("bookings/initiate")
    suspend fun initiateBooking(@Body request: BookingRequest): Response<BookingDto>

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: Long): Response<Unit>

    @GET("bookings/my-bookings")
    suspend fun getMyBookings(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageDto<BookingDto>>

    // System Extras Engines
    @POST("promo")
    suspend fun createPromoCode(@Body promo: PromoCodeDto): Response<PromoCodeDto>

    @GET("promo")
    suspend fun getPromoCodes(): Response<List<PromoCodeDto>>

    @POST("reviews")
    suspend fun addReview(@Body review: ReviewDto): Response<ReviewDto>

    @GET("reviews/{hotelId}")
    suspend fun getReviews(
        @Path("hotelId") hotelId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageDto<ReviewDto>>

    @GET("users/profile")
    suspend fun getProfile(): Response<UserDto>

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateDto): Response<UserDto>

    @Multipart
    @POST("images/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Map<String, String>>

    // Fallback for upload path matching Spring Boot monolith `/upload/image`
    @Multipart
    @POST("upload/image")
    suspend fun uploadImageFallback(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @POST("webhook/stripe")
    suspend fun handleStripeWebhook(@Body payload: String): Response<Unit>
}

package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.data.network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface LegacyApiService {

    @POST("auth/signup")
    suspend fun signup(@Body request: LegacySignUpRequest): Response<LegacyUserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LegacyLoginRequest): Response<LegacyLoginResponse>

    @POST("admin/hotels")
    suspend fun createHotel(@Body hotel: LegacyHotelDto): Response<LegacyHotelDto>

    @GET("admin/hotels/{hotelId}")
    suspend fun getAdminHotel(@Path("hotelId") hotelId: Long): Response<LegacyHotelDto>

    @PUT("admin/hotels/{hotelId}")
    suspend fun updateHotel(@Path("hotelId") hotelId: Long, @Body hotel: LegacyHotelDto): Response<LegacyHotelDto>

    @DELETE("admin/hotels/{hotelId}")
    suspend fun deleteHotel(@Path("hotelId") hotelId: Long): Response<Unit>

    @POST("admin/hotels/{hotelId}/rooms")
    suspend fun createRoom(@Path("hotelId") hotelId: Long, @Body room: LegacyRoomDto): Response<LegacyRoomDto>

    @GET("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun getRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<LegacyRoomDto>

    @PUT("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun updateRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long, @Body room: LegacyRoomDto): Response<LegacyRoomDto>

    @DELETE("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun deleteRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<Unit>

    @PUT("admin/hotels/surge")
    suspend fun updateSurge(@Body request: LegacySurgeUpdateDto): Response<Unit>

    @POST("browse/search")
    suspend fun searchHotels(@Body request: LegacyHotelSearchRequest): Response<List<LegacyHotelPriceDto>>

    @GET("browse/hotels/{hotelId}")
    suspend fun getHotelDetails(@Path("hotelId") hotelId: Long): Response<LegacyHotelDto>

    @POST("bookings/initiate")
    suspend fun initiateBooking(@Body request: LegacyBookingRequest): Response<LegacyBookingDto>

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: Long): Response<Unit>

    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): Response<List<LegacyBookingDto>>

    @POST("promo")
    suspend fun createPromo(@Body promo: LegacyPromoCodeDto): Response<LegacyPromoCodeDto>

    @GET("promo")
    suspend fun getPromos(): Response<List<LegacyPromoCodeDto>>

    @POST("reviews")
    suspend fun postReview(@Body review: LegacyReviewDto): Response<LegacyReviewDto>

    @GET("reviews/{hotelId}")
    suspend fun getReviews(@Path("hotelId") hotelId: Long): Response<List<LegacyReviewDto>>

    @GET("users/profile")
    suspend fun getProfile(): Response<LegacyUserDto>

    @PUT("users/profile")
    suspend fun updateProfile(@Body profile: LegacyProfileUpdateDto): Response<LegacyUserDto>

    @Multipart
    @POST("images/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @POST("webhook/stripe")
    suspend fun handleStripeWebhook(@Body payload: RequestBody): Response<Unit>
}

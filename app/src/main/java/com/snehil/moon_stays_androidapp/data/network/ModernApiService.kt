package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ModernApiService {

    @POST("auth/register")
    suspend fun register(@Body request: ModernRegisterRequest): Response<ModernUserMeResponse>

    @POST("auth/login")
    suspend fun login(@Body request: ModernLoginRequest): Response<ModernAuthResponse>

    @GET("properties")
    suspend fun getProperties(): Response<List<ModernPropertyDto>>

    @POST("properties")
    suspend fun createProperty(@Body property: ModernPropertyDto): Response<ModernPropertyDto>

    @GET("properties/{id}")
    suspend fun getPropertyDetails(@Path("id") id: Long): Response<ModernPropertyDto>

    @GET("bookings")
    suspend fun getBookings(): Response<List<ModernBookingDto>>

    @POST("bookings")
    suspend fun createBooking(@Body booking: ModernBookingDto): Response<ModernBookingDto>

    @GET("bookings/{id}")
    suspend fun getBookingDetails(@Path("id") id: Long): Response<ModernBookingDto>

    @GET("users/me")
    suspend fun getMyProfile(): Response<ModernUserMeResponse>
}

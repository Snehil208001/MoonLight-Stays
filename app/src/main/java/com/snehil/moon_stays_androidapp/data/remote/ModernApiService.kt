package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ModernApiService {

    // Identity Sync Services
    @POST("auth/register")
    suspend fun register(@Body request: RegistrationRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: ModernLoginRequest): Response<AuthResponse>

    // Real-Time Real Estate Catalog
    @GET("properties")
    suspend fun getProperties(): Response<List<ModernPropertyDto>>

    @POST("properties")
    suspend fun createProperty(@Body property: ModernPropertyDto): Response<ModernPropertyDto>

    @GET("properties/{id}")
    suspend fun getPropertyById(@Path("id") id: Long): Response<ModernPropertyDto>

    // Transactional Stays Pipeline
    @GET("bookings")
    suspend fun getMyBookings(): Response<List<ModernBookingDto>>

    @POST("bookings")
    suspend fun createBooking(@Body booking: ModernBookingDto): Response<ModernBookingDto>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): Response<ModernBookingDto>

    // Active Context Claim Engine
    @GET("users/me")
    suspend fun getMe(): Response<ModernUserDto>
}

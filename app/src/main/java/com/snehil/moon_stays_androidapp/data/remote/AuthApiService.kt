package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginResponseDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.UserDto
import com.snehil.moon_stays_androidapp.data.remote.dto.ProfileUpdateDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.DELETE

interface AuthApiService {
    @GET("users/profile")
    suspend fun getProfile(): Response<UserDto>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateDto): Response<UserDto>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("auth/admin/signup")
    suspend fun adminSignup(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginDto): Response<LoginResponseDto>

    // Relies on the httpOnly refreshToken cookie held by SessionCookieJar;
    // normally invoked transparently by TokenAuthenticator on a 401.
    @POST("auth/refresh")
    suspend fun refreshToken(): Response<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("users/favorites")
    suspend fun getFavoriteHotels(): Response<List<HotelDto>>

    @POST("users/favorites/{hotelId}")
    suspend fun addHotelToFavorites(@Path("hotelId") hotelId: Long): Response<Unit>

    @DELETE("users/favorites/{hotelId}")
    suspend fun removeHotelFromFavorites(@Path("hotelId") hotelId: Long): Response<Unit>
}

package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginResponseDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/signup")
    suspend fun signup(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("auth/admin/signup")
    suspend fun adminSignup(@Body request: SignUpRequestDto): Response<UserDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginDto): Response<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}

package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginResponseDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.UserDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signup(signUpRequestDto: SignUpRequestDto): Flow<NetworkResult<UserDto>>
    fun adminSignup(signUpRequestDto: SignUpRequestDto): Flow<NetworkResult<UserDto>>
    fun login(loginDto: LoginDto): Flow<NetworkResult<LoginResponseDto>>
    fun logout(): Flow<NetworkResult<Unit>>
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveUserEmail(email: String)
    fun getUserEmail(): String?
    fun saveUserName(name: String)
    fun getUserName(): String?
    fun saveIsManager(isManager: Boolean)
    fun isManager(): Boolean
    fun clearSession()
}

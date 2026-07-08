package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.local.TokenManager
import com.snehil.moon_stays_androidapp.data.remote.AuthApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginResponseDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.UserDto
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override fun signup(signUpRequestDto: SignUpRequestDto): Flow<NetworkResult<UserDto>> {
        return safeApiCall { authApiService.signup(signUpRequestDto) }
    }

    override fun adminSignup(signUpRequestDto: SignUpRequestDto): Flow<NetworkResult<UserDto>> {
        return safeApiCall { authApiService.adminSignup(signUpRequestDto) }
    }

    override fun login(loginDto: LoginDto): Flow<NetworkResult<LoginResponseDto>> {
        return safeApiCall { authApiService.login(loginDto) }
    }

    override fun logout(): Flow<NetworkResult<Unit>> {
        return safeApiCall { authApiService.logout() }
    }

    override suspend fun fetchProfile(): UserDto? {
        return try {
            val response = authApiService.getProfile()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    override fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }

    override fun getToken(): String? {
        return tokenManager.getToken()
    }

    override fun saveUserEmail(email: String) {
        tokenManager.saveUserEmail(email)
    }

    override fun getUserEmail(): String? {
        return tokenManager.getUserEmail()
    }

    override fun saveUserName(name: String) {
        tokenManager.saveUserName(name)
    }

    override fun getUserName(): String? {
        return tokenManager.getUserName()
    }

    override fun saveIsManager(isManager: Boolean) {
        tokenManager.saveIsManager(isManager)
    }

    override fun isManager(): Boolean {
        return tokenManager.isManager()
    }

    override fun markOnboardingDone() {
        tokenManager.markOnboardingDone()
    }

    override fun isOnboardingDone(): Boolean {
        return tokenManager.isOnboardingDone()
    }

    override fun clearSession() {
        tokenManager.clear()
    }

    override fun updateProfile(name: String): Flow<NetworkResult<UserDto>> {
        android.util.Log.d("AuthRepository", "updateProfile - name: $name")
        return safeApiCall { authApiService.updateProfile(com.snehil.moon_stays_androidapp.data.remote.dto.ProfileUpdateDto(name)) }
            .onEach { result ->
                if (result is NetworkResult.Success) {
                    result.data.name?.let { saveUserName(it) }
                }
            }
    }

    override fun getFavoriteHotels(): Flow<NetworkResult<List<com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto>>> {
        android.util.Log.d("AuthRepository", "getFavoriteHotels - Fetching wishlisted hotels")
        return safeApiCall { authApiService.getFavoriteHotels() }
    }

    override fun addHotelToFavorites(hotelId: Long): Flow<NetworkResult<Unit>> {
        android.util.Log.d("AuthRepository", "addHotelToFavorites - hotelId: $hotelId")
        return safeApiCall { authApiService.addHotelToFavorites(hotelId) }
    }

    override fun removeHotelFromFavorites(hotelId: Long): Flow<NetworkResult<Unit>> {
        android.util.Log.d("AuthRepository", "removeHotelFromFavorites - hotelId: $hotelId")
        return safeApiCall { authApiService.removeHotelFromFavorites(hotelId) }
    }
}

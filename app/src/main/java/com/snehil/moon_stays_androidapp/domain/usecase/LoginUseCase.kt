package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginResponseDto
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(loginDto: LoginDto): Flow<NetworkResult<LoginResponseDto>> {
        return authRepository.login(loginDto).map { result ->
            if (result is NetworkResult.Success) {
                authRepository.saveToken(result.data.accessToken)
                authRepository.saveUserEmail(loginDto.email)

                // Name and role come from the backend profile, same as the web app —
                // HOTEL_MANAGER is assigned server-side, never inferred client-side.
                val profile = authRepository.fetchProfile()
                val fallbackName = loginDto.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                authRepository.saveUserName(profile?.name ?: fallbackName)
                authRepository.saveIsManager(profile?.roles?.contains("HOTEL_MANAGER") == true)
            }
            result
        }
    }
}

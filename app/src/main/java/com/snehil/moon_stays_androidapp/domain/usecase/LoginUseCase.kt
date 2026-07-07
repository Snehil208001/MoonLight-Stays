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
                // Save token to SharedPreferences
                authRepository.saveToken(result.data.accessToken)
                authRepository.saveUserEmail(loginDto.email)
                
                // Simple logic to parse user name from email as fallback
                val calculatedName = loginDto.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                authRepository.saveUserName(calculatedName)
                
                // Determine manager status by email contains manager
                val isManager = loginDto.email.contains("manager", ignoreCase = true)
                authRepository.saveIsManager(isManager)
            }
            result
        }
    }
}

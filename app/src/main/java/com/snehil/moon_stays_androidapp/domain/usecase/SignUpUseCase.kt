package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.UserDto
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(request: SignUpRequestDto, isManager: Boolean): Flow<NetworkResult<UserDto>> {
        return if (isManager) {
            authRepository.adminSignup(request)
        } else {
            authRepository.signup(request)
        }
    }
}

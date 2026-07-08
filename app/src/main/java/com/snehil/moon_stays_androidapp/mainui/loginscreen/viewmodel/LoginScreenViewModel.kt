package com.snehil.moon_stays_androidapp.mainui.loginscreen.viewmodel

import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.LoginDto
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import com.snehil.moon_stays_androidapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    fun onEmailChanged(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun signIn() {
        if (_email.value.isBlank() || _password.value.isBlank()) return

        launchSafe {
            loginUseCase(LoginDto(_email.value, _password.value)).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                        _errorMessage.value = null
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _loginSuccess.value = true
                    }
                }
            }
        }
    }

    fun resetSuccessState() {
        _loginSuccess.value = false
    }

    // Role saved by LoginUseCase from the backend profile (HOTEL_MANAGER role).
    fun isManagerAccount(): Boolean = authRepository.isManager()
}


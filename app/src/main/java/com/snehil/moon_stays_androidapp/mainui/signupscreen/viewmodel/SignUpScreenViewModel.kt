package com.snehil.moon_stays_androidapp.mainui.signupscreen.viewmodel

import com.snehil.moon_stays_androidapp.core.base.BaseViewModel
import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.SignUpRequestDto
import com.snehil.moon_stays_androidapp.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _confirmPasswordVisible = MutableStateFlow(false)
    val confirmPasswordVisible: StateFlow<Boolean> = _confirmPasswordVisible.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    fun onFullNameChanged(newValue: String) {
        _fullName.value = newValue
    }

    fun onEmailChanged(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChanged(newValue: String) {
        _password.value = newValue
    }

    fun onConfirmPasswordChanged(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun toggleConfirmPasswordVisibility() {
        _confirmPasswordVisible.value = !_confirmPasswordVisible.value
    }

    fun signUp() {
        if (_fullName.value.isBlank() ||
            _email.value.isBlank() ||
            _password.value.isBlank() ||
            _confirmPassword.value.isBlank() ||
            _password.value != _confirmPassword.value
        ) return

        launchSafe {
            val request = SignUpRequestDto(
                email = _email.value,
                password = _password.value,
                name = _fullName.value
            )
            signUpUseCase(request).collect { result ->
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
                        _signUpSuccess.value = true
                    }
                }
            }
        }
    }

    fun resetSuccessState() {
        _signUpSuccess.value = false
    }
}


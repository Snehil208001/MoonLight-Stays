package com.snehil.moon_stays_androidapp.mainui.signupscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor() : ViewModel() {

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

    private val _isHotelManager = MutableStateFlow(false)
    val isHotelManager: StateFlow<Boolean> = _isHotelManager.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    fun toggleHotelManager(newValue: Boolean) {
        _isHotelManager.value = newValue
    }

    fun signUp() {
        if (_fullName.value.isBlank() ||
            _email.value.isBlank() ||
            _password.value.isBlank() ||
            _confirmPassword.value.isBlank() ||
            _password.value != _confirmPassword.value
        ) return

        viewModelScope.launch {
            _isLoading.value = true
            // Simulate API signup request delay
            delay(1500)
            _isLoading.value = false
            _signUpSuccess.value = true
        }
    }

    fun resetSuccessState() {
        _signUpSuccess.value = false
    }
}

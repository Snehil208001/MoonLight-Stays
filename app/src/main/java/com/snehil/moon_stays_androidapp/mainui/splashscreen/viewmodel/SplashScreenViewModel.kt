package com.snehil.moon_stays_androidapp.mainui.splashscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _statusText = MutableStateFlow("Initializing Sanctuary")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    init {
        startLoading()
    }

    private fun startLoading() {
        viewModelScope.launch {
            // New design uses a 3-second immersive shader loading experience before transitioning
            val minSplashTime = launch { delay(3000) }
            validateSession()
            minSplashTime.join()
            _isFinished.value = true
        }
    }

    // Mirrors the web app's refreshAuth on load: a stored token is only trusted
    // if the backend still accepts it; otherwise the session is cleared.
    private suspend fun validateSession() {
        if (authRepository.getToken().isNullOrEmpty()) return
        val profile = authRepository.fetchProfile()
        if (profile == null) {
            authRepository.clearSession()
        } else {
            authRepository.saveUserName(profile.name)
            authRepository.saveIsManager(profile.roles.contains("HOTEL_MANAGER"))
        }
    }

    // Session gate — decides where splash lands, mirroring the web flow:
    // saved session → Dashboard; first launch → Onboarding; otherwise → Login.
    fun isLoggedIn(): Boolean = !authRepository.getToken().isNullOrEmpty()

    fun isManager(): Boolean = authRepository.isManager()

    fun isOnboardingDone(): Boolean = authRepository.isOnboardingDone()

    fun markOnboardingDone() = authRepository.markOnboardingDone()
}

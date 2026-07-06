package com.snehil.moon_stays_androidapp.mainui.splashscreen.viewmodel

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
class SplashScreenViewModel @Inject constructor() : ViewModel() {
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
            delay(3000)
            _isFinished.value = true
        }
    }
}
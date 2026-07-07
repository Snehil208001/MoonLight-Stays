package com.snehil.moon_stays_androidapp.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    protected val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _errorMessage.value = throwable.localizedMessage ?: "An unexpected error occurred"
        onError(throwable)
    }

    protected open fun onError(throwable: Throwable) {
        // Can be overridden by subclasses to handle specific errors
    }

    fun clearError() {
        _errorMessage.value = null
    }

    protected fun launchSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }
}

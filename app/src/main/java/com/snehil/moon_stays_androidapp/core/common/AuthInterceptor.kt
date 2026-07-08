package com.snehil.moon_stays_androidapp.core.common

import com.snehil.moon_stays_androidapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        val authenticatedRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // Handle error state status signals elegantly
        if (response.code == 401) {
            // Token is expired or invalid; clear session to trigger UI redirection to Auth Screen
            tokenManager.clear()
        }

        return response
    }
}

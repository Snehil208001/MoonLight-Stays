package com.snehil.moon_stays_androidapp.data.network

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

        // Elegant handling of systemic API status signals (e.g. invalidating tokens on 401)
        if (response.code == 401) {
            android.util.Log.w("AuthInterceptor", "Received 401 Unauthorized. Clearing local session.")
            tokenManager.clear()
        }

        return response
    }
}

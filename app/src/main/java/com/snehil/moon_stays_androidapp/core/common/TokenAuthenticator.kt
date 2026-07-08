package com.snehil.moon_stays_androidapp.core.common

import com.google.gson.JsonParser
import com.snehil.moon_stays_androidapp.data.local.TokenManager
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On a 401, calls POST /auth/refresh (authenticated by the httpOnly
 * refreshToken cookie held in SessionCookieJar), stores the new access token
 * and retries the original request once. Returning null lets the 401 through,
 * where AuthInterceptor clears the session.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val cookieJar: SessionCookieJar
) : Authenticator {

    // Bare client (no auth interceptor / no authenticator, to avoid recursion)
    // sharing the session cookie jar so the refreshToken cookie is sent.
    private val refreshClient by lazy {
        OkHttpClient.Builder().cookieJar(cookieJar).build()
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        // 401s from the auth endpoints themselves mean bad credentials, not an expired token
        if (path.endsWith("/auth/login") ||
            path.endsWith("/auth/signup") ||
            path.endsWith("/auth/admin/signup") ||
            path.endsWith("/auth/refresh")
        ) return null

        // Give up after one refresh attempt for this call
        if (responseCount(response) >= 2) return null

        val newToken = synchronized(this) {
            val current = tokenManager.getToken()
            val failedWith = response.request.header("Authorization")?.removePrefix("Bearer ")
            // Another request may have refreshed the token while we waited on the lock
            if (!current.isNullOrEmpty() && current != failedWith) current
            else refreshAccessToken(response)
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun refreshAccessToken(failedResponse: Response): String? {
        return try {
            // Both backends serve under the /api/v1 context path
            val refreshUrl = failedResponse.request.url.newBuilder()
                .encodedPath("/api/v1/auth/refresh")
                .query(null)
                .build()
            val request = Request.Builder()
                .url(refreshUrl)
                .post(ByteArray(0).toRequestBody(null, 0, 0))
                .build()
            refreshClient.newCall(request).execute().use { res ->
                if (!res.isSuccessful) return null
                val raw = res.body?.string() ?: return null
                val root = JsonParser.parseString(raw)
                // Response is enveloped: {"timeStamp": ..., "data": {"accessToken": ...}}
                val payload = if (root.isJsonObject && root.asJsonObject.has("data")) {
                    root.asJsonObject.get("data")
                } else root
                payload.takeIf { it.isJsonObject }
                    ?.asJsonObject?.get("accessToken")
                    ?.takeIf { it.isJsonPrimitive }?.asString
                    ?.also { tokenManager.saveToken(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

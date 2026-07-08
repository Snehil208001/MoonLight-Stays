package com.snehil.moon_stays_androidapp.core.common

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cookie store. The backend sets an httpOnly "refreshToken" cookie on
 * POST /auth/login; keeping it for the app process lifetime lets the
 * TokenAuthenticator call POST /auth/refresh when the access token expires.
 */
@Singleton
class SessionCookieJar @Inject constructor() : CookieJar {

    private val store = mutableMapOf<String, List<Cookie>>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val kept = store[url.host].orEmpty().filter { old -> cookies.none { it.name == old.name } }
        store[url.host] = kept + cookies
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return store[url.host].orEmpty().filter { it.expiresAt > now && it.matches(url) }
    }

    @Synchronized
    fun clear() {
        store.clear()
    }
}

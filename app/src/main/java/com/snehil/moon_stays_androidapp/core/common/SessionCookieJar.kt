package com.snehil.moon_stays_androidapp.core.common

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistent cookie store. The backend sets an httpOnly "refreshToken" cookie on
 * POST /auth/login with a 6-month max-age; persisting it to disk (not just for the
 * process lifetime) lets the TokenAuthenticator call POST /auth/refresh after the
 * app is killed and reopened — so the user stays signed in instead of having to log
 * in again every launch.
 */
@Singleton
class SessionCookieJar @Inject constructor(
    @ApplicationContext private val context: Context
) : CookieJar {

    private val prefs = context.getSharedPreferences("moon_stays_cookies", Context.MODE_PRIVATE)
    private val gson = Gson()

    // host -> cookies
    private val store: MutableMap<String, List<Cookie>> = loadFromDisk()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val kept = store[url.host].orEmpty().filter { old -> cookies.none { it.name == old.name } }
        store[url.host] = kept + cookies
        persist()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return store[url.host].orEmpty().filter { it.expiresAt > now && it.matches(url) }
    }

    @Synchronized
    fun clear() {
        store.clear()
        prefs.edit().remove(KEY_COOKIES).apply()
    }

    // --- persistence ---

    private fun persist() {
        val now = System.currentTimeMillis()
        val serializable = store.mapValues { (_, cookies) ->
            // Only persist unexpired cookies (drops session cookies that already lapsed)
            cookies.filter { it.expiresAt > now }.map { SerializableCookie.from(it) }
        }.filterValues { it.isNotEmpty() }
        prefs.edit().putString(KEY_COOKIES, gson.toJson(serializable)).apply()
    }

    private fun loadFromDisk(): MutableMap<String, List<Cookie>> {
        val json = prefs.getString(KEY_COOKIES, null) ?: return mutableMapOf()
        return try {
            val type = object : TypeToken<Map<String, List<SerializableCookie>>>() {}.type
            val raw: Map<String, List<SerializableCookie>> = gson.fromJson(json, type) ?: emptyMap()
            val now = System.currentTimeMillis()
            raw.mapValues { (_, list) ->
                list.mapNotNull { it.toCookie() }.filter { it.expiresAt > now }
            }.filterValues { it.isNotEmpty() }.toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private data class SerializableCookie(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean
    ) {
        fun toCookie(): Cookie? = try {
            Cookie.Builder()
                .name(name)
                .value(value)
                .expiresAt(expiresAt)
                .path(path)
                .apply {
                    if (hostOnly) hostOnlyDomain(domain) else domain(domain)
                    if (secure) secure()
                    if (httpOnly) httpOnly()
                }
                .build()
        } catch (e: Exception) {
            null
        }

        companion object {
            fun from(c: Cookie) = SerializableCookie(
                name = c.name,
                value = c.value,
                expiresAt = c.expiresAt,
                domain = c.domain,
                path = c.path,
                secure = c.secure,
                httpOnly = c.httpOnly,
                hostOnly = c.hostOnly
            )
        }
    }

    companion object {
        private const val KEY_COOKIES = "session_cookies"
    }
}

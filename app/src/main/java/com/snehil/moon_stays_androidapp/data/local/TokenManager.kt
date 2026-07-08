package com.snehil.moon_stays_androidapp.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("moon_stays_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_MANAGER = "is_manager"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun saveIsManager(isManager: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MANAGER, isManager).apply()
    }

    fun isManager(): Boolean {
        return prefs.getBoolean(KEY_IS_MANAGER, false)
    }

    fun markOnboardingDone() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    fun isOnboardingDone(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    // Clears the session only; onboarding completion survives logout,
    // mirroring the web app's separate localStorage flag.
    fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_IS_MANAGER)
            .apply()
    }
}

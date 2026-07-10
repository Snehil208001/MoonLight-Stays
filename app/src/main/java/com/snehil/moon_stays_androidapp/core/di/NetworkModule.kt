package com.snehil.moon_stays_androidapp.core.di

import com.snehil.moon_stays_androidapp.core.common.AuthInterceptor
import com.snehil.moon_stays_androidapp.core.common.SessionCookieJar
import com.snehil.moon_stays_androidapp.core.common.TokenAuthenticator
import com.snehil.moon_stays_androidapp.data.remote.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LegacyRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ModernRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Monolith Core Client Base URL — deployed Azure backend.
    // For a local backend, use "http://10.0.2.2:8080/api/v1/" (emulator alias for localhost:8080).
    private const val LEGACY_BASE_URL = "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1/"
    
    // Modern Restructured Core Client Base URL
    private const val MODERN_BASE_URL = "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(authInterceptor: AuthInterceptor): Interceptor {
        return authInterceptor
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        sessionCookieJar: SessionCookieJar,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            // Keeps the httpOnly refreshToken cookie set by POST /auth/login
            .cookieJar(sessionCookieJar)
            // Refreshes the access token via POST /auth/refresh on 401 and retries once
            .authenticator(tokenAuthenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @LegacyRetrofit
    fun provideLegacyRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()
        return Retrofit.Builder()
            .baseUrl(LEGACY_BASE_URL)
            .client(okHttpClient)
            // Unwraps the legacy backend's {"timeStamp": ..., "data": <payload>} envelope
            .addConverterFactory(ApiEnvelopeConverterFactory(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @ModernRetrofit
    fun provideModernRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()
        return Retrofit.Builder()
            .baseUrl(MODERN_BASE_URL)
            .client(okHttpClient)
            // The modern restructured backend does NOT use the response envelope; responses are raw JSON.
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Legacy Client Api Services (mapping existing implementations)
    @Provides
    @Singleton
    fun provideAuthApiService(@LegacyRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHotelApiService(@LegacyRetrofit retrofit: Retrofit): HotelApiService {
        return retrofit.create(HotelApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBookingApiService(@LegacyRetrofit retrofit: Retrofit): BookingApiService {
        return retrofit.create(BookingApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAdminApiService(@LegacyRetrofit retrofit: Retrofit): AdminApiService {
        return retrofit.create(AdminApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiApiService(@LegacyRetrofit retrofit: Retrofit): AiApiService {
        return retrofit.create(AiApiService::class.java)
    }

    // Unified Legacy and Modern API interfaces definitions
    @Provides
    @Singleton
    fun provideLegacyApiService(@LegacyRetrofit retrofit: Retrofit): LegacyApiService {
        return retrofit.create(LegacyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideModernApiService(@ModernRetrofit retrofit: Retrofit): ModernApiService {
        return retrofit.create(ModernApiService::class.java)
    }
}

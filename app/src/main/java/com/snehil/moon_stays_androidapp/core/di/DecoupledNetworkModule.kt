package com.snehil.moon_stays_androidapp.core.di

import com.google.gson.Gson
import com.snehil.moon_stays_androidapp.data.local.TokenManager
import com.snehil.moon_stays_androidapp.data.network.*
import dagger.Binds
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
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DecoupledNetworkModule {

    private const val LEGACY_BASE_URL = "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1/"
    private const val MODERN_BASE_URL = "https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1/"

    @Provides
    @Singleton
    @Named("AuthInterceptor")
    fun provideDecoupledAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    @Named("DecoupledOkHttpClient")
    fun provideDecoupledOkHttpClient(
        @Named("AuthInterceptor") authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("LegacyRetrofit")
    fun provideLegacyRetrofit(@Named("DecoupledOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()
        return Retrofit.Builder()
            .baseUrl(LEGACY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("ModernRetrofit")
    fun provideModernRetrofit(@Named("DecoupledOkHttpClient") okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()
        return Retrofit.Builder()
            .baseUrl(MODERN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideLegacyApiService(@Named("LegacyRetrofit") retrofit: Retrofit): LegacyApiService {
        return retrofit.create(LegacyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideModernApiService(@Named("ModernRetrofit") retrofit: Retrofit): ModernApiService {
        return retrofit.create(ModernApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface DecoupledRepositoryModule {

    @Binds
    @Singleton
    fun bindLegacyRepository(impl: LegacyRepositoryImpl): LegacyRepository

    @Binds
    @Singleton
    fun bindModernRepository(impl: ModernRepositoryImpl): ModernRepository
}

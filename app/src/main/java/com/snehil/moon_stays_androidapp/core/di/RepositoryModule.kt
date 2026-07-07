package com.snehil.moon_stays_androidapp.core.di

import com.snehil.moon_stays_androidapp.data.repository.AuthRepositoryImpl
import com.snehil.moon_stays_androidapp.data.repository.BookingRepositoryImpl
import com.snehil.moon_stays_androidapp.data.repository.HotelRepositoryImpl
import com.snehil.moon_stays_androidapp.domain.repository.AuthRepository
import com.snehil.moon_stays_androidapp.domain.repository.BookingRepository
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHotelRepository(
        hotelRepositoryImpl: HotelRepositoryImpl
    ): HotelRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository
}

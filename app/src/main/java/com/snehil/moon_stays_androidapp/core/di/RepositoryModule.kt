package com.snehil.moon_stays_androidapp.core.di

import com.snehil.moon_stays_androidapp.data.repository.*
import com.snehil.moon_stays_androidapp.domain.repository.*
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

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindTripPlannerRepository(
        tripPlannerRepositoryImpl: TripPlannerRepositoryImpl
    ): TripPlannerRepository

    // Decoupled Multi-Backend Repositories
    @Binds
    @Singleton
    abstract fun bindLegacyRepository(
        legacyRepositoryImpl: LegacyRepositoryImpl
    ): LegacyRepository

    @Binds
    @Singleton
    abstract fun bindModernRepository(
        modernRepositoryImpl: ModernRepositoryImpl
    ): ModernRepository
}

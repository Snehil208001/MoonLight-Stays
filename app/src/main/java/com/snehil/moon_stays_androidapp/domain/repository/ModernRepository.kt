package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

interface ModernRepository {

    // Identity Sync Services
    fun register(request: RegistrationRequest): Flow<NetworkResult<AuthResponse>>
    fun login(request: ModernLoginRequest): Flow<NetworkResult<AuthResponse>>

    // Real-Time Real Estate Catalog
    fun getProperties(): Flow<NetworkResult<List<ModernPropertyDto>>>
    fun createProperty(property: ModernPropertyDto): Flow<NetworkResult<ModernPropertyDto>>
    fun getPropertyById(id: Long): Flow<NetworkResult<ModernPropertyDto>>

    // Transactional Stays Pipeline
    fun getMyBookings(): Flow<NetworkResult<List<ModernBookingDto>>>
    fun createBooking(booking: ModernBookingDto): Flow<NetworkResult<ModernBookingDto>>
    fun getBookingById(id: Long): Flow<NetworkResult<ModernBookingDto>>

    // Active Context Claim Engine
    fun getMe(): Flow<NetworkResult<ModernUserDto>>
}

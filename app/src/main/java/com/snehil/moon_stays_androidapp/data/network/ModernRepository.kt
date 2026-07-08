package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.network.dto.*
import kotlinx.coroutines.flow.Flow

interface ModernRepository {
    fun register(request: ModernRegisterRequest): Flow<NetworkResult<ModernUserMeResponse>>
    fun login(request: ModernLoginRequest): Flow<NetworkResult<ModernAuthResponse>>
    fun getProperties(): Flow<NetworkResult<List<ModernPropertyDto>>>
    fun createProperty(property: ModernPropertyDto): Flow<NetworkResult<ModernPropertyDto>>
    fun getPropertyDetails(id: Long): Flow<NetworkResult<ModernPropertyDto>>
    fun getBookings(): Flow<NetworkResult<List<ModernBookingDto>>>
    fun createBooking(booking: ModernBookingDto): Flow<NetworkResult<ModernBookingDto>>
    fun getBookingDetails(id: Long): Flow<NetworkResult<ModernBookingDto>>
    fun getMyProfile(): Flow<NetworkResult<ModernUserMeResponse>>
}

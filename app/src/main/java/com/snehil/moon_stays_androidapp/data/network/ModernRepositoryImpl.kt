package com.snehil.moon_stays_androidapp.data.network

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.network.dto.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModernRepositoryImpl @Inject constructor(
    private val modernApiService: ModernApiService
) : ModernRepository {

    override fun register(request: ModernRegisterRequest): Flow<NetworkResult<ModernUserMeResponse>> =
        safeApiCall { modernApiService.register(request) }

    override fun login(request: ModernLoginRequest): Flow<NetworkResult<ModernAuthResponse>> =
        safeApiCall { modernApiService.login(request) }

    override fun getProperties(): Flow<NetworkResult<List<ModernPropertyDto>>> =
        safeApiCall { modernApiService.getProperties() }

    override fun createProperty(property: ModernPropertyDto): Flow<NetworkResult<ModernPropertyDto>> =
        safeApiCall { modernApiService.createProperty(property) }

    override fun getPropertyDetails(id: Long): Flow<NetworkResult<ModernPropertyDto>> =
        safeApiCall { modernApiService.getPropertyDetails(id) }

    override fun getBookings(): Flow<NetworkResult<List<ModernBookingDto>>> =
        safeApiCall { modernApiService.getBookings() }

    override fun createBooking(booking: ModernBookingDto): Flow<NetworkResult<ModernBookingDto>> =
        safeApiCall { modernApiService.createBooking(booking) }

    override fun getBookingDetails(id: Long): Flow<NetworkResult<ModernBookingDto>> =
        safeApiCall { modernApiService.getBookingDetails(id) }

    override fun getMyProfile(): Flow<NetworkResult<ModernUserMeResponse>> =
        safeApiCall { modernApiService.getMyProfile() }
}

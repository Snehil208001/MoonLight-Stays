package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.ModernApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.*
import com.snehil.moon_stays_androidapp.domain.repository.ModernRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModernRepositoryImpl @Inject constructor(
    private val modernApiService: ModernApiService
) : ModernRepository {

    override fun register(request: RegistrationRequest): Flow<NetworkResult<AuthResponse>> {
        return safeApiCall { modernApiService.register(request) }
    }

    override fun login(request: ModernLoginRequest): Flow<NetworkResult<AuthResponse>> {
        return safeApiCall { modernApiService.login(request) }
    }

    override fun getProperties(): Flow<NetworkResult<List<ModernPropertyDto>>> {
        return safeApiCall { modernApiService.getProperties() }
    }

    override fun createProperty(property: ModernPropertyDto): Flow<NetworkResult<ModernPropertyDto>> {
        return safeApiCall { modernApiService.createProperty(property) }
    }

    override fun getPropertyById(id: Long): Flow<NetworkResult<ModernPropertyDto>> {
        return safeApiCall { modernApiService.getPropertyById(id) }
    }

    override fun getMyBookings(): Flow<NetworkResult<List<ModernBookingDto>>> {
        return safeApiCall { modernApiService.getMyBookings() }
    }

    override fun createBooking(booking: ModernBookingDto): Flow<NetworkResult<ModernBookingDto>> {
        return safeApiCall { modernApiService.createBooking(booking) }
    }

    override fun getBookingById(id: Long): Flow<NetworkResult<ModernBookingDto>> {
        return safeApiCall { modernApiService.getBookingById(id) }
    }

    override fun getMe(): Flow<NetworkResult<ModernUserDto>> {
        return safeApiCall { modernApiService.getMe() }
    }
}

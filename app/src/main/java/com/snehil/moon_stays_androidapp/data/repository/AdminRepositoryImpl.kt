package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.AdminApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SurgeUpdateDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoCodeDto
import com.snehil.moon_stays_androidapp.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApiService: AdminApiService
) : AdminRepository {
    override fun createHotel(hotel: HotelDto): Flow<NetworkResult<HotelDto>> = safeApiCall {
        adminApiService.createHotel(hotel)
    }

    override fun getMyHotels(): Flow<NetworkResult<List<HotelDto>>> = safeApiCall {
        adminApiService.getMyHotels()
    }

    override fun getHotelById(id: Long): Flow<NetworkResult<HotelDto>> = safeApiCall {
        adminApiService.getHotelById(id)
    }

    override fun updateHotel(id: Long, hotel: HotelDto): Flow<NetworkResult<HotelDto>> = safeApiCall {
        adminApiService.updateHotel(id, hotel)
    }

    override fun deleteHotel(id: Long): Flow<NetworkResult<Unit>> = safeApiCall {
        adminApiService.deleteHotel(id)
    }

    override fun toggleHotelStatus(id: Long): Flow<NetworkResult<Unit>> = safeApiCall {
        adminApiService.toggleHotelStatus(id)
    }

    override fun updateSurgeFactor(id: Long, request: SurgeUpdateDto): Flow<NetworkResult<Unit>> = safeApiCall {
        adminApiService.updateSurgeFactor(id, request)
    }

    override fun createRoom(hotelId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>> = safeApiCall {
        adminApiService.createRoom(hotelId, room)
    }

    override fun getHotelRooms(hotelId: Long): Flow<NetworkResult<List<RoomDto>>> = safeApiCall {
        adminApiService.getHotelRooms(hotelId)
    }

    override fun getRoomById(hotelId: Long, roomId: Long): Flow<NetworkResult<RoomDto>> = safeApiCall {
        adminApiService.getRoomById(hotelId, roomId)
    }

    override fun updateRoom(hotelId: Long, roomId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>> = safeApiCall {
        adminApiService.updateRoom(hotelId, roomId, room)
    }

    override fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>> = safeApiCall {
        adminApiService.deleteRoom(hotelId, roomId)
    }

    override fun getPromoCodes(): Flow<NetworkResult<List<PromoCodeDto>>> = safeApiCall {
        adminApiService.getPromoCodes()
    }

    override fun createPromoCode(promo: PromoCodeDto): Flow<NetworkResult<PromoCodeDto>> = safeApiCall {
        adminApiService.createPromoCode(promo)
    }

    override fun deletePromoCode(id: Long): Flow<NetworkResult<Unit>> = safeApiCall {
        adminApiService.deletePromoCode(id)
    }

    override fun uploadImage(file: okhttp3.MultipartBody.Part): Flow<NetworkResult<Map<String, String>>> = safeApiCall {
        adminApiService.uploadImage(file)
    }
}

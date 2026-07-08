package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SurgeUpdateDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoCodeDto
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun createHotel(hotel: HotelDto): Flow<NetworkResult<HotelDto>>
    fun getMyHotels(): Flow<NetworkResult<List<HotelDto>>>
    fun getHotelById(id: Long): Flow<NetworkResult<HotelDto>>
    fun updateHotel(id: Long, hotel: HotelDto): Flow<NetworkResult<HotelDto>>
    fun deleteHotel(id: Long): Flow<NetworkResult<Unit>>
    fun toggleHotelStatus(id: Long): Flow<NetworkResult<Unit>>
    fun updateSurgeFactor(id: Long, request: SurgeUpdateDto): Flow<NetworkResult<Unit>>
    
    fun createRoom(hotelId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>>
    fun getHotelRooms(hotelId: Long): Flow<NetworkResult<List<RoomDto>>>
    fun getRoomById(hotelId: Long, roomId: Long): Flow<NetworkResult<RoomDto>>
    fun updateRoom(hotelId: Long, roomId: Long, room: RoomDto): Flow<NetworkResult<RoomDto>>
    fun deleteRoom(hotelId: Long, roomId: Long): Flow<NetworkResult<Unit>>
    
    fun getPromoCodes(): Flow<NetworkResult<List<PromoCodeDto>>>
    fun createPromoCode(promo: PromoCodeDto): Flow<NetworkResult<PromoCodeDto>>
    fun deletePromoCode(id: Long): Flow<NetworkResult<Unit>>
}

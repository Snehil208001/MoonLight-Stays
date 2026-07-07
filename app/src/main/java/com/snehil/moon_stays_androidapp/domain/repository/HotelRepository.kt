package com.snehil.moon_stays_androidapp.domain.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelInfoDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomPriceDto
import kotlinx.coroutines.flow.Flow

interface HotelRepository {
    fun searchHotels(request: HotelSearchRequest): Flow<NetworkResult<List<HotelPriceDto>>>
    fun getHotelInfo(hotelId: Long): Flow<NetworkResult<HotelInfoDto>>
    fun getRoomPrices(
        hotelId: Long,
        checkIn: String,
        checkOut: String,
        roomsCount: Int
    ): Flow<NetworkResult<List<RoomPriceDto>>>
    fun addReview(
        hotelId: Long,
        review: com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto
    ): Flow<NetworkResult<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>>
    fun getHotelReviews(
        hotelId: Long,
        page: Int,
        size: Int
    ): Flow<NetworkResult<com.snehil.moon_stays_androidapp.data.remote.dto.PageDto<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>>>
}

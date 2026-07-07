package com.snehil.moon_stays_androidapp.data.repository

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.core.common.safeApiCall
import com.snehil.moon_stays_androidapp.data.remote.HotelApiService
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelInfoDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomPriceDto
import com.snehil.moon_stays_androidapp.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotelRepositoryImpl @Inject constructor(
    private val hotelApiService: HotelApiService
) : HotelRepository {

    override fun searchHotels(request: HotelSearchRequest): Flow<NetworkResult<List<HotelPriceDto>>> {
        return safeApiCall { hotelApiService.searchHotels(request) }
    }

    override fun getHotelInfo(hotelId: Long): Flow<NetworkResult<HotelInfoDto>> {
        return safeApiCall { hotelApiService.getHotelInfo(hotelId) }
    }

    override fun getRoomPrices(
        hotelId: Long,
        checkIn: String,
        checkOut: String,
        roomsCount: Int
    ): Flow<NetworkResult<List<RoomPriceDto>>> {
        return safeApiCall { hotelApiService.getRoomPrices(hotelId, checkIn, checkOut, roomsCount) }
    }

    override fun addReview(
        hotelId: Long,
        review: com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto
    ): Flow<NetworkResult<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>> {
        return safeApiCall { hotelApiService.addReview(hotelId, review) }
    }

    override fun getHotelReviews(
        hotelId: Long,
        page: Int,
        size: Int
    ): Flow<NetworkResult<com.snehil.moon_stays_androidapp.data.remote.dto.PageDto<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>>> {
        return safeApiCall { hotelApiService.getHotelReviews(hotelId, page, size) }
    }
}

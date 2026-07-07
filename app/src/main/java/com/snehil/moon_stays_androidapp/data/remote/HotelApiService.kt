package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.HotelInfoDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelPriceDto
import com.snehil.moon_stays_androidapp.data.remote.dto.HotelSearchRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomPriceDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelApiService {
    @POST("hotels/search")
    suspend fun searchHotels(@Body request: HotelSearchRequest): Response<List<HotelPriceDto>>

    @GET("hotels/{hotelId}/info")
    suspend fun getHotelInfo(@Path("hotelId") hotelId: Long): Response<HotelInfoDto>

    @GET("hotels/{hotelId}/room-prices")
    suspend fun getRoomPrices(
        @Path("hotelId") hotelId: Long,
        @Query("checkIn") checkIn: String,
        @Query("checkOut") checkOut: String,
        @Query("roomsCount") roomsCount: Int
    ): Response<List<RoomPriceDto>>

    @POST("hotels/{hotelId}/reviews")
    suspend fun addReview(
        @Path("hotelId") hotelId: Long,
        @Body review: com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto
    ): Response<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>

    @GET("hotels/{hotelId}/reviews")
    suspend fun getHotelReviews(
        @Path("hotelId") hotelId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<com.snehil.moon_stays_androidapp.data.remote.dto.PageDto<com.snehil.moon_stays_androidapp.data.remote.dto.ReviewDto>>
}

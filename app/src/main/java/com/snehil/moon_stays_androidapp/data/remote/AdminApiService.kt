package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.HotelDto
import com.snehil.moon_stays_androidapp.data.remote.dto.RoomDto
import com.snehil.moon_stays_androidapp.data.remote.dto.SurgeUpdateDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PromoCodeDto
import retrofit2.Response
import retrofit2.http.*

interface AdminApiService {
    // Hotel Admin APIs
    @POST("admin/hotels")
    suspend fun createHotel(@Body hotel: HotelDto): Response<HotelDto>

    @GET("admin/hotels")
    suspend fun getMyHotels(): Response<List<HotelDto>>

    @GET("admin/hotels/{id}")
    suspend fun getHotelById(@Path("id") id: Long): Response<HotelDto>

    @PUT("admin/hotels/{id}")
    suspend fun updateHotel(@Path("id") id: Long, @Body hotel: HotelDto): Response<HotelDto>

    @DELETE("admin/hotels/{id}")
    suspend fun deleteHotel(@Path("id") id: Long): Response<Unit>

    @PATCH("admin/hotels/{id}")
    suspend fun toggleHotelStatus(@Path("id") id: Long): Response<Unit>

    @PATCH("admin/hotels/{id}/surge")
    suspend fun updateSurgeFactor(@Path("id") id: Long, @Body request: SurgeUpdateDto): Response<Unit>

    // Room Admin APIs
    @POST("admin/hotels/{hotelId}/rooms")
    suspend fun createRoom(@Path("hotelId") hotelId: Long, @Body room: RoomDto): Response<RoomDto>

    @GET("admin/hotels/{hotelId}/rooms")
    suspend fun getHotelRooms(@Path("hotelId") hotelId: Long): Response<List<RoomDto>>

    @GET("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun getRoomById(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<RoomDto>

    @POST("admin/hotels/{hotelId}/rooms/{roomId}/update")
    suspend fun updateRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long, @Body room: RoomDto): Response<RoomDto>

    @DELETE("admin/hotels/{hotelId}/rooms/{roomId}")
    suspend fun deleteRoom(@Path("hotelId") hotelId: Long, @Path("roomId") roomId: Long): Response<Unit>

    // Promo Code Admin APIs
    @GET("admin/promocodes")
    suspend fun getPromoCodes(): Response<List<PromoCodeDto>>

    @POST("admin/promocodes")
    suspend fun createPromoCode(@Body promo: PromoCodeDto): Response<PromoCodeDto>

    @DELETE("admin/promocodes/{id}")
    suspend fun deletePromoCode(@Path("id") id: Long): Response<Unit>
}

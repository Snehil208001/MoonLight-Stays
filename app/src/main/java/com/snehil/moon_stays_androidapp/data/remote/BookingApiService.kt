package com.snehil.moon_stays_androidapp.data.remote

import com.snehil.moon_stays_androidapp.data.remote.dto.BookingDto
import com.snehil.moon_stays_androidapp.data.remote.dto.BookingRequest
import com.snehil.moon_stays_androidapp.data.remote.dto.GuestDto
import com.snehil.moon_stays_androidapp.data.remote.dto.PageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApiService {
    @POST("bookings/init")
    suspend fun initialiseBooking(@Body request: BookingRequest): Response<BookingDto>

    @POST("bookings/{bookingId}/addGuests")
    suspend fun addGuests(
        @Path("bookingId") bookingId: Long,
        @Body guests: List<GuestDto>
    ): Response<BookingDto>

    @GET("bookings/{bookingId}")
    suspend fun getBookingById(@Path("bookingId") bookingId: Long): Response<BookingDto>

    @GET("bookings/myBookings")
    suspend fun getMyBookings(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("status") status: List<String>? = null
    ): Response<PageDto<BookingDto>>

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: Long): Response<Unit>

    @POST("bookings/{bookingId}/payments")
    suspend fun initiatePayment(@Path("bookingId") bookingId: Long): Response<Map<String, String>>
}

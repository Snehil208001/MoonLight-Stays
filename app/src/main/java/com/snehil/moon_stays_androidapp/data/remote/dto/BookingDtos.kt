package com.snehil.moon_stays_androidapp.data.remote.dto

import java.math.BigDecimal

data class BookingRequest(
    val hotelId: Long,
    val roomId: Long,
    val checkInDate: String, // yyyy-MM-dd
    val checkOutDate: String, // yyyy-MM-dd
    val roomsCount: Int,
    val promoCode: String? = null
)

data class GuestDto(
    val id: Long? = null,
    val name: String,
    val gender: String, // MALE, FEMALE
    val age: Int
)

data class BookingDto(
    val id: Long,
    val hotel: HotelDto,
    val room: RoomDto,
    val user: UserDto,
    val checkInDate: String,
    val checkOutDate: String,
    val roomsCount: Int,
    val amount: BigDecimal,
    val bookingStatus: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class PageDto<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int
)

package com.snehil.moon_stays_androidapp.data.remote.dto

import java.math.BigDecimal

data class HotelSearchRequest(
    val city: String?,
    val checkInDate: String?,
    val endDate: String?,
    val roomsCount: Int?,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val roomType: String? = null,
    val amenity: String? = null,
    val page: Int = 0,
    val size: Int = 10
)

data class HotelContactInfo(
    val address: String?,
    val phoneNumber: String?,
    val email: String?,
    val location: String?
)

data class HotelDto(
    val id: Long,
    val name: String,
    val city: String,
    val photos: List<String>?,
    val amenities: List<String>?,
    val contactInfo: HotelContactInfo?,
    val active: Boolean?
)

data class HotelPriceDto(
    val hotel: HotelDto,
    val price: Double
)

data class RoomDto(
    val id: Long,
    val types: String,
    val basePrice: BigDecimal,
    val photos: List<String>?,
    val amenities: List<String>?,
    val totalCount: Int?,
    val capacity: Int?
)

data class HotelInfoDto(
    val hotelDto: HotelDto,
    val rooms: List<RoomDto>
)

data class RoomPriceDto(
    val roomId: Long,
    val pricePerNight: BigDecimal,
    val totalForStay: BigDecimal
)

data class ReviewDto(
    val id: Long?,
    val rating: Int,
    val content: String,
    val hotelId: Long?,
    val userId: Long?
)


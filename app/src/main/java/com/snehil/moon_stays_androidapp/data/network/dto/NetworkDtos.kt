package com.snehil.moon_stays_androidapp.data.network.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ==========================================
// MODULE 1: Legacy Monolith Core DTOs
// ==========================================

data class LegacySignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val roles: List<String>? = null
)

data class LegacyLoginRequest(
    val email: String,
    val password: String
)

data class LegacyLoginResponse(
    val id: Long?,
    val name: String?,
    val email: String?,
    val token: String?,
    val roles: List<String>?
)

data class LegacyContactInfoDto(
    val address: String?,
    val location: String?,
    val phoneNumber: String?,
    val email: String?
)

data class LegacyHotelDto(
    val id: Long? = null,
    val name: String,
    val city: String,
    val photos: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val basePrice: Double? = null,
    val contactInfo: LegacyContactInfoDto? = null,
    val active: Boolean? = null
)

data class LegacyRoomDto(
    val id: Long? = null,
    val types: String,
    val basePrice: Double,
    val photos: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val totalCount: Int,
    val capacity: Int
)

data class LegacySurgeUpdateDto(
    val hotelId: Long,
    val surgeFactor: Double
)

data class LegacyHotelSearchRequest(
    val city: String?,
    val checkInDate: String,
    val endDate: String,
    val roomsCount: Int,
    val roomType: String? = null,
    val amenity: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null
)

data class LegacyHotelPriceDto(
    val hotel: LegacyHotelDto,
    val price: Double
)

data class LegacyBookingRequest(
    val hotelId: Long,
    val roomId: Long,
    val checkInDate: String,
    val checkOutDate: String,
    val roomsCount: Int
)

data class LegacyBookingDto(
    val id: Long?,
    val hotelId: Long?,
    val hotelName: String?,
    val roomType: String?,
    val checkInDate: String?,
    val checkOutDate: String?,
    val totalAmount: Double?,
    val roomsCount: Int?
)

data class LegacyPromoCodeDto(
    val id: Long?,
    val code: String,
    val discountPercentage: Int,
    val active: Boolean?
)

data class LegacyReviewDto(
    val id: Long? = null,
    val hotelId: Long,
    val rating: Int,
    val comment: String,
    val userEmail: String? = null
)

data class LegacyProfileUpdateDto(
    val name: String
)

data class LegacyUserDto(
    val id: Long?,
    val name: String?,
    val email: String?,
    val roles: List<String>?
)

// ==========================================
// MODULE 2: Modern Restructured Core DTOs
// ==========================================

data class ModernRegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class ModernLoginRequest(
    val email: String,
    val password: String
)

data class ModernAuthResponse(
    val token: String,
    val email: String,
    val name: String,
    val roles: List<String>
)

data class ModernPropertyDto(
    val id: Long? = null,
    val title: String,
    val description: String,
    val address: String,
    val city: String,
    val country: String,
    val pricePerNight: BigDecimal,
    val active: Boolean = true
)

data class ModernBookingDto(
    val id: Long?,
    val propertyId: Long?,
    val checkInDate: String,
    val checkOutDate: String,
    val totalPrice: BigDecimal,
    val status: String
)

data class ModernUserMeResponse(
    val id: Long,
    val email: String,
    val name: String,
    val roles: List<String>
)

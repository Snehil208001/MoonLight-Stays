package com.snehil.moon_stays_androidapp.data.remote.dto

import java.math.BigDecimal

data class RegistrationRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String // ROLE_GUEST, ROLE_HOST, ROLE_ADMIN
)

data class ModernLoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val role: String
)

data class ModernUserDto(
    val id: Long?,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val verified: Boolean,
    val createdAt: String?
)

data class ModernPropertyDto(
    val id: Long?,
    val title: String,
    val description: String,
    val pricePerNight: BigDecimal,
    val host: ModernUserDto? = null,
    val approved: Boolean = false
)

data class ModernBookingDto(
    val id: Long?,
    val property: ModernPropertyDto,
    val guest: ModernUserDto? = null,
    val checkInDate: String, // LocalDate (yyyy-MM-dd)
    val checkOutDate: String, // LocalDate (yyyy-MM-dd)
    val totalPrice: BigDecimal,
    val status: String,
    val createdAt: String? = null
)

package com.snehil.moon_stays_androidapp.data.remote.dto

data class LoginDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val accessToken: String
)

data class SignUpRequestDto(
    val email: String,
    val password: String,
    val name: String
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val roles: Set<String>
)

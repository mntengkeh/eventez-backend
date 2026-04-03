package com.veridyl.eventez.dto.auth

data class AuthResponse(
    val user: UserResponse,
    val message: String = "Success"
)

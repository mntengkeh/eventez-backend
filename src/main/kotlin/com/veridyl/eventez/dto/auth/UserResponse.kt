package com.veridyl.eventez.dto.auth

import com.veridyl.eventez.entity.enums.UserRole
import java.time.Instant

data class UserResponse(
    val id: Long,
    val email: String,
    val role: UserRole,
    val fullName: String,
    val phone: String?,
    val createdAt: Instant
)

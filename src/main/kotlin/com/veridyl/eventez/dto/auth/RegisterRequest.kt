package com.veridyl.eventez.dto.auth

import com.veridyl.eventez.entity.enums.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email
    val email: String,

    @field:NotEmpty
    @field:Size(min = 8, max = 100)
    val password: String,

    @field:NotBlank
    val fullName: String,

    val phone: String? = null,
    val role: UserRole
)

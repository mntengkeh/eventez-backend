package com.veridyl.eventez.dto.inquiry

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class CreateInquiryRequest(
    @field:NotNull
    val providerId: Long,
    val eventId: Long? = null,
    @field:NotBlank
    val message: String
)

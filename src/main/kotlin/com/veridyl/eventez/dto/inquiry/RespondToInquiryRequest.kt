package com.veridyl.eventez.dto.inquiry

import jakarta.validation.constraints.NotBlank

data class RespondToInquiryRequest(
    @field:NotBlank
    val response: String
)
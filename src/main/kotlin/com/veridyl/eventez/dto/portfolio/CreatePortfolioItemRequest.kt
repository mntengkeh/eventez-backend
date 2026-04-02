package com.veridyl.eventez.dto.portfolio

import com.veridyl.eventez.entity.enums.MediaType
import jakarta.validation.constraints.NotBlank

data class CreatePortfolioItemRequest(
    @field:NotBlank
    val mediaUrl: String,
    val mediaType: MediaType = MediaType.IMAGE,
    val caption: String? = null,
    val displayOrder: Int = 0
)
package com.veridyl.eventez.dto.portfolio

import com.veridyl.eventez.entity.enums.MediaType
import java.time.Instant

data class PortfolioItemResponse(
    val id: Long,
    val mediaUrl: String,
    val mediaType: MediaType,
    val caption: String?,
    val displayOrder: Int,
    val createdAt: Instant
)
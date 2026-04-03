package com.veridyl.eventez.dto.inquiry

import com.veridyl.eventez.entity.enums.InquiryStatus
import java.time.Instant

data class InquiryResponse(
    val id: Long,
    val plannerId: Long,
    val plannerName: String,
    val providerId: Long,
    val providerBusinessName: String,
    val eventId: Long?,
    val message: String,
    val status: InquiryStatus,
    val response: String?,
    val respondedAt: Instant?,
    val createdAt: Instant
)

package com.veridyl.eventez.dto.availability

import com.veridyl.eventez.entity.enums.AvailabilityStatus
import java.time.LocalDate

data class AvailabilityResponse(
    val id: Long,
    val date: LocalDate,
    val status: AvailabilityStatus,
    val note: String?
)
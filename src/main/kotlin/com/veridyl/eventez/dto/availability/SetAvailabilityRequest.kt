package com.veridyl.eventez.dto.availability

import com.veridyl.eventez.entity.enums.AvailabilityStatus
import org.jetbrains.annotations.NotNull
import java.time.LocalDate

data class SetAvailabilityRequest(
    @field:NotNull
    val date: LocalDate,
    @field:NotNull
    val status: AvailabilityStatus,
    val note: String? = null
)

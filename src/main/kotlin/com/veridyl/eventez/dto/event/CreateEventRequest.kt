package com.veridyl.eventez.dto.event

import com.veridyl.eventez.entity.enums.EventType
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class CreateEventRequest(
    @field:NotBlank
    val title: String,
    @field:NotNull
    val eventType: EventType,
    @field:NotNull
    @field:FutureOrPresent
    val eventDate: LocalDate,
    val location: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val guestCount: Int? = null,
    val description: String? = null,
    val requiredCategoryIds: List<Long> = emptyList()
)

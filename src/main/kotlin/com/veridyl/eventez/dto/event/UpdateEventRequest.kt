package com.veridyl.eventez.dto.event

import com.veridyl.eventez.entity.enums.EventStatus
import com.veridyl.eventez.entity.enums.EventType
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateEventRequest(
    val title: String? = null,
    val eventType: EventType? = null,
    val eventDate: LocalDate? = null,
    val location: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val guestCount: Int? = null,
    val description: String? = null,
    val status: EventStatus? = null,
    val requiredCategoryIds: List<Long>? = null
)

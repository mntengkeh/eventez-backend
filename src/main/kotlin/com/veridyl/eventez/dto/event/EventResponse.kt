package com.veridyl.eventez.dto.event

import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.entity.enums.EventStatus
import com.veridyl.eventez.entity.enums.EventType
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class EventResponse(
    val id: Long,
    val plannerId: Long,
    val title: String,
    val eventType: EventType,
    val eventDate: LocalDate,
    val location: String?,
    val city: String?,
    val state: String?,
    val budgetMin: BigDecimal?,
    val budgetMax: BigDecimal?,
    val guestCount: Int?,
    val description: String?,
    val status: EventStatus,
    val requiredCategories: List<ServiceCategoryResponse>,
    val createdAt: Instant
)

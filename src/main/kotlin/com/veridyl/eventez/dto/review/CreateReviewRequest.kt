package com.veridyl.eventez.dto.review

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull

data class CreateReviewRequest(
    @field:NotNull
    val providerId: Long,
    val eventId: Long? = null,
    @field:Min(1) @field:Max(5)
    val rating: Short,
    val title: String? = null,
    val comment: String? = null
)
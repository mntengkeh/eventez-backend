package com.veridyl.eventez.dto.review

import java.time.Instant

data class ReviewResponse(
    val id: Long,
    val plannerId: Long,
    val plannerName: String,
    val providerId: Long,
    val eventId: Long?,
    val rating: Short,
    val title: String?,
    val comment: String?,
    val createdAt: Instant
)

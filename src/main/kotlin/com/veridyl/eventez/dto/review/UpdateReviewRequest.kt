package com.veridyl.eventez.dto.review

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class UpdateReviewRequest(
    @field:Min(1) @field:Max(5)
    val rating: Short? = null,
    val title: String? = null,
    val comment: String? = null
)
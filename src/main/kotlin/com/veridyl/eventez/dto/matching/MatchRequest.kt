package com.veridyl.eventez.dto.matching

import java.math.BigDecimal
import java.time.LocalDate

data class MatchRequest(
    val eventId: Long?,                       // use existing event data, OR supply inline:
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val categoryIds: List<Long> = emptyList(),
    val eventDate: LocalDate? = null,
    val minRating: Double? = null,
    val sortBy: MatchSortBy = MatchSortBy.RELEVANCE
)

enum class MatchSortBy { RELEVANCE, PRICE_LOW, PRICE_HIGH, RATING, DISTANCE }

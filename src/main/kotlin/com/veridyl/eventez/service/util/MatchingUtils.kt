package com.veridyl.eventez.service.util

import com.veridyl.eventez.dto.matching.MatchSortBy
import com.veridyl.eventez.entity.ProviderProfile
import com.veridyl.eventez.entity.Service
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.*

// Internal data structures

internal data class ResolvedMatchParams(
    val latitude:    Double?,
    val longitude:   Double?,
    val budgetMin:   BigDecimal?,
    val budgetMax:   BigDecimal?,
    val categoryIds: List<Long>,
    val eventDate:   LocalDate?,
    val minRating:   Double?,
    val sortBy:      MatchSortBy
)

internal data class ScoredCandidate(
    val service:    Service,
    val provider:   ProviderProfile,
    val distanceKm: Double?,
    val score:      Double
)

// Scoring constants

internal object ScoringWeights {
    const val DISTANCE      = 30.0
    const val PRICE_FIT     = 25.0
    const val RATING        = 25.0
    const val RESPONSE_RATE = 10.0
    const val REVIEW_VOLUME = 10.0
}

// Scoring

internal fun calculateRelevanceScore(
    provider:   ProviderProfile,
    service:    Service,
    distanceKm: Double?,
    resolved:   ResolvedMatchParams
): Double {
    val distanceScore   = scoreDistance(distanceKm, provider.serviceRadius)
    val priceFitScore   = scorePriceFit(service, resolved.budgetMin, resolved.budgetMax)
    val ratingScore     = ScoringWeights.RATING * (provider.avgRating / 5.0)
    val responseScore   = ScoringWeights.RESPONSE_RATE * (provider.responseRate / 100.0)
    val reviewVolScore  = min(ScoringWeights.REVIEW_VOLUME, provider.reviewCount.toDouble())

    return distanceScore + priceFitScore + ratingScore + responseScore + reviewVolScore
}

private fun scoreDistance(distanceKm: Double?, serviceRadius: Int): Double {
    distanceKm ?: return ScoringWeights.DISTANCE / 2.0   // no location → neutral half-credit
    val ratio = distanceKm / serviceRadius.toDouble()
    return ScoringWeights.DISTANCE * (1.0 - ratio).coerceAtLeast(0.0)
}

private fun scorePriceFit(
    service:   Service,
    budgetMin: BigDecimal?,
    budgetMax: BigDecimal?
): Double {
    if (budgetMin == null || budgetMax == null ||
        service.priceMin == null || service.priceMax == null
    ) return ScoringWeights.PRICE_FIT / 2.0              // missing data → neutral half-credit

    val budgetMid   = (budgetMin + budgetMax).toDouble() / 2.0
    val priceMid    = (service.priceMin!! + service.priceMax!!).toDouble() / 2.0
    val budgetRange = (budgetMax - budgetMin).toDouble()

    if (budgetRange <= 0.0) return ScoringWeights.PRICE_FIT / 2.0

    val deviation = abs(priceMid - budgetMid) / budgetRange
    return ScoringWeights.PRICE_FIT * (1.0 - deviation).coerceAtLeast(0.0)
}

// Sorting

internal fun List<ScoredCandidate>.sortedBy(sortBy: MatchSortBy): List<ScoredCandidate> =
    when (sortBy) {
        MatchSortBy.RELEVANCE  -> sortedByDescending { it.score }
        MatchSortBy.PRICE_LOW  -> sortedBy { it.service.priceMin ?: BigDecimal.ZERO }
        MatchSortBy.PRICE_HIGH -> sortedByDescending { it.service.priceMax ?: BigDecimal.ZERO }
        MatchSortBy.RATING     -> sortedByDescending { it.provider.avgRating }
        MatchSortBy.DISTANCE   -> sortedBy { it.distanceKm ?: Double.MAX_VALUE }
    }

// Distance

//  Haversine formula — returns great-circle distance in kilometres.

internal fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}

internal fun resolveDistance(resolved: ResolvedMatchParams, provider: ProviderProfile): Double? {
    val lat = resolved.latitude   ?: return null
    val lng = resolved.longitude  ?: return null
    val pLat = provider.latitude  ?: return null
    val pLng = provider.longitude ?: return null
    return calculateDistance(lat, lng, pLat, pLng)
}

internal fun withinRadius(distanceKm: Double?, serviceRadius: Int): Boolean =
    distanceKm == null || distanceKm <= serviceRadius

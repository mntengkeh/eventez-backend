package com.veridyl.eventez.dto.matching

import com.veridyl.eventez.dto.provider.ProviderSummaryResponse
import com.veridyl.eventez.dto.service.ServiceResponse

data class MatchedProviderResponse(
    val provider: ProviderSummaryResponse,
    val service: ServiceResponse,
    val distanceKm: Double?,        // null if location not provided
    val relevanceScore: Double      // 0-100
)

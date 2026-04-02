package com.veridyl.eventez.dto.provider

data class ProviderSummaryResponse(
    val id: Long,
    val businessName: String,
    val city: String?,
    val state: String?,
    val avgRating: Double,
    val reviewCount: Int,
    val verified: Boolean,
    val thumbnailUrl: String?   // first portfolio image
)
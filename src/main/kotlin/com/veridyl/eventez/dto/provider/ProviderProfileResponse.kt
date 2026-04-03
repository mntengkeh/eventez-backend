package com.veridyl.eventez.dto.provider

import java.time.Instant

data class ProviderProfileResponse(
    val id: Long,
    val userId: Long,
    val businessName: String,
    val description: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val serviceRadius: Int,
    val website: String?,
    val verified: Boolean,
    val avgRating: Double,
    val reviewCount: Int,
    val responseRate: Double,
    val createdAt: Instant
)

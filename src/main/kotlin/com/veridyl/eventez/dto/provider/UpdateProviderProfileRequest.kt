package com.veridyl.eventez.dto.provider

data class UpdateProviderProfileRequest(
    val businessName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceRadius: Int? = null,
    val website: String? = null
)

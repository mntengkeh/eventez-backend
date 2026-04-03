package com.veridyl.eventez.dto.provider

import jakarta.validation.constraints.NotBlank

data class CreateProviderProfileRequest(
    @field:NotBlank
    val businessName: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceRadius: Int = 50,
    val website: String? = null
)

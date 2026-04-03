package com.veridyl.eventez.dto.service

import com.veridyl.eventez.entity.enums.PriceType
import java.math.BigDecimal

data class ServiceResponse(
    val id: Long,
    val providerId: Long,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val description: String?,
    val priceMin: BigDecimal?,
    val priceMax: BigDecimal?,
    val priceType: PriceType,
    val active: Boolean
)

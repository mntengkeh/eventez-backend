package com.veridyl.eventez.dto.service

import com.veridyl.eventez.entity.enums.PriceType
import java.math.BigDecimal

data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val categoryId: Long? = null,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
    val priceType: PriceType? = null,
    val active: Boolean? = null
)
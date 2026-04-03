package com.veridyl.eventez.dto.service

import com.veridyl.eventez.entity.enums.PriceType
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal

data class CreateServiceRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotNull
    val categoryId: Long,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
    val priceType: PriceType = PriceType.FIXED
)

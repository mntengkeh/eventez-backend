package com.veridyl.eventez.dto.matching

import com.veridyl.eventez.dto.service.ServiceCategoryResponse

data class CategoryMatchResult(
    val category: ServiceCategoryResponse,
    val providers: List<MatchedProviderResponse>
)
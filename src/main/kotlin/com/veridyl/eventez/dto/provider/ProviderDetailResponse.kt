package com.veridyl.eventez.dto.provider

import com.veridyl.eventez.dto.portfolio.PortfolioItemResponse
import com.veridyl.eventez.dto.review.ReviewResponse
import com.veridyl.eventez.dto.service.ServiceResponse

data class ProviderDetailResponse(
    val profile: ProviderProfileResponse,
    val services: List<ServiceResponse>,
    val portfolio: List<PortfolioItemResponse>,
    val reviews: List<ReviewResponse>
)
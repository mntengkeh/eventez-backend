package com.veridyl.eventez.dto.bookmark

import com.veridyl.eventez.dto.provider.ProviderSummaryResponse
import java.time.Instant

data class BookmarkResponse(
    val id: Long,
    val provider: ProviderSummaryResponse,
    val createdAt: Instant
)

package com.veridyl.eventez.dto.availability

data class BulkSetAvailabilityRequest(
    val entries: List<SetAvailabilityRequest>
)

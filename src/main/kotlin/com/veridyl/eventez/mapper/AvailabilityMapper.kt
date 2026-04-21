package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.availability.AvailabilityResponse
import com.veridyl.eventez.dto.availability.SetAvailabilityRequest
import com.veridyl.eventez.entity.Availability
import com.veridyl.eventez.entity.ProviderProfile
import org.mapstruct.*

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface AvailabilityMapper {

    fun toResponse(entity: Availability): AvailabilityResponse

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "date", source = "request.date")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "note", source = "request.note")
    fun toEntity(request: SetAvailabilityRequest, provider: ProviderProfile): Availability

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "date", ignore = true)
    fun updateEntity(request: SetAvailabilityRequest, @MappingTarget entity: Availability)
}

package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.event.CreateEventRequest
import com.veridyl.eventez.dto.event.EventResponse
import com.veridyl.eventez.dto.event.UpdateEventRequest
import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.entity.AppUser
import com.veridyl.eventez.entity.Event
import com.veridyl.eventez.entity.EventServiceRequirement
import org.mapstruct.*

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface EventMapper {

    @Mapping(target = "plannerId", source = "planner.id")
    @Mapping(target = "requiredCategories", source = "serviceRequirements")
    fun toResponse(entity: Event): EventResponse

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planner", source = "planner")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "serviceRequirements", ignore = true)
    fun toEntity(request: CreateEventRequest, planner: AppUser): Event

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "serviceRequirements", ignore = true)
    fun updateEntity(request: UpdateEventRequest, @MappingTarget entity: Event)

    /**
     * Maps the requirement entity to the category response for the EventResponse DTO.
     */
    fun mapRequirementToCategoryResponse(requirement: EventServiceRequirement): ServiceCategoryResponse {
        val cat = requirement.category
        return ServiceCategoryResponse(cat.id, cat.name, cat.slug, cat.icon, cat.description)
    }
}

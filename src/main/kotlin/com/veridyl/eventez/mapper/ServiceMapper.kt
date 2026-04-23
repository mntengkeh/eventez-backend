package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.service.ServiceResponse
import com.veridyl.eventez.entity.Service
import org.mapstruct.*

@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface ServiceMapper {

    @Mapping(source = "provider.id", target = "providerId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    fun toResponse(service: Service): ServiceResponse

    fun toResponseList(services: List<Service>): List<ServiceResponse>
}

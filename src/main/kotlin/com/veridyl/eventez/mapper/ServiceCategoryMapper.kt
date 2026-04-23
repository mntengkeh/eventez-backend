package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.entity.ServiceCategory
import org.mapstruct.Mapper

@Mapper
interface ServiceCategoryMapper {
    fun toResponse(category: ServiceCategory): ServiceCategoryResponse
}
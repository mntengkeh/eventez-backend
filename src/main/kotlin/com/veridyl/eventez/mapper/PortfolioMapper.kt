package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.portfolio.PortfolioItemResponse
import com.veridyl.eventez.entity.PortfolioItem
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface PortfolioMapper {
    fun toResponse(entity: PortfolioItem): PortfolioItemResponse
    fun toResponseList(entities: List<PortfolioItem>): List<PortfolioItemResponse>
}

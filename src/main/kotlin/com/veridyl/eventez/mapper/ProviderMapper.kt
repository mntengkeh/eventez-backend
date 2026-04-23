package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.provider.ProviderSummaryResponse
import com.veridyl.eventez.entity.PortfolioItem
import com.veridyl.eventez.entity.ProviderProfile
import com.veridyl.eventez.repository.PortfolioItemRepository
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.mapstruct.ReportingPolicy

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface ProviderMapper {

    @Mapping(source = "id", target = "thumbnailUrl", qualifiedByName = ["resolveThumbnail"])
    fun toSummaryResponse(provider: ProviderProfile, @Context portfolioItemRepository: PortfolioItemRepository): ProviderSummaryResponse

    @Named("resolveThumbnail")
    fun resolveThumbnail(id: Long, @Context portfolioItemRepository: PortfolioItemRepository): String? {
        val portfolioItems = portfolioItemRepository.findByProviderIdOrderByDisplayOrderAsc(providerId = id)
        return portfolioItems
            .filter { it.mediaType.name == "IMAGE" }
            .minByOrNull { it.displayOrder }
            ?.mediaUrl
    }

}

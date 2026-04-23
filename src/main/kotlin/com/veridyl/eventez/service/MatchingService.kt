package com.veridyl.eventez.service

import com.veridyl.eventez.dto.matching.*
import com.veridyl.eventez.entity.AppUser
import com.veridyl.eventez.entity.Service
import com.veridyl.eventez.entity.ServiceCategory
import com.veridyl.eventez.entity.enums.UserRole
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.mapper.ProviderMapper
import com.veridyl.eventez.mapper.ServiceCategoryMapper
import com.veridyl.eventez.mapper.ServiceMapper
import com.veridyl.eventez.repository.EventRepository
import com.veridyl.eventez.repository.EventServiceRequirementRepository
import com.veridyl.eventez.repository.PortfolioItemRepository
import com.veridyl.eventez.repository.ServiceCategoryRepository
import com.veridyl.eventez.repository.ServiceRepository
import com.veridyl.eventez.service.util.ResolvedMatchParams
import com.veridyl.eventez.service.util.ScoredCandidate
import com.veridyl.eventez.service.util.calculateRelevanceScore
import com.veridyl.eventez.service.util.resolveDistance
import com.veridyl.eventez.service.util.sortedBy
import com.veridyl.eventez.service.util.withinRadius
import com.veridyl.eventez.specification.ServiceSpecification
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service as SpringService
import java.time.LocalDate

@SpringService
class MatchingService(
    private val eventRepository: EventRepository,
    private val serviceRepository: ServiceRepository,
    private val serviceCategoryRepository: ServiceCategoryRepository,
    private val availabilityService: AvailabilityService,       // assumed implemented
    private val serviceMapper: ServiceMapper,
    private val providerMapper: ProviderMapper,
    private val serviceCategoryMapper: ServiceCategoryMapper,
    private val authService: AuthService,
    private val eventServiceRequirementRepository: EventServiceRequirementRepository,
    private val portfolioItemRepository: PortfolioItemRepository
) {

    fun findMatches(request: MatchRequest): MatchResponse {
        val currentUser: AppUser = authService.getAuthenticatedAppUser()

        if (currentUser.role != UserRole.PLANNER) {
            throw AccessDeniedException("Only PLANNERs can initiate matches")
        }

        if (request.eventId != null) {
            val event = eventRepository.findById(request.eventId).orElseThrow {
                ResourceNotFoundException("Event ID ${request.eventId} not found.")
            }
            if (event.planner.id != currentUser.id) {
                throw AccessDeniedException("You do not own event ${request.eventId}.")
            }
        }

        val resolved = resolveSearchParameters(request)

        if (resolved.categoryIds.isEmpty()) return MatchResponse(categories = emptyList())

        val categories = serviceCategoryRepository.findAllById(resolved.categoryIds).toList()
        if (categories.isEmpty()) return MatchResponse(categories = emptyList())

        val categoryResults = categories.map { category ->
            CategoryMatchResult(
                category  = serviceCategoryMapper.toResponse(category),
                providers = findAndScoreForCategory(category, resolved)
            )
        }

        return MatchResponse(categories = categoryResults)
    }

    //  Input resolution

    private fun resolveSearchParameters(request: MatchRequest): ResolvedMatchParams {
        if (request.eventId == null) {
            return ResolvedMatchParams(
                latitude    = request.latitude,
                longitude   = request.longitude,
                budgetMin   = request.budgetMin,
                budgetMax   = request.budgetMax,
                categoryIds = request.categoryIds,
                eventDate   = request.eventDate,
                minRating   = request.minRating,
                sortBy      = request.sortBy
            )
        }

        val event = eventRepository.findById(request.eventId)
            .orElseThrow { ResourceNotFoundException("Event not found: ${request.eventId}") }

        val eventCategoryIds = eventServiceRequirementRepository.findByEventId(request.eventId).map { it.category.id }

        // Inline request fields take precedence over event-sourced values
        return ResolvedMatchParams(
            latitude    = request.latitude  ?: event.latitude,
            longitude   = request.longitude ?: event.longitude,
            budgetMin   = request.budgetMin ?: event.budgetMin,
            budgetMax   = request.budgetMax ?: event.budgetMax,
            categoryIds = request.categoryIds.ifEmpty { eventCategoryIds },
            eventDate   = request.eventDate ?: event.eventDate,
            minRating   = request.minRating,
            sortBy      = request.sortBy
        )
    }

    // Query, filter, score per category

    private fun findAndScoreForCategory(
        category: ServiceCategory,
        resolved: ResolvedMatchParams
    ): List<MatchedProviderResponse> {

        val services = serviceRepository.findAll(buildServiceSpec(category.id, resolved))

        val candidates = services.mapNotNull { service ->
            val provider   = service.provider
            val distanceKm = resolveDistance(resolved, provider)

            if (!withinRadius(distanceKm, provider.serviceRadius))           return@mapNotNull null
            if (!passesRatingFilter(provider.avgRating, resolved.minRating)) return@mapNotNull null
            if (!passesAvailabilityFilter(provider.id, resolved.eventDate))  return@mapNotNull null

            ScoredCandidate(
                service = service,
                provider = provider,
                distanceKm = distanceKm,
                score = calculateRelevanceScore(provider, service, distanceKm, resolved)
            )
        }

        return candidates
            .sortedBy(resolved.sortBy)
            .map { candidate ->
                MatchedProviderResponse(
                    provider       = providerMapper.toSummaryResponse(candidate.provider, portfolioItemRepository),
                    service        = serviceMapper.toResponse(candidate.service),
                    distanceKm     = candidate.distanceKm,
                    relevanceScore = candidate.score
                )
            }
    }

    //  Helpers

    private fun buildServiceSpec(categoryId: Long, resolved: ResolvedMatchParams): Specification<Service> {
        var spec: Specification<Service> = ServiceSpecification.categoryId(categoryId)
            .and(ServiceSpecification.active())

        if (resolved.budgetMin != null && resolved.budgetMax != null)
            spec = spec.and(ServiceSpecification.priceBetween(resolved.budgetMin, resolved.budgetMax))

        return spec
    }

    private fun passesRatingFilter(avgRating: Double, minRating: Double?): Boolean =
        minRating == null || avgRating >= minRating

    private fun passesAvailabilityFilter(providerId: Long, eventDate: LocalDate?): Boolean =
        eventDate == null || availabilityService.isAvailable(providerId, eventDate)
}

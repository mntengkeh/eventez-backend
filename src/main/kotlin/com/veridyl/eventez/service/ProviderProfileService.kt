package com.veridyl.eventez.service

import com.veridyl.eventez.dto.provider.*
import com.veridyl.eventez.dto.service.ServiceResponse
import com.veridyl.eventez.dto.portfolio.PortfolioItemResponse
import com.veridyl.eventez.dto.review.ReviewResponse
import com.veridyl.eventez.dto.common.PagedResponse
import com.veridyl.eventez.entity.ProviderProfile
import com.veridyl.eventez.entity.enums.UserRole
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.DuplicateResourceException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.repository.ProviderProfileRepository
import com.veridyl.eventez.repository.ServiceRepository
import com.veridyl.eventez.repository.PortfolioItemRepository
import com.veridyl.eventez.repository.ReviewRepository
import com.veridyl.eventez.specification.ProviderSpecification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class ProviderProfileService(
    private val providerProfileRepository: ProviderProfileRepository,
    private val serviceRepository: ServiceRepository,
    private val portfolioItemRepository: PortfolioItemRepository,
    private val reviewRepository: ReviewRepository,
    private val authService: AuthService
) {

    // ------------------------------
    // CREATE profile
    // -----------------------------------------------------------------------

    fun createProfile(request: CreateProviderProfileRequest): ProviderProfileResponse {
        val currentUser = authService.getAuthenticatedAppUser()

        if (currentUser.role != UserRole.PROVIDER) {
            throw AccessDeniedException("Only PROVIDER users can create a provider profile.")
        }
        if (providerProfileRepository.findByUserId(currentUser.id) != null) {
            throw DuplicateResourceException("A provider profile already exists for this account.")
        }

        val profile = ProviderProfile(
            user          = currentUser,
            businessName  = request.businessName,
            description   = request.description,
            address       = request.address,
            city          = request.city,
            state         = request.state,
            zipCode       = request.zipCode,
            latitude      = request.latitude,
            longitude     = request.longitude,
            serviceRadius = request.serviceRadius,
            website       = request.website
        )

        return providerProfileRepository.save(profile).toProfileResponse()
    }

    // --------------------------------------
    // UPDATE profile
    // --------------------------------------

    fun updateProfile(profileId: Long, request: UpdateProviderProfileRequest): ProviderProfileResponse {
        val currentUser = authService.getAuthenticatedAppUser()
        val profile = findProfileOrThrow(profileId)

        if (profile.user.id != currentUser.id) {
            throw AccessDeniedException("You do not own this provider profile.")
        }

        request.businessName?.let  { profile.businessName  = it }
        request.description?.let   { profile.description   = it }
        request.address?.let       { profile.address       = it }
        request.city?.let          { profile.city          = it }
        request.state?.let         { profile.state         = it }
        request.zipCode?.let       { profile.zipCode       = it }
        request.latitude?.let      { profile.latitude      = it }
        request.longitude?.let     { profile.longitude     = it }
        request.serviceRadius?.let { profile.serviceRadius = it }
        request.website?.let       { profile.website       = it }
        profile.updatedAt = Instant.now()

        return providerProfileRepository.save(profile).toProfileResponse()
    }

    // ----------------------------------------
    // the profile info for a provider
    // -----------------------------------------

    @Transactional(readOnly = true)
    fun getProfile(profileId: Long): ProviderDetailResponse {
        val profile   = findProfileOrThrow(profileId)
        val services  = serviceRepository.findByProviderIdAndActiveTrue(profileId)
            .map { it.toServiceResponse() }
        val portfolio = portfolioItemRepository.findByProviderIdOrderByDisplayOrderAsc(profileId)
            .map { it.toPortfolioItemResponse() }
        val reviews   = reviewRepository.findByProviderId(profileId)
            .map { it.toReviewResponse() }

        return ProviderDetailResponse(
            profile   = profile.toProfileResponse(),
            services  = services,
            portfolio = portfolio,
            reviews   = reviews
        )
    }

    // -------------------------------------
    // get profile bt id
    // ------------------------------------

    @Transactional(readOnly = true)
    fun getProfileByUserId(userId: Long): ProviderProfileResponse {
        val profile = providerProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("No provider profile found for user $userId")
        return profile.toProfileResponse()
    }

    // -----------------------------------
    // SEARCH providers
    // -----------------------------------
    @Transactional(readOnly = true)
    fun searchProviders(
        city: String?,
        state: String?,
        minRating: Double?,
        verified: Boolean?,
        search: String?,
        pageable: Pageable
    ): PagedResponse<ProviderSummaryResponse> {
        val specs = mutableListOf<Specification<ProviderProfile>>()

        if (city != null)      specs.add(ProviderSpecification.inCity(city))
        if (state != null)     specs.add(ProviderSpecification.inState(state))
        if (minRating != null) specs.add(ProviderSpecification.minRating(minRating))
        if (verified == true)  specs.add(ProviderSpecification.isVerified())
        if (search != null)    specs.add(ProviderSpecification.businessNameContains(search))

        val spec = specs.reduceOrNull { acc, s -> acc.and(s) }

        val page = if (spec != null)
            providerProfileRepository.findAll(spec, pageable)
        else
            providerProfileRepository.findAll(pageable)

        val content = page.content.map { it.toSummaryResponse() }

        return PagedResponse(
            content       = content,
            page          = page.number,
            size          = page.size,
            totalElements = page.totalElements,
            totalPages    = page.totalPages,
            last          = page.isLast
        )
    }

    // ---------------------------------------------
    // DELETE profile as provider
    // -----------------------------------------------

    fun deleteProfile(profileId: Long) {
        val currentUser = authService.getAuthenticatedAppUser()
        val profile     = findProfileOrThrow(profileId)

        if (profile.user.id != currentUser.id) {
            throw AccessDeniedException("You do not own this provider profile.")
        }

        providerProfileRepository.delete(profile)
    }

    // -----------------------------------------
    // helpers methods
    // -------------------------------------


    fun findProfileForCurrentUser(): ProviderProfile {
        val currentUser = authService.getAuthenticatedAppUser()
        return providerProfileRepository.findByUserId(currentUser.id)
            ?: throw ResourceNotFoundException("No provider profile found for current user.")
    }

    fun findProfileOrThrow(profileId: Long): ProviderProfile =
        providerProfileRepository.findById(profileId)
            .orElseThrow { ResourceNotFoundException("Provider profile not found: $profileId") }

    // -------------------------------------
    // mapping extensions
    // -----------------------------------------

    private fun ProviderProfile.toProfileResponse() = ProviderProfileResponse(
        id            = id,
        userId        = user.id,
        businessName  = businessName,
        description   = description,
        address       = address,
        city          = city,
        state         = state,
        zipCode       = zipCode,
        latitude      = latitude,
        longitude     = longitude,
        serviceRadius = serviceRadius,
        website       = website,
        verified      = verified,
        avgRating     = avgRating,
        reviewCount   = reviewCount,
        responseRate  = responseRate,
        createdAt     = createdAt
    )

    private fun ProviderProfile.toSummaryResponse(): ProviderSummaryResponse {
        val thumbnail = portfolioItemRepository
            .findByProviderIdOrderByDisplayOrderAsc(id)
            .firstOrNull()?.mediaUrl
        return ProviderSummaryResponse(
            id           = id,
            businessName = businessName,
            city         = city,
            state        = state,
            avgRating    = avgRating,
            reviewCount  = reviewCount,
            verified     = verified,
            thumbnailUrl = thumbnail
        )
    }


    private fun com.veridyl.eventez.entity.Service.toServiceResponse() = ServiceResponse(
        id           = id,
        providerId   = provider.id,
        categoryId   = category.id,
        categoryName = category.name,
        name         = name,
        description  = description,
        priceMin     = priceMin,
        priceMax     = priceMax,
        priceType    = priceType,
        active       = active
    )

    private fun com.veridyl.eventez.entity.PortfolioItem.toPortfolioItemResponse() = PortfolioItemResponse(
        id           = id,
        mediaUrl     = mediaUrl,
        mediaType    = mediaType,
        caption      = caption,
        displayOrder = displayOrder,
        createdAt    = createdAt
    )

    private fun com.veridyl.eventez.entity.Review.toReviewResponse() = ReviewResponse(
        id           = id,
        plannerId    = planner.id,
        plannerName  = planner.fullName,
        providerId   = provider.id,
        eventId      = event?.id,
        rating       = rating,
        title        = title,
        comment      = comment,
        createdAt    = createdAt
    )
}
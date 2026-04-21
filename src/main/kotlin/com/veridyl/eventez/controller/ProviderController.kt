package com.veridyl.eventez.controller

import com.veridyl.eventez.dto.availability.AvailabilityResponse
import com.veridyl.eventez.dto.availability.BulkSetAvailabilityRequest
import com.veridyl.eventez.dto.availability.SetAvailabilityRequest
import com.veridyl.eventez.dto.common.PagedResponse
import com.veridyl.eventez.dto.portfolio.CreatePortfolioItemRequest
import com.veridyl.eventez.dto.portfolio.PortfolioItemResponse
import com.veridyl.eventez.dto.provider.*
import com.veridyl.eventez.dto.review.ReviewResponse
import com.veridyl.eventez.dto.service.CreateServiceRequest
import com.veridyl.eventez.dto.service.ServiceResponse
import com.veridyl.eventez.dto.service.UpdateServiceRequest
//import com.veridyl.eventez.service.AvailabilityService
//import com.veridyl.eventez.service.PortfolioService
import com.veridyl.eventez.service.ProviderProfileService
//import com.veridyl.eventez.service.ReviewService
import com.veridyl.eventez.service.ServiceService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/v1/providers")
class ProviderController(
    private val providerProfileService: ProviderProfileService,
    private val serviceService: ServiceService,
//    private val portfolioService: PortfolioService,
//    private val availabilityService: AvailabilityService,
//    private val reviewService: ReviewService
) {

    // ======================
    // PROFILE ENDPOINTS
    // =====================


//     * Create the provider profile for the currently authenticated provider user.

    @PostMapping("/profile")
    fun createProfile(
        @Valid @RequestBody request: CreateProviderProfileRequest
    ): ResponseEntity<ProviderProfileResponse> {
        val response = providerProfileService.createProfile(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }



//      Updates the currently authenticated provider's own profile.

    @PutMapping("/profile")
    fun updateMyProfile(
        @Valid @RequestBody request: UpdateProviderProfileRequest
    ): ResponseEntity<ProviderProfileResponse> {
        // Fetch the current user's profile id via the service
        val currentUser = providerProfileService.findProfileForCurrentUser()
        val response = providerProfileService.updateProfile(currentUser.id, request)
        return ResponseEntity.ok(response)
    }

//     Returns detailed information about a specific provider.

    @GetMapping("/{id}")
    fun getProvider(@PathVariable id: Long): ResponseEntity<ProviderDetailResponse> =
        ResponseEntity.ok(providerProfileService.getProfile(id))


//      Search  providers with optional filters.

    @GetMapping
    fun searchProviders(
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) minRating: Double?,
        @RequestParam(required = false) verified: Boolean?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "avgRating,desc") sort: String
    ): ResponseEntity<PagedResponse<ProviderSummaryResponse>> {
        val parts    = sort.split(",")
        val property = parts[0]
        val direction = if (parts.getOrNull(1)?.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(direction, property))

        val result = providerProfileService.searchProviders(
            city      = city,
            state     = state,
            minRating = minRating,
            verified  = verified,
            search    = search,
            pageable  = pageable
        )
        return ResponseEntity.ok(result)
    }

//    allows the provider to delete his profile
    @DeleteMapping("/{id}")
    fun deleteProfile(@PathVariable id: Long): ResponseEntity<Void> {
        providerProfileService.deleteProfile(id)
        return ResponseEntity.noContent().build()
    }

    // ==================
    // SERVICE ENDPOINTS
    // ======================


//      adds a new service to the provider profile.

    @PostMapping("/{providerId}/services")
    fun createService(
        @PathVariable providerId: Long,
        @Valid @RequestBody request: CreateServiceRequest
    ): ResponseEntity<ServiceResponse> {
        val response = serviceService.createService(providerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }


//     gets all active services for a provider.

    @GetMapping("/{providerId}/services")
    fun getProviderServices(@PathVariable providerId: Long): ResponseEntity<List<ServiceResponse>> =
        ResponseEntity.ok(serviceService.getServicesByProvider(providerId))


//     * updates a service for a provider.

    @PutMapping("/services/{serviceId}")
    fun updateService(
        @PathVariable serviceId: Long,
        @Valid @RequestBody request: UpdateServiceRequest
    ): ResponseEntity<ServiceResponse> =
        ResponseEntity.ok(serviceService.updateService(serviceId, request))


//     deletes a service the provider.

    @DeleteMapping("/services/{serviceId}")
    fun deleteService(@PathVariable serviceId: Long): ResponseEntity<Void> {
        serviceService.deleteService(serviceId)
        return ResponseEntity.noContent().build()
    }


}

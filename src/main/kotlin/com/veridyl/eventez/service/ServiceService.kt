package com.veridyl.eventez.service

import com.veridyl.eventez.dto.service.CreateServiceRequest
import com.veridyl.eventez.dto.service.ServiceResponse
import com.veridyl.eventez.dto.service.UpdateServiceRequest
import com.veridyl.eventez.entity.Service as ServiceEntity
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.repository.ServiceCategoryRepository
import com.veridyl.eventez.repository.ServiceRepository
import com.veridyl.eventez.specification.ServiceSpecification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service
@Transactional
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val serviceCategoryRepository: ServiceCategoryRepository,
    private val providerProfileService: ProviderProfileService,
    private val authService: AuthService
) {


    // CREATE

    fun createService(providerId: Long, request: CreateServiceRequest): ServiceResponse {
        val currentUser = authService.getAuthenticatedUser()
        val profile     = providerProfileService.findProfileOrThrow(providerId)

        if (profile.user.id != currentUser.id) {
            throw AccessDeniedException("You do not own this provider profile.")
        }

        val category = serviceCategoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Service category not found: ${request.categoryId}") }

        validatePriceRange(request.priceMin, request.priceMax)

        val service = ServiceEntity(
            provider    = profile,
            category    = category,
            name        = request.name,
            description = request.description,
            priceMin    = request.priceMin,
            priceMax    = request.priceMax,
            priceType   = request.priceType
        )

        return serviceRepository.save(service).toServiceResponse()
    }


    // UPDATE service


    fun updateService(serviceId: Long, request: UpdateServiceRequest): ServiceResponse {
        val currentUser = authService.getAuthenticatedUser()
        val service     = findServiceOrThrow(serviceId)

        if (service.provider.user.id != currentUser.id) {
            throw AccessDeniedException("You do not own this service.")
        }

        request.name?.let        { service.name        = it }
        request.description?.let { service.description = it }
        request.priceType?.let   { service.priceType   = it }
        request.active?.let      { service.active      = it }

        // Update category if requested
        request.categoryId?.let { catId ->
            val category = serviceCategoryRepository.findById(catId)
                .orElseThrow { ResourceNotFoundException("Service category not found: $catId") }
            service.category = category
        }

        // Update prices validate after applying both fields
        val newMin = request.priceMin ?: service.priceMin
        val newMax = request.priceMax ?: service.priceMax
        validatePriceRange(newMin, newMax)
        request.priceMin?.let { service.priceMin = it }
        request.priceMax?.let { service.priceMax = it }
        
        return serviceRepository.save(service).toServiceResponse()
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    fun getServicesByProvider(providerId: Long): List<ServiceResponse> =
        serviceRepository.findByProviderIdAndActiveTrue(providerId)
            .map { it.toServiceResponse() }

    @Transactional(readOnly = true)
    fun getServicesByCategory(categoryId: Long): List<ServiceResponse> =
        serviceRepository.findByCategoryIdAndActiveTrue(categoryId)
            .map { it.toServiceResponse() }

    @Transactional(readOnly = true)
    fun searchServices(
        spec: Specification<ServiceEntity>,
        pageable: Pageable
    ) = serviceRepository.findAll(spec, pageable)
        .map { it.toServiceResponse() }

    // -----------------------------------------------------------------------
    // DELETE service
    // -----------------------------------------------------------------------

    fun deleteService(serviceId: Long) {
        val currentUser = authService.getAuthenticatedUser()
        val service     = findServiceOrThrow(serviceId)

        if (service.provider.user.id != currentUser.id) {
            throw AccessDeniedException("You do not own this service.")
        }

        serviceRepository.delete(service)
    }


    //  helpers methods
    private fun findServiceOrThrow(serviceId: Long): ServiceEntity =
        serviceRepository.findById(serviceId)
            .orElseThrow { ResourceNotFoundException("Service not found: $serviceId") }

    private fun validatePriceRange(priceMin: BigDecimal?, priceMax: BigDecimal?) {
        if (priceMin != null && priceMax != null && priceMin > priceMax) {
            throw IllegalArgumentException("priceMin must be less than or equal to priceMax.")
        }
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    fun ServiceEntity.toServiceResponse() = ServiceResponse(
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
}
package com.veridyl.eventez.service

import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.repository.ServiceCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ServiceCategoryService(
    private val serviceCategoryRepository: ServiceCategoryRepository
) {

    fun getAllCategories(): List<ServiceCategoryResponse> =
        serviceCategoryRepository.findAll().map { it.toResponse() }

    fun getCategoryById(id: Long): ServiceCategoryResponse =
        serviceCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Service category not found: $id") }
            .toResponse()

    fun getCategoryBySlug(slug: String): ServiceCategoryResponse =
        serviceCategoryRepository.findBySlug(slug)
            ?.toResponse()
            ?: throw ResourceNotFoundException("Service category not found with slug: $slug")

    // -----------------------------------------------------------------------
    // mappings
    // -----------------------------------------------------------------------

    private fun com.veridyl.eventez.entity.ServiceCategory.toResponse() = ServiceCategoryResponse(
        id          = id,
        name        = name,
        slug        = slug,
        icon        = icon,
        description = description
    )
}

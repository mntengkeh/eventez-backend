package com.veridyl.eventez.service

import com.veridyl.eventez.dto.availability.AvailabilityResponse
import com.veridyl.eventez.dto.availability.BulkSetAvailabilityRequest
import com.veridyl.eventez.dto.availability.SetAvailabilityRequest
import com.veridyl.eventez.entity.Availability
import com.veridyl.eventez.entity.ProviderProfile
import com.veridyl.eventez.entity.enums.AvailabilityStatus
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.mapper.AvailabilityMapper
import com.veridyl.eventez.repository.AvailabilityRepository
import com.veridyl.eventez.repository.ProviderProfileRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AvailabilityService(
    private val availabilityRepository: AvailabilityRepository,
    private val providerRepository: ProviderProfileRepository,
    private val availabilityMapper: AvailabilityMapper,
    private val authService: AuthService
) {

    @Transactional
    fun setAvailability(providerId: Long, request: SetAvailabilityRequest): AvailabilityResponse {
        val provider = providerRepository.findByIdOrNull(providerId)
            ?: throw ResourceNotFoundException("Provider profile not found")

        verifyOwnership(provider)

        // Upsert logic: check if entry exists for this specific date
        val existingEntry = availabilityRepository.findByProviderIdAndDate(providerId, request.date)

        return saveAvailability(existingEntry, provider, request)
    }

    @Transactional
    fun bulkSetAvailability(providerId: Long, request: BulkSetAvailabilityRequest): List<AvailabilityResponse> {
        val provider = providerRepository.findByIdOrNull(providerId)
            ?: throw ResourceNotFoundException("Provider profile not found")

        verifyOwnership(provider)

        return request.entries.map { entryRequest ->
            val existingEntry = availabilityRepository.findByProviderIdAndDate(providerId, entryRequest.date)
            saveAvailability(existingEntry, provider, entryRequest)
        }
    }


    @Transactional(readOnly = true)
    fun getAvailability(providerId: Long, startDate: LocalDate, endDate: LocalDate): List<AvailabilityResponse> {
        return availabilityRepository.findByProviderIdAndDateBetween(providerId, startDate, endDate)
            .map { availabilityMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun isAvailable(providerId: Long, date: LocalDate): Boolean {
        val entry = availabilityRepository.findByProviderIdAndDate(providerId, date)
        // Rule: no entry = available by default
        return entry == null || entry.status == AvailabilityStatus.AVAILABLE
    }

    private fun verifyOwnership(provider: ProviderProfile) {
        val currentUserEmail = authService.getAuthenticatedUser().email
        if (provider.user.email != currentUserEmail) {
            throw AccessDeniedException("You do not have permission to manage availability for this profile")
        }
    }

    private fun saveAvailability(
        existingAvailability: Availability?,
        provider: ProviderProfile,
        request: SetAvailabilityRequest
    ): AvailabilityResponse {
        return availabilityMapper.toResponse(availabilityRepository.save(
            if (existingAvailability != null) {
                availabilityMapper.updateEntity(request, existingAvailability)
                existingAvailability
            } else {
                availabilityMapper.toEntity(request, provider)
            }
        ))
    }

}

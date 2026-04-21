package com.veridyl.eventez.service

import com.veridyl.eventez.dto.event.CreateEventRequest
import com.veridyl.eventez.dto.event.EventResponse
import com.veridyl.eventez.dto.event.UpdateEventRequest
import com.veridyl.eventez.entity.AppUser
import com.veridyl.eventez.entity.EventServiceRequirement
import com.veridyl.eventez.entity.enums.UserRole
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.mapper.EventMapper
import com.veridyl.eventez.repository.EventRepository
import com.veridyl.eventez.repository.ServiceCategoryRepository
import com.veridyl.eventez.repository.UserRepository
import org.apache.coyote.BadRequestException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val categoryRepository: ServiceCategoryRepository,
    private val eventMapper: EventMapper,
    private val authService: AuthService
) {

    @Transactional
    fun createEvent(request: CreateEventRequest): EventResponse {
        val user = authService.getAuthenticatedAppUser()

        if (user.role != UserRole.PLANNER) {
            throw AccessDeniedException("Only planners can create events")
        }

        if (!request.requiredCategoryIds.isNotEmpty()) {
            throw BadRequestException("No event service category ids provided")
        }

        val event = eventMapper.toEntity(request, user)
        val categories = categoryRepository.findAllById(request.requiredCategoryIds)
        val requirements = categories.map { category ->
            EventServiceRequirement(event = event, category = category)
        }
        event.serviceRequirements.addAll(requirements)

        return eventMapper.toResponse(eventRepository.save(event))
    }

    @Transactional
    fun updateEvent(eventId: Long, request: UpdateEventRequest): EventResponse {
        val event = eventRepository.findByIdOrNull(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        verifyOwnership(event.planner)

        eventMapper.updateEntity(request, event)

        request.requiredCategoryIds?.let { newIds ->
            event.serviceRequirements.clear()
            val categories = categoryRepository.findAllById(newIds)
            val newRequirements = categories.map { category ->
                EventServiceRequirement(event = event, category = category)
            }
            event.serviceRequirements.addAll(newRequirements)
        }

        return eventMapper.toResponse(eventRepository.save(event))
    }

    @Transactional(readOnly = true)
    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findByIdOrNull(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        verifyOwnership(event.planner)

        return eventMapper.toResponse(event)
    }

    @Transactional(readOnly = true)
    fun getEventsByPlanner(plannerId: Long): List<EventResponse> {
        return eventRepository.findByPlannerId(plannerId)
            .map { eventMapper.toResponse(it) }
    }

    @Transactional
    fun deleteEvent(eventId: Long) {
        val event = eventRepository.findByIdOrNull(eventId)
            ?: throw ResourceNotFoundException("Event not found")

        verifyOwnership(event.planner)

        eventRepository.delete(event)
    }

    private fun verifyOwnership(owner: AppUser) {
        val currentUser = authService.getAuthenticatedUser()
        if (owner.email != currentUser.email) {
            throw AccessDeniedException("You do not have permission to manage this event")
        }
    }
}

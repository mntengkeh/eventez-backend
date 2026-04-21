package com.veridyl.eventez.controller

import com.veridyl.eventez.dto.event.CreateEventRequest
import com.veridyl.eventez.dto.event.EventResponse
import com.veridyl.eventez.dto.event.UpdateEventRequest
import com.veridyl.eventez.service.EventService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/events")
class EventController(
    private val eventService: EventService
) {

    @PostMapping
    @PreAuthorize("hasAuthority('PLANNER')")
    fun createEvent(
        @RequestBody @Valid request: CreateEventRequest
    ): ResponseEntity<EventResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(eventService.createEvent(request))
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PLANNER')")
    fun getMyEvents(
        @RequestParam plannerId: Long
    ): ResponseEntity<List<EventResponse>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(eventService.getEventsByPlanner(plannerId))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNER')")
    fun getEvent(@PathVariable id: Long): ResponseEntity<EventResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(eventService.getEvent(id))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNER')")
    fun updateEvent(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateEventRequest
    ): ResponseEntity<EventResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(eventService.updateEvent(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNER')")
    fun deleteEvent(@PathVariable id: Long): ResponseEntity<Void> {
        eventService.deleteEvent(id)
        return ResponseEntity.noContent().build()
    }
}

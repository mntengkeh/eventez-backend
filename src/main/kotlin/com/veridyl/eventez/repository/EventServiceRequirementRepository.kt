package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.EventServiceRequirement
import org.springframework.data.jpa.repository.JpaRepository

interface EventServiceRequirementRepository : JpaRepository<EventServiceRequirement, Long> {
    fun findByEventId(eventId: Long): List<EventServiceRequirement>
}
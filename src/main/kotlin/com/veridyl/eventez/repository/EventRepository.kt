package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Event
import com.veridyl.eventez.entity.enums.EventStatus
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPlannerId(plannerId: Long): List<Event>
    fun findByPlannerIdAndStatus(plannerId: Long, status: EventStatus): List<Event>
}

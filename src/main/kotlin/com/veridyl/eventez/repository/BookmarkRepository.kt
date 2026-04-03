package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Bookmark
import org.springframework.data.jpa.repository.JpaRepository

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun findByPlannerId(plannerId: Long): List<Bookmark>
    fun findByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Bookmark?
    fun existsByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Boolean
    fun deleteByPlannerIdAndProviderId(plannerId: Long, providerId: Long)
}

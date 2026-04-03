package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.ServiceCategory
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceCategoryRepository : JpaRepository<ServiceCategory, Long> {
    fun findBySlug(slug: String): ServiceCategory?
    fun findAllByIdIn(ids: List<Long>): List<ServiceCategory>
}

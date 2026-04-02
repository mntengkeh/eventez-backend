package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByProviderId(providerId: Long): List<Review>
    fun findByPlannerId(plannerId: Long): List<Review>
    fun existsByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Boolean

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId")
    fun averageRatingByProviderId(@Param("providerId") providerId: Long): Double?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.provider.id = :providerId")
    fun countByProviderId(@Param("providerId") providerId: Long): Long
}
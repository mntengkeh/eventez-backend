package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Availability
import com.veridyl.eventez.entity.enums.AvailabilityStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface AvailabilityRepository : JpaRepository<Availability, Long> {
    fun findByProviderIdAndDateBetween(
        providerId: Long, startDate: LocalDate, endDate: LocalDate
    ): List<Availability>

    fun findByProviderIdAndDate(providerId: Long, date: LocalDate): Availability?

    fun findByProviderIdAndDateAndStatus(
        providerId: Long, date: LocalDate, status: AvailabilityStatus
    ): Availability?
}
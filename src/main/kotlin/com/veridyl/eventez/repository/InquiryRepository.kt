package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Inquiry
import com.veridyl.eventez.entity.enums.InquiryStatus
import org.springframework.data.jpa.repository.JpaRepository

interface InquiryRepository : JpaRepository<Inquiry, Long> {
    fun findByPlannerId(plannerId: Long): List<Inquiry>
    fun findByProviderId(providerId: Long): List<Inquiry>
    fun findByProviderIdAndStatus(providerId: Long, status: InquiryStatus): List<Inquiry>
    fun countByProviderIdAndStatus(providerId: Long, status: InquiryStatus): Long
}

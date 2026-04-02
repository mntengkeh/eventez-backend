package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.PortfolioItem
import org.springframework.data.jpa.repository.JpaRepository

interface PortfolioItemRepository : JpaRepository<PortfolioItem, Long> {
    fun findByProviderIdOrderByDisplayOrderAsc(providerId: Long): List<PortfolioItem>
    fun countByProviderId(providerId: Long): Long
}
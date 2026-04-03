package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ServiceRepository :
    JpaRepository<Service, Long>,
    JpaSpecificationExecutor<Service> {

    fun findByProviderId(providerId: Long): List<Service>
    fun findByProviderIdAndActiveTrue(providerId: Long): List<Service>
    fun findByCategoryId(categoryId: Long): List<Service>
    fun findByCategoryIdAndActiveTrue(categoryId: Long): List<Service>
}

package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.ProviderProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProviderProfileRepository :
    JpaRepository<ProviderProfile, Long>,
    JpaSpecificationExecutor<ProviderProfile> {

    fun findByUserId(userId: Long): ProviderProfile?
    fun findByCity(city: String): List<ProviderProfile>
    fun findByVerifiedTrue(): List<ProviderProfile>
}

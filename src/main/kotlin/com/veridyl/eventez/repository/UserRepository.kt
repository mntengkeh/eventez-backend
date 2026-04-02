package com.veridyl.eventez.repository

import com.veridyl.eventez.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByEmail(email: String): AppUser?
    fun existsByEmail(email: String): Boolean
}
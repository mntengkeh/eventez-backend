package com.veridyl.eventez.entity.base

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class CreationMetadata {
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now()
        protected set
}

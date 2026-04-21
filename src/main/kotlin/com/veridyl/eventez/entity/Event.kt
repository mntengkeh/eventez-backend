package com.veridyl.eventez.entity

import com.veridyl.eventez.entity.base.AuditMetaData
import com.veridyl.eventez.entity.enums.EventStatus
import com.veridyl.eventez.entity.enums.EventType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate


@Entity
@Table(name = "event")
class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    val planner: AppUser,

    @Column(nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: EventType,

    @Column(name = "event_date", nullable = false)
    var eventDate: LocalDate,

    var location: String? = null,
    var city: String? = null,
    var state: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(name = "budget_min")
    var budgetMin: BigDecimal? = null,

    @Column(name = "budget_max")
    var budgetMax: BigDecimal? = null,

    @Column(name = "guest_count")
    var guestCount: Int? = null,

    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EventStatus = EventStatus.DRAFT,

//    @Column(name = "created_at", nullable = false, updatable = false)
//    val createdAt: Instant = Instant.now(),
//
//    @Column(name = "updated_at", nullable = false)
//    var updatedAt: Instant = Instant.now(),

    // -- Relationships --

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val serviceRequirements: MutableList<EventServiceRequirement> = mutableListOf()
): AuditMetaData()

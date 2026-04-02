package com.veridyl.eventez.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "provider_profile")
class ProviderProfile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: AppUser,

    @Column(name = "business_name", nullable = false)
    var businessName: String,

    var description: String? = null,

    var address: String? = null,
    var city: String? = null,
    var state: String? = null,
    @Column(name = "zip_code")
    var zipCode: String? = null,

    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(name = "service_radius")
    var serviceRadius: Int = 50,

    var website: String? = null,

    var verified: Boolean = false,

    @Column(name = "avg_rating")
    var avgRating: Double = 0.0,

    @Column(name = "review_count")
    var reviewCount: Int = 0,

    @Column(name = "response_rate")
    var responseRate: Double = 0.0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    // -- Relationships --

    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    val services: MutableList<Service> = mutableListOf(),

    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    val portfolioItems: MutableList<PortfolioItem> = mutableListOf(),

    @OneToMany(mappedBy = "provider", cascade = [CascadeType.ALL], orphanRemoval = true)
    val availabilities: MutableList<Availability> = mutableListOf()
)
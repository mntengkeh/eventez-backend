package com.veridyl.eventez.entity

import com.veridyl.eventez.entity.base.CreationMetadata
import com.veridyl.eventez.entity.enums.MediaType
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
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "portfolio_item")
class PortfolioItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @Column(name = "media_url", nullable = false)
    var mediaUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    var mediaType: MediaType = MediaType.IMAGE,

    var caption: String? = null,

    @Column(name = "display_order")
    var displayOrder: Int = 0,

//    @Column(name = "created_at", nullable = false, updatable = false)
//    val createdAt: Instant = Instant.now()
): CreationMetadata()

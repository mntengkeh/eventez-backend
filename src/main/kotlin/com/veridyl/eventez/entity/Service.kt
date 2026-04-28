package com.veridyl.eventez.entity

import com.veridyl.eventez.entity.base.AuditMetaData
import com.veridyl.eventez.entity.enums.PriceType
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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant


@Entity
@Table(name = "service")
class Service(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: ServiceCategory,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(name = "price_min")
    var priceMin: BigDecimal? = null,

    @Column(name = "price_max")
    var priceMax: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "price_type", nullable = false)
    var priceType: PriceType = PriceType.FIXED,

    var active: Boolean = true,

//    @Column(name = "created_at", nullable = false, updatable = false)
//    val createdAt: Instant = Instant.now(),
//
//    @Column(name = "updated_at", nullable = false)
//    var updatedAt: Instant = Instant.now()
): AuditMetaData()

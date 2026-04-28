package com.veridyl.eventez.specification

import com.veridyl.eventez.entity.Service as ServiceEntity
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal

object ServiceSpecification {

    fun active(): Specification<ServiceEntity> =
        Specification { root, _, cb -> cb.equal(root.get<Boolean>("active"), true) }

    fun categoryId(categoryId: Long): Specification<ServiceEntity> =
        Specification { root, _, cb -> cb.equal(root.get<Long>("category").get<Long>("id"), categoryId) }

    fun providerId(providerId: Long): Specification<ServiceEntity> =
        Specification { root, _, cb -> cb.equal(root.get<Long>("provider").get<Long>("id"), providerId) }

    fun nameContains(search: String): Specification<ServiceEntity> =
        Specification { root, _, cb -> cb.like(cb.lower(root.get("name")), "%${search.lowercase()}%") }

    fun priceBetween(min: BigDecimal, max: BigDecimal): Specification<ServiceEntity> =
        Specification { root, _, cb ->
            cb.and(
                cb.lessThanOrEqualTo(root.get<BigDecimal>("priceMin"), max),
                cb.greaterThanOrEqualTo(root.get<BigDecimal>("priceMax"), min)
            )
        }
}

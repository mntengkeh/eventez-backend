package com.veridyl.eventez.specification

import com.veridyl.eventez.entity.ProviderProfile
import org.springframework.data.jpa.domain.Specification

object ProviderSpecification {

    fun inCity(city: String): Specification<ProviderProfile> =
        Specification { root, _, cb -> cb.equal(cb.lower(root.get("city")), city.lowercase()) }

    fun inState(state: String): Specification<ProviderProfile> =
        Specification { root, _, cb -> cb.equal(cb.lower(root.get("state")), state.lowercase()) }

    fun minRating(rating: Double): Specification<ProviderProfile> =
        Specification { root, _, cb -> cb.greaterThanOrEqualTo(root.get("avgRating"), rating) }

    fun isVerified(): Specification<ProviderProfile> =
        Specification { root, _, cb -> cb.isTrue(root.get("verified")) }

    fun businessNameContains(search: String): Specification<ProviderProfile> =
        Specification { root, _, cb ->
            cb.like(cb.lower(root.get("businessName")), "%${search.lowercase()}%")
        }
}

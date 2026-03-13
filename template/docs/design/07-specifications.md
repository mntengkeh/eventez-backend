# 7. Specifications

All specifications go under `com.veridyl.eventez.specification`.
Use Spring Data JPA `Specification<T>` for dynamic query building.

## 7.1 ProviderSpecification

Used for searching/filtering providers in the matching engine and search.

```kotlin
object ProviderSpecification {

    /** Providers whose service area covers a given point (approximate bounding box) */
    fun withinServiceArea(lat: Double, lng: Double): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            // Approximate: 1 degree latitude ≈ 111 km
            // Filter providers whose (lat,lng) is within their service_radius of the event location
            val latDiff = cb.abs(cb.diff(root.get<Double>("latitude"), lat))
            val lngDiff = cb.abs(cb.diff(root.get<Double>("longitude"), lng))
            val radiusDegrees = cb.quot(root.get<Int>("serviceRadius"), 111.0)

            cb.and(
                cb.isNotNull(root.get<Double>("latitude")),
                cb.isNotNull(root.get<Double>("longitude")),
                cb.le(latDiff, radiusDegrees),
                cb.le(lngDiff, radiusDegrees)
            )
        }
    }

    /** Providers in a specific city */
    fun inCity(city: String): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            cb.equal(cb.lower(root.get("city")), city.lowercase())
        }
    }

    /** Providers in a specific state */
    fun inState(state: String): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            cb.equal(cb.lower(root.get("state")), state.lowercase())
        }
    }

    /** Providers with an average rating >= given value */
    fun minRating(rating: Double): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            cb.greaterThanOrEqualTo(root.get("avgRating"), rating)
        }
    }

    /** Verified providers only */
    fun isVerified(): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            cb.isTrue(root.get("verified"))
        }
    }

    /** Providers whose business name contains search term (case insensitive) */
    fun businessNameContains(term: String): Specification<ProviderProfile> {
        return Specification { root, _, cb ->
            cb.like(cb.lower(root.get("businessName")), "%${term.lowercase()}%")
        }
    }
}
```

## 7.2 ServiceSpecification

Used for filtering services by category, price range, and provider.

```kotlin
object ServiceSpecification {

    /** Services in a specific category */
    fun inCategory(categoryId: Long): Specification<Service> {
        return Specification { root, _, cb ->
            cb.equal(root.get<ServiceCategory>("category").get<Long>("id"), categoryId)
        }
    }

    /** Services within a budget range (overlapping ranges) */
    fun withinBudget(budgetMin: BigDecimal, budgetMax: BigDecimal): Specification<Service> {
        return Specification { root, _, cb ->
            cb.and(
                cb.or(
                    cb.isNull(root.get<BigDecimal>("priceMin")),
                    cb.lessThanOrEqualTo(root.get("priceMin"), budgetMax)
                ),
                cb.or(
                    cb.isNull(root.get<BigDecimal>("priceMax")),
                    cb.greaterThanOrEqualTo(root.get("priceMax"), budgetMin)
                )
            )
        }
    }

    /** Only active services */
    fun isActive(): Specification<Service> {
        return Specification { root, _, cb ->
            cb.isTrue(root.get("active"))
        }
    }

    /** Services for a specific provider */
    fun forProvider(providerId: Long): Specification<Service> {
        return Specification { root, _, cb ->
            cb.equal(root.get<ProviderProfile>("provider").get<Long>("id"), providerId)
        }
    }

    /** Price type filter */
    fun hasPriceType(priceType: PriceType): Specification<Service> {
        return Specification { root, _, cb ->
            cb.equal(root.get<PriceType>("priceType"), priceType)
        }
    }
}
```

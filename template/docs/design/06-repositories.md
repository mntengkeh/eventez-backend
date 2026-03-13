# 6. Repositories

All repositories go under `com.veridyl.eventez.repository`.
Extend `JpaRepository<Entity, Long>` and `JpaSpecificationExecutor<Entity>` where filtering is needed.

## 6.1 UserRepository

```kotlin
interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByEmail(email: String): AppUser?
    fun existsByEmail(email: String): Boolean
}
```

## 6.2 ProviderProfileRepository

```kotlin
interface ProviderProfileRepository :
    JpaRepository<ProviderProfile, Long>,
    JpaSpecificationExecutor<ProviderProfile> {

    fun findByUserId(userId: Long): ProviderProfile?
    fun findByCity(city: String): List<ProviderProfile>
    fun findByVerifiedTrue(): List<ProviderProfile>
}
```

## 6.3 ServiceRepository

```kotlin
interface ServiceRepository :
    JpaRepository<Service, Long>,
    JpaSpecificationExecutor<Service> {

    fun findByProviderId(providerId: Long): List<Service>
    fun findByProviderIdAndActiveTrue(providerId: Long): List<Service>
    fun findByCategoryId(categoryId: Long): List<Service>
    fun findByCategoryIdAndActiveTrue(categoryId: Long): List<Service>
}
```

## 6.4 ServiceCategoryRepository

```kotlin
interface ServiceCategoryRepository : JpaRepository<ServiceCategory, Long> {
    fun findBySlug(slug: String): ServiceCategory?
    fun findAllByIdIn(ids: List<Long>): List<ServiceCategory>
}
```

## 6.5 PortfolioItemRepository

```kotlin
interface PortfolioItemRepository : JpaRepository<PortfolioItem, Long> {
    fun findByProviderIdOrderByDisplayOrderAsc(providerId: Long): List<PortfolioItem>
    fun countByProviderId(providerId: Long): Long
}
```

## 6.6 AvailabilityRepository

```kotlin
interface AvailabilityRepository : JpaRepository<Availability, Long> {
    fun findByProviderIdAndDateBetween(
        providerId: Long, startDate: LocalDate, endDate: LocalDate
    ): List<Availability>

    fun findByProviderIdAndDate(providerId: Long, date: LocalDate): Availability?

    fun findByProviderIdAndDateAndStatus(
        providerId: Long, date: LocalDate, status: AvailabilityStatus
    ): Availability?
}
```

## 6.7 EventRepository

```kotlin
interface EventRepository : JpaRepository<Event, Long> {
    fun findByPlannerId(plannerId: Long): List<Event>
    fun findByPlannerIdAndStatus(plannerId: Long, status: EventStatus): List<Event>
}
```

## 6.8 EventServiceRequirementRepository

```kotlin
interface EventServiceRequirementRepository : JpaRepository<EventServiceRequirement, Long> {
    fun findByEventId(eventId: Long): List<EventServiceRequirement>
}
```

## 6.9 BookmarkRepository

```kotlin
interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun findByPlannerId(plannerId: Long): List<Bookmark>
    fun findByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Bookmark?
    fun existsByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Boolean
    fun deleteByPlannerIdAndProviderId(plannerId: Long, providerId: Long)
}
```

## 6.10 InquiryRepository

```kotlin
interface InquiryRepository : JpaRepository<Inquiry, Long> {
    fun findByPlannerId(plannerId: Long): List<Inquiry>
    fun findByProviderId(providerId: Long): List<Inquiry>
    fun findByProviderIdAndStatus(providerId: Long, status: InquiryStatus): List<Inquiry>
    fun countByProviderIdAndStatus(providerId: Long, status: InquiryStatus): Long
}
```

## 6.11 ReviewRepository

```kotlin
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByProviderId(providerId: Long): List<Review>
    fun findByPlannerId(plannerId: Long): List<Review>
    fun existsByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Boolean

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.provider.id = :providerId")
    fun averageRatingByProviderId(@Param("providerId") providerId: Long): Double?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.provider.id = :providerId")
    fun countByProviderId(@Param("providerId") providerId: Long): Long
}
```

# 4. Entities

All entities go under `com.veridyl.eventez.entity`.

> **Conventions:**
> - Use `@Entity` + `@Table(name = "...")` annotations.
> - Every entity has a `Long` primary key with `@GeneratedValue(strategy = IDENTITY)`.
> - Use `@Enumerated(EnumType.STRING)` for enum fields.
> - Use `Instant` or `LocalDate`/`LocalDateTime` for timestamps.
> - Relationships use lazy loading by default (`FetchType.LAZY`).

## 4.1 AppUser

```kotlin
@Entity
@Table(name = "app_user")
class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    var phone: String? = null,

    var enabled: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    // -- Relationships --

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var providerProfile: ProviderProfile? = null,

    @OneToMany(mappedBy = "planner", fetch = FetchType.LAZY)
    val events: MutableList<Event> = mutableListOf(),

    @OneToMany(mappedBy = "planner", fetch = FetchType.LAZY)
    val bookmarks: MutableList<Bookmark> = mutableListOf()
)
```

## 4.2 ProviderProfile

```kotlin
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
```

## 4.3 ServiceCategory

```kotlin
@Entity
@Table(name = "service_category")
class ServiceCategory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false, unique = true)
    val slug: String,

    val icon: String? = null,

    val description: String? = null
)
```

## 4.4 Service

```kotlin
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
    val category: ServiceCategory,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(name = "price_min")
    var priceMin: BigDecimal? = null,

    @Column(name = "price_max")
    var priceMax: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    var priceType: PriceType = PriceType.FIXED,

    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
```

## 4.5 PortfolioItem

```kotlin
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

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
```

## 4.6 Availability

```kotlin
@Entity
@Table(name = "availability")
class Availability(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @Column(nullable = false)
    var date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AvailabilityStatus = AvailabilityStatus.AVAILABLE,

    var note: String? = null
)
```

## 4.7 Event

```kotlin
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

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    // -- Relationships --

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val serviceRequirements: MutableList<EventServiceRequirement> = mutableListOf()
)
```

## 4.8 EventServiceRequirement

```kotlin
@Entity
@Table(name = "event_service_requirement")
class EventServiceRequirement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: ServiceCategory,

    var note: String? = null
)
```

## 4.9 Bookmark

```kotlin
@Entity
@Table(name = "bookmark")
class Bookmark(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    val planner: AppUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
```

## 4.10 Inquiry

```kotlin
@Entity
@Table(name = "inquiry")
class Inquiry(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    val planner: AppUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @Column(nullable = false)
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InquiryStatus = InquiryStatus.PENDING,

    var response: String? = null,

    @Column(name = "responded_at")
    var respondedAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
```

## 4.11 Review

```kotlin
@Entity
@Table(name = "review")
class Review(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    val planner: AppUser,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    val provider: ProviderProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    val event: Event? = null,

    @Column(nullable = false)
    var rating: Short,

    var title: String? = null,

    var comment: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
```

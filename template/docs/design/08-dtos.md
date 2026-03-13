# 8. DTOs (Request/Response)

All DTOs go under `com.veridyl.eventez.dto`. Use `data class` for all DTOs.

## 8.1 Auth DTOs

```kotlin
// --- Requests ---

data class RegisterRequest(
    @field:Email
    val email: String,
    @field:Size(min = 8, max = 100)
    val password: String,
    @field:NotBlank
    val fullName: String,
    val phone: String? = null,
    val role: UserRole
)

data class LoginRequest(
    @field:Email
    val email: String,
    val password: String
)

// --- Responses ---

data class UserResponse(
    val id: Long,
    val email: String,
    val role: UserRole,
    val fullName: String,
    val phone: String?,
    val createdAt: Instant
)

data class AuthResponse(
    val user: UserResponse,
    val message: String = "Success"
)
```

## 8.2 Provider DTOs

```kotlin
// --- Requests ---

data class CreateProviderProfileRequest(
    @field:NotBlank
    val businessName: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceRadius: Int = 50,
    val website: String? = null
)

data class UpdateProviderProfileRequest(
    val businessName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val serviceRadius: Int? = null,
    val website: String? = null
)

// --- Responses ---

data class ProviderProfileResponse(
    val id: Long,
    val userId: Long,
    val businessName: String,
    val description: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val serviceRadius: Int,
    val website: String?,
    val verified: Boolean,
    val avgRating: Double,
    val reviewCount: Int,
    val responseRate: Double,
    val createdAt: Instant
)

data class ProviderDetailResponse(
    val profile: ProviderProfileResponse,
    val services: List<ServiceResponse>,
    val portfolio: List<PortfolioItemResponse>,
    val reviews: List<ReviewResponse>
)

data class ProviderSummaryResponse(
    val id: Long,
    val businessName: String,
    val city: String?,
    val state: String?,
    val avgRating: Double,
    val reviewCount: Int,
    val verified: Boolean,
    val thumbnailUrl: String?   // first portfolio image
)
```

## 8.3 Service DTOs

```kotlin
// --- Requests ---

data class CreateServiceRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotNull
    val categoryId: Long,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
    val priceType: PriceType = PriceType.FIXED
)

data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val categoryId: Long? = null,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
    val priceType: PriceType? = null,
    val active: Boolean? = null
)

// --- Responses ---

data class ServiceResponse(
    val id: Long,
    val providerId: Long,
    val categoryId: Long,
    val categoryName: String,
    val name: String,
    val description: String?,
    val priceMin: BigDecimal?,
    val priceMax: BigDecimal?,
    val priceType: PriceType,
    val active: Boolean
)

data class ServiceCategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val icon: String?,
    val description: String?
)
```

## 8.4 Event DTOs

```kotlin
// --- Requests ---

data class CreateEventRequest(
    @field:NotBlank
    val title: String,
    @field:NotNull
    val eventType: EventType,
    @field:NotNull
    @field:FutureOrPresent
    val eventDate: LocalDate,
    val location: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val guestCount: Int? = null,
    val description: String? = null,
    val requiredCategoryIds: List<Long> = emptyList()
)

data class UpdateEventRequest(
    val title: String? = null,
    val eventType: EventType? = null,
    val eventDate: LocalDate? = null,
    val location: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val guestCount: Int? = null,
    val description: String? = null,
    val status: EventStatus? = null,
    val requiredCategoryIds: List<Long>? = null
)

// --- Responses ---

data class EventResponse(
    val id: Long,
    val plannerId: Long,
    val title: String,
    val eventType: EventType,
    val eventDate: LocalDate,
    val location: String?,
    val city: String?,
    val state: String?,
    val budgetMin: BigDecimal?,
    val budgetMax: BigDecimal?,
    val guestCount: Int?,
    val description: String?,
    val status: EventStatus,
    val requiredCategories: List<ServiceCategoryResponse>,
    val createdAt: Instant
)
```

## 8.5 Matching DTOs

```kotlin
// --- Request ---

data class MatchRequest(
    val eventId: Long?,                       // use existing event data, OR supply inline:
    val latitude: Double? = null,
    val longitude: Double? = null,
    val budgetMin: BigDecimal? = null,
    val budgetMax: BigDecimal? = null,
    val categoryIds: List<Long> = emptyList(),
    val eventDate: LocalDate? = null,
    val minRating: Double? = null,
    val sortBy: MatchSortBy = MatchSortBy.RELEVANCE
)

enum class MatchSortBy { RELEVANCE, PRICE_LOW, PRICE_HIGH, RATING, DISTANCE }

// --- Response ---

data class MatchResponse(
    val categories: List<CategoryMatchResult>
)

data class CategoryMatchResult(
    val category: ServiceCategoryResponse,
    val providers: List<MatchedProviderResponse>
)

data class MatchedProviderResponse(
    val provider: ProviderSummaryResponse,
    val service: ServiceResponse,
    val distanceKm: Double?,        // null if location not provided
    val relevanceScore: Double      // 0-100
)
```

## 8.6 Portfolio DTOs

```kotlin
data class CreatePortfolioItemRequest(
    @field:NotBlank
    val mediaUrl: String,
    val mediaType: MediaType = MediaType.IMAGE,
    val caption: String? = null,
    val displayOrder: Int = 0
)

data class PortfolioItemResponse(
    val id: Long,
    val mediaUrl: String,
    val mediaType: MediaType,
    val caption: String?,
    val displayOrder: Int,
    val createdAt: Instant
)
```

## 8.7 Availability DTOs

```kotlin
data class SetAvailabilityRequest(
    @field:NotNull
    val date: LocalDate,
    @field:NotNull
    val status: AvailabilityStatus,
    val note: String? = null
)

data class BulkSetAvailabilityRequest(
    val entries: List<SetAvailabilityRequest>
)

data class AvailabilityResponse(
    val id: Long,
    val date: LocalDate,
    val status: AvailabilityStatus,
    val note: String?
)
```

## 8.8 Inquiry DTOs

```kotlin
data class CreateInquiryRequest(
    @field:NotNull
    val providerId: Long,
    val eventId: Long? = null,
    @field:NotBlank
    val message: String
)

data class RespondToInquiryRequest(
    @field:NotBlank
    val response: String
)

data class InquiryResponse(
    val id: Long,
    val plannerId: Long,
    val plannerName: String,
    val providerId: Long,
    val providerBusinessName: String,
    val eventId: Long?,
    val message: String,
    val status: InquiryStatus,
    val response: String?,
    val respondedAt: Instant?,
    val createdAt: Instant
)
```

## 8.9 Review DTOs

```kotlin
data class CreateReviewRequest(
    @field:NotNull
    val providerId: Long,
    val eventId: Long? = null,
    @field:Min(1) @field:Max(5)
    val rating: Short,
    val title: String? = null,
    val comment: String? = null
)

data class UpdateReviewRequest(
    @field:Min(1) @field:Max(5)
    val rating: Short? = null,
    val title: String? = null,
    val comment: String? = null
)

data class ReviewResponse(
    val id: Long,
    val plannerId: Long,
    val plannerName: String,
    val providerId: Long,
    val eventId: Long?,
    val rating: Short,
    val title: String?,
    val comment: String?,
    val createdAt: Instant
)
```

## 8.10 Bookmark DTOs

```kotlin
data class BookmarkResponse(
    val id: Long,
    val provider: ProviderSummaryResponse,
    val createdAt: Instant
)
```

## 8.11 Common DTOs

```kotlin
/** Standard error response */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now()
)

/** Paginated response wrapper */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)
```

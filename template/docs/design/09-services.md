# 9. Services

All services go under `com.veridyl.eventez.service`.
Each service class should be annotated with `@Service` and use constructor injection.

## 9.1 AuthService

```
AuthService
├── register(request: RegisterRequest): UserResponse
│   ├── Validate email not taken (UserRepository.existsByEmail)
│   ├── Hash password with PasswordEncoder
│   ├── Save AppUser
│   └── Return UserResponse
│
├── login(request: LoginRequest): UserResponse
│   ├── Authenticate via AuthenticationManager
│   ├── Set SecurityContext
│   └── Return UserResponse
│
├── getCurrentUser(): AppUser
│   └── Get user from SecurityContextHolder → load from UserRepository
│
└── logout(): void
    └── Invalidate session
```

## 9.2 ProviderProfileService

```
ProviderProfileService
├── createProfile(userId: Long, request: CreateProviderProfileRequest): ProviderProfileResponse
│   ├── Verify user exists and has role PROVIDER
│   ├── Verify user doesn't already have a profile
│   ├── Save ProviderProfile
│   └── Return ProviderProfileResponse
│
├── updateProfile(profileId: Long, request: UpdateProviderProfileRequest): ProviderProfileResponse
│   ├── Load existing profile
│   ├── Apply non-null fields from request
│   ├── Save and return
│   └── Verify ownership (current user must own this profile)
│
├── getProfile(profileId: Long): ProviderDetailResponse
│   ├── Load profile with services, portfolio, reviews
│   └── Map to ProviderDetailResponse
│
├── getProfileByUserId(userId: Long): ProviderProfileResponse
│
├── searchProviders(filters...): Page<ProviderSummaryResponse>
│   ├── Build Specification from filters using ProviderSpecification
│   ├── Apply pagination and sorting
│   └── Map results to ProviderSummaryResponse
│
└── deleteProfile(profileId: Long): void
    └── Verify ownership, then delete
```

## 9.3 ServiceService (or ProviderServiceService)

```
ServiceService
├── createService(providerId: Long, request: CreateServiceRequest): ServiceResponse
│   ├── Verify provider exists
│   ├── Verify category exists
│   ├── Verify ownership
│   ├── Save Service entity
│   └── Return ServiceResponse
│
├── updateService(serviceId: Long, request: UpdateServiceRequest): ServiceResponse
│   ├── Load service, verify ownership
│   ├── Apply non-null updates
│   └── Save and return
│
├── getServicesByProvider(providerId: Long): List<ServiceResponse>
│
├── getServicesByCategory(categoryId: Long): List<ServiceResponse>
│
├── deleteService(serviceId: Long): void
│   └── Verify ownership, then delete
│
└── searchServices(spec: Specification<Service>, pageable: Pageable): Page<ServiceResponse>
```

## 9.4 ServiceCategoryService

```
ServiceCategoryService
├── getAllCategories(): List<ServiceCategoryResponse>
├── getCategoryById(id: Long): ServiceCategoryResponse
└── getCategoryBySlug(slug: String): ServiceCategoryResponse
```

## 9.5 EventService

```
EventService
├── createEvent(plannerId: Long, request: CreateEventRequest): EventResponse
│   ├── Verify user is a PLANNER
│   ├── Build Event entity
│   ├── Create EventServiceRequirement entries for each categoryId
│   ├── Save all
│   └── Return EventResponse with categories
│
├── updateEvent(eventId: Long, request: UpdateEventRequest): EventResponse
│   ├── Verify ownership
│   ├── Apply non-null updates
│   ├── If requiredCategoryIds provided, replace all EventServiceRequirements
│   └── Save and return
│
├── getEvent(eventId: Long): EventResponse
│   └── Verify ownership or admin
│
├── getEventsByPlanner(plannerId: Long): List<EventResponse>
│
└── deleteEvent(eventId: Long): void
    └── Verify ownership, then delete
```

## 9.6 MatchingService

This is the **core business logic** of the application.

```
MatchingService
├── findMatches(request: MatchRequest, currentUser: AppUser): MatchResponse
│   │
│   ├── STEP 1: Resolve input
│   │   ├── If eventId provided, load event and extract location/budget/categories
│   │   └── Otherwise use inline request fields
│   │
│   ├── STEP 2: For each required category:
│   │   │
│   │   ├── Query services WHERE:
│   │   │   ├── category matches
│   │   │   ├── service is active
│   │   │   ├── price range overlaps budget (if budget provided)
│   │   │   └── provider is within service area of event location (if location provided)
│   │   │
│   │   ├── Check provider availability on event date (if date provided)
│   │   │
│   │   ├── Calculate relevance score for each result:
│   │   │   ├── Distance score: closer = higher (0-30 points)
│   │   │   ├── Price fit: mid-budget = higher (0-25 points)
│   │   │   ├── Rating score: higher = better (0-25 points)
│   │   │   ├── Response rate score: higher = better (0-10 points)
│   │   │   └── Review count bonus: more reviews = more trustworthy (0-10 points)
│   │   │
│   │   └── Sort by relevance score (or requested sortBy)
│   │
│   └── STEP 3: Return results grouped by category
│
└── calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double
    └── Haversine formula to compute distance in km
```

### Relevance Score Breakdown

| Factor | Max Points | Calculation |
|--------|-----------|-------------|
| Distance | 30 | `30 * (1 - distance/maxDistance)`, clamped to 0 |
| Price Fit | 25 | `25 * (1 - abs(midPrice - midBudget) / budgetRange)` |
| Rating | 25 | `25 * (avgRating / 5.0)` |
| Response Rate | 10 | `10 * (responseRate / 100.0)` |
| Review Volume | 10 | `min(10, reviewCount)` — caps at 10 reviews |
| **Total** | **100** | |

## 9.7 PortfolioService

```
PortfolioService
├── addItem(providerId: Long, request: CreatePortfolioItemRequest): PortfolioItemResponse
│   └── Verify ownership, save
│
├── getItems(providerId: Long): List<PortfolioItemResponse>
│
├── deleteItem(itemId: Long): void
│   └── Verify ownership, delete
│
└── reorderItems(providerId: Long, orderedIds: List<Long>): List<PortfolioItemResponse>
    └── Update display_order for each item
```

## 9.8 AvailabilityService

```
AvailabilityService
├── setAvailability(providerId: Long, request: SetAvailabilityRequest): AvailabilityResponse
│   ├── Verify ownership
│   ├── Upsert: if entry exists for date, update status; otherwise create
│   └── Return response
│
├── bulkSetAvailability(providerId: Long, request: BulkSetAvailabilityRequest): List<AvailabilityResponse>
│
├── getAvailability(providerId: Long, startDate: LocalDate, endDate: LocalDate): List<AvailabilityResponse>
│
└── isAvailable(providerId: Long, date: LocalDate): Boolean
    └── Check if date has status AVAILABLE (or no entry = available by default)
```

## 9.9 BookmarkService

```
BookmarkService
├── addBookmark(plannerId: Long, providerId: Long): BookmarkResponse
│   ├── Verify not already bookmarked
│   └── Save and return
│
├── removeBookmark(plannerId: Long, providerId: Long): void
│
├── getBookmarks(plannerId: Long): List<BookmarkResponse>
│
└── isBookmarked(plannerId: Long, providerId: Long): Boolean
```

## 9.10 InquiryService

```
InquiryService
├── createInquiry(plannerId: Long, request: CreateInquiryRequest): InquiryResponse
│   ├── Verify provider exists
│   ├── Verify event belongs to planner (if eventId provided)
│   ├── Save Inquiry
│   └── Return response
│
├── respondToInquiry(inquiryId: Long, providerId: Long, request: RespondToInquiryRequest): InquiryResponse
│   ├── Verify inquiry belongs to this provider
│   ├── Set response text, respondedAt, status = RESPONDED
│   ├── Update provider's response_rate
│   └── Return response
│
├── getInquiriesForPlanner(plannerId: Long): List<InquiryResponse>
│
├── getInquiriesForProvider(providerId: Long): List<InquiryResponse>
│
├── markAsRead(inquiryId: Long, providerId: Long): InquiryResponse
│
└── closeInquiry(inquiryId: Long): InquiryResponse
```

## 9.11 ReviewService

```
ReviewService
├── createReview(plannerId: Long, request: CreateReviewRequest): ReviewResponse
│   ├── Verify provider exists
│   ├── Verify planner hasn't already reviewed this provider
│   ├── Save Review
│   ├── Recalculate provider's avgRating and reviewCount
│   └── Return response
│
├── updateReview(reviewId: Long, request: UpdateReviewRequest): ReviewResponse
│   ├── Verify ownership
│   ├── Apply updates
│   ├── Recalculate provider's avgRating
│   └── Return response
│
├── getReviewsForProvider(providerId: Long): List<ReviewResponse>
│
├── getReviewsByPlanner(plannerId: Long): List<ReviewResponse>
│
└── deleteReview(reviewId: Long, plannerId: Long): void
    ├── Verify ownership
    ├── Delete
    └── Recalculate provider's avgRating and reviewCount
```

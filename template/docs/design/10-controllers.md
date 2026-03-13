# 10. Controllers

All controllers go under `com.veridyl.eventez.controller`.
Use `@RestController` and `@RequestMapping("/api/v1/...")`.

## 10.1 AuthController

```
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService)

POST /api/v1/auth/register
  Body: RegisterRequest
  Response: 201 Created → AuthResponse
  Access: Public (permitAll)

POST /api/v1/auth/login
  Body: LoginRequest
  Response: 200 OK → AuthResponse
  Access: Public (permitAll)

POST /api/v1/auth/logout
  Response: 200 OK
  Access: Authenticated

GET  /api/v1/auth/me
  Response: 200 OK → UserResponse
  Access: Authenticated
```

## 10.2 ProviderController

```
@RestController
@RequestMapping("/api/v1/providers")
class ProviderController(
    private val providerProfileService: ProviderProfileService,
    private val serviceService: ServiceService,
    private val portfolioService: PortfolioService,
    private val availabilityService: AvailabilityService,
    private val reviewService: ReviewService
)

--- Profile ---

POST /api/v1/providers/profile
  Body: CreateProviderProfileRequest
  Response: 201 Created → ProviderProfileResponse
  Access: PROVIDER only

PUT  /api/v1/providers/profile
  Body: UpdateProviderProfileRequest
  Response: 200 OK → ProviderProfileResponse
  Access: PROVIDER only (own profile)

GET  /api/v1/providers/{id}
  Response: 200 OK → ProviderDetailResponse
  Access: Public

GET  /api/v1/providers
  Query params: city, state, minRating, verified, search, page, size, sort
  Response: 200 OK → PagedResponse<ProviderSummaryResponse>
  Access: Public

--- Services ---

POST /api/v1/providers/{providerId}/services
  Body: CreateServiceRequest
  Response: 201 Created → ServiceResponse
  Access: PROVIDER only (own profile)

GET  /api/v1/providers/{providerId}/services
  Response: 200 OK → List<ServiceResponse>
  Access: Public

PUT  /api/v1/providers/services/{serviceId}
  Body: UpdateServiceRequest
  Response: 200 OK → ServiceResponse
  Access: PROVIDER only (own service)

DELETE /api/v1/providers/services/{serviceId}
  Response: 204 No Content
  Access: PROVIDER only (own service)

--- Portfolio ---

POST /api/v1/providers/{providerId}/portfolio
  Body: CreatePortfolioItemRequest
  Response: 201 Created → PortfolioItemResponse
  Access: PROVIDER only (own profile)

GET  /api/v1/providers/{providerId}/portfolio
  Response: 200 OK → List<PortfolioItemResponse>
  Access: Public

DELETE /api/v1/providers/portfolio/{itemId}
  Response: 204 No Content
  Access: PROVIDER only (own item)

--- Availability ---

PUT  /api/v1/providers/{providerId}/availability
  Body: SetAvailabilityRequest
  Response: 200 OK → AvailabilityResponse
  Access: PROVIDER only (own profile)

PUT  /api/v1/providers/{providerId}/availability/bulk
  Body: BulkSetAvailabilityRequest
  Response: 200 OK → List<AvailabilityResponse>
  Access: PROVIDER only (own profile)

GET  /api/v1/providers/{providerId}/availability
  Query params: startDate, endDate
  Response: 200 OK → List<AvailabilityResponse>
  Access: Public

--- Reviews (read only from provider side) ---

GET  /api/v1/providers/{providerId}/reviews
  Response: 200 OK → List<ReviewResponse>
  Access: Public
```

## 10.3 CategoryController

```
@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(private val categoryService: ServiceCategoryService)

GET  /api/v1/categories
  Response: 200 OK → List<ServiceCategoryResponse>
  Access: Public

GET  /api/v1/categories/{id}
  Response: 200 OK → ServiceCategoryResponse
  Access: Public
```

## 10.4 EventController

```
@RestController
@RequestMapping("/api/v1/events")
class EventController(private val eventService: EventService)

POST /api/v1/events
  Body: CreateEventRequest
  Response: 201 Created → EventResponse
  Access: PLANNER only

GET  /api/v1/events
  Response: 200 OK → List<EventResponse>
  Access: PLANNER only (own events)

GET  /api/v1/events/{id}
  Response: 200 OK → EventResponse
  Access: PLANNER only (own event)

PUT  /api/v1/events/{id}
  Body: UpdateEventRequest
  Response: 200 OK → EventResponse
  Access: PLANNER only (own event)

DELETE /api/v1/events/{id}
  Response: 204 No Content
  Access: PLANNER only (own event)
```

## 10.5 MatchController

```
@RestController
@RequestMapping("/api/v1/match")
class MatchController(private val matchingService: MatchingService)

POST /api/v1/match
  Body: MatchRequest
  Response: 200 OK → MatchResponse
  Access: PLANNER only
```

## 10.6 BookmarkController

```
@RestController
@RequestMapping("/api/v1/bookmarks")
class BookmarkController(private val bookmarkService: BookmarkService)

POST /api/v1/bookmarks/{providerId}
  Response: 201 Created → BookmarkResponse
  Access: PLANNER only

DELETE /api/v1/bookmarks/{providerId}
  Response: 204 No Content
  Access: PLANNER only

GET  /api/v1/bookmarks
  Response: 200 OK → List<BookmarkResponse>
  Access: PLANNER only (own bookmarks)

GET  /api/v1/bookmarks/{providerId}/status
  Response: 200 OK → { bookmarked: Boolean }
  Access: PLANNER only
```

## 10.7 InquiryController

```
@RestController
@RequestMapping("/api/v1/inquiries")
class InquiryController(private val inquiryService: InquiryService)

POST /api/v1/inquiries
  Body: CreateInquiryRequest
  Response: 201 Created → InquiryResponse
  Access: PLANNER only

GET  /api/v1/inquiries/sent
  Response: 200 OK → List<InquiryResponse>
  Access: PLANNER only (own inquiries)

GET  /api/v1/inquiries/received
  Response: 200 OK → List<InquiryResponse>
  Access: PROVIDER only (inquiries to own profile)

PUT  /api/v1/inquiries/{id}/respond
  Body: RespondToInquiryRequest
  Response: 200 OK → InquiryResponse
  Access: PROVIDER only (own inquiry)

PUT  /api/v1/inquiries/{id}/read
  Response: 200 OK → InquiryResponse
  Access: PROVIDER only (own inquiry)
```

## 10.8 ReviewController

```
@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(private val reviewService: ReviewService)

POST /api/v1/reviews
  Body: CreateReviewRequest
  Response: 201 Created → ReviewResponse
  Access: PLANNER only

PUT  /api/v1/reviews/{id}
  Body: UpdateReviewRequest
  Response: 200 OK → ReviewResponse
  Access: PLANNER only (own review)

DELETE /api/v1/reviews/{id}
  Response: 204 No Content
  Access: PLANNER only (own review)

GET  /api/v1/reviews/mine
  Response: 200 OK → List<ReviewResponse>
  Access: PLANNER only (own reviews)
```

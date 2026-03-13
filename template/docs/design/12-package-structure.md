# 12. Package Structure

```
src/main/kotlin/com/veridyl/eventez/
├── EventezApplication.kt
│
├── config/
│   ├── SecurityConfig.kt
│   └── WebConfig.kt                   // CORS, Jackson config
│
├── entity/
│   ├── AppUser.kt
│   ├── ProviderProfile.kt
│   ├── Service.kt
│   ├── ServiceCategory.kt
│   ├── PortfolioItem.kt
│   ├── Availability.kt
│   ├── Event.kt
│   ├── EventServiceRequirement.kt
│   ├── Bookmark.kt
│   ├── Inquiry.kt
│   └── Review.kt
│
├── entity/enums/
│   ├── UserRole.kt
│   ├── PriceType.kt
│   ├── MediaType.kt
│   ├── AvailabilityStatus.kt
│   ├── EventType.kt
│   ├── EventStatus.kt
│   └── InquiryStatus.kt
│
├── repository/
│   ├── UserRepository.kt
│   ├── ProviderProfileRepository.kt
│   ├── ServiceRepository.kt
│   ├── ServiceCategoryRepository.kt
│   ├── PortfolioItemRepository.kt
│   ├── AvailabilityRepository.kt
│   ├── EventRepository.kt
│   ├── EventServiceRequirementRepository.kt
│   ├── BookmarkRepository.kt
│   ├── InquiryRepository.kt
│   └── ReviewRepository.kt
│
├── specification/
│   ├── ProviderSpecification.kt
│   └── ServiceSpecification.kt
│
├── dto/
│   ├── auth/
│   │   ├── RegisterRequest.kt
│   │   ├── LoginRequest.kt
│   │   ├── UserResponse.kt
│   │   └── AuthResponse.kt
│   ├── provider/
│   │   ├── CreateProviderProfileRequest.kt
│   │   ├── UpdateProviderProfileRequest.kt
│   │   ├── ProviderProfileResponse.kt
│   │   ├── ProviderDetailResponse.kt
│   │   └── ProviderSummaryResponse.kt
│   ├── service/
│   │   ├── CreateServiceRequest.kt
│   │   ├── UpdateServiceRequest.kt
│   │   ├── ServiceResponse.kt
│   │   └── ServiceCategoryResponse.kt
│   ├── event/
│   │   ├── CreateEventRequest.kt
│   │   ├── UpdateEventRequest.kt
│   │   └── EventResponse.kt
│   ├── match/
│   │   ├── MatchRequest.kt
│   │   ├── MatchResponse.kt
│   │   ├── CategoryMatchResult.kt
│   │   └── MatchedProviderResponse.kt
│   ├── portfolio/
│   │   ├── CreatePortfolioItemRequest.kt
│   │   └── PortfolioItemResponse.kt
│   ├── availability/
│   │   ├── SetAvailabilityRequest.kt
│   │   ├── BulkSetAvailabilityRequest.kt
│   │   └── AvailabilityResponse.kt
│   ├── inquiry/
│   │   ├── CreateInquiryRequest.kt
│   │   ├── RespondToInquiryRequest.kt
│   │   └── InquiryResponse.kt
│   ├── review/
│   │   ├── CreateReviewRequest.kt
│   │   ├── UpdateReviewRequest.kt
│   │   └── ReviewResponse.kt
│   ├── bookmark/
│   │   └── BookmarkResponse.kt
│   └── common/
│       ├── ErrorResponse.kt
│       └── PagedResponse.kt
│
├── service/
│   ├── AuthService.kt
│   ├── CustomUserDetailsService.kt
│   ├── ProviderProfileService.kt
│   ├── ServiceService.kt
│   ├── ServiceCategoryService.kt
│   ├── EventService.kt
│   ├── MatchingService.kt
│   ├── PortfolioService.kt
│   ├── AvailabilityService.kt
│   ├── BookmarkService.kt
│   ├── InquiryService.kt
│   └── ReviewService.kt
│
├── controller/
│   ├── AuthController.kt
│   ├── ProviderController.kt
│   ├── CategoryController.kt
│   ├── EventController.kt
│   ├── MatchController.kt
│   ├── BookmarkController.kt
│   ├── InquiryController.kt
│   └── ReviewController.kt
│
└── exception/
    ├── GlobalExceptionHandler.kt
    ├── ResourceNotFoundException.kt
    ├── AccessDeniedException.kt
    └── DuplicateResourceException.kt
```

# EventEz - Implementation Design

> This directory contains the complete technical blueprint for implementing EventEz.
> Each document describes **what** to build and **how** the pieces connect.
> Your job as a developer is to write the Kotlin code that brings this design to life.

## Documents

| # | Document | Description |
|---|----------|-------------|
| 1 | [Dependency Changes](01-dependency-changes.md) | Gradle and application.yaml changes needed before coding |
| 2 | [ER Diagram](02-er-diagram.md) | Visual entity-relationship diagram with all tables and relationships |
| 3 | [Database Schema](03-database-schema.md) | Complete PostgreSQL DDL: tables, indexes, enums, seed data |
| 4 | [Entities](04-entities.md) | All 11 JPA entity classes with annotations and relationships |
| 5 | [Enums](05-enums.md) | All 7 enum types used across the application |
| 6 | [Repositories](06-repositories.md) | All 11 Spring Data JPA repository interfaces |
| 7 | [Specifications](07-specifications.md) | JPA Specifications for dynamic provider and service filtering |
| 8 | [DTOs](08-dtos.md) | All request/response data classes (~30 DTOs) |
| 9 | [Services](09-services.md) | All 11 service classes with method signatures and logic pseudocode |
| 10 | [Controllers](10-controllers.md) | All 8 REST controllers with endpoints, methods, and access rules |
| 11 | [Security](11-security.md) | Spring Security config, access rules matrix, UserDetailsService |
| 12 | [Package Structure](12-package-structure.md) | Complete file tree showing where every class goes |
| 13 | [Validation Rules](13-validation-rules.md) | Field-level and business rule validation tables |
| 14 | [Error Handling](14-error-handling.md) | GlobalExceptionHandler and custom exception classes |

## Implementation Order

For interns, implement in this order to build upon each layer:

| Phase | What | Dependencies |
|-------|------|-------------|
| 1 | Enums, Entities, SQL schema | None |
| 2 | Repositories | Entities |
| 3 | DTOs (all request/response classes) | Enums |
| 4 | Exceptions + GlobalExceptionHandler | DTOs |
| 5 | SecurityConfig + CustomUserDetailsService | UserRepository |
| 6 | AuthService + AuthController | UserRepository, DTOs, Security |
| 7 | ServiceCategoryService + CategoryController | ServiceCategoryRepository |
| 8 | ProviderProfileService + ProviderController (profile CRUD) | ProviderProfileRepository |
| 9 | ServiceService + ProviderController (service endpoints) | ServiceRepository |
| 10 | PortfolioService + ProviderController (portfolio endpoints) | PortfolioItemRepository |
| 11 | AvailabilityService + ProviderController (availability endpoints) | AvailabilityRepository |
| 12 | EventService + EventController | EventRepository |
| 13 | Specifications (ProviderSpecification, ServiceSpecification) | Entities |
| 14 | MatchingService + MatchController | Specifications, multiple repos |
| 15 | BookmarkService + BookmarkController | BookmarkRepository |
| 16 | InquiryService + InquiryController | InquiryRepository |
| 17 | ReviewService + ReviewController | ReviewRepository |

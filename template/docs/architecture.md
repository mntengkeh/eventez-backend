# Architecture Overview

## System Components

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend                           │
│                   (Web Application)                     │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      API Layer                          │
│                   (REST/GraphQL)                        │
└─────────────────────────┬───────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │   Auth   │    │ Matching │    │  Search  │
    │ Service  │    │  Engine  │    │  Service │
    └──────────┘    └──────────┘    └──────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │       Database        │
              └───────────────────────┘
```

## Data Models

### User
```
User
├── id
├── email
├── password_hash
├── user_type (planner | provider)
├── created_at
└── profile → Profile
```

### Provider Profile
```
ProviderProfile
├── id
├── user_id
├── business_name
├── description
├── location (lat, lng, address)
├── service_area_radius
├── contact_info
├── portfolio_images[]
└── services[] → Service
```

### Service
```
Service
├── id
├── provider_id
├── category_id
├── name
├── description
├── price_min
├── price_max
├── price_type (fixed | hourly | per_event)
└── is_active
```

### Service Category
```
ServiceCategory
├── id
├── name (catering, photography, venue, dj, decorator, etc.)
├── icon
└── description
```

### Event (Wizard Input)
```
Event
├── id
├── planner_id
├── event_type
├── event_date
├── location
├── budget_min
├── budget_max
├── guest_count
└── required_services[] → ServiceCategory
```

## Matching Algorithm

```
1. INPUT: Event wizard data (location, budget, services)

2. FILTER:
   ├── Service category matches required_services
   ├── Provider service_area covers event location
   └── Provider price_range overlaps budget

3. RANK:
   ├── Distance score (closer = higher)
   ├── Price fit score (mid-budget = higher)
   ├── Rating score
   └── Response rate score

4. OUTPUT: Sorted providers grouped by category
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Angular |
| Backend | Spring Boot 4 (Kotlin) |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL |
| Auth | Spring Security (session-based, JDBC session store) |
| Storage | S3 / Cloudinary (images) |
| Build | Gradle (Kotlin DSL) |

## API Endpoints (Core)

```
Auth
  POST /auth/register
  POST /auth/login

Providers
  GET    /providers
  GET    /providers/:id
  POST   /providers (create profile)
  PUT    /providers/:id

Services
  GET    /providers/:id/services
  POST   /providers/:id/services
  PUT    /services/:id

Categories
  GET    /categories

Matching
  POST   /match (wizard input → matched providers)

Events
  POST   /events (save wizard data)
  GET    /events/:id
```

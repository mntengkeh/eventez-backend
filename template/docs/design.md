# EventEz - Implementation Design Document

> This document provides a complete technical blueprint for implementing EventEz.
> Each section describes **what** to build and **how** the pieces connect.
> Your job as a developer is to write the Kotlin code that brings this design to life.

---

## Table of Contents

1. [Dependency Changes](#1-dependency-changes)
2. [ER Diagram](#2-er-diagram)
3. [Database Schema (SQL)](#3-database-schema-sql)
4. [Entities](#4-entities)
5. [Enums](#5-enums)
6. [Repositories](#6-repositories)
7. [Specifications](#7-specifications)
8. [DTOs (Request/Response)](#8-dtos-requestresponse)
9. [Services](#9-services)
10. [Controllers](#10-controllers)
11. [Security Configuration](#11-security-configuration)
12. [Package Structure](#12-package-structure)
13. [Validation Rules](#13-validation-rules)
14. [Error Handling](#14-error-handling)

---

## 1. Dependency Changes

The current `build.gradle.kts` uses `spring-boot-starter-jdbc`. To support JPA entities,
repositories, and specifications, **replace** the JDBC starter with JPA:

```kotlin
// REMOVE:
implementation("org.springframework.boot:spring-boot-starter-jdbc")

// ADD:
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
```

Also add the `kotlin("plugin.jpa")` plugin for no-arg constructors:

```kotlin
plugins {
    // ... existing plugins
    kotlin("plugin.jpa") version "2.2.21"
}
```

Update `application.yaml`:

```yaml
spring:
  application:
    name: eventez
  datasource:
    url: jdbc:postgresql://localhost:5432/eventez
    username: ${DB_USERNAME:eventez}
    password: ${DB_PASSWORD:eventez}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  session:
    store-type: jdbc
    jdbc:
      initialize-schema: always

server:
  servlet:
    session:
      timeout: 24h
```

---

## 2. ER Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    ER DIAGRAM                                           │
└─────────────────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │   app_user   │
    │──────────────│
    │ PK id        │
    │    email     │
    │    password  │
    │    role      │──────── enum: PLANNER, PROVIDER
    │    full_name │
    │    phone     │
    │    created_at│
    │    updated_at│
    │    enabled   │
    └──────┬───────┘
           │
           │ 1:0..1 (only for PROVIDER users)
           ▼
    ┌──────────────────┐           ┌───────────────────┐
    │ provider_profile  │           │ service_category   │
    │──────────────────│           │───────────────────│
    │ PK id            │           │ PK id              │
    │ FK user_id (UQ)  │           │    name             │
    │    business_name │           │    slug (UQ)        │
    │    description   │           │    icon             │
    │    address       │           │    description      │
    │    city          │           └─────────┬───────────┘
    │    state         │                     │
    │    zip_code      │                     │ 1:*
    │    latitude      │                     │
    │    longitude     │                     ▼
    │    service_radius│           ┌───────────────────┐
    │    website       │           │     service        │
    │    verified      │           │───────────────────│
    │    avg_rating    │     *:1   │ PK id              │
    │    review_count  │◄─────────│ FK provider_id     │
    │    response_rate │           │ FK category_id     │───── *:1 ──► service_category
    │    created_at    │           │    name             │
    │    updated_at    │           │    description      │
    └──┬───┬───┬───────┘           │    price_min        │
       │   │   │                   │    price_max        │
       │   │   │                   │    price_type       │──── enum: FIXED, HOURLY,
       │   │   │                   │    active           │          PER_EVENT, CUSTOM
       │   │   │                   │    created_at       │
       │   │   │                   │    updated_at       │
       │   │   │                   └───────────────────┘
       │   │   │
       │   │   │ 1:*
       │   │   └──────────────────────┐
       │   │                          ▼
       │   │                ┌───────────────────┐
       │   │                │  portfolio_item    │
       │   │                │───────────────────│
       │   │                │ PK id              │
       │   │                │ FK provider_id     │
       │   │                │    media_url       │
       │   │                │    media_type      │──── enum: IMAGE, VIDEO
       │   │                │    caption         │
       │   │                │    display_order   │
       │   │                │    created_at      │
       │   │                └───────────────────┘
       │   │
       │   │ 1:*
       │   └──────────────────────────┐
       │                              ▼
       │                    ┌───────────────────┐
       │                    │   availability     │
       │                    │───────────────────│
       │                    │ PK id              │
       │                    │ FK provider_id     │
       │                    │    date            │
       │                    │    status          │──── enum: AVAILABLE, BOOKED,
       │                    │    note            │          TENTATIVE, BLOCKED
       │                    └───────────────────┘
       │
       │  (target of bookmarks, inquiries, reviews)
       │
       ▼

    ┌──────────────────┐
    │     event         │  (created by PLANNER users)
    │──────────────────│
    │ PK id            │
    │ FK planner_id    │───── *:1 ──► app_user (role=PLANNER)
    │    title         │
    │    event_type    │──── enum: WEDDING, CORPORATE, BIRTHDAY,
    │    event_date    │          CONFERENCE, PARTY, SOCIAL,
    │    location      │          FUNDRAISER, OTHER
    │    city          │
    │    state         │
    │    latitude      │
    │    longitude     │
    │    budget_min    │
    │    budget_max    │
    │    guest_count   │
    │    description   │
    │    status        │──── enum: DRAFT, ACTIVE, COMPLETED, CANCELLED
    │    created_at    │
    │    updated_at    │
    └──────┬───────────┘
           │
           │ 1:*
           ▼
    ┌─────────────────────────┐
    │ event_service_requirement│
    │─────────────────────────│
    │ PK id                    │
    │ FK event_id              │
    │ FK category_id           │───── *:1 ──► service_category
    │    note                  │
    └─────────────────────────┘


    ┌──────────────────┐
    │    bookmark       │
    │──────────────────│
    │ PK id            │
    │ FK planner_id    │───── *:1 ──► app_user (role=PLANNER)
    │ FK provider_id   │───── *:1 ──► provider_profile
    │    created_at    │
    │                  │  UQ(planner_id, provider_id)
    └──────────────────┘


    ┌──────────────────┐
    │    inquiry        │
    │──────────────────│
    │ PK id            │
    │ FK planner_id    │───── *:1 ──► app_user (role=PLANNER)
    │ FK provider_id   │───── *:1 ──► provider_profile
    │ FK event_id      │───── *:0..1 ► event (optional)
    │    message       │
    │    status        │──── enum: PENDING, READ, RESPONDED, CLOSED
    │    response      │
    │    responded_at  │
    │    created_at    │
    └──────────────────┘


    ┌──────────────────┐
    │     review        │
    │──────────────────│
    │ PK id            │
    │ FK planner_id    │───── *:1 ──► app_user (role=PLANNER)
    │ FK provider_id   │───── *:1 ──► provider_profile
    │ FK event_id      │───── *:0..1 ► event (optional)
    │    rating        │  (1-5)
    │    title         │
    │    comment       │
    │    created_at    │
    │    updated_at    │
    │                  │  UQ(planner_id, provider_id)
    └──────────────────┘
```

### Relationship Summary

| Relationship | Type | Description |
|---|---|---|
| `app_user` → `provider_profile` | 1:0..1 | Only PROVIDER users have a profile |
| `app_user` → `event` | 1:* | A planner creates many events |
| `provider_profile` → `service` | 1:* | A provider offers many services |
| `provider_profile` → `portfolio_item` | 1:* | A provider has many portfolio items |
| `provider_profile` → `availability` | 1:* | A provider has many availability entries |
| `service` → `service_category` | *:1 | Each service belongs to one category |
| `event` → `event_service_requirement` | 1:* | An event needs many service categories |
| `event_service_requirement` → `service_category` | *:1 | Each requirement is for one category |
| `app_user` → `bookmark` → `provider_profile` | *:* | Planners bookmark many providers |
| `app_user` → `inquiry` → `provider_profile` | *:* | Planners send inquiries to providers |
| `app_user` → `review` → `provider_profile` | *:* | Planners review providers (one review per provider) |

---

## 3. Database Schema (SQL)

Create a migration file at `src/main/resources/db/migration/V1__init_schema.sql` (if using Flyway)
or `src/main/resources/schema.sql`:

```sql
-- ============================================================
-- ENUMS (PostgreSQL native enums)
-- ============================================================

CREATE TYPE user_role AS ENUM ('PLANNER', 'PROVIDER');
CREATE TYPE price_type AS ENUM ('FIXED', 'HOURLY', 'PER_EVENT', 'CUSTOM');
CREATE TYPE media_type AS ENUM ('IMAGE', 'VIDEO');
CREATE TYPE availability_status AS ENUM ('AVAILABLE', 'BOOKED', 'TENTATIVE', 'BLOCKED');
CREATE TYPE event_type AS ENUM ('WEDDING', 'CORPORATE', 'BIRTHDAY', 'CONFERENCE', 'PARTY', 'SOCIAL', 'FUNDRAISER', 'OTHER');
CREATE TYPE event_status AS ENUM ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED');
CREATE TYPE inquiry_status AS ENUM ('PENDING', 'READ', 'RESPONDED', 'CLOSED');

-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            user_role NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE service_category (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    icon            VARCHAR(100),
    description     TEXT
);

CREATE TABLE provider_profile (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
    business_name   VARCHAR(255) NOT NULL,
    description     TEXT,
    address         VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    zip_code        VARCHAR(20),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    service_radius  INTEGER DEFAULT 50,           -- in kilometers
    website         VARCHAR(500),
    verified        BOOLEAN NOT NULL DEFAULT FALSE,
    avg_rating      DOUBLE PRECISION DEFAULT 0.0,
    review_count    INTEGER DEFAULT 0,
    response_rate   DOUBLE PRECISION DEFAULT 0.0, -- percentage 0-100
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE service (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES service_category(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price_min       NUMERIC(12, 2),
    price_max       NUMERIC(12, 2),
    price_type      price_type NOT NULL DEFAULT 'FIXED',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE portfolio_item (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    media_url       VARCHAR(1000) NOT NULL,
    media_type      media_type NOT NULL DEFAULT 'IMAGE',
    caption         VARCHAR(500),
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE availability (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    status          availability_status NOT NULL DEFAULT 'AVAILABLE',
    note            VARCHAR(500),

    UNIQUE (provider_id, date)
);

CREATE TABLE event (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    event_type      event_type NOT NULL,
    event_date      DATE NOT NULL,
    location        VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    budget_min      NUMERIC(12, 2),
    budget_max      NUMERIC(12, 2),
    guest_count     INTEGER,
    description     TEXT,
    status          event_status NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE event_service_requirement (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES service_category(id),
    note            VARCHAR(500),

    UNIQUE (event_id, category_id)
);

CREATE TABLE bookmark (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE (planner_id, provider_id)
);

CREATE TABLE inquiry (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    event_id        BIGINT REFERENCES event(id) ON DELETE SET NULL,
    message         TEXT NOT NULL,
    status          inquiry_status NOT NULL DEFAULT 'PENDING',
    response        TEXT,
    responded_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE review (
    id              BIGSERIAL PRIMARY KEY,
    planner_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    provider_id     BIGINT NOT NULL REFERENCES provider_profile(id) ON DELETE CASCADE,
    event_id        BIGINT REFERENCES event(id) ON DELETE SET NULL,
    rating          SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title           VARCHAR(255),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE (planner_id, provider_id)
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_provider_profile_city ON provider_profile(city);
CREATE INDEX idx_provider_profile_location ON provider_profile(latitude, longitude);
CREATE INDEX idx_service_provider ON service(provider_id);
CREATE INDEX idx_service_category ON service(category_id);
CREATE INDEX idx_service_active ON service(active) WHERE active = TRUE;
CREATE INDEX idx_event_planner ON event(planner_id);
CREATE INDEX idx_event_date ON event(event_date);
CREATE INDEX idx_availability_provider_date ON availability(provider_id, date);
CREATE INDEX idx_inquiry_provider ON inquiry(provider_id);
CREATE INDEX idx_inquiry_planner ON inquiry(planner_id);
CREATE INDEX idx_review_provider ON review(provider_id);
CREATE INDEX idx_bookmark_planner ON bookmark(planner_id);

-- ============================================================
-- SEED DATA: Service Categories
-- ============================================================

INSERT INTO service_category (name, slug, icon, description) VALUES
    ('Catering', 'catering', 'utensils', 'Food and beverage services'),
    ('Photography', 'photography', 'camera', 'Professional photography services'),
    ('Videography', 'videography', 'video', 'Professional video recording services'),
    ('Venue', 'venue', 'building', 'Event venues and spaces'),
    ('DJ & Music', 'dj-music', 'music', 'DJs, bands, and musical entertainment'),
    ('Decoration', 'decoration', 'palette', 'Event decoration and styling'),
    ('Florist', 'florist', 'flower', 'Floral arrangements and bouquets'),
    ('Wedding Cake', 'wedding-cake', 'cake', 'Custom cakes and desserts'),
    ('Transportation', 'transportation', 'car', 'Limo, shuttle, and transport services'),
    ('Event Planning', 'event-planning', 'clipboard', 'Full event coordination services'),
    ('Lighting & Sound', 'lighting-sound', 'lightbulb', 'Professional AV equipment and setup'),
    ('Security', 'security', 'shield', 'Event security personnel'),
    ('Rentals', 'rentals', 'box', 'Tables, chairs, tents, and equipment rental'),
    ('Hair & Makeup', 'hair-makeup', 'scissors', 'Beauty and styling services'),
    ('Entertainment', 'entertainment', 'star', 'Performers, comedians, magicians, etc.');
```

---

## 4. Entities

All entities go under `com.veridyl.eventez.entity`.

> **Conventions:**
> - Use `@Entity` + `@Table(name = "...")` annotations.
> - Every entity has a `Long` primary key with `@GeneratedValue(strategy = IDENTITY)`.
> - Use `@Enumerated(EnumType.STRING)` for enum fields.
> - Use `Instant` or `LocalDate`/`LocalDateTime` for timestamps.
> - Relationships use lazy loading by default (`FetchType.LAZY`).

### 4.1 AppUser

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

### 4.2 ProviderProfile

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

### 4.3 ServiceCategory

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

### 4.4 Service

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

### 4.5 PortfolioItem

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

### 4.6 Availability

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

### 4.7 Event

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

### 4.8 EventServiceRequirement

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

### 4.9 Bookmark

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

### 4.10 Inquiry

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

### 4.11 Review

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

---

## 5. Enums

All enums go under `com.veridyl.eventez.entity.enums`.

```kotlin
enum class UserRole { PLANNER, PROVIDER }

enum class PriceType { FIXED, HOURLY, PER_EVENT, CUSTOM }

enum class MediaType { IMAGE, VIDEO }

enum class AvailabilityStatus { AVAILABLE, BOOKED, TENTATIVE, BLOCKED }

enum class EventType { WEDDING, CORPORATE, BIRTHDAY, CONFERENCE, PARTY, SOCIAL, FUNDRAISER, OTHER }

enum class EventStatus { DRAFT, ACTIVE, COMPLETED, CANCELLED }

enum class InquiryStatus { PENDING, READ, RESPONDED, CLOSED }
```

---

## 6. Repositories

All repositories go under `com.veridyl.eventez.repository`.
Extend `JpaRepository<Entity, Long>` and `JpaSpecificationExecutor<Entity>` where filtering is needed.

### 6.1 UserRepository

```kotlin
interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByEmail(email: String): AppUser?
    fun existsByEmail(email: String): Boolean
}
```

### 6.2 ProviderProfileRepository

```kotlin
interface ProviderProfileRepository :
    JpaRepository<ProviderProfile, Long>,
    JpaSpecificationExecutor<ProviderProfile> {

    fun findByUserId(userId: Long): ProviderProfile?
    fun findByCity(city: String): List<ProviderProfile>
    fun findByVerifiedTrue(): List<ProviderProfile>
}
```

### 6.3 ServiceRepository

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

### 6.4 ServiceCategoryRepository

```kotlin
interface ServiceCategoryRepository : JpaRepository<ServiceCategory, Long> {
    fun findBySlug(slug: String): ServiceCategory?
    fun findAllByIdIn(ids: List<Long>): List<ServiceCategory>
}
```

### 6.5 PortfolioItemRepository

```kotlin
interface PortfolioItemRepository : JpaRepository<PortfolioItem, Long> {
    fun findByProviderIdOrderByDisplayOrderAsc(providerId: Long): List<PortfolioItem>
    fun countByProviderId(providerId: Long): Long
}
```

### 6.6 AvailabilityRepository

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

### 6.7 EventRepository

```kotlin
interface EventRepository : JpaRepository<Event, Long> {
    fun findByPlannerId(plannerId: Long): List<Event>
    fun findByPlannerIdAndStatus(plannerId: Long, status: EventStatus): List<Event>
}
```

### 6.8 EventServiceRequirementRepository

```kotlin
interface EventServiceRequirementRepository : JpaRepository<EventServiceRequirement, Long> {
    fun findByEventId(eventId: Long): List<EventServiceRequirement>
}
```

### 6.9 BookmarkRepository

```kotlin
interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun findByPlannerId(plannerId: Long): List<Bookmark>
    fun findByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Bookmark?
    fun existsByPlannerIdAndProviderId(plannerId: Long, providerId: Long): Boolean
    fun deleteByPlannerIdAndProviderId(plannerId: Long, providerId: Long)
}
```

### 6.10 InquiryRepository

```kotlin
interface InquiryRepository : JpaRepository<Inquiry, Long> {
    fun findByPlannerId(plannerId: Long): List<Inquiry>
    fun findByProviderId(providerId: Long): List<Inquiry>
    fun findByProviderIdAndStatus(providerId: Long, status: InquiryStatus): List<Inquiry>
    fun countByProviderIdAndStatus(providerId: Long, status: InquiryStatus): Long
}
```

### 6.11 ReviewRepository

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

---

## 7. Specifications

All specifications go under `com.veridyl.eventez.specification`.
Use Spring Data JPA `Specification<T>` for dynamic query building.

### 7.1 ProviderSpecification

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

### 7.2 ServiceSpecification

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

---

## 8. DTOs (Request/Response)

All DTOs go under `com.veridyl.eventez.dto`. Use `data class` for all DTOs.

### 8.1 Auth DTOs

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

### 8.2 Provider DTOs

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

### 8.3 Service DTOs

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

### 8.4 Event DTOs

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

### 8.5 Matching DTOs

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

### 8.6 Portfolio DTOs

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

### 8.7 Availability DTOs

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

### 8.8 Inquiry DTOs

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

### 8.9 Review DTOs

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

### 8.10 Bookmark DTOs

```kotlin
data class BookmarkResponse(
    val id: Long,
    val provider: ProviderSummaryResponse,
    val createdAt: Instant
)
```

### 8.11 Common DTOs

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

---

## 9. Services

All services go under `com.veridyl.eventez.service`.
Each service class should be annotated with `@Service` and use constructor injection.

### 9.1 AuthService

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

### 9.2 ProviderProfileService

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

### 9.3 ServiceService (or ProviderServiceService)

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

### 9.4 ServiceCategoryService

```
ServiceCategoryService
├── getAllCategories(): List<ServiceCategoryResponse>
├── getCategoryById(id: Long): ServiceCategoryResponse
└── getCategoryBySlug(slug: String): ServiceCategoryResponse
```

### 9.5 EventService

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

### 9.6 MatchingService

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

**Relevance Score Breakdown:**

| Factor | Max Points | Calculation |
|--------|-----------|-------------|
| Distance | 30 | `30 * (1 - distance/maxDistance)`, clamped to 0 |
| Price Fit | 25 | `25 * (1 - abs(midPrice - midBudget) / budgetRange)` |
| Rating | 25 | `25 * (avgRating / 5.0)` |
| Response Rate | 10 | `10 * (responseRate / 100.0)` |
| Review Volume | 10 | `min(10, reviewCount)` — caps at 10 reviews |
| **Total** | **100** | |

### 9.7 PortfolioService

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

### 9.8 AvailabilityService

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

### 9.9 BookmarkService

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

### 9.10 InquiryService

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

### 9.11 ReviewService

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

---

## 10. Controllers

All controllers go under `com.veridyl.eventez.controller`.
Use `@RestController` and `@RequestMapping("/api/v1/...")`.

### 10.1 AuthController

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

### 10.2 ProviderController

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

### 10.3 CategoryController

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

### 10.4 EventController

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

### 10.5 MatchController

```
@RestController
@RequestMapping("/api/v1/match")
class MatchController(private val matchingService: MatchingService)

POST /api/v1/match
  Body: MatchRequest
  Response: 200 OK → MatchResponse
  Access: PLANNER only
```

### 10.6 BookmarkController

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

### 10.7 InquiryController

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

### 10.8 ReviewController

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

---

## 11. Security Configuration

Create `com.veridyl.eventez.config.SecurityConfig`.

### Access Rules Summary

| Pattern | Method | Access |
|---------|--------|--------|
| `/api/v1/auth/register` | POST | Public |
| `/api/v1/auth/login` | POST | Public |
| `/api/v1/auth/logout` | POST | Authenticated |
| `/api/v1/auth/me` | GET | Authenticated |
| `/api/v1/categories/**` | GET | Public |
| `/api/v1/providers` | GET | Public |
| `/api/v1/providers/{id}` | GET | Public |
| `/api/v1/providers/{id}/services` | GET | Public |
| `/api/v1/providers/{id}/portfolio` | GET | Public |
| `/api/v1/providers/{id}/availability` | GET | Public |
| `/api/v1/providers/{id}/reviews` | GET | Public |
| `/api/v1/providers/profile` | POST/PUT | PROVIDER |
| `/api/v1/providers/*/services` | POST | PROVIDER |
| `/api/v1/providers/services/*` | PUT/DELETE | PROVIDER |
| `/api/v1/providers/*/portfolio` | POST | PROVIDER |
| `/api/v1/providers/portfolio/*` | DELETE | PROVIDER |
| `/api/v1/providers/*/availability` | PUT | PROVIDER |
| `/api/v1/events/**` | ALL | PLANNER |
| `/api/v1/match` | POST | PLANNER |
| `/api/v1/bookmarks/**` | ALL | PLANNER |
| `/api/v1/inquiries` | POST | PLANNER |
| `/api/v1/inquiries/sent` | GET | PLANNER |
| `/api/v1/inquiries/received` | GET | PROVIDER |
| `/api/v1/inquiries/*/respond` | PUT | PROVIDER |
| `/api/v1/inquiries/*/read` | PUT | PROVIDER |
| `/api/v1/reviews` | POST | PLANNER |
| `/api/v1/reviews/*` | PUT/DELETE | PLANNER |

### Security Config Skeleton

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }  // Disable for API usage; enable if using Thymeleaf forms
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers", "/api/v1/providers/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/services").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/portfolio").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/availability").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/providers/*/reviews").permitAll()
                    // ... remaining rules per table above
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.status = 401
                    response.contentType = "application/json"
                    response.writer.write("""{"status":401,"error":"Unauthorized","message":"Authentication required"}""")
                }
                ex.accessDeniedHandler { _, response, _ ->
                    response.status = 403
                    response.contentType = "application/json"
                    response.writer.write("""{"status":403,"error":"Forbidden","message":"Access denied"}""")
                }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

### CustomUserDetailsService

```kotlin
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found: $email")

        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .roles(user.role.name)       // yields ROLE_PLANNER or ROLE_PROVIDER
            .disabled(!user.enabled)
            .build()
    }
}
```

---

## 12. Package Structure

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

---

## 13. Validation Rules

### Entity-Level Validation (applied via DTO annotations)

| Field | Rule |
|-------|------|
| `email` | Must be valid email format, unique |
| `password` | Minimum 8 characters |
| `fullName` | Not blank |
| `businessName` | Not blank |
| `service.name` | Not blank |
| `service.categoryId` | Must reference existing category |
| `event.title` | Not blank |
| `event.eventType` | Must be valid enum value |
| `event.eventDate` | Must be today or in the future |
| `review.rating` | Integer between 1 and 5 |
| `inquiry.message` | Not blank |
| `inquiry.response` | Not blank (when responding) |
| `portfolioItem.mediaUrl` | Not blank, valid URL |
| `availability.date` | Not null |
| `availability.status` | Must be valid enum value |
| `priceMin / priceMax` | If both provided, priceMin <= priceMax |
| `budgetMin / budgetMax` | If both provided, budgetMin <= budgetMax |

### Business Rules (enforced in Service layer)

| Rule | Where | Error |
|------|-------|-------|
| User email must be unique | AuthService.register | 409 Conflict |
| Only PROVIDER users can create a profile | ProviderProfileService | 403 Forbidden |
| A provider can have only one profile | ProviderProfileService | 409 Conflict |
| Only profile owner can edit/delete | All provider services | 403 Forbidden |
| Only PLANNER users can create events | EventService | 403 Forbidden |
| Only event owner can view/edit/delete events | EventService | 403 Forbidden |
| One review per planner per provider | ReviewService | 409 Conflict |
| One bookmark per planner per provider | BookmarkService | 409 Conflict |
| Only profile owner can respond to inquiries | InquiryService | 403 Forbidden |
| Event date must be in the future for ACTIVE status | EventService | 400 Bad Request |

---

## 14. Error Handling

Create `com.veridyl.eventez.exception.GlobalExceptionHandler`:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404).body(
            ErrorResponse(404, "Not Found", ex.message ?: "Resource not found")
        )
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleConflict(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(409).body(
            ErrorResponse(409, "Conflict", ex.message ?: "Resource already exists")
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(403).body(
            ErrorResponse(403, "Forbidden", ex.message ?: "Access denied")
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(400).body(
            ErrorResponse(400, "Bad Request", message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(500).body(
            ErrorResponse(500, "Internal Server Error", "An unexpected error occurred")
        )
    }
}
```

### Custom Exceptions

```kotlin
class ResourceNotFoundException(message: String) : RuntimeException(message)
class DuplicateResourceException(message: String) : RuntimeException(message)
class AccessDeniedException(message: String) : RuntimeException(message)
```

---

## Implementation Order (Suggested)

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
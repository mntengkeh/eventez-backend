# 2. ER Diagram

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

## Relationship Summary

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

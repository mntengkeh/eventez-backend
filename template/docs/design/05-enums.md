# 5. Enums

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

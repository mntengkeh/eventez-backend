# 13. Validation Rules

## Entity-Level Validation (applied via DTO annotations)

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

## Business Rules (enforced in Service layer)

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

# API Integration Plan – Moonlight Stays

Complete plan to integrate all backend APIs with the frontend.

---

## Current Status Overview

| Category | Integrated | Missing | UI Usage |
|----------|------------|---------|----------|
| Auth | ✅ All | - | AuthModal, Login, AuthContext |
| Hotel Browse | ✅ Search, Info | - | Home, Hotel Detail |
| Bookings | ✅ All | - | Bookings page, BookingModal |
| Reviews | ✅ All | - | Hotel Detail |
| User | ✅ All | - | Profile, Favorites |
| AI Trip Planner | ✅ All | - | TripPlanner page, AI itinerary generation |
| Hotel Admin | ✅ All | - | Admin page |
| Room Admin | ✅ All | - | Admin page |
| Promo | ✅ createPromo | - | Admin page |
| Image Upload | ✅ uploadImage | - | Admin (hotel/room photos) |

---

## Phase 1: Add Missing API Methods ✅ COMPLETE

### 1.1 Booking APIs
- [x] **GET /bookings/{bookingId}** – `api.getBookingById(bookingId)`  
  - **Use case:** Booking detail page, show full booking info before payment/cancel

### 1.2 Hotel Admin APIs
- [x] **GET /admin/hotels/{hotelId}** – `api.getHotelById(hotelId)`  
  - **Use case:** Admin hotel edit form, pre-fill data
- [x] **PUT /admin/hotels/{hotelId}** – `api.updateHotel(hotelId, data)`  
  - **Use case:** Admin edit hotel
- [x] **DELETE /admin/hotels/{hotelId}** – `api.deleteHotel(hotelId)`  
  - **Use case:** Admin delete hotel

### 1.3 Room Admin APIs
- [x] **GET /admin/hotels/{hotelId}/rooms** – `api.getHotelRooms(hotelId)`  
  - **Use case:** Admin list rooms, edit/delete
- [x] **GET /admin/hotels/{hotelId}/rooms/{roomId}** – `api.getRoomById(hotelId, roomId)`  
  - **Use case:** Admin edit room form
- [x] **DELETE /admin/hotels/{hotelId}/rooms/{roomId}** – `api.deleteRoom(hotelId, roomId)`  
  - **Use case:** Admin delete room

### 1.4 Image Upload API
- [x] **POST /upload/image** – `api.uploadImage(file: File)`  
  - **Use case:** Hotel photos, room photos in admin  
  - **Note:** Multipart/form-data, returns `{ url: string }`

---

## Phase 2: Search API Enhancements ✅ COMPLETE

Current search uses: `city`, `checkInDate`, `endDate`, `roomsCount`, `page`, `size`.

Backend `HotelSearchRequest` also supports:
- [x] **minPrice**, **maxPrice** – Add to HeroSearch
- [x] **roomType** – Filter by room type
- [x] **amenity** – Filter by amenity

**Action:** Extend `HeroSearch` with optional filters (collapsible “More filters”).

---

## Phase 3: UI Integration Tasks ✅ COMPLETE

### 3.1 Admin Dashboard
- [x] **Edit Hotel** – Use `getHotelById` + `updateHotel`, add Edit button and form
- [x] **Delete Hotel** – Use `deleteHotel`, add Delete with confirmation
- [x] **List Rooms** – Use `getHotelRooms` to show rooms per hotel
- [x] **Delete Room** – Use `deleteRoom`, add Delete with confirmation (Edit Room: backend has no updateRoom)
- [x] **Image Upload** – Use `uploadImage` for hotel/room photos in create/edit forms

### 3.2 Bookings Page
- [x] **Booking Detail** – Use `getBookingById` for a booking detail view/modal
- [x] **Cancel** – Clearer confirmation message

### 3.3 Hotel Create/Edit (Admin)
- [x] **Photos** – Add image upload for hotel photos (array of URLs)
- [x] **Room Photos** – Add image upload when creating rooms

---

## Phase 4: API Consistency & Robustness ✅ COMPLETE

### 4.1 Auth & Token Handling
- [x] Refresh token on 401 (already done)
- [x] `credentials: 'include'` for cookie-based APIs (already done)
- [x] `getMyBookings` uses `fetchApi` for 401 retry

### 4.2 Error Handling
- [x] Centralized error toast (react-hot-toast + `showApiError` in `lib/toast.ts`)
- [x] Consistent error messages from API responses (used in catch blocks app-wide)

### 4.3 Response Parsing
- [x] Handle `ApiResponse` wrapper (`data` field) via `fetchApi` return `json.data ?? json`
- [x] Handle pagination (`content`, `totalPages`) for `getMyBookings`, `getHotelReviews`

---

## Phase 5: UX Polish & Success Feedback ✅ COMPLETE

### 5.1 Success Toasts
- [x] Auth: Sign in / Account created
- [x] Admin: Hotel created/updated/deleted, Room created/deleted, Promo created, Surge updated, Toggle status
- [x] Bookings: Booking cancelled
- [x] Profile: Profile updated
- [x] Favorites: Added/removed (home + hotel detail + favorites page)
- [x] Hotel Detail: Review added

### 5.2 Plan Maintenance
- [x] Update status overview table (all categories ✅)

---

## Implementation Order

1. **Phase 1.1–1.4** – Add missing API methods in `api.ts`
2. **Phase 1.4** – Implement `uploadImage` (needed for admin)
3. **Phase 3.1** – Admin: Edit/Delete hotel, List/Delete rooms, Image upload
4. **Phase 3.2** – Bookings: Optional booking detail view
5. **Phase 2** – Search filters (optional, lower priority)
6. **Phase 4** – Consistency and robustness
7. **Phase 5** – UX polish (success toasts)

---

## API Reference Quick Map

| Backend Endpoint | Frontend Method | Status |
|------------------|-----------------|--------|
| POST /auth/signup | api.signup | ✅ |
| POST /auth/admin/signup | api.adminSignup | ✅ |
| POST /auth/login | api.login | ✅ |
| POST /auth/refresh | api.refreshToken (internal) | ✅ |
| POST /auth/logout | api.logout | ✅ |
| POST /hotels/search | api.searchHotels | ✅ |
| GET /hotels/{id}/info | api.getHotelInfo | ✅ |
| POST /bookings/init | api.initBooking | ✅ |
| POST /bookings/{id}/addGuests | api.addGuests | ✅ |
| GET /bookings/{id} | api.getBookingById | ✅ |
| GET /bookings/myBookings | api.getMyBookings | ✅ |
| POST /bookings/{id}/cancel | api.cancelBooking | ✅ |
| POST /bookings/{id}/payments | api.initiatePayment | ✅ |
| POST /hotels/{id}/reviews | api.addReview | ✅ |
| GET /hotels/{id}/reviews | api.getHotelReviews | ✅ |
| GET /hotels/{id}/reviews/average | api.getHotelAverageRating | ✅ |
| GET /users/profile | api.getProfile | ✅ |
| PATCH /users/profile | api.updateProfile | ✅ |
| GET /users/favorites | api.getFavoriteHotels | ✅ |
| POST /users/favorites/{id} | api.addToFavorites | ✅ |
| DELETE /users/favorites/{id} | api.removeFromFavorites | ✅ |
| POST /admin/hotels | api.createHotel | ✅ |
| GET /admin/hotels | api.getMyHotels | ✅ |
| GET /admin/hotels/{id} | api.getHotelById | ✅ |
| PUT /admin/hotels/{id} | api.updateHotel | ✅ |
| DELETE /admin/hotels/{id} | api.deleteHotel | ✅ |
| PATCH /admin/hotels/{id} | api.toggleHotelStatus | ✅ |
| PATCH /admin/hotels/{id}/surge | api.updateSurgeFactor | ✅ |
| POST /admin/hotels/{id}/rooms | api.createRoom | ✅ |
| GET /admin/hotels/{id}/rooms | api.getHotelRooms | ✅ |
| GET /admin/hotels/{id}/rooms/{roomId} | api.getRoomById | ✅ |
| DELETE /admin/hotels/{id}/rooms/{roomId} | api.deleteRoom | ✅ |
| POST /admin/promocodes | api.createPromoCode | ✅ |
| POST /upload/image | api.uploadImage | ✅ |
| POST /ai/trip-plan | api.generateTripPlan | ✅ |

---

## Notes

- **Webhook** (`POST /webhooks/payment`) – Backend-only; Stripe calls it. No frontend integration.
- **Context path** – All APIs are under `/api/v1` (Next.js rewrites proxy to backend).
- **Image URLs** – Upload returns path like `/images/filename.jpg`; use relative path for display.

---

## ✅ Integration Complete

All 5 phases are implemented. The frontend is fully integrated with the backend API:

- **Phases 1–4**: API methods, search filters, admin UI, error handling, pagination
- **Phase 5**: Success toasts for all key user actions

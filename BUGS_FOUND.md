# Bugs Found: Admin Hotel Creation & Search Test

**Test Date:** March 1, 2026  
**Credentials:** snehil7542@gmail.com / pass (HOTEL_MANAGER)  
**Deployed URL:** https://main.d30tl6vi1qydms.amplifyapp.com

---

## Test Flow Summary

| Step | Action | Result |
|------|--------|--------|
| 1 | Login | ✅ Success |
| 2 | Create hotel "Test Hotel Mumbai" | ✅ Created (ID: 4) |
| 3 | Add room (Deluxe, ₹2500) | ✅ Success |
| 4 | Activate hotel (PATCH) | ✅ Success |
| 5 | Search hotels (POST) | ✅ Test Hotel Mumbai appears |
| 6 | Search hotels (GET with query params) | ❌ 500 Internal Server Error |

---

## Bugs Identified

### 1. **CRITICAL: Password Exposed in API Response** ✅ FIXED

**Location:** `User` entity – `password` field serialized in JSON

**Description:** When hotel search returns `HotelPriceDto`, the nested `hotel.owner` (User) includes the hashed password in the response. Even hashed passwords must never be exposed.

**Fix Applied:** Added `@JsonIgnore` on `User.password` in `User.java`.

---

### 2. **GET /api/v1/hotels/search Returns 500**

**Location:** `HotelBrowseController.searchHotelsGet`

**Description:** Calling search with GET and query params like `?city=Mumbai&checkIn=2025-03-15&checkOut=2025-03-17&guests=2` returns 500. The controller expects `checkInDate` and `endDate`, not `checkIn`/`checkOut`. Clients using `checkIn`/`checkOut` (e.g. from URL builders) will fail.

**Recommendation:** Support both param naming conventions or document the correct params clearly. Consider adding `checkIn`/`checkOut` as aliases for `checkInDate`/`endDate`.

---

### 3. **Search with Past Dates Returns Empty**

**Description:** Search with `checkInDate: 2025-03-15` returns `[]` because inventory is only created from `LocalDate.now()` onward (1 year). Past dates have no inventory.

**Status:** Expected behavior, but the UI could validate dates and show a clearer message when dates are in the past.

---

### 4. **Search Returns 500 – LazyInitializationException (ROOT CAUSE)** ✅ FIXED

**Location:** `User.favoriteHotels` – lazy collection accessed during JSON serialization

**Description:** When search returns `HotelPriceDto` with `Hotel`, Jackson serializes `hotel.owner` (User). Accessing `User.favoriteHotels` (lazy `@ManyToMany`) triggers `LazyInitializationException` because the Hibernate session is already closed. This causes the search endpoint to return 500.

**Fix Applied:** Added `@JsonIgnore` on `User.favoriteHotels`. The search response does not need the owner's favorite hotels; the frontend uses `/users/favorites` for that.

---

### 5. **Large Response Payload (~329 KB)**

**Description:** Search response includes full `hotel.owner` (User). With `favoriteHotels` now excluded, payload size should decrease.

**Recommendation:** Consider using a DTO for hotel search that excludes `owner` entirely if the browse UI does not need it.

---

## Notes

- **POST** `/api/v1/hotels/search` works correctly with body: `{"city":"Mumbai","checkInDate":"2026-03-15","endDate":"2026-03-17","roomsCount":1}`
- New hotels are created with `active: false`; they must be activated via PATCH before appearing in search.
- Hotels need at least one room to be activated.

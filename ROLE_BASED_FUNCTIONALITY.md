# Backend Functionality by Role

Based on the Spring Boot backend security configuration and controllers.

---

## Roles

| Role | Description |
|------|-------------|
| **GUEST** | Regular user who can browse, book, and manage their stays |
| **HOTEL_MANAGER** | Hotel owner/admin who can manage hotels, rooms, and promo codes |

---

## Public (No Login Required)

Anyone can access these endpoints without authentication:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/signup` | POST | Create new **Guest** account |
| `/auth/admin/signup` | POST | Create new **Hotel Manager** account (typically used via Swagger/Postman) |
| `/auth/login` | POST | Login (Guest or Hotel Manager) |
| `/auth/refresh` | POST | Refresh access token (uses cookie) |
| `/auth/logout` | POST | Logout (clears refresh token cookie) |
| `/hotels/search` | POST | Search hotels by city, dates, rooms, filters |
| `/hotels/Search` | GET | Same as above (legacy, use POST) |
| `/hotels/{hotelId}/info` | GET | Get hotel details + rooms |
| `/images/**` | GET | Serve uploaded images |
| `/upload/image` | POST | Upload image |
| `/webhooks/payment` | POST | Stripe payment webhook (server-to-server) |
| `/v3/api-docs/**`, `/swagger-ui/**` | GET | API documentation |

---

## GUEST (Authenticated)

Requires login. Available to users with role **GUEST** (or HOTEL_MANAGER, since they are also authenticated):

| Endpoint | Method | Description |
|----------|--------|-------------|
| **Bookings** | | |
| `/bookings/init` | POST | Initialize a new booking |
| `/bookings/{bookingId}/addGuests` | POST | Add guest details to booking |
| `/bookings/{bookingId}` | GET | Get booking by ID |
| `/bookings/myBookings` | GET | Get my bookings (paginated) |
| `/bookings/{bookingId}/cancel` | POST | Cancel a booking |
| `/bookings/{bookingId}/payments` | POST | Initiate Stripe payment (returns session URL) |
| **Reviews** | | |
| `POST /hotels/{hotelId}/reviews` | POST | Add a review (must have confirmed booking) |
| **User Profile** | | |
| `/users/profile` | GET | Get my profile |
| `/users/profile` | PATCH | Update my profile |
| `/users/favorites` | GET | Get my favorite hotels |
| `/users/favorites/{hotelId}` | POST | Add hotel to favorites |
| `/users/favorites/{hotelId}` | DELETE | Remove hotel from favorites |

---

## HOTEL_MANAGER (Admin Only)

Requires login **and** role **HOTEL_MANAGER**. GUEST cannot access these:

| Endpoint | Method | Description |
|----------|--------|-------------|
| **Hotels** | | |
| `/admin/hotels` | POST | Create new hotel |
| `/admin/hotels` | GET | Get all hotels (owned by current user in practice) |
| `/admin/hotels/{hotelId}` | GET | Get hotel by ID |
| `/admin/hotels/{hotelId}` | PUT | Update hotel |
| `/admin/hotels/{hotelId}` | DELETE | Delete hotel |
| `/admin/hotels/{hotelId}` | PATCH | Toggle hotel active/inactive status |
| `/admin/hotels/{hotelId}/surge` | PATCH | Update surge pricing for date range |
| **Rooms** | | |
| `/admin/hotels/{hotelId}/rooms` | POST | Create new room |
| `/admin/hotels/{hotelId}/rooms` | GET | Get all rooms in hotel |
| `/admin/hotels/{hotelId}/rooms/{roomId}` | GET | Get room by ID |
| `/admin/hotels/{hotelId}/rooms/{roomId}` | DELETE | Delete room |
| **Promo Codes** | | |
| `/admin/promocodes` | POST | Create promo code |

---

## Summary Matrix

| Functionality | Public | GUEST | HOTEL_MANAGER |
|---------------|:------:|:-----:|:-------------:|
| Sign up (Guest) | ✅ | - | - |
| Sign up (Hotel Manager) | ✅ | - | - |
| Login / Logout / Refresh | ✅ | - | - |
| Search hotels | ✅ | - | - |
| View hotel info | ✅ | - | - |
| Initialize booking | - | ✅ | ✅ |
| Add guests to booking | - | ✅ | ✅ |
| View my bookings | - | ✅ | ✅ |
| Cancel booking | - | ✅ | ✅ |
| Initiate payment | - | ✅ | ✅ |
| Add review | - | ✅ | ✅ |
| View/update profile | - | ✅ | ✅ |
| Manage favorites | - | ✅ | ✅ |
| Create/update/delete hotel | - | ❌ | ✅ |
| Toggle hotel status | - | ❌ | ✅ |
| Update surge pricing | - | ❌ | ✅ |
| Create/delete rooms | - | ❌ | ✅ |
| Create promo codes | - | ❌ | ✅ |
| Upload image | ✅ | - | - |

---

## Notes

- **Context path**: All endpoints are prefixed with `/api/v1`
- **Ownership**: Hotel CRUD enforces ownership (only the owner can update/delete their hotels)
- **Reviews**: User must have a **confirmed** booking to add a review; one review per user per hotel
- **Bookings**: User can only access/modify their own bookings

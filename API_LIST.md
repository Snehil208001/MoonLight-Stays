# airBnbApp API List

Complete catalog of REST endpoints exposed by the primary backend (`airBnbApp/`, Spring Boot).

- **Base URL (local):** `http://localhost:8080/api/v1` (Android emulator: `http://10.0.2.2:8080/api/v1`)
- **Base URL (prod):** `https://moonlight-stays-backend-d6hga6dtg6c3cya2.centralindia-01.azurewebsites.net/api/v1`
- **Response envelope:** every successful response is wrapped by `GlobalResponseHandler` as `{"timeStamp": ..., "data": <payload>}`; errors arrive as `{"timeStamp": ..., "data": null, "error": {"status", "message", ...}}`. The Android app unwraps this via `ApiEnvelopeConverterFactory`.
- **Auth:** `Authorization: Bearer <accessToken>` JWT header. Roles: `GUEST`, `HOTEL_MANAGER`. `/admin/**` requires `HOTEL_MANAGER`.

## Auth — `AuthController` (`/auth`)

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 1 | POST | `/auth/signup` | Public | `{name, email, password}` | `UserDto {id, name, email, roles}` |
| 2 | POST | `/auth/admin/signup` | Public | `{name, email, password}` | `UserDto` (with `HOTEL_MANAGER` role) |
| 3 | POST | `/auth/login` | Public | `{email, password}` | `{accessToken}` + httpOnly `refreshToken` cookie (6 months) |
| 4 | POST | `/auth/refresh` | `refreshToken` cookie | — | `{accessToken}` |
| 5 | POST | `/auth/logout` | Bearer | — | 204, clears `refreshToken` cookie |

## Hotel browsing — `HotelBrowseController` (`/hotels`)

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 6 | GET | `/hotels/search` | Public | Query: `city, checkInDate, endDate, roomsCount, page, size` | `HotelPriceDto[] {hotel, price}` |
| 7 | POST | `/hotels/search` | Public | Body: `HotelSearchRequest {city, checkInDate, endDate, roomsCount, minPrice, maxPrice, roomType, amenity, page, size}` | `HotelPriceDto[]` |
| 8 | GET | `/hotels/Search` | Public | Legacy GET-with-body variant — avoid; use #6/#7 | `HotelPriceDto[]` |
| 9 | GET | `/hotels/{hotelId}/info` | Public | — | `HotelInfoDto {hotelDto, rooms[]}` |
| 10 | GET | `/hotels/{hotelId}/room-prices` | Public | Query: `checkIn, checkOut, roomsCount` (ISO dates) | `RoomPriceDto[] {roomId, pricePerNight, totalForStay}` |

## Reviews — `ReviewController` (`/hotels/{hotelId}/reviews`)

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 11 | POST | `/hotels/{hotelId}/reviews` | Bearer | `ReviewDto {rating, content}` | `ReviewDto` (201) |
| 12 | GET | `/hotels/{hotelId}/reviews` | Public | Query: `page, size` | `Page<ReviewDto>` |
| 13 | GET | `/hotels/{hotelId}/reviews/average` | Public | — | `Double` |

## Bookings — `HotelBookingController` (`/bookings`)

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 14 | POST | `/bookings/init` | Bearer | `BookingRequest {hotelId, roomId, checkInDate, checkOutDate, roomsCount, promoCode?}` | `BookingDto` |
| 15 | POST | `/bookings/{bookingId}/addGuests` | Bearer | `GuestDto[] {name, gender, age}` | `BookingDto` |
| 16 | GET | `/bookings/{bookingId}` | Bearer | — | `BookingDto` |
| 17 | GET | `/bookings/myBookings` | Bearer | Query: `page, size, status?` (repeatable, e.g. `CONFIRMED`) | `Page<BookingDto>` |
| 18 | POST | `/bookings/{bookingId}/cancel` | Bearer | — | 204 |
| 19 | POST | `/bookings/{bookingId}/payments` | Bearer | — | `{sessionUrl}` (Stripe Checkout URL) |

## Users — `UserController` (`/users`)

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 20 | GET | `/users/profile` | Bearer | — | `UserDto` |
| 21 | PATCH | `/users/profile` | Bearer | `ProfileUpdateDto {name}` | `UserDto` |
| 22 | GET | `/users/favorites` | Bearer | — | `HotelDto[]` |
| 23 | POST | `/users/favorites/{hotelId}` | Bearer | — | 204 |
| 24 | DELETE | `/users/favorites/{hotelId}` | Bearer | — | 204 |

## Promo codes — `PromoCodeController`

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 25 | GET | `/promocodes` | Public | — | `PromoCodeDto[] {code, discountPercentage}` |
| 26 | GET | `/promocodes/validate` | Public | Query: `code` | `{valid, code?, discountPercentage?}` |
| 27 | GET | `/admin/promocodes` | Manager | — | `PromoCode[]` (full entity) |
| 28 | POST | `/admin/promocodes` | Manager | `{code, discountPercentage, active?}` | `PromoCode` |
| 29 | DELETE | `/admin/promocodes/{id}` | Manager | — | 204 |

## Hotel admin — `HotelController` (`/admin/hotels`, manager-only)

| # | Method | Path | Request | Response (`data`) |
|---|---|---|---|---|
| 30 | POST | `/admin/hotels` | `HotelDto {name, city, photos[], amenities[], contactInfo}` | `HotelDto` (201) |
| 31 | GET | `/admin/hotels` | — | `HotelDto[]` (own hotels) |
| 32 | GET | `/admin/hotels/{hotelId}` | — | `HotelDto` |
| 33 | PUT | `/admin/hotels/{hotelId}` | `HotelDto` | `HotelDto` |
| 34 | DELETE | `/admin/hotels/{hotelId}` | — | 204 |
| 35 | PATCH | `/admin/hotels/{hotelId}` | — | 204 (activates hotel + creates inventory) |
| 36 | PATCH | `/admin/hotels/{hotelId}/surge` | `SurgeUpdateDto {surgeFactor, startDate, endDate}` | 204 |

## Room admin — `RoomAdminController` (`/admin/hotels/{hotelId}/rooms`, manager-only)

| # | Method | Path | Request | Response (`data`) |
|---|---|---|---|---|
| 37 | POST | `/admin/hotels/{hotelId}/rooms` | `RoomDto {types, basePrice, photos[], amenities[], totalCount, capacity}` | `RoomDto` (201) |
| 38 | GET | `/admin/hotels/{hotelId}/rooms` | — | `RoomDto[]` |
| 39 | GET | `/admin/hotels/{hotelId}/rooms/{roomId}` | — | `RoomDto` |
| 40 | PUT | `/admin/hotels/{hotelId}/rooms/{roomId}` | `RoomDto` | `RoomDto` |
| 41 | POST | `/admin/hotels/{hotelId}/rooms/{roomId}/update` | `RoomDto` (POST alias of #40) | `RoomDto` |
| 42 | DELETE | `/admin/hotels/{hotelId}/rooms/{roomId}` | — | 204 |

## Uploads & webhooks

| # | Method | Path | Auth | Request | Response (`data`) |
|---|---|---|---|---|---|
| 43 | POST | `/upload/image` | Bearer | multipart form, part name `file` | `{url: "/images/<name>"}` — served under `/images/**` |
| 44 | POST | `/webhooks/payment` | Stripe signature | Stripe event payload | Server-to-server only (Stripe → backend); not a client API |

## Android integration map

All client-relevant endpoints are integrated in `app/src/main/java/com/snehil/moon_stays_androidapp/`:

| Endpoints | Retrofit service | Repository | Use cases |
|---|---|---|---|
| 1–5, 20–24 | `data/remote/AuthApiService.kt` | `AuthRepositoryImpl` | `LoginUseCase`, `SignUpUseCase`, … |
| 7, 9–13, 25–26 | `data/remote/HotelApiService.kt` | `HotelRepositoryImpl` | `SearchHotelsUseCase`, `GetHotelInfoUseCase`, `GetReviewsUseCase`, `AddReviewUseCase`, `GetActivePromoCodesUseCase`, `ValidatePromoCodeUseCase` |
| 14–19 | `data/remote/BookingApiService.kt` | `BookingRepositoryImpl` | `BookRoomUseCase`, `GetMyBookingsUseCase`, `CancelBookingUseCase` |
| 27–43 | `data/remote/AdminApiService.kt` | `AdminRepositoryImpl` | `UploadImageUseCase` |

Notes:
- Token refresh (#4) runs transparently: `SessionCookieJar` keeps the httpOnly `refreshToken` cookie from login, and `TokenAuthenticator` (`core/common/`) calls `/auth/refresh` on any 401 and retries the request once. If refresh fails, `AuthInterceptor` clears the session.
- #6 and #8 are redundant with #7 (the app uses the POST search). #44 is Stripe-only.
- `data/remote/LegacyApiService.kt` / `ModernApiService.kt` and everything under `data/network/` belong to the decoupled multi-backend layer and do **not** target `airBnbApp` paths.

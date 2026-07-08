# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

Moonlight Stays — an Airbnb-style hotel booking platform. This is a monorepo with four distinct codebases:

| Directory | What it is | Build tool |
|---|---|---|
| `airBnbApp/` | **Primary backend** — Spring Boot 3.5, Java 17. This is what gets deployed. | Maven |
| `moonlight-stays/` | Next.js 14 frontend (App Router, TypeScript, Redux Toolkit, Tailwind) | npm |
| `app/` (+ root `build.gradle.kts`, `gradlew`) | Android client — Kotlin, Jetpack Compose, Hilt. The **root Gradle project is the Android app** (`Moon-Stays-AndroidApp`), not the backend. | Gradle |
| `moonlight-stays-backend/` | A separate, alternate Spring Boot backend (Gradle, own Dockerfile/docker-compose). **Not** the deployed backend — don't confuse it with `airBnbApp/`. | Gradle |

## Commands

```bash
# Full local dev stack (backend + frontend + stripe webhook listener) from repo root
npm install
npm run dev            # or .\start-dev.ps1 to open 3 PowerShell windows

# Backend only
cd airBnbApp && mvn spring-boot:run     # runs on http://localhost:8080/api/v1

# Backend tests / build
cd airBnbApp && mvn test
cd airBnbApp && mvn test -Dtest=ClassName#methodName   # single test
cd airBnbApp && mvn clean package -DskipTests

# Frontend
cd moonlight-stays && npm run dev       # http://localhost:3000
cd moonlight-stays && npm run build
cd moonlight-stays && npm run lint

# Android (from repo root)
.\gradlew assembleDebug

# Smoke-test the deployed Azure API
.\test-api.ps1
```

Local backend expects PostgreSQL at `localhost:5432/airBnb` (user `postgres`); credentials and other secrets come from env vars with fallbacks in `airBnbApp/src/main/resources/application.properties` (`DB_PASSWORD`, `JWT_SECRET_KEY`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `MAIL_USERNAME`/`MAIL_PASSWORD`). Sample data seeding is controlled by `app.seed.enabled`.

## Architecture

### Backend (`airBnbApp/`)

Package root: `com.moonlight.project.airBnbApp`. Standard layering: `controller` → `service` (interface + `*Impl`) → `repository` → `entity`, with DTOs in `dto`.

- **Context path is `/api/v1`** (set in application.properties) — all endpoints live under it, including Swagger at `/api/v1/swagger-ui.html`.
- **Response envelope**: `advice/GlobalResponseHandler` wraps every controller response in `ApiResponse` — clients read payloads from `response.data`. Errors go through `advice/GlobalExceptionHandler` → `ApiError`.
- **Dynamic pricing** (`strategy/`): decorator-chained Strategy pattern. `PricingService` wraps `BasePricingStrategy` with Surge → Occupancy → Urgency → Holiday strategies. `HotelMinPrice` caches the cheapest daily price per hotel for search results; `PricingUpdateService` keeps it updated.
- **Inventory model**: `Inventory` is one row per room per date (availability + surge factor). Booking flow: `POST /bookings/init` reserves inventory → guest details → `POST /bookings/{id}/payments` creates a Stripe Checkout session → Stripe webhook (`WebhookController`, `/webhooks/payment`) confirms the booking. `BookingCleanupService` expires unpaid bookings.
- **Auth** (`security/`): JWT access + refresh tokens, roles `GUEST` and `HOTEL_MANAGER`. `JWTAuthFilter` + `WebSecurityConfig`; `/admin/**` endpoints are manager-only. See `ROLE_BASED_FUNCTIONALITY.md` for the endpoint-by-role breakdown.
- **Image uploads**: stored on the local filesystem (`uploads/`), served under `/images/**` via `FileSystemStorageService`.

### Frontend (`moonlight-stays/`)

- `src/lib/api.ts` is the single API client; state in Redux Toolkit (`src/store/`), auth in `authSlice`.
- **CORS is avoided by proxying**: `next.config.js` rewrites `/api/v1/*` and `/images/*` to the backend origin (from `NEXT_PUBLIC_API_URL`, default `http://localhost:8080`). Frontend code calls relative `/api/v1/...` paths.
- Create `moonlight-stays/.env.local` with `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1` (see `.env.local.example`).

### Payments (local dev)

Stripe webhooks require `stripe listen --forward-to localhost:8080/api/v1/webhooks/payment` (included in `npm run dev`). Real checkout completions fire the webhook automatically; `stripe trigger` only produces fake events that won't confirm real bookings (see `DEV_SETUP.md`).

## Cross-platform design system (mandatory)

`design-system/tokens.json` + `design-system/DESIGN_SYSTEM.md` are the single source of truth for ALL UI on both clients. The web app and Android app must look like the same product ("Midnight Glassmorphism": midnight `#0A0A1A` backgrounds, glass surfaces, cyan `#00FFFF` primary accent, coral `#FF7F50` secondary, Plus Jakarta Sans).

- **Never hardcode** colors, font sizes, radii, spacing, durations, or easings in screen code. Web: use the Tailwind tokens / CSS vars (`tailwind.config.ts`, `globals.css`). Android: use `ui/theme/` (`Color.kt`, `Type.kt`, `Dimens.kt`, `Motion.kt`, `Theme.kt`).
- Changing a token means updating `tokens.json` **and** both platform implementations in the same commit.
- New UI components need a spec in `DESIGN_SYSTEM.md` §5 and visually equivalent implementations on both platforms; screen names and navigation order must match across platforms (§6).

## Deployment

Push to `main` triggers `.github/workflows/azure-deploy.yml`: builds the **root `Dockerfile`** (which packages `airBnbApp/` with Maven), pushes to Azure Container Registry (`moonlightstaysacr`), and deploys to Azure App Service (`moonlight-stays-backend`, Central India). Production runs with `SPRING_PROFILES_ACTIVE=prod` (`application-prod.properties`) — all config via App Service environment variables (`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `JWT_SECRET_KEY`, etc.); seeding is off in prod.

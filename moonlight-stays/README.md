# Moonlight Stays — Frontend

Ethereal hotel booking interface for the AirBnb Clone backend.

## Tech Stack

- **Framework:** Next.js 14 (React)
- **Styling:** Tailwind CSS
- **Animations:** Framer Motion
- **Icons:** Lucide React

## Design System

- **Theme:** Ethereal Glassmorphism & Depth
- **Mode:** Dark-mode first
- **Background:** Mesh gradient (midnight blues & purples)
- **Surfaces:** Glassmorphism with `backdrop-filter: blur(16px)`
- **Accent:** Electric Cyan (#00FFFF)

## Getting Started

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Configure API URL** (optional):
   Create `.env.local`:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
   ```
   Default is `http://localhost:8080/api/v1`.

3. **Run the backend** (Spring Boot) on port 8080.

4. **Run the frontend:**
   ```bash
   npm run dev
   ```
   Open [http://localhost:3000](http://localhost:3000).

## Flow

1. **Splash Screen** — 2.5s intro with logo and "Curating premium stays..."
2. **Onboarding** — 3-step carousel (Discover, Smart Pricing, Seamless Escapes)
3. **Landing** — Hero search + hotel grid with 3D tilt cards
4. **Booking** — Modal with promo code → Stripe checkout

## Backend Integration

- `POST /hotels/search` — Search hotels
- `GET /hotels/{id}/info` — Hotel details + rooms
- `POST /auth/signup` — Register
- `POST /auth/login` — Login (JWT)
- `POST /bookings/init` — Create booking
- `POST /bookings/{id}/payments` — Get Stripe session URL

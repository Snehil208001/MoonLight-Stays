# Moonlight Stays

**Airbnb-style hotel booking platform** — Full-stack application with Spring Boot backend, Next.js frontend, Stripe payments, and AWS deployment.

---

## Live Demo

| | URL |
|---|---|
| **Frontend** | [https://main.d30tl6vi1qydms.amplifyapp.com](https://main.d30tl6vi1qydms.amplifyapp.com) |
| **Backend API** | [http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1](http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1) |
| **API Docs (Swagger)** | [http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html](http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html) |

---

## Features

### For Guests
- **Search hotels** — Filter by city, dates, rooms, price range, amenities
- **Hotel details** — Photos, amenities, room types, dynamic pricing
- **Bookings** — Multi-step flow with guest details and promo codes
- **Stripe payments** — Secure checkout integration
- **Reviews & ratings** — Add and view hotel reviews
- **Favorites** — Save hotels for later
- **My Bookings** — View, filter, and cancel bookings

### For Hotel Managers
- **Admin dashboard** — Manage hotels, rooms, and promo codes
- **CRUD operations** — Create, update, delete hotels and rooms
- **Surge pricing** — Set dynamic pricing for date ranges
- **Image uploads** — Add hotel and room photos
- **Promo codes** — Create and manage discount codes

---

## Tech Stack

| Layer | Technologies |
|-------|--------------|
| **Frontend** | Next.js 14, React 18, TypeScript, Tailwind CSS, Framer Motion, Redux Toolkit |
| **Backend** | Spring Boot 3.5, Java 17, Spring Data JPA, Spring Security (JWT) |
| **Database** | PostgreSQL |
| **Payments** | Stripe Checkout |
| **Deployment** | AWS Amplify (frontend), AWS Elastic Beanstalk (backend), AWS RDS (database) |

---

## Architecture

```
┌─────────────────┐     HTTPS      ┌──────────────────┐     HTTP      ┌─────────────────────┐
│   AWS Amplify   │ ◄─────────────► │  Next.js (proxy)  │ ◄───────────► │ Elastic Beanstalk   │
│   (Frontend)    │                 │  /api/v1 rewrites │               │ (Spring Boot API)   │
└─────────────────┘                 └──────────────────┘               └──────────┬──────────┘
                                                                                    │
                                                                                    ▼
                                                                          ┌─────────────────────┐
                                                                          │   AWS RDS            │
                                                                          │   (PostgreSQL)       │
                                                                          └─────────────────────┘
```

- **Frontend** → Served over HTTPS from Amplify
- **Next.js rewrites** → Proxy `/api/v1` requests to backend (avoids mixed content)
- **Backend** → Spring Boot on Elastic Beanstalk
- **Database** → PostgreSQL on RDS

---

## Project Structure

```
├── airBnbApp/                 # Spring Boot backend
│   ├── src/main/java/         # Controllers, services, entities, security
│   └── pom.xml
├── moonlight-stays/           # Next.js frontend
│   ├── src/
│   │   ├── app/               # Pages and layouts
│   │   ├── components/        # React components
│   │   └── lib/               # API client, utilities
│   └── package.json
├── amplify.yml                # AWS Amplify build config
├── AWS_DEPLOYMENT.md          # Deployment guide
└── README.md
```

---

## Quick Start (Local Development)

### Prerequisites
- Node.js 18+, npm
- Java 17, Maven
- PostgreSQL (or use H2 for dev)
- [Stripe CLI](https://stripe.com/docs/stripe-cli) (for payment webhooks)

### Run everything

```bash
# From project root
npm install
npm run dev
```

This starts:
- **Backend** — `http://localhost:8080`
- **Frontend** — `http://localhost:3000`
- **Stripe webhook listener** — For local payment testing

### Run individually

```bash
# Backend only
cd airBnbApp && mvn spring-boot:run

# Frontend only
cd moonlight-stays && npm run dev

# Stripe webhooks (for payments)
stripe listen --forward-to localhost:8080/api/v1/webhooks/payment
```

### Environment variables

Create `moonlight-stays/.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

---

## API Overview

| Area | Key Endpoints |
|------|---------------|
| **Auth** | `POST /auth/signup`, `POST /auth/login`, `POST /auth/refresh` |
| **Hotels** | `POST /hotels/search`, `GET /hotels/{id}/info` |
| **Bookings** | `POST /bookings/init`, `POST /bookings/{id}/payments` |
| **Admin** | `POST /admin/hotels`, `GET /admin/hotels`, `PATCH /admin/hotels/{id}/surge` |

Full API documentation: [Swagger UI](http://moonlight-stays.ap-south-1.elasticbeanstalk.com/api/v1/swagger-ui.html)

---

## Documentation

| Document | Description |
|----------|-------------|
| [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) | Step-by-step AWS deployment (RDS, Elastic Beanstalk, Amplify) |
| [DEV_SETUP.md](./DEV_SETUP.md) | Local development setup |
| [ROLE_BASED_FUNCTIONALITY.md](./ROLE_BASED_FUNCTIONALITY.md) | API endpoints by user role |
| [API_INTEGRATION_PLAN.md](./API_INTEGRATION_PLAN.md) | API integration details |

---

## Highlights for Recruiters

- **Full-stack** — End-to-end ownership from UI to database
- **Production deployment** — Live on AWS (Amplify + Elastic Beanstalk + RDS)
- **Modern stack** — Spring Boot 3, Next.js 14, TypeScript
- **Payments** — Stripe Checkout with webhook integration
- **Auth** — JWT with refresh tokens, role-based access (Guest / Hotel Manager)
- **Responsive UI** — Dark-mode glassmorphism design, Framer Motion animations

---

## License

Private project — All rights reserved.
